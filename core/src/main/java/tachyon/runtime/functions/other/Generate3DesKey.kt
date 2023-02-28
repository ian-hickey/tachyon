package tachyon.runtime.functions.other

import javax.crypto.spec.SecretKeySpec

/**
 * Generates a 3DES key
 */
object Generate3DesKey : Function {
    @Throws(PageException::class)
    fun call(pc: PageContext?): String? {
        return GenerateSecretKey.call(pc, "DESede")
    }

    @Throws(PageException::class)
    fun call(pc: PageContext?, input: String?): String? {
        val keySpec = SecretKeySpec(input.getBytes(), "DESede")
        return Base64Coder.encode(keySpec.getEncoded())
    }
}