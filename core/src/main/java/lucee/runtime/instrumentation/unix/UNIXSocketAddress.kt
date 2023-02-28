package lucee.runtime.instrumentation.unix

import java.io.File

/**
 * Describes an [InetSocketAddress] that actually uses AF_UNIX sockets instead of AF_INET.
 *
 * The ability to specify a port number is not specified by AF_UNIX sockets, but we need it
 * sometimes, for example for RMI-over-AF_UNIX.
 */
class UNIXSocketAddress(socketFile: File?, port: Int) : InetSocketAddress(0) {
    private val socketFile: String?

    /**
     * Creates a new [UNIXSocketAddress] that points to the AF_UNIX socket specified by the given
     * file.
     *
     * @param socketFile The socket to connect to.
     */
    constructor(socketFile: File?) : this(socketFile, 0) {}

    /**
     * Returns the (canonical) file path for this [UNIXSocketAddress].
     *
     * @return The file path.
     */
    fun getSocketFile(): String? {
        return socketFile
    }

    @Override
    override fun toString(): String {
        return getClass().getName().toString() + "[host=" + getHostName() + ";port=" + getPort() + ";file=" + socketFile + "]"
    }

    companion object {
        private const val serialVersionUID = 1L
    }

    /**
     * Creates a new [UNIXSocketAddress] that points to the AF_UNIX socket specified by the given
     * file, assigning the given port to it.
     *
     * @param socketFile The socket to connect to.
     * @param port The port associated with this socket, or `0` when no port should be assigned.
     */
    init {
        if (port != 0) {
            NativeUnixSocket.setPort1(this, port)
        }
        this.socketFile = socketFile.getCanonicalPath()
    }
}