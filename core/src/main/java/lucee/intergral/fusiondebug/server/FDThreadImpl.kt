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
package lucee.intergral.fusiondebug.server

import java.util.ArrayList

class FDThreadImpl(engine: FDControllerImpl?, factory: CFMLFactoryImpl?, name: String?, pc: PageContextImpl?) : IFDThread {
    private val pc: PageContextImpl?
    private val name: String?
    private val engine: FDControllerImpl?
    @Override
    fun getName(): String? {
        return name.toString() + ":" + pc.getCFID()
    }

    @Override
    fun id(): Int {
        return pc.getId()
    }

    @Override
    fun stop() {
        val log: Log = ThreadLocalPageContext.getLog(pc, "application")
        SystemUtil.stop(pc, true)
    }

    @Override
    fun getThread(): Thread? {
        return pc.getThread()
    }

    @Override
    fun getOutputBuffer(): String? {
        return pc.getRootOut().toString()
    }

    fun getStackFrames(): List<IFDStackFrame?>? {
        return getStack()
    }

    @Override
    fun getStack(): List<IFDStackFrame?>? {
        val traces: Array<StackTraceElement?> = pc.getThread().getStackTrace()
        var template = ""
        var trace: StackTraceElement? = null
        val list: ArrayList<IFDStackFrame?> = ArrayList<IFDStackFrame?>()
        var ps: PageSource?
        for (i in traces.indices.reversed()) {
            trace = traces[i]
            if (trace.getLineNumber() <= 0) continue
            template = trace.getFileName()
            if (template == null || ResourceUtil.getExtension(template, "").equals("java")) continue
            ps = toPageSource(pc, template)
            val frame = FDStackFrameImpl(this, pc, trace, ps)
            if (ASMUtil.isOverfowMethod(trace.getMethodName())) list.set(0, frame) else list.add(0, frame)
        }
        return list
    }

    fun getTopStack(): IFDStackFrame? {
        return getTopStackFrame()
    }

    @Override
    fun getTopStackFrame(): IFDStackFrame? {
        var ps: PageSource? = pc.getCurrentPageSource()
        val traces: Array<StackTraceElement?> = pc.getThread().getStackTrace()
        var template = ""
        var trace: StackTraceElement? = null
        for (i in traces.indices) {
            trace = traces[i]
            if (trace.getLineNumber() <= 0) continue
            template = trace.getFileName()
            if (template == null || ResourceUtil.getExtension(template, "").equals("java")) continue
            if (ps == null || !isEqual(ps, trace)) {
                ps = toPageSource(pc, template)
            }
            break
        }
        return FDStackFrameImpl(this, pc, trace, ps)
    }

    private fun toPageSource(pc2: PageContextImpl?, template: String?): PageSource? {
        val res: Resource = ResourceUtil.toResourceNotExisting(pc, template)
        return pc.toPageSource(res, null)
    }

    private fun isEqual(ps: PageSource?, trace: StackTraceElement?): Boolean {
        // class name do not match
        if (!ps.getClassName().equals(trace.getClassName())) return false
        // filename to not match
        return if (!ps.getResource().getAbsolutePath().equals(trace.getFileName())) false else true
    }

    /**
     * @return the engine
     */
    fun getController(): IFDController? {
        return engine
    }

    companion object {
        fun id(pc: PageContext?): Int {
            return pc.getId()
        }
    }

    // private CFMLFactoryImpl factory;
    init {
        this.engine = engine
        // this.factory=factory;
        this.name = name
        this.pc = pc
    }
}