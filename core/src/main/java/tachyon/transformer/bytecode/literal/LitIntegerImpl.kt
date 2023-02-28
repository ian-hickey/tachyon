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
package tachyon.transformer.bytecode.literal

import org.objectweb.asm.Type

/**
 * Literal Double Value
 */
class LitIntegerImpl
/**
 * constructor of the class
 *
 * @param d
 * @param line
 */(f: Factory?, private val i: Int, start: Position?, end: Position?) : ExpressionBase(f, start, end), LitInteger, ExprInt {
    /**
     * @return return value as int
     */
    @Override
    fun geIntValue(): Int {
        return i
    }

    /**
     * @return return value as Double Object
     */
    @Override
    fun getInteger(): Integer? {
        return Integer.valueOf(i)
    }

    @Override
    fun getNumber(): Number? {
        return getInteger()
    }

    @Override
    fun getNumber(defaultValue: Number?): Number? {
        return getInteger()
    }

    /**
     * @see tachyon.transformer.expression.literal.Literal.getString
     */
    @Override
    fun getString(): String? {
        return Caster.toString(i)
    }

    /**
     * @return return value as a Boolean Object
     */
    fun getBoolean(): Boolean? {
        return Caster.toBoolean(i)
    }

    /**
     * @return return value as a boolean value
     */
    fun getBooleanValue(): Boolean {
        return Caster.toBooleanValue(i)
    }

    /**
     * @see tachyon.transformer.expression.Expression._writeOut
     */
    @Override
    fun _writeOut(bc: BytecodeContext?, mode: Int): Type? {
        val adapter: GeneratorAdapter = bc.getAdapter()
        adapter.push(i)
        if (mode == MODE_REF) {
            adapter.invokeStatic(Types.CASTER, Methods.METHOD_TO_INTEGER_FROM_INT)
            return Types.INTEGER
        }
        return Types.INT_VALUE
    }

    @Override
    fun getBoolean(defaultValue: Boolean?): Boolean? {
        return getBoolean()
    }
}