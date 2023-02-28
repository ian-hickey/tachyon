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
package lucee.transformer.bytecode.literal

import java.util.Map

class Null(f: Factory?, start: Position?, end: Position?) : ExpressionBase(f, start, end), Literal {
    @Override
    @Throws(TransformerException::class)
    fun _writeOut(bc: BytecodeContext?, mode: Int): Type? {
        ASMConstants.NULL(bc.getAdapter())
        return Types.OBJECT
    }

    @Override
    fun getNumber(defaultValue: Number?): Number? {
        return null
    }

    @Override
    fun getString(): String? {
        return null
    }

    @Override
    fun getBoolean(defaultValue: Boolean?): Boolean? {
        return null
    }

    fun toVariable(): Variable? {
        val v: Variable = getFactory().createVariable(Scope.SCOPE_UNDEFINED, getStart(), getEnd())
        v.addMember(getFactory().createDataMember(getFactory().createLitString("null")))
        return v
    }

    companion object {
        private val instances: Map<Factory?, Null?>? = ConcurrentHashMap()
        fun getSingleInstance(f: Factory?): Null? {
            var n = instances!![f]
            if (n == null) {
                instances.put(f, Null(f, null, null).also { n = it })
            }
            return n
        }
    }
}