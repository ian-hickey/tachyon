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
package lucee.intergral.fusiondebug.server.type.simple

import java.util.List

class FDSimpleVariable(frame: IFDStackFrame?, name: String?, value: IFDValue?) : IFDVariable {
    private val name: String?
    private val value: IFDValue?
    private val frame: IFDStackFrame?

    /**
     * Constructor of the class
     *
     * @param name
     * @param value
     * @param children
     */
    constructor(frame: IFDStackFrame?, name: String?, value: String?, children: List?) : this(frame, name, FDSimpleValue(children, value)) {}

    @Override
    fun getName(): String? {
        return name
    }

    @Override
    fun getValue(): IFDValue? {
        return value
    }

    @Override
    fun getStackFrame(): IFDStackFrame? {
        return frame
    }

    /**
     * Constructor of the class
     *
     * @param frame
     * @param name
     * @param value
     * @param children
     */
    init {
        this.frame = frame
        this.name = name
        this.value = value
    }
}