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
package lucee.runtime.db

import java.sql.Connection

/**
 * interface for a datasource
 */
interface DataSource : Cloneable, Cloneable {
    /**
     * @return original DSN
     */
    @get:Deprecated("""use instead <code>getConnectionString()</code>
	  """)
    @get:Deprecated
    val dsnOriginal: String?

    /**
     * @return DSN Translated
     */
    @get:Deprecated("""use instead <code>getConnectionStringTranslated()</code>
	  """)
    @get:Deprecated
    val dsnTranslated: String?

    /**
     * @return Returns the connection string with NOT replaced placeholders.
     */
    val connectionString: String?

    /**
     * @return unique id of the DataSource
     */
    fun id(): String?

    /**
     * @return Returns the connection string with replaced placeholders.
     */
    val connectionStringTranslated: String?

    @Throws(ClassException::class, BundleException::class, SQLException::class)
    fun getConnection(config: Config?, user: String?, pass: String?): Connection?

    /**
     * @return Returns the password.
     */
    val password: String?

    /**
     * @return Returns the username.
     */
    val username: String?

    /**
     * @return Returns the readOnly.
     */
    val isReadOnly: Boolean

    /**
     * @param allow allow
     * @return returns if given allow exists
     */
    fun hasAllow(allow: Int): Boolean

    /**
     * @return Returns the clazz.
     */
    @get:SuppressWarnings("rawtypes")
    val classDefinition: ClassDefinition?

    /**
     * @return Returns the database.
     */
    val database: String?

    /**
     * @return Returns the port.
     */
    val port: Int

    /**
     * @return Returns the host.
     */
    val host: String?

    /**
     * @return cloned Object
     */
    fun clone(): Object

    /**
     * @return clone the DataSource as ReadOnly
     */
    fun cloneReadOnly(): DataSource?

    /**
     * @return Returns the blob.
     */
    val isBlob: Boolean

    /**
     * @return Returns the clob.
     */
    val isClob: Boolean

    /**
     * @return Returns the connectionLimit.
     */
    val connectionLimit: Int

    /**
     * @return Returns the connection idle timeout.
     */
    // FUTURE @Deprecated
    val connectionTimeout: Int
    // FUTURE public abstract int getIdleTimeout();
    // FUTURE public abstract int getLiveTimeout();
    /**
     * network timeout in seconds
     *
     * @return Returns the network timeout.
     */
    val networkTimeout: Int
    val metaCacheTimeout: Long
    val timeZone: TimeZone?

    /**
     * @param key key
     * @return Returns matching custom value or null if not exist.
     */
    fun getCustomValue(key: String?): String?

    /**
     * @return returns all custom names
     */
    val customNames: Array<String?>?

    /**
     * @return returns custom
     */
    val customs: Struct?

    /**
     * @return returns if database has a SQL restriction
     */
    fun hasSQLRestriction(): Boolean

    /**
     * @return Returns the name.
     */
    val name: String?
    val isStorage: Boolean
    fun validate(): Boolean
    val log: Log?

    companion object {
        /**
         * Field `ALLOW_SELECT`
         */
        const val ALLOW_SELECT = 1

        /**
         * Field `ALLOW_DELETE`
         */
        const val ALLOW_DELETE = 2

        /**
         * Field `ALLOW_UPDATE`
         */
        const val ALLOW_UPDATE = 4

        /**
         * Field `ALLOW_INSERT`
         */
        const val ALLOW_INSERT = 8

        /**
         * Field `ALLOW_CREATE`
         */
        const val ALLOW_CREATE = 16

        /**
         * Field `ALLOW_GRANT`
         */
        const val ALLOW_GRANT = 32

        /**
         * Field `ALLOW_REVOKE`
         */
        const val ALLOW_REVOKE = 64

        /**
         * Field `ALLOW_DROP`
         */
        const val ALLOW_DROP = 128

        /**
         * Field `ALLOW_ALTER`
         */
        const val ALLOW_ALTER = 256

        /**
         * Field `ALLOW_ALL`
         */
        const val ALLOW_ALL = ALLOW_SELECT + ALLOW_DELETE + ALLOW_UPDATE + ALLOW_INSERT + ALLOW_CREATE + ALLOW_GRANT + ALLOW_REVOKE + ALLOW_DROP + ALLOW_ALTER
    }
}