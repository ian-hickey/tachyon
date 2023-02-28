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
package tachyon.runtime.security

import java.util.Iterator

/**
 * Script-protect to remove cross-attacks from strings
 */
object ScriptProtect {
    val invalids: Array<String?>? = arrayOf("object", "embed", "script", "applet", "meta", "iframe")

    /**
     * translate all strig values of the struct i script-protected form
     *
     * @param sct Struct to translate its values
     */
    fun translate(sct: Struct?) {
        val it: Iterator<Entry<Key?, Object?>?> = sct.entryIterator()
        var e: Entry<Key?, Object?>?
        var value: Object
        while (it.hasNext()) {
            e = it.next()
            value = e.getValue()
            if (value is String) {
                sct.setEL(e.getKey(), translate(value as String))
            }
        }
    }

    /**
     * translate string to script-protected form
     *
     * @param str
     * @return translated String
     */
    fun translate(str: String?): String? {
        if (str == null) return ""
        // TODO do-while machen
        var index: Int
        var last = 0
        var endIndex: Int
        var sb: StringBuilder? = null
        var tagName: String?
        while (str.indexOf('<', last).also { index = it } != -1) {
            // read tagname
            val len: Int = str.length()
            var c: Char
            endIndex = index + 1
            while (endIndex < len) {
                c = str.charAt(endIndex)
                if ((c < 'a' || c > 'z') && (c < 'A' || c > 'Z')) break
                endIndex++
            }
            tagName = str.substring(index + 1, endIndex)
            if (compareTagName(tagName)) {
                if (sb == null) {
                    sb = StringBuilder()
                    last = 0
                }
                sb.append(str.substring(last, index + 1))
                sb.append("invalidTag")
                last = endIndex
            } else if (sb != null) {
                sb.append(str.substring(last, index + 1))
                last = index + 1
            } else last = index + 1
        }
        if (sb != null) {
            if (last != str.length()) sb.append(str.substring(last))
            return sb.toString()
        }
        return str
    }

    private fun compareTagName(tagName: String?): Boolean {
        for (i in invalids.indices) {
            if (invalids!![i].equalsIgnoreCase(tagName)) return true
        }
        return false
    }
}