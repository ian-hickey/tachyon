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
package tachyon.runtime.engine

import tachyon.runtime.PageSource

/**
 * this class is just used to make the pagesource availble for old code in ra files
 */
object ThreadLocalPageSource {
    private val local: ThreadLocal<PageSource?>? = ThreadLocal<PageSource?>()

    /**
     * register a Config for he current thread
     *
     * @param config Config to register
     */
    fun register(ps: PageSource?) {
        local.set(ps)
    }

    /**
     * returns Config registered for the current thread
     *
     * @return Config for the current thread or null
     */
    fun get(): PageSource? {
        return local.get()
    }

    /**
     * release the pagecontext for the current thread
     */
    fun release() {
        local.set(null)
    }
}