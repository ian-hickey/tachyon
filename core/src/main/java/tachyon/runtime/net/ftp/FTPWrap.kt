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
package tachyon.runtime.net.ftp

import java.io.IOException

/**
 * Wrap a Client and a Connection
 */
class FTPWrap(connection: FTPConnection?) {
    private val conn: FTPConnection?
    private var client: AFTPClient? = null
    private val address: InetAddress?
    /**
     * @return the lastAccess
     */
    /**
     * @param lastAccess the lastAccess to set
     */
    var lastAccess: Long = 0

    /**
     * @return Returns the connection.
     */
    val connection: tachyon.runtime.net.ftp.FTPConnection?
        get() = conn

    /**
     * @return Returns the client.
     */
    fun getClient(): AFTPClient? {
        return client
    }

    /**
     * @throws IOException
     */
    @Throws(IOException::class)
    fun reConnect() {
        try {
            if (client != null && client.isConnected()) client.disconnect()
        } catch (ioe: IOException) {
        }
        connect()
    }

    @Throws(IOException::class)
    fun reConnect(transferMode: Short) {
        if (transferMode != conn.getTransferMode()) (conn as FTPConnectionImpl?).setTransferMode(transferMode)
        reConnect()
    }

    /**
     * connects the client
     *
     * @throws IOException
     */
    @Throws(IOException::class)
    private fun connect() {
        client = AFTPClient.getInstance(conn!!.secure(), address, conn.getPort(), conn.getUsername(), conn.getPassword(), conn.getFingerprint(), conn.getStopOnError())
        if (client is SFTPClientImpl && conn.getKey() != null) {
            (client as SFTPClientImpl?)!!.setSshKey(conn.getKey(), conn.getPassphrase())
        }
        setConnectionSettings(client, conn)

        // transfer mode
        if (conn.getTransferMode() === FTPConstant.TRANSFER_MODE_ASCCI) getClient()!!.setFileType(FTP.ASCII_FILE_TYPE) else if (conn.getTransferMode() === FTPConstant.TRANSFER_MODE_BINARY) getClient()!!.setFileType(FTP.BINARY_FILE_TYPE)

        // Connect
        try {
            Proxy.start(conn.getProxyServer(), conn.getProxyPort(), conn.getProxyUser(), conn.getProxyPassword())
            client!!.connect()
        } finally {
            Proxy.end()
        }
    }

    companion object {
        fun setConnectionSettings(client: AFTPClient?, conn: FTPConnection?) {
            if (client == null) return

            // timeout
            client.setTimeout(conn.getTimeout() * 1000)

            // passive/active Mode
            val mode: Int = client.getDataConnectionMode()
            if (conn!!.isPassive()) {
                if (FTPClient.PASSIVE_LOCAL_DATA_CONNECTION_MODE !== mode) client.enterLocalPassiveMode()
            } else {
                if (FTPClient.ACTIVE_LOCAL_DATA_CONNECTION_MODE !== mode) client.enterLocalActiveMode()
            }
        }
    }

    /**
     *
     * @param connection
     * @throws IOException
     */
    init {
        conn = connection
        address = InetAddress.getByName(connection.getServer())
        connect()
    }
}