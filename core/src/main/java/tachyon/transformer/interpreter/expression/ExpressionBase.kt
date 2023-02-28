package tachyon.transformer.interpreter.expression

import tachyon.runtime.exp.PageException

/**
 * An Expression (Operation, Literal aso.)
 */
abstract class ExpressionBase(factory: Factory?, start: Position?, end: Position?) : Expression {
    private var start: Position?
    private var end: Position?
    private val factory: Factory?
    @Throws(TransformerException::class)
    fun writeOut(c: Context?, mode: Int): Class<*>? {
        return try {
            _writeOut(c as InterpreterContext?, mode)
        } catch (e: PageException) {
            // MUST make better different exception type with interface
            throw PageRuntimeException(e)
        }
    }

    @Throws(PageException::class)
    abstract fun _writeOut(ic: InterpreterContext?, mode: Int): Class<*>?
    @Override
    fun getFactory(): Factory? {
        return factory
    }

    @Override
    fun getStart(): Position? {
        return start
    }

    @Override
    fun getEnd(): Position? {
        return end
    }

    @Override
    fun setStart(start: Position?) {
        this.start = start
    }

    @Override
    fun setEnd(end: Position?) {
        this.end = end
    }

    class Result(type: Class<*>?, value: Object?) {
        var type: Class<*>?
        var value: Object?

        init {
            this.type = type
            this.value = value
        }
    }

    init {
        this.start = start
        this.end = end
        this.factory = factory
    }
}