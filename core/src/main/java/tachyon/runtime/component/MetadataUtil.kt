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
package tachyon.runtime.component

import tachyon.runtime.ComponentImpl

object MetadataUtil {
    @Throws(PageException::class)
    fun getPageWhenMetaDataStillValid(pc: PageContext?, comp: ComponentImpl?, ignoreCache: Boolean): Page? {
        val page: Page? = getPage(pc, comp!!._getPageSource())
        if (ignoreCache) return page
        if (page != null && page.metaData != null && page.metaData.get() != null) {
            if (hasChanged(pc, (page.metaData as MetaDataSoftReference)!!.creationTime, comp)) {
                page.metaData = null
            }
        }
        return page
    }

    @Throws(PageException::class)
    fun getPageWhenMetaDataStillValid(pc: PageContext?, interf: InterfaceImpl?, ignoreCache: Boolean): Page? {
        val page: Page? = getPage(pc, interf.getPageSource())
        if (ignoreCache) return page
        if (page != null && page.metaData != null && page.metaData.get() != null) {
            if (hasChanged(pc, (page.metaData as MetaDataSoftReference)!!.creationTime, interf)) page.metaData = null
        }
        return page
    }

    @Throws(PageException::class)
    private fun hasChanged(pc: PageContext?, lastMetaCreation: Long, component: ComponentImpl?): Boolean {
        if (component == null) return false

        // check the component
        val p: Page? = getPage(pc, component._getPageSource())
        if (p == null || hasChanged(p.getCompileTime(), lastMetaCreation)) return true

        // check interfaces
        val interfaces: Array<Interface?> = component.getInterfaces()
        if (!ArrayUtil.isEmpty(interfaces)) {
            if (hasChanged(pc, lastMetaCreation, interfaces)) return true
        }

        // check base
        return hasChanged(pc, lastMetaCreation, component.getBaseComponent() as ComponentImpl)
    }

    @Throws(PageException::class)
    private fun hasChanged(pc: PageContext?, lastMetaCreation: Long, interfaces: Array<Interface?>?): Boolean {
        if (!ArrayUtil.isEmpty(interfaces)) {
            for (i in interfaces.indices) {
                if (hasChanged(pc, lastMetaCreation, interfaces!![i])) return true
            }
        }
        return false
    }

    @Throws(PageException::class)
    private fun hasChanged(pc: PageContext?, lastMetaCreation: Long, inter: Interface?): Boolean {
        val p: Page? = getPage(pc, inter.getPageSource())
        return if (p == null || hasChanged(p.getCompileTime(), lastMetaCreation)) true else hasChanged(pc, lastMetaCreation, inter.getExtends())
    }

    private fun hasChanged(compileTime: Long, lastMetaCreation: Long): Boolean {
        return compileTime > lastMetaCreation
    }

    @Throws(PageException::class)
    private fun getPage(pc: PageContext?, ps: PageSource?): Page? {
        return try {
            ps.loadPage(pc, false)
        } catch (mie: MissingIncludeException) {
            null
        }
    }
}