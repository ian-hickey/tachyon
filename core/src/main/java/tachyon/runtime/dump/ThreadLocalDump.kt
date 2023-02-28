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
package tachyon.runtime.dump

import java.util.HashMap

object ThreadLocalDump {
    private val local: ThreadLocal<Map<Integer?, String?>?>? = ThreadLocal<Map<Integer?, String?>?>()
    operator fun set(o: Object?, c: String?) {
        touch().put(hash(o), c)
    }

    val map: Map<Any?, String?>?
        get() = touch()

    fun remove(o: Object?) {
        touch().remove(hash(o))
    }

    operator fun get(o: Object?): String? {
        val list: Map<Integer?, String?>? = touch()
        return list!![hash(o)]
    }

    private fun touch(): Map<Integer?, String?>? {
        var set: Map<Integer?, String?>? = local.get()
        if (set == null) {
            set = HashMap<Integer?, String?>()
            local.set(set)
        }
        return set
    }

    // LDEV-3731 - use System.identityHashCode to avoid problems with hashing "arrays that contain themselves"
    private fun hash(o: Object?): Integer? {
        return System.identityHashCode(o)
    }
}