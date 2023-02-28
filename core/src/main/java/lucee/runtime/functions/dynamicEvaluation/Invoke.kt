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
package lucee.runtime.functions.dynamicEvaluation

import lucee.commons.lang.StringUtil

object Invoke : Function {
    private const val serialVersionUID = 3451409617437302246L
    private val EMPTY: Struct? = StructImpl()
    @Throws(PageException::class)
    fun call(pc: PageContext?, obj: Object?, name: String?): Object? {
        return call(pc, obj, name, null)
    }

    @Throws(PageException::class)
    fun call(pc: PageContext?, obj: Object?, name: String?, arguments: Object?): Object? {
        var obj: Object? = obj
        var arguments: Object? = arguments
        if (arguments == null) arguments = EMPTY
        if (obj is String) {
            obj = if (StringUtil.isEmpty(obj as String?)) {
                if (pc.getActiveComponent() != null) pc.getActiveComponent() else pc.variablesScope()
            } else pc.loadComponent(Caster.toString(obj))
        }
        if (Decision.isStruct(arguments)) {
            var args: Struct = Caster.toStruct(arguments)
            if (args === arguments && args != null) args = args.duplicate(false) as Struct
            return pc.getVariableUtil().callFunctionWithNamedValues(pc, obj, KeyImpl.init(name), args)
        }
        var args: Array<Object?>? = Caster.toNativeArray(arguments)
        if (args == arguments && args != null) {
            val tmp: Array<Object?> = arrayOfNulls<Object?>(args.size)
            for (i in args.indices) {
                tmp[i] = args[i]
            }
            args = tmp
        }
        return pc.getVariableUtil().callFunctionWithoutNamedValues(pc, obj, KeyImpl.init(name), args)
    }
}