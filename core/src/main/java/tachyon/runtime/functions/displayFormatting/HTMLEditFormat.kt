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
 * Implements the CFML Function htmleditformat
 */
package tachyon.runtime.functions.displayFormatting

import tachyon.commons.lang.HTMLEntities

object HTMLEditFormat : Function {
    fun call(pc: PageContext?, html: String?): String? {
        return HTMLEntities.escapeHTML(html!!, HTMLEntities.HTMLV20)
    }

    fun call(pc: PageContext?, html: String?, version: Double): String? {
        var v: Short = HTMLEntities.HTMLV20
        if (version == 2.0) v = HTMLEntities.HTMLV20 else if (version == 3.2) v = HTMLEntities.HTMLV32 else if (version == 4.0) v = HTMLEntities.HTMLV40 else if (version <= 0.0) v = HTMLEntities.HTMLV40
        return HTMLEntities.escapeHTML(html!!, v)
    }
}