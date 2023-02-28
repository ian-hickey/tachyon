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
package lucee.commons.io.log.log4j2.layout

import java.util.Locale

class ClassicLayout  // TODO <Serializable>
    : AbstractStringLayout(CharsetUtil.UTF8, ("\"Severity\",\"ThreadID\",\"Date\",\"Time\",\"Application\",\"Message\"" + LINE_SEPARATOR).getBytes(CharsetUtil.UTF8), ByteArray(0)) {
    // TODO Auto-generated method stub
    @get:Override
    val contentType: String
        get() =// TODO Auto-generated method stub
            super.getContentType()

    @Override
    fun toSerializable(event: LogEvent): String {
        val data = StringBuilder()
        val application: String
        var msg: String? = Caster.toString(event.getMessage(), null)
        val index: Int = msg.indexOf("->")
        if (index > -1) {
            application = msg.substring(0, index)
            msg = msg.substring(index + 2)
        } else application = ""

        // if(!ArrayUtil.isEmpty(params))
        // application=Caster.toString(params[0],"");
        // Severity
        data.append('"')
        data.append(event.getLevel().toString())
        data.append('"')
        data.append(',')
        data.append('"')
        data.append(event.getThreadName())
        data.append('"')
        data.append(',')

        // Date
        data.append('"')
        data.append(dateFormat.format(event.getTimeMillis(), "mm/dd/yyyy", TimeZone.getDefault()))
        data.append('"')
        data.append(',')

        // Time
        data.append('"')
        data.append(timeFormat.format(event.getTimeMillis(), "HH:mm:ss", TimeZone.getDefault()))
        data.append('"')
        data.append(',')

        // Application
        data.append('"')
        data.append(StringUtil.replace(application, "\"", "\"\"", false))
        data.append('"')
        data.append(',')

        // Message
        data.append('"')
        if (msg == null && event.getMessage() != null) msg = event.getMessage().toString()
        msg = StringUtil.replace(msg, "\"", "\"\"", false)
        data.append(msg)
        val t: Throwable = event.getThrown()
        if (t != null) {
            data.append(';')
            val em: String = StringUtil.replace(ExceptionUtil.getMessage(t), "\"", "\"\"", false)
            if (!em.equals(msg)) {
                data.append(em)
                data.append(';')
            }
            val est: String = ExceptionUtil.getStacktrace(t, false, true)
            data.append(StringUtil.replace(est, "\"", "\"\"", false))
        }
        data.append('"')
        return data.append(LINE_SEPARATOR).toString()
    }

    companion object {
        private val LINE_SEPARATOR: String = System.getProperty("line.separator")
        private val dateFormat: DateFormat = DateFormat(Locale.US)
        private val timeFormat: TimeFormat = TimeFormat(Locale.US)
    }
}