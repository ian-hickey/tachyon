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

/*
 * only use by CFML dialect so checking for dialect is not necessary
 */
class ClassicAppListener : AppListenerSupport() {
    private var mode: Int = MODE_CURRENT2ROOT
    @Override
    @Throws(PageException::class)
    fun onRequest(pc: PageContext?, requestedPage: PageSource?, rl: RequestListener?) {
        val application: Page = AppListenerUtil.getApplicationPage(pc, requestedPage, Constants.CFML_CLASSIC_APPLICATION_EVENT_HANDLER, mode, ApplicationListener.TYPE_CLASSIC)
        _onRequest(pc, requestedPage, application, rl)
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
        _onDebug(pc)
    }

    @Override
    fun onError(pc: PageContext?, pe: PageException?) {
        _onError(pc, pe)
    }

    @Override
    override fun onTimeout(pc: PageContext?) {
        _onTimeout(pc)
    }

    @Override
    override fun hasOnApplicationStart(): Boolean {
        return false
    }

    @Override
    override fun hasOnSessionStart(pc: PageContext?): Boolean {
        return false
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
        return "classic"
    }

    companion object {
        @Throws(PageException::class)
        fun _onRequest(pc: PageContext?, requestedPage: PageSource?, application: Page?, rl: RequestListener?) {
            var requestedPage: PageSource? = requestedPage
            val pci: PageContextImpl? = pc as PageContextImpl?
            pci.setAppListenerType(ApplicationListener.TYPE_CLASSIC)

            // on requestStart
            if (application != null) pci._doInclude(arrayOf<PageSource?>(application.getPageSource()), false, null)
            if (rl != null) {
                requestedPage = rl.execute(pc, requestedPage)
                if (requestedPage == null) return
            }

            // request
            try {
                pci._doInclude(arrayOf<PageSource?>(requestedPage), false, null)
            } catch (mie: MissingIncludeException) {
                val ac: ApplicationContext = pc.getApplicationContext()
                var rethrow = true
                if (ac is ClassicApplicationContext) {
                    val udf: UDF = ac!!.getOnMissingTemplate()
                    if (udf != null) {
                        val targetPage: String = requestedPage.getRealpathWithVirtual()
                        rethrow = !Caster.toBooleanValue(udf.call(pc, arrayOf(targetPage), true), true)
                    }
                }
                if (rethrow) throw mie
            }

            // on Request End
            if (application != null) {
                val onReqEnd: PageSource = application.getPageSource().getRealPage(Constants.CFML_CLASSIC_APPLICATION_END_EVENT_HANDLER)
                if (onReqEnd.exists()) pci._doInclude(arrayOf<PageSource?>(onReqEnd), false, null)
            }
        }

        @Throws(PageException::class)
        fun _onDebug(pc: PageContext?) {
            try {
                if (pc.getConfig().debug()) pc.getDebugger().writeOut(pc)
            } catch (e: IOException) {
                throw Caster.toPageException(e)
            }
        }

        fun _onError(pc: PageContext?, pe: PageException?) {
            pc.handlePageException(pe)
        }

        fun _onTimeout(pc: PageContext?) {}
        fun _hasOnApplicationStart(): Boolean {
            return false
        }

        fun _hasOnSessionStart(pc: PageContext?): Boolean {
            return false
        }
    }
}