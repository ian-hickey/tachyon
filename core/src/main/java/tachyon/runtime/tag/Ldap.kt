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

import java.io.IOException

// TODO tag ldap 
// attr rebind
/**
 * Provides an interface to LDAP Lightweight Directory Access Protocol directory servers like the
 * Netscape Directory Server.
 */
class Ldap : TagImpl() {
    private var delimiter: String? = ";"
    private var server: String? = null
    private var port = 389
    private var secureLevel: Short = LDAPClient.SECURE_NONE
    private var returnAsBinary: Array<String?>? = arrayOfNulls<String?>(0)
    private var attributes: String? = null
    private var username: String? = null
    private var password: String? = null
    private var action: String? = "query"
    private var sort: Array<String?>? = arrayOfNulls<String?>(0)
    private var dn: String? = null
    private var referral = 0
    private var scope: Int = SearchControls.SUBTREE_SCOPE
    private var sortType: Int = LDAPClient.SORT_TYPE_CASE
    private var sortDirection: Int = LDAPClient.SORT_DIRECTION_ASC
    private var startrow = 1
    private var timeout = 60000
    private var maxrows = 0
    private var name: String? = null
    private var start: String? = null
    private var separator: String? = ","
    private var filter: String? = "objectclass = *"
    private var modifyType: Int = DirContext.REPLACE_ATTRIBUTE
    private var rebind = false
    @Override
    fun release() {
        action = "query"
        delimiter = ";"
        port = 389
        secureLevel = LDAPClient.SECURE_NONE
        returnAsBinary = arrayOfNulls<String?>(0)
        username = null
        password = null
        referral = 0
        attributes = null
        sort = arrayOfNulls<String?>(0)
        dn = null
        name = null
        scope = SearchControls.SUBTREE_SCOPE
        startrow = 1
        timeout = 60000
        maxrows = -1
        sortType = LDAPClient.SORT_TYPE_CASE
        sortDirection = LDAPClient.SORT_DIRECTION_ASC
        start = null
        separator = ","
        filter = "objectclass = *"
        modifyType = DirContext.REPLACE_ATTRIBUTE
        rebind = false
        super.release()
    }

    /**
     * @param filterfile The filterfile to set.
     * @throws ApplicationException
     */
    fun setFilterfile(filterfile: String?) {
        // DeprecatedUtil.tagAttribute(pageContext,"LDAP", "filterfile");
    }

    /**
     * Specifies the character that cfldap uses to separate multiple attribute name/value pairs when
     * more than one attribute is specified in the attribute attribute or the attribute that you want to
     * use has the default delimiter character, which is the semicolon (;), such as
     * mgrpmsgrejecttext;lang-en. The delimiter character is used by the query, add, and modify action
     * attributes, and is used by cfldap to output multi-value attributes
     *
     * @param delimiter delimiter to set
     */
    fun setDelimiter(delimiter: String?) {
        this.delimiter = delimiter
    }

    /**
     * Used in conjunction with action = "Query". Specifies the first row of the LDAP query to insert
     * into the query. The default is 1.
     *
     * @param startrow The startrow to set.
     */
    fun setStartrow(startrow: Double) {
        this.startrow = startrow.toInt()
    }

    /**
     * Specifies the maximum number of entries for LDAP queries.
     *
     * @param maxrows The maxrows to set.
     */
    fun setMaxrows(maxrows: Double) {
        this.maxrows = maxrows.toInt()
    }

    /**
     * Specifies the maximum amount of time, in milliseconds, to wait for LDAP processing. Defaults to 60
     * seconds.
     *
     * @param timeout The timeout to set.
     */
    fun setTimeout(timeout: Double) {
        this.timeout = timeout.toInt()
    }

    /**
     * @param password The password to set.
     */
    fun setPassword(password: String?) {
        this.password = password
    }

    /**
     * Port defaults to the standard LDAP port, 389.
     *
     * @param port The port to set.
     */
    fun setPort(port: Double) {
        this.port = port.toInt()
    }

    /**
     * Identifies the type of security to employ, CFSSL_BASIC or CFSSL_CLIENT_AUTH, and additional
     * information that is required by the specified security type.
     *
     * @param referral The referral to set.
     */
    fun setReferral(referral: Double) {
        this.referral = referral.toInt()
    }

    /**
     * Host name "biff.upperlip.com" or IP address "192.1.2.225" of the LDAP server.
     *
     * @param server The server to set.
     */
    fun setServer(server: String?) {
        this.server = server
    }

    /**
     * If no user name is specified, the LDAP connection is anonymous.
     *
     * @param username The username to set.
     */
    fun setUsername(username: String?) {
        this.username = username
    }

    /**
     * @param secure The secureLevel to set.
     * @throws ApplicationException
     */
    @Throws(ApplicationException::class)
    fun setSecure(secure: String?) {
        var secure = secure
        secure = secure.trim().toUpperCase()
        secureLevel = if (secure.equals("CFSSL_BASIC")) LDAPClient.SECURE_CFSSL_BASIC else if (secure.equals("CFSSL_CLIENT_AUTH")) LDAPClient.SECURE_CFSSL_CLIENT_AUTH else throw ApplicationException("invalid value for attribute secure [$secure], valid values are [CFSSL_BASIC, CFSSL_CLIENT_AUTH]")
    }

    /**
     * Specifies the scope of the search from the entry specified in the Start attribute for action =
     * "Query".
     *
     * @param strScope The scope to set.
     * @throws ApplicationException
     */
    @Throws(ApplicationException::class)
    fun setScope(strScope: String?) {
        var strScope = strScope
        strScope = strScope.trim().toLowerCase()
        scope = if (strScope.equals("onelevel")) SearchControls.ONELEVEL_SCOPE else if (strScope.equals("base")) SearchControls.OBJECT_SCOPE else if (strScope.equals("subtree")) SearchControls.SUBTREE_SCOPE else throw ApplicationException("invalid value for attribute scope [$strScope], valid values are [oneLevel,base,subtree]")
    }

    /**
     * Indicates whether to add, delete, or replace an attribute in a multi-value list of attributes.
     *
     * @param modifyType The modifyType to set.
     * @throws ApplicationException
     */
    @Throws(ApplicationException::class)
    fun setModifytype(modifyType: String?) {
        var modifyType = modifyType
        modifyType = modifyType.trim().toLowerCase()
        if (modifyType.equals("add")) this.modifyType = DirContext.ADD_ATTRIBUTE else if (modifyType.equals("delete")) this.modifyType = DirContext.REMOVE_ATTRIBUTE else if (modifyType.equals("replace")) this.modifyType = DirContext.REPLACE_ATTRIBUTE else throw ApplicationException("invalid value for attribute modifyType [$modifyType], valid values are [add,replace,delete]")
    }

    /**
     * @param returnAsBinary The returnAsBinary to set.
     * @throws PageException
     */
    @Throws(PageException::class)
    fun setReturnasbinary(returnAsBinary: String?) {
        this.returnAsBinary = ArrayUtil.trimItems(ListUtil.toStringArray(ListUtil.listToArrayRemoveEmpty(returnAsBinary, ',')))
    }

    /**
     * Indicates the attribute or attributes by which to sort query results. Use a comma [,] to separate
     * attributes.
     *
     * @param sort The sort to set.
     * @throws PageException
     */
    @Throws(PageException::class)
    fun setSort(sort: String?) {
        this.sort = ArrayUtil.trimItems(ListUtil.toStringArray(ListUtil.listToArrayRemoveEmpty(sort, ',')))
    }

    /**
     * Specifies how to sort query results.
     *
     * @param sortControl sortControl to set
     * @throws PageException
     */
    @Throws(PageException::class)
    fun setSortcontrol(sortControl: String?) {
        val sortControlArr: Array<String?> = ArrayUtil.trimItems(ListUtil.toStringArray(ListUtil.listToArrayRemoveEmpty(sortControl, ',')))
        for (i in sortControlArr.indices) {
            val scs: String = sortControlArr[i].trim().toLowerCase()
            if (scs.equals("asc")) sortDirection = LDAPClient.SORT_DIRECTION_ASC else if (scs.equals("desc")) sortDirection = LDAPClient.SORT_DIRECTION_DESC else if (scs.equals("case")) sortType = LDAPClient.SORT_TYPE_CASE else if (scs.equals("nocase")) sortType = LDAPClient.SORT_TYPE_NOCASE else throw ApplicationException("invalid value for attribute sortControl [$sortControl], valid values are [asc,desc,case,nocase]")
        }
    }

    /**
     * @param strAttributes
     */
    fun setAttributes(strAttributes: String?) {
        attributes = strAttributes
    }

    /**
     * Specifies the LDAP action.
     *
     * @param action The action to set.
     */
    fun setAction(action: String?) {
        this.action = action.trim().toLowerCase()
    }

    /**
     * Specifies the distinguished name for update actions.
     *
     * @param dn The dn to set.
     */
    fun setDn(dn: String?) {
        this.dn = dn
    }

    /**
     * The name you assign to the LDAP query.
     *
     * @param name The name to set.
     */
    fun setName(name: String?) {
        this.name = name
    }

    /**
     * Specifies the character that cfldap uses to separate attribute values in multi-value attributes.
     * This character is used by the query, add, and modify action attributes, and by cfldap to output
     * multi-value attributes. The default character is the comma (,).
     *
     * @param separator The separator to set.
     */
    fun setSeparator(separator: String?) {
        this.separator = separator
    }

    /**
     * Specifies the distinguished name of the entry to be used to start the search.
     *
     * @param start The start to set.
     */
    fun setStart(start: String?) {
        this.start = start
    }

    /**
     * @param filter The filter to set.
     */
    fun setFilter(filter: String?) {
        this.filter = filter
    }

    /**
     * If you set rebind to Yes, cfldap attempts to rebind the referral callback and reissue the query
     * by the referred address using the original credentials. The default is No, which means referred
     * connections are anonymous.
     *
     * @param rebind The rebind to set.
     */
    fun setRebind(rebind: Boolean) {
        this.rebind = rebind
    }

    @Override
    @Throws(PageException::class)
    fun doStartTag(): Int {
        return try {
            _doStartTag()
        } catch (e: Exception) {
            throw Caster.toPageException(e)
        }
    }

    @Throws(NamingException::class, PageException::class, IOException::class, ClassException::class)
    private fun _doStartTag(): Int {

        // LDAPClient client=new
        // LDAPClient(server,port,secureLevel,returnAsBinary,username,password,referral);
        val client = LDAPClient(server, port, timeout, returnAsBinary)
        if (secureLevel != LDAPClient.SECURE_NONE) client.setSecureLevel(secureLevel)
        if (username != null) client.setCredential(username, password)
        if (referral > 0) client.setReferral(referral)
        if (action!!.equals("add")) {
            required("LDAP", action, "attributes", attributes)
            required("LDAP", action, "dn", dn)
            client.add(dn, attributes, delimiter, separator)
        } else if (action!!.equals("delete")) {
            required("LDAP", action, "dn", dn)
            client.delete(dn)
        } else if (action!!.equals("modifydn")) {
            required("LDAP", action, "attributes", attributes)
            required("LDAP", action, "dn", dn)
            client.modifydn(dn, attributes)
        } else if (action!!.equals("modify")) {
            required("LDAP", action, "attributes", attributes)
            required("LDAP", action, "dn", dn)
            client.modify(dn, modifyType, attributes, delimiter, separator)
        } else if (action!!.equals("query")) {
            required("LDAP", action, "start", start)
            required("LDAP", action, "attributes", attributes)
            required("LDAP", action, "name", name)
            val qry: Query = client.query(attributes, scope, startrow, maxrows, timeout, sort, sortType, sortDirection, start, separator, filter)
            pageContext.setVariable(name, qry)
        } else throw ApplicationException("invalid value for attribute action [$action], valid values are [add,delete,modifydn,modify,query]")
        return SKIP_BODY
    }
}