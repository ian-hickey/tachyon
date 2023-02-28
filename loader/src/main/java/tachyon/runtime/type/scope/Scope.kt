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
package tachyon.runtime.type.scope

import tachyon.runtime.PageContext

/**
 * abstract class for all scopes
 */
interface Scope : Struct {
    /**
     * return if the scope is Initialized
     *
     * @return scope is init
     */
    val isInitalized: Boolean

    /**
     * Initialize Scope
     *
     * @param pc Page Context
     */
    fun initialize(pc: PageContext?)

    /**
     * release scope for reuse
     *
     * @param pc Page Context
     */
    fun release(pc: PageContext?)

    /**
     * @return return the scope type (SCOPE_SERVER, SCOPE_SESSION usw.)
     */
    val type: Int

    /**
     * @return return the scope type as a String (server,session usw.)
     */
    val typeAsString: String?

    companion object {
        /**
         * Scope Undefined
         */
        const val SCOPE_UNDEFINED = 0

        /**
         * Scope Variables
         */
        const val SCOPE_VARIABLES = 1

        /**
         * Scope Request
         */
        const val SCOPE_REQUEST = 2

        /**
         * Scope URL
         */
        const val SCOPE_URL = 3

        /**
         * Scope Form
         */
        const val SCOPE_FORM = 4

        /**
         * Scope Client
         */
        const val SCOPE_CLIENT = 5

        /**
         * Scope Cookie
         */
        const val SCOPE_COOKIE = 6

        /**
         * Scope Session
         */
        const val SCOPE_SESSION = 7

        /**
         * Scope Application
         */
        const val SCOPE_APPLICATION = 8

        /**
         * Scope Arguments
         */
        const val SCOPE_ARGUMENTS = 9

        /**
         * Scope CGI
         */
        const val SCOPE_CGI = 10

        /**
         * Scope Server
         */
        const val SCOPE_SERVER = 11

        /**
         * Scope Local
         */
        const val SCOPE_LOCAL = 12

        /**
         * Scope Caller
         */
        const val SCOPE_CALLER = 13
        const val SCOPE_CLUSTER = 14
        const val SCOPE_VAR = 15
        const val SCOPE_COUNT = 16
    }
}