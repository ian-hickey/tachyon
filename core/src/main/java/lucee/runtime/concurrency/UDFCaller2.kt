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
package lucee.runtime.concurrency

import java.io.ByteArrayInputStream

class UDFCaller2<P> private constructor(parent: PageContext?) : Callable<Data<P?>?> {
    private val parent: PageContext?
    private var pc: PageContextImpl? = null
    private val baos: ByteArrayOutputStream?
    private var udf: UDF? = null
    private var doIncludePath = false
    private var arguments: Array<Object?>?
    private var namedArguments: Struct? = null
    private var passed: P? = null

    constructor(parent: PageContext?, udf: UDF?, arguments: Array<Object?>?, passed: P?, doIncludePath: Boolean) : this(parent) {
        this.udf = udf
        this.arguments = arguments
        this.doIncludePath = doIncludePath
        this.passed = passed
    }

    constructor(parent: PageContext?, udf: UDF?, namedArguments: Struct?, passed: P?, doIncludePath: Boolean) : this(parent) {
        this.udf = udf
        this.namedArguments = namedArguments
        this.doIncludePath = doIncludePath
        this.passed = passed
    }

    @Override
    @Throws(PageException::class)
    fun call(): Data<P?>? {
        if (pc == null) {
            ThreadLocalPageContext.register(parent)
            pc = ThreadUtil.clonePageContext(parent, baos, false, false, false)
        }
        ThreadLocalPageContext.register(pc)
        pc.getRootOut().setAllowCompression(false) // make sure content is not compressed
        var str: String? = null
        var result: Object? = null
        result = try {
            if (namedArguments != null) udf.callWithNamedValues(pc, namedArguments, doIncludePath) else udf.call(pc, arguments, doIncludePath)
        } finally {
            try {
                val rsp: HttpServletResponseDummy = pc.getHttpServletResponse() as HttpServletResponseDummy
                val cs: Charset = ReqRspUtil.getCharacterEncoding(pc, rsp)
                // if(enc==null) enc="ISO-8859-1";
                pc.getOut().flush() // make sure content is flushed
                pc.getConfig().getFactory().releasePageContext(pc)
                str = IOUtil.toString(ByteArrayInputStream(baos.toByteArray()), cs) // TODO add support for none string content
            } catch (e: Exception) {
                LogUtil.log(pc, "loading", e)
            }
        }
        return Data<P?>(str, result, passed)
    }

    init {
        this.parent = parent
        baos = ByteArrayOutputStream()
    }
}