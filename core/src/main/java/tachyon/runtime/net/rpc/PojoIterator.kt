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
package tachyon.runtime.net.rpc

import java.lang.reflect.Method

class PojoIterator(pojo: Pojo?) : Iterator<Pair<Collection.Key?, Object?>?> {
    private val pojo: Pojo?
    private val getters: Array<Method?>?
    private val clazz: Class<out Pojo?>?
    private var index = -1
    fun size(): Int {
        return getters!!.size
    }

    @Override
    override fun hasNext(): Boolean {
        return index + 1 < getters!!.size
    }

    @Override
    override fun next(): Pair<Collection.Key?, Object?>? {
        val g: Method? = getters!![++index]
        return try {
            Pair<Collection.Key?, Object?>(KeyImpl.init(g.getName().substring(3)), g.invoke(pojo, EMPTY_ARG))
        } catch (t: Throwable) {
            ExceptionUtil.rethrowIfNecessary(t)
            throw PageRuntimeException(Caster.toPageException(t))
        }
    }

    @Override
    fun remove() {
        throw RuntimeException("method remove is not supported!")
    }

    companion object {
        private val EMPTY_ARG: Array<Object?>? = arrayOf<Object?>()
    }

    init {
        this.pojo = pojo
        clazz = pojo.getClass()
        getters = Reflector.getGetters(pojo.getClass())
    }
}