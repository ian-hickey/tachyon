package tachyon.runtime.net.amf

import java.io.IOException

class AMFEngineDummy private constructor() : AMFEngine {
    @Override
    @Throws(IOException::class)
    fun init(config: ConfigWeb?, arguments: Map<String?, String?>?) {
        // do nothing
    }

    @Override
    @Throws(IOException::class)
    fun service(servlet: HttpServlet?, req: HttpServletRequest?, rsp: HttpServletResponse?) {
        throw notInstalledEL()
    }

    companion object {
        var instance: AMFEngine? = null
            get() {
                if (field == null) field = AMFEngineDummy()
                return field
            }
            private set

        fun notInstalled(): PageException? {
            return ApplicationException("No AMF Engine (Flex) installed!", "Check out the Extension Store in the Tachyon Administrator for \"Flex\".")
        }

        fun notInstalledEL(): PageRuntimeException? {
            return PageRuntimeException(notInstalled())
        }
    }
}