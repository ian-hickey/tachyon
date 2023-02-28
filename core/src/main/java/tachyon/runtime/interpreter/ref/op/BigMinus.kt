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
package tachyon.runtime.interpreter.ref.op

import tachyon.commons.math.MathUtil

/**
 * Minus operation
 */
class BigMinus
/**
 * constructor of the class
 *
 * @param left
 * @param right
 */
(left: Ref?, right: Ref?, limited: Boolean) : Big(left, right, limited) {
    @Override
    @Throws(PageException::class)
    fun getValue(pc: PageContext?): Object? {
        if (limited) throw InterpreterException("invalid syntax, math operations are not supported in a json string.")
        return MathUtil.subtract(getLeft(pc), getRight(pc)).toString()
    }
}