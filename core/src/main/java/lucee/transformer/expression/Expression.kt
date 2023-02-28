/**
 * Copyright (c) 2015, Lucee Assosication Switzerland. All rights reserved.
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
package lucee.transformer.expression

import lucee.runtime.exp.TemplateException

/**
 * An Expression (Operation, Literal aso.)
 */
interface Expression {
    /**
     * write out the stament to adapter
     *
     * @param adapter
     * @param mode
     * @return return Type of expression
     * @throws TemplateException
     */
    @Throws(TransformerException::class)
    fun writeOut(bc: Context?, mode: Int): Class<*>?
    fun getStart(): Position?
    fun getEnd(): Position?
    fun setStart(start: Position?)
    fun setEnd(end: Position?)
    fun getFactory(): Factory?

    companion object {
        /**
         * Field `MODE_REF`
         */
        const val MODE_REF = 0

        /**
         * Field `MODE_VALUE`
         */
        const val MODE_VALUE = 1
    }
}