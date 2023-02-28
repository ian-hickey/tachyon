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
/**
 * Implements the CFML Function structfindkey
 */
package tachyon.runtime.functions.struct

import java.util.Iterator

class StructFindKey : BIF() {
    @Override
    @Throws(PageException::class)
    operator fun invoke(pc: PageContext?, args: Array<Object?>?): Object? {
        if (args!!.size == 3) return call(pc, Caster.toStruct(args[0]), Caster.toString(args[1]), Caster.toString(args[2]))
        if (args.size == 2) return call(pc, Caster.toStruct(args[0]), Caster.toString(args[1]))
        throw FunctionException(pc, "StructFindKey", 2, 3, args.size)
    }

    companion object {
        private const val serialVersionUID = 598706098288773975L
        @Throws(PageException::class)
        fun call(pc: PageContext?, struct: tachyon.runtime.type.Struct?, value: String?): Array? {
            return _call(pc, struct, value, false)
        }

        @Throws(PageException::class)
        fun call(pc: PageContext?, struct: Struct?, value: String?, scope: String?): Array? {
            // Scope
            var all = false
            all = if (scope.equalsIgnoreCase("one")) false else if (scope.equalsIgnoreCase("all")) true else throw FunctionException(pc, "structFindValue", 3, "scope", "invalid scope definition [$scope], valid scopes are [one, all]")
            return _call(pc, struct, value, all)
        }

        @Throws(PageException::class)
        private fun _call(pc: PageContext?, struct: Struct?, value: String?, all: Boolean): Array? {
            val array: Array = ArrayImpl()
            getValues(array, struct, value, all, "")
            return array
        }

        /**
         * @param coll
         * @param value
         * @param all
         * @param buffer
         * @return
         * @throws PageException
         */
        @Throws(PageException::class)
        private fun getValues(array: Array?, coll: Collection?, value: String?, all: Boolean, path: String?): Boolean {
            // Collection.Key[] keys=coll.keys();
            val it: Iterator<Entry<Key?, Object?>?> = coll.entryIterator()
            var e: Entry<Key?, Object?>?
            var abort = false
            var key: Collection.Key
            while (it.hasNext()) {
                e = it.next()
                if (abort) break
                key = e.getKey()
                val o: Object = e.getValue()

                // matching value (this function search first for base)
                if (key.getString().equalsIgnoreCase(value)) {
                    val sct: Struct = StructImpl()
                    sct.setEL(KeyConstants._value, o)
                    sct.setEL(KeyConstants._path, createKey(coll, path, key))
                    sct.setEL(KeyConstants._owner, coll)
                    array.append(sct)
                    if (!all) abort = true
                }

                // Collection
                if (!abort) {
                    if (o is Collection) {
                        abort = getValues(array, o as Collection, value, all, createKey(coll, path, key))
                    } else if (o is List) {
                        abort = getValues(array, ListAsArray.toArray(o as List<*>), value, all, createKey(coll, path, key))
                    } else if (o is Map) {
                        abort = getValues(array, MapAsStruct.toStruct(o as Map<*, *>), value, all, createKey(coll, path, key))
                    }
                }
            }
            return abort
        }

        fun createKey(coll: Collection?, path: String?, key: Collection.Key?): String? {
            val p = StringBuilder(path.toString())
            if (isArray(coll)) {
                p.append('[').append(key.getString()).append(']')
            } else {
                p.append('.').append(key.getString())
            }
            return p.toString()
        }

        fun isArray(coll: Collection?): Boolean {
            return coll is Array && coll !is Argument
        }
    }
}