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
 * Implements the CFML Function hash
 */
package lucee.runtime.functions.string

import java.security.MessageDigest

object Hash40 : Function {
    private const val serialVersionUID = 937180000352201249L

    // function for old code in ra files calling this function
    @Throws(PageException::class)
    fun call(pc: PageContext?, input: String?): String? {
        return invoke(pc.getConfig(), input, null, null, 1)
    }

    @Throws(PageException::class)
    fun call(pc: PageContext?, input: String?, algorithm: String?): String? {
        return invoke(pc.getConfig(), input, algorithm, null, 1)
    }

    @Throws(PageException::class)
    fun call(pc: PageContext?, input: String?, algorithm: String?, encoding: String?): String? {
        return invoke(pc.getConfig(), input, algorithm, encoding, 1)
    }

    //////
    @Throws(PageException::class)
    fun call(pc: PageContext?, input: Object?): String? {
        return invoke(pc.getConfig(), input, null, null, 1)
    }

    @Throws(PageException::class)
    fun call(pc: PageContext?, input: Object?, algorithm: String?): String? {
        return invoke(pc.getConfig(), input, algorithm, null, 1)
    }

    @Throws(PageException::class)
    fun call(pc: PageContext?, input: Object?, algorithm: String?, encoding: String?): String? {
        return invoke(pc.getConfig(), input, algorithm, encoding, 1)
    }

    @Throws(PageException::class)
    fun call(pc: PageContext?, input: Object?, algorithm: String?, encoding: String?, numIterations: Double): String? {
        return invoke(pc.getConfig(), input, algorithm, encoding, numIterations.toInt())
    }

    @Throws(PageException::class)
    operator fun invoke(config: Config?, input: Object?, algorithm: String?, encoding: String?, numIterations: Int): String? {
        var algorithm = algorithm
        var encoding = encoding
        algorithm = if (StringUtil.isEmpty(algorithm)) "md5" else algorithm.trim().toLowerCase()
        if (StringUtil.isEmpty(encoding)) encoding = config.getWebCharset().name()
        val isDefaultAlgo = numIterations == 1 && ("md5".equals(algorithm) || "cfmx_compat".equals(algorithm))
        var arrBytes: ByteArray? = null
        return try {
            if (input is ByteArray) {
                arrBytes = input
                if (isDefaultAlgo) return MD5Legacy.getDigestAsString(arrBytes).toUpperCase()
            } else {
                val string: String = Caster.toString(input)
                if (isDefaultAlgo) return MD5Legacy.getDigestAsString(string).toUpperCase()
                arrBytes = string.getBytes(encoding)
            }
            val md: MessageDigest = MessageDigest.getInstance(algorithm)
            md.reset()
            for (i in 0 until numIterations) md.update(arrBytes)
            MD5Legacy.stringify(md.digest()).toUpperCase()
        } catch (t: Throwable) {
            ExceptionUtil.rethrowIfNecessary(t)
            throw Caster.toPageException(t)
        }
    }
}