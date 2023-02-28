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
package lucee.runtime.net.mail

import java.io.Serializable

/**
 *
 */
class ServerImpl(// FUTURE add to interface
        val id: Int, @get:Override val hostName: String?, port: Int, @get:Override val username: String?, private val password: String?, val lifeTimeSpan: Long, val idleTimeSpan: Long, tls: Boolean, ssl: Boolean, reuseConnections: Boolean,
        type: Int) : Server, Serializable {

    @get:Override
    val port: Int = DEFAULT_PORT

    @get:Override
    var isReadOnly = false
        private set

    @get:Override
    var isTLS: Boolean

    @get:Override
    var isSSL: Boolean
    private val reuse: Boolean

    // FUTURE add to interface
    val type: Int
    @Override
    fun getPassword(): String? {
        return if (password == null && hasAuthentication()) "" else password
    }

    @Override
    fun hasAuthentication(): Boolean {
        return username != null && username.length() > 0
    }

    @Override
    override fun toString(): String {
        return if (username != null) {
            "$username:$password@$hostName:$port"
        } else hostName.toString() + ":" + port + ":" + isSSL + ":" + isTLS + ":" + idleTimeSpan + ":" + lifeTimeSpan
    }

    @Override
    override fun equals(obj: Object?): Boolean {
        return toString().equals(obj.toString())
    }

    @Override
    fun cloneReadOnly(): Server? {
        val s = ServerImpl(id, hostName, port, username, password, lifeTimeSpan, idleTimeSpan, isTLS, isSSL, reuse, type)
        s.isReadOnly = true
        return s
    }

    @Override
    @Throws(SMTPException::class)
    fun verify(): Boolean {
        return SMTPVerifier.verify(hostName, username, password, port)
    }

    fun reuseConnections(): Boolean {
        return reuse
    }

    companion object {
        private const val serialVersionUID = -3352908216814744100L
        const val TYPE_GLOBAL = 1
        const val TYPE_LOCAL = 2
        @Throws(MailException::class)
        fun getInstance(host: String?, defaultPort: Int, defaultUsername: String?, defaultPassword: String?, defaultLifeTimespan: Long, defaultIdleTimespan: Long,
                        defaultTls: Boolean, defaultSsl: Boolean): ServerImpl? {
            var host = host
            val userpass: String?
            var user = defaultUsername
            var pass = defaultPassword
            val tmp: String
            var port = defaultPort

            // [user:password@]server[:port]
            var index: Int = host.indexOf('@')

            // username:password
            if (index != -1) {
                userpass = host.substring(0, index)
                host = host.substring(index + 1)
                index = userpass.indexOf(':')
                if (index != -1) {
                    user = userpass.substring(0, index).trim()
                    pass = userpass.substring(index + 1).trim()
                } else user = userpass.trim()
            }

            // server:port
            index = host.indexOf(':')
            if (index != -1) {
                tmp = host.substring(index + 1).trim()
                if (!StringUtil.isEmpty(tmp)) {
                    port = try {
                        Caster.toIntValue(tmp)
                    } catch (e: ExpressionException) {
                        throw MailException("Mail server port definition is invalid [$tmp]")
                    }
                }
                host = host.substring(0, index).trim()
            } else host = host.trim()
            return ServerImpl(-1, host, port, user, pass, defaultLifeTimespan, defaultIdleTimespan, defaultTls, defaultSsl, true, TYPE_LOCAL)
        }

        fun merge(arr1: Array<lucee.runtime.net.mail.Server?>?, arr2: Array<lucee.runtime.net.mail.Server?>?): Array<lucee.runtime.net.mail.Server?>? {
            val result: ArrayList<lucee.runtime.net.mail.Server?> = ArrayList<Server?>()

            // first we fill it with the left array
            for (i in arr1.indices) {
                result.add(arr1!![i])
            }

            // Now we fill the second array, but only the one not existing yet
            for (i in arr2.indices) {
                if (!result.contains(arr2!![i])) result.add(arr2[i])
            }
            return result.toArray(arrayOfNulls<lucee.runtime.net.mail.Server?>(result.size()))
        }
    }

    /*
	 * public ServerImpl(String server,int port) { this.hostName=server; this.port=port; }
	 */
    init {
        this.port = port
        isTLS = tls
        isSSL = ssl
        reuse = reuseConnections
        this.type = type
    }
}