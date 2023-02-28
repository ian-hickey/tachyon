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
package tachyon.runtime.osgi

import tachyon.commons.lang.StringUtil

class BundleVersion {
    private val major: Int
    private val minor: Int
    private val micro: Int
    private val qualifier: String?
    private var str: String? = null

    private constructor(str: String?) {
        val arr: Array<String?>
        arr = try {
            ListUtil.toStringArrayTrim(ListUtil.listToArray(str.trim(), '.'))
        } catch (e: PageException) {
            arrayOfNulls<String?>(0) // should not happen
        }
        if (arr.size == 0) {
            major = 0
            minor = 0
            micro = 0
            qualifier = null
        } else if (arr.size == 1) {
            major = Caster.toIntValue(arr[0], 0)
            minor = 0
            micro = 0
            qualifier = null
        } else if (arr.size == 2) {
            major = Caster.toIntValue(arr[0], 0)
            minor = Caster.toIntValue(arr[1], 0)
            micro = 0
            qualifier = null
        } else if (arr.size == 3) {
            major = Caster.toIntValue(arr[0], 0)
            minor = Caster.toIntValue(arr[1], 0)
            micro = Caster.toIntValue(arr[2], 0)
            qualifier = null
        } else {
            major = Caster.toIntValue(arr[0], 0)
            minor = Caster.toIntValue(arr[1], 0)
            micro = Caster.toIntValue(arr[2], 0)
            qualifier = arr[3]
        }
        val sb: StringBuilder = StringBuilder().append(major).append('.').append(minor).append('.').append(micro)
        if (qualifier != null) sb.append('.').append(qualifier)
        this.str = sb.toString()
    }

    private constructor() {
        major = 0
        minor = 0
        micro = 0
        qualifier = null
    }

    @Override
    override fun toString(): String {
        return str!!
    }

    @Override
    override fun equals(obj: Object?): Boolean {
        if (obj !is BundleVersion) return false
        val other = obj as BundleVersion?
        return if (major != other!!.major || minor != other.minor || micro != other.micro) false else qualifier?.equals(other.qualifier)
                ?: StringUtil.isEmpty(other.qualifier)
    }
}