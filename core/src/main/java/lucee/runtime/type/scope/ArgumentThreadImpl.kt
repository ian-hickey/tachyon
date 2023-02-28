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

import java.util.Comparator

class ArgumentThreadImpl(sct: Struct?) : Argument, Cloneable {
    private val sct: Struct?
    @Override
    fun getFunctionArgument(key: String?, defaultValue: Object?): Object? {
        return sct.get(key, defaultValue)
    }

    @Override
    fun getFunctionArgument(key: Key?, defaultValue: Object?): Object? {
        return sct.get(key, defaultValue)
    }

    @Override
    fun containsFunctionArgumentKey(key: Key?): Boolean {
        return sct.containsKey(key)
    }

    @Override
    @Throws(PageException::class)
    fun setArgument(obj: Object?): Object? {
        // TODO Auto-generated method stub
        return null
    }

    @Override
    fun setFunctionArgumentNames(functionArgumentNames: Set?) {
    }

    @Override
    @Throws(PageException::class)
    fun insert(index: Int, key: String?, value: Object?): Boolean {
        // TODO Auto-generated method stub
        return false
    }

    @Override
    fun isBind(): Boolean {
        return true
    }

    @Override
    fun setBind(bind: Boolean) {
    }

    @Override
    fun getType(): Int {
        return SCOPE_ARGUMENTS
    }

    @Override
    fun getTypeAsString(): String? {
        return "arguments"
    }

    @Override
    fun initialize(pc: PageContext?) {
    }

    @Override
    fun isInitalized(): Boolean {
        return true
    }

    @Override
    fun release(pc: PageContext?) {
    }

    @Override
    fun clear() {
        sct.clear()
    }

    @Override
    fun containsKey(key: String?): Boolean {
        return sct.containsKey(key)
    }

    @Override
    fun containsKey(key: Key?): Boolean {
        return sct.containsKey(key)
    }

    @Override
    fun duplicate(deepCopy: Boolean): Collection? {
        return ArgumentThreadImpl(Duplicator.duplicate(sct, deepCopy) as Struct)
    }

    @Override
    @Throws(PageException::class)
    operator fun get(key: String?): Object? {
        return get(KeyImpl.init(key))
    }

    @Override
    @Throws(PageException::class)
    operator fun get(key: Key?): Object? {
        return sct.get(key)
    }

    @Override
    operator fun get(key: String?, defaultValue: Object?): Object? {
        return sct.get(key, defaultValue)
    }

    @Override
    operator fun get(key: Key?, defaultValue: Object?): Object? {
        return sct.get(key, defaultValue)
    }

    @Override
    fun keys(): Array<Key?>? {
        return sct.keys()
    }

    @Override
    @Throws(PageException::class)
    fun remove(key: Key?): Object? {
        return sct.remove(key)
    }

    @Override
    fun removeEL(key: Key?): Object? {
        return sct.removeEL(key)
    }

    @Override
    fun remove(key: Key?, defaultValue: Object?): Object? {
        return sct.remove(key, defaultValue)
    }

    @Override
    @Throws(PageException::class)
    operator fun set(key: String?, value: Object?): Object? {
        return sct.set(key, value)
    }

    @Override
    @Throws(PageException::class)
    operator fun set(key: Key?, value: Object?): Object? {
        return sct.set(key, value)
    }

    @Override
    fun setEL(key: String?, value: Object?): Object? {
        return sct.setEL(key, value)
    }

    @Override
    fun setEL(key: Key?, value: Object?): Object? {
        return sct.setEL(key, value)
    }

    @Override
    fun size(): Int {
        return sct.size()
    }

    @Override
    fun toDumpData(pageContext: PageContext?, maxlevel: Int, properties: DumpProperties?): DumpData? {
        return sct.toDumpData(pageContext, maxlevel, properties)
    }

    @Override
    fun keyIterator(): Iterator<Collection.Key?>? {
        return sct.keyIterator()
    }

    @Override
    fun keysAsStringIterator(): Iterator<String?>? {
        return sct.keysAsStringIterator()
    }

    @Override
    fun entryIterator(): Iterator<Entry<Key?, Object?>?>? {
        return sct.entryIterator()
    }

    @Override
    fun valueIterator(): Iterator<Object?>? {
        return sct.valueIterator()
    }

    @Override
    fun castToBoolean(defaultValue: Boolean?): Boolean? {
        return sct.castToBoolean(defaultValue)
    }

    @Override
    @Throws(PageException::class)
    fun castToBooleanValue(): Boolean {
        return sct.castToBooleanValue()
    }

    @Override
    @Throws(PageException::class)
    fun castToDateTime(): DateTime? {
        return sct.castToDateTime()
    }

    @Override
    fun castToDateTime(defaultValue: DateTime?): DateTime? {
        return sct.castToDateTime(defaultValue)
    }

    @Override
    @Throws(PageException::class)
    fun castToDoubleValue(): Double {
        return sct.castToDoubleValue()
    }

    @Override
    fun castToDoubleValue(defaultValue: Double): Double {
        return sct.castToDoubleValue(defaultValue)
    }

    @Override
    @Throws(PageException::class)
    fun castToString(): String? {
        return sct.castToString()
    }

    @Override
    fun castToString(defaultValue: String?): String? {
        return sct.castToString(defaultValue)
    }

    @Override
    @Throws(PageException::class)
    operator fun compareTo(str: String?): Int {
        return sct.compareTo(str)
    }

    @Override
    @Throws(PageException::class)
    operator fun compareTo(b: Boolean): Int {
        return sct.compareTo(b)
    }

    @Override
    @Throws(PageException::class)
    operator fun compareTo(d: Double): Int {
        return sct.compareTo(d)
    }

    @Override
    @Throws(PageException::class)
    operator fun compareTo(dt: DateTime?): Int {
        return sct.compareTo(dt)
    }

    @Override
    fun containsKey(key: Object?): Boolean {
        return sct.containsKey(key)
    }

    @Override
    fun containsValue(value: Object?): Boolean {
        return sct.containsValue(value)
    }

    @Override
    fun entrySet(): Set? {
        return sct.entrySet()
    }

    @Override
    operator fun get(key: Object?): Object? {
        return sct.get(key)
    }

    @Override
    fun isEmpty(): Boolean {
        return sct.isEmpty()
    }

    @Override
    fun keySet(): Set? {
        return sct.keySet()
    }

    @Override
    fun put(key: Object?, value: Object?): Object? {
        return sct.put(key, value)
    }

    @Override
    fun putAll(m: Map?) {
        sct.putAll(m)
    }

    @Override
    fun remove(key: Object?): Object? {
        return sct.remove(key)
    }

    @Override
    fun values(): Collection<*>? {
        return sct.values()
    }

    @Override
    @Throws(PageException::class)
    fun append(o: Object?): Object? {
        throw CasterException(sct, "Array")
    }

    @Override
    fun appendEL(o: Object?): Object? {
        throw PageRuntimeException(CasterException(sct, "Array"))
    }

    @Override
    fun containsKey(key: Int): Boolean {
        return sct.containsKey(ArgumentIntKey.init(key))
    }

    @Override
    operator fun get(key: Int, defaultValue: Object?): Object? {
        return sct.get(ArgumentIntKey.init(key), defaultValue)
    }

    @Override
    fun getDimension(): Int {
        throw PageRuntimeException(CasterException(sct, "Array"))
    }

    @Override
    @Throws(PageException::class)
    fun getE(key: Int): Object? {
        return sct.get(KeyImpl.init(Caster.toString(key)))
    }

    @Override
    @Throws(PageException::class)
    fun insert(key: Int, value: Object?): Boolean {
        throw CasterException(sct, "Array")
    }

    @Override
    fun intKeys(): IntArray? {
        throw PageRuntimeException(CasterException(sct, "Array"))
    }

    @Override
    @Throws(PageException::class)
    fun prepend(o: Object?): Object? {
        throw CasterException(sct, "Array")
    }

    @Override
    @Throws(PageException::class)
    fun removeE(key: Int): Object? {
        return sct.remove(KeyImpl.init(Caster.toString(key)))
    }

    @Override
    fun removeEL(key: Int): Object? {
        return sct.removeEL(KeyImpl.init(Caster.toString(key)))
    }

    @Override
    @Throws(PageException::class)
    fun resize(to: Int) {
        throw CasterException(sct, "Array")
    }

    /**
     * @param key
     * @param value
     * @return
     * @throws PageException
     */
    @Override
    @Throws(PageException::class)
    fun setE(key: Int, value: Object?): Object? {
        return sct.set(Caster.toString(key), value)
    }

    @Override
    fun setEL(key: Int, value: Object?): Object? {
        return sct.setEL(Caster.toString(key), value)
    }

    @Override
    @Throws(PageException::class)
    fun sort(sortType: String?, sortOrder: String?) {
        throw CasterException(sct, "Array")
    }

    @Override
    fun sortIt(com: Comparator?) {
        throw PageRuntimeException(CasterException(sct, "Array"))
    }

    @Override
    fun toArray(): Array<Object?>? {
        return try {
            Caster.toArray(sct).toArray()
        } catch (pe: PageException) {
            throw PageRuntimeException(pe)
        }
    }

    @Override
    fun toList(): List? {
        return try {
            Caster.toArray(sct).toList()
        } catch (pe: PageException) {
            throw PageRuntimeException(pe)
        }
    }

    @Override
    fun clone(): Object {
        return duplicate(true)
    }

    @Override
    operator fun get(pc: PageContext?, key: Key?, defaultValue: Object?): Object? {
        return get(key, defaultValue)
    }

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
        return MemberUtil.call(pc, this, methodName, args, shortArrayOf(CFTypes.TYPE_ARRAY), arrayOf<String?>("array"))
    }

    @Override
    @Throws(PageException::class)
    fun callWithNamedValues(pc: PageContext?, methodName: Key?, args: Struct?): Object? {
        return MemberUtil.callWithNamedValues(pc, this, methodName, args, CFTypes.TYPE_ARRAY, "array")
    }

    @Override
    fun getIterator(): Iterator<String?>? {
        return keysAsStringIterator()
    }

    init {
        this.sct = sct
    }
}