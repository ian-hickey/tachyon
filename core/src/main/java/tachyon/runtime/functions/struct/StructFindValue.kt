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
 * Implements the CFML Function structfindvalue
 */
package tachyon.runtime.functions.struct

import java.util.Iterator

class StructFindValue : BIF() {
    @Override
    @Throws(PageException::class)
    operator fun invoke(pc: PageContext?, args: Array<Object?>?): Object? {
        if (args!!.size == 3) return call(pc, Caster.toStruct(args[0]), Caster.toString(args[1]), Caster.toString(args[2]))
        if (args.size == 2) return call(pc, Caster.toStruct(args[0]), Caster.toString(args[1]))
        throw FunctionException(pc, "StructFindValue", 2, 3, args.size)
    }

    companion object {
        private const val serialVersionUID = 1499023912262918840L
        @Throws(PageException::class)
        fun call(pc: PageContext?, struct: tachyon.runtime.type.Struct?, value: String?): Array? {
            return call(pc, struct, value, "one")
        }

        @Throws(PageException::class)
        fun call(pc: PageContext?, struct: Struct?, value: String?, scope: String?): Array? {
            // Scope
            var all = false
            all = if (scope.equalsIgnoreCase("one")) false else if (scope.equalsIgnoreCase("all")) true else throw FunctionException(pc, "structFindValue", 3, "scope", "invalid scope definition [$scope], valid scopes are [one, all]")
            val array: Array = ArrayImpl()
            getValues(pc, array, struct, value, all, "")
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
        private fun getValues(pc: PageContext?, array: Array?, coll: Collection?, value: String?, all: Boolean, path: String?): Boolean {
            // Key[] keys = coll.keys();
            var abort = false
            var key: Key
            val it: Iterator<Entry<Key?, Object?>?> = coll.entryIterator()
            var e: Entry<Key?, Object?>?
            loop@ while (it.hasNext()) {
                e = it.next()
                if (abort) break@loop
                key = e.getKey()
                val o: Object = e.getValue()

                // Collection (this function search first for sub)
                if (o is Collection) {
                    abort = getValues(pc, array, o as Collection, value, all, StructFindKey.createKey(coll, path, key))
                }
                // matching value
                if (!abort && !StructFindKey.isArray(coll)) {
                    val target: String = Caster.toString(o, null)
                    if (target != null && target.equalsIgnoreCase(value) /* || (o instanceof Array && checkSub(array,((Array)o),value,all,path,abort)) */) {
                        val sct: Struct = StructImpl()
                        sct.setEL(KeyConstants._key, key.getString())
                        sct.setEL(KeyConstants._path, StructFindKey.createKey(coll, path, key))
                        sct.setEL(KeyConstants._owner, coll)
                        array.append(sct)
                        if (!all) abort = true
                    }
                }
            }
            return abort
        }
    }
}