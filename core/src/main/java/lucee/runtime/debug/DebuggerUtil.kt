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
package lucee.runtime.debug

import java.util.HashSet

class DebuggerUtil {
    fun pointOutClosuresInPersistentScopes(pc: PageContext?): Struct? {
        val sct: Struct = StructImpl()
        val done: Set<Object?> = HashSet<Object?>()
        // Application Scope
        try {
            sct.set(KeyConstants._application, _pointOutClosuresInPersistentScopes(pc, pc.applicationScope(), done))
        } catch (e: PageException) {
        }

        // Session Scope
        try {
            sct.set(KeyConstants._application, _pointOutClosuresInPersistentScopes(pc, pc.sessionScope(), done))
        } catch (e: PageException) {
        }

        // Server Scope
        try {
            sct.set(KeyConstants._application, _pointOutClosuresInPersistentScopes(pc, pc.serverScope(), done))
        } catch (e: PageException) {
        }
        return null
    }

    private fun _pointOutClosuresInPersistentScopes(pc: PageContext?, sct: Struct?, done: Set<Object?>?): Struct? {
        return null
    }

    companion object {
        fun debugQueryUsage(pageContext: PageContext?, query: Query?): Boolean {
            if (pageContext.getConfig().debug() && query is Query) {
                if ((pageContext.getConfig() as ConfigWebPro).hasDebugOptions(ConfigPro.DEBUG_QUERY_USAGE)) {
                    query.enableShowQueryUsage()
                    return true
                }
            }
            return false
        }
    }
}