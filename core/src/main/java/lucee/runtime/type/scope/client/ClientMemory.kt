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
package lucee.runtime.type.scope.client

import lucee.commons.io.log.Log

class ClientMemory : StorageScopeMemory, Client, MemoryScope {
    /**
     * Constructor of the class
     *
     * @param pc
     * @param log
     * @param name
     */
    private constructor(pc: PageContext?, log: Log?) : super(pc, "client", SCOPE_CLIENT, log) {}

    /**
     * Constructor of the class, clone existing
     *
     * @param other
     */
    private constructor(other: ClientMemory?, deepCopy: Boolean) : super(other, deepCopy) {}

    @Override
    fun duplicate(deepCopy: Boolean): Collection? {
        return ClientMemory(this, deepCopy)
    }

    companion object {
        private const val serialVersionUID = 5032226519712666589L

        /**
         * load a new instance of the class
         *
         * @param pc
         * @param log
         * @return
         */
        fun getInstance(pc: PageContext?, log: Log?): Client? {
            return ClientMemory(pc, log)
        }
    }
}