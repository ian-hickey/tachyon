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
package tachyon.runtime.net.http

import java.io.File

class ServletContextDummy(config: Config?, root: Resource?, attributes: Struct?, parameters: Struct?, majorVersion: Int, minorVersion: Int) : ServletContext {
    private val attributes: Struct?
    private val parameters: Struct?

    @get:Override
    val majorVersion: Int

    @get:Override
    val minorVersion: Int
    private val config: Config?
    private val log: Log?
    private val root: Resource?
    @Override
    fun getAttribute(key: String?): Object? {
        return attributes.get(key, null)
    }

    @get:Override
    val attributeNames: Enumeration?
        get() = ItAsEnum.toStringEnumeration(attributes.keyIterator())

    @Override
    fun getInitParameter(key: String?): String? {
        return Caster.toString(parameters.get(key, null), null)
    }

    @get:Override
    val initParameterNames: Enumeration?
        get() = EnumerationWrapper(parameters.keyIterator())

    @Override
    fun getMimeType(file: String?): String? {
        return ResourceUtil.getMimeType(config.getResource(file), null)
    }

    @Override
    fun getRealPath(realpath: String?): String? {
        return root.getRealResource(realpath).getAbsolutePath()
    }

    @Override
    @Throws(MalformedURLException::class)
    fun getResource(realpath: String?): URL? {
        val res: Resource? = getRealResource(realpath)
        return if (res is File) (res as File?).toURL() else URL(res.getAbsolutePath())
    }

    @Override
    fun getResourceAsStream(realpath: String?): InputStream? {
        return try {
            getRealResource(realpath).getInputStream()
        } catch (e: IOException) {
            null
        }
    }

    fun getRealResource(realpath: String?): Resource? {
        return root.getRealResource(realpath)
    }

    @Override
    fun getResourcePaths(realpath: String?): Set? {
        // TODO Auto-generated method stub
        return null
    }

    @Override
    fun getRequestDispatcher(path: String?): RequestDispatcher? {
        // TODO Auto-generated method stub
        return null
    }

    @Override
    fun getContext(key: String?): ServletContext? {
        // TODO ?
        return this
    }

    @Override
    fun getNamedDispatcher(name: String?): RequestDispatcher? {
        // TODO Auto-generated method stub
        return null
    }

    @Override
    fun log(msg: String?, t: Throwable?) {
        if (t == null) log.log(Log.LEVEL_INFO, null, msg) else log.log(Log.LEVEL_ERROR, null, msg, t)
    }

    @Override
    fun log(e: Exception?, msg: String?) {
        log(msg, e)
    }

    @Override
    fun log(msg: String?) {
        log(msg, null)
    }

    @Override
    fun removeAttribute(key: String?) {
        attributes.removeEL(KeyImpl.init(key))
    }

    @Override
    fun setAttribute(key: String?, value: Object?) {
        attributes.setEL(KeyImpl.init(key), value)
    }

    @get:Override
    val servletContextName: String?
        get() {
            throw RuntimeException("not supported")
        }

    @get:Override
    val serverInfo: String?
        get() {
            throw RuntimeException("not supported")
        }

    @Override
    @Throws(ServletException::class)
    fun getServlet(arg0: String?): Servlet? {
        throw RuntimeException("not supported")
    }

    // deprecated
    @get:Override
    val servletNames: Enumeration?
        get() =// deprecated
            null

    @get:Override
    val servlets: Enumeration?
        get() {
            throw RuntimeException("not supported")
        }

    @Override
    fun addFilter(arg0: String?, arg1: String?): Dynamic? {
        throw RuntimeException("not supported")
    }

    @Override
    fun addFilter(arg0: String?, arg1: Filter?): Dynamic? {
        // TODO Auto-generated method stub
        return null
    }

    @Override
    fun addFilter(arg0: String?, arg1: Class<out Filter?>?): Dynamic? {
        throw RuntimeException("not supported")
    }

    @Override
    fun addListener(arg0: String?) {
        throw RuntimeException("not supported")
    }

    @Override
    fun <T : EventListener?> addListener(arg0: T?) {
        throw RuntimeException("not supported")
    }

    @Override
    fun addListener(arg0: Class<out EventListener?>?) {
        throw RuntimeException("not supported")
    }

    @Override
    fun addServlet(arg0: String?, arg1: String?): javax.servlet.ServletRegistration.Dynamic? {
        throw RuntimeException("not supported")
    }

    @Override
    fun addServlet(arg0: String?, arg1: Servlet?): javax.servlet.ServletRegistration.Dynamic? {
        throw RuntimeException("not supported")
    }

    @Override
    fun addServlet(arg0: String?, arg1: Class<out Servlet?>?): javax.servlet.ServletRegistration.Dynamic? {
        throw RuntimeException("not supported")
    }

    @Override
    @Throws(ServletException::class)
    fun <T : Filter?> createFilter(arg0: Class<T?>?): T? {
        throw RuntimeException("not supported")
    }

    @Override
    @Throws(ServletException::class)
    fun <T : EventListener?> createListener(arg0: Class<T?>?): T? {
        throw RuntimeException("not supported")
    }

    @Override
    @Throws(ServletException::class)
    fun <T : Servlet?> createServlet(arg0: Class<T?>?): T? {
        throw RuntimeException("not supported")
    }

    @Override
    fun declareRoles(vararg arg0: String?) {
        throw RuntimeException("not supported")
    }

    @get:Override
    val classLoader: ClassLoader?
        get() {
            throw RuntimeException("not supported")
        }

    @get:Override
    val contextPath: String?
        get() {
            throw RuntimeException("not supported")
        }

    @get:Override
    val defaultSessionTrackingModes: Set<Any?>?
        get() {
            throw RuntimeException("not supported")
        }

    @get:Override
    val effectiveMajorVersion: Int
        get() = majorVersion

    @get:Override
    val effectiveMinorVersion: Int
        get() = minorVersion

    @get:Override
    val effectiveSessionTrackingModes: Set<Any?>?
        get() {
            throw RuntimeException("not supported")
        }

    @Override
    fun getFilterRegistration(arg0: String?): FilterRegistration? {
        throw RuntimeException("not supported")
    }

    @get:Override
    val filterRegistrations: Map<String?, Any?>?
        get() {
            throw RuntimeException("not supported")
        }

    @get:Override
    val jspConfigDescriptor: JspConfigDescriptor?
        get() {
            throw RuntimeException("not supported")
        }

    @Override
    fun getServletRegistration(arg0: String?): ServletRegistration? {
        throw RuntimeException("not supported")
    }

    @get:Override
    val servletRegistrations: Map<String?, Any?>?
        get() {
            throw RuntimeException("not supported")
        }

    @get:Override
    val sessionCookieConfig: SessionCookieConfig?
        get() {
            throw RuntimeException("not supported")
        }

    @get:Override
    val virtualServerName: String?
        get() {
            throw RuntimeException("not supported")
        }

    @Override
    fun setInitParameter(arg0: String?, arg1: String?): Boolean {
        throw RuntimeException("not supported")
    }

    @Override
    fun setSessionTrackingModes(arg0: Set<SessionTrackingMode?>?) {
        throw RuntimeException("not supported")
    }

    /* implement noop for abstract methods added in Servlet 4.0 */
    fun addJspFile(s: String?, s1: String?): ServletRegistration.Dynamic? {
        return null
    }

    var sessionTimeout: Int
        get() = 0
        set(i) {}
    var requestCharacterEncoding: String?
        get() = null
        set(s) {}
    var responseCharacterEncoding: String?
        get() = null
        set(s) {}

    init {
        this.config = config
        this.root = root
        this.attributes = attributes
        this.parameters = parameters
        this.majorVersion = majorVersion
        this.minorVersion = minorVersion
        log = (config as ConfigPro?).getLogEngine().getConsoleLog(false, "servlet-context-dummy", Log.LEVEL_INFO)
    }
}