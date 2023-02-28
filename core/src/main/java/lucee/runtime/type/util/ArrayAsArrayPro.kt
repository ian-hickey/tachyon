package lucee.runtime.type.util

import java.util.Comparator

class ArrayAsArrayPro(array: Array?) : ArrayPro, Cloneable {
    private val array: Array?
    @Override
    fun entryArrayIterator(): Iterator<Entry<Integer?, Object?>?>? {
        return EntryArrayIterator(array, array.intKeys())
    }

    @Override
    @Throws(PageException::class)
    fun pop(): Object? {
        return array.removeE(size())
    }

    @Override
    fun pop(defaultValue: Object?): Object? {
        return try {
            array.removeE(size())
        } catch (e: Exception) {
            defaultValue
        }
    }

    @Override
    @Throws(PageException::class)
    fun shift(): Object? {
        return array.removeE(1)
    }

    @Override
    fun shift(defaultValue: Object?): Object? {
        return try {
            array.removeE(1)
        } catch (e: Exception) {
            defaultValue
        }
    }

    /////////////////////////// array methods //////////////////////////////
    @Override
    fun clone(): Object {
        return array.duplicate(true)
    }

    @Override
    @Throws(PageException::class)
    fun append(arg0: Object?): Object? {
        return array.append(arg0)
    }

    @Override
    fun appendEL(arg0: Object?): Object? {
        return array.appendEL(arg0)
    }

    @Override
    fun containsKey(arg0: Int): Boolean {
        return array.containsKey(arg0)
    }

    @Override
    operator fun get(arg0: Int, arg1: Object?): Object? {
        return array.get(arg0, arg1)
    }

    @get:Override
    val dimension: Int
        get() = array.getDimension()

    @Override
    @Throws(PageException::class)
    fun getE(arg0: Int): Object? {
        return array.getE(arg0)
    }

    @Override
    @Throws(PageException::class)
    fun insert(arg0: Int, arg1: Object?): Boolean {
        return array.insert(arg0, arg1)
    }

    @Override
    fun intKeys(): IntArray? {
        return array.intKeys()
    }

    @Override
    @Throws(PageException::class)
    fun prepend(arg0: Object?): Object? {
        return array.prepend(arg0)
    }

    @Override
    @Throws(PageException::class)
    fun removeE(arg0: Int): Object? {
        return array.removeE(arg0)
    }

    @Override
    fun removeEL(arg0: Int): Object? {
        return array.removeEL(arg0)
    }

    @Override
    @Throws(PageException::class)
    fun resize(arg0: Int) {
        array.resize(arg0)
    }

    @Override
    @Throws(PageException::class)
    fun setE(arg0: Int, arg1: Object?): Object? {
        return array.setE(arg0, arg1)
    }

    @Override
    fun setEL(arg0: Int, arg1: Object?): Object? {
        return array.setEL(arg0, arg1)
    }

    @Override
    @Throws(PageException::class)
    fun sort(arg0: String?, arg1: String?) {
        array.sort(arg0, arg1)
    }

    @Override
    fun sortIt(arg0: Comparator?) {
        array.sortIt(arg0)
    }

    @Override
    fun toArray(): Array<Object?>? {
        return array.toArray()
    }

    @Override
    fun toList(): List? {
        return array.toList()
    }

    @Override
    fun clear() {
        array.clear()
    }

    @Override
    fun containsKey(arg0: String?): Boolean {
        return array.containsKey(arg0)
    }

    @Override
    fun containsKey(arg0: Key?): Boolean {
        return array.containsKey(arg0)
    }

    @Override
    fun duplicate(arg0: Boolean): Collection? {
        return array.duplicate(arg0)
    }

    @Override
    @Throws(PageException::class)
    operator fun get(arg0: String?): Object? {
        return array.get(arg0)
    }

    @Override
    @Throws(PageException::class)
    operator fun get(arg0: Key?): Object? {
        return array.get(arg0)
    }

    @Override
    operator fun get(arg0: String?, arg1: Object?): Object? {
        return array.get(arg0, arg1)
    }

    @Override
    operator fun get(arg0: Key?, arg1: Object?): Object? {
        return array.get(arg0, arg1)
    }

    @Override
    fun keys(): Array<Key?>? {
        return array.keys()
    }

    @Override
    @Throws(PageException::class)
    fun remove(arg0: Key?): Object? {
        return array.remove(arg0)
    }

    @Override
    fun remove(arg0: Key?, arg1: Object?): Object? {
        return array.remove(arg0, arg1)
    }

    @Override
    fun removeEL(arg0: Key?): Object? {
        return array.removeEL(arg0)
    }

    @Override
    @Throws(PageException::class)
    operator fun set(arg0: String?, arg1: Object?): Object? {
        return array.set(arg0, arg1)
    }

    @Override
    @Throws(PageException::class)
    operator fun set(arg0: Key?, arg1: Object?): Object? {
        return array.set(arg0, arg1)
    }

    @Override
    fun setEL(arg0: String?, arg1: Object?): Object? {
        return array.setEL(arg0, arg1)
    }

    @Override
    fun setEL(arg0: Key?, arg1: Object?): Object? {
        return array.setEL(arg0, arg1)
    }

    @Override
    fun size(): Int {
        return array.size()
    }

    @Override
    fun toDumpData(arg0: PageContext?, arg1: Int, arg2: DumpProperties?): DumpData? {
        return array.toDumpData(arg0, arg1, arg2)
    }

    @Override
    fun entryIterator(): Iterator<Entry<Key?, Object?>?>? {
        return array.entryIterator()
    }

    @Override
    fun keyIterator(): Iterator<Key?>? {
        return array.keyIterator()
    }

    @Override
    fun keysAsStringIterator(): Iterator<String?>? {
        return array.keysAsStringIterator()
    }

    @Override
    fun valueIterator(): Iterator<Object?>? {
        return array.valueIterator()
    }

    @Override
    fun castToBoolean(arg0: Boolean?): Boolean? {
        return array.castToBoolean(arg0)
    }

    @Override
    @Throws(PageException::class)
    fun castToBooleanValue(): Boolean {
        return array.castToBooleanValue()
    }

    @Override
    @Throws(PageException::class)
    fun castToDateTime(): DateTime? {
        return array.castToDateTime()
    }

    @Override
    fun castToDateTime(arg0: DateTime?): DateTime? {
        return array.castToDateTime(arg0)
    }

    @Override
    @Throws(PageException::class)
    fun castToDoubleValue(): Double {
        return array.castToDoubleValue()
    }

    @Override
    fun castToDoubleValue(arg0: Double): Double {
        return array.castToDoubleValue(arg0)
    }

    @Override
    @Throws(PageException::class)
    fun castToString(): String? {
        return array.castToString()
    }

    @Override
    fun castToString(arg0: String?): String? {
        return array.castToString(arg0)
    }

    @Override
    @Throws(PageException::class)
    operator fun compareTo(arg0: String?): Int {
        return array.compareTo(arg0)
    }

    @Override
    @Throws(PageException::class)
    operator fun compareTo(arg0: Boolean): Int {
        return array.compareTo(arg0)
    }

    @Override
    @Throws(PageException::class)
    operator fun compareTo(arg0: Double): Int {
        return array.compareTo(arg0)
    }

    @Override
    @Throws(PageException::class)
    operator fun compareTo(arg0: DateTime?): Int {
        return array.compareTo(arg0)
    }

    @Override
    @Throws(PageException::class)
    fun call(arg0: PageContext?, arg1: Key?, arg2: Array<Object?>?): Object? {
        return array.call(arg0, arg1, arg2)
    }

    @Override
    @Throws(PageException::class)
    fun callWithNamedValues(arg0: PageContext?, arg1: Key?, arg2: Struct?): Object? {
        return array.callWithNamedValues(arg0, arg1, arg2)
    }

    @Override
    @Throws(PageException::class)
    operator fun get(arg0: PageContext?, arg1: Key?): Object? {
        return array.get(arg0, arg1)
    }

    @Override
    operator fun get(arg0: PageContext?, arg1: Key?, arg2: Object?): Object? {
        return array.get(arg0, arg1, arg2)
    }

    @Override
    @Throws(PageException::class)
    operator fun set(arg0: PageContext?, arg1: Key?, arg2: Object?): Object? {
        return array.set(arg0, arg1, arg2)
    }

    @Override
    fun setEL(arg0: PageContext?, arg1: Key?, arg2: Object?): Object? {
        return array.setEL(arg0, arg1, arg2)
    }

    @get:Override
    val iterator: Iterator<*>?
        get() = array.getIterator()

    init {
        this.array = array
    }
}