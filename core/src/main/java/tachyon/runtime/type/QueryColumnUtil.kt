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
package tachyon.runtime.type

import java.io.Serializable

/**
 * Helper class for the QueryColumnImpl
 */
object QueryColumnUtil : Serializable {
    private const val serialVersionUID = 4654833724194716718L

    /**
     * reset the type of the column
     */
    internal fun resetType(column: QueryColumnImpl?) {
        column!!.type = Types.OTHER
    }

    /**
     * redefine type of value
     *
     * @param value
     * @return redefined type of the value
     */
    internal fun reDefineType(column: QueryColumnImpl?, value: Object?): Object? {
        column!!.typeChecked = false
        if (value == null || column!!.type === Types.OTHER) return value
        return if (value is String && (value as String?).isEmpty()) value else when (column!!.type) {
            Types.DOUBLE -> reDefineDouble(column, value)
            Types.BIGINT -> reDefineDecimal(column, value)
            Types.NUMERIC -> reDefineDouble(column, value)
            Types.INTEGER -> reDefineInteger(column, value)
            Types.TINYINT -> reDefineTinyInt(column, value)
            Types.FLOAT -> reDefineFloat(column, value)
            Types.DECIMAL -> reDefineDecimal(column, value)
            Types.REAL -> reDefineFloat(column, value)
            Types.SMALLINT -> reDefineShort(column, value)
            Types.TIMESTAMP -> reDefineDateTime(column, value)
            Types.DATE -> reDefineDateTime(column, value)
            Types.TIME -> reDefineDateTime(column, value)
            Types.CHAR -> reDefineString(column, value)
            Types.VARCHAR -> reDefineString(column, value)
            Types.LONGVARCHAR -> reDefineString(column, value)
            Types.CLOB -> reDefineClob(column, value)
            Types.BOOLEAN -> reDefineBoolean(column, value)
            Types.BIT -> reDefineBoolean(column, value)
            Types.BINARY -> reDefineBinary(column, value)
            Types.VARBINARY -> reDefineBinary(column, value)
            Types.LONGVARBINARY -> reDefineBinary(column, value)
            Types.BLOB -> reDefineBlob(column, value)
            Types.ARRAY -> reDefineOther(column, value)
            Types.DATALINK -> reDefineOther(column, value)
            Types.DISTINCT -> reDefineOther(column, value)
            Types.JAVA_OBJECT -> reDefineOther(column, value)
            Types.NULL -> reDefineOther(column, value)
            Types.STRUCT -> reDefineOther(column, value)
            Types.REF -> reDefineOther(column, value)
            else -> value
        }
    }

    private fun reDefineBoolean(column: QueryColumnImpl?, value: Object?): Object? {
        if (Decision.isCastableToBoolean(value)) return value
        resetType(column)
        return value
    }

    private fun reDefineDouble(column: QueryColumnImpl?, value: Object?): Object? {
        if (Decision.isCastableToNumeric(value)) return value
        resetType(column)
        return value
    }

    private fun reDefineFloat(column: QueryColumnImpl?, value: Object?): Object? {
        if (Decision.isCastableToNumeric(value)) return value
        resetType(column)
        return value
    }

    private fun reDefineInteger(column: QueryColumnImpl?, value: Object?): Object? {
        if (Decision.isCastableToNumeric(value)) return value
        resetType(column)
        return value
    }

    private fun reDefineShort(column: QueryColumnImpl?, value: Object?): Object? {
        val dbl: Double = Caster.toDoubleValue(value, true, Double.NaN)
        if (Decision.isValid(dbl)) {
            val sht = dbl.toShort()
            if (sht.toDouble() == dbl) return value
            column!!.type = Types.DOUBLE
            return value
        }
        resetType(column)
        return value
    }

    private fun reDefineTinyInt(column: QueryColumnImpl?, value: Object?): Object? {
        if (Decision.isCastableToNumeric(value)) return value
        resetType(column)
        return value
    }

    private fun reDefineDecimal(column: QueryColumnImpl?, value: Object?): Object? {
        if (Decision.isCastableToNumeric(value)) return value
        resetType(column)
        return value
    }

    private fun reDefineDateTime(column: QueryColumnImpl?, value: Object?): Object? {
        if (Decision.isDateSimple(value, true)) return value
        resetType(column)
        return value
    }

    private fun reDefineString(column: QueryColumnImpl?, value: Object?): Object? {
        if (Decision.isCastableToString(value)) return value
        resetType(column)
        return value
    }

    private fun reDefineClob(column: QueryColumnImpl?, value: Object?): Object? {
        if (Decision.isCastableToString(value)) return value
        resetType(column)
        return value
    }

    private fun reDefineBinary(column: QueryColumnImpl?, value: Object?): Object? {
        if (Decision.isCastableToBinary(value, false)) return value
        resetType(column)
        return value
    }

    private fun reDefineBlob(column: QueryColumnImpl?, value: Object?): Object? {
        if (Decision.isCastableToBinary(value, false)) return value
        resetType(column)
        return value
    }

    private fun reDefineOther(column: QueryColumnImpl?, value: Object?): Object? {
        resetType(column)
        return value
    }

    /**
     * reorganize type of a column
     *
     * @param reorganize
     */
    internal fun reOrganizeType(column: QueryColumnImpl?) {
        if (column!!.type === Types.OTHER && !column!!.typeChecked) {
            column!!.typeChecked = true
            if (column!!.size() > 0) {
                checkOther(column, column!!.data!!.get(0))

                // get Type
                for (i in 1 until column!!.size()) {
                    when (column!!.type) {
                        Types.NULL -> checkOther(column, column!!.data!!.get(i))
                        Types.TIMESTAMP -> checkDate(column, column!!.data!!.get(i))
                        Types.BOOLEAN -> checkBoolean(column, column!!.data!!.get(i))
                        Types.DOUBLE -> checkDouble(column, column!!.data!!.get(i))
                        Types.VARCHAR -> checkBasic(column, column!!.data!!.get(i))
                        else -> {
                        }
                    }
                }
            }
        }
    }

    private fun checkOther(column: QueryColumnImpl?, value: Object?) {
        // NULL
        if (value == null) {
            column!!.type = Types.NULL
            return
        }
        // DateTime
        if (Decision.isDateSimple(value, false)) {
            column!!.type = Types.TIMESTAMP
            return
        }
        // Boolean
        if (Decision.isBoolean(value)) {
            column!!.type = Types.BOOLEAN
            return
        }
        // Double
        if (Decision.isNumber(value)) {
            column!!.type = Types.DOUBLE
            return
        }
        // String
        val str: String = Caster.toString(value, null)
        if (str != null) {
            column!!.type = Types.VARCHAR
            return
        }
    }

    private fun checkDate(column: QueryColumnImpl?, value: Object?) {
        // NULL
        if (value == null) return
        // DateTime
        if (Decision.isDateSimple(value, false)) {
            column!!.type = Types.TIMESTAMP
            return
        }
        // String
        val str: String = Caster.toString(value, null)
        if (str != null) {
            column!!.type = Types.VARCHAR
            return
        }
        // Other
        column!!.type = Types.OTHER
        return
    }

    private fun checkBoolean(column: QueryColumnImpl?, value: Object?) {
        // NULL
        if (value == null) return
        // Boolean
        if (Decision.isBoolean(value)) {
            column!!.type = Types.BOOLEAN
            return
        }
        // Double
        if (Decision.isNumber(value)) {
            column!!.type = Types.DOUBLE
            return
        }
        // String
        val str: String = Caster.toString(value, null)
        if (str != null) {
            column!!.type = Types.VARCHAR
            return
        }
        // Other
        column!!.type = Types.OTHER
        return
    }

    private fun checkDouble(column: QueryColumnImpl?, value: Object?) {
        // NULL
        if (value == null) return
        // Double
        if (Decision.isNumber(value)) {
            column!!.type = Types.DOUBLE
            return
        }
        // String
        val str: String = Caster.toString(value, null)
        if (str != null) {
            column!!.type = Types.VARCHAR
            return
        }
        // Other
        column!!.type = Types.OTHER
        return
    }

    private fun checkBasic(column: QueryColumnImpl?, value: Object?) {
        // NULL
        if (value == null) return
        // Date
        if (value is Date || value is Number) return
        // String
        val str: String = Caster.toString(value, null)
        if (str != null) {
            return
        }
        // OTHER
        column!!.type = Types.OTHER
        return
    }
}