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
package tachyon.runtime.functions.dateTime

import java.util.TimeZone

/**
 * Implements the CFML Function now
 *
 */
@Deprecated
@Deprecated("removed with no replacement")
object NowServer : Function {
    /**
     * @param pc
     * @return server time
     * @throws ExpressionException
     */
    @Throws(ExpressionException::class)
    fun call(pc: PageContext?): DateTime? {
        DeprecatedUtil.function(pc, "nowServer")
        val now: Long = System.currentTimeMillis()
        val tachyon: Int = pc.getTimeZone().getOffset(now)
        val server: Int = TimeZone.getDefault().getOffset(now)
        return DateTimeImpl(pc, now - (tachyon - server), false)
    }
}