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
package tachyon.transformer.cfml.evaluator.func.impl

import tachyon.runtime.exp.TemplateException

class StructKeyExists : FunctionEvaluator {
    @Override
    @Throws(TemplateException::class)
    fun execute(bif: BIF?, flf: FunctionLibFunction?) {
        val arg: Argument = bif.getArguments().get(1)
        val value: Expression = arg.getValue()
        if (value is LitString) {
            val str: String = (value as LitString).getString()

            // update first arg
            arg.setValue(CollectionKey(bif.getFactory(), str), Collection.Key::class.java.getName())
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