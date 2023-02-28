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
package lucee.runtime.type.scope.storage.clean

import java.sql.SQLException

class DatasourceStorageScopeCleaner  // private String strType;
(type: Int, listener: StorageScopeListener?) : StorageScopeCleanerSupport(type, listener, INTERVALL_HOUR) {
    @Override
    override fun init(engine: StorageScopeEngine?) {
        super.init(engine)
    }

    @Override
    protected override fun _clean() {
        val config: ConfigWeb = engine.getFactory().getConfig()
        val datasources: Array<DataSource?> = config.getDataSources()
        for (i in datasources.indices) {
            if (datasources[i].isStorage()) {
                try {
                    clean(config, datasources[i])
                } catch (t: Throwable) {
                    ExceptionUtil.rethrowIfNecessary(t)
                    error(t)
                }
            }
        }
    }

    @Throws(PageException::class, SQLException::class)
    private fun clean(config: ConfigWeb?, dataSource: DataSource?) {
        val cwi: ConfigWebPro? = config as ConfigWebPro?
        var dc: DatasourceConnection? = null
        try {
            val pool: DatasourceConnPool = cwi.getDatasourceConnectionPool(dataSource, null, null)
            dc = pool.borrowObject()
            val log: Log = ThreadLocalPageContext.getLog(config, "scope")
            val executor: SQLExecutor = SQLExecutionFactory.getInstance(dc)
            executor.clean(config, dc, type, engine, this, listener, log)
        } finally {
            if (dc != null) (dc as DatasourceConnectionPro?).release()
        }
    }
}