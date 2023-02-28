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
package tachyon.runtime.text.feed

import tachyon.runtime.type.Collection

class FeedStruct : StructImpl {
    private var hasAttribute = false

    /**
     * @return the path
     */
    var path: String? = null
        private set
    private var inside: Key? = null
    private var content: StringBuilder? = null

    /**
     * @return the uri
     */
    var uri: String? = null
        private set

    constructor(path: String?, inside: Key?, uri: String?) {
        this.path = path
        this.inside = inside
        this.uri = uri
    }

    constructor() {}

    /**
     * @param hasAttribute the hasAttribute to set
     */
    fun setHasAttribute(hasAttribute: Boolean) {
        this.hasAttribute = hasAttribute
    }

    fun hasAttribute(): Boolean {
        return hasAttribute || !isEmpty()
    }

    /**
     * @return the inside
     */
    fun getInside(): Key? {
        return inside
    }

    fun append(str: String?) {
        if (content == null) content = StringBuilder()
        content.append(str)
    }

    val string: String?
        get() = if (content == null) "" else content.toString()

    @Override
    fun duplicate(deepCopy: Boolean): Collection? {
        val trg = FeedStruct(path, inside, uri)
        trg.hasAttribute = hasAttribute
        copy(this, trg, deepCopy)
        return trg
    }
}