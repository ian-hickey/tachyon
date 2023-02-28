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

import java.util.Comparator

/**
 *
 */
interface Array : Collection, Cloneable, Objects {
    /**
     * return dimension of the array
     *
     * @return dimension of the array
     */
    val dimension: Int

    /**
     * return object a given position, key can only be an integer from 1 to array len
     *
     * @param key key as integer
     * @param defaultValue default value
     * @return value at key position
     */
    operator fun get(key: Int, defaultValue: Object?): Object?

    /**
     * return object a given position, key can only be an integer from 1 to array len
     *
     * @param key key as integer
     * @return value at key position
     * @throws PageException Page Exception
     */
    @Throws(PageException::class)
    fun getE(key: Int): Object?

    /**
     * set value at defined position, on error return null
     *
     * @param key key of the new value
     * @param value value to set
     * @return setted value
     */
    fun setEL(key: Int, value: Object?): Object?

    /**
     * set value at defined position
     *
     * @param key key
     * @param value value
     * @return defined value
     * @throws PageException Page Exception
     */
    @Throws(PageException::class)
    fun setE(key: Int, value: Object?): Object?

    /**
     * @return return all array keys as int
     */
    fun intKeys(): IntArray?

    /**
     * insert a value add defined position
     *
     * @param key position to insert
     * @param value value to insert
     * @return has done or not
     * @throws PageException Page Exception
     */
    @Throws(PageException::class)
    fun insert(key: Int, value: Object?): Boolean

    /**
     * append a new value to the end of the array
     *
     * @param o value to insert
     * @return inserted value
     * @throws PageException Page Exception
     */
    @Throws(PageException::class)
    fun append(o: Object?): Object?
    fun appendEL(o: Object?): Object?

    /**
     * add a new value to the begin of the array
     *
     * @param o value to insert
     * @return inserted value
     * @throws PageException Page Exception
     */
    @Throws(PageException::class)
    fun prepend(o: Object?): Object?

    /**
     * resize array to defined size
     *
     * @param to new minimum size of the array
     * @throws PageException Page Exception
     */
    @Throws(PageException::class)
    fun resize(to: Int)

    /**
     * sort values of an array
     *
     * @param sortType search type (text,textnocase,numeric)
     * @param sortOrder (asc,desc)
     * @throws PageException Page Exception
     */
    @Deprecated
    @Deprecated("use instead <code>sort(Comparator comp)</code>")
    @Throws(PageException::class)
    fun sort(sortType: String?, sortOrder: String?)
    fun sortIt(@SuppressWarnings("rawtypes") comp: Comparator?) // this name was chosen to avoid conflict with java.util.List

    /**
     * @return return array as native (Java) Object Array
     */
    fun toArray(): Array<Object?>?

    /**
     * @return return array as ArrayList
     */
    // public ArrayList toArrayList();
    @SuppressWarnings("rawtypes")
    fun toList(): List?

    /**
     * removes a value ad defined key
     *
     * @param key key to remove
     * @return returns if value is removed or not
     * @throws PageException Page Exception
     */
    @Throws(PageException::class)
    fun removeE(key: Int): Object?

    /**
     * removes a value ad defined key
     *
     * @param key key to remove
     * @return returns if value is removed or not
     */
    fun removeEL(key: Int): Object?

    /**
     * contains this key
     *
     * @param key key
     * @return returns if collection has a key with given name
     */
    fun containsKey(key: Int): Boolean
}