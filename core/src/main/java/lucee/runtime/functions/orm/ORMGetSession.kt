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
package lucee.runtime.functions.orm

import lucee.runtime.PageContext

class ORMGetSession : BIF() {
    @Override
    @Throws(PageException::class)
    operator fun invoke(pc: PageContext?, args: Array<Object?>?): Object? {
        return if (args!!.size == 0) call(pc) else call(pc, Caster.toString(args[0]))
    }

    companion object {
        private const val serialVersionUID = 349899413869883140L
        @Throws(PageException::class)
        fun call(pc: PageContext?): Object? {
            return call(pc, null)
        }

        @Throws(PageException::class)
        fun call(pc: PageContext?, datasource: String?): Object? {
            val dsn: String = ORMUtil.getDataSource(pc, datasource).getName()
            return ORMUtil.getSession(pc).getRawSession(dsn)
        }
    }
}