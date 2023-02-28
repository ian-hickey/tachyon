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
package lucee.commons.io.res.util

import java.io.IOException

/**
 * this class holds information about a snippet from text with its start and end line numbers
 */
class ResourceSnippet(text: String?, startLine: Int, endLine: Int) : java.io.Serializable {
    /** returns the actual text of the snippet  */
    var content: String? = null
    /** returns the start line number  */
    val startLine = 0
    /** returns the end line number  */
    val endLine = 0

    companion object {
        val Empty = ResourceSnippet("", 0, 0)
        fun getContents(`is`: InputStream?, charset: String?): String {
            val result: String
            val scanner: java.util.Scanner = Scanner(`is`, charset).useDelimiter("\\A")
            result = if (scanner.hasNext()) scanner.next() else ""
            if (`is` != null) try {
                `is`.close()
            } catch (ex: IOException) {
            }
            return result
        }

        fun getContents(res: Resource, charset: String?): String {
            return try {
                getContents(res.getInputStream(), charset)
            } catch (ex: IOException) {
                ""
            }
        }

        fun createResourceSnippet(src: String?, startChar: Int, endChar: Int): ResourceSnippet {
            var text: String? = ""
            if (endChar > startChar && endChar <= src!!.length()) text = src.substring(startChar, endChar)
            return ResourceSnippet(text, getLineNumber(src, startChar), getLineNumber(src, endChar))
        }

        /**
         * extract a ResourceSnippet from InputStream at the given char positions
         *
         * @param is - InputStream of the Resource
         * @param startChar - start position of the snippet
         * @param endChar - end position of the snippet
         * @param charset - use server's charset, default should be UTF-8
         * @return
         */
        fun createResourceSnippet(`is`: InputStream?, startChar: Int, endChar: Int, charset: String?): ResourceSnippet {
            return createResourceSnippet(getContents(`is`, charset), startChar, endChar)
        }

        /**
         * extract a ResourceSnippet from a Resource at the given char positions
         *
         * @param res - Resource from which to extract the snippet
         * @param startChar - start position of the snippet
         * @param endChar - end position of the snippet
         * @param charset - use server's charset, default should be UTF-8
         * @return
         */
        fun createResourceSnippet(res: Resource, startChar: Int, endChar: Int, charset: String?): ResourceSnippet {
            return try {
                createResourceSnippet(res.getInputStream(), startChar, endChar, charset)
            } catch (ex: IOException) {
                Empty
            }
        }

        /** returns the line number of the given char in the text  */
        fun getLineNumber(text: String?, posChar: Int): Int {
            val len: Int = Math.min(posChar, text!!.length())
            var result = 1
            for (i in 0 until len) {
                if (text.charAt(i) === '\n') result++
            }
            return result
        }
    }

    init {
        content = text
        this.startLine = startLine
        this.endLine = endLine
    }
}