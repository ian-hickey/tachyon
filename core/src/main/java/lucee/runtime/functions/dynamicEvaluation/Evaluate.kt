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

import lucee.runtime.PageContext

/**
 * Implements the CFML Function evaluate
 */
object Evaluate : Function {
    private const val serialVersionUID = 2259041678381553989L
    @Throws(PageException::class)
    fun call(pc: PageContext?, objs: Array<Object?>?): Object? {
        return call(pc, objs, false)
    }

    @Throws(PageException::class)
    fun call(pc: PageContext?, objs: Array<Object?>?, preciseMath: Boolean): Object? {
        // define another environment for the function
        if (objs!!.size > 1 && objs[objs.size - 1] is Scope) {

            // Variables Scope
            var `var`: Variables? = null
            var lcl: Local? = null
            var cLcl: Local? = null
            var arg: Argument? = null
            var cArg: Argument? = null
            if (objs[objs.size - 1] is Variables) {
                `var` = objs[objs.size - 1] as Variables?
            } else if (objs[objs.size - 1] is CallerImpl) {
                val ci: CallerImpl? = objs[objs.size - 1] as CallerImpl?
                `var` = ci.getVariablesScope()
                lcl = ci.getLocalScope()
                arg = ci.getArgumentsScope()
            }
            if (`var` != null) {
                val cVar: Variables = pc.variablesScope()
                if (cVar !== `var`) pc.setVariablesScope(`var`)
                if (lcl != null && lcl !is LocalNotSupportedScope) {
                    cLcl = pc.localScope()
                    cArg = pc.argumentsScope()
                    pc.setFunctionScopes(lcl, arg)
                }
                return try {
                    _call(pc, objs, objs.size - 1, preciseMath)
                } finally {
                    if (cVar !== `var`) pc.setVariablesScope(cVar)
                    if (cLcl != null) pc.setFunctionScopes(cLcl, cArg)
                }
            } else if (objs[objs.size - 1] is Undefined) {
                val pci: PageContextImpl? = pc as PageContextImpl?
                val undefined: Undefined? = objs[objs.size - 1] as Undefined?
                val check: Boolean = undefined.getCheckArguments()
                val orgVar: Variables = pc.variablesScope()
                val orgArgs: Argument = pc.argumentsScope()
                val orgLocal: Local = pc.localScope()
                val us: Variables = undefined.variablesScope()
                if (us !== orgVar) pci.setVariablesScope(us)
                if (check) pci.setFunctionScopes(undefined.localScope(), undefined.argumentsScope())
                return try {
                    _call(pc, objs, objs.size - 1, preciseMath)
                } finally {
                    if (us !== orgVar) pc.setVariablesScope(orgVar)
                    if (check) pci.setFunctionScopes(orgLocal, orgArgs)
                }
            }
        }
        return _call(pc, objs, objs.size, preciseMath)
    }

    @Throws(PageException::class)
    private fun _call(pc: PageContext?, objs: Array<Object?>?, len: Int, preciseMath: Boolean): Object? {
        var rst: Object? = null
        for (i in 0 until len) {
            rst = if (objs!![i] is Number) objs[i] else CFMLExpressionInterpreter(false).interpret(pc, Caster.toString(objs[i]), preciseMath)
        }
        return rst
    }
}