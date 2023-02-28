/**
 * Copyright (c) 2014, the Railo Company Ltd.
 * Copyright (c) 2015, Lucee Assosication Switzerland
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
package lucee.runtime.debug

import java.io.IOException

/**
 * Class to debug the application
 */
class DebuggerImpl : Debugger {
    private val entries: Map<String?, DebugEntryTemplateImpl?>? = HashMap<String?, DebugEntryTemplateImpl?>()
    private var partEntries: Map<String?, DebugEntryTemplatePartImpl?>? = null
    private val snippetsMap: ResourceSnippetsMap? = ResourceSnippetsMap(1024, 128)
    private val queries: List<QueryEntry?>? = ArrayList<QueryEntry?>()
    private val timers: List<DebugTimerImpl?>? = ArrayList<DebugTimerImpl?>()
    private val traces: List<DebugTraceImpl?>? = ArrayList<DebugTraceImpl?>()
    private val dumps: List<DebugDump?>? = ArrayList<DebugDump?>()
    private val exceptions: List<CatchBlock?>? = ArrayList<CatchBlock?>()
    private val implicitAccesses: Map<String?, ImplicitAccessImpl?>? = HashMap<String?, ImplicitAccessImpl?>()
    private var output = true
    private var lastEntry: Long = 0
    private var lastTrace: Long = 0
    private val historyId: Array? = ArrayImpl()
    private val historyLevel: Array? = ArrayImpl()
    private var starttime: Long = System.currentTimeMillis()
    private var outputLog: DebugOutputLog? = null
    private var genericData: Map<String?, Map<String?, List<String?>?>?>? = null
    private var abort: TemplateLine? = null
    private var outputContext: ApplicationException? = null
    private var queryTime: Long = 0
    private var threadName: String? = null
    @Override
    fun reset() {
        entries.clear()
        if (partEntries != null) partEntries.clear()
        queries.clear()
        implicitAccesses.clear()
        if (genericData != null) genericData.clear()
        timers.clear()
        traces.clear()
        dumps.clear()
        exceptions.clear()
        historyId.clear()
        historyLevel.clear()
        output = true
        outputLog = null
        abort = null
        outputContext = null
        queryTime = 0
        threadName = null
    }

    @Override
    fun getEntry(pc: PageContext?, source: PageSource?): DebugEntryTemplate? {
        return getEntry(pc, source, null)
    }

    // add pages entry
    @Override
    fun getEntry(pc: PageContext?, source: PageSource?, key: String?): DebugEntryTemplate? {
        lastEntry = System.currentTimeMillis()
        val src: String = DebugEntryTemplateImpl.getSrc(if (source == null) "" else source.getDisplayPath(), key)
        var de: DebugEntryTemplateImpl? = entries!![src]
        if (de != null) {
            de.countPP()
            try {
                historyId.appendEL(Caster.toInteger(de.getId()))
                historyLevel.appendEL(Caster.toInteger(pc.getCurrentLevel()))
            } catch (e: PageException) {
                historyId.appendEL(de.getId())
                historyLevel.appendEL(pc.getCurrentLevel())
            }
            return de
        }
        de = DebugEntryTemplateImpl(source, key)
        entries.put(src, de)
        try {
            historyId.appendEL(Caster.toInteger(de.getId()))
            historyLevel.appendEL(Caster.toInteger(pc.getCurrentLevel()))
        } catch (e: PageException) {
            historyId.appendEL(de.getId())
            historyLevel.appendEL(pc.getCurrentLevel())
        }
        return de
    }

    // add page parts entry
    @Override
    fun getEntry(pc: PageContext?, source: PageSource?, startPos: Int, endPos: Int): DebugEntryTemplatePart? {
        val src: String = DebugEntryTemplatePartImpl.getSrc(if (source == null) "" else source.getDisplayPath(), startPos, endPos)
        var de: DebugEntryTemplatePartImpl? = null
        if (partEntries != null) {
            de = partEntries!![src]
            if (de != null) {
                de.countPP()
                return de
            }
        } else {
            partEntries = HashMap<String?, DebugEntryTemplatePartImpl?>()
        }
        val snippet: ResourceSnippet = snippetsMap.getSnippet(source, startPos, endPos, (pc as PageContextImpl?).getResourceCharset().name())
        de = DebugEntryTemplatePartImpl(source, startPos, endPos, snippet.getStartLine(), snippet.getEndLine(), snippet.getContent())
        partEntries.put(src, de)
        return de
    }

    private fun toArray(): ArrayList<DebugEntryTemplate?>? {
        val arrPages: ArrayList<DebugEntryTemplate?> = ArrayList<DebugEntryTemplate?>(entries!!.size())
        val it: Iterator<String?> = entries.keySet().iterator()
        while (it.hasNext()) {
            val page: DebugEntryTemplate? = entries!![it.next()]
            page.resetQueryTime()
            arrPages.add(page)
        }
        Collections.sort(arrPages, DEBUG_ENTRY_TEMPLATE_COMPARATOR)

        // Queries
        val len: Int = queries!!.size()
        var entry: QueryEntry?
        for (i in 0 until len) {
            entry = queries[i]
            val path: String = entry.getSrc()
            val o: Object? = entries!![path]
            if (o != null) {
                val oe: DebugEntryTemplate? = o as DebugEntryTemplate?
                oe.updateQueryTime(entry.getExecutionTime())
            }
        }
        return arrPages
    }

    private fun _toDouble(value: Long): Double? {
        return if (value <= 0) ZERO else Double.valueOf(value)
    }

    private fun _toDouble(value: Int): Double? {
        return if (value <= 0) ZERO else Double.valueOf(value)
    }

    @Override
    fun addQuery(query: Query?, datasource: String?, name: String?, sql: SQL?, recordcount: Int, src: PageSource?, time: Int) {
        addQuery(query, datasource, name, sql, recordcount, src, time.toLong())
    }

    @Override
    fun addQuery(query: Query?, datasource: String?, name: String?, sql: SQL?, recordcount: Int, src: PageSource?, time: Long) {
        var tl: TemplateLine? = null
        if (src != null) tl = TemplateLine(src.getDisplayPath(), 0)
        queries.add(QueryResultEntryImpl(query as QueryResult?, datasource, name, sql, recordcount, tl, time))
    }

    fun addQuery(qr: QueryResult?, datasource: String?, name: String?, sql: SQL?, recordcount: Int, tl: TemplateLine?, time: Long) {
        queries.add(QueryResultEntryImpl(qr, datasource, name, sql, recordcount, tl, time))
    }

    fun addQuery(time: Long) {
        queryTime += time
    }

    @Override
    fun setOutput(output: Boolean) {
        setOutput(output, false)
    }

    fun setOutput(output: Boolean, listen: Boolean) {
        this.output = output
        if (listen) {
            outputContext = ApplicationException("")
        }
    }

    fun setThreadName(threadName: String?) {
        this.threadName = threadName
    }

    // FUTURE add to inzerface
    fun getOutput(): Boolean {
        return output
    }

    fun getOutputContext(): PageException? {
        return outputContext
    }

    @Override
    fun getQueries(): List<QueryEntry?>? {
        return queries
    }

    @Override
    @Throws(IOException::class)
    fun writeOut(pc: PageContext?) {
        // stop();
        if (!output) return
        val debugEntry: lucee.runtime.config.DebugEntry = getDebugEntry(pc)
                ?: // pc.forceWrite(pc.getConfig().getDefaultDumpWriter().toString(pc,toDumpData(pc,
                // 9999,DumpUtil.toDumpProperties()),true));
                return
        val args: Struct = StructImpl()
        args.setEL(KeyConstants._custom, debugEntry.getCustom())
        try {
            args.setEL(KeyConstants._debugging, pc.getDebugger().getDebuggingData(pc))
        } catch (e1: PageException) {
        }
        try {
            var path: String = debugEntry.getPath()
            var arr: Array<PageSource?> = (pc as PageContextImpl?).getPageSources(path)
            var p: Page = PageSourceImpl.loadPage(pc, arr, null)

            // patch for old path
            var fullname: String = debugEntry.getFullname()
            if (p == null) {
                if (path != null) {
                    var changed = false
                    if (path.endsWith("/Modern.cfc") || path.endsWith("\\Modern.cfc")) {
                        path = "/lucee-server-context/admin/debug/Modern.cfc"
                        fullname = "lucee-server-context.admin.debug.Modern"
                        changed = true
                    } else if (path.endsWith("/Classic.cfc") || path.endsWith("\\Classic.cfc")) {
                        path = "/lucee-server-context/admin/debug/Classic.cfc"
                        fullname = "lucee-server-context.admin.debug.Classic"
                        changed = true
                    } else if (path.endsWith("/Comment.cfc") || path.endsWith("\\Comment.cfc")) {
                        path = "/lucee-server-context/admin/debug/Comment.cfc"
                        fullname = "lucee-server-context.admin.debug.Comment"
                        changed = true
                    }
                    if (changed) pc.write(
                            "<span style='color:red'>Please update your debug template definitions in the Lucee admin by going into the detail view and hit the \"update\" button.</span>")
                }
                arr = (pc as PageContextImpl?).getPageSources(path)
                p = PageSourceImpl.loadPage(pc, arr)
            }
            pc.addPageSource(p.getPageSource(), true)
            try {
                val c: Component = pc.loadComponent(fullname)
                c.callWithNamedValues(pc, "output", args)
            } finally {
                pc.removeLastPageSource(true)
            }
        } catch (e: PageException) {
            pc.handlePageException(e)
        }
    }

    @Override
    @Throws(DatabaseException::class)
    fun getDebuggingData(pc: PageContext?): Struct? {
        return getDebuggingData(pc, false)
    }

    @Override
    @Throws(DatabaseException::class)
    fun getDebuggingData(pc: PageContext?, addAddionalInfo: Boolean): Struct? {
        val pci: PageContextImpl? = pc as PageContextImpl?
        val debugging: Struct = StructImpl()

        // datasources
        debugging.setEL(KeyConstants._datasources, DatasourceConnPool.meta((pc.getConfig() as ConfigPro).getDatasourceConnectionPools()))
        val ci: ConfigPro = ThreadLocalPageContext.getConfig(pc) as ConfigPro
        //////////////////////////////////////////
        //////// QUERIES ///////////////////////////
        //////////////////////////////////////////
        var queryTime: Long = 0
        if (ci.hasDebugOptions(ConfigPro.DEBUG_DATABASE)) {
            val queries: List<QueryEntry?>? = getQueries()
            var qryQueries: Query? = null
            try {
                qryQueries = QueryImpl(QUERY_COLUMNS, QUERY_COLUMN_TYPES, queries!!.size(), "query")
            } catch (e: DatabaseException) {
                qryQueries = QueryImpl(QUERY_COLUMNS, queries!!.size(), "query")
            }
            debugging.setEL(KeyConstants._queries, qryQueries)
            val qryExe: Struct = StructImpl()
            val qryIt: ListIterator<QueryEntry?> = queries!!.listIterator()
            var row = 0
            try {
                var qe: QueryEntry?
                while (qryIt.hasNext()) {
                    row++
                    qe = qryIt.next()
                    queryTime += qe.getExecutionTime()
                    qryQueries.setAt(KeyConstants._name, row, if (qe.getName() == null) "" else qe.getName())
                    qryQueries.setAt(KeyConstants._time, row, Long.valueOf(qe.getExecutionTime()))
                    qryQueries.setAt(KeyConstants._sql, row, qe.getSQL().toString())
                    if (qe is QueryResultEntryImpl) {
                        val tl: TemplateLine = (qe as QueryResultEntryImpl?)!!.getTemplateLine()
                        if (tl != null) {
                            qryQueries.setAt(KeyConstants._src, row, tl.template)
                            qryQueries.setAt(KeyConstants._line, row, tl.line)
                        }
                    } else qryQueries.setAt(KeyConstants._src, row, qe.getSrc())
                    qryQueries.setAt(KeyConstants._count, row, Integer.valueOf(qe.getRecordcount()))
                    qryQueries.setAt(KeyConstants._datasource, row, qe.getDatasource())
                    qryQueries.setAt(CACHE_TYPE, row, qe.getCacheType())
                    val usage: Struct? = getUsage(qe)
                    if (usage != null) qryQueries.setAt(KeyConstants._usage, row, usage)
                    val o: Object = qryExe.get(KeyImpl.init(qe.getSrc()), null)
                    if (o == null) qryExe.setEL(KeyImpl.init(qe.getSrc()), Long.valueOf(qe.getExecutionTime())) else qryExe.setEL(KeyImpl.init(qe.getSrc()), Long.valueOf((o as Long).longValue() + qe.getExecutionTime()))
                }
            } catch (dbe: PageException) {
            }
        } else {
            queryTime = this.queryTime
        }

        //////////////////////////////////////////
        //////// PAGES ///////////////////////////
        //////////////////////////////////////////
        var totalTime: Long = 0
        var arrPages: ArrayList<DebugEntryTemplate?>? = null
        if (ci.hasDebugOptions(ConfigPro.DEBUG_TEMPLATE)) {
            var row = 0
            arrPages = toArray()
            val len: Int = arrPages.size()
            val qryPage: Query = QueryImpl(PAGE_COLUMNS, len, "query")
            debugging.setEL(KeyConstants._pages, qryPage)
            if (len > 0) {
                try {
                    var de: DebugEntryTemplate
                    // PageSource ps;
                    for (i in 0 until len) {
                        row++
                        de = arrPages.get(i)
                        // ps = de.getPageSource();
                        totalTime += de.getFileLoadTime() + de.getExeTime()
                        qryPage.setAt(KeyConstants._id, row, Caster.toInteger(de.getId()))
                        qryPage.setAt(KeyConstants._count, row, _toDouble(de.getCount()))
                        qryPage.setAt(KeyConstants._min, row, _toDouble(de.getMin()))
                        qryPage.setAt(KeyConstants._max, row, _toDouble(de.getMax()))
                        qryPage.setAt(KeyConstants._avg, row, _toDouble(de.getExeTime() / de.getCount()))
                        qryPage.setAt(KeyConstants._app, row, _toDouble(de.getExeTime() - de.getQueryTime()))
                        qryPage.setAt(KeyConstants._load, row, _toDouble(de.getFileLoadTime()))
                        qryPage.setAt(KeyConstants._query, row, _toDouble(de.getQueryTime()))
                        qryPage.setAt(KeyConstants._total, row, _toDouble(de.getFileLoadTime() + de.getExeTime()))
                        qryPage.setAt(KeyConstants._src, row, de.getSrc())
                    }
                } catch (dbe: PageException) {
                }
            }
        } else {
            totalTime = if (pci.getEndTimeNS() > pci.getStartTimeNS()) pci.getEndTimeNS() - pci.getStartTimeNS() else 0
        }

        //////////////////////////////////////////
        //////// TIMES ///////////////////////////
        //////////////////////////////////////////
        val times: Struct = StructImpl()
        times.setEL(KeyConstants._total, Caster.toDouble(totalTime))
        times.setEL(KeyConstants._query, Caster.toDouble(queryTime))
        debugging.setEL(KeyConstants._times, times)

        //////////////////////////////////////////
        //////// PAGE PARTS ///////////////////////////
        //////////////////////////////////////////
        val hasParts = partEntries != null && !partEntries!!.isEmpty() && arrPages != null && !arrPages.isEmpty()
        var qrySize = 0
        var qryPart: Query? = null
        if (hasParts) {
            val slowestTemplate: String = arrPages.get(0).getPath()
            val filteredPartEntries: List<DebugEntryTemplatePart?> = ArrayList()
            val col: Collection<DebugEntryTemplatePartImpl?> = partEntries!!.values()
            for (detp in col) {
                if (detp.getPath().equals(slowestTemplate)) filteredPartEntries.add(detp)
            }
            qrySize = Math.min(filteredPartEntries.size(), MAX_PARTS)
            qryPart = QueryImpl(PAGE_PART_COLUMNS, qrySize, "query")
            debugging.setEL(PAGE_PARTS, qryPart)
            var row = 0
            Collections.sort(filteredPartEntries, DEBUG_ENTRY_TEMPLATE_PART_COMPARATOR)
            var parts: Array<DebugEntryTemplatePart?>? = arrayOfNulls<DebugEntryTemplatePart?>(qrySize)
            if (filteredPartEntries.size() > MAX_PARTS) parts = filteredPartEntries.subList(0, MAX_PARTS).toArray(parts) else parts = filteredPartEntries.toArray(parts)
            try {
                var de: DebugEntryTemplatePart?
                // PageSource ps;
                for (i in parts.indices) {
                    row++
                    de = parts!![i]
                    qryPart.setAt(KeyConstants._id, row, Caster.toInteger(de.getId()))
                    qryPart.setAt(KeyConstants._count, row, _toDouble(de.getCount()))
                    qryPart.setAt(KeyConstants._min, row, _toDouble(de.getMin()))
                    qryPart.setAt(KeyConstants._max, row, _toDouble(de.getMax()))
                    qryPart.setAt(KeyConstants._avg, row, _toDouble(de.getExeTime() / de.getCount()))
                    qryPart.setAt(KeyConstants._start, row, _toDouble(de.getStartPosition()))
                    qryPart.setAt(KeyConstants._end, row, _toDouble(de.getEndPosition()))
                    qryPart.setAt(KeyConstants._total, row, _toDouble(de.getExeTime()))
                    qryPart.setAt(KeyConstants._path, row, de.getPath())
                    if (de is DebugEntryTemplatePartImpl) {
                        qryPart.setAt(KeyConstants._startLine, row, _toDouble((de as DebugEntryTemplatePartImpl?)!!.getStartLine()))
                        qryPart.setAt(KeyConstants._endLine, row, _toDouble((de as DebugEntryTemplatePartImpl?)!!.getEndLine()))
                        qryPart.setAt(KeyConstants._snippet, row, (de as DebugEntryTemplatePartImpl?)!!.getSnippet())
                    }
                }
            } catch (dbe: PageException) {
            }
        }

        //////////////////////////////////////////
        //////// EXCEPTIONS ///////////////////////////
        //////////////////////////////////////////
        if (ci.hasDebugOptions(ConfigPro.DEBUG_EXCEPTION)) {
            val len = exceptions?.size() ?: 0
            val arrExceptions: Array = ArrayImpl()
            debugging.setEL(KeyConstants._exceptions, arrExceptions)
            if (len > 0) {
                val it: Iterator<CatchBlock?> = exceptions!!.iterator()
                while (it.hasNext()) {
                    arrExceptions.appendEL(it.next())
                }
            }
        }

        //////////////////////////////////////////
        //////// GENERIC DATA ///////////////////////////
        //////////////////////////////////////////
        var qryGenData: Query? = null
        val genData = getGenericData()
        if (genData != null && genData.size() > 0) {
            qryGenData = QueryImpl(GEN_DATA_COLUMNS, 0, "query")
            debugging.setEL(GENERIC_DATA, qryGenData)
            val it: Iterator<Entry<String?, Map<String?, List<String?>?>?>?> = genData.entrySet().iterator()
            var e: Entry<String?, Map<String?, List<String?>?>?>?
            var itt: Iterator<Entry<String?, List<String?>?>?>
            var ee: Entry<String?, List<String?>?>?
            var cat: String
            var r: Int
            var list: List<String?>
            var `val`: Object?
            while (it.hasNext()) {
                e = it.next()
                cat = e.getKey()
                itt = e.getValue().entrySet().iterator()
                while (itt.hasNext()) {
                    ee = itt.next()
                    r = qryGenData.addRow()
                    list = ee.getValue()
                    `val` = if (list.size() === 1) list[0] else ListUtil.listToListEL(list, ", ")
                    qryGenData.setAtEL(KeyConstants._category, r, cat)
                    qryGenData.setAtEL(KeyConstants._name, r, ee.getKey())
                    qryGenData.setAtEL(KeyConstants._value, r, `val`)
                }
            }
        }

        //////////////////////////////////////////
        //////// TIMERS ///////////////////////////
        //////////////////////////////////////////
        if (ci.hasDebugOptions(ConfigPro.DEBUG_TIMER)) {
            val len = timers?.size() ?: 0
            val qryTimers: Query = QueryImpl(TIMER_COLUMNS, len, "timers")
            debugging.setEL(KeyConstants._timers, qryTimers)
            if (len > 0) {
                try {
                    val it: Iterator<DebugTimerImpl?> = timers!!.iterator()
                    var timer: DebugTimer?
                    var row = 0
                    while (it.hasNext()) {
                        timer = it.next()
                        row++
                        qryTimers.setAt(KeyConstants._label, row, timer.getLabel())
                        qryTimers.setAt(KeyConstants._template, row, timer.getTemplate())
                        qryTimers.setAt(KeyConstants._time, row, Caster.toDouble(timer.getTime()))
                    }
                } catch (dbe: PageException) {
                }
            }
        }

        //////////////////////////////////////////
        //////// HISTORY ///////////////////////////
        //////////////////////////////////////////
        val history: Query = QueryImpl(arrayOf<Collection.Key?>(), 0, "history")
        debugging.setEL(KeyConstants._history, history)
        try {
            history.addColumn(KeyConstants._id, historyId)
            history.addColumn(KeyConstants._level, historyLevel)
        } catch (e: PageException) {
        }

        //////////////////////////////////////////
        //////// DUMPS ///////////////////////////
        //////////////////////////////////////////
        if (ci.hasDebugOptions(ConfigPro.DEBUG_DUMP)) {
            var len = dumps?.size() ?: 0
            if (!(pc.getConfig() as ConfigPro).hasDebugOptions(ConfigPro.DEBUG_DUMP)) len = 0
            var qryDumps: Query? = null
            qryDumps = QueryImpl(DUMP_COLUMNS, len, "dumps")
            debugging.setEL(KeyConstants._dumps, qryDumps)
            if (len > 0) {
                try {
                    val it: Iterator<DebugDump?> = dumps!!.iterator()
                    var dd: DebugDump?
                    var row = 0
                    while (it.hasNext()) {
                        dd = it.next()
                        row++
                        qryDumps.setAt(KeyConstants._output, row, dd.getOutput())
                        if (!StringUtil.isEmpty(dd.getTemplate())) qryDumps.setAt(KeyConstants._template, row, dd.getTemplate())
                        if (dd.getLine() > 0) qryDumps.setAt(KeyConstants._line, row, Double.valueOf(dd.getLine()))
                    }
                } catch (dbe: PageException) {
                }
            }
        }

        //////////////////////////////////////////
        //////// TRACES ///////////////////////////
        //////////////////////////////////////////
        if (ci.hasDebugOptions(ConfigPro.DEBUG_TRACING)) {
            var len = traces?.size() ?: 0
            if (!(pc.getConfig() as ConfigPro).hasDebugOptions(ConfigPro.DEBUG_TRACING)) len = 0
            var qryTraces: Query? = null
            qryTraces = QueryImpl(TRACES_COLUMNS, len, "traces")
            debugging.setEL(KeyConstants._traces, qryTraces)
            if (len > 0) {
                try {
                    val it: Iterator<DebugTraceImpl?> = traces!!.iterator()
                    var trace: DebugTraceImpl?
                    var row = 0
                    while (it.hasNext()) {
                        trace = it.next()
                        row++
                        qryTraces.setAt(KeyConstants._type, row, DebugTraceImpl.toType(trace!!.getType(), "INFO"))
                        if (!StringUtil.isEmpty(trace!!.getCategory())) qryTraces.setAt(KeyConstants._category, row, trace!!.getCategory())
                        if (!StringUtil.isEmpty(trace!!.getText())) qryTraces.setAt(KeyConstants._text, row, trace!!.getText())
                        if (!StringUtil.isEmpty(trace!!.getTemplate())) qryTraces.setAt(KeyConstants._template, row, trace!!.getTemplate())
                        if (trace!!.getLine() > 0) qryTraces.setAt(KeyConstants._line, row, Double.valueOf(trace!!.getLine()))
                        if (!StringUtil.isEmpty(trace!!.getAction())) qryTraces.setAt(KeyConstants._action, row, trace!!.getAction())
                        if (!StringUtil.isEmpty(trace!!.getVarName())) qryTraces.setAt(KeyImpl.getInstance("varname"), row, trace!!.getVarName())
                        if (!StringUtil.isEmpty(trace!!.getVarValue())) qryTraces.setAt(KeyImpl.getInstance("varvalue"), row, trace!!.getVarValue())
                        qryTraces.setAt(KeyConstants._time, row, Double.valueOf(trace!!.getTime()))
                    }
                } catch (dbe: PageException) {
                }
            }
        }

        //////////////////////////////////////////
        //////// SCOPE ACCESS ////////////////////
        //////////////////////////////////////////
        if (ci.hasDebugOptions(ConfigPro.DEBUG_IMPLICIT_ACCESS)) {
            val len = implicitAccesses?.size() ?: 0
            val qryImplicitAccesseses: Query = QueryImpl(IMPLICIT_ACCESS_COLUMNS, len, "implicitAccess")
            debugging.setEL(IMPLICIT_ACCESS, qryImplicitAccesseses)
            if (len > 0) {
                try {
                    val it: Iterator<ImplicitAccessImpl?> = implicitAccesses!!.values().iterator()
                    var das: ImplicitAccessImpl?
                    var row = 0
                    while (it.hasNext()) {
                        das = it.next()
                        row++
                        qryImplicitAccesseses.setAt(KeyConstants._template, row, das!!.getTemplate())
                        qryImplicitAccesseses.setAt(KeyConstants._line, row, Double.valueOf(das!!.getLine()))
                        qryImplicitAccesseses.setAt(KeyConstants._scope, row, das!!.getScope())
                        qryImplicitAccesseses.setAt(KeyConstants._count, row, Double.valueOf(das!!.getCount()))
                        qryImplicitAccesseses.setAt(KeyConstants._name, row, das!!.getName())
                    }
                } catch (dbe: PageException) {
                }
            }
        }

        //////////////////////////////////////////
        //////// ABORT /////////////////////////
        //////////////////////////////////////////
        if (abort != null) {
            val sct: Struct = StructImpl()
            sct.setEL(KeyConstants._template, abort.template)
            sct.setEL(KeyConstants._line, Double.valueOf(abort.line))
            debugging.setEL(KeyConstants._abort, sct)
        }

        //////////////////////////////////////////
        //////// SCOPES /////////////////////////
        //////////////////////////////////////////
        if (addAddionalInfo) {
            val scopes: Struct = StructImpl()
            scopes.setEL(KeyConstants._cgi, pc.cgiScope())
            debugging.setEL(KeyConstants._scope, scopes)
        }

        //////////////////////////////////////////
        //////// THREAD NAME ////////////////////
        //////////////////////////////////////////
        if (threadName != null) debugging.setEL(KeyImpl.getInstance("threadName"), threadName)
        val rsp: HttpServletResponse = pc.getHttpServletResponse()
        debugging.setEL(KeyImpl.getInstance("statusCode"), rsp.getStatus())
        debugging.setEL(KeyImpl.getInstance("contentType"), rsp.getContentType())
        // TODO ContentLength ReqRspUtil?
        debugging.setEL(KeyImpl.getInstance("starttime"), DateTimeImpl(starttime, false))
        debugging.setEL(KeyConstants._id, pci.getRequestId().toString() + "-" + pci.getId())
        return debugging
    }

    fun setAbort(abort: TemplateLine?) {
        this.abort = abort
    }

    fun getAbort(): TemplateLine? {
        return abort
    }

    @Override
    fun addTimer(label: String?, time: Long, template: String?): DebugTimer? {
        var t: DebugTimerImpl?
        timers.add(DebugTimerImpl(label, time, template).also { t = it })
        return t
    }

    @Override
    fun addTrace(type: Int, category: String?, text: String?, ps: PageSource?, varName: String?, varValue: String?): DebugTrace? {
        val _lastTrace = if (traces!!.isEmpty()) lastEntry else lastTrace
        lastTrace = System.currentTimeMillis()
        val t = DebugTraceImpl(type, category, text, if (ps == null) "unknown template" else ps.getDisplayPath(), SystemUtil.getCurrentContext(null).line, "", varName,
                varValue, lastTrace - _lastTrace)
        traces.add(t)
        return t
    }

    @Override
    fun addDump(ps: PageSource?, dump: String?): DebugDump? {
        val dt: DebugDump = DebugDumpImpl(ps.getDisplayPath(), SystemUtil.getCurrentContext(null).line, dump)
        dumps.add(dt)
        return dt
    }

    @Override
    fun addTrace(type: Int, category: String?, text: String?, template: String?, line: Int, action: String?, varName: String?, varValue: String?): DebugTrace? {
        val _lastTrace = if (traces!!.isEmpty()) lastEntry else lastTrace
        lastTrace = System.currentTimeMillis()
        val t = DebugTraceImpl(type, category, text, template, line, action, varName, varValue, lastTrace - _lastTrace)
        traces.add(t)
        return t
    }

    @Override
    fun getTraces(): Array<DebugTrace?>? {
        return getTraces(ThreadLocalPageContext.get())
    }

    @Override
    fun getTraces(pc: PageContext?): Array<DebugTrace?>? {
        return if (pc != null && (pc.getConfig() as ConfigPro).hasDebugOptions(ConfigPro.DEBUG_TRACING)) traces.toArray(arrayOfNulls<DebugTrace?>(traces!!.size())) else arrayOfNulls<DebugTrace?>(0)
    }

    @Override
    fun addException(config: Config?, pe: PageException?) {
        if (exceptions!!.size() > 1000) return
        try {
            exceptions.add((pe as PageExceptionImpl?).getCatchBlock(config))
        } catch (t: Throwable) {
            ExceptionUtil.rethrowIfNecessary(t)
        }
    }

    @Override
    fun getExceptions(): Array<CatchBlock?>? {
        return exceptions.toArray(arrayOfNulls<CatchBlock?>(exceptions!!.size()))
    }

    @Override
    fun init(config: Config?) {
        starttime = System.currentTimeMillis() + config.getTimeServerOffset()
    }

    @Override
    fun addImplicitAccess(scope: String?, name: String?) {
        addImplicitAccess(null, scope, name)
    }

    // FUTURE add to interface
    fun addImplicitAccess(pc: PageContext?, scope: String?, name: String?) {
        if (implicitAccesses!!.size() > 1000) return
        try {
            val tl: SystemUtil.TemplateLine = SystemUtil.getCurrentContext(pc)
            val key: String = tl.toString(StringBuilder()).append(':').append(scope).append(':').append(name).toString()
            val dsc: ImplicitAccessImpl? = implicitAccesses[key]
            if (dsc != null) dsc.inc() else implicitAccesses.put(key, ImplicitAccessImpl(scope, name, tl.template, tl.line))
        } catch (t: Throwable) {
            ExceptionUtil.rethrowIfNecessary(t)
        }
    }

    @Override
    fun getImplicitAccesses(scope: Int, name: String?): Array<ImplicitAccess?>? {
        return implicitAccesses!!.values().toArray(arrayOfNulls<ImplicitAccessImpl?>(implicitAccesses.size()))
    }

    @Override
    fun setOutputLog(outputLog: DebugOutputLog?) {
        this.outputLog = outputLog
    }

    fun getOutputTextFragments(): Array<DebugTextFragment?>? {
        return outputLog.getFragments()
    }

    @Throws(DatabaseException::class)
    fun getOutputText(): Query? {
        val fragments: Array<DebugTextFragment?> = outputLog.getFragments()
        val len = fragments?.size ?: 0
        val qryOutputLog: Query = QueryImpl(arrayOf<Collection.Key?>(KeyConstants._line, KeyConstants._template, KeyConstants._text), len, "query")
        if (len > 0) {
            for (i in fragments.indices) {
                qryOutputLog.setAtEL(KeyConstants._line, i + 1, fragments[i].getLine())
                qryOutputLog.setAtEL(KeyConstants._template, i + 1, fragments[i].getTemplate())
                qryOutputLog.setAtEL(KeyConstants._text, i + 1, fragments[i].getText())
            }
        }
        return qryOutputLog
    }

    fun resetTraces() {
        traces.clear()
    }

    @Override
    fun addGenericData(labelCategory: String?, data: Map<String?, String?>?) {
        // init generic data if necessary
        if (genericData == null) genericData = ConcurrentHashMap<String?, Map<String?, List<String?>?>?>()

        // category
        var cat = genericData!![labelCategory]
        if (cat == null) genericData.put(labelCategory, ConcurrentHashMap<String?, List<String?>?>().also { cat = it })

        // data
        val it: Iterator<Entry<String?, String?>?> = data.entrySet().iterator()
        var e: Entry<String?, String?>?
        var entry: List<String?>?
        while (it.hasNext()) {
            e = it.next()
            entry = cat!![e.getKey()]
            if (entry == null) {
                cat.put(e.getKey(), ArrayList<String?>().also { entry = it })
            }
            entry.add(e.getValue())
        }
    }

    /*
	 * private List<String> createAndFillList(Map<String, List<String>> cat) { Iterator<List<String>> it
	 * = cat.values().iterator(); int size=0; while(it.hasNext()){ size=it.next().size(); break; }
	 * ArrayList<String> list = new ArrayList<String>();
	 * 
	 * // fill with empty values to be on the same level as other columns for(int
	 * i=0;i<size;i++)list.add("");
	 * 
	 * return list; }
	 */
    @Override
    fun getGenericData(): Map<String?, Map<String?, List<String?>?>?>? {
        return genericData
    }

    companion object {
        private const val serialVersionUID = 3957043879267494311L
        private val IMPLICIT_ACCESS: Collection.Key? = KeyImpl.getInstance("implicitAccess")
        private val GENERIC_DATA: Collection.Key? = KeyImpl.getInstance("genericData")
        private val PAGE_PARTS: Collection.Key? = KeyImpl.getInstance("pageParts")

        // private static final Collection.Key OUTPUT_LOG= KeyImpl.intern("outputLog");
        private const val MAX_PARTS = 100
        val DEBUG_ENTRY_TEMPLATE_COMPARATOR: Comparator? = DebugEntryTemplateComparator()
        val DEBUG_ENTRY_TEMPLATE_PART_COMPARATOR: Comparator? = DebugEntryTemplatePartComparator()
        private val CACHE_TYPE: Key? = KeyImpl.getInstance("cacheType")
        private val PAGE_COLUMNS: Array<Key?>? = arrayOf<Collection.Key?>(KeyConstants._id, KeyConstants._count, KeyConstants._min, KeyConstants._max, KeyConstants._avg,
                KeyConstants._app, KeyConstants._load, KeyConstants._query, KeyConstants._total, KeyConstants._src)
        private val QUERY_COLUMNS: Array<Key?>? = arrayOf<Collection.Key?>(KeyConstants._name, KeyConstants._time, KeyConstants._sql, KeyConstants._src, KeyConstants._line,
                KeyConstants._count, KeyConstants._datasource, KeyConstants._usage, CACHE_TYPE)
        private val QUERY_COLUMN_TYPES: Array<String?>? = arrayOf("VARCHAR", "DOUBLE", "VARCHAR", "VARCHAR", "DOUBLE", "DOUBLE", "VARCHAR", "ANY", "VARCHAR")
        private val GEN_DATA_COLUMNS: Array<Key?>? = arrayOf<Collection.Key?>(KeyConstants._category, KeyConstants._name, KeyConstants._value)
        private val TIMER_COLUMNS: Array<Key?>? = arrayOf<Collection.Key?>(KeyConstants._label, KeyConstants._time, KeyConstants._template)
        private val DUMP_COLUMNS: Array<Key?>? = arrayOf<Collection.Key?>(KeyConstants._output, KeyConstants._template, KeyConstants._line)
        private val PAGE_PART_COLUMNS: Array<Key?>? = arrayOf<Collection.Key?>(KeyConstants._id, KeyConstants._count, KeyConstants._min, KeyConstants._max, KeyConstants._avg,
                KeyConstants._total, KeyConstants._path, KeyConstants._start, KeyConstants._end, KeyConstants._startLine, KeyConstants._endLine, KeyConstants._snippet)
        private val TRACES_COLUMNS: Array<Key?>? = arrayOf<Collection.Key?>(KeyConstants._type, KeyConstants._category, KeyConstants._text, KeyConstants._template, KeyConstants._line,
                KeyConstants._action, KeyConstants._varname, KeyConstants._varvalue, KeyConstants._time)
        private val IMPLICIT_ACCESS_COLUMNS: Array<Key?>? = arrayOf<Collection.Key?>(KeyConstants._template, KeyConstants._line, KeyConstants._scope, KeyConstants._count,
                KeyConstants._name)
        private val ZERO: Double? = Double.valueOf(0)
        fun debugQueryUsage(pageContext: PageContext?, qr: QueryResult?): Boolean {
            if (pageContext.getConfig().debug() && qr is Query) {
                if ((pageContext.getConfig() as ConfigWebPro).hasDebugOptions(ConfigPro.DEBUG_QUERY_USAGE)) {
                    (qr as Query?).enableShowQueryUsage()
                    return true
                }
            }
            return false
        }

        fun debugQueryUsage(pageContext: PageContext?, qry: Query?): Boolean {
            if (pageContext.getConfig().debug() && qry is Query) {
                if ((pageContext.getConfig() as ConfigWebPro).hasDebugOptions(ConfigPro.DEBUG_QUERY_USAGE)) {
                    qry.enableShowQueryUsage()
                    return true
                }
            }
            return false
        }

        /**
         * returns the DebugEntry for the current request's IP address, or null if no template matches the
         * address
         *
         * @param pc
         * @return
         */
        fun getDebugEntry(pc: PageContext?): lucee.runtime.config.DebugEntry? {
            val addr: String = pc.getHttpServletRequest().getRemoteAddr()
            return (pc.getConfig() as ConfigPro).getDebugEntry(addr, null)
        }

        @Throws(PageException::class)
        private fun getUsage(qe: QueryEntry?): Struct? {
            val qry: Query = qe.getQry()
            var c: QueryColumn
            var dqc: DebugQueryColumn
            outer@ if (qry != null) {
                var usage: Struct? = null
                val columnNames: Array<Collection.Key?> = qry.getColumnNames()
                var columnName: Collection.Key?
                for (i in columnNames.indices) {
                    columnName = columnNames[i]
                    c = qry.getColumn(columnName)
                    if (c !is DebugQueryColumn) break@outer
                    dqc = c as DebugQueryColumn
                    if (usage == null) usage = StructImpl()
                    usage.setEL(columnName, Caster.toBoolean(dqc.isUsed()))
                }
                return usage
            }
            return null
        }

        fun deprecated(pc: PageContext?, key: String?, msg: String?) {
            if (pc.getConfig().debug()) {
                // do we already have set?
                var exists = false
                val gd: Map<String?, Map<String?, List<String?>?>?> = pc.getDebugger().getGenericData()
                if (gd != null) {
                    val warning = gd["Warning"]
                    if (warning != null) {
                        exists = warning.containsKey(key)
                    }
                }
                if (!exists) {
                    val map: Map<String?, String?> = HashMap()
                    map.put(key, msg)
                    pc.getDebugger().addGenericData("Warning", map)
                }
            }
        }
    }
}

internal class DebugEntryTemplateComparator : Comparator<DebugEntryTemplate?> {
    @Override
    fun compare(de1: DebugEntryTemplate?, de2: DebugEntryTemplate?): Int {
        val result: Long = de2.getExeTime() + de2.getFileLoadTime() - (de1.getExeTime() + de1.getFileLoadTime())
        // we do this additional step to try to avoid ticket LUCEE-2076
        return if (result > 0L) 1 else if (result < 0L) -1 else 0
    }
}

internal class DebugEntryTemplatePartComparator : Comparator<DebugEntryTemplatePart?> {
    @Override
    fun compare(de1: DebugEntryTemplatePart?, de2: DebugEntryTemplatePart?): Int {
        val result: Long = de2.getExeTime() - de1.getExeTime()
        // we do this additional step to try to avoid ticket LUCEE-2076
        return if (result > 0L) 1 else if (result < 0L) -1 else 0
    }
}