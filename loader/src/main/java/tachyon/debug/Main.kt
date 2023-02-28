package tachyon.debug

import java.io.File

object Main {
    const val ARG_HOST = "LUCEE_DEBUG_HOST"
    const val ARG_PORT = "LUCEE_DEBUG_PORT"
    const val ARG_BASE = "LUCEE_DEBUG_BASE"
    const val ARG_WEBXML = "LUCEE_DEBUG_BASE"
    const val DEF_HOST = "localhost"
    const val DEF_PORT = "48080"
    const val DEF_BASE = "/workspace/test/TachyonDebugWebapp"
    @Throws(Exception::class)
    fun main(args: Array<String?>?) {
        var s: String
        System.setProperty("tachyon.controller.disabled", "true")
        var webxml = getSystemPropOrEnvVar(ARG_WEBXML, "")
        if (webxml.isEmpty()) webxml = Main::class.java.getResource("/debug/web.xml").getPath()
        s = getSystemPropOrEnvVar(ARG_BASE, DEF_BASE)
        val appBase: String = File(s).getCanonicalPath().replace('\\', '/')
        val docBase = "$appBase/webroot"
        System.out.println("Setting appBase: $appBase")
        System.out.println("Setting docBase: $docBase")
        val clsTomcat: Class = Class.forName("org.apache.catalina.startup.Tomcat")
        val tAddWebApp: Method = clsTomcat.getMethod("addWebapp", String::class.java, String::class.java)
        val tGetConnector: Method = clsTomcat.getMethod("getConnector")
        val tGetServer: Method = clsTomcat.getMethod("getServer")
        val tSetAddDefaultWebXmlToWebapp: Method = clsTomcat.getMethod("setAddDefaultWebXmlToWebapp", Boolean::class.javaPrimitiveType)
        val tSetBaseDir: Method = clsTomcat.getMethod("setBaseDir", String::class.java)
        val tSetHostname: Method = clsTomcat.getMethod("setHostname", String::class.java)
        val tSetPort: Method = clsTomcat.getMethod("setPort", Int::class.javaPrimitiveType)
        val tStart: Method = clsTomcat.getMethod("start")
        val clsContext: Class = Class.forName("org.apache.catalina.Context")
        val cSetAltDDName: Method = clsContext.getMethod("setAltDDName", String::class.java)
        val cSetLogEffectiveWebXml: Method = clsContext.getMethod("setLogEffectiveWebXml", Boolean::class.javaPrimitiveType)
        val cSetResourceOnlyServlets: Method = clsContext.getMethod("setResourceOnlyServlets", String::class.java)
        val clsServer: Class = Class.forName("org.apache.catalina.Server")
        val sAwait: Method = clsServer.getMethod("await")
        val oTomcat: Object = clsTomcat.newInstance()
        tSetBaseDir.invoke(oTomcat, appBase)
        s = getSystemPropOrEnvVar(ARG_HOST, DEF_HOST)
        tSetHostname.invoke(oTomcat, s)
        s = getSystemPropOrEnvVar(ARG_PORT, DEF_PORT)
        tSetPort.invoke(oTomcat, Integer.parseInt(s))
        tSetAddDefaultWebXmlToWebapp.invoke(oTomcat, false)
        val oContext: Object = tAddWebApp.invoke(oTomcat, "", docBase)
        cSetAltDDName.invoke(oContext, webxml)
        cSetLogEffectiveWebXml.invoke(oContext, true)
        cSetResourceOnlyServlets.invoke(oContext, "CFMLServlet")
        System.out.println(
                tGetConnector.invoke(oTomcat)
        )

        // tomcat.start()
        tStart.invoke(oTomcat)

        // tomcat.getServer()
        val oServer: Object = tGetServer.invoke(oTomcat)

        // server.await();
        sAwait.invoke(oServer)
    }

    /**
     * converts a System property format to its equivalent Environment variable, e.g. an input of
     * "tachyon.conf.name" will return "LUCEE_CONF_NAME"
     *
     * @param name the System property name
     * @return the equivalent Environment variable name
     */
    private fun convertSystemPropToEnvVar(name: String): String {
        return name.replace('.', '_').toUpperCase()
    }

    /**
     * returns a system setting by either a Java property name or a System environment variable
     *
     * @param name - either a lowercased Java property name (e.g. tachyon.controller.disabled) or an
     * UPPERCASED Environment variable name ((e.g. LUCEE_CONTROLLER_DISABLED))
     * @param defaultValue - value to return if the neither the property nor the environment setting was
     * found
     * @return - the value of the property referenced by propOrEnv or the defaultValue if not found
     */
    private fun getSystemPropOrEnvVar(name: String, defaultValue: String): String {
        // env
        var name = name
        var value: String = System.getenv(name)
        if (!isEmpty(value)) return value

        // prop
        value = System.getProperty(name)
        if (!isEmpty(value)) return value

        // env 2
        name = convertSystemPropToEnvVar(name)
        value = System.getenv(name)
        return if (!isEmpty(value)) value else defaultValue
    }

    private fun isEmpty(value: String?): Boolean {
        return value == null || value.isEmpty()
    }
}