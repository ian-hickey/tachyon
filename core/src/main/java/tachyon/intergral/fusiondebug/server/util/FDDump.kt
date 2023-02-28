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
package tachyon.intergral.fusiondebug.server.util

import java.util.Iterator

object FDDump {
    fun dump(`var`: IFDVariable?) {
        LogUtil.log(Log.LEVEL_INFO, FDDump::class.java.getName(), toString(`var`))
    }

    fun toString(value: Object?): String? {
        val sb = StringBuffer()
        dump(sb, value, 0)
        return sb.toString()
    }

    fun toString(`var`: IFDVariable?): String? {
        val sb = StringBuffer()
        dump(sb, `var`, 0)
        return sb.toString()
    }

    private fun dump(sb: StringBuffer?, value: Object?, level: Int) {
        if (value is IFDValue) dump(sb, value as IFDValue?, level) else dump(sb, value as IFDVariable?, level)
    }

    private fun dump(sb: StringBuffer?, value: IFDValue?, level: Int) {
        for (i in 0 until level) {
            sb.append(" - ")
        }
        sb.append(value.toString())
        sb.append("\n")
        if (value.hasChildren()) {
            val it: Iterator = value.getChildren().iterator()
            while (it.hasNext()) {
                val o: Object = it.next()
                dump(sb, o as IFDVariable, level + 1)
            }
        }
    }

    private fun dump(sb: StringBuffer?, `var`: IFDVariable?, level: Int) {
        for (i in 0 until level) {
            sb.append(" - ")
        }
        sb.append(`var`.getName())
        sb.append(":")
        val value: IFDValue = `var`.getValue()
        sb.append(value.toString())
        sb.append("\n")
        // print.err(value.getClass().getName());
        if (value.hasChildren()) {
            val it: Iterator = value.getChildren().iterator()
            while (it.hasNext()) {
                val o: Object = it.next()
                // print.err(o.getClass().getName());
                dump(sb, o as IFDVariable, level + 1)
                // dump(sb,(IFDVariable) it.next(),level+1);
            }
        }
    }
}