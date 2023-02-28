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
package coldfusion.server

import java.io.File

interface RuntimeService : Service {
    fun getWhitespace(): Boolean?
    fun getLocking(): Map?
    fun getCfxtags(): Map?
    fun getCustomtags(): Map?
    fun getCorba(): Map?
    fun getApplets(): Map?
    fun getVariables(): Map?
    fun getErrors(): Map?
    fun getScriptProtect(): String?
    fun setScriptProtect(arg0: String?)
    fun getMappings(): Map?
    fun getApplications(): Map?
    fun getRootDir(): String?
    fun setWhitespace(arg0: String?)
    fun resolveTemplateName(arg0: String?, arg1: String?): File?

    @Throws(IOException::class)
    fun getFullTagName(arg0: ServletContext?, arg1: String?): String?
    fun resolveTemplatePath(arg0: String?): File?
    fun getRealPath(arg0: ServletContext?, arg1: String?): String?
    fun getServerScope(): Scope?
    fun getRegistryDir(): String?
    fun getSlowRequestLimit(): Long
    fun logSlowRequests(): Boolean
    fun getRequestTimeoutLimit(): Long
    fun timeoutRequests(): Boolean
    fun getNumberSimultaneousRequests(): Int
    fun getNumberSimultaneousReports(): Int
    fun setNumberSimultaneousReports(arg0: Int)
    fun setNumberSimultaneousRequests(arg0: Int)
    fun getMaxQueued(): Int
    fun setMaxQueued(arg0: Int)
    fun getMinRequests(): Int
    fun setMinRequests(arg0: Int)
    fun isCachePaths(): Boolean
    fun setCachePaths(arg0: Boolean)
    fun isTrustedCache(): Boolean
    fun setTrustedCache(arg0: Boolean)
    fun setTemplateCacheSize(arg0: Int)
    fun getTemplateCacheSize(): Int
    fun getApplicationTimeout(): Long
    fun getApplicationMaxTimeout(): Long
    fun isApplicationEnabled(): Boolean
    fun getSessionTimeout(): Long
    fun getSessionMaxTimeout(): Long
    fun isSessionEnabled(): Boolean
    fun useJ2eeSession(): Boolean
    fun isPureJavaKit(): Boolean
    fun getRequestSettings(): Map?
    fun setSaveClassFiles(arg0: Boolean)
    fun getSaveClassFiles(): Boolean
    fun getRequestThrottleSettings(): Map?
    fun getFileLockSettings(): Map?
    fun isFileLockEnabled(): Boolean
    fun getPostSizeLimit(): Float
    fun isEnabledFlexDataServices(): Boolean

    @Throws(ServiceException::class)
    fun setEnableFlexDataServices(arg0: Boolean)
    fun getFlexAssemblerIPList(): String?
    fun setFlexAssemblerIPList(arg0: String?)
    fun isEnabledFlashRemoting(): Boolean
    fun setEnableFlashRemoting(arg0: Boolean)
    fun isEnabledRmiSSL(): Boolean

    @Throws(ServiceException::class)
    fun setEnableRmiSSL(arg0: Boolean)
    fun setKeystore(arg0: String?)
    fun getKeystore(): String?
    fun setKeystorePassword(arg0: String?)
    fun getKeystorePassword(): String?
    fun setDataServiceId(arg0: String?)
    fun getDataServiceId(): String?
}