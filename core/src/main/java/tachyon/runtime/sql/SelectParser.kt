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
package tachyon.runtime.sql

import java.util.ArrayList

class SelectParser {
    /*
	 * SELECT [{LIMIT <offset> <limit> | TOP <limit>}[1]][ALL | DISTINCT] { <selectExpression> | table.*
	 * | * } [, ...] [INTO [CACHED | TEMP | TEXT][1] newTable] FROM tableList [WHERE Expression] [GROUP
	 * BY Expression [, ...]] [HAVING Expression] [{ UNION [ALL | DISTINCT] | {MINUS [DISTINCT] | EXCEPT
	 * [DISTINCT] } | INTERSECT [DISTINCT] } selectStatement] [ORDER BY orderExpression [, ...]] [LIMIT
	 * <limit> [OFFSET <offset>]];
	 */
    private var columnIndex = 0
    private var allColumns: Set<String?>? = HashSet<String?>()
    private var cachingColumn = true

    // select <select-statement> from <tables> where <where-statement>
    @Throws(SQLParserException::class)
    fun parse(sql: String?): Selects? {
        columnIndex = 0
        val raw = ParserString(sql.trim())
        val selects = Selects()
        var select: Select? = Select()
        var runAgain = false
        do {

            // select
            if (!raw.forwardIfCurrentAndNoWordNumberAfter("select")) throw SQLParserException("missing select")
            raw.removeSpace()

            // top
            if (raw.forwardIfCurrentAndNoWordNumberAfter("top")) {
                raw.removeSpace()
                val number: ValueNumber = number(raw) ?: throw SQLParserException("missing top number")
                select.setTop(number)
                raw.removeSpace()
            }

            // distinct
            if (raw.forwardIfCurrentAndNoWordNumberAfter("distinct")) {
                select.setDistinct(true)
                raw.removeSpace()
            }

            // all
            if (raw.forwardIfCurrentAndNoWordNumberAfter("all")) {
                select.setDistinct(false)
                raw.removeSpace()
            }

            // select expression
            selectExpressions(raw, select)
            raw.removeSpace()

            // from
            if (!raw.forwardIfCurrentAndNoWordNumberAfter("from")) throw SQLParserException("missing from")
            tableList(raw, select)
            raw.removeSpace()

            // where
            if (raw.forwardIfCurrentAndNoWordNumberAfter("where")) whereExpressions(raw, select)
            raw.removeSpace()

            // group by
            if (raw.forwardIfCurrentAndNoWordNumberAfter("group")) {
                raw.removeSpace()
                if (raw.forwardIfCurrentAndNoWordNumberAfter("by")) {
                    groupByExpressions(raw, select)
                    raw.removeSpace()

                    // having
                    if (raw.forwardIfCurrentAndNoWordNumberAfter("having")) havingExpressions(raw, select)
                    raw.removeSpace()
                } else {
                    throw SQLParserException("Incomplete group by clause (stop at:" + raw.getCurrent().toString() + ")")
                }
            }
            select!!.calcAdditionalColumns(allColumns)
            allColumns = HashSet<String?>()
            selects!!.addSelect(select)
            runAgain = false
            // union
            if (raw.forwardIfCurrentAndNoWordNumberAfter("union")) {
                select = Select()
                select.setUnionDistinct(true)
                raw.removeSpace()
                // "union all" does not distinct whilst combing the selects
                if (raw.forwardIfCurrentAndNoWordNumberAfter("all")) {
                    raw.removeSpace()
                    select.setUnionDistinct(false)
                }
                // "union distinct" is the same as "union"
                raw.forwardIfCurrentAndNoWordNumberAfter("distinct")
                raw.removeSpace()
                runAgain = true
            }
        } while (runAgain)

        // order by
        if (raw.forwardIfCurrentAndNoWordNumberAfter("order")) {
            raw.removeSpace()
            if (raw.forwardIfCurrentAndNoWordNumberAfter("by")) {
                orderByExpressions(raw, selects)
            } else {
                throw SQLParserException("Incomplete order by clause (stop at:" + raw.getCurrent().toString() + ")")
            }
        }
        raw.removeSpace()
        if (raw.forwardIfCurrent(';')) raw.removeSpace()
        if (!raw.isAfterLast()) throw SQLParserException("can not read the full sql statement (stop at:" + raw.getCurrent().toString() + ")")
        return selects
    }

    @Throws(SQLParserException::class)
    private fun orderByExpressions(raw: ParserString?, selects: Selects?) {
        var exp: Expression? = null
        do {
            raw.removeSpace()
            // print.out(raw.getCurrent());
            exp = expression(raw)
            // if (!(exp instanceof Column)) throw new SQLParserException("invalid order by part of
            // query" + exp.getClass().getName());
            // Column col = (Column) exp;
            raw.removeSpace()
            if (raw.forwardIfCurrent("desc")) exp.setDirectionBackward(true)
            if (raw.forwardIfCurrent("asc")) exp.setDirectionBackward(false)
            selects!!.addOrderByExpression(exp)
            raw.removeSpace()
        } while (raw.forwardIfCurrent(','))
        raw.removeSpace()
    }

    @Throws(SQLParserException::class)
    private fun whereExpressions(raw: ParserString?, select: Select?) {
        raw.removeSpace()
        val exp: Expression = expression(raw) ?: throw SQLParserException("missing where expression")
        if (exp !is Operation) throw SQLParserException("invalid where expression (" + Caster.toClassName(exp).toString() + ")")
        select!!.setWhereExpression(exp as Operation)
        raw.removeSpace()
    }

    @Throws(SQLParserException::class)
    private fun havingExpressions(raw: ParserString?, select: Select?) {
        raw.removeSpace()
        cachingColumn = false
        val exp: Expression? = expression(raw)
        cachingColumn = true
        if (exp == null) throw SQLParserException("missing having expression")
        if (exp !is Operation) throw SQLParserException("invalid having expression")
        select.setHaving(exp as Operation?)
        raw.removeSpace()
    }

    @Throws(SQLParserException::class)
    private fun groupByExpressions(raw: ParserString?, select: Select?) {
        var exp: Expression? = null
        do {
            raw.removeSpace()
            // print.out(raw.getCurrent());
            exp = expression(raw)
            if (exp is OperationAggregate) {
                throw SQLParserException("Cannot use an aggregate [" + exp.toString(true).toString() + "] in GROUP BY clause")
            }
            select!!.addGroupByExpression(exp)
            raw.removeSpace()
        } while (raw.forwardIfCurrent(','))
        raw.removeSpace()
    }

    @Throws(SQLParserException::class)
    private fun tableList(raw: ParserString?, select: Select?) {
        var column: Column? = null
        var exp: Expression? = null
        do {
            raw.removeSpace()
            exp = column(raw)
            if (exp !is Column) throw SQLParserException("invalid table definition")
            column = exp as Column?
            raw.removeSpace()
            if (raw.forwardIfCurrent("as ")) {
                val alias = identifier(raw, RefBooleanImpl(false))
                        ?: throw SQLParserException("missing alias in select part")
                column.setAlias(alias)
            } else {
                val start: Int = raw.getPos()
                val hasBracked: RefBoolean = RefBooleanImpl(false)
                val alias = identifier(raw, hasBracked) // TODO having usw
                if (!hasBracked.toBooleanValue()) {
                    if ("where".equalsIgnoreCase(alias)) raw.setPos(start) else if ("group".equalsIgnoreCase(alias)) raw.setPos(start) else if ("having".equalsIgnoreCase(alias)) raw.setPos(start) else if ("union".equalsIgnoreCase(alias)) raw.setPos(start) else if ("order".equalsIgnoreCase(alias)) raw.setPos(start) else if ("limit".equalsIgnoreCase(alias)) raw.setPos(start) else if (alias != null) column.setAlias(alias)
                } else {
                    if (alias != null) column.setAlias(alias)
                }
            }
            select!!.addFromExpression(column)
            raw.removeSpace()
        } while (raw.forwardIfCurrent(','))
    }

    // { (selectStatement) [AS] label | tableName [AS] label}
    @Throws(SQLParserException::class)
    private fun selectExpressions(raw: ParserString?, select: Select?) {
        var exp: Expression? = null
        do {
            raw.removeSpace()
            exp = expression(raw)
            if (exp == null) throw SQLParserException("missing expression in select part of query")
            raw.removeSpace()
            if (raw.forwardIfCurrent("as ")) {
                val alias = identifier(raw, RefBooleanImpl(false))
                        ?: throw SQLParserException("missing alias in select part")
                exp.setAlias(alias)
            } else {
                val start: Int = raw.getPos()
                val hb: RefBoolean = RefBooleanImpl(false)
                val alias = identifier(raw, hb)
                if (!hb.toBooleanValue() && "from".equalsIgnoreCase(alias)) raw.setPos(start) else if (alias != null) exp.setAlias(alias)
            }
            select!!.addSelectExpression(exp)
            raw.removeSpace()
        } while (raw.forwardIfCurrent(','))
    }

    @Throws(SQLParserException::class)
    private fun expression(raw: ParserString?): Expression? {
        return xorOp(raw)
    }

    @Throws(SQLParserException::class)
    private fun xorOp(raw: ParserString?): Expression? {
        var expr: Expression? = orOp(raw)
        while (raw.forwardIfCurrentAndNoWordNumberAfter("xor")) {
            raw.removeSpace()
            expr = Operation2(expr, orOp(raw), Operation.OPERATION2_XOR)
        }
        return expr
    }

    @Throws(SQLParserException::class)
    private fun orOp(raw: ParserString?): Expression? {
        var expr: Expression? = andOp(raw)
        while (raw.forwardIfCurrentAndNoWordNumberAfter("or")) {
            raw.removeSpace()
            expr = Operation2(expr, andOp(raw), Operation.OPERATION2_OR)
        }
        return expr
    }

    @Throws(SQLParserException::class)
    private fun andOp(raw: ParserString?): Expression? {
        var expr: Expression? = notOp(raw)
        while (raw.forwardIfCurrentAndNoWordNumberAfter("and")) {
            raw.removeSpace()
            expr = Operation2(expr, notOp(raw), Operation.OPERATION2_AND)
        }
        return expr
    }

    @Throws(SQLParserException::class)
    private fun notOp(raw: ParserString?): Expression? {
        // NOT
        if (raw.forwardIfCurrentAndNoWordNumberAfter("not")) {
            raw.removeSpace()
            return Operation1(decsionOp(raw), Operation.OPERATION1_NOT)
        }
        return decsionOp(raw)
    }

    @Throws(SQLParserException::class)
    private fun decsionOp(raw: ParserString?): Expression? {
        var expr: Expression? = plusMinusOp(raw)
        var hasChanged = false
        do {
            hasChanged = false

            // value BETWEEN value AND value
            if (raw.forwardIfCurrent("between ")) {
                raw.removeSpace()
                val left: Expression? = plusMinusOp(raw)
                raw.removeSpace()
                if (!raw.forwardIfCurrent("and ")) throw SQLParserException("invalid operation (between) missing operator and")
                raw.removeSpace()
                val right: Expression? = plusMinusOp(raw)
                raw.removeSpace()
                expr = Operation3(expr, left, right, Operation.OPERATION3_BETWEEN)
                hasChanged = true
            } else if (raw.forwardIfCurrentAndNoWordNumberAfter("like")) {
                raw.removeSpace()
                val left: Expression? = plusMinusOp(raw)
                raw.removeSpace()
                if (raw.forwardIfCurrentAndNoWordNumberAfter("escape ")) {
                    raw.removeSpace()
                    val right: Expression? = plusMinusOp(raw)
                    raw.removeSpace()
                    expr = Operation3(expr, left, right, Operation.OPERATION3_LIKE)
                } else {
                    raw.removeSpace()
                    expr = Operation2(expr, left, Operation.OPERATION2_LIKE)
                }
                hasChanged = true
            } else if (raw.isCurrent("is ")) {
                val start: Int = raw.getPos()
                if (raw.forwardIfCurrentAndNoWordNumberAfter("is", "null")) {
                    raw.removeSpace()
                    return Operation1(expr, Operation.OPERATION1_IS_NULL)
                } else if (raw.forwardIfCurrentAndNoWordNumberAfter("is", "not", "null")) {
                    raw.removeSpace()
                    return Operation1(expr, Operation.OPERATION1_IS_NOT_NULL)
                } else {
                    raw.setPos(start)
                    raw.removeSpace()
                }
            } else if (raw.forwardIfCurrent("not", "in", '(')) {
                expr = OperationN("not_in", readArguments(raw, expr))
                hasChanged = true
            } else if (raw.forwardIfCurrent("in", '(')) {
                expr = OperationN("in", readArguments(raw, expr))
                hasChanged = true
            }
            // not like
            if (raw.forwardIfCurrentAndNoWordNumberAfter("not", "like")) {
                expr = decisionOpCreate(raw, Operation.OPERATION2_NOT_LIKE, expr)
                hasChanged = true
            } else if (raw.forwardIfCurrent('=')) {
                expr = decisionOpCreate(raw, Operation.OPERATION2_EQ, expr)
                hasChanged = true
            } else if (raw.forwardIfCurrent("!=")) {
                expr = decisionOpCreate(raw, Operation.OPERATION2_NEQ, expr)
                hasChanged = true
            } else if (raw.forwardIfCurrent("<>")) {
                expr = decisionOpCreate(raw, Operation.OPERATION2_LTGT, expr)
                hasChanged = true
            } else if (raw.isCurrent('<')) {
                if (raw.forwardIfCurrent("<=")) {
                    expr = decisionOpCreate(raw, Operation.OPERATION2_LTE, expr)
                    hasChanged = true
                } else {
                    raw.next()
                    expr = decisionOpCreate(raw, Operation.OPERATION2_LT, expr)
                    hasChanged = true
                }
            } else if (raw.isCurrent('>')) {
                if (raw.forwardIfCurrent("=>")) {
                    expr = decisionOpCreate(raw, Operation.OPERATION2_GTE, expr)
                    hasChanged = true
                }
                if (raw.forwardIfCurrent(">=")) {
                    expr = decisionOpCreate(raw, Operation.OPERATION2_GTE, expr)
                    hasChanged = true
                } else {
                    raw.next()
                    expr = decisionOpCreate(raw, Operation.OPERATION2_GT, expr)
                    hasChanged = true
                }
            }
        } while (hasChanged)
        return expr
    }

    @Throws(SQLParserException::class)
    private fun decisionOpCreate(raw: ParserString?, operation: Int, left: Expression?): Expression? {
        raw.removeSpace()
        return Operation2(left, plusMinusOp(raw), operation)
    }

    @Throws(SQLParserException::class)
    private fun plusMinusOp(raw: ParserString?): Expression? {
        var expr: Expression? = modOp(raw)
        while (!raw.isLast()) {

            // Plus Operation
            if (raw.forwardIfCurrent('+')) {
                raw.removeSpace()
                expr = Operation2(expr, modOp(raw), Operation.OPERATION2_PLUS)
            } else if (raw.forwardIfCurrent('-')) {
                raw.removeSpace()
                expr = Operation2(expr, modOp(raw), Operation.OPERATION2_MINUS)
            } else break
        }
        return expr
    }

    @Throws(SQLParserException::class)
    private fun modOp(raw: ParserString?): Expression? {
        var expr: Expression? = divMultiOp(raw)

        // Modulus Operation
        while (raw.forwardIfCurrent('%')) {
            raw.removeSpace()
            expr = Operation2(expr, divMultiOp(raw), Operation.OPERATION2_MOD)
        }
        return expr
    }

    @Throws(SQLParserException::class)
    private fun divMultiOp(raw: ParserString?): Expression? {
        var expr: Expression? = expoOp(raw)
        while (!raw.isLast()) {
            // Multiply Operation
            if (raw.forwardIfCurrent('*')) {
                raw.removeSpace()
                expr = Operation2(expr, expoOp(raw), Operation.OPERATION2_MULTIPLY)
            } else if (raw.forwardIfCurrent('/')) {
                raw.removeSpace()
                expr = Operation2(expr, expoOp(raw), Operation.OPERATION2_DIVIDE)
            } else {
                break
            }
        }
        return expr
    }

    @Throws(SQLParserException::class)
    private fun expoOp(raw: ParserString?): Expression? {
        var exp: Expression? = negateMinusOp(raw)

        // Modulus Operation
        while (raw.forwardIfCurrent('^')) {
            raw.removeSpace()
            exp = Operation2(exp, negateMinusOp(raw), Operation.OPERATION2_BITWISE)
        }
        return exp
    }

    @Throws(SQLParserException::class)
    private fun negateMinusOp(raw: ParserString?): Expression? {
        // And Operation
        if (raw.forwardIfCurrent('-')) {
            raw.removeSpace()
            return Operation1(clip(raw), Operation.OPERATION1_MINUS)
        } else if (raw.forwardIfCurrent('+')) {
            raw.removeSpace()
            return Operation1(clip(raw), Operation.OPERATION1_PLUS)
        }
        return clip(raw)
    }

    // { Expression | COUNT(*) | {COUNT | MIN | MAX | SUM | AVG | SOME | EVERY | VAR_POP | VAR_SAMP
    // |
    // STDDEV_POP | STDDEV_SAMP} ([ALL | DISTINCT][1]] Expression) } [[AS] label]
    @Throws(SQLParserException::class)
    private fun clip(raw: ParserString?): Expression? {
        var exp: Expression? = column(raw)
        // if(exp==null)exp=brackedColumn(raw);
        if (exp == null) exp = date(raw)
        if (exp == null) exp = bracked(raw)
        if (exp == null) exp = number(raw)
        if (exp == null) exp = string(raw)

        // If there is a random : laying around like :id where we expected a column or value, it's
        // likley a named param and the user forgot to pass their params to the query.
        if (exp == null && raw.isCurrent(":")) {
            var name = ""
            val pos: Int = raw.getPos()
            // Strip out the next word to show the user what was after their errant :
            do {
                if (raw.isCurrentWhiteSpace() || raw.isCurrent(")")) break
                name += raw.getCurrent()
                raw.next()
            } while (raw.isValidIndex())
            throw SQLParserException("Unexpected token [$name] found at position $pos. Did you forget to specify all your named params?") // Need to sneak this past Java's checked exception types
                    .initCause(IllegalQoQException("Unsupported SQL", "", null, null)) as SQLParserException
        }
        return exp
    }

    @Throws(SQLParserException::class)
    private fun bracked(raw: ParserString?): Expression? {
        if (!raw.forwardIfCurrent('(')) return null
        raw.removeSpace()
        val exp: Expression? = expression(raw)
        raw.removeSpace()
        if (!raw.forwardIfCurrent(')')) throw SQLParserException("missing closing )")
        raw.removeSpace()
        return exp // new BracketExpression(exp);
    }

    @Throws(SQLParserException::class)
    private fun column(raw: ParserString?): Expression? {
        val hb: RefBoolean = RefBooleanImpl(false)
        val name = identifier(raw, hb) ?: return null
        if (!hb.toBooleanValue()) {
            if ("true".equalsIgnoreCase(name)) return ValueBoolean(true)
            if ("false".equalsIgnoreCase(name)) return ValueBoolean(false)
            if ("null".equalsIgnoreCase(name)) return ValueNull()
        }
        val column = ColumnExpression(name, if (name.equals("?")) columnIndex++ else 0, cachingColumn)
        allColumns.add(column.getColumnName())
        raw.removeSpace()
        while (raw.forwardIfCurrent(".")) {
            raw.removeSpace()
            val sub = identifier(raw, hb) ?: throw SQLParserException("invalid column definition")
            column.setSub(sub)
        }
        raw.removeSpace()
        if (raw.forwardIfCurrent('(')) {
            val thisName: String = column.getFullName().toLowerCase()
            return if (thisName.equals("avg") || thisName.equals("max") || thisName.equals("min") || thisName.equals("sum")) {
                OperationAggregate(thisName, readArguments(raw))
            } else if (thisName.equals("count")) {
                raw.removeSpace()
                if (raw.forwardIfCurrent("all")) {
                    return OperationAggregate(thisName, readArguments(raw, ValueString("all")))
                }
                if (raw.forwardIfCurrent("distinct")) {
                    OperationAggregate(thisName, readArguments(raw, ValueString("distinct")))
                } else OperationAggregate(thisName, readArguments(raw))
            } else {
                OperationN(thisName, readArguments(raw))
            }
        }
        return column
    }

    @Throws(SQLParserException::class)
    private fun readArguments(raw: ParserString?): List? {
        return readArguments(raw, null)
    }

    @Throws(SQLParserException::class)
    private fun readArguments(raw: ParserString?, exp: Expression?): List? {
        val args: List = ArrayList()
        var arg: Expression?
        if (exp != null) args.add(exp)
        do {
            raw.removeSpace()
            if (raw.isCurrent(')')) break
            args.add(expression(raw).also { arg = it })
            raw.removeSpace()
            // check for alias
            if (raw.forwardIfCurrent("as ")) {
                raw.removeSpace()
                arg.setAlias(identifier(raw, null))
                raw.removeSpace()
            }
        } while (raw.forwardIfCurrent(','))
        if (!raw.forwardIfCurrent(')')) throw SQLParserException("missing closing )")
        raw.removeSpace()
        return args
    }

    @Throws(SQLParserException::class)
    private fun number(raw: ParserString?): ValueNumber? {
        // check first character is a number literal representation
        if (!(raw.isCurrentBetween('0', '9') || raw.isCurrent('.'))) return null
        val rtn = StringBuffer()

        // get digit on the left site of the dot
        if (raw.isCurrent('.')) rtn.append('0') else rtn.append(digit(raw))
        // read dot if exist
        if (raw.forwardIfCurrent('.')) {
            rtn.append('.')
            var rightSite = digit(raw)
            if (rightSite!!.length() > 0 && raw.forwardIfCurrent('e')) {
                if (raw.isCurrentBetween('0', '9')) {
                    rightSite += 'e' + digit(raw)
                } else {
                    raw.previous()
                }
            }
            // read right side of the dot
            if (rightSite.length() === 0) throw SQLParserException("Number can't end with [.]")
            rtn.append(rightSite)
        }
        raw.removeSpace()
        return ValueNumber(rtn.toString())
    }

    private fun digit(raw: ParserString?): String? {
        var rtn = ""
        while (raw.isValidIndex()) {
            if (!raw.isCurrentBetween('0', '9')) break
            rtn += raw.getCurrentLower()
            raw.next()
        }
        return rtn
    }

    @Throws(SQLParserException::class)
    private fun string(raw: ParserString?): ValueString? {

        // check starting character for a string literal
        if (!raw.isCurrent('\'')) return null

        // Init Parameter
        val str = StringBuffer()
        while (raw.hasNext()) {
            raw.next()

            // check quoter
            if (raw.isCurrent('\'')) {
                // Ecaped sharp
                if (raw.isNext('\'')) {
                    raw.next()
                    str.append('\'')
                } else {
                    break
                }
            } else {
                str.append(raw.getCurrent())
            }
        }
        if (!raw.forwardIfCurrent('\'')) throw SQLParserException("Invalid Syntax Closing ['] not found")
        raw.removeSpace()
        return ValueString(str.toString())
    }

    @Throws(SQLParserException::class)
    private fun date(raw: ParserString?): ValueDate? {
        if (!raw.isCurrent('{')) return null

        // Init Parameter
        val str = StringBuilder()
        while (raw.hasNext()) {
            raw.next()
            if (raw.isCurrent('}')) break
            str.append(raw.getCurrent())
        }
        if (!raw.forwardIfCurrent('}')) throw SQLParserException("Invalid Syntax Closing [}] not found")
        raw.removeSpace()
        return try {
            ValueDate("{" + str.toString().toString() + "}")
        } catch (e: PageException) {
            throw SQLParserException("can't cast value [{" + str.toString().toString() + "}] to date object")
        }
    }

    @Throws(SQLParserException::class)
    private fun identifier(raw: ParserString?, hasBracked: RefBoolean?): String? {
        if (hasBracked != null && raw.forwardIfCurrent('[')) {
            hasBracked.setValue(true)
            return identifierBracked(raw)
        } else if (!(raw.isCurrentLetter() || raw.isCurrent('*') || raw.isCurrent('?') || raw.isCurrent('_'))) return null
        val start: Int = raw.getPos()
        var first = true
        do {
            raw.next()
            if (first && !(raw.isCurrentLetter() || raw.isCurrentBetween('0', '9') || raw.isCurrent('*') || raw.isCurrent('?') || raw.isCurrent('_'))) {
                break
            } else if (!(raw.isCurrentLetter() || raw.isCurrentBetween('0', '9') || raw.isCurrent('_'))) {
                break
            }
            first = false
        } while (raw.isValidIndex())
        val str: String = raw.substring(start, raw.getPos() - start)
        raw.removeSpace()
        return str
    }

    @Throws(SQLParserException::class)
    private fun identifierBracked(raw: ParserString?): String? {
        val start: Int = raw.getPos()
        do {
            raw.next()
        } while (raw.isValidIndex() && !raw.isCurrent(']'))
        val str: String = raw.substring(start, raw.getPos() - start)
        if (!raw.forwardIfCurrent(']')) throw SQLParserException("missing ending ] of identifier")
        return str
    }

    companion object {
        fun main(args: Array<String?>?) {}
    }
}