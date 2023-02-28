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
package tachyon.transformer.bytecode.statement

import tachyon.runtime.exp.TemplateException

/**
 * A single Statement
 */
abstract class StatementBase(factory: Factory?, start: Position?, end: Position?) : Statement {
    private var start: Position?
    private var end: Position?
    private var parent: Statement? = null
    private var hasReturnChild = -1
    private val factory: Factory?
    @Override
    fun getParent(): Statement? {
        return parent
    }

    @Override
    fun getFactory(): Factory? {
        return factory
    }

    /**
     * @see tachyon.transformer.bytecode.Statement.setParent
     */
    @Override
    fun setParent(parent: Statement?) {
        this.parent = parent
        if (hasReturnChild != -1 && parent != null) parent.setHasFlowController(hasReturnChild == 1)
    }

    /**
     * write out the statement to adapter
     *
     * @param adapter
     * @throws TemplateException
     */
    @Override
    @Throws(TransformerException::class)
    fun writeOut(c: Context?) {
        val bc: BytecodeContext? = c as BytecodeContext?
        ExpressionUtil.visitLine(bc, start)
        _writeOut(bc)
        ExpressionUtil.visitLine(bc, end)
    }

    /**
     * write out the statement to the adapter
     *
     * @param adapter
     * @throws TransformerException
     */
    @Throws(TransformerException::class)
    abstract fun _writeOut(bc: BytecodeContext?)

    /**
     * sets the line value.
     *
     * @param line The line to set.
     */
    @Override
    fun setStart(start: Position?) {
        this.start = start
    }

    @Override
    fun setEnd(end: Position?) {
        this.end = end
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
    fun hasFlowController(): Boolean {
        return hasReturnChild == 1
    }

    @Override
    fun setHasFlowController(hasReturnChild: Boolean) {
        if (parent != null) parent.setHasFlowController(hasReturnChild)
        this.hasReturnChild = if (hasReturnChild) 1 else 0
    }

    /**
     * constructor of the class
     *
     * @param line
     */
    init {
        this.factory = factory
        this.start = start
        this.end = end
    }
}