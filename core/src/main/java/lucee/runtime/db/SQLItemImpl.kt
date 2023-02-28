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
package lucee.runtime.db

import java.io.Serializable

/**
 *
 */
class SQLItemImpl : SQLItem, Serializable {
    /**
     * Yes or No. Indicates whether the parameter is passed as a null. If Yes, the tag ignores the value
     * attribute. The default is No.
     */
    private var nulls = false

    /**
     * Specifies the actual value that get passed to the right of the comparison operator in a where
     * clause.
     */
    private var value: Object? = null
    private var cfValue: Object? = null

    /** Number of decimal places of the parameter. The default value is zero.  */
    private var scale = 0

    /** The SQL type that the parameter (any type) will be bound to.  */
    private var type: Int = Types.CHAR

    @get:Override
    var isValueSet = false
        private set
    private var charset: Charset? = null
    var maxlength = -1
        private set

    /**
     * constructor of the class
     */
    constructor() {}

    /**
     * constructor of the class
     *
     * @param value
     */
    constructor(value: Object?) {
        this.value = value
    }

    /**
     * constructor of the class
     *
     * @param value
     */
    constructor(value: Object?, type: Int) {
        this.value = value
        this.type = type
    }

    constructor(value: Object?, type: Int, maxlength: Int, charset: Charset?) {
        this.value = value
        this.type = type
        this.charset = charset
        this.maxlength = maxlength
    }

    @Override
    fun isNulls(): Boolean {
        return nulls
    }

    @Override
    fun setNulls(nulls: Boolean) {
        this.nulls = nulls
    }

    @Override
    fun getScale(): Int {
        return scale
    }

    @Override
    fun setScale(scale: Int) {
        this.scale = scale
    }

    @Override
    fun getValue(): Object? {
        return value
    }

    @Override
    fun setValue(value: Object?) {
        isValueSet = true
        this.value = value
    }

    @Override
    fun getType(): Int {
        return type
    }

    @Override
    fun setType(type: Int) {
        this.type = type
    }

    @Override
    fun clone(`object`: Object?): SQLItem {
        val item = SQLItemImpl()
        item.nulls = nulls
        item.scale = scale
        item.type = type
        item.value = `object`
        return item
    }

    @get:Throws(PageException::class)
    @get:Override
    val valueForCF: Object?
        get() {
            if (cfValue == null) {
                cfValue = SQLCaster.toCFTypex(this)
            }
            return cfValue
        }

    fun getCharset(): Charset? {
        return charset
    }

    @Override
    override fun toString(): String {
        return try {
            Caster.toString(valueForCF, "")
        } catch (e: PageException) {
            Caster.toString(getValue(), "")
        }
    }

    fun duplicate(): SQLItem {
        val rtn = SQLItemImpl(value, type, maxlength, charset)
        rtn.nulls = nulls
        rtn.cfValue = cfValue
        rtn.isValueSet = isValueSet
        rtn.scale = scale
        return rtn
    }

    companion object {
        fun duplicate(item: SQLItem?): SQLItem? {
            return if (item !is SQLItemImpl) item else (item as SQLItemImpl?)!!.duplicate()
        }
    }
}