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
package lucee.transformer.cfml.evaluator.func.impl

import lucee.commons.lang.StringList

class IsDefined : FunctionEvaluator {
    @Override
    @Throws(TemplateException::class)
    fun execute(bif: BIF?, flf: FunctionLibFunction?) {
        var arg: Argument? = bif.getArguments().get(0)
        val value: Expression = arg.getValue()
        if (value is LitString) {
            var str: String = (value as LitString).getString()
            val sl: StringList = VariableInterpreter.parse(str, false)
            if (sl != null) {
                // scope
                str = sl.next()
                val scope: Int = VariableInterpreter.scopeString2Int(bif.ts.ignoreScopes, str)
                if (scope == Scope.SCOPE_UNDEFINED) sl.reset()

                // keys
                val arr: Array<String?> = sl.toArray()
                ArrayUtil.trimItems(arr)

                // update first arg
                arg.setValue(bif.getFactory().createLitNumber(scope), "number")

                // add second argument
                if (arr.size == 1) {
                    val expr: Expression = CollectionKey(bif.getFactory(), arr[0]) // LitString.toExprString(str);
                    arg = Argument(expr, Collection.Key::class.java.getName())
                    bif.addArgument(arg)
                } else {
                    val expr = CollectionKeyArray(bif.getFactory(), arr)
                    // LiteralStringArray expr = new LiteralStringArray(arr);
                    arg = Argument(expr, Array<Collection.Key>::class.java.getName())
                    bif.addArgument(arg)
                }
            }
        }
        // print.out("bif:"+arg.getValue().getClass().getName());
    }

    @Override
    @Throws(EvaluatorException::class)
    fun evaluate(bif: BIF?, flf: FunctionLibFunction?) {
    }

    @Override
    @Throws(TemplateException::class)
    fun pre(bif: BIF?, flf: FunctionLibFunction?): FunctionLibFunction? {
        return null
    }
}