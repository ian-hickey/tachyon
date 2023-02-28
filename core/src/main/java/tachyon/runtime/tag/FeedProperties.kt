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
package tachyon.runtime.tag

import tachyon.runtime.op.Caster

object FeedProperties {
    private val ITEM: Collection.Key? = KeyConstants._ITEM
    private val ITEMS: Collection.Key? = KeyConstants._ITEMS
    private val ENTRY: Collection.Key? = KeyConstants._ENTRY
    private val RDF: Collection.Key? = KeyImpl.getInstance("RDF")
    private val RSS: Collection.Key? = KeyImpl.getInstance("RSS")
    private val CHANNEL: Collection.Key? = KeyImpl.getInstance("channel")
    fun toProperties(data: Struct?): Struct? {
        var data: Struct? = data
        data = Duplicator.duplicate(data, true) as Struct
        var rdf: Struct = Caster.toStruct(data.removeEL(RDF), null, false)
        if (rdf == null) rdf = Caster.toStruct(data.removeEL(RSS), null, false)
        if (rdf != null) {
            rdf.removeEL(ITEM)
            val channel: Struct = Caster.toStruct(rdf.get(CHANNEL, null), null, false)
            if (channel != null) {
                channel.removeEL(ITEMS)
                StructUtil.copy(channel, data, true)
            }
        }
        data.removeEL(ITEM)
        data.removeEL(ENTRY)
        return data
    }
}