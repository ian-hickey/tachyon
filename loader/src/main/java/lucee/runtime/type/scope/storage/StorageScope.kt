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
package lucee.runtime.type.scope.storage

import lucee.runtime.config.Config

/**
 * scope that can be stored, in a storage
 */
interface StorageScope : SharedScope {
    /*
	 * public static Collection.Key CFID=Util.toKey("cfid"); public static Collection.Key
	 * CFTOKEN=Util.toKey("cftoken"); public static Collection.Key URLTOKEN=Util.toKey("urltoken");
	 * public static Collection.Key LASTVISIT=Util.toKey("lastvisit"); public static Collection.Key
	 * HITCOUNT=Util.toKey("hitcount"); public static Collection.Key
	 * TIMECREATED=Util.toKey("timecreated"); public static Collection.Key
	 * SESSION_ID=Util.toKey("sessionid");
	 */
    /**
     * @return time when the Scope last time was visited
     */
    fun lastVisit(): Long
    val storageType: String?
    val lastAccess: Long
    fun touch()
    val isExpired: Boolean
    val timeSpan: Long

    /**
     * store content on persistent layer
     *
     * @param config config
     */
    fun store(config: Config?)

    /**
     * remove stored data from persistent layer
     *
     * @param config config
     */
    fun unstore(config: Config?)
    /**
     * return the name of the storage used, this is not the storage type!
     *
     * @return Returns the storage name.
     */
    /**
     * sets the name of the storage used, this is not the storage type!
     *
     * @param storage storage name
     */
    var storage: String?
    /**
     * Returns the maximum time interval, in seconds, that the servlet container will keep this session
     * open between client accesses. After this interval, the servlet container will invalidate the
     * session. The maximum time interval can be set with the setMaxInactiveInterval method. A negative
     * time indicates the session should never timeout.
     *
     * @return an integer specifying the number of seconds this session remains open between client
     * requests
     */
    /**
     * Specifies the time, in seconds, between client requests before the servlet container will
     * invalidate this session. A negative time indicates the session should never timeout.
     *
     * @param interval - An integer specifying the number of seconds
     */
    var maxInactiveInterval: Int
    val created: Long
    fun generateToken(key: String?, forceNew: Boolean): String?
    fun verifyToken(token: String?, key: String?): Boolean
}