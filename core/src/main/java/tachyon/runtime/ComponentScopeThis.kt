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
package tachyon.runtime

import java.util.Iterator

/**
 *
 */
class ComponentScopeThis(component: ComponentImpl?) : StructSupport(), ComponentScope {
    private val component: ComponentImpl?
    @Override
    fun initialize(pc: PageContext?) {
    }

    @Override
    fun release(pc: PageContext?) {
    }

    @Override
    fun getType(): Int {
        return SCOPE_VARIABLES
    }

    @Override
    fun getTypeAsString(): String? {
        return "variables"
    }

    @Override
    fun size(): Int {
        return component!!.size(access) + 1
    }

    @Override
    fun keys(): Array<Collection.Key?>? {
        val keySet: Set<Key?> = component!!.keySet(access)
        keySet.add(KeyConstants._this)
        val arr: Array<Collection.Key?> = arrayOfNulls<Collection.Key?>(keySet.size())
        val it: Iterator<Key?> = keySet.iterator()
        var index = 0
        while (it.hasNext()) {
            arr[index++] = it.next()
        }
        return arr
    }

    @Override
    @Throws(PageException::class)
    fun remove(key: Collection.Key?): Object? {
        return component!!.remove(key)
    }

    @Override
    fun removeEL(key: Collection.Key?): Object? {
        return component!!.removeEL(key)
    }

    @Override
    fun clear() {
        component!!.clear()
    }

    @Override
    @Throws(PageException::class)
    operator fun get(key: Key?): Object? {
        if (key.equalsIgnoreCase(KeyConstants._THIS)) {
            return component!!.top
        }
        return if (key.equalsIgnoreCase(KeyConstants._STATIC)) {
            component!!.staticScope()
        } else component.get(access, key)
    }

    @Override
    @Throws(PageException::class)
    operator fun get(pc: PageContext?, key: Collection.Key?): Object? {
        return get(key)
    }

    @Override
    operator fun get(key: Collection.Key?, defaultValue: Object?): Object? {
        if (key.equalsIgnoreCase(KeyConstants._THIS)) {
            return component!!.top
        }
        return if (key.equalsIgnoreCase(KeyConstants._STATIC)) {
            component!!.staticScope()
        } else component.get(access, key, defaultValue)
    }

    @Override
    operator fun get(pc: PageContext?, key: Collection.Key?, defaultValue: Object?): Object? {
        return get(key, defaultValue)
    }

    @Override
    @Throws(PageException::class)
    operator fun set(key: Collection.Key?, value: Object?): Object? {
        return component!!.set(key, value)
    }

    @Override
    fun setEL(key: Collection.Key?, value: Object?): Object? {
        return component!!.setEL(key, value)
    }

    @Override
    fun keyIterator(): Iterator<Collection.Key?>? {
        return component!!.keyIterator(access)
    }

    @Override
    fun keysAsStringIterator(): Iterator<String?>? {
        return component!!.keysAsStringIterator(access)
    }

    @Override
    fun entryIterator(): Iterator<Entry<Key?, Object?>?>? {
        return component!!.entryIterator(access)
    }

    @Override
    fun valueIterator(): Iterator<Object?>? {
        return component!!.valueIterator(access)
    }

    @Override
    fun containsKey(key: Key?): Boolean {
        return get(key, null) != null
    }

    @Override
    fun containsKey(pc: PageContext?, key: Key?): Boolean {
        return get(pc, key, null) != null
    }

    @Override
    fun toDumpData(pageContext: PageContext?, maxlevel: Int, dp: DumpProperties?): DumpData? {
        val cp: DumpTable = StructUtil.toDumpTable(this, "This Scope", pageContext, maxlevel, dp)
        cp.setComment("Component: " + component!!.getPageSource().getComponentName())
        return cp
    }

    @Override
    @Throws(PageException::class)
    fun castToString(): String? {
        return component!!.castToString()
    }

    @Override
    fun castToString(defaultValue: String?): String? {
        return component!!.castToString(defaultValue)
    }

    @Override
    @Throws(PageException::class)
    fun castToBooleanValue(): Boolean {
        return component!!.castToBooleanValue()
    }

    @Override
    fun castToBoolean(defaultValue: Boolean?): Boolean? {
        return component!!.castToBoolean(defaultValue)
    }

    @Override
    @Throws(PageException::class)
    fun castToDoubleValue(): Double {
        return component!!.castToDoubleValue()
    }

    @Override
    fun castToDoubleValue(defaultValue: Double): Double {
        return component!!.castToDoubleValue(defaultValue)
    }

    @Override
    @Throws(PageException::class)
    fun castToDateTime(): DateTime? {
        return component!!.castToDateTime()
    }

    @Override
    fun castToDateTime(defaultValue: DateTime?): DateTime? {
        return component.castToDateTime(defaultValue)
    }

    @Override
    @Throws(PageException::class)
    operator fun compareTo(b: Boolean): Int {
        return component.compareTo(b)
    }

    @Override
    @Throws(PageException::class)
    operator fun compareTo(dt: DateTime?): Int {
        return component!!.compareTo(dt)
    }

    @Override
    @Throws(PageException::class)
    operator fun compareTo(d: Double): Int {
        return component.compareTo(d)
    }

    @Override
    @Throws(PageException::class)
    operator fun compareTo(str: String?): Int {
        return component.compareTo(str)
    }

    @Override
    fun duplicate(deepCopy: Boolean): Collection? {
        val sct = StructImpl()
        StructImpl.copy(this, sct, deepCopy)
        return sct
    }

    /**
     * Returns the value of component.
     *
     * @return value component
     */
    @Override
    fun getComponent(): Component? {
        return component!!.top
    }

    @Override
    @Throws(PageException::class)
    operator fun set(pc: PageContext?, propertyName: Collection.Key?, value: Object?): Object? {
        return component!!.set(propertyName, value)
    }

    @Override
    fun setEL(pc: PageContext?, propertyName: Collection.Key?, value: Object?): Object? {
        return component!!.setEL(propertyName, value)
    }

    @Override
    @Throws(PageException::class)
    fun call(pc: PageContext?, key: Collection.Key?, arguments: Array<Object?>?): Object? {
        val m: Member = component!!.getMember(access, key, false, false)
        return if (m != null) {
            if (m is UDF) (m as UDF).call(pc, key, arguments, false) else MemberUtil.call(pc, this, key, arguments, shortArrayOf(CFTypes.TYPE_STRUCT), arrayOf<String?>("struct"))
            // throw ComponentUtil.notFunction(component, key, m.getValue(),access);
        } else MemberUtil.call(pc, this, key, arguments, shortArrayOf(CFTypes.TYPE_STRUCT), arrayOf<String?>("struct"))
        // throw ComponentUtil.notFunction(component, key, null,access);
    }

    /*
	 * public Object callWithNamedValues(PageContext pc, String key, Struct args) throws PageException {
	 * return callWithNamedValues(pc, KeyImpl.init(key), args); }
	 */
    @Override
    @Throws(PageException::class)
    fun callWithNamedValues(pc: PageContext?, key: Collection.Key?, args: Struct?): Object? {
        val m: Member = component!!.getMember(access, key, false, false)
        return if (m != null) {
            if (m is UDF) (m as UDF).callWithNamedValues(pc, key, args, false) else MemberUtil.callWithNamedValues(pc, this, key, args, CFTypes.TYPE_STRUCT, "struct")
            // throw ComponentUtil.notFunction(component, key, m.getValue(),access);
        } else MemberUtil.callWithNamedValues(pc, this, key, args, CFTypes.TYPE_STRUCT, "struct")
        // throw ComponentUtil.notFunction(component, key, null,access);
    }

    @Override
    fun isInitalized(): Boolean {
        return component!!.isInitalized()
    }

    @Override
    fun setBind(bind: Boolean) {
    }

    @Override
    fun isBind(): Boolean {
        return true
    }

    companion object {
        private val access: Int = Component.ACCESS_PRIVATE
    }

    /**
     * constructor of the class
     *
     * @param component
     */
    init {
        this.component = component
    }
}