package tachyon.runtime.net.http.sni

import java.security.cert.X509Certificate

class DefaultHostnameVerifierImpl : AbsDefaultHostnameVerifier() {
    @Override
    override fun verify(host: String?, session: SSLSession?): Boolean {
        return if (SSLConnectionSocketFactoryImpl.ENABLE_SNI!!.equals(host)) true else super.verify(host, session)
    }

    @Override
    @Throws(SSLException::class)
    override fun verify(host: String?, cert: X509Certificate?) {
        if (SSLConnectionSocketFactoryImpl.ENABLE_SNI!!.equals(host)) return
        super.verify(host, cert)
    }
}