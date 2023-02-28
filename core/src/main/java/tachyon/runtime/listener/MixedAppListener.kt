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

import tachyon.commons.io.res.Resource

/**
 * this class is only used by the CFML dialect, so checking for the dialect is not necessary
 */
class MixedAppListener : ModernAppListener() {
    @Override
    @Throws(PageException::class)
    override fun onRequest(pc: PageContext?, requestedPage: PageSource?, rl: RequestListener?) {
        val isCFC: RefBoolean = RefBooleanImpl(false)
        val appP: Page? = getApplicationPage(pc, requestedPage, mode, isCFC)
        if (isCFC.toBooleanValue()) _onRequest(pc, requestedPage, appP, rl) else ClassicAppListener._onRequest(pc, requestedPage, appP, rl)
    }

    @Override
    override fun getType(): String? {
        return "mixed"
    }

    @Override
    @Throws(PageException::class)
    override fun onDebug(pc: PageContext?) {
        if ((pc as PageContextImpl?).getAppListenerType() === ApplicationListener.TYPE_CLASSIC) ClassicAppListener._onDebug(pc) else super.onDebug(pc)
    }

    @Override
    override fun onError(pc: PageContext?, pe: PageException?) {
        if ((pc as PageContextImpl?).getAppListenerType() === ApplicationListener.TYPE_CLASSIC) ClassicAppListener._onError(pc, pe) else super.onError(pc, pe)
    }

    @Override
    override fun hasOnSessionStart(pc: PageContext?): Boolean {
        return if ((pc as PageContextImpl?).getAppListenerType() === ApplicationListener.TYPE_CLASSIC) ClassicAppListener._hasOnSessionStart(pc) else super.hasOnSessionStart(pc)
    }

    @Override
    override fun hasOnApplicationStart(): Boolean {
        val pc: PageContext = ThreadLocalPageContext.get()
        return if (pc != null && (pc as PageContextImpl).getAppListenerType() === ApplicationListener.TYPE_CLASSIC) ClassicAppListener._hasOnApplicationStart() else super.hasOnApplicationStart()
    }

    @Override
    override fun onTimeout(pc: PageContext?) {
        if ((pc as PageContextImpl?).getAppListenerType() === ApplicationListener.TYPE_CLASSIC) ClassicAppListener._onTimeout(pc) else super.onTimeout(pc)
    }

    companion object {
        @Throws(PageException::class)
        private fun getApplicationPage(pc: PageContext?, requestedPage: PageSource?, mode: Int, isCFC: RefBoolean?): Page? {
            val ps: PageSource
            val res: Resource = requestedPage.getPhyscalFile()
            if (res != null) {
                ps = (pc.getConfig() as ConfigPro).getApplicationPageSource(pc, res.getParent(), "Application.[cfc|cfm]", mode, isCFC)
                if (ps != null) {
                    if (ps.exists()) return ps.loadPage(pc, false)
                }
            }
            val p: Page
            p = if (mode == ApplicationListener.MODE_CURRENT2ROOT) getApplicationPageCurrToRoot(pc, requestedPage, isCFC) else if (mode == ApplicationListener.MODE_CURRENT_OR_ROOT) getApplicationPageCurrOrRoot(pc, requestedPage, isCFC) else if (mode == ApplicationListener.MODE_CURRENT) getApplicationPageCurrent(pc, requestedPage, isCFC) else getApplicationPageRoot(pc, isCFC)
            if (res != null && p != null) (pc.getConfig() as ConfigPro).putApplicationPageSource(requestedPage.getPhyscalFile().getParent(), p.getPageSource(), "Application.[cfc|cfm]",
                    mode, isCFC.toBooleanValue())
            return p
        }

        @Throws(PageException::class)
        private fun getApplicationPageCurrent(pc: PageContext?, requestedPage: PageSource?, isCFC: RefBoolean?): Page? {
            var ps: PageSource = requestedPage.getRealPage(Constants.CFML_APPLICATION_EVENT_HANDLER)
            if (ps != null) {
                var p: Page? = null
                if (ps.exists()) p = ps.loadPage(pc, false)
                if (p != null) {
                    isCFC.setValue(true)
                    return p
                }
            }
            ps = requestedPage.getRealPage(Constants.CFML_CLASSIC_APPLICATION_EVENT_HANDLER)
            return if (ps != null && ps.exists()) {
                ps.loadPage(pc, false)
            } else null
        }

        @Throws(PageException::class)
        private fun getApplicationPageCurrToRoot(pc: PageContext?, requestedPage: PageSource?, isCFC: RefBoolean?): Page? {
            var p: Page? = getApplicationPageCurrent(pc, requestedPage, isCFC)
            if (p != null) return p
            val arr: Array = tachyon.runtime.type.util.ListUtil.listToArrayRemoveEmpty(requestedPage.getRealpathWithVirtual(), "/")
            // Config config = pc.getConfig();
            var path: String
            var ps: PageSource
            for (i in arr.size() - 1 downTo 1) {
                val sb = StringBuilder("/")
                for (y in 1 until i) {
                    sb.append(arr.get(y, "") as String)
                    sb.append('/')
                }
                path = sb.toString()
                ps = (pc as PageContextImpl?).getPageSource(path.concat(Constants.CFML_APPLICATION_EVENT_HANDLER))
                if (ps != null) {
                    p = null
                    if (ps.exists()) p = ps.loadPage(pc, false)
                    if (p != null) {
                        isCFC.setValue(true)
                        return p
                    }
                }
                ps = (pc as PageContextImpl?).getPageSource(path.concat(Constants.CFML_CLASSIC_APPLICATION_EVENT_HANDLER))
                if (ps != null) {
                    p = null
                    if (ps.exists()) p = ps.loadPage(pc, false)
                    if (p != null) {
                        return p
                    }
                }
            }
            return null
        }

        @Throws(PageException::class)
        private fun getApplicationPageCurrOrRoot(pc: PageContext?, requestedPage: PageSource?, isCFC: RefBoolean?): Page? {
            // current
            val p: Page? = getApplicationPageCurrent(pc, requestedPage, isCFC)
            return if (p != null) p else getApplicationPageRoot(pc, isCFC)

            // root
        }

        @Throws(PageException::class)
        private fun getApplicationPageRoot(pc: PageContext?, isCFC: RefBoolean?): Page? {
            var ps: PageSource = (pc as PageContextImpl?).getPageSource("/" + Constants.CFML_APPLICATION_EVENT_HANDLER)
            if (ps != null) {
                var p: Page? = null
                if (ps.exists()) p = ps.loadPage(pc, false)
                if (p != null) {
                    isCFC.setValue(true)
                    return p
                }
            }
            ps = (pc as PageContextImpl?).getPageSource("/" + Constants.CFML_CLASSIC_APPLICATION_EVENT_HANDLER)
            if (ps != null) {
                var p: Page? = null
                if (ps.exists()) p = ps.loadPage(pc, false)
                if (p != null) {
                    return p
                }
            }
            return null
        }
    }
}