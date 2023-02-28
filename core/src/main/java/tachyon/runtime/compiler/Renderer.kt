/**
 * Copyright (c) 2015, Tachyon Assosication Switzerland. All rights reserved.
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

import java.io.IOException

object Renderer {
    private const val MAX_SIZE = (1024 * 1024).toLong()
    private var mcl: MemoryClassLoader? = null
    private val pages: Map<String?, Page?>? = HashMap<String?, Page?>()
    @Throws(Exception::class)
    private fun loadClass(config: ConfigWebPro?, className: String?, cfml: String?, dialect: Int, ignoreScopes: Boolean): Class<out Page?>? {
        val compiler: CFMLCompilerImpl = config.getCompiler()
        // create className based o the content
        var clazz: Class<out Page?>? = null
        if (mcl == null) {
            mcl = createMemoryClassLoader(config)
        } else clazz = ClassUtil.loadClass(mcl, className, null)
        if (clazz != null) return clazz
        val sc = SourceCode(null, cfml, false, dialect)

        // compile
        val result: tachyon.runtime.compiler.CFMLCompilerImpl.Result = compiler!!.compile(config, sc, config.getTLDs(dialect), config.getFLDs(dialect), null, className, true, ignoreScopes)

        // before we add a new class, we make sure we are still in range
        if (mcl.getSize() + result!!.barr.length > MAX_SIZE) {
            mcl = createMemoryClassLoader(config)
            pages.clear()
        }
        return mcl.loadClass(className, result!!.barr) as Class<out Page?>
    }

    @Throws(IOException::class)
    private fun createMemoryClassLoader(cw: ConfigWeb?): MemoryClassLoader? {
        return MemoryClassLoader(cw, cw.getClass().getClassLoader())
    }

    @Throws(Exception::class)
    private fun loadPage(cw: ConfigWebPro?, ps: PageSource?, cfml: String?, dialect: Int, ignoreScopes: Boolean): Page? {
        val className: String = HashUtil.create64BitHashAsString(cfml)

        // do we already have the page?
        var p: Page? = pages!![className]
        if (p != null) return p

        // load class
        val constr: Constructor<out Page?> = loadClass(cw, className, cfml, dialect, ignoreScopes).getDeclaredConstructor(PageSource::class.java)
        p = constr.newInstance(ps)
        pages.put(className, p)
        return p
    }

    @Throws(PageException::class)
    fun script(pc: PageContext?, cfml: String?, dialect: Int, catchOutput: Boolean, ignoreScopes: Boolean): Result? {
        val prefix: String = (pc.getConfig() as ConfigPro).getCoreTagLib(dialect).getNameSpaceAndSeparator()
        val name = prefix + if (dialect == CFMLEngine.DIALECT_CFML) Constants.CFML_SCRIPT_TAG_NAME else Constants.LUCEE_SCRIPT_TAG_NAME
        return tag(pc, "<$name>$cfml</$name>", dialect, catchOutput, ignoreScopes)
    }

    @Throws(PageException::class)
    fun tag(pc: PageContext?, cfml: String?, dialect: Int, catchOutput: Boolean, ignoreScopes: Boolean): Result? {
        // execute
        val res = Result()
        var bc: BodyContent? = null
        try {
            if (catchOutput) bc = pc.pushBody()
            res.value = loadPage(pc.getConfig() as ConfigWebPro, null, cfml, dialect, ignoreScopes).call(pc)
        } catch (t: Throwable) {
            ExceptionUtil.rethrowIfNecessary(t)
            throw Caster.toPageException(t)
        } finally {
            if (catchOutput) {
                if (bc != null) res.output = bc.getString()
                pc.popBody()
            }
        }
        return res
    }

    class Result {
        val output: String? = null
        val value: Object? = null
        fun getOutput(): String? {
            return output ?: ""
        }

        fun getValue(): Object? {
            return value
        }

        @Override
        override fun toString(): String {
            return "output:$output;value:$value"
        }
    }
}