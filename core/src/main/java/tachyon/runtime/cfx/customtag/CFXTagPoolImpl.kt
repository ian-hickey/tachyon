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
package tachyon.runtime.cfx.customtag

import java.util.Map

/**
 *
 */
class CFXTagPoolImpl(classes: Map<String?, CFXTagClass?>?) : CFXTagPool {
    var config: Config? = null
    var classes: Map<String?, CFXTagClass?>?
    var objects: Map<String?, CFXTagClass?>? = MapFactory.< String, CFXTagClass>getConcurrentMap<String?, CFXTagClass?>()
    @Override
    fun getClasses(): Map<String?, CFXTagClass?>? {
        return classes
    }

    @Override
    @Synchronized
    @Throws(CFXTagException::class)
    fun getCustomTag(name: String?): CustomTag? {
        var name = name
        name = name.toLowerCase()
        val o: Object? = classes!![name]
        if (o == null) {
            val set: Set<String?> = classes.keySet()
            val names: String = ListUtil.arrayToList(set.toArray(arrayOfNulls<String?>(set.size())), ",")
            throw CFXTagException("there is no Custom Tag (CFX) with name [$name], available Custom Tags are [$names]")
        }
        val ctc: CFXTagClass? = o as CFXTagClass?
        // if(!(o instanceof CustomTag))throw new CFXTagException("["+name+"] is not of type
        // ["+CustomTag.class.getName()+"]");
        return ctc.newInstance()
    }

    @Override
    @Synchronized
    @Throws(CFXTagException::class)
    fun getCFXTagClass(name: String?): CFXTagClass? {
        var name = name
        name = name.toLowerCase()
        return classes!![name]
                ?: throw CFXTagException("there is not Custom Tag (CFX) with name [$name]")
    }

    @Override
    fun releaseCustomTag(ct: CustomTag?) {
        // table.put(ct.getClass().toString(),ct);
    }

    @Override
    fun releaseTag(tag: Object?) {
        // table.put(ct.getClass().toString(),ct);
    }

    /**
     * constructor of the class
     *
     * @param classes
     */
    init {
        this.classes = classes
    }
}