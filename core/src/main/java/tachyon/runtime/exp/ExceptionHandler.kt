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
package tachyon.runtime.exp

import java.io.PrintWriter

/**
 * Handle Excpetions
 */
object ExceptionHandler {
    fun log(config: Config?, t: Throwable?) {
        val pe: PageException = Caster.toPageException(t)
        pe.printStackTrace(config.getErrWriter())
        val ll: Int = if (t is MissingIncludeException) Log.LEVEL_WARN else Log.LEVEL_ERROR
        ThreadLocalPageContext.getLog(config, "exception").log(ll, "", pe)
    }

    fun printStackTrace(pc: PageContext?, t: Throwable?) {
        val pw: PrintWriter = pc.getConfig().getErrWriter()
        t.printStackTrace(pw)
        pw.flush()
    }

    fun printStackTrace(t: Throwable?) {
        val pc: PageContext = ThreadLocalPageContext.get()
        if (pc != null) printStackTrace(pc, t) else t.printStackTrace()
    }
}