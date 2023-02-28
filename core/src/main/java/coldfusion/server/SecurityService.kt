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

import java.security.Permission

//import coldfusion.security.BasicPolicy;
interface SecurityService : Service {
    fun getContexts(): Map?

    // public abstract BasicPolicy getBasicPolicy();
    fun getCompiledCrossSiteScriptPatterns(): HashMap?
    fun crossSiteProtectString(arg0: String?): String?
    fun isJvmSecurityEnabled(): Boolean
    fun isSandboxSecurityEnabled(): Boolean
    fun setSandboxSecurityEnabled(arg0: Boolean)
    fun checkPermission(arg0: Permission?)
    fun setJvmSecurityEnabled(arg0: Boolean)
    fun authenticateAdmin()
    fun setAdminPassword(arg0: String?)
    fun isAdminSecurityEnabled(): Boolean
    fun setAdminSecurityEnabled(arg0: Boolean)
    fun checkAdminPassword(arg0: String?, arg1: String?): Boolean
    fun checkAdminPassword(arg0: String?): Boolean
    fun getAdminHash(arg0: Object?): String?
    fun setRdsPassword(arg0: String?)
    fun checkRdsPassword(arg0: String?): Boolean
    fun isRdsSecurityEnabled(): Boolean
    fun setRdsSecurityEnabled(arg0: Boolean)
    fun getSettings(): Map?

    @Throws(ServiceException::class)
    fun setSettings(arg0: Map?)
    fun registerWithWatchService()
    fun setEnableWatch(arg0: Boolean)
}