/**
 * Copyright (c) 2015, Lucee Assosication Switzerland. All rights reserved.
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
package lucee.runtime.orm

import java.lang.reflect.Method

object ORMExceptionUtil {
    private var setAdditional: Method? = null
    fun createException(session: ORMSession?, cfc: Component?, t: Throwable?): PageException? {
        val pe: PageException = Caster.toPageException(t) // CFMLEngineFactory.getInstance().getExceptionUtil().createApplicationException(t.getMessage());
        pe.setStackTrace(t.getStackTrace())
        if (session != null) setAddional(session, pe)
        if (cfc != null) setContext(pe, cfc)
        return pe
    }

    fun createException(session: ORMSession?, cfc: Component?, message: String?, detail: String?): PageException? {
        val pe: PageException = CFMLEngineFactory.getInstance().getExceptionUtil().createApplicationException(message)
        if (session != null) setAddional(session, pe)
        if (cfc != null) setContext(pe, cfc)
        return pe
    }

    private fun setContext(pe: PageException?, cfc: Component?) {
        if (cfc != null && getPageDeep(pe) == 0) pe.addContext(cfc.getPageSource(), 1, 1, null)
    }

    private fun setAddional(session: ORMSession?, pe: PageException?) {
        val names: Array<String?> = session.getEntityNames()
        setAdditional(pe, KeyConstants._Entities, ListUtil.arrayToList(names, ", "))
        setAddional(pe, session.getDataSources())
    }

    private fun setAddional(pe: PageException?, vararg sources: DataSource?) {
        if (sources != null && sources.size > 0) {
            val sb = StringBuilder()
            for (i in 0 until sources.size) {
                if (i > 0) sb.append(", ")
                sb.append(sources[i].getName())
            }
            setAdditional(pe, KeyConstants._Datasource, sb.toString())
        }
    }

    private fun getPageDeep(pe: PageException?): Int {
        val traces: Array<StackTraceElement?>? = getStackTraceElements(pe)
        var template = ""
        var tlast: String
        var trace: StackTraceElement? = null
        var index = 0
        for (i in traces.indices) {
            trace = traces!![i]
            tlast = template
            template = trace.getFileName()
            if (trace.getLineNumber() <= 0 || template == null || CFMLEngineFactory.getInstance().getResourceUtil().getExtension(template, "").equals("java")) continue
            if (!(tlast ?: "").equals(template)) index++
        }
        return index
    }

    private fun getStackTraceElements(t: Throwable?): Array<StackTraceElement?>? {
        var st: Array<StackTraceElement?>? = getStackTraceElements(t, true)
        if (st == null) st = getStackTraceElements(t, false)
        return st
    }

    private fun getStackTraceElements(t: Throwable?, onlyWithCML: Boolean): Array<StackTraceElement?>? {
        var st: Array<StackTraceElement?>?
        val cause: Throwable = t.getCause()
        if (cause != null) {
            st = getStackTraceElements(cause, onlyWithCML)
            if (st != null) return st
        }
        st = t.getStackTrace()
        return if (!onlyWithCML || hasCFMLinStacktrace(st)) {
            st
        } else null
    }

    private fun hasCFMLinStacktrace(traces: Array<StackTraceElement?>?): Boolean {
        for (i in traces.indices) {
            if (traces!![i].getFileName() != null && !traces[i].getFileName().endsWith(".java")) return true
        }
        return false
    }

    fun setAdditional(pe: PageException?, name: Key?, value: Object?) {
        try {
            if (setAdditional == null || setAdditional.getDeclaringClass() !== pe.getClass()) {
                setAdditional = pe.getClass().getMethod("setAdditional", arrayOf<Class?>(Key::class.java, Object::class.java))
            }
            setAdditional.invoke(pe, arrayOf<Object?>(name, value))
        } catch (t: Throwable) {
            ExceptionUtil.rethrowIfNecessary(t)
        }
    }
}