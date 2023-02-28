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

import tachyon.runtime.type.Collection.Key

class MissingAttribute(name: Key?, type: String?, alias: Array<String?>?) {
    private val name: Key?

    /**
     * @return the type
     */
    val type: String?
    val alias: Array<String?>?

    /**
     * @return the name
     */
    fun getName(): Key? {
        return name
    }

    @Override
    override fun toString(): String {
        return "name:" + name + ";type:" + type + ";alias:" + (if (alias == null) "null" else ListUtil.arrayToList(alias, ",")) + ";"
    }

    companion object {
        fun newInstance(name: Key?, type: String?): MissingAttribute? {
            return MissingAttribute(name, type, null)
        }

        fun newInstance(name: String?, type: String?): MissingAttribute? {
            return newInstance(KeyImpl.init(name), type, null)
        }

        fun newInstance(name: Key?, type: String?, alias: Array<String?>?): MissingAttribute? {
            return MissingAttribute(name, type, alias)
        }

        fun newInstance(name: String?, type: String?, alias: Array<String?>?): MissingAttribute? {
            return newInstance(KeyImpl.init(name), type)
        }
    }

    init {
        this.name = name
        this.type = type
        this.alias = alias
    }
}