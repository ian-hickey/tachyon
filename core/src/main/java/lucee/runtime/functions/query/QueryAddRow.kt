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
 * Implements the CFML Function queryaddrow
 */
package lucee.runtime.functions.query

import lucee.runtime.PageContext

class QueryAddRow : BIF() {
    @Override
    @Throws(PageException::class)
    operator fun invoke(pc: PageContext?, args: Array<Object?>?): Object? {
        return if (args!!.size == 1) call(pc, Caster.toQuery(args[0])) else call(pc, Caster.toQuery(args[0]), args[1])
    }

    companion object {
        private const val serialVersionUID = 1252130736067181453L
        fun call(pc: PageContext?, query: Query?): Double {
            return query.addRow()
        }

        @Throws(PageException::class)
        fun call(pc: PageContext?, query: Query?, numberOrData: Object?): Double {
            if (numberOrData == null) return call(pc, query) else if (Decision.isNumber(numberOrData)) {
                return (query as QueryImpl?).addRowAndGet(Caster.toIntValue(numberOrData))
            } else {
                QueryNew.populate(pc, query, numberOrData, false)
            }
            return query.getRecordcount()
        }
    }
}