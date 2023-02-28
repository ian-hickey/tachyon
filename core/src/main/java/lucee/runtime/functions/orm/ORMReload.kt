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

object ORMReload {
    @Throws(PageException::class)
    fun call(pc: PageContext?): String? {

        // flush and close session
        val session: ORMSession = ORMUtil.getSession(pc, false)
        if (session != null) { // MUST do the same with all sesson using the same engine
            val config: ORMConfiguration = session.getEngine().getConfiguration(pc)
            if (config.autoManageSession()) {
                session.flushAll(pc)
                session.closeAll(pc)
            }
        }
        pc.getApplicationContext().reinitORM(pc)
        ORMUtil.resetEngine(pc, true)
        return null
    }
}