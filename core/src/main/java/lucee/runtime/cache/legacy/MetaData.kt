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
package lucee.runtime.cache.legacy

import java.io.IOException

class MetaData : Serializable {
    private var data: HashMap<String?, String?>? = HashMap<String?, String?>()
    private var file: Resource?

    private constructor(file: Resource?) {
        this.file = file
        data = HashMap<String?, String?>()
    }

    constructor(file: Resource?, data: HashMap<String?, String?>?) {
        this.file = file
        this.data = data
    }

    @Throws(IOException::class)
    fun add(name: String?, raw: String?) {
        synchronized(data) {
            data.put(name, raw)
            JavaConverter.serialize(data, file)
        }
    }

    @Throws(IOException::class)
    operator fun get(wildcard: String?): List<String?>? {
        synchronized(data) {
            val list: List<String?> = ArrayList<String?>()
            val it: Iterator<Entry<String?, String?>?> = data.entrySet().iterator()
            val filter = WildCardFilter(wildcard)
            var entry: Entry<String?, String?>?
            var value: String
            while (it.hasNext()) {
                entry = it.next()
                value = entry.getValue()
                if (filter.accept(value)) {
                    list.add(entry.getKey())
                    it.remove()
                }
            }
            if (list.size() > 0) JavaConverter.serialize(data, file)
            return list
        }
    }

    companion object {
        private val instances: Map<String?, MetaData?>? = HashMap<String?, MetaData?>()
        fun getInstance(directory: Resource?): MetaData? {
            var instance = instances!![directory.getAbsolutePath()]
            if (instance == null) {
                val file: Resource = directory.getRealResource("meta")
                if (file.exists()) {
                    try {
                        instance = MetaData(file, JavaConverter.deserialize(file) as HashMap)
                    } catch (t: Throwable) {
                        ExceptionUtil.rethrowIfNecessary(t)
                    }
                }
                if (instance == null) instance = MetaData(file)
                instances.put(directory.getAbsolutePath(), instance)
            }
            return instance
        }
    }
}