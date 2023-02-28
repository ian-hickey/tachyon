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
package tachyon.runtime.exp

import tachyon.runtime.PageContext

/**
 * Box a Native Exception, Native = !PageException
 */
class NativeException// set stacktrace

/*
  * StackTraceElement[] st = getRootCause(t).getStackTrace();
  * if(hasTachyonRuntime(st))setStackTrace(st); else { StackTraceElement[] cst = new
  * Exception().getStackTrace(); if(hasTachyonRuntime(cst)){ StackTraceElement[] mst=new
  * StackTraceElement[st.length+cst.length-1]; System.arraycopy(st, 0, mst, 0, st.length);
  * System.arraycopy(cst, 1, mst, st.length, cst.length-1);
  * 
  * setStackTrace(mst); } else setStackTrace(st); }
  */
/**
 * Standart constructor for native Exception class
 *
 * @param t Throwable
 */ protected constructor(val exception: Throwable?) : PageExceptionImpl(exception, exception.getClass().getName()) {
    private fun hasTachyonRuntime(st: Array<StackTraceElement?>?): Boolean {
        if (st != null) for (i in st.indices) {
            if (st[i].getClassName().indexOf("tachyon.runtime") !== -1) return true
        }
        return false
    }

    @Override
    override fun toDumpData(pageContext: PageContext?, maxlevel: Int, dp: DumpProperties?): DumpData? {
        val data: DumpData = super.toDumpData(pageContext, maxlevel, dp)
        if (data is DumpTable) (data as DumpTable)
                .setTitle(Constants.NAME.toString() + " [" + pageContext.getConfig().getFactory().getEngine().getInfo().getVersion() + "] - Error (" + Caster.toClassName(exception) + ")")
        return data
    }

    @Override
    override fun typeEqual(type: String?): Boolean {
        return if (super.typeEqual(type)) true else Reflector.isInstaneOfIgnoreCase(exception.getClass(), type)
    }

    @Override
    override fun setAdditional(key: Collection.Key?, value: Object?) {
        super.setAdditional(key, value)
    }

    companion object {
        private const val serialVersionUID = 6221156691846424801L
        fun newInstance(t: Throwable?): NativeException? {
            return newInstance(t, true)
        }

        fun newInstance(t: Throwable?, rethrowIfNecessary: Boolean): NativeException? {
            if (rethrowIfNecessary && t is ThreadDeath) throw t as ThreadDeath?
            return NativeException(t)
        }

        private fun getRootCause(t: Throwable?): Throwable? {
            var t = t
            var c: Throwable?
            do {
                c = t.getCause()
                if (c == null || c === t) return t
                t = c
            } while (true)
        }
    }
}