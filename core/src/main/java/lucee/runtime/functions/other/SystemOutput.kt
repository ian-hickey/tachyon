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
/**
 * Implements the CFML Function writeoutput
 */
package lucee.runtime.functions.other

import java.io.PrintStream

object SystemOutput : Function {
    @Throws(PageException::class)
    fun call(pc: PageContext?, obj: Object?): Boolean {
        return call(pc, obj, false, false)
    }

    @Throws(PageException::class)
    fun call(pc: PageContext?, obj: Object?, addNewLine: Boolean): Boolean {
        return call(pc, obj, addNewLine, false)
    }

    @Throws(PageException::class)
    fun call(pc: PageContext?, obj: Object?, addNewLine: Boolean, doErrorStream: Boolean): Boolean {
        var string: String
        string = if (Decision.isSimpleValue(obj)) Caster.toString(obj) else {
            try {
                Serialize.call(pc, obj)
            } catch (t: Throwable) {
                ExceptionUtil.rethrowIfNecessary(t)
                obj.toString()
            }
        }
        var stream: PrintStream = CFMLEngineImpl.CONSOLE_OUT
        // string+=":"+Thread.currentThread().getId();
        if (doErrorStream) stream = CFMLEngineImpl.CONSOLE_ERR
        if (string != null) {
            if (StringUtil.indexOfIgnoreCase(string, "<print-stack-trace>") !== -1) {
                val st: String = ExceptionUtil.getStacktrace(Exception("Stack trace"), false)
                string = StringUtil.replace(string, "<print-stack-trace>", """
     
     $st
     
     """.trimIndent(), true).trim()
            }
            if (StringUtil.indexOfIgnoreCase(string, "<hash-code>") !== -1) {
                val st: String = obj.hashCode().toString() + ""
                string = StringUtil.replace(string, "<hash-code>", st, true).trim()
            }
        }
        if (addNewLine) stream.println(string) else stream.print(string)
        return true
    }
}