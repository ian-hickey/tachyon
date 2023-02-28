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
package tachyon.transformer.bytecode.util

import org.objectweb.asm.Type

object Methods_Caster {
    const val OBJECT = 0
    const val BOOLEAN = 1
    const val DOUBLE = 2
    const val STRING = 3

    // tachyon.runtime.type.Array toArray (Object)
    val TO_ARRAY: Method? = Method("toArray", Types.ARRAY, arrayOf<Type?>(Types.OBJECT))

    /*
	 * ext.img final public static Method TO_IMAGE = new Method("toImage", ImageUtil.getImageType(), new
	 * Type[]{Types.OBJECT,Types.PAGE_CONTEXT});
	 */
    // String toBase64 (Object)
    val TO_BASE64: Method? = Method("toBase64", Types.STRING, arrayOf<Type?>(Types.OBJECT))

    // byte[] toBinary(Object)
    val TO_BINARY: Method? = Method("toBinary", Types.BYTE_VALUE_ARRAY, arrayOf<Type?>(Types.OBJECT))

    // tachyon.runtime.type.Collection toCollection (Object)
    val TO_COLLECTION: Method? = Method("toCollection", Types.COLLECTION, arrayOf<Type?>(Types.OBJECT))

    // tachyon.runtime.Component toComponent (Object)
    val TO_COMPONENT: Method? = Method("toComponent", Types.COMPONENT, arrayOf<Type?>(Types.OBJECT))

    // java.io.File toFile (Object)
    val TO_FILE: Method? = Method("toFile", Types.FILE, arrayOf<Type?>(Types.OBJECT))

    // org.w3c.dom.Node toNode (Object)
    val TO_NODE: Method? = Method("toNode", Types.NODE, arrayOf<Type?>(Types.OBJECT))

    // Object toNull (Object)
    val TO_NULL: Method? = Method("toNull", Types.OBJECT, arrayOf<Type?>(Types.OBJECT))

    // tachyon.runtime.type.Query toQuery (Object)
    val TO_QUERY: Method? = Method("toQuery", Types.QUERY, arrayOf<Type?>(Types.OBJECT))

    // tachyon.runtime.type.Query toQueryColumn (Object)
    val TO_QUERY_COLUMN: Method? = Method("toQueryColumn", Types.QUERY_COLUMN, arrayOf<Type?>(Types.OBJECT, Types.PAGE_CONTEXT))

    // tachyon.runtime.type.Struct toStruct (Object)
    val TO_STRUCT: Method? = Method("toStruct", Types.STRUCT, arrayOf<Type?>(Types.OBJECT))

    // tachyon.runtime.type.dt.TimeSpan toTimespan (Object)
    val TO_TIMESPAN: Method? = Method("toTimespan", Types.TIMESPAN, arrayOf<Type?>(Types.OBJECT))
    val TO_TIMEZONE: Method? = Method("toTimeZone", Types.TIMEZONE, arrayOf<Type?>(Types.OBJECT))
    val TO_LOCALE: Method? = Method("toLocale", Types.LOCALE, arrayOf<Type?>(Types.OBJECT))
    val TO_STRING_BUFFER: Method? = Method("toStringBuffer", Types.STRING_BUFFER, arrayOf<Type?>(Types.OBJECT))

    //
    val TO_DECIMAL: Array<Method?>? = arrayOf<Method?>(Method("toDecimal", Types.STRING, arrayOf<Type?>(Types.OBJECT)),
            Method("toDecimal", Types.STRING, arrayOf<Type?>(Types.BOOLEAN_VALUE)), Method("toDecimal", Types.STRING, arrayOf<Type?>(Types.DOUBLE_VALUE)),
            Method("toDecimal", Types.STRING, arrayOf<Type?>(Types.STRING)))
    val TO_DATE: Array<Method?>? = arrayOf<Method?>(Method("toDate", Types.DATE_TIME, arrayOf<Type?>(Types.OBJECT, Types.TIMEZONE)),
            Method("toDate", Types.DATE_TIME, arrayOf<Type?>(Types.BOOLEAN, Types.TIMEZONE)), Method("toDate", Types.DATE_TIME, arrayOf<Type?>(Types.NUMBER, Types.TIMEZONE)),
            Method("toDate", Types.DATE_TIME, arrayOf<Type?>(Types.STRING, Types.TIMEZONE)))
    val TO_STRING: Array<Method?>? = arrayOf<Method?>(Method("toString", Types.STRING, arrayOf<Type?>(Types.OBJECT)),
            Method("toString", Types.STRING, arrayOf<Type?>(Types.BOOLEAN_VALUE)), Method("toString", Types.STRING, arrayOf<Type?>(Types.DOUBLE_VALUE)),
            Method("toString", Types.STRING, arrayOf<Type?>(Types.STRING)))
    val TO_BOOLEAN: Array<Method?>? = arrayOf<Method?>(Method("toBoolean", Types.BOOLEAN, arrayOf<Type?>(Types.OBJECT)),
            Method("toBoolean", Types.BOOLEAN, arrayOf<Type?>(Types.BOOLEAN_VALUE)), Method("toBoolean", Types.BOOLEAN, arrayOf<Type?>(Types.DOUBLE_VALUE)),
            Method("toBoolean", Types.BOOLEAN, arrayOf<Type?>(Types.STRING)))
    val TO_BOOLEAN_VALUE: Array<Method?>? = arrayOf<Method?>(Method("toBooleanValue", Types.BOOLEAN_VALUE, arrayOf<Type?>(Types.OBJECT)),
            Method("toBooleanValue", Types.BOOLEAN_VALUE, arrayOf<Type?>(Types.BOOLEAN_VALUE)),
            Method("toBooleanValue", Types.BOOLEAN_VALUE, arrayOf<Type?>(Types.DOUBLE_VALUE)), Method("toBooleanValue", Types.BOOLEAN_VALUE, arrayOf<Type?>(Types.STRING)))
    val TO_BYTE: Array<Method?>? = arrayOf<Method?>(Method("toByte", Types.BYTE, arrayOf<Type?>(Types.OBJECT)),
            Method("toByte", Types.BYTE, arrayOf<Type?>(Types.BOOLEAN)), Method("toByte", Types.BYTE, arrayOf<Type?>(Types.NUMBER)),
            Method("toByte", Types.BYTE, arrayOf<Type?>(Types.STRING)))
    val TO_BYTE_VALUE: Array<Method?>? = arrayOf<Method?>(Method("toByteValue", Types.BYTE_VALUE, arrayOf<Type?>(Types.OBJECT)),
            Method("toByteValue", Types.BYTE_VALUE, arrayOf<Type?>(Types.BOOLEAN)), Method("toByteValue", Types.BYTE_VALUE, arrayOf<Type?>(Types.NUMBER)),
            Method("toByteValue", Types.BYTE_VALUE, arrayOf<Type?>(Types.STRING)))
    val TO_CHARACTER: Array<Method?>? = arrayOf<Method?>(Method("toCharacter", Types.CHARACTER, arrayOf<Type?>(Types.OBJECT)),
            Method("toCharacter", Types.CHARACTER, arrayOf<Type?>(Types.BOOLEAN)), Method("toCharacter", Types.CHARACTER, arrayOf<Type?>(Types.NUMBER)),
            Method("toCharacter", Types.CHARACTER, arrayOf<Type?>(Types.STRING)))
    val TO_CHAR_VALUE: Array<Method?>? = arrayOf<Method?>(Method("toCharValue", Types.CHAR, arrayOf<Type?>(Types.OBJECT)),
            Method("toCharValue", Types.CHAR, arrayOf<Type?>(Types.BOOLEAN)), Method("toCharValue", Types.CHAR, arrayOf<Type?>(Types.NUMBER)),
            Method("toCharValue", Types.CHAR, arrayOf<Type?>(Types.STRING)))
    val TO_SHORT: Array<Method?>? = arrayOf<Method?>(Method("toShort", Types.SHORT, arrayOf<Type?>(Types.OBJECT)),
            Method("toShort", Types.SHORT, arrayOf<Type?>(Types.BOOLEAN)), Method("toShort", Types.SHORT, arrayOf<Type?>(Types.NUMBER)),
            Method("toShort", Types.SHORT, arrayOf<Type?>(Types.STRING)))
    val TO_SHORT_VALUE: Array<Method?>? = arrayOf<Method?>(Method("toShortValue", Types.SHORT_VALUE, arrayOf<Type?>(Types.OBJECT)),
            Method("toShortValue", Types.SHORT_VALUE, arrayOf<Type?>(Types.BOOLEAN)), Method("toShortValue", Types.SHORT_VALUE, arrayOf<Type?>(Types.NUMBER)),
            Method("toShortValue", Types.SHORT_VALUE, arrayOf<Type?>(Types.STRING)))
    val TO_INTEGER: Array<Method?>? = arrayOf<Method?>(Method("toInteger", Types.INTEGER, arrayOf<Type?>(Types.OBJECT)),
            Method("toInteger", Types.INTEGER, arrayOf<Type?>(Types.BOOLEAN)), Method("toInteger", Types.INTEGER, arrayOf<Type?>(Types.NUMBER)),
            Method("toInteger", Types.INTEGER, arrayOf<Type?>(Types.STRING)))
    val TO_INT_VALUE: Array<Method?>? = arrayOf<Method?>(Method("toIntValue", Types.INT_VALUE, arrayOf<Type?>(Types.OBJECT)),
            Method("toIntValue", Types.INT_VALUE, arrayOf<Type?>(Types.BOOLEAN)), Method("toIntValue", Types.INT_VALUE, arrayOf<Type?>(Types.NUMBER)),
            Method("toIntValue", Types.INT_VALUE, arrayOf<Type?>(Types.STRING)))
    val TO_LONG: Array<Method?>? = arrayOf<Method?>(Method("toLong", Types.LONG, arrayOf<Type?>(Types.OBJECT)),
            Method("toLong", Types.LONG, arrayOf<Type?>(Types.BOOLEAN)), Method("toLong", Types.LONG, arrayOf<Type?>(Types.NUMBER)),
            Method("toLong", Types.LONG, arrayOf<Type?>(Types.STRING)))
    val TO_LONG_VALUE: Array<Method?>? = arrayOf<Method?>(Method("toLongValue", Types.LONG_VALUE, arrayOf<Type?>(Types.OBJECT)),
            Method("toLongValue", Types.LONG_VALUE, arrayOf<Type?>(Types.BOOLEAN)), Method("toLongValue", Types.LONG_VALUE, arrayOf<Type?>(Types.NUMBER)),
            Method("toLongValue", Types.LONG_VALUE, arrayOf<Type?>(Types.STRING)))
    val TO_FLOAT: Array<Method?>? = arrayOf<Method?>(Method("toFloat", Types.FLOAT, arrayOf<Type?>(Types.OBJECT)),
            Method("toFloat", Types.FLOAT, arrayOf<Type?>(Types.BOOLEAN)), Method("toFloat", Types.FLOAT, arrayOf<Type?>(Types.NUMBER)),
            Method("toFloat", Types.FLOAT, arrayOf<Type?>(Types.STRING)))
    val TO_FLOAT_VALUE: Array<Method?>? = arrayOf<Method?>(Method("toFloatValue", Types.FLOAT_VALUE, arrayOf<Type?>(Types.OBJECT)),
            Method("toFloatValue", Types.FLOAT_VALUE, arrayOf<Type?>(Types.BOOLEAN)), Method("toFloatValue", Types.FLOAT_VALUE, arrayOf<Type?>(Types.NUMBER)),
            Method("toFloatValue", Types.FLOAT_VALUE, arrayOf<Type?>(Types.STRING)))
    val TO_DOUBLE: Array<Method?>? = arrayOf<Method?>(Method("toDouble", Types.DOUBLE, arrayOf<Type?>(Types.OBJECT)),
            Method("toDouble", Types.DOUBLE, arrayOf<Type?>(Types.BOOLEAN_VALUE)), Method("toDouble", Types.DOUBLE, arrayOf<Type?>(Types.DOUBLE_VALUE)),
            Method("toDouble", Types.DOUBLE, arrayOf<Type?>(Types.STRING)))
    val TO_DOUBLE_VALUE: Array<Method?>? = arrayOf<Method?>(Method("toDoubleValue", Types.DOUBLE_VALUE, arrayOf<Type?>(Types.OBJECT)),
            Method("toDoubleValue", Types.DOUBLE_VALUE, arrayOf<Type?>(Types.BOOLEAN_VALUE)), Method("toDoubleValue", Types.DOUBLE_VALUE, arrayOf<Type?>(Types.DOUBLE_VALUE)),
            Method("toDoubleValue", Types.DOUBLE_VALUE, arrayOf<Type?>(Types.STRING)))
}