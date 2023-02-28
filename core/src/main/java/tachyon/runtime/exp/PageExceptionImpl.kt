/**
 * Copyright (c) 2023, TachyonCFML.org
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
package tachyon.runtime.exp

import java.io.IOException

/**
 * Page Exception, all runtime Exception are sub classes of this class
 */
abstract class PageExceptionImpl : PageException {
    private val tagContext: Array? = ArrayImpl()
    private var additional: Struct? = StructImpl(Struct.TYPE_LINKED)

    /**
     * Field `detail`
     */
    protected var detail: String? = ""

    // private Throwable rootCause;
    @get:Override
    @set:Override
    var tracePointer = 0

    @get:Override
    @set:Override
    var errorCode: String? = "0"
        get() = if (field == null) "" else field

    @get:Override
    @set:Override
    var extendedInfo: String? = null
        get() = if (field == null) "" else field

    // for compatibility to ACF
    var typeAsString: String?
        private set
        @Override get() = field
    set
    private var customType: String? = null
    private var isInitTagContext = false
    private val sources: LinkedList<PageSource?>? = LinkedList<PageSource?>()
    private val varName: String? = null

    @get:Override
    @set:Override
    var exposeMessage = false

    /**
     * Class Constructor
     *
     * @param message Exception Message
     * @param type Type as String
     */
    constructor(message: String?, type: String?) : this(message, type, null) {}

    /**
     * Class Constructor
     *
     * @param message Exception Message
     * @param type Type as String
     * @param customType CUstom Type as String
     */
    constructor(message: String?, type: String?, customType: String?) : super(message ?: "") {
        // rootCause=this;
        typeAsString = type.toLowerCase().trim()
        this.customType = customType
        // setAdditional("customType",getCustomTypeAsString());
    }

    /**
     * Class Constructor
     *
     * @param e exception
     * @param type Type as String
     */
    constructor(e: Throwable?, type: String?) : super(if (StringUtil.isEmpty(e.getMessage(), true)) e.getClass().getName() else e.getMessage()) {
        var e = e
        if (e is InvocationTargetException) e = (e as InvocationTargetException?).getTargetException()

        // Throwable cause = e.getCause();
        // if(cause!=null)initCause(cause);
        initCause(e)
        setStackTrace(e.getStackTrace())
        if (e is IPageException) {
            val pe: IPageException? = e as IPageException?
            additional = pe.getAdditional()
            setDetail(pe.getDetail())
            errorCode = pe.getErrorCode()
            extendedInfo = pe.getExtendedInfo()
        }
        typeAsString = type.trim()
    }

    @Override
    fun getDetail(): String? {
        return if (detail == null || detail!!.equals(getMessage())) "" else detail
    }

    @Override
    fun setDetail(detail: String?) {
        this.detail = detail
    }

    val catchBlock: Struct?
        get() = getCatchBlock(ThreadLocalPageContext.getConfig())

    @Override
    fun getCatchBlock(pc: PageContext?): Struct? {
        return getCatchBlock(ThreadLocalPageContext.getConfig(pc))
    }

    @Override
    fun getCatchBlock(config: Config?): CatchBlock? {
        return CatchBlockImpl(this)
    }

    fun getTagContext(config: Config?): Array? {
        if (isInitTagContext) return tagContext
        _getTagContext(config, tagContext, getStackTraceElements(this), sources)
        isInitTagContext = true
        return tagContext
    }

    val pageDeep: Int
        get() {
            val traces: Array<StackTraceElement?>? = getStackTraceElements(this)
            var template = ""
            var tlast: String
            var trace: StackTraceElement? = null
            var index = 0
            for (i in traces.indices) {
                trace = traces!![i]
                tlast = template
                template = trace.getFileName()
                if (trace.getLineNumber() <= 0 || template == null || ResourceUtil.getExtension(template, "").equals("java")) continue
                if (!StringUtil.emptyIfNull(tlast).equals(template)) index++
            }
            return index
        }

    @Override
    fun getErrorBlock(pc: PageContext?, ep: ErrorPage?): Struct? {
        val struct: Struct = StructImpl()
        struct.setEL(KeyConstants._browser, pc.cgiScope().get("HTTP_USER_AGENT", ""))
        struct.setEL("datetime", DateTimeImpl(pc))
        struct.setEL("diagnostics", getMessage() + ' ' + getDetail() + "<br>The error occurred on line " + getLine(pc.getConfig()) + " in file " + getFile(pc.getConfig()) + ".")
        struct.setEL("GeneratedContent", getGeneratedContent(pc))
        struct.setEL("HTTPReferer", pc.cgiScope().get("HTTP_REFERER", ""))
        struct.setEL("mailto", ep.getMailto())
        struct.setEL(KeyConstants._message, getMessage())
        struct.setEL("QueryString", StringUtil.emptyIfNull(pc.getHttpServletRequest().getQueryString()))
        struct.setEL("RemoteAddress", pc.cgiScope().get("REMOTE_ADDR", ""))
        struct.setEL("RootCause", getCatchBlock(pc))
        struct.setEL("StackTrace", stackTraceAsString)
        struct.setEL(KeyConstants._template, pc.getHttpServletRequest().getServletPath())
        struct.setEL(KeyConstants._Detail, getDetail())
        struct.setEL("ErrorCode", errorCode)
        struct.setEL("ExtendedInfo", extendedInfo)
        struct.setEL(KeyConstants._type, typeAsString)
        struct.setEL("TagContext", getTagContext(pc.getConfig()))
        struct.setEL("additional", additional)
        // TODO RootCause,StackTrace
        return struct
    }

    private fun getGeneratedContent(pc: PageContext?): String? {
        val pci: PageContextImpl? = pc as PageContextImpl?
        val ro: CFMLWriter = pci.getRootOut()
        val gc: String = ro.toString()
        try {
            ro.clearBuffer()
        } catch (ioe: IOException) {
        }
        return gc ?: ""
    }

    /**
     * @return return the file where the failure occurred
     */
    private fun getFile(config: Config?): String? {
        if (getTagContext(config).size() === 0) return ""
        val sct: Struct = getTagContext(config).get(1, null) as Struct
        return Caster.toString(sct.get(KeyConstants._template, ""), "")
    }

    fun getLine(config: Config?): String? {
        if (getTagContext(config).size() === 0) return ""
        val sct: Struct = getTagContext(config).get(1, null) as Struct
        return Caster.toString(sct.get(KeyConstants._line, ""), "")
    }

    @Override
    fun addContext(ps: PageSource?, line: Int, column: Int, element: StackTraceElement?) {
        if (line == -187 && ps != null) {
            sources.add(ps)
            return
        }
        val struct: Struct = StructImpl()
        // print.out(pr.getDisplayPath());
        try {
            val content: Array<String?>? = if (ps == null) null else ps.getSource()
            struct.setEL(KeyConstants._template, if (ps == null) "" else ps.getDisplayPath())
            struct.setEL(KeyConstants._line, Double.valueOf(line))
            struct.setEL(KeyConstants._id, "??")
            struct.setEL(KeyConstants._Raw_Trace, if (element != null) element.toString() else "")
            struct.setEL(KeyConstants._Type, "cfml")
            struct.setEL(KeyConstants._column, Double.valueOf(column))
            if (content != null) {
                struct.setEL(KeyConstants._codePrintHTML, getCodePrint(content, line, true))
                struct.setEL(KeyConstants._codePrintPlain, getCodePrint(content, line, false))
            }
            tagContext.appendEL(struct)
        } catch (e: Exception) {
            LogUtil.logGlobal(ThreadLocalPageContext.getConfig(), PageException::class.java.getName(), e)
        }
    }

    @Override
    fun toDumpData(pageContext: PageContext?, maxlevel: Int, dp: DumpProperties?): DumpData? {

        // FFFFCF
        val htmlBox = DumpTable("exception", "#ff9900", "#FFCC00", "#000000")
        htmlBox.setTitle(
                Constants.NAME.toString() + " [" + pageContext.getConfig().getFactory().getEngine().getInfo().getVersion() + "] - Error (" + StringUtil.ucFirst(typeAsString) + ")")

        // Message
        htmlBox.appendRow(1, SimpleDumpData("Message"), SimpleDumpData(getMessage()))

        // Detail
        val detail = getDetail()
        if (!StringUtil.isEmpty(detail, true)) htmlBox.appendRow(1, SimpleDumpData("Detail"), SimpleDumpData(detail))

        // additional
        val it: Iterator<Key?> = additional.keyIterator()
        var k: Collection.Key?
        while (it.hasNext()) {
            k = it.next()
            htmlBox.appendRow(1, SimpleDumpData(k.getString()), SimpleDumpData(additional.get(k, "").toString()))
        }
        val tagContext: Array? = getTagContext(pageContext.getConfig())
        // Context MUSTMUST
        if (tagContext.size() > 0) {
            // Collection.Key[] keys=tagContext.keys();
            val vit: Iterator<Object?> = tagContext.valueIterator()
            // Entry<Key, Object> te;
            val context = DumpTable("#ff9900", "#FFCC00", "#000000")
            // context.setTitle("The Error Occurred in");
            // context.appendRow(0,new SimpleDumpData("The Error Occurred in"));
            context.appendRow(7, SimpleDumpData(""), SimpleDumpData("template"), SimpleDumpData("line"))
            try {
                var first = true
                while (vit.hasNext()) {
                    val struct: Struct? = vit.next() as Struct?
                    context.appendRow(1, SimpleDumpData(if (first) "called from " else "occurred in"), SimpleDumpData(struct.get(KeyConstants._template, "").toString() + ""),
                            SimpleDumpData(Caster.toString(struct.get(KeyConstants._line, null))))
                    first = false
                }
                htmlBox.appendRow(1, SimpleDumpData("Context"), context)

                // Code
                val strCode: String = (tagContext.get(1, null) as Struct).get(KeyConstants._codePrintPlain, "").toString()
                var arrCode: Array<String?> = ListUtil.listToStringArray(strCode, '\n')
                arrCode = ListUtil.trim(arrCode)
                val code = DumpTable("#ff9900", "#FFCC00", "#000000")
                for (i in arrCode.indices) {
                    code.appendRow(if (i == 2) 1 else 0, SimpleDumpData(arrCode[i]))
                }
                htmlBox.appendRow(1, SimpleDumpData("Code"), code)
            } catch (e: PageException) {
            }
        }

        // Java Stacktrace
        val strST = stackTraceAsString
        var arrST: Array<String?> = ListUtil.listToStringArray(strST, '\n')
        arrST = ListUtil.trim(arrST)
        val st = DumpTable("#ff9900", "#FFCC00", "#000000")
        for (i in arrST.indices) {
            st.appendRow(if (i == 0) 1 else 0, SimpleDumpData(arrST[i]))
        }
        htmlBox.appendRow(1, SimpleDumpData("Java Stacktrace"), st)
        return htmlBox
    }

    @get:Override
    val stackTraceAsString: String?
        get() = getStackTraceAsString(ThreadLocalPageContext.get())

    fun getStackTraceAsString(pc: PageContext?): String? {
        val sw = StringWriter()
        val pw = PrintWriter(sw)
        printStackTrace(pw)
        pw.flush()
        return sw.toString()
    }

    @Override
    fun printStackTrace() {
        printStackTrace(System.err)
    }

    fun printStackTrace(pc: PageContext?) {
        printStackTrace(System.err, pc)
    }

    @Override
    fun printStackTrace(s: PrintStream?) {
        printStackTrace(s, ThreadLocalPageContext.get())
    }

    fun printStackTrace(s: PrintStream?, pc: PageContext?) {
        super.printStackTrace(s)

        /*
		 * StackTraceElement[] traces = getStackTraceElements(this); StackTraceElement trace;
		 * 
		 * s.println(getMessage()); for(int i=0;i<traces.length;i++){ trace=traces[i];
		 * s.println("\tat "+toString(pc,trace)+":"+trace.getLineNumber()); }
		 */
    }

    @Override
    fun printStackTrace(s: PrintWriter?) {
        printStackTrace(s, ThreadLocalPageContext.get())
    }

    fun printStackTrace(s: PrintWriter?, pc: PageContext?) {
        super.printStackTrace(s)
        /*
		 * StackTraceElement[] traces = getStackTraceElements(this); StackTraceElement trace;
		 * 
		 * s.println(getMessage()); for(int i=0;i<traces.length;i++){ trace=traces[i];
		 * s.println("\tat "+toString(pc,trace)+":"+trace.getLineNumber()); }
		 */
    }
    /*
	 * ths code has produced duplettes private static void
	 * fillStackTraceElements(ArrayList<StackTraceElement> causes, Throwable t) { if(t==null) return;
	 * fillStackTraceElements(causes, t.getCause()); StackTraceElement[] traces = t.getStackTrace();
	 * for(int i=0;i<traces.length;i++) { //if(causes.contains(traces[i])) causes.add(traces[i]); } }
	 */
    /**
     * set an additional key value
     *
     * @param key
     * @param value
     */
    fun setAdditional(key: Collection.Key?, value: Object?) {
        additional.setEL(key, StringUtil.toStringEmptyIfNull(value))
    }

    @get:Override
    val rootCause: Throwable?
        get() {
            var cause: Throwable? = this
            var temp: Throwable?
            while (cause.getCause().also { temp = it } != null) cause = temp
            return cause
        }

    @Override
    fun typeEqual(type: String?): Boolean {
        var type = type ?: return true
        type = StringUtil.toUpperCase(type)
        // ANY
        if (type.equals("ANY")) return true // MUST check
        // Type Compare
        return if (typeAsString.equalsIgnoreCase(type)) true else getClass().getName().equalsIgnoreCase(type)
    }

    @get:Override
    val customTypeAsString: String?
        get() = if (customType == null) typeAsString else customType

    @Override
    fun getAdditional(): Struct? {
        return additional
    }

    @get:Override
    val addional: Struct?
        get() = additional

    @get:Override
    val stackTrace: Array<Any?>?
        get() = super.getStackTrace()

    companion object {
        private const val serialVersionUID = -5816929795661373219L
        fun getTagContext(config: Config?, traces: Array<StackTraceElement?>?): Array? {
            val tagContext: Array = ArrayImpl()
            _getTagContext(config, tagContext, traces, LinkedList<PageSource?>())
            return tagContext
        }

        private fun _getTagContext(config: Config?, tagContext: Array?, traces: Array<StackTraceElement?>?, sources: LinkedList<PageSource?>?) {
            // StackTraceElement[] traces = getStackTraceElements(t);
            var line = 0
            var template = ""
            var tlast: String
            var item: Struct?
            var trace: StackTraceElement? = null
            var index = -1
            var ps: PageSource?
            var pc: PageContextImpl? = null
            if (config is ConfigWeb) pc = ThreadLocalPageContext.get() as PageContextImpl
            for (i in traces.indices) {
                trace = traces!![i]
                tlast = template
                template = trace.getFileName()
                if (trace.getLineNumber() <= 0 || template == null || ResourceUtil.getExtension(template, "").equals("java")) continue
                // content
                if (!StringUtil.emptyIfNull(tlast).equals(template)) index++
                var content: Array<String?>? = null
                var dspPath = template
                try {
                    var res: Resource? = config.getResource(template)
                    if (!res.exists()) {
                        val _ps: PageSource? = if (pc == null) null else pc.getPageSource(template)
                        res = if (_ps == null) null else _ps.getPhyscalFile()
                        if (res == null || !res.exists()) {
                            res = config.getResource(_ps.getDisplayPath())
                            if (res != null && res.exists()) dspPath = res.getAbsolutePath()
                        } else dspPath = res.getAbsolutePath()
                    } else dspPath = res.getAbsolutePath()

                    // class was not build on the local filesystem
                    if (!res.exists()) {
                        val si: SourceInfo = if (pc != null) MappingUtil.getMatch(pc, trace) else MappingUtil.getMatch(config, trace)
                        if (si != null && si.relativePath != null) {
                            dspPath = si.relativePath
                            res = ResourceUtil.toResourceNotExisting(ThreadLocalPageContext.get(), si.relativePath, true, true)
                            if (!res.exists()) {
                                val _ps: PageSource = PageSourceImpl.best(config.getPageSources(ThreadLocalPageContext.get(), null, si.relativePath, false, false, true))
                                if (_ps != null && _ps.exists()) {
                                    res = _ps.getResource()
                                    if (res != null && res.exists()) dspPath = res.getAbsolutePath()
                                } else dspPath = res.getAbsolutePath()
                            } else dspPath = res.getAbsolutePath()
                        }
                    }
                    if (res.exists()) {
                        val `is`: InputStream = res.getInputStream()
                        content = if (ClassUtil.isBytecode(`is`)) {
                            arrayOf() // empty code array to show ??
                        } else IOUtil.toStringArray(IOUtil.getReader(res, config.getTemplateCharset()))
                        IOUtil.close(`is`)
                    } else {
                        ps = if (sources.size() > index) sources.get(index) else null
                        if (ps != null && trace.getClassName().equals(ps.getClassName())) {
                            if (ps.physcalExists()) content = IOUtil.toStringArray(IOUtil.getReader(ps.getPhyscalFile(), config.getTemplateCharset()))
                            template = ps.getDisplayPath()
                        }
                    }
                } catch (th: Throwable) {
                }

                // check last
                if (tagContext.size() > 0) {
                    try {
                        val last: Struct = tagContext.getE(tagContext.size()) as Struct
                        if (last.get(KeyConstants._Raw_Trace).equals(trace.toString())) continue
                    } catch (e: Exception) {
                    }
                }
                item = StructImpl()
                line = trace.getLineNumber()
                item.setEL(KeyConstants._template, dspPath)
                item.setEL(KeyConstants._line, Double.valueOf(line))
                item.setEL(KeyConstants._id, "??")
                item.setEL(KeyConstants._Raw_Trace, trace.toString())
                item.setEL(KeyConstants._type, "cfml")
                item.setEL(KeyConstants._column, Double.valueOf(0))
                if (content != null) {
                    if (content.size > 0) {
                        item.setEL(KeyConstants._codePrintHTML, getCodePrint(content, line, true))
                        item.setEL(KeyConstants._codePrintPlain, getCodePrint(content, line, false))
                    } else {
                        item.setEL(KeyConstants._codePrintHTML, "??")
                        item.setEL(KeyConstants._codePrintPlain, "??")
                    }
                } else {
                    item.setEL(KeyConstants._codePrintHTML, "")
                    item.setEL(KeyConstants._codePrintPlain, "")
                }
                // FUTURE id
                tagContext.appendEL(item)
            }
        }

        private fun getCodePrint(content: Array<String?>?, line: Int, asHTML: Boolean): String? {
            val sb = StringBuilder()
            // bad Line
            for (i in line - 2 until line + 3) {
                if (i > 0 && i <= content!!.size) {
                    if (asHTML && i == line) sb.append("<b>")
                    if (asHTML) sb.append(i.toString() + ": " + StringUtil.escapeHTML(content[i - 1])) else sb.append(i.toString() + ": " + content[i - 1])
                    if (asHTML && i == line) sb.append("</b>")
                    if (asHTML) sb.append("<br>")
                    sb.append('\n')
                }
            }
            return sb.toString()
        }

        fun toString(pc: PageContext?, trace: StackTraceElement?): String? {
            var path: String? = null
            if (trace.getFileName() == null || trace.getFileName().endsWith(".java")) return trace.toString()
            val config: Config = ThreadLocalPageContext.getConfig(pc)
            if (config != null) {
                var res: Resource = pc.getConfig().getResource(trace.getFileName())
                if (res.exists()) path = trace.getFileName()

                // get path from source
                if (path == null) {
                    val si: SourceInfo = MappingUtil.getMatch(pc, trace)
                    if (si != null) {
                        if (si.absolutePath(pc) != null) {
                            res = pc.getConfig().getResource(si.absolutePath(pc))
                            if (res.exists()) path = si.absolutePath(pc)
                        }
                        if (path == null && si.relativePath != null) path = si.relativePath
                    }
                    if (path == null) path = trace.getFileName()
                }
            }
            return trace.getClassName().toString() + "." + trace.getMethodName() + if (trace.isNativeMethod()) "(Native Method)" else if (path != null && trace.getLineNumber() >= 0) "(" + path + ":" + trace.getLineNumber() + ")" else if (path != null) "($path)" else "(Unknown Source)"
        }

        private fun getStackTraceElements(t: Throwable?): Array<StackTraceElement?>? {
            var st: Array<StackTraceElement?>? = getStackTraceElements(t, true)
            if (st == null) st = getStackTraceElements(t, false)
            return st
        }

        private fun getStackTraceElements(t: Throwable?, onlyWithCML: Boolean): Array<StackTraceElement?>? {
            var st: Array<StackTraceElement?>?
            val cause: Throwable = t.getCause()
            if (cause != null) {
                st = getStackTraceElements(cause, onlyWithCML)
                if (st != null) return st
            }
            st = t.getStackTrace()
            return if (!onlyWithCML || hasCFMLinStacktrace(st)) {
                st
            } else null
        }

        private fun hasCFMLinStacktrace(traces: Array<StackTraceElement?>?): Boolean {
            for (i in traces.indices) {
                if (traces!![i].getFileName() != null && !traces[i].getFileName().endsWith(".java")) return true
            }
            return false
        }
    }
}