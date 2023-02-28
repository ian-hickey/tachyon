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
package coldfusion.server

import java.util.Map

interface RegistryService : Service {
    operator fun set(arg0: String?, arg1: String?, arg2: String?, arg3: String?)
    fun getAll(arg0: String?, arg1: String?): Map?
    operator fun get(arg0: String?, arg1: String?, arg2: String?): Object?

    // public abstract void delete(String arg0, String arg1) throws RegistryException;
    fun flush()
    fun clearDirtyBit()
    fun dump(): Map?
    fun isCrossPlatform(): Boolean
}