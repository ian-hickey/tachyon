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
package lucee.runtime.util

import java.io.File

object PageContextUtil {
    fun getPageSource(mappings: Array<Mapping?>?, realPath: String?): PageSource? {
        var ps: PageSource
        for (i in mappings.indices) {
            ps = mappings!![i].getPageSource(realPath)
            if (ps.exists()) return ps
        }
        return null
    }

    fun merge(mappings1: Array<Mapping?>?, mappings2: Array<Mapping?>?): Array<Mapping?>? {
        val mappings: Array<Mapping?> = arrayOfNulls<Mapping?>(mappings1!!.size + mappings2!!.size)
        for (i in mappings1.indices) {
            mappings[i] = mappings1!![i]
        }
        for (i in mappings2.indices) {
            mappings[mappings1!!.size + i] = mappings2!![i]
        }
        return mappings
    }

    fun getApplicationListener(pc: PageContext?): ApplicationListener? {
        val ps: PageSource = pc.getBasePageSource()
        if (ps != null) {
            val mapp: MappingImpl = ps.getMapping() as MappingImpl
            if (mapp != null) return mapp.getApplicationListener()
        }
        return pc.getConfig().getApplicationListener()
    }

    fun getCookieDomain(pc: PageContext?): String? {
        if (!pc.getApplicationContext().isSetDomainCookies()) return null
        var result: String? = Caster.toString(pc.cgiScope().get(KeyConstants._server_name, null), null)
        if (!StringUtil.isEmpty(result)) {
            val listLast: String = ListUtil.last(result, '.')
            if (!lucee.runtime.op.Decision.isNumber(listLast)) { // if it's numeric then must be IP address
                var numparts = 2
                val listLen: Int = ListUtil.len(result, '.', true)
                if (listLen > 2) {
                    if (listLast.length() === 2 || !StringUtil.isAscii(listLast)) { // country TLD
                        val tldMinus1: Int = ListUtil.getAt(result, '.', listLen - 1, true, "").length()
                        if (tldMinus1 == 2 || tldMinus1 == 3) // domain is in country like, example.co.uk or example.org.il
                            numparts++
                    }
                }
                if (listLen > numparts) result = result.substring(result.indexOf('.')) else if (listLen == numparts) result = ".$result"
            }
        }
        return result
    }

    @Throws(ServletException::class)
    fun getPageContext(config: Config?, servletConfig: ServletConfig?, contextRoot: File?, host: String?, scriptName: String?, queryString: String?, cookies: Array<Cookie?>?,
                       headers: Map<String?, Object?>?, parameters: Map<String?, String?>?, attributes: Map<String?, Object?>?, os: OutputStream?, register: Boolean, timeout: Long, ignoreScopes: Boolean): PageContext? {
        var config: Config? = config
        var servletConfig: ServletConfig? = servletConfig
        var contextRoot: File? = contextRoot
        var headers: Map<String?, Object?>? = headers
        var parameters = parameters
        var attributes: Map<String?, Object?>? = attributes
        val callOnStart: Boolean = ThreadLocalPageContext.callOnStart.get()
        return try {
            ThreadLocalPageContext.callOnStart.set(false)
            if (contextRoot == null) contextRoot = File(".")
            // Engine
            var engine: CFMLEngine? = null
            try {
                engine = CFMLEngineFactory.getInstance()
            } catch (t: Throwable) {
                ExceptionUtil.rethrowIfNecessary(t)
            }
            if (engine == null) throw ServletException("there is no ServletContext")
            if (headers == null) headers = HashMap<String?, Object?>()
            if (parameters == null) parameters = HashMap<String?, String?>()
            if (attributes == null) attributes = HashMap<String?, Object?>()

            // Request
            val req: HttpServletRequest = CreationImpl.getInstance(engine).createHttpServletRequest(contextRoot, host, scriptName, queryString, cookies, headers, parameters, attributes,
                    null)

            // Response
            val rsp: HttpServletResponse = CreationImpl.getInstance(engine).createHttpServletResponse(os)
            if (config == null) config = ThreadLocalPageContext.getConfig()
            var factory: CFMLFactory? = null
            val servlet: HttpServlet?
            if (config is ConfigWeb) {
                val cw: ConfigWeb? = config as ConfigWeb?
                factory = cw.getFactory()
                servlet = factory.getServlet()
            } else {
                if (servletConfig == null) {
                    val configs: Array<ServletConfig?> = engine.getServletConfigs()
                    val rootDir: String = contextRoot.getAbsolutePath()
                    for (conf in configs) {
                        if (lucee.commons.io.SystemUtil.arePathsSame(rootDir, ReqRspUtil.getRootPath(conf.getServletContext()))) {
                            servletConfig = conf
                            break
                        }
                    }
                    if (servletConfig == null) servletConfig = configs[0]
                }
                var e: CFMLEngine? = engine
                if (engine is CFMLEngineWrapper) {
                    e = (engine as CFMLEngineWrapper?).getEngine()
                }
                factory = if (e is CFMLEngineImpl && config is ConfigServerImpl) (e as CFMLEngineImpl?).getCFMLFactory(config as ConfigServerImpl?, servletConfig, req) else e.getCFMLFactory(servletConfig, req)
                servlet = HTTPServletImpl(servletConfig, servletConfig.getServletContext(), servletConfig.getServletName())
            }
            factory.getLuceePageContext(servlet, req, rsp, null, false, -1, false, register, timeout, false, ignoreScopes)
        } finally {
            ThreadLocalPageContext.callOnStart.set(callOnStart)
        }
    }

    fun releasePageContext(pc: PageContext?, register: Boolean) {
        if (pc != null) pc.getConfig().getFactory().releaseLuceePageContext(pc, register)
        ThreadLocalPageContext.register(null)
    }

    @Throws(RequestTimeoutException::class)
    fun remainingTime(pc: PageContext?, throwWhenAlreadyTimeout: Boolean): TimeSpan? {
        var ms: Long = pc.getRequestTimeout() - (System.currentTimeMillis() - pc.getStartTime())
        if (ms > 0) {
            if (ms < 5) {
            } else if (ms < 10) ms = ms - 1 else if (ms < 50) ms = ms - 5 else if (ms < 200) ms = ms - 10 else if (ms < 1000) ms = ms - 50 else ms = ms - 100
            return TimeSpanImpl.fromMillis(ms)
        }
        if (throwWhenAlreadyTimeout && allowRequestTimeout(pc) && (pc as PageContextImpl?).getTimeoutStackTrace() == null) throw CFMLFactoryImpl.createRequestTimeoutException(pc)
        return TimeSpanImpl.fromMillis(0)
    }

    @Throws(RequestTimeoutException::class)
    fun checkRequestTimeout(pc: PageContext?) {
        if (pc.getRequestTimeout() - (System.currentTimeMillis() - pc.getStartTime()) > 0 || (pc as PageContextImpl?).getTimeoutStackTrace() != null) return
        if (allowRequestTimeout(pc)) throw CFMLFactoryImpl.createRequestTimeoutException(pc)
    }

    private fun allowRequestTimeout(pc: PageContext?): Boolean {
        if (!(ThreadLocalPageContext.getConfig(pc) as ConfigPro).allowRequestTimeout()) return false
        val factory: CFMLFactoryImpl = pc.getConfig().getFactory() as CFMLFactoryImpl
        return factory.reachedConcurrentReqThreshold() && factory.reachedCPUThreshold() && factory.reachedMemoryThreshold()
    }

    @Throws(PageException::class)
    fun getHandlePageException(pc: PageContextImpl?, pe: PageException?): String? {
        var bc: BodyContent? = null
        var str: String? = null
        try {
            bc = pc.pushBody()
            pc.handlePageException(pe, false)
        } catch (t: Throwable) {
            ExceptionUtil.rethrowIfNecessary(t)
            throw Caster.toPageException(t)
        } finally {
            if (bc != null) str = bc.getString()
            pc.popBody()
        }
        return str
    }

    @Throws(PageException::class)
    fun getFunction(pc: PageContext?, coll: Object?, args: Array<Object?>?): Object? {
        return Caster.toFunction(coll).call(pc, args, true)
    }

    @Throws(PageException::class)
    fun getFunctionWithNamedValues(pc: PageContext?, coll: Object?, args: Array<Object?>?): Object? {
        return Caster.toFunction(coll).callWithNamedValues(pc, Caster.toFunctionValues(args), true)
    }
}