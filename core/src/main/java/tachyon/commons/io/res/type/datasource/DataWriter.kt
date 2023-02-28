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
package tachyon.commons.io.res.type.datasource

import java.io.InputStream

class DataWriter(core: Core, dc: DatasourceConnection?, prefix: String?, attr: Attr?, `is`: InputStream, drp: DatasourceResourceProvider, append: Boolean) : Thread() {
    private val core: Core
    private val dc: DatasourceConnection?
    private val prefix: String?
    private val attr: Attr?
    private val `is`: InputStream
    private var e: SQLException? = null
    private val append: Boolean
    private val drp: DatasourceResourceProvider
    @Override
    fun run() {
        try {
            core.write(dc, prefix, attr, `is`, append)
            drp.release(dc)
            // manager.releaseConnection(connId,dc);
        } catch (e: SQLException) {
            this.e = e
        }
    }

    val exception: SQLException?
        get() = e

    init {
        this.core = core
        this.dc = dc
        this.prefix = prefix
        this.attr = attr
        this.`is` = `is`
        this.drp = drp
        this.append = append
    }
}