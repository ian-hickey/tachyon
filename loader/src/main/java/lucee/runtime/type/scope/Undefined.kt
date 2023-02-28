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

import java.util.List

/**
 * interface of the cope undefined
 */
interface Undefined : Scope {
    /**
     * @return returns the current local scope defined in the undefined scope
     */
    fun localScope(): Local?
    fun argumentsScope(): Argument?
    fun variablesScope(): Variables?

    /**
     * sets mode of scope
     *
     * @param mode new mode
     * @return old mode
     */
    fun setMode(mode: Int): Int
    val localAlways: Boolean

    /**
     * sets the functions scopes
     *
     * @param local local scope
     * @param argument argument scope
     */
    fun setFunctionScopes(local: Local?, argument: Argument?)
    /**
     * @return returns current collection stack
     */
    /**
     * sets an individual query stack to the undefined scope
     *
     * @param qryStack Query stack
     */
    var queryStack: QueryStack?

    /**
     * add a collection to the undefined scope
     *
     * @param qry Query to add to undefined scope
     */
    fun addQuery(qry: Query?)

    /**
     * remove a collection from the undefined scope
     */
    fun removeQuery()

    /**
     * return value matching key, if value is from Query return a QueryColumn
     *
     * @param key key
     * @return return matched value
     * @throws PageException Page Exception
     */
    @Deprecated
    @Deprecated("use instead <code>{@link #getCollection(lucee.runtime.type.Collection.Key)}</code>")
    @Throws(PageException::class)
    fun getCollection(key: String?): Object?
    val scopeNames: List<String?>?

    /**
     * return value matching key, if value is from Query return a QueryColumn
     *
     * @param key key
     * @return return matched value
     * @throws PageException Page Exception
     */
    @Throws(PageException::class)
    fun getCollection(key: Collection.Key?): Object?

    /**
     * gets a key from all cascaded scopes, but not from variables scope
     *
     * @param key key to get
     * @return matching value or null
     * @see .getCascading
     */
    @Deprecated
    @Deprecated("""use instead
	  """)
    fun getCascading(key: Collection.Key?): Object?

    /**
     * gets a key from all cascaded scopes, but not from variables scope
     *
     * @param key key to get
     * @param defaultValue default value
     * @return matching value or null
     */
    fun getCascading(key: Collection.Key?, defaultValue: Object?): Object?

    /**
     * change the variable scope
     *
     * @param scope scope
     */
    fun setVariableScope(scope: Variables?)

    /**
     * @return if check for arguments and local scope values
     */
    val checkArguments: Boolean
    fun getScope(key: Collection.Key?): Struct?
    fun setAllowImplicidQueryCall(allowImplicidQueryCall: Boolean): Boolean
    fun reinitialize(pc: PageContext?)

    companion object {
        const val MODE_NO_LOCAL_AND_ARGUMENTS = 0
        const val MODE_LOCAL_OR_ARGUMENTS_ONLY_WHEN_EXISTS = 1
        const val MODE_LOCAL_OR_ARGUMENTS_ALWAYS = 2
    }
}