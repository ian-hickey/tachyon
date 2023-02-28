/**
 * Copyright (c) 2014, the Railo Company Ltd.
 * Copyright (c) 2015, Lucee Assosication Switzerland
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
package lucee.runtime.listener

import lucee.runtime.CFMLFactory

/**
 * interface for PageContext to interact with CFML
 *
 */
interface ApplicationListener {
    fun setMode(mode: Int)
    fun getMode(): Int

    /**
     * @return the type
     */
    fun getType(): String?

    /**
     * this method will be called the Application self
     *
     * @param pc Page Context
     * @param requestedPage Requested Page
     * @param rl Request Listener
     * @throws PageException Page Exception
     */
    @Throws(PageException::class)
    fun onRequest(pc: PageContext?, requestedPage: PageSource?, rl: RequestListener?)

    /**
     * this method will be called when a new Session starts
     *
     * @param pc Page Context
     * @throws PageException Page Exception
     */
    @Throws(PageException::class)
    fun onSessionStart(pc: PageContext?)

    /**
     * this method will be called when a Session ends
     *
     * @param cfmlFactory CFML Factory
     * @param applicationName Application Name
     * @param cfid cfid
     * @throws PageException Page Exception
     */
    @Throws(PageException::class)
    fun onSessionEnd(cfmlFactory: CFMLFactory?, applicationName: String?, cfid: String?)

    /**
     * this method will be called when a new Application Context starts
     *
     * @param pc Page Context
     * @return success or failure
     * @throws PageException Page Exception
     */
    @Throws(PageException::class)
    fun onApplicationStart(pc: PageContext?): Boolean

    /**
     * this method will be called when an Application scope ends
     *
     * @param cfmlFactory CFML Factory
     * @param applicationName Application Name
     * @throws PageException Page Exception
     */
    @Throws(PageException::class)
    fun onApplicationEnd(cfmlFactory: CFMLFactory?, applicationName: String?)

    /**
     * this method will be called when a Server starts
     *
     * @throws PageException Page Exception
     */
    @Throws(PageException::class)
    fun onServerStart()

    /**
     * this method will be called when the Server shutdown correctly (no crashes)
     *
     * @throws PageException Page Exception
     */
    @Throws(PageException::class)
    fun onServerEnd()

    /**
     * this method will be called if Server has an error (exception) not thrown by a try-catch block
     *
     * @param pc Page Context
     * @param pe PageException Exception that has been thrown
     */
    fun onError(pc: PageContext?, pe: PageException?)

    /**
     * called after "onRequestEnd" to generate debugging output, will only be called when debugging is
     * enabled
     *
     * @param pc Page Context
     * @throws PageException Page Exception
     */
    @Throws(PageException::class)
    fun onDebug(pc: PageContext?)

    /**
     * will be called when the Server runs into a timeout
     *
     * @param pc Page Context
     */
    fun onTimeout(pc: PageContext?)
    fun hasOnApplicationStart(): Boolean
    fun hasOnSessionStart(pc: PageContext?): Boolean

    companion object {
        const val MODE_CURRENT2ROOT = 0
        const val MODE_CURRENT = 1
        const val MODE_ROOT = 2
        const val MODE_CURRENT_OR_ROOT = 4
        const val TYPE_NONE = 0
        const val TYPE_CLASSIC = 1
        const val TYPE_MODERN = 2
        const val TYPE_MIXED = 4
        const val CFC_EXTENSION = "cfc"
    }
}