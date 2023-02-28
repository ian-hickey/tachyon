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
package lucee.runtime.type.scope

import kotlin.Throws
import lucee.loader.util.Util
import kotlin.jvm.Synchronized
import kotlin.jvm.Transient
import org.osgi.framework.Version

/**
 * interface for the caller scope
 */
interface Caller : Scope {
    /**
     * sets the scopes
     *
     * @param variablesScope variables scope
     * @param localScope local scope
     * @param argumentsScope arguments scope
     * @param checkArgs check arguments
     */
    fun setScope(variablesScope: Variables?, localScope: Local?, argumentsScope: Argument?, checkArgs: Boolean)

    /**
     * @return the variablesScope
     */
    val variablesScope: lucee.runtime.type.scope.Variables?

    /**
     * @return the localScope
     */
    val localScope: lucee.runtime.type.scope.Local?

    /**
     * @return the argumentsScope
     */
    val argumentsScope: lucee.runtime.type.scope.Argument?
}