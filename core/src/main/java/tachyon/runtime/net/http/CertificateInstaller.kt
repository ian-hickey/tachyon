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
package tachyon.runtime.net.http

import java.io.IOException

class CertificateInstaller(source: Resource?, host: String?, port: Int, passphrase: CharArray?) {
    private val host: String?
    private val port: Int
    private val passphrase: CharArray?
    private val source: Resource?
    private val tmf: TrustManagerFactory?
    private val tm: SavingTrustManager?
    private val context: SSLContext?
    private var ks: KeyStore?

    constructor(source: Resource?, host: String?, port: Int) : this(source, host, port, "changeit".toCharArray()) {}

    @Throws(IOException::class, KeyStoreException::class, NoSuchAlgorithmException::class, CertificateException::class)
    fun installAll() {
        for (i in tm!!.chain.indices) {
            install(i)
        }
    }

    @Throws(IOException::class, KeyStoreException::class, NoSuchAlgorithmException::class, CertificateException::class)
    fun install(index: Int) {
        val cert: X509Certificate? = tm!!.chain!![index]
        val alias = host.toString() + "-" + (index + 1)
        ks.setCertificateEntry(alias, cert)
        val os: OutputStream = source.getOutputStream()
        try {
            ks.store(os, passphrase)
        } finally {
            IOUtil.close(os)
        }
    }

    /**
     * checks if a certificate is installed for given host:port
     *
     * @param context
     * @param host
     * @param port
     * @return
     */
    fun checkCertificate(): IOException? {
        val factory: SSLSocketFactory = context.getSocketFactory()
        return try {
            val socket: SSLSocket = factory.createSocket(host, port) as SSLSocket
            socket.setSoTimeout(10000)
            socket.startHandshake()
            socket.close()
            null
        } catch (e: IOException) {
            e
        }
    }

    val certificates: Array<Any?>?
        get() = tm!!.chain

    private class SavingTrustManager internal constructor(tm: X509TrustManager?) : X509TrustManager {
        private val tm: X509TrustManager?
        var chain: Array<X509Certificate?>?

        @get:Override
        val acceptedIssuers: Array<Any?>?
            get() {
                throw UnsupportedOperationException()
            }

        @Override
        @Throws(CertificateException::class)
        fun checkClientTrusted(chain: Array<X509Certificate?>?, authType: String?) {
            throw UnsupportedOperationException()
        }

        @Override
        @Throws(CertificateException::class)
        fun checkServerTrusted(chain: Array<X509Certificate?>?, authType: String?) {
            this.chain = chain
            tm.checkServerTrusted(chain, authType)
        }

        init {
            this.tm = tm
        }
    } /*
	 * public static void main(String[] args) throws Exception { //String host="jira.jboss.org";
	 * 
	 * String host="sso.vogel.de"; int port=443; char[] passphrase="changeit".toCharArray();
	 * 
	 * ResourceProvider frp = ResourcesImpl.getFileResourceProvider(); Resource source =
	 * frp.getResource("/Users/mic/Temp/cacerts");
	 * 
	 * 
	 * CertificateInstaller util = new CertificateInstaller(source,host,port,passphrase);
	 * util.printCertificates(); util.installAll();
	 * 
	 * }
	 */

    init {
        this.source = source
        this.host = host
        this.port = port
        this.passphrase = passphrase
        ks = null
        val `in`: InputStream = source.getInputStream()
        try {
            ks = KeyStore.getInstance(KeyStore.getDefaultType())
            ks.load(`in`, passphrase)
        } finally {
            IOUtil.close(`in`)
        }
        context = SSLContext.getInstance("SSL")
        tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
        tmf.init(ks)
        val defaultTrustManager: X509TrustManager = tmf.getTrustManagers().get(0) as X509TrustManager
        tm = SavingTrustManager(defaultTrustManager)
        context.init(null, arrayOf(tm), null)
        val e: IOException? = checkCertificate()
        if (tm.chain == null) {
            if (e == null) {
                throw IOException("Could not obtain server certificate chain")
            } else {
                throw IOException("Could not obtain server certificate chain, [ $e ]")
            }
        }
    }
}