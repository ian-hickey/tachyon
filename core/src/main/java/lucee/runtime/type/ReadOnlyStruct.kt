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
package lucee.runtime.type

import lucee.runtime.exp.ExpressionException

/**
 * a read only Struct if flag is set to readonly
 */
class ReadOnlyStruct : StructImpl() {
    private var isReadOnly = false

    /**
     * sets if scope is readonly or not
     *
     * @param isReadOnly
     */
    fun setReadOnly(isReadOnly: Boolean) {
        this.isReadOnly = isReadOnly
    }

    @Override
    @Throws(PageException::class)
    override fun remove(key: Collection.Key?): Object? {
        if (isReadOnly) throw ExpressionException("can't remove key [" + key.getString().toString() + "] from struct, struct is readonly")
        return super.remove(key)
    }

    @Override
    override fun removeEL(key: Collection.Key?): Object? {
        return if (isReadOnly) null else super.removeEL(key)
    }

    fun removeAll() {
        if (!isReadOnly) super.clear()
    }

    @Override
    @Throws(PageException::class)
    override operator fun set(key: Collection.Key?, value: Object?): Object? {
        if (isReadOnly) throw ExpressionException("can't set key [" + key.getString().toString() + "] to struct, struct is readonly")
        return super.set(key, value)
    }

    @Override
    override fun setEL(key: Collection.Key?, value: Object?): Object? {
        if (!isReadOnly) super.setEL(key, value)
        return value
    }

    @Override
    override fun duplicate(deepCopy: Boolean): Collection? {
        val trg: Struct = StructImpl()
        // trg.isReadOnly=isReadOnly;
        copy(this, trg, deepCopy)
        return trg
    }

    @Override
    override fun clear() {
        if (isReadOnly) throw PageRuntimeException(ExpressionException("can't clear struct, struct is readonly"))
        super.clear()
    }
}