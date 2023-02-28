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
package lucee.intergral.fusiondebug.server.type

import java.util.List

abstract class FDNodeValueSupport(frame: IFDStackFrame?) : FDValueSupport() {
    private val frame: IFDStackFrame?
    @Override
    fun getChildren(): List? {
        return getChildren(frame, getName(), getRawValue())
    }

    /*
	 * public IFDValue getValue() { Object value = getRawValue(); if(isSimpleValue(value)) return
	 * getFDNodeVariableSupport(); return FDCaster.toFDVariable(getName(), value).getValue(); }
	 */
    @Override
    override fun toString(): String {
        val raw: Object? = getRawValue()
        return if (raw is UDF) FDUDF.toString(raw as UDF?) else FDCaster.serialize(raw)
    }

    @Override
    fun hasChildren(): Boolean {
        return hasChildren(getRawValue())
    }

    protected abstract fun getRawValue(): Object? // protected abstract FDNodeValueSupport getFDNodeVariableSupport();

    init {
        this.frame = frame
    }
}