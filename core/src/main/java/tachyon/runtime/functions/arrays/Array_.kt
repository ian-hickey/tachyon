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
 * Implements the CFML Function array
 */
package tachyon.runtime.functions.arrays

import tachyon.runtime.PageContext

/**
 * implementation of the Function array
 */
class Array_ : BIF() {
    @Override
    @Throws(PageException::class)
    operator fun invoke(pc: PageContext?, args: Array<Object?>?): Object? {
        return call(pc, args!![0] as Array<Object?>?)
    }

    companion object {
        private const val serialVersionUID = 4974431571073577001L

        /**
         * @param pc
         * @param objArr
         * @return
         * @throws ExpressionException
         */
        fun call(pc: PageContext?, objArr: Array<Object?>?): Array? {
            /*
		 * if(objArr.length==0) return new ArrayImpl(objArr);
		 * 
		 * 
		 * // ordered struct if(allowStruct && objArr[0] instanceof FunctionValue) { return
		 * Struct_._call(objArr,
		 * "invalid argument for literal ordered struct, only named arguments are allowed like {name:\"value\",name2:\"value2\"}"
		 * ,Struct.TYPE_LINKED); }
		 * 
		 * // for(int i=0;i<objArr.length;i++) { if(objArr[i] instanceof FunctionValue) throw new
		 * ExpressionException("invalid argument for literal array, named arguments are not allowed");
		 * //objArr[i]=((FunctionValue)objArr[i]).getValue(); }
		 */
            return ArrayImpl(objArr)
        }
    }
}