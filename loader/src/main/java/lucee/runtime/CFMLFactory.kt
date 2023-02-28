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
package lucee.runtime

import java.net.URL

/**
 * implements a JSP Factory, this class procduce JSP Compatible PageContext Object this object holds
 * also the must interfaces to coldfusion specified functionality
 */
abstract class CFMLFactory : JspFactory() {
    /**
     * reset the PageContexes
     */
    abstract fun resetPageContext()

    /**
     * similar to getPageContext Method but return the concrete implementation of the Lucee PageContext
     * and take the HTTP Version of the Servlet Objects
     *
     * @param servlet servlet
     * @param req http request
     * @param rsp http response
     * @param errorPageURL error page URL
     * @param needsSession need session
     * @param bufferSize buffer size
     * @param autoflush auto flush
     * @return page context created
     */
    @Deprecated
    abstract fun getLuceePageContext(servlet: HttpServlet?, req: HttpServletRequest?, rsp: HttpServletResponse?, errorPageURL: String?, needsSession: Boolean, bufferSize: Int,
                                     autoflush: Boolean): PageContext?

    /**
     * similar to getPageContext Method but return the concrete implementation of the Lucee PageCOntext
     * and take the HTTP Version of the Servlet Objects
     *
     * @param servlet servlet
     * @param req http request
     * @param rsp http response
     * @param errorPageURL error page URL
     * @param needsSession need session
     * @param bufferSize buffer size
     * @param autoflush auto flush
     * @param register register the PageContext to the current thread
     * @param timeout timeout in ms, if the value is smaller than 1 it is ignored and the value comming
     * from the context is used
     * @param register2RunningThreads register to running threads
     * @param ignoreScopes ignore scopes
     * @return return the PageContext
     */
    abstract fun getLuceePageContext(servlet: HttpServlet?, req: HttpServletRequest?, rsp: HttpServletResponse?, errorPageURL: String?, needsSession: Boolean, bufferSize: Int,
                                     autoflush: Boolean, register: Boolean, timeout: Long, register2RunningThreads: Boolean, ignoreScopes: Boolean): PageContext?

    /**
     * Similar to the releasePageContext Method, but take lucee PageContext as entry
     *
     * @param pc page context
     */
    @Deprecated
    @Deprecated("use instead <code>releaseLuceePageContext(PageContext pc, boolean unregister)</code>")
    abstract fun releaseLuceePageContext(pc: PageContext?)

    /**
     * Similar to the releasePageContext Method, but take lucee PageContext as entry
     *
     * @param pc page context
     * @param unregister unregister from current thread
     */
    abstract fun releaseLuceePageContext(pc: PageContext?, unregister: Boolean)

    /**
     * check timeout of all running threads, downgrade also priority from all thread run longer than 10
     * seconds
     */
    abstract fun checkTimeout()

    /**
     * @return returns count of pagecontext in use
     */
    abstract fun getUsedPageContextLength(): Int

    /**
     * @return Returns the config.
     */
    abstract fun getConfig(): ConfigWeb?

    /**
     * @return label of the factory
     */
    abstract fun getLabel(): Object?
    abstract fun getURL(): URL?

    /**
     * @param label a label
     */
    @Deprecated
    @Deprecated("""no replacement
	  """)
    abstract fun setLabel(label: String?)

    /**
     * @return the servlet
     */
    abstract fun getServlet(): HttpServlet?
    abstract fun getEngine(): CFMLEngine?
    abstract fun toDialect(ext: String?): Int // FUTURE deprecate

    // public abstract int toDialect(String ext, int defaultValue);// FUTURE
    abstract fun getCFMLExtensions(): Iterator<String?>?
    abstract fun getLuceeExtensions(): Iterator<String?>?
}