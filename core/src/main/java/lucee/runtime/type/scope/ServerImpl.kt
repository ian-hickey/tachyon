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
package lucee.runtime.type.scope

import java.io.File

/**
 * Server Scope
 */
class ServerImpl(pc: PageContext?, jsr223: Boolean) : ScopeSupport("server", SCOPE_SERVER, Struct.TYPE_LINKED), Server, SharedScope {
    @Override
    fun reload() {
        reload(ThreadLocalPageContext.get())
    }

    fun reload(pc: PageContext?) {}
    fun reload(pc: PageContext?, jsr223: Boolean?) {
        val info: Info = pc.getConfig().getFactory().getEngine().getInfo()
        val coldfusion = ReadOnlyStruct()
        coldfusion.setEL(PRODUCT_LEVEL, info.getLevel())
        // coldfusion.setEL(PRODUCT_VERSION,"11,0,07,296330");
        coldfusion.setEL(PRODUCT_VERSION, "2016,0,03,300357")
        coldfusion.setEL(SERIAL_NUMBER, "0")
        coldfusion.setEL(PRODUCT_NAME, "Lucee")

        // TODO scope server missing values
        coldfusion.setEL(KeyConstants._appserver, "") // Jrun
        coldfusion.setEL(EXPIRATION, expired) //
        coldfusion.setEL(INSTALL_KIT, "") //
        var rootdir = ""
        try {
            rootdir = ThreadLocalPageContext.getConfig(pc).getRootDirectory().getAbsolutePath()
        } catch (t: Throwable) {
            ExceptionUtil.rethrowIfNecessary(t)
        }
        coldfusion.setEL(ROOT_DIR, rootdir) //
        coldfusion.setEL(SUPPORTED_LOCALES, LocaleFactory.getLocaleList()) //
        coldfusion.setReadOnly(true)
        super.setEL(KeyConstants._coldfusion, coldfusion)
        val os = ReadOnlyStruct()
        os.setEL(KeyConstants._name, System.getProperty("os.name"))
        os.setEL(ARCH, System.getProperty("os.arch"))
        os.setEL(MAC_ADDRESS, SystemUtil.getMacAddressAsWrap())
        var arch: Int = SystemUtil.getOSArch()
        if (arch != SystemUtil.ARCH_UNKNOW) os.setEL(ARCH_MODEL, Double.valueOf(arch))
        os.setEL(KeyConstants._version, System.getProperty("os.version"))
        os.setEL(ADDITIONAL_INFORMATION, "")
        os.setEL(BUILD_NUMBER, "")
        os.setEL(HOST_NAME, SystemUtil.getLocalHostName())
        os.setReadOnly(true)
        super.setEL(KeyConstants._os, os)
        val lucee = ReadOnlyStruct()
        lucee.setEL(KeyConstants._version, info.getVersion().toString())
        lucee.setEL(lucee.runtime.type.scope.ServerImpl.Companion.VERSION_NAME, info.getVersionName())
        lucee.setEL(lucee.runtime.type.scope.ServerImpl.Companion.VERSION_NAME_EXPLANATION, info.getVersionNameExplanation())
        lucee.setEL(KeyConstants._state, lucee.runtime.type.scope.ServerImpl.Companion.getStateAsString(info.getVersion()))
        lucee.setEL(lucee.runtime.type.scope.ServerImpl.Companion.RELEASE_DATE, DateTimeImpl(info.getRealeaseTime(), false))
        lucee.setEL(lucee.runtime.type.scope.ServerImpl.Companion.LOADER_VERSION, Caster.toDouble(SystemUtil.getLoaderVersion()))
        lucee.setEL(lucee.runtime.type.scope.ServerImpl.Companion.LOADER_PATH, ClassUtil.getSourcePathForClass("lucee.loader.servlet.CFMLServlet", ""))
        lucee.setEL(lucee.runtime.type.scope.ServerImpl.Companion.ENVIRONMENT, if (jsr223 != null && jsr223.booleanValue()) "jsr223" else "servlet")

        // singleContext admin Mode
        lucee.setEL(lucee.runtime.type.scope.ServerImpl.Companion.ADMIN_MODE, ConfigWebUtil.toAdminMode((pc.getConfig() as ConfigPro).getAdminMode(), "single") === "single")
        lucee.setReadOnly(true)
        super.setEL(KeyConstants._lucee, lucee)
        val separator = ReadOnlyStruct()
        separator.setEL(KeyConstants._path, System.getProperty("path.separator"))
        separator.setEL(KeyConstants._file, System.getProperty("file.separator"))
        separator.setEL(KeyConstants._line, System.getProperty("line.separator"))
        separator.setReadOnly(true)
        super.setEL(KeyConstants._separator, separator)
        val java = ReadOnlyStruct()
        java.setEL(KeyConstants._version, System.getProperty("java.version"))
        java.setEL(lucee.runtime.type.scope.ServerImpl.Companion.VENDOR, System.getProperty("java.vendor"))
        arch = SystemUtil.getJREArch()
        if (arch != SystemUtil.ARCH_UNKNOW) java.setEL(lucee.runtime.type.scope.ServerImpl.Companion.ARCH_MODEL, Double.valueOf(arch))
        val rt: Runtime = Runtime.getRuntime()
        java.setEL(lucee.runtime.type.scope.ServerImpl.Companion.FREE_MEMORY, Double.valueOf(rt.freeMemory()))
        java.setEL(lucee.runtime.type.scope.ServerImpl.Companion.TOTAL_MEMORY, Double.valueOf(rt.totalMemory()))
        java.setEL(lucee.runtime.type.scope.ServerImpl.Companion.MAX_MEMORY, Double.valueOf(rt.maxMemory()))
        java.setEL(lucee.runtime.type.scope.ServerImpl.Companion.JAVA_AGENT_SUPPORTED, Boolean.TRUE)
        if (lucee.runtime.type.scope.ServerImpl.Companion.jep == null) {
            var temp: String = System.getProperty("user.dir", "")
            if (!StringUtil.isEmpty(temp) && !temp.endsWith(File.separator)) temp = temp + File.separator
            lucee.runtime.type.scope.ServerImpl.Companion.jep = temp
        }
        java.setEL(lucee.runtime.type.scope.ServerImpl.Companion.JAVA_EXECUTION_PATH, lucee.runtime.type.scope.ServerImpl.Companion.jep)
        java.setReadOnly(true)
        super.setEL(KeyConstants._java, java)
        val servlet = ReadOnlyStruct()
        var name = ""
        try {
            name = pc.getServletContext().getServerInfo()
        } catch (t: Throwable) {
            ExceptionUtil.rethrowIfNecessary(t)
        }
        servlet.setEL(KeyConstants._name, name)
        servlet.setReadOnly(true)
        super.setEL(KeyConstants._servlet, servlet)
        val system = ReadOnlyStruct()
        system.setEL(KeyConstants._properties, SystemPropStruct.getInstance())
        system.setEL(KeyConstants._environment, EnvStruct.getInstance())
        system.setReadOnly(true)
        super.setEL(KeyConstants._system, system)
    }

    @Override
    @Throws(PageException::class)
    operator fun set(key: Collection.Key?, value: Object?): Object? {
        if (isReadOnlyKey(key)) throw ExpressionException("Key [$key] in Server scope is read-only and can not be modified")
        return super.set(key, value)
    }

    @Override
    fun setEL(key: Collection.Key?, value: Object?): Object? {
        return if (!isReadOnlyKey(key)) super.setEL(key, value) else value
    }

    @Override
    operator fun get(key: Key?, defaultValue: Object?): Object? {
        return if (key.equalsIgnoreCase(KeyConstants._railo)) super.get(KeyConstants._lucee, defaultValue) else super.get(key, defaultValue)
    }

    @Override
    fun g(key: Key?, defaultValue: Object?): Object? {
        return if (key.equalsIgnoreCase(KeyConstants._railo)) super.g(KeyConstants._lucee, defaultValue) else super.g(key, defaultValue)
    }

    @Override
    @Throws(PageException::class)
    fun g(key: Key?): Object? {
        return if (key.equalsIgnoreCase(KeyConstants._railo)) super.g(KeyConstants._lucee) else super.g(key)
    }

    @Override
    @Throws(PageException::class)
    operator fun get(key: Key?): Object? {
        return if (key.equalsIgnoreCase(KeyConstants._railo)) super.get(KeyConstants._lucee) else super.get(key)
    }

    @Override
    @Throws(PageException::class)
    operator fun get(pc: PageContext?, key: Key?): Object? {
        return if (key.equalsIgnoreCase(KeyConstants._railo)) super.get(pc, KeyConstants._lucee) else super.get(pc, key)
    }

    /**
     * returns if the key is a readonly key
     *
     * @param key key to check
     * @return is readonly
     */
    private fun isReadOnlyKey(key: Collection.Key?): Boolean {
        return (key.equals(KeyConstants._java) || key.equals(KeyConstants._separator) || key.equals(KeyConstants._os) || key.equals(KeyConstants._coldfusion)
                || key.equals(KeyConstants._lucee))
    }

    @Override
    fun touchBeforeRequest(pc: PageContext?) {
        // do nothing
    }

    @Override
    fun touchAfterRequest(pc: PageContext?) {
        // do nothing
    }

    companion object {
        private val expired: DateTimeImpl? = DateTimeImpl(2145913200000L, false)
        private val PRODUCT_NAME: Key? = KeyImpl.getInstance("productname")
        private val PRODUCT_LEVEL: Key? = KeyImpl.getInstance("productlevel")
        private val PRODUCT_VERSION: Key? = KeyImpl.getInstance("productversion")
        private val SERIAL_NUMBER: Key? = KeyImpl.getInstance("serialnumber")
        private val EXPIRATION: Key? = KeyImpl.getInstance("expiration")
        private val INSTALL_KIT: Key? = KeyImpl.getInstance("installkit")
        private val ROOT_DIR: Key? = KeyImpl.getInstance("rootdir")
        private val SUPPORTED_LOCALES: Key? = KeyImpl.getInstance("supportedlocales")
        private val ARCH: Key? = KeyImpl.getInstance("arch")
        private val MAC_ADDRESS: Key? = KeyImpl.getInstance("macAddress")
        private val ARCH_MODEL: Key? = KeyImpl.getInstance("archModel")

        // private static final Key JAVA_AGENT_PATH = KeyImpl.getInstance("javaAgentPath");
        private val JAVA_EXECUTION_PATH: Key? = KeyImpl.getInstance("executionPath")
        private val JAVA_AGENT_SUPPORTED: Key? = KeyImpl.getInstance("javaAgentSupported")
        private val LOADER_VERSION: Key? = KeyImpl.getInstance("loaderVersion")
        private val LOADER_PATH: Key? = KeyImpl.getInstance("loaderPath")
        private val ADDITIONAL_INFORMATION: Key? = KeyImpl.getInstance("additionalinformation")
        private val BUILD_NUMBER: Key? = KeyImpl.getInstance("buildnumber")
        private val RELEASE_DATE: Key? = KeyImpl.getInstance("release-date")
        private val VENDOR: Key? = KeyImpl.getInstance("vendor")
        private val FREE_MEMORY: Key? = KeyImpl.getInstance("freeMemory")
        private val MAX_MEMORY: Key? = KeyImpl.getInstance("maxMemory")
        private val TOTAL_MEMORY: Key? = KeyImpl.getInstance("totalMemory")
        private val VERSION_NAME: Key? = KeyImpl.getInstance("versionName")
        private val VERSION_NAME_EXPLANATION: Key? = KeyImpl.getInstance("versionNameExplanation")
        private val HOST_NAME: Key? = KeyImpl.getInstance("hostname")
        private val ENVIRONMENT: Key? = KeyConstants._environment
        private val ADMIN_MODE: Key? = KeyImpl.getInstance("singleContext")
        private val jep: String? = null
        private fun getStateAsString(version: Version?): String? {
            val q: String = version.getQualifier()
            val index: Int = q.indexOf('-')
            return if (index == -1) "stable" else q.substring(index + 1)
        }
    }
    /*
	 * Supported CFML Application
	 * 
	 * Blog - http://www.blogcfm.org
	 * 
	 * 
	 * 
	 */
    /**
     * constructor of the server scope
     *
     * @param pc
     */
    init {
        reload(pc, jsr223)
    }
}