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
package com.intergral.fusiondebug.server

import lucee.loader.engine.CFMLEngineFactory

/**
 *
 */
object FDControllerFactory {
    var complete: Long = 0
    fun notifyPageComplete() {
        complete++
    }

    /**
     * returns a singelton instance of the class
     *
     * @return singelton instance
     */
    val instance: Object
        get() = CFMLEngineFactory.getInstance().getFDController()

    /**
     * makes the class visible for the FD Client
     */
    fun makeVisible() {
        // this method does nothing, only make this class visible for the FD Client
    }

    // make sure FD see this class
    init {
        try {
            CFMLEngineFactory.getInstance().getClassUtil().loadClass("com.intergral.fusiondebug.server.FDSignalException")
        } catch (t: Throwable) {
            t.printStackTrace()
        }
    }
}