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
package tachyon.runtime.listener

import java.util.ArrayList

object AppListenerUtil {
    val ACCESS_KEY_ID: Collection.Key? = KeyImpl.getInstance("accessKeyId")
    val AWS_SECRET_KEY: Collection.Key? = KeyImpl.getInstance("awsSecretKey")
    val SECRET_KEY: Collection.Key? = KeyImpl.getInstance("secretKey")
    val DEFAULT_LOCATION: Collection.Key? = KeyImpl.getInstance("defaultLocation")
    val ACL: Collection.Key? = KeyConstants._acl
    val CONNECTION_STRING: Collection.Key? = KeyConstants._connectionString
    val BLOB: Collection.Key? = KeyImpl.getInstance("blob")
    val CLOB: Collection.Key? = KeyImpl.getInstance("clob")
    val CONNECTION_LIMIT: Collection.Key? = KeyImpl.getInstance("connectionLimit")
    val CONNECTION_TIMEOUT: Collection.Key? = KeyImpl.getInstance("connectionTimeout")
    val IDLE_TIMEOUT: Collection.Key? = KeyImpl.getInstance("idleTimeout")
    val LIVE_TIMEOUT: Collection.Key? = KeyImpl.getInstance("liveTimeout")
    val META_CACHE_TIMEOUT: Collection.Key? = KeyImpl.getInstance("metaCacheTimeout")
    val ALLOW: Collection.Key? = KeyImpl.getInstance("allow")
    val DISABLE_UPDATE: Collection.Key? = KeyImpl.getInstance("disableUpdate")
    private val FIVE_MINUTES: TimeSpan? = TimeSpanImpl(0, 0, 5, 0)
    private val ONE_MINUTE: TimeSpan? = TimeSpanImpl(0, 0, 1, 0)
    @Throws(PageException::class)
    fun getApplicationPage(pc: PageContext?, requestedPage: PageSource?, filename: String?, mode: Int, type: Int): Page? {
        val res: Resource = requestedPage.getPhyscalFile()
        if (res != null) {
            val ps: PageSource = (pc.getConfig() as ConfigPro).getApplicationPageSource(pc, res.getParent(), filename, mode, null)
            if (ps != null) {
                if (ps.exists()) return ps.loadPage(pc, false, null)
            }
        }
        val p: Page
        p = if (mode == ApplicationListener.MODE_CURRENT) getApplicationPageCurrent(pc, requestedPage, filename) else if (mode == ApplicationListener.MODE_ROOT) getApplicationPageRoot(pc, filename) else getApplicationPageCurr2Root(pc, requestedPage, filename)
        if (res != null && p != null) (pc.getConfig() as ConfigPro).putApplicationPageSource(requestedPage.getPhyscalFile().getParent(), p.getPageSource(), filename, mode, isCFC(type))
        return p
    }

    private fun isCFC(type: Int): Boolean {
        return if (type == ApplicationListener.TYPE_CLASSIC) false else true
    }

    @Throws(PageException::class)
    fun getApplicationPageCurrent(pc: PageContext?, requestedPage: PageSource?, filename: String?): Page? {
        val ps: PageSource = requestedPage.getRealPage(filename)
        if (ps.exists()) ps.loadPage(pc, false)
        return null
    }

    @Throws(PageException::class)
    fun getApplicationPageRoot(pc: PageContext?, filename: String?): Page? {
        val ps: PageSource = (pc as PageContextImpl?).getPageSource("/".concat(filename))
        return if (ps.exists()) ps.loadPage(pc, false) else null
    }

    @Throws(PageException::class)
    fun getApplicationPageCurr2Root(pc: PageContext?, requestedPage: PageSource?, filename: String?): Page? {
        var ps: PageSource = requestedPage.getRealPage(filename)
        if (ps.exists()) return ps.loadPage(pc, false)
        val arr: Array = tachyon.runtime.type.util.ListUtil.listToArrayRemoveEmpty(requestedPage.getRealpathWithVirtual(), "/")
        // Config config = pc.getConfig();
        for (i in arr.size() - 1 downTo 1) {
            val sb = StringBuilder("/")
            for (y in 1 until i) {
                sb.append(arr.get(y, "") as String)
                sb.append('/')
            }
            sb.append(filename)
            ps = (pc as PageContextImpl?).getPageSource(sb.toString())
            if (ps.exists()) return ps.loadPage(pc, false)
        }
        return null
    }

    fun toStringMode(mode: Int): String? {
        if (mode == ApplicationListener.MODE_CURRENT) return "curr"
        if (mode == ApplicationListener.MODE_ROOT) return "root"
        if (mode == ApplicationListener.MODE_CURRENT2ROOT) return "curr2root"
        return if (mode == ApplicationListener.MODE_CURRENT_OR_ROOT) "currorroot" else "curr2root"
    }

    fun toStringType(listener: ApplicationListener?): String? {
        if (listener is NoneAppListener) return "none" else if (listener is MixedAppListener) return "mixed" else if (listener is ClassicAppListener) return "classic" else if (listener is ModernAppListener) return "modern"
        return ""
    }

    fun toDataSources(config: Config?, o: Object?, defaultValue: Array<DataSource?>?, log: Log?): Array<DataSource?>? {
        return try {
            toDataSources(config, o, log)
        } catch (t: Throwable) {
            ExceptionUtil.rethrowIfNecessary(t)
            defaultValue
        }
    }

    @Throws(PageException::class)
    fun toDataSources(config: Config?, o: Object?, log: Log?): Array<DataSource?>? {
        val sct: Struct = Caster.toStruct(o)
        val it: Iterator<Entry<Key?, Object?>?> = sct.entryIterator()
        var e: Entry<Key?, Object?>?
        val dataSources: MutableList<DataSource?> = ArrayList<DataSource?>()
        while (it.hasNext()) {
            e = it.next()
            dataSources.add(toDataSource(config, e.getKey().getString().trim(), Caster.toStruct(e.getValue()), log))
        }
        return dataSources.toArray(arrayOfNulls<DataSource?>(dataSources.size()))
    }

    @Throws(PageException::class)
    fun toDataSource(config: Config?, name: String?, data: Struct?, log: Log?): DataSource? {
        var user: String? = Caster.toString(data.get(KeyConstants._username, null), null)
        var pass: String? = Caster.toString(data.get(KeyConstants._password, ""), "")
        if (StringUtil.isEmpty(user)) {
            user = null
            pass = null
        } else {
            user = translateValue(user)
            pass = translateValue(pass)
        }

        // listener
        var listener: TagListener? = null
        run {
            val o: Object = data.get(KeyConstants._listener, null)
            if (o != null) listener = Query.toTagListener(o, null)
        }
        var timezone: TimeZone? = null
        val obj: Object = data.get(KeyConstants._timezone, null)
        if (obj != null) timezone = Caster.toTimeZone(obj, null)

        // first check for {class:... , connectionString:...}
        val oConnStr: Object = data.get(CONNECTION_STRING, null)
        if (oConnStr != null) {
            var className: String = Caster.toString(data.get(KeyConstants._class))
            if ("com.microsoft.jdbc.sqlserver.SQLServerDriver".equals(className)) {
                className = "com.microsoft.sqlserver.jdbc.SQLServerDriver"
            }
            val cd: ClassDefinition = ClassDefinitionImpl(className, Caster.toString(data.get(KeyConstants._bundleName, null), null),
                    Caster.toString(data.get(KeyConstants._bundleVersion, null), null), ThreadLocalPageContext.getConfig().getIdentification())
            return try {
                var idle: Int = Caster.toIntValue(data.get(IDLE_TIMEOUT, null), -1)
                if (idle == -1) idle = Caster.toIntValue(data.get(CONNECTION_TIMEOUT, null), 1)
                ApplicationDataSource.getInstance(config, name, cd, Caster.toString(oConnStr), user, pass, listener, Caster.toBooleanValue(data.get(BLOB, null), false),
                        Caster.toBooleanValue(data.get(CLOB, null), false), Caster.toIntValue(data.get(CONNECTION_LIMIT, null), -1), idle,
                        Caster.toIntValue(data.get(LIVE_TIMEOUT, null), 60), Caster.toIntValue(data.get("minIdle", null), 60), Caster.toIntValue(data.get("maxIdle", null), 60),
                        Caster.toIntValue(data.get("maxTotal", null), 60), Caster.toLongValue(data.get(META_CACHE_TIMEOUT, null), 60000L), timezone,
                        Caster.toIntValue(data.get(ALLOW, null), DataSource.ALLOW_ALL), Caster.toBooleanValue(data.get(KeyConstants._storage, null), false),
                        Caster.toBooleanValue(data.get(KeyConstants._readonly, null), false), Caster.toBooleanValue(data.get(KeyConstants._validate, null), false),
                        Caster.toBooleanValue(data.get("requestExclusive", null), false), Caster.toBooleanValue(data.get("alwaysResetConnections", null), false),
                        readliteralTimestampWithTSOffset(data), log)
            } catch (cnfe: Exception) {
                throw Caster.toPageException(cnfe)
            }
        }
        // then for {type:... , host:... , ...}
        val type: String = Caster.toString(data.get(KeyConstants._type))
        val dbt: DataSourceDefintion = DBUtil.getDataSourceDefintionForType(config, type, null)
                ?: throw ApplicationException("no datasource type [$type] found")
        return try {
            var idle: Int = Caster.toIntValue(data.get(IDLE_TIMEOUT, null), -1)
            if (idle == -1) idle = Caster.toIntValue(data.get(CONNECTION_TIMEOUT, null), 1)
            DataSourceImpl(config, name, dbt.classDefinition, Caster.toString(data.get(KeyConstants._host)), dbt.connectionString,
                    Caster.toString(data.get(KeyConstants._database)), Caster.toIntValue(data.get(KeyConstants._port, null), -1), user, pass, listener,
                    Caster.toIntValue(data.get(CONNECTION_LIMIT, null), -1), idle, Caster.toIntValue(data.get(LIVE_TIMEOUT, null), 1),
                    Caster.toIntValue(data.get("minIdle", null), 0), Caster.toIntValue(data.get("maxIdle", null), 0), Caster.toIntValue(data.get("maxTotal", null), 0),
                    Caster.toLongValue(data.get(META_CACHE_TIMEOUT, null), 60000L), Caster.toBooleanValue(data.get(BLOB, null), false),
                    Caster.toBooleanValue(data.get(CLOB, null), false), DataSource.ALLOW_ALL, Caster.toStruct(data.get(KeyConstants._custom, null), null, false),
                    Caster.toBooleanValue(data.get(KeyConstants._readonly, null), false), true, Caster.toBooleanValue(data.get(KeyConstants._storage, null), false), timezone, "",
                    ParamSyntax.toParamSyntax(data, ParamSyntax.DEFAULT), readliteralTimestampWithTSOffset(data), Caster.toBooleanValue(data.get("alwaysSetTimeout", null), false),
                    Caster.toBooleanValue(data.get("requestExclusive", null), false), Caster.toBooleanValue(data.get("alwaysResetConnections", null), false), log)
        } catch (cnfe: Exception) {
            throw Caster.toPageException(cnfe)
        }
    }

    private fun translateValue(str: String?): String? {
        var str = str ?: return null
        str = str.trim()
        if (str.startsWith("{env:") && str.endsWith("}")) {
            val tmp: String = str.substring(5, str.length() - 1)
            return SystemUtil.getSystemPropOrEnvVar(tmp, "")
        }
        return str
    }

    private fun readliteralTimestampWithTSOffset(data: Struct?): Boolean {
        var literalTimestampWithTSOffset: Boolean = Caster.toBoolean(data.get("literalTimestampWithTSOffset", null), null)
        if (literalTimestampWithTSOffset == null) literalTimestampWithTSOffset = Caster.toBoolean(data.get("timestampWithTSOffset", null), null)
        if (literalTimestampWithTSOffset == null) literalTimestampWithTSOffset = Caster.toBoolean(data.get("fulltimestamp", null), null)
        return literalTimestampWithTSOffset ?: false
    }

    fun toMappings(cw: ConfigWeb?, o: Object?, defaultValue: Array<Mapping?>?, source: Resource?): Array<Mapping?>? {
        return try {
            toMappings(cw, o, source)
        } catch (t: Throwable) {
            ExceptionUtil.rethrowIfNecessary(t)
            defaultValue
        }
    }

    @Throws(PageException::class)
    fun toMappings(cw: ConfigWeb?, o: Object?, source: Resource?): Array<Mapping?>? {
        val sct: Struct = Caster.toStruct(o)
        val it: Iterator<Entry<Key?, Object?>?> = sct.entryIterator()
        var e: Entry<Key?, Object?>?
        val mappings: MutableList<Mapping?> = ArrayList<Mapping?>()
        val config: ConfigWebPro? = cw as ConfigWebPro?
        var virtual: String?
        while (it.hasNext()) {
            e = it.next()
            virtual = translateMappingVirtual(e.getKey().getString())
            val md = toMappingData(e.getValue(), source)
            mappings.add(config.getApplicationMapping("application", virtual, md!!.physical, md.archive, md.physicalFirst, false, !md.physicalMatch, !md.archiveMatch))
        }
        return ConfigWebUtil.sort(mappings.toArray(arrayOfNulls<Mapping?>(mappings.size())))
    }

    @Throws(PageException::class)
    private fun toMappingData(value: Object?, source: Resource?): MappingData? {
        val md = MappingData()
        if (Decision.isStruct(value)) {
            val map: Struct = Caster.toStruct(value)

            // allowRelPath
            val allowRelPath: Boolean = Caster.toBooleanValue(map.get("allowRelPath", null), true)

            // physical
            val physical: String = Caster.toString(map.get("physical", null), null)
            if (!StringUtil.isEmpty(physical, true)) {
                translateMappingPhysical(md, physical.trim(), source, allowRelPath, false)
            }

            // archive
            val archive: String = Caster.toString(map.get("archive", null), null)
            if (!StringUtil.isEmpty(archive, true)) {
                translateMappingPhysical(md, archive.trim(), source, allowRelPath, true)
            }
            if (archive == null && physical == null) throw ApplicationException("you must define archive or/and physical!")

            // primary
            md.physicalFirst = true
            // primary is only of interest when both values exists
            if (archive != null && physical != null) {
                val primary: String = Caster.toString(map.get("primary", null), null)
                if (primary != null && primary.trim().equalsIgnoreCase("archive")) md.physicalFirst = false
            } else if (archive != null) md.physicalFirst = false
        } else {
            md.physicalFirst = true
            translateMappingPhysical(md, Caster.toString(value).trim(), source, true, false)
        }
        return md
    }

    private fun translateMappingPhysical(md: MappingData?, path: String?, source: Resource?, allowRelPath: Boolean, isArchive: Boolean) {
        var source: Resource? = source
        if (source == null || !allowRelPath) {
            if (isArchive) md!!.archive = path else md!!.physical = path
            return
        }
        source = source.getParentResource().getRealResource(path)
        if (source.exists()) {
            if (isArchive) {
                md!!.archive = source.getAbsolutePath()
                md.archiveMatch = true
            } else {
                md!!.physical = source.getAbsolutePath()
                md.physicalMatch = true
            }
            return
        }
        if (isArchive) md!!.archive = path else md!!.physical = path
    }

    private fun translateMappingVirtual(virtual: String?): String? {
        var virtual = virtual
        virtual = virtual.replace('\\', '/')
        if (!StringUtil.startsWith(virtual, '/')) virtual = "/".concat(virtual)
        return virtual
    }

    @Throws(PageException::class)
    fun toCustomTagMappings(cw: ConfigWeb?, o: Object?, source: Resource?): Array<Mapping?>? {
        return toMappings(cw, "custom", o, false, source)
    }

    fun toCustomTagMappings(cw: ConfigWeb?, o: Object?, source: Resource?, defaultValue: Array<Mapping?>?): Array<Mapping?>? {
        return try {
            toMappings(cw, "custom", o, false, source)
        } catch (t: Throwable) {
            ExceptionUtil.rethrowIfNecessary(t)
            defaultValue
        }
    }

    @Throws(PageException::class)
    fun toComponentMappings(cw: ConfigWeb?, o: Object?, source: Resource?): Array<Mapping?>? {
        return toMappings(cw, "component", o, true, source)
    }

    fun toComponentMappings(cw: ConfigWeb?, o: Object?, source: Resource?, defaultValue: Array<Mapping?>?): Array<Mapping?>? {
        return try {
            toMappings(cw, "component", o, true, source)
        } catch (t: Throwable) {
            ExceptionUtil.rethrowIfNecessary(t)
            defaultValue
        }
    }

    @Throws(PageException::class)
    private fun toMappings(cw: ConfigWeb?, type: String?, o: Object?, useStructNames: Boolean, source: Resource?): Array<Mapping?>? {
        val config: ConfigWebPro? = cw as ConfigWebPro?
        val array: Array?
        if (o is String) {
            array = ListUtil.listToArrayRemoveEmpty(Caster.toString(o), ',')
        } else if (o is Struct) {
            val sct: Struct? = o as Struct?
            if (useStructNames) {
                val it: Iterator<Entry<Key?, Object?>?> = sct.entryIterator()
                val list: List<Mapping?> = ArrayList<Mapping?>()
                var e: Entry<Key?, Object?>?
                var virtual: String?
                while (it.hasNext()) {
                    e = it.next()
                    virtual = e.getKey().getString()
                    if (virtual.length() === 0) virtual = "/"
                    if (!virtual.startsWith("/")) virtual = "/$virtual"
                    if (!virtual.equals("/") && virtual.endsWith("/")) virtual = virtual.substring(0, virtual.length() - 1)
                    val md = toMappingData(e.getValue(), source)
                    list.add(config.getApplicationMapping(type, virtual, md!!.physical, md.archive, md.physicalFirst, true, !md.physicalMatch, !md.archiveMatch))
                }
                return list.toArray(arrayOfNulls<Mapping?>(list.size()))
            }
            array = ArrayImpl()
            val it: Iterator<Object?> = sct.valueIterator()
            while (it.hasNext()) {
                array.append(it.next())
            }
        } else {
            array = Caster.toArray(o)
        }
        val mappings: Array<MappingImpl?> = arrayOfNulls<MappingImpl?>(array.size())
        for (i in mappings.indices) {
            val md = toMappingData(array.getE(i + 1), source)
            mappings[i] = config.getApplicationMapping(type, "/$i", md!!.physical, md.archive, md.physicalFirst, true, !md.physicalMatch, !md.archiveMatch) as MappingImpl
        }
        return mappings
    }

    fun toLocalMode(mode: Int, defaultValue: String?): String? {
        if (Undefined.MODE_LOCAL_OR_ARGUMENTS_ALWAYS === mode) return "modern"
        return if (Undefined.MODE_LOCAL_OR_ARGUMENTS_ONLY_WHEN_EXISTS === mode) "classic" else defaultValue
    }

    fun toLocalMode(oMode: Object?, defaultValue: Int): Int {
        if (oMode == null) return defaultValue
        if (Decision.isBoolean(oMode)) {
            return if (Caster.toBooleanValue(oMode, false)) Undefined.MODE_LOCAL_OR_ARGUMENTS_ALWAYS else Undefined.MODE_LOCAL_OR_ARGUMENTS_ONLY_WHEN_EXISTS
        }
        val strMode: String = Caster.toString(oMode, null)
        if ("always".equalsIgnoreCase(strMode) || "modern".equalsIgnoreCase(strMode)) return Undefined.MODE_LOCAL_OR_ARGUMENTS_ALWAYS
        return if ("update".equalsIgnoreCase(strMode) || "classic".equalsIgnoreCase(strMode)) Undefined.MODE_LOCAL_OR_ARGUMENTS_ONLY_WHEN_EXISTS else defaultValue
    }

    @Throws(ApplicationException::class)
    fun toLocalMode(strMode: String?): Int {
        val lm = toLocalMode(strMode, -1)
        if (lm != -1) return lm
        throw ApplicationException("invalid localMode definition [$strMode] for tag application, valid values are [classic,modern,true,false]")
    }

    fun toSessionType(type: Short, defaultValue: String?): String? {
        if (type == Config.SESSION_TYPE_APPLICATION) return "cfml"
        return if (type == Config.SESSION_TYPE_JEE) "jee" else defaultValue
    }

    fun toSessionType(str: String?, defaultValue: Short): Short {
        var str = str
        if (!StringUtil.isEmpty(str, true)) {
            str = str.trim().toLowerCase()
            if ("cfml".equals(str)) return Config.SESSION_TYPE_APPLICATION
            if ("j2ee".equals(str)) return Config.SESSION_TYPE_JEE
            if ("cfm".equals(str)) return Config.SESSION_TYPE_APPLICATION
            if ("application".equals(str)) return Config.SESSION_TYPE_APPLICATION
            if ("jee".equals(str)) return Config.SESSION_TYPE_JEE
            if ("j".equals(str)) return Config.SESSION_TYPE_JEE
            if ("c".equals(str)) return Config.SESSION_TYPE_APPLICATION
        }
        return defaultValue
    }

    @Throws(ApplicationException::class)
    fun toSessionType(str: String?): Short {
        val undefined = (-1).toShort()
        val type = toSessionType(str, undefined)
        if (type != undefined) return type
        throw ApplicationException("invalid sessionType definition [$str] for tag application, valid values are [application,jee]")
    }

    fun toS3(sct: Struct?): Properties? {
        var host: String = Caster.toString(sct.get(KeyConstants._host, null), null)
        if (StringUtil.isEmpty(host)) host = Caster.toString(sct.get(KeyConstants._server, null), null)
        var sk: String = Caster.toString(sct.get(AWS_SECRET_KEY, null), null)
        if (StringUtil.isEmpty(sk)) sk = Caster.toString(sct.get(SECRET_KEY, null), null)
        return toS3(Caster.toString(sct.get(ACCESS_KEY_ID, null), null), sk, Caster.toString(sct.get(DEFAULT_LOCATION, null), null), host,
                Caster.toString(sct.get(ACL, null), null), Caster.toTimespan(sct.get(KeyConstants._cache, null), null))
    }

    private fun toS3(accessKeyId: String?, awsSecretKey: String?, defaultLocation: String?, host: String?, acl: String?, cache: TimeSpan?): Properties? {
        val s3 = PropertiesImpl()
        if (!StringUtil.isEmpty(accessKeyId)) s3.setAccessKeyId(accessKeyId)
        if (!StringUtil.isEmpty(awsSecretKey)) s3.setSecretAccessKey(awsSecretKey)
        if (!StringUtil.isEmpty(defaultLocation)) s3.setDefaultLocation(defaultLocation)
        if (!StringUtil.isEmpty(host)) s3.setHost(host)
        if (!StringUtil.isEmpty(acl)) s3.setACL(acl)
        if (cache != null) s3.setCache(cache.getMillis())
        return s3
    }

    @Throws(PageException::class)
    fun setORMConfiguration(pc: PageContext?, ac: ApplicationContext?, sct: Struct?) {
        var sct: Struct? = sct
        if (sct == null) sct = StructImpl()
        val config: ConfigPro = pc.getConfig() as ConfigPro
        val curr: PageSource = pc.getCurrentTemplatePageSource()
        val res: Resource? = if (curr == null) null else curr.getResourceTranslated(pc).getParentResource()
        ac.setORMConfiguration(ORMConfigurationImpl.load(config, ac, sct, res, config.getORMConfig()))

        // datasource
        var o: Object? = sct.get(KeyConstants._datasource, null)
        if (o != null) {
            o = toDefaultDatasource(config, o, ThreadLocalPageContext.getLog(pc, "application"))
            if (o != null) ac.setORMDataSource(o)
        }
    }

    /**
     * translate int definition of script protect to string definition
     *
     * @param scriptProtect
     * @return
     */
    fun translateScriptProtect(scriptProtect: Int): String? {
        if (scriptProtect == ApplicationContext.SCRIPT_PROTECT_NONE) return "none"
        if (scriptProtect == ApplicationContext.SCRIPT_PROTECT_ALL) return "all"
        val arr: Array = ArrayImpl()
        if (scriptProtect and ApplicationContext.SCRIPT_PROTECT_CGI > 0) arr.appendEL("cgi")
        if (scriptProtect and ApplicationContext.SCRIPT_PROTECT_COOKIE > 0) arr.appendEL("cookie")
        if (scriptProtect and ApplicationContext.SCRIPT_PROTECT_FORM > 0) arr.appendEL("form")
        if (scriptProtect and ApplicationContext.SCRIPT_PROTECT_URL > 0) arr.appendEL("url")
        return try {
            ListUtil.arrayToList(arr, ",")
        } catch (e: PageException) {
            "none"
        }
    }

    /**
     * translate string definition of script protect to int definition
     *
     * @param strScriptProtect
     * @return
     */
    fun translateScriptProtect(strScriptProtect: String?): Int {
        var strScriptProtect = strScriptProtect
        strScriptProtect = strScriptProtect.toLowerCase().trim()
        if ("none".equals(strScriptProtect)) return ApplicationContext.SCRIPT_PROTECT_NONE
        if ("no".equals(strScriptProtect)) return ApplicationContext.SCRIPT_PROTECT_NONE
        if ("false".equals(strScriptProtect)) return ApplicationContext.SCRIPT_PROTECT_NONE
        if ("all".equals(strScriptProtect)) return ApplicationContext.SCRIPT_PROTECT_ALL
        if ("true".equals(strScriptProtect)) return ApplicationContext.SCRIPT_PROTECT_ALL
        if ("yes".equals(strScriptProtect)) return ApplicationContext.SCRIPT_PROTECT_ALL
        val arr: Array<String?> = ListUtil.listToStringArray(strScriptProtect, ',')
        var item: String?
        var scriptProtect = 0
        for (i in arr.indices) {
            item = arr[i].trim()
            if ("cgi".equals(item) && scriptProtect and ApplicationContext.SCRIPT_PROTECT_CGI === 0) scriptProtect += ApplicationContext.SCRIPT_PROTECT_CGI else if ("cookie".equals(item) && scriptProtect and ApplicationContext.SCRIPT_PROTECT_COOKIE === 0) scriptProtect += ApplicationContext.SCRIPT_PROTECT_COOKIE else if ("form".equals(item) && scriptProtect and ApplicationContext.SCRIPT_PROTECT_FORM === 0) scriptProtect += ApplicationContext.SCRIPT_PROTECT_FORM else if ("url".equals(item) && scriptProtect and ApplicationContext.SCRIPT_PROTECT_URL === 0) scriptProtect += ApplicationContext.SCRIPT_PROTECT_URL
        }
        return scriptProtect
    }

    fun translateLoginStorage(loginStorage: Int): String? {
        return if (loginStorage == Scope.SCOPE_SESSION) "session" else "cookie"
    }

    fun translateLoginStorage(strLoginStorage: String?, defaultValue: Int): Int {
        var strLoginStorage = strLoginStorage
        strLoginStorage = strLoginStorage.toLowerCase().trim()
        if (strLoginStorage.equals("session")) return Scope.SCOPE_SESSION
        return if (strLoginStorage.equals("cookie")) Scope.SCOPE_COOKIE else defaultValue
    }

    @Throws(ApplicationException::class)
    fun translateLoginStorage(strLoginStorage: String?): Int {
        val ls = translateLoginStorage(strLoginStorage, -1)
        if (ls != -1) return ls
        throw ApplicationException("invalid loginStorage definition [$strLoginStorage], valid values are [session,cookie]")
    }

    @Throws(PageException::class)
    fun toDefaultDatasource(config: Config?, o: Object?, log: Log?): Object? {
        if (Decision.isStruct(o)) {
            val sct: Struct? = o as Struct?

            // fix for Jira ticket LUCEE-1931
            if (sct.size() === 1) {
                val keys: Array<Key?> = CollectionUtil.keys(sct)
                if (keys.size == 1 && keys[0].equalsIgnoreCase(KeyConstants._name)) {
                    return Caster.toString(sct.get(KeyConstants._name))
                }
            }
            return try {
                toDataSource(config, "__default__", sct, log)
            } catch (pe: PageException) {
                // again try fix for Jira ticket LUCEE-1931
                val name: String = Caster.toString(sct.get(KeyConstants._name, null), null)
                if (!StringUtil.isEmpty(name)) return name
                throw pe
            } catch (e: Exception) {
                throw Caster.toPageException(e)
            }
        }
        return Caster.toString(o)
    }

    fun toWSType(wstype: Short, defaultValue: String?): String? {
        if (ApplicationContext.WS_TYPE_AXIS1 === wstype) return "Axis1"
        if (ApplicationContext.WS_TYPE_JAX_WS === wstype) return "JAX-WS"
        return if (ApplicationContext.WS_TYPE_CXF === wstype) "CXF" else defaultValue
    }

    fun toWSType(wstype: String?, defaultValue: Short): Short {
        var wstype: String? = wstype ?: return defaultValue
        wstype = wstype.trim()
        return if ("axis".equalsIgnoreCase(wstype) || "axis1".equalsIgnoreCase(wstype)) ApplicationContext.WS_TYPE_AXIS1 else defaultValue
        /*
		 * if("jax".equalsIgnoreCase(wstype) || "jaxws".equalsIgnoreCase(wstype) ||
		 * "jax-ws".equalsIgnoreCase(wstype)) return ApplicationContextPro.WS_TYPE_JAX_WS;
		 * if("cxf".equalsIgnoreCase(wstype)) return ApplicationContextPro.WS_TYPE_CXF;
		 */
    }

    fun toCachedWithinType(type: String?, defaultValue: Int): Int {
        var type = type
        if (StringUtil.isEmpty(type, true)) return defaultValue
        type = type.trim().toLowerCase()
        if ("function".equalsIgnoreCase(type)) return Config.CACHEDWITHIN_FUNCTION
        if ("udf".equalsIgnoreCase(type)) return Config.CACHEDWITHIN_FUNCTION
        if ("include".equalsIgnoreCase(type)) return Config.CACHEDWITHIN_INCLUDE
        if ("query".equalsIgnoreCase(type)) return Config.CACHEDWITHIN_QUERY
        if ("resource".equalsIgnoreCase(type)) return Config.CACHEDWITHIN_RESOURCE
        if ("http".equalsIgnoreCase(type)) return Config.CACHEDWITHIN_HTTP
        if ("file".equalsIgnoreCase(type)) return Config.CACHEDWITHIN_FILE
        return if ("webservice".equalsIgnoreCase(type)) Config.CACHEDWITHIN_WEBSERVICE else defaultValue
    }

    fun toCachedWithinType(type: Int, defaultValue: String?): String? {
        if (type == Config.CACHEDWITHIN_FUNCTION) return "function"
        if (type == Config.CACHEDWITHIN_INCLUDE) return "include"
        if (type == Config.CACHEDWITHIN_QUERY) return "query"
        if (type == Config.CACHEDWITHIN_RESOURCE) return "resource"
        if (type == Config.CACHEDWITHIN_HTTP) return "http"
        if (type == Config.CACHEDWITHIN_FILE) return "file"
        return if (type == Config.CACHEDWITHIN_WEBSERVICE) "webservice" else defaultValue
    }

    @Throws(ApplicationException::class)
    fun toWSType(wstype: String?): Short {
        val str = ""
        val cs: KeyImpl = object : KeyImpl(str) {
            @Override
            fun getString(): String? {
                return null
            }
        }
        val wst = toWSType(wstype, (-1).toShort())
        if (wst.toInt() != -1) return wst
        throw ApplicationException("invalid webservice type [$wstype], valid values are [axis1]")
        // throw new ApplicationException("invalid webservice type ["+wstype+"], valid values are
        // [axis1,jax-ws,cxf]");
    }

    fun toSessionCookie(config: ConfigWeb?, data: Struct?): SessionCookieData? {
        return if (data == null) SessionCookieDataImpl.DEFAULT else SessionCookieDataImpl(Caster.toBooleanValue(data.get(KeyConstants._httponly, null), SessionCookieDataImpl.DEFAULT!!.isHttpOnly()),
                Caster.toBooleanValue(data.get(KeyConstants._secure, null), SessionCookieDataImpl.DEFAULT!!.isSecure()),
                toTimespan(data.get(KeyConstants._timeout, null), SessionCookieDataImpl.DEFAULT!!.getTimeout()),
                Caster.toString(data.get(KeyConstants._domain, null), SessionCookieDataImpl.DEFAULT!!.getDomain()),
                Caster.toBooleanValue(data.get(DISABLE_UPDATE, null), SessionCookieDataImpl.DEFAULT!!.isDisableUpdate()),
                SessionCookieDataImpl.toSamesite(Caster.toString(data.get(KeyConstants._SameSite, null), null), SessionCookieDataImpl.DEFAULT!!.getSamesite()),
                Caster.toString(data.get(KeyConstants._path, null), SessionCookieDataImpl.DEFAULT!!.getPath())
        )
    }

    fun toAuthCookie(config: ConfigWeb?, data: Struct?): AuthCookieData? {
        return if (data == null) AuthCookieDataImpl.DEFAULT else AuthCookieDataImpl(toTimespan(data.get(KeyConstants._timeout, null), AuthCookieDataImpl.DEFAULT!!.getTimeout()),
                Caster.toBooleanValue(data.get(DISABLE_UPDATE, null), AuthCookieDataImpl.DEFAULT!!.isDisableUpdate()))
    }

    private fun toTimespan(obj: Object?, defaultValue: TimeSpan?): TimeSpan? {
        if (obj !is TimeSpan) {
            val tmp: Double = Caster.toDouble(obj, null)
            if (tmp != null && tmp.doubleValue() <= 0.0) return TimeSpanImpl.fromMillis(CookieImpl.NEVER * 1000)
        }
        return Caster.toTimespan(obj, defaultValue)
    }

    fun toMailServers(config: Config?, data: Array?, defaultValue: Server?): Array<Server?>? {
        val list: List<Server?> = ArrayList<Server?>()
        if (data != null) {
            val it: Iterator<Object?> = data.valueIterator()
            var sct: Struct
            var se: Server?
            while (it.hasNext()) {
                sct = Caster.toStruct(it.next(), null)
                if (sct == null) continue
                se = toMailServer(config, sct, null)
                if (se != null) list.add(se)
            }
        }
        return list.toArray(arrayOfNulls<Server?>(list.size()))
    }

    fun toMailServer(config: Config?, data: Struct?, defaultValue: Server?): Server? {
        var hostName: String = Caster.toString(data.get(KeyConstants._host, null), null)
        if (StringUtil.isEmpty(hostName, true)) hostName = Caster.toString(data.get(KeyConstants._server, null), null)
        if (StringUtil.isEmpty(hostName, true)) return defaultValue
        val port: Int = Caster.toIntValue(data.get(KeyConstants._port, null), 25)
        var username: String? = Caster.toString(data.get(KeyConstants._username, null), null)
        if (StringUtil.isEmpty(username, true)) username = Caster.toString(data.get(KeyConstants._user, null), null)
        var password: String? = ConfigWebUtil.decrypt(Caster.toString(data.get(KeyConstants._password, null), null))
        username = translateValue(username)
        password = translateValue(password)
        var lifeTimespan: TimeSpan = Caster.toTimespan(data.get("lifeTimespan", null), null)
        if (lifeTimespan == null) lifeTimespan = Caster.toTimespan(data.get("life", null), FIVE_MINUTES)
        var idleTimespan: TimeSpan = Caster.toTimespan(data.get("idleTimespan", null), null)
        if (idleTimespan == null) idleTimespan = Caster.toTimespan(data.get("idle", null), ONE_MINUTE)
        var value: Object = data.get("tls", null)
        if (value == null) value = data.get("useTls", null)
        val tls: Boolean = Caster.toBooleanValue(value, false)
        value = data.get("ssl", null)
        if (value == null) value = data.get("useSsl", null)
        val ssl: Boolean = Caster.toBooleanValue(value, false)
        return ServerImpl(-1, hostName, port, username, password, lifeTimespan.getMillis(), idleTimespan.getMillis(), tls, ssl, false, ServerImpl.TYPE_LOCAL) // MUST improve
        // store
        // connection
        // somehow
    }

    fun toFTP(sct: Struct?): FTPConnectionData? {
        // username
        var o: Object = sct.get(KeyConstants._username, null)
        if (o == null) o = sct.get(KeyConstants._user, null)
        var user: String? = Caster.toString(o, null)
        if (StringUtil.isEmpty(user)) user = null

        // password
        o = sct.get(KeyConstants._password, null)
        if (o == null) o = sct.get(KeyConstants._pass, null)
        var pass: String? = Caster.toString(o, null)
        if (StringUtil.isEmpty(pass)) pass = if (user != null) "" else null
        user = translateValue(user)
        pass = translateValue(pass)

        // host
        o = sct.get(KeyConstants._host, null)
        if (o == null) o = sct.get(KeyConstants._server, null)
        var host: String? = Caster.toString(o, null)
        if (StringUtil.isEmpty(host)) host = null

        // port
        o = sct.get(KeyConstants._port, null)
        val port: Int = Caster.toIntValue(o, 0)
        return FTPConnectionData(host, user, pass, port)
    }

    fun loadResources(config: Config?, ac: ApplicationContext?, obj: Object?, onlyDir: Boolean): List<Resource?>? {
        var obj: Object? = obj
        var res: Resource?
        if (!Decision.isArray(obj)) {
            val list: String = Caster.toString(obj, null)
            if (!StringUtil.isEmpty(list)) {
                obj = ListUtil.listToArray(list, ',')
            }
        }
        if (Decision.isArray(obj)) {
            val arr: Array = Caster.toArray(obj, null)
            val list: MutableList<Resource?> = ArrayList<Resource?>()
            val it: Iterator<Object?> = arr.valueIterator()
            while (it.hasNext()) {
                try {
                    val path: String = Caster.toString(it.next(), null) ?: continue
                    res = toResourceExisting(config, ac, path, onlyDir)
                    if (res != null) list.add(res)
                } catch (t: Throwable) {
                    ExceptionUtil.rethrowIfNecessary(t)
                }
            }
            return list
        }
        return null
    }

    fun toResourceExisting(config: Config?, ac: ApplicationContext?, obj: Object?, onlyDir: Boolean): Resource? {
        // Resource root = config.getRootDirectory();
        var ac: ApplicationContext? = ac
        var path: String? = Caster.toString(obj, null)
        if (StringUtil.isEmpty(path, true)) return null
        path = path.trim()
        var res: Resource
        val pc: PageContext = ThreadLocalPageContext.get()

        // first check relative to application . cfc
        if (pc != null) {
            if (ac == null) ac = pc.getApplicationContext()

            // abs path
            if (path.startsWith("/")) {
                val cwi: ConfigWebPro? = config as ConfigWebPro?
                val ps: PageSource = cwi.getPageSourceExisting(pc, if (ac == null) null else ac.getMappings(), path, false, false, true, false)
                if (ps != null) {
                    res = ps.getResource()
                    if (res != null && (!onlyDir || res.isDirectory())) return res
                }
            } else {
                val src: Resource? = if (ac != null) ac.getSource() else null
                if (src != null) {
                    res = src.getParentResource().getRealResource(path)
                    if (res != null && (!onlyDir || res.isDirectory())) return res
                } else {
                    res = ResourceUtil.toResourceNotExisting(pc, path)
                    if (res != null && (!onlyDir || res.isDirectory())) return res
                }
            }
        }

        // then in the webroot
        res = config.getRootDirectory().getRealResource(path)
        if (res != null && (!onlyDir || res.isDirectory())) return res

        // then absolute
        res = ResourceUtil.toResourceNotExisting(config, path)
        return if (res != null && (!onlyDir || res.isDirectory())) res else null
    }

    @Throws(ApplicationException::class)
    fun toVariableUsage(str: String?): Int {
        val i = toVariableUsage(str, 0)
        if (i != 0) return i
        throw ApplicationException("variable usage [$str] is invalid, valid values are [ignore,warn,error]")
    }

    fun toVariableUsage(str: String?, defaultValue: Int): Int {
        var str = str ?: return defaultValue
        str = str.trim().toLowerCase()
        if ("ignore".equals(str)) return ConfigPro.QUERY_VAR_USAGE_IGNORE
        if ("warn".equals(str)) return ConfigPro.QUERY_VAR_USAGE_WARN
        if ("warning".equals(str)) return ConfigPro.QUERY_VAR_USAGE_WARN
        if ("error".equals(str)) return ConfigPro.QUERY_VAR_USAGE_ERROR
        val b: Boolean = Caster.toBoolean(str, null)
        return if (b != null) {
            if (b.booleanValue()) ConfigPro.QUERY_VAR_USAGE_ERROR else ConfigPro.QUERY_VAR_USAGE_IGNORE
        } else defaultValue
    }

    fun toVariableUsage(i: Int, defaultValue: String?): String? {
        if (ConfigPro.QUERY_VAR_USAGE_IGNORE === i) return "ignore"
        if (ConfigPro.QUERY_VAR_USAGE_WARN === i) return "warn"
        return if (ConfigPro.QUERY_VAR_USAGE_ERROR === i) "error" else defaultValue
    }

    fun toClassName(sct: Struct?): String? {
        if (sct == null) return null
        var className: String = Caster.toString(sct.get("class", null), null)
        if (StringUtil.isEmpty(className)) className = Caster.toString(sct.get("classname", null), null)
        if (StringUtil.isEmpty(className)) className = Caster.toString(sct.get("class-name", null), null)
        return if (StringUtil.isEmpty(className)) null else className
    }

    fun toBundleName(sct: Struct?): String? {
        if (sct == null) return null
        var name: String = Caster.toString(sct.get("bundlename", null), null)
        if (StringUtil.isEmpty(name)) name = Caster.toString(sct.get("bundle-name", null), null)
        if (StringUtil.isEmpty(name)) name = Caster.toString(sct.get("name", null), null)
        return if (StringUtil.isEmpty(name)) null else name
    }

    fun toBundleVersion(sct: Struct?): Version? {
        if (sct == null) return null
        var version: Version = OSGiUtil.toVersion(Caster.toString(sct.get("bundleversion", null), null), null)
        if (version == null) version = OSGiUtil.toVersion(Caster.toString(sct.get("bundle-version", null), null), null)
        if (version == null) version = OSGiUtil.toVersion(Caster.toString(sct.get("version", null), null), null)
        return version
    }

    fun getPreciseMath(pc: PageContext?, config: Config?): Boolean {
        var pc: PageContext? = pc
        var config: Config? = config
        pc = ThreadLocalPageContext.get(pc)
        if (pc != null) return (pc.getApplicationContext() as ApplicationContextSupport)!!.getPreciseMath()
        config = ThreadLocalPageContext.getConfig(config)
        return if (config != null) (config as ConfigPro?).getPreciseMath() else false
    }

    private class MappingData {
        var archiveMatch = false
        var physicalMatch = false
        val physical: String? = null
        val archive: String? = null
        val physicalFirst = false
    }
}