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
 * Implements the CFML Function isdefined
 */
package tachyon.runtime.functions.decision

import tachyon.commons.lang.ExceptionUtil

object IsDefined : Function {
    private const val serialVersionUID = -6477602189364145523L
    fun call(pc: PageContext?, varName: String?): Boolean {
        return VariableInterpreter.isDefined(pc, varName)
        // return pc.isDefined(varName);
    }

    fun call(pc: PageContext?, scope: Double, key: Collection.Key?): Boolean {
        try {
            var coll: Object = VariableInterpreter.scope(pc, scope.toInt(), false) ?: return false
            val _null: Object = NullSupportHelper.NULL(pc)
            coll = (pc.getVariableUtil() as VariableUtilImpl).get(pc, coll, key, _null)
            if (coll === _null) return false
            // return pc.scope((int)scope).get(key,null)!=null;
        } catch (t: Throwable) {
            ExceptionUtil.rethrowIfNecessary(t)
            return false
        }
        return true
    }

    fun call(pc: PageContext?, scope: Double, varNames: Array<Collection.Key?>?): Boolean {
        val defVal: Object = NullSupportHelper.NULL(pc)
        try {
            var coll: Object = VariableInterpreter.scope(pc, scope.toInt(), false)
            // Object coll =pc.scope((int)scope);
            val vu: VariableUtilImpl = pc.getVariableUtil() as VariableUtilImpl
            for (i in varNames.indices) {
                coll = vu.getCollection(pc, coll, varNames!![i], defVal)
                if (coll === defVal) return false
            }
        } catch (t: Throwable) {
            ExceptionUtil.rethrowIfNecessary(t)
            return false
        }
        return true
    }

    // used for older compiled code in ra files
    operator fun invoke(pc: PageContext?, varNames: Array<String?>?, allowNull: Boolean): Boolean {
        val scope: Int = VariableInterpreter.scopeString2Int(pc.ignoreScopes(), varNames!![0])
        val defVal: Object? = if (allowNull) Null.NULL else null
        try {
            var coll: Object = VariableInterpreter.scope(pc, scope, false)
            // Object coll =pc.scope((int)scope);
            for (i in if (scope == Scope.SCOPE_UNDEFINED) 0 else 1 until varNames.size) {
                coll = pc.getVariableUtil().getCollection(pc, coll, varNames[i], defVal)
                if (coll === defVal) return false
            }
        } catch (t: Throwable) {
            ExceptionUtil.rethrowIfNecessary(t)
            return false
        }
        return true
    }

    // used for older compiled code in ra files
    fun call(pc: PageContext?, scope: Double, key: String?): Boolean {
        return call(pc, scope, KeyImpl.getInstance(key))
    }

    // used for older compiled code in ra files
    fun call(pc: PageContext?, scope: Double, varNames: Array<String?>?): Boolean {
        return call(pc, scope, KeyImpl.toKeyArray(varNames))
    }
}