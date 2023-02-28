package tachyon.commons.io.log.log4j2

import java.io.IOException

class Log4j2Engine(config: Config) : LogEngine() {
    private val config: Config

    @get:Override
    var version: String? = null
        get() {
            if (field == null) {
                val cl: ClassLoader = LogManager::class.java.getClassLoader()
                field = if (cl is BundleClassLoader) {
                    val bcl: BundleClassLoader = cl as BundleClassLoader
                    val b: Bundle = bcl.getBundle()
                    b.getVersion().toString()
                } else "2"
            }
            return field
        }
        private set

    @Override
    fun getConsoleLog(errorStream: Boolean, name: String, level: Int): Log {
        var pw: PrintWriter? = if (errorStream) config.getErrWriter() else config.getOutWriter()
        if (pw == null) pw = PrintWriter(if (errorStream) System.err else System.out)
        return _getLogger(config,
                getConsoleAppender(createFullName(ThreadLocalPageContext.getConfig(), name), pw, PatternLayout.newBuilder().withPattern(DEFAULT_PATTERN).build(), true), name,
                level)
    }

    @Override
    @Throws(PageException::class)
    fun getResourceLog(res: Resource?, charset: Charset?, name: String, level: Int, timeout: Int, listener: RetireListener?, async: Boolean): Log {
        var a: Appender = toResourceAppender(createFullName(ThreadLocalPageContext.getConfig(), name), res, ClassicLayout(), charset, DEFAULT_MAX_BACKUP_INDEX,
                DEFAULT_MAX_FILE_SIZE, timeout, true)
        if (async) {
            a = TaskAppender(config, a)
        }
        return _getLogger(config, a, name, level)
    }

    @Override
    fun appenderClassDefintion(className: String?): ClassDefinition {
        // we define the old classes for all existing log entries
        if ("console".equalsIgnoreCase(className) || "tachyon.commons.io.log.log4j.appender.ConsoleAppender".equals(className)
                || "tachyon.commons.io.log.log4j2.appender.ConsoleAppender".equals(className)) {
            return ClassDefinitionImpl(ConsoleAppender::class.java)
        }
        if ("resource".equalsIgnoreCase(className) || "tachyon.commons.io.log.log4j.appender.RollingResourceAppender".equals(className)
                || "tachyon.commons.io.log.log4j2.appender.ResourceAppender".equals(className)) {
            return ClassDefinitionImpl(ResourceAppender::class.java)
        }
        return if ("datasource".equalsIgnoreCase(className) || "tachyon.commons.io.log.log4j.appender.DatasourceAppender".equals(className)
                || "tachyon.commons.io.log.log4j2.appender.DatasourceAppender".equals(className)) {
            ClassDefinitionImpl(DatasourceAppender::class.java)
        } else ClassDefinitionImpl(className)
    }

    /*
	 * public ClassDefinition toClassDefinitionAppender(Struct sct, ClassDefinition defaultValue) { if
	 * (sct == null) return defaultValue;
	 * 
	 * // class String className = Caster.toString(sct.get("class", null), null); if
	 * (StringUtil.isEmpty(className)) return defaultValue;
	 * 
	 * if ("console".equalsIgnoreCase(className)) return new ClassDefinitionImpl(ConsoleAppender.class);
	 * if ("resource".equalsIgnoreCase(className)) return new
	 * ClassDefinitionImpl(RollingResourceAppender.class); if ("datasource".equalsIgnoreCase(className))
	 * return new ClassDefinitionImpl(DatasourceAppender.class);
	 * 
	 * // name String name = bundleName(sct); Version version = bundleVersion(sct);
	 * 
	 * if (StringUtil.isEmpty(name)) return new ClassDefinitionImpl(className);
	 * 
	 * return new ClassDefinitionImpl(null, className, name, version); }
	 */
    @Override
    fun layoutClassDefintion(className: String): ClassDefinition<*> {
        if ("classic".equalsIgnoreCase(className) || "tachyon.commons.io.log.log4j.layout.ClassicLayout".equals(className)
                || "tachyon.commons.io.log.log4j2.layout.ClassicLayout".equals(className)) {
            return ClassDefinitionImpl(ClassicLayout::class.java)
        }
        if ("datasource".equalsIgnoreCase(className) || "tachyon.commons.io.log.log4j.layout.DatasourceLayout".equals(className)) return ClassDefinitionImpl(ClassicLayout::class.java)
        if ("html".equalsIgnoreCase(className) || "org.apache.log4j.HTMLLayout".equals(className) || "org.apache.logging.log4j.core.layout.HtmlLayout".equals(className)) {
            return ClassDefinitionImpl(HtmlLayout::class.java)
        }
        if ("xml".equalsIgnoreCase(className) || "org.apache.log4j.xml.XMLLayout".equalsIgnoreCase(className)
                || "org.apache.logging.log4j.core.layout.XmlLayout".equalsIgnoreCase(className) || "tachyon.commons.io.log.log4j2.layout.XMLLayout".equals(className)) {
            return ClassDefinitionImpl(XMLLayout::class.java)
        }
        if ("pattern".equalsIgnoreCase(className) || "org.apache.log4j.PatternLayout".equals(className) || "org.apache.logging.log4j.core.layout.PatternLayout".equals(className)) {
            return ClassDefinitionImpl(PatternLayout::class.java)
        }
        return if ("datadog".equalsIgnoreCase(className) || className.indexOf(".DataDogLayout") !== -1) {
            ClassDefinitionImpl(DataDogLayout::class.java)
        } else ClassDefinitionImpl(className)
    }

    /*
	 * public ClassDefinition toClassDefinitionLayout(Struct sct, ClassDefinition defaultValue) { if
	 * (sct == null) return defaultValue;
	 * 
	 * // class String className = Caster.toString(sct.get("class", null), null); if
	 * (StringUtil.isEmpty(className)) return defaultValue;
	 * 
	 * if ("classic".equalsIgnoreCase(className)) return new ClassDefinitionImpl(ClassicLayout.class);
	 * if ("datasource".equalsIgnoreCase(className)) return new
	 * ClassDefinitionImpl(DatasourceLayout.class); if ("html".equalsIgnoreCase(className)) return new
	 * ClassDefinitionImpl(HTMLLayout.class); if ("xml".equalsIgnoreCase(className)) return new
	 * ClassDefinitionImpl(XMLLayout.class); if ("pattern".equalsIgnoreCase(className)) return new
	 * ClassDefinitionImpl(PatternLayout.class);
	 * 
	 * String name = bundleName(sct); Version version = bundleVersion(sct);
	 * 
	 * if (StringUtil.isEmpty(name)) return new ClassDefinitionImpl(className);
	 * 
	 * return new ClassDefinitionImpl(null, className, name, version); }
	 */
    @Override
    @Throws(PageException::class)
    fun getLayout(cd: ClassDefinition?, layoutArgs: Map<String?, String?>?, cdAppender: ClassDefinition?, name: String?): Object {
        var layoutArgs = layoutArgs
        if (layoutArgs == null) layoutArgs = HashMap<String, String>()

        // Layout
        var layout: Layout? = null
        if (cd != null && cd.hasClass()) {
            // Classic Layout
            if (ClassicLayout::class.java.getName().equalsIgnoreCase(cd.getClassName())) {
                layout = ClassicLayout()
            } else if (HtmlLayout::class.java.getName().equalsIgnoreCase(cd.getClassName())) {
                val builder: Builder = HtmlLayout.newBuilder()

                // Location Info
                var locInfo: Boolean? = Caster.toBoolean(layoutArgs!!["locationinfo"], null)
                if (locInfo != null) builder.withLocationInfo(locInfo.booleanValue()) else locInfo = Boolean.FALSE
                layoutArgs.put("locationinfo", locInfo.toString())

                // Title
                val title: String = Caster.toString(layoutArgs["title"], "")
                if (!StringUtil.isEmpty(title, true)) builder.withTitle(title)
                layoutArgs.put("title", title)

                // font name
                val fontName: String = Caster.toString(layoutArgs["fontName"], "")
                if (!StringUtil.isEmpty(fontName, true)) builder.withFontName(fontName)
                layoutArgs.put("fontName", fontName)

                // font size
                val fontSize: FontSize? = toFontSize(Caster.toString(layoutArgs["fontSize"], null))
                if (fontSize != null) builder.withFontSize(fontSize)
                layoutArgs.put("fontSize", if (fontSize == null) "" else fontSize.name())
                layout = builder.build()
            } else if (XMLLayout::class.java.getName().equalsIgnoreCase(cd.getClassName())) {

                // Location Info
                val locInfo: Boolean = Caster.toBooleanValue(layoutArgs!!["locationinfo"], false)
                layoutArgs.put("locationinfo", locInfo.toString() + "")

                // Properties TODO
                val props: Boolean = Caster.toBoolean(layoutArgs["properties"], null)
                layoutArgs.put("properties", props.toString())
                // TODO add more attribute
                return XMLLayout(CharsetUtil.UTF8, true, locInfo)
            } else if (PatternLayout::class.java.getName().equalsIgnoreCase(cd.getClassName())) {
                val builder: org.apache.logging.log4j.core.layout.PatternLayout.Builder = PatternLayout.newBuilder()

                // pattern
                val pattern: String = Caster.toString(layoutArgs!!["pattern"], null)
                if (!StringUtil.isEmpty(pattern, true)) builder.withPattern(pattern) else {
                    builder.withPattern(DEFAULT_PATTERN)
                    layoutArgs.put("pattern", DEFAULT_PATTERN)
                }
                layout = builder.build()
            } else if (cd.getClassName().indexOf(".DataDogLayout") !== -1) {
                layout = DataDogLayout()
            } else {
                // MUST that will no longer work that way
                val obj: Object = ClassUtil.loadInstance(cd.getClazz(null), null, null)
                if (obj is Layout) {
                    Reflector.callSetter(obj, "name", name)
                    Reflector.callSetter(obj, "layout", toLayout(layout))
                    val it: Iterator<Entry<String, String>> = layoutArgs.entrySet().iterator()
                    var entry: Entry<String, String>
                    while (it.hasNext()) {
                        entry = it.next()
                        val mi: MethodInstance = Reflector.getSetter(obj, entry.getKey(), entry.getValue(), null)
                        if (mi != null) {
                            try {
                                mi.invoke(obj)
                            } catch (e: Exception) {
                                throw Caster.toPageException(e)
                            }
                        }
                    }
                }
            }
        }
        return if (layout != null) layout else ClassicLayout()
    }

    @Override
    fun getAppender(config: Config, layout: Object, name: String, cd: ClassDefinition?, appenderArgs: Map<String?, String?>?): Object? {
        var appenderArgs = appenderArgs
        if (appenderArgs == null) appenderArgs = HashMap<String, String>()
        // Appender
        var appender: Appender? = null
        if (cd != null && cd.hasClass()) {
            // Console Appender
            if (ConsoleAppender::class.java.getName().equalsIgnoreCase(cd.getClassName())) {
                // stream-type
                var doError = false
                var st: String = Caster.toString(appenderArgs!!["streamtype"], null)
                if (!StringUtil.isEmpty(st, true)) {
                    st = st.trim().toLowerCase()
                    if (st.equals("err") || st.equals("error")) doError = true
                }
                appenderArgs.put("streamtype", if (doError) "error" else "output")

                // get print writer
                val pw: PrintWriter
                if (doError) {
                    if (config.getErrWriter() == null) pw = PrintWriter(System.err) else pw = config.getErrWriter()
                } else {
                    if (config.getOutWriter() == null) pw = PrintWriter(System.out) else pw = config.getOutWriter()
                }
                var l: Layout
                try {
                    l = toLayout(layout)
                } catch (e: Exception) {
                    LogUtil.logGlobal(config, "loading-log", e)
                    l = ClassicLayout()
                }
                appender = getConsoleAppender(createFullName(config, name), pw, l, true)
            } else if (DatasourceAppender::class.java.getName().equalsIgnoreCase(cd.getClassName())) {
                // datasource
                var dsn: String = Caster.toString(appenderArgs!!["datasource"], null)
                if (StringUtil.isEmpty(dsn, true)) dsn = Caster.toString(appenderArgs["datasourceName"], null)
                if (!StringUtil.isEmpty(dsn, true)) dsn = dsn.trim()
                appenderArgs.put("datasource", dsn)

                // username
                var user: String? = Caster.toString(appenderArgs["username"], null)
                if (StringUtil.isEmpty(user, true)) user = Caster.toString(appenderArgs["user"], null)
                if (!StringUtil.isEmpty(user, true)) user = user.trim() else user = null
                appenderArgs.put("username", user)

                // password
                var pass: String? = Caster.toString(appenderArgs["password"], null)
                if (StringUtil.isEmpty(pass, true)) pass = Caster.toString(appenderArgs["pass"], null)
                if (!StringUtil.isEmpty(pass, true)) pass = pass.trim() else pass = null
                appenderArgs.put("password", pass)

                // table
                var table: String = Caster.toString(appenderArgs["table"], null)
                if (!StringUtil.isEmpty(table, true)) table = table.trim() else table = "LOGS"
                appenderArgs.put("table", table)

                // custom
                var custom: String? = Caster.toString(appenderArgs["custom"], null)
                if (!StringUtil.isEmpty(custom, true)) custom = custom.trim() else custom = null
                appenderArgs.put("custom", custom)
                // load appender
                try {
                    appender = getDatasourceAppender(config, createFullName(config, name), dsn, user, pass, table, custom, true)
                } catch (e: Exception) {
                    LogUtil.logGlobal(config, "loading-log", e)
                }
            } else if (ResourceAppender::class.java.getName().equalsIgnoreCase(cd.getClassName())) {

                // path
                var res: Resource? = null
                var path: String = Caster.toString(appenderArgs!!["path"], null)
                if (!StringUtil.isEmpty(path, true)) {
                    path = path.trim()
                    path = ConfigWebUtil.translateOldPath(path)
                    res = ConfigWebUtil.getFile(config, config.getConfigDir(), path, ResourceUtil.TYPE_FILE)
                    if (res.isDirectory()) {
                        res = res.getRealResource("$name.log")
                    }
                }
                if (res == null) {
                    res = ConfigWebUtil.getFile(config, config.getConfigDir(), "logs/$name.log", ResourceUtil.TYPE_FILE)
                }

                // charset
                var charset: Charset = CharsetUtil.toCharset(Caster.toString(appenderArgs["charset"], null), null)
                if (charset == null) {
                    charset = config.getResourceCharset()
                    appenderArgs.put("charset", charset.name())
                }

                // maxfiles
                val maxfiles: Int = Caster.toIntValue(appenderArgs["maxfiles"], 10)
                appenderArgs.put("maxfiles", Caster.toString(maxfiles))

                // maxfileSize
                val maxfilesize: Long = Caster.toLongValue(appenderArgs["maxfilesize"], 1024 * 1024 * 10)
                appenderArgs.put("maxfilesize", Caster.toString(maxfilesize))

                // timeout
                val timeout: Int = Caster.toIntValue(appenderArgs["timeout"], 60) // timeout in seconds
                appenderArgs.put("timeout", Caster.toString(timeout))
                try {
                    appender = toResourceAppender(createFullName(config, name), res, toLayout(layout), charset, maxfiles, maxfilesize, timeout, true)
                } catch (e: Exception) {
                    LogUtil.logGlobal(config, "loading-log", e)
                }
            } else {
                try {
                    val obj: Object = ClassUtil.loadInstance(cd.getClazz(null), null, null)
                    if (obj is Appender) {
                        appender = obj as Appender
                        Reflector.callSetter(obj, "name", name)
                        Reflector.callSetter(obj, "layout", toLayout(layout))
                        val it: Iterator<Entry<String, String>> = appenderArgs.entrySet().iterator()
                        var entry: Entry<String, String>
                        while (it.hasNext()) {
                            entry = it.next()
                            val mi: MethodInstance = Reflector.getSetter(obj, entry.getKey(), entry.getValue(), null)
                            if (mi != null) {
                                try {
                                    mi.invoke(obj)
                                } catch (e: Exception) {
                                    throw Caster.toPageException(e)
                                }
                            }
                        }
                    }
                } catch (e: Exception) {
                    LogUtil.logGlobal(config, "loading-log", e)
                    appender = null
                }
            }
        }
        // if (appender instanceof AppenderSkeleton) {
        // TODO ((AppenderSkeleton) appender).activateOptions();
        // }
        if (appender == null) {
            val pw: PrintWriter
            if (config.getOutWriter() == null) pw = PrintWriter(System.out) else pw = config.getOutWriter()
            var l: Layout
            try {
                l = toLayout(layout)
            } catch (e: Exception) {
                LogUtil.logGlobal(config, "loading-log", e)
                appender = null
                l = ClassicLayout()
            } // l = new ClassicLayout();
            appender = getConsoleAppender(createFullName(config, name), pw, l, true)
        }
        return appender
    }

    @Override
    @Throws(ApplicationException::class)
    fun getLogger(config: Config, appender: Object, name: String, level: Int): Log {
        return _getLogger(config, toAppender(appender), name, level)
    }

    @Override
    @Throws(ApplicationException::class)
    fun closeAppender(appender: Object) {
        toAppender(appender).stop()
    }

    @Throws(ApplicationException::class)
    private fun toAppender(l: Object): Appender {
        if (l is Appender) return l as Appender
        throw ApplicationException("cannot convert [$l] to an Appender")
    }

    @Throws(ApplicationException::class)
    private fun toLayout(l: Object): Layout {
        if (l is Layout) return l as Layout
        throw ApplicationException("cannot convert [$l] to a Layout")
    }

    @get:Override
    val defaultLayout: Object
        get() = PatternLayout.newBuilder().withPattern("%d{dd.MM.yyyy HH:mm:ss,SSS} %-5p [%c] %m%n").build()

    @get:Override
    val classicLayout: Object
        get() = ClassicLayout()

    @Throws(PageException::class)
    private fun toResourceAppender(name: String, res: Resource?, layout: Layout<*>, charset: Charset?, maxfiles: Int, maxFileSize: Long, timeout: Int, start: Boolean): Appender {
        return try {
            val appender = ResourceAppender(name, null, layout, res, charset, true, timeout, maxFileSize, maxfiles, null)
            if (start) appender.start()
            appender
        } catch (e: IOException) {
            throw Caster.toPageException(e)
        }
    }

    private fun toFontSize(str: String): FontSize? {
        var str = str
        if (StringUtil.isEmpty(str, true)) return null
        str = str.trim()
        if ("large".equalsIgnoreCase(str)) return FontSize.LARGE
        if ("larger".equalsIgnoreCase(str)) return FontSize.LARGER
        if ("medium".equalsIgnoreCase(str)) return FontSize.MEDIUM
        if ("small".equalsIgnoreCase(str)) return FontSize.SMALL
        if ("smaller".equalsIgnoreCase(str)) return FontSize.SMALLER
        if ("xlarge".equalsIgnoreCase(str)) return FontSize.XLARGE
        if ("xsmall".equalsIgnoreCase(str)) return FontSize.XSMALL
        if ("xxlarge".equalsIgnoreCase(str)) return FontSize.XXLARGE
        return if ("xxsmall".equalsIgnoreCase(str)) FontSize.XXSMALL else null
    }

    companion object {
        const val DEFAULT_MAX_FILE_SIZE = (10 * 1024 * 1024).toLong()
        const val DEFAULT_MAX_BACKUP_INDEX = 10
        private const val DEFAULT_PATTERN = "%d{dd.MM.yyyy HH:mm:ss,SSS} %-5p [%c] %m%n"
        private val loggers: Map<String, LogAdapter> = ConcurrentHashMap()
        private var fallback: Appender? = null
        private fun _getLogger(config: Config, appender: Appender, name: String, level: Int): LogAdapter {
            val le: Level = LogAdapter.toLevel(level)
            if (LogManager.getFactory() !is org.apache.logging.log4j.core.impl.Log4jContextFactory) {
                init()
            }
            val fullname = createFullName(config, name)

            // fullname
            val l: Logger = LogManager.getLogger(fullname)
            if (l is org.apache.logging.log4j.core.Logger) {
                val cl: org.apache.logging.log4j.core.Logger = l as org.apache.logging.log4j.core.Logger
                for (a in cl.getAppenders().values()) {
                    cl.removeAppender(a)
                }
                cl.setAdditive(false)
                cl.addAppender(appender)
                cl.setLevel(LogAdapter.toLevel(level))
            } else {
                l.atLevel(LogAdapter.toLevel(level))
            }
            val la = LogAdapter(l, le)
            loggers.put(fullname, la)

            // rest the log level of all existing new, because they get lost when creating a new one
            for (tmp in loggers.values()) {
                tmp.validate()
            }
            return la
        }

        private fun createFullName(config: Config?, name: String): String {
            var fullname: String? = name
            if (config is ConfigWeb) {
                val cw: ConfigWeb? = config as ConfigWeb?
                return "web." + cw.getLabel().toString() + "." + name
            }
            return if (config == null) name else "server." + name.also { fullname = it }
        }

        private fun init() {
            StatusLogger.getLogger().setLevel(Level.FATAL)
            LogManager.setFactory(Log4jContextFactory())
            PluginManager.addPackage("")
        }

        @Throws(PageException::class)
        private fun getDatasourceAppender(config: Config, name: String, dsn: String, user: String?, pass: String?, table: String, custom: String?, start: Boolean): Appender {
            val appender = DatasourceAppender(config, getFallback(config), name, null, dsn, user, pass, table, custom)
            if (start) appender.start()
            return appender
        }

        private fun getConsoleAppender(name: String, pw: PrintWriter?, layout: Layout<*>, start: Boolean): Appender {
            val appender: WriterAppender = WriterAppender.newBuilder()
                    .setName(name)
                    .setTarget(pw)
                    .setLayout(layout)
                    .build()
            if (start) appender.start()
            return appender
        }

        private fun getFallback(config: Config): Appender? {
            if (fallback == null) {
                val pw: PrintWriter
                if (config.getErrWriter() == null) pw = PrintWriter(System.err) else pw = config.getErrWriter()
                fallback = getConsoleAppender(createFullName(ThreadLocalPageContext.getConfig(), "fallback"), pw, PatternLayout.newBuilder().withPattern(DEFAULT_PATTERN).build(),
                        true)
            }
            return fallback
        }
    }

    init {
        this.config = config
        init()
    }
}