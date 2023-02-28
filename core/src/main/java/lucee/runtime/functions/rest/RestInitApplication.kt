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
package lucee.runtime.functions.rest

import lucee.commons.io.res.Resource

object RestInitApplication {
    @Throws(PageException::class)
    fun call(pc: PageContext?, dirPath: String?): String? {
        return _call(pc, dirPath, null, null, null)
    }

    @Throws(PageException::class)
    fun call(pc: PageContext?, dirPath: String?, serviceMapping: String?): String? {
        return _call(pc, dirPath, serviceMapping, null, null)
    }

    @Throws(PageException::class)
    fun call(pc: PageContext?, dirPath: String?, serviceMapping: String?, defaultMapping: Boolean): String? {
        return _call(pc, dirPath, serviceMapping, defaultMapping, null)
    }

    @Throws(PageException::class)
    fun call(pc: PageContext?, dirPath: String?, serviceMapping: String?, defaultMapping: Boolean, webAdminPassword: String?): String? {
        return _call(pc, dirPath, serviceMapping, defaultMapping, webAdminPassword)
    }

    @Throws(PageException::class)
    fun _call(pc: PageContext?, dirPath: String?, serviceMapping: String?, defaultMapping: Boolean?, webAdminPassword: String?): String? {
        var serviceMapping = serviceMapping
        if (StringUtil.isEmpty(serviceMapping, true)) {
            serviceMapping = pc.getApplicationContext().getName()
        }
        val dir: Resource = RestDeleteApplication.toResource(pc, dirPath)
        val config: ConfigWebPro = pc.getConfig() as ConfigWebPro
        val mappings: Array<Mapping?> = config.getRestMappings()
        var mapping: Mapping?

        // id is mapping name
        var virtual: String = serviceMapping.trim()
        if (!virtual.startsWith("/")) virtual = "/$virtual"
        if (!virtual.endsWith("/")) virtual += "/"
        var hasResetted = false
        for (i in mappings.indices) {
            mapping = mappings[i]
            if (mapping.getVirtualWithSlash().equals(virtual)) {
                // directory has changed
                if (!RestUtil.isMatch(pc, mapping, dir) || defaultMapping != null && mapping.isDefault() !== defaultMapping.booleanValue()) {
                    update(pc, dir, virtual, CacheUtil.getPassword(pc, webAdminPassword, false), if (defaultMapping == null) mapping.isDefault() else defaultMapping.booleanValue())
                }
                mapping.reset(pc)
                hasResetted = true
            }
        }
        if (!hasResetted) {
            update(pc, dir, virtual, CacheUtil.getPassword(pc, webAdminPassword, false), if (defaultMapping == null) false else defaultMapping.booleanValue())
        }
        return null
    }

    @Throws(PageException::class)
    private fun update(pc: PageContext?, dir: Resource?, virtual: String?, webAdminPassword: Password?, defaultMapping: Boolean) {
        try {
            val admin: ConfigAdmin = ConfigAdmin.newInstance(pc.getConfig(), webAdminPassword)
            admin.updateRestMapping(virtual, dir.getAbsolutePath(), defaultMapping)
            admin.storeAndReload()
        } catch (e: Exception) {
            throw Caster.toPageException(e)
        }
    }
}