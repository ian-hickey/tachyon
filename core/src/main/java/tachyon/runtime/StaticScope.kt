/**
 * Copyright (c) 2015, Tachyon Assosication Switzerland. All rights reserved.
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
package tachyon.runtime

import java.util.HashMap

class StaticScope(private val base: StaticScope?, c: ComponentImpl?, cp: ComponentPageImpl?, dataMemberDefaultAccess: Int) : StructSupport(), Variables, Objects {
    private val cp: ComponentPageImpl?
    private val dataMemberDefaultAccess: Int
    private val c: ComponentImpl?
    fun getPageSource(): PageSource? {
        return cp.getPageSource()
    }

    @Override
    fun size(): Int {
        val s: Int = cp!!.getStaticStruct().size()
        return if (base == null) s else base.size() + s
    }

    @Throws(PageException::class)
    fun _remove(pc: PageContext?, key: Key?): Member? {
        // does the current struct has this key
        val ss: StaticStruct = cp!!.getStaticStruct()
        val m: Member = ss.get(key)
        if (m != null) {
            if (m.getModifier() === Member.MODIFIER_FINAL) throw ExpressionException("Cannot remove key [" + key + "] in static scope from component [" + cp!!.getComponentName() + "], that member is set to final")
            if (!c.isAccessible(ThreadLocalPageContext.get(pc), m.getAccess())) throw notExisting(key)
            return ss.remove(key)
        }
        // if not the parent (inside the static constructor we do not remove keys from base static scopes)
        return if (base != null && !c!!.insideStaticConstrThread.get()) base._remove(pc, key) else null
    }

    @Override
    @Throws(PageException::class)
    fun remove(key: Key?): Object? {
        val m: Member? = _remove(ThreadLocalPageContext.get(), key)
        return if (m != null) m.getValue() else null
    }

    @Override
    fun removeEL(key: Key?): Object? {
        return try {
            remove(key)
        } catch (t: Throwable) {
            ExceptionUtil.rethrowIfNecessary(t)
            null
        }
    }

    @Override
    fun clear() {
        base?.clear()
        cp!!.getStaticStruct().clear()
    }

    private fun _get(pc: PageContext?, key: Key?, defaultValue: Member?): Member? {
        // does the current struct has this key
        val ss: StaticStruct = cp!!.getStaticStruct()
        if (!ss.isEmpty()) {
            val m: Member = ss.get(key)
            if (m != null) {
                return if (c.isAccessible(pc, m)) m else null
            }
        }
        // if not the parent
        return base?._get(pc, key, defaultValue)
    }

    @Override
    @Throws(PageException::class)
    operator fun get(key: Key?): Object? {
        return get(null as PageContext?, key)
    }

    @Override
    @Throws(PageException::class)
    operator fun get(pc: PageContext?, key: Key?): Object? {
        if (key.equalsIgnoreCase(KeyConstants._STATIC)) return c!!.top!!._static
        val m: Member? = _get(ThreadLocalPageContext.get(pc), key, null)
        if (m != null) return m.getValue()
        throw notExisting(key)
    }

    @Override
    operator fun get(key: Key?, defaultValue: Object?): Object? {
        return get(null, key, defaultValue)
    }

    @Override
    operator fun get(pc: PageContext?, key: Key?, defaultValue: Object?): Object? {
        if (key.equalsIgnoreCase(KeyConstants._STATIC)) return c!!.top!!._static
        val m: Member? = _get(ThreadLocalPageContext.get(pc), key, null)
        return if (m != null) m.getValue() else defaultValue
    }

    fun getMember(pc: PageContext?, key: Key?, defaultValue: Member?): Member? {
        return _get(ThreadLocalPageContext.get(pc), key, null)
    }

    @Throws(PageException::class)
    private fun _setIfExists(pc: PageContext?, key: Key?, value: Object?): Member? {
        // does the current struct has this key
        val ss: StaticStruct = cp!!.getStaticStruct()
        val m: Member = ss.get(key)
        if (m != null) {
            if (m.getModifier() === Member.MODIFIER_FINAL) throw ExpressionException("Cannot update key [" + key + "] in static scope from component [" + cp!!.getComponentName() + "], that member is set to final")
            return _set(pc, m, key, value)
        }

        // if not the parent (we only do this if we are outside the static constructor)
        return if (base != null && !c!!.insideStaticConstrThread.get()) base._setIfExists(pc, key, value) else null
    }

    @Throws(ExpressionException::class)
    private fun _set(pc: PageContext?, existing: Member?, key: Key?, value: Object?): Member? {
        if (value is Member) {
            return cp!!.getStaticStruct().put(key, value as Member?)
        }

        // check if user has access
        if (!c.isAccessible(pc, if (existing != null) existing.getAccess() else dataMemberDefaultAccess)) throw notExisting(key)

        // set
        return cp!!.getStaticStruct().put(key,
                DataMember(if (existing != null) existing.getAccess() else dataMemberDefaultAccess, if (existing != null) existing.getModifier() else Member.MODIFIER_NONE, value))
    }

    @Override
    @Throws(PageException::class)
    operator fun set(propertyName: Key?, value: Object?): Object? {
        return set(null, propertyName, value)
    }

    @Override
    @Throws(PageException::class)
    operator fun set(pc: PageContext?, key: Key?, value: Object?): Object? {
        var pc: PageContext? = pc
        pc = ThreadLocalPageContext.get(pc)
        var m: Member? = _setIfExists(pc, key, value)
        if (m != null) return m.getValue()
        // if not exists set to current
        m = _set(pc, null, key, value)
        return if (m != null) m.getValue() else null
    }

    @Override
    fun setEL(key: Key?, value: Object?): Object? {
        return setEL(null, key, value)
    }

    @Override
    fun setEL(pc: PageContext?, propertyName: Key?, value: Object?): Object? {
        return try {
            set(ThreadLocalPageContext.get(pc), propertyName, value)
        } catch (e: PageException) {
            null
        }
    }

    @Override
    fun duplicate(deepCopy: Boolean): Collection? {
        return this
    }

    @Override
    fun containsKey(key: Key?): Boolean {
        return if (base != null && base.containsKey(key)) true else cp!!.getStaticStruct().containsKey(key)
    }

    @Override
    fun containsKey(pc: PageContext?, key: Key?): Boolean {
        return if (base != null && base.containsKey(pc, key)) true else cp!!.getStaticStruct().containsKey(key)
    }

    @Override
    fun keys(): Array<Key?>? {
        val keys: Set<Key?> = _entries(HashMap<Key?, Object?>(), c!!.getAccess(ThreadLocalPageContext.get())).keySet()
        return keys.toArray(arrayOfNulls<Key?>(keys.size()))
    }

    @Override
    fun keyIterator(): Iterator<Key?>? {
        return _entries(HashMap<Key?, Object?>(), c!!.getAccess(ThreadLocalPageContext.get())).keySet().iterator()
    }

    @Override
    fun valueIterator(): Iterator<Object?>? {
        return _entries(HashMap<Key?, Object?>(), c!!.getAccess(ThreadLocalPageContext.get()))!!.values().iterator()
    }

    @Override
    fun entryIterator(): Iterator<Entry<Key?, Object?>?>? {
        return _entries(HashMap<Key?, Object?>(), c!!.getAccess(ThreadLocalPageContext.get())).entrySet().iterator()
    }

    fun entryIterator(access: Int): Iterator<Entry<Key?, Object?>?>? {
        return _entries(HashMap<Key?, Object?>(), access).entrySet().iterator()
    }

    fun _entries(map: Map<Key?, Object?>?, access: Int): Map<Key?, Object?>? {
        // call parent
        base?._entries(map, access)

        // fill accessable keys
        val ss: StaticStruct = cp!!.getStaticStruct()
        val it: Iterator<Entry<Key?, Member?>?> = ss.entrySet().iterator()
        var e: Entry<Key?, Member?>?
        while (it.hasNext()) {
            e = it.next()
            if (e.getValue().getAccess() <= access) map.put(e.getKey(), e.getValue().getValue())
        }
        return map
    }

    private fun all(map: Map<Key?, Member?>?): Map<Key?, Member?>? {
        // call parent
        base?.all(map)

        // fill accessable keys
        val ss: StaticStruct = cp!!.getStaticStruct()
        val it: Iterator<Entry<Key?, Member?>?> = ss.entrySet().iterator()
        var e: Entry<Key?, Member?>?
        while (it.hasNext()) {
            e = it.next()
            map.put(e.getKey(), e.getValue())
        }
        return map
    }

    @Override
    @Throws(PageException::class)
    fun call(pc: PageContext?, key: Key?, args: Array<Object?>?): Object? {
        val m: Member? = _get(pc, key, null)
        if (m is UDF) {
            return _call(pc, key, m as UDF?, null, args)
        }
        throw notExisting(key)
    }

    @Override
    @Throws(PageException::class)
    fun callWithNamedValues(pc: PageContext?, key: Key?, args: Struct?): Object? {
        val m: Member? = _get(pc, key, null)
        if (m is UDF) {
            return _call(pc, key, m as UDF?, args, null)
        }
        throw notExisting(key)
    }

    @Throws(PageException::class)
    fun _call(pc: PageContext?, calledName: Collection.Key?, udf: UDF?, namedArgs: Struct?, args: Array<Object?>?): Object? {
        var rtn: Object? = null
        var parent: Variables? = null

        // INFO duplicate code is for faster execution -> less contions

        // debug yes
        if (pc.getConfig().debug() && (pc.getConfig() as ConfigPro).hasDebugOptions(ConfigPro.DEBUG_TEMPLATE)) {
            val debugEntry: DebugEntryTemplate = pc.getDebugger().getEntry(pc, cp.getPageSource(), udf.getFunctionName()) // new DebugEntry(src,udf.getFunctionName());
            val currTime: Long = pc.getExecutionTime()
            val time: Long = System.nanoTime()
            try {
                parent = beforeStaticCall(pc, c, this)
                rtn = if (args != null) udf.call(pc, calledName, args, true) else udf.callWithNamedValues(pc, calledName, namedArgs, true)
            } finally {
                if (parent != null) afterStaticCall(pc, c, parent)
                val diff: Long = System.nanoTime() - time - (pc.getExecutionTime() - currTime)
                pc.setExecutionTime(pc.getExecutionTime() + diff)
                debugEntry.updateExeTime(diff)
            }
        } else { // this.cp._static
            try {
                parent = beforeStaticCall(pc, c, this)
                rtn = if (args != null) udf.call(pc, calledName, args, true) else udf.callWithNamedValues(pc, calledName, namedArgs, true)
            } finally {
                if (parent != null) afterStaticCall(pc, c, parent)
            }
        }
        return rtn
    }

    @Override
    fun isInitalized(): Boolean {
        return true
    }

    @Override
    fun initialize(pc: PageContext?) {
    }

    @Override
    fun release(pc: PageContext?) {
    }

    @Override
    fun getType(): Int {
        return SCOPE_VARIABLES
    }

    @Override
    fun getTypeAsString(): String? {
        return "variables"
    }

    @Override
    fun setBind(bind: Boolean) {
    }

    @Override
    fun isBind(): Boolean {
        return true
    }

    @Override
    fun toDumpData(pageContext: PageContext?, maxlevel: Int, dp: DumpProperties?): DumpData? {
        val access: Int = c!!.getAccess(pageContext)
        val table = DumpTable("component", "#99cc99", "#ccffcc", "#000000")
        table.setTitle("Static Scope from Component " + cp!!.getComponentName())
        table.setComment("Only the functions and data members that are accessible from your location are displayed")
        val content: DumpTable? = _toDumpData(c!!.top, pageContext, maxlevel, dp, access)
        if (!content.isEmpty()) table.appendRow(1, SimpleDumpData(""), content)
        return table
    }

    fun _toDumpData(ci: ComponentImpl?, pc: PageContext?, maxlevel: Int, dp: DumpProperties?, access: Int): DumpTable? {
        var maxlevel = maxlevel
        maxlevel--
        val accesses: Array<DumpTable?> = arrayOfNulls<DumpTable?>(4)
        accesses[Component.ACCESS_PRIVATE] = DumpTable("#ff6633", "#ff9966", "#000000")
        accesses[Component.ACCESS_PRIVATE].setTitle("private")
        accesses[Component.ACCESS_PRIVATE].setWidth("100%")
        accesses[Component.ACCESS_PACKAGE] = DumpTable("#ff9966", "#ffcc99", "#000000")
        accesses[Component.ACCESS_PACKAGE].setTitle("package")
        accesses[Component.ACCESS_PACKAGE].setWidth("100%")
        accesses[Component.ACCESS_PUBLIC] = DumpTable("#ffcc99", "#ffffcc", "#000000")
        accesses[Component.ACCESS_PUBLIC].setTitle("public")
        accesses[Component.ACCESS_PUBLIC].setWidth("100%")
        accesses[Component.ACCESS_REMOTE] = DumpTable("#ccffcc", "#ffffcc", "#000000")
        accesses[Component.ACCESS_REMOTE].setTitle("remote")
        accesses[Component.ACCESS_REMOTE].setWidth("100%")
        val it: Iterator<Entry<Key?, Member?>?> = all(HashMap<Key?, Member?>()).entrySet().iterator()
        var e: Entry<Key?, Member?>?
        while (it.hasNext()) {
            e = it.next()
            val a = access(pc, e.getValue().getAccess())
            val box: DumpTable? = accesses[a]
            val o: Object = e.getValue().getValue()
            if (DumpUtil.keyValid(dp, maxlevel, e.getKey())) {
                box.appendRow(1, SimpleDumpData(e.getKey().getString()), DumpUtil.toDumpData(o, pc, maxlevel, dp))
            }
        }
        val table = DumpTable("#ffffff", "#cccccc", "#000000")
        if (!accesses[Component.ACCESS_REMOTE].isEmpty()) {
            table.appendRow(0, accesses[Component.ACCESS_REMOTE])
        }
        if (!accesses[Component.ACCESS_PUBLIC].isEmpty()) {
            table.appendRow(0, accesses[Component.ACCESS_PUBLIC])
        }
        if (!accesses[Component.ACCESS_PACKAGE].isEmpty()) {
            table.appendRow(0, accesses[Component.ACCESS_PACKAGE])
        }
        if (!accesses[Component.ACCESS_PRIVATE].isEmpty()) {
            table.appendRow(0, accesses[Component.ACCESS_PRIVATE])
        }
        return table
    }

    private fun access(pc: PageContext?, access: Int): Int {
        return if (access > -1) access else pc.getConfig().getComponentDataMemberDefaultAccess()
    }

    fun getComponent(): Component? {
        return c
    }

    private fun notExisting(key: Collection.Key?): ExpressionException? {
        return ExpressionException(
                ExceptionUtil.similarKeyMessage(this, key.getString(), "static member", "static members", "Component [" + cp!!.getComponentName().toString() + "]", true))
    }

    fun index(): Long {
        return cp!!.getStaticStruct().index()
    }

    companion object {
        private const val serialVersionUID = -2692540782121852340L
        fun beforeStaticConstructor(pc: PageContext?, c: ComponentImpl?, ss: StaticScope?): Variables? {
            c!!.insideStaticConstrThread.set(Boolean.TRUE)
            val parent: Variables = pc.variablesScope()
            if (parent !== ss) {
                pc.setVariablesScope(ss)
                return parent
            }
            return null
        }

        fun afterStaticConstructor(pc: PageContext?, c: ComponentImpl?, parent: Variables?) {
            c!!.insideStaticConstrThread.set(Boolean.FALSE)
            if (parent != null) pc.setVariablesScope(parent)
        }

        fun beforeStaticCall(pc: PageContext?, c: ComponentImpl?, ss: StaticScope?): Variables? {
            val parent: Variables = pc.variablesScope()
            // only if we are not already in that context
            if (parent !== ss) {
                pc.setVariablesScope(ss)
                return parent
            }
            return null
        }

        fun afterStaticCall(pc: PageContext?, c: ComponentImpl?, parent: Variables?) {
            pc.setVariablesScope(parent)
        }
    }

    init {
        this.cp = cp
        this.c = c
        this.dataMemberDefaultAccess = dataMemberDefaultAccess
    }
}