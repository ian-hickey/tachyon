package tachyon.runtime.net.ftp

import java.io.IOException

class SFTPClientImpl internal constructor() : AFTPClient() {
    private val jsch: JSch?
    private var timeout = 60000
    private var session: Session? = null
    private var channelSftp: ChannelSftp? = null
    private var host: InetAddress? = null
    private var port = 0
    private var username: String? = null
    private var password: String? = null
    private var stopOnError = false
    private var fingerprint: String? = null

    @get:Override
    override var replyString: String? = null
        private set

    @get:Override
    override var replyCode = 0
        private set

    @get:Override
    override var isPositiveCompletion = false
        private set
    private var sshKey: String? = null
    private var passphrase: String? = null

    companion object {
        init {
            // set system property tachyon.debug.jsch=true to enable debug output from JSch
            if (Caster.toBooleanValue(SystemUtil.getSystemPropOrEnvVar("tachyon.debug.jsch", ""), false)) {
                JSch.setLogger(object : Logger() {
                    @Override
                    fun isEnabled(i: Int): Boolean {
                        return true
                    }

                    @Override
                    fun log(i: Int, s: String?) {
                        // System. out.println("JSch: " + s);
                    }
                })
            }
        }
    }

    @Override
    @Throws(SocketException::class, IOException::class)
    override fun init(host: InetAddress?, port: Int, username: String?, password: String?, fingerprint: String?, stopOnError: Boolean) {
        var port = port
        if (port < 1) port = 22
        this.host = host
        this.port = port
        this.username = username
        this.password = password
        this.fingerprint = fingerprint?.trim()
        this.stopOnError = stopOnError
    }

    fun setSshKey(sshKey: String?, passphrase: String?) {
        this.sshKey = sshKey
        this.passphrase = passphrase ?: ""
    }

    @Override
    @Throws(IOException::class)
    override fun connect() {
        try {
            session = jsch.getSession(username, host.getHostAddress(), port)
            session.setConfig("StrictHostKeyChecking", "no")
            if (password != null) session.setPassword(password)
            if (sshKey != null) jsch.addIdentity(sshKey, passphrase)
            if (timeout > 0) session.setTimeout(timeout)
            session.connect()
            val channel: Channel = session.openChannel("sftp")
            channel.connect()
            channelSftp = channel as ChannelSftp

            // check fingerprint
            if (!StringUtil.isEmpty(fingerprint)) {
                if (!fingerprint.equalsIgnoreCase(fingerprint())) {
                    disconnect()
                    throw IOException("given fingerprint is not a match.")
                }
            }
            handleSucess()
        } catch (e: JSchException) {
            handleFail(e, stopOnError)
        }
    }

    private fun fingerprint(): String? {
        return session.getHostKey().getFingerPrint(jsch)
    }

    @Override
    @Throws(IOException::class)
    override fun rename(from: String?, to: String?): Boolean {
        try {
            if (channelSftp == null) connect()
            channelSftp.rename(from, to)
            handleSucess()
            return true
        } catch (e: SftpException) {
            handleFail(e, stopOnError)
        }
        return false
    }

    @Override
    @Throws(IOException::class)
    override fun removeDirectory(pathname: String?): Boolean {
        try {
            if (channelSftp == null) connect()
            channelSftp.rmdir(pathname)
            handleSucess()
            return true
        } catch (ioe: SftpException) {
            handleFail(ioe, stopOnError)
        }
        return false
    }

    @Override
    @Throws(IOException::class)
    override fun makeDirectory(pathname: String?): Boolean {
        try {
            if (channelSftp == null) connect()
            channelSftp.mkdir(pathname)
            handleSucess()
            return true
        } catch (ioe: SftpException) {
            handleFail(ioe, stopOnError)
        }
        return false
    }

    @Override
    @Throws(IOException::class)
    override fun directoryExists(pathname: String?): Boolean {
        try {
            if (channelSftp == null) connect()
            val pwd: String = channelSftp.pwd()
            channelSftp.cd(pathname)
            channelSftp.cd(pwd) // we change it back to what it was
            handleSucess()
            return true
        } catch (e: SftpException) {
            /* do nothing */
        }
        return false
    }

    @Override
    @Throws(IOException::class)
    override fun changeWorkingDirectory(pathname: String?): Boolean {
        try {
            if (channelSftp == null) connect()
            channelSftp.cd(pathname)
            handleSucess()
            return true
        } catch (ioe: SftpException) {
            handleFail(ioe, stopOnError)
        }
        return false
    }

    @Override
    @Throws(IOException::class)
    override fun printWorkingDirectory(): String? {
        try {
            if (channelSftp == null) connect()
            val pwd: String = channelSftp.pwd()
            handleSucess()
            return pwd
        } catch (ioe: SftpException) {
            handleFail(ioe, stopOnError)
        }
        return null
    }

    @Override
    @Throws(IOException::class)
    override fun deleteFile(pathname: String?): Boolean {
        try {
            if (channelSftp == null) connect()
            channelSftp.rm(pathname)
            handleSucess()
            return true
        } catch (ioe: SftpException) {
            handleFail(ioe, stopOnError)
        }
        return false
    }

    @Override
    @Throws(IOException::class)
    override fun retrieveFile(remote: String?, local: OutputStream?): Boolean {
        var success = false
        try {
            if (channelSftp == null) connect()
            channelSftp.get(remote, local)
            handleSucess()
            success = true
        } catch (ioe: SftpException) {
            handleFail(ioe, stopOnError)
        }
        return success
    }

    @Override
    @Throws(IOException::class)
    override fun storeFile(remote: String?, local: InputStream?): Boolean {
        try {
            if (channelSftp == null) connect()
            channelSftp.put(local, remote) // TODO add progress monitor?
            handleSucess()
            return true
        } catch (ioe: SftpException) {
            handleFail(ioe, stopOnError)
        }
        return false
    }

    @Override
    @Throws(IOException::class)
    override fun listFiles(pathname: String?): Array<FTPFile?>? {
        var pathname = pathname
        pathname = cleanPath(pathname)
        val files: List<FTPFile?> = ArrayList<FTPFile?>()
        try {
            if (channelSftp == null) connect()
            val list: Vector = channelSftp.ls(pathname)
            val it: Iterator<ChannelSftp.LsEntry?> = list.iterator()
            var entry: ChannelSftp.LsEntry?
            var attrs: SftpATTRS
            var file: FTPFile?
            var fileName: String
            while (it.hasNext()) {
                entry = it.next()
                attrs = entry.getAttrs()
                fileName = entry.getFilename()
                if (fileName.equals(".") || fileName.equals("..")) continue
                file = FTPFile()
                files.add(file)
                // is dir
                file.setType(if (attrs.isDir()) FTPFile.DIRECTORY_TYPE else FTPFile.FILE_TYPE)
                file.setTimestamp(Caster.toCalendar(attrs.getMTime() * 1000L, null, Locale.ENGLISH))
                file.setSize(if (attrs.isDir()) 0 else attrs.getSize())
                FTPConstant.setPermission(file, attrs.getPermissions())
                file.setName(fileName)
            }
            handleSucess()
        } catch (e: SftpException) {
            handleFail(e, stopOnError)
        }
        return files.toArray(arrayOfNulls<FTPFile?>(files.size()))
    }

    private fun cleanPath(pathname: String?): String? {
        var pathname = pathname
        if (!pathname.endsWith("/")) pathname = pathname.toString() + "/"
        return pathname
    }

    @Override
    @Throws(IOException::class)
    override fun setFileType(fileType: Int): Boolean {
        // not used
        return true
    }

    @get:Override
    override val prefix: String?
        get() = "sftp"

    @get:Override
    override val remoteAddress: InetAddress?
        get() = host

    @get:Override
    override val isConnected: Boolean
        get() = if (channelSftp == null) false else channelSftp.isConnected()

    @Override
    @Throws(IOException::class)
    override fun quit(): Int {
        // do nothing
        return 0
    }

    @Override
    @Throws(IOException::class)
    override fun disconnect() {
        if (session != null && session.isConnected()) {
            session.disconnect()
            session = null
        }
    }

    @Override
    override fun setTimeout(timeout: Int) {
        this.timeout = timeout
        if (session != null) {
            try {
                session.setTimeout(timeout)
            } catch (e: JSchException) {
            }
        }
    }

    // not used
    @get:Override
    override val dataConnectionMode: Int
        get() =// not used
            -1

    @Override
    override fun enterLocalPassiveMode() {
        // not used
    }

    @Override
    override fun enterLocalActiveMode() {
        // not used
    }

    private fun handleSucess() {
        replyCode = 0
        replyString = "SSH_FX_OK successful completion of the operation"
        isPositiveCompletion = true
    }

    @Throws(IOException::class)
    private fun handleFail(e: Exception?, stopOnError: Boolean) {
        val msg = if (e.getMessage() == null) "" else e.getMessage()
        replyCode = if (StringUtil.indexOfIgnoreCase(msg, "AUTHENTICATION") !== -1 || StringUtil.indexOfIgnoreCase(msg, "PRIVATEKEY") !== -1) {
            51
        } else 82
        replyString = msg
        isPositiveCompletion = false
        if (stopOnError) {
            disconnect()
            if (e is IOException) throw e as IOException?
            throw IOException(e)
        } else LogUtil.log(null as PageContext?, "application", "ftp", e, Log.LEVEL_INFO)
    }

    init {
        jsch = JSch()
    }
}