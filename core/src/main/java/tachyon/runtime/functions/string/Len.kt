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
 * Implements the CFML Function len
 */
package tachyon.runtime.functions.string

import java.util.List

object Len : Function {
    fun call(pc: PageContext?, string: String?): Double {
        return string!!.length()
    }

    @Throws(FunctionException::class)
    fun call(pc: PageContext?, obj: Object?): Double {
        val len = invoke(obj, -1.0)
        if (len == -1.0) throw FunctionException(pc, "len", 1, "object", "this type  [" + Caster.toTypeName(obj).toString() + "] is not supported for returning the len")
        return len
    }

    operator fun invoke(obj: Object?, defaultValue: Double): Double {
        if (obj is CharSequence) return (obj as CharSequence?)!!.length()
        if (obj is Query) return (obj as Query?).getRecordcount()
        if (obj is Collection) return (obj as Collection?)!!.size()
        if (obj is Map) return (obj as Map?)!!.size()
        if (obj is List) return (obj as List?)!!.size()
        if (obj is Array<Object>) return (obj as Array<Object?>?)!!.size.toDouble()
        if (obj is ShortArray) return (obj as ShortArray?)!!.size.toDouble()
        if (obj is IntArray) return (obj as IntArray?)!!.size.toDouble()
        if (obj is FloatArray) return (obj as FloatArray?)!!.size.toDouble()
        if (obj is DoubleArray) return (obj as DoubleArray?)!!.size.toDouble()
        if (obj is LongArray) return (obj as LongArray?)!!.size.toDouble()
        if (obj is CharArray) return (obj as CharArray?)!!.size.toDouble()
        if (obj is BooleanArray) return (obj as BooleanArray?)!!.size.toDouble()
        if (obj is ByteArray) return (obj as ByteArray?)!!.size.toDouble()
        val str: String = Caster.toString(obj, null)
        return str?.length() ?: defaultValue
    }
}