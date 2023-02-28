package tachyon.runtime.type

import java.util.Comparator

abstract class EnvUDFMulti : EnvUDF, Comparator, ToIntBiFunction, ToLongBiFunction, ToDoubleBiFunction, BiConsumer, BiFunction, BiPredicate, BinaryOperator, LongBinaryOperator, DoubleBinaryOperator, IntBinaryOperator, ObjDoubleConsumer, ObjIntConsumer, ObjLongConsumer {
    constructor() : super() {}
    internal constructor(properties: UDFProperties?) : super(properties) {}
    internal constructor(properties: UDFProperties?, variables: Variables?) : super(properties, variables) {}

    @Override
    fun compare(o1: Object?, o2: Object?): Int {
        return try {
            Caster.toIntValue(call(ThreadLocalPageContext.get(), arrayOf<Object?>(o1, o2), true))
        } catch (pe: PageException) {
            throw PageRuntimeException(pe)
        }
    }

    @Override
    fun applyAsDouble(t: Object?, u: Object?): Double {
        return try {
            Caster.toDoubleValue(call(ThreadLocalPageContext.get(), arrayOf<Object?>(t, u), true))
        } catch (pe: PageException) {
            throw PageRuntimeException(pe)
        }
    }

    @Override
    fun applyAsLong(t: Object?, u: Object?): Long {
        return try {
            Caster.toLongValue(call(ThreadLocalPageContext.get(), arrayOf<Object?>(t, u), true))
        } catch (pe: PageException) {
            throw PageRuntimeException(pe)
        }
    }

    @Override
    fun applyAsLong(left: Long, right: Long): Long {
        return applyAsLong(Long.valueOf(left), Long.valueOf(right))
    }

    @Override
    fun applyAsInt(t: Object?, u: Object?): Int {
        return try {
            Caster.toIntValue(call(ThreadLocalPageContext.get(), arrayOf<Object?>(t, u), true))
        } catch (pe: PageException) {
            throw PageRuntimeException(pe)
        }
    }

    @Override
    fun applyAsInt(left: Int, right: Int): Int {
        return applyAsInt(Double.valueOf(left), Double.valueOf(right))
    }

    @Override
    fun accept(t: Object?, u: Object?) {
        try {
            call(ThreadLocalPageContext.get(), arrayOf<Object?>(t, u), true)
        } catch (pe: PageException) {
            throw PageRuntimeException(pe)
        }
    }

    @Override
    fun accept(t: Object?, u: Double) {
        accept(t, Double.valueOf(u))
    }

    @Override
    fun accept(t: Object?, u: Int) {
        accept(t, Integer.valueOf(u))
    }

    @Override
    fun accept(t: Object?, u: Long) {
        accept(t, Long.valueOf(u))
    }

    @Override
    fun apply(t: Object?, u: Object?): Object? {
        return try {
            call(ThreadLocalPageContext.get(), arrayOf<Object?>(t, u), true)
        } catch (pe: PageException) {
            throw PageRuntimeException(pe)
        }
    }

    @Override
    fun applyAsDouble(left: Double, right: Double): Double {
        return try {
            Caster.toDoubleValue(call(ThreadLocalPageContext.get(), arrayOf(left, right), true))
        } catch (pe: PageException) {
            throw PageRuntimeException(pe)
        }
    }

    @Override
    fun test(t: Object?, u: Object?): Boolean {
        return try {
            Caster.toBooleanValue(call(ThreadLocalPageContext.get(), arrayOf<Object?>(t, u), true))
        } catch (pe: PageException) {
            throw PageRuntimeException(pe)
        }
    }
}