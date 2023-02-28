/**
 * Copyright (c) 2014, the Railo Company Ltd.
 * Copyright (c) 2015, Lucee Assosication Switzerland
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
package lucee.transformer.cfml.evaluator.impl

import java.io.ByteArrayInputStream

/**
 *
 */
class Import : EvaluatorSupport() {
    @Override
    @Throws(EvaluatorException::class)
    fun evaluate(tag: Tag?, libTag: TagLibTag?) {
    }

    @Override
    @Throws(TemplateException::class)
    fun execute(config: Config?, tag: Tag?, libTag: TagLibTag?, flibs: Array<FunctionLib?>?, data: Data?): TagLib? {
        val ti: TagImport? = tag as TagImport?
        val p: Attribute = tag.getAttribute("prefix")
        val t: Attribute = tag.getAttribute("taglib")
        val path: Attribute = tag.getAttribute("path")
        if (p != null || t != null) {
            if (p == null) throw TemplateException(data.srcCode, "Wrong Context, missing attribute [prefix] for tag [" + tag.getFullname().toString() + "]")
            if (t == null) throw TemplateException(data.srcCode, "Wrong Context, missing attribute [taglib] for tag [" + tag.getFullname().toString() + "]")
            if (path != null) throw TemplateException(data.srcCode, "Wrong context, you have an invalid combination of attributes for the tag [" + tag.getFullname().toString() + "], " + "you cannot mix attribute [path] with attributes [taglib] and [prefix]")
            return executePT(config, tag, libTag, flibs, data.srcCode)
        }
        if (path == null) throw TemplateException(data.srcCode, "Wrong context, you have an invalid combination of attributes for the tag [" + tag.getFullname().toString() + "], " + "you need to define the attributes [prefix] and [taglib], the attribute [path] or simply define an attribute value")
        val strPath: String = ASMUtil.getAttributeString(tag, "path", null)
                ?: throw TemplateException(data.srcCode, "attribute [path] must be a constant value")
        ti.setPath(strPath)
        return null
    }

    @Throws(TemplateException::class)
    private fun executePT(config: Config?, tag: Tag?, libTag: TagLibTag?, flibs: Array<FunctionLib?>?, sc: SourceCode?): TagLib? {

        // Attribute prefix
        var nameSpace: String? = ASMUtil.getAttributeString(tag, "prefix", null)
                ?: throw TemplateException(sc, "attribute [prefix] must be a constant value")
        nameSpace = nameSpace.trim()
        val nameSpaceSeparator = if (StringUtil.isEmpty(nameSpace)) "" else ":"

        // Attribute taglib
        var textTagLib: String? = ASMUtil.getAttributeString(tag, "taglib", null)
                ?: throw TemplateException(sc, "attribute [taglib] must be a constant value")
        textTagLib = textTagLib.replace('\\', '/')
        textTagLib = ConfigWebUtil.replacePlaceholder(textTagLib, config)
        // File TagLib
        val ext: String = ResourceUtil.getExtension(textTagLib, null)
        val hasTldExtension = "tld".equalsIgnoreCase(ext) || "tldx".equalsIgnoreCase(ext)
        val absFile: Resource = config.getResource(textTagLib)
        // TLD
        if (absFile.isFile()) return _executeTLD(config, absFile, nameSpace, nameSpaceSeparator, sc)
        // CTD
        // else if(absFile.isDirectory()) return _executeCTD(absFile,textPrefix);

        // Second Change
        if (textTagLib.startsWith("/")) {
            // config.getPhysical(textTagLib);
            val ps: PageSource = config.getPageSourceExisting(null, null, textTagLib, false, false, true, false)

            // config.getConfigDir()
            if (ps != null) {
                if (ps.physcalExists()) {
                    val file: Resource = ps.getPhyscalFile()
                    // TLD
                    if (file.isFile()) return _executeTLD(config, file, nameSpace, nameSpaceSeparator, sc)
                }
                // CTD
                if (!hasTldExtension) return _executeCTD(textTagLib, nameSpace, nameSpaceSeparator)
            }
        } else {
            val ps: PageSource? = if (sc is PageSourceCode) (sc as PageSourceCode?).getPageSource() else null
            val sourceFile: Resource? = if (ps == null) null else ps.getPhyscalFile()
            if (sourceFile != null) {
                val file: Resource = sourceFile.getParentResource().getRealResource(textTagLib)
                // TLD
                if (file.isFile()) return _executeTLD(config, file, nameSpace, nameSpaceSeparator, sc)
                // CTD
                if (!hasTldExtension) return _executeCTD(textTagLib, nameSpace, nameSpaceSeparator)
            }
        }
        throw TemplateException(sc, "invalid definition of the attribute taglib [$textTagLib]")
    }

    /**
     * @param fileTagLib
     * @return
     * @throws EvaluatorException
     */
    @Throws(TemplateException::class)
    private fun _executeTLD(config: Config?, fileTagLib: Resource?, nameSpace: String?, nameSpaceSeparator: String?, cfml: SourceCode?): TagLib? {
        // change extesnion
        var fileTagLib: Resource? = fileTagLib
        val ext: String = ResourceUtil.getExtension(fileTagLib, null)
        if ("jar".equalsIgnoreCase(ext)) {
            // check anchestor file
            val newFileTagLib: Resource = ResourceUtil.changeExtension(fileTagLib, "tld")
            if (newFileTagLib.exists()) fileTagLib = newFileTagLib else {
                val tmp: Resource? = getTLDFromJarAsFile(config, fileTagLib)
                if (tmp != null) fileTagLib = tmp
            }
        }
        return try {
            val taglib: TagLib = TagLibFactory.loadFromFile(fileTagLib, config.getIdentification())
            taglib.setNameSpace(nameSpace)
            taglib.setNameSpaceSeperator(nameSpaceSeparator)
            taglib
        } catch (e: TagLibException) {
            throw TemplateException(cfml, e.getMessage())
        }
    }

    private fun getTLDFromJarAsFile(config: Config?, jarFile: Resource?): Resource? {
        val jspTagLibDir: Resource = config.getTempDirectory().getRealResource("jsp-taglib")
        if (!jspTagLibDir.exists()) jspTagLibDir.mkdirs()
        var filename: String? = null
        try {
            filename = Md5.getDigestAsString(ResourceUtil.getCanonicalPathEL(jarFile) + jarFile.lastModified())
        } catch (e: IOException) {
        }
        var tldFile: Resource = jspTagLibDir.getRealResource(filename.toString() + ".tld")
        if (tldFile.exists()) return tldFile
        tldFile = jspTagLibDir.getRealResource(filename.toString() + ".tldx")
        if (tldFile.exists()) return tldFile
        val barr = getTLDFromJarAsBarr(config, jarFile) ?: return null
        try {
            IOUtil.copy(ByteArrayInputStream(barr), tldFile, true)
        } catch (e: IOException) {
        }
        return tldFile
    }

    private fun getTLDFromJarAsBarr(c: Config?, jarFile: Resource?): ByteArray? {
        var zis: ZipInputStream? = null
        try {
            zis = ZipInputStream(IOUtil.toBufferedInputStream(jarFile.getInputStream()))
            val buffer = ByteArray(0xffff)
            var bytes_read: Int
            var ze: ZipEntry?
            val barr: ByteArray
            while (zis.getNextEntry().also { ze = it } != null) {
                if (!ze.isDirectory() && (StringUtil.endsWithIgnoreCase(ze.getName(), ".tld") || StringUtil.endsWithIgnoreCase(ze.getName(), ".tldx"))) {
                    LogUtil.log(c, lucee.commons.io.log.Log.LEVEL_INFO, Import::class.java.getName(), "found tld in file [" + jarFile + "] at position " + ze.getName())
                    val baos = ByteArrayOutputStream()
                    while (zis.read(buffer).also { bytes_read = it } != -1) baos.write(buffer, 0, bytes_read)
                    // String name = ze.getName().replace('\\', '/');
                    barr = baos.toByteArray()
                    zis.closeEntry()
                    baos.close()
                    return barr
                }
            }
        } catch (ioe: IOException) {
        } finally {
            IOUtil.closeEL(zis)
        }
        return null
    }

    /**
     * @param textTagLib
     * @param nameSpace
     * @return
     */
    private fun _executeCTD(textTagLib: String?, nameSpace: String?, nameSpaceSeparator: String?): TagLib? {
        return CustomTagLib(textTagLib, nameSpace, nameSpaceSeparator)
    }
}