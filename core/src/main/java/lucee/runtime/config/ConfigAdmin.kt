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
package lucee.runtime.config

import java.io.ByteArrayInputStream

/**
 *
 */
class ConfigAdmin private constructor(config: ConfigPro?, password: Password?) {
    private val config: ConfigPro?
    private val root: Struct?
    private val password: Password?
    @Throws(SecurityException::class)
    private fun checkWriteAccess() {
        ConfigWebUtil.checkGeneralWriteAccess(config, password)
    }

    @Throws(SecurityException::class)
    private fun checkReadAccess() {
        ConfigWebUtil.checkGeneralReadAccess(config, password)
    }

    /**
     * @param password
     * @throws IOException
     * @throws DOMException
     * @throws ExpressionException
     */
    @Throws(SecurityException::class, IOException::class)
    fun setPassword(password: Password?) {
        checkWriteAccess()
        PasswordImpl.writeToStruct(root, password, false)
    }
    /*
	 * public void setId(String id) {
	 * 
	 * Element root=doc.getDocumentElement(); if(!StringUtil.isEmpty(root.get("id"))) return;
	 * root.setEL("id",id); try { store(config); } catch (Exception e) {} }
	 */
    /**
     * @param contextPath
     * @param password
     * @throws FunctionLibException
     * @throws TagLibException
     * @throws IOException
     * @throws ClassNotFoundException
     * @throws SAXException
     * @throws PageException
     * @throws BundleException
     */
    @Throws(PageException::class, ClassException::class, IOException::class, TagLibException::class, FunctionLibException::class, BundleException::class)
    fun removePassword(contextPath: String?) {
        checkWriteAccess()
        if (contextPath == null || contextPath.length() === 0 || config !is ConfigServerImpl) {
            // config.setPassword(password); do nothing!
        } else {
            val cw: ConfigWebImpl = config!!.getConfigWeb(contextPath)
            if (cw != null) cw.updatePassword(false, cw.getPassword(), null)
        }
    }

    @Throws(PageException::class)
    private fun addResourceProvider(scheme: String?, cd: ClassDefinition?, arguments: String?) {
        checkWriteAccess()
        val rpElements: Array = ConfigWebUtil.getAsArray("resourceProviders", root)
        // Element[] rpElements = ConfigWebFactory.getChildren(resources, "resource-provider");
        var s: String
        // update
        if (rpElements != null) {
            var rpElement: Struct
            for (i in 1..rpElements.size()) {
                rpElement = Caster.toStruct(rpElements.getE(i))
                s = Caster.toString(rpElement.get("scheme"))
                if (!StringUtil.isEmpty(s) && s.equalsIgnoreCase(scheme)) {
                    setClass(rpElement, null, "", cd)
                    rpElement.setEL("scheme", scheme)
                    rpElement.setEL("arguments", arguments)
                    return
                }
            }
        }
        // Insert
        val el: Struct = StructImpl(Struct.TYPE_LINKED)
        setClass(el, null, "", cd)
        el.setEL("scheme", scheme)
        el.setEL("arguments", arguments)
        rpElements.appendEL(el)
    }

    @Synchronized
    @Throws(PageException::class, ClassException::class, IOException::class, TagLibException::class, FunctionLibException::class, BundleException::class, ConverterException::class)
    private fun _storeAndReload() {
        _store()
        _reload()
    }

    @Synchronized
    @Throws(PageException::class, ClassException::class, IOException::class, TagLibException::class, FunctionLibException::class, BundleException::class, ConverterException::class)
    fun storeAndReload() {
        checkWriteAccess()
        _store()
        _reload()
    }

    @Synchronized
    @Throws(PageException::class, ConverterException::class, IOException::class)
    private fun _store() {
        val json = JSONConverter(true, CharsetUtil.UTF8, JSONDateFormat.PATTERN_CF, true, true)
        val str: String = json.serialize(null, root, SerializationSettings.SERIALIZE_AS_ROW)
        IOUtil.write(config.getConfigFile(), str, CharsetUtil.UTF8, false)
    }

    @Synchronized
    @Throws(PageException::class, ClassException::class, IOException::class, TagLibException::class, FunctionLibException::class, BundleException::class)
    private fun _reload() {

        // if(storeInMemoryData)XMLCaster.writeTo(doc,config.getConfigFile());
        val engine: CFMLEngine = ConfigWebUtil.getEngine(config)
        if (config is ConfigServerImpl) {
            val cs: ConfigServerImpl? = config
            ConfigServerFactory.reloadInstance(engine, cs)
            val webs: Array<ConfigWeb?> = cs!!.getConfigWebs()
            for (web in webs) {
                if (web is ConfigWebImpl) ConfigWebFactory.reloadInstance(engine, config as ConfigServerImpl?, web as ConfigWebImpl?, true) else if (web is SingleContextConfigWeb) (web as SingleContextConfigWeb?)!!.reload()
            }
        } else if (config is ConfigWebImpl) {
            val cs: ConfigServerImpl = (config as ConfigWebImpl?)!!.getConfigServerImpl()
            ConfigWebFactory.reloadInstance(engine, cs, config as ConfigWebImpl?, false)
        } else if (config is SingleContextConfigWeb) {
            val sccw: SingleContextConfigWeb? = config
            val cs: ConfigServerImpl = sccw!!.getConfigServerImpl()
            ConfigServerFactory.reloadInstance(engine, cs)
            sccw!!.reload()
            /*
			 * ConfigWeb[] webs = cs.getConfigWebs(); for (int i = 0; i < webs.length; i++) { if (webs[i]
			 * instanceof ConfigWebImpl) ConfigWebFactory.reloadInstance(engine, (ConfigServerImpl) config,
			 * (ConfigWebImpl) webs[i], true); }
			 */
        }
    }

    /*
	 * private void createAbort() { try {
	 * ConfigWebFactory.getChildByName(doc.getDocumentElement(),"cfabort",true); } catch(Throwable t)
	 * {ExceptionUtil.rethrowIfNecessary(t);} }
	 */
    @Throws(SecurityException::class)
    fun setTaskMaxThreads(maxThreads: Integer?) {
        checkWriteAccess()
        val hasAccess: Boolean = ConfigWebUtil.hasAccess(config, SecurityManager.TYPE_SETTING)
        if (!hasAccess) throw SecurityException("no access to update task settings")
        val mail: Struct? = _getRootElement("remoteClients")
        mail.setEL("maxThreads", Caster.toString(maxThreads, ""))
    }

    /**
     * sets Mail Logger to Config
     *
     * @param logFile
     * @param level
     * @throws PageException
     */
    @Throws(PageException::class)
    fun setMailLog(config: Config?, logFile: String?, level: String?) {
        val ci: ConfigPro? = config
        checkWriteAccess()
        val hasAccess: Boolean = ConfigWebUtil.hasAccess(config, SecurityManager.TYPE_MAIL)
        if (!hasAccess) throw SecurityException("no access to update mail server settings")
        ConfigWebUtil.getFile(config, config.getRootDirectory(), logFile, FileUtil.TYPE_FILE)
        val loggers: Struct = ConfigWebUtil.getAsStruct("loggers", root)
        var logger: Struct? = Caster.toStruct(loggers.get(KeyConstants._mail, null), null)
        if (logger == null) {
            logger = StructImpl(Struct.TYPE_LINKED)
            loggers.setEL(KeyConstants._mail, logger)
        }
        if ("console".equalsIgnoreCase(logFile)) {
            setClass(logger, null, "appender", ci!!.getLogEngine().appenderClassDefintion("console"))
            setClass(logger, null, "layout", ci!!.getLogEngine().layoutClassDefintion("pattern"))
        } else {
            setClass(logger, null, "appender", ci!!.getLogEngine().appenderClassDefintion("resource"))
            setClass(logger, null, "layout", ci!!.getLogEngine().layoutClassDefintion("classic"))
            logger.setEL("appenderArguments", "path:$logFile")
        }
        logger.setEL("logLevel", level)
    }

    /**
     * sets if spool is enable or not
     *
     * @param spoolEnable
     * @throws SecurityException
     */
    @Throws(SecurityException::class)
    fun setMailSpoolEnable(spoolEnable: Boolean?) {
        checkWriteAccess()
        val hasAccess: Boolean = ConfigWebUtil.hasAccess(config, SecurityManager.TYPE_MAIL)
        if (!hasAccess) throw SecurityException("no access to update mail server settings")
        root.setEL("mailSpoolEnable", Caster.toString(spoolEnable, ""))
    }

    /**
     * sets the timeout for the spooler for one job
     *
     * @param timeout
     * @throws SecurityException
     */
    @Throws(SecurityException::class)
    fun setMailTimeout(timeout: Integer?) {
        checkWriteAccess()
        val hasAccess: Boolean = ConfigWebUtil.hasAccess(config, SecurityManager.TYPE_MAIL)
        if (!hasAccess) throw SecurityException("no access to update mail server settings")
        root.setEL("mailConnectionTimeout", Caster.toString(timeout, ""))
    }

    /**
     * sets the charset for the mail
     *
     * @param charset
     * @throws SecurityException
     */
    @Throws(PageException::class)
    fun setMailDefaultCharset(charset: String?) {
        checkWriteAccess()
        val hasAccess: Boolean = ConfigWebUtil.hasAccess(config, SecurityManager.TYPE_MAIL)
        if (!hasAccess) throw SecurityException("no access to update mail server settings")
        if (!StringUtil.isEmpty(charset)) {
            try {
                IOUtil.checkEncoding(charset)
            } catch (e: IOException) {
                throw Caster.toPageException(e)
            }
        }
        root.setEL("mailDefaultEncoding", charset)
    }

    /**
     * insert or update a mailserver on system
     *
     * @param hostName
     * @param username
     * @param password
     * @param port
     * @param ssl
     * @param tls
     * @throws PageException
     */
    @Throws(PageException::class)
    fun updateMailServer(id: Int, hostName: String?, username: String?, password: String?, port: Int, tls: Boolean, ssl: Boolean, lifeTimeSpan: Long, idleTimeSpan: Long,
                         reuseConnections: Boolean) {
        var hostName = hostName
        var port = port
        checkWriteAccess()
        val hasAccess: Boolean = ConfigWebUtil.hasAccess(config, SecurityManager.TYPE_MAIL)
        if (!hasAccess) throw SecurityException("no access to update mail server settings")
        if (port < 1) port = 21
        if (hostName == null || hostName.trim().length() === 0) throw ExpressionException("Host (SMTP) cannot be an empty value")
        hostName = hostName.trim()
        val children: Array = ConfigWebUtil.getAsArray("mailServers", root)
        val checkId = id > 0

        // Update
        var server: Struct? = null
        val _hostName: String
        val _username: String
        for (i in 1..children.size()) {
            val el: Struct = Caster.toStruct(children.get(i, null), null) ?: continue
            if (checkId) {
                if (i == id) {
                    server = el
                    break
                }
            } else {
                _hostName = StringUtil.emptyIfNull(Caster.toString(el.get("smtp", null)))
                _username = StringUtil.emptyIfNull(Caster.toString(el.get("username", null)))
                if (_hostName.equalsIgnoreCase(hostName) && _username.equals(StringUtil.emptyIfNull(username))) {
                    server = el
                    break
                }
            }
        }

        // Insert
        if (server == null) {
            server = StructImpl(Struct.TYPE_LINKED)
            children.appendEL(server)
        }
        server.setEL("smtp", hostName)
        server.setEL(KeyConstants._username, username)
        server.setEL(KeyConstants._password, ConfigWebUtil.encrypt(password))
        server.setEL(KeyConstants._port, port)
        server.setEL("tls", tls)
        server.setEL("ssl", ssl)
        server.setEL("life", lifeTimeSpan)
        server.setEL("idle", idleTimeSpan)
        server.setEL("reuseConnection", reuseConnections)
    }

    /**
     * removes a mailserver from system
     *
     * @param hostName
     * @throws SecurityException
     */
    @Throws(SecurityException::class)
    fun removeMailServer(hostName: String?, username: String?) {
        checkWriteAccess()
        val children: Array = ConfigWebUtil.getAsArray("mailServers", root)
        val keys: Array<Key?> = children.keys()
        val _hostName: String
        val _username: String
        if (children.size() > 0) {
            for (i in keys.indices.reversed()) {
                val key: Key? = keys[i]
                val el: Struct = Caster.toStruct(children.get(key, null), null) ?: continue
                _hostName = Caster.toString(el.get("smtp", null), null)
                _username = Caster.toString(el.get("username", null), null)
                if (StringUtil.emptyIfNull(_hostName).equalsIgnoreCase(StringUtil.emptyIfNull(hostName))
                        && StringUtil.emptyIfNull(_username).equalsIgnoreCase(StringUtil.emptyIfNull(username))) {
                    children.removeEL(key)
                }
            }
        }
    }

    @Throws(SecurityException::class)
    fun removeLogSetting(name: String?) {
        checkWriteAccess()
        val children: Struct = ConfigWebUtil.getAsStruct("loggers", root)
        if (children.size() > 0) {
            var _name: String
            val keys: Array<Key?> = children.keys()
            for (key in keys) {
                _name = key.getString()
                if (_name != null && _name.equalsIgnoreCase(name)) {
                    children.removeEL(key)
                }
            }
        }
    }

    @Throws(ExpressionException::class, SecurityException::class)
    private fun _updateScheduledTask(task: ScheduleTask?) {
        val data: Struct? = _getScheduledTask(task.getTask(), false)
        data.setEL(KeyConstants._name, task.getTask())
        if (task.getResource() != null) data.setEL(KeyConstants._file, task.getResource().getAbsolutePath()) else if (data.containsKey(KeyConstants._file)) data.removeEL(KeyConstants._file)
        if (task.getStartDate() != null) data.setEL("startDate", task.getStartDate().castToString(null))
        if (task.getStartTime() != null) data.setEL("startTime", task.getStartTime().castToString(null))
        if (task.getEndDate() != null) data.setEL("endDate", task.getEndDate().castToString(null)) else if (data.containsKey("endDate")) rem(data, "endDate")
        if (task.getEndTime() != null) data.setEL("endTime", task.getEndTime().castToString(null)) else if (data.containsKey("endTime")) rem(data, "endTime")
        data.setEL(KeyConstants._url, task.getUrl().toExternalForm())
        data.setEL(KeyConstants._port, task.getUrl().getPort())
        data.setEL(KeyConstants._interval, task.getIntervalAsString())
        data.setEL("timeout", task.getTimeout() as Int)
        val c: Credentials = task.getCredentials()
        if (c != null) {
            if (c.getUsername() != null) data.setEL("username", c.getUsername())
            if (c.getPassword() != null) data.setEL("password", c.getPassword())
        } else {
            if (data.containsKey("username")) rem(data, "username")
            if (data.containsKey("password")) rem(data, "password")
        }
        val pd: ProxyData = task.getProxyData()
        if (pd != null) {
            if (!StringUtil.isEmpty(pd.getServer(), true)) data.setEL("proxyHost", pd.getServer()) else if (data.containsKey("proxyHost")) rem(data, "proxyHost")
            if (!StringUtil.isEmpty(pd.getUsername(), true)) data.setEL("proxyUser", pd.getUsername()) else if (data.containsKey("proxyUser")) rem(data, "proxyUser")
            if (!StringUtil.isEmpty(pd.getPassword(), true)) data.setEL("proxyPassword", pd.getPassword()) else if (data.containsKey("proxyPassword")) rem(data, "proxyPassword")
            if (pd.getPort() > 0) data.setEL("proxyPort", pd.getPort()) else if (data.containsKey("proxyPort")) rem(data, "proxyPort")
        } else {
            if (data.containsKey("proxyHost")) rem(data, "proxyHost")
            if (data.containsKey("proxyUser")) rem(data, "proxyUser")
            if (data.containsKey("proxyPassword")) rem(data, "proxyPassword")
            if (data.containsKey("proxyPort")) rem(data, "proxyPort")
        }
        data.setEL("resolveUrl", task.isResolveURL())
        data.setEL("publish", task.isPublish())
        data.setEL("hidden", (task as ScheduleTaskImpl?).isHidden())
        data.setEL("readonly", (task as ScheduleTaskImpl?).isReadonly())
        data.setEL("autoDelete", (task as ScheduleTaskImpl?).isAutoDelete())
        data.setEL("unique", (task as ScheduleTaskImpl?).unique())
        if ((task as ScheduleTaskImpl?).getUserAgent() != null) data.setEL("userAgent", (task as ScheduleTaskImpl?).getUserAgent()) else if (data.containsKey("userAgent")) rem(data, "userAgent")
    }

    /**
     * insert or update a mapping on system
     *
     * @param virtual
     * @param physical
     * @param archive
     * @param primary
     * @param trusted
     * @param toplevel
     * @throws ExpressionException
     * @throws SecurityException
     */
    @Throws(ExpressionException::class, SecurityException::class)
    fun updateMapping(virtual: String?, physical: String?, archive: String?, primary: String?, inspect: Short, toplevel: Boolean, listenerMode: Int, listenerType: Int,
                      readOnly: Boolean) {
        checkWriteAccess()
        _updateMapping(virtual, physical, archive, primary, inspect, toplevel, listenerMode, listenerType, readOnly)
    }

    @Throws(ExpressionException::class, SecurityException::class)
    private fun _updateMapping(virtual: String?, physical: String?, archive: String?, primary: String?, inspect: Short, toplevel: Boolean, listenerMode: Int, listenerType: Int,
                               readOnly: Boolean) {
        var virtual = virtual
        var physical = physical
        var archive = archive
        var primary = primary
        val hasAccess: Boolean = ConfigWebUtil.hasAccess(config, SecurityManager.TYPE_MAPPING)
        virtual = virtual.trim()
        if (physical == null) physical = "" else physical = physical.trim()
        if (archive == null) archive = "" else archive = archive.trim()
        primary = primary.trim()
        if (!hasAccess) throw SecurityException("no access to update mappings")

        // check virtual
        if (virtual == null || virtual.length() === 0) throw ExpressionException("virtual path cannot be an empty value")
        virtual = virtual.replace('\\', '/')
        if (!virtual.equals("/") && virtual.endsWith("/")) virtual = virtual.substring(0, virtual.length() - 1)
        if (virtual.charAt(0) !== '/') throw ExpressionException("virtual path must start with [/]")
        var isArchive: Boolean = primary.equalsIgnoreCase("archive")
        if (physical.length() + archive.length() === 0) throw ExpressionException("physical or archive must have a value")
        if (isArchive && archive.length() === 0) isArchive = false
        if (!isArchive && archive.length() > 0 && physical.length() === 0) isArchive = true
        var children: Struct = ConfigWebUtil.getAsStruct("mappings", root)
        var keys: Array<Key?> = children.keys()
        // Element mappings = _getRootElement("mappings");
        // Element[] children = ConfigWebFactory.getChildren(mappings, "mapping");
        var el: Struct? = null
        for (key in keys) {
            val tmp: Struct = Caster.toStruct(children.get(key, null), null) ?: continue
            var v: String? = key.getString()
            if (!StringUtil.isEmpty(v)) {
                if (!v!!.equals("/") && v.endsWith("/")) v = v.substring(0, v.length() - 1)
                if (v.equals(virtual)) {
                    el = tmp
                    el.remove("trusted")
                    break
                }
            }
        }

        // create element if necessary
        val update = el != null
        if (el == null) {
            el = StructImpl(Struct.TYPE_LINKED)
            children.setEL(virtual, el)
        }

        // physical
        if (physical.length() > 0) {
            el.setEL("physical", physical)
        } else if (el.containsKey("physical")) {
            el.remove("physical")
        }

        // archive
        if (archive.length() > 0) {
            el.setEL("archive", archive)
        } else if (el.containsKey("archive")) {
            el.remove("archive")
        }

        // primary
        el.setEL("primary", if (isArchive) "archive" else "physical")

        // listener-type
        val type: String = ConfigWebUtil.toListenerType(listenerType, null)
        if (type != null) {
            el.setEL("listenerType", type)
        } else if (el.containsKey("listenerType")) {
            el.remove("listenerType")
        }

        // listener-mode
        val mode: String = ConfigWebUtil.toListenerMode(listenerMode, null)
        if (mode != null) {
            el.setEL("listenerMode", mode)
        } else if (el.containsKey("listenerMode")) {
            el.remove("listenerMode")
        }

        // others
        el.setEL("inspectTemplate", ConfigWebUtil.inspectTemplate(inspect, ""))
        el.setEL("topLevel", Caster.toString(toplevel))
        el.setEL("readOnly", Caster.toString(readOnly))

        // set / to the end
        if (!update) {
            children = ConfigWebUtil.getAsStruct("mappings", root)
            keys = children.keys()
            for (key in keys) {
                val tmp: Struct = Caster.toStruct(children.get(key, null), null) ?: continue
                val v: String = key.getString()
                if (v != null && v.equals("/")) {
                    children.removeEL(key)
                    children.setEL(v, tmp)
                    return
                }
            }
        }
    }

    @Throws(ExpressionException::class, SecurityException::class)
    fun updateRestMapping(virtual: String?, physical: String?, _default: Boolean) {
        var virtual = virtual
        var physical = physical
        checkWriteAccess()
        val hasAccess = true // TODO ConfigWebUtil.hasAccess(config,SecurityManager.TYPE_REST);
        virtual = virtual.trim()
        physical = physical.trim()
        if (!hasAccess) throw SecurityException("no access to update REST mapping")

        // check virtual
        if (virtual == null || virtual.length() === 0) throw ExpressionException("virtual path cannot be an empty value")
        virtual = virtual.replace('\\', '/')
        if (virtual.equals("/")) throw ExpressionException("virtual path cannot be /")
        if (virtual.endsWith("/")) virtual = virtual.substring(0, virtual.length() - 1)
        if (virtual.charAt(0) !== '/') virtual = "/$virtual"
        if (physical!!.length() === 0) throw ExpressionException("physical path cannot be an empty value")
        val rest: Struct? = _getRootElement("rest")
        val children: Array = ConfigWebUtil.getAsArray("mapping", rest)

        // remove existing default
        if (_default) {
            for (i in 1..children.size()) {
                val tmp: Struct = Caster.toStruct(children.get(i, null), null) ?: continue
                if (Caster.toBooleanValue(tmp.get("default", null), false)) tmp.setEL("default", "false")
            }
        }

        // Update
        val v: String
        var el: Struct? = null
        for (i in 1..children.size()) {
            val tmp: Struct = Caster.toStruct(children.get(i, null), null) ?: continue
            v = ConfigWebUtil.getAsString("virtual", tmp, null)
            if (v != null && v.equals(virtual)) {
                el = tmp
            }
        }

        // Insert
        if (el == null) {
            el = StructImpl(Struct.TYPE_LINKED)
            children.appendEL(el)
        }
        el.setEL("virtual", virtual)
        el.setEL("physical", physical)
        el.setEL("default", Caster.toString(_default))
    }

    /**
     * delete a mapping on system
     *
     * @param virtual
     * @throws ExpressionException
     * @throws SecurityException
     */
    @Throws(ExpressionException::class, SecurityException::class)
    fun removeMapping(virtual: String?) {
        checkWriteAccess()
        _removeMapping(virtual)
    }

    @Throws(ExpressionException::class)
    fun _removeMapping(virtual: String?) {
        // check parameters
        var virtual = virtual
        if (virtual == null || virtual.length() === 0) throw ExpressionException("virtual path cannot be an empty value")
        virtual = virtual.replace('\\', '/')
        if (!virtual.equals("/") && virtual.endsWith("/")) virtual = virtual.substring(0, virtual.length() - 1)
        if (virtual.charAt(0) !== '/') throw ExpressionException("virtual path must start with [/]")
        val children: Struct = ConfigWebUtil.getAsStruct("mappings", root)
        val keys: Array<Key?> = children.keys()
        for (key in keys) {
            val tmp: Struct = Caster.toStruct(children.get(key, null), null) ?: continue
            var v: String? = key.getString()
            if (v != null) {
                if (!v.equals("/") && v.endsWith("/")) v = v.substring(0, v.length() - 1)
                if (v != null && v.equals(virtual)) {
                    children.removeEL(key)
                }
            }
        }
    }

    @Throws(ExpressionException::class, SecurityException::class)
    fun removeRestMapping(virtual: String?) {
        var virtual = virtual
        checkWriteAccess()
        // check parameters
        if (virtual == null || virtual.length() === 0) throw ExpressionException("virtual path cannot be an empty value")
        virtual = virtual.replace('\\', '/')
        if (virtual.equals("/")) throw ExpressionException("virtual path cannot be /")
        if (virtual.endsWith("/")) virtual = virtual.substring(0, virtual.length() - 1)
        if (virtual.charAt(0) !== '/') virtual = "/$virtual"
        val children: Array = ConfigWebUtil.getAsArray("rest", "mapping", root)
        val keys: Array<Key?> = children.keys()
        for (i in keys.indices.reversed()) {
            val key: Key? = keys[i]
            val tmp: Struct = Caster.toStruct(children.get(key, null), null) ?: continue
            var v: String? = ConfigWebUtil.getAsString("virtual", tmp, null)
            if (v != null) {
                if (!v.equals("/") && v.endsWith("/")) v = v.substring(0, v.length() - 1)
                if (v != null && v.equals(virtual)) {
                    children.removeEL(key)
                }
            }
        }
    }

    /**
     * delete a customtagmapping on system
     *
     * @param virtual
     * @throws SecurityException
     */
    @Throws(SecurityException::class)
    fun removeCustomTag(virtual: String?) {
        checkWriteAccess()
        val mappings: Array = ConfigWebUtil.getAsArray("customTagMappings", root)
        val keys: Array<Key?> = mappings.keys()
        var data: Struct
        var v: String?
        for (i in keys.indices.reversed()) {
            val key: Key? = keys[i]
            data = Caster.toStruct(mappings.get(key, null), null)
            if (data == null) continue
            v = createVirtual(data)
            if (virtual!!.equals(v)) {
                mappings.removeEL(key)
            }
        }
    }

    @Throws(SecurityException::class, ExpressionException::class)
    private fun _removeScheduledTask(name: String?) {
        val tasks: Array = ConfigWebUtil.getAsArray("scheduledTasks", root)
        val keys: Array<Key?> = tasks.keys()
        var data: Struct
        var n: String
        var exist = false
        for (i in keys.indices.reversed()) {
            val key: Key? = keys[i]
            data = Caster.toStruct(tasks.get(key, null), null)
            if (data == null) continue
            n = Caster.toString(data.get(KeyConstants._name, null), null)
            if (name!!.equals(n)) {
                exist = true
                tasks.removeEL(key)
            }
        }
        if (!exist) throw ExpressionException("can't delete schedule task [ $name ], task doesn't exist")
    }

    @Throws(SecurityException::class)
    fun removeComponentMapping(virtual: String?) {
        checkWriteAccess()
        val mappings: Array = ConfigWebUtil.getAsArray("componentMappings", root)
        val keys: Array<Key?> = mappings.keys()
        var data: Struct
        var v: String?
        for (i in keys.indices.reversed()) {
            val key: Key? = keys[i]
            data = Caster.toStruct(mappings.get(key, null), null)
            if (data == null) continue
            v = createVirtual(data)
            if (virtual!!.equals(v)) {
                mappings.removeEL(key)
            }
        }
    }

    /**
     * insert or update a mapping for Custom Tag
     *
     * @param virtual
     * @param physical
     * @param archive
     * @param primary
     * @param trusted
     * @throws ExpressionException
     * @throws SecurityException
     */
    @Throws(ExpressionException::class, SecurityException::class)
    fun updateCustomTag(virtual: String?, physical: String?, archive: String?, primary: String?, inspect: Short) {
        checkWriteAccess()
        _updateCustomTag(virtual, physical, archive, primary, inspect)
    }

    @Throws(ExpressionException::class, SecurityException::class)
    private fun _updateCustomTag(virtual: String?, physical: String?, archive: String?, primary: String?, inspect: Short) {
        var virtual = virtual
        var physical = physical
        var archive = archive
        val hasAccess: Boolean = ConfigWebUtil.hasAccess(config, SecurityManager.TYPE_CUSTOM_TAG)
        if (!hasAccess) throw SecurityException("no access to change custom tag settings")
        if (physical == null) physical = ""
        if (archive == null) archive = ""

        // virtual="/custom-tag";
        if (StringUtil.isEmpty(virtual)) virtual = createVirtual(physical, archive)
        val isArchive: Boolean = primary.equalsIgnoreCase("archive")
        if (isArchive && archive.length() === 0) {
            throw ExpressionException("archive must have a value when primary has value archive")
        }
        if (!isArchive && physical.length() === 0) {
            throw ExpressionException("physical must have a value when primary has value physical")
        }
        val mappings: Array = ConfigWebUtil.getAsArray("customTagMappings", root)
        val keys: Array<Key?> = mappings.keys()
        // Update
        val v: String?
        // Element[] children = ConfigWebFactory.getChildren(mappings, "mapping");
        for (i in keys.indices.reversed()) {
            val key: Key? = keys[i]
            val el: Struct = Caster.toStruct(mappings.get(key, null), null) ?: continue
            v = createVirtual(el)
            if (virtual!!.equals(v)) {
                el.setEL("virtual", v)
                el.setEL("physical", physical)
                el.setEL("archive", archive)
                el.setEL("primary", if (primary.equalsIgnoreCase("archive")) "archive" else "physical")
                el.setEL("inspectTemplate", ConfigWebUtil.inspectTemplate(inspect, ""))
                el.removeEL(KeyImpl.init("trusted"))
                return
            }
        }

        // Insert
        val el: Struct = StructImpl(Struct.TYPE_LINKED)
        mappings.appendEL(el)
        if (physical.length() > 0) el.setEL("physical", physical)
        if (archive.length() > 0) el.setEL("archive", archive)
        el.setEL("primary", if (primary.equalsIgnoreCase("archive")) "archive" else "physical")
        el.setEL("inspectTemplate", ConfigWebUtil.inspectTemplate(inspect, ""))
        el.setEL("virtual", if (StringUtil.isEmpty(virtual)) createVirtual(el) else virtual)
    }

    @Throws(ExpressionException::class)
    private fun _getScheduledTask(name: String?, throwWhenNotExist: Boolean): Struct? {
        val scheduledTasks: Array = ConfigWebUtil.getAsArray("scheduledTasks", root)
        val keys: Array<Key?> = scheduledTasks.keys()
        // Update
        var data: Struct? = null
        for (i in keys.indices.reversed()) {
            val key: Key? = keys[i]
            val tmp: Struct = Caster.toStruct(scheduledTasks.get(key, null), null) ?: continue
            val n: String = Caster.toString(tmp.get(KeyConstants._name, null), null)
            if (name.equalsIgnoreCase(n)) {
                data = tmp
                break
            }
        }

        // Insert
        if (data == null) {
            if (throwWhenNotExist) throw ExpressionException("scheduled task [$name] does not exist!")
            data = StructImpl(Struct.TYPE_LINKED)
            scheduledTasks.appendEL(data)
        }
        return data
    }

    private fun _getScheduledTasks(): Array? {
        return ConfigWebUtil.getAsArray("scheduledTasks", root)
    }

    @Throws(ExpressionException::class, SecurityException::class)
    fun updateComponentMapping(virtual: String?, physical: String?, archive: String?, primary: String?, inspect: Short) {
        checkWriteAccess()
        _updateComponentMapping(virtual, physical, archive, primary, inspect)
    }

    @Throws(ExpressionException::class)
    private fun _updateComponentMapping(virtual: String?, physical: String?, archive: String?, primary: String?, inspect: Short) {
        var physical = physical
        var archive = archive
        var primary = primary
        primary = if (primary.equalsIgnoreCase("archive")) "archive" else "physical"
        if (physical == null) physical = "" else physical = physical.trim()
        if (archive == null) archive = "" else archive = archive.trim()
        val isArchive: Boolean = primary.equalsIgnoreCase("archive")
        if (isArchive && archive.length() === 0) {
            throw ExpressionException("archive must have a value when primary has value archive")
        }
        if (!isArchive && physical.length() === 0) {
            throw ExpressionException("physical must have a value when primary has value physical")
        }
        val componentMappings: Array = ConfigWebUtil.getAsArray("componentMappings", root)
        val keys: Array<Key?> = componentMappings.keys()
        val el: Struct?

        // Update
        var v: String?
        var data: Struct
        for (i in keys.indices.reversed()) {
            val key: Key? = keys[i]
            data = Caster.toStruct(componentMappings.get(key, null), null)
            if (data == null) continue
            v = createVirtual(data)
            if (virtual!!.equals(v)) {
                data.setEL("virtual", v)
                data.setEL("physical", physical)
                data.setEL("archive", archive)
                data.setEL("primary", if (primary.equalsIgnoreCase("archive")) "archive" else "physical")
                data.setEL("inspectTemplate", ConfigWebUtil.inspectTemplate(inspect, ""))
                data.removeEL(KeyImpl.init("trusted"))
                return
            }
        }

        // Insert
        el = StructImpl(Struct.TYPE_LINKED)
        componentMappings.appendEL(el)
        if (physical.length() > 0) el.setEL("physical", physical)
        if (archive.length() > 0) el.setEL("archive", archive)
        el.setEL("primary", if (primary.equalsIgnoreCase("archive")) "archive" else "physical")
        el.setEL("inspectTemplate", ConfigWebUtil.inspectTemplate(inspect, ""))
        el.setEL("virtual", if (StringUtil.isEmpty(virtual)) createVirtual(el) else virtual)
    }

    @Throws(IOException::class, BundleException::class)
    fun updateJar(resJar: Resource?) {
        updateJar(config, resJar, true)
    }

    /**
     * insert or update a Java CFX Tag
     *
     * @param name
     * @param strClass
     * @throws PageException
     */
    @Throws(PageException::class)
    fun updateJavaCFX(name: String?, cd: ClassDefinition?) {
        checkWriteAccess()
        val hasAccess: Boolean = ConfigWebUtil.hasAccess(config, SecurityManager.TYPE_CFX_SETTING)
        if (!hasAccess) throw SecurityException("no access to change cfx settings")
        if (name == null || name.length() === 0) throw ExpressionException("class name can't be an empty value")
        val cfxs: Struct = ConfigWebUtil.getAsStruct("cfx", root)
        val keys: Array<Key?> = cfxs.keys()
        // Update
        for (key in keys) {
            val n: String = key.getString()
            if (n != null && n.equalsIgnoreCase(name)) {
                val data: Struct = Caster.toStruct(cfxs.get(key, null), null) ?: continue
                if (!"java".equalsIgnoreCase(ConfigWebUtil.getAsString("type", data, ""))) throw ExpressionException("there is already a c++ cfx tag with this name")
                setClass(data, CustomTag::class.java, "", cd)
                data.setEL("type", "java")
                return
            }
        }

        // Insert
        val el: Struct = StructImpl(Struct.TYPE_LINKED)
        cfxs.setEL(name, el)
        setClass(el, CustomTag::class.java, "", cd)
        el.setEL("type", "java")
    }

    @Throws(PageException::class)
    fun verifyCFX(name: String?) {
        val pool: CFXTagPool = config.getCFXTagPool()
        var ct: CustomTag? = null
        ct = try {
            pool.getCustomTag(name)
        } catch (e: CFXTagException) {
            throw Caster.toPageException(e)
        } finally {
            if (ct != null) pool.releaseCustomTag(ct)
        }
    }

    @Throws(PageException::class)
    fun verifyJavaCFX(name: String?, cd: ClassDefinition?) {
        var name = name
        try {
            val clazz: Class = cd.getClazz()
            if (!Reflector.isInstaneOf(clazz, CustomTag::class.java, false)) throw ExpressionException("class [" + cd + "] must implement interface [" + CustomTag::class.java.getName() + "]")
        } catch (e: ClassException) {
            throw Caster.toPageException(e)
        } catch (e: BundleException) {
            throw Caster.toPageException(e)
        }
        if (StringUtil.startsWithIgnoreCase(name, "cfx_")) name = name.substring(4)
        if (StringUtil.isEmpty(name)) throw ExpressionException("class name can't be an empty value")
    }

    /**
     * remove a CFX Tag
     *
     * @param name
     * @throws ExpressionException
     * @throws SecurityException
     */
    @Throws(ExpressionException::class, SecurityException::class)
    fun removeCFX(name: String?) {
        checkWriteAccess()
        // check parameters
        if (name == null || name.length() === 0) throw ExpressionException("name for CFX Tag can be an empty value")
        val cfxs: Struct = ConfigWebUtil.getAsStruct("cfx", root)
        val keys: Array<Key?> = cfxs.keys()
        for (key in keys) {
            val n: String = key.getString()
            if (n != null && n.equalsIgnoreCase(name)) {
                cfxs.removeEL(key)
            }
        }
    }

    /**
     * update or insert new database connection
     *
     * @param name
     * @param clazzName
     * @param dsn
     * @param username
     * @param password
     * @param host
     * @param database
     * @param port
     * @param connectionLimit
     * @param connectionTimeout
     * @param blob
     * @param clob
     * @param allow
     * @param storage
     * @param custom
     * @throws PageException
     */
    @Throws(PageException::class)
    fun updateDataSource(id: String?, name: String?, newName: String?, cd: ClassDefinition?, dsn: String?, username: String?, password: String?, host: String?, database: String?, port: Int,
                         connectionLimit: Int, idleTimeout: Int, liveTimeout: Int, metaCacheTimeout: Long, blob: Boolean, clob: Boolean, allow: Int, validate: Boolean, storage: Boolean, timezone: String?,
                         custom: Struct?, dbdriver: String?, paramSyntax: ParamSyntax?, literalTimestampWithTSOffset: Boolean, alwaysSetTimeout: Boolean, requestExclusive: Boolean,
                         alwaysResetConnections: Boolean) {
        var password = password
        checkWriteAccess()
        val sm: SecurityManager = config.getSecurityManager()
        val access: Short = sm.getAccess(SecurityManager.TYPE_DATASOURCE)
        var hasAccess = true
        var hasInsertAccess = true
        var maxLength = 0
        if (access == SecurityManager.VALUE_YES) hasAccess = true else if (access == SecurityManager.VALUE_NO) hasAccess = false else if (access >= SecurityManager.VALUE_1 && access <= SecurityManager.VALUE_10) {
            val existingLength = getDatasourceLength(config)
            maxLength = access - SecurityManager.NUMBER_OFFSET
            hasInsertAccess = maxLength > existingLength
        }
        if (!hasAccess) throw SecurityException("no access to update datasource connections")

        // check parameters
        if (name == null || name.length() === 0) throw ExpressionException("name can't be an empty value")
        val children: Struct = ConfigWebUtil.getAsStruct("dataSources", root)
        val keys: Array<Key?> = children.keys()
        for (key in keys) {
            if (key.getString().equalsIgnoreCase(name)) {
                val tmp: Struct = Caster.toStruct(children.get(key, null), null) ?: continue
                val el: Struct = tmp
                if (password.equalsIgnoreCase("****************")) password = ConfigWebUtil.getAsString("password", el, null)
                if (!StringUtil.isEmpty(newName) && !newName!!.equals(name)) el.setEL("name", newName)
                setClass(el, null, "", cd)
                if (!StringUtil.isEmpty(id)) el.setEL(KeyConstants._id, id) else if (el.containsKey(KeyConstants._id)) el.removeEL(KeyConstants._id)
                el.setEL("dsn", dsn)
                el.setEL("username", username)
                el.setEL("password", ConfigWebUtil.encrypt(password))
                el.setEL("host", host)
                if (!StringUtil.isEmpty(timezone)) el.setEL(KeyConstants._timezone, timezone) else if (el.containsKey(KeyConstants._timezone)) el.removeEL(KeyConstants._timezone)
                el.setEL("database", database)
                el.setEL("port", Caster.toString(port))
                el.setEL("connectionLimit", Caster.toString(connectionLimit))
                el.setEL("connectionTimeout", Caster.toString(idleTimeout))
                el.setEL("liveTimeout", Caster.toString(liveTimeout))
                el.setEL("metaCacheTimeout", Caster.toString(metaCacheTimeout))
                el.setEL("blob", Caster.toString(blob))
                el.setEL("clob", Caster.toString(clob))
                el.setEL("allow", Caster.toString(allow))
                el.setEL("validate", Caster.toString(validate))
                el.setEL("storage", Caster.toString(storage))
                el.setEL("custom", toStringURLStyle(custom))
                if (!StringUtil.isEmpty(dbdriver)) el.setEL("dbdriver", Caster.toString(dbdriver))

                // Param Syntax
                el.setEL("paramDelimiter", paramSyntax.delimiter)
                el.setEL("paramLeadingDelimiter", paramSyntax.leadingDelimiter)
                el.setEL("paramSeparator", paramSyntax.separator)
                if (literalTimestampWithTSOffset) el.setEL("literalTimestampWithTSOffset", "true") else if (el.containsKey("literalTimestampWithTSOffset")) el.removeEL(KeyImpl.init("literalTimestampWithTSOffset"))
                if (alwaysSetTimeout) el.setEL("alwaysSetTimeout", "true") else if (el.containsKey("alwaysSetTimeout")) el.removeEL(KeyImpl.init("alwaysSetTimeout"))
                if (requestExclusive) el.setEL("requestExclusive", "true") else if (el.containsKey("requestExclusive")) el.removeEL(KeyImpl.init("requestExclusive"))
                if (alwaysResetConnections) el.setEL("alwaysResetConnections", "true") else if (el.containsKey("alwaysResetConnections")) el.removeEL(KeyImpl.init("alwaysResetConnections"))
                return
            }
        }
        if (!hasInsertAccess) throw SecurityException("Unable to add a datasource connection, the maximum count of [" + maxLength + "] datasources has been reached. "
                + " This can be configured in the Server Admin, under Security, Access")

        // Insert
        val el: Struct = StructImpl(Struct.TYPE_LINKED)
        children.setEL(if (!StringUtil.isEmpty(newName)) newName else name, el)
        setClass(el, null, "", cd)
        el.setEL("dsn", dsn)
        if (!StringUtil.isEmpty(id)) el.setEL(KeyConstants._id, id) else if (el.containsKey(KeyConstants._id)) el.removeEL(KeyConstants._id)
        el.setEL(KeyConstants._username, username)
        el.setEL(KeyConstants._password, ConfigWebUtil.encrypt(password))
        el.setEL("host", host)
        if (!StringUtil.isEmpty(timezone)) el.setEL("timezone", timezone)
        el.setEL("database", database)
        if (port > -1) el.setEL("port", Caster.toString(port))
        el.setEL("connectionLimit", Caster.toString(connectionLimit))
        if (idleTimeout > -1) el.setEL("connectionTimeout", Caster.toString(idleTimeout))
        if (liveTimeout > -1) el.setEL("liveTimeout", Caster.toString(liveTimeout))
        if (metaCacheTimeout > -1) el.setEL("metaCacheTimeout", Caster.toString(metaCacheTimeout))
        el.setEL("blob", Caster.toString(blob))
        el.setEL("clob", Caster.toString(clob))
        el.setEL("validate", Caster.toString(validate))
        el.setEL("storage", Caster.toString(storage))
        if (allow > -1) el.setEL("allow", Caster.toString(allow))
        el.setEL("custom", toStringURLStyle(custom))
        if (!StringUtil.isEmpty(dbdriver)) el.setEL("dbdriver", Caster.toString(dbdriver))

        // Param Syntax
        el.setEL("paramDelimiter", paramSyntax.delimiter)
        el.setEL("paramLeadingDelimiter", paramSyntax.leadingDelimiter)
        el.setEL("paramSeparator", paramSyntax.separator)
        if (literalTimestampWithTSOffset) el.setEL("literalTimestampWithTSOffset", "true")
        if (alwaysSetTimeout) el.setEL("alwaysSetTimeout", "true")
        if (requestExclusive) el.setEL("requestExclusive", "true")
        if (alwaysResetConnections) el.setEL("alwaysResetConnections", "true")
    }

    @Throws(PageException::class)
    private fun _removeJDBCDriver(cd: ClassDefinition?) {
        if (!cd.isBundle()) throw ApplicationException("missing bundle name")
        val children: Struct = ConfigWebUtil.getAsStruct("jdbcDrivers", root)
        val keys: Array<Key?> = children.keys()
        // Remove
        for (key in keys) {
            if (key.getString().equalsIgnoreCase(cd.getClassName())) {
                children.removeEL(key)
                break
            }
        }

        // now unload (maybe not necessary)
        if (cd.isBundle()) {
            val bl: Bundle = OSGiUtil.getBundleLoaded(cd.getName(), cd.getVersion(), null)
            if (bl != null) {
                try {
                    OSGiUtil.uninstall(bl)
                } catch (e: BundleException) {
                }
            }
        }
    }

    @Throws(PageException::class)
    private fun _removeStartupHook(cd: ClassDefinition?) {
        if (!cd.isBundle()) throw ApplicationException("missing bundle name")
        val children: Array = ConfigWebUtil.getAsArray("startupHooks", root)
        val keys: Array<Key?> = children.keys()
        // Remove
        for (i in keys.indices.reversed()) {
            val key: Key? = keys[i]
            val tmp: Struct = Caster.toStruct(children.get(key, null), null) ?: continue
            val n: String = ConfigWebUtil.getAsString("class", tmp, "")
            if (n.equalsIgnoreCase(cd.getClassName())) {
                children.removeEL(key)
                break
            }
        }

        // now unload (maybe not necessary)
        if (cd.isBundle()) {
            unloadStartupIfNecessary(config, cd, true)
            val bl: Bundle = OSGiUtil.getBundleLoaded(cd.getName(), cd.getVersion(), null)
            if (bl != null) {
                try {
                    OSGiUtil.uninstall(bl)
                } catch (e: BundleException) {
                }
            }
        }
    }

    private fun unloadStartupIfNecessary(config: ConfigPro?, cd: ClassDefinition<*>?, force: Boolean) {
        val startup: ConfigBase.Startup = config!!.getStartups()!!.get(cd.getClassName()) ?: return
        if (startup.cd.equals(cd) && !force) return
        try {
            val fin: Method = Reflector.getMethod(startup.instance.getClass(), "finalize", arrayOfNulls<Class?>(0), null)
            if (fin != null) {
                fin.invoke(startup.instance, arrayOfNulls<Object?>(0))
            }
            config!!.getStartups().remove(cd.getClassName())
        } catch (e: Exception) {
        }
    }

    @Throws(PageException::class)
    fun updateJDBCDriver(label: String?, id: String?, cd: ClassDefinition?) {
        checkWriteAccess()
        _updateJDBCDriver(label, id, cd)
    }

    @Throws(PageException::class)
    private fun _updateJDBCDriver(label: String?, id: String?, cd: ClassDefinition?) {

        // check if label exists
        if (StringUtil.isEmpty(label)) throw ApplicationException("missing label for jdbc driver [" + cd.getClassName().toString() + "]")
        // check if it is a bundle
        if (!cd.isBundle()) throw ApplicationException("missing bundle name for [$label]")
        val children: Struct = ConfigWebUtil.getAsStruct("jdbcDrivers", root)
        val keys: Array<Key?> = children.keys()
        // Update
        var child: Struct? = null
        for (key in keys) {
            val n: String = key.getString()
            if (key.getString().equalsIgnoreCase(cd.getClassName())) {
                val tmp: Struct = Caster.toStruct(children.get(key, null), null) ?: continue
                child = tmp
                break
            }
        }

        // Insert
        if (child == null) {
            child = StructImpl(Struct.TYPE_LINKED)
            children.setEL(cd.getClassName(), child)
        }
        child.setEL("label", label)
        if (!StringUtil.isEmpty(id)) child.setEL(KeyConstants._id, id) else child.removeEL(KeyConstants._id)
        // make sure the class exists
        setClass(child, null, "", cd)
        child.removeEL(KeyConstants._class)

        // now unload again, JDBC driver can be loaded when necessary
        if (cd.isBundle()) {
            val bl: Bundle = OSGiUtil.getBundleLoaded(cd.getName(), cd.getVersion(), null)
            if (bl != null) {
                try {
                    OSGiUtil.uninstall(bl)
                } catch (e: BundleException) {
                }
            }
        }
    }

    @Throws(PageException::class)
    private fun _updateStartupHook(cd: ClassDefinition?) {
        unloadStartupIfNecessary(config, cd, false)
        // check if it is a bundle
        if (!cd.isBundle()) throw ApplicationException("missing bundle info")
        val children: Array = ConfigWebUtil.getAsArray("startupHooks", root)

        // Update
        var child: Struct? = null
        for (i in 1..children.size()) {
            val tmp: Struct = Caster.toStruct(children.get(i, null), null) ?: continue
            val n: String = ConfigWebUtil.getAsString("class", tmp, null)
            if (n.equalsIgnoreCase(cd.getClassName())) {
                child = tmp
                break
            }
        }

        // Insert
        if (child == null) {
            child = StructImpl(Struct.TYPE_LINKED)
            children.appendEL(child)
        }

        // make sure the class exists
        setClass(child, null, "", cd)

        // now unload again, JDBC driver can be loaded when necessary
        if (cd.isBundle()) {
            val bl: Bundle = OSGiUtil.getBundleLoaded(cd.getName(), cd.getVersion(), null)
            if (bl != null) {
                try {
                    OSGiUtil.uninstall(bl)
                } catch (e: BundleException) {
                }
            }
        }
    }

    @Throws(PageException::class)
    fun updateGatewayEntry(id: String?, cd: ClassDefinition?, componentPath: String?, listenerCfcPath: String?, startupMode: Int, custom: Struct?, readOnly: Boolean) {
        checkWriteAccess()
        val hasAccess: Boolean = ConfigWebUtil.hasAccess(config, SecurityManagerImpl.TYPE_GATEWAY)
        if (!hasAccess) throw SecurityException("no access to update gateway entry")
        _updateGatewayEntry(id, cd, componentPath, listenerCfcPath, startupMode, custom, readOnly)
    }

    @Throws(PageException::class)
    fun _updateGatewayEntry(id: String?, cd: ClassDefinition?, componentPath: String?, listenerCfcPath: String?, startupMode: Int, custom: Struct?, readOnly: Boolean) {

        // check parameters
        var id = id
        id = id.trim()
        if (StringUtil.isEmpty(id)) throw ExpressionException("id can't be an empty value")
        if ((cd == null || StringUtil.isEmpty(cd.getClassName())) && StringUtil.isEmpty(componentPath)) throw ExpressionException("you must define className or componentPath")
        val children: Struct = ConfigWebUtil.getAsStruct("gateways", root)
        val keys: Array<Key?> = children.keys()

        // Update
        for (key in keys) {
            val el: Struct = Caster.toStruct(children.get(key, null), null) ?: continue
            val n: String = key.getString()
            if (n.equalsIgnoreCase(id)) {
                setClass(el, null, "", cd)
                el.setEL("cfcPath", componentPath)
                el.setEL("listenerCFCPath", listenerCfcPath)
                el.setEL("startupMode", GatewayEntryImpl.toStartup(startupMode, "automatic"))
                el.setEL("custom", toStringURLStyle(custom))
                el.setEL("readOnly", Caster.toString(readOnly))
                return
            }
        }

        // Insert
        val el: Struct = StructImpl(Struct.TYPE_LINKED)
        children.setEL(id, el)
        el.setEL("cfcPath", componentPath)
        el.setEL("listenerCFCPath", listenerCfcPath)
        el.setEL("startupMode", GatewayEntryImpl.toStartup(startupMode, "automatic"))
        setClass(el, null, "", cd)
        el.setEL("custom", toStringURLStyle(custom))
        el.setEL("readOnly", Caster.toString(readOnly))
    }

    private fun _removeSearchEngine() {
        val orm: Struct? = _getRootElement("search")
        removeClass(orm, "engine")
    }

    @Throws(PageException::class)
    fun updateSearchEngine(cd: ClassDefinition?) {
        checkWriteAccess()
        _updateSearchEngine(cd)
    }

    @Throws(PageException::class)
    private fun _updateSearchEngine(cd: ClassDefinition?) {
        val orm: Struct? = _getRootElement("search")
        setClass(orm, SearchEngine::class.java, "engine", cd)
    }

    @Throws(SecurityException::class)
    fun removeSearchEngine() {
        checkWriteAccess()
        val orm: Struct? = _getRootElement("search")
        removeClass(orm, "engine")
    }

    private fun _removeORMEngine() {
        val orm: Struct? = _getRootElement("orm")
        removeClass(orm, "engine")
        removeClass(orm, "") // in the beginning we had no prefix
    }

    private fun _removeWebserviceHandler() {
        val orm: Struct? = _getRootElement("webservice")
        removeClass(orm, "")
    }

    @Throws(SecurityException::class)
    fun removeORMEngine() {
        checkWriteAccess()
        _removeORMEngine()
    }

    @Throws(PageException::class)
    fun updateORMEngine(cd: ClassDefinition?) {
        checkWriteAccess()
        _updateORMEngine(cd)
    }

    @Throws(PageException::class)
    private fun _updateORMEngine(cd: ClassDefinition?) {
        val orm: Struct? = _getRootElement("orm")
        removeClass(orm, "") // in the beginning we had no prefix
        setClass(orm, ORMEngine::class.java, "engine", cd)
    }

    @Throws(PageException::class)
    private fun _updateWebserviceHandler(cd: ClassDefinition?) {
        val orm: Struct? = _getRootElement("webservice")
        setClass(orm, null, "", cd)
    }

    @Throws(PageException::class)
    fun updateCacheConnection(name: String?, cd: ClassDefinition?, _default: Int, custom: Struct?, readOnly: Boolean, storage: Boolean) {
        var name = name
        checkWriteAccess()
        val hasAccess: Boolean = ConfigWebUtil.hasAccess(config, SecurityManagerImpl.TYPE_CACHE)
        if (!hasAccess) throw SecurityException("no access to update cache connection")

        // check parameters
        name = name.trim()
        if (StringUtil.isEmpty(name)) throw ExpressionException("name can't be an empty value")
        // else if(name.equals("template") || name.equals("object"))
        // throw new ExpressionException("name ["+name+"] is not allowed for a cache connection, the
        // following names are reserved words [object,template]");
        try {
            val clazz: Class
            clazz = if (cd.getClassName() != null && cd.getClassName().endsWith(".EHCacheLite")) ClassUtil.loadClass(config.getClassLoader(), "org.lucee.extension.cache.eh.EHCache") else ClassUtil.loadClass(config.getClassLoader(), cd.getClassName())
            if (!Reflector.isInstaneOf(clazz, Cache::class.java, false)) throw ExpressionException("class [" + clazz.getName().toString() + "] is not of type [" + Cache::class.java.getName().toString() + "]")
        } catch (e: ClassException) {
            throw ExpressionException(e.getMessage())
        }
        if (name.equalsIgnoreCase(Caster.toString(root.get("defaultTemplate", null), null))) rem(root, "defaultTemplate")
        if (name.equalsIgnoreCase(Caster.toString(root.get("defaultObject", null), null))) rem(root, "defaultObject")
        if (name.equalsIgnoreCase(Caster.toString(root.get("defaultQuery", null), null))) rem(root, "defaultQuery")
        if (name.equalsIgnoreCase(Caster.toString(root.get("defaultResource", null), null))) rem(root, "defaultResource")
        if (name.equalsIgnoreCase(Caster.toString(root.get("defaultFunction", null), null))) rem(root, "defaultFunction")
        if (name.equalsIgnoreCase(Caster.toString(root.get("defaultInclude", null), null))) rem(root, "defaultInclude")
        if (_default == ConfigPro.CACHE_TYPE_OBJECT) {
            root.setEL("defaultObject", name)
        } else if (_default == ConfigPro.CACHE_TYPE_TEMPLATE) {
            root.setEL("defaultTemplate", name)
        } else if (_default == ConfigPro.CACHE_TYPE_QUERY) {
            root.setEL("defaultQuery", name)
        } else if (_default == ConfigPro.CACHE_TYPE_RESOURCE) {
            root.setEL("defaultResource", name)
        } else if (_default == ConfigPro.CACHE_TYPE_FUNCTION) {
            root.setEL("defaultFunction", name)
        } else if (_default == ConfigPro.CACHE_TYPE_INCLUDE) {
            root.setEL("defaultInclude", name)
        } else if (_default == ConfigPro.CACHE_TYPE_HTTP) {
            root.setEL("defaultHttp", name)
        } else if (_default == ConfigPro.CACHE_TYPE_FILE) {
            root.setEL("defaultFile", name)
        } else if (_default == ConfigPro.CACHE_TYPE_WEBSERVICE) {
            root.setEL("defaultWebservice", name)
        }

        // Update
        // boolean isUpdate=false;
        val conns: Struct = ConfigWebUtil.getAsStruct("caches", root)
        val it: Iterator<Key?> = conns.keyIterator()
        var key: Key?
        while (it.hasNext()) {
            key = it.next()
            if (key.getString().equalsIgnoreCase(name)) {
                val el: Struct = Caster.toStruct(conns.get(key, null), null)
                setClass(el, null, "", cd)
                el.setEL("custom", toStringURLStyle(custom))
                el.setEL("readOnly", Caster.toString(readOnly))
                el.setEL("storage", Caster.toString(storage))
                return
            }
        }

        // Insert
        val data: Struct = StructImpl(Struct.TYPE_LINKED)
        conns.setEL(name, data)
        setClass(data, null, "", cd)
        data.setEL("custom", toStringURLStyle(custom))
        data.setEL("readOnly", Caster.toString(readOnly))
        data.setEL("storage", Caster.toString(storage))
    }

    private fun rem(sct: Struct?, key: String?) {
        sct.removeEL(KeyImpl.init(key))
    }

    @Throws(PageException::class)
    fun removeCacheDefaultConnection(type: Int) {
        checkWriteAccess()
        val hasAccess: Boolean = ConfigWebUtil.hasAccess(config, SecurityManagerImpl.TYPE_CACHE)
        if (!hasAccess) throw SecurityException("no access to update cache connections")
        val parent: Struct? = _getRootElement("cache")
        if (type == ConfigPro.CACHE_TYPE_OBJECT) {
            rem(parent, "defaultObject")
        } else if (type == ConfigPro.CACHE_TYPE_TEMPLATE) {
            rem(parent, "defaultTemplate")
        } else if (type == ConfigPro.CACHE_TYPE_QUERY) {
            rem(parent, "defaultQuery")
        } else if (type == ConfigPro.CACHE_TYPE_RESOURCE) {
            rem(parent, "defaultResource")
        } else if (type == ConfigPro.CACHE_TYPE_FUNCTION) {
            rem(parent, "defaultFunction")
        } else if (type == ConfigPro.CACHE_TYPE_INCLUDE) {
            rem(parent, "defaultInclude")
        } else if (type == ConfigPro.CACHE_TYPE_HTTP) {
            rem(parent, "defaultHttp")
        } else if (type == ConfigPro.CACHE_TYPE_FILE) {
            rem(parent, "defaultFile")
        } else if (type == ConfigPro.CACHE_TYPE_WEBSERVICE) {
            rem(parent, "defaultWebservice")
        }
    }

    @Throws(PageException::class)
    fun updateCacheDefaultConnection(type: Int, name: String?) {
        checkWriteAccess()
        val hasAccess: Boolean = ConfigWebUtil.hasAccess(config, SecurityManagerImpl.TYPE_CACHE)
        if (!hasAccess) throw SecurityException("no access to update cache default connections")
        val parent: Struct? = _getRootElement("cache")
        if (type == ConfigPro.CACHE_TYPE_OBJECT) {
            parent.setEL("defaultObject", name)
        } else if (type == ConfigPro.CACHE_TYPE_TEMPLATE) {
            parent.setEL("defaultTemplate", name)
        } else if (type == ConfigPro.CACHE_TYPE_QUERY) {
            parent.setEL("defaultQuery", name)
        } else if (type == ConfigPro.CACHE_TYPE_RESOURCE) {
            parent.setEL("defaultResource", name)
        } else if (type == ConfigPro.CACHE_TYPE_FUNCTION) {
            parent.setEL("defaultFunction", name)
        } else if (type == ConfigPro.CACHE_TYPE_INCLUDE) {
            parent.setEL("defaultInclude", name)
        } else if (type == ConfigPro.CACHE_TYPE_HTTP) {
            parent.setEL("defaultHttp", name)
        } else if (type == ConfigPro.CACHE_TYPE_FILE) {
            parent.setEL("defaultFile", name)
        } else if (type == ConfigPro.CACHE_TYPE_WEBSERVICE) {
            parent.setEL("defaultWebservice", name)
        }
    }

    @Throws(PageException::class)
    fun removeResourceProvider(scheme: String?) {
        checkWriteAccess()
        val sm: SecurityManager = config.getSecurityManager()
        val access: Short = sm.getAccess(SecurityManager.TYPE_FILE)
        val hasAccess = access == SecurityManager.VALUE_YES
        if (!hasAccess) throw SecurityException("no access to remove resource provider")
        _removeResourceProvider(scheme)
    }

    @Throws(PageException::class)
    fun _removeResourceProvider(scheme: String?) {
        val children: Array = ConfigWebUtil.getAsArray("resourceProviders", root)
        val keys: Array<Key?> = children.keys()

        // remove
        for (i in keys.indices.reversed()) {
            val key: Key? = keys[i]
            val tmp: Struct = Caster.toStruct(children.get(key, null), null) ?: continue
            val elScheme: String = ConfigWebUtil.getAsString("scheme", tmp, "")
            if (elScheme.equalsIgnoreCase(scheme)) {
                children.removeEL(key)
                break
            }
        }
    }

    @Throws(PageException::class)
    fun updateResourceProvider(scheme: String?, cd: ClassDefinition?, arguments: Struct?) {
        updateResourceProvider(scheme, cd, toStringCSSStyle(arguments))
    }

    @Throws(PageException::class)
    fun _updateResourceProvider(scheme: String?, cd: ClassDefinition?, arguments: Struct?) {
        _updateResourceProvider(scheme, cd, toStringCSSStyle(arguments))
    }

    @Throws(PageException::class)
    fun updateResourceProvider(scheme: String?, cd: ClassDefinition?, arguments: String?) {
        checkWriteAccess()
        val sm: SecurityManager = config.getSecurityManager()
        val access: Short = sm.getAccess(SecurityManager.TYPE_FILE)
        val hasAccess = access == SecurityManager.VALUE_YES
        if (!hasAccess) throw SecurityException("no access to update resources")
        _updateResourceProvider(scheme, cd, arguments)
    }

    @Throws(PageException::class)
    fun _updateResourceProvider(scheme: String?, cd: ClassDefinition?, arguments: String?) {

        // check parameters
        if (StringUtil.isEmpty(scheme)) throw ExpressionException("scheme can't be an empty value")
        val children: Array = ConfigWebUtil.getAsArray("resourceProviders", root)

        // Update
        for (i in 1..children.size()) {
            val el: Struct = Caster.toStruct(children.get(i, null), null) ?: continue
            val elScheme: String = ConfigWebUtil.getAsString("scheme", el, null)
            if (elScheme.equalsIgnoreCase(scheme)) {
                setClass(el, null, "", cd)
                el.setEL("scheme", scheme)
                el.setEL("arguments", arguments)
                return
            }
        }

        // Insert
        val el: Struct = StructImpl()
        children.appendEL(el)
        el.setEL("scheme", scheme)
        el.setEL("arguments", arguments)
        setClass(el, null, "", cd)
    }

    @Throws(PageException::class)
    fun updateDefaultResourceProvider(cd: ClassDefinition?, arguments: String?) {
        checkWriteAccess()
        val sm: SecurityManager = config.getSecurityManager()
        val access: Short = sm.getAccess(SecurityManager.TYPE_FILE)
        val hasAccess = access == SecurityManager.VALUE_YES
        if (!hasAccess) throw SecurityException("no access to update resources")
        val children: Array = ConfigWebUtil.getAsArray("defaultResourceProviders", root)

        // Update
        for (i in 1..children.size()) {
            val el: Struct = Caster.toStruct(children.get(i, null), null) ?: continue
            el.setEL("arguments", arguments)
            return
        }

        // Insert
        val el: Struct = StructImpl(Struct.TYPE_LINKED)
        children.appendEL(el)
        el.setEL("arguments", arguments)
        setClass(el, null, "", cd)
    }

    private fun getDatasourceLength(config: ConfigPro?): Int {
        val ds: Map = config.getDataSourcesAsMap()
        val it: Iterator = ds.keySet().iterator()
        var len = 0
        while (it.hasNext()) {
            if (!(ds.get(it.next()) as DataSource).isReadOnly()) len++
        }
        return len
    }

    @Throws(PageException::class)
    fun getResourceProviders(): Query? {
        checkReadAccess()
        // check parameters
        val elProviders: Array = ConfigWebUtil.getAsArray("resourceProviders", root)
        val elDefaultProviders: Array = ConfigWebUtil.getAsArray("defaultResourceProvider", root)
        val providers: Array<ResourceProvider?> = config.getResourceProviders()
        val defaultProvider: ResourceProvider = config.getDefaultResourceProvider()
        val qry: Query = QueryImpl(arrayOf<String?>("support", "scheme", "caseSensitive", "default", "class", "bundleName", "bundleVersion", "arguments"),
                elProviders.size() + elDefaultProviders.size(), "resourceproviders")
        var row = 1
        for (i in 1..elDefaultProviders.size()) {
            val tmp: Struct = Caster.toStruct(elDefaultProviders.get(i, null), null) ?: continue
            getResourceProviders(arrayOf<ResourceProvider?>(defaultProvider), qry, tmp, row++, Boolean.TRUE)
        }
        for (i in 1..elProviders.size()) {
            val tmp: Struct = Caster.toStruct(elProviders.get(i, null), null) ?: continue
            getResourceProviders(providers, qry, tmp, row++, Boolean.FALSE)
        }
        return qry
    }

    @Throws(PageException::class)
    private fun getResourceProviders(providers: Array<ResourceProvider?>?, qry: Query?, p: Struct?, row: Int, def: Boolean?) {
        val support: Array = ArrayImpl()
        val cn: String = ConfigWebUtil.getAsString("class", p, null)
        val name: String = ConfigWebUtil.getAsString("bundleName", p, null)
        val version: String = ConfigWebUtil.getAsString("bundleVersion", p, null)
        val cd: ClassDefinition = ClassDefinitionImpl(cn, name, version, ThreadLocalPageContext.getConfig().getIdentification())
        qry.setAt("scheme", row, p.get("scheme"))
        qry.setAt("arguments", row, p.get("arguments"))
        qry.setAt("class", row, cd.getClassName())
        qry.setAt("bundleName", row, cd.getName())
        qry.setAt("bundleVersion", row, cd.getVersionAsString())
        for (i in providers.indices) {
            if (providers!![i].getClass().getName().equals(cd.getClassName())) {
                if (providers[i].isAttributesSupported()) support.append("attributes")
                if (providers[i].isModeSupported()) support.append("mode")
                qry.setAt("support", row, ListUtil.arrayToList(support, ","))
                qry.setAt("scheme", row, providers[i].getScheme())
                qry.setAt("caseSensitive", row, Caster.toBoolean(providers[i].isCaseSensitive()))
                qry.setAt("default", row, def)
                break
            }
        }
    }

    @Throws(ExpressionException::class, SecurityException::class)
    fun removeJDBCDriver(className: String?) {
        checkWriteAccess()
        // check parameters
        if (StringUtil.isEmpty(className)) throw ExpressionException("class name for jdbc driver cannot be empty")
        val children: Struct = ConfigWebUtil.getAsStruct("jdbcDrivers", root)
        val keys: Array<Key?> = children.keys()
        for (key in keys) {
            if (key.getString().equalsIgnoreCase(className)) {
                children.removeEL(key)
            }
        }
    }

    /**
     * remove a DataSource Connection
     *
     * @param name
     * @throws ExpressionException
     * @throws SecurityException
     */
    @Throws(ExpressionException::class, SecurityException::class)
    fun removeDataSource(name: String?) {
        checkWriteAccess()
        // check parameters
        if (name == null || name.length() === 0) throw ExpressionException("name for Datasource Connection can be an empty value")
        val children: Struct = ConfigWebUtil.getAsStruct("dataSources", root)
        val keys: Array<Key?> = children.keys()
        for (key in keys) {
            val tmp: Struct = Caster.toStruct(children.get(key, null), null) ?: continue
            if (key.getString().equalsIgnoreCase(name)) {
                children.removeEL(key)
            }
        }
    }

    @Throws(ExpressionException::class, SecurityException::class)
    fun removeCacheConnection(name: String?) {
        checkWriteAccess()
        val hasAccess: Boolean = ConfigWebUtil.hasAccess(config, SecurityManagerImpl.TYPE_CACHE)
        if (!hasAccess) throw SecurityException("no access to remove cache connection")

        // check parameters
        if (StringUtil.isEmpty(name)) throw ExpressionException("name for Cache Connection can not be an empty value")
        val parent: Struct? = _getRootElement("cache")

        // remove default flag
        if (name.equalsIgnoreCase(Caster.toString(parent.get("defaultObject", null), null))) rem(parent, "defaultObject")
        if (name.equalsIgnoreCase(Caster.toString(parent.get("defaultTemplate", null), null))) rem(parent, "defaultTemplate")
        if (name.equalsIgnoreCase(Caster.toString(parent.get("defaultQuery", null), null))) rem(parent, "defaultQuery")
        if (name.equalsIgnoreCase(Caster.toString(parent.get("defaultResource", null), null))) rem(parent, "defaultResource")

        // remove element
        val children: Struct = ConfigWebUtil.getAsStruct("caches", root)
        val keys: Array<Key?> = children.keys()
        for (key in keys) {
            val tmp: Struct = Caster.toStruct(children.get(key, null), null) ?: continue
            val n: String = key.getString()
            if (n != null && n.equalsIgnoreCase(name)) {
                val conns: Map<String?, CacheConnection?> = config.getCacheConnections()
                val cc: CacheConnection? = conns[n.toLowerCase()]
                if (cc != null) {
                    CacheUtil.releaseEL(cc)
                    // CacheUtil.removeEL( config instanceof ConfigWeb ? (ConfigWeb) config : null, cc );
                }
                children.removeEL(key)
            }
        }
    }

    @Throws(ExpressionException::class, SecurityException::class)
    fun cacheConnectionExists(name: String?): Boolean {
        checkReadAccess()
        if (!ConfigWebUtil.hasAccess(config, SecurityManagerImpl.TYPE_CACHE)) throw SecurityException("no access to check cache connection")
        if (name == null || name.isEmpty()) throw ExpressionException("name for Cache Connection can not be an empty value")
        val children: Array = ConfigWebUtil.getAsArray("cache", "connection", root)
        for (i in 1..children.size()) {
            val tmp: Struct = Caster.toStruct(children.get(i, null), null) ?: continue
            val n: String = ConfigWebUtil.getAsString("name", tmp, null)
            if (n != null && n.equalsIgnoreCase(name)) return true
        }
        return false
    }

    @Throws(PageException::class)
    fun removeGatewayEntry(name: String?) {
        checkWriteAccess()
        val hasAccess: Boolean = ConfigWebUtil.hasAccess(config, SecurityManagerImpl.TYPE_GATEWAY)
        if (!hasAccess) throw SecurityException("no access to remove gateway entry")
        _removeGatewayEntry(name)
    }

    @Throws(PageException::class)
    protected fun _removeGatewayEntry(name: String?) {
        if (StringUtil.isEmpty(name)) throw ExpressionException("name for Gateway Id can be an empty value")
        val children: Struct = ConfigWebUtil.getAsStruct("gateways", root)
        val keys: Array<Key?> = children.keys()
        // remove element
        for (key in keys) {
            val n: String = key.getString()
            if (n != null && n.equalsIgnoreCase(name)) {
                if (config is ConfigWeb) {
                    _removeGatewayEntry(config as ConfigWebPro?, n)
                } else {
                    val cws: Array<ConfigWeb?> = (config as ConfigServerImpl?)!!.getConfigWebs()
                    for (cw in cws) {
                        _removeGatewayEntry(cw as ConfigWebPro?, name)
                    }
                }
                children.removeEL(key)
            }
        }
    }

    private fun _removeGatewayEntry(cw: ConfigWebPro?, name: String?) {
        val engine: GatewayEngineImpl = cw!!.getGatewayEngine() as GatewayEngineImpl
        val conns: Map<String?, GatewayEntry?> = engine.getEntries()
        val ge: GatewayEntry? = conns[name]
        if (ge != null) {
            engine.remove(ge)
        }
    }

    @Throws(ExpressionException::class, SecurityException::class)
    fun removeRemoteClient(url: String?) {
        checkWriteAccess()

        // SNSN
        val hasAccess: Boolean = ConfigWebUtil.hasAccess(config, SecurityManagerImpl.TYPE_REMOTE)
        if (!hasAccess) throw SecurityException("no access to remove remote client settings")

        // check parameters
        if (StringUtil.isEmpty(url)) throw ExpressionException("url for Remote Client can be an empty value")
        val children: Array = ConfigWebUtil.getAsArray("remoteClients", "remoteClient", root)
        val keys: Array<Key?> = children.keys()
        for (i in keys.indices.reversed()) {
            val key: Key? = keys[i]
            val tmp: Struct = Caster.toStruct(children.get(key, null), null) ?: continue
            val n: String = ConfigWebUtil.getAsString("url", tmp, null)
            if (n != null && n.equalsIgnoreCase(url)) {
                children.removeEL(key)
            }
        }
    }

    /**
     * update PSQ State
     *
     * @param psq Preserver Single Quote
     * @throws SecurityException
     */
    @Throws(SecurityException::class)
    fun updatePSQ(psq: Boolean?) {
        checkWriteAccess()
        val hasAccess: Boolean = ConfigWebUtil.hasAccess(config, SecurityManager.TYPE_DATASOURCE)
        if (!hasAccess) throw SecurityException("no access to update datasource connections")
        root.setEL("preserveSingleQuote", Caster.toBooleanValue(psq, true))
    }

    @Throws(SecurityException::class)
    fun updateInspectTemplate(str: String?) {
        checkWriteAccess()
        val hasAccess: Boolean = ConfigWebUtil.hasAccess(config, SecurityManager.TYPE_SETTING)
        if (!hasAccess) throw SecurityException("no access to update")
        root.setEL("inspectTemplate", str)
    }

    @Throws(SecurityException::class)
    fun updateTypeChecking(typeChecking: Boolean?) {
        checkWriteAccess()
        val hasAccess: Boolean = ConfigWebUtil.hasAccess(config, SecurityManager.TYPE_SETTING)
        if (!hasAccess) throw SecurityException("no access to update")
        if (typeChecking == null) rem(root, "typeChecking") else root.setEL("typeChecking", Caster.toString(typeChecking.booleanValue()))
    }

    @Throws(SecurityException::class, ApplicationException::class)
    fun updateCachedAfterTimeRange(ts: TimeSpan?) {
        checkWriteAccess()
        val hasAccess: Boolean = ConfigWebUtil.hasAccess(config, SecurityManager.TYPE_SETTING)
        if (!hasAccess) throw SecurityException("no access to update")
        if (ts == null) rem(root, "cachedAfter") else {
            if (ts.getMillis() < 0) throw ApplicationException("value cannot be a negative number")
            root.setEL("cachedAfter", ts.getDay().toString() + "," + ts.getHour() + "," + ts.getMinute() + "," + ts.getSecond())
        }
    }

    /**
     * sets the scope cascading type
     *
     * @param type (SCOPE_XYZ)
     * @throws SecurityException
     */
    @Throws(SecurityException::class)
    fun updateScopeCascadingType(type: String?) {
        checkWriteAccess()
        val hasAccess: Boolean = ConfigWebUtil.hasAccess(config, SecurityManager.TYPE_SETTING)
        if (!hasAccess) throw SecurityException("no access to update scope setting")
        if (type.equalsIgnoreCase("strict")) root.setEL("scopeCascading", "strict") else if (type.equalsIgnoreCase("small")) root.setEL("scopeCascading", "small") else if (type.equalsIgnoreCase("standard")) root.setEL("scopeCascading", "standard") else root.setEL("scopeCascading", "standard")
    }

    /**
     * sets the scope cascading type
     *
     * @param type (SCOPE_XYZ)
     * @throws SecurityException
     */
    @Throws(SecurityException::class)
    fun updateScopeCascadingType(type: Short) {
        checkWriteAccess()
        val hasAccess: Boolean = ConfigWebUtil.hasAccess(config, SecurityManager.TYPE_SETTING)
        if (!hasAccess) throw SecurityException("no access to update scope setting")
        if (type == ConfigWeb.SCOPE_STRICT) root.setEL("scopeCascading", "strict") else if (type == ConfigWeb.SCOPE_SMALL) root.setEL("scopeCascading", "small") else if (type == ConfigWeb.SCOPE_STANDARD) root.setEL("scopeCascading", "standard")
    }

    /**
     * sets if allowed implicid query call
     *
     * @param allow
     * @throws SecurityException
     */
    @Throws(SecurityException::class)
    fun updateAllowImplicidQueryCall(allow: Boolean?) {
        checkWriteAccess()
        val hasAccess: Boolean = ConfigWebUtil.hasAccess(config, SecurityManager.TYPE_SETTING)
        if (!hasAccess) throw SecurityException("no access to update scope setting")
        root.setEL("cascadeToResultset", allow)
    }

    @Throws(SecurityException::class)
    fun updateMergeFormAndUrl(merge: Boolean?) {
        checkWriteAccess()
        val hasAccess: Boolean = ConfigWebUtil.hasAccess(config, SecurityManager.TYPE_SETTING)
        if (!hasAccess) throw SecurityException("no access to update scope setting")
        root.setEL("mergeUrlForm", merge)
    }

    /**
     * updates request timeout value
     *
     * @param span
     * @throws SecurityException
     * @throws ApplicationException
     */
    @Throws(SecurityException::class, ApplicationException::class)
    fun updateRequestTimeout(span: TimeSpan?) {
        checkWriteAccess()
        val hasAccess: Boolean = ConfigWebUtil.hasAccess(config, SecurityManager.TYPE_SETTING)
        if (!hasAccess) throw SecurityException("no access to update scope setting")
        if (span != null) {
            if (span.getMillis() <= 0) throw ApplicationException("value must be a positive number")
            root.setEL("requestTimeout", span.getDay().toString() + "," + span.getHour() + "," + span.getMinute() + "," + span.getSecond())
        } else rem(root, "requestTimeout")
    }

    @Throws(SecurityException::class, ApplicationException::class)
    fun updateApplicationPathTimeout(span: TimeSpan?) {
        checkWriteAccess()
        val hasAccess: Boolean = ConfigWebUtil.hasAccess(config, SecurityManager.TYPE_SETTING)
        if (!hasAccess) throw SecurityException("no access to update scope setting")
        if (span != null) {
            if (span.getMillis() <= 0) throw ApplicationException("value must be a positive number")
            root.setEL("applicationPathTimeout", span.getDay().toString() + "," + span.getHour() + "," + span.getMinute() + "," + span.getSecond())
        } else rem(root, "applicationPathTimeout")
    }

    /**
     * updates session timeout value
     *
     * @param span
     * @throws SecurityException
     */
    @Throws(SecurityException::class)
    fun updateSessionTimeout(span: TimeSpan?) {
        checkWriteAccess()
        val hasAccess: Boolean = ConfigWebUtil.hasAccess(config, SecurityManager.TYPE_SETTING)
        if (!hasAccess) throw SecurityException("no access to update scope setting")
        if (span != null) root.setEL("sessiontimeout", span.getDay().toString() + "," + span.getHour() + "," + span.getMinute() + "," + span.getSecond()) else rem(root, "sessiontimeout")
    }

    @Throws(SecurityException::class, ApplicationException::class)
    fun updateClientStorage(storage: String?) {
        updateStorage("client", storage)
    }

    @Throws(SecurityException::class, ApplicationException::class)
    fun updateSessionStorage(storage: String?) {
        updateStorage("session", storage)
    }

    @Throws(SecurityException::class, ApplicationException::class)
    private fun updateStorage(storageName: String?, storage: String?) {
        var storage = storage
        checkWriteAccess()
        val hasAccess: Boolean = ConfigWebUtil.hasAccess(config, SecurityManager.TYPE_SETTING)
        if (!hasAccess) throw SecurityException("no access to update scope setting")
        storage = validateStorage(storage)
        if (!StringUtil.isEmpty(storage, true)) root.setEL(storageName.toString() + "Storage", storage) else rem(root, storageName.toString() + "Storage")
    }

    @Throws(ApplicationException::class)
    private fun validateStorage(storage: String?): String? {
        var storage = storage
        storage = storage.trim().toLowerCase()

        // empty
        if (StringUtil.isEmpty(storage, true)) return ""

        // standard storages
        if ("cookie".equals(storage) || "memory".equals(storage) || "file".equals(storage)) return storage

        // aliases
        if ("ram".equals(storage)) return "memory"
        if ("registry".equals(storage)) return "file"

        // datasource
        val ds: DataSource = config.getDataSource(storage, null)
        if (ds != null) {
            if (ds.isStorage()) return storage
            throw ApplicationException("datasource [$storage] is not enabled to be used as session/client storage")
        }

        // cache
        val cc: CacheConnection = CacheUtil.getCacheConnection(ThreadLocalPageContext.get(config), storage, null)
        if (cc != null) {
            if (cc.isStorage()) return storage
            throw ApplicationException("cache [$storage] is not enabled to be used as session/client storage")
        }
        val sdx: String = StringUtil.soundex(storage)

        // check if a datasource has a similar name
        val sources: Array<DataSource?> = config.getDataSources()
        for (i in sources.indices) {
            if (StringUtil.soundex(sources[i].getName()).equals(sdx)) throw ApplicationException("no matching storage for [" + storage + "] found, did you mean [" + sources[i].getName() + "]")
        }

        // check if a cache has a similar name
        val it: Iterator<String?> = config.getCacheConnections().keySet().iterator()
        var name: String?
        while (it.hasNext()) {
            name = it.next()
            if (StringUtil.soundex(name).equals(sdx)) throw ApplicationException("no matching storage for [$storage] found, did you mean [$name]")
        }
        throw ApplicationException("no matching storage for [$storage] found")
    }

    /**
     * updates session timeout value
     *
     * @param span
     * @throws SecurityException
     */
    @Throws(SecurityException::class)
    fun updateClientTimeout(span: TimeSpan?) {
        checkWriteAccess()
        val hasAccess: Boolean = ConfigWebUtil.hasAccess(config, SecurityManager.TYPE_SETTING)
        if (!hasAccess) throw SecurityException("no access to update scope setting")
        if (span != null) root.setEL("clientTimeout", span.getDay().toString() + "," + span.getHour() + "," + span.getMinute() + "," + span.getSecond()) else rem(root, "clientTimeout")
    }

    @Throws(SecurityException::class, ApplicationException::class)
    fun updateCFMLWriterType(writerType: String?) {
        var writerType = writerType
        checkWriteAccess()
        val hasAccess: Boolean = ConfigWebUtil.hasAccess(config, SecurityManager.TYPE_SETTING)
        if (!hasAccess) throw SecurityException("no access to update scope setting")
        writerType = writerType.trim()

        // remove
        if (StringUtil.isEmpty(writerType)) {
            if (root.containsKey("cfmlWriter")) rem(root, "cfmlWriter")
            return
        }
        if ("smart".equalsIgnoreCase(writerType)) writerType = "white-space-pref" else if (Decision.isBoolean(writerType)) {
            writerType = if (Caster.toBooleanValue(writerType, false)) "white-space" else "regular"
        }

        // update
        if (!"white-space".equalsIgnoreCase(writerType) && !"white-space-pref".equalsIgnoreCase(writerType) && !"regular".equalsIgnoreCase(writerType)) throw ApplicationException("invalid writer type definition [$writerType], valid types are [white-space, white-space-pref, regular]")
        root.setEL("cfmlWriter", writerType.toLowerCase())
    }

    @Throws(SecurityException::class)
    fun updateSuppressContent(value: Boolean?) {
        checkWriteAccess()
        val hasAccess: Boolean = ConfigWebUtil.hasAccess(config, SecurityManager.TYPE_SETTING)
        if (!hasAccess) throw SecurityException("no access to update scope setting")
        root.setEL("suppressContent", Caster.toString(value, ""))
    }

    @Throws(SecurityException::class)
    fun updateShowVersion(value: Boolean?) {
        checkWriteAccess()
        val hasAccess: Boolean = ConfigWebUtil.hasAccess(config, SecurityManager.TYPE_SETTING)
        if (!hasAccess) throw SecurityException("no access to update scope setting")
        root.setEL("showVersion", Caster.toString(value, ""))
    }

    @Throws(SecurityException::class)
    fun updateAllowCompression(value: Boolean?) {
        checkWriteAccess()
        val hasAccess: Boolean = ConfigWebUtil.hasAccess(config, SecurityManager.TYPE_SETTING)
        if (!hasAccess) throw SecurityException("no access to update scope setting")
        root.setEL("allowCompression", value)
    }

    @Throws(SecurityException::class)
    fun updateContentLength(value: Boolean?) {
        checkWriteAccess()
        val hasAccess: Boolean = ConfigWebUtil.hasAccess(config, SecurityManager.TYPE_SETTING)
        if (!hasAccess) throw SecurityException("no access to update scope setting")
        root.setEL("showContentLength", Caster.toString(value, ""))
    }

    @Throws(SecurityException::class)
    fun updateBufferOutput(value: Boolean?) {
        checkWriteAccess()
        val hasAccess: Boolean = ConfigWebUtil.hasAccess(config, SecurityManager.TYPE_SETTING)
        if (!hasAccess) throw SecurityException("no access to update scope setting")
        root.setEL("bufferTagBodyOutput", value)
    }

    /**
     * updates request timeout value
     *
     * @param span
     * @throws SecurityException
     */
    @Throws(SecurityException::class)
    fun updateApplicationTimeout(span: TimeSpan?) {
        checkWriteAccess()
        val hasAccess: Boolean = ConfigWebUtil.hasAccess(config, SecurityManager.TYPE_SETTING)
        if (!hasAccess) throw SecurityException("no access to update scope setting")
        if (span != null) root.setEL("applicationTimeout", span.getDay().toString() + "," + span.getHour() + "," + span.getMinute() + "," + span.getSecond()) else rem(root, "applicationTimeout")
    }

    @Throws(SecurityException::class)
    fun updateApplicationListener(type: String?, mode: String?) {
        checkWriteAccess()
        val hasAccess: Boolean = ConfigWebUtil.hasAccess(config, SecurityManager.TYPE_SETTING)
        if (!hasAccess) throw SecurityException("no access to update listener type")
        root.setEL("listenerType", type.toLowerCase().trim())
        root.setEL("listenerMode", mode.toLowerCase().trim())
    }

    @Throws(SecurityException::class, ApplicationException::class)
    fun updateCachedWithin(type: Int, value: Object?) {
        checkWriteAccess()
        val hasAccess: Boolean = ConfigWebUtil.hasAccess(config, SecurityManager.TYPE_SETTING)
        if (!hasAccess) throw SecurityException("no access to update cachedwithin setting")
        val t: String = AppListenerUtil.toCachedWithinType(type, "")
                ?: throw ApplicationException("invalid cachedwithin type definition")
        val v: String = Caster.toString(value, null)
        if (v != null) root.setEL("cachedWithin" + StringUtil.ucFirst(t), v) else rem(root, "cachedWithin" + StringUtil.ucFirst(t))
    }

    @Throws(SecurityException::class)
    fun updateProxy(enabled: Boolean, server: String?, port: Int, username: String?, password: String?) {
        checkWriteAccess()
        val hasAccess: Boolean = ConfigWebUtil.hasAccess(config, SecurityManager.TYPE_SETTING)
        if (!hasAccess) throw SecurityException("no access to update listener type")
        val proxy: Struct? = _getRootElement("proxy")
        proxy.setEL("enabled", Caster.toString(enabled))
        if (!StringUtil.isEmpty(server)) proxy.setEL("server", server)
        if (port > 0) proxy.setEL("port", Caster.toString(port))
        if (!StringUtil.isEmpty(username)) proxy.setEL("username", username)
        if (!StringUtil.isEmpty(password)) proxy.setEL("password", password)
    }
    /*
	 * public void removeProxy() throws SecurityException { boolean
	 * hasAccess=ConfigWebUtil.hasAccess(config,SecurityManager.TYPE_SETTING); if(!hasAccess) throw new
	 * SecurityException("no access to remove proxy settings");
	 * 
	 * Element proxy=_getRootElement("proxy"); proxy.removeAttribute("server");
	 * proxy.removeAttribute("port"); proxy.removeAttribute("username");
	 * proxy.removeAttribute("password"); }
	 */
    /**
     * enable or desable session management
     *
     * @param sessionManagement
     * @throws SecurityException
     */
    @Throws(SecurityException::class)
    fun updateSessionManagement(sessionManagement: Boolean?) {
        checkWriteAccess()
        val hasAccess: Boolean = ConfigWebUtil.hasAccess(config, SecurityManager.TYPE_SETTING)
        if (!hasAccess) throw SecurityException("no access to update scope setting")
        root.setEL("sessionManagement", Caster.toString(sessionManagement, ""))
    }

    /**
     * enable or desable client management
     *
     * @param clientManagement
     * @throws SecurityException
     */
    @Throws(SecurityException::class)
    fun updateClientManagement(clientManagement: Boolean?) {
        checkWriteAccess()
        val hasAccess: Boolean = ConfigWebUtil.hasAccess(config, SecurityManager.TYPE_SETTING)
        if (!hasAccess) throw SecurityException("no access to update scope setting")
        root.setEL("clientManagement", Caster.toString(clientManagement, ""))
    }

    /**
     * set if client cookies are enabled or not
     *
     * @param clientCookies
     * @throws SecurityException
     */
    @Throws(SecurityException::class)
    fun updateClientCookies(clientCookies: Boolean?) {
        checkWriteAccess()
        val hasAccess: Boolean = ConfigWebUtil.hasAccess(config, SecurityManager.TYPE_SETTING)
        if (!hasAccess) throw SecurityException("no access to update scope setting")
        root.setEL("clientCookies", clientCookies)
    }

    /**
     * set if it's develop mode or not
     *
     * @param developmode
     * @throws SecurityException
     */
    @Throws(SecurityException::class)
    fun updateMode(developmode: Boolean?) {
        checkWriteAccess()
        val hasAccess: Boolean = ConfigWebUtil.hasAccess(config, SecurityManager.TYPE_SETTING)
        if (!hasAccess) throw SecurityException("no access to update scope setting")
        root.setEL("developMode", Caster.toString(developmode, ""))
    }

    /**
     * set if domain cookies are enabled or not
     *
     * @param domainCookies
     * @throws SecurityException
     */
    @Throws(SecurityException::class)
    fun updateDomaincookies(domainCookies: Boolean?) {
        checkWriteAccess()
        val hasAccess: Boolean = ConfigWebUtil.hasAccess(config, SecurityManager.TYPE_SETTING)
        if (!hasAccess) throw SecurityException("no access to update scope setting")
        root.setEL("domainCookies", Caster.toString(domainCookies, ""))
    }

    /**
     * update the locale
     *
     * @param locale
     * @throws SecurityException
     */
    @Throws(SecurityException::class)
    fun updateLocale(locale: String?) {
        checkWriteAccess()
        val hasAccess: Boolean = ConfigWebUtil.hasAccess(config, SecurityManager.TYPE_SETTING)
        if (!hasAccess) throw SecurityException("no access to update regional setting")
        root.setEL("locale", locale.trim())
    }

    @Throws(SecurityException::class)
    fun updateMonitorEnabled(updateMonitorEnabled: Boolean) {
        checkWriteAccess()
        _updateMonitorEnabled(updateMonitorEnabled)
    }

    fun _updateMonitorEnabled(updateMonitorEnabled: Boolean) {
        root.setEL("monitorEnable", Caster.toString(updateMonitorEnabled))
    }

    @Throws(SecurityException::class)
    fun updateScriptProtect(strScriptProtect: String?) {
        checkWriteAccess()
        val hasAccess: Boolean = ConfigWebUtil.hasAccess(config, SecurityManager.TYPE_SETTING)
        if (!hasAccess) throw SecurityException("no access to update script protect")
        root.setEL("scriptProtect", strScriptProtect.trim())
    }

    @Throws(SecurityException::class)
    fun updateAllowURLRequestTimeout(allowURLRequestTimeout: Boolean?) {
        checkWriteAccess()
        val hasAccess: Boolean = ConfigWebUtil.hasAccess(config, SecurityManager.TYPE_SETTING)
        if (!hasAccess) throw SecurityException("no access to update AllowURLRequestTimeout")
        root.setEL("requestTimeoutInURL", Caster.toString(allowURLRequestTimeout, ""))
    }

    @Throws(SecurityException::class)
    fun updateScriptProtect(scriptProtect: Int) {
        updateScriptProtect(AppListenerUtil.translateScriptProtect(scriptProtect))
    }

    /**
     * update the timeZone
     *
     * @param timeZone
     * @throws SecurityException
     */
    @Throws(SecurityException::class)
    fun updateTimeZone(timeZone: String?) {
        checkWriteAccess()
        val hasAccess: Boolean = ConfigWebUtil.hasAccess(config, SecurityManager.TYPE_SETTING)
        if (!hasAccess) throw SecurityException("no access to update regional setting")
        root.setEL("timezone", timeZone.trim())
    }

    /**
     * update the timeServer
     *
     * @param timeServer
     * @param useTimeServer
     * @throws PageException
     */
    @Throws(PageException::class)
    fun updateTimeServer(timeServer: String?, useTimeServer: Boolean?) {
        checkWriteAccess()
        if (useTimeServer != null && useTimeServer.booleanValue() && !StringUtil.isEmpty(timeServer, true)) {
            try {
                NtpClient(timeServer).getOffset()
            } catch (e: IOException) {
                try {
                    NtpClient(timeServer).getOffset()
                } catch (ee: IOException) {
                    throw Caster.toPageException(ee)
                }
            }
        }
        val hasAccess: Boolean = ConfigWebUtil.hasAccess(config, SecurityManager.TYPE_SETTING)
        if (!hasAccess) throw SecurityException("no access to update regional setting")
        root.setEL("timeserver", timeServer.trim())
        if (useTimeServer != null) root.setEL("useTimeserver", Caster.toBooleanValue(useTimeServer)) else rem(root, "useTimeserver")
    }

    /**
     * update the baseComponent
     *
     * @param baseComponent
     * @throws SecurityException
     */
    @Throws(SecurityException::class)
    fun updateBaseComponent(baseComponentCFML: String?, baseComponentLucee: String?) {
        checkWriteAccess()
        val hasAccess: Boolean = ConfigWebUtil.hasAccess(config, SecurityManager.TYPE_SETTING)
        if (!hasAccess) throw SecurityException("no access to update component setting")
        root.setEL("componentBase", baseComponentCFML)
        root.setEL("componentBaseLuceeDialect", baseComponentLucee)
    }

    @Throws(SecurityException::class)
    fun updateComponentDeepSearch(deepSearch: Boolean?) {
        checkWriteAccess()
        val hasAccess: Boolean = ConfigWebUtil.hasAccess(config, SecurityManager.TYPE_SETTING)
        if (!hasAccess) throw SecurityException("no access to update component setting")
        if (deepSearch != null) root.setEL("componentDeepSearch", Caster.toString(deepSearch.booleanValue())) else {
            if (root.containsKey("componentDeepSearch")) rem(root, "componentDeepSearch")
        }
    }

    @Throws(SecurityException::class)
    fun updateComponentDefaultImport(componentDefaultImport: String?) {
        checkWriteAccess()
        val hasAccess: Boolean = ConfigWebUtil.hasAccess(config, SecurityManager.TYPE_SETTING)
        if (!hasAccess) throw SecurityException("no access to update component setting")
        root.setEL("componentAutoImport", componentDefaultImport)
    }

    /**
     * update the Component Data Member default access type
     *
     * @param strAccess
     * @throws SecurityException
     * @throws ExpressionException
     */
    @Throws(SecurityException::class, ApplicationException::class)
    fun updateComponentDataMemberDefaultAccess(strAccess: String?) {
        checkWriteAccess()
        val hasAccess: Boolean = ConfigWebUtil.hasAccess(config, SecurityManager.TYPE_SETTING)
        if (!hasAccess) throw SecurityException("no access to update component setting")
        if (StringUtil.isEmpty(strAccess)) {
            root.setEL("componentDataMemberAccess", "")
        } else {
            root.setEL("componentDataMemberAccess", ComponentUtil.toStringAccess(ComponentUtil.toIntAccess(strAccess)))
        }
    }

    /**
     * update the Component Data Member default access type
     *
     * @param triggerDataMember
     * @throws SecurityException
     */
    @Throws(SecurityException::class)
    fun updateTriggerDataMember(triggerDataMember: Boolean?) {
        checkWriteAccess()
        val hasAccess: Boolean = ConfigWebUtil.hasAccess(config, SecurityManager.TYPE_SETTING)
        if (!hasAccess) throw SecurityException("no access to update trigger-data-member")
        root.setEL("componentImplicitNotation", Caster.toString(triggerDataMember, ""))
    }

    @Throws(SecurityException::class)
    fun updateComponentUseShadow(useShadow: Boolean?) {
        checkWriteAccess()
        val hasAccess: Boolean = ConfigWebUtil.hasAccess(config, SecurityManager.TYPE_SETTING)
        if (!hasAccess) throw SecurityException("no access to update use-shadow")
        root.setEL("componentUseVariablesScope", Caster.toString(useShadow, ""))
    }

    @Throws(SecurityException::class)
    fun updateComponentLocalSearch(componentLocalSearch: Boolean?) {
        checkWriteAccess()
        val hasAccess: Boolean = ConfigWebUtil.hasAccess(config, SecurityManager.TYPE_SETTING)
        if (!hasAccess) throw SecurityException("no access to update component Local Search")
        root.setEL("componentLocalSearch", Caster.toString(componentLocalSearch, ""))
    }

    @Throws(SecurityException::class)
    fun updateComponentPathCache(componentPathCache: Boolean?) {
        checkWriteAccess()
        val hasAccess: Boolean = ConfigWebUtil.hasAccess(config, SecurityManager.TYPE_SETTING)
        if (!hasAccess) throw SecurityException("no access to update component Cache Path")
        if (!Caster.toBooleanValue(componentPathCache, false)) config!!.clearComponentCache()
        root.setEL("componentUseCachePath", Caster.toString(componentPathCache, ""))
    }

    @Throws(SecurityException::class)
    fun updateCTPathCache(ctPathCache: Boolean?) {
        checkWriteAccess()
        if (!ConfigWebUtil.hasAccess(config, SecurityManager.TYPE_CUSTOM_TAG)) throw SecurityException("no access to update custom tag setting")
        if (!Caster.toBooleanValue(ctPathCache, false)) config!!.clearCTCache()
        root.setEL("customTagUseCachePath", Caster.toString(ctPathCache, ""))
    }

    @Throws(SecurityException::class)
    fun updateSecurity(varUsage: String?) {
        checkWriteAccess()
        val el: Struct? = _getRootElement("security")
        if (el != null) {
            if (!StringUtil.isEmpty(varUsage)) el.setEL("variableUsage", Caster.toString(varUsage)) else rem(el, "variableUsage")
        }
    }

    /**
     * updates if debugging or not
     *
     * @param debug if value is null server setting is used
     * @throws SecurityException
     */
    @Throws(SecurityException::class)
    fun updateDebug(debug: Boolean?, template: Boolean?, database: Boolean?, exception: Boolean?, tracing: Boolean?, dump: Boolean?, timer: Boolean?, implicitAccess: Boolean?,
                    queryUsage: Boolean?, thread: Boolean?) {
        checkWriteAccess()
        val hasAccess: Boolean = ConfigWebUtil.hasAccess(config, SecurityManager.TYPE_DEBUGGING)
        if (!hasAccess) throw SecurityException("no access to change debugging settings")
        if (debug != null) root.setEL("debuggingEnabled", debug.booleanValue()) else rem(root, "debuggingEnabled")
        if (database != null) root.setEL("debuggingDatabase", database.booleanValue()) else rem(root, "debuggingDatabase")
        if (template != null) root.setEL("debuggingTemplate", template.booleanValue()) else rem(root, "debuggingTemplate")
        if (exception != null) root.setEL("debuggingException", exception.booleanValue()) else rem(root, "debuggingException")
        if (tracing != null) root.setEL("debuggingTracing", tracing.booleanValue()) else rem(root, "debuggingTracing")
        if (dump != null) root.setEL("debuggingDump", dump.booleanValue()) else rem(root, "debuggingDump")
        if (timer != null) root.setEL("debuggingTimer", timer.booleanValue()) else rem(root, "debuggingTimer")
        if (implicitAccess != null) root.setEL("debuggingImplicitAccess", implicitAccess.booleanValue()) else rem(root, "debuggingImplicitAccess")
        if (queryUsage != null) root.setEL("debuggingQueryUsage", queryUsage.booleanValue()) else rem(root, "debuggingQueryUsage")
        if (thread != null) root.setEL("debuggingThread", thread.booleanValue()) else rem(root, "debuggingThread")
    }

    /**
     * updates the ErrorTemplate
     *
     * @param template
     * @throws SecurityException
     */
    @Throws(SecurityException::class)
    fun updateErrorTemplate(statusCode: Int, template: String?) {
        checkWriteAccess()
        val hasAccess: Boolean = ConfigWebUtil.hasAccess(config, SecurityManager.TYPE_SETTING)
        if (!hasAccess) throw SecurityException("no access to change error settings")
        if (statusCode == 404) root.setEL("errorMissingTemplate", template) else root.setEL("errorGeneralTemplate", template)
    }

    @Throws(SecurityException::class)
    fun updateErrorStatusCode(doStatusCode: Boolean?) {
        checkWriteAccess()
        val hasAccess: Boolean = ConfigWebUtil.hasAccess(config, SecurityManager.TYPE_SETTING)
        if (!hasAccess) throw SecurityException("no access to change error settings")
        root.setEL("errorStatusCode", Caster.toString(doStatusCode, ""))
    }

    @Throws(PageException::class)
    fun updateRegexType(type: String?) {
        checkWriteAccess()
        val hasAccess: Boolean = ConfigWebUtil.hasAccess(config, SecurityManager.TYPE_SETTING)
        if (!hasAccess) throw SecurityException("no access to change regex settings")
        if (StringUtil.isEmpty(type)) rem(root, "regexType") else root.setEL("regexType", RegexFactory.toType(RegexFactory.toType(type), "perl"))
    }

    /**
     * updates the DebugTemplate
     *
     * @param template
     * @throws SecurityException
     */
    @Throws(SecurityException::class)
    fun updateComponentDumpTemplate(template: String?) {
        checkWriteAccess()
        val hasAccess: Boolean = ConfigWebUtil.hasAccess(config, SecurityManager.TYPE_SETTING)
        if (!hasAccess) throw SecurityException("no access to update component setting")
        root.setEL("componentDumpTemplate", template)
    }

    private fun _getRootElement(name: String?): Struct? {
        return ConfigWebUtil.getAsStruct(name, root)
    }

    /**
     * @param setting
     * @param file
     * @param directJavaAccess
     * @param mail
     * @param datasource
     * @param mapping
     * @param customTag
     * @param cfxSetting
     * @param cfxUsage
     * @param debugging
     * @param search
     * @param scheduledTasks
     * @param tagExecute
     * @param tagImport
     * @param tagObject
     * @param tagRegistry
     * @throws SecurityException
     */
    @Throws(SecurityException::class)
    fun updateDefaultSecurity(setting: Short, file: Short, fileAccess: Array<Resource?>?, directJavaAccess: Short, mail: Short, datasource: Short, mapping: Short, remote: Short,
                              customTag: Short, cfxSetting: Short, cfxUsage: Short, debugging: Short, search: Short, scheduledTasks: Short, tagExecute: Short, tagImport: Short, tagObject: Short,
                              tagRegistry: Short, cache: Short, gateway: Short, orm: Short, accessRead: Short, accessWrite: Short) {
        checkWriteAccess()
        if (config !is ConfigServer) throw SecurityException("can't change security settings from this context")
        val security: Struct? = _getRootElement("security")
        updateSecurityFileAccess(security, fileAccess, file)
        security.setEL("setting", SecurityManagerImpl.toStringAccessValue(setting))
        security.setEL("file", SecurityManagerImpl.toStringAccessValue(file))
        security.setEL("direct_java_access", SecurityManagerImpl.toStringAccessValue(directJavaAccess))
        security.setEL("mail", SecurityManagerImpl.toStringAccessValue(mail))
        security.setEL("datasource", SecurityManagerImpl.toStringAccessValue(datasource))
        security.setEL("mapping", SecurityManagerImpl.toStringAccessValue(mapping))
        security.setEL("remote", SecurityManagerImpl.toStringAccessValue(remote))
        security.setEL("custom_tag", SecurityManagerImpl.toStringAccessValue(customTag))
        security.setEL("cfx_setting", SecurityManagerImpl.toStringAccessValue(cfxSetting))
        security.setEL("cfx_usage", SecurityManagerImpl.toStringAccessValue(cfxUsage))
        security.setEL("debugging", SecurityManagerImpl.toStringAccessValue(debugging))
        security.setEL("search", SecurityManagerImpl.toStringAccessValue(search))
        security.setEL("scheduled_task", SecurityManagerImpl.toStringAccessValue(scheduledTasks))
        security.setEL("tag_execute", SecurityManagerImpl.toStringAccessValue(tagExecute))
        security.setEL("tag_import", SecurityManagerImpl.toStringAccessValue(tagImport))
        security.setEL("tag_object", SecurityManagerImpl.toStringAccessValue(tagObject))
        security.setEL("tag_registry", SecurityManagerImpl.toStringAccessValue(tagRegistry))
        security.setEL("cache", SecurityManagerImpl.toStringAccessValue(cache))
        security.setEL("gateway", SecurityManagerImpl.toStringAccessValue(gateway))
        security.setEL("orm", SecurityManagerImpl.toStringAccessValue(orm))
        security.setEL("access_read", SecurityManagerImpl.toStringAccessRWValue(accessRead))
        security.setEL("access_write", SecurityManagerImpl.toStringAccessRWValue(accessWrite))
    }

    private fun removeSecurityFileAccess(parent: Struct?) {
        val children: Array = ConfigWebUtil.getAsArray("fileAccess", parent)
        val keys: Array<Key?> = children.keys()
        // remove existing
        if (children.size() > 0) {
            for (i in keys.indices.reversed()) {
                val key: Key? = keys[i]
                children.removeEL(key)
            }
        }
    }

    private fun updateSecurityFileAccess(parent: Struct?, fileAccess: Array<Resource?>?, file: Short) {
        removeSecurityFileAccess(parent)

        // insert
        if (!ArrayUtil.isEmpty(fileAccess) && file != SecurityManager.VALUE_ALL) {
            var fa: Struct?
            val children: Array = ConfigWebUtil.getAsArray("fileAccess", parent)
            for (i in fileAccess.indices) {
                fa = StructImpl()
                fa.setEL("path", fileAccess!![i].getAbsolutePath())
                children.appendEL(fa)
            }
        }
    }

    /**
     * update a security manager that match the given id
     *
     * @param id
     * @param setting
     * @param file
     * @param fileAccess
     * @param directJavaAccess
     * @param mail
     * @param datasource
     * @param mapping
     * @param customTag
     * @param cfxSetting
     * @param cfxUsage
     * @param debugging
     * @param search
     * @param scheduledTasks
     * @param tagExecute
     * @param tagImport
     * @param tagObject
     * @param tagRegistry
     * @throws SecurityException
     * @throws ApplicationException
     */
    @Throws(SecurityException::class, ApplicationException::class)
    fun updateSecurity(id: String?, setting: Short, file: Short, fileAccess: Array<Resource?>?, directJavaAccess: Short, mail: Short, datasource: Short, mapping: Short, remote: Short,
                       customTag: Short, cfxSetting: Short, cfxUsage: Short, debugging: Short, search: Short, scheduledTasks: Short, tagExecute: Short, tagImport: Short, tagObject: Short,
                       tagRegistry: Short, cache: Short, gateway: Short, orm: Short, accessRead: Short, accessWrite: Short) {
        checkWriteAccess()
        if (config !is ConfigServer) throw SecurityException("can't change security settings from this context")
        val security: Struct? = _getRootElement("security")
        val children: Array = ConfigWebUtil.getAsArray("accessor", security)
        var accessor: Struct? = null
        for (i in 1..children.size()) {
            val tmp: Struct = Caster.toStruct(children.get(i, null), null) ?: continue
            if (id!!.equals(tmp.get("id", ""))) {
                accessor = tmp
            }
        }
        if (accessor == null) throw ApplicationException("there is noc Security Manager for id [$id]")
        updateSecurityFileAccess(accessor, fileAccess, file)
        accessor.setEL("setting", SecurityManagerImpl.toStringAccessValue(setting))
        accessor.setEL("file", SecurityManagerImpl.toStringAccessValue(file))
        accessor.setEL("direct_java_access", SecurityManagerImpl.toStringAccessValue(directJavaAccess))
        accessor.setEL("mail", SecurityManagerImpl.toStringAccessValue(mail))
        accessor.setEL("datasource", SecurityManagerImpl.toStringAccessValue(datasource))
        accessor.setEL("mapping", SecurityManagerImpl.toStringAccessValue(mapping))
        accessor.setEL("remote", SecurityManagerImpl.toStringAccessValue(remote))
        accessor.setEL("custom_tag", SecurityManagerImpl.toStringAccessValue(customTag))
        accessor.setEL("cfx_setting", SecurityManagerImpl.toStringAccessValue(cfxSetting))
        accessor.setEL("cfx_usage", SecurityManagerImpl.toStringAccessValue(cfxUsage))
        accessor.setEL("debugging", SecurityManagerImpl.toStringAccessValue(debugging))
        accessor.setEL("search", SecurityManagerImpl.toStringAccessValue(search))
        accessor.setEL("scheduled_task", SecurityManagerImpl.toStringAccessValue(scheduledTasks))
        accessor.setEL("cache", SecurityManagerImpl.toStringAccessValue(cache))
        accessor.setEL("gateway", SecurityManagerImpl.toStringAccessValue(gateway))
        accessor.setEL("orm", SecurityManagerImpl.toStringAccessValue(orm))
        accessor.setEL("tag_execute", SecurityManagerImpl.toStringAccessValue(tagExecute))
        accessor.setEL("tag_import", SecurityManagerImpl.toStringAccessValue(tagImport))
        accessor.setEL("tag_object", SecurityManagerImpl.toStringAccessValue(tagObject))
        accessor.setEL("tag_registry", SecurityManagerImpl.toStringAccessValue(tagRegistry))
        accessor.setEL("access_read", SecurityManagerImpl.toStringAccessRWValue(accessRead))
        accessor.setEL("access_write", SecurityManagerImpl.toStringAccessRWValue(accessWrite))
    }

    /**
     * @return returns the default password
     * @throws SecurityException
     */
    @Throws(SecurityException::class)
    fun getDefaultPassword(): Password? {
        checkReadAccess()
        if (config is ConfigServerImpl) {
            return (config as ConfigServerImpl?)!!.getDefaultPassword()
        }
        throw SecurityException("can't access default password within this context")
    }

    /**
     * @param password
     * @throws SecurityException
     * @throws IOException
     * @throws DOMException
     */
    @Throws(SecurityException::class, IOException::class)
    fun updateDefaultPassword(password: String?) {
        checkWriteAccess()
        (config as ConfigServerImpl?)!!.setDefaultPassword(PasswordImpl.writeToStruct(root, password, true))
    }

    @Throws(SecurityException::class)
    fun removeDefaultPassword() {
        checkWriteAccess()
        PasswordImpl.removeFromStruct(root, true)
        (config as ConfigServerImpl?)!!.setDefaultPassword(null)
    }

    /**
     * session type update
     *
     * @param type
     * @throws SecurityException
     */
    @Throws(SecurityException::class)
    fun updateSessionType(type: String?) {
        var type = type
        checkWriteAccess()
        val hasAccess: Boolean = ConfigWebUtil.hasAccess(config, SecurityManager.TYPE_SETTING)
        if (!hasAccess) throw SecurityException("no access to update scope setting")
        type = type.toLowerCase().trim()
        root.setEL("sessionType", type)
    }

    @Throws(SecurityException::class)
    fun updateLocalMode(mode: String?) {
        var mode = mode
        checkWriteAccess()
        val hasAccess: Boolean = ConfigWebUtil.hasAccess(config, SecurityManager.TYPE_SETTING)
        if (!hasAccess) throw SecurityException("no access to update scope setting")
        mode = mode.toLowerCase().trim()
        root.setEL("localScopeMode", mode)
    }

    @Throws(SecurityException::class)
    fun updateRestList(list: Boolean?) {
        checkWriteAccess()
        val hasAccess = true // TODO ConfigWebUtil.hasAccess(config,SecurityManager.TYPE_REST);
        if (!hasAccess) throw SecurityException("no access to update rest setting")
        val rest: Struct? = _getRootElement("rest")
        if (list == null) {
            if (rest.containsKey("list")) rem(rest, "list")
        } else rest.setEL("list", Caster.toString(list.booleanValue()))
    }

    /**
     * updates update settingd for Lucee
     *
     * @param type
     * @param location
     * @throws SecurityException
     */
    @Throws(SecurityException::class)
    fun updateUpdate(type: String?, location: String?) {
        var location = location
        checkWriteAccess()
        if (config !is ConfigServer) {
            throw SecurityException("can't change update setting from this context, access is denied")
        }
        root.setEL("updateType", type)
        try {
            location = HTTPUtil.toURL(location, HTTPUtil.ENCODED_AUTO).toString()
        } catch (e: Throwable) {
            ExceptionUtil.rethrowIfNecessary(e)
        }
        root.setEL("updateLocation", location)
    }

    /**
     * creates an individual security manager based on the default security manager
     *
     * @param id
     * @throws DOMException
     * @throws PageException
     */
    @Throws(PageException::class)
    fun createSecurityManager(password: Password?, id: String?) {
        checkWriteAccess()
        val cs: ConfigServerImpl = ConfigWebUtil.getConfigServer(config, password)
        val dsm: SecurityManagerImpl = cs!!.getDefaultSecurityManager().cloneSecurityManager() as SecurityManagerImpl
        cs!!.setSecurityManager(id, dsm)
        val security: Struct? = _getRootElement("security")
        var accessor: Struct? = null
        val children: Array = ConfigWebUtil.getAsArray("accessor", security)
        for (i in 1..children.size()) {
            val tmp: Struct = Caster.toStruct(children.get(i, null), null) ?: continue
            if (id!!.equals(tmp.get("id"))) {
                accessor = tmp
            }
        }
        if (accessor == null) {
            accessor = StructImpl(Struct.TYPE_LINKED)
            children.appendEL(accessor)
        }
        updateSecurityFileAccess(accessor, dsm.getCustomFileAccess(), dsm.getAccess(SecurityManager.TYPE_FILE))
        accessor.setEL("id", id)
        accessor.setEL("setting", SecurityManagerImpl.toStringAccessValue(dsm.getAccess(SecurityManager.TYPE_SETTING)))
        accessor.setEL("file", SecurityManagerImpl.toStringAccessValue(dsm.getAccess(SecurityManager.TYPE_FILE)))
        accessor.setEL("direct_java_access", SecurityManagerImpl.toStringAccessValue(dsm.getAccess(SecurityManager.TYPE_DIRECT_JAVA_ACCESS)))
        accessor.setEL("mail", SecurityManagerImpl.toStringAccessValue(dsm.getAccess(SecurityManager.TYPE_MAIL)))
        accessor.setEL("datasource", SecurityManagerImpl.toStringAccessValue(dsm.getAccess(SecurityManager.TYPE_DATASOURCE)))
        accessor.setEL("mapping", SecurityManagerImpl.toStringAccessValue(dsm.getAccess(SecurityManager.TYPE_MAPPING)))
        accessor.setEL("custom_tag", SecurityManagerImpl.toStringAccessValue(dsm.getAccess(SecurityManager.TYPE_CUSTOM_TAG)))
        accessor.setEL("cfx_setting", SecurityManagerImpl.toStringAccessValue(dsm.getAccess(SecurityManager.TYPE_CFX_SETTING)))
        accessor.setEL("cfx_usage", SecurityManagerImpl.toStringAccessValue(dsm.getAccess(SecurityManager.TYPE_CFX_USAGE)))
        accessor.setEL("debugging", SecurityManagerImpl.toStringAccessValue(dsm.getAccess(SecurityManager.TYPE_DEBUGGING)))
        accessor.setEL("cache", SecurityManagerImpl.toStringAccessValue(dsm.getAccess(SecurityManagerImpl.TYPE_CACHE)))
        accessor.setEL("gateway", SecurityManagerImpl.toStringAccessValue(dsm.getAccess(SecurityManagerImpl.TYPE_GATEWAY)))
        accessor.setEL("orm", SecurityManagerImpl.toStringAccessValue(dsm.getAccess(SecurityManagerImpl.TYPE_ORM)))
        accessor.setEL("tag_execute", SecurityManagerImpl.toStringAccessValue(dsm.getAccess(SecurityManager.TYPE_TAG_EXECUTE)))
        accessor.setEL("tag_import", SecurityManagerImpl.toStringAccessValue(dsm.getAccess(SecurityManager.TYPE_TAG_IMPORT)))
        accessor.setEL("tag_object", SecurityManagerImpl.toStringAccessValue(dsm.getAccess(SecurityManager.TYPE_TAG_OBJECT)))
        accessor.setEL("tag_registry", SecurityManagerImpl.toStringAccessValue(dsm.getAccess(SecurityManager.TYPE_TAG_REGISTRY)))
    }

    /**
     * remove security manager matching given id
     *
     * @param id
     * @throws PageException
     */
    @Throws(PageException::class)
    fun removeSecurityManager(password: Password?, id: String?) {
        checkWriteAccess()
        (ConfigWebUtil.getConfigServer(config, password) as ConfigServerImpl)!!.removeSecurityManager(id)
        val children: Array = ConfigWebUtil.getAsArray("security", "accessor", root)
        val keys: Array<Key?> = children.keys()
        for (i in keys.indices.reversed()) {
            val key: Key? = keys[i]
            val tmp: Struct = Caster.toStruct(children.get(key, null), null) ?: continue
            val n: String = ConfigWebUtil.getAsString("id", tmp, "")
            if (id!!.equals(n)) {
                children.removeEL(key)
            }
        }
    }

    /**
     * run update from cfml engine
     *
     * @throws PageException
     */
    @Throws(PageException::class)
    fun runUpdate(password: Password?) {
        checkWriteAccess()
        val cs: ConfigServerImpl = ConfigWebUtil.getConfigServer(config, password)
        val factory: CFMLEngineFactory = cs!!.getCFMLEngine().getCFMLEngineFactory()
        synchronized(factory) {
            try {
                cleanUp(factory)
                factory.update(cs!!.getPassword(), cs!!.getIdentification())
            } catch (e: Exception) {
                throw Caster.toPageException(e)
            }
        }
    }

    /**
     * run update from cfml engine
     *
     * @throws PageException
     */
    @Throws(PageException::class)
    fun removeLatestUpdate(password: Password?) {
        _removeUpdate(password, true)
    }

    @Throws(PageException::class)
    fun removeUpdate(password: Password?) {
        _removeUpdate(password, false)
    }

    @Throws(PageException::class)
    private fun _removeUpdate(password: Password?, onlyLatest: Boolean) {
        checkWriteAccess()
        val cs: ConfigServerImpl = ConfigWebUtil.getConfigServer(config, password)
        try {
            val factory: CFMLEngineFactory = cs!!.getCFMLEngine().getCFMLEngineFactory()
            if (onlyLatest) {
                factory.removeLatestUpdate(cs!!.getPassword())
            } else factory.removeUpdate(cs!!.getPassword())
        } catch (e: Exception) {
            throw Caster.toPageException(e)
        }
    }

    @Throws(PageException::class)
    fun changeVersionTo(version: Version?, password: Password?, id: IdentificationWeb?) {
        checkWriteAccess()
        val cs: ConfigServerImpl = ConfigWebUtil.getConfigServer(config, password)
        val logger: Log = cs!!.getLog("deploy")
        try {
            val factory: CFMLEngineFactory = cs!!.getCFMLEngine().getCFMLEngineFactory()
            cleanUp(factory)
            // do we have the core file?
            val patchDir: File = factory.getPatchDirectory()
            var localPath: File? = File(version.toString().toString() + ".lco")
            if (!localPath.isFile()) {
                localPath = null
                var v: Version
                val patches: Array<File?> = patchDir.listFiles(ExtensionFilter(arrayOf<String?>(".lco")))
                for (patch in patches) {
                    v = CFMLEngineFactory.toVersion(patch.getName(), null)
                    // not a valid file get deleted
                    if (v == null) {
                        patch.delete()
                    } else {
                        if (v.equals(version)) { // match!
                            localPath = patch
                        } else if (OSGiUtil.isNewerThan(v, version)) {
                            patch.delete()
                        }
                    }
                }
            }

            // download patch
            if (localPath == null) {
                downloadCore(factory, version, id)
            }
            logger.log(Log.LEVEL_INFO, "Update-Engine", "Installing Lucee version [" + version + "] (previous version was [" + cs!!.getEngine().getInfo().getVersion() + "])")
            factory.restart(password)
        } catch (e: Exception) {
            throw Caster.toPageException(e)
        }
    }

    @Throws(IOException::class)
    private fun cleanUp(factory: CFMLEngineFactory?) {
        val patchDir: File = factory.getPatchDirectory()
        val patches: Array<File?> = patchDir.listFiles(ExtensionFilter(arrayOf<String?>(".lco")))
        for (patch in patches) {
            if (!IsZipFile.invoke(patch)) patch.delete()
        }
    }

    @Throws(IOException::class)
    private fun downloadCore(factory: CFMLEngineFactory?, version: Version?, id: Identification?): File? {
        val updateProvider: URL = factory.getUpdateLocation()
        val updateUrl = URL(updateProvider, "/rest/update/provider/download/" + version.toString().toString() + (if (id != null) id.toQueryString() else "").toString() + (if (id == null) "?" else "&").toString() + "allowRedirect=true")
        // log.debug("Admin", "download "+version+" from " + updateUrl);
        // System. out.println(updateUrl);

        // local resource
        val patchDir: File = factory.getPatchDirectory()
        val newLucee = File(patchDir, version.toString() + ".lco")
        var code: Int
        var conn: HttpURLConnection
        try {
            conn = updateUrl.openConnection() as HttpURLConnection
            conn.setRequestMethod("GET")
            conn.setConnectTimeout(10000)
            conn.connect()
            code = conn.getResponseCode()
        } catch (e: UnknownHostException) {
            // log.error("Admin", e);
            throw e
        }

        // the update provider is not providing a download for this
        if (code != 200) {
            var count = 0
            val max = 5
            // the update provider can also provide a different (final) location for this
            while ((code == 301 || code == 302) && count++ < max) {
                var location: String = conn.getHeaderField("Location")
                // just in case we check invalid names
                if (location == null) location = conn.getHeaderField("location")
                if (location == null) location = conn.getHeaderField("LOCATION")
                if (location == null) break
                // System. out.println("download redirected:" + location); // MUST remove
                conn.disconnect()
                val url = URL(location)
                try {
                    conn = url.openConnection() as HttpURLConnection
                    conn.setRequestMethod("GET")
                    conn.setConnectTimeout(10000)
                    conn.connect()
                    code = conn.getResponseCode()
                } catch (e: UnknownHostException) {
                    // log.error("Admin", e);
                    throw e
                }
            }

            // no download available!
            if (code != 200) {
                val msg = ("Lucee Core download failed (response status:" + code + ") the core for version [" + version.toString() + "] from " + updateUrl
                        + ", please download it manually and copy to [" + patchDir + "]")
                // log.debug("Admin", msg);
                conn.disconnect()
                throw IOException(msg)
            }
        }

        // copy it to local directory
        if (newLucee.createNewFile()) {
            IOUtil.copy(conn.getContent() as InputStream, FileOutputStream(newLucee), false, true)
            conn.disconnect()

            // when it is a loader extract the core from it
            val tmp: File = CFMLEngineFactory.extractCoreIfLoader(newLucee)
            if (tmp != null) {
                // System .out.println("extract core from loader"); // MUST remove
                // log.debug("Admin", "extract core from loader");
                newLucee.delete()
                tmp.renameTo(newLucee)
                tmp.delete()
                // System. out.println("exist?" + newLucee.exists()); // MUST remove
            }
        } else {
            conn.disconnect()
            // log.debug("Admin","File for new Version already exists, won't copy new one");
            return null
        }
        return newLucee
    }

    private fun getCoreExtension(): String? {
        return "lco"
    }

    private fun isNewerThan(left: Int, right: Int): Boolean {
        return left > right
    }
    /*
	 * private Resource getPatchDirectory(CFMLEngine engine) throws IOException { //File
	 * f=engine.getCFMLEngineFactory().getResourceRoot(); Resource res =
	 * ResourcesImpl.getFileResourceProvider().getResource(engine.getCFMLEngineFactory().getResourceRoot
	 * ().getAbsolutePath()); Resource pd = res.getRealResource("patches"); if(!pd.exists())pd.mkdirs();
	 * return pd; }
	 */
    /**
     * run update from cfml engine
     *
     * @throws PageException
     */
    @Throws(PageException::class)
    fun restart(password: Password?) {
        checkWriteAccess()
        val cs: ConfigServerImpl = ConfigWebUtil.getConfigServer(config, password)
        val factory: CFMLEngineFactory = cs!!.getCFMLEngine().getCFMLEngineFactory()
        synchronized(factory) {
            try {
                cleanUp(factory)
                factory.restart(cs!!.getPassword())
            } catch (e: Exception) {
                throw Caster.toPageException(e)
            }
        }
    }

    @Throws(PageException::class)
    fun restart(cs: ConfigServerImpl?) {
        val factory: CFMLEngineFactory = cs!!.getCFMLEngine().getCFMLEngineFactory()
        synchronized(factory) {
            try {
                val m: Method = factory.getClass().getDeclaredMethod("_restart", arrayOfNulls<Class?>(0))
                        ?: throw ApplicationException("Cannot restart Lucee.")
                m.setAccessible(true)
                m.invoke(factory, arrayOfNulls<Object?>(0))
            } catch (e: Exception) {
                throw Caster.toPageException(e)
            }
        }
    }

    @Throws(PageException::class)
    fun updateWebCharset(charset: String?) {
        var charset = charset
        checkWriteAccess()
        if (StringUtil.isEmpty(charset)) {
            if (config is ConfigWeb) rem(root, "webCharset") else root.setEL("webCharset", "UTF-8")
        } else {
            charset = checkCharset(charset)
            root.setEL("webCharset", charset)
        }
    }

    @Throws(PageException::class)
    fun updateResourceCharset(charset: String?) {
        var charset = charset
        checkWriteAccess()
        if (StringUtil.isEmpty(charset)) {
            rem(root, "resourceCharset")
        } else {
            charset = checkCharset(charset)
            root.setEL("resourceCharset", charset)
        }
    }

    @Throws(PageException::class)
    fun updateTemplateCharset(charset: String?) {
        var charset = charset
        checkWriteAccess()
        if (StringUtil.isEmpty(charset, true)) {
            rem(root, "templateCharset")
        } else {
            charset = checkCharset(charset)
            root.setEL("templateCharset", charset)
        }
    }

    @Throws(PageException::class)
    private fun checkCharset(charset: String?): String? {
        var charset = charset
        charset = charset.trim()
        if ("system".equalsIgnoreCase(charset)) charset = SystemUtil.getCharset().name() else if ("jre".equalsIgnoreCase(charset)) charset = SystemUtil.getCharset().name() else if ("os".equalsIgnoreCase(charset)) charset = SystemUtil.getCharset().name()

        // check access
        val hasAccess: Boolean = ConfigWebUtil.hasAccess(config, SecurityManager.TYPE_SETTING)
        if (!hasAccess) {
            throw SecurityException("Access Denied to update regional setting")
        }

        // check encoding
        try {
            IOUtil.checkEncoding(charset)
        } catch (e: IOException) {
            throw Caster.toPageException(e)
        }
        return charset
    }

    private fun getStoragDir(config: Config?): Resource? {
        val storageDir: Resource = config.getConfigDir().getRealResource("storage")
        if (!storageDir.exists()) storageDir.mkdirs()
        return storageDir
    }

    @Throws(ConverterException::class, IOException::class, SecurityException::class)
    fun storageSet(config: Config?, key: String?, value: Object?) {
        checkWriteAccess()
        val storageDir: Resource? = getStoragDir(config)
        val storage: Resource = storageDir.getRealResource(key.toString() + ".wddx")
        val converter = WDDXConverter(config.getTimeZone(), true, true)
        val wddx: String = converter.serialize(value)
        IOUtil.write(storage, wddx, "UTF-8", false)
    }

    @Throws(ConverterException::class, IOException::class, SecurityException::class)
    fun storageGet(config: Config?, key: String?): Object? {
        checkReadAccess()
        val storageDir: Resource? = getStoragDir(config)
        val storage: Resource = storageDir.getRealResource(key.toString() + ".wddx")
        if (!storage.exists()) throw IOException("There is no storage named [$key]")
        val converter = WDDXConverter(config.getTimeZone(), true, true)
        return converter.deserialize(IOUtil.toString(storage, "UTF-8"), true)
    }

    @Throws(SecurityException::class)
    fun updateCustomTagDeepSearch(customTagDeepSearch: Boolean) {
        checkWriteAccess()
        if (!ConfigWebUtil.hasAccess(config, SecurityManager.TYPE_CUSTOM_TAG)) throw SecurityException("Access Denied to update custom tag setting")
        root.setEL("customTagDeepSearch", Caster.toString(customTagDeepSearch))
    }

    @Throws(PageException::class)
    fun resetId() {
        checkWriteAccess()
        val res: Resource = config.getConfigDir().getRealResource("id")
        try {
            if (res.exists()) res.remove(false)
        } catch (e: IOException) {
            throw Caster.toPageException(e)
        }
    }

    @Throws(SecurityException::class)
    fun updateCustomTagLocalSearch(customTagLocalSearch: Boolean) {
        checkWriteAccess()
        if (!ConfigWebUtil.hasAccess(config, SecurityManager.TYPE_CUSTOM_TAG)) throw SecurityException("Access Denied to update custom tag setting")
        root.setEL("customTagLocalSearch", Caster.toString(customTagLocalSearch))
    }

    @Throws(PageException::class)
    fun updateCustomTagExtensions(extensions: String?) {
        checkWriteAccess()
        if (!ConfigWebUtil.hasAccess(config, SecurityManager.TYPE_CUSTOM_TAG)) throw SecurityException("Access Denied to update custom tag setting")

        // check
        val arr: Array = ListUtil.listToArrayRemoveEmpty(extensions, ',')
        ListUtil.trimItems(arr)
        // throw new ApplicationException("you must define at least one extension");

        // update charset
        root.setEL("customTagExtensions", ListUtil.arrayToList(arr, ","))
    }

    @Throws(PageException::class)
    fun updateRemoteClient(label: String?, url: String?, type: String?, securityKey: String?, usage: String?, adminPassword: String?, serverUsername: String?, serverPassword: String?,
                           proxyServer: String?, proxyUsername: String?, proxyPassword: String?, proxyPort: String?) {
        var url = url
        var securityKey = securityKey
        var adminPassword = adminPassword
        checkWriteAccess()

        // SNSN
        val hasAccess: Boolean = ConfigWebUtil.hasAccess(config, SecurityManagerImpl.TYPE_REMOTE)
        if (!hasAccess) throw SecurityException("Access Denied to update remote client settings")
        val clients: Struct? = _getRootElement("remoteClients")
        if (StringUtil.isEmpty(url)) throw ExpressionException("[url] cannot be empty")
        if (StringUtil.isEmpty(securityKey)) throw ExpressionException("[securityKey] cannot be empty")
        if (StringUtil.isEmpty(adminPassword)) throw ExpressionException("[adminPassword] can not be empty")
        url = url.trim()
        securityKey = securityKey.trim()
        adminPassword = adminPassword.trim()
        val children: Array = ConfigWebUtil.getAsArray("remoteClient", clients)

        // Update
        for (i in 1..children.size()) {
            val el: Struct = Caster.toStruct(children.get(i, null), null) ?: continue
            val _url: String = ConfigWebUtil.getAsString("url", el, "")
            if (_url != null && _url.equalsIgnoreCase(url)) {
                el.setEL("label", label)
                el.setEL("type", type)
                el.setEL("usage", usage)
                el.setEL("serverUsername", serverUsername)
                el.setEL("proxyServer", proxyServer)
                el.setEL("proxyUsername", proxyUsername)
                el.setEL("proxyPort", proxyPort)
                el.setEL("securityKey", ConfigWebUtil.encrypt(securityKey))
                el.setEL("adminPassword", ConfigWebUtil.encrypt(adminPassword))
                el.setEL("serverPassword", ConfigWebUtil.encrypt(serverPassword))
                el.setEL("proxyPassword", ConfigWebUtil.encrypt(proxyPassword))
                return
            }
        }

        // Insert
        val el: Struct = StructImpl(Struct.TYPE_LINKED)
        el.setEL("label", label)
        el.setEL("url", url)
        el.setEL("type", type)
        el.setEL("usage", usage)
        el.setEL("serverUsername", serverUsername)
        el.setEL("proxyServer", proxyServer)
        el.setEL("proxyUsername", proxyUsername)
        el.setEL("proxyPort", proxyPort)
        el.setEL("securityKey", ConfigWebUtil.encrypt(securityKey))
        el.setEL("adminPassword", ConfigWebUtil.encrypt(adminPassword))
        el.setEL("serverPassword", ConfigWebUtil.encrypt(serverPassword))
        el.setEL("proxyPassword", ConfigWebUtil.encrypt(proxyPassword))
        children.appendEL(el)
    }

    @Throws(PageException::class)
    fun updateUpdateAdminMode(mode: String?, merge: Boolean, keep: Boolean) {
        var mode = mode
        checkWriteAccess()
        if (config!!.getAdminMode() === ConfigImpl.ADMINMODE_MULTI) {
            // copy the content from all web cfconfig into the server cfconfig
            if (merge) {
                val webs: Array<ConfigWeb?> = (config as ConfigServer?).getConfigWebs()
                for (cw in webs) {
                    try {
                        merge(root, ConfigWebFactory.loadDocument(cw.getConfigFile()))
                    } catch (e: IOException) {
                        throw Caster.toPageException(e)
                    }
                }
            }

            // delete all the server configs
            if (!keep) {
                val webs: Array<ConfigWeb?> = (config as ConfigServer?).getConfigWebs()
                for (cw in webs) {
                    cw.getConfigFile().delete()
                }
            }
        }
        if (StringUtil.isEmpty(mode, true)) return
        mode = mode.trim()
        mode = if (mode.equalsIgnoreCase("m") || mode.equalsIgnoreCase("multi") || mode.equalsIgnoreCase("multiple")) "multi" else if (mode.equalsIgnoreCase("s") || mode.equalsIgnoreCase("single")) "single" else throw ApplicationException("invalid mode [$mode], valid modes are [single,multi]")
        root.setEL(KeyConstants._mode, mode)
    }

    private fun merge(server: Collection?, web: Collection?) {
        val keys: Array<Key?> = web.keys()
        var exServer: Object
        var exWeb: Object
        for (key in keys) {
            exServer = server.get(key, null)
            if (exServer is Collection) {
                exWeb = web.get(key, null)
                if (exWeb is Collection) merge(exServer as Collection, exWeb as Collection)
            } else {
                if (server is Array) (server as Array?).appendEL(web.get(key, null)) // TODO can create a duplicate
                else server.setEL(key, web.get(key, null))
            }
        }
    }

    @Throws(PageException::class)
    fun updateMonitor(cd: ClassDefinition?, type: String?, name: String?, logEnabled: Boolean) {
        checkWriteAccess()
        _updateMonitor(cd, type, name, logEnabled)
    }

    @Throws(PageException::class)
    fun _updateMonitor(cd: ClassDefinition?, type: String?, name: String?, logEnabled: Boolean) {
        stopMonitor(ConfigWebUtil.toMonitorType(type, Monitor.TYPE_INTERVAL), name)
        val children: Struct = ConfigWebUtil.getAsStruct("monitors", root)
        val keys: Array<Key?> = children.keys()
        var monitor: Struct? = null
        // Update
        for (key in keys) {
            val el: Struct = Caster.toStruct(children.get(key, null), null) ?: continue
            val _name: String = key.getString()
            if (_name != null && _name.equalsIgnoreCase(name)) {
                monitor = el
                break
            }
        }

        // Insert
        if (monitor == null) {
            monitor = StructImpl(Struct.TYPE_LINKED)
            children.setEL(name, monitor)
        }
        setClass(monitor, null, "", cd)
        monitor.setEL("type", type)
        monitor.setEL("name", name)
        monitor.setEL("log", Caster.toString(logEnabled))
    }

    private fun stopMonitor(type: Int, name: String?) {
        var monitor: Monitor? = null
        try {
            if (Monitor.TYPE_ACTION === type) monitor = config.getActionMonitor(name) else if (Monitor.TYPE_REQUEST === type) monitor = config.getRequestMonitor(name) else if (Monitor.TYPE_REQUEST === type) monitor = config.getIntervallMonitor(name)
        } catch (t: Throwable) {
            ExceptionUtil.rethrowIfNecessary(t)
        }
        IOUtil.closeEL(monitor)
    }

    private fun _removeCache(cd: ClassDefinition?) {
        val children: Array = ConfigWebUtil.getAsArray("cacheClasses", root)
        val keys: Array<Key?> = children.keys()
        for (i in keys.indices.reversed()) {
            val key: Key? = keys[i]
            val el: Struct = Caster.toStruct(children.get(key, null), null) ?: continue
            val _class: String = ConfigWebUtil.getAsString("virtual", el, null)
            if (_class != null && _class.equalsIgnoreCase(cd.getClassName())) {
                children.removeEL(key)
                break
            }
        }
    }

    private fun _removeCacheHandler(id: String?) {
        val handlers: Struct = ConfigWebUtil.getAsStruct("cacheHandlers", root)
        val keys: Array<Key?> = handlers.keys()
        for (key in keys) {
            val _id: String = key.getString()
            if (_id.equalsIgnoreCase(id)) {
                val el: Struct = Caster.toStruct(handlers.get(key, null), null) ?: continue
                handlers.removeEL(key)
                break
            }
        }
    }

    @Throws(PageException::class)
    fun updateCacheHandler(id: String?, cd: ClassDefinition?) {
        checkWriteAccess()
        _updateCacheHandler(id, cd)
    }

    @Throws(PageException::class)
    private fun _updateCache(cd: ClassDefinition?) {
        val children: Array = ConfigWebUtil.getAsArray("cacheClasses", root)
        var ch: Struct? = null
        // Update
        for (i in 1..children.size()) {
            val el: Struct = Caster.toStruct(children.get(i, null), null) ?: continue
            val _class: String = ConfigWebUtil.getAsString("class", el, null)
            if (_class != null && _class.equalsIgnoreCase(cd.getClassName())) {
                ch = el
                break
            }
        }

        // Insert
        if (ch == null) {
            ch = StructImpl(Struct.TYPE_LINKED)
            children.appendEL(ch)
        }
        setClass(ch, null, "", cd)
    }

    @Throws(PageException::class)
    private fun _updateCacheHandler(id: String?, cd: ClassDefinition?) {
        val handlers: Struct = ConfigWebUtil.getAsStruct("cacheHandlers", root)
        val it: Iterator<Entry<Key?, Object?>?> = handlers.entryIterator()
        var ch: Struct? = null
        // Update
        var entry: Entry<Key?, Object?>?
        while (it.hasNext()) {
            entry = it.next()
            val _id: String = entry.getKey().getString()
            if (_id != null && _id.equalsIgnoreCase(id)) {
                val el: Struct = Caster.toStruct(entry.getValue(), null) ?: continue
                ch = el
                break
            }
        }

        // Insert
        if (ch == null) {
            ch = StructImpl(Struct.TYPE_LINKED)
            handlers.setEL(id, ch)
        }
        setClass(ch, null, "", cd)
    }

    @Throws(PageException::class)
    fun updateExecutionLog(cd: ClassDefinition?, args: Struct?, enabled: Boolean) {
        val el: Struct? = _getRootElement("executionLog")
        setClass(el, null, "", cd)
        el.setEL("arguments", toStringCSSStyle(args))
        el.setEL("enabled", Caster.toString(enabled))
    }

    @Throws(SecurityException::class)
    fun removeMonitor(type: String?, name: String?) {
        checkWriteAccess()
        _removeMonitor(type, name)
    }

    fun _removeMonitor(type: String?, name: String?) {
        stopMonitor(ConfigWebUtil.toMonitorType(type, Monitor.TYPE_INTERVAL), name)
        val children: Array = ConfigWebUtil.getAsArray("monitors", root)
        val keys: Array<Key?> = children.keys()
        for (key in keys) {
            val _name: String = key.getString()
            if (_name != null && _name.equalsIgnoreCase(name)) {
                children.removeEL(key)
            }
        }
    }

    @Throws(PageException::class)
    fun removeCacheHandler(id: String?) {
        val handlers: Struct = ConfigWebUtil.getAsStruct("cacheHandlers", root)
        val keys: Array<Key?> = handlers.keys()
        for (key in keys) {
            val _id: String = key.getString()
            if (_id.equalsIgnoreCase(id)) {
                val el: Struct = Caster.toStruct(handlers.get(key, null), null) ?: continue
                handlers.removeEL(key)
                break
            }
        }
    }

    fun updateExtensionInfo(enabled: Boolean) {
        root.setEL("extensionEnabled", enabled)
    }

    @Throws(MalformedURLException::class, PageException::class)
    fun updateRHExtensionProvider(strUrl: String?) {
        updateExtensionProvider(strUrl)
    }

    @Throws(MalformedURLException::class, PageException::class)
    fun updateExtensionProvider(strUrl: String?) {
        var strUrl = strUrl
        val children: Array = ConfigWebUtil.getAsArray("extensionProviders", root)
        strUrl = strUrl.trim()
        val _url: URL = HTTPUtil.toURL(strUrl, HTTPUtil.ENCODED_NO)
        strUrl = _url.toExternalForm()

        // Update
        var url: String
        for (i in 1..children.size()) {
            url = Caster.toString(children.get(i, null), null)
            if (url == null) continue
            if (url.trim().equalsIgnoreCase(strUrl)) {
                return
            }
        }

        // Insert
        children.prepend(strUrl)
    }

    fun removeExtensionProvider(strUrl: String?) {
        var strUrl = strUrl
        val children: Array = ConfigWebUtil.getAsArray("extensionProviders", root)
        val keys: Array<Key?> = children.keys()
        strUrl = strUrl.trim()
        var url: String
        for (i in keys.indices.reversed()) {
            val key: Key? = keys[i]
            url = Caster.toString(children.get(key, null), null)
            if (url == null) continue
            if (url.trim().equalsIgnoreCase(strUrl)) {
                children.removeEL(key)
                return
            }
        }
    }

    fun removeRHExtensionProvider(strUrl: String?) {
        removeExtensionProvider(strUrl)
    }

    @Throws(PageException::class)
    private fun createUid(pc: PageContext?, provider: String?, id: String?): String? {
        return if (Decision.isUUId(id)) {
            Hash.invoke(pc.getConfig(), id, null, null, 1)
        } else Hash.invoke(pc.getConfig(), provider + id, null, null, 1)
    }

    private fun setExtensionAttrs(el: Struct?, extension: Extension?) {
        el.setEL("version", extension.getVersion())
        el.setEL("config", extension.getStrConfig())
        // el.setEL("config",new ScriptConverter().serialize(extension.getConfig()));
        el.setEL("category", extension.getCategory())
        el.setEL("description", extension.getDescription())
        el.setEL("image", extension.getImage())
        el.setEL("label", extension.getLabel())
        el.setEL("name", extension.getName())
        el.setEL("author", extension.getAuthor())
        el.setEL("type", extension.getType())
        el.setEL("codename", extension.getCodename())
        el.setEL("video", extension.getVideo())
        el.setEL("support", extension.getSupport())
        el.setEL("documentation", extension.getDocumentation())
        el.setEL("forum", extension.getForum())
        el.setEL("mailinglist", extension.getMailinglist())
        el.setEL("network", extension.getNetwork())
        el.setEL("created", Caster.toString(extension.getCreated(), null))
    }

    @Throws(SecurityException::class)
    fun resetORMSetting() {
        val hasAccess: Boolean = ConfigWebUtil.hasAccess(config, SecurityManagerImpl.TYPE_ORM)
        if (!hasAccess) throw SecurityException("Access Denied to update ORM Settings")
        val orm: Struct? = _getRootElement("orm")
        if (root.containsKey("orm")) rem(root, "orm")
    }

    @Throws(SecurityException::class)
    fun updateORMSetting(oc: ORMConfiguration?) {
        val hasAccess: Boolean = ConfigWebUtil.hasAccess(config, SecurityManagerImpl.TYPE_ORM)
        if (!hasAccess) throw SecurityException("Access Denied to update ORM Settings")
        val orm: Struct? = _getRootElement("orm")
        orm.setEL("autogenmap", Caster.toString(oc.autogenmap(), "true"))
        orm.setEL("eventHandler", Caster.toString(oc.eventHandler(), ""))
        orm.setEL("eventHandling", Caster.toString(oc.eventHandling(), "false"))
        orm.setEL("namingStrategy", Caster.toString(oc.namingStrategy(), ""))
        orm.setEL("flushAtRequestEnd", Caster.toString(oc.flushAtRequestEnd(), "true"))
        orm.setEL("cacheProvider", Caster.toString(oc.getCacheProvider(), ""))
        orm.setEL("cacheConfig", Caster.toString(oc.getCacheConfig(), "true"))
        orm.setEL("catalog", Caster.toString(oc.getCatalog(), ""))
        orm.setEL("dbCreate", ORMConfigurationImpl.dbCreateAsString(oc.getDbCreate()))
        orm.setEL("dialect", Caster.toString(oc.getDialect(), ""))
        orm.setEL("schema", Caster.toString(oc.getSchema(), ""))
        orm.setEL("logSql", Caster.toString(oc.logSQL(), "false"))
        orm.setEL("saveMapping", Caster.toString(oc.saveMapping(), "false"))
        orm.setEL("secondaryCacheEnable", Caster.toString(oc.secondaryCacheEnabled(), "false"))
        orm.setEL("useDbForMapping", Caster.toString(oc.useDBForMapping(), "true"))
        orm.setEL("ormConfig", Caster.toString(oc.getOrmConfig(), ""))
        orm.setEL("sqlCcript", Caster.toString(oc.getSqlScript(), "true"))
        if (oc.isDefaultCfcLocation()) {
            rem(orm, "cfcLocation")
        } else {
            val locations: Array<Resource?> = oc.getCfcLocations()
            val sb = StringBuilder()
            for (i in locations.indices) {
                if (i != 0) sb.append(",")
                sb.append(locations[i].getAbsolutePath())
            }
            orm.setEL("cfcLocation", sb.toString())
        }
        orm.setEL("sqlScript", Caster.toString(oc.getSqlScript(), "true"))
    }

    @Throws(PageException::class)
    fun removeRHExtension(id: String?) {
        checkWriteAccess()
        if (StringUtil.isEmpty(id, true)) return
        val children: Array = ConfigWebUtil.getAsArray("extensions", root)
        val keys: IntArray = children.intKeys()
        var child: Struct
        var rhe: RHExtension?
        var key: Int
        for (i in keys.indices.reversed()) {
            key = keys[i]
            child = Caster.toStruct(children.get(key, null), null)
            if (child == null) continue
            try {
                rhe = RHExtension(config, Caster.toString(child.get(KeyConstants._id), null), Caster.toString(child.get(KeyConstants._version), null), null, false)
            } catch (t: Throwable) {
                ExceptionUtil.rethrowIfNecessary(t)
                continue
            }
            if (id.equalsIgnoreCase(rhe.getId()) || id.equalsIgnoreCase(rhe.getSymbolicName())) {
                removeRHExtension(config, rhe, null, true)
                children.removeEL(key)
            }
        }
    }

    @Throws(PageException::class)
    fun removeExtension(provider: String?, id: String?) {
        removeRHExtension(id)
    }

    @Throws(PageException::class)
    fun updateArchive(config: Config?, archive: Resource?) {
        val logger: Log = ThreadLocalPageContext.getLog(config, "deploy")
        var type: String? = null
        var virtual: String? = null
        var name: String? = null
        val readOnly: Boolean
        val topLevel: Boolean
        val hidden: Boolean
        val physicalFirst: Boolean
        var inspect: Short
        val listMode: Int
        val listType: Int
        var `is`: InputStream? = null
        var file: ZipFile? = null
        try {
            file = ZipFile(FileWrapper.toFile(archive))
            val entry: ZipEntry = file.getEntry("META-INF/MANIFEST.MF")

            // no manifest
            if (entry == null) {
                DeployHandler.moveToFailedFolder(config.getDeployDirectory(), archive)
                throw ApplicationException("Cannot deploy " + Constants.NAME.toString() + " Archive [" + archive.toString() + "], file is to old, the file does not have a MANIFEST.")
            }
            `is` = file.getInputStream(entry)
            val manifest = Manifest(`is`)
            val attr: Attributes = manifest.getMainAttributes()

            // id = unwrap(attr.getValue("mapping-id"));
            type = StringUtil.unwrap(attr.getValue("mapping-type"))
            virtual = StringUtil.unwrap(attr.getValue("mapping-virtual-path"))
            name = ListUtil.trim(virtual, "/")
            readOnly = Caster.toBooleanValue(StringUtil.unwrap(attr.getValue("mapping-readonly")), false)
            topLevel = Caster.toBooleanValue(StringUtil.unwrap(attr.getValue("mapping-top-level")), false)
            listMode = ConfigWebUtil.toListenerMode(StringUtil.unwrap(attr.getValue("mapping-listener-mode")), -1)
            listType = ConfigWebUtil.toListenerType(StringUtil.unwrap(attr.getValue("mapping-listener-type")), -1)
            inspect = ConfigWebUtil.inspectTemplate(StringUtil.unwrap(attr.getValue("mapping-inspect")), Config.INSPECT_UNDEFINED)
            if (inspect == Config.INSPECT_UNDEFINED) {
                val trusted: Boolean = Caster.toBoolean(StringUtil.unwrap(attr.getValue("mapping-trusted")), null)
                if (trusted != null) {
                    inspect = if (trusted.booleanValue()) Config.INSPECT_NEVER else Config.INSPECT_ALWAYS
                }
            }
            hidden = Caster.toBooleanValue(StringUtil.unwrap(attr.getValue("mapping-hidden")), false)
            physicalFirst = Caster.toBooleanValue(StringUtil.unwrap(attr.getValue("mapping-physical-first")), false)
        } catch (t: Throwable) {
            ExceptionUtil.rethrowIfNecessary(t)
            DeployHandler.moveToFailedFolder(config.getDeployDirectory(), archive)
            throw Caster.toPageException(t)
        } finally {
            try {
                IOUtil.close(`is`)
            } catch (e: IOException) {
                throw Caster.toPageException(e)
            }
            ZipUtil.close(file)
        }
        try {
            val trgDir: Resource = config.getConfigDir().getRealResource("archives").getRealResource(type).getRealResource(name)
            val trgFile: Resource = trgDir.getRealResource(archive.getName())
            trgDir.mkdirs()

            // delete existing files
            ResourceUtil.deleteContent(trgDir, null)
            ResourceUtil.moveTo(archive, trgFile, true)
            logger.log(Log.LEVEL_INFO, "archive", "Add " + type + " mapping [" + virtual + "] with archive [" + trgFile.getAbsolutePath() + "]")
            if ("regular".equalsIgnoreCase(type)) _updateMapping(virtual, null, trgFile.getAbsolutePath(), "archive", inspect, topLevel, listMode, listType, readOnly) else if ("cfc".equalsIgnoreCase(type)) _updateComponentMapping(virtual, null, trgFile.getAbsolutePath(), "archive", inspect) else if ("ct".equalsIgnoreCase(type)) _updateCustomTag(virtual, null, trgFile.getAbsolutePath(), "archive", inspect)
        } catch (t: Throwable) {
            ExceptionUtil.rethrowIfNecessary(t)
            DeployHandler.moveToFailedFolder(config.getDeployDirectory(), archive)
            throw Caster.toPageException(t)
        }
    }

    @Throws(PageException::class)
    fun updateRHExtension(config: Config?, ext: Resource?, reload: Boolean, force: Boolean) {
        val rhext: RHExtension?
        try {
            rhext = RHExtension(config, ext, true)
            rhext.validate()
        } catch (t: Throwable) {
            ExceptionUtil.rethrowIfNecessary(t)
            DeployHandler.moveToFailedFolder(ext.getParentResource(), ext)
            throw Caster.toPageException(t)
        }
        updateRHExtension(config, rhext, reload, force)
    }

    @Throws(PageException::class)
    fun updateRHExtension(config: Config?, rhext: RHExtension?, reload: Boolean, force: Boolean) {
        try {
            if (!force && hasRHExtensions(config as ConfigPro?, rhext.toExtensionDefinition()) != null) {
                throw ApplicationException("the extension " + rhext.getName().toString() + " (id: " + rhext.getId().toString() + ") in version " + rhext.getVersion().toString() + " is already installed")
            }
        } catch (e: Exception) {
            throw Caster.toPageException(e)
        }
        val ci: ConfigPro? = config
        val logger: Log = ThreadLocalPageContext.getLog(ci, "deploy")
        val type = if (ci is ConfigWeb) "web" else "server"
        // load already installed previous version and uninstall the parts no longer needed
        val existingRH: RHExtension? = getRHExtension(ci, rhext.getId(), null)
        if (existingRH != null) {
            // same version
            if (existingRH.getVersion().compareTo(rhext.getVersion()) === 0) {
                removeRHExtension(config, existingRH, rhext, false)
            } else removeRHExtension(config, existingRH, rhext, true)
        }
        // INSTALL
        try {

            // boolean clearTags=false,clearFunction=false;
            var reloadNecessary = false

            // store to xml
            val existing: Array<BundleDefinition?>? = _updateExtension(ci, rhext)
            // _storeAndReload();
            // this must happen after "store"
            cleanBundles(rhext, ci, existing) // clean after populating the new ones
            // ConfigWebAdmin.updateRHExtension(ci,rhext);
            val zis = ZipInputStream(IOUtil.toBufferedInputStream(rhext.getExtensionFile().getInputStream()))
            var entry: ZipEntry?
            var path: String
            var fileName: String?
            while (zis.getNextEntry().also { entry = it } != null) {
                path = entry.getName()
                fileName = fileName(entry)
                // jars
                if (!entry.isDirectory() && (startsWith(path, type, "jars") || startsWith(path, type, "jar") || startsWith(path, type, "bundles")
                                || startsWith(path, type, "bundle") || startsWith(path, type, "lib") || startsWith(path, type, "libs")) && StringUtil.endsWithIgnoreCase(path, ".jar")) {
                    val obj: Object? = installBundle(config, zis, fileName, rhext.getVersion(), false, false)
                    // jar is not a bundle, only a regular jar
                    if (obj !is BundleFile) {
                        val tmp: Resource? = obj as Resource?
                        val tmpJar: Resource = tmp.getParentResource().getRealResource(ListUtil.last(path, "\\/"))
                        tmp.moveTo(tmpJar)
                        updateJar(config, tmpJar, false)
                    }
                }

                // flds
                if (!entry.isDirectory() && startsWith(path, type, "flds") && (StringUtil.endsWithIgnoreCase(path, ".fld") || StringUtil.endsWithIgnoreCase(path, ".fldx"))) {
                    logger.log(Log.LEVEL_DEBUG, "extension", "Deploy fld [$fileName]")
                    updateFLD(zis, fileName, false)
                    reloadNecessary = true
                }
                // tlds
                if (!entry.isDirectory() && startsWith(path, type, "tlds") && (StringUtil.endsWithIgnoreCase(path, ".tld") || StringUtil.endsWithIgnoreCase(path, ".tldx"))) {
                    logger.log(Log.LEVEL_DEBUG, "extension", "Deploy tld/tldx [$fileName]")
                    updateTLD(zis, fileName, false)
                    reloadNecessary = true
                }

                // tags
                if (!entry.isDirectory() && startsWith(path, type, "tags")) {
                    val sub = subFolder(entry)
                    logger.log(Log.LEVEL_DEBUG, "extension", "Deploy tag [$sub]")
                    updateTag(zis, sub, false)
                    // clearTags=true;
                    reloadNecessary = true
                }

                // functions
                if (!entry.isDirectory() && startsWith(path, type, "functions")) {
                    val sub = subFolder(entry)
                    logger.log(Log.LEVEL_DEBUG, "extension", "Deploy function [$sub]")
                    updateFunction(zis, sub, false)
                    // clearFunction=true;
                    reloadNecessary = true
                }

                // mappings
                if (!entry.isDirectory() && (startsWith(path, type, "archives") || startsWith(path, type, "mappings"))) {
                    val sub = subFolder(entry)
                    logger.log(Log.LEVEL_DEBUG, "extension", "deploy mapping $sub")
                    updateArchive(zis, sub, false)
                    reloadNecessary = true
                    // clearFunction=true;
                }

                // event-gateway
                if (!entry.isDirectory() && (startsWith(path, type, "event-gateways") || startsWith(path, type, "eventGateways"))
                        && (StringUtil.endsWithIgnoreCase(path, "." + Constants.getCFMLComponentExtension())
                                || StringUtil.endsWithIgnoreCase(path, "." + Constants.getLuceeComponentExtension()))) {
                    val sub = subFolder(entry)
                    logger.log(Log.LEVEL_DEBUG, "extension", "Deploy event-gateway [$sub]")
                    updateEventGateway(zis, sub, false)
                }

                // context
                var realpath: String?
                if (!entry.isDirectory() && startsWith(path, type, "context") && !StringUtil.startsWith(fileName(entry), '.')) {
                    realpath = path.substring(8)
                    logger.log(Log.LEVEL_DEBUG, "extension", "Deploy context [$realpath]")
                    updateContext(zis, realpath, false, false)
                }
                // web contextS
                var first: Boolean
                if (!entry.isDirectory() && (startsWith(path, type, "webcontexts").also { first = it } || startsWith(path, type, "web.contexts"))
                        && !StringUtil.startsWith(fileName(entry), '.')) {
                    realpath = path.substring(if (first) 12 else 13)
                    logger.log(Log.LEVEL_DEBUG, "extension", "Deploy webcontext [$realpath]")
                    updateWebContexts(zis, realpath, false, false)
                }
                // applications
                if (!entry.isDirectory() && (startsWith(path, type, "applications") || startsWith(path, type, "web.applications") || startsWith(path, type, "web"))
                        && !StringUtil.startsWith(fileName(entry), '.')) {
                    var index: Int
                    index = if (startsWith(path, type, "applications")) 13 else if (startsWith(path, type, "web.applications")) 17 else 4 // web
                    realpath = path.substring(index)
                    logger.log(Log.LEVEL_DEBUG, "extension", "Deploy application [$realpath]")
                    updateApplication(zis, realpath, false)
                }
                // configs
                if (!entry.isDirectory() && startsWith(path, type, "config") && !StringUtil.startsWith(fileName(entry), '.')) {
                    realpath = path.substring(7)
                    logger.log(Log.LEVEL_DEBUG, "extension", "Deploy config [$realpath]")
                    updateConfigs(zis, realpath, false, false)
                }
                // components
                if (!entry.isDirectory() && startsWith(path, type, "components") && !StringUtil.startsWith(fileName(entry), '.')) {
                    realpath = path.substring(11)
                    logger.log(Log.LEVEL_DEBUG, "extension", "Deploy component [$realpath]")
                    updateComponent(zis, realpath, false, false)
                }

                // plugins
                if (!entry.isDirectory() && startsWith(path, type, "plugins") && !StringUtil.startsWith(fileName(entry), '.')) {
                    realpath = path.substring(8)
                    logger.log(Log.LEVEL_INFO, "extension", "Deploy plugin [$realpath]")
                    updatePlugin(zis, realpath, false)
                }
                zis.closeEntry()
            }
            ////////////////////////////////////////////

            // load the bundles
            if (rhext.getStartBundles()) {
                rhext.deployBundles(ci)
                val bfs: Array<BundleInfo?> = rhext.getBundles()
                if (bfs != null) {
                    for (bf in bfs) {
                        OSGiUtil.loadBundleFromLocal(bf.getSymbolicName(), bf.getVersion(), null, false, null)
                    }
                }
            }

            // update cache
            if (!ArrayUtil.isEmpty(rhext.getCaches())) {
                val itl: Iterator<Map<String?, String?>?> = rhext.getCaches().iterator()
                var map: Map<String?, String?>?
                while (itl.hasNext()) {
                    map = itl.next()
                    val cd: ClassDefinition = RHExtension.toClassDefinition(config, map, null)
                    if (cd != null && cd.isBundle()) {
                        _updateCache(cd)
                        reloadNecessary = true
                    }
                    logger.info("extension", "Update cache [" + cd + "] from extension [" + rhext.getName() + ":" + rhext.getVersion() + "]")
                }
            }

            // update cache handler
            if (!ArrayUtil.isEmpty(rhext.getCacheHandlers())) {
                val itl: Iterator<Map<String?, String?>?> = rhext.getCacheHandlers().iterator()
                var map: Map<String?, String?>?
                while (itl.hasNext()) {
                    map = itl.next()
                    val cd: ClassDefinition = RHExtension.toClassDefinition(config, map, null)
                    val _id = map!!["id"]
                    if (!StringUtil.isEmpty(_id) && cd != null && cd.hasClass()) {
                        _updateCacheHandler(_id, cd)
                        reloadNecessary = true
                    }
                    logger.info("extension", "Update cache handler [" + cd + "] from extension [" + rhext.getName() + ":" + rhext.getVersion() + "]")
                }
            }

            // update Search
            if (!ArrayUtil.isEmpty(rhext.getSearchs())) {
                val itl: Iterator<Map<String?, String?>?> = rhext.getSearchs().iterator()
                var map: Map<String?, String?>?
                while (itl.hasNext()) {
                    map = itl.next()
                    val cd: ClassDefinition = RHExtension.toClassDefinition(config, map, null)
                    if (cd != null && cd.hasClass()) {
                        _updateSearchEngine(cd)
                        reloadNecessary = true
                    }
                    logger.info("extension", "Update search engine [" + cd + "] from extension [" + rhext.getName() + ":" + rhext.getVersion() + "]")
                }
            }

            // update Resource
            if (!ArrayUtil.isEmpty(rhext.getResources())) {
                val itl: Iterator<Map<String?, String?>?> = rhext.getResources().iterator()
                var map: Map<String?, String?>?
                while (itl.hasNext()) {
                    map = itl.next()
                    val cd: ClassDefinition = RHExtension.toClassDefinition(config, map, null)
                    val scheme = map!!["scheme"]
                    if (cd != null && cd.hasClass() && !StringUtil.isEmpty(scheme)) {
                        val args: Struct = StructImpl(Struct.TYPE_LINKED)
                        copyButIgnoreClassDef(map, args)
                        args.remove("scheme")
                        _updateResourceProvider(scheme, cd, args)
                        reloadNecessary = true
                    }
                    logger.info("extension", "Update resource provider [" + scheme + "] from extension [" + rhext.getName() + ":" + rhext.getVersion() + "]")
                }
            }

            // update orm
            if (!ArrayUtil.isEmpty(rhext.getOrms())) {
                val itl: Iterator<Map<String?, String?>?> = rhext.getOrms().iterator()
                var map: Map<String?, String?>?
                while (itl.hasNext()) {
                    map = itl.next()
                    val cd: ClassDefinition = RHExtension.toClassDefinition(config, map, null)
                    if (cd != null && cd.hasClass()) {
                        _updateORMEngine(cd)
                        reloadNecessary = true
                    }
                    logger.info("extension", "Update orm engine [" + cd + "] from extension [" + rhext.getName() + ":" + rhext.getVersion() + "]")
                }
            }

            // update webservice
            if (!ArrayUtil.isEmpty(rhext.getWebservices())) {
                val itl: Iterator<Map<String?, String?>?> = rhext.getWebservices().iterator()
                var map: Map<String?, String?>?
                while (itl.hasNext()) {
                    map = itl.next()
                    val cd: ClassDefinition = RHExtension.toClassDefinition(config, map, null)
                    if (cd != null && cd.hasClass()) {
                        _updateWebserviceHandler(cd)
                        reloadNecessary = true
                    }
                    logger.info("extension", "Update webservice handler [" + cd + "] from extension [" + rhext.getName() + ":" + rhext.getVersion() + "]")
                }
            }

            // update monitor
            if (!ArrayUtil.isEmpty(rhext.getMonitors())) {
                val itl: Iterator<Map<String?, String?>?> = rhext.getMonitors().iterator()
                var map: Map<String?, String?>?
                while (itl.hasNext()) {
                    map = itl.next()
                    val cd: ClassDefinition = RHExtension.toClassDefinition(config, map, null)
                    if (cd != null && cd.hasClass()) {
                        _updateMonitorEnabled(true)
                        _updateMonitor(cd, map!!["type"], map["name"], true)
                        reloadNecessary = true
                    }
                    logger.info("extension", "Update monitor engine [" + cd + "] from extension [" + rhext.getName() + ":" + rhext.getVersion() + "]")
                }
            }

            // update jdbc
            if (!ArrayUtil.isEmpty(rhext.getJdbcs())) {
                val itl: Iterator<Map<String?, String?>?> = rhext.getJdbcs().iterator()
                var map: Map<String?, String?>?
                while (itl.hasNext()) {
                    map = itl.next()
                    val cd: ClassDefinition = RHExtension.toClassDefinition(config, map, null)
                    val _label = map!!["label"]
                    val _id = map["id"]
                    if (cd != null && cd.isBundle()) {
                        _updateJDBCDriver(_label, _id, cd)
                        reloadNecessary = true
                    }
                    logger.info("extension", "Update JDBC Driver [" + _label + ":" + cd + "] from extension [" + rhext.getName() + ":" + rhext.getVersion() + "]")
                }
            }

            // update startup hook
            if (!ArrayUtil.isEmpty(rhext.getStartupHooks())) {
                val itl: Iterator<Map<String?, String?>?> = rhext.getStartupHooks().iterator()
                var map: Map<String?, String?>?
                while (itl.hasNext()) {
                    map = itl.next()
                    val cd: ClassDefinition = RHExtension.toClassDefinition(config, map, null)
                    if (cd != null && cd.isBundle()) {
                        _updateStartupHook(cd)
                        reloadNecessary = true
                    }
                    logger.info("extension", "Update Startup Hook [" + cd + "] from extension [" + rhext.getName() + ":" + rhext.getVersion() + "]")
                }
            }

            // update mapping
            if (!ArrayUtil.isEmpty(rhext.getMappings())) {
                val itl: Iterator<Map<String?, String?>?> = rhext.getMappings().iterator()
                var map: Map<String?, String?>?
                var virtual: String?
                var physical: String?
                var archive: String?
                var primary: String?
                var inspect: Short
                var lmode: Int
                var ltype: Int
                var toplevel: Boolean
                var readonly: Boolean
                while (itl.hasNext()) {
                    map = itl.next()
                    virtual = map!!["virtual"]
                    physical = map["physical"]
                    archive = map["archive"]
                    primary = map["primary"]
                    inspect = ConfigWebUtil.inspectTemplate(map["inspect"], Config.INSPECT_UNDEFINED)
                    lmode = ConfigWebUtil.toListenerMode(map["listener-mode"], -1)
                    ltype = ConfigWebUtil.toListenerType(map["listener-type"], -1)
                    toplevel = Caster.toBooleanValue(map["toplevel"], false)
                    readonly = Caster.toBooleanValue(map["readonly"], false)
                    _updateMapping(virtual, physical, archive, primary, inspect, toplevel, lmode, ltype, readonly)
                    reloadNecessary = true
                    logger.debug("extension", "Update Mapping [$virtual]")
                }
            }

            // update event-gateway-instance
            if (!ArrayUtil.isEmpty(rhext.getEventGatewayInstances())) {
                val itl: Iterator<Map<String?, Object?>?> = rhext.getEventGatewayInstances().iterator()
                var map: Map<String?, Object?>?
                while (itl.hasNext()) {
                    map = itl.next()
                    // id
                    val id: String = Caster.toString(map!!["id"], null)
                    // class
                    val cd: ClassDefinition = RHExtension.toClassDefinition(config, map, null)
                    // component path
                    var cfcPath: String = Caster.toString(map["cfcPath"], null)
                    if (StringUtil.isEmpty(cfcPath)) cfcPath = Caster.toString(map["componentPath"], null)
                    // listener component path
                    var listenerCfcPath: String = Caster.toString(map["listenerCFCPath"], null)
                    if (StringUtil.isEmpty(listenerCfcPath)) listenerCfcPath = Caster.toString(map["listenerComponentPath"], null)
                    // startup mode
                    val strStartupMode: String = Caster.toString(map["startupMode"], "automatic")
                    val startupMode: Int = GatewayEntryImpl.toStartup(strStartupMode, GatewayEntryImpl.STARTUP_MODE_AUTOMATIC)
                    // read only
                    val readOnly: Boolean = Caster.toBooleanValue(map["readOnly"], false)
                    // custom
                    val custom: Struct = Caster.toStruct(map["custom"], null)
                    /*
					 * print.e("::::::::::::::::::::::::::::::::::::::::::"); print.e("id:"+id); print.e("cd:"+cd);
					 * print.e("cfc:"+cfcPath); print.e("listener:"+listenerCfcPath);
					 * print.e("startupMode:"+startupMode); print.e(custom);
					 */if (!StringUtil.isEmpty(id) && (!StringUtil.isEmpty(cfcPath) || cd != null && cd.hasClass())) {
                        _updateGatewayEntry(id, cd, cfcPath, listenerCfcPath, startupMode, custom, readOnly)
                    }
                    logger.info("extension", "Update event gateway entry [" + id + "] from extension [" + rhext.getName() + ":" + rhext.getVersion() + "]")
                }
            }

            // reload
            // if(reloadNecessary){
            reloadNecessary = true
            if (reload && reloadNecessary) _storeAndReload() else _store()
            // }
        } catch (t: Throwable) {
            ExceptionUtil.rethrowIfNecessary(t)
            DeployHandler.moveToFailedFolder(rhext.getExtensionFile().getParentResource(), rhext.getExtensionFile())
            try {
                removeRHExtensions(config as ConfigPro?, ThreadLocalPageContext.getLog(config, "deploy"), arrayOf(rhext.getId()), false)
            } catch (t2: Throwable) {
                ExceptionUtil.rethrowIfNecessary(t2)
            }
            throw Caster.toPageException(t)
        }
    }

    private fun copyButIgnoreClassDef(src: Map<String?, String?>?, trg: Struct?) {
        val it: Iterator<Entry<String?, String?>?> = src.entrySet().iterator()
        var e: Entry<String?, String?>?
        var name: String
        while (it.hasNext()) {
            e = it.next()
            name = e.getKey()
            if ("class".equals(name) || "bundle-name".equals(name) || "bundlename".equals(name) || "bundleName".equals(name) || "bundle-version".equals(name)
                    || "bundleversion".equals(name) || "bundleVersion".equals(name)) continue
            trg.setEL(name, e.getValue())
        }
    }

    /**
     * removes an installed extension from the system
     *
     * @param config
     * @param rhe extension to remove
     * @param replacementRH the extension that will replace this extension, so do not remove parts
     * defined in this extension.
     * @throws PageException
     */
    @Throws(PageException::class)
    private fun removeRHExtension(config: Config?, rhe: RHExtension?, replacementRH: RHExtension?, deleteExtension: Boolean) {
        val ci: ConfigPro? = config
        val logger: Log = ThreadLocalPageContext.getLog(ci, "deploy")

        // MUST check replacementRH everywhere
        try {
            // remove the bundles
            var candidatesToRemove: Array<BundleDefinition?> = OSGiUtil.toBundleDefinitions(rhe.getBundles(EMPTY))
            if (replacementRH != null) {
                // spare bundles used in the new extension as well
                val notRemove: Map<String?, BundleDefinition?>? = toMap(OSGiUtil.toBundleDefinitions(replacementRH.getBundles(EMPTY)))
                val tmp: List<BundleDefinition?> = ArrayList<OSGiUtil.BundleDefinition?>()
                var key: String
                for (i in candidatesToRemove.indices) {
                    key = candidatesToRemove[i].getName().toString() + "|" + candidatesToRemove[i].getVersionAsString()
                    if (notRemove!!.containsKey(key)) continue
                    tmp.add(candidatesToRemove[i])
                }
                candidatesToRemove = tmp.toArray(arrayOfNulls<BundleDefinition?>(tmp.size()))
            }
            cleanBundles(rhe, ci, candidatesToRemove)

            // FLD
            removeFLDs(logger, rhe.getFlds()) // MUST check if others use one of this fld

            // TLD
            removeTLDs(logger, rhe.getTlds()) // MUST check if others use one of this tld

            // Tag
            removeTags(logger, rhe.getTags())

            // Functions
            removeFunctions(logger, rhe.getFunctions())

            // Event Gateway
            removeEventGateways(logger, rhe.getEventGateways())

            // context
            removeContext(config, false, logger, rhe.getContexts()) // MUST check if others use one of this

            // web contextS
            removeWebContexts(config, false, logger, rhe.getWebContexts()) // MUST check if others use one of this

            // applications
            removeApplications(config, logger, rhe.getApplications()) // MUST check if others use one of this

            // plugins
            removePlugins(config, logger, rhe.getPlugins()) // MUST check if others use one of this

            // remove cache handler
            if (!ArrayUtil.isEmpty(rhe.getCacheHandlers())) {
                val itl: Iterator<Map<String?, String?>?> = rhe.getCacheHandlers().iterator()
                var map: Map<String?, String?>?
                while (itl.hasNext()) {
                    map = itl.next()
                    val cd: ClassDefinition = RHExtension.toClassDefinition(config, map, null)
                    val _id = map!!["id"]
                    if (!StringUtil.isEmpty(_id) && cd != null && cd.hasClass()) {
                        _removeCacheHandler(_id)
                        // reload=true;
                    }
                    logger.info("extension", "Remove cache handler [" + cd + "] from extension [" + rhe.getName() + ":" + rhe.getVersion() + "]")
                }
            }

            // remove cache
            if (!ArrayUtil.isEmpty(rhe.getCaches())) {
                val itl: Iterator<Map<String?, String?>?> = rhe.getCaches().iterator()
                var map: Map<String?, String?>?
                while (itl.hasNext()) {
                    map = itl.next()
                    val cd: ClassDefinition = RHExtension.toClassDefinition(config, map, null)
                    if (cd != null && cd.isBundle()) {
                        _removeCache(cd)
                        // reload=true;
                    }
                    logger.info("extension", "Remove cache handler [" + cd + "] from extension [" + rhe.getName() + ":" + rhe.getVersion() + "]")
                }
            }

            // remove Search
            if (!ArrayUtil.isEmpty(rhe.getSearchs())) {
                val itl: Iterator<Map<String?, String?>?> = rhe.getSearchs().iterator()
                var map: Map<String?, String?>?
                while (itl.hasNext()) {
                    map = itl.next()
                    val cd: ClassDefinition = RHExtension.toClassDefinition(config, map, null)
                    if (cd != null && cd.hasClass()) {
                        _removeSearchEngine()
                        // reload=true;
                    }
                    logger.info("extension", "Remove search engine [" + cd + "] from extension [" + rhe.getName() + ":" + rhe.getVersion() + "]")
                }
            }

            // remove resource
            if (!ArrayUtil.isEmpty(rhe.getResources())) {
                val itl: Iterator<Map<String?, String?>?> = rhe.getResources().iterator()
                var map: Map<String?, String?>?
                while (itl.hasNext()) {
                    map = itl.next()
                    val cd: ClassDefinition = RHExtension.toClassDefinition(config, map, null)
                    val scheme = map!!["scheme"]
                    if (cd != null && cd.hasClass()) {
                        _removeResourceProvider(scheme)
                    }
                    logger.info("extension", "Remove resource [" + cd + "] from extension [" + rhe.getName() + ":" + rhe.getVersion() + "]")
                }
            }

            // remove orm
            if (!ArrayUtil.isEmpty(rhe.getOrms())) {
                val itl: Iterator<Map<String?, String?>?> = rhe.getOrms().iterator()
                var map: Map<String?, String?>?
                while (itl.hasNext()) {
                    map = itl.next()
                    val cd: ClassDefinition = RHExtension.toClassDefinition(config, map, null)
                    if (cd != null && cd.hasClass()) {
                        _removeORMEngine()
                        // reload=true;
                    }
                    logger.info("extension", "Remove orm engine [" + cd + "] from extension [" + rhe.getName() + ":" + rhe.getVersion() + "]")
                }
            }

            // remove webservice
            if (!ArrayUtil.isEmpty(rhe.getWebservices())) {
                val itl: Iterator<Map<String?, String?>?> = rhe.getWebservices().iterator()
                var map: Map<String?, String?>?
                while (itl.hasNext()) {
                    map = itl.next()
                    val cd: ClassDefinition = RHExtension.toClassDefinition(config, map, null)
                    if (cd != null && cd.hasClass()) {
                        _removeWebserviceHandler()
                        // reload=true;
                    }
                    logger.info("extension", "Remove webservice handler [" + cd + "] from extension [" + rhe.getName() + ":" + rhe.getVersion() + "]")
                }
            }

            // remove monitor
            if (!ArrayUtil.isEmpty(rhe.getMonitors())) {
                val itl: Iterator<Map<String?, String?>?> = rhe.getMonitors().iterator()
                var map: Map<String?, String?>?
                var name: String?
                while (itl.hasNext()) {
                    map = itl.next()

                    // ClassDefinition cd = RHExtension.toClassDefinition(config,map);

                    // if(cd.hasClass()) {
                    _removeMonitor(map!!["type"], map["name"].also { name = it })
                    // reload=true;
                    // }
                    logger.info("extension", "Remove monitor [" + name + "] from extension [" + rhe.getName() + ":" + rhe.getVersion() + "]")
                }
            }

            // remove jdbc
            if (!ArrayUtil.isEmpty(rhe.getJdbcs())) {
                val itl: Iterator<Map<String?, String?>?> = rhe.getJdbcs().iterator()
                var map: Map<String?, String?>?
                while (itl.hasNext()) {
                    map = itl.next()
                    val cd: ClassDefinition = RHExtension.toClassDefinition(config, map, null)
                    if (cd != null && cd.isBundle()) {
                        _removeJDBCDriver(cd)
                    }
                    logger.info("extension", "Remove JDBC Driver [" + cd + "] from extension [" + rhe.getName() + ":" + rhe.getVersion() + "]")
                }
            }

            // remove startup hook
            if (!ArrayUtil.isEmpty(rhe.getStartupHooks())) {
                val itl: Iterator<Map<String?, String?>?> = rhe.getStartupHooks().iterator()
                var map: Map<String?, String?>?
                while (itl.hasNext()) {
                    map = itl.next()
                    val cd: ClassDefinition = RHExtension.toClassDefinition(config, map, null)
                    if (cd != null && cd.isBundle()) {
                        _removeStartupHook(cd)
                    }
                    logger.info("extension", "Remove Startup Hook [" + cd + "] from extension [" + rhe.getName() + ":" + rhe.getVersion() + "]")
                }
            }

            // remove mapping
            if (!ArrayUtil.isEmpty(rhe.getMappings())) {
                val itl: Iterator<Map<String?, String?>?> = rhe.getMappings().iterator()
                var map: Map<String?, String?>?
                var virtual: String?
                while (itl.hasNext()) {
                    map = itl.next()
                    virtual = map!!["virtual"]
                    _removeMapping(virtual)
                    logger.info("extension", "remove Mapping [$virtual]")
                }
            }

            // remove event-gateway-instance
            if (!ArrayUtil.isEmpty(rhe.getEventGatewayInstances())) {
                val itl: Iterator<Map<String?, Object?>?> = rhe.getEventGatewayInstances().iterator()
                var map: Map<String?, Object?>?
                var id: String
                while (itl.hasNext()) {
                    map = itl.next()
                    id = Caster.toString(map!!["id"], null)
                    if (!StringUtil.isEmpty(id)) {
                        _removeGatewayEntry(id)
                        logger.info("extension", "remove event gateway entry [$id]")
                    }
                }
            }

            // Loop Files
            val zis = ZipInputStream(IOUtil.toBufferedInputStream(rhe.getExtensionFile().getInputStream()))
            val type = if (ci is ConfigWeb) "web" else "server"
            try {
                var entry: ZipEntry?
                var path: String
                var fileName: String?
                var tmp: Resource
                while (zis.getNextEntry().also { entry = it } != null) {
                    path = entry.getName()
                    fileName = fileName(entry)

                    // archives
                    if (!entry.isDirectory() && (startsWith(path, type, "archives") || startsWith(path, type, "mappings"))) {
                        val sub = subFolder(entry)
                        logger.log(Log.LEVEL_INFO, "extension", "Remove archive [$sub] registered as a mapping")
                        tmp = SystemUtil.getTempFile(".lar", false)
                        IOUtil.copy(zis, tmp, false)
                        removeArchive(tmp)
                    }
                    zis.closeEntry()
                }
            } finally {
                IOUtil.close(zis)
            }

            // now we can delete the extension
            if (deleteExtension) rhe.getExtensionFile().delete()
        } catch (t: Throwable) {
            ExceptionUtil.rethrowIfNecessary(t)
            // failed to uninstall, so we install it again
            try {
                updateRHExtension(config, rhe.getExtensionFile(), true, true)
                // RHExtension.install(config, rhe.getExtensionFile());
            } catch (t2: Throwable) {
                ExceptionUtil.rethrowIfNecessary(t2)
            }
            throw Caster.toPageException(t)
        }
    }

    private fun toMap(bundleDefinitions: Array<BundleDefinition?>?): Map<String?, BundleDefinition?>? {
        val rtn: Map<String?, BundleDefinition?> = HashMap<String?, OSGiUtil.BundleDefinition?>()
        for (i in bundleDefinitions.indices) {
            rtn.put(bundleDefinitions!![i].getName().toString() + "|" + bundleDefinitions[i].getVersionAsString(), bundleDefinitions[i])
        }
        return rtn
    }

    @Throws(PageException::class)
    fun verifyExtensionProvider(strUrl: String?) {
        var method: HTTPResponse? = null
        method = try {
            val url: URL = HTTPUtil.toURL(strUrl.toString() + "?wsdl", HTTPUtil.ENCODED_AUTO)
            HTTPEngine.get(url, null, null, 2000, true, null, null, null, null)
        } catch (e: MalformedURLException) {
            throw ApplicationException("Url definition [$strUrl] is invalid")
        } catch (e: IOException) {
            throw ApplicationException("Can't invoke [$strUrl]", e.getMessage())
        }
        if (method.getStatusCode() !== 200) {
            val code: Int = method.getStatusCode()
            val text: String = method.getStatusText()
            val msg = "$code $text"
            throw HTTPException(msg, null, code, text, method.getURL())
        }
        // Object o =
        CreateObject.doWebService(null, strUrl.toString() + "?wsdl")
        HTTPEngine.closeEL(method)
    }

    @Throws(IOException::class)
    fun updateTLD(resTld: Resource?) {
        updateLD(config.getTldFile(), resTld)
    }

    @Throws(IOException::class)
    fun updateFLD(resFld: Resource?) {
        updateLD(config.getFldFile(), resFld)
    }

    @Throws(IOException::class)
    private fun updateLD(dir: Resource?, res: Resource?) {
        if (!dir.exists()) dir.createDirectory(true)
        val file: Resource = dir.getRealResource(res.getName())
        if (file.length() !== res.length()) {
            ResourceUtil.copy(res, file)
        }
    }

    @Throws(SecurityException::class)
    fun updateFilesystem(fldDefaultDirectory: String?, functionDefaultDirectory: String?, tagDefaultDirectory: String?, tldDefaultDirectory: String?,
                         functionAddionalDirectory: String?, tagAddionalDirectory: String?) {
        checkWriteAccess()
        val fs: Struct = ConfigWebUtil.getAsStruct("fileSystem", root)
        if (!StringUtil.isEmpty(fldDefaultDirectory, true)) {
            fs.setEL(KeyImpl.init("fldDefaultDirectory"), fldDefaultDirectory)
        }
        if (!StringUtil.isEmpty(functionDefaultDirectory, true)) {
            fs.setEL(KeyImpl.init("functionDefaultDirectory"), functionDefaultDirectory)
        }
        if (!StringUtil.isEmpty(tagDefaultDirectory, true)) {
            fs.setEL(KeyImpl.init("tagDefaultDirectory"), tagDefaultDirectory)
        }
        if (!StringUtil.isEmpty(tldDefaultDirectory, true)) {
            fs.setEL(KeyImpl.init("tldDefaultDirectory"), tldDefaultDirectory)
        }
        if (!StringUtil.isEmpty(functionAddionalDirectory, true)) {
            fs.setEL(KeyImpl.init("functionAddionalDirectory"), functionAddionalDirectory)
        }
        if (!StringUtil.isEmpty(tagAddionalDirectory, true)) {
            fs.setEL(KeyImpl.init("tagAddionalDirectory"), tagAddionalDirectory)
        }
    }

    @Throws(IOException::class)
    fun updateTLD(`is`: InputStream?, name: String?, closeStream: Boolean) {
        write(config.getTldFile(), `is`, name, closeStream)
    }

    @Throws(IOException::class)
    fun updateFLD(`is`: InputStream?, name: String?, closeStream: Boolean) {
        write(config.getFldFile(), `is`, name, closeStream)
    }

    @Throws(IOException::class)
    fun updateTag(`is`: InputStream?, name: String?, closeStream: Boolean) {
        write(config!!.getDefaultTagMapping().getPhysical(), `is`, name, closeStream)
    }

    @Throws(IOException::class)
    fun updateFunction(`is`: InputStream?, name: String?, closeStream: Boolean) {
        write(config!!.getDefaultFunctionMapping().getPhysical(), `is`, name, closeStream)
    }

    @Throws(IOException::class)
    fun updateEventGateway(`is`: InputStream?, name: String?, closeStream: Boolean) {
        write(config!!.getEventGatewayDirectory(), `is`, name, closeStream)
    }

    @Throws(IOException::class, PageException::class)
    fun updateArchive(`is`: InputStream?, name: String?, closeStream: Boolean) {
        val res: Resource? = write(SystemUtil.getTempDirectory(), `is`, name, closeStream)
        // Resource res = write(DeployHandler.getDeployDirectory(config),is,name,closeStream);
        updateArchive(config, res)
    }

    @Throws(IOException::class)
    fun removeTLD(name: String?) {
        removeFromDirectory(config.getTldFile(), name)
    }

    @Throws(IOException::class)
    fun removeTLDs(logger: Log?, names: Array<String?>?) {
        if (ArrayUtil.isEmpty(names)) return
        val file: Resource = config.getTldFile()
        for (i in names.indices) {
            logger.log(Log.LEVEL_INFO, "extension", "Remove TLD file " + names!![i])
            removeFromDirectory(file, names[i])
        }
    }

    @Throws(IOException::class)
    fun removeEventGateways(logger: Log?, relpath: Array<String?>?) {
        if (ArrayUtil.isEmpty(relpath)) return
        val dir: Resource = config!!.getEventGatewayDirectory() // get Event gateway Directory
        for (i in relpath.indices) {
            logger.log(Log.LEVEL_INFO, "extension", "Remove Event Gateway " + relpath!![i])
            removeFromDirectory(dir, relpath[i])
        }
    }

    @Throws(IOException::class)
    fun removeFunctions(logger: Log?, relpath: Array<String?>?) {
        if (ArrayUtil.isEmpty(relpath)) return
        val file: Resource = config!!.getDefaultFunctionMapping().getPhysical()
        for (i in relpath.indices) {
            logger.log(Log.LEVEL_INFO, "extension", "Remove Function " + relpath!![i])
            removeFromDirectory(file, relpath[i])
        }
    }

    @Throws(IOException::class, PageException::class)
    fun removeArchive(archive: Resource?) {
        val logger: Log = ThreadLocalPageContext.getLog(config, "deploy")
        var virtual: String? = null
        var type: String? = null
        var `is`: InputStream? = null
        var file: ZipFile? = null
        try {
            file = ZipFile(FileWrapper.toFile(archive))
            val entry: ZipEntry = file.getEntry("META-INF/MANIFEST.MF")
                    ?: throw ApplicationException("Cannot remove " + Constants.NAME.toString() + " Archive [" + archive.toString() + "], file is to old, the file does not have a MANIFEST.")

            // no manifest
            `is` = file.getInputStream(entry)
            val manifest = Manifest(`is`)
            val attr: Attributes = manifest.getMainAttributes()
            virtual = StringUtil.unwrap(attr.getValue("mapping-virtual-path"))
            type = StringUtil.unwrap(attr.getValue("mapping-type"))
            logger.info("archive", "Remove $type mapping [$virtual]")
            if ("regular".equalsIgnoreCase(type)) removeMapping(virtual) else if ("cfc".equalsIgnoreCase(type)) removeComponentMapping(virtual) else if ("ct".equalsIgnoreCase(type)) removeCustomTag(virtual) else throw ApplicationException("Invalid type [$type], valid types are [regular, cfc, ct]")
        } catch (t: Throwable) {
            ExceptionUtil.rethrowIfNecessary(t)
            throw Caster.toPageException(t)
        } finally {
            IOUtil.close(`is`)
            ZipUtil.close(file)
        }
    }

    @Throws(IOException::class)
    fun removeTags(logger: Log?, relpath: Array<String?>?) {
        if (ArrayUtil.isEmpty(relpath)) return
        val file: Resource = config!!.getDefaultTagMapping().getPhysical()
        for (i in relpath.indices) {
            logger.log(Log.LEVEL_INFO, "extension", "Remove Tag [" + relpath!![i] + "]")
            removeFromDirectory(file, relpath[i])
        }
    }

    @Throws(IOException::class)
    fun removeFLDs(logger: Log?, names: Array<String?>?) {
        if (ArrayUtil.isEmpty(names)) return
        val file: Resource = config.getFldFile()
        for (i in names.indices) {
            logger.log(Log.LEVEL_INFO, "extension", "Remove FLD file [" + names!![i] + "]")
            removeFromDirectory(file, names[i])
        }
    }

    @Throws(IOException::class)
    fun removeFLD(name: String?) {
        removeFromDirectory(config.getFldFile(), name)
    }

    @Throws(IOException::class)
    private fun removeFromDirectory(dir: Resource?, relpath: String?) {
        if (dir.isDirectory()) {
            val file: Resource = dir.getRealResource(relpath)
            if (file.isFile()) file.remove(false)
        }
    }

    fun updateRemoteClientUsage(code: String?, displayname: String?) {
        val usage: Struct = config.getRemoteClientUsage()
        usage.setEL(code, displayname)
        val extensions: Struct? = _getRootElement("remoteClients")
        extensions.setEL("usage", toStringURLStyle(usage))
    }

    @Throws(PageException::class)
    fun updateVideoExecuterClass(cd: ClassDefinition?) {
        var cd: ClassDefinition? = cd
        if (cd.getClassName() == null) cd = ClassDefinitionImpl(VideoExecuterNotSupported::class.java.getName())
        val app: Struct? = _getRootElement("video")
        setClass(app, VideoExecuter::class.java, "videoExecuter", cd)
    }

    @Throws(PageException::class)
    fun updateAdminSyncClass(cd: ClassDefinition?) {
        var cd: ClassDefinition? = cd
        if (cd.getClassName() == null) cd = ClassDefinitionImpl(AdminSyncNotSupported::class.java.getName())
        setClass(root, AdminSync::class.java, "adminSync", cd)
    }

    fun removeRemoteClientUsage(code: String?) {
        val usage: Struct = config.getRemoteClientUsage()
        usage.removeEL(KeyImpl.getInstance(code))
        val extensions: Struct? = _getRootElement("remoteClients")
        extensions.setEL("usage", toStringURLStyle(usage))
    }

    internal inner class MyResourceNameFilter(private val name: String?) : ResourceNameFilter {
        @Override
        fun accept(parent: Resource?, name: String?): Boolean {
            return name!!.equals(this.name)
        }
    }

    @Throws(PageException::class)
    fun updateSerial(serial: String?) {
        var serial = serial
        checkWriteAccess()
        if (config !is ConfigServer) {
            throw SecurityException("Can't change serial number from this context, access is denied")
        }
        if (!StringUtil.isEmpty(serial)) {
            serial = serial.trim()
            if (!SerialNumber(serial).isValid(serial)) throw SecurityException("Serial number is invalid")
            root.setEL("serialNumber", serial)
        } else {
            try {
                rem(root, "serialNumber")
            } catch (t: Throwable) {
                ExceptionUtil.rethrowIfNecessary(t)
            }
        }
        try {
            rem(root, "serial")
        } catch (t: Throwable) {
            ExceptionUtil.rethrowIfNecessary(t)
        }
    }

    fun updateLabel(hash: String?, label: String?): Boolean {
        // check
        var hash = hash
        var label = label
        if (StringUtil.isEmpty(hash, true)) return false
        if (StringUtil.isEmpty(label, true)) return false
        hash = hash.trim()
        label = label.trim()
        val children: Array = ConfigWebUtil.getAsArray("labels", "label", root)

        // Update
        for (i in 1..children.size()) {
            val tmp: Struct = Caster.toStruct(children.get(i, null), null) ?: continue
            val h: String = ConfigWebUtil.getAsString("id", tmp, null)
            if (h != null) {
                if (h.equals(hash)) {
                    if (label!!.equals(tmp.get("name", null))) return false
                    tmp.setEL("name", label)
                    return true
                }
            }
        }

        // Insert
        val el: Struct = StructImpl(Struct.TYPE_LINKED)
        children.appendEL(el)
        el.setEL("id", hash)
        el.setEL("name", label)
        return true
    }

    @Throws(SecurityException::class)
    fun updateDebugSetting(maxLogs: Int) {
        checkWriteAccess()
        val hasAccess: Boolean = ConfigWebUtil.hasAccess(config, SecurityManager.TYPE_DEBUGGING)
        if (!hasAccess) throw SecurityException("Access denied to change debugging settings")
        if (maxLogs == -1) rem(root, "debuggingMaxRecordsLogged") else root.setEL("debuggingMaxRecordsLogged", maxLogs)
    }

    @Throws(SecurityException::class, IOException::class)
    fun updateDebugEntry(type: String?, iprange: String?, label: String?, path: String?, fullname: String?, custom: Struct?) {
        var type = type
        var iprange = iprange
        var label = label
        checkWriteAccess()
        val hasAccess: Boolean = ConfigWebUtil.hasAccess(config, SecurityManager.TYPE_DEBUGGING)
        if (!hasAccess) throw SecurityException("Access denied to change debugging settings")

        // leave this, this method throws an exception when ip range is not valid
        IPRange.getInstance(iprange)
        val id: String = MD5.getDigestAsString(label.trim().toLowerCase())
        type = type.trim()
        iprange = iprange.trim()
        label = label.trim()
        val children: Array = ConfigWebUtil.getAsArray("debugTemplates", root)

        // Update
        var el: Struct? = null
        for (i in 1..children.size()) {
            val tmp: Struct = Caster.toStruct(children.get(i, null), null) ?: continue
            val _id: String = ConfigWebUtil.getAsString("id", tmp, null)
            if (_id != null) {
                if (_id.equals(id)) {
                    el = tmp
                    break
                }
            }
        }

        // Insert
        if (el == null) {
            el = StructImpl(Struct.TYPE_LINKED)
            children.appendEL(el)
            el.setEL("id", id)
        }
        el.setEL("type", type)
        el.setEL("iprange", iprange)
        el.setEL("label", label)
        el.setEL("path", path)
        el.setEL("fullname", fullname)
        el.setEL("custom", toStringURLStyle(custom))
    }

    @Throws(SecurityException::class)
    fun removeDebugEntry(id: String?) {
        checkWriteAccess()
        val hasAccess: Boolean = ConfigWebUtil.hasAccess(config, SecurityManager.TYPE_DEBUGGING)
        if (!hasAccess) throw SecurityException("Access denied to change debugging settings")
        val children: Array = ConfigWebUtil.getAsArray("debugTemplates", root)
        val keys: Array<Key?> = children.keys()
        val _id: String
        if (children.size() > 0) {
            for (i in keys.indices.reversed()) {
                val key: Key? = keys[i]
                val el: Struct = Caster.toStruct(children.get(key, null), null) ?: continue
                _id = ConfigWebUtil.getAsString("id", el, null)
                if (_id != null && _id.equalsIgnoreCase(id)) {
                    children.removeEL(key)
                }
            }
        }
    }

    fun updateLoginSettings(captcha: Boolean, rememberMe: Boolean, delay: Int) {
        root.setEL("loginCaptcha", captcha)
        root.setEL("loginRememberme", rememberMe)
        root.setEL("loginDelay", delay)
    }

    @Throws(PageException::class)
    fun updateLogSettings(name: String?, level: Int, appenderCD: ClassDefinition?, appenderArgs: Struct?, layoutCD: ClassDefinition?, layoutArgs: Struct?) {
        var name = name
        checkWriteAccess()
        // TODO
        // boolean hasAccess=ConfigWebUtil.hasAccess(config,SecurityManagerImpl.TYPE_GATEWAY);
        // if(!hasAccess) throw new SecurityException("no access to update gateway entry");

        // check parameters
        name = name.trim()
        if (StringUtil.isEmpty(name)) throw ApplicationException("Log file name cannot be empty")
        if (appenderCD == null || !appenderCD.hasClass()) throw ExpressionException("Appender class is required")
        if (layoutCD == null || !layoutCD.hasClass()) throw ExpressionException("Layout class is required")
        try {
            appenderCD.getClazz()
            layoutCD.getClazz()
        } catch (e: Exception) {
            throw Caster.toPageException(e)
        }
        val children: Struct = ConfigWebUtil.getAsStruct("loggers", root)
        val keys: Array<Key?> = children.keys()
        // Update
        var el: Struct? = null
        for (key in keys) {
            val tmp: Struct = Caster.toStruct(children.get(key, null), null) ?: continue
            val n: String = key.getString()
            if (name.equalsIgnoreCase(n)) {
                el = tmp
                break
            }
        }
        // Insert
        if (el == null) {
            el = StructImpl(Struct.TYPE_LINKED)
            children.setEL(name, el)
        }
        el.setEL("level", LogUtil.levelToString(level, ""))
        setClass(el, null, "appender", appenderCD)
        el.setEL("appenderArguments", toStringCSSStyle(appenderArgs))
        setClass(el, null, "layout", layoutCD)
        el.setEL("layoutArguments", toStringCSSStyle(layoutArgs))
        if (el.containsKey("appender")) rem(el, "appender")
        if (el.containsKey("layout")) rem(el, "layout")
    }

    @Throws(PageException::class)
    fun updateCompilerSettings(dotNotationUpperCase: Boolean?, suppressWSBeforeArg: Boolean?, nullSupport: Boolean?, handleUnQuotedAttrValueAsString: Boolean?,
                               externalizeStringGTE: Integer?, preciseMath: Boolean?) {

        // Struct element = _getRootElement("compiler");
        checkWriteAccess()
        if (dotNotationUpperCase == null) {
            if (root.containsKey("dotNotationUpperCase")) rem(root, "dotNotationUpperCase")
        } else {
            root.setEL("dotNotationUpperCase", dotNotationUpperCase)
        }
        if (suppressWSBeforeArg == null) {
            if (root.containsKey("suppressWhitespaceBeforeArgument")) rem(root, "suppressWhitespaceBeforeArgument")
        } else {
            root.setEL("suppressWhitespaceBeforeArgument", suppressWSBeforeArg)
        }

        // full null support
        if (nullSupport == null) {
            if (root.containsKey("nullSupport")) rem(root, "nullSupport")
        } else {
            root.setEL("nullSupport", Caster.toString(nullSupport))
        }

        // externalize-string-gte
        if (externalizeStringGTE == null) {
            if (root.containsKey("externalizeStringGte")) rem(root, "externalizeStringGte")
        } else {
            root.setEL("externalizeStringGte", Caster.toString(externalizeStringGTE))
        }

        // handle Unquoted Attribute Values As String
        if (handleUnQuotedAttrValueAsString == null) {
            if (root.containsKey("handleUnquotedAttributeValueAsString")) rem(root, "handleUnquotedAttributeValueAsString")
        } else {
            root.setEL("handleUnquotedAttributeValueAsString", Caster.toString(handleUnQuotedAttrValueAsString))
        }

        // preciseMath
        if (preciseMath == null) {
            if (root.containsKey("preciseMath")) rem(root, "preciseMath")
        } else {
            root.setEL("preciseMath", Caster.toString(preciseMath))
        }
    }

    @Throws(PageException::class, IOException::class, BundleException::class, ConverterException::class)
    fun updateWebContexts(`is`: InputStream?, realpath: String?, closeStream: Boolean, store: Boolean): Array<Resource?>? {
        val filesDeployed: List<Resource?> = ArrayList<Resource?>()
        if (config is ConfigWeb) {
            _updateContextClassic(config, `is`, realpath, closeStream, filesDeployed)
        } else _updateWebContexts(config, `is`, realpath, closeStream, filesDeployed, store)
        return filesDeployed.toArray(arrayOfNulls<Resource?>(filesDeployed.size()))
    }

    @Throws(PageException::class, IOException::class, BundleException::class, ConverterException::class)
    fun updateConfigs(`is`: InputStream?, realpath: String?, closeStream: Boolean, store: Boolean): Array<Resource?>? {
        val filesDeployed: List<Resource?> = ArrayList<Resource?>()
        _updateConfigs(config, `is`, realpath, closeStream, filesDeployed, store)
        return filesDeployed.toArray(arrayOfNulls<Resource?>(filesDeployed.size()))
    }

    @Throws(PageException::class, IOException::class, BundleException::class, ConverterException::class)
    fun updateComponent(`is`: InputStream?, realpath: String?, closeStream: Boolean, store: Boolean): Array<Resource?>? {
        val filesDeployed: List<Resource?> = ArrayList<Resource?>()
        _updateComponent(config, `is`, realpath, closeStream, filesDeployed, store)
        return filesDeployed.toArray(arrayOfNulls<Resource?>(filesDeployed.size()))
    }

    @Throws(PageException::class, IOException::class, BundleException::class, ConverterException::class)
    fun updateContext(`is`: InputStream?, realpath: String?, closeStream: Boolean, store: Boolean): Array<Resource?>? {
        val filesDeployed: List<Resource?> = ArrayList<Resource?>()
        _updateContext(config, `is`, realpath, closeStream, filesDeployed, store)
        return filesDeployed.toArray(arrayOfNulls<Resource?>(filesDeployed.size()))
    }

    @Throws(PageException::class, IOException::class, BundleException::class, ConverterException::class)
    fun removeConfigs(config: Config?, store: Boolean, vararg realpathes: String?): Boolean {
        if (ArrayUtil.isEmpty(realpathes)) return false
        var force = false
        for (i in 0 until realpathes.size) {
            if (_removeConfigs(config, realpathes[i], store)) force = true
        }
        return force
    }

    @Throws(PageException::class, IOException::class, BundleException::class, ConverterException::class)
    private fun _removeConfigs(config: Config?, realpath: String?, _store: Boolean): Boolean {
        val context: Resource = config.getConfigDir() // MUST get dyn
        val trg: Resource = context.getRealResource(realpath)
        if (trg.exists()) {
            trg.remove(true)
            if (_store) _storeAndReload(config as ConfigPro?)
            ResourceUtil.removeEmptyFolders(context, null)
            return true
        }
        return false
    }

    @Throws(PageException::class, IOException::class, BundleException::class, ConverterException::class)
    fun removeComponents(config: Config?, store: Boolean, vararg realpathes: String?): Boolean {
        if (ArrayUtil.isEmpty(realpathes)) return false
        var force = false
        for (i in 0 until realpathes.size) {
            if (_removeComponent(config, realpathes[i], store)) force = true
        }
        return force
    }

    @Throws(PageException::class, IOException::class, BundleException::class, ConverterException::class)
    private fun _removeComponent(config: Config?, realpath: String?, _store: Boolean): Boolean {
        val context: Resource = config.getConfigDir().getRealResource("components") // MUST get dyn
        val trg: Resource = context.getRealResource(realpath)
        if (trg.exists()) {
            trg.remove(true)
            if (_store) _storeAndReload(config as ConfigPro?)
            ResourceUtil.removeEmptyFolders(context, null)
            return true
        }
        return false
    }

    @Throws(PageException::class, IOException::class, BundleException::class, ConverterException::class)
    fun removeContext(config: Config?, store: Boolean, logger: Log?, vararg realpathes: String?): Boolean {
        if (ArrayUtil.isEmpty(realpathes)) return false
        var force = false
        for (i in 0 until realpathes.size) {
            logger.log(Log.LEVEL_INFO, "extension", "remove " + realpathes[i])
            if (_removeContext(config, realpathes[i], store)) force = true
        }
        return force
    }

    @Throws(PageException::class, IOException::class, BundleException::class, ConverterException::class)
    private fun _removeContext(config: Config?, realpath: String?, _store: Boolean): Boolean {
        val context: Resource = config.getConfigDir().getRealResource("context")
        val trg: Resource = context.getRealResource(realpath)
        if (trg.exists()) {
            trg.remove(true)
            if (_store) _storeAndReload(config as ConfigPro?)
            ResourceUtil.removeEmptyFolders(context, null)
            return true
        }
        return false
    }

    @Throws(PageException::class, IOException::class, BundleException::class, ConverterException::class)
    fun removeWebContexts(config: Config?, store: Boolean, logger: Log?, vararg realpathes: String?): Boolean {
        if (ArrayUtil.isEmpty(realpathes)) return false
        if (config is ConfigWeb) {
            return removeContext(config, store, logger, *realpathes)
        }
        var force = false
        for (i in 0 until realpathes.size) {
            logger.log(Log.LEVEL_INFO, "extension", "Remove Context [" + realpathes[i] + "]")
            if (_removeWebContexts(config, realpathes[i], store)) force = true
        }
        return force
    }

    @Throws(PageException::class, IOException::class, BundleException::class, ConverterException::class)
    private fun _removeWebContexts(config: Config?, realpath: String?, _store: Boolean): Boolean {
        if (config is ConfigServer) {
            val cs: ConfigServer? = config as ConfigServer?

            // remove files from deploy folder
            val deploy: Resource = cs.getConfigDir().getRealResource("web-context-deployment")
            val trg: Resource = deploy.getRealResource(realpath)
            if (trg.exists()) {
                trg.remove(true)
                ResourceUtil.removeEmptyFolders(deploy, null)
            }

            // remove files from lucee web context
            var store = false
            val webs: Array<ConfigWeb?> = cs.getConfigWebs()
            for (i in webs.indices) {
                if (_removeContext(webs[i], realpath, _store)) {
                    store = true
                }
            }
            return store
        }
        return false
    }

    @Throws(PageException::class, IOException::class)
    fun updateApplication(`is`: InputStream?, realpath: String?, closeStream: Boolean): Array<Resource?>? {
        val filesDeployed: List<Resource?> = ArrayList<Resource?>()
        val dir: Resource
        // server context
        dir = if (config is ConfigServer) config.getConfigDir().getRealResource("web-deployment") else config.getRootDirectory()
        deployFilesFromStream(config, dir, `is`, realpath, closeStream, filesDeployed)
        return filesDeployed.toArray(arrayOfNulls<Resource?>(filesDeployed.size()))
    }

    @Throws(PageException::class, IOException::class)
    private fun removePlugins(config: Config?, logger: Log?, realpathes: Array<String?>?) {
        if (ArrayUtil.isEmpty(realpathes)) return
        for (i in realpathes.indices) {
            logger.log(Log.LEVEL_INFO, "extension", "Remove plugin [" + realpathes!![i] + "]")
            removeFiles(config, (config as ConfigPro?)!!.getPluginDirectory(), realpathes[i])
        }
    }

    @Throws(PageException::class, IOException::class)
    private fun removeApplications(config: Config?, logger: Log?, realpathes: Array<String?>?) {
        if (ArrayUtil.isEmpty(realpathes)) return
        for (i in realpathes.indices) {
            logger.log(Log.LEVEL_INFO, "extension", "Remove application [" + realpathes!![i] + "]")
            removeFiles(config, config.getRootDirectory(), realpathes[i])
        }
    }

    @Throws(PageException::class, IOException::class)
    private fun removeFiles(config: Config?, root: Resource?, realpath: String?) {
        if (config is ConfigServer) {
            val webs: Array<ConfigWeb?> = (config as ConfigServer?).getConfigWebs()
            for (i in webs.indices) {
                removeFiles(webs[i], root, realpath)
            }
            return
        }

        // ConfigWeb
        val trg: Resource = root.getRealResource(realpath)
        if (trg.exists()) trg.remove(true)
    }

    @Throws(IOException::class, PageException::class, BundleException::class, ConverterException::class)
    fun _removeExtension(config: ConfigPro?, extensionID: String?, removePhysical: Boolean): Array<BundleDefinition?>? {
        if (!Decision.isUUId(extensionID)) throw IOException("id [$extensionID] is invalid, it has to be a UUID")
        val children: Array = ConfigWebUtil.getAsArray("extensions", root)
        val keys: IntArray = children.intKeys()

        // Update
        var el: Struct
        var id: String
        var arr: Array<String?>?
        var storeChildren = false
        val bundles: Array<BundleDefinition?>
        val log: Log = ThreadLocalPageContext.getLog(config, "deploy")
        var key: Int
        for (i in keys.indices.reversed()) {
            key = keys[i]
            el = Caster.toStruct(children.get(key, null), null)
            if (el == null) continue
            id = Caster.toString(el.get(KeyConstants._id), null)
            if (extensionID.equalsIgnoreCase(id)) {
                bundles = RHExtension.toBundleDefinitions(ConfigWebUtil.getAsString("bundles", el, null)) // get existing bundles before populate new ones

                // bundles
                arr = _removeExtensionCheckOtherUsage(children, el, "bundles")
                // removeBundles(arr,removePhysical);
                // flds
                arr = _removeExtensionCheckOtherUsage(children, el, "flds")
                removeFLDs(log, arr)
                // tlds
                arr = _removeExtensionCheckOtherUsage(children, el, "tlds")
                removeTLDs(log, arr)
                // contexts
                arr = _removeExtensionCheckOtherUsage(children, el, "contexts")
                storeChildren = removeContext(config, false, log, arr)

                // webcontexts
                arr = _removeExtensionCheckOtherUsage(children, el, "webcontexts")
                storeChildren = removeWebContexts(config, false, log, arr)

                // applications
                arr = _removeExtensionCheckOtherUsage(children, el, "applications")
                removeApplications(config, log, arr)

                // components
                arr = _removeExtensionCheckOtherUsage(children, el, "components")
                removeComponents(config, false, arr)

                // configs
                arr = _removeExtensionCheckOtherUsage(children, el, "config")
                removeConfigs(config, false, arr)

                // plugins
                arr = _removeExtensionCheckOtherUsage(children, el, "plugins")
                removePlugins(config, log, arr)
                children.removeEL(key)

                // remove files
                val version: String = Caster.toString(el.get(KeyConstants._version, null), null)
                var file: Resource = RHExtension.getMetaDataFile(config, id, version)
                if (file.isFile()) file.delete()
                file = RHExtension.getExtensionFile(config, id, version)
                if (file.isFile()) file.delete()
                return bundles
            }
        }
        return null
    }

    private fun _removeExtensionCheckOtherUsage(children: Array?, curr: Struct?, type: String?): Array<String?>? {
        val currVal: String = ConfigWebUtil.getAsString(type, curr, null)
        if (StringUtil.isEmpty(currVal)) return null
        val keys: Array<Key?> = children.keys()
        val otherVal: String
        val other: Struct?
        val currSet: Set<String?> = ListUtil.toSet(ListUtil.trimItems(ListUtil.listToStringArray(currVal, ',')))
        val otherArr: Array<String?>
        var key: Key?
        for (i in keys.indices.reversed()) {
            key = keys[i]
            val tmp: Struct = Caster.toStruct(children.get(key, null), null) ?: continue
            other = tmp
            if (other === curr) continue
            otherVal = ConfigWebUtil.getAsString(type, other, null)
            if (StringUtil.isEmpty(otherVal)) continue
            otherArr = ListUtil.trimItems(ListUtil.listToStringArray(otherVal, ','))
            for (y in otherArr.indices) {
                currSet.remove(otherArr[y])
            }
        }
        return currSet.toArray(arrayOfNulls<String?>(currSet.size()))
    }

    /**
     *
     * @param config
     * @param ext
     * @return the bundles used before when this was a update, if it is a new extension then null is
     * returned
     * @throws IOException
     * @throws BundleException
     * @throws PageException
     */
    @Throws(IOException::class, BundleException::class, PageException::class)
    fun _updateExtension(config: ConfigPro?, ext: RHExtension?): Array<BundleDefinition?>? {
        if (!Decision.isUUId(ext.getId())) throw IOException("id [" + ext.getId().toString() + "] is invalid, it has to be a UUID")
        val children: Array = ConfigWebUtil.getAsArray("extensions", root)
        val keys: IntArray = children.intKeys()
        var key: Int
        // Update
        var el: Struct?
        var id: String
        var old: Array<BundleDefinition?>?
        for (i in keys.indices.reversed()) {
            key = keys[i]
            el = Caster.toStruct(children.get(key, null), null)
            if (el == null) continue
            id = Caster.toString(el.get(KeyConstants._id), null)
            if (ext.getId().equalsIgnoreCase(id)) {
                old = RHExtension.toBundleDefinitions(ConfigWebUtil.getAsString("bundles", el, null)) // get existing bundles before populate new ones
                ext.populate(el, false)
                old = minus(old, OSGiUtil.toBundleDefinitions(ext.getBundles()))
                return old
            }
        }

        // Insert
        el = StructImpl(Struct.TYPE_LINKED)
        ext.populate(el, false)
        children.appendEL(el)
        return null
    }

    private fun minus(oldBD: Array<BundleDefinition?>?, newBD: Array<BundleDefinition?>?): Array<BundleDefinition?>? {
        val list: List<BundleDefinition?> = ArrayList()
        var has: Boolean
        for (o in oldBD!!) {
            has = false
            for (n in newBD!!) {
                if (o.equals(n)) {
                    has = true
                    break
                }
            }
            if (!has) list.add(o)
        }
        return list.toArray(arrayOfNulls<BundleDefinition?>(list.size()))
    }

    private fun getRHExtension(config: ConfigPro?, id: String?, defaultValue: RHExtension?): RHExtension? {
        val children: Array = ConfigWebUtil.getAsArray("extensions", root)
        if (children != null) {
            val keys: IntArray = children.intKeys()
            for (i in keys) {
                val tmp: Struct = Caster.toStruct(children.get(i, null), null) ?: continue
                val _id: String = Caster.toString(tmp.get(KeyConstants._id, null), null)
                if (!id!!.equals(_id)) continue
                return try {
                    RHExtension(config, _id, Caster.toString(tmp.get(KeyConstants._version), null), null, false)
                } catch (e: Exception) {
                    defaultValue
                }
            }
        }
        return defaultValue
    }

    @Throws(PageException::class)
    private fun _hasRHExtensions(config: ConfigPro?, ed: ExtensionDefintion?): RHExtension? {
        val children: Array = ConfigWebUtil.getAsArray("extensions", root)
        val keys: IntArray = children.intKeys()
        var tmp: RHExtension
        return try {
            val id: String
            val v: String
            for (key in keys) {
                val sct: Struct = Caster.toStruct(children.get(key, null), null) ?: continue
                id = Caster.toString(sct.get(KeyConstants._id, null), null)
                v = Caster.toString(sct.get(KeyConstants._version, null), null)
                if (!RHExtension.isInstalled(config, id, v)) continue
                if (ed.equals(ExtensionDefintion(id, v))) return RHExtension(config, id, v, null, false)
            }
            null
        } catch (e: Exception) {
            throw Caster.toPageException(e)
        }
    }

    @Throws(PageException::class)
    fun updateAuthKey(key: String?) {
        var key = key
        checkWriteAccess()
        key = key.trim()

        // merge new key and existing
        val cs: ConfigServerImpl? = config as ConfigServerImpl?
        val keys: Array<String?> = cs!!.getAuthenticationKeys()
        val set: Set<String?> = HashSet<String?>()
        for (i in keys.indices) {
            set.add(keys[i])
        }
        set.add(key)
        root.setEL("authKeys", authKeysAsList(set))
    }

    @Throws(PageException::class)
    fun removeAuthKeys(key: String?) {
        var key = key
        checkWriteAccess()
        key = key.trim()

        // remove key
        val cs: ConfigServerImpl? = config as ConfigServerImpl?
        val keys: Array<String?> = cs!!.getAuthenticationKeys()
        val set: Set<String?> = HashSet<String?>()
        for (i in keys.indices) {
            if (!key!!.equals(keys[i])) set.add(keys[i])
        }
        root.setEL("authKeys", authKeysAsList(set))
    }

    @Throws(SecurityException::class, ApplicationException::class)
    fun updateAPIKey(key: String?) {
        var key = key
        checkWriteAccess()
        key = key.trim()
        if (!Decision.isGUId(key)) throw ApplicationException("Passed API Key [$key] is not valid")
        root.setEL("apiKey", key)
    }

    @Throws(PageException::class)
    fun removeAPIKey() {
        checkWriteAccess()
        if (root.containsKey("apiKey")) rem(root, "apiKey")
    }

    @Throws(PageException::class)
    private fun authKeysAsList(set: Set<String?>?): String? {
        val sb = StringBuilder()
        val it = set!!.iterator()
        var key: String?
        while (it.hasNext()) {
            key = it.next().trim()
            if (sb.length() > 0) sb.append(',')
            try {
                sb.append(URLEncoder.encode(key, "UTF-8"))
            } catch (e: UnsupportedEncodingException) {
                throw Caster.toPageException(e)
            }
        }
        return sb.toString()
    }

    @Throws(PageException::class, IOException::class)
    fun updatePlugin(`is`: InputStream?, realpath: String?, closeStream: Boolean): Array<Resource?>? {
        val filesDeployed: List<Resource?> = ArrayList<Resource?>()
        deployFilesFromStream(config, config!!.getPluginDirectory(), `is`, realpath, closeStream, filesDeployed)
        return filesDeployed.toArray(arrayOfNulls<Resource?>(filesDeployed.size()))
    }

    @Throws(PageException::class, IOException::class)
    fun updatePlugin(pc: PageContext?, src: Resource?) {
        // convert to a directory when it is a zip
        var src: Resource? = src
        if (!src.isDirectory()) {
            if (!IsZipFile.invoke(src)) throw ApplicationException("Path [" + src.getAbsolutePath().toString() + "] is invalid, it has to be a path to an existing zip file or a directory containing a plugin")
            src = ResourceUtil.toResourceExisting(pc, "zip://" + src.getAbsolutePath())
        }
        val name: String = ResourceUtil.getName(src.getName())
        if (!PluginFilter.doAccept(src)) throw ApplicationException("Plugin [" + src.getAbsolutePath().toString() + "] is invalid, missing one of the following files [Action."
                + Constants.getCFMLComponentExtension().toString() + " or Action." + Constants.getLuceeComponentExtension().toString() + ",language.xml] in root, existing files are ["
                + lucee.runtime.type.util.ListUtil.arrayToList(src.list(), ", ").toString() + "]")
        val dir: Resource = config!!.getPluginDirectory()
        val trgDir: Resource = dir.getRealResource(name)
        if (trgDir.exists()) {
            trgDir.remove(true)
        }
        ResourceUtil.copyRecursive(src, trgDir)
    }

    private fun removeClass(el: Struct?, prefix: String?) {
        var prefix = prefix
        if (prefix.endsWith("-")) prefix = prefix.substring(0, prefix!!.length() - 1)
        val hp: Boolean = !prefix.isEmpty()
        el.removeEL(KeyImpl.init(if (hp) prefix.toString() + "Class" else "class"))
        el.removeEL(KeyImpl.init(if (hp) prefix.toString() + "BundleName" else "bundleName"))
        el.removeEL(KeyImpl.init(if (hp) prefix.toString() + "BundleVersion" else "bundleVersion"))
    }

    class PluginFilter : ResourceFilter {
        @Override
        fun accept(res: Resource?): Boolean {
            return doAccept(res)
        }

        companion object {
            fun doAccept(res: Resource?): Boolean {
                return res.isDirectory() && (res.getRealResource("/Action." + Constants.getCFMLComponentExtension()).isFile()
                        || res.getRealResource("/Action." + Constants.getLuceeComponentExtension()).isFile()) && res.getRealResource("/language.xml").isFile()
            }
        }
    }

    @Throws(SecurityException::class)
    fun updateQueue(max: Integer?, timeout: Integer?, enable: Boolean?) {
        checkWriteAccess()
        val hasAccess: Boolean = ConfigWebUtil.hasAccess(config, SecurityManager.TYPE_SETTING)
        if (!hasAccess) throw SecurityException("Accces Denied to update queue settings")

        // max
        if (max == null) rem(root, "requestQueueMax") else root.setEL("requestQueueMax", max)
        // total
        if (timeout == null) rem(root, "requestQueueTimeout") else root.setEL("requestQueueTimeout", timeout)
        // enable
        if (enable == null) rem(root, "requestQueueEnable") else root.setEL("requestQueueEnable", enable)
    }

    @Throws(SecurityException::class)
    fun updateCGIReadonly(cgiReadonly: Boolean?) {
        checkWriteAccess()
        val hasAccess: Boolean = ConfigWebUtil.hasAccess(config, SecurityManager.TYPE_SETTING)
        if (!hasAccess) throw SecurityException("Accces Denied to update scope setting")
        root.setEL("cgiScopeReadOnly", Caster.toString(cgiReadonly, ""))
    }

    companion object {
        private val EMPTY: Array<BundleInfo?>? = arrayOfNulls<BundleInfo?>(0)

        /**
         *
         * @param config
         * @param password
         * @return returns a new instance of the class
         * @throws SAXException
         * @throws IOException
         * @throws PageException
         */
        @Throws(IOException::class, PageException::class)
        fun newInstance(config: Config?, password: Password?): ConfigAdmin? {
            return ConfigAdmin(config as ConfigPro?, password)
        }

        /*
	 * public void setVersion(double version) { setVersion(doc,version);
	 * 
	 * }
	 */
        fun setVersion(root: Struct?, version: Version?) {
            root.setEL("version", version.getMajor().toString() + "." + version.getMinor())
        }

        fun checkForChangesInConfigFile(config: Config?) {
            val ci: ConfigPro? = config
            if (!ci!!.checkForChangesInConfigFile()) return
            val file: Resource = config.getConfigFile()
            val diff: Long = file.lastModified() - ci!!.lastModified()
            if (diff < 10 && diff > -10) return
            // reload
            try {
                val admin = newInstance(ci, null)
                admin!!._reload()
                LogUtil.log(ThreadLocalPageContext.getConfig(config), Log.LEVEL_INFO, ConfigAdmin::class.java.getName(), "reloaded the configuration [$file] automatically")
            } catch (t: Throwable) {
                ExceptionUtil.rethrowIfNecessary(t)
            }
        }

        @Synchronized
        @Throws(PageException::class, ClassException::class, IOException::class, TagLibException::class, FunctionLibException::class, BundleException::class, ConverterException::class)
        fun _storeAndReload(config: ConfigPro?) {
            val admin = ConfigAdmin(config, null)
            admin._store()
            admin._reload()
        }

        @Throws(IOException::class, PageException::class, BundleException::class, ConverterException::class)
        fun updateMapping(config: ConfigPro?, virtual: String?, physical: String?, archive: String?, primary: String?, inspect: Short, toplevel: Boolean, listenerMode: Int,
                          listenerType: Int, readonly: Boolean, reload: Boolean) {
            val admin = ConfigAdmin(config, null)
            admin._updateMapping(virtual, physical, archive, primary, inspect, toplevel, listenerMode, listenerType, readonly)
            admin._store()
            if (reload) admin._reload()
        }

        @Throws(IOException::class, PageException::class, BundleException::class, ConverterException::class)
        fun updateComponentMapping(config: ConfigPro?, virtual: String?, physical: String?, archive: String?, primary: String?, inspect: Short, reload: Boolean) {
            val admin = ConfigAdmin(config, null)
            admin._updateComponentMapping(virtual, physical, archive, primary, inspect)
            admin._store()
            if (reload) admin._reload()
        }

        @Throws(IOException::class, PageException::class, BundleException::class, ConverterException::class)
        fun updateCustomTagMapping(config: ConfigPro?, virtual: String?, physical: String?, archive: String?, primary: String?, inspect: Short, reload: Boolean) {
            val admin = ConfigAdmin(config, null)
            admin._updateCustomTag(virtual, physical, archive, primary, inspect)
            admin._store()
            if (reload) admin._reload()
        }

        @Throws(IOException::class, PageException::class, BundleException::class, ConverterException::class)
        fun updateScheduledTask(config: ConfigPro?, task: ScheduleTask?, reload: Boolean): Array? {
            val admin = ConfigAdmin(config, null)
            admin._updateScheduledTask(task)
            admin._store()
            if (reload) admin._reload()
            return admin._getScheduledTasks()
        }

        @Throws(PageException::class, IOException::class, ConverterException::class, BundleException::class)
        fun pauseScheduledTask(config: ConfigPro?, name: String?, pause: Boolean, throwWhenNotExist: Boolean, reload: Boolean) {
            val admin = ConfigAdmin(config, null)
            var data: Struct? = null
            data = try {
                admin._getScheduledTask(name, true)
            } catch (ee: ExpressionException) {
                if (throwWhenNotExist) throw ee
                return
            }
            data.setEL("paused", pause)
            admin._store()
            if (reload) admin._reload()
        }

        @Throws(PageException::class, IOException::class, ConverterException::class, BundleException::class)
        fun removeScheduledTask(config: ConfigPro?, name: String?, reload: Boolean) {
            val admin = ConfigAdmin(config, null)
            admin._removeScheduledTask(name)
            admin._store()
            if (reload) admin._reload()
        }

        fun createVirtual(data: Struct?): String? {
            val str: String = ConfigWebFactory.getAttr(data, "virtual")
            return if (!StringUtil.isEmpty(str)) str else createVirtual(ConfigWebFactory.getAttr(data, "physical"), ConfigWebFactory.getAttr(data, "archive"))
        }

        fun createVirtual(physical: String?, archive: String?): String? {
            return "/" + MD5.getDigestAsString(physical.toString() + ":" + archive, "")
        }

        @Throws(IOException::class, BundleException::class)
        fun updateJar(config: Config?, resJar: Resource?, reloadWhenClassicJar: Boolean) {
            var bf: BundleFile? = BundleFile.getInstance(resJar)

            // resJar is a bundle
            if (bf.isBundle()) {
                bf = installBundle(config, bf)
                OSGiUtil.loadBundle(bf)
                return
            }
            val lib: Resource = (config as ConfigPro?)!!.getLibraryDirectory()
            if (!lib.exists()) lib.mkdir()
            val fileLib: Resource = lib.getRealResource(resJar.getName())

            // if there is an existing, has the file changed?
            if (fileLib.length() !== resJar.length()) {
                IOUtil.closeEL(config.getClassLoader())
                ResourceUtil.copy(resJar, fileLib)
                if (reloadWhenClassicJar) ConfigWebUtil.reloadLib(config)
            }
        }

        /*
	 * important! returns null when not a bundle!
	 */
        @Throws(IOException::class, BundleException::class)
        fun installBundle(config: Config?, resJar: Resource?, extVersion: String?, convert2bundle: Boolean): BundleFile? {
            var bf: BundleFile = BundleFile.getInstance(resJar)

            // resJar is a bundle
            if (bf.isBundle()) {
                return installBundle(config, bf)
            }
            if (!convert2bundle) return null

            // name
            var name: String = bf.getSymbolicName()
            if (StringUtil.isEmpty(name)) name = BundleBuilderFactory.createSymbolicName(resJar)

            // version
            var version: Version = bf.getVersion()
            if (version == null) version = OSGiUtil.toVersion(extVersion)
            LogUtil.log(ThreadLocalPageContext.getConfig(config), Log.LEVEL_INFO, ConfigAdmin::class.java.getName(), "failed to load [$resJar] as OSGi Bundle")
            val bbf = BundleBuilderFactory(resJar, name)
            bbf.setVersion(version)
            bbf.setIgnoreExistingManifest(false)
            bbf.build()
            bf = BundleFile.getInstance(resJar)
            LogUtil.log(ThreadLocalPageContext.getConfig(config), Log.LEVEL_INFO, ConfigAdmin::class.java.getName(), "converted  [$resJar] to an OSGi Bundle")
            return installBundle(config, bf)
        }

        @Throws(IOException::class, BundleException::class)
        private fun installBundle(config: Config?, bf: BundleFile?): BundleFile? {

            // does this bundle already exists
            val _bf: BundleFile = OSGiUtil.getBundleFile(bf.getSymbolicName(), bf.getVersion(), null, null, false, null)
            if (_bf != null) return _bf
            val engine: CFMLEngine = CFMLEngineFactory.getInstance()
            val factory: CFMLEngineFactory = engine.getCFMLEngineFactory()

            // copy to jar directory
            val jar = File(factory.getBundleDirectory(), bf.getSymbolicName().toString() + "-" + bf.getVersion().toString() + ".jar")
            val `is`: InputStream = bf.getInputStream()
            val os: OutputStream = FileOutputStream(jar)
            try {
                IOUtil.copy(`is`, os, false, false)
            } finally {
                IOUtil.close(`is`, os)
            }
            return BundleFile.getInstance(jar)
        }

        /**
         *
         * @param config
         * @param is
         * @param name
         * @param extensionVersion if given jar is no bundle the extension version is used for the bundle
         * created
         * @param closeStream
         * @return
         * @throws IOException
         * @throws BundleException
         */
        @Throws(IOException::class, BundleException::class)
        fun updateBundle(config: Config?, `is`: InputStream?, name: String?, extensionVersion: String?, closeStream: Boolean): Bundle? {
            val obj: Object = installBundle(config, `is`, name, extensionVersion, closeStream, false) as? BundleFile
                    ?: throw BundleException("input is not an OSGi Bundle.")
            val bf: BundleFile = obj as BundleFile
            return OSGiUtil.loadBundle(bf)
        }

        /**
         * @param config
         * @param is
         * @param name
         * @param extensionVersion
         * @param closeStream
         * @param convert2bundle
         * @return return the Bundle File or the file in case it is not a bundle.
         * @throws IOException
         * @throws BundleException
         */
        @Throws(IOException::class, BundleException::class)
        fun installBundle(config: Config?, `is`: InputStream?, name: String?, extensionVersion: String?, closeStream: Boolean, convert2bundle: Boolean): Object? {
            val tmp: Resource = SystemUtil.getTempDirectory().getRealResource(name)
            val os: OutputStream = tmp.getOutputStream()
            IOUtil.copy(`is`, os, closeStream, true)
            val bf: BundleFile? = installBundle(config, tmp, extensionVersion, convert2bundle)
            if (bf != null) {
                tmp.delete()
                return bf
            }
            return tmp
        }

        @Throws(IOException::class, BundleException::class)
        fun updateJar(config: Config?, `is`: InputStream?, name: String?, closeStream: Boolean) {
            val tmp: Resource = SystemUtil.getTempDirectory().getRealResource(name)
            try {
                IOUtil.copy(`is`, tmp, closeStream)
                updateJar(config, tmp, true)
            } finally {
                tmp.delete()
            }
        }

        @Throws(IOException::class, PageException::class, BundleException::class, ConverterException::class)
        fun removeJDBCDriver(config: ConfigPro?, cd: ClassDefinition?, reload: Boolean) {
            val admin = ConfigAdmin(config, null)
            admin._removeJDBCDriver(cd)
            admin._store() // store is necessary, otherwise it get lost
            if (reload) admin._reload()
        }

        @Throws(IOException::class, PageException::class, BundleException::class, ConverterException::class)
        fun removeSearchEngine(config: ConfigPro?, reload: Boolean) {
            val admin = ConfigAdmin(config, null)
            admin._removeSearchEngine()
            admin._store()
            if (reload) admin._reload()
        }

        @Throws(IOException::class, PageException::class, BundleException::class, ConverterException::class)
        fun removeORMEngine(config: ConfigPro?, reload: Boolean) {
            val admin = ConfigAdmin(config, null)
            admin._removeORMEngine()
            admin._store()
            if (reload) admin._reload()
        }

        private fun toStringURLStyle(sct: Struct?): String? {
            if (sct == null) return ""
            val it: Iterator<Entry<Key?, Object?>?> = sct.entryIterator()
            var e: Entry<Key?, Object?>?
            val rtn = StringBuilder()
            while (it.hasNext()) {
                e = it.next()
                if (rtn.length() > 0) rtn.append('&')
                rtn.append(URLEncoder.encode(e.getKey().getString()))
                rtn.append('=')
                rtn.append(URLEncoder.encode(Caster.toString(e.getValue(), "")))
            }
            return rtn.toString()
        }

        private fun toStringCSSStyle(sct: Struct?): String? {
            // Collection.Key[] keys = sct.keys();
            val rtn = StringBuilder()
            val it: Iterator<Entry<Key?, Object?>?> = sct.entryIterator()
            var e: Entry<Key?, Object?>?
            while (it.hasNext()) {
                e = it.next()
                if (rtn.length() > 0) rtn.append(';')
                rtn.append(encode(e.getKey().getString()))
                rtn.append(':')
                rtn.append(encode(Caster.toString(e.getValue(), "")))
            }
            return rtn.toString()
        }

        private fun encode(str: String?): String? {
            return try {
                URLEncodedFormat.invoke(str, "UTF-8", false)
            } catch (e: PageException) {
                URLEncoder.encode(str)
            }
        }

        @Throws(IOException::class, PageException::class, BundleException::class, ConverterException::class)
        fun removeCacheHandler(config: ConfigPro?, id: String?, reload: Boolean) {
            val admin = ConfigAdmin(config, null)
            admin._removeCacheHandler(id)
            admin._store()
            if (reload) admin._reload()
        }

        @Throws(PageException::class)
        fun updateArchive(config: ConfigPro?, arc: Resource?, reload: Boolean) {
            try {
                val admin = ConfigAdmin(config, null)
                admin.updateArchive(config, arc)
                admin._store()
                if (reload) admin._reload()
            } catch (t: Throwable) {
                ExceptionUtil.rethrowIfNecessary(t)
                throw Caster.toPageException(t)
            }
        }

        @Throws(PageException::class)
        fun updateCore(config: ConfigServerImpl?, core: Resource?, reload: Boolean) {
            var core: Resource? = core
            try {
                // get patches directory
                val engine: CFMLEngine = ConfigWebUtil.getEngine(config)
                val cs: ConfigServerImpl? = config
                val v: Version
                v = CFMLEngineFactory.toVersion(core.getName(), null)
                val logger: Log = cs!!.getLog("deploy")
                val f: File = engine.getCFMLEngineFactory().getResourceRoot()
                val res: Resource = ResourcesImpl.getFileResourceProvider().getResource(f.getAbsolutePath())
                val pd: Resource = res.getRealResource("patches")
                if (!pd.exists()) pd.mkdirs()
                val pf: Resource = pd.getRealResource(core.getName())

                // move to patches directory
                core.moveTo(pf)
                core = pf
                logger.log(Log.LEVEL_INFO, "Update-Engine", "Installing Lucee [" + v + "] (previous version was [" + cs!!.getEngine().getInfo().getVersion() + "] )")
                //
                val admin = ConfigAdmin(config, null)
                admin.restart(config)
            } catch (t: Throwable) {
                ExceptionUtil.rethrowIfNecessary(t)
                DeployHandler.moveToFailedFolder(config!!.getDeployDirectory(), core)
                throw Caster.toPageException(t)
            }
        }

        @Throws(PageException::class)
        fun _updateRHExtension(config: ConfigPro?, ext: Resource?, reload: Boolean, force: Boolean) {
            try {
                val admin = ConfigAdmin(config, null)
                admin.updateRHExtension(config, ext, reload, force)
            } catch (e: Exception) {
                throw Caster.toPageException(e)
            }
        }

        private fun startsWith(path: String?, type: String?, name: String?): Boolean {
            return StringUtil.startsWithIgnoreCase(path, name.toString() + "/") || StringUtil.startsWithIgnoreCase(path, type.toString() + "/" + name + "/")
        }

        private fun fileName(entry: ZipEntry?): String? {
            val name: String = entry.getName()
            val index: Int = name.lastIndexOf('/')
            return if (index == -1) name else name.substring(index + 1)
        }

        private fun subFolder(entry: ZipEntry?): String? {
            val name: String = entry.getName()
            val index: Int = name.indexOf('/')
            return if (index == -1) name else name.substring(index + 1)
        }

        @Throws(IOException::class)
        private fun write(dir: Resource?, `is`: InputStream?, name: String?, closeStream: Boolean): Resource? {
            if (!dir.exists()) dir.createDirectory(true)
            val file: Resource = dir.getRealResource(name)
            val p: Resource = file.getParentResource()
            if (!p.exists()) p.createDirectory(true)
            IOUtil.copy(`is`, file.getOutputStream(), closeStream, true)
            return file
        }

        @Throws(PageException::class, IOException::class, BundleException::class, ConverterException::class)
        private fun _updateWebContexts(config: Config?, `is`: InputStream?, realpath: String?, closeStream: Boolean, filesDeployed: List<Resource?>?, store: Boolean) {
            if (config !is ConfigServer) throw ApplicationException("Invalid context, you can only call this method from server context")
            val cs: ConfigServer? = config as ConfigServer?
            val wcd: Resource = cs.getConfigDir().getRealResource("web-context-deployment")
            val trg: Resource = wcd.getRealResource(realpath)
            if (trg.exists()) trg.remove(true)
            val p: Resource = trg.getParentResource()
            if (!p.isDirectory()) p.createDirectory(true)
            IOUtil.copy(`is`, trg.getOutputStream(false), closeStream, true)
            filesDeployed.add(trg)
            if (store) _storeAndReload(config as ConfigPro?)
        }

        @Throws(PageException::class, IOException::class, BundleException::class, ConverterException::class)
        private fun _updateConfigs(config: Config?, `is`: InputStream?, realpath: String?, closeStream: Boolean, filesDeployed: List<Resource?>?, store: Boolean) {
            val configs: Resource = config.getConfigDir() // MUST get that dynamically
            val trg: Resource = configs.getRealResource(realpath)
            if (trg.exists()) trg.remove(true)
            val p: Resource = trg.getParentResource()
            if (!p.isDirectory()) p.createDirectory(true)
            IOUtil.copy(`is`, trg.getOutputStream(false), closeStream, true)
            filesDeployed.add(trg)
            if (store) _storeAndReload(config as ConfigPro?)
        }

        @Throws(PageException::class, IOException::class, BundleException::class, ConverterException::class)
        private fun _updateComponent(config: Config?, `is`: InputStream?, realpath: String?, closeStream: Boolean, filesDeployed: List<Resource?>?, store: Boolean) {
            val comps: Resource = config.getConfigDir().getRealResource("components") // MUST get that dynamically
            val trg: Resource = comps.getRealResource(realpath)
            if (trg.exists()) trg.remove(true)
            val p: Resource = trg.getParentResource()
            if (!p.isDirectory()) p.createDirectory(true)
            IOUtil.copy(`is`, trg.getOutputStream(false), closeStream, true)
            filesDeployed.add(trg)
            if (store) _storeAndReload(config as ConfigPro?)
        }

        @Throws(PageException::class, IOException::class, BundleException::class, ConverterException::class)
        private fun _updateContext(config: Config?, `is`: InputStream?, realpath: String?, closeStream: Boolean, filesDeployed: List<Resource?>?, store: Boolean) {
            val trg: Resource = config.getConfigDir().getRealResource("context").getRealResource(realpath)
            if (trg.exists()) trg.remove(true)
            val p: Resource = trg.getParentResource()
            if (!p.isDirectory()) p.createDirectory(true)
            IOUtil.copy(`is`, trg.getOutputStream(false), closeStream, true)
            filesDeployed.add(trg)
            if (store) _storeAndReload(config as ConfigPro?)
        }

        @Deprecated
        @Throws(PageException::class, IOException::class, BundleException::class, ConverterException::class)
        fun updateContextClassic(config: ConfigPro?, `is`: InputStream?, realpath: String?, closeStream: Boolean): Array<Resource?>? {
            val filesDeployed: List<Resource?> = ArrayList<Resource?>()
            _updateContextClassic(config, `is`, realpath, closeStream, filesDeployed)
            return filesDeployed.toArray(arrayOfNulls<Resource?>(filesDeployed.size()))
        }

        @Deprecated
        @Throws(PageException::class, IOException::class, BundleException::class, ConverterException::class)
        private fun _updateContextClassic(config: Config?, `is`: InputStream?, realpath: String?, closeStream: Boolean, filesDeployed: List<Resource?>?) {
            if (config is ConfigServer) {
                val webs: Array<ConfigWeb?> = (config as ConfigServer?).getConfigWebs()
                if (webs.size == 0) return
                if (webs.size == 1) {
                    _updateContextClassic(webs[0], `is`, realpath, closeStream, filesDeployed)
                    return
                }
                try {
                    val barr: ByteArray = IOUtil.toBytes(`is`)
                    for (i in webs.indices) {
                        _updateContextClassic(webs[i], ByteArrayInputStream(barr), realpath, true, filesDeployed)
                    }
                } finally {
                    if (closeStream) IOUtil.close(`is`)
                }
                return
            }

            // ConfigWeb
            val trg: Resource = config.getConfigDir().getRealResource("context").getRealResource(realpath)
            if (trg.exists()) trg.remove(true)
            val p: Resource = trg.getParentResource()
            if (!p.isDirectory()) p.createDirectory(true)
            IOUtil.copy(`is`, trg.getOutputStream(false), closeStream, true)
            filesDeployed.add(trg)
            _storeAndReload(config as ConfigPro?)
        }

        @Throws(PageException::class, IOException::class)
        private fun deployFilesFromStream(config: Config?, root: Resource?, `is`: InputStream?, realpath: String?, closeStream: Boolean, filesDeployed: List<Resource?>?) {
            // MUST this makes no sense at this point
            if (config is ConfigServer) {
                val webs: Array<ConfigWeb?> = (config as ConfigServer?).getConfigWebs()
                if (webs.size == 0) return
                if (webs.size == 1) {
                    deployFilesFromStream(webs[0], root, `is`, realpath, closeStream, filesDeployed)
                    return
                }
                try {
                    val barr: ByteArray = IOUtil.toBytes(`is`)
                    for (i in webs.indices) {
                        deployFilesFromStream(webs[i], root, ByteArrayInputStream(barr), realpath, true, filesDeployed)
                    }
                } finally {
                    if (closeStream) IOUtil.close(`is`)
                }
                return
            }

            // ConfigWeb
            val trg: Resource = root.getRealResource(realpath)
            if (trg.exists()) trg.remove(true)
            val p: Resource = trg.getParentResource()
            if (!p.isDirectory()) p.createDirectory(true)
            IOUtil.copy(`is`, trg.getOutputStream(false), closeStream, true)
            filesDeployed.add(trg)
        }

        @Throws(IOException::class, PageException::class, BundleException::class, ConverterException::class)
        fun removeRHExtensions(config: ConfigPro?, log: Log?, extensionIDs: Array<String?>?, removePhysical: Boolean) {
            val admin = ConfigAdmin(config, null)
            val oldMap: Map<String?, BundleDefinition?> = HashMap()
            var bds: Array<BundleDefinition?>?
            for (extensionID in extensionIDs!!) {
                try {
                    bds = admin._removeExtension(config, extensionID, removePhysical)
                    if (bds != null) {
                        for (bd in bds) {
                            if (bd == null) continue  // TODO why are they Null?
                            oldMap.put(bd.toString(), bd)
                        }
                    }
                } catch (e: Exception) {
                    log.log(Log.LEVEL_ERROR, ConfigAdmin::class.java.getName(), e)
                }
            }
            admin._storeAndReload()
            if (!oldMap.isEmpty() && config is ConfigServer) {
                val cs: ConfigServer? = config as ConfigServer?
                val webs: Array<ConfigWeb?> = cs.getConfigWebs()
                for (i in webs.indices) {
                    try {
                        _storeAndReload(webs[i] as ConfigPro?)
                    } catch (e: Exception) {
                        log.log(Log.LEVEL_ERROR, ConfigAdmin::class.java.getName(), e)
                    }
                }
            }
            cleanBundles(null, config, oldMap.values().toArray(arrayOfNulls<BundleDefinition?>(oldMap.size()))) // clean after populating the new ones
        }

        @Throws(BundleException::class, ApplicationException::class, IOException::class)
        fun cleanBundles(rhe: RHExtension?, config: ConfigPro?, candiatesToRemove: Array<BundleDefinition?>?) {
            if (ArrayUtil.isEmpty(candiatesToRemove)) return
            val coreBundles: BundleCollection = ConfigWebUtil.getEngine(config).getBundleCollection()

            // core master
            _cleanBundles(candiatesToRemove, coreBundles.core.getSymbolicName(), coreBundles.core.getVersion())

            // core slaves
            val it: Iterator<Bundle?> = coreBundles.getSlaves()
            var b: Bundle?
            while (it.hasNext()) {
                b = it.next()
                _cleanBundles(candiatesToRemove, b.getSymbolicName(), b.getVersion())
            }

            // all extension
            val itt: Iterator<RHExtension?> = config!!.getAllRHExtensions()!!.iterator()
            var _rhe: RHExtension?
            while (itt.hasNext()) {
                _rhe = itt.next()
                if (rhe != null && rhe.equals(_rhe)) continue
                val bundles: Array<BundleInfo?> = _rhe.getBundles(null)
                if (bundles != null) {
                    for (bi in bundles) {
                        _cleanBundles(candiatesToRemove, bi.getSymbolicName(), bi.getVersion())
                    }
                }
            }

            // now we only have BundlesDefs in the array no longer used
            for (ctr in candiatesToRemove!!) {
                if (ctr != null) OSGiUtil.removeLocalBundleSilently(ctr.getName(), ctr.getVersion(), null, true)
            }
        }

        private fun _cleanBundles(candiatesToRemove: Array<BundleDefinition?>?, name: String?, version: Version?) {
            var bd: BundleDefinition?
            for (i in candiatesToRemove.indices) {
                bd = candiatesToRemove!![i]
                if (bd != null && name.equalsIgnoreCase(bd.getName())) {
                    if (version == null) {
                        if (bd.getVersion() == null) candiatesToRemove[i] = null // remove that from array
                    } else if (bd.getVersion() != null && version.equals(bd.getVersion())) {
                        candiatesToRemove[i] = null // remove that from array
                    }
                }
            }
        }

        /**
         * returns the version if the extension is available
         *
         * @param config
         * @param id
         * @return
         * @throws PageException
         * @throws IOException
         * @throws SAXException
         */
        @Throws(PageException::class, IOException::class)
        fun hasRHExtensions(config: ConfigPro?, ed: ExtensionDefintion?): RHExtension? {
            val admin = ConfigAdmin(config, null)
            return admin._hasRHExtensions(config, ed)
        }

        @Throws(PageException::class)
        private fun setClass(el: Struct?, instanceOfClass: Class?, prefix: String?, cd: ClassDefinition?) {
            var prefix = prefix
            if (cd == null || StringUtil.isEmpty(cd.getClassName())) return
            if (prefix.endsWith("-")) prefix = prefix.substring(0, prefix!!.length() - 1)
            val hp: Boolean = !prefix.isEmpty()
            // validate class
            try {
                val clazz: Class = cd.getClazz()
                if (instanceOfClass != null && !Reflector.isInstaneOf(clazz, instanceOfClass, false)) throw ApplicationException("Class [" + clazz.getName().toString() + "] is not of type [" + instanceOfClass.getName().toString() + "]")
            } catch (e: Exception) {
                throw Caster.toPageException(e)
            }
            el.setEL(if (hp) prefix.toString() + "Class" else "class", cd.getClassName().trim())
            if (cd.isBundle()) {
                el.setEL(if (hp) prefix.toString() + "BundleName" else "bundleName", cd.getName())
                if (cd.hasVersion()) el.setEL(if (hp) prefix.toString() + "BundleVersion" else "bundleVersion", cd.getVersionAsString())
            } else {
                if (el.containsKey(if (hp) prefix.toString() + "BundleName" else "bundleName")) el.remove(if (hp) prefix.toString() + "BundleName" else "bundleName")
                if (el.containsKey(if (hp) prefix.toString() + "BundleVersion" else "bundleVersion")) el.remove(if (hp) prefix.toString() + "BundleVersion" else "bundleVersion")
            }
        }
    }

    init {
        this.config = config
        this.password = password
        root = ConfigWebFactory.loadDocument(config.getConfigFile())
    }
}