/**
 *
 * Copyright (c) 2014, the Railo Company Ltd. All rights reserved.
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
package tachyon.runtime.sql.old

import java.util.Vector

// Referenced classes of package Zql:
//            ZExp, ZQuery, ZConstant, ZUtils
class ZExpression : ZExp {
    constructor(s: String?) {
        operator = null
        operands_ = null
        operator = String(s)
    }

    constructor(s: String?, zexp: ZExp?) {
        operator = null
        operands_ = null
        operator = String(s)
        addOperand(zexp)
    }

    constructor(s: String?, zexp: ZExp?, zexp1: ZExp?) {
        operator = null
        operands_ = null
        operator = String(s)
        addOperand(zexp)
        addOperand(zexp1)
    }

    var operands: Vector?
        get() = operands_
        set(vector) {
            operands_ = vector
        }

    fun addOperand(zexp: ZExp?) {
        if (operands_ == null) operands_ = Vector()
        operands_.addElement(zexp)
    }

    fun getOperand(i: Int): ZExp? {
        return if (operands_ == null || i >= operands_.size()) null else operands_.elementAt(i)
    }

    fun nbOperands(): Int {
        return if (operands_ == null) 0 else operands_.size()
    }

    fun toReversePolish(): String? {
        val stringbuffer = StringBuffer("(")
        stringbuffer.append(operator)
        for (i in 0 until nbOperands()) {
            val zexp: ZExp? = getOperand(i)
            if (zexp is ZExpression) stringbuffer.append(" " + (zexp as ZExpression?)!!.toReversePolish()) else if (zexp is ZQuery) stringbuffer.append(" (" + zexp.toString().toString() + ")") else stringbuffer.append(" " + zexp.toString())
        }
        stringbuffer.append(")")
        return stringbuffer.toString()
    }

    @Override
    override fun toString(): String {
        if (operator!!.equals("?")) return operator!!
        if (ZUtils.isCustomFunction(operator) > 0) return formatFunction()!!
        val stringbuffer = StringBuffer()
        if (needPar(operator)) stringbuffer.append("(")
        when (nbOperands()) {
            1 -> {
                val zexp: ZExp? = getOperand(0)
                if (zexp is ZConstant) {
                    if (ZUtils.isAggregate(operator)) stringbuffer.append(operator.toString() + "(" + zexp.toString() + ")") else stringbuffer.append(operator.toString() + " " + zexp.toString())
                } else if (zexp is ZQuery) stringbuffer.append(operator.toString() + " (" + zexp.toString() + ")") else stringbuffer.append(operator.toString() + " " + zexp.toString())
            }
            3 -> {
                if (operator.toUpperCase().endsWith("BETWEEN")) {
                    stringbuffer.append(getOperand(0).toString().toString() + " " + operator + " " + getOperand(1).toString() + " AND " + getOperand(2).toString())
                    break
                }
                val flag = operator!!.equals("IN") || operator!!.equals("NOT IN")
                val i = nbOperands()
                var j = 0
                while (j < i) {
                    if (flag && j == 1) stringbuffer.append(" " + operator + " (")
                    val zexp1: ZExp? = getOperand(j)
                    if (zexp1 is ZQuery && !flag) stringbuffer.append("(" + zexp1.toString().toString() + ")") else stringbuffer.append(zexp1.toString())
                    if (j < i - 1) if (operator!!.equals(",") || flag && j > 0) stringbuffer.append(", ") else if (!flag) stringbuffer.append(" " + operator + " ")
                    j++
                }
                if (flag) stringbuffer.append(")")
            }
            else -> {
                val flag = operator!!.equals("IN") || operator!!.equals("NOT IN")
                val i = nbOperands()
                var j = 0
                while (j < i) {
                    if (flag && j == 1) stringbuffer.append(" " + operator + " (")
                    val zexp1: ZExp? = getOperand(j)
                    if (zexp1 is ZQuery && !flag) stringbuffer.append("(" + zexp1.toString().toString() + ")") else stringbuffer.append(zexp1.toString())
                    if (j < i - 1) if (operator!!.equals(",") || flag && j > 0) stringbuffer.append(", ") else if (!flag) stringbuffer.append(" " + operator + " ")
                    j++
                }
                if (flag) stringbuffer.append(")")
            }
        }
        if (needPar(operator)) stringbuffer.append(")")
        return stringbuffer.toString()
    }

    private fun needPar(s: String?): Boolean {
        var s = s
        s = s.toUpperCase()
        return !s!!.equals("ANY") && !s.equals("ALL") && !s.equals("UNION") && !ZUtils.isAggregate(s)
    }

    private fun formatFunction(): String? {
        val stringbuffer = StringBuffer(operator.toString() + "(")
        val i = nbOperands()
        for (j in 0 until i) stringbuffer.append(getOperand(j).toString().toString() + if (j >= i - 1) "" else ",")
        stringbuffer.append(")")
        return stringbuffer.toString()
    }

    var operator: String?
    var operands_: Vector?
}