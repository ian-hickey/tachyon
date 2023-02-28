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
package tachyon.runtime.customtag

import tachyon.commons.lang.StringUtil

class InitFile(pc: PageContext?, ps: PageSource?, filename: String?) {
    private val ps: PageSource?
    private val filename: String?
    private var isCFC: Boolean
    fun getPageSource(): PageSource? {
        return ps
    }

    fun getFilename(): String? {
        return filename
    }

    fun isCFC(): Boolean {
        return isCFC
    }

    init {
        this.ps = ps
        this.filename = filename

        // the tachyon dialect has not different extension for component and templates, but this dialect also
        // only supports components
        isCFC = false
        val extensions: Array<String?> = Constants.getComponentExtensions() // CustomTagUtil.getComponentExtension(pc,ps);
        for (i in extensions.indices) {
            if (StringUtil.endsWithIgnoreCase(filename!!, '.' + extensions[i])) {
                isCFC = true
                break
            }
        }
    }
}