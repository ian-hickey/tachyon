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
package tachyon.runtime.tag

import java.util.Iterator

/**
 * The cfthrow tag raises a developer-specified exception that can be caught with cfcatch tag having
 * any of the following type specifications - cfcatch type = 'custom_type', cfcatch type =
 * 'Application' 'cfcatch' type = 'Any'
 *
 *
 *
 */
class Throw : TagImpl() {
    /** A custom error code that you supply.  */
    private var extendedinfo: String? = null
    private var type: String? = "application"
    private var detail: String? = ""

    /** A message that describes the exceptional event.  */
    private var message: Object? = null

    /** A custom error code that you supply.  */
    private var errorcode: String? = ""
    private var `object`: Object? = null
    private var level = 1
    @Override
    fun release() {
        super.release()
        extendedinfo = null
        type = "application"
        detail = ""
        message = null
        errorcode = ""
        `object` = null
        level = 1
    }

    /**
     * set the value extendedinfo A custom error code that you supply.
     *
     * @param extendedinfo value to set
     */
    fun setExtendedinfo(extendedinfo: String?) {
        this.extendedinfo = extendedinfo
    }

    /**
     * set the value type
     *
     * @param type value to set
     */
    fun setType(type: String?) {
        this.type = type
    }

    /**
     * set the value detail
     *
     * @param detail value to set
     */
    fun setDetail(detail: String?) {
        this.detail = detail
    }

    /**
     * set the value message A message that describes the exceptional event.
     *
     * @param message value to set
     */
    fun setMessage(message: Object?) {
        this.message = message
    }

    @Deprecated
    @Deprecated("this method should no longer be used.")
    fun setMessage(message: String?) {
        this.message = message
    }

    /**
     * set the value errorcode A custom error code that you supply.
     *
     * @param errorcode value to set
     */
    fun setErrorcode(errorcode: String?) {
        this.errorcode = errorcode
    }

    /**
     * set the value object a native java exception Object, if this attribute is defined all other will
     * be ignored.
     *
     * @param object object to set
     * @throws PageException
     */
    @Throws(PageException::class)
    fun setObject(`object`: Object?) {
        this.`object` = `object`
    }

    fun setContextlevel(level: Double) {
        this.level = level.toInt()
    }

    @Override
    @Throws(PageException::class)
    fun doStartTag(): Int {
        _doStartTag(message)
        _doStartTag(`object`)
        throw CustomTypeException("", detail, errorcode, type, extendedinfo, level)
    }

    @Throws(PageException::class)
    private fun _doStartTag(obj: Object?) {
        if (!StringUtil.isEmpty(obj)) {
            val pe: PageException? = toPageException(obj, null)
            if (pe != null) throw pe
            val exception = CustomTypeException(Caster.toString(obj), detail, errorcode, type, extendedinfo, level)
            throw exception
        }
    }

    @Override
    fun doEndTag(): Int {
        return EVAL_PAGE
    }

    companion object {
        @Throws(PageException::class)
        fun toPageException(`object`: Object?, defaultValue: PageException?): PageException? {
            if (`object` is ObjectWrap) return toPageException((`object` as ObjectWrap?).getEmbededObject(), defaultValue)
            if (`object` is CatchBlock) {
                val cb: CatchBlock? = `object` as CatchBlock?
                return cb.getPageException()
            }
            if (`object` is PageException) return `object` as PageException?
            if (`object` is Throwable) {
                val t = `object` as Throwable?
                return CustomTypeException(t.getMessage(), "", "", t.getClass().getName(), "")
            }
            if (`object` is Struct) {
                var sct: Struct? = `object` as Struct?
                val type: String = Caster.toString(sct.get(KeyConstants._type, ""), "").trim()
                val msg: String = Caster.toString(sct.get(KeyConstants._message, null), null)
                if (!StringUtil.isEmpty(msg, true)) {
                    val detail: String = Caster.toString(sct.get(KeyConstants._detail, null), null)
                    val errCode: String = Caster.toString(sct.get("ErrorCode", null), null)
                    val extInfo: String = Caster.toString(sct.get("ExtendedInfo", null), null)
                    var pe: PageException? = null
                    if ("application".equalsIgnoreCase(type)) pe = ApplicationException(msg, detail) else if ("expression".equalsIgnoreCase(type)) pe = ExpressionException(msg, detail) else pe = CustomTypeException(msg, detail, errCode, type, extInfo)

                    // Extended Info
                    if (!StringUtil.isEmpty(extInfo, true)) pe.setExtendedInfo(extInfo)

                    // Error Code
                    if (!StringUtil.isEmpty(errCode, true)) pe.setErrorCode(errCode)

                    // Additional
                    if (pe is PageExceptionImpl) {
                        val pei: PageExceptionImpl? = pe as PageExceptionImpl?
                        sct = Caster.toStruct(sct.get("additional", null), null)
                        if (sct != null) {
                            val it: Iterator<Entry<Key?, Object?>?> = sct.entryIterator()
                            var e: Entry<Key?, Object?>?
                            while (it.hasNext()) {
                                e = it.next()
                                pei.setAdditional(e.getKey(), e.getValue())
                            }
                        }
                    }
                    return pe
                }
            }
            return defaultValue
        }
    }
}