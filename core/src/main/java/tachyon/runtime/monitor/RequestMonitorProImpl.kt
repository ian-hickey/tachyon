package tachyon.runtime.monitor

import java.io.IOException

class RequestMonitorProImpl(monitor: RequestMonitor?) : RequestMonitorPro {
    private val monitor // do not change that name, used by Argus Monitor
            : RequestMonitor?
    private var init: Method? = null
    @Override
    fun getClazz(): Class? {
        return monitor.getClazz()
    }

    @Override
    fun getName(): String? {
        return monitor.getName()
    }

    @Override
    fun getType(): Short {
        return monitor.getType()
    }

    @Override
    fun init(cs: ConfigServer?, name: String?, logEnable: Boolean) {
        monitor.init(cs, name, logEnable)
    }

    @Override
    fun isLogEnabled(): Boolean {
        return monitor.isLogEnabled()
    }

    @Override
    @Throws(PageException::class)
    fun getData(config: ConfigWeb?, arguments: Map<String?, Object?>?): Query? {
        return monitor.getData(config, arguments)
    }

    /*
	 * (non-Javadoc)
	 * 
	 * @see tachyon.runtime.monitor.RequestMonitorPro#init(tachyon.runtime.PageContext)
	 */
    @Override
    @Throws(IOException::class)
    override fun init(pc: PageContext?) {
        if (init != null) {
            try {
                init.invoke(monitor, arrayOf<Object?>(pc))
            } catch (e: Exception) {
                throw ExceptionUtil.toIOException(e)
            }
        }
    }

    @Override
    @Throws(IOException::class)
    fun log(arg0: PageContext?, arg1: Boolean) {
        monitor.log(arg0, arg1)
    }

    init {
        this.monitor = monitor

        // do we have an init method?
        try {
            init = monitor.getClass().getDeclaredMethod("init", arrayOf<Class?>(PageContext::class.java))
        } catch (e: Exception) {
        }
    }
}