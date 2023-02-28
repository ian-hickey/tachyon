package lucee.runtime.instrumentation

import java.lang.reflect.InvocationTargetException

/**
 * A Java program that attaches a Java agent to an external process.
 */
class Attacher private constructor() {
    companion object {
        /**
         * Base for access to a reflective member to make the code more readable.
         */
        private val STATIC_MEMBER: Object? = null

        /**
         * The name of the `attach` method of the `VirtualMachine` class.
         */
        private val ATTACH_METHOD_NAME: String? = "attach"

        /**
         * The name of the `loadAgent` method of the `VirtualMachine` class.
         */
        private val LOAD_AGENT_METHOD_NAME: String? = "loadAgent"

        /**
         * The name of the `detach` method of the `VirtualMachine` class.
         */
        private val DETACH_METHOD_NAME: String? = "detach"

        /**
         * Runs the attacher as a Java application.
         *
         * @param args A list containing the fully qualified name of the virtual machine type, the process
         * id, the fully qualified name of the Java agent jar followed by an empty string if the
         * argument to the agent is `null` or any number of strings where the first
         * argument is proceeded by any single character which is stripped off.
         */
        fun main(args: Array<String?>?) {
            try {
                val argument: String?
                argument = if (args!!.size < 4 || args[3].isEmpty()) {
                    null
                } else {
                    val stringBuilder = StringBuilder(args[3].substring(1))
                    for (index in 4 until args.size) {
                        stringBuilder.append(' ').append(args[index])
                    }
                    stringBuilder.toString()
                }
                install(Class.forName(args[0]), args[1], args[2], argument)
            } catch (ignored: Exception) {
                System.exit(1)
            }
        }

        /**
         * Installs a Java agent on a target VM.
         *
         * @param virtualMachineType The virtual machine type to use for the external attachment.
         * @param processId The id of the process being target of the external attachment.
         * @param agent The Java agent to attach.
         * @param argument The argument to provide or `null` if no argument is provided.
         * @throws NoSuchMethodException If the virtual machine type does not define an expected method.
         * @throws InvocationTargetException If the virtual machine type raises an error.
         * @throws IllegalAccessException If a method of the virtual machine type cannot be accessed.
         */
        @Throws(NoSuchMethodException::class, InvocationTargetException::class, IllegalAccessException::class)
        protected fun install(virtualMachineType: Class<*>?, processId: String?, agent: String?, argument: String?) {
            val virtualMachineInstance: Object = virtualMachineType.getMethod(ATTACH_METHOD_NAME, String::class.java).invoke(STATIC_MEMBER, processId)
            try {
                virtualMachineType.getMethod(LOAD_AGENT_METHOD_NAME, String::class.java, String::class.java).invoke(virtualMachineInstance, agent, argument)
            } finally {
                virtualMachineType.getMethod(DETACH_METHOD_NAME).invoke(virtualMachineInstance)
            }
        }
    }

    /**
     * The attacher provides only `static` utility methods and should not be instantiated.
     */
    init {
        throw UnsupportedOperationException("This class is a utility class and not supposed to be instantiated")
    }
}