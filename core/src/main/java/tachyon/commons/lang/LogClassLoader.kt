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

import java.io.IOException

class LogClassLoader(cl: ClassLoader, log: Log) : ClassLoader() {
    private val cl: ClassLoader
    private val log: Log
    @Override
    @Synchronized
    fun clearAssertionStatus() {
        log.log(Log.LEVEL_DEBUG, "LogClassLoader", "clearAssertion")
        cl.clearAssertionStatus()
    }

    @Override
    @Throws(IllegalArgumentException::class)
    protected fun definePackage(name: String?, specTitle: String?, specVersion: String?, specVendor: String?, implTitle: String?, implVersion: String?, implVendor: String?, sealBase: URL?): Package? {
        log.log(Log.LEVEL_DEBUG, "LogClassLoader", "definePackage")
        return null
    }

    @Override
    @Throws(ClassNotFoundException::class)
    protected fun findClass(name: String?): Class? {
        log.log(Log.LEVEL_DEBUG, "LogClassLoader", "findClass")
        return null
    }

    @Override
    protected fun findLibrary(libname: String?): String? {
        log.log(Log.LEVEL_DEBUG, "LogClassLoader", "findLibrary")
        return null
    }

    @Override
    protected fun findResource(name: String?): URL? {
        log.log(Log.LEVEL_DEBUG, "LogClassLoader", "findResource")
        return null
    }

    @Override
    @Throws(IOException::class)
    protected fun findResources(name: String?): Enumeration? {
        log.log(Log.LEVEL_DEBUG, "LogClassLoader", "findResources")
        return null
    }

    @Override
    protected fun getPackage(name: String?): Package? {
        log.log(Log.LEVEL_DEBUG, "LogClassLoader", "getPackage")
        return null
    }

    @get:Override
    protected val packages: Array<Any>?
        protected get() {
            log.log(Log.LEVEL_DEBUG, "LogClassLoader", "getPackages")
            return null
        }

    @Override
    fun getResource(name: String?): URL {
        log.log(Log.LEVEL_DEBUG, "LogClassLoader", "getResource")
        return cl.getResource(name)
    }

    @Override
    fun getResourceAsStream(name: String?): InputStream {
        log.log(Log.LEVEL_DEBUG, "LogClassLoader", "getResourceAsStream")
        return cl.getResourceAsStream(name)
    }

    @Override
    @Synchronized
    @Throws(ClassNotFoundException::class)
    protected fun loadClass(name: String?, resolve: Boolean): Class? {
        log.log(Log.LEVEL_DEBUG, "LogClassLoader", "loadClass")
        return null
    }

    @Override
    @Throws(ClassNotFoundException::class)
    fun loadClass(name: String): Class {
        val clazz: Class = cl.loadClass(name)
        log.log(Log.LEVEL_DEBUG, "LogClassLoader", "loadClass($name):$clazz")
        return clazz
    }

    @Override
    @Synchronized
    fun setClassAssertionStatus(className: String?, enabled: Boolean) {
        log.log(Log.LEVEL_DEBUG, "LogClassLoader", "setClassAssertionStatus")
        cl.setClassAssertionStatus(className, enabled)
    }

    @Override
    @Synchronized
    fun setDefaultAssertionStatus(enabled: Boolean) {
        log.log(Log.LEVEL_DEBUG, "LogClassLoader", "setdefaultAssertionStatus")
        cl.setDefaultAssertionStatus(enabled)
    }

    @Override
    @Synchronized
    fun setPackageAssertionStatus(packageName: String?, enabled: Boolean) {
        log.log(Log.LEVEL_DEBUG, "LogClassLoader", "setPackageAssertionStatus")
        cl.setPackageAssertionStatus(packageName, enabled)
    }

    init {
        this.cl = cl
        this.log = log
    }
}