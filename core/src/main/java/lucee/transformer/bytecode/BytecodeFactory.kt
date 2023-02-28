/**
 * Copyright (c) 2015, Lucee Assosication Switzerland. All rights reserved.
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
package lucee.transformer.bytecode

import java.math.BigDecimal

class BytecodeFactory(config: Config?) : FactoryBase() {
    private val TRUE: LitBoolean?
    private val FALSE: LitBoolean?
    private val EMPTY: LitString?
    private val NULL: Expression?
    private val NUMBER_ZERO: LitNumber?
    private val NUMBER_ONE: LitNumber?
    private val config: Config?
    @Override
    fun createLitString(str: String?): LitString? {
        return LitStringImpl(this, str, null, null)
    }

    @Override
    fun createLitString(str: String?, start: Position?, end: Position?): LitString? {
        return LitStringImpl(this, str, start, end)
    }

    @Override
    fun createLitBoolean(b: Boolean): LitBoolean? {
        return LitBooleanImpl(this, b, null, null)
    }

    @Override
    fun createLitBoolean(b: Boolean, start: Position?, end: Position?): LitBoolean? {
        return LitBooleanImpl(this, b, start, end)
    }

    @Override
    @Throws(CasterException::class)
    fun createLitNumber(number: String?): LitNumber? {
        return createLitNumber(number, null, null)
    }

    @Override
    @Throws(CasterException::class)
    fun createLitNumber(number: String?, start: Position?, end: Position?): LitNumber? {
        return LitNumberImpl(this, number, start, end)
    }

    @Override
    fun createLitNumber(bd: BigDecimal?): LitNumber? {
        return createLitNumber(bd, null, null)
    }

    @Override
    fun createLitNumber(bd: BigDecimal?, start: Position?, end: Position?): LitNumber? {
        return LitNumberImpl(this, bd, start, end)
    }

    @Override
    fun createLitNumber(n: Number?): LitNumber? {
        return createLitNumber(n, null, null)
    }

    @Override
    fun createLitNumber(n: Number?, start: Position?, end: Position?): LitNumber? {
        return LitNumberImpl(this, if (n is BigDecimal) n as BigDecimal? else BigDecimal.valueOf(n.doubleValue()), start, end)
    }

    @Override
    fun createLitLong(l: Long): LitLong? {
        return LitLongImpl(this, l, null, null)
    }

    @Override
    fun createLitLong(l: Long, start: Position?, end: Position?): LitLong? {
        return LitLongImpl(this, l, start, end)
    }

    @Override
    fun createLitInteger(i: Int): LitInteger? {
        return LitIntegerImpl(this, i, null, null)
    }

    @Override
    fun createLitInteger(i: Int, start: Position?, end: Position?): LitInteger? {
        return LitIntegerImpl(this, i, start, end)
    }

    @Override
    fun isNull(e: Expression?): Boolean {
        return e is Null
    }

    @Override
    fun createNull(): Expression? {
        return Null(this, null, null)
    }

    @Override
    fun createNull(start: Position?, end: Position?): Expression? {
        return Null(this, start, end)
    }

    @Override
    fun createNullConstant(start: Position?, end: Position?): Expression? {
        return NullConstant(this, null, null)
    }

    @Override
    fun createEmpty(): Expression? {
        return Empty(this, null, null)
    }

    @Override
    fun createDataMember(name: ExprString?): DataMember? {
        return DataMemberImpl(name)
    }

    @Override
    fun TRUE(): LitBoolean? {
        return TRUE
    }

    @Override
    fun FALSE(): LitBoolean? {
        return FALSE
    }

    @Override
    fun EMPTY(): LitString? {
        return EMPTY
    }

    @Override
    fun NUMBER_ZERO(): LitNumber? {
        return NUMBER_ZERO
    }

    @Override
    fun NUMBER_ONE(): LitNumber? {
        return NUMBER_ONE
    }

    @Override
    fun NULL(): Expression? {
        return NULL
    }

    @Override
    fun toExprNumber(expr: Expression?): ExprNumber? {
        return CastNumber.toExprNumber(expr)
    }

    @Override
    fun toExprString(expr: Expression?): ExprString? {
        return CastString.toExprString(expr)
    }

    @Override
    fun toExprBoolean(expr: Expression?): ExprBoolean? {
        return CastBoolean.toExprBoolean(expr)
    }

    @Override
    fun toExprInt(expr: Expression?): ExprInt? {
        return CastInt.toExprInt(expr)
    }

    @Override
    fun toExpression(expr: Expression?, type: String?): Expression? {
        return CastOther.toExpression(expr, type)
    }

    @Override
    fun createVariable(start: Position?, end: Position?): Variable? {
        return VariableImpl(this, start, end)
    }

    @Override
    fun createVariable(scope: Int, start: Position?, end: Position?): Variable? {
        return VariableImpl(this, scope, start, end)
    }

    @Override
    fun opString(left: Expression?, right: Expression?): ExprString? {
        return OpString.toExprString(left, right, true)
    }

    @Override
    fun opString(left: Expression?, right: Expression?, concatStatic: Boolean): ExprString? {
        return OpString.toExprString(left, right, concatStatic)
    }

    @Override
    fun opBool(left: Expression?, right: Expression?, operation: Int): ExprBoolean? {
        return OpBool.toExprBoolean(left, right, operation)
    }

    @Override
    fun opNumber(left: Expression?, right: Expression?, operation: Int): ExprNumber? {
        return OpNumber.toExprNumber(left, right, operation)
    }

    @Override
    fun opNegate(expr: Expression?, start: Position?, end: Position?): Expression? {
        return OpNegate.toExprBoolean(expr, start, end)
    }

    @Override
    fun removeCastString(expr: Expression?): Expression? {
        var expr: Expression? = expr
        while (true) {
            expr = if (expr is CastString) {
                (expr as CastString?).getExpr()
            } else if (expr is CastOther && ((expr as CastOther?).getType().equalsIgnoreCase("String") || (expr as CastOther?).getType().equalsIgnoreCase("java.lang.String"))) {
                (expr as CastOther?).getExpr()
            } else break
        }
        return expr
    }

    @Override
    @Throws(TransformerException::class)
    fun registerKey(c: Context?, name: Expression?, doUpperCase: Boolean) {
        val bc: BytecodeContext? = c
        if (name is Literal) {
            val l: Literal? = name as Literal?
            var ls: LitString = if (name is LitString) l as LitString? else c.getFactory().createLitString(l.getString())
            if (doUpperCase) {
                ls = ls.duplicate()
                ls.upperCase()
            }
            val key: String = KeyConstants.getFieldName(ls.getString())
            if (key != null) {
                bc!!.getAdapter().getStatic(KEY_CONSTANTS, key, Types.COLLECTION_KEY)
                return
            }
            val index: Int = bc!!.registerKey(ls)
            bc!!.getAdapter().visitVarInsn(Opcodes.ALOAD, 0)
            bc!!.getAdapter().visitFieldInsn(Opcodes.GETFIELD, bc!!.getClassName(), "keys", Types.COLLECTION_KEY_ARRAY.toString())
            bc!!.getAdapter().push(index)
            bc!!.getAdapter().visitInsn(Opcodes.AALOAD)

            // ExpressionUtil.writeOutSilent(lit,bc, Expression.MODE_REF);
            // bc.getAdapter().invokeStatic(Page.KEY_IMPL, Page.KEY_INTERN);
            return
        }
        name.writeOut(bc, Expression.MODE_REF)
        bc!!.getAdapter().invokeStatic(Page.KEY_IMPL, Page.KEY_INTERN)
        // bc.getAdapter().invokeStatic(Types.CASTER, TO_KEY);
        return
    }

    @Override
    fun getConfig(): Config? {
        return config
    }

    @Override
    fun createStruct(): Expression? {
        return EmptyStruct(this)
    }

    @Override
    fun createArray(): Expression? {
        return EmptyArray(this)
    }

    @Override
    fun opUnaryNumber(`var`: Variable?, value: Expression?, type: Short, operation: Int, start: Position?, end: Position?): ExprNumber? {
        return OpUnaryNumber(`var`, value, type, operation, start, end)
    }

    @Override
    fun opUnaryString(`var`: Variable?, value: Expression?, type: Short, operation: Int, start: Position?, end: Position?): ExprString? {
        return OpUnaryString(`var`, value, type, operation, start, end)
    }

    @Override
    fun opContional(cont: Expression?, left: Expression?, right: Expression?): Expression? {
        return OpContional.toExpr(cont, left, right)
    }

    @Override
    fun opDecision(left: Expression?, right: Expression?, operation: Int): ExprBoolean? {
        return OpDecision.toExprBoolean(left, right, operation)
    }

    @Override
    fun opElvis(left: Variable?, right: Expression?): Expression? {
        return OpElvis.toExpr(left, right)
    }

    @Override
    fun opNegateNumber(expr: Expression?, operation: Int, start: Position?, end: Position?): ExprNumber? {
        return OpNegateNumber.toExprNumber(expr, operation, start, end)
    }

    companion object {
        private val INIT: Method? = Method("init", Types.COLLECTION_KEY, arrayOf<Type?>(Types.STRING))
        private val KEY_CONSTANTS: Type? = Type.getType(KeyConstants::class.java)
        private var instance: BytecodeFactory? = null
        fun getInstance(config: Config?): Factory? {
            if (instance == null) instance = BytecodeFactory(if (config == null) ThreadLocalPageContext.getConfig() else config)
            return instance
        }
    }

    init {
        TRUE = createLitBoolean(true)
        FALSE = createLitBoolean(false)
        EMPTY = createLitString("")
        NULL = Null.getSingleInstance(this)
        NUMBER_ZERO = createLitNumber(0)
        NUMBER_ONE = createLitNumber(1)
        this.config = config
    }
}