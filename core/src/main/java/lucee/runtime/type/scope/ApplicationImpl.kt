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
package lucee.runtime.type.scope

import java.util.Map

/**
 * Session Scope
 */
class ApplicationImpl : ScopeSupport("application", SCOPE_APPLICATION, Struct.TYPE_LINKED), Application, SharedScope {
    private var lastAccess: Long = 0
    private var timeSpan: Long = 0
    private val created: Long
    private var component: Component? = null
    @Override
    fun getLastAccess(): Long {
        return lastAccess
    }

    @Override
    fun getTimeSpan(): Long {
        return timeSpan
    }

    @Override
    fun touchBeforeRequest(pc: PageContext?) {
        val appContext: ApplicationContext = pc.getApplicationContext()
        setEL(APPLICATION_NAME, appContext.getName())
        timeSpan = appContext.getApplicationTimeout().getMillis()
        lastAccess = System.currentTimeMillis()
    }

    @Override
    fun touchAfterRequest(pc: PageContext?) {
        // do nothing
    }

    @Override
    fun isExpired(): Boolean {
        return lastAccess + timeSpan < System.currentTimeMillis()
    }

    /**
     * @param lastAccess the lastAccess to set
     */
    fun setLastAccess(lastAccess: Long) {
        this.lastAccess = lastAccess
    }

    @Override
    fun touch() {
        lastAccess = System.currentTimeMillis()
    }

    /**
     * undocumented Feature in ACF
     *
     * @return
     * @throws PageException
     */
    @Throws(PageException::class)
    fun getApplicationSettings(): Map? {
        return GetApplicationSettings.call(ThreadLocalPageContext.get())
    }

    @Override
    fun getCreated(): Long {
        return created
    }

    fun setComponent(component: Component?) {
        this.component = component
    }

    fun getComponent(): Component? {
        return component
    }

    companion object {
        private const val serialVersionUID = 700830188207594563L
        private val APPLICATION_NAME: Collection.Key? = KeyImpl.getInstance("applicationname")
    }

    /**
     * default constructor of the session scope
     */
    init {
        created = System.currentTimeMillis()
    }
}