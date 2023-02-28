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
package lucee.runtime.tag

import java.sql.Connection

/**
 * Transaction class
 */
class Transaction : BodyTagTryCatchFinallyImpl() {
    // private boolean hasBody;
    private var isolation: Int = Connection.TRANSACTION_NONE
    private var action = ACTION_NONE
    private var innerTag = false
    private var ignore = false
    private var savepoint: String? = null
    @Override
    fun release() {
        // hasBody=false;
        isolation = Connection.TRANSACTION_NONE
        action = ACTION_NONE
        innerTag = false
        ignore = false
        savepoint = null
        super.release()
    }

    /**
     * @param action The action to set.
     * @throws DatabaseException
     */
    @Throws(DatabaseException::class)
    fun setAction(strAction: String?) {
        var strAction = strAction
        strAction = strAction.trim().toLowerCase()
        action = if (strAction.equals("begin")) ACTION_BEGIN else if (strAction.equals("commit")) ACTION_COMMIT else if (strAction.equals("rollback")) ACTION_ROLLBACK else if (strAction.equals("setsavepoint")) ACTION_SET_SAVEPOINT else {
            throw DatabaseException("Attribute [action] has an invalid value, valid values are [begin,commit,setsavepoint and rollback]", null, null, null)
        }
    }

    /**
     * @param isolation The isolation to set.
     * @throws DatabaseException
     */
    @Throws(DatabaseException::class)
    fun setIsolation(isolation: String?) {
        var isolation = isolation
        isolation = isolation.trim().toLowerCase()
        if (isolation.equals("read_uncommitted")) this.isolation = Connection.TRANSACTION_READ_UNCOMMITTED else if (isolation.equals("read_committed")) this.isolation = Connection.TRANSACTION_READ_COMMITTED else if (isolation.equals("repeatable_read")) this.isolation = Connection.TRANSACTION_REPEATABLE_READ else if (isolation.equals("serializable")) this.isolation = Connection.TRANSACTION_SERIALIZABLE else if (isolation.equals("none")) this.isolation = Connection.TRANSACTION_NONE else throw DatabaseException(
                "Transaction has an invalid isolation level (attribute [isolation], valid values are [read_uncommitted,read_committed,repeatable_read,serializable])", null, null,
                null)
    }

    /**
     * @param isolation The isolation to set.
     * @throws DatabaseException
     */
    @Throws(DatabaseException::class)
    fun setSavepoint(savepoint: String?) {
        if (StringUtil.isEmpty(savepoint, true)) this.savepoint = null else this.savepoint = savepoint.trim().toLowerCase()
    }

    @Override
    @Throws(PageException::class)
    fun doStartTag(): Int {
        val manager: DataSourceManager = pageContext.getDataSourceManager()
        // first transaction
        if (manager.isAutoCommit()) {
            // if(!hasBody)throw new DatabaseException("transaction tag with no end Tag can only be used inside
            // a transaction tag",null,null,null);
            manager.begin(isolation)
            return EVAL_BODY_INCLUDE
        }
        // inside transaction
        innerTag = true
        when (action) {
            ACTION_NONE, ACTION_BEGIN -> ignore = true
            ACTION_COMMIT -> manager.commit()
            ACTION_ROLLBACK -> (manager as DatasourceManagerImpl).rollback(savepoint)
            ACTION_SET_SAVEPOINT -> (manager as DatasourceManagerImpl).savepoint(savepoint)
        }
        return EVAL_BODY_INCLUDE
    }

    @Override
    @Throws(Throwable::class)
    fun doCatch(t: Throwable?) {
        ExceptionUtil.rethrowIfNecessary(t)
        if (innerTag || ignore) throw t!!
        val manager: DataSourceManager = pageContext.getDataSourceManager()
        try {
            manager.rollback()
        } catch (e: DatabaseException) {
            // print.printST(e);
        }
        throw t!!
    }

    /**
     * @param hasBody
     */
    fun hasBody(hasBody: Boolean) { // print.out("hasBody"+hasBody);
        // this.hasBody=hasBody;
    }

    @Override
    fun doFinally() {
        if (!ignore && !innerTag) {
            pageContext.getDataSourceManager().end()
        }
        super.doFinally()
    }

    @Override
    @Throws(JspException::class)
    fun doAfterBody(): Int {
        if (!ignore && !innerTag) {
            pageContext.getDataSourceManager().commit()
        }
        return super.doAfterBody()
    }

    companion object {
        private const val ACTION_NONE = 0
        private const val ACTION_BEGIN = 1
        private const val ACTION_COMMIT = 2
        private const val ACTION_ROLLBACK = 4
        private const val ACTION_SET_SAVEPOINT = 8
    }
}