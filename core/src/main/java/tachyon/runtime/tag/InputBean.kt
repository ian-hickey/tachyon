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
package tachyon.runtime.tag

import tachyon.commons.lang.StringUtil

/**
 *
 */
class InputBean {
    /**
     * @return Returns the type.
     */
    /**
     * @param type The type to set.
     */
    var type: Short = Input.TYPE_TEXT
    /**
     * @return Returns the validate.
     */
    /**
     * @param validate The validate to set.
     */
    var validate: Short = Input.VALIDATE_NONE
    /**
     * @return Returns the name.
     */
    /**
     * @param name The name to set.
     */
    var name: String? = null
    /**
     * @return Returns the required.
     */
    /**
     * @param required The required to set.
     */
    var isRequired = false
    /**
     * @return Returns the onValidate.
     */
    /**
     * @param onValidate The onValidate to set.
     */
    var onValidate: String? = null
    /**
     * @return Returns the onError.
     */
    /**
     * @param onError The onError to set.
     */
    var onError: String? = null
    /**
     * @return Returns the pattern.
     */// '
    // "
    /**
     * @param pattern The pattern to set.
     * @throws ExpressionException
     */
    @set:Throws(ExpressionException::class)
    var pattern: String? = null
        set(pattern) {
            // '
            var pattern = pattern
            if (StringUtil.startsWith(pattern, '\'')) {
                if (!StringUtil.endsWith(pattern, '\'')) throw ExpressionException("invalid pattern definition [$pattern, missing closing [']")
                pattern = pattern.substring(1, pattern!!.length() - 1)
            }
            // "
            if (StringUtil.startsWith(pattern, '"')) {
                if (!StringUtil.endsWith(pattern, '"')) throw ExpressionException("invalid pattern definition [$pattern, missing closing [\"]")
                pattern = pattern.substring(1, pattern!!.length() - 1)
            }
            if (!StringUtil.startsWith(pattern, '/')) pattern = "/".concat(pattern)
            if (!StringUtil.endsWith(pattern, '/')) pattern = pattern.concat("/")
            field = pattern
        }
    /**
     * @return Returns the range_min.
     */
    /**
     * @param range_min The range_min to set.
     */
    // private String passThrough;
    var rangeMin = Double.NaN
    /**
     * @return Returns the range_max.
     */
    /**
     * @param range_max The range_max to set.
     */
    var rangeMax = Double.NaN
    /**
     * @return Returns the message.
     */
    /**
     * @param message The message to set.
     */
    var message: String? = null
    var maxLength = -1
}