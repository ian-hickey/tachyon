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

class TextDumpWriter : DumpWriter {
    // private static int count=0;
    @Override
    @Throws(IOException::class)
    fun writeOut(pc: PageContext?, data: DumpData?, writer: Writer?, expand: Boolean) {
        writeOut(pc, data, writer, expand, 0)
    }

    @Throws(IOException::class)
    private fun writeOut(pc: PageContext?, data: DumpData?, writer: Writer?, expand: Boolean, level: Int) {
        var pc: PageContext? = pc
        if (data == null) return
        if (data !is DumpTable) {
            writer.write(StringUtil.escapeHTML(data.toString()))
            return
        }
        val table: DumpTable? = data as DumpTable?
        val rows: Array<DumpRow?> = table.getRows()
        var cols = 0
        for (i in rows.indices) if (rows[i].getItems().length > cols) cols = rows[i].getItems().length

        // header
        if (!StringUtil.isEmpty(table.getTitle(), true)) {
            var contextPath = ""
            pc = ThreadLocalPageContext.get(pc)
            if (pc != null) {
                contextPath = pc.getHttpServletRequest().getContextPath()
                if (contextPath == null) contextPath = ""
            }
            val header: String = table.getTitle().toString() + if (StringUtil.isEmpty(table.getComment())) "" else """
     
     ${table.getComment()}
     """.trimIndent()
            writer.write("""
    $header
    
    """.trimIndent())
            if (level > 0) writer.write(StringUtil.repeatString("	", level))
        }

        // items
        var value: DumpData?
        for (i in rows.indices) {
            val items: Array<DumpData?> = rows[i].getItems()
            // int comperator=1;
            for (y in 0 until cols) {
                if (y <= items.size - 1) value = items[y] else value = SimpleDumpData("")
                // comperator*=2;
                if (value == null) value = SimpleDumpData("null")
                writeOut(pc, value, writer, expand, level + 1)
                writer.write(" ")
            }
            writer.write("\n")
            if (level > 0) writer.write(StringUtil.repeatString("	", level))
        }
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
}