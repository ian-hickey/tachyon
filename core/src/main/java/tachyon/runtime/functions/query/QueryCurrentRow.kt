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
package tachyon.runtime.functions.query

import tachyon.runtime.PageContext

/**
 * Implements the CFML Function querynew
 */
class QueryCurrentRow : BIF() {
    @Override
    @Throws(PageException::class)
    operator fun invoke(pc: PageContext?, args: Array<Object?>?): Object? {
        if (args!!.size == 1) return call(pc, Caster.toQuery(args[0]))
        throw FunctionException(pc, "QueryCurrentRow", 1, 1, args.size)
    }

    companion object {
        private const val serialVersionUID = 6744860152468692462L
        fun call(pc: PageContext?, qry: Query?): Double {
            return qry.getCurrentrow(pc.getId())
        }
    }
}