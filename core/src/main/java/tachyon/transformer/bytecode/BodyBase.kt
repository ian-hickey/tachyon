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
package tachyon.transformer.bytecode

import java.util.ArrayList

/**
 * Base Body implementation
 */
class BodyBase
/**
 * Constructor of the class
 */
(f: Factory?) : StatementBaseNoFinal(f, null, null), Body {
    private val statements: LinkedList<Statement?>? = LinkedList<Statement?>()
    private var last: Statement? = null

    /**
     *
     * @see tachyon.transformer.bytecode.Body.addStatement
     */
    @Override
    override fun addStatement(statement: Statement?) {
        if (statement is PrintOut) {
            val expr: Expression = (statement as PrintOut?).getExpr()
            if (expr is LitString && concatPrintouts((expr as LitString).getString())) return
        }
        statement!!.setParent(this)
        statements.add(statement)
        last = statement
    }

    @Override
    override fun addFirst(statement: Statement?) {
        statement!!.setParent(this)
        statements.add(0, statement)
    }

    @Override
    override fun remove(statement: Statement?) {
        statement!!.setParent(null)
        statements.remove(statement)
    }

    /**
     *
     * @see tachyon.transformer.bytecode.Body.getStatements
     */
    @Override
    override fun getStatements(): List<Statement?>? {
        return statements
    }

    @Override
    override fun hasStatements(): Boolean {
        return !statements.isEmpty()
    }

    /**
     *
     * @see tachyon.transformer.bytecode.Body.moveStatmentsTo
     */
    @Override
    override fun moveStatmentsTo(trg: Body?) {
        val it: Iterator<Statement?> = statements.iterator()
        while (it.hasNext()) {
            val stat: Statement? = it.next()
            stat!!.setParent(trg)
            trg!!.getStatements().add(stat)
        }
        statements.clear()
    }

    @Override
    override fun addPrintOut(f: Factory?, str: String?, start: Position?, end: Position?) {
        if (concatPrintouts(str)) return
        last = PrintOut(f.createLitString(str, start, end), start, end)
        last!!.setParent(this)
        statements.add(last)
    }

    private fun concatPrintouts(str: String?): Boolean {
        if (last is PrintOut) {
            val po: PrintOut? = last as PrintOut?
            val expr: Expression = po.getExpr()
            if (expr is LitString) {
                val lit: LitString = expr as LitString
                if (lit.getString().length() < 1024) {
                    po.setExpr(lit.getFactory().createLitString(lit.getString().concat(str), lit.getStart(), lit.getEnd()))
                    return true
                }
            }
        }
        return false
    }

    @Override
    @Throws(TransformerException::class)
    fun _writeOut(bc: BytecodeContext?) {
        writeOut(bc, this)
    }

    /**
     *
     * @see tachyon.transformer.bytecode.Body.isEmpty
     */
    @Override
    override fun isEmpty(): Boolean {
        return statements.isEmpty()
    }

    companion object {
        private const val counter: Long = 0

        // private int count=-1;
        private const val MAX_STATEMENTS = 206
        @Throws(TransformerException::class)
        fun writeOut(bc: BytecodeContext?, body: Body?) {
            writeOut(bc, body!!.getStatements())
        }

        @Throws(TransformerException::class)
        fun writeOut(bc: BytecodeContext?, statements: List<Statement?>?) {
            val adapter: GeneratorAdapter = bc!!.getAdapter()
            var isOutsideMethod: Boolean
            var a: GeneratorAdapter? = null
            var m: Method?
            var _bc: BytecodeContext? = bc
            val it: Iterator<Statement?> = statements!!.iterator()
            val split: Boolean = bc!!.getPage()!!.getSplitIfNecessary()

            // int lastLine=-1;
            while (it.hasNext()) {
                isOutsideMethod = bc!!.getMethod().getReturnType().equals(Types.VOID)
                val s: Statement? = it.next()
                if (split && _bc!!.incCount() > MAX_STATEMENTS && bc!!.doSubFunctions() && (isOutsideMethod || !s!!.hasFlowController()) && s!!.getStart() != null) {
                    if (a != null) {
                        a.returnValue()
                        a.endMethod()
                    }
                    // ExpressionUtil.visitLine(bc, s.getLine());
                    val method: String = ASMUtil.createOverfowMethod(bc!!.getMethod().getName(), bc!!.getPage()!!.getMethodCount())
                    ExpressionUtil.visitLine(bc, s!!.getStart())
                    // ExpressionUtil.lastLine(bc);
                    m = Method(method, Types.VOID, arrayOf<Type?>(Types.PAGE_CONTEXT))
                    a = GeneratorAdapter(Opcodes.ACC_PRIVATE + Opcodes.ACC_FINAL, m, null, arrayOf<Type?>(Types.THROWABLE), bc!!.getClassWriter())
                    _bc = BytecodeContext(bc!!.getConstructor(), bc!!.getKeys(), bc, a, m)
                    if (bc!!.getRoot() != null) _bc.setRoot(bc!!.getRoot()) else _bc.setRoot(bc)
                    adapter.visitVarInsn(Opcodes.ALOAD, 0)
                    adapter.visitVarInsn(Opcodes.ALOAD, 1)
                    adapter.visitMethodInsn(Opcodes.INVOKEVIRTUAL, bc!!.getClassName(), method, "(Ltachyon/runtime/PageContext;)V")
                }
                if (_bc !== bc && s!!.hasFlowController()) {
                    if (a != null) {
                        a.returnValue()
                        a.endMethod()
                    }
                    _bc = bc
                    a = null
                }
                ExpressionUtil.writeOut(s, _bc)
            }
            if (a != null) {
                a.returnValue()
                a.endMethod()
            }
        }

        @Throws(TransformerException::class)
        fun writeOutNew(bc: BytecodeContext?, statements: List<Statement?>?) {
            if (statements == null || statements.size() === 0) return
            var s: Statement
            val it: Iterator<Statement?> = statements.iterator()
            val isVoidMethod: Boolean = bc!!.getMethod().getReturnType().equals(Types.VOID)
            val split: Boolean = bc!!.getPage()!!.getSplitIfNecessary()

            // split
            if (split && isVoidMethod && statements.size() > 1 && bc!!.doSubFunctions()) {
                var collectionSize: Int = statements.size() / 10
                if (collectionSize < 1) collectionSize = 1
                val _statements: List<Statement?> = ArrayList<Statement?>()
                while (it.hasNext()) {
                    s = it.next()
                    if (s!!.hasFlowController()) {
                        // add existing statements to sub method
                        if (_statements.size() > 0) {
                            addToSubMethod(bc, _statements.toArray(arrayOfNulls<Statement?>(_statements.size())))
                            _statements.clear()
                        }
                        ExpressionUtil.writeOut(s, bc)
                    } else {
                        _statements.add(s)
                        if (_statements.size() >= collectionSize) {
                            if (_statements.size() <= 10 && ASMUtil.count(_statements, true) <= 20) {
                                val _it: Iterator<Statement?> = _statements.iterator()
                                while (_it.hasNext()) ExpressionUtil.writeOut(_it.next(), bc)
                            } else addToSubMethod(bc, _statements.toArray(arrayOfNulls<Statement?>(_statements.size())))
                            _statements.clear()
                        }
                    }
                }
                if (_statements.size() > 0) addToSubMethod(bc, _statements.toArray(arrayOfNulls<Statement?>(_statements.size())))
            } else {
                while (it.hasNext()) {
                    ExpressionUtil.writeOut(it.next(), bc)
                }
            }
        }

        @Throws(TransformerException::class)
        private fun addToSubMethod(bc: BytecodeContext?, vararg statements: Statement?) {
            if (statements == null || statements.size == 0) return
            val adapter: GeneratorAdapter = bc!!.getAdapter()
            val method: String = ASMUtil.createOverfowMethod(bc!!.getMethod().getName(), bc!!.getPage()!!.getMethodCount())
            for (i in 0 until statements.size) {
                if (statements[i]!!.getStart() != null) {
                    ExpressionUtil.visitLine(bc, statements[i]!!.getStart())
                    break
                }
            }

            // ExpressionUtil.lastLine(bc);
            val m = Method(method, Types.VOID, arrayOf<Type?>(Types.PAGE_CONTEXT))
            val a = GeneratorAdapter(Opcodes.ACC_PRIVATE + Opcodes.ACC_FINAL, m, null, arrayOf<Type?>(Types.THROWABLE), bc!!.getClassWriter())
            val _bc = BytecodeContext(bc!!.getConstructor(), bc!!.getKeys(), bc, a, m)
            if (bc!!.getRoot() != null) _bc!!.setRoot(bc!!.getRoot()) else _bc!!.setRoot(bc)
            adapter.visitVarInsn(Opcodes.ALOAD, 0)
            adapter.visitVarInsn(Opcodes.ALOAD, 1)
            adapter.visitMethodInsn(Opcodes.INVOKEVIRTUAL, bc!!.getClassName(), method, "(Ltachyon/runtime/PageContext;)V")
            for (i in 0 until statements.size) {
                ExpressionUtil.writeOut(statements[i], _bc)
            }
            a.returnValue()
            a.endMethod()
        }
    }
}