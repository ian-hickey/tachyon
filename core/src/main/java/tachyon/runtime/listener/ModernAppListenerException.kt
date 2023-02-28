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
package tachyon.runtime.listener

import java.io.PrintStream

class ModernAppListenerException(pe: PageException?, eventName: String?) : PageException(pe.getMessage()) {
    private val rootCause: PageException?
    private val eventName: String?
    @Override
    fun addContext(pageSource: PageSource?, line: Int, column: Int, ste: StackTraceElement?) {
        rootCause.addContext(pageSource, line, column, ste)
    }

    @Override
    fun getAdditional(): Struct? {
        return rootCause.getAdditional()
    }

    @Override
    fun getAddional(): Struct? {
        return rootCause.getAdditional()
    }

    fun getCatchBlock(): Struct? {
        return getCatchBlock(ThreadLocalPageContext.getConfig())
    }

    @Override
    fun getCatchBlock(pc: PageContext?): Struct? {
        return getCatchBlock(pc.getConfig())
    }

    @Override
    fun getCatchBlock(config: Config?): CatchBlock? {
        val cb: CatchBlock = rootCause.getCatchBlock(config)
        val cause: Collection = Duplicator.duplicate(cb, false)
        // rtn.setEL("message", getMessage());
        if (!cb.containsKey(KeyConstants._detail)) cb.setEL(KeyConstants._detail, "Exception thrown while invoking function [$eventName] in application event handler ")
        cb.setEL(ROOT_CAUSE, cause)
        cb.setEL(CAUSE, cause)
        // cb.setEL("stacktrace", getStackTraceAsString());
        // rtn.setEL("tagcontext", new ArrayImpl());
        // rtn.setEL("type", getTypeAsString());
        cb.setEL(KeyConstants._name, eventName)
        return cb
    }

    @Override
    fun getCustomTypeAsString(): String? {
        return rootCause.getCustomTypeAsString()
    }

    @Override
    fun getDetail(): String? {
        return rootCause.getDetail()
    }

    @Override
    fun getErrorBlock(pc: PageContext?, ep: ErrorPage?): Struct? {
        return rootCause.getErrorBlock(pc, ep)
    }

    @Override
    fun getErrorCode(): String? {
        return rootCause.getErrorCode()
    }

    @Override
    fun getExtendedInfo(): String? {
        return rootCause.getExtendedInfo()
    }

    @Override
    fun getStackTraceAsString(): String? {
        return rootCause.getStackTraceAsString()
    }

    @Override
    fun getTracePointer(): Int {
        return rootCause.getTracePointer()
    }

    @Override
    fun getTypeAsString(): String? {
        return rootCause.getTypeAsString()
    }

    @Override
    fun setDetail(detail: String?) {
        rootCause.setDetail(detail)
    }

    @Override
    fun setErrorCode(errorCode: String?) {
        rootCause.setErrorCode(errorCode)
    }

    @Override
    fun setExtendedInfo(extendedInfo: String?) {
        rootCause.setExtendedInfo(extendedInfo)
    }

    @Override
    fun setTracePointer(tracePointer: Int) {
        rootCause.setTracePointer(tracePointer)
    }

    @Override
    fun typeEqual(type: String?): Boolean {
        return rootCause.equals(type)
    }

    @Override
    fun toDumpData(pageContext: PageContext?, maxlevel: Int, dp: DumpProperties?): DumpData? {
        return rootCause.toDumpData(pageContext, maxlevel, dp)
    }

    /**
     * @return the eventName
     */
    fun getEventName(): String? {
        return eventName
    }

    fun getLine(config: Config?): String? {
        return (rootCause as PageExceptionImpl?).getLine(config)
    }

    @Override
    fun getRootCause(): Throwable? {
        return rootCause.getRootCause()
    }

    @Override
    fun getStackTrace(): Array<StackTraceElement?>? {
        return rootCause.getStackTrace()
    }

    @Override
    fun printStackTrace() {
        rootCause.printStackTrace()
    }

    @Override
    fun printStackTrace(s: PrintStream?) {
        rootCause.printStackTrace(s)
    }

    @Override
    fun printStackTrace(s: PrintWriter?) {
        rootCause.printStackTrace(s)
    }

    fun getPageException(): PageException? {
        return rootCause
    }

    @Override
    fun setExposeMessage(exposeMessage: Boolean) {
        rootCause.setExposeMessage(exposeMessage)
    }

    @Override
    fun getExposeMessage(): Boolean {
        return rootCause.getExposeMessage()
    }

    companion object {
        private val ROOT_CAUSE: Collection.Key? = KeyConstants._rootCause
        private val CAUSE: Collection.Key? = KeyConstants._cause
    }

    /**
     * Constructor of the class
     *
     * @param pe
     * @param eventName
     */
    init {
        setStackTrace(pe.getStackTrace())
        rootCause = pe
        this.eventName = eventName
    }
}