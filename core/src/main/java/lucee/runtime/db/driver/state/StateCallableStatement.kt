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
package lucee.runtime.db.driver.state

import java.sql.CallableStatement

class StateCallableStatement(conn: ConnectionProxy?, prepareCall: CallableStatement?, sql: String?) : CallableStatementProxy(conn, prepareCall, sql) {
    @Override
    @Throws(SQLException::class)
    fun execute(sql: String?): Boolean {
        return StateUtil.execute(ThreadLocalPageContext.get(), stat, sql)
    }

    @Override
    @Throws(SQLException::class)
    fun execute(pc: PageContext?, sql: String?): Boolean {
        return StateUtil.execute(pc, stat, sql)
    }

    @Override
    @Throws(SQLException::class)
    fun execute(sql: String, autoGeneratedKeys: Int): Boolean {
        return StateUtil.execute(ThreadLocalPageContext.get(), stat, sql, autoGeneratedKeys)
    }

    @Override
    @Throws(SQLException::class)
    fun execute(pc: PageContext?, sql: String, autoGeneratedKeys: Int): Boolean {
        return StateUtil.execute(pc, stat, sql, autoGeneratedKeys)
    }

    @Override
    @Throws(SQLException::class)
    fun execute(sql: String, columnIndexes: IntArray?): Boolean {
        return StateUtil.execute(ThreadLocalPageContext.get(), stat, sql, columnIndexes)
    }

    @Override
    @Throws(SQLException::class)
    fun execute(pc: PageContext?, sql: String, columnIndexes: IntArray?): Boolean {
        return StateUtil.execute(pc, stat, sql, columnIndexes)
    }

    @Override
    @Throws(SQLException::class)
    fun execute(sql: String, columnNames: Array<String?>?): Boolean {
        return StateUtil.execute(ThreadLocalPageContext.get(), stat, sql, columnNames)
    }

    @Override
    @Throws(SQLException::class)
    fun execute(pc: PageContext?, sql: String, columnNames: Array<String?>?): Boolean {
        return StateUtil.execute(pc, stat, sql, columnNames)
    }

    @Override
    @Throws(SQLException::class)
    fun executeQuery(sql: String?): ResultSet {
        return StateUtil.executeQuery(ThreadLocalPageContext.get(), stat, sql)
    }

    @Override
    @Throws(SQLException::class)
    fun executeQuery(pc: PageContext?, sql: String?): ResultSet {
        return StateUtil.executeQuery(pc, stat, sql)
    }

    @Override
    @Throws(SQLException::class)
    fun executeUpdate(sql: String?): Int {
        return StateUtil.executeUpdate(ThreadLocalPageContext.get(), stat, sql)
    }

    @Override
    @Throws(SQLException::class)
    fun executeUpdate(pc: PageContext?, sql: String?): Int {
        return StateUtil.executeUpdate(pc, stat, sql)
    }

    @Override
    @Throws(SQLException::class)
    fun executeUpdate(sql: String, autoGeneratedKeys: Int): Int {
        return StateUtil.executeUpdate(ThreadLocalPageContext.get(), stat, sql, autoGeneratedKeys)
    }

    @Override
    @Throws(SQLException::class)
    fun executeUpdate(pc: PageContext?, sql: String, autoGeneratedKeys: Int): Int {
        return StateUtil.executeUpdate(pc, stat, sql, autoGeneratedKeys)
    }

    @Override
    @Throws(SQLException::class)
    fun executeUpdate(sql: String, columnIndexes: IntArray?): Int {
        return StateUtil.executeUpdate(ThreadLocalPageContext.get(), stat, sql, columnIndexes)
    }

    @Override
    @Throws(SQLException::class)
    fun executeUpdate(pc: PageContext?, sql: String, columnIndexes: IntArray?): Int {
        return StateUtil.executeUpdate(pc, stat, sql, columnIndexes)
    }

    @Override
    @Throws(SQLException::class)
    fun executeUpdate(sql: String, columnNames: Array<String?>?): Int {
        return StateUtil.executeUpdate(ThreadLocalPageContext.get(), stat, sql, columnNames)
    }

    @Override
    @Throws(SQLException::class)
    fun executeUpdate(pc: PageContext?, sql: String, columnNames: Array<String?>?): Int {
        return StateUtil.executeUpdate(pc, stat, sql, columnNames)
    }

    @Override
    @Throws(SQLException::class)
    fun execute(): Boolean {
        return StateUtil.execute(ThreadLocalPageContext.get(), stat, sql)
    }

    @Override
    @Throws(SQLException::class)
    fun execute(pc: PageContext?): Boolean {
        return StateUtil.execute(pc, stat, sql)
    }

    @Override
    @Throws(SQLException::class)
    fun executeQuery(): ResultSet {
        return StateUtil.executeQuery(ThreadLocalPageContext.get(), stat, sql)
    }

    @Override
    @Throws(SQLException::class)
    fun executeQuery(pc: PageContext?): ResultSet {
        return StateUtil.executeQuery(pc, stat, sql)
    }

    @Override
    @Throws(SQLException::class)
    fun executeUpdate(): Int {
        return StateUtil.executeUpdate(ThreadLocalPageContext.get(), stat, sql)
    }

    @Override
    @Throws(SQLException::class)
    fun executeUpdate(pc: PageContext?): Int {
        return StateUtil.executeUpdate(pc, stat, sql)
    }

    protected fun setActiveStatement(pc: PageContextImpl, stat: Statement?, sql: String?) {
        pc.setActiveQuery(ActiveQuery(sql, System.currentTimeMillis()))
    }
}