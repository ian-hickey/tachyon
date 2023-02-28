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
package tachyon.runtime.exp

import tachyon.runtime.type.FunctionArgument

class UDFCasterException : CasterException {
    constructor(udf: UDF?, arg: FunctionArgument?, value: Object?, index: Int) : super(createMessage(udf, arg, value, index), createDetail(udf)) {}
    constructor(udf: UDF?, returnType: String?, value: Object?) : super(createMessage(udf, returnType, value), createDetail(udf)) {}

    companion object {
        private const val serialVersionUID = 4863042711433241644L
        private fun createMessage(udf: UDF?, type: String?, value: Object?): String? {
            val detail: String
            detail = if (value is String) return "Cannot cast String [" + CasterException.crop(value).toString() + "] to a value of type [" + type.toString() + "]" else if (value != null) "Cannot cast Object type [" + Type.getName(value).toString() + "] to a value of type [" + type.toString() + "]" else "Cannot cast null value to value of type [$type]"
            return "The function [" + udf.getFunctionName().toString() + "] has an invalid return value , [" + detail + "]"
        }

        private fun createMessage(udf: UDF?, arg: FunctionArgument?, value: Object?, index: Int): String? {
            val detail: String
            detail = if (value is String) "Cannot cast String [" + CasterException.crop(value).toString() + "] to a value of type [" + arg.getTypeAsString().toString() + "]" else if (value != null) "Cannot cast Object type [" + Type.getName(value).toString() + "] to a value of type [" + arg.getTypeAsString().toString() + "]" else "Can't cast Null value to value of type [" + arg.getTypeAsString().toString() + "]"
            return "Invalid call of the function [" + udf.getFunctionName().toString() + "], " + posToString(index).toString() + " Argument [" + arg.getName().toString() + "] is of invalid type, " + detail
        }

        private fun createDetail(udf: UDF?): String? {
            return "the function is located at [" + udf.getSource().toString() + "]"
        }

        private fun posToString(index: Int): String? {
            if (index == 1) return "first"
            return if (index == 2) "second" else index.toString() + "th"
        }
    }
}