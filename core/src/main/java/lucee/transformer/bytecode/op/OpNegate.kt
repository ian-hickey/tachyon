/**
 * Copyright (c) 2014, the Railo Company Ltd.
 * Copyright (c) 2015, Lucee Assosication Switzerland
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
package lucee.transformer.bytecode.op

import org.objectweb.asm.Label

class OpNegate private constructor(expr: Expression?, start: Position?, end: Position?) : ExpressionBase(expr.getFactory(), start, end), ExprBoolean {
    private val expr: ExprBoolean?

    /**
     *
     * @see lucee.transformer.bytecode.expression.ExpressionBase._writeOut
     */
    @Override
    @Throws(TransformerException::class)
    fun _writeOut(bc: BytecodeContext?, mode: Int): Type? {
        val adapter: GeneratorAdapter = bc.getAdapter()
        if (mode == MODE_REF) {
            _writeOut(bc, MODE_VALUE)
            adapter.invokeStatic(Types.CASTER, Methods.METHOD_TO_BOOLEAN_FROM_BOOLEAN)
            return Types.BOOLEAN
        }
        val l1 = Label()
        val l2 = Label()
        expr.writeOut(bc, MODE_VALUE)
        adapter.ifZCmp(Opcodes.IFEQ, l1)
        adapter.visitInsn(Opcodes.ICONST_0)
        adapter.visitJumpInsn(Opcodes.GOTO, l2)
        adapter.visitLabel(l1)
        adapter.visitInsn(Opcodes.ICONST_1)
        adapter.visitLabel(l2)
        return Types.BOOLEAN_VALUE
    } /*
	 * public int getType() { return Types._BOOLEAN; }
	 */

    companion object {
        /**
         * Create a String expression from an Expression
         *
         * @param left
         * @param right
         *
         * @return String expression
         * @throws TemplateException
         */
        fun toExprBoolean(expr: Expression?, start: Position?, end: Position?): ExprBoolean? {
            if (expr is Literal) {
                val b: Boolean = (expr as Literal?).getBoolean(null)
                if (b != null) {
                    return expr.getFactory().createLitBoolean(!b.booleanValue(), start, end)
                }
            }
            return OpNegate(expr, start, end)
        }
    }

    init {
        this.expr = expr.getFactory().toExprBoolean(expr)
    }
}