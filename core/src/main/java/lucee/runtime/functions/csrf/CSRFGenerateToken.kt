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
package lucee.runtime.functions.csrf

import lucee.runtime.PageContext

object CSRFGenerateToken : Function {
    private const val serialVersionUID = -2411153524245619987L
    @Throws(PageException::class)
    fun call(pc: PageContext?): String? {
        return call(pc, null, false)
    }

    @Throws(PageException::class)
    fun call(pc: PageContext?, key: String?): String? {
        return call(pc, key, false)
    }

    @Throws(PageException::class)
    fun call(pc: PageContext?, key: String?, forceNew: Boolean): String? {
        return getStorageScope(pc).generateToken(key, forceNew)
    }

    @Throws(PageException::class)
    fun getStorageScope(pc: PageContext?): CSRFTokenSupport? {
        val session: Session = pc.sessionScope() as? CSRFTokenSupport
                ?: throw ExpressionException("Session scope does not support CSRF Tokens")
        return session as CSRFTokenSupport
    }
}