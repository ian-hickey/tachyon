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

import javax.servlet.jsp.JspException

/**
 * Implementation of the Tag
 */
abstract class TagSupport : Tag {
    /**
     * Field `pageContext`
     */
    protected var pageContext: PageContext? = null
    private var parent: Tag? = null

    /**
     * sets a Lucee PageContext
     *
     * @param pageContext page context
     */
    fun setPageContext(pageContext: PageContext?) {
        this.pageContext = pageContext
    }

    /**
     * @see javax.servlet.jsp.tagext.Tag.setPageContext
     */
    @Override
    fun setPageContext(pageContext: javax.servlet.jsp.PageContext?) {
        this.pageContext = pageContext as PageContext?
    }

    /**
     * @see javax.servlet.jsp.tagext.Tag.setParent
     */
    @Override
    fun setParent(parent: Tag?) {
        this.parent = parent
    }

    /**
     * @see javax.servlet.jsp.tagext.Tag.getParent
     */
    @Override
    fun getParent(): Tag? {
        return parent
    }

    /**
     * @see javax.servlet.jsp.tagext.Tag.doStartTag
     */
    @Override
    @Throws(JspException::class)
    fun doStartTag(): Int {
        return SKIP_BODY
    }

    /**
     * @see javax.servlet.jsp.tagext.Tag.doEndTag
     */
    @Override
    @Throws(JspException::class)
    fun doEndTag(): Int {
        return EVAL_PAGE
    }

    /**
     * @see javax.servlet.jsp.tagext.Tag.release
     */
    @Override
    fun release() {
        pageContext = null
        parent = null
    }

    /**
     * check if value is not empty
     *
     * @param tagName tag name
     * @param actionName action name
     * @param attributeName attribute name
     * @param attribute attribute
     * @throws PageException Page Exception
     */
    @Throws(PageException::class)
    fun required(tagName: String, actionName: String, attributeName: String, attribute: Object?) {
        if (attribute == null) {
            val util: Excepton = CFMLEngineFactory.getInstance().getExceptionUtil()
            throw util.createApplicationException("Attribute [$attributeName] for tag [$tagName] is required if attribute action has the value [$actionName]")
        }
    }
}