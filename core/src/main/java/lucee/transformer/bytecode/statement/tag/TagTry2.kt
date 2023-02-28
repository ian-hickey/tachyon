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
package lucee.transformer.bytecode.statement.tag

import java.util.ArrayList

class TagTry2(f: Factory?, start: Position?, end: Position?) : TagBase(f, start, end), FlowControlRetry {
    private var fcf: FlowControlFinal? = null
    private var checked = false
    private val begin: Label? = Label()

    @Override
    @Throws(TransformerException::class)
    override fun _writeOut(bc: BytecodeContext?) {
        val adapter: GeneratorAdapter = bc.getAdapter()
        adapter.visitLabel(begin)
        val tryBody: Body = BodyBase(getFactory())
        val catches: List<Tag?> = ArrayList<Tag?>()
        var tmpFinal: Tag? = null
        tryBody.setParent(getBody().getParent())
        val statements: List<Statement?> = getBody().getStatements()
        var stat: Statement
        var tag: Tag
        run {
            val it: Iterator<Statement?> = statements.iterator()
            while (it.hasNext()) {
                stat = it.next()
                if (stat is Tag) {
                    tag = stat
                    if (tag!!.getTagLibTag().getTagClassDefinition().isClassNameEqualTo("lucee.runtime.tag.Catch")) {
                        catches.add(tag)
                        continue
                    } else if (tag!!.getTagLibTag().getTagClassDefinition().isClassNameEqualTo("lucee.runtime.tag.Finally")) {
                        tmpFinal = tag
                        continue
                    }
                }
                tryBody.addStatement(stat)
            }
        }
        val _finally: Tag? = tmpFinal

        // has no try body, if there is no try body, no catches are executed, only finally
        if (!tryBody.hasStatements()) {
            if (_finally != null && _finally.getBody() != null) {
                BodyBase.writeOut(bc, _finally.getBody())
                // ExpressionUtil.writeOut(_finally.getBody(), bc);
            }
            return
        }
        val tcfv = TryCatchFinallyVisitor(object : OnFinally() {
            @Override
            @Throws(TransformerException::class)
            fun _writeOut(bc: BytecodeContext?) {
                if (_finally != null) {
                    ExpressionUtil.visitLine(bc, _finally.getStart())
                    BodyBase.writeOut(bc, _finally.getBody())
                    // ExpressionUtil.writeOut(_finally.getBody(), bc);
                }
            }
        }, getFlowControlFinal())

        // Try
        tcfv.visitTryBegin(bc)
        BodyBase.writeOut(bc, tryBody)
        // ExpressionUtil.writeOut(tryBody, bc);
        val e: Int = tcfv.visitTryEndCatchBeging(bc)
        // if(e instanceof lucee.runtime.exp.Abort) throw e;
        val abortEnd = Label()
        adapter.loadLocal(e)
        // Abort.isAbort(t);
        adapter.invokeStatic(Types.ABORT, TryCatchFinally.IS_ABORT)
        // adapter.instanceOf(Types.ABORT);
        adapter.ifZCmp(Opcodes.IFEQ, abortEnd)
        adapter.loadLocal(e)
        adapter.throwException()
        adapter.visitLabel(abortEnd)

        // PageExceptionImpl old=pc.getCatch();
        val oldPE: Int = adapter.newLocal(Types.PAGE_EXCEPTION)
        val oldName: Int = adapter.newLocal(Types.STRING)
        adapter.loadArg(0)
        adapter.invokeVirtual(Types.PAGE_CONTEXT, GET_CATCH)
        adapter.storeLocal(oldPE)
        adapter.loadArg(0)
        adapter.checkCast(Types.PAGE_CONTEXT_IMPL)
        adapter.invokeVirtual(Types.PAGE_CONTEXT_IMPL, GET_CATCH_NAME)
        adapter.storeLocal(oldName)

        // PageException pe=Caster.toPageEception(e);
        val pe: Int = adapter.newLocal(Types.PAGE_EXCEPTION)
        adapter.loadLocal(e)
        adapter.invokeStatic(Types.CASTER, TO_PAGE_EXCEPTION)
        adapter.storeLocal(pe)
        val it: Iterator<Tag?> = catches.iterator()
        var attrType: Attribute
        var type: Expression
        val endAllIfs = Label()
        var tagElse: Tag? = null
        while (it.hasNext()) {
            tag = it.next()
            val endIf = Label()

            // type
            attrType = tag!!.getAttribute("type")
            type = bc.getFactory().createLitString("any")
            if (attrType != null) type = attrType.getValue()
            if (type is LitString && (type as LitString).getString().equalsIgnoreCase("any")) {
                tagElse = tag
                continue
            }
            ExpressionUtil.visitLine(bc, tag.getStart())

            // if(pe.typeEqual(@type)
            adapter.loadLocal(pe)
            type.writeOut(bc, Expression.MODE_REF)
            adapter.invokeVirtual(Types.PAGE_EXCEPTION, TYPE_EQUAL)
            adapter.ifZCmp(Opcodes.IFEQ, endIf)
            catchBody(bc, adapter, tag, pe, true, true, extractName(tag))
            adapter.visitJumpInsn(Opcodes.GOTO, endAllIfs)
            adapter.visitLabel(endIf)
        }
        // else
        if (tagElse != null) {
            catchBody(bc, adapter, tagElse, pe, true, true, extractName(tagElse))
        } else {
            // pc.setCatch(pe,true);
            adapter.loadArg(0)
            adapter.loadLocal(pe)
            adapter.push(false)
            adapter.push(true)
            adapter.invokeVirtual(Types.PAGE_CONTEXT, SET_CATCH3)

            // throw pe;
            adapter.loadLocal(pe)
            adapter.throwException()
        }
        adapter.visitLabel(endAllIfs)
        adapter.loadLocal(oldName)
        val notNull = Label()
        adapter.visitJumpInsn(Opcodes.IFNONNULL, notNull)
        // NULL
        adapter.loadArg(0)
        adapter.loadLocal(oldPE)
        adapter.invokeVirtual(Types.PAGE_CONTEXT, SET_CATCH_PE)
        val end = Label()
        adapter.visitJumpInsn(Opcodes.GOTO, end)
        adapter.visitLabel(notNull)
        // NOT NULL
        adapter.loadArg(0)
        adapter.checkCast(Types.PAGE_CONTEXT_IMPL)
        adapter.loadLocal(oldPE)
        adapter.loadLocal(oldName)
        adapter.invokeVirtual(Types.PAGE_CONTEXT_IMPL, SET_CATCH_PE2)
        adapter.visitLabel(end)
        tcfv.visitCatchEnd(bc)
    }

    private fun extractName(tag: Tag?): Expression? {
        val attrName: Attribute = tag!!.getAttribute("name")
        return if (attrName != null) attrName.getValue() else null
    }

    private fun hasFinally(): Boolean {
        val statements: List<Statement?> = getBody().getStatements()
        var stat: Statement
        var tag: Tag
        val it: Iterator<Statement?> = statements.iterator()
        while (it.hasNext()) {
            stat = it.next()
            if (stat is Tag) {
                tag = stat
                if (tag!!.getTagLibTag().getTagClassDefinition().isClassNameEqualTo("lucee.runtime.tag.Finally")) {
                    return true
                }
            }
        }
        return false
    }

    @Override
    fun getFlowControlFinal(): FlowControlFinal? {
        if (!checked) {
            checked = true
            if (!hasFinally()) return null
            fcf = FlowControlFinalImpl()
        }
        return fcf
    }

    @Override
    fun getRetryLabel(): Label? {
        return begin
    }

    @Override
    fun getLabel(): String? {
        return null
    }

    companion object {
        // private static final ExprString ANY=LitString.toExprString("any");
        private val GET_VARIABLE: Method? = Method("getVariable", Types.OBJECT, arrayOf<Type?>(Types.STRING))
        private val TO_PAGE_EXCEPTION: Method? = Method("toPageException", Types.PAGE_EXCEPTION, arrayOf<Type?>(Types.THROWABLE))
        val SET_CATCH_PE: Method? = Method("setCatch", Types.VOID, arrayOf<Type?>(Types.PAGE_EXCEPTION))
        private val SET_CATCH_PE2: Method? = Method("setCatch", Types.VOID, arrayOf<Type?>(Types.PAGE_EXCEPTION, Types.STRING))
        val SET_CATCH3: Method? = Method("setCatch", Types.VOID, arrayOf<Type?>(Types.PAGE_EXCEPTION, Types.BOOLEAN_VALUE, Types.BOOLEAN_VALUE))
        private val SET_CATCH4: Method? = Method("setCatch", Types.VOID, arrayOf<Type?>(Types.PAGE_EXCEPTION, Types.BOOLEAN_VALUE, Types.BOOLEAN_VALUE, Types.STRING))
        val GET_CATCH: Method? = Method("getCatch", Types.PAGE_EXCEPTION, arrayOf<Type?>())
        val GET_CATCH_NAME: Method? = Method("getCatchName", Types.STRING, arrayOf<Type?>())

        // public boolean typeEqual(String type);
        private val TYPE_EQUAL: Method? = Method("typeEqual", Types.BOOLEAN_VALUE, arrayOf<Type?>(Types.STRING))
        @Throws(TransformerException::class)
        private fun catchBody(bc: BytecodeContext?, adapter: GeneratorAdapter?, tag: Tag?, pe: Int, caugth: Boolean, store: Boolean, name: Expression?) {
            adapter.loadArg(0)
            if (name == null) {
                adapter.loadLocal(pe)
                adapter.push(caugth)
                adapter.push(store)
                adapter.invokeVirtual(Types.PAGE_CONTEXT, SET_CATCH3)
            } else {
                adapter.checkCast(Types.PAGE_CONTEXT_IMPL)
                adapter.loadLocal(pe)
                adapter.push(caugth)
                adapter.push(store)
                name.writeOut(bc, Expression.MODE_REF)
                adapter.invokeVirtual(Types.PAGE_CONTEXT_IMPL, SET_CATCH4)
            }
            BodyBase.writeOut(bc, tag!!.getBody())
        }
    }
}