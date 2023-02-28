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
package tachyon.runtime.interpreter.ref.`var`

import tachyon.runtime.PageContext

/**
 *
 */
class Scope
/**
 * contructor of the class
 *
 * @param pc
 * @param scope
 */(private val scope: Int) : RefSupport(), Set {
    @Override
    @Throws(PageException::class)
    fun getValue(pc: PageContext?): Object? {
        return VariableInterpreter.scope(pc, scope, false)
    }

    @Override
    fun getTypeName(): String? {
        return "scope"
    }

    @Override
    @Throws(PageException::class)
    fun touchValue(pc: PageContext?): Object? {
        return VariableInterpreter.scope(pc, scope, true)
    }

    @Override
    @Throws(PageException::class)
    fun setValue(pc: PageContext?, obj: Object?): Object? {
        return pc.undefinedScope().set(getKeyAsString(pc), obj)
    }

    /**
     * @return scope
     */
    fun getScope(): Int {
        return scope
    }

    @Override
    @Throws(PageException::class)
    fun getParent(pc: PageContext?): Ref? {
        return null
    }

    @Override
    @Throws(PageException::class)
    fun getKey(pc: PageContext?): Ref? {
        return LString(getKeyAsString(pc))
    }

    @Override
    @Throws(PageException::class)
    fun getKeyAsString(pc: PageContext?): String? {
        // return ScopeFactory.toStringScope(scope,null);
        return VariableInterpreter.scopeInt2String(scope)
    }
}