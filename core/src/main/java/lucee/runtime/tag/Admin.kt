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
package lucee.runtime.tag

import java.io.ByteArrayInputStream

/**
 *
 */
class Admin : TagImpl(), DynamicAttributes {
    private val attributes: Struct? = StructImpl()
    private var action: String? = null
    private var type: Short = 0
    private var singleMode = false
    private var password: Password? = null
    private var admin: ConfigAdmin? = null
    private var config: ConfigPro? = null
    private var configWeb: ConfigWebPro? = null
    private var adminSync: AdminSync? = null
    @Override
    fun release() {
        super.release()
        attributes.clear()
    }

    @Override
    fun setDynamicAttribute(uri: String?, localName: String?, value: Object?) {
        attributes.setEL(KeyImpl.getInstance(localName), value)
    }

    @Override
    fun setDynamicAttribute(uri: String?, localName: Collection.Key?, value: Object?) {
        attributes.setEL(localName, value)
    }

    @Override
    @Throws(PageException::class)
    fun doStartTag(): Int {
        configWeb = pageContext.getConfig() as ConfigWebPro
        config = configWeb

        // print();

        // Action
        val objAction: Object = attributes.get(KeyConstants._action)
                ?: throw ApplicationException("Missing attribute [action] for tag [admin]")
        action = StringUtil.toLowerCase(Caster.toString(objAction)).trim()

        // Generals
        if (action!!.equals("buildbundle")) {
            doBuildBundle()
            return SKIP_BODY
        }
        if (action!!.equals("readbundle")) {
            doReadBundle()
            return SKIP_BODY
        }
        if (action!!.equals("getlocales")) {
            doGetLocales()
            return SKIP_BODY
        }
        if (action!!.equals("gettimezones")) {
            doGetTimeZones()
            return SKIP_BODY
        }
        if (action!!.equals("printdebug")) {
            throw DeprecatedException("Action [printdebug] is no longer supported, use instead [getdebugdata]")
        }
        if (action!!.equals("getdebugdata")) {
            doGetDebugData()
            return SKIP_BODY
        }
        if (action!!.equals("adddump")) {
            doAddDump()
            return SKIP_BODY
        }
        if (action!!.equals("addgenericdata")) {
            doAddGenericData()
            return SKIP_BODY
        }
        if (action!!.equals("getloginsettings")) {
            doGetLoginSettings()
            return SKIP_BODY
        }

        // Type
        singleMode = config.getAdminMode() === ConfigImpl.ADMINMODE_SINGLE
        type = if (singleMode) TYPE_SERVER else toType(getString("type", "web"), true)

        // has Password
        if (action!!.equals("haspassword")) {
            val hasPassword: Boolean = if (type == TYPE_WEB) pageContext.getConfig().hasPassword() else pageContext.getConfig().hasServerPassword()
            pageContext.setVariable(getString("admin", action, "returnVariable", true), Caster.toBoolean(hasPassword))
            return SKIP_BODY
        } else if (action!!.equals("checkpassword")) {
            try {
                config.checkPassword()
            } catch (e: Exception) {
                throw Caster.toPageException(e)
            }
            return SKIP_BODY
        } else if (action!!.equals("updatepassword")) {
            try {
                (pageContext.getConfig() as ConfigWebPro).updatePassword(type != TYPE_WEB, getString("oldPassword", null), getString("admin", action, "newPassword", true))
            } catch (e: Exception) {
                throw Caster.toPageException(e)
            }
            return SKIP_BODY
        }
        try {
            _doStartTag()
        } catch (e: IOException) {
            throw Caster.toPageException(e)
        }
        return Tag.SKIP_BODY
    }

    private fun print() {
        val action: String = Caster.toString(attributes.get(KeyConstants._action, ""), "")
        if (action.toLowerCase().indexOf("update") === -1) return
        val sb = StringBuilder("set(json, \"")
        sb.append(action)
        sb.append('"')
        var e: Entry<Key?, Object?>
        val it: Iterator<Entry<Key?, Object?>?> = attributes.entryIterator()
        while (it.hasNext()) {
            e = it.next()
            if (KeyConstants._password.equals(e.getKey()) || KeyConstants._remoteclients.equals(e.getKey()) || KeyConstants._type.equals(e.getKey())
                    || KeyConstants._action.equals(e.getKey())) continue
            sb.append(", new Item(\"").append(e.getKey()).append("\")")
        }
        sb.append(");")
        aprint.e(sb)
    }

    @Throws(ApplicationException::class)
    private fun doAddDump() {
        val debugger: Debugger = pageContext.getDebugger()
        val ps: PageSource = pageContext.getCurrentTemplatePageSource()
        if (ps != null) debugger.addDump(ps, getString("admin", action, "dump", true))
    }

    @Throws(PageException::class)
    private fun doAddGenericData() {
        val debugger: Debugger = pageContext.getDebugger()
        debugger.addGenericData(getString("admin", action, "category", true), toMapStrStr(getStruct("admin", action, "data")))
    }

    @Throws(PageException::class)
    private fun toMapStrStr(struct: Struct?): Map<String?, String?>? {
        val it: Iterator<Entry<Key?, Object?>?> = struct.entryIterator()
        val map: Map<String?, String?> = HashMap<String?, String?>()
        var e: Entry<Key?, Object?>?
        while (it.hasNext()) {
            e = it.next()
            map.put(e.getKey().getString(), Caster.toString(e.getValue()))
        }
        return map
    }

    @Throws(ApplicationException::class)
    private fun toType(strType: String?, throwError: Boolean): Short {
        var strType = strType
        strType = StringUtil.toLowerCase(strType).trim()
        if ("web".equals(strType)) return TYPE_WEB else if ("server".equals(strType)) return TYPE_SERVER
        if (throwError) throw ApplicationException("Invalid value for attribute type [$strType] of tag admin", "valid values are [web, server]")
        return TYPE_WEB
    }

    @Throws(PageException::class)
    private fun doTagSchedule() {
        val schedule = Schedule()
        try {
            schedule.setPageContext(pageContext)
            schedule!!.setAction(getString("admin", action, "scheduleAction"))
            schedule!!.setTask(getString("task", null))
            schedule!!.setHidden(getBoolV("hidden", false))
            schedule!!.setReadonly(getBoolV("readonly", false))
            schedule!!.setOperation(getString("operation", null))
            schedule!!.setFile(getString("file", null))
            schedule!!.setPath(getString("path", null))
            schedule!!.setStartdate(getObject("startDate", null))
            schedule!!.setStarttime(getObject("startTime", null))
            schedule!!.setUrl(getString("url", null))
            schedule!!.setUseragent(getString("userAgent", null))
            schedule!!.setPublish(getBoolV("publish", false))
            schedule!!.setEnddate(getObject("endDate", null))
            schedule!!.setEndtime(getObject("endTime", null))
            schedule!!.setInterval(getString("interval", null))
            schedule!!.setRequesttimeout(Double.valueOf(getDouble("requestTimeOut", -1.0)))
            schedule!!.setUsername(getString("username", null))
            schedule!!.setPassword(getString("schedulePassword", null))
            schedule!!.setProxyserver(getString("proxyServer", null))
            schedule!!.setProxyuser(getString("proxyuser", null))
            schedule!!.setProxypassword(getString("proxyPassword", null))
            schedule!!.setResolveurl(getBoolV("resolveURL", false))
            schedule!!.setPort(Double.valueOf(getDouble("port", -1.0)))
            schedule!!.setProxyport(Double.valueOf(getDouble("proxyPort", 80.0)))
            schedule!!.setUnique(getBoolV("unique", false))
            var rtn = getString("returnvariable", null)
            if (StringUtil.isEmpty(rtn)) rtn = getString("result", "cfschedule")
            schedule!!.setResult(rtn)
            schedule!!.doStartTag()
        } finally {
            schedule!!.release()
            adminSync.broadcast(attributes, config)
            adminSync.broadcast(attributes, config)
        }
    }

    /*
	 * private void doTagSearch() throws PageException { Search search=new Search(); try {
	 * 
	 * search.setPageContext(pageContext);
	 * 
	 * search.setName(getString("admin",action,"name"));
	 * search.setCollection(getString("admin",action,"collection"));
	 * search.setType(getString("type",null)); search.setMaxrows(getDouble("maxRows",-1));
	 * search.setStartrow(getDouble("startRow",1)); search.setCategory(getString("category",null));
	 * search.setCategorytree(getString("categoryTree",null));
	 * search.setStatus(getString("status",null)); search.setSuggestions(getString("suggestions",null));
	 * 
	 * search.doStartTag(); } finally { search.release(); } }
	 */
    @Throws(PageException::class)
    private fun doTagIndex() {
        val index = Index()
        try {
            index.setPageContext(pageContext)
            index!!.setCollection(getString("admin", action, "collection"))
            index!!.setAction(getString("admin", action, "indexAction"))
            index!!.setType(getString("indexType", null))
            index!!.setTitle(getString("title", null))
            index!!.setKey(getString("key", null))
            index!!.setBody(getString("body", null))
            index!!.setCustom1(getString("custom1", null))
            index!!.setCustom2(getString("custom2", null))
            index!!.setCustom3(getString("custom3", null))
            index!!.setCustom4(getString("custom4", null))
            index!!.setUrlpath(getString("URLpath", null))
            index!!.setExtensions(getString("extensions", null))
            index!!.setQuery(getString("query", null))
            index!!.setRecurse(getBoolV("recurse", false))
            index!!.setLanguage(getString("language", null))
            index!!.setCategory(getString("category", null))
            index!!.setCategorytree(getString("categoryTree", null))
            index!!.setStatus(getString("status", null))
            index!!.setPrefix(getString("prefix", null))
            index!!.doStartTag()
        } finally {
            index!!.release()
            adminSync.broadcast(attributes, config)
        }
    }

    @Throws(PageException::class)
    private fun doTagCollection() {
        val coll: lucee.runtime.tag.Collection = Collection()
        try {
            coll.setPageContext(pageContext)

            // coll.setCollection(getString("admin",action,"collection"));
            coll!!.setAction(getString("collectionAction", null))
            coll!!.setCollection(getString("collection", null))
            coll!!.setPath(getString("path", null))
            coll!!.setLanguage(getString("language", null))
            coll!!.setName(getString("name", null))
            coll!!.doStartTag()
        } finally {
            coll!!.release()
            adminSync.broadcast(attributes, config)
        }
    }

    /**
     * @throws PageException
     */
    @Throws(PageException::class, IOException::class)
    private fun _doStartTag() {
        configWeb = pageContext.getConfig() as ConfigWebPro
        config = configWeb

        // getToken
        if (action!!.equals("gettoken")) {
            doGetToken()
            return
        }

        // schedule
        if (action!!.equals("schedule")) {
            doTagSchedule()
            return
        }
        // search
        if (action!!.equals("collection")) {
            doTagCollection()
            return
        }
        // index
        if (action!!.equals("index")) {
            doTagIndex()
            return
        }
        // cluster
        if (action!!.equals("setcluster")) {
            doSetCluster()
            return
        }
        if (action!!.equals("getcluster")) {
            doGetCluster()
            return
        }
        if (action!!.equals("getextension")) {
            if (type == TYPE_SERVER) doGetRHServerExtension() else doGetRHExtension()
            return
        }
        if (action!!.equals("getextensions") || action!!.equals("getrhextensions")) {
            if (type == TYPE_SERVER) doGetRHServerExtensions() else doGetRHExtensions()
            return
        }
        if (action!!.equals("getserverextensions") || action!!.equals("getrhserverextensions")) {
            doGetRHServerExtensions()
            return
        }
        if (check("hashpassword", ACCESS_FREE)) {
            val raw = getString("admin", action, "pw")
            var pw: Password? = PasswordImpl.passwordToCompare(pageContext.getConfig(), type != TYPE_WEB, raw)
            val changed: Password = (pageContext.getConfig() as ConfigWebPro).updatePasswordIfNecessary(type == TYPE_SERVER, raw)
            if (changed != null) pw = changed
            pageContext.setVariable(getString("admin", action, "returnVariable"), pw.getPassword())
            return  // do not remove
        }
        try {
            // Password
            val strPW = getString("password", "")
            val tmp: Password = if (type == TYPE_SERVER) configWeb.isServerPasswordEqual(strPW) else config.isPasswordEqual(strPW) // hash password if
            // necessary (for
            // backward
            // compatibility)
            password = if (tmp != null) tmp else null

            // Config
            if (type == TYPE_SERVER) config = pageContext.getConfig().getConfigServer(password) as ConfigPro
            adminSync = config.getAdminSync()
            admin = ConfigAdmin.newInstance(config, password)
        } catch (e: Exception) {
            throw Caster.toPageException(e)
        }
        if (check("connect", ACCESS_FREE)) {
            ConfigWebUtil.checkPassword(config, null, password)
            ConfigWebUtil.checkGeneralReadAccess(config, password)
            try {
                if (config is ConfigServer) (pageContext as PageContextImpl?).setServerPassword(password)
            } catch (t: Throwable) {
                ExceptionUtil.rethrowIfNecessary(t)
            }
        } else if (check("getinfo", ACCESS_FREE) && check2(ACCESS_READ)) doGetInfo() else if (check("surveillance", ACCESS_FREE) && check2(ACCESS_READ)) doSurveillance() else if (check("getRegional", ACCESS_FREE) && check2(ACCESS_READ)) doGetRegional() else if (check("isMonitorEnabled", ACCESS_NOT_WHEN_WEB) && check2(ACCESS_READ)) doIsMonitorEnabled() else if (check("resetORMSetting", ACCESS_FREE) && check2(ACCESS_READ)) doResetORMSetting() else if (check("getORMSetting", ACCESS_FREE) && check2(ACCESS_READ)) doGetORMSetting() else if (check("getORMEngine", ACCESS_FREE) && check2(ACCESS_READ)) doGetORMEngine() else if (check("updateORMSetting", ACCESS_FREE) && check2(ACCESS_READ)) doUpdateORMSetting() else if (check("getApplicationListener", ACCESS_FREE) && check2(ACCESS_READ)) doGetApplicationListener() else if (check("getProxy", ACCESS_FREE) && check2(ACCESS_READ)) doGetProxy() else if (check("getCharset", ACCESS_FREE) && check2(ACCESS_READ)) doGetCharset() else if (check("getComponent", ACCESS_FREE) && check2(ACCESS_READ)) doGetComponent() else if (check("getScope", ACCESS_FREE) && check2(ACCESS_READ)) doGetScope() else if (check("getDevelopMode", ACCESS_FREE) && check2(ACCESS_READ)) doGetDevelopMode() else if (check("getApplicationSetting", ACCESS_FREE) && check2(ACCESS_READ)) doGetApplicationSetting() else if (check("getQueueSetting", ACCESS_FREE) && check2(ACCESS_READ)) doGetQueueSetting() else if (check("getOutputSetting", ACCESS_FREE) && check2(ACCESS_READ)) doGetOutputSetting() else if (check("getDatasourceSetting", ACCESS_FREE) && check2(ACCESS_READ)) doGetDatasourceSetting() else if (check("getCustomTagSetting", ACCESS_FREE) && check2(ACCESS_READ)) doGetCustomTagSetting() else if (check("getDatasource", ACCESS_FREE) && check2(ACCESS_READ)) doGetDatasource() else if (check("getDatasources", ACCESS_FREE) && check2(ACCESS_READ)) doGetDatasources() else if (check("getJDBCDrivers", ACCESS_FREE) && check2(ACCESS_READ)) doGetJDBCDrivers() else if (check("getCacheConnections", ACCESS_FREE) && check2(ACCESS_READ)) doGetCacheConnections() else if (check("getCacheConnection", ACCESS_FREE) && check2(ACCESS_READ)) doGetCacheConnection() else if (check("getCacheDefaultConnection", ACCESS_FREE) && check2(ACCESS_READ)) doGetCacheDefaultConnection() else if (check("getRemoteClients", ACCESS_FREE) && check2(ACCESS_READ)) doGetRemoteClients() else if (check("getRemoteClient", ACCESS_FREE) && check2(ACCESS_READ)) doGetRemoteClient() else if (check("hasRemoteClientUsage", ACCESS_FREE) && check2(ACCESS_READ)) doHasRemoteClientUsage() else if (check("getRemoteClientUsage", ACCESS_FREE) && check2(ACCESS_READ)) doGetRemoteClientUsage() else if (check("getSpoolerTasks", ACCESS_FREE) && check2(ACCESS_READ)) doGetSpoolerTasks() else if (check("getPerformanceSettings", ACCESS_FREE) && check2(ACCESS_READ)) doGetPerformanceSettings() else if (check("getLogSettings", ACCESS_FREE) && check2(ACCESS_READ)) doGetLogSettings() else if (check("getCompilerSettings", ACCESS_FREE) && check2(ACCESS_READ)) doGetCompilerSettings() else if (check("updatePerformanceSettings", ACCESS_FREE) && check2(ACCESS_WRITE)) doUpdatePerformanceSettings() else if (check("updateCompilerSettings", ACCESS_FREE) && check2(ACCESS_WRITE)) doUpdateCompilerSettings() else if (check("getGatewayentries", ACCESS_NOT_WHEN_SERVER) && check2(ACCESS_READ)) doGetGatewayEntries() else if (check("getGatewayentry", ACCESS_NOT_WHEN_SERVER) && check2(ACCESS_READ)) doGetGatewayEntry() else if (check("getRunningThreads", ACCESS_FREE) && check2(ACCESS_READ)) doGetRunningThreads() else if (check("getMonitors", ACCESS_NOT_WHEN_WEB) && check2(ACCESS_READ)) doGetMonitors() else if (check("getMonitor", ACCESS_NOT_WHEN_WEB) && check2(ACCESS_READ)) doGetMonitor() else if (check("getBundles", ACCESS_FREE) && check2(ACCESS_READ)) doGetBundles() else if (check("getBundle", ACCESS_FREE) && check2(ACCESS_READ)) doGetBundle() else if (check("getExecutionLog", ACCESS_FREE) && check2(ACCESS_READ)) doGetExecutionLog() else if (check("gateway", ACCESS_NOT_WHEN_SERVER) && check2(ACCESS_READ)) doGateway() else if (check("getRemoteClientTasks", ACCESS_FREE) && check2(ACCESS_READ)) doGetSpoolerTasks() else if (check("getDatasourceDriverList", ACCESS_FREE) && check2(ACCESS_READ)) doGetDatasourceDriverList() else if (check("getDebuggingList", ACCESS_FREE) && check2(ACCESS_READ)) doGetDebuggingList() else if (check("getLoggedDebugData", ACCESS_FREE)) // no password necessary for this
            doGetLoggedDebugData() else if (check("PurgeDebugPool", ACCESS_FREE) && check2(ACCESS_WRITE)) doPurgeDebugPool() else if (check("PurgeExpiredSessions", ACCESS_FREE) && check2(ACCESS_WRITE)) doPurgeExpiredSessions() else if (check("getDebugSetting", ACCESS_FREE) && check2(ACCESS_READ)) doGetDebugSetting() else if (check("getSSLCertificate", ACCESS_NOT_WHEN_WEB) && check2(ACCESS_READ)) doGetSSLCertificate() else if (check("getPluginDirectory", ACCESS_FREE) && check2(ACCESS_READ)) doGetPluginDirectory() else if (check("getPlugins", ACCESS_FREE) && check2(ACCESS_READ)) doGetPlugins() else if (check("updatePlugin", ACCESS_FREE) && check2(ACCESS_WRITE)) doUpdatePlugin() else if (check("removePlugin", ACCESS_FREE) && check2(ACCESS_WRITE)) doRemovePlugin() else if (check("getContextDirectory", ACCESS_NOT_WHEN_WEB) && check2(ACCESS_READ)) contextDirectory else if (check("updateContext", ACCESS_NOT_WHEN_WEB) && check2(ACCESS_WRITE)) doUpdateContext() else if (check("removeContext", ACCESS_NOT_WHEN_WEB) && check2(ACCESS_WRITE)) doRemoveContext() else if (check("getJars", ACCESS_FREE) && check2(ACCESS_READ)) doGetJars() else if (check("getFlds", ACCESS_FREE) && check2(ACCESS_READ)) doGetFLDs() else if (check("getTlds", ACCESS_FREE) && check2(ACCESS_READ)) doGetTLDs() else if (check("getLocalExtension", ACCESS_FREE) && check2(ACCESS_READ)) doGetLocalExtension() else if (check("getLocalExtensions", ACCESS_FREE) && check2(ACCESS_READ)) doGetLocalExtensions() else if (check("getMailSetting", ACCESS_FREE) && check2(ACCESS_READ)) doGetMailSetting() else if (check("getTaskSetting", ACCESS_FREE) && check2(ACCESS_READ)) doGetTaskSetting() else if (check("getMailServers", ACCESS_FREE) && check2(ACCESS_READ)) doGetMailServers() else if (check("getMapping", ACCESS_FREE) && check2(ACCESS_READ)) doGetMapping() else if (check("getMappings", ACCESS_FREE) && check2(ACCESS_READ)) doGetMappings() else if (check("getRestMappings", ACCESS_FREE) && check2(ACCESS_READ)) doGetRestMappings() else if (check("getRestSettings", ACCESS_FREE) && check2(ACCESS_READ)) doGetRestSettings() else if ((check("getRHExtensionProviders", ACCESS_FREE) || check("getExtensionProviders", ACCESS_FREE)) && check2(ACCESS_READ)) doGetRHExtensionProviders() else if (check("getExtensionInfo", ACCESS_FREE) && check2(ACCESS_READ)) doGetExtensionInfo() else if (check("getCustomTagMappings", ACCESS_FREE) && check2(ACCESS_READ)) doGetCustomTagMappings() else if (check("getComponentMappings", ACCESS_FREE) && check2(ACCESS_READ)) doGetComponentMappings() else if (check("getCfxTags", ACCESS_FREE) && check2(ACCESS_READ)) doGetCFXTags() else if (check("getJavaCfxTags", ACCESS_FREE) && check2(ACCESS_READ)) doGetJavaCFXTags() else if (check("getDebug", ACCESS_FREE) && check2(ACCESS_READ)) doGetDebug() else if (check("getSecurity", ACCESS_FREE) && check2(ACCESS_READ)) doGetSecurity() else if (check("getDebugEntry", ACCESS_FREE)) doGetDebugEntry() else if (check("getError", ACCESS_FREE) && check2(ACCESS_READ)) doGetError() else if (check("getRegex", ACCESS_FREE) && check2(ACCESS_READ)) doGetRegex() else if (check("verifyremoteclient", ACCESS_FREE) && check2(ACCESS_READ)) doVerifyRemoteClient() else if (check("verifyDatasource", ACCESS_FREE) && check2(ACCESS_READ)) doVerifyDatasource() else if (check("verifyCacheConnection", ACCESS_FREE) && check2(ACCESS_READ)) doVerifyCacheConnection() else if (check("verifyMailServer", ACCESS_FREE) && check2(ACCESS_READ)) doVerifyMailServer() else if (check("verifyExtensionProvider", ACCESS_FREE) && check2(ACCESS_READ)) doVerifyExtensionProvider() else if (check("verifyJavaCFX", ACCESS_FREE) && check2(ACCESS_READ)) doVerifyJavaCFX() else if (check("verifyCFX", ACCESS_FREE) && check2(ACCESS_READ)) doVerifyCFX() else if (check("resetId", ACCESS_FREE) && check2(ACCESS_WRITE)) doResetId() else if (check("updateLoginSettings", ACCESS_NOT_WHEN_WEB) && check2(ACCESS_WRITE)) doUpdateLoginSettings() else if (check("updateLogSettings", ACCESS_FREE) && check2(ACCESS_WRITE)) doUpdateLogSettings() else if (check("updateJar", ACCESS_FREE) && check2(ACCESS_WRITE)) doUpdateJar() else if (check("updateSSLCertificate", ACCESS_NOT_WHEN_WEB) && check2(ACCESS_WRITE)) doUpdateSSLCertificate() else if (check("updateMonitorEnabled", ACCESS_NOT_WHEN_WEB) && check2(ACCESS_WRITE)) doUpdateMonitorEnabled() else if (check("updateTLD", ACCESS_FREE) && check2(ACCESS_WRITE)) doUpdateTLD() else if (check("updateFLD", ACCESS_FREE) && check2(ACCESS_WRITE)) doUpdateFLD() else if (check("updateFilesystem", ACCESS_FREE) && check2(ACCESS_WRITE)) doUpdateFilesystem() else if (check("updateregional", ACCESS_FREE) && check2(ACCESS_WRITE)) doUpdateRegional() else if (check("updateApplicationListener", ACCESS_FREE) && check2(ACCESS_WRITE)) doUpdateApplicationListener() else if (check("updateCachedWithin", ACCESS_FREE) && check2(ACCESS_WRITE)) doUpdateCachedWithin() else if (check("updateproxy", ACCESS_FREE) && check2(ACCESS_WRITE)) doUpdateProxy() else if (check("updateCharset", ACCESS_FREE) && check2(ACCESS_WRITE)) doUpdateCharset() else if (check("updatecomponent", ACCESS_FREE) && check2(ACCESS_WRITE)) doUpdateComponent() else if (check("updatescope", ACCESS_FREE) && check2(ACCESS_WRITE)) doUpdateScope() else if (check("updateDevelopMode", ACCESS_FREE) && check2(ACCESS_WRITE)) doUpdateDevelopMode() else if (check("updateRestSettings", ACCESS_FREE) && check2(ACCESS_WRITE)) doUpdateRestSettings() else if (check("updateRestMapping", ACCESS_FREE) && check2(ACCESS_WRITE)) doUpdateRestMapping() else if (check("removeRestMapping", ACCESS_FREE) && check2(ACCESS_WRITE)) doRemoveRestMapping() else if (check("updateApplicationSetting", ACCESS_FREE) && check2(ACCESS_WRITE)) doUpdateApplicationSettings() else if (check("updateOutputSetting", ACCESS_FREE) && check2(ACCESS_WRITE)) doUpdateOutputSettings() else if (check("updateQueueSetting", ACCESS_NOT_WHEN_WEB) && check2(ACCESS_WRITE)) doUpdateQueueSettings() else if (check("updatepsq", ACCESS_FREE) && check2(ACCESS_WRITE)) doUpdatePSQ() else if (check("updatedatasource", ACCESS_FREE) && check2(ACCESS_WRITE)) doUpdateDatasource() else if (check("updateJDBCDriver", ACCESS_FREE) && check2(ACCESS_WRITE)) doUpdateJDBCDriver() else if (check("updateCacheDefaultConnection", ACCESS_FREE) && check2(ACCESS_WRITE)) doUpdateCacheDefaultConnection() else if (check("updateCacheConnection", ACCESS_FREE) && check2(ACCESS_WRITE)) doUpdateCacheConnection() else if (check("updateremoteclient", ACCESS_FREE) && check2(ACCESS_WRITE)) doUpdateRemoteClient() else if (check("updateRemoteClientUsage", ACCESS_FREE) && check2(ACCESS_WRITE)) doUpdateRemoteClientUsage() else if (check("updatemailsetting", ACCESS_FREE) && check2(ACCESS_WRITE)) doUpdateMailSetting() else if (check("updatemailserver", ACCESS_FREE) && check2(ACCESS_WRITE)) doUpdateMailServer() else if (check("updatetasksetting", ACCESS_FREE) && check2(ACCESS_WRITE)) doUpdateTaskSetting() else if (check("updatemapping", ACCESS_FREE) && check2(ACCESS_WRITE)) doUpdateMapping() else if (check("updatecustomtag", ACCESS_FREE) && check2(ACCESS_WRITE)) doUpdateCustomTag() else if (check("updateComponentMapping", ACCESS_FREE) && check2(ACCESS_WRITE)) doUpdateComponentMapping() else if (check("stopThread", ACCESS_NOT_WHEN_WEB) && check2(ACCESS_WRITE)) doStopThread() else if (check("updateAdminMode", ACCESS_NOT_WHEN_WEB) && check2(ACCESS_WRITE)) doUpdateAdminMode() else if (check("updatejavacfx", ACCESS_FREE) && check2(ACCESS_WRITE)) doUpdateJavaCFX() else if (check("updatedebug", ACCESS_FREE) && check2(ACCESS_WRITE)) doUpdateDebug() else if (check("updatesecurity", ACCESS_FREE) && check2(ACCESS_WRITE)) doUpdateSecurity() else if (check("updatedebugentry", ACCESS_FREE) && check2(ACCESS_WRITE)) doUpdateDebugEntry() else if (check("updatedebugsetting", ACCESS_FREE) && check2(ACCESS_WRITE)) doUpdateDebugSetting() else if (check("updateerror", ACCESS_FREE) && check2(ACCESS_WRITE)) doUpdateError() else if (check("updateregex", ACCESS_FREE) && check2(ACCESS_WRITE)) doUpdateRegex() else if (check("updateCustomTagSetting", ACCESS_FREE) && check2(ACCESS_WRITE)) doUpdateCustomTagSetting() else if ((check("updateRHExtension", ACCESS_FREE) || check("updateExtension", ACCESS_FREE)) && check2(ACCESS_WRITE)) doUpdateRHExtension(true) else if ((check("removeRHExtension", ACCESS_FREE) || check("removeExtension", ACCESS_FREE)) && check2(ACCESS_WRITE)) doRemoveRHExtension() else if (check("updateExtensionProvider", ACCESS_FREE) && check2(ACCESS_WRITE)) doUpdateExtensionProvider() else if ((check("updateRHExtensionProvider", ACCESS_FREE) || check("updateExtensionProvider", ACCESS_FREE)) && check2(ACCESS_WRITE)) doUpdateRHExtensionProvider() else if (check("updateExtensionInfo", ACCESS_FREE) && check2(ACCESS_WRITE)) doUpdateExtensionInfo() else if (check("updateGatewayEntry", ACCESS_NOT_WHEN_SERVER) && check2(ACCESS_WRITE)) doUpdateGatewayEntry() else if (check("updateMonitor", ACCESS_NOT_WHEN_WEB) && check2(ACCESS_WRITE)) doUpdateMonitor() else if (check("updateCacheHandler", ACCESS_NOT_WHEN_WEB) && check2(ACCESS_WRITE)) doUpdateCacheHandler() else if (check("updateORMEngine", ACCESS_FREE) && check2(ACCESS_WRITE)) doUpdateORMEngine() else if (check("updateExecutionLog", ACCESS_FREE) && check2(ACCESS_WRITE)) doUpdateExecutionLog() else if (check("removeMonitor", ACCESS_NOT_WHEN_WEB) && check2(ACCESS_WRITE)) doRemoveMonitor() else if (check("removeCacheHandler", ACCESS_NOT_WHEN_WEB) && check2(ACCESS_WRITE)) doRemoveCacheHandler() else if (check("removeORMEngine", ACCESS_FREE) && check2(ACCESS_WRITE)) doRemoveORMEngine() else if (check("removebundle", ACCESS_FREE) && check2(ACCESS_WRITE)) doRemoveBundle() else if (check("removeTLD", ACCESS_FREE) && check2(ACCESS_WRITE)) doRemoveTLD() else if (check("removeFLD", ACCESS_FREE) && check2(ACCESS_WRITE)) doRemoveFLD() else if (check("removeJDBCDriver", ACCESS_FREE) && check2(ACCESS_WRITE)) doRemoveJDBCDriver() else if (check("removedatasource", ACCESS_FREE) && check2(ACCESS_WRITE)) doRemoveDatasource() else if (check("removeCacheConnection", ACCESS_FREE) && check2(ACCESS_WRITE)) doRemoveCacheConnection() else if (check("removeremoteclient", ACCESS_FREE) && check2(ACCESS_WRITE)) doRemoveRemoteClient() else if (check("removeRemoteClientUsage", ACCESS_FREE) && check2(ACCESS_WRITE)) doRemoveRemoteClientUsage() else if (check("removeSpoolerTask", ACCESS_FREE) && check2(ACCESS_WRITE)) doRemoveSpoolerTask() else if (check("removeAllSpoolerTask", ACCESS_FREE) && check2(ACCESS_WRITE)) doRemoveAllSpoolerTask() else if (check("removeRemoteClientTask", ACCESS_FREE) && check2(ACCESS_WRITE)) doRemoveSpoolerTask() else if (check("executeSpoolerTask", ACCESS_FREE) && check2(ACCESS_WRITE)) doExecuteSpoolerTask() else if (check("executeRemoteClientTask", ACCESS_FREE) && check2(ACCESS_WRITE)) doExecuteSpoolerTask() else if (check("removemailserver", ACCESS_FREE) && check2(ACCESS_WRITE)) doRemoveMailServer() else if (check("removemapping", ACCESS_FREE) && check2(ACCESS_WRITE)) doRemoveMapping() else if (check("removecustomtag", ACCESS_FREE) && check2(ACCESS_WRITE)) doRemoveCustomTag() else if (check("removecomponentmapping", ACCESS_FREE) && check2(ACCESS_WRITE)) doRemoveComponentMapping() else if (check("removecfx", ACCESS_FREE) && check2(ACCESS_WRITE)) doRemoveCFX() else if (check("removeExtension", ACCESS_FREE) && check2(ACCESS_WRITE)) doRemoveExtension() else if (check("removeExtensionProvider", ACCESS_FREE) && check2(ACCESS_WRITE)) doRemoveExtensionProvider() else if (check("removeRHExtensionProvider", ACCESS_FREE) && check2(ACCESS_WRITE)) doRemoveRHExtensionProvider() else if (check("removeDefaultPassword", ACCESS_FREE) && check2(ACCESS_WRITE)) doRemoveDefaultPassword() else if (check("removeGatewayEntry", ACCESS_NOT_WHEN_SERVER) && check2(ACCESS_WRITE)) doRemoveGatewayEntry() else if (check("removeDebugEntry", ACCESS_FREE) && check2(ACCESS_WRITE)) doRemoveDebugEntry() else if (check("removeCacheDefaultConnection", ACCESS_FREE) && check2(ACCESS_WRITE)) doRemoveCacheDefaultConnection() else if (check("removeLogSetting", ACCESS_FREE) && check2(ACCESS_WRITE)) doRemoveLogSetting() else if (check("storageGet", ACCESS_FREE) && check2(ACCESS_READ)) doStorageGet() else if (check("storageSet", ACCESS_FREE) && check2(ACCESS_WRITE)) doStorageSet() else if (check("getdefaultpassword", ACCESS_FREE) && check2(ACCESS_READ)) doGetDefaultPassword() else if (check("getContexts", ACCESS_FREE) && check2(ACCESS_READ)) doGetContexts() else if (check("getContextes", ACCESS_FREE) && check2(ACCESS_READ)) doGetContexts() else if (check("updatedefaultpassword", ACCESS_FREE) && check2(ACCESS_WRITE)) doUpdateDefaultPassword() else if (check("hasindividualsecurity", ACCESS_FREE) && check2(ACCESS_READ)) doHasIndividualSecurity() else if (check("resetpassword", ACCESS_FREE) && check2(ACCESS_WRITE)) doResetPassword() else if (check("stopThread", ACCESS_NOT_WHEN_WEB) && check2(ACCESS_WRITE)) doStopThread() else if (check("updateAuthKey", ACCESS_NOT_WHEN_WEB) && check2(ACCESS_WRITE)) doUpdateAuthKey() else if (check("removeAuthKey", ACCESS_NOT_WHEN_WEB) && check2(ACCESS_WRITE)) doRemoveAuthKey() else if (check("listAuthKey", ACCESS_NOT_WHEN_WEB) && check2(ACCESS_WRITE)) doListAuthKey() else if (check("updateAPIKey", ACCESS_FREE) && check2(ACCESS_WRITE)) doUpdateAPIKey() else if (check("removeAPIKey", ACCESS_FREE) && check2(ACCESS_WRITE)) doRemoveAPIKey() else if (check("getAPIKey", ACCESS_FREE) && check2(ACCESS_READ)) doGetAPIKey() else if (check("createsecuritymanager", ACCESS_NOT_WHEN_WEB) && check2(ACCESS_WRITE)) doCreateSecurityManager() else if (check("getsecuritymanager", ACCESS_NOT_WHEN_WEB) && check2(ACCESS_READ)) doGetSecurityManager() else if (check("removesecuritymanager", ACCESS_NOT_WHEN_WEB) && check2(ACCESS_WRITE)) doRemoveSecurityManager() else if (check("getdefaultsecuritymanager", ACCESS_NOT_WHEN_WEB) && check2(ACCESS_READ)) doGetDefaultSecurityManager() else if (check("updatesecuritymanager", ACCESS_NOT_WHEN_WEB) && check2(ACCESS_WRITE)) doUpdateSecurityManager() else if (check("updatedefaultsecuritymanager", ACCESS_NOT_WHEN_WEB) && check2(ACCESS_WRITE)) doUpdateDefaultSecurityManager() else if (check("compileMapping", ACCESS_FREE) && check2(ACCESS_WRITE)) doCompileMapping() else if (check("compileComponentMapping", ACCESS_FREE) && check2(ACCESS_WRITE)) doCompileComponentMapping() else if (check("compileCTMapping", ACCESS_FREE) && check2(ACCESS_WRITE)) doCompileCTMapping() else if (check("createArchive", ACCESS_FREE) && check2(ACCESS_WRITE)) doCreateArchive(MAPPING_REGULAR) else if (check("createComponentArchive", ACCESS_FREE) && check2(ACCESS_WRITE)) doCreateArchive(MAPPING_CFC) else if (check("createCTArchive", ACCESS_FREE) && check2(ACCESS_WRITE)) doCreateArchive(MAPPING_CT) else if (check("reload", ACCESS_FREE) && check2(ACCESS_WRITE)) doReload() else if (check("getResourceProviders", ACCESS_FREE) && check2(ACCESS_READ)) doGetResourceProviders() else if (check("updateResourceProvider", ACCESS_FREE) && check2(ACCESS_WRITE)) doUpdateResourceProvider() else if (check("updateDefaultResourceProvider", ACCESS_FREE) && check2(ACCESS_WRITE)) doUpdateDefaultResourceProvider() else if (check("removeResourceProvider", ACCESS_FREE) && check2(ACCESS_WRITE)) doRemoveResourceProvider() else if (check("getAdminSyncClass", ACCESS_FREE) && check2(ACCESS_READ)) doGetAdminSyncClass() else if (check("updateAdminSyncClass", ACCESS_FREE) && check2(ACCESS_WRITE)) doUpdateAdminSyncClass() else if (check("getVideoExecuterClass", ACCESS_FREE) && check2(ACCESS_READ)) doGetVideoExecuterClass() else if (check("updateVideoExecuterClass", ACCESS_FREE) && check2(ACCESS_WRITE)) doUpdateVideoExecuterClass() else if (check("terminateRunningThread", ACCESS_FREE) && check2(ACCESS_WRITE)) doTerminateRunningThread() else if (check("updateLabel", ACCESS_NOT_WHEN_WEB) && check2(ACCESS_WRITE)) doUpdateLabel() else if (check("restart", ACCESS_NOT_WHEN_WEB) && check2(ACCESS_WRITE)) doRestart() else if (check("runUpdate", ACCESS_NOT_WHEN_WEB) && check2(ACCESS_WRITE)) doRunUpdate() else if (check("removeUpdate", ACCESS_NOT_WHEN_WEB) && check2(ACCESS_WRITE)) doRemoveUpdate() else if (check("changeVersionTo", ACCESS_NOT_WHEN_WEB) && check2(ACCESS_WRITE)) doChangeVersionTo() else if (check("getUpdate", ACCESS_NOT_WHEN_WEB) && check2(ACCESS_WRITE)) doGetUpdate() else if (check("getMinVersion", ACCESS_NOT_WHEN_WEB) && check2(ACCESS_READ)) minVersion else if (check("getLoaderInfo", ACCESS_NOT_WHEN_WEB) && check2(ACCESS_READ)) loaderInfo else if (check("listPatches", ACCESS_NOT_WHEN_WEB) && check2(ACCESS_READ)) listPatches() else if (check("updateupdate", ACCESS_NOT_WHEN_WEB) && check2(ACCESS_WRITE)) doUpdateUpdate() else if (check("getSerial", ACCESS_FREE) && check2(ACCESS_READ)) doGetSerial() else if (check("updateSerial", ACCESS_NOT_WHEN_WEB) && check2(ACCESS_WRITE)) doUpdateSerial() else if (check("heapDump", ACCESS_NOT_WHEN_WEB) && check2(ACCESS_WRITE)) doHeapDump() else if (check("securitymanager", ACCESS_FREE) && check2(ACCESS_READ)) doSecurityManager() else throw ApplicationException("Invalid action [$action] for tag admin")
    }

    @Throws(SecurityException::class)
    private fun check2(accessRW: Short): Boolean {
        if (accessRW == ACCESS_READ) ConfigWebUtil.checkGeneralReadAccess(config, password) else if (accessRW == ACCESS_WRITE) ConfigWebUtil.checkGeneralWriteAccess(config, password)
        /*
		 * else if(accessRW==CHECK_PW) { ConfigWebUtil.checkGeneralReadAccess(config,password);
		 * ConfigWebUtil.checkPassword(config,null,password); }
		 */return true
    }

    @Throws(ApplicationException::class)
    private fun check(action: String?, access: Short): Boolean {
        if (this.action.equalsIgnoreCase(action)) {
            if (access == ACCESS_FREE) {
            } else if (access == ACCESS_NOT_WHEN_SERVER) {
                throwNoAccessWhenServer()
            } else if (access == ACCESS_NOT_WHEN_WEB) {
                throwNoAccessWhenWeb()
            } else if (access == ACCESS_NEVER) {
                throwNoAccessWhenServer()
                throwNoAccessWhenServer()
            }
            return true
        }
        return false
    }

    @Throws(PageException::class)
    private fun doRunUpdate() {
        admin.runUpdate(password)
        adminSync.broadcast(attributes, config)
    }

    @Throws(PageException::class)
    private fun doRemoveUpdate() {
        val onlyLatest = getBoolV("onlyLatest", false)
        if (onlyLatest) admin.removeLatestUpdate(password) else admin.removeUpdate(password)
        adminSync.broadcast(attributes, config)
    }

    @Throws(PageException::class)
    private fun doChangeVersionTo() {
        try {
            val version: Version = OSGiUtil.toVersion(getString("admin", "changeVersionTo", "version"))
            admin.changeVersionTo(version, password, pageContext.getConfig().getIdentification())
            adminSync.broadcast(attributes, config)
        } catch (e: BundleException) {
            throw Caster.toPageException(e)
        }
    }

    @Throws(PageException::class)
    private fun doRestart() {
        admin.restart(password)
        adminSync.broadcast(attributes, config)
    }

    @Throws(PageException::class)
    private fun doCreateArchive(mappingType: Short) {
        val virtual: String = getString("admin", action, "virtual").toLowerCase()
        val strFile = getString("admin", action, "file")
        val file: Resource = ResourceUtil.toResourceNotExisting(pageContext, strFile)
        val addCFMLFiles = getBoolV("addCFMLFiles", true)
        val addNonCFMLFiles = getBoolV("addNonCFMLFiles", true)
        val ignoreScopes = getBool("ignoreScopes", null)

        // compile
        val mapping: MappingImpl = doCompileMapping(mappingType, virtual, true, ignoreScopes) as MappingImpl?
                ?: throw ApplicationException("There is no mapping for [$virtual]")

        // class files
        if (!mapping.hasPhysical()) throw ApplicationException("Mapping [$virtual] has no physical directory")
        val classRoot: Resource = mapping.getClassRootDirectory()
        val temp: Resource = SystemUtil.getTempDirectory().getRealResource("mani-" + IDGenerator.stringId())
        val mani: Resource = temp.getRealResource("META-INF/MANIFEST.MF")
        try {
            if (file.exists()) file.delete()
            if (!file.exists()) file.createFile(true)
            val filter: ResourceFilter?

            // include everything, no filter needed
            if (addCFMLFiles && addNonCFMLFiles) filter = null else if (addCFMLFiles) {
                if (mappingType == MAPPING_CFC) filter = ExtensionResourceFilter(ArrayUtil.toArray(Constants.getComponentExtensions(), "class", "MF"), true, true) else filter = ExtensionResourceFilter(ArrayUtil.toArray(Constants.getExtensions(), "class", "MF"), true, true)
            } else if (addNonCFMLFiles) {
                filter = NotResourceFilter(ExtensionResourceFilter(Constants.getExtensions(), false, true))
            } else {
                filter = ExtensionResourceFilter(arrayOf<String?>("class", "MF"), true, true)
            }
            val id: String = HashUtil.create64BitHashAsString(mapping.getStrPhysical(), Character.MAX_RADIX)
            // String id = MD5.getDigestAsString(mapping.getStrPhysical());
            val type: String
            type = if (mappingType == MAPPING_CFC) "cfc" else if (mappingType == MAPPING_CT) "ct" else "regular"
            val token: String = HashUtil.create64BitHashAsString(System.currentTimeMillis().toString() + "", Character.MAX_RADIX)

            // create manifest
            val mf = Manifest()
            // StringBuilder manifest=new StringBuilder();

            // Write OSGi specific stuff
            val attrs: Attributes = mf.getMainAttributes()
            attrs.putValue("Bundle-ManifestVersion", Caster.toString(BundleBuilderFactory.MANIFEST_VERSION))
            attrs.putValue("Bundle-SymbolicName", id)
            attrs.putValue("Bundle-Name", ListUtil.trim(mapping.getVirtual().replace('/', '.'), "."))
            attrs.putValue("Bundle-Description", "this is a " + type + " mapping generated by " + Constants.NAME + ".")
            attrs.putValue("Bundle-Version", "1.0.0.$token")
            // attrs.putValue("Import-Package","lucee.*");
            attrs.putValue("Require-Bundle", "lucee.core")

            // Mapping
            attrs.putValue("mapping-id", id)
            attrs.putValue("mapping-type", type)
            attrs.putValue("mapping-virtual-path", mapping.getVirtual())
            attrs.putValue("mapping-hidden", Caster.toString(mapping.isHidden()))
            attrs.putValue("mapping-physical-first", Caster.toString(mapping.isPhysicalFirst()))
            attrs.putValue("mapping-readonly", Caster.toString(mapping.isReadonly()))
            attrs.putValue("mapping-top-level", Caster.toString(mapping.isTopLevel()))
            attrs.putValue("mapping-inspect", ConfigWebUtil.inspectTemplate(mapping.getInspectTemplateRaw(), ""))
            attrs.putValue("mapping-listener-type", ConfigWebUtil.toListenerType(mapping.getListenerType(), ""))
            attrs.putValue("mapping-listener-mode", ConfigWebUtil.toListenerMode(mapping.getListenerMode(), ""))
            mani.createFile(true)
            IOUtil.write(mani, ManifestUtil.toString(mf, 100, null, null), "UTF-8", false)

            // source files
            val sources: Array<Resource?>?
            if (!addCFMLFiles && !addNonCFMLFiles) sources = arrayOf<Resource?>(temp, classRoot) else sources = arrayOf<Resource?>(temp, mapping.getPhysical(), classRoot)
            CompressUtil.compressZip(ResourceUtil.listResources(sources, filter), file, filter)
            if (getBoolV("append", false)) {
                if (mappingType == MAPPING_CFC) {
                    admin.updateComponentMapping(mapping.getVirtual(), mapping.getStrPhysical(), strFile, if (mapping.isPhysicalFirst()) "physical" else "archive",
                            mapping.getInspectTemplateRaw())
                } else if (mappingType == MAPPING_CT) {
                    admin.updateCustomTag(mapping.getVirtual(), mapping.getStrPhysical(), strFile, if (mapping.isPhysicalFirst()) "physical" else "archive",
                            mapping.getInspectTemplateRaw())
                } else admin.updateMapping(mapping.getVirtual(), mapping.getStrPhysical(), strFile, if (mapping.isPhysicalFirst()) "physical" else "archive",
                        mapping.getInspectTemplateRaw(), mapping.isTopLevel(), mapping.getListenerMode(), mapping.getListenerType(), mapping.isReadonly())
                store()
            }
        } catch (e: IOException) {
            throw Caster.toPageException(e)
        } finally {
            ResourceUtil.removeEL(temp, true)
        }
        adminSync.broadcast(attributes, config)
    }

    @Throws(PageException::class)
    private fun doCompileMapping() {
        doCompileMapping(MAPPING_REGULAR, getString("admin", action, "virtual").toLowerCase(), getBoolV("stoponerror", true), getBool("ignoreScopes", null))
        adminSync.broadcast(attributes, config)
    }

    @Throws(PageException::class)
    private fun doCompileComponentMapping() {
        doCompileMapping(MAPPING_CFC, getString("admin", action, "virtual").toLowerCase(), getBoolV("stoponerror", true), getBool("ignoreScopes", null))
        adminSync.broadcast(attributes, config)
    }

    @Throws(PageException::class)
    private fun doCompileCTMapping() {
        doCompileMapping(MAPPING_CT, getString("admin", action, "virtual").toLowerCase(), getBoolV("stoponerror", true), getBool("ignoreScopes", null))
        adminSync.broadcast(attributes, config)
    }

    @Throws(PageException::class)
    private fun doCompileMapping(mappingType: Short, virtual: String?, stoponerror: Boolean, ignoreScopes: Boolean?): Mapping? {
        var virtual = virtual
        if (StringUtil.isEmpty(virtual)) return null
        if (!StringUtil.startsWith(virtual, '/')) virtual = '/' + virtual
        if (!StringUtil.endsWith(virtual, '/')) virtual += '/'
        var mappings: Array<Mapping?>? = null
        mappings = if (mappingType == MAPPING_CFC) config.getComponentMappings() else if (mappingType == MAPPING_CT) config.getCustomTagMappings() else config.getMappings()
        for (i in mappings.indices) {
            val mapping: Mapping? = mappings.get(i)
            if (mapping.getVirtualLowerCaseWithSlash().equals(virtual)) {
                val errors: Map<String?, String?>? = if (stoponerror) null else MapFactory.< String, String>getConcurrentMap<String?, String?>()
                doCompileFile(mapping, mapping.getPhysical(), "", errors, ignoreScopes)
                if (errors != null && errors.size() > 0) {
                    val sb = StringBuilder()
                    val it: Iterator<String?> = errors.keySet().iterator()
                    var key: Object?
                    while (it.hasNext()) {
                        key = it.next()
                        if (sb.length() > 0) sb.append("\n\n")
                        sb.append(errors[key])
                    }
                    throw ApplicationException(sb.toString())
                }
                return mapping
            }
        }
        return null
    }

    @Throws(PageException::class)
    private fun doCompileFile(mapping: Mapping?, file: Resource?, path: String?, errors: Map<String?, String?>?, explicitIgnoreScope: Boolean?) {
        if (ResourceUtil.exists(file)) {
            if (file.isDirectory()) {
                val files: Array<Resource?> = file.listResources(FILTER_CFML_TEMPLATES)
                if (files != null) for (i in files.indices) {
                    val p = path + '/' + files[i].getName()
                    // print.ln(files[i]+" - "+p);
                    doCompileFile(mapping, files[i], p, errors, explicitIgnoreScope)
                }
            } else if (file.isFile()) {
                val ps: PageSource = mapping.getPageSource(path)
                val pci: PageContextImpl? = pageContext as PageContextImpl?
                val envIgnoreScopes: Boolean = pci.ignoreScopes()
                try {
                    if (explicitIgnoreScope != null) pci.setIgnoreScopes(explicitIgnoreScope)
                    (ps as PageSourceImpl).clear()
                    (ps as PageSourceImpl).loadPage(pageContext, explicitIgnoreScope != null)
                    // pageContext.compile(ps);
                } catch (pe: PageException) {
                    LogUtil.log(pageContext, Admin::class.java.getName(), pe)
                    val template: String = ps.getDisplayPath()
                    val msg = StringBuilder(pe.getMessage())
                    msg.append(", Error Occurred in File [")
                    msg.append(template)
                    if (pe is PageExceptionImpl) {
                        try {
                            val pei: PageExceptionImpl = pe as PageExceptionImpl
                            val context: Array = pei.getTagContext(config)
                            if (context.size() > 0) {
                                msg.append(":")
                                msg.append(Caster.toString((context.getE(1) as Struct).get("line")))
                            }
                        } catch (t: Throwable) {
                            ExceptionUtil.rethrowIfNecessary(t)
                        }
                    }
                    msg.append("]")
                    if (errors != null) errors.put(template, msg.toString()) else throw ApplicationException(msg.toString())
                } finally {
                    pci.setIgnoreScopes(envIgnoreScopes)
                }
            }
        }
    }

    /**
     * @throws PageException
     */
    @Throws(PageException::class)
    private fun doResetPassword() {
        try {
            admin.removePassword(getString("contextPath", null))
        } catch (e: Exception) {
            LogUtil.log(pageContext, Admin::class.java.getName(), e)
        }
        store()
    }

    @Throws(PageException::class)
    private fun doUpdateAPIKey() {
        admin.updateAPIKey(getString("key", null))
        store()
    }

    @Throws(PageException::class)
    private fun doRemoveAPIKey() {
        try {
            admin.removeAPIKey()
        } catch (e: Exception) {
        }
        store()
    }

    @Throws(PageException::class)
    private fun doGetAPIKey() {
        pageContext.setVariable(getString("admin", action, "returnVariable"), config.getIdentification().getApiKey())
    }

    @Throws(PageException::class)
    private fun doUpdateAuthKey() {
        try {
            admin.updateAuthKey(getString("key", null))
        } catch (e: Exception) {
        }
        store()
    }

    @Throws(PageException::class)
    private fun doRemoveAuthKey() {
        try {
            admin.removeAuthKeys(getString("key", null))
        } catch (e: Exception) {
        }
        store()
    }

    @Throws(PageException::class)
    private fun doListAuthKey() {
        val cs: ConfigServerImpl = if (config is ConfigServer) config as ConfigServerImpl? else ConfigWebUtil.getConfigServer(config, password) as ConfigServerImpl
        pageContext.setVariable(getString("admin", action, "returnVariable"), Caster.toArray(cs.getAuthenticationKeys()))
    }

    /**
     * @throws PageException
     */
    @Throws(PageException::class)
    private fun doGetContexts() {
        val factories: Array<CFMLFactory?>?
        if (config is ConfigServerImpl) {
            val cs: ConfigServerImpl? = config as ConfigServerImpl?
            factories = cs.getJSPFactories()
        } else {
            val cw: ConfigWebPro? = config as ConfigWebPro?
            factories = arrayOf<CFMLFactory?>(cw.getFactory())
        }
        val qry: lucee.runtime.type.Query = QueryImpl(arrayOf<Collection.Key?>(KeyConstants._path, KeyConstants._id, KeyConstants._hash, KeyConstants._label, HAS_OWN_SEC_CONTEXT,
                KeyConstants._url, CONFIG_FILE, CLIENT_SIZE, CLIENT_ELEMENTS, SESSION_SIZE, SESSION_ELEMENTS), factories!!.size, getString("admin", action, "returnVariable"))
        pageContext.setVariable(getString("admin", action, "returnVariable"), qry)
        var cw: ConfigWebPro
        for (i in factories.indices) {
            val row: Int = i + 1
            val factory: CFMLFactoryImpl? = factories!![i] as CFMLFactoryImpl?
            cw = factory.getConfig() as ConfigWebPro
            qry.setAtEL(KeyConstants._path, row, ReqRspUtil.getRootPath(factory.getConfig().getServletContext()))
            qry.setAtEL(CONFIG_FILE, row, factory.getConfig().getConfigFile().getAbsolutePath())
            if (factory.getURL() != null) qry.setAtEL(KeyConstants._url, row, factory.getURL().toExternalForm())
            qry.setAtEL(KeyConstants._id, row, factory.getConfig().getIdentification().getId())
            qry.setAtEL(KeyConstants._hash, row, SystemUtil.hash(factory.getConfig().getServletContext()))
            qry.setAtEL(KeyConstants._label, row, factory.getLabel())
            qry.setAtEL(HAS_OWN_SEC_CONTEXT, row, Caster.toBoolean(cw.hasIndividualSecurityManager()))
            setScopeDirInfo(qry, row, CLIENT_SIZE, CLIENT_ELEMENTS, cw.getClientScopeDir())
            setScopeDirInfo(qry, row, SESSION_SIZE, SESSION_ELEMENTS, cw.getSessionScopeDir())
        }
    }

    private fun setScopeDirInfo(qry: Query?, row: Int, sizeName: Key?, elName: Key?, dir: Resource?) {
        qry.setAtEL(sizeName, row, Caster.toDouble(ResourceUtil.getRealSize(dir)))
        qry.setAtEL(elName, row, Caster.toDouble(ResourceUtil.getChildCount(dir)))
    }

    @Throws(PageException::class)
    private fun doHasIndividualSecurity() {
        pageContext.setVariable(getString("admin", action, "returnVariable"),
                Caster.toBoolean(pageContext.getConfig().getConfigServer(password).hasIndividualSecurityManager(getString("admin", action, "id")
                )))
    }

    @Throws(PageException::class)
    private fun doUpdateUpdate() {
        admin.updateUpdate(getString("admin", action, "updatetype"), getString("admin", action, "updatelocation"))
        store()
        adminSync.broadcast(attributes, config)
    }

    /**
     * @throws PageException
     */
    @Throws(PageException::class)
    private fun doUpdateSerial() {
        admin.updateSerial(getString("admin", action, "serial"))
        store()
        pageContext.serverScope().reload()
    }

    /**
     * @throws PageException
     */
    @Throws(PageException::class)
    private fun doGetSerial() {
        pageContext.setVariable(getString("admin", action, "returnVariable"), config.getSerialNumber())
    }

    @get:Throws(PageException::class)
    private val contextDirectory: Resource?
        private get() {
            val cs: ConfigServerImpl = ConfigWebUtil.getConfigServer(config, password) as ConfigServerImpl
            val dist: Resource = cs.getConfigDir().getRealResource("distribution")
            dist.mkdirs()
            return dist
        }

    @Throws(PageException::class)
    private fun doGetPluginDirectory() {
        pageContext.setVariable(getString("admin", action, "returnVariable"), config.getPluginDirectory().getAbsolutePath())
    }

    @Throws(PageException::class, IOException::class)
    private fun doUpdatePlugin() {
        val strSrc = getString("admin", action, "source")
        val src: Resource = ResourceUtil.toResourceExisting(pageContext, strSrc)
        admin.updatePlugin(pageContext, src)
        store()
    }

    @Throws(PageException::class)
    private fun doUpdateLabel() {
        if (config is ConfigServer) {
            if (admin.updateLabel(getString("admin", action, "hash"), getString("admin", action, "label"))) {
                store()
                adminSync.broadcast(attributes, config)
            }
        }
    }

    @Throws(PageException::class, IOException::class)
    private fun doUpdateContext() {
        val strSrc = getString("admin", action, "source")
        val strRealpath = getString("admin", action, "destination")
        val src: Resource = ResourceUtil.toResourceExisting(pageContext, strSrc)
        val server: ConfigServerImpl = ConfigWebUtil.getConfigServer(config, password) as ConfigServerImpl
        val trg: Resource
        val p: Resource
        val deploy: Resource = server.getConfigDir().getRealResource("web-context-deployment")
        deploy.mkdirs()

        // deploy it
        trg = deploy.getRealResource(strRealpath)
        if (trg.exists()) trg.remove(true)
        p = trg.getParentResource()
        if (!p.isDirectory()) p.createDirectory(true)
        src.copyTo(trg, false)
        store()
        val webs: Array<ConfigWeb?> = server.getConfigWebs()
        for (i in webs.indices) {
            ConfigWebUtil.deployWebContext(server, webs[i], true)
        }
    }

    @Throws(PageException::class)
    private fun doRemoveContext() {
        val strRealpath = getString("admin", action, "destination")
        val server: ConfigServerImpl = if (config is ConfigServer) config as ConfigServerImpl? else ConfigWebUtil.getConfigServer(config, password) as ConfigServerImpl
        try {
            admin.removeContext(server, true, ThreadLocalPageContext.getLog(pageContext, "deploy"), strRealpath)
        } catch (t: Throwable) {
            ExceptionUtil.rethrowIfNecessary(t)
            throw Caster.toPageException(t)
        }
        store()
    }

    @Throws(PageException::class, IOException::class)
    private fun doRemovePlugin() {
        val dir: Resource = config.getPluginDirectory()
        val name = getString("admin", action, "name")
        val trgDir: Resource = dir.getRealResource(name)
        trgDir.remove(true)
        store()
    }

    @Throws(PageException::class)
    private fun doGetPlugins() {
        val dir: Resource = config.getPluginDirectory()
        val list: Array<String?> = dir.list(PluginFilter())
        val qry: lucee.runtime.type.Query = QueryImpl(arrayOf<Collection.Key?>(KeyConstants._name), list.size, getString("admin", action, "returnVariable"))
        pageContext.setVariable(getString("admin", action, "returnVariable"), qry)
        for (i in list.indices) {
            val row: Int = i + 1
            qry.setAtEL(KeyConstants._name, row, list[i])
        }
    }

    @Throws(PageException::class)
    private fun doStorageSet() {
        try {
            admin.storageSet(config, getString("admin", action, "key"), getObject("admin", action, "value"))
        } catch (e: Exception) {
            throw Caster.toPageException(e)
        }
    }

    @Throws(PageException::class)
    private fun doStorageGet() {
        try {
            pageContext.setVariable(getString("admin", action, "returnVariable"), admin.storageGet(config, getString("admin", action, "key")))
        } catch (e: Exception) {
            throw Caster.toPageException(e)
        }
    }

    /**
     * @throws PageException
     */
    @Throws(PageException::class)
    private fun doGetDefaultPassword() {
        val password: Password = admin.getDefaultPassword()
        pageContext.setVariable(getString("admin", action, "returnVariable"), if (password == null) "" else password.getPassword())
    }

    /**
     * @throws PageException
     */
    @Throws(PageException::class)
    private fun doUpdateDefaultPassword() {
        try {
            admin.updateDefaultPassword(getString("admin", action, "newPassword"))
        } catch (e: Exception) {
            throw Caster.toPageException(e)
        }
        store()
    }

    @Throws(PageException::class)
    private fun doRemoveDefaultPassword() {
        admin.removeDefaultPassword()
        store()
    }

    /*
	 * *
	 * 
	 * @throws PageException
	 * 
	 * / private void doUpdatePassword() throws PageException { try {
	 * ConfigWebAdmin.setPassword(config,password==null?null:Caster.toString(password),getString("admin"
	 * ,action,"newPassword")); } catch (Exception e) { throw Caster.toPageException(e); } //store(); }
	 */
    @Throws(PageException::class)
    private fun doGetSecurity() {
        val sct: Struct = StructImpl()
        pageContext.setVariable(getString("admin", action, "returnVariable"), sct)
        sct.set("varUsage", AppListenerUtil.toVariableUsage(config.getQueryVarUsage(), "ignore"))
    }

    /**
     * @throws PageException
     */
    @Throws(PageException::class)
    private fun doGetDebug() {
        val sct: Struct = StructImpl()
        pageContext.setVariable(getString("admin", action, "returnVariable"), sct)
        sct.set(DEBUG, Caster.toBoolean(config.debug()))
        sct.set(KeyConstants._database, Caster.toBoolean(config.hasDebugOptions(ConfigPro.DEBUG_DATABASE)))
        sct.set(KeyConstants._exception, Caster.toBoolean(config.hasDebugOptions(ConfigPro.DEBUG_EXCEPTION)))
        sct.set(KeyConstants._template, Caster.toBoolean(config.hasDebugOptions(ConfigPro.DEBUG_TEMPLATE)))
        sct.set("tracing", Caster.toBoolean(config.hasDebugOptions(ConfigPro.DEBUG_TRACING)))
        sct.set(KeyConstants._dump, Caster.toBoolean(config.hasDebugOptions(ConfigPro.DEBUG_DUMP)))
        sct.set("timer", Caster.toBoolean(config.hasDebugOptions(ConfigPro.DEBUG_TIMER)))
        sct.set("implicitAccess", Caster.toBoolean(config.hasDebugOptions(ConfigPro.DEBUG_IMPLICIT_ACCESS)))
        sct.set("queryUsage", Caster.toBoolean(config.hasDebugOptions(ConfigPro.DEBUG_QUERY_USAGE)))
        sct.set("thread", Caster.toBoolean(config.hasDebugOptions(ConfigPro.DEBUG_THREAD)))
    }

    @Throws(PageException::class)
    private fun doGetError() {
        val sct: Struct = StructImpl()
        pageContext.setVariable(getString("admin", action, "returnVariable"), sct)
        // sct.set("errorTemplate",config.getErrorTemplate());
        val templates: Struct = StructImpl()
        val str: Struct = StructImpl()
        sct.set(TEMPLATES, templates)
        sct.set(STR, str)
        sct.set(DO_STATUS_CODE, Caster.toBoolean(config.getErrorStatusCode()))

        // 500
        var template: String = config.getErrorTemplate(500)
        try {
            val ps: PageSource = (pageContext as PageContextImpl?).getPageSourceExisting(template)
            if (ps != null) templates.set("500", ps.getDisplayPath()) else templates.set("500", "")
        } catch (e: PageException) {
            templates.set("500", "")
        }
        str.set("500", template)

        // 404
        template = config.getErrorTemplate(404)
        try {
            val ps: PageSource = (pageContext as PageContextImpl?).getPageSourceExisting(template)
            if (ps != null) templates.set("404", ps.getDisplayPath()) else templates.set("404", "")
        } catch (e: PageException) {
            templates.set("404", "")
        }
        str.set("404", template)
    }

    @Throws(PageException::class)
    private fun doGetRegex() {
        val sct: Struct = StructImpl()
        pageContext.setVariable(getString("admin", action, "returnVariable"), sct)
        sct.set(KeyConstants._type, config.getRegex().getTypeName())
    }

    /**
     * @throws PageException
     */
    @Throws(PageException::class)
    private fun doGetDebugData() {
        pageContext.setVariable(getString("admin", action, "returnVariable"), if (pageContext.getConfig().debug()) pageContext.getDebugger().getDebuggingData(pageContext) else null)
    }

    @Throws(PageException::class)
    private fun doGetLoggedDebugData() {

        // to get logged debugging data config should be a ConfigWebPro,
        // for singleMode config is always ConfigServer so config must be redefine if it was singleMode
        if (singleMode) {
            configWeb = pageContext.getConfig() as ConfigWebPro
            config = configWeb
        }
        val cw: ConfigWebPro? = configWeb
        val id = getString("id", null)
        val data: Array = cw.getDebuggerPool().getData(pageContext)
        if (StringUtil.isEmpty(id)) {
            pageContext.setVariable(getString("admin", action, "returnVariable"), data)
        } else {
            val it: Iterator<Object?> = data.valueIterator()
            var sct: Struct?
            while (it.hasNext()) {
                sct = it.next() as Struct?
                if (OpUtil.equalsEL(ThreadLocalPageContext.get(), id, sct.get(KeyConstants._id, ""), false, true)) {
                    pageContext.setVariable(getString("admin", action, "returnVariable"), sct)
                    return
                }
            }
            throw ApplicationException("No debugging data with id [$id] found.")
        }
    }

    @Throws(PageException::class)
    private fun doPurgeDebugPool() {
        configWeb.getDebuggerPool().purge()
    }

    @Throws(PageException::class)
    private fun doPurgeExpiredSessions() {
        val cs: ConfigServer? = config as ConfigServer?
        val webs: Array<ConfigWeb?> = cs.getConfigWebs()
        for (i in webs.indices) {
            val cw: ConfigWeb? = webs[i]
            try {
                (cw.getFactory() as CFMLFactoryImpl).getScopeContext().clearUnused()
            } catch (t: Throwable) {
                ExceptionUtil.rethrowIfNecessary(t)
            }
        }
    }

    @Throws(PageException::class)
    private fun doGetInfo() {
        val sct: Struct = StructImpl()
        pageContext.setVariable(getString("admin", action, "returnVariable"), sct)
        if (config is ConfigWebPro || configWeb is SingleContextConfigWeb) {
            val cw: ConfigWebPro = if (configWeb is SingleContextConfigWeb) configWeb else config as ConfigWebPro?
            sct.setEL(KeyConstants._id, config.getIdentification().getId())
            sct.setEL(KeyConstants._label, cw.getLabel())
            sct.setEL(KeyConstants._hash, cw.getHash())
            sct.setEL(KeyConstants._root, cw.getRootDirectory().getAbsolutePath())
            sct.setEL("configServerDir", cw.getConfigServerDir().getAbsolutePath())
            sct.setEL("configWebDir", cw.getConfigDir().getAbsolutePath())
        } else {
            sct.setEL("configServerDir", config.getConfigDir().getAbsolutePath())
            sct.setEL("configWebDir", configWeb.getConfigDir().getAbsolutePath())
        }
        sct.setEL(KeyConstants._config, config.getConfigFile().getAbsolutePath())

        // Servlets
        if (config is ConfigServer) {
            val cs: ConfigServer? = config as ConfigServer?
            val engine: CFMLEngineImpl = cs.getCFMLEngine() as CFMLEngineImpl
            val srv: Struct = StructImpl()
            var params: Struct?
            val configs: Array<ServletConfig?> = engine.getServletConfigs()
            var sc: ServletConfig?
            var e: Enumeration
            var name: String
            var value: String
            for (i in configs.indices) {
                sc = configs[i]
                e = sc.getInitParameterNames()
                params = StructImpl()
                while (e.hasMoreElements()) {
                    name = e.nextElement()
                    value = sc.getInitParameter(name)
                    params.set(name, value)
                }
                srv.set(sc.getServletName(), params)
            }
            sct.set(KeyConstants._servlets, srv)
        }

        // sct.setEL("javaAgentSupported", Caster.toBoolean(InstrumentationUtil.isSupported()));
        sct.setEL("javaAgentSupported", Boolean.TRUE)
        // sct.setEL("javaAgentPath", ClassUtil.getSourcePathForClass("lucee.runtime.instrumentation.Agent",
        // ""));
    }

    /**
     * @throws PageException
     */
    @Throws(PageException::class)
    private fun doCreateSecurityManager() {
        admin.createSecurityManager(password, getString("admin", action, "id"))
        store()
    }

    @Throws(PageException::class)
    private fun doRemoveSecurityManager() {
        admin.removeSecurityManager(password, getString("admin", action, "id"))
        store()
    }

    @Throws(PageException::class)
    private fun fb(key: String?): Short {
        return if (getBool("admin", action, key)) SecurityManager.VALUE_YES else SecurityManager.VALUE_NO
    }

    @Throws(PageException::class)
    private fun fb2(key: String?): Short {
        return SecurityManagerImpl.toShortAccessRWValue(getString("admin", action, key))
    }

    @Throws(PageException::class)
    private fun doUpdateDefaultSecurityManager() {
        admin.updateDefaultSecurity(fb("setting"), SecurityManagerImpl.toShortAccessValue(getString("admin", action, "file")), fileAcces, fb("direct_java_access"), fb("mail"),
                SecurityManagerImpl.toShortAccessValue(getString("admin", action, "datasource")), fb("mapping"), fb("remote"), fb("custom_tag"), fb("cfx_setting"), fb("cfx_usage"),
                fb("debugging"), fb("search"), fb("scheduled_task"), fb("tag_execute"), fb("tag_import"), fb("tag_object"), fb("tag_registry"), fb("cache"), fb("gateway"),
                fb("orm"), fb2("access_read"), fb2("access_write"))
        store()
        adminSync.broadcast(attributes, config)
    }

    @get:Throws(PageException::class)
    private val fileAcces: Array<Any?>?
        private get() {
            val value: Object = attributes.get(FILE_ACCESS, null) ?: return null
            val arr: Array = Caster.toArray(value)
            val rtn: List<Resource?> = ArrayList<Resource?>()
            val it: Iterator = arr.valueIterator()
            var path: String
            var res: Resource
            while (it.hasNext()) {
                path = Caster.toString(it.next())
                if (StringUtil.isEmpty(path)) continue
                res = config.getResource(path)
                if (!res.exists()) throw ApplicationException("Path [$path] does not exist")
                if (!res.isDirectory()) throw ApplicationException("Path [$path] is not a directory")
                rtn.add(res)
            }
            return rtn.toArray(arrayOfNulls<Resource?>(rtn.size()))
        }

    @Throws(PageException::class)
    private fun doUpdateSecurityManager() {
        admin.updateSecurity(getString("admin", action, "id"), fb("setting"), SecurityManagerImpl.toShortAccessValue(getString("admin", action, "file")), fileAcces,
                fb("direct_java_access"), fb("mail"), SecurityManagerImpl.toShortAccessValue(getString("admin", action, "datasource")), fb("mapping"), fb("remote"),
                fb("custom_tag"), fb("cfx_setting"), fb("cfx_usage"), fb("debugging"), fb("search"), fb("scheduled_task"), fb("tag_execute"), fb("tag_import"), fb("tag_object"),
                fb("tag_registry"), fb("cache"), fb("gateway"), fb("orm"), fb2("access_read"), fb2("access_write"))
        store()
    }

    /**
     * @throws PageException
     */
    @Throws(PageException::class)
    private fun doGetDefaultSecurityManager() {
        val cs: ConfigServer = ConfigWebUtil.getConfigServer(config, password)
        val dsm: SecurityManager = cs.getDefaultSecurityManager()
        _fillSecData(dsm)
    }

    @Throws(PageException::class)
    private fun doGetSecurityManager() {
        val cs: ConfigServer = ConfigWebUtil.getConfigServer(config, password)
        val sm: SecurityManager = cs.getSecurityManager(getString("admin", action, "id"))
        _fillSecData(sm)
    }

    @Throws(PageException::class)
    private fun _fillSecData(sm: SecurityManager?) {
        val sct: Struct = StructImpl()
        pageContext.setVariable(getString("admin", action, "returnVariable"), sct)
        sct.set("cfx_setting", Caster.toBoolean(sm.getAccess(SecurityManager.TYPE_CFX_SETTING) === SecurityManager.VALUE_YES))
        sct.set("cfx_usage", Caster.toBoolean(sm.getAccess(SecurityManager.TYPE_CFX_USAGE) === SecurityManager.VALUE_YES))
        sct.set("custom_tag", Caster.toBoolean(sm.getAccess(SecurityManager.TYPE_CUSTOM_TAG) === SecurityManager.VALUE_YES))
        sct.set(KeyConstants._datasource, _fillSecDataDS(sm.getAccess(SecurityManager.TYPE_DATASOURCE)))
        sct.set("debugging", Caster.toBoolean(sm.getAccess(SecurityManager.TYPE_DEBUGGING) === SecurityManager.VALUE_YES))
        sct.set("direct_java_access", Caster.toBoolean(sm.getAccess(SecurityManager.TYPE_DIRECT_JAVA_ACCESS) === SecurityManager.VALUE_YES))
        sct.set("mail", Caster.toBoolean(sm.getAccess(SecurityManager.TYPE_MAIL) === SecurityManager.VALUE_YES))
        sct.set(KeyConstants._mapping, Caster.toBoolean(sm.getAccess(SecurityManager.TYPE_MAPPING) === SecurityManager.VALUE_YES))
        sct.set("remote", Caster.toBoolean(sm.getAccess(SecurityManagerImpl.TYPE_REMOTE) === SecurityManager.VALUE_YES))
        sct.set("setting", Caster.toBoolean(sm.getAccess(SecurityManager.TYPE_SETTING) === SecurityManager.VALUE_YES))
        sct.set("search", Caster.toBoolean(sm.getAccess(SecurityManager.TYPE_SEARCH) === SecurityManager.VALUE_YES))
        sct.set("scheduled_task", Caster.toBoolean(sm.getAccess(SecurityManager.TYPE_SCHEDULED_TASK) === SecurityManager.VALUE_YES))
        sct.set(KeyConstants._cache, Caster.toBoolean(sm.getAccess(SecurityManagerImpl.TYPE_CACHE) === SecurityManager.VALUE_YES))
        sct.set("gateway", Caster.toBoolean(sm.getAccess(SecurityManagerImpl.TYPE_GATEWAY) === SecurityManager.VALUE_YES))
        sct.set(KeyConstants._orm, Caster.toBoolean(sm.getAccess(SecurityManagerImpl.TYPE_ORM) === SecurityManager.VALUE_YES))
        sct.set("tag_execute", Caster.toBoolean(sm.getAccess(SecurityManager.TYPE_TAG_EXECUTE) === SecurityManager.VALUE_YES))
        sct.set("tag_import", Caster.toBoolean(sm.getAccess(SecurityManager.TYPE_TAG_IMPORT) === SecurityManager.VALUE_YES))
        sct.set("tag_object", Caster.toBoolean(sm.getAccess(SecurityManager.TYPE_TAG_OBJECT) === SecurityManager.VALUE_YES))
        sct.set("tag_registry", Caster.toBoolean(sm.getAccess(SecurityManager.TYPE_TAG_REGISTRY) === SecurityManager.VALUE_YES))
        sct.set("access_read", SecurityManagerImpl.toStringAccessRWValue(sm.getAccess(SecurityManager.TYPE_ACCESS_READ)))
        sct.set("access_write", SecurityManagerImpl.toStringAccessRWValue(sm.getAccess(SecurityManager.TYPE_ACCESS_WRITE)))
        val accessFile: Short = sm.getAccess(SecurityManager.TYPE_FILE)
        var str: String = SecurityManagerImpl.toStringAccessValue(accessFile)
        if (str.equals("yes")) str = "all"
        sct.set(KeyConstants._file, str)
        val arr: Array = ArrayImpl()
        if (accessFile != SecurityManager.VALUE_ALL) {
            val reses: Array<Resource?> = (sm as SecurityManagerImpl?).getCustomFileAccess()
            for (i in reses.indices) {
                arr.appendEL(reses[i].getAbsolutePath())
            }
        }
        sct.set("file_access", arr)
    }

    private fun _fillSecDataDS(access: Short): Double? {
        when (access) {
            SecurityManager.VALUE_YES -> return Caster.toDouble(-1)
            SecurityManager.VALUE_NO -> return Caster.toDouble(0)
            SecurityManager.VALUE_1 -> return Caster.toDouble(1)
            SecurityManager.VALUE_2 -> return Caster.toDouble(2)
            SecurityManager.VALUE_3 -> return Caster.toDouble(3)
            SecurityManager.VALUE_4 -> return Caster.toDouble(4)
            SecurityManager.VALUE_5 -> return Caster.toDouble(5)
            SecurityManager.VALUE_6 -> return Caster.toDouble(6)
            SecurityManager.VALUE_7 -> return Caster.toDouble(7)
            SecurityManager.VALUE_8 -> return Caster.toDouble(8)
            SecurityManager.VALUE_9 -> return Caster.toDouble(9)
            SecurityManager.VALUE_10 -> return Caster.toDouble(10)
        }
        return Caster.toDouble(-1)
    }

    @Throws(PageException::class)
    private fun doUpdateSecurity() {
        admin.updateSecurity(getString("varUsage", ""))
        store()
        adminSync.broadcast(attributes, config)
    }

    /**
     * @throws PageException
     */
    @Throws(PageException::class)
    private fun doUpdateDebug() {
        admin.updateDebug(Caster.toBoolean(getString("debug", ""), null), Caster.toBoolean(getString("template", ""), null), Caster.toBoolean(getString("database", ""), null),
                Caster.toBoolean(getString("exception", ""), null), Caster.toBoolean(getString("tracing", ""), null), Caster.toBoolean(getString("dump", ""), null),
                Caster.toBoolean(getString("timer", ""), null), Caster.toBoolean(getString("implicitAccess", ""), null), Caster.toBoolean(getString("queryUsage", ""), null),
                Caster.toBoolean(getString("thread", ""), null))

        // TODO?admin.updateDebugTemplate(getString("admin", action, "debugTemplate"));
        store()
        adminSync.broadcast(attributes, config)
        if (!Caster.toBooleanValue(getString("debug", ""), false)) doPurgeDebugPool() // purge the debug log pool when disabling debug to free up memory
    }

    @Throws(PageException::class)
    private fun doGetDebugSetting() {
        val sct: Struct = StructImpl()
        sct.set("maxLogs", Caster.toDouble(config.getDebugMaxRecordsLogged()))
        pageContext.setVariable(getString("admin", action, "returnVariable"), sct)
    }

    @Throws(PageException::class)
    private fun doUpdateDebugSetting() {
        val str = getString("admin", action, "maxLogs")
        val maxLogs: Int
        maxLogs = if (StringUtil.isEmpty(str, true)) -1 else Caster.toIntValue(str)
        admin.updateDebugSetting(maxLogs)
        store()
        adminSync.broadcast(attributes, config)
    }

    @Throws(PageException::class)
    private fun doUpdateDebugEntry() {
        try {
            admin.updateDebugEntry(getString("admin", "updateDebugEntry", "debugtype"), getString("admin", "updateDebugEntry", "iprange"),
                    getString("admin", "updateDebugEntry", "label"), getString("admin", "updateDebugEntry", "path"), getString("admin", "updateDebugEntry", "fullname"),
                    getStruct("admin", "updateDebugEntry", "custom"))
        } catch (e: IOException) {
            throw Caster.toPageException(e)
        }
        store()
        adminSync.broadcast(attributes, config)
    }

    @Throws(PageException::class)
    private fun doGetDebugEntry() {
        val entries: Array<DebugEntry?> = config.getDebugEntries()
        val rtn = getString("admin", action, "returnVariable")
        val qry: lucee.runtime.type.Query = QueryImpl(arrayOf<Collection.Key?>(KeyConstants._id, LABEL, IP_RANGE, READONLY, KeyConstants._type, CUSTOM), entries.size, rtn)
        pageContext.setVariable(rtn, qry)
        var de: DebugEntry?
        for (i in entries.indices) {
            val row: Int = i + 1
            de = entries[i]
            qry.setAtEL(KeyConstants._id, row, de.getId())
            qry.setAtEL(LABEL, row, de.getLabel())
            qry.setAtEL(IP_RANGE, row, de.getIpRangeAsString())
            qry.setAtEL(KeyConstants._type, row, de.getType())
            qry.setAtEL(READONLY, row, Caster.toBoolean(de.isReadOnly()))
            qry.setAtEL(CUSTOM, row, de.getCustom())
        }
    }

    @Throws(PageException::class)
    private fun doUpdateError() {
        admin.updateErrorTemplate(500, getString("admin", action, "template500"))
        admin.updateErrorTemplate(404, getString("admin", action, "template404"))
        admin.updateErrorStatusCode(getBoolObject("admin", action, "statuscode"))
        store()
        adminSync.broadcast(attributes, config)
    }

    @Throws(PageException::class)
    private fun doUpdateRegex() {
        admin.updateRegexType(getString("admin", action, "regextype"))
        store()
        adminSync.broadcast(attributes, config)
    }

    /**
     * @throws PageException
     */
    @Throws(PageException::class)
    private fun doUpdateJavaCFX() {
        var name = getString("admin", action, "name")
        if (StringUtil.startsWithIgnoreCase(name, "cfx_")) name = name.substring(4)
        val cd: lucee.runtime.db.ClassDefinition = ClassDefinitionImpl(getString("admin", action, "class"), getString("bundleName", null), getString("bundleVersion", null),
                config.getIdentification())
        admin.updateJavaCFX(name, cd)
        store()
        adminSync.broadcast(attributes, config)
    }

    @Throws(PageException::class)
    private fun doVerifyJavaCFX() {
        val name = getString("admin", action, "name")
        val cd: ClassDefinition = ClassDefinitionImpl(getString("admin", action, "class"), getString("bundleName", null), getString("bundleVersion", null),
                config.getIdentification())
        admin.verifyJavaCFX(name, cd)
    }

    @Throws(PageException::class)
    private fun doVerifyCFX() {
        var name = getString("admin", action, "name")
        if (StringUtil.startsWithIgnoreCase(name, "cfx_")) name = name.substring(4)
        admin.verifyCFX(name)
    }

    /**
     * @throws PageException
     */
    @Throws(PageException::class)
    private fun doRemoveCFX() {
        admin.removeCFX(getString("admin", action, "name"))
        store()
        adminSync.broadcast(attributes, config)
    }

    @Throws(PageException::class)
    private fun doRemoveExtension() {
        admin.removeExtension(getString("admin", action, "provider"), getString("admin", action, "id"))
        store()
        // adminSync.broadcast(attributes, config);
    }

    /**
     * @throws PageException
     */
    @Throws(PageException::class)
    private fun doGetJavaCFXTags() {
        val map: Map = config.getCFXTagPool().getClasses()
        val qry: lucee.runtime.type.Query = QueryImpl(arrayOf<Collection.Key?>(KeyConstants._displayname, KeyConstants._sourcename, KeyConstants._readonly, KeyConstants._name,
                KeyConstants._class, KeyConstants._bundleName, KeyConstants._bundleVersion, KeyConstants._isvalid), 0, "query")
        val it: Iterator = map.keySet().iterator()
        var row = 0
        while (it.hasNext()) {
            val tag: CFXTagClass = map.get(it.next()) as CFXTagClass
            if (tag is JavaCFXTagClass) {
                row++
                qry.addRow(1)
                val jtag: JavaCFXTagClass = tag as JavaCFXTagClass
                qry.setAt(KeyConstants._displayname, row, tag.getDisplayType())
                qry.setAt(KeyConstants._sourcename, row, tag.getSourceName())
                qry.setAt(KeyConstants._readonly, row, Caster.toBoolean(tag.isReadOnly()))
                qry.setAt(KeyConstants._isvalid, row, Caster.toBoolean(tag.isValid()))
                qry.setAt(KeyConstants._name, row, jtag.getName())
                qry.setAt(KeyConstants._class, row, jtag.getClassDefinition().getClassName())
                qry.setAt(KeyConstants._bundleName, row, jtag.getClassDefinition().getName())
                qry.setAt(KeyConstants._bundleVersion, row, jtag.getClassDefinition().getVersionAsString())
            }
        }
        pageContext.setVariable(getString("admin", action, "returnVariable"), qry)
    }

    /**
     * @throws PageException
     */
    @Throws(PageException::class)
    private fun doGetCFXTags() {
        val map: Map = config.getCFXTagPool().getClasses()
        val qry: lucee.runtime.type.Query = QueryImpl(arrayOf<String?>("displayname", "sourcename", "readonly", "isvalid", "name", "procedure_class", "procedure_bundleName", "procedure_bundleVersion", "keep_alive"),
                map.size(), "query")
        val it: Iterator = map.keySet().iterator()
        var row = 0
        while (it.hasNext()) {
            row++
            val tag: CFXTagClass = map.get(it.next()) as CFXTagClass
            qry.setAt("displayname", row, tag.getDisplayType())
            qry.setAt("sourcename", row, tag.getSourceName())
            qry.setAt("readonly", row, Caster.toBoolean(tag.isReadOnly()))
            qry.setAt("isvalid", row, Caster.toBoolean(tag.isValid()))
            if (tag is JavaCFXTagClass) {
                val jtag: JavaCFXTagClass = tag as JavaCFXTagClass
                qry.setAt(KeyConstants._name, row, jtag.getName())
                qry.setAt("procedure_class", row, jtag.getClassDefinition().getClassName())
                qry.setAt("procedure_bundleName", row, jtag.getClassDefinition().getName())
                qry.setAt("procedure_bundleVersion", row, jtag.getClassDefinition().getVersionAsString())
            }
        }
        pageContext.setVariable(getString("admin", action, "returnVariable"), qry)
    }

    /**
     * @throws PageException
     */
    @Throws(PageException::class)
    private fun doUpdateComponentMapping() {
        admin.updateComponentMapping(getString("virtual", ""), getString("physical", ""), getString("archive", ""), getString("primary", "physical"),
                ConfigWebUtil.inspectTemplate(getString("inspect", ""), ConfigPro.INSPECT_UNDEFINED))
        store()
        adminSync.broadcast(attributes, config)
    }

    /**
     * @throws PageException
     */
    @Throws(PageException::class)
    private fun doRemoveComponentMapping() {
        admin.removeComponentMapping(getString("admin", action, "virtual"))
        store()
        adminSync.broadcast(attributes, config)
    }

    /**
     * @throws PageException
     */
    @Throws(PageException::class)
    private fun doUpdateCustomTag() {
        admin.updateCustomTag(getString("admin", action, "virtual"), getString("admin", action, "physical"), getString("admin", action, "archive"),
                getString("admin", action, "primary"), ConfigWebUtil.inspectTemplate(getString("inspect", ""), ConfigPro.INSPECT_UNDEFINED))
        store()
        adminSync.broadcast(attributes, config)
    }

    /**
     * @throws PageException
     */
    @Throws(PageException::class)
    private fun doRemoveCustomTag() {
        admin.removeCustomTag(getString("admin", action, "virtual"))
        store()
        adminSync.broadcast(attributes, config)
    }

    /**
     * @throws PageException
     */
    @Throws(PageException::class)
    private fun doGetCustomTagMappings() {
        val mappings: Array<Mapping?> = config.getCustomTagMappings()
        val qry: lucee.runtime.type.Query = QueryImpl(arrayOf<String?>("archive", "strarchive", "physical", "strphysical", "virtual", "hidden", "physicalFirst", "readonly", "inspect"), mappings.size, "query")
        for (i in mappings.indices) {
            val m: MappingImpl? = mappings[i] as MappingImpl?
            val row: Int = i + 1
            qry.setAt("archive", row, m.getArchive())
            qry.setAt("strarchive", row, m.getStrArchive())
            qry.setAt("physical", row, m.getPhysical())
            qry.setAt("strphysical", row, m.getStrPhysical())
            qry.setAt("virtual", row, m.getVirtual())
            qry.setAt("hidden", row, Caster.toBoolean(m.isHidden()))
            qry.setAt("physicalFirst", row, Caster.toBoolean(m.isPhysicalFirst()))
            qry.setAt("readonly", row, Caster.toBoolean(m.isReadonly()))
            qry.setAt("inspect", row, ConfigWebUtil.inspectTemplate(m.getInspectTemplateRaw(), ""))
        }
        pageContext.setVariable(getString("admin", action, "returnVariable"), qry)
    }

    @Throws(PageException::class)
    private fun doGetComponentMappings() {
        val mappings: Array<Mapping?> = config.getComponentMappings()
        val qry: lucee.runtime.type.Query = QueryImpl(arrayOf<String?>("archive", "strarchive", "physical", "strphysical", "virtual", "hidden", "physicalFirst", "readonly", "inspect"), mappings.size, "query")
        for (i in mappings.indices) {
            val m: MappingImpl? = mappings[i] as MappingImpl?
            val row: Int = i + 1
            qry.setAt("archive", row, m.getArchive())
            qry.setAt("strarchive", row, m.getStrArchive())
            qry.setAt("physical", row, m.getPhysical())
            qry.setAt("strphysical", row, m.getStrPhysical())
            qry.setAt("virtual", row, m.getVirtual())
            qry.setAt("hidden", row, Caster.toBoolean(m.isHidden()))
            qry.setAt("physicalFirst", row, Caster.toBoolean(m.isPhysicalFirst()))
            qry.setAt("readonly", row, Caster.toBoolean(m.isReadonly()))
            qry.setAt("inspect", row, ConfigWebUtil.inspectTemplate(m.getInspectTemplateRaw(), ""))
        }
        pageContext.setVariable(getString("admin", action, "returnVariable"), qry)
    }

    /**
     * @throws PageException
     */
    @Throws(PageException::class)
    private fun doRemoveMapping() {
        admin.removeMapping(getString("admin", action, "virtual"))
        store()
        adminSync.broadcast(attributes, config)
    }

    /**
     * @throws PageException
     */
    @Throws(PageException::class)
    private fun doUpdateRestMapping() {
        admin.updateRestMapping(getString("admin", action, "virtual"), getString("admin", action, "physical"), getBool("admin", action, "default"))
        store()
        adminSync.broadcast(attributes, config)
        RestUtil.release(config.getRestMappings())
    }

    @Throws(PageException::class)
    private fun doRemoveRestMapping() {
        admin.removeRestMapping(getString("admin", action, "virtual"))
        store()
        adminSync.broadcast(attributes, config)
        RestUtil.release(config.getRestMappings())
    }

    @Throws(PageException::class)
    private fun doUpdateMapping() {
        admin.updateMapping(getString("admin", action, "virtual"), getString("admin", action, "physical"), getString("admin", action, "archive"),
                getString("admin", action, "primary"), ConfigWebUtil.inspectTemplate(getString("inspect", ""), ConfigPro.INSPECT_UNDEFINED),
                Caster.toBooleanValue(getString("toplevel", "true")), ConfigWebUtil.toListenerMode(getString("listenerMode", ""), -1),
                ConfigWebUtil.toListenerType(getString("listenerType", ""), -1), Caster.toBooleanValue(getString("readonly", "false"))
        )
        store()
        adminSync.broadcast(attributes, config)
    }

    /**
     * @throws PageException
     */
    @Throws(PageException::class)
    private fun doGetMapping() {
        val mappings: Array<Mapping?> = config.getMappings()
        val sct: Struct = StructImpl()
        val virtual = getString("admin", action, "virtual")
        for (i in mappings.indices) {
            val m: MappingImpl? = mappings[i] as MappingImpl?
            if (!m.getVirtual().equals(virtual)) continue
            sct.set("archive", m.getArchive())
            sct.set("strarchive", m.getStrArchive())
            sct.set("physical", m.getPhysical())
            sct.set("strphysical", m.getStrPhysical())
            sct.set("virtual", m.getVirtual())
            sct.set(KeyConstants._hidden, Caster.toBoolean(m.isHidden()))
            sct.set("physicalFirst", Caster.toBoolean(m.isPhysicalFirst()))
            sct.set("readonly", Caster.toBoolean(m.isReadonly()))
            sct.set("inspect", ConfigWebUtil.inspectTemplate(m.getInspectTemplateRaw(), ""))
            sct.set("toplevel", Caster.toBoolean(m.isTopLevel()))
            pageContext.setVariable(getString("admin", action, "returnVariable"), sct)
            return
        }
        throw ApplicationException("there is no mapping with virtual [$virtual]")
    }

    @Throws(PageException::class)
    private fun doGetRHExtensionProviders() {
        val providers: Array<RHExtensionProvider?> = config.getRHExtensionProviders()
        val qry: lucee.runtime.type.Query = QueryImpl(arrayOf<Key?>(KeyConstants._url, KeyConstants._readonly), providers.size, "query")
        var provider: RHExtensionProvider?
        for (i in providers.indices) {
            provider = providers[i]
            val row: Int = i + 1
            qry.setAt(KeyConstants._url, row, provider.getURL().toExternalForm())
            qry.setAt(KeyConstants._readonly, row, provider.isReadonly())
        }
        pageContext.setVariable(getString("admin", action, "returnVariable"), qry)
    }

    @Throws(PageException::class)
    private fun doGetExtensionInfo() {
        val ed: Resource = config.getExtensionDirectory()
        val sct: Struct = StructImpl()
        sct.set(KeyConstants._directory, ed.getPath())
        sct.set(KeyConstants._enabled, Caster.toBoolean(config.isExtensionEnabled()))
        pageContext.setVariable(getString("admin", action, "returnVariable"), sct)
    }

    /*
	 * private void doGetExtensions() throws PageException { Extension[] extensions =
	 * config.getExtensions(); lucee.runtime.type.Query qry = new QueryImpl(new String[] { "type",
	 * "provider", "id", "config", "version", "category", "description", "image", "label", "name",
	 * "author", "codename", "video", "support", "documentation", "forum", "mailinglist", "network",
	 * "created" }, 0, "query");
	 * 
	 * String provider = getString("provider", null); String id = getString("id", null); Extension
	 * extension; String extProvider, extId; int row = 0; for (int i = 0; i < extensions.length; i++) {
	 * extension = extensions[i]; if(!extension.getType().equalsIgnoreCase("all") &&
	 * toType(extension.getType(), false) != type) continue;
	 * 
	 * extProvider = extension.getProvider(); extId = extension.getId(); if(provider != null &&
	 * !provider.equalsIgnoreCase(extProvider)) continue; if(id != null && !id.equalsIgnoreCase(extId))
	 * continue;
	 * 
	 * qry.addRow(); row++; qry.setAt("provider", row, extProvider); qry.setAt(KeyConstants._id, row,
	 * extId); qry.setAt(KeyConstants._config, row, extension.getConfig(pageContext));
	 * qry.setAt(KeyConstants._version, row, extension.getVersion());
	 * 
	 * qry.setAt("category", row, extension.getCategory()); qry.setAt(KeyConstants._description, row,
	 * extension.getDescription()); qry.setAt("image", row, extension.getImage());
	 * qry.setAt(KeyConstants._label, row, extension.getLabel()); qry.setAt(KeyConstants._name, row,
	 * extension.getName());
	 * 
	 * qry.setAt(KeyConstants._author, row, extension.getAuthor()); qry.setAt("codename", row,
	 * extension.getCodename()); qry.setAt("video", row, extension.getVideo()); qry.setAt("support",
	 * row, extension.getSupport()); qry.setAt("documentation", row, extension.getDocumentation());
	 * qry.setAt("forum", row, extension.getForum()); qry.setAt("mailinglist", row,
	 * extension.getMailinglist()); qry.setAt("network", row, extension.getNetwork());
	 * qry.setAt(KeyConstants._created, row, extension.getCreated()); qry.setAt(KeyConstants._type, row,
	 * extension.getType());
	 * 
	 * } pageContext.setVariable(getString("admin", action, "returnVariable"), qry); }
	 */
    @Throws(PageException::class)
    private fun doGetMappings() {
        val mappings: Array<Mapping?> = config.getMappings()
        val qry: lucee.runtime.type.Query = QueryImpl(arrayOf<String?>("archive", "strarchive", "physical", "strphysical", "virtual", "hidden", "physicalFirst", "readonly", "inspect", "toplevel"), mappings.size,
                "query")
        for (i in mappings.indices) {
            val m: MappingImpl? = mappings[i] as MappingImpl?
            val row: Int = i + 1
            qry.setAt("archive", row, m.getArchive())
            qry.setAt("strarchive", row, m.getStrArchive())
            qry.setAt("physical", row, m.getPhysical())
            qry.setAt("strphysical", row, m.getStrPhysical())
            qry.setAt("virtual", row, m.getVirtual())
            qry.setAt("hidden", row, Caster.toBoolean(m.isHidden()))
            qry.setAt("physicalFirst", row, Caster.toBoolean(m.isPhysicalFirst()))
            qry.setAt("readonly", row, Caster.toBoolean(m.isReadonly()))
            qry.setAt("inspect", row, ConfigWebUtil.inspectTemplate(m.getInspectTemplateRaw(), ""))
            qry.setAt("toplevel", row, Caster.toBoolean(m.isTopLevel()))
        }
        pageContext.setVariable(getString("admin", action, "returnVariable"), qry)
    }

    @Throws(PageException::class)
    private fun doGetRestMappings() {
        val mappings: Array<lucee.runtime.rest.Mapping?> = config.getRestMappings()
        val qry: lucee.runtime.type.Query = QueryImpl(arrayOf<String?>("physical", "strphysical", "virtual", "hidden", "readonly", "default"), mappings.size, "query")
        var m: lucee.runtime.rest.Mapping?
        for (i in mappings.indices) {
            m = mappings[i]
            val row: Int = i + 1
            qry.setAt("physical", row, m!!.getPhysical())
            qry.setAt("strphysical", row, m.getStrPhysical())
            qry.setAt("virtual", row, m.getVirtual())
            qry.setAt("hidden", row, Caster.toBoolean(m!!.isHidden()))
            qry.setAt("readonly", row, Caster.toBoolean(m!!.isReadonly()))
            qry.setAt("default", row, Caster.toBoolean(m!!.isDefault()))
        }
        pageContext.setVariable(getString("admin", action, "returnVariable"), qry)
    }

    @Throws(PageException::class)
    private fun doGetRestSettings() {
        val sct: Struct = StructImpl()
        sct.set(KeyConstants._list, Caster.toBoolean(config.getRestList()))
        pageContext.setVariable(getString("admin", action, "returnVariable"), sct)
    }

    @Throws(PageException::class)
    private fun doGetResourceProviders() {
        pageContext.setVariable(getString("admin", action, "returnVariable"), admin.getResourceProviders())
    }

    @Throws(PageException::class)
    private fun doUpdateAdminSyncClass() {
        val cd: ClassDefinition = ClassDefinitionImpl(getString("admin", action, "class"), getString("bundleName", null), getString("bundleVersion", null),
                config.getIdentification())
        admin.updateAdminSyncClass(cd)
        store()
    }

    @Throws(PageException::class)
    private fun doGetAdminSyncClass() {
        pageContext.setVariable(getString("admin", action, "returnVariable"), config.getAdminSyncClass().getName())
    }

    @Throws(PageException::class)
    private fun doUpdateVideoExecuterClass() {
        val cd: ClassDefinition = ClassDefinitionImpl(getString("admin", action, "class"), getString("bundleName", null), getString("bundleVersion", null),
                config.getIdentification())
        admin.updateVideoExecuterClass(cd)
        store()
    }

    @Throws(PageException::class)
    private fun doGetVideoExecuterClass() {
        pageContext.setVariable(getString("admin", action, "returnVariable"), config.getVideoExecuterClass().getName())
    }

    /**
     * @throws PageException
     */
    @Throws(PageException::class)
    private fun doRemoveMailServer() {
        admin.removeMailServer(getString("admin", action, "hostname"), getString("username", null))
        store()
        adminSync.broadcast(attributes, config)
    }

    /**
     * @throws PageException
     */
    @Throws(PageException::class)
    private fun doUpdateMailServer() {
        admin.updateMailServer(getInt("id", -1), getString("admin", action, "hostname"), getString("admin", action, "dbusername"), getString("admin", action, "dbpassword"),
                Caster.toIntValue(getString("admin", action, "port")), getBoolV("tls", false), getBoolV("ssl", false), toTimeout(getObject("life", null), (1000 * 60 * 5).toLong()),
                toTimeout(getObject("idle", null), (1000 * 60 * 5).toLong()), getBoolV("reuseConnection", true)
        )
        store()
        adminSync.broadcast(attributes, config)
    }

    /**
     * @throws PageException
     */
    @Throws(PageException::class)
    private fun doUpdateMailSetting() {
        // admin.setMailLog(getString("admin",action,"logfile"),getString("loglevel","ERROR"));
        admin.setMailSpoolEnable(getBoolObject("admin", action, "spoolenable"))

        /*
		 * / spool interval String str=getString("admin",action,"maxThreads"); Integer i=null;
		 * if(!StringUtil.isEmpty(str))i=Caster.toInteger(maxThreads);
		 */

        // timeout
        val str = getString("admin", action, "timeout")
        var i: Integer? = null
        if (!StringUtil.isEmpty(str)) i = Caster.toInteger(str)
        admin.setMailTimeout(i)
        admin.setMailDefaultCharset(getString("admin", action, "defaultencoding"))
        store()
        adminSync.broadcast(attributes, config)
    }

    @Throws(PageException::class)
    private fun doUpdateTaskSetting() {

        // max Threads
        val str = getString("admin", action, "maxThreads")
        var i: Integer? = null
        if (!StringUtil.isEmpty(str)) {
            i = Caster.toInteger(str)
            if (i.intValue() < 10) throw ApplicationException("We need at least 10 threads to run tasks properly")
        }
        admin.setTaskMaxThreads(i)
        store()
        adminSync.broadcast(attributes, config)
    }

    @Throws(PageException::class)
    private fun listPatches() {
        try {
            pageContext.setVariable(getString("admin", action, "returnVariable"), Caster.toArray((config as ConfigServerImpl?).getInstalledPatches()))
        } catch (e: Exception) {
            throw Caster.toPageException(e)
        }
    }

    @get:Throws(PageException::class)
    private val minVersion: Unit
        private get() {
            try {
                pageContext.setVariable(getString("admin", action, "returnVariable"), VersionInfo.getIntVersion().toString())
            } catch (e: Exception) {
                throw Caster.toPageException(e)
            }
        }

    @get:Throws(PageException::class)
    private val loaderInfo: Unit
        private get() {
            try {
                val sct: Struct = StructImpl()
                sct.set("LoaderVersion", VersionInfo.getIntVersion().toString())
                sct.set("LuceeVersion", pageContext.getConfig().getFactory().getEngine().getInfo().getVersion().toString())
                sct.set("LoaderPath", ClassUtil.getSourcePathForClass("lucee.loader.servlet.CFMLServlet", ""))
                pageContext.setVariable(getString("admin", action, "returnVariable"), sct)
            } catch (e: Exception) {
                throw Caster.toPageException(e)
            }
        }

    @Throws(PageException::class)
    private fun doGetMailServers() {
        val servers: Array<Server?> = config.getMailServers()
        val qry: lucee.runtime.type.Query = QueryImpl(arrayOf<String?>("id", "hostname", "password", "passwordEncrypted", "username", "port", "authentication", "readonly", "tls", "ssl", "life", "idle", "type"),
                servers.size, "query")
        for (i in servers.indices) {
            val s: Server? = servers[i]
            val row: Int = i + 1
            qry.setAt("id", row, if (s is ServerImpl) (s as ServerImpl?).getId() else -1)
            qry.setAt("hostname", row, s.getHostName())
            qry.setAt("password", row, if (s.isReadOnly()) "" else s.getPassword())
            qry.setAt("passwordEncrypted", row, if (s.isReadOnly()) "" else ConfigWebUtil.encrypt(s.getPassword()))
            qry.setAt("username", row, if (s.isReadOnly()) "" else s.getUsername())
            qry.setAt("port", row, Caster.toInteger(s.getPort()))
            qry.setAt("readonly", row, Caster.toBoolean(s.isReadOnly()))
            qry.setAt("authentication", row, Caster.toBoolean(s.hasAuthentication()))
            qry.setAt("ssl", row, Caster.toBoolean(s.isSSL()))
            qry.setAt("tls", row, Caster.toBoolean(s.isTLS()))
            if (s is ServerImpl) {
                val si: ServerImpl? = s as ServerImpl?
                qry.setAt("type", row, if (si.getType() === ServerImpl.TYPE_GLOBAL) "global" else "local")
                qry.setAt("life", row, si.getLifeTimeSpan() / 1000)
                qry.setAt("idle", row, si.getIdleTimeSpan() / 1000)
            }
        }
        pageContext.setVariable(getString("admin", action, "returnVariable"), qry)
    }

    @Throws(PageException::class)
    private fun doGetRunningThreads() {
        val qry: lucee.runtime.type.Query = QueryImpl(arrayOf<String?>("Id", "Start", "Timeout", "ThreadType", "StackTrace", "TagContext", "Label", "RootPath", "ConfigFile", "URL"),
                0, "query")
        if (type == TYPE_WEB) {
            fillGetRunningThreads(qry, pageContext.getConfig())
        } else {
            val cs: ConfigServer = pageContext.getConfig().getConfigServer(password)
            val webs: Array<ConfigWeb?> = cs.getConfigWebs()
            for (i in webs.indices) {
                fillGetRunningThreads(qry, webs[i])
            }
        }
        pageContext.setVariable(getString("admin", action, "returnVariable"), qry)
    }

    /**
     * @throws PageException
     */
    @Throws(PageException::class)
    private fun doGetMailSetting() {
        val sct: Struct = StructImpl()
        pageContext.setVariable(getString("admin", action, "returnVariable"), sct)
        var maxThreads = 20
        val engine: SpoolerEngine = config.getSpoolerEngine()
        if (engine is SpoolerEngineImpl) {
            maxThreads = (engine as SpoolerEngineImpl).getMaxThreads()
        }
        sct.set("spoolEnable", Caster.toBoolean(config.isMailSpoolEnable()))
        sct.set("spoolInterval", Caster.toInteger(config.getMailSpoolInterval()))
        sct.set("maxThreads", Caster.toDouble(maxThreads))
        sct.set("timeout", Caster.toInteger(config.getMailTimeout()))
        sct.set("defaultencoding", config.getMailDefaultCharset().name())
    }

    @Throws(PageException::class)
    private fun doGetTaskSetting() {
        val sct: Struct = StructImpl()
        pageContext.setVariable(getString("admin", action, "returnVariable"), sct)
        var maxThreads = 20
        val engine: SpoolerEngine = config.getSpoolerEngine()
        if (engine is SpoolerEngineImpl) {
            val ei: SpoolerEngineImpl = engine as SpoolerEngineImpl
            maxThreads = ei.getMaxThreads()
        }
        sct.set("maxThreads", Caster.toDouble(maxThreads))
    }

    /**
     * @throws PageException
     */
    @Throws(PageException::class)
    private fun doGetTLDs() {
        val qry: lucee.runtime.type.Query = QueryImpl(arrayOf<String?>("displayname", "namespace", "namespaceseparator", "shortname", "type", "description", "uri", "elclass", "elBundleName", "elBundleVersion",
                "source"), arrayOf<String?>("varchar", "varchar", "varchar", "varchar", "varchar", "varchar", "varchar", "varchar", "varchar", "varchar", "varchar"), 0, "tlds")
        val dialect: Int = if ("lucee".equalsIgnoreCase(getString("dialect", "cfml"))) CFMLEngine.DIALECT_LUCEE else CFMLEngine.DIALECT_CFML
        val libs: Array<TagLib?> = config.getTLDs(dialect)
        for (i in libs.indices) {
            qry.addRow()
            qry.setAt("displayname", i + 1, libs[i].getDisplayName())
            qry.setAt("namespace", i + 1, libs[i].getNameSpace())
            qry.setAt("namespaceseparator", i + 1, libs[i].getNameSpaceSeparator())
            qry.setAt("shortname", i + 1, libs[i].getShortName())
            qry.setAt("type", i + 1, libs[i].getType())
            qry.setAt("description", i + 1, libs[i].getDescription())
            qry.setAt("uri", i + 1, Caster.toString(libs[i].getUri()))
            qry.setAt("elclass", i + 1, libs[i].getELClassDefinition().getClassName())
            qry.setAt("elBundleName", i + 1, libs[i].getELClassDefinition().getName())
            qry.setAt("elBundleVersion", i + 1, libs[i].getELClassDefinition().getVersionAsString())
            qry.setAt("source", i + 1, StringUtil.emptyIfNull(libs[i].getSource()))
        }
        pageContext.setVariable(getString("admin", action, "returnVariable"), qry)
    }

    @Throws(PageException::class)
    private fun doGetRHServerExtension() {
        _doGetRHExtension(config.getServerRHExtensions())
    }

    @Throws(PageException::class)
    private fun doGetRHExtension() {
        _doGetRHExtension(config.getRHExtensions())
    }

    @Throws(PageException::class)
    private fun _doGetRHExtension(extensions: Array<RHExtension?>?) {
        val id = getString("admin", action, "id")
        if (StringUtil.isEmpty(id, true)) throw ApplicationException("Extension ID cannot be empty")
        for (ext in extensions!!) {
            if (id!!.equals(ext.getId()) || id.equals(ext.getSymbolicName())) {
                pageContext.setVariable(getString("admin", action, "returnVariable"), ext.toStruct())
                return
            }
        }
        throw ApplicationException("No Extension found with ID [$id]")
    }

    @Throws(PageException::class)
    private fun doGetRHExtensions() {
        pageContext.setVariable(getString("admin", action, "returnVariable"), RHExtension.toQuery(config, config.getRHExtensions(), null))
    }

    @Throws(PageException::class)
    private fun doGetRHServerExtensions() {
        pageContext.setVariable(getString("admin", action, "returnVariable"), RHExtension.toQuery(config, config.getServerRHExtensions(), null))
    }

    @Throws(PageException::class)
    private fun doGetLocalExtension() {
        val id = getString("admin", action, "id")
        val asBinary = getBoolV("asBinary", false)
        if (asBinary) {
            val it: Iterator<ExtensionDefintion?> = DeployHandler.getLocalExtensions(config, false).iterator()
            var ext: ExtensionDefintion?
            while (it.hasNext()) {
                ext = it.next()
                if (id.equalsIgnoreCase(ext.getId())) {
                    try {
                        pageContext.setVariable(getString("admin", action, "returnVariable"), IOUtil.toBytes(ext.getSource()))
                        return
                    } catch (e: IOException) {
                        throw Caster.toPageException(e)
                    }
                }
            }
            throw ApplicationException("No local Extension found with with id [$id]")
        } else {
            val locals: List<RHExtension?> = RHExtension.toRHExtensions(DeployHandler.getLocalExtensions(config, false))
            val qry: Query = RHExtension.toQuery(config, locals, null)
            val rows: Int = qry.getRecordcount()
            var _id: String
            var row = 0
            for (r in 1..rows) {
                _id = Caster.toString(qry.getAt(KeyConstants._id, r), null)
                if (id.equalsIgnoreCase(_id)) {
                    row = r
                    break
                }
            }
            if (row == 0) throw ApplicationException("No local Extension found with id [$id]")
            pageContext.setVariable(getString("admin", action, "returnVariable"), Caster.toStruct(qry, row))
        }
    }

    @Throws(PageException::class)
    private fun doGetLocalExtensions() {
        val locals: List<RHExtension?> = RHExtension.toRHExtensions(DeployHandler.getLocalExtensions(config, false))
        val qry: Query = RHExtension.toQuery(config, locals, null)
        pageContext.setVariable(getString("admin", action, "returnVariable"), qry)
    }

    /**
     * @throws PageException
     */
    @Throws(PageException::class)
    private fun doGetFLDs() {
        val qry: lucee.runtime.type.Query = QueryImpl(arrayOf<String?>("displayname", "namespace", "namespaceseparator", "shortname", "description", "uri", "source"), arrayOf<String?>("varchar", "varchar", "varchar", "varchar", "varchar", "varchar", "varchar"), 0, "tlds")
        val dialect: Int = if ("lucee".equalsIgnoreCase(getString("dialect", "cfml"))) CFMLEngine.DIALECT_LUCEE else CFMLEngine.DIALECT_CFML
        val libs: Array<FunctionLib?> = config.getFLDs(dialect)
        for (i in libs.indices) {
            qry.addRow()
            qry.setAt("displayname", i + 1, libs[i].getDisplayName())
            qry.setAt("namespace", i + 1, "") // TODO support for namespace
            qry.setAt("namespaceseparator", i + 1, "")
            qry.setAt("shortname", i + 1, libs[i].getShortName())
            qry.setAt("description", i + 1, libs[i].getDescription())
            qry.setAt("uri", i + 1, Caster.toString(libs[i].getUri()))
            qry.setAt("source", i + 1, StringUtil.emptyIfNull(libs[i].getSource()))
        }
        pageContext.setVariable(getString("admin", action, "returnVariable"), qry)
    }

    @Throws(PageException::class)
    private fun doGetRemoteClientUsage() {
        val qry: lucee.runtime.type.Query = QueryImpl(arrayOf<String?>("code", "displayname"), arrayOf<String?>("varchar", "varchar"), 0, "usage")
        val usages: Struct = config.getRemoteClientUsage()
        // Key[] keys = usages.keys();
        val it: Iterator<Entry<Key?, Object?>?> = usages.entryIterator()
        var e: Entry<Key?, Object?>?
        var i = -1
        while (it.hasNext()) {
            i++
            e = it.next()
            qry.addRow()
            qry.setAt(KeyConstants._code, i + 1, e.getKey().getString())
            qry.setAt(KeyConstants._displayname, i + 1, e.getValue())
            // qry.setAt("description", i+1, usages[i].getDescription());
        }
        pageContext.setVariable(getString("admin", action, "returnVariable"), qry)
    }

    @Throws(PageException::class)
    private fun doHasRemoteClientUsage() {
        val usages: Struct = config.getRemoteClientUsage()
        pageContext.setVariable(getString("admin", action, "returnVariable"), if (usages.isEmpty()) Boolean.FALSE else Boolean.TRUE)
    }

    @Throws(PageException::class)
    private fun doGetJars() {
        val lib: Resource = config.getLibraryDirectory()
        val qry: lucee.runtime.type.Query = QueryImpl(arrayOf<Key?>(KeyConstants._name, KeyConstants._source, KeyConstants._info), arrayOf<String?>("varchar", "varchar", "varchar"),
                0, "jars")
        if (lib.isDirectory()) {
            val children: Array<Resource?> = lib.listResources(ExtensionResourceFilter(arrayOf<String?>(".jar", ".zip"), false, true))
            for (i in children.indices) {
                qry.addRow()
                qry.setAt(KeyConstants._name, i + 1, children[i].getName())
                qry.setAt(KeyConstants._source, i + 1, children[i].getAbsolutePath())
                try {
                    qry.setAt(KeyConstants._info, i + 1, BundleFile.getInstance(children[i]).info())
                } catch (e: Exception) {
                }
            }
        }
        pageContext.setVariable(getString("admin", action, "returnVariable"), qry)
    }

    @Throws(PageException::class)
    private fun doUpdateJDBCDriver() {
        val cd: ClassDefinition = ClassDefinitionImpl(getString("admin", action, "classname"), getString("bundleName", null), getString("bundleVersion", null),
                config.getIdentification())
        val label = getString("admin", action, "label")
        val id = getString("id", null)
        admin.updateJDBCDriver(label, id, cd)
        store()
        adminSync.broadcast(attributes, config)
    }

    /**
     * @throws PageException
     */
    @Throws(PageException::class)
    private fun doUpdateDatasource() {
        var allow: Int = ((if (getBoolV("allowed_select", false)) DataSource.ALLOW_SELECT else 0) + (if (getBoolV("allowed_insert", false)) DataSource.ALLOW_INSERT else 0)
                + (if (getBoolV("allowed_update", false)) DataSource.ALLOW_UPDATE else 0) + (if (getBoolV("allowed_delete", false)) DataSource.ALLOW_DELETE else 0)
                + (if (getBoolV("allowed_alter", false)) DataSource.ALLOW_ALTER else 0) + (if (getBoolV("allowed_drop", false)) DataSource.ALLOW_DROP else 0)
                + (if (getBoolV("allowed_revoke", false)) DataSource.ALLOW_REVOKE else 0) + (if (getBoolV("allowed_grant", false)) DataSource.ALLOW_GRANT else 0)
                + if (getBoolV("allowed_create", false)) DataSource.ALLOW_CREATE else 0)
        if (allow == 0) allow = DataSource.ALLOW_ALL
        var cn = getString("admin", action, "classname")
        if ("com.microsoft.jdbc.sqlserver.SQLServerDriver".equals(cn)) {
            cn = "com.microsoft.sqlserver.jdbc.SQLServerDriver"
        }
        val pattern: Pattern = Pattern.compile("[a-zA-Z0-9_]*")
        val matcher: Matcher = pattern.matcher(getString("admin", action, "newName"))
        if (matcher.matches() === false) {
            throw ExpressionException("Trying to create a data source with a name that is invalid. Data source Names must match proper variable naming conventions")
        }
        val cd: ClassDefinition = ClassDefinitionImpl(cn, getString("bundleName", null), getString("bundleVersion", null), config.getIdentification())

        // customParameterSyntax
        val sct: Struct? = getStruct("customParameterSyntax", null)
        val ps: ParamSyntax = if (sct != null && sct.containsKey("delimiter") && sct.containsKey("separator")) ParamSyntax.toParamSyntax(sct) else ParamSyntax.DEFAULT

        //
        val literalTimestampWithTSOffset = getBoolV("literalTimestampWithTSOffset", false)
        val alwaysSetTimeout = getBoolV("alwaysSetTimeout", false)
        val requestExclusive = getBoolV("requestExclusive", false)
        val alwaysResetConnections = getBoolV("alwaysResetConnections", false)
        val id = getString("id", null)
        val dsn = getString("admin", action, "dsn")
        val name = getString("admin", action, "name")
        val newName = getString("admin", action, "newName")
        val username = getString("admin", action, "dbusername")
        val password = getString("admin", action, "dbpassword")
        val host = getString("host", "")
        val timezone = getString("timezone", "")
        val database = getString("database", "")
        val port = getInt("port", -1)
        val connLimit = getInt("connectionLimit", -1)
        var idleTimeout = getInt("connectionTimeout", -1)
        if (idleTimeout == -1) idleTimeout = getInt("idleTimeout", -1)
        val liveTimeout = getInt("liveTimeout", -1)
        val minIdle = getInt("minIdle", -1)
        val maxIdle = getInt("maxIdle", -1)
        val maxTotal = getInt("maxTotal", -1)
        val metaCacheTimeout = getLong("metaCacheTimeout", 60000)
        val blob = getBoolV("blob", false)
        val clob = getBoolV("clob", false)
        val validate = getBoolV("validate", false)
        val storage = getBoolV("storage", false)
        val verify = getBoolV("verify", true)
        val custom: Struct? = getStruct("custom", StructImpl())
        val dbdriver = getString("dbdriver", "")

        // config.getDatasourceConnectionPool().remove(name);
        var ds: DataSourcePro? = null
        try {
            ds = DataSourceImpl(config, name, cd, host, dsn, database, port, username, password, null, connLimit, idleTimeout, liveTimeout, minIdle, maxIdle, maxTotal,
                    metaCacheTimeout, blob, clob, allow, custom, false, validate, storage, null, dbdriver, ps, literalTimestampWithTSOffset, alwaysSetTimeout, requestExclusive,
                    alwaysResetConnections, ThreadLocalPageContext.getLog(pageContext, "application"))
        } catch (e: Exception) {
            throw Caster.toPageException(e)
        }
        if (verify) _doVerifyDatasource(ds, username, password)
        // print.out("limit:"+connLimit);
        admin.updateDataSource(id, name, newName, cd, dsn, username, password, host, database, port, connLimit, idleTimeout, liveTimeout, metaCacheTimeout, blob, clob, allow,
                validate, storage, timezone, custom, dbdriver, ps, literalTimestampWithTSOffset, alwaysSetTimeout, requestExclusive, alwaysResetConnections)
        store()
        adminSync.broadcast(attributes, config)
    }

    @Throws(PageException::class)
    private fun doUpdateCacheConnection() {
        val cd: ClassDefinition = ClassDefinitionImpl(getString("admin", action, "class"), getString("bundleName", null), getString("bundleVersion", null),
                config.getIdentification())
        admin.updateCacheConnection(getString("admin", action, "name"), cd, toCacheConstant("default"), getStruct("admin", action, "custom"), getBoolV("readOnly", false),
                getBoolV("storage", false)
        )
        store()
        adminSync.broadcast(attributes, config)
    }

    @Throws(PageException::class)
    private fun doUpdateGatewayEntry() {
        val strStartupMode = getString("admin", action, "startupMode")
        val startup: Int = GatewayEntryImpl.toStartup(strStartupMode, -1)
        if (startup == -1) throw ApplicationException("Invalid startup mode [$strStartupMode], valid values are [automatic,manual,disabled]")

        // custom validation
        val custom: Struct? = getStruct("admin", action, "custom")
        if (custom != null) {
            val path: String = Caster.toString(custom.get("directory", null), null)
            if (!StringUtil.isEmpty(path)) { //
                val dir: Resource = ResourceUtil.toResourceNotExisting(pageContext, path)
                if (!dir.isDirectory()) throw ApplicationException("Directory [$path ] not exists ")
            }
        }
        // listenerCfcPath validation
        /*
		 * String path = getString("admin", action, "listenerCfcPath"); if(!StringUtil.isEmpty(path,true)) {
		 * path=path.trim().replace('\\','/'); if(path.indexOf("./")==-1)path=path.replace('.','/'); String
		 * ext = "."+Constants.getCFMLComponentExtension(); if(!path.endsWith(ext)) path+=ext;
		 * 
		 * Resource listnerCFC = ResourceUtil.toResourceNotExisting(pageContext, path);
		 * if(!listnerCFC.exists()) throw new ApplicationException("invalid [" + listnerCFC
		 * +" ] listener CFC"); }
		 */
        val cd: ClassDefinition = ClassDefinitionImpl(getString("admin", action, "class"), getString("bundleName", null), getString("bundleVersion", null),
                config.getIdentification())
        admin.updateGatewayEntry(getString("admin", action, "id"), cd, getString("admin", action, "cfcPath"), getString("admin", action, "listenerCfcPath"), startup,
                getStruct("admin", action, "custom"), getBoolV("readOnly", false)
        )
        store()
        adminSync.broadcast(attributes, config)
    }

    @Throws(ApplicationException::class)
    private fun toCacheConstant(name: String?): Int {
        var def = getString(name, null)
        if (StringUtil.isEmpty(def)) return Config.CACHE_TYPE_NONE
        def = def.trim().toLowerCase()
        if (def.equals("object")) return ConfigPro.CACHE_TYPE_OBJECT
        if (def.equals("template")) return ConfigPro.CACHE_TYPE_TEMPLATE
        if (def.equals("query")) return ConfigPro.CACHE_TYPE_QUERY
        if (def.equals("resource")) return ConfigPro.CACHE_TYPE_RESOURCE
        if (def.equals("function")) return ConfigPro.CACHE_TYPE_FUNCTION
        if (def.equals("include")) return ConfigPro.CACHE_TYPE_INCLUDE
        if (def.equals("http")) return ConfigPro.CACHE_TYPE_HTTP
        if (def.equals("file")) return ConfigPro.CACHE_TYPE_FILE
        if (def.equals("webservice")) return ConfigPro.CACHE_TYPE_WEBSERVICE
        throw ApplicationException("Invalid default type [$def], valid default types are [object,template,query,resource,function]")
    }

    @Throws(PageException::class)
    private fun doUpdateCacheDefaultConnection() {
        admin.updateCacheDefaultConnection(ConfigPro.CACHE_TYPE_OBJECT, getString("admin", action, "object"))
        admin.updateCacheDefaultConnection(ConfigPro.CACHE_TYPE_TEMPLATE, getString("admin", action, "template"))
        admin.updateCacheDefaultConnection(ConfigPro.CACHE_TYPE_QUERY, getString("admin", action, "query"))
        admin.updateCacheDefaultConnection(ConfigPro.CACHE_TYPE_RESOURCE, getString("admin", action, "resource"))
        admin.updateCacheDefaultConnection(ConfigPro.CACHE_TYPE_FUNCTION, getString("admin", action, "function"))
        admin.updateCacheDefaultConnection(ConfigPro.CACHE_TYPE_INCLUDE, getString("admin", action, "include"))
        admin.updateCacheDefaultConnection(ConfigPro.CACHE_TYPE_HTTP, getString("admin", action, "http"))
        admin.updateCacheDefaultConnection(ConfigPro.CACHE_TYPE_FILE, getString("admin", action, "file"))
        admin.updateCacheDefaultConnection(ConfigPro.CACHE_TYPE_WEBSERVICE, getString("admin", action, "webservice"))
        store()
        adminSync.broadcast(attributes, config)
    }

    @Throws(PageException::class)
    private fun doRemoveCacheDefaultConnection() {
        admin.removeCacheDefaultConnection(ConfigPro.CACHE_TYPE_OBJECT)
        admin.removeCacheDefaultConnection(ConfigPro.CACHE_TYPE_TEMPLATE)
        admin.removeCacheDefaultConnection(ConfigPro.CACHE_TYPE_QUERY)
        admin.removeCacheDefaultConnection(ConfigPro.CACHE_TYPE_RESOURCE)
        admin.removeCacheDefaultConnection(ConfigPro.CACHE_TYPE_FUNCTION)
        admin.removeCacheDefaultConnection(ConfigPro.CACHE_TYPE_INCLUDE)
        admin.removeCacheDefaultConnection(ConfigPro.CACHE_TYPE_HTTP)
        admin.removeCacheDefaultConnection(ConfigPro.CACHE_TYPE_FILE)
        admin.removeCacheDefaultConnection(ConfigPro.CACHE_TYPE_WEBSERVICE)
        store()
        adminSync.broadcast(attributes, config)
    }

    @Throws(PageException::class)
    private fun doRemoveLogSetting() {
        admin.removeLogSetting(getString("admin", "RemoveLogSettings", "name"))
        store()
        adminSync.broadcast(attributes, config)
    }

    @Throws(PageException::class)
    private fun doRemoveResourceProvider() {
        /*
		 * ClassDefinition cd = new ClassDefinitionImpl( getString("admin",action,"class") ,
		 * getString("bundleName",null) ,getString("bundleVersion",null), config.getIdentification());
		 */
        admin.removeResourceProvider(getString("admin", action, "scheme"))
        store()
        adminSync.broadcast(attributes, config)
    }

    @Throws(PageException::class)
    private fun doUpdateResourceProvider() {
        val cd: ClassDefinition = ClassDefinitionImpl(getString("admin", action, "class"), getString("bundleName", null), getString("bundleVersion", null),
                config.getIdentification())
        val scheme = getString("admin", action, "scheme")
        val sctArguments: Struct? = getStruct("arguments", null)
        if (sctArguments != null) {
            admin.updateResourceProvider(scheme, cd, sctArguments)
        } else {
            val strArguments = getString("admin", action, "arguments")
            admin.updateResourceProvider(scheme, cd, strArguments)
        }

        // admin.updateResourceProvider(scheme,clazz,arguments);
        store()
        adminSync.broadcast(attributes, config)
    }

    @Throws(PageException::class)
    private fun doUpdateDefaultResourceProvider() {
        val cd: ClassDefinition = ClassDefinitionImpl(getString("admin", action, "class"), getString("bundleName", null), getString("bundleVersion", null),
                config.getIdentification())
        val arguments = getString("admin", action, "arguments")
        admin.updateDefaultResourceProvider(cd, arguments)
        store()
        adminSync.broadcast(attributes, config)
    }

    @Throws(PageException::class)
    private fun doVerifyMailServer() {
        _doVerifyMailServer(getString("admin", action, "hostname"), getInt("admin", action, "port"), getString("admin", action, "mailusername"),
                getString("admin", action, "mailpassword"))
    }

    @Throws(PageException::class)
    private fun _doVerifyMailServer(host: String?, port: Int, user: String?, pass: String?) {
        try {
            SMTPVerifier.verify(host, user, pass, port)
        } catch (e: SMTPException) {
            throw Caster.toPageException(e)
        }
    }

    /**
     * @throws PageException
     */
    @Throws(PageException::class)
    private fun doVerifyDatasource() {
        val cd: ClassDefinition = ClassDefinitionImpl(Caster.toString(attributes.get("classname", null), null), Caster.toString(attributes.get("bundleName", null), null),
                Caster.toString(attributes.get("bundleVersion", null), null), config.getIdentification())
        var connStr = attributes.get("connStr", null) as String
        if (StringUtil.isEmpty(connStr)) connStr = attributes.get("dsn", null)
        if (cd.hasClass() && connStr != null) {
            _doVerifyDatasource(cd, connStr, getString("admin", action, "dbusername"), getString("admin", action, "dbpassword"))
        } else {
            _doVerifyDatasource(getString("admin", action, "name"), getString("admin", action, "dbusername"), getString("admin", action, "dbpassword"))
        }
    }

    @Throws(PageException::class)
    private fun doVerifyRemoteClient() {
        // SNSN
        /*
		 * SerialNumber sn = config.getSerialNumber(); if(sn.getVersion()==SerialNumber.VERSION_COMMUNITY)
		 * throw new
		 * SecurityException("can not verify remote client with "+sn.getStringVersion()+" version of Lucee"
		 * );
		 */
        var pd: ProxyData? = null
        val proxyServer = getString("proxyServer", null)
        if (!StringUtil.isEmpty(proxyServer)) {
            val proxyUsername = getString("proxyUsername", null)
            val proxyPassword = getString("proxyPassword", null)
            val proxyPort = getInt("proxyPort", -1)
            pd = ProxyDataImpl()
            pd.setServer(proxyServer)
            if (!StringUtil.isEmpty(proxyUsername)) pd.setUsername(proxyUsername)
            if (!StringUtil.isEmpty(proxyPassword)) pd.setPassword(proxyPassword)
            if (proxyPort != -1) pd.setPort(proxyPort)
        }
        val client: RemoteClient = RemoteClientImpl(getString("admin", action, "label"), if (type == TYPE_WEB) "web" else "server", getString("admin", action, "url"),
                getString("serverUsername", null), getString("serverPassword", null), getString("admin", action, "adminPassword"), pd, getString("admin", action, "securityKey"),
                getString("admin", action, "usage")
        )
        val attrColl: Struct = StructImpl()
        attrColl.setEL(KeyConstants._action, "connect")
        try {
            RemoteClientTask(null, client, attrColl, callerId, "synchronisation").execute(config)
        } catch (t: Throwable) {
            ExceptionUtil.rethrowIfNecessary(t)
            throw Caster.toPageException(t)
        }
    }

    @Throws(PageException::class)
    private fun _doVerifyDatasource(ds: DataSourcePro?, username: String?, password: String?) {
        try {
            val dc = DatasourceConnectionImpl(null, ds.getConnection(config, username, password), ds, username, password)
            dc.close()
        } catch (e: Exception) {
            throw Caster.toPageException(e)
        }
    }

    @Throws(PageException::class)
    private fun _doVerifyDatasource(name: String?, username: String?, password: String?) {
        val manager: DataSourceManager = pageContext.getDataSourceManager()
        manager.releaseConnection(pageContext, manager.getConnection(pageContext, name, username, password))
    }

    @Throws(PageException::class)
    private fun _doVerifyDatasource(cd: ClassDefinition?, connStrTranslated: String?, user: String?, pass: String?) {
        try {
            DataSourceImpl.verify(config, cd, connStrTranslated, user, pass)
        } catch (e: Exception) {
            throw Caster.toPageException(e)
        }
    }

    /**
     * @throws PageException
     */
    @Throws(PageException::class)
    private fun doUpdatePSQ() {
        admin.updatePSQ(getBoolObject("admin", action, "psq"))
        store()
        adminSync.broadcast(attributes, config)
    }

    @Throws(PageException::class)
    private fun doReload() {
        store()
    }

    @Throws(PageException::class)
    private fun doRemoveJDBCDriver() {
        admin.removeJDBCDriver(getString("admin", action, "class"))
        store()
        adminSync.broadcast(attributes, config)
    }

    /**
     * @throws PageException
     */
    @Throws(PageException::class)
    private fun doRemoveDatasource() {
        admin.removeDataSource(getString("admin", action, "name"))
        store()
        adminSync.broadcast(attributes, config)
    }

    @Throws(PageException::class)
    private fun doTerminateRunningThread() {
        val id = getInt("admin", "RemoveRunningThread", "id")
        if (type == TYPE_WEB) {
            terminateRunningThread(pageContext.getConfig(), id)
        } else {
            val cs: ConfigServer = pageContext.getConfig().getConfigServer(password)
            val webs: Array<ConfigWeb?> = cs.getConfigWebs()
            for (i in webs.indices) {
                if (terminateRunningThread(webs[i], id)) break
            }
        }
    }

    @Throws(PageException::class)
    private fun doRemoveRemoteClient() {
        admin.removeRemoteClient(getString("admin", action, "url"))
        store()
    }

    @Throws(PageException::class)
    private fun doRemoveSpoolerTask() {
        config.getSpoolerEngine().remove(getString("admin", action, "id"))
    }

    private fun doRemoveAllSpoolerTask() {
        (config.getSpoolerEngine() as SpoolerEngineImpl).removeAll()
    }

    @Throws(PageException::class)
    private fun doExecuteSpoolerTask() {
        val pe: PageException = config.getSpoolerEngine().execute(getString("admin", action, "id"))
        if (pe != null) throw pe
    }

    /**
     * @throws PageException
     */
    @Throws(PageException::class)
    private fun doGetDatasourceSetting() {
        val sct: Struct = StructImpl()
        pageContext.setVariable(getString("admin", action, "returnVariable"), sct)
        sct.set("psq", Caster.toBoolean(config.getPSQL()))
    }

    @Throws(PageException::class)
    private fun doGetORMSetting() {
        pageContext.setVariable(getString("admin", action, "returnVariable"), config.getORMConfig().toStruct())
    }

    @Throws(PageException::class)
    private fun doGetORMEngine() {
        val cd: ClassDefinition<out ORMEngine?> = config.getORMEngineClassDefintion()
        val sct: Struct = StructImpl()
        sct.set(KeyConstants._class, cd.getClassName())
        sct.set(KeyConstants._bundleName, cd.getName())
        sct.set(KeyConstants._bundleVersion, cd.getVersionAsString())
        pageContext.setVariable(getString("admin", action, "returnVariable"), sct)
    }

    @Throws(SecurityException::class, PageException::class)
    private fun doUpdateORMSetting() {
        val oc: ORMConfiguration = config.getORMConfig()
        val settings: Struct = StructImpl()
        settings.set(ORMConfigurationImpl.AUTO_GEN_MAP, getBool("admin", action, "autogenmap"))
        settings.set(ORMConfigurationImpl.EVENT_HANDLING, getBool("admin", action, "eventHandling"))
        settings.set(ORMConfigurationImpl.FLUSH_AT_REQUEST_END, getBool("admin", action, "flushatrequestend"))
        settings.set(ORMConfigurationImpl.LOG_SQL, getBool("admin", action, "logSQL"))
        settings.set(ORMConfigurationImpl.SAVE_MAPPING, getBool("admin", action, "savemapping"))
        settings.set(ORMConfigurationImpl.USE_DB_FOR_MAPPING, getBool("admin", action, "useDBForMapping"))
        settings.set(ORMConfigurationImpl.SECONDARY_CACHE_ENABLED, getBool("admin", action, "secondarycacheenabled"))
        settings.set(ORMConfigurationImpl.CATALOG, getString("admin", action, "catalog"))
        settings.set(ORMConfigurationImpl.SCHEMA, getString("admin", action, "schema"))
        settings.set(ORMConfigurationImpl.SQL_SCRIPT, getString("admin", action, "sqlscript"))
        settings.set(ORMConfigurationImpl.CACHE_CONFIG, getString("admin", action, "cacheconfig"))
        settings.set(ORMConfigurationImpl.CACHE_PROVIDER, getString("admin", action, "cacheProvider"))
        settings.set(ORMConfigurationImpl.ORM_CONFIG, getString("admin", action, "ormConfig"))

        // dbcreate
        val strDbcreate = getString("admin", action, "dbcreate")
        var dbcreate = "none"
        dbcreate = if ("none".equals(strDbcreate)) "none" else if ("update".equals(strDbcreate)) "update" else if ("dropcreate".equals(strDbcreate)) "dropcreate" else throw ApplicationException("Invalid dbcreate definition [$strDbcreate], valid dbcreate definitions are [none,update,dropcreate]")
        settings.set(ORMConfigurationImpl.DB_CREATE, dbcreate)

        // cfclocation
        val strCfclocation = getString("admin", action, "cfclocation")
        val arrCfclocation: Array = lucee.runtime.type.util.ListUtil.listToArray(strCfclocation, ",\n")
        val it: Iterator = arrCfclocation.valueIterator()
        var path: String
        while (it.hasNext()) {
            path = it.next()
            ResourceUtil.toResourceExisting(config, path)
        }
        settings.set(KeyConstants._cfcLocation, arrCfclocation)
        admin.updateORMSetting(ORMConfigurationImpl.load(config, null, settings, null, oc))
        store()
        adminSync.broadcast(attributes, config)
    }

    @Throws(SecurityException::class, PageException::class)
    private fun doResetORMSetting() {
        config.getORMConfig()
        admin.resetORMSetting()
        store()
        adminSync.broadcast(attributes, config)
    }

    @Throws(SecurityException::class, PageException::class)
    private fun doUpdatePerformanceSettings() {
        admin.updateInspectTemplate(getString("admin", action, "inspectTemplate"))
        admin.updateTypeChecking(getBoolObject("admin", action, "typeChecking"))

        // cached after
        var obj: Object? = getObject("cachedAfter", null)
        if (StringUtil.isEmpty(obj)) obj = null
        if (obj != null) admin.updateCachedAfterTimeRange(Caster.toTimeSpan(obj)) else admin.updateCachedAfterTimeRange(null)
        store()
        adminSync.broadcast(attributes, config)
    }

    @Throws(SecurityException::class, PageException::class)
    private fun doUpdateCompilerSettings() {
        admin.updateCompilerSettings(getBoolObject("admin", "UpdateCompilerSettings", "dotNotationUpperCase"),
                getBoolObject("admin", "UpdateCompilerSettings", "suppressWSBeforeArg"), getBoolObject("admin", "UpdateCompilerSettings", "nullSupport"),
                getBoolObject("admin", "UpdateCompilerSettings", "handleUnquotedAttrValueAsString"), getInteger("admin", "UpdateCompilerSettings", "externalizeStringGTE"),
                getBoolObject("admin", "UpdateCompilerSettings", "preciseMath"))
        admin.updateTemplateCharset(getString("admin", action, "templateCharset"))
        store()
        adminSync.broadcast(attributes, config)
    }

    /*
	 * private void doGetLogSetting() throws PageException { String name=getString("admin",
	 * "GetLogSetting", "name"); name=name.trim().toLowerCase(); Query qry=_doGetLogSettings();
	 * 
	 * int records = qry.getRecordcount(); for(int row=1;row<=records;row++){ String n =
	 * Caster.toString(qry.getAt("name", row, null),null); if(!StringUtil.isEmpty(n) &&
	 * n.trim().equalsIgnoreCase(name)) { Struct sct=new StructImpl(); String
	 * returnVariable=getString("admin",action,"returnVariable");
	 * pageContext.setVariable(returnVariable,sct);
	 * 
	 * sct.setEL(KeyConstants._name, qry.getAt(KeyConstants._name, row, ""));
	 * sct.setEL(KeyConstants._level, qry.getAt(KeyConstants._level, row, "")); sct.setEL("virtualpath",
	 * qry.getAt("virtualpath", row, "")); sct.setEL(KeyConstants._class, qry.getAt(KeyConstants._class,
	 * row, "")); sct.setEL("maxFile", qry.getAt("maxFile", row, "")); sct.setEL("maxFileSize",
	 * qry.getAt("maxFileSize", row, "")); sct.setEL(KeyConstants._path, qry.getAt(KeyConstants._path,
	 * row, ""));
	 * 
	 * return; } } throw new ApplicationException("invalid log name ["+name+"]");
	 * 
	 * }
	 */
    @Throws(PageException::class)
    private fun doGetCompilerSettings() {
        val returnVariable = getString("admin", action, "returnVariable")
        val sct: Struct = StructImpl()
        pageContext.setVariable(returnVariable, sct)
        sct.set("DotNotationUpperCase", if (config.getDotNotationUpperCase()) Boolean.TRUE else Boolean.FALSE)
        sct.set("suppressWSBeforeArg", if (config.getSuppressWSBeforeArg()) Boolean.TRUE else Boolean.FALSE)
        sct.set("nullSupport", if (config.getFullNullSupport()) Boolean.TRUE else Boolean.FALSE)
        sct.set("handleUnquotedAttrValueAsString", if (config.getHandleUnQuotedAttrValueAsString()) Boolean.TRUE else Boolean.FALSE)
        sct.set("templateCharset", config.getTemplateCharset())
        sct.set("externalizeStringGTE", Caster.toDouble(config.getExternalizeStringGTE()))
        sct.set("preciseMath", config.getPreciseMath())
    }

    @Throws(PageException::class)
    private fun doGetLogSettings() {
        val returnVariable = getString("admin", action, "returnVariable")
        pageContext.setVariable(returnVariable, _doGetLogSettings())
    }

    @Throws(PageException::class)
    private fun _doGetLogSettings(): Query? {
        val loggers: Map<String?, LoggerAndSourceData?> = config.getLoggers()
        val qry: Query = QueryImpl(arrayOf<String?>("name", "level", "appenderClass", "appenderBundleName", "appenderBundleVersion", "appenderArgs", "layoutClass", "layoutBundleName",
                "layoutBundleVersion", "layoutArgs", "readonly"), 0, lucee.runtime.type.util.ListUtil.last("logs", '.'))
        var row = 0
        val it: Iterator<Entry<String?, LoggerAndSourceData?>?> = loggers.entrySet().iterator()
        var e: Entry<String?, LoggerAndSourceData?>?
        var logger: LoggerAndSourceData
        while (it.hasNext()) {
            e = it.next()
            logger = e.getValue()
            if (logger.getDyn()) continue
            row = qry.addRow()
            // row++;
            qry.setAtEL("name", row, e.getKey())
            qry.setAtEL("level", row, LogUtil.levelToString(logger.getLevel(), ""))
            qry.setAtEL("appenderClass", row, logger.getAppenderClassDefinition().getClassName())
            qry.setAtEL("appenderBundleName", row, logger.getAppenderClassDefinition().getName())
            qry.setAtEL("appenderBundleVersion", row, logger.getAppenderClassDefinition().getVersionAsString())
            qry.setAtEL("appenderArgs", row, toStruct(logger.getAppenderArgs(true)))
            qry.setAtEL("layoutClass", row, logger.getLayoutClassDefinition().getClassName())
            qry.setAtEL("layoutBundleName", row, logger.getLayoutClassDefinition().getName())
            qry.setAtEL("layoutBundleVersion", row, logger.getLayoutClassDefinition().getVersionAsString())
            qry.setAtEL("layoutArgs", row, toStruct(logger.getLayoutArgs(true)))
            qry.setAtEL("readonly", row, logger.getReadOnly())
        }
        return qry
    }

    private fun toStruct(map: Map<String?, String?>?): Object? {
        val sct: Struct = StructImpl()
        if (map != null) {
            val it: Iterator<Entry<String?, String?>?> = map.entrySet().iterator()
            var e: Entry<String?, String?>?
            while (it.hasNext()) {
                e = it.next()
                sct.setEL(e.getKey(), e.getValue())
            }
        }
        return sct
    }

    @Throws(ApplicationException::class, PageException::class)
    private fun doGetPerformanceSettings() {
        val sct: Struct = StructImpl()
        pageContext.setVariable(getString("admin", action, "returnVariable"), sct)
        val it: Short = config.getInspectTemplate()
        var str = "once"
        if (it == ConfigPro.INSPECT_ALWAYS) str = "always" else if (it == ConfigPro.INSPECT_NEVER) str = "never"
        sct.set("inspectTemplate", str)
        sct.set("typeChecking", config.getTypeChecking())

        // cached within
        var cachedAfter: TimeSpan? = config.getCachedAfterTimeRange()
        if (cachedAfter == null) cachedAfter = TimeSpanImpl(0, 0, 0, 0)
        sct.set("cachedAfter", cachedAfter)
        sct.set("cachedAfter_day", cachedAfter.getDay())
        sct.set("cachedAfter_hour", cachedAfter.getHour())
        sct.set("cachedAfter_minute", cachedAfter.getMinute())
        sct.set("cachedAfter_second", cachedAfter.getSecond())
    }

    @Throws(PageException::class)
    private fun doGetCustomTagSetting() {
        val sct: Struct = StructImpl()
        pageContext.setVariable(getString("admin", action, "returnVariable"), sct)
        sct.set("customTagDeepSearch", Caster.toBoolean(config.doCustomTagDeepSearch())) // deprecated
        sct.set("customTagLocalSearch", Caster.toBoolean(config.doLocalCustomTag())) // deprecated
        sct.set("deepSearch", Caster.toBoolean(config.doCustomTagDeepSearch()))
        sct.set("localSearch", Caster.toBoolean(config.doLocalCustomTag()))
        sct.set("customTagPathCache", Caster.toBoolean(config.useCTPathCache()))
        sct.set("extensions", ArrayImpl(config.getCustomTagExtensions()))
    }

    @Throws(PageException::class)
    private fun doGetDatasourceDriverList() {
        val luceeContext: Resource = ResourceUtil.toResourceExisting(pageContext, "/lucee/admin/dbdriver/")
        val children: Array<Resource?> = luceeContext.listResources(ExtensionResourceFilter(Constants.getComponentExtensions()))
        val rtnVar = getString("admin", action, "returnVariable")
        val qry: lucee.runtime.type.Query = QueryImpl(arrayOf<String?>("name"), children.size, rtnVar)
        for (i in children.indices) {
            qry.setAt("name", i + 1, children[i].getName())
        }
        pageContext.setVariable(rtnVar, qry)
    }

    @Throws(PageException::class)
    private fun doGetDebuggingList() {
        val luceeContext: Resource = ResourceUtil.toResourceExisting(pageContext, "/lucee/templates/debugging/")
        val children: Array<Resource?> = luceeContext.listResources(ExtensionResourceFilter(Constants.getTemplateExtensions()))
        val rtnVar = getString("admin", action, "returnVariable")
        val qry: lucee.runtime.type.Query = QueryImpl(arrayOf<String?>("name"), children.size, rtnVar)
        for (i in children.indices) {
            qry.setAt("name", i + 1, children[i].getName())
        }
        pageContext.setVariable(rtnVar, qry)
    }

    @Throws(PageException::class)
    private fun doGetGatewayEntries() {
        val entries: Map = (configWeb.getGatewayEngine() as GatewayEngineImpl).getEntries()
        val it: Iterator = entries.entrySet().iterator()
        val qry: lucee.runtime.type.Query = QueryImpl(arrayOf<String?>("class", "bundleName", "bundleVersion", "id", "custom", "cfcPath", "listenerCfcPath", "startupMode", "state", "readOnly"), 0, "entries")
        var entry: Map.Entry
        var ge: GatewayEntry
        // Gateway g;
        var row = 0
        while (it.hasNext()) {
            row++
            entry = it.next() as Entry
            ge = entry.getValue() as GatewayEntry
            // g=ge.getGateway();
            qry.addRow()
            qry.setAtEL("class", row, ge.getClassDefinition().getClassName())
            qry.setAtEL("bundleName", row, ge.getClassDefinition().getName())
            qry.setAtEL("bundleVersion", row, ge.getClassDefinition().getVersionAsString())
            qry.setAtEL("id", row, ge.getId())
            qry.setAtEL("listenerCfcPath", row, ge.getListenerCfcPath())
            qry.setAtEL("cfcPath", row, ge.getCfcPath())
            qry.setAtEL("startupMode", row, GatewayEntryImpl.toStartup(ge.getStartupMode(), "automatic"))
            qry.setAtEL("custom", row, ge.getCustom())
            qry.setAtEL("readOnly", row, Caster.toBoolean(ge.isReadOnly()))
            qry.setAtEL("state", row, GatewayEngineImpl.toStringState(GatewayUtil.getState(ge), "failed"))
        }
        pageContext.setVariable(getString("admin", action, "returnVariable"), qry)
    }

    @Throws(PageException::class)
    private fun doGetBundle() {
        val symbolicName = getString("admin", "getBundle", "symbolicName", true)
        val version: Version = OSGiUtil.toVersion(getString("version", null), null)
        val bd: BundleDefinition?
        var bf: BundleFile? = null
        var b: Bundle = OSGiUtil.getBundleLoaded(symbolicName, version, null)
        if (b != null) {
            bd = BundleDefinition(b)
        } else {
            try {
                bf = OSGiUtil.getBundleFile(symbolicName, version, null, null, false)
                bd = bf.toBundleDefinition()
                b = bd.getLoadedBundle()
            } catch (e: BundleException) {
                throw Caster.toPageException(e)
            }
        }
        val engine: CFMLEngine = ConfigWebUtil.getEngine(config)
        val coreBundles: BundleCollection = engine.getBundleCollection()
        val extBundles: Collection<BundleDefinition?> = config.getAllExtensionBundleDefintions()
        val sct: Struct = StructImpl()
        pageContext.setVariable(getString("admin", action, "returnVariable"), sct)
        sct.set(SYMBOLIC_NAME, bd.getName())
        sct.set(KeyConstants._title, bd.getName())
        sct.set(KeyConstants._version, bd.getVersionAsString())
        sct.set(USED_BY, _usedBy(bd.getName(), bd.getVersion(), coreBundles, extBundles))
        try {
            if (b != null) {
                sct.set(PATH, b.getLocation())
            } else {
                if (bf == null) bf = bd.getBundleFile(false, JavaSettingsImpl.getBundleDirectories(pageContext))
                sct.set(PATH, bf.getFile())
            }
        } catch (t: Throwable) {
            ExceptionUtil.rethrowIfNecessary(t)
        }
        var headers: Map<String?, Object?>? = null
        if (b != null) {
            sct.set(KeyConstants._version, bd.getVersion().toString())
            sct.set(KeyConstants._id, b.getBundleId())
            sct.set(KeyConstants._state, OSGiUtil.toState(b.getState(), null))
            sct.set(FRAGMENT, OSGiUtil.isFragment(b))
            headers = OSGiUtil.getHeaders(b)
        } else {
            sct.set(KeyConstants._state, "notinstalled")
            try {
                if (bf == null) bf = bd.getBundleFile(false, null)
                sct.set(KeyConstants._version, bf.getVersionAsString())
                sct.set(FRAGMENT, OSGiUtil.isFragment(bf))
                headers = bf.getHeaders()
            } catch (e: BundleException) {
            }
        }
        if (headers != null) {
            val h: Struct = Caster.toStruct(headers, false)
            sct.set(HEADERS, h)

            // title
            var str: String = Caster.toString(h.get("Bundle-Title", null), null)
            if (StringUtil.isEmpty(str)) str = Caster.toString(h.get("Implementation-Title", null), null)
            if (StringUtil.isEmpty(str)) str = Caster.toString(h.get("Specification-Title", null), null)
            if (StringUtil.isEmpty(str)) str = Caster.toString(h.get("Bundle-Name", null), null)
            if (!StringUtil.isEmpty(str)) sct.set(KeyConstants._title, str)

            // description
            str = Caster.toString(h.get("Bundle-Description", null), null)
            if (StringUtil.isEmpty(str)) str = Caster.toString(h.get("Implementation-Description", null), null)
            if (StringUtil.isEmpty(str)) str = Caster.toString(h.get("Specification-Description", null), null)
            if (!StringUtil.isEmpty(str)) sct.set(KeyConstants._description, str)

            // Vendor
            str = Caster.toString(h.get("Bundle-Vendor", null), null)
            if (StringUtil.isEmpty(str)) str = Caster.toString(h.get("Implementation-Vendor", null), null)
            if (StringUtil.isEmpty(str)) str = Caster.toString(h.get("Specification-Vendor", null), null)
            if (!StringUtil.isEmpty(str)) sct.set(VENDOR, str)
        }
    }

    @Throws(PageException::class)
    private fun doGetBundles() {
        val engine: CFMLEngine = ConfigWebUtil.getEngine(config)
        val coreBundles: BundleCollection = engine.getBundleCollection()
        val extBundles: Collection<BundleDefinition?> = config.getAllExtensionBundleDefintions()
        val bds: List<BundleDefinition?> = OSGiUtil.getBundleDefinitions(engine.getBundleContext())
        val it: Iterator<BundleDefinition?> = bds.iterator()
        var bd: BundleDefinition?
        var b: Bundle
        var str: String
        val qry: Query = QueryImpl(arrayOf<Key?>(SYMBOLIC_NAME, KeyConstants._title, KeyConstants._description, KeyConstants._version, VENDOR, KeyConstants._state, PATH, USED_BY,
                KeyConstants._id, FRAGMENT, HEADERS), bds.size(), "bundles")
        var row = 0
        while (it.hasNext()) {
            row++
            bd = it.next()
            b = bd.getLoadedBundle()
            qry.setAt(SYMBOLIC_NAME, row, bd.getName())
            qry.setAt(KeyConstants._title, row, bd.getName())
            qry.setAt(KeyConstants._version, row, bd.getVersionAsString())
            qry.setAt(USED_BY, row, _usedBy(bd.getName(), bd.getVersion(), coreBundles, extBundles))
            var bf: BundleFile? = null
            try {
                if (b != null) {
                    qry.setAt(PATH, row, b.getLocation())
                } else {
                    bf = bd.getBundleFile(false, null)
                    qry.setAt(PATH, row, bf.getFile())
                }
            } catch (t: Throwable) {
                ExceptionUtil.rethrowIfNecessary(t)
            }
            var headers: Map<String?, Object?>? = null
            if (b != null) {
                qry.setAt(KeyConstants._version, row, bd.getVersion().toString())
                qry.setAt(KeyConstants._id, row, b.getBundleId())
                qry.setAt(KeyConstants._state, row, OSGiUtil.toState(b.getState(), null))
                qry.setAt(FRAGMENT, row, OSGiUtil.isFragment(b))
                headers = OSGiUtil.getHeaders(b)
            } else {
                qry.setAt(KeyConstants._state, row, "notinstalled")
                try {
                    if (b != null) {
                        qry.setAt(KeyConstants._version, row, b.getVersion().toString())
                        qry.setAt(FRAGMENT, row, OSGiUtil.isFragment(b))
                        val dic: Dictionary<String?, String?> = b.getHeaders()
                        val keys: Enumeration<String?> = dic.keys()
                        headers = HashMap<String?, Object?>()
                        var key: String
                        while (keys.hasMoreElements()) {
                            key = keys.nextElement()
                            headers.put(key, dic.get(key))
                        }
                    } else {
                        if (bf != null) bf = bd.getBundleFile(false, null)
                        qry.setAt(KeyConstants._version, row, bf.getVersionAsString())
                        // qry.setAt(KeyConstants._id, row, bf.getBundleId());
                        qry.setAt(FRAGMENT, row, OSGiUtil.isFragment(bf))
                        headers = bf.getHeaders()
                    }
                } catch (e: BundleException) {
                }
            }
            if (headers != null) {
                val h: Struct = Caster.toStruct(headers, false)
                qry.setAt(HEADERS, row, h)

                // title
                str = Caster.toString(h.get("Bundle-Title", null), null)
                if (StringUtil.isEmpty(str)) str = Caster.toString(h.get("Implementation-Title", null), null)
                if (StringUtil.isEmpty(str)) str = Caster.toString(h.get("Specification-Title", null), null)
                if (StringUtil.isEmpty(str)) str = Caster.toString(h.get("Bundle-Name", null), null)
                if (!StringUtil.isEmpty(str)) qry.setAt(KeyConstants._title, row, str)

                // description
                str = Caster.toString(h.get("Bundle-Description", null), null)
                if (StringUtil.isEmpty(str)) str = Caster.toString(h.get("Implementation-Description", null), null)
                if (StringUtil.isEmpty(str)) str = Caster.toString(h.get("Specification-Description", null), null)
                if (!StringUtil.isEmpty(str)) qry.setAt(KeyConstants._description, row, str)

                // Vendor
                str = Caster.toString(h.get("Bundle-Vendor", null), null)
                if (StringUtil.isEmpty(str)) str = Caster.toString(h.get("Implementation-Vendor", null), null)
                if (StringUtil.isEmpty(str)) str = Caster.toString(h.get("Specification-Vendor", null), null)
                if (!StringUtil.isEmpty(str)) qry.setAt(VENDOR, row, str)

                // Specification-Vendor,Bundle-Vendor
            }
        }
        QuerySort.call(pageContext, qry, "title")
        pageContext.setVariable(getString("admin", action, "returnVariable"), qry)
    }

    private fun _usedBy(name: String?, version: Version?, coreBundles: BundleCollection?, extBundles: Collection<BundleDefinition?>?): String? {
        val extensions: Set<String?> = HashSet<String?>()

        // core
        if (_eq(name, version, coreBundles.core.getSymbolicName(), coreBundles.core.getVersion())) {
            extensions.add("Lucee")
            // return "Lucee";
        }
        val it: Iterator<Bundle?> = coreBundles.getSlaves()
        var b: Bundle?
        while (it.hasNext()) {
            b = it.next()
            if (_eq(name, version, b.getSymbolicName(), b.getVersion())) {
                extensions.add("Lucee")
                // return "Lucee";
                break
            }
        }
        val itt: Iterator<BundleDefinition?> = extBundles!!.iterator()
        var bd: BundleDefinition?
        while (itt.hasNext()) {
            bd = itt.next()
            if (_eq(name, version, bd.getName(), bd.getVersion())) {
                findExtension(extensions, bd)
            }
        }
        if (extensions.size() === 0) return ""
        return if (extensions.size() === 1) extensions.iterator().next() else ListUtil.arrayToList(extensions.toArray(arrayOfNulls<String?>(extensions.size())), ", ")
    }

    private fun findExtension(extensions: Set<String?>?, bd: BundleDefinition?) {
        val ci: ConfigPro? = config
        _findExtension(ci.getRHExtensions(), bd, extensions)
        _findExtension(ci.getServerRHExtensions(), bd, extensions)
    }

    private fun _findExtension(extensions: Array<RHExtension?>?, bd: BundleDefinition?, set: Set?) {
        var bundles: Array<BundleInfo?>
        for (e in extensions!!) {
            try {
                bundles = e.getBundles()
                if (bundles != null) {
                    for (b in bundles) {
                        if (_eq(bd.getName(), bd.getVersion(), b.getSymbolicName(), b.getVersion())) {
                            set.add(e.getName())
                        }
                    }
                }
            } catch (ex: Exception) {
            }
        }
    }

    private fun _eq(lName: String?, lVersion: Version?, rName: String?, rVersion: Version?): Boolean {
        if (!lName!!.equals(rName)) return false
        return if (lVersion == null) rVersion == null else lVersion.equals(rVersion)
    }

    @Throws(PageException::class)
    private fun doGetMonitors() {
        if (config !is ConfigServerImpl) throw ApplicationException("invalid context for this action")
        val cs: ConfigServerImpl? = config as ConfigServerImpl?
        val intervalls: Array<IntervallMonitor?> = cs.getIntervallMonitors()
        val requests: Array<RequestMonitor?> = cs.getRequestMonitors()
        val qry: lucee.runtime.type.Query = QueryImpl(arrayOf<Collection.Key?>(KeyConstants._name, KeyConstants._type, LOG_ENABLED, CLASS), 0, "monitors")
        doGetMonitors(qry, intervalls)
        doGetMonitors(qry, requests)
        pageContext.setVariable(getString("admin", action, "returnVariable"), qry)
    }

    @Throws(PageException::class)
    private fun doGetMonitor() {
        if (config !is ConfigServerImpl) throw ApplicationException("invalid context for this action")
        val cs: ConfigServerImpl? = config as ConfigServerImpl?
        var type = getString("admin", action, "monitorType")
        val name = getString("admin", action, "name")
        type = type.trim()
        val m: Monitor
        m = if ("request".equalsIgnoreCase(type)) cs.getRequestMonitor(name) else cs.getIntervallMonitor(name)
        val sct: Struct = StructImpl()
        sct.setEL(KeyConstants._name, m.getName())
        sct.setEL(KeyConstants._type, if (m.getType() === Monitor.TYPE_INTERVAL) "intervall" else "request")
        sct.setEL(LOG_ENABLED, m.isLogEnabled())
        sct.setEL(CLASS, m.getClazz().getName())
        pageContext.setVariable(getString("admin", action, "returnVariable"), sct)
    }

    @Throws(PageException::class)
    private fun doGetExecutionLog() {
        val factory: ExecutionLogFactory = config.getExecutionLogFactory()
        val sct: Struct = StructImpl()
        sct.set(KeyConstants._enabled, Caster.toBoolean(config.getExecutionLogEnabled()))
        val clazz: Class = factory.getClazz()
        sct.set(KeyConstants._class, if (clazz != null) clazz.getName() else "")
        sct.set(KeyConstants._arguments, factory.getArgumentsAsStruct())
        pageContext.setVariable(getString("admin", action, "returnVariable"), sct)
    }

    private fun doGetMonitors(qry: Query?, monitors: Array<Monitor?>?) {
        var m: Monitor?
        var row: Int
        for (i in monitors.indices) {
            m = monitors!![i]
            row = qry.addRow()
            qry.setAtEL(KeyConstants._name, row, m.getName())
            qry.setAtEL(KeyConstants._type, row, if (m.getType() === Monitor.TYPE_INTERVAL) "intervall" else "request")
            qry.setAtEL(LOG_ENABLED, row, m.isLogEnabled())
            qry.setAtEL(CLASS, row, m.getClazz().getName())
        }
    }

    @Throws(PageException::class)
    private fun doGetGatewayEntry() {
        val id = getString("admin", action, "id")
        val entries: Map = (configWeb.getGatewayEngine() as GatewayEngineImpl).getEntries()
        val it: Iterator = entries.keySet().iterator()
        val ge: GatewayEntry
        // Gateway g;
        val sct: Struct?
        while (it.hasNext()) {
            val key = it.next() as String
            if (key.equalsIgnoreCase(id)) {
                ge = entries.get(key) as GatewayEntry
                // g=ge.getGateway();
                sct = StructImpl()
                sct.setEL(KeyConstants._id, ge.getId())
                sct.setEL(KeyConstants._class, ge.getClassDefinition().getClassName())
                sct.setEL(KeyConstants._bundleName, ge.getClassDefinition().getName())
                sct.setEL(KeyConstants._bundleVersion, ge.getClassDefinition().getVersionAsString())
                sct.setEL(KeyConstants._listenerCfcPath, ge.getListenerCfcPath())
                sct.setEL(KeyConstants._cfcPath, ge.getCfcPath())
                sct.setEL(KeyConstants._startupMode, GatewayEntryImpl.toStartup(ge.getStartupMode(), "automatic"))
                sct.setEL(KeyConstants._custom, ge.getCustom())
                sct.setEL(KeyConstants._readOnly, Caster.toBoolean(ge.isReadOnly()))
                sct.setEL(KeyConstants._state, GatewayEngineImpl.toStringState(GatewayUtil.getState(ge), "failed"))
                pageContext.setVariable(getString("admin", action, "returnVariable"), sct)
                return
            }
        }
        throw ApplicationException("No Gateway entry found with id [$id]")
    }

    @Throws(PageException::class)
    private fun doGateway() {
        val id = getString("admin", action, "id")
        val act: String = getString("admin", action, "gatewayAction").trim().toLowerCase()
        val eng: GatewayEngineImpl = configWeb.getGatewayEngine() as GatewayEngineImpl
        if ("restart".equals(act)) eng.restart(id) else if ("start".equals(act)) eng.start(id) else if ("stop".equals(act)) eng.stop(id) else throw ApplicationException("Invalid gateway action [$act], valid actions are [start,stop,restart]")
    }

    @Throws(PageException::class)
    private fun doGetCacheConnections() {
        val conns: Map = config.getCacheConnections()
        val it: Iterator = conns.entrySet().iterator()
        val qry: lucee.runtime.type.Query = QueryImpl(arrayOf<String?>("class", "bundleName", "bundleVersion", "name", "custom", "default", "readOnly", "storage"), 0, "connections")
        var entry: Map.Entry
        var cc: CacheConnection
        val defObj: CacheConnection = config.getCacheDefaultConnection(ConfigPro.CACHE_TYPE_OBJECT)
        val defTmp: CacheConnection = config.getCacheDefaultConnection(ConfigPro.CACHE_TYPE_TEMPLATE)
        val defQry: CacheConnection = config.getCacheDefaultConnection(ConfigPro.CACHE_TYPE_QUERY)
        val defRes: CacheConnection = config.getCacheDefaultConnection(ConfigPro.CACHE_TYPE_RESOURCE)
        val defUDF: CacheConnection = config.getCacheDefaultConnection(ConfigPro.CACHE_TYPE_FUNCTION)
        val defInc: CacheConnection = config.getCacheDefaultConnection(ConfigPro.CACHE_TYPE_INCLUDE)
        val defHTT: CacheConnection = config.getCacheDefaultConnection(ConfigPro.CACHE_TYPE_HTTP)
        val defFil: CacheConnection = config.getCacheDefaultConnection(ConfigPro.CACHE_TYPE_FILE)
        val defWSe: CacheConnection = config.getCacheDefaultConnection(ConfigPro.CACHE_TYPE_WEBSERVICE)
        var row = 0
        var def: String
        while (it.hasNext()) {
            row++
            entry = it.next() as Entry
            cc = entry.getValue() as CacheConnection
            qry.addRow()
            def = ""
            if (cc === defObj) def = "object"
            if (cc === defTmp) def = "template"
            if (cc === defQry) def = "query"
            if (cc === defRes) def = "resource"
            if (cc === defUDF) def = "function"
            if (cc === defInc) def = "include"
            if (cc === defHTT) def = "http"
            if (cc === defFil) def = "file"
            if (cc === defWSe) def = "webservice"
            qry.setAtEL(KeyConstants._class, row, cc.getClassDefinition().getClassName())
            qry.setAtEL(KeyConstants._bundleName, row, cc.getClassDefinition().getName())
            qry.setAtEL(KeyConstants._bundleVersion, row, cc.getClassDefinition().getVersionAsString())
            qry.setAtEL(KeyConstants._name, row, cc.getName())
            qry.setAtEL(KeyConstants._custom, row, cc.getCustom())
            qry.setAtEL(KeyConstants._default, row, def)
            qry.setAtEL(KeyConstants._readonly, row, Caster.toBoolean(cc.isReadOnly()))
            qry.setAtEL(KeyConstants._storage, row, Caster.toBoolean(cc.isStorage()))
        }
        pageContext.setVariable(getString("admin", action, "returnVariable"), qry)
    }

    @Throws(PageException::class)
    private fun doGetCacheDefaultConnection() {
        val type: Int
        var strType = getString("admin", "GetCacheDefaultConnection", "cacheType")
        strType = strType.toLowerCase().trim()
        type = if (strType.equals("object")) ConfigPro.CACHE_TYPE_OBJECT else if (strType.equals("template")) ConfigPro.CACHE_TYPE_TEMPLATE else if (strType.equals("query")) ConfigPro.CACHE_TYPE_QUERY else if (strType.equals("resource")) ConfigPro.CACHE_TYPE_RESOURCE else if (strType.equals("function")) ConfigPro.CACHE_TYPE_FUNCTION else if (strType.equals("include")) ConfigPro.CACHE_TYPE_INCLUDE else if (strType.equals("http")) ConfigPro.CACHE_TYPE_HTTP else if (strType.equals("file")) ConfigPro.CACHE_TYPE_FILE else if (strType.equals("webservice")) ConfigPro.CACHE_TYPE_WEBSERVICE else throw ApplicationException("inv,query,resource invalid type definition, valid values are [object,template,query,resource,function,include]")
        val cc: CacheConnection = config.getCacheDefaultConnection(type)
        if (cc != null) {
            val sct: Struct = StructImpl()
            sct.setEL(KeyConstants._name, cc.getName())
            sct.setEL(KeyConstants._class, cc.getClassDefinition().getClassName())
            sct.setEL(KeyConstants._bundleName, cc.getClassDefinition().getName())
            sct.setEL(KeyConstants._bundleVersion, cc.getClassDefinition().getVersionAsString())
            sct.setEL(KeyConstants._custom, cc.getCustom())
            sct.setEL(KeyConstants._default, Caster.toBoolean(true))
            sct.setEL(KeyConstants._readonly, Caster.toBoolean(cc.isReadOnly()))
            pageContext.setVariable(getString("admin", action, "returnVariable"), sct)
        } else throw ApplicationException("There is no cache default connection")
    }

    @Throws(PageException::class)
    private fun doGetCacheConnection() {
        val name = getString("admin", action, "name")
        val conns: Map = config.getCacheConnections()
        val it: Iterator = conns.keySet().iterator()
        val cc: CacheConnection
        val dObj: CacheConnection = config.getCacheDefaultConnection(ConfigPro.CACHE_TYPE_OBJECT)
        val dTmp: CacheConnection = config.getCacheDefaultConnection(ConfigPro.CACHE_TYPE_TEMPLATE)
        val dQry: CacheConnection = config.getCacheDefaultConnection(ConfigPro.CACHE_TYPE_QUERY)
        val dRes: CacheConnection = config.getCacheDefaultConnection(ConfigPro.CACHE_TYPE_RESOURCE)
        val dUDF: CacheConnection = config.getCacheDefaultConnection(ConfigPro.CACHE_TYPE_FUNCTION)
        val dInc: CacheConnection = config.getCacheDefaultConnection(ConfigPro.CACHE_TYPE_INCLUDE)
        val dHTT: CacheConnection = config.getCacheDefaultConnection(ConfigPro.CACHE_TYPE_HTTP)
        val dFil: CacheConnection = config.getCacheDefaultConnection(ConfigPro.CACHE_TYPE_FILE)
        val dWSe: CacheConnection = config.getCacheDefaultConnection(ConfigPro.CACHE_TYPE_WEBSERVICE)
        val sct: Struct?
        var d: String
        while (it.hasNext()) {
            val key = it.next() as String
            if (key.equalsIgnoreCase(name)) {
                cc = conns.get(key) as CacheConnection
                sct = StructImpl()
                d = ""
                if (cc === dObj) d = "object" else if (cc === dTmp) d = "template" else if (cc === dQry) d = "query" else if (cc === dRes) d = "resource" else if (cc === dUDF) d = "function" else if (cc === dInc) d = "include" else if (cc === dHTT) d = "http" else if (cc === dFil) d = "file" else if (cc === dWSe) d = "webservice"
                sct.setEL(KeyConstants._name, cc.getName())
                sct.setEL(KeyConstants._class, cc.getClassDefinition().getClassName())
                sct.setEL(KeyConstants._bundleName, cc.getClassDefinition().getName())
                sct.setEL(KeyConstants._bundleVersion, cc.getClassDefinition().getVersionAsString())
                sct.setEL(KeyConstants._custom, cc.getCustom())
                sct.setEL(KeyConstants._default, d)
                sct.setEL(KeyConstants._readOnly, Caster.toBoolean(cc.isReadOnly()))
                sct.setEL(KeyConstants._storage, Caster.toBoolean(cc.isStorage()))
                pageContext.setVariable(getString("admin", action, "returnVariable"), sct)
                return
            }
        }
        throw ApplicationException("There is no cache connection with name [$name]")
    }

    @Throws(PageException::class)
    private fun doRemoveCacheConnection() {
        admin.removeCacheConnection(getString("admin", action, "name"))
        store()
        adminSync.broadcast(attributes, config)
    }

    @Throws(PageException::class)
    private fun doRemoveGatewayEntry() {
        admin.removeGatewayEntry(getString("admin", action, "id"))
        store()
        adminSync.broadcast(attributes, config)
    }

    @Throws(PageException::class)
    private fun doRemoveDebugEntry() {
        admin.removeDebugEntry(getString("admin", action, "id"))
        store()
        adminSync.broadcast(attributes, config)
    }

    @Throws(PageException::class)
    private fun doVerifyCacheConnection() {
        try {
            val cache: Cache = CacheUtil.getCache(pageContext, getString("admin", action, "name"))
            if (cache is CachePro) (cache as CachePro).verify() else cache.getCustomInfo()
        } catch (e: IOException) {
            throw Caster.toPageException(e)
        }
    }

    /**
     * @throws PageException
     */
    @Throws(PageException::class)
    private fun doGetDatasource() {
        val name = getString("admin", action, "name")
        val ds: Map = config.getDataSourcesAsMap()
        val it: Iterator = ds.keySet().iterator()
        while (it.hasNext()) {
            val key = it.next() as String
            if (key.equalsIgnoreCase(name)) {
                val d: DataSource = ds.get(key) as DataSource
                val sct: Struct = StructImpl()
                val cd: ClassDefinition = d.getClassDefinition()
                sct.setEL(KeyConstants._name, key)
                sct.setEL(KeyConstants._host, d.getHost())
                sct.setEL(KeyConstants._classname, cd.getClassName())
                sct.setEL(KeyConstants._class, cd.getClassName())
                sct.setEL(KeyConstants._bundleName, cd.getName())
                sct.setEL(KeyConstants._bundleVersion, cd.getVersionAsString())
                sct.setEL(KeyConstants._dsn, d.getDsnOriginal())
                sct.setEL(KeyConstants._database, d.getDatabase())
                sct.setEL(KeyConstants._port, if (d.getPort() < 1) "" else Caster.toString(d.getPort()))
                sct.setEL(KeyConstants._dsnTranslated, d.getDsnTranslated())
                sct.setEL(KeyConstants._timezone, toStringTimeZone(d.getTimeZone()))
                sct.setEL(KeyConstants._password, d.getPassword())
                sct.setEL(KeyConstants._passwordEncrypted, ConfigWebUtil.encrypt(d.getPassword()))
                sct.setEL(KeyConstants._username, d.getUsername())
                sct.setEL(KeyConstants._readonly, Caster.toBoolean(d.isReadOnly()))
                sct.setEL(KeyConstants._select, Boolean.valueOf(d.hasAllow(DataSource.ALLOW_SELECT)))
                sct.setEL(KeyConstants._delete, Boolean.valueOf(d.hasAllow(DataSource.ALLOW_DELETE)))
                sct.setEL(KeyConstants._update, Boolean.valueOf(d.hasAllow(DataSource.ALLOW_UPDATE)))
                sct.setEL(KeyConstants._insert, Boolean.valueOf(d.hasAllow(DataSource.ALLOW_INSERT)))
                sct.setEL(KeyConstants._create, Boolean.valueOf(d.hasAllow(DataSource.ALLOW_CREATE)))
                sct.setEL(KeyConstants._insert, Boolean.valueOf(d.hasAllow(DataSource.ALLOW_INSERT)))
                sct.setEL(KeyConstants._drop, Boolean.valueOf(d.hasAllow(DataSource.ALLOW_DROP)))
                sct.setEL(KeyConstants._grant, Boolean.valueOf(d.hasAllow(DataSource.ALLOW_GRANT)))
                sct.setEL(KeyConstants._revoke, Boolean.valueOf(d.hasAllow(DataSource.ALLOW_REVOKE)))
                sct.setEL(KeyConstants._alter, Boolean.valueOf(d.hasAllow(DataSource.ALLOW_ALTER)))
                sct.setEL("connectionLimit", if (d.getConnectionLimit() < 1) "-1" else Caster.toString(d.getConnectionLimit()))
                sct.setEL("connectionTimeout", if (d.getConnectionTimeout() < 1) "" else Caster.toString(d.getConnectionTimeout()))
                sct.setEL("metaCacheTimeout", Caster.toDouble(d.getMetaCacheTimeout()))
                sct.setEL("custom", d.getCustoms())
                sct.setEL("blob", Boolean.valueOf(d.isBlob()))
                sct.setEL("clob", Boolean.valueOf(d.isClob()))
                sct.setEL("validate", Boolean.valueOf(d.validate()))
                sct.setEL("storage", Boolean.valueOf(d.isStorage()))
                if (d is DataSourcePro) {
                    val dp: DataSourcePro = d as DataSourcePro
                    sct.setEL("requestExclusive", Boolean.valueOf(dp.isRequestExclusive()))
                    sct.setEL("alwaysResetConnections", Boolean.valueOf(dp.isAlwaysResetConnections()))
                    sct.setEL("liveTimeout", if (dp.getLiveTimeout() < 1) "" else Caster.toString(dp.getLiveTimeout()))
                }
                if (d is DataSourceImpl) {
                    val di: DataSourceImpl = d as DataSourceImpl
                    sct.setEL("literalTimestampWithTSOffset", Boolean.valueOf(di.getLiteralTimestampWithTSOffset()))
                    sct.setEL("alwaysSetTimeout", Boolean.valueOf(di.getAlwaysSetTimeout()))
                    sct.setEL("dbdriver", Caster.toString(di.getDbDriver(), ""))
                }
                pageContext.setVariable(getString("admin", action, "returnVariable"), sct)
                return
            }
        }
        throw ApplicationException("There is no datasource with name [$name]")
    }

    private fun toStringTimeZone(timeZone: TimeZone?): Object? {
        return if (timeZone == null) "" else timeZone.getID()
    }

    @Throws(PageException::class)
    private fun doGetRemoteClient() {
        val url = getString("admin", action, "url")
        val clients: Array<RemoteClient?> = config.getRemoteClients()
        var client: RemoteClient?
        for (i in clients.indices) {
            client = clients[i]
            if (client.getUrl().equalsIgnoreCase(url)) {
                val sct: Struct = StructImpl()
                val pd: ProxyData = client.getProxyData()
                sct.setEL("label", client.getLabel())
                sct.setEL("usage", client.getUsage())
                sct.setEL("securityKey", client.getSecurityKey())
                sct.setEL("adminPassword", client.getAdminPassword())
                sct.setEL("ServerUsername", client.getServerUsername())
                sct.setEL("ServerPassword", client.getServerPassword())
                sct.setEL("type", client.getType())
                sct.setEL("url", client.getUrl())
                sct.setEL("proxyServer", if (pd == null) "" else StringUtil.emptyIfNull(pd.getServer()))
                sct.setEL("proxyUsername", if (pd == null) "" else StringUtil.emptyIfNull(pd.getUsername()))
                sct.setEL("proxyPassword", if (pd == null) "" else StringUtil.emptyIfNull(pd.getPassword()))
                sct.setEL("proxyPort", if (pd == null) "" else if (pd.getPort() === -1) "" else Caster.toString(pd.getPort()))
                pageContext.setVariable(getString("admin", action, "returnVariable"), sct)
                return
            }
        }
        throw ApplicationException("No remote client found with url [$url]")
    }

    @Throws(PageException::class)
    private fun doGetSpoolerTasks() {
        var startrow = getInt("startrow", 1)
        if (startrow < 1) startrow = 1
        val maxrow = getInt("maxrow", -1)
        val result = getString("result", null)
        val engine: SpoolerEngineImpl = config.getSpoolerEngine() as SpoolerEngineImpl
        val qry: Query = engine.getAllTasksAsQuery(startrow, maxrow)
        pageContext.setVariable(getString("admin", action, "returnVariable"), qry)
        if (!StringUtil.isEmpty(result)) {
            val sct: Struct = StructImpl()
            pageContext.setVariable(result, sct)
            sct.setEL("open", engine.getOpenTaskCount())
            sct.setEL("closed", engine.getClosedTaskCount())
        }
    }

    private fun doGetRemoteClientTasks(qry: lucee.runtime.type.Query?, tasks: Array<SpoolerTask?>?, row: Int): Int {
        var row = row
        var task: SpoolerTask?
        for (i in tasks.indices) {
            row++
            task = tasks!![i]
            try {
                qry.setAt("type", row, task.getType())
                qry.setAt("name", row, task.subject())
                qry.setAt("detail", row, task.detail())
                qry.setAt("id", row, task.getId())
                qry.setAt("lastExecution", row, DateTimeImpl(pageContext, task.lastExecution(), true))
                qry.setAt("nextExecution", row, DateTimeImpl(pageContext, task.nextExecution(), true))
                qry.setAt("closed", row, Caster.toBoolean(task.closed()))
                qry.setAt("tries", row, Caster.toDouble(task.tries()))
                qry.setAt("triesmax", row, Caster.toDouble(task.tries()))
                qry.setAt("exceptions", row, translateTime(task.getExceptions()))
                var triesMax = 0
                val plans: Array<ExecutionPlan?> = task.getPlans()
                for (y in plans.indices) {
                    triesMax += plans[y].getTries()
                }
                qry.setAt("triesmax", row, Caster.toDouble(triesMax))
            } catch (t: Throwable) {
                ExceptionUtil.rethrowIfNecessary(t)
            }
        }
        return row
    }

    private fun translateTime(exp: Array?): Array? {
        var exp: Array? = exp
        exp = Duplicator.duplicate(exp, true)
        val it: Iterator<Object?> = exp.valueIterator()
        var sct: Struct?
        while (it.hasNext()) {
            sct = it.next() as Struct?
            sct.setEL("time", DateTimeImpl(pageContext, Caster.toLongValue(sct.get("time", null), 0), true))
        }
        return exp
    }

    @Throws(PageException::class)
    private fun doGetRemoteClients() {
        val clients: Array<RemoteClient?> = config.getRemoteClients()
        var client: RemoteClient?
        var pd: ProxyData
        val qry: lucee.runtime.type.Query = QueryImpl(arrayOf<String?>("label", "usage", "securityKey", "adminPassword", "serverUsername", "serverPassword", "type", "url",
                "proxyServer", "proxyUsername", "proxyPassword", "proxyPort"), clients.size, "query")
        var row = 0
        for (i in clients.indices) {
            client = clients[i]
            pd = client.getProxyData()
            row = i + 1
            qry.setAt("label", row, client.getLabel())
            qry.setAt("usage", row, client.getUsage())
            qry.setAt("securityKey", row, client.getSecurityKey())
            qry.setAt("adminPassword", row, client.getAdminPassword())
            qry.setAt("ServerUsername", row, client.getServerUsername())
            qry.setAt("ServerPassword", row, client.getServerPassword())
            qry.setAt("type", row, client.getType())
            qry.setAt("url", row, client.getUrl())
            qry.setAt("proxyServer", row, if (pd == null) "" else pd.getServer())
            qry.setAt("proxyUsername", row, if (pd == null) "" else pd.getUsername())
            qry.setAt("proxyPassword", row, if (pd == null) "" else pd.getPassword())
            qry.setAt("proxyPort", row, if (pd == null) "" else Caster.toString(pd.getPort()))
        }
        pageContext.setVariable(getString("admin", action, "returnVariable"), qry)
    }

    private fun doSetCluster() { // MUST remove this
        try {
            _doSetCluster()
        } catch (t: Throwable) {
            ExceptionUtil.rethrowIfNecessary(t)
        }
    }

    @Throws(PageException::class)
    private fun _doSetCluster() {
        val entries: Struct = Caster.toStruct(getObject("admin", action, "entries"))
        var entry: Struct
        val it: Iterator<Object?> = entries.valueIterator()
        val cluster: Cluster = pageContext.clusterScope()
        while (it.hasNext()) {
            entry = Caster.toStruct(it.next())
            cluster.setEntry(ClusterEntryImpl(KeyImpl.getInstance(Caster.toString(entry.get(KeyConstants._key))),
                    Caster.toSerializable(entry.get(KeyConstants._value, null), null), Caster.toLongValue(entry.get(KeyConstants._time))))
        }
        cluster.broadcast()
    }

    @Throws(PageException::class)
    private fun doGetCluster() {
        pageContext.setVariable(getString("admin", action, "returnVariable"), (pageContext as PageContextImpl?).clusterScope(false))
    }

    @Throws(PageException::class)
    private fun doGetToken() {
        pageContext.setVariable(getString("admin", action, "returnVariable"), config.getIdentification().getSecurityToken())
    }

    @Throws(PageException::class)
    private fun doGetJDBCDrivers() {
        val drivers: Array<JDBCDriver?> = config.getJDBCDrivers()
        val qry: lucee.runtime.type.Query = QueryImpl(arrayOf<Key?>(KeyConstants._id, KeyConstants._label, KeyConstants._class, KeyConstants._bundleName, KeyConstants._bundleVersion, KeyConstants._connectionString),
                drivers.size, "jdbc")
        var driver: JDBCDriver?
        var row: Int
        for (i in drivers.indices) {
            row = i + 1
            driver = drivers[i]
            if (!StringUtil.isEmpty(driver.id)) qry.setAt(KeyConstants._id, row, driver.id)
            if (!StringUtil.isEmpty(driver.connStr)) qry.setAt(KeyConstants._connectionString, row, driver.connStr)
            qry.setAt(KeyConstants._label, row, driver.label)
            qry.setAt(KeyConstants._class, row, driver.cd.getClassName())
            qry.setAt(KeyConstants._bundleName, row, driver.cd.getName())
            qry.setAt(KeyConstants._bundleVersion, row, driver.cd.getVersion().toString())
        }
        pageContext.setVariable(getString("admin", action, "returnVariable"), qry)
    }

    /**
     * @throws PageException
     */
    @Throws(PageException::class)
    private fun doGetDatasources() {
        val ds: Map = config.getDataSourcesAsMap()
        val it: Iterator = ds.keySet().iterator()
        val qry: lucee.runtime.type.Query = QueryImpl(arrayOf<String?>("name", "host", "classname", "bundleName", "bundleVersion", "dsn", "DsnTranslated", "database", "port",
                "timezone", "username", "password", "passwordEncrypted", "readonly", "grant", "drop", "create", "revoke", "alter", "select", "delete", "update", "insert",
                "connectionLimit", "openConnections", "idleConnections", "activeConnections", "waitingForConnection", "connectionTimeout", "clob", "blob", "validate", "storage",
                "customSettings", "metaCacheTimeout"), ds.size(), "query")
        var row = 0
        while (it.hasNext()) {
            val key: Object = it.next()
            val d: DataSource = ds.get(key) as DataSource
            row++
            qry.setAt(KeyConstants._name, row, key)
            qry.setAt(KeyConstants._host, row, d.getHost())
            qry.setAt("classname", row, d.getClassDefinition().getClassName())
            qry.setAt("bundleName", row, d.getClassDefinition().getName())
            qry.setAt("bundleVersion", row, d.getClassDefinition().getVersionAsString())
            qry.setAt("dsn", row, d.getDsnOriginal())
            qry.setAt("database", row, d.getDatabase())
            qry.setAt(KeyConstants._port, row, if (d.getPort() < 1) "" else Caster.toString(d.getPort()))
            qry.setAt("dsnTranslated", row, d.getDsnTranslated())
            qry.setAt("timezone", row, toStringTimeZone(d.getTimeZone()))
            qry.setAt(KeyConstants._password, row, d.getPassword())
            qry.setAt("passwordEncrypted", row, ConfigWebUtil.encrypt(d.getPassword()))
            qry.setAt(KeyConstants._username, row, d.getUsername())
            qry.setAt(KeyConstants._readonly, row, Caster.toBoolean(d.isReadOnly()))
            qry.setAt(KeyConstants._select, row, Boolean.valueOf(d.hasAllow(DataSource.ALLOW_SELECT)))
            qry.setAt(KeyConstants._delete, row, Boolean.valueOf(d.hasAllow(DataSource.ALLOW_DELETE)))
            qry.setAt(KeyConstants._update, row, Boolean.valueOf(d.hasAllow(DataSource.ALLOW_UPDATE)))
            qry.setAt(KeyConstants._create, row, Boolean.valueOf(d.hasAllow(DataSource.ALLOW_CREATE)))
            qry.setAt(KeyConstants._insert, row, Boolean.valueOf(d.hasAllow(DataSource.ALLOW_INSERT)))
            qry.setAt(KeyConstants._drop, row, Boolean.valueOf(d.hasAllow(DataSource.ALLOW_DROP)))
            qry.setAt(KeyConstants._grant, row, Boolean.valueOf(d.hasAllow(DataSource.ALLOW_GRANT)))
            qry.setAt(KeyConstants._revoke, row, Boolean.valueOf(d.hasAllow(DataSource.ALLOW_REVOKE)))
            qry.setAt(KeyConstants._alter, row, Boolean.valueOf(d.hasAllow(DataSource.ALLOW_ALTER)))

            // open connections
            var idle = 0
            var active = 0
            var waiters = 0
            for (pool in config.getDatasourceConnectionPools()) {
                if (!d.getName().equalsIgnoreCase(pool.getFactory().getDatasource().getName())) continue
                idle += pool.getNumIdle()
                active += pool.getNumActive()
                waiters += pool.getNumWaiters()
            }
            qry.setAt("openConnections", row, idle + active)
            qry.setAt("idleConnections", row, idle)
            qry.setAt("activeConnections", row, active)
            qry.setAt("waitingForConnection", row, waiters)
            qry.setAt("connectionLimit", row, if (d.getConnectionLimit() < 1) "" else Caster.toString(d.getConnectionLimit()))
            qry.setAt("connectionTimeout", row, if (d.getConnectionTimeout() < 1) "" else Caster.toString(d.getConnectionTimeout()))
            qry.setAt("customSettings", row, d.getCustoms())
            qry.setAt("blob", row, Boolean.valueOf(d.isBlob()))
            qry.setAt("clob", row, Boolean.valueOf(d.isClob()))
            qry.setAt("validate", row, Boolean.valueOf(d.validate()))
            qry.setAt("storage", row, Boolean.valueOf(d.isStorage()))
            qry.setAt("metaCacheTimeout", row, Caster.toDouble(d.getMetaCacheTimeout()))
        }
        pageContext.setVariable(getString("admin", action, "returnVariable"), qry)
    }

    /**
     * @throws PageException
     */
    @Throws(PageException::class)
    private fun doUpdateScope() {
        admin.updateScopeCascadingType(getString("admin", action, "scopeCascadingType"))
        admin.updateAllowImplicidQueryCall(getBoolObject("admin", action, "allowImplicidQueryCall"))
        admin.updateMergeFormAndUrl(getBoolObject("admin", action, "mergeFormAndUrl"))
        admin.updateSessionManagement(getBoolObject("admin", action, "sessionManagement"))
        admin.updateClientManagement(getBoolObject("admin", action, "clientManagement"))
        admin.updateDomaincookies(getBoolObject("admin", action, "domainCookies"))
        admin.updateClientCookies(getBoolObject("admin", action, "clientCookies"))
        // admin.updateRequestTimeout(getTimespan("admin",action,"requestTimeout"));
        admin.updateClientTimeout(getTimespan("admin", action, "clientTimeout"))
        admin.updateSessionTimeout(getTimespan("admin", action, "sessionTimeout"))
        admin.updateClientStorage(getString("admin", action, "clientStorage"))
        admin.updateSessionStorage(getString("admin", action, "sessionStorage"))
        admin.updateApplicationTimeout(getTimespan("admin", action, "applicationTimeout"))
        admin.updateSessionType(getString("admin", action, "sessionType"))
        admin.updateLocalMode(getString("admin", action, "localMode"))
        admin.updateCGIReadonly(getBoolObject("admin", action, "cgiReadonly"))
        store()
        adminSync.broadcast(attributes, config)
    }

    /**
     * @throws PageException
     */
    @Throws(PageException::class)
    private fun doUpdateDevelopMode() {
        admin.updateMode(getBoolObject("admin", action, "mode"))
        store()
        adminSync.broadcast(attributes, config)
    }

    @Throws(PageException::class)
    private fun doUpdateRestSettings() {
        admin.updateRestList(getBool("list", null))
        // admin.updateRestAllowChanges(getBool("allowChanges", null));
        store()
        adminSync.broadcast(attributes, config)
    }

    @Throws(PageException::class)
    private fun doUpdateApplicationSettings() {
        admin.updateRequestTimeout(getTimespan("admin", action, "requestTimeout"))
        admin.updateScriptProtect(getString("admin", action, "scriptProtect"))
        admin.updateAllowURLRequestTimeout(getBoolObject("admin", action, "allowURLRequestTimeout")) // DIFF 23
        store()
        adminSync.broadcast(attributes, config)
    }

    @Throws(PageException::class)
    private fun doUpdateQueueSettings() {
        admin.updateQueue(getInteger("admin", action, "max"), getInteger("admin", action, "timeout"), getBoolObject("admin", action, "enable"))
        store()
        adminSync.broadcast(attributes, config)
    }

    @Throws(PageException::class)
    private fun doUpdateOutputSettings() {
        admin.updateCFMLWriterType(getString("admin", action, "cfmlWriter"))
        admin.updateSuppressContent(getBoolObject("admin", action, "suppressContent"))
        // admin.updateShowVersion(getBoolObject("admin",action, "showVersion"));
        admin.updateAllowCompression(getBoolObject("admin", action, "allowCompression"))
        admin.updateContentLength(getBoolObject("admin", action, "contentLength"))
        admin.updateBufferOutput(getBoolObject("admin", action, "bufferOutput"))
        store()
        adminSync.broadcast(attributes, config)
    }

    @Throws(PageException::class)
    private fun doUpdateCustomTagSetting() {
        admin.updateCustomTagDeepSearch(getBool("admin", action, "deepSearch"))
        admin.updateCustomTagLocalSearch(getBool("admin", action, "localSearch"))
        admin.updateCTPathCache(getBool("admin", action, "customTagPathCache"))
        admin.updateCustomTagExtensions(getString("admin", action, "extensions"))
        store()
        adminSync.broadcast(attributes, config)
    }

    @Throws(PageException::class)
    private fun doUpdateAdminMode() {
        admin.updateUpdateAdminMode(getString("admin", "updateAdminMode", "mode"), getBool("admin", "updateAdminMode", "merge"), getBool("admin", "updateAdminMode", "keep"))
        store()
        adminSync.broadcast(attributes, config)
    }

    @Throws(PageException::class)
    private fun doUpdateMonitor() {
        val cd: ClassDefinition = ClassDefinitionImpl(getString("admin", action, "class"), getString("bundleName", null), getString("bundleVersion", null),
                config.getIdentification())
        admin.updateMonitor(cd, getString("admin", "updateMonitor", "monitorType"), getString("admin", "updateMonitor", "name"), getBool("admin", "updateMonitor", "logEnabled"))
        store()
        adminSync.broadcast(attributes, config)
    }

    @Throws(PageException::class)
    private fun doUpdateORMEngine() {
        val cd: ClassDefinition = ClassDefinitionImpl(getString("admin", action, "class"), getString("bundleName", null), getString("bundleVersion", null),
                config.getIdentification())
        admin.updateORMEngine(cd)
        store()
        adminSync.broadcast(attributes, config)
    }

    @Throws(PageException::class)
    private fun doUpdateCacheHandler() {
        val cd: ClassDefinition = ClassDefinitionImpl(getString("admin", action, "class"), getString("bundleName", null), getString("bundleVersion", null),
                config.getIdentification())
        admin.updateCacheHandler(getString("admin", "updateCacheHandler", "id"), cd)
        store()
        adminSync.broadcast(attributes, config)
    }

    @Throws(PageException::class)
    private fun doUpdateExecutionLog() {
        val cd: lucee.runtime.db.ClassDefinition = ClassDefinitionImpl(getString("admin", action, "class"), getString("bundleName", null), getString("bundleVersion", null),
                config.getIdentification())
        admin.updateExecutionLog(cd, getStruct("admin", "updateExecutionLog", "arguments"), getBool("admin", "updateExecutionLog", "enabled"))
        store()
        adminSync.broadcast(attributes, config)
    }

    @Throws(PageException::class)
    private fun doRemoveMonitor() {
        admin.removeMonitor(getString("admin", "removeMonitor", "type"), getString("admin", "removeMonitor", "name"))
        store()
        adminSync.broadcast(attributes, config)
    }

    @Throws(PageException::class)
    private fun doRemoveCacheHandler() {
        admin.removeCacheHandler(getString("admin", "removeCacheHandler", "id"))
        store()
        adminSync.broadcast(attributes, config)
    }

    @Throws(PageException::class)
    private fun doRemoveORMEngine() {
        admin.removeORMEngine()
        store()
        adminSync.broadcast(attributes, config)
    }

    @Throws(PageException::class)
    private fun doUpdateRHExtension(throwOnError: Boolean) {

        // ID
        val id = getString("id", null)
        if (!StringUtil.isEmpty(id)) {
            val ed: ExtensionDefintion?
            val version = getString("version", null)
            if (!StringUtil.isEmpty(version, true) && !"latest".equalsIgnoreCase(version)) ed = ExtensionDefintion(id, version) else ed = RHExtension.toExtensionDefinition(id)
            DeployHandler.deployExtension(config, ed, if (config == null) null else ThreadLocalPageContext.getLog(pageContext, "application"), true, true, throwOnError)
            return
        }

        // this can be a binary that represent the extension, a string that is a path to the extension or a
        // base64 base encoded string
        var obj: Object? = getObject("admin", "UpdateRHExtensions", "source")
        if (obj is String) {
            val str = obj as String?
            // we assume that when the string is more than 5000 it is a base64 encoded binary
            if (str!!.length() > 5000) {
                obj = try {
                    Base64Encoder.decode(str, true)
                } catch (e: CoderException) {
                    val ce = CasterException(e.getMessage())
                    ce.initCause(e)
                    throw ce
                }
            }
        }

        // path
        if (obj is String) {
            val src: Resource = ResourceUtil.toResourceExisting(config, obj as String?)
            ConfigAdmin._updateRHExtension(config, src, true, true)
        } else {
            try {
                val tmp: Resource = SystemUtil.getTempFile("lex", true)
                IOUtil.copy(ByteArrayInputStream(Caster.toBinary(obj)), tmp, true)
                ConfigAdmin._updateRHExtension(config, tmp, true, true)
            } catch (ioe: IOException) {
                throw Caster.toPageException(ioe)
            }
        }
    }

    @Throws(PageException::class)
    private fun doRemoveRHExtension() {
        val id = getString("admin", "removeRHExtensions", "id")
        if (!Decision.isUUId(id)) throw ApplicationException("Invalid id [$id], id must be a UUID")
        try {
            admin.removeRHExtension(id)
        } catch (e: Exception) {
            throw Caster.toPageException(e)
        }
        store()
    }

    /*
	 * private void doUpdateExtension() throws PageException {
	 * 
	 * admin.updateExtension(pageContext, new ExtensionImpl(getStruct("config", null),
	 * getString("admin", "UpdateExtensions", "id"), getString("admin", "UpdateExtensions", "provider"),
	 * getString("admin", "UpdateExtensions", "version"),
	 * 
	 * getString("admin", "UpdateExtensions", "name"), getString("label", ""), getString("description",
	 * ""), getString("category", ""), getString("image", ""), getString("author", ""),
	 * getString("codename", ""), getString("video", ""), getString("support", ""),
	 * getString("documentation", ""), getString("forum", ""), getString("mailinglist", ""),
	 * getString("network", ""), getDateTime("created", null), getString("admin", "UpdateExtensions",
	 * "_type")));
	 * 
	 * store(); // adminSync.broadcast(attributes, config); }
	 */
    @Throws(PageException::class, MalformedURLException::class)
    private fun doUpdateExtensionProvider() {
        admin.updateExtensionProvider(getString("admin", "UpdateExtensionProvider", "url"))
        store()
    }

    @Throws(PageException::class)
    private fun doUpdateRHExtensionProvider() {
        try {
            admin.updateRHExtensionProvider(getString("admin", "UpdateRHExtensionProvider", "url"))
        } catch (e: MalformedURLException) {
            throw Caster.toPageException(e)
        }
        store()
    }

    @Throws(PageException::class)
    private fun doUpdateExtensionInfo() {
        admin.updateExtensionInfo(getBool("admin", "UpdateExtensionInfo", "enabled"))
        store()
    }

    @Throws(PageException::class)
    private fun doVerifyExtensionProvider() {
        admin.verifyExtensionProvider(getString("admin", "VerifyExtensionProvider", "url"))
    }

    @Throws(PageException::class)
    private fun doResetId() {
        admin.resetId()
        store()
    }

    @Throws(PageException::class)
    private fun doRemoveExtensionProvider() {
        admin.removeExtensionProvider(getString("admin", "RemoveExtensionProvider", "url"))
        store()
    }

    @Throws(PageException::class)
    private fun doRemoveRHExtensionProvider() {
        admin.removeRHExtensionProvider(getString("admin", "RemoveRHExtensionProvider", "url"))
        store()
    }

    /**
     * @throws PageException
     */
    @Throws(PageException::class)
    private fun doGetApplicationSetting() {
        val sct: Struct = StructImpl()
        pageContext.setVariable(getString("admin", action, "returnVariable"), sct)
        sct.set("scriptProtect", AppListenerUtil.translateScriptProtect(config.getScriptProtect()))

        // request timeout
        var ts: TimeSpan = config.getRequestTimeout()
        sct.set("requestTimeout", ts)
        sct.set("requestTimeout_day", Caster.toInteger(ts.getDay()))
        sct.set("requestTimeout_hour", Caster.toInteger(ts.getHour()))
        sct.set("requestTimeout_minute", Caster.toInteger(ts.getMinute()))
        sct.set("requestTimeout_second", Caster.toInteger(ts.getSecond()))

        // application path timeout
        ts = TimeSpanImpl.fromMillis(config.getApplicationPathCacheTimeout())
        sct.set("applicationPathTimeout", ts)
        sct.set("applicationPathTimeout_day", Caster.toInteger(ts.getDay()))
        sct.set("applicationPathTimeout_hour", Caster.toInteger(ts.getHour()))
        sct.set("applicationPathTimeout_minute", Caster.toInteger(ts.getMinute()))
        sct.set("applicationPathTimeout_second", Caster.toInteger(ts.getSecond()))

        // AllowURLRequestTimeout
        sct.set("AllowURLRequestTimeout", Caster.toBoolean(config.isAllowURLRequestTimeout()))
    }

    @Throws(PageException::class)
    private fun doGetQueueSetting() {
        val sct: Struct = StructImpl()
        pageContext.setVariable(getString("admin", action, "returnVariable"), sct)
        sct.set(KeyConstants._max, Caster.toInteger(config.getQueueMax()))
        sct.set(KeyConstants._timeout, Caster.toInteger(config.getQueueTimeout()))
        sct.set("enable", Caster.toBoolean(config.getQueueEnable()))
    }

    @Throws(PageException::class)
    private fun doGetOutputSetting() {
        val sct: Struct = StructImpl()
        pageContext.setVariable(getString("admin", action, "returnVariable"), sct)
        sct.set("suppressContent", Caster.toBoolean(config.isSuppressContent()))
        sct.set("contentLength", Caster.toBoolean(config.contentLength()))
        // sct.set("showVersion",Caster.toBoolean(config.isShowVersion()));
        sct.set("allowCompression", Caster.toBoolean(config.allowCompression()))
        val wt: Int = config.getCFMLWriterType()
        var cfmlWriter = "regular"
        if (wt == ConfigPro.CFML_WRITER_WS) cfmlWriter = "white-space" else if (wt == ConfigPro.CFML_WRITER_WS_PREF) cfmlWriter = "white-space-pref"
        sct.set("cfmlWriter", cfmlWriter)
        sct.set("bufferOutput", Caster.toBoolean(config.getBufferOutput()))
    }

    /**
     * @throws PageException
     */
    @Throws(PageException::class)
    private fun doGetScope() {
        val sessionType: String = AppListenerUtil.toSessionType(config.getSessionType(), "application")
        val localMode: String = AppListenerUtil.toLocalMode(config.getLocalMode(), "classic")
        val sct: Struct = StructImpl()
        pageContext.setVariable(getString("admin", action, "returnVariable"), sct)
        sct.set("allowImplicidQueryCall", Caster.toBoolean(config.allowImplicidQueryCall()))
        sct.set("mergeFormAndUrl", Caster.toBoolean(config.mergeFormAndURL()))
        sct.set("sessiontype", sessionType)
        sct.set("localmode", localMode)
        sct.set("sessionManagement", Caster.toBoolean(config.isSessionManagement()))
        sct.set("clientManagement", Caster.toBoolean(config.isClientManagement()))
        sct.set("domainCookies", Caster.toBoolean(config.isDomainCookies()))
        sct.set("clientCookies", Caster.toBoolean(config.isClientCookies()))
        sct.set("clientStorage", config.getClientStorage())
        sct.set("sessionStorage", config.getSessionStorage())
        sct.set("cgiReadonly", config.getCGIScopeReadonly())
        var ts: TimeSpan = config.getSessionTimeout()
        sct.set("sessionTimeout", ts)
        sct.set("sessionTimeout_day", Caster.toInteger(ts.getDay()))
        sct.set("sessionTimeout_hour", Caster.toInteger(ts.getHour()))
        sct.set("sessionTimeout_minute", Caster.toInteger(ts.getMinute()))
        sct.set("sessionTimeout_second", Caster.toInteger(ts.getSecond()))
        ts = config.getApplicationTimeout()
        sct.set("applicationTimeout", ts)
        sct.set("applicationTimeout_day", Caster.toInteger(ts.getDay()))
        sct.set("applicationTimeout_hour", Caster.toInteger(ts.getHour()))
        sct.set("applicationTimeout_minute", Caster.toInteger(ts.getMinute()))
        sct.set("applicationTimeout_second", Caster.toInteger(ts.getSecond()))
        ts = config.getClientTimeout()
        sct.set("clientTimeout", ts)
        sct.set("clientTimeout_day", Caster.toInteger(ts.getDay()))
        sct.set("clientTimeout_hour", Caster.toInteger(ts.getHour()))
        sct.set("clientTimeout_minute", Caster.toInteger(ts.getMinute()))
        sct.set("clientTimeout_second", Caster.toInteger(ts.getSecond()))

        // scope cascading type
        if (config.getScopeCascadingType() === Config.SCOPE_STRICT) sct.set("scopeCascadingType", "strict") else if (config.getScopeCascadingType() === Config.SCOPE_SMALL) sct.set("scopeCascadingType", "small") else if (config.getScopeCascadingType() === Config.SCOPE_STANDARD) sct.set("scopeCascadingType", "standard")
    }

    /**
     * @throws PageException
     */
    @Throws(PageException::class)
    private fun doGetDevelopMode() {
        val sct: Struct = StructImpl()
        pageContext.setVariable(getString("admin", action, "returnVariable"), sct)
        sct.set("developMode", Caster.toBoolean(config.isDevelopMode()))
    }

    /**
     * @throws PageException
     */
    @Throws(PageException::class)
    private fun doUpdateComponent() {
        admin.updateComponentDeepSearch(getBoolObject("admin", action, "deepSearch"))
        admin.updateBaseComponent(getString("admin", action, "baseComponentTemplateCFML"), getString("admin", action, "baseComponentTemplateLucee"))
        admin.updateComponentDumpTemplate(getString("admin", action, "componentDumpTemplate"))
        admin.updateComponentDataMemberDefaultAccess(getString("admin", action, "componentDataMemberDefaultAccess"))
        admin.updateTriggerDataMember(getBoolObject("admin", action, "triggerDataMember"))
        admin.updateComponentUseShadow(getBoolObject("admin", action, "useShadow"))
        admin.updateComponentDefaultImport(getString("admin", action, "componentDefaultImport"))
        admin.updateComponentLocalSearch(getBoolObject("admin", action, "componentLocalSearch"))
        admin.updateComponentPathCache(getBoolObject("admin", action, "componentPathCache"))
        store()
        adminSync.broadcast(attributes, config)
    }

    /**
     * @throws PageException
     * @throws PageException
     */
    @Throws(PageException::class)
    private fun doGetComponent() {
        val sct: Struct = StructImpl()
        pageContext.setVariable(getString("admin", action, "returnVariable"), sct)
        // Base Component
        try {
            val psCFML: PageSource = config.getBaseComponentPageSource(CFMLEngine.DIALECT_CFML)
            if (psCFML != null && psCFML.exists()) sct.set("baseComponentTemplateCFML", psCFML.getDisplayPath()) else sct.set("baseComponentTemplateCFML", "")
        } catch (e: PageException) {
            sct.set("baseComponentTemplateCFML", "")
        }
        try {
            val psLucee: PageSource = config.getBaseComponentPageSource(CFMLEngine.DIALECT_LUCEE)
            if (psLucee != null && psLucee.exists()) sct.set("baseComponentTemplateLucee", psLucee.getDisplayPath()) else sct.set("baseComponentTemplateLucee", "")
        } catch (e: PageException) {
            sct.set("baseComponentTemplateLucee", "")
        }
        sct.set("strBaseComponentTemplateCFML", config.getBaseComponentTemplate(CFMLEngine.DIALECT_CFML))
        sct.set("strBaseComponentTemplateLucee", config.getBaseComponentTemplate(CFMLEngine.DIALECT_LUCEE))

        // dump template
        try {
            val ps: PageSource = (pageContext as PageContextImpl?).getPageSourceExisting(config.getComponentDumpTemplate())
            if (ps != null) sct.set("componentDumpTemplate", ps.getDisplayPath()) else sct.set("componentDumpTemplate", "")
        } catch (e: PageException) {
            sct.set("componentDumpTemplate", "")
        }
        sct.set("strComponentDumpTemplate", config.getComponentDumpTemplate())
        sct.set("deepSearch", Caster.toBoolean(config.doComponentDeepSearch()))
        sct.set("componentDataMemberDefaultAccess", ComponentUtil.toStringAccess(config.getComponentDataMemberDefaultAccess()))
        sct.set("triggerDataMember", Caster.toBoolean(config.getTriggerComponentDataMember()))
        sct.set("useShadow", Caster.toBoolean(config.useComponentShadow()))
        sct.set("ComponentDefaultImport", config.getComponentDefaultImport())
        sct.set("componentLocalSearch", config.getComponentLocalSearch())
        sct.set("componentPathCache", config.useComponentPathCache())
    }

    /**
     * @throws PageException
     */
    @Throws(PageException::class)
    private fun doUpdateRegional() {
        val useTimeServer = getBool("usetimeserver", null)
        try {
            admin.updateLocale(getString("admin", action, "locale"))
            admin.updateTimeZone(getString("admin", action, "timezone"))
            admin.updateTimeServer(getString("admin", action, "timeserver"), useTimeServer)
        } finally {
            store()
        }
        adminSync.broadcast(attributes, config)
    }

    @Throws(PageException::class)
    private fun doUpdateMonitorEnabled() {
        try {
            admin.updateMonitorEnabled(getBool("admin", "UpdateMonitorEnabled", "monitorEnabled"))
        } finally {
            store()
        }
        adminSync.broadcast(attributes, config)
    }

    @Throws(PageException::class)
    private fun doUpdateTLD() {
        try {
            val jar = getString("jar", null)
            if (!StringUtil.isEmpty(jar, true)) {
                val resJar: Resource = ResourceUtil.toResourceExisting(pageContext, jar)
                admin.updateJar(resJar)
            }
            val resTld: Resource = ResourceUtil.toResourceExisting(pageContext, getString("admin", action, "tld"))
            admin.updateTLD(resTld)
        } catch (e: Exception) {
            throw Caster.toPageException(e)
        }
        store()
    }

    @Throws(PageException::class)
    private fun doUpdateFLD() {
        try {
            val jar = getString("jar", null)
            if (!StringUtil.isEmpty(jar, true)) {
                val resJar: Resource = ResourceUtil.toResourceExisting(pageContext, jar)
                admin.updateJar(resJar)
            }
            val resFld: Resource = ResourceUtil.toResourceExisting(pageContext, getString("admin", action, "fld"))
            admin.updateFLD(resFld)
        } catch (e: Exception) {
            throw Caster.toPageException(e)
        }
        store()
    }

    @Throws(PageException::class)
    private fun doUpdateFilesystem() {
        try {
            admin.updateFilesystem(
                    StringUtil.emptyAsNull(getString("fldDefaultDirectory", null), true), StringUtil.emptyAsNull(getString("functionDefaultDirectory", null), true), StringUtil.emptyAsNull(getString("tagDefaultDirectory", null), true), StringUtil.emptyAsNull(getString("tldDefaultDirectory", null), true), StringUtil.emptyAsNull(getString("functionAddionalDirectory", null), true), StringUtil.emptyAsNull(getString("tagAddionalDirectory", null), true)
            )
        } catch (e: Exception) {
            throw Caster.toPageException(e)
        }
        store()
        (pageContext.getConfig() as ConfigWebPro).resetServerFunctionMappings()
    }

    @Throws(PageException::class)
    private fun doUpdateJar() {
        try {
            val resJar: Resource = ResourceUtil.toResourceExisting(pageContext, getString("admin", action, "jar"))
            admin.updateJar(resJar)
        } catch (e: Exception) {
            throw Caster.toPageException(e)
        }
        store()
    }

    @Throws(PageException::class)
    private fun doUpdateLoginSettings() {
        val rememberMe = getBool("admin", "UpdateLoginSettings", "rememberme")
        val captcha = getBool("admin", "UpdateLoginSettings", "captcha")
        val delay = getInt("admin", "UpdateLoginSettings", "delay")
        admin.updateLoginSettings(captcha, rememberMe, delay)
        store()
    }

    @Throws(PageException::class)
    private fun doUpdateLogSettings() {
        val str = getString("admin", "UpdateLogSettings", "level", true)
        val l: Int = LogUtil.toLevel(str, -1)
        if (l == -1) throw ApplicationException("Invalid log level name [$str], valid log level names are [INFO,DEBUG,WARN,ERROR,FATAL,TRACE]")
        val eng: LogEngine = config.getLogEngine()
        // appender
        var className = getString("admin", action, "appenderClass", true)
        var bundleName = getString("appenderBundleName", null)
        var bundleVersion = getString("appenderBundleVersion", null)
        val acd: ClassDefinition = if (StringUtil.isEmpty(bundleName)) eng.appenderClassDefintion(className) else ClassDefinitionImpl(className, bundleName, bundleVersion, config.getIdentification())

        // layout
        className = getString("admin", action, "layoutClass", true)
        bundleName = getString("layoutBundleName", null)
        bundleVersion = getString("layoutBundleVersion", null)
        val lcd: ClassDefinition = if (StringUtil.isEmpty(bundleName)) eng.layoutClassDefintion(className) else ClassDefinitionImpl(className, bundleName, bundleVersion, config.getIdentification())
        admin.updateLogSettings(getString("admin", "UpdateLogSettings", "name", true), l, acd, Caster.toStruct(getObject("admin", "UpdateLogSettings", "appenderArgs")), lcd,
                Caster.toStruct(getObject("admin", "UpdateLogSettings", "layoutArgs")))
        store()
    }

    @Throws(PageException::class)
    private fun doUpdateSSLCertificate() {
        val host = getString("admin", "UpdateSSLCertificateInstall", "host")
        val port = getInt("port", 443)
        updateSSLCertificate(config, host, port)
    }

    @Throws(PageException::class)
    private fun doGetSSLCertificate() {
        val host = getString("admin", "GetSSLCertificate", "host")
        val port = getInt("port", 443)
        pageContext.setVariable(getString("admin", action, "returnVariable"), getSSLCertificate(config, host, port))
    }

    @Throws(PageException::class)
    private fun doRemoveBundle() {
        try {
            val name = getString("admin", action, "name")
            val version = getString("admin", action, "version")
            val removePhysical = getBoolV("removePhysical", true)
            OSGiUtil.removeLocalBundle(name.trim(), OSGiUtil.toVersion(version.trim()), null, removePhysical, false)
        } catch (e: Exception) {
            throw Caster.toPageException(e)
        }
        store()
    }

    @Throws(PageException::class)
    private fun doRemoveTLD() {
        try {
            var name = getString("tld", null)
            if (StringUtil.isEmpty(name)) name = getString("admin", action, "name")
            admin.removeTLD(name)
        } catch (e: Exception) {
            throw Caster.toPageException(e)
        }
        store()
    }

    @Throws(PageException::class)
    private fun doRemoveFLD() {
        try {
            var name = getString("fld", null)
            if (StringUtil.isEmpty(name)) name = getString("admin", action, "name")
            admin.removeFLD(name)
        } catch (e: Exception) {
            throw Caster.toPageException(e)
        }
        store()
    }

    @Throws(PageException::class)
    private fun doUpdateRemoteClient() {
        admin.updateRemoteClient(getString("admin", action, "label"), getString("admin", action, "url"), getString("admin", action, "remotetype"),
                getString("admin", action, "securityKey"), getString("admin", action, "usage"), getString("admin", action, "adminPassword"), getString("ServerUsername", ""),
                getString("ServerPassword", ""), getString("proxyServer", ""), getString("proxyUsername", ""), getString("proxyPassword", ""), getString("proxyPort", "")
        )
        store()
    }

    @Throws(PageException::class)
    private fun doReadBundle() {
        val ret = getString("admin", action, "returnvariable")
        val res: Resource = ResourceUtil.toResourceExisting(pageContext, getString("admin", action, "bundle"))
        if (!res.isFile()) throw ApplicationException("[$res] is not a file")
        try {
            val sct: Struct = StructImpl()
            pageContext.setVariable(ret, BundleFile.getInstance(res).info())
        } catch (e: Exception) {
            throw Caster.toPageException(e)
        }
    }

    @Throws(PageException::class)
    private fun doBuildBundle() {
        val name = getString("admin", action, "name")
        var symName = getString("symbolicname", null)
        val existingRelation = getString("existingrelation", null)
        // boolean doDyn=StringUtil.isEmpty(existingRelation) ||
        // (existingRelation=existingRelation.trim()).equalsIgnoreCase("dynamic");
        // print.e("dynamic:"+existingRelation+"<>"+doDyn);
        val ignoreExistingManifest = getBoolV("ignoreExistingManifest", false)
        val dest: Resource = ResourceUtil.toResourceNotExisting(pageContext, getString("admin", action, "destination"))
        val strJar = getString("admin", action, "jar")
        if (StringUtil.isEmpty(strJar, true)) throw ApplicationException("Missing valid jar path")
        val jar: Resource = ResourceUtil.toResourceExisting(pageContext, strJar.trim())
        var relatedPackages: Set<String?>? = null
        try {
            relatedPackages = JarUtil.getExternalImports(jar, arrayOfNulls<String?>(0)) // OSGiUtil.getBootdelegation()
        } catch (e1: IOException) {
            LogUtil.log(pageContext, Admin::class.java.getName(), e1)
        }
        if (relatedPackages == null) relatedPackages = HashSet<String?>()

        // org.osgi.framework.bootdelegation
        val factory: BundleBuilderFactory?
        try {
            symName = if (StringUtil.isEmpty(symName, true)) null else symName.trim()
            if (symName == null) symName = name
            factory = BundleBuilderFactory(jar, symName)
            factory.setName(name)
            factory.setIgnoreExistingManifest(ignoreExistingManifest)
        } catch (e: Exception) {
            throw Caster.toPageException(e)
        }
        var activator = getString("bundleActivator", null)
        if (activator == null) activator = getString("activator", null)
        if (!StringUtil.isEmpty(activator, true)) factory.setActivator(activator.trim())
        val version = getString("version", null)
        if (!StringUtil.isEmpty(version, true)) factory.setVersion(OSGiUtil.toVersion(version, null))
        val description = getString("description", null)
        if (!StringUtil.isEmpty(description, true)) factory.setDescription(description.trim())
        val classPath = getString("classPath", null)
        if (!StringUtil.isEmpty(classPath, true)) factory.addClassPath(classPath.trim())

        // dynamic import packages
        var dynamicImportPackage = getString("dynamicimportpackage", null)
        if (!StringUtil.isEmpty(dynamicImportPackage, true)) factory.addDynamicImportPackage(dynamicImportPackage.trim().also { dynamicImportPackage = it })
        val dynamicImportPackageSet: Set<String?> = ListUtil.listToSet(dynamicImportPackage, ",", true)
        /*
		 * String dynamicImportPackage=getString("dynamicimportpackage",null); if(doDyn) {
		 * if(relatedPackages.size()>0) { // add importPackage to set
		 * if(!StringUtil.isEmpty(dynamicImportPackage)) { String[] arr =
		 * ListUtil.trimItems(ListUtil.listToStringArray(dynamicImportPackage, ',')); for(int
		 * i=0;i<arr.length;i++){ relatedPackages.add(arr[i]); } }
		 * dynamicImportPackage=ListUtil.toList(relatedPackages, ","); } relatedPackages.clear(); }
		 * if(!StringUtil.isEmpty(dynamicImportPackage,true))factory.addDynamicImportPackage(
		 * dynamicImportPackage.trim());
		 */

        // Import Package
        // we remove all imports that are defined as dyn import
        val it = dynamicImportPackageSet.iterator()
        while (it.hasNext()) {
            relatedPackages.remove(it.next())
        }
        var importPackage = getString("importpackage", null)
        // add importPackage to set
        if (!StringUtil.isEmpty(importPackage)) {
            val arr: Array<String?> = ListUtil.trimItems(ListUtil.listToStringArray(importPackage, ','))
            for (i in arr.indices) {
                relatedPackages.add(arr[i])
            }
        }

        // remove all packages defined in dynamic imports
        if (!StringUtil.isEmpty(dynamicImportPackage)) {
            val arr: Array<String?> = ListUtil.trimItems(ListUtil.listToStringArray(dynamicImportPackage, ','))
            val newDynImport: List<String?> = ArrayList<String?>()
            for (i in arr.indices) {
                if (!relatedPackages!!.contains(arr[i])) newDynImport.add(arr[i])
                // relatedPackages.remove(arr[i]);
            }
            if (arr.size != newDynImport.size()) dynamicImportPackage = ListUtil.listToListEL(newDynImport, ",")
        }
        val sortedList: List = ArrayList(relatedPackages)
        Collections.sort(sortedList)
        importPackage = ListUtil.toList(sortedList, ",")
        if (!StringUtil.isEmpty(importPackage, true)) factory.addImportPackage(importPackage.trim())
        val bundleActivationPolicy = getString("bundleActivationPolicy", null)
        if (!StringUtil.isEmpty(bundleActivationPolicy, true)) factory.setBundleActivationPolicy(bundleActivationPolicy.trim())
        var exportPackage = getString("exportpackage", null)
        if (!StringUtil.isEmpty(exportPackage, true)) {
            exportPackage = ListUtil.sort(exportPackage.trim(), "text", "asc", ",")
            factory.addExportPackage(exportPackage)
        }
        var requireBundle = getString("requireBundle", null)
        if (!StringUtil.isEmpty(requireBundle, true)) {
            requireBundle = ListUtil.sort(requireBundle.trim(), "text", "asc", ",")
            factory.addRequireBundle(requireBundle)
        }
        var requireBundleFragment = getString("requireBundleFragment", null)
        if (!StringUtil.isEmpty(requireBundleFragment, true)) {
            requireBundleFragment = ListUtil.sort(requireBundleFragment.trim(), "text", "asc", ",")
            factory.addRequireBundleFragment(requireBundleFragment)
        }
        val fragmentHost = getString("fragmentHost", null)
        if (!StringUtil.isEmpty(fragmentHost, true)) factory.addFragmentHost(fragmentHost.trim())
        try {
            factory.build(dest)
        } catch (e: IOException) {
            throw Caster.toPageException(e)
        }
    }

    @Throws(PageException::class)
    private fun doUpdateRemoteClientUsage() {
        admin.updateRemoteClientUsage(getString("admin", action, "code"), getString("admin", action, "displayname")
        )
        store()
    }

    @Throws(PageException::class)
    private fun doRemoveRemoteClientUsage() {
        admin.removeRemoteClientUsage(getString("admin", action, "code")
        )
        store()
    }

    @get:Throws(IOException::class)
    private val callerId: String?
        private get() {
            if (type == TYPE_WEB) {
                return config.getIdentification().getId()
            }
            if (config is ConfigWeb) {
                val cw: ConfigWeb? = config as ConfigWeb?
                return cw.getIdentification().getServerIdentification().getId()
            }
            if (config is ConfigServer) {
                return config.getIdentification().getId()
            }
            throw IOException("can not create id")
        }

    @Throws(PageException::class)
    private fun doUpdateApplicationListener() {
        admin.updateApplicationListener(getString("admin", action, "listenerType"), getString("admin", action, "listenerMode"))
        admin.updateApplicationPathTimeout(getTimespan("admin", action, "applicationPathTimeout"))
        store()
        adminSync.broadcast(attributes, config)
    }

    @Throws(PageException::class)
    private fun doUpdateCachedWithin() {
        val str = getString("admin", action, "cachedWithinType")
        val type: Int = AppListenerUtil.toCachedWithinType(str, -1)
        if (type == -1) throw ApplicationException("Cached within type [$str] is invalid, valid types are [function,include,query,resource]")
        admin.updateCachedWithin(type, getString("admin", action, "cachedWithin"))
        store()
        adminSync.broadcast(attributes, config)
    }

    @Throws(PageException::class)
    private fun doUpdateProxy() {
        admin.updateProxy(getBool("admin", action, "proxyenabled"), getString("admin", action, "proxyserver"), getInt("admin", action, "proxyport"),
                getString("admin", action, "proxyusername"), getString("admin", action, "proxypassword"))
        store()
    }

    @Throws(PageException::class)
    private fun doUpdateCharset() {
        admin.updateResourceCharset(getString("admin", action, "resourceCharset"))
        admin.updateTemplateCharset(getString("admin", action, "templateCharset"))
        admin.updateWebCharset(getString("admin", action, "webCharset"))
        store()
        adminSync.broadcast(attributes, config)
    }

    /**
     * @throws PageException
     */
    @Throws(PageException::class)
    private fun doSecurityManager() {
        val rtnVar = getString("admin", action, "returnVariable")
        val secType = getString("admin", action, "sectype")
        val secValue = getString("secvalue", null)
        val isServer = config is ConfigServer
        if (secValue == null) {
            if (isServer) {
                pageContext.setVariable(rtnVar, SecurityManagerImpl.toStringAccessValue(SecurityManager.VALUE_YES))
            } else {
                pageContext.setVariable(rtnVar, SecurityManagerImpl.toStringAccessValue(config.getSecurityManager().getAccess(secType)))
            }
            return
        }
        pageContext.setVariable(rtnVar, Caster.toBoolean(isServer || config.getSecurityManager().getAccess(secType) === SecurityManagerImpl.toShortAccessValue(secValue)))
    }

    /**
     * @throws PageException
     */
    @Throws(PageException::class)
    private fun doGetTimeZones() {
        val strLocale = getString("locale", "english (united kingdom)")
        val locale: Locale = LocaleFactory.getLocale(strLocale)
        val timeZones: Array<String?> = TimeZone.getAvailableIDs()
        val qry: lucee.runtime.type.Query = QueryImpl(arrayOf<String?>("id", "display"), arrayOf<String?>("varchar", "varchar"), timeZones.size, "timezones")
        Arrays.sort(timeZones)
        var timeZone: TimeZone
        for (i in timeZones.indices) {
            timeZone = TimeZone.getTimeZone(timeZones[i])
            qry.setAt("id", i + 1, timeZones[i])
            qry.setAt("display", i + 1, timeZone.getDisplayName(locale))
        }
        pageContext.setVariable(getString("admin", action, "returnVariable"), qry)
    }

    /**
     * @throws PageException
     */
    @Throws(PageException::class)
    private fun doGetLocales() {
        val sct: Struct = StructImpl(StructImpl.TYPE_LINKED)
        // Array arr=new ArrayImpl();
        val strLocale = getString("locale", "english (united kingdom)")
        val locale: Locale = LocaleFactory.getLocale(strLocale)
        pageContext.setVariable(getString("admin", action, "returnVariable"), sct)
        val locales: Map = LocaleFactory.getLocales()
        val it: Iterator = locales.keySet().iterator()
        var key: String
        var l: Locale
        while (it.hasNext()) {
            key = it.next()
            l = locales.get(key) as Locale
            sct.setEL(l.toString(), l.getDisplayName(locale))
            // arr.append(locale.getDisplayName());
        }
        // arr.sort("textnocase","asc");
    }

    @Throws(PageException::class)
    private fun doGetApplicationListener() {
        val sct: Struct = StructImpl()
        pageContext.setVariable(getString("admin", action, "returnVariable"), sct)
        val appListener: ApplicationListener = config.getApplicationListener()
        sct.set("type", AppListenerUtil.toStringType(appListener))
        sct.set("mode", AppListenerUtil.toStringMode(appListener.getMode()))
        // replaced with encoding outputsct.set("defaultencoding", config.get DefaultEncoding());
    }

    /**
     * @throws PageException
     */
    @Throws(PageException::class)
    private fun doGetRegional() {
        val sct: Struct = StructImpl()
        pageContext.setVariable(getString("admin", action, "returnVariable"), sct)
        sct.set("locale", Caster.toString(config.getLocale()))
        sct.set("timezone", toStringTimeZone(config.getTimeZone()))
        sct.set("timeserver", config.getTimeServer())
        sct.set("usetimeserver", config.getUseTimeServer())
        // replaced with encoding outputsct.set("defaultencoding", config.get DefaultEncoding());
    }

    @Throws(PageException::class)
    private fun doIsMonitorEnabled() {
        if (config is ConfigServerImpl) {
            val cs: ConfigServerImpl? = config as ConfigServerImpl?
            pageContext.setVariable(getString("admin", action, "returnVariable"), Caster.toBoolean(cs.isMonitoringEnabled()))
        }
    }

    @Throws(PageException::class)
    private fun doSurveillance() {
        // Server
        if (config is ConfigServer) {
            val cs: ConfigServer? = config as ConfigServer?
            val webs: Array<ConfigWeb?> = cs.getConfigWebs()
            val sct: Struct = StructImpl()
            for (i in webs.indices) {
                val cw: ConfigWeb? = webs[i]
                try {
                    sct.setEL(cw.getIdentification().getId(), (cw.getFactory() as CFMLFactoryImpl).getInfo())
                } catch (t: Throwable) {
                    ExceptionUtil.rethrowIfNecessary(t)
                }
            }
            pageContext.setVariable(getString("admin", action, "returnVariable"), sct)
        } else {
            val factory: CFMLFactoryImpl = (config as ConfigWeb?).getFactory() as CFMLFactoryImpl
            pageContext.setVariable(getString("admin", action, "returnVariable"), factory.getInfo())
        }
    }

    @Throws(PageException::class)
    private fun doStopThread() {
        val contextId = getString("admin", "stopThread", "contextId")
        val threadId = getString("admin", "stopThread", "threadId")
        val stopType = getString("stopType", "exception")
        if (config !is ConfigServer) throw ApplicationException("Invalid context for this action")
        val cs: ConfigServer? = config as ConfigServer?
        val webs: Array<ConfigWeb?> = cs.getConfigWebs()
        var has = false
        for (i in webs.indices) {
            val cw: ConfigWeb? = webs[i]
            if (!cw.getIdentification().getId().equals(contextId)) continue
            (cw.getFactory() as CFMLFactoryImpl).stopThread(threadId, stopType)
            has = true
            break
        }
        if (!has) {
            for (i in webs.indices) {
                val cw: ConfigWeb? = webs[i]
                if (!contextId!!.equals(cw.getLabel())) continue
                (cw.getFactory() as CFMLFactoryImpl).stopThread(threadId, stopType)
                has = true
                break
            }
        }
    }

    @Throws(PageException::class)
    private fun doHeapDump() {
        val strDestination = getString("admin", action, "destination")
        val live = getBoolV("live", true)
        val destination: Resource = ResourceUtil.toResourceNotExisting(pageContext, strDestination)
        try {
            HeapDumper.dumpTo(destination, live)
        } catch (e: IOException) {
            throw Caster.toPageException(e)
        }
    }

    @Throws(PageException::class)
    private fun doGetProxy() {
        val sct: Struct = StructImpl()
        pageContext.setVariable(getString("admin", action, "returnVariable"), sct)
        val pd: ProxyData = config.getProxyData()
        val port = if (pd == null || pd.getPort() <= 0) "" else Caster.toString(pd.getPort())

        // sct.set("enabled",Caster.toBoolean(config.isProxyEnable()));
        sct.set("port", port)
        sct.set("server", if (pd == null) "" else emptyIfNull(pd.getServer()))
        sct.set("username", if (pd == null) "" else emptyIfNull(pd.getUsername()))
        sct.set("password", if (pd == null) "" else emptyIfNull(pd.getPassword()))
    }

    @Throws(ApplicationException::class, PageException::class)
    private fun doGetLoginSettings() {
        val sct: Struct = StructImpl()
        val c: ConfigPro = ThreadLocalPageContext.getConfig(config) as ConfigPro
        pageContext.setVariable(getString("admin", action, "returnVariable"), sct)
        sct.set("captcha", Caster.toBoolean(c.getLoginCaptcha()))
        sct.set("delay", Caster.toDouble(c.getLoginDelay()))
        sct.set("rememberme", Caster.toBoolean(c.getRememberMe()))
        if (c is ConfigWebPro) {
            val cw: ConfigWebPro = c as ConfigWebPro
            val origin: Short = cw.getPasswordSource()
            if (origin == ConfigWebPro.PASSWORD_ORIGIN_DEFAULT) sct.set("origin", "default") else if (origin == ConfigWebPro.PASSWORD_ORIGIN_WEB) sct.set("origin", "web") else if (origin == ConfigWebPro.PASSWORD_ORIGIN_SERVER) sct.set("origin", "server")
        }
    }

    @Throws(PageException::class)
    private fun doGetCharset() {
        val sct: Struct = StructImpl()
        pageContext.setVariable(getString("admin", action, "returnVariable"), sct)
        sct.set("resourceCharset", config.getResourceCharset().name())
        sct.set("templateCharset", config.getTemplateCharset().name())
        sct.set("webCharset", config.getWebCharset().name())
        sct.set("jreCharset", SystemUtil.getCharset().name())
    }

    /**
     * @throws PageException
     */
    @Throws(PageException::class)
    private fun doGetUpdate() {
        val sct: Struct = StructImpl()
        pageContext.setVariable(getString("admin", action, "returnVariable"), sct)
        var location: URL = config.getUpdateLocation()
        if (location == null) location = Constants.DEFAULT_UPDATE_URL
        var type: String = config.getUpdateType()
        if (StringUtil.isEmpty(type)) type = "manual"
        sct.set("location", location.toExternalForm())
        sct.set("type", type)
    }

    /**
     * @throws PageException
     */
    @Throws(PageException::class)
    private fun store() {
        try {
            admin.storeAndReload()
        } catch (e: Exception) {
            throw Caster.toPageException(e)
        }
    }

    @Throws(ApplicationException::class)
    private fun getString(tagName: String?, actionName: String?, attributeName: String?): String? {
        return getString(tagName, actionName, attributeName, true)
    }

    @Throws(ApplicationException::class)
    private fun getString(tagName: String?, actionName: String?, attributeName: String?, trim: Boolean): String? {
        val value = getString(attributeName, null)
                ?: throw ApplicationException("Attribute [$attributeName] for tag [$tagName] is required if attribute action has the value [$actionName]")
        return if (trim) value.trim() else value
    }

    @Throws(ApplicationException::class)
    private fun getDouble(tagName: String?, actionName: String?, attributeName: String?): Double {
        val value = getDouble(attributeName, Double.NaN)
        if (!Decision.isValid(value)) throw ApplicationException("Attribute [$attributeName] for tag [$tagName] is required if attribute action has the value [$actionName]")
        return value
    }

    private fun getString(attributeName: String?, defaultValue: String?): String? {
        val value: Object = attributes.get(attributeName, null) ?: return defaultValue
        return Caster.toString(value, null)
    }

    private fun getDateTime(attributeName: String?, defaultValue: DateTime?): DateTime? {
        val value: Object = attributes.get(attributeName, null) ?: return defaultValue
        return DateCaster.toDateAdvanced(value, null, defaultValue)
    }

    private fun getObject(attributeName: String?, defaultValue: Object?): Object? {
        return attributes.get(attributeName, defaultValue)
    }

    @Throws(PageException::class)
    private fun getBool(tagName: String?, actionName: String?, attributeName: String?): Boolean {
        val value: Object = attributes.get(attributeName, null)
                ?: throw ApplicationException("Attribute [$attributeName] for tag [$tagName] is required if attribute action has the value [$actionName]")
        return Caster.toBooleanValue(value)
    }

    @Throws(PageException::class)
    private fun getBoolObject(tagName: String?, actionName: String?, attributeName: String?): Boolean? {
        val value: Object = attributes.get(attributeName, null)
                ?: throw ApplicationException("Attribute [$attributeName] for tag [$tagName] is required if attribute action has the value [$actionName]")
        return if (StringUtil.isEmpty(value)) null else Caster.toBoolean(value)
    }

    @Throws(PageException::class)
    private fun getObject(tagName: String?, actionName: String?, attributeName: String?): Object? {
        return attributes.get(attributeName, null)
                ?: throw ApplicationException("Attribute [$attributeName] for tag [$tagName] is required if attribute action has the value [$actionName]")
    }

    @Throws(PageException::class)
    private fun toTimeout(timeout: Object?, defaultValue: Long): Long {
        if (timeout is TimeSpan) return (timeout as TimeSpan?).getMillis()
        // seconds
        val i: Int = Caster.toIntValue(timeout)
        if (i < 0) throw ApplicationException("Invalid value [$i], value must be a positive integer greater or equal than 0")
        return (i * 1000).toLong()
    }

    private fun getBoolV(attributeName: String?, defaultValue: Boolean): Boolean {
        val value: Object = attributes.get(attributeName, null) ?: return defaultValue
        return Caster.toBooleanValue(value, defaultValue)
    }

    private fun getBool(attributeName: String?, defaultValue: Boolean?): Boolean? {
        val value: Object = attributes.get(attributeName, null) ?: return defaultValue
        return Caster.toBoolean(value, defaultValue)
    }

    private fun getStruct(attributeName: String?, defaultValue: Struct?): Struct? {
        val value: Object = attributes.get(attributeName, null) ?: return defaultValue
        return try {
            Caster.toStruct(value)
        } catch (e: PageException) {
            defaultValue
        }
    }

    @Throws(PageException::class)
    private fun getStruct(tagName: String?, actionName: String?, attributeName: String?): Struct? {
        val value: Object = attributes.get(attributeName, null)
                ?: throw ApplicationException("Attribute [$attributeName] for tag [$tagName] is required if attribute action has the value [$actionName]")
        return Caster.toStruct(value)
    }

    @Throws(PageException::class)
    private fun getInteger(tagName: String?, actionName: String?, attributeName: String?): Integer? {
        val value: Object = attributes.get(attributeName, null)
                ?: throw ApplicationException("Attribute [$attributeName] for tag [$tagName] is required if attribute action has the value [$actionName]")
        return if (StringUtil.isEmpty(value)) null else Caster.toIntValue(value)
    }

    @Throws(PageException::class)
    private fun getInt(tagName: String?, actionName: String?, attributeName: String?): Int {
        val value: Object = attributes.get(attributeName, null)
                ?: throw ApplicationException("Attribute [$attributeName] for tag [$tagName] is required if attribute action has the value [$actionName]")
        return Caster.toDoubleValue(value)
    }

    private fun getInt(attributeName: String?, defaultValue: Int): Int {
        val value: Object = attributes.get(attributeName, null) ?: return defaultValue
        return Caster.toIntValue(value, defaultValue)
    }

    private fun getLong(attributeName: String?, defaultValue: Long): Long {
        val value: Object = attributes.get(attributeName, null) ?: return defaultValue
        return Caster.toLongValue(value, defaultValue)
    }

    private fun getDouble(attributeName: String?, defaultValue: Double): Double {
        val value: Object = attributes.get(attributeName, null) ?: return defaultValue
        return Caster.toDoubleValue(value, true, defaultValue)
    }

    @Throws(PageException::class)
    private fun getTimespan(tagName: String?, actionName: String?, attributeName: String?): TimeSpan? {
        val value: Object = attributes.get(attributeName, null)
                ?: throw ApplicationException("Attribute [$attributeName] for tag [$tagName] is required if attribute action has the value [$actionName]")
        return if (StringUtil.isEmpty(value)) null else Caster.toTimespan(value)
    }

    private fun emptyIfNull(str: String?): Object? {
        return str ?: ""
    }

    @Throws(ApplicationException::class)
    private fun throwNoAccessWhenWeb() {
        if (!singleMode && type == TYPE_WEB) throw ApplicationException("Action [$action] is not available for Web Admin ( Server Admin only )")
    }

    @Throws(ApplicationException::class)
    private fun throwNoAccessWhenServer() {
        if (!singleMode && type == TYPE_SERVER) {
            throw ApplicationException("Action [$action] is not available for Server Admin ( Web Admin only )")
        }
    }

    companion object {
        private const val TYPE_WEB: Short = 0
        private const val TYPE_SERVER: Short = 1
        private const val ACCESS_FREE: Short = 0
        private const val ACCESS_NOT_WHEN_WEB: Short = 1
        private const val ACCESS_NOT_WHEN_SERVER: Short = 2
        private const val ACCESS_NEVER: Short = 3
        private const val ACCESS_READ: Short = 10
        private const val ACCESS_WRITE: Short = 11
        private val DEBUG: Collection.Key? = KeyConstants._debug

        // private static final Collection.Key DEBUG_TEMPLATE = KeyImpl.intern("debugTemplate");
        private val DEBUG_SHOW_QUERY_USAGE: Collection.Key? = KeyImpl.getInstance("debugShowQueryUsage")

        // private static final Collection.Key STR_DEBUG_TEMPLATE = KeyImpl.intern("strdebugTemplate");
        private val TEMPLATES: Collection.Key? = KeyConstants._templates
        private val STR: Collection.Key? = KeyConstants._str
        private val DO_STATUS_CODE: Collection.Key? = KeyImpl.getInstance("doStatusCode")
        private val LABEL: Collection.Key? = KeyConstants._label
        private val FILE_ACCESS: Collection.Key? = KeyImpl.getInstance("file_access")
        private val IP_RANGE: Collection.Key? = KeyImpl.getInstance("ipRange")
        private val CUSTOM: Collection.Key? = KeyConstants._custom
        private val READONLY: Collection.Key? = KeyConstants._readOnly
        private val LOG_ENABLED: Collection.Key? = KeyImpl.getInstance("logEnabled")
        private val CLASS: Collection.Key? = KeyConstants._class
        private val HAS_OWN_SEC_CONTEXT: Key? = KeyImpl.getInstance("hasOwnSecContext")
        private val CONFIG_FILE: Key? = KeyImpl.getInstance("config_file")
        private val PROCEDURE: Key? = KeyImpl.getInstance("procedure")
        private val SERVER_LIBRARY: Key? = KeyImpl.getInstance("serverlibrary")
        private val KEEP_ALIVE: Key? = KeyImpl.getInstance("keepalive")
        private val CLIENT_SIZE: Key? = KeyImpl.getInstance("clientSize")
        private val SESSION_SIZE: Key? = KeyImpl.getInstance("sessionSize")
        private val CLIENT_ELEMENTS: Key? = KeyImpl.getInstance("clientElements")
        private val SESSION_ELEMENTS: Key? = KeyImpl.getInstance("sessionElements")
        private const val MAPPING_REGULAR: Short = 1
        private const val MAPPING_CT: Short = 2
        private const val MAPPING_CFC: Short = 4
        private val FILTER_CFML_TEMPLATES: ResourceFilter? = OrResourceFilter(arrayOf<ResourceFilter?>(DirectoryResourceFilter(), ExtensionResourceFilter(Constants.getExtensions())))
        private val FRAGMENT: Key? = KeyImpl.getInstance("fragment")
        private val HEADERS: Key? = KeyConstants._headers
        private val SYMBOLIC_NAME: Key? = KeyImpl.getInstance("symbolicName")
        private val VENDOR: Key? = KeyImpl.getInstance("vendor")
        private val USED_BY: Key? = KeyImpl.getInstance("usedBy")
        private val PATH: Key? = KeyConstants._path
        @Throws(PageException::class)
        private fun fillGetRunningThreads(qry: lucee.runtime.type.Query?, configWeb: ConfigWeb?) {
            val factory: CFMLFactoryImpl = configWeb.getFactory() as CFMLFactoryImpl
            val pcs: Map<Integer?, PageContextImpl?> = factory.getActivePageContexts()
            val it: Iterator<PageContextImpl?> = pcs.values().iterator()
            var pc: PageContextImpl?
            var key: Collection.Key
            var row = 0
            while (it.hasNext()) {
                pc = it.next()
                qry.addRow()
                row++
                val st: Array<StackTraceElement?> = pc.getThread().getStackTrace()
                configWeb.getConfigDir()
                configWeb.getIdentification().getId()
                configWeb.getConfigDir()
                qry.setAt("Id", row, Double.valueOf(pc.getId()))
                qry.setAt("Start", row, DateTimeImpl(pc.getStartTime(), false))
                qry.setAt("Timeout", row, Double.valueOf(pc.getRequestTimeout() / 1000))
                val root: PageContext = pc.getRootPageContext()
                qry.setAt("ThreadType", row, if (root != null && root !== pc) "main" else "child")
                qry.setAt("StackTrace", row, toString(st))
                qry.setAt("TagContext", row, PageExceptionImpl.getTagContext(pc.getConfig(), st))
                qry.setAt("label", row, factory.getLabel())
                qry.setAt("RootPath", row, ReqRspUtil.getRootPath(configWeb.getServletContext()))
                qry.setAt("ConfigFile", row, configWeb.getConfigFile().getAbsolutePath())
                if (factory.getURL() != null) qry.setAt("url", row, factory.getURL().toExternalForm())
            }
        }

        private fun toString(traces: Array<StackTraceElement?>?): String? {
            var trace: StackTraceElement?
            val sb = StringBuilder(traces!!.size * 32)
            for (i in traces.indices) {
                trace = traces!![i]
                sb.append("\tat ")
                sb.append(trace.toString())
                sb.append(':')
                sb.append(trace.getLineNumber())
                sb.append(SystemUtil.getOSSpecificLineSeparator())
            }
            return sb.toString()
        }

        private fun terminateRunningThread(configWeb: ConfigWeb?, id: Int): Boolean {
            val pcs: Map<Integer?, PageContextImpl?> = (configWeb.getFactory() as CFMLFactoryImpl).getActivePageContexts()
            val it: Iterator<PageContextImpl?> = pcs.values().iterator()
            var pc: PageContextImpl?
            var key: Collection.Key
            while (it.hasNext()) {
                pc = it.next()
                if (pc.getId() === id) {
                    CFMLFactoryImpl.terminate(pc, true)
                    return true
                }
            }
            return false
        }

        @Throws(PageException::class)
        fun updateSSLCertificate(config: Config?, host: String?, port: Int) {
            val cacerts: Resource = config.getSecurityDirectory()
            try {
                val installer = CertificateInstaller(cacerts, host, port)
                installer.installAll()
            } catch (e: Exception) {
                throw Caster.toPageException(e)
            }
        }

        @Throws(PageException::class)
        fun getSSLCertificate(config: Config?, host: String?, port: Int): Query? {
            val cacerts: Resource = config.getSecurityDirectory()
            val installer: CertificateInstaller?
            try {
                installer = CertificateInstaller(cacerts, host, port)
            } catch (e: Exception) {
                throw Caster.toPageException(e)
            }
            val certs: Array<X509Certificate?> = installer.getCertificates()
            var cert: X509Certificate?
            val qry: Query = QueryImpl(arrayOf<String?>("subject", "issuer"), certs.size, "certificates")
            for (i in certs.indices) {
                cert = certs[i]
                qry.setAtEL("subject", i + 1, cert.getSubjectDN().getName())
                qry.setAtEL("issuer", i + 1, cert.getIssuerDN().getName())
            }
            return qry
        }
    }
}