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
package tachyon.runtime.security

import tachyon.commons.io.res.Resource

/**
 * SecurityManager to control access to different services
 */
class SecurityManagerImpl : Cloneable, SecurityManager, Cloneable {
    private val accesses: ShortArray? = ShortArray(22)
    private var rootDirectory: Resource? = null
    private var customFileAccess: Array<Resource?>? = EMPTY_RESOURCE_ARRAY

    private constructor() {}

    /**
     * create a new Accessor
     *
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
     * @param t
     * @param accessRead
     */
    constructor(setting: Short, file: Short, directJavaAccess: Short, mail: Short, datasource: Short, mapping: Short, remote: Short, customTag: Short, cfxSetting: Short,
                cfxUsage: Short, debugging: Short, search: Short, scheduledTasks: Short, tagExecute: Short, tagImport: Short, tagObject: Short, tagRegistry: Short, cache: Short, gateway: Short,
                orm: Short, accessRead: Short, accessWrite: Short) {
        accesses!![TYPE_SETTING] = setting
        accesses[TYPE_FILE] = file
        accesses[TYPE_DIRECT_JAVA_ACCESS] = directJavaAccess
        accesses[TYPE_MAIL] = mail
        accesses[TYPE_DATASOURCE] = datasource
        accesses[TYPE_MAPPING] = mapping
        accesses[TYPE_CUSTOM_TAG] = customTag
        accesses[TYPE_CFX_SETTING] = cfxSetting
        accesses[TYPE_CFX_USAGE] = cfxUsage
        accesses[TYPE_DEBUGGING] = debugging
        accesses[TYPE_SEARCH] = search
        accesses[TYPE_SCHEDULED_TASK] = scheduledTasks
        accesses[TYPE_TAG_EXECUTE] = tagExecute
        accesses[TYPE_TAG_IMPORT] = tagImport
        accesses[TYPE_TAG_OBJECT] = tagObject
        accesses[TYPE_TAG_REGISTRY] = tagRegistry
        accesses[TYPE_CACHE] = cache
        accesses[TYPE_GATEWAY] = gateway
        accesses[TYPE_ORM] = orm
        accesses[TYPE_ACCESS_READ] = accessRead
        accesses[TYPE_ACCESS_WRITE] = accessWrite
        accesses[TYPE_REMOTE] = remote
    }

    @Override
    fun getAccess(access: Int): Short {
        return accesses!![access]
    }

    fun setAccess(access: Int, value: Short) {
        accesses!![access] = value
    }

    @Override
    @Throws(SecurityException::class)
    fun getAccess(access: String?): Short {
        return getAccess(toIntAccessType(access))
    }

    @Override
    @Throws(SecurityException::class)
    fun checkFileLocation(res: Resource?) {
        checkFileLocation(null, res, null)
    }

    @Override
    @Throws(SecurityException::class)
    fun checkFileLocation(cw: ConfigWeb?, res: Resource?, strServerPassword: String?) {
        var cw: ConfigWeb? = cw
        var res: Resource? = res
        if (res == null || res.getResourceProvider() !is FileResourceProvider) {
            return
        }
        cw = ThreadLocalPageContext.getConfig(cw) as ConfigWeb
        val serverPassword: Password = PasswordImpl.passwordToCompare(cw, true, strServerPassword)

        // All
        if (getAccess(TYPE_FILE) == VALUE_ALL) return
        // Local
        if (getAccess(TYPE_FILE) == VALUE_LOCAL) {
            res = ResourceUtil.getCanonicalResourceEL(res)

            // local
            if (rootDirectory != null) if (ResourceUtil.isChildOf(res, rootDirectory)) return
            // custom
            if (!ArrayUtil.isEmpty(customFileAccess)) {
                for (i in customFileAccess.indices) {
                    if (ResourceUtil.isChildOf(res, customFileAccess!![i])) return
                }
            }
            if (isValid(cw, serverPassword) || isAdminContext()) return
            throw SecurityException(createExceptionMessage(res, true), "access is prohibited by security manager")
        }
        // None
        if (isValid(cw, serverPassword)) return

        // custom
        if (!ArrayUtil.isEmpty(customFileAccess)) {
            res = ResourceUtil.getCanonicalResourceEL(res)
            for (i in customFileAccess.indices) {
                if (ResourceUtil.isChildOf(res, customFileAccess!![i])) return
            }
        }
        if (isAdminContext()) return
        throw SecurityException(createExceptionMessage(res, false), "access is prohibited by security manager")
    }

    private fun isAdminContext(): Boolean {
        val pc: PageContext = ThreadLocalPageContext.get()
        try {
            if (pc != null && "/tachyon".equals(pc.getBasePageSource().getMapping().getVirtualLowerCase())) {
                return true
            }
        } catch (t: Throwable) {
            ExceptionUtil.rethrowIfNecessary(t)
        }
        return false
    }

    private fun createExceptionMessage(res: Resource?, localAllowed: Boolean): String? {
        val sb = StringBuffer(if (localAllowed && rootDirectory != null) rootDirectory.getAbsolutePath() else "")
        if (customFileAccess != null) {
            for (i in customFileAccess.indices) {
                if (sb.length() > 0) sb.append(" | ")
                sb.append(customFileAccess!![i].getAbsolutePath())
            }
        }
        val rtn = StringBuffer("can't access [")
        rtn.append(res.getAbsolutePath())
        rtn.append("]")
        if (sb.length() > 0) {
            rtn.append(" ")
            rtn.append(if (res.isDirectory()) "directory" else "file")
            rtn.append(" must be inside [")
            rtn.append(sb.toString())
            rtn.append("]")
        }
        return rtn.toString()
    }

    private fun isValid(config: Config?, spw: Password?): Boolean {
        var config: Config? = config
        var spw: Password? = spw
        if (spw == null) {
            try {
                val pc: PageContextImpl = ThreadLocalPageContext.get() as PageContextImpl
                spw = pc.getServerPassword()
            } catch (t: Throwable) {
                ExceptionUtil.rethrowIfNecessary(t)
            }
        }
        config = ThreadLocalPageContext.getConfig(config)
        return if (config == null || spw == null) false else try {
            ConfigWebUtil.getConfigServer(config, spw)
            true
        } catch (e: PageException) {
            false
        }
    }

    @Override
    fun cloneSecurityManager(): SecurityManager? {
        val sm = SecurityManagerImpl()
        for (i in accesses.indices) {
            sm.accesses!![i] = accesses!![i]
        }
        if (customFileAccess != null) sm.customFileAccess = ArrayUtil.clone(customFileAccess, arrayOfNulls<Resource?>(customFileAccess!!.size))
        sm.rootDirectory = rootDirectory
        return sm
    }

    @Override
    fun clone(): Object {
        return cloneSecurityManager()
    }

    fun getCustomFileAccess(): Array<Resource?>? {
        return if (ArrayUtil.isEmpty(customFileAccess)) EMPTY_RESOURCE_ARRAY else ArrayUtil.clone(customFileAccess, arrayOfNulls<Resource?>(customFileAccess!!.size))
    }

    fun setCustomFileAccess(fileAccess: Array<Resource?>?) {
        customFileAccess = merge(customFileAccess, fileAccess)
    }

    fun setRootDirectory(rootDirectory: Resource?) {
        this.rootDirectory = rootDirectory
    }

    companion object {
        private val EMPTY_RESOURCE_ARRAY: Array<Resource?>? = arrayOfNulls<Resource?>(0)

        /**
         * @return return default accessor (no restriction)
         */
        fun getOpenSecurityManager(): SecurityManager? {
            return SecurityManagerImpl(VALUE_YES,  // Setting
                    VALUE_ALL,  // File
                    VALUE_YES,  // Direct Java Access
                    VALUE_YES,  // Mail
                    VALUE_YES,  // Datasource
                    VALUE_YES,  // Mapping
                    VALUE_YES,  // Remote
                    VALUE_YES,  // Custom tag
                    VALUE_YES,  // CFX Setting
                    VALUE_YES,  // CFX Usage
                    VALUE_YES,  // Debugging
                    VALUE_YES,  // Search
                    VALUE_YES,  // Scheduled Tasks
                    VALUE_YES,  // Tag Execute
                    VALUE_YES,  // Tag Import
                    VALUE_YES,  // Tag Object
                    VALUE_YES,  // Tag Registry
                    VALUE_YES,  // Cache
                    VALUE_YES,  // Gateway
                    VALUE_YES,  // ORM
                    ACCESS_OPEN, ACCESS_PROTECTED)
        }

        /**
         * translate a string access type (cfx,file ...) to int type
         *
         * @param accessType
         * @return return access value (all,local,none ...) for given type (cfx,file ...)
         * @throws SecurityException
         */
        @Throws(SecurityException::class)
        private fun toIntAccessType(accessType: String?): Int {
            var accessType = accessType
            accessType = accessType.trim().toLowerCase()
            return if (accessType.equals("setting")) TYPE_SETTING else if (accessType.equals("file")) TYPE_FILE else if (accessType.equals("direct_java_access")) TYPE_DIRECT_JAVA_ACCESS else if (accessType.equals("mail")) TYPE_MAIL else if (accessType.equals("datasource")) TYPE_DATASOURCE else if (accessType.equals("mapping")) TYPE_MAPPING else if (accessType.equals("remote")) TYPE_REMOTE else if (accessType.equals("custom_tag")) TYPE_CUSTOM_TAG else if (accessType.equals("cfx_setting")) TYPE_CFX_SETTING else if (accessType.equals("cfx_usage")) TYPE_CFX_USAGE else if (accessType.equals("debugging")) TYPE_DEBUGGING else if (accessType.equals("tag_execute")) TYPE_TAG_EXECUTE else if (accessType.equals("tag_import")) TYPE_TAG_IMPORT else if (accessType.equals("tag_object")) TYPE_TAG_OBJECT else if (accessType.equals("tag_registry")) TYPE_TAG_REGISTRY else if (accessType.equals("search")) TYPE_SEARCH else if (accessType.equals("cache")) TYPE_CACHE else if (accessType.equals("gateway")) TYPE_GATEWAY else if (accessType.equals("orm")) TYPE_ORM else if (accessType.startsWith("scheduled_task")) TYPE_SCHEDULED_TASK else throw SecurityException("invalid access type [$accessType]",
                    "valid access types are [setting,file,direct_java_access,mail,datasource,mapping,custom_tag,cfx_setting" + "cfx_usage,debugging]")
        }

        /**
         * translate a string access value (all,local,none,no,yes) to int type
         *
         * @param accessValue
         * @return return int access value (VALUE_ALL,VALUE_LOCAL,VALUE_NO,VALUE_NONE,VALUE_YES)
         * @throws SecurityException
         */
        @Throws(SecurityException::class)
        fun toShortAccessValue(accessValue: String?): Short {
            var accessValue = accessValue
            accessValue = accessValue.trim().toLowerCase()
            return if (accessValue.equals("all")) VALUE_ALL else if (accessValue.equals("local")) VALUE_LOCAL else if (accessValue.equals("none")) VALUE_NONE else if (accessValue.equals("no")) VALUE_NO else if (accessValue.equals("yes")) VALUE_YES else if (accessValue.equals("1")) VALUE_1 else if (accessValue.equals("2")) VALUE_2 else if (accessValue.equals("3")) VALUE_3 else if (accessValue.equals("4")) VALUE_4 else if (accessValue.equals("5")) VALUE_5 else if (accessValue.equals("6")) VALUE_6 else if (accessValue.equals("7")) VALUE_7 else if (accessValue.equals("8")) VALUE_8 else if (accessValue.equals("9")) VALUE_9 else if (accessValue.equals("10")) VALUE_10 else throw SecurityException("invalid access value [$accessValue]", "valid access values are [all,local,no,none,yes,1,...,10]")
        }

        @Throws(SecurityException::class)
        fun toShortAccessRWValue(accessValue: String?): Short {
            var accessValue = accessValue
            accessValue = accessValue.trim().toLowerCase()
            return if (accessValue.equals("open")) ACCESS_OPEN else if (accessValue.equals("close")) ACCESS_CLOSE else if (accessValue.equals("protected")) ACCESS_PROTECTED else throw SecurityException("invalid access value [$accessValue]", "valid access values are [open,protected,close]")
        }

        /**
         * translate a string access value (all,local,none,no,yes) to int type
         *
         * @param accessValue
         * @param defaultValue when accessValue is invlaid this value will be returned
         * @return return int access value (VALUE_ALL,VALUE_LOCAL,VALUE_NO,VALUE_NONE,VALUE_YES)
         */
        fun toShortAccessValue(accessValue: String?, defaultValue: Short): Short {
            var accessValue = accessValue ?: return defaultValue
            accessValue = accessValue.trim().toLowerCase()
            return if (accessValue.equals("no")) VALUE_NO else if (accessValue.equals("yes")) VALUE_YES else if (accessValue.equals("all")) VALUE_ALL else if (accessValue.equals("local")) VALUE_LOCAL else if (accessValue.equals("none")) VALUE_NONE else if (accessValue.equals("1")) VALUE_1 else if (accessValue.equals("2")) VALUE_2 else if (accessValue.equals("3")) VALUE_3 else if (accessValue.equals("4")) VALUE_4 else if (accessValue.equals("5")) VALUE_5 else if (accessValue.equals("6")) VALUE_6 else if (accessValue.equals("7")) VALUE_7 else if (accessValue.equals("8")) VALUE_8 else if (accessValue.equals("9")) VALUE_9 else if (accessValue.equals("10")) VALUE_10 else if (accessValue.equals("0")) VALUE_NO else if (accessValue.equals("-1")) VALUE_YES else defaultValue
        }

        fun toShortAccessRWValue(accessValue: String?, defaultValue: Short): Short {
            var accessValue = accessValue
            accessValue = accessValue.trim().toLowerCase()
            return if (accessValue.equals("open")) ACCESS_OPEN else if (accessValue.equals("close")) ACCESS_CLOSE else if (accessValue.equals("protected")) ACCESS_PROTECTED else defaultValue
        }

        /**
         * translate a short access value (all,local,none,no,yes) to String type
         *
         * @param accessValue
         * @return return int access value (VALUE_ALL,VALUE_LOCAL,VALUE_NO,VALUE_NONE,VALUE_YES)
         * @throws SecurityException
         */
        @Throws(SecurityException::class)
        fun toStringAccessValue(accessValue: Short): String? {
            when (accessValue) {
                VALUE_NONE -> return "none"
                VALUE_YES -> return "yes"
                VALUE_LOCAL -> return "local"
                VALUE_1 -> return "1"
                VALUE_2 -> return "2"
                VALUE_3 -> return "3"
                VALUE_4 -> return "4"
                VALUE_5 -> return "5"
                VALUE_6 -> return "6"
                VALUE_7 -> return "7"
                VALUE_8 -> return "8"
                VALUE_9 -> return "9"
                VALUE_10 -> return "10"
            }
            throw SecurityException("invalid access value", "valid access values are [all,local,no,none,yes,1,...,10]")
        }

        @Throws(SecurityException::class)
        fun toStringAccessRWValue(accessValue: Short): String? {
            when (accessValue) {
                ACCESS_CLOSE -> return "close"
                ACCESS_OPEN -> return "open"
                ACCESS_PROTECTED -> return "protected"
            }
            throw SecurityException("invalid access value", "valid access values are [open,close,protected]")
        }

        private fun merge(first: Array<Resource?>?, second: Array<Resource?>?): Array<Resource?>? {
            if (ArrayUtil.isEmpty(second)) return first
            if (ArrayUtil.isEmpty(first)) return second
            val tmp: Array<Resource?> = arrayOfNulls<Resource?>(first!!.size + second!!.size)
            for (i in first.indices) {
                tmp[i] = first!![i]
            }
            for (i in second.indices) {
                tmp[first!!.size + i] = second!![i]
            }
            return tmp
        }
    }
}