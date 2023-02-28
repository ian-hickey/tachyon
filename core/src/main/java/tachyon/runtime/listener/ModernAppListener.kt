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
package tachyon.runtime.listener

import java.io.IOException

class ModernAppListener : AppListenerSupport() {
    // private Map<String,Component> apps=new HashMap<String,Component>();// TODO no longer use this,
    // find a better way to store components for end methods
    protected var mode: Int = MODE_CURRENT2ROOT
    @Override
    @Throws(PageException::class)
    fun onRequest(pc: PageContext?, requestedPage: PageSource?, rl: RequestListener?) {
        // on requestStart
        val appPS: Page = AppListenerUtil.getApplicationPage(pc, requestedPage,
                if (pc.getRequestDialect() === CFMLEngine.DIALECT_CFML) Constants.CFML_APPLICATION_EVENT_HANDLER else Constants.LUCEE_APPLICATION_EVENT_HANDLER, mode,
                ApplicationListener.TYPE_MODERN)
        _onRequest(pc, requestedPage, appPS, rl)
    }

    @Throws(PageException::class)
    protected fun _onRequest(pc: PageContext?, requestedPage: PageSource?, appP: Page?, rl: RequestListener?) {
        var requestedPage: PageSource? = requestedPage
        val pci: PageContextImpl? = pc as PageContextImpl?
        pci.setAppListenerType(ApplicationListener.TYPE_MODERN)
        if (appP != null) {
            val callPath: String = appP.getPageSource().getComponentName()
            val app: Component = ComponentLoader.loadComponent(pci, appP, callPath, false, false, false, true)

            // init
            val appContext: ModernApplicationContext? = initApplicationContext(pci, app)

            // apps.put(appContext.getName(), app);
            if (!pci.initApplicationContext(this)) return
            if (rl != null) {
                requestedPage = rl.execute(pc, requestedPage)
                if (requestedPage == null) return
            }
            val targetPage: String = requestedPage.getRealpathWithVirtual()
            val goon: RefBoolean = RefBooleanImpl(true)

            // onRequestStart
            if (app.contains(pc, ON_REQUEST_START)) {
                try {
                    val rtn: Object? = call(app, pci, ON_REQUEST_START, arrayOf(targetPage), false)
                    if (!Caster.toBooleanValue(rtn, true)) return
                } catch (pe: PageException) {
                    pe = handlePageException(pci, app, pe, requestedPage, targetPage, goon)
                    if (pe != null) throw pe
                }
            }

            // onRequest
            if (goon.toBooleanValue()) {
                val isComp = isComponent(pc, requestedPage)
                var method: Object?
                if (isComp && app.contains(pc, ON_CFCREQUEST) && pc.urlFormScope().get(KeyConstants._method, null).also { method = it } != null) {
                    val url: Struct = Duplicator.duplicate(pc.urlFormScope(), true) as Struct
                    url.removeEL(KeyConstants._fieldnames)
                    url.removeEL(KeyConstants._method)
                    var args: Object? = url.get(KeyConstants._argumentCollection, null)

                    // url returnFormat
                    val oReturnFormat: Object = url.removeEL(KeyConstants._returnFormat)
                    var urlReturnFormat = -1
                    if (oReturnFormat != null) urlReturnFormat = UDFUtil.toReturnFormat(Caster.toString(oReturnFormat, null), -1)

                    // request header accept
                    val accept: List<MimeType?> = ReqRspUtil.getAccept(pc)
                    val headerReturnFormat: Int = MimeType.toFormat(accept, -1, -1)
                    val queryFormat: Object = url.removeEL(KeyConstants._queryFormat)
                    if (args == null) {
                        args = pc.getHttpServletRequest().getAttribute("argumentCollection")
                    }
                    if (args is String) {
                        args = JSONExpressionInterpreter().interpret(pc, args as String?)
                    }
                    if (args != null) {
                        if (Decision.isCastableToStruct(args)) {
                            val sct: Struct = Caster.toStruct(args, false)
                            // Key[] keys = url.keys();
                            val it: Iterator<Entry<Key?, Object?>?> = url.entryIterator()
                            var e: Entry<Key?, Object?>?
                            while (it.hasNext()) {
                                e = it.next()
                                sct.setEL(e.getKey(), e.getValue())
                            }
                            args = sct
                        } else if (Decision.isCastableToArray(args)) {
                            args = Caster.toArray(args)
                        } else {
                            val arr: Array = ArrayImpl()
                            arr.appendEL(args)
                            args = arr
                        }
                    } else args = url
                    val rtn: Object? = call(app, pci, ON_CFCREQUEST, arrayOf<Object?>(requestedPage.getComponentName(), method, args), true)
                    if (rtn != null) {
                        if (pc.getHttpServletRequest().getHeader("AMF-Forward") != null) {
                            pc.variablesScope().setEL("AMF-Forward", rtn)
                        } else {
                            try {
                                ComponentPageImpl.writeToResponseStream(pc, app, method.toString(), urlReturnFormat, headerReturnFormat, queryFormat, rtn)
                            } catch (e: Exception) {
                                throw Caster.toPageException(e)
                            }
                        }
                    }
                } else {
                    try {
                        if (!isComp && app.contains(pc, ON_REQUEST)) call(app, pci, ON_REQUEST, arrayOf(targetPage), false) else pci._doInclude(arrayOf<PageSource?>(requestedPage), false, null)
                    } catch (pe: PageException) {
                        pe = handlePageException(pci, app, pe, requestedPage, targetPage, goon)
                        if (pe != null) throw pe
                    }
                }
            }
            // onRequestEnd
            if (goon.toBooleanValue() && app.contains(pc, ON_REQUEST_END)) {
                try {
                    call(app, pci, ON_REQUEST_END, arrayOf(targetPage), false)
                } catch (pe: PageException) {
                    pe = handlePageException(pci, app, pe, requestedPage, targetPage, goon)
                    if (pe != null) throw pe
                }
            }
        } else {
            // apps.put(pc.getApplicationContext().getName(), null);
            if (rl != null) {
                requestedPage = rl.execute(pc, requestedPage)
                if (requestedPage == null) return
            }
            pci._doInclude(arrayOf<PageSource?>(requestedPage), false, null)
        }
    }

    private fun isComponent(pc: PageContext?, requestedPage: PageSource?): Boolean {
        // CFML
        return if (pc.getRequestDialect() === CFMLEngine.DIALECT_CFML) {
            ResourceUtil.getExtension(requestedPage.getRealpath(), "").equalsIgnoreCase(Constants.getCFMLComponentExtension())
        } else !PageSourceImpl.isTemplate(pc, requestedPage, true)
        // Tachyon
    }

    @Throws(PageException::class)
    private fun handlePageException(pci: PageContextImpl?, app: Component?, pe: PageException?, requestedPage: PageSource?, targetPage: String?, goon: RefBoolean?): PageException? {
        var _pe: PageException? = pe
        if (pe is ModernAppListenerException) {
            _pe = (pe as ModernAppListenerException?)!!.getPageException()
        }
        if (!Abort.isSilentAbort(_pe)) {
            if (_pe is MissingIncludeException) {
                if ((_pe as MissingIncludeException?).getPageSource().equals(requestedPage)) {
                    if (app.contains(pci, ON_MISSING_TEMPLATE)) {
                        goon.setValue(false)
                        if (!Caster.toBooleanValue(call(app, pci, ON_MISSING_TEMPLATE, arrayOf(targetPage), true), true)) return pe
                    } else return pe
                } else return pe
            } else return pe
        } else {
            if (!pci.isGatewayContext() && pci.getConfig().debug()) {
                (pci.getDebugger() as DebuggerImpl).setAbort(ExceptionUtil.getThrowingPosition(pci, _pe))
            }
            goon.setValue(false)
            if (app.contains(pci, ON_ABORT)) {
                call(app, pci, ON_ABORT, arrayOf(targetPage), true)
            }
        }
        return null
    }

    @Override
    @Throws(PageException::class)
    fun onApplicationStart(pc: PageContext?): Boolean {
        return onApplicationStart(pc, pc.applicationScope())
    }

    @Override
    @Throws(PageException::class)
    override fun onApplicationStart(pc: PageContext?, application: Application?): Boolean {
        val app: Component? = getComponent(pc)
        if (app != null && app.contains(pc, ON_APPLICATION_END)) {
            if (application is ApplicationImpl) (application as ApplicationImpl?).setComponent(app)
        }
        if (app != null && app.contains(pc, ON_APPLICATION_START)) {
            val rtn: Object? = call(app, pc, ON_APPLICATION_START, ArrayUtil.OBJECT_EMPTY, true)
            return Caster.toBooleanValue(rtn, true)
        }
        return true
    }

    @Override
    @Throws(PageException::class)
    fun onApplicationEnd(factory: CFMLFactory?, applicationName: String?) {
        var app: Component? = null
        val scope: Application = (factory as CFMLFactoryImpl?).getScopeContext().getExistingApplicationScope(applicationName)
        if (scope is ApplicationImpl) app = (scope as ApplicationImpl).getComponent()
        if (app == null) return
        var pc: PageContextImpl? = ThreadLocalPageContext.get() as PageContextImpl
        val createPc = pc == null
        try {
            if (createPc) pc = createPageContext(factory, app, applicationName, null, ON_APPLICATION_END, true, -1)
            call(app, pc, ON_APPLICATION_END, arrayOf<Object?>(pc.applicationScope()), true)
        } finally {
            if (createPc && pc != null) {
                factory.releaseTachyonPageContext(pc, createPc)
            }
        }
    }

    @Override
    @Throws(PageException::class)
    fun onSessionStart(pc: PageContext?) {
        onSessionStart(pc, pc.sessionScope())
    }

    @Override
    @Throws(PageException::class)
    override fun onSessionStart(pc: PageContext?, session: Session?) {

        // component
        val app: Component? = getComponent(pc)
        if (hasOnSessionStart(pc, app)) {
            call(app, pc, ON_SESSION_START, ArrayUtil.OBJECT_EMPTY, true)
        }
        if (hasOnSessionEnd(pc, app)) {
            if (session is SessionMemory) (session as SessionMemory?).setComponent(app) else if (session is JSession) (session as JSession?).setComponent(app)
        }
    }

    @Override
    @Throws(PageException::class)
    fun onSessionEnd(factory: CFMLFactory?, applicationName: String?, cfid: String?) {
        try {
            var app: Component? = null
            val scope: Session = (factory as CFMLFactoryImpl?).getScopeContext().getExistingCFSessionScope(applicationName, cfid)
            if (scope is SessionMemory) app = (scope as SessionMemory).getComponent()
            if (scope is JSession) app = (scope as JSession).getComponent()
            if (app == null || !app.containsKey(ON_SESSION_END)) return
            var pc: PageContextImpl? = null
            try {
                pc = createPageContext(factory, app, applicationName, cfid, ON_SESSION_END, true, -1)
                call(app, pc, ON_SESSION_END, arrayOf<Object?>(pc.sessionScope(false), pc.applicationScope()), true)
            } finally {
                if (pc != null) {
                    factory.releaseTachyonPageContext(pc, true)
                }
            }
        } catch (t: Throwable) {
            LogUtil.log("application", t)
        }
    }

    @Throws(PageException::class)
    private fun createPageContext(factory: CFMLFactory?, app: Component?, applicationName: String?, cfid: String?, methodName: Collection.Key?, register: Boolean, timeout: Long): PageContextImpl? {
        val root: Resource = factory.getConfig().getRootDirectory()
        val path: String = app.getPageSource().getRealpathWithVirtual()

        // Request
        val req = HttpServletRequestDummy(root, "localhost", path, "", null, null, null, null, null, null)
        if (!StringUtil.isEmpty(cfid)) req.setCookies(arrayOf<Cookie?>(Cookie("cfid", cfid), Cookie("cftoken", "0")))

        // Response
        val os: OutputStream = DevNullOutputStream.DEV_NULL_OUTPUT_STREAM
        val rsp = HttpServletResponseDummy(os)

        // PageContext
        val pc: PageContextImpl = factory.getTachyonPageContext(factory.getServlet(), req, rsp, null, false, -1, false, register, timeout, true, false) as PageContextImpl

        // ApplicationContext
        val ap = ClassicApplicationContext(factory.getConfig(), applicationName, false,
                if (app == null) null else ResourceUtil.getResource(pc, app.getPageSource(), null))
        initApplicationContext(pc, app)
        ap!!.setName(applicationName)
        ap!!.setSetSessionManagement(true)

        // Base
        pc.setBase(app.getPageSource())
        return pc
    }

    @Override
    @Throws(PageException::class)
    fun onDebug(pc: PageContext?) {
        if ((pc as PageContextImpl?).isGatewayContext() || !pc.getConfig().debug()) return
        val app: Component? = getComponent(pc)
        if (app != null && app.contains(pc, ON_DEBUG)) {
            call(app, pc, ON_DEBUG, arrayOf<Object?>(pc.getDebugger().getDebuggingData(pc)), true)
            return
        }
        try {
            pc.getDebugger().writeOut(pc)
        } catch (e: IOException) {
            throw Caster.toPageException(e)
        }
    }

    @Override
    fun onError(pc: PageContext?, pe: PageException?) {
        var pe: PageException? = pe
        val app: Component? = getComponent(pc)
        if (app != null && app.containsKey(ON_ERROR) && !Abort.isSilentAbort(pe)) {
            try {
                var eventName = ""
                if (pe is ModernAppListenerException) eventName = (pe as ModernAppListenerException?)!!.getEventName()
                if (eventName == null) eventName = ""
                call(app, pc, ON_ERROR, arrayOf(pe.getCatchBlock(pc.getConfig()), eventName), true)
                return
            } catch (_pe: PageException) {
                pe = _pe
            }
        }
        pc.handlePageException(pe)
    }

    @Throws(PageException::class)
    private fun call(app: Component?, pc: PageContext?, eventName: Collection.Key?, args: Array<Object?>?, catchAbort: Boolean): Object? {
        return try {
            app.call(pc, eventName, args)
        } catch (pe: PageException) {
            if (Abort.isSilentAbort(pe)) {
                if (catchAbort) return if (pe is PostContentAbort) Boolean.TRUE else Boolean.FALSE
                throw pe
            }
            throw ModernAppListenerException(pe, eventName.getString())
        }
    }

    @Throws(PageException::class)
    private fun initApplicationContext(pc: PageContextImpl?, app: Component?): ModernApplicationContext? {

        // use existing app context
        val throwsErrorWhileInit: RefBoolean = RefBooleanImpl(false)
        val appContext = ModernApplicationContext(pc, app, throwsErrorWhileInit)
        pc.setApplicationContext(appContext)

        // scope cascading
        if (pc.getRequestDialect() === CFMLEngine.DIALECT_CFML && (pc.undefinedScope() as UndefinedImpl).getScopeCascadingType() !== appContext!!.getScopeCascading()) {
            pc.undefinedScope().initialize(pc)
        }

        // ORM
        if (appContext!!.isORMEnabled()) {
            val hasError: Boolean = throwsErrorWhileInit.toBooleanValue()
            if (hasError) pc.addPageSource(app.getPageSource(), true)
            try {
                ORMUtil.resetEngine(pc, false)
            } finally {
                if (hasError) pc.removeLastPageSource(true)
            }
        }
        return appContext
    }

    @Override
    fun setMode(mode: Int) {
        this.mode = mode
    }

    @Override
    fun getMode(): Int {
        return mode
    }

    @Override
    fun getType(): String? {
        return "modern"
    }

    @Override
    override fun hasOnSessionStart(pc: PageContext?): Boolean {
        return hasOnSessionStart(pc, getComponent(pc))
    }

    private fun hasOnSessionStart(pc: PageContext?, app: Component?): Boolean {
        return app != null && app.contains(pc, ON_SESSION_START)
    }

    private fun hasOnSessionEnd(pc: PageContext?, app: Component?): Boolean {
        return app != null && app.contains(pc, ON_SESSION_END)
    }

    private fun getComponent(pc: PageContext?): Component? {
        val ac: ApplicationContext = (pc as PageContextImpl?).getApplicationContext()
        var cfc: Component? = null
        if (ac is ModernApplicationContext) cfc = (ac as ModernApplicationContext)!!.getComponent()
        // if(cfc==null) cfc = apps.get(pc.getApplicationContext().getName());
        return cfc
    }

    companion object {
        val instance: ModernAppListener? = ModernAppListener()
        private val ON_REQUEST_START: Collection.Key? = KeyImpl.getInstance("onRequestStart")
        private val ON_CFCREQUEST: Collection.Key? = KeyImpl.getInstance("onCFCRequest")
        private val ON_REQUEST: Collection.Key? = KeyImpl.getInstance("onRequest")
        private val ON_REQUEST_END: Collection.Key? = KeyImpl.getInstance("onRequestEnd")
        private val ON_ABORT: Collection.Key? = KeyImpl.getInstance("onAbort")
        private val ON_APPLICATION_START: Collection.Key? = KeyImpl.getInstance("onApplicationStart")
        private val ON_APPLICATION_END: Collection.Key? = KeyImpl.getInstance("onApplicationEnd")
        private val ON_SESSION_START: Collection.Key? = KeyImpl.getInstance("onSessionStart")
        private val ON_SESSION_END: Collection.Key? = KeyImpl.getInstance("onSessionEnd")
        private val ON_DEBUG: Collection.Key? = KeyImpl.getInstance("onDebug")
        private val ON_ERROR: Collection.Key? = KeyConstants._onError
        private val ON_MISSING_TEMPLATE: Collection.Key? = KeyImpl.getInstance("onMissingTemplate")
        private operator fun get(app: Component?, name: Key?, defaultValue: String?): Object? {
            val mem: Member = app.getMember(Component.ACCESS_PRIVATE, name, true, false) ?: return defaultValue
            return mem.getValue()
        }

        fun getInstance(): ModernAppListener? {
            return instance
        }
    }
}