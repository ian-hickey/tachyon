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
package tachyon.runtime.functions.string

import java.security.MessageDigest

object Hash : Function {
    private const val serialVersionUID = 1161445102079248547L

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
        var numIterations = numIterations
        if (numIterations < 1) numIterations = 1
        algorithm = if (StringUtil.isEmpty(algorithm)) "md5" else algorithm.trim().toLowerCase()
        if ("cfmx_compat".equals(algorithm)) algorithm = "md5" else if ("quick".equals(algorithm)) {
            if (numIterations > 1) throw ExpressionException("for algorithm [quick], argument [numIterations] makes no sense, because this algorithm has no security in mind")
            return HashUtil.create64BitHashAsString(Caster.toString(input), 16)
        }
        if (StringUtil.isEmpty(encoding)) encoding = config.getWebCharset().name()
        var data: ByteArray? = null
        return try {
            data = if (input is ByteArray) input else Caster.toString(input).getBytes(encoding)
            val md: MessageDigest = MessageDigest.getInstance(algorithm)
            md.reset()
            for (i in 0 until numIterations) {
                data = md.digest(data)
            }
            tachyon.commons.digest.Hash.toHexString(data, true)
        } catch (t: Throwable) {
            ExceptionUtil.rethrowIfNecessary(t)
            throw Caster.toPageException(t)
        }
    }
}