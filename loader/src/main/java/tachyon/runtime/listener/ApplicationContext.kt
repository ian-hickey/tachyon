/**
 *
 * Copyright (c) 2014, the Railo Company Ltd. All rights reserved.
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

import java.io.Serializable

/**
 * DTO Interface for Application Context data (defined by tag application)
 */
interface ApplicationContext : Serializable {
    /**
     * @return Returns the applicationTimeout.
     */
    fun getApplicationTimeout(): TimeSpan?

    /**
     * @return Returns the loginStorage.
     */
    fun getLoginStorage(): Int

    /**
     * @return Returns the name.
     */
    fun getName(): String?

    /**
     * @return Returns the sessionTimeout.
     */
    fun getSessionTimeout(): TimeSpan?

    /**
     * @return Returns the setClientCookies.
     */
    fun isSetClientCookies(): Boolean

    /**
     * @return Returns the setClientManagement.
     */
    fun isSetClientManagement(): Boolean

    /**
     * @return Returns the setDomainCookies.
     */
    fun isSetDomainCookies(): Boolean

    /**
     * @return Returns the setSessionManagement.
     */
    fun isSetSessionManagement(): Boolean

    /**
     * @return Returns the clientstorage.
     */
    fun getClientstorage(): String?

    /**
     * @return if application context has a name
     */
    fun hasName(): Boolean

    /**
     * @return return script protect setting
     */
    fun getScriptProtect(): Int
    fun getMappings(): Array<Mapping?>?
    fun getCustomTagMappings(): Array<Mapping?>?
    fun getSecureJsonPrefix(): String?
    fun getSecureJson(): Boolean

    /**
     * @return Default Datasource
     */
    @Deprecated
    @Deprecated("use instead getDefDataSource()")
    fun getDefaultDataSource(): String?
    fun isORMEnabled(): Boolean

    /**
     * @return ORM Datasource
     */
    @Deprecated
    @Deprecated("use instead getDefaultDataSource()")
    fun getORMDatasource(): String?
    fun getORMConfiguration(): ORMConfiguration?
    fun getS3(): Properties?
    fun getLocalMode(): Int
    fun getSessionstorage(): String?
    fun getClientTimeout(): TimeSpan?
    fun getSessionType(): Short
    fun getSessionCluster(): Boolean
    fun getClientCluster(): Boolean
    fun getComponentMappings(): Array<Mapping?>?
    fun setApplicationTimeout(applicationTimeout: TimeSpan?)
    fun setSessionTimeout(sessionTimeout: TimeSpan?)
    fun setClientTimeout(clientTimeout: TimeSpan?)
    fun setClientstorage(clientstorage: String?)
    fun setSessionstorage(sessionstorage: String?)
    fun setCustomTagMappings(customTagMappings: Array<Mapping?>?)
    fun setComponentMappings(componentMappings: Array<Mapping?>?)
    fun setMappings(mappings: Array<Mapping?>?)
    fun setLoginStorage(loginstorage: Int)
    fun setDefaultDataSource(datasource: String?)
    fun setScriptProtect(scriptrotect: Int)
    fun setSecureJson(secureJson: Boolean)
    fun setSecureJsonPrefix(secureJsonPrefix: String?)
    fun setSetClientCookies(setClientCookies: Boolean)
    fun setSetClientManagement(setClientManagement: Boolean)
    fun setSetDomainCookies(setDomainCookies: Boolean)
    fun setSetSessionManagement(setSessionManagement: Boolean)
    fun setLocalMode(localMode: Int)
    fun setSessionType(sessionType: Short)
    fun setClientCluster(clientCluster: Boolean)
    fun setSessionCluster(sessionCluster: Boolean)
    fun setS3(s3: Properties?)
    fun setORMEnabled(ormenabled: Boolean)
    fun setORMConfiguration(ormConf: ORMConfiguration?)
    fun setORMDatasource(string: String?)
    fun getSecurityApplicationToken(): String?
    fun getSecurityCookieDomain(): String?
    fun getSecurityIdleTimeout(): Int
    fun setSecuritySettings(applicationtoken: String?, cookiedomain: String?, idletimeout: Int)

    @Throws(PageException::class)
    fun reinitORM(pc: PageContext?)
    fun getSource(): Resource?
    fun getTriggerComponentDataMember(): Boolean
    fun setTriggerComponentDataMember(triggerComponentDataMember: Boolean)

    /**
     * return the default cache name for a certain type
     *
     * @param type can be one of the following constants Config.CACHE_DEFAULT_OBJECT,
     * Config.CACHE_DEFAULT_TEMPLATE, Config.CACHE_DEFAULT_QUERY,
     * Config.CACHE_DEFAULT_RESOURCE, Config.CACHE_DEFAULT_FUNCTION
     * @return name of the cache defined
     */
    fun getDefaultCacheName(type: Int): String?
    fun setDefaultCacheName(type: Int, cacheName: String?)

    /**
     * merge the fields with same name to array if true, otherwise to a comma separated string list
     *
     * @param scope scope type, one of the following: Scope.SCOPE_FORM or Scope.SCOPE_URL
     * @return Returns a list of fields.
     */
    fun getSameFieldAsArray(scope: Int): Boolean
    fun getRestSettings(): RestSettings?
    fun getJavaSettings(): JavaSettings?
    fun getRestCFCLocations(): Array<Resource?>?
    fun getDataSources(): Array<DataSource?>?

    @Throws(PageException::class)
    fun getDataSource(dataSourceName: String?): DataSource?
    fun getDataSource(dataSourceName: String?, defaultValue: DataSource?): DataSource?
    fun setDataSources(dataSources: Array<DataSource?>?)

    /**
     * default datasource name (String) or datasource (DataSource Object)
     *
     * @return Returns the Default Datasource.
     */
    fun getDefDataSource(): Object?

    /**
     * orm datasource name (String) or datasource (DataSource Object)
     *
     * @return Returns the Default ORM Datasource.
     */
    fun getORMDataSource(): Object?
    fun setDefDataSource(datasource: Object?)
    fun setORMDataSource(string: Object?)
    fun getBufferOutput(): Boolean
    fun setBufferOutput(bufferOutput: Boolean)
    fun getLocale(): Locale?
    fun setLocale(locale: Locale?)
    fun setTimeZone(timeZone: TimeZone?)
    fun getTimeZone(): TimeZone?
    fun getResourceCharset(): Charset?
    fun getWebCharset(): Charset?
    fun setResourceCharset(cs: Charset?)
    fun setWebCharset(cs: Charset?)
    fun setScopeCascading(scopeCascading: Short)
    fun getScopeCascading(): Short
    fun getTypeChecking(): Boolean
    fun setTypeChecking(typeChecking: Boolean)
    fun getTagAttributeDefaultValues(pc: PageContext?): Map<Collection.Key?, Map<Collection.Key?, Object?>?>?
    fun getTagAttributeDefaultValues(pc: PageContext?, fullName: String?): Map<Collection.Key?, Object?>?
    fun setTagAttributeDefaultValues(pc: PageContext?, sct: Struct?)
    fun getRequestTimeout(): TimeSpan?
    fun setRequestTimeout(timeout: TimeSpan?)
    fun getCustomType(strType: String?): CustomType?
    fun getAllowCompression(): Boolean
    fun setAllowCompression(allowCompression: Boolean)
    fun getSuppressContent(): Boolean
    fun setSuppressContent(suppressContent: Boolean)
    fun getWSType(): Short
    fun setWSType(wstype: Short)
    fun getCachedWithin(type: Int): Object?
    fun setCachedWithin(type: Int, value: Object?)
    fun getCGIScopeReadonly(): Boolean
    fun setCGIScopeReadonly(cgiScopeReadonly: Boolean)

    companion object {
        const val WS_TYPE_AXIS1: Short = 1
        const val WS_TYPE_AXIS2: Short = 2
        const val WS_TYPE_JAX_WS: Short = 4
        const val WS_TYPE_CXF: Short = 8
        const val SCRIPT_PROTECT_NONE = 0
        const val SCRIPT_PROTECT_FORM = 1
        const val SCRIPT_PROTECT_URL = 2
        const val SCRIPT_PROTECT_CGI = 4
        const val SCRIPT_PROTECT_COOKIE = 8
        const val SCRIPT_PROTECT_ALL = SCRIPT_PROTECT_CGI + SCRIPT_PROTECT_COOKIE + SCRIPT_PROTECT_FORM + SCRIPT_PROTECT_URL
    }
}