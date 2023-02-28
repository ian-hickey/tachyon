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
package tachyon.runtime.functions.arrays

import tachyon.runtime.PageContext

class ArrayEach : BIF() {
    @Override
    @Throws(PageException::class)
    operator fun invoke(pc: PageContext?, args: Array<Object?>?): Object? {
        if (args!!.size == 2) return call(pc, Caster.toArray(args[0]), Caster.toFunction(args[1]))
        if (args.size == 3) return call(pc, Caster.toArray(args[0]), Caster.toFunction(args[1]), Caster.toBooleanValue(args[2]))
        if (args.size == 4) return call(pc, Caster.toArray(args[0]), Caster.toFunction(args[1]), Caster.toBooleanValue(args[2]), Caster.toDoubleValue(args[3]))
        throw FunctionException(pc, "ArrayEach", 2, 4, args.size)
    }

    companion object {
        private const val serialVersionUID = -2271260656749514177L
        @Throws(PageException::class)
        fun call(pc: PageContext?, array: Array?, udf: UDF?): String? {
            return _call(pc, array, udf, false, 20)
        }

        @Throws(PageException::class)
        fun call(pc: PageContext?, array: Array?, udf: UDF?, parallel: Boolean): String? {
            return _call(pc, array, udf, parallel, 20)
        }

        @Throws(PageException::class)
        fun call(pc: PageContext?, array: Array?, udf: UDF?, parallel: Boolean, maxThreads: Double): String? {
            return _call(pc, array, udf, parallel, maxThreads.toInt())
        }

        @Throws(PageException::class)
        private fun _call(pc: PageContext?, array: Array?, udf: UDF?, parallel: Boolean, maxThreads: Int): String? {
            return Each.call(pc, array, udf, parallel, maxThreads)

            /*
		 * ExecutorService execute=null; List<Future<Data<Object>>> futures=null; if(parallel) { execute =
		 * Executors.newFixedThreadPool(maxThreads); futures=new ArrayList<Future<Data<Object>>>(); }
		 * Each.invoke(pc, array, udf,execute,futures);
		 * 
		 * if(parallel) Each.afterCall(pc,futures);
		 * 
		 * return null;
		 */
        }
    }
}