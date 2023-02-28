package tachyon.runtime.instrumentation.unix

import java.io.IOException

/**
 * The server part of an AF_UNIX domain socket.
 */
class UNIXServerSocket protected constructor() : ServerSocket() {
    private val implementation: UNIXSocketImpl?
    private var boundEndpoint: UNIXSocketAddress? = null
    private val shutdownThread: Thread? = object : Thread() {
        @Override
        fun run() {
            try {
                if (boundEndpoint != null) {
                    NativeUnixSocket.unlink(boundEndpoint.getSocketFile())
                }
            } catch (e: IOException) {
                // ignore
            }
        }
    }

    @Override
    @Throws(IOException::class)
    fun bind(endpoint: SocketAddress?, backlog: Int) {
        if (isClosed()) {
            throw SocketException("Socket is closed")
        }
        if (isBound()) {
            throw SocketException("Already bound")
        }
        if (endpoint !is UNIXSocketAddress) {
            throw IOException("Can only bind to endpoints of type " + UNIXSocketAddress::class.java.getName())
        }
        implementation.bind(backlog, endpoint)
        boundEndpoint = endpoint
    }

    @Override
    fun isBound(): Boolean {
        return boundEndpoint != null
    }

    @Override
    @Throws(IOException::class)
    fun accept(): Socket? {
        if (isClosed()) {
            throw SocketException("Socket is closed")
        }
        val `as`: UNIXSocket = UNIXSocket.newInstance()
        implementation!!.accept(`as`!!.impl)
        `as`!!.addr = boundEndpoint
        NativeUnixSocket.setConnected(`as`)
        return `as`
    }

    @Override
    override fun toString(): String {
        return if (!isBound()) {
            "AFUNIXServerSocket[unbound]"
        } else "AFUNIXServerSocket[" + boundEndpoint!!.getSocketFile().toString() + "]"
    }

    @Override
    @Throws(IOException::class)
    fun close() {
        if (isClosed()) {
            return
        }
        super.close()
        implementation!!.close()
        if (boundEndpoint != null) {
            NativeUnixSocket.unlink(boundEndpoint.getSocketFile())
        }
        try {
            Runtime.getRuntime().removeShutdownHook(shutdownThread)
        } catch (e: IllegalStateException) {
            // ignore
        }
    }

    companion object {
        /**
         * Returns a new, unbound AF_UNIX [ServerSocket].
         *
         * @return The new, unbound [UNIXServerSocket].
         */
        @Throws(IOException::class)
        fun newInstance(): UNIXServerSocket? {
            return UNIXServerSocket()
        }

        /**
         * Returns a new AF_UNIX [ServerSocket] that is bound to the given [UNIXSocketAddress].
         *
         * @return The new, unbound [UNIXServerSocket].
         */
        @Throws(IOException::class)
        fun bindOn(addr: UNIXSocketAddress?): UNIXServerSocket? {
            val socket = newInstance()
            socket!!.bind(addr)
            return socket
        }

        fun isSupported(): Boolean {
            return NativeUnixSocket.isLoaded()
        }
    }

    init {
        implementation = UNIXSocketImpl()
        NativeUnixSocket.initServerImpl(this, implementation)
        Runtime.getRuntime().addShutdownHook(shutdownThread)
        NativeUnixSocket.setCreatedServer(this)
    }
}