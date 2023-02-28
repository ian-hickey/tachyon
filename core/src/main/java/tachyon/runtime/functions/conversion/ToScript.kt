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
package tachyon.runtime.functions.conversion

import tachyon.runtime.PageContext

/**
 *
 */
object ToScript : Function {
    @Throws(PageException::class)
    fun call(pc: PageContext?, `var`: Object?, jsName: String?): String? {
        return call(pc, `var`, jsName, true, false)
    }

    @Throws(PageException::class)
    fun call(pc: PageContext?, `var`: Object?, jsName: String?, outputFormat: Boolean): String? {
        return call(pc, `var`, jsName, outputFormat, false)
    }

    @Throws(PageException::class)
    fun call(pc: PageContext?, `var`: Object?, jsName: String?, outputFormat: Boolean, asFormat: Boolean): String? {
        // if(!Decision.isVariableName(jsName))
        // throw new FunctionException(pc,"toScript",2,"jsName","value does not contain a valid variable
        // String");
        val converter = JSConverter()
        converter.useShortcuts(asFormat)
        converter.useWDDX(outputFormat)
        return try {
            converter.serialize(`var`, jsName)
        } catch (e: ConverterException) {
            throw Caster.toPageException(e)
        }
    }
}