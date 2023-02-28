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
package tachyon.runtime.exp

import tachyon.runtime.PageSource

/**
 * Template Exception Object
 */
class TemplateException : PageExceptionImpl {
    /**
     * @return the line
     */
    var line = 0
        private set
    private var pageSource: PageSource? = null

    /**
     * constructor of the template exception
     *
     * @param message Exception Message
     */
    constructor(message: String?) : super(message, "template") {}

    /**
     * constructor of the template exception
     *
     * @param message Exception Message
     * @param detail Detailed Exception Message
     */
    constructor(message: String?, detail: String?) : super(message, "template") {
        setDetail(detail)
    }

    /**
     * Constructor of the class
     *
     * @param srcCode
     * @param message
     */
    constructor(ps: PageSource?, line: Int, column: Int, message: String?) : super(if (ps != null) "failure in " + ps.getDisplayPath().toString() + ";" + message else message, "template") {
        // print.err(line+"+"+column);
        addContext(ps, line, column, null)
        this.line = line
        pageSource = ps
    }

    constructor(ps: PageSource?, line: Int, column: Int, t: Throwable?) : super(t, "template") {
        // print.err(line+"+"+column);
        addContext(ps, line, column, null)
        this.line = line
        pageSource = ps
    }

    /**
     * Constructor of the class
     *
     * @param cfml
     * @param message
     */
    constructor(sc: SourceCode?, message: String?) : this(getPageSource(sc), sc.getLine(), sc.getColumn(), message) {}
    constructor(sc: SourceCode?, line: Int, column: Int, message: String?) : this(getPageSource(sc), line, column, message) {}

    /**
     * Constructor of the class
     *
     * @param cfml
     * @param message
     * @param detail
     */
    constructor(sc: SourceCode?, message: String?, detail: String?) : this(getPageSource(sc), sc.getLine(), sc.getColumn(), message) {
        setDetail(detail)
    }

    /**
     * Constructor of the class
     *
     * @param cfml
     * @param e
     */
    constructor(sc: SourceCode?, t: Throwable?) : this(getPageSource(sc), sc.getLine(), sc.getColumn(), t) {}

    /**
     * @return the pageSource
     */
    fun getPageSource(): PageSource? {
        return pageSource
    }

    companion object {
        private fun getPageSource(sc: SourceCode?): PageSource? {
            return if (sc is PageSourceCode) (sc as PageSourceCode?).getPageSource() else null
        }
    }
}