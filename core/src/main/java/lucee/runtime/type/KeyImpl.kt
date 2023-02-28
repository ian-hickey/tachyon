/**
 * Copyright (c) 2014, the Railo Company Ltd.
 * Copyright (c) 2015, Lucee Assosication Switzerland
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library.  If not, see <http:></http:>//www.gnu.org/licenses/>.
 *
 */
package lucee.runtime.type

import java.io.Externalizable

class KeyImpl : Collection.Key, Castable, Comparable, Externalizable, WangJenkins, CharSequence {
    // private boolean intern;
    private var key: String? = null

    @Transient
    private var lcKey: String? = null

    @Transient
    private var ucKey: String? = null

    @Transient
    private var wjh = 0

    @Transient
    private var sfm = -1

    @Transient
    private var h64: Long = 0

    constructor() {
        // DO NOT USE, JUST FOR UNSERIALIZE
    }

    @Override
    fun wangJenkinsHash(): Int {
        if (wjh == 0) {
            var h = hashCode()
            h += h shl 15 xor -0x3283
            h = h xor (h ushr 10)
            h += h shl 3
            h = h xor (h ushr 6)
            h += (h shl 2) + (h shl 14)
            wjh = h xor (h ushr 16)
        }
        return wjh
    }

    fun slotForMap(): Int {
        if (sfm == -1) {
            var h = 0
            h = h xor hashCode()
            h = h xor (h ushr 20 xor (h ushr 12))
            sfm = h xor (h ushr 7) xor (h ushr 4)
        }
        return sfm
    }

    @Override
    @Throws(IOException::class)
    fun writeExternal(out: ObjectOutput?) {
        out.writeObject(key)
    }

    @Override
    @Throws(IOException::class, ClassNotFoundException::class)
    fun readExternal(`in`: ObjectInput?) {
        key = `in`.readObject()
        ucKey = key.toUpperCase()
        h64 = createHash64(ucKey)
    }

    constructor(key: String?) {
        this.key = key
        ucKey = key.toUpperCase()
        h64 = createHash64(ucKey)
    }

    @Override
    fun charAt(index: Int): Char {
        return key.charAt(index)
    }

    @Override
    fun lowerCharAt(index: Int): Char {
        return getLowerString().charAt(index)
    }

    @Override
    fun upperCharAt(index: Int): Char {
        return ucKey.charAt(index)
    }

    @Override
    fun getLowerString(): String? {
        if (lcKey == null) lcKey = StringUtil.toLowerCase(key)
        return lcKey
    }

    @Override
    fun getUpperString(): String? {
        return ucKey
    }

    @Override
    override fun toString(): String {
        return key!!
    }

    @Override
    fun getString(): String? {
        return key
    }

    @Override
    override fun equals(other: Object?): Boolean {
        if (this === other) return true
        if (other is KeyImpl) {
            return hash() == (other as KeyImpl?)!!.hash()
        }
        if (other is String) {
            return key.equalsIgnoreCase(other as String?)
        }
        return if (other is Key) {
            // Both strings are guaranteed to be upper case
            ucKey!!.equals((other as Key?).getUpperString())
        } else false
    }

    @Override
    fun equalsIgnoreCase(other: Key?): Boolean {
        if (this === other) return true
        return if (other is KeyImpl) {
            h64 == (other as KeyImpl?)!!.h64 // return lcKey.equals((((KeyImpl)other).lcKey));
        } else ucKey.equalsIgnoreCase(other.getLowerString())
    }

    @Override
    override fun hashCode(): Int {
        return ucKey!!.hashCode()
    }

    @Override
    fun hash(): Long {
        return h64
    }

    @Override
    @Throws(PageException::class)
    fun castToBooleanValue(): Boolean {
        return Caster.toBooleanValue(key)
    }

    @Override
    fun castToBoolean(defaultValue: Boolean?): Boolean? {
        return Caster.toBoolean(key, defaultValue)
    }

    @Override
    @Throws(PageException::class)
    fun castToDateTime(): DateTime? {
        return Caster.toDatetime(key, null)
    }

    @Override
    fun castToDateTime(defaultValue: DateTime?): DateTime? {
        return DateCaster.toDateAdvanced(key, DateCaster.CONVERTING_TYPE_OFFSET, null, defaultValue)
    }

    @Override
    @Throws(PageException::class)
    fun castToDoubleValue(): Double {
        return Caster.toDoubleValue(key)
    }

    @Override
    fun castToDoubleValue(defaultValue: Double): Double {
        return Caster.toDoubleValue(key, defaultValue)
    }

    @Override
    @Throws(PageException::class)
    fun castToString(): String? {
        return key
    }

    @Override
    fun castToString(defaultValue: String?): String? {
        return key
    }

    @Override
    @Throws(PageException::class)
    operator fun compareTo(b: Boolean): Int {
        return OpUtil.compare(ThreadLocalPageContext.get(), key, if (b) Boolean.TRUE else Boolean.FALSE)
    }

    @Override
    @Throws(PageException::class)
    operator fun compareTo(dt: DateTime?): Int {
        return OpUtil.compare(ThreadLocalPageContext.get(), key, dt as Date?)
    }

    @Override
    @Throws(PageException::class)
    operator fun compareTo(d: Double): Int {
        return OpUtil.compare(ThreadLocalPageContext.get(), key, Double.valueOf(d))
    }

    @Override
    @Throws(PageException::class)
    operator fun compareTo(str: String?): Int {
        return OpUtil.compare(ThreadLocalPageContext.get(), key, str)
    }

    @Override
    operator fun compareTo(o: Object?): Int {
        return try {
            OpUtil.compare(ThreadLocalPageContext.get(), key, o)
        } catch (e: PageException) {
            val cce = ClassCastException(e.getMessage())
            cce.setStackTrace(e.getStackTrace())
            throw cce
        }
    }

    @Override
    fun length(): Int {
        return key!!.length()
    }

    @Override
    override fun subSequence(start: Int, end: Int): CharSequence? {
        return getString()!!.subSequence(start, end)
    }

    companion object {
        private const val serialVersionUID = -8864844181140115609L // do not change
        private val byteTable = createLookupTable()
        private const val HSTART = -0x44bf19b25dfa4f9cL
        private const val HMULT = 7664345821815920749L
        private fun createLookupTable(): LongArray? {
            val _byteTable = LongArray(256)
            var h = 0x544B2FBACAAF1684L
            for (i in 0..255) {
                for (j in 0..30) {
                    h = h ushr 7 xor h
                    h = h shl 11 xor h
                    h = h ushr 10 xor h
                }
                _byteTable[i] = h
            }
            return _byteTable
        }

        fun createHash64(cs: CharSequence?): Long {
            var h = HSTART
            val hmult = HMULT
            val ht = byteTable
            val len: Int = cs!!.length()
            for (i in 0 until len) {
                val ch: Char = cs.charAt(i)
                h = h * hmult xor ht!![ch.toInt() and 0xff]
                h = h * hmult xor ht[ch.toInt() ushr 8 and 0xff]
            }
            return h
        }

        /**
         * for dynamic loading of key objects
         *
         * @param string
         * @return
         */
        fun init(key: String?): Collection.Key? {
            return KeyImpl(key)
        }

        fun _const(key: String?): Collection.Key? {
            return KeyImpl(key)
        }

        fun getInstance(key: String?): Collection.Key? {
            return KeyImpl(key)
        }

        fun intern(key: String?): Collection.Key? {
            return KeyImpl(key)
        }

        fun toUpperCaseArray(keys: Array<Key?>?): Array? {
            val arr = ArrayImpl()
            for (i in keys.indices) {
                arr.appendEL((keys!![i] as KeyImpl?)!!.getUpperString())
            }
            return arr
        }

        fun toLowerCaseArray(keys: Array<Key?>?): Array? {
            val arr = ArrayImpl()
            for (i in keys.indices) {
                arr.appendEL((keys!![i] as KeyImpl?)!!.getLowerString())
            }
            return arr
        }

        fun toArray(keys: Array<Key?>?): Array? {
            val arr = ArrayImpl()
            for (i in keys.indices) {
                arr.appendEL((keys!![i] as KeyImpl?)!!.getString())
            }
            return arr
        }

        fun toUpperCaseList(array: Array<Key?>?, delimiter: String?): String? {
            if (array!!.size == 0) return ""
            val sb = StringBuffer((array[0] as KeyImpl?)!!.getUpperString())
            if (delimiter!!.length() === 1) {
                val c: Char = delimiter.charAt(0)
                for (i in 1 until array.size) {
                    sb.append(c)
                    sb.append((array[i] as KeyImpl?)!!.getUpperString())
                }
            } else {
                for (i in 1 until array.size) {
                    sb.append(delimiter)
                    sb.append((array[i] as KeyImpl?)!!.getUpperString())
                }
            }
            return sb.toString()
        }

        fun toList(array: Array<Key?>?, delimiter: String?): String? {
            if (array!!.size == 0) return ""
            val sb = StringBuilder((array[0] as KeyImpl?)!!.getString())
            if (delimiter!!.length() === 1) {
                val c: Char = delimiter.charAt(0)
                for (i in 1 until array.size) {
                    sb.append(c)
                    sb.append(array[i].getString())
                }
            } else {
                for (i in 1 until array.size) {
                    sb.append(delimiter)
                    sb.append(array[i].getString())
                }
            }
            return sb.toString()
        }

        fun toLowerCaseList(array: Array<Key?>?, delimiter: String?): String? {
            if (array!!.size == 0) return ""
            val sb = StringBuffer((array[0] as KeyImpl?)!!.getLowerString())
            if (delimiter!!.length() === 1) {
                val c: Char = delimiter.charAt(0)
                for (i in 1 until array.size) {
                    sb.append(c)
                    sb.append((array[i] as KeyImpl?)!!.getLowerString())
                }
            } else {
                for (i in 1 until array.size) {
                    sb.append(delimiter)
                    sb.append((array[i] as KeyImpl?)!!.getLowerString())
                }
            }
            return sb.toString()
        }

        fun toKey(obj: Object?, defaultValue: Collection.Key?): Collection.Key? {
            if (obj is Collection.Key) return obj as Collection.Key?
            val str: String = Caster.toString(obj, null) ?: return defaultValue
            return init(str)
        }

        @Throws(CasterException::class)
        fun toKey(obj: Object?): Collection.Key? {
            if (obj is Collection.Key) return obj as Collection.Key?
            val str: String = Caster.toString(obj, null) ?: throw CasterException(obj, Collection.Key::class.java)
            return init(str)
        }

        fun toKey(i: Int): Collection.Key? {
            return init(Caster.toString(i))
        }

        fun toKeyArray(arr: Array<String?>?): Array<Key?>? {
            if (arr == null) return null
            val keys: Array<Key?> = arrayOfNulls<Key?>(arr.size)
            for (i in keys.indices) {
                keys[i] = init(arr[i])
            }
            return keys
        }
    }
}