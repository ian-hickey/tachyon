/**
 * Copyright (c) 2014, the Railo Company Ltd.
 * Copyright (c) 2015, Lucee Assosication Switzerland
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
package lucee.runtime.sql.exp

import lucee.runtime.PageContext

class ColumnExpression(var columnName: String?,
                       /**
                        * @return the columnIndex
                        */
                       @get:Override override val columnIndex: Int, private var cacheColumn: Boolean) : ExpressionSupport(), Column {
    @get:Override
    override var table: String? = null
        private set
    private var columnKey: Collection.Key? = null
    private var columnAliasKey: Collection.Key? = null
    private var hasBracked = false

    @get:Override
    override var isParam = false

    private var col: QueryColumn? = null

    @Override
    override fun toString(): String {
        return "table:" + table + ";column:" + columnName + ";hasBracked:" + hasBracked + ";columnIndex:" + columnIndex
    }

    constructor(value: String?, columnIndex: Int) : this(value, columnIndex, true) {}

    fun setSub(sub: String?) {
        if (table == null) {
            table = columnName
            columnName = sub
        } else columnName = columnName.toString() + "." + sub
    }

    @Override
    override fun toString(noAlias: Boolean): String? {
        return if (hasAlias() && !noAlias) fullName.toString() + " as " + getAlias() else fullName
    }

    @get:Override
    override val fullName: String?
        get() = if (table == null) columnName else table.toString() + "." + columnName

    @Override
    override fun getAlias(): String? {
        return if (!hasAlias()) getColumn().getString() else super.getAlias()
    }

    @Override
    fun getColumn(): Collection.Key? {
        if (columnKey == null) columnKey = KeyImpl.init(columnName)
        return columnKey
    }

    @get:Override
    override val columnAlias: Collection.Key?
        get() {
            if (columnAliasKey == null) columnAliasKey = KeyImpl.init(getAlias())
            return columnAliasKey
        }

    @Override
    override fun hasBracked(): Boolean {
        return hasBracked
    }

    @Override
    override fun hasBracked(b: Boolean) {
        hasBracked = b
    }

    // MUST handle null correctly
    @Override
    @Throws(PageException::class)
    override operator fun getValue(pc: PageContext?, qr: Query?, row: Int): Object? {
        return QueryUtil.getValue(pc, getCol(qr), row)
    }

    @Override
    override operator fun getValue(pc: PageContext?, qr: Query?, row: Int, defaultValue: Object?): Object? {
        return try {
            getCol(qr).get(row, defaultValue)
            // Per the interface, methods accepting a default value cannot throw an exception,
            // so we must return the default value if any exceptions happen.
        } catch (e: PageException) {
            defaultValue
        }
    }

    /**
     * Tells this column expression to not cache the column reference back to the original query
     */
    override fun setCacheColumn(cacheColumn: Boolean) {
        this.cacheColumn = cacheColumn
    }

    /**
     * Acquire the actual query column reference, taking caching into account
     * We cache the lookup of the column for basic selects because we run the same thing
     * over and over on the same query object.  But for partitioned selects, we have multiple query
     * objects we run this on, so we can't cache the column reference
     */
    @Throws(PageException::class)
    private fun getCol(qr: Query?): QueryColumn? {
        // If we're not caching the query column, get it fresh
        if (!cacheColumn) {
            return qr.getColumn(getColumn())
            // If we are caching and we have no reference, create it and return it
        } else if (col == null) {
            // This behavior needs to be thread safe.
            synchronized(this) {

                // Double check lock pattern in case another thread beat us
                return if (col != null) {
                    col
                } else qr.getColumn(getColumn()).also { col = it }
            }
            // If we are caching and we have the reference already, just return it!
        } else {
            return col
        }
    }

    @Override
    override fun reset() {
        col = null
    }

    init {
        if (columnName!!.equals("?")) {
            isParam = true
        }
    }
}