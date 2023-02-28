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
package lucee.runtime.util

import lucee.runtime.PageContext

/**
 * Variable Util
 */
interface VariableUtil {
    /**
     * return a property from the given Object, when property doesn't exists return null
     *
     * @param pc Page Context
     * @param coll Collection to check
     * @param key to get from Collection
     * @param defaultValue default value
     * @return value or null
     */
    @Deprecated
    @Deprecated("use instead")
    fun getCollection(pc: PageContext?, coll: Object?, key: String?, defaultValue: Object?): Object?

    /**
     * return a property from the given Object, when property doesn't exists return null
     *
     * @param pc Page Context
     * @param coll Collection to check
     * @param key to get from Collection
     * @param defaultValue default value
     * @return value or null
     */
    fun getCollection(pc: PageContext?, coll: Object?, key: Collection.Key?, defaultValue: Object?): Object?

    /**
     * return a property from the given Object, when property doesn't exists return null
     *
     * @param pc Page Context
     * @param coll Collection to check
     * @param key to get from Collection
     * @param defaultValue default value
     * @return value or null
     */
    @Deprecated
    @Deprecated("""use instead
	              <code>get(PageContext pc, Object coll, Collection.Key key, Object defaultValue);</code>""")
    operator fun get(pc: PageContext?, coll: Object?, key: String?, defaultValue: Object?): Object?

    /**
     * return a property from the given Object, when property doesn't exists return null
     *
     * @param pc Page Context
     * @param coll Collection to check
     * @param key to get from Collection
     * @param defaultValue default value
     * @return value or null
     */
    operator fun get(pc: PageContext?, coll: Object?, key: Collection.Key?, defaultValue: Object?): Object?

    /**
     * return a property from the given Object, when property doesn't exists return null
     *
     * @param pc Page Context
     * @param coll Collection to check
     * @param key to get from Collection
     * @param defaultValue default value
     * @return value or null
     */
    fun getLight(pc: PageContext?, coll: Object?, key: String?, defaultValue: Object?): Object?

    /**
     * return a property from the given Object, when coll is a query return a Column,when property
     * doesn't exists throw exception
     *
     * @param pc Page Context
     * @param coll Collection to check
     * @param key to get from Collection
     * @return value value to get
     * @throws PageException Page Context
     */
    @Throws(PageException::class)
    fun getCollection(pc: PageContext?, coll: Object?, key: String?): Object?

    /**
     * return a property from the given Object, when property doesn't exists throw exception
     *
     * @param pc Page Context
     * @param coll Collection to check
     * @param key to get from Collection
     * @return value value to get
     * @throws PageException Page Context
     */
    @Throws(PageException::class)
    operator fun get(pc: PageContext?, coll: Object?, key: String?): Object?

    /**
     * sets a value to the Object
     *
     * @param pc Page Context
     * @param coll Collection to check
     * @param key to get from Collection
     * @param value Value to set
     * @return value setted
     * @throws PageException Page Context
     */
    @Deprecated
    @Deprecated("""use instead
	              <code>set(PageContext pc, Object coll, Collection.Key key,Object value)</code>""")
    @Throws(PageException::class)
    operator fun set(pc: PageContext?, coll: Object?, key: String?, value: Object?): Object?

    @Throws(PageException::class)
    operator fun set(pc: PageContext?, coll: Object?, key: Collection.Key?, value: Object?): Object?

    /**
     * sets a value to the Object
     *
     * @param pc Page Context
     * @param coll Collection to check
     * @param key to get from Collection
     * @param value Value to set
     * @return value setted or null if can't set
     */
    @Deprecated
    @Deprecated("""use instead
	              <code>setEL(PageContext pc, Object coll, Collection.Key key,Object value);</code>""")
    fun setEL(pc: PageContext?, coll: Object?, key: String?, value: Object?): Object?

    /**
     * sets a value to the Object
     *
     * @param pc Page Context
     * @param coll Collection to check
     * @param key to get from Collection
     * @param value Value to set
     * @return value setted or null if can't set
     */
    fun setEL(pc: PageContext?, coll: Object?, key: Collection.Key?, value: Object?): Object?

    /**
     * remove value from Collection
     *
     * @param coll Collection
     * @param key key
     * @return has cleared or not
     */
    @Deprecated
    fun removeEL(coll: Object?, key: String?): Object?
    fun removeEL(coll: Object?, key: Collection.Key?): Object?

    /**
     * clear value from Collection
     *
     * @param coll Collection
     * @param key key
     * @return has cleared or not
     * @throws PageException Page Context
     */
    @Deprecated
    @Throws(PageException::class)
    fun remove(coll: Object?, key: String?): Object?

    @Throws(PageException::class)
    fun remove(coll: Object?, key: Collection.Key?): Object?

    /**
     * call a Function (UDF, Method) with or witout named values
     *
     * @param pc Page Context
     * @param coll Collection of the UDF Function
     * @param key name of the function
     * @param args arguments to call the function
     * @return return value of the function
     * @throws PageException Page Context
     */
    @Throws(PageException::class)
    fun callFunction(pc: PageContext?, coll: Object?, key: String?, args: Array<Object?>?): Object?

    /**
     * call a Function (UDF, Method) without Named Values
     *
     * @param pc Page Context
     * @param coll Collection of the UDF Function
     * @param key name of the function
     * @param args arguments to call the function
     * @return return value of the function
     * @throws PageException Page Context
     */
    @Deprecated
    @Deprecated("""use instead
	              <code>callFunctionWithoutNamedValues(PageContext pc, Object coll, Collection.Key key, Object[] args)</code>""")
    @Throws(PageException::class)
    fun callFunctionWithoutNamedValues(pc: PageContext?, coll: Object?, key: String?, args: Array<Object?>?): Object?

    /**
     * call a Function (UDF, Method) without Named Values
     *
     * @param pc Page Context
     * @param coll Collection of the UDF Function
     * @param key name of the function
     * @param args arguments to call the function
     * @return return value of the function
     * @throws PageException Page Context
     */
    @Throws(PageException::class)
    fun callFunctionWithoutNamedValues(pc: PageContext?, coll: Object?, key: Collection.Key?, args: Array<Object?>?): Object?

    /**
     * call a Function (UDF, Method) with Named Values
     *
     * @param pc Page Context
     * @param coll Collection of the UDF Function
     * @param key name of the function
     * @param args arguments to call the function
     * @return return value of the function
     * @throws PageException Page Context
     */
    @Deprecated
    @Deprecated("""use instead
	              <code>callFunctionWithNamedValues(PageContext pc, Object coll, Collection.Key key, Object[] args)</code>""")
    @Throws(PageException::class)
    fun callFunctionWithNamedValues(pc: PageContext?, coll: Object?, key: String?, args: Array<Object?>?): Object?

    /**
     * call a Function (UDF, Method) with Named Values
     *
     * @param pc Page Context
     * @param coll Collection of the UDF Function
     * @param key name of the function
     * @param args arguments to call the function
     * @return return value of the function
     * @throws PageException Page Context
     */
    @Throws(PageException::class)
    fun callFunctionWithNamedValues(pc: PageContext?, coll: Object?, key: Collection.Key?, args: Array<Object?>?): Object?

    @Throws(PageException::class)
    fun callFunctionWithNamedValues(pc: PageContext?, coll: Object?, key: Collection.Key?, args: Struct?): Object?
}