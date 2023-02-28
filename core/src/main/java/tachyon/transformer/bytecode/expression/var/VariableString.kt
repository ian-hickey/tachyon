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

import java.util.ArrayList

class VariableString(expr: Expression?) : ExpressionBase(expr.getFactory(), expr.getStart(), expr.getEnd()), ExprString {
    private val expr: Expression?
    @Override
    @Throws(TransformerException::class)
    fun _writeOut(bc: BytecodeContext?, mode: Int): Type? {
        return (translateVariableToExprString(bc, expr, false) as ExpressionBase?).writeOutAsType(bc, mode)
    }

    @Throws(TransformerException::class)
    fun castToString(bc: BytecodeContext?): String? {
        return translateVariableToString(bc, expr, false)
    }

    companion object {
        fun toExprString(expr: Expression?): ExprString? {
            return if (expr is ExprString) expr as ExprString? else VariableString(expr)
        }

        @Throws(TransformerException::class)
        fun translateVariableToExprString(bc: BytecodeContext?, expr: Expression?, rawIfPossible: Boolean): ExprString? {
            return if (expr is ExprString) expr as ExprString? else expr.getFactory().createLitString(translateVariableToString(bc, expr, rawIfPossible), expr.getStart(), expr.getEnd())
        }

        @Throws(TransformerException::class)
        private fun translateVariableToString(bc: BytecodeContext?, expr: Expression?, rawIfPossible: Boolean): String? {
            if (expr !is Variable) throw TransformerException(bc, "can't translate value to a string", expr.getStart())
            return variableToString(bc, expr as Variable?, rawIfPossible)
        }

        @Throws(TransformerException::class)
        fun variableToString(bc: BytecodeContext?, `var`: Variable?, rawIfPossible: Boolean): String? {
            return tachyon.runtime.type.util.ListUtil.arrayToList(variableToStringArray(bc, `var`, rawIfPossible), ".")
        }

        @Throws(TransformerException::class)
        fun variableToStringArray(bc: BytecodeContext?, `var`: Variable?, rawIfPossible: Boolean): Array<String?>? {
            val members: List<Member?> = `var`.getMembers()
            val arr: List<String?> = ArrayList<String?>()
            if (`var`.getScope() !== Scope.SCOPE_UNDEFINED) arr.add(ScopeFactory.toStringScope(`var`.getScope(), "undefined"))
            val it: Iterator<Member?> = members.iterator()
            val dm: DataMember
            val n: Expression
            while (it.hasNext()) {
                val o: Object = it.next() as? DataMember
                        ?: throw TransformerException(bc, "can't translate Variable to a String", `var`.getStart())
                dm = o as DataMember
                n = dm.getName()
                if (n is Literal) {
                    if (rawIfPossible && n is Identifier) {
                        arr.add((n as Identifier).getRaw())
                    } else {
                        arr.add((n as Literal).getString())
                    }
                } else throw TransformerException(bc, "argument name must be a constant value", `var`.getStart())
            }
            return arr.toArray(arrayOfNulls<String?>(arr.size()))
        }
    }

    init {
        this.expr = expr
    }
}