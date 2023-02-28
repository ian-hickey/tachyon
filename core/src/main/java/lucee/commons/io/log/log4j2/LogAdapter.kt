package lucee.commons.io.log.log4j2

import java.lang.reflect.InvocationTargetException

class LogAdapter(logger: Logger, level: Level) : Log {
    private val logger: Logger
    private val level: Level
    fun validate() {
        if (logger is org.apache.logging.log4j.core.Logger && !logger.getLevel().equals(level)) {
            val cl: org.apache.logging.log4j.core.Logger = logger as org.apache.logging.log4j.core.Logger
            cl.setLevel(level)
        }
    }

    @Override
    fun log(level: Int, application: String, message: String) {
        logger.log(toLevel(level), merge(application, message))
    }

    @Override
    fun log(level: Int, application: String, message: String, t: Throwable?) {
        if (StringUtil.isEmpty(message)) logger.log(toLevel(level), application, t) else logger.log(toLevel(level), merge(application, message), t)
    }

    @Override
    fun log(level: Int, application: String, t: Throwable) {
        var t = t
        t = toThrowable(t)
        var msg: String = t.getMessage()
        if (StringUtil.isEmpty(msg)) msg = t.getClass().getName()
        log(level, application, msg, t)
    }

    @Override
    fun trace(application: String, message: String) {
        log(Log.LEVEL_TRACE, application, message)
    }

    @Override
    fun info(application: String, message: String) {
        log(Log.LEVEL_INFO, application, message)
    }

    @Override
    fun debug(application: String, message: String) {
        log(Log.LEVEL_DEBUG, application, message)
    }

    @Override
    fun warn(application: String, message: String) {
        log(Log.LEVEL_WARN, application, message)
    }

    @Override
    fun error(application: String, message: String) {
        log(Log.LEVEL_ERROR, application, message)
    }

    @Override
    fun fatal(application: String, message: String) {
        log(Log.LEVEL_FATAL, application, message)
    }

    @Override
    fun error(application: String, t: Throwable) {
        log(LEVEL_ERROR, application, t)
    }

    @Override
    fun error(application: String, message: String, t: Throwable?) {
        log(LEVEL_ERROR, application, message, t)
    }

    @get:Override
    @set:Override
    var logLevel: Int
        get() = toLevel(logger.getLevel())
        set(level) {
            if (logger is org.apache.logging.log4j.core.Logger) {
                val cl: org.apache.logging.log4j.core.Logger = logger as org.apache.logging.log4j.core.Logger
                cl.setLevel(toLevel(level))
            } else {
                logger.atLevel(toLevel(level))
            }
        }

    fun getLogger(): Logger {
        return logger
    }

    private fun toThrowable(t: Throwable): Throwable {
        ExceptionUtil.rethrowIfNecessary(t)
        return if (t is InvocationTargetException) (t as InvocationTargetException).getTargetException() else t
    }

    private fun merge(application: String, message: String): String {
        return if (StringUtil.isEmpty(application)) message else "$application->$message"
    }

    companion object {
        fun toLevel(level: Int): Level {
            when (level) {
                Log.LEVEL_FATAL -> return Level.FATAL
                Log.LEVEL_ERROR -> return Level.ERROR
                Log.LEVEL_WARN -> return Level.WARN
                Log.LEVEL_DEBUG -> return Level.DEBUG
                Log.LEVEL_INFO -> return Level.INFO
                Log.LEVEL_TRACE -> return Level.TRACE
            }
            return Level.INFO
        }

        private fun toLevel(level: Level): Int {
            if (Level.FATAL.equals(level)) return Log.LEVEL_FATAL
            if (Level.ERROR.equals(level)) return Log.LEVEL_ERROR
            if (Level.WARN.equals(level)) return Log.LEVEL_WARN
            if (Level.DEBUG.equals(level)) return Log.LEVEL_DEBUG
            if (Level.INFO.equals(level)) return Log.LEVEL_INFO
            return if (Level.TRACE.equals(level)) Log.LEVEL_TRACE else Log.LEVEL_INFO
        }
    }

    init {
        this.logger = logger
        this.level = level
    }
}