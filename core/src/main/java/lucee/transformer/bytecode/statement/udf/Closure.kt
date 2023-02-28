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
package lucee.transformer.bytecode.statement.udf

import lucee.transformer.Position

class Closure : Function {
    constructor(name: Expression?, returnType: Expression?, returnFormat: Expression?, output: Expression?, bufferOutput: Expression?, access: Int, displayName: Expression?,
                description: Expression?, hint: Expression?, secureJson: Expression?, verifyClient: Expression?, localMode: Expression?, cachedWithin: Literal?, modifier: Int, body: Body?,
                start: Position?, end: Position?) : super(name, returnType, returnFormat, output, bufferOutput, access, displayName, description, hint, secureJson, verifyClient, localMode, cachedWithin, modifier, body,
            start, end) {
    }

    constructor(name: String?, access: Int, modifier: Int, returnType: String?, body: Body?, start: Position?, end: Position?) : super(name, access, modifier, returnType, body, start, end) {}

    @Override
    @Throws(TransformerException::class)
    override fun _writeOut(bc: BytecodeContext?, pageType: Int) {
        createFunction(bc, index, TYPE_CLOSURE)
    }

    @Override
    fun getType(): Int {
        return TYPE_CLOSURE
    }
}