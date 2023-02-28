package lucee.runtime.config

import java.lang.ref.SoftReference

class ConfigWebHelper(cs: ConfigServerImpl?, cw: ConfigWebPro?) {
    private val cs: ConfigServerImpl?
    private val cw: ConfigWebPro?
    private val tagHandlerPool: TagHandlerPool?
    private var debuggerPool: DebuggerPool? = null
    private var contextLock: KeyLock<String?>? = KeyLockImpl<String?>()
    private var cacheHandlerCollections: CacheHandlerCollections? = null
    private val applicationMappings: Map<String?, SoftReference<Mapping?>?>? = ConcurrentHashMap<String?, SoftReference<Mapping?>?>()
    private var baseComponentPageCFML: CIPage? = null
    private var baseComponentPageLucee: CIPage? = null
    private val compiler: CFMLCompilerImpl? = CFMLCompilerImpl()
    private var wsHandler: WSHandler? = null
    private var gatewayEngine: GatewayEngineImpl? = null
    private var serverTagMappings: Map<String?, Mapping?>? = null
    private var serverFunctionMappings: Map<String?, Mapping?>? = null
    private var searchEngine: SearchEngine? = null
    private var amfEngine: AMFEngine? = null
    protected var id: IdentificationWeb? = null
    fun getPasswordSource(): Short {
        return if (cs!!.hasCustomDefaultPassword()) ConfigWebImpl.PASSWORD_ORIGIN_DEFAULT else ConfigWebImpl.PASSWORD_ORIGIN_SERVER
    }

    fun hasIndividualSecurityManager(cwp: ConfigWebPro?): Boolean {
        return cs!!.hasIndividualSecurityManager(cwp.getIdentification().getId())
    }

    fun reset() {
        tagHandlerPool.reset()
        contextLock = KeyLockImpl<String?>()
        baseComponentPageCFML = null
        baseComponentPageLucee = null
    }

    fun setIdentification(id: IdentificationWeb?) {
        this.id = id
    }

    fun getIdentification(): IdentificationWeb? {
        return id
    }

    fun setAMFEngine(engine: AMFEngine?) {
        amfEngine = engine
    }

    fun getAMFEngine(): AMFEngine? {
        return if (amfEngine == null) AMFEngineDummy.getInstance() else amfEngine
    }

    fun getLabel(): String? {
        val hash: String = cw!!.getHash()
        var label = hash
        val labels: Map<String?, String?> = cs!!.getLabels()
        if (labels != null) {
            val l = labels[hash]
            if (!StringUtil.isEmpty(l)) {
                label = l
            }
        }
        return label
    }

    fun getLockManager(): LockManager? {
        return lockManager
    }

    @Throws(PageException::class)
    fun getSearchEngine(pc: PageContext?): SearchEngine? {
        if (searchEngine == null) {
            try {
                val o: Object = ClassUtil.loadInstance(cw.getSearchEngineClassDefinition().getClazz())
                searchEngine = if (o is SearchEngine) o as SearchEngine else throw ApplicationException("class [" + o.getClass().getName().toString() + "] does not implement the interface SearchEngine")
                searchEngine.init(cw, ConfigWebUtil.getFile(cw.getConfigDir(), ConfigWebUtil.translateOldPath(cw.getSearchEngineDirectory()), "search", cw.getConfigDir(),
                        FileUtil.TYPE_DIR, cw))
            } catch (e: Exception) {
                throw Caster.toPageException(e)
            }
        }
        return searchEngine
    }

    fun getTagHandlerPool(): TagHandlerPool? {
        return tagHandlerPool
    }

    fun getServerTagMappings(): Collection<Mapping?>? {
        if (serverTagMappings == null) {
            val it: Iterator<Entry<String?, Mapping?>?> = cs!!.tagMappings.entrySet().iterator() // .cloneReadOnly(this);
            var e: Entry<String?, Mapping?>?
            serverTagMappings = ConcurrentHashMap<String?, Mapping?>()
            while (it.hasNext()) {
                e = it.next()
                serverTagMappings.put(e.getKey(), (e.getValue() as MappingImpl).cloneReadOnly(cw))
            }
        }
        return serverTagMappings!!.values()
    }

    fun getServerTagMapping(mappingName: String?): Mapping? {
        getServerTagMappings() // necessary to make sure it exists
        return serverTagMappings!![mappingName]
    }

    fun getServerFunctionMappings(): Collection<Mapping?>? {
        if (serverFunctionMappings == null) {
            val it: Iterator<Entry<String?, Mapping?>?> = cs!!.functionMappings.entrySet().iterator()
            var e: Entry<String?, Mapping?>?
            serverFunctionMappings = ConcurrentHashMap<String?, Mapping?>()
            while (it.hasNext()) {
                e = it.next()
                serverFunctionMappings.put(e.getKey(), (e.getValue() as MappingImpl).cloneReadOnly(cw))
            }
        }
        return serverFunctionMappings!!.values()
    }

    fun getServerFunctionMapping(mappingName: String?): Mapping? {
        getServerFunctionMappings() // call this to make sure it exists
        return serverFunctionMappings!![mappingName]
    }

    fun resetServerFunctionMappings() {
        serverFunctionMappings = null
    }

    fun getGatewayEngineImpl(): GatewayEngineImpl? {
        if (gatewayEngine == null) {
            gatewayEngine = GatewayEngineImpl(cw)
        }
        return gatewayEngine
    }

    @Throws(PageException::class)
    fun getWSHandler(): WSHandler? {
        if (wsHandler == null) {
            var cd: ClassDefinition? = if (cw is ConfigImpl) (cw as ConfigImpl?)!!.getWSHandlerClassDefinition() else null
            if (isEmpty(cd)) cd = cs!!.getWSHandlerClassDefinition()
            try {
                if (isEmpty(cd)) return DummyWSHandler()
                val obj: Object = ClassUtil.newInstance(cd.getClazz())
                if (obj is WSHandler) wsHandler = obj as WSHandler else wsHandler = WSHandlerReflector(obj)
            } catch (e: Exception) {
                throw Caster.toPageException(e)
            }
        }
        return wsHandler
    }

    fun getCFMLWriter(pc: PageContext?, req: HttpServletRequest?, rsp: HttpServletResponse?): CFMLWriter? {
        return if (cw!!.getCFMLWriterType() === ConfigPro.CFML_WRITER_WS) CFMLWriterWS(pc, req, rsp, -1, false, cw!!.closeConnection(), cw.isShowVersion(), cw!!.contentLength()) else if (cw!!.getCFMLWriterType() === ConfigPro.CFML_WRITER_REFULAR) CFMLWriterImpl(pc, req, rsp, -1, false, cw!!.closeConnection(), cw.isShowVersion(), cw!!.contentLength()) else CFMLWriterWSPref(pc, req, rsp, -1, false, cw!!.closeConnection(), cw.isShowVersion(), cw!!.contentLength())
    }

    fun getDebuggerPool(): DebuggerPool? {
        if (debuggerPool == null) {
            val dir: Resource = cw.getConfigDir().getRealResource("debugger")
            dir.mkdirs()
            debuggerPool = DebuggerPool(dir)
        }
        return debuggerPool
    }

    fun getContextLock(): KeyLock<String?>? {
        return contextLock
    }

    fun getCacheHandlerCollection(type: Int, defaultValue: CacheHandlerCollection?): CacheHandlerCollection? {
        if (cacheHandlerCollections == null) cacheHandlerCollections = CacheHandlerCollections(cw)
        when (type) {
            Config.CACHE_TYPE_FILE -> return cacheHandlerCollections.file
            Config.CACHE_TYPE_FUNCTION -> return cacheHandlerCollections.function
            Config.CACHE_TYPE_HTTP -> return cacheHandlerCollections.http
            Config.CACHE_TYPE_INCLUDE -> return cacheHandlerCollections.include
            Config.CACHE_TYPE_QUERY -> return cacheHandlerCollections.query
            Config.CACHE_TYPE_RESOURCE -> return cacheHandlerCollections.resource
            Config.CACHE_TYPE_WEBSERVICE -> return cacheHandlerCollections.webservice
        }
        return defaultValue
    }

    fun releaseCacheHandlers(pc: PageContext?) {
        if (cacheHandlerCollections == null) return
        cacheHandlerCollections.releaseCacheHandlers(pc)
    }

    @Throws(PageException::class)
    fun getBaseComponentPage(dialect: Int, pc: PageContext?): CIPage? {
        // CFML
        if (dialect == CFMLEngine.DIALECT_CFML) {
            if (baseComponentPageCFML == null) {
                baseComponentPageCFML = cw!!.getBaseComponentPageSource(dialect, pc).loadPage(pc, false) as CIPage
            }
            return baseComponentPageCFML
        }
        // Lucee
        if (baseComponentPageLucee == null) {
            baseComponentPageLucee = cw!!.getBaseComponentPageSource(dialect, pc).loadPage(pc, false) as CIPage
        }
        return baseComponentPageLucee
    }

    fun resetBaseComponentPage() {
        baseComponentPageCFML = null
        baseComponentPageLucee = null
    }

    fun getApplicationMappings(): Array<Mapping?>? {
        val list: List<Mapping?> = ArrayList()
        val it: Iterator<SoftReference<Mapping?>?> = applicationMappings!!.values().iterator()
        var sr: SoftReference<Mapping?>?
        while (it.hasNext()) {
            sr = it.next()
            if (sr != null) list.add(sr.get())
        }
        return list.toArray(arrayOfNulls<Mapping?>(list.size()))
    }

    fun getApplicationMapping(type: String?, virtual: String?, physical: String?, archive: String?, physicalFirst: Boolean, ignoreVirtual: Boolean,
                              checkPhysicalFromWebroot: Boolean, checkArchiveFromWebroot: Boolean): Mapping? {
        var key = (type.toString() + ":" + virtual.toLowerCase() + ":" + (if (physical == null) "" else physical.toLowerCase()) + ":" + (if (archive == null) "" else archive.toLowerCase()) + ":"
                + physicalFirst)
        key = toString(HashUtil.create64BitHash(key), Character.MAX_RADIX)
        val t: SoftReference<Mapping?>? = applicationMappings!![key]
        var m: Mapping? = if (t == null) null else t.get()
        if (m == null) {
            m = MappingImpl(cw, virtual, physical, archive, Config.INSPECT_UNDEFINED, physicalFirst, false, false, false, true, ignoreVirtual, null, -1, -1,
                    checkPhysicalFromWebroot, checkArchiveFromWebroot)
            applicationMappings.put(key, SoftReference<Mapping?>(m))
        } else m.check()
        return m
    }

    fun isApplicationMapping(mapping: Mapping?): Boolean {
        val it: Iterator<SoftReference<Mapping?>?> = applicationMappings!!.values().iterator()
        var sr: SoftReference<Mapping?>?
        while (it.hasNext()) {
            sr = it.next()
            if (sr != null && mapping.equals(sr.get())) return true
        }
        return false
    }

    fun getCompiler(): CFMLCompilerImpl? {
        return compiler
    }

    fun isEmpty(cd: ClassDefinition?): Boolean {
        return cd == null || StringUtil.isEmpty(cd.getClassName())
    }

    companion object {
        private val lockManager: LockManager? = LockManagerImpl.getInstance(false)
    }

    init {
        this.cs = cs
        this.cw = cw
        tagHandlerPool = TagHandlerPool(cw)
    }
}