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
package lucee.intergral.fusiondebug.server.util

import com.intergral.fusiondebug.server.IFDStackFrame

object FDCaster {
    fun toFDValue(frame: IFDStackFrame?, name: String?, value: Object?): IFDValue? {
        if (value is UDF) return FDUDF(frame, name, value as UDF?)
        if (value is Query) return FDQuery(frame, value as Query?)
        // if(value instanceof Array)
        // return new FDArray(frame,name,(Array)value);
        if (value is Collection) return FDCollection(frame, name, value as Collection?)
        return if (Decision.isCastableToString(value)) FDSimpleValue(null, Caster.toString(value, null)) else FDNative(frame, name, value)
    }

    fun toFDValue(frame: IFDStackFrame?, value: Object?): IFDValue? {
        return toFDValue(frame, "", value)
    }

    /**
     * translate an object to its string representation
     *
     * @param object
     * @return
     */
    fun serialize(`object`: Object?): String? {
        return if (`object` == null) "[null]" else try {
            ScriptConverter().serialize(`object`)
        } catch (t: Throwable) {
            ExceptionUtil.rethrowIfNecessary(t)
            `object`.toString()
        }
    }

    fun unserialize(value: String?): Object? {
        // TODO
        return value
    }
}