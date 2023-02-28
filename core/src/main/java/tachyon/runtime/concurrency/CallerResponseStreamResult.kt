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
package tachyon.runtime.concurrency

import java.io.ByteArrayInputStream

abstract class CallerResponseStreamResult(parent: PageContext?) : Callable<String?> {
    private val parent: PageContext?
    private val pc: PageContextImpl?
    private val baos: ByteArrayOutputStream?
    @Override
    @Throws(PageException::class)
    fun call(): String? {
        ThreadLocalPageContext.register(pc)
        pc.getRootOut().setAllowCompression(false) // make sure content is not compressed
        var str: String? = null
        try {
            _call(parent, pc)
        } finally {
            try {
                val rsp: HttpServletResponseDummy = pc.getHttpServletResponse() as HttpServletResponseDummy
                val cs: Charset = ReqRspUtil.getCharacterEncoding(pc, rsp)
                // if(enc==null) enc="ISO-8859-1";
                pc.getOut().flush() // make sure content is flushed
                pc.getConfig().getFactory().releasePageContext(pc)
                str = IOUtil.toString(ByteArrayInputStream(baos.toByteArray()), cs) // TODO add support for none string content
            } catch (e: Exception) {
                LogUtil.log(pc, "concurrency", e)
            }
        }
        return str
    }

    @Throws(PageException::class)
    abstract fun _call(parent: PageContext?, pc: PageContext?) // public abstract void afterCleanup(PageContext parent, ByteArrayOutputStream baos);

    init {
        this.parent = parent
        baos = ByteArrayOutputStream()
        pc = ThreadUtil.clonePageContext(parent, baos, false, false, false)
    }
}