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
package lucee.commons.io.log

import kotlin.Throws
import lucee.loader.util.Util
import kotlin.jvm.Synchronized
import kotlin.jvm.Transient
import org.osgi.framework.Version

/**
 * Log Interface
 */
interface Log {
    /**
     * log one line
     *
     * @param level level to log (Log.LEVEL_DEBUG, Log.LEVEL_WARN, Log.LEVEL_ERROR)
     * @param application application name
     * @param message message to log
     */
    fun log(level: Int, application: String?, message: String?)
    fun log(level: Int, application: String?, message: String?, t: Throwable?)
    fun log(level: Int, application: String?, t: Throwable?)

    /**
     * log level trace
     *
     * @param application application name
     * @param message message to log
     */
    fun trace(application: String?, message: String?)

    /**
     * log level info
     *
     * @param application application name
     * @param message message to log
     */
    fun info(application: String?, message: String?)

    /**
     * log level debug
     *
     * @param application application name
     * @param message message to log
     */
    fun debug(application: String?, message: String?)

    /**
     * log level warn
     *
     * @param application application name
     * @param message message to log
     */
    fun warn(application: String?, message: String?)

    /**
     * log level error
     *
     * @param application application name
     * @param message message to log
     */
    fun error(application: String?, message: String?)
    fun error(application: String?, t: Throwable?)
    fun error(application: String?, message: String?, t: Throwable?)

    /**
     * log level fatal
     *
     * @param application application name
     * @param message message to log
     */
    fun fatal(application: String?, message: String?)
    /**
     * @return returns the log level of the log
     */
    /**
     * @param level sets the log level of the log
     */
    var logLevel: Int

    companion object {
        const val LEVEL_TRACE = 0

        /**
         * Field `LEVEL_INFO`
         */
        const val LEVEL_INFO = 1

        /**
         * Field `LEVEL_DEBUG`
         */
        const val LEVEL_DEBUG = 2

        /**
         * Field `LEVEL_WARN`
         */
        const val LEVEL_WARN = 3

        /**
         * Field `LEVEL_ERROR`
         */
        const val LEVEL_ERROR = 4

        /**
         * Field `LEVEL_FATAL`
         */
        const val LEVEL_FATAL = 5
    }
}