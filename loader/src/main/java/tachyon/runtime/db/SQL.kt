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
package tachyon.runtime.db

import kotlin.Throws
import tachyon.loader.util.Util
import kotlin.jvm.Synchronized
import kotlin.jvm.Transient
import org.osgi.framework.Version

/**
 * represent a SQL Statement
 */
interface SQL {
    /**
     * @return Returns the items.
     */
    val items: Array<tachyon.runtime.db.SQLItem?>?
    /**
     * @return Returns the position.
     */
    /**
     * @param position The position to set.
     */
    var position: Int
    /**
     * @return returns the pure SQL String
     */
    /**
     * @param strSQL sets the SQL String
     */
    var sQLString: String?

    /**
     * @return returns Unique String for Hash
     */
    fun toHashString(): String?
}