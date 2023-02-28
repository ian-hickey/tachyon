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
package tachyon.runtime.instrumentation

import java.io.IOException

object Agent {
    private var instrumentation: Instrumentation? = null
    fun premain(agentArgs: String?, inst: Instrumentation?) {
        if (inst != null) instrumentation = inst
    }

    fun agentmain(agentArgs: String?, inst: Instrumentation?) {
        if (inst != null) instrumentation = inst
    }

    @Throws(IOException::class)
    fun getInstrumentation(): Instrumentation? {
        if (instrumentation == null) throw IOException("There is no Instrumentation class available")
        return instrumentation
    }

    fun getInstrumentation(defaultValue: Instrumentation?): Instrumentation? {
        return if (instrumentation == null) defaultValue else instrumentation
    }
}