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
package lucee.commons.sql

import java.lang.reflect.Method

object OracleBlob {
    private var duration: Integer? = null
    private var mode: Integer? = null
    private var createTemporary: Method? = null
    private var open: Method? = null
    private var setBytes: Method? = null
    fun createBlob(conn: Connection?, barr: ByteArray, defaultValue: Blob?): Blob? {
        try {
            val clazz: Class = ClassUtil.loadClass("oracle.sql.BLOB")

            // BLOB.DURATION_SESSION
            if (duration == null) duration = Caster.toInteger(clazz.getField("DURATION_SESSION").getInt(null))
            // BLOB.MODE_READWRITE
            if (mode == null) mode = Caster.toInteger(clazz.getField("MODE_READWRITE").getInt(null))

            // BLOB blob = BLOB.createTemporary(conn, false, BLOB.DURATION_SESSION);
            if (createTemporary == null || createTemporary.getDeclaringClass() !== clazz) createTemporary = clazz.getMethod("createTemporary", arrayOf<Class>(Connection::class.java, Boolean::class.javaPrimitiveType, Int::class.javaPrimitiveType))
            val blob: Object = createTemporary.invoke(null, arrayOf<Object?>(conn, Boolean.FALSE, duration))

            // blob.open(BLOB.MODE_READWRITE);
            if (open == null || open.getDeclaringClass() !== clazz) open = clazz.getMethod("open", arrayOf<Class>(Int::class.javaPrimitiveType))
            open.invoke(blob, arrayOf<Object?>(mode))

            // blob.setBytes(1,barr);
            if (setBytes == null || setBytes.getDeclaringClass() !== clazz) setBytes = clazz.getMethod("setBytes", arrayOf<Class>(Long::class.javaPrimitiveType, ByteArray::class.java))
            setBytes.invoke(blob, arrayOf(Long.valueOf(1), barr))
            return blob as Blob
        } catch (t: Throwable) {
            ExceptionUtil.rethrowIfNecessary(t)
            // print.printST(t);
        }
        return defaultValue
    }
}