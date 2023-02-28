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
package tachyon.runtime.exp

/**
 * Illegal QoQ Exception Object
 * This subtype of DatabaseException is to signal when a QoQ cannot
 * continue due to a fatal error and should NOT attempt to fall back to HSQLDB
 */
import java.sql.SQLException

class IllegalQoQException : DatabaseException {
    constructor(sqle: SQLException?, dc: DatasourceConnection?) : super(sqle, dc) {}
    constructor(message: String?, detail: String?, sql: SQL?, dc: DatasourceConnection?) : super(message, detail, sql, dc) {}
    constructor(e: PageException?, sql: SQL?, dc: DatasourceConnection?) : super(e.getMessage(), e.getDetail(), sql, dc) {}
}