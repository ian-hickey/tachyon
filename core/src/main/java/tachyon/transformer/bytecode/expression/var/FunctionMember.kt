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
package tachyon.transformer.bytecode.expression.`var`

import tachyon.transformer.expression.Expression

abstract class FunctionMember : Member, Func {
    private var arguments: Array<Argument?>? = arrayOfNulls<Argument?>(0)
    private var _hasNamedArgs = false
    private var parent: Variable? = null
    private var safeNavigated = false
    private var safeNavigatedValue: Expression? = null
    @Override
    fun setParent(parent: Variable?) {
        this.parent = parent
    }

    @Override
    fun getParent(): Variable? {
        return parent
    }

    @Override
    override fun addArgument(argument: Argument?) {
        if (argument is NamedArgument) _hasNamedArgs = true
        val tmp: Array<Argument?> = arrayOfNulls<Argument?>(arguments!!.size + 1)
        for (i in arguments.indices) {
            tmp[i] = arguments!![i]
        }
        tmp[arguments!!.size] = argument
        arguments = tmp
    }

    /**
     * @return the arguments
     */
    fun getArguments(): Array<Argument?>? {
        return arguments
    }

    fun setArguments(arguments: Array<Argument?>?) {
        this.arguments = arguments
    }

    fun hasNamedArgs(): Boolean {
        return _hasNamedArgs
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
}