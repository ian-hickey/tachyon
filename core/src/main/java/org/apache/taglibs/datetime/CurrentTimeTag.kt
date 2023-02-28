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
package org.apache.taglibs.datetime

import java.util.Date

class CurrentTimeTag : TagSupport() {
    @Override
    @Throws(JspException::class)
    fun doEndTag(): Int {
        val date = Date()
        try {
            super.pageContext.getOut().write("" + date.getTime())
        } catch (e: Exception) {
            throw JspException("IO Error: " + e.getMessage())
        }
        return 6
    }
}