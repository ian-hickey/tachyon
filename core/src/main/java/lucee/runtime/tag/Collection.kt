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
package lucee.runtime.tag

import lucee.commons.io.res.Resource

/**
 * Allows you to create and administer Collections.
 */
class Collection : TagImpl() {
    /** Specifies the action to perform.  */
    private var action: String? = "list"

    /**   */
    private var path: Resource? = null

    /** Specifies a collection name or an alias if action = "map"  */
    private var collection: String? = null

    /** Name of the output variable (action=list)  */
    private var name: String? = null

    /** language of the collection (operators,stopwords)  */
    private var language: String? = "english"

    // private boolean categories=false;
    @Override
    fun release() {
        super.release()
        action = "list"
        path = null
        collection = null
        name = null
        language = "english"
        // categories=false;
    }

    /**
     * @param categories the categories to set
     * @throws ApplicationException
     */
    fun setCategories(categories: Boolean) {
        // Lucee always support categories
        // this.categories = categories;
    }

    /**
     * set the value action Specifies the action to perform.
     *
     * @param action value to set
     */
    fun setAction(action: String?) {
        if (action == null) return
        this.action = action.toLowerCase().trim()
    }

    fun setEngine(engine: String?) {
        // This setter only exists for compatibility reasons to other CFML engines, the attribute is
        // completely ignored.
    }

    /**
     * set the value path
     *
     * @param path value to set
     * @throws PageException
     */
    @Throws(PageException::class)
    fun setPath(strPath: String?) {
        if (strPath == null) return
        path = ResourceUtil.toResourceNotExisting(pageContext, strPath.trim())
        pageContext.getConfig().getSecurityManager().checkFileLocation(path)
        if (!path.exists()) {
            val parent: Resource = path.getParentResource()
            if (parent != null && parent.exists()) path.mkdirs() else {
                throw ApplicationException("Attribute [path] of the tag [collection] must be an existing directory")
            }
        } else if (!path.isDirectory()) throw ApplicationException("Attribute [path] of the tag [collection] must be an existing directory")
    }

    /**
     * set the value collection Specifies a collection name or an alias if action = "map"
     *
     * @param collection value to set
     */
    fun setCollection(collection: String?) {
        if (collection == null) return
        this.collection = collection.toLowerCase().trim()
    }

    /**
     * set the value name
     *
     * @param name value to set
     */
    fun setName(name: String?) {
        if (name == null) return
        this.name = name.toLowerCase().trim()
    }

    /**
     * set the value language
     *
     * @param language value to set
     */
    fun setLanguage(language: String?) {
        if (language == null) return
        this.language = validateLanguage(language)
    }

    @Override
    @Throws(PageException::class)
    fun doStartTag(): Int {
        // SerialNumber sn = pageContext.getConfig().getSerialNumber();
        // if(sn.getVersion()==SerialNumber.VERSION_COMMUNITY)
        // throw new SecurityException("no access to this functionality with the "+sn.getStringVersion()+"
        // version of Lucee");
        try {
            if (action!!.equals("create")) doCreate() else if (action!!.equals("repair")) doRepair() else if (action!!.equals("delete")) doDelete() else if (action!!.equals("optimize")) doOptimize() else if (action!!.equals("list")) doList() else if (action!!.equals("map")) doMap() else if (action!!.equals("categorylist")) doCategoryList() else throw ApplicationException("Invalid value [$action] for attribute [action].", "allowed values are [create, repair, map, delete, optimize, list]")
        } catch (e: SearchException) {
            throw Caster.toPageException(e)
        }
        return SKIP_BODY
    }

    /**
     * @throws SearchException
     * @throws PageException
     */
    @Throws(SearchException::class, PageException::class)
    private fun doMap() {
        required("collection", action, "collection", collection)
        required("collection", action, "path", path)
        getCollection().map(path)
    }

    /**
     * Creates a query in the PageContext containing all available Collections of the current
     * searchStorage
     *
     * @throws ApplicationException
     * @throws PageException
     * @throws SearchException
     */
    @Throws(PageException::class, SearchException::class)
    private fun doList() {
        required("collection", action, "name", name)
        // if(StringUtil.isEmpty(name))throw new ApplicationException("for action list attribute name is
        // required");
        pageContext.setVariable(name, searchEngine.getCollectionsAsQuery())
    }

    @Throws(PageException::class, SearchException::class)
    private fun doCategoryList() {
        // check attributes
        required("collection", action, "collection", collection)
        required("collection", action, "name", name)
        pageContext.setVariable(name, getCollection().getCategoryInfo())
    }

    /**
     * Optimizes the Collection
     *
     * @throws SearchException
     * @throws PageException
     */
    @Throws(SearchException::class, PageException::class)
    private fun doOptimize() {
        required("collection", action, "collection", collection)
        getCollection().optimize()
    }

    /**
     * Deletes a Collection
     *
     * @throws SearchException
     * @throws PageException
     */
    @Throws(SearchException::class, PageException::class)
    private fun doDelete() {
        required("collection", action, "collection", collection)
        getCollection().delete()
    }

    /**
     *
     * @throws SearchException
     * @throws PageException
     */
    @Throws(SearchException::class, PageException::class)
    private fun doRepair() {
        required("collection", action, "collection", collection)
        getCollection().repair()
    }

    /**
     * Creates a new collection
     *
     * @throws SearchException
     * @throws PageException
     */
    @Throws(SearchException::class, PageException::class)
    private fun doCreate() {
        required("collection", action, "collection", collection)
        required("collection", action, "path", path)
        searchEngine.createCollection(collection, path, language, SearchEngine.DENY_OVERWRITE)
    }

    /**
     * Returns the Searchstorage defined in the Environment
     *
     * @return searchStorage
     * @throws PageException
     */
    @get:Throws(PageException::class)
    private val searchEngine: SearchEngine?
        private get() = pageContext.getConfig().getSearchEngine(pageContext)

    /**
     * the collection matching the collection name
     *
     * @return collection
     * @throws SearchException
     * @throws PageException
     */
    @Throws(SearchException::class, PageException::class)
    private fun getCollection(): SearchCollection? {
        return searchEngine.getCollectionByName(collection)
    }

    @Override
    fun doEndTag(): Int {
        return EVAL_PAGE
    }

    companion object {
        fun validateLanguage(language: String?): String? {
            var language = language
            if (StringUtil.isEmpty(language, true)) return "english"
            language = language.toLowerCase().trim()
            return if ("standard".equals(language)) "english" else language
        }
    }
}