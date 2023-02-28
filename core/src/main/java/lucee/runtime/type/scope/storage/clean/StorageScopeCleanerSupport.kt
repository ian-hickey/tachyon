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
package lucee.runtime.type.scope.storage.clean

import lucee.commons.lang.ExceptionUtil

abstract class StorageScopeCleanerSupport(protected var type: Int, listener: StorageScopeListener?, intervall: Int) : StorageScopeCleaner {
    protected var engine: StorageScopeEngine? = null
    protected var listener: StorageScopeListener?
    private val application: String?
    protected var strType: String?
    private val intervall: Int
    private var lastClean: Long = 0
    @Override
    fun init(engine: StorageScopeEngine?) {
        this.engine = engine
    }

    @Override
    fun clean() {
        if (lastClean + intervall < System.currentTimeMillis()) {
            // info("cleaning "+application);
            _clean()
            lastClean = System.currentTimeMillis()
            // info("next cleaning intervall in "+(intervall/1000)+" seconds");
        }
    }

    protected abstract fun _clean()

    /**
     * @return the log
     */
    @Override
    fun info(msg: String?) {
        engine.getFactory().getScopeContext().info(msg)
    }

    @Override
    fun error(msg: String?) {
        engine.getFactory().getScopeContext().error(msg)
        engine._getLog().error(application, msg)
    }

    @Override
    fun error(t: Throwable?) {
        engine.getFactory().getScopeContext().error(t)
        engine._getLog().error(application, ExceptionUtil.getStacktrace(t!!, true))
    }

    companion object {
        protected const val INTERVALL_MINUTE = 60 * 1000
        protected const val INTERVALL_HOUR = 60 * 60 * 1000
        protected const val INTERVALL_DAY = 24 * 60 * 60 * 1000
    }

    init {
        this.listener = listener
        strType = VariableInterpreter.scopeInt2String(type)
        application = strType.toString() + " storage"
        this.intervall = intervall
    }
}