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
package tachyon.runtime.tag

import java.io.IOException

/**
 * Stops the time from starttag to endtag
 *
 *
 *
 */
class Stopwatch : BodyTagImpl() {
    private var label: String? = null
    var time: Long = 0
    private var variable: String? = null
    @Override
    fun release() {
        super.release()
        label = null
        time = 0L
        variable = null
    }

    /**
     * Label of the Stopwatch
     *
     * @param label sets the Label of the Stopwatch
     */
    fun setLabel(label: String?) {
        this.label = label
    }

    /**
     * Variable Name to write result to it
     *
     * @param variable variable name
     */
    fun setVariable(variable: String?) {
        this.variable = variable
    }

    @Override
    fun doStartTag(): Int {
        time = System.currentTimeMillis()
        return EVAL_BODY_INCLUDE
    }

    @Override
    @Throws(PageException::class)
    fun doEndTag(): Int {
        val exe: Long = System.currentTimeMillis() - time
        if (variable != null) {
            pageContext.setVariable(variable, Double.valueOf(exe))
        } else {
            val table = DumpTable("#ff9900", "#ffcc00", "#000000")
            table.appendRow(1, SimpleDumpData(if (label == null) "Stopwatch" else label), SimpleDumpData(exe))
            val writer: DumpWriter = pageContext.getConfig().getDefaultDumpWriter(DumpWriter.DEFAULT_RICH)
            try {
                pageContext.forceWrite(writer.toString(pageContext, table, true))
            } catch (e: IOException) {
            }
        }
        return EVAL_PAGE
    }

    @Override
    fun doInitBody() {
    }

    @Override
    fun doAfterBody(): Int {
        return SKIP_BODY
    }
}