package lucee.runtime.net.http.sni

import java.io.IOException

class DefaultHttpClientConnectionOperatorImpl(socketFactoryRegistry: Lookup<ConnectionSocketFactory?>?) : DefaultHttpClientConnectionOperator(socketFactoryRegistry, null, null) {
    @Override
    @Throws(IOException::class)
    fun connect(conn: ManagedHttpClientConnection?, host: HttpHost?, localAddress: InetSocketAddress?, connectTimeout: Int,
                socketConfig: SocketConfig?, context: HttpContext?) {
        try {
            super.connect(conn, host, localAddress, connectTimeout, socketConfig, context)
        } catch (e: SSLProtocolException) {
            val enableSniValue = context.getAttribute(SSLConnectionSocketFactoryImpl.ENABLE_SNI) as Boolean
            val enableSni = enableSniValue == null || enableSniValue
            if (enableSni && e.getMessage() != null && e.getMessage().equals("handshake alert:  unrecognized_name")) {
                // print.e("Server received saw wrong SNI host, retrying without SNI");
                context.setAttribute(SSLConnectionSocketFactoryImpl.ENABLE_SNI, false)
                super.connect(conn, host, localAddress, connectTimeout, socketConfig, context)
            } else throw e
        }
    }
}