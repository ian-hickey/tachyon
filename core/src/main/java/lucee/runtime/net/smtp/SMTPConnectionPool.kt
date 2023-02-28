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
package lucee.runtime.net.smtp

import java.util.Date

object SMTPConnectionPool {
    private val sessions: Map<String?, Stack<SessionAndTransport?>?>? = HashMap<String?, Stack<SessionAndTransport?>?>()
    @Throws(MessagingException::class)
    fun getSessionAndTransport(props: Properties?, key: String?, auth: Authenticator?, lifeTimespan: Long, idleTimespan: Long): SessionAndTransport? {

        // Session
        var sat: SessionAndTransport? = null
        val satStack: Stack<SessionAndTransport?>? = getSATStack(key)
        sat = pop(satStack)

        // when sat still valid return it
        if (sat != null) {
            if (isValid(sat, lifeTimespan, idleTimespan)) {
                return sat.touch()
            }
            disconnect(sat.transport)
        }
        return SessionAndTransport(key, props, auth, lifeTimespan, idleTimespan)
    }

    private fun isValid(sat: SessionAndTransport?, lifeTimespan: Long, idleTimespan: Long): Boolean {
        return (idleTimespan <= 0 || sat!!.lastAccess + idleTimespan > System.currentTimeMillis()) && (lifeTimespan <= 0 || sat!!.created + lifeTimespan > System.currentTimeMillis())
    }

    fun releaseSessionAndTransport(sat: SessionAndTransport?) {
        getSATStack(sat!!.key).add(sat.touch())
    }

    fun listSessions(): String? {
        val it: Iterator<Entry<String?, Stack<SessionAndTransport?>?>?> = sessions.entrySet().iterator()
        var entry: Entry<String?, Stack<SessionAndTransport?>?>?
        var stack: Stack<SessionAndTransport?>
        val sb = StringBuilder()
        while (it.hasNext()) {
            entry = it.next()
            sb.append(entry.getKey()).append('\n')
            stack = entry.getValue()
            if (stack.isEmpty()) continue
            listSessions(sb, stack)
        }
        return sb.toString()
    }

    private fun listSessions(sb: StringBuilder?, stack: Stack<SessionAndTransport?>?) {
        val it: Iterator<SessionAndTransport?> = stack.iterator()
        while (it.hasNext()) {
            val sat = it.next()
            sb.append("- " + sat!!.key + ":" + Date(sat.lastAccess)).append('\n')
        }
    }

    fun closeSessions() {
        val it: Iterator<Entry<String?, Stack<SessionAndTransport?>?>?> = sessions.entrySet().iterator()
        var entry: Entry<String?, Stack<SessionAndTransport?>?>?
        var oldStack: Stack<SessionAndTransport?>
        var newStack: Stack<SessionAndTransport?>?
        while (it.hasNext()) {
            entry = it.next()
            oldStack = entry.getValue()
            if (oldStack.isEmpty()) continue
            newStack = Stack<SessionAndTransport?>()
            entry.setValue(newStack)
            closeSessions(oldStack, newStack)
        }
    }

    private fun closeSessions(oldStack: Stack<SessionAndTransport?>?, newStack: Stack<SessionAndTransport?>?) {
        var sat: SessionAndTransport?
        while (pop(oldStack).also { sat = it } != null) {
            if (!isValid(sat, sat!!.lifeTimespan, sat!!.idleTimespan)) {
                disconnect(sat!!.transport)
            } else newStack.add(sat)
        }
    }

    fun disconnect(transport: Transport?) {
        if (transport != null && transport.isConnected()) {
            try {
                transport.close()
            } catch (e: MessagingException) {
            }
        }
    }

    private fun getSATStack(key: String?): Stack<SessionAndTransport?>? {
        var stack: Stack<SessionAndTransport?>?
        synchronized(sessions) {
            stack = sessions!![key]
            if (stack == null) {
                stack = Stack<SessionAndTransport?>()
                sessions.put(key, stack)
            }
        }
        return stack
    }

    private fun createSession(key: String?, props: Properties?, auth: Authenticator?): Session? {
        return if (auth != null) Session.getInstance(props, auth) else Session.getInstance(props)
    }

    private fun pop(satStack: Stack<SessionAndTransport?>?): SessionAndTransport? {
        try {
            return satStack.pop()
        } catch (t: Throwable) {
            ExceptionUtil.rethrowIfNecessary(t)
        }
        return null
    }

    class SessionAndTransport internal constructor(val key: String?, props: Properties?, auth: Authenticator?, lifeTimespan: Long, idleTimespan: Long) {
        val session: Session?
        val transport: Transport?
        var lastAccess: Long = 0
        val created: Long
        val lifeTimespan: Long
        val idleTimespan: Long
        fun touch(): SessionAndTransport? {
            lastAccess = System.currentTimeMillis()
            return this
        }

        init {
            session = createSession(key, props, auth)
            transport = session.getTransport("smtp")
            created = System.currentTimeMillis()
            this.lifeTimespan = lifeTimespan
            this.idleTimespan = idleTimespan
            touch()
        }
    }
}