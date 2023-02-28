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

import java.util.Iterator

object EntityNew {
    @Throws(PageException::class)
    fun call(pc: PageContext?, name: String?): Object? {
        return call(pc, name, null)
    }

    @Throws(PageException::class)
    fun call(pc: PageContext?, name: String?, properties: Struct?): Object? {
        val session: ORMSession = ORMUtil.getSession(pc)
        if (properties == null) return session.create(pc, name)
        val entity: Component = session.create(pc, name)
        setPropeties(pc, entity, properties, false)
        return entity
    }

    @Throws(PageException::class)
    fun setPropeties(pc: PageContext?, c: Component?, properties: Struct?, ignoreNotExisting: Boolean) {
        var properties: Struct = properties ?: return

        // argumentCollection
        if (properties.size() === 1 && properties.containsKey(KeyConstants._argumentCollection) && !c.containsKey(KeyConstants._setArgumentCollection)) {
            properties = Caster.toStruct(properties.get(KeyConstants._argumentCollection))
        }
        val it: Iterator<Entry<Key?, Object?>?> = properties.entryIterator()
        var e: Entry<Key?, Object?>?
        while (it.hasNext()) {
            e = it.next()
            val funcName: Key = KeyImpl.init("set" + e.getKey().getString())
            if (ignoreNotExisting) {
                if (c.get(funcName, null) is UDF) c.call(pc, funcName, arrayOf<Object?>(e.getValue()))
            } else {
                c.call(pc, funcName, arrayOf<Object?>(e.getValue()))
            }
        }
    }
}