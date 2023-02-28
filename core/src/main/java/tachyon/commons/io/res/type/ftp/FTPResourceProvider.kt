/**
 * Copyright (c) 2023, TachyonCFML.org
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
package tachyon.commons.io.res.type.ftp

import java.io.IOException

// TODO check connection timeout
class FTPResourceProvider : ResourceProviderPro {
    private var scheme = "ftp"
    private val clients: Map = HashMap()
    private var clientTimeout = 60000
    private var socketTimeout = -1
    private var lockTimeout = 20000

    /**
     * @return the cache
     */
    var cache = 20000
        private set
    private var closer: FTPResourceClientCloser? = null
    private val lock: ResourceLockImpl = ResourceLockImpl(lockTimeout, true)
    private var arguments: Map? = null
    private val sync: Object = SerializableObject()
    @Override
    fun init(scheme: String, arguments: Map?): ResourceProvider {
        setScheme(scheme)
        if (arguments != null) {
            this.arguments = arguments
            // client-timeout
            var strTimeout = arguments.get("client-timeout") as String
            if (strTimeout != null) {
                clientTimeout = Caster.toIntValue(strTimeout, clientTimeout)
            }
            // socket-timeout
            strTimeout = arguments.get("socket-timeout")
            if (strTimeout != null) {
                socketTimeout = Caster.toIntValue(strTimeout, socketTimeout)
            }
            // lock-timeout
            strTimeout = arguments.get("lock-timeout")
            if (strTimeout != null) {
                lockTimeout = Caster.toIntValue(strTimeout, lockTimeout)
            }
            // cache
            val strCache = arguments.get("cache") as String
            if (strCache != null) {
                cache = Caster.toIntValue(strCache, cache)
            }
        }
        lock.setLockTimeout(lockTimeout)
        return this
    }

    @Override
    fun getResource(path: String): Resource {
        var path = path
        path = ResourceUtil.removeScheme(scheme, path)
        val pc: PageContext = ThreadLocalPageContext.get()
        var base: FTPConnectionData? = null
        if (pc != null) {
            base = (pc.getApplicationContext() as ApplicationContextSupport).getFTP()
        }
        val dap: DataAndPath = FTPConnectionData.load(base, path)
        return FTPResource(this, dap.data, dap.path)
    }

    @Throws(IOException::class)
    fun getClient(data: FTPConnectionData): FTPResourceClient {
        var client: FTPResourceClient? = clients.remove(data.toString())
        if (client == null) {
            client = FTPResourceClient(data, cache)
            if (socketTimeout > 0) client.setSoTimeout(socketTimeout)
        }
        if (!client.isConnected()) {
            if (ProxyDataImpl.isValid(data.getProxyData(), data.host)) {
                try {
                    Proxy.start(data.getProxyData())
                    connect(client, data)
                } finally {
                    Proxy.end()
                }
            } else {
                connect(client, data)
            }
            val replyCode: Int = client.getReplyCode()
            if (replyCode >= 400) throw FTPException(replyCode)
        }
        startCloser()
        return client
    }

    private fun startCloser() {
        synchronized(sync) {
            if (closer == null || !closer.isAlive()) {
                closer = FTPResourceClientCloser(this)
                closer.start()
            }
        }
    }

    @Throws(SocketException::class, IOException::class)
    private fun connect(client: FTPResourceClient, data: FTPConnectionData) {
        if (data.port > 0) client.connect(data.host, data.port) else client.connect(data.host)
        if (!StringUtil.isEmpty(data.username)) client.login(data.username, data.password)
    }

    fun returnClient(client: FTPResourceClient?) {
        if (client == null) return
        client.touch()
        clients.put(client.getFtpConnectionData().toString(), client)
    }

    @Override
    fun getScheme(): String {
        return scheme
    }

    fun setScheme(scheme: String) {
        if (!StringUtil.isEmpty(scheme)) this.scheme = scheme
    }

    @Override
    fun setResources(resources: Resources?) {
        // this.resources=resources;
    }

    @Override
    @Throws(IOException::class)
    fun lock(res: Resource?) {
        lock.lock(res)
    }

    @Override
    fun unlock(res: Resource?) {
        lock.unlock(res)
    }

    @Override
    @Throws(IOException::class)
    fun read(res: Resource?) {
        lock.read(res)
    }

    fun clean() {
        val keys: Array<Object> = clients.keySet().toArray()
        var client: FTPResourceClient
        for (i in keys.indices) {
            client = clients.get(keys[i])
            if (client.getLastAccess() + clientTimeout < System.currentTimeMillis()) {
                // tachyon.print.ln("disconnect:"+client.getFtpConnectionData().key());
                if (client.isConnected()) {
                    try {
                        client.disconnect()
                    } catch (e: IOException) {
                    }
                }
                clients.remove(client.getFtpConnectionData().toString())
            }
        }
    }

    internal inner class FTPResourceClientCloser(private val provider: FTPResourceProvider) : Thread() {
        @Override
        fun run() {
            // tachyon.print.ln("closer start");
            do {
                sleepEL()
                provider.clean()
            } while (!clients.isEmpty())
            // tachyon.print.ln("closer stop");
        }

        private fun sleepEL() {
            try {
                sleep(provider.clientTimeout)
            } catch (e: InterruptedException) {
            }
        }
    }

    @get:Override
    val isAttributesSupported: Boolean
        get() = false

    @get:Override
    val isCaseSensitive: Boolean
        get() = true

    @get:Override
    val isModeSupported: Boolean
        get() = true

    @Override
    fun getArguments(): Map? {
        return arguments
    }

    @get:Override
    val separator: Char
        get() = '/'
}