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
 * Implementation of the BodyTag
 */
abstract class BodyTagSupport : TagSupport(), BodyTag {
    /**
     * Field `bodyContent`
     */
    protected var bodyContent: BodyContent? = null

    /**
     * @see javax.servlet.jsp.tagext.BodyTag.setBodyContent
     */
    @Override
    fun setBodyContent(bodyContent: BodyContent?) {
        this.bodyContent = bodyContent
    }

    /**
     * @see javax.servlet.jsp.tagext.BodyTag.doInitBody
     */
    @Override
    @Throws(JspException::class)
    fun doInitBody() {
    }

    /**
     * @see javax.servlet.jsp.tagext.IterationTag.doAfterBody
     */
    @Override
    @Throws(JspException::class)
    fun doAfterBody(): Int {
        return SKIP_BODY
    }

    /**
     * @see javax.servlet.jsp.tagext.Tag.release
     */
    @Override
    override fun release() {
        super.release()
        bodyContent = null
    }
}