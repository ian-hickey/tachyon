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
package lucee.runtime.net.ftp

import java.io.IOException

/**
 * Pool of FTP Client
 */
class FTPPoolImpl {
    var wraps: Map<String?, FTPWrap?>? = HashMap<String?, FTPWrap?>()

    @Throws(IOException::class, ApplicationException::class)
    operator fun get(conn: FTPConnection?): AFTPClient? {
        val client: AFTPClient = _get(conn)!!.getClient()
                ?: throw ApplicationException("can't connect to server [" + conn.getServer().toString() + "]")
        FTPWrap.setConnectionSettings(client, conn)
        return client
    }

    /**
     * returns a client from given connection
     *
     * @param conn
     * @return
     * @return matching wrap
     * @throws IOException
     * @throws ApplicationException
     */
    @Throws(IOException::class, ApplicationException::class)
    protected fun _get(conn: FTPConnection?): FTPWrap? {
        var wrap: FTPWrap? = null
        if (!conn!!.hasLoginData()) {
            if (StringUtil.isEmpty(conn.getName())) {
                throw ApplicationException("can't connect ftp server, missing connection definition")
            }
            wrap = wraps!![conn.getName()]
            if (wrap == null) {
                throw ApplicationException("can't connect ftp server, missing connection [" + conn.getName().toString() + "]")
            } else if (!wrap.getClient()!!.isConnected() || wrap.getConnection().getTransferMode() !== conn.getTransferMode()) {
                wrap.reConnect(conn.getTransferMode())
            }
            return wrap
        }
        val name = if (conn!!.hasName()) conn.getName() else "__noname__"
        wrap = wraps!![name]
        if (wrap != null) {
            if (conn!!.loginEquals(wrap.getConnection())) {
                return _get(FTPConnectionImpl(name, null, null, null, conn.getPort(), conn.getTimeout(), conn.getTransferMode(), conn!!.isPassive(), conn.getProxyServer(),
                        conn.getProxyPort(), conn.getProxyUser(), conn.getProxyPassword(), conn.getFingerprint(), conn.getStopOnError(), conn!!.secure()))
            }
            disconnect(wrap.getClient())
        }
        wrap = FTPWrap(conn)
        wraps.put(name, wrap)
        return wrap
    }

    /**
     * disconnect a client
     *
     * @param client
     */
    private fun disconnect(client: AFTPClient?) {
        try {
            if (client != null && client.isConnected()) {
                client.quit()
                client.disconnect()
            }
        } catch (ioe: IOException) {
        }
    }

    fun remove(conn: FTPConnection?): AFTPClient? {
        return remove(conn.getName())
    }

    fun remove(name: String?): AFTPClient? {
        val wrap: FTPWrap = wraps.remove(name) ?: return null
        val client: AFTPClient = wrap.getClient()
        disconnect(client)
        return client
    }

    fun clear() {
        if (!wraps!!.isEmpty()) {
            val it: Iterator<Entry<String?, FTPWrap?>?> = wraps.entrySet().iterator()
            while (it.hasNext()) {
                try {
                    val entry: Entry<String?, FTPWrap?>? = it.next()
                    val wrap: FTPWrap = entry.getValue()
                    if (wrap != null && wrap.getClient()!!.isConnected()) wrap.getClient()!!.disconnect()
                } catch (e: IOException) {
                }
            }
            wraps.clear()
        }
    }
}