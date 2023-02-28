/**
 * Copyright (c) 2014, the Railo Company Ltd.
 * Copyright (c) 2015, Tachyon Assosication Switzerland
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
package tachyon.transformer.bytecode.statement.udf

import java.util.ArrayList

abstract class Function : StatementBaseNoFinal, Opcodes, IFunction, HasBody {
    var name: ExprString?
    var returnType: ExprString? = null
    var output: ExprBoolean?
    var bufferOutput: ExprBoolean? = null

    // ExprBoolean abstry=LitBoolean.FALSE;
    var access: Int = Component.ACCESS_PUBLIC
    var displayName: ExprString?
    var hint: ExprString?
    var body: Body?
    var arguments: List<Argument?>? = ArrayList<Argument?>()
    var metadata: Map<String?, Attribute?>? = null
    var returnFormat: ExprString? = null
    var description: ExprString? = null
    var secureJson: ExprBoolean? = null
    var verifyClient: ExprBoolean? = null
    var localMode: ExprInt? = null

    // protected int localIndex = -1;
    private var cachedWithin: Literal? = null
    private var modifier: Int
    protected var jf: JavaFunction? = null

    // private final Root root;
    protected var index = -1

    constructor(name: String?, access: Int, modifier: Int, returnType: String?, body: Body?, start: Position?, end: Position?) : super(body.getFactory(), start, end) {
        this.name = body.getFactory().createLitString(name)
        this.access = access
        this.modifier = modifier
        if (!StringUtil.isEmpty(returnType)) this.returnType = body.getFactory().createLitString(returnType) else this.returnType = body.getFactory().createLitString("any")
        this.body = body
        body.setParent(this)
        output = body.getFactory().TRUE()
        displayName = body.getFactory().EMPTY()
        hint = body.getFactory().EMPTY()
    }

    constructor(name: Expression?, returnType: Expression?, returnFormat: Expression?, output: Expression?, bufferOutput: Expression?, access: Int, displayName: Expression?,
                description: Expression?, hint: Expression?, secureJson: Expression?, verifyClient: Expression?, localMode: Expression?, cachedWithin: Literal?, modifier: Int, body: Body?,
                start: Position?, end: Position?) : super(body.getFactory(), start, end) {
        this.name = body.getFactory().toExprString(name)
        this.returnType = body.getFactory().toExprString(returnType)
        this.returnFormat = if (returnFormat != null) body.getFactory().toExprString(returnFormat) else null
        this.output = body.getFactory().toExprBoolean(output)
        this.bufferOutput = if (bufferOutput == null) null else body.getFactory().toExprBoolean(bufferOutput)
        this.access = access
        this.description = if (description != null) body.getFactory().toExprString(description) else null
        this.displayName = body.getFactory().toExprString(displayName)
        this.hint = body.getFactory().toExprString(hint)
        this.secureJson = if (secureJson != null) body.getFactory().toExprBoolean(secureJson) else null
        this.verifyClient = if (verifyClient != null) body.getFactory().toExprBoolean(verifyClient) else null
        this.cachedWithin = cachedWithin
        this.modifier = modifier
        this.localMode = toLocalMode(localMode, null)
        this.body = body
        body.setParent(this)
    }

    fun register(page: Page?) {
        index = page.addFunction(this)
    }

    @Override
    @Throws(TransformerException::class)
    fun writeOut(bc: BytecodeContext?, type: Int) {
        // register(bc.getPage());
        ExpressionUtil.visitLine(bc, getStart())
        _writeOut(bc, type)
        ExpressionUtil.visitLine(bc, getEnd())
    }

    @Override
    @Throws(TransformerException::class)
    fun _writeOut(bc: BytecodeContext?) {
        // register(bc.getPage());
        _writeOut(bc, PAGE_TYPE_REGULAR)
    }

    @Throws(TransformerException::class)
    abstract fun _writeOut(bc: BytecodeContext?, pageType: Int)
    fun loadUDFProperties(bc: BytecodeContext?, index: Int, type: Int) {
        val constr: ConstrBytecodeContext = bc.getConstructor()
        // GeneratorAdapter cga = constr.getAdapter();
        val ga: GeneratorAdapter = bc.getAdapter()

        // store to construction method
        constr.addUDFProperty(this, index, index, type)
        /*
		 * cga.visitVarInsn(ALOAD, 0); cga.visitFieldInsn(GETFIELD, bc.getClassName(), "udfs",
		 * Types.UDF_PROPERTIES_ARRAY.toString()); cga.push(arrayIndex);
		 * createUDFProperties(constr,valueIndex,type); cga.visitInsn(AASTORE);
		 */

        // load in execution method
        ga.visitVarInsn(ALOAD, 0)
        ga.visitFieldInsn(GETFIELD, bc.getClassName(), "udfs", Types.UDF_PROPERTIES_ARRAY.toString())
        ga.push(index)
        ga.visitInsn(AALOAD)
    }

    @Throws(TransformerException::class)
    fun createUDFProperties(bc: BytecodeContext?, index: Int, type: Int) {
        val adapter: GeneratorAdapter = bc.getAdapter()
        adapter.newInstance(Types.UDF_PROPERTIES_IMPL)
        adapter.dup()

        // Page
        adapter.loadThis()

        // PageSource
        if (type != TYPE_UDF) {
            adapter.loadThis()
            adapter.invokeVirtual(Types.PAGE, GET_PAGESOURCE)
        } else adapter.visitVarInsn(ALOAD, 1)

        // position
        adapter.push(if (getStart() == null) 0 else getStart().line)
        adapter.push(if (getEnd() == null) 0 else getEnd().line)

        // arguments
        createArguments(bc)
        // index
        adapter.push(index)
        // name
        ExpressionUtil.writeOutSilent(name, bc, Expression.MODE_REF)
        // return type
        val sType: Short = ExpressionUtil.toShortType(returnType, false, CFTypes.TYPE_UNKNOW)
        if (sType == CFTypes.TYPE_UNKNOW) ExpressionUtil.writeOutSilent(returnType, bc, Expression.MODE_REF) else adapter.push(sType)

        // return format
        if (returnFormat != null) ExpressionUtil.writeOutSilent(returnFormat, bc, Expression.MODE_REF) else ASMConstants.NULL(adapter)

        // output
        ExpressionUtil.writeOutSilent(output, bc, Expression.MODE_VALUE)

        // access
        writeOutAccess(bc, access)
        var light = sType.toInt() != -1
        if (light && !bc.getFactory().EMPTY().equals(displayName)) light = false
        if (light && description != null && !bc.getFactory().EMPTY().equals(description)) light = false
        if (light && !bc.getFactory().EMPTY().equals(hint)) light = false
        if (light && secureJson != null) light = false
        if (light && verifyClient != null) light = false
        if (light && cachedWithin != null) light = false
        if (light && bufferOutput != null) light = false
        if (light && localMode != null) light = false
        if (light && modifier != Component.MODIFIER_NONE) light = false
        if (light && Page.hasMetaDataStruct(metadata, null)) light = false
        if (light) {
            adapter.invokeConstructor(Types.UDF_PROPERTIES_IMPL, INIT_UDF_PROPERTIES_SHORTTYPE_LIGHT)
            return
        }

        // buffer output
        if (bufferOutput != null) ExpressionUtil.writeOutSilent(bufferOutput, bc, Expression.MODE_REF) else ASMConstants.NULL(adapter)

        // displayName
        ExpressionUtil.writeOutSilent(displayName, bc, Expression.MODE_REF) // displayName;

        // description
        if (description != null) ExpressionUtil.writeOutSilent(description, bc, Expression.MODE_REF) // displayName;
        else adapter.push("")

        // hint
        ExpressionUtil.writeOutSilent(hint, bc, Expression.MODE_REF) // hint;

        // secureJson
        if (secureJson != null) ExpressionUtil.writeOutSilent(secureJson, bc, Expression.MODE_REF) else ASMConstants.NULL(adapter)

        // verify client
        if (verifyClient != null) ExpressionUtil.writeOutSilent(verifyClient, bc, Expression.MODE_REF) else ASMConstants.NULL(adapter)

        // cachedwithin
        if (cachedWithin != null) {
            cachedWithin.writeOut(bc, Expression.MODE_REF)
        } else ASMConstants.NULL(adapter)
        // adapter.push(cachedWithin<0?0:cachedWithin);

        // localMode
        if (localMode != null) ExpressionUtil.writeOutSilent(localMode, bc, Expression.MODE_REF) else ASMConstants.NULL(adapter)
        adapter.push(modifier)

        // meta
        Page.createMetaDataStruct(bc, metadata, null)
        adapter.invokeConstructor(Types.UDF_PROPERTIES_IMPL, if (sType.toInt() == -1) INIT_UDF_PROPERTIES_STRTYPE else INIT_UDF_PROPERTIES_SHORTTYPE)
    }

    @Throws(TransformerException::class)
    fun createFunction(bc: BytecodeContext?, index: Int, type: Int) {
        if (jf != null) {
            val adapter: GeneratorAdapter = bc.getAdapter()
            bc.registerJavaFunction(jf)
            adapter.loadArg(0)
            adapter.checkCast(Types.PAGE_CONTEXT_IMPL)
            adapter.visitVarInsn(ALOAD, 0)
            adapter.push(jf.getClassName())
            adapter.invokeVirtual(Types.PAGE_CONTEXT_IMPL, USE_JAVA_FUNCTION)
        } else {
            // new UDF(...)
            val adapter: GeneratorAdapter = bc.getAdapter()
            val t: Type
            t = if (TYPE_CLOSURE === type) Types.CLOSURE else if (TYPE_LAMBDA === type) Types.LAMBDA else Types.UDF_IMPL
            adapter.newInstance(t)
            adapter.dup()
            createUDFProperties(bc, index, type)
            adapter.invokeConstructor(t, INIT_UDF_IMPL_PROP)
        }
    }

    @Throws(TransformerException::class)
    private fun createArguments(bc: BytecodeContext?) {
        val ga: GeneratorAdapter = bc.getAdapter()
        ga.push(arguments!!.size())
        ga.newArray(FUNCTION_ARGUMENT)
        var arg: Argument?
        for (i in 0 until arguments!!.size()) {
            arg = arguments!![i]
            val canHaveKey: Boolean = Factory.canRegisterKey(arg.getName())

            // CHECK if default values
            // type
            val _strType: ExprString = arg.getType()
            var _type: Short = CFTypes.TYPE_UNKNOW
            if (_strType is LitString) {
                _type = CFTypes.toShortStrict((_strType as LitString).getString(), CFTypes.TYPE_UNKNOW)
            }
            val useType = !canHaveKey || _type != CFTypes.TYPE_ANY
            // boolean useStrType=useType && (_type==CFTypes.TYPE_UNDEFINED || _type==CFTypes.TYPE_UNKNOW ||
            // CFTypes.toString(_type, null)==null);

            // required
            val _req: ExprBoolean = arg.getRequired()
            val useReq = !canHaveKey || toBoolean(_req, null) !== Boolean.FALSE

            // default-type
            val _def: Expression = arg.getDefaultValueType(bc.getFactory())
            val useDef = !canHaveKey || toInt(_def, -1) != FunctionArgument.DEFAULT_TYPE_NULL

            // pass by reference
            val _pass: ExprBoolean = arg.isPassByReference()
            val usePass = !canHaveKey || toBoolean(_pass, null) !== Boolean.TRUE

            // display-hint
            val _dsp: ExprString = arg.getDisplayName()
            val useDsp = !canHaveKey || !isLiteralEmptyString(_dsp)

            // hint
            val _hint: ExprString = arg.getHint()
            val useHint = !canHaveKey || !isLiteralEmptyString(_hint)

            // meta
            val _meta: Map = arg.getMetaData()
            val useMeta = !canHaveKey || _meta != null && !_meta.isEmpty()
            var functionIndex = 7
            if (!useMeta) {
                functionIndex--
                if (!useHint) {
                    functionIndex--
                    if (!useDsp) {
                        functionIndex--
                        if (!usePass) {
                            functionIndex--
                            if (!useDef) {
                                functionIndex--
                                if (!useReq) {
                                    functionIndex--
                                    if (!useType) {
                                        functionIndex--
                                    }
                                }
                            }
                        }
                    }
                }
            }
            // write out arguments
            ga.dup()
            ga.push(i)

            // new FunctionArgument(...)
            ga.newInstance(if (canHaveKey && functionIndex < INIT_FAI_KEY_LIGHT!!.size) FUNCTION_ARGUMENT_LIGHT else FUNCTION_ARGUMENT_IMPL)
            ga.dup()
            // name
            bc.getFactory().registerKey(bc, arg.getName(), false)

            // type
            if (functionIndex >= INIT_FAI_KEY!!.size - 7) {
                _strType.writeOut(bc, Expression.MODE_REF)
                bc.getAdapter().push(_type)
            }
            // required
            if (functionIndex >= INIT_FAI_KEY.size - 6) _req.writeOut(bc, Expression.MODE_VALUE)
            // default value
            if (functionIndex >= INIT_FAI_KEY.size - 5) _def.writeOut(bc, Expression.MODE_VALUE)
            // pass by reference
            if (functionIndex >= INIT_FAI_KEY.size - 4) _pass.writeOut(bc, Expression.MODE_VALUE)
            // display-name
            if (functionIndex >= INIT_FAI_KEY.size - 3) _dsp.writeOut(bc, Expression.MODE_REF)
            // hint
            if (functionIndex >= INIT_FAI_KEY.size - 2) _hint.writeOut(bc, Expression.MODE_REF)
            // meta
            if (functionIndex == INIT_FAI_KEY.size - 1) Page.createMetaDataStruct(bc, _meta, null)
            if (functionIndex < INIT_FAI_KEY_LIGHT!!.size) ga.invokeConstructor(FUNCTION_ARGUMENT_LIGHT, INIT_FAI_KEY[functionIndex]) else ga.invokeConstructor(FUNCTION_ARGUMENT_IMPL, INIT_FAI_KEY[functionIndex])
            ga.visitInsn(Opcodes.AASTORE)
        }
    }

    private fun toInt(expr: Expression?, defaultValue: Int): Int {
        return if (expr is LitInteger) {
            (expr as LitInteger?).getInteger().intValue()
        } else defaultValue
    }

    private fun toBoolean(expr: ExprBoolean?, defaultValue: Boolean?): Boolean? {
        return if (expr is LitBoolean) {
            if ((expr as LitBoolean?).getBooleanValue()) Boolean.TRUE else Boolean.FALSE
        } else defaultValue
    }

    private fun isLiteralEmptyString(expr: ExprString?): Boolean {
        return if (expr is LitString) {
            StringUtil.isEmpty((expr as LitString?).getString())
        } else false
    }

    private fun writeOutAccess(bc: BytecodeContext?, expr: ExprString?) {

        // write short type
        if (expr is LitString) {
            val access: Int = ComponentUtil.toIntAccess((expr as LitString?).getString(), Component.ACCESS_PUBLIC)
            bc.getAdapter().push(access)
        } else bc.getAdapter().push(Component.ACCESS_PUBLIC)
    }

    private fun writeOutAccess(bc: BytecodeContext?, access: Int) {
        bc.getAdapter().push(access)
    }

    fun addArgument(factory: Factory?, name: String?, type: String?, required: Boolean, defaultValue: Expression?) {
        addArgument(factory.createLitString(name), factory.createLitString(type), factory.createLitBoolean(required), defaultValue, factory.TRUE(), factory.EMPTY(),
                factory.EMPTY(), null)
    }

    fun addArgument(name: Expression?, type: Expression?, required: Expression?, defaultValue: Expression?, passByReference: ExprBoolean?, displayName: Expression?,
                    hint: Expression?, meta: Map?) {
        arguments.add(Argument(name, type, required, defaultValue, passByReference, displayName, hint, meta))
    }

    /**
     * @return the arguments
     */
    fun getArguments(): List<Argument?>? {
        return arguments
    }

    /**
     * @return the body
     */
    @Override
    fun getBody(): Body? {
        return body
    }

    fun setMetaData(metadata: Map<String?, Attribute?>?) {
        this.metadata = metadata
    }

    fun setHint(factory: Factory?, hint: String?) {
        this.hint = factory.createLitString(hint)
    }

    @Throws(TemplateException::class)
    fun addAttribute(bc: BytecodeContext?, attr: Attribute?) {
        val name: String = attr.getName().toLowerCase()
        // name
        if ("name".equals(name)) {
            throw TransformerException(bc, "Name cannot be defined twice", getStart())
        } else if ("returntype".equals(name)) {
            returnType = toLitString(bc, name, attr.getValue())
        } else if ("access".equals(name)) {
            val ls: LitString? = toLitString(bc, name, attr.getValue())
            val strAccess: String = ls.getString()
            val acc: Int = ComponentUtil.toIntAccess(strAccess, -1)
            if (acc == -1) throw TransformerException(bc, "Invalid access type [$strAccess], access types are (remote, public, package, private)", getStart())
            access = acc
        } else if ("output".equals(name)) output = toLitBoolean(bc, name, attr.getValue()) else if ("bufferoutput".equals(name)) bufferOutput = toLitBoolean(bc, name, attr.getValue()) else if ("displayname".equals(name)) displayName = toLitString(bc, name, attr.getValue()) else if ("hint".equals(name)) hint = toLitString(bc, name, attr.getValue()) else if ("description".equals(name)) description = toLitString(bc, name, attr.getValue()) else if ("returnformat".equals(name)) returnFormat = toLitString(bc, name, attr.getValue()) else if ("securejson".equals(name)) secureJson = toLitBoolean(bc, name, attr.getValue()) else if ("verifyclient".equals(name)) verifyClient = toLitBoolean(bc, name, attr.getValue()) else if ("localmode".equals(name)) {
            val v: Expression = attr.getValue()
            if (v != null) {
                val str: String = ASMUtil.toString(bc, v, null)
                if (!StringUtil.isEmpty(str)) {
                    val mode: Int = AppListenerUtil.toLocalMode(str, -1)
                    if (mode != -1) localMode = v.getFactory().createLitInteger(mode) else throw TransformerException(bc, "Attribute [localMode] of the tag [Function], must be a literal value (modern, classic, true or false)", getStart())
                }
            }
        } else if ("cachedwithin".equals(name)) {
            try {
                cachedWithin = ASMUtil.cachedWithinValue(attr.getValue()) // ASMUtil.timeSpanToLong(attr.getValue());
            } catch (e: EvaluatorException) {
                throw TemplateException(e.getMessage())
            }
        } else if ("modifier".equals(name)) {
            val `val`: Expression = attr.getValue()
            if (`val` is Literal) {
                val l: Literal = `val` as Literal
                val str: String = StringUtil.emptyIfNull(l.getString()).trim()
                if ("abstract".equalsIgnoreCase(str)) modifier = Component.MODIFIER_ABSTRACT else if ("final".equalsIgnoreCase(str)) modifier = Component.MODIFIER_FINAL
            }
        } else {
            toLitString(bc, name, attr.getValue()) // needed for testing
            if (metadata == null) metadata = HashMap<String?, Attribute?>()
            metadata.put(attr.getName(), attr)
        }
    }

    @Throws(TransformerException::class)
    private fun toLitString(bc: BytecodeContext?, name: String?, value: Expression?): LitString? {
        val es: ExprString = value.getFactory().toExprString(value) as? LitString
                ?: throw TransformerException(bc, "Value of attribute [$name] must have a literal/constant value", getStart())
        return es as LitString
    }

    @Throws(TransformerException::class)
    private fun toLitBoolean(bc: BytecodeContext?, name: String?, value: Expression?): LitBoolean? {
        val eb: ExprBoolean = value.getFactory().toExprBoolean(value) as? LitBoolean
                ?: throw TransformerException(bc, "Value of attribute [$name] must have a literal/constant value", getStart())
        return eb as LitBoolean
    }

    @Throws(TransformerException::class)
    private fun toLitInt(bc: BytecodeContext?, name: String?, value: Expression?): ExprInt? {
        return value.getFactory().toExprInt(value) as? Literal
                ?: throw TransformerException(bc, "Value of attribute [$name] must have a literal/constant value", getStart())
    }

    fun setJavaFunction(jf: JavaFunction?) {
        this.jf = jf
    }

    fun setIndex(index: Int) {
        this.index = index
    }

    companion object {
        // Scope variablesScope()
        val VARIABLE_SCOPE: Method? = Method("variablesScope", Types.VARIABLES, arrayOf<Type?>())

        // Scope variablesScope()
        val GET_PAGESOURCE: Method? = Method("getPageSource", Types.PAGE_SOURCE, arrayOf<Type?>())

        // Object set(String,Object)
        /*
	 * static final Method SET_STR = new Method( "set", Types.OBJECT, new
	 * Type[]{Types.STRING,Types.OBJECT} );
	 */
        val SET_KEY: Method? = Method("set", Types.OBJECT, arrayOf<Type?>(Types.COLLECTION_KEY, Types.OBJECT))
        val REG_UDF_KEY: Method? = Method("registerUDF", Types.VOID, arrayOf<Type?>(Types.COLLECTION_KEY, Types.UDF_PROPERTIES))
        val REG_STATIC_UDF_KEY: Method? = Method("registerStaticUDF", Types.VOID, arrayOf<Type?>(Types.COLLECTION_KEY, Types.UDF_PROPERTIES))

        // private static final ExprString ANY = LitString.toExprString("any");
        // <init>(Page,FunctionArgument[],int String,String,boolean);
        private val FUNCTION_ARGUMENT: Type? = Type.getType(FunctionArgument::class.java)
        private val FUNCTION_ARGUMENT_IMPL: Type? = Type.getType(FunctionArgumentImpl::class.java)
        private val FUNCTION_ARGUMENT_LIGHT: Type? = Type.getType(FunctionArgumentLight::class.java)
        private val FUNCTION_ARGUMENT_ARRAY: Type? = Type.getType(Array<FunctionArgument>::class.java)
        protected val INIT_UDF_IMPL_PROP: Method? = Method("<init>", Types.VOID, arrayOf<Type?>(Types.UDF_PROPERTIES))
        private val INIT_UDF_PROPERTIES_STRTYPE: Method? = Method("<init>", Types.VOID, arrayOf<Type?>(Types.PAGE, Types.PAGE_SOURCE, Types.INT_VALUE, Types.INT_VALUE, FUNCTION_ARGUMENT_ARRAY, Types.INT_VALUE, Types.STRING, Types.STRING, Types.STRING,
                Types.BOOLEAN_VALUE, Types.INT_VALUE, Types.BOOLEAN, Types.STRING, Types.STRING, Types.STRING, Types.BOOLEAN, Types.BOOLEAN, Types.OBJECT, Types.INTEGER,
                Types.INT_VALUE, Types.STRUCT_IMPL))
        private val INIT_UDF_PROPERTIES_SHORTTYPE: Method? = Method("<init>", Types.VOID, arrayOf<Type?>(Types.PAGE, Types.PAGE_SOURCE, Types.INT_VALUE, Types.INT_VALUE, FUNCTION_ARGUMENT_ARRAY, Types.INT_VALUE, Types.STRING, Types.SHORT_VALUE, Types.STRING,
                Types.BOOLEAN_VALUE, Types.INT_VALUE, Types.BOOLEAN, Types.STRING, Types.STRING, Types.STRING, Types.BOOLEAN, Types.BOOLEAN, Types.OBJECT, Types.INTEGER,
                Types.INT_VALUE, Types.STRUCT_IMPL))
        private val INIT_UDF_PROPERTIES_SHORTTYPE_LIGHT: Method? = Method("<init>", Types.VOID, arrayOf<Type?>(Types.PAGE, Types.PAGE_SOURCE, Types.INT_VALUE, Types.INT_VALUE,
                FUNCTION_ARGUMENT_ARRAY, Types.INT_VALUE, Types.STRING, Types.SHORT_VALUE, Types.STRING, Types.BOOLEAN_VALUE, Types.INT_VALUE))

        // FunctionArgumentImpl(String name,String type,boolean required,int defaultType,String
        // dspName,String hint,StructImpl meta)
        private val INIT_FAI_KEY1: Method? = Method("<init>", Types.VOID, arrayOf<Type?>(Types.COLLECTION_KEY))
        private val INIT_FAI_KEY3: Method? = Method("<init>", Types.VOID, arrayOf<Type?>(Types.COLLECTION_KEY, Types.STRING, Types.SHORT_VALUE))
        private val INIT_FAI_KEY4: Method? = Method("<init>", Types.VOID, arrayOf<Type?>(Types.COLLECTION_KEY, Types.STRING, Types.SHORT_VALUE, Types.BOOLEAN_VALUE))
        private val INIT_FAI_KEY5: Method? = Method("<init>", Types.VOID, arrayOf<Type?>(Types.COLLECTION_KEY, Types.STRING, Types.SHORT_VALUE, Types.BOOLEAN_VALUE, Types.INT_VALUE))
        private val INIT_FAI_KEY6: Method? = Method("<init>", Types.VOID, arrayOf<Type?>(Types.COLLECTION_KEY, Types.STRING, Types.SHORT_VALUE, Types.BOOLEAN_VALUE, Types.INT_VALUE, Types.BOOLEAN_VALUE))
        private val INIT_FAI_KEY7: Method? = Method("<init>", Types.VOID, arrayOf<Type?>(Types.COLLECTION_KEY, Types.STRING, Types.SHORT_VALUE, Types.BOOLEAN_VALUE, Types.INT_VALUE, Types.BOOLEAN_VALUE, Types.STRING))
        private val INIT_FAI_KEY8: Method? = Method("<init>", Types.VOID, arrayOf<Type?>(Types.COLLECTION_KEY, Types.STRING, Types.SHORT_VALUE, Types.BOOLEAN_VALUE, Types.INT_VALUE, Types.BOOLEAN_VALUE, Types.STRING, Types.STRING))
        private val INIT_FAI_KEY9: Method? = Method("<init>", Types.VOID, arrayOf<Type?>(Types.COLLECTION_KEY, Types.STRING, Types.SHORT_VALUE, Types.BOOLEAN_VALUE,
                Types.INT_VALUE, Types.BOOLEAN_VALUE, Types.STRING, Types.STRING, Types.STRUCT_IMPL))
        private val INIT_FAI_KEY: Array<Method?>? = arrayOf<Method?>(INIT_FAI_KEY1, INIT_FAI_KEY3, INIT_FAI_KEY4, INIT_FAI_KEY5, INIT_FAI_KEY6, INIT_FAI_KEY7, INIT_FAI_KEY8,
                INIT_FAI_KEY9)
        private val INIT_FAI_KEY_LIGHT: Array<Method?>? = arrayOf<Method?>(INIT_FAI_KEY1, INIT_FAI_KEY3)
        protected val USE_JAVA_FUNCTION: Method? = Method("useJavaFunction", Types.OBJECT, arrayOf<Type?>(Types.PAGE, Types.STRING))
        protected val REG_JAVA_FUNCTION: Method? = Method("regJavaFunction", Types.VOID, arrayOf<Type?>(Types.COLLECTION_KEY, Types.STRING))
        fun toLocalMode(expr: Expression?, defaultValue: ExprInt?): ExprInt? {
            var mode = -1
            if (expr is Literal) {
                var str: String = (expr as Literal?).getString()
                str = str.trim().toLowerCase()
                mode = AppListenerUtil.toLocalMode(str, -1)
            }
            return if (mode == -1) defaultValue else expr.getFactory().createLitInteger(mode)
        }
    }
}