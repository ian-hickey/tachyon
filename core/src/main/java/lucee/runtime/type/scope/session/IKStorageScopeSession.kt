/**
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
package lucee.runtime.type.scope.session

import java.util.Map

class IKStorageScopeSession(pc: PageContext?, handler: IKHandler?, appName: String?, name: String?, data: Map<Collection.Key?, IKStorageScopeItem?>?, lastModified: Long, timeSpan: Long) : IKStorageScopeSupport(pc, handler, appName, name, "session", SCOPE_SESSION, data, lastModified, timeSpan), Session {
    @Override
    fun duplicate(deepCopy: Boolean): Collection? {
        return StructUtil.duplicate(this, deepCopy)
    }

    companion object {
        private const val serialVersionUID = -875719423763891692L
    }
}