package tachyon.runtime.instrumentation.unix

import java.io.FileDescriptor

/**
 * The Java-part of the [UNIXSocket] implementation.
 */
class UNIXSocketImpl : SocketImpl() {
    private var socketFile: String? = null
    private var closed = false
    private var bound = false
    private var connected = false
    private var closedInputStream = false
    private var closedOutputStream = false
    private val `in`: AFUNIXInputStream? = AFUNIXInputStream()
    private val out: AFUNIXOutputStream? = AFUNIXOutputStream()
    fun getFD(): FileDescriptor? {
        return fd
    }

    @Override
    @Throws(IOException::class)
    fun accept(socket: SocketImpl?) {
        val si = socket as UNIXSocketImpl?
        NativeUnixSocket.accept(socketFile, fd, si.fd)
        si!!.socketFile = socketFile
        si.connected = true
    }

    @Override
    @Throws(IOException::class)
    protected fun available(): Int {
        return NativeUnixSocket.available(fd)
    }

    @Throws(IOException::class)
    protected fun bind(addr: SocketAddress?) {
        bind(0, addr)
    }

    @Throws(IOException::class)
    protected fun bind(backlog: Int, addr: SocketAddress?) {
        if (addr !is UNIXSocketAddress) {
            throw SocketException("Cannot bind to this type of address: " + addr.getClass())
        }
        val socketAddress: UNIXSocketAddress? = addr
        socketFile = socketAddress!!.getSocketFile()
        NativeUnixSocket.bind(socketFile, fd, backlog)
        bound = true
        this.localport = socketAddress.getPort()
    }

    @Override
    @SuppressWarnings("hiding")
    @Throws(IOException::class)
    protected fun bind(host: InetAddress?, port: Int) {
        throw SocketException("Cannot bind to this type of address: " + InetAddress::class.java)
    }

    @Throws(IOException::class)
    private fun checkClose() {
        if (closedInputStream && closedOutputStream) {
            // close();
        }
    }

    @Override
    @Synchronized
    @Throws(IOException::class)
    fun close() {
        if (closed) {
            return
        }
        closed = true
        if (fd.valid()) {
            NativeUnixSocket.shutdown(fd, SHUT_RD_WR)
            NativeUnixSocket.close(fd)
        }
        if (bound) {
            NativeUnixSocket.unlink(socketFile)
        }
        connected = false
    }

    @Override
    @SuppressWarnings("hiding")
    @Throws(IOException::class)
    protected fun connect(host: String?, port: Int) {
        throw SocketException("Cannot bind to this type of address: " + InetAddress::class.java)
    }

    @Override
    @SuppressWarnings("hiding")
    @Throws(IOException::class)
    protected fun connect(address: InetAddress?, port: Int) {
        throw SocketException("Cannot bind to this type of address: " + InetAddress::class.java)
    }

    @Override
    @Throws(IOException::class)
    protected fun connect(addr: SocketAddress?, timeout: Int) {
        if (addr !is UNIXSocketAddress) {
            throw SocketException("Cannot bind to this type of address: " + addr.getClass())
        }
        val socketAddress: UNIXSocketAddress? = addr
        socketFile = socketAddress!!.getSocketFile()
        NativeUnixSocket.connect(socketFile, fd)
        this.address = socketAddress.getAddress()
        this.port = socketAddress.getPort()
        this.localport = 0
        connected = true
    }

    @Override
    @Throws(IOException::class)
    protected fun create(stream: Boolean) {
    }

    @Override
    @Throws(IOException::class)
    protected fun getInputStream(): InputStream? {
        if (!connected && !bound) {
            throw IOException("Not connected/not bound")
        }
        return `in`
    }

    @Override
    @Throws(IOException::class)
    protected fun getOutputStream(): OutputStream? {
        if (!connected && !bound) {
            throw IOException("Not connected/not bound")
        }
        return out
    }

    @Override
    @Throws(IOException::class)
    protected fun listen(backlog: Int) {
        NativeUnixSocket.listen(fd, backlog)
    }

    @Override
    @Throws(IOException::class)
    protected fun sendUrgentData(data: Int) {
        NativeUnixSocket.write(fd, byteArrayOf((data and 0xFF).toByte()), 0, 1)
    }

    private inner class AFUNIXInputStream : InputStream() {
        private var streamClosed = false
        @Override
        @Throws(IOException::class)
        fun read(buf: ByteArray?, off: Int, len: Int): Int {
            var len = len
            if (streamClosed) {
                throw IOException("This InputStream has already been closed.")
            }
            if (len == 0) {
                return 0
            }
            val maxRead = buf!!.size - off
            if (len > maxRead) {
                len = maxRead
            }
            return try {
                NativeUnixSocket.read(fd, buf, off, len)
            } catch (e: IOException) {
                throw IOException(e.getMessage().toString() + " at " + this@UNIXSocketImpl.toString()).initCause(e) as IOException
            }
        }

        @Override
        @Throws(IOException::class)
        fun read(): Int {
            val buf1 = ByteArray(1)
            val numRead = read(buf1, 0, 1)
            return if (numRead <= 0) {
                -1
            } else {
                buf1[0] and 0xFF
            }
        }

        @Override
        @Throws(IOException::class)
        fun close() {
            if (streamClosed) {
                return
            }
            streamClosed = true
            if (fd.valid()) {
                NativeUnixSocket.shutdown(fd, SHUT_RD)
            }
            closedInputStream = true
            checkClose()
        }

        @Override
        @Throws(IOException::class)
        fun available(): Int {
            return NativeUnixSocket.available(fd)
        }
    }

    private inner class AFUNIXOutputStream : OutputStream() {
        private var streamClosed = false
        @Override
        @Throws(IOException::class)
        fun write(oneByte: Int) {
            val buf1 = byteArrayOf(oneByte.toByte())
            write(buf1, 0, 1)
        }

        @Override
        @Throws(IOException::class)
        fun write(buf: ByteArray?, off: Int, len: Int) {
            var off = off
            var len = len
            if (streamClosed) {
                throw UNIXSocketException("This OutputStream has already been closed.")
            }
            if (len > buf!!.size - off) {
                throw IndexOutOfBoundsException()
            }
            try {
                while (len > 0 && !Thread.interrupted()) {
                    val written: Int = NativeUnixSocket.write(fd, buf, off, len)
                    if (written == -1) {
                        throw IOException("Unspecific error while writing")
                    }
                    len -= written
                    off += written
                }
            } catch (e: IOException) {
                throw IOException(e.getMessage().toString() + " at " + this@UNIXSocketImpl.toString()).initCause(e) as IOException
            }
        }

        @Override
        @Throws(IOException::class)
        fun close() {
            if (streamClosed) {
                return
            }
            streamClosed = true
            if (fd.valid()) {
                NativeUnixSocket.shutdown(fd, SHUT_WR)
            }
            closedOutputStream = true
            checkClose()
        }
    }

    @Override
    override fun toString(): String {
        return super.toString() + "[fd=" + fd + "; file=" + socketFile + "; connected=" + connected + "; bound=" + bound + "]"
    }

    @Override
    @Throws(SocketException::class)
    fun getOption(optID: Int): Object? {
        return try {
            when (optID) {
                SocketOptions.SO_KEEPALIVE, SocketOptions.TCP_NODELAY -> if (NativeUnixSocket.getSocketOptionInt(fd, optID) !== 0) true else false
                SocketOptions.SO_LINGER, SocketOptions.SO_TIMEOUT, SocketOptions.SO_RCVBUF, SocketOptions.SO_SNDBUF -> NativeUnixSocket.getSocketOptionInt(fd, optID)
                else -> throw UNIXSocketException("Unsupported option: $optID")
            }
        } catch (e: UNIXSocketException) {
            throw e
        } catch (e: Exception) {
            throw UNIXSocketException("Error while getting option", e)
        }
    }

    @Override
    @Throws(SocketException::class)
    fun setOption(optID: Int, value: Object?) {
        try {
            when (optID) {
                SocketOptions.SO_LINGER -> {
                    if (value is Boolean) {
                        val b = (value as Boolean?)!!
                        if (b) {
                            throw SocketException("Only accepting Boolean.FALSE here")
                        }
                        NativeUnixSocket.setSocketOptionInt(fd, optID, -1)
                        return
                    }
                    NativeUnixSocket.setSocketOptionInt(fd, optID, expectInteger(value))
                    return
                }
                SocketOptions.SO_RCVBUF, SocketOptions.SO_SNDBUF, SocketOptions.SO_TIMEOUT -> {
                    NativeUnixSocket.setSocketOptionInt(fd, optID, expectInteger(value))
                    return
                }
                SocketOptions.SO_KEEPALIVE, SocketOptions.TCP_NODELAY -> {
                    NativeUnixSocket.setSocketOptionInt(fd, optID, expectBoolean(value))
                    return
                }
                else -> throw UNIXSocketException("Unsupported option: $optID")
            }
        } catch (e: UNIXSocketException) {
            throw e
        } catch (e: Exception) {
            throw UNIXSocketException("Error while setting option", e)
        }
    }

    @Override
    @Throws(IOException::class)
    protected fun shutdownInput() {
        if (!closed && fd.valid()) {
            NativeUnixSocket.shutdown(fd, SHUT_RD)
        }
    }

    @Override
    @Throws(IOException::class)
    protected fun shutdownOutput() {
        if (!closed && fd.valid()) {
            NativeUnixSocket.shutdown(fd, SHUT_WR)
        }
    }

    /**
     * Changes the behavior to be somewhat lenient with respect to the specification.
     *
     * In particular, we ignore calls to [Socket.getTcpNoDelay] and
     * [Socket.setTcpNoDelay].
     */
    internal class Lenient : UNIXSocketImpl() {
        @Override
        @Throws(SocketException::class)
        override fun setOption(optID: Int, value: Object?) {
            try {
                super.setOption(optID, value)
            } catch (e: SocketException) {
                when (optID) {
                    SocketOptions.TCP_NODELAY -> return
                    else -> throw e
                }
            }
        }

        @Override
        @Throws(SocketException::class)
        override fun getOption(optID: Int): Object? {
            return try {
                super.getOption(optID)
            } catch (e: SocketException) {
                when (optID) {
                    SocketOptions.TCP_NODELAY, SocketOptions.SO_KEEPALIVE -> false
                    else -> throw e
                }
            }
        }
    }

    companion object {
        private const val SHUT_RD = 0
        private const val SHUT_WR = 1
        private const val SHUT_RD_WR = 2
        @Throws(SocketException::class)
        private fun expectInteger(value: Object?): Int {
            return try {
                value as Integer?
            } catch (e: ClassCastException) {
                throw UNIXSocketException("Unsupported value: $value", e)
            } catch (e: NullPointerException) {
                throw UNIXSocketException("Value must not be null", e)
            }
        }

        @Throws(SocketException::class)
        private fun expectBoolean(value: Object?): Int {
            return try {
                if ((value as Boolean?).booleanValue()) 1 else 0
            } catch (e: ClassCastException) {
                throw UNIXSocketException("Unsupported value: $value", e)
            } catch (e: NullPointerException) {
                throw UNIXSocketException("Value must not be null", e)
            }
        }
    }

    init {
        this.fd = FileDescriptor()
    }
}