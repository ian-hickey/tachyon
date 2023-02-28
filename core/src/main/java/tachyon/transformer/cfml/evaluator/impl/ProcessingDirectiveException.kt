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
package tachyon.transformer.cfml.evaluator.impl

import java.nio.charset.Charset

class ProcessingDirectiveException(cfml: SourceCode?, charset: Charset?, dotNotationUpperCase: Boolean?, writeLog: Boolean?) : TemplateException(cfml, createMessage(cfml, charset, writeLog!!)) {
    private val charset: CharSet?
    private val writeLog: Boolean?
    private val dotNotationUpperCase: Boolean?
    fun getCharset(): Charset? {
        return CharsetUtil.toCharset(charset)
    }

    fun getDotNotationUpperCase(): Boolean? {
        return dotNotationUpperCase
    }

    fun getWriteLog(): Boolean? {
        return writeLog
    }

    companion object {
        private fun createMessage(sc: SourceCode?, charset: Charset?, writeLog: Boolean): String? {
            val msg = StringBuffer()
            if (sc is PageSourceCode && !(sc as PageSourceCode?).getCharset().equals(charset)) msg.append("change charset from [" + (sc as PageSourceCode?).getCharset().toString() + "] to [" + charset.toString() + "].")
            if (sc.getWriteLog() !== writeLog) msg.append("change writelog from [" + sc.getWriteLog().toString() + "] to [" + writeLog.toString() + "].")
            return msg.toString()
        }
    }

    init {
        this.charset = CharsetUtil.toCharSet(charset)
        this.writeLog = writeLog
        this.dotNotationUpperCase = dotNotationUpperCase
    }
}