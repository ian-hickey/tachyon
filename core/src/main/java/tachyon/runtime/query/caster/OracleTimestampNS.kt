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
package tachyon.runtime.query.caster

import java.io.IOException

class OracleTimestampNS : Cast {
    @Override
    @Throws(SQLException::class, IOException::class)
    override fun toCFType(tz: TimeZone?, rst: ResultSet?, columnIndex: Int): Object? {
        val o: Object = rst.getObject(columnIndex) ?: return null

        // we do not have oracle.sql.TIMESTAMPTZ in the core, so we need reflection for this
        return try {
            val ts: Timestamp = Reflector.callMethod(o, "timestampValue", arrayOf<Object?>()) as Timestamp
            DateTimeImpl(ts.getTime(), false)
        } catch (pe: PageException) {
            throw ExceptionUtil.toIOException(pe)
        }
    }
}