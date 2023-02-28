package lucee.commons.io.log

import java.nio.charset.Charset

abstract class LogEngine {
    @Throws(PageException::class)
    abstract fun getConsoleLog(errorStream: Boolean, name: String?, level: Int): Log?
    @Throws(PageException::class)
    abstract fun getResourceLog(res: Resource?, charset: Charset?, name: String?, level: Int, timeout: Int, listener: RetireListener?, async: Boolean): Log?
    @Throws(PageException::class)
    abstract fun appenderClassDefintion(string: String?): ClassDefinition?
    @Throws(PageException::class)
    abstract fun layoutClassDefintion(string: String?): ClassDefinition?
    @Throws(PageException::class)
    abstract fun getLogger(config: Config?, appender: Object?, name: String?, level: Int): Log?
    @Throws(PageException::class)
    abstract fun getLayout(cd: ClassDefinition?, layoutArgs: Map<String?, String?>?, cdAppender: ClassDefinition?, name: String?): Object?
    @Throws(PageException::class)
    abstract fun getAppender(config: Config?, layout: Object?, name: String?, cd: ClassDefinition?, appenderArgs: Map<String?, String?>?): Object?
    @Throws(PageException::class)
    abstract fun closeAppender(appender: Object?)

    @get:Throws(PageException::class)
    abstract val defaultLayout: Object?

    @get:Throws(PageException::class)
    abstract val classicLayout: Object?
    abstract val version: String?

    companion object {
        fun newInstance(config: Config?): LogEngine {
            return Log4j2Engine(config)
        }
    }
}