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
package lucee.runtime.tag

import java.io.IOException

/**
 * Suppresses extra white space and other output, produced by CFML within the tag's scope.
 *
 *
 *
 */
class ProcessingDirective
/**
 * constructor for the tag class
 */
    : BodyTagTryCatchFinallyImpl() {
    /**
     * A string literal; the character encoding to use to read the page. The value may be enclosed in
     * single or double quotation marks, or none.
     */
    // private String pageencoding=null;
    private var suppresswhitespace: Boolean? = null
    private var hasBody = false
    @Override
    fun release() {
        super.release()
        // pageencoding=null;
        suppresswhitespace = null
    }

    /**
     * set the value pageencoding A string literal; the character encoding to use to read the page. The
     * value may be enclosed in single or double quotation marks, or none.
     *
     * @param pageencoding value to set
     */
    fun setPageencoding(pageencoding: String?) {}
    fun setExecutionlog(executionlog: Boolean) {}
    fun setPreservecase(b: Boolean) {}

    /**
     * set the value suppresswhitespace Boolean indicating whether to suppress the white space and other
     * output generated by the CFML tags within the cfprocessingdirective block.
     *
     * @param suppresswhitespace value to set
     */
    fun setSuppresswhitespace(suppresswhitespace: Boolean) {
        this.suppresswhitespace = Caster.toBoolean(suppresswhitespace)
    }

    @Override
    @Throws(ApplicationException::class)
    fun doStartTag(): Int {
        if (suppresswhitespace != null && !hasBody) {
            throw ApplicationException("To suppress whitespace, an end tag [cfprocessingdirective] is also required")
        }
        return if (suppresswhitespace != null) EVAL_BODY_BUFFERED else EVAL_BODY_INCLUDE
    }

    @Override
    fun doInitBody() {
    }

    @Override
    fun doAfterBody(): Int {
        return SKIP_BODY
    }

    /**
     * sets if tag has a body or not
     *
     * @param hasBody
     */
    fun hasBody(hasBody: Boolean) {
        this.hasBody = hasBody
    }

    @Override
    fun doFinally() {
        if (suppresswhitespace != null) {
            try {
                val out: JspWriter = pageContext.getOut()
                if (suppresswhitespace.booleanValue()) {
                    if (out is WhiteSpaceWriter) out.write(bodyContent.getString()) else out.write(StringUtil.suppressWhiteSpace(bodyContent.getString()))
                } else {
                    if (out is CFMLWriter) {
                        (out as CFMLWriter).writeRaw(bodyContent.getString())
                    } else out.write(bodyContent.getString())
                }
            } catch (e: IOException) {
                throw PageRuntimeException(Caster.toPageException(e))
            }
        }
    }
}