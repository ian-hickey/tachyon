package lucee.transformer.cfml.script.java.function

import java.io.File

class JavaFunctionDef(javaClassName: Class<*>?, javaMethodName: String?, args: Array<Class<*>?>?, rtn: Class<*>?, throwException: Boolean) : FunctionDef {
    private val javaClass: Class<*>?
    private val javaMethodName: String?
    protected var args: Array<Class<*>?>?
    protected var rtn: Class<*>?
    protected var throwException: Boolean

    constructor(javaClassName: Class<*>?, javaMethodName: String?, args: Array<Class<*>?>?, rtn: Class<*>?) : this(javaClassName, javaMethodName, args, rtn, false) {}

    /*
	 * public Class<?> getClazz() { return clazz; }
	 */
    fun getArgs(): Array<Class<*>?>? {
        return args
    }

    fun getRtn(): Class<*>? {
        return rtn
    }

    private fun toStringShort(): Object? {
        val sb: StringBuilder = StringBuilder().append(javaMethodName).append('(')
        var del = false
        for (arg in args!!) {
            if (del) sb.append(',')
            sb.append(toString(arg))
            del = true
        }
        return sb.append(')').toString()
    }

    @Override
    override fun toString(): String {
        val sb = StringBuilder()
        if (javaClass != null) sb.append(toString(javaClass)).append('.')
        return sb.append(toStringShort()).append('+').append(toString(rtn)).toString()
    }

    @Override
    override fun createSourceCode(ps: PageSource?, javaCode: String?, id: String?, functionName: String?, access: Int, modifier: Int, hint: String?, listArgs: List<Argument?>?,
                                  output: Boolean?, bufferOutput: Boolean?, displayName: String?, description: String?, returnFormat: Int, secureJson: Boolean?, verifyClient: Boolean?, localMode: Int): SourceCode? {
        val pack: String
        val className: String
        val argNames = toArgumentNames(listArgs)
        val argHints = toArgumentHints(listArgs)
        val parent: String = ps.getClassName()
        val index: Int = parent.lastIndexOf('.')
        if (index == -1) {
            pack = ""
            className = parent + "$" + id + id() // TODO id should not be necessary, but rename the class does not work, we first have to update ASM.
        } else {
            pack = parent.substring(0, index)
            className = parent.substring(index + 1).toString() + "$" + id + CreateUniqueId.invoke()
        }
        val sb = StringBuilder()
        sb.append("	public ").append(rtn.getName()).append(' ').append(javaMethodName).append('(')

        // args
        var del = false
        var n: String
        for (i in args.indices) {
            if (del) sb.append(',')
            sb.append(toString(args!![i])).append(' ').append(argNames!![i])
            del = true
        }
        sb.append(") ")
        if (throwException) sb.append(" throws Exception ")
        sb.append(StringUtil.replace(javaCode, "{", """
     {
     ${if (throwException) "" else "\ntry {"}
     lucee.loader.engine.CFMLEngine engine = CFMLEngineFactory.getInstance();
     PageContext pc = engine.getThreadPageContext();
     
     """.trimIndent(), true))
        if (!throwException) sb.append(" catch(Exception eeeee){throw new RuntimeException(eeeee);}} ")
        sb.append(createConstructor(className, id, functionName, access, modifier, hint, output, bufferOutput, displayName, description, returnFormat, secureJson, verifyClient,
                localMode))
        sb.append(createCallFunction(javaClass))
        sb.append(createGetFunctionArguments(argNames, argHints))
        return outerShell(ps, pack, className, id, functionName, javaClass, sb.toString())
    }

    private fun outerShell(ps: PageSource?, pack: String?, className: String?, id: String?, functionName: String?, javaFunctionClass: Class<*>?,
                           javaFunctionCode: String?): SourceCode? {
        val sb = StringBuilder()
        if (!pack.isEmpty()) sb.append("package $pack;\n")
        sb.append("import lucee.runtime.PageContext;\n")
        sb.append("import lucee.runtime.config.Config;\n")
        sb.append("import lucee.runtime.type.Query;\n")
        sb.append("import lucee.runtime.ext.function.BIF;\n")
        sb.append("import lucee.loader.engine.CFMLEngineFactory;\n")
        sb.append("import lucee.runtime.Component;\n")
        sb.append("import lucee.runtime.PageSource;\n")
        sb.append("import lucee.runtime.dump.DumpData;\n")
        sb.append("import lucee.runtime.type.FunctionArgument;\n")
        sb.append("import lucee.loader.engine.CFMLEngine;\n")
        sb.append("import lucee.runtime.exp.PageException;\n")
        sb.append("import lucee.runtime.type.Struct;\n")
        sb.append("import lucee.runtime.dump.DumpProperties;\n")
        sb.append("import lucee.runtime.type.UDF;\n")
        sb.append("import lucee.runtime.type.Collection.Key;\n")
        sb.append("import lucee.runtime.op.Caster;\n")
        sb.append("import lucee.runtime.type.FunctionArgumentImpl;\n")
        sb.append("public class $className")
        sb.append(" extends lucee.runtime.JF ")
        sb.append(" implements UDF")
        if (javaFunctionClass != null) sb.append("," + javaFunctionClass.getName())
        sb.append(" {\n")
        if (!StringUtil.isEmpty(javaFunctionCode)) sb.append(javaFunctionCode)
        sb.append("}")
        return SourceCode(functionName, if (pack.isEmpty()) className else pack.toString() + "." + className, sb.toString())
    }

    private fun createConstructor(className: String?, id: String?, functionName: String?, access: Int, modifier: Int, hint: String?, output: Boolean?, bufferOutput: Boolean?,
                                  displayName: String?, description: String?, returnFormat: Int, secureJson: Boolean?, verifyClient: Boolean?, localMode: Int): String? {
        val sb = StringBuilder()
        sb.append("public $className() {\n")

        // super(functionName, type, strType, description)
        sb.append("super(")
        sb.append(esc(functionName)).append(',')
        sb.append(access).append(',')
        sb.append(modifier).append(',')
        sb.append("(short)").append(CFTypes.toShortStrict(Caster.toTypeName(rtn), 0.toShort())).append(',')
        sb.append(esc(Caster.toTypeName(rtn))).append(',')
        sb.append(esc(hint)).append(',')
        sb.append(b(output)).append(',')
        sb.append(b(bufferOutput)).append(',')
        sb.append(esc(displayName)).append(',')
        sb.append(esc(description)).append(',')
        sb.append(returnFormat).append(',')
        sb.append(b(secureJson)).append(',')
        sb.append(b(verifyClient)).append(',')
        sb.append(localMode)
        sb.append(");\n")
        sb.append("}\n")
        return sb.toString()
    }

    private fun b(b: Boolean?): String? {
        if (b == null) return "null"
        return if (b) "Boolean.TRUE" else "Boolean.FALSE"
    }

    private fun createCallFunction(clazz: Class<*>?): String? {
        val sb = StringBuilder()
        sb.append("public Object call(PageContext pc, Object[] args, boolean bbbbbbbbbbbb) throws PageException {\n")
        sb.append("CFMLEngine engine = CFMLEngineFactory.getInstance();\n")
        sb.append("""	if (args.length == ${args!!.size}) {
""")
        sb.append("	try {\n")
        if (Void.TYPE !== rtn) sb.append("		return ")
        sb.append(javaMethodName).append('(')
        val types = StringBuilder()
        for (i in args.indices) {
            if (types.length() > 0) types.append(", ")
            types.append(args!![i].getName())
            if (i > 0) sb.append(", ")
            if (Object::class.java !== args!![i]) caster(sb, args!![i])
            sb.append("args[").append(i).append(']')
            if (Object::class.java !== args!![i]) sb.append(')')
        }
        sb.append(");\n")
        if (Void.TYPE === rtn) sb.append("		return null;\n")
        sb.append("	}\n")
        sb.append("	catch (Exception e) {\n")
        sb.append("		throw Caster.toPageException(e);\n")
        sb.append("	}\n")
        sb.append("	}\n")
        sb.append("""	throw engine.getExceptionUtil().createApplicationException("invalid argument count (" + args.length + "), java function [${if (clazz == null) "" else clazz.getName().toString() + "."}$javaMethodName($types)] takes ${args!!.size} argument${if (args!!.size == 1) "" else "s"}");
""")
        sb.append("}\n")
        return sb.toString()
    }

    private fun createGetFunctionArguments(argNames: Array<String?>?, argHints: Array<String?>?): String? {
        val sb = StringBuilder()
        sb.append("public FunctionArgument[] getFunctionArguments() {\n")
        sb.append("	return new FunctionArgument[] {")
        for (i in argNames.indices) {
            if (i > 0) sb.append(',')
            sb.append("new FunctionArgumentImpl(lucee.runtime.type.KeyImpl.intern(").append(esc(argNames!![i])).append("),").append(esc(Caster.toClassName(args!![i]))).append(",")
                    .append("(short)").append(CFTypes.toShortStrict(Caster.toTypeName(args!![i]), 0.toShort())).append(',')
                    .append("false,FunctionArgument.DEFAULT_TYPE_NULL, true,\"\",").append(esc(argHints!![i])).append(")\n")
        }
        sb.append("};\n")
        sb.append("}\n")
        return sb.toString()
    }

    private fun esc(str: String?): String? {
        return if (str == null) "\"\"" else '"' + StringUtil.replace(StringUtil.replace(str, "\n", "\\n", false), "\"", "\\\"", false) + '"'
    }

    protected fun caster(sb: StringBuilder?, clazz: Class<*>?) {
        if (Long::class.javaPrimitiveType === clazz) sb.append("engine.getCastUtil().toLongValue(") else if (Long::class.java === clazz) sb.append("engine.getCastUtil().toLong(") else if (Double::class.javaPrimitiveType === clazz) sb.append("engine.getCastUtil().toDoubleValue(") else if (Double::class.java === clazz) sb.append("engine.getCastUtil().toDouble(") else if (Int::class.javaPrimitiveType === clazz) sb.append("engine.getCastUtil().toIntValue(") else if (Integer::class.java === clazz) sb.append("engine.getCastUtil().toInteger(") else if (Boolean::class.javaPrimitiveType === clazz) sb.append("engine.getCastUtil().toBooleanValue(") else if (Boolean::class.java === clazz) sb.append("engine.getCastUtil().toBoolean(") else if (Char::class.javaPrimitiveType === clazz) sb.append("engine.getCastUtil().toCharValue(") else if (Character::class.java === clazz) sb.append("engine.getCastUtil().toCharacter(") else if (Short::class.javaPrimitiveType === clazz) sb.append("engine.getCastUtil().toShortValue(") else if (Short::class.java === clazz) sb.append("engine.getCastUtil().toShort(") else if (Byte::class.javaPrimitiveType === clazz) sb.append("engine.getCastUtil().toByteValue(") else if (Byte::class.java === clazz) sb.append("engine.getCastUtil().toByte(") else if (Float::class.javaPrimitiveType === clazz) sb.append("engine.getCastUtil().toFloatValue(") else if (Float::class.java === clazz) sb.append("engine.getCastUtil().toFloat(") else if (Array::class.java === clazz) sb.append("engine.getCastUtil().toArray(") else if (BigDecimal::class.java === clazz) sb.append("engine.getCastUtil().toBigDecimal(") else if (ByteArray::class.java === clazz) sb.append("engine.getCastUtil().toBinary(") else if (Collection::class.java === clazz) sb.append("engine.getCastUtil().toCollection(") else if (Component::class.java === clazz) sb.append("engine.getCastUtil().toComponent(") else if (File::class.java === clazz) sb.append("engine.getCastUtil().toFile(") else if (Iterator::class.java === clazz) sb.append("engine.getCastUtil().toIterator(") else if (Locale::class.java === clazz) sb.append("engine.getCastUtil().toLocale(") else if (Map::class.java === clazz) sb.append("engine.getCastUtil().toMap(") else if (Array<Object>::class.java === clazz) sb.append("engine.getCastUtil().toNativeArray(") else if (Node::class.java === clazz) sb.append("engine.getCastUtil().toNode(") else if (NodeList::class.java === clazz) sb.append("engine.getCastUtil().toNodeList(") else if (Query::class.java === clazz) sb.append("engine.getCastUtil().toQuery(") else if (String::class.java === clazz) sb.append("engine.getCastUtil().toString(") else if (Struct::class.java === clazz) sb.append("engine.getCastUtil().toStruct(") else if (TimeSpan::class.java === clazz) sb.append("engine.getCastUtil().toTimeSpan(") else if (TimeZone::class.java === clazz) sb.append("engine.getCastUtil().toTimeZone(") else if (Object::class.java === clazz) sb.append("(") else if (BigInteger::class.java === clazz) sb.append("lucee.runtime.op.Caster.toBigInteger(") else if (CharSequence::class.java === clazz) sb.append("lucee.runtime.op.Caster.toCharSequence(") else if (UDF::class.java === clazz) sb.append("lucee.runtime.op.Caster.toFunction(") else if (MutableCollection::class.java === clazz) sb.append("lucee.runtime.op.Caster.toJavaCollection(") else if (List::class.java === clazz) sb.append("lucee.runtime.op.Caster.toList(") else if (Date::class.java === clazz) sb.append("lucee.runtime.op.Caster.toDate(") else sb.append("(" + Caster.toClassName(clazz).toString() + ")lucee.runtime.op.Caster.castTo(\"" + Caster.toClassName(clazz).toString() + "\",")
        // else sb.append("(" + Caster.toClassName(clazz) + ")(");
    }

    companion object {
        private var _id: Long = 0
        protected fun toString(clazz: Class<*>?): Object? {
            return StringUtil.replace(Caster.toClassName(clazz), "java.lang.", "", true)
        }

        private fun toArgumentNames(args: List<Argument?>?): Array<String?>? {
            if (args == null) return arrayOfNulls<String?>(0)
            val it: Iterator<Argument?> = args.iterator()
            val arr = arrayOfNulls<String?>(args.size())
            var es: ExprString
            var i = 0
            while (it.hasNext()) {
                es = it.next().getName()
                if (es is LitString) arr[i] = (es as LitString).getString()
                if (StringUtil.isEmpty(arr[i])) arr[i] = "arg" + (i + 1)
                i++
            }
            return arr
        }

        private fun toArgumentHints(args: List<Argument?>?): Array<String?>? {
            if (args == null) return arrayOfNulls<String?>(0)
            val it: Iterator<Argument?> = args.iterator()
            val arr = arrayOfNulls<String?>(args.size())
            var es: ExprString
            var i = 0
            while (it.hasNext()) {
                es = it.next().getHint()
                if (es is LitString) arr[i] = (es as LitString).getString()
                if (StringUtil.isEmpty(arr[i])) arr[i] = ""
                i++
            }
            return arr
        }

        @Synchronized
        fun id(): String? {
            _id++
            if (_id < 0) _id = 1
            return Long.toString(_id, Character.MAX_RADIX)
        }
    }

    init {
        javaClass = javaClassName
        this.javaMethodName = javaMethodName
        this.args = args
        this.rtn = rtn
        this.throwException = throwException
    }
}