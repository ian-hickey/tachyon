package lucee.commons.lang.compiler

import java.io.IOException

class CustomClassloaderJavaFileManager(context: BundleContext, classLoader: ClassLoader, standardFileManager: JavaFileManager) : ForwardingJavaFileManager<JavaFileManager?>(standardFileManager), JavaFileManager, BundleListener {
    private val classLoader: ClassLoader
    private val standardFileManager: JavaFileManager
    private val folderMap: Map<String, CustomJavaFileFolder> = HashMap<String, CustomJavaFileFolder>()
    private val fileMap: Map<String, CustomJavaFileObject> = HashMap<String, CustomJavaFileObject>()
    private val bundleContext: BundleContext
    private val log: Log
    @Override
    fun getClassLoader(location: Location?): ClassLoader {
        return classLoader
    }

    @Override
    fun inferBinaryName(location: Location?, file: JavaFileObject): String {
        return if (file is CustomJavaFileObject) {
            var binaryName: String = (file as CustomJavaFileObject).binaryName()
            if (binaryName.indexOf('/') >= 0) {
                binaryName = binaryName.substring(binaryName.lastIndexOf("/") + 1)
            }
            if (binaryName.indexOf('.') >= 0) {
                binaryName = binaryName.substring(0, binaryName.indexOf('.'))
            }
            binaryName
        } else {
            standardFileManager.inferBinaryName(location, file)
        }
    }

    @Override
    fun hasLocation(location: Location?): Boolean {
        return true
    }

    @Override
    @Throws(IOException::class)
    fun getJavaFileForInput(location: Location?, className: String, kind: JavaFileObject.Kind): JavaFileObject? {
        var binaryName: String = className.replaceAll("\\.", "/")
        binaryName = if (kind.equals(Kind.CLASS)) {
            "$binaryName.class"
        } else {
            "$binaryName.java"
        }
        val cjfo: CustomJavaFileObject? = fileMap[binaryName]
        if (cjfo == null) {
        }
        return cjfo
    }

    @Override
    @Throws(IOException::class)
    fun getJavaFileForOutput(location: Location?, className: String, kind: JavaFileObject.Kind, sibling: FileObject?): JavaFileObject {
        val binaryName: String = className.replaceAll("\\.", "/") + kind.extension
        val uri: URI = URI.create("file:///$binaryName")
        var cjfo: CustomJavaFileObject? = fileMap[binaryName] // new
        // CustomJavaFileObject(binaryName,
        // uri,fileMap.get(uri.toString()));
        if (cjfo == null) {
            cjfo = CustomJavaFileObject(binaryName, uri, null as InputStream?, kind)
            fileMap.put(binaryName, cjfo)
        }
        return cjfo
    }

    @Override
    @Throws(IOException::class)
    fun getFileForInput(location: Location, packageName: String?, relativeName: String?): FileObject {
        val jf: JavaFileObject? = fileMap[location.getName()]
        return if (jf != null) {
            jf
        } else super.getFileForInput(location, packageName, relativeName)
    }

    @Override
    @Throws(IOException::class)
    fun list(location: Location, packageName: String, kinds: Set<JavaFileObject.Kind?>, recurse: Boolean): Iterable<JavaFileObject> {
        if (location === StandardLocation.PLATFORM_CLASS_PATH) {
            return standardFileManager.list(location, packageName, kinds, recurse)
        } else if (location === StandardLocation.CLASS_PATH && kinds.contains(JavaFileObject.Kind.CLASS)) {
            try {
                var folder: CustomJavaFileFolder? = folderMap[packageName]
                if (folder == null) {
                    folder = CustomJavaFileFolder(bundleContext, packageName)
                    folderMap.put(packageName, folder)
                }
                return folder.getEntries()
            } catch (e: URISyntaxException) {
                log.error("compiler", "Illegal URI while listing entries for package: $packageName", e)
            }
            // }
        }
        return Collections.emptyList()
    }

    @Override
    fun isSupportedOption(option: String?): Int {
        return -1
    }

    @Override
    fun bundleChanged(be: BundleEvent) {
        val bundle: Bundle = be.getBundle()
        when (be.getType()) {
            BundleEvent.UNRESOLVED, BundleEvent.RESOLVED -> {
                val bw: BundleWiring = bundle.adapt(BundleWiring::class.java)
                val pkgs = getAffectedPackages(bw)
                flush(pkgs)
            }
        }
    }

    private fun flush(pkgs: Iterable<String>) {
        for (pkg in pkgs) {
            if (folderMap.containsKey(pkg)) {
                log.info("compiler", "Flushed package: $pkg")
            }
            folderMap.remove(pkg)
        }
    }

    private fun getAffectedPackages(bw: BundleWiring?): Iterable<String> {
        val result: List<String> = ArrayList<String>()
        if (bw == null) {
            return result
        }
        val l: List<BundleCapability> = bw.getCapabilities("osgi.wiring.package")
        for (bundleCapability in l) {
            val pkg = bundleCapability.getAttributes().get("osgi.wiring.package") as String
            log.debug("compiler", "Affected package: $pkg")
            result.add(pkg)
        }
        return result
    }

    @Override
    @Throws(IOException::class)
    fun close() {
        super.close()
        bundleContext.removeBundleListener(this)
        fileMap.clear()
    }

    init {
        this.classLoader = classLoader
        this.standardFileManager = standardFileManager
        bundleContext = context
        bundleContext.addBundleListener(this)
        log = ThreadLocalPageContext.getLog("application")
    }
}