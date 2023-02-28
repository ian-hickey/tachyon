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
package tachyon.commons.lang

import tachyon.runtime.op.Caster

object ByteSizeParser {
    private const val B: Long = 1
    private const val KB: Long = 1024
    private const val MB = KB * 1024
    private const val GB = MB * 1024
    private const val TB = GB * 1024
    fun parseByteSizeDefinition(value: String, defaultValue: Long): Long {
        var value = value
        value = value.trim().toLowerCase()
        var factor = B
        var num = value
        if (value.endsWith("kb")) {
            factor = KB
            num = value.substring(0, value.length() - 2).trim()
        } else if (value.endsWith("k")) {
            factor = KB
            num = value.substring(0, value.length() - 1).trim()
        } else if (value.endsWith("mb")) {
            factor = MB
            num = value.substring(0, value.length() - 2).trim()
        } else if (value.endsWith("m")) {
            factor = MB
            num = value.substring(0, value.length() - 1).trim()
        } else if (value.endsWith("gb")) {
            factor = GB
            num = value.substring(0, value.length() - 2).trim()
        } else if (value.endsWith("g")) {
            factor = GB
            num = value.substring(0, value.length() - 1).trim()
        } else if (value.endsWith("tb")) {
            factor = TB
            num = value.substring(0, value.length() - 2).trim()
        } else if (value.endsWith("t")) {
            factor = TB
            num = value.substring(0, value.length() - 1).trim()
        } else if (value.endsWith("b")) {
            factor = B
            num = value.substring(0, value.length() - 1).trim()
        }
        val tmp: Long = Caster.toLongValue(num, Long.MIN_VALUE)
        return if (tmp == Long.MIN_VALUE) defaultValue else tmp * factor
    }
}