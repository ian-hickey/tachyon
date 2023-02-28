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
package lucee.runtime.type.scope.storage.clean

import java.io.IOException

class CacheStorageScopeCleaner(type: Int, listener: StorageScopeListener?) : StorageScopeCleanerSupport(type, listener, INTERVALL_MINUTE) {
    private val filter: Filter?

    @Override
    override fun init(engine: StorageScopeEngine?) {
        super.init(engine)
    }

    @Override
    protected override fun _clean() {
        val config: ConfigWeb = engine.getFactory().getConfig()
        val connections: Map<String?, CacheConnection?> = config.getCacheConnections()
        var cc: CacheConnection
        if (connections != null) {
            var entry: Map.Entry<String?, CacheConnection?>
            val it: Iterator<Entry<String?, CacheConnection?>?> = connections.entrySet().iterator()
            while (it.hasNext()) {
                entry = it.next()
                cc = entry.getValue()
                if (cc.isStorage()) {
                    try {
                        clean(cc, config)
                    } catch (e: IOException) {
                        error(e)
                    }
                }
            }
        }
    }

    @Throws(IOException::class)
    private fun clean(cc: CacheConnection?, config: ConfigWeb?) {
        val cache: Cache = cc.getInstance(config)
        val len = filter!!.length()
        var index: Int
        val entries: List<CacheEntry?> = cache.entries(filter)
        var ce: CacheEntry
        var expires: Long
        var key: String
        var appname: String?
        var cfid: String?
        if (entries.size() > 0) {
            val it: Iterator<CacheEntry?> = entries.iterator()
            while (it.hasNext()) {
                ce = it.next()
                val lm: Date = ce.lastModified()
                val time: Long = if (lm != null) lm.getTime() else 0
                expires = time + ce.idleTimeSpan() - SAVE_EXPIRES_OFFSET
                if (expires <= System.currentTimeMillis()) {
                    key = ce.getKey().substring(len)
                    index = key.indexOf(':')
                    cfid = key.substring(0, index)
                    appname = key.substring(index + 1)
                    if (listener != null) listener.doEnd(engine, this, appname, cfid)
                    info("remove " + strType.toString() + "/" + appname.toString() + "/" + cfid.toString() + " from cache " + cc.getName())
                    engine.remove(type, appname, cfid)
                    cache.remove(ce.getKey())
                }
            }
        }

        // engine.remove(type,appName,cfid);

        // return (Struct) cache.getValue(key,null);
    }

    class Filter(type: String?) : CacheKeyFilter {
        private val startsWith: String?
        @Override
        fun toPattern(): String? {
            return startsWith.toString() + "*"
        }

        @Override
        fun accept(key: String?): Boolean {
            return key.startsWith(startsWith)
        }

        fun length(): Int {
            return startsWith!!.length()
        }

        init {
            startsWith = StringBuilder("lucee-storage:").append(type).append(":").toString().toUpperCase()
        }
    }

    companion object {
        const val SAVE_EXPIRES_OFFSET = (60 * 60 * 1000).toLong()
    }

    init {
        // this.strType=VariableInterpreter.scopeInt2String(type);
        filter = Filter(strType)
    }
}