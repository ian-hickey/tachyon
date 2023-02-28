package lucee.transformer.expression.literal

import lucee.transformer.expression.ExprNumber

interface LitNumber : Literal, ExprNumber {
    /**
     * @return return value as a boolean value
     */
    fun getNumber(): Number?
}