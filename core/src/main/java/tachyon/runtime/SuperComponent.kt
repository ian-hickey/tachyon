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
/**
 *
 */
package tachyon.runtime

import java.util.Iterator

/**
 *
 */
class SuperComponent private constructor(comp: ComponentImpl?) : MemberSupport(Component.ACCESS_PRIVATE), Component, Member, Cloneable {
    private val comp: ComponentImpl?
    @Override
    fun getValue(): Object? {
        return this
    }

    @Override
    @Throws(PageException::class)
    fun call(pc: PageContext?, name: String?, args: Array<Object?>?): Object? {
        return comp!!._call(pc, getAccess(), KeyImpl.init(name), null, args, true)
    }

    @Override
    @Throws(PageException::class)
    fun call(pc: PageContext?, name: Key?, args: Array<Object?>?): Object? {
        return comp!!._call(pc, getAccess(), name, null, args, true)
    }

    @Override
    @Throws(PageException::class)
    fun callWithNamedValues(pc: PageContext?, name: String?, args: Struct?): Object? {
        return comp!!._call(pc, getAccess(), KeyImpl.init(name), args, null, true)
    }

    @Override
    @Throws(PageException::class)
    fun callWithNamedValues(pc: PageContext?, methodName: Key?, args: Struct?): Object? {
        return comp!!._call(pc, getAccess(), methodName, args, null, true)
    }

    @Override
    @Throws(PageException::class)
    fun castToBooleanValue(): Boolean {
        return comp!!.castToBooleanValue(true)
    }

    @Override
    fun castToBoolean(defaultValue: Boolean?): Boolean? {
        return comp!!.castToBoolean(true, defaultValue)
    }

    @Override
    @Throws(PageException::class)
    fun castToDateTime(): DateTime? {
        return comp.castToDateTime(true)
    }

    @Override
    fun castToDateTime(defaultValue: DateTime?): DateTime? {
        return comp!!.castToDateTime(true, defaultValue)
    }

    @Override
    @Throws(PageException::class)
    fun castToDoubleValue(): Double {
        return comp!!.castToDoubleValue(true)
    }

    @Override
    fun castToDoubleValue(defaultValue: Double): Double {
        return comp!!.castToDoubleValue(true, defaultValue)
    }

    @Override
    @Throws(PageException::class)
    fun castToString(): String? {
        return comp!!.castToString(true)
    }

    @Override
    fun castToString(defaultValue: String?): String? {
        return comp!!.castToString(true, defaultValue)
    }

    @Override
    fun clear() {
        comp!!.clear()
    }

    @Override
    fun clone(): Object {
        return duplicate(true)
    }

    @Override
    @Throws(PageException::class)
    operator fun compareTo(b: Boolean): Int {
        return comp.compareTo(b)
    }

    @Override
    @Throws(PageException::class)
    operator fun compareTo(dt: DateTime?): Int {
        return comp!!.compareTo(dt)
    }

    @Override
    @Throws(PageException::class)
    operator fun compareTo(d: Double): Int {
        return comp.compareTo(d)
    }

    @Override
    @Throws(PageException::class)
    operator fun compareTo(str: String?): Int {
        return comp.compareTo(str)
    }

    @Override
    fun containsKey(name: String?): Boolean {
        return comp.contains(getAccess(), name)
    }

    @Override
    fun containsKey(key: Key?): Boolean {
        return comp.contains(getAccess(), key.getLowerString())
    }

    @Override
    fun duplicate(deepCopy: Boolean): Collection? {
        return SuperComponent(Duplicator.duplicate(comp, deepCopy) as ComponentImpl)
    }

    @Override
    @Throws(PageException::class)
    operator fun get(pc: PageContext?, key: Key?): Object? {
        return get(key)
    }

    @Override
    operator fun get(pc: PageContext?, key: Key?, defaultValue: Object?): Object? {
        return get(key, defaultValue)
    }

    @Override
    @Throws(PageException::class)
    operator fun get(name: String?): Object? {
        return get(KeyImpl.init(name))
    }

    @Override
    operator fun get(name: String?, defaultValue: Object?): Object? {
        return get(KeyImpl.init(name), defaultValue)
    }

    @Override
    @Throws(PageException::class)
    operator fun get(key: Key?): Object? {
        val member: Member = comp!!.getMember(getAccess(), key, true, true)
        return if (member != null) member.getValue() else comp.get(getAccess(), key)
    }

    @Override
    operator fun get(key: Key?, defaultValue: Object?): Object? {
        val member: Member = comp!!.getMember(getAccess(), key, true, true)
        return if (member != null) member.getValue() else comp.get(getAccess(), key, defaultValue)
    }

    @Override
    fun getAbsName(): String? {
        return comp!!.getAbsName()
    }

    @Override
    fun getBaseAbsName(): String? {
        return comp!!.getBaseAbsName()
    }

    @Override
    fun isBasePeristent(): Boolean {
        return comp!!.isPersistent()
    }

    @Override
    fun getModifier(): Int {
        return comp!!.getModifier()
    }

    @Override
    fun getCallName(): String? {
        return comp!!.getCallName()
    }

    @Override
    fun getDisplayName(): String? {
        return comp!!.getDisplayName()
    }

    @Override
    fun getExtends(): String? {
        return comp!!.getExtends()
    }

    @Override
    fun getHint(): String? {
        return comp!!.getHint()
    }

    @Override
    @Throws(PageException::class)
    fun getJavaAccessClass(isNew: RefBoolean?): Class? {
        return comp!!.getJavaAccessClass(isNew)
    }

    @Override
    @Throws(PageException::class)
    fun getMetaData(pc: PageContext?): Struct? {
        return comp!!.getMetaData(pc)
    }

    @Override
    fun getName(): String? {
        return comp!!.getName()
    }

    @Override
    fun getOutput(): Boolean {
        return comp!!.getOutput()
    }

    @Override
    fun instanceOf(type: String?): Boolean {
        return comp!!.top!!.instanceOf(type)
    }

    fun isInitalized(): Boolean {
        return comp!!.top!!.isInitalized()
    }

    @Override
    fun isValidAccess(access: Int): Boolean {
        return comp!!.isValidAccess(access)
    }

    @Override
    fun keyIterator(): Iterator<Collection.Key?>? {
        return comp!!.keyIterator(getAccess())
    }

    @Override
    fun keysAsStringIterator(): Iterator<String?>? {
        return comp!!.keysAsStringIterator(getAccess())
    }

    @Override
    fun entryIterator(): Iterator<Entry<Key?, Object?>?>? {
        return comp!!.entryIterator(getAccess())
    }

    @Override
    fun keys(): Array<Key?>? {
        return comp!!.keys(getAccess())
    }

    @Override
    @Throws(PageException::class)
    fun remove(key: Key?): Object? {
        return comp!!.remove(key)
    }

    @Override
    fun removeEL(key: Key?): Object? {
        return comp!!.removeEL(key)
    }

    @Override
    fun remove(key: Key?, defaultValue: Object?): Object? {
        return comp!!.remove(key, defaultValue)
    }

    @Override
    @Throws(PageException::class)
    operator fun set(pc: PageContext?, key: Key?, value: Object?): Object? {
        return comp!!.set(pc, key, value)
    }

    @Override
    @Throws(PageException::class)
    operator fun set(name: String?, value: Object?): Object? {
        return comp!!.set(name, value)
    }

    @Override
    @Throws(PageException::class)
    operator fun set(key: Key?, value: Object?): Object? {
        return comp!!.set(key, value)
    }

    @Override
    fun setEL(pc: PageContext?, name: Key?, value: Object?): Object? {
        return comp!!.setEL(pc, name, value)
    }

    @Override
    fun setEL(name: String?, value: Object?): Object? {
        return comp!!.setEL(name, value)
    }

    @Override
    fun setEL(key: Key?, value: Object?): Object? {
        return comp!!.setEL(key, value)
    }

    @Override
    fun size(): Int {
        return comp!!.size(getAccess())
    }

    @Override
    fun toDumpData(pageContext: PageContext?, maxlevel: Int, dp: DumpProperties?): DumpData? {
        return comp!!.top!!.toDumpData(pageContext, maxlevel, dp)
    }

    @Override
    fun getPageSource(): PageSource? {
        return comp!!.getPageSource()
    }

    @Override
    fun containsKey(key: Object?): Boolean {
        return containsKey(KeyImpl.toKey(key, null))
    }

    @Override
    fun entrySet(): Set? {
        return StructUtil.entrySet(this)
    }

    @Override
    operator fun get(key: Object?): Object? {
        return get(KeyImpl.toKey(key, null), null)
    }

    @Override
    fun isEmpty(): Boolean {
        return size() == 0
    }

    @Override
    fun keySet(): Set? {
        return StructUtil.keySet(this)
    }

    @Override
    fun put(key: Object?, value: Object?): Object? {
        return setEL(KeyImpl.toKey(key, null), value)
    }

    @Override
    fun putAll(map: Map?) {
        StructUtil.putAll(this, map)
    }

    @Override
    fun remove(key: Object?): Object? {
        return removeEL(KeyImpl.toKey(key, null))
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
    fun valueIterator(): Iterator<Object?>? {
        return comp!!.valueIterator()
    }

    @Override
    fun getProperties(onlyPeristent: Boolean): Array<Property?>? {
        return comp!!.getProperties(onlyPeristent)
    }

    @Override
    fun getProperties(onlyPeristent: Boolean, includeBaseProperties: Boolean, overrideProperties: Boolean, inheritedMappedSuperClassOnly: Boolean): Array<Property?>? {
        return comp!!.getProperties(onlyPeristent, includeBaseProperties, overrideProperties, inheritedMappedSuperClassOnly)
    }

    @Override
    fun getComponentScope(): ComponentScope? {
        return comp!!.getComponentScope()
    }

    @Override
    fun contains(pc: PageContext?, key: Key?): Boolean {
        return comp.contains(getAccess(), key)
    }

    /*
	 * private Member getMember(int access, Key key, boolean dataMember,boolean superAccess) { return
	 * comp.getMember(access, key, dataMember, superAccess); }
	 */
    @Override
    @Throws(PageException::class)
    fun setProperty(property: Property?) {
        comp!!.setProperty(property)
    }

    @Override
    fun equalTo(type: String?): Boolean {
        return comp!!.top!!.equalTo(type)
    }

    @Override
    fun getWSDLFile(): String? {
        return comp!!.getWSDLFile()
    }

    @Override
    @Throws(ApplicationException::class)
    fun registerUDF(key: Collection.Key?, udf: UDF?) {
        comp.registerUDF(key, udf)
    }

    @Override
    @Throws(ApplicationException::class)
    fun registerUDF(key: Collection.Key?, props: UDFProperties?) {
        comp.registerUDF(key, props)
    }

    @Override
    fun getIterator(): Iterator<String?>? {
        return keysAsStringIterator()
    }

    @Override
    @Throws(PageException::class)
    fun getJavaAccessClass(pc: PageContext?, isNew: RefBoolean?, writeLog: Boolean, takeTop: Boolean, create: Boolean, supressWSbeforeArg: Boolean): Class? {
        return comp!!.getJavaAccessClass(pc, isNew, writeLog, takeTop, create, supressWSbeforeArg)
    }

    @Override
    @Throws(PageException::class)
    fun getJavaAccessClass(pc: PageContext?, isNew: RefBoolean?, writeLog: Boolean, takeTop: Boolean, create: Boolean, supressWSbeforeArg: Boolean, output: Boolean,
                           returnValue: Boolean): Class? {
        return comp!!.getJavaAccessClass(pc, isNew, writeLog, takeTop, create, supressWSbeforeArg, output, returnValue)
    }

    @Override
    fun isPersistent(): Boolean {
        return comp!!.isPersistent()
    }

    @Override
    fun isAccessors(): Boolean {
        return comp!!.isAccessors()
    }

    @Override
    fun setEntity(entity: Boolean) {
        comp!!.setEntity(entity)
    }

    @Override
    fun isEntity(): Boolean {
        return comp!!.isEntity()
    }

    @Override
    fun getBaseComponent(): Component? {
        return comp!!.getBaseComponent()
    }

    @Override
    fun keySet(access: Int): Set<Key?>? {
        return comp!!.keySet(access)
    }

    @Override
    fun getMetaStructItem(name: Key?): Object? {
        return comp!!.getMetaStructItem(name)
    }

    @Override
    @Throws(PageException::class)
    fun call(pc: PageContext?, access: Int, name: Key?, args: Array<Object?>?): Object? {
        return comp!!.call(pc, access, name, args)
    }

    @Override
    @Throws(PageException::class)
    fun callWithNamedValues(pc: PageContext?, access: Int, name: Key?, args: Struct?): Object? {
        return comp!!.callWithNamedValues(pc, access, name, args)
    }

    @Override
    fun size(access: Int): Int {
        return comp!!.size()
    }

    @Override
    fun keys(access: Int): Array<Key?>? {
        return comp!!.keys(access)
    }

    @Override
    fun keyIterator(access: Int): Iterator<Key?>? {
        return comp!!.keyIterator(access)
    }

    @Override
    fun keysAsStringIterator(access: Int): Iterator<String?>? {
        return comp!!.keysAsStringIterator(access)
    }

    @Override
    fun entryIterator(access: Int): Iterator<Entry<Key?, Object?>?>? {
        return comp!!.entryIterator(access)
    }

    @Override
    fun valueIterator(access: Int): Iterator<Object?>? {
        return comp!!.valueIterator(access)
    }

    @Override
    @Throws(PageException::class)
    operator fun get(access: Int, key: Key?): Object? {
        return comp.get(access, key)
    }

    @Override
    operator fun get(access: Int, key: Key?, defaultValue: Object?): Object? {
        return comp.get(access, key, defaultValue)
    }

    @Override
    fun toDumpData(pageContext: PageContext?, maxlevel: Int, dp: DumpProperties?, access: Int): DumpData? {
        return toDumpData(pageContext, maxlevel, dp, access)
    }

    @Override
    fun contains(access: Int, name: Key?): Boolean {
        return comp.contains(access, name)
    }

    @Override
    fun getMember(access: Int, key: Key?, dataMember: Boolean, superAccess: Boolean): Member? {
        return comp!!.getMember(access, key, dataMember, superAccess)
    }

    @Override
    fun staticScope(): Scope? {
        return comp!!.staticScope()
    }

    @Override
    fun beforeStaticConstructor(pc: PageContext?): Variables? {
        return comp!!.beforeStaticConstructor(pc)
    }

    @Override
    fun afterStaticConstructor(pc: PageContext?, `var`: Variables?) {
        comp!!.afterStaticConstructor(pc, `var`)
    }

    @Override
    fun getInterfaces(): Array<Interface?>? {
        return comp!!.getInterfaces()
    }

    @Override
    fun id(): String? {
        return comp!!.id()
    }

    companion object {
        fun superMember(comp: ComponentImpl?): Member? {
            return if (comp == null) DataMember(Component.ACCESS_PRIVATE, Member.MODIFIER_NONE, StructImpl()) else SuperComponent(comp)
        }

        fun superInstance(comp: ComponentImpl?): Collection? {
            return if (comp == null) StructImpl() else SuperComponent(comp)
        }
    }

    init {
        this.comp = comp
    }
}