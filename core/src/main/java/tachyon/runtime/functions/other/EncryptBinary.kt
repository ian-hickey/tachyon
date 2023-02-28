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
/**
 * Implements the CFML Function encrypt
 */
package tachyon.runtime.functions.other

import tachyon.runtime.PageContext

object EncryptBinary : Function {
    private const val serialVersionUID = -6067145650968986379L
    @Throws(PageException::class)
    fun call(pc: PageContext?, oBytes: Object?, key: String?): Object? {
        return call(pc, oBytes, key, CFMXCompat.ALGORITHM_NAME, true)
    }

    @Throws(PageException::class)
    fun call(pc: PageContext?, oBytes: Object?, key: String?, algorithm: String?): Object? {
        return Encrypt.invoke(Caster.toBinary(oBytes), key, algorithm, null, 0, true)
    }

    @Throws(PageException::class)
    fun call(pc: PageContext?, oBytes: Object?, key: String?, algorithm: String?, ivOrSalt: Object?): Object? {
        return Encrypt.invoke(Caster.toBinary(oBytes), key, algorithm, Caster.toBinary(ivOrSalt), 0, true)
    }

    @Throws(PageException::class)
    fun call(pc: PageContext?, oBytes: Object?, key: String?, algorithm: String?, ivOrSalt: Object?, iterations: Double): Object? {
        return Encrypt.invoke(Caster.toBinary(oBytes), key, algorithm, Caster.toBinary(ivOrSalt), Caster.toInteger(iterations), true)
    }

    @Throws(PageException::class)
    fun call(pc: PageContext?, oBytes: Object?, key: String?, algorithm: String?, ivOrSalt: Object?, iterations: Double, precise: Boolean): Object? {
        return Encrypt.invoke(Caster.toBinary(oBytes), key, algorithm, Caster.toBinary(ivOrSalt), Caster.toInteger(iterations), precise)
    }
}