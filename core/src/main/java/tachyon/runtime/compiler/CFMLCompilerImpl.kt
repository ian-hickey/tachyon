/**
 * Copyright (c) 2014, the Railo Company Ltd.
 * Copyright (c) 2015, Tachyon Assosication Switzerland
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library.  If not, see <http:></http:>//www.gnu.org/licenses/>.
 *
 */
package tachyon.runtime.compiler

import java.io.ByteArrayInputStream

/**
 * CFML Compiler compiles CFML source templates
 */
class CFMLCompilerImpl : CFMLCompiler {
    private val cfmlTransformer: CFMLTransformer?
    private val watched: ConcurrentLinkedQueue<WatchEntry?>? = ConcurrentLinkedQueue<WatchEntry?>()
    @Throws(TemplateException::class, IOException::class)
    fun compile(config: ConfigPro?, ps: PageSource?, tld: Array<TagLib?>?, fld: Array<FunctionLib?>?, classRootDir: Resource?, returnValue: Boolean, ignoreScopes: Boolean): Result? {
        return _compile(config, ps, null, null, tld, fld, classRootDir, returnValue, ignoreScopes)
    }

    @Throws(TemplateException::class, IOException::class)
    fun compile(config: ConfigPro?, sc: SourceCode?, tld: Array<TagLib?>?, fld: Array<FunctionLib?>?, classRootDir: Resource?, className: String?, returnValue: Boolean, ignoreScopes: Boolean): Result? {

        // just to be sure
        val ps: PageSource? = if (sc is PageSourceCode) (sc as PageSourceCode?).getPageSource() else null
        return _compile(config, ps, sc, className, tld, fld, classRootDir, returnValue, ignoreScopes)
    }

    @Throws(TemplateException::class, IOException::class)
    private fun _compile(config: ConfigPro?, ps: PageSource?, sc: SourceCode?, className: String?, tld: Array<TagLib?>?, fld: Array<FunctionLib?>?, classRootDir: Resource?, returnValue: Boolean,
                         ignoreScopes: Boolean): Result? {
        var className = className
        val javaName: String?
        if (className == null) {
            javaName = ListUtil.trim(ps.getJavaName(), "\\/", false)
            className = ps.getClassName()
        } else {
            javaName = className.replace('.', '/')
        }
        var result: Result? = null
        // byte[] barr = null;
        var page: Page? = null
        val factory: Factory = BytecodeFactory.getInstance(config)
        return try {
            page = if (sc == null) cfmlTransformer.transform(factory, config, ps, tld, fld, returnValue, ignoreScopes) else cfmlTransformer.transform(factory, config, sc, tld, fld, System.currentTimeMillis(),
                    sc.getDialect() === CFMLEngine.DIALECT_CFML && config.getDotNotationUpperCase(), returnValue, ignoreScopes)
            page.setSplitIfNecessary(false)
            try {
                val barr: ByteArray = page.execute(className)
                result = Result(page, barr, page.getJavaFunctions())
            } catch (re: RuntimeException) {
                val msg: String = StringUtil.emptyIfNull(re.getMessage())
                if (StringUtil.indexOfIgnoreCase(msg, "Method code too large!") !== -1) {
                    page = if (sc == null) cfmlTransformer.transform(factory, config, ps, tld, fld, returnValue, ignoreScopes) else cfmlTransformer.transform(factory, config, sc, tld, fld, System.currentTimeMillis(),
                            sc.getDialect() === CFMLEngine.DIALECT_CFML && config.getDotNotationUpperCase(), returnValue, ignoreScopes)
                    page.setSplitIfNecessary(true)
                    val barr: ByteArray = page.execute(className)
                    result = Result(page, barr, page.getJavaFunctions())
                } else throw re
            } catch (cfe: ClassFormatError) {
                val msg: String = StringUtil.emptyIfNull(cfe.getMessage())
                if (StringUtil.indexOfIgnoreCase(msg, "Invalid method Code length") !== -1) {
                    page = if (ps != null) cfmlTransformer.transform(factory, config, ps, tld, fld, returnValue, ignoreScopes) else cfmlTransformer.transform(factory, config, sc, tld, fld, System.currentTimeMillis(),
                            sc.getDialect() === CFMLEngine.DIALECT_CFML && config.getDotNotationUpperCase(), returnValue, ignoreScopes)
                    page.setSplitIfNecessary(true)
                    val barr: ByteArray = page.execute(className)
                    result = Result(page, barr, page.getJavaFunctions())
                } else throw cfe
            }

            // store
            if (classRootDir != null) {
                val classFile: Resource = classRootDir.getRealResource(page.getClassName().toString() + ".class")
                val classFileDirectory: Resource = classFile.getParentResource()
                if (!classFileDirectory.exists()) classFileDirectory.mkdirs() else if (classFile.exists() && !SystemUtil.isWindows()) {
                    val prefix: String = page.getClassName().toString() + "$"
                    classRootDir.list(object : ResourceNameFilter() {
                        @Override
                        fun accept(parent: Resource?, name: String?): Boolean {
                            if (name.startsWith(prefix)) parent.getRealResource(name).delete()
                            return false
                        }
                    })
                }
                IOUtil.copy(ByteArrayInputStream(result!!.barr), classFile, true)
                if (result!!.javaFunctions != null) {
                    for (jf in result.javaFunctions!!) {
                        IOUtil.copy(ByteArrayInputStream(jf.byteCode), classFileDirectory.getRealResource(jf.getName().toString() + ".class"), true)
                    }
                }
                /// TODO; //store java functions
            }
            result
        } catch (ace: AlreadyClassException) {
            val bytes = if (ace.getEncrypted()) readEncrypted(ace) else readPlain(ace)
            result = Result(null, bytes, null) // TODO handle better Java Functions
            val displayPath = if (ps != null) "[" + ps.getDisplayPath().toString() + "] " else ""
            val srcName: String = ASMUtil.getClassName(result!!.barr)
            val dialect: Int = if (sc == null) ps.getDialect() else sc.getDialect()
            // source is cfm and target cfc
            if (dialect == CFMLEngine.DIALECT_CFML && endsWith(srcName, Constants.getCFMLTemplateExtensions(), dialect) && className
                            .endsWith("_" + Constants.getCFMLComponentExtension() + if (dialect == CFMLEngine.DIALECT_CFML) Constants.CFML_CLASS_SUFFIX else Constants.LUCEE_CLASS_SUFFIX)) {
                throw TemplateException("Source file [$displayPath] contains the bytecode for a regular cfm template not for a component")
            }
            // source is cfc and target cfm
            if (dialect == CFMLEngine.DIALECT_CFML && srcName.endsWith(
                            "_" + Constants.getCFMLComponentExtension() + if (dialect == CFMLEngine.DIALECT_CFML) Constants.CFML_CLASS_SUFFIX else Constants.LUCEE_CLASS_SUFFIX)
                    && endsWith(className, Constants.getCFMLTemplateExtensions(), dialect)) throw TemplateException("Source file [$displayPath] contains a component not a regular cfm template")

            // rename class name when needed
            if (!srcName.equals(javaName)) {
                val barr: ByteArray = ClassRenamer.rename(result.barr, javaName)
                if (barr != null) result = Result(result.page, barr, null) // TODO handle java functions
            }
            // store
            if (classRootDir != null) {
                val classFile: Resource = classRootDir.getRealResource(javaName.toString() + ".class")
                val classFileDirectory: Resource = classFile.getParentResource()
                if (!classFileDirectory.exists()) classFileDirectory.mkdirs()
                result = Result(result.page, Page.setSourceLastModified(result.barr, if (ps != null) ps.getPhyscalFile().lastModified() else System.currentTimeMillis()), null) // TODO
                // handle
                // java
                // functions
                IOUtil.copy(ByteArrayInputStream(result.barr), classFile, true)
            }
            result
        } catch (bce: TransformerException) {
            val pos: Position = bce.getPosition()
            val line = if (pos == null) -1 else pos.line
            val col = if (pos == null) -1 else pos.column
            if (ps != null) bce.addContext(ps, line, col, null)
            throw bce
        }
    }

    @Throws(IOException::class)
    private fun readPlain(ace: AlreadyClassException?): ByteArray? {
        return IOUtil.toBytes(ace.getInputStream(), true)
    }

    @Throws(IOException::class)
    private fun readEncrypted(ace: AlreadyClassException?): ByteArray? {
        var str: String = System.getenv("PUBLIC_KEY")
        if (str == null) str = System.getProperty("PUBLIC_KEY")
        if (str == null) throw RuntimeException("To decrypt encrypted bytecode, you need to set PUBLIC_KEY as system property or as an environment variable")
        var bytes: ByteArray = IOUtil.toBytes(ace.getInputStream(), true)
        bytes = try {
            val publicKey: PublicKey = RSA.toPublicKey(str)
            // first 2 bytes are just a mask to detect encrypted code, so we need to set offset 2
            RSA.decrypt(bytes, publicKey, 2)
        } catch (ioe: IOException) {
            throw ioe
        } catch (e: Exception) {
            throw RuntimeException(e)
        }
        return bytes
    }

    private fun endsWith(name: String?, extensions: Array<String?>?, dialect: Int): Boolean {
        for (i in extensions.indices) {
            if (name.endsWith("_" + extensions!![i] + if (dialect == CFMLEngine.DIALECT_CFML) Constants.CFML_CLASS_SUFFIX else Constants.LUCEE_CLASS_SUFFIX)) return true
        }
        return false
    }

    @Throws(TemplateException::class, IOException::class)
    fun transform(config: ConfigPro?, source: PageSource?, tld: Array<TagLib?>?, fld: Array<FunctionLib?>?, returnValue: Boolean, ignoreScopes: Boolean): Page? {
        return cfmlTransformer.transform(BytecodeFactory.getInstance(config), config, source, tld, fld, returnValue, ignoreScopes)
    }

    inner class Result(page: Page?, barr: ByteArray?, javaFunctions: List<JavaFunction?>?) {
        val page: Page?
        val barr: ByteArray?
        val javaFunctions: List<JavaFunction?>?

        init {
            this.page = page
            this.barr = barr
            this.javaFunctions = javaFunctions
        }
    }

    fun watch(ps: PageSource?, now: Long) {
        watched.offer(WatchEntry(ps, now, ps.getPhyscalFile().length(), ps.getPhyscalFile().lastModified()))
    }

    fun checkWatched() {
        var we: WatchEntry?
        val now: Long = System.currentTimeMillis()
        val tmp: Stack<WatchEntry?> = Stack<WatchEntry?>()
        while (watched.poll().also { we = it } != null) {
            // to young
            if (we!!.now + 1000 > now) {
                tmp.add(we)
                continue
            }
            if (we!!.length != we!!.ps.getPhyscalFile().length() && we!!.ps.getPhyscalFile().length() > 0) { // TODO this is set to avoid that removed files are removed from pool, remove
                // this line if a UDF still wprks fine when the page is gone
                (we!!.ps as PageSourceImpl?).flush()
            }
        }

        // add again entries that was to young for next round
        val it: Iterator<WatchEntry?> = tmp.iterator()
        while (it.hasNext()) {
            watched.add(it.next().also { we = it })
        }
    }

    private inner class WatchEntry(ps: PageSource?, now: Long, length: Long, lastModified: Long) {
        val ps: PageSource?
        val now: Long
        val length: Long
        private val lastModified: Long

        init {
            this.ps = ps
            this.now = now
            this.length = length
            this.lastModified = lastModified
        }
    }

    /**
     * Constructor of the compiler
     *
     * @param config
     */
    init {
        cfmlTransformer = CFMLTransformer()
    }
}