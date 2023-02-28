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

class CollectionKey : ExpressionBase {
    private var value: String?

    constructor(factory: Factory?, value: String?) : super(factory, null, null) {
        this.value = value
    }

    constructor(factory: Factory?, value: String?, start: Position?, end: Position?) : super(factory, start, end) {
        this.value = value
    }

    @Override
    @Throws(TransformerException::class)
    fun _writeOut(bc: BytecodeContext?, mode: Int): Type? {
        getFactory().registerKey(bc, bc.getFactory().createLitString(value), false)
        return Types.COLLECTION_KEY
    }
}