/**
 * Copyright (c) 2015, Lucee Assosication Switzerland. All rights reserved.
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

import kotlin.Throws
import lucee.loader.util.Util
import kotlin.jvm.Synchronized
import kotlin.jvm.Transient
import org.osgi.framework.Version

/**
 */
interface SearchIndex {
    /**
     * @return Returns the custom1.
     */
    fun getCustom1(): String?

    /**
     * @return Returns the custom2.
     */
    fun getCustom2(): String?

    /**
     * @return Returns the custom3.
     */
    fun getCustom3(): String?

    /**
     * @return Returns the custom4.
     */
    fun getCustom4(): String?

    /**
     * @return Returns the extensions.
     */
    fun getExtensions(): Array<String?>?

    /**
     * @return Returns the key.
     */
    fun getKey(): String?

    /**
     * @return Returns the language.
     */
    fun getLanguage(): String?

    /**
     * @return Returns the title.
     */
    fun getTitle(): String?

    /**
     * @return Returns the type.
     */
    fun getType(): Short

    /**
     * @return Returns the id.
     */
    fun getId(): String?
    /**
     * @param id The id to set. / public void setId(String id) { this.id = id; }
     */
    /**
     * @return Returns the urlpath.
     */
    fun getUrlpath(): String?

    /**
     * @return Returns the query.
     */
    fun getQuery(): String?

    /**
     * @return the categories
     */
    fun getCategories(): Array<String?>?

    /**
     * @return the categoryTree
     */
    fun getCategoryTree(): String?

    companion object {
        /**
         * Field `TYPE_FILE`
         */
        const val TYPE_FILE: Short = 0

        /**
         * Field `TYPE_PATH`
         */
        const val TYPE_PATH: Short = 1

        /**
         * Field `TYPE_CUSTOM`
         */
        const val TYPE_CUSTOM: Short = 2

        /**
         * Field `TYPE_URL`
         */
        const val TYPE_URL: Short = 3
    }
}