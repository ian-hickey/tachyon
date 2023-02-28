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
package lucee.runtime.net.mail

import java.io.UnsupportedEncodingException

object MailUtil {
    val SYSTEM_PROP_MAIL_SSL_PROTOCOLS: String? = "mail.smtp.ssl.protocols"
    @Throws(UnsupportedEncodingException::class)
    fun encode(text: String?, encoding: String?): String? {
        // print.ln(StringUtil.changeCharset(text,encoding));
        return MimeUtility.encodeText(text, encoding, "Q")
    }

    @Throws(UnsupportedEncodingException::class)
    fun decode(text: String?): String? {
        return MimeUtility.decodeText(text)
    }

    @Throws(MailException::class, UnsupportedEncodingException::class, PageException::class)
    fun toInternetAddress(emails: Object?): InternetAddress? {
        if (emails is String) {
            return parseEmail(emails, null)
        }
        val addresses: Array<InternetAddress?>? = toInternetAddresses(emails)
        if (addresses != null && addresses.size > 0) return addresses[0]
        throw MailException("invalid email address definition") // should never come to this!
    }

    @Throws(MailException::class, UnsupportedEncodingException::class, PageException::class)
    fun toInternetAddresses(emails: Object?): Array<InternetAddress?>? {
        return if (emails is Array<InternetAddress>) emails else if (emails is String) fromList(emails as String?) else if (Decision.isArray(emails)) fromArray(Caster.toArray(emails)) else if (Decision.isStruct(emails)) arrayOf<InternetAddress?>(fromStruct(Caster.toStruct(emails))) else throw MailException("e-mail definitions must be one of the following types [string,array,struct], not [" + emails.getClass().getName().toString() + "]")
    }

    @Throws(MailException::class, PageException::class, UnsupportedEncodingException::class)
    private fun fromArray(array: Array?): Array<InternetAddress?>? {
        val it: Iterator = array.valueIterator()
        var el: Object
        val pairs: ArrayList<InternetAddress?> = ArrayList()
        while (it.hasNext()) {
            el = it.next()
            if (Decision.isStruct(el)) {
                pairs.add(fromStruct(Caster.toStruct(el)))
            } else {
                val addr: InternetAddress? = parseEmail(Caster.toString(el), null)
                if (addr != null) pairs.add(addr)
            }
        }
        return pairs.toArray(arrayOfNulls<InternetAddress?>(pairs.size()))
    }

    @Throws(MailException::class, UnsupportedEncodingException::class)
    private fun fromStruct(sct: Struct?): InternetAddress? {
        var name: String = Caster.toString(sct.get("label", null), null)
        if (name == null) name = Caster.toString(sct.get("name", null), null)
        var email: String = Caster.toString(sct.get("email", null), null)
        if (email == null) email = Caster.toString(sct.get("e-mail", null), null)
        if (email == null) email = Caster.toString(sct.get("mail", null), null)
        if (StringUtil.isEmpty(email)) throw MailException("missing e-mail definition in struct")
        if (name == null) name = ""
        return InternetAddress(email, name)
    }

    @Throws(MailException::class)
    private fun fromList(strEmails: String?): Array<InternetAddress?>? {
        if (StringUtil.isEmpty(strEmails, true)) return arrayOfNulls<InternetAddress?>(0)
        val raw: Array = ListUtil.listWithQuotesToArray(strEmails, ",;", "\"")
        val it: Iterator<Object?> = raw.valueIterator()
        val al: ArrayList<InternetAddress?> = ArrayList()
        while (it.hasNext()) {
            val addr: InternetAddress? = parseEmail(it.next())
            if (addr != null) al.add(addr)
        }
        return al.toArray(arrayOfNulls<InternetAddress?>(al.size()))
    }

    /**
     * returns true if the passed value is a in valid email address format
     *
     * @param value
     * @return
     */
    fun isValidEmail(value: Object?): Boolean {
        try {
            val addr: InternetAddress? = parseEmail(value, null)
            if (addr != null) {
                val address: String = addr.getAddress()
                if (address.contains("..")) return false
                var pos: Int = address.indexOf('@')
                if (pos < 1 || pos == address.length() - 1) return false
                val local: String = address.substring(0, pos)
                val domain: String = address.substring(pos + 1)
                if (local.length() > 64) return false // local part may only be 64 characters
                if (domain.length() > 255) return false // domain may only be 255 characters
                if (domain.charAt(0) === '.' || local.charAt(0) === '.' || local.charAt(local.length() - 1) === '.') return false
                pos = domain.lastIndexOf('.')
                if (pos > 0 && pos < domain.length() - 2) { // test TLD to be at
                    // least 2 chars all
                    // alpha characters
                    if (StringUtil.isAllAlpha(domain.substring(pos + 1))) return true
                    try {
                        addr.validate()
                        return true
                    } catch (e: AddressException) {
                    }
                }
            }
        } catch (e: Exception) {
        }
        return false
    }

    @Throws(MailException::class)
    fun parseEmail(value: Object?): InternetAddress? {
        val ia: InternetAddress? = parseEmail(value, null)
        if (ia != null) return ia
        if (value is CharSequence) {
            if (StringUtil.isEmpty(value.toString())) return null
            throw MailException("[$value] cannot be converted to an email address")
        }
        throw MailException("input cannot be converted to an email address")
    }

    /**
     * returns an InternetAddress object or null if the parsing fails. to be be used in multiple places.
     *
     * @param value
     * @return
     */
    fun parseEmail(value: Object?, defaultValue: InternetAddress?): InternetAddress? {
        var str: String? = Caster.toString(value, "")
        if (StringUtil.isEmpty(str)) return defaultValue
        if (str.indexOf('@') > -1) {
            try {
                str = fixIDN(str)
                // fixIDN( addr );
                return InternetAddress(str)
            } catch (ex: AddressException) {
            }
        }
        return defaultValue
    }

    /**
     * converts IDN to ASCII if needed
     *
     * @param addr
     * @return
     */
    fun fixIDN(addr: String?): String? {
        val pos: Int = addr.indexOf('@')
        if (pos > 0 && pos < addr!!.length() - 1) {
            var domain: String = addr.substring(pos + 1)
            if (!StringUtil.isAscii(domain)) {
                domain = IDN.toASCII(domain)
                return addr.substring(0, pos).toString() + "@" + domain
            }
        }
        return addr
    }

    /**
     * This method should be called when TLS is used to ensure that the supported protocols are set.
     * Some servers, e.g. Outlook365, reject lists with older protocols so we only pass protocols that
     * start with the prefix "TLS"
     */
    fun setSystemPropMailSslProtocols() {
        var protocols: String = SystemUtil.getSystemPropOrEnvVar(SYSTEM_PROP_MAIL_SSL_PROTOCOLS, "")
        if (protocols.isEmpty()) {
            val supportedProtocols: List<String?> = SSLConnectionSocketFactoryImpl.getSupportedSslProtocols()
            protocols = supportedProtocols.stream().filter { el -> el.startsWith("TLS") }.collect(Collectors.joining(" "))
            if (!protocols.isEmpty()) {
                System.setProperty(SYSTEM_PROP_MAIL_SSL_PROTOCOLS, protocols)
                val config: Config = ThreadLocalPageContext.getConfig()
                if (config != null) ThreadLocalPageContext.getLog(config, "mail").info("mail", "Lucee system property " + SYSTEM_PROP_MAIL_SSL_PROTOCOLS + " set to [" + protocols + "]")
            }
        }
    }
}