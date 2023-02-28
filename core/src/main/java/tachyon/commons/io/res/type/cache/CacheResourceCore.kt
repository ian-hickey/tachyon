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
package tachyon.commons.io.res.type.cache

import java.io.Serializable

/**
 * Core of a Ram Resource, holds the concrete data for an existing resource
 */
class CacheResourceCore
/**
 * Konstruktor
 *
 * @param parent
 * @param type
 * @param name
 * @param caseSensitive
 */(private var type: Int,
    /**
     * @return the path
     */
    val path: String?, private var name: String?) : Serializable {
    /**
     * Gibt den Feldnamen data zurueck.
     *
     * @return data
     */
    var data: ByteArray?
        private set
    /**
     * Gibt den Feldnamen lastModified zurueck.
     *
     * @return lastModified
     */
    /**
     * Setzt den Feldnamen lastModified.
     *
     * @param lastModified lastModified
     */
    var lastModified: Long = System.currentTimeMillis()
    /**
     * @return the mode
     */
    /**
     * @param mode the mode to set
     */
    var mode = 511
    var attributes = 0

    /**
     * Setzt den Feldnamen data.
     *
     * @param data data
     * @param append
     */
    fun setData(data: ByteArray?, append: Boolean) {
        lastModified = System.currentTimeMillis()

        // set data
        if (append) {
            if (this.data != null && data != null) {
                val newData = ByteArray(this.data!!.size + data.size)
                var i = 0
                while (i < this.data!!.size) {
                    newData[i] = this.data!![i]
                    i++
                }
                while (i < this.data!!.size + data.size) {
                    newData[i] = data[i - this.data!!.size]
                    i++
                }
                this.data = newData
            } else if (data != null) {
                this.data = data
            }
        } else {
            this.data = data
        }

        // set type
        if (this.data != null) type = TYPE_FILE
    }

    /**
     * Gibt den Feldnamen name zurueck.
     *
     * @return name
     */
    fun getName(): String? {
        return name
    }

    /**
     * Setzt den Feldnamen name.
     *
     * @param name name
     */
    fun setName(name: String?) {
        lastModified = System.currentTimeMillis()
        this.name = name
    }

    /**
     * Gibt den Feldnamen type zurueck.
     *
     * @return type
     */
    fun getType(): Int {
        return type
    }

    /**
     * Setzt den Feldnamen type.
     *
     * @param type type
     */
    fun setType(type: Int) {
        lastModified = System.currentTimeMillis()
        this.type = type
    }

    fun remove() {
        setType(0)
        setData(null, false)
    }

    companion object {
        /**
         * Directory Resource
         */
        const val TYPE_DIRECTORY = 1

        /**
         * Directory Resource
         */
        const val TYPE_FILE = 2
    }
}