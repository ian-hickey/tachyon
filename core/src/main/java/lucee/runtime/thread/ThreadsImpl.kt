/**
 *
 * Copyright (c) 2014, the Railo Company Ltd. All rights reserved.
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
package lucee.runtime.thread

import java.io.ByteArrayInputStream

class ThreadsImpl(ct: ChildThreadImpl?) : StructSupport(), lucee.runtime.type.scope.Threads {
    private val ct: ChildThreadImpl?
    @Override
    fun getChildThread(): ChildThread? {
        return ct
    }

    @Override
    fun containsKey(key: Key?): Boolean {
        return get(key, null) != null
    }

    @Override
    fun containsKey(pc: PageContext?, key: Key?): Boolean {
        return get(key, null) != null
    }

    /////////////////////////////////////////////////////////////
    @Override
    fun getType(): Int {
        return -1
    }

    @Override
    fun getTypeAsString(): String? {
        return "thread"
    }

    @Override
    fun initialize(pc: PageContext?) {
    }

    @Override
    fun isInitalized(): Boolean {
        return true
    }

    @Override
    fun release(pc: PageContext?) {
    }

    @Override
    fun clear() {
        ct!!.content.clear()
    }

    @Override
    fun duplicate(deepCopy: Boolean): Collection? {
        val sct = StructImpl()
        val inside = if (deepCopy) ThreadLocalDuplication.set(this, sct) else true
        try {
            val it: Iterator<Entry<Key?, Object?>?>? = entryIterator()
            var e: Entry<Key?, Object?>?
            while (it!!.hasNext()) {
                e = it.next()
                sct.setEL(e.getKey(), if (deepCopy) Duplicator.duplicate(e.getValue(), deepCopy) else e.getValue())
            }
        } finally {
            if (!inside) ThreadLocalDuplication.reset()
        }
        return sct
    }

    private fun getMeta(key: Key?, defaultValue: Object?): Object? {
        if (KEY_ELAPSEDTIME.equalsIgnoreCase(key)) return if (getState() as String? === "TERMINATED") 0 else Double.valueOf(ct!!.getEndTime() - ct!!.getStartTime())
        if (KeyConstants._NAME.equalsIgnoreCase(key)) return ct!!.getTagName()
        if (KEY_OUTPUT.equalsIgnoreCase(key)) return getOutput()
        if (KEY_PRIORITY.equalsIgnoreCase(key)) return ThreadUtil.toStringPriority(ct.getPriority())
        if (KEY_STARTTIME.equalsIgnoreCase(key)) return DateTimeImpl(ct!!.getStartTime(), true)
        if (KEY_STATUS.equalsIgnoreCase(key)) return getState()
        if (KEY_ERROR.equalsIgnoreCase(key)) return ct!!.catchBlock
        if (KEY_STACKTRACE.equalsIgnoreCase(key)) return getStackTrace()
        return if (KEY_CHILD_THREADS.equalsIgnoreCase(key)) Duplicator.duplicate(getThreads(), false) else defaultValue
    }

    private fun getThreads(): Object? {
        return ct!!.getThreads()
    }

    private fun getStackTrace(): String? {
        val sb = StringBuilder()
        try {
            val trace: Array<StackTraceElement?> = ct.getStackTrace()
            if (trace != null) for (i in trace.indices) {
                sb.append("\tat ")
                sb.append(trace[i])
                sb.append("\n")
            }
        } catch (t: Throwable) {
            ExceptionUtil.rethrowIfNecessary(t)
        }
        return sb.toString()
    }

    private fun getOutput(): Object? {
        if (ct!!.output == null) return ""
        val `is`: InputStream = ByteArrayInputStream(ct!!.output.toByteArray())
        return Http.getOutput(`is`, ct!!.contentType, ct!!.contentEncoding, true)
    }

    private fun getState(): Object? {
        /*
		 * 
		 * 
		 * The current status of the thread; one of the following values:
		 * 
		 */
        return try {
            val state: State = ct.getState()
            if (State.NEW.equals(state)) return "NOT_STARTED"
            if (State.WAITING.equals(state)) return "WAITING"
            if (State.TERMINATED.equals(state)) {
                return if (ct!!.terminated || ct!!.catchBlock != null) "TERMINATED" else "COMPLETED"
            }
            "RUNNING"
        } // java 1.4 execution
        catch (t: Throwable) {
            ExceptionUtil.rethrowIfNecessary(t)
            if (ct!!.terminated || ct!!.catchBlock != null) return "TERMINATED"
            if (ct!!.completed) return "COMPLETED"
            if (!ct.isAlive()) "WAITING" else "RUNNING"
        }
    }

    @Override
    operator fun get(key: Key?, defaultValue: Object?): Object? {
        return get(null as PageContext?, key, defaultValue)
    }

    @Override
    operator fun get(pc: PageContext?, key: Key?, defaultValue: Object?): Object? {
        val _null: Object = NullSupportHelper.NULL(pc)
        val meta: Object? = getMeta(key, _null)
        return if (meta !== _null) meta else ct!!.content.get(pc, key, defaultValue)
    }

    @Override
    @Throws(PageException::class)
    operator fun get(key: Key?): Object? {
        return get(null as PageContext?, key)
    }

    @Override
    @Throws(PageException::class)
    operator fun get(pc: PageContext?, key: Key?): Object? {
        val _null: Object = NullSupportHelper.NULL(pc)
        val meta: Object? = getMeta(key, _null)
        return if (meta !== _null) meta else ct!!.content.get(pc, key)
    }

    @Override
    fun keys(): Array<Key?>? {
        val skeys: Array<Key?> = CollectionUtil.keys(ct!!.content)
        if (skeys.size == 0 && ct!!.catchBlock == null) return DEFAULT_KEYS
        val rtn: Array<Key?> = arrayOfNulls<Key?>(skeys.size + (if (ct!!.catchBlock != null) 1 else 0) + DEFAULT_KEYS!!.size)
        var index = 0
        while (index < DEFAULT_KEYS!!.size) {
            rtn[index] = DEFAULT_KEYS[index]
            index++
        }
        if (ct!!.catchBlock != null) {
            rtn[index] = KEY_ERROR
            index++
        }
        for (i in skeys.indices) {
            rtn[index++] = skeys[i]
        }
        return rtn
    }

    @Override
    @Throws(PageException::class)
    fun remove(key: Key?): Object? {
        val _null: Object = NullSupportHelper.NULL()
        if (isReadonly()) throw errorOutside()
        val meta: Object? = getMeta(key, _null)
        if (meta !== _null) throw errorMeta(key)
        return ct!!.content.remove(key)
    }

    @Override
    fun removeEL(key: Key?): Object? {
        return if (isReadonly()) null else ct!!.content.removeEL(key)
    }

    @Override
    @Throws(PageException::class)
    operator fun set(key: Key?, value: Object?): Object? {
        if (isReadonly()) throw errorOutside()
        val _null: Object = NullSupportHelper.NULL()
        val meta: Object? = getMeta(key, _null)
        if (meta !== _null) throw errorMeta(key)
        return ct!!.content.set(key, value)
    }

    @Override
    fun setEL(key: Key?, value: Object?): Object? {
        if (isReadonly()) return null
        val _null: Object = NullSupportHelper.NULL()
        val meta: Object? = getMeta(key, _null)
        return if (meta !== _null) null else ct!!.content.setEL(key, value)
    }

    @Override
    fun size(): Int {
        return ct!!.content.size() + DEFAULT_KEYS!!.size + if (ct!!.catchBlock == null) 0 else 1
    }

    @Override
    fun toDumpData(pageContext: PageContext?, maxlevel: Int, dp: DumpProperties?): DumpData? {
        var maxlevel = maxlevel
        val keys: Array<Key?>? = keys()
        val table = DumpTable("struct", "#9999ff", "#ccccff", "#000000")
        table.setTitle("Struct")
        maxlevel--
        val maxkeys: Int = dp.getMaxKeys()
        var index = 0
        for (i in keys.indices) {
            val key: Key? = keys!![i]
            if (maxkeys <= index++) break
            if (DumpUtil.keyValid(dp, maxlevel, key)) table.appendRow(1, SimpleDumpData(key.getString()), DumpUtil.toDumpData(get(key, null), pageContext, maxlevel, dp))
        }
        return table
    }

    @Override
    fun keyIterator(): Iterator<Collection.Key?>? {
        return KeyIterator(keys())
    }

    @Override
    fun keysAsStringIterator(): Iterator<String?>? {
        return StringIterator(keys())
    }

    @Override
    fun entryIterator(): Iterator<Entry<Key?, Object?>?>? {
        return EntryIterator(this, keys())
    }

    @Override
    fun valueIterator(): Iterator<Object?>? {
        return ValueIterator(this, keys())
    }

    @Override
    @Throws(PageException::class)
    fun castToBooleanValue(): Boolean {
        return ct!!.content.castToBooleanValue()
    }

    @Override
    fun castToBoolean(defaultValue: Boolean?): Boolean? {
        return ct!!.content.castToBoolean(defaultValue)
    }

    @Override
    @Throws(PageException::class)
    fun castToDateTime(): DateTime? {
        return ct!!.content.castToDateTime()
    }

    @Override
    fun castToDateTime(defaultValue: DateTime?): DateTime? {
        return ct!!.content.castToDateTime(defaultValue)
    }

    @Override
    @Throws(PageException::class)
    fun castToDoubleValue(): Double {
        return ct!!.content.castToDoubleValue()
    }

    @Override
    fun castToDoubleValue(defaultValue: Double): Double {
        return ct!!.content.castToDoubleValue(defaultValue)
    }

    @Override
    @Throws(PageException::class)
    fun castToString(): String? {
        return ct!!.content.castToString()
    }

    @Override
    fun castToString(defaultValue: String?): String? {
        return ct!!.content.castToString(defaultValue)
    }

    @Override
    @Throws(PageException::class)
    operator fun compareTo(str: String?): Int {
        return ct!!.content.compareTo(str)
    }

    @Override
    @Throws(PageException::class)
    operator fun compareTo(b: Boolean): Int {
        return ct!!.content.compareTo(b)
    }

    @Override
    @Throws(PageException::class)
    operator fun compareTo(d: Double): Int {
        return ct!!.content.compareTo(d)
    }

    @Override
    @Throws(PageException::class)
    operator fun compareTo(dt: DateTime?): Int {
        return ct!!.content.compareTo(dt)
    }

    private fun isReadonly(): Boolean {
        val pc: PageContext = ThreadLocalPageContext.get() ?: return true
        return pc.getThread() !== ct
    }

    private fun errorOutside(): ApplicationException? {
        return ApplicationException("the thread scope cannot be modified from outside the owner thread")
    }

    private fun errorMeta(key: Key?): ApplicationException? {
        return ApplicationException("the metadata " + key.getString().toString() + " of the thread scope are readonly")
    }

    companion object {
        private val KEY_ERROR: Key? = KeyConstants._ERROR
        private val KEY_ELAPSEDTIME: Key? = KeyImpl.getInstance("ELAPSEDTIME")
        private val KEY_OUTPUT: Key? = KeyConstants._OUTPUT
        private val KEY_PRIORITY: Key? = KeyImpl.getInstance("PRIORITY")
        private val KEY_STARTTIME: Key? = KeyImpl.getInstance("STARTTIME")
        private val KEY_STATUS: Key? = KeyConstants._STATUS
        private val KEY_STACKTRACE: Key? = KeyConstants._STACKTRACE
        private val KEY_CHILD_THREADS: Key? = KeyImpl.getInstance("childThreads")
        private val DEFAULT_KEYS: Array<Key?>? = arrayOf<Key?>(KEY_ELAPSEDTIME, KeyConstants._NAME, KEY_OUTPUT, KEY_PRIORITY, KEY_STARTTIME, KEY_STATUS, KEY_STACKTRACE,
                KEY_CHILD_THREADS)
    }

    init {
        this.ct = ct
    }
}