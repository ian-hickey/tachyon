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

class ClientFile : StorageScopeFile, Client {
    /**
     * Constructor of the class
     *
     * @param pc
     * @param name
     * @param sct
     */
    private constructor(pc: PageContext?, res: Resource?, sct: Struct?) : super(pc, res, "client", SCOPE_CLIENT, sct) {}

    /**
     * Constructor of the class, clone existing
     *
     * @param other
     */
    private constructor(other: ClientFile?, deepCopy: Boolean) : super(other, deepCopy) {}

    @Override
    fun duplicate(deepCopy: Boolean): Collection? {
        return ClientFile(this, deepCopy)
    }

    companion object {
        /**
         * load new instance of the class
         *
         * @param name
         * @param pc
         * @param log
         * @return
         */
        fun getInstance(name: String?, pc: PageContext?, log: Log?): Client? {
            val res: Resource = _loadResource(pc.getConfig(), SCOPE_CLIENT, name, pc.getCFID())
            val data: Struct = _loadData(pc, res, log)
            return ClientFile(pc, res, data)
        }
    }
}