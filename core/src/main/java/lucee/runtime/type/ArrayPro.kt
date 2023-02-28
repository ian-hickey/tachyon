package lucee.runtime.type

import java.util.Iterator

// FUTURE move to Array
interface ArrayPro : Array {
    fun entryArrayIterator(): Iterator<Entry<Integer?, Object?>?>?

    @Throws(PageException::class)
    fun pop(): Object?
    fun pop(defaultValue: Object?): Object?

    @Throws(PageException::class)
    fun shift(): Object?
    fun shift(defaultValue: Object?): Object?
}