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

object Methods_Operator {
    val OPERATOR_EQV_PC_B_B: Method? = Method("eqv", Types.BOOLEAN_VALUE, arrayOf<Type?>(Types.PAGE_CONTEXT, Types.BOOLEAN, Types.BOOLEAN))
    val OPERATOR_IMP_PC_B_B: Method? = Method("imp", Types.BOOLEAN_VALUE, arrayOf<Type?>(Types.PAGE_CONTEXT, Types.BOOLEAN, Types.BOOLEAN))
    val OPERATOR_CT_PC_O_O: Method? = Method("ct", Types.BOOLEAN_VALUE, arrayOf<Type?>(Types.PAGE_CONTEXT, Types.OBJECT, Types.OBJECT))
    val OPERATOR_EEQ_PC_O_O: Method? = Method("eeq", Types.BOOLEAN_VALUE, arrayOf<Type?>(Types.PAGE_CONTEXT, Types.OBJECT, Types.OBJECT))
    val OPERATOR_NEEQ_PC_O_O: Method? = Method("neeq", Types.BOOLEAN_VALUE, arrayOf<Type?>(Types.PAGE_CONTEXT, Types.OBJECT, Types.OBJECT))
    val OPERATOR_NCT_PC_O_O: Method? = Method("nct", Types.BOOLEAN_VALUE, arrayOf<Type?>(Types.PAGE_CONTEXT, Types.OBJECT, Types.OBJECT))
    val COMPARATORS: Array<Array<Method?>?>? = arrayOf(arrayOf<Method?>(Method("compare", Types.INT_VALUE, arrayOf<Type?>(Types.PAGE_CONTEXT, Types.OBJECT, Types.OBJECT)),
            Method("compare", Types.INT_VALUE, arrayOf<Type?>(Types.PAGE_CONTEXT, Types.OBJECT, Types.BOOLEAN)),
            Method("compare", Types.INT_VALUE, arrayOf<Type?>(Types.PAGE_CONTEXT, Types.OBJECT, Types.NUMBER)),
            Method("compare", Types.INT_VALUE, arrayOf<Type?>(Types.PAGE_CONTEXT, Types.OBJECT, Types.STRING))), arrayOf<Method?>(Method("compare", Types.INT_VALUE, arrayOf<Type?>(Types.PAGE_CONTEXT, Types.BOOLEAN, Types.OBJECT)),
            Method("compare", Types.INT_VALUE, arrayOf<Type?>(Types.PAGE_CONTEXT, Types.BOOLEAN, Types.BOOLEAN)),
            Method("compare", Types.INT_VALUE, arrayOf<Type?>(Types.PAGE_CONTEXT, Types.BOOLEAN, Types.NUMBER)),
            Method("compare", Types.INT_VALUE, arrayOf<Type?>(Types.PAGE_CONTEXT, Types.BOOLEAN, Types.STRING))), arrayOf<Method?>(Method("compare", Types.INT_VALUE, arrayOf<Type?>(Types.PAGE_CONTEXT, Types.NUMBER, Types.OBJECT)),
            Method("compare", Types.INT_VALUE, arrayOf<Type?>(Types.PAGE_CONTEXT, Types.NUMBER, Types.BOOLEAN)),
            Method("compare", Types.INT_VALUE, arrayOf<Type?>(Types.PAGE_CONTEXT, Types.NUMBER, Types.NUMBER)),
            Method("compare", Types.INT_VALUE, arrayOf<Type?>(Types.PAGE_CONTEXT, Types.NUMBER, Types.STRING))), arrayOf<Method?>(Method("compare", Types.INT_VALUE, arrayOf<Type?>(Types.PAGE_CONTEXT, Types.STRING, Types.OBJECT)),
            Method("compare", Types.INT_VALUE, arrayOf<Type?>(Types.PAGE_CONTEXT, Types.STRING, Types.BOOLEAN)),
            Method("compare", Types.INT_VALUE, arrayOf<Type?>(Types.PAGE_CONTEXT, Types.STRING, Types.NUMBER)),
            Method("compare", Types.INT_VALUE, arrayOf<Type?>(Types.PAGE_CONTEXT, Types.STRING, Types.STRING))))

    fun getType(type: Type?): Int {
        val className: String = type.getClassName()
        if (Types.BOOLEAN.equals(type)) return Types._BOOLEAN
        if (Types.DOUBLE.equals(type)) return Types._NUMBER
        if (Types.NUMBER.equals(type)) return Types._NUMBER
        if (Types.STRING.equals(type)) return Types._STRING
        if (Types.BYTE.equals(type)) return Types._NUMBER
        if (Types.SHORT.equals(type)) return Types._NUMBER
        if (Types.FLOAT.equals(type)) return Types._NUMBER
        if (Types.LONG.equals(type)) return Types._NUMBER
        return if (Types.INTEGER.equals(type)) Types._NUMBER else Types._OBJECT
    }
}