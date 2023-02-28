package tachyon.runtime.coder

import java.io.IOException

class RSA(privateKeyToEncrypt: PrivateKey?, publicKeyToDecrypt: PublicKey?) {
    private var encCipher: Cipher? = null
    private var decCipher: Cipher? = null
    @Throws(NoSuchAlgorithmException::class, NoSuchPaddingException::class, InvalidKeyException::class, IllegalBlockSizeException::class, BadPaddingException::class)
    fun encrypt(data: ByteArray?): ByteArray? {
        if (encCipher == null) throw RuntimeException("Cipher is not initialized!")
        val max = KEY_SIZE / 8 - 11

        // we need to split in pieces, because RSA cannot handle pieces bigger than the key size
        val list: List<ByteArray?> = ArrayList<ByteArray?>()
        var offset = 0
        val len = data!!.size
        var l: Int
        var total = 0
        var part: ByteArray
        while (offset < len) {
            l = if (len - offset < max) len - offset else max
            part = encCipher.doFinal(data, offset, l)
            total += part.size
            list.add(part)
            offset += l
        }

        // now we merge to one piece
        val bytes = ByteArray(total)
        val it = list.iterator()
        var count = 0
        while (it.hasNext()) {
            part = it.next()
            for (i in part.indices) {
                bytes[count++] = part[i]
            }
        }
        return bytes
    }

    @Throws(NoSuchAlgorithmException::class, NoSuchPaddingException::class, InvalidKeyException::class, IllegalBlockSizeException::class, BadPaddingException::class)
    fun decrypt(data: ByteArray?, offset: Int): ByteArray? {
        if (decCipher == null) throw RuntimeException("Cipher is not initialized!")
        val max = KEY_SIZE / 8

        // we need to split in pieces, because RSA cannot handle pieces bigger than the key size
        val list: List<ByteArray?> = ArrayList<ByteArray?>()
        var off = offset
        val len = data!!.size
        var l: Int
        var total = 0
        var part: ByteArray
        while (off < len) {
            l = if (len - off < max) len - off else max
            part = decCipher.doFinal(data, off, l)
            total += part.size
            list.add(part)
            off += l
        }

        // now we merge to one piece
        val bytes = ByteArray(total)
        val it = list.iterator()
        var count = 0
        while (it.hasNext()) {
            part = it.next()
            for (i in part.indices) {
                bytes[count++] = part[i]
            }
        }
        return bytes
    } /*
	 * public static void main(String[] args) throws Exception { KeyPair kp = createKeyPair(); String
	 * str = ""; for (int i = 0; i <= 1700; i++) {
	 * 
	 * str += i + "Hello there how are you?\n"; } byte[] bytes = str.getBytes();
	 * 
	 * long start = System.currentTimeMillis(); byte[] enc = encrypt(bytes, kp.getPrivate()); byte[]
	 * encPlus = new byte[enc.length + 2];
	 * 
	 * for (int i = 0; i < enc.length; i++) { encPlus[i + 2] = enc[i]; }
	 * 
	 * System. out.println(enc.length); byte[] dec = decrypt(encPlus, kp.getPublic(), 2); System
	 * .out.println(new String(dec)); System .out.println("->" + (System.currentTimeMillis() - start));
	 * }
	 */

    companion object {
        private const val KEY_SIZE = 1024
        fun toString(privateKey: PrivateKey?): String? {
            val pkcs8EncodedKeySpec = PKCS8EncodedKeySpec(privateKey.getEncoded())
            return toString(pkcs8EncodedKeySpec.getEncoded())
        }

        fun toString(publicKey: PublicKey?): String? {
            val x509EncodedKeySpec = X509EncodedKeySpec(publicKey.getEncoded())
            return toString(x509EncodedKeySpec.getEncoded())
        }

        @Throws(IOException::class, NoSuchAlgorithmException::class, InvalidKeySpecException::class)
        fun toPrivateKey(privateKey: String?): PrivateKey? {
            val bytes = toBytes(privateKey)
            val keyFactory: KeyFactory = KeyFactory.getInstance("RSA")
            val privateKeySpec = PKCS8EncodedKeySpec(bytes)
            return keyFactory.generatePrivate(privateKeySpec)
        }

        @Throws(IOException::class, NoSuchAlgorithmException::class, InvalidKeySpecException::class)
        fun toPublicKey(publicKey: String?): PublicKey? {
            val bytes = toBytes(publicKey)
            val keyFactory: KeyFactory = KeyFactory.getInstance("RSA")
            val publicKeySpec = X509EncodedKeySpec(bytes)
            return keyFactory.generatePublic(publicKeySpec)
        }

        private fun toString(barr: ByteArray?): String? {
            return Base64Coder.encode(barr)
        }

        @Throws(CoderException::class)
        private fun toBytes(str: String?): ByteArray? {
            return Base64Coder.decode(str, true)
        }

        @Throws(NoSuchAlgorithmException::class)
        fun createKeyPair(): KeyPair? {
            val kpg: KeyPairGenerator = KeyPairGenerator.getInstance("RSA")
            kpg.initialize(KEY_SIZE)
            return kpg.genKeyPair()
        }

        @Throws(NoSuchAlgorithmException::class, NoSuchPaddingException::class, InvalidKeyException::class, IllegalBlockSizeException::class, BadPaddingException::class)
        private fun encrypt(data: ByteArray?, privateKeyToEncrypt: PrivateKey?): ByteArray? {
            return RSA(privateKeyToEncrypt, null).encrypt(data)
        }

        @Throws(NoSuchAlgorithmException::class, NoSuchPaddingException::class, InvalidKeyException::class, IllegalBlockSizeException::class, BadPaddingException::class)
        private fun decrypt(data: ByteArray?, publicKeyToDecrypt: PublicKey?, offset: Int): ByteArray? {
            return RSA(null, publicKeyToDecrypt).decrypt(data, offset)
        }
    }

    init {
        if (privateKeyToEncrypt != null) {
            encCipher = Cipher.getInstance("RSA")
            encCipher.init(Cipher.ENCRYPT_MODE, privateKeyToEncrypt)
        }
        if (publicKeyToDecrypt != null) {
            decCipher = Cipher.getInstance("RSA")
            decCipher.init(Cipher.DECRYPT_MODE, publicKeyToDecrypt)
        }
    }
}