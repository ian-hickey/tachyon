/**
 *
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
package lucee.runtime.tag

import java.util.Iterator

/**
 * Saves the generated content inside the tag body in a variable.
 *
 *
 *
 */
class Script2 : BodyTagTryCatchFinallyImpl() {
    /** The name of the variable in which to save the generated content inside the tag.  */
    private var language: String? = null
    private val engine: CFMLEngine?
    private var script: String? = null
    @Override
    fun release() {
        super.release()
        language = null
    }

    fun setLanguage(language: String?) {
        this.language = language
    }

    @Override
    fun doStartTag(): Int {
        return EVAL_BODY_BUFFERED
    }

    @Override
    @Throws(PageException::class)
    fun doAfterBody(): Int {
        script = bodyContent.getString()
        bodyContent.clearBody()
        return SKIP_BODY
    }

    @Override
    @Throws(PageException::class)
    fun doEndTag(): Int {
        val engine: ScriptEngine? = scriptEngine
        val bindings: Bindings = engine.createBindings()
        try {
            bindings.put("pageContext", pageContext)
            bindings.put("application", pageContext.applicationScope())
            bindings.put("session", pageContext.sessionScope())
            bindings.put("request", pageContext.requestScope())
            bindings.put("variables", pageContext.variablesScope())
            bindings.put("caster", this.engine.getCastUtil())
            // TODO more
            engine.setBindings(bindings, ScriptContext.ENGINE_SCOPE)
            engine.eval(script)
        } catch (e: ScriptException) {
            throw this.engine.getCastUtil().toPageException(e)
        }
        // remove all presets
        bindings.remove("pageContext")
        bindings.remove("application")
        bindings.remove("session")
        bindings.remove("request")
        bindings.remove("variables")
        bindings.remove("caster")
        pageContext.setVariable("cfscript", bindings)
        return EVAL_PAGE
    }
    // get engine by engine name

    // get engine by language name
    @get:Throws(PageException::class)
    val scriptEngine: ScriptEngine?
        get() {
            val manager = ScriptEngineManager()
            var engine: ScriptEngine = manager.getEngineByName(language)

            // get engine by engine name
            if (engine == null) {
                val it: Iterator<ScriptEngineFactory?> = manager.getEngineFactories().iterator()
                var factory: ScriptEngineFactory?
                while (it.hasNext()) {
                    factory = it.next()
                    if (language.equalsIgnoreCase(factory.getEngineName())) {
                        engine = factory.getScriptEngine()
                        break
                    }
                }
            }

            // get engine by language name
            if (engine == null) {
                val it: Iterator<ScriptEngineFactory?> = manager.getEngineFactories().iterator()
                var factory: ScriptEngineFactory?
                while (it.hasNext()) {
                    factory = it.next()
                    if (language.equalsIgnoreCase(factory.getEngineName())) {
                        engine = factory.getScriptEngine()
                        break
                    }
                }
            }
            if (engine == null) {
                val it: Iterator<ScriptEngineFactory?> = manager.getEngineFactories().iterator()
                var factory: ScriptEngineFactory?
                val sb = StringBuilder()
                while (it.hasNext()) {
                    factory = it.next()
                    if (sb.length() > 0) sb.append(',')
                    sb.append(factory.getEngineName())
                }
                throw this.engine.getExceptionUtil().createApplicationException("language [$language] is not supported, supported languages are [$sb]")
            }
            return engine
        }

    init {
        engine = CFMLEngineFactory.getInstance()
    }
}