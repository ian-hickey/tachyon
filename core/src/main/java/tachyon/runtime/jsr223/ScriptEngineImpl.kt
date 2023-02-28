/**
 * Copyright (c) 2015, Tachyon Assosication Switzerland. All rights reserved.
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
package tachyon.runtime.jsr223

import java.io.File

class ScriptEngineImpl(factory: ScriptEngineFactoryImpl?) : ScriptEngine {
    private val factory: ScriptEngineFactoryImpl?
    private var context: ScriptContext? = null
    private var _pc: PageContext? = null
    private val token: Object? = SerializableObject()
    @Override
    @Throws(ScriptException::class)
    fun eval(script: String?, context: ScriptContext?): Object? {
        var context: ScriptContext? = context
        if (context == null) context = getContext()
        val printExceptions: Boolean = Caster.toBooleanValue(SystemUtil.getSystemPropOrEnvVar("tachyon.cli.printExceptions", null), false)
        val oldPC: PageContext = ThreadLocalPageContext.get()
        val pc: PageContext? = getPageContext(context)
        return try {
            val res: Result = if (factory!!.tag) Renderer.tag(pc, script, factory!!.dialect, false, true) else Renderer.script(pc, script, factory!!.dialect, false, true)
            res.getValue()
        } catch (pe: PageException) {
            if (printExceptions) {
                pe.printStackTrace()
            }
            throw toScriptException(pe)
        } catch (re: RuntimeException) {
            if (printExceptions) {
                re.printStackTrace()
            }
            throw re
        } catch (t: Throwable) {
            if (printExceptions) {
                if (t is ThreadDeath) throw t as ThreadDeath
                t.printStackTrace()
            }
            throw RuntimeException(t)
        } finally {
            releasePageContext(pc, oldPC)
        }
    }

    @Override
    fun put(key: String?, value: Object?) {
        val oldPC: PageContext = ThreadLocalPageContext.get()
        val pc: PageContext? = getPageContext(getContext())
        try {
            pc.undefinedScope().set(KeyImpl.init(key), value)
        } catch (e: PageException) {
            // ignored
        } finally {
            releasePageContext(pc, oldPC)
        }
    }

    @Override
    operator fun get(key: String?): Object? {
        val oldPC: PageContext = ThreadLocalPageContext.get()
        val pc: PageContext? = getPageContext(getContext())
        return try {
            pc.undefinedScope().get(KeyImpl.init(key), null)
        } finally {
            releasePageContext(pc, oldPC)
        }
    }

    @Override
    fun getBindings(scope: Int): Bindings? {
        return getContext().getBindings(scope)
    }

    @Override
    fun setBindings(bindings: Bindings?, scope: Int) {
        getContext().setBindings(bindings, scope)
    }

    private fun toScriptException(e: Exception?): ScriptException? {
        val se = ScriptException(e)
        se.setStackTrace(e.getStackTrace())
        return se
    }

    @Override
    fun getContext(): ScriptContext? {
        if (context == null) {
            context = SimpleScriptContext()
            context.setBindings(VariablesBinding(), ScriptContext.ENGINE_SCOPE) // we do our own
        }
        return context
    }

    private fun getContext(b: Bindings?): ScriptContext? {
        val def: ScriptContext? = getContext()
        val custom = SimpleScriptContext()
        val gs: Bindings? = getBindings(ScriptContext.GLOBAL_SCOPE)
        if (gs != null) custom.setBindings(gs, ScriptContext.GLOBAL_SCOPE)
        custom.setBindings(b, ScriptContext.ENGINE_SCOPE)
        custom.setReader(def.getReader())
        custom.setWriter(def.getWriter())
        custom.setErrorWriter(def.getErrorWriter())
        return custom
    }

    @Override
    fun setContext(context: ScriptContext?) {
        this.context = context
    }

    @Override
    fun createBindings(): Bindings? {
        return VariablesBinding()
    }

    private fun getPageContext(context: ScriptContext?): PageContext? {
        val pc: PageContext? = _getPageContext(true)
        pc.setVariablesScope(toVariables(context.getBindings(ScriptContext.ENGINE_SCOPE)))
        ThreadLocalPageContext.register(pc)
        return pc
    }

    private fun _getPageContext(throwOnError: Boolean): PageContext? {
        if (_pc == null) {
            synchronized(token) {
                if (_pc == null) {
                    try {
                        val root = File(factory!!.engine.getCFMLEngineFactory().getResourceRoot(), "jsr223-webroot")
                        _pc = PageContextUtil.getPageContext(null, null, root, "localhost", "/index.cfm", "", null, null, null, null, CFMLEngineImpl.CONSOLE_OUT, false,
                                Long.MAX_VALUE, Caster.toBooleanValue(SystemUtil.getSystemPropOrEnvVar("tachyon.ignore.scopes", null), false))
                    } catch (e: Exception) {
                        if (throwOnError) throw RuntimeException(e)
                    }
                }
            }
        }
        return _pc
    }

    private fun releasePageContext(pc: PageContext?, oldPC: PageContext?) {
        pc.flush()
        ThreadLocalPageContext.release()
        if (oldPC != null) ThreadLocalPageContext.register(oldPC)
    }

    private fun toVariables(bindings: Bindings?): Variables? {
        if (bindings is VariablesBinding) return (bindings as VariablesBinding?)!!.getVaraibles()
        val t = RuntimeException("not supported! " + bindings.getClass().getName())
        throw t
        // return new BindingsAsVariables(bindings);
    }

    ///////////// calling other methods of the same class /////////////////
    @Override
    @Throws(ScriptException::class)
    fun eval(reader: Reader?, context: ScriptContext?): Object? {
        return try {
            eval(IOUtil.toString(reader), context)
        } catch (ioe: IOException) {
            throw toScriptException(ioe)
        }
    }

    @Override
    @Throws(ScriptException::class)
    fun eval(script: String?): Object? {
        return eval(script, getContext())
    }

    @Override
    @Throws(ScriptException::class)
    fun eval(reader: Reader?): Object? {
        return eval(reader, getContext())
    }

    @Override
    @Throws(ScriptException::class)
    fun eval(script: String?, b: Bindings?): Object? { // TODO
        return eval(script, getContext(b))
    }

    @Override
    @Throws(ScriptException::class)
    fun eval(reader: Reader?, b: Bindings?): Object? { // TODO
        return eval(reader, getContext(b))
    }

    @Override
    fun getFactory(): ScriptEngineFactory? {
        return factory
    }

    init {
        this.factory = factory
        _getPageContext(false)
    }
}