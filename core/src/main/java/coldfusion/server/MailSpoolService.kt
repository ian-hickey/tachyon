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

interface MailSpoolService : Service {
    // public abstract void storeMail(MailImpl arg0) throws MailSessionException,MailDeliveryException;
    // public abstract void validate(MailImpl arg0) throws ServiceException;
    fun getSettings(): Map?
    fun setSettings(arg0: Map?)
    fun getPort(): Int
    fun getSchedule(): Long
    fun getServer(): String?
    fun getSeverity(): String?
    fun getTimeout(): Int
    fun isMailSentLoggingEnable(): Boolean
    fun setMailSentLoggingEnable(arg0: Boolean)
    fun setPort(arg0: Int)
    fun setSchedule(arg0: Int)
    fun setServer(arg0: String?)
    fun setSeverity(arg0: String?)
    fun setTimeout(arg0: Int)
    fun verifyServer(): Boolean
    fun writeToLog(arg0: String?, arg1: Boolean)
}