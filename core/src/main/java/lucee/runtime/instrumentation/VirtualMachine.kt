package lucee.runtime.instrumentation

import java.io.File

/**
 *
 *
 * An implementation for attachment on a virtual machine. This interface mimics the tooling API's
 * virtual machine interface to allow for similar usage by [InstrumentationFactoryExternal]
 * where all calls are made via reflection such that this structural typing suffices for
 * interoperability.
 *
 *
 *
 * **Note**: Implementations are required to declare a static method `attach(String)`
 * returning an instance of a class that declares the methods defined by [VirtualMachine].
 *
 */
interface VirtualMachine {
    /**
     * Loads an agent into the represented virtual machine.
     *
     * @param jarFile The jar file to attach.
     * @param argument The argument to provide or `null` if no argument should be provided.
     * @throws IOException If an I/O exception occurs.
     */
    @SuppressWarnings("unused")
    @Throws(IOException::class)
    fun loadAgent(jarFile: String?, argument: String?)

    /**
     * Detaches this virtual machine representation.
     *
     * @throws IOException If an I/O exception occurs.
     */
    @SuppressWarnings("unused")
    @Throws(IOException::class)
    fun detach()

    /**
     * A virtual machine implementation for a HotSpot VM or any compatible VM.
     */
    abstract class ForHotSpot
    /**
     * Creates a new HotSpot-compatible VM implementation.
     *
     * @param processId The target process's id.
     */ protected constructor(
            /**
             * The target process's id.
             */
            protected val processId: String?) : VirtualMachine {
        @Override
        @Throws(IOException::class)
        override fun loadAgent(jarFile: String?, argument: String?) {
            connect()
            write(PROTOCOL_VERSION.getBytes(UTF_8))
            write(BLANK)
            write(LOAD_COMMAND.getBytes(UTF_8))
            write(BLANK)
            write(INSTRUMENT_COMMAND.getBytes(UTF_8))
            write(BLANK)
            write(Boolean.FALSE.toString().getBytes(UTF_8))
            write(BLANK)
            write((if (argument == null) jarFile else jarFile + ARGUMENT_DELIMITER + argument).getBytes(UTF_8))
            write(BLANK)
            var buffer = ByteArray(1)
            var stringBuilder: StringBuilder? = StringBuilder()
            var length: Int
            while (read(buffer).also { length = it } != -1) {
                if (length > 0) {
                    if (buffer[0] == 10) {
                        break
                    }
                    stringBuilder.append(buffer[0].toChar())
                }
            }
            when (Integer.parseInt(stringBuilder.toString())) {
                0 -> return
                101 -> throw IOException("Protocol mismatch with target VM")
                else -> {
                    buffer = ByteArray(1024)
                    stringBuilder = StringBuilder()
                    while (read(buffer).also { length = it } != -1) {
                        stringBuilder.append(String(buffer, 0, length, UTF_8))
                    }
                    throw IllegalStateException(stringBuilder.toString())
                }
            }
        }

        /**
         * Connects to the target VM.
         *
         * @throws IOException If an I/O exception occurs.
         */
        @Throws(IOException::class)
        protected abstract fun connect()

        /**
         * Reads from the communication channel.
         *
         * @param buffer The buffer to read into.
         * @return The amount of bytes read.
         * @throws IOException If an I/O exception occurs.
         */
        @Throws(IOException::class)
        protected abstract fun read(buffer: ByteArray?): Int

        /**
         * Writes to the communication channel.
         *
         * @param buffer The buffer to write from.
         * @throws IOException If an I/O exception occurs.
         */
        @Throws(IOException::class)
        protected abstract fun write(buffer: ByteArray?)

        /**
         * A virtual machine implementation for a HotSpot VM running on Unix.
         */
        class OnUnix(processId: String?, socket: Object?, attempts: Int, pause: Long, timeout: Long, timeUnit: TimeUnit?) : ForHotSpot(processId) {
            /**
             * The Unix socket to use for communication. The containing object is supposed to be an instance of
             * [UNIXSocket] which is however not set to avoid eager loading
             */
            private val socket: Object?

            /**
             * The number of attempts to connect.
             */
            private val attempts: Int

            /**
             * The time to pause between attempts.
             */
            private val pause: Long

            /**
             * The socket timeout.
             */
            private val timeout: Long

            /**
             * The time unit of the pause time.
             */
            private val timeUnit: TimeUnit?
            @Override
            @Throws(IOException::class)
            override fun connect() {
                val socketFile = File(TEMPORARY_DIRECTORY, SOCKET_FILE_PREFIX + processId)
                if (!socketFile.exists()) {
                    val target = ATTACH_FILE_PREFIX + processId
                    val path = "/proc/$processId/cwd/$target"
                    var attachFile: File? = File(path)
                    try {
                        if (!attachFile.createNewFile() && !attachFile.isFile()) {
                            throw IllegalStateException("Could not create attach file: $attachFile")
                        }
                    } catch (ignored: IOException) {
                        attachFile = File(TEMPORARY_DIRECTORY, target)
                        if (!attachFile.createNewFile() && !attachFile.isFile()) {
                            throw IllegalStateException("Could not create attach file: $attachFile")
                        }
                    }
                    try {
                        // The HotSpot attachment API attempts to send the signal to all children of a process
                        val process: Process = Runtime.getRuntime().exec("kill -3 $processId")
                        var attempts = attempts
                        var killed = false
                        do {
                            try {
                                if (process.exitValue() !== 0) {
                                    throw IllegalStateException("Error while sending signal to target VM: $processId")
                                }
                                killed = true
                                break
                            } catch (ignored: IllegalThreadStateException) {
                                attempts -= 1
                                Thread.sleep(timeUnit.toMillis(pause))
                            }
                        } while (attempts > 0)
                        if (!killed) {
                            throw IllegalStateException("Target VM did not respond to signal: $processId")
                        }
                        attempts = this.attempts
                        while (attempts-- > 0 && !socketFile.exists()) {
                            Thread.sleep(timeUnit.toMillis(pause))
                        }
                        if (!socketFile.exists()) {
                            throw IllegalStateException("Target VM did not respond: $processId")
                        }
                    } catch (exception: InterruptedException) {
                        throw IllegalStateException("Interrupted during wait for process", exception)
                    } finally {
                        if (!attachFile.delete()) {
                            attachFile.deleteOnExit()
                        }
                    }
                }
                if (timeout != 0L) {
                    (socket as UNIXSocket?).setSoTimeout(timeUnit.toMillis(timeout) as Int)
                }
                (socket as UNIXSocket?).connect(UNIXSocketAddress(socketFile))
            }

            @Override
            @Throws(IOException::class)
            public override fun read(buffer: ByteArray?): Int {
                return (socket as UNIXSocket?).getInputStream().read(buffer)
            }

            @Override
            @Throws(IOException::class)
            public override fun write(buffer: ByteArray?) {
                (socket as UNIXSocket?).getOutputStream().write(buffer)
            }

            @Override
            @Throws(IOException::class)
            override fun detach() {
                (socket as UNIXSocket?).close()
            }

            companion object {
                /**
                 * The default amount of attempts to connect.
                 */
                private const val DEFAULT_ATTEMPTS = 10

                /**
                 * The default pause between two attempts.
                 */
                private const val DEFAULT_PAUSE: Long = 200

                /**
                 * The default socket timeout.
                 */
                private const val DEFAULT_TIMEOUT: Long = 5000

                /**
                 * The temporary directory on Unix systems.
                 */
                private val TEMPORARY_DIRECTORY: String? = "/tmp"

                /**
                 * The name prefix for a socket.
                 */
                private val SOCKET_FILE_PREFIX: String? = ".java_pid"

                /**
                 * The name prefix for an attachment file indicator.
                 */
                private val ATTACH_FILE_PREFIX: String? = ".attach_pid"

                /**
                 * Asserts the availability of this virtual machine implementation. If the Unix socket library is
                 * missing or if this VM does not support Unix socket communication, a [Throwable] is thrown.
                 *
                 * @return This virtual machine type.
                 * @throws Throwable If this VM does not support POSIX sockets or is not running on a HotSpot VM.
                 */
                @Throws(Throwable::class)
                fun assertAvailability(): Class<*>? {
                    return if (!UNIXSocket.isSupported()) {
                        throw IllegalStateException("POSIX sockets are not supported on the current system")
                    } else if (!System.getProperty("java.vm.name").toLowerCase(Locale.US).contains("hotspot")) {
                        throw IllegalStateException("Cannot apply attachment on non-Hotspot compatible VM")
                    } else {
                        OnUnix::class.java
                    }
                }

                /**
                 * Attaches to the supplied VM process.
                 *
                 * @param processId The process id of the target VM.
                 * @return An appropriate virtual machine implementation.
                 * @throws IOException If an I/O exception occurs.
                 */
                @Throws(IOException::class)
                fun attach(processId: String?): VirtualMachine? {
                    return OnUnix(processId, UNIXSocket.newInstance(), DEFAULT_ATTEMPTS, DEFAULT_PAUSE, DEFAULT_TIMEOUT, TimeUnit.MILLISECONDS)
                }
            }

            /**
             * Creates a new VM implementation for a HotSpot VM running on Unix.
             *
             * @param processId The process id of the target VM.
             * @param socket The Unix socket to use for communication.
             * @param attempts The number of attempts to connect.
             * @param pause The pause time between two VMs.
             * @param timeout The socket timeout.
             * @param timeUnit The time unit of the pause time.
             */
            init {
                this.socket = socket
                this.attempts = attempts
                this.pause = pause
                this.timeout = timeout
                this.timeUnit = timeUnit
            }
        }

        companion object {
            /**
             * The UTF-8 charset.
             */
            private val UTF_8: Charset? = Charset.forName("UTF-8")

            /**
             * The protocol version to use for communication.
             */
            private val PROTOCOL_VERSION: String? = "1"

            /**
             * The `load` command.
             */
            private val LOAD_COMMAND: String? = "load"

            /**
             * The `instrument` command.
             */
            private val INSTRUMENT_COMMAND: String? = "instrument"

            /**
             * A delimiter to be used for attachment.
             */
            private val ARGUMENT_DELIMITER: String? = "="

            /**
             * A blank line argument.
             */
            private val BLANK: ByteArray? = byteArrayOf(0)
        }
    }
}