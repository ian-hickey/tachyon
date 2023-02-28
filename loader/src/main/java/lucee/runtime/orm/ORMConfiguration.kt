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
package lucee.runtime.orm

import lucee.commons.io.res.Resource

interface ORMConfiguration {
    // FUTURE enable
    // public static final int DBCREATE_CREATE = 3;
    // public static final int DBCREATE_CREATE_DROP = 4;
    // public static final int DBCREATE_VALIDATE = 5;
    fun hash(): String?

    /**
     * @return the autogenmap
     */
    fun autogenmap(): Boolean

    /**
     * @return the catalog
     */
    val catalog: String?

    /**
     * @return the cfcLocation
     */
    val cfcLocations: Array<lucee.commons.io.res.Resource?>?
    val isDefaultCfcLocation: Boolean

    /**
     * @return the dbCreate
     */
    val dbCreate: Int

    /**
     * @return the dialect
     */
    val dialect: String?

    /**
     * @return the eventHandling
     */
    fun eventHandling(): Boolean
    fun eventHandler(): String?
    fun namingStrategy(): String?

    /**
     * @return the flushAtRequestEnd
     */
    fun flushAtRequestEnd(): Boolean

    /**
     * @return the logSQL
     */
    fun logSQL(): Boolean

    /**
     * @return the saveMapping
     */
    fun saveMapping(): Boolean

    /**
     * @return the schema
     */
    val schema: String?

    /**
     * @return the secondaryCacheEnabled
     */
    fun secondaryCacheEnabled(): Boolean

    /**
     * @return the sqlScript
     */
    val sqlScript: lucee.commons.io.res.Resource?

    /**
     * @return the useDBForMapping
     */
    fun useDBForMapping(): Boolean

    /**
     * @return the cacheConfig
     */
    val cacheConfig: lucee.commons.io.res.Resource?

    /**
     * @return the cacheProvider
     */
    val cacheProvider: String?

    /**
     * @return the ormConfig
     */
    val ormConfig: lucee.commons.io.res.Resource?
    fun skipCFCWithError(): Boolean
    fun autoManageSession(): Boolean
    fun toStruct(): Object?

    companion object {
        const val DBCREATE_NONE = 0
        const val DBCREATE_UPDATE = 1
        const val DBCREATE_DROP_CREATE = 2
    }
}