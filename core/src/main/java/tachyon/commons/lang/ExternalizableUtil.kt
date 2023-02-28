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
package tachyon.commons.lang

import java.io.IOException

object ExternalizableUtil {
    @Throws(ClassNotFoundException::class, IOException::class)
    fun readString(`in`: ObjectInput): String {
        return `in`.readObject()
    }

    @Throws(IOException::class)
    fun writeString(out: ObjectOutput, str: String?) {
        // if(str==null) out.writeObject(""); string and null is not necessary the same
        out.writeObject(str)
    }

    @Throws(IOException::class)
    fun readBoolean(`in`: ObjectInput): Boolean? {
        val b: Int = `in`.readInt()
        if (b == -1) return null
        return if (b == 1) Boolean.TRUE else Boolean.FALSE
    }

    @Throws(IOException::class)
    fun writeBoolean(out: ObjectOutput, b: Boolean?) {
        if (b == null) out.writeInt(-1) else out.writeInt(if (b.booleanValue()) 1 else 0)
    }
}