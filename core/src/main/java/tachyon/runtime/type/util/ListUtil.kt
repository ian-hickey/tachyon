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
package tachyon.runtime.type.util

import java.util.ArrayList

/**
 * List is not a type, only some static method to manipulate String lists
 */
object ListUtil {
    /**
     * casts a list to Array object, the list can be have quoted (",') arguments and delimter in this
     * arguments are ignored. quotes are not removed example:
     * listWithQuotesToArray("aab,a'a,b',a\"a,b\"",",","\"'") will be translated to
     * ["aab","a'a,b'","a\"a,b\""]
     *
     * @param list list to cast
     * @param delimiter delimter of the list
     * @param quotes quotes of the list
     * @return Array Object
     */
    fun listWithQuotesToArray(list: String?, delimiter: String?, quotes: String?): Array? {
        if (list!!.length() === 0) return ArrayImpl()
        val len: Int = list!!.length()
        var last = 0
        val del: CharArray = delimiter.toCharArray()
        val quo: CharArray = quotes.toCharArray()
        var c: Char
        var inside = 0.toChar()
        val array = ArrayImpl()
        // try{
        for (i in 0 until len) {
            c = list.charAt(i)
            for (y in quo.indices) {
                if (c == quo[y]) {
                    if (c == inside) inside = 0.toChar() else if (inside.toInt() == 0) inside = c
                    continue
                }
            }
            for (y in del.indices) {
                if (inside.toInt() == 0 && c == del[y]) {
                    array.appendEL(list.substring(last, i))
                    last = i + 1
                    break
                }
            }
        }
        if (last <= len) array.appendEL(list.substring(last))
        // }catch(PageException e){}
        return array
    }

    /**
     * casts a list to Array object
     *
     * @param list list to cast
     * @param delimiter delimter of the list
     * @return Array Object
     */
    fun listToArray(list: String?, delimiter: String?): Array? {
        if (delimiter!!.length() === 1) return listToArray(list, delimiter.charAt(0))
        if (list!!.length() === 0) return ArrayImpl()
        if (delimiter!!.length() === 0) {
            val len: Int = list!!.length()
            val array = ArrayImpl()
            array.appendEL("") // ACF compatibility
            for (i in 0 until len) {
                array.appendEL(list.charAt(i))
            }
            array.appendEL("") // ACF compatibility
            return array
        }
        val len: Int = list!!.length()
        var last = 0
        val del: CharArray = delimiter.toCharArray()
        var c: Char
        val array = ArrayImpl()
        // try{
        for (i in 0 until len) {
            c = list.charAt(i)
            for (y in del.indices) {
                if (c == del[y]) {
                    array.appendEL(list.substring(last, i))
                    last = i + 1
                    break
                }
            }
        }
        if (last <= len) array.appendEL(list.substring(last))
        // }catch(ExpressionException e){}
        return array
    }

    fun listToArray(list: String?, delimiter: String?, includeEmptyFields: Boolean, multiCharDelim: Boolean): Array? {
        return if (includeEmptyFields) listToArray(list, delimiter, multiCharDelim) else listToArrayRemoveEmpty(list, delimiter, multiCharDelim)
    }

    private fun listToArray(list: String?, delimiter: String?, multiCharDelim: Boolean): Array? {
        if (!multiCharDelim || delimiter!!.length() === 0) return listToArray(list, delimiter)
        if (delimiter!!.length() === 1) return listToArray(list, delimiter.charAt(0))
        val len: Int = list!!.length()
        if (len == 0) return ArrayImpl()
        val array: Array = ArrayImpl()
        var from = 0
        var index: Int
        val dl: Int = delimiter!!.length()
        while (list.indexOf(delimiter, from).also { index = it } != -1) {
            array.appendEL(list.substring(from, index))
            from = index + dl
        }
        array.appendEL(list.substring(from, len))
        return array
    }

    /**
     * casts a list to Array object
     *
     * @param list list to cast
     * @param delimiter delimter of the list
     * @return Array Object
     */
    fun listToArray(list: String?, delimiter: Char): Array? {
        if (list!!.length() === 0) return ArrayImpl()
        val len: Int = list!!.length()
        var last = 0
        val array: Array = ArrayImpl()
        try {
            for (i in 0 until len) {
                if (list.charAt(i) === delimiter) {
                    array.append(list.substring(last, i))
                    last = i + 1
                }
            }
            if (last <= len) array.append(list.substring(last))
        } catch (e: PageException) {
        }
        return array
    }

    /**
     * casts a list to Array object remove Empty Elements
     *
     * @param list list to cast
     * @param delimiter delimter of the list
     * @return Array Object
     */
    private fun listToArrayRemoveEmpty(list: String?, delimiter: String?, multiCharDelim: Boolean): Array? {
        if (!multiCharDelim || delimiter!!.length() === 0) return listToArrayRemoveEmpty(list, delimiter)
        if (delimiter!!.length() === 1) return listToArrayRemoveEmpty(list, delimiter.charAt(0))
        val len: Int = list!!.length()
        if (len == 0) return ArrayImpl()
        val array: Array = ArrayImpl()
        var from = 0
        var index: Int
        val dl: Int = delimiter!!.length()
        while (list.indexOf(delimiter, from).also { index = it } != -1) {
            if (from < index) array.appendEL(list.substring(from, index))
            from = index + dl
        }
        if (from < len) array.appendEL(list.substring(from, len))
        return array
    }

    fun listToArrayRemoveEmpty(list: String?, delimiter: String?): Array? {
        if (delimiter!!.length() === 1) return listToArrayRemoveEmpty(list, delimiter.charAt(0))
        val len: Int = list!!.length()
        val array = ArrayImpl()
        if (len == 0) return array
        if (delimiter!!.length() === 0) {
            for (i in 0 until len) {
                array.appendEL(String.valueOf(list.charAt(i)))
            }
            return array
        }
        var last = 0
        val del: CharArray = delimiter.toCharArray()
        var c: Char
        for (i in 0 until len) {
            c = list.charAt(i)
            for (y in del.indices) {
                if (c == del[y]) {
                    if (last < i) array.appendEL(list.substring(last, i))
                    last = i + 1
                    break
                }
            }
        }
        if (last < len) array.appendEL(list.substring(last))
        return array
    }

    fun listRemoveEmpty(list: String?, delimiter: String?): String? {
        if (delimiter!!.length() === 1) return listRemoveEmpty(list, delimiter.charAt(0))
        if (delimiter!!.length() === 0) return list
        val len: Int = list!!.length()
        if (len == 0) return ""
        val sb = StringBuilder()
        var last = 0
        val del: CharArray = delimiter.toCharArray()
        var c: Char
        var first = true
        for (i in 0 until len) {
            c = list.charAt(i)
            for (y in del.indices) {
                if (c == del[y]) {
                    if (last < i) {
                        if (first) first = false else sb.append(delimiter)
                        sb.append(list.substring(last, i))
                    }
                    last = i + 1
                    break
                }
            }
        }
        if (last < len) {
            if (!first) sb.append(delimiter)
            sb.append(list.substring(last))
        }
        return sb.toString()
    }

    /**
     * casts a list to Array object remove Empty Elements
     *
     * @param list list to cast
     * @param delimiter delimter of the list
     * @return Array Object
     */
    fun listToArrayRemoveEmpty(list: String?, delimiter: Char): Array? {
        val len: Int = list!!.length()
        val array = ArrayImpl()
        if (len == 0) return array
        var last = 0
        for (i in 0 until len) {
            if (list.charAt(i) === delimiter) {
                if (last < i) array.appendEL(list.substring(last, i))
                last = i + 1
            }
        }
        if (last < len) array.appendEL(list.substring(last))
        return array
    }

    fun listRemoveEmpty(list: String?, delimiter: Char): String? {
        val len: Int = list!!.length()
        if (len == 0) return ""
        val sb = StringBuilder()
        var last = 0
        var first = true
        for (i in 0 until len) {
            if (list.charAt(i) === delimiter) {
                if (last < i) {
                    if (first) first = false else sb.append(delimiter)
                    sb.append(list.substring(last, i))
                }
                last = i + 1
            }
        }
        if (last < len) {
            if (!first) sb.append(delimiter)
            sb.append(list.substring(last))
        }
        return sb.toString()
    }

    fun toListRemoveEmpty(list: String?, delimiter: Char): List<String?>? {
        val len: Int = list!!.length()
        val array: List<String?> = ArrayList<String?>()
        if (len == 0) return array
        var last = 0
        for (i in 0 until len) {
            if (list.charAt(i) === delimiter) {
                if (last < i) array.add(list.substring(last, i))
                last = i + 1
            }
        }
        if (last < len) array.add(list.substring(last))
        return array
    }

    /**
     * casts a list to Array object remove Empty Elements
     *
     * @param list list to cast
     * @param delimiter delimter of the list
     * @return Array Object
     */
    fun listToStringListRemoveEmpty(list: String?, delimiter: Char): StringList? {
        val len: Int = list!!.length()
        val rtn = StringList()
        if (len == 0) return rtn.reset()
        var last = 0
        for (i in 0 until len) {
            if (list.charAt(i) === delimiter) {
                if (last < i) rtn.add(list.substring(last, i))
                last = i + 1
            }
        }
        if (last < len) rtn.add(list.substring(last))
        return rtn.reset()
    }

    /**
     * casts a list to Array object, remove all empty items at start and end of the list
     *
     * @param list list to cast
     * @param delimiter delimter of the list
     * @return Array Object
     */
    fun listToArrayTrim(list: String?, delimiter: String?): Array? {
        var list = list
        if (delimiter!!.length() === 1) return listToArrayTrim(list, delimiter.charAt(0))
        if (list!!.length() === 0) return ArrayImpl()
        val del: CharArray = delimiter.toCharArray()
        var c: Char

        // remove at start
        outer@ while (list!!.length() > 0) {
            c = list.charAt(0)
            for (i in del.indices) {
                if (c == del[i]) {
                    list = list.substring(1)
                    continue@outer
                }
            }
            break
        }
        var len: Int
        outer@ while (list!!.length() > 0) {
            c = list.charAt(list.length() - 1)
            for (i in del.indices) {
                if (c == del[i]) {
                    len = list!!.length()
                    list = list.substring(0, if (len - 1 < 0) 0 else len - 1)
                    continue@outer
                }
            }
            break
        }
        return listToArray(list, delimiter)
    }

    /**
     * casts a list to Array object, remove all empty items at start and end of the list and store count
     * to info
     *
     * @param list list to cast
     * @param delimiter delimter of the list
     * @param info
     * @return Array Object
     */
    fun listToArrayTrim(list: String?, delimiter: String?, info: IntArray?): Array? {
        var list = list
        if (delimiter!!.length() === 1) return listToArrayTrim(list, delimiter.charAt(0), info)
        if (list!!.length() === 0) return ArrayImpl()
        val del: CharArray = delimiter.toCharArray()
        var c: Char

        // remove at start
        outer@ while (list!!.length() > 0) {
            c = list.charAt(0)
            for (i in del.indices) {
                if (c == del[i]) {
                    info!![0]++
                    list = list.substring(1)
                    continue@outer
                }
            }
            break
        }
        var len: Int
        outer@ while (list!!.length() > 0) {
            c = list.charAt(list.length() - 1)
            for (i in del.indices) {
                if (c == del[i]) {
                    info!![1]++
                    len = list!!.length()
                    list = list.substring(0, if (len - 1 < 0) 0 else len - 1)
                    continue@outer
                }
            }
            break
        }
        return listToArray(list, delimiter)
    }

    /**
     * casts a list to int array with max range its used in CFIMAP/CFPOP tag with messageNumber attribute
     *
     * @param list list to cast
     * @param delimiter delimter of the list
     * @param maxRange maximum range that element can
     * @return int array
     */
    fun listToIntArrayWithMaxRange(list: String?, delimiter: Char, maxRange: Int): IntArray? {
        val len: Int = list!!.length()
        val array = ArrayImpl()
        if (len == 0) return IntArray(0)
        var last = 0
        var l: Int
        for (i in 0 until len) {
            if (list.charAt(i) === delimiter) {
                l = Caster.toIntValue(list.substring(last, i).trim(), 0)
                if (l > 0 && l <= maxRange) array.appendEL(l)
                last = i + 1
            }
        }
        if (last < len) {
            l = Caster.toIntValue(list.substring(last).trim(), 0)
            if (l > 0 && l <= maxRange) array.appendEL(l)
        }
        val intArr = IntArray(array.size())
        for (j in intArr.indices) {
            intArr[j] = array.get(j) as Int
        }
        return intArr
    }

    /**
     * casts a list to Long array  --  its used in CFIMAP/CFPOP tag with uid attribute
     *
     * @param list list to cast
     * @param delimiter delimter of the list
     * @return long array
     */
    fun listToLongArray(list: String?, delimiter: String?): LongArray? {
        val len: Int = list!!.length()
        val array = ArrayImpl()
        if (len == 0) return LongArray(0)
        var last = 0
        var l: Long
        val del: CharArray = delimiter.toCharArray()
        var c: Char
        for (i in 0 until len) {
            c = list.charAt(i)
            for (y in del.indices) {
                if (c == del[y]) {
                    l = Caster.toLong(list.substring(last, i).trim(), 0L)
                    if (l > 0) array.appendEL(l)
                    last = i + 1
                    break
                }
            }
        }
        if (last < len) {
            l = Caster.toLong(list.substring(last).trim(), 0L)
            if (l > 0) array.appendEL(l)
        }
        val longArr = LongArray(array.size())
        for (j in longArr.indices) {
            longArr[j] = array.get(j) as Long
        }
        return longArr
    }

    /**
     * casts a list to Array object, remove all empty items at start and end of the list and store count
     * to info
     *
     * @param list list to cast
     * @param pos
     * @param delimiter delimter of the list
     * @return Array Object
     * @throws ExpressionException
     */
    @Throws(ExpressionException::class)
    fun listInsertAt(list: String?, pos: Int, value: String?, delimiter: String?, ignoreEmpty: Boolean): String? {
        var list = list
        if (pos < 1) throw ExpressionException("invalid string list index [$pos]")
        val del: CharArray = delimiter.toCharArray()
        var c: Char
        val result = StringBuilder()
        var end = ""
        var len: Int

        // remove at start
        if (ignoreEmpty) {
            outer@ while (list!!.length() > 0) {
                c = list.charAt(0)
                for (i in del.indices) {
                    if (c == del[i]) {
                        list = list.substring(1)
                        result.append(c)
                        continue@outer
                    }
                }
                break
            }
        }

        // remove at end
        if (ignoreEmpty) {
            outer@ while (list!!.length() > 0) {
                c = list.charAt(list.length() - 1)
                for (i in del.indices) {
                    if (c == del[i]) {
                        len = list!!.length()
                        list = list.substring(0, if (len - 1 < 0) 0 else len - 1)
                        end = c + end
                        continue@outer
                    }
                }
                break
            }
        }
        len = list!!.length()
        var last = 0
        var count = 0
        outer@ for (i in 0 until len) {
            c = list.charAt(i)
            for (y in del.indices) {
                if (c == del[y]) {
                    if (!ignoreEmpty || last < i) {
                        if (pos == ++count) {
                            result.append(value)
                            result.append(del[0])
                        }
                    }
                    result.append(list.substring(last, i))
                    result.append(c)
                    last = i + 1
                    continue@outer
                }
            }
        }
        count++
        if (last <= len) {
            if (pos == count) {
                result.append(value)
                result.append(del[0])
            }
            result.append(list.substring(last))
        }
        if (pos > count) {
            throw ExpressionException("invalid string list index [$pos], indexes go from 1 to $count")
        }
        return result + end
    }

    /**
     * casts a list to Array object, remove all empty items at start and end of the list
     *
     * @param list list to cast
     * @param delimiter delimter of the list
     * @return Array Object
     */
    fun listToArrayTrim(list: String?, delimiter: Char): Array? {
        var list = list
        if (list!!.length() === 0) return ArrayImpl()
        // remove at start
        while (list.indexOf(delimiter) === 0) {
            list = list.substring(1)
        }
        var len: Int = list!!.length()
        if (len == 0) return ArrayImpl()
        while (list.lastIndexOf(delimiter) === len - 1) {
            list = list.substring(0, if (len - 1 < 0) 0 else len - 1)
            len = list!!.length()
        }
        return listToArray(list, delimiter)
    }

    /**
     * @param list
     * @param delimiter
     * @return trimmed list
     */
    fun toListTrim(list: String?, delimiter: Char): StringList? {
        var list = list
        if (list!!.length() === 0) return StringList()
        // remove at start
        while (list.indexOf(delimiter) === 0) {
            list = list.substring(1)
        }
        var len: Int = list!!.length()
        if (len == 0) return StringList()
        while (list.lastIndexOf(delimiter) === len - 1) {
            list = list.substring(0, if (len - 1 < 0) 0 else len - 1)
            len = list!!.length()
        }
        return toList(list, delimiter)
    }

    /**
     * @param list
     * @param delimiter
     * @return list
     */
    fun toList(list: String?, delimiter: Char): StringList? {
        if (list!!.length() === 0) return StringList()
        val len: Int = list!!.length()
        var last = 0
        val rtn = StringList()
        for (i in 0 until len) {
            if (list.charAt(i) === delimiter) {
                rtn.add(list.substring(last, i))
                last = i + 1
            }
        }
        if (last <= len) rtn.add(list.substring(last))
        rtn.reset()
        return rtn
    }

    fun toWordList(list: String?): StringList? {
        if (list!!.length() === 0) return StringList()
        val len: Int = list!!.length()
        var last = 0
        var c: Char
        var l = 0.toChar()
        val rtn = StringList()
        for (i in 0 until len) {
            if (StringUtil.isWhiteSpace(list.charAt(i).also { c = it })) {
                rtn.add(list.substring(last, i), l)
                l = c
                last = i + 1
            }
        }
        if (last <= len) rtn.add(list.substring(last), l)
        rtn.reset()
        return rtn
    }

    /**
     * casts a list to Array object, remove all empty items at start and end of the list
     *
     * @param list list to cast
     * @param delimiter delimter of the list
     * @param info
     * @return Array Object
     */
    fun listToArrayTrim(list: String?, delimiter: Char, info: IntArray?): Array? {
        var list = list
        if (list!!.length() === 0) return ArrayImpl()
        // remove at start
        while (list.indexOf(delimiter) === 0) {
            info!![0]++
            list = list.substring(1)
        }
        var len: Int = list!!.length()
        if (len == 0) return ArrayImpl()
        while (list.lastIndexOf(delimiter) === len - 1) {
            info!![1]++
            list = list.substring(0, if (len - 1 < 0) 0 else len - 1)
            len = list!!.length()
        }
        return listToArray(list, delimiter)
    }
    /*
	 * * finds a value inside a list, ignore case
	 * 
	 * @param list list to search
	 * 
	 * @param value value to find
	 * 
	 * @return position in list (0-n) or -1
	 *
	 * private static int listFindNoCase(String list, String value) { return listFindNoCase(list, value,
	 * ",", true); }
	 */
    /**
     * finds a value inside a list, do not ignore case
     *
     * @param list list to search
     * @param value value to find
     * @param delimiter delimiter of the list
     * @return position in list (0-n) or -1
     */
    fun listFindNoCase(list: String?, value: String?, delimiter: String?): Int {
        return listFindNoCase(list, value, delimiter, true)
    }

    /**
     * finds a value inside a list, do not ignore case
     *
     * @param list list to search
     * @param value value to find
     * @param delimiter delimiter of the list
     * @param trim trim the list or not
     * @return position in list (0-n) or -1
     */
    fun listFindNoCase(list: String?, value: String?, delimiter: String?, trim: Boolean): Int {
        val arr: Array? = listToArray(list, delimiter)
        val len: Int = arr.size()
        for (i in 1..len) {
            if ((arr.get(i, "") as String).equalsIgnoreCase(value)) return i - 1
        }
        return -1
    }

    fun listFindForSwitch(list: String?, value: String?, delimiter: String?): Int {
        if (list.indexOf(delimiter) === -1 && list.equalsIgnoreCase(value)) return 1
        val arr: Array? = listToArray(list, delimiter)
        val len: Int = arr.size()
        for (i in 1..len) {
            if ((arr.get(i, "") as String).equalsIgnoreCase(value)) return i
        }
        return -1
    }

    /**
     * finds a value inside a list, ignore case, ignore empty items
     *
     * @param list list to search
     * @param value value to find
     * @param delimiter delimiter of the list
     * @return position in list or 0
     */
    fun listFindNoCaseIgnoreEmpty(list: String?, value: String?, delimiter: String?): Int {
        if (delimiter!!.length() === 1) return listFindNoCaseIgnoreEmpty(list, value, delimiter.charAt(0))
        if (list == null) return -1
        val len: Int = list.length()
        if (len == 0) return -1
        var last = 0
        var count = 0
        val del: CharArray = delimiter.toCharArray()
        var c: Char
        for (i in 0 until len) {
            c = list.charAt(i)
            for (y in del.indices) {
                if (c == del[y]) {
                    if (last < i) {
                        if (list.substring(last, i).equalsIgnoreCase(value)) return count
                        count++
                    }
                    last = i + 1
                    break
                }
            }
        }
        if (last < len) {
            if (list.substring(last).equalsIgnoreCase(value)) return count
        }
        return -1
    }

    /**
     * finds a value inside a list, ignore case, ignore empty items
     *
     * @param list list to search
     * @param value value to find
     * @param delimiter delimiter of the list
     * @return position in list or 0
     */
    fun listFindNoCaseIgnoreEmpty(list: String?, value: String?, delimiter: Char): Int {
        if (list == null) return -1
        val len: Int = list.length()
        if (len == 0) return -1
        var last = 0
        var count = 0
        for (i in 0 until len) {
            if (list.charAt(i) === delimiter) {
                if (last < i) {
                    if (list.substring(last, i).equalsIgnoreCase(value)) return count
                    count++
                }
                last = i + 1
            }
        }
        if (last < len) {
            if (list.substring(last).equalsIgnoreCase(value)) return count
        }
        return -1
    }

    /**
     * finds a value inside a list, case sensitive
     *
     * @param list list to search
     * @param value value to find
     * @return position in list or 0
     */
    fun listFind(list: String?, value: String?): Int {
        return listFind(list, value, ",")
    }

    /**
     * finds a value inside a list, do not case sensitive
     *
     * @param list list to search
     * @param value value to find
     * @param delimiter delimiter of the list
     * @return position in list or 0
     */
    fun listFind(list: String?, value: String?, delimiter: String?): Int {
        val arr: Array? = listToArray(list, delimiter)
        val len: Int = arr.size()
        for (i in 1..len) {
            if (arr.get(i, "").equals(value)) return i - 1
        }
        return -1
    }

    /**
     * finds a value inside a list, case sensitive, ignore empty items
     *
     * @param list list to search
     * @param value value to find
     * @param delimiter delimiter of the list
     * @return position in list or 0
     */
    fun listFindIgnoreEmpty(list: String?, value: String?, delimiter: String?): Int {
        if (delimiter!!.length() === 1) return listFindIgnoreEmpty(list, value, delimiter.charAt(0))
        if (list == null) return -1
        val len: Int = list.length()
        if (len == 0) return -1
        var last = 0
        var count = 0
        val del: CharArray = delimiter.toCharArray()
        var c: Char
        for (i in 0 until len) {
            c = list.charAt(i)
            for (y in del.indices) {
                if (c == del[y]) {
                    if (last < i) {
                        if (list.substring(last, i).equals(value)) return count
                        count++
                    }
                    last = i + 1
                    break
                }
            }
        }
        if (last < len) {
            if (list.substring(last).equals(value)) return count
        }
        return -1
    }

    /**
     * finds a value inside a list, case sensitive, ignore empty items
     *
     * @param list list to search
     * @param value value to find
     * @param delimiter delimiter of the list
     * @return position in list or 0
     */
    fun listFindIgnoreEmpty(list: String?, value: String?, delimiter: Char): Int {
        if (list == null) return -1
        val len: Int = list.length()
        if (len == 0) return -1
        var last = 0
        var count = 0
        for (i in 0 until len) {
            if (list.charAt(i) === delimiter) {
                if (last < i) {
                    if (list.substring(last, i).equals(value)) return count
                    count++
                }
                last = i + 1
            }
        }
        if (last < len) {
            if (list.substring(last).equals(value)) return count
        }
        return -1
    }

    /**
     * returns if a value of the list contains given value, ignore case
     *
     * @param list list to search in
     * @param value value to serach
     * @param delimiter delimiter of the list
     * @return position in list or 0
     */
    fun listContainsNoCase(list: String?, value: String?, delimiter: String?, includeEmptyFields: Boolean, multiCharacterDelimiter: Boolean): Int {
        if (StringUtil.isEmpty(value)) return -1
        val arr: Array? = listToArray(list, delimiter, includeEmptyFields, multiCharacterDelimiter)
        val len: Int = arr.size()
        for (i in 1..len) {
            if (StringUtil.indexOfIgnoreCase(arr.get(i, "").toString(), value) !== -1) return i - 1
        }
        return -1
    }
    /*
	 * * returns if a value of the list contains given value, ignore case, ignore empty values
	 * 
	 * @param list list to search in
	 * 
	 * @param value value to serach
	 * 
	 * @param delimiter delimiter of the list
	 * 
	 * @return position in list or 0
	 * 
	 * public static int listContainsIgnoreEmptyNoCase(String list, String value, String delimiter) {
	 * if(StringUtil.isEmpty(value)) return -1; Array arr=listToArrayRemoveEmpty(list,delimiter); int
	 * count=0; int len=arr.size();
	 * 
	 * for(int i=1;i<=len;i++) { String item=arr.get(i,"").toString();
	 * if(StringUtil.indexOfIgnoreCase(item, value)!=-1) return count; count++; } return -1; }
	 */
    /**
     * returns if a value of the list contains given value, case sensitive
     *
     * @param list list to search in
     * @param value value to serach
     * @param delimiter delimiter of the list
     * @return position in list or 0
     */
    fun listContains(list: String?, value: String?, delimiter: String?, includeEmptyFields: Boolean, multiCharacterDelimiter: Boolean): Int {
        if (StringUtil.isEmpty(value)) return -1
        val arr: Array? = listToArray(list, delimiter, includeEmptyFields, multiCharacterDelimiter)
        val len: Int = arr.size()
        for (i in 1..len) {
            if (arr.get(i, "").toString().indexOf(value) !== -1) return i - 1
        }
        return -1
    }
    /*
	 * * returns if a value of the list contains given value, case sensitive, ignore empty positions
	 * 
	 * @param list list to search in
	 * 
	 * @param value value to serach
	 * 
	 * @param delimiter delimiter of the list
	 * 
	 * @return position in list or 0
	 * 
	 * private static int listContainsIgnoreEmpty(String list, String value, String delimiter, boolean
	 * multiCharacterDelimiter) { if(StringUtil.isEmpty(value)) return -1; Array
	 * arr=listToArrayRemoveEmpty(list,delimiter); int count=0; int len=arr.size();
	 * 
	 * String item; for(int i=1;i<=len;i++) { item=arr.get(i,"").toString(); if(item.indexOf(value)!=-1)
	 * return count; count++; } return -1; }
	 */
    /**
     * convert a string array to string list, removes empty values at begin and end of the list
     *
     * @param array array to convert
     * @param delimiter delimiter for the new list
     * @return list generated from string array
     */
    fun arrayToListTrim(array: Array<String?>?, delimiter: String?): String? {
        return trim(arrayToList(array, delimiter), delimiter, false)
    }

    /**
     * convert a string array to string list
     *
     * @param array array to convert
     * @param delimiter delimiter for the new list
     * @return list generated from string array
     */
    fun arrayToList(array: Array<String?>?, delimiter: String?): String? {
        if (ArrayUtil.isEmpty(array)) return ""
        val sb = StringBuilder(array!![0])
        if (delimiter!!.length() === 1) {
            val c: Char = delimiter.charAt(0)
            for (i in 1 until array!!.size) {
                sb.append(c)
                sb.append(array!![i])
            }
        } else {
            for (i in 1 until array!!.size) {
                sb.append(delimiter)
                sb.append(array!![i])
            }
        }
        return sb.toString()
    }

    fun arrayToList(array: Array<Collection.Key?>?, delimiter: String?): String? {
        if (array!!.size == 0) return ""
        val sb = StringBuilder(array[0].getString())
        if (delimiter!!.length() === 1) {
            val c: Char = delimiter.charAt(0)
            for (i in 1 until array.size) {
                sb.append(c)
                sb.append(array[i].getString())
            }
        } else {
            for (i in 1 until array.size) {
                sb.append(delimiter)
                sb.append(array[i].getString())
            }
        }
        return sb.toString()
    }

    /**
     * convert Array Object to string list
     *
     * @param array array to convert
     * @param delimiter delimiter for the new list
     * @return list generated from string array
     * @throws PageException
     */
    @Throws(PageException::class)
    fun arrayToList(array: Array?, delimiter: String?): String? {
        if (array.size() === 0) return ""
        val sb = StringBuilder(Caster.toString(array.get(1, "")))
        val len: Int = array.size()
        for (i in 2..len) {
            sb.append(delimiter)
            sb.append(Caster.toString(array.get(i, "")))
        }
        return sb.toString()
    }

    fun toList(list: Collection<String?>?, delimiter: String?): String? {
        if (list!!.size() === 0) return ""
        val sb = StringBuilder()
        val it = list!!.iterator()
        if (it.hasNext()) sb.append(it.next())
        while (it.hasNext()) {
            sb.append(delimiter)
            sb.append(it.next())
        }
        return sb.toString()
    }

    @Throws(PageException::class)
    fun listToList(list: List<*>?, delimiter: String?): String? {
        if (list!!.size() === 0) return ""
        val sb = StringBuilder()
        val it = list!!.iterator()
        if (it.hasNext()) sb.append(Caster.toString(it.next()))
        while (it.hasNext()) {
            sb.append(delimiter)
            sb.append(Caster.toString(it.next()))
        }
        return sb.toString()
    }

    fun listToListEL(list: List<String?>?, delimiter: String?): String? {
        if (list!!.size() === 0) return ""
        val sb = StringBuilder()
        val it = list!!.iterator()
        if (it.hasNext()) sb.append(it.next())
        while (it.hasNext()) {
            sb.append(delimiter)
            sb.append(it.next())
        }
        return sb.toString()
    }

    /**
     * trims a string array, removes all empty array positions at the start and the end of the array
     *
     * @param array array to remove elements
     * @return cleared array
     */
    fun trim(array: Array<String?>?): Array<String?>? {
        var from = 0
        var to = 0

        // test start
        for (i in array.indices) {
            from = i
            if (array!![i]!!.length() !== 0) break
        }

        // test end
        for (i in array.indices.reversed()) {
            to = i
            if (array!![i]!!.length() !== 0) break
        }
        val newLen = to - from + 1
        if (newLen < array!!.size) {
            val rtn = arrayOfNulls<String?>(newLen)
            System.arraycopy(array, from, rtn, 0, newLen)
            return rtn
        }
        return array
    }

    fun trim(array: Array?): Array? {
        while (array.size() > 0 && Caster.toString(array.get(1, ""), "").isEmpty()) {
            array.removeEL(1)
        }
        while (array.size() > 0 && Caster.toString(array.get(array.size(), ""), "").isEmpty()) {
            array.removeEL(array.size())
        }
        return array
    }

    /**
     * trims a string list, remove all empty delimiter at start and the end
     *
     * @param list list to trim
     * @param delimiter delimiter of the list
     * @return trimed list
     */
    fun trim(list: String?, delimiter: String?): String? {
        return trim(list, delimiter, IntArray(2), false)
    }

    fun trim(list: String?, delimiter: String?, multiCharacterDelimiter: Boolean): String? {
        return trim(list, delimiter, IntArray(2), multiCharacterDelimiter)
    }

    /**
     * trims a string list, remove all empty delimiter at start and the end
     *
     * @param list list to trim
     * @param delimiter delimiter of the list
     * @param removeInfo int array contain count of removed values (removeInfo[0]=at the
     * begin;removeInfo[1]=at the end)
     * @return trimed list
     */
    fun trim(list: String?, delimiter: String?, removeInfo: IntArray?, multiCharacterDelimiter: Boolean): String? {
        var list = list
        if (list!!.length() === 0) return ""
        if (multiCharacterDelimiter && delimiter!!.length() > 1) {
            var from = 0

            // remove at start
            while (list!!.length() >= from + delimiter!!.length()) {
                if (list.indexOf(delimiter, from) === from) {
                    from += delimiter!!.length()
                    removeInfo!![0]++
                    continue
                }
                break
            }
            if (from > 0) list = list.substring(from)

            // remove at end
            while (list!!.length() >= delimiter!!.length()) {
                if (list.lastIndexOf(delimiter) === list.length() - delimiter.length()) {
                    removeInfo!![1]++
                    list = list.substring(0, list.length() - delimiter!!.length())
                    continue
                }
                break
            }
            return list
        }
        if (list!!.length() === 0) return ""
        var from = 0
        var to: Int = list!!.length()
        // int len=delimiter.length();
        val del: CharArray = delimiter.toCharArray()
        var c: Char

        // remove at start
        outer@ while (list.length() > from) {
            c = list.charAt(from)
            for (i in del.indices) {
                if (c == del[i]) {
                    from++
                    removeInfo!![0]++
                    // list=list.substring(from);
                    continue@outer
                }
            }
            break
        }

        // int len;
        outer@ while (to > from) {
            c = list.charAt(to - 1)
            for (i in del.indices) {
                if (c == del[i]) {
                    to--
                    removeInfo!![1]++
                    continue@outer
                }
            }
            break
        }
        val newLen = to - from
        return if (newLen < list.length()) {
            list.substring(from, to)
        } else list
    }

    /**
     * sorts a string list
     *
     * @param list list to sort
     * @param sortType sort type (numeric,text,textnocase)
     * @param sortOrder sort order (asc,desc)
     * @param delimiter list delimiter
     * @return sorted list
     * @throws PageException
     */
    @Throws(PageException::class)
    fun sortIgnoreEmpty(list: String?, sortType: String?, sortOrder: String?, delimiter: String?): String? {
        return _sort(toStringArray(listToArrayRemoveEmpty(list, delimiter)), sortType, sortOrder, delimiter)
    }

    /**
     * sorts a string list
     *
     * @param list list to sort
     * @param sortType sort type (numeric,text,textnocase)
     * @param sortOrder sort order (asc,desc)
     * @param delimiter list delimiter
     * @return sorted list
     * @throws PageException
     */
    @Throws(PageException::class)
    fun sort(list: String?, sortType: String?, sortOrder: String?, delimiter: String?): String? {
        return _sort(toStringArray(listToArray(list, delimiter)), sortType, sortOrder, delimiter)
    }

    @Throws(PageException::class)
    private fun _sort(arr: Array<Object?>?, sortType: String?, sortOrder: String?, delimiter: String?): String? {
        Arrays.sort(arr, ArrayUtil.toComparator(null, sortType, sortOrder, false))
        val sb = StringBuilder()
        for (i in arr.indices) {
            if (i != 0) sb.append(delimiter)
            sb.append(arr!![i])
        }
        return sb.toString()
    }

    /**
     * cast an Object Array to a String Array
     *
     * @param array
     * @return String Array
     */
    fun toStringArrayEL(array: Array?): Array<String?>? {
        val arr = arrayOfNulls<String?>(array.size())
        for (i in arr.indices) {
            arr[i] = Caster.toString(array.get(i + 1, null), null)
        }
        return arr
    }

    /**
     * cast an Object Array to a String Array
     *
     * @param array
     * @return String Array
     * @throws PageException
     */
    @Throws(PageException::class)
    fun toStringArray(array: Array?): Array<String?>? {
        val arr = arrayOfNulls<String?>(array.size())
        for (i in arr.indices) {
            arr[i] = Caster.toString(array.get(i + 1, null))
        }
        return arr
    }

    fun toStringArray(coll: Collection<String?>?): Array<String?>? {
        return coll.toArray(arrayOfNulls<String?>(coll!!.size()))
    }

    fun toStringArray(list: List<String?>?): Array<String?>? {
        return list.toArray(arrayOfNulls<String?>(list!!.size()))
    }

    /**
     * cast an Object Array to a String Array
     *
     * @param array
     * @param defaultValue
     * @return String Array
     */
    fun toStringArray(array: Array?, defaultValue: String?): Array<String?>? {
        val arr = arrayOfNulls<String?>(array.size())
        for (i in arr.indices) {
            arr[i] = Caster.toString(array.get(i + 1, defaultValue), defaultValue)
        }
        return arr
    }

    /**
     * cast an Object Array to a String Array and trim all values
     *
     * @param array
     * @return String Array
     * @throws PageException
     */
    @Throws(PageException::class)
    fun toStringArrayTrim(array: Array?): Array<String?>? {
        val arr = arrayOfNulls<String?>(array.size())
        for (i in arr.indices) {
            arr[i] = Caster.toString(array.get(i + 1, "")).trim()
        }
        return arr
    }

    /**
     * return first element of the list
     *
     * @param list
     * @param delimiter
     * @return returns the first element of the list
     */
    @Deprecated
    @Deprecated("use instead first(String list, String delimiter, boolean ignoreEmpty)")
    fun first(list: String?, delimiter: String?): String? {
        return first(list, delimiter, true, 1)
    }

    /**
     * return last element of the list
     *
     * @param list
     * @param delimiter
     * @return returns the last Element of a list
     */
    @Deprecated
    @Deprecated("use instead last(String list, String delimiter, boolean ignoreEmpty)")
    fun last(list: String?, delimiter: String?): String? {
        return last(list, delimiter, true)
    }

    /**
     * return last element of the list
     *
     * @param list
     * @param delimiter
     * @param ignoreEmpty
     * @return returns the last Element of a list
     */
    fun last(list: String?, delimiter: String?, ignoreEmpty: Boolean): String? {
        var list = list
        if (StringUtil.isEmpty(list)) return ""
        var len: Int = list!!.length()
        val del: CharArray?
        if (StringUtil.isEmpty(delimiter)) {
            del = charArrayOf(',')
        } else del = delimiter.toCharArray()
        var index: Int
        var x: Int
        while (true) {
            index = -1
            for (i in del.indices) {
                x = list.lastIndexOf(del!![i])
                if (x > index) index = x
            }
            if (index == -1) {
                return list
            } else if (index + 1 == len) {
                if (!ignoreEmpty) return ""
                list = list.substring(0, len - 1)
                len--
            } else {
                return list.substring(index + 1)
            }
        }
    }

    /**
     * return last element of the list
     *
     * @param list
     * @param delimiter
     * @return returns the last Element of a list
     */
    fun last(list: String?, delimiter: Char): String? {
        var list = list
        var len: Int = list!!.length()
        if (len == 0) return ""
        var index = 0
        while (true) {
            index = list.lastIndexOf(delimiter)
            if (index == -1) {
                return list
            } else if (index + 1 == len) {
                list = list.substring(0, len - 1)
                len--
            } else {
                return list.substring(index + 1)
            }
        }
    }

    /**
     * returns count of items in the list
     *
     * @param list
     * @param delimiter
     * @return list len
     */
    fun len(list: String?, delimiter: Char, ignoreEmpty: Boolean): Int {
        val len: Int = StringUtil.length(list)
        if (len == 0 && ignoreEmpty) return 0
        var count = 0
        var last = 0
        for (i in 0 until len) {
            if (list.charAt(i) === delimiter) {
                if (!ignoreEmpty || last < i) count++
                last = i + 1
            }
        }
        if (!ignoreEmpty || last < len) count++
        return count
    }

    /**
     * returns count of items in the list
     *
     * @param list
     * @param delimiter
     * @return list len
     */
    fun len(list: String?, delimiter: String?, ignoreEmpty: Boolean): Int {
        if (delimiter!!.length() === 1) return len(list, delimiter.charAt(0), ignoreEmpty)
        val del: CharArray = delimiter.toCharArray()
        val len: Int = StringUtil.length(list)
        if (len == 0 && ignoreEmpty) return 0
        var count = 0
        var last = 0
        var c: Char
        for (i in 0 until len) {
            c = list.charAt(i)
            for (y in del.indices) {
                if (c == del[y]) {
                    if (!ignoreEmpty || last < i) count++
                    last = i + 1
                    break
                }
            }
        }
        if (!ignoreEmpty || last < len) count++
        return count
    }
    /*
	 * * cast an int into a char
	 * 
	 * @param i int to cast
	 * 
	 * @return int as char / private char c(int i) { return (char)i; }
	 */
    /**
     * gets a value from list
     *
     * @param list list to cast
     * @param delimiter delimter of the list
     * @param position
     * @return Array Object
     */
    fun getAt(list: String?, delimiter: String?, position: Int, ignoreEmpty: Boolean, defaultValue: String?): String? {
        if (delimiter!!.length() === 1) return getAt(list, delimiter.charAt(0), position, ignoreEmpty, defaultValue)
        val len: Int = list!!.length()
        if (len == 0) return defaultValue
        var last = -1
        var count = -1
        val del: CharArray = delimiter.toCharArray()
        var c: Char
        for (i in 0 until len) {
            c = list.charAt(i)
            for (y in del.indices) {
                if (c == del[y]) {
                    if (ignoreEmpty && last + 1 == i) {
                        last = i
                        break
                    }
                    count++
                    if (count == position) {
                        return list.substring(last + 1, i)
                    }
                    last = i
                    break
                }
            }
        }
        if (position == count + 1) {
            if (!ignoreEmpty || last + 1 < len) return list.substring(last + 1)
        }
        return defaultValue
    }

    /**
     * get an element at a specified position in list
     *
     * @param list list to cast
     * @param delimiter delimter of the list
     * @param position
     * @return Array Object
     */
    fun getAt(list: String?, delimiter: Char, position: Int, ignoreEmpty: Boolean, defaultValue: String?): String? {
        val len: Int = list!!.length()
        if (len == 0) return defaultValue
        var last = -1
        var count = -1
        for (i in 0 until len) {
            // char == delimiter
            if (list.charAt(i) === delimiter) {
                if (ignoreEmpty && last + 1 == i) {
                    last = i
                    continue
                }
                count++
                if (count == position) {
                    return list.substring(last + 1, i)
                }
                last = i
            }
        }
        if (position == count + 1) {
            if (!ignoreEmpty || last + 1 < len) return list.substring(last + 1)
        }
        return defaultValue
    }

    fun listToStringArray(list: String?, delimiter: Char): Array<String?>? {
        val array: Array? = listToArrayRemoveEmpty(list, delimiter)
        val arr = arrayOfNulls<String?>(array.size())
        for (i in arr.indices) {
            arr[i] = Caster.toString(array.get(i + 1, ""), "")
        }
        return arr
    }

    fun listToStringArray(list: String?, delimiter: String?): Array<String?>? {
        val array: Array? = listToArrayRemoveEmpty(list, delimiter)
        val arr = arrayOfNulls<String?>(array.size())
        for (i in arr.indices) {
            arr[i] = Caster.toString(array.get(i + 1, ""), "")
        }
        return arr
    }

    /**
     * trim every single item of the array
     *
     * @param arr
     * @return
     */
    fun trimItems(arr: Array<String?>?): Array<String?>? {
        for (i in arr.indices) {
            arr!![i] = arr[i].trim()
        }
        return arr
    }

    /**
     * trim every single item of the array
     *
     * @param arr
     * @return
     * @throws PageException
     */
    @Throws(PageException::class)
    fun trimItems(arr: Array?): Array? {
        val keys: Array<Key?> = CollectionUtil.keys(arr)
        for (i in keys.indices) {
            arr.setEL(keys[i], Caster.toString(arr.get(keys[i], null)).trim())
        }
        return arr
    }

    fun listToSet(list: String?, delimiter: String?, trim: Boolean): Set<String?>? {
        if (list == null || list.length() === 0) return HashSet<String?>()
        val len: Int = list.length()
        var last = 0
        val del: CharArray = delimiter.toCharArray()
        var c: Char
        val set: HashSet<String?> = HashSet<String?>()
        for (i in 0 until len) {
            c = list.charAt(i)
            for (y in del.indices) {
                if (c == del[y]) {
                    set.add(if (trim) list.substring(last, i).trim() else list.substring(last, i))
                    last = i + 1
                    break
                }
            }
        }
        if (last <= len) set.add(if (trim) list.substring(last).trim() else list.substring(last))
        return set
    }

    fun listToSet(list: String?, delimiter: Char, trim: Boolean): Set<String?>? {
        if (list!!.length() === 0) return HashSet<String?>()
        val len: Int = list!!.length()
        var last = 0
        var c: Char
        val set: HashSet<String?> = HashSet<String?>()
        for (i in 0 until len) {
            c = list.charAt(i)
            if (c == delimiter) {
                set.add(if (trim) list.substring(last, i).trim() else list.substring(last, i))
                last = i + 1
            }
        }
        if (last <= len) set.add(if (trim) list.substring(last).trim() else list.substring(last))
        return set
    }

    fun toSet(arr: Array<String?>?): Set<String?>? {
        val set: Set<String?> = HashSet<String?>()
        for (i in arr.indices) {
            set.add(arr!![i])
        }
        return set
    }

    fun listToList(list: String?, delimiter: Char, trim: Boolean): List<String?>? {
        if (list!!.length() === 0) return ArrayList<String?>()
        val len: Int = list!!.length()
        var last = 0
        var c: Char
        val rtn: ArrayList<String?> = ArrayList<String?>()
        for (i in 0 until len) {
            c = list.charAt(i)
            if (c == delimiter) {
                rtn.add(if (trim) list.substring(last, i).trim() else list.substring(last, i))
                last = i + 1
            }
        }
        if (last <= len) rtn.add(list.substring(last))
        return rtn
    }

    fun first(list: String?, delimiters: String?, ignoreEmpty: Boolean, count: Int): String? {
        if (count < 1) return ""
        val delims = if (StringUtil.isEmpty(delimiters)) charArrayOf(',') else delimiters.toCharArray()
        val sbList = StringBuilder(list)
        val ix = getDelimIndex(sbList, count, delims, ignoreEmpty)
        return if (ix == -1) list else sbList.substring(0, ix)
    }

    fun last(list: String?, delimiters: String?, ignoreEmpty: Boolean, count: Int): String? {
        if (count < 1) return ""
        val delims = if (StringUtil.isEmpty(delimiters)) charArrayOf(',') else delimiters.toCharArray()
        val sbList: StringBuilder? = rev(list) // new StringBuilder(list);
        val ix = getDelimIndex(sbList, count, delims, ignoreEmpty)
        return if (ix == -1) list else rev(sbList.substring(0, ix)).toString()
    }

    private fun rev(list: CharSequence?): StringBuilder? {
        if (StringUtil.isEmpty(list)) return StringBuilder()
        val sb = StringBuilder()
        for (i in list!!.length() - 1 downTo 0) {
            sb.append(list.charAt(i))
        }
        return sb
    }

    fun first(list: String?, delimiters: String?, ignoreEmpty: Boolean): String? {
        return first(list, delimiters, ignoreEmpty, 1)
    }

    fun rest(list: String?, delimiters: String?, ignoreEmpty: Boolean, offset: Int): String? {
        if (offset < 1) return list
        val delims = if (StringUtil.isEmpty(delimiters)) charArrayOf(',') else delimiters.toCharArray()
        val sbList = StringBuilder(list)
        val ix = getDelimIndex(sbList, offset, delims, ignoreEmpty)
        return if (ix == -1 || ix >= sbList.length() - 1) "" else sbList.substring(ix + 1)
    }

    fun rest(list: String?, delimiters: String?, ignoreEmpty: Boolean): String? {
        return rest(list, delimiters, ignoreEmpty, 1)
    }

    /**
     * returns the 0-based delimiter position for the specified item
     *
     * @param list
     * @param itemPos
     * @param ignoreEmpty
     * @return
     */
    fun getDelimIndex(sb: StringBuilder?, itemPos: Int, delims: CharArray?, ignoreEmpty: Boolean): Int {
        if (StringUtil.isEmpty(sb)) return -1
        var curr = -1
        var listIndex = 0
        var len: Int = sb.length()
        var i = 0
        while (i < len) {
            if (contains(delims, sb.charAt(i))) {
                curr = i
                if (ignoreEmpty) {
                    if (i == 0 || i + 1 < len && contains(delims, sb.charAt(i + 1))) {
                        sb.delete(curr, curr + 1)
                        len--
                        i--
                        curr--
                        i++
                        continue
                    }
                }
                if (++listIndex == itemPos) break
            }
            i++
        }
        return if (listIndex < itemPos) len else curr
    }

    private fun contains(carr: CharArray?, c: Char): Boolean {
        if (carr!!.size == 1) return carr[0] == c
        for (ca in carr) {
            if (ca == c) return true
        }
        return false
    }

    fun toList(set: Set<String?>?): List<String?>? {
        val it = set!!.iterator()
        val list: List<String?> = ArrayList<String?>()
        while (it.hasNext()) {
            list.add(it.next())
        }
        return list
    }

    fun arrayToList(arr: Array<String?>?): List<String?>? {
        val list: List<String?> = ArrayList<String?>()
        for (i in arr.indices) {
            list.add(arr!![i])
        }
        return list
    }

    @Throws(PageException::class)
    fun toList(arr: Array?): List<String?>? {
        val it: Iterator<Object?> = arr.valueIterator()
        val list: List<String?> = ArrayList<String?>()
        while (it.hasNext()) {
            list.add(Caster.toString(it.next()))
        }
        return list
    }

    fun toIterator(input: Enumeration<String?>?): Iterator<String?>? {
        val output: List<String?> = ArrayList<String?>()
        while (input.hasMoreElements()) {
            output.add(input.nextElement())
        }
        return output.iterator()
    }
}