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

object TagGroupUtil {
    // Undefined us()
    val UNDEFINED: Type? = Type.getType(Undefined::class.java)
    val US: Method? = Method("us", UNDEFINED, arrayOf<Type?>())

    // void addQuery(Query coll)
    val ADD_QUERY: Method? = Method("addQuery", Types.VOID, arrayOf<Type?>(Types.QUERY))

    // void removeQuery()
    val REMOVE_QUERY: Method? = Method("removeQuery", Types.VOID, arrayOf<Type?>())

    // int getRecordcount()
    val GET_RECORDCOUNT: Method? = Method("getRecordcount", Types.INT_VALUE, arrayOf<Type?>())

    // double range(double number, double from)
    val RANGE: Method? = Method("range", Types.INT_VALUE, arrayOf<Type?>(Types.INT_VALUE, Types.INT_VALUE))
    val NUMBER_ITERATOR: Type? = Type.getType(NumberIterator::class.java)

    // NumberIterator load(double from, double to, double max)
    val LOAD_MAX: Method? = Method("loadMax", NUMBER_ITERATOR, arrayOf<Type?>(Types.INT_VALUE, Types.INT_VALUE, Types.INT_VALUE))
    val LOAD_END: Method? = Method("loadEnd", NUMBER_ITERATOR, arrayOf<Type?>(Types.INT_VALUE, Types.INT_VALUE, Types.INT_VALUE))

    // NumberIterator load(double from, double to, double max)
    val LOAD_2: Method? = Method("load", NUMBER_ITERATOR, arrayOf<Type?>(Types.INT_VALUE, Types.INT_VALUE))

    // NumberIterator load(NumberIterator ni, Query query, String groupName, boolean caseSensitive)
    val LOAD_5: Method? = Method("load", NUMBER_ITERATOR, arrayOf<Type?>(Types.PAGE_CONTEXT, NUMBER_ITERATOR, Types.QUERY, Types.STRING, Types.BOOLEAN_VALUE))

    // boolean isValid()
    /*
	 * public static final Method IS_VALID_0 = new Method( "isValid", Types.BOOLEAN_VALUE, new
	 * Type[]{});
	 */
    val IS_VALID_1: Method? = Method("isValid", Types.BOOLEAN_VALUE, arrayOf<Type?>(Types.INT_VALUE))

    // int current()
    val CURRENT: Method? = Method("current", Types.INT_VALUE, arrayOf<Type?>())

    // void release(NumberIterator ni)
    val REALEASE: Method? = Method("release", Types.VOID, arrayOf<Type?>(NUMBER_ITERATOR))

    // void setCurrent(int current)
    val SET_CURRENT: Method? = Method("setCurrent", Types.VOID, arrayOf<Type?>(Types.INT_VALUE))

    // void reset()
    val RESET: Method? = Method("reset", Types.VOID, arrayOf<Type?>(Types.INT_VALUE))

    // int first()
    val FIRST: Method? = Method("first", Types.INT_VALUE, arrayOf<Type?>())
    val GET_ID: Method? = Method("getId", Types.INT_VALUE, arrayOf<Type?>())
    @Throws(TransformerException::class)
    fun writeOutTypeQuery(tag: TagGroup?, bc: BytecodeContext?) {
        val adapter: GeneratorAdapter = bc.getAdapter()
        tag!!.setNumberIterator(adapter.newLocal(NUMBER_ITERATOR))
        val isOutput = tag!!.getType() === TagGroup.TAG_OUTPUT
        val pbv: ParseBodyVisitor? = if (isOutput) ParseBodyVisitor() else null
        if (isOutput) pbv.visitBegin(bc)

        // Query query=pc.getQuery(@query);
        tag!!.setQuery(adapter.newLocal(Types.QUERY))
        adapter.loadArg(0)
        val `val`: Expression = tag!!.getAttribute("query")!!.getValue()
        `val`.writeOut(bc, Expression.MODE_REF)
        if (`val` is LitString) adapter.invokeVirtual(Types.PAGE_CONTEXT, TagLoop.GET_QUERY_STRING) else adapter.invokeVirtual(Types.PAGE_CONTEXT, TagLoop.GET_QUERY_OBJ)
        adapter.storeLocal(tag!!.getQuery())
        tag!!.setPID(adapter.newLocal(Types.INT_VALUE))
        adapter.loadArg(0)
        adapter.invokeVirtual(Types.PAGE_CONTEXT, TagLoop.GET_ID)
        adapter.storeLocal(tag!!.getPID())

        // int startAt=query.getCurrentrow();
        val startAt: Int = adapter.newLocal(Types.INT_VALUE)
        adapter.loadLocal(tag!!.getQuery())
        adapter.loadLocal(tag!!.getPID())
        // adapter.loadArg(0);
        // adapter.invokeVirtual(Types.PAGE_CONTEXT, TagLoop.GET_ID);
        adapter.invokeInterface(Types.QUERY, TagLoop.GET_CURRENTROW_1)
        adapter.storeLocal(startAt)

        // if(query.getRecordcount()>0) {
        val div = DecisionIntVisitor()
        div.visitBegin()
        adapter.loadLocal(tag!!.getQuery())
        adapter.invokeInterface(Types.QUERY, GET_RECORDCOUNT)
        div.visitGT()
        adapter.push(0)
        div.visitEnd(bc)
        val ifRecCount = Label()
        adapter.ifZCmp(Opcodes.IFEQ, ifRecCount)

        // startrow
        val from: Int = adapter.newLocal(Types.INT_VALUE)
        val attrStartRow: Attribute = tag!!.getAttribute("startrow")
        if (attrStartRow != null) {
            // NumberRange.range(@startrow,1)
            // attrStartRow.getValue().writeOut(bc, Expression.MODE_VALUE);
            bc.getFactory().toExprInt(attrStartRow.getValue()).writeOut(bc, Expression.MODE_VALUE)
            // adapter.visitInsn(Opcodes.D2I);
            adapter.push(1)
            adapter.invokeStatic(Types.NUMBER_RANGE, RANGE)
            // adapter.visitInsn(Opcodes.D2I);
        } else {
            adapter.push(1)
        }
        adapter.storeLocal(from)

        // numberIterator
        adapter.loadLocal(from)
        adapter.loadLocal(tag!!.getQuery())
        adapter.invokeInterface(Types.QUERY, GET_RECORDCOUNT)
        // adapter.visitInsn(Opcodes.I2D);
        val attrMaxRow: Attribute = tag!!.getAttribute("maxrows")
        val attrEndRow: Attribute = tag!!.getAttribute("endrow")
        if (attrMaxRow != null) {
            bc.getFactory().toExprInt(attrMaxRow.getValue()).writeOut(bc, Expression.MODE_VALUE)
            adapter.invokeStatic(NUMBER_ITERATOR, LOAD_MAX)
        } else if (attrEndRow != null) {
            bc.getFactory().toExprInt(attrEndRow.getValue()).writeOut(bc, Expression.MODE_VALUE)
            adapter.invokeStatic(NUMBER_ITERATOR, LOAD_END)
        } else {
            adapter.invokeStatic(NUMBER_ITERATOR, LOAD_2)
        }
        adapter.storeLocal(tag!!.getNumberIterator())

        // Group
        val attrGroup: Attribute = tag!!.getAttribute("group")
        val attrGroupCS: Attribute = tag!!.getAttribute("groupcasesensitive")
        tag!!.setGroup(adapter.newLocal(Types.STRING))
        val groupCaseSensitive: Int = adapter.newLocal(Types.BOOLEAN_VALUE)
        if (attrGroup != null) {
            attrGroup.getValue()!!.writeOut(bc, Expression.MODE_REF)
            adapter.storeLocal(tag!!.getGroup())
            if (attrGroupCS != null) attrGroupCS.getValue()!!.writeOut(bc, Expression.MODE_VALUE) else adapter.push(false)
            adapter.storeLocal(groupCaseSensitive)
        }

        // pc.us().addQuery(query);
        adapter.loadArg(0)
        adapter.invokeVirtual(Types.PAGE_CONTEXT, US)
        adapter.loadLocal(tag!!.getQuery())
        adapter.invokeInterface(UNDEFINED, ADD_QUERY)

        // current
        val current: Int = adapter.newLocal(Types.INT_VALUE)
        adapter.loadLocal(from)
        adapter.push(1)
        adapter.visitInsn(Opcodes.ISUB)
        adapter.storeLocal(current)

        // Try
        val tfv = TryFinallyVisitor(object : OnFinally() {
            @Override
            fun _writeOut(bc: BytecodeContext?) {
                // query.reset();

                // query.go(startAt);
                adapter.loadLocal(tag!!.getQuery())
                adapter.loadLocal(startAt)
                adapter.loadLocal(tag!!.getPID())
                // adapter.loadArg(0);
                // adapter.invokeVirtual(Types.PAGE_CONTEXT, TagLoop.GET_ID);
                adapter.invokeInterface(Types.QUERY, TagLoop.GO)
                adapter.pop()

                // pc.us().removeQuery();
                adapter.loadArg(0)
                adapter.invokeVirtual(Types.PAGE_CONTEXT, US)
                adapter.invokeInterface(UNDEFINED, REMOVE_QUERY)

                // NumberIterator.release(ni);
                adapter.loadLocal(tag!!.getNumberIterator())
                adapter.invokeStatic(NUMBER_ITERATOR, REALEASE)
            }
        }, null)
        tfv.visitTryBegin(bc)
        val wv = WhileVisitor()
        if (tag is TagLoop) (tag as TagLoop?)!!.setLoopVisitor(wv)
        wv.visitBeforeExpression(bc)

        // while(ni.isValid()) {
        adapter.loadLocal(tag!!.getNumberIterator())
        adapter.loadLocal(current)
        adapter.push(1)
        adapter.visitInsn(Opcodes.IADD)
        adapter.invokeVirtual(NUMBER_ITERATOR, IS_VALID_1)
        wv.visitAfterExpressionBeforeBody(bc)

        // if(!query.go(ni.current()))break;
        adapter.loadLocal(tag!!.getQuery())
        adapter.loadLocal(tag!!.getNumberIterator())
        adapter.invokeVirtual(NUMBER_ITERATOR, CURRENT)
        adapter.loadLocal(tag!!.getPID())
        adapter.invokeInterface(Types.QUERY, TagLoop.GO)
        NotVisitor.visitNot(bc)
        val _if = Label()
        adapter.ifZCmp(Opcodes.IFEQ, _if)
        wv.visitBreak(bc)
        adapter.visitLabel(_if)
        if (attrGroup != null) {
            // NumberIterator oldNi=numberIterator;
            val oldNi: Int = adapter.newLocal(NUMBER_ITERATOR)
            adapter.loadLocal(tag!!.getNumberIterator())
            adapter.storeLocal(oldNi)

            // numberIterator=NumberIterator.load(ni,query,group,grp_case);
            adapter.loadArg(0)
            adapter.loadLocal(tag!!.getNumberIterator())
            adapter.loadLocal(tag!!.getQuery())
            adapter.loadLocal(tag!!.getGroup())
            adapter.loadLocal(groupCaseSensitive)
            adapter.invokeStatic(NUMBER_ITERATOR, LOAD_5)
            adapter.storeLocal(tag!!.getNumberIterator())

            // current=oldNi.current();
            adapter.loadLocal(oldNi)
            adapter.invokeVirtual(NUMBER_ITERATOR, CURRENT)
            adapter.storeLocal(current)
            tag!!.getBody().writeOut(bc)

            // tmp(adapter,current);

            // NumberIterator.release(ni);
            adapter.loadLocal(tag!!.getNumberIterator())
            adapter.invokeStatic(NUMBER_ITERATOR, REALEASE)

            // numberIterator=oldNi;
            adapter.loadLocal(oldNi)
            adapter.storeLocal(tag!!.getNumberIterator())
        } else {
            // current=ni.current();
            adapter.loadLocal(tag!!.getNumberIterator())
            adapter.invokeVirtual(NUMBER_ITERATOR, CURRENT)
            adapter.storeLocal(current)
            tag!!.getBody().writeOut(bc)
        }

        // ni.setCurrent(current+1);
        /*
		 * adapter.loadLocal(tag.getNumberIterator()); adapter.loadLocal(current); adapter.push(1);
		 * adapter.visitInsn(Opcodes.IADD); adapter.invokeVirtual(NUMBER_ITERATOR, SET_CURRENT);
		 */wv.visitAfterBody(bc, tag.getEnd())
        tfv.visitTryEnd(bc)
        adapter.visitLabel(ifRecCount)
        if (isOutput) pbv.visitEnd(bc)
    }

    @Throws(TransformerException::class)
    fun writeOutTypeGroup(tag: TagGroup?, bc: BytecodeContext?) {
        val adapter: GeneratorAdapter = bc.getAdapter()
        val isOutput = tag!!.getType() === TagGroup.TAG_OUTPUT
        val pbv: ParseBodyVisitor? = if (isOutput) ParseBodyVisitor() else null
        if (isOutput) pbv.visitBegin(bc)

        // Group
        val attrGroup: Attribute = tag!!.getAttribute("group")
        tag!!.setGroup(adapter.newLocal(Types.STRING))
        attrGroup!!.getValue()!!.writeOut(bc, Expression.MODE_REF)
        adapter.storeLocal(tag!!.getGroup())

        // Group Case Sensitve
        val attrGroupCS: Attribute = tag!!.getAttribute("groupcasesensitive")
        val groupCaseSensitive: Int = adapter.newLocal(Types.BOOLEAN_VALUE)
        if (attrGroupCS != null) attrGroupCS.getValue()!!.writeOut(bc, Expression.MODE_VALUE) else adapter.push(true)
        adapter.storeLocal(groupCaseSensitive)
        val parent: TagGroup? = getParentTagGroupQuery(bc, tag, tag!!.getType())
        tag!!.setNumberIterator(parent!!.getNumberIterator())
        tag!!.setQuery(parent!!.getQuery())
        // queryImpl = parent.getQueryImpl();

        // current
        val current: Int = adapter.newLocal(Types.INT_VALUE)
        adapter.loadLocal(tag!!.getNumberIterator())
        adapter.invokeVirtual(NUMBER_ITERATOR, CURRENT)
        adapter.storeLocal(current)

        // current
        val icurrent: Int = adapter.newLocal(Types.INT_VALUE)
        adapter.loadLocal(current)
        adapter.push(1)
        adapter.visitInsn(Opcodes.ISUB)
        adapter.storeLocal(icurrent)
        val wv = WhileVisitor()
        if (tag is TagLoop) (tag as TagLoop?)!!.setLoopVisitor(wv)
        wv.visitBeforeExpression(bc)

        // while(ni.isValid()) {
        adapter.loadLocal(tag!!.getNumberIterator())
        adapter.loadLocal(icurrent)
        adapter.push(1)
        adapter.visitInsn(Opcodes.IADD)
        adapter.invokeVirtual(NUMBER_ITERATOR, IS_VALID_1)
        wv.visitAfterExpressionBeforeBody(bc)

        // if(!query.go(ni.current()))break;
        adapter.loadLocal(tag!!.getQuery())
        adapter.loadLocal(tag!!.getNumberIterator())
        adapter.invokeVirtual(NUMBER_ITERATOR, CURRENT)
        adapter.loadArg(0)
        adapter.invokeVirtual(Types.PAGE_CONTEXT, GET_ID)
        adapter.invokeInterface(Types.QUERY, TagLoop.GO)
        NotVisitor.visitNot(bc)
        val _if = Label()
        adapter.ifZCmp(Opcodes.IFEQ, _if)
        wv.visitBreak(bc)
        adapter.visitLabel(_if)

        // NumberIterator oldNi=numberIterator;
        val oldNi: Int = adapter.newLocal(NUMBER_ITERATOR)
        adapter.loadLocal(tag!!.getNumberIterator())
        adapter.storeLocal(oldNi)

        // numberIterator=NumberIterator.load(ni,query,group,grp_case);
        adapter.loadArg(0)
        adapter.loadLocal(tag!!.getNumberIterator())
        adapter.loadLocal(tag!!.getQuery())
        adapter.loadLocal(tag!!.getGroup())
        adapter.loadLocal(groupCaseSensitive)
        adapter.invokeStatic(NUMBER_ITERATOR, LOAD_5)
        adapter.storeLocal(tag!!.getNumberIterator())

        // current=oldNi.current();
        adapter.loadLocal(oldNi)
        adapter.invokeVirtual(NUMBER_ITERATOR, CURRENT)
        adapter.storeLocal(icurrent)
        tag!!.getBody().writeOut(bc)

        // tmp(adapter,current);

        // NumberIterator.release(ni);
        adapter.loadLocal(tag!!.getNumberIterator())
        adapter.invokeStatic(NUMBER_ITERATOR, REALEASE)

        // numberIterator=oldNi;
        adapter.loadLocal(oldNi)
        adapter.storeLocal(tag!!.getNumberIterator())

        // ni.setCurrent(current+1);
        /*
		 * adapter.loadLocal(tag.getNumberIterator()); adapter.loadLocal(icurrent); adapter.push(1);
		 * adapter.visitInsn(Opcodes.IADD); adapter.invokeVirtual(NUMBER_ITERATOR, SET_CURRENT);
		 */wv.visitAfterBody(bc, tag.getEnd())

        // query.go(ni.current(),pc.getId())
        resetCurrentrow(adapter, tag, current)

        // ni.first();
        adapter.loadLocal(tag!!.getNumberIterator())
        adapter.invokeVirtual(NUMBER_ITERATOR, FIRST)
        adapter.pop()
        if (isOutput) pbv.visitEnd(bc)
    }

    @Throws(TransformerException::class)
    fun writeOutTypeInnerGroup(tag: TagGroup?, bc: BytecodeContext?) {
        val adapter: GeneratorAdapter = bc.getAdapter()
        val parent: TagGroup? = getParentTagGroupQuery(bc, tag, tag!!.getType())
        tag!!.setNumberIterator(parent!!.getNumberIterator())
        tag!!.setQuery(parent!!.getQuery())
        // queryImpl = parent.getQueryImpl();
        val current: Int = adapter.newLocal(Types.INT_VALUE)
        adapter.loadLocal(tag!!.getNumberIterator())
        adapter.invokeVirtual(NUMBER_ITERATOR, CURRENT)
        adapter.storeLocal(current)

        // inner current
        val icurrent: Int = adapter.newLocal(Types.INT_VALUE)
        adapter.loadLocal(current)
        adapter.push(1)
        adapter.visitInsn(Opcodes.ISUB)
        adapter.storeLocal(icurrent)
        val wv = WhileVisitor()
        if (tag is TagLoop) (tag as TagLoop?)!!.setLoopVisitor(wv)
        wv.visitBeforeExpression(bc)

        // while(ni.isValid()) {
        adapter.loadLocal(tag!!.getNumberIterator())
        adapter.loadLocal(icurrent)
        adapter.push(1)
        adapter.visitInsn(Opcodes.IADD)
        adapter.invokeVirtual(NUMBER_ITERATOR, IS_VALID_1)
        wv.visitAfterExpressionBeforeBody(bc)

        // if(!query.go(ni.current()))break;
        adapter.loadLocal(tag!!.getQuery())
        adapter.loadLocal(tag!!.getNumberIterator())
        adapter.invokeVirtual(NUMBER_ITERATOR, CURRENT)
        adapter.loadArg(0)
        adapter.invokeVirtual(Types.PAGE_CONTEXT, GET_ID)
        adapter.invokeInterface(Types.QUERY, TagLoop.GO)

        /*
		 * OLD adapter.invokeInterface(Types.QUERY, TagLoop.GO_1);
		 */NotVisitor.visitNot(bc)
        val _if = Label()
        adapter.ifZCmp(Opcodes.IFEQ, _if)
        wv.visitBreak(bc)
        adapter.visitLabel(_if)

        // current=ni.current();
        adapter.loadLocal(tag!!.getNumberIterator())
        adapter.invokeVirtual(NUMBER_ITERATOR, CURRENT)
        adapter.storeLocal(icurrent)
        tag!!.getBody().writeOut(bc)

        // ni.setCurrent(current+1);
        /*
		 * adapter.loadLocal(tag.getNumberIterator()); adapter.loadLocal(icurrent); adapter.push(1);
		 * adapter.visitInsn(Opcodes.IADD); adapter.invokeVirtual(NUMBER_ITERATOR, SET_CURRENT);
		 */wv.visitAfterBody(bc, tag.getEnd())
        resetCurrentrow(adapter, tag, current)

        // ni.first();
        adapter.loadLocal(tag!!.getNumberIterator())
        adapter.invokeVirtual(NUMBER_ITERATOR, FIRST)
        adapter.pop()
    }

    @Throws(TransformerException::class)
    fun writeOutTypeInnerQuery(tag: TagGroup?, bc: BytecodeContext?) {
        val adapter: GeneratorAdapter = bc.getAdapter()
        // if(tr ue)return ;
        val parent: TagGroup? = getParentTagGroupQuery(bc, tag, tag!!.getType())
        tag!!.setNumberIterator(parent!!.getNumberIterator())
        tag!!.setQuery(parent!!.getQuery())
        tag!!.setPID(parent!!.getPID())
        // queryImpl = parent.getQueryImpl();

        // int currentOuter=ni.current();
        val current: Int = adapter.newLocal(Types.INT_VALUE)
        adapter.loadLocal(tag!!.getNumberIterator())
        adapter.invokeVirtual(NUMBER_ITERATOR, CURRENT)
        adapter.storeLocal(current)

        // current
        val icurrent: Int = adapter.newLocal(Types.INT_VALUE)
        adapter.loadLocal(current)
        adapter.push(1)
        adapter.visitInsn(Opcodes.ISUB)
        adapter.storeLocal(icurrent)
        val wv = WhileVisitor()
        if (tag is TagLoop) (tag as TagLoop?)!!.setLoopVisitor(wv)
        wv.visitBeforeExpression(bc)

        // while(ni.isValid()) {
        adapter.loadLocal(tag!!.getNumberIterator())
        adapter.loadLocal(icurrent)
        adapter.push(1)
        adapter.visitInsn(Opcodes.IADD)
        adapter.invokeVirtual(NUMBER_ITERATOR, IS_VALID_1)
        wv.visitAfterExpressionBeforeBody(bc)

        // if(!query.go(ni.current()))break;
        adapter.loadLocal(tag!!.getQuery())
        adapter.loadLocal(tag!!.getNumberIterator())
        adapter.invokeVirtual(NUMBER_ITERATOR, CURRENT)
        adapter.loadLocal(tag!!.getPID())
        adapter.invokeInterface(Types.QUERY, TagLoop.GO)
        NotVisitor.visitNot(bc)
        val _if = Label()
        adapter.ifZCmp(Opcodes.IFEQ, _if)
        wv.visitBreak(bc)
        adapter.visitLabel(_if)

        // current=ni.current();
        adapter.loadLocal(tag!!.getNumberIterator())
        adapter.invokeVirtual(NUMBER_ITERATOR, CURRENT)
        adapter.storeLocal(icurrent)
        tag!!.getBody().writeOut(bc)

        // ni.setCurrent(current+1);
        /*
		 * adapter.loadLocal(tag.getNumberIterator()); adapter.loadLocal(icurrent); adapter.push(1);
		 * adapter.visitInsn(Opcodes.IADD); adapter.invokeVirtual(NUMBER_ITERATOR, SET_CURRENT);
		 */wv.visitAfterBody(bc, tag.getEnd())

        // ni.setCurrent(currentOuter);
        adapter.loadLocal(tag!!.getNumberIterator())
        adapter.loadLocal(current)
        adapter.invokeVirtual(NUMBER_ITERATOR, SET_CURRENT)
        adapter.loadLocal(tag!!.getQuery())
        adapter.loadLocal(current)
        adapter.loadLocal(tag!!.getPID())
        // adapter.loadArg(0);
        // adapter.invokeVirtual(Types.PAGE_CONTEXT, GET_ID);
        adapter.invokeInterface(Types.QUERY, TagLoop.GO)
        adapter.pop()
        // adapter.pop();
    }

    @Throws(TransformerException::class)
    fun getParentTagGroupQuery(bc: BytecodeContext?, st: Statement?, type: Short): TagGroup? {
        val parent: Statement = st.getParent()
        if (parent == null) throw TransformerException(bc, "there is no parent output with query", null) else if (parent is TagGroup && type == (parent as TagGroup)!!.getType()) {
            if ((parent as TagGroup)!!.hasQuery()) return parent
        }
        return getParentTagGroupQuery(bc, parent, type)
    }

    private fun resetCurrentrow(adapter: GeneratorAdapter?, tg: TagGroup?, current: Int) {
        // query.go(ni.current(),pc.getId())
        adapter.loadLocal(tg!!.getQuery())
        adapter.loadLocal(current)
        adapter.loadArg(0)
        adapter.invokeVirtual(Types.PAGE_CONTEXT, GET_ID)
        adapter.invokeInterface(Types.QUERY, TagLoop.GO)

        /*
		 * OLD adapter.invokeInterface(Types.QUERY, TagLoop.GO_1);
		 */adapter.pop()
    }
}