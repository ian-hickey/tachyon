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

class SessionFile : StorageScopeFile, Session {
    /**
     * Constructor of the class
     *
     * @param pc
     * @param name
     * @param sct
     */
    private constructor(pc: PageContext?, res: Resource?, sct: Struct?) : super(pc, res, "session", SCOPE_SESSION, sct) {}

    /**
     * Constructor of the class, clone existing
     *
     * @param other
     */
    private constructor(other: SessionFile?, deepCopy: Boolean) : super(other, deepCopy) {}

    @Override
    fun duplicate(deepCopy: Boolean): Collection? {
        return SessionFile(this, deepCopy)
    }

    companion object {
        private const val serialVersionUID = 3896214476118229640L

        /**
         * load new instance of the class
         *
         * @param name
         * @param pc
         * @param checkExpires
         * @return
         */
        fun getInstance(name: String?, pc: PageContext?, log: Log?): Session? {
            val res: Resource = _loadResource(pc.getConfig(), SCOPE_SESSION, name, pc.getCFID())
            val data: Struct = _loadData(pc, res, log)
            return SessionFile(pc, res, data)
        }

        fun hasInstance(name: String?, pc: PageContext?): Boolean {
            val res: Resource = _loadResource(pc.getConfig(), SCOPE_SESSION, name, pc.getCFID())
            val data: Struct = _loadData(pc, res, null)
            return data != null
        }
    }
}