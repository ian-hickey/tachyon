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
package lucee.runtime.type.scope

import java.io.Serializable

class ClusterWrap private constructor(configServer: ConfigServer?, core: ClusterRemote?, duplicate: Boolean) : ScopeSupport("cluster", Scope.SCOPE_CLUSTER, Struct.TYPE_LINKED), Cluster {
    private val core: ClusterRemote? = null
    private val offset = 0
    private val configServer: ConfigServer?

    constructor(cs: ConfigServer?, core: ClusterRemote?) : this(cs, core, false) {}

    @Override
    fun init(configServer: ConfigServer?) {
        // for the custer wrap this method is not invoked, but it is part of the interface
    }

    @Override
    @Throws(PageException::class)
    operator fun get(key: Key?): Object? {
        return (super.get(key) as ClusterEntry?).getValue()
    }

    @Override
    @Throws(PageException::class)
    operator fun get(pc: PageContext?, key: Key?): Object? {
        return (super.get(pc, key) as ClusterEntry?).getValue()
    }

    @Override
    operator fun get(key: Key?, defaultValue: Object?): Object? {
        val res: Object = super.get(key, defaultValue)
        return if (res is ClusterEntry) (res as ClusterEntry).getValue() else res
    }

    @Override
    operator fun get(pc: PageContext?, key: Key?, defaultValue: Object?): Object? {
        val res: Object = super.get(pc, key, defaultValue)
        return if (res is ClusterEntry) (res as ClusterEntry).getValue() else res
    }

    @Override
    @Throws(PageException::class)
    fun remove(key: Key?): Object? {
        core.addEntry(ClusterEntryImpl(key, null, offset))
        return (super.remove(key) as ClusterEntry?).getValue()
    }

    @Override
    fun removeEL(key: Key?): Object? {
        core.addEntry(ClusterEntryImpl(key, null, offset))
        val entry: ClusterEntry? = super.removeEL(key) as ClusterEntry?
        return if (entry != null) entry.getValue() else null
    }

    @Override
    fun setEL(key: Key?, value: Object?): Object? {
        if (core.checkValue(value)) {
            var entry: ClusterEntry?
            core.addEntry(ClusterEntryImpl(key, value as Serializable?, offset).also { entry = it })
            super.setEL(key, entry)
        }
        return value
    }

    @Override
    fun setEntry(newEntry: ClusterEntry?) {
        val existingEntry: ClusterEntry? = super.get(newEntry.getKey(), null) as ClusterEntry?
        // add
        if (existingEntry == null || existingEntry.getTime() < newEntry.getTime()) {
            if (newEntry.getValue() == null) removeEL(newEntry.getKey()) else {
                core.addEntry(newEntry)
                super.setEL(newEntry.getKey(), newEntry)
            }
        }
    }

    @Override
    @Throws(PageException::class)
    operator fun set(key: Key?, value: Object?): Object? {
        if (!core.checkValue(value)) throw ExpressionException("object from type [" + Caster.toTypeName(value).toString() + "] are not allowed in cluster scope")
        var entry: ClusterEntry?
        core.addEntry(ClusterEntryImpl(key, value as Serializable?, offset).also { entry = it })
        super.setEL(key, entry)
        return value
    }

    @Override
    override fun toDumpData(pageContext: PageContext?, maxlevel: Int, dp: DumpProperties?): DumpData? {
        return super.toDumpData(pageContext, maxlevel, dp)
    }

    @Override
    override fun getType(): Int {
        return SCOPE_CLUSTER
    }

    @Override
    override fun getTypeAsString(): String? {
        return "cluster"
    }

    @Override
    fun duplicate(deepCopy: Boolean): Collection? {
        return ClusterWrap(configServer, core, true)
    }

    @Override
    fun broadcast() {
        core.broadcastEntries()
    }

    companion object {
        private const val serialVersionUID = -4952656252539755770L
    }

    init {
        this.configServer = configServer
        if (duplicate) this.core = core.duplicate() else this.core = core
        this.core.init(configServer, this)
    }
}