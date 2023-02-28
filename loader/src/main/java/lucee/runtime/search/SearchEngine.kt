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
package lucee.runtime.search

import java.io.IOException

/**
 * interface for a Search Engine
 */
interface SearchEngine {
    @Throws(IOException::class, SearchException::class)
    fun init(config: Config?, searchDir: Resource?)

    /**
     * returns a collection by name
     *
     * @param name name of the desired collection (case insensitive)
     * @return returns lucene collection object matching name
     * @throws SearchException if no matching Collection exist
     */
    @Throws(SearchException::class)
    fun getCollectionByName(name: String?): SearchCollection?

    /**
     * @return returns all collections as a query object
     * @throws SearchException if no matching Collection exist
     */
    @Throws(SearchException::class)
    fun getCollectionsAsQuery(): Query?

    /**
     * Creates a new Collection and Store it (creating always a spellindex)
     *
     * @param name The Name of the Collection
     * @param path the path to store
     * @param language The language of the collection
     * @param allowOverwrite Allow Overwrite
     * @return New SearchCollection
     * @throws SearchException Search Exception
     */
    @Throws(SearchException::class)
    fun createCollection(name: String?, path: Resource?, language: String?, allowOverwrite: Boolean): SearchCollection?

    /**
     * @return returns the directory of the search storage
     */
    fun getDirectory(): Resource?
    /*
	 * * return XML Element Matching index id
	 * 
	 * @param collElement XML Collection Element
	 * 
	 * @param id
	 * 
	 * @return XML Element
	 */
    // public abstract Element getIndexElement(Element collElement, String id);
    /**
     * @return returns the Name of the search engine to display in admin
     */
    fun getDisplayName(): String?
    fun createSearchData(suggestionMax: Int): SearchData?

    companion object {
        /**
         * overwrite allowed
         */
        const val ALLOW_OVERWRITE = true

        /**
         * overwrite denied
         */
        const val DENY_OVERWRITE = false
    }
}