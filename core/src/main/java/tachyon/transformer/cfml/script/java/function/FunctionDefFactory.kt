package tachyon.transformer.cfml.script.java.function

import java.util.ArrayList

object FunctionDefFactory {
    private val map: Map<Class<*>?, Array<JavaFunctionDef?>?>? = HashMap()
    @Throws(JavaSourceException::class, ClassException::class)
    fun getFunctionDef(argList: ArrayList<Argument?>?, strRtnType: String?): FunctionDef? {
        if (StringUtil.isEmpty(strRtnType)) {
            throw JavaSourceException("you need to define a return type for java type functions, possible values are [void,boolean,double,int,long,Object]")
        }
        // convert type definition to classes
        val rtnType: Class<*> = toClass(strRtnType)
        val argTypes: Array<Class<*>?> = arrayOfNulls<Class<*>?>(argList.size())
        val it: Iterator<Argument?> = argList.iterator()
        run {
            var i = 0
            while (it.hasNext()) {
                argTypes[i++] = toClass(it.next().getType())
            }
        }

        // Java Function
        val jcs: Array<JavaFunctionDef?>? = map!![rtnType]
        if (jcs != null) {
            var args: Array<Class<*>?>
            outer@ for (jc in jcs) {
                args = jc!!.getArgs()
                if (argList.size() !== args.size) continue@outer
                for (i in args.indices) {
                    if (!argTypes[i].equals(args[i])) continue@outer
                }
                return jc
            }

            // create exception
            /*
			 * StringBuilder sb = new StringBuilder(); for (JavaFunctionDef jc: jcs) { if (sb.length() > 0)
			 * sb.append(','); sb.append(jc.toStringShort()); } throw new JavaSourceException(
			 * "found no matching function interface with return type [" + strRtnType +
			 * "], for the arguments defined, valid argument combinations are [" + sb + "]");
			 */
        }
        return JavaFunctionDef(null, "invoke", argTypes, rtnType, true)
    }

    @Throws(JavaSourceException::class, ClassException::class)
    private fun toClass(type: ExprString?): Class<*>? {
        if (type is LitString) return toClass((type as LitString?).getString())
        throw JavaSourceException("type definition must be literal")
    }

    @Throws(ClassException::class)
    private fun toClass(type: String?): Class<*>? {
        var type = type
        type = type.trim()
        if ("void".equals(type)) return Void.TYPE
        if ("double".equals(type)) return Double::class.javaPrimitiveType
        if ("int".equals(type)) return Int::class.javaPrimitiveType
        if ("long".equals(type)) return Long::class.javaPrimitiveType
        if ("short".equals(type)) return Short::class.javaPrimitiveType
        if ("char".equals(type)) return Char::class.javaPrimitiveType
        if ("byte".equals(type)) return Byte::class.javaPrimitiveType
        if ("boolean".equals(type)) return Boolean::class.javaPrimitiveType
        if ("float".equals(type)) return Float::class.javaPrimitiveType
        if ("Void".equals(type)) return Void.TYPE
        if ("Double".equals(type)) return Double::class.java
        if ("Integer".equals(type)) return Integer::class.java
        if ("Long".equals(type)) return Long::class.java
        if ("Short".equals(type)) return Short::class.java
        if ("Character".equals(type)) return Character::class.java
        if ("Byte".equals(type)) return Byte::class.java
        if ("Boolean".equals(type)) return Boolean::class.java
        if ("Float".equals(type)) return Float::class.java
        if ("Object".equalsIgnoreCase(type)) return Object::class.java
        val lcType: String = StringUtil.toLowerCase(type)
        if (lcType.length() > 2) {
            val first: Char = lcType.charAt(0)
            when (first) {
                'a' -> if (lcType.equals("any")) {
                    return Object::class.java
                } else if (lcType.equals("array")) {
                    return Array::class.java
                }
                'b' -> if (lcType.equals("binary")) {
                    return ByteArray::class.java
                } else if (lcType.equals("base64")) {
                    return String::class.java
                }
                'c' -> if (lcType.equals("component")) {
                    return Component::class.java
                }
                'd' -> if (lcType.equals("date")) {
                    return Date::class.java
                } else if (lcType.equals("datetime")) {
                    return Date::class.java
                }
                'n' -> if (lcType.equals("numeric")) {
                    return Double::class.java
                } else if (lcType.equals("number")) {
                    return Double::class.java
                } else if (lcType.equals("node")) {
                    return Node::class.java
                }
                'o' -> if (lcType.equals("object")) {
                    return Object::class.java
                }
                'q' -> if (lcType.equals("query")) {
                    return Query::class.java
                }
                's' -> if (lcType.equals("string")) {
                    return String::class.java
                } else if (lcType.equals("struct")) {
                    return Struct::class.java
                }
                't' -> if (lcType.equals("timespan")) {
                    return TimeSpan::class.java
                }
                'x' -> if (lcType.equals("xml")) {
                    return Node::class.java
                }
            }
        }

        // array
        /*
		 * if (type.endsWith("[]")) { Class clazz = toClass(type.substring(0, type.length() - 2)); clazz =
		 * ClassUtil.toArrayClass(clazz); return clazz; }
		 */return ClassUtil.loadClass(CFMLEngineFactory::class.java.getClassLoader(), type)
    }

    private fun toJavaFunctionType(clazz: Class<*>?, defaultValue: Class<*>?): Class<*>? {
        if (Void.TYPE === clazz) return Void.TYPE
        if (Double::class.javaPrimitiveType === clazz) return Double::class.javaPrimitiveType
        if (Double::class.java === clazz) return Double::class.javaPrimitiveType
        if (Int::class.javaPrimitiveType === clazz) return Int::class.javaPrimitiveType
        if (Integer::class.java === clazz) return Int::class.javaPrimitiveType
        if (Long::class.javaPrimitiveType === clazz) return Long::class.javaPrimitiveType
        if (Long::class.java === clazz) return Long::class.javaPrimitiveType
        if (Boolean::class.javaPrimitiveType === clazz) return Boolean::class.javaPrimitiveType
        if (Boolean::class.java === clazz) return Boolean::class.javaPrimitiveType
        return if (Object::class.java === clazz) Object::class.java else defaultValue
        // throw new JavaSourceException("invalid type definition [" + clazz.getName() + "],valid types are
        // [void,boolean,double,int,long,Object]");
    }

    init {
        // void
        map.put(Void.TYPE, arrayOf<JavaFunctionDef?>(JavaFunctionDef(Consumer::class.java, "accept", arrayOf<Class?>(Object::class.java), Void.TYPE),
                JavaFunctionDef(DoubleConsumer::class.java, "accept", arrayOf<Class?>(Double::class.javaPrimitiveType), Void.TYPE),
                JavaFunctionDef(IntConsumer::class.java, "accept", arrayOf<Class?>(Int::class.javaPrimitiveType), Void.TYPE),
                JavaFunctionDef(LongConsumer::class.java, "accept", arrayOf<Class?>(Long::class.javaPrimitiveType), Void.TYPE),
                JavaFunctionDef(ObjDoubleConsumer::class.java, "accept", arrayOf<Class?>(Object::class.java, Double::class.javaPrimitiveType), Void.TYPE),
                JavaFunctionDef(BiConsumer::class.java, "accept", arrayOf<Class?>(Object::class.java, Object::class.java), Void.TYPE),
                JavaFunctionDef(ObjIntConsumer::class.java, "accept", arrayOf<Class?>(Object::class.java, Int::class.javaPrimitiveType), Void.TYPE),
                JavaFunctionDef(ObjLongConsumer::class.java, "accept", arrayOf<Class?>(Object::class.java, Long::class.javaPrimitiveType), Void.TYPE)
        ))
        // boolean
        map.put(Boolean::class.javaPrimitiveType, arrayOf<JavaFunctionDef?>(JavaFunctionDef(BooleanSupplier::class.java, "getAsBoolean", arrayOf<Class?>(), Boolean::class.javaPrimitiveType),
                JavaFunctionDef(DoublePredicate::class.java, "test", arrayOf<Class?>(Double::class.javaPrimitiveType), Boolean::class.javaPrimitiveType),
                JavaFunctionDef(IntPredicate::class.java, "test", arrayOf<Class?>(Int::class.javaPrimitiveType), Boolean::class.javaPrimitiveType),
                JavaFunctionDef(LongPredicate::class.java, "test", arrayOf<Class?>(Long::class.javaPrimitiveType), Boolean::class.javaPrimitiveType),
                JavaFunctionDef(Predicate::class.java, "test", arrayOf<Class?>(Object::class.java), Boolean::class.javaPrimitiveType),
                JavaFunctionDef(BiPredicate::class.java, "test", arrayOf<Class?>(Object::class.java, Object::class.java), Boolean::class.javaPrimitiveType)
        ))
        // double
        map.put(Double::class.javaPrimitiveType, arrayOf<JavaFunctionDef?>(JavaFunctionDef(IntToDoubleFunction::class.java, "applyAsDouble", arrayOf<Class?>(Int::class.javaPrimitiveType), Double::class.javaPrimitiveType),
                JavaFunctionDef(DoubleSupplier::class.java, "getAsDouble", arrayOf<Class?>(), Double::class.javaPrimitiveType),
                JavaFunctionDef(DoubleBinaryOperator::class.java, "applyAsDouble", arrayOf<Class?>(Double::class.javaPrimitiveType, Double::class.javaPrimitiveType), Double::class.javaPrimitiveType),
                JavaFunctionDef(DoubleUnaryOperator::class.java, "applyAsDouble", arrayOf<Class?>(Double::class.javaPrimitiveType), Double::class.javaPrimitiveType),
                JavaFunctionDef(LongToDoubleFunction::class.java, "applyAsDouble", arrayOf<Class?>(Long::class.javaPrimitiveType), Double::class.javaPrimitiveType),
                JavaFunctionDef(ToDoubleBiFunction::class.java, "applyAsDouble", arrayOf<Class?>(Object::class.java, Object::class.java), Double::class.javaPrimitiveType),
                JavaFunctionDef(ToDoubleFunction::class.java, "applyAsDouble", arrayOf<Class?>(Object::class.java), Double::class.javaPrimitiveType)
        ))
        // int
        map.put(Int::class.javaPrimitiveType, arrayOf<JavaFunctionDef?>(JavaFunctionDef(IntSupplier::class.java, "getAsInt", arrayOf<Class?>(), Int::class.javaPrimitiveType),
                JavaFunctionDef(DoubleToIntFunction::class.java, "applyAsInt", arrayOf<Class?>(Double::class.javaPrimitiveType), Int::class.javaPrimitiveType),
                JavaFunctionDef(IntBinaryOperator::class.java, "applyAsInt", arrayOf<Class?>(Int::class.javaPrimitiveType, Int::class.javaPrimitiveType), Int::class.javaPrimitiveType),
                JavaFunctionDef(IntUnaryOperator::class.java, "applyAsInt", arrayOf<Class?>(Int::class.javaPrimitiveType), Int::class.javaPrimitiveType),
                JavaFunctionDef(LongToIntFunction::class.java, "applyAsInt", arrayOf<Class?>(Long::class.javaPrimitiveType), Int::class.javaPrimitiveType),
                JavaFunctionDef(ToIntBiFunction::class.java, "applyAsInt", arrayOf<Class?>(Object::class.java, Object::class.java), Int::class.javaPrimitiveType),
                JavaFunctionDef(ToIntFunction::class.java, "applyAsInt", arrayOf<Class?>(Object::class.java), Int::class.javaPrimitiveType)
        ))
        // long
        map.put(Long::class.javaPrimitiveType, arrayOf<JavaFunctionDef?>(JavaFunctionDef(LongSupplier::class.java, "getAsLong", arrayOf<Class?>(), Long::class.javaPrimitiveType),
                JavaFunctionDef(DoubleToLongFunction::class.java, "applyAsLong", arrayOf<Class?>(Double::class.javaPrimitiveType), Long::class.javaPrimitiveType),
                JavaFunctionDef(IntToLongFunction::class.java, "applyAsLong", arrayOf<Class?>(Int::class.javaPrimitiveType), Long::class.javaPrimitiveType),
                JavaFunctionDef(LongBinaryOperator::class.java, "applyAsLong", arrayOf<Class?>(Long::class.javaPrimitiveType, Long::class.javaPrimitiveType), Long::class.javaPrimitiveType),
                JavaFunctionDef(LongUnaryOperator::class.java, "applyAsLong", arrayOf<Class?>(Long::class.javaPrimitiveType), Long::class.javaPrimitiveType),
                JavaFunctionDef(ToLongBiFunction::class.java, "applyAsLong", arrayOf<Class?>(Object::class.java, Object::class.java), Long::class.javaPrimitiveType),
                JavaFunctionDef(ToLongFunction::class.java, "applyAsLong", arrayOf<Class?>(Object::class.java), Long::class.javaPrimitiveType)
        ))
        // Object
        map.put(Object::class.java, arrayOf<JavaFunctionDef?>(JavaFunctionDef(Function::class.java, "apply", arrayOf<Class?>(Object::class.java), Object::class.java),
                JavaFunctionDef(DoubleFunction::class.java, "apply", arrayOf<Class?>(Double::class.javaPrimitiveType), Object::class.java),
                JavaFunctionDef(BiFunction::class.java, "apply", arrayOf<Class?>(Object::class.java, Object::class.java), Object::class.java),
                JavaFunctionDef(IntFunction::class.java, "apply", arrayOf<Class?>(Int::class.javaPrimitiveType), Object::class.java),
                JavaFunctionDef(LongFunction::class.java, "apply", arrayOf<Class?>(Long::class.javaPrimitiveType), Object::class.java),
                JavaFunctionDef(Supplier::class.java, "get", arrayOf<Class?>(), Object::class.java)))
    }
}