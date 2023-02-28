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

object Methods {
    // Caster String
    // String toString (Object)
    val METHOD_TO_STRING: Method? = Method("toString", Types.STRING, arrayOf<Type?>(Types.OBJECT))

    // String toString (String)
    // final public static Method METHOD_TO_STRING_FROM_STRING = new Method("toString",Types.STRING,new
    // Type[]{Types.STRING});
    // String toString (double)
    val METHOD_TO_STRING_FROM_DOUBLE_VALUE: Method? = Method("toString", Types.STRING, arrayOf<Type?>(Types.DOUBLE_VALUE))
    val METHOD_TO_STRING_FROM_NUMBER: Method? = Method("toString", Types.STRING, arrayOf<Type?>(Types.NUMBER))

    // String toString (boolean)
    val METHOD_TO_STRING_FROM_BOOLEAN: Method? = Method("toString", Types.STRING, arrayOf<Type?>(Types.BOOLEAN_VALUE))

    // Caster Boolean
    // Boolean toBoolean (Object)
    val METHOD_TO_BOOLEAN: Method? = Method("toBoolean", Types.BOOLEAN, arrayOf<Type?>(Types.OBJECT))

    // boolean toBooleanValue (Object)
    val METHOD_TO_BOOLEAN_VALUE: Method? = Method("toBooleanValue", Types.BOOLEAN_VALUE, arrayOf<Type?>(Types.OBJECT))

    // Boolean toBoolean (double)
    val METHOD_TO_BOOLEAN_FROM_DOUBLE_VALUE: Method? = Method("toBoolean", Types.BOOLEAN, arrayOf<Type?>(Types.DOUBLE_VALUE))
    val METHOD_TO_BOOLEAN_FROM_NUMBER: Method? = Method("toBoolean", Types.BOOLEAN, arrayOf<Type?>(Types.NUMBER))

    // boolean toBooleanValue (double)
    val METHOD_TO_BOOLEAN_VALUE_FROM_DOUBLE_VALUE: Method? = Method("toBooleanValue", Types.BOOLEAN_VALUE, arrayOf<Type?>(Types.DOUBLE_VALUE))
    val METHOD_TO_BOOLEAN_VALUE_FROM_NUMBER: Method? = Method("toBooleanValue", Types.BOOLEAN_VALUE, arrayOf<Type?>(Types.NUMBER))

    // Boolean toBoolean (boolean)
    val METHOD_TO_BOOLEAN_FROM_BOOLEAN: Method? = Method("toBoolean", Types.BOOLEAN, arrayOf<Type?>(Types.BOOLEAN_VALUE))

    // boolean toBooleanValue (boolean)
    // final public static Method METHOD_TO_BOOLEAN_VALUE_FROM_BOOLEAN = new
    // Method("toBooleanValue",Types.BOOLEAN_VALUE,new Type[]{Types.BOOLEAN_VALUE});
    // Boolean toBoolean (String)
    val METHOD_TO_BOOLEAN_FROM_STRING: Method? = Method("toBoolean", Types.BOOLEAN, arrayOf<Type?>(Types.STRING))

    // boolean toBooleanValue (String)
    val METHOD_TO_BOOLEAN_VALUE_FROM_STRING: Method? = Method("toBooleanValue", Types.BOOLEAN_VALUE, arrayOf<Type?>(Types.STRING))

    // Caster Double
    // Double toDouble (Object)
    val METHOD_TO_DOUBLE: Method? = Method("toDouble", Types.DOUBLE, arrayOf<Type?>(Types.OBJECT))
    val METHOD_TO_NUMBER: Method? = Method("toNumber", Types.NUMBER, arrayOf<Type?>(Types.OBJECT))
    val METHOD_TO_FLOAT: Method? = Method("toFloat", Types.FLOAT, arrayOf<Type?>(Types.OBJECT))
    val METHOD_TO_INTEGER: Method? = Method("toInteger", Types.INTEGER, arrayOf<Type?>(Types.OBJECT))

    // double toDouble Value(Object)
    val METHOD_TO_DOUBLE_VALUE: Method? = Method("toDoubleValue", Types.DOUBLE_VALUE, arrayOf<Type?>(Types.OBJECT))
    val METHOD_TO_FLOAT_VALUE: Method? = Method("toFloatValue", Types.FLOAT_VALUE, arrayOf<Type?>(Types.OBJECT))
    val METHOD_TO_INT_VALUE: Method? = Method("toIntValue", Types.FLOAT_VALUE, arrayOf<Type?>(Types.OBJECT))
    val METHOD_TO_INTEGER_FROM_INT: Method? = Method("toInteger", Types.INTEGER, arrayOf<Type?>(Types.INT_VALUE))
    val METHOD_TO_LONG_FROM_LONG_VALUE: Method? = Method("toLong", Types.LONG, arrayOf<Type?>(Types.LONG_VALUE))

    // Double toDouble (double)
    val METHOD_TO_DOUBLE_FROM_DOUBLE_VALUE: Method? = Method("toDouble", Types.DOUBLE, arrayOf<Type?>(Types.DOUBLE_VALUE))
    val METHOD_TO_DOUBLE_FROM_FLOAT_VALUE: Method? = Method("toDouble", Types.DOUBLE, arrayOf<Type?>(Types.FLOAT_VALUE))
    val METHOD_TO_FLOAT_FROM_DOUBLE: Method? = Method("toFloat", Types.FLOAT, arrayOf<Type?>(Types.DOUBLE_VALUE))
    val METHOD_TO_FLOAT_FROM_NUMBER: Method? = Method("toFloat", Types.FLOAT, arrayOf<Type?>(Types.NUMBER))
    val METHOD_TO_FLOAT_FROM_FLOAT: Method? = Method("toFloat", Types.FLOAT, arrayOf<Type?>(Types.FLOAT_VALUE))

    // double toDoubleValue (double)
    // final public static Method METHOD_TO_DOUBLE_VALUE_FROM_DOUBLE = new
    // Method("toDoubleValue",Types.DOUBLE_VALUE,new Type[]{Types.DOUBLE_VALUE});
    val METHOD_TO_FLOAT_VALUE_FROM_DOUBLE: Method? = Method("toFloatValue", Types.FLOAT_VALUE, arrayOf<Type?>(Types.DOUBLE_VALUE))
    val METHOD_TO_FLOAT_VALUE_FROM_NUMBER: Method? = Method("toFloatValue", Types.FLOAT_VALUE, arrayOf<Type?>(Types.NUMBER))
    val METHOD_TO_INT_VALUE_FROM_DOUBLE_VALUE: Method? = Method("toIntValue", Types.INT_VALUE, arrayOf<Type?>(Types.DOUBLE_VALUE))
    val METHOD_TO_INTEGER_FROM_DOUBLE_VALUE: Method? = Method("toInteger", Types.INTEGER, arrayOf<Type?>(Types.DOUBLE_VALUE))
    val METHOD_TO_NUMBER_FROM_BOOLEAN_VALUE: Method? = Method("toNumber", Types.NUMBER, arrayOf<Type?>(Types.BOOLEAN_VALUE))
    val METHOD_TO_NUMBER_FROM_DOUBLE_VALUE: Method? = Method("toNumber", Types.NUMBER, arrayOf<Type?>(Types.DOUBLE_VALUE))

    // Double toDouble (boolean)
    val METHOD_TO_DOUBLE_FROM_BOOLEAN_VALUE: Method? = Method("toDouble", Types.DOUBLE, arrayOf<Type?>(Types.BOOLEAN_VALUE))
    val METHOD_TO_FLOAT_FROM_BOOLEAN_VALUE: Method? = Method("toFloat", Types.FLOAT, arrayOf<Type?>(Types.BOOLEAN_VALUE))

    // double toDoubleValue (boolean)
    val METHOD_TO_DOUBLE_VALUE_FROM_BOOLEAN_VALUE: Method? = Method("toDoubleValue", Types.DOUBLE_VALUE, arrayOf<Type?>(Types.BOOLEAN_VALUE))
    val METHOD_TO_FLOAT_VALUE_FROM_BOOLEAN_VALUE: Method? = Method("toFloatValue", Types.FLOAT_VALUE, arrayOf<Type?>(Types.BOOLEAN_VALUE))
    val METHOD_TO_INT_VALUE_FROM_BOOLEAN_VALUE: Method? = Method("toIntValue", Types.INT_VALUE, arrayOf<Type?>(Types.BOOLEAN_VALUE))
    val METHOD_TO_INTEGER_FROM_BOOLEAN_VALUE: Method? = Method("toInteger", Types.INTEGER, arrayOf<Type?>(Types.BOOLEAN_VALUE))
    val METHOD_TO_DOUBLE_VALUE_FROM_DOUBLE: Method? = Method("toDoubleValue", Types.DOUBLE_VALUE, arrayOf<Type?>(Types.DOUBLE))
    val METHOD_TO_DOUBLE_VALUE_FROM_NUMBER: Method? = Method("toDoubleValue", Types.DOUBLE_VALUE, arrayOf<Type?>(Types.NUMBER))
    val METHOD_TO_DOUBLE_FROM_NUMBER: Method? = Method("toDouble", Types.DOUBLE, arrayOf<Type?>(Types.NUMBER))

    // Double toDouble (String)
    val METHOD_TO_DOUBLE_FROM_STRING: Method? = Method("toDouble", Types.DOUBLE, arrayOf<Type?>(Types.STRING))
    val METHOD_TO_BIG_DECIMAL_FROM_STRING: Method? = Method("toBigDecimal", Types.BIG_DECIMAL, arrayOf<Type?>(Types.STRING))
    val METHOD_TO_NUMBER_FROM_PC_STRING: Method? = Method("toNumber", Types.NUMBER, arrayOf<Type?>(Types.PAGE_CONTEXT, Types.STRING))
    val METHOD_TO_FLOAT_FROM_STRING: Method? = Method("toFloat", Types.FLOAT, arrayOf<Type?>(Types.STRING))
    val METHOD_TO_INTEGER_FROM_STRING: Method? = Method("toInteger", Types.INTEGER, arrayOf<Type?>(Types.STRING))

    // double toDoubleValue (String)
    val METHOD_TO_DOUBLE_VALUE_FROM_PC_STRING: Method? = Method("toDoubleValue", Types.DOUBLE_VALUE, arrayOf<Type?>(Types.PAGE_CONTEXT, Types.STRING))
    val METHOD_TO_DOUBLE_VALUE_FROM_STRING: Method? = Method("toDoubleValue", Types.DOUBLE_VALUE, arrayOf<Type?>(Types.STRING))
    val METHOD_TO_FLOAT_VALUE_FROM_STRING: Method? = Method("toFloatValue", Types.FLOAT_VALUE, arrayOf<Type?>(Types.STRING))
    val METHOD_TO_INT_VALUE_FROM_STRING: Method? = Method("toIntValue", Types.INT_VALUE, arrayOf<Type?>(Types.STRING))
    val METHOD_TO_BIG_DECIMAL_STR: Method? = Method("toBigDecimal", Types.BIG_DECIMAL, arrayOf<Type?>(Types.STRING))
    val METHOD_TO_BIG_DECIMAL_OBJ: Method? = Method("toBigDecimal", Types.BIG_DECIMAL, arrayOf<Type?>(Types.OBJECT))
    val METHOD_NEGATE_NUMBER: Method? = Method("negate", Types.NUMBER, arrayOf<Type?>(Types.NUMBER))
}