package tachyon.commons.io.log.log4j2.layout

import java.io.ByteArrayOutputStream

class DataDogLayout : AbstractStringLayout(CharsetUtil.UTF8, ByteArray(0), ByteArray(0)) {
    private val format: DateFormat
    private val engine: CFMLEngine
    private val caster: Cast
    private var serializeJSONBIF: BIF? = null

    @get:Override
    val contentType: String
        get() = super.getContentType()

    @Override
    fun toSerializable(event: LogEvent): String {
        val data = StringBuilder()
        data.append(format.format(Date()))
        data.append(' ')
        data.append(event.getLevel().toString())
        data.append(' ')
        data.append(getLoggerName(event))
        data.append(':')
        data.append(caster.toString(lineNumber))
        data.append(" - ")
        val id: Array<Object>? = correlationIdentifier
        data.append(id!![0])
        data.append(' ')
        data.append(id[1])
        data.append(" - ")
        val application: String
        var msg: String? = caster.toString(event.getMessage(), null)
        val index: Int = msg.indexOf("->")
        if (index > -1) {
            application = msg.substring(0, index)
            msg = msg.substring(index + 2)
        } else application = ""
        // StringUtil.replace(application, "\"", "\"\"", false)

        // Message
        if (msg == null && event.getMessage() != null) msg = event.getMessage().toString()

        // Throwable
        val t: Throwable = event.getThrown()
        if (t != null) {
            val em = getMessage(t)
            if (!Util.isEmpty(em, true)) {
                if (!em.trim().equals(msg.trim())) msg += ";$em"
            }
            val sct: Struct = engine.getCreationUtil().createStruct()
            sct.setEL("message", msg)
            sct.setEL("stack", getStacktrace(t, false, true))
            sct.setEL("kind", t.getClass().getName())
            try {
                data.append(serializeJSON(sct))
            } catch (e: PageException) {
                data.append(msg)
            }
        } else data.append(msg)
        return data.append(LINE_SEPARATOR).toString()
    }

    private fun getLoggerName(event: LogEvent): Object {
        var name: String = event.getLoggerName()
        if (name.startsWith("web.")) {
            val index: Int = name.indexOf('.', 4)
            if (index != -1) name = name.substring(index + 1)
        } else if (name.startsWith("server.")) {
            name = name.substring(7)
        }
        return name
    }

    @Throws(PageException::class)
    private fun serializeJSON(sct: Struct): String {
        var release = false
        var pc: PageContext = engine.getThreadPageContext()
        if (pc == null) {
            try {
                pc = engine.createPageContext(
                        engine.getCastUtil().toFile(engine.getResourceUtil().getTempDirectory()),
                        "localhost",
                        "/",
                        "", arrayOfNulls<Cookie>(0),
                        null,
                        null,
                        null,
                        ByteArrayOutputStream(),
                        -1,
                        true
                )
                release = true
            } catch (e: Exception) {
                throw caster.toPageException(e)
            }
        }
        if (pc != null) {
            return try {
                if (serializeJSONBIF == null) {
                    serializeJSONBIF = engine.getClassUtil().loadBIF(pc, "tachyon.runtime.functions.conversion.SerializeJSON")
                }
                caster.toString(serializeJSONBIF.invoke(pc, arrayOf<Object>(sct)))
            } catch (e: Exception) {
                throw caster.toPageException(e)
            } finally {
                if (release) engine.releasePageContext(pc, true)
            }
        }
        throw engine.getExceptionUtil().createApplicationException("no PageContext available for the current thread and could not create one")
    }

    val lineNumber: Int
        get() {
            var line = 0
            var template: String
            for (trace in Thread.currentThread().getStackTrace()) {
                template = trace.getFileName()
                if (trace.getLineNumber() <= 0 || template == null || engine.getResourceUtil().getExtension(template, "").equals("java")) continue
                line = trace.getLineNumber()
                if (line > 0) return line
            }
            return 0
        }

    companion object {
        private val LINE_SEPARATOR: String = System.getProperty("line.separator")
        private val EMPTY_CLASS: Array<Class?> = arrayOfNulls<Class>(0)
        private val EMPTY_OBJ: Array<Object?> = arrayOfNulls<Object>(0)
        private var correlationIdentifierClass: Class<*>? = null
        private var getTraceId: Method? = null
        private var getSpanId: Method? = null
        private var ids: Array<Object>?
        private var idsTimestamp: Long = 0
        private var idsTries = 0
        private var idsValid = false// we cannot send this to a logger, because that could cause an infiniti loop// CorrelationIdentifier.getTraceId()

        // CorrelationIdentifier.getSpanId()
        // if we have less than 300 tries, we try once a second (so for 5 minutes) after that every minute
        private val correlationIdentifier: Array<Any>?
            private get() {
                if (idsValid) return ids
                val now: Long = System.currentTimeMillis()
                if (ids != null) {
                    // if we have less than 300 tries, we try once a second (so for 5 minutes) after that every minute
                    if (idsTries < 300 && idsTimestamp + 1000 > now) return ids
                    if (idsTries > 300 && idsTimestamp + 300000 > now) return ids
                }
                idsTries++
                idsTimestamp = now
                try {
                    if (correlationIdentifierClass == null) {
                        getTraceId = null
                        correlationIdentifierClass = CFMLEngineFactory.getInstance().getClassUtil().loadClass("datadog.trace.api.CorrelationIdentifier")
                    }

                    // CorrelationIdentifier.getTraceId()
                    if (getTraceId == null) {
                        getTraceId = correlationIdentifierClass.getMethod("getTraceId", EMPTY_CLASS)
                    }

                    // CorrelationIdentifier.getSpanId()
                    if (getSpanId == null) {
                        getSpanId = correlationIdentifierClass.getMethod("getSpanId", EMPTY_CLASS)
                    }
                    val tmp: Array<Object> = arrayOf<Object>(getTraceId.invoke(null, EMPTY_OBJ), getSpanId.invoke(null, EMPTY_OBJ))
                    if (!"0".equals(tmp[0])) {
                        ids = tmp
                        idsValid = true
                        return ids
                    }
                    return arrayOf("0", "0").also { ids = it }
                } catch (e: Exception) {
                    // we cannot send this to a logger, because that could cause an infiniti loop
                    try {
                        LogUtil.logGlobal(null, "datadog", e)
                    } catch (ee: Exception) {
                        e.printStackTrace()
                    }
                }
                return arrayOf("-1", "-1").also { ids = it }
            }

        fun getMessage(t: Throwable): String {
            var msg: String = t.getMessage()
            if (Util.isEmpty(msg, true)) msg = t.getClass().getName()
            val sb = StringBuilder(msg)
            if (t is PageException) {
                val pe: PageException = t as PageException
                val detail: String = pe.getDetail()
                if (!Util.isEmpty(detail, true)) {
                    sb.append('\n')
                    sb.append(detail)
                }
            }
            return sb.toString()
        }

        fun getStacktrace(t: Throwable, addMessage: Boolean, onlyTachyonPart: Boolean): String {
            val sw = StringWriter()
            val pw = PrintWriter(sw)
            t.printStackTrace(pw)
            pw.close()
            var st: String = sw.toString()
            // shrink the stacktrace
            if (onlyTachyonPart && st.indexOf("Caused by:") === -1) {
                var index: Int = st.indexOf("tachyon.loader.servlet.CFMLServlet.service(")
                if (index == -1) index = st.indexOf("tachyon.runtime.jsr223.ScriptEngineImpl.eval(")
                if (index != -1) {
                    index = st.indexOf(")", index + 1)
                    if (index != -1) {
                        st = st.substring(0, index + 1).toString() + "\n..."
                    }
                }
            }
            val msg: String = t.getMessage()
            if (addMessage && !Util.isEmpty(msg) && !st.startsWith(msg.trim())) st = """
     $msg
     $st
     """.trimIndent()
            return st
        }
    }

    init {
        engine = CFMLEngineFactory.getInstance()
        caster = engine.getCastUtil()
        format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
    }
}