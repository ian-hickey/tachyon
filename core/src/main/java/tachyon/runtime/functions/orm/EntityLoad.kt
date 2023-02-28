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
package tachyon.runtime.functions.orm

import tachyon.runtime.PageContext

object EntityLoad {
    @Throws(PageException::class)
    fun call(pc: PageContext?, name: String?): Object? {
        val session: ORMSession = ORMUtil.getSession(pc)
        return session.loadAsArray(pc, name, StructImpl())
    }

    @Throws(PageException::class)
    fun call(pc: PageContext?, name: String?, idOrFilter: Object?): Object? {
        return call(pc, name, idOrFilter, Boolean.FALSE)
    }

    @Throws(PageException::class)
    fun call(pc: PageContext?, name: String?, idOrFilter: Object?, uniqueOrOptions: Object?): Object? {
        val session: ORMSession = ORMUtil.getSession(pc)

        // id
        if (Decision.isSimpleValue(idOrFilter)) {
            // id,unique
            if (Decision.isCastableToBoolean(uniqueOrOptions)) {
                // id,unique=true
                return if (Caster.toBooleanValue(uniqueOrOptions)) session.load(pc, name, Caster.toString(idOrFilter)) else session.loadAsArray(pc, name, Caster.toString(idOrFilter))
                // id,unique=false
            } else if (Decision.isString(uniqueOrOptions)) {
                return session.loadAsArray(pc, name, Caster.toString(idOrFilter), Caster.toString(uniqueOrOptions))
            }

            // id,options
            return session.loadAsArray(pc, name, Caster.toString(idOrFilter))
        }

        // filter,[unique|sortorder]
        return if (Decision.isSimpleValue(uniqueOrOptions)) {
            // filter,unique
            if (Decision.isBoolean(uniqueOrOptions)) {
                if (Caster.toBooleanValue(uniqueOrOptions)) session.load(pc, name, Caster.toStruct(idOrFilter)) else session.loadAsArray(pc, name, Caster.toStruct(idOrFilter))
            } else session.loadAsArray(pc, name, Caster.toStruct(idOrFilter), null as Struct?, Caster.toString(uniqueOrOptions))
            // filter,sortorder
        } else session.loadAsArray(pc, name, Caster.toStruct(idOrFilter), Caster.toStruct(uniqueOrOptions))
        // filter,options
    }

    @Throws(PageException::class)
    fun call(pc: PageContext?, name: String?, filter: Object?, order: Object?, options: Object?): Object? {
        val session: ORMSession = ORMUtil.getSession(pc)
        return session.loadAsArray(pc, name, Caster.toStruct(filter), Caster.toStruct(options), Caster.toString(order))
    }
}