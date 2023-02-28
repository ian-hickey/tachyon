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
package lucee.runtime.type

import java.util.Date

class DoubleStruct : StructImpl() {
    @Override
    fun castToBoolean(defaultValue: Boolean?): Boolean? {
        return try {
            Caster.toBoolean(castToBooleanValue())
        } catch (e: PageException) {
            defaultValue
        }
    }

    @Override
    fun castToDateTime(defaultValue: DateTime?): DateTime? {
        return try {
            castToDateTime()
        } catch (e: PageException) {
            defaultValue
        }
    }

    @Override
    fun castToDoubleValue(defaultValue: Double): Double {
        return try {
            castToDoubleValue()
        } catch (e: PageException) {
            defaultValue
        }
    }

    @Override
    fun castToString(defaultValue: String?): String? {
        return try {
            castToString()
        } catch (e: PageException) {
            defaultValue
        }
    }

    @Override
    @Throws(PageException::class)
    fun castToBooleanValue(): Boolean {
        return Caster.toBooleanValue(castToDoubleValue())
    }

    @Override
    @Throws(PageException::class)
    fun castToDateTime(): DateTime? {
        return Caster.toDate(castToDateTime(), null)
    }

    @Override
    @Throws(PageException::class)
    fun castToDoubleValue(): Double {
        val it: Iterator = valueIterator()
        var value = 0.0
        while (it.hasNext()) {
            value += Caster.toDoubleValue(it.next())
        }
        return value
    }

    @Override
    @Throws(PageException::class)
    fun castToString(): String? {
        return Caster.toString(castToDoubleValue())
    }

    @Override
    @Throws(PageException::class)
    operator fun compareTo(b: Boolean): Int {
        return OpUtil.compare(ThreadLocalPageContext.get(), Double.valueOf(castToDoubleValue()), if (b) Boolean.TRUE else Boolean.FALSE)
    }

    @Override
    @Throws(PageException::class)
    operator fun compareTo(dt: DateTime?): Int {
        return OpUtil.compare(ThreadLocalPageContext.get(), Double.valueOf(castToDoubleValue()), dt as Date?)
    }

    @Override
    @Throws(PageException::class)
    operator fun compareTo(d: Double): Int {
        return OpUtil.compare(ThreadLocalPageContext.get(), Double.valueOf(castToDoubleValue()), Double.valueOf(d))
    }

    @Override
    @Throws(PageException::class)
    operator fun compareTo(str: String?): Int {
        return OpUtil.compare(ThreadLocalPageContext.get(), Double.valueOf(castToDoubleValue()), str)
    }

    @Override
    override fun duplicate(deepCopy: Boolean): Collection? {
        val sct: Struct = DoubleStruct()
        copy(this, sct, deepCopy)
        return sct
    }

    @Override
    override fun toDumpData(pageContext: PageContext?, maxlevel: Int, dp: DumpProperties?): DumpData? {
        val table: DumpTable? = super.toDumpData(pageContext, maxlevel, dp) as DumpTable?
        try {
            table.setTitle("Double Struct (" + castToString() + ")")
        } catch (pe: PageException) {
        }
        return table
    }
}