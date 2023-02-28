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

/**
 * interface for a Component
 */
interface Component : Struct, Objects, CIObject {
    /**
     * returns java class to the component interface (all UDFs), this class is generated dynamic when
     * used
     *
     * @param isNew is new
     * @return Java Class
     * @throws PageException Page Exception
     */
    @Deprecated
    @Deprecated("""use instead
	              <code>getJavaAccessClass(PageContext pc,RefBoolean isNew,boolean writeLog, boolean takeTop, boolean create, boolean supressWSbeforeArg,boolean output)</code>""")
    @Throws(PageException::class)
    fun getJavaAccessClass(isNew: RefBoolean?): Class<*>?

    /**
     * returns java class to the component interface (all UDFs), this class is generated dynamic when
     * used
     *
     * @param pc page context
     * @param isNew is new
     * @param writeLog write log
     * @param takeTop take top
     * @param create create
     * @param supressWSbeforeArg suppress whitesapce before argument
     * @return Java Class
     * @throws PageException Page Exception
     */
    @Deprecated
    @Deprecated("""use instead
	              <code>getJavaAccessClass(PageContext pc,RefBoolean isNew,boolean writeLog, boolean takeTop, boolean create, boolean supressWSbeforeArg, boolean output, boolean returnValue)</code>""")
    @Throws(PageException::class)
    fun getJavaAccessClass(pc: PageContext?, isNew: RefBoolean?, writeLog: Boolean, takeTop: Boolean, create: Boolean, supressWSbeforeArg: Boolean): Class<*>?

    @Throws(PageException::class)
    fun getJavaAccessClass(pc: PageContext?, isNew: RefBoolean?, writeLog: Boolean, takeTop: Boolean, create: Boolean, supressWSbeforeArg: Boolean, output: Boolean,
                           returnValue: Boolean): Class<*>?

    /**
     * @return Returns the display name.
     */
    fun getDisplayName(): String?

    /**
     * @return Returns the Extends.
     */
    fun getExtends(): String?
    fun getModifier(): Int

    /**
     * @return Returns the Hint.
     */
    fun getHint(): String?

    /**
     * @return Returns the Name.
     */
    fun getName(): String?

    /**
     * @return Returns the Name.
     */
    fun getCallName(): String?

    /**
     * @return Returns the Name.
     */
    fun getAbsName(): String?

    /**
     * @return Returns the output.
     */
    fun getOutput(): Boolean

    /**
     * check if Component is instance of this type
     *
     * @param type type to compare as String
     * @return is instance of this type
     */
    fun instanceOf(type: String?): Boolean

    /**
     * check if value is a valid access modifier constant
     *
     * @param access access
     * @return is valid access
     */
    fun isValidAccess(access: Int): Boolean

    /**
     * is a persistent component (orm)
     *
     * @return is a persistent component
     */
    fun isPersistent(): Boolean

    /**
     * has accessors set
     *
     * @return are accessors enabled
     */
    fun isAccessors(): Boolean

    /**
     * returns Meta Data to the Component
     *
     * @param pc page context
     * @return meta data to component
     * @throws PageException Page Exception
     */
    @Throws(PageException::class)
    fun getMetaData(pc: PageContext?): Struct?
    fun getMetaStructItem(name: Collection.Key?): Object?

    /**
     * call a method of the component with no named arguments
     *
     * @param pc PageContext
     * @param key name of the method
     * @param args Arguments for the method
     * @return return result of the method
     * @throws PageException Page Exception
     */
    @Throws(PageException::class)
    fun call(pc: PageContext?, key: String?, args: Array<Object?>?): Object?

    /**
     * call a method of the component with named arguments
     *
     * @param pc PageContext
     * @param key name of the method
     * @param args Named Arguments for the method
     * @return return result of the method
     * @throws PageException Page Exception
     */
    @Throws(PageException::class)
    fun callWithNamedValues(pc: PageContext?, key: String?, args: Struct?): Object?

    /**
     * return all properties from component
     *
     * @param onlyPeristent if true return only columns where attribute persistent is not set to false
     * @return all component properties
     */
    @Deprecated
    @Deprecated("""use instead
	              <code>getProperties(boolean onlyPeristent, boolean includeBaseProperties, boolean preferBaseProperties, boolean inheritedMappedSuperClassOnly)</code>""")
    fun getProperties(onlyPeristent: Boolean): Array<Property?>?

    /**
     * return all properties from component
     *
     * @param onlyPeristent if true return only columns where attribute persistent is not set to false
     * @param includeBaseProperties include base properties
     * @param preferBaseProperties prefer base properties
     * @param inheritedMappedSuperClassOnly inherited Mapped Super Class Only
     * @return all component properties
     */
    fun getProperties(onlyPeristent: Boolean, includeBaseProperties: Boolean, preferBaseProperties: Boolean, inheritedMappedSuperClassOnly: Boolean): Array<Property?>?

    /**
     * set component property
     *
     * @param property property
     * @throws PageException Page Exception
     */
    @Throws(PageException::class)
    fun setProperty(property: Property?)
    fun getComponentScope(): ComponentScope?
    fun contains(pc: PageContext?, key: Key?): Boolean
    fun getPageSource(): PageSource?

    // public Member getMember(int access,Collection.Key key, boolean dataMember,boolean superAccess);
    fun getBaseAbsName(): String?
    fun isBasePeristent(): Boolean
    fun equalTo(type: String?): Boolean
    fun getWSDLFile(): String?
    fun setEntity(entity: Boolean)
    fun isEntity(): Boolean
    fun getBaseComponent(): Component?

    /**
     * register UDF
     *
     * @param key key
     * @param udf User Defined Function
     * @throws PageException Page Exception
     */
    @Throws(PageException::class)
    fun registerUDF(key: Collection.Key?, udf: UDF?)

    @Throws(PageException::class)
    fun registerUDF(key: Collection.Key?, props: UDFProperties?)

    // access
    fun keySet(access: Int): Set<Key?>?

    @Throws(PageException::class)
    fun call(pc: PageContext?, access: Int, name: Collection.Key?, args: Array<Object?>?): Object?

    @Throws(PageException::class)
    fun callWithNamedValues(pc: PageContext?, access: Int, name: Collection.Key?, args: Struct?): Object?
    fun size(access: Int): Int
    fun keys(access: Int): Array<Collection.Key?>?
    fun keyIterator(access: Int): Iterator<Collection.Key?>?
    fun keysAsStringIterator(access: Int): Iterator<String?>?
    fun entryIterator(access: Int): Iterator<Entry<Key?, Object?>?>?
    fun valueIterator(access: Int): Iterator<Object?>?

    @Throws(PageException::class)
    operator fun get(access: Int, key: Collection.Key?): Object?
    operator fun get(access: Int, key: Collection.Key?, defaultValue: Object?): Object?
    fun toDumpData(pageContext: PageContext?, maxlevel: Int, dp: DumpProperties?, access: Int): DumpData?
    fun contains(access: Int, name: Key?): Boolean
    fun getMember(access: Int, key: Collection.Key?, dataMember: Boolean, superAccess: Boolean): Member?
    fun staticScope(): Scope?
    fun getInterfaces(): Array<Interface?>?
    fun id(): String?

    companion object {
        /**
         * Constant for Access Mode Remote
         */
        const val ACCESS_REMOTE = 0

        /**
         * Constant for Access Mode Public
         */
        const val ACCESS_PUBLIC = 1

        /**
         * Constant for Access Mode Package
         */
        const val ACCESS_PACKAGE = 2

        /**
         * Constant for Access Mode Private
         */
        const val ACCESS_PRIVATE = 3
        val MODIFIER_NONE: Int = Member.MODIFIER_NONE
        val MODIFIER_FINAL: Int = Member.MODIFIER_FINAL
        val MODIFIER_ABSTRACT: Int = Member.MODIFIER_ABSTRACT
    }
}