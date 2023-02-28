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
package tachyon.runtime.type

import tachyon.runtime.Component

/**
 * a user defined function
 *
 */
interface UDF : Function, Dumpable, Member, Cloneable {
    /**
     * abstract method for the function Body
     *
     * @param pageContext Page Context
     * @return Object
     * @throws Throwable Throwable
     */
    @Throws(Throwable::class)
    fun implementation(pageContext: PageContext?): Object?

    /**
     * return all function arguments of this UDF
     *
     * @return the arguments.
     */
    val functionArguments: Array<FunctionArgument?>?

    /**
     * @param pc Page Context
     * @param index index
     * @return default value
     * @throws PageException Page Exception
     */
    @Deprecated
    @Deprecated("""use instead
	              <code> getDefaultValue(PageContext pc, int index, Object defaultValue)</code>
	  """)
    @Throws(PageException::class)
    fun getDefaultValue(pc: PageContext?, index: Int): Object?

    @Throws(PageException::class)
    fun getDefaultValue(pc: PageContext?, index: Int, defaultValue: Object?): Object?
    val index: Int

    /**
     * @return Returns the functionName.
     */
    val functionName: String?

    /**
     * @return Returns the output.
     */
    val output: Boolean

    /**
     * @return Returns the returnType.
     */
    val returnType: Int
    fun getBufferOutput(pc: PageContext?): Boolean

    /**
     *
     * @return return format
     */
    @get:Deprecated("use instead")
    @get:Deprecated
    val returnFormat: Int
    fun getReturnFormat(defaultFormat: Int): Int

    /**
     * returns null when not defined
     *
     * @return value of attribute securejson
     */
    val secureJson: Boolean?

    /**
     * returns null when not defined
     *
     * @return value of attribute verifyclient
     */
    val verifyClient: Boolean?

    /**
     * @return Returns the returnType.
     */
    val returnTypeAsString: String?
    val description: String?

    /**
     * call user defined Function with a hashmap of named values
     *
     * @param pageContext Page Context
     * @param values named values
     * @param doIncludePath do include path
     * @return return value of the function
     * @throws PageException Page Exception
     */
    @Throws(PageException::class)
    fun callWithNamedValues(pageContext: PageContext?, values: Struct?, doIncludePath: Boolean): Object?

    /**
     * call user defined Function with parameters as Object Array
     *
     * @param pageContext Page Context
     * @param args parameters for the function
     * @param doIncludePath do include path
     * @return return value of the function
     * @throws PageException Page Exception
     */
    @Throws(PageException::class)
    fun call(pageContext: PageContext?, args: Array<Object?>?, doIncludePath: Boolean): Object?

    /**
     * @return Returns the displayName.
     */
    val displayName: String?

    /**
     * @return Returns the hint.
     */
    val hint: String?

    // public abstract PageSource getPageSource();
    val source: String?

    @Throws(PageException::class)
    fun getMetaData(pc: PageContext?): Struct?
    fun duplicate(): UDF?

    /**
     * it is the component in which this udf is constructed, must not be the same as active udf
     *
     * @return owner component
     */
    @get:Deprecated("")
    @get:Deprecated
    val ownerComponent: tachyon.runtime.Component?

    /**
     * call user defined Function with a struct
     *
     * @param pageContext Page Context
     * @param calledName called Name
     * @param values named values
     * @param doIncludePath do include path
     * @return return value of the function
     * @throws PageException Page Exception
     */
    @Throws(PageException::class)
    fun callWithNamedValues(pageContext: PageContext?, calledName: Collection.Key?, values: Struct?, doIncludePath: Boolean): Object?

    /**
     * call user defined Function with parameters as Object Array
     *
     * @param pageContext Page Context
     * @param calledName called Name
     * @param args parameters for the function
     * @param doIncludePath do include path
     * @return return value of the function
     * @throws PageException Page Exception
     */
    @Throws(PageException::class)
    fun call(pageContext: PageContext?, calledName: Collection.Key?, args: Array<Object?>?, doIncludePath: Boolean): Object?

    /**
     * unique identifier for the function
     *
     * @return Returns the unique identifier.
     */
    fun id(): String?

    // public abstract Page getPage(PageContext pc);
    val pageSource: PageSource?

    companion object {
        const val RETURN_FORMAT_WDDX = 0
        const val RETURN_FORMAT_JSON = 1
        const val RETURN_FORMAT_PLAIN = 2
        const val RETURN_FORMAT_SERIALIZE = 3
        const val RETURN_FORMAT_XML = 4
        const val RETURN_FORMAT_JAVA = 5
    }
}