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
package tachyon.runtime.functions.international

import tachyon.runtime.PageContext

/**
 * Implements the CFML Function getEncoding
 */
object GetEncoding : Function {
    @Throws(FunctionException::class)
    fun call(pc: PageContext?, scope: String?): String? {
        var scope = scope
        scope = scope.trim().toLowerCase()
        if (scope.equals("url")) return pc.urlScope().getEncoding()
        if (scope.equals("form")) return pc.formScope().getEncoding()
        throw FunctionException(pc, "getEncoding", 1, "scope", "scope must have the one of the following values [url,form] not [$scope]")
    }
}