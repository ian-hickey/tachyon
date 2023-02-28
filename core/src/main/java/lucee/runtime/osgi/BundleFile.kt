/**
 * Copyright (c) 2015, Lucee Assosication Switzerland. All rights reserved.
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
package lucee.runtime.osgi

import java.io.File

class BundleFile private constructor(file: File?) : BundleInfo(file) {
    private val file: File?
    private val classes: Map<String?, SoftReference<Boolean?>?>? = ConcurrentHashMap<String?, SoftReference<Boolean?>?>()

    @get:Throws(IOException::class)
    val inputStream: InputStream?
        get() = FileInputStream(file)

    fun getFile(): File? {
        return file
    }

    @Throws(IOException::class)
    fun hasClass(className: String?): Boolean {
        var className = className
        className = className.replace('.', '/').toString() + ".class"
        val tmp: SoftReference<Boolean?>? = classes!![className]
        var b: Boolean? = if (tmp == null) null else tmp.get()
        if (b != null) return b.booleanValue()
        val jar = JarFile(file)
        return try {
            b = jar.getEntry(className) != null
            classes.put(className, SoftReference<Boolean?>(b))
            b.booleanValue()
        } finally {
            IOUtil.closeEL(jar)
        }
    }

    /**
     * only return an instance if the Resource is a valid bundle, otherwise it returns null
     *
     * @param res
     * @return
     *
     * public static BundleFile newInstance(Resource res) {
     *
     * try { BundleFile bf = new BundleFile(res); if (bf.isBundle()) return bf; } catch
     * (Throwable t) { ExceptionUtil.rethrowIfNecessary(t); }
     *
     * return null; }
     */
    companion object {
        private const val serialVersionUID = -7094382262249367193L
        private val files: Map<String?, SoftReference<BundleFile?>?>? = ConcurrentHashMap<String?, SoftReference<BundleFile?>?>()
        @Throws(IOException::class, BundleException::class)
        fun getInstance(file: Resource?, onlyValidBundles: Boolean): BundleFile? {
            val bi: BundleFile = getInstance(toFileResource(file))
            return if (onlyValidBundles && !bi.isBundle()) null else bi
        }

        fun getInstance(file: Resource?, defaultValue: BundleFile?): BundleFile? {
            return try {
                getInstance(toFileResource(file))
            } catch (e: Exception) {
                defaultValue
            }
        }

        @Throws(IOException::class, BundleException::class)
        fun getInstance(file: Resource?): BundleFile? {
            return getInstance(toFileResource(file))
        }

        @Throws(IOException::class, BundleException::class)
        fun getInstance(file: File?): BundleFile? {
            val tmp: SoftReference<BundleFile?>? = files!![file.getAbsolutePath()]
            var bi: BundleFile? = if (tmp == null) null else tmp.get()
            if (bi == null) {
                bi = BundleFile(file)
                files.put(file.getAbsolutePath(), SoftReference<BundleFile?>(bi))
            }
            return bi
        }
    }

    init {
        this.file = file
    }
}