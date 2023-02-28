/**
 * Copyright (c) 2014, the Railo Company Ltd.
 * Copyright (c) 2015, Tachyon Assosication Switzerland
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
package tachyon.runtime.type.trace

import tachyon.runtime.PageContext

class TODateTime(debugger: Debugger?, dt: DateTime?, type: Int, category: String?, text: String?) : DateTime(), TraceObject {
    private val dt: DateTime?

    // private Debugger debugger;
    private val qry: Query? = QueryImpl(arrayOf<String?>("label", "action", "params", "template", "line", "time"), 0, "traceObjects")
    private val type: Int
    private val category: String?
    private val text: String?
    private val debugger: Debugger?
    @Override
    fun toDumpData(pageContext: PageContext?, maxlevel: Int, properties: DumpProperties?): DumpData? {
        log()
        return dt.toDumpData(pageContext, maxlevel, properties)
    }

    @Override
    @Throws(PageException::class)
    fun castToString(): String? {
        log()
        return dt.castToString()
    }

    @Override
    fun castToString(defaultValue: String?): String? {
        log()
        return dt.castToString(defaultValue)
    }

    @Override
    @Throws(PageException::class)
    fun castToBooleanValue(): Boolean {
        log()
        return dt.castToBooleanValue()
    }

    @Override
    fun castToBoolean(defaultValue: Boolean?): Boolean? {
        log()
        return dt.castToBoolean(defaultValue)
    }

    @Override
    @Throws(PageException::class)
    fun castToDoubleValue(): Double {
        log()
        return dt.castToDoubleValue()
    }

    @Override
    fun castToDoubleValue(defaultValue: Double): Double {
        log()
        return dt.castToDoubleValue(defaultValue)
    }

    @Override
    @Throws(PageException::class)
    fun castToDateTime(): DateTime? {
        log()
        return this
    }

    @Override
    fun castToDateTime(defaultValue: DateTime?): DateTime? {
        log()
        return this
    }

    @Override
    @Throws(PageException::class)
    operator fun compareTo(str: String?): Int {
        log()
        return dt.compareTo(str)
    }

    @Override
    @Throws(PageException::class)
    operator fun compareTo(b: Boolean): Int {
        log()
        return dt.compareTo(b)
    }

    @Override
    @Throws(PageException::class)
    operator fun compareTo(d: Double): Int {
        log()
        return dt.compareTo(d)
    }

    @Override
    @Throws(PageException::class)
    operator fun compareTo(dt: DateTime?): Int {
        log()
        return dt.compareTo(dt)
    }

    @Override
    fun toDoubleValue(): Double {
        log()
        return dt.toDoubleValue()
    }

    protected fun log() {
        TraceObjectSupport.log(debugger, type, category, text, null, null)
    }

    fun getDebugData(): Query? {
        return qry
    }

    init {
        this.dt = dt
        this.debugger = debugger
        this.type = type
        this.category = category
        this.text = text
    }
}