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
package tachyon.transformer.bytecode.expression.type

import org.objectweb.asm.Type

class LiteralStringArray : ExpressionBase {
    private var arr: Array<String?>?

    constructor(f: Factory?, arr: Array<String?>?) : super(f, null, null) {
        this.arr = arr
    }

    constructor(f: Factory?, arr: Array<String?>?, start: Position?, end: Position?) : super(f, start, end) {
        this.arr = arr
    }

    @Override
    @Throws(TransformerException::class)
    fun _writeOut(bc: BytecodeContext?, mode: Int): Type? {
        val adapter: GeneratorAdapter = bc.getAdapter()
        val av = ArrayVisitor()
        av.visitBegin(adapter, Types.STRING, arr!!.size)
        for (y in arr.indices) {
            av.visitBeginItem(adapter, y)
            adapter.push(arr!![y])
            av.visitEndItem(bc.getAdapter())
        }
        av.visitEnd()
        return Types.STRING_ARRAY
    }
}