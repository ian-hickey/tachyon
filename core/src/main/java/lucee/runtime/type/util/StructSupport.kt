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
package lucee.runtime.type.util

import java.util.Iterator

abstract class StructSupport : Map, Struct, Cloneable {
    @Override
    fun entrySet(): Set? {
        return StructUtil.entrySet(this)
    }

    @Override
    operator fun get(key: Object?): Object? {
        return get(KeyImpl.toKey(key, null), null)
    }

    @get:Override
    val isEmpty: Boolean
        get() = size() === 0

    @Override
    fun keySet(): Set? {
        return StructUtil.keySet(this)
    }

    @Override
    fun put(key: Object?, value: Object?): Object? {
        return setEL(KeyImpl.toKey(key, null), value)
    }

    @Override
    fun putAll(t: Map?) {
        StructUtil.putAll(this, t)
    }

    @Override
    fun remove(key: Object?): Object? {
        return removeEL(KeyImpl.toKey(key, null))
    }

    @Override
    fun remove(key: Collection.Key?, defaultValue: Object?): Object? {
        return try {
            remove(key)
        } catch (e: PageException) {
            defaultValue
        }
    }

    @Override
    fun clone(): Object {
        return duplicate(true)
    }

    @Override
    fun containsKey(key: Object?): Boolean {
        return containsKey(KeyImpl.toKey(key, null))
    }

    @Override
    fun containsKey(key: String?): Boolean {
        return containsKey(KeyImpl.init(key))
    }

    abstract fun containsKey(pc: PageContext?, key: Key?): Boolean // FUTURE add to Struct

    @Override
    operator fun get(key: String?, defaultValue: Object?): Object? {
        return get(KeyImpl.init(key), defaultValue)
    }

    @Override
    @Throws(PageException::class)
    operator fun get(key: String?): Object? {
        return get(KeyImpl.init(key))
    }

    @Override
    @Throws(PageException::class)
    operator fun set(key: String?, value: Object?): Object? {
        return set(KeyImpl.init(key), value)
    }

    @Override
    fun setEL(key: String?, value: Object?): Object? {
        return setEL(KeyImpl.init(key), value)
    }

    @Override
    fun toDumpData(pageContext: PageContext?, maxlevel: Int, properties: DumpProperties?): DumpData? {
        return StructUtil.toDumpTable(this, "Struct", pageContext, maxlevel, properties)
    }

    @Override
    @Throws(PageException::class)
    fun castToBooleanValue(): Boolean {
        throw ExpressionException("can't cast Complex Object Type Struct to a boolean value")
    }

    @Override
    fun castToBoolean(defaultValue: Boolean?): Boolean? {
        return defaultValue
    }

    @Override
    @Throws(PageException::class)
    fun castToDoubleValue(): Double {
        throw ExpressionException("can't cast Complex Object Type Struct to a number value")
    }

    @Override
    fun castToDoubleValue(defaultValue: Double): Double {
        return defaultValue
    }

    @Override
    @Throws(PageException::class)
    fun castToDateTime(): DateTime? {
        throw ExpressionException("can't cast Complex Object Type Struct to a Date")
    }

    @Override
    fun castToDateTime(defaultValue: DateTime?): DateTime? {
        return defaultValue
    }

    @Override
    @Throws(PageException::class)
    fun castToString(): String? {
        throw ExpressionException("Can't cast Complex Object Type Struct to String", "Use Built-In-Function \"serialize(Struct):String\" to create a String from Struct")
    }

    @Override
    fun castToString(defaultValue: String?): String? {
        return defaultValue
    }

    @Override
    @Throws(PageException::class)
    operator fun compareTo(b: Boolean): Int {
        throw ExpressionException("can't compare Complex Object Type Struct with a boolean value")
    }

    @Override
    @Throws(PageException::class)
    operator fun compareTo(dt: DateTime?): Int {
        throw ExpressionException("can't compare Complex Object Type Struct with a DateTime Object")
    }

    @Override
    @Throws(PageException::class)
    operator fun compareTo(d: Double): Int {
        throw ExpressionException("can't compare Complex Object Type Struct with a numeric value")
    }

    @Override
    @Throws(PageException::class)
    operator fun compareTo(str: String?): Int {
        throw ExpressionException("can't compare Complex Object Type Struct with a String")
    }

    @Override
    override fun toString(): String {
        return LazyConverter.serialize(this)
    }

    @Override
    fun values(): Collection<*>? {
        return StructUtil.values(this)
    }

    @Override
    fun containsValue(value: Object?): Boolean {
        return values()!!.contains(value)
    }

    @Override
    fun keysAsStringIterator(): Iterator<String?>? {
        return KeyAsStringIterator(keyIterator())
    }

    /*
	 * @Override public Object get(PageContext pc, Key key, Object defaultValue) { return get(key,
	 * defaultValue); }
	 */
    @Override
    @Throws(PageException::class)
    operator fun get(pc: PageContext?, key: Key?): Object? {
        return get(key)
    }

    @Override
    @Throws(PageException::class)
    operator fun set(pc: PageContext?, propertyName: Key?, value: Object?): Object? {
        return set(propertyName, value)
    }

    @Override
    fun setEL(pc: PageContext?, propertyName: Key?, value: Object?): Object? {
        return setEL(propertyName, value)
    }

    @Override
    @Throws(PageException::class)
    fun call(pc: PageContext?, methodName: Key?, args: Array<Object?>?): Object? {
        val obj: Object = get(methodName, null)
        if (obj is UDF) {
            return (obj as UDF).call(pc, methodName, args, false)
        }
        return if (this is Node) MemberUtil.call(pc, this, methodName, args, shortArrayOf(CFTypes.TYPE_XML, CFTypes.TYPE_STRUCT), arrayOf<String?>("xml", "struct")) else MemberUtil.call(pc, this, methodName, args, shortArrayOf(CFTypes.TYPE_STRUCT), arrayOf<String?>("struct"))
    }

    @Override
    @Throws(PageException::class)
    fun callWithNamedValues(pc: PageContext?, methodName: Key?, args: Struct?): Object? {
        val obj: Object = get(methodName, null)
        return if (obj is UDF) {
            (obj as UDF).callWithNamedValues(pc, methodName, args, false)
        } else MemberUtil.callWithNamedValues(pc, this, methodName, args, CFTypes.TYPE_STRUCT, "struct")
    }

    @get:Override
    val iterator: Iterator<*>?
        get() = keysAsStringIterator()

    @Override
    override fun equals(obj: Object?): Boolean {
        return if (obj !is Collection) false else CollectionUtil.equals(this, obj as Collection?)
    }

    // FUTURE add to loader
    abstract val type: Int

    /*
         * @Override public int hashCode() { return CollectionUtil.hashCode(this); }
         */
    companion object {
        private const val serialVersionUID = 7433668961838400995L

        /**
         * throw exception for invalid key
         *
         * @param key Invalid key
         * @return returns an invalid key Exception
         */
        fun invalidKey(config: Config?, sct: Struct?, key: Key?, `in`: String?): ExpressionException? {
            var config: Config? = config
            val appendix = if (StringUtil.isEmpty(`in`, true)) "" else " in the $`in`"
            val it: Iterator<Key?> = sct.keyIterator()
            var k: Key?
            while (it.hasNext()) {
                k = it.next()
                if (k.equals(key)) return ExpressionException("the value from key [" + key.getString().toString() + "] " + appendix + " is NULL, which is the same as not existing in CFML")
            }
            config = ThreadLocalPageContext.getConfig(config)
            val msg: String = ExceptionUtil.similarKeyMessage(sct, key.getString(), "key", "keys", `in`, true)
            val detail: String = ExceptionUtil.similarKeyMessage(sct, key.getString(), "keys", `in`, true)
            return if (config != null && config.debug()) ExpressionException(msg, detail) else ExpressionException("key [" + key.getString().toString() + "] doesn't exist" + appendix)
        }

        fun invalidKey(map: Map<*, *>?, key: Object?, remove: Boolean): PageException? {
            val sb = StringBuilder()
            val it: Iterator<*> = map.keySet().iterator()
            var k: Object?
            while (it.hasNext()) {
                k = it.next()
                if (sb.length() > 0) sb.append(", ")
                sb.append(k.toString())
            }
            return ExpressionException(
                    (if (remove) "cannot remove key [$key] from struct, key doesn't exist" else "key [$key] doesn't exist") + " (existing keys: [" + sb.toString() + "])")
        }
    }
}