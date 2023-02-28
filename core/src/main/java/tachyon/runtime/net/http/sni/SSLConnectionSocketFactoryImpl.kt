package tachyon.runtime.net.http.sni

import java.io.IOException

class SSLConnectionSocketFactoryImpl : SSLConnectionSocketFactory {
    /*
	 * Implement any constructor you need for your particular application - SSLConnectionSocketFactory
	 * has many variants
	 */
    constructor(sslContext: SSLContext?, verifier: HostnameVerifier?) : super(sslContext, verifier) {}
    constructor(sslContext: SSLContext?) : super(sslContext) {}

    @Override
    @Throws(IOException::class)
    fun createLayeredSocket(socket: Socket?, target: String?, port: Int, context: HttpContext?): Socket? {
        val enableSniValue = context.getAttribute(ENABLE_SNI) as Boolean
        val enableSni = enableSniValue == null || enableSniValue
        return super.createLayeredSocket(socket, if (enableSni) target else ENABLE_SNI, port, context)
    }

    companion object {
        val ENABLE_SNI: String? = "*.disable.sni"
        val supportedSslProtocols: List<String?>?
            get() {
                try {
                    return Arrays.asList(SSLContext.getDefault().getSupportedSSLParameters().getProtocols())
                } catch (ex: NoSuchAlgorithmException) {
                }
                return Collections.emptyList()
            }
    }
}