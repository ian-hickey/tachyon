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

class Filter : BIF(), ClosureFunc {
    @Override
    @Throws(PageException::class)
    operator fun invoke(pc: PageContext?, args: Array<Object?>?): Object? {
        if (args!!.size == 2) return call(pc, args[0], Caster.toFunction(args[1]))
        if (args.size == 3) return call(pc, args[0], Caster.toFunction(args[1]), Caster.toBooleanValue(args[2]))
        if (args.size == 4) return call(pc, args[0], Caster.toFunction(args[1]), Caster.toBooleanValue(args[2]), Caster.toDoubleValue(args[3]))
        throw FunctionException(pc, "Filter", 2, 4, args.size)
    }

    companion object {
        private const val serialVersionUID = -5940580562772523622L
        @Throws(PageException::class)
        fun call(pc: PageContext?, obj: Object?, udf: UDF?): Object? {
            return _call(pc, obj, udf, false, 20, TYPE_UNDEFINED)
        }

        @Throws(PageException::class)
        fun call(pc: PageContext?, obj: Object?, udf: UDF?, parallel: Boolean): Object? {
            return _call(pc, obj, udf, parallel, 20, TYPE_UNDEFINED)
        }

        @Throws(PageException::class)
        fun call(pc: PageContext?, obj: Object?, udf: UDF?, parallel: Boolean, maxThreads: Double): Object? {
            return _call(pc, obj, udf, parallel, maxThreads.toInt(), TYPE_UNDEFINED)
        }

        @Throws(PageException::class)
        fun call(pc: PageContext?, obj: Object?, udf: UDF?, parallel: Boolean, maxThreads: Int, type: Short): Object? {
            return _call(pc, obj, udf, parallel, maxThreads, type)
        }

        @Throws(PageException::class)
        private fun _call(pc: PageContext?, obj: Object?, udf: UDF?, parallel: Boolean, maxThreads: Int, type: Short): Collection? {
            var execute: ExecutorService? = null
            var futures: List<Future<Data<Pair<Object?, Object?>?>?>?>? = null
            if (parallel) {
                execute = Executors.newFixedThreadPool(maxThreads)
                futures = ArrayList<Future<Data<Pair<Object?, Object?>?>?>?>()
            }
            val coll: Collection?
            // !!!! Don't combine the first 3 ifs with the ifs below, type overrules instanceof check
            // Array
            if (type == TYPE_ARRAY) {
                coll = invoke(pc, obj as Array?, udf, execute, futures)
            } else if (type == TYPE_QUERY) {
                coll = invoke(pc, obj as Query?, udf, execute, futures)
            } else if (type == TYPE_STRUCT) {
                coll = invoke(pc, obj as Struct?, udf, execute, futures)
            } else if (obj is Array && obj !is Argument) {
                coll = invoke(pc, obj as Array?, udf, execute, futures)
            } else if (obj is Query) {
                coll = invoke(pc, obj as Query?, udf, execute, futures)
            } else if (obj is Struct) {
                coll = invoke(pc, obj as Struct?, udf, execute, futures)
            } else if (obj is Iteratorable) {
                coll = invoke(pc, obj as Iteratorable?, udf, execute, futures)
            } else if (obj is Map<*, *>) {
                coll = invoke(pc, obj as Map<*, *>?, udf, execute, futures)
            } else if (obj is List) {
                coll = invoke(pc, obj as List?, udf, execute, futures)
            } else if (obj is Iterator) {
                coll = invoke(pc, obj as Iterator?, udf, execute, futures)
            } else if (obj is Enumeration) {
                coll = invoke(pc, obj as Enumeration?, udf, execute, futures)
            } else if (obj is StringListData) {
                coll = invoke(pc, obj as StringListData?, udf, execute, futures)
            } else throw FunctionException(pc, "Filter", 1, "data", "cannot iterate througth this type " + Caster.toTypeName(obj.getClass()))
            if (parallel) afterCall(pc, coll, futures, execute)
            return coll
        }

        @Throws(CasterException::class, PageException::class)
        private operator fun invoke(pc: PageContext?, arr: Array?, udf: UDF?, es: ExecutorService?, futures: List<Future<Data<Pair<Object?, Object?>?>?>?>?): Collection? {
            val rtn: Array = ArrayImpl()
            val async = es != null
            val it: Iterator = if (arr is ArrayPro) (arr as ArrayPro?).entryArrayIterator() else arr.entryIterator()
            var e: Entry
            var res: Object?
            while (it.hasNext()) {
                e = it.next() as Entry
                res = _inv(pc, udf, arrayOf<Object?>(e.getValue(), Caster.toDoubleValue(e.getKey()), arr), e.getKey(), e.getValue(), es, futures)
                if (!async && Caster.toBooleanValue(res)) {
                    rtn.append(e.getValue())
                }
            }
            return rtn
        }

        @Throws(CasterException::class, PageException::class)
        private operator fun invoke(pc: PageContext?, sld: StringListData?, udf: UDF?, es: ExecutorService?, futures: List<Future<Data<Pair<Object?, Object?>?>?>?>?): Collection? {
            val arr: Array = ListUtil.listToArray(sld.list, sld.delimiter, sld.includeEmptyFieldsx, sld.multiCharacterDelimiter)
            val rtn: Array = ArrayImpl()
            val it: Iterator = if (arr is ArrayPro) (arr as ArrayPro).entryArrayIterator() else arr.entryIterator()
            var e: Entry
            val k: KeyImpl? = null
            val async = es != null
            var res: Object?
            while (it.hasNext()) {
                e = it.next() as Entry
                res = _inv(pc, udf, arrayOf<Object?>(e.getValue(), Caster.toDoubleValue(e.getKey()), sld.list, sld.delimiter), e.getKey(), e.getValue(), es, futures)
                if (!async && Caster.toBooleanValue(res)) {
                    rtn.append(e.getValue())
                }
            }
            return rtn
        }

        @Throws(CasterException::class, PageException::class)
        private operator fun invoke(pc: PageContext?, qry: Query?, udf: UDF?, es: ExecutorService?, futures: List<Future<Data<Pair<Object?, Object?>?>?>?>?): Collection? {
            val colNames: Array<Key?> = qry.getColumnNames()
            val rtn: Query = QueryImpl(colNames, 0, qry.getName())
            val pid: Int = pc.getId()
            val it = ForEachQueryIterator(pc, qry, pid)
            var rowNbr: Int
            var row: Object
            val async = es != null
            var res: Object?
            while (it.hasNext()) {
                row = it.next()
                rowNbr = qry.getCurrentrow(pid)
                res = _inv(pc, udf, arrayOf<Object?>(row, Caster.toDoubleValue(rowNbr), qry), rowNbr, qry, es, futures)
                if (!async && Caster.toBooleanValue(res)) {
                    addRow(qry, rtn, rowNbr)
                }
            }
            return rtn
        }

        private fun addRow(src: Query?, trg: Query?, srcRow: Int) {
            val colNames: Array<Key?> = src.getColumnNames()
            val trgRow: Int = trg.addRow()
            for (c in colNames.indices) {
                trg.setAtEL(colNames[c], trgRow, src.getAt(colNames[c], srcRow, null))
            }
        }

        @Throws(CasterException::class, PageException::class)
        private operator fun invoke(pc: PageContext?, list: List?, udf: UDF?, es: ExecutorService?, futures: List<Future<Data<Pair<Object?, Object?>?>?>?>?): Collection? {
            val rtn: Array = ArrayImpl()
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
                if (!async && Caster.toBooleanValue(res)) rtn.append(v)
            }
            return rtn
        }

        @Throws(PageException::class)
        private operator fun invoke(pc: PageContext?, sct: Struct?, udf: UDF?, es: ExecutorService?, futures: List<Future<Data<Pair<Object?, Object?>?>?>?>?): Struct? {
            val rtn: Struct = if (sct is StructImpl) StructImpl((sct as StructImpl?).getType()) else StructImpl()
            val it: Iterator<Entry<Key?, Object?>?> = sct.entryIterator()
            var e: Entry<Key?, Object?>?
            val async = es != null
            var res: Object?
            while (it.hasNext()) {
                e = it.next()
                res = _inv(pc, udf, arrayOf<Object?>(e.getKey().getString(), e.getValue(), sct), e.getKey(), e.getValue(), es, futures)
                if (!async && Caster.toBooleanValue(res)) rtn.set(e.getKey(), e.getValue())
            }
            return rtn
        }

        @Throws(PageException::class)
        private operator fun invoke(pc: PageContext?, map: Map<*, *>?, udf: UDF?, es: ExecutorService?, futures: List<Future<Data<Pair<Object?, Object?>?>?>?>?): Struct? {
            val rtn: Struct = StructImpl()
            val it: Iterator<Entry?> = map.entrySet().iterator()
            var e: Entry?
            val async = es != null
            var res: Object?
            while (it.hasNext()) {
                e = it.next()
                res = _inv(pc, udf, arrayOf(e.getKey(), e.getValue(), map), e.getKey(), e.getValue(), es, futures)
                if (!async && Caster.toBooleanValue(res)) rtn.set(KeyImpl.toKey(e.getKey()), e.getValue())
            }
            return rtn
        }

        @Throws(PageException::class)
        private operator fun invoke(pc: PageContext?, i: Iteratorable?, udf: UDF?, es: ExecutorService?, futures: List<Future<Data<Pair<Object?, Object?>?>?>?>?): Struct? {
            val it: Iterator<Entry<Key?, Object?>?> = i.entryIterator()
            val rtn: Struct = StructImpl()
            var e: Entry<Key?, Object?>?
            val async = es != null
            var res: Object?
            while (it.hasNext()) {
                e = it.next()
                res = _inv(pc, udf, arrayOf<Object?>(e.getKey().getString(), e.getValue()), e.getKey(), e.getValue(), es, futures)
                if (!async && Caster.toBooleanValue(res)) rtn.set(e.getKey(), e.getValue())
            }
            return rtn
        }

        @Throws(PageException::class)
        private operator fun invoke(pc: PageContext?, it: Iterator?, udf: UDF?, es: ExecutorService?, futures: List<Future<Data<Pair<Object?, Object?>?>?>?>?): Array? {
            val rtn: Array = ArrayImpl()
            var v: Object
            val async = es != null
            var res: Object?
            var count = 0
            var k: ArgumentIntKey
            while (it.hasNext()) {
                v = it.next()
                k = ArgumentIntKey.init(++count)
                res = _inv(pc, udf, arrayOf<Object?>(v), k, v, es, futures)
                if (!async && Caster.toBooleanValue(res)) rtn.append(v)
            }
            return rtn
        }

        @Throws(PageException::class)
        private operator fun invoke(pc: PageContext?, e: Enumeration?, udf: UDF?, es: ExecutorService?, futures: List<Future<Data<Pair<Object?, Object?>?>?>?>?): Array? {
            val rtn: Array = ArrayImpl()
            var v: Object
            val async = es != null
            var res: Object?
            var count = 0
            var k: ArgumentIntKey
            while (e.hasMoreElements()) {
                v = e.nextElement()
                k = ArgumentIntKey.init(++count)
                res = _inv(pc, udf, arrayOf<Object?>(v), k, v, es, futures)
                if (!async && Caster.toBooleanValue(res)) rtn.append(v)
            }
            return rtn
        }

        @Throws(PageException::class)
        private fun _inv(pc: PageContext?, udf: UDF?, args: Array<Object?>?, key: Object?, value: Object?, es: ExecutorService?, futures: List<Future<Data<Pair<Object?, Object?>?>?>?>?): Object? {
            if (es == null) {
                return udf.call(pc, args, true)
            }
            futures.add(es.submit(UDFCaller2<Pair<Object?, Object?>?>(pc, udf, args, Pair<Object?, Object?>(key, value), true)))
            return null
        }

        @Throws(PageException::class)
        fun afterCall(pc: PageContext?, coll: Collection?, futures: List<Future<Data<Pair<Object?, Object?>?>?>?>?, es: ExecutorService?) {
            try {
                var isArray = false
                var isQuery = false
                if (coll is Array) isArray = true else if (coll is Query) isQuery = true
                val it: Iterator<Future<Data<Pair<Object?, Object?>?>?>?> = futures!!.iterator()
                var d: Data<Pair<Object?, Object?>?>
                while (it.hasNext()) {
                    d = it.next().get()
                    if (Caster.toBooleanValue(d.result)) {
                        if (isArray) (coll as Array?).append(d.passed.getValue()) else if (isQuery) addRow(d.passed.getValue() as Query, coll as Query?, Caster.toIntValue(d.passed.getName())) else coll.set(KeyImpl.toKey(d.passed.getName()), d.passed.getValue())
                    }
                    pc.write(d.output)
                }
            } catch (e: Exception) {
                throw Caster.toPageException(e)
            } finally {
                es.shutdown()
            }
        }
    }
}