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
 * represent a ftp connection
 */
interface FTPConnection {
    /**
     * @return Returns the name.
     */
    val name: String?

    /**
     * @return Returns the password.
     */
    val password: String?

    /**
     * @return Returns the server.
     */
    val server: String?

    /**
     * @return Returns the username.
     */
    val username: String?

    /**
     * @return returns if has logindata or not
     */
    fun hasLoginData(): Boolean

    /**
     * @return has name
     */
    fun hasName(): Boolean

    /**
     * @return Returns the port.
     */
    val port: Int

    /**
     * @return Returns the timeout.
     */
    val timeout: Int

    /**
     * @return Returns the transferMode.
     */
    val transferMode: Short

    /**
     * @return Returns the passive.
     */
    val isPassive: Boolean

    /**
     * @param conn
     * @return has equal login
     */
    fun loginEquals(conn: FTPConnection?): Boolean

    /**
     * @return Returns the proxyserver.
     */
    val proxyServer: String?
    val proxyPort: Int

    /**
     * return the proxy username
     *
     * @return proxy username
     */
    val proxyUser: String?

    /**
     * return the proxy password
     *
     * @return proxy password
     */
    val proxyPassword: String?
    fun secure(): Boolean
    val stopOnError: Boolean
    val fingerprint: String?
    val key: String?
    val passphrase: String?
}