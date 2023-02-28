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
package tachyon.transformer.bytecode.statement

import tachyon.transformer.TransformerException

interface IFunction {
    @Throws(TransformerException::class)
    fun writeOut(bc: BytecodeContext?, type: Int)
    fun getType(): Int

    companion object {
        const val PAGE_TYPE_REGULAR = 0
        const val PAGE_TYPE_COMPONENT = 1
        const val PAGE_TYPE_INTERFACE = 2
        const val TYPE_CLOSURE = 1
        const val TYPE_LAMBDA = 2
        const val TYPE_UDF = 3
        const val ARRAY_INDEX = 0
        const val VALUE_INDEX = 1
    }
}