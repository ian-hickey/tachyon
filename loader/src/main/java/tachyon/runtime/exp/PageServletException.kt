/**
 * Copyright (c) 2014, the Railo Company Ltd.
 * Copyright (c) 2015, Tachyon Assosication Switzerland
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

import javax.servlet.ServletException

/**
 * by definition a JSP Tag can only throw JSPExceptions, for that case the PageException is a
 * Subclass of the JSPExceptions, but when a PageException, is escaleted to a parent page, this goes
 * over the include method of the PageContext Object, but this can only throw ServletException. For
 * that this class can Box a JSPException (PageException) in a ServletException
 * (PageServletException)
 */
class PageServletException(pe: PageException) : ServletException(pe.getMessage()), IPageException, PageExceptionBox {
    private val pe: PageException

    /**
     * @see tachyon.runtime.exp.PageExceptionBox.getPageException
     */
    @get:Override
    override val pageException: tachyon.runtime.exp.PageException?
        get() = pe
    /**
     * @see tachyon.runtime.exp.IPageException.getDetail
     */
    /**
     * @see tachyon.runtime.exp.IPageException.setDetail
     */
    @get:Override
    @set:Override
    override var detail: String?
        get() = pe.getDetail()
        set(detail) {
            pe.setDetail(detail)
        }
    /**
     * @see tachyon.runtime.exp.IPageException.getErrorCode
     */
    /**
     * @see tachyon.runtime.exp.IPageException.setErrorCode
     */
    @get:Override
    @set:Override
    override var errorCode: String?
        get() = pe.getErrorCode()
        set(errorCode) {
            pe.setErrorCode(errorCode)
        }
    /**
     * @see tachyon.runtime.exp.IPageException.getExtendedInfo
     */
    /**
     * @see tachyon.runtime.exp.IPageException.setExtendedInfo
     */
    @get:Override
    @set:Override
    override var extendedInfo: String?
        get() = pe.getExtendedInfo()
        set(extendedInfo) {
            pe.setExtendedInfo(extendedInfo)
        }

    /**
     *
     * @see tachyon.runtime.exp.IPageException.getCatchBlock
     */
    @Override
    override fun getCatchBlock(pc: PageContext): Struct {
        return pe.getCatchBlock(pc.getConfig())
    }

    /**
     *
     * @see tachyon.runtime.exp.IPageException.getCatchBlock
     */
    @Override
    override fun getCatchBlock(config: Config?): CatchBlock {
        return pe.getCatchBlock(config)
    }

    /**
     * @see tachyon.runtime.exp.IPageException.getErrorBlock
     */
    @Override
    override fun getErrorBlock(pc: PageContext?, ep: ErrorPage?): Struct {
        return pe.getErrorBlock(pc, ep)
    }

    /**
     * @see tachyon.runtime.exp.IPageException.addContext
     */
    @Override
    override fun addContext(template: PageSource?, line: Int, column: Int, ste: StackTraceElement?) {
        pe.addContext(template, line, column, ste)
    }

    /**
     * @see tachyon.runtime.dump.Dumpable.toDumpData
     */
    @Override
    fun toDumpData(pageContext: PageContext?, maxlevel: Int, dp: DumpProperties?): DumpData {
        return pe.toDumpData(pageContext, maxlevel, dp)
    }

    /**
     * @see tachyon.runtime.exp.IPageException.getTypeAsString
     */
    @get:Override
    override val typeAsString: String?
        get() = pe.getTypeAsString()

    /**
     * @see tachyon.runtime.exp.IPageException.typeEqual
     */
    @Override
    override fun typeEqual(type: String?): Boolean {
        return pe.typeEqual(type)
    }

    /**
     * @see tachyon.runtime.exp.IPageException.getCustomTypeAsString
     */
    @get:Override
    override val customTypeAsString: String?
        get() = pe.getCustomTypeAsString()
    /*
	 * *
	 * 
	 * @see tachyon.runtime.exp.IPageException#getLine() / public String getLine() { return pe.getLine();
	 * }
	 */
    /**
     * @see tachyon.runtime.exp.IPageException.getTracePointer
     */
    /**
     * @see tachyon.runtime.exp.IPageException.setTracePointer
     */
    @get:Override
    @set:Override
    override var tracePointer: Int
        get() = pe.getTracePointer()
        set(tracePointer) {
            pe.setTracePointer(tracePointer)
        }

    /**
     * @see tachyon.runtime.exp.IPageException.getAdditional
     */
    @get:Override
    override val additional: Struct
        get() = pe.getAdditional()

    @get:Override
    override val addional: Struct
        get() = pe.getAdditional()

    /**
     * @see tachyon.runtime.exp.IPageException.getStackTraceAsString
     */
    @get:Override
    override val stackTraceAsString: String?
        get() = pe.getStackTraceAsString()

    companion object {
        private const val serialVersionUID = -3654238294705464067L
    }

    /**
     * constructor of the class
     *
     * @param pe page exception to hold
     */
    init {
        this.pe = pe
    }
}