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
package tachyon.runtime.text.xml.storage

import tachyon.runtime.type.dt.Date

/**
 * An Object to store to XML File
 */
abstract class StorageItem {
    /**
     * gets a value from the storage item as String
     *
     * @param key key of the value to get
     * @return matching value
     * @throws StorageException
     */
    @Throws(StorageException::class)
    fun getString(key: String?): String? {
        throw StorageException("there is no value with the key $key")
    }

    /**
     * gets a value from the storage item as int
     *
     * @param key key of the value to get
     * @return matching value
     * @throws StorageException
     */
    @Throws(StorageException::class)
    fun getInt(key: String?): Int {
        throw StorageException("there is no value with the key $key")
    }

    /**
     * gets a value from the storage item as Date Object
     *
     * @param key key of the value to get
     * @return matching value
     * @throws StorageException
     */
    @Throws(StorageException::class)
    fun getDate(key: String?): Date? {
        throw StorageException("there is no value with the key $key")
    }

    /**
     * gets a value from the storage item as Time Object
     *
     * @param key key of the value to get
     * @return matching value
     * @throws StorageException
     */
    @Throws(StorageException::class)
    fun getTime(key: String?): Time? {
        throw StorageException("there is no value with the key $key")
    }

    /**
     * gets a value from the storage item as Date Object
     *
     * @param key key of the value to get
     * @return matching value
     * @throws StorageException
     */
    @Throws(StorageException::class)
    fun getDateTime(key: String?): DateTime? {
        throw StorageException("there is no value with the key $key")
    }

    /**
     * sets a value to the storage item as String
     *
     * @param key key of the value to set
     * @param value value to set
     * @throws StorageException
     */
    @Throws(StorageException::class)
    fun setString(key: String?, value: String?) {
        throw StorageException("key $key is not supported for this item")
    }

    /**
     * sets a value to the storage item as int
     *
     * @param key key of the value to set
     * @param value value to set
     * @throws StorageException
     */
    @Throws(StorageException::class)
    fun setInt(key: String?, value: Int) {
        throw StorageException("key $key is not supported for this item")
    }

    /**
     * sets a value to the storage item as Date Object
     *
     * @param key key of the value to set
     * @param value value to set
     * @throws StorageException
     */
    @Throws(StorageException::class)
    fun setDate(key: String?, value: Date?) {
        throw StorageException("key $key is not supported for this item")
    }

    /**
     * sets a value to the storage item as Time Object
     *
     * @param key key of the value to set
     * @param value value to set
     * @throws StorageException
     */
    @Throws(StorageException::class)
    fun setTime(key: String?, value: Time?) {
        throw StorageException("key $key is not supported for this item")
    }

    /**
     * sets a value to the storage item as DateTime Object
     *
     * @param key key of the value to set
     * @param value value to set
     * @throws StorageException
     */
    @Throws(StorageException::class)
    fun setDateTime(key: String?, value: DateTime?) {
        throw StorageException("key $key is not supported for this item")
    }
}