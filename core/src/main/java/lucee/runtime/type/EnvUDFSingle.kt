package lucee.runtime.type

import java.util.function.Consumer

// TODO DoublePredicate,IntPredicate,LongPredicate
abstract class EnvUDFSingle : EnvUDF, ToIntFunction, ToLongFunction, ToDoubleFunction, Consumer, LongConsumer, IntConsumer, UnaryOperator, DoubleUnaryOperator, IntUnaryOperator, IntFunction, Function, LongFunction, Predicate, DoubleConsumer, DoubleFunction, DoubleToIntFunction, DoubleToLongFunction, IntToDoubleFunction, IntToLongFunction, LongToDoubleFunction, LongToIntFunction, LongUnaryOperator {
    constructor() : super() {}
    internal constructor(properties: UDFProperties?) : super(properties) {}
    internal constructor(properties: UDFProperties?, variables: Variables?) : super(properties, variables) {}

    @Override
    fun applyAsInt(value: Object?): Int {
        return try {
            Caster.toIntValue(call(ThreadLocalPageContext.get(), arrayOf<Object?>(value), true))
        } catch (pe: PageException) {
            throw PageRuntimeException(pe)
        }
    }

    @Override
    fun applyAsInt(value: Double): Int {
        return applyAsInt(Double.valueOf(value))
    }

    @Override
    fun applyAsInt(value: Int): Int {
        return applyAsInt(Integer.valueOf(value))
    }

    @Override
    fun applyAsInt(value: Long): Int {
        return applyAsInt(Long.valueOf(value))
    }

    @Override
    fun applyAsDouble(value: Object?): Double {
        return try {
            Caster.toDoubleValue(call(ThreadLocalPageContext.get(), arrayOf<Object?>(value), true))
        } catch (pe: PageException) {
            throw PageRuntimeException(pe)
        }
    }

    @Override
    fun applyAsDouble(value: Double): Double {
        return applyAsDouble(Double.valueOf(value))
    }

    @Override
    fun applyAsDouble(value: Int): Double {
        return applyAsDouble(Integer.valueOf(value))
    }

    @Override
    fun applyAsDouble(value: Long): Double {
        return applyAsDouble(Long.valueOf(value))
    }

    @Override
    fun applyAsLong(value: Object?): Long {
        return try {
            Caster.toLongValue(call(ThreadLocalPageContext.get(), arrayOf<Object?>(value), true))
        } catch (pe: PageException) {
            throw PageRuntimeException(pe)
        }
    }

    @Override
    fun applyAsLong(value: Double): Long {
        return applyAsLong(Double.valueOf(value))
    }

    @Override
    fun applyAsLong(value: Int): Long {
        return applyAsLong(Integer.valueOf(value))
    }

    @Override
    fun applyAsLong(value: Long): Long {
        return applyAsLong(Long.valueOf(value))
    }

    @Override
    fun accept(t: Object?) {
        try {
            call(ThreadLocalPageContext.get(), arrayOf<Object?>(t), true)
        } catch (pe: PageException) {
            throw PageRuntimeException(pe)
        }
    }

    @Override
    fun accept(value: Int) {
        accept(Integer.valueOf(value))
    }

    @Override
    fun accept(value: Long) {
        accept(Long.valueOf(value))
    }

    @Override
    fun accept(value: Double) {
        accept(Double.valueOf(value))
    }

    @Override
    fun apply(t: Object?): Object? {
        return try {
            call(ThreadLocalPageContext.get(), arrayOf<Object?>(t), true)
        } catch (pe: PageException) {
            throw PageRuntimeException(pe)
        }
    }

    @Override
    fun apply(value: Double): Object? {
        return apply(Double.valueOf(value))
    }

    @Override
    fun apply(value: Long): Object? {
        return apply(Long.valueOf(value))
    }

    @Override
    fun apply(value: Int): Object? {
        return apply(Integer.valueOf(value))
    }

    @Override
    fun test(t: Object?): Boolean {
        return try {
            Caster.toBooleanValue(call(ThreadLocalPageContext.get(), arrayOf<Object?>(t), true))
        } catch (pe: PageException) {
            throw PageRuntimeException(pe)
        }
    }
}