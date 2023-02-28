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

import org.objectweb.asm.Type

/**
 * Literal Double Value
 */
class LitLongImpl
/**
 * constructor of the class
 *
 * @param d
 * @param line
 */(f: Factory?, private val l: Long, start: Position?, end: Position?) : ExpressionBase(f, start, end), LitLong {
    @Override
    fun getLongValue(): Long {
        return l
    }

    @Override
    fun getLong(): Long? {
        return Long.valueOf(l)
    }

    @Override
    fun getNumber(): Number? {
        return getLong()
    }

    @Override
    fun getNumber(dv: Number?): Number? {
        return getLong()
    }

    @Override
    fun getString(): String? {
        return Caster.toString(l)
    }

    /**
     * @return return value as a Boolean Object
     */
    fun getBoolean(): Boolean? {
        return Caster.toBoolean(l)
    }

    /**
     * @return return value as a boolean value
     */
    fun getBooleanValue(): Boolean {
        return Caster.toBooleanValue(l)
    }

    /**
     * @see lucee.transformer.expression.Expression._writeOut
     */
    @Override
    fun _writeOut(bc: BytecodeContext?, mode: Int): Type? {
        val adapter: GeneratorAdapter = bc.getAdapter()
        adapter.push(l)
        if (mode == MODE_REF) {
            adapter.invokeStatic(Types.CASTER, Methods.METHOD_TO_LONG_FROM_LONG_VALUE)
            return Types.LONG
        }
        return Types.LONG_VALUE
    }

    private fun getDouble(): Double? {
        return Double.valueOf(l)
    }

    @Override
    fun getBoolean(defaultValue: Boolean?): Boolean? {
        return getBoolean()
    }
}