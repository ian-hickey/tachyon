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
package lucee.runtime.functions.closure

import java.util.Enumeration

class Reduce : BIF(), ClosureFunc {
    @Override
    @Throws(PageException::class)
    operator fun invoke(pc: PageContext?, args: Array<Object?>?): Object? {
        if (args!!.size == 2) return call(pc, args[0], Caster.toFunction(args[1]))
        if (args.size == 3) return call(pc, args[0], Caster.toFunction(args[1]), args[2])
        throw FunctionException(pc, "Reduce", 2, 3, args.size)
    }

    companion object {
        private const val serialVersionUID = -5940580562772523622L
        @Throws(PageException::class)
        fun call(pc: PageContext?, obj: Object?, udf: UDF?): Object? {
            return _call(pc, obj, udf, null, TYPE_UNDEFINED)
        }

        @Throws(PageException::class)
        fun call(pc: PageContext?, obj: Object?, udf: UDF?, initalValue: Object?): Object? {
            return _call(pc, obj, udf, initalValue, TYPE_UNDEFINED)
        }

        @Throws(PageException::class)
        fun call(pc: PageContext?, obj: Object?, udf: UDF?, initalValue: Object?, type: Short): Object? {
            return _call(pc, obj, udf, initalValue, type)
        }

        @Throws(PageException::class)
        private fun _call(pc: PageContext?, obj: Object?, udf: UDF?, initalValue: Object?, type: Short): Object? {
            val value: Object?

            // !!!! Don't combine the first 3 ifs with the ifs below, type overrules instanceof check
            // Array
            if (type == TYPE_ARRAY) {
                value = invoke(pc, obj as Array?, udf, initalValue)
            } else if (type == TYPE_QUERY) {
                value = invoke(pc, obj as Query?, udf, initalValue)
            } else if (type == TYPE_STRUCT) {
                value = invoke(pc, obj as Struct?, udf, initalValue)
            } else if (obj is Array && obj !is Argument) {
                value = invoke(pc, obj as Array?, udf, initalValue)
            } else if (obj is Query) {
                value = invoke(pc, obj as Query?, udf, initalValue)
            } else if (obj is Struct) {
                value = invoke(pc, obj as Struct?, udf, initalValue)
            } else if (obj is Iteratorable) {
                value = invoke(pc, obj as Iteratorable?, udf, initalValue)
            } else if (obj is Map<*, *>) {
                value = invoke(pc, obj as Map<*, *>?, udf, initalValue)
            } else if (obj is List) {
                value = invoke(pc, obj as List?, udf, initalValue)
            } else if (obj is Iterator) {
                value = invoke(pc, obj as Iterator?, udf, initalValue)
            } else if (obj is Enumeration) {
                value = invoke(pc, obj as Enumeration?, udf, initalValue)
            } else if (obj is StringListData) {
                value = invoke(pc, obj as StringListData?, udf, initalValue)
            } else throw FunctionException(pc, "Filter", 1, "data", "cannot iterate througth this type " + Caster.toTypeName(obj.getClass()))
            return value
        }

        @Throws(CasterException::class, PageException::class)
        private operator fun invoke(pc: PageContext?, arr: Array?, udf: UDF?, initalValue: Object?): Object? {
            var initalValue: Object? = initalValue
            val it: Iterator = if (arr is ArrayPro) (arr as ArrayPro?).entryArrayIterator() else arr.entryIterator()
            var e: Entry
            while (it.hasNext()) {
                e = it.next() as Entry
                initalValue = udf.call(pc, arrayOf<Object?>(initalValue, e.getValue(), Caster.toDoubleValue(e.getKey()), arr), true)
            }
            return initalValue
        }

        @Throws(CasterException::class, PageException::class)
        private operator fun invoke(pc: PageContext?, sld: StringListData?, udf: UDF?, initalValue: Object?): Object? {
            var initalValue: Object? = initalValue
            val arr: Array = ListUtil.listToArray(sld.list, sld.delimiter, sld.includeEmptyFieldsx, sld.multiCharacterDelimiter)
            val it: Iterator = if (arr is ArrayPro) (arr as ArrayPro).entryArrayIterator() else arr.entryIterator()
            var e: Entry
            while (it.hasNext()) {
                e = it.next() as Entry
                initalValue = udf.call(pc, arrayOf<Object?>(initalValue, e.getValue(), Caster.toDoubleValue(e.getKey()), sld.list, sld.delimiter), true)
            }
            return initalValue
        }

        @Throws(CasterException::class, PageException::class)
        private operator fun invoke(pc: PageContext?, qry: Query?, udf: UDF?, initalValue: Object?): Object? {
            var initalValue: Object? = initalValue
            val pid: Int = pc.getId()
            val it = ForEachQueryIterator(pc, qry, pid)
            var rowNbr: Int
            var row: Object
            while (it.hasNext()) {
                row = it.next()
                rowNbr = qry.getCurrentrow(pid)
                initalValue = udf.call(pc, arrayOf<Object?>(initalValue, row, Caster.toDoubleValue(rowNbr), qry), true)
            }
            return initalValue
        }

        @Throws(CasterException::class, PageException::class)
        private operator fun invoke(pc: PageContext?, list: List?, udf: UDF?, initalValue: Object?): Object? {
            var initalValue: Object? = initalValue
            val it: ListIterator = list.listIterator()
            var v: Object
            var index: Int
            var k: ArgumentIntKey
            while (it.hasNext()) {
                index = it.nextIndex()
                k = ArgumentIntKey.init(index)
                v = it.next()
                initalValue = udf.call(pc, arrayOf<Object?>(initalValue, v, Caster.toDoubleValue(k.getString()), list), true)
            }
            return initalValue
        }

        @Throws(PageException::class)
        private operator fun invoke(pc: PageContext?, sct: Struct?, udf: UDF?, initalValue: Object?): Object? {
            var initalValue: Object? = initalValue
            val it: Iterator<Entry<Key?, Object?>?> = sct.entryIterator()
            var e: Entry<Key?, Object?>?
            while (it.hasNext()) {
                e = it.next()
                initalValue = udf.call(pc, arrayOf<Object?>(initalValue, e.getKey().getString(), e.getValue(), sct), true)
            }
            return initalValue
        }

        @Throws(PageException::class)
        private operator fun invoke(pc: PageContext?, map: Map<*, *>?, udf: UDF?, initalValue: Object?): Object? {
            var initalValue: Object? = initalValue
            val it: Iterator<Entry?> = map.entrySet().iterator()
            var e: Entry?
            while (it.hasNext()) {
                e = it.next()
                initalValue = udf.call(pc, arrayOf(initalValue, e.getKey(), e.getValue(), map), true)
            }
            return initalValue
        }

        @Throws(PageException::class)
        private operator fun invoke(pc: PageContext?, i: Iteratorable?, udf: UDF?, initalValue: Object?): Object? {
            var initalValue: Object? = initalValue
            val it: Iterator<Entry<Key?, Object?>?> = i.entryIterator()
            var e: Entry<Key?, Object?>?
            while (it.hasNext()) {
                e = it.next()
                initalValue = udf.call(pc, arrayOf<Object?>(initalValue, e.getKey().getString(), e.getValue()), true)
            }
            return initalValue
        }

        @Throws(PageException::class)
        private operator fun invoke(pc: PageContext?, it: Iterator?, udf: UDF?, initalValue: Object?): Object? {
            var initalValue: Object? = initalValue
            var v: Object
            var count = 0
            var k: ArgumentIntKey
            while (it.hasNext()) {
                v = it.next()
                k = ArgumentIntKey.init(++count)
                initalValue = udf.call(pc, arrayOf<Object?>(initalValue, v), true)
            }
            return initalValue
        }

        @Throws(PageException::class)
        private operator fun invoke(pc: PageContext?, e: Enumeration?, udf: UDF?, initalValue: Object?): Object? {
            var initalValue: Object? = initalValue
            var v: Object
            var count = 0
            var k: ArgumentIntKey
            while (e.hasMoreElements()) {
                v = e.nextElement()
                k = ArgumentIntKey.init(++count)
                initalValue = udf.call(pc, arrayOf<Object?>(initalValue, v), true)
            }
            return initalValue
        }
    }
}