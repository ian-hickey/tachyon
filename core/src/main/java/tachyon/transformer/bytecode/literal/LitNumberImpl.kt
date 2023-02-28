package tachyon.transformer.bytecode.literal

import java.math.BigDecimal

/**
 * A Literal String
 */
class LitNumberImpl : ExpressionBase, LitNumber, ExprNumber {
    private var number: String?
    private var bd: BigDecimal? = null

    constructor(f: Factory?, number: String?, start: Position?, end: Position?) : super(f, start, end) {
        this.number = number
    }

    constructor(f: Factory?, bd: BigDecimal?, start: Position?, end: Position?) : super(f, start, end) {
        this.bd = bd
        number = Caster.toString(bd)
    }

    @Override
    fun getNumber(): Number? {
        return try {
            getBigDecimal()
        } catch (e: CasterException) {
            throw PageRuntimeException(e)
        }
    }

    @Override
    fun getNumber(defaultValue: Number?): Number? {
        return try {
            getBigDecimal()
        } catch (e: CasterException) {
            defaultValue
        }
    }

    @Override
    fun getString(): String? {
        return number
    }

    @Override
    fun getBoolean(defaultValue: Boolean?): Boolean? {
        return try {
            getBigDecimal().compareTo(BigDecimal.ZERO) !== 0
        } catch (e: CasterException) {
            defaultValue
        }
    }

    @Throws(CasterException::class)
    fun getBigDecimal(): BigDecimal? {
        if (bd == null) bd = Caster.toBigDecimal(number)
        return bd
    }

    @Override
    @Throws(TransformerException::class)
    fun _writeOut(bc: BytecodeContext?, mode: Int): Type? {
        val adapter: GeneratorAdapter = bc.getAdapter()
        if (MODE_VALUE === mode) {
            try {
                adapter.push(getBigDecimal().doubleValue())
            } catch (e: CasterException) {
                TransformerException(bc, e, getStart())
            }
            // print.ds();
            return Types.DOUBLE_VALUE
        }
        val l: Long? = if (justNumberDigits(number)) Caster.toLong(number, null) else null
        if (l != null) {
            adapter.loadArg(0)
            adapter.push(l.longValue())
            adapter.invokeStatic(LITERAL_VALUE, TO_NUMBER_LONG_VALUE)
        } else {
            adapter.loadArg(0)
            adapter.push(number)
            adapter.invokeStatic(LITERAL_VALUE, TO_NUMBER_STRING)
        }

        // adapter.invokeStatic(Types.CASTER, Methods.METHOD_TO_BIG_DECIMAL_STR); // TODOX call constructor
        // directly
        return Types.NUMBER
    }

    companion object {
        private val LITERAL_VALUE: Type? = Type.getType(LiteralValue::class.java)
        private val CONSTR_STRING: Method? = Method("<init>", Types.VOID, arrayOf<Type?>(Types.STRING))
        private val VALUE_OF: Method? = Method("valueOf", Types.BIG_DECIMAL, arrayOf<Type?>(Types.LONG_VALUE))
        private val TO_NUMBER_LONG_VALUE: Method? = Method("toNumber", Types.NUMBER, arrayOf<Type?>(Types.PAGE_CONTEXT, Types.LONG_VALUE))
        private val TO_NUMBER_STRING: Method? = Method("toNumber", Types.NUMBER, arrayOf<Type?>(Types.PAGE_CONTEXT, Types.STRING))
        private fun justNumberDigits(number: String?): Boolean {
            for (c in number.toCharArray()) {
                if (c >= '0' && c <= '9') continue
                return false
            }
            return true
        }
    }
}