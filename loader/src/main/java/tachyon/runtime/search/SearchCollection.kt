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
package tachyon.runtime.search

import java.io.Serializable

/**
 * a Search Collection
 */
interface SearchCollection : Serializable {
    /**
     * create a collection
     *
     * @throws SearchException Search Exception
     */
    @Throws(SearchException::class)
    fun create()

    /**
     * optimize a Collection
     *
     * @throws SearchException Search Exception
     */
    @Throws(SearchException::class)
    fun optimize()

    /**
     * map a Collection
     *
     * @param path path
     * @throws SearchException Search Exception
     */
    @Throws(SearchException::class)
    fun map(path: Resource?)

    /**
     * repair a Collection
     *
     * @throws SearchException Search Exception
     */
    @Throws(SearchException::class)
    fun repair()

    /**
     * updates an index of a collection
     *
     * @param pc Page Context
     * @param key Key
     * @param type Type
     * @param urlpath Query Name
     * @param title title
     * @param body body
     * @param language language
     * @param extensions extensions
     * @param query query
     * @param recurse recure
     * @param categoryTree category tree
     * @param categories categories
     * @param timeout timeout
     * @param custom1 custom1
     * @param custom2 custom2
     * @param custom3 custom3
     * @param custom4 custom4
     * @return Index Result
     * @throws PageException Page Exception
     * @throws MalformedURLException Malformed URL Exception
     * @throws SearchException Search Exception
     */
    @Throws(PageException::class, MalformedURLException::class, SearchException::class)
    fun index(pc: PageContext?, key: String?, type: Short, urlpath: String?, title: String?, body: String?, language: String?, extensions: Array<String?>?, query: String?,
              recurse: Boolean, categoryTree: String?, categories: Array<String?>?, timeout: Long, custom1: String?, custom2: String?, custom3: String?, custom4: String?): IndexResult?

    /**
     * updates a collection with a file
     *
     * @param id id
     * @param title Title
     * @param file file
     * @param language language
     * @return Index Result
     * @throws SearchException Search Exception
     */
    @Throws(SearchException::class)
    fun indexFile(id: String?, title: String?, file: Resource?, language: String?): IndexResult?

    /**
     * updates a collection with a path
     *
     * @param id id
     * @param title Title
     * @param dir Directory
     * @param recurse recurse
     * @param extensions extensions
     * @param language language
     * @return Index Result
     * @throws SearchException Search Exception
     */
    @Throws(SearchException::class)
    fun indexPath(id: String?, title: String?, dir: Resource?, extensions: Array<String?>?, recurse: Boolean, language: String?): IndexResult?

    /**
     * updates a collection with an url
     *
     * @param id id
     * @param title Title
     * @param recurse Recurse
     * @param extensions extensions
     * @param url url
     * @param language language
     * @param timeout timeout
     * @return Index Result
     * @throws SearchException Search Exception
     */
    @Throws(SearchException::class)
    fun indexURL(id: String?, title: String?, url: URL?, extensions: Array<String?>?, recurse: Boolean, language: String?, timeout: Long): IndexResult?

    /**
     * updates a collection with a custom
     *
     * @param id id
     * @param title Title for the Index
     * @param keyColumn Key Column
     * @param bodyColumns Body Column Array
     * @param language Language for index
     * @param custom1 custom1
     * @param custom2 custom2
     * @param custom3 custom3
     * @param custom4 custom4
     * @return Index Result
     * @throws SearchException Search Exception
     */
    @Throws(SearchException::class)
    fun indexCustom(id: String?, title: QueryColumn?, keyColumn: QueryColumn?, bodyColumns: Array<QueryColumn?>?, language: String?, custom1: QueryColumn?,
                    custom2: QueryColumn?, custom3: QueryColumn?, custom4: QueryColumn?): IndexResult?
    /**
     * updates a collection with a custom
     *
     * @param id id
     * @param title Title for the Index
     * @param keyColumn Key Column
     * @param bodyColumns Body Column Array
     * @param language Language for index
     * @param urlpath Url Path
     * @param custom1 custom1
     * @param custom2 custom2
     * @param custom3 custom3
     * @param custom4 custom4
     * @throws SearchException FUTURE add public abstract IndexResult indexCustom(String id, QueryColumn
     * title, QueryColumn keyColumn, QueryColumn[] bodyColumns, String language,QueryColumn
     * urlpath, QueryColumn custom1, QueryColumn custom2, QueryColumn custom3,QueryColumn
     * custom4) throws SearchException;
     */
    /**
     * @return Returns the language.
     */
    fun getLanguage(): String?

    /**
     * purge a collection
     *
     * @return Index Result
     * @throws SearchException Search Exception
     */
    @Throws(SearchException::class)
    fun purge(): IndexResult?

    /**
     * delete the collection
     *
     * @return Index Result
     * @throws SearchException Search Exception
     */
    @Throws(SearchException::class)
    fun delete(): IndexResult?

    /**
     * delete an Index from collection
     *
     * @param pc Page Context
     * @param key Key
     * @param type Type
     * @param queryName Query Name
     * @return Index Result
     * @throws SearchException Search Exception
     */
    @Throws(SearchException::class)
    fun deleteIndex(pc: PageContext?, key: String?, type: Short, queryName: String?): IndexResult?

    /**
     * @return Returns the path.
     */
    fun getPath(): Resource?

    /**
     * @return returns when collection is created
     */
    fun getCreated(): DateTime?

    /**
     * @return Returns the lastUpdate.
     */
    fun getLastUpdate(): DateTime?

    /**
     * @return Returns the name.
     */
    fun getName(): String?

    /**
     * @return Returns the logFile.
     */
    fun getLogger(): Log?

    /**
     * @return Returns the searchEngine.
     */
    fun getSearchEngine(): SearchEngine?

    /**
     * return time when collection was created
     *
     * @return create time
     */
    fun created(): Object?

    /**
     * search the collection
     *
     * @param data data
     * @param qry Query to append results
     * @param criteria Search Criteria
     * @param language Language
     * @param type SEARCH_TYPE_EXPLICIT or SEARCH_TYPE_SIMPLE
     * @param startrow start row
     * @param maxrow max rows
     * @param categoryTree catgeory Tree
     * @param category catgeory
     * @return new startrow
     * @throws SearchException Search Exception
     * @throws PageException Page Exception
     */
    @Throws(SearchException::class, PageException::class)
    fun search(data: SearchData?, qry: Query?, criteria: String?, language: String?, type: Short, startrow: Int, maxrow: Int, categoryTree: String?, category: Array<String?>?): Int

    /**
     * search the collection
     *
     * @param data data
     * @param criteria Search Criteria
     * @param language Language
     * @param type SEARCH_TYPE_EXPLICIT or SEARCH_TYPE_SIMPLE
     * @param categoryTree catgeory Tree
     * @param category catgeory
     * @return Result as SearchRecord Array
     * @throws SearchException Search Exception
     */
    @Throws(SearchException::class)
    fun _search(data: SearchData?, criteria: String?, language: String?, type: Short, categoryTree: String?, category: Array<String?>?): Array<SearchResulItem?>?

    /**
     * @return the size of the collection in KB
     */
    fun getSize(): Long

    /**
     * @return the counts of the documents in the collection
     */
    fun getDocumentCount(): Int
    fun getDocumentCount(id: String?): Int
    fun getCategoryInfo(): Object?
    fun getIndexesAsQuery(): Query?
    fun addIndex(si: SearchIndex?)

    companion object {
        /**
         * Field `SEARCH_TYPE_SIMPLE`
         */
        const val SEARCH_TYPE_SIMPLE: Short = 0

        /**
         * Field `SEARCH_TYPE_EXPLICIT`
         */
        const val SEARCH_TYPE_EXPLICIT: Short = 1
    }
}