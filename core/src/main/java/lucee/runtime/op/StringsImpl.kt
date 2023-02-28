/**
 * Copyright (c) 2015, Lucee Assosication Switzerland. All rights reserved.
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

import lucee.commons.digest.HashUtil

class StringsImpl : Strings {
    @Override
    fun replace(input: String?, find: String?, repl: String?, firstOnly: Boolean, ignoreCase: Boolean): String? {
        return StringUtil.replace(input, find, repl, firstOnly, ignoreCase)
    }

    @Override
    fun toVariableName(str: String?, addIdentityNumber: Boolean, allowDot: Boolean): String? {
        return StringUtil.toVariableName(str, addIdentityNumber, allowDot)
    }

    @Override
    fun first(list: String?, delimiter: String?, ignoreEmpty: Boolean): String? {
        return ListUtil.first(list, delimiter, ignoreEmpty)
    }

    @Override
    fun last(list: String?, delimiter: String?, ignoreEmpty: Boolean): String? {
        return ListUtil.last(list, delimiter, ignoreEmpty)
    }

    @Override
    fun removeQuotes(str: String?, trim: Boolean): String? {
        return StringUtil.removeQuotes(str, trim)
    }

    @Override
    fun create64BitHash(cs: CharSequence?): Long {
        return HashUtil.create64BitHash(cs!!)
    }

    @Override
    fun isEmpty(str: String?): Boolean {
        return StringUtil.isEmpty(str)
    }

    @Override
    fun isEmpty(str: String?, trim: Boolean): Boolean {
        return StringUtil.isEmpty(str, trim)
    }

    @Override
    fun emptyIfNull(str: String?): String? {
        return StringUtil.emptyIfNull(str)
    }

    @Override
    fun startsWithIgnoreCase(haystack: String?, needle: String?): Boolean {
        return StringUtil.startsWithIgnoreCase(haystack, needle)
    }

    @Override
    fun endsWithIgnoreCase(haystack: String?, needle: String?): Boolean {
        return StringUtil.endsWithIgnoreCase(haystack, needle)
    }

    @Override
    fun ucFirst(str: String?): String? {
        return StringUtil.ucFirst(str)
    }

    companion object {
        private var singelton: Strings? = null
        val instance: Strings?
            get() {
                if (singelton == null) singelton = StringsImpl()
                return singelton
            }
    }
}