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
package tachyon.runtime.ext.tag

import javax.servlet.jsp.JspException

/**
 * Implementation of the Tag
 */
abstract class TagImpl : Tag {
    protected var pageContext: PageContext? = null
    private var parent: Tag? = null
    protected var sourceTemplate: String? = null

    /**
     * sets a PageContext
     *
     * @param pageContext
     */
    fun setPageContext(pageContext: PageContext?) {
        this.pageContext = pageContext
    }

    @Override
    fun setPageContext(pageContext: javax.servlet.jsp.PageContext?) {
        this.pageContext = pageContext as PageContext?
    }

    @Override
    fun setParent(parent: Tag?) {
        this.parent = parent
    }

    fun setSourceTemplate(source: String?) {
        sourceTemplate = source
    }

    @Override
    fun getParent(): Tag? {
        return parent
    }

    @Override
    @Throws(JspException::class)
    fun doStartTag(): Int {
        return SKIP_BODY
    }

    @Override
    @Throws(JspException::class)
    fun doEndTag(): Int {
        return EVAL_PAGE
    }

    @Override
    fun release() {
        pageContext = null
        parent = null
    }

    /**
     * check if value is not empty
     *
     * @param tagName
     * @param attributeName
     * @param attribute
     * @throws ApplicationException
     */
    @Throws(ApplicationException::class)
    fun required(tagName: String?, actionName: String?, attributeName: String?, attribute: Object?) {
        if (attribute == null) throw ApplicationException("Attribute [$attributeName] for tag [$tagName] is required if attribute action has the value [$actionName]")
    }

    @Throws(ApplicationException::class)
    fun required(tagName: String?, attributeName: String?, attribute: Object?) {
        if (attribute == null) throw ApplicationException("Attribute [$attributeName] for tag [$tagName] is required")
    }

    @Throws(ApplicationException::class)
    fun required(tagName: String?, actionName: String?, attributeName: String?, attribute: String?, trim: Boolean) {
        if (StringUtil.isEmpty(attribute, trim)) throw ApplicationException("Attribute [$attributeName] for tag [$tagName] is required if attribute action has the value [$actionName]")
    }

    @Throws(ApplicationException::class)
    fun required(tagName: String?, actionName: String?, attributeName: String?, attributeValue: Double, nullValue: Double) {
        if (attributeValue == nullValue) throw ApplicationException("Attribute [$attributeName] for tag [$tagName] is required if attribute action has the value [$actionName]")
    }
}