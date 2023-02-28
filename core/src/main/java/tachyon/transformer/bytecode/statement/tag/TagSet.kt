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
package tachyon.transformer.bytecode.statement.tag

import org.objectweb.asm.Type

class TagSet(f: Factory?, start: Position?, end: Position?) : TagBaseNoFinal(f, start, end) {
    @Override
    @Throws(TransformerException::class)
    override fun _writeOut(bc: BytecodeContext?) {
        val rtn: Type = (getAttribute("noname")!!.getValue() as ExpressionBase).writeOutAsType(bc, Expression.MODE_VALUE)
        // TODO sollte nicht auch long geprueft werden?
        ASMUtil.pop(bc.getAdapter(), rtn)
        // if(rtn.equals(Types.DOUBLE_VALUE))bc.getAdapter().pop2();
        // else bc.getAdapter().pop();
    }
}