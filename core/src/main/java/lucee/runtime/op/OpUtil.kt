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
package lucee.runtime.op

import java.io.IOException

/**
 * class to compare objects and primitive value types
 *
 *
 */
object OpUtil {
    /**
     * compares two Objects
     *
     * @param left
     * @param right
     * @return different of objects as int
     * @throws PageException
     */
    @Throws(PageException::class)
    fun compare(pc: PageContext?, left: Object?, right: Object?): Int {
        // print.dumpStack();
        return if (left is String) compare(pc, left as String?, right) else if (left is Number) compare(pc, left as Number?, right) else if (left is Boolean) compare(pc, left as Boolean?, right) else if (left is Date) compare(pc, left as Date?, right) else if (left is Castable) compare(pc, left as Castable?, right) else if (left is Locale) compare(pc, left as Locale?, right) else if (left == null) compare(pc, "", right) else if (left is Enum) compare(pc, (left as Enum?).toString(), right) else if (left is Character) compare(pc, (left as Character?).toString(), right) else if (left is Calendar) compare(pc, (left as Calendar?).getTime(), right) else if (left is TimeZone) compare(pc, left as TimeZone?, right) else {
            error(false, true)
        }
    }

    @Throws(PageException::class)
    fun compare(pc: PageContext?, left: TimeZone?, right: Object?): Int {
        return if (right is String) compare(pc, left, right as String?) else if (right is Number) compare(pc, left, Caster.toString(right)) else if (right is Boolean) compare(pc, left, Caster.toString(right)) else if (right is Date) compare(pc, left, Caster.toString(right)) else if (right is Castable) {
            if (isComparableComponent(right as Castable?)) -compareComponent(pc, right as Castable?, left) else compare(pc, left, (right as Castable?).castToString())
        } else if (right is TimeZone) left.toString().compareTo(right.toString()) else if (right == null) compare(pc, left, "") else if (right is Enum) compare(pc, left, (right as Enum?).toString()) else if (right is Character) compare(pc, left, (right as Character?).toString()) else if (right is Calendar) compare(pc, left, Caster.toString((right as Calendar?).getTime())) else if (right is Locale) compare(pc, left, Caster.toString(right)) else error(false, true)
    }

    @Throws(PageException::class)
    fun compare(pc: PageContext?, left: Locale?, right: Object?): Int {
        return if (right is String) compare(pc, left, right as String?) else if (right is Number) compare(pc, left, Caster.toString(right)) else if (right is Boolean) compare(pc, left, Caster.toString(right)) else if (right is Date) compare(pc, left, Caster.toString(right)) else if (right is Castable) {
            if (isComparableComponent(right as Castable?)) -compareComponent(pc, right as Castable?, left) else compare(pc, left, (right as Castable?).castToString())
        } else if (right is Locale) left.toString().compareTo(right.toString()) else if (right == null) compare(pc, left, "") else if (right is Enum) compare(pc, left, (right as Enum?).toString()) else if (right is Character) compare(pc, left, (right as Character?).toString()) else if (right is Calendar) compare(pc, left, Caster.toString((right as Calendar?).getTime())) else if (right is TimeZone) compare(pc, left, Caster.toString(right)) else error(false, true)
    }

    fun compare(pc: PageContext?, left: Locale?, right: String?): Int {
        val rightLocale: Locale = LocaleFactory.getLocale(right, null)
                ?: return LocaleFactory.toString(left).compareTo(right)
        return left.toString().compareTo(rightLocale.toString())
    }

    fun compare(pc: PageContext?, left: TimeZone?, right: String?): Int {
        val rtz: TimeZone = TimeZoneUtil.toTimeZone(right, null) ?: return TimeZoneUtil.toString(left).compareTo(right)
        return left.toString().compareTo(rtz.toString())
    }

    /**
     * compares an Object with a String
     *
     * @param left
     * @param right
     * @return difference as int
     * @throws PageException
     */
    @Throws(PageException::class)
    fun compare(pc: PageContext?, left: Object?, right: String?): Int {
        return if (left is String) compare(pc, left as String?, right) else if (left is Number) compare(pc, left as Number?, right) else if (left is Boolean) compare(pc, left as Boolean?, right) else if (left is Date) compare(pc, left as Date?, right) else if (left is Castable) {
            if (isComparableComponent(left as Castable?)) compareComponent(pc, left as Castable?, right) else (left as Castable?).compareTo(right)
        } else if (left is Locale) compare(pc, left as Locale?, right) else if (left == null) "".compareToIgnoreCase(right) else if (left is Enum) compare(pc, (left as Enum?).toString(), right) else if (left is Character) compare(pc, (left as Character?).toString(), right) else if (left is Calendar) compare(pc, (left as Calendar?).getTime(), right) else if (left is TimeZone) compare(pc, left as TimeZone?, right) else error(false, true)
    }

    /**
     * compares a String with an Object
     *
     * @param left
     * @param right
     * @return difference as int
     * @throws PageException
     */
    @Throws(PageException::class)
    fun compare(pc: PageContext?, left: String?, right: Object?): Int {
        return if (right is String) compare(pc, left, right as String?) else if (right is Number) compare(pc, left, right as Number?) else if (right is Boolean) compare(pc, left, if ((right as Boolean?)!!) BigDecimal.ONE else BigDecimal.ZERO) else if (right is Date) compare(pc, left, right as Date?) else if (right is Castable) {
            if (isComparableComponent(right as Castable?)) -compareComponent(pc, right as Castable?, left) else -(right as Castable?).compareTo(left)
            // compare(left ,((Castable)right).castToString());
        } else if (right is Locale) compare(pc, left, right as Locale?) else if (right == null) left.compareToIgnoreCase("") else if (right is Enum) compare(pc, left, (right as Enum?).toString()) else if (right is Character) compare(pc, left, (right as Character?).toString()) else if (right is Calendar) compare(pc, left, (right as Calendar?).getTime()) else if (right is TimeZone) compare(pc, left, right as TimeZone?) else error(false, true)
    }

    /**
     * compares an Object with a double
     *
     * @param left
     * @param right
     * @return difference as int
     * @throws PageException
     */
    @Throws(PageException::class)
    fun compare(pc: PageContext?, left: Object?, right: Number?): Int {
        return if (left is Number) compare(pc, left as Number?, right) else if (left is String) compare(pc, left as String?, right) else if (left is Boolean) compare(pc, if ((left as Boolean?).booleanValue()) BigDecimal.ONE else BigDecimal.ZERO, right) else if (left is Date) compare(pc, left as Date?, right) else if (left is Castable) {
            if (isComparableComponent(left as Castable?)) compareComponent(pc, left as Castable?, right) else (left as Castable?).compareTo(right.doubleValue())
        } else if (left is Locale) compare(pc, left as Locale?, Caster.toString(right)) else if (left == null) -1 else if (left is Enum) compare(pc, (left as Enum?).toString(), right) else if (left is Character) compare(pc, (left as Character?).toString(), right) else if (left is Calendar) compare(pc, (left as Calendar?).getTime(), right) else if (left is TimeZone) compare(pc, left as TimeZone?, Caster.toString(right)) else {
            error(false, true)
        }
    }

    /**
     * compares a double with an Object
     *
     * @param left
     * @param right
     * @return difference as int
     * @throws PageException
     */
    @Throws(PageException::class)
    fun compare(pc: PageContext?, left: Number?, right: Object?): Int {
        return if (right is Number) compare(pc, left, right as Number?) else if (right is String) compare(pc, left, right as String?) else if (right is Boolean) compare(pc, left, if ((right as Boolean?).booleanValue()) BigDecimal.ONE else BigDecimal.ZERO) else if (right is Date) compare(pc, left, right as Date?) else if (right is Castable) {
            if (isComparableComponent(right as Castable?)) -compareComponent(pc, right as Castable?, left) else -(right as Castable?).compareTo(left.doubleValue())
            // compare(left ,((Castable)right).castToDoubleValue());
        } else if (right is Locale) compare(pc, Caster.toString(left), right as Locale?) else if (right == null) 1 else if (right is Enum) compare(pc, left, (right as Enum?).toString()) else if (right is Character) compare(pc, left, (right as Character?).toString()) else if (right is Calendar) compare(pc, left, (right as Calendar?).getTime()) else if (right is TimeZone) compare(pc, Caster.toString(left), right as TimeZone?) else error(true, false)
    }

    /**
     * compares a boolean with an Object
     *
     * @param left
     * @param right
     * @return difference as int
     * @throws PageException
     */
    @Throws(PageException::class)
    fun compare(pc: PageContext?, left: Boolean?, right: Object?): Int {
        return if (right is Boolean) compare(pc, left, right as Boolean?) else if (right is String) compare(pc, left, right as String?) else if (right is Number) compare(pc, left, right as Number?) else if (right is Date) compare(pc, if (left!!) BigDecimal.ONE else BigDecimal.ZERO, right as Date?) else if (right is Castable) {
            if (isComparableComponent(right as Castable?)) -compareComponent(pc, right as Castable?, left) else -(right as Castable?).compareTo(left)
            // compare(left ,((Castable)right).castToBooleanValue());
        } else if (right is Locale) compare(pc, Caster.toString(left), right as Locale?) else if (right == null) 1 else if (right is Enum) compare(pc, left, (right as Enum?).toString()) else if (right is Character) compare(pc, left, (right as Character?).toString()) else if (right is Calendar) compare(pc, if (left!!) BigDecimal.ONE else BigDecimal.ZERO, (right as Calendar?).getTime()) else if (right is TimeZone) compare(pc, Caster.toString(left), right as TimeZone?) else error(true, false)
    }

    /**
     * compares a Date with an Object
     *
     * @param left
     * @param right
     * @return difference as int
     * @throws PageException
     */
    @Throws(PageException::class)
    fun compare(pc: PageContext?, left: Date?, right: Object?): Int {
        return if (right is String) compare(pc, left, right as String?) else if (right is Number) compare(pc, left, right as Number?) else if (right is Boolean) compare(pc, left, if ((right as Boolean?).booleanValue()) BigDecimal.ONE else BigDecimal.ZERO) else if (right is Date) compare(pc, left, right as Date?) else if (right is Castable) {
            if (isComparableComponent(right as Castable?)) -compareComponent(pc, right as Castable?, left) else -(right as Castable?).compareTo(Caster.toDate(left, null))
            // compare(left ,(Date)((Castable)right).castToDateTime());
        } else if (right is Locale) compare(pc, Caster.toString(left), right as Locale?) else if (right == null) compare(pc, left, "") else if (right is Enum) compare(pc, left, (right as Enum?).toString()) else if (right is Character) compare(pc, left, (right as Character?).toString()) else if (right is Calendar) compare(pc, left, (right as Calendar?).getTime()) else if (right is TimeZone) compare(pc, Caster.toString(left), right as TimeZone?) else error(true, false)
    }

    @Throws(PageException::class)
    fun compare(pc: PageContext?, left: Castable?, right: Object?): Int {
        //
        if (isComparableComponent(left)) return compareComponent(pc, left, right)
        return if (right is String) left.compareTo(right as String?) else if (right is Number) left.compareTo((right as Number?).doubleValue()) else if (right is Boolean) left.compareTo(if ((right as Boolean?).booleanValue()) 1.0 else 0.0) else if (right is Date) left.compareTo(Caster.toDate(right)) else if (right is Castable) compare(pc, left.castToString(), (right as Castable?).castToString()) else if (right is Locale) compare(pc, left.castToString(), right) else if (right == null) compare(pc, left.castToString(), "") else if (right is Enum) left.compareTo((right as Enum?).toString()) else if (right is Character) left.compareTo((right as Character?).toString()) else if (right is Calendar) left.compareTo(DateTimeImpl((right as Calendar?).getTime())) else if (right is TimeZone) compare(pc, left.castToString(), right) else error(true, false)
    }

    @Throws(PageException::class)
    private fun compareComponent(pc: PageContext?, c: Castable?, o: Object?): Int {
        return Caster.toIntValue((c as Component?).call(pc, KeyConstants.__compare, arrayOf<Object?>(o)))
    }

    private fun isComparableComponent(c: Castable?): Boolean {
        if (c !is Component) return false
        val member: Member = (c as Component?).getMember(Component.ACCESS_PRIVATE, KeyConstants.__compare, false, false) as? UDF
                ?: return false
        val udf: UDF = member as UDF
        return if (udf.getReturnType() === CFTypes.TYPE_NUMERIC && udf.getFunctionArguments().length === 1) {
            true
        } else false
    }

    /**
     * compares a String with a String
     *
     * @param left
     * @param right
     * @return difference as int
     * @throws PageException
     */
    @Throws(PageException::class)
    fun compare(pc: PageContext?, left: String?, right: String?): Int {
        if (Decision.isNumber(left)) {
            return if (Decision.isNumber(right)) {
                compare(pc, Caster.toNumber(pc, left), Caster.toNumber(pc, right))
            } else compare(pc, Caster.toNumber(pc, left), right)
        }
        return if (Decision.isBoolean(left)) compare(pc, Caster.toBoolean(left), right) else left.compareToIgnoreCase(right)
        // NICE Date compare, perhaps datetime to double
    }

    /**
     * compares a String with a double
     *
     * @param left
     * @param right
     * @return difference as int
     * @throws PageException
     */
    @Throws(PageException::class)
    fun compare(pc: PageContext?, left: String?, right: Number?): Int {
        if (Decision.isNumber(left)) return compare(pc, Caster.toNumber(pc, left), right)
        if (Decision.isBoolean(left)) return compare(pc, Caster.toBoolean(left), right)
        if (left!!.length() === 0) return -1
        val leftFirst: Char = left.charAt(0)
        return if (leftFirst >= '0' && leftFirst <= '9') left.compareToIgnoreCase(Caster.toString(right)) else leftFirst - '0'
    }

    /**
     * compares a String with a boolean
     *
     * @param left
     * @param right
     * @return difference as int
     * @throws PageException
     */
    @Throws(PageException::class)
    fun compare(pc: PageContext?, left: String?, right: Boolean?): Int {
        if (Decision.isBoolean(left)) return compare(pc, Caster.toBoolean(left), right)
        if (Decision.isNumber(left)) return compare(pc, Caster.toNumber(pc, left), if (right!!) BigDecimal.ONE else BigDecimal.ZERO)
        if (left!!.length() === 0) return -1
        val leftFirst: Char = left.charAt(0)
        // print.ln(left+".compareTo("+Caster.toString(right)+")");
        // p(left);
        return if (leftFirst >= '0' && leftFirst <= '9') left.compareToIgnoreCase(Caster.toString(if (right!!) 1.0 else 0.0)) else leftFirst - '0'
    }

    /**
     * compares a double with a double
     *
     * @param left
     * @param right
     * @return difference as int
     */
    fun compare(pc: PageContext?, left: Number?, right: Number?): Int {
        if (AppListenerUtil.getPreciseMath(pc, null)) {
            return Caster.toBigDecimal(left).compareTo(Caster.toBigDecimal(right))
        }
        val l: Double = left.doubleValue()
        val r: Double = right.doubleValue()
        return if (l < r) -1 else if (l > r) 1 else 0
    }

    /**
     * compares a double with a boolean
     *
     * @param left
     * @param right
     * @return difference as int
     */
    fun compare(pc: PageContext?, left: Number?, right: Boolean?): Int {
        return compare(pc, left, if (right!!) BigDecimal.ONE else BigDecimal.ZERO)
    }

    /**
     * compares a double with a Date
     *
     * @param left
     * @param right
     * @return difference as int
     */
    fun compare(pc: PageContext?, left: Number?, right: Date?): Int {
        return compare(pc, DateTimeUtil.getInstance().toDateTime(left.doubleValue()) as Date, right)
    }

    /**
     * compares a boolean with a double
     *
     * @param left
     * @param right
     * @return difference as int
     */
    fun compare(pc: PageContext?, left: Boolean?, right: Number?): Int {
        return compare(pc, if (left!!) BigDecimal.ONE else BigDecimal.ZERO, right)
    }

    /**
     * compares a boolean with a boolean
     *
     * @param left
     * @param right
     * @return difference as int
     */
    fun compare(pc: PageContext?, left: Boolean?, right: Boolean?): Int {
        if (left!!) return if (right!!) 0 else 1
        return if (right!!) -1 else 0
    }

    /**
     * compares a boolean with a Date
     *
     * @param left
     * @param right
     * @return difference as int
     */
    fun compare(pc: PageContext?, left: Boolean?, right: Date?): Int {
        return compare(pc, if (left!!) BigDecimal.ONE else BigDecimal.ZERO, right)
    }

    /**
     * compares a Date with a String
     *
     * @param left
     * @param right
     * @return difference as int
     * @throws PageException
     */
    @Throws(PageException::class)
    fun compare(pc: PageContext?, left: Date?, right: String?): Int {
        if (Decision.isNumber(right)) return compare(pc, left, Caster.toNumber(pc, right))
        val dt: Date = DateCaster.toDateAdvanced(right, DateCaster.CONVERTING_TYPE_OFFSET, null, null)
        return if (dt != null) {
            compare(pc, left, dt)
        } else Caster.toString(left).compareToIgnoreCase(right)
    }

    /**
     * compares a Date with a boolean
     *
     * @param left
     * @param right
     * @return difference as int
     */
    fun compare(pc: PageContext?, left: Date?, right: Boolean?): Int {
        return compare(pc, left, if (right!!) BigDecimal.ONE else BigDecimal.ZERO)
    }

    /**
     * compares a Date with a Date
     *
     * @param left
     * @param right
     * @return difference as int
     */
    fun compare(pc: PageContext?, left: Date?, right: Date?): Int {
        val l: Long = left.getTime() / 1000L
        val r: Long = right.getTime() / 1000L
        return if (l < r) -1 else if (l > r) 1 else 0
    }

    @Throws(PageException::class)
    fun compare(pc: PageContext?, left: Object?, right: Boolean?): Int {
        return -compare(pc, right, left)
    }

    @Throws(PageException::class)
    fun compare(pc: PageContext?, left: Object?, right: Locale?): Int {
        return -compare(pc, right, left)
    }

    @Throws(PageException::class)
    fun compare(pc: PageContext?, left: Object?, right: TimeZone?): Int {
        return -compare(pc, right, left)
    }

    fun compare(pc: PageContext?, left: String?, right: Locale?): Int {
        return -compare(pc, right, left)
    }

    fun compare(pc: PageContext?, left: String?, right: TimeZone?): Int {
        return -compare(pc, right, left)
    }

    @Throws(PageException::class)
    fun compare(pc: PageContext?, left: Object?, right: Date?): Int {
        return -compare(pc, right, left)
    }

    @Throws(PageException::class)
    fun compare(pc: PageContext?, left: Object?, right: Castable?): Int {
        return -compare(pc, right, left)
    }

    @Throws(PageException::class)
    fun compare(pc: PageContext?, left: String?, right: Date?): Int {
        return -compare(pc, right, left)
    }

    @Throws(PageException::class)
    fun compare(pc: PageContext?, left: Number?, right: String?): Int {
        return -compare(pc, right, left)
    }

    fun compare(pc: PageContext?, left: Date?, right: Number?): Int {
        return -compare(pc, right, left)
    }

    @Throws(PageException::class)
    fun compare(pc: PageContext?, left: Boolean?, right: String?): Int {
        return -compare(pc, right, left)
    }

    @Throws(ExpressionException::class)
    private fun error(leftIsOk: Boolean, rightIsOk: Boolean): Int {
        // TODO remove this method
        throw ExpressionException("can't compare complex object types as simple value")
    }

    /**
     * Method to compare to different values, return true of objects are same otherwise false
     *
     * @param left left value to compare
     * @param right right value to compare
     * @param caseSensitive check case sensitive or not
     * @return is same or not
     * @throws PageException
     */
    @Throws(PageException::class)
    fun equals(pc: PageContext?, left: Object?, right: Object?, caseSensitive: Boolean): Boolean {
        return if (caseSensitive) {
            try {
                Caster.toString(left).equals(Caster.toString(right))
            } catch (e: ExpressionException) {
                compare(pc, left, right) == 0
            }
        } else compare(pc, left, right) == 0
    }

    fun equalsEL(pc: PageContext?, left: Object?, right: Object?, caseSensitive: Boolean, allowComplexValues: Boolean): Boolean {
        return if (!allowComplexValues || Decision.isSimpleValue(left) && Decision.isSimpleValue(right)) {
            try {
                equals(pc, left, right, caseSensitive)
            } catch (e: PageException) {
                false
            }
        } else equalsComplexEL(pc, left, right, caseSensitive, false)
    }

    fun equalsComplexEL(pc: PageContext?, left: Object?, right: Object?, caseSensitive: Boolean, checkOnlyPublicAppearance: Boolean): Boolean {
        return _equalsComplexEL(pc, null, left, right, caseSensitive, checkOnlyPublicAppearance)
    }

    fun _equalsComplexEL(pc: PageContext?, done: Set<Object?>?, left: Object?, right: Object?, caseSensitive: Boolean, checkOnlyPublicAppearance: Boolean): Boolean {
        var done: Set<Object?>? = done
        if (left === right) return true
        if (Decision.isSimpleValue(left) && Decision.isSimpleValue(right)) {
            return try {
                equals(pc, left, right, caseSensitive)
            } catch (e: PageException) {
                false
            }
        }
        if (left == null) return right == null
        if (done == null) done = HashSet<Object?>() else if (done.contains(left) && done.contains(right)) return true
        done.add(left)
        done.add(right)
        if (left is Component && right is Component) return __equalsComplexEL(pc, done, left as Component?, right as Component?, caseSensitive, checkOnlyPublicAppearance)
        if (left is UDF && right is UDF) return __equalsComplexEL(pc, done, left as UDF?, right as UDF?, caseSensitive, checkOnlyPublicAppearance)
        if (left is Collection && right is Collection) return __equalsComplexEL(pc, done, left as Collection?, right as Collection?, caseSensitive, checkOnlyPublicAppearance)
        if (left is List && right is List) return __equalsComplexEL(pc, done, ListAsArray.toArray(left as List?), ListAsArray.toArray(right as List?), caseSensitive, checkOnlyPublicAppearance)
        return if (left is Map && right is Map) __equalsComplexEL(pc, done, MapAsStruct.toStruct(left as Map?, true), MapAsStruct.toStruct(right as Map?, true), caseSensitive, checkOnlyPublicAppearance) else left.equals(right)
    }

    private fun __equalsComplexEL(pc: PageContext?, done: Set<Object?>?, left: UDF?, right: UDF?, caseSensitive: Boolean, checkOnlyPublicAppearance: Boolean): Boolean {
        return if (left == null || right == null) {
            if (left === right) true else false
        } else left.equals(right)
    }

    private fun __equalsComplexEL(pc: PageContext?, done: Set<Object?>?, left: Component?, right: Component?, caseSensitive: Boolean, checkOnlyPublicAppearance: Boolean): Boolean {
        if (left == null || right == null) {
            return if (left === right) true else false
        }
        if (!left.getPageSource().equals(right.getPageSource())) return false
        if (!checkOnlyPublicAppearance && !__equalsComplexEL(pc, done, left.getComponentScope(), right.getComponentScope(), caseSensitive, checkOnlyPublicAppearance)) return false
        return if (!__equalsComplexEL(pc, done, left as Collection?, right as Collection?, caseSensitive, checkOnlyPublicAppearance)) false else true
    }

    private fun __equalsComplexEL(pc: PageContext?, done: Set<Object?>?, left: Collection?, right: Collection?, caseSensitive: Boolean, checkOnlyPublicAppearance: Boolean): Boolean {
        if (left.size() !== right.size()) return false
        val it: Iterator<Key?> = left.keyIterator()
        var k: Key?
        var l: Object
        var r: Object
        while (it.hasNext()) {
            k = it.next()
            l = left.get(k, CollectionUtil.NULL)
            r = right.get(k, CollectionUtil.NULL)
            if (l === CollectionUtil.NULL || r === CollectionUtil.NULL) {
                if (l === r) continue
                return false
            }
            if (!_equalsComplexEL(pc, done, r, l, caseSensitive, checkOnlyPublicAppearance)) {
                return false
            }
        }
        return true
    }

    @Throws(PageException::class)
    fun equals(pc: PageContext?, left: Object?, right: Object?, caseSensitive: Boolean, allowComplexValues: Boolean): Boolean {
        return if (!allowComplexValues || Decision.isSimpleValue(left) && Decision.isSimpleValue(right)) equals(pc, left, right, caseSensitive) else equalsComplex(pc, left, right, caseSensitive, false)
    }

    @Throws(PageException::class)
    fun equalsComplex(pc: PageContext?, left: Object?, right: Object?, caseSensitive: Boolean, checkOnlyPublicAppearance: Boolean): Boolean {
        if (checkOnlyPublicAppearance) throw IllegalArgumentException("checkOnlyPublicAppearance cannot be true") // MUST implement
        return _equalsComplex(pc, null, left, right, caseSensitive)
    }

    @Throws(PageException::class)
    fun _equalsComplex(pc: PageContext?, done: Set<Object?>?, left: Object?, right: Object?, caseSensitive: Boolean): Boolean {
        var done: Set<Object?>? = done
        if (Decision.isSimpleValue(left) && Decision.isSimpleValue(right)) {
            return equals(pc, left, right, caseSensitive)
        }
        if (left == null) return right == null
        if (done == null) done = HashSet<Object?>() else if (done.contains(left) && done.contains(right)) return true
        done.add(left)
        done.add(right)
        if (left is Collection && right is Collection) return __equalsComplex(pc, done, left as Collection?, right as Collection?, caseSensitive)
        if (left is List && right is List) return __equalsComplex(pc, done, ListAsArray.toArray(left as List?), ListAsArray.toArray(right as List?), caseSensitive)
        return if (left is Map && right is Map) __equalsComplex(pc, done, MapAsStruct.toStruct(left as Map?, true), MapAsStruct.toStruct(right as Map?, true), caseSensitive) else left.equals(right)
    }

    @Throws(PageException::class)
    private fun __equalsComplex(pc: PageContext?, done: Set<Object?>?, left: Collection?, right: Collection?, caseSensitive: Boolean): Boolean {
        if (left.size() !== right.size()) return false
        val it: Iterator<Key?> = left.keyIterator()
        var k: Key?
        var l: Object
        var r: Object
        while (it.hasNext()) {
            k = it.next()
            r = right.get(k, CollectionUtil.NULL)
            if (r === CollectionUtil.NULL) return false
            l = left.get(k, CollectionUtil.NULL)
            if (!_equalsComplex(pc, done, r, l, caseSensitive)) return false
        }
        return true
    }

    /**
     * check if left is inside right (String-> ignore case)
     *
     * @param left string to check
     * @param right substring to find in string
     * @return return if substring has been found
     * @throws PageException
     */
    @Throws(PageException::class)
    fun ct(pc: PageContext?, left: Object?, right: Object?): Boolean {
        return Caster.toString(left).toLowerCase().indexOf(Caster.toString(right).toLowerCase()) !== -1
    }

    /**
     * Equivalence: Return True if both operands are True or both are False. The EQV operator is the
     * opposite of the XOR operator. For example, True EQV True is True, but True EQV False is False.
     *
     * @param left value to check
     * @param right value to check
     * @return result of operation
     * @throws PageException
     */
    @Throws(PageException::class)
    fun eqv(pc: PageContext?, left: Object?, right: Object?): Boolean {
        return eqv(pc, Caster.toBooleanValue(left), Caster.toBooleanValue(right))
    }

    /**
     * Equivalence: Return True if both operands are True or both are False. The EQV operator is the
     * opposite of the XOR operator. For example, True EQV True is True, but True EQV False is False.
     *
     * @param left value to check
     * @param right value to check
     * @return result of operation
     */
    fun eqv(pc: PageContext?, left: Boolean?, right: Boolean?): Boolean {
        return left == true && right == true || left == false && right == false
    }

    /**
     * Implication: The statement A IMP B is the equivalent of the logical statement "If A Then B." A
     * IMP B is False only if A is True and B is False. It is True in all other cases.
     *
     * @param left value to check
     * @param right value to check
     * @return result
     * @throws PageException
     */
    @Throws(PageException::class)
    fun imp(pc: PageContext?, left: Object?, right: Object?): Boolean {
        return imp(pc, Caster.toBoolean(left), Caster.toBoolean(right))
    }

    /**
     * Implication: The statement A IMP B is the equivalent of the logical statement "If A Then B." A
     * IMP B is False only if A is True and B is False. It is True in all other cases.
     *
     * @param left value to check
     * @param right value to check
     * @return result
     */
    fun imp(pc: PageContext?, left: Boolean?, right: Boolean?): Boolean {
        return !(left == true && right == false)
    }

    /**
     * check if left is not inside right (String-> ignore case)
     *
     * @param left string to check
     * @param right substring to find in string
     * @return return if substring NOT has been found
     * @throws PageException
     */
    @Throws(PageException::class)
    fun nct(pc: PageContext?, left: Object?, right: Object?): Boolean {
        return !ct(pc, left, right)
    }

    /**
     * simple reference compersion
     *
     * @param left
     * @param right
     * @return
     * @throws PageException
     */
    @Throws(PageException::class)
    fun eeq(pc: PageContext?, left: Object?, right: Object?): Boolean {
        return left === right
    }

    /**
     * simple reference compersion
     *
     * @param left
     * @param right
     * @return
     * @throws PageException
     */
    @Throws(PageException::class)
    fun neeq(pc: PageContext?, left: Object?, right: Object?): Boolean {
        return left !== right
    }

    /**
     * calculate the exponent of the left value
     *
     * @param left value to get exponent from
     * @param right exponent count
     * @return return expoinended value
     * @throws PageException
     */
    @Throws(PageException::class)
    fun exponent(pc: PageContext?, left: Object?, right: Object?): Double {
        return if (AppListenerUtil.getPreciseMath(pc, null)) {
            Caster.toBigDecimal(left).pow(Caster.toIntValue(right)).doubleValue()
        } else StrictMath.pow(Caster.toDoubleValue(left), Caster.toDoubleValue(right))
    }

    @Throws(PageException::class)
    fun exponent(pc: PageContext?, left: Number?, right: Number?): Double {
        return if (AppListenerUtil.getPreciseMath(pc, null)) {
            Caster.toBigDecimal(left).pow(right.intValue()).doubleValue()
        } else StrictMath.pow(left.doubleValue(), right.doubleValue())
    }

    @Throws(PageException::class)
    fun intdiv(pc: PageContext?, left: Object?, right: Object?): Double {
        return Double.valueOf(Caster.toIntValue(left) / Caster.toIntValue(right))
    }

    fun intdiv(pc: PageContext?, left: Number?, right: Number?): Double {
        return Double.valueOf(left.intValue() / right.intValue())
    }

    fun exponent(pc: PageContext?, left: Float, right: Float): Float {
        return StrictMath.pow(left, right)
    }

    /**
     * concat 2 CharSequences
     *
     * @param left
     * @param right
     * @return concated String
     */
    fun concat(pc: PageContext?, left: CharSequence?, right: CharSequence?): CharSequence? {
        if (left is Appendable) {
            try {
                (left as Appendable?).append(right)
                return left
            } catch (e: IOException) {
            }
        }
        return StringBuilder(left).append(right)
    }

    @Throws(PageException::class)
    fun plus(pc: PageContext?, left: Object?, right: Object?): Double {
        return if (AppListenerUtil.getPreciseMath(pc, null)) {
            Caster.toBigDecimal(left).add(Caster.toBigDecimal(right)).doubleValue()
        } else Caster.toDoubleValue(left) + Caster.toDoubleValue(right)
    }

    fun plus(pc: PageContext?, left: Number?, right: Number?): Double {
        return if (AppListenerUtil.getPreciseMath(pc, null)) {
            Caster.toBigDecimal(left).add(Caster.toBigDecimal(right)).doubleValue()
        } else left.doubleValue() + right.doubleValue()
    }

    /**
     * minus operation
     *
     * @param left
     * @param right
     * @return result of the opertions
     */
    fun minus(pc: PageContext?, left: Number?, right: Number?): Double {
        return if (AppListenerUtil.getPreciseMath(pc, null)) {
            Caster.toBigDecimal(left).subtract(Caster.toBigDecimal(right)).doubleValue()
        } else left.doubleValue() - right.doubleValue()
    }

    @Throws(PageException::class)
    fun minus(pc: PageContext?, left: Object?, right: Object?): Double {
        return if (AppListenerUtil.getPreciseMath(pc, null)) {
            Caster.toBigDecimal(left).subtract(Caster.toBigDecimal(right)).doubleValue()
        } else Caster.toDoubleValue(left) - Caster.toDoubleValue(right)
    }

    @Throws(PageException::class)
    fun modulus(pc: PageContext?, left: Object?, right: Object?): Double {
        val r: Double = Caster.toDoubleValue(right)
        if (r == 0.0) throw ArithmeticException("Division by zero is not possible")
        return Caster.toDoubleValue(left) % r
    }

    fun modulus(pc: PageContext?, left: Number?, right: Number?): Double {
        val r: Double = right.doubleValue()
        if (r == 0.0) throw ArithmeticException("Division by zero is not possible")
        return left.doubleValue() % r
    }

    /**
     * divide operation
     *
     * @param left
     * @param right
     * @return result of the opertions
     * @throws PageException
     */
    @Throws(PageException::class)
    fun divide(pc: PageContext?, left: Object?, right: Object?): Double {
        return if (AppListenerUtil.getPreciseMath(pc, null)) {
            MathUtil.divide(Caster.toBigDecimal(left), Caster.toBigDecimal(right)).doubleValue()
        } else Double.valueOf(Caster.toDoubleValue(left) / Caster.toDoubleValue(right))
    }

    fun divide(pc: PageContext?, left: Number?, right: Number?): Double {
        return if (AppListenerUtil.getPreciseMath(pc, null)) {
            MathUtil.divide(Caster.toBigDecimal(left), Caster.toBigDecimal(right)).doubleValue()
        } else Double.valueOf(left.doubleValue() / right.doubleValue())
    }

    @Throws(PageException::class)
    fun multiply(pc: PageContext?, left: Object?, right: Object?): Double {
        return if (AppListenerUtil.getPreciseMath(pc, null)) {
            Caster.toBigDecimal(left).multiply(Caster.toBigDecimal(right)).doubleValue()
        } else Caster.toDoubleValue(left) * Caster.toDoubleValue(right)
    }

    fun multiply(pc: PageContext?, left: Number?, right: Number?): Double {
        return if (AppListenerUtil.getPreciseMath(pc, null)) {
            Caster.toBigDecimal(left).multiply(Caster.toBigDecimal(right)).doubleValue()
        } else left.doubleValue() * right.doubleValue()
    }

    /**
     * bitand operation
     *
     * @param left
     * @param right
     * @return result of the opertions
     */
    fun bitand(pc: PageContext?, left: Number?, right: Number?): Number? {
        return left.intValue() and right.intValue()
    }

    /**
     * bitand operation
     *
     * @param left
     * @param right
     * @return result of the opertions
     */
    fun bitor(pc: PageContext?, left: Number?, right: Number?): Number? {
        return left.intValue() or right.intValue()
    }

    @Throws(PageException::class)
    fun divideRef(pc: PageContext?, left: Number?, right: Number?): Number? {
        if (AppListenerUtil.getPreciseMath(pc, null)) {
            val bd: BigDecimal = Caster.toBigDecimal(right)
            if (bd.equals(BigDecimal.ZERO)) throw ArithmeticException("Division by zero is not possible")
            return MathUtil.divide(Caster.toBigDecimal(left), bd)
        }
        val r: Double = Caster.toDoubleValue(right)
        if (r == 0.0) throw ArithmeticException("Division by zero is not possible")
        return Caster.toDouble(Caster.toDoubleValue(left) / r)
    }

    @Throws(PageException::class)
    fun divideRef(pc: PageContext?, left: Object?, right: Object?): Number? {
        if (AppListenerUtil.getPreciseMath(pc, null)) {
            val bd: BigDecimal = Caster.toBigDecimal(right)
            if (bd.equals(BigDecimal.ZERO)) throw ArithmeticException("Division by zero is not possible")
            return MathUtil.divide(Caster.toBigDecimal(left), bd)
        }
        val r: Double = Caster.toDoubleValue(right)
        if (r == 0.0) throw ArithmeticException("Division by zero is not possible")
        return Caster.toDouble(Caster.toDoubleValue(left) / r)
    }

    @Throws(PageException::class)
    fun intdivRef(pc: PageContext?, left: Object?, right: Object?): Number? {
        return Caster.toDouble(Caster.toIntValue(left) / Caster.toIntValue(right))
    }

    @Throws(PageException::class)
    fun intdivRef(pc: PageContext?, left: Number?, right: Number?): Number? {
        return Caster.toDouble(Caster.toIntValue(left) / Caster.toIntValue(right))
    }

    @Throws(PageException::class)
    fun exponentRef(pc: PageContext?, left: Object?, right: Object?): Number? {
        return if (AppListenerUtil.getPreciseMath(pc, null)) {
            Caster.toBigDecimal(left).pow(Caster.toIntValue(right))
        } else Caster.toDouble(StrictMath.pow(Caster.toDoubleValue(left), Caster.toDoubleValue(right)))
    }

    @Throws(PageException::class)
    fun plusRef(pc: PageContext?, left: Object?, right: Object?): Number? {
        return if (AppListenerUtil.getPreciseMath(pc, null)) {
            Caster.toBigDecimal(left).add(Caster.toBigDecimal(right))
        } else Caster.toDouble(Caster.toDoubleValue(left) + Caster.toDoubleValue(right))
    }

    @Throws(PageException::class)
    fun plusRef(pc: PageContext?, left: Number?, right: Number?): Number? {
        return if (AppListenerUtil.getPreciseMath(pc, null)) {
            Caster.toBigDecimal(left).add(Caster.toBigDecimal(right))
        } else Caster.toDouble(Caster.toDoubleValue(left) + Caster.toDoubleValue(right))
    }

    @Throws(PageException::class)
    fun minusRef(pc: PageContext?, left: Object?, right: Object?): Number? {
        return if (AppListenerUtil.getPreciseMath(pc, null)) {
            Caster.toBigDecimal(left).subtract(Caster.toBigDecimal(right))
        } else Caster.toDouble(Caster.toDoubleValue(left) - Caster.toDoubleValue(right))
    }

    @Throws(PageException::class)
    fun minusRef(pc: PageContext?, left: Number?, right: Number?): Number? {
        return if (AppListenerUtil.getPreciseMath(pc, null)) {
            Caster.toBigDecimal(left).subtract(Caster.toBigDecimal(right))
        } else Caster.toDouble(Caster.toDoubleValue(left) - Caster.toDoubleValue(right))
    }

    @Throws(PageException::class)
    fun modulusRef(pc: PageContext?, left: Object?, right: Object?): Number? {
        val rightAsDouble: Double = Caster.toDoubleValue(right)
        if (rightAsDouble == 0.0) throw ArithmeticException("Division by zero is not possible")
        return Caster.toDouble(Caster.toDoubleValue(left) % rightAsDouble)
    }

    @Throws(PageException::class)
    fun modulusRef(pc: PageContext?, left: Number?, right: Number?): Number? {
        val rightAsDouble: Double = Caster.toDoubleValue(right)
        if (rightAsDouble == 0.0) throw ArithmeticException("Division by zero is not possible")
        return Caster.toDouble(Caster.toDoubleValue(left) % rightAsDouble)
    }

    @Throws(PageException::class)
    fun multiplyRef(pc: PageContext?, left: Object?, right: Object?): Number? {
        return if (AppListenerUtil.getPreciseMath(pc, null)) {
            Caster.toBigDecimal(left).multiply(Caster.toBigDecimal(right))
        } else Caster.toDouble(Caster.toDoubleValue(left) * Caster.toDoubleValue(right))
    }

    @Throws(PageException::class)
    fun multiplyRef(pc: PageContext?, left: Number?, right: Number?): Number? {
        return if (AppListenerUtil.getPreciseMath(pc, null)) {
            Caster.toBigDecimal(left).multiply(Caster.toBigDecimal(right))
        } else Caster.toDouble(left.doubleValue() * right.doubleValue())
    }

    // post plus
    @Throws(PageException::class)
    fun unaryPostPlus(pc: PageContext?, keys: Array<Collection.Key?>?, value: Number?): Number? {
        val ref: VariableReference = VariableInterpreter.getVariableReference(pc, keys, true)
        val rtn: Number = Caster.toNumber(pc, ref.get(pc))
        ref.set(plusRef(pc, rtn, value))
        return rtn
    }

    @Throws(PageException::class)
    fun unaryPostPlus(pc: PageContext?, coll: Collection?, key: Collection.Key?, value: Number?): Number? {
        val rtn: Number = Caster.toNumber(pc, coll.get(key))
        coll.set(key, plusRef(pc, rtn, value))
        return rtn
    }

    @Throws(PageException::class)
    fun unaryPoPl(pc: PageContext?, keys: Array<Collection.Key?>?, value: Number?): Number? {
        val ref: VariableReference = VariableInterpreter.getVariableReference(pc, keys, true)
        val rtn: Number = Caster.toNumber(pc, ref.get(pc))
        ref.set(plusRef(pc, rtn, value))
        return rtn
    }

    @Throws(PageException::class)
    fun unaryPoPl(pc: PageContext?, key: Collection.Key?, value: Number?): Number? {
        return unaryPoPl(pc, pc.undefinedScope(), key, value)
    }

    @Throws(PageException::class)
    fun unaryPoPl(pc: PageContext?, coll: Collection?, key: Collection.Key?, value: Number?): Number? {
        val rtn: Number = Caster.toNumber(pc, coll.get(key))
        coll.set(key, plusRef(pc, rtn, value))
        return rtn
    }

    // post minus
    @Throws(PageException::class)
    fun unaryPostMinus(pc: PageContext?, keys: Array<Collection.Key?>?, value: Number?): Number? {
        val ref: VariableReference = VariableInterpreter.getVariableReference(pc, keys, true)
        val rtn: Number = Caster.toNumber(pc, ref.get(pc))
        ref.set(minusRef(pc, rtn, value))
        return rtn
    }

    @Throws(PageException::class)
    fun unaryPostMinus(pc: PageContext?, coll: Collection?, key: Collection.Key?, value: Number?): Number? {
        val rtn: Number = Caster.toNumber(pc, coll.get(key))
        coll.set(key, minusRef(pc, rtn, value))
        return rtn
    }

    @Throws(PageException::class)
    fun unaryPoMi(pc: PageContext?, keys: Array<Collection.Key?>?, value: Number?): Number? {
        val ref: VariableReference = VariableInterpreter.getVariableReference(pc, keys, true)
        val rtn: Number = Caster.toNumber(pc, ref.get(pc))
        ref.set(minusRef(pc, rtn, value))
        return rtn
    }

    @Throws(PageException::class)
    fun unaryPoMi(pc: PageContext?, key: Collection.Key?, value: Number?): Number? {
        return unaryPoMi(pc, pc.undefinedScope(), key, value)
    }

    @Throws(PageException::class)
    fun unaryPoMi(pc: PageContext?, coll: Collection?, key: Collection.Key?, value: Number?): Number? {
        val rtn: Number = Caster.toNumber(pc, coll.get(key))
        coll.set(key, minusRef(pc, rtn, value))
        return rtn
    }

    // pre plus
    @Throws(PageException::class)
    fun unaryPrePlus(pc: PageContext?, keys: Array<Collection.Key?>?, value: Number?): Number? {
        val ref: VariableReference = VariableInterpreter.getVariableReference(pc, keys, true)
        val rtn: Number = plusRef(pc, Caster.toNumber(pc, ref.get(pc)), value)
        ref.set(rtn)
        return rtn
    }

    @Throws(PageException::class)
    fun unaryPrePlus(pc: PageContext?, coll: Collection?, key: Collection.Key?, value: Number?): Number? {
        val rtn: Number = plusRef(pc, Caster.toNumber(pc, coll.get(key)), value)
        coll.set(key, rtn)
        return rtn
    }

    @Throws(PageException::class)
    fun unaryPrPl(pc: PageContext?, keys: Array<Collection.Key?>?, value: Number?): Number? {
        val ref: VariableReference = VariableInterpreter.getVariableReference(pc, keys, true)
        val rtn: Number = plusRef(pc, Caster.toNumber(pc, ref.get(pc)), value)
        ref.set(rtn)
        return rtn
    }

    @Throws(PageException::class)
    fun unaryPrPl(pc: PageContext?, key: Collection.Key?, value: Number?): Number? {
        return unaryPrPl(pc, pc.undefinedScope(), key, value)
    }

    @Throws(PageException::class)
    fun unaryPrPl(pc: PageContext?, coll: Collection?, key: Collection.Key?, value: Number?): Number? {
        val rtn: Number = plusRef(pc, Caster.toNumber(pc, coll.get(key)), value)
        coll.set(key, rtn)
        return rtn
    }

    // pre minus
    @Throws(PageException::class)
    fun unaryPreMinus(pc: PageContext?, keys: Array<Collection.Key?>?, value: Number?): Number? {
        val ref: VariableReference = VariableInterpreter.getVariableReference(pc, keys, true)
        val rtn: Number = minusRef(pc, Caster.toNumber(pc, ref.get(pc)), value)
        ref.set(rtn)
        return rtn
    }

    @Throws(PageException::class)
    fun unaryPreMinus(pc: PageContext?, coll: Collection?, key: Collection.Key?, value: Number?): Number? {
        val rtn: Number = minusRef(pc, Caster.toNumber(pc, coll.get(key)), value)
        coll.set(key, rtn)
        return rtn
    }

    @Throws(PageException::class)
    fun unaryPrMi(pc: PageContext?, keys: Array<Collection.Key?>?, value: Number?): Number? {
        val ref: VariableReference = VariableInterpreter.getVariableReference(pc, keys, true)
        val rtn: Number = minusRef(pc, Caster.toNumber(pc, ref.get(pc)), value)
        ref.set(rtn)
        return rtn
    }

    @Throws(PageException::class)
    fun unaryPrMi(pc: PageContext?, key: Collection.Key?, value: Number?): Number? {
        return unaryPrMi(pc, pc.undefinedScope(), key, value)
    }

    @Throws(PageException::class)
    fun unaryPrMi(pc: PageContext?, coll: Collection?, key: Collection.Key?, value: Number?): Number? {
        val rtn: Number = minusRef(pc, Caster.toNumber(pc, coll.get(key)), value)
        coll.set(key, rtn)
        return rtn
    }

    // pre multiply
    @Throws(PageException::class)
    fun unaryPreMultiply(pc: PageContext?, keys: Array<Collection.Key?>?, value: Number?): Number? {
        val ref: VariableReference = VariableInterpreter.getVariableReference(pc, keys, true)
        val rtn: Number = multiplyRef(pc, Caster.toNumber(pc, ref.get(pc)), value)
        ref.set(rtn)
        return rtn
    }

    @Throws(PageException::class)
    fun unaryPreMultiply(pc: PageContext?, coll: Collection?, key: Collection.Key?, value: Number?): Number? {
        val rtn: Number = multiplyRef(pc, Caster.toNumber(pc, coll.get(key)), value)
        coll.set(key, rtn)
        return rtn
    }

    @Throws(PageException::class)
    fun unaryPrMu(pc: PageContext?, keys: Array<Collection.Key?>?, value: Number?): Number? {
        val ref: VariableReference = VariableInterpreter.getVariableReference(pc, keys, true)
        val rtn: Number = multiplyRef(pc, Caster.toNumber(pc, ref.get(pc)), value)
        ref.set(rtn)
        return rtn
    }

    @Throws(PageException::class)
    fun unaryPrMu(pc: PageContext?, key: Collection.Key?, value: Number?): Number? {
        return unaryPrMu(pc, pc.undefinedScope(), key, value)
    }

    @Throws(PageException::class)
    fun unaryPrMu(pc: PageContext?, coll: Collection?, key: Collection.Key?, value: Number?): Number? {
        val rtn: Number = multiplyRef(pc, Caster.toNumber(pc, coll.get(key)), value)
        coll.set(key, rtn)
        return rtn
    }

    // pre divide
    @Throws(PageException::class)
    fun unaryPreDivide(pc: PageContext?, keys: Array<Collection.Key?>?, value: Number?): Number? {
        val ref: VariableReference = VariableInterpreter.getVariableReference(pc, keys, true)
        val rtn: Number = divideRef(pc, Caster.toNumber(pc, ref.get(pc)), value)
        ref.set(rtn)
        return rtn
    }

    @Throws(PageException::class)
    fun unaryPreDivide(pc: PageContext?, coll: Collection?, key: Collection.Key?, value: Number?): Number? {
        val rtn: Number = divideRef(pc, Caster.toNumber(pc, coll.get(key)), value)
        coll.set(key, rtn)
        return rtn
    }

    @Throws(PageException::class)
    fun unaryPrDi(pc: PageContext?, keys: Array<Collection.Key?>?, value: Number?): Number? {
        val ref: VariableReference = VariableInterpreter.getVariableReference(pc, keys, true)
        val rtn: Number = divideRef(pc, Caster.toNumber(pc, ref.get(pc)), value)
        ref.set(rtn)
        return rtn
    }

    @Throws(PageException::class)
    fun unaryPrDi(pc: PageContext?, key: Collection.Key?, value: Number?): Number? {
        return unaryPrDi(pc, pc.undefinedScope(), key, value)
    }

    @Throws(PageException::class)
    fun unaryPrDi(pc: PageContext?, coll: Collection?, key: Collection.Key?, value: Number?): Number? {
        val rtn: Number = divideRef(pc, Caster.toNumber(pc, coll.get(key)), value)
        coll.set(key, rtn)
        return rtn
    }

    // Concat
    @Throws(PageException::class)
    fun unaryPreConcat(pc: PageContext?, keys: Array<Collection.Key?>?, value: String?): String? {
        val ref: VariableReference = VariableInterpreter.getVariableReference(pc, keys, true)
        val rtn: String = Caster.toString(ref.get(pc)).concat(value)
        ref.set(pc, rtn)
        return rtn
    }

    @Throws(PageException::class)
    fun unaryPreConcat(pc: PageContext?, coll: Collection?, key: Collection.Key?, value: String?): String? {
        val rtn: String = Caster.toString(coll.get(key)).concat(value)
        coll.set(key, rtn)
        return rtn
    }

    @Throws(PageException::class)
    fun unaryPreConcat(pc: PageContext?, key: Collection.Key?, value: String?): String? {
        val ref: VariableReference = VariableInterpreter.getVariableReference(pc, key, true)
        val rtn: String = Caster.toString(ref.get(pc)).concat(value)
        ref.set(pc, rtn)
        return rtn
    }
}