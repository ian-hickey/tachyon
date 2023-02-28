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

import java.util.Date

/**
 * class to compare objects and primitive value types
 */
interface Operation {
    /**
     * compares two Objects
     *
     * @param left Left Object
     * @param right Right Object
     * @return different of objects as int
     * @throws PageException Page Exception
     */
    @Throws(PageException::class)
    fun compare(left: Object?, right: Object?): Int

    /**
     * compares an Object with a String
     *
     * @param left Left Object
     * @param right Right Object
     * @return difference as int
     * @throws PageException Page Exception
     */
    @Throws(PageException::class)
    fun compare(left: Object?, right: String?): Int

    /**
     * compares an Object with a double
     *
     * @param left Left Object
     * @param right Right Object
     * @return difference as int
     * @throws PageException Page Exception
     */
    @Throws(PageException::class)
    fun compare(left: Object?, right: Double): Int

    /**
     * compares an Object with a boolean
     *
     * @param left Left Object
     * @param right Right Object
     * @return difference as int
     * @throws PageException Page Exception
     */
    @Throws(PageException::class)
    fun compare(left: Object?, right: Boolean): Int

    /**
     * compares an Object with a Date
     *
     * @param left Left Object
     * @param right Right Object
     * @return difference as int
     * @throws PageException Page Exception
     */
    @Throws(PageException::class)
    fun compare(left: Object?, right: Date?): Int

    /**
     * compares a String with an Object
     *
     * @param left Left Object
     * @param right Right Object
     * @return difference as int
     * @throws PageException Page Exception
     */
    @Throws(PageException::class)
    fun compare(left: String?, right: Object?): Int

    /**
     * compares a String with a String
     *
     * @param left Left Object
     * @param right Right Object
     * @return difference as int
     */
    fun compare(left: String?, right: String?): Int

    /**
     * compares a String with a double
     *
     * @param left Left Object
     * @param right Right Object
     * @return difference as int
     */
    fun compare(left: String?, right: Double): Int

    /**
     * compares a String with a boolean
     *
     * @param left Left Object
     * @param right Right Object
     * @return difference as int
     */
    fun compare(left: String?, right: Boolean): Int

    /**
     * compares a String with a Date
     *
     * @param left Left Object
     * @param right Right Object
     * @return difference as int
     * @throws PageException Page Exception
     */
    @Throws(PageException::class)
    fun compare(left: String?, right: Date?): Int

    /**
     * compares a double with an Object
     *
     * @param left Left Object
     * @param right Right Object
     * @return difference as int
     * @throws PageException Page Exception
     */
    @Throws(PageException::class)
    fun compare(left: Double, right: Object?): Int

    /**
     * compares a double with a String
     *
     * @param left Left Object
     * @param right Right Object
     * @return difference as int
     */
    fun compare(left: Double, right: String?): Int

    /**
     * compares a double with a double
     *
     * @param left Left Object
     * @param right Right Object
     * @return difference as int
     */
    fun compare(left: Double, right: Double): Int

    /**
     * compares a double with a boolean
     *
     * @param left Left Object
     * @param right Right Object
     * @return difference as int
     */
    fun compare(left: Double, right: Boolean): Int

    /**
     * compares a double with a Date
     *
     * @param left Left Object
     * @param right Right Object
     * @return difference as int
     */
    fun compare(left: Double, right: Date?): Int

    /**
     * compares a boolean with an Object
     *
     * @param left Left Object
     * @param right Right Object
     * @return difference as int
     * @throws PageException Page Exception
     */
    @Throws(PageException::class)
    fun compare(left: Boolean, right: Object?): Int

    /**
     * compares a boolean with a double
     *
     * @param left Left Object
     * @param right Right Object
     * @return difference as int
     */
    fun compare(left: Boolean, right: Double): Int

    /**
     * compares a boolean with a double
     *
     * @param left Left Object
     * @param right Right Object
     * @return difference as int
     */
    fun compare(left: Boolean, right: String?): Int

    /**
     * compares a boolean with a boolean
     *
     * @param left Left Object
     * @param right Right Object
     * @return difference as int
     */
    fun compare(left: Boolean, right: Boolean): Int

    /**
     * compares a boolean with a Date
     *
     * @param left Left Object
     * @param right Right Object
     * @return difference as int
     */
    fun compare(left: Boolean, right: Date?): Int

    /**
     * compares a Date with an Object
     *
     * @param left Left Object
     * @param right Right Object
     * @return difference as int
     * @throws PageException Page Exception
     */
    @Throws(PageException::class)
    fun compare(left: Date?, right: Object?): Int

    /**
     * compares a Date with a String
     *
     * @param left Left Object
     * @param right Right Object
     * @return difference as int
     * @throws PageException Page Exception
     */
    @Throws(PageException::class)
    fun compare(left: Date?, right: String?): Int

    /**
     * compares a Date with a double
     *
     * @param left Left Object
     * @param right Right Object
     * @return difference as int
     */
    fun compare(left: Date?, right: Double): Int

    /**
     * compares a Date with a boolean
     *
     * @param left Left Object
     * @param right Right Object
     * @return difference as int
     */
    fun compare(left: Date?, right: Boolean): Int

    /**
     * compares a Date with a Date
     *
     * @param left Left Object
     * @param right Right Object
     * @return difference as int
     */
    fun compare(left: Date?, right: Date?): Int

    /**
     * Method to compare to different values, return true of objects are same otherwise false
     *
     * @param left left value to compare
     * @param right right value to compare
     * @param caseSensitive check case sensitive or not
     * @return is same or not
     * @throws PageException Page Exception
     */
    @Throws(PageException::class)
    fun equals(left: Object?, right: Object?, caseSensitive: Boolean): Boolean
    fun equalsComplexEL(left: Object?, right: Object?, caseSensitive: Boolean, checkOnlyPublicAppearance: Boolean): Boolean

    @Throws(PageException::class)
    fun equalsComplex(left: Object?, right: Object?, caseSensitive: Boolean, checkOnlyPublicAppearance: Boolean): Boolean

    /**
     * check if left is inside right
     *
     * @param left string to check
     * @param right substring to find in string
     * @return return if substring has been found
     * @throws PageException Page Exception
     */
    @Throws(PageException::class)
    fun ct(left: Object?, right: Object?): Boolean

    /**
     * Equivalence: Return True if both operands are True or both are False. The EQV operator is the
     * opposite of the XOR operator. For example, True EQV True is True, but True EQV False is False.
     *
     * @param left value to check
     * @param right value to check
     * @return result of operation
     * @throws PageException Page Exception
     */
    @Throws(PageException::class)
    fun eqv(left: Object?, right: Object?): Boolean

    /**
     * Implication: The statement A IMP B is the equivalent of the logical statement "If A Then B." A
     * IMP B is False only if A is True and B is False. It is True in all other cases.
     *
     * @param left value to check
     * @param right value to check
     * @return result
     * @throws PageException Page Exception
     */
    @Throws(PageException::class)
    fun imp(left: Object?, right: Object?): Boolean

    /**
     * check if left is not inside right
     *
     * @param left string to check
     * @param right substring to find in string
     * @return return if substring NOT has been found
     * @throws PageException Page Exception
     */
    @Throws(PageException::class)
    fun nct(left: Object?, right: Object?): Boolean

    /**
     * calculate the exponent of the left value
     *
     * @param left value to get exponent from
     * @param right exponent count
     * @return return exponent value
     * @throws PageException Page Exception
     */
    @Throws(PageException::class)
    fun exponent(left: Object?, right: Object?): Double

    /**
     * concat to Strings
     *
     * @param left Left Object
     * @param right Right Object
     * @return concatenated String
     */
    fun concat(left: String?, right: String?): String?

    /**
     * plus operation
     *
     * @param left Left Object
     * @param right Right Object
     * @return result of the operations
     */
    fun plus(left: Double, right: Double): Double

    /**
     * minus operation
     *
     * @param left Left Object
     * @param right Right Object
     * @return result of the operations
     */
    fun minus(left: Double, right: Double): Double

    /**
     * modulus operation
     *
     * @param left Left Object
     * @param right Right Object
     * @return result of the operations
     */
    fun modulus(left: Double, right: Double): Double

    /**
     * divide operation
     *
     * @param left Left Object
     * @param right Right Object
     * @return result of the operations
     */
    fun divide(left: Double, right: Double): Double

    /**
     * multiply operation
     *
     * @param left Left Object
     * @param right Right Object
     * @return result of the operations
     */
    fun multiply(left: Double, right: Double): Double
}