package tachyon.commons.io.log

import java.util.ArrayList

class LogBuffer : Log {
    private val datas: List<LogData> = ArrayList()

    @get:Override
    @set:Override
    var logLevel: Int = Log.LEVEL_TRACE
    @Override
    fun debug(application: String?, message: String?) {
        datas.add(LogData(Log.LEVEL_DEBUG, application, message, null))
    }

    @Override
    fun error(application: String?, message: String?) {
        datas.add(LogData(Log.LEVEL_ERROR, application, message, null))
    }

    @Override
    fun error(application: String?, exeption: Throwable?) {
        datas.add(LogData(Log.LEVEL_ERROR, application, null, exeption))
    }

    @Override
    fun error(application: String?, message: String?, exception: Throwable?) {
        datas.add(LogData(Log.LEVEL_ERROR, application, message, exception))
    }

    @Override
    fun fatal(application: String?, message: String?) {
        datas.add(LogData(Log.LEVEL_FATAL, application, message, null))
    }

    @Override
    fun info(application: String?, message: String?) {
        datas.add(LogData(Log.LEVEL_INFO, application, message, null))
    }

    @Override
    fun log(level: Int, application: String?, message: String?) {
        datas.add(LogData(level, application, message, null))
    }

    @Override
    fun log(level: Int, application: String?, exception: Throwable?) {
        datas.add(LogData(level, application, null, exception))
    }

    @Override
    fun log(level: Int, application: String?, message: String?, exception: Throwable?) {
        datas.add(LogData(level, application, message, exception))
    }

    @Override
    fun trace(application: String?, message: String?) {
        datas.add(LogData(Log.LEVEL_TRACE, application, message, null))
    }

    @Override
    fun warn(application: String?, message: String?) {
        datas.add(LogData(Log.LEVEL_WARN, application, message, null))
    }

    fun flush(config: Config?, logName: String?) {
        val log: Log
        log = try {
            ThreadLocalPageContext.getLog(config, logName)
        } catch (e: Exception) {
            return
        }
        for (data in datas) {
            if (data.exception != null) log.log(data.level, data.application, data.message, data.exception) else log.log(data.level, data.application, data.message)
        }
        datas.clear()
    }

    fun flush(log: Log) {
        for (data in datas) {
            if (data.exception != null) log.log(data.level, data.application, data.message, data.exception) else log.log(data.level, data.application, data.message)
        }
        datas.clear()
    }

    private class LogData(val level: Int, val application: String?, val message: String?, val exception: Throwable?)
}