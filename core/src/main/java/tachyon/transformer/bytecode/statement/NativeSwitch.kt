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

class NativeSwitch(f: Factory?, private val value: Int, private val type: Short, start: Position?, end: Position?) : StatementBaseNoFinal(f, start, end), FlowControlBreak, FlowControlContinue, HasBodies {
    private var end: Label? = null
    private var defaultCase: Statement? = null
    var cases: List<Case?>? = ArrayList<Case?>()
    private var labels: Array<Label?>? = arrayOfNulls<Label?>(0)
    private var values: IntArray? = IntArray(0)

    @Override
    @Throws(TransformerException::class)
    override fun _writeOut(bc: BytecodeContext?) {
        end = Label()
        val adapter: GeneratorAdapter = bc.getAdapter()
        if (type == LOCAL_REF) adapter.loadLocal(value) else if (type == ARG_REF) adapter.loadArg(value) else adapter.push(value)
        val beforeDefault = Label()
        adapter.visitLookupSwitchInsn(beforeDefault, values, labels)
        val it = cases!!.iterator()
        var c: Case?
        while (it.hasNext()) {
            c = it.next()
            adapter.visitLabel(c!!.label)
            ExpressionUtil.visitLine(bc, c.startPos)
            c.body.writeOut(bc)
            ExpressionUtil.visitLine(bc, c.endPos)
            if (c.doBreak) {
                adapter.goTo(end)
            }
        }
        adapter.visitLabel(beforeDefault)
        if (defaultCase != null) defaultCase.writeOut(bc)
        adapter.visitLabel(end)
    }

    fun addCase(value: Int, body: Statement?, start: Position?, end: Position?, doBreak: Boolean) {
        val nc: Case = Case(value, body, start, end, doBreak)
        val labelsTmp: Array<Label?> = arrayOfNulls<Label?>(cases!!.size() + 1)
        val valuesTmp = IntArray(cases!!.size() + 1)
        var count = 0
        var hasAdd = false
        for (i in labels.indices) {
            if (!hasAdd && nc.value < values!![i]) {
                labelsTmp[count] = nc.label
                valuesTmp[count] = nc.value
                count++
                hasAdd = true
            }
            labelsTmp[count] = labels!![i]
            valuesTmp[count] = values!![i]
            count++
        }
        if (!hasAdd) {
            labelsTmp[labels!!.size] = nc.label
            valuesTmp[values!!.size] = nc.value
        }
        labels = labelsTmp
        values = valuesTmp
        cases.add(nc)
    }

    fun addDefaultCase(defaultStatement: Statement?) {
        defaultCase = defaultStatement
    }

    inner class Case(val value: Int, body: Statement?, startline: Position?, endline: Position?, doBreak: Boolean) {
        var doBreak: Boolean
        val body: Statement?
        val label: Label? = Label()
        val startPos: Position?
        val endPos: Position?

        init {
            this.body = body
            startPos = startline
            endPos = endline
            this.doBreak = doBreak
        }
    }

    /**
     *
     * @see tachyon.transformer.bytecode.statement.FlowControl.getBreakLabel
     */
    @Override
    override fun getBreakLabel(): Label? {
        return end
    }

    /**
     *
     * @see tachyon.transformer.bytecode.statement.FlowControl.getContinueLabel
     */
    @Override
    override fun getContinueLabel(): Label? {
        return end
    }

    /**
     * @see tachyon.transformer.bytecode.statement.HasBodies.getBodies
     */
    @Override
    override fun getBodies(): Array<Body?>? {
        if (cases == null) {
            return if (defaultCase != null) arrayOf<Body?>(defaultCase as Body?) else arrayOf<Body?>()
        }
        var len: Int = cases!!.size()
        var count = 0
        if (defaultCase != null) len++
        val bodies: Array<Body?> = arrayOfNulls<Body?>(len)
        var c: Case
        val it = cases!!.iterator()
        while (it.hasNext()) {
            c = it.next()
            bodies[count++] = c.body as Body?
        }
        if (defaultCase != null) bodies[count++] = defaultCase as Body?
        return bodies
    }

    @Override
    override fun getLabel(): String? {
        return null
    }

    companion object {
        const val LOCAL_REF: Short = 0
        const val ARG_REF: Short = 1
        const val PRIMITIVE: Short = 1
    }
}