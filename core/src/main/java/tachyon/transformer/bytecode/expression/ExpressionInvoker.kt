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
package tachyon.transformer.bytecode.expression

import java.util.ArrayList

class ExpressionInvoker(expr: Expression?) : ExpressionBase(expr.getFactory(), expr.getStart(), expr.getEnd()), Invoker {
    private val expr: Expression?
    private val members: List<Member?>? = ArrayList<Member?>()

    @Override
    @Throws(TransformerException::class)
    override fun _writeOut(bc: BytecodeContext?, mode: Int): Type? {
        val adapter: GeneratorAdapter = bc.getAdapter()
        var rtn: Type = Types.OBJECT
        val count: Int = members!!.size()
        for (i in 0 until count) {
            adapter.loadArg(0)
        }
        expr.writeOut(bc, Expression.MODE_REF)
        for (i in 0 until count) {
            val member: Member? = members[i]

            // Data Member
            if (member is DataMember) {
                (member as DataMember?).getName().writeOut(bc, MODE_REF)
                adapter.invokeVirtual(Types.PAGE_CONTEXT, if (i + 1 == count) GET else GET_COLLECTION)
                rtn = Types.OBJECT
            } else if (member is UDF) {
                val udf: UDF? = member as UDF?
                udf.getName().writeOut(bc, MODE_REF)
                ExpressionUtil.writeOutExpressionArray(bc, Types.OBJECT, udf.getArguments())
                adapter.invokeVirtual(Types.PAGE_CONTEXT, if (udf.hasNamedArgs()) GET_FUNCTION_WITH_NAMED_ARGS else GET_FUNCTION)
                rtn = Types.OBJECT
            }
        }
        return rtn
    }

    /**
     *
     * @see tachyon.transformer.expression.Invoker.addMember
     */
    @Override
    fun addMember(member: Member?) {
        members.add(member)
    }

    /**
     *
     * @see tachyon.transformer.expression.Invoker.getMembers
     */
    @Override
    fun getMembers(): List<Member?>? {
        return members
    }

    @Override
    fun removeMember(index: Int): Member? {
        return members.remove(index)
    }

    companion object {
        // Object getCollection (Object,String)
        private val GET_COLLECTION: Method? = Method("getCollection", Types.OBJECT, arrayOf<Type?>(Types.OBJECT, Types.STRING))

        // Object get (Object,String)
        private val GET: Method? = Method("get", Types.OBJECT, arrayOf<Type?>(Types.OBJECT, Types.STRING))

        // Object getFunction (Object,String,Object[])
        private val GET_FUNCTION: Method? = Method("getFunction", Types.OBJECT, arrayOf<Type?>(Types.OBJECT, Types.STRING, Types.OBJECT_ARRAY))

        // Object getFunctionWithNamedValues (Object,String,Object[])
        private val GET_FUNCTION_WITH_NAMED_ARGS: Method? = Method("getFunctionWithNamedValues", Types.OBJECT, arrayOf<Type?>(Types.OBJECT, Types.STRING, Types.OBJECT_ARRAY))
    }

    init {
        this.expr = expr
    }
}