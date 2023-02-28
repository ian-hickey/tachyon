/**
 * Copyright (c) 2014, the Railo Company Ltd.
 * Copyright (c) 2015, Lucee Assosication Switzerland
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
package lucee.runtime.type

import java.io.Serializable
/**
 * interface collection, used for all collection types of Lucee (array, struct, query)
 */
interface Collection : Dumpable, Iteratorable, Cloneable, Serializable, Castable, ForEachIteratorableCloneable{
    /**
     * @return the size of the collection
     */
    fun size(): Int

    /**
     * @return returns a string array of all keys in the collection
     */
    @Deprecated
    @Deprecated("use instead <code>keyIterator()</code>")
    fun keys(): Array<Collection.Key?>?

    /**
     * removes value from collection and return it when it exists, otherwise throws an exception
     *
     * @param key key of the collection
     * @return removed Object
     * @throws PageException thrown when cannot remove value
     */
    @Throws(PageException::class)
    fun remove(key: Collection.Key?): Object?

    /**
     * removes value from collection and return it when it exists, otherwise returns null
     *
     * @param key key of the collection
     * @return removed Object
     */
    fun removeEL(key: Collection.Key?): Object?

    /**
     * removes value from collection and return it when it exists, otherwise returns the given default
     * value
     *
     * @param key key of the collection
     * @param defaultValue value to return if the entry does not exist
     * @return removed Object
     */
    fun remove(key: Collection.Key?, defaultValue: Object?): Object?

    /**
     * clears the collection
     */
    fun clear()

    /**
     * return a value from the collection
     *
     * @param key key of the value to get
     * @return value on key position
     * @throws PageException thrown when no value exist for given key
     */
    @Deprecated
    @Deprecated("use instead <code>{@link #get(lucee.runtime.type.Collection.Key)}</code>")
    @Throws(PageException::class)
    fun get(key: String?): Object?

    /**
     * return a value from the collection
     *
     * @param key key of the value to get must be lower case
     * @return value on key position
     * @throws PageException thrown when no value exist for given key
     */
    @Throws(PageException::class)
    fun get(key: Collection.Key?): Object?

    /**
     * return a value from the collection, if key doesn't exist, dont throw an exception, returns null
     *
     * @param key key of the value to get
     * @param defaultValue value returned when no value exists for given key
     * @return value on key position or null
     */
    @Deprecated
    @Deprecated("use instead <code>{@link #get(lucee.runtime.type.Collection.Key, Object)}</code>")
    fun get(key: String?, defaultValue: Object?): Object?

    /**
     * return a value from the collection, if key doesn't exist, dont throw an exception, returns null
     *
     * @param key key of the value to get
     * @param defaultValue value returned when no value exists for given key
     * @return value on key position or null
     */
    fun get(key: Collection.Key?, defaultValue: Object?): Object?

    /**
     * sets a value to the collection
     *
     * @param key key of the new value
     * @param value value to set
     * @return value setted
     * @throws PageException exception thrown when fails to set the value
     */
    @Deprecated
    @Deprecated("use instead <code>{@link #set(lucee.runtime.type.Collection.Key, Object)}</code>")
    @Throws(PageException::class)
    fun set(key: String?, value: Object?): Object?

    /**
     * sets a value to the collection
     *
     * @param key key of the new value
     * @param value value to set
     * @return value setted
     * @throws PageException exception thrown when fails to set the value
     */
    @Throws(PageException::class)
    fun set(key: Collection.Key?, value: Object?): Object?

    /**
     * sets a value to the collection, if key doesn't exist, dont throw an exception, returns null
     *
     * @param key key of the value to get
     * @param value value to set
     * @return value on key position or null
     */
    @Deprecated
    @Deprecated("use instead <code>{@link #setEL(lucee.runtime.type.Collection.Key, Object)}</code>")
    fun setEL(key: String?, value: Object?): Object?

    /**
     * sets a value to the collection, if key doesn't exist, dont throw an exception, returns null
     *
     * @param key key of the value to get
     * @param value value to set
     * @return value on key position or null
     */
    fun setEL(key: Collection.Key?, value: Object?): Object?

    /**
     * @return this object cloned
     */
    fun clone(): Object
    fun duplicate(deepCopy: Boolean): Collection?

    /**
     * contains this key
     *
     * @param key key to check for
     * @return returns if collection has a key with given name
     */
    @Deprecated
    @Deprecated("use instead <code>{@link #containsKey(lucee.runtime.type.Collection.Key)}</code>")
    fun containsKey(key: String?): Boolean

    /**
     * contains this key
     *
     * @param key key to check for
     * @return returns if collection has a key with given name
     */
    fun containsKey(key: Collection.Key?): Boolean
    interface Key : Serializable {
        /**
         * return key as String
         * @return string
         */
        val string: String?

        /**
         * return key as lower case String
         * @return lower case string
         */
        val lowerString: String?

        /**
         * return key as upper case String
         * @return upper case string
         */
        val upperString: String?

        /**
         * return char at given position
         *
         * @param index index
         * @return character at given position
         */
        fun charAt(index: Int): Char

        /**
         * return lower case char a given position
         *
         * @param index index
         * @return lower case char from given position
         */
        fun lowerCharAt(index: Int): Char

        /**
         * return upper case char a given position
         *
         * @param index index
         * @return upper case char from given position
         */
        fun upperCharAt(index: Int): Char

        /**
         * compare to object, ignore case of input
         *
         * @param key key
         * @return is equal to given key?
         */
        fun equalsIgnoreCase(key: Collection.Key?): Boolean
        fun hash(): Long

        /**
         * Returns the length of this string.
         *
         * @return length of the string
         */
        fun length(): Int // Future add; returns a 64 bit based hashcode for the Key
        // public long hash();
    }
}