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
package tachyon.commons.io.res.type.datasource.core

import java.sql.PreparedStatement

abstract class CoreSupport : Core {
    @Throws(SQLException::class)
    fun prepareStatement(dc: DatasourceConnection, sql: String?): PreparedStatement {
        return dc.getPreparedStatement(SQLImpl(sql), false, true)
    }

    companion object {
        val ATTR_ROOT: Attr = Attr(0, null, null, true, Attr.TYPE_DIRECTORY, 0, 0, 511.toShort(), 0.toShort(), 0)
        fun isDirectory(type: Int): Boolean {
            return type == Attr.TYPE_DIRECTORY
        }

        fun isFile(type: Int): Boolean {
            return type == Attr.TYPE_FILE
        }

        fun isLink(type: Int): Boolean {
            return type == Attr.TYPE_LINK
        }

        val calendar: Calendar
            get() = JREDateTimeUtil.getThreadCalendar(ThreadLocalPageContext.getTimeZone())

        fun log(s1: String?) {}
        fun log(s1: String?, s2: String?) {}
        fun log(s1: String?, s2: String?, s3: String?) {}
        fun log(s1: String?, s2: String?, s3: String?, s4: String?) {}
    }
}