/**
 * Copyright (c) 2014, the Railo Company Ltd.
 * Copyright (c) 2015, Tachyon Assosication Switzerland
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
package tachyon.runtime.type.scope.storage

import java.util.ArrayList

abstract class StorageScopeImpl : StructSupport, StorageScope, CSRFTokenSupport {
    private var id = 0

    companion object {
        var CFID: Collection.Key? = KeyConstants._cfid
        var CFTOKEN: Collection.Key? = KeyConstants._cftoken
        var URLTOKEN: Collection.Key? = KeyConstants._urltoken
        var LASTVISIT: Collection.Key? = KeyConstants._lastvisit
        var HITCOUNT: Collection.Key? = KeyConstants._hitcount
        var TIMECREATED: Collection.Key? = KeyConstants._timecreated
        var SESSION_ID: Collection.Key? = KeyConstants._sessionid
        private var _id = 0
        private const val serialVersionUID = 7874930250042576053L
        private val FIX_KEYS: Set<Collection.Key?>? = HashSet<Collection.Key?>()
        var KEYS: Set<Collection.Key?>? = HashSet<Collection.Key?>()
        protected var ignoreSet: Set<Collection.Key?>? = HashSet<Collection.Key?>()
        fun encode(input: String?): String? {
            val len: Int = input!!.length()
            val sb = StringBuilder()
            var c: Char
            for (i in 0 until len) {
                c = input.charAt(i)
                if (c >= '0' && c <= '9' || c >= 'a' && c <= 'z' || c >= 'A' && c <= 'Z' || c == '_' || c == '-') sb.append(c) else {
                    sb.append('$')
                    sb.append(Integer.toString(c, Character.MAX_RADIX))
                    sb.append('$')
                }
            }
            return sb.toString()
        }

        fun decode(input: String?): String? {
            val len: Int = input!!.length()
            val sb = StringBuilder()
            var c: Char
            var ni: Int
            var i = 0
            while (i < len) {
                c = input.charAt(i)
                if (c == '$') {
                    ni = input.indexOf('$', i + 1)
                    sb.append(Integer.parseInt(input.substring(i + 1, ni), Character.MAX_RADIX) as Char)
                    i = ni
                } else {
                    sb.append(c)
                }
                i++
            }
            return sb.toString()
        }

        init {
            FIX_KEYS.add(KeyConstants._cfid)
            FIX_KEYS.add(KeyConstants._cftoken)
            FIX_KEYS.add(KeyConstants._urltoken)
            FIX_KEYS.add(KeyConstants._lastvisit)
            FIX_KEYS.add(KeyConstants._hitcount)
            FIX_KEYS.add(KeyConstants._timecreated)
        }

        init {
            KEYS.add(KeyConstants._cfid)
            KEYS.add(KeyConstants._cftoken)
            KEYS.add(KeyConstants._urltoken)
            KEYS.add(KeyConstants._lastvisit)
            KEYS.add(KeyConstants._hitcount)
            KEYS.add(KeyConstants._timecreated)
            KEYS.add(KeyConstants._sessionid)
        }

        init {
            ignoreSet.add(KeyConstants._cfid)
            ignoreSet.add(KeyConstants._cftoken)
            ignoreSet.add(KeyConstants._urltoken)
        }
    }

    protected var isinit = true
    protected var sct: Struct?
    protected var lastvisit: Long = 0
    protected var _lastvisit: DateTime? = null
    protected var hitcount = 0
    protected var timecreated: DateTime?
    private var hasChanges = false
    private var strType: String?
    private var type: Int
    private var timeSpan: Long = -1
    private var storage: String? = null
    private val tokens: Map<Collection.Key?, String?>? = ConcurrentHashMap<Collection.Key?, String?>()

    /**
     * Constructor of the class
     *
     * @param sct
     * @param timecreated
     * @param _lastvisit
     * @param lastvisit
     * @param hitcount
     */
    constructor(sct: Struct?, timecreated: DateTime?, _lastvisit: DateTime?, lastvisit: Long, hitcount: Int, strType: String?, type: Int) {
        this.sct = sct
        this.timecreated = timecreated
        if (_lastvisit == null) this._lastvisit = timecreated else this._lastvisit = _lastvisit
        if (lastvisit == -1L) this.lastvisit = this._lastvisit.getTime() else this.lastvisit = lastvisit
        this.hitcount = hitcount
        this.strType = strType
        this.type = type
        id = ++_id
    }

    /**
     * Constructor of the class
     *
     * @param other
     * @param deepCopy
     */
    constructor(other: StorageScopeImpl?, deepCopy: Boolean) {
        sct = Duplicator.duplicate(other!!.sct, deepCopy) as Struct
        timecreated = other.timecreated
        _lastvisit = other._lastvisit
        hitcount = other.hitcount
        isinit = other.isinit
        lastvisit = other.lastvisit
        strType = other.strType
        type = other.type
        timeSpan = other.timeSpan
        id = ++_id
    }

    @Override
    fun touchBeforeRequest(pc: PageContext?) {
        hasChanges = false
        setTimeSpan(pc)

        // lastvisit=System.currentTimeMillis();
        if (sct == null) sct = StructImpl()
        sct.setEL(KeyConstants._cfid, pc.getCFID())
        sct.setEL(KeyConstants._cftoken, pc.getCFToken())
        sct.setEL(URLTOKEN, pc.getURLToken())
        sct.setEL(LASTVISIT, _lastvisit)
        _lastvisit = DateTimeImpl(pc.getConfig())
        lastvisit = System.currentTimeMillis()
        if (type == SCOPE_CLIENT) {
            sct.setEL(HITCOUNT, Double.valueOf(hitcount++))
        } else {
            sct.setEL(SESSION_ID, pc.getApplicationContext().getName().toString() + "_" + pc.getCFID() + "_" + pc.getCFToken())
        }
        sct.setEL(TIMECREATED, timecreated)
    }

    fun resetEnv(pc: PageContext?) {
        _lastvisit = DateTimeImpl(pc.getConfig())
        timecreated = DateTimeImpl(pc.getConfig())
        touchBeforeRequest(pc)
    }

    fun setTimeSpan(pc: PageContext?) {
        val ac: ApplicationContext = pc.getApplicationContext()
        timeSpan = if (getType() == SCOPE_SESSION) ac.getSessionTimeout().getMillis() else ac.getClientTimeout().getMillis()
    }

    @Override
    fun setMaxInactiveInterval(interval: Int) {
        timeSpan = interval * 1000L
    }

    @Override
    fun getMaxInactiveInterval(): Int {
        return (timeSpan / 1000L).toInt()
    }

    @Override
    fun isInitalized(): Boolean {
        return isinit
    }

    @Override
    fun initialize(pc: PageContext?) {
        // StorageScopes need only request initialisation no global init, they are not reused;
    }

    @Override
    fun touchAfterRequest(pc: PageContext?) {
        sct.setEL(LASTVISIT, _lastvisit)
        sct.setEL(TIMECREATED, timecreated)
        if (type == SCOPE_CLIENT) {
            sct.setEL(HITCOUNT, Double.valueOf(hitcount))
        }
    }

    @Override
    fun release(pc: PageContext?) {
        clear()
        isinit = false
    }

    /**
     * @return returns if the scope is empty or not, this method ignore the "constant" entries of the
     * scope (cfid,cftoken,urltoken)
     */
    fun hasContent(): Boolean {
        return if (sct.size() === if (type == SCOPE_CLIENT) 6 else 5 && sct.containsKey(URLTOKEN) && sct.containsKey(KeyConstants._cftoken) && sct.containsKey(KeyConstants._cfid)) {
            false
        } else true
    }

    @Override
    fun clear() {
        sct.clear()
    }

    @Override
    fun containsKey(key: Key?): Boolean {
        return sct.containsKey(key)
    }

    @Override
    fun containsKey(pc: PageContext?, key: Key?): Boolean {
        return if (sct is StructSupport) (sct as StructSupport?).containsKey(pc, key) else sct.containsKey(key)
    }

    @Override
    @Throws(PageException::class)
    operator fun get(key: Key?): Object? {
        return sct.get(key)
    }

    @Override
    @Throws(PageException::class)
    operator fun get(pc: PageContext?, key: Key?): Object? {
        return sct.get(key)
    }

    @Override
    operator fun get(key: Key?, defaultValue: Object?): Object? {
        return sct.get(key, defaultValue)
    }

    @Override
    operator fun get(pc: PageContext?, key: Key?, defaultValue: Object?): Object? {
        return sct.get(pc, key, defaultValue)
    }

    @Override
    fun keyIterator(): Iterator<Collection.Key?>? {
        return sct.keyIterator()
    }

    @Override
    fun keysAsStringIterator(): Iterator<String?>? {
        return sct.keysAsStringIterator()
    }

    @Override
    fun entryIterator(): Iterator<Entry<Key?, Object?>?>? {
        return sct.entryIterator()
    }

    @Override
    fun valueIterator(): Iterator<Object?>? {
        return sct.valueIterator()
    }

    @Override
    fun keys(): Array<tachyon.runtime.type.Collection.Key?>? {
        return CollectionUtil.keys(this)
    }

    @Override
    @Throws(PageException::class)
    fun remove(key: Key?): Object? {
        hasChanges = true
        return sct.remove(key)
    }

    @Override
    fun removeEL(key: Key?): Object? {
        hasChanges = true
        return sct.removeEL(key)
    }

    @Override
    @Throws(PageException::class)
    operator fun set(key: Key?, value: Object?): Object? {
        hasChanges = true
        return sct.set(key, value)
    }

    @Override
    fun setEL(key: Key?, value: Object?): Object? {
        hasChanges = true
        return sct.setEL(key, value)
    }

    @Override
    fun size(): Int {
        return sct.size()
    }

    @Override
    @Throws(PageException::class)
    fun castToBooleanValue(): Boolean {
        return sct.castToBooleanValue()
    }

    @Override
    fun castToBoolean(defaultValue: Boolean?): Boolean? {
        return sct.castToBoolean(defaultValue)
    }

    @Override
    @Throws(PageException::class)
    fun castToDateTime(): DateTime? {
        return sct.castToDateTime()
    }

    @Override
    fun castToDateTime(defaultValue: DateTime?): DateTime? {
        return sct.castToDateTime(defaultValue)
    }

    @Override
    @Throws(PageException::class)
    fun castToDoubleValue(): Double {
        return sct.castToDoubleValue()
    }

    @Override
    fun castToDoubleValue(defaultValue: Double): Double {
        return sct.castToDoubleValue(defaultValue)
    }

    @Override
    @Throws(PageException::class)
    fun castToString(): String? {
        return sct.castToString()
    }

    @Override
    fun castToString(defaultValue: String?): String? {
        return sct.castToString(defaultValue)
    }

    @Override
    @Throws(PageException::class)
    operator fun compareTo(b: Boolean): Int {
        return sct.compareTo(b)
    }

    @Override
    @Throws(PageException::class)
    operator fun compareTo(dt: DateTime?): Int {
        return sct.compareTo(dt)
    }

    @Override
    @Throws(PageException::class)
    operator fun compareTo(d: Double): Int {
        return sct.compareTo(d)
    }

    @Override
    @Throws(PageException::class)
    operator fun compareTo(str: String?): Int {
        return sct.compareTo(str)
    }

    @Override
    fun lastVisit(): Long {
        return lastvisit
    }

    fun pureKeys(): Array<Collection.Key?>? {
        val keys: List<Collection.Key?> = ArrayList<Collection.Key?>()
        val it: Iterator<Key?>? = keyIterator()
        var key: Collection.Key?
        while (it!!.hasNext()) {
            key = it.next()
            if (!FIX_KEYS!!.contains(key)) keys.add(key)
        }
        return keys.toArray(arrayOfNulls<Collection.Key?>(keys.size()))
    }

    @Override
    fun store(config: Config?) {
        store(ThreadLocalPageContext.get(config))
    }

    @Override
    fun unstore(config: Config?) {
        unstore(ThreadLocalPageContext.get(config))
    }

    fun store(pc: PageContext?) {}
    fun unstore(pc: PageContext?) {}

    /**
     * @return the hasChanges
     */
    fun hasChanges(): Boolean {
        return hasChanges
    }

    @Override
    fun containsValue(value: Object?): Boolean {
        return sct.containsValue(value)
    }

    @Override
    fun values(): Collection<*>? {
        return sct.values()
    }

    @Override
    fun getType(): Int {
        return type
    }

    @Override
    fun getTypeAsString(): String? {
        return strType
    }

    @Override
    fun toDumpData(pageContext: PageContext?, maxlevel: Int, dp: DumpProperties?): DumpData? {
        return StructUtil.toDumpTable(this, StringUtil.ucFirst(getTypeAsString()).toString() + " Scope (" + getStorageType() + ")", pageContext, maxlevel, dp)
    }

    @Override
    fun getLastAccess(): Long {
        return lastvisit
    }

    @Override
    fun getTimeSpan(): Long {
        return timeSpan
    }

    @Override
    fun touch() {
        lastvisit = System.currentTimeMillis()
        _lastvisit = DateTimeImpl(ThreadLocalPageContext.getConfig())
    }

    @Override
    fun isExpired(): Boolean {
        return getLastAccess() + getTimeSpan() < System.currentTimeMillis()
    }

    @Override
    fun setStorage(storage: String?) {
        this.storage = storage
    }

    @Override
    fun getStorage(): String? {
        return storage
    }

    fun _getId(): Int {
        return id
    }

    @Override
    fun getCreated(): Long {
        return if (timecreated == null) 0 else timecreated.getTime()
    }

    @Override
    fun generateToken(key: String?, forceNew: Boolean): String? {
        return ScopeUtil.generateCsrfToken(tokens, key, forceNew)
    }

    @Override
    fun verifyToken(token: String?, key: String?): Boolean {
        return ScopeUtil.verifyCsrfToken(tokens, token, key)
    }
}