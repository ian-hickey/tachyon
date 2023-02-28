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
package lucee.runtime.tag

import java.sql.Types.BIGINT

/**
 * Checks the data type of a query parameter. The cfqueryparam tag is nested within a cfquery tag.
 * It is embedded within the query SQL statement. If you specify its optional parameters,
 * cfqueryparam also performs data validation.
 *
 *
 *
 */
class QueryParam : TagImpl() {
    private var item: SQLItemImpl? = SQLItemImpl()

    /**
     * Specifies the character that separates values in the list of parameter values in the value
     * attribute. The default is a comma. If you specify a list of values for the value attribute, you
     * must also specify the list attribute.
     */
    private var separator: String? = ","

    /**
     * Yes or No. Indicates that the parameter value of the value attribute is a list of values,
     * separated by a separator character. The default is No
     */
    private var list: Boolean? = null

    /**
     * Maximum length of the parameter. The default value is the length of the string specified in the
     * value attribute.
     */
    private var maxlength = -1.0
    private var charset: Charset? = null
    @Override
    fun release() {
        separator = ","
        list = null
        maxlength = -1.0
        item = SQLItemImpl()
        charset = null
    }

    /**
     * set the value list Yes or No. Indicates that the parameter value of the value attribute is a list
     * of values, separated by a separator character. The default is No
     *
     * @param list value to set
     */
    fun setList(list: Boolean) {
        this.list = list
    }

    /**
     * set the value null Yes or No. Indicates whether the parameter is passed as a null. If Yes, the
     * tag ignores the value attribute. The default is No.
     *
     * @param nulls value to set
     */
    fun setNull(nulls: Boolean) {
        item.setNulls(nulls)
    }

    /**
     * set the value value
     *
     * @param value value to set
     */
    fun setValue(value: Object?) {
        item.setValue(value)
    }

    /**
     * set the value maxlength Maximum length of the parameter. The default value is the length of the
     * string specified in the value attribute.
     *
     * @param maxlength value to set
     */
    fun setMaxlength(maxlength: Double) {
        this.maxlength = maxlength
    }

    fun setCharset(charset: String?) {
        this.charset = CharsetUtil.toCharset(charset)
    }

    /**
     * set the value separator Specifies the character that separates values in the list of parameter
     * values in the value attribute. The default is a comma. If you specify a list of values for the
     * value attribute, you must also specify the list attribute.
     *
     * @param separator value to set
     */
    fun setSeparator(separator: String?) {
        this.separator = separator
    }

    /**
     * set the value scale Number of decimal places of the parameter. The default value is zero.
     *
     * @param scale value to set
     */
    fun setScale(scale: Double) {
        item.setScale(scale.toInt())
    }

    /**
     * set the value cfsqltype The SQL type that the parameter (any type) will be bound to.
     *
     * @param type value to set
     * @throws DatabaseException
     */
    @Throws(DatabaseException::class)
    fun setCfsqltype(type: String?) {
        item.setType(SQLCaster.toSQLType(type))
    }

    @Throws(DatabaseException::class)
    fun setSqltype(type: String?) {
        item.setType(SQLCaster.toSQLType(type))
    }

    @Override
    @Throws(PageException::class)
    fun doStartTag(): Int {
        var parent: Tag = getParent()
        while (parent != null && parent !is Query) {
            parent = parent.getParent()
        }
        if (parent is Query) {
            val query: Query = parent
            if (!item.isNulls() && !item.isValueSet()) throw ApplicationException("Attribute [value] from tag [queryparam] is required when attribute [null] is false")
            val value: Object = item.getValue()
            if (Boolean.TRUE.equals(list) || list == null && Decision.isArray(value) && ARRAY_TYPES!!.contains(item.getType())) {
                val arr: Array?
                if (Decision.isArray(value)) {
                    arr = Caster.toArray(value)
                } else {
                    val v: String = Caster.toString(value)
                    if (StringUtil.isEmpty(v)) {
                        arr = ArrayImpl()
                        arr.append("")
                    } else arr = ListUtil.listToArrayRemoveEmpty(v, separator)
                }
                val len: Int = arr.size()
                val sb = StringBuffer()
                for (i in 1..len) {
                    query!!.setParam(item.clone(check(arr.getE(i), item.getType(), maxlength.toInt(), charset)))
                    if (i > 1) sb.append(',')
                    sb.append('?')
                }
                write(sb.toString())
            } else {
                check(item.getValue(), item.getType(), maxlength.toInt(), charset)
                val vals: String
                vals = ""
                if (vals === item.getValue() && (item.getType() === 4 || item.getType() === -7 || item.getType() === -5)) throw ApplicationException("Invalid data ['" + item.getValue().toString() + "'] for CFSQLTYPE in CFQUERYPARAM")
                query!!.setParam(item)
                write("?")
            }
        } else {
            throw ApplicationException("Wrong Context, tag QueryParam must be inside a Query tag")
        }
        return SKIP_BODY
    }

    private fun write(str: String?) {
        try {
            pageContext.write(str)
        } catch (e: IOException) {
        }
    }

    companion object {
        val ARRAY_TYPES: List<Integer?>? = Arrays.asList(BIGINT, BOOLEAN, CHAR, DATE, DECIMAL, DOUBLE, FLOAT, INTEGER, NCHAR, NUMERIC, NVARCHAR, REAL, SMALLINT, TIME,
                TIMESTAMP, TINYINT, VARCHAR)

        @Throws(PageException::class)
        fun check(value: Object?, type: Int, maxlength: Int, charset: Charset?): Object? {
            if (maxlength != -1 || charset != null) {
                val str: String
                str = if (BIGINT === type || INTEGER === type || SMALLINT === type || TINYINT === type) {
                    Caster.toString(Caster.toIntValue(value))
                } else if (BOOLEAN === type) {
                    Caster.toString(Caster.toBooleanValue(value))
                } else if (DECIMAL === type) {
                    Caster.toDecimal(value, false)
                } else Caster.toString(value)
                if (charset != null) {
                    if (!StringUtil.isCompatibleWith(str, charset)) throw DatabaseException(
                            "the given value [" + (if (str.length() > 20) str.substring(0, 20).toString() + "..." else str) + "] is not compatible with the requested charset [" + charset + "] ", null,
                            null, null)
                }
                if (maxlength > 0) {
                    val len: Int = if (charset == null) str.length() else str.getBytes(charset).length
                    if (len > maxlength) {
                        throw DatabaseException(
                                "value [" + value + "] is too large, defined maxlength is [" + Caster.toString(maxlength) + "] but binary length of value is [" + len + "]", null, null,
                                null)
                    }
                }
            }
            return value
        }
    }
}