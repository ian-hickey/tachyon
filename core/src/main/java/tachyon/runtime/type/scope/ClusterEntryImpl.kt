/**
 * Copyright (c) 2023, TachyonCFML.org
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
package tachyon.runtime.type.scope

import java.io.Serializable

class ClusterEntryImpl : ClusterEntry {
    private var key: Collection.Key? = null
    private var time: Long = 0
    private var value: Serializable? = null

    constructor(key: Collection.Key?, value: Serializable?, offset: Int) {
        this.key = key
        time = System.currentTimeMillis() + offset
        this.value = value
    }

    constructor(key: Collection.Key?, value: Serializable?, time: Long) {
        this.key = key
        this.time = time
        this.value = value
    }

    /**
     * Constructor of the class for Webservice Bean Deserializer
     */
    constructor() {}

    /**
     * @param key the key to set
     */
    @Override
    fun setKey(key: Collection.Key?) {
        this.key = key
    }

    /**
     * @param time the time to set
     */
    @Override
    fun setTime(time: Long) {
        this.time = time
    }

    /**
     * @param value the value to set
     */
    @Override
    fun setValue(value: Serializable?) {
        this.value = value
    }

    /**
     * @return the key
     */
    @Override
    fun getKey(): Collection.Key? {
        return key
    }

    /**
     * @return the time
     */
    @Override
    fun getTimeRef(): Long? {
        return Long.valueOf(time)
    }

    @Override
    fun getTime(): Long {
        return time
    }

    /**
     * @return the value
     */
    @Override
    fun getValue(): Serializable? {
        return value
    }

    @Override
    override fun equals(obj: Object?): Boolean {
        if (obj is ClusterEntry) {
            val other: ClusterEntry? = obj as ClusterEntry?
            return key.equalsIgnoreCase(other.getKey())
        }
        return false
    }
}