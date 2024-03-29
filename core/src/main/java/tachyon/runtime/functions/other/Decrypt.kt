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
 * Implements the CFML Function decrypt
 */
package tachyon.runtime.functions.other

import tachyon.commons.digest.RSA

object Decrypt : Function {
    private const val serialVersionUID = 6501313271641463777L
    @Throws(PageException::class)
    fun call(pc: PageContext?, input: String?, key: String?): String? {
        return invoke(input, key, CFMXCompat.ALGORITHM_NAME, Cryptor.DEFAULT_ENCODING, null, 0, true)
    }

    @Throws(PageException::class)
    fun call(pc: PageContext?, input: String?, key: String?, algorithm: String?): String? {
        return invoke(input, key, algorithm, Cryptor.DEFAULT_ENCODING, null, 0, true)
    }

    @Throws(PageException::class)
    fun call(pc: PageContext?, input: String?, key: String?, algorithm: String?, encoding: String?): String? {
        return invoke(input, key, algorithm, encoding, null, 0, true)
    }

    @Throws(PageException::class)
    fun call(pc: PageContext?, input: String?, key: String?, algorithm: String?, encoding: String?, ivOrSalt: Object?): String? {
        return invoke(input, key, algorithm, encoding, ivOrSalt, 0, true)
    }

    @Throws(PageException::class)
    fun call(pc: PageContext?, input: String?, key: String?, algorithm: String?, encoding: String?, ivOrSalt: Object?, iterations: Double): String? {
        return invoke(input, key, algorithm, encoding, ivOrSalt, Caster.toInteger(iterations), true)
    }

    @Throws(PageException::class)
    fun call(pc: PageContext?, input: String?, key: String?, algorithm: String?, encoding: String?, ivOrSalt: Object?, iterations: Double, precise: Boolean): String? {
        return invoke(input, key, algorithm, encoding, ivOrSalt, Caster.toInteger(iterations), precise)
    }

    @Throws(PageException::class)
    operator fun invoke(input: String?, key: String?, algorithm: String?, encoding: String?, ivOrSalt: Object?, iterations: Int, precise: Boolean): String? {
        return try {
            if ("RSA".equalsIgnoreCase(algorithm)) {
                return String(RSA.decrypt(Coder.decode(encoding, input, false), RSA.toKey(key!!), 0), Cryptor.DEFAULT_CHARSET)
            } else if (CFMXCompat.isCfmxCompat(algorithm)) return String(invoke(Coder.decode(encoding, input, false), key, algorithm, null, 0, precise), Cryptor.DEFAULT_CHARSET)
            var baIVS: ByteArray? = null
            if (ivOrSalt is String) baIVS = (ivOrSalt as String?).getBytes(Cryptor.DEFAULT_CHARSET) else if (ivOrSalt != null) baIVS = Caster.toBinary(ivOrSalt)
            Cryptor.decrypt(input, key, algorithm, baIVS, iterations, encoding, Cryptor.DEFAULT_CHARSET, precise)
        } catch (t: Throwable) {
            ExceptionUtil.rethrowIfNecessary(t)
            throw Caster.toPageException(t)
        }
    }

    @Throws(PageException::class)
    operator fun invoke(input: ByteArray?, key: String?, algorithm: String?, ivOrSalt: ByteArray?, iterations: Int, precise: Boolean): ByteArray? {
        return if (CFMXCompat.isCfmxCompat(algorithm)) CFMXCompat().transformString(key, input) else Cryptor.decrypt(input, key, algorithm, ivOrSalt, iterations, precise)
    }
}