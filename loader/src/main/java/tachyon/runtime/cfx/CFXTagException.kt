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
package tachyon.runtime.cfx

import kotlin.Throws
import tachyon.loader.util.Util
import kotlin.jvm.Synchronized
import kotlin.jvm.Transient
import org.osgi.framework.Version

/**
 * Custom Tag Exception
 */
class CFXTagException : Exception {
    /**
     * Constructor of the Exception
     *
     * @param message exception message
     */
    constructor(message: String?) : super(message) {}

    /**
     * Constructor of the Exception
     *
     * @param e exception
     */
    constructor(e: Throwable) : super(e.getClass().getName().toString() + ":" + e.getMessage()) {}

    companion object {
        private const val serialVersionUID = 680174337882347105L
    }
}