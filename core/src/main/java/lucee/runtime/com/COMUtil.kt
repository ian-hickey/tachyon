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
package lucee.runtime.com

import java.lang.reflect.Method

/**
 *
 */
object COMUtil {
    /**
     * translate a Variant Object to Object, when it is a Dispatch translate it to COMWrapper
     *
     * @param parent
     * @param variant
     * @param key
     * @return Object from Variant
     */
    fun toObject(parent: COMObject?, variant: Variant?, key: String?, defaultValue: Object?): Object? {
        return try {
            toObject(parent, variant, key)
        } catch (ee: ExpressionException) {
            defaultValue
        }
    }

    /**
     * translate a Variant Object to Object, when it is a Dispatch translate it to COMWrapper
     *
     * @param parent
     * @param variant
     * @param key
     * @return Object from Variant
     * @throws ExpressionException
     */
    @Throws(ExpressionException::class)
    fun toObject(parent: COMObject?, variant: Variant?, key: String?): Object? {
        val type: Short = variant.getvt()
        // print.ln(key+" -> variant.getvt("+toStringType(type)+")");

        /*
		 * TODO impl this Variant.VariantByref; Variant.VariantError; Variant.VariantTypeMask;
		 */if (type == Variant.VariantEmpty) return null else if (type == Variant.VariantNull) return null else if (type == Variant.VariantShort) return Short.valueOf(variant.getShort()) else if (type == Variant.VariantInt) return Integer.valueOf(variant.getInt()) else if (type == Variant.VariantFloat) return Float.valueOf(variant.getFloat()) else if (type == Variant.VariantDouble) return Double.valueOf(variant.getDouble()) else if (type == Variant.VariantCurrency) {
            val l: Long
            l = try {
                variant.getCurrency().longValue()
            } // this reflection allows support for old and new jacob version
            catch (t: Throwable) {
                ExceptionUtil.rethrowIfNecessary(t)
                try {
                    val toCurrency: Method = variant.getClass().getMethod("toCurrency", arrayOfNulls<Class?>(0))
                    val curreny: Object = toCurrency.invoke(variant, arrayOfNulls<Object?>(0))
                    val longValue: Method = curreny.getClass().getMethod("longValue", arrayOfNulls<Class?>(0))
                    Caster.toLongValue(longValue.invoke(curreny, arrayOfNulls<Object?>(0)), 0)
                } catch (t2: Throwable) {
                    ExceptionUtil.rethrowIfNecessary(t2)
                    0
                }
            }
            return Long.valueOf(l)
        } else if (type == Variant.VariantObject) return variant.toEnumVariant() else if (type == Variant.VariantDate) return DateTimeImpl(variant.getDate() as Long, true) else if (type == Variant.VariantString) return variant.getString() else if (type == Variant.VariantBoolean) return if (variant.getBoolean()) Boolean.TRUE else Boolean.FALSE else if (type == Variant.VariantByte) return Byte.valueOf(variant.getByte()) else if (type == Variant.VariantVariant) {
            throw ExpressionException("type variant is not supported")
            // return toObject(variant.getV.get());
        } else if (type == Variant.VariantArray) {
            val varr: Array<Variant?> = variant.getVariantArrayRef()
            val oarr: Array<Object?> = arrayOfNulls<Object?>(varr.size)
            for (i in varr.indices) {
                oarr[i] = toObject(parent, varr[i], Caster.toString(i))
            }
            return ArrayImpl(oarr)
        } else if (type == Variant.VariantDispatch) {
            return COMObject(variant, variant.toDispatch(), parent.getName().toString() + "." + key)
        }
        throw ExpressionException("COM Type [" + toStringType(type) + "] not supported")
    }

    /**
     * translate a short Variant Type Definition to a String (string,empty,null,short ...)
     *
     * @param type
     * @return String Variant Type
     */
    fun toStringType(type: Short): String? {
        return if (type == Variant.VariantEmpty) "empty" else if (type == Variant.VariantNull) "null" else if (type == Variant.VariantShort) "Short" else if (type == Variant.VariantInt) "Integer" else if (type == Variant.VariantFloat) "Float" else if (type == Variant.VariantDouble) "Double" else if (type == Variant.VariantCurrency) "Currency" else if (type == Variant.VariantDate) "Date" else if (type == Variant.VariantString) "String" else if (type == Variant.VariantBoolean) "Boolean" else if (type == Variant.VariantByte) "Byte" else if (type == Variant.VariantArray) "Array" else if (type == Variant.VariantDispatch) "Dispatch" else if (type == Variant.VariantByref) "Byref" else if (type == Variant.VariantCurrency) "Currency" else if (type == Variant.VariantError) "Error" else if (type == Variant.VariantInt) "int" else if (type == Variant.VariantObject) "Object" else if (type == Variant.VariantTypeMask) "TypeMask" else if (type == Variant.VariantVariant) "Variant" else "unknown"
    }
}