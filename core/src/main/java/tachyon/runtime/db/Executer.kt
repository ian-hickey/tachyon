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
package tachyon.runtime.db

import java.io.ByteArrayInputStream

/**
 *
 */
class Executer {
    /**
     * execute a SQL Statement against CFML Scopes
     *
     * @param pc PageContext of the Request
     * @param sql
     * @param maxrows
     * @return result
     * @throws PageException
     */
    @Throws(PageException::class)
    fun execute(statements: Vector, pc: PageContext?, sql: SQL?, maxrows: Int): QueryImpl {
        // parse sql
        if (statements.size() !== 1) throw DatabaseException("only one SQL Statement allowed at time", null, null, null)
        val query: ZQuery = statements.get(0) as ZQuery

        // single table
        if (query.getFrom().size() === 1) {
            return testExecute(pc, sql, getSingleTable(pc, query), query, maxrows)
        }
        throw DatabaseException("can only work with single tables yet", null, null, null)
    }

    @Throws(PageException::class)
    fun execute(pc: PageContext?, sql: SQL, prettySQL: String, maxrows: Int): QueryImpl {
        var prettySQL = prettySQL
        if (StringUtil.isEmpty(prettySQL)) prettySQL = SQLPrettyfier.prettyfie(sql.getSQLString())
        val parser = ZqlParser(ByteArrayInputStream(prettySQL.getBytes()))
        val statements: Vector
        statements = try {
            parser.readStatements()
        } catch (t: Throwable) {
            ExceptionUtil.rethrowIfNecessary(t)
            throw Caster.toPageException(t)
        }
        return execute(statements, pc, sql, maxrows)
    }

    @Throws(PageException::class)
    private fun testExecute(pc: PageContext, sql: SQL, qr: Query, query: ZQuery, maxrows: Int): QueryImpl {
        val recCount: Int = qr.getRecordcount()
        val vSelects: Vector = query.getSelect()
        val selCount: Int = vSelects.size()
        val selects: Map<Collection.Key, Object> = MapFactory.Key, Object>getConcurrentMap<Collection.Key?, Object?>()
        var isSMS = false
        // headers
        for (i in 0 until selCount) {
            val select: ZSelectItem = vSelects.get(i) as ZSelectItem
            if (select.isWildcard() || select.getColumn().equals(SQLPrettyfier.PLACEHOLDER_ASTERIX).also { isSMS = it }) {
                if (!isSMS && !select.getColumn().equals("*")) throw DatabaseException("can't execute this type of query at the moment", null, sql, null)
                // Collection.Key[] keys = qr.keys();
                val it: Iterator<Key> = qr.keyIterator()
                var k: Key
                while (it.hasNext()) {
                    k = it.next()
                    selects.put(k, k.getString())
                }
                isSMS = false
            } else {
                // if(SQLPrettyfier.PLACEHOLDER_COUNT.equals(select.getAlias())) select.setAlias("count");
                // if(SQLPrettyfier.PLACEHOLDER_COUNT.equals(select.getColumn())) select.setExpression(new
                // ZConstant("count",ZConstant.COLUMNNAME));
                var alias: String? = select.getAlias()
                val column: String = select.getColumn()
                if (alias == null) alias = column
                alias = alias.toLowerCase()
                selects.put(KeyImpl.init(alias), select)
            }
        }
        val headers: Array<Key> = selects.keySet().toArray(arrayOfNulls<Collection.Key>(selects.size()))

        // aHeaders.toArray(new String[aHeaders.size()]);
        val rtn = QueryImpl(headers, 0, "query", sql)

        // loop records
        val orders: Vector = query.getOrderBy()
        val where: ZExp = query.getWhere()
        // print.out(headers);
        // int newRecCount=0;
        val hasMaxrow = maxrows > -1 && (orders == null || orders.size() === 0)
        for (row in 1..recCount) {
            sql.setPosition(0)
            if (hasMaxrow && maxrows <= rtn.getRecordcount()) break
            val useRow = where == null || Caster.toBooleanValue(executeExp(pc, sql, qr, where, row))
            if (useRow) {
                rtn.addRow(1)
                for (cell in headers.indices) {
                    val value: Object? = selects[headers[cell]]
                    rtn.setAt(headers[cell], rtn.getRecordcount(), getValue(pc, sql, qr, row, headers[cell], value))
                }
            }
        }

        // Group By
        if (query.getGroupBy() != null) throw DatabaseException("group by are not supported at the moment", null, sql, null)

        // Order By
        if (orders != null && orders.size() > 0) {
            val len: Int = orders.size()
            for (i in len - 1 downTo 0) {
                val order: ZOrderBy = orders.get(i) as ZOrderBy
                val name: ZConstant = order.getExpression() as ZConstant
                rtn.sort(name.getValue().toLowerCase(), if (order.getAscOrder()) Query.ORDER_ASC else Query.ORDER_DESC)
            }
            if (maxrows > -1) {
                rtn.cutRowsTo(maxrows)
            }
        }
        // Distinct
        if (query.isDistinct()) {
            val keys: Array<String> = rtn.getColumns()
            val columns: Array<QueryColumn?> = arrayOfNulls<QueryColumn>(keys.size)
            for (i in columns.indices) {
                columns[i] = rtn.getColumn(keys[i])
            }
            var i: Int
            outer@ for (row in rtn.getRecordcount() downTo 2) {
                i = 0
                while (i < columns.size) {
                    if (!OpUtil.equals(pc, QueryUtil.getValue(columns[i], row), QueryUtil.getValue(columns[i], row - 1), true)) continue@outer
                    i++
                }
                rtn.removeRow(row)
            }
        }
        // UNION // TODO support it
        val set: ZExpression = query.getSet()
        if (set != null) {
            val op: ZExp = set.getOperand(0)
            if (op is ZQuery) throw DatabaseException("union is not supported at the moment", null, sql, null)
            // getInvokedTables((ZQuery)op, tablesNames);
        }
        return rtn
    }

    /**
     * return value
     *
     * @param sql
     * @param querySource
     * @param row
     * @param key
     * @param value
     * @return value
     * @throws PageException
     */
    @Throws(PageException::class)
    private operator fun getValue(pc: PageContext, sql: SQL, querySource: Query, row: Int, key: Collection.Key, value: Object?): Object? {
        return if (value is ZSelectItem) executeExp(pc, sql, querySource, (value as ZSelectItem?).getExpression(), row) else querySource.getAt(key, row)
    }

    /**
     * @param pc Page Context of the Request
     * @param query ZQLQuery
     * @return Query
     * @throws PageException
     */
    @Throws(PageException::class)
    private fun getSingleTable(pc: PageContext, query: ZQuery): Query {
        return Caster.toQuery(pc.getVariable((query.getFrom().get(0) as ZFromItem).getFullName()))
    }

    /**
     * Executes a ZEXp
     *
     * @param sql
     * @param qr Query Result
     * @param exp expression to execute
     * @param row current row of resultset
     * @return result
     * @throws PageException
     */
    @Throws(PageException::class)
    private fun executeExp(pc: PageContext, sql: SQL, qr: Query, exp: ZExp, row: Int): Object? {
        if (exp is ZConstant) return executeConstant(sql, qr, exp as ZConstant, row) else if (exp is ZExpression) return executeExpression(pc, sql, qr, exp as ZExpression, row)
        throw DatabaseException("unsupported sql statement [$exp]", null, sql, null)
    }

    /**
     * Executes an Expression
     *
     * @param sql
     * @param qr
     * @param expression
     * @param row
     * @return result
     * @throws PageException
     */
    @Throws(PageException::class)
    private fun executeExpression(pc: PageContext, sql: SQL, qr: Query, expression: ZExpression, row: Int): Object {
        val op: String = StringUtil.toLowerCase(expression.getOperator())
        val count: Int = expression.nbOperands()
        if (op.equals("and")) return executeAnd(pc, sql, qr, expression, row) else if (op.equals("or")) return executeOr(pc, sql, qr, expression, row)
        if (count == 0 && op.equals("?")) {
            val pos: Int = sql.getPosition()
            if (sql.getItems().length <= pos) throw DatabaseException("invalid syntax for SQL Statement", null, sql, null)
            sql.setPosition(pos + 1)
            return sql.getItems().get(pos).getValueForCF()
        } else if (count == 1) {
            val value: Object? = executeExp(pc, sql, qr, expression.getOperand(0), row)
            when (op.charAt(0)) {
                'a' -> {
                    if (op.equals("abs")) return Double.valueOf(MathUtil.abs(Caster.toDoubleValue(value)))
                    if (op.equals("acos")) return Double.valueOf(Math.acos(Caster.toDoubleValue(value)))
                    if (op.equals("asin")) return Double.valueOf(Math.asin(Caster.toDoubleValue(value)))
                    if (op.equals("atan")) return Double.valueOf(Math.atan(Caster.toDoubleValue(value)))
                }
                'c' -> {
                    if (op.equals("ceiling")) return Double.valueOf(Math.ceil(Caster.toDoubleValue(value)))
                    if (op.equals("cos")) return Double.valueOf(Math.cos(Caster.toDoubleValue(value)))
                }
                'e' -> if (op.equals("exp")) return Double.valueOf(Math.exp(Caster.toDoubleValue(value)))
                'f' -> if (op.equals("floor")) return Double.valueOf(Math.floor(Caster.toDoubleValue(value)))
                'i' -> {
                    if (op.equals("is not null")) return Boolean.valueOf(value != null)
                    if (op.equals("is null")) return Boolean.valueOf(value == null)
                }
                'u' -> if (op.equals("upper") || op.equals("ucase")) return Caster.toString(value).toUpperCase()
                'l' -> {
                    if (op.equals("lower") || op.equals("lcase")) return Caster.toString(value).toLowerCase()
                    if (op.equals("ltrim")) return StringUtil.ltrim(Caster.toString(value), null)
                    if (op.equals("length")) return Double.valueOf(Caster.toString(value).length())
                }
                'r' -> if (op.equals("rtrim")) return StringUtil.rtrim(Caster.toString(value), null)
                's' -> {
                    if (op.equals("sign")) return Double.valueOf(MathUtil.sgn(Caster.toDoubleValue(value)))
                    if (op.equals("sin")) return Double.valueOf(Math.sin(Caster.toDoubleValue(value)))
                    if (op.equals("soundex")) return StringUtil.soundex(Caster.toString(value))
                    if (op.equals("sin")) return Double.valueOf(Math.sqrt(Caster.toDoubleValue(value)))
                }
                't' -> {
                    if (op.equals("tan")) return Double.valueOf(Math.tan(Caster.toDoubleValue(value)))
                    if (op.equals("trim")) return Caster.toString(value).trim()
                }
            }
        } else if (count == 2) {
            if (op.equals("=") || op.equals("in")) return executeEQ(pc, sql, qr, expression, row) else if (op.equals("!=") || op.equals("<>")) return executeNEQ(pc, sql, qr, expression, row) else if (op.equals("<")) return executeLT(pc, sql, qr, expression, row) else if (op.equals("<=")) return executeLTE(pc, sql, qr, expression, row) else if (op.equals(">")) return executeGT(pc, sql, qr, expression, row) else if (op.equals(">=")) return executeGTE(pc, sql, qr, expression, row) else if (op.equals("-")) return executeMinus(pc, sql, qr, expression, row) else if (op.equals("+")) return executePlus(pc, sql, qr, expression, row) else if (op.equals("/")) return executeDivide(pc, sql, qr, expression, row) else if (op.equals("*")) return executeMultiply(pc, sql, qr, expression, row) else if (op.equals("^")) return executeExponent(pc, sql, qr, expression, row)
            val left: Object? = executeExp(pc, sql, qr, expression.getOperand(0), row)
            val right: Object? = executeExp(pc, sql, qr, expression.getOperand(1), row)
            when (op.charAt(0)) {
                'a' -> if (op.equals("atan2")) return Double.valueOf(Math.atan2(Caster.toDoubleValue(left), Caster.toDoubleValue(right)))
                'b' -> {
                    if (op.equals("bitand")) return OpUtil.bitand(pc, Caster.toDoubleValue(left), Caster.toDoubleValue(right))
                    if (op.equals("bitor")) return OpUtil.bitor(pc, Caster.toDoubleValue(left), Caster.toDoubleValue(right))
                }
                'c' -> if (op.equals("concat")) return Caster.toString(left).concat(Caster.toString(right))
                'l' -> if (op.equals("like")) return executeLike(pc, sql, qr, expression, row)
                'm' -> if (op.equals("mod")) return OpUtil.modulusRef(pc, Caster.toDoubleValue(left), Caster.toDoubleValue(right))
            }
            throw DatabaseException("unsopprted sql statement [$op]", null, sql, null)
        } else if (count == 3) {
            if (op.equals("between")) return executeBetween(pc, sql, qr, expression, row)
        }
        if (op.equals("in")) return executeIn(pc, sql, qr, expression, row)
        throw DatabaseException("unsopprted sql statement (op-count:" + expression.nbOperands().toString() + ";operator:" + op + ") ", null, sql, null)
    }

    /*
	 * *
	 * 
	 * @param expression / private void print(ZExpression expression) {
	 * print.ln("Operator:"+expression.getOperator().toLowerCase()); int len=expression.nbOperands();
	 * for(int i=0;i<len;i++) { print.ln("	["+i+"]=	"+expression.getOperand(i)); } }/ *
	 * 
	 * / **
	 * 
	 * execute an and operation
	 * 
	 * @param qr QueryResult to execute on it
	 * 
	 * @param expression
	 * 
	 * @param row row of resultset to execute
	 * 
	 * @return
	 * 
	 * @throws PageException
	 */
    @Throws(PageException::class)
    private fun executeAnd(pc: PageContext, sql: SQL, qr: Query, expression: ZExpression, row: Int): Object {
        val len: Int = expression.nbOperands()

        // boolean rtn=Caster.toBooleanValue(executeExp(pc,sql,qr,expression.getOperand(0),row));
        for (i in 0 until len) {
            // if(!rtn)break;
            // rtn=rtn && Caster.toBooleanValue(executeExp(pc,sql,qr,expression.getOperand(i),row));
            if (!Caster.toBooleanValue(executeExp(pc, sql, qr, expression.getOperand(i), row))) return Boolean.FALSE
        }
        return Boolean.TRUE
    }

    /**
     *
     * execute an and operation
     *
     * @param sql
     * @param qr QueryResult to execute on it
     * @param expression
     * @param row row of resultset to execute
     * @return result
     * @throws PageException
     */
    @Throws(PageException::class)
    private fun executeOr(pc: PageContext, sql: SQL, qr: Query, expression: ZExpression, row: Int): Object {
        val len: Int = expression.nbOperands()

        // boolean rtn=Caster.toBooleanValue(executeExp(pc,sql,qr,expression.getOperand(0),row));
        for (i in 0 until len) {
            if (Caster.toBooleanValue(executeExp(pc, sql, qr, expression.getOperand(i), row))) return Boolean.TRUE
            // if(rtn)break;
            // rtn=rtn || Caster.toBooleanValue(executeExp(pc,sql,qr,expression.getOperand(i),row));
        }
        return Boolean.FALSE
    }

    /**
     *
     * execute an equal operation
     *
     * @param sql
     * @param qr QueryResult to execute on it
     * @param expression
     * @param row row of resultset to execute
     * @return result
     * @throws PageException
     */
    @Throws(PageException::class)
    private fun executeEQ(pc: PageContext, sql: SQL, qr: Query, expression: ZExpression, row: Int): Object {
        return if (executeCompare(pc, sql, qr, expression, row) == 0) Boolean.TRUE else Boolean.FALSE
    }

    /**
     *
     * execute a not equal operation
     *
     * @param sql
     * @param qr QueryResult to execute on it
     * @param expression
     * @param row row of resultset to execute
     * @return result
     * @throws PageException
     */
    @Throws(PageException::class)
    private fun executeNEQ(pc: PageContext, sql: SQL, qr: Query, expression: ZExpression, row: Int): Object {
        return if (executeCompare(pc, sql, qr, expression, row) != 0) Boolean.TRUE else Boolean.FALSE
    }

    /**
     *
     * execute a less than operation
     *
     * @param sql
     * @param qr QueryResult to execute on it
     * @param expression
     * @param row row of resultset to execute
     * @return result
     * @throws PageException
     */
    @Throws(PageException::class)
    private fun executeLT(pc: PageContext, sql: SQL, qr: Query, expression: ZExpression, row: Int): Object {
        return if (executeCompare(pc, sql, qr, expression, row) < 0) Boolean.TRUE else Boolean.FALSE
    }

    /**
     *
     * execute a less than or equal operation
     *
     * @param sql
     * @param qr QueryResult to execute on it
     * @param expression
     * @param row row of resultset to execute
     * @return result
     * @throws PageException
     */
    @Throws(PageException::class)
    private fun executeLTE(pc: PageContext, sql: SQL, qr: Query, expression: ZExpression, row: Int): Object {
        return if (executeCompare(pc, sql, qr, expression, row) <= 0) Boolean.TRUE else Boolean.FALSE
    }

    /**
     *
     * execute a greater than operation
     *
     * @param sql
     * @param qr QueryResult to execute on it
     * @param expression
     * @param row row of resultset to execute
     * @return result
     * @throws PageException
     */
    @Throws(PageException::class)
    private fun executeGT(pc: PageContext, sql: SQL, qr: Query, expression: ZExpression, row: Int): Object {
        return if (executeCompare(pc, sql, qr, expression, row) > 0) Boolean.TRUE else Boolean.FALSE
    }

    /**
     *
     * execute a greater than or equal operation
     *
     * @param sql
     * @param qr QueryResult to execute on it
     * @param expression
     * @param row row of resultset to execute
     * @return result
     * @throws PageException
     */
    @Throws(PageException::class)
    private fun executeGTE(pc: PageContext, sql: SQL, qr: Query, expression: ZExpression, row: Int): Object {
        return if (executeCompare(pc, sql, qr, expression, row) >= 0) Boolean.TRUE else Boolean.FALSE
    }

    /**
     *
     * execute an equal operation
     *
     * @param sql
     * @param qr QueryResult to execute on it
     * @param expression
     * @param row row of resultset to execute
     * @return result
     * @throws PageException
     */
    @Throws(PageException::class)
    private fun executeCompare(pc: PageContext, sql: SQL, qr: Query, expression: ZExpression, row: Int): Int {
        return OpUtil.compare(pc, executeExp(pc, sql, qr, expression.getOperand(0), row), executeExp(pc, sql, qr, expression.getOperand(1), row))
    }

    @Throws(PageException::class)
    private fun executeLike(pc: PageContext, sql: SQL, qr: Query, expression: ZExpression, row: Int): Object {
        return Caster.toBoolean(
                like(sql, Caster.toString(executeExp(pc, sql, qr, expression.getOperand(0), row)), Caster.toString(executeExp(pc, sql, qr, expression.getOperand(1), row))))
    }

    @Throws(PageException::class)
    private fun like(sql: SQL, haystack: String, needle: String): Boolean {
        return LikeCompare.like(sql, haystack, needle)
    }

    /**
     *
     * execute a greater than or equal operation
     *
     * @param sql
     * @param qr QueryResult to execute on it
     * @param expression
     * @param row row of resultset to execute
     * @return result
     * @throws PageException
     */
    @Throws(PageException::class)
    private fun executeIn(pc: PageContext, sql: SQL, qr: Query, expression: ZExpression, row: Int): Object {
        val len: Int = expression.nbOperands()
        val left: Object? = executeExp(pc, sql, qr, expression.getOperand(0), row)
        for (i in 1 until len) {
            if (OpUtil.compare(pc, left, executeExp(pc, sql, qr, expression.getOperand(i), row)) === 0) return Boolean.TRUE
        }
        return Boolean.FALSE
    }

    /**
     *
     * execute a minus operation
     *
     * @param sql
     * @param qr QueryResult to execute on it
     * @param expression
     * @param row row of resultset to execute
     * @return result
     * @throws PageException
     */
    @Throws(PageException::class)
    private fun executeMinus(pc: PageContext, sql: SQL, qr: Query, expression: ZExpression, row: Int): Object {
        return Caster.toDouble(executeExp(pc, sql, qr, expression.getOperand(0), row)) - Caster.toDoubleValue(executeExp(pc, sql, qr, expression.getOperand(1), row))
    }

    /**
     *
     * execute a divide operation
     *
     * @param sql
     * @param qr QueryResult to execute on it
     * @param expression
     * @param row row of resultset to execute
     * @return result
     * @throws PageException
     */
    @Throws(PageException::class)
    private fun executeDivide(pc: PageContext, sql: SQL, qr: Query, expression: ZExpression, row: Int): Object {
        return Caster.toDouble(executeExp(pc, sql, qr, expression.getOperand(0), row)) / Caster.toDoubleValue(executeExp(pc, sql, qr, expression.getOperand(1), row))
    }

    /**
     *
     * execute a multiply operation
     *
     * @param sql
     * @param qr QueryResult to execute on it
     * @param expression
     * @param row row of resultset to execute
     * @return result
     * @throws PageException
     */
    @Throws(PageException::class)
    private fun executeMultiply(pc: PageContext, sql: SQL, qr: Query, expression: ZExpression, row: Int): Object {
        return Caster.toDouble(executeExp(pc, sql, qr, expression.getOperand(0), row)) * Caster.toDoubleValue(executeExp(pc, sql, qr, expression.getOperand(1), row))
    }

    /**
     *
     * execute a multiply operation
     *
     * @param sql
     * @param qr QueryResult to execute on it
     * @param expression
     * @param row row of resultset to execute
     * @return result
     * @throws PageException
     */
    @Throws(PageException::class)
    private fun executeExponent(pc: PageContext, sql: SQL, qr: Query, expression: ZExpression, row: Int): Object {
        return Integer
                .valueOf(Caster.toIntValue(executeExp(pc, sql, qr, expression.getOperand(0), row)) xor Caster.toIntValue(executeExp(pc, sql, qr, expression.getOperand(1), row)))
    }

    /**
     *
     * execute a plus operation
     *
     * @param sql
     * @param qr QueryResult to execute on it
     * @param expression
     * @param row row of resultset to execute
     * @return result
     * @throws PageException
     */
    @Throws(PageException::class)
    private fun executePlus(pc: PageContext, sql: SQL, qr: Query, expression: ZExpression, row: Int): Object {
        val left: Object? = executeExp(pc, sql, qr, expression.getOperand(0), row)
        val right: Object? = executeExp(pc, sql, qr, expression.getOperand(1), row)
        return try {
            OpUtil.plusRef(pc, Caster.toNumber(left), Caster.toNumber(right))
        } catch (e: PageException) {
            Caster.toString(left) + Caster.toString(right)
        }
    }

    /**
     *
     * execute a between operation
     *
     * @param sql
     * @param qr QueryResult to execute on it
     * @param expression
     * @param row row of resultset to execute
     * @return result
     * @throws PageException
     */
    @Throws(PageException::class)
    private fun executeBetween(pc: PageContext, sql: SQL, qr: Query, expression: ZExpression, row: Int): Object {
        val left: Object? = executeExp(pc, sql, qr, expression.getOperand(0), row)
        val right1: Object? = executeExp(pc, sql, qr, expression.getOperand(1), row)
        val right2: Object? = executeExp(pc, sql, qr, expression.getOperand(2), row)
        return if (OpUtil.compare(pc, left, right1) <= 0 && OpUtil.compare(pc, left, right2) >= 0) Boolean.TRUE else Boolean.FALSE
    }

    /**
     * Executes a constant value
     *
     * @param sql
     * @param qr
     * @param constant
     * @param row
     * @return result
     * @throws PageException
     */
    @Throws(PageException::class)
    private fun executeConstant(sql: SQL, qr: Query, constant: ZConstant, row: Int): Object? {
        return when (constant.getType()) {
            ZConstant.COLUMNNAME -> {
                if (constant.getValue().equals(SQLPrettyfier.PLACEHOLDER_QUESTION)) {
                    val pos: Int = sql.getPosition()
                    sql.setPosition(pos + 1)
                    if (sql.getItems().length <= pos) throw DatabaseException("invalid syntax for SQL Statement", null, sql, null)
                    return sql.getItems().get(pos).getValueForCF()
                }
                qr.getAt(ListUtil.last(constant.getValue(), ".", true), row)
            }
            ZConstant.NULL -> null
            ZConstant.NUMBER -> Caster.toDouble(constant.getValue())
            ZConstant.STRING -> constant.getValue()
            ZConstant.UNKNOWN -> throw DatabaseException("invalid constant value", null, sql, null)
            else -> throw DatabaseException("invalid constant value", null, sql, null)
        }
    }
}