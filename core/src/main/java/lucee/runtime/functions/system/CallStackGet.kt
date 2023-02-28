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
package lucee.runtime.functions.system

import java.util.Iterator

/**
 * returns the root of this current Page Context
 */
object CallStackGet : Function {
    private const val serialVersionUID = -5853145189662102420L
    val LINE_NUMBER: Collection.Key? = KeyImpl.getInstance("LineNumber")
    fun call(pc: PageContext?): Object? {
        val arr: Array = ArrayImpl()
        _getTagContext(pc, arr, Exception("Stack trace"), LINE_NUMBER)
        return arr
    }

    @Throws(PageException::class)
    fun call(pc: PageContext?, type: String?): Object? {
        return call(pc, type, 0.0, 0.0)
    }

    @Throws(PageException::class)
    fun call(pc: PageContext?, type: String?, offset: Double): Object? {
        return call(pc, type, offset, 0.0)
    }

    @Throws(PageException::class)
    fun call(pc: PageContext?, type: String?, offset: Double, maxFrames: Double): Object? {
        var arr: Array? = call(pc)
        if (offset > 0 || maxFrames > 0) {
            val sliceFrom = offset.toInt() + 1
            val sliceTo = if (maxFrames > 0) (maxFrames + offset) as Int else 0
            arr = ArraySlice.get(arr, sliceFrom, sliceTo)
        }
        if (type.equalsIgnoreCase("array")) return arr
        if (type.equalsIgnoreCase("json")) {
            return try {
                JSONConverter(true, null).serialize(pc, arr, SerializationSettings.SERIALIZE_AS_ROW)
            } catch (t: Throwable) {
                ExceptionUtil.rethrowIfNecessary(t)
                throw Caster.toPageException(t)
            }
        }
        val sb = StringBuilder(64 * arr.size())
        var struct: Struct
        var func: String
        val it: Iterator = arr.valueIterator()
        if (type.equalsIgnoreCase("text") || type.equalsIgnoreCase("string")) {
            while (it.hasNext()) {
                struct = it.next() as Struct
                sb.append(struct.get(KeyConstants._template) as String)
                func = struct.get(KeyConstants._function)
                if (!func.isEmpty()) {
                    sb.append('.').append(func).append("()")
                }
                sb.append(':').append((struct.get(LINE_NUMBER) as Double).intValue())
                if (it.hasNext()) sb.append("; ")
            }
            return sb.toString()
        }
        if (type.equalsIgnoreCase("html")) {
            sb.append("<ul class='-lucee-array'>")
            while (it.hasNext()) {
                struct = it.next() as Struct
                sb.append("<li>")
                sb.append(struct.get(KeyConstants._template) as String)
                func = struct.get(KeyConstants._function)
                if (!func.isEmpty()) {
                    sb.append('.').append(func).append("()")
                }
                sb.append(':').append((struct.get(LINE_NUMBER) as Double).intValue())
                sb.append("</li>")
            }
            sb.append("</ul>")
            return sb.toString()
        }
        throw FunctionException(pc, CallStackGet::class.java.getSimpleName(), 1, "type", "Argument type [$type] is not valid.  Valid types are: [array], text, html, json.")
    }

    fun _getTagContext(pc: PageContext?, tagContext: Array?, t: Throwable?, lineNumberName: Collection.Key?) {
        // Throwable root = t.getRootCause();
        val cause: Throwable = t.getCause()
        if (cause != null) _getTagContext(pc, tagContext, cause, lineNumberName)
        val traces: Array<StackTraceElement?> = t.getStackTrace()
        val udfs: Array<UDF?> = (pc as PageContextImpl?).getUDFs()
        var line = 0
        var template: String
        var item: Struct?
        var trace: StackTraceElement? = null
        var functionName: String
        var methodName: String
        var index = udfs.size - 1
        for (i in traces.indices) {
            trace = traces[i]
            template = trace.getFileName()
            if (trace.getLineNumber() <= 0 || template == null || ResourceUtil.getExtension(template, "").equals("java")) continue
            methodName = trace.getMethodName()
            functionName = if (methodName != null && methodName.startsWith("udfCall") && index > -1) udfs[index--].getFunctionName() else ""
            item = StructImpl()
            line = trace.getLineNumber()
            item.setEL(KeyConstants._function, functionName)
            /*
			 * template is now an absolute path try { template=ExpandPath.call(pc, template); } catch
			 * (PageException e) {}
			 */item.setEL(KeyConstants._template, abs(pc as PageContextImpl?, template))
            item.setEL(lineNumberName, Double.valueOf(line))
            tagContext.appendEL(item)
        }
    }

    private fun abs(pc: PageContextImpl?, template: String?): String? {
        val config: ConfigWeb = pc.getConfig()
        var res: Resource? = config.getResource(template)
        if (res.exists()) return template
        val tmp: String
        val ps: PageSource? = if (pc == null) null else pc.getPageSource(template)
        res = if (ps == null) null else ps.getPhyscalFile()
        if (res == null || !res.exists()) {
            tmp = ps.getDisplayPath()
            res = if (StringUtil.isEmpty(tmp)) null else config.getResource(tmp)
            if (res != null && res.exists()) return res.getAbsolutePath()
        } else return res.getAbsolutePath()
        return template
    }
}