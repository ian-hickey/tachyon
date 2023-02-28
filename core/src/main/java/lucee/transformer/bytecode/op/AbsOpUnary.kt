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
package lucee.transformer.bytecode.op

import java.util.Iterator

abstract class AbsOpUnary(`var`: Variable?, value: Expression?, type: Short, operation: Int, start: Position?, end: Position?) : ExpressionBase(`var`.getFactory(), start, end) {
    private val `var`: Variable?
    private var value: Expression?
    private val type: Short
    private val operation: Int
    @Override
    @Throws(TransformerException::class)
    fun _writeOut(bc: BytecodeContext?, mode: Int): Type? {
        val adapter: GeneratorAdapter = bc.getAdapter()
        // convert value
        value = if (operation == Factory.OP_UNARY_CONCAT) `var`.getFactory().toExprString(value) else `var`.getFactory().toExprNumber(value)
        val members: List<Member?> = `var`.getMembers()
        val size: Int = members.size()
        val scope: String = VariableInterpreter.scopeInt2String(`var`.getScope())

        /*
		 * (susi.sorglos++ or variables.susi++)
		 */if (scope == null && size > 1 || scope != null && size > 0) {
            val last: Member = `var`.removeMember(members.size() - 1)
            if (last !is DataMember) throw TransformerException(bc, "you cannot use a unary operator with a function " + last.getClass().getName(), getStart())

            // if (operation == Factory.OP_UNARY_CONCAT || operation == Factory.OP_UNARY_MULTIPLY || operation
            // == Factory.OP_UNARY_PLUS || operation == Factory.OP_UNARY_MINUS
            // || operation == Factory.OP_UNARY_PLUS || operation == Factory.OP_UNARY_MINUS || operation ==
            // Factory.OP_UNARY_DIVIDE)
            adapter.loadArg(0)

            // write the variable
            `var`.setAsCollection(Boolean.TRUE)
            `var`.writeOut(bc, mode)

            // write out last Key
            getFactory().registerKey(bc, (last as DataMember).getName(), false)

            // write out value
            value.writeOut(bc, MODE_REF)
            if (type == Factory.OP_UNARY_POST) {
                if (operation != Factory.OP_UNARY_PLUS && operation != Factory.OP_UNARY_MINUS) throw TransformerException(bc, "Post only possible with plus or minus $operation", value.getStart())
                if (operation == Factory.OP_UNARY_PLUS) adapter.invokeStatic(Types.OP_UTIL, UNARY_POST_PLUS4) else if (operation == Factory.OP_UNARY_MINUS) adapter.invokeStatic(Types.OP_UTIL, UNARY_POST_MINUS4)
            } else if (type == Factory.OP_UNARY_PRE) {
                if (operation == Factory.OP_UNARY_PLUS) adapter.invokeStatic(Types.OP_UTIL, UNARY_PRE_PLUS4) else if (operation == Factory.OP_UNARY_MINUS) adapter.invokeStatic(Types.OP_UTIL, UNARY_PRE_MINUS4) else if (operation == Factory.OP_UNARY_DIVIDE) adapter.invokeStatic(Types.OP_UTIL, UNARY_PRE_DIVIDE4) else if (operation == Factory.OP_UNARY_MULTIPLY) adapter.invokeStatic(Types.OP_UTIL, UNARY_PRE_MULTIPLY4) else if (operation == Factory.OP_UNARY_CONCAT) adapter.invokeStatic(Types.OP_UTIL, UNARY_PRE_CONCAT4)
            }
            if (operation == Factory.OP_UNARY_CONCAT) return Types.STRING

            // convert from NUmber to double value (if necessary)
            if (mode == MODE_VALUE) {
                adapter.invokeStatic(Types.CASTER, Methods.METHOD_TO_DOUBLE_VALUE_FROM_NUMBER)
                return Types.DOUBLE_VALUE
            }
            return Types.NUMBER
        }

        /*
		 * undefined scope only with one key (susi++;)
		 */

        // PageContext instance
        adapter.loadArg(0)

        // Collection key Array
        val arrSize: Int = if (scope != null) members.size() + 1 else members.size()
        val useArray = arrSize > 1 || scope != null
        if (useArray) {
            val av = ArrayVisitor()
            var index = 0
            av.visitBegin(adapter, Types.COLLECTION_KEY, arrSize)
            val it: Iterator<Member?> = members.iterator()
            var m: Member?
            var dm: DataMember
            if (scope != null) {
                av.visitBeginItem(adapter, index++)
                getFactory().registerKey(bc, getFactory().createLitString(scope), false)
                av.visitEndItem(adapter)
            }
            while (it.hasNext()) {
                av.visitBeginItem(adapter, index++)
                m = it.next()
                if (m !is DataMember) throw TransformerException(bc, "you cannot use a unary operator with a function " + m.getClass().getName(), getStart())
                getFactory().registerKey(bc, (m as DataMember?).getName(), false)
                av.visitEndItem(adapter)
            }
            av.visitEnd()
        } else {
            val m: Member? = members.iterator().next()
            if (m !is DataMember) throw TransformerException(bc, "you cannot use a unary operator with a function " + m.getClass().getName(), getStart())
            getFactory().registerKey(bc, (m as DataMember?).getName(), false)
        }
        if (type == Factory.OP_UNARY_POST) {
            if (operation != Factory.OP_UNARY_PLUS && operation != Factory.OP_UNARY_MINUS) throw TransformerException(bc, "Post only possible with plus or minus $operation", value.getStart())
            value.writeOut(bc, MODE_REF)
            if (operation == Factory.OP_UNARY_PLUS) adapter.invokeStatic(Types.OP_UTIL, if (useArray) UNARY_POST_PLUS_N else UNARY_POST_PLUS_1) else if (operation == Factory.OP_UNARY_MINUS) adapter.invokeStatic(Types.OP_UTIL, if (useArray) UNARY_POST_MINUS_N else UNARY_POST_MINUS_1)
        } else if (type == Factory.OP_UNARY_PRE) {
            value.writeOut(bc, MODE_REF) // TODOX uses a no ref method
            if (operation == Factory.OP_UNARY_PLUS) adapter.invokeStatic(Types.OP_UTIL, if (useArray) UNARY_PRE_PLUS_N else UNARY_PRE_PLUS_1) else if (operation == Factory.OP_UNARY_MINUS) adapter.invokeStatic(Types.OP_UTIL, if (useArray) UNARY_PRE_MINUS_N else UNARY_PRE_MINUS_1) else if (operation == Factory.OP_UNARY_DIVIDE) adapter.invokeStatic(Types.OP_UTIL, if (useArray) UNARY_PRE_DIVIDE_N else UNARY_PRE_DIVIDE_1) else if (operation == Factory.OP_UNARY_MULTIPLY) adapter.invokeStatic(Types.OP_UTIL, if (useArray) UNARY_PRE_MULTIPLY_N else UNARY_PRE_MULTIPLY_1) else if (operation == Factory.OP_UNARY_CONCAT) adapter.invokeStatic(Types.OP_UTIL, if (useArray) UNARY_PRE_CONCAT_N else UNARY_PRE_CONCAT_1)
        }
        if (operation == Factory.OP_UNARY_CONCAT) return Types.STRING

        // convert from NUmber to double value (if necessary)
        if (mode == MODE_VALUE) {
            adapter.invokeStatic(Types.CASTER, Methods.METHOD_TO_DOUBLE_VALUE_FROM_NUMBER)
            return Types.DOUBLE_VALUE
        }
        return Types.NUMBER
    }

    companion object {
        private val UNARY_POST_PLUS_1: Method? = Method("unaryPoPl", Types.NUMBER, arrayOf<Type?>(Types.PAGE_CONTEXT, Types.COLLECTION_KEY, Types.NUMBER))
        private val UNARY_POST_PLUS_N: Method? = Method("unaryPoPl", Types.NUMBER, arrayOf<Type?>(Types.PAGE_CONTEXT, Types.COLLECTION_KEY_ARRAY, Types.NUMBER))
        private val UNARY_POST_MINUS_N: Method? = Method("unaryPoMi", Types.NUMBER, arrayOf<Type?>(Types.PAGE_CONTEXT, Types.COLLECTION_KEY_ARRAY, Types.NUMBER))
        private val UNARY_POST_MINUS_1: Method? = Method("unaryPoMi", Types.NUMBER, arrayOf<Type?>(Types.PAGE_CONTEXT, Types.COLLECTION_KEY, Types.NUMBER))
        private val UNARY_PRE_PLUS_N: Method? = Method("unaryPrPl", Types.NUMBER, arrayOf<Type?>(Types.PAGE_CONTEXT, Types.COLLECTION_KEY_ARRAY, Types.NUMBER))
        private val UNARY_PRE_PLUS_1: Method? = Method("unaryPrPl", Types.NUMBER, arrayOf<Type?>(Types.PAGE_CONTEXT, Types.COLLECTION_KEY, Types.NUMBER))
        private val UNARY_PRE_MINUS_N: Method? = Method("unaryPrMi", Types.NUMBER, arrayOf<Type?>(Types.PAGE_CONTEXT, Types.COLLECTION_KEY_ARRAY, Types.NUMBER))
        private val UNARY_PRE_MINUS_1: Method? = Method("unaryPrMi", Types.NUMBER, arrayOf<Type?>(Types.PAGE_CONTEXT, Types.COLLECTION_KEY, Types.NUMBER))
        private val UNARY_PRE_MULTIPLY_N: Method? = Method("unaryPrMu", Types.NUMBER, arrayOf<Type?>(Types.PAGE_CONTEXT, Types.COLLECTION_KEY_ARRAY, Types.NUMBER))
        private val UNARY_PRE_MULTIPLY_1: Method? = Method("unaryPrMu", Types.NUMBER, arrayOf<Type?>(Types.PAGE_CONTEXT, Types.COLLECTION_KEY, Types.NUMBER))
        private val UNARY_PRE_DIVIDE_N: Method? = Method("unaryPrDi", Types.NUMBER, arrayOf<Type?>(Types.PAGE_CONTEXT, Types.COLLECTION_KEY_ARRAY, Types.NUMBER))
        private val UNARY_PRE_DIVIDE_1: Method? = Method("unaryPrDi", Types.NUMBER, arrayOf<Type?>(Types.PAGE_CONTEXT, Types.COLLECTION_KEY, Types.NUMBER))
        private val UNARY_PRE_CONCAT_N: Method? = Method("unaryPreConcat", Types.STRING, arrayOf<Type?>(Types.PAGE_CONTEXT, Types.COLLECTION_KEY_ARRAY, Types.STRING))
        private val UNARY_PRE_CONCAT_1: Method? = Method("unaryPreConcat", Types.STRING, arrayOf<Type?>(Types.PAGE_CONTEXT, Types.COLLECTION_KEY, Types.STRING))
        private val UNARY_POST_PLUS4: Method? = Method("unaryPoPl", Types.NUMBER, arrayOf<Type?>(Types.PAGE_CONTEXT, Types.COLLECTION, Types.COLLECTION_KEY, Types.NUMBER))
        private val UNARY_POST_MINUS4: Method? = Method("unaryPoMi", Types.NUMBER, arrayOf<Type?>(Types.PAGE_CONTEXT, Types.COLLECTION, Types.COLLECTION_KEY, Types.NUMBER))
        private val UNARY_PRE_PLUS4: Method? = Method("unaryPrPl", Types.NUMBER, arrayOf<Type?>(Types.PAGE_CONTEXT, Types.COLLECTION, Types.COLLECTION_KEY, Types.NUMBER))
        private val UNARY_PRE_MINUS4: Method? = Method("unaryPrMi", Types.NUMBER, arrayOf<Type?>(Types.PAGE_CONTEXT, Types.COLLECTION, Types.COLLECTION_KEY, Types.NUMBER))
        private val UNARY_PRE_MULTIPLY4: Method? = Method("unaryPrMu", Types.NUMBER, arrayOf<Type?>(Types.PAGE_CONTEXT, Types.COLLECTION, Types.COLLECTION_KEY, Types.NUMBER))
        private val UNARY_PRE_DIVIDE4: Method? = Method("unaryPrDi", Types.NUMBER, arrayOf<Type?>(Types.PAGE_CONTEXT, Types.COLLECTION, Types.COLLECTION_KEY, Types.NUMBER))
        private val UNARY_PRE_CONCAT4: Method? = Method("unaryPreConcat", Types.STRING, arrayOf<Type?>(Types.PAGE_CONTEXT, Types.COLLECTION, Types.COLLECTION_KEY, Types.STRING))
    }

    init {
        this.`var` = `var`
        this.value = value
        this.type = type
        this.operation = operation
    }
}