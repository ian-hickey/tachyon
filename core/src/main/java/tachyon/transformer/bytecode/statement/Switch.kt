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

import java.util.ArrayList

class Switch(expr: Expression?, start: Position?, end: Position?) : StatementBaseNoFinal(expr.getFactory(), start, end), FlowControlBreak, HasBodies {
    private val cases: List<Case?>? = ArrayList<Case?>()
    private var defaultCase: Body? = null
    private val expr: Expression?
    private var ns: NativeSwitch? = null
    fun addCase(expr: Expression?, body: Body?) {
        addCase(expr, body, null, null)
    }

    fun addCase(expr: Expression?, body: Body?, start: Position?, end: Position?) {
        // if(cases==null) cases=new ArrayList();
        cases.add(Case(expr, body, start, end))
        body.setParent(this)
    }

    fun setDefaultCase(body: Body?) {
        defaultCase = body
        body.setParent(this)
    }

    inner class Case(expression: Expression?, body: Body?, start: Position?, end: Position?) {
        val expression: Expression?
        val body: Body?
        val startPos: Position?
        val endPos: Position?

        init {
            this.expression = expression
            this.body = body
            startPos = start
            endPos = end
        }
    }

    @Override
    @Throws(TransformerException::class)
    override fun _writeOut(bc: BytecodeContext?) {
        val adapter: GeneratorAdapter = bc.getAdapter()

        // Array cases=new ArrayImpl();
        val array: Int = adapter.newLocal(Types.ARRAY)
        adapter.newInstance(Types.ARRAY_IMPL)
        adapter.dup()
        adapter.invokeConstructor(Types.ARRAY_IMPL, INIT)
        adapter.storeLocal(array)

        // cases.append(case.value);
        var it = cases!!.iterator()
        var c: Case?
        while (it.hasNext()) {
            c = it.next()
            adapter.loadLocal(array)
            c!!.expression.writeOut(bc, Expression.MODE_REF)
            adapter.invokeVirtual(Types.ARRAY_IMPL, APPEND)
            adapter.pop()
        }

        // int result=ArrayUtil.find(array,expression);
        val result: Int = adapter.newLocal(Types.INT_VALUE)
        adapter.loadLocal(array)
        expr.writeOut(bc, Expression.MODE_REF)
        adapter.invokeStatic(Types.ARRAY_UTIL, FIND)
        adapter.storeLocal(result)

        // switch(result)
        ns = NativeSwitch(bc.getFactory(), result, NativeSwitch.LOCAL_REF, getStart(), getEnd())
        it = cases.iterator()
        var count = 1
        while (it.hasNext()) {
            c = it.next()
            ns.addCase(count++, c!!.body, c.startPos, c.endPos, false)
        }
        if (defaultCase != null) ns.addDefaultCase(defaultCase)
        ns.writeOut(bc)
    }

    /**
     *
     * @see tachyon.transformer.bytecode.statement.FlowControl.getBreakLabel
     */
    @Override
    override fun getBreakLabel(): Label? {
        return ns!!.getBreakLabel()
    }

    /**
     *
     * @see tachyon.transformer.bytecode.statement.FlowControl.getContinueLabel
     */
    fun getContinueLabel(): Label? {
        return ns!!.getContinueLabel()
    }

    /**
     * @see tachyon.transformer.bytecode.statement.HasBodies.getBodies
     */
    @Override
    override fun getBodies(): Array<Body?>? {
        if (cases == null) {
            return if (defaultCase != null) arrayOf<Body?>(defaultCase) else arrayOf<Body?>()
        }
        var len: Int = cases.size()
        var count = 0
        if (defaultCase != null) len++
        val bodies: Array<Body?> = arrayOfNulls<Body?>(len)
        var c: Case
        val it = cases.iterator()
        while (it.hasNext()) {
            c = it.next()
            bodies[count++] = c.body
        }
        if (defaultCase != null) bodies[count++] = defaultCase
        return bodies
    }

    @Override
    override fun getLabel(): String? {
        return null
    }

    companion object {
        // Object append(Object o)
        private val APPEND: Method? = Method("append", Types.OBJECT, arrayOf<Type?>(Types.OBJECT))
        val INIT: Method? = Method("<init>", Types.VOID, arrayOf<Type?>())

        // int find(Array array, Object object)
        private val FIND: Method? = Method("find", Types.INT_VALUE, arrayOf<Type?>(Types.ARRAY, Types.OBJECT))
    }

    init {
        this.expr = expr
    }
}