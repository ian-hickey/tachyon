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
package lucee.commons.io.res.type.datasource

import lucee.commons.io.res.type.datasource.core.CoreSupport

class Attr(
        /**
         * @return the id
         */
        val id: Int,
        /**
         * @return the name
         */
        val name: String?,
        /**
         * @return the parent
         */
        val parent: String?, exists: Boolean, type: Int, size: Int, lastModified: Long, mode: Short, attributes: Short, data: Int) {
    private val exists = true
    private val size = 0

    /**
     * @return the mode
     */
    val mode: Short

    /**
     * @return the attributes
     */
    val attributes: Short

    /**
     * @return the lastModified
     */
    val lastModified: Long

    /**
     * @return the type
     */
    val type: Int

    /**
     * @return the data
     */
    val data: Int
    val isFile: Boolean
    val isDirectory: Boolean
    private val created: Long = System.currentTimeMillis()
    fun exists(): Boolean {
        return exists
    }

    fun size(): Int {
        return size
    }

    fun timestamp(): Long {
        return created
    }

    companion object {
        const val TYPE_DIRECTORY = 0
        const val TYPE_FILE = 1
        const val TYPE_LINK = 2
        const val TYPE_UNDEFINED = 3
        fun notExists(name: String?, parent: String?): Attr {
            return Attr(0, name, parent, false, TYPE_UNDEFINED, 0, 0, 0.toShort(), 0.toShort(), 0)
        }
    }

    init {
        // if(mode==0)print.dumpStack();
        this.exists = exists
        this.type = type
        this.size = size
        this.lastModified = lastModified
        this.mode = mode
        this.attributes = attributes
        this.data = data
        isDirectory = CoreSupport.isDirectory(type)
        isFile = CoreSupport.isFile(type)
    }
}