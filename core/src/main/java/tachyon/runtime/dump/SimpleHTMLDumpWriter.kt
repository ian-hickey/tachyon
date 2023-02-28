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
package tachyon.runtime.dump

import java.io.IOException

class SimpleHTMLDumpWriter : DumpWriter {
    @Override
    @Throws(IOException::class)
    fun writeOut(pc: PageContext?, data: DumpData?, writer: Writer?, expand: Boolean) {
        writeOut(pc, data, writer, expand, false)
    }

    @Throws(IOException::class)
    private fun writeOut(pc: PageContext?, data: DumpData?, writer: Writer?, expand: Boolean, inside: Boolean) {
        var pc: PageContext? = pc
        if (data == null) return
        if (data !is DumpTable) {
            writer.write(StringUtil.escapeHTML(data.toString()))
            return
        }
        val table: DumpTable? = data as DumpTable?

        // prepare data
        val rows: Array<DumpRow?> = table.getRows()
        var cols = 0
        for (i in rows.indices) if (rows[i].getItems().length > cols) cols = rows[i].getItems().length
        var tl: TemplateLine? = null
        if (!inside) tl = SystemUtil.getCurrentContext(null)
        val context = if (tl == null) "" else tl.toString()
        if (rows.size == 1 && rows[0].getItems().length === 2) {
            val d: DumpData = rows[0].getItems().get(1)
            if (d !is DumpTable) {
                writer.write(StringUtil.escapeHTML(d.toString()))
                return
            }
        }
        writer.write("<table  cellpadding=\"1\" cellspacing=\"0\" " + (if (table.getWidth() != null) " width=\"" + table.getWidth().toString() + "\"" else "") + ""
                + (if (table.getHeight() != null) " height=\"" + table.getHeight().toString() + "\"" else "") + " border=\"1\">")

        // header
        if (!StringUtil.isEmpty(table.getTitle())) {
            writer.write("<tr><td title=\"$context\" colspan=\"$cols\">")
            // isSetContext=true;
            var contextPath = ""
            pc = ThreadLocalPageContext.get(pc)
            if (pc != null) {
                contextPath = pc.getHttpServletRequest().getContextPath()
                if (contextPath == null) contextPath = ""
            }
            writer.write("<b>" + (if (!StringUtil.isEmpty(table.getTitle())) table.getTitle() else "") + "</b>"
                    + (if (!StringUtil.isEmpty(table.getComment())) "<br>" + table.getComment() else "") + "</td></tr>")
        }

        // items
        var value: DumpData?
        for (i in rows.indices) {
            writer.write("<tr>")
            val items: Array<DumpData?> = rows[i].getItems()
            // int comperator=1;
            for (y in 0 until cols) {
                if (y <= items.size - 1) value = items[y] else value = SimpleDumpData("&nbsp;")
                // comperator*=2;
                if (value == null) value = SimpleDumpData("null")
                // else if(value.equals(""))value="&nbsp;";
                if (!inside) {
                    writer.write("<td title=\"$context\">")
                } else writer.write("<td>")
                writeOut(pc, value, writer, expand, true)
                writer.write("</td>")
            }
            writer.write("</tr>")
        }

        // footer
        writer.write("</table>")
    }

    @Override
    fun toString(pc: PageContext?, data: DumpData?, expand: Boolean): String? {
        val sw = StringWriter()
        try {
            writeOut(pc, data, sw, expand)
        } catch (e: IOException) {
            return ""
        }
        return sw.toString()
    }

    companion object {
        private const val count = 0
    }
}