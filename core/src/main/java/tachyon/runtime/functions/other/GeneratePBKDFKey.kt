/**
 *
 * Copyright (c) 2015, Tachyon Assosication Switzerland
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
package tachyon.runtime.functions.other

import java.security.NoSuchAlgorithmException

class GeneratePBKDFKey : BIF() {
    @Override
    @Throws(PageException::class)
    operator fun invoke(pc: PageContext?, args: Array<Object?>?): Object? {
        if (args!!.size == 5) return call(pc, Caster.toString(args[0]), Caster.toString(args[1]), Caster.toString(args[2]), Caster.toDoubleValue(args[3]), Caster.toDoubleValue(args[4]))
        if (args.size == 4) return call(pc, Caster.toString(args[0]), Caster.toString(args[1]), Caster.toString(args[2]), Caster.toDoubleValue(args[3]))
        if (args.size == 3) return call(pc, Caster.toString(args[0]), Caster.toString(args[1]), Caster.toString(args[2]))
        throw FunctionException(pc, "GeneratePBKDFKey", 3, 5, args.size)
    }

    companion object {
        private const val serialVersionUID = -2558116913822203235L
        @Throws(PageException::class)
        fun call(pc: PageContext?, algorithm: String?, passPhrase: String?, salt: String?): String? {
            return call(pc, algorithm, passPhrase, salt, 4096.0, 128.0)
        }

        @Throws(PageException::class)
        fun call(pc: PageContext?, algorithm: String?, passPhrase: String?, salt: String?, iterations: Double): String? {
            return call(pc, algorithm, passPhrase, salt, iterations, 128.0)
        }

        @Throws(PageException::class)
        fun call(pc: PageContext?, algorithm: String?, passPhrase: String?, salt: String?, iterations: Double, keySize: Double): String? {
            // algo
            var algorithm = algorithm
            if (StringUtil.isEmpty(algorithm)) throw FunctionException(pc, "GeneratePBKDFKey", 1, "algorithm", "Argument [algorithm] is empty.")
            algorithm = algorithm.trim()
            if (!StringUtil.startsWithIgnoreCase(algorithm, "PBK")) throw FunctionException(pc, "GeneratePBKDFKey", 1, "algorithm", "Algorithm [$algorithm] is not supported.")

            // TODO add provider to support addional keys by addin a provider that is supporting it
            var key: SecretKeyFactory? = null
            key = try {
                SecretKeyFactory.getInstance(algorithm)
            } catch (e: NoSuchAlgorithmException) {
                if (!algorithm.equalsIgnoreCase("PBKDF2WithHmacSHA1")) throw FunctionException(pc, "GeneratePBKDFKey", 1, "algorithm", "The only supported algorithm is [PBKDF2WithHmacSHA1].") else throw Caster.toPageException(e)
            }
            return try {
                val spec = PBEKeySpec(passPhrase.toCharArray(), salt.getBytes(), iterations.toInt(), keySize.toInt())
                Base64Coder.encode(key.generateSecret(spec).getEncoded())
            } catch (ikse: InvalidKeySpecException) {
                throw Caster.toPageException(ikse)
            }
        }
    }
}