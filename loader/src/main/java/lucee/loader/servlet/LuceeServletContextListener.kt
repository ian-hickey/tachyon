package lucee.loader.servlet

import java.util.Enumeration

@WebListener
class LuceeServletContextListener : ServletContextListener {
    @Override
    fun contextInitialized(sce: ServletContextEvent) {
        try {
            val engine: CFMLEngine = CFMLEngineFactory.getInstance()
            // FUTURE add exeServletContextEvent
            engine.addServletConfig(LuceeServletContextListenerImpl(sce, "init"))
        } catch (se: Exception) {
            se.printStackTrace()
        }
    }

    @Override
    fun contextDestroyed(sce: ServletContextEvent) {
        try {
            val engine: CFMLEngine = CFMLEngineFactory.getInstance()
            // FUTURE add addServletContextEvent
            engine.addServletConfig(LuceeServletContextListenerImpl(sce, "release"))
        } catch (se: Exception) {
            se.printStackTrace()
        }
    }

    class LuceeServletContextListenerImpl(sce: ServletContextEvent, status: String) : ServletConfig {
        private val sce: ServletContextEvent
        private val status: String

        @get:Override
        val servletName: String
            get() = "LuceeServletContextListener"

        @get:Override
        val servletContext: ServletContext
            get() = sce.getServletContext()
        val servletContextEvent: ServletContextEvent
            get() = sce

        @Override
        fun getInitParameter(name: String?): String? {
            return if ("status".equalsIgnoreCase(name)) status else null
        }

        @get:Override
        val initParameterNames: Enumeration<String>
            get() {
                val set: HashSet<String> = HashSet<String>()
                set.add("status")
                return EnumerationWrapper<String>(set)
            }

        init {
            this.sce = sce
            this.status = status
        }
    }
}