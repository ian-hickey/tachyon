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
package tachyon.runtime

import java.io.PrintWriter

/**
 * implements a JSP Factory, this class produces JSP compatible PageContext objects, as well as the
 * required ColdFusion specified interfaces
 */
class CFMLFactoryImpl(engine: CFMLEngineImpl?, sg: ServletConfig?) : CFMLFactory() {
    private var config: ConfigWebPro? = null
    var pcs: ConcurrentLinkedDeque<PageContextImpl?>? = ConcurrentLinkedDeque<PageContextImpl?>()
    private val runningPcs: Map<Integer?, PageContextImpl?>? = ConcurrentHashMap<Integer?, PageContextImpl?>()
    private val runningChildPcs: Map<Integer?, PageContextImpl?>? = ConcurrentHashMap<Integer?, PageContextImpl?>()
    private val scopeContext: ScopeContext? = ScopeContext(this)
    private var _servlet: HttpServlet? = null
    private var url: URL? = null
    private val engine: CFMLEngineImpl?
    private var cfmlExtensions: ArrayList<String?>? = null
    private var tachyonExtensions: ArrayList<String?>? = null
    private val servletConfig: ServletConfig?
    private val memoryThreshold: Float
    private val cpuThreshold: Float
    private val concurrentReqThreshold: Int

    /**
     * reset the PageContexes
     */
    @Override
    fun resetPageContext() {
        LogUtil.log(config, Log.LEVEL_INFO, CFMLFactoryImpl::class.java.getName(), "Reset " + pcs.size().toString() + " Unused PageContexts")
        pcs.clear()
        val it: Iterator<PageContextImpl?> = runningPcs!!.values().iterator()
        while (it.hasNext()) {
            it.next()!!.reset()
        }
    }

    @Override
    fun getPageContext(servlet: Servlet?, req: ServletRequest?, rsp: ServletResponse?, errorPageURL: String?, needsSession: Boolean, bufferSize: Int,
                       autoflush: Boolean): javax.servlet.jsp.PageContext? {
        return getPageContextImpl(servlet as HttpServlet?, req as HttpServletRequest?, rsp as HttpServletResponse?, errorPageURL, needsSession, bufferSize, autoflush, true, false, -1,
                true, false, false, null)
    }

    @Override
    @Deprecated
    fun getTachyonPageContext(servlet: HttpServlet?, req: HttpServletRequest?, rsp: HttpServletResponse?, errorPageURL: String?, needsSession: Boolean, bufferSize: Int,
                            autoflush: Boolean): PageContext? {
        // runningCount++;
        return getPageContextImpl(servlet, req, rsp, errorPageURL, needsSession, bufferSize, autoflush, true, false, -1, true, false, false, null)
    }

    @Override
    fun getTachyonPageContext(servlet: HttpServlet?, req: HttpServletRequest?, rsp: HttpServletResponse?, errorPageURL: String?, needsSession: Boolean, bufferSize: Int,
                            autoflush: Boolean, register: Boolean, timeout: Long, register2RunningThreads: Boolean, ignoreScopes: Boolean): PageContext? {
        // runningCount++;
        return getPageContextImpl(servlet, req, rsp, errorPageURL, needsSession, bufferSize, autoflush, register, false, timeout, register2RunningThreads, ignoreScopes, false,
                null)
    }

    fun getPageContextImpl(servlet: HttpServlet?, req: HttpServletRequest?, rsp: HttpServletResponse?, errorPageURL: String?, needsSession: Boolean, bufferSize: Int,
                           autoflush: Boolean, register2Thread: Boolean, isChild: Boolean, timeout: Long, register2RunningThreads: Boolean, ignoreScopes: Boolean, createNew: Boolean,
                           tmplPC: PageContextImpl?): PageContextImpl? {
        if (!isChild) {
            val ra: String = req.getRemoteAddr()
            var tmp: String
            if (ra != null) {
                var resetToNormPrio = true
                var count = 0
                for (opc in runningPcs!!.values()) {
                    if (opc != null) {
                        val tmpReq: HttpServletRequest = opc.getHttpServletRequest()
                        if (tmpReq != null) {
                            tmp = tmpReq.getRemoteAddr()
                            if (ra.equals(tmp)) count++
                        }
                    }
                }
                // has already running requests?
                if (count > 0) {

                    // reached max amount of request for norm prio
                    val maxNormPrio: Int = Caster.toIntValue(SystemUtil.getSystemPropOrEnvVar("tachyon.request.limit.concurrent.maxnormprio", null), MAX_NORMAL_PRIORITY)
                    if (maxNormPrio > 0 && count >= maxNormPrio) {
                        for (opc in runningPcs.values()) {
                            if (opc != null) {
                                val t: Thread = opc.getThread()
                                if (t != null) {
                                    t.setPriority(Thread.MIN_PRIORITY)
                                }
                            }
                        }
                        Thread.currentThread().setPriority(Thread.MIN_PRIORITY)
                        resetToNormPrio = false
                    }

                    // reached max amount of request allowed in without a nap
                    val maxNoSleep: Int = Caster.toIntValue(SystemUtil.getSystemPropOrEnvVar("tachyon.request.limit.concurrent.maxnosleep", null), MAX_NO_SLEEP)
                    if (maxNoSleep > 0 && count >= maxNoSleep) {
                        val ms: Int = Caster.toIntValue(SystemUtil.getSystemPropOrEnvVar("tachyon.request.limit.concurrent.sleeptime", null), SLEEP_TIME)
                        if (ms > 0) {
                            SystemUtil.sleep(ms)
                        }
                    }
                }
                if (resetToNormPrio && Thread.currentThread().getPriority() !== Thread.NORM_PRIORITY) Thread.currentThread().setPriority(Thread.NORM_PRIORITY)
            }
        }
        var pc: PageContextImpl?
        pc = if (createNew || pcs.isEmpty()) {
            null
        } else {
            try {
                pcs.pop()
            } catch (nsee: NoSuchElementException) {
                null
            }
        }
        if (pc == null) pc = PageContextImpl(scopeContext, config, servlet, ignoreScopes)
        if (timeout > 0) pc.setRequestTimeout(timeout)
        if (register2RunningThreads) {
            runningPcs.put(Integer.valueOf(pc.getId()), pc)
            if (isChild) runningChildPcs.put(Integer.valueOf(pc.getId()), pc)
        }
        _servlet = servlet
        if (register2Thread) ThreadLocalPageContext.register(pc)
        pc.initialize(servlet, req, rsp, errorPageURL, needsSession, bufferSize, autoflush, isChild, ignoreScopes, tmplPC)
        return pc
    }

    @Override
    fun releasePageContext(pc: javax.servlet.jsp.PageContext?) {
        releaseTachyonPageContext(pc as PageContext?, true)
    }

    @Override
    fun getEngine(): CFMLEngine? {
        return engine
    }

    @Override
    @Deprecated
    fun releaseTachyonPageContext(pc: PageContext?) {
        releaseTachyonPageContext(pc, true)
    }

    /**
     * Similar to the releasePageContext Method, but take tachyon PageContext as entry
     *
     * @param pc
     */
    @Override
    fun releaseTachyonPageContext(pc: PageContext?, unregisterFromThread: Boolean) {
        if (pc.getId() < 0) return
        val isChild = pc.getParentPageContext() != null // we need to get this check before release is executed

        // when pc was registered with an other thread, we register with this thread when calling release
        val beforePC: PageContext = ThreadLocalPageContext.get()
        var tmpRegister = false
        if (beforePC !== pc) {
            ThreadLocalPageContext.register(pc)
            tmpRegister = true
        }
        var reuse = true
        try {
            reuse = !pc.hasFamily() // we do not recycle when still referenced by child threads
            pc.release()
        } catch (e: Exception) {
            reuse = false
            ThreadLocalPageContext.getLog(config, "application").error("release page context", e)
        }
        if (tmpRegister) ThreadLocalPageContext.register(beforePC)
        if (unregisterFromThread) ThreadLocalPageContext.release()
        runningPcs.remove(Integer.valueOf(pc.getId()))
        if (isChild) {
            runningChildPcs.remove(Integer.valueOf(pc.getId()))
        }
        if (pcs.size() < 100 && (pc as PageContextImpl?)!!.getTimeoutStackTrace() == null && reuse) // not more than 100 PCs
            pcs.push(pc as PageContextImpl?)
        if (runningPcs!!.size() > MAX_SIZE) clean(runningPcs)
        if (runningChildPcs!!.size() > MAX_SIZE) clean(runningChildPcs)
    }

    private fun clean(map: Map<Integer?, PageContextImpl?>?) {
        val it: Iterator<PageContextImpl?> = map!!.values().iterator()
        var pci: PageContextImpl?
        val now: Long = System.currentTimeMillis()
        while (it.hasNext()) {
            pci = it.next()
            if (pci!!.isGatewayContext() || pci!!.getStartTime() + MAX_AGE > now) continue
        }
    }

    /**
     * check timeout of all running threads, downgrade also priority from all thread run longer than 10
     * seconds
     */
    @Override
    fun checkTimeout() {
        if (!engine.allowRequestTimeout()) return

        // print.e(MonitorState.checkForBlockedThreads(runningPcs.values()));
        // print.e(MonitorState.checkForBlockedThreads(runningChildPcs.values()));

        // synchronized (runningPcs) {
        // int len=runningPcs.size();
        // we only terminate child threads
        val map: Map<Integer?, PageContextImpl?>? = if (engine.exeRequestAsync()) runningChildPcs else runningPcs
        run {
            val it: Iterator<Entry<Integer?, PageContextImpl?>?> = map.entrySet().iterator()
            var pc: PageContextImpl
            var e: Entry<Integer?, PageContextImpl?>?
            while (it.hasNext()) {
                e = it.next()
                pc = e.getValue()
                val timeout: Long = pc.getRequestTimeout()
                // reached timeout
                if (pc.getStartTime() + timeout < System.currentTimeMillis() && Long.MAX_VALUE !== timeout) {
                    val log: Log = ThreadLocalPageContext.getLog(pc, "requesttimeout")
                    if (reachedConcurrentReqThreshold() && reachedMemoryThreshold() && reachedCPUThreshold()) {
                        if (log != null) {
                            val root: PageContext = pc.getRootPageContext()
                            log.log(Log.LEVEL_ERROR, "controller",
                                    "stop " + (if (root != null && root !== pc) "thread" else "request") + " (" + pc.getId() + ") because run into a timeout. ATM we have "
                                            + getActiveRequests() + " active request(s) and " + getActiveThreads() + " active cfthreads " + getPath(pc) + "."
                                            + MonitorState.getBlockedThreads(pc) + RequestTimeoutException.locks(pc),
                                    ExceptionUtil.toThrowable(pc.getThread().getStackTrace()))
                        }
                        terminate(pc, true)
                        runningPcs.remove(Integer.valueOf(pc.getId()))
                        it.remove()
                    } else {
                        if (log != null) {
                            val root: PageContext = pc.getRootPageContext()
                            log.log(Log.LEVEL_WARN, "controller", "reach request timeout with " + (if (root != null && root !== pc) "thread" else "request") + " [" + pc.getId()
                                    + "], but the request is not killed because we did not reach all thresholds set. ATM we have " + getActiveRequests() + " active request(s) and "
                                    + getActiveThreads() + " active cfthreads " + getPath(pc) + "." + MonitorState.getBlockedThreads(pc) + RequestTimeoutException.locks(pc),
                                    ExceptionUtil.toThrowable(pc.getThread().getStackTrace()))
                        }
                    }
                } else if (pc.getStartTime() + 10000 < System.currentTimeMillis() && pc.getThread().getPriority() !== Thread.MIN_PRIORITY) {
                    val log: Log = ThreadLocalPageContext.getLog(pc, "requesttimeout")
                    if (log != null) {
                        val root: PageContext = pc.getRootPageContext()
                        log.log(Log.LEVEL_INFO, "controller", "downgrade priority of the a " + (if (root != null && root !== pc) "thread" else "request") + " at " + getPath(pc) + ". "
                                + MonitorState.getBlockedThreads(pc) + RequestTimeoutException.locks(pc), ExceptionUtil.toThrowable(pc.getThread().getStackTrace()))
                    }
                    try {
                        pc.getThread().setPriority(Thread.MIN_PRIORITY)
                    } catch (t: Throwable) {
                        ExceptionUtil.rethrowIfNecessary(t)
                    }
                }
            }
        }
    }

    fun reachedConcurrentReqThreshold(): Boolean {
        return if (concurrentReqThreshold == 0) true else concurrentReqThreshold <= runningPcs!!.size()
    }

    fun reachedMemoryThreshold(): Boolean {
        return if (memoryThreshold == 0f) true else memoryThreshold <= SystemUtil.getMemoryPercentage()
    }

    fun reachedCPUThreshold(): Boolean {
        return if (cpuThreshold == 0f) true else cpuThreshold <= SystemUtil.getCpuPercentage()
    }

    @Override
    fun getEngineInfo(): JspEngineInfo? {
        return info
    }

    /**
     * @return returns count of pagecontext in use
     */
    @Override
    fun getUsedPageContextLength(): Int {
        var length = 0
        try {
            val it: Iterator<PageContextImpl?> = runningPcs!!.values().iterator()
            while (it.hasNext()) {
                val pc: PageContextImpl? = it.next()
                if (!pc!!.isGatewayContext()) length++
            }
        } catch (t: Throwable) {
            ExceptionUtil.rethrowIfNecessary(t)
            return length
        }
        return length
    }

    /**
     * @return Returns the config.
     */
    @Override
    fun getConfig(): ConfigWeb? {
        return config
    }

    /**
     * @return Returns the scopeContext.
     */
    fun getScopeContext(): ScopeContext? {
        return scopeContext
    }

    /**
     * @return label of the factory
     */
    @Override
    fun getLabel(): Object? {
        return getConfig().getLabel()
    }

    /**
     * @param label
     */
    @Override
    fun setLabel(label: String?) {
        // deprecated
    }

    @Override
    fun getURL(): URL? {
        return url
    }

    fun setURL(url: URL?) {
        this.url = url
    }

    /**
     * @return the servlet
     */
    @Override
    fun getServlet(): HttpServlet? {
        if (_servlet == null) _servlet = HTTPServletImpl(servletConfig, servletConfig.getServletContext(), servletConfig.getServletName())
        return _servlet
    }

    fun setConfig(config: ConfigWebPro?) {
        this.config = config
    }

    fun getActivePageContexts(): Map<Integer?, PageContextImpl?>? {
        return runningPcs
    }

    fun getPageContextsSize(): Long {
        return SizeOf.size(pcs)
    }

    fun getActiveRequests(): Long {
        return runningPcs!!.size()
    }

    fun getActiveThreads(): Long {
        return runningChildPcs!!.size()
    }

    fun getInfo(): Array? {
        val info: Array = ArrayImpl()

        // synchronized (runningPcs) {
        // int len=runningPcs.size();
        val it: Iterator<PageContextImpl?> = runningPcs!!.values().iterator()
        var pc: PageContextImpl?
        var data: Struct?
        var sctThread: Struct?
        var scopes: Struct?
        var thread: Thread
        var e: Entry<Integer?, PageContextImpl?>
        var cw: ConfigWebPro
        while (it.hasNext()) {
            pc = it.next()
            cw = pc!!.getConfig() as ConfigWebPro
            data = StructImpl()
            sctThread = StructImpl()
            scopes = StructImpl()
            data.setEL("thread", sctThread)
            data.setEL("scopes", scopes)
            if (pc!!.isGatewayContext()) continue
            thread = pc!!.getThread()
            if (thread === Thread.currentThread()) continue
            thread = pc!!.getThread()
            if (thread === Thread.currentThread()) continue
            data.setEL("startTime", DateTimeImpl(pc!!.getStartTime(), false))
            data.setEL("endTime", DateTimeImpl(pc!!.getStartTime() + pc!!.getRequestTimeout(), false))
            data.setEL(KeyConstants._timeout, Double.valueOf(pc!!.getRequestTimeout()))

            // thread
            sctThread.setEL(KeyConstants._name, thread.getName())
            sctThread.setEL(KeyConstants._priority, Caster.toDouble(thread.getPriority()))
            sctThread.setEL(KeyConstants._state, thread.getState().name())
            val stes: Array<StackTraceElement?> = thread.getStackTrace()
            data.setEL("TagContext", PageExceptionImpl.getTagContext(pc!!.getConfig(), stes))

            // Java Stacktrace
            val sw = StringWriter()
            val pw = PrintWriter(sw)
            val t = Throwable()
            t.setStackTrace(stes)
            t.printStackTrace(pw)
            pw.close()
            data.setEL("JavaStackTrace", sw.toString())
            data.setEL(KeyConstants._urltoken, pc!!.getURLToken())
            try {
                if (pc!!.getConfig().debug()) data.setEL("debugger", pc!!.getDebugger().getDebuggingData(pc))
            } catch (e2: PageException) {
            }
            try {
                data.setEL(KeyConstants._id, Hash.call(pc, pc!!.getId().toString() + ":" + pc!!.getStartTime()))
            } catch (e1: PageException) {
            }
            data.setEL(KeyConstants._hash, cw.getHash())
            data.setEL("contextId", cw.getIdentification().getId())
            data.setEL(KeyConstants._label, cw.getLabel())
            data.setEL("requestId", pc!!.getId())

            // Scopes
            scopes.setEL(KeyConstants._name, pc!!.getApplicationContext().getName())
            try {
                scopes.setEL(KeyConstants._application, pc!!.applicationScope())
            } catch (pe: PageException) {
            }
            try {
                scopes.setEL(KeyConstants._session, pc!!.sessionScope())
            } catch (pe: PageException) {
            }
            try {
                scopes.setEL(KeyConstants._client, pc!!.clientScope())
            } catch (pe: PageException) {
            }
            scopes.setEL(KeyConstants._cookie, pc!!.cookieScope())
            scopes.setEL(KeyConstants._variables, pc!!.variablesScope())
            if (pc!!.localScope() !is LocalNotSupportedScope) {
                scopes.setEL(KeyConstants._local, pc!!.localScope())
                scopes.setEL(KeyConstants._arguments, pc!!.argumentsScope())
            }
            scopes.setEL(KeyConstants._cgi, pc!!.cgiScope())
            scopes.setEL(KeyConstants._form, pc!!.formScope())
            scopes.setEL(KeyConstants._url, pc!!.urlScope())
            scopes.setEL(KeyConstants._request, pc!!.requestScope())
            info.appendEL(data)
        }
        return info
        // }
    }

    fun stopThread(threadId: String?, stopType: String?) {
        // synchronized (runningPcs) {
        var stopType = stopType
        val it: Iterator<PageContextImpl?> = runningPcs!!.values().iterator()
        var pc: PageContext?
        while (it.hasNext()) {
            pc = it.next()
            try {
                val id: String = Hash.call(pc, pc.getId().toString() + ":" + pc.getStartTime())
                if (id.equals(threadId)) {
                    stopType = stopType.trim()
                    // Throwable t;
                    if ("abort".equalsIgnoreCase(stopType) || "cfabort".equalsIgnoreCase(stopType)) throw RuntimeException("type [$stopType] is no longer supported")
                    // t=new Abort(Abort.SCOPE_REQUEST);
                    // else t=new RequestTimeoutException(pc.getThread(),"request has been forced to stop.");
                    SystemUtil.stop(pc, true)
                    SystemUtil.sleep(10)
                    break
                }
            } catch (e1: PageException) {
            }
        }
        // }
    }

    @Override
    fun getJspApplicationContext(arg0: ServletContext?): JspApplicationContext? {
        throw RuntimeException("not supported!")
    }

    @Override
    fun toDialect(ext: String?): Int {
        // MUST improve perfomance
        if (cfmlExtensions == null) _initExtensions()
        return if (cfmlExtensions.contains(ext.toLowerCase())) CFMLEngine.DIALECT_CFML else CFMLEngine.DIALECT_CFML
    }

    // FUTURE add to loader
    fun toDialect(ext: String?, defaultValue: Int): Int {
        var ext: String? = ext ?: return defaultValue
        if (cfmlExtensions == null) _initExtensions()
        if (cfmlExtensions.contains(ext.toLowerCase().also { ext = it })) return CFMLEngine.DIALECT_CFML
        return if (tachyonExtensions.contains(ext)) CFMLEngine.DIALECT_LUCEE else defaultValue
    }

    private fun _initExtensions() {
        cfmlExtensions = ArrayList<String?>()
        tachyonExtensions = ArrayList<String?>()
        try {
            val it: Iterator<*> = getServlet().getServletContext().getServletRegistrations().entrySet().iterator()
            var e: Entry<String?, out ServletRegistration?>?
            var cn: String
            while (it.hasNext()) {
                e = it.next() as Entry<String?, out ServletRegistration?>?
                cn = e.getValue().getClassName()
                if (cn != null && cn.indexOf("TachyonServlet") !== -1) {
                    setExtensions(tachyonExtensions, e.getValue().getMappings().iterator())
                } else if (cn != null && cn.indexOf("CFMLServlet") !== -1) {
                    setExtensions(cfmlExtensions, e.getValue().getMappings().iterator())
                }
            }
        } catch (t: Throwable) {
            ExceptionUtil.rethrowIfNecessary(t)
            ArrayUtil.addAll(cfmlExtensions, Constants.getCFMLExtensions())
            ArrayUtil.addAll(tachyonExtensions, Constants.getTachyonExtensions())
        }
    }

    private fun setExtensions(extensions: ArrayList<String?>?, it: Iterator<String?>?) {
        var str: String?
        var str2: String
        var it2: Iterator<String?>
        while (it!!.hasNext()) {
            str = it.next()
            it2 = ListUtil.listToSet(str, ',', true).iterator()
            while (it2.hasNext()) {
                str2 = it2.next()
                extensions.add(str2.substring(2)) // MUSTMUST better impl
            }
        }
    }

    @Override
    fun getCFMLExtensions(): Iterator<String?>? {
        if (cfmlExtensions == null) _initExtensions()
        return cfmlExtensions.iterator()
    }

    @Override
    fun getTachyonExtensions(): Iterator<String?>? {
        if (tachyonExtensions == null) _initExtensions()
        return tachyonExtensions.iterator()
    }

    companion object {
        private const val MAX_NORMAL_PRIORITY = 0
        private const val MAX_NO_SLEEP = 10
        private const val SLEEP_TIME = 100
        private const val MAX_AGE = (5 * 60000 // 5 minutes
                ).toLong()
        private const val MAX_SIZE = 10000
        private val info: JspEngineInfo? = JspEngineInfoImpl("1.0")
        private fun getSystemPropOrEnvVarAsFloat(name: String?): Float {
            var str: String = SystemUtil.getSystemPropOrEnvVar(name, null)
            if (StringUtil.isEmpty(str)) return 0f
            str = StringUtil.unwrap(str)
            if (StringUtil.isEmpty(str)) return 0f
            val res: Float = Caster.toFloatValue(str, 0f)
            if (res < 0f) return 0f
            return if (res > 1f) 1f else res
        }

        private fun getSystemPropOrEnvVarAsInt(name: String?): Int {
            var str: String = SystemUtil.getSystemPropOrEnvVar(name, null)
            if (StringUtil.isEmpty(str)) return 0
            str = StringUtil.unwrap(str)
            if (StringUtil.isEmpty(str)) return 0
            val res: Int = Caster.toIntValue(str, 0)
            return if (res < 0) 0 else res
        }

        fun terminate(pc: PageContextImpl?, async: Boolean) {
            pc!!.getConfig().getThreadQueue().exit(pc)
            SystemUtil.stop(pc, async)
        }

        private fun getPath(pc: PageContext?): String? {
            return try {
                val base: String = ResourceUtil.getResource(pc, pc.getBasePageSource()).getAbsolutePath()
                val current: String = ResourceUtil.getResource(pc, pc.getCurrentPageSource()).getAbsolutePath()
                if (base.equals(current)) "path: $base" else "path: $base ($current)"
            } catch (npe: NullPointerException) {
                "(no path available)"
            } catch (t: Throwable) {
                ExceptionUtil.rethrowIfNecessary(t)
                "(fail to retrieve path:" + t.getClass().getName().toString() + ":" + t.getMessage().toString() + ")"
            }
        }

        fun createRequestTimeoutException(pc: PageContext?): RequestTimeoutException? {
            return RequestTimeoutException(pc, pc.getThread().getStackTrace())
        }
    }

    init {
        this.engine = engine
        servletConfig = sg
        memoryThreshold = getSystemPropOrEnvVarAsFloat("tachyon.requesttimeout.memorythreshold")
        cpuThreshold = getSystemPropOrEnvVarAsFloat("tachyon.requesttimeout.cputhreshold")
        concurrentReqThreshold = getSystemPropOrEnvVarAsInt("tachyon.requesttimeout.concurrentrequestthreshold")
    }
}