/**
 * Copyright (c) 2017, Tachyon Assosication Switzerland
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

abstract class IKStorageScopeSupport(pc: PageContext?, handler: IKHandler?, appName: String?, name: String?, strType: String?, type: Int, data: Map<Collection.Key?, IKStorageScopeItem?>?,
                                     lastModified: Long, timeSpan: Long) : StructSupport(), StorageScope, CSRFTokenSupport {
    private var id = 0

    companion object {
        protected val ONE: IKStorageScopeItem? = IKStorageScopeItem("1")
        private var _id = 0
        private const val serialVersionUID = 7874930250042576053L
        private val NULL: IKStorageScopeItem? = IKStorageScopeItem("null")
        private val FIX_KEYS: Set<Collection.Key?>? = HashSet<Collection.Key?>()
        protected var ignoreSet: Set<Collection.Key?>? = HashSet<Collection.Key?>()
        @Throws(PageException::class)
        fun getInstance(scope: Int, handler: IKHandler?, appName: String?, name: String?, pc: PageContext?, existing: Scope?, log: Log?): Scope? {
            var sv: IKStorageValue? = null
            if (Scope.SCOPE_SESSION === scope) sv = handler!!.loadData(pc, appName, name, "session", Scope.SCOPE_SESSION, log) else if (Scope.SCOPE_CLIENT === scope) sv = handler!!.loadData(pc, appName, name, "client", Scope.SCOPE_CLIENT, log)
            if (sv != null) {
                val time: Long = sv.lastModified()
                if (existing is IKStorageScopeSupport) {
                    val tmp = existing as IKStorageScopeSupport?
                    if (tmp!!.lastModified() >= time && name.equalsIgnoreCase(tmp.getStorage())) {
                        return existing
                    }
                }
                if (Scope.SCOPE_SESSION === scope) return IKStorageScopeSession(pc, handler, appName, name, sv.getValue(), time, getSessionTimeout(pc)) else if (Scope.SCOPE_CLIENT === scope) return IKStorageScopeClient(pc, handler, appName, name, sv.getValue(), time, getClientTimeout(pc))
            } else if (existing is IKStorageScopeSupport) {
                val tmp = existing as IKStorageScopeSupport?
                if (name.equalsIgnoreCase(tmp!!.getStorage())) {
                    return existing
                }
            }
            var rtn: IKStorageScopeSupport? = null
            val map: Map<Key?, IKStorageScopeItem?> = MapFactory.getConcurrentMap()
            if (Scope.SCOPE_SESSION === scope) rtn = IKStorageScopeSession(pc, handler, appName, name, map, 0, getSessionTimeout(pc)) else if (Scope.SCOPE_CLIENT === scope) rtn = IKStorageScopeClient(pc, handler, appName, name, map, 0, getClientTimeout(pc))
            rtn.store(pc)
            return rtn
        }

        private fun getClientTimeout(pc: PageContext?): Long {
            var pc: PageContext? = pc
            pc = ThreadLocalPageContext.get(pc)
            val ac: ApplicationContext? = if (pc == null) null else pc.getApplicationContext()
            val timeout: TimeSpan? = if (ac == null) null else ac.getClientTimeout()
            return if (timeout == null) 0 else timeout.getMillis()
        }

        private fun getSessionTimeout(pc: PageContext?): Long {
            var pc: PageContext? = pc
            pc = ThreadLocalPageContext.get(pc)
            val ac: ApplicationContext? = if (pc == null) null else pc.getApplicationContext()
            val timeout: TimeSpan? = if (ac == null) null else ac.getSessionTimeout()
            return if (timeout == null) 0 else timeout.getMillis()
        }

        fun getInstance(scope: Int, handler: IKHandler?, appName: String?, name: String?, pc: PageContext?, existing: Session?, log: Log?, defaultValue: Session?): Scope? {
            try {
                return getInstance(scope, handler, appName, name, pc, existing, log)
            } catch (e: PageException) {
            }
            return defaultValue
        }

        fun hasInstance(scope: Int, handler: IKHandler?, appName: String?, name: String?, pc: PageContext?): Boolean {
            return try {
                val log: Log = ThreadLocalPageContext.getLog(pc, "scope")
                if (Scope.SCOPE_SESSION === scope) return handler!!.loadData(pc, appName, name, "session", Scope.SCOPE_SESSION, log) != null else if (Scope.SCOPE_CLIENT === scope) return handler!!.loadData(pc, appName, name, "client", Scope.SCOPE_CLIENT, log) != null
                false
            } catch (e: PageException) {
                false
            }
        }

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

        fun merge(local: Map<Key?, IKStorageScopeItem?>?, storage: Map<Key?, IKStorageScopeItem?>?) {
            val it: Iterator<Entry<Key?, IKStorageScopeItem?>?> = local.entrySet().iterator()
            var e: Entry<Key?, IKStorageScopeItem?>?
            var storageItem: IKStorageScopeItem?
            while (it.hasNext()) {
                e = it.next()
                storageItem = storage!![e.getKey()]
                // this entry not exist in the storage
                if (storageItem == null) {
                    if (!e.getValue().removed()) storage.put(e.getKey(), e.getValue())
                } else if (e.getValue().lastModified() > storageItem.lastModified()) {
                    if (e.getValue().removed()) storage.remove(e.getKey()) else {
                        storage.put(e.getKey(), e.getValue())
                    }
                }
                // local is older than storage is ignored?
            }
        }

        fun cleanRemoved(local: Map<Key?, IKStorageScopeItem?>?): Map<Key?, IKStorageScopeItem?>? {
            val it: Iterator<Entry<Key?, IKStorageScopeItem?>?> = local.entrySet().iterator()
            var e: Entry<Key?, IKStorageScopeItem?>?
            while (it.hasNext()) {
                e = it.next()
                if (e.getValue().removed()) local.remove(e.getKey())
            }
            return local
        }

        @Throws(PageException::class)
        fun prepareToStore(local: Map<Key?, IKStorageScopeItem?>?, oStorage: Object?, lastModified: Long): Map<Key?, IKStorageScopeItem?>? {
            // cached data changed in meantime
            if (oStorage is IKStorageValue) {
                val storage: IKStorageValue? = oStorage
                return if (storage!!.lastModified() > lastModified) {
                    val trg: Map<Key?, IKStorageScopeItem?> = storage!!.getValue()
                    merge(local, trg)
                    trg
                } else {
                    cleanRemoved(local)
                }
            } else if (oStorage is Array<ByteArray>) {
                val barrr = oStorage as Array<ByteArray?>?
                return if (IKStorageValue.toLong(barrr!![1]) > lastModified) {
                    if (barrr[0] == null || barrr[0].length == 0) return local
                    val trg: Map<Key?, IKStorageScopeItem?> = IKStorageValue.deserialize(barrr[0])
                    merge(local, trg)
                    trg
                } else {
                    cleanRemoved(local)
                }
            }
            return local
        }

        protected fun doNowIfNull(config: Config?, dt: DateTime?): DateTime? {
            return if (dt == null) DateTimeImpl(config) else dt
        }

        init {
            FIX_KEYS.add(KeyConstants._cfid)
            FIX_KEYS.add(KeyConstants._cftoken)
            FIX_KEYS.add(KeyConstants._urltoken)
            FIX_KEYS.add(KeyConstants._lastvisit)
            FIX_KEYS.add(KeyConstants._hitcount)
            FIX_KEYS.add(KeyConstants._timecreated)
            FIX_KEYS.add(KeyConstants._csrf_token)
        }

        init {
            ignoreSet.add(KeyConstants._cfid)
            ignoreSet.add(KeyConstants._cftoken)
            ignoreSet.add(KeyConstants._urltoken)
        }
    }

    protected var isinit = true
    protected var data0: Map<Collection.Key?, IKStorageScopeItem?>?
    protected var lastvisit: Long
    protected var _lastvisit: DateTime?
    protected var hitcount = 0
    protected var timecreated: DateTime?
    private var hasChanges = false
    protected var strType: String?
    protected var type: Int
    private var timeSpan: Long = -1
    private var storage: String? = null
    private var tokens: Struct? = StructImpl()
    private val lastModified: Long
    private val handler: IKHandler?
    private val appName: String?
    private val name: String?
    @Override
    fun touchBeforeRequest(pc: PageContext?) {
        hasChanges = false
        setTimeSpan(pc)

        // lastvisit=System.currentTimeMillis();
        if (data0 == null) data0 = MapFactory.getConcurrentMap()
        data0.put(KeyConstants._cfid, IKStorageScopeItem(pc.getCFID()))
        data0.put(KeyConstants._cftoken, IKStorageScopeItem(pc.getCFToken()))
        data0.put(KeyConstants._urltoken, IKStorageScopeItem(pc.getURLToken()))
        data0.put(KeyConstants._lastvisit, IKStorageScopeItem(_lastvisit))
        _lastvisit = DateTimeImpl(pc.getConfig())
        lastvisit = System.currentTimeMillis()
        if (type == SCOPE_CLIENT) {
            data0.put(KeyConstants._hitcount, IKStorageScopeItem(Double.valueOf(hitcount++)))
        } else {
            data0.put(KeyConstants._sessionid, IKStorageScopeItem(pc.getApplicationContext().getName().toString() + "_" + pc.getCFID() + "_" + pc.getCFToken()))
        }
        val ac: ApplicationContext = pc.getApplicationContext()
        if (ac != null && ac.getSessionCluster() && isSessionStorage(pc)) {
            data0.put(KeyConstants._csrf_token, IKStorageScopeItem(tokens))
        }
        data0.put(KeyConstants._timecreated, IKStorageScopeItem(timecreated))
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

    fun lastModified(): Long {
        return lastModified
    }

    @Override
    fun initialize(pc: PageContext?) {
        // StorageScopes need only request initialisation no global init, they are not reused;
    }

    @Override
    fun touchAfterRequest(pc: PageContext?) {
        setTimeSpan(pc)
        data0.put(KeyConstants._lastvisit, IKStorageScopeItem(_lastvisit))
        data0.put(KeyConstants._timecreated, IKStorageScopeItem(timecreated))
        if (type == SCOPE_CLIENT) {
            data0.put(KeyConstants._hitcount, IKStorageScopeItem(Double.valueOf(hitcount)))
        }
        val ac: ApplicationContext = pc.getApplicationContext()
        if (ac != null && (tokens == null || tokens.isEmpty()) && ac.getSessionCluster() && isSessionStorage(pc)) {
            data0.remove(KeyConstants._csrf_token)
        }
        store(pc)
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
        val size = size()
        if (size == 0) return false
        if (size > 7) return true
        return if (size == 7 && !containsKey(KeyConstants._csrf_token)) true else !(containsKey(KeyConstants._cfid) && containsKey(KeyConstants._cftoken) && containsKey(KeyConstants._urltoken) && containsKey(KeyConstants._timecreated)
                && containsKey(KeyConstants._lastvisit) && if (type == SCOPE_CLIENT) containsKey(KeyConstants._hitcount) else containsKey(KeyConstants._sessionid))
    }

    @Override
    fun clear() {
        val it: Iterator<Key?> = data0.keySet().iterator()
        var k: Key?
        while (it.hasNext()) {
            k = it.next()
            removeEL(k)
        }
    }

    @Override
    fun containsKey(key: Key?): Boolean {
        val v: IKStorageScopeItem = data0.getOrDefault(key, NULL)
        return v !== NULL && !v!!.removed()
    }

    @Override
    fun containsKey(pc: PageContext?, key: Key?): Boolean {
        val v: IKStorageScopeItem = data0.getOrDefault(key, NULL)
        return v !== NULL && !v!!.removed()
    }

    @Override
    @Throws(PageException::class)
    operator fun get(key: Key?): Object? {
        return get(null as PageContext?, key)
    }

    @Override
    @Throws(PageException::class)
    operator fun get(pc: PageContext?, key: Key?): Object? {
        val v: IKStorageScopeItem = data0.getOrDefault(key, null) ?: throw StructSupport.invalidKey(data0, key, false)
        if (v.removed()) {
            val sb = StringBuilder()
            val it: Iterator<*> = keySet()!!.iterator()
            var k: Object?
            while (it.hasNext()) {
                k = it.next()
                if (sb.length() > 0) sb.append(", ")
                sb.append(k.toString())
            }
            return ExpressionException("key [" + key + "] doesn't exist (existing keys: [" + sb.toString() + "])")
        }
        return v.getValue()
    }

    @Override
    operator fun get(key: Key?, defaultValue: Object?): Object? {
        return get(null as PageContext?, key, defaultValue)
    }

    @Override
    operator fun get(pc: PageContext?, key: Key?, defaultValue: Object?): Object? {
        val v: IKStorageScopeItem = data0.getOrDefault(key, NULL)
        return if (v === NULL || v!!.removed()) defaultValue else v!!.getValue()
    }

    @Override
    fun keyIterator(): Iterator<Collection.Key?>? {
        return keySet()!!.iterator()
    }

    @Override
    fun keySet(): Set<Collection.Key?>? {
        val keys: Set<Collection.Key?> = HashSet<Collection.Key?>()
        val it: Iterator<Entry<Key?, IKStorageScopeItem?>?> = data0.entrySet().iterator()
        var e: Entry<Key?, IKStorageScopeItem?>?
        var v: IKStorageScopeItem
        while (it.hasNext()) {
            e = it.next()
            v = e.getValue()
            if (v !== NULL && !v.removed()) keys.add(e.getKey())
        }
        return keys
    }

    @Override
    fun entryIterator(): Iterator<Entry<Key?, Object?>?>? {
        return EntryIterator(this, keys())
    }

    @Override
    fun valueIterator(): Iterator<Object?>? {
        return ValueIterator(this, keys()) // TODO use or make a faster iterator
    }

    @Override
    fun keys(): Array<tachyon.runtime.type.Collection.Key?>? {
        return CollectionUtil.keys(this)
    }

    @Override
    @Throws(PageException::class)
    fun remove(key: Key?): Object? {
        hasChanges = true
        val existing: IKStorageScopeItem? = data0!![key]
        if (existing != null) {
            return existing.remove()
        }
        throw ExpressionException("can't remove key [" + key.getString().toString() + "] from map, key doesn't exist")
    }

    @Override
    fun removeEL(key: Key?): Object? {
        hasChanges = true
        val existing: IKStorageScopeItem? = data0!![key]
        return if (existing != null) {
            existing.remove()
        } else null
    }

    @Override
    @Throws(PageException::class)
    operator fun set(key: Key?, value: Object?): Object? {
        hasChanges = true
        return data0.put(key, IKStorageScopeItem(value))
    }

    @Override
    fun setEL(key: Key?, value: Object?): Object? {
        hasChanges = true
        return data0.put(key, IKStorageScopeItem(value))
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
    fun size(): Int {
        var size = 0
        val it: Iterator<Entry<Key?, IKStorageScopeItem?>?> = data0.entrySet().iterator()
        var e: Entry<Key?, IKStorageScopeItem?>?
        var v: IKStorageScopeItem
        while (it.hasNext()) {
            e = it.next()
            v = e.getValue()
            if (v !== NULL && !v.removed()) size++
        }
        return size
    }

    fun store(pc: PageContext?) { // FUTURE add to interface
        handler!!.store(this, pc, appName, name, data0, ThreadLocalPageContext.getLog(pc, "scope"))
    }

    fun unstore(pc: PageContext?) {
        handler!!.unstore(this, pc, appName, name, ThreadLocalPageContext.getLog(pc, "scope"))
    }

    @Override
    fun store(config: Config?) {
        store(ThreadLocalPageContext.get())
    }

    @Override
    fun unstore(config: Config?) {
        unstore(ThreadLocalPageContext.get())
    }

    /**
     * @return the hasChanges
     */
    fun hasChanges(): Boolean {
        return hasChanges
    }

    @Override
    fun containsValue(value: Object?): Boolean {
        val it = values()!!.iterator()
        while (it.hasNext()) {
            if (it.next()!!.equals(value)) return true
        }
        return false
    }

    @Override
    fun values(): Collection<*>? {
        val res: MutableCollection<Object?> = ArrayList<Object?>()
        val it: Iterator<IKStorageScopeItem?> = data0!!.values().iterator()
        var v: IKStorageScopeItem?
        while (it.hasNext()) {
            v = it.next()
            if (v !== NULL && !v!!.removed()) res.add(v!!.getValue())
        }
        return res
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

    @Override
    fun getStorageType(): String? {
        return handler!!.getType()
    }

    private fun isSessionStorage(pc: PageContext?): Boolean {
        val ac: ApplicationContext = pc.getApplicationContext()
        val storage: String? = if (ac == null) null else ac.getSessionstorage()
        if (StringUtil.isEmpty(storage)) return false

        // datasource?
        val ds: DataSource = pc.getDataSource(storage, null)
        return if (ds != null && ds.isStorage()) true else CacheUtil.getCache(pc, storage, null) != null

        // cache
    } // protected abstract IKStorageValue loadData(PageContext pc, String appName, String name,String

    // strType,int type, Log log) throws PageException;
    init {
        // !!! do not store the pagecontext or config object, this object is Serializable !!!
        val config: Config = ThreadLocalPageContext.getConfig(pc)
        data0 = data
        timecreated = doNowIfNull(config, Caster.toDate(data.getOrDefault(KeyConstants._timecreated, null), false, pc.getTimeZone(), null))
        _lastvisit = doNowIfNull(config, Caster.toDate(data.getOrDefault(KeyConstants._lastvisit, null), false, pc.getTimeZone(), null))
        if (_lastvisit == null) _lastvisit = timecreated
        lastvisit = if (_lastvisit == null) 0 else _lastvisit.getTime()
        val ac: ApplicationContext = pc.getApplicationContext()
        if (ac != null && ac.getSessionCluster() && isSessionStorage(pc)) {
            val csrfTokens: IKStorageScopeItem = data.getOrDefault(KeyConstants._csrf_token, null)
            val `val`: Object? = if (csrfTokens == null) null else csrfTokens.getValue()
            if (Decision.isStruct(`val`)) {
                tokens = Caster.toStruct(`val`, null)
            }
        }
        hitcount = if (type == SCOPE_CLIENT) Caster.toIntValue(data.getOrDefault(KeyConstants._hitcount, ONE), 1) else 1
        this.strType = strType
        this.type = type
        this.lastModified = lastModified
        this.handler = handler
        this.appName = appName
        this.name = name
        id = ++_id
        this.timeSpan = timeSpan
    }
}