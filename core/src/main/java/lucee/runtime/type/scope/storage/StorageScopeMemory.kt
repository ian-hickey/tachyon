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
package lucee.runtime.type.scope.storage

import lucee.commons.io.log.Log

/**
 * client scope that not store it's data
 */
abstract class StorageScopeMemory : StorageScopeImpl, MemoryScope {
    /**
     * Constructor of the class
     *
     * @param pc
     * @param log
     * @param name
     */
    protected constructor(pc: PageContext?, strType: String?, type: Int, log: Log?) : super(StructImpl(), DateTimeImpl(pc.getConfig()), null, -1, 1, strType, type) {
        ScopeContext.debug(log, "create new memory based " + strType + " scope for " + pc.getApplicationContext().getName() + "/" + pc.getCFID())
    }

    /**
     * Constructor of the class, clone existing
     *
     * @param other
     */
    protected constructor(other: StorageScopeMemory?, deepCopy: Boolean) : super(other, deepCopy) {}

    @Override
    fun getStorageType(): String? {
        return "Memory"
    }

    companion object {
        private const val serialVersionUID = -6917303245683342065L
    }
}