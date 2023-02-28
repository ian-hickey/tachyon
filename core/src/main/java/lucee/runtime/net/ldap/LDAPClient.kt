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
package lucee.runtime.net.ldap

import java.io.IOException

/**
 * Ldap Client
 */
class LDAPClient(server: String?, port: Int, timeout: Int, binaryColumns: Array<String?>?) {
    var env: Hashtable? = Hashtable()

    /**
     * sets username password for the connection
     *
     * @param username
     * @param password
     */
    fun setCredential(username: String?, password: String?) {
        if (username != null) {
            env.put("java.naming.security.principal", username)
            env.put("java.naming.security.credentials", password)
        } else {
            env.remove("java.naming.security.principal")
            env.remove("java.naming.security.credentials")
        }
    }

    /**
     * sets the secure Level
     *
     * @param secureLevel [SECURE_CFSSL_BASIC, SECURE_CFSSL_CLIENT_AUTH, SECURE_NONE]
     * @throws ClassNotFoundException
     * @throws ClassException
     */
    @Throws(ClassException::class)
    fun setSecureLevel(secureLevel: Short) {
        // Security
        if (secureLevel == SECURE_CFSSL_BASIC) {
            env.put("java.naming.security.protocol", "ssl")
            env.put("java.naming.ldap.factory.socket", "javax.net.ssl.SSLSocketFactory")
            val clazz: Class = ClassUtil.loadClass("com.sun.net.ssl.internal.ssl.Provider")
            try {
                Security.addProvider(ClassUtil.newInstance(clazz) as Provider)
            } catch (e: Exception) {
                throw RuntimeException(e)
            }
        } else if (secureLevel == SECURE_CFSSL_CLIENT_AUTH) {
            env.put("java.naming.security.protocol", "ssl")
            env.put("java.naming.security.authentication", "EXTERNAL")
        } else {
            env.put("java.naming.security.authentication", "simple")
            env.remove("java.naming.security.protocol")
            env.remove("java.naming.ldap.factory.socket")
        }
    }

    /**
     * sets thr referral
     *
     * @param referral
     */
    fun setReferral(referral: Int) {
        if (referral > 0) {
            env.put("java.naming.referral", "follow")
            env.put("java.naming.ldap.referral.limit", Caster.toString(referral))
        } else {
            env.put("java.naming.referral", "ignore")
            env.remove("java.naming.ldap.referral.limit")
        }
    }

    /**
     * adds LDAP entries to LDAP server
     *
     * @param dn
     * @param attributes
     * @param delimiter
     * @throws NamingException
     * @throws PageException
     */
    @Throws(NamingException::class, PageException::class)
    fun add(dn: String?, attributes: String?, delimiter: String?, seperator: String?) {
        val ctx: DirContext = InitialDirContext(env)
        ctx.createSubcontext(dn, toAttributes(attributes, delimiter, seperator))
        ctx.close()
    }

    /**
     * deletes LDAP entries on an LDAP server
     *
     * @param dn
     * @throws NamingException
     */
    @Throws(NamingException::class)
    fun delete(dn: String?) {
        val ctx: DirContext = InitialDirContext(env)
        ctx.destroySubcontext(dn)
        ctx.close()
    }

    /**
     * modifies distinguished name attribute for LDAP entries on LDAP server
     *
     * @param dn
     * @param attributes
     * @throws NamingException
     */
    @Throws(NamingException::class)
    fun modifydn(dn: String?, attributes: String?) {
        val ctx: DirContext = InitialDirContext(env)
        ctx.rename(dn, attributes)
        ctx.close()
    }

    @Throws(NamingException::class, PageException::class)
    fun modify(dn: String?, modifytype: Int, strAttributes: String?, delimiter: String?, separator: String?) {
        val context: DirContext = InitialDirContext(env)
        val strArrAttributes = toStringAttributes(strAttributes, delimiter)
        var count = 0
        for (i in strArrAttributes.indices) {
            val attributesValues = getAttributesValues(strArrAttributes!![i], separator)
            if (attributesValues == null) count++ else count += attributesValues.size
        }
        val modItems: Array<ModificationItem?> = arrayOfNulls<ModificationItem?>(count)
        var basicAttr: BasicAttribute? = null
        var k = 0
        for (i in strArrAttributes.indices) {
            val attribute = strArrAttributes!![i]
            val type = getAttrValueType(attribute)
            val values = getAttributesValues(attribute, separator)
            if (modifytype == DirContext.REPLACE_ATTRIBUTE) {
                if (values == null) basicAttr = BasicAttribute(type) else basicAttr = BasicAttribute(type, values[0])
                modItems[k] = ModificationItem(modifytype, basicAttr)
                k++
                if (values != null && values.size > 1) {
                    for (j in 1 until values.size) {
                        basicAttr = BasicAttribute(type, values[j])
                        modItems[k] = ModificationItem(DirContext.ADD_ATTRIBUTE, basicAttr)
                        k++
                    }
                }
            } else {
                for (j in values.indices) {
                    if (type != null || modifytype == DirContext.ADD_ATTRIBUTE) basicAttr = BasicAttribute(type, values!![j]) else basicAttr = BasicAttribute(values!![j])
                    modItems[k] = ModificationItem(modifytype, basicAttr)
                    k++
                }
            }
        }
        context.modifyAttributes(dn, modItems)
        context.close()
    }

    /**
     * @param dn
     * @param strAttributes
     * @param scope
     * @param startrow
     * @param maxrows
     * @param timeout
     * @param sort
     * @param sortType
     * @param sortDirection
     * @param start
     * @param separator
     * @param filter
     * @return
     * @throws NamingException
     * @throws PageException
     * @throws IOException
     */
    @Throws(NamingException::class, PageException::class, IOException::class)
    fun query(strAttributes: String?, scope: Int, startrow: Int, maxrows: Int, timeout: Int, sort: Array<String?>?, sortType: Int, sortDirection: Int, start: String?, separator: String?,
              filter: String?): Query? {
        // strAttributes=strAttributes.trim();
        val attEQAsterix: Boolean = strAttributes.trim().equals("*")
        val attributes = if (attEQAsterix) arrayOf<String?>("name", "value") else toStringAttributes(strAttributes, ",")

        // Control
        val controls = SearchControls()
        controls.setReturningObjFlag(true)
        controls.setSearchScope(scope)
        if (!attEQAsterix) controls.setReturningAttributes(toStringAttributes(strAttributes, ","))
        if (maxrows > 0) controls.setCountLimit(startrow + maxrows + 1)
        if (timeout > 0) controls.setTimeLimit(timeout)
        val context = InitialLdapContext(env, null)

        // Search
        val qry: Query = QueryImpl(attributes, 0, "query")
        try {
            val results: NamingEnumeration = context.search(start, filter, controls)

            // Fill result
            var row = 1
            if (!attEQAsterix) {
                while (results.hasMoreElements()) {
                    val resultRow: SearchResult = results.next() as SearchResult
                    if (row++ < startrow) continue
                    val len: Int = qry.addRow()
                    val rowEnum: NamingEnumeration = resultRow.getAttributes().getAll()
                    val dn: String = resultRow.getNameInNamespace()
                    qry.setAtEL("dn", len, dn)
                    while (rowEnum.hasMore()) {
                        val attr: Attribute = rowEnum.next() as Attribute
                        val key: Collection.Key = KeyImpl.init(attr.getID())
                        val values: Enumeration = attr.getAll()
                        var value: Object?
                        var existing: String
                        var strValue: String
                        while (values.hasMoreElements()) {
                            value = values.nextElement()
                            strValue = Caster.toString(value, null)
                            existing = Caster.toString(qry.getAt(key, len, null), null)
                            if (!StringUtil.isEmpty(existing) && !StringUtil.isEmpty(strValue)) {
                                value = existing + separator + strValue
                            } else if (!StringUtil.isEmpty(existing)) value = existing
                            qry.setAtEL(key, len, value)
                        }
                    }
                    if (maxrows > 0 && len >= maxrows) break
                }
            } else {
                outer@ while (results.hasMoreElements()) {
                    val resultRow: SearchResult = results.next() as SearchResult
                    if (row++ < startrow) continue
                    val attributesRow: Attributes = resultRow.getAttributes()
                    val rowEnum: NamingEnumeration = attributesRow.getIDs()
                    while (rowEnum.hasMoreElements()) {
                        val len: Int = qry.addRow()
                        val name: String = Caster.toString(rowEnum.next())
                        var value: Object? = null
                        try {
                            value = attributesRow.get(name).get()
                        } catch (e: Exception) {
                        }
                        qry.setAtEL("name", len, name)
                        qry.setAtEL("value", len, value)
                        if (maxrows > 0 && len >= maxrows) break@outer
                    }
                    qry.setAtEL("name", qry.size(), "dn")
                }
            }
        } finally {
            context.close()
        }
        // Sort
        if (sort != null && sort.size > 0) {
            val order: Int = if (sortDirection == SORT_DIRECTION_ASC) Query.ORDER_ASC else Query.ORDER_DESC
            for (i in sort.indices.reversed()) {
                var item = sort[i]
                if (item.indexOf(' ') !== -1) item = ListUtil.first(item, " ", true)
                qry.sort(KeyImpl.getInstance(item), order)
                // keys[i] = new SortKey(item);
            }
        }
        return qry
    }

    private fun getAttrValueType(attribute: String?): String? {
        val eqIndex: Int = attribute.indexOf("=")
        return if (eqIndex != -1) attribute.substring(0, eqIndex).trim() else null
    }

    @Throws(PageException::class)
    private fun getAttributesValues(attribute: String?, separator: String?): Array<String?>? {
        val strValue: String = attribute.substring(attribute.indexOf("=") + 1)
        return if (strValue.length() === 0) null else ListUtil.toStringArray(ListUtil.listToArrayRemoveEmpty(strValue, if (separator!!.equals(", ")) "," else separator))
    }

    companion object {
        /**
         * Field `SECURE_NONE`
         */
        const val SECURE_NONE: Short = 0

        /**
         * Field `SECURE_CFSSL_BASIC`
         */
        const val SECURE_CFSSL_BASIC: Short = 1

        /**
         * Field `SECURE_CFSSL_CLIENT_AUTH`
         */
        const val SECURE_CFSSL_CLIENT_AUTH: Short = 2

        /**
         * Field `SORT_TYPE_CASE`
         */
        const val SORT_TYPE_CASE = 0

        /**
         * Field `SORT_TYPE_NOCASE`
         */
        const val SORT_TYPE_NOCASE = 1

        /**
         * Field `SORT_DIRECTION_ASC`
         */
        const val SORT_DIRECTION_ASC = 0

        /**
         * Field `SORT_DIRECTION_DESC`
         */
        const val SORT_DIRECTION_DESC = 1
        @Throws(PageException::class)
        private fun toStringAttributes(strAttributes: String?, delimiter: String?): Array<String?>? {
            return ListUtil.toStringArrayTrim(ListUtil.listToArrayRemoveEmpty(strAttributes, delimiter))
        }

        @Throws(PageException::class)
        private fun toAttributes(strAttributes: String?, delimiter: String?, separator: String?): Attributes? {
            val arrAttr = toStringAttributes(strAttributes, delimiter)
            val attributes = BasicAttributes()
            for (i in arrAttr.indices) {
                val strAttr = arrAttr!![i]

                // Type
                val eqIndex: Int = strAttr.indexOf('=')
                val attr: Attribute = BasicAttribute(if (eqIndex != -1) strAttr.substring(0, eqIndex).trim() else null)

                // Value
                val strValue = if (eqIndex != -1) strAttr.substring(eqIndex + 1) else strAttr
                val arrValue: Array<String?> = ListUtil.toStringArray(ListUtil.listToArrayRemoveEmpty(strValue, separator))

                // Fill
                for (y in arrValue.indices) {
                    attr.add(arrValue[y])
                }
                attributes.put(attr)
            }
            return attributes
        }
    }

    /**
     * constructor of the class
     *
     * @param server
     * @param port
     * @param binaryColumns
     */
    init {
        env.put("java.naming.factory.initial", "com.sun.jndi.ldap.LdapCtxFactory")
        env.put("java.naming.provider.url", "ldap://$server:$port")

        // rEAD AS bINARY
        for (i in binaryColumns.indices) env.put("java.naming.ldap.attributes.binary", binaryColumns!![i])

        // Referral
        env.put("java.naming.referral", "ignore")

        // timeout
        env.put("com.sun.jndi.ldap.read.timeout", String.valueOf(timeout))
    }
}