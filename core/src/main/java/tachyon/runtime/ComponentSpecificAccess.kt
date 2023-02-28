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
package tachyon.runtime

import java.util.Iterator

class ComponentSpecificAccess(private val access: Int, component: Component?) : StructSupport(), Component, Objects {
    private val component: Component?
    @Override
    fun getPageSource(): PageSource? {
        return component.getPageSource()
    }

    @Override
    fun keySet(): Set? {
        return component.keySet(access)
    }

    @Override
    fun getDisplayName(): String? {
        return component.getDisplayName()
    }

    @Override
    fun getExtends(): String? {
        return component.getExtends()
    }

    @Override
    fun getHint(): String? {
        return component.getHint()
    }

    @Override
    fun getName(): String? {
        return component.getName()
    }

    @Override
    fun getCallName(): String? {
        return component.getCallName()
    }

    @Override
    fun getAbsName(): String? {
        return component.getAbsName()
    }

    @Override
    fun getBaseAbsName(): String? {
        return component.getBaseAbsName()
    }

    @Override
    fun isBasePeristent(): Boolean {
        return component.isPersistent()
    }

    @Override
    fun getOutput(): Boolean {
        return component.getOutput()
    }

    @Override
    fun instanceOf(type: String?): Boolean {
        return component.instanceOf(type)
    }

    @Override
    fun isValidAccess(access: Int): Boolean {
        return component.isValidAccess(access)
    }

    @Override
    @Throws(PageException::class)
    fun getMetaData(pc: PageContext?): Struct? {
        return component.getMetaData(pc)
    }

    @Override
    @Throws(PageException::class)
    fun call(pc: PageContext?, key: String?, args: Array<Object?>?): Object? {
        return call(pc, KeyImpl.init(key), args)
    }

    @Override
    @Throws(PageException::class)
    fun call(pc: PageContext?, key: Collection.Key?, args: Array<Object?>?): Object? {
        return component.call(pc, access, key, args)
    }

    @Override
    @Throws(PageException::class)
    fun callWithNamedValues(pc: PageContext?, key: String?, args: Struct?): Object? {
        return callWithNamedValues(pc, KeyImpl.init(key), args)
    }

    @Override
    @Throws(PageException::class)
    fun callWithNamedValues(pc: PageContext?, key: Collection.Key?, args: Struct?): Object? {
        return component.callWithNamedValues(pc, access, key, args)
    }

    @Override
    fun size(): Int {
        return component.size(access)
    }

    @Override
    fun keys(): Array<Collection.Key?>? {
        return component.keys(access)
    }

    @Override
    @Throws(PageException::class)
    fun remove(key: Collection.Key?): Object? {
        return component.remove(key)
    }

    @Override
    fun removeEL(key: Collection.Key?): Object? {
        return component.removeEL(key)
    }

    @Override
    fun clear() {
        component.clear()
    }

    @Override
    @Throws(PageException::class)
    operator fun get(key: Collection.Key?): Object? {
        return component.get(access, key)
    }

    @Override
    operator fun get(key: Collection.Key?, defaultValue: Object?): Object? {
        return component.get(access, key, defaultValue)
    }

    @Override
    @Throws(PageException::class)
    operator fun set(key: Collection.Key?, value: Object?): Object? {
        return component.set(key, value)
    }

    @Override
    fun setEL(key: Collection.Key?, value: Object?): Object? {
        return component.setEL(key, value)
    }

    @Override
    fun keyIterator(): Iterator<Collection.Key?>? {
        return component.keyIterator(access)
    }

    @Override
    fun keysAsStringIterator(): Iterator<String?>? {
        return component.keysAsStringIterator(access)
    }

    @Override
    fun entryIterator(): Iterator<Entry<Key?, Object?>?>? {
        return component.entryIterator(access)
    }

    @Override
    fun valueIterator(): Iterator<Object?>? {
        return component.valueIterator(access)
    }

    @Override
    fun containsKey(key: Collection.Key?): Boolean {
        return component.get(access, key, null) != null
    }

    @Override
    fun containsKey(pc: PageContext?, key: Collection.Key?): Boolean {
        return component.get(access, key, null) != null
    }

    @Override
    fun toDumpData(pageContext: PageContext?, maxlevel: Int, dp: DumpProperties?): DumpData? {
        return component.toDumpData(pageContext, maxlevel, dp, access)
    }

    @Override
    @Throws(PageException::class)
    fun castToString(): String? {
        return component.castToString()
    }

    @Override
    fun castToString(defaultValue: String?): String? {
        return component.castToString(defaultValue)
    }

    @Override
    @Throws(PageException::class)
    fun castToBooleanValue(): Boolean {
        return component.castToBooleanValue()
    }

    @Override
    fun castToBoolean(defaultValue: Boolean?): Boolean? {
        return component.castToBoolean(defaultValue)
    }

    @Override
    @Throws(PageException::class)
    fun castToDoubleValue(): Double {
        return component.castToDoubleValue()
    }

    @Override
    fun castToDoubleValue(defaultValue: Double): Double {
        return component.castToDoubleValue(defaultValue)
    }

    @Override
    @Throws(PageException::class)
    fun castToDateTime(): DateTime? {
        return component.castToDateTime()
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
        return component.compareTo(dt)
    }

    @Override
    @Throws(PageException::class)
    operator fun compareTo(str: String?): Int {
        return component.compareTo(str)
    }

    @Override
    @Throws(PageException::class)
    operator fun compareTo(d: Double): Int {
        return component.compareTo(d)
    }

    /*
	 * public Object get(PageContext pc, String key, Object defaultValue) { return
	 * get(pc,KeyImpl.init(key),defaultValue); }
	 */
    @Override
    operator fun get(pc: PageContext?, key: Collection.Key?, defaultValue: Object?): Object? {
        return component.get(access, key, defaultValue)
    }

    /*
	 * public Object get(PageContext pc, String key) throws PageException { return
	 * get(pc,KeyImpl.init(key)); }
	 */
    @Override
    @Throws(PageException::class)
    operator fun get(pc: PageContext?, key: Collection.Key?): Object? {
        return component.get(access, key)
    }

    @Override
    fun duplicate(deepCopy: Boolean): Collection? {
        return ComponentSpecificAccess(access, component.duplicate(deepCopy) as Component)
    }

    /*
	 * public Object set(PageContext pc, String propertyName, Object value) throws PageException {
	 * return component.set(propertyName,value); }
	 */
    @Override
    @Throws(PageException::class)
    operator fun set(pc: PageContext?, propertyName: Collection.Key?, value: Object?): Object? {
        return component.set(propertyName, value)
    }

    /*
	 * public Object setEL(PageContext pc, String propertyName, Object value) { return
	 * component.setEL(propertyName,value); }
	 */
    @Override
    fun setEL(pc: PageContext?, propertyName: Key?, value: Object?): Object? {
        return component.setEL(propertyName, value)
    }

    fun getAccess(): Int {
        return access
    }

    @Override
    @Throws(PageException::class)
    fun getJavaAccessClass(isNew: RefBoolean?): Class? {
        return component.getJavaAccessClass(isNew)
    }

    @Override
    fun getWSDLFile(): String? {
        return component.getWSDLFile()
    }

    @Override
    fun getProperties(onlyPeristent: Boolean): Array<Property?>? {
        return component.getProperties(onlyPeristent)
    }

    @Override
    fun getProperties(onlyPeristent: Boolean, includeBaseProperties: Boolean, overrideProperties: Boolean, inheritedMappedSuperClassOnly: Boolean): Array<Property?>? {
        return component.getProperties(onlyPeristent, includeBaseProperties, overrideProperties, inheritedMappedSuperClassOnly)
    }

    @Override
    fun getComponentScope(): ComponentScope? {
        return component.getComponentScope()
    }

    fun getComponent(): Component? {
        return component
    }

    @Override
    fun contains(pc: PageContext?, key: Key?): Boolean {
        return component.contains(access, key)
    }

    @Override
    fun getMember(access: Int, key: Key?, dataMember: Boolean, superAccess: Boolean): Member? {
        return component.getMember(access, key, dataMember, superAccess)
    }

    @Override
    @Throws(PageException::class)
    fun setProperty(property: Property?) {
        component.setProperty(property)
    }

    @Override
    fun equalTo(type: String?): Boolean {
        return component.equalTo(type)
    }

    @Override
    @Throws(PageException::class)
    fun registerUDF(key: Collection.Key?, udf: UDF?) {
        component.registerUDF(key, udf)
    }

    @Override
    @Throws(PageException::class)
    fun registerUDF(key: Collection.Key?, props: UDFProperties?) {
        component.registerUDF(key, props)
    }

    @Override
    fun isPersistent(): Boolean {
        return component.isPersistent()
    }

    @Override
    fun isAccessors(): Boolean {
        return component.isAccessors()
    }

    @Override
    fun setEntity(entity: Boolean) {
        component.setEntity(entity)
    }

    @Override
    fun isEntity(): Boolean {
        return component.isEntity()
    }

    @Override
    fun getBaseComponent(): Component? {
        return component.getBaseComponent()
    }

    @Override
    fun keySet(access: Int): Set<Key?>? {
        return component.keySet(access)
    }

    @Override
    fun getMetaStructItem(name: Key?): Object? {
        return component.getMetaStructItem(name)
    }

    @Override
    @Throws(PageException::class)
    fun call(pc: PageContext?, access: Int, name: Key?, args: Array<Object?>?): Object? {
        return component.call(pc, access, name, args)
    }

    @Override
    @Throws(PageException::class)
    fun callWithNamedValues(pc: PageContext?, access: Int, name: Key?, args: Struct?): Object? {
        return component.callWithNamedValues(pc, access, name, args)
    }

    @Override
    fun size(access: Int): Int {
        return component.size()
    }

    @Override
    fun keys(access: Int): Array<Key?>? {
        return component.keys(access)
    }

    @Override
    fun keyIterator(access: Int): Iterator<Key?>? {
        return component.keyIterator(access)
    }

    @Override
    fun keysAsStringIterator(access: Int): Iterator<String?>? {
        return component.keysAsStringIterator(access)
    }

    @Override
    fun entryIterator(access: Int): Iterator<Entry<Key?, Object?>?>? {
        return component.entryIterator(access)
    }

    @Override
    fun valueIterator(access: Int): Iterator<Object?>? {
        return component.valueIterator(access)
    }

    @Override
    @Throws(PageException::class)
    operator fun get(access: Int, key: Key?): Object? {
        return component.get(access, key)
    }

    @Override
    operator fun get(access: Int, key: Key?, defaultValue: Object?): Object? {
        return component.get(access, key, defaultValue)
    }

    @Override
    fun toDumpData(pageContext: PageContext?, maxlevel: Int, dp: DumpProperties?, access: Int): DumpData? {
        return toDumpData(pageContext, maxlevel, dp, access)
    }

    @Override
    fun contains(access: Int, name: Key?): Boolean {
        return component.contains(access, name)
    }

    @Override
    @Throws(PageException::class)
    fun getJavaAccessClass(pc: PageContext?, isNew: RefBoolean?, writeLog: Boolean, takeTop: Boolean, create: Boolean, supressWSbeforeArg: Boolean): Class? {
        return component.getJavaAccessClass(pc, isNew, writeLog, takeTop, create, supressWSbeforeArg)
    }

    @Override
    @Throws(PageException::class)
    fun getJavaAccessClass(pc: PageContext?, isNew: RefBoolean?, writeLog: Boolean, takeTop: Boolean, create: Boolean, supressWSbeforeArg: Boolean, output: Boolean,
                           returnValue: Boolean): Class? {
        return component.getJavaAccessClass(pc, isNew, writeLog, takeTop, create, supressWSbeforeArg, output, returnValue)
    }

    @Override
    fun getModifier(): Int {
        return component.getModifier()
    }

    @Override
    fun id(): String? {
        return component.id()
    }

    @Override
    fun staticScope(): Scope? {
        return component.staticScope()
    }

    @Override
    fun beforeStaticConstructor(pc: PageContext?): Variables? {
        return component.beforeStaticConstructor(pc)
    }

    @Override
    fun afterStaticConstructor(pc: PageContext?, `var`: Variables?) {
        component.afterStaticConstructor(pc, `var`)
    }

    @Override
    fun getInterfaces(): Array<Interface?>? {
        return component.getInterfaces()
    }

    @Override
    fun getType(): Int {
        return if (component is ComponentImpl) {
            (component as ComponentImpl?)!!.getType()
        } else Struct.TYPE_REGULAR
    }

    companion object {
        fun toComponentSpecificAccess(access: Int, component: Component?): ComponentSpecificAccess? {
            var component: Component? = component
            if (component is ComponentSpecificAccess) {
                val csa = component as ComponentSpecificAccess?
                if (access == csa!!.getAccess()) return csa
                component = csa.getComponent()
            }
            return ComponentSpecificAccess(access, component)
        }
    }

    /**
     * constructor of the class
     *
     * @param access
     * @param component
     * @throws ExpressionException
     */
    init {
        this.component = component
    }
}