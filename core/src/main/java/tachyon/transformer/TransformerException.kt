/**
 * Copyright (c) 2015, Tachyon Assosication Switzerland. All rights reserved.
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
package tachyon.transformer

import tachyon.runtime.PageSource

class TransformerException(c: Context?, message: String?, pos: Position?) : TemplateException(message) {
    private val pos: Position? = null

    constructor(c: Context?, cause: Throwable?, start: Position?) : this(c, cause.getMessage(), start) {
        initCause(cause)
        setAddional(c)
    }

    fun getPosition(): Position? {
        return pos
    }

    private fun setAddional(c: Context?) {
        if (c is BytecodeContext) {
            val bc: BytecodeContext? = c as BytecodeContext?
            val ps: PageSource = bc.getPageSource()
            if (ps != null) setAdditional(KeyConstants._source, ps.getDisplayPath())
        }
    }

    companion object {
        private const val serialVersionUID = 6750275378601018748L
    }

    init {
        this.pos = pos
        setAddional(c)
    }
}