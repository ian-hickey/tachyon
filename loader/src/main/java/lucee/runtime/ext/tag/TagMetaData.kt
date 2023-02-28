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
package lucee.runtime.ext.tag

import kotlin.Throws
import lucee.loader.util.Util
import kotlin.jvm.Synchronized
import kotlin.jvm.Transient
import org.osgi.framework.Version

interface TagMetaData {
    /**
     * type of the body content
     *
     * @return TagMetaData.BODY_CONTENT_EMPTY,TagMetaData.BODY_CONTENT_FREE, TagMetaData
     * .BODY_CONTENT_MUST
     */
    val bodyContent: Int

    /**
     * attribute type
     *
     * @return TagMetaData.ATTRIBUTE_TYPE_FIX,TagMetaData.ATTRIBUTE_TYPE_DYNAMIC ,
     * TagMetaData.ATTRIBUTE_TYPE_MIXED
     */
    val attributeType: Int

    /**
     * minimal count of attributes needed for tag
     *
     * @return minimal count of attributes
     */
    val attributeMin: Int

    /**
     * maximum count of attributes needed for tag
     *
     * @return maximum count of attributes or -1 for infinity attributes
     */
    val attributeMax: Int

    /**
     * is the body of the tag parsed like inside a cfoutput
     *
     * @return parsed or not
     */
    val isBodyRuntimeExpressionValue: Boolean

    /**
     * A description of the tag.
     *
     * @return description of the tag
     */
    val description: String?

    /**
     * get attributes of the tag
     * @return attributes of the tag
     */
    val attributes: Array<lucee.runtime.ext.tag.TagMetaDataAttr?>?

    /**
     * has the tag a body
     *
     * @return has a body
     */
    fun hasBody(): Boolean

    /**
     * can the tag handle exceptions
     *
     * @return can handle exceptions
     */
    fun handleException(): Boolean

    /**
     * has the tag an appendix
     *
     * @return has appendix
     */
    fun hasAppendix(): Boolean

    companion object {
        /**
         * Body is not allowed for this tag
         */
        const val BODY_CONTENT_EMPTY = 0

        /**
         * tag can have a body, but it is not required
         */
        const val BODY_CONTENT_FREE = 1

        /**
         * body is required for this tag
         */
        const val BODY_CONTENT_MUST = 2

        /**
         * tag has a fix defined group of attributes, only this attributes are allowed
         */
        const val ATTRIBUTE_TYPE_FIX = 4

        /**
         * there is no restriction or rules for attributes, tag can have as many as whished
         */
        const val ATTRIBUTE_TYPE_DYNAMIC = 8

        /**
         * tag has a fix set of attributes, but is also free in use additional tags
         */
        const val ATTRIBUTE_TYPE_MIXED = 16
    }
}