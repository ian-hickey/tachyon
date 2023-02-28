package lucee.runtime.jsr223

import java.io.Reader

/**
 * Provides a standard implementation for several of the variants of the `eval` method.
 * <br></br>
 * <br></br>
 * `**eval(Reader)**`
 *
 *
 * `**eval(String)**`
 *
 *
 * `**eval(String, Bindings)**`
 *
 *
 * `**eval(Reader, Bindings)**` <br></br>
 * <br></br>
 * are implemented using the abstract methods <br></br>
 * <br></br>
 * `**eval(Reader,ScriptContext)**` or `**eval(String, ScriptContext)**`
 * <br></br>
 * <br></br>
 * with a `SimpleScriptContext`. <br></br>
 * <br></br>
 * A `SimpleScriptContext` is used as the default `ScriptContext` of the
 * `AbstractScriptEngine`..
 *
 * @author Mike Grogan
 * @since 1.6
 */
abstract class AbstractScriptEngine : ScriptEngine {
    /**
     * The default `ScriptContext` of this `AbstractScriptEngine`.
     */
    protected var context: ScriptContext?

    /**
     * Sets the value of the protected `context` field to the specified
     * `ScriptContext`.
     *
     * @param ctxt The specified `ScriptContext`.
     * @throws NullPointerException if ctxt is null.
     */
    @Override
    fun setContext(ctxt: ScriptContext?) {
        if (ctxt == null) {
            throw NullPointerException("null context")
        }
        context = ctxt
    }

    /**
     * Returns the value of the protected `context` field.
     *
     * @return The value of the protected `context` field.
     */
    @Override
    fun getContext(): ScriptContext? {
        return context
    }

    /**
     * Returns the `Bindings` with the specified scope value in the protected
     * `context` field.
     *
     * @param scope The specified scope
     *
     * @return The corresponding `Bindings`.
     *
     * @throws IllegalArgumentException if the value of scope is invalid for the type the protected
     * `context` field.
     */
    @Override
    fun getBindings(scope: Int): Bindings? {
        return if (scope == ScriptContext.GLOBAL_SCOPE) {
            context.getBindings(ScriptContext.GLOBAL_SCOPE)
        } else if (scope == ScriptContext.ENGINE_SCOPE) {
            context.getBindings(ScriptContext.ENGINE_SCOPE)
        } else {
            throw IllegalArgumentException("Invalid scope value.")
        }
    }

    /**
     * Sets the `Bindings` with the corresponding scope value in the `context`
     * field.
     *
     * @param bindings The specified `Bindings`.
     * @param scope The specified scope.
     *
     * @throws IllegalArgumentException if the value of scope is invalid for the type the
     * `context` field.
     * @throws NullPointerException if the bindings is null and the scope is
     * `ScriptContext.ENGINE_SCOPE`
     */
    @Override
    fun setBindings(bindings: Bindings?, scope: Int) {
        if (scope == ScriptContext.GLOBAL_SCOPE) {
            context.setBindings(bindings, ScriptContext.GLOBAL_SCOPE)
        } else if (scope == ScriptContext.ENGINE_SCOPE) {
            context.setBindings(bindings, ScriptContext.ENGINE_SCOPE)
        } else {
            throw IllegalArgumentException("Invalid scope value.")
        }
    }

    /**
     * Sets the specified value with the specified key in the `ENGINE_SCOPE`
     * `Bindings` of the protected `context` field.
     *
     * @param key The specified key.
     * @param value The specified value.
     *
     * @throws NullPointerException if key is null.
     * @throws IllegalArgumentException if key is empty.
     */
    @Override
    fun put(key: String?, value: Object?) {
        val nn: Bindings? = getBindings(ScriptContext.ENGINE_SCOPE)
        if (nn != null) {
            nn.put(key, value)
        }
    }

    /**
     * Gets the value for the specified key in the `ENGINE_SCOPE` of the protected
     * `context` field.
     *
     * @return The value for the specified key.
     *
     * @throws NullPointerException if key is null.
     * @throws IllegalArgumentException if key is empty.
     */
    @Override
    operator fun get(key: String?): Object? {
        val nn: Bindings? = getBindings(ScriptContext.ENGINE_SCOPE)
        return if (nn != null) {
            nn.get(key)
        } else null
    }

    /**
     * `eval(Reader, Bindings)` calls the abstract `eval(Reader, ScriptContext)`
     * method, passing it a `ScriptContext` whose Reader, Writers and Bindings for scopes
     * other that `ENGINE_SCOPE` are identical to those members of the protected
     * `context` field. The specified `Bindings` is used instead of the
     * `ENGINE_SCOPE`
     *
     * `Bindings` of the `context` field.
     *
     * @param reader A `Reader` containing the source of the script.
     * @param bindings A `Bindings` to use for the `ENGINE_SCOPE` while the script
     * executes.
     *
     * @return The return value from `eval(Reader, ScriptContext)`
     * @throws ScriptException if an error occurs in script.
     * @throws NullPointerException if any of the parameters is null.
     */
    @Override
    @Throws(ScriptException::class)
    fun eval(reader: Reader?, bindings: Bindings?): Object? {
        val ctxt: ScriptContext? = getScriptContext(bindings)
        return eval(reader, ctxt)
    }

    /**
     * Same as `eval(Reader, Bindings)` except that the abstract
     * `eval(String, ScriptContext)` is used.
     *
     * @param script A `String` containing the source of the script.
     *
     * @param bindings A `Bindings` to use as the `ENGINE_SCOPE` while the script
     * executes.
     *
     * @return The return value from `eval(String, ScriptContext)`
     * @throws ScriptException if an error occurs in script.
     * @throws NullPointerException if any of the parameters is null.
     */
    @Override
    @Throws(ScriptException::class)
    fun eval(script: String?, bindings: Bindings?): Object? {
        val ctxt: ScriptContext? = getScriptContext(bindings)
        return eval(script, ctxt)
    }

    /**
     * `eval(Reader)` calls the abstract `eval(Reader, ScriptContext)` passing the
     * value of the `context` field.
     *
     * @param reader A `Reader` containing the source of the script.
     * @return The return value from `eval(Reader, ScriptContext)`
     * @throws ScriptException if an error occurs in script.
     * @throws NullPointerException if any of the parameters is null.
     */
    @Override
    @Throws(ScriptException::class)
    fun eval(reader: Reader?): Object? {
        return eval(reader, context)
    }

    /**
     * Same as `eval(Reader)` except that the abstract
     * `eval(String, ScriptContext)` is used.
     *
     * @param script A `String` containing the source of the script.
     * @return The return value from `eval(String, ScriptContext)`
     * @throws ScriptException if an error occurrs in script.
     * @throws NullPointerException if any of the parameters is null.
     */
    @Override
    @Throws(ScriptException::class)
    fun eval(script: String?): Object? {
        return eval(script, context)
    }

    /**
     * Returns a `SimpleScriptContext`. The `SimpleScriptContext`: <br></br>
     * <br></br>
     *
     *  * Uses the specified `Bindings` for its `ENGINE_SCOPE`
     *  * Uses the `Bindings` returned by the abstract `getGlobalScope` method as
     * its `GLOBAL_SCOPE`
     *  * Uses the Reader and Writer in the default `ScriptContext` of this
     * `ScriptEngine`
     *
     * <br></br>
     * <br></br>
     * A `SimpleScriptContext` returned by this method is used to implement eval methods
     * using the abstract `eval(Reader,Bindings)` and `eval(String,Bindings)`
     * versions.
     *
     * @param nn Bindings to use for the `ENGINE_SCOPE`
     * @return The `SimpleScriptContext`
     */
    protected fun getScriptContext(nn: Bindings?): ScriptContext? {
        val ctxt = SimpleScriptContext()
        val gs: Bindings? = getBindings(ScriptContext.GLOBAL_SCOPE)
        if (gs != null) {
            ctxt.setBindings(gs, ScriptContext.GLOBAL_SCOPE)
        }
        if (nn != null) {
            ctxt.setBindings(nn, ScriptContext.ENGINE_SCOPE)
        } else {
            throw NullPointerException("Engine scope Bindings may not be null.")
        }
        ctxt.setReader(context.getReader())
        ctxt.setWriter(context.getWriter())
        ctxt.setErrorWriter(context.getErrorWriter())
        return ctxt
    }

    /**
     * Creates a new instance of AbstractScriptEngine using a `SimpleScriptContext` as its
     * default `ScriptContext`.
     */
    init {
        context = SimpleScriptContext()
    }
}