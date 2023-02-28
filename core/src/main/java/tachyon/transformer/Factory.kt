/**
 * Copyright (c) 2015, Tachyon Assosication Switzerland. All rights reserved.
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
package tachyon.transformer

import java.math.BigDecimal

abstract class Factory {
    abstract fun TRUE(): LitBoolean?
    abstract fun FALSE(): LitBoolean?
    abstract fun EMPTY(): LitString?
    abstract fun NUMBER_ZERO(): LitNumber?
    abstract fun NUMBER_ONE(): LitNumber?
    abstract fun NULL(): Expression?

    // CREATION
    abstract fun createLitString(str: String?): LitString?
    abstract fun createLitString(str: String?, start: Position?, end: Position?): LitString?
    abstract fun createLitBoolean(b: Boolean): LitBoolean?
    abstract fun createLitBoolean(b: Boolean, start: Position?, end: Position?): LitBoolean?
    @Throws(PageException::class)
    abstract fun createLitNumber(number: String?): LitNumber?
    @Throws(PageException::class)
    abstract fun createLitNumber(number: String?, start: Position?, end: Position?): LitNumber?
    abstract fun createLitNumber(n: Number?): LitNumber?
    abstract fun createLitNumber(n: Number?, start: Position?, end: Position?): LitNumber?
    abstract fun createLitNumber(bd: BigDecimal?): LitNumber?
    abstract fun createLitNumber(bd: BigDecimal?, start: Position?, end: Position?): LitNumber?
    abstract fun createLitLong(l: Long): LitLong?
    abstract fun createLitLong(l: Long, start: Position?, end: Position?): LitLong?
    abstract fun createLitInteger(i: Int): LitInteger?
    abstract fun createLitInteger(i: Int, start: Position?, end: Position?): LitInteger?
    abstract fun createNull(): Expression?
    abstract fun createNull(start: Position?, end: Position?): Expression?
    abstract fun createNullConstant(start: Position?, end: Position?): Expression?
    abstract fun isNull(expr: Expression?): Boolean

    /**
     * return null if full null support is enabled, otherwise an empty string
     *
     * @return
     */
    abstract fun createEmpty(): Expression?
    abstract fun createLiteral(obj: Object?, defaultValue: Literal?): Literal?
    abstract fun createDataMember(name: ExprString?): DataMember?
    abstract fun createVariable(start: Position?, end: Position?): Variable?
    abstract fun createVariable(scope: Int, start: Position?, end: Position?): Variable?
    abstract fun createStruct(): Expression?
    abstract fun createArray(): Expression?

    // CASTING
    abstract fun toExprNumber(expr: Expression?): ExprNumber?
    abstract fun toExprString(expr: Expression?): ExprString?
    abstract fun toExprBoolean(expr: Expression?): ExprBoolean?
    abstract fun toExprInt(expr: Expression?): ExprInt?
    abstract fun toExpression(expr: Expression?, type: String?): Expression?

    // OPERATIONS
    abstract fun opString(left: Expression?, right: Expression?): ExprString?
    abstract fun opString(left: Expression?, right: Expression?, concatStatic: Boolean): ExprString?
    abstract fun opBool(left: Expression?, right: Expression?, operation: Int): ExprBoolean?
    abstract fun opNumber(left: Expression?, right: Expression?, operation: Int): ExprNumber?
    abstract fun opUnaryNumber(`var`: Variable?, value: Expression?, type: Short, operation: Int, start: Position?, end: Position?): ExprNumber?
    abstract fun opUnaryString(`var`: Variable?, value: Expression?, type: Short, operation: Int, start: Position?, end: Position?): ExprString?
    abstract fun opNegate(expr: Expression?, start: Position?, end: Position?): Expression?
    abstract fun opNegateNumber(expr: Expression?, operation: Int, start: Position?, end: Position?): ExprNumber?
    abstract fun opContional(cont: Expression?, left: Expression?, right: Expression?): Expression?
    abstract fun opDecision(left: Expression?, concatOp: Expression?, operation: Int): ExprBoolean?
    abstract fun opElvis(left: Variable?, right: Expression?): Expression?
    abstract fun removeCastString(expr: Expression?): Expression?

    // TODO more removes?
    @Throws(TransformerException::class)
    abstract fun registerKey(bc: Context?, name: Expression?, doUpperCase: Boolean)
    abstract fun getConfig(): Config?

    companion object {
        const val OP_BOOL_AND = 0
        const val OP_BOOL_OR = 1
        const val OP_BOOL_XOR = 2
        const val OP_BOOL_EQV = 3
        const val OP_BOOL_IMP = 4
        const val OP_DBL_PLUS = 0
        const val OP_DBL_MINUS = 1
        const val OP_DBL_MODULUS = 2
        const val OP_DBL_DIVIDE = 3
        const val OP_DBL_MULTIPLY = 4
        const val OP_DBL_EXP = 5
        const val OP_DBL_INTDIV = 6
        const val OP_UNARY_POST: Short = 1
        const val OP_UNARY_PRE: Short = 2

        // must always be alias to OP only
        const val OP_UNARY_PLUS = OP_DBL_PLUS
        const val OP_UNARY_MINUS = OP_DBL_MINUS
        const val OP_UNARY_DIVIDE = OP_DBL_DIVIDE
        const val OP_UNARY_MULTIPLY = OP_DBL_MULTIPLY
        const val OP_UNARY_CONCAT = 1001314342
        const val OP_DEC_LT = 1
        const val OP_DEC_LTE = 2
        const val OP_DEC_GTE = 3
        const val OP_DEC_GT = 4
        const val OP_DEC_EQ = 5
        const val OP_DEC_NEQ = 6
        const val OP_DEC_CT = 1000
        const val OP_DEC_NCT = 1001
        const val OP_DEC_EEQ = 1002
        const val OP_DEC_NEEQ = 1003
        const val OP_NEG_NBR_PLUS = 0
        const val OP_NEG_NBR_MINUS = 1
        fun canRegisterKey(name: Expression?): Boolean {
            return name is LitString
        }
    }
}