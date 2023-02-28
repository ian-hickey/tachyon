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
package tachyon.runtime.tag

import tachyon.runtime.config.ConfigPro

/**
 * Saves the generated content inside the tag body in a variable.
 *
 *
 *
 */
class SaveContent : BodyTagTryCatchFinallyImpl() {
    /** The name of the variable in which to save the generated content inside the tag.  */
    private var variable: String? = null
    private var trim: Boolean? = null
    private var append = false
    @Override
    fun release() {
        super.release()
        variable = null
        trim = null
        append = false
    }

    /**
     * set the value variable The name of the variable in which to save the generated content inside the
     * tag.
     *
     * @param variable value to set
     */
    fun setVariable(variable: String?) {
        this.variable = variable
    }

    fun setTrim(trim: Boolean) {
        this.trim = trim
    }

    /**
     * if true, and a variable with the passed name already exists, the content will be appended to the
     * variable instead of overwriting it
     */
    fun setAppend(append: Boolean) {
        this.append = append
    }

    @Override
    fun doStartTag(): Int {
        return EVAL_BODY_BUFFERED
    }

    @Override
    @Throws(PageException::class)
    fun doAfterBody(): Int {
        // If trim-attribute is not set by the user, use the whitespace-setting
        if (trim == null) {
            val config: ConfigWebPro = pageContext.getConfig() as ConfigWebPro
            trim = config.getCFMLWriterType() !== ConfigPro.CFML_WRITER_REFULAR
        }
        var value: String = if (trim!!) bodyContent.getString().trim() else bodyContent.getString()
        if (append) {
            value = Caster.toString(VariableInterpreter.getVariableEL(pageContext, variable, ""), "") + value // prepend the current variable or empty-string if not found
        }
        pageContext.setVariable(variable, value)
        bodyContent.clearBody()
        return SKIP_BODY
    }
}