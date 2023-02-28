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
/**
 * Implements the CFML Function getmetadata
 */
package lucee.runtime.functions.other

import java.lang.reflect.Method

object GetMetaData : Function {
    private const val serialVersionUID = -3787469574373656167L

    // TODO support enties more deeply
    @Throws(PageException::class)
    fun call(pc: PageContext?): Object? {
        val ac: Component = pc.getActiveComponent()
        return if (ac != null) {
            call(pc, ac)
        } else StructImpl()
    }

    @Throws(PageException::class)
    fun call(pc: PageContext?, `object`: Object?): Object? {
        return call(pc, `object`, false)
    }

    @Throws(PageException::class)
    fun call(pc: PageContext?, `object`: Object?, source: Boolean): Object? {
        if (`object` is JavaObject) {
            return call(pc, (`object` as JavaObject?).getClazz(), source)
        } else if (`object` is ObjectWrap) {
            return call(pc, (`object` as ObjectWrap?).getEmbededObject(), source)
        }
        if (!source) {
            // Component
            if (`object` is Component) {
                return getMetaData(`object` as Component?, pc)
                // return ((Component)object).getMetaData(pc);
            }
            // UDF
            if (`object` is UDF) {
                return (`object` as UDF?).getMetaData(pc)
            } else if (`object` is Query) {
                return (`object` as Query?).getMetaDataSimple()
            } else if (`object` is Array) {
                return ArrayUtil.getMetaData(`object` as Array?)
            } else if (`object` is Struct) {
                return StructUtil.getMetaData(`object` as Struct?)
            }

            // FUTURE add interface with getMetaData
            try {
                val m: Method = `object`.getClass().getMethod("info", arrayOf<Class?>())
                return m.invoke(`object`, arrayOf<Object?>())
            } catch (e: Exception) {
            }
            if (`object` == null) throw FunctionException(pc, "GetMetaData", 1, "object", "value is null")
            return `object`.getClass()
        }
        val str: String = Caster.toString(`object`, null)
                ?: throw FunctionException(pc, "GetMetaData", 1, "object", "must be a string when second argument is true")
        return pc.undefinedScope().getScope(KeyImpl.init(str))
    }

    @Throws(PageException::class)
    fun getMetaData(cfc: Component?, pc: PageContext?): Struct? {
        return cfc.getMetaData(pc)
    }
}