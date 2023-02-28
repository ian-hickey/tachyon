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

import lucee.runtime.exp.TemplateException

class PrecisionEvaluate : FunctionEvaluator {
    @Override
    @Throws(TemplateException::class)
    fun execute(bif: BIF?, flf: FunctionLibFunction?) {
        val args: Array<Argument?> = bif.getArguments()
        for (arg in args) {
            val value: Expression = arg.getValue()
            if (value is OpNumber) {
                arg.setValue(value.getFactory().toExprString(toOpBigDecimal(value as OpNumber)), "any")
            }
        }
    }

    private fun toOpBigDecimal(op: OpNumber?): OpBigDecimal? {
        var left: Expression? = op.getLeft()
        var right: Expression? = op.getRight()
        if (left is OpNumber) left = toOpBigDecimal(left as OpNumber?)
        if (right is OpNumber) right = toOpBigDecimal(right as OpNumber?)
        return OpBigDecimal(left, right, op.getOperation())
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