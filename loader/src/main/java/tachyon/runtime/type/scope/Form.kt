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
package tachyon.runtime.type.scope

import java.io.UnsupportedEncodingException

/**
 * interface fro scope form
 */
interface Form : Scope {
    /**
     * @return Returns the encoding.
     */
    val encoding: String?

    /**
     * @param ac current ApplicationContext
     * @param encoding The encoding to set.
     * @throws UnsupportedEncodingException Unsupported Encoding Exception
     */
    @Throws(UnsupportedEncodingException::class)
    fun setEncoding(ac: ApplicationContext?, encoding: String?)

    /**
     * @return return the exception when initialized
     */
    val initException: PageException?
    fun setScriptProtecting(ac: ApplicationContext?, b: Boolean)
    fun getUploadResource(key: String?): FormItem?
    val fileItems: Array<tachyon.runtime.type.scope.FormItem?>?
    val inputStream: ServletInputStream?
    fun reinitialize(ac: ApplicationContext?)
}