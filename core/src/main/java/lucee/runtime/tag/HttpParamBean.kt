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
package lucee.runtime.tag

import lucee.commons.io.res.Resource

/**
 *
 */
class HttpParamBean {
    /** Specifies the value of the URL, FormField, Cookie, File, or CGI variable being passed.  */
    private var value: Object? = null
    /**
     * @return Returns the type.
     */
    /**
     * set the value type The transaction type.
     *
     * @param type value to set
     */
    /** The transaction type.  */
    var type = TYPE_URL
    /**
     * @return Returns the file.
     */
    /**
     * set the value file Required for type = "File".
     *
     * @param file value to set
     */
    /** Required for type = "File".  */
    var file: Resource? = null
    /**
     * @return Returns the name.
     */
    /**
     * set the value name A variable name for the data being passed.
     *
     * @param name value to set
     */
    /** A variable name for the data being passed.  */
    var name: String? = null
    /**
     * Returns the value of encoded.
     *
     * @return value encoded
     */
    /**
     * sets the encoded value.
     *
     * @param encoded The encoded to set.
     */
    var encoded: Short = Http.ENCODED_AUTO
    /**
     * Returns the value of mimeType.
     *
     * @return value mimeType
     */
    /**
     * sets the mimeType value.
     *
     * @param mimeType The mimeType to set.
     */
    var mimeType: String? = ""

    /**
     * set the value value Specifies the value of the URL, FormField, Cookie, File, or CGI variable
     * being passed.
     *
     * @param value value to set
     */
    fun setValue(value: Object?) {
        this.value = value
    }

    /**
     * @return Returns the value.
     * @throws PageException
     */
    @get:Throws(PageException::class)
    val valueAsString: String?
        get() = Caster.toString(value)

    /**
     * @return Returns the value.
     */
    fun getValue(): Object? {
        return value
    }

    companion object {
        const val TYPE_URL = 1
        const val TYPE_FORM = 2
        const val TYPE_CGI = 3
        const val TYPE_HEADER = 4
        const val TYPE_COOKIE = 5
        const val TYPE_FILE = 6
        const val TYPE_XML = 7
        const val TYPE_BODY = 8
    }
}