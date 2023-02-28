/**
 * Copyright (c) 2023, TachyonCFML.org
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
package tachyon.runtime.config

import java.io.FileNotFoundException

object ConfigFactory {
    const val NEW_NONE = 0
    const val NEW_MINOR = 1
    const val NEW_FRESH = 2
    const val NEW_FROM4 = 3
    fun getNew(engine: CFMLEngine?, contextDir: Resource?, readOnly: Boolean, defaultValue: UpdateInfo?): UpdateInfo? {
        return try {
            getNew(engine, contextDir, readOnly)
        } catch (e: Exception) {
            defaultValue
        }
    }

    @Throws(IOException::class, BundleException::class)
    fun getNew(engine: CFMLEngine?, contextDir: Resource?, readOnly: Boolean): UpdateInfo? {
        val info: tachyon.Info = engine.getInfo()
        var strOldVersion: String?
        val resOldVersion: Resource = contextDir.getRealResource("version")
        val strNewVersion: String = info.getVersion().toString() + "-" + info.getRealeaseTime()
        // fresh install
        if (!resOldVersion.exists()) {
            if (!readOnly) {
                resOldVersion.createNewFile()
                IOUtil.write(resOldVersion, strNewVersion, SystemUtil.getCharset(), false)
            }
            return UpdateInfo.NEW_FRESH
        } else if (!IOUtil.toString(resOldVersion, SystemUtil.getCharset()).also { strOldVersion = it }.equals(strNewVersion)) {
            if (!readOnly) {
                IOUtil.write(resOldVersion, strNewVersion, SystemUtil.getCharset(), false)
            }
            val oldVersion: Version = OSGiUtil.toVersion(strOldVersion)
            return UpdateInfo(oldVersion, if (oldVersion.getMajor() < 5) NEW_FROM4 else NEW_MINOR)
        }
        return UpdateInfo.NEW_NONE
    }

    fun updateRequiredExtension(engine: CFMLEngine?, contextDir: Resource?, log: Log?) {
        val info: tachyon.Info = engine.getInfo()
        try {
            val res: Resource = contextDir.getRealResource("required-extension")
            val str: String = info.getVersion().toString() + "-" + info.getRealeaseTime()
            if (!res.exists()) res.createNewFile()
            IOUtil.write(res, str, SystemUtil.getCharset(), false)
        } catch (e: Exception) {
            if (log != null) log.error("required-extension", e)
        }
    }

    fun isRequiredExtension(engine: CFMLEngine?, contextDir: Resource?, log: Log?): Boolean {
        val info: tachyon.Info = engine.getInfo()
        try {
            val res: Resource = contextDir.getRealResource("required-extension")
            if (!res.exists()) return false
            val writtenVersion: String = IOUtil.toString(res, SystemUtil.getCharset())
            val currVersion: String = info.getVersion().toString() + "-" + info.getRealeaseTime()
            return writtenVersion.equals(currVersion)
        } catch (e: Exception) {
            if (log != null) log.error("required-extension", e)
        }
        return false
    }

    /**
     * load XML Document from XML File
     *
     * @param xmlFile XML File to read
     * @return returns the Document
     * @throws SAXException
     * @throws IOException
     * @throws PageException
     */
    @Throws(IOException::class, PageException::class)
    fun loadDocument(file: Resource?): Struct? {
        val `is`: InputStream? = null
        return try {
            _loadDocument(file)
        } finally {
            IOUtil.close(`is`)
        }
    }

    @Throws(SAXException::class, IOException::class, PageException::class)
    fun loadDocumentCreateIfFails(configFile: Resource?, type: String?): Struct? {
        return try {
            _loadDocument(configFile)
        } catch (e: Exception) {
            // rename buggy config files
            if (configFile.exists()) {
                LogUtil.log(ThreadLocalPageContext.getConfig(), Log.LEVEL_INFO, ConfigFactory::class.java.getName(),
                        "Config file [$configFile] was not valid and has been replaced")
                LogUtil.log(ThreadLocalPageContext.get(), ConfigFactory::class.java.getName(), e)
                var count = 1
                var bugFile: Resource?
                val configDir: Resource = configFile.getParentResource()
                while (configDir.getRealResource("tachyon-" + type + "." + count++ + ".buggy").also { bugFile = it }.exists()) {
                }
                IOUtil.copy(configFile, bugFile)
                configFile.delete()
            }
            createConfigFile(type, configFile)
            loadDocument(configFile)
        }
    }

    @Throws(ConverterException::class, IOException::class, SAXException::class)
    fun translateConfigFile(config: ConfigPro?, configFileOld: Resource?, configFileNew: Resource?, mode: String?, isServer: Boolean) {
        // read the old config (XML)
        var root: Struct? = ConfigWebUtil.getAsStruct("cfTachyonConfiguration", XMLConfigReader(configFileOld, true, ReadRule(), NameRule()).getData())

        //////////////////// charset ////////////////////
        run {
            val charset: Struct = ConfigWebUtil.getAsStruct("charset", root)
            val regional: Struct = ConfigWebUtil.getAsStruct("regional", root)
            val fileSystem: Struct = ConfigWebUtil.getAsStruct("fileSystem", root)
            copy("charset", "templateCharset", fileSystem, root) // deprecated but still supported
            copy("encoding", "templateCharset", fileSystem, root) // deprecated but still supported
            move("templateCharset", charset, root)
            move("charset", "webCharset", charset, root) // deprecated but still supported
            copy("encoding", "webCharset", fileSystem, root) // deprecated but still supported
            copy("defaultEncoding", "webCharset", regional, root) // deprecated but still supported
            move("webCharset", charset, root)
            copy("charset", "resourceCharset", fileSystem, root) // deprecated but still supported
            copy("encoding", "resourceCharset", fileSystem, root) // deprecated but still supported
            move("resourceCharset", charset, root)
            rem("charset", root)
        }
        //////////////////// regional ////////////////////
        run {
            val regional: Struct = ConfigWebUtil.getAsStruct("regional", root)
            move("timezone", regional, root)
            move("locale", regional, root)
            move("timeserver", regional, root)
            moveAsBool("useTimeserver", "useTimeserver", regional, root)
            rem("regional", root)
        }
        //////////////////// application ////////////////////
        run {
            val application: Struct = ConfigWebUtil.getAsStruct("application", root)
            val scope: Struct = ConfigWebUtil.getAsStruct("scope", root)
            move("listenerType", application, root)
            move("listenerMode", application, root)
            move("typeChecking", application, root)
            move("cachedAfter", application, root)
            for (type in ConfigWebFactory.STRING_CACHE_TYPES) {
                move("cachedWithin" + StringUtil.ucFirst(type), application, root)
            }
            moveAsBool("allowUrlRequesttimeout", "requestTimeoutInURL", application, root)
            move("requesttimeout", "requestTimeout", scope, root) // deprecated but still supported
            move("requesttimeout", "requestTimeout", application, root)
            move("scriptProtect", application, root)
            move("classicDateParsing", application, root)
            move("cacheDirectory", application, root)
            move("cacheDirectoryMaxSize", application, root)
            move("adminSynchronisation", "adminSync", application, root)
            move("adminSync", application, root)
            rem("application", root)
        }

        //////////////////// caches ////////////////////
        run {
            val cache: Struct = ConfigWebUtil.getAsStruct("cache", root)
            val caches: Struct = ConfigWebUtil.getAsStruct("caches", root)
            val conns: Array = ConfigWebUtil.getAsArray("connection", cache)

            // classes
            move("cache", "cacheClasses", caches, root)

            // defaults
            for (type in ConfigWebFactory.STRING_CACHE_TYPES_MAX) {
                move("default" + StringUtil.ucFirst(type), cache, root)
            }
            // connections
            val it: Iterator<*> = conns.getIterator()
            while (it.hasNext()) {
                val conn: Struct = Caster.toStruct(it.next(), null) ?: continue
                add(conn, Caster.toString(conn.remove(KeyConstants._name, null), null), caches)
            }
            rem("cache", root)
        }

        //////////////////// cache handlers ////////////////////
        run {
            val handlers: Struct = ConfigWebUtil.getAsStruct("cacheHandlers", root)
            val handler: Array = ConfigWebUtil.getAsArray("cacheHandler", handlers)
            val keys: Array<Key?> = handler.keys()
            for (i in keys.indices.reversed()) {
                val k: Key? = keys[i]
                val data: Struct = Caster.toStruct(handler.get(k, null), null) ?: continue
                add(data, Caster.toString(data.remove(KeyConstants._id, null), null), handlers)
                handler.remove(k, null)
            }
        }

        //////////////////// CFX ////////////////////
        run {
            val extTags: Struct = ConfigWebUtil.getAsStruct("extTags", root)
            val extTag: Array = ConfigWebUtil.getAsArray("extTag", extTags)
            val cfx: Struct = ConfigWebUtil.getAsStruct("cfx", root)
            val it: Iterator<*> = extTag.getIterator()
            while (it.hasNext()) {
                val conn: Struct = Caster.toStruct(it.next(), null) ?: continue
                add(conn, Caster.toString(conn.remove(KeyConstants._name, null), null), cfx)
            }
            rem("extTags", root)
        }

        //////////////////// Compiler ////////////////////
        run {
            val compiler: Struct = ConfigWebUtil.getAsStruct("compiler", root)
            moveAsBool("supressWsBeforeArg", "suppressWhitespaceBeforeArgument", compiler, root) // deprecated but still supported
            moveAsBool("suppressWsBeforeArg", "suppressWhitespaceBeforeArgument", compiler, root)
            moveAsBool("dotNotationUpperCase", "dotNotationUpperCase", compiler, root)
            moveAsBool("fullNullSupport", "nullSupport", compiler, root)
            move("defaultFunctionOutput", compiler, root)
            move("externalizeStringGte", compiler, root)
            moveAsBool("allowTachyonDialect", "allowTachyonDialect", compiler, root)
            moveAsBool("handleUnquotedAttributeValueAsString", "handleUnquotedAttributeValueAsString", compiler, root)
            rem("compiler", root)
        }

        //////////////////// Component ////////////////////
        run {
            val component: Struct = ConfigWebUtil.getAsStruct("component", root)
            move("componentDefaultImport", "componentAutoImport", component, root)
            move("base", "componentBase", component, root) // deprecated but still supported
            move("baseCfml", "componentBase", component, root)
            move("baseTachyon", "componentBaseTachyonDialect", component, root)
            moveAsBool("deepSearch", "componentDeepSearch", component, root)
            move("dumpTemplate", "componentDumpTemplate", component, root)
            move("dataMemberDefaultAccess", "componentDataMemberAccess", component, root)
            moveAsBool("triggerDataMember", "componentImplicitNotation", component, root)
            moveAsBool("localSearch", "componentLocalSearch", component, root)
            moveAsBool("useCachePath", "componentUseCachePath", component, root)
            moveAsBool("useShadow", "componentUseVariablesScope", component, root)

            // mappings
            val ctMappings: Array = ConfigWebUtil.getAsArray("mapping", component)
            add(ctMappings, "componentMappings", root)
            rem("mapping", component)
        }

        //////////////////// Custom tags ////////////////////
        run {
            val ct: Struct = ConfigWebUtil.getAsStruct("customTag", root)
            moveAsBool("customTagUseCachePath", "customTagUseCachePath", ct, root)
            moveAsBool("useCachePath", "customTagUseCachePath", ct, root)
            moveAsBool("customTagLocalSearch", "customTagLocalSearch", ct, root)
            moveAsBool("localSearch", "customTagLocalSearch", ct, root)
            moveAsBool("deepSearch", "customTagDeepSearch", ct, root)
            moveAsBool("customTagDeepSearch", "customTagDeepSearch", ct, root)
            move("extensions", "customTagExtensions", ct, root)
            move("customTagExtensions", "customTagExtensions", ct, root)
            val ctMappings: Array = ConfigWebUtil.getAsArray("mapping", ct)
            add(ctMappings, "customTagMappings", root)
            rem("mapping", ct)
        }

        //////////////////// Constants ////////////////////
        run {
            val constants: Struct = ConfigWebUtil.getAsStruct("constants", root)
            val constant: Array = ConfigWebUtil.getAsArray("constant", constants)
            rem("constant", constants)
            val keys: Array<Key?> = constant.keys()
            for (i in keys.indices.reversed()) {
                val k: Key? = keys[i]
                val data: Struct = Caster.toStruct(constant.get(k, null), null) ?: continue
                constants.setEL(KeyImpl.init(Caster.toString(data.get(KeyConstants._name, null), null)), data.get(KeyConstants._value, null))
            }
        }

        //////////////////// JDBC ////////////////////
        run {
            val jdbc: Struct = ConfigWebUtil.getAsStruct("jdbc", root)
            val driver: Array = ConfigWebUtil.getAsArray("driver", jdbc)
            val jdbcDrivers: Struct = ConfigWebUtil.getAsStruct("jdbcDrivers", root)
            val keys: Array<Key?> = driver.keys()
            for (i in keys.indices.reversed()) {
                val k: Key? = keys[i]
                val data: Struct = Caster.toStruct(driver.get(k, null), null) ?: continue
                add(data, Caster.toString(data.remove(KeyConstants._class, null), null), jdbcDrivers)
                driver.remove(k, null)
            }
        }

        //////////////////// Datasource ////////////////////
        run {
            val dataSources: Struct = ConfigWebUtil.getAsStruct("dataSources", root)
            // preserveSingleQuote
            var b: Boolean = Caster.toBoolean(dataSources.get("psq", null), null)
            if (b == null) {
                b = Caster.toBoolean(dataSources.get("preserveSingleQuote", null), null)
                if (b != null) b = if (b.booleanValue()) Boolean.FALSE else Boolean.TRUE
            }
            if (b != null) root.setEL("preserveSingleQuote", b.booleanValue())
            val dataSource: Array = ConfigWebUtil.getAsArray("dataSource", dataSources)
            val keys: Array<Key?> = dataSource.keys()
            for (i in keys.indices.reversed()) {
                val k: Key? = keys[i]
                val data: Struct = Caster.toStruct(dataSource.get(k, null), null) ?: continue
                add(data, Caster.toString(data.remove(KeyConstants._name, null), null), dataSources)
                dataSource.remove(k, null)
            }
        }

        //////////////////// Debugging ////////////////////
        run {
            val debugging: Struct = ConfigWebUtil.getAsStruct("debugging", root)
            moveAsBool("debug", "debuggingEnabled", debugging, root)
            moveAsBool("debugLogOutput", "debuggingLogOutput", debugging, root)
            moveAsBool("database", "debuggingDatabase", debugging, root)
            moveAsBool("exception", "debuggingException", debugging, root)
            moveAsBool("templenabled", "debuggingTemplate", debugging, root)
            moveAsBool("dump", "debuggingDump", debugging, root)
            moveAsBool("tracing", "debuggingTracing", debugging, root)
            moveAsBool("timer", "debuggingTimer", debugging, root)
            moveAsBool("implicitAccess", "debuggingImplicitAccess", debugging, root)
            moveAsBool("queryUsage", "debuggingQueryUsage", debugging, root)
            moveAsBool("showQueryUsage", "debuggingQueryUsage", debugging, root)
            moveAsBool("thread", "debuggingThread", debugging, root)
            moveAsInt("maxRecordsLogged", "debuggingMaxRecordsLogged", debugging, root)
            val entries: Array = ConfigWebUtil.getAsArray("debugEntry", debugging)
            add(entries, "debugTemplates", root)
            rem("debugEntry", debugging)
        }

        //////////////////// Dump Writer ////////////////////
        run {
            val dumpWriters: Struct = ConfigWebUtil.getAsStruct("dumpWriters", root)
            val dumpWriter: Array = ConfigWebUtil.getAsArray("dumpWriter", dumpWriters)
            add(dumpWriter, "dumpWriters", root)
            rem("dumpWriter", dumpWriters)
        }

        //////////////////// Error ////////////////////
        run {
            val error: Struct = ConfigWebUtil.getAsStruct("error", root)
            val tmpl: String = Caster.toString(error.get("template", null), null)
            val tmpl500: String = Caster.toString(error.get("template500", null), null)
            val tmpl404: String = Caster.toString(error.get("template404", null), null)

            // generalErrorTemplate
            if (!StringUtil.isEmpty(tmpl500)) root.setEL("errorGeneralTemplate", tmpl500) else if (!StringUtil.isEmpty(tmpl)) root.setEL("errorGeneralTemplate", tmpl)

            // missingErrorTemplate
            if (!StringUtil.isEmpty(tmpl404)) root.setEL("errorMissingTemplate", tmpl404) else if (!StringUtil.isEmpty(tmpl)) root.setEL("errorMissingTemplate", tmpl)
            moveAsBool("status", "errorStatusCode", error, root)
            moveAsBool("statusCode", "errorStatusCode", error, root)
        }

        //////////////////// Extensions ////////////////////
        run {
            val extensions: Struct = ConfigWebUtil.getAsStruct("extensions", root)
            val rhextension: Array = ConfigWebUtil.getAsArray("rhextension", extensions)
            val newExtensions: Array = ArrayImpl()
            rem("enabled", extensions)
            rem("extension", extensions)

            // extensions
            val keys: Array<Key?> = rhextension.keys()
            for (i in keys.indices.reversed()) {
                val k: Key? = keys[i]
                val data: Struct = Caster.toStruct(rhextension.get(k, null), null) ?: continue
                val id: String = Caster.toString(data.get(KeyConstants._id, null), null)
                val version: String = Caster.toString(data.get(KeyConstants._version, null), null)
                val name: String = Caster.toString(data.get(KeyConstants._name, null), null)
                RHExtension.storeMetaData(config, id, version, data)
                val sct: Struct = StructImpl(Struct.TYPE_LINKED)
                sct.setEL(KeyConstants._id, id)
                sct.setEL(KeyConstants._version, version)
                if (name != null) sct.setEL(KeyConstants._name, name)
                // add(sct, Caster.toString(data.remove(KeyConstants._id, null), null), extensions);
                newExtensions.appendEL(sct)
                rhextension.remove(k, null)
            }
            root.setEL("extensions", newExtensions)

            // providers
            val rhprovider: Array = ConfigWebUtil.getAsArray("rhprovider", extensions)
            val extensionProviders: Array = ConfigWebUtil.getAsArray("extensionProviders", root)
            val it: Iterator<Object?> = rhprovider.valueIterator()
            while (it.hasNext()) {
                val data: Struct = Caster.toStruct(it.next(), null) ?: continue
                val url: String = Caster.toString(data.get(KeyConstants._url, null), null)
                if (!StringUtil.isEmpty(url)) extensionProviders.appendEL(url)
            }
            rem("rhprovider", extensions)
        }

        //////////////////// Gateway ////////////////////
        run {
            val gateways: Struct = ConfigWebUtil.getAsStruct("gateways", root)
            val gateway: Array = ConfigWebUtil.getAsArray("gateway", gateways)
            val keys: Array<Key?> = gateway.keys()
            for (i in keys.indices.reversed()) {
                val k: Key? = keys[i]
                val data: Struct = Caster.toStruct(gateway.get(k, null), null) ?: continue
                add(data, Caster.toString(data.remove(KeyConstants._id, null), null), gateways)
                gateway.remove(k, null)
            }
        }

        //////////////////// Java ////////////////////
        run {
            val java: Struct = ConfigWebUtil.getAsStruct("java", root)
            move("inspectTemplate", java, root)
            move("compileType", java, root)
        }

        //////////////////// Loggers ////////////////////
        run {
            val logging: Struct = ConfigWebUtil.getAsStruct("logging", root)
            val logger: Array = ConfigWebUtil.getAsArray("logger", logging)
            val loggers: Struct = ConfigWebUtil.getAsStruct("loggers", root)
            val keys: Array<Key?> = logger.keys()
            for (i in keys.indices.reversed()) {
                val k: Key? = keys[i]
                val data: Struct = Caster.toStruct(logger.get(k, null), null) ?: continue
                add(data, Caster.toString(data.remove(KeyConstants._name, null), null), loggers)
                logger.remove(k, null)
            }
        }

        //////////////////// Login ////////////////////
        run {
            val login: Struct = ConfigWebUtil.getAsStruct("login", root)
            moveAsBool("captcha", "loginCaptcha", login, root)
            moveAsBool("rememberme", "loginRememberme", login, root)
            moveAsInt("delay", "loginDelay", login, root)
        }

        //////////////////// Mail ////////////////////
        run {
            val mail: Struct = ConfigWebUtil.getAsStruct("mail", root)
            moveAsBool("sendPartial", "mailSendPartial", mail, root)
            moveAsBool("userSet", "mailUserSet", mail, root)
            moveAsInt("spoolInterval", "mailSpoolInterval", mail, root)
            move("defaultEncoding", "mailDefaultEncoding", mail, root)
            moveAsBool("spoolEnable", "mailSpoolEnable", mail, root)
            moveAsInt("timeout", "mailConnectionTimeout", mail, root)
            val server: Array = ConfigWebUtil.getAsArray("server", mail)
            add(server, "mailServers", root)
            rem("mail", root)
        }
        // Array _mappings = ConfigWebUtil.getAsArray("mappings", "mapping", root);

        //////////////////// Mappings ////////////////////
        run {
            val mappings: Struct = ConfigWebUtil.getAsStruct("mappings", root)
            val mapping: Array = ConfigWebUtil.getAsArray("mapping", mappings)
            val keys: Array<Key?> = mapping.keys()
            for (i in keys.indices.reversed()) {
                val k: Key? = keys[i]
                val data: Struct = Caster.toStruct(mapping.get(k, null), null) ?: continue
                add(data, Caster.toString(data.remove(KeyConstants._virtual, null), null), mappings)
                mapping.remove(k, null)
            }
        }

        //////////////////// Monitor ////////////////////
        run {
            val monitoring: Struct = ConfigWebUtil.getAsStruct("monitoring", root)
            val monitor: Array = ConfigWebUtil.getAsArray("monitor", monitoring)
            moveAsBool("enabled", "monitorEnable", monitoring, root)
            val monitors: Struct = ConfigWebUtil.getAsStruct("monitors", root)
            val keys: Array<Key?> = monitor.keys()
            for (i in keys.indices.reversed()) {
                val k: Key? = keys[i]
                val data: Struct = Caster.toStruct(monitor.get(k, null), null) ?: continue
                add(data, Caster.toString(data.remove(KeyConstants._name, null), null), monitors)
                monitor.remove(k, null)
            }
        }

        //////////////////// queue ////////////////////
        run {
            val queue: Struct = ConfigWebUtil.getAsStruct("queue", root)
            moveAsInt("enable", "requestQueueEnable", queue, root)
            moveAsInt("max", "requestQueueMax", queue, root)
            moveAsInt("timeout", "requestQueueTimeout", queue, root)
        }

        //////////////////// regex ////////////////////
        run {
            val regex: Struct = ConfigWebUtil.getAsStruct("regex", root)
            move("type", "regexType", regex, root)
        }

        //////////////////// version ////////////////////
        run { root.setEL(KeyConstants._version, "5.0") }

        //////////////////// scheduler ////////////////////
        if (config != null) {
            val configDir: Resource = config.getConfigDir()
            val scheduler: Struct = ConfigWebUtil.getAsStruct("scheduler", root)

            // set scheduler
            val schedulerDir: Resource = ConfigWebUtil.getFile(config.getRootDirectory(), ConfigWebFactory.getAttr(scheduler, "directory"), "scheduler", configDir, FileUtil.TYPE_DIR,
                    config)
            val schedulerFile: Resource = schedulerDir.getRealResource("scheduler.xml")
            if (schedulerFile.isFile()) {
                val schedulerRoot: Struct = XMLConfigReader(schedulerFile, true, ReadRule(), NameRule()).getData()
                val task: Array = ConfigWebUtil.getAsArray("schedule", "task", schedulerRoot)
                add(task, "scheduledTasks", root)
            }
            rem("scheduler", root)
        }

        //////////////////// Scope ////////////////////
        run {
            val scope: Struct = ConfigWebUtil.getAsStruct("scope", root)
            move("localMode", "localScopeMode", scope, root)
            moveAsBool("cgiReadonly", "cgiScopeReadonly", scope, root)
            move("sessionType", "sessionType", scope, root)
            move("cascading", "scopeCascading", scope, root)
            moveAsBool("cascadeToResultset", "cascadeToResultset", scope, root)
            moveAsBool("mergeUrlForm", "mergeUrlForm", scope, root)
            move("clientStorage", "clientStorage", scope, root)
            move("sessionStorage", "sessionStorage", scope, root)
            move("clientTimeout", "clientTimeout", scope, root)
            move("sessionTimeout", "sessionTimeout", scope, root)
            move("applicationTimeout", "applicationTimeout", scope, root)
            move("clientType", "clientType", scope, root)
            move("clientDirectory", "clientDirectory", scope, root)
            move("clientDirectoryMaxSize", "clientDirectoryMaxSize", scope, root)
            moveAsBool("sessionManagement", "sessionManagement", scope, root)
            moveAsBool("setclientcookies", "clientCookies", scope, root)
            moveAsBool("setdomaincookies", "domainCookies", scope, root)
            moveAsBool("clientManagement", "clientManagement", scope, root)
            if (!root.containsKey("clientTimeout")) {
                val clientMaxAge: Int = Caster.toIntValue(scope.get(KeyConstants._clientMaxAge, null), -1)
                if (clientMaxAge >= 0) root.setEL("clientTimeout", "0,0,$clientMaxAge,0")
            }
            scope.removeEL(KeyConstants._clientMaxAge)
            rem("scope", root)
        }

        //////////////////// Setting ////////////////////
        run {
            val setting: Struct = ConfigWebUtil.getAsStruct("setting", root)
            moveAsBool("suppressContent", "suppressContent", setting, root)
            move("cfmlWriter", "cfmlWriter", setting, root)
            moveAsBool("showVersion", "showVersion", setting, root)
            moveAsBool("closeConnection", "closeConnection", setting, root)
            moveAsBool("contentLength", "showContentLength", setting, root)
            moveAsBool("bufferOutput", "bufferTagBodyOutput", setting, root)
            moveAsBool("bufferingOutput", "bufferTagBodyOutput", setting, root)
            moveAsBool("allowCompression", "allowCompression", setting, root)
            val _mode: Struct = ConfigWebUtil.getAsStruct("mode", root)
            moveAsBool("develop", "developMode", _mode, root)

            // now that mode is free we can use it for the admin mode
            if (!StringUtil.isEmpty(mode)) root.setEL(KeyConstants._mode, mode)
        }

        //////////////////// startup Hooks ////////////////////
        run {
            val startup: Struct = ConfigWebUtil.getAsStruct("startup", root)
            val hook: Array = ConfigWebUtil.getAsArray("hook", startup)
            add(hook, "startupHooks", root)
            rem("startup", root)
        }

        //////////////////// System ////////////////////
        run {
            val system: Struct = ConfigWebUtil.getAsStruct("system", root)
            move("out", "systemOut", system, root)
            move("err", "systemErr", system, root)
        }

        //////////////////// Tags ////////////////////
        run {
            val tags: Struct = ConfigWebUtil.getAsStruct("tags", root)
            val _default: Array = ConfigWebUtil.getAsArray("default", tags)
            val tag: Array = ConfigWebUtil.getAsArray("tag", tags)
            add(_default, "tagDefaults", root)
            add(tag, "tags", root)
        }

        //////////////////// System ////////////////////
        run {
            val fs: Struct = ConfigWebUtil.getAsStruct("fileSystem", root)
            move("tempDirectory", "tempDirectory", fs, root)
        }

        //////////////////// Update ////////////////////
        run {
            val update: Struct = ConfigWebUtil.getAsStruct("update", root)
            move("location", "updateLocation", update, root)
            move("type", "updateType", update, root)
        }

        //////////////////// Resources ////////////////////
        run {
            val resources: Struct = ConfigWebUtil.getAsStruct("resources", root)
            val providers: Array = ConfigWebUtil.getAsArray("resourceProvider", resources)

            // Ram -> Cache (Ram is no longer supported)
            val it: Iterator<Object?> = providers.valueIterator()
            var data: Struct
            var hasRam = false
            while (it.hasNext()) {
                data = Caster.toStruct(it.next(), null)
                if (Caster.toString(data.get(KeyConstants._class, ""), "").equals("tachyon.commons.io.res.type.ram.RamResourceProvider")) {
                    hasRam = true
                    data.setEL(KeyConstants._class, "tachyon.commons.io.res.type.cache.CacheResourceProvider")
                }
                if (Caster.toString(data.get(KeyConstants._class, ""), "").equals("tachyon.commons.io.res.type.cache.CacheResourceProvider")) {
                    hasRam = true
                }
            }
            // we need the ram cache set in server, so we can go to single mode without harm
            if (isServer && !hasRam) {
                val sct: Struct = StructImpl(Struct.TYPE_LINKED)
                sct.setEL("scheme", "ram")
                sct.setEL(KeyConstants._class, "tachyon.commons.io.res.type.cache.CacheResourceProvider")
                sct.setEL(KeyConstants._arguments, "case-sensitive:true;lock-timeout:1000")
                providers.appendEL(sct)
            }
            val defaultProviders: Array = ConfigWebUtil.getAsArray("defaultResourceProvider", resources)
            add(providers, "resourceProviders", root)
            add(defaultProviders, "defaultResourceProvider", root)
            rem("resources", root)
        }

        // startupHooks
        remIfEmpty(root)

        // TODO scope?
        //////////////////// translate ////////////////////
        // allowTachyonDialect,cacheDirectory,cacheDirectoryMaxSize,componentAutoImport,componentBase,componentBaseTachyonDialect,componentDeepSearch
        // ,componentDumpTemplate, componentDataMemberDefaultAccess,componentUseVariablesScope,
        // componentLocalSearch,componentUseCachePath,componentMappings
        // classicDateParsing,cacheClasses,cacheHandlers,cfx,defaultFunctionOutput,externalizeStringGte,handleUnquotedAttributeValueAsString,
        // constants, customTagUseCachePath, customTagLocalSearch, customTagDeepSearch, customTagExtensions,
        // customTagMappings, debugTemplates,debuggingShowDump, debuggingImplicitAccess,
        // debuggingQueryUsage, debuggingMaxRecordsLogged
        // preserveSingleQuote,extensions,fileSystem, gateways,jdbcDrivers, loginCaptcha, loginRememberme,
        // loginDelay, mailSendPartial, mailUserSet, requestQueueEnable, requestQueueMax, regexType,
        // scheduledTasks<array>, localMode,
        // cgiReadonly->cgiScopeReadonly,cascadeToResultset,mergeUrlForm,clientType,clientDirectory,clientDirectoryMaxSize,
        // search,suppressContent,cfmlWriter,showVersion,showContentLength,allowCompression,startupHooks,systemErr,systemOut,tags,
        // tagDefaults,tempDirectory,updateLocation,updateType
        root = sort(root)

        // store it as Json
        val json = JSONConverter(true, CharsetUtil.UTF8, JSONDateFormat.PATTERN_CF, true, true)
        val str: String = json.serialize(null, root, SerializationSettings.SERIALIZE_AS_ROW)
        IOUtil.write(configFileNew, str, CharsetUtil.UTF8, false)
        aprint.o("DONE!")
    }

    private fun sort(root: Struct?): Struct? {
        val keys: Array<Key?> = root.keys()
        Arrays.sort(keys)
        val sct: Struct = StructImpl(Struct.TYPE_LINKED)
        var `val`: Object
        // simple values first
        for (key in keys) {
            `val` = root.get(key, null)
            if (Decision.isSimpleValue(`val`)) sct.setEL(key, `val`)
        }
        // simple values first
        for (key in keys) {
            `val` = root.get(key, null)
            if (!Decision.isSimpleValue(`val`)) sct.setEL(key, `val`)
        }
        return sct
    }

    private fun remIfEmpty(coll: Collection?) {
        val keys: Array<Key?> = coll.keys()
        var v: Object
        var sub: Collection
        for (k in keys) {
            v = coll.get(k, null)
            if (v is Collection) {
                sub = v
                if (sub.size() > 0) remIfEmpty(sub)
                if (sub.size() === 0) coll.remove(k, null)
            }
        }
    }

    private fun rem(key: String?, sct: Struct?) {
        sct.remove(KeyImpl.init(key), null)
    }

    private fun move(key: String?, from: Struct?, to: Struct?) {
        val k: Key = KeyImpl.init(key)
        val `val`: Object = from.remove(k, null)
        if (`val` != null) to.setEL(k, `val`)
    }

    private fun move(fromKey: String?, toKey: String?, from: Struct?, to: Struct?) {
        val `val`: Object = from.remove(KeyImpl.init(fromKey), null)
        if (`val` != null) to.setEL(KeyImpl.init(toKey), `val`)
    }

    private fun moveAsBool(fromKey: String?, toKey: String?, from: Struct?, to: Struct?) {
        val `val`: Object = from.remove(KeyImpl.init(fromKey), null)
        if (`val` != null && Decision.isCastableToBoolean(`val`)) to.setEL(KeyImpl.init(toKey), Caster.toBooleanValue(`val`, false))
    }

    private fun moveAsInt(fromKey: String?, toKey: String?, from: Struct?, to: Struct?) {
        val `val`: Object = from.remove(KeyImpl.init(fromKey), null)
        if (`val` != null && Decision.isCastableToNumeric(`val`)) to.setEL(KeyImpl.init(toKey), Caster.toIntValue(`val`, 0))
    }

    private fun add(fromData: Object?, toKey: String?, to: Struct?) {
        if (fromData == null) return
        to.setEL(KeyImpl.init(toKey), fromData)
    }

    private fun copy(fromKey: String?, toKey: String?, from: Struct?, to: Struct?) {
        val `val`: Object = from.get(KeyImpl.init(fromKey), null)
        if (`val` != null) to.setEL(KeyImpl.init(toKey), `val`)
    }

    fun createVirtual(data: Struct?): String? {
        val str: String = ConfigWebFactory.getAttr(data, "virtual")
        return if (!StringUtil.isEmpty(str)) str else createVirtual(ConfigWebFactory.getAttr(data, "physical"), ConfigWebFactory.getAttr(data, "archive"))
    }

    private fun createVirtual(physical: String?, archive: String?): String? {
        return "/" + MD5.getDigestAsString(physical.toString() + ":" + archive, "")
    }

    /**
     * creates the Config File, if File not exist
     *
     * @param xmlName
     * @param configFile
     * @throws IOException
     */
    @Throws(IOException::class)
    fun createConfigFile(name: String?, configFile: Resource?) {
        createFileFromResource("/resource/config/$name.json", configFile.getAbsoluteResource())
    }

    @Throws(IOException::class, PageException::class)
    private fun _loadDocument(res: Resource?): Struct? {
        val name: String = res.getName()
        // That step is not necessary anymore TODO remove
        return if (StringUtil.endsWithIgnoreCase(name, ".xml.cfm") || StringUtil.endsWithIgnoreCase(name, ".xml")) {
            try {
                ConfigWebUtil.getAsStruct("cfTachyonConfiguration", XMLConfigReader(res, true, ReadRule(), NameRule()).getData())
            } catch (e: SAXException) {
                throw Caster.toPageException(e)
            }
        } else try {
            Caster.toStruct(JSONExpressionInterpreter().interpret(null, IOUtil.toString(res, CharsetUtil.UTF8)))
        } catch (fnfe: FileNotFoundException) {
            val dir: Resource = res.getParentResource()
            val ls: Resource = dir.getRealResource("tachyon-server.xml")
            val lw: Resource = dir.getRealResource("tachyon-web.xml.cfm")
            if (ls.isFile()) _loadDocument(ls) else if (lw.isFile()) _loadDocument(lw) else throw fnfe
        }
    }

    /**
     * creates a File and his content froma a resurce
     *
     * @param resource
     * @param file
     * @param password
     * @throws IOException
     */
    @Throws(IOException::class)
    fun createFileFromResource(resource: String?, file: Resource?, password: String?) {
        LogUtil.logGlobal(ThreadLocalPageContext.getConfig(), Log.LEVEL_DEBUG, ConfigFactory::class.java.getName(), "Write file: [$file]")
        if (file.exists()) file.delete()
        val `is`: InputStream = InfoImpl::class.java.getResourceAsStream(resource)
                ?: throw IOException("File [$resource] does not exist.")
        file.createNewFile()
        IOUtil.copy(`is`, file, true)
    }

    /**
     * creates a File and his content froma a resurce
     *
     * @param resource
     * @param file
     * @throws IOException
     */
    @Throws(IOException::class)
    fun createFileFromResource(resource: String?, file: Resource?) {
        createFileFromResource(resource, file, null)
    }

    fun createFileFromResourceEL(resource: String?, file: Resource?) {
        try {
            createFileFromResource(resource, file, null)
        } catch (e: Exception) {
            LogUtil.logGlobal(ThreadLocalPageContext.getConfig(), ConfigFactory::class.java.getName(), e)
        }
    }

    fun create(srcPath: String?, names: Array<String?>?, dir: Resource?, doNew: Boolean) {
        for (i in names.indices) {
            create(srcPath, names!![i], dir, doNew)
        }
    }

    fun create(srcPath: String?, name: String?, dir: Resource?, doNew: Boolean): Resource? {
        if (!dir.exists()) dir.mkdirs()
        val f: Resource = dir.getRealResource(name)
        if (!f.exists() || doNew) createFileFromResourceEL(srcPath + name, f)
        return f
    }

    fun delete(dbDir: Resource?, names: Array<String?>?) {
        for (i in names.indices) {
            delete(dbDir, names!![i])
        }
    }

    fun delete(dbDir: Resource?, name: String?) {
        val f: Resource = dbDir.getRealResource(name)
        if (f.exists()) {
            LogUtil.logGlobal(ThreadLocalPageContext.getConfig(), Log.LEVEL_INFO, ConfigFactory::class.java.getName(), "Delete file: [$f]")
            f.delete()
        }
    }

    class UpdateInfo {
        val oldVersion: Version?
        val updateType: Int

        constructor(updateType: Int) {
            oldVersion = null
            this.updateType = updateType
        }

        constructor(oldVersion: Version?, updateType: Int) {
            this.oldVersion = oldVersion
            this.updateType = updateType
        }

        fun getUpdateTypeAsString(): String? {
            if (updateType == ConfigWebFactory.NEW_NONE) return "new-none"
            if (updateType == ConfigWebFactory.NEW_FRESH) return "new-fresh"
            if (updateType == ConfigWebFactory.NEW_FROM4) return "new-from4"
            return if (updateType == ConfigWebFactory.NEW_MINOR) "new-minor" else "unkown:$updateType"
        }

        companion object {
            val NEW_NONE: UpdateInfo? = UpdateInfo(ConfigWebFactory.NEW_NONE)
            val NEW_FRESH: UpdateInfo? = UpdateInfo(ConfigWebFactory.NEW_FRESH)
        }
    }
}