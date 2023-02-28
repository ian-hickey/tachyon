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
package lucee.runtime.query.caster

import java.sql.ResultSet

class OtherCast(private val type: Int) : Cast {
    @Override
    @Throws(SQLException::class)
    override fun toCFType(tz: TimeZone?, rst: ResultSet?, columnIndex: Int): Object? {
        return if (type != Types.SMALLINT) {
            val value: Object = rst.getObject(columnIndex)

            // Drivers like Postgres like to return java.util.UUID instances instead of the string GUID 
            if (value is UUID) {
                return (value as UUID).toString()
            }

            // Drivers like Postgres have a custom type that returns java.net.InetAddress 
            if (value is InetAddress) {
                (value as InetAddress).toString()
            } else value
        } else {
            try {
                rst.getObject(columnIndex)
            } // workaround for MSSQL Driver, in some situation getObject throws a cast exception using getString
            // avoids this
            catch (e: SQLException) {
                try {
                    rst.getString(columnIndex)
                } catch (e2: SQLException) {
                    throw e
                }
            }
        }
    }
}