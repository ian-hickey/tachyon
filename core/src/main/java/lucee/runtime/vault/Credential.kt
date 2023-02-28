package lucee.runtime.vault

import java.io.IOException

class Credential(key: String?, private val username: ByteArray?, private val password: ByteArray?) {
    private val key: String? = null
    @Throws(InvalidKeyException::class, UnsupportedEncodingException::class, NoSuchAlgorithmException::class, NoSuchPaddingException::class, IllegalBlockSizeException::class, BadPaddingException::class, CoderException::class)
    fun getUsername(decryptKey: PublicKey?): String? {
        validate()
        return String(RSA.decrypt(username, decryptKey, 0), Cryptor.DEFAULT_CHARSET)
    }

    @Throws(InvalidKeyException::class, UnsupportedEncodingException::class, NoSuchAlgorithmException::class, NoSuchPaddingException::class, IllegalBlockSizeException::class, BadPaddingException::class, CoderException::class)
    fun getPassword(decryptKey: PublicKey?): String? {
        validate()
        return String(RSA.decrypt(password, decryptKey, 0), Cryptor.DEFAULT_CHARSET)
    }

    fun getKey(): String? {
        return key
    }

    @Override
    override fun toString(): String {
        return "credential:$key"
    }

    companion object {
        fun validate() {
            val caller: StackTraceElement = Caller.caller(5)
            if (!caller.getClassName().startsWith("lucee.runtime.")) {
                val util: Excepton = CFMLEngineFactory.getInstance().getExceptionUtil()
                util.createPageRuntimeException(util.createApplicationException("You cannot access the credentials info from your context"))
            }
        }
    }
}