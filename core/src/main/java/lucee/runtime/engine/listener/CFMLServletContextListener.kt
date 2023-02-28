package lucee.runtime.engine.listener

import javax.servlet.ServletContextEvent

class CFMLServletContextListener(engine: CFMLEngineImpl?) : ServletContextListener {
    private val engine: CFMLEngineImpl?
    @Override
    fun contextInitialized(sce: ServletContextEvent?) {
    }

    @Override
    fun contextDestroyed(sce: ServletContextEvent?) {
        engine.reset()
    }

    init {
        this.engine = engine
    }
}