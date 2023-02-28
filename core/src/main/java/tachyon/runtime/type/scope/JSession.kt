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
package tachyon.runtime.type.scope

import java.util.ArrayList

/**
 *
 */
class JSession : ScopeSupport("session", SCOPE_SESSION, Struct.TYPE_LINKED), Session, HttpSessionBindingListener, MemoryScope, CSRFTokenSupport {
    companion object {
        val SESSION_ID: Collection.Key? = KeyConstants._sessionid
        private val FIX_KEYS: Set<Collection.Key?>? = HashSet<Collection.Key?>()

        init {
            FIX_KEYS.add(KeyConstants._sessionid)
            FIX_KEYS.add(KeyConstants._urltoken)
        }
    }

    private var name: String? = null
    private var timespan: Long = -1

    @Transient
    private var httpSession: HttpSession? = null
    private var lastAccess: Long = 0
    private var created: Long
    private val _tokens: Struct? = StructImpl()
    private var component: Component? = null
    @Override
    fun touchBeforeRequest(pc: PageContext?) {
        val appContext: ApplicationContext = pc.getApplicationContext()
        timespan = appContext.getSessionTimeout().getMillis()
        name = appContext.getName()
        val hs: HttpSession = pc.getSession()
        var id = ""
        try {
            if (hs != null) httpSession = hs
            if (httpSession != null) {
                id = httpSession.getId()
                val timeoutInSeconds = (timespan / 1000).toInt() + 60
                if (httpSession.getMaxInactiveInterval() < timeoutInSeconds) httpSession.setMaxInactiveInterval(timeoutInSeconds)
            }
        } catch (t: Throwable) {
            ExceptionUtil.rethrowIfNecessary(t)
        }
        lastAccess = System.currentTimeMillis()
        setEL(KeyConstants._sessionid, id)
        setEL(KeyConstants._urltoken, "CFID=" + pc.getCFID().toString() + "&CFTOKEN=" + pc.getCFToken().toString() + "&jsessionid=" + id)
    }

    @Override
    fun touchAfterRequest(pc: PageContext?) {
    }

    @Override
    override fun release(pc: PageContext?) {
        if (httpSession != null) {
            try {
                var key: Object
                val it: Iterator<String?> = ListUtil.toIterator(httpSession.getAttributeNames())
                while (it.hasNext()) {
                    // TODO set inative time new
                    key = it.next()
                    if (key.equals(name)) httpSession.removeAttribute(name)
                }
                name = null
                timespan = -1
                httpSession = null
                lastAccess = -1
            } catch (t: Throwable) {
                ExceptionUtil.rethrowIfNecessary(t)
            }
        }
        super.release(pc)
    }

    @Override
    fun getLastAccess(): Long {
        return lastAccess
    }

    @Override
    fun getTimeSpan(): Long {
        return timespan
    }

    @Override
    fun isExpired(): Boolean {
        return getLastAccess() + getTimeSpan() < System.currentTimeMillis()
    }

    @Override
    fun valueBound(event: HttpSessionBindingEvent?) {
    }

    @Override
    fun valueUnbound(event: HttpSessionBindingEvent?) {
        clear()
    }

    @Override
    fun touch() {
        lastAccess = System.currentTimeMillis()
    }

    @Override
    fun getCreated(): Long {
        return created
    }

    @Override
    fun pureKeys(): Array<Collection.Key?>? {
        val keys: List<Collection.Key?> = ArrayList<Collection.Key?>()
        val it: Iterator<Key?> = keyIterator()
        var key: Collection.Key?
        while (it.hasNext()) {
            key = it.next()
            if (!FIX_KEYS!!.contains(key)) keys.add(key)
        }
        return keys.toArray(arrayOfNulls<Collection.Key?>(keys.size()))
    }

    @Override
    fun resetEnv(pc: PageContext?) {
        created = System.currentTimeMillis()
        lastAccess = System.currentTimeMillis()
        touchBeforeRequest(pc)
    }

    @Override
    override fun generateToken(key: String?, forceNew: Boolean): String? {
        return ScopeUtil.generateCsrfToken(_tokens, key, forceNew)
    }

    @Override
    override fun verifyToken(token: String?, key: String?): Boolean {
        return ScopeUtil.verifyCsrfToken(_tokens, token, key)
    }

    fun setComponent(component: Component?) {
        this.component = component
    }

    fun getComponent(): Component? {
        return component
    }

    /**
     * constructor of the class
     */
    init {
        setDisplayName("Scope Session (Type JEE)")
        created = System.currentTimeMillis()
    }
}