/**
 * Copyright (c) 2015, Tachyon Assosication Switzerland. All rights reserved.
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
package tachyon.runtime.util

import java.util.List

interface ListUtil {
    /**
     * casts a list to Array object, the list can be have quoted (",') arguments and delimiter in this
     * arguments are ignored. quotes are not removed example:
     * listWithQuotesToArray("aab,a'a,b',a\"a,b\"",",","\"'") will be translated to
     * ["aab","a'a,b'","a\"a,b\""]
     *
     * @param list list to cast
     * @param delimiter delimiter of the list
     * @param quotes quotes of the list
     * @return Array Object
     */
    fun listWithQuotesToArray(list: String?, delimiter: String?, quotes: String?): Array?

    /**
     * casts a list to Array object
     *
     * @param list list to cast
     * @param delimiter delimiter of the list
     * @return Array Object
     */
    fun toArray(list: String?, delimiter: String?): Array?
    fun toArray(list: String?, delimiter: String?, includeEmptyFields: Boolean, multiCharDelim: Boolean): Array?

    /**
     * casts a list to Array object remove Empty Elements
     *
     * @param list list to cast
     * @param delimiter delimiter of the list
     * @return Array Object
     */
    fun toArrayRemoveEmpty(list: String?, delimiter: String?): Array?
    fun toListRemoveEmpty(list: String?, delimiter: Char): List<String?>?

    /**
     * casts a list to Array object, remove all empty items at start and end of the list
     *
     * @param list list to cast
     * @param delimiter delimiter of the list
     * @return Array Object
     */
    fun toArrayTrim(list: String?, delimiter: String?): Array?

    /**
     * casts a list to Array object, remove all empty items at start and end of the list and store count
     * to info
     *
     * @param list list to cast
     * @param pos position
     * @param value value
     * @param delimiter delimiter of the list
     * @param ignoreEmpty ignore empty
     * @return Array Object
     * @throws PageException Page Exception
     */
    @Throws(PageException::class)
    fun insertAt(list: String?, pos: Int, value: String?, delimiter: String?, ignoreEmpty: Boolean): String?

    /**
     * finds a value inside a list, do not ignore case
     *
     * @param list list to search
     * @param value value to find
     * @param delimiter delimiter of the list
     * @return position in list (0-n) or -1
     */
    fun findNoCase(list: String?, value: String?, delimiter: String?): Int

    /**
     * finds a value inside a list, do not ignore case
     *
     * @param list list to search
     * @param value value to find
     * @param delimiter delimiter of the list
     * @param trim trim the list or not
     * @return position in list (0-n) or -1
     */
    fun findNoCase(list: String?, value: String?, delimiter: String?, trim: Boolean): Int
    fun findForSwitch(list: String?, value: String?, delimiter: String?): Int

    /**
     * finds a value inside a list, ignore case, ignore empty items
     *
     * @param list list to search
     * @param value value to find
     * @param delimiter delimiter of the list
     * @return position in list or 0
     */
    fun findNoCaseIgnoreEmpty(list: String?, value: String?, delimiter: String?): Int

    /**
     * finds a value inside a list, ignore case, ignore empty items
     *
     * @param list list to search
     * @param value value to find
     * @param delimiter delimiter of the list
     * @return position in list or 0
     */
    fun findNoCaseIgnoreEmpty(list: String?, value: String?, delimiter: Char): Int

    /**
     * finds a value inside a list, case sensitive
     *
     * @param list list to search
     * @param value value to find
     * @return position in list or 0
     */
    fun find(list: String?, value: String?): Int

    /**
     * finds a value inside a list, do not case sensitive
     *
     * @param list list to search
     * @param value value to find
     * @param delimiter delimiter of the list
     * @return position in list or 0
     */
    fun find(list: String?, value: String?, delimiter: String?): Int

    /**
     * finds a value inside a list, case sensitive, ignore empty items
     *
     * @param list list to search
     * @param value value to find
     * @param delimiter delimiter of the list
     * @return position in list or 0
     */
    fun findIgnoreEmpty(list: String?, value: String?, delimiter: String?): Int

    /**
     * finds a value inside a list, case sensitive, ignore empty items
     *
     * @param list list to search
     * @param value value to find
     * @param delimiter delimiter of the list
     * @return position in list or 0
     */
    fun findIgnoreEmpty(list: String?, value: String?, delimiter: Char): Int

    /**
     * returns if a value of the list contains given value, ignore case
     *
     * @param list list to search in
     * @param value value to search
     * @param delimiter delimiter of the list
     * @param includeEmptyFields include empty fields
     * @param multiCharacterDelimiter multi character delimiter
     * @return position in list or 0
     */
    fun containsNoCase(list: String?, value: String?, delimiter: String?, includeEmptyFields: Boolean, multiCharacterDelimiter: Boolean): Int

    /**
     * returns if a value of the list contains given value, case sensitive
     *
     * @param list list to search in
     * @param value value to search
     * @param delimiter delimiter of the list
     * @param includeEmptyFields include empty fields
     * @param multiCharacterDelimiter multi character delimiter
     *
     * @return position in list or 0
     */
    fun contains(list: String?, value: String?, delimiter: String?, includeEmptyFields: Boolean, multiCharacterDelimiter: Boolean): Int

    /**
     * convert a string array to string list, removes empty values at begin and end of the list
     *
     * @param array array to convert
     * @param delimiter delimiter for the new list
     * @return list generated from string array
     */
    fun toListTrim(array: Array<String?>?, delimiter: String?): String?

    /**
     * convert a string array to string list
     *
     * @param array array to convert
     * @param delimiter delimiter for the new list
     * @return list generated from string array
     */
    fun toList(array: Array<String?>?, delimiter: String?): String?
    fun toList(array: Array<Collection.Key?>?, delimiter: String?): String?

    /**
     * convert Array Object to string list
     *
     * @param array Array to convert
     * @param delimiter delimiter for the new list
     * @return list generated from string Array
     * @throws PageException Page Exception
     */
    @Throws(PageException::class)
    fun toList(array: Array?, delimiter: String?): String?

    @Throws(PageException::class)
    fun toList(list: List<*>?, delimiter: String?): String?

    /**
     * input is already a String List, so no casting necessary
     *
     * @param list List
     * @param delimiter delimiter of the list
     * @return Returns a list.
     */
    fun toListEL(list: List<String?>?, delimiter: String?): String?

    /**
     * trims a string array, removes all empty array positions at the start and the end of the array
     *
     * @param array array to remove elements
     * @return cleared array
     */
    fun trim(array: Array<String?>?): Array<String?>?

    /**
     * trims a string list, remove all empty delimiter at start and the end
     *
     * @param list list to trim
     * @param delimiter delimiter of the list
     * @param multiCharacterDelimiter is a delimeter with multiple character handled as ne character or
     * as many
     * @return trimmed list
     */
    fun trim(list: String?, delimiter: String?, multiCharacterDelimiter: Boolean): String?

    /**
     * sorts a string list
     *
     * @param list list to sort
     * @param sortType sort type (numeric,text,textnocase)
     * @param sortOrder sort order (asc,desc)
     * @param delimiter list delimiter
     * @return sorted list
     * @throws PageException Page Exception
     */
    @Throws(PageException::class)
    fun sortIgnoreEmpty(list: String?, sortType: String?, sortOrder: String?, delimiter: String?): String?

    /**
     * sorts a string list
     *
     * @param list list to sort
     * @param sortType sort type (numeric,text,textnocase)
     * @param sortOrder sort order (asc,desc)
     * @param delimiter list delimiter
     * @return sorted list
     * @throws PageException Page Exception
     */
    @Throws(PageException::class)
    fun sort(list: String?, sortType: String?, sortOrder: String?, delimiter: String?): String?

    /**
     * cast an Object Array to a String Array
     *
     * @param array Array to be casted
     * @return String Array
     * @throws PageException Page Exception
     */
    @Throws(PageException::class)
    fun toStringArray(array: Array?): Array<String?>?
    fun toStringArray(set: Set<String?>?): Array<String?>?
    fun toStringArray(list: List<String?>?): Array<String?>?

    /**
     * cast an Object Array to a String Array
     *
     * @param array Array
     * @param defaultValue default Value
     * @return String Array
     */
    fun toStringArray(array: Array?, defaultValue: String?): Array<String?>?

    /**
     * cast an Object Array to a String Array and trim all values
     *
     * @param array Array
     * @return String Array
     * @throws PageException Page Exception
     */
    @Throws(PageException::class)
    fun toStringArrayTrim(array: Array?): Array<String?>?

    /**
     * return last element of the list
     *
     * @param list List
     * @param delimiter delimiter of the list
     * @param ignoreEmpty ignore empty
     * @return returns the last Element of a list
     */
    fun last(list: String?, delimiter: String?, ignoreEmpty: Boolean): String?

    /**
     * returns count of items in the list
     *
     * @param list List
     * @param delimiter delimiter of the list
     * @param ignoreEmpty ignore empty
     * @return list len
     */
    fun len(list: String?, delimiter: String?, ignoreEmpty: Boolean): Int

    /**
     * gets a value from list
     *
     * @param list list to cast
     * @param delimiter delimiter of the list
     * @param position position
     * @param ignoreEmpty ignore empty
     * @param defaultValue default Value
     * @return Array Object
     */
    fun getAt(list: String?, delimiter: String?, position: Int, ignoreEmpty: Boolean, defaultValue: String?): String?
    fun toStringArray(list: String?, delimiter: String?): Array<String?>?

    /**
     * trim every single item of the Array
     *
     * @param arr Array
     * @return Returns a trimmed list.
     */
    fun trimItems(arr: Array<String?>?): Array<String?>?

    /**
     * trim every single item of the Array
     *
     * @param arr Array
     * @return Returns a trimmed list.
     * @throws PageException Page Exception
     */
    @Throws(PageException::class)
    fun trimItems(arr: Array?): Array?
    fun toSet(list: String?, delimiter: String?, trim: Boolean): Set<String?>?
    fun toSet(arr: Array<String?>?): Set<String?>?
    fun first(list: String?, delimiters: String?, ignoreEmpty: Boolean, count: Int): String?
    fun first(list: String?, delimiters: String?, ignoreEmpty: Boolean): String?
    fun rest(list: String?, delimiters: String?, ignoreEmpty: Boolean, offset: Int): String?
    fun rest(list: String?, delimiters: String?, ignoreEmpty: Boolean): String?

    /**
     * returns the 0-based delimiter position for the specified item
     *
     * @param list List
     * @param itemPos Item Position
     * @param delims delimiters of the list
     * @param ignoreEmpty Ignore Empty
     * @return Returns the delimiter position.
     */
    fun getDelimIndex(list: String?, itemPos: Int, delims: CharArray?, ignoreEmpty: Boolean): Int
    fun toList(set: Set<String?>?): List<String?>?
    fun toList(arr: Array<String?>?): List<String?>?
}