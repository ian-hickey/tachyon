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
package lucee.transformer.bytecode

import lucee.runtime.exp.TemplateException

/**
 * A single Statement
 */
interface Statement {
    /**
     * sets parent statement to statement
     *
     * @param parent
     */
    fun setParent(parent: Statement?)
    fun hasFlowController(): Boolean
    fun setHasFlowController(has: Boolean)

    /**
     * @return returns the parent statement
     */
    fun getParent(): Statement?

    /**
     * write out the statement to adapter
     *
     * @param c
     * @throws TemplateException
     */
    @Throws(TransformerException::class)
    fun writeOut(c: Context?)

    /**
     * sets the line value.
     *
     * @param line The line to set.
     */
    fun setStart(startLine: Position?)

    /**
     * sets the line value.
     *
     * @param line The line to set.
     */
    fun setEnd(endLine: Position?)

    /**
     * @return the startLine
     */
    fun getStart(): Position?

    /**
     * @return the endLine
     */
    fun getEnd(): Position?

    /**
     * @return return the label where the finally block of this tags starts, IF there is a finally
     * block, otherwise return null;
     */
    fun getFlowControlFinal(): FlowControlFinal?
    fun getFactory(): Factory?
}