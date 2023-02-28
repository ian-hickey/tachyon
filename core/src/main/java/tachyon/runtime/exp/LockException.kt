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
 */
class LockException : PageExceptionImpl {
    private var lockName: String? = ""
    private var lockOperation: String? = "Unknown"

    /**
     * Class Constuctor
     *
     * @param operation
     * @param name
     * @param message error message
     */
    constructor(operation: String?, name: String?, message: String?) : super(message, "lock") {
        lockName = name
        lockOperation = operation
    }

    /**
     * Class Constuctor
     *
     * @param operation
     * @param name
     * @param message error message
     * @param detail detailed error message
     */
    constructor(operation: String?, name: String?, message: String?, detail: String?) : super(message, "lock") {
        lockName = name
        lockOperation = operation
        setDetail(detail)
    }

    @Override
    override fun getCatchBlock(config: Config?): CatchBlock? {
        val sct: CatchBlock = super.getCatchBlock(config)
        sct.setEL("LockName", lockName)
        sct.setEL("LockOperation", lockOperation)
        return sct
    }

    companion object {
        /**
         * Field `OPERATION_TIMEOUT`
         */
        val OPERATION_TIMEOUT: String? = "Timeout"

        /**
         * Field `OPERATION_MUTEX`
         */
        val OPERATION_MUTEX: String? = "Mutex"

        /**
         * Field `OPERATION_CREATE`
         */
        val OPERATION_CREATE: String? = "Create"

        /**
         * Field `OPERATION_UNKNOW`
         */
        val OPERATION_UNKNOW: String? = "Unknown"
    }
}