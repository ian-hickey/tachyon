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
package coldfusion.server

import java.io.IOException

interface GraphingService : Service {
    fun getSettings(): Map?
    fun getCacheType(): Int
    fun getCachePath(): String?
    fun getCacheSize(): Int
    fun getMaxEngines(): Int
    fun generateGraph(arg0: String?, arg1: Int, arg2: Int, arg3: String?, arg4: String?, arg5: String?, arg6: String?, arg7: Boolean): String?
    fun generateGraph(arg0: String?, arg1: Int, arg2: Int, arg3: String?, arg4: String?, arg5: String?, arg6: String?): String?

    @Throws(IOException::class)
    fun generateBytes(arg0: String?, arg1: Int, arg2: Int, arg3: String?, arg4: String?, arg5: String?): ByteArray?

    @Throws(IOException::class)
    fun generateBytes(arg0: String?, arg1: Int, arg2: Int, arg3: String?, arg4: String?, arg5: String?, arg6: Boolean): ByteArray?

    @Throws(IOException::class)
    fun getGraphData(arg0: String?, arg1: ServletContext?, arg2: Boolean): ByteArray?
    fun initializeEngine(arg0: ServletContext?)
    fun setUpWatermark()
}