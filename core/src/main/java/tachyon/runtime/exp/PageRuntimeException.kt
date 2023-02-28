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

import tachyon.runtime.PageContext

/**
 *
 *
 * To change the template for this generated type comment go to Window - Preferences - Java - Code
 * Generation - Code and Comments
 */
class PageRuntimeException : RuntimeException, IPageException, PageExceptionBox {
    private var pe: PageException?

    constructor(t: Throwable?) : super(t.getMessage(), t) {
        pe = Caster.toPageException(t)
    }

    /**
     * constructor of the class
     *
     * @param pe page exception to hold
     */
    constructor(pe: PageException?) : super(pe.getMessage(), pe) {
        setStackTrace(pe.getStackTrace())
        this.pe = pe
    }

    /**
     * standart excption constructor
     *
     * @param message message of the exception
     */
    constructor(message: String?) : super(message) {
        pe = ApplicationException(message)
    }

    /**
     * standart excption constructor
     *
     * @param message message of the exception
     * @param detail detailed information to the exception
     */
    constructor(message: String?, detail: String?) : super(message) {
        pe = ApplicationException(message, detail)
    }

    @get:Override
    @set:Override
    var detail: String?
        get() = pe.getDetail()
        set(detail) {
            pe.setDetail(detail)
        }

    @get:Override
    @set:Override
    var errorCode: String?
        get() = pe.getErrorCode()
        set(errorCode) {
            pe.setErrorCode(errorCode)
        }

    @get:Override
    @set:Override
    var extendedInfo: String?
        get() = pe.getExtendedInfo()
        set(extendedInfo) {
            pe.setExtendedInfo(extendedInfo)
        }

    @Override
    fun getCatchBlock(config: Config?): CatchBlock? {
        return pe.getCatchBlock(config)
    }

    @Override
    fun getCatchBlock(pc: PageContext?): Struct? {
        return pe.getCatchBlock(pc.getConfig())
    }

    // TLPC
    val catchBlock: Struct?
        get() =// TLPC
            pe.getCatchBlock(ThreadLocalPageContext.getConfig())

    @Override
    fun getErrorBlock(pc: PageContext?, ep: ErrorPage?): Struct? {
        return pe.getErrorBlock(pc, ep)
    }

    @Override
    fun addContext(template: PageSource?, line: Int, column: Int, ste: StackTraceElement?) {
        pe.addContext(template, line, column, ste)
    }

    @Override
    fun toDumpData(pageContext: PageContext?, maxlevel: Int, dp: DumpProperties?): DumpData? {
        return pe.toDumpData(pageContext, maxlevel, dp)
    }

    @get:Override
    val pageException: PageException?
        get() = pe

    @Override
    fun typeEqual(type: String?): Boolean {
        return pe.typeEqual(type)
    }

    @get:Override
    val typeAsString: String?
        get() = pe.getTypeAsString()

    @get:Override
    val customTypeAsString: String?
        get() = pe.getCustomTypeAsString()

    @get:Override
    @set:Override
    var tracePointer: Int
        get() = pe.getTracePointer()
        set(tracePointer) {
            pe.setTracePointer(tracePointer)
        }

    @get:Override
    val additional: Struct?
        get() = pe.getAdditional()

    @get:Override
    val addional: Struct?
        get() = pe.getAdditional()

    @get:Override
    val stackTraceAsString: String?
        get() = pe.getStackTraceAsString()
}