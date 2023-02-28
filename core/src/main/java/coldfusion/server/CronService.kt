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

import java.util.Date

interface CronService : Service {
    @Throws(ServiceException::class)
    fun updateTask(arg0: String?, arg1: String?, arg2: String?, arg3: String?, arg4: String?, arg5: Date?, arg6: Date?, arg7: Date?, arg8: Date?, arg9: String?, arg10: Boolean,
                   arg11: String?, arg12: String?, arg13: String?, arg14: String?, arg15: String?, arg16: String?, arg17: Boolean, arg18: String?, arg19: String?)

    fun listAll(): List?
    fun list(): String?

    // public abstract CronTabEntry findTask(String arg0);
    @Throws(ServiceException::class)
    fun deleteTask(arg0: String?)

    @Throws(ServiceException::class)
    fun runCall(arg0: String?)

    @Throws(ServiceException::class)
    fun setLogFlag(arg0: Boolean)
    fun getLogFlag(): Boolean

    // public abstract void updateTasks(ConfigMap arg0) throws ServiceException;
    fun saveCronEntries()
}