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
package tachyon.runtime.converter

import java.util.Date

object ClientScopeConverter {
    fun unserialize(str: String?): Struct? {
        val sct: Struct = StructImpl()
        val ps = ParserString(str)
        var sb: StringBuilder? = StringBuilder()
        var key: String? = null
        while (!ps.isAfterLast()) {
            if (ps.isCurrent('#')) {
                if (ps.isNext('=')) {
                    ps.next()
                    sb.append('=')
                } else if (ps.isNext('#')) {
                    ps.next()
                    sb.append('#')
                } else {
                    sct.setEL(key, sb.toString())
                    sb = StringBuilder()
                }
            } else if (ps.isCurrent('=')) {
                key = sb.toString()
                sb = StringBuilder()
            } else sb.append(ps.getCurrent())
            ps.next()
        }
        if (!StringUtil.isEmpty(key) && !StringUtil.isEmpty(sb)) {
            sct.setEL(key, sb.toString())
        }
        return sct

        /*
		 * int index=0,last=0; while((index=str.indexOf('#',last))!=-1) { outer:while(str.length()+1>index)
		 * { c=str.charAt(index+1); if(c=='#' || c=='=') { last=index+1; continue; } }
		 * _unserialize(str.substring(last,index)); last=index+1; } _unserialize(str.substring(last));
		 */
    }

    @Throws(ConverterException::class)
    fun serialize(sct: Struct?): String? {
        // TODO Auto-generated method stub
        return serialize(sct, null)
    }

    @Throws(ConverterException::class)
    fun serialize(sct: Struct?, ignoreSet: Set?): String? {
        val sb = StringBuilder()
        val it: Iterator = sct.keyIterator()
        var doIt = false
        var oKey: Object
        while (it.hasNext()) {
            oKey = it.next()
            if (ignoreSet != null && ignoreSet.contains(oKey)) continue
            val key: String = Caster.toString(oKey, "")
            if (doIt) sb.append('#')
            doIt = true
            sb.append(escape(key))
            sb.append('=')
            sb.append(_serialize(sct.get(key, "")))
        }
        return sb.toString()
    }

    private fun escape(str: String?): String? {
        val len: Int = str!!.length()
        val sb = StringBuilder()
        var c: Char
        for (i in 0 until len) {
            c = str.charAt(i)
            if (c == '=') sb.append("#=") else if (c == '#') sb.append("##") else sb.append(c)
        }
        return sb.toString()
    }

    @Throws(ConverterException::class)
    private fun _serialize(`object`: Object?): String? {
        if (`object` == null) return "" else if (`object` is String) return escape(`object`.toString()) else if (`object` is Number) return Caster.toString(`object` as Number?) else if (`object` is Boolean) return Caster.toString((`object` as Boolean?).booleanValue()) else if (`object` is DateTime) return Caster.toString(`object`, null) else if (`object` is Date) return Caster.toString(`object`, null)
        throw ConverterException("Can't convert complex value [" + Caster.toTypeName(`object`).toString() + "] to a simple value")
    }
}