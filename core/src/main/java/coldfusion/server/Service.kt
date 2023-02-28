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

import java.util.Map

interface Service {
    @Throws(ServiceException::class)
    fun start()

    @Throws(ServiceException::class)
    fun stop()

    @Throws(ServiceException::class)
    fun restart()
    fun getStatus(): Int
    fun getMetaData(): ServiceMetaData?
    fun getProperty(arg0: String?): Object?
    fun setProperty(arg0: String?, arg1: Object?)
    fun getResourceBundle(): Map?

    companion object {
        const val UNINITALIZED = 1
        const val STARTING = 2
        const val STARTED = 4
        const val STOPPING = 8
        const val STOOPED = 16
    }
}