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
 * Implements the CFML Function javacast
 */
package lucee.runtime.functions.string

import java.math.BigDecimal

object JavaCast : Function {
    private const val serialVersionUID = -5053403312467568511L
    @Throws(PageException::class)
    fun calls(pc: PageContext?, string: String?, `object`: Object?): Object? {
        throw ExpressionException("method javacast not implemented yet") // MUST ????
    }

    @Throws(PageException::class)
    fun call(pc: PageContext?, type: String?, obj: Object?): Object? {
        var type = type
        type = type.trim()
        val lcType: String = StringUtil.toLowerCase(type)
        if (type.endsWith("[]")) {
            return toArray(pc, type, lcType, obj)
        }
        val clazz: Class<*>? = toClass(pc, lcType, type)
        return to(pc, obj, clazz)
    }

    @Throws(PageException::class)
    fun toArray(pc: PageContext?, type: String?, lcType: String?, obj: Object?): Object? {
        // byte
        if ("byte[]".equals(lcType)) {
            if (obj is ByteArray) return obj
            if (Decision.isBinary(obj)) return Caster.toBinary(obj)
        } else if ("char[]".equals(lcType)) {
            if (obj is CharArray) return obj
            if (obj is CharSequence) return obj.toString().toCharArray()
        }
        return _toArray(pc, type, lcType, obj)
    }

    @Throws(PageException::class)
    fun _toArray(pc: PageContext?, type: String?, lcType: String?, obj: Object?): Object? {
        var type = type
        var lcType = lcType
        lcType = lcType.substring(0, lcType!!.length() - 2)
        type = type.substring(0, type!!.length() - 2)

        // other
        val arr: Array<Object?> = Caster.toList(obj).toArray()
        val clazz: Class<*>? = toClass(pc, lcType, type)
        val trg: Object = java.lang.reflect.Array.newInstance(clazz, arr.size)
        for (i in arr.indices.reversed()) {
            java.lang.reflect.Array.set(trg, i, if (type.endsWith("[]")) _toArray(pc, type, lcType, arr[i]) else to(pc, arr[i], clazz))
        }
        return trg
    }

    @Throws(PageException::class)
    private fun to(pc: PageContext?, obj: Object?, trgClass: Class<*>?): Object? {
        if (trgClass == null) return Caster.toNull(obj) else if (trgClass === BigDecimal::class.java) return Caster.toBigDecimal(obj) else if (trgClass === BigInteger::class.java) return Caster.toBigInteger(obj)
        return Caster.castTo(pc, trgClass, obj)
        // throw new ExpressionException("can't cast only to the following data types (bigdecimal,int, long,
        // float ,double ,boolean ,string,null ), "+lcType+" is invalid");
    }

    @Throws(PageException::class)
    private fun toClass(pc: PageContext?, lcType: String?, type: String?): Class<*>? {
        if (lcType!!.equals("null")) {
            return null
        }
        if (lcType.equals("biginteger")) {
            return BigInteger::class.java
        }
        return if (lcType.equals("bigdecimal")) {
            BigDecimal::class.java
        } else try {
            ClassUtil.toClass(type)
        } catch (e: ClassException) {
            throw Caster.toPageException(e)
        }
    }
}