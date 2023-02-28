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

import java.util.Iterator

class Search : TagImpl() {
    /** Specifies the criteria type for the search.  */
    private var type: Short = SearchCollection.SEARCH_TYPE_SIMPLE

    /** Specifies the maximum number of entries for index queries. If omitted, all rows are returned.  */
    private var maxrows = -1

    /** Specifies the criteria for the search following the syntactic rules specified by type.  */
    private var criteria: String? = ""

    /** Specifies the first row number to be retrieved. Default is 1.  */
    private var startrow = 1

    /**
     * The logical collection name that is the target of the search operation or an external collection
     * with fully qualified path.
     */
    private var collections: Array<SearchCollection?>?

    /** A name for the search query.  */
    private var name: String? = null
    private var category = EMPTY
    private var categoryTree: String? = ""
    private var status: String? = null
    private var suggestions = SUGGESTIONS_NEVER
    private var contextPassages = 0
    private var contextBytes = 300
    private var contextHighlightBegin: String? = "<b>"
    private var contextHighlightEnd: String? = "</b>"
    private var previousCriteria: String? = null

    // private int spellCheckMaxLevel=10;
    // private String result=null;
    @Override
    fun release() {
        super.release()
        type = SearchCollection.SEARCH_TYPE_SIMPLE
        maxrows = -1
        criteria = ""
        startrow = 1
        collections = null
        category = EMPTY
        categoryTree = ""
        status = null
        suggestions = SUGGESTIONS_NEVER
        contextPassages = 0
        contextBytes = 300
        contextHighlightBegin = "<b>"
        contextHighlightEnd = "</b>"
        previousCriteria = null

        // spellCheckMaxLevel=10;
        // result=null;
    }

    /**
     * set the value type Specifies the criteria type for the search.
     *
     * @param type value to set
     * @throws ApplicationException
     */
    @Throws(ApplicationException::class)
    fun setType(type: String?) {
        var type = type ?: return
        type = type.toLowerCase().trim()
        if (type.equals("simple")) this.type = SearchCollection.SEARCH_TYPE_SIMPLE else if (type.equals("explicit")) this.type = SearchCollection.SEARCH_TYPE_EXPLICIT else throw ApplicationException("attribute type of tag search has an invalid value, valid values are [simple,explicit] now is [$type]")
    }

    /**
     * set the value maxrows Specifies the maximum number of entries for index queries. If omitted, all
     * rows are returned.
     *
     * @param maxrows value to set
     */
    fun setMaxrows(maxrows: Double) {
        this.maxrows = maxrows.toInt()
    }

    /**
     * set the value criteria Specifies the criteria for the search following the syntactic rules
     * specified by type.
     *
     * @param criteria value to set
     */
    fun setCriteria(criteria: String?) {
        this.criteria = criteria
    }

    /**
     * set the value startrow Specifies the first row number to be retrieved. Default is 1.
     *
     * @param startrow value to set
     */
    fun setStartrow(startrow: Double) {
        this.startrow = startrow.toInt()
    }

    /**
     * set the value collection The logical collection name that is the target of the search operation
     * or an external collection with fully qualified path.
     *
     * @param collection value to set
     * @throws PageException
     */
    @Throws(PageException::class)
    fun setCollection(collection: String?) {
        val collNames: Array<String?> = ListUtil.toStringArrayTrim(ListUtil.listToArrayRemoveEmpty(collection, ','))
        collections = arrayOfNulls<SearchCollection?>(collNames.size)
        val se: SearchEngine = pageContext.getConfig().getSearchEngine(pageContext)
        try {
            for (i in collections.indices) {
                collections!![i] = se.getCollectionByName(collNames[i])
            }
        } catch (e: SearchException) {
            collections = null
            throw Caster.toPageException(e)
        }
    }

    /**
     * set the value language
     *
     * @param language value to set
     */
    fun setLanguage(language: String?) {
        // DeprecatedUtil.tagAttribute(pageContext,"Search", "language");
    }

    /**
     * set the value external
     *
     * @param external value to set
     * @throws ApplicationException
     */
    @Throws(ApplicationException::class)
    fun setExternal(external: Boolean) {
        // DeprecatedUtil.tagAttribute(pageContext,"Search", "external");
    }

    /**
     * set the value name A name for the search query.
     *
     * @param name value to set
     */
    fun setName(name: String?) {
        this.name = name
    }

    /**
     * @param category the category to set
     * @throws ApplicationException
     */
    fun setCategory(listCategories: String?) {
        if (StringUtil.isEmpty(listCategories)) return
        category = ListUtil.trimItems(ListUtil.listToStringArray(listCategories, ','))
    }

    /**
     * @param categoryTree the categoryTree to set
     * @throws ApplicationException
     */
    fun setCategorytree(categoryTree: String?) {
        var categoryTree = categoryTree
        if (StringUtil.isEmpty(categoryTree)) return
        categoryTree = categoryTree.replace('\\', '/').trim()
        if (StringUtil.startsWith(categoryTree, '/')) categoryTree = categoryTree.substring(1)
        if (!StringUtil.endsWith(categoryTree, '/') && categoryTree.length() > 0) categoryTree += "/"
        this.categoryTree = categoryTree
    }

    /**
     * @param contextBytes the contextBytes to set
     * @throws ApplicationException
     */
    @Throws(ApplicationException::class)
    fun setContextbytes(contextBytes: Double) {
        this.contextBytes = contextBytes.toInt()
    }

    /**
     * @param contextHighlightBegin the contextHighlightBegin to set
     * @throws ApplicationException
     */
    fun setContexthighlightbegin(contextHighlightBegin: String?) {
        this.contextHighlightBegin = contextHighlightBegin
    }

    /**
     * @param contextHighlightEnd the contextHighlightEnd to set
     * @throws ApplicationException
     */
    fun setContexthighlightend(contextHighlightEnd: String?) {
        this.contextHighlightEnd = contextHighlightEnd
    }

    /**
     * @param contextPassages the contextPassages to set
     * @throws ApplicationException
     */
    @Throws(ApplicationException::class)
    fun setContextpassages(contextPassages: Double) {
        this.contextPassages = contextPassages.toInt()
    }

    /**
     * @param previousCriteria the previousCriteria to set
     * @throws ApplicationException
     */
    @Throws(ApplicationException::class)
    fun setPreviouscriteria(previousCriteria: String?) {
        this.previousCriteria = previousCriteria
        throw ApplicationException("attribute previousCriteria for tag search is not supported yet")
        // TODO impl tag attribute
    }

    /**
     * @param status the status to set
     * @throws ApplicationException
     */
    fun setStatus(status: String?) {
        if (!StringUtil.isEmpty(status)) this.status = status
    }

    /**
     * @param suggestions the suggestions to set
     * @throws ApplicationException
     */
    @Throws(PageException::class)
    fun setSuggestions(suggestions: String?) {
        var suggestions = suggestions
        if (StringUtil.isEmpty(suggestions)) return
        suggestions = suggestions.trim().toLowerCase()
        if ("always".equals(suggestions)) this.suggestions = SUGGESTIONS_ALWAYS else if ("never".equals(suggestions)) this.suggestions = SUGGESTIONS_NEVER else if (Decision.isNumber(suggestions)) {
            this.suggestions = Caster.toIntValue(suggestions)
        } else throw ApplicationException("attribute suggestions has an invalid value [$suggestions], valid values are [always,never,<positive numeric value>]")
    }

    @Override
    @Throws(PageException::class)
    fun doStartTag(): Int {
        // SerialNumber sn = pageContext.getConfig().getSerialNumber();
        // if(sn.getVersion()==SerialNumber.VERSION_COMMUNITY)
        // throw new SecurityException("no access to this functionality with the "+sn.getStringVersion()+"
        // version of Lucee");
        val v = "VARCHAR"
        val d = "DOUBLE"
        val cols = arrayOf<String?>("title", "url", "summary", "score", "recordssearched", "key", "custom1", "custom2", "custom3", "custom4", "categoryTree", "category",
                "context", "size", "rank", "author", "type", "collection")

        // TODO support context
        val types = arrayOf<String?>(v, v, v, d, d, v, v, v, v, v, v, v, v, d, d, v, v, v)
        val data: SearchData = pageContext.getConfig().getSearchEngine(pageContext).createSearchData(suggestions)
        var item: SuggestionItem? = null // this is already here to make sure the classloader load this sinstance
        val qry: lucee.runtime.type.Query = QueryImpl(cols, types, 0, "query")
        var collection: SearchCollection?
        var time: Long = System.currentTimeMillis()
        AddionalAttrs.setAddionalAttrs(contextBytes, contextPassages, contextHighlightBegin, contextHighlightEnd)
        try {
            for (i in collections.indices) {
                collection = collections!![i]
                startrow = collection.search(data, qry, criteria, collection.getLanguage(), type, startrow, maxrows, categoryTree, category)
                if (maxrows >= 0 && qry.getRecordcount() >= maxrows) break
            }
            pageContext.setVariable(name, qry)
        } catch (se: SearchException) {
            throw Caster.toPageException(se)
        } finally {
            AddionalAttrs.removeAddionalAttrs()
        }
        time = System.currentTimeMillis() - time
        val recSearched: Double = Double.valueOf(data.getRecordsSearched())
        val len: Int = qry.getRecordcount()
        for (i in 1..len) {
            qry.setAt("recordssearched", i, recSearched)
        }

        // status
        if (status != null) {
            val sct: Struct = StructImpl()
            pageContext.setVariable(status, sct)
            sct.set(FOUND, Double.valueOf(qry.getRecordcount()))
            sct.set(SEARCHED, recSearched)
            sct.set(KeyConstants._time, Double.valueOf(time))

            // TODO impl this values
            val s: Map = data.getSuggestion()
            if (s.size() > 0) {
                var key: String
                val it: Iterator = s.keySet().iterator()
                val keywords: Struct = StructImpl()
                val keywordScore: Struct = StructImpl()
                sct.set(KEYWORDS, keywords)
                sct.set(KEYWORD_SCORE, keywordScore)
                var obj: Object
                while (it.hasNext()) {
                    key = it.next()

                    // the problem is a conflict between the SuggestionItem version from core and extension
                    obj = s.get(key)
                    if (obj is SuggestionItem) {
                        item = obj as SuggestionItem
                        keywords.set(key, item.getKeywords())
                        keywordScore.set(key, item.getKeywordScore())
                    } else {
                        val clazz: Class = obj.getClass()
                        try {
                            keywords.set(key, clazz.getMethod("getKeywords", arrayOfNulls<Class?>(0)).invoke(obj, arrayOfNulls<Object?>(0)))
                            keywordScore.set(key, clazz.getMethod("getKeywordScore", arrayOfNulls<Class?>(0)).invoke(obj, arrayOfNulls<Object?>(0)))
                        } catch (e: Exception) {
                        }
                    }
                }
                val query: String = data.getSuggestionQuery()
                if (query != null) {
                    var html: String = StringUtil.replace(query, "<suggestion>", "<b>", false)
                    html = StringUtil.replace(html, "</suggestion>", "</b>", false)
                    sct.set("suggestedQueryHTML", html)
                    var plain: String = StringUtil.replace(query, "<suggestion>", "", false)
                    plain = StringUtil.replace(plain, "</suggestion>", "", false)
                    sct.set("suggestedQuery", plain)
                }
            }

            // if(suggestions!=SUGGESTIONS_NEVER)sct.set("suggestedQuery", "");
            // sct.set("keywords", "");
            // sct.set("keywordScore", "");
        }
        return SKIP_BODY
    }

    @Override
    fun doEndTag(): Int {
        return EVAL_PAGE
    }

    companion object {
        private val EMPTY: Array<String?>? = arrayOfNulls<String?>(0)
        private val SUGGESTIONS_ALWAYS: Int = Integer.MAX_VALUE
        private const val SUGGESTIONS_NEVER = -1
        private val FOUND: lucee.runtime.type.Collection.Key? = KeyImpl.getInstance("found")
        private val SEARCHED: lucee.runtime.type.Collection.Key? = KeyImpl.getInstance("searched")
        private val KEYWORDS: lucee.runtime.type.Collection.Key? = KeyImpl.getInstance("keywords")
        private val KEYWORD_SCORE: lucee.runtime.type.Collection.Key? = KeyImpl.getInstance("keywordScore")
    }
}