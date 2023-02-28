package tachyon.runtime.type.scope.storage

import java.io.Serializable

class IKStorageScopeItem(value: Object?, lastModified: Long) : Serializable, ObjectWrap, Castable {
    private var value: Object?
    private var lastModifed: Long
    private var removed = false

    constructor(value: Object?) : this(value, System.currentTimeMillis()) {}

    fun getValue(): Object? {
        return value
    }

    @Override
    fun getEmbededObject(): Object? {
        return value
    }

    @Override
    fun getEmbededObject(defaultValue: Object?): Object? {
        return value
    }

    // needed for containsValue
    @Override
    override fun equals(o: Object?): Boolean {
        return value.equals(o)
    }

    fun remove(): Object? {
        return remove(System.currentTimeMillis())
    }

    fun remove(lastMod: Long): Object? {
        lastModifed = lastMod
        val v: Object? = value
        value = null
        removed = true
        return v
    }

    fun removed(): Boolean {
        return removed
    }

    fun lastModified(): Long {
        return lastModifed
    }

    @Override
    fun castToBoolean(df: Boolean?): Boolean? {
        return Caster.toBoolean(getValue(), df)
    }

    @Override
    @Throws(PageException::class)
    fun castToBooleanValue(): Boolean {
        return Caster.toBoolean(getValue())
    }

    @Override
    @Throws(PageException::class)
    fun castToDateTime(): DateTime? {
        return Caster.toDate(getValue(), true, null)
    }

    @Override
    fun castToDateTime(df: DateTime?): DateTime? {
        return Caster.toDate(getValue(), true, null, df)
    }

    @Override
    @Throws(PageException::class)
    fun castToDoubleValue(): Double {
        return Caster.toDoubleValue(getValue())
    }

    @Override
    fun castToDoubleValue(df: Double): Double {
        return Caster.toDoubleValue(getValue(), false, df)
    }

    @Override
    @Throws(PageException::class)
    fun castToString(): String? {
        return Caster.toString(getValue())
    }

    @Override
    fun castToString(df: String?): String? {
        return Caster.toString(getValue(), df)
    }

    @Override
    @Throws(PageException::class)
    operator fun compareTo(str: String?): Int {
        return OpUtil.compare(ThreadLocalPageContext.get(), getValue(), str)
    }

    @Override
    @Throws(PageException::class)
    operator fun compareTo(b: Boolean): Int {
        return OpUtil.compare(ThreadLocalPageContext.get(), getValue(), if (b) Boolean.TRUE else Boolean.FALSE)
    }

    @Override
    @Throws(PageException::class)
    operator fun compareTo(d: Double): Int {
        return OpUtil.compare(ThreadLocalPageContext.get(), getValue(), Double.valueOf(d))
    }

    @Override
    @Throws(PageException::class)
    operator fun compareTo(dt: DateTime?): Int {
        return OpUtil.compare(ThreadLocalPageContext.get(), getValue(), dt as Date?)
    }

    @Override
    override fun toString(): String {
        return getValue().toString() + ""
    }

    companion object {
        private const val serialVersionUID = -8187816208907138226L
    }

    // DO NOT CHANGE, USED BY REDIS EXTENSION
    init {
        this.value = value
        lastModifed = lastModified
    }
}