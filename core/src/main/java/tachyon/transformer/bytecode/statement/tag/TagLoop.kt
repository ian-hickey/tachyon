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
package tachyon.transformer.bytecode.statement.tag

import org.objectweb.asm.Label

class TagLoop(f: Factory?, start: Position?, end: Position?) : TagGroup(f, start, end), FlowControlBreak, FlowControlContinue {
    private var type = 0
    private var loopVisitor: LoopVisitor? = null
    private var label: String? = null
    fun setType(type: Int) {
        this.type = type
    }

    /**
     *
     * @see tachyon.transformer.bytecode.statement.tag.TagBase._writeOut
     */
    @Override
    @Throws(TransformerException::class)
    override fun _writeOut(bc: BytecodeContext?) {
        val old: Boolean
        when (type) {
            TYPE_STRUCT, TYPE_COLLECTION -> writeOutTypeCollection(bc)
            TYPE_CONDITION -> writeOutTypeCondition(bc)
            TYPE_FILE -> writeOutTypeFile(bc)
            TYPE_FROM_TO -> writeOutTypeFromTo(bc)
            TYPE_LIST -> writeOutTypeListArray(bc, false)
            TYPE_ARRAY -> writeOutTypeListArray(bc, true)
            TYPE_QUERY -> {
                old = bc.changeDoSubFunctions(false)
                TagGroupUtil.writeOutTypeQuery(this, bc)
                bc.changeDoSubFunctions(old)
            }
            TYPE_GROUP -> {
                old = bc.changeDoSubFunctions(false)
                TagGroupUtil.writeOutTypeGroup(this, bc)
                bc.changeDoSubFunctions(old)
            }
            TYPE_INNER_GROUP -> {
                old = bc.changeDoSubFunctions(false)
                TagGroupUtil.writeOutTypeInnerGroup(this, bc)
                bc.changeDoSubFunctions(old)
            }
            TYPE_INNER_QUERY -> {
                old = bc.changeDoSubFunctions(false)
                TagGroupUtil.writeOutTypeInnerQuery(this, bc)
                bc.changeDoSubFunctions(old)
            }
            TYPE_TIMES -> writeOutTypeTimes(bc)
            TYPE_NOTHING -> {
                val a: GeneratorAdapter = bc.getAdapter()
                val dwv = DoWhileVisitor()
                setLoopVisitor(dwv)
                dwv.visitBeginBody(a)
                getBody().writeOut(bc)
                dwv.visitEndBodyBeginExpr(a)
                a.push(false)
                dwv.visitEndExpr(a)
            }
            else -> throw TransformerException(bc, "invalid type", getStart())
        }
    }

    @Throws(TransformerException::class)
    private fun writeOutTypeTimes(bc: BytecodeContext?) {
        val f: Factory = bc.getFactory()
        val adapter: GeneratorAdapter = bc.getAdapter()
        val times: Int = adapter.newLocal(Types.INT_VALUE)
        val timesExpr: ExprInt = f.toExprInt(getAttribute("times")!!.getValue())
        ExpressionUtil.writeOutSilent(timesExpr, bc, Expression.MODE_VALUE)
        adapter.storeLocal(times)
        val fiv = ForVisitor()
        fiv.visitBegin(adapter, 1, false)
        getBody().writeOut(bc)
        fiv.visitEnd(bc, times, true, getStart())
    }

    /**
     * write out collection loop
     *
     * @param adapter
     * @throws TemplateException
     */
    @Throws(TransformerException::class)
    private fun writeOutTypeCollection(bc: BytecodeContext?) {
        val adapter: GeneratorAdapter = bc.getAdapter()

        // VariableReference item=VariableInterpreter.getVariableReference(pc,index);
        var index = -1
        var attrIndex: Attribute? = getAttribute("index")
        if (attrIndex == null) attrIndex = getAttribute("key")
        if (attrIndex != null) {
            index = adapter.newLocal(Types.VARIABLE_REFERENCE)
            adapter.loadArg(0)
            attrIndex.getValue()!!.writeOut(bc, Expression.MODE_REF)
            adapter.invokeStatic(Types.VARIABLE_INTERPRETER, GET_VARIABLE_REFERENCE)
            adapter.storeLocal(index)
        }

        // VariableReference item=VariableInterpreter.getVariableReference(pc,item);
        var item = -1
        var attrItem: Attribute? = getAttribute("item")
        if (attrItem == null) attrItem = getAttribute("value")
        if (attrItem != null) {
            item = adapter.newLocal(Types.VARIABLE_REFERENCE)
            adapter.loadArg(0)
            attrItem.getValue()!!.writeOut(bc, Expression.MODE_REF)
            adapter.invokeStatic(Types.VARIABLE_INTERPRETER, GET_VARIABLE_REFERENCE)
            adapter.storeLocal(item)
        }
        val hasIndexAndItem = index != -1 && item != -1
        val hasItem = item != -1
        val whileVisitor = WhileVisitor()
        loopVisitor = whileVisitor
        // java.util.Iterator it=Caster.toIterator(@collection');
        val it: Int = adapter.newLocal(Types.ITERATOR)
        var coll: Attribute? = getAttribute("struct")
        if (coll == null) coll = getAttribute("collection")
        coll!!.getValue()!!.writeOut(bc, Expression.MODE_REF)

        // item and index
        var entry = -1
        if (hasIndexAndItem) {
            entry = adapter.newLocal(Types.MAP_ENTRY)
            // Caster.toCollection(collection)
            adapter.invokeStatic(Types.CASTER, Methods_Caster.TO_COLLECTION)
            // coll.entryIterator();
            adapter.invokeInterface(Types.COLLECTION, ENTRY_ITERATOR)
        } else {
            // if(hasItem) adapter.invokeStatic(ForEach.FOR_EACH_UTIL,ForEach.FOR_EACH);
            // else
            adapter.invokeStatic(ForEach.FOR_EACH_UTIL, ForEach.LOOP_COLLECTION)
        }
        adapter.storeLocal(it)

        // while(it.hasNext()) {
        whileVisitor.visitBeforeExpression(bc)
        adapter.loadLocal(it)
        adapter.invokeInterface(Types.ITERATOR, HAS_NEXT)
        whileVisitor.visitAfterExpressionBeforeBody(bc)
        if (hasIndexAndItem) {
            // entry=it.next();
            adapter.loadLocal(it)
            adapter.invokeInterface(Types.ITERATOR, NEXT)
            adapter.storeLocal(entry)

            // keyRef.set(pc,entry.getKey())
            adapter.loadLocal(index)
            adapter.loadArg(0)
            adapter.loadLocal(entry)
            adapter.invokeInterface(Types.MAP_ENTRY, GET_KEY)
            adapter.invokeStatic(Types.CASTER, Methods.METHOD_TO_STRING)
            adapter.invokeVirtual(Types.VARIABLE_REFERENCE, SET)
            adapter.pop()

            // valueRef.set(pc,entry.getKey())
            adapter.loadLocal(item)
            adapter.loadArg(0)
            adapter.loadLocal(entry)
            adapter.invokeInterface(Types.MAP_ENTRY, GET_VALUE)
            adapter.invokeVirtual(Types.VARIABLE_REFERENCE, SET)
            adapter.pop()
        } else {
            if (index == -1) adapter.loadLocal(item) else adapter.loadLocal(index)
            adapter.loadArg(0)
            adapter.loadLocal(it)
            adapter.invokeInterface(Types.ITERATOR, NEXT)
            adapter.invokeVirtual(Types.VARIABLE_REFERENCE, SET)
            adapter.pop()
        }
        getBody().writeOut(bc)
        whileVisitor.visitAfterBody(bc, getEnd())

        // Reset
        adapter.loadLocal(it)
        adapter.invokeStatic(ForEach.FOR_EACH_UTIL, ForEach.RESET)
    }

    /**
     * write out condition loop
     *
     * @param adapter
     * @throws TemplateException
     */
    @Throws(TransformerException::class)
    private fun writeOutTypeCondition(bc: BytecodeContext?) {
        val whileVisitor = WhileVisitor()
        loopVisitor = whileVisitor
        whileVisitor.visitBeforeExpression(bc)
        bc.getFactory().toExprBoolean(getAttribute("condition")!!.getValue()).writeOut(bc, Expression.MODE_VALUE)
        whileVisitor.visitAfterExpressionBeforeBody(bc)
        getBody().writeOut(bc)
        whileVisitor.visitAfterBody(bc, getEnd())
    }

    /**
     * write out file loop
     *
     * @param adapter
     * @throws TemplateException
     */
    @Throws(TransformerException::class)
    private fun writeOutTypeFile(bc: BytecodeContext?) {
        val whileVisitor = WhileVisitor()
        loopVisitor = whileVisitor
        val adapter: GeneratorAdapter = bc.getAdapter()

        // charset=@charset
        val charset: Int = adapter.newLocal(Types.STRING)
        val attrCharset: Attribute = getAttribute("charset")
        if (attrCharset == null) adapter.visitInsn(Opcodes.ACONST_NULL) else attrCharset.getValue()!!.writeOut(bc, Expression.MODE_REF)
        adapter.storeLocal(charset)

        // startline=@startline
        val startline: Int = adapter.newLocal(Types.INT_VALUE)
        var attrStartLine: Attribute? = getAttribute("startline")
        if (attrStartLine == null) attrStartLine = getAttribute("from") // CF8
        if (attrStartLine == null) adapter.push(1) else {
            attrStartLine.getValue()!!.writeOut(bc, Expression.MODE_VALUE)
            adapter.visitInsn(Opcodes.D2I)
        }
        adapter.storeLocal(startline)

        // endline=@endline
        val endline: Int = adapter.newLocal(Types.INT_VALUE)
        var attrEndLine: Attribute? = getAttribute("endline")
        if (attrEndLine == null) attrEndLine = getAttribute("to")
        if (attrEndLine == null) adapter.push(-1) else {
            attrEndLine.getValue()!!.writeOut(bc, Expression.MODE_VALUE)
            adapter.visitInsn(Opcodes.D2I)
        }
        adapter.storeLocal(endline)

        // VariableReference index=VariableInterpreter.getVariableReference(pc,@index);
        var index = -1
        var item = -1

        // item
        val attrItem: Attribute = getAttribute("item")
        if (attrItem != null) {
            item = adapter.newLocal(Types.VARIABLE_REFERENCE)
            adapter.loadArg(0)
            attrItem.getValue()!!.writeOut(bc, Expression.MODE_REF)
            adapter.invokeStatic(Types.VARIABLE_INTERPRETER, GET_VARIABLE_REFERENCE)
            adapter.storeLocal(item)
        }

        // index
        val attrIndex: Attribute = getAttribute("index")
        if (attrIndex != null) {
            index = adapter.newLocal(Types.VARIABLE_REFERENCE)
            adapter.loadArg(0)
            attrIndex.getValue()!!.writeOut(bc, Expression.MODE_REF)
            adapter.invokeStatic(Types.VARIABLE_INTERPRETER, GET_VARIABLE_REFERENCE)
            adapter.storeLocal(index)
        }

        // java.io.File file=FileUtil.toResourceExisting(pc,@file);
        val resource: Int = adapter.newLocal(Types.RESOURCE)
        adapter.loadArg(0)
        getAttribute("file")!!.getValue()!!.writeOut(bc, Expression.MODE_REF)
        adapter.invokeStatic(RESOURCE_UTIL, TO_RESOURCE_EXISTING)
        adapter.storeLocal(resource)

        // pc.getConfig().getSecurityManager().checkFileLocation(resource);
        adapter.loadArg(0)
        adapter.invokeVirtual(Types.PAGE_CONTEXT, GET_CONFIG)
        adapter.invokeInterface(Types.CONFIG_WEB, GET_SECURITY_MANAGER)
        adapter.loadLocal(resource)
        adapter.invokeInterface(Types.SECURITY_MANAGER, CHECK_FILE_LOCATION)

        // char[] carr=new char[characters];
        val attr: Attribute = getAttribute("characters")
        var carr = -1
        if (attr != null) {
            carr = adapter.newLocal(Types.CHAR_ARRAY)
            attr.getValue()!!.writeOut(bc, Expression.MODE_VALUE)
            adapter.cast(Types.DOUBLE_VALUE, Types.INT_VALUE)
            adapter.newArray(Types.CHAR)
            adapter.storeLocal(carr)
        }

        // BufferedReader reader = IOUtil.getBufferedReader(resource,charset);
        val br: Int = adapter.newLocal(Types.BUFFERED_READER)
        adapter.loadLocal(resource)
        adapter.loadLocal(charset)
        adapter.invokeStatic(IO_UTIL, GET_BUFFERED_READER)
        adapter.storeLocal(br)

        // String line;
        val line: Int = adapter.newLocal(Types.STRING)

        // int count=0;
        val count: Int = adapter.newLocal(Types.INT_VALUE)
        adapter.push(0)
        adapter.storeLocal(count)
        val tfv = TryFinallyVisitor(object : OnFinally() {
            @Override
            fun _writeOut(bc: BytecodeContext?) {
                bc.getAdapter().loadLocal(br)
                bc.getAdapter().invokeStatic(IO_UTIL, CLOSE_EL)
            }
        }, null)
        // TryFinallyVisitor tcfv=new TryFinallyVisitor();

        // try
        tfv.visitTryBegin(bc)
        // tcfv.visitTryBegin(bc);
        // while((line=br.readLine())!=null) {
        // WhileVisitor wv=new WhileVisitor();
        whileVisitor.visitBeforeExpression(bc)
        val dv = DecisionObjectVisitor()
        dv.visitBegin()
        if (attr != null) {
            // IOUtil.read(bufferedreader,12)
            adapter.loadLocal(br)
            adapter.loadLocal(carr)
            adapter.arrayLength()
            adapter.invokeStatic(Types.IOUTIL, READ)
        } else {
            // br.readLine()
            adapter.loadLocal(br)
            adapter.invokeVirtual(Types.BUFFERED_READER, READ_LINE)
        }
        adapter.dup()
        adapter.storeLocal(line)
        dv.visitNEQ()
        adapter.visitInsn(Opcodes.ACONST_NULL)
        dv.visitEnd(bc)
        whileVisitor.visitAfterExpressionBeforeBody(bc)
        // if(++count < startLine) continue;
        val dv2 = DecisionIntVisitor()
        dv2.visitBegin()
        adapter.iinc(count, 1)
        adapter.loadLocal(count)
        dv2.visitLT()
        adapter.loadLocal(startline)
        dv2.visitEnd(bc)
        val end = Label()
        adapter.ifZCmp(Opcodes.IFEQ, end)
        whileVisitor.visitContinue(bc)
        adapter.visitLabel(end)

        // if(endLine!=-1 && count > endLine) break;
        val div = DecisionIntVisitor()
        div.visitBegin()
        adapter.loadLocal(endline)
        div.visitNEQ()
        adapter.push(-1)
        div.visitEnd(bc)
        val end2 = Label()
        adapter.ifZCmp(Opcodes.IFEQ, end2)
        val div2 = DecisionIntVisitor()
        div2.visitBegin()
        adapter.loadLocal(count)
        div2.visitGT()
        adapter.loadLocal(endline)
        div2.visitEnd(bc)
        val end3 = Label()
        adapter.ifZCmp(Opcodes.IFEQ, end3)
        whileVisitor.visitBreak(bc)
        adapter.visitLabel(end3)
        adapter.visitLabel(end2)

        // index and item
        if (index != -1 && item != -1) {
            // index.set(pc,line);
            adapter.loadLocal(index)
            adapter.loadArg(0)
            adapter.loadLocal(count)
            adapter.cast(Types.INT_VALUE, Types.DOUBLE_VALUE)
            adapter.invokeStatic(Types.CASTER, Methods.METHOD_TO_DOUBLE_FROM_DOUBLE_VALUE)
            adapter.invokeVirtual(Types.VARIABLE_REFERENCE, SET)
            adapter.pop()

            // item.set(pc,line);
            adapter.loadLocal(item)
            adapter.loadArg(0)
            adapter.loadLocal(line)
            adapter.invokeVirtual(Types.VARIABLE_REFERENCE, SET)
            adapter.pop()
        } else if (index != -1) {
            // index.set(pc,line);
            adapter.loadLocal(index)
            adapter.loadArg(0)
            adapter.loadLocal(line)
            adapter.invokeVirtual(Types.VARIABLE_REFERENCE, SET)
            adapter.pop()
        } else {
            // item.set(pc,line);
            adapter.loadLocal(item)
            adapter.loadArg(0)
            adapter.loadLocal(line)
            adapter.invokeVirtual(Types.VARIABLE_REFERENCE, SET)
            adapter.pop()
        }
        getBody().writeOut(bc)
        whileVisitor.visitAfterBody(bc, getEnd())
        tfv.visitTryEnd(bc)
    }

    /**
     * write out index loop
     *
     * @param adapter
     * @throws TemplateException
     */
    @Throws(TransformerException::class)
    private fun writeOutTypeFromTo(bc: BytecodeContext?) {
        val forDoubleVisitor = ForDoubleVisitor()
        loopVisitor = forDoubleVisitor
        val adapter: GeneratorAdapter = bc.getAdapter()

        // int from=(int)@from;
        val from: Int = adapter.newLocal(Types.DOUBLE_VALUE)
        ExpressionUtil.writeOutSilent(getAttribute("from")!!.getValue(), bc, Expression.MODE_VALUE)
        adapter.storeLocal(from)

        // int to=(int)@to;
        val to: Int = adapter.newLocal(Types.DOUBLE_VALUE)
        ExpressionUtil.writeOutSilent(getAttribute("to")!!.getValue(), bc, Expression.MODE_VALUE)
        adapter.storeLocal(to)

        // int step=(int)@step;
        val step: Int = adapter.newLocal(Types.DOUBLE_VALUE)
        val attrStep: Attribute = getAttribute("step")
        if (attrStep != null) {
            ExpressionUtil.writeOutSilent(attrStep.getValue(), bc, Expression.MODE_VALUE)
        } else {
            adapter.push(1.0)
        }
        adapter.storeLocal(step)

        // boolean dirPlus=(step > 0);
        val dirPlus: Int = adapter.newLocal(Types.BOOLEAN_VALUE)
        var div: DecisionDoubleVisitor? = DecisionDoubleVisitor()
        div.visitBegin()
        adapter.loadLocal(step)
        div.visitGT()
        adapter.push(0.0)
        div.visitEnd(bc)
        adapter.storeLocal(dirPlus)

        // if(step!=0) {
        div = DecisionDoubleVisitor()
        div.visitBegin()
        adapter.loadLocal(step)
        div.visitNEQ()
        adapter.push(0.0)
        div.visitEnd(bc)
        val ifEnd = Label()
        adapter.ifZCmp(Opcodes.IFEQ, ifEnd)

        // VariableReference index>=VariableInterpreter.getVariableReference(pc,@index));
        val index: Int = adapter.newLocal(Types.VARIABLE_REFERENCE)
        adapter.loadArg(0)
        var attr: Attribute? = getAttribute("index")
        if (attr == null) attr = getAttribute("item")
        ExpressionUtil.writeOutSilent(attr!!.getValue(), bc, Expression.MODE_REF)
        adapter.invokeStatic(Types.VARIABLE_INTERPRETER, GET_VARIABLE_REFERENCE)
        adapter.storeLocal(index)

        // index.set(from);
        adapter.loadLocal(index)
        adapter.loadLocal(from)
        adapter.invokeVirtual(Types.VARIABLE_REFERENCE, SET_DOUBLE)

        // for

        // int i=forConditionVisitor.visitBeforeExpression(adapter,from,step,true);

        // init
        adapter.visitLabel(forDoubleVisitor.beforeInit)
        forDoubleVisitor.forInit(adapter, from, true)
        adapter.goTo(forDoubleVisitor.beforeExpr)

        // update
        adapter.visitLabel(forDoubleVisitor.beforeUpdate)
        adapter.loadLocal(index)
        // forConditionVisitor.forUpdate(adapter, step, true);
        adapter.visitVarInsn(Opcodes.DLOAD, forDoubleVisitor.i)
        adapter.loadLocal(step)
        adapter.visitInsn(Opcodes.DADD)
        adapter.visitInsn(Opcodes.DUP2)
        adapter.visitVarInsn(Opcodes.DSTORE, forDoubleVisitor.i)
        adapter.invokeVirtual(Types.VARIABLE_REFERENCE, SET_DOUBLE)

        // expression
        adapter.visitLabel(forDoubleVisitor.beforeExpr)
        val i: Int = forDoubleVisitor.i
        adapter.loadLocal(dirPlus)
        val l1 = Label()
        adapter.visitJumpInsn(Opcodes.IFEQ, l1)
        div = DecisionDoubleVisitor()
        div.visitBegin()
        adapter.visitVarInsn(Opcodes.DLOAD, i)
        div.visitLTE()
        adapter.loadLocal(to)
        div.visitEnd(bc)
        val l2 = Label()
        adapter.visitJumpInsn(Opcodes.GOTO, l2)
        adapter.visitLabel(l1)
        div = DecisionDoubleVisitor()
        div.visitBegin()
        adapter.visitVarInsn(Opcodes.DLOAD, i)
        div.visitGTE()
        adapter.loadLocal(to)
        div.visitEnd(bc)
        adapter.visitLabel(l2)
        forDoubleVisitor.visitAfterExpressionBeginBody(adapter)

        // adapter.loadLocal(index);
        // adapter.visitVarInsn(Opcodes.DLOAD, i);
        // adapter.invokeVirtual(Types.VARIABLE_REFERENCE, SET_DOUBLE);
        getBody().writeOut(bc)
        forDoubleVisitor.visitEndBody(bc, getEnd())

        ////// set i after usage
        // adapter.loadLocal(index);
        // adapter.visitVarInsn(Opcodes.DLOAD, i);
        // adapter.invokeVirtual(Types.VARIABLE_REFERENCE, SET_DOUBLE);
        adapter.visitLabel(ifEnd)
    }

    /**
     * write out list loop
     *
     * @param adapter
     * @throws TemplateException
     */
    @Throws(TransformerException::class)
    private fun writeOutTypeListArray(bc: BytecodeContext?, isArray: Boolean) {
        val forVisitor = ForVisitor()
        loopVisitor = forVisitor
        val adapter: GeneratorAdapter = bc.getAdapter()

        // List.listToArrayRemoveEmpty("", 'c')
        val array: Int = adapter.newLocal(Types.ARRAY)
        val len: Int = adapter.newLocal(Types.INT_VALUE)
        if (isArray) {
            getAttribute("array")!!.getValue()!!.writeOut(bc, Expression.MODE_REF)
        } else {
            // array=List.listToArrayRemoveEmpty(list, delimter)
            getAttribute("list")!!.getValue()!!.writeOut(bc, Expression.MODE_REF)
            if (containsAttribute("delimiters")) {
                getAttribute("delimiters")!!.getValue()!!.writeOut(bc, Expression.MODE_REF)
                adapter.invokeStatic(Types.LIST_UTIL, LIST_TO_ARRAY_REMOVE_EMPTY_SS)
            } else {
                adapter.visitIntInsn(Opcodes.BIPUSH, 44) // ','
                // adapter.push(',');
                adapter.invokeStatic(Types.LIST_UTIL, LIST_TO_ARRAY_REMOVE_EMPTY_SC)
            }
        }
        adapter.storeLocal(array)

        // int len=array.size();
        adapter.loadLocal(array)
        adapter.invokeInterface(Types.ARRAY, SIZE)
        adapter.storeLocal(len)

        // VariableInterpreter.getVariableReference(pc,Caster.toString(index));
        val attrIndex: Attribute = getAttribute("index")
        var index = -1
        if (attrIndex != null) {
            index = adapter.newLocal(Types.VARIABLE_REFERENCE)
            adapter.loadArg(0)
            attrIndex.getValue()!!.writeOut(bc, Expression.MODE_REF)
            adapter.invokeStatic(Types.VARIABLE_INTERPRETER, GET_VARIABLE_REFERENCE)
            adapter.storeLocal(index)
        }

        // VariableInterpreter.getVariableReference(pc,Caster.toString(item));
        val attrItem: Attribute = getAttribute("item")
        var item = -1
        if (attrItem != null) {
            item = adapter.newLocal(Types.VARIABLE_REFERENCE)
            adapter.loadArg(0)
            attrItem.getValue()!!.writeOut(bc, Expression.MODE_REF)
            adapter.invokeStatic(Types.VARIABLE_INTERPRETER, GET_VARIABLE_REFERENCE)
            adapter.storeLocal(item)
        }
        var obj = 0
        if (isArray) obj = adapter.newLocal(Types.OBJECT)

        // for(int i=1;i<=len;i++) {
        val i: Int = forVisitor.visitBegin(adapter, 1, false)
        // index.set(pc, list.get(i));
        if (isArray) {

            // value
            adapter.loadLocal(array)
            adapter.visitVarInsn(Opcodes.ILOAD, i)
            ASMConstants.NULL(adapter)
            adapter.invokeInterface(Types.ARRAY, GET)
            adapter.dup()
            adapter.storeLocal(obj)
            val endIf = Label()
            // adapter.loadLocal(obj);
            adapter.visitJumpInsn(Opcodes.IFNONNULL, endIf)
            adapter.goTo(forVisitor.getContinueLabel())
            adapter.visitLabel(endIf)
            if (item == -1) adapter.loadLocal(index) else adapter.loadLocal(item)
            adapter.loadArg(0)
            adapter.loadLocal(obj)
        } else {
            if (item == -1) adapter.loadLocal(index) else adapter.loadLocal(item)
            adapter.loadArg(0)
            adapter.loadLocal(array)
            adapter.visitVarInsn(Opcodes.ILOAD, i)
            adapter.invokeInterface(Types.ARRAY, GETE)
        }
        adapter.invokeVirtual(Types.VARIABLE_REFERENCE, SET)
        adapter.pop()

        // key
        if (index != -1 && item != -1) {
            adapter.loadLocal(index)
            adapter.loadArg(0)
            adapter.visitVarInsn(Opcodes.ILOAD, i)
            adapter.cast(Types.INT_VALUE, Types.DOUBLE_VALUE)
            adapter.invokeStatic(Types.CASTER, Methods_Caster.TO_DOUBLE.get(Methods_Caster.DOUBLE))
            adapter.invokeVirtual(Types.VARIABLE_REFERENCE, SET)
            adapter.pop()
        }
        getBody().writeOut(bc)
        forVisitor.visitEnd(bc, len, true, getStart())
    }

    /**
     * @see tachyon.transformer.bytecode.statement.FlowControl.getBreakLabel
     */
    @Override
    fun getBreakLabel(): Label? {
        return loopVisitor.getBreakLabel()
    }

    /**
     * @see tachyon.transformer.bytecode.statement.FlowControl.getContinueLabel
     */
    @Override
    fun getContinueLabel(): Label? {
        return loopVisitor.getContinueLabel()
    }

    @Override
    override fun getType(): Short {
        return TAG_LOOP
    }

    fun setLoopVisitor(loopVisitor: LoopVisitor?) {
        this.loopVisitor = loopVisitor
    }

    @Override
    fun getFlowControlFinal(): FlowControlFinal? {
        return null
    }

    fun setLabel(label: String?) {
        this.label = label
    }

    @Override
    fun getLabel(): String? {
        return label
    }

    companion object {
        const val TYPE_FILE = 1
        const val TYPE_LIST = 2
        const val TYPE_FROM_TO = 3
        const val TYPE_CONDITION = 4
        const val TYPE_QUERY = 5
        const val TYPE_COLLECTION = 6
        const val TYPE_ARRAY = 7
        const val TYPE_GROUP = 8
        const val TYPE_INNER_GROUP = 9
        const val TYPE_INNER_QUERY = 10
        const val TYPE_NOTHING = 11
        const val TYPE_STRUCT = 12
        const val TYPE_TIMES = 13

        // VariableReference getVariableReference(PageContext pc,String var)
        private val GET_VARIABLE_REFERENCE: Method? = Method("getVariableReference", Types.VARIABLE_REFERENCE, arrayOf<Type?>(Types.PAGE_CONTEXT, Types.STRING))

        // Object set(PageContext pc, Object value)
        private val SET: Method? = Method("set", Types.OBJECT, arrayOf<Type?>(Types.PAGE_CONTEXT, Types.OBJECT))

        // Object set(double value)
        private val SET_DOUBLE: Method? = Method("set", Types.VOID, arrayOf<Type?>(Types.DOUBLE_VALUE))

        /*
	 * private static final Method KEYS = new Method( "keyIterator", Types.COLLECTION_KEY_ARRAY, new
	 * Type[]{});
	 */
        private val GET: Method? = Method("get", Types.OBJECT, arrayOf<Type?>(Types.INT_VALUE, Types.OBJECT))
        private val NEXT: Method? = Method("next", Types.OBJECT, arrayOf<Type?>())
        private val HAS_NEXT: Method? = Method("hasNext", Types.BOOLEAN_VALUE, arrayOf<Type?>())

        // File toFileExisting(PageContext pc ,String destination)
        private val RESOURCE_UTIL: Type? = Type.getType(ResourceUtil::class.java)
        private val TO_RESOURCE_EXISTING: Method? = Method("toResourceExisting", Types.RESOURCE, arrayOf<Type?>(Types.PAGE_CONTEXT, Types.STRING))

        // Config getConfig()
        private val GET_CONFIG: Method? = Method("getConfig", Types.CONFIG_WEB, arrayOf<Type?>())

        // SecurityManager getSecurityManager()
        private val GET_SECURITY_MANAGER: Method? = Method("getSecurityManager", Types.SECURITY_MANAGER, arrayOf<Type?>())

        // void checkFileLocation(File file)
        private val CHECK_FILE_LOCATION: Method? = Method("checkFileLocation", Types.VOID, arrayOf<Type?>(Types.RESOURCE))

        // Reader getReader(File file, String charset)
        private val IO_UTIL: Type? = Type.getType(IOUtil::class.java)
        private val GET_BUFFERED_READER: Method? = Method("getBufferedReader", Types.BUFFERED_READER, arrayOf<Type?>(Types.RESOURCE, Types.STRING))

        // void closeEL(Reader r)
        private val CLOSE_EL: Method? = Method("closeEL", Types.VOID, arrayOf<Type?>(Types.READER))

        // String readLine()
        private val READ_LINE: Method? = Method("readLine", Types.STRING, arrayOf<Type?>())

        // Array listToArrayRemoveEmpty(String list, String delimiter)
        private val LIST_TO_ARRAY_REMOVE_EMPTY_SS: Method? = Method("listToArrayRemoveEmpty", Types.ARRAY, arrayOf<Type?>(Types.STRING, Types.STRING))

        // Array listToArrayRemoveEmpty(String list, char delimiter)
        private val LIST_TO_ARRAY_REMOVE_EMPTY_SC: Method? = Method("listToArrayRemoveEmpty", Types.ARRAY, arrayOf<Type?>(Types.STRING, Types.CHAR))
        private val SIZE: Method? = Method("size", Types.INT_VALUE, arrayOf<Type?>())

        // Object get(int key) klo
        private val GETE: Method? = Method("getE", Types.OBJECT, arrayOf<Type?>(Types.INT_VALUE))

        // Query getQuery(String key)
        val GET_QUERY_OBJ: Method? = Method("getQuery", Types.QUERY, arrayOf<Type?>(Types.OBJECT))
        val GET_QUERY_STRING: Method? = Method("getQuery", Types.QUERY, arrayOf<Type?>(Types.STRING))

        // int getCurrentrow()
        val GET_CURRENTROW_1: Method? = Method("getCurrentrow", Types.INT_VALUE, arrayOf<Type?>(Types.INT_VALUE))
        val GO: Method? = Method("go", Types.BOOLEAN_VALUE, arrayOf<Type?>(Types.INT_VALUE, Types.INT_VALUE))
        val GET_ID: Method? = Method("getId", Types.INT_VALUE, arrayOf<Type?>())
        private val READ: Method? = Method("read", Types.STRING, arrayOf<Type?>(Types.READER, Types.INT_VALUE))
        private val ENTRY_ITERATOR: Method? = Method("entryIterator", Types.ITERATOR, arrayOf<Type?>())
        private val GET_KEY: Method? = Method("getKey", Types.OBJECT, arrayOf<Type?>())
        private val GET_VALUE: Method? = Method("getValue", Types.OBJECT, arrayOf<Type?>())
    }
}