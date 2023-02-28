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
package tachyon.runtime.security

import java.io.IOException

/**
 * User Password Information
 */
class CredentialImpl(var username: String?, var password: String?, var roles: Array<String?>?, rolesDir: Resource?, privateKey: String?, salt: String?, iter: Int) : Credential {
    private val rolesDir: Resource?
    private val privateKey: String?
    private val salt: ByteArray?
    private val iter: Int

    companion object {
        private val staticSalt: ByteArray?
        private const val staticIter = 0
        private val staticPrivateKey: String? = null
        private const val ONE = 1.toChar()
        private val ALGO: String? = "Blowfish/CBC/PKCS5Padding"
        private fun toSalt(salt: String?): ByteArray? {
            val barr: ByteArray = salt.trim().getBytes(CharsetUtil.UTF8)
            if (barr.size == 8) return barr
            // we only take the first 8 bytes
            if (barr.size > 8) {
                val tmp = ByteArray(8)
                for (i in tmp.indices) {
                    tmp[i] = barr[i]
                }
                return tmp
            }
            // we repeat the bytes until we reach 8
            val tmp = ByteArray(8)
            var index = 0
            outer@ while (true) {
                for (i in barr.indices) {
                    if (index >= 8) break@outer
                    tmp[index++] = barr[i]
                }
            }
            return tmp
        }

        /**
         * convert an Object to a String Array of Roles
         *
         * @param oRoles
         * @return roles
         * @throws PageException
         */
        @Throws(PageException::class)
        fun toRole(oRoles: Object?): Array<String?>? {
            var oRoles: Object? = oRoles
            if (oRoles is String) {
                oRoles = ListUtil.listToArrayRemoveEmpty(oRoles.toString(), ",")
            }
            if (oRoles is Array) {
                val arrRoles: Array? = oRoles
                val roles = arrayOfNulls<String?>(arrRoles.size())
                for (i in roles.indices) {
                    roles[i] = Caster.toString(arrRoles.get(i + 1, ""))
                }
                return roles
            }
            throw ApplicationException("invalid roles definition for tag loginuser")
        }

        @Throws(PageException::class)
        private fun encrypt(input: String?, privateKey: String?, salt: ByteArray?, iter: Int, precise: Boolean): String? {
            return if (StringUtil.isEmpty(privateKey, true)) Caster.toB64(input.getBytes(CharsetUtil.UTF8)) else try {
                Cryptor.encrypt(input, privateKey, ALGO, salt, iter, "Base64", Cryptor.DEFAULT_CHARSET, precise)
            } catch (e: Exception) {
                throw Caster.toPageException(e)
            }
        }

        @Throws(PageException::class)
        private fun decrypt(input: Object?, privateKey: String?, salt: ByteArray?, iter: Int, precise: Boolean): String? {
            return if (StringUtil.isEmpty(privateKey, true)) {
                try {
                    Base64Coder.decodeToString(Caster.toString(input), "UTF-8", true)
                } catch (e: Exception) {
                    throw Caster.toPageException(e)
                }
            } else try {
                Cryptor.decrypt(Caster.toString(input), privateKey, ALGO, salt, iter, "Base64", Cryptor.DEFAULT_CHARSET, precise)
            } catch (e: Exception) {
                throw Caster.toPageException(e)
            }
        }

        fun decode(encoded: Object?, rolesDir: Resource?, precise: Boolean): Credential? {
            return try {
                decode(encoded, rolesDir, null, null, 0, precise)
            } catch (e: Exception) {
                null
            }
        }

        /**
         * decode the Credential form a Base64 String value
         *
         * @param encoded
         * @return Credential from decoded string
         * @throws PageException
         */
        @Throws(PageException::class)
        fun decode(encoded: Object?, rolesDir: Resource?, privateKey: String?, salt: String?, iter: Int, precise: Boolean): Credential? {
            val _privateKey = if (StringUtil.isEmpty(privateKey, true)) staticPrivateKey else privateKey.trim()
            val _salt = if (StringUtil.isEmpty(salt, true)) staticSalt else toSalt(salt)
            val _iter = if (iter < 1) staticIter else iter
            val dec = decrypt(encoded, _privateKey, _salt, _iter, precise)
            val arr: Array = ListUtil.listToArray(dec, "" + ONE)
            val len: Int = arr.size()
            if (len == 3) {
                var str: String? = Caster.toString(arr.get(3, ""))
                if (str.startsWith("md5:")) {
                    if (!rolesDir.exists()) rolesDir.mkdirs()
                    str = str.substring(4)
                    val md5: Resource = rolesDir.getRealResource(str)
                    str = try {
                        IOUtil.toString(md5, CharsetUtil.UTF8)
                    } catch (e: IOException) {
                        ""
                    }
                }
                return CredentialImpl(Caster.toString(arr.get(1, "")), Caster.toString(arr.get(2, "")), str, rolesDir)
            }
            if (len == 2) return CredentialImpl(Caster.toString(arr.get(1, "")), Caster.toString(arr.get(2, "")), rolesDir)
            return if (len == 1) CredentialImpl(Caster.toString(arr.get(1, "")), rolesDir) else null
        }

        init {
            // salt
            val tmp: String = SystemUtil.getSystemPropOrEnvVar("tachyon.loginstorage.salt", null)
            if (StringUtil.isEmpty(tachyon.runtime.security.tmp, true)) tachyon.runtime.security.tmp = "nkhuvghc" else tachyon.runtime.security.tmp = tachyon.runtime.security.tmp.trim()
            staticSalt = toSalt(tachyon.runtime.security.tmp)

            // salt iteration
            val itmp: Int = Caster.toIntValue(SystemUtil.getSystemPropOrEnvVar("tachyon.loginstorage.iterations", null), 0)
            if (tachyon.runtime.security.itmp < 1) tachyon.runtime.security.itmp = 10
            staticIter = tachyon.runtime.security.itmp

            // private key
            tachyon.runtime.security.tmp = SystemUtil.getSystemPropOrEnvVar("tachyon.loginstorage.privatekey", null)
            if (!StringUtil.isEmpty(tachyon.runtime.security.tmp, true)) {
                staticPrivateKey = tachyon.runtime.security.tmp.trim()
            } else staticPrivateKey = null
        }
    }

    /**
     * credential constructor
     *
     * @param username
     */
    constructor(username: String?, rolesDir: Resource?) : this(username, null, arrayOfNulls<String?>(0), rolesDir, null, null, 0) {}

    /**
     * credential constructor
     *
     * @param username
     * @param password
     */
    constructor(username: String?, password: String?, rolesDir: Resource?) : this(username, password, arrayOfNulls<String?>(0), rolesDir, null, null, 0) {}

    /**
     * credential constructor
     *
     * @param username
     * @param password
     * @param roles
     * @throws PageException
     */
    constructor(username: String?, password: String?, roles: String?, rolesDir: Resource?) : this(username, password, toRole(roles), rolesDir, null, null, 0) {}

    /**
     * credential constructor
     *
     * @param username
     * @param password
     * @param roles
     * @throws PageException
     */
    constructor(username: String?, password: String?, roles: Array?, rolesDir: Resource?) : this(username, password, toRole(roles), rolesDir, null, null, 0) {}

    /**
     * credential constructor
     *
     * @param username
     * @param password
     * @param roles
     */
    constructor(username: String?, password: String?, roles: Array<String?>?, rolesDir: Resource?) : this(username, password, roles, rolesDir, null, null, 0) {}

    @Override
    fun getPassword(): String? {
        return password
    }

    @Override
    fun getRoles(): Array<String?>? {
        return roles
    }

    @Override
    fun getUsername(): String? {
        return username
    }

    @Override
    fun serialize(): String? {
        return serialize(null)
    }

    @Override
    fun serialize(done: Set<Object?>?): String? {
        return "createObject('java','tachyon.runtime.security.Credential').init('" + username + "','" + password + "','" + ListUtil.arrayToList(roles, ",") + "')"
    }

    @Override
    @Throws(PageException::class)
    fun encode(): String? {
        val raw: String = ListUtil.arrayToList(roles, ",")
        if (raw.length() > 100) {
            try {
                if (!rolesDir.exists()) rolesDir.mkdirs()
                val md5: String = MD5.getDigestAsString(raw)
                IOUtil.write(rolesDir.getRealResource(md5), raw, CharsetUtil.UTF8, false)
                return encrypt(username + ONE + password + ONE + "md5:" + md5, privateKey, salt, iter, true)
            } catch (e: IOException) {
            }
        }
        return try {
            encrypt(username + ONE + password + ONE.toInt() + raw, privateKey, salt, iter, true)
        } catch (e: Exception) {
            throw Caster.toPageException(e)
        }
    }

    @Override
    override fun toString(): String {
        return "username:$username;password:$password;roles:$roles"
    } /*
	 * public static void main(String[] args) throws PageException { int i = 20; Resource rolesDir =
	 * ResourcesImpl.getFileResourceProvider().getResource("/Users/mic/Temp/"); String key =
	 * "vhvzglmjknkvug"; String salt = "dbjvzvhvnbubvuh"; CredentialImpl c = new CredentialImpl("susi",
	 * "sorglos", new String[] { "qqq" }, rolesDir, key, salt, i); String enc = c.encode(); Credential
	 * res = CredentialImpl.decode(enc, rolesDir, key, "df", i); print.e(enc); print.e(res.toString());
	 * }
	 */

    init {
        this.rolesDir = rolesDir
        this.privateKey = if (StringUtil.isEmpty(privateKey, true)) staticPrivateKey else privateKey.trim()
        this.salt = if (StringUtil.isEmpty(salt, true)) staticSalt else toSalt(salt)
        this.iter = if (iter < 1) staticIter else iter
    }
}