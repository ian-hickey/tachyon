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
package coldfusion.cfc

import java.io.File

class CFCProxy(path: String?, initialThis: Map?, invokeDirectly: Boolean) {
    private val engine: CFMLEngine
    private val caster: Cast
    private val creator: Creation
    private var cfc: Component? = null
    private val path: String?
    private var thisData: Map? = null
    private val invokeDirectly = true
    private var autoFlush = false

    constructor(path: String?) : this(path, null, true) {}
    constructor(path: String?, invokeDirectly: Boolean) : this(path, null, invokeDirectly) {}
    constructor(path: String?, initialThis: Map?) : this(path, initialThis, true) {}

    private fun initCFC(pc: PageContext?) {
        var pc: PageContext? = pc
        if (cfc == null && (invokeDirectly || pc != null)) try {
            if (pc == null) pc = engine.getThreadPageContext()
            cfc = engine.getCreationUtil().createComponentFromPath(pc, path)
        } catch (pe: PageException) {
        }
    }

    @SuppressWarnings("rawtypes")
    fun setThisScope(data: Map?) {
        if (data != null) {
            if (thisData == null) thisData = HashMap()
            val it: Iterator<Entry> = data.entrySet().iterator()
            var entry: Entry
            while (it.hasNext()) {
                entry = it.next()
                thisData.put(entry.getKey(), entry.getValue())
            }
        }
    }

    @SuppressWarnings("rawtypes")
    fun getThisScope(): Map? {
        initCFC(null)
        if (cfc == null) return null
        val rtn: Struct = creator.createStruct()
        val it: Iterator<Entry<Key, Object>> = cfc.entryIterator()
        var entry: Entry<Key, Object>
        while (it.hasNext()) {
            entry = it.next()
            rtn.setEL(entry.getKey(), entry.getValue())
        }
        return rtn
    }

    @Throws(Throwable::class)
    operator fun invoke(methodName: String, args: Array<Object>): Object {
        return if (invokeDirectly) _invoke(methodName, args) else _invoke(methodName, args, null, null, null)
    }

    @Throws(Throwable::class)
    operator fun invoke(methodName: String, args: Array<Object>, request: HttpServletRequest?, response: HttpServletResponse?): Object {
        return if (invokeDirectly) _invoke(methodName, args) else _invoke(methodName, args, request, response, null)
    }

    @Throws(Throwable::class)
    operator fun invoke(methodName: String, args: Array<Object>, request: HttpServletRequest?, response: HttpServletResponse?, out: OutputStream?): Object {
        return if (invokeDirectly) _invoke(methodName, args) else _invoke(methodName, args, request, response, out)
    }

    @Throws(PageException::class)
    private fun _invoke(methodName: String, args: Array<Object>): Object {
        val engine: CFMLEngine = CFMLEngineFactory.getInstance()
        val pc: PageContext = engine.getThreadPageContext()
        initCFC(pc)
        return cfc.call(pc, methodName, args)
    }

    @Throws(PageException::class)
    private fun _invoke(methodName: String, args: Array<Object>, req: HttpServletRequest?, rsp: HttpServletResponse?, out: OutputStream?): Object {
        var req: HttpServletRequest? = req
        var rsp: HttpServletResponse? = rsp
        var out: OutputStream? = out
        val engine: CFMLEngine = CFMLEngineFactory.getInstance()
        val creator: Creation = engine.getCreationUtil()
        val originalPC: PageContext = engine.getThreadPageContext()

        // no OutputStream
        if (out == null) out = DevNullOutputStream.DEV_NULL_OUTPUT_STREAM

        // no Request
        if (req == null) // TODO new File
            req = creator.createHttpServletRequest(File("."), "Lucee", "/", "", null, null, null, null, null)
        // noRespone
        if (rsp == null) rsp = creator.createHttpServletResponse(out)
        val pc: PageContext = creator.createPageContext(req, rsp, out)
        return try {
            engine.registerThreadPageContext(pc)
            initCFC(pc)
            cfc.call(pc, methodName, args)
        } finally {
            if (autoFlush) try {
                pc.getRootWriter().flush()
            } catch (t: Throwable) {
            }
            engine.registerThreadPageContext(originalPC)
        }
    }

    @Throws(IOException::class)
    fun flush() {
        val engine: CFMLEngine = CFMLEngineFactory.getInstance()
        val pc: PageContext = engine.getThreadPageContext()
        pc.getRootWriter().flush()
    }

    fun setAutoFlush(autoFlush: Boolean) {
        this.autoFlush = autoFlush
    }

    fun setApplicationExecution(doApp: Boolean) {
        // executeApplication = doApp;
    }

    companion object {
        fun inInvoke(): Boolean {
            return false
        }
    }

    init {
        engine = CFMLEngineFactory.getInstance()
        caster = engine.getCastUtil()
        creator = engine.getCreationUtil()
        this.path = path
        this.invokeDirectly = invokeDirectly
        setThisScope(initialThis)
    }
}

internal class DevNullOutputStream
/**
 * Constructor of the class
 */
private constructor() : OutputStream(), Serializable {
    /**
     * @see java.io.OutputStream.close
     */
    @Override
    fun close() {
    }

    /**
     * @see java.io.OutputStream.flush
     */
    @Override
    fun flush() {
    }

    /**
     * @see java.io.OutputStream.write
     */
    @Override
    fun write(b: ByteArray?, off: Int, len: Int) {
    }

    /**
     * @see java.io.OutputStream.write
     */
    @Override
    fun write(b: ByteArray?) {
    }

    /**
     * @see java.io.OutputStream.write
     */
    @Override
    fun write(b: Int) {
    }

    companion object {
        private const val serialVersionUID = -4707810151743493285L
        val DEV_NULL_OUTPUT_STREAM = DevNullOutputStream()
    }
}