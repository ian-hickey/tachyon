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

import java.util.Iterator

class ExecutionLogFactory(clazz: Class?, arguments: Map<String?, String?>?) {
    private val clazz: Class?
    private val arguments: Map<String?, String?>?
    fun getInstance(pc: PageContext?): ExecutionLog? {
        var el: ExecutionLog?
        try {
            el = ClassUtil.newInstance(clazz) as ExecutionLog
        } catch (e: Exception) {
            el = ConsoleExecutionLog()
        }
        el.init(pc, arguments)
        return el
    }

    @Override
    override fun toString(): String {
        return super.toString() + ":" + clazz.getName()
    }

    fun getClazz(): Class? {
        return clazz
    }

    fun getArgumentsAsStruct(): Struct? {
        val sct = StructImpl()
        if (arguments != null) {
            val it: Iterator<Entry<String?, String?>?> = arguments.entrySet().iterator()
            var e: Entry<String?, String?>?
            while (it.hasNext()) {
                e = it.next()
                sct.setEL(e.getKey(), e.getValue())
            }
        }
        return sct
    }

    // private ExecutionLog executionLog;
    init {
        this.clazz = clazz
        this.arguments = arguments
    }
}