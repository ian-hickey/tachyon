/**
 * Copyright (c) 2015, Lucee Assosication Switzerland. All rights reserved.
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
package lucee.runtime.config

import java.io.IOException

class PasswordImpl : Password {
    private val rawPassword: String?
    private val password: String?
    private val salt: String?
    private val type: Int
    private val origin: Int

    private constructor(origin: Int, password: String?, salt: String?, type: Int) {
        rawPassword = null
        this.password = password
        this.salt = salt
        this.type = type
        this.origin = origin
    }

    internal constructor(origin: Int, rawPassword: String?, salt: String?) {
        this.rawPassword = rawPassword
        password = hash(rawPassword, salt)
        this.salt = salt
        type = if (StringUtil.isEmpty(salt)) HASHED else HASHED_SALTED
        this.origin = origin
    }

    @Override
    fun getPassword(): String? {
        return password
    }

    @Override
    fun getSalt(): String? {
        return salt
    }

    @Override
    fun getType(): Int {
        return type
    }

    @Override
    fun getOrigin(): Int {
        return origin
    }

    @Override
    fun isEqual(config: Config?, other: String?): Password? {
        // an already hashed password that matches
        if (password!!.equals(other)) return this

        // current password is only hashed
        if (type == HASHED) return if (password.equals(hash(other, null))) this else null
        // current password is hashed and salted
        return if (password.equals(hash(other, salt))) this else null
    }

    @Override
    override fun equals(obj: Object?): Boolean {
        if (this === obj) return true
        if (obj is Password) {
            val opw: Password? = obj as Password?
            if (password!!.equals(opw.getPassword())) return true
            if (obj is PasswordImpl) {
                val pi = obj as PasswordImpl?
                if (pi!!.rawPassword != null) {
                    return if (type == HASHED) hash(pi.rawPassword, null)!!.equals(password) else hash(pi.rawPassword, salt)!!.equals(password)
                }
            }
        }
        if (obj is CharSequence) {
            val str: String = obj.toString()
            if (password!!.equals(str)) return true
            return if (type == HASHED) hash(str, null)!!.equals(password) else hash(str, salt)!!.equals(password)
        }
        return false
    }

    companion object {
        private fun hash(str: String?, salt: String?): String? {
            return try {
                Hash.hash(if (StringUtil.isEmpty(salt, true)) str else str.toString() + ":" + salt, Hash.ALGORITHM_SHA_256, 5, Hash.ENCODING_HEX)
            } catch (e: NoSuchAlgorithmException) {
                throw RuntimeException(e)
            }
        }

        fun readFromStruct(data: Struct?, salt: String?, isDefault: Boolean): Password? {
            val prefix = if (isDefault) "adminDefault" else "admin"
            val prefixOlder = if (isDefault) "default" else ""

            // first we look for the hashed and salted password
            // preferred adminDefaultHSPW adminHSPW
            var pw: String = ConfigWebFactory.getAttr(data, prefix + "hspw")
            if (StringUtil.isEmpty(pw, true)) pw = ConfigWebFactory.getAttr(data, prefixOlder + "hspw")
            if (!StringUtil.isEmpty(pw, true)) {
                // password is only of use when there is a salt as well
                return if (salt == null) null else PasswordImpl(ORIGIN_HASHED_SALTED, pw, salt, HASHED_SALTED)
            }

            // fall back to password that is hashed but not salted
            // preferred adminDefaultPW adminPW
            pw = ConfigWebFactory.getAttr(data, prefix + "pw")
            if (StringUtil.isEmpty(pw, true)) pw = ConfigWebFactory.getAttr(data, prefixOlder + "pw")
            if (!StringUtil.isEmpty(pw, true)) {
                return PasswordImpl(ORIGIN_HASHED, pw, null, HASHED)
            }

            // fall back to encrypted password
            // preferred adminDefaultPassword adminPassword
            var pwEnc: String = ConfigWebFactory.getAttr(data, prefix + "Password")
            if (StringUtil.isEmpty(pwEnc, true)) pwEnc = ConfigWebFactory.getAttr(data, prefixOlder + "Password")
            if (isDefault && StringUtil.isEmpty(pwEnc, true)) pwEnc = ConfigWebFactory.getAttr(data, "adminPasswordDefault")
            if (!StringUtil.isEmpty(pwEnc, true)) {
                val rawPassword: String = BlowfishEasy("tpwisgh").decryptString(pwEnc)
                return PasswordImpl(ORIGIN_ENCRYPTED, rawPassword, salt)
            }
            return null
        }

        fun writeToStruct(el: Struct?, passwordRaw: String?, isDefault: Boolean): Password? {
            // salt
            val salt = getSalt(el)
            val pw: Password = PasswordImpl(ORIGIN_UNKNOW, passwordRaw, salt)
            writeToStruct(el, pw, isDefault)
            return pw
        }

        private fun getSalt(data: Struct?): String? {
            var salt: String = Caster.toString(data.get("salt", null), null)
            if (StringUtil.isEmpty(salt, true)) salt = Caster.toString(data.get("adminsalt", null), null)
            if (StringUtil.isEmpty(salt, true)) throw RuntimeException("missing salt!") // this should never happen
            return salt.trim()
        }

        fun writeToStruct(data: Struct?, pw: Password?, isDefault: Boolean) {
            val prefix = if (isDefault) "default-" else ""
            if (pw == null) {
                if (data.containsKey(prefix + "hspw")) data.remove(prefix + "hspw")
                if (data.containsKey(prefix + "pw")) data.remove(prefix + "pw")
                if (data.containsKey(prefix + "password")) data.remove(prefix + "password")
            } else {
                // remove backward compatibility
                if (data.containsKey(prefix + "pw")) data.remove(prefix + "pw")
                if (data.containsKey(prefix + "password")) data.remove(prefix + "password")
                if (pw.getType() === HASHED_SALTED) data.setEL(prefix + "hspw", pw.getPassword()) else {
                    var pwi: PasswordImpl?
                    if (pw is PasswordImpl && (pw as PasswordImpl?).also { pwi = it }.rawPassword != null) {
                        data.setEL(prefix + "hspw", hash(pwi!!.rawPassword, getSalt(data)))
                    } else {
                        data.setEL(prefix + "pw", pw.getPassword()) // this should never happen
                    }
                }
            }
        }

        fun removeFromStruct(root: Struct?, isDefault: Boolean) {
            writeToStruct(root, null as Password?, isDefault)
        }

        fun updatePasswordIfNecessary(config: ConfigPro?, passwordOld: Password?, strPasswordNew: String?): Password? {
            try {
                // is the server context default password used
                var defPass = false
                if (config is ConfigWebPro) defPass = (config as ConfigWebPro?)!!.isDefaultPassword()
                val origin: Int = config!!.getPasswordOrigin()

                // current is old style password and not a default password!
                if ((origin == Password.ORIGIN_HASHED || origin == Password.ORIGIN_ENCRYPTED) && !defPass) {
                    // is passord valid!
                    if (config!!.isPasswordEqual(strPasswordNew) != null) {
                        // new salt
                        val saltn: String = config!!.getSalt() // get salt from context, not from old password that can be different when default password

                        // new password
                        var passwordNew: Password? = null
                        if (!StringUtil.isEmpty(strPasswordNew, true)) passwordNew = PasswordImpl(ORIGIN_UNKNOW, strPasswordNew, saltn)
                        updatePassword(config, passwordOld, passwordNew)
                        return passwordNew
                    }
                }
            } catch (t: Throwable) {
                ExceptionUtil.rethrowIfNecessary(t)
            }
            return null
        }

        /**
         *
         * @param config Config of the context (ConfigServer to set a server level password)
         * @param strPasswordOld the old password to replace or null if there is no password set yet
         * @param strPasswordNew the new password
         * @throws IOException
         * @throws SAXException
         * @throws PageException
         * @throws BundleException
         * @throws ConverterException
         */
        @Throws(IOException::class, PageException::class, BundleException::class, ConverterException::class)
        fun updatePassword(config: ConfigPro?, strPasswordOld: String?, strPasswordNew: String?) {

            // old salt
            val pwType: Int = config!!.getPasswordType() // get type from password
            var salto: String? = config!!.getPasswordSalt() // get salt from password
            if (pwType == Password.HASHED) salto = null // if old password does not use a salt, we do not use a salt to hash

            // new salt
            val saltn: String = config!!.getSalt() // get salt from context, not from old password that can be different when default password

            // old password
            var passwordOld: Password? = null
            if (!StringUtil.isEmpty(strPasswordOld, true)) passwordOld = PasswordImpl(ORIGIN_UNKNOW, strPasswordOld, salto)

            // new password
            var passwordNew: Password? = null
            if (!StringUtil.isEmpty(strPasswordNew, true)) passwordNew = PasswordImpl(ORIGIN_UNKNOW, strPasswordNew, saltn)
            updatePassword(config, passwordOld, passwordNew)
        }

        @Throws(IOException::class, PageException::class, BundleException::class, ConverterException::class)
        fun updatePassword(config: ConfigPro?, passwordOld: Password?, passwordNew: Password?) {
            if (!config.hasPassword()) {
                config!!.setPassword(passwordNew)
                val admin: ConfigAdmin = ConfigAdmin.newInstance(config, passwordNew)
                admin!!.setPassword(passwordNew)
                admin!!.storeAndReload()
            } else {
                ConfigWebUtil.checkPassword(config, "write", passwordOld)
                ConfigWebUtil.checkGeneralWriteAccess(config, passwordOld)
                val admin: ConfigAdmin = ConfigAdmin.newInstance(config, passwordOld)
                admin!!.setPassword(passwordNew)
                admin!!.storeAndReload()
            }
        }

        fun passwordToCompare(cw: ConfigWeb?, server: Boolean, rawPassword: String?): Password? {
            if (StringUtil.isEmpty(rawPassword, true)) return null
            val cwi: ConfigWebPro? = cw
            val pwType: Int
            val pwSalt: String
            if (server) {
                pwType = cwi!!.getServerPasswordType()
                pwSalt = cwi!!.getServerPasswordSalt()
            } else {
                pwType = cwi!!.getPasswordType()
                pwSalt = cwi!!.getPasswordSalt()
            }

            // if the internal password is not using the salt yet, this hash should eigther
            val salt = if (pwType == Password.HASHED) null else pwSalt
            return PasswordImpl(ORIGIN_UNKNOW, rawPassword, salt)
        }
    }
}