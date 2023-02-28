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
package tachyon.transformer.bytecode.statement

import org.objectweb.asm.Label

class ForEach(key: Variable?, value: Expression?, body: Body?, start: Position?, end: Position?, label: String?) : StatementBase(key.getFactory(), start, end), FlowControlBreak, FlowControlContinue, HasBody {
    private val body: Body?
    private val key: VariableRef?
    private val value: Expression?

    // private static final Type COLLECTION_UTIL = Type.getType(CollectionUtil.class);
    private val begin: Label? = Label()
    private val end: Label? = Label()
    private var fcf: FlowControlFinal? = null
    private val label: String?

    @Override
    @Throws(TransformerException::class)
    override fun _writeOut(bc: BytecodeContext?) {
        val adapter: GeneratorAdapter = bc.getAdapter()
        val it: Int = adapter.newLocal(Types.ITERATOR)
        val item: Int = adapter.newLocal(Types.REFERENCE)

        // Value
        // ForEachUtil.toIterator(value)
        value.writeOut(bc, Expression.MODE_REF)
        adapter.invokeStatic(FOR_EACH_UTIL, FOR_EACH)
        // adapter.invokeStatic(COLLECTION_UTIL, TO_ITERATOR);
        // Iterator it=...
        adapter.storeLocal(it)
        val tfv = TryFinallyVisitor(object : OnFinally() {
            @Override
            @Throws(TransformerException::class)
            fun _writeOut(bc: BytecodeContext?) {
                val a: GeneratorAdapter = bc.getAdapter()
                // if(fcf!=null &&
                // fcf.getAfterFinalGOTOLabel()!=null)ASMUtil.visitLabel(a,fcf.getFinalEntryLabel());
                a.loadLocal(it)
                a.invokeStatic(FOR_EACH_UTIL, RESET)
                /*
				 * if(fcf!=null){ Label l=fcf.getAfterFinalGOTOLabel(); if(l!=null)a.visitJumpInsn(Opcodes.GOTO, l);
				 * }
				 */
            }
        }, getFlowControlFinal())
        tfv.visitTryBegin(bc)
        // Key
        // new VariableReference(...)
        key.writeOut(bc, Expression.MODE_REF)
        // VariableReference item=...
        adapter.storeLocal(item)

        // while
        ExpressionUtil.visitLine(bc, getStart())
        adapter.visitLabel(begin)

        // hasNext
        adapter.loadLocal(it)
        adapter.invokeInterface(Types.ITERATOR, HAS_NEXT)
        adapter.ifZCmp(Opcodes.IFEQ, end)

        // item.set(pc,it.next());
        adapter.loadLocal(item)
        adapter.loadArg(0)
        adapter.loadLocal(it)
        adapter.invokeInterface(Types.ITERATOR, NEXT)
        adapter.invokeInterface(Types.REFERENCE, SET)
        adapter.pop()

        // Body
        body.writeOut(bc)
        adapter.visitJumpInsn(Opcodes.GOTO, begin)
        adapter.visitLabel(end)
        tfv.visitTryEnd(bc)
    }

    @Override
    override fun getBreakLabel(): Label? {
        return end
    }

    @Override
    override fun getContinueLabel(): Label? {
        return begin
    }

    @Override
    override fun getBody(): Body? {
        return body
    }

    @Override
    fun getFlowControlFinal(): FlowControlFinal? {
        if (fcf == null) fcf = FlowControlFinalImpl()
        return fcf
    }

    @Override
    override fun getLabel(): String? {
        return label
    }

    companion object {
        private val HAS_NEXT: Method? = Method("hasNext", Types.BOOLEAN_VALUE, arrayOf<Type?>())
        private val NEXT: Method? = Method("next", Types.OBJECT, arrayOf<Type?>())
        private val SET: Method? = Method("set", Types.OBJECT, arrayOf<Type?>(Types.PAGE_CONTEXT, Types.OBJECT))
        val LOOP_COLLECTION: Method? = Method("loopCollection", Types.ITERATOR, arrayOf<Type?>(Types.OBJECT))
        val FOR_EACH: Method? = Method("forEach", Types.ITERATOR, arrayOf<Type?>(Types.OBJECT))
        val FOR_EACH_UTIL: Type? = Type.getType(ForEachUtil::class.java)
        val RESET: Method? = Method("reset", Types.VOID, arrayOf<Type?>(Types.ITERATOR))
    }

    /**
     * Constructor of the class
     *
     * @param key
     * @param value
     * @param body
     * @param line
     */
    init {
        this.key = VariableRef(key, false)
        this.value = value
        this.body = body
        this.label = label
        body.setParent(this)
    }
}