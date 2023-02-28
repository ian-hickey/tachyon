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

import java.sql.Types

/**
 *
 */
class QoQ {
    companion object {
        private val paramKey: Collection.Key = KeyImpl("?")
        private var qoqParallelism = 0

        /**
         * Order the rows in a query
         *
         * @param target Query to order
         * @param columns Column expressions to order on
         * @param isUnion Is this a union
         * @param sql
         * @throws PageException
         */
        @Throws(PageException::class)
        private fun order(pc: PageContext, target: QueryImpl, columns: Array<Expression>, isUnion: Boolean, sql: SQL) {
            var col: Expression
            // Build up a int[] that represents where each row needs to be in the final query
            val sortedIndexes: IntArray = getStream(target)
                    .boxed()
                    .sorted(QueryComparator(pc, target, columns, isUnion, sql))
                    .mapToInt { i -> (i as Integer).intValue() }
                    .toArray()

            // Move the data around to match
            target.sort(sortedIndexes)
        }

        fun getStream(qry: QueryImpl): IntStream {
            return if (qry.getRecordcount() > 0) {
                val qStream: IntStream = IntStream.range(1, qry.getRecordcount() + 1)
                if (qry.getRecordcount() >= qoqParallelism) {
                    qStream.parallel()
                } else qStream
            } else {
                IntStream.empty()
            }
        }

        fun getStream(queryPartitions: QueryPartitions): Stream<Map.Entry<String, QueryImpl>> {
            return if (queryPartitions.getPartitions().size() > 0) {
                var qStream: Stream<Map.Entry<String, QueryImpl>> = queryPartitions.getPartitions().entrySet().stream()
                if (queryPartitions.getPartitions().size() >= qoqParallelism) {
                    qStream = qStream.parallel()
                }
                qStream
            } else {
                Stream.empty()
            }
        }

        fun throwingIntConsumer(throwingIntConsumer: ThrowingIntConsumer): IntConsumer {
            return object : IntConsumer() {
                @Override
                fun accept(t: Int) {
                    try {
                        throwingIntConsumer.accept(t)
                    } catch (ex: Exception) {
                        throw RuntimeException(ex)
                    }
                }
            }
        }

        fun throwingConsumer(throwingConsumer: ThrowingConsumer): Consumer<Map.Entry<String, QueryImpl>> {
            return object : Consumer<Map.Entry<String?, QueryImpl?>?>() {
                @Override
                fun accept(t: Map.Entry<String?, QueryImpl?>?) {
                    try {
                        throwingConsumer.accept(t)
                    } catch (ex: Exception) {
                        throw RuntimeException(ex)
                    }
                }
            }
        }

        fun throwingFilter(throwingFilter: ThrowingFilter): IntPredicate {
            return object : IntPredicate() {
                @Override
                fun test(t: Int): Boolean {
                    return try {
                        throwingFilter.test(t)
                    } catch (ex: Exception) {
                        throw RuntimeException(ex)
                    }
                }
            }
        }

        init {
            qoqParallelism = Caster.toIntValue(SystemUtil.getSystemPropOrEnvVar("tachyon.qoq.parallelism", "50"), 50)
        }
    }

    /**
     * Execute a QofQ against a SQL object
     *
     * @param pc
     * @param sql
     * @param maxrows
     * @return
     * @throws PageException
     */
    @Throws(PageException::class)
    fun execute(pc: PageContext, sql: SQL, maxrows: Int): QueryImpl {
        return try {
            val parser = SelectParser()
            val selects: Selects = parser.parse(sql.getSQLString())
            execute(pc, sql, selects, maxrows)
        } catch (t: Throwable) {
            throw Caster.toPageException(t)
        }
    }

    /**
     * execute a SQL Statement against CFML Scopes
     */
    @Throws(PageException::class)
    fun execute(pc: PageContext, sql: SQL, selects: Selects?, maxrows: Int): QueryImpl {
        var maxrows = maxrows
        val arrSelects: Array<Select> = selects.getSelects()
        val isUnion = arrSelects.size > 1
        var target = QueryImpl(arrayOfNulls<Collection.Key>(0), 0, "query", sql)

        // For each select (more than one when using union)
        for (i in arrSelects.indices) {
            arrSelects[i].getFroms()
            val froms: Array<Column> = arrSelects[i].getFroms()
            if (froms.size > 1) throw DatabaseException("QoQ can only select from a single tables at a time.", null, sql, null)

            // Lookup actual Query variable on page
            val source: QueryImpl = getSingleTable(pc, froms[0])
            arrSelects[i].expandAsterisks(source)

            // Unions don't allow operations in the order by
            if (!isUnion) {
                selects.calcOrderByExpressions()
            }
            // Run a select statement. If we have a union, we run this once per select being unioned
            target = executeSingle(pc, arrSelects[i], getSingleTable(pc, froms[0]), target, if (isUnion) -1 else maxrows, sql, selects.getOrderbys().length > 0, isUnion)
        }

        // DON'T GET THIS SOONER! We recalculate the order bys above based on the columns in the
        // first select
        val orders: Array<Expression> = selects.getOrderbys()

        // Order By
        if (orders.size > 0) {
            order(pc, target, orders, isUnion, sql)
            // Clean up extra columns that we added in just for the sorting
            for (col in target.getColumnNames()) {
                if (col.getLowerString().startsWith("__order_by_expression__")) {
                    target.removeColumn(col)
                }
            }
        }

        // If we only had a single select, but couldn't apply the top earlier, apply it now
        if (!isUnion) {
            val oTop: ValueNumber = arrSelects[0].getTop()
            var top = -1
            if (oTop != null) {
                top = oTop.getValueAsDouble()
                if (maxrows == -1 || maxrows > top) maxrows = top
            }
        }
        // Choppy chop
        if (maxrows > -1) {
            (target as QueryImpl).cutRowsTo(maxrows)
        }

        // New query is populated and ready to go!
        return target
    }

    /**
     * Process a single select statement. If this is a union, append it to the incoming "previous" Query
     * and return the new, combined query with all rows
     *
     * @param pc PageContext
     * @param select Select instance
     * @param source Source query to pull data from
     * @param previous Previous query in case of union. May be empty if this is the first select in the
     * union
     * @param maxrows max rows from cfquery tag. Not necessarily the same as TOP
     * @param sql SQL object
     * @param hasOrders Is this overall Selects instance ordered? This affects whether we can optimize
     * maxrows or not
     * @param isUnion Is this select part of a union of several selects
     * @return
     * @throws PageException
     */
    @Throws(PageException::class)
    private fun executeSingle(pc: PageContext, select: Select, source: QueryImpl, previous: QueryImpl, maxrows: Int, sql: SQL, hasOrders: Boolean, isUnion: Boolean): QueryImpl {

        // Our records will be placed here to return
        var maxrows = maxrows
        val target = QueryImpl(arrayOfNulls<Collection.Key>(0), 0, "query", sql)

        // Make max rows the smaller of the two
        val oTop: ValueNumber = select.getTop()
        var top = -1
        if (oTop != null) {
            top = oTop.getValueAsDouble()
            if (maxrows == -1 || maxrows > top) maxrows = top
        }
        val expSelects: Array<Expression> = select.getSelects()
        val selCount = expSelects.size
        val expSelectsMap: Map<Collection.Key, Object> = HashMap<Collection.Key, Object>()
        // Build up the final columns we need in our target query
        for (i in 0 until selCount) {
            val expSelect: Expression = expSelects[i]
            val alias: Key = Caster.toKey(expSelect.getAlias())
            expSelectsMap.put(alias, expSelect)
            var type: Int = Types.OTHER
            if (expSelect is ColumnExpression) {
                val ce: ColumnExpression = expSelect as ColumnExpression
                // A query param being selected back out uses the type other. We should probably use
                // the query param type, but we don't actually know what param we'll bind to at this
                // point.
                if (!ce.isParam()) type = source.getColumn(Caster.toKey(ce.getColumnName())).getType()
            }
            queryAddColumn(target, alias, type)
        }
        val headers: Array<Collection.Key> = expSelectsMap.keySet().toArray(arrayOfNulls<Collection.Key>(expSelectsMap.size()))

        // get target columns
        val trgColumns: Array<QueryColumnImpl?> = arrayOfNulls<QueryColumnImpl>(headers.size)
        val trgValues: Array<Object?> = arrayOfNulls<Object>(headers.size)
        for (cell in headers.indices) {
            trgColumns[cell] = target.getColumn(headers[cell]) as QueryColumnImpl
            trgValues[cell] = expSelectsMap[headers[cell]]
        }

        // If have a group by, a distinct, or this is part of a "union", or has aggregates in the
        // select list then we partition
        if (select.getGroupbys().length > 0 || select.isDistinct() || select.hasAggregateSelect() && select.getWhere() != null) {
            executeSinglePartitioned(pc, select, source, target, maxrows, sql, hasOrders, isUnion, trgColumns, trgValues, headers)
        } else {
            executeSingleNonPartitioned(pc, select, source, target, maxrows, sql, hasOrders, isUnion, trgColumns, trgValues, headers)
        }

        // Top is applied to a union regardless of order. This is because you can't order the
        // individual selects of a union. You can only order the final result. So any top on an
        // individual select is just blindly applied to whatever order the records may be in
        if (isUnion && top > -1) {
            (target as QueryImpl).cutRowsTo(top)
        }

        // For a union all, we just slam all the rows together, keeping any duplicate record
        if (isUnion && !select.isUnionDistinct()) {
            return doUnionAll(previous, target, sql)
        } else if (isUnion && select.isUnionDistinct()) {
            return doUnionDistinct(pc, previous, target, sql)
        }
        return target
    }

    /**
     * Combine two queries while retaining all rows.
     *
     * @param previous Query from previous select to union
     * @param target New query to add into the previous
     * @return Combined Query with potential duplicate rows
     * @throws PageException
     */
    @Throws(PageException::class)
    private fun doUnionAll(previous: QueryImpl, target: QueryImpl, sql: SQL): QueryImpl {
        // If this is the first select in a series of unions, just return it directly. It's column
        // names now get set in stone as the column names the next union(s) will use!
        if (previous.getRecordcount() === 0) {
            return target
        }
        val previousColKeys: Array<Collection.Key> = previous.getColumnNames()
        val targetColKeys: Array<Collection.Key> = target.getColumnNames()
        if (previousColKeys.size != targetColKeys.size) {
            throw IllegalQoQException("Cannot perform union as number of columns in selects do not match.", null, sql, null)
        }

        // Queries being joined need to have the same number of columns and the data is fully
        // realized, so just copy it over positionally. The column names may not match, but that's
        // fine.
        getStream(target)
                .forEach(throwingIntConsumer(ThrowingIntConsumer { row: Int ->
                    val newRow: Int = previous.addRow()
                    for (col in targetColKeys.indices) {
                        previous.setAt(previousColKeys[col], newRow, target.getColumn(targetColKeys[col]).get(row, null), true)
                    }
                }))
        return previous
    }

    /**
     * Combine two queries while removing duplicate rows
     *
     * @param pc PageContext
     * @param previous Query from previous select to union
     * @param target New query to add into the previous
     * @param sql SQL instance
     * @return Combined Query with no duplicate rows
     * @throws PageException
     */
    @Throws(PageException::class)
    private fun doUnionDistinct(pc: PageContext, previous: QueryImpl, target: QueryImpl, sql: SQL): QueryImpl {
        val previousColKeys: Array<Collection.Key> = previous.getColumnNames()
        val targetColKeys: Array<Collection.Key> = target.getColumnNames()
        if (previousColKeys.size != targetColKeys.size) {
            throw IllegalQoQException("Cannot perform union as number of columns in selects do not match.", null, sql, null)
        }
        val selectExpressions: Array<Expression?> = arrayOfNulls<Expression>(previousColKeys.size)
        // We want the exact columns from the previous query, but not necessarily all the data. Make
        // a new target and copy the columns
        val newTarget = QueryImpl(arrayOfNulls<Collection.Key>(0), 0, "query", sql)
        for (col in previousColKeys.indices) {
            newTarget.addColumn(previousColKeys[col], ArrayImpl(), previous.getColumn(previousColKeys[col]).getType())
            // While we're looping, build up a handy array of expressions from the previous query.
            selectExpressions[col] = ColumnExpression(previousColKeys[col].getString(), 0)
        }

        // Initialize our object to track the partitions
        val queryPartitions = QueryPartitions(sql, selectExpressions, arrayOfNulls<Expression>(0), newTarget, HashSet<String>(), this)

        // Add in all the rows from our previous work
        getStream(previous)
                .forEach(throwingIntConsumer(ThrowingIntConsumer { row: Int -> queryPartitions.addRow(pc, previous, row, true) }))

        // ...and all of the new rows
        getStream(target)
                .forEach(throwingIntConsumer(ThrowingIntConsumer { row: Int -> queryPartitions.addRow(pc, target, row, true) }))

        // Loop over the partitions and take one from each and add to our new target question for a
        // distinct result
        getStream(queryPartitions)
                .forEach(throwingConsumer(ThrowingConsumer { sourcePartition: Map.Entry<String?, QueryImpl?> ->
                    val newRow: Int = newTarget.addRow()
                    for (col in targetColKeys.indices) {
                        newTarget.setAt(previousColKeys[col], newRow, sourcePartition.getValue().getColumn(previousColKeys[col]).get(1, null), true)
                    }
                }))
        return newTarget
    }

    /**
     * Process a single select that is not partitioned (grouped or distinct)
     *
     * @param pc PageContext
     * @param select Select instance
     * @param source Query we're select from
     * @param target Query object we're adding rows into. (passed back by reference)
     * @param maxrows Max rows from cfquery.
     * @param sql
     * @param hasOrders Is this overall query ordered?
     * @param isUnion Is this part of a union?
     * @param trgColumns Lookup array of column
     * @param trgValues Lookup array of expressions
     * @param headers Select lists
     * @throws PageException
     */
    @Throws(PageException::class)
    private fun executeSingleNonPartitioned(pc: PageContext, select: Select, source: QueryImpl, target: QueryImpl, maxrows: Int, sql: SQL, hasOrders: Boolean, isUnion: Boolean,
                                            trgColumns: Array<QueryColumnImpl?>, trgValues: Array<Object?>, headers: Array<Collection.Key>) {
        val where: Operation = select.getWhere()

        // If we are ordering or distincting the result, we can't enforce the maxrows until after
        // we've built the entire query
        val hasMaxrow = maxrows > -1 && !hasOrders
        // Is there at least on aggregate expression in the select list
        val hasAggregateSelect: Boolean = select.hasAggregateSelect()

        // For a non-grouping query with aggregates in the select such as
        // SELECT count(1) FROM qry
        // then we need to return a single row
        if (hasAggregateSelect && source.getRecordcount() === 0) {
            target.addRow()
            for (cell in headers.indices) {
                trgColumns[cell].set(1, getValue(pc, sql, source, 1, headers[cell], trgValues[cell], null), true)
            }
            return
        }
        var stream: IntStream = getStream(source)
        if (where != null) {
            stream = stream
                    .filter(throwingFilter(ThrowingFilter { row: Int -> Caster.toBooleanValue(executeExp(pc, sql, source, where, row)) }))
        }

        // If this was a non-grouped select with only aggregates like select "count(1) from
        // table" than bail after a single row
        if (hasAggregateSelect) {
            stream = stream.limit(1)
            // If we can, optimize the max rows exit strategy
            // This won't fire if there is an ORDER BY since we can't limit the rows until we sort (later)
        } else if (hasMaxrow) {
            stream = stream.limit(maxrows)
        }
        stream
                .forEach(throwingIntConsumer(ThrowingIntConsumer { row: Int ->
                    val newRow: Int = target.addRow()
                    for (cell in headers.indices) {
                        trgColumns[cell].set(newRow, getValue(pc, sql, source, row, headers[cell], trgValues[cell], null), true)
                    }
                }))
    }

    /**
     * Process a single select that is partitioned (grouped)
     *
     * @param pc PageContext
     * @param select Select instance
     * @param source Query we're selecting from
     * @param target Query object we're adding rows into. (passed back by reference)
     * @param maxrows
     * @param sql
     * @param hasOrders Is this overall query ordered?
     * @param isUnion Is this part of a union?
     * @param trgColumns Lookup array of column
     * @param trgValues Lookup array of expressions
     * @param headers select columns
     * @throws PageException
     */
    @Throws(PageException::class)
    private fun executeSinglePartitioned(pc: PageContext, select: Select, source: QueryImpl, target: QueryImpl, maxrows: Int, sql: SQL, hasOrders: Boolean, isUnion: Boolean,
                                         trgColumns: Array<QueryColumnImpl?>, trgValues: Array<Object?>, headers: Array<Collection.Key>) {

        // Is there at least on aggregate expression in the select list
        val hasAggregateSelect: Boolean = select.hasAggregateSelect()

        // For a non-grouping query with aggregates in the select such as
        // SELECT count(1) FROM qry WHERE col='foo'
        // then we need to return a single row
        if (hasAggregateSelect && select.getGroupbys().length === 0 && source.getRecordcount() === 0) {
            val newRow: Int = target.addRow()
            for (cell in headers.indices) {
                trgColumns[cell].set(1, getValue(pc, sql, source, 1, headers[cell], trgValues[cell], null), true)
            }
            return
        }
        val where: Operation = select.getWhere()
        // Initialize object to track our partitioned data
        val queryPartitions = QueryPartitions(sql, select.getSelects(), select.getGroupbys(), target, select.getAdditionalColumns(), this)
        var stream: IntStream = getStream(source)
        if (where != null) {
            stream = stream
                    .filter(throwingFilter(ThrowingFilter { row: Int -> Caster.toBooleanValue(executeExp(pc, sql, source, where, row)) }))
        }
        stream
                .forEach(throwingIntConsumer(ThrowingIntConsumer { row: Int ->
                    // ... add this row to our partitioned data
                    queryPartitions.addRow(pc, source, row, false)
                }))

        // For a non-grouping query with aggregates where no records matched the where clause
        // SELECT count(1) FROM qry WHERE 1=0
        // then we need to add a single empty partition so our final select will have a single row.
        if (hasAggregateSelect && select.getGroupbys().length === 0 && queryPartitions.getPartitions().size() === 0) {
            queryPartitions.addEmptyPartition(source, target)
        }

        // Now that all rows are partitioned, eliminate partitions we don't need via the having
        // clause
        if (select.getHaving() != null) {

            // Loop over the partitions and take one from each and add to our new target question for a
            // distinct result
            getStream(queryPartitions)
                    .forEach(throwingConsumer(ThrowingConsumer { sourcePartition: Map.Entry<String?, QueryImpl?> ->
                        // Eval the having clause on it
                        if (!Caster.toBooleanValue(executeExp(pc, sql, sourcePartition.getValue(), select.getHaving(), 1))) {
                            // Voted off the island :/
                            queryPartitions.getPartitions().remove(sourcePartition.getKey())
                        }
                    }))
        }

        // Turn off query caching for our column references
        // Sharing columnExpressions across different query objects will have issues
        for (cell in headers.indices) {
            if (trgValues[cell] is Expression) {
                (trgValues[cell] as Expression?).setCacheColumn(false)
            }
        }
        getStream(queryPartitions)
                .forEach(throwingConsumer(ThrowingConsumer { sourcePartition: Map.Entry<String?, QueryImpl?> ->
                    val newRow: Int = target.addRow()
                    for (cell in headers.indices) {

                        // If this is a column
                        if (trgValues[cell] is ColumnExpression) {
                            val ce: ColumnExpression? = trgValues[cell] as ColumnExpression?
                            if (ce.getColumn().equals(paramKey)) {
                                target.setAt(headers[cell], newRow, getValue(pc, sql, sourcePartition.getValue(), 1, null, trgValues[cell], null), true)
                            } else {
                                // Then make sure to use the alias now to reference it since it changed
                                // names after going into the partition
                                target.setAt(headers[cell], newRow, getValue(pc, sql, sourcePartition.getValue(), 1, ce.getColumnAlias(), null, null), true)
                            }
                        } else {
                            target.setAt(headers[cell], newRow, getValue(pc, sql, sourcePartition.getValue(), 1, null, trgValues[cell], null), true)
                        }
                    }
                }))
    }

    /**
     * Helper for adding a column to a query
     *
     * @param query
     * @param column
     * @param type
     * @throws PageException
     */
    @Throws(PageException::class)
    private fun queryAddColumn(query: QueryImpl, column: Collection.Key, type: Int) {
        if (!query.containsKey(column)) {
            query.addColumn(column, ArrayImpl(), type)
        }
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
    operator fun getValue(pc: PageContext, sql: SQL, querySource: QueryImpl, row: Int, key: Collection.Key?, value: Object): Object? {
        return if (value is Expression) executeExp(pc, sql, querySource, value as Expression, row) else querySource.getColumn(key).get(row, null)
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
    operator fun getValue(pc: PageContext, sql: SQL, querySource: QueryImpl, row: Int, key: Collection.Key?, value: Object?, defaultValue: Object?): Object? {
        return if (value is Expression) executeExp(pc, sql, querySource, value as Expression?, row, defaultValue) else querySource.getColumn(key).get(row, null)
    }

    /**
     * @param pc Page Context of the Request
     * @param table ZQLQuery
     * @return Query
     * @throws PageException
     */
    @Throws(PageException::class)
    private fun getSingleTable(pc: PageContext, table: Column): QueryImpl {
        return Caster.toQuery(pc.getVariable(table.getFullName())) as QueryImpl
    }

    /**
     * Executes a ZEXp
     *
     * @param sql
     * @param source Query Result
     * @param exp expression to execute
     * @param row current row of resultset
     * @return result
     * @throws PageException
     */
    @Throws(PageException::class)
    private fun executeExp(pc: PageContext, sql: SQL, source: QueryImpl, exp: Expression, row: Int): Object? {
        if (exp is Value) return (exp as Value).getValue()
        if (exp is Column) return executeColumn(pc, sql, source, exp as Column, row)
        if (exp is Operation) return executeOperation(pc, sql, source, exp as Operation, row)
        if (exp is BracketExpression) return executeBracked(pc, sql, source, exp as BracketExpression, row)
        throw DatabaseException("unsupported sql statement [$exp]", null, sql, null)
    }

    @Throws(PageException::class)
    private fun executeExp(pc: PageContext, sql: SQL, source: QueryImpl, exp: Expression?, row: Int, columnDefault: Object?): Object? {
        if (exp is Value) return (exp as Value?).getValue()
        if (exp is Column) return executeColumn(pc, sql, source, exp as Column?, row, columnDefault)
        if (exp is Operation) return executeOperation(pc, sql, source, exp as Operation?, row)
        if (exp is BracketExpression) return executeBracked(pc, sql, source, exp as BracketExpression?, row)
        throw DatabaseException("unsupported sql statement [$exp]", null, sql, null)
    }

    /**
     * Accepts an expression which is the input to an aggregate operation.
     *
     * @param pc
     * @param sql
     * @param source
     * @param exp
     * @param row
     * @param includeNull
     * @return an array with as many items as rows in the source query containing the corresponding
     * expression result for each matching row
     * @throws PageException
     */
    @Throws(PageException::class)
    private fun executeAggregateExp(pc: PageContext, sql: SQL, source: QueryImpl, exp: Expression, includeNull: Boolean, returnDistinct: Boolean): Array<Object?> {
        var result: Array<Object?> = arrayOfNulls<Object>(source.getRecordcount())

        // For a literal value, just fill an array with that value
        if (exp is Value) {
            val value: Object = (exp as Value).getValue()
            for (i in 0 until source.getRecordcount()) {
                result[i] = value
            }
            return result
        }

        // For a column, return the data in that column as an array
        if (exp is Column) {
            result = (source.getColumn((exp as Column).getColumn()) as QueryColumnImpl).toArray()
            // Simple return if we want all values
            return if (!returnDistinct && includeNull) {
                result
            } else {
                var resultStream: Stream<Object?> = Arrays.stream(result)
                if (!includeNull) resultStream = resultStream.filter { s -> s != null }
                if (returnDistinct) resultStream = resultStream.distinct()
                resultStream.toArray()
            }
        }
        // For an operation, we need to execute the operation once for each row and capture the
        // results as our final array
        if (exp is Operation) {
            for (i in 0 until source.getRecordcount()) {
                result[i] = executeOperation(pc, sql, source, exp as Operation, i + 1)
            }

            // Simple return if we want all values
            return if (!returnDistinct && includeNull) {
                result
            } else {
                var resultStream: Stream<Object?> = Arrays.stream(result)
                if (!includeNull) resultStream = resultStream.filter { s -> s != null }
                if (returnDistinct) resultStream = resultStream.distinct()
                resultStream.toArray()
            }
        }
        throw DatabaseException("unsupported sql statement [$exp]", null, sql, null)
    }

    @Throws(PageException::class)
    private fun executeOperation(pc: PageContext, sql: SQL, source: QueryImpl, operation: Operation?, row: Int): Object? {
        if (operation is Operation2) {
            val op2: Operation2? = operation as Operation2?
            when (op2.getOperator()) {
                Operation.OPERATION2_AND -> return executeAnd(pc, sql, source, op2, row)
                Operation.OPERATION2_OR -> return executeOr(pc, sql, source, op2, row)
                Operation.OPERATION2_XOR -> return executeXor(pc, sql, source, op2, row)
                Operation.OPERATION2_EQ -> return executeEQ(pc, sql, source, op2, row)
                Operation.OPERATION2_NEQ -> return executeNEQ(pc, sql, source, op2, row)
                Operation.OPERATION2_LTGT -> return executeNEQ(pc, sql, source, op2, row)
                Operation.OPERATION2_LT -> return executeLT(pc, sql, source, op2, row)
                Operation.OPERATION2_LTE -> return executeLTE(pc, sql, source, op2, row)
                Operation.OPERATION2_GT -> return executeGT(pc, sql, source, op2, row)
                Operation.OPERATION2_GTE -> return executeGTE(pc, sql, source, op2, row)
                Operation.OPERATION2_MINUS -> return executeMinus(pc, sql, source, op2, row)
                Operation.OPERATION2_PLUS -> return executePlus(pc, sql, source, op2, row)
                Operation.OPERATION2_DIVIDE -> return executeDivide(pc, sql, source, op2, row)
                Operation.OPERATION2_MULTIPLY -> return executeMultiply(pc, sql, source, op2, row)
                Operation.OPERATION2_BITWISE -> return executeBitwise(pc, sql, source, op2, row)
                Operation.OPERATION2_LIKE -> return Caster.toBoolean(executeLike(pc, sql, source, op2, row))
                Operation.OPERATION2_NOT_LIKE -> return Caster.toBoolean(!executeLike(pc, sql, source, op2, row))
                Operation.OPERATION2_MOD -> return executeMod(pc, sql, source, op2, row)
            }
        }
        if (operation is Operation1) {
            val op1: Operation1? = operation as Operation1?
            val o: Int = op1.getOperator()
            if (o == Operation.OPERATION1_IS_NULL) {
                val value: Object? = executeExp(pc, sql, source, op1.getExp(), row, null)
                return Caster.toBoolean(value == null)
            }
            if (o == Operation.OPERATION1_IS_NOT_NULL) {
                val value: Object? = executeExp(pc, sql, source, op1.getExp(), row, null)
                return Caster.toBoolean(value != null)
            }
            val value: Object? = executeExp(pc, sql, source, op1.getExp(), row)
            if (o == Operation.OPERATION1_MINUS) return Caster.toDouble(-Caster.toDoubleValue(value))
            if (o == Operation.OPERATION1_PLUS) return Caster.toDouble(value)
            if (o == Operation.OPERATION1_NOT) return Caster.toBoolean(!Caster.toBooleanValue(value))
        }
        if (operation is Operation3) {
            val op3: Operation3? = operation as Operation3?
            val o: Int = op3.getOperator()
            if (o == Operation.OPERATION3_BETWEEN) return executeBetween(pc, sql, source, op3, row)
            if (o == Operation.OPERATION3_LIKE) return executeLike(pc, sql, source, op3, row)
        }
        if (operation !is OperationN) throw DatabaseException("invalid syntax for SQL Statement", null, sql, null)
        val opn: OperationN? = operation as OperationN?
        val op: String = opn.getOperator()
        val operators: Array<Expression> = opn.getOperants()

        // 11111111111111111111111111111111111111111111111111111
        if (operators.size == 1) {
            var value: Object? = null
            var aggregateValues: Array<Object?>? = null

            // Aggregate operations use the entire array of values for the column instead
            // of a single value at a given row
            if (operation is OperationAggregate) {
                // count() has special handling below
                if (!op.equals("count")) {
                    aggregateValues = executeAggregateExp(pc, sql, source, operators[0], false, false)
                }
            } else {
                value = executeExp(pc, sql, source, operators[0], row)
            }
            when (op.charAt(0)) {
                'a' -> {
                    if (op.equals("abs")) return Double.valueOf(MathUtil.abs(Caster.toDoubleValue(value)))
                    if (op.equals("acos")) return Double.valueOf(Math.acos(Caster.toDoubleValue(value)))
                    if (op.equals("asin")) return Double.valueOf(Math.asin(Caster.toDoubleValue(value)))
                    if (op.equals("atan")) return Double.valueOf(Math.atan(Caster.toDoubleValue(value)))
                    if (op.equals("avg")) {
                        // If there are no non-null values, return empty
                        return if (aggregateValues!!.size == 0) {
                            null
                        } else ArrayUtil.avg(Caster.toArray(aggregateValues))
                    }
                }
                'c' -> {
                    if (op.equals("ceiling")) return Double.valueOf(Math.ceil(Caster.toDoubleValue(value)))
                    if (op.equals("cos")) return Double.valueOf(Math.cos(Caster.toDoubleValue(value)))
                    if (op.equals("count")) return executeCount(pc, sql, source, operators)
                    if (op.equals("cast")) {
                        // Cast is a single operand operator, but it gets the type from the alias of the single operand
                        // i.e. cast( col1 as date )
                        // If there is no alias, throw an exception.
                        if (!operators[0].hasAlias()) {
                            throw IllegalQoQException("No type provided to cast to. [" + opn.toString(true).toString() + "] ", null, sql, null)
                        }
                        return executeCast(pc, value, Caster.toString(operators[0].getAlias()))
                    }
                    if (op.equals("coalesce")) return executeCoalesce(pc, sql, source, operators, row)
                }
                'e' -> if (op.equals("exp")) return Double.valueOf(Math.exp(Caster.toDoubleValue(value)))
                'f' -> if (op.equals("floor")) return Double.valueOf(Math.floor(Caster.toDoubleValue(value)))
                'u' -> if (op.equals("upper") || op.equals("ucase")) return Caster.toString(value).toUpperCase()
                'l' -> {
                    if (op.equals("lower") || op.equals("lcase")) return Caster.toString(value).toLowerCase()
                    if (op.equals("ltrim")) return StringUtil.ltrim(Caster.toString(value), null)
                    if (op.equals("length")) return Double.valueOf(Caster.toString(value).length())
                }
                'm' -> if (op.equals("max") || op.equals("min")) {
                    // Get column data as array
                    val colData: Array = Caster.toArray(aggregateValues)
                    // Get column type
                    var colType: String = QueryImpl.getColumTypeName(Types.OTHER)

                    // If we're passing a column directly, get the type from it
                    if (operators[0] is ColumnExpression) {
                        val ce: ColumnExpression = operators[0] as ColumnExpression
                        colType = source.getColumn(ce.getColumn()).getTypeAsString()
                    } else if (operators[0] is Operation && aggregateValues!!.size > 0) {
                        if (Decision.isNumber(aggregateValues[0])) {
                            colType = "NUMERIC"
                        }
                    }
                    var sortDir = "desc"
                    var sortType = "text"
                    if (op.equals("min")) {
                        sortDir = "asc"
                    }
                    // Numeric-based sort
                    if (colType.equals("NUMERIC") || colType.equals("INTEGER") || colType.equals("DOUBLE") || colType.equals("DECIMAL") || colType.equals("BIGINT")
                            || colType.equals("TINYINT") || colType.equals("SMALLINT") || colType.equals("REAL")) {
                        sortType = "numeric"
                    }

                    // text-based sort
                    val comp: Comparator = ArrayUtil.toComparator(pc, sortType, sortDir, false)
                    // Sort the array with proper type and direction
                    colData.sortIt(comp)

                    // If there are no non-null values, return empty
                    return if (colData.size() === 0) {
                        null
                    } else colData.getE(1)
                    // The first item in the array is our "max" or "min"
                }
                'r' -> if (op.equals("rtrim")) return StringUtil.rtrim(Caster.toString(value), null)
                's' -> {
                    if (op.equals("sign")) return Double.valueOf(MathUtil.sgn(Caster.toDoubleValue(value)))
                    if (op.equals("sin")) return Double.valueOf(Math.sin(Caster.toDoubleValue(value)))
                    if (op.equals("soundex")) return StringUtil.soundex(Caster.toString(value))
                    if (op.equals("sin")) return Double.valueOf(Math.sqrt(Caster.toDoubleValue(value)))
                    if (op.equals("sum")) {
                        // If there are no non-null values, return empty
                        return if (aggregateValues!!.size == 0) {
                            null
                        } else ArrayUtil.sum(Caster.toArray(aggregateValues))
                    }
                }
                't' -> {
                    if (op.equals("tan")) return Double.valueOf(Math.tan(Caster.toDoubleValue(value)))
                    if (op.equals("trim")) return Caster.toString(value).trim()
                }
            }
        } else if (operators.size == 2) {

            // if(op.equals("=") || op.equals("in")) return executeEQ(pc,sql,qr,expression,row);
            val left: Object? = executeExp(pc, sql, source, operators[0], row)
            var right: Object? = executeExp(pc, sql, source, operators[1], row)
            when (op.charAt(0)) {
                'a' -> if (op.equals("atan2")) return Double.valueOf(Math.atan2(Caster.toDoubleValue(left), Caster.toDoubleValue(right)))
                'b' -> {
                    if (op.equals("bitand")) return OpUtil.bitand(pc, Caster.toDoubleValue(left), Caster.toDoubleValue(right))
                    if (op.equals("bitor")) return OpUtil.bitor(pc, Caster.toDoubleValue(left), Caster.toDoubleValue(right))
                }
                'c' -> {
                    if (op.equals("concat")) return Caster.toString(left).concat(Caster.toString(right))
                    if (op.equals("count")) return executeCount(pc, sql, source, operators)
                    if (op.equals("coalesce")) return executeCoalesce(pc, sql, source, operators, row)
                    if (op.equals("convert")) {
                        // If the user does convert( col1, 'string' ) it will be a ValueExpression and we can use it
                        // directly;
                        // If the user does convert( col1, string ) it will be a ColumnExpressin and we just want to use the
                        // column name ("string" in this case).
                        // convert() is the binary version of the unary operator cast()
                        // i.e. convert( col1, string ) is the same as cast( col1 as string )
                        if (operators[1] is ColumnExpression) {
                            right = (operators[1] as ColumnExpression).getColumnName()
                        }
                        return executeCast(pc, left, Caster.toString(right))
                    }
                }
                'i' -> if (op.equals("isnull")) return executeCoalesce(pc, sql, source, operators, row)
                'm' -> if (op.equals("mod")) {
                    // The result of any mathmatical operation involving a null is null
                    return if (left == null || right == null) {
                        null
                    } else Double.valueOf(castForMathDouble(left) % castForMathDouble(right))
                }
                'p' -> if (op.equals("power")) {
                    // The result of any mathmatical operation involving a null is null
                    return if (left == null || right == null) {
                        null
                    } else Math.pow(castForMathDouble(left), castForMathDouble(right))
                }
            }
        }
        // 3333333333333333333333333333333333333333333333333333333333333333333
        if (op.equals("in")) return executeIn(pc, sql, source, opn, row, false)
        if (op.equals("not_in")) return executeIn(pc, sql, source, opn, row, true)
        if (op.equals("coalesce")) return executeCoalesce(pc, sql, source, operators, row)
        if (op.equals("count")) return executeCount(pc, sql, source, operators)
        throw DatabaseException("unsupported sql statement ($op) ", null, sql, null)
    }

    @Throws(PageException::class)
    private fun executeCount(pc: PageContext, sql: SQL, source: QueryImpl, inputs: Array<Expression>): Integer {
        var isDistinct = false
        val inputList: List<Expression> = ArrayList<Expression>(Arrays.asList(inputs))
        val first: Expression = inputList[0]
        if (inputList.size() > 1 && first is Value && (first as Value).getString().equals("all")) {
            inputList.remove(0)
        } else if (inputList.size() > 1 && first is Value && (first as Value).getString().equals("distinct")) {
            isDistinct = true
            inputList.remove(0)
            // This would be count( DISTINCT col1, col2 )
            // HSQLDB doesn't support this either
            if (inputList.size() > 1) {
                throw IllegalQoQException("count( DISTINCT ... ) doesn't support more than one expression at this time", null, sql, null)
            }
        }
        if (inputList.size() > 1) {
            // HSQLDB doesn't support this either
            throw IllegalQoQException("count() only accepts one expression, but you provided " + inputList.size().toString() + ".", null, sql, null)
        }
        val input: Expression = inputList[0]
        // count(*), count(1), or count('asdf') just count the rows
        return if (input is Column && (input as Column).getAlias().equals("*") || input is Value) {
            Caster.toIntValue(source.getRecordcount())
        } else if (input is Column || input is Operation) {
            Caster.toIntValue(executeAggregateExp(pc, sql, source, input, false, isDistinct).size)
        } else {
            // I'm not sure if this would ever get hit.
            throw IllegalQoQException("count() function can only accept [*], a literal value, a column name, or an expression.", null, sql, null)
        }
    }

    @Throws(PageException::class)
    private fun executeCoalesce(pc: PageContext, sql: SQL, source: QueryImpl, inputs: Array<Expression>, row: Integer): Object? {
        for (thisOp in inputs) {
            val thisValue: Object? = executeExp(pc, sql, source, thisOp, row, null)
            if (thisValue != null) {
                return thisValue
            }
        }
        return null
    }

    @Throws(PageException::class)
    private fun executeCast(pc: PageContext, value: Object?, type: String): Object {
        return Caster.castTo(pc, CFTypes.toShort(type, true, CFTypes.TYPE_UNKNOW), type, value)
    }

    /*
	 * *
	 *
	 * @param expression / private void print(ZExpression expression) {
	 * print.ln("Operator:"+expression.getOperator().toLowerCase()); int len=expression.nbOperands();
	 * for(int i=0;i<len;i++) { print.ln("	["+i+"]=	" +expression.getOperand(i)); } }/ *
	 *
	 *
	 *
	 * / **
	 *
	 * execute an and operation
	 *
	 * @param source QueryResult to execute on it
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
    private fun executeAnd(pc: PageContext, sql: SQL, source: QueryImpl, expression: Operation2?, row: Int): Object {
        // print.out("("+expression.getLeft().toString(true)+" AND
        // "+expression.getRight().toString(true)+")");
        val rtn: Boolean = Caster.toBooleanValue(executeExp(pc, sql, source, expression.getLeft(), row))
        return if (!rtn) Boolean.FALSE else Caster.toBoolean(executeExp(pc, sql, source, expression.getRight(), row))
    }

    @Throws(PageException::class)
    private fun executeBracked(pc: PageContext, sql: SQL, source: QueryImpl, expression: BracketExpression?, row: Int): Object? {
        return executeExp(pc, sql, source, expression.getExp(), row)
    }

    /**
     *
     * execute an and operation
     *
     * @param sql
     * @param source QueryResult to execute on it
     * @param expression
     * @param row row of resultset to execute
     * @return result
     * @throws PageException
     */
    @Throws(PageException::class)
    private fun executeOr(pc: PageContext, sql: SQL, source: QueryImpl, expression: Operation2?, row: Int): Object {
        // print.out("("+expression.getLeft().toString(true)+" OR
        // "+expression.getRight().toString(true)+")");
        val rtn: Boolean = Caster.toBooleanValue(executeExp(pc, sql, source, expression.getLeft(), row))
        return if (rtn) Boolean.TRUE else Caster.toBoolean(executeExp(pc, sql, source, expression.getRight(), row))

        // print.out(rtn+ " or "+rtn2);
    }

    @Throws(PageException::class)
    private fun executeXor(pc: PageContext, sql: SQL, source: QueryImpl, expression: Operation2?, row: Int): Object {
        return if (Caster.toBooleanValue(executeExp(pc, sql, source, expression.getLeft(), row)) xor Caster.toBooleanValue(executeExp(pc, sql, source, expression.getRight(), row))) Boolean.TRUE else Boolean.FALSE
    }

    /**
     *
     * execute an equal operation
     *
     * @param sql
     * @param source QueryResult to execute on it
     * @param expression
     * @param row row of resultset to execute
     * @return result
     * @throws PageException
     */
    @Throws(PageException::class)
    private fun executeEQ(pc: PageContext, sql: SQL, source: QueryImpl, expression: Operation2?, row: Int): Object {
        return if (executeCompare(pc, sql, source, expression, row) == 0) Boolean.TRUE else Boolean.FALSE
    }

    /**
     *
     * execute a not equal operation
     *
     * @param sql
     * @param source QueryResult to execute on it
     * @param expression
     * @param row row of resultset to execute
     * @return result
     * @throws PageException
     */
    @Throws(PageException::class)
    private fun executeNEQ(pc: PageContext, sql: SQL, source: QueryImpl, expression: Operation2?, row: Int): Object {
        return if (executeCompare(pc, sql, source, expression, row) != 0) Boolean.TRUE else Boolean.FALSE
    }

    /**
     *
     * execute a less than operation
     *
     * @param sql
     * @param source QueryResult to execute on it
     * @param expression
     * @param row row of resultset to execute
     * @return result
     * @throws PageException
     */
    @Throws(PageException::class)
    private fun executeLT(pc: PageContext, sql: SQL, source: QueryImpl, expression: Operation2?, row: Int): Object {
        return if (executeCompare(pc, sql, source, expression, row) < 0) Boolean.TRUE else Boolean.FALSE
    }

    /**
     *
     * execute a less than or equal operation
     *
     * @param sql
     * @param source QueryResult to execute on it
     * @param expression
     * @param row row of resultset to execute
     * @return result
     * @throws PageException
     */
    @Throws(PageException::class)
    private fun executeLTE(pc: PageContext, sql: SQL, source: QueryImpl, expression: Operation2?, row: Int): Object {
        return if (executeCompare(pc, sql, source, expression, row) <= 0) Boolean.TRUE else Boolean.FALSE
    }

    /**
     *
     * execute a greater than operation
     *
     * @param sql
     * @param source QueryResult to execute on it
     * @param expression
     * @param row row of resultset to execute
     * @return result
     * @throws PageException
     */
    @Throws(PageException::class)
    private fun executeGT(pc: PageContext, sql: SQL, source: QueryImpl, expression: Operation2?, row: Int): Object {
        return if (executeCompare(pc, sql, source, expression, row) > 0) Boolean.TRUE else Boolean.FALSE
    }

    /**
     *
     * execute a greater than or equal operation
     *
     * @param sql
     * @param source QueryResult to execute on it
     * @param expression
     * @param row row of resultset to execute
     * @return result
     * @throws PageException
     */
    @Throws(PageException::class)
    private fun executeGTE(pc: PageContext, sql: SQL, source: QueryImpl, expression: Operation2?, row: Int): Object {
        return if (executeCompare(pc, sql, source, expression, row) >= 0) Boolean.TRUE else Boolean.FALSE
    }

    /**
     *
     * execute an equal operation
     *
     * @param sql
     * @param source QueryResult to execute on it
     * @param op
     * @param row row of resultset to execute
     * @return result
     * @throws PageException
     */
    @Throws(PageException::class)
    private fun executeCompare(pc: PageContext, sql: SQL, source: QueryImpl, op: Operation2?, row: Int): Int {
        // print.e(op.getLeft().getClass().getName());
        return OpUtil.compare(pc, executeExp(pc, sql, source, op.getLeft(), row), executeExp(pc, sql, source, op.getRight(), row))
    }

    @Throws(PageException::class)
    private fun executeMod(pc: PageContext, sql: SQL, source: QueryImpl, expression: Operation2?, row: Int): Object? {
        val left: Object? = executeExp(pc, sql, source, expression.getLeft(), row)
        val right: Object? = executeExp(pc, sql, source, expression.getRight(), row)

        // The result of any mathmatical operation involving a null is null
        if (left == null || right == null) {
            return null
        }
        val rightDouble = castForMathDouble(right)
        if (rightDouble == 0) {
            throw IllegalQoQException("Divide by zero not allowed.  Encountered while evaluating [" + expression.toString(true).toString() + "] in row " + row, null, sql, null)
        }
        return Double.valueOf(castForMathDouble(left) % rightDouble)
    }

    /**
     *
     * execute a greater than or equal operation
     *
     * @param sql
     * @param source QueryResult to execute on it
     * @param expression
     * @param row row of resultset to execute
     * @return result
     * @throws PageException
     */
    @Throws(PageException::class)
    private fun executeIn(pc: PageContext, sql: SQL, source: QueryImpl, expression: OperationN?, row: Int, isNot: Boolean): Boolean {
        val operators: Array<Expression> = expression.getOperants()
        val left: Object? = executeExp(pc, sql, source, operators[0], row)
        for (i in 1 until operators.size) {
            if (OpUtil.compare(pc, left, executeExp(pc, sql, source, operators[i], row)) === 0) return if (isNot) Boolean.FALSE else Boolean.TRUE
        }
        return if (isNot) Boolean.TRUE else Boolean.FALSE
    }

    /**
     * Cast value to Double, accounting for logic such as turning empty strings into zero.
     *
     * @param value Value for casting. Must be non-null
     * @return Value cast to a Double
     */
    @Throws(PageException::class)
    private fun castForMathDouble(value: Object): Double {
        return if (Caster.toString(value).equals("")) {
            Double.valueOf(0)
        } else Caster.toDoubleValue(value)
    }

    /**
     * Cast value to Int, accounting for logic such as turning empty strings into zero.
     *
     * @param value Value for casting. Must be non-null
     * @return Value cast to a Int
     */
    @Throws(PageException::class)
    private fun castForMathInt(value: Object): Integer {
        return if (Caster.toString(value).equals("")) {
            Integer.valueOf(0)
        } else Caster.toIntValue(value)
    }

    /**
     *
     * execute a minus operation
     *
     * @param sql
     * @param source QueryResult to execute on it
     * @param expression
     * @param row row of resultset to execute
     * @return result
     * @throws PageException
     */
    @Throws(PageException::class)
    private fun executeMinus(pc: PageContext, sql: SQL, source: QueryImpl, expression: Operation2?, row: Int): Object? {
        val left: Object? = executeExp(pc, sql, source, expression.getLeft(), row)
        val right: Object? = executeExp(pc, sql, source, expression.getRight(), row)

        // The result of any mathmatical operation involving a null is null
        return if (left == null || right == null) {
            null
        } else Double.valueOf(castForMathDouble(left) - castForMathDouble(right))
    }

    /**
     *
     * execute a divide operation
     *
     * @param sql
     * @param source QueryResult to execute on it
     * @param expression
     * @param row row of resultset to execute
     * @return result
     * @throws PageException
     */
    @Throws(PageException::class)
    private fun executeDivide(pc: PageContext, sql: SQL, source: QueryImpl, expression: Operation2?, row: Int): Object? {
        val left: Object? = executeExp(pc, sql, source, expression.getLeft(), row)
        val right: Object? = executeExp(pc, sql, source, expression.getRight(), row)

        // The result of any mathmatical operation involving a null is null
        if (left == null || right == null) {
            return null
        }
        val rightDouble = castForMathDouble(right)
        if (rightDouble == 0) {
            throw IllegalQoQException("Divide by zero not allowed.  Encountered while evaluating [" + expression.toString(true).toString() + "] in row " + row, null, sql, null)
        }
        return Double.valueOf(castForMathDouble(left) / rightDouble)
    }

    /**
     *
     * execute a multiply operation
     *
     * @param sql
     * @param source QueryResult to execute on it
     * @param expression
     * @param row row of resultset to execute
     * @return result
     * @throws PageException
     */
    @Throws(PageException::class)
    private fun executeMultiply(pc: PageContext, sql: SQL, source: QueryImpl, expression: Operation2?, row: Int): Object? {
        val left: Object? = executeExp(pc, sql, source, expression.getLeft(), row)
        val right: Object? = executeExp(pc, sql, source, expression.getRight(), row)

        // The result of any mathmatical operation involving a null is null
        return if (left == null || right == null) {
            null
        } else Double.valueOf(castForMathDouble(left) * castForMathDouble(right))
    }

    /**
     *
     * execute a bitwise operation
     *
     * @param sql
     * @param source QueryResult to execute on it
     * @param expression
     * @param row row of resultset to execute
     * @return result
     * @throws PageException
     */
    @Throws(PageException::class)
    private fun executeBitwise(pc: PageContext, sql: SQL, source: QueryImpl, expression: Operation2?, row: Int): Object? {
        val left: Object? = executeExp(pc, sql, source, expression.getLeft(), row)
        val right: Object? = executeExp(pc, sql, source, expression.getRight(), row)

        // The result of any mathmatical operation involving a null is null
        return if (left == null || right == null) {
            null
        } else Integer.valueOf(castForMathInt(left) xor castForMathInt(right))
    }

    /**
     *
     * execute a plus operation
     *
     * @param sql
     * @param source QueryResult to execute on it
     * @param expression
     * @param row row of resultset to execute
     * @return result
     * @throws PageException
     */
    @Throws(PageException::class)
    private fun executePlus(pc: PageContext, sql: SQL, source: QueryImpl, expression: Operation2?, row: Int): Object? {
        val left: Object? = executeExp(pc, sql, source, expression.getLeft(), row)
        val right: Object? = executeExp(pc, sql, source, expression.getRight(), row)
        val leftIsNumber: Boolean = Decision.isNumber(left)
        val rightIsNumber: Boolean = Decision.isNumber(right)

        // Short circuit to string concat if both are not numbers and one isn't a number and the other null.
        // Note, if both are null, we treat the operations as arethmatic and return null.
        // If at least one is a string, we concat turn any nulls to empty strings
        return if ((!leftIsNumber || !rightIsNumber) && !(leftIsNumber && right == null) && !(rightIsNumber && left == null) && !(right == null && left == null)) {
            Caster.toString(left) + Caster.toString(right)
        } else try {
            val dLeft: Double = Caster.toDoubleValue(left)
            val dRight: Double = Caster.toDoubleValue(right)

            // The result of any mathmatical operation involving a null is null
            if (left == null || right == null) {
                null
            } else Double.valueOf(dLeft + dRight)
            // If casting fails, we assume the inputs are strings and concat instead
            // Unlike SQL, we're not going to return null for a null string concat
        } catch (e: PageException) {
            Caster.toString(left) + Caster.toString(right)
        }
    }

    /**
     *
     * execute a between operation
     *
     * @param sql
     * @param source QueryResult to execute on it
     * @param expression
     * @param row row of resultset to execute
     * @return result
     * @throws PageException
     */
    @Throws(PageException::class)
    private fun executeBetween(pc: PageContext, sql: SQL, source: QueryImpl, expression: Operation3, row: Int): Object {
        val left: Object? = executeExp(pc, sql, source, expression.getExp(), row)
        val right1: Object? = executeExp(pc, sql, source, expression.getLeft(), row)
        val right2: Object? = executeExp(pc, sql, source, expression.getRight(), row)
        // print.out(left+" between "+right1+" and "+right2
        // +" = "+((Operator.compare(left,right1)>=0)+" && "+(Operator.compare(left,right2)<=0)));
        return if (OpUtil.compare(pc, left, right1) >= 0 && OpUtil.compare(pc, left, right2) <= 0) Boolean.TRUE else Boolean.FALSE
    }

    @Throws(PageException::class)
    private fun executeLike(pc: PageContext, sql: SQL, source: QueryImpl, expression: Operation3, row: Int): Object {
        return if (LikeCompare.like(sql, Caster.toString(executeExp(pc, sql, source, expression.getExp(), row)),
                        Caster.toString(executeExp(pc, sql, source, expression.getLeft(), row)), Caster.toString(executeExp(pc, sql, source, expression.getRight(), row)))) Boolean.TRUE else Boolean.FALSE
    }

    @Throws(PageException::class)
    private fun executeLike(pc: PageContext, sql: SQL, source: QueryImpl, expression: Operation2, row: Int): Boolean {
        return LikeCompare.like(sql, Caster.toString(executeExp(pc, sql, source, expression.getLeft(), row)),
                Caster.toString(executeExp(pc, sql, source, expression.getRight(), row)))
    }

    /**
     * Executes a constant value
     *
     * @param sql
     * @param source
     * @param column
     * @param row
     * @return result
     * @throws PageException
     */
    @Throws(PageException::class)
    private fun executeColumn(pc: PageContext, sql: SQL, source: QueryImpl, column: Column, row: Int): Object? {
        return executeColumn(pc, sql, source, column, row, null)
    }

    @Throws(PageException::class)
    private fun executeColumn(pc: PageContext, sql: SQL, source: QueryImpl, column: Column?, row: Int, defaultValue: Object?): Object? {
        if (column.isParam()) {
            val pos: Int = column.getColumnIndex()
            if (sql.getItems().length <= pos) throw IllegalQoQException("Invalid SQL Statement. Not enough parameters provided.", null, sql, null)
            val param: SQLItem = sql.getItems().get(pos)
            // If null=true is used with query param
            return if (param.isNulls()) null else try {
                param.getValueForCF()
            } catch (e: PageException) {
                // Create best error message based on whether param was defined as ? or :name
                if (param is NamedSQLItem) {
                    throw IllegalQoQException("Parameter [:" + (param as NamedSQLItem).getName().toString() + "] is invalid.", e.getMessage(), sql, null).initCause(e) as IllegalQoQException
                } else {
                    throw IllegalQoQException(DBUtilImpl().toStringType(param.getType()).toString() + " parameter in position " + (pos + 1) + " is invalid.", e.getMessage(), sql, null).initCause(e) as IllegalQoQException
                }
            }
        }
        return column.getValue(pc, source, row, defaultValue)
    }

    // Helpers for exceptions in Lambdas
    @FunctionalInterface
    interface ThrowingIntConsumer {
        /**
         * Applies this function to the given argument.
         *
         * @param t the Consumer argument
         */
        @Throws(Exception::class)
        fun accept(t: Int)
    }

    @FunctionalInterface
    interface ThrowingConsumer {
        /**
         * Applies this function to the given argument.
         *
         * @param t the Consumer argument
         */
        @Throws(Exception::class)
        fun accept(t: Map.Entry<String?, QueryImpl?>?)
    }

    @FunctionalInterface
    interface ThrowingFilter {
        /**
         * Applies this function to the given argument.
         *
         * @param t the Consumer argument
         */
        @Throws(Exception::class)
        fun test(t: Int): Boolean
    }
}