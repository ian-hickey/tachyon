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

import java.util.ArrayList

class Some : BIF(), ClosureFunc {
    @Override
    @Throws(PageException::class)
    operator fun invoke(pc: PageContext?, args: Array<Object?>?): Object? {
        if (args!!.size == 2) return call(pc, Caster.toStruct(args[0]), Caster.toFunction(args[1]))
        if (args.size == 3) return call(pc, Caster.toStruct(args[0]), Caster.toFunction(args[1]), Caster.toBooleanValue(args[2]))
        if (args.size == 4) return call(pc, Caster.toStruct(args[0]), Caster.toFunction(args[1]), Caster.toBooleanValue(args[2]), Caster.toDoubleValue(args[3]))
        throw FunctionException(pc, "Some", 2, 4, args.size)
    }

    companion object {
        private const val serialVersionUID = -5940580562772523622L
        @Throws(PageException::class)
        fun call(pc: PageContext?, obj: Object?, udf: UDF?): Boolean {
            return _call(pc, obj, udf, false, 20, TYPE_UNDEFINED)
        }

        @Throws(PageException::class)
        fun call(pc: PageContext?, obj: Object?, udf: UDF?, parallel: Boolean): Boolean {
            return _call(pc, obj, udf, parallel, 20, TYPE_UNDEFINED)
        }

        @Throws(PageException::class)
        fun call(pc: PageContext?, obj: Object?, udf: UDF?, parallel: Boolean, maxThreads: Double): Boolean {
            return _call(pc, obj, udf, parallel, maxThreads.toInt(), TYPE_UNDEFINED)
        }

        @Throws(PageException::class)
        fun call(pc: PageContext?, obj: Object?, udf: UDF?, parallel: Boolean, maxThreads: Int, type: Short): Boolean {
            return _call(pc, obj, udf, parallel, maxThreads, type)
        }

        @Throws(PageException::class)
        private fun _call(pc: PageContext?, obj: Object?, udf: UDF?, parallel: Boolean, maxThreads: Int, type: Short): Boolean {
            var execute: ExecutorService? = null
            var futures: List<Future<Data<Object?>?>?>? = null
            if (parallel) {
                execute = Executors.newFixedThreadPool(maxThreads)
                futures = ArrayList<Future<Data<Object?>?>?>()
            }
            var res: Boolean

            // Array
            if (type == TYPE_ARRAY) {
                res = invoke(pc, obj as Array?, udf, execute, futures)
            } else if (type == TYPE_QUERY) {
                res = invoke(pc, obj as Query?, udf, execute, futures)
            } else if (type == TYPE_STRUCT) {
                res = invoke(pc, obj as Struct?, udf, execute, futures)
            } else if (obj is Array && obj !is Argument) {
                res = invoke(pc, obj as Array?, udf, execute, futures)
            } else if (obj is Query) {
                res = invoke(pc, obj as Query?, udf, execute, futures)
            } else if (obj is Struct) {
                res = invoke(pc, obj as Struct?, udf, execute, futures)
            } else if (obj is Iteratorable) {
                res = invoke(pc, obj as Iteratorable?, udf, execute, futures)
            } else if (obj is Map<*, *>) {
                res = invoke(pc, obj as Map<*, *>?, udf, execute, futures)
            } else if (obj is List) {
                res = invoke(pc, obj as List?, udf, execute, futures)
            } else if (obj is Iterator) {
                res = invoke(pc, obj as Iterator?, udf, execute, futures)
            } else if (obj is Enumeration) {
                res = invoke(pc, obj as Enumeration?, udf, execute, futures)
            } else if (obj is StringListData) {
                res = invoke(pc, obj as StringListData?, udf, execute, futures)
            } else throw FunctionException(pc, "Some", 1, "data", "cannot iterate througth this type " + Caster.toTypeName(obj.getClass()))
            if (parallel) res = afterCall(pc, futures, execute)
            return res
        }

        @Throws(CasterException::class, PageException::class)
        private operator fun invoke(pc: PageContext?, qry: Query?, udf: UDF?, es: ExecutorService?, futures: List<Future<Data<Object?>?>?>?): Boolean {
            val pid: Int = pc.getId()
            val it = ForEachQueryIterator(pc, qry, pid)
            val async = es != null
            var r: Double
            var res: Object?
            var row: Object
            try {
                while (it.hasNext()) {
                    row = it.next()
                    r = Caster.toDoubleValue(qry.getCurrentrow(pid))
                    res = _inv(pc, udf, arrayOf(row, r, qry), r, row, es, futures)
                    if (!async && Caster.toBooleanValue(res)) {
                        return true
                    }
                }
            } finally {
                it.reset()
            }
            return false
        }

        @Throws(CasterException::class, PageException::class)
        private operator fun invoke(pc: PageContext?, arr: Array?, udf: UDF?, es: ExecutorService?, futures: List<Future<Data<Object?>?>?>?): Boolean {
            val it: Iterator = if (arr is ArrayPro) (arr as ArrayPro?).entryArrayIterator() else arr.entryIterator()
            var e: Entry
            val async = es != null
            var res: Object?
            while (it.hasNext()) {
                e = it.next() as Entry
                res = _inv(pc, udf, arrayOf<Object?>(e.getValue(), Caster.toDoubleValue(e.getKey()), arr), e.getKey(), e.getValue(), es, futures)
                if (!async && Caster.toBooleanValue(res)) {
                    return true
                }
            }
            return false
        }

        @Throws(CasterException::class, PageException::class)
        private operator fun invoke(pc: PageContext?, sld: StringListData?, udf: UDF?, es: ExecutorService?, futures: List<Future<Data<Object?>?>?>?): Boolean {
            val arr: Array = ListUtil.listToArray(sld.list, sld.delimiter, sld.includeEmptyFieldsx, sld.multiCharacterDelimiter)
            val it: Iterator = if (arr is ArrayPro) (arr as ArrayPro).entryArrayIterator() else arr.entryIterator()
            var e: Entry
            val async = es != null
            var res: Object?
            while (it.hasNext()) {
                e = it.next() as Entry
                res = _inv(pc, udf, arrayOf<Object?>(e.getValue(), Caster.toDoubleValue(e.getKey()), sld.list, sld.delimiter), e.getKey(), e.getValue(), es, futures)
                if (!async && Caster.toBooleanValue(res)) {
                    return true
                }
            }
            return false
        }

        @Throws(CasterException::class, PageException::class)
        private operator fun invoke(pc: PageContext?, list: List?, udf: UDF?, es: ExecutorService?, futures: List<Future<Data<Object?>?>?>?): Boolean {
            val it: ListIterator = list.listIterator()
            val async = es != null
            var res: Object?
            var v: Object
            var index: Int
            var k: ArgumentIntKey
            while (it.hasNext()) {
                index = it.nextIndex()
                k = ArgumentIntKey.init(index)
                v = it.next()
                res = _inv(pc, udf, arrayOf<Object?>(v, Caster.toDoubleValue(k.getString()), list), k, v, es, futures)
                if (!async && Caster.toBooleanValue(res)) return true
            }
            return false
        }

        @Throws(PageException::class)
        private operator fun invoke(pc: PageContext?, sct: Struct?, udf: UDF?, es: ExecutorService?, futures: List<Future<Data<Object?>?>?>?): Boolean {
            val it: Iterator<Entry<Key?, Object?>?> = sct.entryIterator()
            var e: Entry<Key?, Object?>?
            val async = es != null
            var res: Object?
            while (it.hasNext()) {
                e = it.next()
                res = _inv(pc, udf, arrayOf<Object?>(e.getKey().getString(), e.getValue(), sct), e.getKey(), e.getValue(), es, futures)
                if (!async && Caster.toBooleanValue(res)) return true
            }
            return false
        }

        @Throws(PageException::class)
        private operator fun invoke(pc: PageContext?, map: Map<*, *>?, udf: UDF?, es: ExecutorService?, futures: List<Future<Data<Object?>?>?>?): Boolean {
            val it: Iterator<Entry?> = map.entrySet().iterator()
            var e: Entry?
            val async = es != null
            var res: Object?
            while (it.hasNext()) {
                e = it.next()
                res = _inv(pc, udf, arrayOf(e.getKey(), e.getValue(), map), e.getKey(), e.getValue(), es, futures)
                if (!async && Caster.toBooleanValue(res)) return true
            }
            return false
        }

        @Throws(PageException::class)
        private operator fun invoke(pc: PageContext?, i: Iteratorable?, udf: UDF?, es: ExecutorService?, futures: List<Future<Data<Object?>?>?>?): Boolean {
            val it: Iterator<Entry<Key?, Object?>?> = i.entryIterator()
            var e: Entry<Key?, Object?>?
            val async = es != null
            var res: Object?
            while (it.hasNext()) {
                e = it.next()
                res = _inv(pc, udf, arrayOf<Object?>(e.getKey().getString(), e.getValue()), e.getKey(), e.getValue(), es, futures)
                if (!async && Caster.toBooleanValue(res)) return true
            }
            return false
        }

        @Throws(PageException::class)
        private operator fun invoke(pc: PageContext?, it: Iterator?, udf: UDF?, es: ExecutorService?, futures: List<Future<Data<Object?>?>?>?): Boolean {
            var v: Object
            val async = es != null
            var res: Object?
            var count = 0
            var k: ArgumentIntKey
            while (it.hasNext()) {
                v = it.next()
                k = ArgumentIntKey.init(++count)
                res = _inv(pc, udf, arrayOf<Object?>(v), k, v, es, futures)
                if (!async && Caster.toBooleanValue(res)) return true
            }
            return false
        }

        @Throws(PageException::class)
        private operator fun invoke(pc: PageContext?, e: Enumeration?, udf: UDF?, es: ExecutorService?, futures: List<Future<Data<Object?>?>?>?): Boolean {
            var v: Object
            val async = es != null
            var res: Object?
            var count = 0
            var k: ArgumentIntKey
            while (e.hasMoreElements()) {
                v = e.nextElement()
                k = ArgumentIntKey.init(++count)
                res = _inv(pc, udf, arrayOf<Object?>(v), k, v, es, futures)
                if (!async && Caster.toBooleanValue(res)) return true
            }
            return false
        }

        @Throws(PageException::class)
        private fun _inv(pc: PageContext?, udf: UDF?, args: Array<Object?>?, key: Object?, value: Object?, es: ExecutorService?, futures: List<Future<Data<Object?>?>?>?): Object? {
            if (es == null) {
                return udf.call(pc, args, true)
            }
            futures.add(es.submit(UDFCaller2<Object?>(pc, udf, args, null, true)))
            return null
        }

        @Throws(PageException::class)
        fun afterCall(pc: PageContext?, futures: List<Future<Data<Object?>?>?>?, es: ExecutorService?): Boolean {
            return try {
                val it: Iterator<Future<Data<Object?>?>?> = futures!!.iterator()
                var d: Data<Object?>
                while (it.hasNext()) {
                    d = it.next().get()
                    if (Caster.toBooleanValue(d.result)) return true
                    pc.write(d.output)
                }
                false
            } catch (e: Exception) {
                throw Caster.toPageException(e)
            } finally {
                es.shutdown()
            }
        }
    }
}