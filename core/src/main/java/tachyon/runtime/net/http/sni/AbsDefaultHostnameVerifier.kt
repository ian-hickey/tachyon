package tachyon.runtime.net.http.sni

import java.net.InetAddress

class AbsDefaultHostnameVerifier @JvmOverloads constructor(publicSuffixMatcher: PublicSuffixMatcher? = null) : HostnameVerifier {
    companion object {
        const val DNS_NAME_TYPE = 2
        const val IP_ADDRESS_TYPE = 7

        // private static final Object token = new Object();
        private val log: Log? = null
        @Throws(SSLException::class)
        fun matchIPAddress(host: String?, subjectAlts: List<String?>?) {
            for (i in 0 until subjectAlts!!.size()) {
                val subjectAlt = subjectAlts!![i]
                if (host!!.equals(subjectAlt)) {
                    return
                }
            }
            throw SSLException("Certificate for <$host> doesn't match any of the subject alternative names: $subjectAlts")
        }

        @Throws(SSLException::class)
        fun matchIPv6Address(host: String?, subjectAlts: List<String?>?) {
            val normalisedHost = normaliseAddress(host)
            for (i in 0 until subjectAlts!!.size()) {
                val subjectAlt = subjectAlts!![i]
                val normalizedSubjectAlt = normaliseAddress(subjectAlt)
                if (normalisedHost!!.equals(normalizedSubjectAlt)) {
                    return
                }
            }
            throw SSLException("Certificate for <$host> doesn't match any of the subject alternative names: $subjectAlts")
        }

        @Throws(SSLException::class)
        fun matchDNSName(host: String?, subjectAlts: List<String?>?, publicSuffixMatcher: PublicSuffixMatcher?) {
            val normalizedHost: String = host.toLowerCase(Locale.ROOT)
            for (i in 0 until subjectAlts!!.size()) {
                val subjectAlt = subjectAlts!![i]
                val normalizedSubjectAlt: String = subjectAlt.toLowerCase(Locale.ROOT)
                if (matchIdentityStrict(normalizedHost, normalizedSubjectAlt, publicSuffixMatcher)) {
                    return
                }
            }
            throw SSLException("Certificate for <$host> doesn't match any of the subject alternative names: $subjectAlts")
        }

        @Throws(SSLException::class)
        fun matchCN(host: String?, cn: String?, publicSuffixMatcher: PublicSuffixMatcher?) {
            if (!matchIdentityStrict(host, cn, publicSuffixMatcher)) {
                throw SSLException("Certificate for <$host> doesn't match common name of the certificate subject: $cn")
            }
        }

        fun matchDomainRoot(host: String?, domainRoot: String?): Boolean {
            return if (domainRoot == null) {
                false
            } else host.endsWith(domainRoot) && (host!!.length() === domainRoot.length() || host.charAt(host!!.length() - domainRoot.length() - 1) === '.')
        }

        private fun matchIdentity(host: String?, identity: String?, publicSuffixMatcher: PublicSuffixMatcher?, strict: Boolean): Boolean {
            if (publicSuffixMatcher != null && host.contains(".")) {
                if (!matchDomainRoot(host, publicSuffixMatcher.getDomainRoot(identity, DomainType.ICANN))) {
                    return false
                }
            }

            // RFC 2818, 3.1. Server Identity
            // "...Names may contain the wildcard
            // character * which is considered to match any single domain name
            // component or component fragment..."
            // Based on this statement presuming only singular wildcard is legal
            val asteriskIdx: Int = identity.indexOf('*')
            if (asteriskIdx != -1) {
                val prefix: String = identity.substring(0, asteriskIdx)
                val suffix: String = identity.substring(asteriskIdx + 1)
                if (!prefix.isEmpty() && !host.startsWith(prefix)) {
                    return false
                }
                if (!suffix.isEmpty() && !host.endsWith(suffix)) {
                    return false
                }
                // Additional sanity checks on content selected by wildcard can be done here
                if (strict) {
                    val remainder: String = host.substring(prefix.length(), host!!.length() - suffix.length())
                    if (remainder.contains(".")) {
                        return false
                    }
                }
                return true
            }
            return host.equalsIgnoreCase(identity)
        }

        fun matchIdentity(host: String?, identity: String?, publicSuffixMatcher: PublicSuffixMatcher?): Boolean {
            return matchIdentity(host, identity, publicSuffixMatcher, false)
        }

        fun matchIdentity(host: String?, identity: String?): Boolean {
            return matchIdentity(host, identity, null, false)
        }

        fun matchIdentityStrict(host: String?, identity: String?, publicSuffixMatcher: PublicSuffixMatcher?): Boolean {
            return matchIdentity(host, identity, publicSuffixMatcher, true)
        }

        fun matchIdentityStrict(host: String?, identity: String?): Boolean {
            return matchIdentity(host, identity, null, true)
        }

        @Throws(SSLException::class)
        fun extractCN(subjectPrincipal: String?): String? {
            return if (subjectPrincipal == null) {
                null
            } else try {
                val subjectDN = LdapName(subjectPrincipal)
                val rdns: List<Rdn?> = subjectDN.getRdns()
                for (i in rdns.size() - 1 downTo 0) {
                    val rds: Rdn? = rdns[i]
                    val attributes: Attributes = rds.toAttributes()
                    val cn: Attribute = attributes.get("cn")
                    if (cn != null) {
                        try {
                            val value: Object = cn.get()
                            if (value != null) {
                                return value.toString()
                            }
                        } catch (ignore: NoSuchElementException) {
                        } catch (ignore: NamingException) {
                        }
                    }
                }
                null
            } catch (e: InvalidNameException) {
                throw SSLException("$subjectPrincipal is not a valid X500 distinguished name")
            }
        }

        fun extractSubjectAlts(cert: X509Certificate?, subjectType: Int): List<String?>? {
            var c: Collection<List<*>?>? = null
            try {
                c = cert.getSubjectAlternativeNames()
            } catch (ignore: CertificateParsingException) {
            }
            var subjectAltList: List<String?>? = null
            if (c != null) {
                for (aC in c) {
                    val type: Int = (aC!![0] as Integer?).intValue()
                    if (type == subjectType) {
                        val s = aC[1] as String?
                        if (subjectAltList == null) {
                            subjectAltList = ArrayList<String?>()
                        }
                        subjectAltList.add(s)
                    }
                }
            }
            return subjectAltList
        }

        /*
	 * Normalize IPv6 or DNS name.
	 */
        fun normaliseAddress(hostname: String?): String? {
            return if (hostname == null) {
                hostname
            } else try {
                val inetAddress: InetAddress = InetAddress.getByName(hostname)
                inetAddress.getHostAddress()
            } catch (unexpected: UnknownHostException) { // Should not happen, because we check for IPv6 address above
                hostname
            }
        }

        init {
            log = LogFactory.getLog("tachyon.runtime.net.http.sni.AbsDefaultHostnameVerifier")
        }
    }

    private val publicSuffixMatcher: PublicSuffixMatcher?
    @Override
    fun verify(host: String?, session: SSLSession?): Boolean {
        return try {
            val certs: Array<Certificate?> = session.getPeerCertificates()
            val x509: X509Certificate? = certs[0] as X509Certificate?
            verify(host, x509)
            true
        } catch (ex: SSLException) {
            if (log.isDebugEnabled()) {
                log.debug(ex.getMessage(), ex)
            }
            false
        }
    }

    @Throws(SSLException::class)
    fun verify(host: String?, cert: X509Certificate?) {
        val ipv4: Boolean = InetAddressUtils.isIPv4Address(host)
        val ipv6: Boolean = InetAddressUtils.isIPv6Address(host)
        val subjectType = if (ipv4 || ipv6) IP_ADDRESS_TYPE else DNS_NAME_TYPE
        val subjectAlts = extractSubjectAlts(cert, subjectType)
        if (subjectAlts != null && !subjectAlts.isEmpty()) {
            if (ipv4) {
                matchIPAddress(host, subjectAlts)
            } else if (ipv6) {
                matchIPv6Address(host, subjectAlts)
            } else {
                matchDNSName(host, subjectAlts, publicSuffixMatcher)
            }
        } else {
            // CN matching has been deprecated by rfc2818 and can be used
            // as fallback only when no subjectAlts are available
            val subjectPrincipal: X500Principal = cert.getSubjectX500Principal()
            val cn = extractCN(subjectPrincipal.getName(X500Principal.RFC2253))
                    ?: throw SSLException("Certificate subject for <$host> doesn't contain a common name and does not have alternative names")
            matchCN(host, cn, publicSuffixMatcher)
        }
    }

    init {
        this.publicSuffixMatcher = publicSuffixMatcher
    }
}