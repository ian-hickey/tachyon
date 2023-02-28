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

import com.intergral.fusiondebug.server.IFDStackFrame

class FDVariable(frame: IFDStackFrame?, name: Collection.Key?, value: IFDValue?) : IFDVariable {
    private val name: Collection.Key? = null
    private val value: IFDValue? = null
    private val frame: IFDStackFrame? = null

    constructor(frame: IFDStackFrame?, name: String?, value: IFDValue?) : this(frame, KeyImpl.getInstance(name), value) {}

    @Override
    fun getName(): String? {
        return name.getString()
    }

    @Override
    fun getStackFrame(): IFDStackFrame? {
        return frame
    }

    @Override
    fun getValue(): IFDValue? {
        return value
    }

    /**
     * Constructor of the class
     *
     * @param name
     * @param value
     * @param frame
     */
    init {
        this.name = name
        this.value = value
        this.frame = frame
    }
}