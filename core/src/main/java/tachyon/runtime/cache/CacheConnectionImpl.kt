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
package tachyon.runtime.cache

import java.io.IOException

class CacheConnectionImpl(config: Config?, private val name: String?, cd: ClassDefinition<Cache?>?, custom: Struct?, readOnly: Boolean, storage: Boolean) : CacheConnectionPlus {
    private val classDefinition: ClassDefinition<Cache?>?
    private val custom: Struct?
    private var cache: Cache? = null
    private val readOnly: Boolean
    private val storage: Boolean
    @Override
    @Throws(IOException::class)
    fun getInstance(config: Config?): Cache? {
        if (cache == null) {
            try {
                var clazz: Class<Cache?> = classDefinition.getClazz()
                if (!Reflector.isInstaneOf(clazz, Cache::class.java, false)) throw CacheException("class [" + clazz.getName().toString() + "] does not implement interface [" + Cache::class.java.getName().toString() + "]")
                var obj: Object = ClassUtil.loadInstance(clazz)
                if (obj is Exception) {
                    throw ExceptionUtil.toIOException(obj as Exception)
                }
                if (obj !is Cache) {
                    clazz = (classDefinition as ClassDefinitionImpl?).getClazz(true)
                    obj = ClassUtil.loadInstance(clazz)
                }
                cache = obj as Cache
            } catch (be: BundleException) {
                throw PageRuntimeException(be)
            }
            try {
                cache.init(config, getName(), getCustom())
            } catch (ioe: IOException) {
                cache = null
                throw ioe
            }
        }
        return cache
    }

    @Override
    override fun getLoadedInstance(): Cache? {
        return cache
    }

    @Override
    fun getName(): String? {
        return name
    }

    @Override
    fun getClassDefinition(): ClassDefinition<Cache?>? {
        return classDefinition
    }

    @Override
    fun getCustom(): Struct? {
        return custom
    }

    @Override
    override fun toString(): String {
        return "name:" + name + ";" + getClassDefinition() + ";custom:" + custom + ";"
    }

    fun id(): String? {
        val sb: StringBuilder = StringBuilder().append(name.toLowerCase()).append(';').append(getClassDefinition()).append(';')
        val _custom: Struct? = getCustom()
        val keys: Array<Key?> = _custom.keys()
        Arrays.sort(keys)
        for (k in keys) {
            sb.append(k).append(':').append(_custom.get(k, null)).append(';')
        }
        return Caster.toString(HashUtil.create64BitHash(sb.toString()))
    }

    @Override
    @Throws(IOException::class)
    fun duplicate(config: Config?): CacheConnection? {
        return CacheConnectionImpl(config, name, classDefinition, custom, readOnly, storage)
    }

    @Override
    fun isReadOnly(): Boolean {
        return readOnly
    }

    @Override
    fun isStorage(): Boolean {
        return storage
    }

    // private Class<Cache> clazz;
    init {
        classDefinition = cd
        this.custom = if (custom == null) StructImpl() else custom
        this.readOnly = readOnly
        this.storage = storage
    }
}