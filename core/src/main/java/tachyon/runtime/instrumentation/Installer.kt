package tachyon.runtime.instrumentation

import java.lang.instrument.Instrumentation

/**
 * An installer class which defined the hook-in methods that are required by the Java agent
 * specification.
 */
class Installer private constructor() {
    companion object {
        /**
         * A field for carrying the [java.lang.instrument.Instrumentation] that was loaded by the Byte
         * Buddy agent. Note that this field must never be accessed directly as the agent is injected into
         * the VM's system class loader. This way, the field of this class might be `null` even after
         * the installation of the Byte Buddy agent as this class might be loaded by a different class
         * loader than the system class loader.
         */
        @SuppressWarnings("unused")
        @Volatile
        private var instrumentation: Instrumentation? = null

        /**
         *
         *
         * Returns the instrumentation that was loaded by the Byte Buddy agent. When a security manager is
         * active, the [RuntimePermission] for `getInstrumentation` is required by the caller.
         *
         *
         *
         * **Important**: This method must only be invoked via the
         * [ClassLoader.getSystemClassLoader] where any Java agent is loaded. It is possible that
         * two versions of this class exist for different class loaders.
         *
         *
         * @return The instrumentation instance of the Byte Buddy agent.
         */
        fun getInstrumentation(): Instrumentation? {
            val securityManager: SecurityManager = System.getSecurityManager()
            if (securityManager != null) {
                securityManager.checkPermission(RuntimePermission("getInstrumentation"))
            }
            return instrumentation
                    ?: throw IllegalStateException("The Byte Buddy agent is not loaded or this method is not called via the system class loader")
        }

        /**
         * Allows the installation of this agent via a command line argument.
         *
         * @param agentArguments The unused agent arguments.
         * @param instrumentation The instrumentation instance.
         */
        fun premain(agentArguments: String?, instrumentation: Instrumentation?) {
            Companion.instrumentation = instrumentation
        }

        /**
         * Allows the installation of this agent via the Attach API.
         *
         * @param agentArguments The unused agent arguments.
         * @param instrumentation The instrumentation instance.
         */
        @SuppressWarnings("unused")
        fun agentmain(agentArguments: String?, instrumentation: Instrumentation?) {
            Companion.instrumentation = instrumentation
        }
    }

    /**
     * The installer provides only `static` hook-in methods and should not be instantiated.
     */
    init {
        throw UnsupportedOperationException("This class is a utility class and not supposed to be instantiated")
    }
}