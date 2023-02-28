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
package lucee.runtime.functions.file

import java.io.IOException

object FileSetAttribute {
    @Throws(PageException::class)
    fun call(pc: PageContext?, oSrc: Object?, attr: String?): String? {
        var attr = attr
        val src: Resource = Caster.toResource(pc, oSrc, false)
        pc.getConfig().getSecurityManager().checkFileLocation(src)
        attr = attr.trim().toLowerCase()
        try {
            if ("archive".equals(attr)) {
                src.setAttribute(Resource.ATTRIBUTE_ARCHIVE, true)
            } else if ("hidden".equals(attr)) {
                src.setAttribute(Resource.ATTRIBUTE_HIDDEN, true)
            } else if ("readonly".equals(attr)) {
                src.setWritable(false)
            } else if ("system".equals(attr)) {
                src.setAttribute(Resource.ATTRIBUTE_SYSTEM, true)
            } else if ("normal".equals(attr)) {
                src.setAttribute(Resource.ATTRIBUTE_ARCHIVE, false)
                src.setAttribute(Resource.ATTRIBUTE_HIDDEN, false)
                src.setAttribute(Resource.ATTRIBUTE_SYSTEM, false)
                src.setWritable(true)
            } else throw FunctionException(pc, "FileSetAttribute", 3, "attribute", "invalid value [$attr], valid values are [normal,archive,hidden,system,readonly]")
        } catch (ioe: IOException) {
            throw Caster.toPageException(ioe)
        }
        return null
    }
}