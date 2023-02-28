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
 * The CFML Engine
 */
interface CFMLEngine {
    @Throws(ServletException::class)
    fun getCFMLFactory(srvConfig: ServletConfig?, req: HttpServletRequest?): CFMLFactory

    @Throws(ServletException::class)
    fun addServletConfig(config: ServletConfig?)

    @Throws(IOException::class, ServletException::class)
    fun service(servlet: HttpServlet?, req: HttpServletRequest?, rsp: HttpServletResponse?)

    @Throws(IOException::class, ServletException::class)
    fun serviceCFML(servlet: HttpServlet?, req: HttpServletRequest?, rsp: HttpServletResponse?)

    @Throws(ServletException::class, IOException::class)
    fun serviceAMF(servlet: HttpServlet?, req: HttpServletRequest?, rsp: HttpServletResponse?)

    @Throws(ServletException::class, IOException::class)
    fun serviceFile(servlet: HttpServlet?, req: HttpServletRequest?, rsp: HttpServletResponse?)

    @Throws(ServletException::class, IOException::class)
    fun serviceRest(servlet: HttpServlet?, req: HttpServletRequest?, rsp: HttpServletResponse?)
    val info: Info

    /**
     * @return returns the version of the engine in the format [x.x.x.xxx]
     */
    @get:Deprecated("use instead getInfo()")
    @get:Deprecated
    val version: String?

    /**
     * @return returns how this engine will be updated (auto, manual)
     */
    val updateType: String

    /**
     * @return return location URL to get updates for the engines
     */
    val updateLocation: URL
    val identification: Identification

    /**
     * checks if process has the right to do was given with type, the engine with given password
     *
     * @param type restart type (CFMLEngine.CAN_UPDATE, CFMLEngine.CAN_RESTART)
     * @param password password for the env
     * @return has right
     */
    fun can(type: Int, password: Password?): Boolean

    /**
     * @return returns the engine that has produced this engine
     */
    val cFMLEngineFactory: tachyon.loader.engine.CFMLEngineFactory

    /**
     * reset the engine
     */
    fun reset()

    /**
     * reset a specific config
     *
     * @param configId id of the config to reset
     */
    fun reset(configId: String?)

    /**
     * return the cast util
     *
     * @return operaton util
     */
    val castUtil: Cast

    /**
     * return the operation util
     *
     * @return operaton util
     */
    val operatonUtil: Operation

    /**
     * returns the decision util
     *
     * @return decision util
     */
    val decisionUtil: Decision

    /**
     * returns the decision util
     *
     * @return decision util
     */
    val exceptionUtil: Excepton

    /**
     * returns the decision util
     *
     * @return decision util
     */
    val creationUtil: Creation

    // FUTURE return JavaProxyUtil
    val javaProxyUtil: Object

    /**
     * returns the IO util
     *
     * @return decision util
     */
    val iOUtil: IO

    /**
     * returns the IO util
     *
     * @return decision util
     */
    val stringUtil: Strings
    val classUtil: ClassUtil

    /**
     * returns the FusionDebug Engine
     *
     * @return IFDController
     */
    val fDController: Object
    /*
	 * removed to avoid library conflicts, the blazeDS implementation is no longer under development an
	 * in a separate jar
	 */
    // public Object getBlazeDSUtil();
    /**
     * returns the Resource Util
     *
     * @return Blaze DS Util
     */
    val resourceUtil: ResourceUtil

    /**
     * returns the HTTP Util
     *
     * @return the HTTP Util
     */
    val hTTPUtil: HTTPUtil

    // public XMLUtil getXMLUtil();
    val listUtil: ListUtil
    val hTMLUtil: HTMLUtil
    val dBUtil: DBUtil
    val instrumentation: Instrumentation
    val oRMUtil: ORMUtil

    /**
     * @return return existing PageContext for the current PageContext
     */
    val threadPageContext: PageContext
    val threadConfig: Config
    val threadTimeZone: TimeZone

    /**
     * create and register a PageContext, use releasePageContext when done
     *
     * @param contextRoot context root
     * @param host host name
     * @param scriptName script name
     * @param queryString query string
     * @param cookies cookies
     * @param headers header elements
     * @param parameters parameters
     * @param attributes attributes
     * @param os output stream to write response body
     * @param timeout timeout for the thread
     * @param register register to thread or not
     * @return PageContext Object created
     * @throws ServletException in case the PC cannot be created
     */
    @Throws(ServletException::class)
    fun createPageContext(contextRoot: File?, host: String?, scriptName: String?, queryString: String?, cookies: Array<Cookie?>?, headers: Map<String?, Object?>?,
                          parameters: Map<String?, String?>?, attributes: Map<String?, Object?>?, os: OutputStream?, timeout: Long, register: Boolean): PageContext

    fun releasePageContext(pc: PageContext?, unregister: Boolean)

    @Throws(ServletException::class)
    fun createConfig(contextRoot: File?, host: String?, scriptName: String?): ConfigWeb
    val videoUtil: VideoUtil
    val zipUtil: ZipUtil

    @Throws(IOException::class, JspException::class, ServletException::class)
    fun cli(config: Map<String?, String?>?, servletConfig: ServletConfig?)
    fun registerThreadPageContext(pc: PageContext?)

    @Throws(PageException::class)
    fun getConfigServer(password: Password?): ConfigServer

    @Throws(PageException::class)
    fun getConfigServer(key: String?, timeNonce: Long): ConfigServer
    fun uptime(): Long
    val servletConfigs: Array<Any>

    /*
	 * get the OSGi Bundle of the core
	 * 
	 * @return / public abstract Bundle getCoreBundle();
	 */
    val bundleCollection: BundleCollection
    val bundleContext: BundleContext
    fun getScriptEngineFactory(dialect: Int): ScriptEngineFactory
    fun getTagEngineFactory(dialect: Int): ScriptEngineFactory
    val templateUtil: TemplateUtil
    val systemUtil: SystemUtil

    companion object {
        const val DIALECT_LUCEE = 0
        const val DIALECT_CFML = 1
        const val DIALECT_BOTH = 3

        /**
         * Field `CAN_UPDATE`
         */
        const val CAN_UPDATE = 0

        /**
         * Field `CAN_RESTART`
         */
        const val CAN_RESTART = 1
        const val CAN_RESTART_ALL = CAN_RESTART
        const val CAN_RESTART_CONTEXT = 2
    }
}