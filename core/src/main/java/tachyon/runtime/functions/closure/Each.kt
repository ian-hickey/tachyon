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
 * Implements the CFML Function arrayavg
 */
package tachyon.runtime.functions.closure

import java.util.ArrayList

class Each : BIF(), ClosureFunc {
    @Override
    @Throws(PageException::class)
    operator fun invoke(pc: PageContext?, args: Array<Object?>?): Object? {
        if (args!!.size == 2) return call(pc, args[0], Caster.toFunction(args[1]))
        if (args.size == 3) return call(pc, args[0], Caster.toFunction(args[1]), Caster.toBooleanValue(args[2]))
        if (args.size == 4) return call(pc, args[0], Caster.toFunction(args[1]), Caster.toBooleanValue(args[2]), Caster.toDoubleValue(args[3]))
        throw FunctionException(pc, "Each", 2, 4, args.size)
    }

    companion object {
        private const val serialVersionUID = 1955185705863596525L
        @Throws(PageException::class)
        fun call(pc: PageContext?, obj: Object?, udf: UDF?): String? {
            return _call(pc, obj, udf, false, 20, TYPE_UNDEFINED)
        }

        @Throws(PageException::class)
        fun call(pc: PageContext?, obj: Object?, udf: UDF?, parallel: Boolean): String? {
            return _call(pc, obj, udf, parallel, 20, TYPE_UNDEFINED)
        }

        @Throws(PageException::class)
        fun call(pc: PageContext?, obj: Object?, udf: UDF?, parallel: Boolean, maxThreads: Double): String? {
            return _call(pc, obj, udf, parallel, maxThreads.toInt(), TYPE_UNDEFINED)
        }

        @Throws(PageException::class)
        private fun _call(pc: PageContext?, obj: Object?, udf: UDF?, parallel: Boolean, maxThreads: Int, type: Short): String? {
            var execute: ExecutorService? = null
            var futures: List<Future<Data<Object?>?>?>? = null
            if (parallel) {
                execute = Executors.newFixedThreadPool(maxThreads)
                futures = ArrayList<Future<Data<Object?>?>?>()
            }

            // !!!! Don't combine the first 2 ifs with the ifs below, type overrules instanceof check
            // Array
            if (type == TYPE_ARRAY) {
                invoke(pc, obj as Array?, udf, execute, futures)
            } else if (type == TYPE_QUERY) {
                invoke(pc, obj as Query?, udf, execute, futures)
            } else if (obj is Array && obj !is Argument) {
                invoke(pc, obj as Array?, udf, execute, futures)
            } else if (obj is Query) {
                invoke(pc, obj as Query?, udf, execute, futures)
            } else if (obj is Iteratorable) {
                invoke(pc, obj as Iteratorable?, udf, execute, futures)
            } else if (obj is Map) {
                val it: Iterator = (obj as Map?).entrySet().iterator()
                var e: Entry
                while (it.hasNext()) {
                    e = it.next() as Entry
                    _call(pc, udf, arrayOf<Object?>(e.getKey(), e.getValue(), obj), execute, futures)
                    // udf.call(pc, new Object[]{e.getKey(),e.getValue()}, true);
                }
            } else if (obj is List) {
                val it: ListIterator = (obj as List?)!!.listIterator()
                var index: Int
                while (it.hasNext()) {
                    index = it.nextIndex()
                    _call(pc, udf, arrayOf<Object?>(it.next(), Double.valueOf(index), obj), execute, futures)
                    // udf.call(pc, new Object[]{it.next()}, true);
                }
            } else if (obj is Iterator) {
                val it: Iterator? = obj
                while (it.hasNext()) {
                    _call(pc, udf, arrayOf<Object?>(it.next()), execute, futures)
                    // udf.call(pc, new Object[]{it.next()}, true);
                }
            } else if (obj is Enumeration) {
                val e: Enumeration? = obj as Enumeration?
                while (e.hasMoreElements()) {
                    _call(pc, udf, arrayOf<Object?>(e.nextElement()), execute, futures)
                    // udf.call(pc, new Object[]{e.nextElement()}, true);
                }
            } else if (obj is StringListData) {
                invoke(pc, obj as StringListData?, udf, execute, futures)
            } else throw FunctionException(pc, "Each", 1, "data", "cannot iterate througth this type " + Caster.toTypeName(obj.getClass()))
            if (parallel) afterCall(pc, futures, execute)
            return null
        }

        @Throws(PageException::class)
        fun afterCall(pc: PageContext?, futures: List<Future<Data<Object?>?>?>?, es: ExecutorService?) {
            try {
                val it: Iterator<Future<Data<Object?>?>?> = futures!!.iterator()
                // Future<String> f;
                while (it.hasNext()) {
                    pc.write(it.next().get().output)
                }
            } catch (e: Exception) {
                throw Caster.toPageException(e)
            } finally {
                es.shutdown()
            }
        }

        @Throws(PageException::class)
        operator fun invoke(pc: PageContext?, array: Array?, udf: UDF?, execute: ExecutorService?, futures: List<Future<Data<Object?>?>?>?) {
            val it: Iterator = if (array is ArrayPro) (array as ArrayPro?).entryArrayIterator() else array.entryIterator()
            var e: Entry
            while (it.hasNext()) {
                e = it.next() as Entry
                _call(pc, udf, arrayOf<Object?>(e.getValue(), Caster.toDoubleValue(e.getKey()), array), execute, futures)
            }
        }

        @Throws(PageException::class)
        operator fun invoke(pc: PageContext?, qry: Query?, udf: UDF?, execute: ExecutorService?, futures: List<Future<Data<Object?>?>?>?) {
            val pid: Int = pc.getId()
            val it = ForEachQueryIterator(pc, qry, pid)
            try {
                var row: Object
                while (it.hasNext()) {
                    row = it.next()
                    _call(pc, udf, arrayOf<Object?>(row, Caster.toDoubleValue(qry.getCurrentrow(pid)), qry), execute, futures)
                }
            } finally {
                it.reset()
            }
        }

        @Throws(PageException::class)
        operator fun invoke(pc: PageContext?, coll: Iteratorable?, udf: UDF?, execute: ExecutorService?, futures: List<Future<Data<Object?>?>?>?) {
            val it: Iterator<Entry<Key?, Object?>?> = coll.entryIterator()
            var e: Entry<Key?, Object?>?
            while (it.hasNext()) {
                e = it.next()
                _call(pc, udf, arrayOf<Object?>(e.getKey().getString(), e.getValue(), coll), execute, futures)
                // udf.call(pc, new Object[]{e.getKey().getString(),e.getValue()}, true);
            }
        }

        @Throws(PageException::class)
        private operator fun invoke(pc: PageContext?, sld: StringListData?, udf: UDF?, execute: ExecutorService?, futures: List<Future<Data<Object?>?>?>?) {
            val arr: Array = ListUtil.listToArray(sld.list, sld.delimiter, sld.includeEmptyFieldsx, sld.multiCharacterDelimiter)
            val it: Iterator = if (arr is ArrayPro) (arr as ArrayPro).entryArrayIterator() else arr.entryIterator()
            var e: Entry
            while (it.hasNext()) {
                e = it.next() as Entry
                _call(pc, udf, arrayOf<Object?>(e.getValue(), Caster.toDoubleValue(e.getKey()), sld.list, sld.delimiter), execute, futures)
            }
        }

        @Throws(PageException::class)
        private fun _call(pc: PageContext?, udf: UDF?, args: Array<Object?>?, es: ExecutorService?, futures: List<Future<Data<Object?>?>?>?) {
            if (es == null) {
                udf.call(pc, args, true)
                return
            }
            futures.add(es.submit(UDFCaller2<Object?>(pc, udf, args, null, true)))
        }
    }
}