/**
 * Copyright (c) 2014, the Railo Company Ltd.
 * Copyright (c) 2015, Lucee Assosication Switzerland
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
package lucee.transformer.bytecode.cast

import org.objectweb.asm.Type

/**
 * cast an Expression to a Double
 */
class CastOther private constructor(expr: Expression?, type: String?, lcType: String?) : ExpressionBase(expr.getFactory(), expr.getStart(), expr.getEnd()), Cast {
    // TODO support short type
    private val expr: ExpressionBase?
    private val type: String?
    private val lcType: String?

    // Excel toExcel (Object)
    /*
	 * final public static Method TO_EXCEL = new Method("toExcel", Types.EXCEL, new
	 * Type[]{Types.OBJECT});
	 */
    @Override
    @Throws(TransformerException::class)
    fun _writeOut(bc: BytecodeContext?, mode: Int): Type? {
        // Caster.toDecimal(null);
        val adapter: GeneratorAdapter = bc.getAdapter()
        val first: Char = lcType.charAt(0)
        val rtn: Type
        when (first) {
            'a' -> if ("array".equals(lcType)) {
                rtn = expr.writeOutAsType(bc, MODE_REF)
                if (!rtn.equals(Types.ARRAY)) adapter.invokeStatic(Types.CASTER, Methods_Caster.TO_ARRAY)
                return Types.ARRAY
            }
            'b' -> {
                if ("base64".equals(lcType)) {
                    expr.writeOut(bc, MODE_REF)
                    adapter.invokeStatic(Types.CASTER, Methods_Caster.TO_BASE64)
                    return Types.STRING
                }
                if ("binary".equals(lcType)) {
                    rtn = expr.writeOutAsType(bc, MODE_REF)
                    if (!rtn.equals(Types.BYTE_VALUE_ARRAY)) adapter.invokeStatic(Types.CASTER, Methods_Caster.TO_BINARY)
                    return Types.BYTE_VALUE_ARRAY
                }
                if ("byte".equals(type)) {
                    rtn = expr.writeOutAsType(bc, MODE_REF)
                    if (!rtn.equals(Types.BYTE_VALUE)) adapter.invokeStatic(Types.CASTER, Methods_Caster.TO_BYTE_VALUE.get(Methods_Operator.getType(rtn)))
                    return Types.BYTE_VALUE
                }
                if ("byte".equals(lcType)) {
                    rtn = expr.writeOutAsType(bc, MODE_REF)
                    if (!rtn.equals(Types.BYTE)) adapter.invokeStatic(Types.CASTER, Methods_Caster.TO_BYTE.get(Methods_Operator.getType(rtn)))
                    return Types.BYTE
                }
                if ("boolean".equals(lcType)) {
                    return (bc.getFactory().toExprBoolean(expr) as ExpressionBase).writeOutAsType(bc, MODE_REF)
                }
            }
            'c' -> {
                if ("char".equals(lcType)) {
                    rtn = expr.writeOutAsType(bc, MODE_REF)
                    if (!rtn.equals(Types.CHAR)) adapter.invokeStatic(Types.CASTER, Methods_Caster.TO_CHAR_VALUE.get(Methods_Operator.getType(rtn)))
                    return Types.CHAR
                }
                if ("character".equals(lcType)) {
                    rtn = expr.writeOutAsType(bc, MODE_REF)
                    if (!rtn.equals(Types.CHARACTER)) adapter.invokeStatic(Types.CASTER, Methods_Caster.TO_CHARACTER.get(Methods_Operator.getType(rtn)))
                    return Types.CHARACTER
                }
                if ("collection".equals(lcType)) {
                    rtn = expr.writeOutAsType(bc, MODE_REF)
                    if (!rtn.equals(Types.COLLECTION)) adapter.invokeStatic(Types.CASTER, Methods_Caster.TO_COLLECTION)
                    return Types.COLLECTION
                }
                if ("component".equals(lcType) || "class".equals(lcType)) {
                    rtn = expr.writeOutAsType(bc, MODE_REF)
                    if (!rtn.equals(Types.COMPONENT)) adapter.invokeStatic(Types.CASTER, Methods_Caster.TO_COMPONENT)
                    return Types.COMPONENT
                }
            }
            'd' -> {
                if ("double".equals(lcType)) {
                    return (bc.getFactory().toExprNumber(expr) as ExpressionBase).writeOutAsType(bc, MODE_REF)
                }
                if ("date".equals(lcType) || "datetime".equals(lcType)) {
                    // First Arg
                    rtn = expr.writeOutAsType(bc, MODE_REF)
                    if (rtn.equals(Types.DATE_TIME)) return Types.DATE_TIME
                    val type: Int = Methods_Operator.getType(rtn)

                    // Second Arg
                    adapter.loadArg(0)
                    // adapter.invokeVirtual(Types.PAGE_CONTEXT,GET_CONFIG);
                    // adapter.invokeInterface(Types.CONFIG_WEB,GET_TIMEZONE);
                    adapter.invokeVirtual(Types.PAGE_CONTEXT, GET_TIMEZONE)
                    adapter.invokeStatic(Types.CASTER, Methods_Caster.TO_DATE.get(type))
                    return Types.DATE_TIME
                }
                if ("decimal".equals(lcType)) {
                    rtn = expr.writeOutAsType(bc, MODE_REF)
                    adapter.invokeStatic(Types.CASTER, Methods_Caster.TO_DECIMAL.get(Methods_Operator.getType(rtn)))
                    return Types.STRING
                }
            }
            'e' -> {
            }
            'f' -> {
                if ("file".equals(lcType)) {
                    rtn = expr.writeOutAsType(bc, MODE_REF)
                    if (!rtn.equals(Types.FILE)) adapter.invokeStatic(Types.CASTER, Methods_Caster.TO_FILE)
                    return Types.FILE
                }
                if ("float".equals(type)) {
                    rtn = expr.writeOutAsType(bc, MODE_REF)
                    if (!rtn.equals(Types.FLOAT_VALUE)) adapter.invokeStatic(Types.CASTER, Methods_Caster.TO_FLOAT_VALUE.get(Methods_Operator.getType(rtn)))
                    return Types.FLOAT_VALUE
                }
                if ("float".equals(lcType)) {
                    rtn = expr.writeOutAsType(bc, MODE_REF)
                    if (!rtn.equals(Types.FLOAT)) adapter.invokeStatic(Types.CASTER, Methods_Caster.TO_FLOAT.get(Methods_Operator.getType(rtn)))
                    return Types.FLOAT
                }
            }
            'i' -> {
                if ("int".equals(lcType)) {
                    rtn = expr.writeOutAsType(bc, MODE_REF)
                    if (!rtn.equals(Types.INT_VALUE)) adapter.invokeStatic(Types.CASTER, Methods_Caster.TO_INT_VALUE.get(Methods_Operator.getType(rtn)))
                    return Types.INT_VALUE
                }
                if ("integer".equals(lcType)) {
                    rtn = expr.writeOutAsType(bc, MODE_REF)
                    if (!rtn.equals(Types.INTEGER)) adapter.invokeStatic(Types.CASTER, Methods_Caster.TO_INTEGER.get(Methods_Operator.getType(rtn)))
                    return Types.INTEGER
                }
            }
            'j' -> {
                if ("java.lang.boolean".equals(lcType)) {
                    return (bc.getFactory().toExprBoolean(expr) as ExpressionBase).writeOutAsType(bc, MODE_REF)
                }
                if ("java.lang.double".equals(lcType)) {
                    return (bc.getFactory().toExprNumber(expr) as ExpressionBase).writeOutAsType(bc, MODE_REF)
                }
                if ("java.lang.string".equals(lcType)) {
                    return (bc.getFactory().toExprString(expr) as ExpressionBase).writeOutAsType(bc, MODE_REF)
                }
                if ("java.lang.stringbuffer".equals(lcType)) {
                    rtn = expr.writeOutAsType(bc, MODE_REF)
                    if (!rtn.equals(Types.STRING_BUFFER)) adapter.invokeStatic(Types.CASTER, Methods_Caster.TO_STRING_BUFFER)
                    return Types.STRING_BUFFER
                }
                if ("java.lang.byte".equals(lcType)) {
                    rtn = expr.writeOutAsType(bc, MODE_REF)
                    if (!rtn.equals(Types.BYTE)) adapter.invokeStatic(Types.CASTER, Methods_Caster.TO_BYTE.get(Methods_Operator.getType(rtn)))
                    return Types.BYTE
                }
                if ("java.lang.character".equals(lcType)) {
                    rtn = expr.writeOutAsType(bc, MODE_REF)
                    if (!rtn.equals(Types.CHARACTER)) adapter.invokeStatic(Types.CASTER, Methods_Caster.TO_CHARACTER.get(Methods_Operator.getType(rtn)))
                    return Types.CHARACTER
                }
                if ("java.lang.short".equals(lcType)) {
                    rtn = expr.writeOutAsType(bc, MODE_REF)
                    if (!rtn.equals(Types.SHORT)) adapter.invokeStatic(Types.CASTER, Methods_Caster.TO_SHORT.get(Methods_Operator.getType(rtn)))
                    return Types.SHORT
                }
                if ("java.lang.integer".equals(lcType)) {
                    rtn = expr.writeOutAsType(bc, MODE_REF)
                    if (!rtn.equals(Types.INTEGER)) adapter.invokeStatic(Types.CASTER, Methods_Caster.TO_INTEGER.get(Methods_Operator.getType(rtn)))
                    return Types.INTEGER
                }
                if ("java.lang.long".equals(lcType)) {
                    rtn = expr.writeOutAsType(bc, MODE_REF)
                    if (!rtn.equals(Types.LONG)) adapter.invokeStatic(Types.CASTER, Methods_Caster.TO_LONG.get(Methods_Operator.getType(rtn)))
                    return Types.LONG
                }
                if ("java.lang.float".equals(lcType)) {
                    rtn = expr.writeOutAsType(bc, MODE_REF)
                    if (!rtn.equals(Types.FLOAT)) adapter.invokeStatic(Types.CASTER, Methods_Caster.TO_FLOAT.get(Methods_Operator.getType(rtn)))
                    return Types.FLOAT
                }
                if ("java.io.file".equals(lcType)) {
                    rtn = expr.writeOutAsType(bc, MODE_REF)
                    if (!rtn.equals(Types.FILE)) adapter.invokeStatic(Types.CASTER, Methods_Caster.TO_FILE)
                    return Types.FILE
                }
                if ("java.lang.object".equals(lcType)) {
                    return expr.writeOutAsType(bc, MODE_REF)
                } else if ("java.util.date".equals(lcType)) {
                    // First Arg
                    rtn = expr.writeOutAsType(bc, MODE_REF)
                    if (rtn.equals(Types.DATE)) return Types.DATE
                    if (rtn.equals(Types.DATE_TIME)) return Types.DATE_TIME

                    // Second Arg
                    adapter.loadArg(0)
                    // adapter.invokeVirtual(Types.PAGE_CONTEXT,GET_CONFIG);
                    // adapter.invokeVirtual(Types.CONFIG_WEB,GET_TIMEZONE);
                    adapter.invokeVirtual(Types.PAGE_CONTEXT, GET_TIMEZONE)
                    adapter.invokeStatic(Types.CASTER, Methods_Caster.TO_DATE.get(Methods_Operator.getType(rtn)))
                    return Types.DATE_TIME
                }
            }
            'l' -> if ("long".equals(type)) {
                rtn = expr.writeOutAsType(bc, MODE_REF)
                if (!rtn.equals(Types.LONG_VALUE)) adapter.invokeStatic(Types.CASTER, Methods_Caster.TO_LONG_VALUE.get(Methods_Operator.getType(rtn)))
                return Types.LONG_VALUE
            } else if ("long".equals(lcType)) {
                rtn = expr.writeOutAsType(bc, MODE_REF)
                if (!rtn.equals(Types.LONG)) adapter.invokeStatic(Types.CASTER, Methods_Caster.TO_LONG.get(Methods_Operator.getType(rtn)))
                return Types.LONG
            } else if ("locale".equals(lcType)) {
                rtn = expr.writeOutAsType(bc, MODE_REF)
                adapter.invokeStatic(Types.CASTER, Methods_Caster.TO_LOCALE)
                return Types.LOCALE
            }
            'n' -> if ("node".equals(lcType)) {
                rtn = expr.writeOutAsType(bc, MODE_REF)
                if (!rtn.equals(Types.NODE)) adapter.invokeStatic(Types.CASTER, Methods_Caster.TO_NODE)
                return Types.NODE
            } else if ("null".equals(lcType)) {
                expr.writeOut(bc, MODE_REF)
                adapter.invokeStatic(Types.CASTER, Methods_Caster.TO_NULL)
                // TODO gibt es einen null typ?
                return Types.OBJECT
            }
            'o' -> if ("object".equals(lcType) || "other".equals(lcType)) {
                expr.writeOut(bc, MODE_REF)
                return Types.OBJECT
            }
            't' -> if ("timezone".equals(lcType)) {
                rtn = expr.writeOutAsType(bc, MODE_REF)
                adapter.invokeStatic(Types.CASTER, Methods_Caster.TO_TIMEZONE)
                return Types.TIMEZONE
            } else if ("timespan".equals(lcType)) {
                rtn = expr.writeOutAsType(bc, MODE_REF)
                if (!rtn.equals(Types.TIMESPAN)) adapter.invokeStatic(Types.CASTER, Methods_Caster.TO_TIMESPAN)
                return Types.TIMESPAN
            }
            's' -> {
                if ("struct".equals(lcType)) {
                    rtn = expr.writeOutAsType(bc, MODE_REF)
                    if (!rtn.equals(Types.STRUCT)) adapter.invokeStatic(Types.CASTER, Methods_Caster.TO_STRUCT)
                    return Types.STRUCT
                }
                if ("short".equals(type)) {
                    rtn = expr.writeOutAsType(bc, MODE_REF)
                    if (!rtn.equals(Types.SHORT_VALUE)) adapter.invokeStatic(Types.CASTER, Methods_Caster.TO_SHORT_VALUE.get(Methods_Operator.getType(rtn)))
                    return Types.SHORT_VALUE
                }
                if ("short".equals(lcType)) {
                    rtn = expr.writeOutAsType(bc, MODE_REF)
                    if (!rtn.equals(Types.SHORT)) adapter.invokeStatic(Types.CASTER, Methods_Caster.TO_SHORT.get(Methods_Operator.getType(rtn)))
                    return Types.SHORT
                }
                if ("stringbuffer".equals(lcType)) {
                    rtn = expr.writeOutAsType(bc, MODE_REF)
                    if (!rtn.equals(Types.STRING_BUFFER)) adapter.invokeStatic(Types.CASTER, Methods_Caster.TO_STRING_BUFFER)
                    return Types.STRING_BUFFER
                }
            }
            'x' -> if ("xml".equals(lcType)) {
                rtn = expr.writeOutAsType(bc, MODE_REF)
                if (!rtn.equals(Types.NODE)) adapter.invokeStatic(Types.CASTER, Methods_Caster.TO_NODE)
                return Types.NODE
            }
            else -> {
                if ("query".equals(lcType)) {
                    rtn = expr.writeOutAsType(bc, MODE_REF)
                    if (!rtn.equals(Types.QUERY)) adapter.invokeStatic(Types.CASTER, Methods_Caster.TO_QUERY)
                    return Types.QUERY
                }
                if ("querycolumn".equals(lcType)) {
                    rtn = if (expr is Variable) (expr as VariableImpl?).writeOutCollectionAsType(bc, mode) else expr.writeOutAsType(bc, MODE_REF)
                    if (!rtn.equals(Types.QUERY_COLUMN)) {
                        adapter.loadArg(0)
                        adapter.invokeStatic(Types.CASTER, Methods_Caster.TO_QUERY_COLUMN)
                    }
                    return Types.QUERY_COLUMN
                }
            }
        }
        val t: Type? = getType(bc, type)
        expr.writeOut(bc, MODE_REF)
        adapter.checkCast(t)
        return t
    }

    @Override
    fun getExpr(): Expression? {
        return expr
    }

    /**
     * @return the type
     */
    fun getType(): String? {
        return type
    }

    companion object {
        fun toExpression(expr: Expression?, type: String?): Expression? {
            if (type == null) return expr
            val lcType: String = StringUtil.toLowerCase(type)
            when (lcType.charAt(0)) {
                'a' -> if ("any".equals(lcType)) {
                    return expr
                }
                'b' -> if ("boolean".equals(type) || "bool".equals(lcType)) return expr.getFactory().toExprBoolean(expr)
                'd' -> if ("double".equals(type)) return expr.getFactory().toExprNumber(expr)
                'i' -> {
                    if ("int".equals(lcType)) return expr.getFactory().toExprInt(expr)
                    if ("number".equals(lcType) || "numeric".equals(lcType)) {
                        return expr.getFactory().toExprNumber(expr)
                    }
                }
                'n' -> if ("number".equals(lcType) || "numeric".equals(lcType)) {
                    return expr.getFactory().toExprNumber(expr)
                }
                'o' -> if ("object".equals(lcType)) {
                    return expr
                }
                's' -> if ("string".equals(lcType)) return expr.getFactory().toExprString(expr)
                'u' -> if ("uuid".equals(lcType)) return expr.getFactory().toExprString(expr)
                'v' -> {
                    if ("variablename".equals(lcType)) return VariableString.toExprString(expr)
                    if ("variable_name".equals(lcType)) return VariableString.toExprString(expr)
                    if ("variablestring".equals(lcType)) return VariableString.toExprString(expr)
                    if ("variable_string".equals(lcType)) return VariableString.toExprString(expr)
                    if ("void".equals(lcType)) return expr
                }
            }
            return CastOther(expr, type, lcType)
        }

        // Array toArray(Object)
        val TO_ARRAY: Method? = Method("toArray", Types.ARRAY, arrayOf<Type?>(Types.OBJECT))

        // String toBase64 (Object);
        val TO_BASE64: Method? = Method("toBase64", Types.STRING, arrayOf<Type?>(Types.OBJECT))

        // byte[] toBinary (Object)
        val TO_BINARY: Method? = Method("toBinary", Types.BYTE_VALUE_ARRAY, arrayOf<Type?>(Types.OBJECT))

        // byte[] toCollection (Object)
        val TO_COLLECTION: Method? = Method("toCollection", Types.BYTE_VALUE_ARRAY, arrayOf<Type?>(Types.OBJECT))

        // lucee.runtime.Component toComponent (Object)
        val TO_COMPONENT: Method? = Method("toComponent", Types.COMPONENT, arrayOf<Type?>(Types.OBJECT))

        // String toDecimal (Object)
        val TO_DECIMAL: Method? = Method("toDecimal", Types.STRING, arrayOf<Type?>(Types.OBJECT))

        // lucee.runtime.config.Config getConfig ()
        val GET_CONFIG: Method? = Method("getConfig", Types.CONFIG_WEB, arrayOf<Type?>())

        // java.util.TimeZone getTimeZone ()
        val GET_TIMEZONE: Method? = Method("getTimeZone", Types.TIMEZONE, arrayOf<Type?>())
        @Throws(TransformerException::class)
        fun getType(bc: BytecodeContext?, type: String?): Type? {
            if (StringUtil.isEmpty(type)) return Types.OBJECT
            val lcType: String = StringUtil.toLowerCase(type)
            when (lcType.charAt(0)) {
                'a' -> {
                    if ("any".equals(lcType)) return Types.OBJECT
                    if ("array".equals(lcType)) return Types.ARRAY
                }
                'b' -> {
                    if ("bool".equals(lcType) || "boolean".equals(type)) return Types.BOOLEAN_VALUE
                    if ("boolean".equals(lcType)) return Types.BOOLEAN
                    if ("base64".equals(lcType)) return Types.STRING
                    if ("binary".equals(lcType)) return Types.BYTE_VALUE_ARRAY
                    if ("byte".equals(type)) return Types.BYTE_VALUE
                    if ("byte".equals(lcType)) return Types.BYTE
                }
                'c' -> {
                    if ("char".equals(lcType)) return Types.CHAR
                    if ("character".equals(lcType)) return Types.CHARACTER
                    if ("collection".equals(lcType)) return Types.COLLECTION
                    if ("component".equals(lcType)) return Types.COMPONENT
                    if ("class".equals(lcType)) return Types.COMPONENT
                }
                'd' -> {
                    if ("double".equals(type)) return Types.DOUBLE_VALUE
                    if ("double".equals(lcType)) return Types.DOUBLE
                    if ("date".equals(lcType) || "datetime".equals(lcType)) return Types.DATE_TIME
                    if ("decimal".equals(lcType)) return Types.STRING
                }
                'e' -> {
                }
                'f' -> {
                    if ("file".equals(lcType)) return Types.FILE
                    if ("float".equals(type)) return Types.FLOAT_VALUE
                    if ("float".equals(lcType)) return Types.FLOAT
                    if ("function".equals(lcType)) return Types.UDF
                }
                'i' -> {
                    // ext.img if("image".equals(lcType)) return ImageUtil.getImageType();
                    if ("int".equals(lcType)) return Types.INT_VALUE
                    if ("integer".equals(lcType)) return Types.INTEGER
                }
                'l' -> {
                    if ("long".equals(type)) return Types.LONG_VALUE
                    if ("long".equals(lcType)) return Types.LONG
                    if ("locale".equals(lcType)) return Types.LOCALE
                    if ("lucee.runtime.type.Collection\$Key".equals(type)) return Types.COLLECTION_KEY
                }
                'n' -> {
                    if ("node".equals(lcType)) return Types.NODE
                    if ("null".equals(lcType)) return Types.OBJECT
                    if ("number".equals(lcType)) return Types.DOUBLE_VALUE
                    if ("numeric".equals(lcType)) return Types.DOUBLE_VALUE
                }
                's' -> {
                    if ("string".equals(lcType)) return Types.STRING
                    if ("struct".equals(lcType)) return Types.STRUCT
                    if ("short".equals(type)) return Types.SHORT_VALUE
                    if ("short".equals(lcType)) return Types.SHORT
                }
                'o' -> {
                    if ("other".equals(lcType)) return Types.OBJECT
                    if ("object".equals(lcType)) return Types.OBJECT
                }
                'u' -> {
                    if ("uuid".equals(lcType)) return Types.STRING
                    if ("udf".equals(lcType)) return Types.UDF
                }
                'q' -> {
                    if ("query".equals(lcType)) return Types.QUERY
                    if ("querycolumn".equals(lcType)) return Types.QUERY_COLUMN
                }
                't' -> {
                    if ("timespan".equals(lcType)) return Types.TIMESPAN
                    if ("timezone".equals(lcType)) return Types.TIMEZONE
                }
                'v' -> {
                    if ("variablename".equals(lcType)) return Types.STRING
                    if ("variable_name".equals(lcType)) return Types.STRING
                    if ("variablestring".equals(lcType)) return Types.STRING
                    if ("variable_string".equals(lcType)) return Types.STRING
                    if ("void".equals(lcType)) return Types.VOID
                }
                'x' -> if ("xml".equals(lcType)) return Types.NODE
            }
            return try {
                Type.getType(ClassUtil.loadClass(type))
            } catch (e: ClassException) {
                throw TransformerException(bc, e.getMessage(), null)
            }
        }
    }

    init {
        this.expr = expr as ExpressionBase?
        this.type = type
        this.lcType = lcType
    }
}