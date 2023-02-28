/**
 * Copyright (c) 2015, Lucee Assosication Switzerland. All rights reserved.
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
package lucee.runtime.functions.other

import lucee.runtime.Component

object CreateComponent {
    private val EMPTY: Array<Object?>? = arrayOfNulls<Object?>(0)
    @Throws(PageException::class)
    fun call(pc: PageContext?, path: String?): Component? {
        return call(pc, path, null)
    }

    @Throws(PageException::class)
    fun call(pc: PageContext?, path: String?, args: Object?): Component? {

        // first argument is the component itself
        val c: Component = CreateObject.doComponent(pc, path)
        if (c.get(KeyConstants._init, null) is UDF) {
            // no arguments
            if (args == null) {
                c.call(pc, KeyConstants._init, EMPTY)
            } else if (Decision.isStruct(args)) {
                val sct: Struct = Caster.toStruct(args)
                c.callWithNamedValues(pc, KeyConstants._init, sct)
            } else if (Decision.isArray(args)) {
                val arr: Array<Object?> = Caster.toNativeArray(args)
                c.call(pc, KeyConstants._init, arr)
            } else {
                c.call(pc, KeyConstants._init, arrayOf<Object?>(args))
            }
        }
        return c
    }
}