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
package tachyon.commons.lang

import java.io.ByteArrayInputStream

// TODO umbauen auf ZipInputStream oder ein wrapper schreiben fuer resorces der das file interface einhaelt
class ArchiveClassLoader(file: Resource, parent: ClassLoader) : ClassLoader(parent), Closeable {
    private val zip: ZipFile
    private val pcl: ClassLoader

    /**
     * Loads the class with the specified name. This method searches for classes in the same manner as
     * the [.loadClass] method. It is called by the Java virtual machine to
     * resolve class references. Calling this method is equivalent to calling
     * `loadClass(name, false)`.
     *
     * @param name the name of the class
     * @return the resulting `Class` object
     * @exception ClassNotFoundException if the class was not found
     */
    @Override
    @Throws(ClassNotFoundException::class)
    fun loadClass(name: String): Class? {
        return loadClass(name, false)
    }

    /**
     * Loads the class with the specified name. The default implementation of this method searches for
     * classes in the following order:
     *
     *
     *
     *
     *  1. Call [.findLoadedClass] to check if the class has already been loaded.
     *
     *
     *  1. Call the `loadClass` method on the parent class loader. If the parent is
     * `null` the class loader built-in to the virtual machine is used, instead.
     *
     *
     *  1. Call the [.findClass] method to find the class.
     *
     *
     *
     *
     * If the class was found using the above steps, and the `resolve` flag is true, this
     * method will then call the [.resolveClass] method on the resulting class object.
     *
     *
     * From the Java 2 SDK, v1.2, subclasses of ClassLoader are encouraged to override
     * [.findClass], rather than this method.
     *
     *
     *
     * @param name the name of the class
     * @param resolve if `true` then resolve the class
     * @return the resulting `Class` object
     * @exception ClassNotFoundException if the class could not be found
     */
    @Override
    @Synchronized
    @Throws(ClassNotFoundException::class)
    protected fun loadClass(name: String, resolve: Boolean): Class? {
        // First, check if the class has already been loaded
        var c: Class? = findLoadedClass(name)
        if (c == null) {
            c = findClassEL(name)
            if (c == null) {
                c = pcl.loadClass(name)
            }
        }
        if (resolve) {
            resolveClass(c)
        }
        return c
    }

    @Override
    @Throws(ClassNotFoundException::class)
    protected fun findClass(name: String): Class {
        val clazz: Class? = findClassEL(name)
        if (clazz != null) return clazz
        throw ClassNotFoundException("class $name not found")
    }

    private fun findClassEL(name: String): Class? {
        val barr = getBytes(name.replace('.', '/').concat(".class")) ?: return null
        return try {
            val start = if (ClassUtil.hasCF33Prefix(barr)) 10 else 0
            defineClass(name, barr, start, barr.size - start)
        } catch (e: Exception) {
            null
        }
    }

    @Override
    fun getResourceAsStream(name: String): InputStream? {
        val `is`: InputStream = super.getResourceAsStream(name)
        if (`is` != null) return `is`
        val barr = getBytes(name)
        val start = if (ClassUtil.hasCF33Prefix(barr)) 10 else 0
        return if (barr != null) ByteArrayInputStream(barr, start, barr.size - start) else null
    }

    @Override
    fun getResource(name: String?): URL? {
        return null
    }

    private fun getBytes(name: String): ByteArray? {
        val entry: ZipEntry = zip.getEntry(name) ?: return null
        val size = entry.getSize() as Int
        var `is`: InputStream? = null
        try {
            `is` = zip.getInputStream(entry)
            val data = ByteArray(size)
            var pos = 0
            while (pos < size) {
                val n: Int = `is`.read(data, pos, data.size - pos)
                pos += n
            }
            return data
        } catch (ioe: IOException) {
        } finally {
            IOUtil.closeEL(`is`)
        }
        return null
    }

    @Override
    @Throws(IOException::class)
    fun close() {
        zip.close()
    }

    /**
     * constructor of the class
     *
     * @param file
     * @param parent
     * @throws IOException
     */
    init {
        pcl = parent
        // this.file=file;

        // print.ln("archive:"+file.getPath());
        if (!file.exists()) throw FileNotFoundException("file " + file.getAbsolutePath().toString() + " doesn't exist")
        if (!file.isFile()) throw IOException(file.getAbsolutePath().toString() + " is not a file")
        if (!file.isReadable()) throw IOException("no access to " + file.getAbsolutePath().toString() + " file")
        zip = ZipFile(FileWrapper.toFile(file))
    }
}