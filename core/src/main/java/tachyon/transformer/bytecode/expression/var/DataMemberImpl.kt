/**
 * Copyright (c) 2015, Tachyon Assosication Switzerland. All rights reserved.
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
package tachyon.transformer.bytecode.expression.`var`

import tachyon.transformer.expression.ExprString

class DataMemberImpl(name: ExprString?) : DataMember {
    private val name: ExprString?
    private var parent: Variable? = null
    private var safeNavigated = false
    private var safeNavigatedValue: Expression? = null
    fun setParent(parent: Variable?) {
        this.parent = parent
    }

    fun getParent(): Variable? {
        return parent
    }

    @Override
    fun getName(): ExprString? {
        return name
    }

    @Override
    fun setSafeNavigated(safeNavigated: Boolean) {
        this.safeNavigated = safeNavigated
    }

    @Override
    fun getSafeNavigated(): Boolean {
        return safeNavigated
    }

    @Override
    fun setSafeNavigatedValue(safeNavigatedValue: Expression?) {
        this.safeNavigatedValue = safeNavigatedValue
    }

    @Override
    fun getSafeNavigatedValue(): Expression? {
        return safeNavigatedValue
    }

    init {
        this.name = name
    }
}