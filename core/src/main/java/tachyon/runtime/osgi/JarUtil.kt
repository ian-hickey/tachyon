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
package tachyon.runtime.osgi

import java.io.IOException

object JarUtil {
    val DEFAULT_IGNORES: Array<String?>? = arrayOf("java.*"
    )

    /**
     *
     * @param res
     * @param ignores ".*" add the end includes all sub directories
     * @return
     * @throws IOException
     */
    @Throws(IOException::class)
    fun getExternalImports(res: Resource?, ignores: Array<String?>?): Set<String?>? {
        val `is`: InputStream = res.getInputStream()
        return try {
            getExternalImports(`is`, ignores)
        } finally {
            IOUtil.close(`is`)
        }
    }

    fun getExternalImports(`is`: InputStream?, ignores: Array<String?>?): Set<String?>? {
        val imports: Set<String?> = HashSet()
        val classNames: Set<String?> = HashSet()
        var zis: ZipInputStream? = null
        try {
            zis = ZipInputStream(`is`)
            var entry: ZipEntry?
            var name: String?
            while (zis.getNextEntry().also { entry = it } != null) {
                if (entry.isDirectory() || !entry.getName().endsWith(".class")) continue
                name = entry.getName()
                name = name.replace('/', '.')
                name = name.substring(0, name.length() - 6)
                classNames.add(name)
                _getExternalImports(imports, zis, ignores)
            }
        } catch (ioe: IOException) {
            LogUtil.log(ThreadLocalPageContext.get(), JarUtil::class.java.getName(), ioe)
        } finally {
            try {
                IOUtil.close(zis)
            } catch (ioe: IOException) {
                LogUtil.log(ThreadLocalPageContext.get(), JarUtil::class.java.getName(), ioe)
            }
        }

        // remove all class from this jar
        var it = classNames.iterator()
        var cn: String?
        while (it.hasNext()) {
            cn = it.next()
            imports.remove(cn)
        }

        // create package set
        val importPackages: Set<String?> = HashSet()
        it = imports.iterator()
        var index: Int
        while (it.hasNext()) {
            cn = it.next()
            index = cn.lastIndexOf('.')
            if (index == -1) continue  // no package
            importPackages.add(cn.substring(0, index))
        }
        return importPackages
    }

    @Throws(IOException::class)
    private fun _getExternalImports(imports: Set<String?>?, src: InputStream?, ignores: Array<String?>?) {
        val reader = ClassReader(src)
        val remapper: Remapper = Collector(imports, ignores)
        val inner: ClassVisitor = EmptyVisitor()
        val visitor = RemappingClassAdapter(inner, remapper)
        reader.accept(visitor, 0)
    }

    class Collector(private val imports: Set<String?>?, private val ignores: Array<String?>?) : Remapper() {
        @Override
        fun mapDesc(desc: String?): String? {
            if (desc.startsWith("L")) {
                addType(desc.substring(1, desc!!.length() - 1))
            }
            return super.mapDesc(desc)
        }

        @Override
        fun mapTypes(types: Array<String?>?): Array<String?>? {
            for (type in types!!) {
                addType(type)
            }
            return super.mapTypes(types)
        }

        @Override
        fun mapType(type: String?): String? {
            addType(type)
            return type
        }

        private fun addType(type: String?) {
            val className: String = type.replace('/', '.')
            val index: Int = className.lastIndexOf('.')
            if (index == -1) return  // class with no package
            var ignore: String?
            var pack: String?
            for (i in DEFAULT_IGNORES.indices) {
                ignore = DEFAULT_IGNORES!![i]
                // also ignore sub directories
                if (ignore.endsWith(".*")) {
                    ignore = ignore.substring(0, ignore!!.length() - 1)
                    if (className.startsWith(ignore)) return
                } else {
                    pack = className.substring(0, index)
                    if (pack!!.equals(ignore)) return
                }
            }
            for (i in ignores.indices) {
                ignore = ignores!![i]
                // also ignore sub directories
                if (ignore.endsWith(".*")) {
                    ignore = ignore.substring(0, ignore!!.length() - 1)
                    if (className.startsWith(ignore)) return
                } else {
                    pack = className.substring(0, index)
                    if (pack!!.equals(ignore)) return
                }
            }
            imports.add(className)
        }
    }

    private class EmptyVisitor : ClassVisitor(ASM4), Opcodes
}