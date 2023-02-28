/**
 * Copyright (c) 2014, the Railo Company Ltd.
 * Copyright (c) 2015, Lucee Assosication Switzerland
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
package lucee.runtime.interpreter.ref.`var`

import lucee.runtime.PageContext

/**
 *
 */
class Variable : RefSupport, Set {
    private var key: String? = null
    private var parent: Ref?
    private var refKey: Ref? = null
    private var limited: Boolean

    /**
     * @param pc
     * @param parent
     * @param key
     */
    constructor(parent: Ref?, key: String?, limited: Boolean) {
        this.parent = parent
        this.key = key
        this.limited = limited
    }

    /**
     * @param pc
     * @param parent
     * @param refKey
     */
    constructor(parent: Ref?, refKey: Ref?, limited: Boolean) {
        this.parent = parent
        this.refKey = refKey
        this.limited = limited
    }

    @Override
    @Throws(PageException::class)
    fun getValue(pc: PageContext?): Object? {
        if (limited) throw InterpreterException("invalid syntax, variables are not supported in a json string.")
        return pc.get(parent.getCollection(pc), KeyImpl.init(getKeyAsString(pc)))
    }

    @Override
    @Throws(PageException::class)
    fun touchValue(pc: PageContext?): Object? {
        if (limited) throw InterpreterException("invalid syntax, variables are not supported in a json string.")
        val p: Object = parent.touchValue(pc)
        if (p is Query) {
            val o: Object = (p as Query).getColumn(KeyImpl.init(getKeyAsString(pc)), null)
            return if (o != null) o else setValue(pc, StructImpl())
        }
        return pc.touch(p, KeyImpl.init(getKeyAsString(pc)))
    }

    @Override
    @Throws(PageException::class)
    fun getCollection(pc: PageContext?): Object? {
        if (limited) throw InterpreterException("invalid syntax, variables are not supported in a json string.")
        val p: Object = parent.getValue(pc)
        return if (p is Query) {
            (p as Query).getColumn(KeyImpl.init(getKeyAsString(pc)))
        } else pc.get(p, KeyImpl.init(getKeyAsString(pc)))
    }

    @Override
    @Throws(PageException::class)
    fun setValue(pc: PageContext?, obj: Object?): Object? {
        if (limited) throw InterpreterException("invalid syntax, variables are not supported in a json string.")
        return pc.set(parent.touchValue(pc), KeyImpl.init(getKeyAsString(pc)), obj)
    }

    @Override
    fun getTypeName(): String? {
        return "variable"
    }

    @Override
    @Throws(PageException::class)
    fun getKey(pc: PageContext?): Ref? {
        return if (key == null) refKey else LString(key)
    }

    @Override
    @Throws(PageException::class)
    fun getKeyAsString(pc: PageContext?): String? {
        if (key == null) key = Caster.toString(refKey.getValue(pc))
        return key
    }

    @Override
    @Throws(PageException::class)
    fun getParent(pc: PageContext?): Ref? {
        return parent
    }
}