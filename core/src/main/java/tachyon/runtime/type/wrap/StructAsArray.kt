package tachyon.runtime.type.wrap

import java.util.Comparator

class StructAsArray private constructor(sct: Struct?) : ArraySupport(), Array, List {
    private val sct: Struct?
    @Override
    fun duplicate(dc: Boolean): Collection? {
        return StructAsArray(sct.duplicate(dc) as Struct)
    }

    @Override
    @Throws(PageException::class)
    operator fun get(k: String?): Object? {
        return sct.get(k)
    }

    @Override
    @Throws(PageException::class)
    operator fun get(k: Key?): Object? {
        return sct.get(k)
    }

    @Override
    @Throws(PageException::class)
    operator fun get(pc: PageContext?, k: Key?): Object? {
        return sct.get(pc, k)
    }

    @Override
    operator fun get(k: String?, defaultValue: Object?): Object? {
        return sct.get(k, defaultValue)
    }

    @Override
    operator fun get(k: Key?, defaultValue: Object?): Object? {
        return sct.get(k, defaultValue)
    }

    @Override
    operator fun get(pc: PageContext?, k: Key?, defaultValue: Object?): Object? {
        return sct.get(pc, k, defaultValue)
    }

    @Override
    fun keys(): Array<Key?>? {
        return sct.keys()
    }

    @Override
    @Throws(PageException::class)
    fun remove(k: Key?): Object? {
        return sct.remove(k)
    }

    @Override
    fun removeEL(k: Key?): Object? {
        return sct.removeEL(k)
    }

    @Override
    @Throws(PageException::class)
    operator fun set(k: String?, value: Object?): Object? {
        if (!Decision.isInteger(k)) throw ExpressionException("can't cast struct to an array, key [$k] is not a number")
        return sct.set(k, value)
    }

    @Override
    @Throws(PageException::class)
    operator fun set(k: Key?, value: Object?): Object? {
        if (!Decision.isInteger(k.getString())) throw ExpressionException("can't cast struct to an array, key [$k] is not a number")
        return sct.set(k, value)
    }

    @Override
    fun setEL(k: String?, value: Object?): Object? {
        return if (Decision.isInteger(k)) sct.setEL(k, value) else value
    }

    @Override
    fun setEL(k: Key?, value: Object?): Object? {
        return if (Decision.isInteger(k)) sct.setEL(k, value) else value
    }

    @Override
    fun toDumpData(pc: PageContext?, arg1: Int, arg2: DumpProperties?): DumpData? {
        val dd: DumpData = sct.toDumpData(pc, arg1, arg2)
        if (dd is DumpTable) {
            val dt: DumpTable = dd as DumpTable
            dt.setTitle(dt.getTitle().toString() + " as Array")
        }
        return dd
    }

    @Override
    fun entryIterator(): Iterator<Entry<Key?, Object?>?>? {
        return sct.entryIterator()
    }

    @Override
    fun keyIterator(): Iterator<Key?>? {
        return sct.keyIterator()
    }

    @Override
    fun keysAsStringIterator(): Iterator<String?>? {
        return sct.keysAsStringIterator()
    }

    @Override
    @Throws(PageException::class)
    fun append(v: Object?): Object? {
        var newKey = 1
        val keys = intKeys()
        for (k in keys!!) {
            if (k >= newKey) newKey = k + 1
        }
        return setE(newKey, v)
    }

    @Override
    fun appendEL(v: Object?): Object? {
        var newKey = 1
        val keys = intKeys()
        for (k in keys!!) {
            if (k >= newKey) newKey = k + 1
        }
        return set(newKey, v)
    }

    @Override
    operator fun get(key: Int, defaultValue: Object?): Object? {
        return sct.get(KeyImpl.toKey(key), defaultValue)
    }

    @Override
    fun getDimension(): Int {
        return 1
    }

    @Override
    @Throws(PageException::class)
    fun getE(key: Int): Object? {
        return sct.get(KeyImpl.toKey(key))
    }

    @Override
    @Throws(PageException::class)
    fun insert(index: Int, value: Object?): Boolean {
        // TODO make a better impl
        val arr: Array? = asTempArray()
        val res: Boolean = arr.insert(index, value)
        storeBack(arr)
        return res
    }

    @Override
    fun intKeys(): IntArray? {
        val it: Iterator<Key?> = sct.keyIterator()
        val indexes = IntArray(sct.size())
        var index = 0
        try {
            while (it.hasNext()) {
                indexes[index++] = Caster.toIntValue(it.next().getString())
            }
        } catch (ee: ExpressionException) {
            throw PageRuntimeException(ee)
        }
        return indexes
    }

    @Override
    @Throws(PageException::class)
    fun prepend(value: Object?): Object? {
        // TODO make a better impl
        val arr: Array? = asTempArray()
        val res: Object = arr.prepend(value)
        storeBack(arr)
        return res
    }

    @Override
    @Throws(PageException::class)
    fun removeE(k: Int): Object? {
        return remove(KeyImpl.toKey(k))
    }

    @Override
    fun removeEL(k: Int): Object? {
        return removeEL(KeyImpl.toKey(k))
    }

    @Override
    @Throws(PageException::class)
    fun pop(): Object? {
        return removeE(size())
    }

    @Override
    fun pop(defaultValue: Object?): Object? {
        return try {
            removeE(size())
        } catch (e: Exception) {
            defaultValue
        }
    }

    @Override
    @Throws(PageException::class)
    fun shift(): Object? {
        return removeE(1)
    }

    @Override
    fun shift(defaultValue: Object?): Object? {
        return try {
            removeE(1)
        } catch (e: Exception) {
            defaultValue
        }
    }

    @Override
    @Throws(PageException::class)
    fun resize(newSize: Int) {
        // with structs not necessary
    }

    @Override
    @Throws(PageException::class)
    fun setE(k: Int, value: Object?): Object? {
        return set(KeyImpl.toKey(k), value)
    }

    @Override
    fun setEL(k: Int, value: Object?): Object? {
        return setEL(KeyImpl.toKey(k), value)
    }

    @Override
    fun sortIt(c: Comparator?) {
    }

    @Override
    fun size(): Int {
        return sct.size()
    }

    @Throws(PageException::class)
    private fun asTempArray(): Array? {
        val arr: Array = ArrayImpl()
        val it: Iterator<Entry<Key?, Object?>?> = sct.entryIterator()
        var e: Entry<Key?, Object?>? = null
        try {
            while (it.hasNext()) {
                e = it.next()
                arr.setE(Caster.toIntValue(e.getKey().getString()), e.getValue())
            }
        } catch (ee: ExpressionException) {
            throw ExpressionException("can't cast struct to an array, key [" + e.getKey().getString().toString() + "] is not a number")
        }
        return arr
    }

    private fun storeBack(arr: Array?) {
        sct.clear()
        val it: Iterator<Entry<Key?, Object?>?> = arr.entryIterator()
        var e: Entry<Key?, Object?>?
        while (it.hasNext()) {
            e = it.next()
            sct.setEL(e.getKey(), e.getValue())
        }
    }

    companion object {
        @Throws(ExpressionException::class)
        fun toArray(sct: Struct?): Array? {
            if (sct is Array) return sct
            val it: Iterator<Key?> = sct.keyIterator()
            var k: Key?
            while (it.hasNext()) {
                k = it.next()
                if (!Decision.isInteger(k.getString())) throw ExpressionException("can't cast struct to an array, key [" + k.getString().toString() + "] is not a number")
            }
            return StructAsArray(sct)
        }

        fun toArray(sct: Struct?, defaultValue: Array?): Array? {
            if (sct is Array) return sct
            val it: Iterator<Key?> = sct.keyIterator()
            var k: Key?
            while (it.hasNext()) {
                k = it.next()
                if (!Decision.isInteger(k.getString())) return defaultValue
            }
            return StructAsArray(sct)
        }
    }

    init {
        this.sct = sct
    }
}