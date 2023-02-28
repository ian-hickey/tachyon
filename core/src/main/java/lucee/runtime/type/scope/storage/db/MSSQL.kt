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
package lucee.runtime.type.scope.storage.db

import java.sql.SQLException

class MSSQL : SQLExecutorSupport() {
    @Override
    @Throws(PageException::class, SQLException::class)
    override fun select(config: Config?, cfid: String?, applicationName: String?, dc: DatasourceConnection?, type: Int, log: Log?, createTableIfNotExist: Boolean): Query? {
        // TODO Auto-generated method stub
        return null
    }

    @Override
    @Throws(PageException::class, SQLException::class)
    override fun update(config: Config?, cfid: String?, applicationName: String?, dc: DatasourceConnection?, type: Int, data: Object?, timeSpan: Long, log: Log?) {
        // TODO Auto-generated method stub
    }

    @Override
    @Throws(PageException::class, SQLException::class)
    override fun delete(config: Config?, cfid: String?, appName: String?, dc: DatasourceConnection?, type: Int, log: Log?) {
        // TODO Auto-generated method stub
    }

    @Override
    @Throws(PageException::class, SQLException::class)
    override fun clean(config: Config?, dc: DatasourceConnection?, type: Int, engine: StorageScopeEngine?, cleaner: DatasourceStorageScopeCleaner?, listener: StorageScopeListener?, log: Log?) {
        // TODO Auto-generated method stub
    }
}