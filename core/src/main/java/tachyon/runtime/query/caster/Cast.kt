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
package tachyon.runtime.query.caster

import java.io.IOException

interface Cast {
    // public Object toCFType(TimeZone tz,int type,ResultSet rst, int columnIndex) throws SQLException,
    // IOException;
    @Throws(SQLException::class, IOException::class)
    fun toCFType(tz: TimeZone?, rst: ResultSet?, columnIndex: Int): Object?

    companion object {
        val ARRAY: Cast? = ArrayCast()
        val BIT: Cast? = BitCast()
        val BLOB: Cast? = BlobCast()
        val CLOB: Cast? = ClobCast()
        val DATE: Cast? = DateCast(true)
        val ORACLE_OPAQUE: Cast? = OracleOpaqueCast()

        // public static final Cast OTHER=new OtherCast();
        val TIME: Cast? = TimeCast(true)
        val TIMESTAMP: Cast? = TimestampCast(true)
        val BIGINT: Cast? = BigIntCast()
        val TIME_NOTZ: Cast? = TimeCast(false)
        val TIMESTAMP_NOTZ: Cast? = TimestampCast(false)
        val DATE_NOTZ: Cast? = DateCast(false)
        val SQLXML: Cast? = SQLXMLCast()
        val ORACLE_BLOB: Cast? = OracleBlobCast()
        val ORACLE_CLOB: Cast? = OracleClobCast()
        val ORACLE_NCLOB: Cast? = OracleNClobCast()
        val ORACLE_TIMESTAMPTZ: Cast? = OracleTimestampTZ()
        val ORACLE_TIMESTAMPLTZ: Cast? = OracleTimestampLTZ()
        val ORACLE_TIMESTAMPNS: Cast? = OracleTimestampNS()
    }
}