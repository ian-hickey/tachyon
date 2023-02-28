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
package lucee.commons.io.res.type.compress

import java.io.IOException

abstract class CompressResourceProvider : ResourceProviderPro {
    private var resources: Resources? = null

    @get:Override
    var scheme: String? = null
        protected set
    protected var caseSensitive = true
    var async = true
    private var lockTimeout: Long = 10000
    private val lock: ResourceLockImpl = ResourceLockImpl(lockTimeout, caseSensitive)
    private var arguments: Map? = null
    @Override
    fun init(scheme: String?, arguments: Map?): ResourceProvider {
        if (!StringUtil.isEmpty(scheme)) this.scheme = scheme
        if (arguments != null) {
            this.arguments = arguments
            // case-sensitive
            val strCaseSensitive = arguments.get("case-sensitive") as String
            if (strCaseSensitive != null) {
                caseSensitive = Caster.toBooleanValue(strCaseSensitive, true)
            }

            // sync
            var strASync = arguments.get("asynchronus") as String
            if (strASync == null) strASync = arguments.get("async")
            if (strASync != null) {
                async = Caster.toBooleanValue(strASync, true)
            }

            // lock-timeout
            val strTimeout = arguments.get("lock-timeout") as String
            if (strTimeout != null) {
                lockTimeout = Caster.toLongValue(arguments.get("lock-timeout"), lockTimeout)
            }
        }
        lock.setLockTimeout(lockTimeout)
        lock.setCaseSensitive(caseSensitive)
        return this
    }

    fun init(scheme: String?, caseSensitive: Boolean, async: Boolean): ResourceProvider {
        if (!StringUtil.isEmpty(scheme)) this.scheme = scheme
        this.caseSensitive = caseSensitive
        this.async = async
        return this
    }

    @Override
    fun getResource(path: String): Resource {
        var path = path
        path = ResourceUtil.removeScheme(scheme, path)
        val index: Int = path.lastIndexOf('!')
        if (index != -1) {
            val file: Resource = toResource(path.substring(0, index)) // resources.getResource(path.substring(0,index));
            return try {
                CompressResource(this, getCompress(file), path.substring(index + 1), caseSensitive)
            } catch (e: IOException) {
                throw ExceptionUtil.toRuntimeException(e)
            }
        }
        val file: Resource = toResource(path) // resources.getResource(path);
        return try {
            CompressResource(this, getCompress(file), "/", caseSensitive)
        } catch (e: IOException) {
            throw ExceptionUtil.toRuntimeException(e)
        }
    }

    private fun toResource(path: String): Resource {
        val pc: PageContext = ThreadLocalPageContext.get()
        return if (pc != null) {
            ResourceUtil.toResourceNotExisting(ThreadLocalPageContext.get(), path, true, false)
        } else resources.getResource(path)
    }

    @Throws(IOException::class)
    abstract fun getCompress(file: Resource?): Compress
    @Override
    fun setResources(resources: Resources?) {
        this.resources = resources
    }

    @Override
    @Throws(IOException::class)
    fun lock(res: Resource?) {
        lock.lock(res)
    }

    @Override
    fun unlock(res: Resource?) {
        lock.unlock(res)
    }

    @Override
    @Throws(IOException::class)
    fun read(res: Resource?) {
        lock.read(res)
    }

    @Override
    fun getArguments(): Map? {
        return arguments
    }

    @get:Override
    val separator: Char
        get() = '/'

    companion object {
        private const val serialVersionUID = 5930090603192203086L
    }
}