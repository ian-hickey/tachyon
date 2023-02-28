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
 * Implements the CFML Function getbasetagdata
 */
package tachyon.runtime.functions.other

import javax.servlet.jsp.tagext.Tag

object GetBaseTagData : Function {
    private const val serialVersionUID = -7016207088098049143L
    @Throws(PageException::class)
    fun call(pc: PageContext?, tagName: String?): Struct? {
        return call(pc, tagName, -1.0)
    }

    @Throws(PageException::class)
    fun call(pc: PageContext?, tagName: String?, minLevel: Double): Struct? {
        val tag: CFTag = getParentCFTag(pc.getCurrentTag(), tagName, minLevel.toInt())
                ?: throw ExpressionException("can't find base tag with name [$tagName]")
        return tag.getVariablesScope()
    }

    fun getParentCFTag(tag: Tag?, trgTagName: String?, minLevel: Int): CFTag? {
        var tag: Tag? = tag
        var pureName = trgTagName
        var level = 0
        var cfTag: CFTag?
        while (tag != null) {
            if (tag is CFTag && minLevel <= level++) {
                cfTag = tag as CFTag?
                if (cfTag is CFTagCore) {
                    val tc: CFTagCore? = cfTag as CFTagCore?
                    if ((tc.getName().toString() + "").equalsIgnoreCase(pureName)) return cfTag
                    if (StringUtil.startsWithIgnoreCase(pureName, "cf")) {
                        pureName = pureName.substring(2)
                    }
                    if ((tc.getName().toString() + "").equalsIgnoreCase(pureName)) return cfTag
                } else if (cfTag.getAppendix().equalsIgnoreCase(pureName)) {
                    return cfTag
                } else if (StringUtil.startsWithIgnoreCase(pureName, "cf_")) {
                    pureName = pureName.substring(3)
                    if (cfTag.getAppendix().equalsIgnoreCase(pureName)) return cfTag
                }
            }
            tag = tag.getParent()
        }
        return null
    }
}