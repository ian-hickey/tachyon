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

import tachyon.runtime.Component

interface ORMUtil {
    /**
     *
     * @param pc Page Context
     * @param force if set to false the engine is on loaded when the configuration has changed
     * @throws PageException Page Exception
     */
    @Throws(PageException::class)
    fun resetEngine(pc: PageContext?, force: Boolean)
    fun getIds(props: Array<Property?>?): Array<Property?>?
    fun getPropertyValue(cfc: Component?, name: String?, defaultValue: Object?): Object?
    fun isRelated(prop: Property?): Boolean
    fun convertToSimpleMap(paramsStr: String?): Struct?

    @Throws(PageException::class)
    fun getDefaultDataSource(pc: PageContext?): DataSource?
    fun getDefaultDataSource(pc: PageContext?, defaultValue: DataSource?): DataSource?
    fun getDataSource(pc: PageContext?, dsn: String?, defaultValue: DataSource?): DataSource?

    @Throws(PageException::class)
    fun getDataSource(pc: PageContext?, dsn: String?): DataSource?

    /**
     * if the given component has defined a datasource in the meta data, tachyon is returning this
     * datasource, otherwise the default orm datasource is returned
     *
     * @param pc Page Context
     * @param cfc Component
     * @param defaultValue default value
     * @return Return the Datasource
     */
    fun getDataSource(pc: PageContext?, cfc: Component?, defaultValue: DataSource?): DataSource?

    /**
     * if the given component has defined a datasource in the meta data, tachyon is returning this
     * datasource, otherwise the default orm datasource is returned
     *
     * @param pc Page Context
     * @param cfc Component
     * @return Returns the datasource.
     * @throws PageException Page Exception
     */
    @Throws(PageException::class)
    fun getDataSource(pc: PageContext?, cfc: Component?): DataSource?

    @Throws(PageException::class)
    fun getDataSourceName(pc: PageContext?, cfc: Component?): String?
    fun getDataSourceName(pc: PageContext?, cfc: Component?, defaultValue: String?): String?
    fun equals(l: Component?, r: Component?): Boolean
    fun createException(session: ORMSession?, cfc: Component?, t: Throwable?): PageException?
    fun createException(session: ORMSession?, cfc: Component?, message: String?, detail: String?): PageException?
}