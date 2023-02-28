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
package lucee.transformer.bytecode.expression.type

import org.objectweb.asm.Type

class CollectionKeyArray : ExpressionBase {
    private var arr: Array<String?>?

    constructor(factory: Factory?, arr: Array<String?>?) : super(factory, null, null) {
        this.arr = arr
    }

    constructor(factory: Factory?, arr: Array<String?>?, start: Position?, end: Position?) : super(factory, start, end) {
        this.arr = arr
    }

    @Override
    @Throws(TransformerException::class)
    fun _writeOut(bc: BytecodeContext?, mode: Int): Type? {
        val adapter: GeneratorAdapter = bc.getAdapter()
        val av = ArrayVisitor()
        av.visitBegin(adapter, Types.COLLECTION_KEY, arr!!.size)
        for (y in arr.indices) {
            av.visitBeginItem(adapter, y)
            CollectionKey(getFactory(), arr!![y])._writeOut(bc, mode)
            // adapter.push(arr[y]);
            av.visitEndItem(bc.getAdapter())
        }
        av.visitEnd()
        return Types.COLLECTION_KEY_ARRAY
    }
}