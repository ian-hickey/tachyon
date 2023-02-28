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
package lucee.runtime.debug

import lucee.runtime.PageSource

/**
 * a single debug entry
 */
class DebugEntryTemplateImpl
/**
 * constructor of the class
 *
 * @param source
 * @param key
 */(source: PageSource?, private val key: String?) : DebugEntrySupport(source), DebugEntryTemplate {
    private var fileLoadTime: Long = 0
    private var queryTime: Long = 0
    @Override
    fun getFileLoadTime(): Long {
        return positiv(fileLoadTime)
    }

    @Override
    fun updateFileLoadTime(fileLoadTime: Long) {
        if (fileLoadTime > 0) this.fileLoadTime += fileLoadTime
    }

    @Override
    fun updateQueryTime(queryTime: Long) {
        if (queryTime > 0) this.queryTime += queryTime
    }

    @Override
    fun getSrc(): String? {
        return getSrc(getPath(), key)
    }

    @Override
    fun getQueryTime(): Long {
        return positiv(queryTime)
    }

    @Override
    fun resetQueryTime() {
        queryTime = 0
    }

    companion object {
        private const val serialVersionUID = 809949164432900481L

        /**
         * @param source
         * @param key
         * @return Returns the src.
         */
        fun getSrc(path: String?, key: String?): String? {
            return path.toString() + if (key == null) "" else "$$key"
        }
    }
}