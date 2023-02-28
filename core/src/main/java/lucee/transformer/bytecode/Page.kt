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
package lucee.transformer.bytecode

import java.io.IOException

/**
 * represent a single Page
 */
class Page(factory: Factory?, config: Config?, sc: SourceCode?, tc: TagCIObject?, version: Long, lastModifed: Long, writeLog: Boolean, suppressWSbeforeArg: Boolean, output: Boolean,
           returnValue: Boolean, ignoreScopes: Boolean) : BodyBase(factory), Root {
    private val version: Long
    private val lastModifed: Long
    private val length: Long
    private val _writeLog: Boolean
    private val suppressWSbeforeArg: Boolean
    private val output: Boolean
    private val returnValue: Boolean
    val ignoreScopes: Boolean

    // private final PageSource pageSource;
    // private boolean isComponent;
    // private boolean isInterface;
    private val functions: ArrayList<IFunction?>? = ArrayList<IFunction?>()
    private val threads: ArrayList<ATagThread?>? = ArrayList<ATagThread?>()
    private var staticTextLocation: Resource? = null
    private var off = 0
    private var methodCount = 0

    // private final Config config;
    private var splitIfNecessary = false
    private var _comp: TagCIObject?
    private var className // following the pattern "or/susi/Sorglos"
            : String? = null
    private val config: Config?
    private val sourceCode: SourceCode?
    private val hash: Int
    private var javaFunctions: List<JavaFunction?>? = null
    private var javaFunctionNames: Set<String?>? = null

    /**
     * convert the Page Object to java bytecode
     *
     * @param cn name of the genrated class (only necessary when Page object has no PageSource
     * reference)
     * @return
     * @throws TransformerException
     */
    @Override
    @Throws(TransformerException::class)
    override fun execute(className: String?): ByteArray? {
        var className = className
        javaFunctions = null // most likely not necessary
        // not exists in any case, so every usage must have a plan b for not existence
        val optionalPS: PageSource? = if (sourceCode is PageSourceCode) (sourceCode as PageSourceCode?).getPageSource() else null
        val keys: List<LitString?> = ArrayList<LitString?>()
        val cw: ClassWriter = ASMUtil.getClassWriter()
        val imports: ArrayList<String?> = ArrayList<String?>()
        getImports(imports, this)

        // look for component if necessary
        val comp: TagCIObject? = getTagCFObject(null)

        // get class name
        if (!StringUtil.isEmpty(className)) {
            className = className.replace('.', '/')
            this.className = className
        } else {
            className = getClassName()
        }
        val isSub = comp != null && !comp.isMain()

        // parent
        var parent: String? = PageImpl::class.java.getName() // "lucee/runtime/Page";
        var interfaces: Array<String?>? = null
        if (isComponent(comp)) {
            parent = ComponentPageImpl::class.java.getName() // "lucee/runtime/ComponentPage";
            if (isSub) interfaces = arrayOf(SubPage::class.java.getName().replace('.', '/'))
        } else if (isInterface(comp)) parent = InterfacePageImpl::class.java.getName() // "lucee/runtime/InterfacePage";
        parent = parent.replace('.', '/')
        cw.visit(Opcodes.V1_6, Opcodes.ACC_PUBLIC + Opcodes.ACC_FINAL, className, null, parent, interfaces)
        if (optionalPS != null) {
            // we use full path when FD is enabled
            val path: String = if (config.allowRequestTimeout()) optionalPS.getRealpathWithVirtual() else optionalPS.getPhyscalFile().getAbsolutePath()
            cw.visitSource(path, null) // when adding more use ; as delimiter

            // cw.visitSource(optionalPS.getPhyscalFile().getAbsolutePath(),
            // "rel:"+optionalPS.getRealpathWithVirtual()); // when adding more use ; as delimiter
        } else {
            // cw.visitSource("","rel:");
        }

        // static constructor
        // GeneratorAdapter statConstrAdapter = new
        // GeneratorAdapter(Opcodes.ACC_PUBLIC,STATIC_CONSTRUCTOR,null,null,cw);
        // StaticConstrBytecodeContext statConstr = null;//new
        // BytecodeContext(null,null,this,externalizer,keys,cw,name,statConstrAdapter,STATIC_CONSTRUCTOR,writeLog(),suppressWSbeforeArg);

        /// boolean isSub = comp != null && !comp.isMain();

        // constructor
        val constrAdapter = GeneratorAdapter(Opcodes.ACC_PUBLIC, CONSTRUCTOR_PS, null, null, cw)
        val constr = ConstrBytecodeContext(optionalPS, this, keys, cw, className, constrAdapter, CONSTRUCTOR_PS, writeLog(), suppressWSbeforeArg, output,
                returnValue)
        constrAdapter.loadThis()
        val t: Type
        if (isComponent(comp)) {
            t = Types.COMPONENT_PAGE_IMPL

            // extends
            // Attribute attr = comp.getAttribute("extends");
            // if(attr!=null) ExpressionUtil.writeOutSilent(attr.getValue(),constr, Expression.MODE_REF);
            // else constrAdapter.push("");
            constrAdapter.invokeConstructor(t, CONSTRUCTOR)
        } else if (isInterface(comp)) {
            t = Types.INTERFACE_PAGE_IMPL
            constrAdapter.invokeConstructor(t, CONSTRUCTOR)
        } else {
            t = Types.PAGE_IMPL
            constrAdapter.invokeConstructor(t, CONSTRUCTOR)
        }

        // call _init()
        constrAdapter.visitVarInsn(Opcodes.ALOAD, 0)
        constrAdapter.visitMethodInsn(Opcodes.INVOKEVIRTUAL, constr.getClassName(), "initKeys", "()V")

        // private static ImportDefintion[] test=new ImportDefintion[]{...};
        run {
            val fv: FieldVisitor = cw.visitField(Opcodes.ACC_PRIVATE + Opcodes.ACC_FINAL, "imports", "[Llucee/runtime/component/ImportDefintion;", null, null)
            fv.visitEnd()
            constrAdapter.visitVarInsn(Opcodes.ALOAD, 0)
            val av = ArrayVisitor()
            av.visitBegin(constrAdapter, Types.IMPORT_DEFINITIONS, imports.size())
            var index = 0
            val it: Iterator<String?> = imports.iterator()
            while (it.hasNext()) {
                av.visitBeginItem(constrAdapter, index++)
                constrAdapter.push(it.next())
                ASMConstants.NULL(constrAdapter)
                constrAdapter.invokeStatic(Types.IMPORT_DEFINITIONS_IMPL, ID_GET_INSTANCE)
                av.visitEndItem(constrAdapter)
            }
            av.visitEnd()
            constrAdapter.visitFieldInsn(Opcodes.PUTFIELD, className, "imports", "[Llucee/runtime/component/ImportDefintion;")
        }

        // getVersion
        var adapter: GeneratorAdapter? = GeneratorAdapter(Opcodes.ACC_PUBLIC + Opcodes.ACC_FINAL, VERSION, null, null, cw)
        adapter.push(version)
        adapter.returnValue()
        adapter.endMethod()

        // public ImportDefintion[] getImportDefintions()
        if (imports.size() > 0) {
            adapter = GeneratorAdapter(Opcodes.ACC_PUBLIC + Opcodes.ACC_FINAL, GET_IMPORT_DEFINITIONS, null, null, cw)
            adapter.visitVarInsn(Opcodes.ALOAD, 0)
            adapter.visitFieldInsn(Opcodes.GETFIELD, className, "imports", "[Llucee/runtime/component/ImportDefintion;")
            adapter.returnValue()
            adapter.endMethod()
        } else {
            adapter = GeneratorAdapter(Opcodes.ACC_PUBLIC + Opcodes.ACC_FINAL, GET_IMPORT_DEFINITIONS, null, null, cw)
            adapter.visitInsn(Opcodes.ICONST_0)
            adapter.visitTypeInsn(Opcodes.ANEWARRAY, "lucee/runtime/component/ImportDefintion")
            adapter.returnValue()
            adapter.endMethod()
        }

        // getSourceLastModified
        adapter = GeneratorAdapter(Opcodes.ACC_PUBLIC + Opcodes.ACC_FINAL, LAST_MOD, null, null, cw)
        adapter.push(lastModifed)
        adapter.returnValue()
        adapter.endMethod()

        // getSourceLength
        adapter = GeneratorAdapter(Opcodes.ACC_PUBLIC + Opcodes.ACC_FINAL, LENGTH, null, null, cw)
        adapter.push(length)
        adapter.returnValue()
        adapter.endMethod()

        // getSubname
        adapter = GeneratorAdapter(Opcodes.ACC_PUBLIC + Opcodes.ACC_FINAL, GET_SUBNAME, null, null, cw)
        val subName: String?
        subName = if (isSub) getName(comp, null) else null
        if (subName != null) adapter.push(subName) else ASMConstants.NULL(adapter)
        adapter.returnValue()
        adapter.endMethod()

        // getCompileTime
        adapter = GeneratorAdapter(Opcodes.ACC_PUBLIC + Opcodes.ACC_FINAL, COMPILE_TIME, null, null, cw)
        adapter.push(System.currentTimeMillis())
        adapter.returnValue()
        adapter.endMethod()

        // getHash
        adapter = GeneratorAdapter(Opcodes.ACC_PUBLIC + Opcodes.ACC_FINAL, HASH, null, null, cw)
        adapter.push(hash)
        adapter.returnValue()
        adapter.endMethod()

        // static consructor for component/interface
        if (comp != null) {
            writeOutStaticConstructor(constr, keys, cw, comp, className)
        }
        var tmpFunctions: List<Function?>? = getFunctions()
        if (false && _comp is TagComponent) {
            var tc: TagComponent
            val tmps: List<Function?> = ArrayList()
            for (f in tmpFunctions!!) {
                tc = ASMUtil.getAncestorComponent(f)
                // function from another component
                if (tc != null && tc !== _comp) {
                    continue
                }
                tmps.add(f)
            }
            tmpFunctions = tmps
        }
        val functions: Array<Function?> = tmpFunctions.toArray(arrayOfNulls<Function?>(tmpFunctions!!.size()))
        val funcs: List<IFunction?>?
        // newInstance/initComponent/call
        funcs = if (isComponent()) {
            writeOutGetStaticStruct(constr, keys, cw, comp, className)
            writeOutNewComponent(constr, keys, cw, comp, className)
            writeOutInitComponent(constr, functions, keys, cw, comp, className)
        } else if (isInterface()) {
            writeOutGetStaticStruct(constr, keys, cw, comp, className)
            writeOutNewInterface(constr, keys, cw, comp, className)
            writeOutInitInterface(constr, keys, cw, comp, className)
        } else {
            writeOutCall(constr, keys, cw, className)
        }

        // write UDFProperties to constructor
        // writeUDFProperties(bc,funcs,pageType);

        // udfCall
        var cv: ConditionVisitor?
        var div: DecisionIntVisitor?
        // Function[] functions = extractFunctions(constr.getUDFProperties());
        // less/equal than 10 functions
        if (isInterface()) {
        } else if (functions.size <= 10) {
            adapter = GeneratorAdapter(Opcodes.ACC_PUBLIC + Opcodes.ACC_FINAL, UDF_CALL, null, arrayOf<Type?>(Types.THROWABLE), cw)
            val bc = BytecodeContext(optionalPS, constr, this, keys, cw, className, adapter, UDF_CALL, writeLog(), suppressWSbeforeArg, output, returnValue)
            if (functions.size == 0) {
            } else if (functions.size == 1) {
                ExpressionUtil.visitLine(bc, functions[0].getStart())
                functions[0].getBody().writeOut(bc)
                ExpressionUtil.visitLine(bc, functions[0].getEnd())
            } else writeOutUdfCallInner(bc, functions, 0, functions.size)
            adapter.visitInsn(Opcodes.ACONST_NULL)
            adapter.returnValue()
            adapter.endMethod()
        } else {
            adapter = GeneratorAdapter(Opcodes.ACC_PUBLIC + Opcodes.ACC_FINAL, UDF_CALL, null, arrayOf<Type?>(Types.THROWABLE), cw)
            val bc = BytecodeContext(optionalPS, constr, this, keys, cw, className, adapter, UDF_CALL, writeLog(), suppressWSbeforeArg, output, returnValue)
            cv = ConditionVisitor()
            cv.visitBefore()
            var count = 0
            run {
                var i = 0
                while (i < functions.size) {
                    cv.visitWhenBeforeExpr()
                    div = DecisionIntVisitor()
                    div.visitBegin()
                    adapter.loadArg(2)
                    div.visitLT()
                    adapter.push(i + 10)
                    div.visitEnd(bc)
                    cv.visitWhenAfterExprBeforeBody(bc)
                    adapter.visitVarInsn(Opcodes.ALOAD, 0)
                    adapter.visitVarInsn(Opcodes.ALOAD, 1)
                    adapter.visitVarInsn(Opcodes.ALOAD, 2)
                    adapter.visitVarInsn(Opcodes.ILOAD, 3)
                    adapter.visitMethodInsn(Opcodes.INVOKEVIRTUAL, className, createFunctionName(++count), "(Llucee/runtime/PageContext;Llucee/runtime/type/UDF;I)Ljava/lang/Object;")
                    adapter.visitInsn(Opcodes.ARETURN) // adapter.returnValue();
                    cv.visitWhenAfterBody(bc)
                    i += 10
                }
            }
            cv.visitAfter(bc)
            adapter.visitInsn(Opcodes.ACONST_NULL)
            adapter.returnValue()
            adapter.endMethod()
            count = 0
            var innerCall: Method?
            var i = 0
            while (i < functions.size) {
                innerCall = Method(createFunctionName(++count), Types.OBJECT, arrayOf<Type?>(Types.PAGE_CONTEXT, USER_DEFINED_FUNCTION, Types.INT_VALUE))
                adapter = GeneratorAdapter(Opcodes.ACC_PRIVATE + Opcodes.ACC_FINAL, innerCall, null, arrayOf<Type?>(Types.THROWABLE), cw)
                writeOutUdfCallInner(BytecodeContext(optionalPS, constr, this, keys, cw, className, adapter, innerCall, writeLog(), suppressWSbeforeArg, output, returnValue),
                        functions, i, if (i + 10 > functions.size) functions.size else i + 10)
                adapter.visitInsn(Opcodes.ACONST_NULL)
                adapter.returnValue()
                adapter.endMethod()
                i += 10
            }
        }

        // threadCall
        val threads: Array<ATagThread?>? = getThreads()
        if (true) {
            adapter = GeneratorAdapter(Opcodes.ACC_PUBLIC + Opcodes.ACC_FINAL, THREAD_CALL, null, arrayOf<Type?>(Types.THROWABLE), cw)
            if (threads!!.size > 0) writeOutThreadCallInner(
                    BytecodeContext(optionalPS, constr, this, keys, cw, className, adapter, THREAD_CALL, writeLog(), suppressWSbeforeArg, output, returnValue), threads, 0,
                    threads.size)
            // adapter.visitInsn(Opcodes.ACONST_NULL);
            adapter.returnValue()
            adapter.endMethod()
        }

        // udfDefaultValue
        // less/equal than 10 functions
        if (isInterface()) {
        } else if (functions.size <= 10) {
            adapter = GeneratorAdapter(Opcodes.ACC_PUBLIC + Opcodes.ACC_FINAL, UDF_DEFAULT_VALUE, null, arrayOf<Type?>(Types.PAGE_EXCEPTION), cw)
            if (functions.size > 0) writeUdfDefaultValueInner(
                    BytecodeContext(optionalPS, constr, this, keys, cw, className, adapter, UDF_DEFAULT_VALUE, writeLog(), suppressWSbeforeArg, output, returnValue), functions,
                    0, functions.size)
            adapter.loadArg(DEFAULT_VALUE)
            adapter.returnValue()
            adapter.endMethod()
        } else {
            adapter = GeneratorAdapter(Opcodes.ACC_PUBLIC + Opcodes.ACC_FINAL, UDF_DEFAULT_VALUE, null, arrayOf<Type?>(Types.PAGE_EXCEPTION), cw)
            val bc = BytecodeContext(optionalPS, constr, this, keys, cw, className, adapter, UDF_DEFAULT_VALUE, writeLog(), suppressWSbeforeArg, output,
                    returnValue)
            cv = ConditionVisitor()
            cv.visitBefore()
            var count = 0
            run {
                var i = 0
                while (i < functions.size) {
                    cv.visitWhenBeforeExpr()
                    div = DecisionIntVisitor()
                    div.visitBegin()
                    adapter.loadArg(1)
                    div.visitLT()
                    adapter.push(i + 10)
                    div.visitEnd(bc)
                    cv.visitWhenAfterExprBeforeBody(bc)
                    adapter.visitVarInsn(Opcodes.ALOAD, 0)
                    adapter.visitVarInsn(Opcodes.ALOAD, 1)
                    adapter.visitVarInsn(Opcodes.ILOAD, 2)
                    adapter.visitVarInsn(Opcodes.ILOAD, 3)
                    adapter.visitVarInsn(Opcodes.ALOAD, 4)
                    adapter.visitMethodInsn(Opcodes.INVOKEVIRTUAL, className, "udfDefaultValue" + ++count, "(Llucee/runtime/PageContext;IILjava/lang/Object;)Ljava/lang/Object;")
                    adapter.visitInsn(Opcodes.ARETURN) // adapter.returnValue();
                    cv.visitWhenAfterBody(bc)
                    i += 10
                }
            }
            cv.visitAfter(bc)
            adapter.visitInsn(Opcodes.ACONST_NULL)
            adapter.returnValue()
            adapter.endMethod()
            count = 0
            var innerDefaultValue: Method?
            var i = 0
            while (i < functions.size) {
                innerDefaultValue = Method("udfDefaultValue" + ++count, Types.OBJECT, arrayOf<Type?>(Types.PAGE_CONTEXT, Types.INT_VALUE, Types.INT_VALUE, Types.OBJECT))
                adapter = GeneratorAdapter(Opcodes.ACC_PRIVATE + Opcodes.ACC_FINAL, innerDefaultValue, null, arrayOf<Type?>(Types.PAGE_EXCEPTION), cw)
                writeUdfDefaultValueInner(
                        BytecodeContext(optionalPS, constr, this, keys, cw, className, adapter, innerDefaultValue, writeLog(), suppressWSbeforeArg, output, returnValue),
                        functions, i, if (i + 10 > functions.size) functions.size else i + 10)
                adapter.loadArg(DEFAULT_VALUE)
                // adapter.visitInsn(Opcodes.ACONST_NULL);
                adapter.returnValue()
                adapter.endMethod()
                i += 10
            }
        }

        // CONSTRUCTOR
        val udfProperties: List<Data?> = constr!!.getUDFProperties()
        val udfpropsClassName: String = Types.UDF_PROPERTIES_ARRAY.toString()

        // new UDFProperties Array
        constrAdapter.visitVarInsn(Opcodes.ALOAD, 0)
        constrAdapter.push(functions.size) // MUST6 ATM the array is to big, it has empty spaces for every closure, this is not necessary
        constrAdapter.newArray(Types.UDF_PROPERTIES)
        constrAdapter.visitFieldInsn(Opcodes.PUTFIELD, getClassName(), "udfs", udfpropsClassName)

        // set item
        var data: Data?
        var index = -1
        for (f in functions) {
            index++
            data = getMatchingData(f, udfProperties)
            if (data == null) continue

            // for (Data data: udfProperties) {
            constrAdapter.visitVarInsn(Opcodes.ALOAD, 0)
            constrAdapter.visitFieldInsn(Opcodes.GETFIELD, constr.getClassName(), "udfs", Types.UDF_PROPERTIES_ARRAY.toString())
            // constrAdapter.push(data.arrayIndex);
            constrAdapter.push(index)
            // data.function.createUDFProperties(constr, data.valueIndex, data.type);
            data.function.createUDFProperties(constr, index, data.type)
            constrAdapter.visitInsn(Opcodes.AASTORE)
        }

        // setPageSource(pageSource);
        constrAdapter.visitVarInsn(Opcodes.ALOAD, 0)
        constrAdapter.visitVarInsn(Opcodes.ALOAD, 1)
        constrAdapter.invokeVirtual(t, SET_PAGE_SOURCE)
        constrAdapter.returnValue()
        constrAdapter.endMethod()

        // INIT KEYS
        var bcInit: BytecodeContext? = null
        run {
            val aInit = GeneratorAdapter(Opcodes.ACC_PRIVATE + Opcodes.ACC_FINAL, INIT_KEYS, null, null, cw)
            bcInit = BytecodeContext(optionalPS, constr, this, keys, cw, className, aInit, INIT_KEYS, writeLog(), suppressWSbeforeArg, output, returnValue)
            registerFields(bcInit, keys)
            aInit.returnValue()
            aInit.endMethod()
        }

        // set field subs
        val fv: FieldVisitor = cw.visitField(Opcodes.ACC_PRIVATE + Opcodes.ACC_FINAL, "subs", "[Llucee/runtime/CIPage;", null, null)
        fv.visitEnd()

        // create sub components/interfaces
        if (comp != null && comp.isMain()) {
            val subs: List<TagCIObject?>? = getSubs(null)
            if (!ArrayUtil.isEmpty(subs)) {
                val _it: Iterator<TagCIObject?> = subs!!.iterator()
                var tc: TagCIObject?
                while (_it.hasNext()) {
                    tc = _it.next()
                    tc.writeOut(bcInit, this)
                }
                writeGetSubPages(cw, className, subs, sourceCode.getDialect())
            }
        }
        return cw.toByteArray()
    }

    private fun getMatchingData(func: Function?, datas: List<Data?>?): Data? {
        for (d in datas!!) {
            if (d.function === func) return d
        }
        return null
    }

    private fun writeGetSubPages(cw: ClassWriter?, name: String?, subs: List<TagCIObject?>?, dialect: Int) {
        // pageSource.getFullClassName().replace('.', '/');
        val adapter = GeneratorAdapter(Opcodes.ACC_PUBLIC + Opcodes.ACC_FINAL, GET_SUB_PAGES, null, null, cw)
        val endIF = Label()
        adapter.visitVarInsn(Opcodes.ALOAD, 0)
        adapter.visitFieldInsn(Opcodes.GETFIELD, name, "subs", "[Llucee/runtime/CIPage;")
        adapter.visitJumpInsn(Opcodes.IFNONNULL, endIF)
        adapter.visitVarInsn(Opcodes.ALOAD, 0)
        val av = ArrayVisitor()
        av.visitBegin(adapter, Types.CI_PAGE, subs!!.size())
        val it: Iterator<TagCIObject?> = subs!!.iterator()
        var className: String?
        var index = 0
        while (it.hasNext()) {
            val ci: TagCIObject? = it.next()
            av.visitBeginItem(adapter, index++)
            className = createSubClass(name, ci.getName(), dialect)
            // ASMConstants.NULL(adapter);
            adapter.visitTypeInsn(Opcodes.NEW, className)
            adapter.visitInsn(Opcodes.DUP)
            adapter.visitVarInsn(Opcodes.ALOAD, 0)
            adapter.visitMethodInsn(Opcodes.INVOKEVIRTUAL, name, "getPageSource", "()Llucee/runtime/PageSource;")
            adapter.visitMethodInsn(Opcodes.INVOKESPECIAL, className, "<init>", "(Llucee/runtime/PageSource;)V")
            av.visitEndItem(adapter)
        }
        av.visitEnd()
        adapter.visitFieldInsn(Opcodes.PUTFIELD, name, "subs", "[Llucee/runtime/CIPage;")
        adapter.visitLabel(endIF)
        adapter.visitVarInsn(Opcodes.ALOAD, 0)
        adapter.visitFieldInsn(Opcodes.GETFIELD, name, "subs", "[Llucee/runtime/CIPage;")
        adapter.returnValue()
        adapter.endMethod()
    }

    private fun getName(ci: TagCIObject?, defaultValue: String?): String? {
        val attr: Attribute = ci.getAttribute("name") ?: return defaultValue
        val `val`: Expression = attr.getValue()
        return if (`val` !is LitString) defaultValue else (`val` as LitString).getString()
    }

    fun getClassName(): String? {
        if (className == null) {
            // only main components have a pageSource
            val optionalPS: PageSource? = if (sourceCode is PageSourceCode) (sourceCode as PageSourceCode?).getPageSource() else null
            if (optionalPS != null) className = optionalPS.getClassName() else {
                val comp: TagCIObject? = getTagCFObject(null)
                if (comp != null) {
                    className = createSubClass(className, comp.getName(), sourceCode.getDialect())
                }
            }
            if (className != null) className = className.replace('.', '/') else {
                throw IllegalArgumentException("You always need to defined a name for a sub component")
            }
            // in case we have a sub component
        }
        return className
    }

    /**
     * get the main component/interface from the Page
     *
     * @return
     * @throws TransformerException
     */
    private fun getTagCFObject(defaultValue: TagCIObject?): TagCIObject? {
        if (_comp != null) return _comp // return sub component

        // look for main
        val it: Iterator<Statement?> = getStatements().iterator()
        var s: Statement?
        var t: TagCIObject?
        val sub: TagCIObject? = null
        while (it.hasNext()) {
            s = it.next()
            if (s is TagCIObject) {
                t = s as TagCIObject?
                if (t.isMain()) return t.also { _comp = it }
                // else if (sub == null) sub = t;
            }
        }
        // if (sub != null) return _comp = sub;
        return defaultValue
    }

    private fun getSubs(defaultValue: Array<TagCIObject?>?): List<TagCIObject?>? {
        val it: Iterator<Statement?> = getStatements().iterator()
        var s: Statement?
        var t: TagCIObject?
        var subs: List<TagCIObject?>? = null
        while (it.hasNext()) {
            s = it.next()
            if (s is TagCIObject) {
                t = s as TagCIObject?
                if (!t.isMain()) {
                    if (subs == null) subs = ArrayList<TagCIObject?>()
                    subs.add(t)
                }
            }
        }
        return subs
    }

    private fun createFunctionName(i: Int): String? {
        return "udfCall" + Integer.toString(i, Character.MAX_RADIX)
    }

    fun writeLog(): Boolean {
        return _writeLog && !isInterface()
    }

    @Throws(TransformerException::class)
    private fun writeUdfDefaultValueInner(bc: BytecodeContext?, functions: Array<Function?>?, offset: Int, length: Int) {
        val adapter: GeneratorAdapter = bc!!.getAdapter()
        val cv = ConditionVisitor()
        var div: DecisionIntVisitor?
        cv.visitBefore()
        for (i in offset until length) {
            cv.visitWhenBeforeExpr()
            div = DecisionIntVisitor()
            div.visitBegin()
            adapter.loadArg(1)
            div.visitEQ()
            adapter.push(i)
            div.visitEnd(bc)
            cv.visitWhenAfterExprBeforeBody(bc)
            writeOutFunctionDefaultValueInnerInner(bc, functions!![i])
            cv.visitWhenAfterBody(bc)
        }
        cv.visitAfter(bc)
    }

    @Throws(TransformerException::class)
    private fun writeOutUdfCallInnerIf(bc: BytecodeContext?, functions: Array<Function?>?, offset: Int, length: Int) {
        val adapter: GeneratorAdapter = bc!!.getAdapter()
        val cv = ConditionVisitor()
        var div: DecisionIntVisitor?
        cv.visitBefore()
        for (i in offset until length) {
            cv.visitWhenBeforeExpr()
            div = DecisionIntVisitor()
            div.visitBegin()
            adapter.loadArg(2)
            div.visitEQ()
            adapter.push(i)
            div.visitEnd(bc)
            cv.visitWhenAfterExprBeforeBody(bc)
            ExpressionUtil.visitLine(bc, functions!![i].getStart())
            functions[i].getBody().writeOut(bc)
            ExpressionUtil.visitLine(bc, functions[i].getEnd())
            cv.visitWhenAfterBody(bc)
        }
        cv.visitAfter(bc)
    }

    @Throws(TransformerException::class)
    private fun writeOutUdfCallInner(bc: BytecodeContext?, functions: Array<Function?>?, offset: Int, length: Int) {
        val ns = NativeSwitch(bc!!.getFactory(), 2, NativeSwitch.ARG_REF, null, null)
        for (i in offset until length) {
            ns.addCase(i, functions!![i].getBody(), functions[i].getStart(), functions[i].getEnd(), true)
        }
        ns._writeOut(bc)
    }

    @Throws(TransformerException::class)
    private fun writeOutThreadCallInner(bc: BytecodeContext?, threads: Array<ATagThread?>?, offset: Int, length: Int) {
        val adapter: GeneratorAdapter = bc!!.getAdapter()
        val cv = ConditionVisitor()
        var div: DecisionIntVisitor?
        cv.visitBefore()
        for (i in offset until length) {
            cv.visitWhenBeforeExpr()
            div = DecisionIntVisitor()
            div.visitBegin()
            adapter.loadArg(1)
            div.visitEQ()
            adapter.push(i)
            div.visitEnd(bc)
            cv.visitWhenAfterExprBeforeBody(bc)
            val body: Body = threads!![i].getRealBody()
            if (body != null) body.writeOut(bc)
            cv.visitWhenAfterBody(bc)
        }
        cv.visitAfter(bc)
    }

    @Throws(TransformerException::class)
    private fun writeOutGetStaticStruct(constr: ConstrBytecodeContext?, keys: List<LitString?>?, cw: ClassWriter?, component: TagCIObject?, name: String?) {
        // public final static StaticStruct _static = new StaticStruct();
        val fv: FieldVisitor = cw.visitField(Opcodes.ACC_PRIVATE + Opcodes.ACC_STATIC + Opcodes.ACC_FINAL, "staticStruct", "Llucee/runtime/component/StaticStruct;", null, null)
        fv.visitEnd()
        run {
            val ga = GeneratorAdapter(Opcodes.ACC_STATIC, CINIT, null, null, cw)
            ga.newInstance(Types.STATIC_STRUCT)
            ga.dup()
            ga.invokeConstructor(Types.STATIC_STRUCT, CONSTR_STATIC_STRUCT)
            ga.putStatic(Type.getType(name), "staticStruct", Types.STATIC_STRUCT)
            ga.returnValue()
            ga.endMethod()
        }

        // public StaticStruct getStaticStruct() {return _static;}
        run {
            val ga = GeneratorAdapter(Opcodes.ACC_PUBLIC + Opcodes.ACC_FINAL, GET_STATIC_STRUCT, null, null, cw)
            ga.getStatic(Type.getType(name), "staticStruct", Types.STATIC_STRUCT)
            ga.returnValue()
            ga.endMethod()
        }
    }

    @Throws(TransformerException::class)
    private fun writeOutStaticConstructor(constr: ConstrBytecodeContext?, keys: List<LitString?>?, cw: ClassWriter?, component: TagCIObject?, name: String?) {

        // if(true) return;
        val adapter = GeneratorAdapter(Opcodes.ACC_PUBLIC + Opcodes.ACC_FINAL, STATIC_COMPONENT_CONSTR, null, arrayOf<Type?>(Types.PAGE_EXCEPTION), cw)
        val bc = BytecodeContext(null, constr, this, keys, cw, name, adapter, STATIC_COMPONENT_CONSTR, writeLog(), suppressWSbeforeArg, output, returnValue)
        val methodBegin = Label()
        val methodEnd = Label()

        // Scope oldData=null;
        val oldData: Int = adapter.newLocal(Types.VARIABLES)
        ASMConstants.NULL(adapter)
        adapter.storeLocal(oldData)

        // push body
        val localBC: Int = adapter.newLocal(Types.BODY_CONTENT)
        adapter.loadArg(0)
        adapter.invokeVirtual(Types.PAGE_CONTEXT, PUSH_BODY)
        adapter.storeLocal(localBC)

        // int oldCheckArgs= pc.undefinedScope().setMode(Undefined.MODE_NO_LOCAL_AND_ARGUMENTS);
        val oldCheckArgs: Int = adapter.newLocal(Types.INT_VALUE)
        adapter.loadArg(0)
        adapter.invokeVirtual(Types.PAGE_CONTEXT, UNDEFINED_SCOPE)
        adapter.push(Undefined.MODE_NO_LOCAL_AND_ARGUMENTS)
        adapter.invokeInterface(Types.UNDEFINED, SET_MODE)
        adapter.storeLocal(oldCheckArgs)
        val tcf = TryCatchFinallyVisitor(object : OnFinally() {
            @Override
            fun _writeOut(bc: BytecodeContext?) {

                // undefined.setMode(oldMode);
                adapter.loadArg(0)
                adapter.invokeVirtual(Types.PAGE_CONTEXT, UNDEFINED_SCOPE)
                adapter.loadLocal(oldCheckArgs, Types.INT_VALUE)
                adapter.invokeInterface(Types.UNDEFINED, SET_MODE)
                adapter.pop()

                // c.afterCall(pc,_oldData);
                // adapter.loadThis();
                adapter.loadArg(1)
                adapter.loadArg(0)
                adapter.loadLocal(oldData) // old variables scope
                adapter.invokeVirtual(Types.COMPONENT_IMPL, AFTER_STATIC_CONSTR)
            }
        }, null)
        tcf.visitTryBegin(bc)
        // oldData=c.beforeCall(pc);
        adapter.loadArg(1)
        adapter.loadArg(0)
        adapter.invokeVirtual(Types.COMPONENT_IMPL, BEFORE_STATIC_CONSTR)
        adapter.storeLocal(oldData)
        // ExpressionUtil.visitLine(bc, component.getStart());
        val list: List<StaticBody?> = component.getStaticBodies()
        if (list != null) {
            writeOutConstrBody(bc, list, IFunction.PAGE_TYPE_COMPONENT)
        }
        // ExpressionUtil.visitLine(bc, component.getEnd());
        val t: Int = tcf.visitTryEndCatchBeging(bc)
        // BodyContentUtil.flushAndPop(pc,bc);
        adapter.loadArg(0)
        adapter.loadLocal(localBC)
        adapter.invokeStatic(Types.BODY_CONTENT_UTIL, FLUSH_AND_POP)

        // throw Caster.toPageException(t);
        adapter.loadLocal(t)
        adapter.invokeStatic(Types.CASTER, TO_PAGE_EXCEPTION)
        adapter.throwException()
        tcf.visitCatchEnd(bc)
        adapter.loadArg(0)
        adapter.loadLocal(localBC)
        adapter.invokeStatic(Types.BODY_CONTENT_UTIL, FLUSH_AND_POP) // TODO why does the body constuctor call clear and it works?
        adapter.returnValue()
        adapter.visitLabel(methodEnd)
        adapter.endMethod()
    }

    @Throws(TransformerException::class)
    private fun writeOutConstrBody(bc: BytecodeContext?, bodies: List<StaticBody?>?, pageType: Int) {
        // get and remove all functions from body
        val funcs: List<IFunction?> = ArrayList<IFunction?>()
        var it: Iterator<StaticBody?> = bodies!!.iterator()
        while (it.hasNext()) {
            extractFunctions(bc, it.next(), funcs, pageType)
        }
        writeUDFProperties(bc, funcs, pageType)
        it = bodies.iterator()
        while (it.hasNext()) {
            BodyBase.writeOut(bc, it.next())
        }
    }

    @Throws(TransformerException::class)
    private fun writeOutInitComponent(constr: ConstrBytecodeContext?, functions: Array<Function?>?, keys: List<LitString?>?, cw: ClassWriter?, component: Tag?, name: String?): List<IFunction?>? {
        val adapter = GeneratorAdapter(Opcodes.ACC_PUBLIC + Opcodes.ACC_FINAL, INIT_COMPONENT3, null, arrayOf<Type?>(Types.PAGE_EXCEPTION), cw)
        val bc = BytecodeContext(null, constr, this, keys, cw, name, adapter, INIT_COMPONENT3, writeLog(), suppressWSbeforeArg, output, returnValue)
        val methodBegin = Label()
        val methodEnd = Label()
        adapter.visitLocalVariable("this", "L$name;", null, methodBegin, methodEnd, 0)
        adapter.visitLabel(methodBegin)

        // Scope oldData=null;
        val oldData: Int = adapter.newLocal(Types.VARIABLES)
        ASMConstants.NULL(adapter)
        adapter.storeLocal(oldData)
        val localBC: Int = adapter.newLocal(Types.BODY_CONTENT)
        val cv = ConditionVisitor()
        cv.visitBefore()
        cv.visitWhenBeforeExpr()
        adapter.loadArg(1)
        adapter.invokeVirtual(Types.COMPONENT_IMPL, GET_OUTPUT)
        cv.visitWhenAfterExprBeforeBody(bc)
        ASMConstants.NULL(adapter)
        cv.visitWhenAfterBody(bc)
        cv.visitOtherviseBeforeBody()
        adapter.loadArg(0)
        adapter.invokeVirtual(Types.PAGE_CONTEXT, PUSH_BODY)
        cv.visitOtherviseAfterBody()
        cv.visitAfter(bc)
        adapter.storeLocal(localBC)

        // c.init(pc,this);
        adapter.loadArg(1)
        adapter.loadArg(0)
        adapter.loadThis()
        adapter.loadArg(2)
        // adapter.visitVarInsn(Opcodes.ALOAD, 0);
        adapter.invokeVirtual(Types.COMPONENT_IMPL, INIT_COMPONENT)

        // return when executeConstr is false
        adapter.loadArg(2)
        val afterIf = Label()
        adapter.visitJumpInsn(Opcodes.IFNE, afterIf)
        adapter.loadArg(0)
        adapter.loadLocal(localBC)
        adapter.invokeStatic(Types.BODY_CONTENT_UTIL, CLEAR_AND_POP)
        adapter.visitInsn(Opcodes.RETURN)
        adapter.visitLabel(afterIf)

        // int oldCheckArgs= pc.undefinedScope().setMode(Undefined.MODE_NO_LOCAL_AND_ARGUMENTS);
        val oldCheckArgs: Int = adapter.newLocal(Types.INT_VALUE)
        adapter.loadArg(0)
        adapter.invokeVirtual(Types.PAGE_CONTEXT, UNDEFINED_SCOPE)
        adapter.push(Undefined.MODE_NO_LOCAL_AND_ARGUMENTS)
        adapter.invokeInterface(Types.UNDEFINED, SET_MODE)
        adapter.storeLocal(oldCheckArgs)
        val tcf = TryCatchFinallyVisitor(object : OnFinally() {
            @Override
            fun _writeOut(bc: BytecodeContext?) {

                // undefined.setMode(oldMode);
                adapter.loadArg(0)
                adapter.invokeVirtual(Types.PAGE_CONTEXT, UNDEFINED_SCOPE)
                adapter.loadLocal(oldCheckArgs, Types.INT_VALUE)
                adapter.invokeInterface(Types.UNDEFINED, SET_MODE)
                adapter.pop()

                // c.afterCall(pc,_oldData);
                adapter.loadArg(1)
                adapter.loadArg(0)
                adapter.loadLocal(oldData)
                adapter.invokeVirtual(Types.COMPONENT_IMPL, AFTER_CALL)
            }
        }, null)
        tcf.visitTryBegin(bc)
        // oldData=c.beforeCall(pc);
        adapter.loadArg(1)
        adapter.loadArg(0)
        adapter.invokeVirtual(Types.COMPONENT_IMPL, BEFORE_CALL)
        adapter.storeLocal(oldData)
        ExpressionUtil.visitLine(bc, component.getStart())
        val funcs: List<IFunction?>? = writeOutCallBody(bc, component.getBody(), IFunction.PAGE_TYPE_COMPONENT)
        ExpressionUtil.visitLine(bc, component.getEnd())
        val t: Int = tcf.visitTryEndCatchBeging(bc)
        // BodyContentUtil.flushAndPop(pc,bc);
        adapter.loadArg(0)
        adapter.loadLocal(localBC)
        adapter.invokeStatic(Types.BODY_CONTENT_UTIL, FLUSH_AND_POP)

        // throw Caster.toPageException(t);
        adapter.loadLocal(t)
        adapter.invokeStatic(Types.CASTER, TO_PAGE_EXCEPTION)
        adapter.throwException()
        tcf.visitCatchEnd(bc)
        adapter.loadArg(0)
        adapter.loadLocal(localBC)
        adapter.invokeStatic(Types.BODY_CONTENT_UTIL, CLEAR_AND_POP)
        adapter.returnValue()
        adapter.visitLabel(methodEnd)
        adapter.endMethod()
        return funcs
    }

    @Throws(TransformerException::class)
    private fun writeOutInitInterface(constr: ConstrBytecodeContext?, keys: List<LitString?>?, cw: ClassWriter?, interf: Tag?, name: String?): List<IFunction?>? {
        val adapter = GeneratorAdapter(Opcodes.ACC_PUBLIC + Opcodes.ACC_FINAL, INIT_INTERFACE, null, arrayOf<Type?>(Types.PAGE_EXCEPTION), cw)
        val bc = BytecodeContext(null, constr, this, keys, cw, name, adapter, INIT_INTERFACE, writeLog(), suppressWSbeforeArg, output, returnValue)
        val methodBegin = Label()
        val methodEnd = Label()
        adapter.visitLocalVariable("this", "L$name;", null, methodBegin, methodEnd, 0)
        adapter.visitLabel(methodBegin)
        ExpressionUtil.visitLine(bc, interf.getStart())
        val funcs: List<IFunction?>? = writeOutCallBody(bc, interf.getBody(), IFunction.PAGE_TYPE_INTERFACE)
        ExpressionUtil.visitLine(bc, interf.getEnd())
        adapter.returnValue()
        adapter.visitLabel(methodEnd)
        adapter.endMethod()
        return funcs
    }

    @Throws(TransformerException::class)
    private fun writeOutFunctionDefaultValueInnerInner(bc: BytecodeContext?, function: Function?) {
        val adapter: GeneratorAdapter = bc!!.getAdapter()
        val args: List<Argument?> = function.getArguments()
        if (args.size() === 0) {
            adapter.loadArg(DEFAULT_VALUE)
            adapter.returnValue()
            return
        }
        val it: Iterator<Argument?> = args.iterator()
        var arg: Argument?
        val cv = ConditionVisitor()
        var div: DecisionIntVisitor?
        cv.visitBefore()
        var count = 0
        while (it.hasNext()) {
            arg = it.next()
            cv.visitWhenBeforeExpr()
            div = DecisionIntVisitor()
            div.visitBegin()
            adapter.loadArg(2)
            div.visitEQ()
            adapter.push(count++)
            div.visitEnd(bc)
            cv.visitWhenAfterExprBeforeBody(bc)
            val defaultValue: Expression = arg.getDefaultValue()
            if (defaultValue != null) {
                /*
				 * if(defaultValue instanceof Null) { adapter.invokeStatic(NULL, GET_INSTANCE); } else
				 */
                defaultValue.writeOut(bc, Expression.MODE_REF)
            } else adapter.loadArg(DEFAULT_VALUE)
            // adapter.visitInsn(Opcodes.ACONST_NULL);
            adapter.returnValue()
            cv.visitWhenAfterBody(bc)
        }
        cv.visitOtherviseBeforeBody()
        // adapter.visitInsn(ACONST_NULL);
        // adapter.returnValue();
        cv.visitOtherviseAfterBody()
        cv.visitAfter(bc)
    }

    fun getFunctions(): List<Function?>? {
        val funcs: List<Function?> = ArrayList()
        for (f in functions) {
            funcs.add(f as Function)
        }
        return funcs
    }

    private fun getThreads(): Array<ATagThread?>? {
        val threads: Array<ATagThread?> = arrayOfNulls<ATagThread?>(threads.size())
        val it: Iterator<ATagThread?> = this.threads.iterator()
        var count = 0
        while (it.hasNext()) {
            threads[count++] = it.next()
        }
        return threads
    }

    @Override
    @Throws(TransformerException::class)
    override fun _writeOut(bc: BytecodeContext?) {
    }

    @Throws(TransformerException::class)
    private fun writeOutNewComponent(constr: ConstrBytecodeContext?, keys: List<LitString?>?, cw: ClassWriter?, component: Tag?, name: String?) {
        val adapter = GeneratorAdapter(Opcodes.ACC_PUBLIC + Opcodes.ACC_FINAL, NEW_COMPONENT_IMPL_INSTANCE, null, arrayOf<Type?>(Types.PAGE_EXCEPTION), cw)
        val bc = BytecodeContext(null, constr, this, keys, cw, name, adapter, NEW_COMPONENT_IMPL_INSTANCE, writeLog(), suppressWSbeforeArg, output, returnValue)
        val methodBegin = Label()
        val methodEnd = Label()
        adapter.visitLocalVariable("this", "L$name;", null, methodBegin, methodEnd, 0)
        ExpressionUtil.visitLine(bc, component.getStart())
        adapter.visitLabel(methodBegin)
        val comp: Int = adapter.newLocal(Types.COMPONENT_IMPL)
        adapter.newInstance(Types.COMPONENT_IMPL)
        adapter.dup()
        var attr: Attribute
        // ComponentPage
        adapter.visitVarInsn(Opcodes.ALOAD, 0)
        adapter.checkCast(Types.COMPONENT_PAGE_IMPL)

        // !!! also check CFMLScriptTransformer.addMetaData if you do any change here !!!

        // Output
        attr = component.removeAttribute("output")
        if (attr != null) {
            ExpressionUtil.writeOutSilent(attr.getValue(), bc, Expression.MODE_REF)
        } else ASMConstants.NULL(adapter)

        // synchronized
        attr = component.removeAttribute("synchronized")
        if (attr != null) ExpressionUtil.writeOutSilent(attr.getValue(), bc, Expression.MODE_VALUE) else adapter.push(false)

        // extends
        attr = component.removeAttribute("extends")
        if (attr != null) ExpressionUtil.writeOutSilent(attr.getValue(), bc, Expression.MODE_REF) else adapter.push("")

        // implements
        attr = component.removeAttribute("implements")
        if (attr != null) ExpressionUtil.writeOutSilent(attr.getValue(), bc, Expression.MODE_REF) else adapter.push("")

        // hint
        attr = component.removeAttribute("hint")
        if (attr != null) {
            var value: Expression = attr.getValue()
            if (value !is Literal) {
                value = bc!!.getFactory().createLitString("[runtime expression]")
            }
            ExpressionUtil.writeOutSilent(value, bc, Expression.MODE_REF)
        } else adapter.push("")

        // dspName
        attr = component.removeAttribute("displayname")
        if (attr == null) attr = component.getAttribute("display")
        if (attr != null) ExpressionUtil.writeOutSilent(attr.getValue(), bc, Expression.MODE_REF) else adapter.push("")

        // callpath
        adapter.visitVarInsn(Opcodes.ALOAD, 2)
        // realpath
        adapter.visitVarInsn(Opcodes.ILOAD, 3)

        // style
        attr = component.removeAttribute("style")
        if (attr != null) ExpressionUtil.writeOutSilent(attr.getValue(), bc, Expression.MODE_REF) else adapter.push("")

        // persistent
        attr = component.removeAttribute("persistent")
        var persistent = false
        if (attr != null) {
            persistent = ASMUtil.toBoolean(constr, attr, component.getStart()).booleanValue()
        }

        // accessors
        attr = component.removeAttribute("accessors")
        var accessors = false
        if (attr != null) {
            accessors = ASMUtil.toBoolean(constr, attr, component.getStart()).booleanValue()
        }

        // modifier
        attr = component.removeAttribute("modifier")
        var modifiers: Int = Component.MODIFIER_NONE
        if (attr != null) {
            // type already evaluated in evaluator
            val ls: LitString = component.getFactory().toExprString(attr.getValue()) as LitString
            modifiers = ComponentUtil.toModifier(ls.getString(), lucee.runtime.Component.MODIFIER_NONE, lucee.runtime.Component.MODIFIER_NONE)
        }
        adapter.push(persistent)
        adapter.push(accessors)
        adapter.push(modifiers)
        adapter.visitVarInsn(Opcodes.ILOAD, 4)

        // adapter.visitVarInsn(Opcodes.ALOAD, 4);
        createMetaDataStruct(bc, component.getAttributes(), component.getMetaData())
        adapter.invokeConstructor(Types.COMPONENT_IMPL, CONSTR_COMPONENT_IMPL15)
        adapter.storeLocal(comp)

        // Component Impl(ComponentPage componentPage,boolean output, String extend, String hint, String
        // dspName)

        // initComponent(pc,c);
        adapter.visitVarInsn(Opcodes.ALOAD, 0)
        adapter.loadArg(0)
        adapter.loadLocal(comp)
        adapter.loadArg(4)
        adapter.invokeVirtual(Types.COMPONENT_PAGE_IMPL, INIT_COMPONENT3)
        adapter.visitLabel(methodEnd)

        // return component;
        adapter.loadLocal(comp)
        adapter.returnValue()
        // ExpressionUtil.visitLine(adapter, component.getEndLine());
        adapter.endMethod()
    }

    @Throws(TransformerException::class)
    private fun writeOutNewInterface(constr: ConstrBytecodeContext?, keys: List<LitString?>?, cw: ClassWriter?, interf: Tag?, name: String?) {
        val adapter = GeneratorAdapter(Opcodes.ACC_PUBLIC + Opcodes.ACC_FINAL, NEW_INTERFACE_IMPL_INSTANCE, null, arrayOf<Type?>(Types.PAGE_EXCEPTION), cw)
        val bc = BytecodeContext(null, constr, this, keys, cw, name, adapter, NEW_INTERFACE_IMPL_INSTANCE, writeLog(), suppressWSbeforeArg, output, returnValue)
        val methodBegin = Label()
        val methodEnd = Label()
        adapter.visitLocalVariable("this", "L$name;", null, methodBegin, methodEnd, 0)
        ExpressionUtil.visitLine(bc, interf.getStart())
        adapter.visitLabel(methodBegin)

        // ExpressionUtil.visitLine(adapter, interf.getStartLine());
        val comp: Int = adapter.newLocal(Types.INTERFACE_IMPL)
        adapter.newInstance(Types.INTERFACE_IMPL)
        adapter.dup()

        // PageContext
        adapter.visitVarInsn(Opcodes.ALOAD, 1)

        // Interface Page
        adapter.visitVarInsn(Opcodes.ALOAD, 0)
        adapter.checkCast(Types.INTERFACE_PAGE_IMPL)

        // extened
        var attr: Attribute = interf.removeAttribute("extends")
        if (attr != null) ExpressionUtil.writeOutSilent(attr.getValue(), bc, Expression.MODE_REF) else adapter.push("")

        // hint
        attr = interf.removeAttribute("hint")
        if (attr != null) ExpressionUtil.writeOutSilent(attr.getValue(), bc, Expression.MODE_REF) else adapter.push("")

        // dspName
        attr = interf.removeAttribute("displayname")
        if (attr == null) attr = interf.getAttribute("display")
        if (attr != null) ExpressionUtil.writeOutSilent(attr.getValue(), bc, Expression.MODE_REF) else adapter.push("")

        // callpath
        adapter.visitVarInsn(Opcodes.ALOAD, 2)
        // relpath
        adapter.visitVarInsn(Opcodes.ILOAD, 3)

        // interface udfs
        // adapter.visitVarInsn(Opcodes.ALOAD, 3);
        createMetaDataStruct(bc, interf.getAttributes(), interf.getMetaData())
        adapter.invokeConstructor(Types.INTERFACE_IMPL, CONSTR_INTERFACE_IMPL8)
        adapter.storeLocal(comp)

        // initInterface(pc,c);
        adapter.visitVarInsn(Opcodes.ALOAD, 0)
        // adapter.loadArg(0);
        adapter.loadLocal(comp)
        adapter.invokeVirtual(Types.INTERFACE_PAGE_IMPL, INIT_INTERFACE)
        adapter.visitLabel(methodEnd)

        // return interface;
        adapter.loadLocal(comp)
        adapter.returnValue()
        // ExpressionUtil.visitLine(adapter, interf.getEndLine());
        adapter.endMethod()
    }

    @Throws(TransformerException::class)
    private fun writeOutCall(constr: ConstrBytecodeContext?, keys: List<LitString?>?, cw: ClassWriter?, name: String?): List<IFunction?>? {
        // GeneratorAdapter adapter = bc.getAdapter();
        val adapter = GeneratorAdapter(Opcodes.ACC_PUBLIC + Opcodes.ACC_FINAL, CALL1, null, arrayOf<Type?>(Types.THROWABLE), cw)
        val methodBegin = Label()
        val methodEnd = Label()
        adapter.visitLocalVariable("this", "L$name;", null, methodBegin, methodEnd, 0)
        adapter.visitLabel(methodBegin)
        val funcs: List<IFunction?>? = writeOutCallBody(BytecodeContext(null, constr, this, keys, cw, name, adapter, CALL1, writeLog(), suppressWSbeforeArg, output, returnValue),
                this, IFunction.PAGE_TYPE_REGULAR)
        adapter.visitLabel(methodEnd)
        adapter.returnValue()
        adapter.endMethod()
        return funcs
    }

    @Throws(TransformerException::class)
    private fun writeOutCallBody(bc: BytecodeContext?, body: Body?, pageType: Int): List<IFunction?>? {
        val funcs: List<IFunction?> = ArrayList<IFunction?>()
        extractFunctions(bc, body, funcs, pageType)
        writeUDFProperties(bc, funcs, pageType)

        // writeTags(bc, extractProperties(body));
        if (pageType != IFunction.PAGE_TYPE_INTERFACE) {
            var rtn = -1
            if (bc!!.returnValue()) {
                rtn = bc!!.getAdapter().newLocal(Types.OBJECT)
                bc!!.setReturn(rtn)
                // make sure we have a value
                ASMConstants.NULL(bc!!.getAdapter())
                bc!!.getAdapter().storeLocal(rtn)
            }
            BodyBase.writeOut(bc, body)
            if (rtn != -1) bc!!.getAdapter().loadLocal(rtn) else ASMConstants.NULL(bc!!.getAdapter())
        }

        // checkInterface
        if (pageType == IFunction.PAGE_TYPE_COMPONENT) {
            val adapter: GeneratorAdapter = bc!!.getAdapter()
            adapter.loadArg(1)
            adapter.loadArg(0)
            adapter.visitVarInsn(Opcodes.ALOAD, 0)
            adapter.invokeVirtual(Types.COMPONENT_IMPL, CHECK_INTERFACE)
        }
        return funcs
    }

    @Throws(TransformerException::class)
    private fun writeUDFProperties(bc: BytecodeContext?, funcs: List<IFunction?>?, pageType: Int) {
        // set items
        val it: Iterator<IFunction?> = funcs!!.iterator()
        val index = 0
        var f: IFunction?
        while (it.hasNext()) {
            f = it.next()
            f.writeOut(bc, pageType)
        }
    }

    @Throws(TransformerException::class)
    private fun writeTags(bc: BytecodeContext?, tags: List<TagOther?>?) {
        if (tags == null) return
        val it: Iterator<TagOther?> = tags.iterator()
        while (it.hasNext()) {
            it.next().writeOut(bc)
        }
    }

    /**
     * @return if it is a component
     */
    fun isComponent(): Boolean {
        return isComponent(null)
        /*
		 * TagCFObject comp = getTagCFObject(null); if(comp!=null &&
		 * comp.getTagLibTag().getTagClassName().equals("lucee.runtime.tag.Component")) return true; return
		 * false;
		 */
    }

    /**
     * @return if it is an interface
     */
    fun isInterface(): Boolean {
        return isInterface(null)
        /*
		 * TagCFObject comp = getTagCFObject(null); if(comp!=null &&
		 * comp.getTagLibTag().getTagClassName().equals("lucee.runtime.tag.Interface")) return true; return
		 * false;
		 */
    }

    fun isComponent(cio: TagCIObject?): Boolean {
        var cio: TagCIObject? = cio
        if (cio == null) cio = getTagCFObject(null)
        return cio is TagComponent
    }

    /**
     * @return if it is an interface
     */
    fun isInterface(cio: TagCIObject?): Boolean {
        var cio: TagCIObject? = cio
        if (cio == null) cio = getTagCFObject(null)
        return cio is TagInterface
    }

    fun isPage(): Boolean {
        return getTagCFObject(null) == null
    }

    /**
     * @return the lastModifed
     */
    fun getLastModifed(): Long {
        return lastModifed
    }

    @Override
    override fun addFunction(function: IFunction?): Int {
        functions.add(function)
        if (function is Function) {
            (function as Function?).setIndex(functions.size() - 1)
        }
        return functions.size() - 1
    }

    fun removeFunction(function: IFunction?) {
        functions.remove(function)
    }

    @Override
    override fun registerJavaFunctionName(functionName: String?): String? {
        var fn: String = Caster.toVariableName(functionName, null)
        if (fn == null) fn = "tmp" + HashUtil.create64BitHashAsString(functionName) // should never happen
        var count = 0
        if (javaFunctionNames == null) javaFunctionNames = HashSet<String?>()
        var tmp = fn
        while (javaFunctionNames!!.contains(tmp)) {
            tmp = fn + count++
        }
        javaFunctionNames.add(tmp)
        return tmp
    }

    fun addThread(thread: ATagThread?): Int {
        threads.add(thread)
        return threads.size() - 1
    }

    /**
     * return null if not possible to register
     *
     * @param bc
     * @param str
     * @return
     * @throws IOException
     */
    @Throws(IOException::class)
    fun registerString(bc: BytecodeContext?, str: String?): Range? {
        var append = true
        if (staticTextLocation == null) {
            if (bc!!.getPageSource() == null) return null
            val ps: PageSource = bc!!.getPageSource()
            val m: Mapping = ps.getMapping()
            staticTextLocation = m.getClassRootDirectory()
            staticTextLocation.mkdirs()
            staticTextLocation = staticTextLocation.getRealResource(ps.getClassName().replace('.', '/').toString() + ".txt")
            if (staticTextLocation.exists()) append = false else staticTextLocation.createFile(true)
            off = 0
        }
        IOUtil.write(staticTextLocation, str, CharsetUtil.UTF8, append)
        val r = Range(off, str!!.length())
        off += str!!.length()
        return r
    }

    fun getMethodCount(): Int {
        return ++methodCount
    }

    fun getSourceCode(): SourceCode? {
        return sourceCode
    }

    fun setSplitIfNecessary(splitIfNecessary: Boolean) {
        this.splitIfNecessary = splitIfNecessary
    }

    fun getSplitIfNecessary(): Boolean {
        return splitIfNecessary
    }

    fun getSupressWSbeforeArg(): Boolean {
        return suppressWSbeforeArg
    }

    fun getOutput(): Boolean {
        return output
    }

    fun returnValue(): Boolean {
        return returnValue
    }

    fun getConfig(): Config? {
        return config
    }

    fun doFinalize(bc: BytecodeContext?) {
        ExpressionUtil.visitLine(bc, getEnd())
    }

    fun registerJavaFunction(javaFunction: JavaFunction?) {
        if (javaFunctions == null) javaFunctions = ArrayList()
        javaFunctions.add(javaFunction)
    }

    fun getJavaFunctions(): List<JavaFunction?>? {
        return javaFunctions
    }

    companion object {
        val NULL: Type? = Type.getType(lucee.runtime.type.Null::class.java)
        val KEY_IMPL: Type? = Type.getType(KeyImpl::class.java)
        val KEY_CONSTANTS: Type? = Type.getType(KeyConstants::class.java)
        val KEY_INIT: Method? = Method("init", Types.COLLECTION_KEY, arrayOf<Type?>(Types.STRING))
        val KEY_INTERN: Method? = Method("intern", Types.COLLECTION_KEY, arrayOf<Type?>(Types.STRING))

        // public static ImportDefintion getInstance(String fullname,ImportDefintion defaultValue)
        private val ID_GET_INSTANCE: Method? = Method("getInstance", Types.IMPORT_DEFINITIONS, arrayOf<Type?>(Types.STRING, Types.IMPORT_DEFINITIONS))
        val STATIC_CONSTRUCTOR: Method? = Method.getMethod("void <clinit> ()V")

        // public final static Method CONSTRUCTOR = Method.getMethod("void <init> ()V");
        private val CONSTRUCTOR: Method? = Method("<init>", Types.VOID, arrayOf<Type?>())

        /*
	 * private static final Method CONSTRUCTOR_STR = new Method( "<init>", Types.VOID, new
	 * Type[]{Types.STRING}// );
	 */
        private val CONSTRUCTOR_PS: Method? = Method("<init>", Types.VOID, arrayOf<Type?>(Types.PAGE_SOURCE))

        // public static final Type STRUCT_IMPL = Type.getType(StructImpl.class);
        val INIT_STRUCT_IMPL: Method? = Method("<init>", Types.VOID, arrayOf<Type?>())

        // void call (lucee.runtime.PageContext)
        private val CALL1: Method? = Method("call", Types.OBJECT, arrayOf<Type?>(Types.PAGE_CONTEXT))

        /*
	 * / void _try () private final static Method TRY = new Method( "_try", Types.VOID, new Type[]{} );
	 */
        // int getVersion()
        private val VERSION: Method? = Method("getVersion", Types.LONG_VALUE, arrayOf<Type?>())

        // void _init()
        private val INIT_KEYS: Method? = Method("initKeys", Types.VOID, arrayOf<Type?>())
        private val SET_PAGE_SOURCE: Method? = Method("setPageSource", Types.VOID, arrayOf<Type?>(Types.PAGE_SOURCE))

        // public ImportDefintion[] getImportDefintions()
        private val GET_IMPORT_DEFINITIONS: Method? = Method("getImportDefintions", Types.IMPORT_DEFINITIONS_ARRAY, arrayOf<Type?>())
        private val GET_SUB_PAGES: Method? = Method("getSubPages", Types.CI_PAGE_ARRAY, arrayOf<Type?>())

        // long getSourceLastModified()
        private val LAST_MOD: Method? = Method("getSourceLastModified", Types.LONG_VALUE, arrayOf<Type?>())
        private val COMPILE_TIME: Method? = Method("getCompileTime", Types.LONG_VALUE, arrayOf<Type?>())
        private val HASH: Method? = Method("getHash", Types.INT_VALUE, arrayOf<Type?>())
        private val LENGTH: Method? = Method("getSourceLength", Types.LONG_VALUE, arrayOf<Type?>())
        private val GET_SUBNAME: Method? = Method("getSubname", Types.STRING, arrayOf<Type?>())
        private val USER_DEFINED_FUNCTION: Type? = Type.getType(UDF::class.java)
        private val UDF_CALL: Method? = Method("udfCall", Types.OBJECT, arrayOf<Type?>(Types.PAGE_CONTEXT, USER_DEFINED_FUNCTION, Types.INT_VALUE))
        private val THREAD_CALL: Method? = Method("threadCall", Types.VOID, arrayOf<Type?>(Types.PAGE_CONTEXT, Types.INT_VALUE))
        private val UDF_DEFAULT_VALUE: Method? = Method("udfDefaultValue", Types.OBJECT, arrayOf<Type?>(Types.PAGE_CONTEXT, Types.INT_VALUE, Types.INT_VALUE, Types.OBJECT))
        private val NEW_COMPONENT_IMPL_INSTANCE: Method? = Method("newInstance", Types.COMPONENT_IMPL, arrayOf<Type?>(Types.PAGE_CONTEXT, Types.STRING, Types.BOOLEAN_VALUE, Types.BOOLEAN_VALUE, Types.BOOLEAN_VALUE))
        private val NEW_INTERFACE_IMPL_INSTANCE: Method? = Method("newInstance", Types.INTERFACE_IMPL, arrayOf<Type?>(Types.PAGE_CONTEXT, Types.STRING, Types.BOOLEAN_VALUE))
        private val STATIC_COMPONENT_CONSTR: Method? = Method("staticConstructor", Types.VOID, arrayOf<Type?>(Types.PAGE_CONTEXT, Types.COMPONENT_IMPL))

        // MethodVisitor mv = cw.visitMethod(Opcodes.ACC_STATIC, "<clinit>", "()V", null, null);
        private val CINIT: Method? = Method("<clinit>", Types.VOID, arrayOf<Type?>())

        // public StaticStruct getStaticStruct()
        private val GET_STATIC_STRUCT: Method? = Method("getStaticStruct", Types.STATIC_STRUCT, arrayOf<Type?>())

        // void init(PageContext pc,Component Impl c) throws PageException
        private val INIT_COMPONENT3: Method? = Method("initComponent", Types.VOID, arrayOf<Type?>(Types.PAGE_CONTEXT, Types.COMPONENT_IMPL, Types.BOOLEAN_VALUE))
        private val INIT_INTERFACE: Method? = Method("initInterface", Types.VOID, arrayOf<Type?>(Types.INTERFACE_IMPL))

        // public boolean setMode(int mode) {
        private val SET_MODE: Method? = Method("setMode", Types.INT_VALUE, arrayOf<Type?>(Types.INT_VALUE))
        private val CONSTR_INTERFACE_IMPL8: Method? = Method("<init>", Types.VOID, arrayOf<Type?>(Types.PAGE_CONTEXT, Types.INTERFACE_PAGE_IMPL, Types.STRING,  // extends
                Types.STRING,  // hind
                Types.STRING,  // display
                Types.STRING,  // callpath
                Types.BOOLEAN_VALUE,  // realpath
                Types.MAP // meta
        ))
        private val CONSTR_STATIC_STRUCT: Method? = Method("<init>", Types.VOID, arrayOf<Type?>())

        // void init(PageContext pageContext,ComponentPage componentPage)
        private val INIT_COMPONENT: Method? = Method("init", Types.VOID, arrayOf<Type?>(Types.PAGE_CONTEXT, Types.COMPONENT_PAGE_IMPL, Types.BOOLEAN_VALUE))
        private val CHECK_INTERFACE: Method? = Method("checkInterface", Types.VOID, arrayOf<Type?>(Types.PAGE_CONTEXT, Types.COMPONENT_PAGE_IMPL))

        // boolean getOutput()
        private val GET_OUTPUT: Method? = Method("getOutput", Types.BOOLEAN_VALUE, arrayOf<Type?>())
        private val PUSH_BODY: Method? = Method("pushBody", Types.BODY_CONTENT, arrayOf<Type?>())

        /*
	 * / boolean setSilent() private static final Method SET_SILENT = new Method( "setSilent",
	 * Types.BOOLEAN_VALUE, new Type[]{} );
	 */
        // Scope beforeCall(PageContext pc)
        private val BEFORE_CALL: Method? = Method("beforeCall", Types.VARIABLES, arrayOf<Type?>(Types.PAGE_CONTEXT))
        private val TO_PAGE_EXCEPTION: Method? = Method("toPageException", Types.PAGE_EXCEPTION, arrayOf<Type?>(Types.THROWABLE))

        // void afterCall(PageContext pc, Scope parent)
        private val AFTER_CALL: Method? = Method("afterConstructor", Types.VOID, arrayOf<Type?>(Types.PAGE_CONTEXT, Types.VARIABLES))
        private val AFTER_STATIC_CONSTR: Method? = Method("afterStaticConstructor", Types.VOID, arrayOf<Type?>(Types.PAGE_CONTEXT, Types.VARIABLES))
        private val BEFORE_STATIC_CONSTR: Method? = Method("beforeStaticConstructor", Types.VARIABLES, arrayOf<Type?>(Types.PAGE_CONTEXT))
        private val CONSTRUCTOR_EMPTY: org.objectweb.asm.commons.Method? = Method("<init>", Types.VOID, arrayOf<Type?>())

        // Component Impl(ComponentPage,boolean, String, String, String, String) WS==With Style
        private val CONSTR_COMPONENT_IMPL15: Method? = Method("<init>", Types.VOID, arrayOf<Type?>(Types.COMPONENT_PAGE_IMPL, Types.BOOLEAN, Types.BOOLEAN_VALUE, Types.STRING, Types.STRING, Types.STRING, Types.STRING, Types.STRING, Types.BOOLEAN_VALUE,
                Types.STRING, Types.BOOLEAN_VALUE, Types.BOOLEAN_VALUE, Types.INT_VALUE, Types.BOOLEAN_VALUE, Types.STRUCT_IMPL))
        private val SET_EL: Method? = Method("setEL", Types.OBJECT, arrayOf<Type?>(Types.COLLECTION_KEY, Types.OBJECT))
        val UNDEFINED_SCOPE: Method? = Method("us", Types.UNDEFINED, arrayOf<Type?>())
        private val FLUSH_AND_POP: Method? = Method("flushAndPop", Types.VOID, arrayOf<Type?>(Types.PAGE_CONTEXT, Types.BODY_CONTENT))
        private val CLEAR_AND_POP: Method? = Method("clearAndPop", Types.VOID, arrayOf<Type?>(Types.PAGE_CONTEXT, Types.BODY_CONTENT))
        const val CF = 207.toByte()
        const val _33 = 51.toByte()

        // private static final boolean ADD_C33 = false;
        // private static final String SUB_CALL_UDF = "udfCall";
        private val SUB_CALL_UDF: String? = "_"
        private const val DEFAULT_VALUE = 3

        /*
	 * private static Function[] extractFunctions(List<Data> udfProperties) { Function[] functions = new
	 * Function[udfProperties.size()]; int index = 0; for (Data d: udfProperties) { functions[index++] =
	 * d.function; } return functions; }
	 */
        fun createSubClass(name: String?, subName: String?, dialect: Int): String? {
            // TODO handle special characters
            var name = name
            var subName = subName
            if (!StringUtil.isEmpty(subName)) {
                val suffix: String = if (dialect == CFMLEngine.DIALECT_CFML) Constants.CFML_CLASS_SUFFIX else Constants.LUCEE_CLASS_SUFFIX
                subName = subName.toLowerCase()
                if (name.endsWith(suffix)) name = name.substring(0, name!!.length() - 3).toString() + "$" + subName + suffix else name += "$$subName"
            }
            return name
        }

        @Throws(TransformerException::class)
        fun registerFields(bc: BytecodeContext?, keys: List<LitString?>?) {
            // if(keys.size()==0) return;
            val ga: GeneratorAdapter = bc!!.getAdapter()
            val fv: FieldVisitor = bc!!.getClassWriter().visitField(Opcodes.ACC_PRIVATE, "keys", Types.COLLECTION_KEY_ARRAY.toString(), null, null)
            fv.visitEnd()
            var index = 0
            var value: LitString
            val it: Iterator<LitString?> = keys!!.iterator()
            ga.visitVarInsn(Opcodes.ALOAD, 0)
            ga.push(keys.size())
            ga.newArray(Types.COLLECTION_KEY)
            while (it.hasNext()) {
                value = it.next()
                ga.dup()
                ga.push(index++)
                // value.setExternalize(false);
                ExpressionUtil.writeOutSilent(value, bc, Expression.MODE_REF)
                ga.invokeStatic(KEY_IMPL, KEY_INTERN)
                ga.visitInsn(Opcodes.AASTORE)
            }
            ga.visitFieldInsn(Opcodes.PUTFIELD, bc!!.getClassName(), "keys", Types.COLLECTION_KEY_ARRAY.toString())
        }

        fun hasMetaDataStruct(attrs: Map?, meta: Map?): Boolean {
            return if ((attrs == null || attrs.size() === 0) && (meta == null || meta.size() === 0)) {
                false
            } else true
        }

        @Throws(TransformerException::class)
        fun createMetaDataStruct(bc: BytecodeContext?, attrs: Map?, meta: Map?) {
            val adapter: GeneratorAdapter = bc!!.getAdapter()
            if ((attrs == null || attrs.size() === 0) && (meta == null || meta.size() === 0)) {
                ASMConstants.NULL(bc!!.getAdapter())
                bc!!.getAdapter().cast(Types.OBJECT, Types.STRUCT_IMPL)
                return
            }
            val sct: Int = adapter.newLocal(Types.STRUCT_IMPL)
            adapter.newInstance(Types.STRUCT_IMPL)
            adapter.dup()
            adapter.invokeConstructor(Types.STRUCT_IMPL, INIT_STRUCT_IMPL)
            adapter.storeLocal(sct)
            if (meta != null) {
                _createMetaDataStruct(bc, adapter, sct, meta)
            }
            if (attrs != null) {
                _createMetaDataStruct(bc, adapter, sct, attrs)
            }
            adapter.loadLocal(sct)
        }

        @Throws(TransformerException::class)
        private fun _createMetaDataStruct(bc: BytecodeContext?, adapter: GeneratorAdapter?, sct: Int, attrs: Map?) {
            var attr: Attribute
            val it: Iterator = attrs.entrySet().iterator()
            var entry: Entry
            while (it.hasNext()) {
                entry = it.next()
                attr = entry.getValue() as Attribute
                adapter.loadLocal(sct)

                // adapter.push(attr.getName());
                bc!!.getFactory().registerKey(bc, bc!!.getFactory().createLitString(attr.getName()), false)
                if (attr.getValue() is Literal) ExpressionUtil.writeOutSilent(attr.getValue(), bc, Expression.MODE_REF) else adapter.push("[runtime expression]")
                adapter.invokeVirtual(Types.STRUCT_IMPL, SET_EL)
                adapter.pop()
            }
        }

        @Throws(TransformerException::class)
        private fun getImports(list: List<String?>?, body: Body?) {
            if (ASMUtil.isEmpty(body)) return
            var stat: Statement
            val stats: List<Statement?> = body!!.getStatements()
            var len: Int = stats.size()
            var i = 0
            while (i < len) {
                stat = stats[i]

                // IFunction
                if (stat is TagImport && !StringUtil.isEmpty((stat as TagImport).getPath(), true)) {
                    val id: ImportDefintion = ImportDefintionImpl.getInstance((stat as TagImport).getPath(), null)
                    if (id != null && !list!!.contains(id.toString()) && !list.contains(id.getPackage().toString() + ".*")) {
                        list.add(id.toString())
                    }
                    stats.remove(i)
                    len--
                    i--
                } else if (stat is HasBody) getImports(list, (stat as HasBody).getBody()) else if (stat is HasBodies) {
                    val bodies: Array<Body?> = (stat as HasBodies).getBodies()
                    for (y in bodies.indices) {
                        getImports(list, bodies[y])
                    }
                }
                i++
            }
        }

        @Throws(TransformerException::class)
        private fun extractFunctions(bc: BytecodeContext?, body: Body?, funcs: List<IFunction?>?, pageType: Int) {
            if (ASMUtil.isEmpty(body)) return
            var stat: Statement
            val stats: List<Statement?> = body!!.getStatements()
            var len: Int = stats.size()
            var i = 0
            while (i < len) {
                stat = stats[i]

                // IFunction
                if (stat is IFunction) {
                    funcs.add(stat as IFunction)
                    stats.remove(i)
                    len--
                    i--
                } else if (stat is HasBody) {
                    extractFunctions(bc, (stat as HasBody).getBody(), funcs, pageType)
                } else if (stat is HasBodies) {
                    val bodies: Array<Body?> = (stat as HasBodies).getBodies()
                    for (y in bodies.indices) {
                        extractFunctions(bc, bodies[y], funcs, pageType)
                    }
                }
                i++
            }
        }

        @Throws(TransformerException::class)
        private fun extractProperties(body: Body?): List<TagOther?>? {
            if (ASMUtil.isEmpty(body)) return null
            var stat: Statement
            var properties: List<TagOther?>? = null
            val stats: List<Statement?> = body!!.getStatements()
            for (i in stats.size() - 1 downTo 0) {
                stat = stats[i]
                if (stat is TagOther) {
                    val tlt: TagLibTag = (stat as TagOther).getTagLibTag()
                    if (Property::class.java.getName().equals(tlt.getTagClassDefinition().getClassName())) {
                        if (properties == null) properties = ArrayList<TagOther?>()
                        properties.add(0, stat as TagOther)
                        stats.remove(i)
                    }
                }
            }
            return properties
        }

        fun setSourceLastModified(barr: ByteArray?, lastModified: Long): ByteArray? {
            val cr = ClassReader(barr)
            val cw: ClassWriter = ASMUtil.getClassWriter()
            val ca: ClassVisitor = SourceLastModifiedClassAdapter(cw, lastModified)
            cr.accept(ca, 0)
            return cw.toByteArray()
        }

        private fun toArray(funcs: List<IFunction?>?): Array<Function?>? {
            val arr: Array<Function?> = arrayOfNulls<Function?>(funcs!!.size())
            var index = 0
            for (f in funcs!!) {
                arr[index++] = f as Function?
            }
            return arr
        }
    }

    /**
     * @param factory
     * @param config
     * @param sc SourceCode for this Page
     * @param className name of the class produced (pattern: org.whatever.Susi)
     * @param tc
     * @param version
     * @param lastModifed
     * @param writeLog
     * @param suppressWSbeforeArg
     * @param dotNotationUpperCase
     */
    init {
        _comp = tc
        this.version = version
        this.lastModifed = lastModifed
        length = if (sc is PageSourceCode) (sc as PageSourceCode?).getPageSource().getPhyscalFile().length() else 0
        _writeLog = writeLog
        this.suppressWSbeforeArg = suppressWSbeforeArg
        this.returnValue = returnValue
        this.ignoreScopes = ignoreScopes
        this.output = output
        // this.pageSource=ps;
        this.config = config
        sourceCode = sc
        hash = sc.hashCode()
    }
}

internal class SourceLastModifiedClassAdapter(cw: ClassWriter?, private val lastModified: Long) : ClassVisitor(Opcodes.ASM4, cw) {
    @Override
    fun visitMethod(access: Int, name: String?, desc: String?, signature: String?, exceptions: Array<String?>?): MethodVisitor? {
        if (!name!!.equals("getSourceLastModified")) return super.visitMethod(access, name, desc, signature, exceptions)
        val mv: MethodVisitor = cv.visitMethod(access, name, desc, signature, exceptions)
        mv.visitCode()
        mv.visitLdcInsn(Long.valueOf(lastModified))
        mv.visitInsn(Opcodes.LRETURN)
        mv.visitEnd()
        return mv
    }
}