/**
 * Copyright (c) 2014, the Railo Company Ltd.
 * Copyright (c) 2015, Tachyon Assosication Switzerland
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
package tachyon.runtime.functions.other

import tachyon.loader.engine.CFMLEngine

object _CreateComponent {
    private val EMPTY: Array<Object?>? = arrayOfNulls<Object?>(0)
    @Throws(PageException::class)
    fun call(pc: PageContext?, objArr: Array<Object?>?): Object? {
        val path: String = Caster.toString(objArr!![objArr.size - 1])
        // not store the index to make it faster
        val c: Component = CreateObject.doComponent(pc, path)

        // no init method
        if (c.get(KeyConstants._init, null) !is UDF) {
            if (objArr.size > 1) { // we have arguments passed in
                val arg1: Object? = objArr[0]
                if (arg1 is FunctionValue) {
                    val args: Struct = Caster.toFunctionValues(objArr, 0, objArr.size - 1)
                    EntityNew.setPropeties(pc, c, args, true)
                } else if (Decision.isStruct(arg1) && !Decision.isComponent(arg1) && objArr.size == 2) { // we only do this in case there is only argument set, otherwise we assume
                    // that this is simply a missuse of the new operator
                    val args: Struct = Caster.toStruct(arg1)
                    EntityNew.setPropeties(pc, c, args, true)
                }
            }
            return c
        }
        val rtn: Object
        // no arguments
        if (objArr.size == 1) { // no args
            rtn = c.call(pc, KeyConstants._init, EMPTY)
        } else if (objArr[0] is FunctionValue) {
            val args: Struct = Caster.toFunctionValues(objArr, 0, objArr.size - 1)
            rtn = c.callWithNamedValues(pc, KeyConstants._init, args)
        } else {
            val args: Array<Object?> = arrayOfNulls<Object?>(objArr.size - 1)
            for (i in 0 until objArr.size - 1) {
                args[i] = objArr[i]
                if (args[i] is FunctionValue) throw ExpressionException("invalid argument definition, when using named parameters to a function, every parameter must have a name.")
            }
            rtn = c.call(pc, KeyConstants._init, args)
        }
        return if (rtn == null || c.getPageSource() != null && c.getPageSource().getDialect() === CFMLEngine.DIALECT_LUCEE) c else rtn
    }
}