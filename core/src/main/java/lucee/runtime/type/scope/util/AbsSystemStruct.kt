package lucee.runtime.type.scope.util

import java.util.Iterator

abstract class AbsSystemStruct : StructSupport() {
    @Override
    @Throws(PageException::class)
    fun remove(key: Key?): Object? {
        return removeEL(key)
    }

    @Override
    fun duplicate(deepCopy: Boolean): Collection? {
        val sct: Struct = StructImpl()
        StructImpl.copy(this, sct, deepCopy)
        return sct
    }

    @Override
    fun keyIterator(): Iterator<Key?>? {
        return KeyIterator(keys())
    }

    @Override
    fun valueIterator(): Iterator<Object?>? {
        return ValueIterator(this, keys())
    }

    @Override
    fun entryIterator(): Iterator<Entry<Key?, Object?>?>? {
        return EntryIterator(this, keys())
    }
}