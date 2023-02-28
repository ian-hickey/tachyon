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
package lucee.commons.lang

import java.io.IOException

object KeyGenerator {
    @Throws(IOException::class)
    fun createKey(value: String): String {
        // create a crossfoot of the string and change result in constealltion of the position
        var sum: Long = 0
        for (i in value.length() - 1 downTo 0) {
            sum += value.charAt(i) * ((i % 3 + 1) / 2f)
        }
        return Md5.getDigestAsString(value).toString() + ":" + sum
    }

    @Throws(IOException::class)
    fun createVariable(value: String): String {
        // create a crossfoot of the string and change result in constealltion of the position
        var sum: Long = 0
        for (i in value.length() - 1 downTo 0) {
            sum += value.charAt(i) * ((i % 3 + 1) / 2f)
        }
        return "V" + Md5.getDigestAsString(value) + sum
    }
}