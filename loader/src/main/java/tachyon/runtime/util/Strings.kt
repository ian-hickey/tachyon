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

import kotlin.Throws
import tachyon.loader.util.Util
import kotlin.jvm.Synchronized
import kotlin.jvm.Transient
import org.osgi.framework.Version

interface Strings {
    /**
     * performs a replace operation on a string
     *
     * @param input - the string input to work on
     * @param find - the substring to find
     * @param repl - the substring to replace the matches with
     * @param firstOnly - if true then only the first occurrence of `find` will be replaced
     * @param ignoreCase - if true then matches will not be case sensitive
     * @return Returns a string the the substring replaced.
     */
    fun replace(input: String?, find: String?, repl: String?, firstOnly: Boolean, ignoreCase: Boolean): String?
    fun toVariableName(str: String?, addIdentityNumber: Boolean, allowDot: Boolean): String?

    /**
     * return first element of the list
     *
     * @param list List
     * @param delimiter delimiter of the list
     * @param ignoreEmpty ignore empty
     * @return returns the first element of the list
     */
    fun first(list: String?, delimiter: String?, ignoreEmpty: Boolean): String?

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
     * removes quotes(",') that wraps the string
     *
     * @param string string
     * @param trim trim
     * @return Returns a string without wrapping quotes.
     */
    fun removeQuotes(string: String?, trim: Boolean): String?
    fun create64BitHash(cs: CharSequence?): Long
    fun isEmpty(str: String?): Boolean
    fun isEmpty(str: String?, trim: Boolean): Boolean
    fun emptyIfNull(str: String?): String?
    fun startsWithIgnoreCase(haystack: String?, needle: String?): Boolean
    fun endsWithIgnoreCase(haystack: String?, needle: String?): Boolean
    fun ucFirst(str: String?): String?
}