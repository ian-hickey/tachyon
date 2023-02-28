package lucee.runtime.net.ftp

import java.io.IOException

class SSHManager {
    private var jschSSHChannel: JSch? = null
    private var strUserName: String? = null
    private var strConnectionIP: String? = null
    private var intConnectionPort: Int
    private var strPassword: String? = null
    private var sesConnection: Session? = null
    private var intTimeOut: Int
    @Throws(JSchException::class)
    private fun doCommonConstructorActions(userName: String?, password: String?, connectionIP: String?, knownHostsFileName: String?) {
        jschSSHChannel = JSch()
        jschSSHChannel.setKnownHosts(knownHostsFileName)
        strUserName = userName
        strPassword = password
        strConnectionIP = connectionIP
    }

    constructor(userName: String?, password: String?, connectionIP: String?, knownHostsFileName: String?) {
        doCommonConstructorActions(userName, password, connectionIP, knownHostsFileName)
        intConnectionPort = 22
        intTimeOut = 60000
    }

    constructor(userName: String?, password: String?, connectionIP: String?, knownHostsFileName: String?, connectionPort: Int) {
        doCommonConstructorActions(userName, password, connectionIP, knownHostsFileName)
        intConnectionPort = connectionPort
        intTimeOut = 60000
    }

    constructor(userName: String?, password: String?, connectionIP: String?, knownHostsFileName: String?, connectionPort: Int, timeOutMilliseconds: Int) {
        doCommonConstructorActions(userName, password, connectionIP, knownHostsFileName)
        intConnectionPort = connectionPort
        intTimeOut = timeOutMilliseconds
    }

    fun connect(): String? {
        var errorMessage: String? = null
        try {
            sesConnection = jschSSHChannel.getSession(strUserName, strConnectionIP, intConnectionPort)
            sesConnection.setPassword(strPassword)
            // UNCOMMENT THIS FOR TESTING PURPOSES, BUT DO NOT USE IN PRODUCTION
            // sesConnection.setConfig("StrictHostKeyChecking", "no");
            sesConnection.connect(intTimeOut)
        } catch (jschX: JSchException) {
            errorMessage = jschX.getMessage()
        }
        return errorMessage
    }

    @Throws(JSchException::class, IOException::class)
    fun sendCommand(command: String?): String? {
        val outputBuffer = StringBuilder()
        val channel: Channel = sesConnection.openChannel("exec")
        (channel as ChannelExec).setCommand(command)
        val commandOutput: InputStream = channel.getInputStream()
        channel.connect()
        var readByte: Int = commandOutput.read()
        while (readByte != -0x1) {
            outputBuffer.append(readByte.toChar())
            readByte = commandOutput.read()
        }
        channel.disconnect()
        return outputBuffer.toString()
    }

    fun close() {
        sesConnection.disconnect()
    }
}