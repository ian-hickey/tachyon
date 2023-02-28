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
package tachyon.runtime.type.trace

import java.util.Date

class TOObjects(debugger: Debugger?, obj: Object?, type: Int, category: String?, text: String?) : TraceObjectSupport(debugger, obj, type, category, text), Objects {
    @Override
    fun toDumpData(pageContext: PageContext?, maxlevel: Int, properties: DumpProperties?): DumpData? {
        log()
        return DumpUtil.toDumpData(o, pageContext, maxlevel, properties)
    }

    @Override
    @Throws(PageException::class)
    fun castToString(): String? {
        log()
        return Caster.toString(o)
    }

    @Override
    fun castToString(defaultValue: String?): String? {
        log()
        return Caster.toString(o, defaultValue)
    }

    @Override
    @Throws(PageException::class)
    fun castToBooleanValue(): Boolean {
        log()
        return Caster.toBooleanValue(o)
    }

    @Override
    fun castToBoolean(defaultValue: Boolean?): Boolean? {
        log()
        return Caster.toBoolean(o, defaultValue)
    }

    @Override
    @Throws(PageException::class)
    fun castToDoubleValue(): Double {
        log()
        return Caster.toDoubleValue(o)
    }

    @Override
    fun castToDoubleValue(defaultValue: Double): Double {
        log()
        return Caster.toDoubleValue(o, true, defaultValue)
    }

    @Override
    @Throws(PageException::class)
    fun castToDateTime(): DateTime? {
        log()
        return TODateTime(debugger, Caster.toDate(o, false, null), type, category, text)
    }

    @Override
    fun castToDateTime(defaultValue: DateTime?): DateTime? {
        log()
        return TODateTime(debugger, Caster.toDate(o, false, null, defaultValue), type, category, text)
    }

    @Override
    @Throws(PageException::class)
    operator fun compareTo(b: Boolean): Int {
        log()
        return OpUtil.compare(ThreadLocalPageContext.get(), o, if (b) Boolean.TRUE else Boolean.FALSE)
    }

    @Override
    @Throws(PageException::class)
    operator fun compareTo(dt: DateTime?): Int {
        log()
        return OpUtil.compare(ThreadLocalPageContext.get(), o, dt as Date?)
    }

    @Override
    @Throws(PageException::class)
    operator fun compareTo(d: Double): Int {
        log()
        return OpUtil.compare(ThreadLocalPageContext.get(), o, Double.valueOf(d))
    }

    @Override
    @Throws(PageException::class)
    operator fun compareTo(str: String?): Int {
        log()
        return OpUtil.compare(ThreadLocalPageContext.get(), o, str)
    }

    @Override
    @Throws(PageException::class)
    operator fun get(pc: PageContext?, key: Key?): Object? {
        log(key.getString())
        val `var`: VariableUtilImpl = pc.getVariableUtil() as VariableUtilImpl
        return `var`.get(pc, o, key)
        // return TraceObjectSupport.toTraceObject(debugger,var.get(pc, o, key),type,category,text);
    }

    @Override
    operator fun get(pc: PageContext?, key: Key?, defaultValue: Object?): Object? {
        log(key.getString())
        val `var`: VariableUtilImpl = pc.getVariableUtil() as VariableUtilImpl
        return `var`.get(pc, o, key, defaultValue)
        // return TraceObjectSupport.toTraceObject(debugger,var.get(pc, o, key,
        // defaultValue),type,category,text);
    }

    @Override
    @Throws(PageException::class)
    operator fun set(pc: PageContext?, key: Key?, value: Object?): Object? {
        log(key, value)
        val `var`: VariableUtilImpl = pc.getVariableUtil() as VariableUtilImpl
        return `var`.set(pc, o, key, value)
        // return TraceObjectSupport.toTraceObject(debugger,var.set(pc, o, key, value),type,category,text);
    }

    @Override
    fun setEL(pc: PageContext?, key: Key?, value: Object?): Object? {
        log(key, value)
        val `var`: VariableUtilImpl = pc.getVariableUtil() as VariableUtilImpl
        return `var`.setEL(pc, o, key, value)
        // return TraceObjectSupport.toTraceObject(debugger,var.setEL(pc, o, key,
        // value),type,category,text);
    }

    @Override
    @Throws(PageException::class)
    fun call(pc: PageContext?, key: Key?, args: Array<Object?>?): Object? {
        log(key.getString())
        val `var`: VariableUtilImpl = pc.getVariableUtil() as VariableUtilImpl
        return `var`.callFunctionWithoutNamedValues(pc, o, key, args)
    }

    @Override
    @Throws(PageException::class)
    fun callWithNamedValues(pc: PageContext?, key: Key?, args: Struct?): Object? {
        log(key.getString())
        val `var`: VariableUtilImpl = pc.getVariableUtil() as VariableUtilImpl
        return `var`.callFunctionWithNamedValues(pc, o, key, args)
    }

    fun isInitalized(): Boolean {
        log()
        return true
    }

    companion object {
        private const val serialVersionUID = -2011026266467450312L
    }
}