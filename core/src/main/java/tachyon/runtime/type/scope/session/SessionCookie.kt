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
package tachyon.runtime.type.scope.session

import tachyon.commons.io.log.Log

class SessionCookie : StorageScopeCookie, Session {
    private constructor(pc: PageContext?, cookieName: String?, sct: Struct?) : super(pc, cookieName, "session", SCOPE_SESSION, sct) {}

    /**
     * Constructor of the class, clone existing
     *
     * @param other
     */
    private constructor(other: SessionCookie?, deepCopy: Boolean) : super(other, deepCopy) {}

    @Override
    fun duplicate(deepCopy: Boolean): Collection? {
        return SessionCookie(this, deepCopy)
    }

    companion object {
        private const val serialVersionUID = -3166541654190337670L
        private val TYPE: String? = "SESSION"

        /**
         * load new instance of the class
         *
         * @param name
         * @param pc
         * @return
         */
        fun getInstance(name: String?, pc: PageContext?, log: Log?): Session? {
            var name = name
            if (!StringUtil.isEmpty(name)) name = StringUtil.toUpperCase(StringUtil.toVariableName(name))
            val cookieName = "CF_" + TYPE + "_" + name
            return SessionCookie(pc, cookieName, _loadData(pc, cookieName, SCOPE_SESSION, "session", log))
        }

        fun hasInstance(name: String?, pc: PageContext?): Boolean {
            var name = name
            if (!StringUtil.isEmpty(name)) name = StringUtil.toUpperCase(StringUtil.toVariableName(name))
            val cookieName = "CF_" + TYPE + "_" + name
            return has(pc, cookieName, SCOPE_SESSION, "session")
        }
    }
}