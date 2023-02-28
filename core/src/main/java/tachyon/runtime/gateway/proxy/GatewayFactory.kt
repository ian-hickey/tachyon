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
package tachyon.runtime.gateway.proxy

import tachyon.runtime.exp.ApplicationException

// FUTURE remove this class
object GatewayFactory {
    @Throws(ApplicationException::class)
    fun toGateway(obj: Object?): Gateway? {
        if (obj is Gateway) return obj as Gateway?
        throw ApplicationException("the class [" + obj.getClass().getName().toString() + "] does not implement the interface [" + Gateway::class.java.getName()
                .toString() + "], make sure you have not multiple implementation of that interface in your classpath")
    }
}