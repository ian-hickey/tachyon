/**
 * Copyright (c) 2015, Tachyon Assosication Switzerland. All rights reserved.
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
package tachyon.loader.osgi

import java.io.BufferedWriter

class LoggerImpl(logFile: File) : Logger() {
    private val logFile: File
    @SuppressWarnings("rawtypes")
    @Override
    protected fun doLog(bundle: Bundle?, sr: ServiceReference?, level: Int, msg: String, throwable: Throwable?) {
        var throwable = throwable
        if (level > getLogLevel()) return
        var s = ""
        if (sr != null) s = s + "SvcRef " + sr + " " else if (bundle != null) s = s + "Bundle " + bundle.toString() + " "
        s = s + msg

        // throwable
        if (throwable != null) {
            if (throwable is BundleException && (throwable as BundleException).getNestedException() != null) throwable = (throwable as BundleException).getNestedException()
            val sw = StringWriter()
            val pw = PrintWriter(sw)
            throwable.printStackTrace(pw)
            s += """
                
                ${sw.getBuffer()}
                """.trimIndent()
        }
        _log(level, s)
    }

    @Override
    protected fun _log(bundle: Bundle?, sr: ServiceReference?, level: Int, msg: String, throwable: Throwable?) {
        _log(level, msg)
    }

    private fun _log(level: Int, msg: String) {
        if (level > getLogLevel()) return
        var bw: BufferedWriter? = null
        try {
            bw = BufferedWriter(OutputStreamWriter(FileOutputStream(logFile, true)))
            bw.write("""${toLevel(level)} [${Date()}]:
$msg
""")
            bw.flush()
        } catch (ioe: IOException) {
        } finally {
            if (bw != null) try {
                bw.close()
            } catch (e: IOException) {
            }
        }
    }

    private fun toLevel(level: Int): String {
        return when (level) {
            LOG_DEBUG -> "DEBUG"
            LOG_ERROR -> "ERROR"
            LOG_INFO -> "INFO"
            LOG_WARNING -> "WARNING"
            else -> "UNKNOWNN[$level]"
        }
    }

    private fun toLevel(level: String?): Int {
        if (level != null) {
            if ("DEBUG".equalsIgnoreCase(level)) return LOG_DEBUG
            if ("ERROR".equalsIgnoreCase(level)) return LOG_ERROR
            if ("INFO".equalsIgnoreCase(level)) return LOG_INFO
            if ("WARNING".equalsIgnoreCase(level)) return LOG_WARNING
        }
        return LOG_ERROR
    }

    init {
        this.logFile = logFile
        setLogLevel(LOG_ERROR)
        if (!logFile.exists()) try {
            logFile.createNewFile()
        } catch (e: IOException) {
            // TODO Auto-generated catch block
            e.printStackTrace()
        }
    }
}