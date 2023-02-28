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
 * Implements the CFML Function lsiscurrency
 */
package tachyon.runtime.functions.international

import java.util.Locale

object LSIsNumeric : Function {
    private const val serialVersionUID = 4753476752482915194L
    fun call(pc: PageContext?, string: String?): Boolean {
        return call(pc, string, null)
    }

    fun call(pc: PageContext?, string: String?, locale: Locale?): Boolean {
        return try {
            LSParseNumber.call(pc, string, locale)
            true
        } catch (t: Throwable) {
            ExceptionUtil.rethrowIfNecessary(t)
            false
        }
    }
}