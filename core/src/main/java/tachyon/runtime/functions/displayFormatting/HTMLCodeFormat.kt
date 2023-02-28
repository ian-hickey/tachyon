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
 * Implements the CFML Function htmlcodeformat
 */
package tachyon.runtime.functions.displayFormatting

import tachyon.commons.lang.HTMLEntities

object HTMLCodeFormat : Function {
    fun call(pc: PageContext?, html: String?): String? {
        return "<pre>" + HTMLEntities.escapeHTML(html!!, HTMLEntities.HTMLV40).toString() + "</pre>"
    }

    fun call(pc: PageContext?, html: String?, version: Double): String? {
        var v: Short = HTMLEntities.HTMLV40
        if (version == 3.2) v = HTMLEntities.HTMLV32 else if (version == 4.0) v = HTMLEntities.HTMLV40
        return "<pre>" + HTMLEntities.escapeHTML(html!!, v).toString() + "</pre>"
    }
}