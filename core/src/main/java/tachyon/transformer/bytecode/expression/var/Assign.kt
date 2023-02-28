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
package tachyon.transformer.bytecode.expression.`var`

import org.objectweb.asm.Type

class Assign(variable: Variable?, value: Expression?, end: Position?) : ExpressionBase(variable.getFactory(), variable.getStart(), end) {
    private val variable: Variable?
    private val value: Expression?
    private var access = -1
    private var modifier = 0
    @Override
    @Throws(TransformerException::class)
    fun _writeOut(bc: BytecodeContext?, mode: Int): Type? {
        val adapter: GeneratorAdapter = bc.getAdapter()
        val count: Int = variable.getCount()
        // count 0
        if (count == 0) {
            return if (variable.ignoredFirstMember() && variable.getScope() === Scope.SCOPE_VAR) {
                // print.dumpStack();
                Types.VOID
            } else _writeOutEmpty(bc)
        }
        val doOnlyScope = variable.getScope() === Scope.SCOPE_LOCAL
        var rtn: Type? = Types.OBJECT
        // boolean last;
        for (i in if (doOnlyScope) 0 else 1 until count) {
            adapter.loadArg(0)
        }
        rtn = _writeOutFirst(bc, variable.getMembers().get(0), mode, count == 1, doOnlyScope)

        // pc.get(
        for (i in if (doOnlyScope) 0 else 1 until count) {
            val member: Member = variable.getMembers().get(i)
            val last = i + 1 == count

            // Data Member
            if (member is DataMember) {
                // ((DataMember)member).getName().writeOut(bc, MODE_REF);
                getFactory().registerKey(bc, (member as DataMember).getName(), false)
                if (last) writeValue(bc)
                adapter.invokeVirtual(Types.PAGE_CONTEXT, if (last) SET_KEY else TOUCH_KEY)
                rtn = Types.OBJECT
            } else if (member is UDF) {
                if (last) throw TransformerException(bc, "can't assign value to a user defined function", getStart())
                val udf: UDF = member
                getFactory().registerKey(bc, udf!!.getName(), false)
                ExpressionUtil.writeOutExpressionArray(bc, Types.OBJECT, udf!!.getArguments())
                adapter.invokeVirtual(Types.PAGE_CONTEXT, if (udf!!.hasNamedArgs()) GET_FUNCTION_WITH_NAMED_ARGS_KEY else GET_FUNCTION_KEY)
                rtn = Types.OBJECT
            }
        }
        return rtn
    }

    @Throws(TransformerException::class)
    private fun writeValue(bc: BytecodeContext?) {
        // set Access
        if (access > -1 || modifier > 0) {
            val ga: GeneratorAdapter = bc.getAdapter()
            ga.newInstance(Types.DATA_MEMBER)
            ga.dup()
            ga.push(access)
            ga.push(modifier)
            value.writeOut(bc, MODE_REF)
            ga.invokeConstructor(Types.DATA_MEMBER, DATA_MEMBER_INIT)
        } else value.writeOut(bc, MODE_REF)
    }

    @Throws(TransformerException::class)
    private fun _writeOutFirst(bc: BytecodeContext?, member: Member?, mode: Int, last: Boolean, doOnlyScope: Boolean): Type? {
        return if (member is DataMember) {
            _writeOutOneDataMember(bc, member as DataMember?, last, doOnlyScope)
            // return Variable._writeOutFirstDataMember(adapter,(DataMember)member,variable.scope, last);
        } else if (member is UDF) {
            if (last) throw TransformerException(bc, "can't assign value to a user defined function", getStart())
            VariableImpl._writeOutFirstUDF(bc, member as UDF?, variable.getScope(), doOnlyScope)
        } else {
            if (last) throw TransformerException(bc, "can't assign value to a built in function", getStart())
            VariableImpl._writeOutFirstBIF(bc, member as BIF?, mode, last, getStart())
        }
    }

    @Throws(TransformerException::class)
    private fun _writeOutOneDataMember(bc: BytecodeContext?, member: DataMember?, last: Boolean, doOnlyScope: Boolean): Type? {
        val adapter: GeneratorAdapter = bc.getAdapter()
        if (doOnlyScope) {
            adapter.loadArg(0)
            return if (variable.getScope() === Scope.SCOPE_LOCAL) {
                TypeScope.invokeScope(adapter, TypeScope.METHOD_LOCAL_TOUCH, Types.PAGE_CONTEXT)
            } else TypeScope.invokeScope(adapter, variable.getScope())
        }

        // pc.get
        adapter.loadArg(0)
        if (last) {
            TypeScope.invokeScope(adapter, variable.getScope())
            getFactory().registerKey(bc, member.getName(), false)
            writeValue(bc)
            adapter.invokeInterface(TypeScope.SCOPES.get(variable.getScope()), METHOD_SCOPE_SET_KEY)
        } else {
            adapter.loadArg(0)
            TypeScope.invokeScope(adapter, variable.getScope())
            getFactory().registerKey(bc, member.getName(), false)
            adapter.invokeVirtual(Types.PAGE_CONTEXT, TOUCH_KEY)
        }
        return Types.OBJECT
    }

    @Throws(TransformerException::class)
    private fun _writeOutEmpty(bc: BytecodeContext?): Type? {
        val adapter: GeneratorAdapter = bc.getAdapter()
        if (variable.getScope() === Scope.SCOPE_ARGUMENTS) {
            adapter.loadArg(0)
            TypeScope.invokeScope(adapter, Scope.SCOPE_ARGUMENTS)
            writeValue(bc)
            adapter.invokeInterface(TypeScope.SCOPE_ARGUMENT, SET_ARGUMENT)
        } else {
            adapter.loadArg(0)
            TypeScope.invokeScope(adapter, Scope.SCOPE_UNDEFINED)
            getFactory().registerKey(bc, bc.getFactory().createLitString(ScopeFactory.toStringScope(variable.getScope(), "undefined")), false)
            writeValue(bc)
            adapter.invokeInterface(TypeScope.SCOPES.get(Scope.SCOPE_UNDEFINED), METHOD_SCOPE_SET_KEY)
        }
        return Types.OBJECT
    }

    /**
     * @return the value
     */
    fun getValue(): Expression? {
        return value
    }

    /**
     * @return the variable
     */
    fun getVariable(): Variable? {
        return variable
    }

    fun setAccess(access: Int) {
        this.access = access
    }

    fun getAccess(): Int {
        return access
    }

    fun setModifier(modifier: Int) {
        this.modifier = modifier
    }

    fun getModifier(): Int {
        return modifier
    }

    fun setFinal(_final: Boolean) {
        // TODO Auto-generated method stub
    }

    companion object {
        // java.lang.Object set(String,Object)
        private val METHOD_SCOPE_SET_KEY: Method? = Method("set", Types.OBJECT, arrayOf<Type?>(Types.COLLECTION_KEY, Types.OBJECT))

        // .setArgument(obj)
        private val SET_ARGUMENT: Method? = Method("setArgument", Types.OBJECT, arrayOf<Type?>(Types.OBJECT))

        // Object touch (Object,String)
        private val TOUCH_KEY: Method? = Method("touch", Types.OBJECT, arrayOf<Type?>(Types.OBJECT, Types.COLLECTION_KEY))

        // Object set (Object,String,Object)
        private val SET_KEY: Method? = Method("set", Types.OBJECT, arrayOf<Type?>(Types.OBJECT, Types.COLLECTION_KEY, Types.OBJECT))

        // Object getFunction (Object,String,Object[])
        private val GET_FUNCTION_KEY: Method? = Method("getFunction", Types.OBJECT, arrayOf<Type?>(Types.OBJECT, Types.COLLECTION_KEY, Types.OBJECT_ARRAY))

        // Object getFunctionWithNamedValues (Object,String,Object[])
        private val GET_FUNCTION_WITH_NAMED_ARGS_KEY: Method? = Method("getFunctionWithNamedValues", Types.OBJECT, arrayOf<Type?>(Types.OBJECT, Types.COLLECTION_KEY, Types.OBJECT_ARRAY))
        private val DATA_MEMBER_INIT: Method? = Method("<init>", Types.VOID, arrayOf<Type?>(Types.INT_VALUE, Types.INT_VALUE, Types.OBJECT))
    }

    /**
     * Constructor of the class
     *
     * @param variable
     * @param value
     */
    init {
        this.variable = variable
        this.value = value
        if (value is Variable) (value as Variable?).assign(this)
        // this.returnOldValue=returnOldValue;
    }
}