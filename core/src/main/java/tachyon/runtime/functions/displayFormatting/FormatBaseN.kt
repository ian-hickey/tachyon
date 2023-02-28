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
 * Implements the CFML Function formatbasen
 */
package tachyon.runtime.functions.displayFormatting

import tachyon.runtime.PageContext

object FormatBaseN : Function {
    @Throws(ExpressionException::class)
    fun call(pc: PageContext?, number: Double, radix: Double): String? {
        if (radix < 2 || radix > 36) throw FunctionException(pc, "formatBaseN", 2, "radix", "radix must be between 2 an 36")
        return toString(Caster.toLongValue(number), radix.toInt())
    }
}