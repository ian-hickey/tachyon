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
package lucee.cli.servlet

import java.io.File

class ServletContextImpl(root: File, attributes: Map<String, Object>, parameters: Map<String, String>, majorVersion: Int, minorVersion: Int) : ServletContext {
    private val attributes: Map<String, Object>
    private val parameters: Map<String, String>

    /**
     * @see javax.servlet.ServletContext.getMajorVersion
     */
    @get:Override
    val majorVersion: Int

    /**
     * @see javax.servlet.ServletContext.getMinorVersion
     */
    @get:Override
    val minorVersion: Int
    private val root: File
    private var logger: Logger? = null

    /**
     * @see javax.servlet.ServletContext.getAttribute
     */
    @Override
    fun getAttribute(key: String): Object? {
        return attributes[key]
    }

    /**
     * @see javax.servlet.ServletContext.getAttributeNames
     */
    @get:Override
    val attributeNames: Enumeration<String>
        get() = EnumerationWrapper<String>(attributes)

    /**
     * @see javax.servlet.ServletContext.getInitParameter
     */
    @Override
    fun getInitParameter(key: String): String? {
        return parameters[key]
    }

    /**
     * @see javax.servlet.ServletContext.getInitParameterNames
     */
    @get:Override
    val initParameterNames: Enumeration<String>
        get() = EnumerationWrapper<String>(parameters)

    /**
     * @see javax.servlet.ServletContext.getMimeType
     */
    @Override
    fun getMimeType(file: String?): String {
        throw notSupported("getMimeType(String file)")
    }

    /**
     * @see javax.servlet.ServletContext.getRealPath
     */
    @Override
    fun getRealPath(realpath: String?): String {
        return getRealFile(realpath).getAbsolutePath()
    }

    /**
     * @see javax.servlet.ServletContext.getResource
     */
    @Override
    @Throws(MalformedURLException::class)
    fun getResource(realpath: String?): URL {
        val file: File = getRealFile(realpath)
        return file.toURI().toURL()
    }

    /**
     * @see javax.servlet.ServletContext.getResourceAsStream
     */
    @Override
    fun getResourceAsStream(realpath: String?): InputStream? {
        return try {
            FileInputStream(getRealFile(realpath))
        } catch (e: IOException) {
            null
        }
    }

    fun getRealFile(realpath: String?): File {
        return File(root, realpath)
    }

    fun getRoot(): File {
        return root
    }

    @Override
    fun getResourcePaths(realpath: String?): Set<String> {
        throw notSupported("getResourcePaths(String realpath)")
    }

    @Override
    fun getRequestDispatcher(path: String?): RequestDispatcher {
        throw notSupported("getNamedDispatcher(String name)")
    }

    @Override
    fun getContext(key: String?): ServletContext {
        // TODO ?
        return this
    }

    @Override
    fun getNamedDispatcher(name: String?): RequestDispatcher {
        throw notSupported("getNamedDispatcher(String name)")
    }

    /**
     * @see javax.servlet.ServletContext.log
     */
    @Override
    fun log(msg: String?, t: Throwable?) { // TODO better
        if (logger == null) return
        if (t == null) logger.log(Logger.LOG_INFO, msg) else logger.log(Logger.LOG_ERROR, msg, t)
    }

    /**
     * @see javax.servlet.ServletContext.log
     */
    @Override
    fun log(e: Exception?, msg: String?) {
        log(msg, e)
    }

    /**
     * @see javax.servlet.ServletContext.log
     */
    @Override
    fun log(msg: String?) {
        log(msg, null)
    }

    /**
     * @see javax.servlet.ServletContext.removeAttribute
     */
    @Override
    fun removeAttribute(key: String?) {
        attributes.remove(key)
    }

    /**
     * @see javax.servlet.ServletContext.setAttribute
     */
    @Override
    fun setAttribute(key: String?, value: Object?) {
        attributes.put(key, value)
    }

    // can return null
    @get:Override
    val servletContextName: String?
        get() =// can return null
            null

    // deprecated
    @get:Override
    val serverInfo: String
        get() {
            // deprecated
            throw notSupported("getServlet()")
        }

    @Override
    @Throws(ServletException::class)
    fun getServlet(arg0: String?): Servlet {
        // deprecated
        throw notSupported("getServlet()")
    }

    // deprecated
    @get:Override
    val servletNames: Enumeration<String>
        get() {
            // deprecated
            throw notSupported("getServlet()")
        }

    // deprecated
    @get:Override
    val servlets: Enumeration<Servlet>
        get() {
            // deprecated
            throw notSupported("getServlet()")
        }

    private fun notSupported(method: String): RuntimeException {
        throw RuntimeException(ServletException("method $method not supported"))
    }

    @Override
    fun addFilter(arg0: String?, arg1: String?): Dynamic {
        throw notSupported("")
    }

    @Override
    fun addFilter(arg0: String?, arg1: Filter?): Dynamic {
        throw notSupported("")
    }

    @Override
    fun addFilter(arg0: String?, arg1: Class<out Filter?>?): Dynamic {
        throw notSupported("")
    }

    @Override
    fun addListener(arg0: String?) {
        throw notSupported("")
    }

    @Override
    fun <T : EventListener?> addListener(arg0: T) {
        throw notSupported("")
    }

    @Override
    fun addListener(arg0: Class<out EventListener?>?) {
        throw notSupported("")
    }

    @Override
    fun addServlet(arg0: String?, arg1: String?): javax.servlet.ServletRegistration.Dynamic {
        throw notSupported("")
    }

    @Override
    fun addServlet(arg0: String?, arg1: Servlet?): javax.servlet.ServletRegistration.Dynamic {
        throw notSupported("")
    }

    @Override
    fun addServlet(arg0: String?, arg1: Class<out Servlet?>?): javax.servlet.ServletRegistration.Dynamic {
        throw notSupported("addServlet")
    }

    @Override
    @Throws(ServletException::class)
    fun <T : Filter?> createFilter(arg0: Class<T>?): T {
        throw notSupported("createFilter")
    }

    @Override
    @Throws(ServletException::class)
    fun <T : EventListener?> createListener(arg0: Class<T>?): T {
        throw notSupported("createListener")
    }

    @Override
    @Throws(ServletException::class)
    fun <T : Servlet?> createServlet(arg0: Class<T>?): T {
        throw notSupported("createServlet")
    }

    @Override
    fun declareRoles(vararg arg0: String?) {
        throw notSupported("declareRoles(String ...)")
    }

    @get:Override
    val classLoader: ClassLoader
        get() = this.getClass().getClassLoader()

    @get:Override
    val contextPath: String
        get() = root.getAbsolutePath()

    @get:Override
    val defaultSessionTrackingModes: Set<Any>
        get() {
            throw notSupported("getDefaultSessionTrackingModes()")
        }

    @get:Override
    val effectiveMajorVersion: Int
        get() = majorVersion

    @get:Override
    val effectiveMinorVersion: Int
        get() = minorVersion

    @get:Override
    val effectiveSessionTrackingModes: Set<Any>
        get() {
            throw notSupported("getEffectiveSessionTrackingModes()")
        }

    @Override
    fun getFilterRegistration(arg0: String?): FilterRegistration {
        throw notSupported("getFilterRegistration(String)")
    }

    @get:Override
    val filterRegistrations: Map<String, Any?>
        get() {
            throw notSupported("getFilterRegistrations()")
        }

    @get:Override
    val jspConfigDescriptor: JspConfigDescriptor
        get() {
            throw notSupported("getJspConfigDescriptor()")
        }

    @Override
    fun getServletRegistration(arg0: String?): ServletRegistration {
        throw notSupported("getServletRegistration(String)")
    }

    @get:Override
    val servletRegistrations: Map<String, Any?>
        get() {
            throw notSupported("getServletRegistrations()")
        }

    @get:Override
    val sessionCookieConfig: SessionCookieConfig
        get() {
            throw notSupported("getSessionCookieConfig()")
        }

    @get:Override
    val virtualServerName: String
        get() {
            throw notSupported("getVirtualServerName()")
        }

    @Override
    fun setInitParameter(name: String, value: String?): Boolean {
        if (!parameters.containsKey(name)) {
            parameters.put(name, value)
            return true
        }
        return false
    }

    @Override
    fun setSessionTrackingModes(arg0: Set<SessionTrackingMode?>?) {
        throw notSupported("setSessionTrackingModes(Set<SessionTrackingMode>) ")
    }

    fun setLogger(logger: Logger?) {
        this.logger = logger
    }

    /* noop impl for abstract methods added in Servlet 4.0 */
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
        this.root = root
        this.attributes = attributes
        this.parameters = parameters
        this.majorVersion = majorVersion
        this.minorVersion = minorVersion
    }
}