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
package tachyon.runtime.tag

import tachyon.runtime.exp.ApplicationException

/**
 * Reads, writes, and deletes keys and values in the system registry. The cfregistry tag is
 * supported on all platforms, including Linux, Solaris, and HP-UX.
 *
 *
 *
 */
class Registry : TagImpl() {
    /**
     * Value data to set. If you omit this attribute, cfregistry creates default value, as follows:
     *
     * string: creates an empty string: "" dWord: creates a value of 0 (zero)
     */
    private var value: String? = null

    /** action to the registry  */
    private var action: Short = -1

    /**
     * Sorts query column data (case-insensitive). Sorts on Entry, Type, and Value columns as text.
     * Specify a combination of columns from query output, in a comma-delimited list. For example: sort
     * = "value desc, entry asc"
     *
     * asc: ascending (a to z) sort order desc: descending (z to a) sort order
     */
    private var sort: String? = null

    /**
     * string: return string values dWord: return DWord values key: return keys any: return keys and
     * values
     */
    private var type: Short = RegistryEntry.TYPE_ANY

    /** Name of a registry branch.  */
    private var branch: String? = null

    /** Registry value to access.  */
    private var entry: String? = null

    /** Variable into which to put value.  */
    private var variable: String? = null

    /** Name of record set to contain returned keys and values.  */
    private var name: String? = null
    @Override
    fun release() {
        super.release()
        value = null
        action = -1
        sort = null
        type = RegistryEntry.TYPE_ANY
        branch = null
        entry = null
        variable = null
        name = null
    }

    /**
     * set the value value Value data to set. If you omit this attribute, cfregistry creates default
     * value, as follows:
     *
     * string: creates an empty string: "" dWord: creates a value of 0 (zero)
     *
     * @param value value to set
     */
    fun setValue(value: String?) {
        this.value = value
    }

    /**
     * set the value action action to the registry
     *
     * @param action value to set
     * @throws ApplicationException
     */
    @Throws(ApplicationException::class)
    fun setAction(action: String?) {
        var action = action
        action = action.toLowerCase().trim()
        if (action.equals("getall")) this.action = ACTION_GET_ALL else if (action.equals("get")) this.action = ACTION_GET else if (action.equals("set")) this.action = ACTION_SET else if (action.equals("delete")) this.action = ACTION_DELETE else throw ApplicationException("attribute action of the tag registry has an invalid value [$action], valid values are [getAll, get, set, delete]")
    }

    /**
     * set the value sort Sorts query column data (case-insensitive). Sorts on Entry, Type, and Value
     * columns as text. Specify a combination of columns from query output, in a comma-delimited list.
     * For example: sort = "value desc, entry asc"
     *
     * asc: ascending (a to z) sort order desc: descending (z to a) sort order
     *
     * @param sort value to set
     */
    fun setSort(sort: String?) {
        this.sort = sort
    }

    /**
     * set the value type string: return string values dWord: return DWord values key: return keys any:
     * return keys and values
     *
     * @param type value to set
     * @throws ApplicationException
     */
    @Throws(ApplicationException::class)
    fun setType(type: String?) {
        var type = type
        type = type.toLowerCase().trim()
        if (type.equals("string")) this.type = RegistryEntry.TYPE_STRING else if (type.equals("dword")) this.type = RegistryEntry.TYPE_DWORD else if (type.equals("key")) this.type = RegistryEntry.TYPE_KEY else if (type.equals("any")) this.type = RegistryEntry.TYPE_ANY else throw ApplicationException("attribute type of the tag registry has an invalid value [$type], valid values are [string, dword]")
    }

    /**
     * set the value branch Name of a registry branch.
     *
     * @param branch value to set
     */
    fun setBranch(branch: String?) {
        this.branch = branch
    }

    /**
     * set the value entry Registry value to access.
     *
     * @param entry value to set
     */
    fun setEntry(entry: String?) {
        this.entry = entry
    }

    /**
     * set the value variable Variable into which to put value.
     *
     * @param variable value to set
     */
    fun setVariable(variable: String?) {
        this.variable = variable
    }

    /**
     * set the value name Name of record set to contain returned keys and values.
     *
     * @param name value to set
     */
    fun setName(name: String?) {
        this.name = name
    }

    @Override
    @Throws(PageException::class)
    fun doStartTag(): Int {
        if (pageContext.getConfig().getSecurityManager().getAccess(SecurityManager.TYPE_TAG_REGISTRY) === SecurityManager.VALUE_NO) throw SecurityException("can't access tag [registry]", "access is prohibited by security manager")
        if (action == ACTION_GET) doGet() else if (action == ACTION_GET_ALL) doGetAll() else if (action == ACTION_SET) doSet() else if (action == ACTION_DELETE) doDelete()
        return SKIP_BODY
    }

    /**
     * @throws PageException
     */
    @Throws(PageException::class)
    private fun doDelete() {
        try {
            RegistryQuery.deleteValue(branch, entry)
        } catch (e: Exception) {
            throw Caster.toPageException(e)
        }
    }

    /**
     * @throws PageException
     */
    @Throws(PageException::class)
    private fun doSet() {
        if (entry == null) throw ApplicationException("attribute [entry] is required for tag [registry], when action is [set]")
        if (type == RegistryEntry.TYPE_ANY) type = RegistryEntry.TYPE_STRING
        if (value == null) {
            value = if (type == RegistryEntry.TYPE_DWORD) "0" else ""
        }
        try {
            RegistryQuery.setValue(branch, entry, type, value)
        } catch (e: Exception) {
            throw Caster.toPageException(e)
        }
    }

    /**
     * @throws PageException
     */
    @Throws(PageException::class)
    private fun doGetAll() {
        if (name == null) throw ApplicationException("attribute [name] is required for tag [registry], when action is [getAll]")
        try {
            val entries: Array<RegistryEntry?> = RegistryQuery.getValues(branch, type)
            if (entries != null) {
                val qry: tachyon.runtime.type.Query = QueryImpl(arrayOf<String?>("entry", "type", "value"), arrayOf<String?>("VARCHAR", "VARCHAR", "OTHER"), entries.size, "query")
                for (i in entries.indices) {
                    val e: RegistryEntry? = entries[i]
                    val row: Int = i + 1
                    qry.setAt(KeyConstants._entry, row, e.getKey())
                    qry.setAt(KeyConstants._type, row, RegistryEntry.toCFStringType(e.getType()))
                    qry.setAt(KeyConstants._value, row, e.getValue())
                }

                // sort
                if (sort != null) {
                    val arr: Array<String?> = sort.toLowerCase().split(",")
                    for (i in arr.indices.reversed()) {
                        val col: Array<String?> = arr[i].trim().split("\\s+")
                        if (col.size == 1) qry.sort(KeyImpl.init(col[0].trim())) else if (col.size == 2) {
                            val order: String = col[1].toLowerCase().trim()
                            if (order.equals("asc")) qry.sort(KeyImpl.init(col[0]), tachyon.runtime.type.Query.ORDER_ASC) else if (order.equals("desc")) qry.sort(KeyImpl.init(col[0]), tachyon.runtime.type.Query.ORDER_DESC) else throw ApplicationException("invalid order type [" + col[1] + "]")
                        }
                    }
                }
                pageContext.setVariable(name, qry)
            }
        } catch (e: Exception) {
            throw Caster.toPageException(e)
        }
    }

    /**
     * @throws PageException
     */
    @Throws(PageException::class)
    private fun doGet() {
        if (entry == null) throw ApplicationException("attribute [entry] is required for tag [registry], when action is [get]")
        if (variable == null) throw ApplicationException("attribute [variable] is required for tag [registry], when action is [get]")
        try {
            val re: RegistryEntry = RegistryQuery.getValue(branch, entry, type)
            if (re != null) pageContext.setVariable(variable, re.getValue())
        } catch (e: Exception) {
            throw Caster.toPageException(e)
        }
    }

    @Override
    fun doEndTag(): Int {
        return EVAL_PAGE
    }

    companion object {
        private const val ACTION_GET_ALL: Short = 0
        private const val ACTION_GET: Short = 1
        private const val ACTION_SET: Short = 2
        private const val ACTION_DELETE: Short = 3
    }
}