package tachyon.runtime.instrumentation.unix

import java.io.IOException

/**
 * Implementation of an AF_UNIX domain socket.
 */
class UNIXSocket private constructor(impl: UNIXSocketImpl?) : Socket(impl) {
    var impl: UNIXSocketImpl? = null
    var addr: UNIXSocketAddress? = null

    /**
     * Binds this [UNIXSocket] to the given bindpoint. Only bindpoints of the type
     * [UNIXSocketAddress] are supported.
     */
    @Override
    @Throws(IOException::class)
    fun bind(bindpoint: SocketAddress?) {
        super.bind(bindpoint)
        addr = bindpoint
    }

    @Override
    @Throws(IOException::class)
    fun connect(endpoint: SocketAddress?) {
        connect(endpoint, 0)
    }

    @Override
    @Throws(IOException::class)
    fun connect(endpoint: SocketAddress?, timeout: Int) {
        if (endpoint !is UNIXSocketAddress) {
            throw IOException("Can only connect to endpoints of type " + UNIXSocketAddress::class.java.getName())
        }
        impl.connect(endpoint, timeout)
        addr = endpoint
        NativeUnixSocket.setConnected(this)
    }

    @Override
    override fun toString(): String {
        return if (isConnected()) {
            "AFUNIXSocket[fd=" + impl!!.getFD().toString() + ";path=" + addr!!.getSocketFile().toString() + "]"
        } else "AFUNIXSocket[unconnected]"
    }

    companion object {
        /**
         * Creates a new, unbound [UNIXSocket].
         *
         * This "default" implementation is a bit "lenient" with respect to the specification.
         *
         * In particular, we ignore calls to [Socket.getTcpNoDelay] and
         * [Socket.setTcpNoDelay].
         *
         * @return A new, unbound socket.
         */
        @Throws(IOException::class)
        fun newInstance(): UNIXSocket? {
            val impl: UNIXSocketImpl = Lenient()
            val instance = UNIXSocket(impl)
            instance.impl = impl
            return instance
        }

        /**
         * Creates a new, unbound, "strict" [UNIXSocket].
         *
         * This call uses an implementation that tries to be closer to the specification than
         * [.newInstance], at least for some cases.
         *
         * @return A new, unbound socket.
         */
        @Throws(IOException::class)
        fun newStrictInstance(): UNIXSocket? {
            val impl = UNIXSocketImpl()
            val instance = UNIXSocket(impl)
            instance.impl = impl
            return instance
        }

        /**
         * Creates a new [UNIXSocket] and connects it to the given [UNIXSocketAddress].
         *
         * @param addr The address to connect to.
         * @return A new, connected socket.
         */
        @Throws(IOException::class)
        fun connectTo(addr: UNIXSocketAddress?): UNIXSocket? {
            val socket = newInstance()
            socket!!.connect(addr)
            return socket
        }

        /**
         * Returns `true` iff [UNIXSocket]s are supported by the current Java VM.
         *
         * To support [UNIXSocket]s, a custom JNI library must be loaded that is supplied with
         * *junixsocket*.
         *
         * @return `true` iff supported.
         */
        fun isSupported(): Boolean {
            return NativeUnixSocket.isLoaded()
        }
    }

    init {
        try {
            NativeUnixSocket.setCreated(this)
        } catch (e: UnsatisfiedLinkError) {
            e.printStackTrace()
        }
    }
}