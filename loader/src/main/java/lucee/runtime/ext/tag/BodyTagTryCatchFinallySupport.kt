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
package lucee.runtime.ext.tag

import javax.servlet.jsp.tagext.TryCatchFinally

/**
 * extends Body Support Tag with TryCatchFinally Functionality
 */
abstract class BodyTagTryCatchFinallySupport : BodyTagSupport(), TryCatchFinally {
    /**
     * @see javax.servlet.jsp.tagext.TryCatchFinally.doCatch
     */
    @Override
    @Throws(Throwable::class)
    fun doCatch(t: Throwable) {
        var t = t
        if (t is PageServletException) {
            val pse: PageServletException = t as PageServletException
            t = pse.getPageException()
        }
        if (bodyContent != null) {
            val util: Excepton = CFMLEngineFactory.getInstance().getExceptionUtil()
            if (util.isOfType(Excepton.TYPE_ABORT, t)) bodyContent.writeOut(bodyContent.getEnclosingWriter())
            bodyContent.clearBuffer()
        }
        throw t
    }

    /**
     * @see javax.servlet.jsp.tagext.TryCatchFinally.doFinally
     */
    @Override
    fun doFinally() {
    }
}