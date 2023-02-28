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

import java.util.Map

class DebugExecutionLog : ExecutionLogSupport() {
    private var pc: PageContext? = null
    @Override
    protected override fun _init(pc: PageContext?, arguments: Map<String?, String?>?) {
        this.pc = pc
    }

    @Override
    protected override fun _log(startPos: Int, endPos: Int, startTime: Long, endTime: Long) {
        if (!pc.getConfig().debug()) return
        var diff = endTime - startTime
        if (unit === UNIT_MICRO) diff /= 1000 else if (unit === UNIT_MILLI) diff /= 1000000
        val de: DebugEntry = pc.getDebugger().getEntry(pc, pc.getCurrentPageSource(), startPos, endPos)
        de.updateExeTime(diff.toInt())
    }

    @Override
    protected override fun _release() {
    }
}