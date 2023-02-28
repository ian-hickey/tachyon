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
package lucee.runtime.functions.string

import lucee.commons.lang.StringUtil

/**
 * implements the String member method isEmpty()
 */
class StringIsEmpty : BIF() {
    @Override
    @Throws(PageException::class)
    operator fun invoke(pc: PageContext?, args: Array<Object?>?): Object? {
        if (args!!.size != 1) throw FunctionException(pc, "IsEmpty", 1, 1, args.size)
        return call(pc, Caster.toString(args[0]))
    }

    companion object {
        private const val serialVersionUID = -85767818984230151L
        fun call(pc: PageContext?, value: String?): Boolean {
            return StringUtil.isEmpty(value)
        }
    }
}