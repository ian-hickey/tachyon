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

import java.util.concurrent.ConcurrentLinkedQueue

/**
 * creates Local and Argument scopes and recyle it
 */
class ScopeFactory {
    var argumentCounter = 0
    private val arguments: ConcurrentLinkedQueue<Argument?>? = ConcurrentLinkedQueue<Argument?>()
    private val locals: ConcurrentLinkedQueue<LocalImpl?>? = ConcurrentLinkedQueue<LocalImpl?>()

    /**
     * @return returns an Argument scope
     */
    fun getArgumentInstance(): Argument? {
        val arg: Argument = arguments.poll()
        return if (arg != null) {
            arg
        } else ArgumentImpl()
    }

    /**
     * @return retruns a Local Instance
     */
    fun getLocalInstance(): LocalImpl? {
        val lcl: LocalImpl = locals.poll()
        return if (lcl != null) {
            lcl
        } else LocalImpl()
    }

    /**
     * @param argument recycle an Argument scope for reuse
     */
    fun recycle(pc: PageContext?, argument: Argument?) {
        if (arguments.size() >= MAX_SIZE || argument.isBind()) return
        argument.release(pc)
        arguments.add(argument)
    }

    /**
     * @param local recycle a Local scope for reuse
     */
    fun recycle(pc: PageContext?, local: LocalImpl?) {
        if (locals.size() >= MAX_SIZE || local!!.isBind()) return
        local!!.release(pc)
        locals.add(local)
    }

    companion object {
        private const val MAX_SIZE = 50

        /**
         * cast an int scope definition to a string definition
         *
         * @param scope
         * @return
         */
        fun toStringScope(scope: Int, defaultValue: String?): String? {
            when (scope) {
                Scope.SCOPE_APPLICATION -> return "application"
                Scope.SCOPE_ARGUMENTS -> return "arguments"
                Scope.SCOPE_CALLER -> return "caller"
                Scope.SCOPE_CGI -> return "cgi"
                Scope.SCOPE_CLIENT -> return "client"
                Scope.SCOPE_COOKIE -> return "cookie"
                Scope.SCOPE_FORM -> return "form"
                Scope.SCOPE_VAR, Scope.SCOPE_LOCAL -> return "local"
                Scope.SCOPE_REQUEST -> return "request"
                Scope.SCOPE_SERVER -> return "server"
                Scope.SCOPE_SESSION -> return "session"
                Scope.SCOPE_UNDEFINED -> return "undefined"
                Scope.SCOPE_URL -> return "url"
                Scope.SCOPE_VARIABLES -> return "variables"
                Scope.SCOPE_CLUSTER -> return "cluster"
            }
            return defaultValue
        }
    }
}