package lucee.transformer.interpreter.cast

import java.io.File

/**
 * cast an Expression to a Double
 */
class CastOther private constructor(expr: Expression?, type: String?, lcType: String?) : ExpressionBase(expr.getFactory(), expr.getStart(), expr.getEnd()), Cast {
    // TODO support short type
    private val expr: ExpressionBase?
    private val type: String?
    private var lcType: String?
    @Override
    @Throws(PageException::class)
    fun _writeOut(ic: InterpreterContext?, mode: Int): Class<*>? {
        if ("java.lang.boolean".equals(lcType)) lcType = "boolean" else if ("java.lang.double".equals(lcType)) lcType = "double" else if ("java.lang.string".equals(lcType)) lcType = "string" else if ("java.lang.byte".equals(lcType)) lcType = "byte" else if ("java.lang.character".equals(lcType)) lcType = "character" else if ("java.lang.short".equals(lcType)) lcType = "short" else if ("java.lang.integer".equals(lcType)) lcType = "integer" else if ("java.lang.long".equals(lcType)) lcType = "long" else if ("java.lang.float".equals(lcType)) lcType = "float" else if ("java.io.file".equals(lcType)) lcType = "file" else if ("java.lang.object".equals(lcType)) lcType = "object" else if ("java.util.date".equals(lcType)) lcType = "date"
        val first: Char = lcType.charAt(0)
        var `val`: Object
        when (first) {
            'a' -> if ("array".equals(lcType)) {
                `val` = ic.getValue(expr)
                if (`val` !is Array) `val` = Caster.toArray(`val`)
                ic.stack(`val`)
                return Array::class.java
            }
            'b' -> {
                if ("base64".equals(lcType)) {
                    ic.stack(Caster.toBase64(ic.getValue(expr)))
                    return String::class.java
                }
                if ("binary".equals(lcType) || "byte".equals(lcType)) {
                    if (mode == MODE_VALUE) {
                        ic.stack(ic.getValueAsByteValue(expr))
                        return Byte::class.javaPrimitiveType
                    }
                    ic.stack(ic.getValueAsByte(expr))
                    return Byte::class.java
                }
                if ("boolean".equals(lcType)) {
                    if (mode == MODE_VALUE) {
                        ic.stack(ic.getValueAsBooleanValue(expr))
                        return Boolean::class.javaPrimitiveType
                    }
                    ic.stack(ic.getValueAsBoolean(expr))
                    return Boolean::class.java
                }
            }
            'c' -> {
                if ("char".equals(lcType) || "character".equals(lcType)) {
                    if (mode == MODE_VALUE) {
                        ic.stack(ic.getValueAsCharValue(expr))
                        return Char::class.javaPrimitiveType
                    }
                    ic.stack(ic.getValueAsCharacter(expr))
                    return Character::class.java
                }
                if ("collection".equals(lcType)) {
                    `val` = ic.getValue(expr)
                    if (`val` !is Collection) `val` = Caster.toCollection(`val`)
                    ic.stack(`val`)
                    return Collection::class.java
                }
                if ("component".equals(lcType) || "class".equals(lcType)) {
                    `val` = ic.getValue(expr)
                    if (`val` !is Component) `val` = Caster.toComponent(`val`)
                    ic.stack(`val`)
                    return Component::class.java
                }
            }
            'd' -> {
                if ("double".equals(lcType)) {
                    if (mode == MODE_VALUE) {
                        ic.stack(ic.getValueAsDoubleValue(expr))
                        return Double::class.javaPrimitiveType
                    }
                    ic.stack(ic.getValueAsDouble(expr))
                    return Double::class.java
                }
                if ("date".equals(lcType) || "datetime".equals(lcType)) {
                    `val` = ic.getValue(expr)
                    if (`val` !is DateTime) `val` = Caster.toDate(`val`, ThreadLocalPageContext.getTimeZone(ic.getPageContext()))
                    ic.stack(`val`)
                    return DateTime::class.java
                }
                if ("decimal".equals(lcType)) {
                    ic.stack(Caster.toDecimal(ic.getValue(expr), true))
                    return String::class.java
                }
            }
            'f' -> {
                if ("file".equals(lcType)) {
                    `val` = ic.getValue(expr)
                    if (`val` !is File) `val` = Caster.toFile(`val`)
                    ic.stack(`val`)
                    return File::class.java
                }
                if ("float".equals(lcType)) {
                    if (mode == MODE_VALUE) {
                        ic.stack(ic.getValueAsFloatValue(expr))
                        return Float::class.javaPrimitiveType
                    }
                    ic.stack(ic.getValueAsFloat(expr))
                    return Float::class.java
                }
            }
            'i' -> if ("int".equals(lcType) || "integer".equals(lcType)) {
                if (mode == MODE_VALUE) {
                    ic.stack(ic.getValueAsIntValue(expr))
                    return Int::class.javaPrimitiveType
                }
                ic.stack(ic.getValueAsInteger(expr))
                return Integer::class.java
            }
            'j' -> if ("java.lang.stringbuffer".equals(lcType)) {
                `val` = ic.getValue(expr)
                if (`val` !is StringBuffer) `val` = Caster.toStringBuffer(`val`)
                ic.stack(`val`)
                return StringBuffer::class.java
            }
            'l' -> if ("long".equals(lcType)) {
                if (mode == MODE_VALUE) {
                    ic.stack(ic.getValueAsLongValue(expr))
                    return Long::class.javaPrimitiveType
                }
                ic.stack(ic.getValueAsLong(expr))
                return Long::class.java
            } else if ("locale".equals(lcType)) {
                ic.stack(Caster.toLocale(ic.getValue(expr)))
                return Locale::class.java
            }
            'n' -> if ("node".equals(lcType)) {
                `val` = ic.getValue(expr)
                if (`val` !is Node) `val` = Caster.toNode(`val`)
                ic.stack(`val`)
                return Node::class.java
            } else if ("null".equals(lcType)) {
                ic.stack(Caster.toNull(ic.getValue(expr)))
                // TODO gibt es einen null typ?
                return Object::class.java
            }
            'o' -> if ("object".equals(lcType) || "other".equals(lcType)) {
                ic.stack(ic.getValue(expr))
                return Object::class.java
            }
            't' -> if ("timezone".equals(lcType)) {
                ic.stack(Caster.toTimeZone(ic.getValue(expr)))
                return TimeZone::class.java
            } else if ("timespan".equals(lcType)) {
                ic.stack(Caster.toTimespan(ic.getValue(expr)))
                return TimeSpan::class.java
            }
            's' -> {
                if ("struct".equals(lcType)) {
                    `val` = ic.getValue(expr)
                    if (`val` !is Struct) `val` = Caster.toStruct(`val`)
                    ic.stack(`val`)
                    return Struct::class.java
                }
                if ("short".equals(lcType)) {
                    if (mode == MODE_VALUE) {
                        ic.stack(ic.getValueAsShortValue(expr))
                        return Short::class.javaPrimitiveType
                    }
                    ic.stack(ic.getValueAsShort(expr))
                    return Short::class.java
                }
                if ("stringbuffer".equals(lcType)) {
                    `val` = ic.getValue(expr)
                    if (`val` !is StringBuffer) `val` = Caster.toStringBuffer(`val`)
                    ic.stack(`val`)
                    return StringBuffer::class.java
                }
            }
            'x' -> if ("xml".equals(lcType)) {
                `val` = ic.getValue(expr)
                if (`val` !is Node) `val` = Caster.toNode(`val`)
                ic.stack(`val`)
                return Node::class.java
            }
            else -> if ("query".equals(lcType)) {
                `val` = ic.getValue(expr)
                if (`val` !is Query) `val` = Caster.toQuery(`val`)
                ic.stack(`val`)
                return Query::class.java
            }
        }
        ic.stack(Caster.castTo(ic.getPageContext(), type, ic.getValue(expr), false))
        return Object::class.java
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
                'd' -> if ("double".equals(type)) {
                    return expr.getFactory().toExprNumber(expr)
                }
                'f' -> if ("float".equals(type)) return expr.getFactory().toExprNumber(expr)
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
                'v' ->            /*
			 * TODO if("variablename".equals(lcType)) return VariableString.toExprString(expr);
			 * if("variable_name".equals(lcType)) return VariableString.toExprString(expr);
			 * if("variablestring".equals(lcType)) return VariableString.toExprString(expr);
			 * if("variable_string".equals(lcType)) return VariableString.toExprString(expr);
			 */if ("void".equals(lcType)) return expr
            }
            return CastOther(expr, type, lcType)
        }
    }

    init {
        this.expr = expr as ExpressionBase?
        this.type = type
        this.lcType = lcType
    }
}