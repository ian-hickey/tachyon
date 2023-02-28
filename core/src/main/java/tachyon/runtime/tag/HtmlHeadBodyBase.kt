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

import java.io.IOException

/**
 * base class for both cfhtmlhead and cfhtmlbody
 */
abstract class HtmlHeadBodyBase : BodyTagTryCatchFinallyImpl() {
    /**
     * The text to add to the 'head' area of an HTML page. Everything inside the quotation marks is
     * placed in the 'head' section
     */
    protected var text: String? = null
    protected var variable: String? = null
    private var action: String? = null
    private var id: String? = null
    private var force = defaultForce
    @Override
    fun release() {
        super.release()
        text = null
        variable = null
        action = null
        id = null
        force = defaultForce
    }

    abstract val defaultForce: Boolean
    abstract val tagName: String?
    @Throws(IOException::class, ApplicationException::class)
    abstract fun actionAppend()
    @Throws(IOException::class)
    abstract fun actionFlush()
    @Throws(IOException::class, PageException::class)
    abstract fun actionRead()
    @Throws(IOException::class)
    abstract fun actionReset()
    @Throws(IOException::class, ApplicationException::class)
    abstract fun actionWrite()

    /**
     * @param variable the variable to set
     */
    fun setVariable(variable: String?) {
        this.variable = variable
    }

    /**
     * @param action the action to set
     */
    fun setAction(action: String?) {
        if (StringUtil.isEmpty(action, true)) return
        this.action = action.trim().toLowerCase()
    }

    /**
     * set the value text The text to add to the 'head' area of an HTML page. Everything inside the
     * quotation marks is placed in the 'head' section
     *
     * @param text value to set
     */
    fun setText(text: String?) {
        this.text = text
    }

    fun setId(id: String?) {
        this.id = id
    }

    fun setForce(force: Boolean) {
        this.force = force
    }

    @Override
    @Throws(PageException::class)
    fun doStartTag(): Int {
        return EVAL_BODY_BUFFERED
    }

    @Override
    @Throws(PageException::class)
    fun doEndTag(): Int {
        processTag()
        return SKIP_BODY
    }

    @Override
    @Throws(PageException::class)
    fun doAfterBody(): Int {
        if (bodyContent != null) {
            text = if (!StringUtil.isEmpty(text)) {
                StringBuilder(text).append(bodyContent.getString()).toString() // appends text and body content
            } else {
                bodyContent.getString()
            }
            bodyContent.clearBody()
        }
        return SKIP_BODY
    }

    @Throws(PageException::class)
    protected fun processTag() {
        try {
            if (StringUtil.isEmpty(action, true) || action!!.equals("append")) {
                required(tagName, "text", text)
                if (isValid) actionAppend()
            } else if (action!!.equals("reset")) {
                resetIdMap()
                actionReset()
            } else if (action!!.equals("write")) {
                required(tagName, "text", text)
                resetIdMap()
                if (isValid) // call isValid() to register the id if set
                    actionWrite()
            } else if (action!!.equals("read")) actionRead() else if (action!!.equals("flush")) actionFlush() else throw ApplicationException("invalid value [$action] for attribute [action]", "supported actions are [append, read, reset, write, flush]")
        } catch (e: IOException) {
            throw Caster.toPageException(e)
        }
    }

    /**
     *
     * @return - true if the id was not set or was set and was not used yet in the request. if it was
     * not set -- register it for future calls of the tag
     */
    protected val isValid: Boolean
        protected get() {
            if (!force && pageContext is PageContextImpl && (pageContext as PageContextImpl?).isSilent()) return false
            if (StringUtil.isEmpty(id)) return true
            val m: Map? = idMap
            val result: Boolean = !m.containsKey(id)
            if (result) m.put(id, Boolean.TRUE)
            return result
        }
    protected val idMap: Map?
        protected get() {
            val reqAttr = REQUEST_ATTRIBUTE_PREFIX + tagName
            var result: Map? = pageContext.getRequest().getAttribute(reqAttr)
            if (result == null) {
                result = TreeMap(String.CASE_INSENSITIVE_ORDER)
                pageContext.getRequest().setAttribute(reqAttr, result)
            }
            return result
        }

    protected fun resetIdMap() {
        val reqAttr = REQUEST_ATTRIBUTE_PREFIX + tagName
        pageContext.getRequest().setAttribute(reqAttr, null)
    }

    companion object {
        private val REQUEST_ATTRIBUTE_PREFIX: String? = "REQUEST_ATTRIBUTE_IDMAP_"
    }
}