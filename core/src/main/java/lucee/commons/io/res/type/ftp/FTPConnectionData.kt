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
package lucee.commons.io.res.type.ftp

import lucee.commons.lang.StringUtil

class FTPConnectionData {
    var username = ""
    var password = ""
    var host = "localhost"
    var port = 21
    private var customHostPort = false
    private var customUserPass = false
    var data: ProxyData? = null

    constructor() {}
    constructor(host: String, username: String, password: String, port: Int) : this(host, username, password, port, false, false) {}
    constructor(host: String, username: String, password: String, port: Int, customHostPort: Boolean, customUserPass: Boolean) {
        this.host = host
        this.username = username
        this.password = password
        this.port = port
        this.customHostPort = customHostPort
        this.customUserPass = customUserPass
    }

    class DataAndPath(var data: FTPConnectionData, var path: String)

    @Override
    override fun toString(): String {
        return StringBuilder().append("username:").append(username).append(";password:").append(password).append(";hostname:").append(host).append(";port:").append(port)
                .toString()
    }

    fun key(): String {
        val sb = StringBuilder()
        if (!StringUtil.isEmpty(username) && customUserPass) sb.append(username).append(":").append(password).append("@")
        if (customHostPort) sb.append(host).append(_port())
        return sb.toString()
    }

    private fun _port(): String {
        return if (port > 0) ":$port" else ""
    }

    fun hasProxyData(): Boolean {
        return ProxyDataImpl.isValid(data)
    }

    val proxyData: ProxyData?
        get() = data

    @Override
    override fun equals(obj: Object): Boolean {
        if (this === obj) return true
        return if (obj !is FTPConnectionData) false else toString().equals((obj as FTPConnectionData).toString())
    }

    companion object {
        fun load(base: FTPConnectionData?, path: String): DataAndPath {
            var path = path
            var username = base?.username ?: ""
            var password = base?.password ?: ""
            var host = base?.host ?: "localhost"
            var port = base?.port ?: 21
            var customUserPass = false
            var customHostPort = false
            val atIndex: Int = path.indexOf('@')
            var slashIndex: Int = path.indexOf('/')
            if (slashIndex == -1) {
                slashIndex = path.length()
                path += "/"
            }
            var index: Int

            // username/password
            if (atIndex != -1) {
                customUserPass = true
                index = path.indexOf(':')
                if (index != -1 && index < atIndex) {
                    username = path.substring(0, index)
                    password = path.substring(index + 1, atIndex)
                } else {
                    username = path.substring(0, atIndex)
                    password = ""
                }
            }
            // host port
            if (slashIndex > atIndex + 1) {
                customHostPort = true
                index = path.indexOf(':', atIndex + 1)
                if (index != -1 && index > atIndex && index < slashIndex) {
                    host = path.substring(atIndex + 1, index)
                    port = Integer.parseInt(path.substring(index + 1, slashIndex))
                } else {
                    host = path.substring(atIndex + 1, slashIndex)
                    port = 21
                }
            }
            return DataAndPath(FTPConnectionData(host, username, password, port, customHostPort, customUserPass), path.substring(slashIndex))
        }
    }
}