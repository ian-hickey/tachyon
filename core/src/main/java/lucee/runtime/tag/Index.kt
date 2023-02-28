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
package lucee.runtime.tag

import java.io.IOException

/**
 * Populates collections with indexed data.
 */
class Index : TagImpl() {
    /** Specifies the index action.  */
    private var action: String? = null

    /**
     * Specifies the URL path for files if type = "file" and type = "path". When the collection is
     * searched with cfsearch, the pathname is automatically be prepended to filenames and returned as
     * the url attribute.
     */
    private var urlpath: String? = null

    /** Specifies the type of entity being indexed. Default is CUSTOM.  */
    private var type: Short = -1

    /**
     * Title for collection; Query column name for type and a valid query name; Permits searching
     * collections by title or displaying a separate title from the key
     */
    private var title: String? = null
    private var language: String? = null

    /**
     * Specifies the comma-separated list of file extensions that CFML uses to index files if type =
     * "Path". Default is HTM, HTML, CFM, CFML, DBM, DBML. An entry of "*." returns files with no
     * extension
     */
    private var extensions = EXTENSIONS

    /**   */
    private var key: String? = null

    /**
     * A custom field you can use to store data during an indexing operation. Specify a query column
     * name for type and a query name.
     */
    private var custom1: String? = null
    private var timeout: Long = 10000

    /**
     * A custom field you can use to store data during an indexing operation. Usage is the same as for
     * custom1.
     */
    private var custom2: String? = null

    /**
     * A custom field you can use to store data during an indexing operation. Usage is the same as for
     * custom1.
     */
    private var custom3: String? = null

    /**
     * A custom field you can use to store data during an indexing operation. Usage is the same as for
     * custom1.
     */
    private var custom4: String? = null

    /** Specifies the name of the query against which the collection is generated.  */
    private var query: String? = null

    /**
     * Specifies a collection name. If you are indexing an external collection external = "Yes", specify
     * the collection name, including fully qualified path.
     */
    private var collection: SearchCollection? = null

    /**
     * Yes or No. Yes specifies, if type = "Path", that directories below the path specified in key are
     * included in the indexing operation.
     */
    private var recurse = false

    /**   */
    private var body: String? = null
    private var name: String? = null
    private var category = EMPTY
    private var categoryTree: String? = ""
    private var status: String? = null
    private var prefix: String? = null
    private var throwontimeout = false
    @Override
    fun release() {
        super.release()
        action = null
        urlpath = null
        type = -1
        title = null
        language = null
        extensions = EXTENSIONS
        key = null
        custom1 = null
        custom2 = null
        custom3 = null
        custom4 = null
        query = null
        collection = null
        recurse = false
        body = null
        name = null
        category = EMPTY
        categoryTree = ""
        status = null
        prefix = null
        timeout = 10000
        throwontimeout = false
    }

    /**
     * set the value action Specifies the index action.
     *
     * @param action value to set
     */
    fun setAction(action: String?) {
        this.action = action.toLowerCase().trim()
    }

    /**
     * set the value urlpath Specifies the URL path for files if type = "file" and type = "path". When
     * the collection is searched with cfsearch, the pathname is automatically be prepended to filenames
     * and returned as the url attribute.
     *
     * @param urlpath value to set
     */
    fun setUrlpath(urlpath: String?) {
        if (StringUtil.isEmpty(urlpath)) return
        this.urlpath = urlpath.toLowerCase().trim()
    }

    /**
     * set the value type Specifies the type of entity being indexed. Default is CUSTOM.
     *
     * @param type value to set
     * @throws PageException
     */
    @Throws(PageException::class)
    fun setType(type: String?) {
        if (type == null) return
        try {
            this.type = toType(type)
        } catch (e: SearchException) {
            throw Caster.toPageException(e)
        }
    }

    /**
     * @param timeout the timeout in seconds
     * @throws ApplicationException
     */
    @Throws(ApplicationException::class)
    fun setTimeout(timeout: Double) {
        var timeout = timeout
        this.timeout = (timeout * 1000.0).toLong()
        if (this.timeout < 0) throw ApplicationException("attribute timeout must contain a positive number")
        if (timeout == 0.0) timeout = 1.0
    }

    /**
     * set the value throwontimeout Yes or No. Specifies how timeout conditions are handled. If the
     * value is Yes, an exception is generated to provide notification of the timeout. If the value is
     * No, execution continues. Default is Yes.
     *
     * @param throwontimeout value to set
     */
    fun setThrowontimeout(throwontimeout: Boolean) {
        this.throwontimeout = throwontimeout
    }

    fun setName(name: String?) {
        this.name = name
    }

    /**
     * set the value title Title for collection; Query column name for type and a valid query name;
     * Permits searching collections by title or displaying a separate title from the key
     *
     * @param title value to set
     */
    fun setTitle(title: String?) {
        this.title = title
    }

    /**
     * set the value custom1 A custom field you can use to store data during an indexing operation.
     * Specify a query column name for type and a query name.
     *
     * @param custom1 value to set
     */
    fun setCustom1(custom1: String?) {
        this.custom1 = custom1
    }

    /**
     * set the value language
     *
     * @param language value to set
     */
    fun setLanguage(language: String?) {
        if (StringUtil.isEmpty(language)) return
        this.language = Collection.validateLanguage(language)
    }

    /**
     * set the value external
     *
     * @param external value to set
     * @throws ApplicationException
     */
    fun setExternal(external: Boolean) {
        // DeprecatedUtil.tagAttribute(pageContext,"Index", "external");
    }

    /**
     * set the value extensions
     *
     * @param extensions value to set
     * @throws PageException
     */
    @Throws(PageException::class)
    fun setExtensions(extensions: String?) {
        if (extensions == null) return
        this.extensions = ListUtil.toStringArrayTrim(ListUtil.listToArray(extensions, ','))
    }

    /**
     * set the value key
     *
     * @param key value to set
     */
    fun setKey(key: String?) {
        this.key = key
    }

    /**
     * set the value custom2 A custom field you can use to store data during an indexing operation.
     * Usage is the same as for custom1.
     *
     * @param custom2 value to set
     */
    fun setCustom2(custom2: String?) {
        this.custom2 = custom2
    }

    /**
     * @param custom3 The custom3 to set.
     */
    fun setCustom3(custom3: String?) {
        this.custom3 = custom3
    }

    /**
     * @param custom4 The custom4 to set.
     */
    fun setCustom4(custom4: String?) {
        this.custom4 = custom4
    }

    /**
     * set the value query Specifies the name of the query against which the collection is generated.
     *
     * @param query value to set
     */
    fun setQuery(query: String?) {
        this.query = query
    }

    /**
     * set the value collection Specifies a collection name. If you are indexing an external collection
     * external = "Yes", specify the collection name, including fully qualified path.
     *
     * @param collection value to set
     * @throws PageException
     */
    @Throws(PageException::class)
    fun setCollection(collection: String?) {
        try {
            this.collection = pageContext.getConfig().getSearchEngine(pageContext).getCollectionByName(collection.toLowerCase().trim())
        } catch (e: SearchException) {
            throw Caster.toPageException(e)
        }
    }

    /**
     * set the value recurse Yes or No. Yes specifies, if type = "Path", that directories below the path
     * specified in key are included in the indexing operation.
     *
     * @param recurse value to set
     */
    fun setRecurse(recurse: Boolean) {
        this.recurse = recurse
    }

    /**
     * set the value body
     *
     * @param body value to set
     */
    fun setBody(body: String?) {
        this.body = body
    }

    /**
     * @param category the category to set
     * @throws ApplicationException
     */
    fun setCategory(listCategories: String?) {
        if (listCategories == null) return
        category = ListUtil.trimItems(ListUtil.listToStringArray(listCategories, ','))
    }

    /**
     * @param categoryTree the categoryTree to set
     * @throws ApplicationException
     */
    fun setCategorytree(categoryTree: String?) {
        var categoryTree = categoryTree ?: return
        categoryTree = categoryTree.replace('\\', '/').trim()
        if (StringUtil.startsWith(categoryTree, '/')) categoryTree = categoryTree.substring(1)
        if (!StringUtil.endsWith(categoryTree, '/') && categoryTree.length() > 0) categoryTree += "/"
        this.categoryTree = categoryTree
    }

    /**
     * @param prefix the prefix to set
     */
    fun setPrefix(prefix: String?) {
        this.prefix = prefix
    }

    /**
     * @param status the status to set
     * @throws ApplicationException
     */
    fun setStatus(status: String?) {
        this.status = status
    }

    @Override
    @Throws(PageException::class)
    fun doStartTag(): Int {
        // SerialNumber sn = pageContext.getConfig().getSerialNumber();
        // if(sn.getVersion()==SerialNumber.VERSION_COMMUNITY)
        // throw new SecurityException("no access to this functionality with the "+sn.getStringVersion()+"
        // version of Lucee");
        try {
            if (action!!.equals("purge")) doPurge() else if (action!!.equals("update")) doUpdate() else if (action!!.equals("delete")) doDelete() else if (action!!.equals("refresh")) doRefresh() else if (action!!.equals("list")) doList() else throw ApplicationException("invalid action name [$action]", "valid action names are [list,update, delete, purge, refresh]")
        } catch (e: Exception) {
            throw Caster.toPageException(e)
        }
        return SKIP_BODY
    }

    /**
     * @throws PageException
     * @throws SearchException
     * @throws IOException
     */
    @Throws(PageException::class, SearchException::class, IOException::class)
    private fun doRefresh() {
        doPurge()
        doUpdate()
    }

    @Throws(ApplicationException::class, PageException::class)
    private fun doList() {
        required("index", action, "name", name)
        pageContext.setVariable(name, collection.getIndexesAsQuery())
    }

    /**
     * delete a collection
     *
     * @throws PageException
     * @throws SearchException
     */
    @Throws(PageException::class, SearchException::class)
    private fun doDelete() {
        required("index", action, "collection", collection)
        if (type != SearchIndex.TYPE_CUSTOM) required("index", action, "key", key)

        // no type defined
        if (type.toInt() == -1) {
            if (query != null) {
                type = SearchIndex.TYPE_CUSTOM
            } else {
                var file: Resource? = null
                try {
                    file = ResourceUtil.toResourceExisting(pageContext, key)
                    pageContext.getConfig().getSecurityManager().checkFileLocation(file)
                } catch (e: ExpressionException) {
                }
                if (file != null && file.exists() && file.isFile()) type = SearchIndex.TYPE_FILE else if (file != null && file.exists() && file.isDirectory()) type = SearchIndex.TYPE_PATH else {
                    try {
                        URL(key)
                        type = SearchIndex.TYPE_URL
                    } catch (e: MalformedURLException) {
                    }
                }
            }
        }
        collection.deleteIndex(pageContext, key, type, query)
    }

    /**
     * purge a collection
     *
     * @throws PageException
     * @throws SearchException
     */
    @Throws(PageException::class, SearchException::class)
    private fun doPurge() {
        required("index", action, "collection", collection)
        collection.purge()
    }

    /**
     * update a collection
     *
     * @throws PageException
     * @throws SearchException
     * @throws IOException
     */
    @Throws(PageException::class, SearchException::class, IOException::class)
    private fun doUpdate() {
        // check attributes
        required("index", action, "collection", collection)
        required("index", action, "key", key)
        if (type.toInt() == -1) type = if (query == null) SearchIndex.TYPE_FILE else SearchIndex.TYPE_CUSTOM
        if (type == SearchIndex.TYPE_CUSTOM) {
            required("index", action, "body", body)
            // required("index",action,"query",query);
        }
        val result: IndexResult
        result = collection.index(pageContext, key, type, urlpath, title, body, language, extensions, query, recurse, categoryTree, category, timeout, custom1, custom2, custom3,
                custom4)
        if (!StringUtil.isEmpty(status)) pageContext.setVariable(status, toStruct(result))
    }

    @Override
    fun doEndTag(): Int {
        return EVAL_PAGE
    }

    private fun toStruct(result: IndexResult?): Struct? {
        val sct: Struct = StructImpl()
        sct.setEL("deleted", Double.valueOf(result.getCountDeleted()))
        sct.setEL("inserted", Double.valueOf(result.getCountInserted()))
        sct.setEL("updated", Double.valueOf(result.getCountUpdated()))
        return sct
    }

    companion object {
        private val EMPTY: Array<String?>? = arrayOfNulls<String?>(0)
        var EXTENSIONS: Array<String?>? = ArrayUtil.toArray(Constants.getTemplateExtensions(), "htm", "html", "dbm", "dbml")
        @Throws(SearchException::class)
        fun toType(type: String?): Short {
            var type = type
            type = type.toLowerCase().trim()
            return if (type.equals("custom")) SearchIndex.TYPE_CUSTOM else if (type.equals("query")) SearchIndex.TYPE_CUSTOM else if (type.equals("file")) SearchIndex.TYPE_FILE else if (type.equals("path")) SearchIndex.TYPE_PATH else if (type.equals("url")) SearchIndex.TYPE_URL else throw SearchException("invalid value for attribute type [$type]")
        }
    }
}