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

class IsNull : FunctionEvaluator {
    @Override
    @Throws(TemplateException::class)
    fun execute(bif: BIF?, flf: FunctionLibFunction?) {
        val arg: Argument = bif.getArguments().get(0)
        val value: Expression = arg.getValue()

        // set all member to safe navigated
        if (value is Variable) {
            val `var`: Variable = value as Variable
            /*
			 * LDEV-1201 List<Member> members = var.getMembers(); for(Member m:members) {
			 * m.setSafeNavigated(true); }
			 */`var`.setDefaultValue(value.getFactory().createNull())
        }
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