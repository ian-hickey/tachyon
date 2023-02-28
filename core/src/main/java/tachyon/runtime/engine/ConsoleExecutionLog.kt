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
package tachyon.runtime.engine

import java.io.PrintWriter

class ConsoleExecutionLog : ExecutionLogSupport() {
    private var pw: PrintWriter? = null
    private var pc: PageContext? = null
    @Override
    protected override fun _init(pc: PageContext?, arguments: Map<String?, String?>?) {
        this.pc = pc
        if (pw == null) {
            // stream type
            val type = arguments!!["stream-type"]
            if (type != null && type.trim().equalsIgnoreCase("error")) pw = PrintWriter(System.err) else pw = PrintWriter(System.out)
        }
    }

    @Override
    protected override fun _log(startPos: Int, endPos: Int, startTime: Long, endTime: Long) {
        val diff = endTime - startTime
        LogUtil.log(pc, Log.LEVEL_TRACE, Controler::class.java.getName(),
                pc.getId().toString() + ":" + pc.getCurrentPageSource().getDisplayPath() + ":" + positons(startPos, endPos) + " > " + timeLongToString(diff))
    }

    @Override
    protected override fun _release() {
    }

    companion object {
        private fun positons(startPos: Int, endPos: Int): String? {
            return if (startPos == endPos) startPos.toString() + "" else "$startPos:$endPos"
        }
    }
}