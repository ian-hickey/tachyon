/**
 * Copyright (c) 2014, the Railo Company Ltd.
 * Copyright (c) 2015, Tachyon Assosication Switzerland
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
package tachyon.runtime.services

import java.io.IOException

class DataSourceServiceImpl : ServiceSupport(), DataSourceService {
    private var maxQueryCount: Number? = Double.valueOf(500)
    @Override
    fun getDefaults(): Struct? {
        val sct: Struct = StructImpl()
        sct.setEL("alter", Boolean.TRUE)
        sct.setEL("blob_buffer", Double.valueOf(64000))
        sct.setEL("buffer", Double.valueOf(64000))
        sct.setEL("create", Boolean.TRUE)
        sct.setEL("delete", Boolean.TRUE)
        sct.setEL("disable", Boolean.FALSE)
        sct.setEL("disable_blob", Boolean.TRUE)
        sct.setEL("disable_clob", Boolean.TRUE)
        sct.setEL("drop", Boolean.TRUE)
        sct.setEL("grant", Boolean.TRUE)
        sct.setEL("insert", Boolean.TRUE)
        sct.setEL("pooling", Boolean.TRUE)
        sct.setEL("revoke", Boolean.TRUE)
        sct.setEL("select", Boolean.TRUE)
        sct.setEL("storedproc", Boolean.TRUE)
        sct.setEL("update", Boolean.TRUE)
        sct.setEL("", Boolean.TRUE)
        sct.setEL("", Boolean.TRUE)
        sct.setEL("", Boolean.TRUE)
        sct.setEL("interval", Double.valueOf(420))
        sct.setEL("login_timeout", Double.valueOf(30))
        sct.setEL("timeout", Double.valueOf(1200))
        return sct
    }

    @Override
    fun getMaxQueryCount(): Number? {
        return maxQueryCount
    }

    @Override
    fun setMaxQueryCount(maxQueryCount: Number?) {
        this.maxQueryCount = maxQueryCount
    }

    @Override
    fun encryptPassword(pass: String?): String? {
        throw PageRuntimeException(ServiceException("method [encryptPassword] is not supported for datasource service"))
        // return pass;
    }

    @Override
    fun getDbdir(): String? {
        val db: Resource = config().getConfigDir().getRealResource("db")
        if (!db.exists()) db.createNewFile()
        return db.getPath()
    }

    @Override
    fun getCachedQuery(key: String?): Object? {
        throw PageRuntimeException(ServiceException("method [getQueryCache] is not supported for datasource service"))
    }

    @Override
    fun setCachedQuery(arg0: String?, arg1: Object?) {
        throw PageRuntimeException(ServiceException("method [setQueryCache] is not supported for datasource service"))
    }

    @Override
    @Throws(IOException::class)
    fun purgeQueryCache() {
        val pc: PageContext = pc()
        if (pc != null) try {
            pc.getConfig().getCacheHandlerCollection(Config.CACHE_TYPE_QUERY, null).clean(pc)
        } catch (e: PageException) {
            throw ExceptionUtil.toIOException(e)
        }
        // if(pc!=null)pc.getQueryCache().clearUnused(pc);
    }

    @Override
    fun disableConnection(name: String?): Boolean {
        return false
    }

    @Override
    fun isJadoZoomLoaded(): Boolean {
        return false
    }

    @Override
    @Throws(ServiceException::class, SecurityException::class)
    fun getDrivers(): Struct? {
        checkReadAccess()
        val rtn: Struct = StructImpl()
        var driver: Struct?
        try {
            val tachyonContext: Resource = ResourceUtil.toResourceExisting(pc(), "/tachyon/admin/dbdriver/")
            val children: Array<Resource?> = tachyonContext.listResources(ExtensionResourceFilter(Constants.getComponentExtensions()))
            var name: String
            for (i in children.indices) {
                driver = StructImpl()
                name = ListFirst.call(pc(), children[i].getName(), ".", false, 1)
                driver.setEL(KeyConstants._name, name)
                driver.setEL("handler", children[i].getName())
                rtn.setEL(name, driver)
            }
        } catch (e: ExpressionException) {
            throw ServiceException(e.getMessage())
        }
        return rtn
    }

    @Override
    @Throws(SecurityException::class)
    fun getDatasources(): Struct? { // MUST muss struct of struct zurueckgeben!!!
        checkReadAccess()
        val sources: Array<tachyon.runtime.db.DataSource?> = config().getDataSources()
        val rtn: Struct = StructImpl()
        for (i in sources.indices) {
            rtn.setEL(sources[i].getName(), DataSourceImpl(sources[i]))
        }
        return rtn
    }

    @Override
    @Throws(SecurityException::class)
    fun getNames(): Array? {
        checkReadAccess()
        val sources: Array<tachyon.runtime.db.DataSource?> = config().getDataSources()
        val names: Array = ArrayImpl()
        for (i in sources.indices) {
            names.appendEL(sources[i].getName())
        }
        return names
    }

    @Override
    @Throws(SQLException::class, SecurityException::class)
    fun removeDatasource(name: String?) {
        checkWriteAccess()
        try {
            val admin: ConfigAdmin = ConfigAdmin.newInstance(config(), null)
            admin.removeDataSource(name)
        } catch (e: Exception) {
            // ignoriert wenn die db nicht existiert
        }
    }

    @Override
    @Throws(SQLException::class, SecurityException::class)
    fun verifyDatasource(name: String?): Boolean {
        checkReadAccess()
        val d: tachyon.runtime.db.DataSource? = _getDatasource(name)
        val pc: PageContext = pc()
        val manager: DataSourceManager = pc.getDataSourceManager()
        return try {
            manager.releaseConnection(pc, manager.getConnection(pc, name, d.getUsername(), d.getPassword()))
            true
        } catch (e: PageException) {
            false
        }
    }

    @Override
    @Throws(SQLException::class, SecurityException::class)
    fun getDatasource(name: String?): DataSource? {
        return DataSourceImpl(_getDatasource(name))
    }

    @Throws(SQLException::class, SecurityException::class)
    private fun _getDatasource(name: String?): tachyon.runtime.db.DataSource? {
        var name = name
        checkReadAccess()
        name = name.trim()
        val sources: Array<tachyon.runtime.db.DataSource?> = config().getDataSources()
        for (i in sources.indices) {
            if (sources[i].getName().equalsIgnoreCase(name)) return sources[i]
        }
        throw SQLException("no datasource with name [$name] found")
    }
}