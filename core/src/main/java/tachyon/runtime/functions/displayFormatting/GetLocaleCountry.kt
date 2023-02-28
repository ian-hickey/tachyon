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
/**
 * Implements the CFML Function formatbasen
 */
package tachyon.runtime.functions.displayFormatting

import java.util.Locale

object GetLocaleCountry : Function {
    private const val serialVersionUID = -4084704416496042957L
    fun call(pc: PageContext?): String? {
        return _call(pc, pc.getLocale(), pc.getLocale())
    }

    fun call(pc: PageContext?, locale: Locale?): String? {
        return _call(pc, locale, locale)
    }

    fun call(pc: PageContext?, locale: Locale?, dspLocale: Locale?): String? {
        return _call(pc, locale, dspLocale)
    }

    private fun _call(pc: PageContext?, locale: Locale?, dspLocale: Locale?): String? {
        var locale: Locale? = locale
        var dspLocale: Locale? = dspLocale
        if (locale == null) locale = pc.getLocale()
        if (dspLocale == null) dspLocale = locale
        return locale.getDisplayCountry(dspLocale)
    }
}