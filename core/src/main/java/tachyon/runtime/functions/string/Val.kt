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
/**
 * Implements the CFML Function val
 */
package tachyon.runtime.functions.string

import tachyon.runtime.PageContext

object Val : Function {
    private const val serialVersionUID = -4333040593277864043L
    @Throws(PageException::class)
    fun call(pc: PageContext?, value: String?): Double {
        var value: String? = value ?: return 0
        value = value.trim()
        val pos = getPos(value)
        return if (pos <= 0) 0 else Caster.toDoubleValue(value.substring(0, pos))
    }

    private fun getPos(str: String?): Int {
        if (str == null) return 0
        var pos = 0
        val len: Int = str.length()
        if (len == 0) return 0
        var curr: Char = str.charAt(pos)
        if (curr == '+' || curr == '-') {
            if (len == ++pos) return 0
            curr = str.charAt(pos)
        }

        // at least one digit
        if (curr >= '0' && curr <= '9') {
            curr = str.charAt(pos)
        } else if (curr == '.') {
            curr = '.'
        } else return 0
        var hasDot = false
        // boolean hasExp=false;
        while (pos < len) {
            curr = str.charAt(pos)
            if (curr < '0') {
                hasDot = if (curr == '.') {
                    if (pos + 1 >= len || hasDot) return pos
                    true
                } else return pos
            } else if (curr > '9') {
                /*
				 * if(curr=='e' || curr=='E') { if(pos+1>=len || hasExp) return pos; hasExp=true; hasDot=true; }
				 * else
				 */
                return pos
            }
            pos++
        }
        return pos
    }
}