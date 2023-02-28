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
package lucee.runtime.dump

import java.io.IOException

class HTMLDumpWriter : DumpWriter {
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
        var id: String? = "_dump" + count++
        // prepare data
        val rows: Array<DumpRow?> = table.getRows()
        var cols = 0
        for (i in rows.indices) if (rows[i].getItems().length > cols) cols = rows[i].getItems().length
        if (!inside) {
            writer.write("<script>")
            writer.write("function dumpOC(name){")
            writer.write("var tds=document.all?document.getElementsByTagName('tr'):document.getElementsByName('_'+name);")
            // writer.write("var button=document.images['__btn'+name];");
            writer.write("var s=null;")
            // writer.write("if(button.src.indexOf('plus')==-1)
            // button.src=button.src.replace('minus','plus');");
            // writer.write("else button.src=button.src.replace('plus','minus');");
            writer.write("name='_'+name;")
            writer.write("for(var i=0;i<tds.length;i++) {")
            writer.write("if(document.all && tds[i].name!=name)continue;")
            writer.write("s=tds[i].style;")
            writer.write("if(s.display=='none') s.display='';")
            writer.write("else s.display='none';")
            writer.write("}")
            writer.write("}")
            writer.write("</script>")
        }
        var tl: TemplateLine? = null
        if (!inside) tl = SystemUtil.getCurrentContext(pc)
        val context = if (tl == null) "" else tl.toString()
        writer.write(
                "<table" + (if (table.getWidth() != null) " width=\"" + table.getWidth().toString() + "\"" else "") + "" + (if (table.getHeight() != null) " height=\"" + table.getHeight().toString() + "\"" else "")
                        + " cellpadding=\"3\" cellspacing=\"1\" style=\"font-family : Verdana, Geneva, Arial, Helvetica, sans-serif;font-size : 11px;color :" + table.getFontColor()
                        + " ;empty-cells:show;\">")

        // header
        if (!StringUtil.isEmpty(table.getTitle())) {
            writer.write("<tr><td title=\"" + context + "\" onclick=\"dumpOC('" + id + "')\" colspan=\"" + cols + "\" bgcolor=\"" + table.getHighLightColor()
                    + "\" style=\"border : 1px solid " + table.getBorderColor() + "; empty-cells:show;\">")
            // isSetContext=true;
            var contextPath = ""
            pc = ThreadLocalPageContext.get(pc)
            if (pc != null) {
                contextPath = pc.getHttpServletRequest().getContextPath()
                if (contextPath == null) contextPath = ""
            }
            writer.write("<span style=\"font-weight:bold;\">" + (if (!StringUtil.isEmpty(table.getTitle())) table.getTitle() else "") + "</span>"
                    + (if (!StringUtil.isEmpty(table.getComment())) "<br>" + table.getComment() else "") + "</td></tr>")
        } else id = null

        // items
        var value: DumpData?
        for (i in rows.indices) {
            if (id != null) writer.write("<tr name=\"_$id\">") else writer.write("<tr>")
            val items: Array<DumpData?> = rows[i].getItems()
            val hType: Int = rows[i].getHighlightType()
            var comperator = 1
            for (y in 0 until cols) {
                if (y <= items.size - 1) value = items[y] else value = SimpleDumpData("&nbsp;")
                val highLightIt = hType == -1 || hType and comperator > 0
                comperator *= 2
                if (value == null) value = SimpleDumpData("null")
                // else if(value.equals(""))value="&nbsp;";
                if (!inside) {
                    writer.write("<td valign=\"top\" title=\"$context\"")
                } else writer.write("<td valign=\"top\"")
                writer.write(" bgcolor=\"" + (if (highLightIt) table.getHighLightColor() else table.getNormalColor()).toString() + "\" style=\"border : 1px solid " + table.getBorderColor()
                        .toString() + ";empty-cells:show;\">")
                writeOut(pc, value, writer, expand, true)
                writer.write("</td>")
            }
            writer.write("</tr>")
        }

        // footer
        writer.write("</table>")
        if (!expand) writer.write("<script>dumpOC('$id');</script>")
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
        private var count = 0
    }
}