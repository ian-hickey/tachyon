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
package lucee.transformer.bytecode.statement.tag

import lucee.transformer.Factory

class TagJavaScript(f: Factory?, start: Position?, end: Position?) : TagBaseNoFinal(f, start, end) {
    /**
     * @see lucee.transformer.bytecode.statement.StatementBase._writeOut
     */
    @Override
    @Throws(TransformerException::class)
    override fun _writeOut(bc: BytecodeContext?) {
        val ifv = IfVisitor()
        ifv.visitBeforeExpression()
        bc.getAdapter().push(true)
        ifv.visitAfterExpressionBeforeBody(bc)
        getBody().writeOut(bc)
        ifv.visitAfterBody(bc)
    }
}