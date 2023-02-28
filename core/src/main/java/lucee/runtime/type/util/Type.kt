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
package lucee.runtime.type.util

import lucee.runtime.Component

object Type {
    fun getName(o: Object?): String? {
        if (o == null) return "null"
        return if (o is UDF) "user defined function (" + (o as UDF?).getFunctionName().toString() + ")" else if (o is Boolean) "Boolean" else if (o is Number) "Number" else if (o is TimeSpan) "TimeSpan" else if (o is Array) "Array" else if (o is Component) "Component " + (o as Component?).getAbsName() else if (o is Scope) (o as Scope?).getTypeAsString() else if (o is Struct) {
            if (o is XMLStruct) "XML" else "Struct"
        } else if (o is Query) "Query" else if (o is DateTime) "DateTime" else if (o is ByteArray) "Binary" else {
            val className: String = o.getClass().getName()
            if (className.startsWith("java.lang.")) {
                className.substring(10)
            } else className
        }
    }

    fun getName(clazz: Class?): String? {
        if (clazz == null) return "null"
        // String name=clazz.getName();
        // if(Reflector.isInstaneOf(clazz,String.class)) return "String";
        return if (Reflector.isInstaneOf(clazz, UDF::class.java, false)) "user defined function" else if (Reflector.isInstaneOf(clazz, Array::class.java, false)) "Array" else if (Reflector.isInstaneOf(clazz, Struct::class.java, false)) "Struct" else if (Reflector.isInstaneOf(clazz, Query::class.java, false)) "Query" else if (Reflector.isInstaneOf(clazz, DateTime::class.java, false)) "DateTime" else if (Reflector.isInstaneOf(clazz, Component::class.java, false)) "Component" else if (Reflector.isInstaneOf(clazz, ByteArray::class.java, false)) "Binary" else {
            val className: String = clazz.getName()
            if (className.startsWith("java.lang.")) {
                className.substring(10)
            } else className
        }
    }
}