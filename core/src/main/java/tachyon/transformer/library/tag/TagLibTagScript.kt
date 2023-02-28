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
package tachyon.transformer.library.tag

import java.util.Iterator

class TagLibTagScript(tag: TagLibTag?) {
    private val tag: TagLibTag?
    private var rtexpr = false
    private var type = TYPE_NONE
    private var singleAttr: TagLibTagAttr? = UNDEFINED
    private var context = CTX_OTHER
    fun setType(type: Short) {
        this.type = type
    }

    fun setRtexpr(rtexpr: Boolean) {
        this.rtexpr = rtexpr
    }

    /**
     * @return the tag
     */
    fun getTag(): TagLibTag? {
        return tag
    }

    /**
     * @return the rtexpr
     */
    fun getRtexpr(): Boolean {
        return rtexpr
    }

    /**
     * @return the type
     */
    fun getType(): Short {
        return type
    }

    fun getSingleAttr(): TagLibTagAttr? {
        if (singleAttr === UNDEFINED) {
            singleAttr = null
            val it: Iterator<TagLibTagAttr?> = tag!!.getAttributes()!!.values().iterator()
            var attr: TagLibTagAttr?
            while (it.hasNext()) {
                attr = it.next()
                if (attr!!.getScriptSupport() !== TagLibTagAttr.SCRIPT_SUPPORT_NONE) {
                    singleAttr = attr
                    break
                }
            }
        }
        return singleAttr
    }

    fun setContext(str: String?) {
        var str = str
        if (!StringUtil.isEmpty(str, true)) {
            str = str.trim().toLowerCase()
            if ("none".equals(str)) context = CTX_NONE else if ("if".equals(str)) context = CTX_IF else if ("elseif".equals(str)) context = CTX_ELSE_IF else if ("else".equals(str)) context = CTX_ELSE else if ("for".equals(str)) context = CTX_FOR else if ("while".equals(str)) context = CTX_WHILE else if ("dowhile".equals(str)) context = CTX_DO_WHILE else if ("cfc".equals(str)) context = CTX_CFC else if ("component".equals(str)) context = CTX_CFC else if ("class".equals(str)) context = CTX_CFC else if ("interface".equals(str)) context = CTX_INTERFACE else if ("function".equals(str)) context = CTX_FUNCTION else if ("block".equals(str)) context = CTX_BLOCK else if ("finally".equals(str)) context = CTX_FINALLY else if ("switch".equals(str)) context = CTX_SWITCH else if ("try".equals(str)) context = CTX_TRY else if ("catch".equals(str)) context = CTX_CATCH else if ("transaction".equals(str)) context = CTX_TRANSACTION else if ("thread".equals(str)) context = CTX_THREAD else if ("savecontent".equals(str)) context = CTX_SAVECONTENT else if ("lock".equals(str)) context = CTX_LOCK else if ("loop".equals(str)) context = CTX_LOOP else if ("query".equals(str)) context = CTX_QUERY else if ("zip".equals(str)) context = CTX_ZIP
        }
    }

    /**
     * @return the context
     */
    fun getContext(): Short {
        return context
    }

    companion object {
        const val TYPE_NONE: Short = 0
        const val TYPE_SINGLE: Short = 1
        const val TYPE_MULTIPLE: Short = 2
        const val CTX_OTHER: Short = -1
        const val CTX_NONE: Short = 0
        const val CTX_IF: Short = 1
        const val CTX_ELSE_IF: Short = 2
        const val CTX_ELSE: Short = 3
        const val CTX_FOR: Short = 4
        const val CTX_WHILE: Short = 5
        const val CTX_DO_WHILE: Short = 6
        const val CTX_CFC: Short = 7
        const val CTX_INTERFACE: Short = 8
        const val CTX_FUNCTION: Short = 9
        const val CTX_BLOCK: Short = 10
        const val CTX_FINALLY: Short = 11
        const val CTX_SWITCH: Short = 12
        const val CTX_TRY: Short = 13
        const val CTX_CATCH: Short = 14
        const val CTX_TRANSACTION: Short = 15
        const val CTX_THREAD: Short = 16
        const val CTX_SAVECONTENT: Short = 17
        const val CTX_LOCK: Short = 18
        const val CTX_LOOP: Short = 19
        const val CTX_QUERY: Short = 20
        const val CTX_ZIP: Short = 21
        const val CTX_STATIC: Short = 22
        private val UNDEFINED: TagLibTagAttr? = TagLibTagAttr(null)
        fun toType(type: String?, defaultValue: Short): Short {
            var type = type
            if (!StringUtil.isEmpty(type, true)) {
                type = type.trim().toLowerCase()
                if ("single".equals(type)) return TYPE_SINGLE else if ("multiple".equals(type)) return TYPE_MULTIPLE else if ("none".equals(type)) return TYPE_NONE
            }
            return defaultValue
        }

        fun toType(type: Short, defaultValue: String?): String? {
            if (type == TYPE_MULTIPLE) return "multiple"
            if (type == TYPE_SINGLE) return "single"
            return if (type == TYPE_NONE) "none" else defaultValue
        }
    }

    init {
        this.tag = tag
    }
}