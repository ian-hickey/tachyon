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
package lucee.runtime.type.scope.session

import lucee.commons.io.log.Log

class SessionMemory : StorageScopeMemory, Session, MemoryScope {
    private var component: Component? = null

    /**
     * Constructor of the class
     *
     * @param pc
     * @param isNew
     * @param name
     */
    protected constructor(pc: PageContext?, log: Log?) : super(pc, "session", SCOPE_SESSION, log) {}

    /**
     * Constructor of the class, clone existing
     *
     * @param other
     */
    protected constructor(other: StorageScopeMemory?, deepCopy: Boolean) : super(other, deepCopy) {}

    @Override
    fun duplicate(deepCopy: Boolean): Collection? {
        return SessionMemory(this, deepCopy)
    }

    fun setComponent(component: Component?) {
        this.component = component
    }

    fun getComponent(): Component? {
        return component
    }

    companion object {
        private const val serialVersionUID = 7703261878730061485L

        /**
         * load a new instance of the class
         *
         * @param pc
         * @param isNew
         * @return
         */
        fun getInstance(pc: PageContext?, isNew: RefBoolean?, log: Log?): Session? {
            isNew.setValue(true)
            return SessionMemory(pc, log)
        }
    }
}