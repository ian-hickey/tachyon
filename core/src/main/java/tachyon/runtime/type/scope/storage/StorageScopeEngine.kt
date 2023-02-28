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
package tachyon.runtime.type.scope.storage

import tachyon.commons.io.log.Log

class StorageScopeEngine(factory: CFMLFactoryImpl?, log: Log?, cleaners: Array<StorageScopeCleaner?>?) {
    private val cleaners: Array<StorageScopeCleaner?>?
    private val factory: CFMLFactoryImpl?
    private val log: Log?
    fun clean() {
        for (i in cleaners.indices) {
            cleaners!![i]!!.clean()
        }
    }

    /**
     * @return the factory
     */
    fun getFactory(): CFMLFactoryImpl? {
        return factory
    }

    /**
     * @return the log
     */
    fun _getLog(): Log? {
        return log
    }

    fun remove(type: Int, appName: String?, cfid: String?) {
        getFactory().getScopeContext().remove(type, appName, cfid)
    }

    init {
        this.cleaners = cleaners
        this.factory = factory
        this.log = log
        for (i in cleaners.indices) {
            cleaners!![i]!!.init(this)
        }
    }
}