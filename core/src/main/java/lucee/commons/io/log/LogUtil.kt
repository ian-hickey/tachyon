/**
 *
 * Copyright (c) 2014, the Railo Company Ltd. All rights reserved.
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
package lucee.commons.io.log

import java.io.File

/**
 * Helper class for the logs
 */
object LogUtil {
    fun toLevel(strLevel: String?, defaultValue: Int): Int {
        var strLevel = strLevel ?: return defaultValue
        strLevel = strLevel.toLowerCase().trim()
        if (strLevel.startsWith("info")) return Log.LEVEL_INFO
        if (strLevel.startsWith("debug")) return Log.LEVEL_DEBUG
        if (strLevel.startsWith("warn")) return Log.LEVEL_WARN
        if (strLevel.startsWith("error")) return Log.LEVEL_ERROR
        if (strLevel.startsWith("fatal")) return Log.LEVEL_FATAL
        return if (strLevel.startsWith("trace")) Log.LEVEL_TRACE else defaultValue
    }

    fun levelToString(level: Int, defaultValue: String): String {
        if (Log.LEVEL_INFO === level) return "info"
        if (Log.LEVEL_DEBUG === level) return "debug"
        if (Log.LEVEL_WARN === level) return "warn"
        if (Log.LEVEL_ERROR === level) return "error"
        if (Log.LEVEL_FATAL === level) return "fatal"
        return if (Log.LEVEL_TRACE === level) "trace" else defaultValue
    }

    val isAlreadyInLog: Boolean
        get() {
            val stes: Array<StackTraceElement> = Thread.currentThread().getStackTrace()
            if (stes != null) {
                var str: String
                for (ste in stes) {
                    str = ste.getClassName()
                    if (str.indexOf("org.apache.log4j.") === 0 || str.indexOf("org.apache.logging.log4j.") === 0 || str.indexOf("lucee.commons.io.log.log4j") === 0) return true
                }
            }
            return false
        }

    //////////
    fun log(level: Int, type: String, msg: String) {
        log(level, "application", type, msg)
    }

    fun log(pc: PageContext?, level: Int, type: String?, msg: String?) {
        log(pc, level, "application", type, msg)
    }

    fun log(config: Config?, level: Int, type: String?, msg: String?) {
        log(config, level, "application", type, msg)
    }

    //////////
    fun log(type: String?, t: Throwable?) {
        log("application", type, t)
    }

    fun log(pc: PageContext?, type: String?, t: Throwable?) {
        log(pc, "application", type, t)
    }

    fun log(config: Config?, type: String?, t: Throwable?) {
        log(config, "application", type, t, Log.LEVEL_ERROR)
    }

    //////////
    fun log(logName: String?, type: String, t: Throwable?) {
        log(logName, type, t, Log.LEVEL_ERROR)
    }

    fun log(pc: PageContext?, logName: String?, type: String?, t: Throwable?) {
        log(pc, logName, type, t, Log.LEVEL_ERROR)
    }

    //////////
    fun log(logName: String?, type: String, t: Throwable?, logLevel: Int) {
        val log: Log = ThreadLocalPageContext.getLog(logName)
        if (log != null) {
            if (Log.LEVEL_ERROR === logLevel) log.error(type, t) else log.log(logLevel, type, t)
        } else logGlobal(ThreadLocalPageContext.getConfig(), logLevel, type, ExceptionUtil.getStacktrace(t, true))
    }

    fun log(config: Config?, logName: String?, type: String, t: Throwable?, logLevel: Int) {
        val log: Log = ThreadLocalPageContext.getLog(config, logName)
        if (log != null) {
            if (Log.LEVEL_ERROR === logLevel) log.error(type, t) else log.log(logLevel, type, t)
        } else logGlobal(config, logLevel, type, ExceptionUtil.getStacktrace(t, true))
    }

    fun log(pc: PageContext?, logName: String?, type: String, t: Throwable?, logLevel: Int) {
        val log: Log = ThreadLocalPageContext.getLog(pc, logName)
        if (log != null) {
            if (Log.LEVEL_ERROR === logLevel) log.error(type, t) else log.log(logLevel, type, t)
        } else logGlobal(ThreadLocalPageContext.getConfig(pc), logLevel, type, ExceptionUtil.getStacktrace(t, true))
    }

    //////////
    fun log(level: Int, logName: String, type: String, msg: String) {
        val log: Log = ThreadLocalPageContext.getLog(logName)
        if (log != null) log.log(level, type, msg) else {
            logGlobal(ThreadLocalPageContext.getConfig(), level, "$logName:$type", msg)
        }
    }

    fun log(config: Config?, level: Int, logName: String, type: String, msg: String) {
        val log: Log = ThreadLocalPageContext.getLog(config, logName)
        if (log != null) log.log(level, type, msg) else {
            logGlobal(ThreadLocalPageContext.getConfig(config), level, "$logName:$type", msg)
        }
    }

    fun log(pc: PageContext?, level: Int, logName: String, type: String, msg: String) {
        val log: Log = ThreadLocalPageContext.getLog(pc, logName)
        if (log != null) log.log(level, type, msg) else {
            logGlobal(ThreadLocalPageContext.getConfig(pc), level, "$logName:$type", msg)
        }
    }

    fun logGlobal(config: Config?, level: Int, type: String, msg: String) {
        try {
            val engine: CFMLEngine = ConfigWebUtil.getEngine(config)
            val root: File = engine.getCFMLEngineFactory().getResourceRoot()
            val flog = File(root, "context/logs/" + (if (level > Log.LEVEL_DEBUG) "err" else "out") + ".log")
            val log: Resource = ResourceUtil.toResource(flog)
            if (!log.isFile()) {
                log.getParentResource().mkdirs()
                log.createNewFile()
            }
            IOUtil.write(log, SystemOut.FORMAT.format(Date(System.currentTimeMillis())).toString() + " " + type + " " + msg + "\n", CharsetUtil.UTF8, true)
        } catch (ioe: IOException) {
            aprint.e(ioe)
        }
    }

    fun logGlobal(config: Config?, type: String?, t: Throwable?) {
        logGlobal(config, Log.LEVEL_ERROR, type, ExceptionUtil.getStacktrace(t, true))
    }

    fun logGlobal(config: Config?, type: String, msg: String, t: Throwable?) {
        logGlobal(config, Log.LEVEL_ERROR, type, msg + ";" + ExceptionUtil.getStacktrace(t, true))
    }
}