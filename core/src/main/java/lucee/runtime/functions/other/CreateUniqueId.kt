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
package lucee.runtime.functions.other

import lucee.runtime.PageContext

class CreateUniqueId : BIF() {
    @Override
    @Throws(PageException::class)
    operator fun invoke(pc: PageContext?, args: Array<Object?>?): Object? {
        if (args!!.size == 0) return invoke()
        if (args.size == 1) return call(pc, args[0] as String?)
        throw FunctionException(pc, CreateUniqueId::class.java.getSimpleName(), 0, 1, args.size)
    }

    companion object {
        private val counter: AtomicLong? = AtomicLong(0)

        /**
         * method to invoke the function
         *
         * @param pc
         * @return UUID String
         */
        fun call(pc: PageContext?): String? {
            return Base64Util.createUuidAsBase64()
        }

        fun call(pc: PageContext?, type: String?): String? {
            return if ("counter".equalsIgnoreCase(type)) invoke() else Base64Util.createUuidAsBase64()
        }

        operator fun invoke(): String? {
            val value: Long = counter.incrementAndGet()
            if (value < 0) counter.set(1)
            return toString(value, Character.MAX_RADIX)
        }
    }
}