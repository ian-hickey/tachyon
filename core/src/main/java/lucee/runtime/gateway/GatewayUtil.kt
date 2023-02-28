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
package lucee.runtime.gateway

import java.util.Iterator

object GatewayUtil {
    fun toCFML(obj: Object?): Object? {
        if (obj is Map) return toCFML(obj as Map?)
        return if (obj is List) toCFML(obj as List?) else obj
    }

    fun toCFML(map: Map?): Map? {
        val it: Iterator = map.entrySet().iterator()
        var entry: Map.Entry
        while (it.hasNext()) {
            entry = it.next() as Entry
            entry.setValue(toCFML(entry.getValue()))
        }
        return map
    }

    fun toCFML(list: List?): Object? {
        val it: ListIterator = list.listIterator()
        var index: Int
        while (it.hasNext()) {
            index = it.nextIndex()
            list.set(index, toCFML(it.next()))
        }
        return list
    }

    fun getState(ge: GatewayEntry?): Int { // this method only exists to make sure the Gateway interface must not be used outsite the gateway
        // package
        return ge!!.getGateway().getState()
    }
}