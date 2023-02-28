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
package tachyon.intergral.fusiondebug.server.type.simple

import java.util.List

class FDSimpleValue(children: List?, str: String?) : FDValueNotMutability() {
    private val str: String?
    private val children: List?

    @Override
    override fun toString(): String {
        return str!!
    }

    @Override
    fun getChildren(): List? {
        return children
    }

    @Override
    fun hasChildren(): Boolean {
        return children != null
    }

    init {
        this.children = children
        this.str = str
    }
}