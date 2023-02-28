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
package lucee.transformer.bytecode.statement

import java.util.ArrayList

/**
 * produce try-catch-finally
 */
class TryCatchFinally(factory: Factory?, body: Body?, start: Position?, end: Position?) : StatementBase(factory, start, end), Opcodes, HasBodies, FlowControlRetry {
    private val tryBody: Body?
    private var finallyBody: Body? = null
    private val catches: List<Catch?>? = ArrayList<Catch?>()
    private var finallyLine: Position? = null
    private val begin: Label? = Label()
    private var fcf: FlowControlFinal? = null

    /**
     * sets finally body
     *
     * @param body
     */
    fun setFinally(body: Body?, finallyLine: Position?) {
        body.setParent(this)
        finallyBody = body
        this.finallyLine = finallyLine
    }

    /**
     * data for a single catch block
     */
    private inner class Catch(type: ExprString?, name: VariableRef?, body: Body?, line: Position?) {
        val type: ExprString?
        val body: Body?
        val name: VariableRef?
        val line: Position?

        init {
            this.type = type
            this.name = name
            this.body = body
            this.line = line
        }
    }

    /**
     *
     * @see lucee.transformer.bytecode.statement.StatementBase._writeOut
     */
    @Override
    @Throws(TransformerException::class)
    override fun _writeOut(bc: BytecodeContext?) {
        val adapter: GeneratorAdapter = bc.getAdapter()
        adapter.visitLabel(begin)

        // Reference ref=null;
        val lRef: Int = adapter.newLocal(Types.REFERENCE)
        adapter.visitInsn(Opcodes.ACONST_NULL)
        adapter.storeLocal(lRef)

        // has no try body, if there is no try body, no catches are executed, only finally
        if (!tryBody.hasStatements()) {
            if (finallyBody != null) finallyBody.writeOut(bc)
            return
        }

        // PageExceptionImpl old=pc.getCatch();
        val old: Int = adapter.newLocal(Types.PAGE_EXCEPTION)
        adapter.loadArg(0)
        adapter.invokeVirtual(Types.PAGE_CONTEXT, TagTry.GET_CATCH)
        adapter.storeLocal(old)
        val tcfv = TryCatchFinallyVisitor(object : OnFinally() {
            @Override
            @Throws(TransformerException::class)
            fun _writeOut(bc: BytecodeContext?) {
                adapter.loadArg(0)
                adapter.loadLocal(old)
                adapter.invokeVirtual(Types.PAGE_CONTEXT, TagTry.SET_CATCH_PE)
                _writeOutFinally(bc, lRef)
            }
        }, getFlowControlFinal())

        // try
        tcfv.visitTryBegin(bc)
        tryBody.writeOut(bc)
        val lThrow: Int = tcfv.visitTryEndCatchBeging(bc)
        _writeOutCatch(bc, lRef, lThrow, old)
        tcfv.visitCatchEnd(bc)
    }

    @Throws(TransformerException::class)
    private fun _writeOutFinally(bc: BytecodeContext?, lRef: Int) {
        // ref.remove(pc);
        // Reference r=null;
        val adapter: GeneratorAdapter = bc.getAdapter()

        // if(fcf!=null &&
        // fcf.getAfterFinalGOTOLabel()!=null)ASMUtil.visitLabel(adapter,fcf.getFinalEntryLabel());
        ExpressionUtil.visitLine(bc, finallyLine)

        // if (reference != null)
        // reference.removeEL(pagecontext);
        val removeEnd = Label()
        adapter.loadLocal(lRef)
        adapter.ifNull(removeEnd)
        adapter.loadLocal(lRef)
        adapter.loadArg(0)
        adapter.invokeInterface(Types.REFERENCE, REMOVE_EL)
        adapter.pop()
        adapter.visitLabel(removeEnd)
        if (finallyBody != null) finallyBody.writeOut(bc) // finally
        /*
		 * if(fcf!=null){ Label l = fcf.getAfterFinalGOTOLabel();
		 * if(l!=null)adapter.visitJumpInsn(Opcodes.GOTO, l); }
		 */
    }

    @Throws(TransformerException::class)
    private fun _writeOutCatch(bc: BytecodeContext?, lRef: Int, lThrow: Int, old: Int) {
        val adapter: GeneratorAdapter = bc.getAdapter()
        val pe: Int = adapter.newLocal(Types.PAGE_EXCEPTION)

        // instance of Abort
        val abortEnd = Label()
        adapter.loadLocal(lThrow)
        adapter.invokeStatic(Types.ABORT, IS_ABORT)
        // adapter.instanceOf(Types.ABORT);
        adapter.ifZCmp(Opcodes.IFEQ, abortEnd)
        adapter.loadLocal(lThrow)
        adapter.throwException()
        adapter.visitLabel(abortEnd)

        /*
		 * // PageExceptionImpl old=pc.getCatch(); int old=adapter.newLocal(Types.PAGE_EXCEPTION);
		 * adapter.loadArg(0); adapter.invokeVirtual(Types.PAGE_CONTEXT, TagTry.GET_CATCH);
		 * adapter.storeLocal(old);
		 */

        // cast to PageException Caster.toPagException(t);
        adapter.loadLocal(lThrow)
        adapter.invokeStatic(Types.CASTER, TO_PAGE_EXCEPTION)

        // PageException pe=...
        adapter.storeLocal(pe)

        // catch loop
        val endAllIf = Label()
        val it = catches!!.iterator()
        var ctElse: Catch? = null
        while (it.hasNext()) {
            val ct = it.next()
            // store any for else
            if (ct!!.type != null && ct.type is LitString && (ct.type as LitString?).getString().equalsIgnoreCase("any")) {
                ctElse = ct
                continue
            }
            ExpressionUtil.visitLine(bc, ct.line)

            // pe.typeEqual(type)
            if (ct.type == null) {
                getFactory().TRUE().writeOut(bc, Expression.MODE_VALUE)
            } else {
                adapter.loadLocal(pe)
                ct.type.writeOut(bc, Expression.MODE_REF)
                adapter.invokeVirtual(Types.PAGE_EXCEPTION, TYPE_EQUAL)
            }
            val endIf = Label()
            adapter.ifZCmp(Opcodes.IFEQ, endIf)
            catchBody(bc, adapter, ct, pe, lRef, true, true)
            adapter.visitJumpInsn(Opcodes.GOTO, endAllIf)
            adapter.visitLabel(endIf)
        }
        if (ctElse != null) {
            catchBody(bc, adapter, ctElse, pe, lRef, true, true)
        } else {
            // pc.setCatch(pe,true);
            adapter.loadArg(0)
            adapter.loadLocal(pe)
            adapter.push(false)
            adapter.push(false)
            adapter.invokeVirtual(Types.PAGE_CONTEXT, TagTry.SET_CATCH3)
            adapter.loadLocal(pe)
            adapter.throwException()
        }
        adapter.visitLabel(endAllIf)

        /*
		 * adapter.loadArg(0); adapter.loadLocal(old); adapter.invokeVirtual(Types.PAGE_CONTEXT,
		 * TagTry.SET_CATCH_PE);
		 */
    }

    /**
     * @param type
     * @param name
     * @param body
     * @param line
     */
    fun addCatch(type: ExprString?, name: VariableRef?, body: Body?, line: Position?) {
        body.setParent(this)
        catches.add(Catch(type, name, body, line))
    }

    /**
     * @param type
     * @param name
     * @param b
     * @param line
     * @throws TransformerException
     */
    @Throws(TransformerException::class)
    fun addCatch(bc: BytecodeContext?, type: Expression?, name: Expression?, b: Body?, line: Position?) {
        // MUSTMUST
        // type
        var type: Expression? = type
        var name: Expression? = name
        if (type == null || type is ExprString) {
        } else if (type is Variable) {
            type = VariableString.toExprString(type)
        } else throw TransformerException(bc, "type from catch statement is invalid", type.getStart())

        // name
        if (name is LitString) {
            val v: Variable = getFactory().createVariable(Scope.SCOPE_UNDEFINED, name.getStart(), name.getEnd())
            v.addMember(getFactory().createDataMember(getFactory().toExprString(name)))
            name = VariableRef(v, true)
        } else if (name is Variable) name = VariableRef(name as Variable?, true) else throw TransformerException(bc, "name from catch statement is invalid", name.getStart())
        addCatch(type as ExprString?, name as VariableRef?, b, line)
    }

    /**
     * @see lucee.transformer.bytecode.statement.HasBodies.getBodies
     */
    @Override
    override fun getBodies(): Array<Body?>? {
        var len: Int = catches!!.size()
        var count = 0
        if (tryBody != null) len++
        if (finallyBody != null) len++
        val bodies: Array<Body?> = arrayOfNulls<Body?>(len)
        var c: Catch
        val it = catches.iterator()
        while (it.hasNext()) {
            c = it.next()
            bodies[count++] = c.body
        }
        if (tryBody != null) bodies[count++] = tryBody
        if (finallyBody != null) bodies[count++] = finallyBody
        return bodies
    }

    @Override
    fun getFlowControlFinal(): FlowControlFinal? {
        if (fcf == null) fcf = FlowControlFinalImpl()
        return fcf
    }

    @Override
    override fun getRetryLabel(): Label? {
        return begin
    }

    @Override
    override fun getLabel(): String? {
        return null
    }

    companion object {
        // private static LitString ANY=LitString.toExprString("any", -1);
        private val TO_PAGE_EXCEPTION: Method? = Method("toPageException", Types.PAGE_EXCEPTION, arrayOf<Type?>(Types.THROWABLE))

        // public boolean typeEqual(String type);
        private val TYPE_EQUAL: Method? = Method("typeEqual", Types.BOOLEAN_VALUE, arrayOf<Type?>(Types.STRING))

        // Struct getCatchBlock(PageContext pc);
        private val GET_CATCH_BLOCK: Method? = Method("getCatchBlock", Types.STRUCT, arrayOf<Type?>(Types.PAGE_CONTEXT))

        // void isAbort(e)
        val IS_ABORT: Method? = Method("isAbort", Types.BOOLEAN_VALUE, arrayOf<Type?>(Types.THROWABLE))
        private val SET: Method? = Method("set", Types.OBJECT, arrayOf<Type?>(Types.PAGE_CONTEXT, Types.OBJECT))
        private val REMOVE_EL: Method? = Method("removeEL", Types.OBJECT, arrayOf<Type?>(Types.PAGE_CONTEXT))
        @Throws(TransformerException::class)
        private fun catchBody(bc: BytecodeContext?, adapter: GeneratorAdapter?, ct: Catch?, pe: Int, lRef: Int, caugth: Boolean, store: Boolean) {
            // pc.setCatch(pe,true);
            adapter.loadArg(0)
            adapter.loadLocal(pe)
            adapter.push(caugth)
            adapter.push(store)
            adapter.invokeVirtual(Types.PAGE_CONTEXT, TagTry.SET_CATCH3)

            // ref=
            ct!!.name.writeOut(bc, Expression.MODE_REF)
            adapter.storeLocal(lRef)
            adapter.loadLocal(lRef)
            adapter.loadArg(0)
            adapter.loadLocal(pe) // (...,pe.getCatchBlock(pc))
            adapter.loadArg(0)
            adapter.invokeVirtual(Types.PAGE_EXCEPTION, GET_CATCH_BLOCK)
            adapter.invokeInterface(Types.REFERENCE, SET)
            adapter.pop()
            ct.body.writeOut(bc)
        }
    }

    /**
     * Constructor of the class
     *
     * @param body
     * @param line
     */
    init {
        tryBody = body
        body.setParent(this)
    }
}