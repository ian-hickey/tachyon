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
package lucee.transformer.bytecode.statement.tag

import lucee.transformer.Factory

abstract class TagGroup(f: Factory?, start: Position?, end: Position?) : TagBase(f, start, end) {
    private var numberIterator = -1
    private var query = -1
    private var group = -1
    private var pid = 0
    abstract fun getType(): Short

    // public abstract boolean hasQuery();
    // public abstract boolean hasGroup();
    fun getNumberIterator(): Int {
        return numberIterator
    }

    fun setNumberIterator(numberIterator: Int) {
        this.numberIterator = numberIterator
    }

    fun hasNumberIterator(): Boolean {
        return numberIterator != -1
    }

    /**
     * returns if output has query
     *
     * @return has query
     */
    fun hasQuery(): Boolean {
        return getAttribute("query") != null
    }

    /**
     * returns if output has query
     *
     * @return has query
     */
    fun hasGroup(): Boolean {
        return getAttribute("group") != null
    }

    fun getQuery(): Int {
        return query
    }

    fun setQuery(query: Int) {
        this.query = query
    }

    fun getGroup(): Int {
        return group
    }

    fun setGroup(group: Int) {
        this.group = group
    }

    fun getPID(): Int {
        return pid
    }

    fun setPID(pid: Int) {
        this.pid = pid
    }

    companion object {
        const val TAG_LOOP: Short = 1
        const val TAG_OUTPUT: Short = 2
    }
}