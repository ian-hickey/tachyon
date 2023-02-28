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

class Condition : StatementBaseNoFinal, HasBodies {
    private val ifs: ArrayList<Pair?>? = ArrayList<Pair?>()
    private var _else: Pair? = null

    /**
     * Constructor of the class
     *
     * @param condition
     * @param body
     * @param line
     */
    constructor(f: Factory?, start: Position?, end: Position?) : super(f, start, end) {}

    /**
     * Constructor of the class
     *
     * @param condition
     * @param body
     * @param line
     */
    constructor(f: Factory?, condition: ExprBoolean?, body: Statement?, start: Position?, end: Position?) : super(condition.getFactory(), start, end) {
        addElseIf(condition, body, start, end)
        body.setParent(this)
    }

    constructor(b: Boolean, body: Statement?, start: Position?, end: Position?) : this(body.getFactory(), body.getFactory().createLitBoolean(b), body, start, end) {}

    /**
     * adds an else statement
     *
     * @param condition
     * @param body
     */
    fun addElseIf(condition: ExprBoolean?, body: Statement?, start: Position?, end: Position?): Pair? {
        var pair: Pair?
        ifs.add(Pair(condition, body, start, end).also { pair = it })
        body.setParent(this)
        return pair
    }

    /**
     * sets the else Block of the condition
     *
     * @param body
     */
    fun setElse(body: Statement?, start: Position?, end: Position?): Pair? {
        _else = Pair(null, body, start, end)
        body.setParent(this)
        return _else
    }

    inner class Pair(condition: ExprBoolean?, body: Statement?, start: Position?, end: Position?) {
        val condition: ExprBoolean?
        val body: Statement?
        val start: Position?
        var end: Position?

        init {
            this.condition = condition
            this.body = body
            this.start = start
            this.end = end
        }
    }

    @Override
    @Throws(TransformerException::class)
    override fun _writeOut(bc: BytecodeContext?) {
        val it: Iterator<Pair?> = ifs.iterator()
        var pair: Pair?
        val cv = ConditionVisitor()
        cv.visitBefore()
        // ifs
        while (it.hasNext()) {
            pair = it.next()
            ExpressionUtil.visitLine(bc, pair!!.start)
            cv.visitWhenBeforeExpr()
            pair.condition.writeOut(bc, Expression.MODE_VALUE)
            cv.visitWhenAfterExprBeforeBody(bc)
            pair.body.writeOut(bc)
            cv.visitWhenAfterBody(bc)
            if (pair.end != null) ExpressionUtil.visitLine(bc, pair.end)
        }
        // else
        if (_else != null && _else!!.body != null) {
            cv.visitOtherviseBeforeBody()
            _else!!.body.writeOut(bc)
            cv.visitOtherviseAfterBody()
        }
        cv.visitAfter(bc)
    }

    /**
     * @see tachyon.transformer.bytecode.statement.HasBodies.getBodies
     */
    @Override
    override fun getBodies(): Array<Body?>? {
        var len: Int = ifs.size()
        var count = 0
        if (_else != null) len++
        val bodies: Array<Body?> = arrayOfNulls<Body?>(len)
        var p: Pair
        val it: Iterator<Pair?> = ifs.iterator()
        while (it.hasNext()) {
            p = it.next()
            bodies[count++] = p.body as Body?
        }
        if (_else != null) bodies[count++] = _else!!.body as Body?
        return bodies
    }
}