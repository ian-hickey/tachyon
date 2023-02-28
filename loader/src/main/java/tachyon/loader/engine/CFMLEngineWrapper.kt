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
package tachyon.loader.engine

import java.io.File

/**
 * wrapper for a CFMlEngine
 */
class CFMLEngineWrapper(engine: CFMLEngine) : CFMLEngine {
    private var engine: CFMLEngine

    @Override
    @Throws(ServletException::class)
    override fun addServletConfig(config: ServletConfig?) {
        engine.addServletConfig(config)
    }

    @Override
    @Throws(ServletException::class, IOException::class)
    override fun service(servlet: HttpServlet?, req: HttpServletRequest?, rsp: HttpServletResponse?) {
        engine.service(servlet, req, rsp)
    }

    @Override
    @Throws(ServletException::class, IOException::class)
    override fun serviceCFML(servlet: HttpServlet?, req: HttpServletRequest?, rsp: HttpServletResponse?) {
        engine.serviceCFML(servlet, req, rsp)
    }

    @Override
    @Throws(ServletException::class, IOException::class)
    override fun serviceAMF(servlet: HttpServlet?, req: HttpServletRequest?, rsp: HttpServletResponse?) {
        engine.serviceAMF(servlet, req, rsp)
    }

    @Override
    @Throws(ServletException::class, IOException::class)
    override fun serviceFile(servlet: HttpServlet?, req: HttpServletRequest?, rsp: HttpServletResponse?) {
        engine.serviceFile(servlet, req, rsp)
    }

    @Override
    @Throws(ServletException::class, IOException::class)
    override fun serviceRest(servlet: HttpServlet?, req: HttpServletRequest?, rsp: HttpServletResponse?) {
        engine.serviceRest(servlet, req, rsp)
    }

    @get:Override
    override val version: String?
        get() = engine.getInfo().getVersion().toString()

    @get:Override
    override val updateType: String
        get() = engine.getUpdateType()

    @get:Override
    override val updateLocation: URL
        get() = engine.getUpdateLocation()

    @get:Override
    override val identification: Identification
        get() = engine.getIdentification()

    @Override
    override fun can(type: Int, password: Password?): Boolean {
        return engine.can(type, password)
    }

    @get:Override
    override val cFMLEngineFactory: tachyon.loader.engine.CFMLEngineFactory
        get() = engine.getCFMLEngineFactory()

    @Override
    override fun reset() {
        engine.reset()
    }

    @Override
    override fun reset(configId: String?) {
        engine.reset(configId)
    }

    fun setEngine(engine: CFMLEngine) {
        this.engine = engine
    }

    fun getEngine(): CFMLEngine {
        return engine
    }

    fun isIdentical(engine: CFMLEngine): Boolean {
        return this.engine === engine
    }

    @get:Override
    override val castUtil: Cast
        get() = engine.getCastUtil()

    @get:Override
    override val operatonUtil: Operation
        get() = engine.getOperatonUtil()

    @get:Override
    override val decisionUtil: Decision
        get() = engine.getDecisionUtil()

    @get:Override
    override val exceptionUtil: Excepton
        get() = engine.getExceptionUtil()

    @get:Override
    override val creationUtil: Creation
        get() = engine.getCreationUtil()

    // FUTURE return JavaProxyUtil
    @get:Override
    override val javaProxyUtil: Object
        get() =// FUTURE return JavaProxyUtil
            engine.getJavaProxyUtil()

    @get:Override
    override val iOUtil: IO
        get() = engine.getIOUtil()

    @Override
    @Throws(ServletException::class)
    override fun getCFMLFactory(srvConfig: ServletConfig?, req: HttpServletRequest?): CFMLFactory {
        return engine.getCFMLFactory(srvConfig, req)
    }

    @get:Override
    override val fDController: Object
        get() = engine.getFDController()

    @get:Override
    override val hTTPUtil: HTTPUtil
        get() = engine.getHTTPUtil()

    @get:Override
    override val resourceUtil: ResourceUtil
        get() = engine.getResourceUtil()

    @get:Override
    override val threadPageContext: PageContext
        get() = engine.getThreadPageContext()

    @get:Override
    override val threadConfig: Config
        get() = engine.getThreadConfig()

    @get:Override
    override val videoUtil: VideoUtil
        get() = engine.getVideoUtil()

    @get:Override
    override val zipUtil: ZipUtil
        get() = engine.getZipUtil()

    @get:Override
    override val stringUtil: Strings
        get() = engine.getStringUtil()
    /*
	 * public String getState() { return engine.getInfo().getStateAsString(); }
	 */
    /**
     * this interface is new to this class and not officially part of Tachyon 3.x, do not use outside the
     * loader
     *
     * @param other engine to compare
     * @param checkReferenceEqualityOnly check reference equality only
     * @return is equal to given engine
     */
    fun equalTo(other: CFMLEngine, checkReferenceEqualityOnly: Boolean): Boolean {
        var other: CFMLEngine = other
        while (other is CFMLEngineWrapper) other = other.engine
        return if (checkReferenceEqualityOnly) engine === other else engine.equals(other)
    }

    @Override
    @Throws(IOException::class, JspException::class, ServletException::class)
    override fun cli(config: Map<String?, String?>?, servletConfig: ServletConfig?) {
        engine.cli(config, servletConfig)
    }

    @Override
    override fun registerThreadPageContext(pc: PageContext?) {
        engine.registerThreadPageContext(pc)
    }

    @Override
    @Throws(PageException::class)
    override fun getConfigServer(password: Password?): ConfigServer {
        return engine.getConfigServer(password)
    }

    @Override
    @Throws(PageException::class)
    override fun getConfigServer(key: String?, timeNonce: Long): ConfigServer {
        return engine.getConfigServer(key, timeNonce)
    }

    @Override
    override fun uptime(): Long {
        return engine.uptime()
    }

    @get:Override
    override val info: Info
        get() = engine.getInfo()

    @get:Override
    override val bundleContext: BundleContext
        get() = engine.getBundleContext()

    @get:Override
    override val classUtil: ClassUtil
        get() = engine.getClassUtil()

    /*
	 * @Override public XMLUtil getXMLUtil() { return engine.getXMLUtil(); }
	 */
    @Override
    override fun getScriptEngineFactory(dialect: Int): ScriptEngineFactory {
        return engine.getScriptEngineFactory(dialect)
    }

    @Override
    override fun getTagEngineFactory(dialect: Int): ScriptEngineFactory {
        return engine.getTagEngineFactory(dialect)
    }

    @get:Override
    override val servletConfigs: Array<Any>
        get() = engine.getServletConfigs()

    @get:Override
    override val listUtil: ListUtil
        get() = engine.getListUtil()

    @get:Override
    override val dBUtil: DBUtil
        get() = engine.getDBUtil()

    @get:Override
    override val oRMUtil: ORMUtil
        get() = engine.getORMUtil()

    @get:Override
    override val templateUtil: TemplateUtil
        get() = engine.getTemplateUtil()

    @Override
    @Throws(ServletException::class)
    override fun createPageContext(contextRoot: File?, host: String?, scriptName: String?, queryString: String?, cookies: Array<Cookie?>?,
                                   headers: Map<String?, Object?>?, parameters: Map<String?, String?>?, attributes: Map<String?, Object?>?, os: OutputStream?, timeout: Long,
                                   register: Boolean): PageContext {
        return engine.createPageContext(contextRoot, host, scriptName, queryString, cookies, headers, parameters, attributes, os, timeout, register)
    }

    @Override
    override fun releasePageContext(pc: PageContext?, unregister: Boolean) {
        engine.releasePageContext(pc, unregister)
    }

    @Override
    @Throws(ServletException::class)
    override fun createConfig(contextRoot: File?, host: String?, scriptName: String?): ConfigWeb {
        return engine.createConfig(contextRoot, host, scriptName)
    }

    @get:Override
    override val bundleCollection: BundleCollection
        get() = engine.getBundleCollection()

    @get:Override
    override val hTMLUtil: HTMLUtil
        get() = engine.getHTMLUtil()

    @get:Override
    override val threadTimeZone: TimeZone
        get() = engine.getThreadTimeZone()

    @get:Override
    override val systemUtil: SystemUtil
        get() = engine.getSystemUtil()

    @get:Override
    override val instrumentation: Instrumentation
        get() = engine.getInstrumentation()

    /**
     * constructor of the class
     *
     * @param engine engine to wrap
     */
    init {
        this.engine = engine
    }
}