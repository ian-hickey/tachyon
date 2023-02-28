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
package tachyon.runtime.exp

import tachyon.runtime.config.Config

/**
 *
 *
 * To change the template for this generated type comment go to Window>Preferences>Java>Code
 * Generation>Code and Comments
 */
class ExpressionException : PageExceptionImpl {
    /**
     * Class Constuctor
     *
     * @param message error message
     */
    constructor(message: String?) : super(message, "expression") {}

    /**
     * Class Constuctor
     *
     * @param message error message
     * @param detail detailed error message
     */
    constructor(message: String?, detail: String?) : super(message, "expression") {
        setDetail(detail)
    }

    @Override
    override fun getCatchBlock(config: Config?): CatchBlock? {
        val sct: CatchBlock = super.getCatchBlock(config)
        sct.setEL(ERR_NUMBER, Double.valueOf(0))
        return sct
    }

    companion object {
        private val ERR_NUMBER: Collection.Key? = KeyImpl.getInstance("ErrNumber")

        /**
         * @param e
         * @return pageException
         */
        fun newInstance(e: Exception?): ExpressionException? {
            return if (e is ExpressionException) e else if (e is PageException) {
                val pe: PageException? = e as PageException?
                val ee = ExpressionException(pe.getMessage())
                ee.detail = pe.getDetail()
                ee.setStackTrace(pe.getStackTrace())
                ee
            } else {
                val ee = ExpressionException(e.getMessage())
                ee.setStackTrace(e.getStackTrace())
                ee
            }
        }
    }
}