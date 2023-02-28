package tachyon.runtime.net.ftp

import java.io.IOException

class FTPClientImpl : AFTPClient {
    private var client: FTPClient?
    private var host: InetAddress? = null
    private var port = 0
    private var username: String? = null
    private var password: String? = null
    private var stopOnError = false

    constructor(client: FTPClient?) {
        this.client = client
    }

    internal constructor() {
        client = FTPClient()
    }

    @Override
    @Throws(SocketException::class, IOException::class)
    override fun init(host: InetAddress?, port: Int, username: String?, password: String?, fingerprint: String?, stopOnError: Boolean) {
        this.host = host
        this.port = port
        this.username = username
        this.password = password
        this.stopOnError = stopOnError
    }

    @Override
    @Throws(SocketException::class, IOException::class)
    override fun connect() {
        client.connect(host, port)
        if (!StringUtil.isEmpty(username)) client.login(username, password)
    }

    @Override
    @Throws(IOException::class)
    override fun rename(from: String?, to: String?): Boolean {
        return client.rename(from, to)
    }

    @get:Override
    override val replyCode: Int
        get() = client.getReplyCode()

    @get:Override
    override val replyString: String?
        get() = client.getReplyString()

    @Override
    @Throws(IOException::class)
    override fun changeWorkingDirectory(pathname: String?): Boolean {
        return client.changeWorkingDirectory(pathname)
    }

    @Override
    @Throws(IOException::class)
    override fun makeDirectory(pathname: String?): Boolean {
        return client.makeDirectory(pathname)
    }

    @Override
    @Throws(IOException::class)
    override fun listFiles(pathname: String?): Array<FTPFile?>? {
        return client.listFiles(pathname)
    }

    @Override
    @Throws(IOException::class)
    override fun removeDirectory(pathname: String?): Boolean {
        return client.removeDirectory(pathname)
    }

    @Override
    @Throws(IOException::class)
    override fun setFileType(fileType: Int): Boolean {
        return client.setFileType(toFTPClientFileType(fileType))
    }

    private fun toFTPClientFileType(fileType: Int): Int {
        return if (fileType == FILE_TYPE_BINARY) FTP.BINARY_FILE_TYPE else FTP.ASCII_FILE_TYPE
    }

    @Override
    @Throws(IOException::class)
    override fun retrieveFile(remote: String?, local: OutputStream?): Boolean {
        return client.retrieveFile(remote, local)
    }

    @Override
    @Throws(IOException::class)
    override fun storeFile(remote: String?, local: InputStream?): Boolean {
        return client.storeFile(remote, local)
    }

    @Override
    @Throws(IOException::class)
    override fun deleteFile(pathname: String?): Boolean {
        return client.deleteFile(pathname)
    }

    @Override
    @Throws(IOException::class)
    override fun printWorkingDirectory(): String? {
        return client.printWorkingDirectory()
    }

    @get:Override
    override val prefix: String?
        get() = "ftp"

    @get:Override
    override val remoteAddress: InetAddress?
        get() = client.getRemoteAddress()

    @get:Override
    override val isConnected: Boolean
        get() = client.isConnected()

    @Override
    @Throws(IOException::class)
    override fun quit(): Int {
        return client.quit()
    }

    @Override
    @Throws(IOException::class)
    override fun disconnect() {
        client.disconnect()
    }

    @get:Override
    override val dataConnectionMode: Int
        get() = client.getDataConnectionMode()

    @Override
    override fun enterLocalPassiveMode() {
        client.enterLocalPassiveMode()
    }

    @Override
    override fun enterLocalActiveMode() {
        client.enterLocalActiveMode()
    }

    @get:Override
    override val isPositiveCompletion: Boolean
        get() = FTPReply.isPositiveCompletion(client.getReplyCode())

    @Override
    @Throws(IOException::class)
    override fun directoryExists(pathname: String?): Boolean {
        var pwd: String? = null
        return try {
            pwd = client.printWorkingDirectory()
            client.changeWorkingDirectory(pathname)
        } finally {
            if (pwd != null) client.changeWorkingDirectory(pwd)
        }
    }

    @Override
    override fun setTimeout(timeout: Int) {
        client.setDataTimeout(timeout)
        try {
            client.setSoTimeout(timeout)
        } catch (t: Throwable) {
            ExceptionUtil.rethrowIfNecessary(t)
        }
    }
}