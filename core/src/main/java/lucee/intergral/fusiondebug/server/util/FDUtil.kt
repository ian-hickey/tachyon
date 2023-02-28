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
package lucee.intergral.fusiondebug.server.util

import kotlin.Throws
import kotlin.jvm.Synchronized
import lucee.commons.io.SystemUtil.Caller
import kotlin.jvm.Transient
import kotlin.jvm.JvmOverloads
import kotlin.jvm.Volatile
import lucee.commons.collection.concurrent.ConcurrentHashMapNullSupport.EntrySet
import lucee.commons.collection.concurrent.ConcurrentHashMapNullSupport.EntryIterator
import lucee.commons.collection.LongKeyList.Pair
import lucee.commons.collection.AbstractCollection
import lucee.runtime.type.Array
import java.sql.Array
import lucee.commons.lang.Pair
import lucee.runtime.exp.CatchBlockImpl.Pair
import lucee.runtime.type.util.ListIteratorImpl
import lucee.runtime.type.Lambda
import java.util.Random
import lucee.runtime.config.Constants
import lucee.runtime.engine.Request
import lucee.runtime.engine.ExecutionLogSupport.Pair
import lucee.runtime.functions.other.NullValue
import lucee.runtime.functions.string.Val
import lucee.runtime.reflection.Reflector.JavaAnnotation
import lucee.transformer.cfml.evaluator.impl.Output
import lucee.transformer.cfml.evaluator.impl.Property
import lucee.transformer.bytecode.statement.Condition.Pair

object FDUtil {
    /**
     * replace the last occurrence of from with to
     *
     * @param str
     * @param from
     * @param to
     * @return changed string
     */
    private fun replaceLast(str: String?, from: Char, to: Char): String? {
        val index: Int = str.lastIndexOf(from)
        return if (index == -1) str else str.substring(0, index) + to + str.substring(index + 1)
    }

    /**
     * if given string is a keyword it will be replaced with none keyword
     *
     * @param str
     * @return corrected word
     */
    private fun correctReservedWord(str: String?): String? {
        val first: Char = str.charAt(0)
        when (first) {
            'a' -> if (str!!.equals("abstract")) return "_$str"
            'b' -> if (str!!.equals("boolean")) return "_$str" else if (str.equals("break")) return "_$str" else if (str.equals("byte")) return "_$str"
            'c' -> if (str!!.equals("case")) return "_$str" else if (str.equals("catch")) return "_$str" else if (str.equals("char")) return "_$str" else if (str.equals("const")) return "_$str" else if (str.equals("class")) return "_$str" else if (str.equals("continue")) return "_$str"
            'd' -> if (str!!.equals("default")) return "_$str" else if (str.equals("do")) return "_$str" else if (str.equals("double")) return "_$str"
            'e' -> if (str!!.equals("else")) return "_$str" else if (str.equals("extends")) return "_$str" else if (str.equals("enum")) return "_$str"
            'f' -> if (str!!.equals("false")) return "_$str" else if (str.equals("final")) return "_$str" else if (str.equals("finally")) return "_$str" else if (str.equals("float")) return "_$str" else if (str.equals("for")) return "_$str"
            'g' -> if (str!!.equals("goto")) return "_$str"
            'i' -> if (str!!.equals("if")) return "_$str" else if (str.equals("implements")) return "_$str" else if (str.equals("import")) return "_$str" else if (str.equals("instanceof")) return "_$str" else if (str.equals("int")) return "_$str" else if (str.equals("interface")) return "_$str"
            'n' -> if (str!!.equals("native")) return "_$str" else if (str.equals("new")) return "_$str" else if (str.equals("null")) return "_$str"
            'p' -> if (str!!.equals("package")) return "_$str" else if (str.equals("private")) return "_$str" else if (str.equals("protected")) return "_$str" else if (str.equals("public")) return "_$str"
            'r' -> if (str!!.equals("return")) return "_$str"
            's' -> if (str!!.equals("short")) return "_$str" else if (str.equals("static")) return "_$str" else if (str.equals("strictfp")) return "_$str" else if (str.equals("super")) return "_$str" else if (str.equals("switch")) return "_$str" else if (str.equals("synchronized")) return "_$str"
            't' -> if (str!!.equals("this")) return "_$str" else if (str.equals("throw")) return "_$str" else if (str.equals("throws")) return "_$str" else if (str.equals("transient")) return "_$str" else if (str.equals("true")) return "_$str" else if (str.equals("try")) return "_$str"
            'v' -> if (str!!.equals("void")) return "_$str" else if (str.equals("volatile")) return "_$str"
            'w' -> if (str!!.equals("while")) return "_$str"
        }
        return str
    }

    /**
     * translate a string to a valid variable string
     *
     * @param str string to translate
     * @return translated String
     */
    private fun toVariableName(str: String?): String? {
        val rtn = StringBuffer()
        val chars: CharArray = str.toCharArray()
        var changes: Long = 0
        var doCorrect = true
        for (i in chars.indices) {
            val c = chars[i]
            if (i == 0 && c >= '0' && c <= '9') rtn.append("_$c") else if (c >= 'a' && c <= 'z' || c >= 'A' && c <= 'Z' || c >= '0' && c <= '9' || c == '_' || c == '$') rtn.append(c) else {
                doCorrect = false
                rtn.append('_')
                changes += (c.toInt() * (i + 1)).toLong()
            }
        }
        if (changes > 0) rtn.append(changes)
        return if (doCorrect) correctReservedWord(rtn.toString()) else rtn.toString()
    }

    /**
     * creates a classbane from give source path
     *
     * @param str
     * @return
     */
    fun toClassName(str: String?): String? {
        val javaName = StringBuffer()
        val arr: Array<String?> = lucee.runtime.type.util.ListUtil.listToStringArray(str, '/')
        for (i in arr.indices) {
            if (i == arr.size - 1) arr[i] = replaceLast(arr[i], '.', '$')
            if (i != 0) javaName.append('.')
            javaName.append(toVariableName(arr[i]))
        }
        return javaName.toString().toLowerCase()
    }
}