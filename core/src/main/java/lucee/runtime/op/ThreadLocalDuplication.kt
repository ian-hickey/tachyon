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
package lucee.runtime.op

import java.util.IdentityHashMap

object ThreadLocalDuplication {
    private val local: ThreadLocal<Map<Object?, Object?>?>? = ThreadLocal<Map<Object?, Object?>?>()
    private val isInside: ThreadLocal<RefBoolean?>? = ThreadLocal<RefBoolean?>()
        private get() {
            val b: RefBoolean = field.get()
            return b != null && b.toBooleanValue()
        }
        private set(isInside) {
            val b: RefBoolean = ThreadLocalDuplication.isInside.get()
            if (b == null) ThreadLocalDuplication.isInside.set(RefBooleanImpl(isInside)) else b.setValue(isInside)
        }

    operator fun set(o: Object?, c: Object?): Boolean {
        touch(true).put(o, c)
        return isInside
    }

    /*
	 * public static Map<Object, Object> getMap() { return touch(); }
	 * 
	 * public static void removex(Object o) { touch().remove(o); }
	 */
    /*
	 * private static Object get(Object obj) { Map<Object,Object> list = touch(); return list.get(obj);
	 * }
	 */
    operator fun get(`object`: Object?, before: RefBoolean?): Object? {
        if (!isInside) {
            reset()
            isInside = true
            before.setValue(false)
        } else before.setValue(true)
        val list: Map<Object?, Object?>? = touch(false)
        return list?.get(`object`)
    }

    private fun touch(createIfNecessary: Boolean): Map<Object?, Object?>? {
        var set: Map<Object?, Object?>? = local.get()
        if (set == null) {
            if (!createIfNecessary) return null
            set = IdentityHashMap<Object?, Object?>() // it is importend to have a reference comparsion here
            local.set(set)
        }
        return set
    }

    fun reset() {
        val set: Map<Object?, Object?> = local.get()
        if (set != null) set.clear()
        isInside = false
    }
}