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

import java.io.IOException

class NoneAppListener : AppListenerSupport() {
    private var mode = 0
    @Override
    @Throws(PageException::class)
    fun onRequest(pc: PageContext?, requestedPage: PageSource?, rl: RequestListener?) {
        var requestedPage: PageSource? = requestedPage
        if (rl != null) {
            requestedPage = rl.execute(pc, requestedPage)
            if (requestedPage == null) return
        }
        pc.doInclude(arrayOf<PageSource?>(requestedPage), false)
    }

    @Override
    @Throws(PageException::class)
    fun onApplicationStart(pc: PageContext?): Boolean {
        // do nothing
        return true
    }

    @Override
    @Throws(PageException::class)
    override fun onApplicationStart(pc: PageContext?, application: Application?): Boolean {
        // do nothing
        return true
    }

    @Override
    @Throws(PageException::class)
    fun onSessionStart(pc: PageContext?) {
        // do nothing
    }

    @Override
    @Throws(PageException::class)
    override fun onSessionStart(pc: PageContext?, session: Session?) {
        // do nothing
    }

    @Override
    @Throws(PageException::class)
    fun onApplicationEnd(factory: CFMLFactory?, applicationName: String?) {
        // do nothing
    }

    @Override
    @Throws(PageException::class)
    fun onSessionEnd(cfmlFactory: CFMLFactory?, applicationName: String?, cfid: String?) {
        // do nothing
    }

    @Override
    @Throws(PageException::class)
    fun onDebug(pc: PageContext?) {
        try {
            if (pc.getConfig().debug()) pc.getDebugger().writeOut(pc)
        } catch (e: IOException) {
            throw Caster.toPageException(e)
        }
    }

    @Override
    fun onError(pc: PageContext?, pe: PageException?) {
        pc.handlePageException(pe)
    }

    @Override
    fun setMode(mode: Int) {
        this.mode = mode
    }

    @Override
    fun getMode(): Int {
        return mode
    }

    @Override
    fun getType(): String? {
        return "none"
    }
}