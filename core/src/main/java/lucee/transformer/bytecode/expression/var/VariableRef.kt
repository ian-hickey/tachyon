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
package lucee.transformer.bytecode.expression.`var`

import org.objectweb.asm.Type

class VariableRef(variable: Variable?, alwaysLocal: Boolean) : ExpressionBase(variable.getFactory(), variable.getStart(), variable.getEnd()) {
    private val variable: VariableImpl?
    private val alwaysLocal: Boolean

    /**
     *
     * @see lucee.transformer.bytecode.expression.ExpressionBase._writeOut
     */
    @Override
    @Throws(TransformerException::class)
    fun _writeOut(bc: BytecodeContext?, mode: Int): Type? {
        val adapter: GeneratorAdapter = bc.getAdapter()
        val count: Int = variable!!.countFM + variable!!.countDM
        for (i in 0..count) {
            adapter.loadArg(0)
        }
        var scope: Int = variable!!.getScope()
        if (alwaysLocal && scope == Scope.SCOPE_UNDEFINED) scope = TypeScope.SCOPE_UNDEFINED_LOCAL
        TypeScope.invokeScope(adapter, scope)
        var isLast: Boolean
        for (i in 0 until count) {
            isLast = i + 1 == count
            getFactory().registerKey(bc, (variable!!.members!!.get(i) as DataMember).getName(), false)
            adapter.invokeVirtual(Types.PAGE_CONTEXT, if (isLast) GET_REFERENCE_KEY else TOUCH_KEY)
        }
        return Types.REFERENCE
    }

    companion object {
        // Object touch (Object,Key)
        private val TOUCH_KEY: Method? = Method("touch", Types.OBJECT, arrayOf<Type?>(Types.OBJECT, Types.COLLECTION_KEY))

        // lucee.runtime.type.ref.Reference getReference (Object,Key)
        private val GET_REFERENCE_KEY: Method? = Method("getReference", Types.REFERENCE, arrayOf<Type?>(Types.OBJECT, Types.COLLECTION_KEY))
    }

    init {
        this.variable = variable
        this.alwaysLocal = alwaysLocal
    }
}