package tachyon.runtime.instrumentation.unix

import java.net.SocketException

/**
 * Something went wrong with the communication to a Unix socket.
 */
class UNIXSocketException(reason: String?, private val socketFile: String?) : SocketException(reason) {
    constructor(reason: String?) : this(reason, null as String?) {}
    constructor(reason: String?, cause: Throwable?) : this(reason, null as String?) {
        initCause(cause)
    }

    @Override
    override fun toString(): String {
        return if (socketFile == null) {
            super.toString()
        } else {
            super.toString() + " (socket: " + socketFile + ")"
        }
    }

    companion object {
        private const val serialVersionUID = 1L
    }
}