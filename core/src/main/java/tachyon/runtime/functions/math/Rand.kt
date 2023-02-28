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
 * Implements the CFML Function rand
 */
package tachyon.runtime.functions.math

import java.security.NoSuchAlgorithmException

object Rand : Function {
    private val randoms: Map<String?, Random?>? = HashMap<String?, Random?>()
    @Throws(ExpressionException::class)
    fun call(pc: PageContext?): Double {
        return getRandom(CFMXCompat.ALGORITHM_NAME, Double.NaN).nextDouble()
    }

    @Throws(ExpressionException::class)
    fun call(pc: PageContext?, algorithm: String?): Double {
        return getRandom(algorithm, Double.NaN).nextDouble()
    }

    @Throws(ExpressionException::class)
    fun getRandom(algorithm: String?, seed: Double?): Random? {
        var algorithm = algorithm
        algorithm = algorithm.toLowerCase()
        var result: Random? = randoms!![algorithm]
        if (result == null || !seed.isNaN()) {
            if (CFMXCompat.ALGORITHM_NAME.equalsIgnoreCase(algorithm)) {
                result = Random()
            } else {
                result = try {
                    SecureRandom.getInstance(algorithm)
                } catch (e: NoSuchAlgorithmException) {
                    throw ExpressionException("random algorithm [$algorithm] is not installed on the system", e.getMessage())
                }
            }
            if (!seed.isNaN()) result.setSeed(seed.longValue())
            randoms.put(algorithm, result)
        }
        return result
    }
}