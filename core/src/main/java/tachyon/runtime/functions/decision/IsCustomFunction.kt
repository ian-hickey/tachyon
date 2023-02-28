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
 * Implements the CFML Function iscustomfunction
 */
package tachyon.runtime.functions.decision

import tachyon.commons.lang.StringUtil

object IsCustomFunction : Function {
    private const val serialVersionUID = 1578909692090122692L
    @Throws(FunctionException::class)
    fun call(pc: PageContext?, `object`: Object?): Boolean {
        return call(pc, `object`, null)
    }

    @Throws(FunctionException::class)
    fun call(pc: PageContext?, `object`: Object?, type: String?): Boolean {
        var type = type
        if (`object` is ObjectWrap) {
            return call(pc, (`object` as ObjectWrap?).getEmbededObject(null), type)
        }
        // no function at all
        if (!Decision.isUserDefinedFunction(`object`)) return false

        // no type we are good
        if (StringUtil.isEmpty(type, true)) return true

        // check type
        type = type.trim()
        if ("closure".equalsIgnoreCase(type)) return Decision.isClosure(`object`)
        if ("lambda".equalsIgnoreCase(type)) return Decision.isLambda(`object`)
        if ("udf".equalsIgnoreCase(type)) return !Decision.isLambda(`object`) && !Decision.isClosure(`object`)
        throw FunctionException(pc, "IsCustomFunction", 2, "type", "function type [$type] is invalid, only the following values are valid [closure,lambda,udf]")
    }
}