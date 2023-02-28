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
package tachyon.runtime.op

import tachyon.commons.lang.ExceptionUtil

object Elvis {
    /**
     * called by the Elvis operator from generated bytecode
     *
     * @param pc
     * @param scope
     * @param varNames
     * @return
     */
    fun operate(pc: PageContext, scope: Double, varNames: Array<Collection.Key>): Boolean {
        return _operate(pc, scope, varNames, 0)
    }

    /**
     * called by the Elvis operator from generated bytecode
     *
     * @param pc
     * @param scope
     * @param varNames
     * @return
     */
    fun operate(pc: PageContext, scope: Double, varNames: Array<String?>?): Boolean {
        return _operate(pc, scope, KeyImpl.toKeyArray(varNames), 0)
    }

    /**
     * called by the Elvis operator from the interpreter
     *
     * @param pc
     * @param scope
     * @param varNames
     * @return
     */
    fun operate(pc: PageContext, varNames: Array<String?>): Boolean {
        val scope: Int = VariableInterpreter.scopeString2Int(pc.ignoreScopes(), varNames[0])
        return _operate(pc, scope.toDouble(), KeyImpl.toKeyArray(varNames), if (scope == Scope.SCOPE_UNDEFINED) 0 else 1)
    }

    private fun _operate(pc: PageContext, scope: Double, varNames: Array<Collection.Key>, startIndex: Int): Boolean {
        val defVal: Object? = null
        try {
            var coll: Object = VariableInterpreter.scope(pc, scope.toInt(), false)
            // Object coll =pc.scope((int)scope);
            val vu: VariableUtilImpl = pc.getVariableUtil() as VariableUtilImpl
            for (i in startIndex until varNames.size) {
                coll = vu.getCollection(pc, coll, varNames[i], defVal)
                if (coll === defVal) return false
            }
        } catch (t: Throwable) {
            ExceptionUtil.rethrowIfNecessary(t)
            return false
        }
        return true
    }
}