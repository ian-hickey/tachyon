/**
 * Copyright (c) 2014, the Railo Company Ltd.
 * Copyright (c) 2015, Lucee Assosication Switzerland
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
package lucee.runtime.exp

import lucee.runtime.PageContext

/**
 * interface of the root business exception of Lucee
 */
interface IPageException : Dumpable {
    /**
     * return detailed error message
     *
     * @return detailed error message
     */
    /**
     * sets detailed error message
     *
     * @param detail detail
     */
    var detail: String?
    /**
     * Error Code
     *
     * @return Error Code
     */
    /**
     * sets the Error Code
     *
     * @param errorCode error code
     */
    var errorCode: String?
    /**
     * return extended info to the error
     *
     * @return extended info
     */
    /**
     * sets extended info to the error
     *
     * @param extendedInfo extended info
     */
    var extendedInfo: String?
    /*
	 * *
	 * 
	 * @return returns the line where the failure occurred
	 */
    // public String getLine();
    /**
     * @return Returns the tracePointer.
     */
    /**
     * @param tracePointer The tracePointer to set.
     */
    var tracePointer: Int

    /**
     * Error type as String
     *
     * @return error type
     */
    val typeAsString: String?

    /**
     * Error custom type as String
     *
     * @return error type
     */
    val customTypeAsString: String?

    /**
     * return detailed catch block of the error
     *
     * @param pc page context
     * @return catch block
     */
    @Deprecated
    @Deprecated("use instead <code>getCatchBlock(Config config);</code>")
    fun getCatchBlock(pc: PageContext?): Struct?

    /**
     * return detailed catch block of the error
     *
     * @param config config
     * @return catch block
     */
    fun getCatchBlock(config: Config?): CatchBlock?

    /**
     * return detailed error block of the error
     *
     * @param pc page context of the request
     * @param ep error page
     * @return catch block
     */
    fun getErrorBlock(pc: PageContext?, ep: ErrorPage?): Struct?

    /**
     * add a template to the context of the error
     *
     * @param pageSource new template context
     * @param line line of the error
     * @param column column of the error
     * @param element stack trace element
     */
    fun addContext(pageSource: PageSource?, line: Int, column: Int, element: StackTraceElement?)

    /**
     * compare error type as String
     *
     * @param type other error type
     * @return is same error type
     */
    fun typeEqual(type: String?): Boolean

    /**
     * @return Returns the additional.
     */
    @get:Deprecated("use instead <code>getAdditional();</code>")
    @get:Deprecated
    val addional: Struct?

    /**
     * @return Returns the additional.
     */
    val additional: Struct?

    /**
     * returns the java stracktrace as a String
     *
     * @return stack trace
     */
    val stackTraceAsString: String?
}