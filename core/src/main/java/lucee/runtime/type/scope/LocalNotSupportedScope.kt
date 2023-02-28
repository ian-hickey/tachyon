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

import java.util.Iterator

/**
 *
 */
class LocalNotSupportedScope private constructor() : StructSupport(), Scope, Local {
    private var bind = false
    @Override
    fun size(): Int {
        return 0
    }

    @Override
    fun keys(): Array<Collection.Key?>? {
        return null
    }

    @Override
    fun removeEL(key: Key?): Object? {
        // TODO Auto-generated method stub
        return null
    }

    @Override
    @Throws(PageException::class)
    fun remove(key: Key?): Object? {
        throw ExpressionException("Unsupported Context for Local Scope", "Can't invoke key $key, Local Scope can only be invoked inside a Function")
    }

    @Override
    fun clear() {
    }

    @Override
    @Throws(ExpressionException::class)
    operator fun get(key: Collection.Key?): Object? {
        throw ExpressionException("Unsupported Context for Local Scope", "Can't invoke key " + key.getString().toString() + ", Local Scope can only be invoked inside a Function")
    }

    @Override
    @Throws(ExpressionException::class)
    operator fun get(pc: PageContext?, key: Collection.Key?): Object? {
        throw ExpressionException("Unsupported Context for Local Scope", "Can't invoke key " + key.getString().toString() + ", Local Scope can only be invoked inside a Function")
    }

    @Override
    operator fun get(key: Collection.Key?, defaultValue: Object?): Object? {
        return defaultValue
    }

    @Override
    operator fun get(pc: PageContext?, key: Collection.Key?, defaultValue: Object?): Object? {
        return defaultValue
    }

    @Override
    @Throws(ExpressionException::class)
    operator fun set(key: Key?, value: Object?): Object? {
        throw ExpressionException("Unsupported Context for Local Scope", "Can't invoke key " + key.getString().toString() + ", Local Scope can only be invoked inside a Function")
    }

    @Override
    fun setEL(key: Collection.Key?, value: Object?): Object? {
        return null
    }

    @Override
    fun keyIterator(): Iterator<Collection.Key?>? {
        throw PageRuntimeException(ExpressionException("Unsupported Context for Local Scope", "Local Scope can only be invoked inside a Function"))
    }

    @Override
    fun entryIterator(): Iterator<Entry<Key?, Object?>?>? {
        throw PageRuntimeException(ExpressionException("Unsupported Context for Local Scope", "Local Scope can only be invoked inside a Function"))
    }

    @Override
    fun valueIterator(): Iterator<Object?>? {
        throw PageRuntimeException(ExpressionException("Unsupported Context for Local Scope", "Local Scope can only be invoked inside a Function"))
    }

    @Override
    fun isInitalized(): Boolean {
        return false
    }

    @Override
    fun initialize(pc: PageContext?) {
    }

    @Override
    fun release(pc: PageContext?) {
    }

    @Override
    fun toDumpData(pageContext: PageContext?, maxlevel: Int, dp: DumpProperties?): DumpData? {
        throw PageRuntimeException(ExpressionException("Unsupported Context for Local Scope"))
    }

    @Override
    fun duplicate(deepCopy: Boolean): Collection? {
        return LocalNotSupportedScope()
    }

    @Override
    fun containsKey(key: Collection.Key?): Boolean {
        return false
    }

    @Override
    fun containsKey(pc: PageContext?, key: Collection.Key?): Boolean {
        return false
    }

    @Override
    fun containsValue(value: Object?): Boolean {
        return false
    }

    @Override
    fun values(): Collection<*>? {
        return null
    }

    @Override
    @Throws(ExpressionException::class)
    fun castToString(): String? {
        throw ExpressionException("Unsupported Context for Local Scope")
    }

    @Override
    fun castToString(defaultValue: String?): String? {
        return defaultValue
    }

    @Override
    @Throws(ExpressionException::class)
    fun castToBooleanValue(): Boolean {
        throw ExpressionException("Unsupported Context for Local Scope")
    }

    @Override
    fun castToBoolean(defaultValue: Boolean?): Boolean? {
        return defaultValue
    }

    @Override
    @Throws(ExpressionException::class)
    fun castToDoubleValue(): Double {
        throw ExpressionException("Unsupported Context for Local Scope")
    }

    @Override
    fun castToDoubleValue(defaultValue: Double): Double {
        return defaultValue
    }

    @Override
    @Throws(ExpressionException::class)
    fun castToDateTime(): DateTime? {
        throw ExpressionException("Unsupported Context for Local Scope")
    }

    @Override
    fun castToDateTime(defaultValue: DateTime?): DateTime? {
        return defaultValue
    }

    @Override
    fun getType(): Int {
        return SCOPE_LOCAL
    }

    @Override
    fun getTypeAsString(): String? {
        return "local"
    }

    @Override
    @Throws(PageException::class)
    operator fun compareTo(str: String?): Int {
        throw ExpressionException("Unsupported Context for Local Scope")
    }

    @Override
    @Throws(PageException::class)
    operator fun compareTo(b: Boolean): Int {
        throw ExpressionException("Unsupported Context for Local Scope")
    }

    @Override
    @Throws(PageException::class)
    operator fun compareTo(d: Double): Int {
        throw ExpressionException("Unsupported Context for Local Scope")
    }

    @Override
    @Throws(PageException::class)
    operator fun compareTo(dt: DateTime?): Int {
        throw ExpressionException("Unsupported Context for Local Scope")
    }

    @Override
    fun isBind(): Boolean {
        return bind
    }

    @Override
    fun setBind(bind: Boolean) {
        this.bind = bind
    }

    companion object {
        private const val serialVersionUID = 6670210379924188569L
        private val instance: LocalNotSupportedScope? = LocalNotSupportedScope()
        fun getInstance(): LocalNotSupportedScope? {
            return instance
        }
    }
}