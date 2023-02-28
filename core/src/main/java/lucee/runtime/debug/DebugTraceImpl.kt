/**
 * Copyright (c) 2014, the Railo Company Ltd.
 * Copyright (c) 2015, Lucee Assosication Switzerland
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
package lucee.runtime.debug

import lucee.commons.lang.StringUtil

class DebugTraceImpl(private val type: Int, private val category: String?, private val text: String?, private val template: String?, private val line: Int, action: String?, private val varName: String?, private val varValue: String?, time: Long) : DebugTrace {
    private val time: Long
    private val action: String?

    /**
     * @return the category
     */
    @Override
    fun getCategory(): String? {
        return category
    }

    /**
     * @return the line
     */
    @Override
    fun getLine(): Int {
        return line
    }

    /**
     * @return the template
     */
    @Override
    fun getTemplate(): String? {
        return template
    }

    /**
     * @return the text
     */
    @Override
    fun getText(): String? {
        return text
    }

    /**
     * @return the time
     */
    @Override
    fun getTime(): Long {
        return time
    }

    /**
     * @return the type
     */
    @Override
    fun getType(): Int {
        return type
    }

    /**
     * @return the var value
     */
    @Override
    fun getVarValue(): String? {
        return varValue
    }

    @Override
    fun getVarName(): String? {
        return varName
    }

    @Override
    fun getAction(): String? {
        return action
    }

    companion object {
        private const val serialVersionUID = -3619310656845433643L
        fun toType(type: String?, defaultValue: Int): Int {
            var type = type ?: return defaultValue
            type = type.toLowerCase().trim()
            if (type.startsWith("info")) return TYPE_INFO
            if (type.startsWith("debug")) return TYPE_DEBUG
            if (type.startsWith("warn")) return TYPE_WARN
            if (type.startsWith("error")) return TYPE_ERROR
            if (type.startsWith("fatal")) return TYPE_FATAL
            return if (type.startsWith("trace")) TYPE_TRACE else defaultValue
        }

        fun toType(type: Int, defaultValue: String?): String? {
            return when (type) {
                TYPE_INFO -> "INFO"
                TYPE_DEBUG -> "DEBUG"
                TYPE_WARN -> "WARN"
                TYPE_ERROR -> "ERROR"
                TYPE_FATAL -> "FATAL"
                TYPE_TRACE -> "TRACE"
                else -> defaultValue
            }
        }
    }

    init {
        this.time = if (time < 0) 0 else time
        this.action = StringUtil.emptyIfNull(action)
    }
}