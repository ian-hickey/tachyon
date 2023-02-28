package lucee.runtime.instrumentation

import java.io.File

/**
 *
 *
 * The Byte Buddy agent provides a JVM [java.lang.instrument.Instrumentation] in order to
 * allow Byte Buddy the redefinition of already loaded classes. An agent must normally be specified
 * via the command line via the `javaagent` parameter. As an argument to this parameter, one
 * must specify the location of this agent's jar file such as for example in
 *
 *
 *
 * `
 * java -javaagent:byte-buddy-agent.jar -jar app.jar
` *
 *
 *
 *
 * **Note**: The runtime installation of a Java agent is not possible on all JVMs. See the
 * documentation for [InstrumentationFactoryExternal.install] for details on JVMs that are
 * supported out of the box.
 *
 *
 *
 * **Important**: This class's name is known to the Byte Buddy main application and must not be
 * altered.
 *
 *
 *
 * **Note**: Byte Buddy does not execute code using an [java.security.AccessController]. If
 * a security manager is present, the user of this class is responsible for assuring any required
 * privileges.
 *
 */
class InstrumentationFactoryExternal private constructor() {
    /**
     * An attachment provider is responsible for making the Java attachment API available.
     */
    interface AttachmentProvider {
        /**
         * Attempts the creation of an accessor for a specific JVM's attachment API.
         *
         * @return The accessor this attachment provider can supply for the currently running JVM.
         */
        fun attempt(): Accessor?

        /**
         * An accessor for a JVM's attachment API.
         */
        interface Accessor {
            /**
             * Determines if this accessor is applicable for the currently running JVM.
             *
             * @return `true` if this accessor is available.
             */
            fun isAvailable(): Boolean

            /**
             * Returns a `VirtualMachine` class. This method must only be called for available accessors.
             *
             * @return The virtual machine type.
             */
            fun getVirtualMachineType(): Class<*>?

            /**
             * Returns a description of a virtual machine class for an external attachment.
             *
             * @return A description of the external attachment.
             */
            fun getExternalAttachment(): ExternalAttachment?

            /**
             * A canonical implementation of an unavailable accessor.
             */
            enum class Unavailable : Accessor {
                /**
                 * The singleton instance.
                 */
                INSTANCE;

                @Override
                override fun isAvailable(): Boolean {
                    return false
                }

                @Override
                override fun getVirtualMachineType(): Class<*>? {
                    throw IllegalStateException("Cannot read the virtual machine type for an unavailable accessor")
                }

                @Override
                override fun getExternalAttachment(): ExternalAttachment? {
                    throw IllegalStateException("Cannot read the virtual machine type for an unavailable accessor")
                }
            }

            /**
             * Describes an external attachment to a Java virtual machine.
             */
            class ExternalAttachment(
                    /**
                     * The fully-qualified binary name of the virtual machine type.
                     */
                    private val virtualMachineType: String?, classPath: List<File?>?) {
                /**
                 * The class path elements required for loading the supplied virtual machine type.
                 */
                private val classPath: List<File?>?

                /**
                 * Returns the fully-qualified binary name of the virtual machine type.
                 *
                 * @return The fully-qualified binary name of the virtual machine type.
                 */
                fun getVirtualMachineType(): String? {
                    return virtualMachineType
                }

                /**
                 * Returns the class path elements required for loading the supplied virtual machine type.
                 *
                 * @return The class path elements required for loading the supplied virtual machine type.
                 */
                fun getClassPath(): List<File?>? {
                    return classPath
                }

                /**
                 * Creates an external attachment.
                 *
                 * @param virtualMachineType The fully-qualified binary name of the virtual machine type.
                 * @param classPath The class path elements required for loading the supplied virtual machine type.
                 */
                init {
                    this.classPath = classPath
                }
            }

            /**
             * A simple implementation of an accessible accessor.
             */
            abstract class Simple protected constructor(virtualMachineType: Class<*>?) : Accessor {
                /**
                 * A `VirtualMachine` class.
                 */
                protected val virtualMachineType: Class<*>?
                @Override
                override fun isAvailable(): Boolean {
                    return true
                }

                @Override
                override fun getVirtualMachineType(): Class<*>? {
                    return virtualMachineType
                }

                /**
                 * A simple implementation of an accessible accessor that allows for external attachment.
                 */
                protected class WithExternalAttachment(virtualMachineType: Class<*>?, classPath: List<File?>?) : Simple(virtualMachineType) {
                    /**
                     * The class path required for loading the virtual machine type.
                     */
                    private val classPath: List<File?>?
                    @Override
                    override fun getExternalAttachment(): ExternalAttachment? {
                        return ExternalAttachment(virtualMachineType.getName(), classPath)
                    }

                    /**
                     * Creates a new simple accessor that allows for external attachment.
                     *
                     * @param virtualMachineType The `com.sun.tools.attach.VirtualMachine` class.
                     * @param classPath The class path required for loading the virtual machine type.
                     */
                    init {
                        this.classPath = classPath
                    }
                }

                /**
                 * A simple implementation of an accessible accessor that does not allow for external attachment.
                 */
                class WithoutExternalAttachment
                /**
                 * Creates a new simple accessor that does not allow for external attachment.
                 *
                 * @param virtualMachineType A `VirtualMachine` class.
                 */
                (virtualMachineType: Class<*>?) : Simple(virtualMachineType) {
                    @Override
                    override fun getExternalAttachment(): ExternalAttachment? {
                        throw IllegalStateException("Cannot read the virtual machine type for an unavailable accessor")
                    }
                }

                companion object {
                    /**
                     *
                     *
                     * Creates an accessor by reading the process id from the JMX runtime bean and by attempting to load
                     * the `com.sun.tools.attach.VirtualMachine` class from the provided class loader.
                     *
                     *
                     *
                     * This accessor is supposed to work on any implementation of the OpenJDK or Oracle JDK.
                     *
                     *
                     * @param classLoader A class loader that is capable of loading the virtual machine type.
                     * @param classPath The class path required to load the virtual machine class.
                     * @return An appropriate accessor.
                     */
                    fun of(classLoader: ClassLoader?, vararg classPath: File?): Accessor? {
                        return try {
                            WithExternalAttachment(classLoader.loadClass(VIRTUAL_MACHINE_TYPE_NAME), Arrays.asList(classPath))
                        } catch (ignored: ClassNotFoundException) {
                            Unavailable.INSTANCE
                        }
                    }

                    /**
                     *
                     *
                     * Creates an accessor by reading the process id from the JMX runtime bean and by attempting to load
                     * the `com.ibm.tools.attach.VirtualMachine` class from the provided class loader.
                     *
                     *
                     *
                     * This accessor is supposed to work on any implementation of IBM's J9.
                     *
                     *
                     * @return An appropriate accessor.
                     */
                    fun ofJ9(): Accessor? {
                        return try {
                            WithExternalAttachment(ClassLoader.getSystemClassLoader().loadClass(VIRTUAL_MACHINE_TYPE_NAME_J9), Collections.< File > emptyList < File ? > ())
                        } catch (ignored: ClassNotFoundException) {
                            Unavailable.INSTANCE
                        }
                    }
                }

                /**
                 * Creates a new simple accessor.
                 *
                 * @param virtualMachineType A `VirtualMachine` class.
                 */
                init {
                    this.virtualMachineType = virtualMachineType
                }
            }

            companion object {
                /**
                 * The name of the `VirtualMachine` class on any OpenJDK or Oracle JDK implementation.
                 */
                val VIRTUAL_MACHINE_TYPE_NAME: String? = "com.sun.tools.attach.VirtualMachine"

                /**
                 * The name of the `VirtualMachine` class on IBM J9 VMs.
                 */
                val VIRTUAL_MACHINE_TYPE_NAME_J9: String? = "com.ibm.tools.attach.VirtualMachine"
            }
        }

        /**
         * An attachment provider that locates the attach API directly from the system class loader.
         */
        enum class ForJigsawVm : AttachmentProvider {
            /**
             * The singleton instance.
             */
            INSTANCE;

            @Override
            override fun attempt(): Accessor? {
                return Accessor.Simple.of(ClassLoader.getSystemClassLoader())
            }
        }

        /**
         * An attachment provider that locates the attach API directly from the system class loader
         * expecting an IBM J9 VM.
         */
        enum class ForJ9Vm : AttachmentProvider {
            /**
             * The singleton instance.
             */
            INSTANCE;

            @Override
            override fun attempt(): Accessor? {
                return Accessor.Simple.ofJ9()
            }
        }

        /**
         * An attachment provider that is dependant on the existence of a *tools.jar* file on the local
         * file system.
         */
        enum class ForToolsJarVm
        /**
         * Creates a new attachment provider that loads the virtual machine class from the *tools.jar*.
         *
         * @param toolsJarPath The path to the *tools.jar* file, starting from the Java home directory.
         */(
                /**
                 * The path to the *tools.jar* file, starting from the Java home directory.
                 */
                private val toolsJarPath: String?) : AttachmentProvider {
            /**
             * An attachment provider that locates the *tools.jar* from a Java home directory.
             */
            JVM_ROOT("../lib/tools.jar"),

            /**
             * An attachment provider that locates the *tools.jar* from a Java installation directory. In
             * practice, several virtual machines do not return the JRE's location for the *java.home*
             * property against the property's specification.
             */
            JDK_ROOT("lib/tools.jar"),

            /**
             * An attachment provider that locates the *tools.jar* as it is set for several JVM
             * installations on Apple Macintosh computers.
             */
            MACINTOSH("../Classes/classes.jar");

            @Override
            override fun attempt(): Accessor? {
                val toolsJar = File(System.getProperty(JAVA_HOME_PROPERTY), toolsJarPath)
                return try {
                    if (toolsJar.isFile() && toolsJar.canRead()) of(URLClassLoader(arrayOf<URL?>(toolsJar.toURI().toURL()), BOOTSTRAP_CLASS_LOADER), toolsJar) else Accessor.Unavailable.INSTANCE
                } catch (exception: MalformedURLException) {
                    throw IllegalStateException("Could not represent $toolsJar as URL")
                }
            }

            companion object {
                /**
                 * The Java home system property.
                 */
                private val JAVA_HOME_PROPERTY: String? = "java.home"
            }
        }

        /**
         * An attachment provider using a custom protocol implementation for HotSpot on Unix.
         */
        enum class ForUnixHotSpotVm : AttachmentProvider {
            /**
             * The singleton instance.
             */
            INSTANCE;

            @Override
            override fun attempt(): Accessor? {
                return try {
                    Accessor.Simple.WithoutExternalAttachment(VirtualMachine.ForHotSpot.OnUnix.assertAvailability())
                } catch (ignored: Throwable) {
                    Accessor.Unavailable.INSTANCE
                }
            }
        }

        /**
         * A compound attachment provider that attempts the attachment by delegation to other providers. If
         * none of the providers of this compound provider is capable of providing a valid accessor, an
         * non-available accessor is returned.
         */
        class Compound(attachmentProviders: List<AttachmentProvider?>?) : AttachmentProvider {
            /**
             * A list of attachment providers in the order of their application.
             */
            private val attachmentProviders: List<AttachmentProvider?>?

            /**
             * Creates a new compound attachment provider.
             *
             * @param attachmentProvider A list of attachment providers in the order of their application.
             */
            constructor(vararg attachmentProvider: AttachmentProvider?) : this(Arrays.asList(attachmentProvider)) {}

            @Override
            override fun attempt(): Accessor? {
                for (attachmentProvider in attachmentProviders!!) {
                    val accessor = attachmentProvider!!.attempt()
                    if (accessor!!.isAvailable()) {
                        return accessor
                    }
                }
                return Accessor.Unavailable.INSTANCE
            }

            /**
             * Creates a new compound attachment provider.
             *
             * @param attachmentProviders A list of attachment providers in the order of their application.
             */
            init {
                this.attachmentProviders = ArrayList<AttachmentProvider?>()
                for (attachmentProvider in attachmentProviders!!) {
                    if (attachmentProvider is Compound) {
                        this.attachmentProviders.addAll((attachmentProvider as Compound?)!!.attachmentProviders)
                    } else {
                        this.attachmentProviders.add(attachmentProvider)
                    }
                }
            }
        }

        companion object {
            /**
             * The default attachment provider to be used.
             */
            val DEFAULT: AttachmentProvider? = Compound(ForJigsawVm.INSTANCE, ForJ9Vm.INSTANCE, ForToolsJarVm.JVM_ROOT, ForToolsJarVm.JDK_ROOT, ForToolsJarVm.MACINTOSH,
                    ForUnixHotSpotVm.INSTANCE)
        }
    }

    /**
     * A process provider is responsible for providing the process id of the current VM.
     */
    interface ProcessProvider {
        /**
         * Resolves a process id for the current JVM.
         *
         * @return The resolved process id.
         */
        fun resolve(): String?

        /**
         * Supplies the current VM's process id.
         */
        enum class ForCurrentVm : ProcessProvider {
            /**
             * The singleton instance.
             */
            INSTANCE;

            /**
             * The best process provider for the current VM.
             */
            private val dispatcher: ProcessProvider?
            @Override
            override fun resolve(): String? {
                return dispatcher!!.resolve()
            }

            /**
             * A process provider for a legacy VM that reads the process id from its JMX properties.
             */
            protected enum class ForLegacyVm : ProcessProvider {
                /**
                 * The singleton instance.
                 */
                INSTANCE;

                @Override
                override fun resolve(): String? {
                    val runtimeName: String = ManagementFactory.getRuntimeMXBean().getName()
                    val processIdIndex: Int = runtimeName.indexOf('@')
                    return if (processIdIndex == -1) {
                        throw IllegalStateException("Cannot extract process id from runtime management bean")
                    } else {
                        runtimeName.substring(0, processIdIndex)
                    }
                }
            }

            /**
             * A process provider for a Java 9 capable VM with access to the introduced process API.
             */
            protected class ForJava9CapableVm protected constructor(current: Method?, pid: Method?) : ProcessProvider {
                /**
                 * The `java.lang.ProcessHandle#current()` method.
                 */
                private val current: Method?

                /**
                 * The `java.lang.ProcessHandle#pid()` method.
                 */
                private val pid: Method?
                @Override
                override fun resolve(): String? {
                    return try {
                        pid.invoke(current.invoke(STATIC_MEMBER)).toString()
                    } catch (exception: IllegalAccessException) {
                        throw IllegalStateException("Cannot access Java 9 process API", exception)
                    } catch (exception: InvocationTargetException) {
                        throw IllegalStateException("Error when accessing Java 9 process API", exception.getCause())
                    }
                }

                companion object {
                    /**
                     * Attempts to create a dispatcher for a Java 9 VM and falls back to a legacy dispatcher if this is
                     * not possible.
                     *
                     * @return A dispatcher for the current VM.
                     */
                    fun make(): ProcessProvider? {
                        return try {
                            ForJava9CapableVm(Class.forName("java.lang.ProcessHandle").getMethod("current"), Class.forName("java.lang.ProcessHandle").getMethod("pid"))
                        } catch (ignored: Exception) {
                            ForLegacyVm.INSTANCE
                        }
                    }
                }

                /**
                 * Creates a new Java 9 capable dispatcher for reading the current process's id.
                 *
                 * @param current The `java.lang.ProcessHandle#current()` method.
                 * @param pid The `java.lang.ProcessHandle#pid()` method.
                 */
                init {
                    this.current = current
                    this.pid = pid
                }
            }

            /**
             * Creates a process provider that supplies the current VM's process id.
             */
            init {
                dispatcher = ForJava9CapableVm.make()
            }
        }
    }

    /**
     * An agent provider is responsible for handling and providing the jar file of an agent that is
     * being attached.
     */
    protected interface AgentProvider {
        /**
         * Provides an agent jar file for attachment.
         *
         * @return The provided agent.
         * @throws IOException If the agent cannot be written to disk.
         */
        @Throws(IOException::class)
        fun resolve(): File?

        /**
         * An agent provider for a temporary Byte Buddy agent.
         */
        enum class ForByteBuddyAgent : AgentProvider {
            /**
             * The singleton instance.
             */
            INSTANCE;

            @Override
            @Throws(IOException::class)
            override fun resolve(): File? {
                return try {
                    val agentJar: File? = trySelfResolve()
                    if (agentJar == null) createJarFile() else agentJar
                } catch (ignored: Exception) {
                    createJarFile()
                }
            }

            companion object {
                /**
                 * The default prefix of the Byte Buddy agent jar file.
                 */
                private val AGENT_FILE_NAME: String? = "byteBuddyAgent"

                /**
                 * Attempts to resolve the [Installer] class from this jar file if it can be located. Doing
                 * so, it is possible to avoid the creation of a temporary jar file which can remain undeleted on
                 * Windows operating systems where the agent is linked by a class loader such that
                 * [File.deleteOnExit] does not have an effect.
                 *
                 * @return This jar file's location or `null` if this jar file's location is inaccessible.
                 * @throws IOException If an I/O exception occurs.
                 */
                @Throws(IOException::class)
                private fun trySelfResolve(): File? {
                    val protectionDomain: ProtectionDomain = Installer::class.java.getProtectionDomain()
                            ?: return CANNOT_SELF_RESOLVE
                    val codeSource: CodeSource = protectionDomain.getCodeSource()
                            ?: return CANNOT_SELF_RESOLVE
                    val location: URL = codeSource.getLocation()
                    if (!location.getProtocol().equals(FILE_PROTOCOL)) {
                        return CANNOT_SELF_RESOLVE
                    }
                    var agentJar: File?
                    try {
                        agentJar = File(location.toURI())
                    } catch (ignored: URISyntaxException) {
                        agentJar = File(location.getPath())
                    }
                    if (!agentJar.isFile() || !agentJar.canRead()) {
                        return CANNOT_SELF_RESOLVE
                    }
                    // It is necessary to check the manifest of the containing file as this code can be shaded into
                    // another artifact.
                    val jarInputStream = JarInputStream(FileInputStream(agentJar))
                    return try {
                        val manifest: Manifest = jarInputStream.getManifest()
                                ?: return CANNOT_SELF_RESOLVE
                        val attributes: Attributes = manifest.getMainAttributes()
                                ?: return CANNOT_SELF_RESOLVE
                        if (Installer::class.java.getName().equals(attributes.getValue(AGENT_CLASS_PROPERTY)) && Boolean.parseBoolean(attributes.getValue(CAN_REDEFINE_CLASSES_PROPERTY))
                                && Boolean.parseBoolean(attributes.getValue(CAN_RETRANSFORM_CLASSES_PROPERTY))
                                && Boolean.parseBoolean(attributes.getValue(CAN_SET_NATIVE_METHOD_PREFIX))) {
                            agentJar
                        } else {
                            CANNOT_SELF_RESOLVE
                        }
                    } finally {
                        jarInputStream.close()
                    }
                }

                /**
                 * Creates an agent jar file containing the [Installer] class.
                 *
                 * @return The agent jar file.
                 * @throws IOException If an I/O exception occurs.
                 */
                @Throws(IOException::class)
                private fun createJarFile(): File? {
                    val inputStream: InputStream = Installer::class.java.getResourceAsStream('/' + Installer::class.java.getName().replace('.', '/') + CLASS_FILE_EXTENSION)
                            ?: throw IllegalStateException("Cannot locate class file for Byte Buddy installer")
                    return try {
                        val agentJar: File = File.createTempFile(AGENT_FILE_NAME, JAR_FILE_EXTENSION)
                        agentJar.deleteOnExit() // Agent jar is required until VM shutdown due to lazy class loading.
                        val manifest = Manifest()
                        manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, MANIFEST_VERSION_VALUE)
                        manifest.getMainAttributes().put(Name(AGENT_CLASS_PROPERTY), Installer::class.java.getName())
                        manifest.getMainAttributes().put(Name(CAN_REDEFINE_CLASSES_PROPERTY), Boolean.TRUE.toString())
                        manifest.getMainAttributes().put(Name(CAN_RETRANSFORM_CLASSES_PROPERTY), Boolean.TRUE.toString())
                        manifest.getMainAttributes().put(Name(CAN_SET_NATIVE_METHOD_PREFIX), Boolean.TRUE.toString())
                        val jarOutputStream = JarOutputStream(FileOutputStream(agentJar), manifest)
                        try {
                            jarOutputStream.putNextEntry(JarEntry(Installer::class.java.getName().replace('.', '/') + CLASS_FILE_EXTENSION))
                            val buffer = ByteArray(BUFFER_SIZE)
                            var index: Int
                            while (inputStream.read(buffer).also { index = it } != END_OF_FILE) {
                                jarOutputStream.write(buffer, START_INDEX, index)
                            }
                            jarOutputStream.closeEntry()
                        } finally {
                            jarOutputStream.close()
                        }
                        agentJar
                    } finally {
                        inputStream.close()
                    }
                }
            }
        }

        /**
         * An agent provider that supplies an existing agent that is not deleted after attachment.
         */
        class ForExistingAgent(agent: File?) : AgentProvider {
            /**
             * The supplied agent.
             */
            private val agent: File?
            @Override
            override fun resolve(): File? {
                return agent
            }

            /**
             * Creates an agent provider for an existing agent.
             *
             * @param agent The supplied agent.
             */
            init {
                this.agent = agent
            }
        }
    }

    /**
     * An attachment evaluator is responsible for deciding if an agent can be attached from the current
     * process.
     */
    protected interface AttachmentTypeEvaluator {
        /**
         * Checks if the current VM requires external attachment for the supplied process id.
         *
         * @param processId The process id of the process to which to attach.
         * @return `true` if the current VM requires external attachment for the supplied process.
         */
        fun requiresExternalAttachment(processId: String?): Boolean

        /**
         * An installation action for creating an attachment type evaluator.
         */
        enum class InstallationAction : PrivilegedAction<AttachmentTypeEvaluator?> {
            /**
             * The singleton instance.
             */
            INSTANCE;

            @Override
            fun run(): AttachmentTypeEvaluator? {
                return try {
                    if (Boolean.getBoolean(JDK_ALLOW_SELF_ATTACH)) {
                        Disabled.INSTANCE
                    } else {
                        ForJava9CapableVm(Class.forName("java.lang.ProcessHandle").getMethod("current"), Class.forName("java.lang.ProcessHandle").getMethod("pid"))
                    }
                } catch (ignored: Exception) {
                    Disabled.INSTANCE
                }
            }

            companion object {
                /**
                 * The OpenJDK's property for specifying the legality of self-attachment.
                 */
                private val JDK_ALLOW_SELF_ATTACH: String? = "jdk.attach.allowAttachSelf"
            }
        }

        /**
         * An attachment type evaluator that never requires external attachment.
         */
        enum class Disabled : AttachmentTypeEvaluator {
            /**
             * The singleton instance.
             */
            INSTANCE;

            @Override
            override fun requiresExternalAttachment(processId: String?): Boolean {
                return false
            }
        }

        /**
         * An attachment type evaluator that checks a process id against the current process id.
         */
        class ForJava9CapableVm(current: Method?, pid: Method?) : AttachmentTypeEvaluator {
            /**
             * The `java.lang.ProcessHandle#current()` method.
             */
            private val current: Method?

            /**
             * The `java.lang.ProcessHandle#pid()` method.
             */
            private val pid: Method?
            @Override
            override fun requiresExternalAttachment(processId: String?): Boolean {
                return try {
                    pid.invoke(current.invoke(STATIC_MEMBER)).toString().equals(processId)
                } catch (exception: IllegalAccessException) {
                    throw IllegalStateException("Cannot access Java 9 process API", exception)
                } catch (exception: InvocationTargetException) {
                    throw IllegalStateException("Error when accessing Java 9 process API", exception.getCause())
                }
            }

            /**
             * Creates a new attachment type evaluator.
             *
             * @param current The `java.lang.ProcessHandle#current()` method.
             * @param pid The `java.lang.ProcessHandle#pid()` method.
             */
            init {
                this.current = current
                this.pid = pid
            }
        }
    }

    companion object {
        /**
         * The manifest property specifying the agent class.
         */
        private val AGENT_CLASS_PROPERTY: String? = "Agent-Class"

        /**
         * The manifest property specifying the *can redefine* property.
         */
        private val CAN_REDEFINE_CLASSES_PROPERTY: String? = "Can-Redefine-Classes"

        /**
         * The manifest property specifying the *can retransform* property.
         */
        private val CAN_RETRANSFORM_CLASSES_PROPERTY: String? = "Can-Retransform-Classes"

        /**
         * The manifest property specifying the *can set native method prefix* property.
         */
        private val CAN_SET_NATIVE_METHOD_PREFIX: String? = "Can-Set-Native-Method-Prefix"

        /**
         * The manifest property value for the manifest version.
         */
        private val MANIFEST_VERSION_VALUE: String? = "1.0"

        /**
         * The size of the buffer for copying the agent installer file into another jar.
         */
        private const val BUFFER_SIZE = 1024

        /**
         * Convenience indices for reading and writing to the buffer to make the code more readable.
         */
        private const val START_INDEX = 0
        private const val END_OF_FILE = -1

        /**
         * The status code expected as a result of a successful attachment.
         */
        private const val SUCCESSFUL_ATTACH = 0

        /**
         * Base for access to a reflective member to make the code more readable.
         */
        private val STATIC_MEMBER: Object? = null

        /**
         * Representation of the bootstrap [java.lang.ClassLoader].
         */
        private val BOOTSTRAP_CLASS_LOADER: ClassLoader? = null

        /**
         * Represents a no-op argument for a dynamic agent attachment.
         */
        private val WITHOUT_ARGUMENT: String? = null

        /**
         * The naming prefix of all artifacts for an attacher jar.
         */
        private val ATTACHER_FILE_NAME: String? = "byteBuddyAttacher"

        /**
         * The file extension for a class file.
         */
        private val CLASS_FILE_EXTENSION: String? = ".class"

        /**
         * The file extension for a jar file.
         */
        private val JAR_FILE_EXTENSION: String? = ".jar"

        /**
         * The class path argument to specify the class path elements.
         */
        private val CLASS_PATH_ARGUMENT: String? = "-cp"

        /**
         * The Java property denoting the Java home directory.
         */
        private val JAVA_HOME: String? = "java.home"

        /**
         * The Java property denoting the operating system name.
         */
        private val OS_NAME: String? = "os.name"

        /**
         * The name of the method for reading the installer's instrumentation.
         */
        private val INSTRUMENTATION_METHOD: String? = "getInstrumentation"

        /**
         * Represents the `file` URL protocol.
         */
        private val FILE_PROTOCOL: String? = "file"

        /**
         * An indicator variable to express that no instrumentation is available.
         */
        private val UNAVAILABLE: Instrumentation? = null

        /**
         * Represents a failed attempt to self-resolve a jar file location.
         */
        private val CANNOT_SELF_RESOLVE: File? = null

        /**
         * The attachment type evaluator to be used for determining if an attachment requires an external
         * process.
         */
        private val ATTACHMENT_TYPE_EVALUATOR: AttachmentTypeEvaluator? = AccessController.doPrivileged(AttachmentTypeEvaluator.InstallationAction.INSTANCE)

        /**
         *
         *
         * Looks up the [java.lang.instrument.Instrumentation] instance of an installed Byte Buddy
         * agent. Note that this method implies reflective lookup and reflective invocation such that the
         * returned value should be cached rather than calling this method several times.
         *
         *
         *
         * **Note**: This method throws an [java.lang.IllegalStateException] If the Byte Buddy
         * agent is not properly installed.
         *
         *
         * @return The [java.lang.instrument.Instrumentation] instance which is provided by an
         * installed Byte Buddy agent.
         */
        fun getInstrumentation(): Instrumentation? {
            return doGetInstrumentation()
                    ?: throw IllegalStateException("The Byte Buddy agent is not initialized")
        }

        /**
         * Attaches the given agent Jar on the target process which must be a virtual machine process. The
         * default attachment provider is used for applying the attachment. This operation blocks until the
         * attachment is complete. If the current VM does not supply any known form of attachment to a
         * remote VM, an [IllegalStateException] is thrown. The agent is not provided an argument.
         *
         * @param agentJar The agent jar file.
         * @param processId The target process id.
         */
        fun attach(agentJar: File?, processId: String?) {
            attach(agentJar, processId, WITHOUT_ARGUMENT)
        }

        /**
         * Attaches the given agent Jar on the target process which must be a virtual machine process. The
         * default attachment provider is used for applying the attachment. This operation blocks until the
         * attachment is complete. If the current VM does not supply any known form of attachment to a
         * remote VM, an [IllegalStateException] is thrown.
         *
         * @param agentJar The agent jar file.
         * @param processId The target process id.
         * @param argument The argument to provide to the agent.
         */
        fun attach(agentJar: File?, processId: String?, argument: String?) {
            attach(agentJar, processId, argument, AttachmentProvider.DEFAULT)
        }

        /**
         * Attaches the given agent Jar on the target process which must be a virtual machine process. This
         * operation blocks until the attachment is complete. The agent is not provided an argument.
         *
         * @param agentJar The agent jar file.
         * @param processId The target process id.
         * @param attachmentProvider The attachment provider to use.
         */
        fun attach(agentJar: File?, processId: String?, attachmentProvider: AttachmentProvider?) {
            attach(agentJar, processId, WITHOUT_ARGUMENT, attachmentProvider)
        }

        /**
         * Attaches the given agent Jar on the target process which must be a virtual machine process. This
         * operation blocks until the attachment is complete.
         *
         * @param agentJar The agent jar file.
         * @param processId The target process id.
         * @param argument The argument to provide to the agent.
         * @param attachmentProvider The attachment provider to use.
         */
        fun attach(agentJar: File?, processId: String?, argument: String?, attachmentProvider: AttachmentProvider?) {
            install(attachmentProvider, processId, argument, AgentProvider.ForExistingAgent(agentJar))
        }

        /**
         * Attaches the given agent Jar on the target process which must be a virtual machine process. The
         * default attachment provider is used for applying the attachment. This operation blocks until the
         * attachment is complete. If the current VM does not supply any known form of attachment to a
         * remote VM, an [IllegalStateException] is thrown. The agent is not provided an argument.
         *
         * @param agentJar The agent jar file.
         * @param processProvider A provider of the target process id.
         */
        fun attach(agentJar: File?, processProvider: ProcessProvider?) {
            attach(agentJar, processProvider, WITHOUT_ARGUMENT)
        }

        /**
         * Attaches the given agent Jar on the target process which must be a virtual machine process. The
         * default attachment provider is used for applying the attachment. This operation blocks until the
         * attachment is complete. If the current VM does not supply any known form of attachment to a
         * remote VM, an [IllegalStateException] is thrown.
         *
         * @param agentJar The agent jar file.
         * @param processProvider A provider of the target process id.
         * @param argument The argument to provide to the agent.
         */
        fun attach(agentJar: File?, processProvider: ProcessProvider?, argument: String?) {
            attach(agentJar, processProvider, argument, AttachmentProvider.DEFAULT)
        }

        /**
         * Attaches the given agent Jar on the target process which must be a virtual machine process. This
         * operation blocks until the attachment is complete. The agent is not provided an argument.
         *
         * @param agentJar The agent jar file.
         * @param processProvider A provider of the target process id.
         * @param attachmentProvider The attachment provider to use.
         */
        fun attach(agentJar: File?, processProvider: ProcessProvider?, attachmentProvider: AttachmentProvider?) {
            attach(agentJar, processProvider, WITHOUT_ARGUMENT, attachmentProvider)
        }

        /**
         * Attaches the given agent Jar on the target process which must be a virtual machine process. This
         * operation blocks until the attachment is complete.
         *
         * @param agentJar The agent jar file.
         * @param processProvider A provider of the target process id.
         * @param argument The argument to provide to the agent.
         * @param attachmentProvider The attachment provider to use.
         */
        fun attach(agentJar: File?, processProvider: ProcessProvider?, argument: String?, attachmentProvider: AttachmentProvider?) {
            install(attachmentProvider, processProvider!!.resolve(), argument, AgentProvider.ForExistingAgent(agentJar))
        }

        /**
         *
         *
         * Installs an agent on the currently running Java virtual machine. Unfortunately, this does not
         * always work. The runtime installation of a Java agent is supported for:
         *
         *
         *  * **JVM version 9+**: For Java VM of at least version 9, the attachment API was merged into
         * a Jigsaw module and the runtime installation is always possible.
         *  * **OpenJDK / Oracle JDK / IBM J9 versions 8-**: The installation for HotSpot is only
         * possible when bundled with a JDK up until Java version 8. It is not possible for runtime-only
         * installations of HotSpot or J9 for these versions.
         *
         *
         *
         * If an agent cannot be installed, an [IllegalStateException] is thrown.
         *
         *
         *
         * **Important**: This is a rather computation-heavy operation. Therefore, this operation is not
         * repeated after an agent was successfully installed for the first time. Instead, the previous
         * instrumentation instance is returned. However, invoking this method requires synchronization such
         * that subsequently to an installation, [InstrumentationFactoryExternal.getInstrumentation]
         * should be invoked instead.
         *
         *
         * @return An instrumentation instance representing the currently running JVM.
         */
        fun install(): Instrumentation? {
            return install(AttachmentProvider.DEFAULT)
        }

        /**
         * Installs a Java agent using the Java attach API. This API is available under different access
         * routes for different JVMs and JVM versions or it might not be available at all. If a Java agent
         * cannot be installed by using the supplied attachment provider, an [IllegalStateException]
         * is thrown. The same happens if the default process provider cannot resolve a process id for the
         * current VM.
         *
         * @param attachmentProvider The attachment provider to use for the installation.
         * @return An instrumentation instance representing the currently running JVM.
         */
        fun install(attachmentProvider: AttachmentProvider?): Instrumentation? {
            return install(attachmentProvider, ProcessProvider.ForCurrentVm.INSTANCE)
        }

        /**
         * Installs a Java agent using the Java attach API. This API is available under different access
         * routes for different JVMs and JVM versions or it might not be available at all. If a Java agent
         * cannot be installed by using the supplied process provider, an [IllegalStateException] is
         * thrown. The same happens if the default attachment provider cannot be used.
         *
         * @param processProvider The provider for the current JVM's process id.
         * @return An instrumentation instance representing the currently running JVM.
         */
        fun install(processProvider: ProcessProvider?): Instrumentation? {
            return install(AttachmentProvider.DEFAULT, processProvider)
        }

        /**
         * Installs a Java agent using the Java attach API. This API is available under different access
         * routes for different JVMs and JVM versions or it might not be available at all. If a Java agent
         * cannot be installed by using the supplied attachment provider and process provider, an
         * [IllegalStateException] is thrown.
         *
         * @param attachmentProvider The attachment provider to use for the installation.
         * @param processProvider The provider for the current JVM's process id.
         * @return An instrumentation instance representing the currently running JVM.
         */
        @Synchronized
        fun install(attachmentProvider: AttachmentProvider?, processProvider: ProcessProvider?): Instrumentation? {
            val instrumentation: Instrumentation? = doGetInstrumentation()
            if (instrumentation != null) {
                return instrumentation
            }
            install(attachmentProvider, processProvider!!.resolve(), WITHOUT_ARGUMENT, AgentProvider.ForByteBuddyAgent.INSTANCE)
            return doGetInstrumentation()
        }

        /**
         * Installs a Java agent on a target VM.
         *
         * @param attachmentProvider The attachment provider to use.
         * @param processId The process id of the target JVM process.
         * @param argument The argument to provide to the agent.
         * @param agentProvider The agent provider for the agent jar.
         */
        private fun install(attachmentProvider: AttachmentProvider?, processId: String?, argument: String?, agentProvider: AgentProvider?) {
            val attachmentAccessor = attachmentProvider!!.attempt()
            if (!attachmentAccessor!!.isAvailable()) {
                throw IllegalStateException("No compatible attachment provider is available")
            }
            try {
                // if (true || ATTACHMENT_TYPE_EVALUATOR.requiresExternalAttachment(processId)) {
                installExternal(attachmentAccessor.getExternalAttachment(), processId, agentProvider!!.resolve(), argument)
                /*
			 * } else { Attacher.install(attachmentAccessor.getVirtualMachineType(), processId,
			 * agentProvider.resolve().getAbsolutePath(), argument); }
			 */
            } catch (exception: RuntimeException) {
                throw exception
            } catch (exception: Exception) {
                throw IllegalStateException("Error during attachment using: $attachmentProvider", exception)
            }
        }

        /**
         * Installs a Java agent to the current VM via an external process. This is typically required
         * starting with OpenJDK 9 when the `jdk.attach.allowAttachSelf` property is set to
         * `false` what is the default setting.
         *
         * @param externalAttachment A description of the external attachment.
         * @param processId The process id of the current process.
         * @param agent The Java agent to install.
         * @param argument The argument to provide to the agent or `null` if no argument should be
         * supplied.
         * @throws Exception If an exception occurs during the attachment or the external process fails the
         * attachment.
         */
        @Throws(Exception::class)
        private fun installExternal(externalAttachment: AttachmentProvider.Accessor.ExternalAttachment?, processId: String?, agent: File?, argument: String?) {
            val selfResolvedJar: File? = trySelfResolve()
            var attachmentJar: File? = null
            try {
                if (selfResolvedJar == null) {
                    val inputStream: InputStream = Attacher::class.java.getResourceAsStream('/' + Attacher::class.java.getName().replace('.', '/') + CLASS_FILE_EXTENSION)
                            ?: throw IllegalStateException("Cannot locate class file for Byte Buddy installation process")
                    try {
                        attachmentJar = File.createTempFile(ATTACHER_FILE_NAME, JAR_FILE_EXTENSION)
                        val jarOutputStream = JarOutputStream(FileOutputStream(attachmentJar))
                        try {
                            jarOutputStream.putNextEntry(JarEntry(Attacher::class.java.getName().replace('.', '/') + CLASS_FILE_EXTENSION))
                            val buffer = ByteArray(BUFFER_SIZE)
                            var index: Int
                            while (inputStream.read(buffer).also { index = it } != END_OF_FILE) {
                                jarOutputStream.write(buffer, START_INDEX, index)
                            }
                            jarOutputStream.closeEntry()
                        } finally {
                            jarOutputStream.close()
                        }
                    } finally {
                        inputStream.close()
                    }
                }
                val classPath: StringBuilder = StringBuilder().append(quote((if (selfResolvedJar == null) attachmentJar else selfResolvedJar).getCanonicalPath()))
                for (jar in externalAttachment!!.getClassPath()!!) {
                    classPath.append(File.pathSeparatorChar).append(quote(jar.getCanonicalPath()))
                }
                if (ProcessBuilder(
                                quote(System.getProperty(JAVA_HOME) + File.separatorChar.toString() + "bin" + File.separatorChar
                                        .toString() + if (System.getProperty(OS_NAME, "").toLowerCase(Locale.US).contains("windows")) "java.exe" else "java"),
                                CLASS_PATH_ARGUMENT, classPath.toString(), Attacher::class.java.getName(), externalAttachment.getVirtualMachineType(), processId, quote(agent.getAbsolutePath()),
                                if (argument == null) "" else "=$argument").start().waitFor() !== SUCCESSFUL_ATTACH) {
                    throw IllegalStateException("Could not self-attach to current VM using external process")
                }
            } finally {
                if (attachmentJar != null) {
                    if (!attachmentJar.delete()) {
                        attachmentJar.deleteOnExit()
                    }
                }
            }
        }

        /**
         * Attempts to resolve the location of the [Attacher] class for a self-attachment. Doing so
         * avoids the creation of a temporary jar file.
         *
         * @return The self-resolved jar file or `null` if the jar file cannot be located.
         */
        private fun trySelfResolve(): File? {
            return try {
                val protectionDomain: ProtectionDomain = Attacher::class.java.getProtectionDomain()
                        ?: return CANNOT_SELF_RESOLVE
                val codeSource: CodeSource = protectionDomain.getCodeSource()
                        ?: return CANNOT_SELF_RESOLVE
                val location: URL = codeSource.getLocation()
                if (!location.getProtocol().equals(FILE_PROTOCOL)) {
                    return CANNOT_SELF_RESOLVE
                }
                try {
                    File(location.toURI())
                } catch (ignored: URISyntaxException) {
                    File(location.getPath())
                }
            } catch (ignored: Exception) {
                CANNOT_SELF_RESOLVE
            }
        }

        /**
         * Quotes a value if it contains a white space.
         *
         * @param value The value to quote.
         * @return The value being quoted if necessary.
         */
        private fun quote(value: String?): String? {
            return if (value.contains(" ")) '"' + value + '"'.toInt() else value
        }

        /**
         * Performs the actual lookup of the [java.lang.instrument.Instrumentation] from an installed
         * Byte Buddy agent.
         *
         * @return The Byte Buddy agent's [java.lang.instrument.Instrumentation] instance.
         */
        private fun doGetInstrumentation(): Instrumentation? {
            return try {
                ClassLoader.getSystemClassLoader().loadClass(Installer::class.java.getName()).getMethod(INSTRUMENTATION_METHOD).invoke(STATIC_MEMBER) as Instrumentation
            } catch (ignored: Exception) {
                UNAVAILABLE
            }
        }
    }

    /**
     * The agent provides only `static` utility methods and should not be instantiated.
     */
    init {
        throw UnsupportedOperationException("This class is a utility class and not supposed to be instantiated")
    }
}