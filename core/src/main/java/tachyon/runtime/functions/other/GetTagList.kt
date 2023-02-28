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

object GetTagList : Function {
    private const val serialVersionUID = -5143967669895264247L
    @Throws(PageException::class)
    fun call(pc: PageContext?): tachyon.runtime.type.Struct? {
        return _call(pc, pc.getCurrentTemplateDialect())
    }

    @Throws(PageException::class)
    fun call(pc: PageContext?, strDialect: String?): tachyon.runtime.type.Struct? {
        val dialect: Int = ConfigWebUtil.toDialect(strDialect, -1)
        if (dialect == -1) throw FunctionException(pc, "GetTagList", 1, "dialect", "invalid dialect [$strDialect] definition")
        return _call(pc, dialect)
    }

    @Throws(PageException::class)
    private fun _call(pc: PageContext?, dialect: Int): tachyon.runtime.type.Struct? {
        val sct: Struct = StructImpl(StructImpl.TYPE_LINKED)
        // synchronized(sct) {
        // hasSet=true;
        val tlds: Array<TagLib?>
        var tag: TagLibTag
        tlds = (pc.getConfig() as ConfigPro).getTLDs(dialect)
        for (i in tlds.indices) {
            val ns: String = tlds[i].getNameSpaceAndSeparator()
            val tags: Map<String?, TagLibTag?> = tlds[i].getTags()
            val it: Iterator<String?> = tags.keySet().iterator()
            val inner: Struct = StructImpl(StructImpl.TYPE_LINKED)
            sct.set(ns, inner)
            val tagList: ArrayList<String?> = ArrayList()
            while (it.hasNext()) {
                val n: Object? = it.next()
                tag = tlds[i].getTag(n.toString())
                if (tag.getStatus() !== TagLib.STATUS_HIDDEN && tag.getStatus() !== TagLib.STATUS_UNIMPLEMENTED) {
                    // inner.set(n.toString(), "");
                    tagList.add(n.toString())
                }
            }
            Collections.sort(tagList)
            for (t in tagList) {
                inner.put(t, "")
            }
        }
        // }
        // }
        return sct
    }
}