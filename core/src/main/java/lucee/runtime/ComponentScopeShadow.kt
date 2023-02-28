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
package lucee.runtime

import java.util.Iterator

class ComponentScopeShadow : StructSupport, ComponentScope {
    private val component: ComponentImpl?
    private val shadow: Map<Key?, Object?>?

    /**
     * Constructor of the class
     *
     * @param component
     * @param shadow
     */
    constructor(component: ComponentImpl?, shadow: Map<Key?, Object?>?) {
        this.component = component
        this.shadow = shadow
    }

    /**
     * Constructor of the class
     *
     * @param component
     * @param shadow
     */
    constructor(component: ComponentImpl?, scope: ComponentScopeShadow?, cloneShadow: Boolean) {
        this.component = component
        shadow = if (cloneShadow) Duplicator.duplicateMap(scope!!.shadow, MapFactory.getConcurrentMap(), false) else scope!!.shadow
    }

    @Override
    fun getComponent(): Component? {
        return component!!.top
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
    fun initialize(pc: PageContext?) {
    }

    @Override
    fun isInitalized(): Boolean {
        return component!!.isInitalized()
    }

    @Override
    fun release(pc: PageContext?) {
    }

    @Override
    fun clear() {
        shadow.clear()
    }

    @Override
    fun containsKey(key: Collection.Key?): Boolean {
        return get(key, null) != null
    }

    @Override
    fun containsKey(pc: PageContext?, key: Collection.Key?): Boolean {
        return get(pc, key, null) != null
    }

    @Override
    @Throws(PageException::class)
    operator fun get(key: Key?): Object? {
        val o: Object = get(key, CollectionUtil.NULL)
        if (o !== CollectionUtil.NULL) return o
        throw ExpressionException("Component [" + component!!.getCallName().toString() + "] has no accessible Member with name [" + key.toString() + "]")
    }

    @Override
    operator fun get(key: Key?, defaultValue: Object?): Object? {
        return get(null, key, defaultValue)
    }

    @Override
    operator fun get(pc: PageContext?, key: Key?, defaultValue: Object?): Object? {
        if (key.equalsIgnoreCase(KeyConstants._SUPER)) {
            val ac: Component = ComponentUtil.getActiveComponent(ThreadLocalPageContext.get(pc), component)
            return SuperComponent.superInstance(ac.getBaseComponent() as ComponentImpl)
        }
        if (key.equalsIgnoreCase(KeyConstants._THIS)) return component!!.top
        if (key.equalsIgnoreCase(KeyConstants._STATIC)) return component!!.staticScope()
        var `val`: Object = shadow.getOrDefault(key, CollectionUtil.NULL)
        if (`val` !== CollectionUtil.NULL && (NullSupportHelper.full(pc) || `val` != null)) return `val`
        `val` = component!!.staticScope().getOrDefault(key, CollectionUtil.NULL)
        return if (`val` !== CollectionUtil.NULL && (NullSupportHelper.full(pc) || `val` != null)) `val` else defaultValue
    }

    @Override
    fun keyIterator(): Iterator<Collection.Key?>? {
        return KeyIterator(keys())
    }

    @Override
    fun keysAsStringIterator(): Iterator<String?>? {
        return StringIterator(keys())
    }

    @Override
    fun entryIterator(): Iterator<Entry<Key?, Object?>?>? {
        return EntryIterator(this, keys())
    }

    @Override
    fun valueIterator(): Iterator<Object?>? {
        return ValueIterator(this, keys())
    }

    @Override
    fun keys(): Array<Collection.Key?>? {
        val keys: Array<Collection.Key?> = arrayOfNulls<Collection.Key?>(shadow!!.size() + 1)
        val it: Iterator<Key?> = shadow.keySet().iterator()
        var index = 0
        while (it.hasNext()) {
            keys[index++] = it.next()
        }
        keys[index] = KeyConstants._THIS
        return keys
    }

    @Override
    @Throws(PageException::class)
    fun remove(key: Collection.Key?): Object? {
        if (key.equalsIgnoreCase(KeyConstants._this) || key.equalsIgnoreCase(KeyConstants._super) || key.equalsIgnoreCase(KeyConstants._static)) throw ExpressionException("key [" + key.getString().toString() + "] is part of the component and can't be removed")
        if (NullSupportHelper.full()) {
            if (!shadow!!.containsKey(key)) throw ExpressionException("can't remove key [" + key.getString().toString() + "] from struct, key doesn't exist")
            return shadow.remove(key)
        }
        val o: Object = shadow.remove(key)
        if (o != null) return o
        throw ExpressionException("can't remove key [" + key.getString().toString() + "] from struct, key doesn't exist")
    }

    @Override
    fun removeEL(key: Key?): Object? {
        return if (key.equalsIgnoreCase(KeyConstants._this) || key.equalsIgnoreCase(KeyConstants._super) || key.equalsIgnoreCase(KeyConstants._static)) null else shadow.remove(key)
    }

    @Override
    @Throws(ApplicationException::class)
    operator fun set(key: Collection.Key?, value: Object?): Object? {
        if (key.equalsIgnoreCase(KeyConstants._this) || key.equalsIgnoreCase(KeyConstants._super) || key.equalsIgnoreCase(KeyConstants._static)) return value
        if (!component!!.afterConstructor && value is UDF) {
            component!!.addConstructorUDF(key, value as UDF?)
        }
        shadow.put(key, value)
        return value
    }

    @Override
    fun setEL(key: Collection.Key?, value: Object?): Object? {
        return try {
            set(key, value)
        } catch (e: ApplicationException) {
            value
        }
    }

    @Override
    fun size(): Int {
        return keys()!!.size
    }

    @Override
    fun toDumpData(pageContext: PageContext?, maxlevel: Int, dp: DumpProperties?): DumpData? {
        val cp: DumpTable = StructUtil.toDumpTable(this, "Component Variable Scope", pageContext, maxlevel, dp)
        cp.setComment("Component: " + component!!.getPageSource().getComponentName())
        return cp
    }

    @Override
    @Throws(PageException::class)
    fun castToBooleanValue(): Boolean {
        throw ExpressionException("Can't cast Complex Object Type to a boolean value")
    }

    @Override
    fun castToBoolean(defaultValue: Boolean?): Boolean? {
        return defaultValue
    }

    @Override
    @Throws(PageException::class)
    fun castToDateTime(): DateTime? {
        throw ExpressionException("Can't cast Complex Object Type to a Date Object")
    }

    @Override
    fun castToDateTime(defaultValue: DateTime?): DateTime? {
        return defaultValue
    }

    @Override
    @Throws(PageException::class)
    fun castToDoubleValue(): Double {
        throw ExpressionException("Can't cast Complex Object Type to a numeric value")
    }

    @Override
    fun castToDoubleValue(defaultValue: Double): Double {
        return defaultValue
    }

    @Override
    @Throws(PageException::class)
    fun castToString(): String? {
        throw ExpressionException("Can't cast Complex Object Type to a String")
    }

    @Override
    fun castToString(defaultValue: String?): String? {
        return defaultValue
    }

    @Override
    @Throws(PageException::class)
    operator fun compareTo(b: Boolean): Int {
        throw ExpressionException("can't compare Complex Object with a boolean value")
    }

    @Override
    @Throws(PageException::class)
    operator fun compareTo(dt: DateTime?): Int {
        throw ExpressionException("can't compare Complex Object with a DateTime Object")
    }

    @Override
    @Throws(PageException::class)
    operator fun compareTo(d: Double): Int {
        throw ExpressionException("can't compare Complex Object with a numeric value")
    }

    @Override
    @Throws(PageException::class)
    operator fun compareTo(str: String?): Int {
        throw ExpressionException("can't compare Complex Object with a String")
    }

    /*
	 * public Object call(PageContext pc, String key, Object[] arguments) throws PageException { return
	 * call(pc, KeyImpl.init(key), arguments); }
	 */
    @Override
    @Throws(PageException::class)
    fun call(pc: PageContext?, key: Collection.Key?, arguments: Array<Object?>?): Object? {
        // first check variables
        val o: Object? = shadow!![key]
        if (o is UDF) {
            return (o as UDF?).call(pc, key, arguments, false)
        }

        // then check in component
        val m: Member = component!!.getMember(access, key, false, false)
        if (m != null) {
            if (m is UDF) return (m as UDF).call(pc, key, arguments, false)
        }
        return MemberUtil.call(pc, this, key, arguments, shortArrayOf(CFTypes.TYPE_STRUCT), arrayOf<String?>("struct"))
        // throw ComponentUtil.notFunction(component, key, m!=null?m.getValue():null,access);
    }

    /*
	 * public Object callWithNamedValues(PageContext pc, String key,Struct args) throws PageException {
	 * return callWithNamedValues(pc, KeyImpl.init(key), args); }
	 */
    @Override
    @Throws(PageException::class)
    fun callWithNamedValues(pc: PageContext?, key: Key?, args: Struct?): Object? {
        // first check variables
        val o: Object? = shadow!![key]
        if (o is UDF) {
            return (o as UDF?).callWithNamedValues(pc, key, args, false)
        }
        val m: Member = component!!.getMember(access, key, false, false)
        return if (m != null) {
            if (m is UDF) (m as UDF).callWithNamedValues(pc, key, args, false) else MemberUtil.callWithNamedValues(pc, this, key, args, CFTypes.TYPE_STRUCT, "struct")
            // throw ComponentUtil.notFunction(component, key, m.getValue(),access);
        } else MemberUtil.callWithNamedValues(pc, this, key, args, CFTypes.TYPE_STRUCT, "struct")
        // throw ComponentUtil.notFunction(component, key, null,access);
    }

    @Override
    fun duplicate(deepCopy: Boolean): Collection? {
        val sct = StructImpl()
        StructImpl.copy(this, sct, deepCopy)
        return sct
        // MUST muss deepCopy checken
        // return new ComponentScopeShadow(component,shadow);//new
        // ComponentScopeThis(component.cloneComponentImpl());
    }

    /*
	 * public Object get(PageContext pc, String key, Object defaultValue) { return get(key,
	 * defaultValue); }
	 */
    @Override
    @Throws(PageException::class)
    operator fun set(pc: PageContext?, propertyName: Collection.Key?, value: Object?): Object? {
        return set(propertyName, value)
    }

    /*
	 * public Object setEL(PageContext pc, String propertyName, Object value) { return
	 * setEL(propertyName, value); }
	 */
    @Override
    fun setEL(pc: PageContext?, propertyName: Collection.Key?, value: Object?): Object? {
        return setEL(propertyName, value)
    }

    /*
	 * public Object get(PageContext pc, String key) throws PageException { return get(key); }
	 */
    @Override
    @Throws(PageException::class)
    operator fun get(pc: PageContext?, key: Collection.Key?): Object? {
        return get(key)
    }

    fun getShadow(): Map<Key?, Object?>? {
        return shadow
    }

    @Override
    fun setBind(bind: Boolean) {
    }

    @Override
    fun isBind(): Boolean {
        return true
    }

    companion object {
        private const val serialVersionUID = 4930100230796574243L
        private val access: Int = Component.ACCESS_PRIVATE
    }
}