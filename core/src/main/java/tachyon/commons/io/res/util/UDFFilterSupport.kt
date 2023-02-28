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
package tachyon.commons.io.res.util

import tachyon.commons.lang.CFTypes

abstract class UDFFilterSupport(udf: UDF) {
    protected var udf: UDF
    protected var args: Array<Object?> = arrayOfNulls<Object>(1)

    @Override
    override fun toString(): String {
        return "UDFFilter:$udf"
    }

    init {
        this.udf = udf

        // check UDF return type
        var type: Int = udf.getReturnType()
        if (type != CFTypes.TYPE_BOOLEAN && type != CFTypes.TYPE_ANY) throw ExpressionException("invalid return type [" + udf.getReturnTypeAsString().toString() + "] for UDF Filter, valid return types are [boolean,any]")

        // check UDF arguments
        val args: Array<FunctionArgument> = udf.getFunctionArguments()
        if (args.size > 1) throw ExpressionException("UDF filter has too many arguments [" + args.size + "], should have at maximum 1 argument")
        if (args.size == 1) {
            type = args[0].getType()
            if (type != CFTypes.TYPE_STRING && type != CFTypes.TYPE_ANY) throw ExpressionException("invalid type [" + args[0].getTypeAsString().toString() + "] for first argument of UDF Filter, valid return types are [string,any]")
        }
    }
}