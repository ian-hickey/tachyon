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
package lucee.commons.io

import java.io.File

/**
 *
 */
object SystemUtil {
    val MEMORY_TYPE_ALL: Int = lucee.runtime.util.SystemUtil.MEMORY_TYPE_ALL
    val MEMORY_TYPE_HEAP: Int = lucee.runtime.util.SystemUtil.MEMORY_TYPE_HEAP
    val MEMORY_TYPE_NON_HEAP: Int = lucee.runtime.util.SystemUtil.MEMORY_TYPE_NON_HEAP
    val ARCH_UNKNOW: Int = lucee.runtime.util.SystemUtil.ARCH_UNKNOW
    val ARCH_32: Int = lucee.runtime.util.SystemUtil.ARCH_32
    val ARCH_64: Int = lucee.runtime.util.SystemUtil.ARCH_64
    const val SETTING_CONTROLLER_DISABLED = "lucee.controller.disabled"
    const val SETTING_UPLOAD_EXT_BLACKLIST = "lucee.upload.blacklist"
    const val SETTING_UPLOAD_EXT_BLOCKLIST = "lucee.upload.blocklist"
    const val DEFAULT_UPLOAD_EXT_BLOCKLIST = "asp,aspx,cfc,cfm,cfml,do,htm,html,jsp,jspx,php"
    const val CHAR_DOLLAR = 36.toChar()
    const val CHAR_POUND = 163.toChar()
    const val CHAR_EURO = 8364.toChar()
    val JAVA_VERSION_6: Int = lucee.runtime.util.SystemUtil.JAVA_VERSION_1_6
    val JAVA_VERSION_7: Int = lucee.runtime.util.SystemUtil.JAVA_VERSION_1_7
    val JAVA_VERSION_8: Int = lucee.runtime.util.SystemUtil.JAVA_VERSION_1_8
    val JAVA_VERSION_9: Int = lucee.runtime.util.SystemUtil.JAVA_VERSION_1_9
    const val JAVA_VERSION_10 = 10 // FUTURE lucee.runtime.util.SystemUtil.JAVA_VERSION_10;
    const val JAVA_VERSION_11 = 11 // FUTURE lucee.runtime.util.SystemUtil.JAVA_VERSION_11;
    const val JAVA_VERSION_12 = 12 // FUTURE lucee.runtime.util.SystemUtil.JAVA_VERSION_12;
    const val JAVA_VERSION_13 = 13 // FUTURE lucee.runtime.util.SystemUtil.JAVA_VERSION_13;
    const val JAVA_VERSION_14 = 14 // FUTURE lucee.runtime.util.SystemUtil.JAVA_VERSION_14;
    val OUT: Int = lucee.runtime.util.SystemUtil.OUT
    val ERR: Int = lucee.runtime.util.SystemUtil.ERR
    private val PRINTWRITER_OUT: PrintWriter = PrintWriter(System.out)
    private val PRINTWRITER_ERR: PrintWriter = PrintWriter(System.err)
    private val printWriter: Array<PrintWriter?> = arrayOfNulls<PrintWriter>(2)
    val SYMBOL_EURO: Char = "\u20ac".charAt(0)
    val SYMBOL_POUND: Char = "\u00a3".charAt(0)
    val SYMBOL_MICRO: Char = "\u03bc".charAt(0)
    val SYMBOL_A_RING: Char = "\u00e5".charAt(0)

    /**
     * @return is local machine a Windows Machine
     */
    const val isWindows = false

    /**
     * @return is local machine a Solaris Machine
     */
    const val isSolaris = false

    /**
     * @return is local machine a Linux Machine
     */
    const val isLinux = false

    /**
     * @return is local machine a Solaris Machine
     */
    const val isMacOSX = false

    /**
     * @return is local machine a Unix Machine
     */
    const val isUnix = false
    private var homeFile: Resource? = null// java.ext.dirs

    // paths from system properties

    // paths from url class Loader (dynamic loaded classes)
    /**
     * @return returns a string list of all pathes
     */
    var classPathes: Array<Resource>?
        get() {
            if (field != null) return field
            val pathes: ArrayList<Resource> = ArrayList<Resource>()
            var pathSeperator: String = System.getProperty("path.separator")
            if (pathSeperator == null) pathSeperator = ";"

            // java.ext.dirs
            val frp: ResourceProvider = ResourcesImpl.getFileResourceProvider()

            // paths from system properties
            val strPathes: String = System.getProperty("java.class.path")
            if (strPathes != null) {
                val arr: Array = ListUtil.listToArrayRemoveEmpty(strPathes, pathSeperator)
                val len: Int = arr.size()
                for (i in 1..len) {
                    val file: Resource = frp.getResource(Caster.toString(arr.get(i, ""), "").trim())
                    if (file.exists()) pathes.add(ResourceUtil.getCanonicalResourceEL(file))
                }
            }

            // paths from url class Loader (dynamic loaded classes)
            val cl: ClassLoader = InfoImpl::class.java.getClassLoader()
            if (cl is URLClassLoader) getClassPathesFromClassLoader(cl as URLClassLoader, pathes)
            return pathes.toArray(arrayOfNulls<Resource>(pathes.size())).also { field = it }
        }
        private set
    private var charset: CharSet? = null
    val oSSpecificLineSeparator: String = System.getProperty("line.separator", "\n")
    private var permGenSpaceBean: MemoryPoolMXBean? = null
    var osArch = -1
    var jreArch = -1
    private val JAVA_VERSION_STRING: String = System.getProperty("java.version")
    const val JAVA_VERSION = 0
    private val EMPTY_CLASS: Array<Class?> = arrayOfNulls<Class>(0)
    private val EMPTY_OBJ: Array<Object?> = arrayOfNulls<Object>(0)
    private const val TYPE_BUNDLE = 1
    private const val TYPE_SYSTEM = 2
    private const val TYPE_BOOT_DELEGATION = 3
    private val tokens: ConcurrentHashMap<String, String> = ConcurrentHashMap<String, String>()
    private var loaderCL: ClassLoader? = null
    private var coreCL: ClassLoader? = null
    val loaderClassLoader: ClassLoader?
        get() {
            if (loaderCL == null) loaderCL = TP().getClass().getClassLoader()
            return loaderCL
        }
    val coreClassLoader: ClassLoader?
        get() {
            if (coreCL == null) coreCL = ClassLoaderHelper().getClass().getClassLoader()
            return coreCL
        }

    fun getPermGenSpaceBean(): MemoryPoolMXBean? {
        val manager: List<MemoryPoolMXBean> = ManagementFactory.getMemoryPoolMXBeans()
        var bean: MemoryPoolMXBean
        // PERM GEN
        var it: Iterator<MemoryPoolMXBean> = manager.iterator()
        while (it.hasNext()) {
            bean = it.next()
            if ("Perm Gen".equalsIgnoreCase(bean.getName()) || "CMS Perm Gen".equalsIgnoreCase(bean.getName())) {
                return bean
            }
        }
        it = manager.iterator()
        while (it.hasNext()) {
            bean = it.next()
            if (StringUtil.indexOfIgnoreCase(bean.getName(), "Perm Gen") !== -1 || StringUtil.indexOfIgnoreCase(bean.getName(), "PermGen") !== -1) {
                return bean
            }
        }
        // take none-heap when only one
        it = manager.iterator()
        val beans: LinkedList<MemoryPoolMXBean> = LinkedList<MemoryPoolMXBean>()
        while (it.hasNext()) {
            bean = it.next()
            if (bean.getType().equals(MemoryType.NON_HEAP)) {
                beans.add(bean)
                return bean
            }
        }
        if (beans.size() === 1) return beans.getFirst()

        // Class Memory/ClassBlock Memory?
        it = manager.iterator()
        while (it.hasNext()) {
            bean = it.next()
            if (StringUtil.indexOfIgnoreCase(bean.getName(), "Class Memory") !== -1) {
                return bean
            }
        }
        return null
    }

    private var isFSCaseSensitive: Boolean? = null
    private var jsm: JavaSysMon? = null
    private var isCLI: Boolean? = null

    // this is done via reflection to make it work in older version, where the class
    // lucee.loader.Version does not exist
    var loaderVersion = 0.0
        get() {
            // this is done via reflection to make it work in older version, where the class
            // lucee.loader.Version does not exist
            if (field == 0.0) {
                field = 4.0
                val cVersion: Class<*> = ClassUtil.loadClass(loaderClassLoader, "lucee.loader.Version", null)
                if (cVersion != null) {
                    try {
                        val f: Field = cVersion.getField("VERSION")
                        field = f.getDouble(null)
                    } catch (t: Throwable) {
                        ExceptionUtil.rethrowIfNecessary(t)
                    }
                }
            }
            return field
        }
        private set
    private var hasMacAddress = false

    @get:Throws(UnknownHostException::class, SocketException::class)
    var macAddress: String? = null
        get() {
            if (!hasMacAddress) {
                val ip: InetAddress = InetAddress.getLocalHost()
                val network: NetworkInterface = NetworkInterface.getByInetAddress(ip)
                if (network != null) {
                    val mac: ByteArray = network.getHardwareAddress()
                    if (mac != null) {
                        field = mac2String(mac)
                    }
                }
                if (StringUtil.isEmpty(field)) {
                    val nwInterface: Enumeration<NetworkInterface> = NetworkInterface.getNetworkInterfaces()
                    var nis: NetworkInterface
                    var mac: ByteArray
                    while (nwInterface.hasMoreElements()) {
                        nis = nwInterface.nextElement()
                        if (nis != null) {
                            mac = nis.getHardwareAddress()
                            if (mac != null && nis.isUp()) {
                                field = mac2String(mac)
                                break
                            }
                        }
                    }
                }
                hasMacAddress = true
            }
            return field
        }
        private set

    /**
     * returns if the file system case sensitive or not
     *
     * @return is the file system case sensitive or not
     */
    fun isFSCaseSensitive(): Boolean {
        if (isFSCaseSensitive == null) {
            try {
                _isFSCaseSensitive(File.createTempFile("abcx", "txt"))
            } catch (e: IOException) {
                val f: File = File("abcx.txt").getAbsoluteFile()
                try {
                    f.createNewFile()
                    _isFSCaseSensitive(f)
                } catch (e1: IOException) {
                    throw RuntimeException(e1.getMessage())
                }
            }
        }
        return isFSCaseSensitive.booleanValue()
    }

    private fun _isFSCaseSensitive(f: File) {
        val temp = File(f.getPath().toUpperCase())
        isFSCaseSensitive = if (temp.exists()) Boolean.FALSE else Boolean.TRUE
        f.delete()
    }

    /**
     * fixes a java canonical path to a Windows path e.g. /C:/Windows/System32 will be changed to
     * C:\Windows\System32
     *
     * @param path
     * @return
     */
    fun fixWindowsPath(path: String): String {
        var path = path
        if (isWindows && path.length() > 3 && path.charAt(0) === '/' && path.charAt(2) === ':') {
            path = path.substring(1).replace('/', '\\')
        }
        return path
    }/*
		 * if(tempFile!=null) return tempFile; ResourceProvider fr =
		 * ResourcesImpl.getFileResourceProvider(); String tmpStr = System.getProperty("java.io.tmpdir");
		 * if(tmpStr!=null) { tempFile=fr.getResource(tmpStr); if(tempFile.exists()) {
		 * tempFile=ResourceUtil.getCanonicalResourceEL(tempFile); return tempFile; } } File tmp =null; try
		 * { tmp = File.createTempFile("a","a"); tempFile=fr.getResource(tmp.getParent());
		 * tempFile=ResourceUtil.getCanonicalResourceEL(tempFile); } catch(IOException ioe) {} finally {
		 * if(tmp!=null)tmp.delete(); } return tempFile;
		 */

    /**
     * returns the Temp Directory of the System
     *
     * @return temp directory
     * @throws IOException
     */
    val tempDirectory: Resource
        get() = ResourcesImpl.getFileResourceProvider().getResource(CFMLEngineFactory.getTempDirectory().getAbsolutePath())

    /*
      * if(tempFile!=null) return tempFile; ResourceProvider fr =
      * ResourcesImpl.getFileResourceProvider(); String tmpStr = System.getProperty("java.io.tmpdir");
      * if(tmpStr!=null) { tempFile=fr.getResource(tmpStr); if(tempFile.exists()) {
      * tempFile=ResourceUtil.getCanonicalResourceEL(tempFile); return tempFile; } } File tmp =null; try
      * { tmp = File.createTempFile("a","a"); tempFile=fr.getResource(tmp.getParent());
      * tempFile=ResourceUtil.getCanonicalResourceEL(tempFile); } catch(IOException ioe) {} finally {
      * if(tmp!=null)tmp.delete(); } return tempFile;
      */
    /**
     * returns a unique temp file (with no auto delete)
     *
     * @param extension
     * @return temp directory
     * @throws IOException
     */
    @Throws(IOException::class)
    fun getTempFile(extension: String, touch: Boolean): Resource {
        var filename: String = CreateUniqueId.invoke()
        if (!StringUtil.isEmpty(extension, true)) {
            filename += if (extension.startsWith(".")) extension else ".$extension"
        }
        val file: Resource = tempDirectory.getRealResource(filename)
        if (touch) ResourceUtil.touch(file)
        return file
    }/*
		 * String pathes=System.getProperty("java.library.path"); ResourceProvider fr =
		 * ResourcesImpl.getFileResourceProvider(); if(pathes!=null) { String[]
		 * arr=ListUtil.toStringArrayEL(ListUtil.listToArray(pathes,File.pathSeparatorChar)); for(int
		 * i=0;i<arr.length;i++) { if(arr[i].toLowerCase().indexOf("windows\\system")!=-1) { Resource file =
		 * fr.getResource(arr[i]); if(file.exists() && file.isDirectory() && file.isWriteable()) return
		 * ResourceUtil.getCanonicalResourceEL(file);
		 * 
		 * } } for(int i=0;i<arr.length;i++) { if(arr[i].toLowerCase().indexOf("windows")!=-1) { Resource
		 * file = fr.getResource(arr[i]); if(file.exists() && file.isDirectory() && file.isWriteable())
		 * return ResourceUtil.getCanonicalResourceEL(file);
		 * 
		 * } } for(int i=0;i<arr.length;i++) { if(arr[i].toLowerCase().indexOf("winnt")!=-1) { Resource file
		 * = fr.getResource(arr[i]); if(file.exists() && file.isDirectory() && file.isWriteable()) return
		 * ResourceUtil.getCanonicalResourceEL(file);
		 * 
		 * } } for(int i=0;i<arr.length;i++) { if(arr[i].toLowerCase().indexOf("win")!=-1) { Resource file =
		 * fr.getResource(arr[i]); if(file.exists() && file.isDirectory() && file.isWriteable()) return
		 * ResourceUtil.getCanonicalResourceEL(file);
		 * 
		 * } } for(int i=0;i<arr.length;i++) { Resource file = fr.getResource(arr[i]); if(file.exists() &&
		 * file.isDirectory() && file.isWriteable()) return ResourceUtil.getCanonicalResourceEL(file); } }
		 * return null;
		 */

    /**
     * @return return System directory
     */
    val systemDirectory: Resource
        get() = ResourcesImpl.getFileResourceProvider().getResource(CFMLEngineFactory.getSystemDirectory().getAbsolutePath())

    /*
      * String pathes=System.getProperty("java.library.path"); ResourceProvider fr =
      * ResourcesImpl.getFileResourceProvider(); if(pathes!=null) { String[]
      * arr=ListUtil.toStringArrayEL(ListUtil.listToArray(pathes,File.pathSeparatorChar)); for(int
      * i=0;i<arr.length;i++) { if(arr[i].toLowerCase().indexOf("windows\\system")!=-1) { Resource file =
      * fr.getResource(arr[i]); if(file.exists() && file.isDirectory() && file.isWriteable()) return
      * ResourceUtil.getCanonicalResourceEL(file);
      * 
      * } } for(int i=0;i<arr.length;i++) { if(arr[i].toLowerCase().indexOf("windows")!=-1) { Resource
      * file = fr.getResource(arr[i]); if(file.exists() && file.isDirectory() && file.isWriteable())
      * return ResourceUtil.getCanonicalResourceEL(file);
      * 
      * } } for(int i=0;i<arr.length;i++) { if(arr[i].toLowerCase().indexOf("winnt")!=-1) { Resource file
      * = fr.getResource(arr[i]); if(file.exists() && file.isDirectory() && file.isWriteable()) return
      * ResourceUtil.getCanonicalResourceEL(file);
      * 
      * } } for(int i=0;i<arr.length;i++) { if(arr[i].toLowerCase().indexOf("win")!=-1) { Resource file =
      * fr.getResource(arr[i]); if(file.exists() && file.isDirectory() && file.isWriteable()) return
      * ResourceUtil.getCanonicalResourceEL(file);
      * 
      * } } for(int i=0;i<arr.length;i++) { Resource file = fr.getResource(arr[i]); if(file.exists() &&
      * file.isDirectory() && file.isWriteable()) return ResourceUtil.getCanonicalResourceEL(file); } }
      * return null;
      */
    /**
     * @return return running context root
     */
    val runingContextRoot: Resource?
        get() {
            val frp: ResourceProvider = ResourcesImpl.getFileResourceProvider()
            try {
                return frp.getResource(".").getCanonicalResource()
            } catch (e: IOException) {
            }
            val url: URL = InfoImpl::class.java.getClassLoader().getResource(".")
            return try {
                frp.getResource(FileUtil.URLToFile(url).getAbsolutePath())
            } catch (e: MalformedURLException) {
                null
            }
        }

    /**
     * returns the Hoome Directory of the System
     *
     * @return home directory
     */
    val homeDirectory: Resource?
        get() {
            if (homeFile != null) return homeFile
            val frp: ResourceProvider = ResourcesImpl.getFileResourceProvider()
            val homeStr: String = System.getProperty("user.home")
            if (homeStr != null) {
                homeFile = frp.getResource(homeStr)
                homeFile = ResourceUtil.getCanonicalResourceEL(homeFile)
            }
            return homeFile
        }
    val classLoaderDirectory: Resource
        get() = ResourceUtil.toResource(CFMLEngineFactory.getClassLoaderRoot(TP::class.java.getClassLoader()))

    /**
     * get class pathes from all url ClassLoaders
     *
     * @param ucl URL Class Loader
     * @param pathes Hashmap with allpathes
     */
    private fun getClassPathesFromClassLoader(ucl: URLClassLoader, pathes: ArrayList<Resource>) {
        val pcl: ClassLoader = ucl.getParent()
        // parent first
        if (pcl is URLClassLoader) getClassPathesFromClassLoader(pcl as URLClassLoader, pathes)
        val frp: ResourceProvider = ResourcesImpl.getFileResourceProvider()
        // get all pathes
        val urls: Array<URL> = ucl.getURLs()
        for (i in urls.indices) {
            val file: Resource = frp.getResource(urls[i].getPath())
            if (file.exists()) pathes.add(ResourceUtil.getCanonicalResourceEL(file))
        }
    }

    val usedMemory: Long
        get() {
            val r: Runtime = Runtime.getRuntime()
            return r.totalMemory() - r.freeMemory()
        }
    val availableMemory: Long
        get() {
            val r: Runtime = Runtime.getRuntime()
            return r.freeMemory()
        }

    /**
     * return the memory percentage
     *
     * @return value from 0 to 1
     */
    val memoryPercentage: Float
        get() {
            val r: Runtime = Runtime.getRuntime()
            val max: Long = r.maxMemory()
            if (max == Long.MAX_VALUE || max < 0) return (-1).toFloat()
            val used: Long = r.totalMemory() - r.freeMemory()
            return 1f / max * used
        }

    /**
     * replace path placeholder with the real path, placeholders are
     * [{temp-directory},{system-directory},{home-directory}]
     *
     * @param path
     * @return updated path
     */
    fun parsePlaceHolder(path: String?): String {
        return CFMLEngineFactory.parsePlaceHolder(path)
    }

    fun addPlaceHolder(file: Resource?, defaultValue: String): String {
        // Temp
        var path: String = addPlaceHolder(tempDirectory, file, "{temp-directory}")
        if (!StringUtil.isEmpty(path)) return path
        // System
        path = addPlaceHolder(systemDirectory, file, "{system-directory}")
        if (!StringUtil.isEmpty(path)) return path
        // Home
        path = addPlaceHolder(homeDirectory, file, "{home-directory}")
        return if (!StringUtil.isEmpty(path)) path else defaultValue
    }

    private fun addPlaceHolder(dir: Resource, file: Resource, placeholder: String): String? {
        if (ResourceUtil.isChildOf(file, dir)) {
            try {
                return StringUtil.replace(file.getCanonicalPath(), dir.getCanonicalPath(), placeholder, true)
            } catch (e: IOException) {
            }
        }
        return null
    }

    fun addPlaceHolder(file: Resource?, config: Config, defaultValue: String): String {
        // ResourceProvider frp = ResourcesImpl.getFileResourceProvider();

        // temp
        var dir: Resource = config.getTempDirectory()
        var path: String = addPlaceHolder(dir, file, "{temp-directory}")
        if (!StringUtil.isEmpty(path)) return path

        // Config
        dir = config.getConfigDir()
        path = addPlaceHolder(dir, file, "{lucee-config-directory}")
        if (!StringUtil.isEmpty(path)) return path

        // Web root
        dir = config.getRootDirectory()
        path = addPlaceHolder(dir, file, "{web-root-directory}")
        return if (!StringUtil.isEmpty(path)) path else addPlaceHolder(file, defaultValue)
    }

    fun parsePlaceHolder(path: String?, sc: ServletContext?, labels: Map<String?, String?>): String? {
        var path = path ?: return null
        if (path.indexOf('{') !== -1) {
            if (path.indexOf("{web-context-label}") !== -1) {
                val id = hash(sc)
                var label = labels[id]
                if (StringUtil.isEmpty(label)) label = id
                path = StringUtil.replace(path, "{web-context-label}", label, false)
            }
        }
        return parsePlaceHolder(path, sc)
    }

    fun parsePlaceHolder(path: String?, sc: ServletContext?): String? {
        var path = path
        val frp: ResourceProvider = ResourcesImpl.getFileResourceProvider()
        if (path == null) return null
        if (path.indexOf('{') !== -1) {
            if (StringUtil.startsWith(path, '{')) {

                // Web Root
                if (path.startsWith("{web-root")) {
                    if (path.startsWith("}", 9)) path = frp.getResource(ReqRspUtil.getRootPath(sc)).getRealResource(path.substring(10)).toString() else if (path.startsWith("-dir}", 9)) path = frp.getResource(ReqRspUtil.getRootPath(sc)).getRealResource(path.substring(14)).toString() else if (path.startsWith("-directory}", 9)) path = frp.getResource(ReqRspUtil.getRootPath(sc)).getRealResource(path.substring(20)).toString()
                } else path = parsePlaceHolder(path)
            }
            if (path.indexOf("{web-context-hash}") !== -1) {
                val id = hash(sc)
                path = StringUtil.replace(path, "{web-context-hash}", id, false)
            }
        }
        return path
    }

    fun hash(sc: ServletContext?): String? {
        var id: String? = null
        try {
            id = MD5.getDigestAsString(ReqRspUtil.getRootPath(sc))
        } catch (e: IOException) {
        }
        return id
    }

    fun getCharset(): Charset {
        return CharsetUtil.toCharset(charset)
    }

    val charSet: CharSet?
        get() = charset

    fun setCharset(charset: String?) {
        SystemUtil.charset = CharsetUtil.toCharSet(charset)
    }

    fun setCharset(charset: Charset?) {
        SystemUtil.charset = CharsetUtil.toCharSet(charset)
    }

    fun sleep(time: Int) {
        try {
            Thread.sleep(time)
        } catch (e: InterruptedException) {
        }
    }

    fun sleep(time: Long) {
        try {
            Thread.sleep(time)
        } catch (e: InterruptedException) {
        }
    }

    fun join(t: Thread) {
        try {
            t.join()
        } catch (e: InterruptedException) {
        }
    }

    fun resumeEL(t: Thread?) {
        try {
            t.resume()
        } catch (e: Exception) {
        }
    }

    fun suspendEL(t: Thread?) {
        try {
            t.suspend()
        } catch (e: Exception) {
        }
    }

    /**
     * locks the object (synchronized) before calling wait
     *
     * @param lock
     * @param timeout
     * @throws InterruptedException
     */
    fun wait(lock: Object, timeout: Long) {
        try {
            synchronized(lock) { lock.wait(timeout) }
        } catch (e: InterruptedException) {
        }
    }

    fun wait(lock: Object, timeout: Int) {
        try {
            synchronized(lock) { lock.wait(timeout) }
        } catch (e: InterruptedException) {
        }
    }

    /**
     * locks the object (synchronized) before calling wait (no timeout)
     *
     * @param lock
     * @throws InterruptedException
     */
    fun wait(lock: Object) {
        try {
            synchronized(lock) { lock.wait() }
        } catch (e: InterruptedException) {
        }
    }

    /**
     * locks the object (synchronized) before calling notify
     *
     * @param lock
     * @param timeout
     * @throws InterruptedException
     */
    fun notify(lock: Object) {
        synchronized(lock) { lock.notify() }
    }

    /**
     * locks the object (synchronized) before calling notifyAll
     *
     * @param lock
     * @param timeout
     * @throws InterruptedException
     */
    fun notifyAll(lock: Object) {
        synchronized(lock) { lock.notifyAll() }
    }

    /**
     * return the operating system architecture
     *
     * @return one of the following SystemUtil.ARCH_UNKNOW, SystemUtil.ARCH_32, SystemUtil.ARCH_64
     */
    val oSArch: Int
        get() {
            if (osArch == -1) {
                osArch = toIntArch(System.getProperty("os.arch.data.model"))
                if (osArch == ARCH_UNKNOW) osArch = toIntArch(System.getProperty("os.arch"))
            }
            return osArch
        }

    /**
     * return the JRE (Java Runtime Engine) architecture, this can be different from the operating
     * system architecture
     *
     * @return one of the following SystemUtil.ARCH_UNKNOW, SystemUtil.ARCH_32, SystemUtil.ARCH_64
     */
    val jREArch: Int
        get() {
            if (jreArch == -1) {
                jreArch = toIntArch(System.getProperty("sun.arch.data.model"))
                if (jreArch == ARCH_UNKNOW) jreArch = toIntArch(System.getProperty("com.ibm.vm.bitmode"))
                if (jreArch == ARCH_UNKNOW) jreArch = toIntArch(System.getProperty("java.vm.name"))
                if (jreArch == ARCH_UNKNOW) {
                    val addrSize = addressSize
                    if (addrSize == 4) return ARCH_32
                    if (addrSize == 8) return ARCH_64
                }
            }
            return jreArch
        }

    private fun toIntArch(strArch: String): Int {
        if (!StringUtil.isEmpty(strArch)) {
            if (strArch.indexOf("64") !== -1) return ARCH_64
            if (strArch.indexOf("32") !== -1) return ARCH_32
            if (strArch.indexOf("i386") !== -1) return ARCH_32
            if (strArch.indexOf("x86") !== -1) return ARCH_32
        }
        return ARCH_UNKNOW
    }

    val addressSize: Int
        get() = try {
            val unsafe: Class<*> = ClassUtil.loadClass("sun.misc.Unsafe", null) ?: return 0
            val unsafeField: Field = unsafe.getDeclaredField("theUnsafe")
            unsafeField.setAccessible(true)
            val obj: Object = unsafeField.get(null)
            val addressSize: Method = unsafe.getMethod("addressSize", arrayOfNulls<Class>(0))
            val res: Object = addressSize.invoke(obj, arrayOfNulls<Object>(0))
            Caster.toIntValue(res, 0)
        } catch (t: Throwable) {
            ExceptionUtil.rethrowIfNecessary(t)
            0
        }

    /*
	 * private static MemoryUsage getPermGenSpaceSize() { MemoryUsage mu = getPermGenSpaceSize(null);
	 * if(mu!=null) return mu;
	 * 
	 * // create error message including info about available memory blocks StringBuilder sb=new
	 * StringBuilder(); java.util.List<MemoryPoolMXBean> manager =
	 * ManagementFactory.getMemoryPoolMXBeans(); Iterator<MemoryPoolMXBean> it = manager.iterator();
	 * MemoryPoolMXBean bean; while(it.hasNext()){ bean = it.next(); if(sb.length()>0)sb.append(", ");
	 * sb.append(bean.getName()); } throw new
	 * RuntimeException("PermGen Space information not available, available Memory blocks are ["+sb+"]")
	 * ; }
	 */
    private fun getPermGenSpaceSize(defaultValue: MemoryUsage?): MemoryUsage? {
        if (permGenSpaceBean != null) return permGenSpaceBean.getUsage()
        // create on the fly when the bean is not permanent
        val tmp: MemoryPoolMXBean? = getPermGenSpaceBean()
        return if (tmp != null) tmp.getUsage() else defaultValue
    }

    val freePermGenSpaceSize: Long
        get() {
            val mu: MemoryUsage = getPermGenSpaceSize(null) ?: return -1
            val max: Long = mu.getMax()
            val used: Long = mu.getUsed()
            return if (max < 0 || used < 0) -1 else max - used
        }

    // return a value that equates to a percentage of available free memory
    val permGenFreeSpaceAsAPercentageOfAvailable: Int
        get() {
            val mu: MemoryUsage = getPermGenSpaceSize(null) ?: return -1
            val max: Long = mu.getMax()
            val used: Long = mu.getUsed()
            return if (max < 0 || used < 0) -1 else 100 - (100 * (used.toDouble() / max.toDouble())).toInt()

            // return a value that equates to a percentage of available free memory
        }
    val freePermGenSpacePromille: Int
        get() {
            val mu: MemoryUsage = getPermGenSpaceSize(null) ?: return -1
            val max: Long = mu.getMax()
            val used: Long = mu.getUsed()
            return if (max < 0 || used < 0) -1 else (1000L - 1000L * used / max).toInt()
        }

    @Throws(DatabaseException::class)
    fun getMemoryUsageAsQuery(type: Int): Query {
        val manager: List<MemoryPoolMXBean> = ManagementFactory.getMemoryPoolMXBeans()
        val it: Iterator<MemoryPoolMXBean> = manager.iterator()
        val qry: Query = QueryImpl(arrayOf<Collection.Key>(KeyConstants._name, KeyConstants._type, KeyConstants._used, KeyConstants._max, KeyConstants._init), 0, "memory")
        var row = 0
        var bean: MemoryPoolMXBean
        var usage: MemoryUsage
        var _type: MemoryType
        while (it.hasNext()) {
            bean = it.next()
            usage = bean.getUsage()
            _type = bean.getType()
            if (type == MEMORY_TYPE_HEAP && _type !== MemoryType.HEAP) continue
            if (type == MEMORY_TYPE_NON_HEAP && _type !== MemoryType.NON_HEAP) continue
            row++
            qry.addRow()
            qry.setAtEL(KeyConstants._name, row, bean.getName())
            qry.setAtEL(KeyConstants._type, row, _type.name())
            qry.setAtEL(KeyConstants._max, row, Caster.toDouble(usage.getMax()))
            qry.setAtEL(KeyConstants._used, row, Caster.toDouble(usage.getUsed()))
            qry.setAtEL(KeyConstants._init, row, Caster.toDouble(usage.getInit()))
        }
        return qry
    }

    fun getMemoryUsageAsStruct(type: Int): Struct {
        val manager: List<MemoryPoolMXBean> = ManagementFactory.getMemoryPoolMXBeans()
        val it: Iterator<MemoryPoolMXBean> = manager.iterator()
        var bean: MemoryPoolMXBean
        var usage: MemoryUsage
        var _type: MemoryType
        var used: Long = 0
        var max: Long = 0
        var init: Long = 0
        while (it.hasNext()) {
            bean = it.next()
            usage = bean.getUsage()
            _type = bean.getType()
            if (type == MEMORY_TYPE_HEAP && _type === MemoryType.HEAP || type == MEMORY_TYPE_NON_HEAP && _type === MemoryType.NON_HEAP) {
                used += usage.getUsed()
                max += usage.getMax()
                init += usage.getInit()
            }
        }
        val sct: Struct = StructImpl()
        sct.setEL(KeyConstants._used, Caster.toDouble(used))
        sct.setEL(KeyConstants._max, Caster.toDouble(max))
        sct.setEL(KeyConstants._init, Caster.toDouble(init))
        sct.setEL(KeyConstants._available, Caster.toDouble(max - used))
        return sct
    }

    fun getMemoryUsageCompact(type: Int): Struct {
        val manager: List<MemoryPoolMXBean> = ManagementFactory.getMemoryPoolMXBeans()
        val it: Iterator<MemoryPoolMXBean> = manager.iterator()
        var bean: MemoryPoolMXBean
        var usage: MemoryUsage
        var _type: MemoryType
        val sct: Struct = StructImpl()
        while (it.hasNext()) {
            bean = it.next()
            usage = bean.getUsage()
            _type = bean.getType()
            if (type == MEMORY_TYPE_HEAP && _type !== MemoryType.HEAP) continue
            if (type == MEMORY_TYPE_NON_HEAP && _type !== MemoryType.NON_HEAP) continue
            val d = (100.0 / usage.getMax() * usage.getUsed()) as Int / 100.0
            sct.setEL(KeyImpl.init(bean.getName()), Caster.toDouble(d))
        }
        return sct
    }

    fun getPropertyEL(key: String): String? {
        try {
            val str: String = System.getProperty(key)
            if (!StringUtil.isEmpty(str, true)) return str
            val it: Iterator<Entry<Object, Object>> = System.getProperties().entrySet().iterator()
            var e: Entry<Object, Object>
            var n: String
            while (it.hasNext()) {
                e = it.next()
                n = e.getKey()
                if (key.equalsIgnoreCase(n)) return e.getValue()
            }
        } catch (t: Throwable) {
            ExceptionUtil.rethrowIfNecessary(t)
        }
        return null
    }

    fun microTime(): Long {
        return System.nanoTime() / 1000L
    }

    fun getCurrentContext(pc: PageContext?): TemplateLine? {
        // StackTraceElement[] traces = new Exception().getStackTrace();
        var pc: PageContext? = pc
        val traces: Array<StackTraceElement> = Thread.currentThread().getStackTrace()
        var line = 0
        var template: String
        var trace: StackTraceElement? = null
        for (i in traces.indices) {
            trace = traces[i]
            template = trace.getFileName()
            if (trace.getLineNumber() <= 0 || template == null || ResourceUtil.getExtension(template, "").equals("java")) continue
            line = trace.getLineNumber()
            try {
                pc = ThreadLocalPageContext.get(pc)
                if (pc != null) template = ExpandPath.call(pc, template)
            } catch (e: PageException) {
            } // optional step, so in case it fails we are still fine
            return TemplateLine(template, line)
        }
        return null
    }

    @get:Throws(ApplicationException::class)
    val freeBytes: Long
        get() = physical().getFreeBytes()

    @get:Throws(ApplicationException::class)
    val totalBytes: Long
        get() = physical().getTotalBytes()

    @Throws(ApplicationException::class)
    fun getCpuUsage(time: Long): Double {
        if (time < 1) throw ApplicationException("time has to be bigger than 0")
        if (jsm == null) jsm = JavaSysMon()
        val cput: CpuTimes = jsm.cpuTimes()
                ?: throw ApplicationException("CPU information are not available for this OS")
        val previous = CpuTimes(cput.getUserMillis(), cput.getSystemMillis(), cput.getIdleMillis())
        sleep(time)
        return jsm.cpuTimes().getCpuUsage(previous) * 100.0
    }

    val cpuPercentage: Float
        get() {
            if (jsm == null) jsm = JavaSysMon()
            val cput: CpuTimes = jsm.cpuTimes() ?: return (-1).toFloat()
            val previous = CpuTimes(cput.getUserMillis(), cput.getSystemMillis(), cput.getIdleMillis())
            var max = 50
            var res = 0f
            while (true) {
                if (--max == 0) break
                sleep(100)
                res = jsm.cpuTimes().getCpuUsage(previous)
                if (res != 1f) break
            }
            return res
        }

    @Synchronized
    @Throws(ApplicationException::class)
    private fun physical(): MemoryStats {
        if (jsm == null) jsm = JavaSysMon()
        return jsm.physical()
                ?: throw ApplicationException("Memory information are not available for this OS")
    }

    fun setPrintWriter(type: Int, pw: PrintWriter?) {
        printWriter[type] = pw
    }

    fun getPrintWriter(type: Int): PrintWriter? {
        if (printWriter[type] == null) {
            if (type == OUT) printWriter[OUT] = PRINTWRITER_OUT else printWriter[ERR] = PRINTWRITER_ERR
        }
        return printWriter[type]
    }

    val isCLICall: Boolean
        get() {
            if (isCLI == null) {
                isCLI = Caster.toBoolean(System.getProperty("lucee.cli.call"), Boolean.FALSE)
            }
            return isCLI.booleanValue()
        }

    fun getMacAddress(defaultValue: String?): String? {
        return try {
            macAddress
        } catch (e: Exception) {
            defaultValue
        }
    }

    /**
     * loading Mac address is very slow, this method loads only a wrapper that then loads the mac
     * address itself on demand
     *
     * @return
     */
    val macAddressAsWrap: MacAddressWrap
        get() = MacAddressWrap()

    private fun mac2String(mac: ByteArray?): String {
        val sb = StringBuilder()
        for (i in mac.indices) {
            sb.append(String.format("%02X%s", mac!![i], if (i < mac.size - 1) "-" else ""))
        }
        return sb.toString()
    }

    fun getResource(bundle: Bundle?, path: String): URL? {
        val pws: String
        val pns: String
        if (path.startsWith("/")) {
            pws = path
            pns = path.substring(1)
        } else {
            pws = "/$path"
            pns = path
        }
        var url: URL? = null
        if (bundle != null) {
            try {
                url = bundle.getEntry(pns)
            } catch (t: Throwable) {
                ExceptionUtil.rethrowIfNecessary(t)
            }
        }

        // core class loader
        if (url == null) {
            val clazz: Class<*> = PageSourceImpl::class.java
            val cl: ClassLoader = clazz.getClassLoader()
            url = cl.getResource(pns)
            if (url == null) {
                url = cl.getResource(pws)
            }
        }
        return url
    }

    /**
     * converts a System property format to its equivalent Environment variable, e.g. an input of
     * "lucee.conf.name" will return "LUCEE_CONF_NAME"
     *
     * @param name the System property name
     * @return the equivalent Environment variable name
     */
    fun convertSystemPropToEnvVar(name: String): String {
        return name.replace('.', '_').toUpperCase()
    }

    /**
     * returns a system setting by either a Java property name or a System environment variable
     *
     * @param name - either a lowercased Java property name (e.g. lucee.controller.disabled) or an
     * UPPERCASED Environment variable name ((e.g. LUCEE_CONTROLLER_DISABLED))
     * @param defaultValue - value to return if the neither the property nor the environment setting was
     * found
     * @return - the value of the property referenced by propOrEnv or the defaultValue if not found
     */
    fun getSystemPropOrEnvVar(name: String, defaultValue: String?): String? {
        // env
        var name = name
        var value: String = System.getenv(name)
        if (!StringUtil.isEmpty(value)) return value

        // prop
        value = System.getProperty(name)
        if (!StringUtil.isEmpty(value)) return value

        // env 2
        name = convertSystemPropToEnvVar(name)
        value = System.getenv(name)
        return if (!StringUtil.isEmpty(value)) value else defaultValue
    }

    fun addLibraryPathIfNoExist(res: Resource, log: Log?) {
        val existing: String = System.getProperty("java.library.path")
        if (StringUtil.isEmpty(existing)) {
            if (log != null) log.info("Instrumentation", "add " + res.getAbsolutePath().toString() + " to library path")
            System.setProperty("java.library.path", res.getAbsolutePath())
        } else if (existing.indexOf(res.getAbsolutePath()) !== -1) {
            return
        } else {
            if (log != null) log.info("Instrumentation", "add " + res.getAbsolutePath().toString() + " to library path")
            System.setProperty("java.library.path", res.getAbsolutePath().toString() + (if (isWindows) ";" else ":") + existing)
        }
        // Important: java.library.path is cached
        // We will be using reflection to clear the cache
        try {
            val fieldSysPath: Field = ClassLoader::class.java.getDeclaredField("sys_paths")
            fieldSysPath.setAccessible(true)
            fieldSysPath.set(null, null)
        } catch (t: Throwable) {
            ExceptionUtil.rethrowIfNecessary(t)
        }
    }

    fun stop(pc: PageContext, async: Boolean) {
        if (async) StopThread(pc).start() else stop(pc, pc.getThread())
    }

    fun stop(thread: Thread?) {
        stop(null as PageContext?, thread)
    }

    fun stop(pc: PageContext, thread: Thread?) {
        // if (thread == null || !thread.isAlive() || thread == Thread.currentThread() ||
        // ThreadUtil.isInNativeMethod(thread, false)) return;
        if (thread == null || !thread.isAlive() || thread === Thread.currentThread()) return
        var log: Log? = null
        // in case it is the request thread
        if (pc is PageContextImpl && thread === pc.getThread()) {
            (pc as PageContextImpl).setTimeoutStackTrace()
            log = ThreadLocalPageContext.getLog(pc, "requesttimeout")
        }

        // first we try to interupt, the we force a stop
        if (!_stop(thread, log, false)) _stop(thread, log, true)
    }

    private fun _stop(thread: Thread?, log: Log?, force: Boolean): Boolean {
        // we try to interrupt/stop the suspended thrad
        suspendEL(thread)
        try {
            if (isInLucee(thread)) {
                if (!force) thread.interrupt() else thread.stop()
            } else {
                if (log != null) {
                    log.log(Log.LEVEL_INFO, "thread", "do not " + (if (force) "stop" else "interrupt") + " thread because thread is not within Lucee code",
                            ExceptionUtil.toThrowable(thread.getStackTrace()))
                }
                return true
            }
        } finally {
            resumeEL(thread)
        }

        // a request still will create the error template output, so it can take some time to finish
        for (i in 0..99) {
            if (!isInLucee(thread)) {
                if (log != null) log.info("thread", "sucessfully " + (if (force) "stop" else "interrupt") + " thread.")
                return true
            }
            sleep(10)
        }
        if (log != null) {
            log.log(if (force) Log.LEVEL_ERROR else Log.LEVEL_WARN, "thread", """failed to ${if (force) "stop" else "interrupt"} thread.
""",
                    ExceptionUtil.toThrowable(thread.getStackTrace()))
        }
        return false
    }

    val localHostName: String
        get() {
            val result: String = System.getenv(if (isWindows) "COMPUTERNAME" else "HOSTNAME")
            return if (!StringUtil.isEmpty(result)) result else try {
                InetAddress.getLocalHost().getHostName()
            } catch (ex: UnknownHostException) {
                ""
            }
        }

    fun getResourceAsStream(bundle: Bundle?, path: String?): InputStream? {
        // check the bundle for the resource
        var `is`: InputStream
        if (bundle != null) {
            try {
                `is` = bundle.getEntry(path).openStream()
                if (`is` != null) return `is`
            } catch (t: Throwable) {
                ExceptionUtil.rethrowIfNecessary(t)
            }
        }

        // try from core classloader
        var cl: ClassLoader = PageSourceImpl::class.java.getClassLoader()
        try {
            `is` = cl.getResourceAsStream(path)
            if (`is` != null) return `is`
        } catch (t: Throwable) {
            ExceptionUtil.rethrowIfNecessary(t)
        }

        // try from loader classloader
        cl = PageSource::class.java.getClassLoader()
        try {
            `is` = cl.getResourceAsStream(path)
            if (`is` != null) return `is`
        } catch (t: Throwable) {
            ExceptionUtil.rethrowIfNecessary(t)
        }

        // try from loader classloader
        cl = ClassLoader.getSystemClassLoader()
        try {
            `is` = cl.getResourceAsStream(path)
            if (`is` != null) return `is`
        } catch (t: Throwable) {
            ExceptionUtil.rethrowIfNecessary(t)
        }
        return null
    }

    /**
     * @return returns a class stack trace
     */
    val classContext: Array<Any?>
        get() {
            val ref = Ref()
            object : SecurityManager() {
                init {
                    ref.context = classContext
                }
            }
            val context: Array<Class<*>?> = arrayOfNulls<Class>(ref.context.size - 2)
            System.arraycopy(ref.context, 2, context, 0, ref.context.size - 2)
            return context
        }// element at position 2 is the caller

    // analyze the first result
    /**
     *
     * @return the class calling me and the first class not in bootdelegation if the the is in
     * bootdelegation
     */
    val callerClass: Caller
        get() {
            val ref = Ref()
            object : SecurityManager() {
                init {
                    ref.context = classContext
                }
            }
            val rtn = Caller()

            // element at position 2 is the caller
            val caller: Class<*> = ref.context[2]
            var index: RefInteger = RefIntegerImpl(3)
            var clazz: Class<*>? = _getCallerClass(ref.context, caller, index, true, true)
                    ?: return rtn

            // analyze the first result
            if (isFromBundle(clazz)) {
                rtn.fromBundle = clazz
                return rtn
            }
            if (!OSGiUtil.isClassInBootelegation(clazz.getName())) {
                rtn.fromSystem = clazz
            } else {
                rtn.fromBootDelegation = clazz
            }
            clazz = null
            if (rtn.fromBootDelegation != null) {
                index = RefIntegerImpl(3)
                clazz = _getCallerClass(ref.context, caller, index, false, true)
                if (clazz == null) return rtn
                if (isFromBundle(clazz)) {
                    rtn.fromBundle = clazz
                    return rtn
                } else rtn.fromSystem = clazz
            }
            clazz = _getCallerClass(ref.context, caller, index, false, false)
            if (clazz == null) return rtn
            rtn.fromBundle = clazz
            return rtn
        }

    fun getClassLoaderContext(unique: Boolean, id: StringBuilder?): List<ClassLoader> {
        val ref = Ref()
        object : SecurityManager() {
            init {
                ref.context = classContext
            }
        }

        // first we get the right start point, pos 0 is here so we start with 1
        val directCaller: Class<*> = ref.context[2]
        var start = -1
        for (i in 2 until ref.context.size) {
            if (directCaller !== ref.context[i]) {
                start = i
                break
            }
        }

        // extract all the classes
        var last: Class<*>? = null
        var clazz: Class<*>
        // LinkedHashMap<Class<?>, String> map = new LinkedHashMap<>();
        val context: List<ClassLoader> = ArrayList<ClassLoader>()
        var cl: ClassLoader? = null
        for (i in start until ref.context.size) {
            clazz = ref.context[i]

            // check class
            if (Class::class.java === clazz) continue  // the same as the last
            if (last === clazz) continue  // the same as the last
            if (last != null && last.getClassLoader() === clazz.getClassLoader()) continue  // same Classloader
            cl = clazz.getClassLoader()

            // check ClassLoader
            if (cl == null) continue
            if (cl is PhysicalClassLoader) continue
            if (cl is ArchiveClassLoader) continue
            if (cl is MemoryClassLoader) continue
            if (!unique || !context.contains(cl)) {
                context.add(cl)
                if (id != null) {
                    if (cl is BundleReference) {
                        val b: Bundle = (cl as BundleReference).getBundle()
                        id.append(b.getSymbolicName()).append(':').append(b.getVersion()).append(';')
                    } else {
                        id.append(cl).append(';')
                    }
                }
            }
            last = ref.context[i]
        }
        return context
    }

    fun getClassType(clazz: Class<*>): Int {
        if (isFromBundle(clazz)) return TYPE_BUNDLE
        return if (OSGiUtil.isClassInBootelegation(clazz.getName())) TYPE_BOOT_DELEGATION else TYPE_SYSTEM
    }

    fun getClassTypeAsString(clazz: Class<*>): String {
        val type = getClassType(clazz)
        if (type == TYPE_BUNDLE) return "bundle" else if (type == TYPE_BOOT_DELEGATION) return "boot-delegation"
        return "system"
    }

    private fun _getCallerClass(context: Array<Class<*>>, caller: Class<*>, index: RefInteger, acceptBootDelegation: Boolean, acceptSystem: Boolean): Class<*>? {
        var callerCaller: Class<*>?
        do {
            callerCaller = context[index.toInt()]
            index.plus(1)
            if (callerCaller === caller || _isSystem(callerCaller)) {
                callerCaller = null
            }
            if (callerCaller != null && !acceptSystem && !isFromBundle(callerCaller)) {
                callerCaller = null
            } else if (callerCaller != null && !acceptBootDelegation && OSGiUtil.isClassInBootelegation(callerCaller.getName())) {
                callerCaller = null
            }
        } while (callerCaller == null && index.toInt() < context.size)
        return callerCaller
    }

    private fun isFromBundle(clazz: Class<*>?): Boolean {
        if (clazz == null) return false
        if (clazz.getClassLoader() !is BundleReference) return false
        val br: BundleReference = clazz.getClassLoader() as BundleReference
        return !OSGiUtil.isFrameworkBundle(br.getBundle())
    }

    private fun _isSystem(clazz: Class<*>?): Boolean {
        if (clazz.getName() === "java.lang.Class") return true // Class.forName(className)
        if (clazz.getName().startsWith("com.sun.beans.finder.")) return true
        if (clazz.getName().startsWith("java.beans.")) return true
        return if (clazz.getName().startsWith("java.util.ServiceLoader")) true else false
    }

    private val logs: Map<String, Integer> = ConcurrentHashMap<String, Integer>()

    // Java == 9
    // Java < 9
    var isBooted: Boolean? = null
        get() {
            if (Boolean.TRUE.equals(field)) return true
            var clazz: Class = ClassUtil.loadClass("jdk.internal.misc.VM", null) // Java == 9
            if (clazz == null) clazz = ClassUtil.loadClass("sun.misc.VM", null) // Java < 9
            if (clazz != null) {
                try {
                    val m: Method = clazz.getMethod("isBooted", EMPTY_CLASS)
                    field = Caster.toBoolean(m.invoke(null, EMPTY_OBJ))
                    return field.booleanValue()
                } catch (e: Exception) {
                }
            }
            return true
        }
        private set

    fun logUsage() {
        val st: String = ExceptionUtil.getStacktrace(Throwable(), false)
        var res: Integer? = logs[st]
        res = if (res == null) 1 else res.intValue() + 1
        logs.put(st, res)
    }

    val logUsage: Map<String, Any>
        get() = logs

    /**
     * checks if both paths are the same ignoring CaSe, file separator type, and whether one path ends
     * with a separator while the other does not. if either path is empty then false is returned.
     *
     * @param path1
     * @param path2
     * @return true if neither path is empty and the paths are the same ignoring case, separator, and
     * whether either path ends with a separator.
     */
    fun arePathsSame(path1: String, path2: String): Boolean {
        if (StringUtil.isEmpty(path1, true) || StringUtil.isEmpty(path2, true)) return false
        var p1: String = path1.replace('\\', '/')
        var p2: String = path2.replace('\\', '/')
        if (p1.endsWith("/") && !p2.endsWith("/")) p2 = "$p2/" else if (p2.endsWith("/") && !p1.endsWith("/")) p1 = "$p1/"
        return p1.equalsIgnoreCase(p2)
    }

    fun millis(): Double {
        return System.nanoTime() / 1000000.0
    }

    fun isInLucee(thread: Thread?): Boolean {
        val stes: Array<StackTraceElement> = thread.getStackTrace()
        for (ste in stes) {
            if (ste.getClassName().indexOf("lucee.") === 0) return true
        }
        return false
    }

    fun isInNativeCode(thread: Thread): Boolean {
        val stes: Array<StackTraceElement> = thread.getStackTrace()
        return if (stes != null && stes.size > 0) stes[0].isNativeMethod() else false
    }

    fun getJavaObjectInputStreamAccessCheckArray(s: ObjectInputStream, class1: Class<Array<Entry?>?>, cap: Int): Boolean {
        // jdk.internal.misc.SharedSecrets.getJavaObjectInputStreamAccess().checkArray(s, Map.Entry[].class,
        // cap);
        var clazz: Class = ClassUtil.loadClass("jdk.internal.misc.SharedSecrets", null) // Java == 9
        if (clazz == null) clazz = ClassUtil.loadClass("sun.misc.SharedSecrets", null) // Java < 9

        // get object
        var joisa: Object? = null
        if (clazz != null) {
            try {
                val m: Method = clazz.getMethod("getJavaObjectInputStreamAccess", EMPTY_CLASS)
                joisa = m.invoke(null, EMPTY_OBJ)
            } catch (e: Exception) {
            }
        }
        if (joisa != null) {
            clazz = joisa.getClass()
            try {
                val m: Method = clazz.getMethod("checkArray", arrayOf<Class>(ObjectInputStream::class.java, Class::class.java, Int::class.javaPrimitiveType))
                m.invoke(joisa, arrayOf(s, class1, cap))
                return true
            } catch (e: Exception) {
            }
        }
        return false
    }

    fun createToken(prefix: String, name: String): String? {
        val str = "$prefix:$name"
        var lock: String = tokens.putIfAbsent(str, str)
        if (lock == null) {
            lock = str
        }
        return lock
    }

    class TemplateLine : Serializable {
        val template: String
        val line: Int

        constructor(template: String, line: Int) {
            this.template = template
            this.line = line
        }

        constructor(templateAndLine: String) {
            val index: Int = templateAndLine.lastIndexOf(':')
            template = if (index == -1) templateAndLine else templateAndLine.substring(0, index)
            line = if (index == -1) 0 else Caster.toIntValue(templateAndLine.substring(index + 1), 0)
        }

        @Override
        override fun toString(): String {
            return if (line < 1) template else "$template:$line"
        }

        fun toString(sb: StringBuilder): StringBuilder {
            if (line < 1) sb.append(template) else sb.append(template).append(':').append(line)
            return sb
        }

        fun toString(pc: PageContext?, contract: Boolean): String {
            return if (line < 1) if (contract) ContractPath.call(pc, template) else template else (if (contract) ContractPath.call(pc, template) else template).toString() + ":" + line
        }

        fun toStruct(): Object {
            val caller: Struct = StructImpl(Struct.TYPE_LINKED)
            caller.setEL(KeyConstants._template, template)
            caller.setEL(KeyConstants._line, Double.valueOf(line))
            return caller
        }

        companion object {
            private const val serialVersionUID = 6610978291828389799L
        }
    }

    class Caller {
        var fromBootDelegation: Class<*>? = null
        var fromSystem: Class<*>? = null
        var fromBundle: Class<*>? = null

        @Override
        override fun toString(): String {
            return "fromBootDelegation:$fromBootDelegation;fromSystem:$fromSystem;fromBundle:$fromBundle"
        }

        val isEmpty: Boolean
            get() = fromBootDelegation == null && fromBundle == null && fromSystem == null

        fun fromClasspath(): Class<*>? {
            if (fromSystem != null) {
                if (fromSystem.getClassLoader() != null) return fromSystem
                return if (fromBootDelegation != null && fromBootDelegation.getClassLoader() != null) fromBootDelegation else fromSystem
            }
            return fromBootDelegation
        }
    }

    init {
        // OS
        val os: String = System.getProperty("os.name").toLowerCase()
        isWindows = lucee.commons.io.os.startsWith("windows")
        isSolaris = lucee.commons.io.os.startsWith("solaris")
        isLinux = lucee.commons.io.os.startsWith("linux")
        isMacOSX = lucee.commons.io.os.startsWith("mac os x")
        isUnix = !isWindows && File.separatorChar === '/' // deprecated
        val strCharset: String = System.getProperty("file.encoding")
        if (lucee.commons.io.strCharset == null || lucee.commons.io.strCharset.equalsIgnoreCase("MacRoman")) lucee.commons.io.strCharset = "cp1252"
        if (lucee.commons.io.strCharset.equalsIgnoreCase("utf-8")) charset = CharSet.UTF8 else if (lucee.commons.io.strCharset.equalsIgnoreCase("iso-8859-1")) charset = CharSet.ISO88591 else charset = CharsetUtil.toCharSet(lucee.commons.io.strCharset, null)

        // Perm Gen
        permGenSpaceBean = getPermGenSpaceBean()
        // make sure the JVM does not always a new bean
        val tmp: MemoryPoolMXBean? = getPermGenSpaceBean()
        if (lucee.commons.io.tmp !== lucee.commons.io.SystemUtil.permGenSpaceBean) permGenSpaceBean = null
        if (JAVA_VERSION_STRING.startsWith("1.14.") || JAVA_VERSION_STRING.startsWith("14")) JAVA_VERSION = JAVA_VERSION_14 else if (JAVA_VERSION_STRING.startsWith("1.13.") || JAVA_VERSION_STRING.startsWith("13")) JAVA_VERSION = JAVA_VERSION_13 else if (JAVA_VERSION_STRING.startsWith("1.12.") || JAVA_VERSION_STRING.startsWith("12")) JAVA_VERSION = JAVA_VERSION_12 else if (JAVA_VERSION_STRING.startsWith("1.11.") || JAVA_VERSION_STRING.startsWith("11")) JAVA_VERSION = JAVA_VERSION_11 else if (JAVA_VERSION_STRING.startsWith("1.10.") || JAVA_VERSION_STRING.startsWith("10")) JAVA_VERSION = JAVA_VERSION_10 else if (JAVA_VERSION_STRING.startsWith("1.9.") || JAVA_VERSION_STRING.startsWith("9.")) JAVA_VERSION = JAVA_VERSION_9 else if (JAVA_VERSION_STRING.startsWith("1.8.")) JAVA_VERSION = JAVA_VERSION_8 else if (JAVA_VERSION_STRING.startsWith("1.7.")) JAVA_VERSION = JAVA_VERSION_7 else if (JAVA_VERSION_STRING.startsWith("1.6.")) JAVA_VERSION = JAVA_VERSION_6 else JAVA_VERSION = 0
    }
}

internal class Ref {
    var context: Array<Class<*>>
}

internal class StopThread(pc: PageContext) : Thread() {
    private val pc: PageContext
    @Override
    fun run() {
        stop(pc, pc.getThread())
    }

    // private final Log log;
    init {
        this.pc = pc
    }
}

class MacAddressWrap : ObjectWrap, Castable, Serializable {
    @get:Throws(PageException::class)
    @get:Override
    val embededObject: Object?
        get() = try {
            SystemUtil.macAddress
        } catch (e: Exception) {
            throw Caster.toPageException(e)
        }

    @Override
    fun getEmbededObject(defaultValue: Object?): Object? {
        return SystemUtil.getMacAddress(null) ?: return defaultValue
    }

    @Override
    @Throws(PageException::class)
    fun castToBooleanValue(): Boolean {
        return Caster.toBooleanValue(toString())
    }

    @Override
    fun castToBoolean(defaultValue: Boolean): Boolean {
        val obj: Object = getEmbededObject(null) ?: return defaultValue
        return Caster.toBoolean(obj.toString(), defaultValue)
    }

    @Override
    @Throws(PageException::class)
    fun castToDateTime(): DateTime {
        return Caster.toDatetime(toString(), null)
    }

    @Override
    fun castToDateTime(defaultValue: DateTime): DateTime {
        val obj: Object = getEmbededObject(null) ?: return defaultValue
        return DateCaster.toDateAdvanced(obj.toString(), DateCaster.CONVERTING_TYPE_OFFSET, null, defaultValue)
    }

    @Override
    @Throws(PageException::class)
    fun castToDoubleValue(): Double {
        return Caster.toDoubleValue(toString())
    }

    @Override
    fun castToDoubleValue(defaultValue: Double): Double {
        val obj: Object = getEmbededObject(null) ?: return defaultValue
        return Caster.toDoubleValue(obj.toString(), defaultValue)
    }

    @Override
    @Throws(PageException::class)
    fun castToString(): String {
        return toString()
    }

    @Override
    override fun toString(): String {
        return try {
            val eo: Object = embededObject ?: return ""
            eo.toString()
        } catch (pe: PageException) {
            throw PageRuntimeException(pe)
        }
    }

    @Override
    fun castToString(defaultValue: String?): String? {
        return getEmbededObject(defaultValue)
    }

    @Override
    @Throws(PageException::class)
    operator fun compareTo(str: String?): Int {
        return OpUtil.compare(ThreadLocalPageContext.get(), toString(), str)
    }

    @Override
    @Throws(PageException::class)
    operator fun compareTo(b: Boolean): Int {
        return OpUtil.compare(ThreadLocalPageContext.get(), if (castToBooleanValue()) Boolean.TRUE else Boolean.FALSE, if (b) Boolean.TRUE else Boolean.FALSE)
    }

    @Override
    @Throws(PageException::class)
    operator fun compareTo(d: Double): Int {
        return OpUtil.compare(ThreadLocalPageContext.get(), Double.valueOf(castToDoubleValue()), Double.valueOf(d))
    }

    @Override
    @Throws(PageException::class)
    operator fun compareTo(dt: DateTime): Int {
        return OpUtil.compare(ThreadLocalPageContext.get(), toString(), dt.castToString())
    }

    companion object {
        private const val serialVersionUID = 8707984359031327783L
        @Throws(URISyntaxException::class, ZipException::class, IOException::class)
        fun size(clazz: Class): Long {
            val pd: ProtectionDomain = clazz.getProtectionDomain() ?: return 0L
            val sc: CodeSource = pd.getCodeSource() ?: return 0L
            val url: URL = sc.getLocation() ?: return 0L
            if (url.getProtocol().equalsIgnoreCase("file")) {
                val uri: URI = url.toURI()
                val file = File(uri)

                // lose file
                if (file.isDirectory()) {
                    val relPath: String = clazz.getName().replace('.', File.separatorChar).toString() + ".class"
                    val f = File(file, relPath)
                    return f.length()
                } else if (file.isFile()) {
                    val relPath: String = clazz.getName().replace('.', '/').toString() + ".class"
                    var size: Long = 0
                    var zf: ZipFile? = null
                    try {
                        zf = ZipFile(file)
                        var name: String
                        var entry: ZipEntry
                        val en: Enumeration<out ZipEntry?> = zf.entries()
                        while (en.hasMoreElements()) {
                            entry = en.nextElement()
                            if (!entry.isDirectory()) {
                                name = entry.getName().replace('\\', '/')
                                if (name.startsWith("/")) name = name.substring(1) // some zip path start with "/" some not
                                if (relPath.equals(name)) {
                                    size = entry.getSize()
                                    break
                                }
                            }
                        }
                    } finally {
                        zf.close()
                    }
                    return size
                }
            } else {
                var size: Long = 0
                val relPath: String = clazz.getName().replace('.', '/').toString() + ".class"
                val zis = ZipInputStream(url.openStream())
                var name: String
                var entry: ZipEntry
                while (zis.getNextEntry().also { entry = it } != null) {
                    if (!entry.isDirectory()) {
                        name = entry.getName().replace('\\', '/')
                        if (name.startsWith("/")) name = name.substring(1) // some zip path start with "/" some not
                        if (relPath.equals(name)) {
                            size = entry.getSize()
                            break
                        }
                    }
                    zis.closeEntry()
                }
                return size
            }
            return 0L
        }
    }
}