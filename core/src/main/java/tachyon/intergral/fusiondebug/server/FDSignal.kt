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
package tachyon.intergral.fusiondebug.server

import java.util.ArrayList

object FDSignal {
    private val hash: ThreadLocal? = ThreadLocal()
    fun signal(pe: PageException?, caught: Boolean) {
        try {
            val id: String = pe.hashCode().toString() + ":" + caught
            if (Caster.toString(hash.get(), "").equals(id)) return
            val stack: List? = createExceptionStack(pe)
            if (stack.size() > 0) {
                val se = FDSignalException()
                se.setExceptionStack(stack)
                se.setRuntimeExceptionCaughtStatus(caught)
                se.setRuntimeExceptionExpression(createRuntimeExceptionExpression(pe))
                if (pe is NativeException) se.setRuntimeExceptionType("native") else se.setRuntimeExceptionType(pe.getTypeAsString())
                se.setStackTrace(pe.getStackTrace())
                hash.set(id)
                throw se
            }
        } catch (fdse: FDSignalException) {
            // do nothing - will be processed by JDI and handled by FD
        }
    }

    fun createRuntimeExceptionExpression(pe: PageException?): String? {
        return if (!StringUtil.isEmpty(pe.getDetail())) pe.getMessage().toString() + " " + pe.getDetail() else pe.getMessage()
    }

    fun createExceptionStack(pe: PageException?): List? {
        val traces: Array<StackTraceElement?> = pe.getStackTrace()
        val pc: PageContextImpl = ThreadLocalPageContext.get() as PageContextImpl
        var template = ""
        var trace: StackTraceElement? = null
        val list: List = ArrayList()
        var res: Resource
        var ps: PageSource?
        var frame: FDStackFrameImpl?
        for (i in traces.indices.reversed()) {
            trace = traces[i]
            ps = null
            if (trace.getLineNumber() <= 0) continue
            template = trace.getFileName()
            if (template == null || ResourceUtil.getExtension(template, "").equals("java")) continue
            res = ResourceUtil.toResourceNotExisting(pc, template)
            ps = pc.toPageSource(res, null)
            frame = FDStackFrameImpl(null, pc, trace, ps)
            if (ASMUtil.isOverfowMethod(trace.getMethodName())) list.set(0, frame) else list.add(0, frame)
        }
        if (pe is TemplateException) {
            val te: TemplateException? = pe as TemplateException?
            if (te.getPageSource() != null) list.add(0, FDStackFrameImpl(null, pc, te.getPageSource(), te.getLine()))
        }
        return list
    }
}