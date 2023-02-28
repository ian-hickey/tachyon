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

interface DebuggingService : Service {
    // public abstract Debugger getDebugger();
    fun reset(arg0: Int)
    fun reset()
    fun getDebuggerStartTime(): Long
    fun getSettings(): Map?
    fun getDebugTemplate(): String?
    fun getXMLTemplate(): String?
    fun getIplist(): Map?
    fun getShowdebug(): Boolean
    fun setShowdebug(arg0: Boolean)
    fun isEnabled(): Boolean
    fun setEnabled(arg0: Boolean)
    fun isRobustEnabled(): Boolean
    fun setRobustEnabled(arg0: Boolean)
    fun check(arg0: String?): Boolean
    fun check(arg0: Int): Boolean
    fun isValidIP(arg0: String?): Boolean
    fun isTimerEnabled(): Boolean
    fun isFlashFormCompileErrorsEnabled(): Boolean
}