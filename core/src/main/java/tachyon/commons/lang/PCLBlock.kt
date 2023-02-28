/**
 *
 * Copyright (c) 2014, the Railo Company Ltd. All rights reserved.
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
class PCLBlock(directory: Resource, parent: ClassLoader) : ExtendableClassLoader(parent) {
    companion object {
        init {
            val res: Boolean = registerAsParallelCapable()
        }
    }

    private val directory: Resource
    private val pcl: ClassLoader
    private var size = 0
    private var count = 0

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
    fun loadClass(name: String): Class<*> {
        return loadClass(name, false)
    } // 15075171

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
    protected fun loadClass(name: String, resolve: Boolean): Class<*>? {
        // if(!name.endsWith("$cf")) return super.loadClass(name, resolve); this break Webervices
        // First, check if the class has already been loaded
        var c: Class<*> = findLoadedClass(name)
        // print.o("load:"+name+" -> "+c);
        if (c == null) {
            c = try {
                pcl.loadClass(name) // if(name.indexOf("sub")!=-1)print.ds(name);
            } catch (t: Throwable) {
                ExceptionUtil.rethrowIfNecessary(t)
                findClass(name)
            }
        }
        if (resolve) {
            resolveClass(c)
        }
        return c
    }

    @Override
    @Throws(ClassNotFoundException::class)
    protected fun findClass(name: String): Class<*> { // if(name.indexOf("sub")!=-1)print.ds(name);
        val res: Resource = directory.getRealResource(name.replace('.', '/').concat(".class"))
        val baos = ByteArrayOutputStream()
        try {
            IOUtil.copy(res, baos, false)
        } catch (e: IOException) {
            throw ClassNotFoundException("class $name is invalid or doesn't exist")
        }
        val barr: ByteArray = baos.toByteArray()
        size += barr.size
        count++
        IOUtil.closeEL(baos)
        return loadClass(name, barr)
    }

    @Override
    fun loadClass(name: String?, barr: ByteArray): Class<*> {
        val start = 0
        // if(ClassUtil.hasCF33Prefix(barr)) start=10;
        size += barr.size - start
        count++
        return try {
            defineClass(name, barr, start, barr.size - start)
        } catch (t: Throwable) {
            ExceptionUtil.rethrowIfNecessary(t)
            SystemUtil.wait(this, 1)
            try {
                defineClass(name, barr, start, barr.size - start)
            } catch (t2: Throwable) {
                ExceptionUtil.rethrowIfNecessary(t2)
                SystemUtil.wait(this, 1)
                defineClass(name, barr, start, barr.size - start)
            }
        }
        // return loadClass(name,false);
    }

    @Override
    fun getResource(name: String?): URL? {
        /*
		 * URL url=super.getResource(name); if(url!=null) return url;
		 * 
		 * Resource f =_getResource(name); if(f!=null) { try { return f.toURL(); } catch
		 * (MalformedURLException e) {} }
		 */
        return null
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
        // print.o("isClassLoaded:"+className+"-"+(findLoadedClass(className)!=null));
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

    fun count(): Int {
        return count
    }

    /**
     * Constructor of the class
     *
     * @param directory
     * @param parent
     * @throws IOException
     */
    init {
        pcl = parent
        this.directory = directory
    }
}