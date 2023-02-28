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
package tachyon.runtime.type.scope.storage

import java.util.Date

/**
 * client scope that store it's data in the cookie of the client
 */
abstract class StorageScopeCookie : StorageScopeImpl {
    // private Cookie cookie;
    private var cookieName: String?

    companion object {
        private const val serialVersionUID = -3509170569488448183L
        private val serializer: ScriptConverter? = ScriptConverter()
        protected var evaluator: CFMLExpressionInterpreter? = CFMLExpressionInterpreter(false)
        private fun doNowIfNull(pc: PageContext?, dt: DateTime?): DateTime? {
            return if (dt == null) DateTimeImpl(pc.getConfig()) else dt
        }

        protected fun _loadData(pc: PageContext?, cookieName: String?, type: Int, strType: String?, log: Log?): Struct? {
            val data = pc.cookieScope().get(cookieName, null) as String
            if (data != null) {
                try {
                    val sct: Struct = evaluator.interpret(pc, data) as Struct
                    var l: Long
                    var str: String

                    // last visit
                    str = pc.cookieScope().get(cookieName.toString() + "_LV", null)
                    if (!StringUtil.isEmpty(str)) {
                        l = Caster.toLongValue(str, 0)
                        if (l > 0) sct.setEL(LASTVISIT, DateTimeImpl(pc, l, true))
                    }
                    if (type == SCOPE_CLIENT) {
                        // hit count
                        str = pc.cookieScope().get(cookieName.toString() + "_HC", null)
                        if (!StringUtil.isEmpty(str)) sct.setEL(HITCOUNT, Caster.toDouble(str, null))

                        // time created
                        str = pc.cookieScope().get(cookieName.toString() + "_TC", null)
                        if (!StringUtil.isEmpty(str)) {
                            l = Caster.toLongValue(str, 0)
                            if (l > 0) sct.setEL(TIMECREATED, DateTimeImpl(pc, l, true))
                        }
                    }
                    ScopeContext.debug(log, "load data from cookie for " + strType + " scope for " + pc.getApplicationContext().getName() + "/" + pc.getCFID())
                    return sct
                } catch (e: Exception) {
                }
            }
            ScopeContext.debug(log, "create new " + strType + " scope for " + pc.getApplicationContext().getName() + "/" + pc.getCFID())
            return StructImpl()
        }

        protected fun has(pc: PageContext?, cookieName: String?, type: Int, strType: String?): Boolean {
            // TODO better impl
            val data = pc.cookieScope().get(cookieName, null) as String
            return data != null
        }

        // private TimeSpan timeout;
        init {
            ignoreSet.add(KeyConstants._cfid)
            ignoreSet.add(KeyConstants._cftoken)
            ignoreSet.add(KeyConstants._urltoken)
            ignoreSet.add(KeyConstants._lastvisit)
            ignoreSet.add(KeyConstants._hitcount)
            ignoreSet.add(KeyConstants._timecreated)
        }
    }

    /**
     * Constructor of the class
     *
     * @param pc
     * @param name
     * @param sct
     */
    protected constructor(pc: PageContext?, cookieName: String?, strType: String?, type: Int, sct: Struct?) : super(sct, doNowIfNull(pc, Caster.toDate(sct.get(TIMECREATED, null), false, pc.getTimeZone(), null)),
            doNowIfNull(pc, Caster.toDate(sct.get(LASTVISIT, null), false, pc.getTimeZone(), null)), -1,
            if (type == SCOPE_CLIENT) Caster.toIntValue(sct.get(HITCOUNT, "1"), 1) else 0, strType, type) {
        this.cookieName = cookieName
    }

    /**
     * Constructor of the class, clone existing
     *
     * @param other
     */
    protected constructor(other: StorageScopeCookie?, deepCopy: Boolean) : super(other, deepCopy) {
        cookieName = other!!.cookieName
    }

    @Override
    override fun touchAfterRequest(pc: PageContext?) {
        val _isInit: Boolean = isinit
        super.touchAfterRequest(pc)
        if (!_isInit) return
        val ac: ApplicationContext = pc.getApplicationContext()
        val timespan: TimeSpan = if (getType() === SCOPE_CLIENT) ac.getClientTimeout() else ac.getSessionTimeout()
        val cookie: Cookie = pc.cookieScope()
        var isHttpOnly = true
        var isSecure = false
        var domain: String? = null
        var samesite: Short = CookieData.SAMESITE_EMPTY
        if (ac is ApplicationContextSupport) {
            val settings: SessionCookieData = (ac as ApplicationContextSupport).getSessionCookie()
            if (settings != null) {
                isHttpOnly = settings.isHttpOnly()
                isSecure = settings.isSecure()
                domain = settings.getDomain()
                samesite = settings.getSamesite()
            }
        }
        val exp: Date = DateTimeImpl(pc, System.currentTimeMillis() + timespan.getMillis(), true)
        try {
            val ci: CookieImpl = cookie as CookieImpl
            val ser: String = serializer.serializeStruct(sct, ignoreSet)
            if (hasChanges()) {
                ci.setCookie(KeyImpl.init(cookieName), ser, exp, isSecure, "/", domain, isHttpOnly, false, true, samesite)
            }
            ci.setCookie(KeyImpl.init(cookieName.toString() + "_LV"), Caster.toString(_lastvisit.getTime()), exp, isSecure, "/", domain, isHttpOnly, false, true, samesite)
            if (getType() === SCOPE_CLIENT) {
                ci.setCookie(KeyImpl.init(cookieName.toString() + "_TC"), Caster.toString(timecreated.getTime()), exp, isSecure, "/", domain, isHttpOnly, false, true, samesite)
                ci.setCookie(KeyImpl.init(cookieName.toString() + "_HC"), Caster.toString(sct.get(HITCOUNT, "")), exp, isSecure, "/", domain, isHttpOnly, false, true, samesite)
            }
        } catch (t: Throwable) {
            ExceptionUtil.rethrowIfNecessary(t)
        }
    }

    @Override
    fun getStorageType(): String? {
        return "Cookie"
    }
}