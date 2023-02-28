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
package lucee.runtime.sql.old

import java.sql.SQLException

// Referenced classes of package Zql:
//            ZExpression, ZConstant, ZExp, ZTuple, 
//            ZqlParser
class ZEval {
    @Throws(SQLException::class)
    fun eval(ztuple: ZTuple?, zexp: ZExp?): Boolean {
        if (ztuple == null || zexp == null) throw SQLException("ZEval.eval(): null argument or operator")
        if (zexp !is ZExpression) throw SQLException("ZEval.eval(): only expressions are supported")
        val zexpression: ZExpression? = zexp
        val s: String = zexpression.getOperator()
        if (s.equals("AND")) {
            var flag = true
            for (i in 0 until zexpression!!.nbOperands()) flag = flag and eval(ztuple, zexpression!!.getOperand(i))
            return flag
        }
        if (s.equals("OR")) {
            var flag1 = false
            for (j in 0 until zexpression!!.nbOperands()) flag1 = flag1 or eval(ztuple, zexpression!!.getOperand(j))
            return flag1
        }
        if (s.equals("NOT")) return !eval(ztuple, zexpression!!.getOperand(0))
        if (s.equals("=")) return evalCmp(ztuple, zexpression.getOperands()) == 0.0
        if (s.equals("!=")) return evalCmp(ztuple, zexpression.getOperands()) != 0.0
        if (s.equals("<>")) return evalCmp(ztuple, zexpression.getOperands()) != 0.0
        if (s.equals("#")) throw SQLException("ZEval.eval(): Operator # not supported")
        if (s.equals(">")) return evalCmp(ztuple, zexpression.getOperands()) > 0.0
        if (s.equals(">=")) return evalCmp(ztuple, zexpression.getOperands()) >= 0.0
        if (s.equals("<")) return evalCmp(ztuple, zexpression.getOperands()) < 0.0
        if (s.equals("<=")) return evalCmp(ztuple, zexpression.getOperands()) <= 0.0
        if (s.equals("BETWEEN") || s.equals("NOT BETWEEN")) {
            val zexpression1 = ZExpression("AND", ZExpression(">=", zexpression!!.getOperand(0), zexpression!!.getOperand(1)),
                    ZExpression("<=", zexpression!!.getOperand(0), zexpression!!.getOperand(2)))
            return if (s.equals("NOT BETWEEN")) !eval(ztuple, zexpression1) else eval(ztuple, zexpression1)
        }
        if (s.equals("LIKE") || s.equals("NOT LIKE")) throw SQLException("ZEval.eval(): Operator (NOT) LIKE not supported")
        if (s.equals("IN") || s.equals("NOT IN")) {
            val zexpression2 = ZExpression("OR")
            for (k in 1 until zexpression!!.nbOperands()) zexpression2!!.addOperand(ZExpression("=", zexpression!!.getOperand(0), zexpression!!.getOperand(k)))
            return if (s.equals("NOT IN")) !eval(ztuple, zexpression2) else eval(ztuple, zexpression2)
        }
        if (s.equals("IS NULL")) {
            if (zexpression!!.nbOperands() <= 0 || zexpression!!.getOperand(0) == null) return true
            val zexp1: ZExp = zexpression!!.getOperand(0)
            if (zexp1 is ZConstant) return zexp1.getType() === 1
            throw SQLException("ZEval.eval(): can't eval IS (NOT) NULL")
        }
        if (s.equals("IS NOT NULL")) {
            val zexpression3 = ZExpression("IS NULL")
            zexpression3.setOperands(zexpression.getOperands())
            return !eval(ztuple, zexpression3)
        }
        throw SQLException("ZEval.eval(): Unknown operator $s")
    }

    @Throws(SQLException::class)
    fun evalCmp(ztuple: ZTuple?, vector: Vector?): Double {
        if (vector.size() < 2) throw SQLException("ZEval.evalCmp(): Trying to compare less than two values")
        if (vector.size() > 2) throw SQLException("ZEval.evalCmp(): Trying to compare more than two values")
        var obj: Object? = null
        var obj1: Object? = null
        obj = evalExpValue(ztuple, vector.elementAt(0) as ZExp)
        obj1 = evalExpValue(ztuple, vector.elementAt(1) as ZExp)
        if (obj is String || obj1 is String) return if (obj.equals(obj1)) 0 else -1
        if (obj is Number && obj1 is Number) return (obj as Number?).doubleValue() - (obj1 as Number?).doubleValue()
        throw SQLException("ZEval.evalCmp(): can't compare (" + obj.toString().toString() + ") with (" + obj1.toString().toString() + ")")
    }

    @Throws(SQLException::class)
    fun evalNumericExp(ztuple: ZTuple?, zexpression: ZExpression?): Double {
        if (ztuple == null || zexpression == null || zexpression.getOperator() == null) throw SQLException("ZEval.eval(): null argument or operator")
        val s: String = zexpression.getOperator()
        val obj: Object = evalExpValue(ztuple, zexpression.getOperand(0)) as? Double
                ?: throw SQLException("ZEval.evalNumericExp(): expression not numeric")
        val double1 = obj as Double
        if (s.equals("+")) {
            var d: Double = double1.doubleValue()
            for (i in 1 until zexpression.nbOperands()) {
                val obj1: Object? = evalExpValue(ztuple, zexpression.getOperand(i))
                d += (obj1 as Number?).doubleValue()
            }
            return d
        }
        if (s.equals("-")) {
            var d1: Double = double1.doubleValue()
            if (zexpression.nbOperands() === 1) return -d1
            for (j in 1 until zexpression.nbOperands()) {
                val obj2: Object? = evalExpValue(ztuple, zexpression.getOperand(j))
                d1 -= (obj2 as Number?).doubleValue()
            }
            return d1
        }
        if (s.equals("*")) {
            var d2: Double = double1.doubleValue()
            for (k in 1 until zexpression.nbOperands()) {
                val obj3: Object? = evalExpValue(ztuple, zexpression.getOperand(k))
                d2 *= (obj3 as Number?).doubleValue()
            }
            return d2
        }
        if (s.equals("/")) {
            var d3: Double = double1.doubleValue()
            for (l in 1 until zexpression.nbOperands()) {
                val obj4: Object? = evalExpValue(ztuple, zexpression.getOperand(l))
                d3 /= (obj4 as Number?).doubleValue()
            }
            return d3
        }
        if (s.equals("**")) {
            var d4: Double = double1.doubleValue()
            for (i1 in 1 until zexpression.nbOperands()) {
                val obj5: Object? = evalExpValue(ztuple, zexpression.getOperand(i1))
                d4 = Math.pow(d4, (obj5 as Number?).doubleValue())
            }
            return d4
        }
        throw SQLException("ZEval.evalNumericExp(): Unknown operator $s")
    }

    @Throws(SQLException::class)
    fun evalExpValue(ztuple: ZTuple?, zexp: ZExp?): Object? {
        var obj: Object? = null
        if (zexp is ZConstant) {
            val zconstant: ZConstant? = zexp
            when (zconstant.getType()) {
                0 -> {
                    val obj1: Object = ztuple.getAttValue(zconstant.getValue())
                            ?: throw SQLException("ZEval.evalExpValue(): unknown column " + zconstant.getValue())
                    try {
                        obj = Double.valueOf(obj1.toString())
                    } catch (numberformatexception: NumberFormatException) {
                        obj = obj1
                    }
                }
                2 -> obj = Double.valueOf(zconstant.getValue())
                1, 3 -> obj = zconstant.getValue()
                else -> obj = zconstant.getValue()
            }
        } else if (zexp is ZExpression) obj = Double.valueOf(evalNumericExp(ztuple, zexp as ZExpression?))
        return obj
    } /*
	 * public static void main(String args[]) { try { BufferedReader bufferedreader = new
	 * BufferedReader(new FileReader("test.db")); String s = bufferedreader.readLine(); ZTuple ztuple =
	 * new ZTuple(s); ZqlParser zqlparser = new ZqlParser(); ZEval zeval = new ZEval(); while ((s =
	 * bufferedreader.readLine()) != null) { ztuple.setRow(s); BufferedReader bufferedreader1 = new
	 * BufferedReader(new FileReader("test.sql")); String s1; while ((s1 = bufferedreader1.readLine())
	 * != null) { zqlparser.initParser(new ByteArrayInputStream(s1.getBytes())); ZExp zexp =
	 * zqlparser.readExpression(); System.out.print(s + ", " + s1 + ", ");
	 * System.out.println(zeval.eval(ztuple, zexp)); } bufferedreader1.close(); }
	 * bufferedreader.close(); } catch (Exception exception) {
	 * 
	 * } }
	 */
}