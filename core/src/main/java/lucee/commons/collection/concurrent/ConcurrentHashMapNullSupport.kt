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
package lucee.commons.collection.concurrent

import java.io.IOException

/**
 * A hash table supporting full concurrency of retrievals and adjustable expected concurrency for
 * updates. This class obeys the same functional specification as [java.util.Hashtable], and
 * includes versions of methods corresponding to each method of <tt>Hashtable</tt>. However, even
 * though all operations are thread-safe, retrieval operations do *not* entail locking, and
 * there is *not* any support for locking the entire table in a way that prevents all access.
 * This class is fully interoperable with <tt>Hashtable</tt> in programs that rely on its thread
 * safety but not on its synchronization details.
 *
 *
 *
 * Retrieval operations (including <tt>get</tt>) generally do not block, so may overlap with update
 * operations (including <tt>put</tt> and <tt>remove</tt>). Retrievals reflect the results of the
 * most recently *completed* update operations holding upon their onset. For aggregate
 * operations such as <tt>putAll</tt> and <tt>clear</tt>, concurrent retrievals may reflect
 * insertion or removal of only some entries. Similarly, Iterators and Enumerations return elements
 * reflecting the state of the hash table at some point at or since the creation of the
 * iterator/enumeration. They do *not* throw [ConcurrentModificationException].
 * However, iterators are designed to be used by only one thread at a time.
 *
 *
 *
 * The allowed concurrency among update operations is guided by the optional
 * <tt>concurrencyLevel</tt> constructor argument (default <tt>16</tt>), which is used as a hint for
 * internal sizing. The table is internally partitioned to try to permit the indicated number of
 * concurrent updates without contention. Because placement in hash tables is essentially random,
 * the actual concurrency will vary. Ideally, you should choose a value to accommodate as many
 * threads as will ever concurrently modify the table. Using a significantly higher value than you
 * need can waste space and time, and a significantly lower value can lead to thread contention. But
 * overestimates and underestimates within an order of magnitude do not usually have much noticeable
 * impact. A value of one is appropriate when it is known that only one thread will modify and all
 * others will only read. Also, resizing this or any other kind of hash table is a relatively slow
 * operation, so, when possible, it is a good idea to provide estimates of expected table sizes in
 * constructors.
 *
 *
 *
 * This class and its views and iterators implement all of the *optional* methods of the
 * [Map] and [Iterator] interfaces.
 *
 *
 *
 * Like [Hashtable] but unlike [HashMap], this class does *not* allow
 * <tt>null</tt> to be used as a key or value.
 *
 *
 *
 * This class is a member of the [
 * Java Collections Framework]({@docRoot}/../technotes/guides/collections/index.html).
 *
 * @since 1.5
 * @author Doug Lea
 * @param <K> the type of keys maintained by this map
 * @param <V> the type of mapped values
</V></K> */
class ConcurrentHashMapNullSupport<K, V> @JvmOverloads constructor(initialCapacity: Int = DEFAULT_INITIAL_CAPACITY, loadFactor: Float = DEFAULT_LOAD_FACTOR, concurrencyLevel: Int = DEFAULT_CONCURRENCY_LEVEL) : AbstractMap<K, V>(), Serializable {
    /* ---------------- Fields -------------- */
    /**
     * Mask value for indexing into segments. The upper bits of a key's hash code are used to choose the
     * segment.
     */
    val segmentMask: Int

    /**
     * Shift value for indexing within segments.
     */
    val segmentShift: Int

    /**
     * The segments, each of which is a specialized hash table
     */
    val segments: Array<Segment<K, V>>

    @Transient
    var keySet: Set<K>? = null

    @Transient
    var entrySet: Set<Map.Entry<K, V>>? = null

    @Transient
    var values: Collection<V>? = null

    /**
     * Returns the segment that should be used for key with given hash
     *
     * @param hash the hash code for the key
     * @return the segment
     */
    fun segmentFor(hash: Int): Segment<K, V> {
        return segments[hash ushr segmentShift and segmentMask]
    }
    /* ---------------- Inner Classes -------------- */
    /**
     * ConcurrentHashMap list entry. Note that this is never exported out as an user-visible Map.Entry.
     *
     * Because the value field is volatile, not final, it is legal wrt the Java Memory Model for an
     * unsynchronized reader to see null instead of initial value when read via a data race. Although a
     * reordering leading to this is not likely to ever actually occur, the Segment.readValueUnderLock
     * method is used as a backup in case a null (pre-initialized) value is ever seen in an
     * unsynchronized access method.
     */
    internal class HashEntry<K, V>(val key: K, val hash: Int, val next: HashEntry<K, V>, @field:Volatile var value: V) {
        companion object {
            @SuppressWarnings("unchecked")
            fun <K, V> newArray(i: Int): Array<HashEntry<K, V>?> {
                return arrayOfNulls<HashEntry<*, *>?>(i)
            }
        }
    }

    /**
     * Segments are specialized versions of hash tables. This subclasses from ReentrantLock
     * opportunistically, just to simplify some locking and avoid separate construction.
     */
    class Segment<K, V>(initialCapacity: Int,
                        /**
                         * The load factor for the hash table. Even though this value is same for all segments, it is
                         * replicated to avoid needing links to outer object.
                         *
                         * @serial
                         */
                        val loadFactor: Float) : ReentrantLock(), Serializable {
        /**
         * The number of elements in this segment's region.
         */
        @Volatile
        @Transient
        var count = 0

        /**
         * Number of updates that alter the size of the table. This is used during bulk-read methods to make
         * sure they see a consistent snapshot: If modCounts change during a traversal of segments computing
         * size or checking containsValue, then we might have an inconsistent view of state so (usually)
         * must retry.
         */
        @Transient
        var modCount = 0

        /**
         * The table is rehashed when its size exceeds this threshold. (The value of this field is always
         * <tt>(int)(capacity *
         * loadFactor)</tt>.)
         */
        @Transient
        var threshold = 0

        /**
         * The per-segment table.
         */
        @Volatile
        @Transient
        var table: Array<HashEntry<K, V>?>

        /**
         * Sets table to new HashEntry array. Call only while holding lock or in constructor.
         */
        fun setTable(newTable: Array<HashEntry<K, V>?>) {
            threshold = (newTable.size * loadFactor).toInt()
            table = newTable
        }

        /**
         * Returns properly casted first entry of bin for given hash.
         */
        fun getFirst(hash: Int): HashEntry<K, V>? {
            val tab = table
            return tab[hash and tab.size - 1]
        }

        /**
         * Reads value field of an entry under lock. Called if value field ever appears to be null. This is
         * possible only if a compiler happens to reorder a HashEntry initialization with its table
         * assignment, which is legal under memory model but is not known to ever occur.
         */
        fun readValueUnderLock(e: HashEntry<K, V>): V {
            lock()
            // return e.value;
            return try {
                e.value
            } finally {
                unlock()
            }
        }

        /* Specialized implementations of map methods */
        operator fun get(key: Object, hash: Int): V? {
            return get(key, hash, null)
        }

        operator fun get(key: Object, hash: Int, defaultValue: V?): V? {
            if (count != 0) { // read-volatile
                var e = getFirst(hash)
                while (e != null) {
                    if (e.hash == hash && (key === e.key || key.equals(e.key))) {
                        val v: V? = e.value
                        return v ?: readValueUnderLock(e)
                        // recheck; possible unnecessary double check then value can be null
                    }
                    e = e.next
                }
            }
            return defaultValue
        }

        @Throws(PageException::class)
        fun getE(map: Map<K, V>?, key: Object, hash: Int): V {
            if (count != 0) { // read-volatile
                var e = getFirst(hash)
                while (e != null) {
                    if (e.hash == hash && key.equals(e.key)) {
                        val v: V? = e.value
                        return v ?: readValueUnderLock(e)
                        // recheck; possible unnecessary double check then value can be null
                    }
                    e = e.next
                }
            }
            throw StructSupport.invalidKey(map, key, false)
        }

        fun containsKey(key: Object, hash: Int): Boolean {
            if (count != 0) { // read-volatile
                var e = getFirst(hash)
                while (e != null) {
                    if (e.hash == hash && key.equals(e.key)) return true
                    e = e.next
                }
            }
            return false
        }

        fun containsValue(value: Object?): Boolean {
            if (count != 0) { // read-volatile
                val tab = table
                val len = tab.size
                for (i in 0 until len) {
                    var e = tab[i]
                    while (e != null) {
                        var v: V? = e.value
                        if (v == null) // recheck ; possible unnecessary double check then value can be null
                            v = readValueUnderLock(e)
                        if (value == null) {
                            if (v == null) return true
                        } else if (value.equals(v)) return true
                        e = e.next
                    }
                }
            }
            return false
        }

        fun replace(key: K, hash: Int, oldValue: V, newValue: V): Boolean {
            lock()
            return try {
                var e = getFirst(hash)
                while (e != null && (e.hash != hash || !key!!.equals(e.key))) e = e.next
                var replaced = false
                if (e != null && oldValue!!.equals(e.value)) {
                    replaced = true
                    e.value = newValue
                }
                replaced
            } finally {
                unlock()
            }
        }

        fun replace(key: K, hash: Int, newValue: V): V? {
            lock()
            return try {
                var e = getFirst(hash)
                while (e != null && (e.hash != hash || !key!!.equals(e.key))) e = e.next
                var oldValue: V? = null
                if (e != null) {
                    oldValue = e.value
                    e.value = newValue
                }
                oldValue
            } finally {
                unlock()
            }
        }

        fun repl(key: K, hash: Int, newValue: V, replaced: RefBoolean): V? {
            lock()
            return try {
                var e = getFirst(hash)
                while (e != null && (e.hash != hash || !key!!.equals(e.key))) e = e.next
                if (e == null) return null
                replaced.setValue(true)
                val oldValue = e.value
                e.value = newValue
                oldValue
            } finally {
                unlock()
            }
        }

        fun put(key: K, hash: Int, value: V, onlyIfAbsent: Boolean): V? {
            lock()
            return try {
                var c = count
                if (c++ > threshold) // ensure capacity
                    rehash()
                val tab = table
                val index = hash and tab.size - 1
                val first = tab[index]
                var e = first
                while (e != null && (e.hash != hash || !key!!.equals(e.key))) e = e.next
                val oldValue: V?
                if (e != null) {
                    oldValue = e.value
                    if (!onlyIfAbsent) e.value = value
                } else {
                    oldValue = null
                    ++modCount
                    tab[index] = HashEntry(key, hash, first, value)
                    count = c // write-volatile
                }
                oldValue
            } finally {
                unlock()
            }
        }

        fun rehash() {
            val oldTable = table
            val oldCapacity = oldTable.size
            if (oldCapacity >= MAXIMUM_CAPACITY) return

            /*
			 * Reclassify nodes in each list to new Map. Because we are using power-of-two expansion, the
			 * elements from each bin must either stay at same index, or move with a power of two offset. We
			 * eliminate unnecessary node creation by catching cases where old nodes can be reused because their
			 * next fields won't change. Statistically, at the default threshold, only about one-sixth of them
			 * need cloning when a table doubles. The nodes they replace will be garbage collectable as soon as
			 * they are no longer referenced by any reader thread that may be in the midst of traversing table
			 * right now.
			 */
            val newTable: Array<HashEntry<K, V>?> = HashEntry.newArray(oldCapacity shl 1)
            threshold = (newTable.size * loadFactor).toInt()
            val sizeMask = newTable.size - 1
            for (i in 0 until oldCapacity) {
                // We need to guarantee that any existing reads of old Map can
                // proceed. So we cannot yet null out each bin.
                val e = oldTable[i]
                if (e != null) {
                    val next = e.next
                    val idx = e.hash and sizeMask

                    // Single node on list
                    if (next == null) newTable[idx] = e else {
                        // Reuse trailing consecutive sequence at same slot
                        var lastRun: HashEntry<K, V> = e
                        var lastIdx = idx
                        var last = next
                        while (last != null) {
                            val k = last.hash and sizeMask
                            if (k != lastIdx) {
                                lastIdx = k
                                lastRun = last
                            }
                            last = last.next
                        }
                        newTable[lastIdx] = lastRun

                        // Clone all remaining nodes
                        var p: HashEntry<K, V> = e
                        while (p != lastRun) {
                            val k = p.hash and sizeMask
                            val n = newTable[k]
                            newTable[k] = HashEntry(p.key, p.hash, n, p.value)
                            p = p.next
                        }
                    }
                }
            }
            table = newTable
        }

        /**
         * Remove; match on key only if value null, else match both.
         */
        fun _remove(key: Object, hash: Int, value: Object?, defaultValue: Object?): Object? {
            lock()
            return try {
                val c = count - 1
                val tab = table
                val index = hash and tab.size - 1
                val first = tab[index]
                var e = first
                while (e != null && (e.hash != hash || !key.equals(e.key))) e = e.next
                if (e == null) return defaultValue
                val v: V? = e.value
                if (value == null && v == null || value.equals(v)) {
                    ++modCount
                    var newFirst = e.next
                    var p = first
                    while (p != e) {
                        newFirst = HashEntry(p!!.key, p.hash, newFirst, p.value)
                        p = p.next
                    }
                    tab[index] = newFirst
                    count = c // write-volatile
                    return v
                }
                defaultValue
            } finally {
                unlock()
            }
        }

        fun r(key: Object, hash: Int, defaultValue: V): V {
            lock()
            return try {
                val c = count - 1
                val tab = table
                val index = hash and tab.size - 1
                val first = tab[index]
                var e = first
                while (e != null && (e.hash != hash || !key.equals(e.key))) e = e.next
                if (e == null) return defaultValue
                val v = e.value
                ++modCount
                var newFirst = e.next
                var p = first
                while (p != e) {
                    newFirst = HashEntry(p!!.key, p.hash, newFirst, p.value)
                    p = p.next
                }
                tab[index] = newFirst
                count = c // write-volatile
                v
            } finally {
                unlock()
            }
        }

        @Throws(PageException::class)
        fun r(map: Map?, key: Object, hash: Int): V {
            lock()
            return try {
                val c = count - 1
                val tab = table
                val index = hash and tab.size - 1
                val first = tab[index]
                var e = first
                while (e != null && (e.hash != hash || !key.equals(e.key))) e = e.next
                if (e == null) throw StructSupport.invalidKey(map, key, false)
                val v = e.value
                ++modCount
                var newFirst = e.next
                var p = first
                while (p != e) {
                    newFirst = HashEntry(p!!.key, p.hash, newFirst, p.value)
                    p = p.next
                }
                tab[index] = newFirst
                count = c // write-volatile
                v
            } finally {
                unlock()
            }
        }

        fun clear() {
            if (count != 0) {
                lock()
                try {
                    val tab = table
                    for (i in tab.indices) tab[i] = null
                    ++modCount
                    count = 0 // write-volatile
                } finally {
                    unlock()
                }
            }
        }

        companion object {
            /*
		 * Segments maintain a table of entry lists that are ALWAYS kept in a consistent state, so can be
		 * read without locking. Next fields of nodes are immutable (final). All list additions are
		 * performed at the front of each bin. This makes it easy to check changes, and also fast to
		 * traverse. When nodes would otherwise be changed, new nodes are created to replace them. This
		 * works well for hash tables since the bin lists tend to be short. (The average length is less than
		 * two for the default load factor threshold.)
		 *
		 * Read operations can thus proceed without locking, but rely on selected uses of volatiles to
		 * ensure that completed write operations performed by other threads are noticed. For most purposes,
		 * the "count" field, tracking the number of elements, serves as that volatile variable ensuring
		 * visibility. This is convenient because this field needs to be read in many read operations
		 * anyway:
		 *
		 * - All (unsynchronized) read operations must first read the "count" field, and should not look at
		 * table entries if it is 0.
		 *
		 * - All (synchronized) write operations should write to the "count" field after structurally
		 * changing any bin. The operations must not take any action that could even momentarily cause a
		 * concurrent read operation to see inconsistent data. This is made easier by the nature of the read
		 * operations in Map. For example, no operation can reveal that the table has grown but the
		 * threshold has not yet been updated, so there are no atomicity requirements for this with respect
		 * to reads.
		 *
		 * As a guide, all critical volatile reads and writes to the count field are marked in code
		 * comments.
		 */
            private const val serialVersionUID = 2249069246763182397L
            @SuppressWarnings("unchecked")
            fun <K, V> newArray(i: Int): Array<Segment<K, V>> {
                return arrayOfNulls<Segment<*, *>>(i)
            }
        }

        init {
            setTable(HashEntry.newArray(initialCapacity))
        }
    }

    /**
     * Creates a new map with the same mappings as the given map. The map is created with a capacity of
     * 1.5 times the number of mappings in the given map or 16 (whichever is greater), and a default
     * load factor (0.75) and concurrencyLevel (16).
     *
     * @param m the map
     */
    constructor(m: Map<out K, V>) : this(Math.max((m.size() / DEFAULT_LOAD_FACTOR) as Int + 1, DEFAULT_INITIAL_CAPACITY), DEFAULT_LOAD_FACTOR, DEFAULT_CONCURRENCY_LEVEL) {
        putAll(m)
    }/*
		 * We keep track of per-segment modCounts to avoid ABA problems in which an element in one segment
		 * was added and in another removed during traversal, in which case the table was never actually
		 * empty at any point. Note the similar use of modCounts in the size() and containsValue() methods,
		 * which are the only other methods also susceptible to ABA problems.
		 */
    // If mcsum happens to be zero, then we know we got a snapshot
    // before any modifications at all were made. This is
    // probably common enough to bother tracking.
    /**
     * Returns <tt>true</tt> if this map contains no key-value mappings.
     *
     * @return <tt>true</tt> if this map contains no key-value mappings
     */
    @get:Override
    val isEmpty: Boolean
        get() {
            val segments = segments
            /*
      * We keep track of per-segment modCounts to avoid ABA problems in which an element in one segment
      * was added and in another removed during traversal, in which case the table was never actually
      * empty at any point. Note the similar use of modCounts in the size() and containsValue() methods,
      * which are the only other methods also susceptible to ABA problems.
      */
            val mc = IntArray(segments.size)
            var mcsum = 0
            for (i in segments.indices) {
                if (segments[i].count != 0) return false
                mc[i] = segments[i].modCount
                mcsum += mc[i]
            }
            // If mcsum happens to be zero, then we know we got a snapshot
            // before any modifications at all were made. This is
            // probably common enough to bother tracking.
            if (mcsum != 0) {
                for (i in segments.indices) {
                    if (segments[i].count != 0 || mc[i] != segments[i].modCount) return false
                }
            }
            return true
        }

    /**
     * Returns the number of key-value mappings in this map. If the map contains more than
     * <tt>Integer.MAX_VALUE</tt> elements, returns <tt>Integer.MAX_VALUE</tt>.
     *
     * @return the number of key-value mappings in this map
     */
    @Override
    fun size(): Int {
        val segments = segments
        var sum: Long = 0
        var check: Long = 0
        val mc = IntArray(segments.size)
        // Try a few times to get accurate count. On failure due to
        // continuous async changes in table, resort to locking.
        for (k in 0 until RETRIES_BEFORE_LOCK) {
            check = 0
            sum = 0
            var mcsum = 0
            for (i in segments.indices) {
                sum += segments[i].count.toLong()
                mc[i] = segments[i].modCount
                mcsum += mc[i]
            }
            if (mcsum != 0) {
                for (i in segments.indices) {
                    check += segments[i].count.toLong()
                    if (mc[i] != segments[i].modCount) {
                        check = -1 // force retry
                        break
                    }
                }
            }
            if (check == sum) break
        }
        if (check != sum) { // Resort to locking all segments
            sum = 0
            for (i in segments.indices) segments[i].lock()
            for (i in segments.indices) sum += segments[i].count.toLong()
            for (i in segments.indices) segments[i].unlock()
        }
        return if (sum > Integer.MAX_VALUE) Integer.MAX_VALUE else sum.toInt()
    }

    /**
     * Returns the value to which the specified key is mapped, or `null` if this map contains no
     * mapping for the key.
     *
     *
     *
     * More formally, if this map contains a mapping from a key `k` to a value `v` such that
     * `key.equals(k)`, then this method returns `v`; otherwise it returns `null`.
     * (There can be at most one such mapping.)
     *
     * @throws NullPointerException if the specified key is null
     */
    @Override
    operator fun get(key: Object?): V {
        val hash = hash(key)
        return segmentFor(hash)[key, hash]
    }

    @Throws(PageException::class)
    private fun ge(key: Object): V {
        val hash = hash(key)
        return segmentFor(hash).getE(this, key, hash)
    }

    @Override
    fun getOrDefault(key: Object?, defaultValue: V): V {
        val hash = hash(key)
        // int hash = hash(key.hashCode());
        return segmentFor(hash)[key, hash, defaultValue]
    }

    /**
     * Tests if the specified object is a key in this table.
     *
     * @param key possible key
     * @return <tt>true</tt> if and only if the specified object is a key in this table, as determined
     * by the <tt>equals</tt> method; <tt>false</tt> otherwise.
     * @throws NullPointerException if the specified key is null
     */
    @Override
    fun containsKey(key: Object?): Boolean {
        val hash = hash(key)
        return segmentFor(hash).containsKey(key, hash)
    }

    /**
     * Returns <tt>true</tt> if this map maps one or more keys to the specified value. Note: This method
     * requires a full internal traversal of the hash table, and so is much slower than method
     * <tt>containsKey</tt>.
     *
     * @param value value whose presence in this map is to be tested
     * @return <tt>true</tt> if this map maps one or more keys to the specified value
     * @throws NullPointerException if the specified value is null
     */
    @Override
    fun containsValue(value: Object?): Boolean {
        val segments = segments
        val mc = IntArray(segments.size)

        // Try a few times without locking
        for (k in 0 until RETRIES_BEFORE_LOCK) {
            val sum = 0
            var mcsum = 0
            for (i in segments.indices) {
                val c = segments[i].count
                mc[i] = segments[i].modCount
                mcsum += mc[i]
                if (segments[i].containsValue(value)) return true
            }
            var cleanSweep = true
            if (mcsum != 0) {
                for (i in segments.indices) {
                    val c = segments[i].count
                    if (mc[i] != segments[i].modCount) {
                        cleanSweep = false
                        break
                    }
                }
            }
            if (cleanSweep) return false
        }
        // Resort to locking all segments
        for (i in segments.indices) segments[i].lock()
        var found = false
        try {
            for (i in segments.indices) {
                if (segments[i].containsValue(value)) {
                    found = true
                    break
                }
            }
        } finally {
            for (i in segments.indices) segments[i].unlock()
        }
        return found
    }

    /**
     * Legacy method testing if some key maps into the specified value in this table. This method is
     * identical in functionality to [.containsValue], and exists solely to ensure full
     * compatibility with class [java.util.Hashtable], which supported this method prior to
     * introduction of the Java Collections framework.
     *
     * @param value a value to search for
     * @return <tt>true</tt> if and only if some key maps to the <tt>value</tt> argument in this table
     * as determined by the <tt>equals</tt> method; <tt>false</tt> otherwise
     * @throws NullPointerException if the specified value is null
     */
    operator fun contains(value: Object?): Boolean {
        return containsValue(value)
    }

    /**
     * Maps the specified key to the specified value in this table. Neither the key nor the value can be
     * null.
     *
     *
     *
     * The value can be retrieved by calling the <tt>get</tt> method with a key that is equal to the
     * original key.
     *
     * @param key key with which the specified value is to be associated
     * @param value value to be associated with the specified key
     * @return the previous value associated with <tt>key</tt>, or <tt>null</tt> if there was no mapping
     * for <tt>key</tt>
     * @throws NullPointerException if the specified key or value is null
     */
    @Override
    fun put(key: K, value: V): V {
        val hash = hash(key)
        return segmentFor(hash).put(key, hash, value, false)
    }

    /**
     * Copies all of the mappings from the specified map to this one. These mappings replace any
     * mappings that this map had for any of the keys currently in the specified map.
     *
     * @param m mappings to be stored in this map
     */
    @Override
    fun putAll(m: Map<out K, V>) {
        for (e in m.entrySet()) put(e.getKey(), e.getValue())
    }

    /**
     * Removes the key (and its corresponding value) from this map. This method does nothing if the key
     * is not in the map.
     *
     * @param key the key that needs to be removed
     * @return the previous value associated with <tt>key</tt>, or <tt>null</tt> if there was no mapping
     * for <tt>key</tt>
     * @throws NullPointerException if the specified key is null
     */
    @Override
    fun remove(key: Object?): V {
        val hash = hash(key)
        return segmentFor(hash).r(key, hash, null)
    }

    @Throws(PageException::class)
    operator fun rem(key: Object?): V {
        val hash = hash(key)
        return segmentFor(hash).r(this, key, hash)
    }

    private fun remove(e: Map.Entry): Boolean {
        val k: Object = e.getKey()
        val v: Object = e.getValue()
        val hash = hash(k)
        return segmentFor(hash)._remove(k, hash, v, Null.NULL) !== Null.NULL
    }

    /**
     * Removes all of the mappings from this map.
     */
    @Override
    fun clear() {
        for (i in segments.indices) segments[i].clear()
    }

    /**
     * Returns a [Set] view of the keys contained in this map. The set is backed by the map, so
     * changes to the map are reflected in the set, and vice-versa. The set supports element removal,
     * which removes the corresponding mapping from this map, via the <tt>Iterator.remove</tt>,
     * <tt>Set.remove</tt>, <tt>removeAll</tt>, <tt>retainAll</tt>, and <tt>clear</tt> operations. It
     * does not support the <tt>add</tt> or <tt>addAll</tt> operations.
     *
     *
     *
     * The view's <tt>iterator</tt> is a "weakly consistent" iterator that will never throw
     * [ConcurrentModificationException], and guarantees to traverse elements as they existed upon
     * construction of the iterator, and may (but is not guaranteed to) reflect any modifications
     * subsequent to construction.
     */
    @Override
    fun keySet(): Set<K> {
        val ks = keySet
        return ks ?: KeySet().also { keySet = it }
    }

    /**
     * Returns a [Collection] view of the values contained in this map. The collection is backed
     * by the map, so changes to the map are reflected in the collection, and vice-versa. The collection
     * supports element removal, which removes the corresponding mapping from this map, via the
     * <tt>Iterator.remove</tt>, <tt>Collection.remove</tt>, <tt>removeAll</tt>, <tt>retainAll</tt>, and
     * <tt>clear</tt> operations. It does not support the <tt>add</tt> or <tt>addAll</tt> operations.
     *
     *
     *
     * The view's <tt>iterator</tt> is a "weakly consistent" iterator that will never throw
     * [ConcurrentModificationException], and guarantees to traverse elements as they existed upon
     * construction of the iterator, and may (but is not guaranteed to) reflect any modifications
     * subsequent to construction.
     */
    @Override
    fun values(): Collection<V> {
        val vs = values
        return vs ?: Values().also { values = it }
    }

    /**
     * Returns a [Set] view of the mappings contained in this map. The set is backed by the map,
     * so changes to the map are reflected in the set, and vice-versa. The set supports element removal,
     * which removes the corresponding mapping from the map, via the <tt>Iterator.remove</tt>,
     * <tt>Set.remove</tt>, <tt>removeAll</tt>, <tt>retainAll</tt>, and <tt>clear</tt> operations. It
     * does not support the <tt>add</tt> or <tt>addAll</tt> operations.
     *
     *
     *
     * The view's <tt>iterator</tt> is a "weakly consistent" iterator that will never throw
     * [ConcurrentModificationException], and guarantees to traverse elements as they existed upon
     * construction of the iterator, and may (but is not guaranteed to) reflect any modifications
     * subsequent to construction.
     */
    @Override
    fun entrySet(): Set<Map.Entry<K, V>> {
        val es = entrySet
        return es ?: EntrySet().also { entrySet = it }
    }

    /**
     * Returns an enumeration of the keys in this table.
     *
     * @return an enumeration of the keys in this table
     * @see .keySet
     */
    fun keys(): Enumeration<K> {
        return KeyIterator()
    }

    /**
     * Returns an enumeration of the values in this table.
     *
     * @return an enumeration of the values in this table
     * @see .values
     */
    fun elements(): Enumeration<V> {
        return ValueIterator()
    }

    /* ---------------- Iterator Support -------------- */
    internal abstract inner class HashIterator {
        var nextSegmentIndex: Int
        var nextTableIndex: Int
        var currentTable: Array<HashEntry<K, V>?>
        var nextEntry: HashEntry<K, V>? = null
        var lastReturned: HashEntry<K, V>? = null
        fun hasMoreElements(): Boolean {
            return hasNext()
        }

        fun advance() {
            if (nextEntry != null && nextEntry.next.also { nextEntry = it } != null) return
            while (nextTableIndex >= 0) {
                if (currentTable[nextTableIndex--].also { nextEntry = it } != null) return
            }
            while (nextSegmentIndex >= 0) {
                val seg = segments[nextSegmentIndex--]
                if (seg.count != 0) {
                    currentTable = seg.table
                    for (j in currentTable.indices.reversed()) {
                        if (currentTable[j].also { nextEntry = it } != null) {
                            nextTableIndex = j - 1
                            return
                        }
                    }
                }
            }
        }

        operator fun hasNext(): Boolean {
            return nextEntry != null
        }

        fun nextEntry(): HashEntry<K, V>? {
            if (nextEntry == null) throw NoSuchElementException()
            lastReturned = nextEntry
            advance()
            return lastReturned
        }

        fun remove() {
            if (lastReturned == null) throw IllegalStateException()
            this@ConcurrentHashMapNullSupport.remove(lastReturned.key)
            lastReturned = null
        }

        init {
            nextSegmentIndex = segments.size - 1
            nextTableIndex = -1
            advance()
        }
    }

    internal inner class KeyIterator : HashIterator(), Iterator<K>, Enumeration<K> {
        @Override
        override fun next(): K {
            return super.nextEntry().key
        }

        @Override
        fun nextElement(): K {
            return super.nextEntry().key
        }
    }

    internal inner class ValueIterator : HashIterator(), Iterator<V>, Enumeration<V> {
        @Override
        override fun next(): V {
            return super.nextEntry().value
        }

        @Override
        fun nextElement(): V {
            return super.nextEntry().value
        }
    }

    /**
     * Custom Entry class used by EntryIterator.next(), that relays setValue changes to the underlying
     * map.
     */
    internal inner class WriteThroughEntry(k: K, v: V) : AbstractMap.SimpleEntry<K, V>(k, v) {
        /**
         * Set our entry's value and write through to the map. The value to return is somewhat arbitrary
         * here. Since a WriteThroughEntry does not necessarily track asynchronous changes, the most recent
         * "previous" value could be different from what we return (or could even have been removed in which
         * case the put will re-establish). We do not and cannot guarantee more.
         */
        @Override
        fun setValue(value: V): V {
            val v: V = super.setValue(value)
            put(getKey(), value)
            return v
        }
    }

    internal inner class EntryIterator : HashIterator(), Iterator<Entry<K, V>?> {
        @Override
        override fun next(): Map.Entry<K, V> {
            val e: HashEntry<K, V> = super.nextEntry()
            return WriteThroughEntry(e.key, e.value)
        }
    }

    internal inner class KeySet : AbstractSet<K>() {
        @Override
        operator fun iterator(): Iterator<K> {
            return KeyIterator()
        }

        @Override
        fun size(): Int {
            return this@ConcurrentHashMapNullSupport.size()
        }

        @get:Override
        val isEmpty: Boolean
            get() = this@ConcurrentHashMapNullSupport.isEmpty

        @Override
        operator fun contains(o: Object?): Boolean {
            return containsKey(o)
        }

        @Override
        fun remove(o: Object?): Boolean {
            return try {
                rem(o)
                true
            } catch (t: Throwable) {
                ExceptionUtil.rethrowIfNecessary(t)
                false
            }
        }

        @Override
        fun clear() {
            this@ConcurrentHashMapNullSupport.clear()
        }
    }

    internal inner class Values : AbstractCollection<V>() {
        @Override
        operator fun iterator(): Iterator<V> {
            return ValueIterator()
        }

        @Override
        fun size(): Int {
            return this@ConcurrentHashMapNullSupport.size()
        }

        @get:Override
        val isEmpty: Boolean
            get() = this@ConcurrentHashMapNullSupport.isEmpty

        @Override
        operator fun contains(o: Object?): Boolean {
            return containsValue(o)
        }

        @Override
        fun clear() {
            this@ConcurrentHashMapNullSupport.clear()
        }
    }

    internal inner class EntrySet : AbstractSet<Map.Entry<K, V>?>() {
        @Override
        operator fun iterator(): Iterator<Map.Entry<K, V>> {
            return EntryIterator()
        }

        @Override
        operator fun contains(o: Object): Boolean {
            if (o !is Map.Entry) return false
            val e = o as Map.Entry<*, *>
            return try {
                val v: V = ge(e.getKey()) ?: return e.getValue() == null
                v.equals(e.getValue())
            } catch (t: Throwable) {
                ExceptionUtil.rethrowIfNecessary(t)
                false
            }
        }

        @Override
        fun remove(o: Object?): Boolean {
            if (o !is Map.Entry) return false
            val e = o as Map.Entry<*, *>?
            return this@ConcurrentHashMapNullSupport.remove(e)
        }

        @Override
        fun size(): Int {
            return this@ConcurrentHashMapNullSupport.size()
        }

        @get:Override
        val isEmpty: Boolean
            get() = this@ConcurrentHashMapNullSupport.isEmpty

        @Override
        fun clear() {
            this@ConcurrentHashMapNullSupport.clear()
        }
    }
    /* ---------------- Serialization Support -------------- */
    /**
     * Save the state of the <tt>ConcurrentHashMap</tt> instance to a stream (i.e., serialize it).
     *
     * @param s the stream
     * @serialData the key (Object) and value (Object) for each key-value mapping, followed by a null
     * pair. The key-value mappings are emitted in no particular order.
     */
    @Throws(IOException::class)
    private fun writeObject(s: java.io.ObjectOutputStream) {
        s.defaultWriteObject()
        for (k in segments.indices) {
            val seg = segments[k]
            seg.lock()
            try {
                val tab = seg.table
                for (i in tab.indices) {
                    var e = tab[i]
                    while (e != null) {
                        s.writeObject(e.key)
                        s.writeObject(e.value)
                        e = e.next
                    }
                }
            } finally {
                seg.unlock()
            }
        }
        s.writeObject(null)
        s.writeObject(null)
    }

    /**
     * Reconstitute the <tt>ConcurrentHashMap</tt> instance from a stream (i.e., deserialize it).
     *
     * @param s the stream
     */
    @Throws(IOException::class, ClassNotFoundException::class)
    private fun readObject(s: java.io.ObjectInputStream) {
        s.defaultReadObject()

        // Initialize each segment to be minimally sized, and let grow.
        for (i in segments.indices) {
            segments[i].setTable(arrayOfNulls<HashEntry<*, *>>(1))
        }

        // Read the keys and values, and put the mappings in the table
        while (true) {
            val key: K? = s.readObject()
            val value = s.readObject() as V
            if (key == null) break
            put(key, value)
        }
    }

    companion object {
        private const val serialVersionUID = 7249069246763182397L
        /*
	 * The basic strategy is to subdivide the table among Segments, each of which itself is a
	 * concurrently readable hash table.
	 */
        /* ---------------- Constants -------------- */
        /**
         * The default initial capacity for this table, used when not otherwise specified in a constructor.
         */
        const val DEFAULT_INITIAL_CAPACITY = 32

        /**
         * The default load factor for this table, used when not otherwise specified in a constructor.
         */
        const val DEFAULT_LOAD_FACTOR = 0.75f

        /**
         * The default concurrency level for this table, used when not otherwise specified in a constructor.
         */
        const val DEFAULT_CONCURRENCY_LEVEL = 16

        /**
         * The maximum capacity, used if a higher value is implicitly specified by either of the
         * constructors with arguments. MUST be a power of two <= 1<<30 to ensure that entries are indexable
         * using ints.
         */
        const val MAXIMUM_CAPACITY = 1 shl 30

        /**
         * The maximum number of segments to allow; used to bound constructor arguments.
         */
        const val MAX_SEGMENTS = 1 shl 32 // slightly conservative

        /**
         * Number of unsynchronized retries in size and containsValue methods before resorting to locking.
         * This is used to avoid unbounded retries if tables undergo continuous modification which would
         * make it impossible to obtain an accurate result.
         */
        const val RETRIES_BEFORE_LOCK = 2

        /* ---------------- Small Utilities -------------- */
        private fun hash(o: Object?): Int {
            if (o is KeyImpl) {
                return (o as KeyImpl?).wangJenkinsHash()
            }
            if (o == null) return 0
            var h: Int = o.hashCode()
            h += h shl 15 xor -0x3283
            h = h xor (h ushr 10)
            h += h shl 3
            h = h xor (h ushr 6)
            h += (h shl 2) + (h shl 14)
            return h xor (h ushr 16)
        }
    }
    /* ---------------- Public operations -------------- */
    /**
     * Creates a new, empty map with the specified initial capacity, load factor and concurrency level.
     *
     * @param initialCapacity the initial capacity. The implementation performs internal sizing to
     * accommodate this many elements.
     * @param loadFactor the load factor threshold, used to control resizing. Resizing may be performed
     * when the average number of elements per bin exceeds this threshold.
     * @param concurrencyLevel the estimated number of concurrently updating threads. The implementation
     * performs internal sizing to try to accommodate this many threads.
     * @throws IllegalArgumentException if the initial capacity is negative or the load factor or
     * concurrencyLevel are nonpositive.
     */
    /**
     * Creates a new, empty map with a default initial capacity (16), load factor (0.75) and
     * concurrencyLevel (16).
     */
    /**
     * Creates a new, empty map with the specified initial capacity, and with default load factor (0.75)
     * and concurrencyLevel (16).
     *
     * @param initialCapacity the initial capacity. The implementation performs internal sizing to
     * accommodate this many elements.
     * @throws IllegalArgumentException if the initial capacity of elements is negative.
     */
    /**
     * Creates a new, empty map with the specified initial capacity and load factor and with the default
     * concurrencyLevel (16).
     *
     * @param initialCapacity The implementation performs internal sizing to accommodate this many
     * elements.
     * @param loadFactor the load factor threshold, used to control resizing. Resizing may be performed
     * when the average number of elements per bin exceeds this threshold.
     * @throws IllegalArgumentException if the initial capacity of elements is negative or the load
     * factor is nonpositive
     *
     * @since 1.6
     */
    init {
        var initialCapacity = initialCapacity
        var concurrencyLevel = concurrencyLevel
        if (loadFactor <= 0 || initialCapacity < 0 || concurrencyLevel <= 0) throw IllegalArgumentException()
        if (concurrencyLevel > MAX_SEGMENTS) concurrencyLevel = MAX_SEGMENTS

        // Find power-of-two sizes best matching arguments
        var sshift = 0
        var ssize = 1
        while (ssize < concurrencyLevel) {
            ++sshift
            ssize = ssize shl 1
        }
        segmentShift = 32 - sshift
        segmentMask = ssize - 1
        segments = Segment.newArray(ssize)
        if (initialCapacity > MAXIMUM_CAPACITY) initialCapacity = MAXIMUM_CAPACITY
        var c = initialCapacity / ssize
        if (c * ssize < initialCapacity) ++c
        var cap = 1
        while (cap < c) cap = cap shl 1
        for (i in segments.indices) segments[i] = Segment(cap, loadFactor)
    }
}