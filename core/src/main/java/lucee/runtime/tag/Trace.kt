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

import java.io.IOException

class Trace : BodyTagImpl() {
    private var abort = false
    private var follow = false
    private var category: String? = null
    private var inline = false
    private var text: String? = null
    private var type: Int = Log.LEVEL_INFO
    private var `var`: String? = null
    private var caller: Struct? = null
    @Override
    fun release() {
        super.release()
        abort = false
        category = null
        inline = false
        text = null
        type = Log.LEVEL_INFO
        `var` = null
        caller = null
        follow = false
    }

    /**
     * @param abort the abort to set
     */
    fun setAbort(abort: Boolean) {
        this.abort = abort
    }

    fun setFollow(follow: Boolean) {
        this.follow = follow
    }

    /**
     * @param category the category to set
     */
    fun setCategory(category: String?) {
        this.category = category
    }

    /**
     * @param inline the inline to set
     */
    fun setInline(inline: Boolean) {
        this.inline = inline
    }

    /**
     * @param text the text to set
     */
    fun setText(text: String?) {
        this.text = text
    }

    /**
     * @param type the type to set
     * @throws ApplicationException
     */
    @Throws(ApplicationException::class)
    fun setType(strType: String?) {
        var strType = strType
        strType = strType.toLowerCase().trim()
        if ("info".equals(strType)) type = Log.LEVEL_INFO
        type = if ("information".equals(strType)) Log.LEVEL_INFO else if ("warn".equals(strType)) Log.LEVEL_WARN else if ("warning".equals(strType)) Log.LEVEL_WARN else if ("error".equals(strType)) Log.LEVEL_ERROR else if ("fatal information".equals(strType)) Log.LEVEL_FATAL else if ("fatal-information".equals(strType)) Log.LEVEL_FATAL else if ("fatal_information".equals(strType)) Log.LEVEL_FATAL else if ("fatalinformation".equals(strType)) Log.LEVEL_FATAL else if ("fatal info".equals(strType)) Log.LEVEL_FATAL else if ("fatal-info".equals(strType)) Log.LEVEL_FATAL else if ("fatal_info".equals(strType)) Log.LEVEL_FATAL else if ("fatalinfo".equals(strType)) Log.LEVEL_FATAL else if ("fatal".equals(strType)) Log.LEVEL_FATAL else if ("debug".equals(strType)) Log.LEVEL_DEBUG else if ("debugging".equals(strType)) Log.LEVEL_DEBUG else if ("debuging".equals(strType)) Log.LEVEL_DEBUG else if ("trace".equals(strType)) Log.LEVEL_TRACE else throw ApplicationException("invalid value [$strType] for attribute [type], valid values are [Debug, Information, Warning, Error, Fatal Information]")
    }

    /**
     * @param var the var to set
     */
    fun setVar(`var`: String?) {
        this.`var` = `var`
    }

    fun setCaller(caller: Struct?) {
        this.caller = caller
    }

    /**
     * @param var the var to set
     */
    fun setVariable(`var`: String?) {
        this.`var` = `var`
    }

    @Override
    fun doStartTag(): Int {
        return EVAL_BODY_INCLUDE
    }

    @Override
    @Throws(PageException::class)
    fun doEndTag(): Int {
        try {
            _doEndTag()
        } catch (e: IOException) {
        }
        return EVAL_PAGE
    }

    @Throws(IOException::class, PageException::class)
    fun _doEndTag() {
        val ps: PageSource = pageContext.getCurrentTemplatePageSource()

        // var
        var varValue: String? = null
        var value: Object? = null
        var traceValue: Object? = null
        if (!StringUtil.isEmpty(`var`)) {
            try {
                value = if (caller is Scope) VariableInterpreter.getVariable(pageContext, `var`, caller as Scope?) else pageContext.getVariable(`var`)
            } catch (e: PageException) {
                varValue = "(undefined)"
                follow = false
            }
            if (follow) {
                // print.o(1);
                if (StringUtil.isEmpty(text, true)) text = `var`
                // print.o(2);
                traceValue = TraceObjectSupport.toTraceObject(pageContext.getDebugger(), value, type, category, text)
                if (caller is Scope) VariableInterpreter.setVariable(pageContext, `var`, traceValue, caller as Scope?) else pageContext.setVariable(`var`, traceValue)
            }
            try {
                varValue = ScriptConverter().serialize(value)
            } catch (e: ConverterException) {
                if (value != null) varValue = "(" + Caster.toTypeName(value).toString() + ")"
            }
        }
        val trace: DebugTrace = (pageContext.getDebugger() as DebuggerImpl).addTrace(type, category, text, ps, `var`, varValue)
        val traces: Array<DebugTrace?> = pageContext.getDebugger().getTraces(pageContext)
        var total = "(1st trace)"
        if (traces.size > 1) {
            var t: Long = 0
            for (i in traces.indices) {
                t += traces[i].getTime()
            }
            total = "($t)"
        }
        val hasCat: Boolean = !StringUtil.isEmpty(trace.getCategory())
        val hasText: Boolean = !StringUtil.isEmpty(trace.getText())
        val hasVar: Boolean = !StringUtil.isEmpty(`var`)

        // inline
        if (inline) {
            val tf: lucee.runtime.format.TimeFormat = TimeFormat(pageContext.getConfig().getLocale())
            val sb = StringBuffer()
            sb.append("<table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" bgcolor=\"white\">")
            sb.append("<tr>")
            // sb.append("<td><img src=\"/CFIDE/debug/images/Error_16x16.gif\" alt=\"Error type\">");
            sb.append("<td>")
            sb.append("<font color=\"orange\">")
            sb.append("<b>")
            sb.append(DebugTraceImpl.toType(trace.getType(), "INFO").toString() + " - ")
            sb.append("[CFTRACE " + tf!!.format(DateTimeImpl(pageContext.getConfig()), "hh:mm:ss:l").toString() + "]")
            sb.append("[" + trace.getTime().toString() + " ms " + total + "]")
            sb.append("[" + trace.getTemplate().toString() + " @ line: " + trace.getLine().toString() + "]")
            if (hasCat || hasText) sb.append(" -")
            if (hasCat) sb.append("  [" + trace.getCategory().toString() + "]")
            if (hasText) sb.append(" <i>" + trace.getText().toString() + "&nbsp;</i>")
            sb.append("</b>")
            sb.append("</font>")
            sb.append("</td>")
            sb.append("</tr>")
            sb.append("</table>")
            pageContext.forceWrite(sb.toString())
            if (hasVar) Dump.call(pageContext, value, `var`)
        }

        // log
        val log: Log = ThreadLocalPageContext.getLog(pageContext, "trace")
        val msg = StringBuffer()
        msg.append("[" + trace.getTime().toString() + " ms " + total + "] ")
        msg.append("[" + trace.getTemplate().toString() + " @ line: " + trace.getLine().toString() + "]")
        if (hasCat || hasText || hasVar) msg.append("- ")
        if (hasCat) msg.append("[" + trace.getCategory().toString() + "] ")
        if (hasVar) msg.append("[$`var`=$varValue] ")
        if (hasText) msg.append(" " + trace.getText().toString() + " ")
        log!!.log(trace.getType(), "cftrace", msg.toString())

        // abort
        if (abort) throw Abort(Abort.SCOPE_REQUEST)
    }

    @Override
    fun doInitBody() {
    }

    @Override
    fun doAfterBody(): Int {
        return SKIP_BODY
    }

    /**
     * sets if has body or not
     *
     * @param hasBody
     */
    fun hasBody(hasBody: Boolean) {}
}