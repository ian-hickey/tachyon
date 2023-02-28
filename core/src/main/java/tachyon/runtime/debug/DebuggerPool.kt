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
package tachyon.runtime.debug

import java.util.Iterator

class DebuggerPool  // private List<Debugger> list=new ArrayList<Debugger>();
(storage: Resource?) {
    // private Resource storage;
    private val queue: LinkedList<Struct?>? = LinkedList<Struct?>()
    fun store(pc: PageContext?, debugger: Debugger?) {
        if (ReqRspUtil.getScriptName(pc, pc.getHttpServletRequest()).indexOf("/tachyon/") === 0) return
        synchronized(queue) {
            try {
                queue.add(Duplicator.duplicate(debugger.getDebuggingData(pc, true), true) as Struct)
            } catch (e: PageException) {
            }
            while (queue.size() > (pc.getConfig() as ConfigWebPro).getDebugMaxRecordsLogged()) queue.poll()
        }
    }

    fun getData(pc: PageContext?): Array? {
        var it: Iterator<Struct?>
        synchronized(queue) { it = queue.iterator() }
        val arr: Array = ArrayImpl()
        while (it.hasNext()) {
            arr.appendEL(it.next())
        }
        return arr
    }

    fun purge() {
        synchronized(queue) { queue.clear() }
    }
}