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
package lucee.runtime.exp

import java.net.URL

/**
 * Exception class for the HTTP Handling
 */
class HTTPException(message: String?, detail: String?,
                    /**
                     * @return Returns the statusCode.
                     */
                    val statusCode: Int,
                    /**
                     * @return Returns the status text.
                     */
                    val statusText: String?, url: URL?) : ApplicationException(message, detail) {
    private val url: URL?
    val uRL: URL?
        get() = url

    @Override
    override fun getCatchBlock(config: Config?): CatchBlock? {
        val sct: CatchBlock = super.getCatchBlock(config)
        sct.setEL("statusCode", statusCode.toString() + "")
        sct.setEL("statusText", statusText)
        if (url != null) sct.setEL("url", url.toExternalForm())
        return sct
    }

    /**
     * Constructor of the class
     *
     * @param message
     * @param detail
     * @param statusCode
     */
    init {
        this.url = url
        setAdditional(KeyConstants._statuscode, Double.valueOf(statusCode))
        setAdditional(KeyConstants._statustext, statusText)
        if (url != null) setAdditional(KeyConstants._url, url.toExternalForm())
    }
}