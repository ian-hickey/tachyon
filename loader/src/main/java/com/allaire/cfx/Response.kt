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
package com.allaire.cfx

import kotlin.Throws
import lucee.loader.util.Util
import kotlin.jvm.Synchronized
import kotlin.jvm.Transient
import org.osgi.framework.Version

/**
 * Alternative Implementation of Jeremy Allaire's Response Interface
 */
interface Response {
    /**
     * adds a query to response
     *
     * @param name name of the new Query
     * @param column columns of the new Query
     * @return created query
     */
    fun addQuery(name: String?, column: Array<String?>?): Query?

    /**
     * sets a variable to response
     *
     * @param key key of the variable
     * @param value value of the variable
     */
    fun setVariable(key: String?, value: String?)

    /**
     * write out a String to response
     *
     * @param str String to write
     */
    fun write(str: String?)

    /**
     * write out if debug is enabled
     *
     * @param str String to write
     */
    fun writeDebug(str: String?)
}