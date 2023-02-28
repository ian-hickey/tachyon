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

class ListUtilImpl : ListUtil {
    @Override
    fun listWithQuotesToArray(list: String?, delimiter: String?, quotes: String?): Array? {
        return tachyon.runtime.type.util.ListUtil.listWithQuotesToArray(list, delimiter, quotes)
    }

    @Override
    fun toArray(list: String?, delimiter: String?): Array? {
        return tachyon.runtime.type.util.ListUtil.listToArray(list, delimiter)
    }

    @Override
    fun toArray(list: String?, delimiter: String?, includeEmptyFields: Boolean, multiCharDelim: Boolean): Array? {
        return tachyon.runtime.type.util.ListUtil.listToArray(list, delimiter, includeEmptyFields, multiCharDelim)
    }

    @Override
    fun toArrayRemoveEmpty(list: String?, delimiter: String?): Array? {
        return tachyon.runtime.type.util.ListUtil.listToArrayRemoveEmpty(list, delimiter)
    }

    @Override
    fun toListRemoveEmpty(list: String?, delimiter: Char): List<String?>? {
        return tachyon.runtime.type.util.ListUtil.toListRemoveEmpty(list, delimiter)
    }

    @Override
    fun toArrayTrim(list: String?, delimiter: String?): Array? {
        return tachyon.runtime.type.util.ListUtil.listToArrayTrim(list, delimiter)
    }

    @Override
    @Throws(PageException::class)
    fun insertAt(list: String?, pos: Int, value: String?, delimiter: String?, ignoreEmpty: Boolean): String? {
        return tachyon.runtime.type.util.ListUtil.listInsertAt(list, pos, value, delimiter, ignoreEmpty)
    }

    @Override
    fun findNoCase(list: String?, value: String?, delimiter: String?): Int {
        return tachyon.runtime.type.util.ListUtil.listFindNoCase(list, value, delimiter)
    }

    @Override
    fun findNoCase(list: String?, value: String?, delimiter: String?, trim: Boolean): Int {
        return tachyon.runtime.type.util.ListUtil.listFindNoCase(list, value, delimiter)
    }

    @Override
    fun findForSwitch(list: String?, value: String?, delimiter: String?): Int {
        return tachyon.runtime.type.util.ListUtil.listFindForSwitch(list, value, delimiter)
    }

    @Override
    fun findNoCaseIgnoreEmpty(list: String?, value: String?, delimiter: String?): Int {
        return tachyon.runtime.type.util.ListUtil.listFindNoCaseIgnoreEmpty(list, value, delimiter)
    }

    @Override
    fun findNoCaseIgnoreEmpty(list: String?, value: String?, delimiter: Char): Int {
        return tachyon.runtime.type.util.ListUtil.listFindNoCaseIgnoreEmpty(list, value, delimiter)
    }

    @Override
    fun find(list: String?, value: String?): Int {
        return tachyon.runtime.type.util.ListUtil.listFind(list, value)
    }

    @Override
    fun find(list: String?, value: String?, delimiter: String?): Int {
        return tachyon.runtime.type.util.ListUtil.listFind(list, value, delimiter)
    }

    @Override
    fun findIgnoreEmpty(list: String?, value: String?, delimiter: String?): Int {
        return tachyon.runtime.type.util.ListUtil.listFindIgnoreEmpty(list, value, delimiter)
    }

    @Override
    fun findIgnoreEmpty(list: String?, value: String?, delimiter: Char): Int {
        return tachyon.runtime.type.util.ListUtil.listFindIgnoreEmpty(list, value, delimiter)
    }

    @Override
    fun containsNoCase(list: String?, value: String?, delimiter: String?, includeEmptyFields: Boolean, multiCharacterDelimiter: Boolean): Int {
        return tachyon.runtime.type.util.ListUtil.listContainsNoCase(list, value, delimiter, includeEmptyFields, multiCharacterDelimiter)
    }

    @Override
    fun contains(list: String?, value: String?, delimiter: String?, includeEmptyFields: Boolean, multiCharacterDelimiter: Boolean): Int {
        return tachyon.runtime.type.util.ListUtil.listContains(list, value, delimiter, includeEmptyFields, multiCharacterDelimiter)
    }

    @Override
    fun toListTrim(array: Array<String?>?, delimiter: String?): String? {
        return tachyon.runtime.type.util.ListUtil.arrayToListTrim(array, delimiter)
    }

    @Override
    fun toList(array: Array<String?>?, delimiter: String?): String? {
        // TODO Auto-generated method stub
        return tachyon.runtime.type.util.ListUtil.arrayToList(array, delimiter)
    }

    @Override
    fun toList(array: Array<Key?>?, delimiter: String?): String? {
        return tachyon.runtime.type.util.ListUtil.arrayToList(array, delimiter)
    }

    @Override
    @Throws(PageException::class)
    fun toList(array: Array?, delimiter: String?): String? {
        return tachyon.runtime.type.util.ListUtil.arrayToList(array, delimiter)
    }

    @Override
    @Throws(PageException::class)
    fun toList(list: List<*>?, delimiter: String?): String? {
        return tachyon.runtime.type.util.ListUtil.listToList(list, delimiter)
    }

    @Override
    fun toListEL(list: List<String?>?, delimiter: String?): String? {
        return tachyon.runtime.type.util.ListUtil.listToListEL(list, delimiter)
    }

    @Override
    fun trim(array: Array<String?>?): Array<String?>? {
        return tachyon.runtime.type.util.ListUtil.trim(array)
    }

    @Override
    fun trim(list: String?, delimiter: String?, multiCharacterDelimiter: Boolean): String? {
        return tachyon.runtime.type.util.ListUtil.trim(list, delimiter, multiCharacterDelimiter)
    }

    @Override
    @Throws(PageException::class)
    fun sortIgnoreEmpty(list: String?, sortType: String?, sortOrder: String?, delimiter: String?): String? {
        return tachyon.runtime.type.util.ListUtil.sortIgnoreEmpty(list, sortType, sortOrder, delimiter)
    }

    @Override
    @Throws(PageException::class)
    fun sort(list: String?, sortType: String?, sortOrder: String?, delimiter: String?): String? {
        return tachyon.runtime.type.util.ListUtil.sort(list, sortType, sortOrder, delimiter)
    }

    @Override
    @Throws(PageException::class)
    fun toStringArray(array: Array?): Array<String?>? {
        return tachyon.runtime.type.util.ListUtil.toStringArray(array)
    }

    @Override
    fun toStringArray(set: Set<String?>?): Array<String?>? {
        return tachyon.runtime.type.util.ListUtil.toStringArray(set)
    }

    @Override
    fun toStringArray(list: List<String?>?): Array<String?>? {
        return tachyon.runtime.type.util.ListUtil.toStringArray(list)
    }

    @Override
    fun toStringArray(array: Array?, defaultValue: String?): Array<String?>? {
        return tachyon.runtime.type.util.ListUtil.toStringArray(array, defaultValue)
    }

    @Override
    @Throws(PageException::class)
    fun toStringArrayTrim(array: Array?): Array<String?>? {
        return tachyon.runtime.type.util.ListUtil.toStringArrayTrim(array)
    }

    @Override
    fun last(list: String?, delimiter: String?, ignoreEmpty: Boolean): String? {
        return tachyon.runtime.type.util.ListUtil.last(list, delimiter, ignoreEmpty)
    }

    @Override
    fun len(list: String?, delimiter: String?, ignoreEmpty: Boolean): Int {
        return tachyon.runtime.type.util.ListUtil.len(list, delimiter, ignoreEmpty)
    }

    @Override
    fun getAt(list: String?, delimiter: String?, position: Int, ignoreEmpty: Boolean, defaultValue: String?): String? {
        return tachyon.runtime.type.util.ListUtil.getAt(list, delimiter, position, ignoreEmpty, defaultValue)
    }

    @Override
    fun toStringArray(list: String?, delimiter: String?): Array<String?>? {
        return tachyon.runtime.type.util.ListUtil.listToStringArray(list, delimiter)
    }

    @Override
    fun trimItems(arr: Array<String?>?): Array<String?>? {
        return tachyon.runtime.type.util.ListUtil.trimItems(arr)
    }

    @Override
    @Throws(PageException::class)
    fun trimItems(arr: Array?): Array? {
        return tachyon.runtime.type.util.ListUtil.trimItems(arr)
    }

    @Override
    fun toSet(list: String?, delimiter: String?, trim: Boolean): Set<String?>? {
        return tachyon.runtime.type.util.ListUtil.listToSet(list, delimiter, trim)
    }

    @Override
    fun toSet(arr: Array<String?>?): Set<String?>? {
        return tachyon.runtime.type.util.ListUtil.toSet(arr)
    }

    @Override
    fun first(list: String?, delimiters: String?, ignoreEmpty: Boolean, count: Int): String? {
        return tachyon.runtime.type.util.ListUtil.first(list, delimiters, ignoreEmpty, count)
    }

    @Override
    fun first(list: String?, delimiters: String?, ignoreEmpty: Boolean): String? {
        return tachyon.runtime.type.util.ListUtil.first(list, delimiters, ignoreEmpty)
    }

    @Override
    fun rest(list: String?, delimiters: String?, ignoreEmpty: Boolean, offset: Int): String? {
        return tachyon.runtime.type.util.ListUtil.rest(list, delimiters, ignoreEmpty, offset)
    }

    @Override
    fun rest(list: String?, delimiters: String?, ignoreEmpty: Boolean): String? {
        return tachyon.runtime.type.util.ListUtil.rest(list, delimiters, ignoreEmpty)
    }

    @Override
    fun getDelimIndex(list: String?, itemPos: Int, delims: CharArray?, ignoreEmpty: Boolean): Int {
        return tachyon.runtime.type.util.ListUtil.getDelimIndex(StringBuilder(list), itemPos, delims, ignoreEmpty)
    }

    @Override
    fun toList(set: Set<String?>?): List<String?>? {
        return tachyon.runtime.type.util.ListUtil.toList(set)
    }

    @Override
    fun toList(arr: Array<String?>?): List<String?>? {
        return tachyon.runtime.type.util.ListUtil.arrayToList(arr)
    }
}