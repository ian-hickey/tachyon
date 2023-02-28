package tachyon.runtime.vault

import java.io.IOException

object CredentialFactory {
    private var kp: KeyPair? = null
    @Throws(InvalidKeyException::class, NoSuchAlgorithmException::class, NoSuchPaddingException::class, IllegalBlockSizeException::class, BadPaddingException::class, InvalidKeySpecException::class, IOException::class)
    fun getCredential(key: String?, username: String?, password: String?): Credential? {
        val usr: ByteArray = RSA.encrypt(username, kp.getPrivate())
        val pw: ByteArray = RSA.encrypt(password, kp.getPrivate())
        return Credential(key, usr, pw)
    }

    @Throws(InvalidKeyException::class, UnsupportedEncodingException::class, NoSuchAlgorithmException::class, NoSuchPaddingException::class, IllegalBlockSizeException::class, BadPaddingException::class, CoderException::class)
    fun getUsername(c: Credential?): String? {
        return c!!.getUsername(kp.getPublic())
    }

    @Throws(InvalidKeyException::class, UnsupportedEncodingException::class, NoSuchAlgorithmException::class, NoSuchPaddingException::class, IllegalBlockSizeException::class, BadPaddingException::class, CoderException::class)
    fun getPassword(c: Credential?): String? {
        return c!!.getPassword(kp.getPublic())
    }

    init {
        try {
            kp = RSA.createKeyPair()
        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
        }
    }
}