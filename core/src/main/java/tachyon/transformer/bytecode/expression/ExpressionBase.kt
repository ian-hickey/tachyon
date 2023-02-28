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
package tachyon.transformer.bytecode.expression

import org.objectweb.asm.Type

/**
 * An Expression (Operation, Literal aso.)
 */
abstract class ExpressionBase(factory: Factory?, start: Position?, end: Position?) : Expression {
    private var start: Position?
    private var end: Position?
    private val factory: Factory?
    @Override
    @Throws(TransformerException::class)
    fun writeOut(c: Context?, mode: Int): Class<*>? {
        return try {
            Types.toClass(writeOutAsType(c, mode))
        } catch (e: ClassException) {
            throw TransformerException(c, e, null)
        }
    }

    @Throws(TransformerException::class)
    fun writeOutAsType(c: Context?, mode: Int): Type? {
        val bc: BytecodeContext? = c as BytecodeContext?
        ExpressionUtil.visitLine(bc, start)
        val type: Type? = _writeOut(bc, mode)
        ExpressionUtil.visitLine(bc, end)
        return type
    }

    /**
     * write out the statement to the adapter
     *
     * @param adapter
     * @param mode
     * @return return Type of expression
     * @throws TemplateException
     */
    @Throws(TransformerException::class)
    abstract fun _writeOut(bc: BytecodeContext?, mode: Int): Type?
    @Override
    fun getFactory(): Factory? {
        return factory
    }

    @Override
    fun getStart(): Position? {
        return start
    }

    @Override
    fun getEnd(): Position? {
        return end
    }

    @Override
    fun setStart(start: Position?) {
        this.start = start
    }

    @Override
    fun setEnd(end: Position?) {
        this.end = end
    }

    init {
        this.start = start
        this.end = end
        this.factory = factory
    }
}