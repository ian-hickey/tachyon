/**
 * Copyright (c) 2014, the Railo Company Ltd.
 * Copyright (c) 2015, Tachyon Assosication Switzerland
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
package tachyon.transformer.bytecode.op

import org.objectweb.asm.Type

class OpString private constructor(left: Expression?, right: Expression?) : ExpressionBase(left.getFactory(), left.getStart(), right.getEnd()), ExprString {
    private val right: ExprString?
    private val left: ExprString?
    @Override
    @Throws(TransformerException::class)
    fun _writeOut(bc: BytecodeContext?, mode: Int): Type? {
        left.writeOut(bc, MODE_REF)
        right.writeOut(bc, MODE_REF)
        bc.getAdapter().invokeVirtual(Types.STRING, METHOD_CONCAT)
        return Types.STRING
    }

    companion object {
        // String concat (String)
        private val METHOD_CONCAT: Method? = Method("concat", Types.STRING, arrayOf<Type?>(Types.STRING))
        private const val MAX_SIZE = 65535
        fun toExprString(left: Expression?, right: Expression?, concatStatic: Boolean): ExprString? {
            if (concatStatic && left is Literal && right is Literal) {
                val l: String = (left as Literal?).getString()
                val r: String = (right as Literal?).getString()
                if (l.length() + r.length() <= MAX_SIZE) return left.getFactory().createLitString(l.concat(r), left.getStart(), right.getEnd())
            }
            return OpString(left, right)
        }
    }

    init {
        this.left = left.getFactory().toExprString(left)
        this.right = left.getFactory().toExprString(right)
    }
}