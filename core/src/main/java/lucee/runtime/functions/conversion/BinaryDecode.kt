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
package lucee.runtime.functions.conversion

import lucee.runtime.PageContext

/**
 * Decodes Binary Data that are encoded as String
 */
class BinaryDecode : BIF(), Function {
    @Override
    @Throws(PageException::class)
    operator fun invoke(pc: PageContext?, args: Array<Object?>?): Object? {
        return if (args!!.size == 2) call(pc, Caster.toString(args[0]), Caster.toString(args[1])) else if (args.size == 3) call(pc, Caster.toString(args[0]), Caster.toString(args[1]), Caster.toBooleanValue(args[2])) else throw FunctionException(pc, "BinaryDecode", 2, 3, args.size)
    }

    companion object {
        private const val serialVersionUID = -2161056028357718268L
        @Throws(PageException::class)
        fun call(pc: PageContext?, encoded_binary: String?, binaryencoding: String?): ByteArray? {
            return call(pc, encoded_binary, binaryencoding, false)
        }

        @Throws(PageException::class)
        fun call(pc: PageContext?, encoded_binary: String?, binaryencoding: String?, precise: Boolean): ByteArray? {
            return try {
                Coder.decode(binaryencoding, encoded_binary, precise)
            } catch (e: CoderException) {
                throw Caster.toPageException(e)
            }
        }
    }
}