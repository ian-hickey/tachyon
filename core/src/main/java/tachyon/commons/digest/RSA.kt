package tachyon.commons.digest

import java.io.IOException

object RSA {
    private const val KEY_SIZE = 1024
    fun toString(privateKey: PrivateKey): String {
        val pkcs8EncodedKeySpec = PKCS8EncodedKeySpec(privateKey.getEncoded())
        return toString(pkcs8EncodedKeySpec.getEncoded())
    }

    fun toString(publicKey: PublicKey): String {
        val x509EncodedKeySpec = X509EncodedKeySpec(publicKey.getEncoded())
        return toString(x509EncodedKeySpec.getEncoded())
    }

    @Throws(IOException::class, NoSuchAlgorithmException::class, InvalidKeySpecException::class)
    fun toKey(key: String): Key {
        return try {
            toPrivateKey(key)
        } catch (ikse: InvalidKeySpecException) {
            toPublicKey(key)
        }
    }

    @Throws(IOException::class, NoSuchAlgorithmException::class, InvalidKeySpecException::class)
    fun toPrivateKey(privateKey: String): PrivateKey {
        val bytes = toBytes(privateKey)
        val keyFactory: KeyFactory = KeyFactory.getInstance("RSA")
        val privateKeySpec = PKCS8EncodedKeySpec(bytes)
        return keyFactory.generatePrivate(privateKeySpec)
    }

    @Throws(IOException::class, NoSuchAlgorithmException::class, InvalidKeySpecException::class)
    fun toPublicKey(publicKey: String): PublicKey {
        val bytes = toBytes(publicKey)
        val keyFactory: KeyFactory = KeyFactory.getInstance("RSA")
        val publicKeySpec = X509EncodedKeySpec(bytes)
        return keyFactory.generatePublic(publicKeySpec)
    }

    private fun toString(barr: ByteArray): String {
        return Base64Encoder.encode(barr)
    }

    @Throws(CoderException::class)
    private fun toBytes(str: String): ByteArray {
        return Base64Encoder.decode(str, true)
    }

    @Throws(NoSuchAlgorithmException::class)
    fun createKeyPair(): KeyPair {
        val kpg: KeyPairGenerator = KeyPairGenerator.getInstance("RSA")
        kpg.initialize(KEY_SIZE)
        return kpg.genKeyPair()
    }

    @Throws(NoSuchAlgorithmException::class, NoSuchPaddingException::class, InvalidKeyException::class, IllegalBlockSizeException::class, BadPaddingException::class, UnsupportedEncodingException::class)
    fun encrypt(data: String, key: Key?): ByteArray {
        return encrypt(data.getBytes(Cryptor.DEFAULT_CHARSET), key)
    }

    @Throws(NoSuchAlgorithmException::class, NoSuchPaddingException::class, InvalidKeyException::class, IllegalBlockSizeException::class, BadPaddingException::class)
    fun encrypt(data: ByteArray, key: Key?): ByteArray {
        val cipher: Cipher = Cipher.getInstance("RSA")
        cipher.init(Cipher.ENCRYPT_MODE, key)
        val max = KEY_SIZE / 8 - 11

        // we need to split in pieces, because RSA cannot handle pices bigger than the key size
        val list: List<ByteArray> = ArrayList<ByteArray>()
        var offset = 0
        val len = data.size
        var l: Int
        var total = 0
        var part: ByteArray
        while (offset < len) {
            l = if (len - offset < max) len - offset else max
            part = cipher.doFinal(data, offset, l)
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

    @Throws(NoSuchAlgorithmException::class, NoSuchPaddingException::class, InvalidKeyException::class, IllegalBlockSizeException::class, BadPaddingException::class, UnsupportedEncodingException::class)
    fun decryptAsString(data: ByteArray, key: Key?, offset: Int): String {
        return String(decrypt(data, key, offset), Cryptor.DEFAULT_CHARSET)
    }

    @Throws(NoSuchAlgorithmException::class, NoSuchPaddingException::class, InvalidKeyException::class, IllegalBlockSizeException::class, BadPaddingException::class)
    fun decrypt(data: ByteArray, key: Key?, offset: Int): ByteArray {
        val max = KEY_SIZE / 8
        val cipher: Cipher = Cipher.getInstance("RSA")
        cipher.init(Cipher.DECRYPT_MODE, key)

        // we need to split in pieces, because RSA cannot handle pieces bigger than the key size
        val list: List<ByteArray> = ArrayList<ByteArray>()
        var off = offset
        val len = data.size
        var l: Int
        var total = 0
        var part: ByteArray
        while (off < len) {
            l = if (len - off < max) len - off else max
            part = cipher.doFinal(data, off, l)
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
    }
}