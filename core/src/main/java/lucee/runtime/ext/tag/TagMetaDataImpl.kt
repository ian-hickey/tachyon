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
package lucee.runtime.ext.tag

import java.util.ArrayList

class TagMetaDataImpl
/**
 * Constructor of the class
 *
 * @param attrType
 * TagMetaData.ATTRIBUTE_TYPE_FIX,TagMetaData.ATTRIBUTE_TYPE_DYNAMIC,TagMetaData.ATTRIBUTE_TYPE_MIXED
 * @param attrMin minimal count of attributes needed for tag
 * @param attrMax maximum count of attributes or -1 for infinity attributes
 * @param bodyContent
 * TagMetaData.BODY_CONTENT_EMPTY,TagMetaData.BODY_CONTENT_FREE,TagMetaData.BODY_CONTENT_MUST
 * @param isBodyRE is the body of the tag parsed like inside a cfoutput
 * @param description A description of the tag.
 */(@get:Override val attributeType: Int, @get:Override val attributeMin: Int, @get:Override val attributeMax: Int, bodyContent: Int, @get:Override val isBodyRuntimeExpressionValue: Boolean, @get:Override val description: String?, private val handleException: Boolean, private val hasAppendix: Boolean,
    private val hasBody: Boolean) : TagMetaData {

    private val attrs: List<TagMetaDataAttr?>? = ArrayList<TagMetaDataAttr?>()

    @get:Override
    val bodyContent = 0

    @get:Override
    val attributes: Array<Any?>?
        get() = attrs.toArray(arrayOfNulls<TagMetaDataAttr?>(attrs!!.size()))

    /**
     * adds an attribute to the tag
     *
     * @param attr
     */
    fun addAttribute(attr: TagMetaDataAttr?) {
        attrs.add(attr)
    }

    @Override
    fun handleException(): Boolean {
        return handleException
    }

    @Override
    fun hasAppendix(): Boolean {
        return hasAppendix
    }

    @Override
    fun hasBody(): Boolean {
        return hasBody
    }
}