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

import java.io.IOException

class ArgumentIntKey : KeyImpl {
    private var intKey = 0

    /**
     * Do NEVER use, only for Externilze
     */
    constructor() {}
    constructor(key: Int) : super(Caster.toString(key)) {
        intKey = key
    }

    fun getIntKey(): Int {
        return intKey
    }

    @Override
    @Throws(IOException::class)
    fun writeExternal(out: ObjectOutput?) {
        out.writeInt(intKey)
        super.writeExternal(out)
    }

    @Override
    @Throws(IOException::class, ClassNotFoundException::class)
    fun readExternal(`in`: ObjectInput?) {
        intKey = `in`.readInt()
        super.readExternal(`in`)
    }

    companion object {
        private val KEYS: Array<ArgumentIntKey?>? = arrayOf(ArgumentIntKey(0), ArgumentIntKey(1), ArgumentIntKey(2), ArgumentIntKey(3),
                ArgumentIntKey(4), ArgumentIntKey(5), ArgumentIntKey(6), ArgumentIntKey(7), ArgumentIntKey(8), ArgumentIntKey(9), ArgumentIntKey(10),
                ArgumentIntKey(11), ArgumentIntKey(12), ArgumentIntKey(13), ArgumentIntKey(14), ArgumentIntKey(15), ArgumentIntKey(16),
                ArgumentIntKey(17))

        fun init(i: Int): ArgumentIntKey? {
            return if (i >= 0 && i < KEYS!!.size) KEYS[i] else ArgumentIntKey(i)
        }
    }
}