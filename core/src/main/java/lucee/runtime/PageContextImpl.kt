/**
 * Copyright (c) 2014, the Railo Company Ltd.
 * Copyright (c) 2015, Lucee Assosication Switzerland
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
package lucee.runtime

import java.io.IOException

/**
 * page context for every page object. the PageContext is a jsp page context expanded by CFML
 * functionality. for example you have the method getSession to get jsp combatible session object
 * (HTTPSession) and with sessionScope() you get CFML combatible session object (Struct,Scope).
 */
class PageContextImpl(scopeContext: ScopeContext?, config: ConfigWebPro?, servlet: HttpServlet?, jsr223: Boolean) : PageContext() {
    /**
     * Field `pathList`
     */
    private val udfs: LinkedList<UDF?>? = LinkedList<UDF?>()
    private val pathList: LinkedList<PageSource?>? = LinkedList<PageSource?>()
    private val includePathList: LinkedList<PageSource?>? = LinkedList<PageSource?>()
    private val includeOnce: Set<PageSource?>? = HashSet<PageSource?>()

    /**
     * Field `executionTime`
     */
    protected var executionTime: Long = 0
    private var req: HTTPServletRequestWrap? = null
    private var rsp: HttpServletResponse? = null
    private var servlet: HttpServlet?
    private var writer: JspWriter? = null
    private var forceWriter: JspWriter? = null
    private val bodyContentStack: BodyContentStack?
    private val devNull: DevNullBodyContent?
    private val config: ConfigWebPro?

    // private DataSourceManager manager;
    // private CFMLCompilerImpl compiler;
    // Scopes
    private val scopeContext: ScopeContext?
    private var variablesRoot: Variables? = VariablesImpl() // ScopeSupport(false,"variables",Scope.SCOPE_VARIABLES);
    private var variables: Variables? = variablesRoot // new ScopeSupport("variables",Scope.SCOPE_VARIABLES);
    private var undefined: Undefined?
    private var _url: URLImpl? = URLImpl()
    private var _form: FormImpl? = FormImpl()
    private var urlForm: URLForm? = UrlFormImpl(_form, _url)
    private var url: URL? = null
    private var form: Form? = null
    private var request: RequestImpl? = RequestImpl()
    private val cgiR: CGIImplReadOnly? = CGIImplReadOnly()
    private val cgiRW: CGIImpl? = CGIImpl()
    private var argument: Argument? = ArgumentImpl()
    private var local: Local? = localUnsupportedScope
    private var session: Session? = null
    private var server: Server?
    private var cluster: Cluster? = null
    private val cookie: CookieImpl? = CookieImpl()
    private var client: Client? = null
    private var application: Application? = null
    private val debugger: DebuggerImpl? = DebuggerImpl()
    private var requestTimeout: Long = -1
    private var enablecfoutputonly: Short = 0
    private var outputState = 0
    private var cfid: String? = null
    private var cftoken: String? = null
    private val id: Int
    private var requestId = 0
    private var _psq: Boolean? = null
    private var locale: Locale? = null
    private var timeZone: TimeZone? = null

    // Pools
    private val errorPagePool: ErrorPagePool? = ErrorPagePool()
    private val tagHandlerPool: TagHandlerPool?
    private val ftpPool: FTPPoolImpl? = FTPPoolImpl()
    private var activeComponent: Component? = null
    private var activeUDF: UDF? = null
    private var activeUDFCalledName: Collection.Key? = null

    // private ComponentScope componentScope=new ComponentScope(this);
    private var remoteUser: Credential? = null
    protected var variableUtil: VariableUtilImpl? = VariableUtilImpl()
    private var exception: PageException? = null
    private var base: PageSource? = null
    private var applicationContext: ApplicationContextSupport? = null
    private val defaultApplicationContext: ApplicationContextSupport?
    private val scopeFactory: ScopeFactory? = ScopeFactory()
    private var parentTag: Tag? = null
    private var currentTag: Tag? = null
    private var thread: Thread? = null
    private var startTime: Long = 0
    private var startTimeNS: Long = 0
    private var endTimeNS: Long = 0
    private val manager: DatasourceManagerImpl?
    private var threads: CFThread? = null
    private var allThreads: Map<Key?, Threads?>? = null
    private var hasFamily = false
    private var parent: PageContextImpl? = null
    private var caller: PageSource? = null
    private var callerTemplate: PageSource? = null
    private var root: PageContextImpl? = null
    private var parentTags: List<String?>? = null
    private var children: Queue<PageContext?>? = null
    private var lazyStats: List<Statement?>? = null
    private var fdEnabled = false
    private var execLog: ExecutionLog? = null
    private var useSpecialMappings = false
    private var ormSession: ORMSession? = null
    private var isChild = false
    private var gatewayContext = false
    private var listenerContext = false
    private var serverPassword: Password? = null
    private var pe: PageException? = null

    // private Throwable requestTimeoutException;
    private var currentTemplateDialect: Int = CFMLEngine.DIALECT_CFML
    private var requestDialect: Int = CFMLEngine.DIALECT_CFML
    private var ignoreScopes = false
    private var appListenerType: Int = ApplicationListener.TYPE_NONE
    private var currentThread: ThreadsImpl? = null
    private var timeoutStacktrace: Array<StackTraceElement?>?
    private var fullNullSupport = false
    fun isInitialized(): Boolean {
        return rsp != null
    }

    /**
     * return if the PageContext is from a stopped thread, if so it should no longer be used!
     *
     * @return
     */
    @Override
    fun getRequestTimeoutException(): Throwable? {
        throw RuntimeException("method no longer supported")
    }

    fun getTimeoutStackTrace(): Array<StackTraceElement?>? {
        return timeoutStacktrace
    }

    fun setTimeoutStackTrace() {
        timeoutStacktrace = thread.getStackTrace()
    }

    @Override
    @Throws(IOException::class, IllegalStateException::class, IllegalArgumentException::class)
    fun initialize(servlet: Servlet?, req: ServletRequest?, rsp: ServletResponse?, errorPageURL: String?, needsSession: Boolean, bufferSize: Int, autoFlush: Boolean) {
        initialize(servlet as HttpServlet?, req as HttpServletRequest?, rsp as HttpServletResponse?, errorPageURL, needsSession, bufferSize, autoFlush, false, false, null)
    }

    /**
     * initialize an existing page context
     *
     * @param servlet
     * @param req
     * @param rsp
     * @param errorPageURL
     * @param needsSession
     * @param bufferSize
     * @param autoFlush
     * @param tmplPC
     */
    fun initialize(servlet: HttpServlet?, req: HttpServletRequest?, rsp: HttpServletResponse?, errorPageURL: String?, needsSession: Boolean, bufferSize: Int,
                   autoFlush: Boolean, isChild: Boolean, ignoreScopes: Boolean, tmplPC: PageContextImpl?): PageContextImpl? {
        parent = null
        caller = null
        callerTemplate = null
        root = null
        val clone = tmplPC != null
        requestId = counter++
        if (clone) {
            appListenerType = tmplPC!!.appListenerType
            this.ignoreScopes = tmplPC.ignoreScopes
        } else {
            appListenerType = ApplicationListener.TYPE_NONE
            this.ignoreScopes = ignoreScopes
        }
        ReqRspUtil.setContentType(rsp, "text/html; charset=" + config.getWebCharset().name())
        this.isChild = isChild
        applicationContext = defaultApplicationContext
        setFullNullSupport()
        startTime = System.currentTimeMillis()
        startTimeNS = System.nanoTime()
        endTimeNS = 0
        thread = Thread.currentThread()
        if (req is HTTPServletRequestWrap) this.req = req as HTTPServletRequestWrap? else this.req = HTTPServletRequestWrap(req)
        this.rsp = rsp
        this.servlet = servlet

        // Writers
        run {
            val tmp: PageContext? = if (clone) tmplPC else this
            if (config.debugLogOutput()) {
                val w: CFMLWriter = config.getCFMLWriter(tmp, req, rsp)
                w.setAllowCompression(false)
                val dcw = DebugCFMLWriter(w)
                bodyContentStack.init(dcw)
                debugger.setOutputLog(dcw)
            } else {
                bodyContentStack.init(config.getCFMLWriter(tmp, req, rsp))
            }
        }
        writer = bodyContentStack.getWriter()
        forceWriter = writer

        // Scopes
        server = ScopeContext.getServerScope(this, ignoreScopes)
        if (clone) {
            form = tmplPC!!.form
            url = tmplPC.url
            urlForm = tmplPC.urlForm
            _url = tmplPC._url
            _form = tmplPC._form
            variables = tmplPC.variables
            undefined = UndefinedImpl(this, tmplPC.undefined.getType() as Short)
            hasFamily = true
        } else {
            if (hasFamily) {
                variablesRoot = VariablesImpl()
                variables = variablesRoot
                request = RequestImpl()
                _url = URLImpl()
                _form = FormImpl()
                urlForm = UrlFormImpl(_form, _url)
                undefined = UndefinedImpl(this, getScopeCascadingType())
                hasFamily = false
            } else if (variables == null) {
                variablesRoot = VariablesImpl()
                variables = variablesRoot
            }
        }
        if (clone) {
            request = tmplPC!!.request
        } else {
            request.initialize(this)
            if (config.mergeFormAndURL()) {
                url = urlForm
                form = urlForm
            } else {
                url = _url
                form = _form
            }
        }

        // scopes
        if (clone) {
            _psq = tmplPC!!._psq
            gatewayContext = tmplPC.gatewayContext
            listenerContext = tmplPC.listenerContext
        } else {
            _psq = null
        }
        fdEnabled = !config.allowRequestTimeout()
        if (config.getExecutionLogEnabled()) execLog = config.getExecutionLogFactory().getInstance(this)
        if (debugger != null) debugger.init(config)
        undefined.initialize(this)
        timeoutStacktrace = null
        if (clone) {
            getCFID()
            cfid = tmplPC!!.cfid
            cftoken = tmplPC.cftoken
            requestTimeout = tmplPC.requestTimeout
            locale = tmplPC.locale
            timeZone = tmplPC.timeZone
            fdEnabled = tmplPC.fdEnabled
            useSpecialMappings = tmplPC.useSpecialMappings
            serverPassword = tmplPC.serverPassword
            requestDialect = tmplPC.requestDialect
            currentTemplateDialect = tmplPC.currentTemplateDialect
            tmplPC.hasFamily = true
            parent = tmplPC
            caller = tmplPC.getCurrentPageSource()
            callerTemplate = tmplPC.getCurrentTemplatePageSource()
            root = if (tmplPC.root == null) tmplPC else tmplPC.root
            tagName = tmplPC.tagName
            parentTags = if (tmplPC.parentTags == null) null else (tmplPC.parentTags as ArrayList?).clone()
            if (tmplPC.children == null) {
                synchronized(tmplPC) {
                    if (tmplPC.children == null) {
                        tmplPC.children = ConcurrentLinkedQueue<PageContext?>()
                    }
                }
            }
            tmplPC.children.add(this)
            applicationContext = tmplPC.applicationContext
            setFullNullSupport()

            // path
            base = tmplPC.base
            var it: Iterator<PageSource?> = tmplPC.includePathList.iterator()
            while (it.hasNext()) {
                includePathList.add(it.next())
            }
            it = pathList.iterator()
            while (it.hasNext()) {
                pathList.add(it.next())
            }
        }
        return this
    }

    @Override
    fun release() {
        config.releaseCacheHandlers(this)
        if (config.getExecutionLogEnabled() && execLog != null) {
            execLog.release()
            execLog = null
        }
        if (config.debug()) {
            var skipLogThread = isChild
            if (skipLogThread && config.hasDebugOptions(ConfigPro.DEBUG_THREAD)) skipLogThread = false
            if (!gatewayContext && !skipLogThread) config.getDebuggerPool().store(this, debugger)
            debugger.reset()
        } else debugger.resetTraces() // traces can alo be used when debugging is off
        serverPassword = null

        // boolean isChild=parent!=null; // isChild is defined in the class outside this method
        parent = null
        caller = null
        callerTemplate = null
        root = null
        // Attention have to be before close
        if (client != null) {
            client.touchAfterRequest(this)
            client = null
        }
        if (session != null) {
            session.touchAfterRequest(this)
            session = null
        }

        // ORM
        // if(ormSession!=null)releaseORM();

        // Scopes
        if (hasFamily) {
            if (hasFamily && !isChild) {
                req.disconnect(this)
            }
            close()
            base = null
            if (children != null) children.clear()
            request = null
            _url = null
            _form = null
            urlForm = null
            undefined = null
            variables = null
            variablesRoot = null
            // if(threads!=null && threads.size()>0) threads.clear();
            threads = null
            allThreads = null
            currentThread = null
        } else {
            close()
            base = null
            if (variables.isBind()) {
                variables = null
                variablesRoot = null
            } else {
                variables = variablesRoot
                variables.release(this)
            }
            undefined.release(this)
            urlForm.release(this)
            request.release(this)
        }
        cgiR.release(this)
        cgiRW.release(this)
        argument.release(this)
        local = localUnsupportedScope
        cookie.release(this)
        application = null // not needed at the moment -> application.releaseAfterRequest();
        applicationContext = null // do not release may used by child threads

        // Properties
        requestTimeout = -1
        outputState = 0
        cfid = null
        cftoken = null
        locale = null
        timeZone = null
        url = null
        form = null
        currentTemplateDialect = CFMLEngine.DIALECT_LUCEE
        requestDialect = CFMLEngine.DIALECT_LUCEE

        // Pools
        errorPagePool.clear()

        // lazy statements
        if (lazyStats != null && !lazyStats!!.isEmpty()) {
            val it: Iterator<Statement?> = lazyStats!!.iterator()
            while (it.hasNext()) {
                DBUtil.closeEL(it.next())
            }
            lazyStats.clear()
            lazyStats = null
        }
        if (!hasFamily) {
            pathList.clear()
            includePathList.clear()
        }
        executionTime = 0
        bodyContentStack.release()

        // activeComponent=null;
        remoteUser = null
        exception = null
        ftpPool.clear()
        parentTag = null
        currentTag = null

        // Req/Rsp
        if (req is HTTPServletRequestWrap) {
            req.close()
        }
        req = null
        rsp = null
        servlet = null

        // Writer
        writer = null
        forceWriter = null
        if (pagesUsed!!.size() > 0) pagesUsed.clear()
        activeComponent = null
        activeUDF = null
        gatewayContext = false
        listenerContext = false
        manager.release()
        includeOnce.clear()
        pe = null
        literalTimestampWithTSOffset = false
        thread = null
        tagName = null
        parentTags = null
        _psq = null
        dummy = false
        listenSettings = false
        if (ormSession != null) {
            try {
                releaseORM()
            } catch (e: Exception) {
            }
        }
    }

    @Throws(PageException::class)
    private fun releaseORM() {
        try {
            // flush orm session
            val engine: ORMEngine = ormSession.getEngine()
            val config: ORMConfiguration = engine.getConfiguration(this)
            if (config == null || config.flushAtRequestEnd() && config.autoManageSession()) {
                ormSession.flushAll(this)
            }
            ormSession.closeAll(this)
            manager.releaseORM()
        } finally {
            ormSession = null
        }
    }

    @Override
    @Throws(IOException::class)
    fun write(str: String?) {
        writer.write(str)
    }

    @Override
    @Throws(IOException::class)
    fun forceWrite(str: String?) {
        forceWriter.write(str)
    }

    @Override
    @Throws(IOException::class, PageException::class)
    fun writePSQ(o: Object?) {
        // is var usage allowed?
        if (applicationContext != null && applicationContext.getQueryVarUsage() !== ConfigPro.QUERY_VAR_USAGE_IGNORE) {
            // Warning
            if (applicationContext.getQueryVarUsage() === ConfigPro.QUERY_VAR_USAGE_WARN) {
                DebuggerImpl.deprecated(this, "query.variableUsage",
                        "Please do not use variables within the cfquery tag, instead use the tag \"cfqueryparam\" or the attribute \"params\"")
            } else if (applicationContext.getQueryVarUsage() === ConfigPro.QUERY_VAR_USAGE_ERROR) {
                throw ApplicationException("Variables are not allowed within cfquery, please use the tag <cfqueryparam> or the attribute \"params\" instead.")
            }
        }

        // preserve single quote
        if (o is Date || Decision.isDate(o, false)) {
            writer.write(Caster.toString(o))
        } else {
            writer.write(if (getPsq()) Caster.toString(o) else StringUtil.replace(Caster.toString(o), "'", "''", false))
        }
    }

    // FUTURE add both method to interface
    @Throws(IOException::class, PageException::class)
    fun writeEncodeFor(value: String?, encodeType: String?) { // FUTURE keyword:encodefore add to interface
        write(ESAPIUtil.esapiEncode(this, encodeType, value))
    }

    /*
	 * public void writeEncodeFor(String value, short encodeType) throws IOException, PageException { //
	 * FUTURE keyword:encodefore add to interface write(ESAPIUtil.esapiEncode(this,value, encodeType));
	 * }
	 */
    @Override
    fun flush() {
        try {
            getOut().flush()
        } catch (e: IOException) {
        }
    }

    @Override
    fun close() {
        IOUtil.closeEL(getOut())
    }

    fun getRelativePageSource(realPath: String?): PageSource? {
        LogUtil.log(this, Log.LEVEL_INFO, PageContextImpl::class.java.getName(), "method getRelativePageSource is deprecated")
        if (StringUtil.startsWith(realPath, '/')) return PageSourceImpl.best(getPageSources(realPath))
        return if (pathList.size() === 0) null else pathList.getLast().getRealPage(realPath)
    }

    fun getRelativePageSourceExisting(realPath: String?): PageSource? {
        if (StringUtil.startsWith(realPath, '/')) return getPageSourceExisting(realPath)
        if (pathList.size() === 0) return null
        val ps: PageSource = pathList.getLast().getRealPage(realPath)
        return if (PageSourceImpl.pageExist(ps)) ps else null
    }

    /**
     *
     * @param realPath
     * @param previous relative not to the caller, relative to the callers caller
     * @return
     */
    fun getRelativePageSourceExisting(realPath: String?, previous: Boolean): PageSource? {
        if (StringUtil.startsWith(realPath, '/')) return getPageSourceExisting(realPath)
        if (pathList.size() === 0) return null
        var ps: PageSource? = null
        var tmp: PageSource? = null
        if (previous) {
            var valid = false
            ps = pathList.getLast()
            for (i in pathList.size() - 2 downTo 0) {
                tmp = pathList.get(i)
                if (tmp !== ps) {
                    ps = tmp
                    valid = true
                    break
                }
            }
            if (!valid) return null
        } else ps = pathList.getLast()
        ps = ps.getRealPage(realPath)
        return if (PageSourceImpl.pageExist(ps)) ps else null
    }

    fun getRelativePageSources(realPath: String?): Array<PageSource?>? {
        if (StringUtil.startsWith(realPath, '/')) return getPageSources(realPath)
        val ps: PageSource = getCurrentPageSource(null) ?: return null
        return arrayOf<PageSource?>(ps.getRealPage(realPath))
    }

    fun getPageSource(realPath: String?): PageSource? {
        return PageSourceImpl.best(config.getPageSources(this, applicationContext.getMappings(), realPath, false, useSpecialMappings, true))
    }

    fun getPageSources(realPath: String?): Array<PageSource?>? { // to not change, this is used in the flex extension
        return config.getPageSources(this, applicationContext.getMappings(), realPath, false, useSpecialMappings, true)
    }

    fun getPageSourceExisting(realPath: String?): PageSource? { // do not change, this method is used in flex extension
        return config.getPageSourceExisting(this, applicationContext.getMappings(), realPath, false, useSpecialMappings, true, false)
    }

    fun useSpecialMappings(useTagMappings: Boolean): Boolean {
        val b = useSpecialMappings
        useSpecialMappings = useTagMappings
        return b
    }

    fun useSpecialMappings(): Boolean {
        return useSpecialMappings
    }

    fun getPhysical(realPath: String?, alsoDefaultMapping: Boolean): Resource? {
        return config.getPhysical(applicationContext.getMappings(), realPath, alsoDefaultMapping)
    }

    @Override
    fun toPageSource(res: Resource?, defaultValue: PageSource?): PageSource? {
        return config.toPageSource(applicationContext.getMappings(), res, defaultValue)
    }

    @Override
    @Throws(PageException::class)
    fun doInclude(realPath: String?) {
        _doInclude(getRelativePageSources(realPath), false, getCachedWithin(ConfigWeb.CACHEDWITHIN_INCLUDE))
    }

    @Override
    @Throws(PageException::class)
    fun doInclude(realPath: String?, runOnce: Boolean) {
        _doInclude(getRelativePageSources(realPath), runOnce, getCachedWithin(ConfigWeb.CACHEDWITHIN_INCLUDE))
    }

    // used by the transformer
    @Throws(PageException::class)
    fun doInclude(realPath: String?, runOnce: Boolean, cachedWithin: Object?) {
        var cachedWithin: Object? = cachedWithin
        if (cachedWithin == null) cachedWithin = getCachedWithin(ConfigWeb.CACHEDWITHIN_INCLUDE)
        _doInclude(getRelativePageSources(realPath), runOnce, cachedWithin)
    }

    @Override
    @Throws(PageException::class)
    fun doInclude(sources: Array<PageSource?>?, runOnce: Boolean) {
        _doInclude(sources, runOnce, getCachedWithin(ConfigWeb.CACHEDWITHIN_INCLUDE))
    }

    // IMPORTANT!!! we do not getCachedWithin in this method, because Modern|ClassicAppListener is
    // calling this method and in this case it should not be used
    @Throws(PageException::class)
    fun _doInclude(sources: Array<PageSource?>?, runOnce: Boolean, cachedWithin: Object?) {
        PageContextUtil.checkRequestTimeout(this)
        if (cachedWithin == null) {
            _doInclude(sources, runOnce)
            return
        }

        // ignore call when runonce an it is not first call
        if (runOnce) {
            val currentPage: Page = PageSourceImpl.loadPage(this, sources)
            if (runOnce && includeOnce!!.contains(currentPage.getPageSource())) return
        }

        // get cached data
        val cacheId: String = CacheHandlerCollectionImpl.createId(sources)
        val cacheHandler: CacheHandler = config.getCacheHandlerCollection(Config.CACHE_TYPE_INCLUDE, null).getInstanceMatchingObject(cachedWithin, null)
        if (cacheHandler is CacheHandlerPro) {
            val cacheItem: CacheItem = (cacheHandler as CacheHandlerPro).get(this, cacheId, cachedWithin)
            if (cacheItem is IncludeCacheItem) {
                try {
                    write((cacheItem as IncludeCacheItem).getOutput())
                    return
                } catch (e: IOException) {
                    throw Caster.toPageException(e)
                }
            }
        } else if (cacheHandler != null) { // TODO this else block can be removed when all cache handlers implement CacheHandlerPro
            val cacheItem: CacheItem = cacheHandler.get(this, cacheId)
            if (cacheItem is IncludeCacheItem) {
                try {
                    write((cacheItem as IncludeCacheItem).getOutput())
                    return
                } catch (e: IOException) {
                    throw Caster.toPageException(e)
                }
            }
        }

        // cached item not found, process and cache result if needed
        val start: Long = System.nanoTime()
        val bc: BodyContent? = pushBody()
        try {
            _doInclude(sources, runOnce)
            val out: String = bc.getString()
            if (cacheHandler != null) {
                val cacheItem: CacheItem = IncludeCacheItem(out, if (ArrayUtil.isEmpty(sources)) null else sources!![0], System.nanoTime() - start)
                cacheHandler.set(this, cacheId, cachedWithin, cacheItem)
                return
            }
        } finally {
            BodyContentUtil.flushAndPop(this, bc)
        }
    }

    @Throws(PageException::class)
    private fun _doInclude(sources: Array<PageSource?>?, runOnce: Boolean) {
        // debug
        if (!gatewayContext && config.debug() && config.hasDebugOptions(ConfigPro.DEBUG_TEMPLATE)) {
            val currTime = executionTime
            var exeTime: Long = 0
            val time: Long = System.nanoTime()
            val currentPage: Page = PageSourceImpl.loadPage(this, sources)
            notSupported(config, currentPage.getPageSource())
            if (runOnce && includeOnce!!.contains(currentPage.getPageSource())) return
            val debugEntry: DebugEntryTemplate = debugger.getEntry(this, currentPage.getPageSource())
            try {
                addPageSource(currentPage.getPageSource(), true)
                debugEntry.updateFileLoadTime(System.nanoTime() - time)
                exeTime = System.nanoTime()
                currentPage.call(this)
            } catch (t: Throwable) {
                ExceptionUtil.rethrowIfNecessary(t)
                val pe: PageException = Caster.toPageException(t)
                if (Abort.isAbort(pe)) {
                    if (Abort.isAbort(pe, Abort.SCOPE_REQUEST)) throw pe
                } else {
                    if (fdEnabled) {
                        FDSignal.signal(pe, false)
                    }
                    pe.addContext(currentPage.getPageSource(), -187, -187, null) // TODO was soll das 187
                    throw pe
                }
            } finally {
                includeOnce.add(currentPage.getPageSource())
                val diff: Long = System.nanoTime() - exeTime - (executionTime - currTime)
                executionTime += System.nanoTime() - time
                debugEntry.updateExeTime(diff)
                removeLastPageSource(true)
            }
        } else {
            val currentPage: Page = PageSourceImpl.loadPage(this, sources)
            notSupported(config, currentPage.getPageSource())
            if (runOnce && includeOnce!!.contains(currentPage.getPageSource())) return
            try {
                addPageSource(currentPage.getPageSource(), true)
                currentPage.call(this)
            } catch (t: Throwable) {
                ExceptionUtil.rethrowIfNecessary(t)
                val pe: PageException = Caster.toPageException(t)
                if (Abort.isAbort(pe)) {
                    if (Abort.isAbort(pe, Abort.SCOPE_REQUEST)) throw pe
                } else {
                    pe.addContext(currentPage.getPageSource(), -187, -187, null)
                    throw pe
                }
            } finally {
                includeOnce.add(currentPage.getPageSource())
                removeLastPageSource(true)
            }
        }
    }

    @Override
    @Throws(PageException::class)
    fun getTemplatePath(): Array? {
        val len: Int = includePathList.size()
        val sva = SVArray()
        var ps: PageSource
        var bps: PageSource?
        for (i in 0 until len) {
            ps = includePathList.get(i)
            if (i == 0) {
                bps = getBasePageSource()
                if (bps != null && !ps.equals(bps)) sva.append(bps.getResourceTranslated(this).getAbsolutePath())
            }
            sva.append(ps.getResourceTranslated(this).getAbsolutePath())
        }
        return sva
    }

    /**
     * if index is less than 1 it start from the rights
     */
    fun getPageSource(index: Int): PageSource? {
        var index = index
        if (index <= 0) index = includePathList.size() - index
        return includePathList.get(index - 1)
    }

    @Override
    fun getCurrentLevel(): Int {
        return includePathList.size() + 1
    }

    @Override
    fun getCurrentPageSource(): PageSource? {
        if (pathList.isEmpty()) {
            if (parent != null && parent != this && parent!!.isInitialized()) { // second comparision should not be necesary, just in case ...
                return parent!!.getCurrentPageSource()
            } else if (caller != null) return caller
            return null
        }
        return pathList.getLast()
    }

    @Override
    fun getCurrentPageSource(defaultvalue: PageSource?): PageSource? {
        if (pathList.isEmpty()) {
            if (parent != null && parent != this && parent!!.isInitialized()) { // second comparision should not be necesary, just in case ...
                return parent!!.getCurrentPageSource(defaultvalue)
            } else if (caller != null) return caller
            return defaultvalue
        }
        return pathList.getLast()
    }

    /**
     * @return the current template PageSource
     */
    @Override
    fun getCurrentTemplatePageSource(): PageSource? {
        if (includePathList.isEmpty()) {
            if (parent != null && parent != this && parent!!.isInitialized()) { // second comparision should not be necesary, just in case ...
                return parent!!.getCurrentTemplatePageSource()
            } else if (callerTemplate != null) return callerTemplate
            return null
        }
        return includePathList.getLast()
    }

    /**
     * @return base template file
     */
    @Override
    fun getBasePageSource(): PageSource? {
        return base
    }

    @Override
    fun getRootTemplateDirectory(): Resource? {
        return config.getResource(ReqRspUtil.getRootPath(servlet.getServletContext()))
    }

    @Override
    @Throws(PageException::class)
    fun scope(type: Int): Scope? {
        when (type) {
            Scope.SCOPE_UNDEFINED -> return undefinedScope()
            Scope.SCOPE_URL -> return urlScope()
            Scope.SCOPE_FORM -> return formScope()
            Scope.SCOPE_VARIABLES -> return variablesScope()
            Scope.SCOPE_REQUEST -> return requestScope()
            Scope.SCOPE_CGI -> return cgiScope()
            Scope.SCOPE_APPLICATION -> return applicationScope()
            Scope.SCOPE_ARGUMENTS -> return argumentsScope()
            Scope.SCOPE_SESSION -> return sessionScope()
            Scope.SCOPE_SERVER -> return serverScope()
            Scope.SCOPE_COOKIE -> return cookieScope()
            Scope.SCOPE_CLIENT -> return clientScope()
            Scope.SCOPE_LOCAL, ScopeSupport.SCOPE_VAR -> return localScope()
            Scope.SCOPE_CLUSTER -> return clusterScope()
        }
        return variables
    }

    @Throws(PageException::class)
    fun scope(strScope: String?, defaultValue: Scope?): Scope? {
        var strScope = strScope ?: return defaultValue
        if (ignoreScopes()) {
            if ("arguments".equals(strScope)) return argumentsScope()
            if ("local".equals(strScope)) return localScope()
            if ("request".equals(strScope)) return requestScope()
            if ("variables".equals(strScope)) return variablesScope()
            return if ("server".equals(strScope)) serverScope() else defaultValue
        }
        strScope = strScope.toLowerCase().trim()
        if ("variables".equals(strScope)) return variablesScope()
        if ("url".equals(strScope)) return urlScope()
        if ("form".equals(strScope)) return formScope()
        if ("request".equals(strScope)) return requestScope()
        if ("cgi".equals(strScope)) return cgiScope()
        if ("application".equals(strScope)) return applicationScope()
        if ("arguments".equals(strScope)) return argumentsScope()
        if ("session".equals(strScope)) return sessionScope()
        if ("server".equals(strScope)) return serverScope()
        if ("cookie".equals(strScope)) return cookieScope()
        if ("client".equals(strScope)) return clientScope()
        if ("local".equals(strScope)) return localScope()
        return if ("cluster".equals(strScope)) clusterScope() else defaultValue
    }

    @Override
    fun undefinedScope(): Undefined? {
        if (!undefined.isInitalized()) undefined.initialize(this)
        return undefined
    }

    /**
     * @return undefined scope, undefined scope is a placeholder for the scopecascading
     */
    @Override
    fun us(): Undefined? {
        if (!undefined.isInitalized()) undefined.initialize(this)
        return undefined
    }

    fun usl(): Scope? {
        if (!undefined.isInitalized()) undefined.initialize(this)
        return if (undefined.getCheckArguments()) undefined.localScope() else undefined
    }

    @Override
    fun variablesScope(): Variables? {
        return variables
    }

    @Override
    fun urlScope(): URL? {
        if (!url.isInitalized()) url.initialize(this)
        return url
    }

    @Override
    fun formScope(): Form? {
        if (!form.isInitalized()) form.initialize(this)
        return form
    }

    @Override
    fun urlFormScope(): URLForm? {
        if (!urlForm.isInitalized()) urlForm.initialize(this)
        return urlForm
    }

    @Override
    fun requestScope(): Request? {
        return request
    }

    @Override
    fun cgiScope(): CGI? {
        val cgi: CGI = if (applicationContext == null || applicationContext.getCGIScopeReadonly()) cgiR else cgiRW
        if (!cgi.isInitalized()) cgi.initialize(this)
        return cgi
    }

    @Override
    @Throws(PageException::class)
    fun applicationScope(): Application? {
        if (application == null) {
            if (!applicationContext.hasName()) throw ExpressionException("there is no application context defined for this application", hintAplication("you can define an application context"))
            application = scopeContext.getApplicationScope(this, true, DUMMY_BOOL)
        }
        return application
    }

    private fun hintAplication(prefix: String?): String? {
        val isCFML = getRequestDialect() == CFMLEngine.DIALECT_CFML
        return (prefix.toString() + " with the tag " + (if (isCFML) lucee.runtime.config.Constants.CFML_APPLICATION_TAG_NAME else lucee.runtime.config.Constants.LUCEE_APPLICATION_TAG_NAME)
                + " or with the " + if (isCFML) lucee.runtime.config.Constants.CFML_APPLICATION_EVENT_HANDLER else lucee.runtime.config.Constants.LUCEE_APPLICATION_EVENT_HANDLER)
    }

    @Override
    fun argumentsScope(): Argument? {
        return argument
    }

    @Override
    fun argumentsScope(bind: Boolean): Argument? {
        // Argument a=argumentsScope();
        if (bind) argument.setBind(true)
        return argument
    }

    @Override
    fun localScope(): Local? {
        // if(local==localUnsupportedScope)
        // throw new PageRuntimeException(new ExpressionException("Unsupported Context for Local Scope"));
        return local
    }

    @Override
    fun localScope(bind: Boolean): Local? {
        if (bind) local.setBind(true)
        // if(local==localUnsupportedScope)
        // throw new PageRuntimeException(new ExpressionException("Unsupported Context for Local Scope"));
        return local
    }

    @Override
    @Throws(PageException::class)
    fun localGet(): Object? {
        return localGet(false)
    }

    fun localGet(bind: Boolean, defaultValue: Object?): Object? {
        return if (undefined.getCheckArguments()) {
            localScope(bind)
        } else undefinedScope().get(KeyConstants._local, defaultValue)
    }

    @Override
    @Throws(PageException::class)
    fun localGet(bind: Boolean): Object? {
        // inside a local supported block
        return if (undefined.getCheckArguments()) {
            localScope(bind)
        } else undefinedScope().get(KeyConstants._local)
    }

    @Override
    @Throws(PageException::class)
    fun localTouch(): Object? {
        return localTouch(false)
    }

    @Override
    @Throws(PageException::class)
    fun localTouch(bind: Boolean): Object? {
        // inside a local supported block
        return if (undefined.getCheckArguments()) {
            localScope(bind)
        } else touch(undefinedScope(), KeyConstants._local)
        // return undefinedScope().get(LOCAL);
    }

    @Throws(PageException::class)
    fun thisGet(): Object? {
        return thisTouch()
    }

    @Throws(PageException::class)
    fun thisTouch(): Object? {
        // inside a component
        return if (undefined.variablesScope() is ComponentScope) {
            (undefined.variablesScope() as ComponentScope).getComponent()
        } else undefinedScope().get(KeyConstants._THIS)
    }

    fun thisGet(defaultValue: Object?): Object? {
        return thisTouch(defaultValue)
    }

    fun thisTouch(defaultValue: Object?): Object? {
        // inside a component
        return if (undefined.variablesScope() is ComponentScope) {
            (undefined.variablesScope() as ComponentScope).getComponent()
        } else undefinedScope().get(KeyConstants._THIS, defaultValue)
    }

    @Throws(PageException::class)
    fun staticGet(): Object? {
        return staticTouch()
    }

    @Throws(PageException::class)
    fun staticTouch(): Object? {
        // inside a component
        return if (undefined.variablesScope() is ComponentScope) {
            getStatic(undefined)
        } else undefinedScope().get(KeyConstants._STATIC)
    }

    fun staticGet(defaultValue: Object?): Object? {
        return staticTouch(defaultValue)
    }

    fun staticTouch(defaultValue: Object?): Object? {
        // inside a component
        return if (undefined.variablesScope() is ComponentScope) {
            getStatic(undefined)
        } else undefinedScope().get(KeyConstants._STATIC, defaultValue)
    }

    private fun getStatic(undefined: Undefined?): Scope? {
        return (undefined.variablesScope() as ComponentScope).getComponent().staticScope()
    }

    /**
     * @param local sets the current local scope
     * @param argument sets the current argument scope
     */
    @Override
    fun setFunctionScopes(local: Local?, argument: Argument?) {
        this.argument = argument
        this.local = local
        undefined.setFunctionScopes(local, argument)
    }

    @Override
    @Throws(PageException::class)
    fun sessionScope(): Session? {
        return sessionScope(true)
    }

    @Throws(PageException::class)
    fun sessionScope(checkExpires: Boolean): Session? {
        if (session == null) {
            checkSessionContext()
            session = scopeContext.getSessionScope(this, DUMMY_BOOL)
        }
        return session
    }

    @Throws(PageException::class)
    fun hasCFSession(): Boolean {
        if (session != null) return true
        return if (!applicationContext.hasName() || !applicationContext.isSetSessionManagement()) false else scopeContext.hasExistingSessionScope(this)
    }

    @Throws(PageException::class)
    fun invalidateUserScopes(migrateSessionData: Boolean, migrateClientData: Boolean) {
        checkSessionContext()
        scopeContext.invalidateUserScope(this, migrateSessionData, migrateClientData)
    }

    @Throws(ExpressionException::class)
    private fun checkSessionContext() {
        if (!applicationContext.hasName()) throw ExpressionException("there is no session context defined for this application", hintAplication("you can define a session context"))
        if (!applicationContext.isSetSessionManagement()) throw ExpressionException("session scope is not enabled", hintAplication("you can enable session scope"))
    }

    @Override
    fun serverScope(): Server? {
        // if(!server.isInitalized()) server.initialize(this);
        return server
    }

    fun reset() {
        server = ScopeContext.getServerScope(this, ignoreScopes())
    }

    @Override
    @Throws(PageException::class)
    fun clusterScope(): Cluster? {
        return clusterScope(true)
    }

    @Override
    @Throws(PageException::class)
    fun clusterScope(create: Boolean): Cluster? {
        if (cluster == null && create) {
            cluster = ScopeContext.getClusterScope(config, create)
            // cluster.initialize(this);
        }
        // else if(!cluster.isInitalized()) cluster.initialize(this);
        return cluster
    }

    @Override
    fun cookieScope(): Cookie? {
        if (!cookie.isInitalized()) cookie.initialize(this)
        return cookie
    }

    @Override
    @Throws(PageException::class)
    fun clientScope(): Client? {
        if (client == null) {
            if (!applicationContext.hasName()) throw ExpressionException("there is no client context defined for this application", hintAplication("you can define a client context"))
            if (!applicationContext.isSetClientManagement()) throw ExpressionException("client scope is not enabled", hintAplication("you can enable client scope"))
            client = scopeContext.getClientScope(this)
        }
        return client
    }

    @Override
    fun clientScopeEL(): Client? {
        if (client == null) {
            if (applicationContext == null || !applicationContext.hasName()) return null
            if (!applicationContext.isSetClientManagement()) return null
            client = scopeContext.getClientScopeEL(this)
        }
        return client
    }

    operator fun set(coll: Object?, key: String?, value: Object?): Object? {
        throw NoLongerSupported()
        // return variableUtil.set(this,coll,key,value);
    }

    @Override
    @Throws(PageException::class)
    operator fun set(coll: Object?, key: Collection.Key?, value: Object?): Object? {
        return variableUtil.set(this, coll, key, value)
    }

    /*
	 * public Object touch(Object coll, String key) throws PageException { Object
	 * o=getCollection(coll,key,null); if(o!=null) return o; return set(coll,key,new StructImpl()); }
	 */
    @Override
    @Throws(PageException::class)
    fun touch(coll: Object?, key: Collection.Key?): Object? {
        val o: Object = getCollection(coll, key, null)
        return if (o != null) o else set(coll, key, StructImpl())
    }

    /*
	 * private Object _touch(Scope scope, String key) throws PageException { Object
	 * o=scope.get(key,null); if(o!=null) return o; return scope.set(key, new StructImpl()); }
	 */
    @Override
    @Throws(PageException::class)
    fun getCollection(coll: Object?, key: String?): Object? {
        return variableUtil.getCollection(this, coll, key)
    }

    @Override
    @Throws(PageException::class)
    fun getCollection(coll: Object?, key: Collection.Key?): Object? {
        return variableUtil.getCollection(this, coll, key)
    }

    @Override
    fun getCollection(coll: Object?, key: String?, defaultValue: Object?): Object? {
        return variableUtil.getCollection(this, coll, key, defaultValue)
    }

    @Override
    fun getCollection(coll: Object?, key: Collection.Key?, defaultValue: Object?): Object? {
        return variableUtil.getCollection(this, coll, key, defaultValue)
    }

    @Override
    @Throws(PageException::class)
    operator fun get(coll: Object?, key: String?): Object? {
        return variableUtil.get(this, coll, key)
    }

    @Override
    @Throws(PageException::class)
    operator fun get(coll: Object?, key: Collection.Key?): Object? {
        return variableUtil.get(this, coll, key)
    }

    @Override
    @Throws(PageException::class)
    fun getReference(coll: Object?, key: String?): Reference? {
        return VariableReference(coll, key)
    }

    @Override
    @Throws(PageException::class)
    fun getReference(coll: Object?, key: Collection.Key?): Reference? {
        return VariableReference(coll, key)
    }

    @Override
    operator fun get(coll: Object?, key: String?, defaultValue: Object?): Object? {
        return variableUtil.get(this, coll, key, defaultValue)
    }

    @Override
    operator fun get(coll: Object?, key: Collection.Key?, defaultValue: Object?): Object? {
        return variableUtil.get(this, coll, key, defaultValue)
    }

    @Override
    @Throws(PageException::class)
    fun setVariable(`var`: String?, value: Object?): Object? {
        // return new CFMLExprInterpreter().interpretReference(this,new ParserString(var)).set(value);
        return VariableInterpreter.setVariable(this, `var`, value)
    }

    @Override
    @Throws(PageException::class)
    fun getVariable(`var`: String?): Object? {
        return VariableInterpreter.getVariable(this, `var`)
    }

    @Override
    @Throws(PageException::class)
    fun param(type: String?, name: String?, defaultValue: Object?, regex: String?) {
        _param(type, name, defaultValue, Double.NaN, Double.NaN, regex, -1)
    }

    @Override
    @Throws(PageException::class)
    fun param(type: String?, name: String?, defaultValue: Object?, min: Double, max: Double) {
        _param(type, name, defaultValue, min, max, null, -1)
    }

    @Override
    @Throws(PageException::class)
    fun param(type: String?, name: String?, defaultValue: Object?, maxLength: Int) {
        _param(type, name, defaultValue, Double.NaN, Double.NaN, null, maxLength)
    }

    @Override
    @Throws(PageException::class)
    fun param(type: String?, name: String?, defaultValue: Object?) {
        _param(type, name, defaultValue, Double.NaN, Double.NaN, null, -1)
    }

    // used by generated code FUTURE add to interface
    @Throws(PageException::class)
    fun subparam(type: String?, name: String?, value: Object?, min: Double, max: Double, strPattern: String?, maxLength: Int, isNew: Boolean) {

        // check attributes type
        var type = type
        type = type?.trim()?.toLowerCase() ?: "any"

        // cast and set value
        if (!"any".equals(type)) {
            // range
            if ("range".equals(type)) {
                val hasMin: Boolean = Decision.isValid(min)
                val hasMax: Boolean = Decision.isValid(max)
                val number: Double = Caster.toDoubleValue(value)
                if (!hasMin && !hasMax) throw ExpressionException("you need to define one of the following attributes [min,max], when type is set to [range]")
                if (hasMin && number < min) throw ExpressionException("The number [" + Caster.toString(number).toString() + "] is to small, the number must be at least [" + Caster.toString(min).toString() + "]")
                if (hasMax && number > max) throw ExpressionException("The number [" + Caster.toString(number).toString() + "] is to big, the number cannot be bigger than [" + Caster.toString(max).toString() + "]")
                setVariable(name, Caster.toDouble(number))
            } else if ("regex".equals(type) || "regular_expression".equals(type)) {
                val str: String = Caster.toString(value)
                if (strPattern == null) throw ExpressionException("Missing attribute [pattern]")
                if (!getRegex().matches(strPattern, str)) throw ExpressionException("The value [$str] doesn't match the provided pattern [$strPattern]")
                setVariable(name, str)
            } else if (type.equals("int") || type.equals("integer")) {
                if (!Decision.isInteger(value)) throw ExpressionException("The value [$value] is not a valid integer")
                setVariable(name, value)
            } else {
                if (!Decision.isCastableTo(type, value, true, true, maxLength)) {
                    if (maxLength > -1 && ("email".equalsIgnoreCase(type) || "url".equalsIgnoreCase(type) || "string".equalsIgnoreCase(type))) {
                        val msg = StringBuilder(CasterException.createMessage(value, type))
                        msg.append(" with a maximum length of $maxLength characters")
                        throw CasterException(msg.toString())
                    }
                    throw CasterException(value, type)
                }
                setVariable(name, value)
                // REALCAST setVariable(name,Caster.castTo(this,type,value,true));
            }
        } else if (isNew) setVariable(name, value)
    }

    @Throws(PageException::class)
    private fun _param(type: String?, name: String?, defaultValue: Object?, min: Double, max: Double, strPattern: String?, maxLength: Int) {

        // check attributes name
        if (StringUtil.isEmpty(name)) throw ExpressionException("The attribute [name] is required")
        var value: Object? = null
        var isNew = false
        val _null: Object = NullSupportHelper.NULL(this)
        // get value
        value = VariableInterpreter.getVariableEL(this, name, _null)
        if (_null === value) {
            if (defaultValue == null) throw ExpressionException("The required parameter [$name] was not provided.")
            value = defaultValue
            isNew = true
        }
        subparam(type, name, value, min, max, strPattern, maxLength, isNew)
    }

    /*
	 * private void paramX(String type, String name, Object defaultValue, double min,double max, String
	 * strPattern, int maxLength) throws PageException {
	 * 
	 * // check attributes type if(type==null)type="any"; else type=type.trim().toLowerCase();
	 * 
	 * // check attributes name if(StringUtil.isEmpty(name)) throw new
	 * ExpressionException("The attribute name is required");
	 * 
	 * Object value=null; boolean isNew=false;
	 * 
	 * // get value value=VariableInterpreter.getVariableEL(this,name,NullSupportHelper.NULL(this));
	 * if(NullSupportHelper.NULL(this)==value) { if(defaultValue==null) throw new
	 * ExpressionException("The required parameter ["+name+"] was not provided."); value=defaultValue;
	 * isNew=true; }
	 * 
	 * // cast and set value if(!"any".equals(type)) { // range if("range".equals(type)) { boolean
	 * hasMin=Decision.isValid(min); boolean hasMax=Decision.isValid(max); double number =
	 * Caster.toDoubleValue(value);
	 * 
	 * if(!hasMin && !hasMax) throw new
	 * ExpressionException("you need to define one of the following attributes [min,max], when type is set to [range]"
	 * );
	 * 
	 * if(hasMin && number<min) throw new ExpressionException("The number ["+Caster.toString(number)
	 * +"] is to small, the number must be at least ["+Caster.toString(min)+"]");
	 * 
	 * if(hasMax && number>max) throw new ExpressionException("The number ["+Caster.toString(number)
	 * +"] is to big, the number cannot be bigger than ["+Caster.toString(max)+"]");
	 * 
	 * setVariable(name,Caster.toDouble(number)); } // regex else if("regex".equals(type) ||
	 * "regular_expression".equals(type)) { String str=Caster.toString(value);
	 * 
	 * if(strPattern==null) throw new ExpressionException("Missing attribute [pattern]");
	 * 
	 * if(!Perl5Util.matches(strPattern, str)) throw new
	 * ExpressionException("The value ["+str+"] doesn't match the provided pattern ["+strPattern+"]");
	 * setVariable(name,str); } else if ( type.equals( "int" ) || type.equals( "integer" ) ) {
	 * 
	 * if ( !Decision.isInteger( value ) ) throw new ExpressionException( "The value [" + value +
	 * "] is not a valid integer" );
	 * 
	 * setVariable( name, value ); } else { if(!Decision.isCastableTo(type,value,true,true,maxLength)) {
	 * if(maxLength>-1 && ("email".equalsIgnoreCase(type) || "url".equalsIgnoreCase(type) ||
	 * "string".equalsIgnoreCase(type))) { StringBuilder msg=new
	 * StringBuilder(CasterException.createMessage(value, type));
	 * msg.append(" with a maximum length of "+maxLength+" characters"); throw new
	 * CasterException(msg.toString()); } throw new CasterException(value,type); }
	 * 
	 * setVariable(name,value); //REALCAST setVariable(name,Caster.castTo(this,type,value,true)); } }
	 * else if(isNew) setVariable(name,value); }
	 */
    @Override
    @Throws(PageException::class)
    fun removeVariable(`var`: String?): Object? {
        return VariableInterpreter.removeVariable(this, `var`)
    }

    /**
     * a variable reference, references to variable, to modifed it, with global effect.
     *
     * @param var variable name to get
     * @return return a variable reference by string syntax ("scopename.key.key" -> "url.name")
     * @throws PageException
     */
    @Throws(PageException::class)
    fun getVariableReference(`var`: String?): VariableReference? {
        return VariableInterpreter.getVariableReference(this, `var`)
    }

    @Override
    @Throws(PageException::class)
    fun getFunction(coll: Object?, key: String?, args: Array<Object?>?): Object? {
        return variableUtil.callFunctionWithoutNamedValues(this, coll, key, args)
    }

    @Override
    @Throws(PageException::class)
    fun getFunction(coll: Object?, key: Key?, args: Array<Object?>?): Object? {
        return variableUtil.callFunctionWithoutNamedValues(this, coll, key, args)
    }

    // FUTURE add to interface
    fun getFunction(coll: Object?, key: Key?, args: Array<Object?>?, defaultValue: Object?): Object? {
        return variableUtil.callFunctionWithoutNamedValues(this, coll, key, args, false, defaultValue)
    }

    fun getFunction2(coll: Object?, key: Key?, args: Array<Object?>?, defaultValue: Object?): Object? {
        return variableUtil.callFunctionWithoutNamedValues(this, coll, key, args, true, defaultValue)
    }

    @Override
    @Throws(PageException::class)
    fun getFunctionWithNamedValues(coll: Object?, key: String?, args: Array<Object?>?): Object? {
        return variableUtil.callFunctionWithNamedValues(this, coll, key, args)
    }

    @Override
    @Throws(PageException::class)
    fun getFunctionWithNamedValues(coll: Object?, key: Key?, args: Array<Object?>?): Object? {
        return variableUtil.callFunctionWithNamedValues(this, coll, key, args)
    }

    // FUTURE add to interface
    fun getFunctionWithNamedValues(coll: Object?, key: Key?, args: Array<Object?>?, defaultValue: Object?): Object? {
        return variableUtil.callFunctionWithNamedValues(this, coll, key, args, false, defaultValue)
    }

    fun getFunctionWithNamedValues2(coll: Object?, key: Key?, args: Array<Object?>?, defaultValue: Object?): Object? {
        return variableUtil.callFunctionWithNamedValues(this, coll, key, args, true, defaultValue)
    }

    @Override
    fun getConfig(): ConfigWeb? {
        return config
    }

    @Override
    @Throws(PageException::class)
    fun getIterator(key: String?): Iterator? {
        val o: Object = VariableInterpreter.getVariable(this, key)
        if (o is Iterator) return o
        throw ExpressionException("[$key] is not an iterator object")
    }

    @Override
    @Throws(PageException::class)
    fun getQuery(key: String?): Query? {
        val value: Object = VariableInterpreter.getVariable(this, key)
        if (Decision.isQuery(value)) return Caster.toQuery(value)
        throw CasterException(value, Query::class.java) /// ("["+key+"] is not a query object, object is from type ");
    }

    @Override
    @Throws(PageException::class)
    fun getQuery(value: Object?): Query? {
        var value: Object? = value
        if (Decision.isQuery(value)) return Caster.toQuery(value)
        value = VariableInterpreter.getVariable(this, Caster.toString(value))
        if (Decision.isQuery(value)) return Caster.toQuery(value)
        throw CasterException(value, Query::class.java)
    }

    @Override
    fun setAttribute(name: String?, value: Object?) {
        try {
            if (value == null) removeVariable(name) else setVariable(name, value)
        } catch (e: PageException) {
        }
    }

    @Override
    fun setAttribute(name: String?, value: Object?, scope: Int) {
        when (scope) {
            javax.servlet.jsp.PageContext.APPLICATION_SCOPE -> if (value == null) getServletContext().removeAttribute(name) else getServletContext().setAttribute(name, value)
            javax.servlet.jsp.PageContext.PAGE_SCOPE -> setAttribute(name, value)
            javax.servlet.jsp.PageContext.REQUEST_SCOPE -> if (value == null) req.removeAttribute(name) else setAttribute(name, value)
            javax.servlet.jsp.PageContext.SESSION_SCOPE -> {
                val s: HttpSession = req.getSession(true)
                if (value == null) s.removeAttribute(name) else s.setAttribute(name, value)
            }
        }
    }

    @Override
    fun getAttribute(name: String?): Object? {
        return try {
            getVariable(name)
        } catch (e: PageException) {
            null
        }
    }

    @Override
    fun getAttribute(name: String?, scope: Int): Object? {
        when (scope) {
            javax.servlet.jsp.PageContext.APPLICATION_SCOPE -> return getServletContext().getAttribute(name)
            javax.servlet.jsp.PageContext.PAGE_SCOPE -> return getAttribute(name)
            javax.servlet.jsp.PageContext.REQUEST_SCOPE -> return req.getAttribute(name)
            javax.servlet.jsp.PageContext.SESSION_SCOPE -> {
                val s: HttpSession = req.getSession()
                if (s != null) return s.getAttribute(name)
            }
        }
        return null
    }

    @Override
    fun findAttribute(name: String?): Object? {
        // page
        var value: Object? = getAttribute(name)
        if (value != null) return value
        // request
        value = req.getAttribute(name)
        if (value != null) return value
        // session
        val s: HttpSession = req.getSession()
        value = if (s != null) s.getAttribute(name) else null
        if (value != null) return value
        // application
        value = getServletContext().getAttribute(name)
        return if (value != null) value else null
    }

    @Override
    fun removeAttribute(name: String?) {
        setAttribute(name, null)
    }

    @Override
    fun removeAttribute(name: String?, scope: Int) {
        setAttribute(name, null, scope)
    }

    @Override
    fun getAttributesScope(name: String?): Int {
        // page
        if (getAttribute(name) != null) return PageContext.PAGE_SCOPE
        // request
        if (req.getAttribute(name) != null) return PageContext.REQUEST_SCOPE
        // session
        val s: HttpSession = req.getSession()
        if (s != null && s.getAttribute(name) != null) return PageContext.SESSION_SCOPE
        // application
        return if (getServletContext().getAttribute(name) != null) PageContext.APPLICATION_SCOPE else 0
    }

    @Override
    fun getAttributeNamesInScope(scope: Int): Enumeration<String?>? {
        when (scope) {
            javax.servlet.jsp.PageContext.APPLICATION_SCOPE -> return getServletContext().getAttributeNames()
            javax.servlet.jsp.PageContext.PAGE_SCOPE -> return ItAsEnum.toStringEnumeration(variablesScope().keyIterator())
            javax.servlet.jsp.PageContext.REQUEST_SCOPE -> return req.getAttributeNames()
            javax.servlet.jsp.PageContext.SESSION_SCOPE -> return req.getSession(true).getAttributeNames()
        }
        return null
    }

    @Override
    fun getOut(): JspWriter? {
        return forceWriter
    }

    @Override
    fun getSession(): HttpSession? {
        return getHttpServletRequest().getSession()
    }

    @Override
    fun getPage(): Object? {
        return variablesScope()
    }

    @Override
    fun getRequest(): ServletRequest? {
        return getHttpServletRequest()
    }

    @Override
    fun getHttpServletRequest(): HttpServletRequest? {
        return req
    }

    @Override
    fun getResponse(): ServletResponse? {
        return rsp
    }

    @Override
    fun getHttpServletResponse(): HttpServletResponse? {
        return rsp
    }

    @Override
    @Throws(IOException::class)
    fun getResponseStream(): OutputStream? {
        return getRootOut().getResponseStream()
    }

    @Override
    fun getException(): Exception? {
        // TODO impl
        return exception
    }

    @Override
    fun getServletConfig(): ServletConfig? {
        return config
    }

    @Override
    fun getServletContext(): ServletContext? {
        return servlet.getServletContext()
    }

    @Override
    fun handlePageException(pe: PageException?) {
        handlePageException(pe, true)
    }

    fun handlePageException(pe: PageException?, setHeader: Boolean) {
        var pe: PageException? = pe
        if (!Abort.isSilentAbort(pe)) {
            // if(requestTimeoutException!=null)
            // pe=Caster.toPageException(requestTimeoutException);
            val statusCode = getStatusCode(pe)

            // prepare response
            if (rsp != null) {
                // content-type
                val cs: Charset = ReqRspUtil.getCharacterEncoding(this, rsp)
                if (cs == null) ReqRspUtil.setContentType(rsp, "text/html") else ReqRspUtil.setContentType(rsp, "text/html; charset=" + cs.name())

                // expose error message in header
                if (rsp != null && pe.getExposeMessage()) rsp.setHeader("exception-message", StringUtil.emptyIfNull(pe.getMessage()).replace('\n', ' '))

                // status code
                if (getConfig().getErrorStatusCode()) rsp.setStatus(statusCode)
            }
            var ep: ErrorPage = errorPagePool.getErrorPage(pe, ErrorPageImpl.TYPE_EXCEPTION)

            // ExceptionHandler.printStackTrace(this,pe);
            ExceptionHandler.log(getConfig(), pe)

            // error page exception
            if (ep != null) {
                pe = try {
                    val sct: Struct = pe.getErrorBlock(this, ep)
                    variablesScope().setEL(KeyConstants._error, sct)
                    variablesScope().setEL(KeyConstants._cferror, sct)
                    doInclude(arrayOf<PageSource?>(ep.getTemplate()), false)
                    return
                } catch (t: Throwable) {
                    ExceptionUtil.rethrowIfNecessary(t)
                    if (Abort.isSilentAbort(t)) return
                    Caster.toPageException(t)
                }
            }

            // error page request
            ep = errorPagePool.getErrorPage(pe, ErrorPageImpl.TYPE_REQUEST)
            if (ep != null) {
                val ps: PageSource = ep.getTemplate()
                if (ps.physcalExists()) {
                    val res: Resource = ps.getResource()
                    try {
                        var content: String? = IOUtil.toString(res, getConfig().getTemplateCharset())
                        val sct: Struct = pe.getErrorBlock(this, ep)
                        val it: Iterator<Entry<Key?, Object?>?> = sct.entryIterator()
                        var e: Entry<Key?, Object?>?
                        var v: String
                        while (it.hasNext()) {
                            e = it.next()
                            v = Caster.toString(e.getValue(), null)
                            if (v != null) content = repl(content, e.getKey().getString(), v)
                        }
                        write(content)
                        return
                    } catch (t: Throwable) {
                        ExceptionUtil.rethrowIfNecessary(t)
                        pe = Caster.toPageException(t)
                    }
                } else pe = ApplicationException(("The error page template for type request only works if the actual source file also exists. If the exception file is in an "
                        + Constants.NAME) + " archive (.lar), you need to use type exception instead.")
            }
            try {
                val template: String = getConfig().getErrorTemplate(statusCode)
                if (!StringUtil.isEmpty(template)) {
                    pe = try {
                        val catchBlock: Struct = pe.getCatchBlock(getConfig())
                        variablesScope().setEL(KeyConstants._cfcatch, catchBlock)
                        variablesScope().setEL(KeyConstants._catch, catchBlock)
                        doInclude(template, false)
                        return
                    } catch (e: PageException) {
                        e
                    }
                }
                if (!Abort.isSilentAbort(pe)) forceWrite(getConfig().getDefaultDumpWriter(DumpWriter.DEFAULT_RICH).toString(this, pe.toDumpData(this, 9999, DumpUtil.toDumpProperties()), true))
            } catch (e: Exception) {
            }
        }
    }

    private fun getStatusCode(pe: PageException?): Int {
        var pe: PageException? = pe
        var statusCode = 500
        var maxDeepFor404 = 0
        if (pe is ModernAppListenerException) {
            pe = (pe as ModernAppListenerException?).getPageException()
            maxDeepFor404 = 1
        } else if (pe is PageExceptionBox) pe = (pe as PageExceptionBox?).getPageException()
        if (pe is MissingIncludeException) {
            val mie: MissingIncludeException? = pe as MissingIncludeException?
            if (mie.getPageDeep() <= maxDeepFor404) statusCode = 404
        }
        return statusCode
    }

    @Override
    fun handlePageException(e: Exception?) {
        handlePageException(Caster.toPageException(e))
    }

    @Override
    fun handlePageException(t: Throwable?) {
        handlePageException(Caster.toPageException(t))
    }

    @Override
    fun setHeader(name: String?, value: String?) {
        rsp.setHeader(name, value)
    }

    @Override
    fun pushBody(): BodyContent? {
        forceWriter = bodyContentStack.push()
        writer = if (enablecfoutputonly > 0 && outputState == 0) {
            devNull
        } else forceWriter
        return forceWriter as BodyContent?
    }

    @Override
    fun popBody(): JspWriter? {
        forceWriter = bodyContentStack.pop()
        writer = if (enablecfoutputonly > 0 && outputState == 0) {
            devNull
        } else forceWriter
        return forceWriter
    }

    @Override
    fun outputStart() {
        outputState++
        if (enablecfoutputonly > 0 && outputState == 1) writer = forceWriter
        // if(enablecfoutputonly && outputState>0) unsetDevNull();
    }

    @Override
    fun outputEnd() {
        outputState--
        if (enablecfoutputonly > 0 && outputState == 0) writer = devNull
    }

    @Override
    fun setCFOutputOnly(boolEnablecfoutputonly: Boolean) {
        if (boolEnablecfoutputonly) enablecfoutputonly++ else if (enablecfoutputonly > 0) enablecfoutputonly--
        setCFOutputOnly(enablecfoutputonly)
        // if(!boolEnablecfoutputonly)setCFOutputOnly(enablecfoutputonly=0);
    }

    @Override
    fun setCFOutputOnly(enablecfoutputonly: Short) {
        this.enablecfoutputonly = enablecfoutputonly
        if (enablecfoutputonly > 0) {
            if (outputState == 0) writer = devNull
        } else {
            writer = forceWriter
        }
    }

    fun getCFOutputOnly(): Short {
        return enablecfoutputonly
    }

    /**
     * FUTURE - add to interface
     *
     * @return true if the Request is in silent mode via cfslient
     */
    fun isSilent(): Boolean {
        return bodyContentStack.getDevNull()
    }

    @Override
    fun setSilent(): Boolean {
        val before: Boolean = bodyContentStack.getDevNull()
        bodyContentStack.setDevNull(true)
        forceWriter = bodyContentStack.getWriter()
        writer = forceWriter
        return before
    }

    @Override
    fun unsetSilent(): Boolean {
        val before: Boolean = bodyContentStack.getDevNull()
        bodyContentStack.setDevNull(false)
        forceWriter = bodyContentStack.getWriter()
        writer = if (enablecfoutputonly > 0 && outputState == 0) {
            devNull
        } else forceWriter
        return before
    }

    @Override
    fun getDebugger(): Debugger? {
        return debugger
    }

    @Override
    @Throws(PageException::class)
    fun executeRest(realPath: String?, throwExcpetion: Boolean) {
        initallog()
        var listener: ApplicationListener? = null // config.get ApplicationListener();
        try {
            var pathInfo: String? = req.getPathInfo()

            // charset
            try {
                var charset: String = HTTPUtil.splitMimeTypeAndCharset(req.getContentType(), arrayOf<String?>("", "")).get(1)
                if (StringUtil.isEmpty(charset)) charset = getWebCharset().name()
                val reqURL: java.net.URL = URL(req.getRequestURL().toString())
                val path: String = ReqRspUtil.decode(reqURL.getPath(), charset, true)
                val srvPath: String = req.getServletPath()
                if (path.startsWith(srvPath)) {
                    pathInfo = path.substring(srvPath.length())
                }
            } catch (e: Exception) {
            }

            // Service mapping
            if (StringUtil.isEmpty(pathInfo) || pathInfo!!.equals("/")) { // ToDo
                // list available services (if enabled in admin)
                if (config.getRestList()) {
                    try {
                        val _req: HttpServletRequest? = getHttpServletRequest()
                        write("Available sevice mappings are:<ul>")
                        val mappings: Array<lucee.runtime.rest.Mapping?> = config.getRestMappings()
                        var _mapping: lucee.runtime.rest.Mapping?
                        var path: String
                        for (i in mappings.indices) {
                            _mapping = mappings[i]
                            val p: Resource = _mapping!!.getPhysical()
                            path = _req.getContextPath() + ReqRspUtil.getScriptName(this, _req) + _mapping.getVirtual()
                            write("<li " + (if (p == null || !p.isDirectory()) " style=\"color:red\"" else "") + ">" + path + "</li>")
                        }
                        write("</ul>")
                    } catch (e: IOException) {
                        throw Caster.toPageException(e)
                    }
                } else RestUtil.setStatus(this, 404, null)
                return
            }

            // check for matrix
            var index: Int
            var entry: String?
            val matrix: Struct = StructImpl()
            while (pathInfo.lastIndexOf(';').also { index = it } != -1) {
                entry = pathInfo.substring(index + 1)
                pathInfo = pathInfo.substring(0, index)
                if (StringUtil.isEmpty(entry, true)) continue
                index = entry.indexOf('=')
                if (index != -1) matrix.setEL(KeyImpl.init(entry.substring(0, index).trim()), entry.substring(index + 1).trim()) else matrix.setEL(KeyImpl.init(entry.trim()), "")
            }

            // get accept
            val accept: List<MimeType?> = ReqRspUtil.getAccept(this)
            val contentType: MimeType = ReqRspUtil.getContentType(this)

            // check for format extension
            // int format = getApplicationContext().getRestSettings().getReturnFormat();
            val format: Int
            var hasFormatExtension = false
            if (StringUtil.endsWithIgnoreCase(pathInfo, ".json")) {
                pathInfo = pathInfo.substring(0, pathInfo!!.length() - 5)
                format = UDF.RETURN_FORMAT_JSON
                accept.clear()
                accept.add(MimeType.APPLICATION_JSON)
                hasFormatExtension = true
            } else if (StringUtil.endsWithIgnoreCase(pathInfo, ".wddx")) {
                pathInfo = pathInfo.substring(0, pathInfo!!.length() - 5)
                format = UDF.RETURN_FORMAT_WDDX
                accept.clear()
                accept.add(MimeType.APPLICATION_WDDX)
                hasFormatExtension = true
            } else if (StringUtil.endsWithIgnoreCase(pathInfo, ".cfml")) {
                pathInfo = pathInfo.substring(0, pathInfo!!.length() - 5)
                format = UDF.RETURN_FORMAT_SERIALIZE
                accept.clear()
                accept.add(MimeType.APPLICATION_CFML)
                hasFormatExtension = true
            } else if (StringUtil.endsWithIgnoreCase(pathInfo, ".serialize")) {
                pathInfo = pathInfo.substring(0, pathInfo!!.length() - 10)
                format = UDF.RETURN_FORMAT_SERIALIZE
                accept.clear()
                accept.add(MimeType.APPLICATION_CFML)
                hasFormatExtension = true
            } else if (StringUtil.endsWithIgnoreCase(pathInfo, ".xml")) {
                pathInfo = pathInfo.substring(0, pathInfo!!.length() - 4)
                format = UDF.RETURN_FORMAT_XML
                accept.clear()
                accept.add(MimeType.APPLICATION_XML)
                hasFormatExtension = true
            } else if (StringUtil.endsWithIgnoreCase(pathInfo, ".java")) {
                pathInfo = pathInfo.substring(0, pathInfo!!.length() - 5)
                format = UDF.RETURN_FORMAT_JAVA
                accept.clear()
                accept.add(MimeType.APPLICATION_JAVA)
                hasFormatExtension = true
            } else {
                format = if (getApplicationContext() == null) null else getApplicationContext().getRestSettings().getReturnFormat()
                // MimeType mt=MimeType.toMimetype(format);
                // if(mt!=null)accept.add(mt);
            }
            if (accept.size() === 0) accept.add(MimeType.ALL)

            // loop all mappings
            // lucee.runtime.rest.Result result = null;//config.getRestSource(pathInfo, null);
            var rl: RestRequestListener? = null
            val restMappings: Array<lucee.runtime.rest.Mapping?> = config.getRestMappings()
            var m: lucee.runtime.rest.Mapping?
            var mapping: lucee.runtime.rest.Mapping? = null
            var defaultMapping: lucee.runtime.rest.Mapping? = null
            // String callerPath=null;
            if (restMappings != null) for (i in restMappings.indices) {
                m = restMappings[i]
                if (m!!.isDefault()) defaultMapping = m
                if (pathInfo.startsWith(m.getVirtualWithSlash(), 0) && m!!.getPhysical() != null) {
                    mapping = m
                    // result =
                    // m.getResult(this,callerPath=pathInfo.substring(m.getVirtual().length()),format,matrix,null);
                    rl = RestRequestListener(m, pathInfo.substring(m.getVirtual().length()), matrix, format, hasFormatExtension, accept, contentType, null)
                    break
                }
            }

            // default mapping
            if (mapping == null && defaultMapping != null && defaultMapping.getPhysical() != null) {
                mapping = defaultMapping
                // result = mapping.getResult(this,callerPath=pathInfo,format,matrix,null);
                rl = RestRequestListener(mapping, pathInfo, matrix, format, hasFormatExtension, accept, contentType, null)
            }

            // base = PageSourceImpl.best(config.getPageSources(this,null,realPath,true,false,true));
            if (mapping == null || mapping.getPhysical() == null) {
                RestUtil.setStatus(this, 404, "no rest service for [" + HTMLEntities.escapeHTML(pathInfo).toString() + "] found")
                getLog("rest").error("REST", "no rest service for [$pathInfo] found")
            } else {
                base = config.toPageSource(null, mapping.getPhysical(), null)
                listener = (base.getMapping() as MappingImpl)!!.getApplicationListener()
                listener.onRequest(this, base, rl)
            }
        } catch (t: Throwable) {
            ExceptionUtil.rethrowIfNecessary(t)
            val pe: PageException = Caster.toPageException(t)
            if (!Abort.isSilentAbort(pe)) {
                log(true)
                if (fdEnabled) {
                    FDSignal.signal(pe, false)
                }
                if (listener == null) {
                    listener = if (base == null) config.getApplicationListener() else (base.getMapping() as MappingImpl)!!.getApplicationListener()
                }
                listener.onError(this, pe)
            } else log(false)
            if (throwExcpetion) throw pe
        } finally {
            if (enablecfoutputonly > 0) {
                setCFOutputOnly(0.toShort())
            }
            base = null
        }
    }

    @Override
    @Throws(PageException::class)
    fun execute(realPath: String?, throwExcpetion: Boolean, onlyTopLevel: Boolean) {
        currentTemplateDialect = CFMLEngine.DIALECT_LUCEE
        requestDialect = currentTemplateDialect
        setFullNullSupport()
        _execute(realPath, throwExcpetion, onlyTopLevel)
    }

    @Override
    @Throws(PageException::class)
    fun executeCFML(realPath: String?, throwExcpetion: Boolean, onlyTopLevel: Boolean) {
        currentTemplateDialect = CFMLEngine.DIALECT_CFML
        requestDialect = currentTemplateDialect
        setFullNullSupport()
        _execute(realPath, throwExcpetion, onlyTopLevel)
    }

    @Throws(PageException::class)
    private fun _execute(realPath: String?, throwExcpetion: Boolean, onlyTopLevel: Boolean) {
        var realPath = realPath
        if (config.getScriptProtect() and ApplicationContext.SCRIPT_PROTECT_URL > 0) {
            realPath = ScriptProtect.translate(realPath)
        }
        // we do not allow /../ within the real path
        if (realPath.indexOf("/../") !== -1) {
            throw ApplicationException("invalid path [$realPath]")
        }

        // convert realpath to a PageSource
        if (realPath.startsWith("/mapping-")) {
            base = null
            val index: Int = realPath.indexOf('/', 9)
            if (index > -1) {
                val type: String = realPath.substring(9, index)
                if (type.equalsIgnoreCase("tag")) {
                    base = getPageSource(arrayOf<Mapping?>(config.getDefaultTagMapping(), config.getDefaultServerTagMapping()), realPath.substring(index))
                } else if (type.equalsIgnoreCase("customtag")) {
                    base = getPageSource(config.getCustomTagMappings(), realPath.substring(index))
                }
            }
            if (base == null) base = PageSourceImpl.best(config.getPageSources(this, null, realPath, onlyTopLevel, false, true))
        } else base = PageSourceImpl.best(config.getPageSources(this, null, realPath, onlyTopLevel, false, true))
        execute(base, throwExcpetion, onlyTopLevel)
    }

    @Throws(PageException::class)
    private fun execute(ps: PageSource?, throwExcpetion: Boolean, onlyTopLevel: Boolean) {
        var ps: PageSource? = ps
        val listener: ApplicationListener?
        // if a listener is called (Web.cfc/Server.cfc we don't wanna any Application.cfc to be executed)
        if (listenerContext) listener = NoneAppListener() else if (getRequestDialect() == CFMLEngine.DIALECT_LUCEE) listener = ModernAppListener.getInstance() else if (gatewayContext) listener = config.getApplicationListener() else listener = (ps.getMapping() as MappingImpl)!!.getApplicationListener()
        var _t: Throwable? = null
        try {
            initallog()
            listener.onRequest(this, ps, null)
            if (ormSession != null) {
                releaseORM()
                removeLastPageSource(true)
            }
            log(false)
        } catch (t: Throwable) {
            if (ormSession != null) {
                try {
                    releaseORM()
                    removeLastPageSource(true)
                } catch (e: Exception) {
                }
            }
            val pe: PageException?
            if (t is ThreadDeath && getTimeoutStackTrace() != null) {
                pe = RequestTimeoutException(this, t as ThreadDeath)
                t = pe
            } else pe = Caster.toPageException(t, false)
            _t = t
            if (!Abort.isSilentAbort(pe)) {
                this.pe = pe
                log(true)
                if (fdEnabled) {
                    FDSignal.signal(pe, false)
                }
                listener.onError(this, pe) // call Application.onError()
            } else log(false)
            if (throwExcpetion) {
                ExceptionUtil.rethrowIfNecessary(t)
                throw pe
            }
        } finally {
            endTimeNS = System.nanoTime()
            if (enablecfoutputonly > 0) {
                setCFOutputOnly(0.toShort())
            }
            if (!gatewayContext && getConfig().debug()) {
                try {
                    listener.onDebug(this)
                } catch (e: Exception) {
                    pe = Caster.toPageException(e)
                    if (!Abort.isSilentAbort(pe)) listener.onError(this, pe)
                    ExceptionUtil.rethrowIfNecessary(e)
                }
            }
            ps = null
            if (_t != null) ExceptionUtil.rethrowIfNecessary(_t)
        }
    }

    private fun initallog() {
        if (!isGatewayContext() && config.isMonitoringEnabled()) {
            val monitors: Array<RequestMonitor?> = config.getRequestMonitors()
            if (monitors != null) for (i in monitors.indices) {
                if (monitors[i].isLogEnabled()) {
                    try {
                        (monitors[i] as RequestMonitorPro?).init(this)
                    } catch (e: Throwable) {
                        ExceptionUtil.rethrowIfNecessary(e)
                    }
                }
            }
        }
    }

    private fun log(error: Boolean) {
        if (!isGatewayContext() && config.isMonitoringEnabled()) {
            val monitors: Array<RequestMonitor?> = config.getRequestMonitors()
            if (monitors != null) for (i in monitors.indices) {
                if (monitors[i].isLogEnabled()) {
                    try {
                        monitors[i].log(this, error)
                    } catch (e: Throwable) {
                        ExceptionUtil.rethrowIfNecessary(e)
                    }
                }
            }
        }
    }

    private fun getPageSource(mappings: Array<Mapping?>?, realPath: String?): PageSource? {
        var ps: PageSource
        // print.err(mappings.length);
        for (i in mappings.indices) {
            ps = mappings!![i].getPageSource(realPath)
            // print.err(ps.getDisplayPath());
            if (ps.exists()) return ps
        }
        return null
    }

    @Override
    @Throws(ServletException::class, IOException::class)
    fun include(realPath: String?) {
        HTTPUtil.include(this, realPath)
    }

    @Override
    @Throws(ServletException::class, IOException::class)
    fun forward(realPath: String?) {
        HTTPUtil.forward(this, realPath)
    }

    @Override
    fun clear() {
        try {
            // print.o(getOut().getClass().getName());
            getOut().clear()
        } catch (e: IOException) {
        }
    }

    @Override
    fun getRequestTimeout(): Long {
        if (requestTimeout == -1L) {
            if (applicationContext != null) {
                return applicationContext.getRequestTimeout().getMillis()
            }
            requestTimeout = config.getRequestTimeout().getMillis()
        }
        return requestTimeout
    }

    @Override
    fun setRequestTimeout(requestTimeout: Long) {
        this.requestTimeout = requestTimeout
    }

    @Override
    fun getCFID(): String? {
        if (cfid == null) initIdAndToken()
        return cfid
    }

    @Override
    fun getCFToken(): String? {
        if (cftoken == null) initIdAndToken()
        return cftoken
    }

    @Override
    fun getURLToken(): String? {
        if (getConfig().getSessionType() === Config.SESSION_TYPE_JEE) {
            val s: HttpSession? = getSession()
            return "CFID=" + getCFID() + "&CFTOKEN=" + getCFToken() + "&jsessionid=" + if (s != null) s.getId() else ""
        }
        return "CFID=" + getCFID() + "&CFTOKEN=" + getCFToken()
    }

    @Override
    fun getJSessionId(): String? {
        return if (getConfig().getSessionType() === Config.SESSION_TYPE_JEE) {
            getSession().getId()
        } else null
    }

    /**
     * initialize the cfid and the cftoken
     */
    private fun initIdAndToken() {
        var setCookie = true
        // From URL
        var oCfid: Object? = if (READ_CFID_FROM_URL) urlScope().get(KeyConstants._cfid, null) else null
        var oCftoken: Object? = if (READ_CFID_FROM_URL) urlScope().get(KeyConstants._cftoken, null) else null

        // if CFID comes from URL, we only accept if already exists
        if (oCfid != null) {
            if (Decision.isGUIdSimple(oCfid)) {
                if (!scopeContext.hasExistingCFID(this, Caster.toString(oCfid, null))) {
                    oCfid = null
                    oCftoken = null
                }
            } else {
                oCfid = null
                oCftoken = null
            }
        }

        // Cookie
        if (oCfid == null) {
            setCookie = false
            oCfid = cookieScope().get(KeyConstants._cfid, null)
            oCftoken = cookieScope().get(KeyConstants._cftoken, null)
        }

        // check cookie value
        if (oCfid != null) {
            // cookie value is invalid, maybe from ACF
            if (!Decision.isGUIdSimple(oCfid)) {
                oCfid = null
                oCftoken = null
                val charset: Charset? = getWebCharset()

                // check if we have multiple cookies with the name "cfid" and another one is valid
                val cookies: Array<javax.servlet.http.Cookie?> = getHttpServletRequest().getCookies()
                var name: String
                var value: String
                if (cookies != null) {
                    for (i in cookies.indices) {
                        name = ReqRspUtil.decode(cookies[i].getName(), charset.name(), false)
                        // CFID
                        if ("cfid".equalsIgnoreCase(name)) {
                            value = ReqRspUtil.decode(cookies[i].getValue(), charset.name(), false)
                            if (Decision.isGUIdSimple(value)) oCfid = value
                            ReqRspUtil.removeCookie(getHttpServletResponse(), name)
                        } else if ("cftoken".equalsIgnoreCase(name)) {
                            value = ReqRspUtil.decode(cookies[i].getValue(), charset.name(), false)
                            if (isValidCfToken(value)) oCftoken = value
                            ReqRspUtil.removeCookie(getHttpServletResponse(), name)
                        }
                    }
                }
                if (oCfid != null) {
                    setCookie = true
                    if (oCftoken == null) oCftoken = "0"
                }
            }
        }
        // New One
        if (oCfid == null || oCftoken == null) {
            setCookie = true
            cfid = ScopeContext.getNewCFId()
            cftoken = ScopeContext.getNewCFToken()
        } else {
            cfid = Caster.toString(oCfid, null)
            cftoken = Caster.toString(oCftoken, "0")
        }
        if (setCookie && applicationContext.isSetClientCookies()) setClientCookies()
    }

    private fun isValidCfToken(value: String?): Boolean {
        return try {
            OpUtil.compare(this, value, "0") === 0
        } catch (e: PageException) {
            value!!.equals("0")
        }
    }

    fun resetIdAndToken() {
        cfid = ScopeContext.getNewCFId()
        cftoken = ScopeContext.getNewCFToken()
        if (applicationContext.isSetClientCookies()) setClientCookies()
    }

    private fun setClientCookies() {
        var tsExpires: TimeSpan? = SessionCookieDataImpl.DEFAULT.getTimeout()
        var domain: String = PageContextUtil.getCookieDomain(this)
        var httpOnly: Boolean = SessionCookieDataImpl.DEFAULT.isHttpOnly()
        var secure: Boolean = SessionCookieDataImpl.DEFAULT.isSecure()
        var samesite: Short = SessionCookieDataImpl.DEFAULT.getSamesite()
        var path: String = SessionCookieDataImpl.DEFAULT.getPath()
        val ac: ApplicationContext? = getApplicationContext()
        if (ac is ApplicationContextSupport) {
            val acs: ApplicationContextSupport? = ac as ApplicationContextSupport?
            val data: SessionCookieData = acs.getSessionCookie()
            if (data != null) {
                // expires
                val ts: TimeSpan = data.getTimeout()
                if (ts != null) tsExpires = ts
                // httpOnly
                httpOnly = data.isHttpOnly()
                // secure
                secure = data.isSecure()
                // domain
                val tmp: String = data.getDomain()
                if (!StringUtil.isEmpty(tmp, true)) domain = tmp.trim()
                // samesite
                samesite = data.getSamesite()
                // path
                val tmp2: String = data.getPath()
                if (!StringUtil.isEmpty(tmp2, true)) path = tmp2.trim()
            }
        }
        val expires: Int
        val tmp: Long = tsExpires.getSeconds()
        expires = if (Integer.MAX_VALUE < tmp) Integer.MAX_VALUE else tmp.toInt()
        (cookieScope() as CookieImpl?).setCookieEL(KeyConstants._cfid, cfid, expires, secure, path, domain, httpOnly, true, false, samesite)
        (cookieScope() as CookieImpl?).setCookieEL(KeyConstants._cftoken, cftoken, expires, secure, path, domain, httpOnly, true, false, samesite)
    }

    @Override
    fun getId(): Int {
        return id
    }

    /**
     * @return returns the root JSP Writer
     */
    fun getRootOut(): CFMLWriter? { // used in extension PDF
        return bodyContentStack.getBase()
    }

    @Override
    fun getRootWriter(): JspWriter? {
        return bodyContentStack.getBase()
    }

    @Override
    fun setPsq(psq: Boolean) {
        _psq = psq
    }

    @Override
    fun getPsq(): Boolean {
        if (_psq != null) return _psq.booleanValue()
        return if (applicationContext != null) {
            applicationContext.getQueryPSQ()
        } else config.getPSQL()
    }

    @Override
    fun getLocale(): Locale? {
        val l: Locale? = if (getApplicationContext() == null) null else getApplicationContext().getLocale()
        if (l != null) return l
        return if (locale != null) locale else config.getLocale()
    }

    @Override
    fun setLocale(locale: Locale?) {
        if (getApplicationContext() != null) getApplicationContext().setLocale(locale)
        this.locale = locale
        val rsp: HttpServletResponse? = getHttpServletResponse()
        val charEnc: Charset = ReqRspUtil.getCharacterEncoding(this, rsp)
        rsp.setLocale(locale)
        if (charEnc.equals(CharsetUtil.UTF8)) {
            ReqRspUtil.setContentType(rsp, "text/html; charset=UTF-8")
        } else if (!charEnc.equals(ReqRspUtil.getCharacterEncoding(this, rsp))) {
            ReqRspUtil.setContentType(rsp, "text/html; charset=$charEnc")
        }
    }

    @Override
    @Throws(ExpressionException::class)
    fun setLocale(strLocale: String?) {
        setLocale(Caster.toLocale(strLocale))
    }

    @Override
    fun setErrorPage(ep: ErrorPage?) {
        errorPagePool.setErrorPage(ep)
    }

    // called by generated bytecode
    @Throws(PageException::class)
    fun use(tagClassName: String?, fullname: String?, attrType: Int): Tag? {
        return use(tagClassName, null, null, fullname, attrType, null)
    }

    @Throws(PageException::class)
    fun use(tagClassName: String?, fullname: String?, attrType: Int, template: String?): Tag? {
        return use(tagClassName, null, null, fullname, attrType, template)
    }

    // called by generated bytecode
    @Throws(PageException::class)
    fun use(tagClassName: String?, tagBundleName: String?, tagBundleVersion: String?, fullname: String?, attrType: Int): Tag? {
        return use(tagClassName, tagBundleName, tagBundleVersion, fullname, attrType, null)
    }

    @Throws(PageException::class)
    fun use(tagClassName: String?, tagBundleName: String?, tagBundleVersion: String?, fullname: String?, attrType: Int, template: String?): Tag? {
        parentTag = currentTag
        currentTag = tagHandlerPool.use(tagClassName, tagBundleName, tagBundleVersion, getConfig().getIdentification())
        if (currentTag === parentTag) throw ApplicationException("")
        currentTag.setPageContext(this)
        currentTag.setParent(parentTag)
        if (currentTag is TagImpl) (currentTag as TagImpl?).setSourceTemplate(template)
        if (attrType >= 0 && fullname != null) {
            val attrs: Map<Collection.Key?, Object?> = applicationContext.getTagAttributeDefaultValues(this, fullname)
            if (attrs != null) {
                TagUtil.setAttributes(this, currentTag, attrs, attrType)
            }
        }
        return currentTag
    }

    @Throws(ClassException::class, ClassNotFoundException::class, IOException::class)
    fun useJavaFunction(page: Page?, className: String?): Object? {
        val jf: JF = ClassUtil.loadInstance(page.getPageSource().getMapping().getPhysicalClass(className))
        jf!!.setPageSource(page.getPageSource())
        return jf
    }

    fun reuse(tag: Tag?) {
        currentTag = tag.getParent()
        tagHandlerPool.reuse(tag)
    }

    fun reuse(tag: Tag?, tagBundleName: String?, tagBundleVersion: String?) {
        currentTag = tag.getParent()
        tagHandlerPool.reuse(tag, tagBundleName, tagBundleVersion)
    }

    @Override
    @Throws(JspException::class)
    fun initBody(bodyTag: BodyTag?, state: Int) {
        if (state != Tag.EVAL_BODY_INCLUDE) {
            bodyTag.setBodyContent(pushBody())
            bodyTag.doInitBody()
        }
    }

    @Override
    fun releaseBody(bodyTag: BodyTag?, state: Int) {
        if (bodyTag is TryCatchFinally) {
            (bodyTag as TryCatchFinally?).doFinally()
        }
        if (state != Tag.EVAL_BODY_INCLUDE) popBody()
    }

    /*
	 * *
	 * 
	 * @return returns the cfml compiler / public CFMLCompiler getCompiler() { return compiler; }
	 */
    @Override
    fun setVariablesScope(variables: Variables?) {
        var variables: Variables? = variables
        this.variables = variables
        undefinedScope().setVariableScope(variables)
        if (variables is ClosureScope) {
            variables = (variables as ClosureScope?).getVariables()
        }
        activeComponent = if (variables is StaticScope) {
            (variables as StaticScope?)!!.getComponent()
        } else if (variables is ComponentScope) {
            (variables as ComponentScope?).getComponent()
        } else {
            null
        }
    }

    @Override
    fun getActiveComponent(): Component? {
        return activeComponent
    }

    @Override
    @Throws(PageException::class)
    fun getRemoteUser(): Credential? {
        if (remoteUser == null) {
            val name: Key = KeyImpl.init(Login.getApplicationName(applicationContext))
            val roles: Resource = config.getConfigDir().getRealResource("roles")
            if (applicationContext.getLoginStorage() === Scope.SCOPE_SESSION) {
                if (hasCFSession()) {
                    val auth: Object = sessionScope().get(name, null)
                    if (auth != null) {
                        remoteUser = CredentialImpl.decode(auth, roles, true)
                    }
                }
            } else if (applicationContext.getLoginStorage() === Scope.SCOPE_COOKIE) {
                val auth: Object = cookieScope().get(name, null)
                if (auth != null) {
                    remoteUser = CredentialImpl.decode(auth, roles, true)
                }
            }
        }
        return remoteUser
    }

    @Override
    fun clearRemoteUser() {
        if (remoteUser != null) remoteUser = null
        val name: String = Login.getApplicationName(applicationContext)
        cookieScope().removeEL(KeyImpl.init(name))
        try {
            sessionScope().removeEL(KeyImpl.init(name))
        } catch (e: PageException) {
        }
    }

    @Override
    fun setRemoteUser(remoteUser: Credential?) {
        this.remoteUser = remoteUser
    }

    @Override
    fun getVariableUtil(): VariableUtil? {
        return variableUtil
    }

    @Override
    @Throws(PageException::class)
    fun throwCatch() {
        if (exception != null) throw exception
        throw ApplicationException("invalid context for tag/script expression rethow")
    }

    @Override
    fun setCatch(t: Throwable?): PageException? {
        val pe: PageException? = if (t == null) null else Caster.toPageException(t)
        _setCatch(pe, null, false, true, false)
        return pe
    }

    @Override
    fun setCatch(pe: PageException?) {
        _setCatch(pe, null, false, true, false)
    }

    @Override
    fun setCatch(pe: PageException?, caught: Boolean, store: Boolean) {
        _setCatch(pe, null, caught, store, true)
    }

    fun setCatch(pe: PageException?, name: String?, caught: Boolean, store: Boolean) {
        _setCatch(pe, name, caught, store, true)
    }

    fun _setCatch(pe: PageException?, name: String?, caught: Boolean, store: Boolean, signal: Boolean) {
        if (signal && fdEnabled) {
            FDSignal.signal(pe, caught)
        }
        // boolean outer = exception != null && exception == pe;
        exception = pe
        if (store) {
            val u: Undefined? = undefinedScope()
            if (pe == null) {
                (if (u.getCheckArguments()) u.localScope() else u).removeEL(KeyConstants._cfcatch)
                if (name != null && !StringUtil.isEmpty(name, true)) (if (u.getCheckArguments()) u.localScope() else u).removeEL(KeyImpl.getInstance(name.trim()))
            } else {
                (if (u.getCheckArguments()) u.localScope() else u).setEL(KeyConstants._cfcatch, pe.getCatchBlock(config))
                if (name != null && !StringUtil.isEmpty(name, true)) (if (u.getCheckArguments()) u.localScope() else u).setEL(KeyImpl.getInstance(name.trim()), pe.getCatchBlock(config))
                if (!gatewayContext && config.debug() && config.hasDebugOptions(ConfigPro.DEBUG_EXCEPTION)) {
                    /*
					 * print.e("-----------------------"); print.e("msg:" + pe.getMessage()); print.e("caught:" +
					 * caught); print.e("store:" + store); print.e("signal:" + signal); print.e("outer:" + outer);
					 */
                    debugger.addException(config, exception)
                }
            }
        }
    }

    /**
     * @return return current catch
     */
    @Override
    fun getCatch(): PageException? {
        return exception
    }

    @Override
    fun clearCatch() {
        exception = null
        val u: Undefined? = undefinedScope()
        (if (u.getCheckArguments()) u.localScope() else u).removeEL(KeyConstants._cfcatch)
    }

    @Override
    fun addPageSource(ps: PageSource?, alsoInclude: Boolean) {
        currentTemplateDialect = ps.getDialect()
        setFullNullSupport()
        pathList.add(ps)
        if (alsoInclude) includePathList.add(ps)
    }

    fun addPageSource(ps: PageSource?, psInc: PageSource?) {
        currentTemplateDialect = ps.getDialect()
        setFullNullSupport()
        pathList.add(ps)
        if (psInc != null) includePathList.add(psInc)
    }

    @Override
    fun removeLastPageSource(alsoInclude: Boolean) {
        if (!pathList.isEmpty()) pathList.removeLast()
        if (!pathList.isEmpty()) {
            currentTemplateDialect = pathList.getLast().getDialect()
            setFullNullSupport()
        }
        if (alsoInclude && !includePathList.isEmpty()) includePathList.removeLast()
    }

    fun getUDFs(): Array<UDF?>? {
        return udfs.toArray(arrayOfNulls<UDF?>(udfs.size()))
    }

    fun addUDF(udf: UDF?) {
        udfs.add(udf)
    }

    fun removeUDF() {
        if (!udfs.isEmpty()) udfs.removeLast()
    }

    fun getFTPPool(): FTPPoolImpl? {
        return ftpPool
    }

    /*
	 * *
	 * 
	 * @return Returns the manager. / public DataSourceManager getManager() { return manager; }
	 */
    @Override
    fun getApplicationContext(): ApplicationContext? {
        return applicationContext
    }

    @Override
    fun setApplicationContext(applicationContext: ApplicationContext?) {
        var applicationContext: ApplicationContext? = applicationContext
        session = null
        application = null
        client = null
        if (applicationContext != null) this.applicationContext = applicationContext as ApplicationContextSupport? else applicationContext = this.applicationContext
        if (applicationContext == null) return
        setFullNullSupport()
        val scriptProtect: Int = applicationContext.getScriptProtect()

        // ScriptProtecting
        if (config.mergeFormAndURL()) {
            form.setScriptProtecting(applicationContext,
                    scriptProtect and ApplicationContext.SCRIPT_PROTECT_FORM > 0 || scriptProtect and ApplicationContext.SCRIPT_PROTECT_URL > 0)
        } else {
            form.setScriptProtecting(applicationContext, scriptProtect and ApplicationContext.SCRIPT_PROTECT_FORM > 0)
            url.setScriptProtecting(applicationContext, scriptProtect and ApplicationContext.SCRIPT_PROTECT_URL > 0)
        }
        cookie.setScriptProtecting(applicationContext, scriptProtect and ApplicationContext.SCRIPT_PROTECT_COOKIE > 0)
        // CGI
        cgiR.setScriptProtecting(applicationContext, scriptProtect and ApplicationContext.SCRIPT_PROTECT_CGI > 0)
        cgiRW.setScriptProtecting(applicationContext, scriptProtect and ApplicationContext.SCRIPT_PROTECT_CGI > 0)
        undefined.reinitialize(this)
    }

    /**
     * @return return value of method "onApplicationStart" or true
     * @throws PageException
     */
    @Throws(PageException::class)
    fun initApplicationContext(listener: ApplicationListener?): Boolean {
        var initSession = false
        // AppListenerSupport listener = (AppListenerSupport) config.get ApplicationListener();
        val lock: KeyLock<String?> = config.getContextLock()
        val name: String = StringUtil.emptyIfNull(applicationContext.getName())

        // Application
        application = scopeContext.getApplicationScope(this, false, null) // this is needed that the
        // application scope is initilized
        if (application == null || !application.isInitalized()) {
            // because we had no lock so far, it could be that we more than one thread here at the same time
            val nameLock: Lock = lock.lock(name, getRequestTimeout())
            try {
                val isNew: RefBoolean = RefBooleanImpl(false)
                application = scopeContext.getApplicationScope(this, true, isNew)
                // now within the lock, we get the application
                if (isNew.toBooleanValue()) {
                    try {
                        if (!(listener as AppListenerSupport?).onApplicationStart(this, application)) {
                            scopeContext.removeApplicationScope(this)
                            return false
                        }
                    } catch (pe: PageException) {
                        scopeContext.removeApplicationScope(this)
                        throw pe
                    } finally {
                        if (application != null) application.initialize(this)
                    }
                }
            } finally {
                // print.o("inner-unlock:"+token);
                lock.unlock(nameLock)
            }
        }

        // Session
        initSession = applicationContext.isSetSessionManagement() && listener.hasOnSessionStart(this) && !scopeContext.hasExistingSessionScope(this)
        if (initSession) {
            val token = name + ":" + getCFID()
            val tokenLock: Lock = lock.lock(token, getRequestTimeout())
            try {
                // we need to check it again within the lock, to make sure the call is exclusive
                initSession = applicationContext.isSetSessionManagement() && listener.hasOnSessionStart(this) && !scopeContext.hasExistingSessionScope(this)

                // init session
                if (initSession) {
                    // session must be initlaized here
                    (listener as AppListenerSupport?).onSessionStart(this, scopeContext.getSessionScope(this, DUMMY_BOOL))
                }
            } finally {
                // print.o("outer-unlock:"+token);
                lock.unlock(tokenLock)
            }
        }
        return true
    }

    /**
     * @return the scope factory
     */
    fun getScopeFactory(): ScopeFactory? {
        return scopeFactory
    }

    @Override
    fun getCurrentTag(): Tag? {
        return currentTag
    }

    @Override
    fun getStartTime(): Long {
        return startTime
    }

    fun getStartTimeNS(): Long {
        return startTimeNS
    }

    fun getEndTimeNS(): Long {
        return endTimeNS
    }

    fun setEndTimeNS(endTimeNS: Long) {
        this.endTimeNS = endTimeNS
    }

    @Override
    fun getThread(): Thread? {
        return thread
    }

    fun setThread(thread: Thread?) {
        this.thread = thread
    }

    @Override
    fun getExecutionTime(): Long {
        return executionTime
    }

    @Override
    fun setExecutionTime(executionTime: Long) {
        this.executionTime = executionTime
    }

    @Override
    @Throws(PageException::class)
    fun compile(pageSource: PageSource?) {
        val classRootDir: Resource = pageSource.getMapping().getClassRootDirectory()
        val dialect = getCurrentTemplateDialect()
        try {
            config.getCompiler().compile(config, pageSource, config.getTLDs(dialect), config.getFLDs(dialect), classRootDir, false, ignoreScopes())
        } catch (e: Exception) {
            throw Caster.toPageException(e)
        }
    }

    @Override
    @Throws(PageException::class)
    fun compile(realPath: String?) {
        LogUtil.log(this, Log.LEVEL_INFO, PageContextImpl::class.java.getName(), "method PageContext.compile(String) should no longer be used!")
        compile(PageSourceImpl.best(getRelativePageSources(realPath)))
    }

    fun getServlet(): HttpServlet? {
        return servlet
    }

    @Override
    @Throws(PageException::class)
    fun loadComponent(compPath: String?): lucee.runtime.Component? {
        return ComponentLoader.searchComponent(this, null, compPath, null, null, false)
    }

    /**
     * @return the base
     */
    fun getBase(): PageSource? {
        return base
    }

    /**
     * @param base the base to set
     */
    fun setBase(base: PageSource?) {
        this.base = base
    }

    @Override
    fun getDataSourceManager(): DataSourceManager? {
        return manager
    }

    @Override
    @Throws(PageException::class)
    fun evaluate(expression: String?): Object? {
        return CFMLExpressionInterpreter(false).interpret(this, expression)
    }

    @Override
    @Throws(PageException::class)
    fun serialize(expression: Object?): String? {
        return Serialize.call(this, expression)
    }

    /**
     * @return the activeUDF
     */
    @Override
    fun getActiveUDF(): UDF? {
        return activeUDF
    }

    /**
     * @param activeUDF the activeUDF to set
     */
    fun setActiveUDF(activeUDF: UDF?) {
        this.activeUDF = activeUDF
    }

    fun getActiveUDFCalledName(): Collection.Key? {
        return activeUDFCalledName
    }

    fun setActiveUDFCalledName(activeUDFCalledName: Collection.Key?) {
        this.activeUDFCalledName = activeUDFCalledName
    }

    @Override
    fun getCFMLFactory(): CFMLFactory? {
        return config.getFactory()
    }

    @Override
    fun getParentPageContext(): PageContext? {
        // DebuggerImpl.deprecated(this, "PageContext.getParentPageContext", "the method
        // PageContext.getParentPageContext should no longer be used");
        return parent
    }

    fun getRootPageContext(): PageContext? {
        return root
    }

    fun getChildPageContexts(): Queue<PageContext?>? {
        return children
    }

    @Override
    fun getThreadScopeNames(): Array<String?>? {
        return if (threads == null) arrayOfNulls<String?>(0) else CollectionUtil.keysAsString(threads)
    }

    @Override
    fun getThreadScope(name: String?): Threads? {
        return getThreadScope(KeyImpl.init(name))
    }

    @Override
    fun getThreadScope(name: Collection.Key?): Threads? { // MUST who uses this? is cfthread/thread handling necessary
        if (threads == null) threads = CFThread()
        val obj: Object = threads.get(name, null)
        return if (obj is Threads) obj as Threads else null
    }

    fun getThreadScope(name: Collection.Key?, defaultValue: Object?): Object? {
        if (threads == null) threads = CFThread()
        if (name.equalsIgnoreCase(KeyConstants._cfthread)) return threads // do not change this, this is used!
        if (name.equalsIgnoreCase(KeyConstants._thread)) {
            val curr: ThreadsImpl? = getCurrentThreadScope()
            if (curr != null) return curr
        }
        return threads.get(name, defaultValue)
    }

    fun getCFThreadScope(): Struct? {
        if (threads == null) threads = CFThread()
        return threads
    }

    fun isThreads(obj: Object?): Boolean {
        return threads === obj
    }

    fun setCurrentThreadScope(thread: ThreadsImpl?) {
        currentThread = thread
    }

    fun getCurrentThreadScope(): ThreadsImpl? {
        return currentThread
    }

    @Override
    fun setThreadScope(name: String?, ct: Threads?) {
        setThreadScope(KeyImpl.init(name), ct)
    }

    @Override
    fun setThreadScope(name: Collection.Key?, ct: Threads?) {
        hasFamily = true
        if (threads == null) threads = CFThread()
        threads.setEL(name, ct)
    }

    /**
     *
     * @param name
     * @param ct
     */
    fun setAllThreadScope(name: Collection.Key?, ct: Threads?) {
        hasFamily = true
        if (allThreads == null) allThreads = HashMap<Collection.Key?, Threads?>()
        allThreads.put(name, ct)
    }

    fun getAllThreadScope(): Map<Collection.Key?, Threads?>? {
        return allThreads
    }

    @Override
    fun hasFamily(): Boolean {
        return hasFamily
    }

    @Override
    fun getTimeZone(): TimeZone? {
        if (timeZone != null) return timeZone
        val tz: TimeZone? = if (getApplicationContext() == null) null else getApplicationContext().getTimeZone()
        return if (tz != null) tz else config.getTimeZone()
    }

    @Override
    fun setTimeZone(timeZone: TimeZone?) {
        this.timeZone = timeZone
    }

    fun clearTimeZone() {
        timeZone = null
    }

    /**
     * @return the requestId
     */
    fun getRequestId(): Int {
        return requestId
    }

    private val pagesUsed: Set<String?>? = HashSet<String?>()
    private val activeQueries: Stack<ActiveQuery?>? = Stack<ActiveQuery?>()
    private val activeLocks: Stack<ActiveLock?>? = Stack<ActiveLock?>()
    private var literalTimestampWithTSOffset = false
    private var tagName: String? = null
    private var dummy = false
    private var listenSettings = false
    fun isTrusted(page: Page?): Boolean {
        if (page == null) return false
        val it: Short = (page.getPageSource().getMapping() as MappingImpl)!!.getInspectTemplate()
        if (it == ConfigPro.INSPECT_NEVER) return true
        return if (it == ConfigPro.INSPECT_ALWAYS) false else pagesUsed!!.contains("" + page.hashCode())
    }

    fun setPageUsed(page: Page?) {
        pagesUsed.add("" + page.hashCode())
    }

    @Override
    fun exeLogStart(position: Int, id: String?) {
        if (execLog != null) execLog.start(position, id)
    }

    @Override
    fun exeLogEnd(position: Int, id: String?) {
        if (execLog != null) execLog.end(position, id)
    }

    @Override
    @Throws(PageException::class)
    fun getORMSession(create: Boolean): ORMSession? {
        if (ormSession == null || !ormSession.isValid()) {
            if (!create) return null
            ormSession = config.getORMEngine(this).createSession(this)
        }
        val manager: DatasourceManagerImpl? = getDataSourceManager() as DatasourceManagerImpl?
        manager.add(this, ormSession)
        return ormSession
    }

    @Throws(IOException::class)
    fun getClassLoader(): ClassLoader? {
        return getResourceClassLoader()
    }

    @Throws(IOException::class)
    fun getClassLoader(reses: Array<Resource?>?): ClassLoader? {
        val rcl: ResourceClassLoader? = getResourceClassLoader()
        return rcl.getCustomResourceClassLoader(reses)
    }

    @Throws(IOException::class)
    private fun getResourceClassLoader(): ResourceClassLoader? {
        val js: JavaSettingsImpl = applicationContext.getJavaSettings() as JavaSettingsImpl
        if (js != null) {
            val jars: Array<Resource?> = js.getResourcesTranslated()
            if (jars.size > 0) return config.getResourceClassLoader().getCustomResourceClassLoader(jars)
        }
        return config.getResourceClassLoader()
    }

    @Throws(IOException::class)
    fun getRPCClassLoader(reload: Boolean): ClassLoader? {
        return getRPCClassLoader(reload, null)
    }

    @Throws(IOException::class)
    fun getRPCClassLoader(reload: Boolean, parents: Array<ClassLoader?>?): ClassLoader? {
        val js: JavaSettingsImpl = applicationContext.getJavaSettings() as JavaSettingsImpl
        val cl: ClassLoader = config.getRPCClassLoader(reload, parents)
        if (js != null) {
            val jars: Array<Resource?> = js.getResourcesTranslated()
            if (jars.size > 0) return (cl as PhysicalClassLoader).getCustomClassLoader(jars, reload)
        }
        return cl
    }

    fun resetSession() {
        session = null
    }

    fun resetClient() {
        client = null
    }

    /**
     * @return the gatewayContext
     */
    fun isGatewayContext(): Boolean {
        return gatewayContext
    }

    /**
     * @param gatewayContext the gatewayContext to set
     */
    fun setGatewayContext(gatewayContext: Boolean) {
        this.gatewayContext = gatewayContext
    }

    fun setListenerContext(listenerContext: Boolean) {
        this.listenerContext = listenerContext
    }

    fun setServerPassword(serverPassword: Password?) {
        this.serverPassword = serverPassword
    }

    fun getServerPassword(): Password? {
        return serverPassword
    }

    @Override
    fun getSessionType(): Short {
        return if (isGatewayContext()) Config.SESSION_TYPE_APPLICATION else applicationContext.getSessionType()
    }

    // this is just a wrapper method for ACF
    @Throws(PageException::class)
    fun SymTab_findBuiltinScope(name: String?): Scope? {
        return scope(name, null)
    }

    @Override
    @Throws(PageException::class)
    fun getDataSource(datasource: String?): DataSource? {
        var ds: DataSource? = if (getApplicationContext() == null) null else getApplicationContext().getDataSource(datasource, null)
        if (ds != null) return ds
        ds = getConfig().getDataSource(datasource, null)
        if (ds != null) return ds
        throw DatabaseException.notFoundException(this, datasource)
    }

    @Override
    fun getDataSource(datasource: String?, defaultValue: DataSource?): DataSource? {
        var ds: DataSource? = if (getApplicationContext() == null) null else getApplicationContext().getDataSource(datasource, null)
        if (ds == null) ds = getConfig().getDataSource(datasource, defaultValue)
        return ds
    }

    fun getCacheConnection(cacheName: String?, defaultValue: CacheConnection?): CacheConnection? {
        var cacheName = cacheName
        cacheName = cacheName.toLowerCase().trim()
        var cc: CacheConnection? = null
        if (getApplicationContext() != null) cc = (getApplicationContext() as ApplicationContextSupport?).getCacheConnection(cacheName, null)
        if (cc == null) cc = config.getCacheConnections().get(cacheName)
        return if (cc == null) defaultValue else cc
    }

    @Throws(CacheException::class)
    fun getCacheConnection(cacheName: String?): CacheConnection? {
        var cacheName = cacheName
        cacheName = cacheName.toLowerCase().trim()
        var cc: CacheConnection? = null
        if (getApplicationContext() != null) cc = (getApplicationContext() as ApplicationContextSupport?).getCacheConnection(cacheName, null)
        if (cc == null) cc = config.getCacheConnections().get(cacheName)
        if (cc == null) throw CacheUtil.noCache(config, cacheName)
        return cc
    }

    fun setActiveQuery(activeQuery: ActiveQuery?) {
        activeQueries.add(activeQuery)
    }

    fun getActiveQueries(): Array<ActiveQuery?>? {
        return activeQueries.toArray(arrayOfNulls<ActiveQuery?>(activeQueries.size()))
    }

    fun releaseActiveQuery(): ActiveQuery? {
        return activeQueries.pop()
    }

    fun setActiveLock(activeLock: ActiveLock?) {
        activeLocks.add(activeLock)
    }

    fun getActiveLocks(): Array<ActiveLock?>? {
        return activeLocks.toArray(arrayOfNulls<ActiveLock?>(activeLocks.size()))
    }

    fun releaseActiveLock(): ActiveLock? {
        return activeLocks.pop()
    }

    fun getPageException(): PageException? {
        return pe
    }

    @Override
    fun getResourceCharset(): Charset? {
        val cs: Charset? = if (getApplicationContext() == null) null else getApplicationContext().getResourceCharset()
        return if (cs != null) cs else config.getResourceCharset()
    }

    @Override
    fun getWebCharset(): Charset? {
        val cs: Charset? = if (getApplicationContext() == null) null else getApplicationContext().getWebCharset()
        return if (cs != null) cs else config.getWebCharset()
    }

    fun getScopeCascadingType(): Short {
        return if (applicationContext == null) config.getScopeCascadingType() else applicationContext.getScopeCascading()
    }

    fun getTypeChecking(): Boolean {
        return if (applicationContext == null) config.getTypeChecking() else applicationContext.getTypeChecking()
    }

    fun getAllowCompression(): Boolean {
        return if (applicationContext == null) config.allowCompression() else applicationContext.getAllowCompression()
    }

    fun getSuppressContent(): Boolean {
        return if (applicationContext == null) config.isSuppressContent() else applicationContext.getSuppressContent()
    }

    @Override
    fun getCachedWithin(type: Int): Object? {
        return if (applicationContext == null) config.getCachedWithin(type) else applicationContext.getCachedWithin(type)
    }

    // FUTURE add to interface
    fun getMailServers(): Array<lucee.runtime.net.mail.Server?>? {
        if (applicationContext != null) {
            val appms: Array<lucee.runtime.net.mail.Server?> = applicationContext.getMailServers()
            if (ArrayUtil.isEmpty(appms)) return config.getMailServers()
            val cms: Array<lucee.runtime.net.mail.Server?> = config.getMailServers()
            return if (ArrayUtil.isEmpty(cms)) appms else ServerImpl.merge(appms, cms)
        }
        return config.getMailServers()
    }

    // FUTURE add to interface
    fun getFullNullSupport(): Boolean {
        return fullNullSupport
    }

    private fun setFullNullSupport() {
        fullNullSupport = currentTemplateDialect != CFMLEngine.DIALECT_CFML || applicationContext != null && applicationContext.getFullNullSupport()
    }

    fun registerLazyStatement(s: Statement?) {
        if (lazyStats == null) lazyStats = ArrayList<Statement?>()
        lazyStats.add(s)
    }

    @Override
    fun getCurrentTemplateDialect(): Int {
        return currentTemplateDialect
    }

    @Override
    fun getRequestDialect(): Int {
        return requestDialect
    }

    @Override
    @Throws(ServletException::class, IOException::class)
    fun include(realPath: String?, flush: Boolean) {
        include(realPath)
        if (flush) flush()
    }

    @Override
    fun getExpressionEvaluator(): ExpressionEvaluator? {
        throw RuntimeException("not supported!")
    }

    @Override
    fun getVariableResolver(): VariableResolver? {
        throw RuntimeException("not supported!")
    }

    @Override
    fun getELContext(): ELContext? {
        throw RuntimeException("not supported!")
    }

    @Override
    fun ignoreScopes(): Boolean {
        return ignoreScopes
    }

    fun setIgnoreScopes(ignoreScopes: Boolean) {
        this.ignoreScopes = ignoreScopes
    }

    fun setAppListenerType(appListenerType: Int) {
        this.appListenerType = appListenerType
    }

    fun getAppListenerType(): Int {
        return appListenerType
    }

    fun getLog(name: String?): Log? {
        if (applicationContext != null) {
            var log: Log? = null
            try {
                log = applicationContext.getLog(name)
            } catch (e: PageException) {
                config.getLog("application").error(getClass().getName(), e)
            }
            if (log != null) return log
        }
        return config.getLog(name)
    }

    @Throws(PageException::class)
    fun getLog(name: String?, createIfNecessary: Boolean): Log? {
        if (applicationContext != null) {
            val log: Log = applicationContext.getLog(name)
            if (log != null) return log
        }
        return config.getLog(name, createIfNecessary)
    }

    @Throws(PageException::class)
    fun getLogNames(): Collection<String?>? {
        val cnames: Collection<String?> = config.getLoggers().keySet()
        if (applicationContext != null) {
            val anames: Collection<Collection.Key?> = applicationContext.getLogNames()
            val names: MutableCollection<String?> = HashSet<String?>()
            copy(cnames, names)
            copy(anames, names)
            return names
        }
        return cnames
    }

    private fun copy(src: Collection<*>?, trg: MutableCollection<String?>?) {
        val it = src!!.iterator()
        while (it.hasNext()) {
            trg!!.add(it.next().toString())
        }
    }

    fun setTimestampWithTSOffset(literalTimestampWithTSOffset: Boolean) {
        this.literalTimestampWithTSOffset = literalTimestampWithTSOffset
    }

    fun getTimestampWithTSOffset(): Boolean {
        return literalTimestampWithTSOffset
    }

    fun setTagName(tagName: String?) {
        this.tagName = tagName
    }

    fun getTagName(): String? {
        return tagName
    }

    fun getParentTagNames(): List<String?>? {
        return parentTags
    }

    fun addParentTag(tagName: String?) {
        if (!StringUtil.isEmpty(tagName)) {
            if (parentTags == null) parentTags = ArrayList<String?>()
            parentTags.add(tagName)
        }
    }

    fun isDummy(): Boolean {
        return dummy
    }

    fun setDummy(dummy: Boolean): PageContextImpl? {
        this.dummy = dummy
        return this
    }

    fun getCachedAfterTimeRange(): TimeSpan? { // FUTURE add to interface
        return if (applicationContext != null) {
            applicationContext.getQueryCachedAfter()
        } else config.getCachedAfterTimeRange()
    }

    fun getProxyData(): ProxyData? {
        if (applicationContext != null) {
            val pd: ProxyData = applicationContext.getProxyData()
            if (pd != null) return pd
        }
        // TODO check application context
        return config.getProxyData()
    }

    fun setListenSettings(listenSettings: Boolean) {
        this.listenSettings = listenSettings
    }

    fun getListenSettings(): Boolean {
        return listenSettings
    }

    fun allowImplicidQueryCall(): Boolean {
        return if (applicationContext != null) applicationContext.getAllowImplicidQueryCall() else config.allowImplicidQueryCall()
    }

    fun getRegex(): Regex? {
        return if (applicationContext != null) applicationContext.getRegex() else config.getRegex()
    }

    companion object {
        private val DUMMY_BOOL: RefBoolean? = RefBooleanImpl(false)
        private var counter = 0
        private val localUnsupportedScope: LocalNotSupportedScope? = LocalNotSupportedScope.getInstance()
        private val READ_CFID_FROM_URL: Boolean = Caster.toBooleanValue(SystemUtil.getSystemPropOrEnvVar("lucee.read.cfid.from.url", "true"), true)
        private var _idCounter = 1
        @Throws(ApplicationException::class)
        fun notSupported(config: Config?, ps: PageSource?) {
            if (ps.getDialect() === CFMLEngine.DIALECT_LUCEE && config is ConfigPro && !(config as ConfigPro?).allowLuceeDialect()) notSupported()
        }

        @Throws(ApplicationException::class)
        fun notSupported() {
            throw ApplicationException(
                    "The Lucee dialect is disabled, to enable the dialect set the environment variable or system property \"lucee.enable.dialect\" to \"true\" or set the attribute \"allow-lucee-dialect\" to \"true\" with the \"compiler\" tag inside the lucee-server.xml.")
        }

        private fun repl(haystack: String?, needle: String?, replacement: String?): String? {
            var haystack = haystack
            val regex = StringBuilder("#[\\s]*error[\\s]*\\.[\\s]*")
            val carr: CharArray = needle.toCharArray()
            for (i in carr.indices) {
                regex.append("[")
                regex.append(Character.toLowerCase(carr[i]))
                regex.append(Character.toUpperCase(carr[i]))
                regex.append("]")
            }
            regex.append("[\\s]*#")
            // print.o(regex);
            haystack = haystack.replaceAll(regex.toString(), replacement)
            // print.o(haystack);
            return haystack
        }

        @Synchronized
        private fun getIdCounter(): Int {
            _idCounter++
            if (_idCounter < 0) _idCounter = 1
            return _idCounter
        }
    }

    /**
     * default Constructor
     *
     * @param scopeContext
     * @param config Configuration of the CFML Container
     * @param queryCache Query Cache Object
     * @param id identity of the pageContext
     * @param servlet
     */
    init {
        // must be first because is used after
        tagHandlerPool = config.getTagHandlerPool()
        this.servlet = servlet
        bodyContentStack = BodyContentStack()
        devNull = bodyContentStack.getDevNullBodyContent()
        this.config = config
        manager = DatasourceManagerImpl(config)
        this.scopeContext = scopeContext
        undefined = UndefinedImpl(this, getScopeCascadingType())
        server = ScopeContext.getServerScope(this, jsr223)
        defaultApplicationContext = ClassicApplicationContext(config, "", true, null)
        id = getIdCounter()
    }
}