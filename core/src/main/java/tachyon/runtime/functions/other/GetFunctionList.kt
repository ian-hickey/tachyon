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
 * Implements the CFML Function getfunctionlist
 */
package tachyon.runtime.functions.other

import java.util.ArrayList

object GetFunctionList : Function {
    /**
     *
     */
    private const val serialVersionUID = -7313412061811118382L
    @Throws(PageException::class)
    fun call(pc: PageContext?): tachyon.runtime.type.Struct? {
        return _call(pc, pc.getCurrentTemplateDialect())
    }

    @Throws(PageException::class)
    fun call(pc: PageContext?, strDialect: String?): tachyon.runtime.type.Struct? {
        val dialect: Int = ConfigWebUtil.toDialect(strDialect, -1)
        if (dialect == -1) throw FunctionException(pc, "GetFunctionList", 1, "dialect", "value [$strDialect] is invalid, valid values are [cfml,tachyon]")
        return _call(pc, dialect)
    }

    @Throws(PageException::class)
    private fun _call(pc: PageContext?, dialect: Int): tachyon.runtime.type.Struct? {
        val sct: Struct = StructImpl(StructImpl.TYPE_LINKED)
        // synchronized(sct) {
        // hasSet=true;
        val flds: Array<FunctionLib?>
        flds = (pc.getConfig() as ConfigPro).getFLDs(dialect)
        var func: FunctionLibFunction
        var _functions: Map<String?, FunctionLibFunction?>
        var it: Iterator<Entry<String?, FunctionLibFunction?>?>
        var e: Entry<String?, FunctionLibFunction?>?
        val tagList: ArrayList<String?> = ArrayList()
        for (i in flds.indices) {
            _functions = flds[i].getFunctions()
            it = _functions.entrySet().iterator()
            while (it.hasNext()) {
                e = it.next()
                func = e.getValue()
                if (func.getStatus() !== TagLib.STATUS_HIDDEN && func.getStatus() !== TagLib.STATUS_UNIMPLEMENTED) {
                    // sct.set(e.getKey(), "");
                    tagList.add(e.getKey())
                }
            }
            Collections.sort(tagList)
            for (t in tagList) {
                sct.put(t, "")
            }
        }
        return sct
    }
}