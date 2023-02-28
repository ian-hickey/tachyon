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
package tachyon.transformer.bytecode.op

import java.util.ArrayList

class OpElvis private constructor(left: Variable?, right: Expression?) : ExpressionBase(left.getFactory(), left.getStart(), right.getEnd()) {
    private val left: Variable?
    private val right: Expression?

    /**
     *
     * @see tachyon.transformer.bytecode.expression.ExpressionBase._writeOut
     */
    @Override
    @Throws(TransformerException::class)
    fun _writeOut(bc: BytecodeContext?, mode: Int): Type? {
        if (ASMUtil.hasOnlyDataMembers(left)) return _writeOutPureDataMember(bc, mode)
        val notNull = Label()
        val end = Label()
        val ga: GeneratorAdapter = bc.getAdapter()
        val l: Int = ga.newLocal(Types.OBJECT)
        ExpressionUtil.visitLine(bc, left.getStart())
        left.writeOut(bc, MODE_REF)
        ExpressionUtil.visitLine(bc, left.getEnd())
        ga.dup()
        ga.storeLocal(l)
        ga.visitJumpInsn(Opcodes.IFNONNULL, notNull)
        ExpressionUtil.visitLine(bc, right.getStart())
        right.writeOut(bc, MODE_REF)
        ExpressionUtil.visitLine(bc, right.getEnd())
        ga.visitJumpInsn(Opcodes.GOTO, end)
        ga.visitLabel(notNull)
        ga.loadLocal(l)
        ga.visitLabel(end)
        return Types.OBJECT
    }

    @Throws(TransformerException::class)
    fun _writeOutPureDataMember(bc: BytecodeContext?, mode: Int): Type? {
        // TODO use function isNull for this
        val adapter: GeneratorAdapter = bc.getAdapter()
        val yes = Label()
        val end = Label()
        val members: List<Member?> = left.getMembers()

        // to array
        val it: Iterator<Member?> = members.iterator()
        val list: List<DataMember?> = ArrayList<DataMember?>()
        while (it.hasNext()) {
            list.add(it.next() as DataMember?)
        }
        val arr: Array<DataMember?> = list.toArray(arrayOfNulls<DataMember?>(members.size()))
        ExpressionUtil.visitLine(bc, left.getStart())

        // public static boolean call(PageContext pc , double scope,String[] varNames)
        // pc
        adapter.loadArg(0)
        // scope
        adapter.push(left.getScope() as Double)
        // varNames

        // all literal string?
        var allLiteral = true
        for (i in arr.indices) {
            if (arr[i].getName() !is Literal) allLiteral = false
        }
        val av = ArrayVisitor()
        if (!allLiteral) {
            // String Array
            av.visitBegin(adapter, Types.STRING, arr.size)
            for (i in arr.indices) {
                av.visitBeginItem(adapter, i)
                arr[i].getName().writeOut(bc, MODE_REF)
                av.visitEndItem(adapter)
            }
        } else {
            // Collection.Key Array
            av.visitBegin(adapter, Types.COLLECTION_KEY, arr.size)
            for (i in arr.indices) {
                av.visitBeginItem(adapter, i)
                getFactory().registerKey(bc, arr[i].getName(), false)
                av.visitEndItem(adapter)
            }
        }
        av.visitEnd()

        // allowNull
        // adapter.push(false);

        // ASMConstants.NULL(adapter);

        // call IsDefined.invoke
        adapter.invokeStatic(ELVIS, if (allLiteral) INVOKE_KEY else INVOKE_STR)
        ExpressionUtil.visitLine(bc, left.getEnd())
        adapter.visitJumpInsn(Opcodes.IFEQ, yes)

        // left
        ExpressionUtil.visitLine(bc, left.getStart())
        left.writeOut(bc, MODE_REF)
        ExpressionUtil.visitLine(bc, left.getEnd())
        adapter.visitJumpInsn(Opcodes.GOTO, end)

        // right
        ExpressionUtil.visitLine(bc, right.getStart())
        adapter.visitLabel(yes)
        right.writeOut(bc, MODE_REF)
        ExpressionUtil.visitLine(bc, right.getEnd())
        adapter.visitLabel(end)
        return Types.OBJECT
    }

    companion object {
        private val ELVIS: Type? = Type.getType(Elvis::class.java)
        val INVOKE_STR: Method? = Method("operate", Types.BOOLEAN_VALUE, arrayOf<Type?>(Types.PAGE_CONTEXT, Types.DOUBLE_VALUE, Types.STRING_ARRAY))
        val INVOKE_KEY: Method? = Method("operate", Types.BOOLEAN_VALUE, arrayOf<Type?>(Types.PAGE_CONTEXT, Types.DOUBLE_VALUE, Types.COLLECTION_KEY_ARRAY))
        fun toExpr(left: Variable?, right: Expression?): Expression? {
            return OpElvis(left, right)
        }
    }

    init {
        this.left = left
        this.right = right
    }
}