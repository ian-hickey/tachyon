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
package tachyon.runtime.listener

import tachyon.runtime.PageContext

abstract class AppListenerSupport : ApplicationListener {
    @Override
    fun hasOnApplicationStart(): Boolean {
        return false
    }

    @Override
    fun hasOnSessionStart(pc: PageContext?): Boolean {
        return false
    }

    @Override
    @Throws(PageException::class)
    fun onServerStart() {
    }

    @Override
    @Throws(PageException::class)
    fun onServerEnd() {
    }

    @Override
    fun onTimeout(pc: PageContext?) {
    }

    // FUTURE add to interface
    @Throws(PageException::class)
    abstract fun onSessionStart(pc: PageContext?, session: Session?)
    @Throws(PageException::class)
    abstract fun onApplicationStart(pc: PageContext?, application: Application?): Boolean
}