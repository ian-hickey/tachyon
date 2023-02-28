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

import java.io.Serializable

class SessionEndListener : StorageScopeListener, Serializable {
    @Override
    override fun doEnd(engine: StorageScopeEngine?, cleaner: StorageScopeCleaner?, appName: String?, cfid: String?) {
        val factory: CFMLFactoryImpl = engine!!.getFactory()
        val listener: ApplicationListener = factory.getConfig().getApplicationListener()
        try {
            cleaner!!.info("call onSessionEnd for $appName/$cfid")
            listener.onSessionEnd(factory, appName, cfid)
        } catch (t: Throwable) {
            ExceptionUtil.rethrowIfNecessary(t)
            ExceptionHandler.log(factory.getConfig(), Caster.toPageException(t))
        }
    }

    companion object {
        private const val serialVersionUID = -3868545140988347285L
    }
}