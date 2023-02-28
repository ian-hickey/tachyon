/**
 * Copyright (c) 2015, Tachyon Assosication Switzerland. All rights reserved.
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

import tachyon.commons.io.res.Resource

class DummySearchEngine : SearchEngine {
    @Override
    fun init(config: Config?, searchDir: Resource?) {
    }

    @Override
    @Throws(SearchException::class)
    fun getCollectionByName(name: String?): SearchCollection? {
        throw notInstalled()
    }

    @Override
    @Throws(SearchException::class)
    fun getCollectionsAsQuery(): Query? {
        throw notInstalled()
    }

    @Override
    @Throws(SearchException::class)
    fun createCollection(name: String?, path: Resource?, language: String?, allowOverwrite: Boolean): SearchCollection? {
        throw notInstalled()
    }

    @Override
    fun getDirectory(): Resource? {
        throw notInstalledEL()
    }

    /*
	 * @Override public Element getIndexElement(Element collElement, String id) { throw
	 * notInstalledEL(); }
	 */
    @Override
    fun getDisplayName(): String? {
        throw notInstalledEL()
    }

    @Override
    fun createSearchData(suggestionMax: Int): SearchData? {
        throw notInstalledEL()
    }

    private fun notInstalled(): SearchException? {
        return SearchException("No Search Engine installed! Check out the Extension Store in the Tachyon Administrator for \"Search\".")
    }

    private fun notInstalledEL(): PageRuntimeException? {
        return PageRuntimeException(notInstalled())
    }

    companion object {
        private val LUCENE: String? = "EFDEB172-F52E-4D84-9CD1A1F561B3DFC8"
        private const val tryToInstall = true
    }
}