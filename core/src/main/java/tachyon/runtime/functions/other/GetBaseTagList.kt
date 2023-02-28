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
 * Implements the CFML Function getbasetaglist
 */
package tachyon.runtime.functions.other

import java.util.Iterator

object GetBaseTagList : Function {
    fun call(pc: PageContext?): String? {
        return call(pc, ",")
    }

    fun call(pc: PageContext?, delimiter: String?): String? {
        var tag: Tag = pc.getCurrentTag()
        val sb = StringBuilder()
        while (tag != null) {
            if (sb.length() > 0) sb.append(delimiter)
            sb.append(getName(pc, tag))
            tag = tag.getParent()
        }
        return sb.toString()
    }

    private fun getName(pc: PageContext?, tag: Tag?): String? {
        var clazz: Class = tag.getClass()
        if (clazz === CFImportTag::class.java) clazz = CFTag::class.java
        val className: String = clazz.getName()
        val tlds: Array<TagLib?> = (pc.getConfig() as ConfigPro).getTLDs(pc.getCurrentTemplateDialect())
        var tlt: TagLibTag
        for (i in tlds.indices) {
            // String ns = tlds[i].getNameSpaceAndSeparator();
            val tags: Map = tlds[i].getTags()
            val it: Iterator = tags.keySet().iterator()
            while (it.hasNext()) {
                tlt = tags.get(it.next()) as TagLibTag
                if (tlt.getTagClassDefinition().isClassNameEqualTo(className)) {
                    // custm tag
                    if (tag is AppendixTag) {
                        val atag: AppendixTag? = tag as AppendixTag?
                        if (atag.getAppendix() != null && tag !is Module) {
                            return tlt.getFullName().toUpperCase() + atag.getAppendix().toUpperCase()
                        }
                    }
                    // built in cfc based custom tag
                    if (tag is CFTagCore) {
                        if ((tag as CFTagCore?).getName().equals(tlt.getAttribute("__name").getDefaultValue())) return tlt.getFullName().toUpperCase()
                        continue
                    }
                    return tlt.getFullName().toUpperCase()
                }
            }
        }
        return ListUtil.last(className, ".", true).toUpperCase()
    }
}