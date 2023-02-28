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
package tachyon.commons.lang

import java.io.IOException

/**
 * ClassLoader that loads classes in memory that are not stored somewhere physically
 */
class MemoryClassLoader(config: Config, parent: ClassLoader) : ExtendableClassLoader(parent) {
    companion object {
        init {
            val res: Boolean = registerAsParallelCapable()
        }
    }

    private val config: Config
    private val pcl: ClassLoader
    var size: Long = 0
        private set

    @Override
    @Throws(ClassNotFoundException::class)
    fun loadClass(name: String): Class<*> {
        return loadClass(name, false)
    }

    @Override
    @Throws(ClassNotFoundException::class)
    protected fun loadClass(name: String, resolve: Boolean): Class<*>? {
        synchronized(SystemUtil.createToken("MemoryClassLoader", name)) {


            // First, check if the class has already been loaded
            var c: Class<*> = findLoadedClass(name)
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
    }

    @Override
    @Throws(ClassNotFoundException::class)
    protected fun findClass(name: String): Class<*> {
        throw ClassNotFoundException("class $name is invalid or doesn't exist")
    }

    @Override
    @Throws(UnmodifiableClassException::class)
    fun loadClass(name: String, barr: ByteArray): Class<*> {
        synchronized(SystemUtil.createToken("MemoryClassLoader", name)) {
            var clazz: Class<*>? = null
            try {
                clazz = loadClass(name)
            } catch (cnf: ClassNotFoundException) {
            }

            // if class already exists
            return if (clazz != null) {
                // first we try to update the class what needs instrumentation object
                /*
				 * try { InstrumentationFactory.getInstrumentation(config).redefineClasses(new
				 * ClassDefinition(clazz, barr)); return clazz; } catch (Exception e) { LogUtil.log(null,
				 * "compilation", e); }
				 */
                // in case instrumentation fails, we rename it
                rename(clazz, barr)
            } else _loadClass(name, barr)
            // class not exists yet
        }
    }

    private fun rename(clazz: Class<*>, barr: ByteArray): Class<*> {
        val newName: String = clazz.getName().toString() + "$" + PhysicalClassLoader.uid()
        return _loadClass(newName, ClassRenamer.rename(barr, newName))
    }

    private fun _loadClass(name: String, barr: ByteArray): Class<*> {
        size += barr.size.toLong()
        // class not exists yet
        return try {
            defineClass(name, barr, 0, barr.size)
        } catch (t: Throwable) {
            ExceptionUtil.rethrowIfNecessary(t)
            SystemUtil.sleep(1)
            try {
                defineClass(name, barr, 0, barr.size)
            } catch (t2: Throwable) {
                ExceptionUtil.rethrowIfNecessary(t2)
                SystemUtil.sleep(1)
                defineClass(name, barr, 0, barr.size)
            }
        }
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
        this.config = config
    }
}