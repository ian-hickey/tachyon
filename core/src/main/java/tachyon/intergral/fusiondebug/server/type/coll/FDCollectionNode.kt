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
package tachyon.intergral.fusiondebug.server.type.coll

import com.intergral.fusiondebug.server.FDMutabilityException

class FDCollectionNode(frame: IFDStackFrame?, coll: Collection?, key: Key?) : FDNodeValueSupport(frame) {
    private val coll: Collection?
    private val key: Key?
    @Override
    fun getName(): String? {
        return if (coll is Array) "[" + key.getString().toString() + "]" else key.getString()
    }

    @Override
    protected fun getRawValue(): Object? {
        return coll.get(key, null)
    }

    @Override
    fun isMutable(): Boolean {
        return true
    }

    @Override
    @Throws(FDMutabilityException::class)
    fun set(value: String?) {
        coll.setEL(key, FDCaster.unserialize(value))
    }

    /**
     * Constructor of the class
     *
     * @param coll
     * @param key
     */
    init {
        this.coll = coll
        this.key = key
    }
}