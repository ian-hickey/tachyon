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
package lucee.runtime.debug

import lucee.runtime.PageSource

abstract class DebugEntrySupport protected constructor(source: PageSource?) : DebugEntry {
    private val id: String?
    private var exeTime: Long = 0
    private val path: String?
    private var count = 1
    private var min: Long = 0
    private var max: Long = 0
    @Override
    fun getExeTime(): Long {
        return positiv(exeTime)
    }

    @Override
    fun updateExeTime(exeTime: Long) {
        if (exeTime >= 0) {
            if (count == 1 || min > exeTime) min = exeTime
            if (max < exeTime) max = exeTime
            this.exeTime += exeTime
        }
    }

    @Override
    fun getPath(): String? {
        return path
    }

    @Override
    fun getId(): String? {
        return id
    }

    /**
     * increment the inner counter
     */
    fun countPP() {
        count++
    }

    @Override
    fun getCount(): Int {
        return count
    }

    @Override
    fun getMax(): Long {
        return positiv(max)
    }

    @Override
    fun getMin(): Long {
        return positiv(min)
    }

    protected fun positiv(time: Long): Long {
        return if (time < 0) 0 else time
    }

    companion object {
        private const val serialVersionUID = -2495816599745340388L
        private var _id = 1
    }

    /**
     * constructor of the class
     *
     * @param source
     * @param key
     */
    init {
        path = if (source == null) "" else source.getDisplayPath()
        id = Caster.toString(++_id)
    }
}