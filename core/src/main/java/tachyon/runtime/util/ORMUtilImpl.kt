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

class ORMUtilImpl : ORMUtil {
    @Override
    @Throws(PageException::class)
    fun resetEngine(pc: PageContext?, force: Boolean) {
        tachyon.runtime.orm.ORMUtil.resetEngine(pc, force)
    }

    @Override
    fun getIds(props: Array<Property?>?): Array<Property?>? {
        return tachyon.runtime.orm.ORMUtil.getIds(props)
    }

    @Override
    fun getPropertyValue(cfc: Component?, name: String?, defaultValue: Object?): Object? {
        return tachyon.runtime.orm.ORMUtil.getPropertyValue(cfc, name, defaultValue)
    }

    @Override
    fun isRelated(prop: Property?): Boolean {
        return tachyon.runtime.orm.ORMUtil.isRelated(prop)
    }

    @Override
    fun convertToSimpleMap(paramsStr: String?): Struct? {
        return tachyon.runtime.orm.ORMUtil.convertToSimpleMap(paramsStr)
    }

    @Override
    @Throws(PageException::class)
    fun getDefaultDataSource(pc: PageContext?): DataSource? {
        return tachyon.runtime.orm.ORMUtil.getDefaultDataSource(pc)
    }

    @Override
    fun getDefaultDataSource(pc: PageContext?, defaultValue: DataSource?): DataSource? {
        return tachyon.runtime.orm.ORMUtil.getDefaultDataSource(pc, defaultValue)
    }

    @Override
    fun getDataSource(pc: PageContext?, dsn: String?, defaultValue: DataSource?): DataSource? {
        return tachyon.runtime.orm.ORMUtil.getDataSource(pc, dsn, defaultValue)
    }

    @Override
    @Throws(PageException::class)
    fun getDataSource(pc: PageContext?, dsn: String?): DataSource? {
        // TODO Auto-generated method stub
        return null
    }

    @Override
    fun getDataSource(pc: PageContext?, cfc: Component?, defaultValue: DataSource?): DataSource? {
        return tachyon.runtime.orm.ORMUtil.getDataSource(pc, cfc, defaultValue)
    }

    @Override
    @Throws(PageException::class)
    fun getDataSource(pc: PageContext?, cfc: Component?): DataSource? {
        return tachyon.runtime.orm.ORMUtil.getDataSource(pc, cfc)
    }

    @Override
    @Throws(PageException::class)
    fun getDataSourceName(pc: PageContext?, cfc: Component?): String? {
        return tachyon.runtime.orm.ORMUtil.getDataSourceName(pc, cfc)
    }

    @Override
    fun getDataSourceName(pc: PageContext?, cfc: Component?, defaultValue: String?): String? {
        return tachyon.runtime.orm.ORMUtil.getDataSourceName(pc, cfc, defaultValue)
    }

    @Override
    fun equals(l: Component?, r: Component?): Boolean {
        return tachyon.runtime.orm.ORMUtil.equals(l, r)
    }

    @Override
    fun createException(session: ORMSession?, cfc: Component?, t: Throwable?): PageException? {
        return ORMExceptionUtil.createException(session, cfc, t)
    }

    @Override
    fun createException(session: ORMSession?, cfc: Component?, message: String?, detail: String?): PageException? {
        return ORMExceptionUtil.createException(session, cfc, message, detail)
    }
}