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
package tachyon.runtime.exp

import tachyon.commons.lang.StringUtil

/**
 *
 */
class CasterException : ExpressionException {
    /**
     * constructor of the Exception
     *
     * @param o
     * @param type
     */
    constructor(o: Object?, type: String?) : super(createMessage(o, type), createDetail(o)) {}
    constructor(o: Object?, clazz: Class?) : super(createMessage(o, Caster.toTypeName(clazz)), createDetail(o)) {}

    /**
     * constructor of the Exception
     *
     * @param message
     */
    constructor(message: String?) : super(message) {}
    constructor(message: String?, detail: String?) : super(message, detail) {}

    companion object {
        private fun createDetail(o: Object?): String? {
            return if (o != null) "Java type of the object is " + Caster.toClassName(o) else "value is null"
        }

        fun createMessage(o: Object?, type: String?): String? {
            if (o is String) return "Can't cast String [" + crop(o.toString()) + "] to a value of type [" + type + "]"
            return if (o != null) "Can't cast Object type [" + Type.getName(o).toString() + "] to a value of type [" + type.toString() + "]" else "Can't cast Null value to value of type [$type]"
        }

        fun crop(obj: Object?): String? {
            val max = 100
            val str: String = obj.toString()
            return if (StringUtil.isEmpty(str) || str.length() <= max) str else str.substring(0, max).toString() + "..."
        }
    }
}