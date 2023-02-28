package lucee.runtime.instrumentation.unix

import java.io.FileDescriptor

/**
 * JNI connector to native JNI C code.
 *
 */
internal object NativeUnixSocket {
    private var loaded = false
    fun isLoaded(): Boolean {
        return loaded
    }

    fun checkSupported() {}
    @Throws(IOException::class)
    external fun bind(socketFile: String?, fd: FileDescriptor?, backlog: Int)
    @Throws(IOException::class)
    external fun listen(fd: FileDescriptor?, backlog: Int)
    @Throws(IOException::class)
    external fun accept(socketFile: String?, fdServer: FileDescriptor?, fd: FileDescriptor?)
    @Throws(IOException::class)
    external fun connect(socketFile: String?, fd: FileDescriptor?)
    @Throws(IOException::class)
    external fun read(fd: FileDescriptor?, buf: ByteArray?, off: Int, len: Int): Int
    @Throws(IOException::class)
    external fun write(fd: FileDescriptor?, buf: ByteArray?, off: Int, len: Int): Int
    @Throws(IOException::class)
    external fun close(fd: FileDescriptor?)
    @Throws(IOException::class)
    external fun shutdown(fd: FileDescriptor?, mode: Int)
    @Throws(IOException::class)
    external fun getSocketOptionInt(fd: FileDescriptor?, optionId: Int): Int
    @Throws(IOException::class)
    external fun setSocketOptionInt(fd: FileDescriptor?, optionId: Int, value: Int)
    @Throws(IOException::class)
    external fun unlink(socketFile: String?)
    @Throws(IOException::class)
    external fun available(fd: FileDescriptor?): Int
    external fun initServerImpl(serverSocket: UNIXServerSocket?, impl: UNIXSocketImpl?)
    external fun setCreated(socket: UNIXSocket?)
    external fun setConnected(socket: UNIXSocket?)
    external fun setBound(socket: UNIXSocket?)
    external fun setCreatedServer(socket: UNIXServerSocket?)
    external fun setBoundServer(socket: UNIXServerSocket?)
    external fun setPort(addr: UNIXSocketAddress?, port: Int)
    @Throws(UNIXSocketException::class)
    fun setPort1(addr: UNIXSocketAddress?, port: Int) {
        if (port < 0) {
            throw IllegalArgumentException("port out of range:$port")
        }
        var setOk = false
        try {
            val holderField: Field = InetSocketAddress::class.java.getDeclaredField("holder")
            if (holderField != null) {
                holderField.setAccessible(true)
                val holder: Object = holderField.get(addr)
                if (holder != null) {
                    val portField: Field = holder.getClass().getDeclaredField("port")
                    if (portField != null) {
                        portField.setAccessible(true)
                        portField.set(holder, port)
                        setOk = true
                    }
                }
            } else {
                setPort(addr, port)
            }
        } catch (e: RuntimeException) {
            throw e
        } catch (e: Exception) {
            if (e is UNIXSocketException) {
                throw e
            }
            throw UNIXSocketException("Could not set port", e)
        }
        if (!setOk) {
            throw UNIXSocketException("Could not set port")
        }
    }

    init {
        try {
            Class.forName("org.newsclub.net.unix.NarSystem").getMethod("loadLibrary").invoke(null)
        } catch (e: ClassNotFoundException) {
            throw IllegalStateException("""Could not find NarSystem class.

*** ECLIPSE USERS ***
If you're running from within Eclipse, please try closing the "junixsocket-native-common" project
""", e)
        } catch (e: Exception) {
            throw IllegalStateException(e)
        }
        loaded = true
    }
}