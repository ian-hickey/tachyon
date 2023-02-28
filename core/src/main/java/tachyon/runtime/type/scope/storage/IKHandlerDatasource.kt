package tachyon.runtime.type.scope.storage

import java.sql.SQLException

class IKHandlerDatasource : IKHandler {
    protected var storeEmpty: Boolean = Caster.toBooleanValue(SystemUtil.getSystemPropOrEnvVar("tachyon.store.empty", null), false)

    @Override
    @Throws(PageException::class)
    override fun loadData(pc: PageContext?, appName: String?, name: String?, strType: String?, type: Int, log: Log?): IKStorageValue? {
        val query: Query
        val config: ConfigPro = ThreadLocalPageContext.getConfig(pc) as ConfigPro
        var dc: DatasourceConnection? = null
        try {
            val pool: DatasourceConnPool = config.getDatasourceConnectionPool(pc.getDataSource(name), null, null)
            dc = pool.borrowObject()
            val executor: SQLExecutor = SQLExecutionFactory.getInstance(dc)
            if (!dc.getDatasource().isStorage()) throw ApplicationException("storage usage for this datasource is disabled, you can enable this in the Tachyon administrator.")
            query = executor.select(config, pc.getCFID(), pc.getApplicationContext().getName(), dc, type, log, true)
        } catch (se: SQLException) {
            throw Caster.toPageException(se)
        } finally {
            if (dc != null) (dc as DatasourceConnectionPro?).release()
        }
        if (query != null && config.debug()) {
            val debugUsage: Boolean = DebuggerUtil.debugQueryUsage(pc, query)
            pc.getDebugger().addQuery(if (debugUsage) query else null, name, "", query.getSql(), query.getRecordcount(), (pc as PageContextImpl?).getCurrentPageSource(null),
                    query.getExecutionTime())
        }
        val _isNew = query.getRecordcount() === 0
        if (_isNew) {
            ScopeContext.debug(log, "create new " + strType + " scope for " + pc.getApplicationContext().getName() + "/" + pc.getCFID() + " in datasource [" + name + "]")
            return null
        }
        var str: String? = Caster.toString(query.getAt(KeyConstants._data, 1))

        // old style
        var b: Boolean
        if (str.startsWith("struct:").also { b = it } || str.startsWith("{") && str.endsWith("}")) {
            if (b) str = str.substring(7)
            try {
                return toIKStorageValue(pc.evaluate(str) as Struct)
            } catch (e: Exception) {
            }
            return null
        }
        return try {
            ScopeContext.info(log, "load existing data from [" + name + "." + PREFIX + "_" + strType + "_data] to create " + strType + " scope for "
                    + pc.getApplicationContext().getName() + "/" + pc.getCFID())
            JavaConverter.deserialize(str)
        } catch (e: Exception) {
            ScopeContext.error(log, e)
            null
            // throw Caster.toPageException(e);
        }
    }

    @Override
    override fun store(storageScope: IKStorageScopeSupport?, pc: PageContext?, appName: String?, name: String?, data: Map<Key?, IKStorageScopeItem?>?, log: Log?) {
        var pc: PageContext? = pc
        var dc: DatasourceConnection? = null
        val ci: ConfigPro = ThreadLocalPageContext.getConfig(pc) as ConfigPro
        try {
            pc = ThreadLocalPageContext.get(pc)
            val ds: DataSource
            ds = if (pc != null) pc.getDataSource(name) else ci.getDataSource(name)
            val pool: DatasourceConnPool = ci.getDatasourceConnectionPool(ds, null, null)
            dc = pool.borrowObject()
            val executor: SQLExecutor = SQLExecutionFactory.getInstance(dc)
            val existingVal: IKStorageValue? = loadData(pc, appName, name, storageScope!!.getTypeAsString(), storageScope!!.getType(), log)
            if (storeEmpty || storageScope!!.hasContent()) {
                val sv = IKStorageValue(IKStorageScopeSupport.prepareToStore(data, existingVal, storageScope!!.lastModified()))
                executor.update(ci, pc.getCFID(), appName, dc, storageScope!!.getType(), sv, storageScope!!.getTimeSpan(), log)
            } else if (existingVal != null) {
                executor.delete(ci, pc.getCFID(), appName, dc, storageScope!!.getType(), log)
            }
        } catch (e: Exception) {
            ScopeContext.error(log, e)
        } finally {
            if (dc != null) (dc as DatasourceConnectionPro?).release()
        }
    }

    @Override
    override fun unstore(storageScope: IKStorageScopeSupport?, pc: PageContext?, appName: String?, name: String?, log: Log?) {
        var pc: PageContext? = pc
        val ci: ConfigPro = ThreadLocalPageContext.getConfig(pc) as ConfigPro
        var dc: DatasourceConnection? = null
        try {
            pc = ThreadLocalPageContext.get(pc) // FUTURE change method interface
            val ds: DataSource
            ds = if (pc != null) pc.getDataSource(name) else ci.getDataSource(name)
            val pool: DatasourceConnPool = ci.getDatasourceConnectionPool(ds, null, null)
            dc = pool.borrowObject()
            val executor: SQLExecutor = SQLExecutionFactory.getInstance(dc)
            executor.delete(ci, pc.getCFID(), appName, dc, storageScope!!.getType(), log)
        } catch (t: Throwable) {
            ExceptionUtil.rethrowIfNecessary(t)
            ScopeContext.error(log, t)
        } finally {
            if (dc != null) (dc as DatasourceConnectionPro?).release()
        }
    }

    @Override
    override fun getType(): String? {
        return "Datasource"
    }

    companion object {
        val PREFIX: String? = "cf"
        @Throws(PageException::class)
        fun toIKStorageValue(sct: Struct?): IKStorageValue? {
            // last modified
            var lastModified: Long = 0
            var o: Object = sct.get(KeyConstants._lastvisit, null)
            if (o is Date) lastModified = (o as Date).getTime() else {
                o = sct.get(KeyConstants._timecreated, null)
                if (o is Date) lastModified = (o as Date).getTime()
            }
            if (lastModified == 0L) lastModified = System.currentTimeMillis()
            val map: Map<Collection.Key?, IKStorageScopeItem?> = HashMap<Collection.Key?, IKStorageScopeItem?>()
            val it: Iterator<Entry<Key?, Object?>?> = sct.entryIterator()
            var e: Entry<Key?, Object?>?
            while (it.hasNext()) {
                e = it.next()
                map.put(e.getKey(), IKStorageScopeItem(e.getValue(), lastModified))
            }
            return IKStorageValue(map)
        }
    }
}