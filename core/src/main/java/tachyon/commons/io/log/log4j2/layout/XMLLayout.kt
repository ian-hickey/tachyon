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
package tachyon.commons.io.log.log4j2.layout

import java.nio.charset.Charset

class XMLLayout     // private static final DateFormat dateFormat = new DateFormat(Locale.US);
// private static final TimeFormat timeFormat = new TimeFormat(Locale.US);
(cs: Charset, complete: Boolean, private val doLocationInfo: Boolean) : AbstractStringLayout(cs, createHeader(cs, complete), createFooter(cs, complete)) {
    @get:Override
    val contentType: String
        get() = "text/xml; charset=" + this.getCharset()

    // result.put("dtd", "log4j-events.dtd");
    @get:Override
    val contentFormat: Map<String, String>
        get() {
            val result: Map<String, String> = HashMap()
            // result.put("dtd", "log4j-events.dtd");
            result.put("xsd", "log4j-events.xsd")
            result.put("version", "2.0")
            return result
        }

    @Override
    fun toSerializable(event: LogEvent): String {
        val buf = StringBuilder()
        buf.append("	<log4j:event logger=\"")
        buf.append(event.getLoggerName())
        buf.append("\" timestamp=\"")
        buf.append(System.currentTimeMillis())
        buf.append("\" level=\"")
        buf.append(event.getLevel().name())
        buf.append("\" thread=\"")
        buf.append(Thread.currentThread().getName())
        buf.append("\">")
        buf.append(LINE_SEPARATOR)
        buf.append("		<log4j:message>")
        buf.append(createCDATASection(event.getMessage().toString())) // TODO cdata escape
        buf.append("</log4j:message>")
        buf.append(LINE_SEPARATOR)
        if (doLocationInfo) {
            var data: StackTraceElement? = null
            for (ste in Thread.currentThread().getStackTrace()) {
                if (ste.getClassName().startsWith("tachyon.commons.io.log.")) continue
                if (ste.getClassName().startsWith("org.apache.logging.log4j.")) continue
                if (ste.getClassName().equals("tachyon.runtime.tag.Log")) continue
                data = ste
            }
            if (data != null) {
                buf.append("		<log4j:locationInfo class=\"")
                buf.append(data.getClassName())
                buf.append("\" method=\"")
                buf.append(data.getMethodName())
                buf.append("\" file=\"")
                buf.append("LogAppender.java")
                buf.append("\" line=\"")
                buf.append(data.getLineNumber())
                buf.append("\"/>")
                buf.append(LINE_SEPARATOR)
            }
        }
        buf.append("	</log4j:event>")
        buf.append(LINE_SEPARATOR)
        return buf.toString()
    }

    companion object {
        // TODO <Serializable>
        private val LINE_SEPARATOR: String = System.getProperty("line.separator")
        private const val ROOT_TAG = "Events"
        private const val XML_NAMESPACE = "http://logging.apache.org/log4j/2.0/events"
        private fun createHeader(cs: Charset, complete: Boolean): ByteArray? {
            if (!complete) {
                return null
            }
            // .getBytes(CharsetUtil.UTF8)
            val buf = StringBuilder()
            buf.append("<?xml version=\"1.0\" encoding=\"")
            buf.append(cs.name())
            buf.append("\"?>")
            buf.append(LINE_SEPARATOR)
            buf.append('<')
            buf.append(ROOT_TAG)
            buf.append(" xmlns=\"" + XML_NAMESPACE + "\">")
            buf.append(LINE_SEPARATOR)
            return buf.toString().getBytes(cs)
        }

        private fun createFooter(cs: Charset, complete: Boolean): ByteArray? {
            if (!complete) {
                return null
            }
            val buf = StringBuilder()
            buf.append("</")
            buf.append(ROOT_TAG)
            buf.append('>')
            buf.append(LINE_SEPARATOR)
            return buf.toString().getBytes(cs)
        }

        private fun createCDATASection(str: String): String {
            val buf = StringBuilder("<![CDATA[")
            var index: Int
            var lastIndex = 0
            while (str.indexOf("]]>", lastIndex).also { index = it } != -1) {
                buf.append(str.substring(lastIndex, index)).append("]]]]><![CDATA[>")
                lastIndex = index + 3
            }
            return buf.append(str.substring(lastIndex)).append("]]>").toString()
        }
    }
}