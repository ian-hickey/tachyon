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
package lucee.transformer.bytecode.statement.tag

import org.objectweb.asm.Label

class TagParam(f: Factory?, start: Position?, end: Position?) : TagBaseNoFinal(f, start, end) {
    @Throws(ExpressionException::class)
    private fun t(pc: PageContext?): Double {

        // if(StringUtil.isEmpty(name)) throw new ExpressionException("The attribute name is required");

        // String name="kkk";
        // Object value=VariableInterpreter.getVariableEL(pc,name,NullSupportHelper.NULL(pc));

        /*
		 * Object value=null; Object defaultValue=null; boolean isNew=false;
		 * 
		 * if(NullSupportHelper.NULL(pc)==value) { if(defaultValue==null) throw new
		 * ExpressionException("The required parameter ["+name+"] was not provided."); value=defaultValue;
		 * isNew=true; }
		 */
        return Double.NaN
    }

    @Override
    @Throws(TransformerException::class)
    override fun _writeOut(bc: BytecodeContext?) {
        val adapter: GeneratorAdapter = bc.getAdapter()
        Argument.checkDefaultValue(this)

        // attributes
        val name: Expression = getAttribute("name")!!.getValue()
        val attrDefault: Attribute = getAttribute("default")
        var def: Expression? = null
        if (attrDefault != null) def = attrDefault.getValue() // .writeOut(bc, Expression.MODE_REF);
        // else adapter.visitInsn(Opcodes.ACONST_NULL);

        // check attributes name
        // if(StringUtil.isEmpty(name)) throw new ExpressionException("The attribute name is required");
        /*
		 * name.writeOut(bc, Expression.MODE_REF); adapter.invokeStatic(Types.STRING_UTIL, IS_EMPTY); Label
		 * end = new Label(); adapter.visitJumpInsn(Opcodes.IFEQ, end); //adapter.visitLabel(new Label());
		 * adapter.newInstance(Types.EXPRESSION_EXCEPTION); adapter.dup();
		 * adapter.push("for param a name is required");
		 * adapter.invokeConstructor(Types.EXPRESSION_EXCEPTION, CONSTR_STRING); adapter.throwException();
		 * adapter.visitLabel(end);
		 */

        // value
        // Object value=VariableInterpreter.getVariableEL(this,name,NullSupportHelper.NULL(this));
        val value: Int = adapter.newLocal(Types.OBJECT)
        adapter.loadArg(0) // pc
        name.writeOut(bc, Expression.MODE_REF) // name
        adapter.loadArg(0) // pc
        adapter.invokeStatic(NULL_SUPPORT_HELPER, NULL)
        adapter.invokeStatic(VARIABLE_INTERPRETER, GET_VARIABLE_EL)
        adapter.storeLocal(value)

        // check value 2=value; 3=defaultValue; isNew=4
        val isNew: Int = adapter.newLocal(Types.BOOLEAN_VALUE)
        adapter.push(false)
        adapter.storeLocal(isNew, Types.BOOLEAN_VALUE)

        // Label l3 = new Label();
        // mv.visitLabel(l3);
        adapter.loadArg(0)
        adapter.invokeStatic(NULL_SUPPORT_HELPER, NULL)
        adapter.loadLocal(value)
        val l4 = Label()
        adapter.visitJumpInsn(Opcodes.IF_ACMPNE, l4)
        val l5 = Label()
        adapter.visitLabel(l5)
        if (def != null) def.writeOut(bc, Expression.MODE_REF) else ASMConstants.NULL(adapter)
        adapter.dup()
        val ldef: Int = adapter.newLocal(Types.OBJECT)
        adapter.storeLocal(ldef)
        val l6 = Label()
        adapter.visitJumpInsn(Opcodes.IFNONNULL, l6)
        val l7 = Label()
        adapter.visitLabel(l7)
        adapter.newInstance(Types.EXPRESSION_EXCEPTION)
        adapter.dup()
        adapter.newInstance(Types.STRING_BUILDER)
        adapter.dup()
        adapter.push("The required parameter [")
        adapter.invokeConstructor(Types.STRING_BUILDER, CONSTR_STRING)
        name.writeOut(bc, Expression.MODE_REF)
        adapter.invokeVirtual(Types.STRING_BUILDER, APPEND_OBJECT)
        adapter.visitLdcInsn("] was not provided.")
        adapter.invokeVirtual(Types.STRING_BUILDER, APPEND_STRING)
        adapter.invokeVirtual(Types.STRING_BUILDER, TO_STRING)
        adapter.invokeConstructor(Types.EXPRESSION_EXCEPTION, CONSTR_STRING)
        adapter.throwException()
        adapter.visitLabel(l6)
        adapter.loadLocal(ldef)
        adapter.storeLocal(value)
        adapter.push(true)
        adapter.storeLocal(isNew)
        adapter.visitLabel(l4)

        // pc
        adapter.loadArg(0)
        adapter.checkCast(Types.PAGE_CONTEXT_IMPL)

        // type
        val attrType: Attribute = getAttribute("type")
        if (attrType != null) attrType.getValue()!!.writeOut(bc, Expression.MODE_REF) else adapter.push("any")
        // adapter.push("any");

        // name
        name.writeOut(bc, Expression.MODE_REF)
        // adapter.push("url.test");

        // value
        adapter.loadLocal(value)
        val attrMin: Attribute = getAttribute("min")
        val attrMax: Attribute = getAttribute("max")
        val attrPattern: Attribute = getAttribute("pattern")
        val maxLength: Attribute = getAttribute("maxLength")

        // min/max
        if (attrMin != null || attrMax != null) {
            // min
            if (attrMin != null) attrMin.getValue()!!.writeOut(bc, Expression.MODE_VALUE) else adapter.visitLdcInsn(Double.valueOf("NaN"))

            // max
            if (attrMax != null) attrMax.getValue()!!.writeOut(bc, Expression.MODE_VALUE) else adapter.visitLdcInsn(Double.valueOf("NaN"))
        } else {
            adapter.visitLdcInsn(Double.valueOf("NaN"))
            adapter.visitLdcInsn(Double.valueOf("NaN"))
        }
        // adapter.push(-1);
        // adapter.push(-1);

        // adapter.invokeVirtual(Types.PAGE_CONTEXT_IMPL, T);
        // if(true)return;

        // pattern
        if (attrPattern != null) attrPattern.getValue()!!.writeOut(bc, Expression.MODE_REF) else ASMConstants.NULL(adapter)
        // ASMConstants.NULL(adapter);

        // maxlength
        if (maxLength != null) bc.getFactory().toExprInt(maxLength.getValue()).writeOut(bc, Expression.MODE_VALUE) else adapter.push(-1)
        // adapter.push(-1);

        // isNew
        adapter.loadLocal(isNew, Types.BOOLEAN_VALUE)
        // adapter.push(true);

        // subparam
        adapter.invokeVirtual(Types.PAGE_CONTEXT_IMPL, SUB_PARAM)
        // param(type, name, defaultValue,Double.NaN,Double.NaN,regex,-1);
        // subparam(type, name, value, min, max, strPattern, maxLength, isNew); // SSO
    }

    companion object {
        //
        val NULL_SUPPORT_HELPER: Type? = Type.getType(NullSupportHelper::class.java)
        val VARIABLE_INTERPRETER: Type? = Type.getType(VariableInterpreter::class.java)

        // void param(String type, String name, Object defaultValue)
        private val PARAM_TYPE_NAME_DEFAULTVALUE: Method? = Method("param", Types.VOID, arrayOf<Type?>(Types.STRING, Types.STRING, Types.OBJECT))
        private val PARAM_TYPE_NAME_DEFAULTVALUE_REGEX: Method? = Method("param", Types.VOID, arrayOf<Type?>(Types.STRING, Types.STRING, Types.OBJECT, Types.STRING))
        private val PARAM_TYPE_NAME_DEFAULTVALUE_MIN_MAX: Method? = Method("param", Types.VOID, arrayOf<Type?>(Types.STRING, Types.STRING, Types.OBJECT, Types.DOUBLE_VALUE, Types.DOUBLE_VALUE))
        private val PARAM_TYPE_NAME_DEFAULTVALUE_MAXLENGTH: Method? = Method("param", Types.VOID, arrayOf<Type?>(Types.STRING, Types.STRING, Types.OBJECT, Types.INT_VALUE))
        private val IS_EMPTY: Method? = Method("isEmpty", Types.BOOLEAN_VALUE, arrayOf<Type?>(Types.STRING))
        private val CONSTR_STRING: Method? = Method("<init>", Types.VOID, arrayOf<Type?>(Types.STRING))
        private val NULL: Method? = Method("NULL", Types.OBJECT, arrayOf<Type?>(Types.PAGE_CONTEXT))
        private val GET_VARIABLE_EL: Method? = Method("getVariableEL", Types.OBJECT, arrayOf<Type?>(Types.PAGE_CONTEXT, Types.STRING, Types.OBJECT))
        private val APPEND_OBJECT: Method? = Method("append", Types.STRING_BUILDER, arrayOf<Type?>(Types.OBJECT))
        private val APPEND_STRING: Method? = Method("append", Types.STRING_BUILDER, arrayOf<Type?>(Types.STRING))
        private val TO_STRING: Method? = Method("toString", Types.STRING, arrayOf<Type?>())
        private val SUB_PARAM: Method? = Method("subparam", Types.VOID, arrayOf<Type?>(Types.STRING, Types.STRING, Types.OBJECT, Types.DOUBLE_VALUE, Types.DOUBLE_VALUE, Types.STRING, Types.INT_VALUE, Types.BOOLEAN_VALUE))
        private val T: Method? = Method("t", Types.VOID, arrayOf<Type?>(Types.STRING, Types.STRING, Types.OBJECT, Types.DOUBLE_VALUE, Types.DOUBLE_VALUE
        ))
    }
}