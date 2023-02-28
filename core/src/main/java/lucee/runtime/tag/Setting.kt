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

import java.util.Iterator

class Setting : BodyTagImpl() {
    private var hasBody = false

    /**
     * set the value requesttimeout
     *
     * @param requesttimeout value to set
     */
    fun setRequesttimeout(requesttimeout: Double) {
        val rt: Long
        rt = if (requesttimeout <= 0) Long.MAX_VALUE else (requesttimeout * 1000).toLong()
        pageContext.setRequestTimeout(rt)
    }

    /**
     * set the value showdebugoutput Yes or No. When set to No, showDebugOutput suppresses debugging
     * information that would otherwise display at the end of the generated page.Default is Yes.
     *
     * @param showdebugoutput value to set
     */
    fun setShowdebugoutput(showdebugoutput: Boolean) {
        if (pageContext.getConfig().debug()) {
            val d: DebuggerImpl = pageContext.getDebugger() as DebuggerImpl
            d.setOutput(showdebugoutput, (pageContext as PageContextImpl?).getListenSettings())
        }
    }

    fun setListen(listen: Boolean) {
        (pageContext as PageContextImpl?).setListenSettings(true)
    }

    @Throws(PageException::class)
    fun setInfo(varName: String?) {
        val sct: Struct = StructImpl()

        // debugging
        val d: DebuggerImpl = pageContext.getDebugger() as DebuggerImpl
        val debugging: Struct = StructImpl()
        sct.set(KeyConstants._debugging, debugging)
        debugging.set(KeyConstants._status, d.getOutput())
        val pe: PageExceptionImpl = d.getOutputContext() as PageExceptionImpl
        if (pe != null) {
            val arr: Array = pe.getTagContext(pageContext.getConfig())
            val it: Iterator<Object?> = arr.valueIterator()
            val sb = StringBuilder()
            var tmp: Struct
            while (it.hasNext()) {
                if (sb.length() > 0) sb.append('\n')
                tmp = Caster.toStruct(it.next())
                sb.append(tmp.get(KeyConstants._template).toString() + ":" + tmp.get(KeyConstants._line))
            }
            debugging.set("location", sb.toString())
            // debugging.set("location", pe.getStackTraceAsString());
        }

        // request timeout
        val timeout: Struct = StructImpl()
        sct.set(KeyConstants._timeout, timeout)
        timeout.set(KeyConstants._status, Caster.toDouble(pageContext.getRequestTimeout() / 1000))

        // enable cfoutput only
        val output: Struct = StructImpl()
        sct.set(KeyConstants._output, output)
        val level: Short = (pageContext as PageContextImpl?).getCFOutputOnly()
        output.set(KeyConstants._status, level > 0)
        output.set(KeyConstants._level, Caster.toDouble(level))

        // set variable
        pageContext.setVariable(varName, sct)
    }

    /**
     * set the value enablecfoutputonly Yes or No. When set to Yes, cfsetting blocks output of HTML that
     * resides outside cfoutput tags.
     *
     * @param enablecfoutputonly value to set
     * @throws PageException
     */
    @Throws(PageException::class)
    fun setEnablecfoutputonly(enablecfoutputonly: Object?) {
        if (enablecfoutputonly is String && Caster.toString(enablecfoutputonly).trim().equalsIgnoreCase("reset")) {
            pageContext.setCFOutputOnly(0.toShort())
        } else {
            pageContext.setCFOutputOnly(Caster.toBooleanValue(enablecfoutputonly))
        }
    }

    /**
     * @param enablecfoutputonly
     */
    @Deprecated
    @Deprecated("""this method is replaced by the method
	              <code>setEnablecfoutputonly(Object enablecfoutputonly)</code>
	  """)
    fun setEnablecfoutputonly(enablecfoutputonly: Boolean) {
        pageContext.setCFOutputOnly(enablecfoutputonly)
    }

    @Override
    fun doStartTag(): Int {
        return EVAL_BODY_INCLUDE
    }

    @Override
    fun doEndTag(): Int {
        return EVAL_PAGE
    }

    /**
     * sets if tag has a body or not
     *
     * @param hasBody
     */
    fun hasBody(hasBody: Boolean) {
        this.hasBody = hasBody
    }
}