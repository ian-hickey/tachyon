/**
 * Copyright (c) 2014, the Railo Company Ltd.
 * Copyright (c) 2015, Lucee Assosication Switzerland
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library.  If not, see <http:></http:>//www.gnu.org/licenses/>.
 *
 */
package lucee.runtime.instrumentation

import java.io.File

/**
 * Factory for obtaining an [Instrumentation] instance.
 */
object InstrumentationFactory {
    // private static final String _name = InstrumentationFactory.class.getName();
    private val SEP: String? = File.separator
    private val TOOLS_VERSION: String? = "7u25"
    private val AGENT_CLASS_NAME: String? = "lucee.runtime.instrumentation.ExternalAgent"
    private var _instr: Instrumentation? = null
    @Synchronized
    fun getInstrumentation(config: Config?): Instrumentation? {
        val log: Log = ThreadLocalPageContext.getLog(config, "application")
        // final CFMLEngine engine = ConfigWebUtil.getEngine(config);
        var instr: Instrumentation? = _getInstrumentation(log, config)

        // agent already exist
        if (instr != null) return instr
        AccessController.doPrivileged(object : PrivilegedAction<Object?>() {
            @Override
            fun run(): Object? {
                val ccl: ClassLoader = Thread.currentThread().getContextClassLoader()
                Thread.currentThread().setContextClassLoader(ClassLoader.getSystemClassLoader())
                try {
                    val vendor: JavaVendor = JavaVendor.getCurrentVendor()
                    var toolsJar: Resource? = null
                    // When running on IBM, the attach api classes are packaged in vm.jar which is a part
                    // of the default vm classpath.
                    val useOurOwn: RefBoolean = RefBooleanImpl(true)
                    // if (!vendor.isIBM()) {
                    // If we can't find the tools.jar and we're not on IBM we can't load the agent.
                    toolsJar = findToolsJar(config, log, useOurOwn)
                    if (toolsJar == null) {
                        return null
                    }
                    // }
                    log.info("Instrumentation", "tools.jar used:$toolsJar")

                    // add the attach native library
                    if (useOurOwn.toBooleanValue()) addAttachIfNecessary(config, log)
                    val vmClass: Class<*>? = loadVMClass(toolsJar, log, vendor)
                    log.info("Instrumentation", "loaded VirtualMachine class:" + if (vmClass == null) "null" else vmClass.getName())
                    if (vmClass == null) {
                        return null
                    }
                    val agentPath: String = createAgentJar(log, config).getAbsolutePath()
                            ?: return null
                    log.info("Instrumentation", "try to load agent (path:$agentPath)")
                    loadAgent(config, log, agentPath, vmClass)
                    // log.info("Instrumentation","agent loaded (path:"+agentPath+")");
                } catch (ioe: IOException) {
                    log.log(Log.LEVEL_INFO, "Instrumentation", ioe)
                } finally {
                    Thread.currentThread().setContextClassLoader(ccl)
                }
                return null
            } // end run()
        })
        // If the load(...) agent call was successful, this variable will no
        // longer be null.
        instr = _getInstrumentation(log, config)
        if (instr == null) {
            instr = InstrumentationFactoryExternal.install()
        }
        if (instr == null) {
            try {
                val allowAttachSelf: Boolean = Caster.toBooleanValue(System.getProperty("jdk.attach.allowAttachSelf"), false)
                val agentJar: Resource? = createAgentJar(log, config)
                throw PageRuntimeException(ApplicationException(
                        Constants.NAME.toString() + " was not able to load an Agent dynamically! " + "You may add this manually by adding the following to your JVM arguments [-javaagent:\""
                                + agentJar + "\"] " + if (allowAttachSelf) "." else "or supply -Djdk.attach.allowAttachSelf as system property."))
            } catch (ioe: IOException) {
                LogUtil.log(config, InstrumentationFactory::class.java.getName(), ioe)
            }
        }
        return instr
    }

    private fun _getInstrumentation(log: Log?, config: Config?): Instrumentation? {
        if (_instr != null) return _instr

        // try to get from different Classloaders
        _instr = _getInstrumentation(ClassLoader.getSystemClassLoader(), log)
        if (_instr != null) return _instr
        _instr = _getInstrumentation(CFMLEngineFactory::class.java.getClassLoader(), log)
        if (_instr != null) return _instr
        _instr = _getInstrumentation(config.getClassLoader(), log)
        return _instr
    }

    private fun _getInstrumentation(cl: ClassLoader?, log: Log?): Instrumentation? {
        // get Class
        val clazz: Class<*> = ClassUtil.loadClass(cl, AGENT_CLASS_NAME, null)
        if (clazz != null) {
            log.info("Instrumentation", "found [lucee.runtime.instrumentation.ExternalAgent] in ClassLoader [" + clazz.getClassLoader().toString() + "]")
        } else {
            log.info("Instrumentation", "not found [lucee.runtime.instrumentation.ExternalAgent] in ClassLoader [$cl]")
            return null
        }
        try {
            val m: Method = clazz.getMethod("getInstrumentation", arrayOfNulls<Class?>(0))
            _instr = m.invoke(null, arrayOfNulls<Object?>(0)) as Instrumentation
            log.info("Instrumentation", "ExternalAgent does " + (if (_instr != null) "" else "not ") + "contain an Instrumentation instance")
            return _instr
        } catch (t: Throwable) {
            ExceptionUtil.rethrowIfNecessary(t)
            log.log(Log.LEVEL_INFO, "Instrumentation", t)
        }
        return null
    }

    @Throws(IOException::class)
    private fun createAgentJar(log: Log?, c: Config?): Resource? {
        val trg: Resource = getDeployDirectory(c).getRealResource("lucee-external-agent.jar")
        if (!trg.exists() || trg.length() === 0) {
            log.info("Instrumentation", "create $trg")
            val jar: InputStream = InfoImpl::class.java.getResourceAsStream("/resource/lib/lucee-external-agent.jar")
                    ?: throw IOException("could not load jar [/resource/lib/lucee-external-agent.jar]")
            IOUtil.copy(jar, trg, true)
        }
        return trg
    }

    @Throws(IOException::class)
    private fun createToolsJar(config: Config?): Resource? {
        val dir: Resource? = getDeployDirectory(config)
        var os = "bsd" // used for Mac OS X
        if (SystemUtil.isWindows()) {
            os = "windows"
        } else if (SystemUtil.isLinux()) { // not MacOSX
            os = "linux"
        } else if (SystemUtil.isSolaris()) {
            os = "solaris"
        }
        val name = "tools-" + os + "-" + TOOLS_VERSION + ".jar"
        val trg: Resource = dir.getRealResource(name)
        if (!trg.exists() || trg.length() === 0) {
            val jar: InputStream = InfoImpl::class.java.getResourceAsStream("/resource/lib/$name")
            IOUtil.copy(jar, trg, true)
        }
        return trg
    }

    private fun getDeployDirectory(config: Config?): Resource? {
        var dir: Resource = ConfigWebUtil.getConfigServerDirectory(config)
        if (dir == null || !dir.isWriteable() || !dir.isReadable()) dir = ResourceUtil.toResource(CFMLEngineFactory.getClassLoaderRoot(SystemUtil.getLoaderClassLoader()))
        return dir
    }

    private fun getBinDirectory(config: Config?): Resource? {
        var dir: Resource = ConfigWebUtil.getConfigServerDirectory(config)
        if (dir == null || !dir.isWriteable() || !dir.isReadable()) dir = ResourceUtil.toResource(CFMLEngineFactory.getClassLoaderRoot(SystemUtil.getLoaderClassLoader())) else {
            dir = dir.getRealResource("bin")
            if (!dir.exists()) dir.mkdir()
        }
        return dir
    }

    /**
     * This private worker method attempts to find [java_home]/lib/tools.jar. Note: The tools.jar is a
     * part of the SDK, it is not present in the JRE.
     *
     * @return If tools.jar can be found, a File representing tools.jar. <BR></BR>
     * If tools.jar cannot be found, null.
     */
    private fun findToolsJar(config: Config?, log: Log?, useOurOwn: RefBoolean?): Resource? {
        log.info("Instrumentation", "looking for tools.jar")
        val javaHome: String = System.getProperty("java.home")
        var javaHomeFile: Resource = ResourcesImpl.getFileResourceProvider().getResource(javaHome)
        var toolsJarFile: Resource? = javaHomeFile.getRealResource("lib" + File.separator.toString() + "tools.jar")
        if (toolsJarFile.exists()) {
            useOurOwn.setValue(false)
            return toolsJarFile
        }
        log.info("Instrumentation", "couldn't find tools.jar at: " + toolsJarFile.getAbsolutePath())

        // If we're on an IBM SDK, then remove /jre off of java.home and try again.
        if (javaHomeFile.getAbsolutePath().endsWith(SEP.toString() + "jre")) {
            javaHomeFile = javaHomeFile.getParentResource()
            toolsJarFile = javaHomeFile.getRealResource("lib" + SEP + "tools.jar")
            if (!toolsJarFile.exists()) {
                log.info("Instrumentation", "for IBM SDK couldn't find " + toolsJarFile.getAbsolutePath())
            } else {
                useOurOwn.setValue(false)
                return toolsJarFile
            }
        } else if (System.getProperty("os.name").toLowerCase().indexOf("mac") >= 0) {
            // If we're on a Mac, then change the search path to use ../Classes/classes.jar.
            if (javaHomeFile.getAbsolutePath().endsWith(SEP.toString() + "Home")) {
                javaHomeFile = javaHomeFile.getParentResource()
                toolsJarFile = javaHomeFile.getRealResource("Classes" + SEP + "classes.jar")
                if (!toolsJarFile.exists()) {
                    log.info("Instrumentation", "for Mac OS couldn't find " + toolsJarFile.getAbsolutePath())
                } else {
                    useOurOwn.setValue(false)
                    return toolsJarFile
                }
            }
        }

        // if the engine could not find the tools.jar it is using it's own version
        try {
            toolsJarFile = createToolsJar(config)
        } catch (e: IOException) {
            log.log(Log.LEVEL_INFO, "Instrumentation", e)
        }
        if (!toolsJarFile.exists()) {
            log.info("Instrumentation", "could not be created " + toolsJarFile.getAbsolutePath())
            return null
        }
        log.info("Instrumentation", "found " + toolsJarFile.getAbsolutePath())
        return toolsJarFile
    }

    /**
     * Attach and load an agent class.
     *
     * @param log Log used if the agent cannot be loaded.
     * @param agentJar absolute path to the agent jar.
     * @param vmClass VirtualMachine.class from tools.jar.
     */
    private fun loadAgent(config: Config?, log: Log?, agentJar: String?, vmClass: Class<*>?) {
        try {

            // addAttach(config,log);

            // first obtain the PID of the currently-running process
            // ### this relies on the undocumented convention of the
            // RuntimeMXBean's
            // ### name starting with the PID, but there appears to be no other
            // ### way to obtain the current process' id, which we need for
            // ### the attach process
            val runtime: RuntimeMXBean = ManagementFactory.getRuntimeMXBean()
            var pid: String? = runtime.getName()
            if (pid.indexOf("@") !== -1) pid = pid.substring(0, pid.indexOf("@"))
            log.info("Instrumentation", "pid:$pid")
            // JDK1.6: now attach to the current VM so we can deploy a new agent
            // ### this is a Sun JVM specific feature; other JVMs may offer
            // ### this feature, but in an implementation-dependent way
            val vm: Object = vmClass.getMethod("attach", arrayOf<Class<*>?>(String::class.java)).invoke(null, arrayOf(pid))
            // now deploy the actual agent, which will wind up calling
            // agentmain()
            vmClass.getMethod("loadAgent", arrayOf<Class?>(String::class.java)).invoke(vm, arrayOf(agentJar))
            vmClass.getMethod("detach", arrayOf<Class?>()).invoke(vm, arrayOf<Object?>())
        } catch (t: Throwable) {
            ExceptionUtil.rethrowIfNecessary(t)
            // Log the message from the exception. Don't log the entire
            // stack as this is expected when running on a JDK that doesn't
            // support the Attach API.
            log.log(Log.LEVEL_INFO, "Instrumentation", t)
        }
    }

    private fun addAttachIfNecessary(config: Config?, log: Log?) {
        var srcName: String? = null
        var trgName: String? = null
        val archBits = if (SystemUtil.getJREArch() === SystemUtil.ARCH_64) "64" else "32"

        // Windows
        if (SystemUtil.isWindows()) {
            trgName = "attach.dll"
            srcName = "windows$archBits/$trgName"
        } else if (SystemUtil.isLinux()) {
            trgName = "libattach.so"
            srcName = "linux$archBits/$trgName"
        } else if (SystemUtil.isSolaris()) {
            trgName = "libattach.so"
            srcName = "solaris$archBits/$trgName"
        } else if (SystemUtil.isMacOSX()) {
            trgName = "libattach.dylib"
            srcName = "macosx$archBits/$trgName"
        }
        if (srcName != null) {

            // create dll if necessary
            val binDir: Resource? = getBinDirectory(config)
            val trg: Resource = binDir.getRealResource(trgName)
            if (!trg.exists() || trg.length() === 0) {
                log.info("Instrumentation", "deploy /resource/bin/$srcName to $trg")
                val src: InputStream = InfoImpl::class.java.getResourceAsStream("/resource/bin/$srcName")
                try {
                    IOUtil.copy(src, trg, true)
                } catch (e: IOException) {
                    log.log(Log.LEVEL_INFO, "Instrumentation", e)
                }
            }

            // set directory to library path
            SystemUtil.addLibraryPathIfNoExist(binDir, log)
        }
    }

    /**
     * If **ibm** is false, this private method will create a new URLClassLoader and attempt to load
     * the com.sun.tools.attach.VirtualMachine class from the provided toolsJar file.
     *
     *
     *
     * If **ibm** is true, this private method will ignore the toolsJar parameter and load the
     * com.ibm.tools.attach.VirtualMachine class.
     *
     *
     * @return The AttachAPI VirtualMachine class <br></br>
     * or null if something unexpected happened.
     */
    private fun loadVMClass(toolsJar: Resource?, log: Log?, vendor: JavaVendor?): Class<*>? {
        try {
            var loader: ClassLoader? = ClassLoader.getSystemClassLoader()
            val cls: String = vendor!!.getVirtualMachineClassName()
            // if (!vendor.isIBM()) {
            loader = URLClassLoader(arrayOf<URL?>((toolsJar as FileResource?).toURI().toURL()), loader)
            // }
            return loader.loadClass(cls)
        } catch (e: Exception) {
            log.log(Log.LEVEL_INFO, "Instrumentation", e)
        }
        return null
    }
}