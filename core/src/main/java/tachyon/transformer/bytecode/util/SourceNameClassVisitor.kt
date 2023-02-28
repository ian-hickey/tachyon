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
package tachyon.transformer.bytecode.util

import java.io.IOException

class SourceNameClassVisitor(config: Config?, arg0: Int, onlyCFC: Boolean) : ClassVisitor(arg0) {
    private var source: SourceInfo? = null
    private var filter: ExtensionResourceFilter? = null
    @Override
    fun visitSource(source: String?, debug: String?) {
        super.visitSource(source, debug)
        if (!StringUtil.isEmpty(source)) {
            val name: String = ListUtil.last(source, "/\\")
            if (filter.accept(name)) {
                // older than 4.2.1.008
                if (StringUtil.isEmpty(debug)) {
                    this.source = SourceInfo(name, source)
                } else {
                    // in that case source holds the absolute path
                    val arr: Array<String?> = ListUtil.listToStringArray(debug, ';')
                    var str: String?
                    var index: Int
                    val map: Map<String?, String?> = HashMap<String?, String?>()
                    for (i in arr.indices) {
                        str = arr[i].trim()
                        index = str.indexOf(':')
                        if (index == -1) map.put(str.toLowerCase(), "") else map.put(str.substring(0, index).toLowerCase(), str.substring(index + 1))
                    }
                    val rel = map["rel"]
                    var abs = map["abs"]
                    if (StringUtil.isEmpty(abs)) abs = source
                    this.source = SourceInfo(name, rel, abs)
                }
            }
        }
    }

    class SourceInfo(val name: String?, val relativePath: String?, private var absolutePath: String?) {
        constructor(name: String?, relativePath: String?) : this(name, relativePath, null) {}

        fun absolutePath(pc: PageContext?): String? {
            if (!StringUtil.isEmpty(absolutePath)) return absolutePath
            try {
                absolutePath = ExpandPath.call(pc, relativePath)
            } catch (e: Exception) {
            }
            return absolutePath
        }

        override fun toString(): String {
            return StringBuilder("absolute-path:$absolutePath;relative-path:$relativePath;name:$name").toString()
        }
    }

    companion object {
        @Throws(IOException::class)
        fun getSourceInfo(config: Config?, clazz: Class?, onlyCFC: Boolean): SourceInfo? {
            val name = "/" + clazz.getName().replace('.', '/').toString() + ".class"
            val `in`: InputStream = clazz.getResourceAsStream(name)
            val classReader = ClassReader(`in`)
            val visitor = SourceNameClassVisitor(config, 4, onlyCFC)
            classReader.accept(visitor, 0)
            return if (visitor.source == null || visitor.source!!.name == null) null else visitor.source
        }
    }

    init {
        if (onlyCFC) {
            filter = ExtensionResourceFilter(Constants.getComponentExtensions(), true, true)
        } else {
            filter = ExtensionResourceFilter(Constants.getExtensions(), true, true)
            // filter.addExtension(config.getComponentExtension());
        }
    }
}