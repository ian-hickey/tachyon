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
package tachyon.transformer.bytecode.statement.udf

import org.objectweb.asm.commons.GeneratorAdapter

class FunctionImpl : Function {
    constructor(name: Expression?, returnType: Expression?, returnFormat: Expression?, output: Expression?, bufferOutput: Expression?, access: Int, displayName: Expression?,
                description: Expression?, hint: Expression?, secureJson: Expression?, verifyClient: Expression?, localMode: Expression?, cachedWithin: Literal?, modifier: Int, body: Body?,
                start: Position?, end: Position?) : super(name, returnType, returnFormat, output, bufferOutput, access, displayName, description, hint, secureJson, verifyClient, localMode, cachedWithin, modifier, body,
            start, end) {
    }

    constructor(name: String?, access: Int, modifier: Int, returnType: String?, body: Body?, start: Position?, end: Position?) : super(name, access, modifier, returnType, body, start, end) {}

    @Override
    @Throws(TransformerException::class)
    override fun _writeOut(bc: BytecodeContext?, pageType: Int) {
        val adapter: GeneratorAdapter = bc.getAdapter()
        if (pageType == PAGE_TYPE_INTERFACE) {
            adapter.loadArg(0)
        } else if (pageType == PAGE_TYPE_COMPONENT) {
            adapter.loadArg(1)
        } else {
            adapter.loadArg(0)
            adapter.invokeVirtual(Types.PAGE_CONTEXT, VARIABLE_SCOPE)
        }
        bc.getFactory().registerKey(bc, name, true)
        if (pageType == PAGE_TYPE_COMPONENT) {
            if (this.jf != null) {
                bc.registerJavaFunction(jf)
                adapter.push(jf.getClassName())
                adapter.invokeVirtual(Types.COMPONENT_IMPL, REG_JAVA_FUNCTION)
            } else {
                loadUDFProperties(bc, index, TYPE_UDF)
                adapter.invokeVirtual(Types.COMPONENT_IMPL, if ("staticConstructor".equals(bc.getMethod().getName())) REG_STATIC_UDF_KEY else REG_UDF_KEY)
            }
        } else if (pageType == PAGE_TYPE_INTERFACE) {
            if (this.jf != null) {
                bc.registerJavaFunction(jf)
                adapter.push(jf.getClassName())
                adapter.invokeVirtual(Types.INTERFACE_IMPL, REG_JAVA_FUNCTION)
            } else {
                loadUDFProperties(bc, index, TYPE_UDF)
                adapter.invokeVirtual(Types.INTERFACE_IMPL, if ("staticConstructor".equals(bc.getMethod().getName())) REG_STATIC_UDF_KEY else REG_UDF_KEY)
            }
        } else {
            if (this.jf != null) {
                bc.registerJavaFunction(jf)
                adapter.loadArg(0)
                adapter.checkCast(Types.PAGE_CONTEXT_IMPL)
                adapter.visitVarInsn(ALOAD, 0)
                adapter.push(jf.getClassName())
                adapter.invokeVirtual(Types.PAGE_CONTEXT_IMPL, USE_JAVA_FUNCTION)
            } else {
                adapter.newInstance(Types.UDF_IMPL)
                adapter.dup()
                loadUDFProperties(bc, index, TYPE_UDF)
                adapter.invokeConstructor(Types.UDF_IMPL, INIT_UDF_IMPL_PROP)
            }
            // loadUDF(bc, index);
            adapter.invokeInterface(Types.VARIABLES, SET_KEY)
            adapter.pop()
        }
    }

    @Override
    fun getType(): Int {
        return TYPE_UDF
    }
}