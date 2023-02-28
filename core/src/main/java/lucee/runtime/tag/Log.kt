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
package lucee.runtime.tag

import java.nio.charset.Charset

/**
 * Writes a message to a log file.
 *
 *
 *
 */
class Log : TagImpl() {
    /**
     * If you omit the file attribute, specifies the standard log file in which to write the message.
     * Ignored if you specify a file attribute
     */
    var log = DEfAULT_LOG

    /** The message text to log.  */
    private var text: String? = null

    /** The type or severity of the message.  */
    private var type: Short = lucee.commons.io.log.Log.LEVEL_INFO

    /**   */
    private var file: String? = null
    private var exception: Throwable? = null

    /**
     * Specifies whether to log the application name if one has been specified in an application tag.
     */
    private var application = true
    private var charset: CharSet? = null
    private var async = false
    @Override
    fun release() {
        super.release()
        log = DEfAULT_LOG
        type = lucee.commons.io.log.Log.LEVEL_INFO
        file = null
        application = true
        charset = null
        exception = null
        text = null
        async = false
    }

    /**
     * set the value log If you omit the file attribute, specifies the standard log file in which to
     * write the message. Ignored if you specify a file attribute
     *
     * @param log value to set
     * @throws ApplicationException
     */
    @Throws(ApplicationException::class)
    fun setLog(log: String?) {
        if (StringUtil.isEmpty(log, true)) return
        this.log = log.trim()
        // throw new ApplicationException("invalid value for attribute log ["+log+"]","valid values are
        // [application, scheduler,console]");
    }

    /**
     * set the value text The message text to log.
     *
     * @param text value to set
     */
    fun setText(text: String?) {
        this.text = text
    }

    @Throws(PageException::class)
    fun setException(exception: Object?) {
        this.exception = Throw.toPageException(exception, null)
        if (this.exception == null) throw CasterException(exception, Exception::class.java)
    }

    /**
     * set the value type The type or severity of the message.
     *
     * @param type value to set
     * @throws ApplicationException
     */
    @Throws(ApplicationException::class)
    fun setType(type: String?) {
        var type = type
        type = type.toLowerCase().trim()
        if (type.equals("information")) this.type = lucee.commons.io.log.Log.LEVEL_INFO else if (type.equals("info")) this.type = lucee.commons.io.log.Log.LEVEL_INFO else if (type.equals("warning")) this.type = lucee.commons.io.log.Log.LEVEL_WARN else if (type.equals("warn")) this.type = lucee.commons.io.log.Log.LEVEL_WARN else if (type.equals("error")) this.type = lucee.commons.io.log.Log.LEVEL_ERROR else if (type.startsWith("fatal")) this.type = lucee.commons.io.log.Log.LEVEL_FATAL else if (type.startsWith("debug")) this.type = lucee.commons.io.log.Log.LEVEL_DEBUG else if (type.startsWith("trace")) this.type = lucee.commons.io.log.Log.LEVEL_TRACE else throw ApplicationException("Invalid value for attribute type [$type]", "valid values are [information, warning, error, fatal, debug]")
    }

    /**
     * set the value time Specifies whether to log the system time.
     *
     * @param time value to set
     * @throws ApplicationException
     */
    @Throws(ApplicationException::class)
    fun setTime(useTime: Boolean) {
        if (useTime) return
        throw ApplicationException("Attribute [time] for tag [log] is deprecated, only the value [true] is allowed")
    }

    /**
     * set the value file
     *
     * @param file value to set
     * @throws ApplicationException
     */
    @Throws(ApplicationException::class)
    fun setFile(file: String?) {
        var file = file
        if (StringUtil.isEmpty(file)) return
        if (file.indexOf('/') !== -1 || file.indexOf('\\') !== -1) throw ApplicationException(
                "Invalid value [$file] for the attribute [file] for tag [log], it must be a valid filename, file separators like [\\/] are not allowed")
        if (!file.endsWith(".log")) file += ".log"
        this.file = file
    }

    /**
     * set the value date Specifies whether to log the system date.
     *
     * @param date value to set
     * @throws ApplicationException
     */
    @Throws(ApplicationException::class)
    fun setDate(useDate: Boolean) {
        if (useDate) return
        throw ApplicationException("Attribute [date] for tag [log] is deprecated, only the value [true] is allowed")
    }

    /**
     * set the value thread Specifies whether to log the thread ID. The thread ID identifies which
     * internal service thread logged a message. Since a service thread normally services a CFML page
     * request to completion, then moves on to the next queued request, the thread ID serves as a rough
     * indication of which request logged a message. Leaving thread IDs turned on can help diagnose
     * patterns of server activity.
     *
     * @param thread value to set
     * @throws ApplicationException
     */
    @Throws(ApplicationException::class)
    fun setThread(thread: Boolean) {
        if (thread) return
        throw ApplicationException("Attribute [thread] for tag [log] is deprecated, only the value [true] is allowed")
    }

    /**
     * set the value application Specifies whether to log the application name if one has been specified
     * in an application tag.
     *
     * @param application value to set
     */
    fun setApplication(application: Boolean) {
        this.application = application
    }

    // old function for backward compatibility
    fun setSpoolenable(async: Boolean) {
        setAsync(async)
    }

    fun setAsync(async: Boolean) {
        this.async = async
    }

    @Override
    @Throws(PageException::class)
    fun doStartTag(): Int {
        if (text == null && exception == null) throw ApplicationException("Tag [log] requires one of the following attributes [text, exception]")
        val pci: PageContextImpl? = pageContext as PageContextImpl?
        var logger: lucee.commons.io.log.Log?
        if (file == null) {
            logger = pci.getLog(log.toLowerCase(), false)
            if (logger == null) {
                // for backward compatibility
                if ("console".equalsIgnoreCase(log)) logger = (pageContext.getConfig() as ConfigPro).getLogEngine().getConsoleLog(false, "cflog", lucee.commons.io.log.Log.LEVEL_INFO) else {
                    val set: Collection<String?> = pci.getLogNames()
                    val it = set.iterator()
                    val keys: Array<lucee.runtime.type.Collection.Key?> = arrayOfNulls<lucee.runtime.type.Collection.Key?>(set.size())
                    var index = 0
                    while (it.hasNext()) {
                        keys[index++] = KeyImpl.init(it.next())
                    }
                    val msg: String = ExceptionUtil.similarKeyMessage(keys, log, "attribute log", "log names", null, true)
                    val detail: String = ExceptionUtil.similarKeyMessage(keys, log, "log names", null, true)
                    throw ApplicationException(msg, detail)
                }
            }
        } else {
            logger = null
            // if we do have a log with the same name, we use the log
            var tmpName: String? = file.toLowerCase()
            if (tmpName.endsWith(".log")) tmpName = tmpName.substring(0, tmpName!!.length() - 4)
            logger = pci.getLog(tmpName, false)
            if (logger == null) logger = getFileLog(pageContext, file, charset, async)
        }
        var contextName: String = pageContext.getApplicationContext().getName()
        if (contextName == null || !application) contextName = ""
        if (exception != null) {
            if (StringUtil.isEmpty(text)) {
                logger.log(type, contextName, exception)
            } else {
                logger.log(type, contextName, text, exception)
            }
        } else if (!StringUtil.isEmpty(text)) {
            logger.log(type, contextName, text)
        } else throw ApplicationException("Tag [log] requires one of the following attributes [text, exception]")
        // logger.write(toStringType(type),contextName,text);
        return SKIP_BODY
    }

    /**
     * @param charset the charset to set
     */
    fun setCharset(charset: String?) {
        if (StringUtil.isEmpty(charset, true)) return
        this.charset = CharsetUtil.toCharSet(charset)
    }

    private class FileLogPool {
        fun retire(res: Resource?, charset: Charset?) {
            logs.remove(res.getAbsolutePath())
        }

        fun put(res: Resource?, charset: Charset?, log: lucee.commons.io.log.Log?) {
            logs.put(res.getAbsolutePath(), log)
        }

        operator fun get(res: Resource?, charset: Charset?): lucee.commons.io.log.Log? {
            return logs!![res.getAbsolutePath()]
        }

        companion object {
            val instance: FileLogPool? = FileLogPool()
            private val logs: Map<String?, lucee.commons.io.log.Log?>? = ConcurrentHashMap<String?, lucee.commons.io.log.Log?>()
            fun toKey(file: String?, charset: Charset?): String? {
                var charset: Charset? = charset
                if (charset == null) charset = CharsetUtil.UTF8
                return StringUtil.toVariableName(file).toString() + "." + StringUtil.toVariableName(charset.name())
            }
        }
    }

    private class Listener(private val pool: FileLogPool?, res: Resource?, charset: CharSet?) : RetireListener {
        private val res: Resource?
        private val charset: CharSet?
        @Override
        fun retire(os: RetireOutputStream?) {
            pool!!.retire(res, CharsetUtil.toCharset(charset))
        }

        init {
            this.res = res
            this.charset = charset
        }
    }

    companion object {
        private val DEfAULT_LOG: String? = "application"
        @Throws(PageException::class)
        private fun getFileLog(pc: PageContext?, file: String?, charset: CharSet?, async: Boolean): lucee.commons.io.log.Log? {
            var charset: CharSet? = charset
            val config: ConfigPro = pc.getConfig() as ConfigPro
            val logDir: Resource = config.getLogDirectory()
            val res: Resource = logDir.getRealResource(file)
            var log: lucee.commons.io.log.Log? = FileLogPool.instance!![res, CharsetUtil.toCharset(charset)]
            if (log != null) {
                log.setLogLevel(lucee.commons.io.log.Log.LEVEL_TRACE)
                return log
            }
            synchronized(FileLogPool.instance) {
                log = FileLogPool.instance[res, CharsetUtil.toCharset(charset)]
                if (log != null) {
                    log.setLogLevel(lucee.commons.io.log.Log.LEVEL_TRACE)
                    return log
                }
                if (charset == null) charset = CharsetUtil.toCharSet((pc as PageContextImpl?).getResourceCharset())
                log = config.getLogEngine().getResourceLog(res, CharsetUtil.toCharset(charset), "cflog." + FileLogPool.toKey(file, CharsetUtil.toCharset(charset)),
                        lucee.commons.io.log.Log.LEVEL_TRACE, 5, Listener(FileLogPool.instance, res, charset), async)
                FileLogPool.instance.put(res, CharsetUtil.toCharset(charset), log)
                return log
            }
        }
    }
}