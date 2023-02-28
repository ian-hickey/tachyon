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
package tachyon.commons.io.res.type.file

import java.io.File

class FileResourceProvider
/**
 * Constructor of the class
 */
    : ResourceProviderPro {
    @get:Override
    var scheme = "file"
        private set
    private var lockTimeout: Long = 10000

    @get:Override
    val isCaseSensitive: Boolean = SystemUtil.isFSCaseSensitive()
    private val lock: ResourceLockImpl = ResourceLockImpl(lockTimeout, isCaseSensitive)
    private var arguments: Map? = null
    @Override
    fun init(scheme: String, arguments: Map?): ResourceProvider {
        if (!StringUtil.isEmpty(scheme)) this.scheme = scheme
        this.arguments = arguments
        if (arguments != null) {
            // lock-timeout
            val strTimeout = arguments.get("lock-timeout") as String
            if (strTimeout != null) {
                lockTimeout = Caster.toLongValue(arguments.get("lock-timeout"), lockTimeout)
            }
        }
        lock.setLockTimeout(lockTimeout)
        return this
    }

    @Override
    fun getResource(path: String?): Resource {
        return FileResource(this, ResourceUtil.removeScheme("file", path))
    }

    @Override
    fun setResources(resources: Resources?) {
        // this.resources=resources;
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

    @get:Override
    val isAttributesSupported: Boolean
        get() = SystemUtil.isWindows()

    // SystemUtil.isUnix(); FUTURE add again
    @get:Override
    val isModeSupported: Boolean
        get() = false // SystemUtil.isUnix(); FUTURE add again

    @Override
    fun getArguments(): Map? {
        return arguments
    }

    @get:Override
    val separator: Char
        get() = File.separatorChar
}