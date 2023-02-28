/**
 * Copyright (c) 2015, Tachyon Assosication Switzerland. All rights reserved.
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
package tachyon.transformer.bytecode.expression

import org.objectweb.asm.Type

class FunctionAsExpression(function: Function?) : ExpressionBase(function.getFactory(), function.getStart(), function.getEnd()) {
    private val function: Function?

    @Override
    @Throws(TransformerException::class)
    override fun _writeOut(bc: BytecodeContext?, mode: Int): Type? {
        function._writeOut(bc)
        return Types.UDF_IMPL
    }

    /**
     * @return the closure
     */
    fun getFunction(): Function? {
        return function
    }

    init {
        this.function = function
    }
}