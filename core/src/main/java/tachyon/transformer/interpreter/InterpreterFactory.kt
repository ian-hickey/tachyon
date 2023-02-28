package tachyon.transformer.interpreter

import java.math.BigDecimal

class InterpreterFactory(config: Config?) : FactoryBase() {
    private val TRUE: LitBoolean?
    private val FALSE: LitBoolean?
    private val EMPTY: LitString?
    private val NULL: Expression?
    private val NUMBER_ZERO: LitNumber?
    private val NUMBER_ONE: LitNumber?
    private val config: Config?
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
    @Throws(PageException::class)
    fun createLitNumber(number: String?): LitNumber? {
        return createLitNumber(number, null, null)
    }

    @Override
    @Throws(PageException::class)
    fun createLitNumber(number: String?, start: Position?, end: Position?): LitNumber? {
        return LitNumberImpl(this, Caster.toBigDecimal(number), start, end)
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
        return LitNumberImpl(this, n, start, end)
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
        // TODO Auto-generated method stub
        return null
    }

    @Override
    fun createVariable(start: Position?, end: Position?): Variable? {
        // TODO Auto-generated method stub
        return null
    }

    @Override
    fun createVariable(scope: Int, start: Position?, end: Position?): Variable? {
        // TODO Auto-generated method stub
        return null
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
    fun opUnaryNumber(`var`: Variable?, value: Expression?, type: Short, operation: Int, start: Position?, end: Position?): ExprNumber? {
        return null
    }

    @Override
    fun opUnaryString(`var`: Variable?, value: Expression?, type: Short, operation: Int, start: Position?, end: Position?): ExprString? {
        return null
    }

    @Override
    fun opNegate(expr: Expression?, start: Position?, end: Position?): Expression? {
        return OpNegate.toExprBoolean(expr, start, end)
    }

    @Override
    fun opNegateNumber(expr: Expression?, operation: Int, start: Position?, end: Position?): ExprNumber? {
        return OpNegateNumber.toExprNumber(expr, operation, start, end)
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
    fun removeCastString(expr: Expression?): Expression? {
        // TODO Auto-generated method stub
        return null
    }

    @Override
    @Throws(TransformerException::class)
    fun registerKey(bc: Context?, name: Expression?, doUpperCase: Boolean) {
        // TODO Auto-generated method stub
    }

    @Override
    fun getConfig(): Config? {
        // TODO Auto-generated method stub
        return null
    }

    companion object {
        private var instance: InterpreterFactory? = null
        fun getInstance(config: Config?): Factory? {
            if (instance == null) instance = InterpreterFactory(if (config == null) ThreadLocalPageContext.getConfig() else config)
            return instance
        }
    }

    init {
        TRUE = createLitBoolean(true)
        FALSE = createLitBoolean(false)
        EMPTY = createLitString("")
        NULL = Null(this, null, null)
        NUMBER_ZERO = createLitNumber(0)
        NUMBER_ONE = createLitNumber(1)
        this.config = config
    }
}