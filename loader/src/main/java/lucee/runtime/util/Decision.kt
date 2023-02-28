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

import lucee.runtime.exp.PageException

/**
 * Object to test if an Object is a specific type
 */
interface Decision {
    fun isAnyType(type: String?): Boolean

    /**
     * tests if value is a simple value (Number,String,Boolean,Date,Printable)
     *
     * @param value value to test
     * @return is value a simple value
     */
    fun isSimpleValue(value: Object?): Boolean

    /**
     * tests if value is Numeric
     *
     * @param value value to test
     * @return is value numeric
     */
    fun isNumber(value: Object?): Boolean

    /**
     * tests if String value is Numeric
     *
     * @param str value to test
     * @return is value numeric
     */
    fun isNumber(str: String?): Boolean

    /**
     * @param value value to test
     * @return is value numeric
     */
    @Deprecated
    @Deprecated("use instead isNumber")
    fun isNumeric(value: Object?): Boolean

    /**
     *
     * @param str value to test
     * @return is value numeric
     */
    @Deprecated
    @Deprecated("use instead isNumber")
    fun isNumeric(str: String?): Boolean

    /**
     * tests if String value is Hex Value
     *
     * @param str value to test
     * @return is value numeric
     */
    fun isHex(str: String?): Boolean

    /**
     * tests if String value is UUID Value
     *
     * @param str value to test
     * @return is value numeric
     */
    fun isUUID(str: String?): Boolean

    /**
     * tests if value is a Boolean (Numbers are not accepted)
     *
     * @param value value to test
     * @return is value boolean
     */
    fun isBoolean(value: Object?): Boolean

    /**
     * tests if value is a Boolean
     *
     * @param str value to test
     * @return is value boolean
     */
    fun isBoolean(str: String?): Boolean

    /**
     * tests if value is DateTime Object
     *
     * @param value value to test
     * @param alsoNumbers interpret also a number as date
     * @return is value a DateTime Object
     */
    fun isDate(value: Object?, alsoNumbers: Boolean): Boolean

    /**
     * tests if object is a struct
     *
     * @param o Object
     * @return is struct or not
     */
    fun isStruct(o: Object?): Boolean

    /**
     * tests if object is an array
     *
     * @param o Object
     * @return is array or not
     */
    fun isArray(o: Object?): Boolean

    /**
     * tests if object is a native java array
     *
     * @param o Object
     * @return is a native (java) array
     */
    fun isNativeArray(o: Object?): Boolean

    /**
     * tests if object is a binary
     *
     * @param object Object
     * @return boolean
     */
    fun isBinary(`object`: Object?): Boolean

    /**
     * tests if object is a Component
     *
     * @param object Object
     * @return boolean
     */
    fun isComponent(`object`: Object?): Boolean

    /**
     * tests if object is a Query
     *
     * @param object Object
     * @return boolean
     */
    fun isQuery(`object`: Object?): Boolean

    /**
     * tests if object is a binary
     *
     * @param object Object
     * @return boolean
     */
    fun isUserDefinedFunction(`object`: Object?): Boolean

    /**
     * tests if year is a leap year
     *
     * @param year year to check
     * @return boolean
     */
    fun isLeapYear(year: Int): Boolean

    /**
     * tests if object is a WDDX Object
     *
     * @param o Object to check
     * @return boolean
     */
    fun isWddx(o: Object?): Boolean

    /**
     * tests if object is a XML Object
     *
     * @param o Object to check
     * @return boolean
     */
    fun isXML(o: Object?): Boolean

    /**
     * tests if object is a XML Element Object
     *
     * @param o Object to check
     * @return boolean
     */
    fun isXMLElement(o: Object?): Boolean

    /**
     * tests if object is a XML Document Object
     *
     * @param o Object to check
     * @return boolean
     */
    fun isXMLDocument(o: Object?): Boolean

    /**
     * tests if object is a XML Root Element Object
     *
     * @param o Object to check
     * @return boolean
     */
    fun isXMLRootElement(o: Object?): Boolean

    /**
     * @param string string
     * @return returns if string represent a variable name
     */
    fun isVariableName(string: String?): Boolean

    /**
     * @param string string
     * @return returns if string represent a variable name
     */
    fun isSimpleVariableName(string: String?): Boolean

    /**
     * returns if object is a CFML object
     *
     * @param o Object to check
     * @return is or not
     */
    fun isObject(o: Object?): Boolean

    /**
     *
     * @param str string
     * @return return if a String is "Empty", that means NULL or String with length 0 (whitespaces will
     * not counted)
     */
    fun isEmpty(str: String?): Boolean

    /**
     *
     * @param str string
     * @param trim trim
     * @return return if a String is "Empty", that means NULL or String with length 0 (whitespaces will
     * not counted)
     */
    fun isEmpty(str: String?, trim: Boolean): Boolean

    @Throws(PageException::class)
    fun toKey(obj: Object?): Key?
    fun toKey(obj: Object?, defaultValue: Collection.Key?): Key?

    /**
     * Checks if number is valid (not infinity or NaN)
     *
     * @param dbl double
     * @return Returns if the number is valid.
     */
    fun isValid(dbl: Double): Boolean
    fun isCastableTo(type: String?, o: Object?, alsoAlias: Boolean, alsoPattern: Boolean, maxlength: Int): Boolean
    fun isCastableToArray(o: Object?): Boolean
    fun isCastableToBinary(`object`: Object?, checkBase64String: Boolean): Boolean
    fun isCastableToBoolean(obj: Object?): Boolean
    fun isCastableToDate(o: Object?): Boolean
    fun isCastableToNumeric(o: Object?): Boolean
    fun isCastableToString(o: Object?): Boolean
    fun isCastableToStruct(o: Object?): Boolean
    fun isClosure(o: Object?): Boolean
    fun isLambda(o: Object?): Boolean
    fun isFunction(o: Object?): Boolean
    fun isCreditCard(o: Object?): Boolean
    fun isEmpty(o: Object?): Boolean
    fun isGUid(o: Object?): Boolean

    /**
     *
     * @param type type
     * @param o object
     * @return Returns if Object is the Type.
     * @throws PageException when type is unknown
     */
    @Throws(PageException::class)
    fun `is`(type: String?, o: Object?): Boolean
}