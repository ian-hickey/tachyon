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
package tachyon.runtime.type.scope

import java.util.ArrayList

class RequestImpl : StructSupport(), Request {
    private var _req: HttpServletRequest? = null
    private var init = false
    private var id = 0

    /**
     * @return Returns the id.
     */
    fun _getId(): Int {
        return id
    }

    @Override
    fun initialize(pc: PageContext?) {
        _req = pc.getHttpServletRequest() // HTTPServletRequestWrap.pure(pc.getHttpServletRequest());
        init = true
    }

    @Override
    fun isInitalized(): Boolean {
        return init
    }

    @Override
    fun release(pc: PageContext?) {
        init = false
    }

    @Override
    fun getType(): Int {
        return SCOPE_REQUEST
    }

    @Override
    fun getTypeAsString(): String? {
        return "request"
    }

    @Override
    fun size(): Int {
        var size = 0
        synchronized(_req) {
            val names: Enumeration<String?> = _req.getAttributeNames()
            while (names.hasMoreElements()) {
                names.nextElement()
                size++
            }
        }
        return size
    }

    @Override
    fun keyIterator(): Iterator<Collection.Key?>? {
        return keyList()!!.iterator()
    }

    private fun keyList(): List<Key?>? {
        synchronized(_req) {
            val names: Enumeration<String?> = _req.getAttributeNames()
            val list: List<Key?> = ArrayList<Key?>()
            while (names.hasMoreElements()) {
                list.add(KeyImpl.init(names.nextElement()))
            }
            return list
        }
    }

    @Override
    fun entryIterator(): Iterator<Entry<Key?, Object?>?>? {
        return EntryIterator(this, keys())
    }

    @Override
    fun valueIterator(): Iterator<Object?>? {
        synchronized(_req) {
            val names: Enumeration<String?> = _req.getAttributeNames()
            val list: List<Object?> = ArrayList<Object?>()
            while (names.hasMoreElements()) {
                list.add(_req.getAttribute(names.nextElement()))
            }
            return list.iterator()
        }
    }

    @Override
    fun keys(): Array<Key?>? {
        val list: List<Key?>? = keyList()
        return list.toArray(arrayOfNulls<Key?>(list!!.size()))
    }

    @Override
    @Throws(PageException::class)
    fun remove(key: Key?): Object? {
        val _null: Object = NullSupportHelper.NULL()
        val value: Object? = remove(key, _null)
        if (value !== _null) return value
        throw ExpressionException("can't remove key [$key] from struct, key doesn't exist")
    }

    @Override
    fun clear() {
        synchronized(_req) {
            val names: Iterator<String?> = ListUtil.toIterator(_req.getAttributeNames())
            while (names.hasNext()) {
                _req.removeAttribute(names.next())
            }
        }
    }

    @Override
    @Throws(PageException::class)
    operator fun get(key: Key?): Object? {
        val _null: Object = NullSupportHelper.NULL()
        val value: Object = get(key, _null)
        if (value === _null) throw invalidKey(null, this, key, "request scope")
        return value
    }

    @Override
    @Throws(PageException::class)
    operator fun get(pc: PageContext?, key: Key?): Object? {
        val _null: Object = NullSupportHelper.NULL(pc)
        val value: Object? = get(pc, key, _null)
        if (value === _null) throw invalidKey(null, this, key, "request scope")
        return value
    }

    @Override
    fun removeEL(key: Key?): Object? {
        return remove(key, null)
    }

    @Override
    fun remove(key: Key?, defaultValue: Object?): Object? {
        synchronized(_req) {
            var value: Object? = _req.getAttribute(key.getLowerString())
            if (value != null) {
                _req.removeAttribute(key.getLowerString())
                return value
            }
            value = defaultValue
            val it: Iterator<String?> = ListUtil.toIterator(_req.getAttributeNames())
            var k: String?
            while (it.hasNext()) {
                k = it.next()
                if (k.equalsIgnoreCase(key.getString())) {
                    value = _req.getAttribute(k)
                    _req.removeAttribute(k)
                    return value
                }
            }
            return defaultValue
        }
    }

    @Override
    operator fun get(key: Key?, defaultValue: Object?): Object? {
        return get(null as PageContext?, key, defaultValue)
    }

    @Override
    operator fun get(pc: PageContext?, key: Key?, defaultValue: Object?): Object? {
        synchronized(_req) {
            val value: Object = _req.getAttribute(key.getLowerString())
            if (value != null) return value
            val names: Enumeration<String?> = _req.getAttributeNames()
            var k: Collection.Key
            while (names.hasMoreElements()) {
                k = KeyImpl.init(names.nextElement())
                if (key.equals(k)) return _req.getAttribute(k.getString())
            }
            return defaultValue
        }
    }

    @Override
    fun setEL(key: Key?, value: Object?): Object? {
        synchronized(_req) { _req.setAttribute(key.getLowerString(), value) }
        return value
    }

    @Override
    @Throws(PageException::class)
    operator fun set(key: Key?, value: Object?): Object? {
        return setEL(key, value)
    }

    @Override
    fun duplicate(deepCopy: Boolean): Collection? {
        val trg: Struct = StructImpl()
        StructImpl.copy(this, trg, deepCopy)
        return trg
    }

    @Override
    fun containsKey(key: Key?): Boolean {
        val _null: Object = NullSupportHelper.NULL()
        return get(key, _null) !== _null
    }

    @Override
    fun containsKey(pc: PageContext?, key: Key?): Boolean {
        val _null: Object = NullSupportHelper.NULL(pc)
        return get(pc, key, _null) !== _null
    }

    @Override
    fun toDumpData(pageContext: PageContext?, maxlevel: Int, dp: DumpProperties?): DumpData? {
        return ScopeSupport.toDumpData(pageContext, maxlevel, dp, this, getTypeAsString())
    }

    companion object {
        private var _id = 0
    }

    init {
        id = ++_id
        // super("request",SCOPE_REQUEST,Struct.TYPE_REGULAR);
    }
}