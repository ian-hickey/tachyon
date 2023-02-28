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

import tachyon.runtime.exp.TemplateException

/**
 * Used to: Abort the processing of the currently executing CFML custom tag, exit the template
 * within the currently executing CFML custom tag and reexecute a section of code within the
 * currently executing CFML custom tag
 *
 *
 *
 */
class Exit : TagImpl() {
    /**   */
    private var method = MODE_EXIT_TAG
    @Override
    fun release() {
        super.release()
        method = MODE_EXIT_TAG
    }

    /**
     * set the value method
     *
     * @param method value to set
     */
    fun setMethod(method: String?) {
        var method = method
        method = method.toLowerCase()
        if (method!!.equals("loop")) this.method = MODE_LOOP else if (method.equals("exittag")) this.method = MODE_EXIT_TAG else if (method.equals("exittemplate")) this.method = MODE_EXIT_TEMPLATE
    }

    @Override
    fun doStartTag(): Int {
        return SKIP_BODY
    }

    @Override
    @Throws(TemplateException::class)
    fun doEndTag(): Int {
        val variables: Scope = pageContext.variablesScope()
        val thistagObj: Object = variables.get("thistag", null)
        val insideCT = thistagObj != null && thistagObj is tachyon.runtime.type.Collection
        // executebody

        // Inside Custom Tag
        if (insideCT) {
            val thistag: tachyon.runtime.type.Collection = thistagObj as tachyon.runtime.type.Collection
            // executionmode
            val exeModeObj: Object = thistag.get("executionmode", null)
            val isEndMode = exeModeObj != null && exeModeObj is String && exeModeObj.toString().equalsIgnoreCase("end")

            // Start
            if (!isEndMode) {
                if (method == MODE_LOOP) {
                    throw TemplateException("invalid context for the tag exit, method loop can only be used in the end tag of a custom tag")
                } else if (method == MODE_EXIT_TAG) {
                    thistag.setEL("executebody", Boolean.FALSE)
                    return SKIP_PAGE
                }
            } else if (method == MODE_LOOP) {
                thistag.setEL("executebody", Boolean.TRUE)
                return SKIP_PAGE
            }
            return SKIP_PAGE
        }

        // OUTside Custom Tag
        if (method == MODE_LOOP) throw TemplateException("invalid context for the tag exit, method loop can only be used inside a custom tag")
        return SKIP_PAGE
    }

    companion object {
        private const val MODE_LOOP: Short = 0
        private const val MODE_EXIT_TAG: Short = 1
        private const val MODE_EXIT_TEMPLATE: Short = 2
    }
}