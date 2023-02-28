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

object LSIsCurrency : Function {
    private const val serialVersionUID = -8659567712610988769L
    fun call(pc: PageContext?, string: String?): Boolean {
        return try {
            LSParseCurrency.toDoubleValue(pc.getLocale(), string, true)
            true
        } catch (t: Throwable) {
            ExceptionUtil.rethrowIfNecessary(t)
            false
        }
    }

    fun call(pc: PageContext?, string: String?, locale: Locale?): Boolean {
        return try {
            LSParseCurrency.toDoubleValue(if (locale == null) pc.getLocale() else locale, string, false)
            true
        } catch (t: Throwable) {
            ExceptionUtil.rethrowIfNecessary(t)
            false
        }
    }
}