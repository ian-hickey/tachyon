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
package lucee.transformer.bytecode.expression.`var`

import org.objectweb.asm.Type

class Argument(value: Expression?, type: String?) : ExpressionBase(value.getFactory(), value.getStart(), value.getEnd()) {
    private var raw: Expression?
    private var type: String?

    /**
     * @return the value
     */
    fun getValue(): Expression? {
        return raw.getFactory().toExpression(raw, type)
    }

    /**
     * return the uncasted value
     *
     * @return
     */
    fun getRawValue(): Expression? {
        return raw
    }

    fun setValue(value: Expression?, type: String?) {
        raw = value
        this.type = type
    }

    /**
     *
     * @see lucee.transformer.bytecode.expression.ExpressionBase._writeOut
     */
    @Override
    @Throws(TransformerException::class)
    fun _writeOut(bc: BytecodeContext?, mode: Int): Type? {
        return (getValue() as ExpressionBase?).writeOutAsType(bc, mode)
    }

    @Throws(TransformerException::class)
    fun writeOutValue(bc: BytecodeContext?, mode: Int): Type? {
        ExpressionUtil.visitLine(bc, getStart())
        val t: Type = (getValue() as ExpressionBase?).writeOutAsType(bc, mode)
        ExpressionUtil.visitLine(bc, getEnd())
        return t
    }

    /**
     * @return the type
     */
    fun getStringType(): String? {
        return type
    }

    init {
        raw = value // Cast.toExpression(value,type);
        this.type = type
    }
}