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
package tachyon.runtime.search

import kotlin.Throws
import tachyon.loader.util.Util
import kotlin.jvm.Synchronized
import kotlin.jvm.Transient
import org.osgi.framework.Version

/**
 * a single result item
 */
interface SearchResulItem {
    /**
     * @return Returns the recordsSearched.
     */
    fun getRecordsSearched(): Int

    /**
     * @return Returns the score.
     */
    fun getScore(): Float

    /**
     * @return Returns the summary.
     */
    fun getSummary(): String?

    /**
     * @return Returns the title.
     */
    fun getTitle(): String?

    /**
     * @return Returns the id.
     */
    fun getId(): String?

    /**
     * @return Returns the key
     */
    fun getKey(): String?

    /**
     * @return Returns the url
     */
    fun getUrl(): String?

    /**
     * @return Returns the custom1.
     */
    @Deprecated
    @Deprecated("""use instead <code>getCustom(int index)</code>
	  """)
    fun getCustom1(): String?

    /**
     * @return Returns the custom2.
     */
    @Deprecated
    @Deprecated("""use instead <code>getCustom(int index)</code>
	  """)
    fun getCustom2(): String?

    /**
     * @return Returns the custom3.
     */
    @Deprecated
    @Deprecated("""use instead <code>getCustom(int index)</code>
	  """)
    fun getCustom3(): String?

    /**
     * @return Returns the custom4.
     */
    @Deprecated
    @Deprecated("""use instead <code>getCustom(int index)</code>
	  """)
    fun getCustom4(): String?

    @Throws(SearchException::class)
    fun getCustom(index: Int): String?

    /**
     * @return the category
     */
    fun getCategory(): String?

    /**
     * @return the categoryTree
     */
    fun getCategoryTree(): String?

    /**
     * @return the mimeType
     */
    fun getMimeType(): String?

    /**
     * @return the author
     */
    fun getAuthor(): String?

    /**
     * @return the size
     */
    fun getSize(): String?

    /**
     * @return the contextSummary
     */
    fun getContextSummary(): String?
}