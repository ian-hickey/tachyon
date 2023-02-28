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
package tachyon.commons.io.res.type.http

import tachyon.Info

class HTTPConnectionData {
    var username = ""
    var password = ""
    var host: String? = "localhost"
    var port = 0
    var proxyData: ProxyData? = null
    var path: String? = null
    var userAgent: String? = null
    var timeout = 0

    constructor(username: String, password: String, host: String?, port: Int, path: String?, proxyData: ProxyData?, userAgent: String?) {
        this.username = username
        this.password = password
        this.host = host
        this.port = port
        this.proxyData = proxyData
        this.path = path
        if (!StringUtil.isEmpty(userAgent)) this.userAgent = userAgent else {
            this.userAgent = defaultUserAgent()
        }
    }

    constructor(path: String, timeout: Int) {
        load(path)
        this.timeout = timeout
        userAgent = defaultUserAgent()
    }

    constructor(path: String) {
        load(path)
        userAgent = defaultUserAgent()
    }

    fun load(path: String) {
        var path = path
        username = ""
        password = ""
        host = null
        port = -1
        // TODO impl proxy
        var atIndex: Int = path.indexOf('@')
        var slashIndex: Int = path.indexOf('/')
        if (atIndex > slashIndex) atIndex = -1
        if (slashIndex == -1) {
            slashIndex = path.length()
            path += "/"
        }
        var index: Int

        // username/password
        if (atIndex != -1) {
            index = path.indexOf(':')
            if (index != -1 && index < atIndex) {
                username = path.substring(0, index)
                password = path.substring(index + 1, atIndex)
            } else username = path.substring(0, atIndex)
        }
        // host port
        if (slashIndex > atIndex + 1) {
            index = path.indexOf(':', atIndex + 1)
            if (index != -1 && index > atIndex && index < slashIndex) {
                host = path.substring(atIndex + 1, index)
                port = Integer.parseInt(path.substring(index + 1, slashIndex))
            } else host = path.substring(atIndex + 1, slashIndex)
        }
        this.path = path.substring(slashIndex)
    }

    @Override
    override fun toString(): String {
        return "username:$username;password:$password;hostname:$host;port:$port;path:$path"
    }

    fun key(): String {
        return if (StringUtil.isEmpty(username)) host + _port() else username + ":" + password + "@" + host + _port()
    }

    private fun _port(): String {
        return if (port > 0) ":$port" else ""
    }

    fun hasProxyData(): Boolean {
        return ProxyDataImpl.isValid(proxyData)
    }

    @Override
    override fun equals(obj: Object): Boolean {
        if (this === obj) return true
        return if (obj !is HTTPConnectionData) false else key().equals((obj as HTTPConnectionData).key())
    }

    fun setProxyData(proxyData: ProxyData?) {
        this.proxyData = proxyData
    }

    companion object {
        private fun defaultUserAgent(): String {
            val info: Info = CFMLEngineFactory.getInstance().getInfo()
            return Constants.NAME.toString() + " " + info.getVersion()
        }
    }
}