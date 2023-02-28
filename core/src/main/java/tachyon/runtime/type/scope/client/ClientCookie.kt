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
package tachyon.runtime.type.scope.client

import tachyon.commons.io.log.Log

class ClientCookie : StorageScopeCookie, Client {
    private constructor(pc: PageContext?, cookieName: String?, sct: Struct?) : super(pc, cookieName, "client", SCOPE_CLIENT, sct) {}

    /**
     * Constructor of the class, clone existing
     *
     * @param other
     */
    private constructor(other: ClientCookie?, deepCopy: Boolean) : super(other, deepCopy) {}

    @Override
    fun duplicate(deepCopy: Boolean): Collection? {
        return ClientCookie(this, deepCopy)
    }

    companion object {
        private const val serialVersionUID = 4203695198240254464L
        private val TYPE: String? = "CLIENT"

        /**
         * load new instance of the class
         *
         * @param name
         * @param pc
         * @param log
         * @return
         */
        fun getInstance(name: String?, pc: PageContext?, log: Log?): Client? {
            var name = name
            if (!StringUtil.isEmpty(name)) name = StringUtil.toUpperCase(StringUtil.toVariableName(name))
            val cookieName = "CF_" + TYPE + "_" + name
            return ClientCookie(pc, cookieName, _loadData(pc, cookieName, SCOPE_CLIENT, "client", log))
        }
    }
}