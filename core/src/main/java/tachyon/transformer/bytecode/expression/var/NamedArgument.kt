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

class NamedArgument(name: Expression?, value: Expression?, type: String?, varKeyUpperCase: Boolean) : Argument(value, type) {
    private var name: Expression?
    private val varKeyUpperCase: Boolean

    @Override
    @Throws(TransformerException::class)
    override fun _writeOut(bc: BytecodeContext?, mode: Int): Type? {
        var form = VALUE
        var type = STRING
        if (name is Variable && !(name as Variable?).fromHash()) {
            val adapter: GeneratorAdapter = bc.getAdapter()
            val arr: Array<String?> = VariableString.variableToStringArray(bc, name as Variable?, true)
            if (arr.size > 1) {
                form = ARRAY
                val av = ArrayVisitor()
                av.visitBegin(adapter, Types.STRING, arr.size)
                for (y in arr.indices) {
                    av.visitBeginItem(adapter, y)
                    adapter.push(if (varKeyUpperCase) arr[y].toUpperCase() else arr[y])
                    av.visitEndItem(bc.getAdapter())
                }
                av.visitEnd()
            } else {
                // VariableString.toExprString(name).writeOut(bc, MODE_REF);
                val str: String = VariableString.variableToString(bc, name as Variable?, true)
                name = bc.getFactory().createLitString(if (varKeyUpperCase) str.toUpperCase() else str)
                getFactory().registerKey(bc, VariableString.toExprString(name), false)
                type = KEY
            }
        } else {
            getFactory().registerKey(bc, name.getFactory().toExprString(name), false)
            type = KEY
        }
        // name.writeOut(bc, MODE_REF);
        super._writeOut(bc, MODE_REF)
        // bc.getAdapter().push(variableString);
        bc.getAdapter().invokeStatic(Types.FUNCTION_VALUE_IMPL, NEW_INSTANCE!![type]!![form])
        return Types.FUNCTION_VALUE
    }

    @Override
    @Throws(TransformerException::class)
    override fun writeOutValue(bc: BytecodeContext?, mode: Int): Type? {
        return super.writeOutValue(bc, mode)
    }

    /**
     * @return the name
     */
    fun getName(): Expression? {
        return name
    }

    companion object {
        private const val VALUE = 0
        private const val ARRAY = 1
        private const val KEY = 0
        private const val STRING = 1
        private val NEW_INSTANCE: Array<Array<Method?>?>? = arrayOf(arrayOf<Method?>(Method("newInstance", Types.FUNCTION_VALUE, arrayOf<Type?>(Types.COLLECTION_KEY, Types.OBJECT)),
                Method("newInstance", Types.FUNCTION_VALUE, arrayOf<Type?>(Types.COLLECTION_KEY_ARRAY, Types.OBJECT))), arrayOf<Method?>(Method("newInstance", Types.FUNCTION_VALUE, arrayOf<Type?>(Types.STRING, Types.OBJECT)),
                Method("newInstance", Types.FUNCTION_VALUE, arrayOf<Type?>(Types.STRING_ARRAY, Types.OBJECT))))
    }

    init {
        this.name = if (name is Null || name is NullConstant) name.getFactory().createLitString(if (varKeyUpperCase) "NULL" else "null") else name
        this.varKeyUpperCase = varKeyUpperCase
    }
}