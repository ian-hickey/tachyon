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

import lucee.runtime.exp.PageException

/**
 * Outputs variables for debugging purposes. Using cfdump, you can display the contents of simple
 * variables, queries, arrays, structures, and WDDX variables created with cfwddx. if no var
 * attribute defined it dump the hole site information
 *
 *
 *
 */
class Dump : TagImpl() {
    /** Variable to display. Enclose a variable name in pound signs  */
    private var `var`: Object? = null

    /** Name of Variable to display  */
    private var eval: Object? = null

    /** string; header for the dump output.  */
    private var label: String? = null
    private var format: String? = null
    private var output: String? = null

    // private double maxlevel=Integer.MAX_VALUE;
    private var expand = true
    private var top = 9999
    private var hide: String? = null
    private var show: String? = null
    private var keys = 9999.0
    private var showUDFs = true
    private var metainfo = true
    private var abort = false
    @Override
    fun release() {
        super.release()
        `var` = null
        eval = null
        label = null
        // maxlevel=Integer.MAX_VALUE;
        format = null
        output = null
        expand = true
        top = 9999
        hide = null
        show = null
        keys = 9999.0
        metainfo = true
        showUDFs = true
        abort = false
    }

    /**
     * @param top the top to set
     */
    fun setTop(top: Double) {
        this.top = top.toInt() + 1
    }

    fun setHide(hide: String?) {
        this.hide = hide
    }

    fun setShow(show: String?) {
        this.show = show
    }

    fun setOutput(output: String?) {
        this.output = output
    }

    fun setKeys(keys: Double) {
        this.keys = keys
    }

    fun setMetainfo(metainfo: Boolean) {
        this.metainfo = metainfo
    }

    /**
     * set the value expand not supported at the moment
     *
     * @param expand value to set
     */
    fun setExpand(expand: Boolean) {
        this.expand = expand
    }

    /**
     * set the value var Variable to display. Enclose a variable name in pound signs
     *
     * @param var value to set
     */
    fun setVar(`var`: Object?) {
        this.`var` = `var`
    }

    /**
     * set the value eval Variable to display. Enclose a variable name in pound signs
     *
     * @param eval value to set
     */
    fun setEval(eval: Object?) {
        this.eval = eval
    }

    /**
     * set the value label string; header for the dump output.
     *
     * @param label value to set
     */
    fun setLabel(label: String?) {
        this.label = label
    }

    /**
     * @param maxlevel the maxlevel to set
     */
    fun setMaxlevel(maxlevel: Double) {
        top = maxlevel.toInt()
    }

    /**
     * @param type the type to set
     */
    fun setType(type: String?) {
        format = type
    }

    fun setFormat(format: String?) {
        this.format = format
    }

    @Override
    @Throws(PageException::class)
    fun doStartTag(): Int {
        if (`var` == null && eval != null) {
            `var` = Evaluate.call(pageContext, arrayOf<Object?>(eval))
            if (label == null) label = Caster.toString(eval)
        }
        lucee.runtime.functions.other.Dump.call(pageContext, `var`, label, expand, top.toDouble(), show, hide, output, format, keys, metainfo, showUDFs)
        if (abort) throw Abort(lucee.runtime.exp.Abort.SCOPE_REQUEST)
        return SKIP_BODY
    }

    @Override
    fun doEndTag(): Int {
        return EVAL_PAGE
    }

    /**
     * @param showUDFs the showUDFs to set
     */
    fun setShowudfs(showUDFs: Boolean) {
        this.showUDFs = showUDFs
    }

    /**
     * @param abort the abort to set
     */
    fun setAbort(abort: Boolean) {
        this.abort = abort
    }
}