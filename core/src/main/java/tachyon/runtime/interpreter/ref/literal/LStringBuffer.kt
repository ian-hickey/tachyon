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
package tachyon.runtime.interpreter.ref.literal

import java.util.ArrayList

/**
 * Literal String
 *
 */
class LStringBuffer : RefSupport, Literal {
    private val refs: ArrayList? = ArrayList()
    private var sb: StringBuffer? = StringBuffer()

    /**
     * constructor of the class
     *
     * @param str
     */
    constructor(str: String?) {
        sb.append(str)
    }

    /**
     * constructor of the class
     *
     * @param str
     */
    constructor() {}

    @Override
    @Throws(PageException::class)
    fun getValue(pc: PageContext?): Object? {
        if (refs.size() === 0) return sb.toString()
        val tmp = StringBuffer()
        val it: Iterator = refs.iterator()
        while (it.hasNext()) {
            tmp.append(Caster.toString((it.next() as Ref).getValue(pc)))
        }
        if (sb.length() > 0) tmp.append(sb)
        return tmp.toString()
    }

    fun append(ref: Ref?) {
        if (sb.length() > 0) {
            refs.add(LString(sb.toString()))
            sb = StringBuffer()
        }
        refs.add(ref)
    }

    fun append(c: Char) {
        sb.append(c)
    }

    fun append(str: String?) {
        sb.append(str)
    }

    fun isEmpty(): Boolean {
        return sb.length() + refs.size() === 0
    }

    @Override
    fun getTypeName(): String? {
        return "literal"
    }

    @Override
    @Throws(PageException::class)
    override fun getString(pc: PageContext?): String? {
        return getValue(pc)
    }

    @Override
    @Throws(PageException::class)
    fun eeq(pc: PageContext?, other: Ref?): Boolean {
        return RefUtil.eeq(pc, this, other)
    }
}