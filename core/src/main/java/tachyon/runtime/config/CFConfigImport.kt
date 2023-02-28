package tachyon.runtime.config

import java.io.File

class CFConfigImport {
    private var file: Resource? = null
    private var charset: Charset?
    private var password: String?
    private var tag: Tag? = null
    private var dynAttr: DynamicAttributes? = null
    private var type: String? = "server"
    private var engine: CFMLEngine?
    private var config: ConfigPro? = null
    private var placeHolderData: Struct?
    private var data: Struct? = null
    private var pwCheckedServer = false
    private var pwCheckedWeb = false

    constructor(config: Config?, file: Resource?, charset: Charset?, password: String?, type: String?, placeHolderData: Struct?) {
        this.file = file
        this.charset = charset
        this.password = password
        this.type = type
        this.placeHolderData = placeHolderData
        engine = CFMLEngineFactory.getInstance()
        if ("web".equalsIgnoreCase(type) && config !is ConfigWeb) throw engine.getExceptionUtil().createApplicationException("cannot manipulate a web context when you pass in a server config to the constructor!")
        if ("server".equalsIgnoreCase(type) && config is ConfigWeb) {
            setPasswordIfNecessary(config as ConfigWeb?)
            this.config = config.getConfigServer(password)
        } else this.config = config
    }

    constructor(config: Config?, data: Struct?, charset: Charset?, password: String?, type: String?, placeHolderData: Struct?) {
        this.data = data
        this.charset = charset
        this.password = password
        this.type = type
        this.placeHolderData = placeHolderData
        engine = CFMLEngineFactory.getInstance()
        if ("web".equalsIgnoreCase(type) && config !is ConfigWeb) throw engine.getExceptionUtil().createApplicationException("cannot manipulate a web context when you pass in a server config to the constructor!")
        if ("server".equalsIgnoreCase(type) && config is ConfigWeb) {
            setPasswordIfNecessary(config as ConfigWeb?)
            this.config = config.getConfigServer(password)
        } else this.config = config
    }

    @Throws(PageException::class)
    fun execute(): Struct? {
        var unregister = false
        var pc: PageContext = ThreadLocalPageContext.get()
        return try {
            if (pc == null) {
                pc = engine.createPageContext(SystemUtil.getTempDirectory() as File, "localhost", "/", "", arrayOfNulls<Cookie?>(0), null, null, null,
                        DevNullOutputStream.DEV_NULL_OUTPUT_STREAM, 100000, true)
                unregister = true
            }
            if (Util.isEmpty(password)) {
                val sysprop = "tachyon." + type.toUpperCase().toString() + ".admin.password"
                val envVarName: String = sysprop.replace('.', '_').toUpperCase()
                password = SystemUtil.getSystemPropOrEnvVar(sysprop, null)
                if (password == null) throw engine.getExceptionUtil()
                        .createApplicationException("missing password to access the Tachyon configutation. This can be set in two ways, as enviroment variable [" + envVarName
                                + "] or as system property [" + sysprop + "].")
            }
            val cast: Cast = engine.getCastUtil()
            if (ACTION == null) ACTION = cast.toKey("action")
            if (TYPE == null) TYPE = cast.toKey("type")
            if (PASSWORD == null) PASSWORD = cast.toKey("password")
            if (MAPPINGS == null) MAPPINGS = cast.toKey("mappings")
            if (VIRTUAL == null) VIRTUAL = cast.toKey("virtual")
            if (DATASOURCES == null) DATASOURCES = cast.toKey("datasources")
            if (NAME == null) NAME = cast.toKey("name")
            if (DATABASE == null) DATABASE = cast.toKey("database")
            val json: Struct
            json = if (data != null) {
                data
            } else {
                val raw: String = engine.getIOUtil().toString(file, charset)
                cast.toStruct(JSONExpressionInterpreter().interpret(null, raw))
            }
            replacePlaceHolder(json, placeHolderData)
            tag = engine.getClassUtil().loadClass("tachyon.runtime.tag.Admin").newInstance() as Tag
            dynAttr = tag as DynamicAttributes?
            set(pc, json, "updateCharset", Item("webcharset"), Item("resourcecharset"), Item("templatecharset"))
            set(pc, json, "updateRegional", Item("usetimeserver").setDefault(true), Item("locale").setDefault(config.getLocale()),
                    Item("timeserver").setDefault(config.getTimeServer()), Item("timezone").setDefault(config.getTimeZone()))
            set(pc, json, "updateApplicationListener", Item(arrayOf("applicationMode"), "listenermode", null),
                    Item(arrayOf("applicationListener", "applicationType", "listenertype"), "listenertype", null)
                            .setDefault(config.getApplicationListener().getType()))
            set(pc, json, "updatePerformanceSettings", Item("inspecttemplate"), Item("cachedafter"), Item("typechecking"))
            set(pc, json, "updateApplicationSetting", Item("applicationpathtimeout"), Item("requesttimeout"), Item("scriptprotect"),
                    Item("requestTimeoutInURL", "allowurlrequesttimeout"))
            set(pc, json, "updateCompilerSettings", Item("nullsupport"), Item("handleunquotedattrvalueasstring"), Item("externalizestringgte"),
                    Item("dotnotationuppercase", "dotNotationUpperCase"), Item("suppressWhitespaceBeforeArgument", "suppresswsbeforearg"), Item("templatecharset"))
            set(pc, json, "updateSecurity", Item("varusage"))
            set(pc, json, "updateOutputSetting", Item("allowcompression"), Item("whitespaceManagement", "cfmlwriter"), Item("suppresscontent"),
                    Item("bufferTagBodyOutput", "bufferoutput"), Item("showContentLength", "contentlength"))
            set(pc, json, "updateRegex", Item("regextype"))
            set(pc, json, "updateORMSetting", Item("ormconfig"), Item("ormsqlscript", "sqlscript"), Item("ormusedbformapping", "usedbformapping"),
                    Item("ormeventhandling", "eventhandling"), Item("ormsecondarycacheenabled", "secondarycacheenabled"), Item("ormautogenmap", "autogenmap"),
                    Item("ormlogsql", "logsql"), Item("ormcacheconfig", "cacheconfig"), Item("ormsavemapping", "savemapping"), Item("ormschema", "schema"),
                    Item("ormdbcreate", "dbcreate"), Item("ormcfclocation", "cfclocation"), Item("ormflushatrequestend", "flushatrequestend"),
                    Item("ormcacheprovider", "cacheprovider"), Item("ormcatalog", "catalog"))
            set(pc, json, "updateMailSetting", Item(arrayOf("mailDefaultEncoding"), "defaultencoding", null),
                    Item(arrayOf("mailConnectionTimeout"), "timeout", null), Item("mailSpoolEnable", "spoolenable"))
            set(pc, json, "updateRestSettings", Item(arrayOf("Restlist"), "list", null))
            set(pc, json, "updateComponent", Item("componentUseVariablesScope", "useshadow"), Item("componentdumptemplate"),
                    Item(arrayOf("componentDeepSearch"), "deepsearch", null).setDefault(false), Item("basecomponenttemplatetachyon"), Item("componentpathcache"),
                    Item("componentdatamemberdefaultaccess"), Item("basecomponenttemplatecfml"), Item("componentlocalsearch"), Item("componentdefaultimport"),
                    Item("componentImplicitNotation", "triggerdatamember"))
            set(pc, json, "updateCustomTagSetting", Item(arrayOf("customTagLocalSearch"), "localsearch", null).setDefault(config.doLocalCustomTag()),
                    Item(arrayOf("customTagDeepSearch"), "deepsearch", null).setDefault(config.doCustomTagDeepSearch()),
                    Item(arrayOf("customTagExtensions"), "extensions", null).setDefault(config.getCustomTagExtensions()),
                    Item("customtagpathcache").setDefault(true))
            set(pc, json, "updateDebug", Item(arrayOf("debuggingException"), "exception", null),
                    Item(arrayOf("debuggingImplicitAccess"), "implicitaccess", null), Item(arrayOf("debuggingTracing"), "tracing", null),
                    Item(arrayOf("debuggingQueryUsage"), "queryusage", null), Item(arrayOf("debuggingTemplate"), "template", null),
                    Item(arrayOf("debuggingDatabase"), "database", null), Item(arrayOf("debuggingDump"), "dump", null), Item("debugtemplate"),
                    Item(arrayOf("debuggingEnable", "debuggingEnabled"), "debug", null), Item(arrayOf("debuggingTimer"), "timer", null))
            set(pc, json, "updatemailsetting", Item(arrayOf("mailSpoolEnable", "mailSpoolEnabled"), "spoolenable", null),
                    Item(arrayOf("mailConnectionTimeout", "mailTimeout"), "timeout", null),
                    Item(arrayOf("maildefaultencoding", "mailencoding"), "defaultencoding", null))
            set(pc, json, "updateError", Item(arrayOf("generalErrorTemplate"), "template500", null),
                    Item(arrayOf("missingErrorTemplate"), "template404", null), Item(arrayOf("errorStatusCode"), "statuscode", null).setDefault(true))
            setGroup(pc, json, "updateDatasource", "datasources", arrayOf("name", "databases"), Item("class", "classname"), Item("bundleName"),
                    Item("bundleVersion"), Item("connectionlimit").setDefault(-1), Item("connectiontimeout").setDefault(-1), Item("livetimeout").setDefault(-1),
                    Item("custom"), Item("validate").setDefault(false), Item("verify").setDefault(true), Item("host"), Item("port").setDefault(-1),
                    Item("connectionString", "dsn"), Item("username", "dbusername"), Item("password", "dbpassword"), Item("storage").setDefault(false),
                    Item("metacachetimeout").setDefault(60000), Item("alwayssettimeout").setDefault(false), Item("dbdriver"), Item("database"),
                    Item("blob").setDefault(false), Item("name"), Item("requestexclusive").setDefault(false), Item("customparametersyntax"),
                    Item("alwaysresetconnections").setDefault(false), Item("timezone"), Item("clob").setDefault(false),
                    Item("literaltimestampwithtsoffset").setDefault(false), Item("newname")
            )
            setGroup(pc, json, "updateCacheConnection", "caches", arrayOf("name"), Item("bundlename"), Item("default"), Item("storage").setDefault(false),
                    Item("bundleversion"), Item("name"), Item("custom"), Item("class"))
            setGroup(pc, json, "updateGatewayEntry", "gateways", arrayOf("id"), Item("startupmode"), Item("listenercfcpath"), Item("cfcpath"), Item("id"),
                    Item("custom"), Item("class"))
            setGroup(pc, json, "updateLogSettings", "loggers", arrayOf("name"), Item("layoutbundlename"), Item("level"),
                    Item("appenderArguments", "appenderargs").setDefault(engine.getCreationUtil().createStruct()), Item("name"),
                    Item("layoutArguments", "layoutargs").setDefault(engine.getCreationUtil().createStruct()), Item("appenderClass", AppenderModifier()),
                    Item("appender"), Item("layoutClass", LayoutModifier()), Item("layout"))
            setGroup(pc, json, "updateMailServer", "mailServers", arrayOf(), Item("life").setDefault(60), Item("tls").setDefault(false),
                    Item("idle").setDefault(10), Item("username", "dbusername"), Item(arrayOf("smtp", "host", "server"), "hostname", null), Item("id"),
                    Item("port").setDefault(-1), Item("password", "dbpassword"), Item("ssl").setDefault(false))
            setGroup(pc, json, "updateMapping", arrayOf("mappings", "cfmappings"), arrayOf("virtual"), Item("virtual"),
                    Item("inspect").addName("inspectTemplate"), Item("physical"), Item("primary"), Item("toplevel").setDefault(true), Item("archive"))
            setGroup(pc, json, "updateCustomTag", arrayOf("customTagMappings", "customTagPaths"), arrayOf("virtual"),
                    Item("virtual", CreateHashModifier(arrayOf("virtual", "name", "label"), "physical")), Item("inspect").addName("inspectTemplate"),
                    Item("physical"), Item("primary"), Item("archive"))
            setGroup(pc, json, "updateComponentMapping", arrayOf("componentMappings", "componentPaths"), arrayOf("virtual"),
                    Item("virtual", CreateHashModifier(arrayOf("virtual", "name", "label"), "physical")), Item("inspect").addName("inspectTemplate"),
                    Item("physical"), Item("primary"), Item("archive"))
            optimizeExtensions(config, json)
            setGroup(pc, json, "updateRHExtension", "extensions", arrayOf(), Item("source"), Item("id"), Item("version"))

            // need to be at the end
            set(pc, json, "updateScope", Item("sessiontype"), Item("sessionmanagement"), Item("setdomaincookies", "domaincookies"), Item("allowimplicidquerycall"),
                    Item("setclientcookies", "clientcookies"), Item("mergeformandurl"), Item("localScopeMode", "localmode"),
                    Item("cgiScopeReadonly", "cgireadonly"), Item("scopecascadingtype"), Item("sessiontimeout"), Item("clienttimeout"), Item("clientstorage"),
                    Item("clientmanagement"), Item("applicationtimeout"), Item("sessionstorage"))
            json
            // TODO cacheDefaultQuery
        } catch (e: Exception) {
            throw engine.getCastUtil().toPageException(e)
        } finally {
            if (unregister) pc.getConfig().getFactory().releaseTachyonPageContext(pc, false)
        }
    }

    @Throws(IOException::class)
    private fun optimizeExtensions(config: Config?, json: Struct?) {
        val cast: Cast = engine.getCastUtil()
        val arr: Array = cast.toArray(json.get("extensions", null), null) ?: return
        var data: Struct
        var path: String
        var src: Resource
        val it: Iterator<Object?> = arr.valueIterator()
        while (it.hasNext()) {
            data = cast.toStruct(it.next(), null)
            if (data == null) continue
            path = cast.toString(data.get("source", null), null)
            if (Util.isEmpty(path)) continue
            src = cast.toResource(path, null)
            if (!src.isFile()) continue
            val idAndVersion = extractExtesnionInfoFromManifest(src) ?: continue
            val extDir: Resource = config.getLocalExtensionProviderDirectory()
            val trg: Resource = extDir.getRealResource(idAndVersion[0].toString() + "-" + idAndVersion[1] + ".lex")
            if (!trg.isFile()) {
                engine.getIOUtil().copy(src, trg)
            }
            data.remove("source")
            data.setEL("id", idAndVersion[0])
            data.setEL("version", idAndVersion[1])
        }
    }

    @Throws(JspException::class)
    private fun setGroup(pc: PageContext?, json: Struct?, trgActionName: String?, srcGroupName: String?, keyNames: Array<String?>?, vararg items: Item?) {
        setGroup(pc, json, trgActionName, arrayOf(srcGroupName), keyNames, *items)
    }

    @Throws(JspException::class)
    private fun setGroup(pc: PageContext?, json: Struct?, trgActionName: String?, srcGroupNames: Array<String?>?, keyNames: Array<String?>?, vararg items: Item?) {
        val cast: Cast = engine.getCastUtil()
        var group: Collection? = null
        for (srcGroupName in srcGroupNames!!) {
            group = cast.toCollection(json.get(cast.toKey(srcGroupName), null), null)
            if (group != null) break
        }
        if (group != null) {
            val it: Iterator<Entry<Key?, Object?>?> = group.entryIterator()
            var e: Entry<Key?, Object?>?
            var data: Struct
            while (it.hasNext()) {
                e = it.next()
                data = cast.toStruct(e.getValue(), null)
                if (data == null) continue
                if (group !is Array) {
                    for (keyName in keyNames!!) {
                        data.set(cast.toKey(keyName), e.getKey().getString())
                    }
                }
                set(pc, data, trgActionName, *items)
            }
        }
    }

    @Throws(PageException::class)
    private fun setPasswordIfNecessary(config: ConfigWeb?) {
        val isServer: Boolean = "server".equalsIgnoreCase(type)
        if (isServer && !pwCheckedServer || !isServer && !pwCheckedWeb) {
            val hasPassword: Boolean = if (isServer) config.hasServerPassword() else config.hasPassword()
            if (!hasPassword) {
                // create password
                try {
                    (config as ConfigWebPro?)!!.updatePassword(isServer, null, password)
                } catch (e: Exception) {
                    throw Caster.toPageException(e)
                }
            }
            if (isServer) pwCheckedServer = true else pwCheckedWeb = true
        }
    }

    @Throws(JspException::class)
    private fun set(pc: PageContext?, json: Struct?, trgActionName: String?, vararg items: Item?) {
        setPasswordIfNecessary(pc.getConfig())
        var `val`: Object
        try {
            tag.setPageContext(pc)
            var empty = true
            for (item in items) {
                `val` = item!!.getValue(json)
                if (`val` != null) empty = false else `val` = item.getDefault()
                dynAttr.setDynamicAttribute(null, item.getTargetAttrName(), `val`)
            }
            if (empty) {
                tag.release()
                return
            }
            dynAttr.setDynamicAttribute(null, ACTION, trgActionName)
            dynAttr.setDynamicAttribute(null, TYPE, type)
            dynAttr.setDynamicAttribute(null, PASSWORD, password)
            tag.doStartTag()
            tag.doEndTag()
        } finally {
            tag.release()
        }
    }

    private class Item {
        private var srcKeyNames: Array<String?>?
        private val trgAttrName: String?
        private var e: CFMLEngine?
        private var modifier: Modifier?
        private var def: Object? = ""

        constructor(name: String?) : this(name, null as Modifier?) {}
        constructor(name: String?, modifier: Modifier?) {
            srcKeyNames = arrayOf(name)
            trgAttrName = name
            e = CFMLEngineFactory.getInstance()
            this.modifier = modifier
        }

        constructor(srcKeyName: String?, trgAttrName: String?) : this(srcKeyName, trgAttrName, null as Modifier?) {}
        constructor(srcKeyName: String?, trgAttrName: String?, modifier: Modifier?) {
            srcKeyNames = arrayOf(srcKeyName, trgAttrName)
            this.trgAttrName = trgAttrName
            e = CFMLEngineFactory.getInstance()
            this.modifier = modifier
        }

        constructor(srcKeyNames: Array<String?>?, trgAttrName: String?, modifier: Modifier?) {
            this.srcKeyNames = srcKeyNames
            this.trgAttrName = trgAttrName
            e = CFMLEngineFactory.getInstance()
            this.modifier = modifier
        }

        fun setDefault(def: Object?): Item? {
            this.def = def
            return this
        }

        fun addName(name: String?): Item? {
            val tmp = arrayOfNulls<String?>(srcKeyNames!!.size + 1)
            for (i in srcKeyNames.indices) {
                tmp[i] = srcKeyNames!![i]
            }
            tmp[tmp.size - 1] = name
            srcKeyNames = tmp
            return this
        }

        fun getDefault(): Object? {
            return def
        }

        fun getTargetAttrName(): Key? {
            return e.getCastUtil().toKey(trgAttrName)
        }

        fun getValue(json: Struct?): Object? {
            if (modifier != null) {
                return modifier!!.getValue(json)
            }
            var obj: Object? = null
            for (srcKeyName in srcKeyNames!!) {
                obj = json.get(e.getCastUtil().toKey(srcKeyName), null)
                if (obj == null) continue
                if (obj !is String) break
                if (!Util.isEmpty(obj as String?, true)) break
                obj = null
            }
            return obj
        }
    }

    private interface Modifier {
        fun getValue(json: Struct?): String?
    }

    private abstract class ALModifier : Modifier {
        fun getValue(json: Struct?, name: String?): String? {
            // to we have the main key?
            val e: CFMLEngine = CFMLEngineFactory.getInstance()
            var data: String? = null
            data = e.getCastUtil().toString(json.get(e.getCastUtil().toKey(name), null), null)
            return if (!Util.isEmpty(data, true)) data else null
        }
    }

    private class AppenderModifier : ALModifier() {
        @Override
        override fun getValue(json: Struct?): String? {
            var `val` = getValue(json, "appenderclass")
            if (`val` != null) return `val`
            `val` = getValue(json, "appender")
            if ("console".equalsIgnoreCase(`val`)) return ConsoleAppender::class.java.getName()
            if ("resource".equalsIgnoreCase(`val`)) return ResourceAppender::class.java.getName()
            return if ("datasource".equalsIgnoreCase(`val`)) DatasourceAppender::class.java.getName() else `val`
        }
    }

    private class LayoutModifier : ALModifier() {
        @Override
        override fun getValue(json: Struct?): String? {
            var `val` = getValue(json, "layoutclass")
            if (`val` != null) return `val`
            `val` = getValue(json, "layout")
            if ("classic".equalsIgnoreCase(`val`)) return ClassicLayout::class.java.getName()
            if ("datasource".equalsIgnoreCase(`val`)) return ClassicLayout::class.java.getName()
            if ("html".equalsIgnoreCase(`val`)) return HtmlLayout::class.java.getName()
            if ("xml".equalsIgnoreCase(`val`)) return XMLLayout::class.java.getName()
            if ("pattern".equalsIgnoreCase(`val`)) return PatternLayout::class.java.getName()
            return if ("datadog".equalsIgnoreCase(`val`)) DataDogLayout::class.java.getName() else `val`
        }
    }

    private class CreateHashModifier(private val mains: Array<String?>?, vararg keysToHash: String?) : Modifier {
        private val keysToHash: Array<String?>?
        @Override
        override fun getValue(json: Struct?): String? {
            // to we have the main key?
            val e: CFMLEngine = CFMLEngineFactory.getInstance()
            var data: String? = null
            for (main in mains!!) {
                data = e.getCastUtil().toString(json.get(e.getCastUtil().toKey(main), null), null)
                if (!Util.isEmpty(data, true)) break
            }
            if (!Util.isEmpty(data, true)) return data

            // if not we create a hash instead
            val sb = StringBuilder()
            for (keyToHash in keysToHash!!) {
                data = e.getCastUtil().toString(json.get(e.getCastUtil().toKey(keyToHash), null), null)
                if (!Util.isEmpty(data, true)) sb.append(data).append(';')
            }
            return e.getSystemUtil().hash64b(sb.toString())
        }

        init {
            this.keysToHash = keysToHash
        }
    }

    companion object {
        private var ACTION: Key? = null
        private var TYPE: Key? = null
        private var PASSWORD: Key? = null
        private var MAPPINGS: Key? = null
        private var DATASOURCES: Key? = null
        private var VIRTUAL: Key? = null
        private var NAME: Key? = null
        private var DATABASE: Key? = null
        private fun replacePlaceHolder(coll: Collection?, placeHolderData: Struct?) {
            // ${MAILSERVER_HOST:smtp.sendgrid.net}
            val it: Iterator<Entry<Key?, Object?>?> = coll.entryIterator()
            var e: Entry<Key?, Object?>?
            var obj: Object
            while (it.hasNext()) {
                e = it.next()
                obj = e.getValue()
                if (obj is String) replacePlaceHolder(e, placeHolderData)
                if (obj is Collection) replacePlaceHolder(obj as Collection, placeHolderData)
            }
        }

        private fun replacePlaceHolder(e: Entry<Key?, Object?>?, placeHolderData: Struct?) {
            val str = e.getValue() as String
            val startIndex: Int = str.indexOf("\${")
            if (startIndex == -1) return
            val endIndex: Int = str.indexOf("}", startIndex + 1)
            if (endIndex == -1) return
            val content: String = str.substring(startIndex + 2, endIndex)
            val envVarName: String?
            var defaultValue: String? = ""
            val index: Int = content.indexOf(':')
            if (index == -1) {
                envVarName = content
            } else {
                envVarName = content.substring(0, index)
                defaultValue = content.substring(index + 1)
            }
            var `val`: Object? = null
            if (placeHolderData != null) `val` = placeHolderData.get(KeyImpl.init(envVarName), null)
            if (`val` == null) `val` = SystemUtil.getSystemPropOrEnvVar(envVarName, null)
            if (`val` != null) e.setValue(`val`) else e.setValue(defaultValue)
        }

        @Throws(IOException::class)
        private fun extractManifest(src: Resource?): Manifest? {
            var zis: ZipInputStream? = null
            try {
                zis = ZipInputStream(Util.toBufferedInputStream(src.getInputStream()))
                var entry: ZipEntry?
                var mf: Manifest? = null
                while (zis.getNextEntry().also { entry = it } != null) {
                    if (!entry.isDirectory()) {
                        if (entry.getName().indexOf("META-INF/MANIFEST.MF") !== -1) {
                            mf = Manifest(zis)
                        }
                    }
                    zis.closeEntry()
                    if (mf != null) return mf
                }
            } finally {
                Util.closeEL(zis)
            }
            return null
        }

        @Throws(IOException::class)
        private fun extractExtesnionInfoFromManifest(src: Resource?): Array<String?>? {
            val mf: Manifest? = extractManifest(src)
            if (mf != null) {
                val attrs: Attributes = mf.getMainAttributes()
                val id = unwrap(attrs.getValue("id"))
                val version = unwrap(attrs.getValue("version"))
                return arrayOf(id, version)
            }
            return null
        }

        private fun unwrap(str: String?): String? {
            var str = str ?: return null
            if (Util.isEmpty(str)) return ""
            str = str.trim()
            return if (str.startsWith("\"") && str.endsWith("\"")) str.substring(1, str.length() - 1).trim() else str
        }
    }
}