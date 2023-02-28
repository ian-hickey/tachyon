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

import java.io.UnsupportedEncodingException

/**
 * Name Value Pair
 */
class ByteNameValuePair
/**
 * constructor of the class
 *
 * @param name
 * @param value
 */(
        /**
         * @param name The name to set.
         */
        var name: ByteArray,
        /**
         * @param value The value to set.
         */
        var value: ByteArray,
        /**
         * @return the urlEncoded
         */
        val isUrlEncoded: Boolean) {
    /**
     * @return Returns the name.
     */
    /**
     * @return Returns the value.
     */

    /**
     * @param encoding
     * @return Returns the name.
     * @throws UnsupportedEncodingException
     */
    @Throws(UnsupportedEncodingException::class)
    fun getName(encoding: String?): String {
        return String(name, encoding)
    }

    /**
     * @param encoding
     * @param defaultValue
     * @return Returns the name.
     */
    fun getName(encoding: String?, defaultValue: String): String {
        return try {
            String(name, encoding)
        } catch (e: UnsupportedEncodingException) {
            defaultValue
        }
    }

    /**
     * @param encoding
     * @return Returns the name.
     * @throws UnsupportedEncodingException
     */
    @Throws(UnsupportedEncodingException::class)
    fun getValue(encoding: String?): String {
        return String(value, encoding)
    }

    /**
     * @param encoding
     * @param defaultValue
     * @return Returns the name.
     */
    fun getValue(encoding: String?, defaultValue: String): String {
        return try {
            String(value, encoding)
        } catch (e: UnsupportedEncodingException) {
            defaultValue
        }
    }
}