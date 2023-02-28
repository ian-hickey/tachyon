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
package tachyon.runtime.functions.struct

import java.util.Iterator

class StructKeyTranslate : BIF() {
    @Override
    @Throws(PageException::class)
    operator fun invoke(pc: PageContext?, args: Array<Object?>?): Object? {
        if (args!!.size == 3) return call(pc, Caster.toStruct(args[0]), Caster.toBooleanValue(args[1]), Caster.toBooleanValue(args[2]))
        if (args.size == 2) return call(pc, Caster.toStruct(args[0]), Caster.toBooleanValue(args[1]))
        if (args.size == 1) return call(pc, Caster.toStruct(args[0]))
        throw FunctionException(pc, "StructKeyTranslate", 1, 3, args.size)
    }

    companion object {
        private const val serialVersionUID = -7978129950865681102L
        @Throws(PageException::class)
        fun call(pc: PageContext?, sct: Struct?): Double {
            return call(pc, sct, false, false)
        }

        @Throws(PageException::class)
        fun call(pc: PageContext?, sct: Struct?, deepTranslation: Boolean): Double {
            return call(pc, sct, deepTranslation, false)
        }

        @Throws(PageException::class)
        fun call(pc: PageContext?, sct: Struct?, deepTranslation: Boolean, leaveOriginalKey: Boolean): Double {
            return translate(sct, deepTranslation, leaveOriginalKey).toDouble()
        }

        @Throws(PageException::class)
        private fun translate(coll: Collection?, deep: Boolean, leaveOrg: Boolean): Int {
            val keys: Array<Key?> = coll.keys() // we do not entry to avoid ConcurrentModificationException
            val isStruct = coll is Struct
            var key: String
            var value: Object
            var index: Int
            var count = 0
            for (k in keys) {
                key = k.getString()
                value = coll.get(k)
                if (deep) count += translate(value, leaveOrg)
                if (isStruct && key.indexOf('.').also { index = it } != -1) {
                    count++
                    translate(index, k, key, coll, leaveOrg)
                }
            }
            return count
        }

        @Throws(PageException::class)
        private fun translate(value: Object?, leaveOrg: Boolean): Int {
            if (value is Collection) return translate(value as Collection?, true, leaveOrg)
            if (value is List) return translate(value as List<*>?, leaveOrg)
            if (value is Map) return translate(value as Map<*, *>?, leaveOrg)
            return if (Decision.isArray(value)) translate(Caster.toNativeArray(value), leaveOrg) else 0
        }

        @Throws(PageException::class)
        private fun translate(list: List<*>?, leaveOrg: Boolean): Int {
            val it = list!!.iterator()
            var count = 0
            while (it.hasNext()) {
                count += translate(it.next(), leaveOrg)
            }
            return count
        }

        @Throws(PageException::class)
        private fun translate(map: Map<*, *>?, leaveOrg: Boolean): Int {
            val it: Iterator<*> = map.entrySet().iterator()
            var count = 0
            while (it.hasNext()) {
                count += translate((it.next() as Map.Entry<*, *>?).getValue(), leaveOrg)
            }
            return count
        }

        @Throws(PageException::class)
        private fun translate(arr: Array<Object?>?, leaveOrg: Boolean): Int {
            var count = 0
            for (i in arr.indices) {
                count += translate(arr!![i], leaveOrg)
            }
            return count
        }

        @Throws(PageException::class)
        private fun translate(index: Int, key: Key?, strKey: String?, coll: Collection?, leaveOrg: Boolean) {
            var index = index
            var strKey = strKey
            var coll: Collection? = coll
            var left: String?
            val value: Object = if (leaveOrg) coll.get(key) else coll.remove(key)
            do {
                left = strKey.substring(0, index)
                strKey = strKey.substring(index + 1)
                coll = touch(coll, KeyImpl.init(left))
            } while (strKey.indexOf('.').also { index = it } != -1)
            coll.set(KeyImpl.init(strKey), value)
        }

        @Throws(PageException::class)
        private fun touch(coll: Collection?, key: Key?): Collection? {
            var coll: Collection? = coll
            val obj: Object = coll.get(key, null)
            if (obj is Collection) return obj
            if (Decision.isCastableToStruct(obj)) return Caster.toStruct(obj)
            coll.set(key, StructImpl().also { coll = it })
            return coll
        }
    }
}