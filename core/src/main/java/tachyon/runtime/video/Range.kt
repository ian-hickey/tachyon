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
package tachyon.runtime.video

import tachyon.runtime.exp.PageException

class Range(private val from: Double, private val to: Double) {
    /**
     * @return the from
     */
    fun getFrom(): Double {
        return from
    }

    fun getFromAsString(): String? {
        return Caster.toString(from)
    }

    /**
     * @return the to
     */
    fun getTo(): Double {
        return to
    }

    fun getToAsString(): String? {
        return Caster.toString(to)
    }

    /**
     *
     * @see java.lang.Object.equals
     */
    @Override
    override fun equals(obj: Object?): Boolean {
        if (obj === this) return true
        if (obj !is Range) return false
        val other = obj as Range?
        return other!!.from == from && other.to == to
    }

    /**
     *
     * @see java.lang.Object.toString
     */
    @Override
    override fun toString(): String {
        return "$from:$to"
    }

    fun show(): Boolean {
        return !equals(FALSE)
    }

    companion object {
        val TRUE: Range? = Range(0, -1)
        val FALSE: Range? = Range(0, 0)
        @Throws(PageException::class)
        fun toRange(def: String?): Range? {
            var def = def
            def = def.trim()
            // boolean
            if (Decision.isBoolean(def)) {
                return if (Caster.toBooleanValue(def)) TRUE else FALSE
            }
            val index: Int = def.indexOf(',')
            // single value
            if (index == -1) {
                return Range(toSeconds(def), -1)
            }

            // double value
            if (def.startsWith(",")) def = "0$def"
            if (def.endsWith(",")) def += "-1"
            return Range(toSeconds(def.substring(0, index)), toSeconds(def.substring(index + 1)))
        }

        @Throws(PageException::class)
        private fun toSeconds(str: String?): Double {
            var str = str
            str = str.trim().toLowerCase()
            return if (str.endsWith("ms")) Caster.toDoubleValue(str.substring(0, str.length() - 2)) / 1000.0 else if (str.endsWith("s")) Caster.toDoubleValue(str.substring(0, str.length() - 1)) else Caster.toDoubleValue(str) / 1000.0
            // TODO if(str.endsWith("f")) this.startFrame=VideoConfig.toLong(str.substring(0,str.length()-1));
        }
    }
}