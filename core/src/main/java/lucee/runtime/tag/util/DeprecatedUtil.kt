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
package lucee.runtime.tag.util

import lucee.commons.io.log.Log

object DeprecatedUtil {
    fun tagAttribute(tagName: String?, attrName: String?) {
        tagAttribute(null, tagName, attrName)
    }

    fun tagAttribute(pc: PageContext?, tagName: String?, attrName: String?) {
        var pc: PageContext? = pc
        pc = ThreadLocalPageContext.get(pc)
        if (pc == null) return
        LogUtil.log(pc, Log.LEVEL_ERROR, DeprecatedUtil::class.java.getName(), "attribute $attrName of the tag $tagName is no longer supported and ignored.")
    }

    fun function(pc: PageContext?, old: String?) {
        var pc: PageContext? = pc
        pc = ThreadLocalPageContext.get(pc)
        if (pc == null) return
        LogUtil.log(pc, Log.LEVEL_ERROR, DeprecatedUtil::class.java.getName(), "function $old is deprecated")
    }

    fun function(pc: PageContext?, old: String?, replacement: String?) {
        var pc: PageContext? = pc
        pc = ThreadLocalPageContext.get(pc)
        if (pc == null) return
        LogUtil.log(pc, Log.LEVEL_ERROR, DeprecatedUtil::class.java.getName(), "function $old is deprecated, please use instead function $replacement")
    }
}