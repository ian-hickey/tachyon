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

import java.io.IOException

class QueryPartitions(sql: SQL?, columns: Array<Expression?>?, groupbys: Array<Expression?>?, target: QueryImpl?, additionalColumns: Set<String?>?, qoQ: QoQ?) {
    // Select expressions for target query
    private val columns: Array<Expression?>?

    // Array of keys for fast lookup
    private val columnKeys: Array<Collection.Key?>?

    // Needed for functions and aggregates but not explicitly part of the final select
    private val additionalColumns: Set<Collection.Key?>?

    // Group by expressions
    private val groupbys: Array<Expression?>?

    // Target query for column references
    private val target: QueryImpl?

    // Mapof partitioned query data. Key is unique string representing grouped data, value is a
    // Query object representing the matching rows in that group/partition
    private val partitions: ConcurrentHashMap<String?, QueryImpl?>? = ConcurrentHashMap<String?, QueryImpl?>()

    // Reference to QoQ instance
    private val qoQ: QoQ?

    // SQL instance
    private val sql: SQL?

    /**
     * Adds empty partition for aggregating empty results
     *
     * @param source Source query to get data from
     * @param target target query (for column reference)
     * @throws PageException
     */
    @Throws(PageException::class)
    fun addEmptyPartition(source: QueryImpl?, target: QueryImpl?) {
        partitions.put("default", createPartition(target, source, false))
    }

    /**
     * Call this to add a single row to the proper partition finaizedColumnVals is true when all data in
     * the source Query is fully realized and there are no expressions left to evaluate
     *
     * @param pc PageContext
     * @param source Source query to get data from
     * @param row Row to get data from
     * @param finalizedColumnVals If we're adding finalized data, just copy it across. Easy. This
     * applies when distincting a result set after it's already been processed
     * @throws PageException
     */
    @Throws(PageException::class)
    fun addRow(pc: PageContext?, source: QueryImpl?, row: Int, finalizedColumnVals: Boolean) {
        // Generate unique key based on row data
        val partitionKey = buildPartitionKey(pc, source, row, finalizedColumnVals)
        // Create partition if necessary
        val targetPartition: QueryImpl = partitions.computeIfAbsent(partitionKey) { k ->
            try {
                return@computeIfAbsent createPartition(target, source, finalizedColumnVals)
            } catch (e: Exception) {
                throw RuntimeException(e)
            }
        }
        val newRow: Int = targetPartition.addRow()

        // If we're adding finalized data, just copy it across. Easy. This applies when distincting
        // a result set after it's already been processed
        if (finalizedColumnVals) {
            val sourceColKeys: Array<Collection.Key?> = source.getColumnNames()
            val targetColKeys: Array<Collection.Key?> = targetPartition.getColumnNames()
            for (col in targetColKeys.indices) {
                targetPartition.setAt(targetColKeys[col], newRow, source.getColumn(sourceColKeys[col]).get(row, null), true)
            }
        } else {
            for (cell in columns.indices) {

                // Literal values
                if (columns!![cell] is Value) {
                    val v: Value? = columns[cell] as Value?
                    targetPartition.setAt(columnKeys!![cell], newRow, v.getValue(), true)
                } else if (columns[cell] is ColumnExpression) {
                    val ce: ColumnExpression? = columns[cell] as ColumnExpression?
                    targetPartition.setAt(columnKeys!![cell], newRow, ce.getValue(pc, source, row, null), true)
                }
            }
            // Populate additional columns needed for operations but are not found in the select
            // list above
            for (col in additionalColumns!!) {
                if (source.containsKey(col)) {
                    targetPartition.setAt(col, newRow, source.getColumn(col).get(row, null), true)
                }
            }
        }
    }

    /**
     * Generate a unique string that represents the column data being grouped on
     *
     * @param pc PageContext
     * @param source QueryImpl to get data from. Note, operations have not yet been processed
     * @param row Row to get data from
     * @param finalizedColumnVals If we're adding finalized data, just copy it across. Easy. This
     * applies when distincting a result set after it's already been processed
     * @return unique string
     * @throws PageException
     */
    @Throws(PageException::class)
    fun buildPartitionKey(pc: PageContext?, source: QueryImpl?, row: Int, finalizedColumnVals: Boolean): String? {
        var partitionKey: String? = ""
        for (cell in groupbys.indices) {
            var value: String
            // This is when reading columns out of a previous union query that doesn't have any
            // expressions in it, just literal values It's important that we are just getting this
            // value by index since the group by expressions may be a reference to the select
            // expressions from another query object
            value = if (finalizedColumnVals) {
                Caster.toString(source.getAt(source.getColumnNames().get(cell), row))
            } else {
                Caster.toString(qoQ.getValue(pc, sql, source, row, null, groupbys!![cell]))
            }
            // Internally Java uses a StringBuilder for this concatenation
            partitionKey += createUniqueValue(value, groupbys!![cell].toString(false))
        }
        return partitionKey
    }

    /**
     * Helper function to turn column data into string
     *
     * @param value
     * @param col
     * @return
     * @throws PageException
     */
    @Throws(PageException::class)
    private fun createUniqueValue(value: String?, col: String?): String? {

        // There doesn't seem to be a key length on a HashMap, but it seems like a good
        // idea to hash long values. Not hashing everything, because that is slower.
        return if (value!!.length() > 255) {
            try {
                MD5.getDigestAsString(value)
            } catch (e: IOException) {
                throw DatabaseException("Unable to hash query value for column [$col] for partitioning.", e.getMessage(), null, null)
            }
        } else {
            // Inject some characters to prevent accidental overlap of data been nearby columns
            "______________$value"
        }
    }

    /**
     * Get number of partitions
     *
     * @return
     */
    val partitionCount: Int
        get() = partitions.size()

    /**
     * Get partition Map
     *
     * @return
     */
    fun getPartitions(): ConcurrentHashMap<String?, QueryImpl?>? {
        return partitions
    }

    /**
     * Get array of grouped Query objects
     *
     * @return
     */
    val partitionArray: Array<Any?>?
        get() = partitions.values().toArray()

    /**
     * Create new Query for a partition. Needs to have all ColumnExpressions in the final select as well
     * as any additional columns required for operation expressions
     *
     * @param target Query for target data (for column refernces)
     * @param source source query we're getting data from
     * @param finalizedColumnVals If we're adding finalized data, just copy it
     * across. Easy. This applies when distincting a result set after it's already been
     * processed
     * @return Empty Query with all the needed columns
     * @throws PageException
     */
    @Throws(PageException::class)
    private fun createPartition(target: QueryImpl?, source: QueryImpl?, finalizedColumnVals: Boolean): QueryImpl? {
        val newTarget = QueryImpl(arrayOfNulls<Collection.Key?>(0), 0, "query", sql)

        // If we're just distincting fully-realized data, this is just a simple lookup
        if (finalizedColumnVals) {
            for (i in columns.indices) {
                val ce: ColumnExpression? = columns!![i] as ColumnExpression?
                newTarget.addColumn(ce.getColumn(), ArrayImpl(), target.getColumn(target.getColumnNames().get(i)).getType())
            }
        } else {
            val expSelects: Array<Expression?>? = columns
            val selCount = expSelects!!.size

            // Loop over all select expressions and add column to new query for every column
            // expression and literal
            for (i in 0 until selCount) {
                val expSelect: Expression? = expSelects[i]
                val alias: Key = Caster.toKey(expSelect.getAlias())
                if (expSelect is ColumnExpression) {
                    val ce: ColumnExpression? = expSelect as ColumnExpression?
                    var type: Int = Types.OTHER
                    if (!"?".equals(ce.getColumnName())) type = source.getColumn(Caster.toKey(ce.getColumnName())).getType()
                    newTarget.addColumn(alias, ArrayImpl(), type)
                } else if (expSelect is Literal) {
                    newTarget.addColumn(alias, ArrayImpl(), Types.OTHER)
                }
            }

            // As well as any additional columns that need to be used for expressions and aggregates
            // but don't appear in the final select.
            for (col in additionalColumns!!) {
                // This check is here because it seems the SelectsParser also lists table names as
                // ColumnExpressions
                if (source.containsKey(col)) {
                    newTarget.addColumn(col, ArrayImpl(), source.getColumn(col).getType())
                }
            }
        }
        return newTarget
    }

    /**
     * Constructor
     *
     * @param sql
     * @param columns
     * @param groupbys
     * @param target
     * @param additionalColumns
     * @param qoQ
     * @throws PageException
     */
    init {
        this.sql = sql
        this.qoQ = qoQ
        this.columns = columns
        this.groupbys = groupbys
        // This happens when using distinct with no group by
        // Just assume we're grouping on the entire select list
        if (this.groupbys!!.size == 0) {
            val temp: ArrayList<Expression?> = ArrayList<Expression?>()
            for (col in columns!!) {
                if (col !is OperationAggregate) {
                    temp.add(col)
                }
            }
            this.groupbys = temp.toArray(arrayOfNulls<Expression?>(0))
        }
        this.target = target

        // Convert these strings to Keys now so we don't do it over and over later
        this.additionalColumns = HashSet<Collection.Key?>()
        for (col in additionalColumns!!) {
            this.additionalColumns.add(Caster.toKey(col))
        }
        // Convert these Expression aliases to Keys now so we don't do it over and over later
        columnKeys = arrayOfNulls<Collection.Key?>(columns!!.size)
        for (cell in columns.indices) {
            columnKeys!![cell] = Caster.toKey(columns!![cell].getAlias())
        }
    }
}