/**
 * Copyright (c) 2015, Lucee Assosication Switzerland. All rights reserved.
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
package lucee.runtime.util

import lucee.commons.lang.types.RefBoolean

interface TemplateUtil {
    /**
     * generate a ComponentJavaAccess (CJA) class from a component a CJA is a dynamic generated java
     * class that has all method defined inside a component as java methods.
     *
     * This is used to generated server side Webservices.
     *
     * @param pc Page context
     * @param component Component
     * @param isNew is new
     * @param create create
     * @param writeLog write log
     * @param suppressWSbeforeArg suppress whitespace before argument
     * @param output output
     * @param returnValue if true the method returns the value of the last expression executed inside
     * when you call the method "call"
     * @return Returns a Class.
     * @throws PageException Page Exception
     */
    @Throws(PageException::class)
    fun getComponentJavaAccess(pc: PageContext?, component: Component?, isNew: RefBoolean?, create: Boolean, writeLog: Boolean, suppressWSbeforeArg: Boolean, output: Boolean,
                               returnValue: Boolean): Class<*>?

    @Throws(PageException::class)
    fun getComponentPropertiesClass(pc: PageContext?, component: Component?): Class<*>?
    fun getCompileTime(pc: PageContext?, ps: PageSource?, defaultValue: Long): Long

    @Throws(PageException::class)
    fun getCompileTime(pc: PageContext?, ps: PageSource?): Long

    @Throws(PageException::class)
    fun searchComponent(pc: PageContext?, loadingLocation: PageSource?, rawPath: String?, searchLocal: Boolean?, searchRoot: Boolean?, isExtendedComponent: Boolean,
                        executeConstr: Boolean): Component?

    @Throws(PageException::class)
    fun searchInterface(pc: PageContext?, loadingLocation: PageSource?, rawPath: String?, executeConstr: Boolean): Interface?

    @Throws(PageException::class)
    fun searchPage(pc: PageContext?, child: PageSource?, rawPath: String?, searchLocal: Boolean?, searchRoot: Boolean?): Page?

    @Throws(PageException::class)
    fun loadComponent(pc: PageContext?, page: Page?, callPath: String?, isRealPath: Boolean, silent: Boolean, isExtendedComponent: Boolean, executeConstr: Boolean): Component?

    @Throws(PageException::class)
    fun loadComponent(pc: PageContext?, ps: PageSource?, callPath: String?, isRealPath: Boolean, silent: Boolean, executeConstr: Boolean): Component?

    @Throws(PageException::class)
    fun loadPage(pc: PageContext?, ps: PageSource?, forceReload: Boolean): Page?

    @Throws(PageException::class)
    fun loadInterface(pc: PageContext?, page: Page?, ps: PageSource?, callPath: String?, isRealPath: Boolean): Interface?
}