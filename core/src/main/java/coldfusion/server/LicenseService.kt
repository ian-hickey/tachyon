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

import java.util.Date

interface LicenseService : Service {
    fun setLicenseKey(arg0: String?)
    fun getLicenseKey(): String?
    fun isEntKey(arg0: String?): Boolean
    fun isValidKey(arg0: String?): Boolean
    fun isValidOldKey(arg0: String?): Boolean
    fun isUpgradeKey(arg0: String?): Boolean
    fun getRequiredKeyInfo(arg0: String?): Map?

    @Throws(Exception::class)
    fun init()
    fun isValid(): Boolean
    fun getMajorVersion(): Int
    fun getInstallDate(): Date?
    fun getExpirationDate(): Date?
    fun getEvalDays(): Int
    fun getEvalDaysLeft(): Long
    fun isExpired(): Boolean
    fun getEdition(): String?
    fun isEnterprise(): Boolean
    fun isStandard(): Boolean
    fun isDeveloper(): Boolean
    fun isUpgrade(): Boolean
    fun isReportPack(): Boolean
    fun isEducational(): Boolean
    fun isDevNet(): Boolean
    fun isVolume(): Boolean
    fun getProperties(): Map?
    fun getOSPlatform(): String?
    fun getAppServerPlatform(): String?
    fun getVendor(): String?
    fun getServerType(): Int
    fun getVerityLimit(): Long
    fun allowJSP(): Boolean
    fun allowCFImport(): Boolean
    fun allowSandboxSecurity(): Boolean
    fun getCPUNumber(): Int
    fun isSingleIP(): Boolean
    fun allowAdvMgmt(): Boolean
    fun isValidIP(arg0: String?): Boolean
    fun getAllowedIp(): String?
    fun allowFastMail(): Boolean
    fun allowEventService(): Boolean
    fun allowOracleOEM(): Boolean
    fun allowSybaseOEM(): Boolean
    fun allowInformixOEM(): Boolean
    fun allowDB2OEM(): Boolean
    fun isJadoZoomLoaded(): Boolean
    fun registerWithWatchService()
    fun setEnableWatch(arg0: Boolean)
}