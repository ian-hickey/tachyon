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

import java.io.IOException

class Silent : BodyTagTryCatchFinallyImpl() {
    private var bufferOutput: Boolean? = true
    private var bc: BodyContentImpl? = null
    private var wasSilent = false

    /**
     * @param bufferoutput the bufferoutput to set
     */
    fun setBufferoutput(bufferOutput: Boolean) {
        this.bufferOutput = if (bufferOutput) Boolean.TRUE else Boolean.FALSE
    }

    @Override
    @Throws(JspException::class)
    fun doStartTag(): Int {
        if (bufferOutput == null) bufferOutput = if ((pageContext.getApplicationContext() as ApplicationContextSupport).getBufferOutput()) Boolean.TRUE else Boolean.FALSE
        if (bufferOutput.booleanValue()) bc = pageContext.pushBody() as BodyContentImpl else wasSilent = pageContext.setSilent()
        return EVAL_BODY_INCLUDE
    }

    @Override
    @Throws(Throwable::class)
    fun doCatch(t: Throwable?) {
        ExceptionUtil.rethrowIfNecessary(t)
        if (bufferOutput.booleanValue()) {
            try {
                bc.flush()
            } catch (e: IOException) {
            }
            pageContext.popBody()
            bc = null
        } else if (!wasSilent) pageContext.unsetSilent()
        super.doCatch(t)
    }

    @Override
    fun doFinally() {
        if (bufferOutput.booleanValue()) {
            if (bc != null) {
                bc.clearBody()
                pageContext.popBody()
            }
        } else if (!wasSilent) pageContext.unsetSilent()
    }

    @Override
    fun release() {
        super.release()
        bc = null
        bufferOutput = true
    }
}