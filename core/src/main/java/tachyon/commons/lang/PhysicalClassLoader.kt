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

import java.io.ByteArrayOutputStream

/**
 * Directory ClassLoader
 */
class PhysicalClassLoader(c: Config, directory: Resource, parentClassLoaders: Array<ClassLoader?>?, includeCoreCL: Boolean) : ExtendableClassLoader(if (parentClassLoaders == null || parentClassLoaders.size == 0) c.getClassLoader() else parentClassLoaders[0]) {
    companion object {
        private var counter = 0L
        private var _start = 0L
        private var start = toString(_start, Character.MAX_RADIX)
        private val countToken: Object = Object()
        fun uid(): String {
            synchronized(countToken) {
                counter++
                if (counter < 0) {
                    counter = 1
                    start = toString(++_start, Character.MAX_RADIX)
                }
                return if (_start == 0L) toString(counter, Character.MAX_RADIX) else start + "_" + toString(counter, Character.MAX_RADIX)
            }
        }

        init {
            val res: Boolean = registerAsParallelCapable()
        }
    }

    private val directory: Resource
    private val config: ConfigPro
    private val parents: Array<ClassLoader>
    private val loadedClasses: Map<String, String> = ConcurrentHashMap<String, String>()
    private val allLoadedClasses: Map<String, String> = ConcurrentHashMap<String, String>() // this includes all renames
    private val unavaiClasses: Map<String, String> = ConcurrentHashMap<String, String>()
    private var customCLs: Map<String, SoftReference<PhysicalClassLoader>>? = null

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
    protected fun loadClass(name: String, resolve: Boolean): Class<*> {
        synchronized(SystemUtil.createToken("PhysicalClassLoader", name)) { return loadClass(name, resolve, true) }
    }

    @Throws(ClassNotFoundException::class)
    private fun loadClass(name: String, resolve: Boolean, loadFromFS: Boolean): Class<*>? {
        // First, check if the class has already been loaded
        var c: Class<*> = findLoadedClass(name)
        if (c == null) {
            for (p in parents) {
                try {
                    c = p.loadClass(name)
                    break
                } catch (e: Exception) {
                }
            }
            if (c == null) {
                c = if (loadFromFS) findClass(name) else throw ClassNotFoundException(name)
            }
        }
        if (resolve) resolveClass(c)
        return c
    }

    @Override
    @Throws(ClassNotFoundException::class)
    protected fun findClass(name: String): Class<*> { // if(name.indexOf("sub")!=-1)print.ds(name);
        synchronized(SystemUtil.createToken("PhysicalClassLoader", name)) {
            val res: Resource = directory.getRealResource(name.replace('.', '/').concat(".class"))
            val baos = ByteArrayOutputStream()
            try {
                IOUtil.copy(res, baos, false)
            } catch (e: IOException) {
                unavaiClasses.put(name, "")
                throw ClassNotFoundException("Class [$name] is invalid or doesn't exist", e)
            }
            val barr: ByteArray = baos.toByteArray()
            IOUtil.closeEL(baos)
            return _loadClass(name, barr, false)
        }
    }

    @Override
    @Throws(UnmodifiableClassException::class)
    fun loadClass(name: String, barr: ByteArray): Class<*> {
        var clazz: Class<*>? = null
        synchronized(SystemUtil.createToken("PhysicalClassLoader", name)) {


            // new class , not in memory yet
            try {
                clazz = loadClass(name, false, false) // we do not load existing class from disk
            } catch (cnf: ClassNotFoundException) {
            }
            return if (clazz == null) _loadClass(name, barr, false) else rename(clazz, barr)

            // first we try to update the class what needs instrumentation object
            /*
			 * try { InstrumentationFactory.getInstrumentation(config).redefineClasses(new
			 * ClassDefinition(clazz, barr)); return clazz; } catch (Exception e) { LogUtil.log(null,
			 * "compilation", e); }
			 */
            // in case instrumentation fails, we rename it
        }
    }

    private fun rename(clazz: Class<*>?, barr: ByteArray): Class<*> {
        val newName: String = clazz.getName().toString() + "$" + uid()
        return _loadClass(newName, ClassRenamer.rename(barr, newName), true)
    }

    private fun _loadClass(name: String, barr: ByteArray, rename: Boolean): Class<*>? {
        val clazz: Class<*> = defineClass(name, barr, 0, barr.size)
        if (clazz != null) {
            if (!rename) loadedClasses.put(name, "")
            allLoadedClasses.put(name, "")
            resolveClass(clazz)
        }
        return clazz
    }

    @Override
    fun getResource(name: String?): URL? {
        return null
    }

    fun getSize(includeAllRenames: Boolean): Int {
        return if (includeAllRenames) allLoadedClasses.size() else loadedClasses.size()
    }

    @Override
    fun getResourceAsStream(name: String?): InputStream? {
        val `is`: InputStream = super.getResourceAsStream(name)
        if (`is` != null) return `is`
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

    fun isClassLoaded(className: String?): Boolean {
        return findLoadedClass(className) != null
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

    @Throws(IOException::class)
    fun getCustomClassLoader(resources: Array<Resource>, reload: Boolean): PhysicalClassLoader {
        if (ArrayUtil.isEmpty(resources)) return this
        val key = hash(resources)
        if (reload && customCLs != null) customCLs.remove(key)
        val tmp: SoftReference<PhysicalClassLoader>? = if (customCLs == null) null else customCLs!![key]
        var pcl: PhysicalClassLoader? = if (tmp == null) null else tmp.get()
        if (pcl != null) return pcl
        pcl = PhysicalClassLoader(config, getDirectory(), arrayOf<ClassLoader?>(ResourceClassLoader(resources, getParent())), true)
        if (customCLs == null) customCLs = ConcurrentHashMap<String, SoftReference<PhysicalClassLoader>>()
        customCLs.put(key, SoftReference<PhysicalClassLoader>(pcl))
        return pcl
    }

    private fun hash(resources: Array<Resource>): String {
        Arrays.sort(resources)
        val sb = StringBuilder()
        for (i in resources.indices) {
            sb.append(ResourceUtil.getCanonicalPathEL(resources[i]))
            sb.append(';')
        }
        return HashUtil.create64BitHashAsString(sb.toString(), Character.MAX_RADIX)
    }

    fun clear() {
        loadedClasses.clear()
        allLoadedClasses.clear()
        unavaiClasses.clear()
    }

    init {
        config = c as ConfigPro

        // ClassLoader resCL = parent!=null?parent:config.getResourceClassLoader(null);
        val tmp: List<ClassLoader> = ArrayList<ClassLoader>()
        if (parentClassLoaders == null || parentClassLoaders.size == 0) {
            val _cl: ResourceClassLoader = config.getResourceClassLoader(null)
            if (_cl != null) tmp.add(_cl)
        } else {
            for (p in parentClassLoaders) {
                tmp.add(p)
            }
        }
        if (includeCoreCL) tmp.add(config.getClassLoaderCore())
        parents = tmp.toArray(arrayOfNulls<ClassLoader>(tmp.size()))

        // check directory
        if (!directory.exists()) directory.mkdirs()
        if (!directory.isDirectory()) throw IOException("Resource [$directory] is not a directory")
        if (!directory.canRead()) throw IOException("Access denied to [$directory] directory")
        this.directory = directory
    }
}