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
package tachyon.transformer.bytecode.statement.tag

import org.objectweb.asm.Type

class TagInclude(f: Factory?, start: Position?, end: Position?) : TagBaseNoFinal(f, start, end) {
    /**
     * @see tachyon.transformer.bytecode.statement.tag.TagBase._writeOut
     */
    @Override
    @Throws(TransformerException::class)
    override fun _writeOut(bc: BytecodeContext?) {
        var type: Type = Types.PAGE_CONTEXT
        var func: Method? = DO_INCLUDE_RUN_ONCE2

        // cachedwithin
        var cachedwithin: Expression? = null
        var attr: Attribute? = getAttribute("cachedwithin")
        if (attr != null && attr.getValue() != null) {
            cachedwithin = attr.getValue()
            type = Types.PAGE_CONTEXT_IMPL
            func = DO_INCLUDE_RUN_ONCE3
        }
        val adapter: GeneratorAdapter = bc.getAdapter()
        adapter.loadArg(0)
        if (cachedwithin != null) adapter.checkCast(Types.PAGE_CONTEXT_IMPL)

        // template
        getAttribute("template")!!.getValue()!!.writeOut(bc, Expression.MODE_REF)

        // run Once
        attr = getAttribute("runonce")
        val expr: ExprBoolean = if (attr == null) bc.getFactory().FALSE() else bc.getFactory().toExprBoolean(attr.getValue())
        expr.writeOut(bc, Expression.MODE_VALUE)

        // cachedwithin
        if (cachedwithin != null) cachedwithin.writeOut(bc, Expression.MODE_REF)
        adapter.invokeVirtual(type, func)
    }

    companion object {
        private val DO_INCLUDE_RUN_ONCE2: Method? = Method("doInclude", Type.VOID_TYPE, arrayOf<Type?>(Types.STRING, Types.BOOLEAN_VALUE))
        private val DO_INCLUDE_RUN_ONCE3: Method? = Method("doInclude", Type.VOID_TYPE, arrayOf<Type?>(Types.STRING, Types.BOOLEAN_VALUE, Types.OBJECT))
    }
}