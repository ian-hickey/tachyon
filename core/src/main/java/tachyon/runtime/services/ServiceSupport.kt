/**
 * Copyright (c) 2014, the Railo Company Ltd.
 * Copyright (c) 2015, Tachyon Assosication Switzerland
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
package tachyon.runtime.services

import java.util.HashMap

class ServiceSupport : Service {
    @Override
    @Throws(ServiceException::class)
    fun start() {
    }

    @Override
    @Throws(ServiceException::class)
    fun stop() {
    }

    @Override
    @Throws(ServiceException::class)
    fun restart() {
    }

    @Override
    fun getStatus(): Int {
        return STARTED
    }

    @Override
    fun getMetaData(): ServiceMetaData? {
        return EmptyServiceMetaData()
    }

    @Override
    fun getProperty(key: String?): Object? {
        return null
    }

    @Override
    fun setProperty(key: String?, value: Object?) {
    }

    @Override
    fun getResourceBundle(): Map? {
        return HashMap()
    }

    @Throws(SecurityException::class)
    protected fun checkWriteAccess() {
        ConfigWebUtil.checkGeneralWriteAccess(config(), null)
    }

    @Throws(SecurityException::class)
    protected fun checkReadAccess() {
        ConfigWebUtil.checkGeneralReadAccess(config(), null)
    }

    protected fun config(): ConfigPro? {
        return ThreadLocalPageContext.getConfig() as ConfigPro
    }

    protected fun pc(): PageContext? {
        return ThreadLocalPageContext.get()
    }
}