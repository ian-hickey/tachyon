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
package tachyon.commons.sql

import java.lang.reflect.Method

object OracleClob {
    private var duration: Integer? = null
    private var mode: Integer? = null
    private var createTemporary: Method? = null
    private var open: Method? = null
    private var setString: Method? = null
    fun createClob(conn: Connection?, value: String, defaultValue: Clob?): Clob? {
        try {
            val clazz: Class = ClassUtil.loadClass("oracle.sql.CLOB")

            // CLOB.DURATION_SESSION;
            if (duration == null) duration = Caster.toInteger(clazz.getField("DURATION_SESSION").getInt(null))
            // CLOB.MODE_READWRITE
            if (mode == null) mode = Caster.toInteger(clazz.getField("MODE_READWRITE").getInt(null))

            // CLOB c = CLOB.createTemporary(conn, false, CLOB.DURATION_SESSION);
            if (createTemporary == null || createTemporary.getDeclaringClass() !== clazz) createTemporary = clazz.getMethod("createTemporary", arrayOf<Class>(Connection::class.java, Boolean::class.javaPrimitiveType, Int::class.javaPrimitiveType))
            val clob: Object = createTemporary.invoke(null, arrayOf<Object?>(conn, Boolean.FALSE, duration))

            // c.open(CLOB.MODE_READWRITE);
            if (open == null || open.getDeclaringClass() !== clazz) open = clazz.getMethod("open", arrayOf<Class>(Int::class.javaPrimitiveType))
            open.invoke(clob, arrayOf<Object?>(mode))

            // c.setString(1,value);
            if (setString == null || setString.getDeclaringClass() !== clazz) setString = clazz.getMethod("setString", arrayOf<Class>(Long::class.javaPrimitiveType, String::class.java))
            setString.invoke(clob, arrayOf(Long.valueOf(1), value))
            return clob as Clob
        } catch (t: Throwable) {
            ExceptionUtil.rethrowIfNecessary(t)
            // print.printST(t);
        }
        return defaultValue
    }
}