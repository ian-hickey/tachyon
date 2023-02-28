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
package tachyon.transformer.bytecode.literal

import tachyon.transformer.Factory

class Identifier private constructor(f: Factory?, private val raw: String?, private val _case: Short, start: Position?, end: Position?) : LitStringImpl(f, convert(raw, _case), start, end) {
    /**
     * @return the raw
     */
    fun getRaw(): String? {
        return raw
    }

    /**
     * @return the _case
     */
    fun getCase(): Short {
        return _case
    }

    fun getUpper(): String? {
        return if (CASE_UPPER == _case) getString() else raw.toUpperCase()
    }

    fun getLower(): String? {
        return if (CASE_LOWER == _case) getString() else raw.toLowerCase()
    }

    companion object {
        var CASE_ORIGNAL: Short = 0
        var CASE_UPPER: Short = 1
        var CASE_LOWER: Short = 2
        fun toIdentifier(f: Factory?, str: String?, start: Position?, end: Position?): Identifier? {
            return Identifier(f, str, CASE_ORIGNAL, start, end)
        }

        fun toIdentifier(f: Factory?, str: String?, _case: Short, start: Position?, end: Position?): Identifier? {
            return Identifier(f, str, _case, start, end)
        }

        private fun convert(str: String?, _case: Short): String? {
            if (CASE_UPPER == _case) return str.toUpperCase()
            return if (CASE_LOWER == _case) str.toLowerCase() else str
        }
    }
}