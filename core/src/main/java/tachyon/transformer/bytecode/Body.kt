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
package tachyon.transformer.bytecode

import java.util.List

/**
 * Body tag (Statement collector)
 */
interface Body : Statement {
    /**
     * adds a statement to the Page
     *
     * @param statement
     */
    fun addFirst(statement: Statement?)
    fun addStatement(statement: Statement?)

    /**
     * returns all statements
     *
     * @return the statements
     */
    fun hasStatements(): Boolean
    fun getStatements(): List<Statement?>?

    /**
     * move all statements to target body
     *
     * @param trg
     */
    fun moveStatmentsTo(trg: Body?)

    /**
     * returns if body has content or not
     *
     * @return is empty
     */
    fun isEmpty(): Boolean
    fun addPrintOut(f: Factory?, str: String?, start: Position?, end: Position?)
    fun remove(stat: Statement?)
}