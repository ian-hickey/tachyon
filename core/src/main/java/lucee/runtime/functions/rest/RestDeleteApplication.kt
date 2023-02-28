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
package lucee.runtime.functions.rest

import lucee.commons.io.res.Resource

object RestDeleteApplication {
    @Throws(PageException::class)
    fun call(pc: PageContext?, dirPath: String?): String? {
        return call(pc, dirPath, null)
    }

    @Throws(PageException::class)
    fun call(pc: PageContext?, dirPath: String?, strWebAdminPassword: String?): String? {
        val webAdminPassword: Password = CacheUtil.getPassword(pc, strWebAdminPassword, false)
        val dir: Resource? = toResource(pc, dirPath)
        val config: ConfigWebPro = pc.getConfig() as ConfigWebPro
        try {
            val admin: ConfigAdmin = ConfigAdmin.newInstance(pc.getConfig(), webAdminPassword)
            val mappings: Array<Mapping?> = config.getRestMappings()
            var mapping: Mapping?
            for (i in mappings.indices) {
                mapping = mappings[i]
                if (RestUtil.isMatch(pc, mapping, dir)) {
                    admin.removeRestMapping(mapping.getVirtual())
                    admin.storeAndReload()
                }
            }
        } catch (e: Exception) {
            throw Caster.toPageException(e)
        }
        return null
    }

    @Throws(PageException::class)
    fun toResource(pc: PageContext?, dirPath: String?): Resource? {
        val dir: Resource = ResourceUtil.toResourceNotExisting(pc.getConfig(), dirPath)
        pc.getConfig().getSecurityManager().checkFileLocation(dir)
        if (!dir.isDirectory()) throw FunctionException(pc, "RestInitApplication", 1, "dirPath", "argument value [$dirPath] must contain an existing directory")
        return dir
    }
}