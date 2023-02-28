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
package lucee.runtime.functions.orm

import java.util.List

object ORMExecuteQuery {
    @Throws(PageException::class)
    fun call(pc: PageContext?, hql: String?): Object? {
        return _call(pc, hql, null, false, null)
    }

    @Throws(PageException::class)
    fun call(pc: PageContext?, hql: String?, paramsOrUnique: Object?): Object? {
        return if (Decision.isCastableToBoolean(paramsOrUnique)) {
            _call(pc, hql, null, Caster.toBooleanValue(paramsOrUnique), null)
        } else _call(pc, hql, paramsOrUnique, false, null)
    }

    @Throws(PageException::class)
    fun call(pc: PageContext?, hql: String?, paramsOrUnique: Object?, uniqueOrQueryOptions: Object?): Object? {
        if (Decision.isCastableToBoolean(paramsOrUnique)) {
            return _call(pc, hql, null, Caster.toBooleanValue(paramsOrUnique), Caster.toStruct(uniqueOrQueryOptions))
        }
        return if (Decision.isCastableToBoolean(uniqueOrQueryOptions)) {
            _call(pc, hql, paramsOrUnique, Caster.toBooleanValue(uniqueOrQueryOptions), null)
        } else _call(pc, hql, paramsOrUnique, false, Caster.toStruct(uniqueOrQueryOptions))
    }

    @Throws(PageException::class)
    fun call(pc: PageContext?, hql: String?, params: Object?, oUnique: Object?, oQueryOptions: Object?): Object? {
        val unique: Boolean
        unique = if (StringUtil.isEmpty(oUnique)) false else Caster.toBooleanValue(oUnique)
        val queryOptions: Struct?
        queryOptions = if (oQueryOptions == null) null else Caster.toStruct(oQueryOptions)
        return _call(pc, hql, params, unique, queryOptions)
    }

    @Throws(PageException::class)
    private fun _call(pc: PageContext?, hql: String?, params: Object?, unique: Boolean, queryOptions: Struct?): Object? {
        val session: ORMSession = ORMUtil.getSession(pc)
        var dsn: String? = null
        if (queryOptions != null) dsn = Caster.toString(queryOptions.get(KeyConstants._datasource, null), null)
        if (StringUtil.isEmpty(dsn, true)) dsn = ORMUtil.getDefaultDataSource(pc).getName()
        return if (params == null) toCFML(session.executeQuery(pc, dsn, hql, ArrayImpl(), unique, queryOptions)) else if (Decision.isStruct(params)) toCFML(session.executeQuery(pc, dsn, hql, Caster.toStruct(params), unique, queryOptions)) else if (Decision.isArray(params)) toCFML(session.executeQuery(pc, dsn, hql, Caster.toArray(params), unique, queryOptions)) else if (Decision.isCastableToStruct(params)) toCFML(session.executeQuery(pc, dsn, hql, Caster.toStruct(params), unique, queryOptions)) else if (Decision.isCastableToArray(params)) toCFML(session.executeQuery(pc, dsn, hql, Caster.toArray(params), unique, queryOptions)) else throw FunctionException(pc, "ORMExecuteQuery", 2, "params", "cannot convert the params to an array or a struct")
    }

    @Throws(PageException::class)
    private fun toCFML(obj: Object?): Object? {
        if (obj is List<*> && obj !is Array) return Caster.toArray(obj)
        return if (obj is Map<*, *> && obj !is Struct) Caster.toStruct(obj, false) else obj
    }
}