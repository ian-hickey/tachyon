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

import tachyon.runtime.exp.ExpressionException

/**
 * Executes a Java servlet on a JRun engine. This tag is used in conjunction with the cfserletparam
 * tag, which passes data to the servlet.
 *
 *
 *
 */
class Servlet : TagImpl() {
    private var debug = false
    private var code: String? = null
    private var writeoutput = false
    private var timeout = 0.0
    private var jrunproxy: String? = null

    /**
     * set the value debug Boolean specifying whether additional information about the JRun connection
     * status and activity is to be written to the JRun error log
     *
     * @param debug value to set
     */
    fun setDebug(debug: Boolean) {
        this.debug = debug
    }

    /**
     * set the value code The class name of the Java servlet to execute.
     *
     * @param code value to set
     */
    fun setCode(code: String?) {
        this.code = code
    }

    /**
     * set the value writeoutput
     *
     * @param writeoutput value to set
     */
    fun setWriteoutput(writeoutput: Boolean) {
        this.writeoutput = writeoutput
    }

    /**
     * set the value timeout Specifies how many seconds JRun waits for the servlet to complete before
     * timing out.
     *
     * @param timeout value to set
     */
    fun setTimeout(timeout: Double) {
        this.timeout = timeout
    }

    /**
     * set the value jrunproxy
     *
     * @param jrunproxy value to set
     */
    fun setJrunproxy(jrunproxy: String?) {
        this.jrunproxy = jrunproxy
    }

    @Override
    fun doStartTag(): Int {
        return SKIP_BODY
    }

    @Override
    fun doEndTag(): Int {
        return EVAL_PAGE
    }

    @Override
    fun release() {
        super.release()
        debug = false
        code = ""
        writeoutput = false
        timeout = 0.0
        jrunproxy = ""
    }

    /**
     * constructor for the tag class
     *
     * @throws ExpressionException
     */
    init {
        throw ExpressionException("tag cfservlet is deprecated")
    }
}