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
package tachyon.runtime.type.trace

import tachyon.commons.io.res.util.ResourceUtil

class TraceObjectSupport(debugger: Debugger?, o: Object?, type: Int, category: String?, text: String?) : TraceObject {
    protected var o: Object?
    protected var debugger: Debugger?
    protected var type: Int
    protected var category: String?
    protected var text: String?

    @Override
    override fun toString(): String {
        return o.toString()
    }

    @Override
    override fun equals(obj: Object?): Boolean {
        return o.equals(obj)
    }

    protected fun log() {
        try {
            log(debugger, type, category, text, null, null)
        } catch (t: Throwable) {
            ExceptionUtil.rethrowIfNecessary(t)
        }
    }

    protected fun log(varName: Object?) {
        try {
            log(debugger, type, category, text, varName.toString(), null)
        } catch (t: Throwable) {
            ExceptionUtil.rethrowIfNecessary(t)
        }
    }

    protected fun log(varName: Object?, varValue: Object?) {
        try {
            log(debugger, type, category, text, varName.toString(), varValue.toString())
        } catch (t: Throwable) {
            ExceptionUtil.rethrowIfNecessary(t)
        }
    }

    protected fun pc(): PageContext? {
        return ThreadLocalPageContext.get()
    }

    companion object {
        fun log(debugger: Debugger?, type: Int, category: String?, text: String?, varName: String?, varValue: String?) {
            val traces: Array<StackTraceElement?> = Exception().getStackTrace()
            var line = 0
            var template: String? = null
            var trace: StackTraceElement? = null
            for (i in traces.indices) {
                trace = traces[i]
                template = trace.getFileName()
                if (trace.getLineNumber() <= 0 || template == null || ResourceUtil.getExtension(template, "").equals("java") || isDumpTemplate(template)) continue
                line = trace.getLineNumber()
                break
            }
            // print.e(t);
            if (line == 0) return
            val action = type(traces[2].getMethodName())
            if (debugger != null) debugger.addTrace(type, category, text, template, line, action, varName, varValue)
        }

        private fun isDumpTemplate(template: String?): Boolean {
            var template = template
            template = ResourceUtil.removeExtension(template!!, template).toLowerCase()
            return template.endsWith("dump")
        }

        protected fun type(type: String?): String? {
            if (type!!.equals("appendEL")) return "append"
            if (type.equals("setEL")) return "set"
            if (type.equals("removeEL")) return "remove"
            if (type.equals("keys")) return "list"
            return if (type.equals("toDumpData")) "dump" else type
        }

        fun toTraceObject(debugger: Debugger?, obj: Object?, type: Int, category: String?, text: String?): TraceObject? {
            if (obj is TraceObject) return obj else if (obj is UDF) return TOUDF(debugger, obj as UDF?, type, category, text) else if (obj is Query) return TOQuery(debugger, obj as Query?, type, category, text) else if (obj is Array) return TOArray(debugger, obj as Array?, type, category, text) else if (obj is Struct) return TOStruct(debugger, obj as Struct?, type, category, text) else if (obj is DateTime) return TODateTime(debugger, obj as DateTime?, type, category, text)
            return TOObjects(debugger, obj, type, category, text)
        }
    }

    init {
        this.o = o
        // this.log=log;
        this.type = type
        this.category = category
        this.text = text
        this.debugger = debugger
    }
}