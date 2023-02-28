/**
 *
 * Copyright (c) 2014, the Railo Company Ltd. All rights reserved.
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
package tachyon.transformer.bytecode.expression.`var`

import org.objectweb.asm.Type

class DynAssign : ExpressionBase {
    private var name: ExprString? = null
    private var value: Expression? = null

    constructor(f: Factory?, start: Position?, end: Position?) : super(f, start, end) {}

    /**
     * Constructor of the class
     *
     * @param name
     * @param value
     */
    constructor(name: Expression?, value: Expression?) : super(name.getFactory(), name.getStart(), name.getEnd()) {
        this.name = name.getFactory().toExprString(name)
        this.value = value
    }

    @Override
    @Throws(TransformerException::class)
    fun _writeOut(bc: BytecodeContext?, mode: Int): Type? {
        val adapter: GeneratorAdapter = bc.getAdapter()
        adapter.loadArg(0)
        name.writeOut(bc, Expression.MODE_REF)
        value.writeOut(bc, Expression.MODE_REF)
        adapter.invokeVirtual(Types.PAGE_CONTEXT, METHOD_SET_VARIABLE)
        return Types.OBJECT
    }
    /*
	 * *
	 *
	 * @see tachyon.transformer.bytecode.expression.Expression#getType() / public int getType() { return
	 * Types._OBJECT; }
	 */
    /**
     * @return the name
     */
    fun getName(): ExprString? {
        return name
    }

    /**
     * @return the value
     */
    fun getValue(): Expression? {
        return value
    }

    companion object {
        // Object setVariable(String, Object)
        private val METHOD_SET_VARIABLE: Method? = Method("setVariable", Types.OBJECT, arrayOf<Type?>(Types.STRING, Types.OBJECT))
    }
}