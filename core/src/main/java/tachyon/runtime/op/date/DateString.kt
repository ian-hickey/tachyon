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
package tachyon.runtime.op.date

import java.util.HashMap

/**
 * helper class to convert a string to an Object
 */
class DateString
/**
 * constructor of the class
 *
 * @param str Date String
 */(private val str: String) {
    var pos = 0
        private set

    companion object {
        private val ints = arrayOf(intArrayOf(0, 1, 2, 3, 4, 5, 6, 7, 8, 9), intArrayOf(0, 10, 20, 30, 40, 50, 60, 70, 80, 90), intArrayOf(0, 100, 200, 300, 400, 500, 600, 700, 800, 900), intArrayOf(0, 1000, 2000, 3000, 4000, 5000, 6000, 7000, 8000, 9000))
        private val months: Map = HashMap()

        init {
            months.put("january", Constants.INTEGER_1)
            months.put("januar", Constants.INTEGER_1)
            months.put("janua", Constants.INTEGER_1)
            months.put("janu", Constants.INTEGER_1)
            months.put("jan", Constants.INTEGER_1)
            months.put("february", Constants.INTEGER_2)
            months.put("februar", Constants.INTEGER_2)
            months.put("februa", Constants.INTEGER_2)
            months.put("febru", Constants.INTEGER_2)
            months.put("febr", Constants.INTEGER_2)
            months.put("feb", Constants.INTEGER_2)
            months.put("march", Constants.INTEGER_3)
            months.put("marc", Constants.INTEGER_3)
            months.put("mar", Constants.INTEGER_3)
            months.put("april", Constants.INTEGER_4)
            months.put("apri", Constants.INTEGER_4)
            months.put("apr", Constants.INTEGER_4)
            months.put("may", Constants.INTEGER_5)
            months.put("june", Constants.INTEGER_6)
            months.put("jun", Constants.INTEGER_6)
            months.put("july", Constants.INTEGER_7)
            months.put("jul", Constants.INTEGER_7)
            months.put("august", Constants.INTEGER_8)
            months.put("augus", Constants.INTEGER_8)
            months.put("augu", Constants.INTEGER_8)
            months.put("aug", Constants.INTEGER_8)
            months.put("september", Constants.INTEGER_9)
            months.put("septembe", Constants.INTEGER_9)
            months.put("septemb", Constants.INTEGER_9)
            months.put("septem", Constants.INTEGER_9)
            months.put("septe", Constants.INTEGER_9)
            months.put("sept", Constants.INTEGER_9)
            months.put("sep", Constants.INTEGER_9)
            months.put("october", Constants.INTEGER_10)
            months.put("octobe", Constants.INTEGER_10)
            months.put("octob", Constants.INTEGER_10)
            months.put("octo", Constants.INTEGER_10)
            months.put("oct", Constants.INTEGER_10)
            months.put("november", Constants.INTEGER_11)
            months.put("novembe", Constants.INTEGER_11)
            months.put("novemb", Constants.INTEGER_11)
            months.put("novem", Constants.INTEGER_11)
            months.put("nove", Constants.INTEGER_11)
            months.put("nov", Constants.INTEGER_11)
            months.put("december", Constants.INTEGER_12)
            months.put("decembe", Constants.INTEGER_12)
            months.put("decemb", Constants.INTEGER_12)
            months.put("decem", Constants.INTEGER_12)
            months.put("dece", Constants.INTEGER_12)
            months.put("dec", Constants.INTEGER_12)
        }
    }

    /**
     * check if char a current position of the inner cursor is same value like given value
     *
     * @param c char to compare
     * @return is same or not
     */
    fun isNext(c: Char): Boolean {
        return str.length() > pos + 1 && str.charAt(pos + 1) === c
    }

    /**
     * check if char a current position of the inner cursor is same value like given value
     *
     * @param c char to compare
     * @return is same or not
     */
    fun isCurrent(c: Char): Boolean {
        return str.length() > pos && str.charAt(pos) === c
    }

    /**
     * check if last char has same value than given char
     *
     * @param c char to check
     * @return is same or not
     */
    fun isLast(c: Char): Boolean {
        return str.charAt(str.length() - 1) === c
    }

    /**
     * set inner cursor one forward
     */
    operator fun next() {
        pos++
    }

    /**
     * set inner cursor [count] forward
     *
     * @param count forward count
     */
    fun next(count: Int) {
        pos += count
    }

    /**
     * @return the length of the inner String
     */
    fun length(): Int {
        return str.length()
    }

    /**
     * forward inner cursor if value at current position is same as given.
     *
     * @param c char to compare
     * @return has forwared or not
     */
    fun fwIfCurrent(c: Char): Boolean {
        if (isCurrent(c)) {
            pos++
            return true
        }
        return false
    }

    /**
     * forward inner cursor if value at the next position is same as given.
     *
     * @param c char to compare
     * @return has forwared or not
     */
    fun fwIfNext(c: Char): Boolean {
        if (isNext(c)) {
            pos++
            return true
        }
        return false
    }
    /*
	 * read in the next four digits from current position
	 * 
	 * @return value from the 4 digits
	 *
	 * public int read4Digit() { // first char c=str.charAt(pos++); if(!isDigit(c)) return -1; int
	 * value=ints[3][c-48];
	 * 
	 * // second c=str.charAt(pos++); if(!isDigit(c)) return -1; value+=ints[2][c-48];
	 * 
	 * // third c=str.charAt(pos++); if(!isDigit(c)) return -1; value+=ints[1][c-48];
	 * 
	 * // fourt c=str.charAt(pos++); if(!isDigit(c)) return -1; value+=ints[0][c-48];
	 * 
	 * return value; }
	 */
    /*
	 * read in the next four digits from current position
	 * 
	 * @return value from the 4 digits
	 *
	 * public int read2Digit() { // first char c=str.charAt(pos++); if(!isDigit(c)) return -1; int
	 * value=ints[1][c-48];
	 * 
	 * // second c=str.charAt(pos++); if(!isDigit(c)) return -1; value+=ints[0][c-48];
	 * 
	 * return value; }
	 */
    /**
     * read in the next digits from current position
     *
     * @return value from the digits
     */
    fun readDigits(): Int {
        var value = 0
        var c: Char
        if (isValidIndex && isDigit(str.charAt(pos).also { c = it })) {
            value = ints[0][c.toInt() - 48]
            pos++
        } else return -1
        while (isValidIndex && isDigit(str.charAt(pos).also { c = it })) {
            value *= 10
            value += ints[0][c.toInt() - 48]
            pos++
        }
        return value
    }

    fun removeWhitespace(): Boolean {
        val rtn = false
        while (isValidIndex && Character.isWhitespace(str.charAt(pos))) {
            pos++
        }
        return rtn
    }

    fun readMonthString(): Int {
        var c: Char
        val start = pos
        val sb = StringBuilder()
        while (isValidIndex && isMonthChar(str.charAt(pos).also { c = it })) {
            pos++
            sb.append(Character.toLowerCase(c))
        }
        val month: Integer = months.get(sb.toString().trim()) as Integer
        if (month != null) return month.intValue()
        pos = start
        return -1
    }

    private fun isMonthChar(c: Char): Boolean {
        return c >= 'a' && c <= 'z' || c >= 'A' && c <= 'Z'
    }

    /**
     * returns if c is a digit or not
     *
     * @param c char to check
     * @return is digit
     */
    fun isDigit(c: Char): Boolean {
        return c >= '0' && c <= '9'
    }

    /**
     * returns if value at cursor position is a digit or not
     *
     * @return is digit
     */
    val isDigit: Boolean
        get() = isValidIndex && isDigit(str.charAt(pos))

    /**
     * returns if last char is a digit or not
     *
     * @return is digit
     */
    val isLastDigit: Boolean
        get() = isDigit(str.charAt(str.length() - 1))

    /**
     * return char at given position
     *
     * @param pos postion to get value
     * @return character from given position
     */
    fun charAt(pos: Int): Char {
        return str.charAt(pos)
    }

    /**
     * returns if cursor is on the last position
     *
     * @return is on last
     */
    val isLast: Boolean
        get() = pos + 1 == str.length()

    /**
     * returns if cursor is after the last position
     *
     * @return is after the last
     */
    val isAfterLast: Boolean
        get() = pos >= str.length()

    /**
     * returns if cursor is on a valid position
     *
     * @return is after the last
     */
    val isValidIndex: Boolean
        get() = pos < str.length()

    fun current(): Char {
        return str.charAt(pos)
    }

    fun reset() {
        pos = 0
    }
}