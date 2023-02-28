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
/**
 * Implements the CFML Function dump
 */
package lucee.runtime.functions.other

import java.io.IOException

object Dump : Function {
    private const val OUTPUT_TYPE_NONE = 0
    private const val OUTPUT_TYPE_BROWSER = 1
    private const val OUTPUT_TYPE_CONSOLE = 2
    private const val OUTPUT_TYPE_RESOURCE = 3

    // private static final int FORMAT_TYPE_HTML = 0;
    // private static final int FORMAT_TYPE_TEXT = 1;
    @Throws(PageException::class)
    fun call(pc: PageContext?, `object`: Object?): String? {
        return call(pc, `object`, null, true, 9999.0, null, null, null, null, 9999.0, true, true)
    }

    @Throws(PageException::class)
    fun call(pc: PageContext?, `object`: Object?, label: String?): String? {
        return call(pc, `object`, label, true, 9999.0, null, null, null, null, 9999.0, true, true)
    }

    @Throws(PageException::class)
    fun call(pc: PageContext?, `object`: Object?, label: String?, expand: Boolean): String? {
        return call(pc, `object`, label, expand, 9999.0, null, null, null, null, 9999.0, true, true)
    }

    @Throws(PageException::class)
    fun call(pc: PageContext?, `object`: Object?, label: String?, expand: Boolean, maxLevel: Double): String? {
        return call(pc, `object`, label, expand, maxLevel, null, null, null, null, 9999.0, true, true)
    }

    @Throws(PageException::class)
    fun call(pc: PageContext?, `object`: Object?, label: String?, expand: Boolean, maxLevel: Double, show: String?): String? {
        return call(pc, `object`, label, expand, maxLevel, show, null, null, null, 9999.0, true, true)
    }

    @Throws(PageException::class)
    fun call(pc: PageContext?, `object`: Object?, label: String?, expand: Boolean, maxLevel: Double, show: String?, hide: String?): String? {
        return call(pc, `object`, label, expand, maxLevel, show, hide, null, null, 9999.0, true, true)
    }

    @Throws(PageException::class)
    fun call(pc: PageContext?, `object`: Object?, label: String?, expand: Boolean, maxLevel: Double, show: String?, hide: String?, output: String?): String? {
        return call(pc, `object`, label, expand, maxLevel, show, hide, output, null, 9999.0, true, true)
    }

    @Throws(PageException::class)
    fun call(pc: PageContext?, `object`: Object?, label: String?, expand: Boolean, maxLevel: Double, show: String?, hide: String?, output: String?, format: String?): String? {
        return call(pc, `object`, label, expand, maxLevel, show, hide, output, format, 9999.0, true, true)
    }

    @Throws(PageException::class)
    fun call(pc: PageContext?, `object`: Object?, label: String?, expand: Boolean, maxLevel: Double, show: String?, hide: String?, output: String?, format: String?, keys: Double): String? {
        return call(pc, `object`, label, expand, maxLevel, show, hide, output, format, keys, true, true)
    }

    @Throws(PageException::class)
    fun call(pc: PageContext?, `object`: Object?, label: String?, expand: Boolean, maxLevel: Double, show: String?, hide: String?, output: String?, format: String?, keys: Double,
             metainfo: Boolean): String? {
        return call(pc, `object`, label, expand, maxLevel, show, hide, output, format, keys, metainfo, true)
    }

    @Throws(PageException::class)
    fun call(pc: PageContext?, `object`: Object?, label: String?, expand: Boolean, maxLevel: Double, show: String?, hide: String?, output: String?, format: String?, keys: Double,
             metainfo: Boolean, showUDFs: Boolean): String? {
        var show = show
        var hide = hide
        var output = output
        if (show != null && "all".equalsIgnoreCase(show.trim())) show = null
        if (hide != null && "all".equalsIgnoreCase(hide.trim())) hide = null

        // String context = getContext();
        // PageContext pcc = pc;
        try {

            // output
            var defType: Int = DumpWriter.DEFAULT_RICH
            var outputType = OUTPUT_TYPE_NONE
            var outputRes: Resource? = null
            if (!StringUtil.isEmpty(output, true)) {
                output = output.trim()
                if ("browser".equalsIgnoreCase(output)) {
                    outputType = OUTPUT_TYPE_BROWSER
                    defType = DumpWriter.DEFAULT_RICH
                } else if ("console".equalsIgnoreCase(output)) {
                    outputType = OUTPUT_TYPE_CONSOLE
                    defType = DumpWriter.DEFAULT_PLAIN
                } else {
                    outputType = OUTPUT_TYPE_RESOURCE
                    defType = DumpWriter.DEFAULT_RICH
                    outputRes = ResourceUtil.toResourceNotExisting(pc, output)
                }
            }

            // format
            val writer: DumpWriter = pc.getConfig().getDumpWriter(format, defType)
            val setShow: Set<String?>? = if (show != null) ListUtil.listToSet(show.toLowerCase(), ",", true) else null
            val setHide: Set<String?>? = if (hide != null) ListUtil.listToSet(hide.toLowerCase(), ",", true) else null
            val properties = DumpProperties(maxLevel.toInt(), setShow, setHide, keys.toInt(), metainfo, showUDFs)
            var dd: DumpData? = DumpUtil.toDumpData(`object`, pc, maxLevel.toInt(), properties)
            if (!StringUtil.isEmpty(label)) {
                val table = DumpTable("#ffffff", "#cccccc", "#000000")
                table.appendRow(1, SimpleDumpData(label))
                // table.appendRow(1,new SimpleDumpData(getContext()));
                table.appendRow(0, dd)
                dd = table
            }
            val isText: Boolean = "text".equalsIgnoreCase(format) // formatType==FORMAT_TYPE_TEXT
            if (OUTPUT_TYPE_BROWSER == outputType || outputType == OUTPUT_TYPE_NONE) {
                if (isText) pc.forceWrite("<pre>")
                pc.forceWrite(writer.toString(pc, dd, expand))
                if (isText) pc.forceWrite("</pre>")
            } else if (OUTPUT_TYPE_CONSOLE == outputType) System.out.println(writer.toString(pc, dd, expand)) else if (OUTPUT_TYPE_RESOURCE == outputType) IOUtil.write(outputRes, writer.toString(pc, dd, expand).toString() + "\n************************************************************************************\n",
                    (pc as PageContextImpl?).getResourceCharset(), true)
        } catch (e: IOException) {
            throw Caster.toPageException(e)
        }
        return ""
    } /*
	 * public static String getContext() { //Throwable cause = t.getCause(); StackTraceElement[] traces
	 * = new Exception().getStackTrace();
	 * 
	 * int line=0; String template; StackTraceElement trace=null; for(int i=0;i<traces.length;i++) {
	 * trace=traces[i]; template=trace.getFileName(); if((line=trace.getLineNumber())<=0 ||
	 * template==null || ResourceUtil.getExtension(template,"").equals("java")) continue; return
	 * template+":"+line; } return null; }
	 */
}