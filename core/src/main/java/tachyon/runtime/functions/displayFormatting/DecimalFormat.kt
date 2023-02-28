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
 * Implements the CFML Function decimalformat
 */
package tachyon.runtime.functions.displayFormatting

import tachyon.commons.lang.StringUtil

object DecimalFormat : Function {
    private const val serialVersionUID = -2287888250117784383L

    /*
	 * @param pc
	 * 
	 * @param object
	 * 
	 * @return
	 * 
	 * @throws ExpressionException
	 */
    @Throws(PageException::class)
    fun call(pc: PageContext?, `object`: Object?): String? {
        var `object`: Object? = `object`
        if (StringUtil.isEmpty(`object`)) `object` = Constants.DOUBLE_ZERO
        return Caster.toDecimal(`object`, true)
    }
}