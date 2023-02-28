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
package lucee.runtime.engine

import java.util.Locale

/**
 * class to handle thread local PageContext, do use pagecontext in classes that have no method
 * argument pagecontext
 */
object ThreadLocalPageContext {
    private val DEFAULT_LOCALE: Locale? = Locale.getDefault()
    private val DEFAULT_TIMEZONE: TimeZone? = TimeZone.getDefault()
    private val pcThreadLocal: ThreadLocal<PageContext?>? = ThreadLocal<PageContext?>()
    val callOnStart: CallOnStart? = CallOnStart()

    /**
     * register a pagecontext for he current thread
     *
     * @param pc PageContext to register
     */
    fun register(pc: PageContext?) { // print.ds(Thread.currentThread().getName());
        if (pc == null) return  // TODO happens with Gateway, but should not!
        // TODO should i set the old one by "release"?
        val t: Thread = Thread.currentThread()
        t.setContextClassLoader((pc.getConfig() as ConfigPro).getClassLoaderEnv())
        (pc as PageContextImpl?).setThread(t)
        pcThreadLocal.set(pc)
    }

    /**
     * returns pagecontext registered for the current thread
     *
     * @return pagecontext for the current thread or null if no pagecontext is regisred for the current
     * thread
     */
    fun get(): PageContext? { // print.dumpStack();
        return pcThreadLocal.get()
    }

    fun getConfig(): Config? {
        val pc: PageContext? = get()
        return if (pc != null) {
            pc.getConfig()
        } else ThreadLocalConfig.get()
    }

    /**
     * release the pagecontext for the current thread
     */
    fun release() { // print.ds(Thread.currentThread().getName());
        pcThreadLocal.set(null)
    }

    fun getConfig(pc: PageContext?): Config? {
        return if (pc == null) getConfig() else pc.getConfig()
    }

    fun getConfig(config: Config?): Config? {
        return if (config == null) getConfig() else config
    }

    fun getTimeZone(pc: PageContext?): TimeZone? {
        // pc
        var pc: PageContext? = pc
        pc = get(pc)
        if (pc != null) {
            return if (pc.getTimeZone() != null) pc.getTimeZone() else DEFAULT_TIMEZONE
        }

        // config
        val config: Config = getConfig(null as Config?)
        return if (config != null && config.getTimeZone() != null) {
            config.getTimeZone()
        } else DEFAULT_TIMEZONE
    }

    fun getLog(pc: PageContext?, logName: String?): Log? {
        // pc
        var pc: PageContext? = pc
        pc = get(pc)
        if (pc is PageContextImpl) {
            return (pc as PageContextImpl?).getLog(logName)
        }

        // config
        val config: Config = getConfig(pc)
        return if (config != null) {
            config.getLog(logName)
        } else null
    }

    fun getLog(config: Config?, logName: String?): Log? {
        // pc
        var config: Config? = config
        if (config is ConfigWeb) {
            val pc: PageContext = get(config)
            if (pc is PageContextImpl) {
                return (pc as PageContextImpl).getLog(logName)
            }
        }

        // config
        config = getConfig(config)
        return if (config != null) {
            config.getLog(logName)
        } else null
    }

    fun getLog(logName: String?): Log? {
        // pc
        val pc: PageContext? = get()
        if (pc is PageContextImpl) {
            return (pc as PageContextImpl?).getLog(logName)
        }

        // config
        val config: Config? = getConfig()
        return if (config != null) {
            config.getLog(logName)
        } else null
    }

    fun getLocale(): Locale? {
        return getLocale(null as PageContext?)
    }

    fun getLocale(l: Locale?): Locale? {
        return if (l != null) l else getLocale(null as PageContext?)
    }

    fun getLocale(pc: PageContext?): Locale? {
        // pc
        var pc: PageContext? = pc
        pc = get(pc)
        if (pc != null) {
            return if (pc.getLocale() != null) pc.getLocale() else DEFAULT_LOCALE
        }

        // config
        val config: Config = getConfig(null as Config?)
        return if (config != null && config.getLocale() != null) {
            config.getLocale()
        } else DEFAULT_LOCALE
    }

    fun getTimeZone(config: Config?): TimeZone? {
        var config: Config? = config
        val pc: PageContext? = get()
        if (pc != null && pc.getTimeZone() != null) return pc.getTimeZone()
        config = getConfig(config)
        return if (config != null && config.getTimeZone() != null) {
            config.getTimeZone()
        } else DEFAULT_TIMEZONE
    }

    fun getTimeZone(timezone: TimeZone?): TimeZone? {
        return if (timezone != null) timezone else getTimeZone(null as PageContext?)
    }

    fun getTimeZone(): TimeZone? {
        return getTimeZone(null as PageContext?)
    }

    operator fun get(pc: PageContext?): PageContext? {
        return if (pc == null) get() else pc
    }

    operator fun get(config: Config?): PageContext? {
        val pc: PageContext? = get()
        return if (pc != null && pc.getConfig() === config) pc else null
    }

    fun getThreadId(pc: PageContext?): Long {
        return if (pc != null) pc.getThread().getId() else Thread.currentThread().getId()
    }

    class CallOnStart : ThreadLocal<Boolean?>() {
        @Override
        protected fun initialValue(): Boolean? {
            return Boolean.TRUE
        }
    }
}