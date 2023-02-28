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

import java.util.Date

/**
 * represent a named function value for a functions
 */
class FunctionValueImpl : FunctionValue, Dumpable {
    private val name: Collection.Key?
    private val names: Array<String?>?
    private val value: Object?

    /**
     * constructor of the class
     *
     * @param name name of the value
     * @param value value himself
     */
    constructor(name: String?, value: Object?) {
        this.name = KeyImpl.init(name)
        this.value = value
        names = null
    }

    constructor(name: Collection.Key?, value: Object?) {
        this.name = name
        this.value = value
        names = null
    }

    constructor(names: Array<String?>?, value: Object?) {
        this.names = names
        this.value = value
        name = null
    }

    @Override
    fun getName(): String? {
        return getNameAsString()
    }

    @Override
    fun getNameAsString(): String? {
        return if (name == null) {
            ListUtil.arrayToList(names, ".")
        } else name.getString()
    }

    @Override
    fun getNameAsKey(): Collection.Key? {
        return if (name == null) {
            KeyImpl.init(ListUtil.arrayToList(names, "."))
        } else name
    }

    fun getNames(): Array<String?>? {
        return names
    }

    @Override
    fun getValue(): Object? {
        return value
    }

    @Override
    @Throws(PageException::class)
    fun castToString(): String? {
        return Caster.toString(value)
    }

    @Override
    fun castToString(defaultValue: String?): String? {
        return Caster.toString(value, defaultValue)
    }

    @Override
    @Throws(PageException::class)
    fun castToBooleanValue(): Boolean {
        return Caster.toBooleanValue(value)
    }

    @Override
    fun castToBoolean(defaultValue: Boolean?): Boolean? {
        return Caster.toBoolean(value, defaultValue)
    }

    @Override
    @Throws(PageException::class)
    fun castToDoubleValue(): Double {
        return Caster.toDoubleValue(value)
    }

    @Override
    fun castToDoubleValue(defaultValue: Double): Double {
        return Caster.toDoubleValue(value, true, defaultValue)
    }

    @Override
    @Throws(PageException::class)
    fun castToDateTime(): DateTime? {
        return DateCaster.toDateSimple(value, DateCaster.CONVERTING_TYPE_OFFSET, true, null)
    }

    @Override
    fun castToDateTime(defaultValue: DateTime?): DateTime? {
        return DateCaster.toDateSimple(value, DateCaster.CONVERTING_TYPE_OFFSET, true, null, defaultValue)
    }

    @Override
    @Throws(PageException::class)
    operator fun compareTo(b: Boolean): Int {
        return OpUtil.compare(ThreadLocalPageContext.get(), value, if (b) Boolean.TRUE else Boolean.FALSE)
    }

    @Override
    @Throws(PageException::class)
    operator fun compareTo(dt: DateTime?): Int {
        return OpUtil.compare(ThreadLocalPageContext.get(), value, dt as Date?)
    }

    @Override
    @Throws(PageException::class)
    operator fun compareTo(d: Double): Int {
        return OpUtil.compare(ThreadLocalPageContext.get(), value, Double.valueOf(d))
    }

    @Override
    @Throws(PageException::class)
    operator fun compareTo(str: String?): Int {
        return OpUtil.compare(ThreadLocalPageContext.get(), value, str)
    }

    @Override
    fun toDumpData(pageContext: PageContext?, maxlevel: Int, properties: DumpProperties?): DumpData? {
        return DumpUtil.toDumpData(value, pageContext, maxlevel, properties)
    }

    @Override
    override fun toString(): String {
        return name.toString() + ":" + value
    }

    companion object {
        /**
         * @param name
         * @param value
         * @return
         */
        fun newInstance(name: String?, value: Object?): FunctionValue? {
            return FunctionValueImpl(name, value)
        }

        fun newInstance(name: Array<String?>?, value: Object?): FunctionValue? {
            return FunctionValueImpl(name, value)
        }

        fun newInstance(name: Collection.Key?, value: Object?): FunctionValue? {
            return FunctionValueImpl(name, value)
        }

        fun toStruct(fv1: FunctionValueImpl?): Struct? {
            val sct = StructImpl(Struct.TYPE_LINKED)
            sct!!.setEL(fv1!!.getNameAsKey(), fv1)
            return sct
        }

        fun toStruct(fv1: FunctionValueImpl?, fv2: FunctionValueImpl?): Struct? {
            val sct = StructImpl(Struct.TYPE_LINKED)
            sct!!.setEL(fv1!!.getNameAsKey(), fv1)
            sct!!.setEL(fv2!!.getNameAsKey(), fv2)
            return sct
        }

        fun toStruct(fv1: FunctionValueImpl?, fv2: FunctionValueImpl?, fv3: FunctionValueImpl?): Struct? {
            val sct = StructImpl(Struct.TYPE_LINKED)
            sct!!.setEL(fv1!!.getNameAsKey(), fv1)
            sct!!.setEL(fv2!!.getNameAsKey(), fv2)
            sct!!.setEL(fv3!!.getNameAsKey(), fv3)
            return sct
        }

        fun toStruct(fv1: FunctionValueImpl?, fv2: FunctionValueImpl?, fv3: FunctionValueImpl?, fv4: FunctionValueImpl?): Struct? {
            val sct = StructImpl(Struct.TYPE_LINKED)
            sct!!.setEL(fv1!!.getNameAsKey(), fv1)
            sct!!.setEL(fv2!!.getNameAsKey(), fv2)
            sct!!.setEL(fv3!!.getNameAsKey(), fv3)
            sct!!.setEL(fv4!!.getNameAsKey(), fv4)
            return sct
        }
    }
}