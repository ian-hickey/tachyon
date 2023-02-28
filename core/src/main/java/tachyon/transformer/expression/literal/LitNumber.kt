package tachyon.transformer.expression.literal

import tachyon.transformer.expression.ExprNumber

interface LitNumber : Literal, ExprNumber {
    /**
     * @return return value as a boolean value
     */
    fun getNumber(): Number?
}