package tachyon.runtime.osgi

import java.io.ByteArrayInputStream

class EnvClassLoader(config: ConfigPro?) : URLClassLoader(arrayOfNulls<URL?>(0), if (config != null) config.getClassLoaderCore() else ClassLoaderHelper().getClass().getClassLoader()) {
    private val config: Config?
    private val callerCache: Map<String?, SoftReference<Array<Object?>?>?>? = ConcurrentHashMap<String?, SoftReference<Array<Object?>?>?>()
    private val trace: Log?
    @Override
    @Throws(ClassNotFoundException::class)
    fun loadClass(name: String?): Class<*>? {
        return loadClass(name, false)
    }

    @Override
    fun getResource(name: String?): URL? {
        if (trace == null) {
            return load(name, URL, true, null, true) as java.net.URL?
        }
        val start: Double = SystemUtil.millis()
        return try {
            load(name, URL, true, null, true) as java.net.URL?
        } finally {
            trace.trace("EnvClassLoader", "EnvClassLoader.getResource(" + name + "):" + (SystemUtil.millis() - start))
        }
    }

    @Override
    fun getResourceAsStream(name: String?): InputStream? {
        if (trace == null) {
            return load(name, STREAM, true, null, true) as InputStream?
        }
        val start: Double = SystemUtil.millis()
        return try {
            load(name, STREAM, true, null, true) as InputStream?
        } finally {
            trace.trace("EnvClassLoader", "EnvClassLoader.getResourceAsStream(" + name + "):" + (SystemUtil.millis() - start))
        }
    }

    @Override
    @Throws(IOException::class)
    fun getResources(name: String?): Enumeration<URL?>? {
        if (trace == null) {
            val list: List<URL?> = ArrayList<URL?>()
            val url: URL? = load(name, URL, false, null, true) as URL?
            if (url != null) list.add(url)
            return E<URL?>(list.iterator())
        }
        val start: Double = SystemUtil.millis()
        return try {
            val list: List<URL?> = ArrayList<URL?>()
            val url: URL? = load(name, URL, false, null, true) as URL?
            if (url != null) list.add(url)
            E<URL?>(list.iterator())
        } finally {
            trace.trace("EnvClassLoader", "EnvClassLoader.getResources(" + name + "):" + (SystemUtil.millis() - start))
        }
    }

    @Override
    @Synchronized
    @Throws(ClassNotFoundException::class)
    protected fun loadClass(name: String?, resolve: Boolean): Class<*>? {
        if (trace == null) {
            var c: Class<*>? = findLoadedClass(name)
            if (c == null) c = load(name, CLASS, true, null, true) as Class<*>?
            if (c == null) c = findClass(name)
            if (resolve) resolveClass(c)
            return c
        }
        val start: Double = SystemUtil.millis()
        return try {
            var c: Class<*>? = findLoadedClass(name)
            if (c == null) c = load(name, CLASS, true, null, true) as Class<*>?
            if (c == null) c = findClass(name)
            if (resolve) resolveClass(c)
            c
        } finally {
            trace.trace("EnvClassLoader", "EnvClassLoader.loadClass(" + name + "):" + (SystemUtil.millis() - start))
        }
    }

    @Synchronized
    private fun load(name: String?, type: Short, doLog: Boolean, listContext: List<ClassLoader?>?, useCache: Boolean): Object? {
        var listContext: List<ClassLoader?>? = listContext
        val start: Double = SystemUtil.millis()
        val id: StringBuilder = StringBuilder(name).append(';').append(type).append(';')
        val _id: String = id.toString()
        val cache: Set<String?> = checking.get()
        if (useCache && cache.contains(_id)) {
            callerCache.put(id.toString(), SoftReference<Array<Object?>?>(arrayOf(null)))
            return null
        }
        return try {
            cache.add(_id)
            if (listContext == null) {
                listContext = SystemUtil.getClassLoaderContext(true, id)
            }

            // PATCH XML
            if ((name.toString() + "").startsWith("META-INF/services") && !inside.get()) {
                inside.set(Boolean.TRUE)
                try {
                    if (name.equalsIgnoreCase("META-INF/services/javax.xml.parsers.DocumentBuilderFactory")) {
                        if (patchNeeded(name, doLog, listContext)) {
                            if (type == URL) return XMLUtil.getDocumentBuilderFactoryResource() else if (type == STREAM) return ByteArrayInputStream(XMLUtil.getDocumentBuilderFactoryName().getBytes())
                        }
                    } else if (name.equalsIgnoreCase("META-INF/services/javax.xml.parsers.SAXParserFactory")) {
                        if (patchNeeded(name, doLog, listContext)) {
                            if (type == URL) return XMLUtil.getSAXParserFactoryResource() else if (type == STREAM) return ByteArrayInputStream(XMLUtil.getSAXParserFactoryName().getBytes())
                        }
                    } else if (name.equalsIgnoreCase("META-INF/services/javax.xml.transform.TransformerFactory")) {
                        if (patchNeeded(name, doLog, listContext)) {
                            if (type == URL) return XMLUtil.getTransformerFactoryResource() else if (type == STREAM) return ByteArrayInputStream(XMLUtil.getTransformerFactoryName().getBytes())
                        }
                    } else if (name.equalsIgnoreCase("META-INF/services/org.apache.xerces.xni.parser.XMLParserConfiguration")) {
                        if (patchNeeded(name, doLog, listContext)) {
                            if (type == STREAM) return ByteArrayInputStream(XMLUtil.getXMLParserConfigurationName().getBytes())
                        }
                    }
                } catch (e: IOException) {
                } finally {
                    inside.set(Boolean.FALSE)
                }
            }

            // PATCH for com.sun
            if ((name.toString() + "").startsWith("com.sun.")) {
                val obj: Object?
                val loader: ClassLoader = CFMLEngineFactory::class.java.getClassLoader()
                obj = _load(loader, name, type)
                if (obj != null) {
                    if (trace != null) trace.trace("EnvClassLoader", "found [$name] in loader ClassLoader")
                    if (useCache) callerCache.put(id.toString(), SoftReference<Array<Object?>?>(arrayOf<Object?>(obj)))
                    return obj
                }
            }
            val sr: SoftReference<Array<Object?>?>? = callerCache!![id.toString()]
            if (sr != null && sr.get() != null) {
                // print.e(name + " - from cache " + callerCache.size());
                return sr.get().get(0)
            }
            // callers classloader context
            var obj: Object?
            for (cl in listContext) {
                obj = _load(cl, name, type)
                if (obj != null) {
                    if (cl is BundleReference) {
                        if (trace != null) trace.trace("EnvClassLoader", "found [" + name + "] in bundle [" + (cl as BundleReference?).getBundle().getSymbolicName() + ":"
                                + (cl as BundleReference?).getBundle().getVersion() + "]")
                    } else {
                        if (trace != null) trace.trace("EnvClassLoader", "found [$name] in System ClassLoader $cl")
                    }
                    if (useCache) callerCache.put(id.toString(), SoftReference<Array<Object?>?>(arrayOf<Object?>(obj)))
                    return obj
                } else {
                    if (cl is BundleReference) {
                        if (trace != null) trace.trace("EnvClassLoader", "not found [" + name + "] in bundle [" + (cl as BundleReference?).getBundle().getSymbolicName() + ":"
                                + (cl as BundleReference?).getBundle().getVersion() + "]")
                    } else {
                        if (trace != null) trace.trace("EnvClassLoader", "not found [$name] in System ClassLoader $cl")
                    }
                }
            }
            // print.ds("4:" + (SystemUtil.millis() - start) + ":" + name);
            if (trace != null) trace.trace("EnvClassLoader", "not found [$name] ")
            if (useCache) callerCache.put(id.toString(), SoftReference<Array<Object?>?>(arrayOf(null)))
            null
        } finally {
            cache.remove(_id)
        }
    }

    @Throws(IOException::class)
    private fun patchNeeded(name: String?, doLog: Boolean, listContext: List<ClassLoader?>?): Boolean {
        var o: Object? = load(name, STREAM, doLog, listContext, false)
        var patchIt = true
        if (o is InputStream) {
            val className: String = IOUtil.toString(o as InputStream?, null as Charset?)
            o = if (StringUtil.isEmpty(className)) null else load(className.trim(), CLASS, doLog, listContext, false)
            if (o != null) patchIt = false
        }
        return patchIt
    }

    private fun _load(cl: ClassLoader?, name: String?, type: Short): Object? {
        var obj: Object? = null
        var b: Bundle? = null
        if (cl != null) {
            try {
                if (type == CLASS) {
                    if (cl is BundleReference) {
                        b = (cl as BundleReference?).getBundle()
                        obj = if (notFound!!.containsKey(
                                        SoftReference<String?>(StringBuilder(b.getSymbolicName()).append(':').append(b.getVersion()).append(':').append(name).toString()))) return null else cl.loadClass(name)
                    } else obj = cl.loadClass(name)
                } else if (type == URL) obj = cl.getResource(name) else obj = cl.getResourceAsStream(name)
            } catch (cnfe: ClassNotFoundException) {
                if (b != null) notFound.put(SoftReference<String?>(StringBuilder(b.getSymbolicName()).append(':').append(b.getVersion()).append(':').append(name).toString()), EMPTY)
            } catch (e: Exception) {
            }
        }
        return obj
    }

    private fun toType(type: Short): String? {
        if (CLASS == type) return "class"
        return if (STREAM == type) "stream" else "url"
    }

    @Override
    @Throws(ClassNotFoundException::class)
    protected fun findClass(name: String?): Class<*>? {
        throw ClassNotFoundException("class $name not found in the core, the loader and all the extension bundles")
    }

    private class E<T>(private val it: Iterator<T?>?) : Enumeration<T?> {
        @Override
        fun hasMoreElements(): Boolean {
            return it!!.hasNext()
        }

        @Override
        fun nextElement(): T? {
            return it!!.next()
        }
    }

    //////////////////////////////////////////////////
    // URLClassloader methods, need to be supressed //
    //////////////////////////////////////////////////
    @Override
    fun findResource(name: String?): URL? {
        return getResource(name)
    }

    @Override
    @Throws(IOException::class)
    fun findResources(name: String?): Enumeration<URL?>? {
        return getResources(name)
    }

    private fun log(logLevel: Int): Log? {
        if (config == null) return null
        val log: Log = ThreadLocalPageContext.getLog(config, "application")
        return if (log == null || log.getLogLevel() > logLevel) null else log
    }

    companion object {
        private const val FROM_SYSTEM = 1f
        private const val FROM_BOOTDELEGATION = 2f
        private const val FROM_CALLER = 3f
        private val EMPTY: SoftReference<String?>? = SoftReference<String?>(null)
        private val notFound: Map<SoftReference<String?>?, SoftReference<String?>?>? = ConcurrentHashMap()
        private const val CLASS: Short = 1
        private const val URL: Short = 2
        private const val STREAM: Short = 3
        private val checking: ThreadLocal<Set<String?>?>? = object : ThreadLocal<Set<String?>?>() {
            @Override
            protected fun initialValue(): Set<String?>? {
                return HashSet()
            }
        }
        private val inside: ThreadLocal<Boolean?>? = object : ThreadLocal<Boolean?>() {
            @Override
            protected fun initialValue(): Boolean? {
                return Boolean.FALSE
            }
        }
        private val NULL_INSTANCE: EnvClassLoader? = EnvClassLoader(null)
        fun getInstance(config: ConfigPro?): EnvClassLoader? {
            var config: ConfigPro? = config
            config = ThreadLocalPageContext.getConfig(config) as ConfigPro
            return if (config != null) config.getClassLoaderEnv() else NULL_INSTANCE
        }
    }

    init {
        this.config = config
        trace = log(Log.LEVEL_TRACE)
    }
}