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
package lucee.commons.lang

import java.io.ByteArrayOutputStream

/**
 * Directory ClassLoader
 */
class PClassLoader(c: Config, directory: Resource, parentClassLoaders: Array<ClassLoader?>?, includeCoreCL: Boolean) : ClassLoader() {
    private val directory: Resource
    private val config: ConfigPro
    private val parents: Array<ClassLoader>

    // Set<String> loadedClasses = new HashSet<>();
    var unavaiClasses: Set<String> = HashSet()
    private val customCLs: Map<String, PhysicalClassLoader>? = null
    private val classes: Map<String, Class<*>> = ConcurrentHashMap<String, Class<*>>()

    /**
     * Constructor of the class
     *
     * @param directory
     * @param parent
     * @throws IOException
     */
    constructor(c: Config, directory: Resource) : this(c, directory, null as Array<ClassLoader?>?, true) {}

    @Override
    @Throws(ClassNotFoundException::class)
    fun loadClass(name: String): Class<*> {
        return loadClass(name, false)
    }

    @Override
    @Throws(ClassNotFoundException::class)
    fun loadClass(name: String, resolve: Boolean): Class<*> {
        var clazz: Class<*>? = classes[name]
        if (clazz != null) return clazz

        // if(unavaiClasses.contains(name)) return defaultValue;
        clazz = findClass(name, null as Class?)
        return if (clazz != null) clazz else super.loadClass(name, resolve)
    }

    @Override
    @Throws(ClassNotFoundException::class)
    protected fun findClass(name: String): Class<*> {
        val clazz: Class<*>? = findClass(name, null as Class?)
        return if (clazz != null) clazz else super.findClass(name)
    }

    @Override
    fun getResource(name: String?): URL? {
        return null
    }

    @Override
    protected fun findResource(name: String?): URL {
        // TODO Auto-generated method stub
        return super.findResource(name)
    }

    @Override
    @Throws(IOException::class)
    protected fun findResources(name: String?): Enumeration<URL> {
        // TODO
        return super.findResources(name)
    }

    private fun findClass(name: String, defaultValue: Class<*>?): Class<*>? {
        val res: Resource = directory.getRealResource(name.replace('.', '/').concat(".class"))
        val baos = ByteArrayOutputStream()
        try {
            IOUtil.copy(res, baos, false)
        } catch (e: IOException) {
            unavaiClasses.add(name)
            return defaultValue
        }
        val barr: ByteArray = baos.toByteArray()
        IOUtil.closeEL(baos)
        return loadClass(name, barr)
    }

    @Synchronized
    fun loadClass(name: String?, barr: ByteArray): Class<*> {
        val clazz: Class<*> = TestClassLoader().loadClass(name, barr)
        classes.put(name, clazz)
        return clazz
    }

    internal class TestClassLoader : ClassLoader() {
        fun loadClass(name: String?, barr: ByteArray): Class<*> {
            return defineClass(name, barr, 0, barr.size)
        }
    }

    @Override
    fun getResourceAsStream(name: String?): InputStream? {
        val f: Resource? = _getResource(name)
        if (f != null) {
            try {
                return IOUtil.toBufferedInputStream(f.getInputStream())
            } catch (e: IOException) {
            }
        }
        return null
    }

    /**
     * returns matching File Object or null if file not exust
     *
     * @param name
     * @return matching file
     */
    fun _getResource(name: String?): Resource? {
        val f: Resource = directory.getRealResource(name)
        return if (f != null && f.exists() && f.isFile()) f else null
    }

    fun hasClass(className: String): Boolean {
        return hasResource(className.replace('.', '/').concat(".class"))
    }

    fun hasResource(name: String?): Boolean {
        return _getResource(name) != null
    }

    /**
     * @return the directory
     */
    fun getDirectory(): Resource {
        return directory
    }

    init {
        parents = if (parentClassLoaders == null || parentClassLoaders.size == 0) arrayOf<ClassLoader>(c.getClassLoader()) else parentClassLoaders
        config = c as ConfigPro

        // check directory
        if (!directory.exists()) directory.mkdirs()
        if (!directory.isDirectory()) throw IOException("resource $directory is not a directory")
        if (!directory.canRead()) throw IOException("no access to $directory directory")
        this.directory = directory
    }
}