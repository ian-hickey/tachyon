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
package tachyon.commons.io

import javax.servlet.jsp.JspWriter

/**
 * Stack for the Body Content Objects
 */
class BodyContentStack {
    private var base: CFMLWriter? = null
    private val nirvana: DevNullBodyContent = DevNullBodyContent()
    private var current: Entry?
    private val root: Entry?

    /**
     * initialize the BodyContentStack
     *
     * @param rsp
     */
    fun init(writer: CFMLWriter?) {
        base = writer
    }

    /**
     * release the BodyContentStack
     */
    fun release() {
        base = null
        current = root
        current!!.body = null
        current!!.after = null
        current!!.before = null
    }

    /**
     * push a new BodyContent to Stack
     *
     * @return new BodyContent
     */
    fun push(): BodyContent? {
        if (current!!.after == null) {
            current!!.after = Entry(current, BodyContentImpl(if (current!!.body == null) base as JspWriter? else current!!.body))
        } else {
            current!!.after!!.devNull = false
            current!!.after!!.body.init(if (current!!.body == null) base as JspWriter? else current!!.body)
        }
        current = current!!.after
        return current!!.body
    }

    /**
     * pop a BodyContent from Stack
     *
     * @return BodyContent poped
     */
    fun pop(): JspWriter? {
        if (current!!.before != null) current = current!!.before
        return writer
    }

    /**
     * @return returns current writer
     */
    val writer: JspWriter?
        get() = if (!current!!.devNull) {
            if (current!!.body != null) current!!.body else base
        } else nirvana

    internal inner class Entry private constructor(before: Entry, body: BodyContentImpl) {
        val before: Entry?
        val after: Entry? = null
        /**
         * @return returns DevNull Object
         */
        /**
         * set if current BodyContent is DevNull or not
         *
         * @param doDevNull
         */
        val devNull = false
            get() = current.field
            set(doDevNull) {
                current!!.devNull = doDevNull
            }
        val body: BodyContentImpl?

        init {
            this.before = before
            this.body = body
        }
    }

    /**
     * @return returns DevNull Object
     */
    val devNullBodyContent: DevNullBodyContent
        get() = nirvana

    /**
     * @return Returns the base.
     */
    fun getBase(): CFMLWriter? {
        return base
    }

    /**
     * Default Constructor
     */
    init {
        current = Entry(null, null)
        root = current
    }
}