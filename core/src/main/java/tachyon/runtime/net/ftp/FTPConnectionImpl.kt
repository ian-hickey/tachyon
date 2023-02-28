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
package tachyon.runtime.net.ftp

import kotlin.Throws
import kotlin.jvm.Synchronized
import tachyon.commons.io.SystemUtil.Caller
import kotlin.jvm.Transient
import kotlin.jvm.JvmOverloads
import kotlin.jvm.Volatile
import tachyon.commons.collection.concurrent.ConcurrentHashMapNullSupport.EntrySet
import tachyon.commons.collection.concurrent.ConcurrentHashMapNullSupport.EntryIterator
import tachyon.commons.collection.LongKeyList.Pair
import tachyon.commons.collection.AbstractCollection
import tachyon.runtime.type.Array
import java.sql.Array
import tachyon.commons.lang.Pair
import tachyon.runtime.exp.CatchBlockImpl.Pair
import tachyon.runtime.type.util.ListIteratorImpl
import tachyon.runtime.type.Lambda
import java.util.Random
import tachyon.runtime.config.Constants
import tachyon.runtime.engine.Request
import tachyon.runtime.engine.ExecutionLogSupport.Pair
import tachyon.runtime.functions.other.NullValue
import tachyon.runtime.functions.string.Val
import tachyon.runtime.reflection.Reflector.JavaAnnotation
import tachyon.transformer.cfml.evaluator.impl.Output
import tachyon.transformer.cfml.evaluator.impl.Property
import tachyon.transformer.bytecode.statement.Condition.Pair

/**
 *
 */
class FTPConnectionImpl(name: String?, server: String?, username: String?, password: String?, port: Int, timeout: Int, transferMode: Short, passive: Boolean, proxyserver: String?,
                        proxyport: Int, proxyuser: String?, proxypassword: String?, fingerprint: String?, stopOnError: Boolean, secure: Boolean, key: String?, passphrase: String?) : FTPConnection {
    @get:Override
    override val name: String?

    @get:Override
    override val server: String?

    @get:Override
    override val username: String?

    @get:Override
    override val password: String?

    @get:Override
    override val port: Int

    @get:Override
    override val timeout: Int

    @get:Override
    override var transferMode: Short

    @get:Override
    override val isPassive: Boolean

    @get:Override
    override val proxyServer: String?

    @get:Override
    override val proxyPort: Int

    @get:Override
    override val proxyUser: String?

    @get:Override
    override val proxyPassword: String?

    @get:Override
    override val fingerprint: String?

    @get:Override
    override val stopOnError: Boolean
    private val secure: Boolean

    @get:Override
    override val key: String?

    @get:Override
    override val passphrase: String?

    /**
     * Calls the first constructor and sets key and passphrase to null
     *
     * @param name
     * @param server
     * @param username
     * @param password
     * @param port
     * @param timeout
     * @param transferMode
     * @param passive
     * @param proxyserver
     * @param proxyport
     * @param proxyuser
     * @param proxypassword
     * @param fingerprint
     * @param stopOnError
     * @param secure
     */
    constructor(name: String?, server: String?, username: String?, password: String?, port: Int, timeout: Int, transferMode: Short, passive: Boolean, proxyserver: String?,
                proxyport: Int, proxyuser: String?, proxypassword: String?, fingerprint: String?, stopOnError: Boolean, secure: Boolean) : this(name, server, username, password, port, timeout, transferMode, passive, proxyserver, proxyport, proxyuser, proxypassword, fingerprint, stopOnError, secure, null,
            null) {
    }

    @Override
    override fun hasLoginData(): Boolean {
        return server != null // && username!=null && password!=null;
    }

    @Override
    override fun hasName(): Boolean {
        return name != null
    }

    @Override
    override fun loginEquals(conn: FTPConnection?): Boolean {
        return server.equalsIgnoreCase(conn.getServer()) && username!!.equals(conn.getUsername()) && password!!.equals(conn.getPassword())
    }

    fun equal(o: Object?): Boolean {
        if (o !is FTPConnection) return false
        val other: FTPConnection? = o
        if (neq(other.getPassword(), password)) return false
        if (neq(other.getProxyPassword(), proxyPassword)) return false
        if (neq(other.getProxyServer(), proxyServer)) return false
        if (neq(other.getProxyUser(), proxyUser)) return false
        if (neq(other.getServer(), server)) return false
        if (neq(other.getUsername(), username)) return false
        if (other.getPort() !== port) return false
        if (other.getProxyPort() !== proxyPort) return false
        // if(other.getTimeout()!=getTimeout()) return false;
        return if (other.getTransferMode() !== transferMode) false else true
    }

    private fun neq(left: String?, right: String?): Boolean {
        var left = left
        var right = right
        if (left == null) left = ""
        if (right == null) right = ""
        return !left.equals(right)
    }

    @Override
    override fun secure(): Boolean {
        return secure
    }

    /**
     *
     * @param name
     * @param server
     * @param username
     * @param password
     * @param port
     * @param timeout
     * @param transferMode
     * @param passive
     * @param proxyserver
     * @param proxyport
     * @param proxyuser
     * @param proxypassword
     * @param fingerprint
     * @param stopOnError
     * @param secure
     * @param key
     * @param passphrase
     */
    init {
        this.name = name?.toLowerCase()?.trim()
        this.server = server
        this.username = username
        this.password = password
        this.port = port
        this.timeout = timeout
        this.transferMode = transferMode
        isPassive = passive
        proxyServer = proxyserver
        proxyPort = proxyport
        proxyUser = proxyuser
        proxyPassword = proxypassword
        this.fingerprint = fingerprint
        this.stopOnError = stopOnError
        this.secure = secure
        this.key = key
        this.passphrase = passphrase
    }
}