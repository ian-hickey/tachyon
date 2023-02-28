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
package lucee.runtime.type.scope

import java.io.Externalizable

class ClosureScope : ScopeSupport, Variables, Externalizable {
    private var arg: Argument? = null
    private var local: Local? = null
    private var `var`: Variables? = null
    private var debug = false
    private var localAlways = false

    constructor(pc: PageContext?, arg: Argument?, local: Local?, `var`: Variables?) : super("variables", SCOPE_VARIABLES, StructImpl.TYPE_UNDEFINED) {
        arg.setBind(true)
        local.setBind(true)
        `var`.setBind(true)
        localAlways = pc.undefinedScope().getLocalAlways()
        this.arg = arg
        this.local = local
        this.`var` = `var`
        debug = pc.getConfig().debug()
    }

    /*
	 * ONLY USED BY SERIALISATION
	 */
    constructor() : super("variables", SCOPE_VARIABLES, StructImpl.TYPE_UNDEFINED) {}

    @Override
    @Throws(IOException::class)
    fun writeExternal(out: ObjectOutput?) {
        out.writeObject(arg)
        out.writeObject(local)
        out.writeObject(prepare(`var`))
        out.writeBoolean(debug)
        out.writeBoolean(localAlways)
    }

    @Override
    @Throws(IOException::class, ClassNotFoundException::class)
    fun readExternal(`in`: ObjectInput?) {
        arg = `in`.readObject() as Argument
        local = `in`.readObject() as Local
        `var` = `in`.readObject() as Variables
        debug = `in`.readBoolean()
        localAlways = `in`.readBoolean()
    }

    fun getArgument(): Argument? {
        return arg
    }

    fun getVariables(): Variables? {
        return `var`
    }

    @Override
    override fun isInitalized(): Boolean {
        return true
    }

    @Override
    override fun initialize(pc: PageContext?) {
    }

    @Override
    override fun release(pc: PageContext?) {
    }

    @Override
    override fun getType(): Int {
        return SCOPE_VARIABLES
    }

    @Override
    override fun getTypeAsString(): String? {
        return "variables"
    }

    @Override
    fun size(): Int {
        return `var`.size()
    }

    @Override
    fun keys(): Array<Key?>? {
        return `var`.keys()
    }

    @Override
    @Throws(PageException::class)
    fun remove(key: Key?): Object? {
        return if (local.containsKey(key)) local.remove(key) else `var`.remove(key)
    }

    @Override
    fun removeEL(key: Key?): Object? {
        return if (local.containsKey(key)) local.removeEL(key) else `var`.removeEL(key)
    }

    @Override
    fun clear() {
        `var`.clear()
    }

    @Override
    @Throws(PageException::class)
    operator fun get(key: Key?): Object? {
        return get(null as PageContext?, key)
    }

    @Override
    @Throws(PageException::class)
    operator fun get(pc: PageContext?, key: Key?): Object? {
        val _null: Object = CollectionUtil.NULL
        var value: Object = local.get(pc, key, _null)
        if (value !== _null) return value
        value = arg.get(pc, key, _null)
        if (value !== _null) {
            if (debug) UndefinedImpl.debugCascadedAccess(ThreadLocalPageContext.get(pc), arg.getTypeAsString(), key)
            return value
        }
        value = `var`.get(pc, key)
        if (debug) UndefinedImpl.debugCascadedAccess(ThreadLocalPageContext.get(pc), `var`.getTypeAsString(), key)
        return value
    }

    @Override
    operator fun get(key: Key?, defaultValue: Object?): Object? {
        return get(null, key, defaultValue)
    }

    @Override
    operator fun get(pc: PageContext?, key: Key?, defaultValue: Object?): Object? {
        val _null: Object = CollectionUtil.NULL

        // local
        var value: Object = local.get(pc, key, _null)
        if (value !== _null) return value

        // arg
        value = arg.get(pc, key, _null)
        if (value !== _null) {
            if (debug) UndefinedImpl.debugCascadedAccess(ThreadLocalPageContext.get(pc), arg.getTypeAsString(), key)
            return value
        }

        // var
        value = `var`.get(pc, key, _null)
        if (value !== _null) {
            if (debug) UndefinedImpl.debugCascadedAccess(ThreadLocalPageContext.get(pc), `var`.getTypeAsString(), key)
            return value
        }
        return defaultValue
    }

    @Override
    @Throws(PageException::class)
    operator fun set(key: Key?, value: Object?): Object? {
        if (localAlways || local.containsKey(key)) return local.set(key, value)
        if (arg.containsKey(key)) {
            if (debug) UndefinedImpl.debugCascadedAccess(ThreadLocalPageContext.get(), arg.getTypeAsString(), key)
            return arg.set(key, value)
        }
        if (debug) UndefinedImpl.debugCascadedAccess(ThreadLocalPageContext.get(), `var`.getTypeAsString(), key)
        return `var`.set(key, value)
    }

    @Override
    fun setEL(key: Key?, value: Object?): Object? {
        if (localAlways || local.containsKey(key)) return local.setEL(key, value)
        if (arg.containsKey(key)) {
            if (debug) UndefinedImpl.debugCascadedAccess(ThreadLocalPageContext.get(), arg.getTypeAsString(), key)
            return arg.setEL(key, value)
        }
        if (debug) UndefinedImpl.debugCascadedAccess(ThreadLocalPageContext.get(), `var`.getTypeAsString(), key)
        return `var`.setEL(key, value)
    }

    @Override
    fun duplicate(deepCopy: Boolean): Collection? {
        return ClosureScope(ThreadLocalPageContext.get(), Duplicator.duplicate(arg, deepCopy) as Argument, Duplicator.duplicate(local, deepCopy) as Local,
                Duplicator.duplicate(`var`, deepCopy) as Variables)
    }

    @Override
    fun containsKey(key: Key?): Boolean {
        return get(key, CollectionUtil.NULL) !== CollectionUtil.NULL
    }

    @Override
    fun containsKey(pc: PageContext?, key: Key?): Boolean {
        return get(pc, key, CollectionUtil.NULL) !== CollectionUtil.NULL
    }

    @Override
    fun keyIterator(): Iterator<Collection.Key?>? {
        return `var`.keyIterator()
    }

    @Override
    fun keysAsStringIterator(): Iterator<String?>? {
        return `var`.keysAsStringIterator()
    }

    @Override
    fun entryIterator(): Iterator<Entry<Key?, Object?>?>? {
        return `var`.entryIterator()
    }

    @Override
    fun valueIterator(): Iterator<Object?>? {
        return `var`.valueIterator()
    }

    @Override
    fun setBind(bind: Boolean) {
    }

    @Override
    fun isBind(): Boolean {
        return true
    }

    @Override
    override fun toDumpData(pageContext: PageContext?, maxlevel: Int, properties: DumpProperties?): DumpData? {
        val dt: DumpTable? = super.toDumpData(pageContext, maxlevel, properties) as DumpTable?
        dt.setTitle("Closure Variable Scope")
        return dt
    }

    companion object {
        fun prepare(`var`: Variables?): Variables? {
            if (`var` !is ComponentScope) return `var`
            val rtn = VariablesImpl()
            val it: Iterator<Entry<Key?, Object?>?> = `var`.entryIterator()
            var e: Entry<Key?, Object?>?
            while (it.hasNext()) {
                e = it.next()
                if (KeyConstants._this.equals(e.getKey()) && e.getValue() is Component) break
                rtn.setEL(e.getKey(), e.getValue())
            }
            rtn!!.initialize(null)
            return rtn
        }
    }
}