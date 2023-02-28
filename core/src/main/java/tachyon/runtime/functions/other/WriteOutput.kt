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
/**
 * Implements the CFML Function writeoutput
 */
package tachyon.runtime.functions.other

import java.io.IOException

class WriteOutput : BIF() {
    @Override
    @Throws(PageException::class)
    operator fun invoke(pc: PageContext?, args: Array<Object?>?): Object? {
        if (args!!.size == 2) return call(pc, Caster.toString(args[0]), Caster.toString(args[1]))
        if (args.size == 1) return call(pc, Caster.toString(args[0]))
        throw FunctionException(pc, "WriteOutput", 1, 2, args.size)
    }

    companion object {
        @Throws(PageException::class)
        fun call(pc: PageContext?, string: String?): Boolean {
            try {
                pc.forceWrite(string)
            } catch (e: IOException) {
                throw Caster.toPageException(e)
            }
            return true
        }

        @Throws(PageException::class)
        fun call(pc: PageContext?, string: String?, encodeFor: String?): Boolean {
            try {
                if (!StringUtil.isEmpty(string)) pc.forceWrite(ESAPIUtil.esapiEncode(pc, encodeFor, string)) else pc.forceWrite(string)
            } catch (e: IOException) {
                throw Caster.toPageException(e)
            }
            return true
        }
    }
}