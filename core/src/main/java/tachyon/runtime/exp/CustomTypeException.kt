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

import java.util.ArrayList

/**
 * Exception thrown by CFML Code
 */
class CustomTypeException(message: String?, detail: String?, errorCode: String?, customType: String?, extendedinfo: String?, entryLevel: Int) : PageExceptionImpl(message, "custom_type", customType) {
    private val entryLevel: Int

    /**
     * constructor of the Exception
     *
     * @param message Exception Message
     * @param detail Detailed Exception Message
     * @param errorCode Error Code
     * @param customType Type of the Exception
     * @param entryLevel
     */
    constructor(message: String?, detail: String?, errorCode: String?, customType: String?, extendedinfo: String?) : this(message, detail, errorCode, customType, extendedinfo, 1) {}

    @get:Override
    override val stackTrace: Array<Any?>?
        get() {
            val list: List<StackTraceElement?> = ArrayList<StackTraceElement?>()
            val elements: Array<StackTraceElement?> = super.getStackTrace()
            var element: StackTraceElement?
            var template: String
            var level = 0
            for (i in elements.indices) {
                element = elements[i]
                template = element.getFileName()
                if (level < entryLevel && (element.getLineNumber() <= 0 || template == null || ResourceUtil.getExtension(template, "").equals("java"))) continue
                if (++level >= entryLevel) {
                    list.add(element)
                }
            }
            return if (list.size() === 0) elements else list.toArray(arrayOfNulls<StackTraceElement?>(list.size()))
        }

    @Override
    override fun getCatchBlock(config: Config?): CatchBlock? {
        val cb: CatchBlock = super.getCatchBlock(config)
        cb.setEL(KeyConstants._code, cb.get(KeyConstants._errorcode, null))
        cb.setEL(KeyConstants._type, getCustomTypeAsString())
        val ei: String = getExtendedInfo()
        if (ei != null) cb.setEL(KeyConstants._extended_info, ei)
        // cb.setEL("ErrorCode","");
        return cb
    }

    @Override
    override fun getErrorBlock(pc: PageContext?, ep: ErrorPage?): Struct? {
        val eb: Struct = super.getErrorBlock(pc, ep)
        eb.setEL(KeyConstants._type, getCustomTypeAsString())
        return eb
    }

    @Override
    override fun typeEqual(type: String?): Boolean {
        var type = type ?: return true
        type = type.toLowerCase().trim()
        if (type.equals("any")) return true

        // Custom Type
        return if (getTypeAsString().equals("custom_type") || getTypeAsString().equals("customtype")) {
            compareCustomType(type, getCustomTypeAsString().toLowerCase().trim())
        } else super.typeEqual(type)
    }

    /**
     * @param leftType
     * @param rightType
     * @return is same custom type
     */
    private fun compareCustomType(leftType: String?, rightType: String?): Boolean {
        val left: Int = leftType!!.length()
        val right: Int = rightType!!.length()
        if (left > right) return false
        if (left == right) return leftType.equals(rightType)
        for (i in 0 until left) {
            if (leftType.charAt(i) !== rightType.charAt(i)) return false
        }
        return rightType.charAt(left) === '.'
    }

    companion object {
        private const val serialVersionUID = 949287085391895177L
    }

    init {
        setDetail(detail)
        setErrorCode(errorCode)
        extendedinfo?.let { setExtendedInfo(it) }
        this.entryLevel = entryLevel
    }
}