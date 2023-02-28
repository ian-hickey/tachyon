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
package tachyon.runtime.thread

import java.io.OutputStream

object ThreadUtil {
    // do not change, used in Redis extension
    fun clonePageContext(pc: PageContext?, os: OutputStream?, stateless: Boolean, register2Thread: Boolean, register2RunningThreads: Boolean): PageContextImpl? {
        // TODO stateless
        val factory: CFMLFactoryImpl = pc.getConfig().getFactory() as CFMLFactoryImpl
        val req: HttpServletRequest = HTTPServletRequestWrap(cloneHttpServletRequest(pc))
        val rsp: HttpServletResponse? = createHttpServletResponse(os)

        // copy state
        val pci: PageContextImpl? = pc as PageContextImpl?
        // pci.copyStateTo(dest);
        return factory.getPageContextImpl(factory.getServlet(), req, rsp, null, false, -1, false, register2Thread, true, pc.getRequestTimeout(),
                register2RunningThreads, false, false, pci)
    }

    /**
     *
     * @param config
     * @param os
     * @param serverName
     * @param requestURI
     * @param queryString
     * @param cookies
     * @param headers
     * @param parameters
     * @param attributes
     * @param register
     * @param timeout timeout in ms, if the value is smaller than 1 it is ignored and the value coming
     * from the context is used
     * @return
     */
    fun createPageContext(config: ConfigWeb?, os: OutputStream?, serverName: String?, requestURI: String?, queryString: String?, cookies: Array<Cookie?>?, headers: Array<Pair?>?,
                          body: ByteArray?, parameters: Array<Pair?>?, attributes: Struct?, register: Boolean, timeout: Long): PageContextImpl? {
        return createPageContext(config, os, serverName, requestURI, queryString, cookies, headers, body, parameters, attributes, register, timeout, null)
    }

    fun createPageContext(config: ConfigWeb?, os: OutputStream?, serverName: String?, requestURI: String?, queryString: String?, cookies: Array<Cookie?>?, headers: Array<Pair?>?,
                          body: ByteArray?, parameters: Array<Pair?>?, attributes: Struct?, register: Boolean, timeout: Long, session: HttpSession?): PageContextImpl? {
        val factory: CFMLFactory = config.getFactory()
        var req: HttpServletRequest? = HttpServletRequestDummy(config.getRootDirectory(), serverName, requestURI, queryString, cookies, headers, parameters, attributes, session,
                body)
        req = HTTPServletRequestWrap(req)
        val rsp: HttpServletResponse? = createHttpServletResponse(os)
        return factory.getTachyonPageContext(factory.getServlet(), req, rsp, null, false, -1, false, register, timeout, false, false) as PageContextImpl
    }

    fun createDummyPageContext(config: ConfigWeb?): PageContextImpl? {
        return createPageContext(config, DevNullOutputStream.DEV_NULL_OUTPUT_STREAM, Constants.NAME, "/", "", null, null, null, null, null, true, -1, null).setDummy(true)
    }

    /**
     *
     * @param factory
     * @param rootDirectory
     * @param os
     * @param serverName
     * @param requestURI
     * @param queryString
     * @param cookies
     * @param headers
     * @param parameters
     * @param attributes
     * @param register
     * @param timeout in ms, if the value is smaller than 1 it is ignored and the value comming from the
     * context is used
     * @return
     */
    fun createPageContext(factory: CFMLFactory?, rootDirectory: Resource?, os: OutputStream?, serverName: String?, requestURI: String?, queryString: String?,
                          cookies: Array<Cookie?>?, headers: Array<Pair?>?, parameters: Array<Pair?>?, attributes: Struct?, register: Boolean, timeout: Long): PageContextImpl? {
        val req: HttpServletRequest? = createHttpServletRequest(rootDirectory, serverName, requestURI, queryString, cookies, headers, parameters, attributes, null)
        val rsp: HttpServletResponse? = createHttpServletResponse(os)
        return factory.getTachyonPageContext(factory.getServlet(), req, rsp, null, false, -1, false, register, timeout, false, false) as PageContextImpl
    }

    fun createHttpServletRequest(contextRoot: Resource?, serverName: String?, scriptName: String?, queryString: String?, cookies: Array<Cookie?>?, headers: Array<Pair?>?,
                                 parameters: Array<Pair?>?, attributes: Struct?, session: HttpSession?): HttpServletRequest? {
        return HTTPServletRequestWrap(HttpServletRequestDummy(contextRoot, serverName, scriptName, queryString, cookies, headers, parameters, attributes, null, null))
    }

    fun cloneHttpServletRequest(pc: PageContext?): HttpServletRequest? {
        val config: Config = pc.getConfig()
        val req: HttpServletRequest = pc.getHttpServletRequest()
        return HttpServletRequestDummy.clone(config, config.getRootDirectory(), req)
    }

    fun createHttpServletResponse(os: OutputStream?): HttpServletResponse? {
        var os: OutputStream? = os
        if (os == null) os = DevNullOutputStream.DEV_NULL_OUTPUT_STREAM
        return HttpServletResponseDummy(os)
    }

    /**
     * return priority as a String representation
     *
     * @param priority Thread priority
     * @return String definition of priority (null when input is invalid)
     */
    fun toStringPriority(priority: Int): String? {
        if (priority == Thread.NORM_PRIORITY) return "NORMAL"
        if (priority == Thread.MAX_PRIORITY) return "HIGH"
        return if (priority == Thread.MIN_PRIORITY) "LOW" else null
    }

    /**
     * return priority as an int representation
     *
     * @param priority Thread priority as String definition
     * @return int definition of priority (-1 when input is invalid)
     */
    fun toIntPriority(strPriority: String?): Int {
        var strPriority = strPriority
        strPriority = strPriority.trim().toLowerCase()
        if ("low".equals(strPriority)) return Thread.MIN_PRIORITY
        if ("min".equals(strPriority)) return Thread.MIN_PRIORITY
        if ("high".equals(strPriority)) return Thread.MAX_PRIORITY
        if ("max".equals(strPriority)) return Thread.MAX_PRIORITY
        if ("normal".equals(strPriority)) return Thread.NORM_PRIORITY
        return if ("norm".equals(strPriority)) Thread.NORM_PRIORITY else -1
    }

    fun printThreads() {
        val it: Iterator<Entry<Thread?, Array<StackTraceElement?>?>?> = Thread.getAllStackTraces().entrySet().iterator()
        var e: Entry<Thread?, Array<StackTraceElement?>?>?
        while (it.hasNext()) {
            e = it.next()
            aprint.e(e.getKey().getName())
            aprint.e(ExceptionUtil.toString(e.getValue()))
        }
    }

    fun isInNativeMethod(thread: Thread?, defaultValue: Boolean): Boolean {
        if (thread == null) return defaultValue
        val stes: Array<StackTraceElement?> = thread.getStackTrace()
        if (stes == null || stes.size == 0) return defaultValue
        val ste: StackTraceElement? = stes[0]
        return ste.isNativeMethod()
    }
}