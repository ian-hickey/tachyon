package tachyon.runtime.pool

import kotlin.Throws
import kotlin.jvm.Synchronized
import tachyon.commons.io.SystemUtil.Caller
import kotlin.jvm.Transient
import kotlin.jvm.JvmOverloads
import kotlin.jvm.Volatile
import tachyon.commons.collection.concurrent.ConcurrentHashMapNullSupport.EntrySet
import tachyon.commons.collection.concurrent.ConcurrentHashMapNullSupport.EntryIterator
import tachyon.commons.collection.LongKeyList.Pair
import tachyon.commons.collection.AbstractCollection
import tachyon.runtime.type.Array
import java.sql.Array
import tachyon.commons.lang.Pair
import tachyon.runtime.exp.CatchBlockImpl.Pair
import tachyon.runtime.type.util.ListIteratorImpl
import tachyon.runtime.type.Lambda
import java.util.Random
import tachyon.runtime.config.Constants
import tachyon.runtime.engine.Request
import tachyon.runtime.engine.ExecutionLogSupport.Pair
import tachyon.runtime.functions.other.NullValue
import tachyon.runtime.functions.string.Val
import tachyon.runtime.reflection.Reflector.JavaAnnotation
import tachyon.transformer.cfml.evaluator.impl.Output
import tachyon.transformer.cfml.evaluator.impl.Property
import tachyon.transformer.bytecode.statement.Condition.Pair

class PoolItemWrap(value: PoolItem?) {
    private val value: PoolItem?
    private var last: Long
    fun setLastAccess(last: Long): PoolItemWrap? {
        this.last = last
        return this
    }

    fun lastAccess(): Long {
        return last
    }

    fun getValue(): PoolItem? {
        return value
    }

    @Throws(Exception::class)
    fun start() {
        value!!.start()
    }

    @Throws(Exception::class)
    fun end() {
        value!!.end()
    }

    init {
        this.value = value
        last = System.currentTimeMillis()
    }
}