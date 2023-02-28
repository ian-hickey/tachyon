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
package lucee.commons.io.res.type.http

import java.io.IOException

class HTTPResourceProvider : ResourceProviderPro {
    /**
     * @return the lockTimeout
     */
    var lockTimeout = 20000
        private set
    private val lock: ResourceLockImpl = ResourceLockImpl(lockTimeout, false)
    var protocol = "http"
        private set

    /**
     * @return the clientTimeout
     */
    var clientTimeout = 30000
        private set

    /**
     * @return the socketTimeout
     */
    var socketTimeout = 20000
        private set
    private var arguments: Map? = null
    @Override
    fun getScheme(): String {
        return protocol
    }

    fun setScheme(scheme: String) {
        if (!StringUtil.isEmpty(scheme)) protocol = scheme
    }

    @Override
    fun init(scheme: String, arguments: Map?): ResourceProvider {
        setScheme(scheme)
        if (arguments != null) {
            this.arguments = arguments
            // client-timeout
            var strTimeout = arguments.get("client-timeout") as String
            if (strTimeout != null) {
                clientTimeout = Caster.toIntValue(strTimeout, clientTimeout)
            }
            // socket-timeout
            strTimeout = arguments.get("socket-timeout")
            if (strTimeout != null) {
                socketTimeout = Caster.toIntValue(strTimeout, socketTimeout)
            }
            // lock-timeout
            strTimeout = arguments.get("lock-timeout")
            if (strTimeout != null) {
                lockTimeout = Caster.toIntValue(strTimeout, lockTimeout)
            }
        }
        lock.setLockTimeout(lockTimeout)
        return this
    }

    @Override
    fun getResource(path: String): Resource {
        var path = path
        val indexQ: Int = path.indexOf('?')
        if (indexQ != -1) {
            var indexS: Int = path.lastIndexOf('/')
            while (path.lastIndexOf('/').also { indexS = it } > indexQ) {
                path = path.substring(0, indexS).toString() + "%2F" + path.substring(indexS + 1)
            }
        }
        path = ResourceUtil.translatePath(ResourceUtil.removeScheme(protocol, path), false, false)
        return HTTPResource(this, HTTPConnectionData(path, socketTimeout))
    }

    @get:Override
    val isAttributesSupported: Boolean
        get() = false

    @get:Override
    val isCaseSensitive: Boolean
        get() = false

    @get:Override
    val isModeSupported: Boolean
        get() = false

    @Override
    fun setResources(resources: Resources?) {
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
}