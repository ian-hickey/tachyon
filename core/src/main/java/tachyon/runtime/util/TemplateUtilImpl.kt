/**
 * Copyright (c) 2015, Tachyon Assosication Switzerland. All rights reserved.
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
package tachyon.runtime.util

import tachyon.commons.lang.types.RefBoolean

class TemplateUtilImpl : TemplateUtil {
    @Override
    @Throws(PageException::class)
    fun getComponentJavaAccess(pc: PageContext?, component: Component?, isNew: RefBoolean?, create: Boolean, writeLog: Boolean, suppressWSbeforeArg: Boolean, output: Boolean,
                               returnvalue: Boolean): Class<*>? {
        return ComponentUtil.getComponentJavaAccess(pc, component, isNew, create, writeLog, suppressWSbeforeArg, output, returnvalue)
    }

    @Override
    @Throws(PageException::class)
    fun getComponentPropertiesClass(pc: PageContext?, component: Component?): Class<*>? {
        return ComponentUtil.getComponentPropertiesClass(pc, component)
    }

    @Override
    fun getCompileTime(pc: PageContext?, ps: PageSource?, defaultValue: Long): Long {
        return ComponentUtil.getCompileTime(pc, ps, defaultValue)
    }

    @Override
    @Throws(PageException::class)
    fun getCompileTime(pc: PageContext?, ps: PageSource?): Long {
        return ComponentUtil.getCompileTime(pc, ps)
    }

    @Override
    @Throws(PageException::class)
    fun searchComponent(pc: PageContext?, loadingLocation: PageSource?, rawPath: String?, searchLocal: Boolean?, searchRoot: Boolean?, isExtendedComponent: Boolean,
                        executeConstr: Boolean): Component? {
        return ComponentLoader.searchComponent(pc, loadingLocation, rawPath, searchLocal, searchRoot, isExtendedComponent, executeConstr)
    }

    @Override
    @Throws(PageException::class)
    fun searchInterface(pc: PageContext?, loadingLocation: PageSource?, rawPath: String?, executeConstr: Boolean): Interface? {
        return ComponentLoader.searchInterface(pc, loadingLocation, rawPath, executeConstr)
    }

    @Override
    @Throws(PageException::class)
    fun searchPage(pc: PageContext?, child: PageSource?, rawPath: String?, searchLocal: Boolean?, searchRoot: Boolean?): Page? {
        return ComponentLoader.searchPage(pc, child, rawPath, searchLocal, searchRoot)
    }

    @Override
    @Throws(PageException::class)
    fun loadComponent(pc: PageContext?, ps: PageSource?, callPath: String?, isRealPath: Boolean, silent: Boolean, executeConstr: Boolean): Component? {
        return ComponentLoader.loadComponent(pc, ps, callPath, isRealPath, silent, executeConstr)
    }

    @Override
    @Throws(PageException::class)
    fun loadPage(pc: PageContext?, ps: PageSource?, forceReload: Boolean): Page? {
        return ComponentLoader.loadPage(pc, ps, forceReload)
    }

    @Override
    @Throws(PageException::class)
    fun loadInterface(pc: PageContext?, page: Page?, ps: PageSource?, callPath: String?, isRealPath: Boolean): Interface? {
        return ComponentLoader.loadInterface(pc, page, ps, callPath, isRealPath)
    }

    @Override
    @Throws(PageException::class)
    fun loadComponent(pc: PageContext?, page: Page?, callPath: String?, isRealPath: Boolean, silent: Boolean, isExtendedComponent: Boolean, executeConstr: Boolean): Component? {
        return ComponentLoader.loadComponent(pc, page, callPath, isRealPath, silent, isExtendedComponent, executeConstr)
    }
}