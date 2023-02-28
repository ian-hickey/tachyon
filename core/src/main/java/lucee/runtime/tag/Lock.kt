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

import lucee.commons.lang.StringUtil

/**
 * Provides two types of locks to ensure the integrity of shared data: Exclusive lock and Read-only
 * lock. An exclusive lock single-threads access to the CFML constructs in its body. Single-threaded
 * access implies that the body of the tag can be executed by at most one request at a time. A
 * request executing inside a cflock tag has an "exclusive lock" on the tag. No other requests can
 * start executing inside the tag while a request has an exclusive lock. CFML issues exclusive locks
 * on a first-come, first-served basis. A read-only lock allows multiple requests to access the CFML
 * constructs inside its body concurrently. Therefore, read-only locks should be used only when the
 * shared data is read only and not modified. If another request already has an exclusive lock on
 * the shared data, the request waits for the exclusive lock to be released.
 *
 *
 *
 */
class Lock : BodyTagTryCatchFinallyImpl() {
    private var id: String? = "anonymous"

    /**
     * Specifies the maximum amount of time, in seconds, to wait to obtain a lock. If a lock can be
     * obtained within the specified period, execution continues inside the body of the tag. Otherwise,
     * the behavior depends on the value of the throwOnTimeout attribute.
     */
    private var timeoutInMillis = 0

    /**
     * readOnly or Exclusive. Specifies the type of lock: read-only or exclusive. Default is Exclusive.
     * A read-only lock allows more than one request to read shared data. An exclusive lock allows only
     * one request to read or write to shared data.
     */
    private var type: Short = LockManager.TYPE_EXCLUSIVE

    /**
     * Specifies the scope as one of the following: Application, Server, or Session. This attribute is
     * mutually exclusive with the name attribute.
     */
    private var scope = SCOPE_NONE

    /**
     * Yes or No. Specifies how timeout conditions are handled. If the value is Yes, an exception is
     * generated to provide notification of the timeout. If the value is No, execution continues past
     * the cfclock tag. Default is Yes.
     */
    private var throwontimeout = true

    /** Specifies the name of the lock.  */
    private var name: String? = null
    private var manager: LockManager? = null
    private var data: LockData? = null
    private var start: Long = 0
    private var result: String? = "cflock"
    @Override
    fun release() {
        super.release()
        type = LockManager.TYPE_EXCLUSIVE
        scope = SCOPE_NONE
        throwontimeout = true
        name = null
        manager = null
        data = null
        id = "anonymous"
        timeoutInMillis = 0
        result = "cflock"
    }

    /**
     * @param id the id to set
     */
    fun setId(id: String?) {
        this.id = id
    }

    /**
     * set the value timeout Specifies the maximum amount of time, in seconds, to wait to obtain a lock.
     * If a lock can be obtained within the specified period, execution continues inside the body of the
     * tag. Otherwise, the behavior depends on the value of the throwOnTimeout attribute.
     *
     * @param timeout value to set
     */
    @Throws(PageException::class)
    fun setTimeout(oTimeout: Object?) {
        if (oTimeout is TimeSpan) timeoutInMillis = toInt((oTimeout as TimeSpan?).getMillis()) else timeoutInMillis = toInt(Caster.toDoubleValue(oTimeout) * 1000.0)
        // print.out(Caster.toString(timeoutInMillis));
    }

    fun setTimeout(timeout: Double) {
        timeoutInMillis = toInt(timeout * 1000.0)
    }

    /**
     * set the value type readOnly or Exclusive. Specifies the type of lock: read-only or exclusive.
     * Default is Exclusive. A read-only lock allows more than one request to read shared data. An
     * exclusive lock allows only one request to read or write to shared data.
     *
     * @param type value to set
     * @throws ApplicationException
     */
    @Throws(ApplicationException::class)
    fun setType(type: String?) {
        var type = type
        type = type.toLowerCase().trim()
        if (type.equals("exclusive")) {
            this.type = LockManager.TYPE_EXCLUSIVE
        } else if (type.startsWith("read")) {
            this.type = LockManager.TYPE_READONLY
        } else throw ApplicationException("invalid value [$type] for attribute [type] from tag [lock]", "valid values are [exclusive,read-only]")
    }

    /**
     * set the value scope Specifies the scope as one of the following: Application, Server, or Session.
     * This attribute is mutually exclusive with the name attribute.
     *
     * @param scope value to set
     * @throws ApplicationException
     */
    @Throws(ApplicationException::class)
    fun setScope(scope: String?) {
        var scope = scope
        scope = scope.toLowerCase().trim()
        if (scope.equals("server")) this.scope = SCOPE_SERVER else if (scope.equals("application")) this.scope = SCOPE_APPLICATION else if (scope.equals("session")) this.scope = SCOPE_SESSION else if (scope.equals("request")) this.scope = SCOPE_REQUEST else throw ApplicationException("invalid value [$scope] for attribute [scope] from tag [lock]", "valid values are [server,application,session]")
    }

    /**
     * set the value throwontimeout Yes or No. Specifies how timeout conditions are handled. If the
     * value is Yes, an exception is generated to provide notification of the timeout. If the value is
     * No, execution continues past the cfclock tag. Default is Yes.
     *
     * @param throwontimeout value to set
     */
    fun setThrowontimeout(throwontimeout: Boolean) {
        this.throwontimeout = throwontimeout
    }

    /**
     * set the value name
     *
     * @param name value to set
     * @throws ApplicationException
     */
    @Throws(ApplicationException::class)
    fun setName(name: String?) {
        if (name == null) return
        this.name = name.trim()
        if (name.length() === 0) throw ApplicationException("invalid attribute definition", "attribute [name] can't be an empty string")
    }

    @Throws(ApplicationException::class)
    fun setResult(result: String?) {
        if (StringUtil.isEmpty(result)) return
        this.result = result.trim()
    }

    @Override
    @Throws(PageException::class)
    fun doStartTag(): Int {
        if (timeoutInMillis <= 0) {
            val remaining: TimeSpan = PageContextUtil.remainingTime(pageContext, true)
            timeoutInMillis = toInt(remaining.getMillis())
        }
        manager = pageContext.getConfig().getLockManager()
        // check attributes
        if (name != null && scope != SCOPE_NONE) {
            throw LockException(LockException.OPERATION_CREATE, name, "invalid attribute combination", "attribute [name] and [scope] can't be used together")
        }
        if (name == null && scope == SCOPE_NONE) {
            name = "id-$id"
        }
        var lockType: String? = null
        if (name == null) {
            val cid: String = pageContext.getConfig().getIdentification().getId()
            // Session
            if (scope == SCOPE_REQUEST) {
                lockType = "request"
                name = "__request_" + cid + "__" + (pageContext.requestScope() as RequestImpl)._getId()
            } else if (scope == SCOPE_SESSION) {
                lockType = "session"
                name = "__session_" + cid + "__" + pageContext.sessionScope()._getId()
            } else if (scope == SCOPE_APPLICATION) {
                lockType = "application"
                name = "__application_" + cid + "__" + (pageContext.applicationScope() as ApplicationImpl)._getId()
            } else if (scope == SCOPE_SERVER) {
                lockType = "server"
                name = "__server_" + (pageContext.serverScope() as ServerImpl)._getId()
            }
        }
        val cflock: Struct = StructImpl()
        cflock.set(KeyConstants._succeeded, Boolean.TRUE)
        cflock.set(KeyConstants._errortext, "")
        pageContext.setVariable(result, cflock)
        start = System.nanoTime()
        try {
            (pageContext as PageContextImpl?).setActiveLock(ActiveLock(type, name, timeoutInMillis)) // this has to be first, otherwise LockTimeoutException has nothing to
            // release
            data = manager.lock(type, name, timeoutInMillis, pageContext.getId())
        } catch (e: LockTimeoutException) {
            val mi: LockManagerImpl? = manager as LockManagerImpl?
            val hasReadLock: Boolean = mi.isReadLocked(name)
            val hasWriteLock: Boolean = mi.isWriteLocked(name)
            val msg: String = LockTimeoutExceptionImpl.createMessage(type, name, lockType, timeoutInMillis, hasReadLock, hasWriteLock)
            _release(pageContext, System.nanoTime() - start)
            name = null
            cflock.set(KeyConstants._succeeded, Boolean.FALSE)
            cflock.set(KeyConstants._errortext, msg)
            if (throwontimeout) throw LockException(LockException.OPERATION_TIMEOUT, name, msg)
            return SKIP_BODY
        } catch (e: InterruptedException) {
            _release(pageContext, System.nanoTime() - start)
            cflock.set(KeyConstants._succeeded, Boolean.FALSE)
            cflock.set(KeyConstants._errortext, e.getMessage())
            if (throwontimeout) throw Caster.toPageException(e)
            return SKIP_BODY
        }
        return EVAL_BODY_INCLUDE
    }

    private fun toInt(l: Long): Int {
        return if (l > Integer.MAX_VALUE) Integer.MAX_VALUE else l.toInt()
    }

    private fun toInt(d: Double): Int {
        return if (d > Integer.MAX_VALUE) Integer.MAX_VALUE else d.toInt()
    }

    private fun _release(pc: PageContext?, exe: Long) {
        val al: ActiveLock = (pc as PageContextImpl?).releaseActiveLock()
        // listener
        (pc.getConfig() as ConfigWebPro).getActionMonitorCollector().log(pageContext, "lock", "Lock", exe, al.name.toString() + ":" + al.timeoutInMillis)
    }

    @Override
    fun doFinally() {
        _release(pageContext, System.nanoTime() - start)
        if (name != null) manager.unlock(data)
    }

    companion object {
        private const val SCOPE_NONE: Short = 0
        private const val SCOPE_SERVER: Short = 1
        private const val SCOPE_APPLICATION: Short = 2
        private const val SCOPE_SESSION: Short = 3
        private const val SCOPE_REQUEST: Short = 4
    }
}