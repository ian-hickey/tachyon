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
package lucee.runtime.thread

import java.io.ByteArrayOutputStream

class ChildThreadImpl(parent: PageContextImpl?, page: Page?, // PageContextImpl pc =null;
                      private val tagName: String?, // private static final Set EMPTY = new HashSet();
                      private val threadIndex: Int, attrs: Struct?, private val serializable: Boolean) : ChildThread(), Serializable {
    private var pc: PageContextImpl? = null
    private val start: Long
    private var endTime: Long = 0
    private val scope: Threads? = null

    // accesible from scope
    var content: Struct? = StructImpl()
    var catchBlock: Struct? = null
    var terminated = false
    var completed = false
    var output: ByteArrayOutputStream? = null

    // only used for type daemon
    private val page: Page? = null

    // only used for type task, demon attrs are not Serializable
    private val attrs: Struct? = null
    private val cookies: Array<SerializableCookie?>?
    private var serverName: String? = null
    private var queryString: String? = null
    private val parameters: Array<Pair<String?, String?>?>?
    private var requestURI: String? = null
    private val headers: Array<Pair<String?, String?>?>?
    private var attributes: Struct? = null
    private var template: String? = null
    private var requestTimeout: Long = 0
    var contentType: String? = null
    var contentEncoding: String? = null
    private var threadScope: Object? = null
    @Override
    fun run() {
        execute(null)
    }

    fun execute(config: Config?): PageException? {
        val oldPc: PageContext = ThreadLocalPageContext.get()
        var p: Page? = page
        var pc: PageContextImpl? = null
        var debugEntry: DebugEntryTemplate? = null
        val time: Long = System.nanoTime()
        try {
            // daemon
            if (this.pc != null) {
                pc = this.pc
                ThreadLocalPageContext.register(pc)
            } else {
                val cwi: ConfigWebPro?
                try {
                    cwi = config as ConfigWebPro?
                    val os: DevNullOutputStream = DevNullOutputStream.DEV_NULL_OUTPUT_STREAM
                    val session: HttpSession? = if (oldPc != null && oldPc.getSessionType() === Config.SESSION_TYPE_JEE) oldPc.getSession() else null
                    pc = ThreadUtil.createPageContext(cwi, os, serverName, requestURI, queryString, SerializableCookie.toCookies(cookies), headers, null, parameters, attributes,
                            true, -1, session)
                    pc.setRequestTimeout(requestTimeout)
                    p = PageSourceImpl.loadPage(pc, cwi.getPageSources(if (oldPc == null) pc else oldPc, null, template, false, false, true))
                    // p=cwi.getPageSources(oldPc,null, template, false,false,true).loadPage(cwi);
                } catch (e: PageException) {
                    return e
                }
                pc.addPageSource(p.getPageSource(), true)
            }
            val ci: ConfigWebPro = pc.getConfig() as ConfigWebPro
            if (!pc.isGatewayContext() && ci.debug()) {
                (pc.getDebugger() as DebuggerImpl).setThreadName(tagName)
                if (ci.hasDebugOptions(ConfigPro.DEBUG_TEMPLATE)) debugEntry = pc.getDebugger().getEntry(pc, page.getPageSource())
            }
            threadScope = pc.getCFThreadScope()
            pc.setCurrentThreadScope(ThreadsImpl(this))
            pc.setThread(Thread.currentThread())

            // String encodings = pc.getHttpServletRequest().getHeader("Accept-Encoding");
            val undefined: Undefined = pc.us()
            val newArgs: Argument = ArgumentThreadImpl(Duplicator.duplicate(attrs, false) as Struct)
            val newLocal: LocalImpl = pc.getScopeFactory().getLocalInstance()
            // Key[] keys = attrs.keys();
            val it: Iterator<Entry<Key?, Object?>?> = attrs.entryIterator()
            var e: Entry<Key?, Object?>?
            while (it.hasNext()) {
                e = it.next()
                newArgs.setEL(e.getKey(), e.getValue())
            }
            newLocal.setEL(KEY_ATTRIBUTES, newArgs)
            val oldArgs: Argument = pc.argumentsScope()
            val oldLocal: Local = pc.localScope()
            val oldMode: Int = undefined.setMode(Undefined.MODE_LOCAL_OR_ARGUMENTS_ALWAYS)
            pc.setFunctionScopes(newLocal, newArgs)
            try {
                p.threadCall(pc, threadIndex)
            } catch (t: Throwable) {
                ExceptionUtil.rethrowIfNecessary(t)
                if (!Abort.isSilentAbort(t)) {
                    val c: ConfigWeb = pc.getConfig()
                    val log: Log = ThreadLocalPageContext.getLog(c, "thread")
                    if (log != null) log.log(Log.LEVEL_ERROR, this.getName(), t)
                    val pe: PageException = Caster.toPageException(t)
                    // TODO log parent stacktrace as well
                    if (!serializable) catchBlock = pe.getCatchBlock(pc.getConfig())
                    return pe
                }
            } finally {
                completed = true
                pc.setFunctionScopes(oldLocal, oldArgs)
                undefined.setMode(oldMode)
                // pc.getScopeFactory().recycle(newArgs);
                pc.getScopeFactory().recycle(pc, newLocal)
                if (pc.getHttpServletResponse() is HttpServletResponseDummy) {
                    val rsp: HttpServletResponseDummy = pc.getHttpServletResponse() as HttpServletResponseDummy
                    pc.flush()
                    contentType = rsp.getContentType()
                    val _headers: Array<Pair<String?, Object?>?> = rsp.getHeaders()
                    if (_headers != null) for (i in _headers.indices) {
                        if (_headers[i].getName().equalsIgnoreCase("Content-Encoding")) contentEncoding = Caster.toString(_headers[i].getValue(), null)
                    }
                }
            }
        } finally {
            if (debugEntry != null) debugEntry.updateExeTime(System.nanoTime() - time)
            pc.setEndTimeNS(System.nanoTime())
            endTime = System.currentTimeMillis()
            pc.getConfig().getFactory().releaseLuceePageContext(pc, true)
            pc = null
            if (oldPc != null) ThreadLocalPageContext.register(oldPc)
        }
        return null
    }

    @Override
    fun getTagName(): String? {
        return tagName
    }

    @Override
    fun getStartTime(): Long {
        return start
    }

    /*
	 * public Threads getThreadScopeX() { if(scope==null) scope=new ThreadsImpl(this); return scope; }
	 */
    fun getEndTime(): Long {
        return if (endTime == 0L) System.currentTimeMillis() else endTime // endTime = 0 means the thread is still running
    }

    fun getThreads(): Object? {
        return threadScope
    }

    @Override
    fun terminated() {
        terminated = true
    }

    /**
     * @return the pageSource
     */
    fun getTemplate(): String? {
        return template
    }

    companion object {
        private const val serialVersionUID = -8902836175312356628L
        private val KEY_ATTRIBUTES: Collection.Key? = KeyConstants._attributes
    }

    init {
        start = System.currentTimeMillis()
        if (attrs == null) this.attrs = StructImpl() else this.attrs = attrs
        if (!serializable) {
            this.page = page
            if (parent != null) {
                output = ByteArrayOutputStream()
                try {
                    pc = ThreadUtil.clonePageContext(parent, output, false, false, true)
                } catch (e: ConcurrentModificationException) { // MUST search for:hhlhgiug
                    pc = ThreadUtil.clonePageContext(parent, output, false, false, true)
                }
                // tag names
                pc.setTagName(tagName)
                pc.addParentTag(parent.getTagName())
            }
        } else {
            template = page.getPageSource().getRealpathWithVirtual()
            val req: HttpServletRequest = parent.getHttpServletRequest()
            serverName = req.getServerName()
            queryString = ReqRspUtil.getQueryString(req)
            cookies = SerializableCookie.toSerializableCookie(ReqRspUtil.getCookies(req, parent.getWebCharset()))
            parameters = HttpUtil.cloneParameters(req)
            requestURI = req.getRequestURI()
            headers = HttpUtil.cloneHeaders(req)
            attributes = HttpUtil.getAttributesAsStruct(req)
            requestTimeout = parent.getRequestTimeout()
            // MUST here ist sill a mutch state values missing
        }
    }
}