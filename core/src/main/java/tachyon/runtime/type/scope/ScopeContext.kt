/**
 * Copyright (c) 2023, TachyonCFML.org
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
package tachyon.runtime.type.scope

import java.util.Iterator

/**
 * handles the Scopes, e.g. Application, Session, etc., for a ServletContext
 */
class ScopeContext(factory: CFMLFactoryImpl?) {
    private val cfSessionContexts: Map<String?, Map<String?, Scope?>?>? = MapFactory.< String, Map<String, Scope>>getConcurrentMap<String?, Map<String?, Scope?>?>()
    private val cfClientContexts: Map<String?, Map<String?, Scope?>?>? = MapFactory.< String, Map<String, Scope>>getConcurrentMap<String?, Map<String?, Scope?>?>()
    private val applicationContexts: Map<String?, Application?>? = MapFactory.< String, Application>getConcurrentMap<String?, Application?>()
    private var maxSessionTimeout = 0
    private var client: StorageScopeEngine? = null
    private var session: StorageScopeEngine? = null
    private val factory: CFMLFactoryImpl?

    /**
     * @return the log
     */
    private fun getLog(): Log? {
        return ThreadLocalPageContext.getLog(factory.getConfig(), "scope")
    }

    fun debug(msg: String?) {
        debug(getLog(), msg)
    }

    fun info(msg: String?) {
        info(getLog(), msg)
    }

    fun error(msg: String?) {
        error(getLog(), msg)
    }

    fun error(t: Throwable?) {
        error(getLog(), t)
    }

    /**
     * return a map matching key from given map
     *
     * @param parent
     * @param key key of the map
     * @return matching map, if no map exist it willbe one created
     */
    private fun getSubMap(parent: Map<String?, Map<String?, Scope?>?>?, key: String?): Map<String?, Scope?>? {
        var context: Map<String?, Scope?>? = parent!![key]
        if (context != null) return context
        context = MapFactory.< String, Scope>getConcurrentMap<String?, Scope?>()
        parent.put(key, context)
        return context
    }

    @Throws(PageException::class)
    fun getClientScope(pc: PageContext?): Client? {
        val appContext: ApplicationContext = pc.getApplicationContext()
        // get Context
        val context: Map<String?, Scope?>? = getSubMap(cfClientContexts, appContext.getName())

        // get Client
        var isMemory = false
        var storage: String? = appContext.getClientstorage()
        if (StringUtil.isEmpty(storage, true)) {
            storage = ConfigPro.DEFAULT_STORAGE_CLIENT
        } else if ("ram".equalsIgnoreCase(storage)) {
            storage = "memory"
            isMemory = true
        } else if ("registry".equalsIgnoreCase(storage)) {
            storage = "file"
        } else {
            storage = storage.toLowerCase()
            if ("memory".equals(storage)) isMemory = true
        }
        val existing: Client? = context!![pc.getCFID()] as Client?
        var client: Client? = if (appContext.getClientCluster()) null else existing
        // final boolean doMemory=isMemory || !appContext.getClientCluster();
        // client=doMemory?(Client) context.get(pc.getCFID()):null;
        if (client == null || client.isExpired() || !client.getStorage().equalsIgnoreCase(storage)) {
            if ("file".equals(storage)) {
                client = ClientFile.getInstance(appContext.getName(), pc, getLog())
            } else if ("cookie".equals(storage)) client = ClientCookie.getInstance(appContext.getName(), pc, getLog()) else if ("memory".equals(storage)) {
                if (existing != null) client = existing
                client = ClientMemory.getInstance(pc, getLog())
            } else {
                val ds: DataSource = pc.getDataSource(storage, null)
                client = if (ds != null) {
                    IKStorageScopeSupport.getInstance(Scope.SCOPE_CLIENT, IKHandlerDatasource(), appContext.getName(), storage, pc, existing, getLog()) as Client
                } else {
                    IKStorageScopeSupport.getInstance(Scope.SCOPE_CLIENT, IKHandlerCache(), appContext.getName(), storage, pc, existing, getLog()) as Client
                }
                if (client == null) {
                    // datasource not enabled for storage
                    if (ds != null) {
                        if (!ds.isStorage()) throw ApplicationException(
                                "datasource [$storage] is not enabled to be used as client storage, you have to enable it in the Tachyon administrator.")
                        throw ApplicationException("datasource [" + storage
                                + "] could not be reached for client storage. Please make sure the datasource settings are correct, and the datasource is available.")
                    }
                    val cc: CacheConnection = CacheUtil.getCacheConnection(pc, storage, null)
                    if (cc != null) throw ApplicationException(
                            "cache [$storage] is not enabled to be used  as a session/client storage, you have to enable it in the Tachyon administrator.")
                    throw ApplicationException("there is no cache or datasource with name [$storage] defined.")
                }
            }
            client.setStorage(storage)
            context.put(pc.getCFID(), client)
        } else getLog().log(Log.LEVEL_INFO, "scope-context", "use existing client scope for " + appContext.getName().toString() + "/" + pc.getCFID().toString() + " from storage " + storage)
        client.touchBeforeRequest(pc)
        return client
    }

    fun getClientScopeEL(pc: PageContext?): Client? {
        return try {
            getClientScope(pc)
        } catch (pe: PageException) {
            throw PageRuntimeException(pe)
        }
    }

    /**
     * return the session count of all application contexts
     *
     * @return
     */
    fun getSessionCount(pc: PageContext?): Int {
        return if (pc.getSessionType() === Config.SESSION_TYPE_JEE) 0 else getSessionCount()
    }

    fun getSessionCount(): Int {
        val it: Iterator<Entry<String?, Map<String?, Scope?>?>?> = cfSessionContexts.entrySet().iterator()
        var entry: Entry<String?, Map<String?, Scope?>?>?
        var count = 0
        while (it.hasNext()) {
            entry = it.next()
            count += getCount(entry.getValue())
        }
        return count
    }

    fun getClientCount(): Int {
        val it: Iterator<Entry<String?, Map<String?, Scope?>?>?> = cfClientContexts.entrySet().iterator()
        var entry: Entry<String?, Map<String?, Scope?>?>?
        var count = 0
        while (it.hasNext()) {
            entry = it.next()
            count += getCount(entry.getValue())
        }
        return count
    }

    /**
     * return the session count of this application context
     *
     * @return
     */
    fun getAppContextSessionCount(pc: PageContext?): Int {
        val appContext: ApplicationContext = pc.getApplicationContext()
        if (pc.getSessionType() === Config.SESSION_TYPE_JEE) return 0
        val context: Map<String?, Scope?>? = getSubMap(cfSessionContexts, appContext.getName())
        return getCount(context)
    }

    fun getAppContextCount(): Int {
        return applicationContexts!!.size()
    }

    private fun getCount(context: Map<String?, Scope?>?): Int {
        val it: Iterator<Entry<String?, Scope?>?> = context.entrySet().iterator()
        var entry: Entry<String?, Scope?>?
        var count = 0
        var s: StorageScope
        while (it.hasNext()) {
            entry = it.next()
            if (entry.getValue() is StorageScope) {
                s = entry.getValue() as StorageScope
                if (!s.isExpired()) count++
            }
        }
        return count
    }

    /**
     * return all session context of this application context
     *
     * @param pc
     * @return
     */
    fun getAllSessionScopes(pc: PageContext?): Struct? {
        return getAllSessionScopes(pc.getApplicationContext().getName())
    }

    fun getAllApplicationScopes(): Struct? {
        val trg: Struct = StructImpl()
        StructImpl.copy(MapAsStruct.toStruct(applicationContexts, true), trg, false)
        return trg
    }

    fun getAllCFSessionScopes(): Struct? {
        val trg: Struct = StructImpl()
        StructImpl.copy(MapAsStruct.toStruct(cfSessionContexts, true), trg, false)
        return trg
    }

    /**
     * return the size in bytes of all session contexts
     *
     * @return size in bytes
     * @throws ExpressionException
     */
    @Throws(ExpressionException::class)
    fun getScopesSize(scope: Int): Long {
        if (scope == Scope.SCOPE_APPLICATION) return SizeOf.size(applicationContexts)
        if (scope == Scope.SCOPE_CLUSTER) return SizeOf.size(cluster)
        if (scope == Scope.SCOPE_SERVER) return SizeOf.size(server)
        if (scope == Scope.SCOPE_SESSION) return SizeOf.size(cfSessionContexts)
        if (scope == Scope.SCOPE_CLIENT) return SizeOf.size(cfClientContexts)
        throw ExpressionException("can only return information of scope that are not request dependent")
    }

    /**
     * get all session contexts of given applicaton name
     *
     * @param pc
     * @param appName
     * @return
     */
    @Deprecated
    @Deprecated("use instead getAllSessionScopes(String appName)")
    fun getAllSessionScopes(pc: PageContext?, appName: String?): Struct? {
        return getAllSessionScopes(appName)
    }

    /**
     * get all session contexts of given applicaton name
     *
     * @param appName
     * @return
     */
    fun getAllSessionScopes(appName: String?): Struct? {
        // if(pc.getSessionType()==Config.SESSION_TYPE_J2EE)return new StructImpl();
        return getAllSessionScopes(getSubMap(cfSessionContexts, appName), appName)
    }

    private fun getAllSessionScopes(context: Map<String?, Scope?>?, appName: String?): Struct? {
        val it: Iterator<Entry<String?, Scope?>?> = context.entrySet().iterator()
        var entry: Entry<String?, Scope?>?
        val sct: Struct = StructImpl()
        var s: Session
        while (it.hasNext()) {
            entry = it.next()
            s = entry.getValue() as Session
            if (!s.isExpired()) sct.setEL(KeyImpl.init(appName.toString() + "_" + entry.getKey() + "_0"), s)
        }
        return sct
    }

    /**
     * return the session Scope for this context (cfid,cftoken,contextname)
     *
     * @param pc PageContext
     * @return session matching the context
     * @throws PageException
     */
    @Throws(PageException::class)
    fun getSessionScope(pc: PageContext?, isNew: RefBoolean?): Session? {
        return if (pc.getSessionType() === Config.SESSION_TYPE_APPLICATION) getCFSessionScope(pc, isNew) else getJSessionScope(pc, isNew)
    }

    fun hasExistingSessionScope(pc: PageContext?): Boolean {
        return if (pc.getSessionType() === Config.SESSION_TYPE_APPLICATION) hasExistingCFSessionScope(pc) else hasExistingJSessionScope(pc)
    }

    private fun hasExistingJSessionScope(pc: PageContext?): Boolean {
        val httpSession: HttpSession = pc.getSession() ?: return false
        val session: Session = httpSession.getAttribute(pc.getApplicationContext().getName()) as Session
        return session is JSession && !session.isExpired()
    }

    private fun hasExistingCFSessionScope(pc: PageContext?, cfid: String?): Boolean {
        val appContext: ApplicationContext = pc.getApplicationContext()
        val context: Map<String?, Scope?>? = getSubMap(cfSessionContexts, appContext.getName())
        return context!!.containsKey(cfid)
    }

    private fun hasExistingClientScope(pc: PageContext?, cfid: String?): Boolean {
        val appContext: ApplicationContext = pc.getApplicationContext()
        val context: Map<String?, Scope?>? = getSubMap(cfClientContexts, appContext.getName())
        return context!!.containsKey(cfid)
    }

    fun hasExistingCFID(pc: PageContext?, cfid: String?): Boolean {
        return if (hasExistingCFSessionScope(pc, cfid)) true else hasExistingClientScope(pc, cfid)
    }

    private fun hasExistingCFSessionScope(pc: PageContext?): Boolean {
        val appContext: ApplicationContext = pc.getApplicationContext()
        // get Context
        val context: Map<String?, Scope?>? = getSubMap(cfSessionContexts, appContext.getName())

        // get Session
        var storage: String? = appContext.getSessionstorage()
        if (StringUtil.isEmpty(storage, true)) storage = "memory" else if ("ram".equalsIgnoreCase(storage)) storage = "memory" else if ("registry".equalsIgnoreCase(storage)) storage = "file" else storage = storage.toLowerCase()
        val session: Session? = context!![pc.getCFID()] as Session?
        return if (session !is StorageScope || session.isExpired() || !(session as StorageScope?).getStorage().equalsIgnoreCase(storage)) {
            if ("memory".equals(storage)) false else if ("file".equals(storage)) SessionFile.hasInstance(appContext.getName(), pc) else if ("cookie".equals(storage)) SessionCookie.hasInstance(appContext.getName(), pc) else {
                val ds: DataSource = pc.getConfig().getDataSource(storage, null)
                if (ds != null && ds.isStorage()) {
                    IKStorageScopeSupport.hasInstance(Scope.SCOPE_SESSION, IKHandlerDatasource(), appContext.getName(), storage, pc)
                } else IKStorageScopeSupport.hasInstance(Scope.SCOPE_SESSION, IKHandlerCache(), appContext.getName(), storage, pc)
            }
        } else true
    }

    @Throws(PageException::class)
    fun getExistingCFSessionScope(applicationName: String?, cfid: String?): Session? {
        val context: Map<String?, Scope?>? = getSubMap(cfSessionContexts, applicationName)
        return if (context != null) {
            context[cfid] as Session?
        } else null
    }

    /**
     * return cf session scope
     *
     * @param pc PageContext
     * @param isNew
     * @return cf session matching the context
     * @throws PageException
     */
    @Throws(PageException::class)
    private fun getCFSessionScope(pc: PageContext?, isNew: RefBoolean?): Session? {
        val appContext: ApplicationContext = pc.getApplicationContext()
        // get Context
        val context: Map<String?, Scope?>? = getSubMap(cfSessionContexts, appContext.getName())

        // get Session
        var isMemory = false
        var storage: String? = appContext.getSessionstorage()
        if (StringUtil.isEmpty(storage, true)) {
            storage = ConfigPro.DEFAULT_STORAGE_SESSION
            isMemory = true
        } else if ("ram".equalsIgnoreCase(storage)) {
            storage = "memory"
            isMemory = true
        } else if ("registry".equalsIgnoreCase(storage)) {
            storage = "file"
        } else {
            storage = storage.toLowerCase()
            if ("memory".equals(storage)) isMemory = true
        }
        var existing: Session? = context!![pc.getCFID()] as Session?
        if (existing != null && (existing.isExpired() || existing !is StorageScope)) existing = null // second should not happen
        var session: Session? = if (appContext.getSessionCluster()) null else existing
        if (session == null || session !is StorageScope || !(session as StorageScope?).getStorage().equalsIgnoreCase(storage)) {
            // not necessary to check session in the same way, because it is overwritten anyway
            if (isMemory) {
                session = if (existing != null) existing else SessionMemory.getInstance(pc, isNew, getLog())
            } else if ("file".equals(storage)) {
                session = SessionFile.getInstance(appContext.getName(), pc, getLog())
            } else if ("cookie".equals(storage)) session = SessionCookie.getInstance(appContext.getName(), pc, getLog()) else {
                val ds: DataSource = pc.getDataSource(storage, null)
                session = if (ds != null && ds.isStorage()) {
                    IKStorageScopeSupport.getInstance(Scope.SCOPE_SESSION, IKHandlerDatasource(), appContext.getName(), storage, pc, existing, getLog()) as Session
                } else {
                    IKStorageScopeSupport.getInstance(Scope.SCOPE_SESSION, IKHandlerCache(), appContext.getName(), storage, pc, existing, getLog()) as Session
                }
                if (session == null) {
                    // datasource not enabled for storage
                    if (ds != null) {
                        if (!ds.isStorage()) throw ApplicationException("datasource [" + storage + "] is not enabled to be used as session storage, "
                                + "you have to enable it in the Tachyon administrator or define key \"storage=true\" for datasources defined in the application event handler.")
                        throw ApplicationException("datasource [" + storage
                                + "] could not be reached for session storage. Please make sure the datasource settings are correct, and the datasource is available.")
                    }
                    val cc: CacheConnection = CacheUtil.getCacheConnection(pc, storage, null)
                    if (cc != null) throw ApplicationException(
                            "cache [$storage] is not enabled to be used  as a session/client storage, you have to enable it in the Tachyon administrator.")
                    throw ApplicationException("there is no cache or datasource with name [$storage] defined.")
                }
            }
            if (session is StorageScope) (session as StorageScope?).setStorage(storage)
            context.put(pc.getCFID(), session)
            isNew.setValue(true)
        } else {
            getLog().log(Log.LEVEL_INFO, "scope-context", "use existing session scope for " + appContext.getName().toString() + "/" + pc.getCFID().toString() + " from storage " + storage)
        }
        session.touchBeforeRequest(pc)
        return session
    }

    @Throws(PageException::class)
    fun removeSessionScope(pc: PageContext?) {
        removeCFSessionScope(pc)
        removeJSessionScope(pc)
    }

    @Throws(PageException::class)
    fun removeJSessionScope(pc: PageContext?) {
        val httpSession: HttpSession = pc.getSession()
        if (httpSession != null) {
            val appContext: ApplicationContext = pc.getApplicationContext()
            httpSession.removeAttribute(appContext.getName())
        }
    }

    @Throws(PageException::class)
    fun removeCFSessionScope(pc: PageContext?) {
        val sess: Session? = getCFSessionScope(pc, RefBooleanImpl())
        val appContext: ApplicationContext = pc.getApplicationContext()
        val context: Map<String?, Scope?>? = getSubMap(cfSessionContexts, appContext.getName())
        if (context != null) {
            context.remove(pc.getCFID())
            if (sess is StorageScope) (sess as StorageScope?).unstore(pc.getConfig())
        }
    }

    @Throws(PageException::class)
    fun removeClientScope(pc: PageContext?) {
        val cli: Client? = getClientScope(pc)
        val appContext: ApplicationContext = pc.getApplicationContext()
        val context: Map<String?, Scope?>? = getSubMap(cfClientContexts, appContext.getName())
        if (context != null) {
            context.remove(pc.getCFID())
            if (cli != null) cli.unstore(pc.getConfig())
        }
    }

    fun remove(type: Int, appName: String?, cfid: String?): Boolean {
        val contexts: Map<String?, Map<String?, Scope?>?>? = if (type == Scope.SCOPE_CLIENT) cfClientContexts else cfSessionContexts
        val context: Map<String?, Scope?>? = getSubMap(contexts, appName)
        val res: Object = context.remove(cfid)
        getLog().log(Log.LEVEL_INFO, "scope-context", "remove " + VariableInterpreter.scopeInt2String(type).toString() + " scope " + appName.toString() + "/" + cfid.toString() + " from memory")
        return res != null
    }

    /**
     * return j session scope
     *
     * @param pc PageContext
     * @param isNew
     * @return j session matching the context
     * @throws PageException
     */
    @Throws(PageException::class)
    private fun getJSessionScope(pc: PageContext?, isNew: RefBoolean?): Session? {
        val httpSession: HttpSession = pc.getSession()
        val appContext: ApplicationContext = pc.getApplicationContext()
        var session: Object? = null // this is from type object, because it is possible that httpSession return object from
        // prior restart
        val s = appContext.getSessionTimeout().getSeconds() as Int
        if (maxSessionTimeout < s) maxSessionTimeout = s
        session = if (httpSession != null) {
            httpSession.setMaxInactiveInterval(maxSessionTimeout + 60)
            httpSession.getAttribute(appContext.getName())
        } else {
            val context: Map<String?, Scope?>? = getSubMap(cfSessionContexts, appContext.getName())
            context!![pc.getCFID()]
        }
        var jSession: JSession? = null
        if (session is JSession) {
            jSession = session
            try {
                if (jSession!!.isExpired()) {
                    if (httpSession == null) jSession!!.touch() else jSession = createNewJSession(pc, httpSession, isNew)
                }
                info(getLog(), "use existing JSession for " + appContext.getName().toString() + "/" + pc.getCFID())
            } catch (cce: ClassCastException) {
                error(getLog(), cce)
                // if there is no HTTPSession
                if (httpSession == null) return getCFSessionScope(pc, isNew)
                jSession = JSession()
                httpSession.setAttribute(appContext.getName(), jSession)
                isNew.setValue(true)
            }
        } else {
            // if there is no HTTPSession
            if (httpSession == null) return getCFSessionScope(pc, isNew)
            jSession = createNewJSession(pc, httpSession, isNew)
        }
        jSession!!.touchBeforeRequest(pc)
        return jSession
    }

    private fun createNewJSession(pc: PageContext?, httpSession: HttpSession?, isNew: RefBoolean?): JSession? {
        val appContext: ApplicationContext = pc.getApplicationContext()
        debug(getLog(), "create new JSession for " + appContext.getName().toString() + "/" + pc.getCFID())
        val jSession = JSession()
        httpSession.setAttribute(appContext.getName(), jSession)
        isNew.setValue(true)
        val context: Map<String?, Scope?>? = getSubMap(cfSessionContexts, appContext.getName())
        context.put(pc.getCFID(), jSession)
        return jSession
    }

    /**
     * return the application Scope for this context (cfid,cftoken,contextname)
     *
     * @param pc PageContext
     * @param isNew
     * @return session matching the context
     * @throws PageException
     */
    fun getApplicationScope(pc: PageContext?, createUpdateIfNotExist: Boolean, isNew: RefBoolean?): Application? {
        val appContext: ApplicationContext = pc.getApplicationContext()
        // getApplication Scope from Context
        val application: ApplicationImpl?
        val objApp: Object? = applicationContexts!![appContext.getName()]
        if (objApp != null) {
            application = objApp
            if (application!!.isExpired()) {
                if (!createUpdateIfNotExist) return null
                application!!.release(pc)
                isNew.setValue(true)
            }
        } else {
            if (!createUpdateIfNotExist) return null
            application = ApplicationImpl()
            applicationContexts.put(appContext.getName(), application)
            isNew.setValue(true)
        }
        application!!.touchBeforeRequest(pc)
        // if(newApplication)listener.onApplicationStart(pc);
        return application
    }

    fun removeApplicationScope(pc: PageContext?) {
        applicationContexts.remove(pc.getApplicationContext().getName())
    }

    fun getExistingApplicationScope(applicationName: String?): Application? {
        return applicationContexts!![applicationName]
    }

    /**
     * remove all unused scope objects
     */
    fun clearUnused() {
        val log: Log? = getLog()
        try {
            // create cleaner engine for session/client scope
            if (session == null) session = StorageScopeEngine(factory, log, arrayOf<StorageScopeCleaner?>(FileStorageScopeCleaner(Scope.SCOPE_SESSION, null) // new
                    // SessionEndListener())
                    , DatasourceStorageScopeCleaner(Scope.SCOPE_SESSION, null) // new
                    // SessionEndListener())
                    // ,new CacheStorageScopeCleaner(Scope.SCOPE_SESSION, new SessionEndListener())
            ))
            if (client == null) client = StorageScopeEngine(factory, log, arrayOf<StorageScopeCleaner?>(FileStorageScopeCleaner(Scope.SCOPE_CLIENT, null), DatasourceStorageScopeCleaner(Scope.SCOPE_CLIENT, null) // ,new CacheStorageScopeCleaner(Scope.SCOPE_CLIENT, null) //Cache storage need no control, if
                    // there is no listener
            ))

            // store session/client scope and remove from memory
            storeUnusedStorageScope(factory, Scope.SCOPE_CLIENT)
            storeUnusedStorageScope(factory, Scope.SCOPE_SESSION)

            // remove unused memory based client/session scope (invoke onSessonEnd)
            clearUnusedMemoryScope(factory, Scope.SCOPE_CLIENT)
            clearUnusedMemoryScope(factory, Scope.SCOPE_SESSION)

            // session must be executed first, because session creates a reference from client scope
            session.clean()
            client.clean()

            // clean all unused application scopes
            clearUnusedApplications(factory)
        } catch (t: Exception) {
            error(t)
        }
    }

    /**
     * remove all scope objects
     */
    fun clear() {
        try {
            var scope: Scope
            // Map.Entry entry,e;
            // Map context;

            // release all session scopes
            val sit: Iterator<Entry<String?, Map<String?, Scope?>?>?> = cfSessionContexts.entrySet().iterator()
            var sentry: Entry<String?, Map<String?, Scope?>?>?
            var context: Map<String?, Scope?>
            var itt: Iterator<Entry<String?, Scope?>?>
            var e: Entry<String?, Scope?>?
            val pc: PageContext = ThreadLocalPageContext.get()
            while (sit.hasNext()) {
                sentry = sit.next()
                context = sentry.getValue()
                itt = context.entrySet().iterator()
                while (itt.hasNext()) {
                    e = itt.next()
                    scope = e.getValue()
                    scope.release(pc)
                }
            }
            cfSessionContexts.clear()

            // release all application scopes
            val ait: Iterator<Entry<String?, Application?>?> = applicationContexts.entrySet().iterator()
            var aentry: Entry<String?, Application?>?
            while (ait.hasNext()) {
                aentry = ait.next()
                scope = aentry.getValue()
                scope.release(pc)
            }
            applicationContexts.clear()

            // release server scope
            if (server != null) {
                server.release(pc)
                server = null
            }
        } catch (t: Throwable) {
            ExceptionUtil.rethrowIfNecessary(t)
        }
    }

    private fun storeUnusedStorageScope(cfmlFactory: CFMLFactoryImpl?, type: Int) {
        val contexts: Map<String?, Map<String?, Scope?>?>? = if (type == Scope.SCOPE_CLIENT) cfClientContexts else cfSessionContexts
        val timespan = if (type == Scope.SCOPE_CLIENT) CLIENT_MEMORY_TIMESPAN else SESSION_MEMORY_TIMESPAN
        val strType: String = VariableInterpreter.scopeInt2String(type)
        if (contexts!!.size() === 0) return
        val now: Long = System.currentTimeMillis()
        val arrContexts: Array<Object?> = contexts.keySet().toArray()
        var applicationName: Object?
        var cfid: Object
        var o: Object
        var fhm: Map<String?, Scope?>?
        for (i in arrContexts.indices) {
            applicationName = arrContexts[i]
            fhm = contexts!![applicationName]
            if (fhm!!.size() > 0) {
                val arrClients: Array<Object?> = fhm.keySet().toArray()
                var count = arrClients.size
                for (y in arrClients.indices) {
                    cfid = arrClients[y]
                    o = fhm[cfid]
                    if (o !is StorageScope) continue
                    val scope: StorageScope = o as StorageScope
                    if (scope.lastVisit() + timespan < now && scope !is MemoryScope) {
                        getLog().log(Log.LEVEL_INFO, "scope-context",
                                "remove from memory [" + strType + "] scope for [" + applicationName + "/" + cfid + "] from storage [" + scope.getStorage() + "]")
                        fhm.remove(arrClients[y])
                        count--
                    }
                }
                if (count == 0) contexts.remove(arrContexts[i])
            }
        }
    }

    /**
     * @param cfmlFactory
     */
    private fun clearUnusedMemoryScope(cfmlFactory: CFMLFactoryImpl?, type: Int) {
        val contexts: Map<String?, Map<String?, Scope?>?>? = if (type == Scope.SCOPE_CLIENT) cfClientContexts else cfSessionContexts
        if (contexts!!.size() === 0) return
        val arrContexts: Array<Object?> = contexts.keySet().toArray()
        val listener: ApplicationListener = cfmlFactory.getConfig().getApplicationListener()
        var applicationName: Object?
        var cfid: Object
        var o: Object
        var fhm: Map<String?, Scope?>?
        for (i in arrContexts.indices) {
            applicationName = arrContexts[i]
            fhm = contexts!![applicationName]
            if (fhm!!.size() > 0) {
                val cfids: Array<Object?> = fhm.keySet().toArray()
                var count = cfids.size
                for (y in cfids.indices) {
                    cfid = cfids[y]
                    o = fhm[cfid]
                    if (o !is MemoryScope) continue
                    val scope: MemoryScope = o as MemoryScope

                    // close
                    if (scope.isExpired()) {
                        // TODO macht das sinn? ist das nicht kopierleiche?
                        val application: ApplicationImpl? = applicationContexts!![applicationName]
                        var appLastAccess: Long = 0
                        if (application != null) {
                            appLastAccess = application.getLastAccess()
                            application.touch()
                        }
                        scope.touch()
                        try {
                            if (type == Scope.SCOPE_SESSION) {
                                listener.onSessionEnd(cfmlFactory, applicationName as String?, cfid as String)
                            }
                        } catch (t: Throwable) {
                            ExceptionUtil.rethrowIfNecessary(t)
                            ExceptionHandler.log(cfmlFactory.getConfig(), Caster.toPageException(t))
                        } finally {
                            if (application != null) application.setLastAccess(appLastAccess)
                            fhm.remove(cfids[y])
                            scope.release(ThreadLocalPageContext.get())
                            getLog().log(Log.LEVEL_INFO, "scope-context", "remove memory based " + VariableInterpreter.scopeInt2String(type).toString() + " scope for [" + applicationName.toString() + "/" + cfid.toString() + "]")
                            count--
                        }
                    }
                }
                if (count == 0) contexts.remove(arrContexts[i])
            }
        }
    }

    private fun clearUnusedApplications(jspFactory: CFMLFactoryImpl?) {
        if (applicationContexts!!.size() === 0) return
        val now: Long = System.currentTimeMillis()
        val arrContexts: Array<Object?> = applicationContexts.keySet().toArray()
        val listener: ApplicationListener = jspFactory.getConfig().getApplicationListener()
        for (i in arrContexts.indices) {
            val application: Application? = applicationContexts!![arrContexts[i]]
            if (application.getLastAccess() + application.getTimeSpan() < now) {
                application.touch()
                try {
                    listener.onApplicationEnd(jspFactory, arrContexts[i] as String?)
                } catch (t: Throwable) {
                    ExceptionUtil.rethrowIfNecessary(t)
                    ExceptionHandler.log(jspFactory.getConfig(), Caster.toPageException(t))
                } finally {
                    applicationContexts.remove(arrContexts[i])
                    application.release(ThreadLocalPageContext.get())
                }
            }
        }
    }

    @Throws(PageException::class)
    fun clearApplication(pc: PageContext?) {
        if (applicationContexts!!.size() === 0) throw ApplicationException("there is no application context defined")
        val name: String = pc.getApplicationContext().getName()
        val jspFactory: CFMLFactoryImpl = pc.getCFMLFactory() as CFMLFactoryImpl
        val application: Application = applicationContexts!![name]
                ?: throw ApplicationException("there is no application context defined with name [$name]")
        val listener: ApplicationListener = PageContextUtil.getApplicationListener(pc)
        application.touch()
        try {
            listener.onApplicationEnd(jspFactory, name)
        } finally {
            applicationContexts.remove(name)
            application.release(pc)
        }
    }

    @Throws(PageException::class)
    fun invalidateUserScope(pc: PageContextImpl?, migrateSessionData: Boolean, migrateClientData: Boolean) {
        val appContext: ApplicationContext = pc.getApplicationContext()
        val isNew: RefBoolean = RefBooleanImpl()

        // get in memory scopes
        val clientContext: Map<String?, Scope?>? = getSubMap(cfClientContexts, appContext.getName())
        val oldClient: UserScope? = clientContext!![pc.getCFID()] as UserScope?
        val sessionContext: Map<String?, Scope?>? = getSubMap(cfSessionContexts, appContext.getName())
        val oldSession: UserScope? = sessionContext!![pc.getCFID()] as UserScope?

        // remove Scopes completly
        removeCFSessionScope(pc)
        removeClientScope(pc)
        pc.resetIdAndToken()
        pc.resetSession()
        pc.resetClient()
        if (oldSession != null) migrate(pc, oldSession, getCFSessionScope(pc, isNew), migrateSessionData)
        if (oldClient != null) migrate(pc, oldClient, getClientScope(pc), migrateClientData)
    }

    companion object {
        private const val MINUTE = 60 * 1000
        private const val CLIENT_MEMORY_TIMESPAN = (5 * MINUTE).toLong()
        private const val SESSION_MEMORY_TIMESPAN = (5 * MINUTE).toLong()
        private var cluster: Cluster? = null
        private var server: Server? = null
        fun debug(log: Log?, msg: String?) {
            if (log != null) log.log(Log.LEVEL_DEBUG, "scope-context", msg) else LogUtil.logGlobal(ThreadLocalPageContext.getConfig(), Log.LEVEL_DEBUG, "scope", msg)
        }

        fun info(log: Log?, msg: String?) {
            if (log != null) log.log(Log.LEVEL_INFO, "scope-context", msg) else LogUtil.log(ThreadLocalPageContext.get(), Log.LEVEL_INFO, "scope", "scope-context", msg)
        }

        fun error(log: Log?, msg: String?) {
            if (log != null) log.log(Log.LEVEL_ERROR, "scope-context", msg) else LogUtil.log(ThreadLocalPageContext.get(), Log.LEVEL_ERROR, "scope", "scope-context", msg)
        }

        fun error(log: Log?, t: Throwable?) {
            if (log != null) log.log(Log.LEVEL_ERROR, "scope-context", ExceptionUtil.getStacktrace(t, true)) else LogUtil.log(ThreadLocalPageContext.get(), "scope", "scope-context", t)
        }

        /**
         * return the server Scope for this context
         *
         * @param pc
         * @return server scope
         */
        fun getServerScope(pc: PageContext?, jsr223: Boolean): Server? {
            if (server == null) {
                server = ServerImpl(pc, jsr223)
            }
            return server
        }
        /*
	 * * Returns the current Cluster Scope, if there is no current Cluster Scope, this method returns
	 * null.
	 *
	 * @param pc
	 *
	 * @param create
	 *
	 * @return
	 *
	 * @throws SecurityException / public static Cluster getClusterScope() { return cluster; }
	 */
        /**
         * Returns the current Cluster Scope, if there is no current Cluster Scope and create is true,
         * returns a new Cluster Scope. If create is false and the request has no valid Cluster Scope, this
         * method returns null.
         *
         * @param config
         * @param create
         * @return
         * @throws PageException
         */
        @Throws(PageException::class)
        fun getClusterScope(config: Config?, create: Boolean): Cluster? {
            if (cluster == null && create) {
                cluster = (config as ConfigPro?).createClusterScope()
            }
            return cluster
        }

        fun clearClusterScope() {
            cluster = null
        }

        /**
         * @return returns a new CFIs
         */
        fun getNewCFId(): String? {
            return UUID.randomUUID().toString()
        }

        /**
         * @return returns a new CFToken
         */
        fun getNewCFToken(): String? {
            return "0"
        }

        private fun migrate(pc: PageContextImpl?, oldScope: UserScope?, newScope: UserScope?, migrate: Boolean) {
            if (oldScope == null || newScope == null) return
            if (!migrate) oldScope.clear()
            oldScope.resetEnv(pc)
            val it: Iterator<Entry<Key?, Object?>?> = oldScope.entryIterator()
            var e: Entry<Key?, Object?>?
            if (migrate) {
                while (it.hasNext()) {
                    e = it.next()
                    if (StorageScopeImpl.KEYS.contains(e.getKey())) continue
                    newScope.setEL(e.getKey(), e.getValue())
                }
                if (newScope is StorageScope) (newScope as StorageScope?).store(pc.getConfig())
            }
        }
    }

    init {
        this.factory = factory
    }
}