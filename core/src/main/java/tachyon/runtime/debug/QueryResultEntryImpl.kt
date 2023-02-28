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
package tachyon.runtime.debug

import tachyon.commons.io.SystemUtil.TemplateLine

/**
 *
 */
class QueryResultEntryImpl(qr: QueryResult?, datasource: String?, name: String?, sql: SQL?, recordcount: Int, tl: TemplateLine?, exe: Long) : QueryEntry {
    private val sql: SQL?
    private val exe: Long
    private val name: String?
    private val recordcount: Int
    private val datasource: String?
    private val qr: QueryResult?
    private val startTime: Long
    private val tl: TemplateLine?
    @Override
    fun getQry(): Query? { // FUTURE deprecate
        return if (qr is Query) qr as Query? else null
    }

    fun getQueryResult(): QueryResult? {
        return qr
    }

    @Override
    fun getExe(): Int {
        return getExecutionTime().toInt()
    }

    @Override
    fun getExecutionTime(): Long {
        return exe
    }

    @Override
    fun getSQL(): SQL? {
        return sql
    }

    @Override
    fun getSrc(): String? {
        return if (tl == null) "" else tl.template
    }

    fun getTemplateLine(): TemplateLine? {
        return tl
    }

    @Override
    fun getName(): String? {
        return name
    }

    @Override
    fun getRecordcount(): Int {
        return recordcount
    }

    @Override
    fun getDatasource(): String? {
        return datasource
    }

    @Override
    fun getStartTime(): Long {
        return startTime
    }

    @Override
    fun getCacheType(): String? {
        return if (qr == null) null else qr.getCacheType()
    }

    companion object {
        private const val serialVersionUID = 8655915268130645466L
    }

    /**
     * constructor of the class
     *
     * @param recordcount
     * @param query
     * @param src
     * @param exe
     */
    init {
        startTime = System.currentTimeMillis() - exe / 1000000
        this.datasource = datasource
        this.recordcount = recordcount
        this.name = name
        this.tl = tl
        this.sql = sql
        this.exe = exe
        this.qr = qr
    }
}