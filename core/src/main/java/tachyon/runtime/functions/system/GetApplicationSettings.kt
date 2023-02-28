/**
 * Copyright (c) 2014, the Railo Company Ltd.
 * Copyright (c) 2015, Tachyon Assosication Switzerland
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
package tachyon.runtime.functions.system

import java.util.Iterator

class GetApplicationSettings : BIF() {
    @Override
    @Throws(PageException::class)
    operator fun invoke(pc: PageContext?, args: Array<Object?>?): Object? {
        if (args!!.size == 2) return call(pc, Caster.toBooleanValue(args[0]), Caster.toBooleanValue(args[1]))
        if (args.size == 1) return call(pc, Caster.toBooleanValue(args[0]))
        if (args.size == 0) return call(pc)
        throw FunctionException(pc, "GetApplicationSettings", 0, 2, args.size)
    }

    companion object {
        @Throws(PageException::class)
        fun call(pc: PageContext?): Struct? {
            return call(pc, false, false)
        }

        @Throws(PageException::class)
        fun call(pc: PageContext?, suppressFunctions: Boolean): Struct? {
            return call(pc, suppressFunctions, false)
        }

        @Throws(PageException::class)
        fun call(pc: PageContext?, suppressFunctions: Boolean, onlySupported: Boolean): Struct? {
            val ac: ApplicationContext = pc.getApplicationContext()
            val acs: ApplicationContextSupport = ac as ApplicationContextSupport
            var cfc: Component? = null
            if (ac is ModernApplicationContext) cfc = (ac as ModernApplicationContext).getComponent()
            val sct: Struct = StructImpl(Struct.TYPE_LINKED)
            sct.setEL("applicationTimeout", ac.getApplicationTimeout())
            sct.setEL("blockedExtForFileUpload", acs.getBlockedExtForFileUpload())
            sct.setEL("clientManagement", Caster.toBoolean(ac.isSetClientManagement()))
            sct.setEL("clientStorage", ac.getClientstorage())
            sct.setEL("sessionStorage", ac.getSessionstorage())
            sct.setEL("customTagPaths", toArray(ac.getCustomTagMappings()))
            sct.setEL("componentPaths", toArray(ac.getComponentMappings()))
            sct.setEL("loginStorage", AppListenerUtil.translateLoginStorage(ac.getLoginStorage()))
            sct.setEL(KeyConstants._mappings, toStruct(ac.getMappings()))
            sct.setEL(KeyConstants._name, ac.getName())
            sct.setEL("scriptProtect", AppListenerUtil.translateScriptProtect(ac.getScriptProtect()))
            sct.setEL("secureJson", Caster.toBoolean(ac.getSecureJson()))
            sct.setEL("CGIReadOnly", Caster.toBoolean(ac.getCGIScopeReadonly()))
            sct.setEL("typeChecking", Caster.toBoolean(ac.getTypeChecking()))
            sct.setEL("secureJsonPrefix", ac.getSecureJsonPrefix())
            sct.setEL("sessionManagement", Caster.toBoolean(ac.isSetSessionManagement()))
            sct.setEL("sessionTimeout", ac.getSessionTimeout())
            sct.setEL("clientTimeout", ac.getClientTimeout())
            sct.setEL("setClientCookies", Caster.toBoolean(ac.isSetClientCookies()))
            sct.setEL("setDomainCookies", Caster.toBoolean(ac.isSetDomainCookies()))
            sct.setEL(KeyConstants._name, ac.getName())
            sct.setEL("localMode", if (ac.getLocalMode() === Undefined.MODE_LOCAL_OR_ARGUMENTS_ALWAYS) Boolean.TRUE else Boolean.FALSE)
            sct.setEL(KeyConstants._locale, LocaleFactory.toString(pc.getLocale()))
            sct.setEL(KeyConstants._timezone, TimeZoneUtil.toString(pc.getTimeZone()))
            // sct.setEL(KeyConstants._timeout,TimeZoneUtil.toString(pc.getRequestTimeout()));
            sct.setEL("nullSupport", (ac as ApplicationContextSupport).getFullNullSupport())
            sct.setEL("enableNullSupport", (ac as ApplicationContextSupport).getFullNullSupport())

            // scope cascading
            sct.setEL("scopeCascading", ConfigWebUtil.toScopeCascading(ac.getScopeCascading(), null))
            if (ac.getScopeCascading() !== Config.SCOPE_SMALL) {
                sct.setEL("searchImplicitScopes", ac.getScopeCascading() === Config.SCOPE_STANDARD)
            }

            // adminMode
            sct.setEL("singleContext", ConfigWebUtil.toAdminMode((pc.getConfig() as ConfigPro).getAdminMode(), "single") === "single")
            val cs: Struct = StructImpl(Struct.TYPE_LINKED)
            cs.setEL("web", pc.getWebCharset().name())
            cs.setEL("resource", (pc as PageContextImpl?).getResourceCharset().name())
            sct.setEL("charset", cs)
            sct.setEL("sessionType", AppListenerUtil.toSessionType((pc as PageContextImpl?).getSessionType(), "application"))
            val rt: Struct = StructImpl(Struct.TYPE_LINKED)
            if (ac is ModernApplicationContext) rt.setEL("type", (ac as ModernApplicationContext).getRegex().getTypeName())
            sct.setEL("regex", rt)
            sct.setEL("serverSideFormValidation", Boolean.FALSE) // TODO impl
            sct.setEL("clientCluster", Caster.toBoolean(ac.getClientCluster()))
            sct.setEL("sessionCluster", Caster.toBoolean(ac.getSessionCluster()))
            sct.setEL("invokeImplicitAccessor", Caster.toBoolean(ac.getTriggerComponentDataMember()))
            sct.setEL("triggerDataMember", Caster.toBoolean(ac.getTriggerComponentDataMember()))
            sct.setEL("sameformfieldsasarray", Caster.toBoolean(ac.getSameFieldAsArray(Scope.SCOPE_FORM)))
            sct.setEL("sameurlfieldsasarray", Caster.toBoolean(ac.getSameFieldAsArray(Scope.SCOPE_URL)))
            var ds: Object = ac.getDefDataSource()
            ds = if (ds is DataSource) _call(ds as DataSource) else Caster.toString(ds, null)
            sct.setEL(KeyConstants._datasource, ds)
            sct.setEL("defaultDatasource", ds)
            val src: Resource = ac.getSource()
            if (src != null) sct.setEL(KeyConstants._source, src.getAbsolutePath())

            // orm
            if (ac.isORMEnabled()) {
                val conf: ORMConfiguration = ac.getORMConfiguration()
                if (conf != null) sct.setEL(KeyConstants._orm, conf.toStruct())
            }
            // s3
            val props: Properties = ac.getS3()
            if (props != null) {
                sct.setEL(KeyConstants._s3, props.toStruct())
            }

            // ws settings
            try {
                val wssettings: Struct = StructImpl(Struct.TYPE_LINKED)
                wssettings.setEL(KeyConstants._type, AppListenerUtil.toWSType(ac.getWSType(), (ThreadLocalPageContext.getConfig(pc) as ConfigWebPro).getWSHandler().getTypeAsString()))
                sct.setEL("wssettings", wssettings)
            } catch (e: Exception) {
            } // in case the extension is not loaded this will fail // TODO check if the extension is installed
            // query
            run {
                val query: Struct = StructImpl(Struct.TYPE_LINKED)
                query.setEL("varusage", AppListenerUtil.toVariableUsage(acs.getQueryVarUsage(), "ignore"))
                query.setEL("psq", acs.getQueryPSQ())
                sct.setEL("query", query)
            }

            // datasources
            val _sources: Struct = StructImpl(Struct.TYPE_LINKED)
            sct.setEL(KeyConstants._datasources, _sources)
            val sources: Array<DataSource?> = ac.getDataSources()
            if (!ArrayUtil.isEmpty(sources)) {
                for (i in sources.indices) {
                    _sources.setEL(KeyImpl.init(sources[i].getName()), _call(sources[i]))
                }
            }

            // logs
            val _logs: Struct = StructImpl(Struct.TYPE_LINKED)
            sct.setEL("logs", _logs)
            if (ac is ApplicationContextSupport) {
                val it: Iterator<Key?> = acs.getLogNames().iterator()
                var name: Key?
                while (it.hasNext()) {
                    name = it.next()
                    _logs.setEL(name, acs.getLogMetaData(name.getString()))
                }
            }
            val log4j: Struct = StructImpl(Struct.TYPE_LINKED)
            log4j.setEL(KeyConstants._version, (pc.getConfig() as ConfigWebPro).getLogEngine().getVersion())
            sct.setEL("log4j", log4j)

            // mails
            val _mails: Array = ArrayImpl()
            sct.setEL("mails", _mails)
            if (ac is ApplicationContextSupport) {
                val servers: Array<Server?> = acs.getMailServers()
                var s: Struct?
                var srv: Server?
                if (servers != null) {
                    for (i in servers.indices) {
                        srv = servers[i]
                        s = StructImpl(Struct.TYPE_LINKED)
                        _mails.appendEL(s)
                        s.setEL(KeyConstants._host, srv.getHostName())
                        s.setEL(KeyConstants._port, srv.getPort())
                        if (!StringUtil.isEmpty(srv.getUsername())) s.setEL(KeyConstants._username, srv.getUsername())
                        if (!StringUtil.isEmpty(srv.getPassword())) s.setEL(KeyConstants._password, srv.getPassword())
                        s.setEL(KeyConstants._readonly, srv.isReadOnly())
                        s.setEL("ssl", srv.isSSL())
                        s.setEL("tls", srv.isTLS())
                        if (srv is ServerImpl) {
                            val srvi: ServerImpl? = srv as ServerImpl?
                            s.setEL("lifeTimespan", TimeSpanImpl.fromMillis(srvi.getLifeTimeSpan()))
                            s.setEL("idleTimespan", TimeSpanImpl.fromMillis(srvi.getIdleTimeSpan()))
                        }
                    }
                }
            }

            // serialization
            if (ac is ApplicationContextSupport) {
                val ser: Struct = StructImpl(Struct.TYPE_LINKED)
                sct.setEL("serialization", acs.getSerializationSettings().toStruct())
            }

            // tag
            val tags: Map<Key?, Map<Collection.Key?, Object?>?> = ac.getTagAttributeDefaultValues(pc)
            if (tags != null) {
                val tag: Struct = StructImpl(Struct.TYPE_LINKED)
                val it: Iterator<Entry<Key?, Map<Collection.Key?, Object?>?>?> = tags.entrySet().iterator()
                var e: Entry<Collection.Key?, Map<Collection.Key?, Object?>?>?
                var iit: Iterator<Entry<Collection.Key?, Object?>?>
                var ee: Entry<Collection.Key?, Object?>?
                var tmp: Struct?
                while (it.hasNext()) {
                    e = it.next()
                    iit = e.getValue().entrySet().iterator()
                    tmp = StructImpl(Struct.TYPE_LINKED)
                    while (iit.hasNext()) {
                        ee = iit.next()
                        // lib.getTagByClassName(ee.getKey());
                        tmp.setEL(ee.getKey(), ee.getValue())
                    }
                    tag.setEL(e.getKey(), tmp)
                }
                sct.setEL(KeyConstants._tag, tag)
            }

            // cache
            val `fun`: String = ac.getDefaultCacheName(Config.CACHE_TYPE_FUNCTION)
            val obj: String = ac.getDefaultCacheName(Config.CACHE_TYPE_OBJECT)
            val qry: String = ac.getDefaultCacheName(Config.CACHE_TYPE_QUERY)
            val res: String = ac.getDefaultCacheName(Config.CACHE_TYPE_RESOURCE)
            val tmp: String = ac.getDefaultCacheName(Config.CACHE_TYPE_TEMPLATE)
            val inc: String = ac.getDefaultCacheName(Config.CACHE_TYPE_INCLUDE)
            val htt: String = ac.getDefaultCacheName(Config.CACHE_TYPE_HTTP)
            val fil: String = ac.getDefaultCacheName(Config.CACHE_TYPE_FILE)
            val wse: String = ac.getDefaultCacheName(Config.CACHE_TYPE_WEBSERVICE)

            // cache connections
            val conns: Struct = StructImpl(Struct.TYPE_LINKED)
            if (ac is ApplicationContextSupport) {
                val names: Array<Key?> = acs.getCacheConnectionNames()
                for (name in names) {
                    val data: CacheConnection = acs.getCacheConnection(name.getString(), null)
                    val _sct: Struct = StructImpl(Struct.TYPE_LINKED)
                    conns.setEL(name, _sct)
                    _sct.setEL(KeyConstants._custom, data.getCustom())
                    _sct.setEL(KeyConstants._storage, data.isStorage())
                    val cd: ClassDefinition = data.getClassDefinition()
                    if (cd != null) {
                        _sct.setEL(KeyConstants._class, cd.getClassName())
                        if (!StringUtil.isEmpty(cd.getName())) _sct.setEL(KeyConstants._bundleName, cd.getClassName())
                        if (cd.getVersion() != null) _sct.setEL(KeyConstants._bundleVersion, cd.getVersionAsString())
                    }
                }
            }
            if (!conns.isEmpty() || `fun` != null || obj != null || qry != null || res != null || tmp != null || inc != null || htt != null || fil != null || wse != null) {
                val cache: Struct = StructImpl(Struct.TYPE_LINKED)
                sct.setEL(KeyConstants._cache, cache)
                if (`fun` != null) cache.setEL(KeyConstants._function, `fun`)
                if (obj != null) cache.setEL(KeyConstants._object, obj)
                if (qry != null) cache.setEL(KeyConstants._query, qry)
                if (res != null) cache.setEL(KeyConstants._resource, res)
                if (tmp != null) cache.setEL(KeyConstants._template, tmp)
                if (inc != null) cache.setEL(KeyConstants._include, inc)
                if (htt != null) cache.setEL(KeyConstants._http, htt)
                if (fil != null) cache.setEL(KeyConstants._file, fil)
                if (wse != null) cache.setEL(KeyConstants._webservice, wse)
                if (conns != null) cache.setEL(KeyConstants._connections, conns)
            }

            // java settings
            val js: JavaSettings = ac.getJavaSettings()
            val jsSct = StructImpl(Struct.TYPE_LINKED)
            jsSct.put("loadCFMLClassPath", js.loadCFMLClassPath())
            jsSct.put("reloadOnChange", js.reloadOnChange())
            jsSct.put("watchInterval", Double.valueOf(js.watchInterval()))
            jsSct.put("watchExtensions", ListUtil.arrayToList(js.watchedExtensions(), ","))
            val reses: Array<Resource?> = js.getResources()
            val sb = StringBuilder()
            for (i in reses.indices) {
                if (i > 0) sb.append(',')
                sb.append(reses[i].getAbsolutePath())
            }
            jsSct.put("loadCFMLClassPath", sb.toString())
            sct.put("javaSettings", jsSct)
            // REST Settings
            // MUST
            if (cfc != null) {
                sct.setEL(KeyConstants._component, cfc.getPageSource().getDisplayPath())
                try {
                    val cw: ComponentSpecificAccess = ComponentSpecificAccess.toComponentSpecificAccess(Component.ACCESS_PRIVATE, cfc)
                    val it: Iterator<Key?> = cw.keyIterator()
                    var key: Collection.Key?
                    var value: Object
                    while (it.hasNext()) {
                        key = it.next()
                        value = cw.get(key)
                        if (suppressFunctions && value is UDF) continue
                        if (onlySupported) continue
                        if (!sct.containsKey(key)) sct.setEL(key, value)
                    }
                } catch (e: PageException) {
                    LogUtil.log(pc, GetApplicationSettings::class.java.getName(), e)
                }
            }
            // application tag custom attributes
            if (ac is ClassicApplicationContext) {
                val attrs: Map<Key?, Object?> = (ac as ClassicApplicationContext).getCustomAttributes()
                if (attrs != null) {
                    val it: Iterator<Entry<Key?, Object?>?> = attrs.entrySet().iterator()
                    var e: Entry<Key?, Object?>?
                    while (it.hasNext()) {
                        e = it.next()
                        if (suppressFunctions && e.getValue() is UDF) continue
                        if (onlySupported) continue
                        if (!sct.containsKey(e.getKey())) sct.setEL(e.getKey(), e.getValue())
                    }
                }
            }
            return sct
        }

        private fun _call(source: DataSource?): Struct? {
            val s: Struct = StructImpl(Struct.TYPE_LINKED)
            s.setEL(KeyConstants._class, source.getClassDefinition().getClassName())
            s.setEL(KeyConstants._bundleName, source.getClassDefinition().getName())
            s.setEL(KeyConstants._bundleVersion, source.getClassDefinition().getVersionAsString())
            if (source.getConnectionLimit() >= 0) s.setEL(AppListenerUtil.CONNECTION_LIMIT, Caster.toDouble(source.getConnectionLimit()))
            if (source.getConnectionTimeout() !== 1) s.setEL(AppListenerUtil.CONNECTION_TIMEOUT, Caster.toDouble(source.getConnectionTimeout()))
            s.setEL(AppListenerUtil.CONNECTION_STRING, source.getDsnTranslated())
            if (source.getMetaCacheTimeout() !== 60000) s.setEL(AppListenerUtil.META_CACHE_TIMEOUT, Caster.toDouble(source.getMetaCacheTimeout()))
            s.setEL(KeyConstants._username, source.getUsername())
            s.setEL(KeyConstants._password, source.getPassword())
            if (source.getTimeZone() != null) s.setEL(KeyConstants._timezone, source.getTimeZone().getID())
            if (source.isBlob()) s.setEL(AppListenerUtil.BLOB, source.isBlob())
            if (source.isClob()) s.setEL(AppListenerUtil.CLOB, source.isClob())
            if (source.isReadOnly()) s.setEL(KeyConstants._readonly, source.isReadOnly())
            if (source.isStorage()) s.setEL(KeyConstants._storage, source.isStorage())
            s.setEL(KeyConstants._validate, source.validate())
            if (source is DataSourcePro) {
                val dsp: DataSourcePro? = source as DataSourcePro?
                if (dsp.isRequestExclusive()) s.setEL("requestExclusive", dsp.isRequestExclusive())
                if (dsp.isRequestExclusive()) s.setEL("alwaysResetConnections", dsp.isAlwaysResetConnections())
                val res: Object = TagListener.toCFML(dsp.getListener(), null)
                if (res != null) s.setEL("listener", res)
                if (dsp.getLiveTimeout() !== 1) s.setEL(AppListenerUtil.LIVE_TIMEOUT, Caster.toDouble(dsp.getLiveTimeout()))
            }
            if (source is DataSourceImpl) {
                val di: DataSourceImpl? = source as DataSourceImpl?
                s.setEL("literalTimestampWithTSOffset", Boolean.valueOf(di.getLiteralTimestampWithTSOffset()))
                s.setEL("alwaysSetTimeout", Boolean.valueOf(di.getAlwaysSetTimeout()))
                s.setEL("dbdriver", Caster.toString(di.getDbDriver(), ""))
            }
            return s
        }

        private fun toArray(mappings: Array<Mapping?>?): Array? {
            val arr: Array = ArrayImpl()
            if (mappings != null) {
                var str: String
                var sct: Struct?
                var m: Mapping?
                for (i in mappings.indices) {
                    m = mappings[i]
                    sct = StructImpl()
                    // physical
                    str = m.getStrPhysical()
                    if (!StringUtil.isEmpty(str, true)) sct.setEL("physical", str.trim())
                    // archive
                    str = m.getStrArchive()
                    if (!StringUtil.isEmpty(str, true)) sct.setEL("archive", str.trim())
                    // primary
                    sct.setEL("primary", if (m.isPhysicalFirst()) "physical" else "archive")
                    arr.appendEL(sct)
                }
            }
            return arr
        }

        private fun toStruct(mappings: Array<Mapping?>?): Struct? {
            val sct: Struct = StructImpl(Struct.TYPE_LINKED)
            if (mappings != null) for (i in mappings.indices) {
                sct.setEL(KeyImpl.init(mappings[i].getVirtual()), mappings[i].getStrPhysical())
            }
            return sct
        }
    }
}