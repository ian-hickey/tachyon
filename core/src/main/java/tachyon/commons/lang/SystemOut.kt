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
package tachyon.commons.lang

import tachyon.commons.io.SystemUtil.ERR

class SystemOut {
    fun setOut(ps: PrintStream?): PrintStream {
        var ps: PrintStream? = ps
        val org: PrintStream = System.out
        if (ps == null) ps = PrintStream(DevNullOutputStream.DEV_NULL_OUTPUT_STREAM)
        System.setOut(ps)
        return org
    }

    fun setErr(ps: PrintStream?): PrintStream {
        var ps: PrintStream? = ps
        val org: PrintStream = System.err
        if (ps == null) ps = PrintStream(DevNullOutputStream.DEV_NULL_OUTPUT_STREAM)
        System.setErr(ps)
        return org
    }

    companion object {
        val FORMAT: SimpleDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S")

        /**
         * logs a value
         *
         * @param value
         */
        fun printDate(pw: PrintWriter, value: String) {
            val millis: Long = System.currentTimeMillis()
            pw.write(FORMAT.format(Date(millis)).toString() + " " + value + "\n")
            pw.flush()
        }

        fun printDate(pw: PrintWriter, t: Throwable) {
            val millis: Long = System.currentTimeMillis()
            pw.write(FORMAT.format(Date(millis)).toString() + "\n")
            t.printStackTrace(pw)
            pw.write("\n")
            pw.flush()
        }

        /**
         * logs a value
         *
         * @param value
         */
        fun print(pw: PrintWriter, value: String) {
            pw.write("""
    $value
    
    """.trimIndent())
            pw.flush()
        }

        fun printStack(pw: PrintWriter?) {
            Throwable().printStackTrace(pw)
        }

        fun printStack(type: Int) {
            val config: Config = ThreadLocalPageContext.getConfig()
            if (config != null) {
                if (type == ERR) printStack(config.getErrWriter()) else printStack(config.getOutWriter())
            } else {
                printStack(PrintWriter(if (type == ERR) System.err else System.out))
            }
        }

        /**
         * logs a value
         *
         * @param value
         */
        fun printDate(value: String?) {
            printDate(value, OUT)
        }

        fun printDate(e: Throwable) {
            printDate(getPrinWriter(ERR), e)
        }

        fun printDate(value: String, type: Int) {
            printDate(getPrinWriter(type), value)
        }

        fun printDate(e: Throwable, type: Int) {
            printDate(getPrinWriter(type), e)
        }

        fun getPrinWriter(type: Int): PrintWriter {
            val config: Config = ThreadLocalPageContext.getConfig()
            return if (config != null) {
                if (type == ERR) config.getErrWriter() else config.getOutWriter()
            } else SystemUtil.getPrintWriter(type)
        }

        /**
         * logs a value
         *
         * @param value
         */
        fun print(value: String?) {
            print(value, OUT)
        }

        fun print(value: String, type: Int) {
            val pc: PageContext = ThreadLocalPageContext.get()
            if (pc != null) {
                if (type == ERR) print(pc.getConfig().getErrWriter(), value) else print(pc.getConfig().getOutWriter(), value)
            } else {
                print(PrintWriter(if (type == ERR) System.err else System.out), value)
            }
        }
    }
}