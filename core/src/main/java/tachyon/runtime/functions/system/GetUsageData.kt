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
package tachyon.runtime.functions.system

import java.util.Iterator

object GetUsageData : Function {
    private val START_TIME: Key? = KeyImpl.getInstance("starttime")
    private val CACHED_QUERIES: Key? = KeyImpl.getInstance("cachedqueries")
    private val OPEN_CONNECTIONS: Key? = KeyImpl.getInstance("openconnections")
    private val ACTIVE_CONNECTIONS: Key? = KeyImpl.getInstance("activeconnections")
    private val IDLE_CONNECTIONS: Key? = KeyImpl.getInstance("idleconnections")
    private val WAITING_FOR_CONNECTION: Key? = KeyImpl.getInstance("waitingForConnection")
    private val ELEMENTS: Key? = KeyImpl.getInstance("elements")
    private val USERS: Key? = KeyImpl.getInstance("users")
    private val QUERIES: Key? = KeyImpl.getInstance("queries")
    private val LOCKS: Key? = KeyImpl.getInstance("locks")
    @Throws(PageException::class)
    fun call(pc: PageContext?): Struct? {
        val cw: ConfigWeb = pc.getConfig()
        val cs: ConfigServer = cw.getConfigServer("server")
        val webs: Array<ConfigWeb?> = cs.getConfigWebs()
        CFMLEngineFactory.getInstance()
        val engine: CFMLEngineImpl = cs.getCFMLEngine() as CFMLEngineImpl
        val sct: Struct = StructImpl()

        // Locks
        /*
		 * LockManager manager = pc.getConfig().getLockManager(); String[] locks =
		 * manager.getOpenLockNames(); for(int i=0;i<locks.length;i++){ locks[i]. }
		 * if(!ArrayUtil.isEmpty(locks)) strLocks=" open locks at this time ("+List.arrayToList(locks,
		 * ", ")+").";
		 */

        // Requests
        val req: Query = QueryImpl(arrayOf<Collection.Key?>(KeyConstants._web, KeyConstants._uri, START_TIME, KeyConstants._timeout), 0, "requests")
        sct.setEL(KeyConstants._requests, req)

        // Template Cache
        val tc: Query = QueryImpl(arrayOf<Collection.Key?>(KeyConstants._web, ELEMENTS, KeyConstants._size), 0, "templateCache")
        sct.setEL(KeyImpl.getInstance("templateCache"), tc)

        // Scopes
        val scopes: Struct = StructImpl()
        sct.setEL(KeyConstants._scopes, scopes)
        val app: Query = QueryImpl(arrayOf<Collection.Key?>(KeyConstants._web, KeyConstants._application, ELEMENTS, KeyConstants._size), 0, "templateCache")
        scopes.setEL(KeyConstants._application, app)
        val sess: Query = QueryImpl(arrayOf<Collection.Key?>(KeyConstants._web, KeyConstants._application, USERS, ELEMENTS, KeyConstants._size), 0, "templateCache")
        scopes.setEL(KeyConstants._session, sess)

        // Query
        val qry: Query = QueryImpl(arrayOf<Collection.Key?>(KeyConstants._web, KeyConstants._application, START_TIME, KeyConstants._sql), 0, "requests")
        sct.setEL(QUERIES, qry)

        // Locks
        val lck: Query = QueryImpl(arrayOf<Collection.Key?>(KeyConstants._web, KeyConstants._application, KeyConstants._name, START_TIME, KeyConstants._timeout, KeyConstants._type),
                0, "requests")
        sct.setEL(LOCKS, lck)

        // Loop webs
        var web: ConfigWebPro?
        var pcs: Map<Integer?, PageContextImpl?>
        var _pc: PageContextImpl
        var row: Int
        var active = 0
        var idle = 0
        var waiters = 0
        var factory: CFMLFactoryImpl
        var queries: Array<ActiveQuery?>
        var aq: ActiveQuery?
        var locks: Array<ActiveLock?>
        var al: ActiveLock?
        for (i in webs.indices) {

            // Loop requests
            web = webs[i] as ConfigWebPro?
            factory = web.getFactory() as CFMLFactoryImpl
            pcs = factory.getActivePageContexts()
            val it: Iterator<PageContextImpl?> = pcs.values().iterator()
            while (it.hasNext()) {
                _pc = it.next()
                if (_pc.isGatewayContext()) continue

                // Request
                row = req.addRow()
                req.setAt(KeyConstants._web, row, web.getLabel())
                req.setAt(KeyConstants._uri, row, getPath(_pc.getHttpServletRequest()))
                req.setAt(START_TIME, row, DateTimeImpl(pc.getStartTime(), false))
                req.setAt(KeyConstants._timeout, row, Double.valueOf(pc.getRequestTimeout()))

                // Query
                queries = _pc.getActiveQueries()
                if (queries != null) {
                    for (y in queries.indices) {
                        aq = queries[y]
                        row = qry.addRow()
                        qry.setAt(KeyConstants._web, row, web.getLabel())
                        qry.setAt(KeyConstants._application, row, _pc.getApplicationContext().getName())
                        qry.setAt(START_TIME, row, DateTimeImpl(web, aq.startTime, true))
                        qry.setAt(KeyConstants._sql, row, aq.sql)
                    }
                }

                // Lock
                locks = _pc.getActiveLocks()
                if (locks != null) {
                    for (y in locks.indices) {
                        al = locks[y]
                        row = lck.addRow()
                        lck.setAt(KeyConstants._web, row, web.getLabel())
                        lck.setAt(KeyConstants._application, row, _pc.getApplicationContext().getName())
                        lck.setAt(KeyConstants._name, row, al.name)
                        lck.setAt(START_TIME, row, DateTimeImpl(web, al.startTime, true))
                        lck.setAt(KeyConstants._timeout, row, Caster.toDouble(al.timeoutInMillis / 1000))
                        lck.setAt(KeyConstants._type, row, if (al.type === LockManager.TYPE_EXCLUSIVE) "exclusive" else "readonly")
                    }
                }
            }
            for (pool in web.getDatasourceConnectionPools()) {
                active += pool.getNumActive()
                idle += pool.getNumIdle()
                waiters += pool.getNumWaiters()
            }

            // Template Cache
            val mappings: Array<Mapping?> = ConfigWebUtil.getAllMappings(web)
            val tce = templateCacheElements(mappings)
            row = tc.addRow()
            tc.setAt(KeyConstants._web, row, web.getLabel())
            tc.setAt(KeyConstants._size, row, Double.valueOf(tce!![1]))
            tc.setAt(ELEMENTS, row, Double.valueOf(tce!![0]))

            // Scope Application
            getAllApplicationScopes(web, factory.getScopeContext(), app)
            getAllCFSessionScopes(web, factory.getScopeContext(), sess)
        }

        // Datasource
        val ds: Struct = StructImpl()
        sct.setEL(KeyConstants._datasources, ds)
        ds.setEL(CACHED_QUERIES, Caster.toDouble(pc.getConfig().getCacheHandlerCollection(Config.CACHE_TYPE_QUERY, null).size(pc))) // there is only one cache for all contexts
        // ds.setEL(CACHED_QUERIES, Caster.toDouble(pc.getQueryCache().size(pc))); // there is only one
        // cache for all contexts
        ds.setEL(OPEN_CONNECTIONS, Caster.toDouble(active + idle))
        ds.setEL(ACTIVE_CONNECTIONS, Caster.toDouble(active))
        ds.setEL(IDLE_CONNECTIONS, Caster.toDouble(idle))
        ds.setEL(WAITING_FOR_CONNECTION, Caster.toDouble(waiters))

        // Memory
        val mem: Struct = StructImpl()
        sct.setEL(KeyConstants._memory, mem)
        mem.setEL("heap", SystemUtil.getMemoryUsageAsStruct(SystemUtil.MEMORY_TYPE_HEAP))
        mem.setEL("nonheap", SystemUtil.getMemoryUsageAsStruct(SystemUtil.MEMORY_TYPE_NON_HEAP))

        // uptime
        sct.set("uptime", DateTimeImpl(engine.uptime(), true))

        // now
        sct.set("now", DateTimeImpl(pc))

        // SizeAndCount.Size size = SizeAndCount.sizeOf(pc.serverScope());
        return sct
    }

    @Throws(PageException::class)
    private fun getAllApplicationScopes(web: ConfigWeb?, sc: ScopeContext?, app: Query?) {
        val all: Struct = sc.getAllApplicationScopes()
        val it: Iterator<Entry<Key?, Object?>?> = all.entryIterator()
        var e: Entry<Key?, Object?>?
        var row: Int
        var sac: Size
        while (it.hasNext()) {
            e = it.next()
            row = app.addRow()
            sac = SizeAndCount.sizeOf(e.getValue())
            app.setAt(KeyConstants._web, row, web.getLabel())
            app.setAt(KeyConstants._application, row, e.getKey().getString())
            app.setAt(KeyConstants._size, row, Double.valueOf(sac.size))
            app.setAt(ELEMENTS, row, Double.valueOf(sac.count))
        }
    }

    @Throws(PageException::class)
    private fun getAllCFSessionScopes(web: ConfigWeb?, sc: ScopeContext?, sess: Query?) {
        val all: Struct = sc.getAllCFSessionScopes()
        val it: Iterator = all.entryIterator()
        var itt: Iterator
        var e: Entry
        var ee: Entry
        var row: Int
        var size: Int
        var count: Int
        var users: Int
        var sac: Size
        // applications
        while (it.hasNext()) {
            e = it.next() as Entry
            itt = (e.getValue() as Map).entrySet().iterator()
            size = 0
            count = 0
            users = 0
            while (itt.hasNext()) {
                ee = itt.next() as Entry
                sac = SizeAndCount.sizeOf(ee.getValue())
                size += sac.size
                count += sac.count
                users++
            }
            row = sess.addRow()
            sess.setAt(KeyConstants._web, row, web.getLabel())
            sess.setAt(USERS, row, Double.valueOf(users))
            sess.setAt(KeyConstants._application, row, e.getKey().toString())
            sess.setAt(KeyConstants._size, row, Double.valueOf(size))
            sess.setAt(ELEMENTS, row, Double.valueOf(count))
        }
    }

    private fun templateCacheElements(mappings: Array<Mapping?>?): LongArray? {
        var elements: Long = 0
        var size: Long = 0
        var res: Resource
        var mapping: MappingImpl?
        for (i in mappings.indices) {
            mapping = mappings!![i] as MappingImpl?
            for (ps in mapping.getPageSources(true)) {
                elements++
                res = mapping.getClassRootDirectory().getRealResource(ps.getClassName().replace('.', '/').toString() + ".class")
                size += res.length()
            }
        }
        return longArrayOf(elements, size)
    }

    fun getScriptName(req: HttpServletRequest?): String? {
        return emptyIfNull(req.getContextPath()) + emptyIfNull(req.getServletPath())
    }

    fun getPath(req: HttpServletRequest?): String? {
        var qs = emptyIfNull(req.getQueryString())
        if (qs!!.length() > 0) qs = "?$qs"
        return emptyIfNull(req.getContextPath()) + emptyIfNull(req.getServletPath()) + qs
    }

    private fun emptyIfNull(str: String?): String? {
        return str ?: ""
    }
}