/**
 * Copyright (c) 2023, TachyonCFML.org
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

import java.util.Iterator

/**
 * d
 *
 */
class ClusterNotSupported : StructSupport(), Cluster {
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
        return null
    }

    @Override
    @Throws(PageException::class)
    fun remove(key: Key?): Object? {
        throw ExpressionException(NOT_SUPPORTED)
    }

    @Override
    fun clear() {
    }

    @Override
    @Throws(ExpressionException::class)
    operator fun get(key: Collection.Key?): Object? {
        throw ExpressionException(NOT_SUPPORTED)
    }

    @Override
    @Throws(ExpressionException::class)
    operator fun get(pc: PageContext?, key: Collection.Key?): Object? {
        throw ExpressionException(NOT_SUPPORTED)
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
        throw ExpressionException(NOT_SUPPORTED)
    }

    @Override
    fun setEL(key: Collection.Key?, value: Object?): Object? {
        return null
    }

    @Override
    fun setEntry(entry: ClusterEntry?) {
    }

    @Override
    fun keyIterator(): Iterator<Collection.Key?>? {
        return null
    }

    @Override
    fun keysAsStringIterator(): Iterator<String?>? {
        return null
    }

    @Override
    fun entryIterator(): Iterator<Entry<Key?, Object?>?>? {
        return null
    }

    @Override
    fun valueIterator(): Iterator<Object?>? {
        return null
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
        throw PageRuntimeException(ExpressionException(NOT_SUPPORTED))
        // return new SimpleDumpData(NOT_SUPPORTED);
    }

    @Override
    fun duplicate(deepCopy: Boolean): Collection? {
        return ClusterNotSupported()
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
        throw ExpressionException(NOT_SUPPORTED)
    }

    @Override
    fun castToString(defaultValue: String?): String? {
        return defaultValue
    }

    @Override
    @Throws(ExpressionException::class)
    fun castToBooleanValue(): Boolean {
        throw ExpressionException(NOT_SUPPORTED)
    }

    @Override
    fun castToBoolean(defaultValue: Boolean?): Boolean? {
        return defaultValue
    }

    @Override
    @Throws(ExpressionException::class)
    fun castToDoubleValue(): Double {
        throw ExpressionException(NOT_SUPPORTED)
    }

    @Override
    fun castToDoubleValue(defaultValue: Double): Double {
        return defaultValue
    }

    @Override
    @Throws(ExpressionException::class)
    fun castToDateTime(): DateTime? {
        throw ExpressionException(NOT_SUPPORTED)
    }

    @Override
    fun castToDateTime(defaultValue: DateTime?): DateTime? {
        return defaultValue
    }

    @Override
    fun getType(): Int {
        return SCOPE_CLUSTER
    }

    @Override
    fun getTypeAsString(): String? {
        return "Cluster"
    }

    @Override
    @Throws(PageException::class)
    operator fun compareTo(str: String?): Int {
        throw ExpressionException(NOT_SUPPORTED)
    }

    @Override
    @Throws(PageException::class)
    operator fun compareTo(b: Boolean): Int {
        throw ExpressionException(NOT_SUPPORTED)
    }

    @Override
    @Throws(PageException::class)
    operator fun compareTo(d: Double): Int {
        throw ExpressionException(NOT_SUPPORTED)
    }

    @Override
    @Throws(PageException::class)
    operator fun compareTo(dt: DateTime?): Int {
        throw ExpressionException(NOT_SUPPORTED)
    }

    @Override
    fun broadcast() {
        // print.out("Cluster#broadcast()");
    }

    @Override
    fun init(configServer: ConfigServer?) {
    }

    companion object {
        private val NOT_SUPPORTED: String? = "to enable the cluster scope, please install a cluster scope implementation with the help of the extension manager"
    }
}