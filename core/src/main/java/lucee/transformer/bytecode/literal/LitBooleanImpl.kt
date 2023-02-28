/**
 * Copyright (c) 2015, Lucee Assosication Switzerland. All rights reserved.
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
package lucee.transformer.bytecode.literal

import java.math.BigDecimal

/**
 * Literal Boolean
 */
class LitBooleanImpl
/**
 * constructor of the class
 *
 * @param b
 * @param line
 */(f: Factory?, private val b: Boolean, start: Position?, end: Position?) : ExpressionBase(f, start, end), LitBoolean, ExprBoolean {
    /**
     * @see java.lang.Object.toString
     */
    @Override
    override fun toString(): String {
        return b.toString() + ""
    }

    @Override
    fun getNumber(defaultValue: Number?): Number? {
        return if (AppListenerUtil.getPreciseMath(null, null)) if (b) BigDecimal.ONE else BigDecimal.ZERO else Caster.toDouble(b)
    }

    /**
     * @return return value as double value
     */
    fun getDoubleValue(): Double {
        return Caster.toDoubleValue(b)
    }

    /**
     * @return return value as Double Object
     */
    fun getDouble(): Double? {
        return Caster.toDouble(b)
    }

    /**
     * @see lucee.transformer.expression.literal.Literal.getString
     */
    @Override
    fun getString(): String? {
        return Caster.toString(b)
    }

    /**
     * @return return value as a Boolean Object
     */
    fun getBoolean(): Boolean? {
        return Caster.toBoolean(b)
    }

    /**
     * @return return value as a boolean value
     */
    @Override
    fun getBooleanValue(): Boolean {
        return b
    }

    /**
     *
     * @see lucee.transformer.bytecode.expression.ExpressionBase._writeOut
     */
    @Override
    fun _writeOut(bc: BytecodeContext?, mode: Int): Type? {
        val adapter: GeneratorAdapter = bc.getAdapter()
        if (mode == MODE_REF) {
            adapter.getStatic(Types.BOOLEAN, if (b) "TRUE" else "FALSE", Types.BOOLEAN)
            return Types.BOOLEAN
        }
        adapter.visitInsn(if (b) Opcodes.ICONST_1 else Opcodes.ICONST_0)
        return Types.BOOLEAN_VALUE
    }

    @Override
    fun getBoolean(defaultValue: Boolean?): Boolean? {
        return getBoolean()
    }
}