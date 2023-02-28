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
package lucee.commons.lang

import java.nio.charset.Charset

/**
 * Util to do some additional String Operations
 */
object StringUtil {
    private val SPECIAL_WHITE_SPACE_CHARS = charArrayOf(0x85 // NEL, Next line
            .toChar(), 0xa0 // no-break space
            .toChar(), 0x1680 // ogham space mark
            .toChar(), 0x180e // mongolian vowel separator
            .toChar(), 0x2000 // en quad
            .toChar(), 0x2001 // em quad
            .toChar(), 0x2002 // en space
            .toChar(), 0x2003 // em space
            .toChar(), 0x2004 // three-per-em space
            .toChar(), 0x2005 // four-per-em space
            .toChar(), 0x2006 // six-per-em space
            .toChar(), 0x2007 // figure space
            .toChar(), 0x2008 // punctuation space
            .toChar(), 0x2009 // thin space
            .toChar(), 0x200A // hair space
            .toChar(), 0x2028 // line separator
            .toChar(), 0x2029 // paragraph separator
            .toChar(), 0x202F // narrow no-break space
            .toChar(), 0x205F // medium mathematical space
            .toChar(), 0x3000 // ideographic space
            .toChar())
    private val QUOTE_8220 = charArrayOf(226.toChar(), 8364.toChar(), 339.toChar())
    private val QUOTE_8221 = charArrayOf(226.toChar(), 8364.toChar(), 65533.toChar())
    private val SURROGATE_CHARACTERS_RANGE = charArrayOf(55296.toChar(), 57343.toChar())

    /**
     * do first Letter Upper case
     *
     * @param str String to operate
     * @return uppercase string
     */
    fun ucFirst(str: String?): String? {
        return if (str == null) null else if (str.length() <= 1) str.toUpperCase() else {
            str.substring(0, 1).toUpperCase() + str.substring(1)
        }
    }

    fun capitalize(input: String, delims: CharArray?): String {
        var delims = delims
        if (isEmpty(input)) return input
        if (ArrayUtil.isEmpty(delims)) delims = charArrayOf('.', '-', '(', ')')
        val sb = StringBuilder(input.length())
        var isLastDelim = true
        var isLastSpace = true
        val len: Int = input.length()
        for (i in 0 until len) {
            val c: Char = input.charAt(i)
            if (Character.isWhitespace(c)) {
                if (!isLastSpace) sb.append(' ')
                isLastSpace = true
            } else {
                sb.append(if (isLastSpace || isLastDelim) Character.toUpperCase(c) else c)
                isLastDelim = _contains(delims, c)
                isLastSpace = false
            }
        }
        return sb.toString()
    }

    private fun _contains(chars: CharArray?, c: Char): Boolean {
        for (i in chars.indices) {
            if (chars!![i] == c) return true
        }
        return false
    }

    /**
     * do first Letter Upper case
     *
     * @param str String to operate
     * @return lower case String
     */
    fun lcFirst(str: String?): String? {
        return if (str == null) null else if (str.length() <= 1) str.toLowerCase() else {
            str.substring(0, 1).toLowerCase() + str.substring(1)
        }
    }

    /**
     * Unescapes HTML Tags
     *
     * @param html html code to escape
     * @return escaped html code
     */
    fun unescapeHTML(html: String): String {
        return HTMLEntities.unescapeHTML(html)
    }

    /**
     * Escapes XML Tags
     *
     * @param html html code to unescape
     * @return unescaped html code
     */
    fun escapeHTML(html: String): String {
        return HTMLEntities.escapeHTML(html)
    }

    /**
     * escapes JS sensitive characters
     *
     * @param str String to escape
     * @return escapes String
     */
    fun escapeJS(str: String?, quotesUsed: Char): String {
        return escapeJS(str, quotesUsed, null as CharsetEncoder?)
    }

    fun escapeJS(str: String?, quotesUsed: Char, charset: java.nio.charset.Charset?): String {
        return escapeJS(str, quotesUsed, if (charset == null) null else charset.newEncoder())
    }

    /**
     * escapes JS sensitive characters
     *
     * @param str String to escape
     * @param charset if not null, it checks if the given string is supported by the encoding, if not,
     * lucee encodes the string
     * @return escapes String
     */
    fun escapeJS(str: String, quotesUsed: Char, enc: CharsetEncoder?): String {
        val arr: CharArray = str.toCharArray()
        val rtn = StringBuilder(arr.size)
        rtn.append(quotesUsed)
        for (i in arr.indices) {
            when (arr[i]) {
                '\\' -> rtn.append("\\\\")
                '\n' -> rtn.append("\\n")
                '\r' -> rtn.append("\\r")
                '\f' -> rtn.append("\\f")
                '\b' -> rtn.append("\\b")
                '\t' -> rtn.append("\\t")
                '"' -> if (quotesUsed == '"') rtn.append("\\\"") else rtn.append('"')
                '\'' -> if (quotesUsed == '\'') rtn.append("\\\'") else rtn.append('\'')
                '/' -> {
                    // escape </script>
                    if (i > 0 && arr[i - 1] == '<' && i + 1 < arr.size && arr[i + 1] == 's' && i + 2 < arr.size && arr[i + 2] == 'c' && i + 3 < arr.size && arr[i + 3] == 'r' && i + 4 < arr.size && arr[i + 4] == 'i' && i + 5 < arr.size && arr[i + 5] == 'p' && i + 6 < arr.size && arr[i + 6] == 't' && i + 7 < arr.size && (isWhiteSpace(arr[i + 7]) || arr[i + 7] == '>')) {
                        rtn.append("\\/")
                        break
                    }
                    if (Character.isISOControl(arr[i]) || arr[i] >= 128 && (enc == null || !enc.canEncode(arr[i]))) {
                        if (arr[i] < 0x10) rtn.append("\\u000") else if (arr[i] < 0x100) rtn.append("\\u00") else if (arr[i] < 0x1000) rtn.append("\\u0") else rtn.append("\\u")
                        rtn.append(Integer.toHexString(arr[i]))
                    } else {
                        rtn.append(arr[i])
                    }
                }
                else -> if (Character.isISOControl(arr[i]) || arr[i] >= 128 && (enc == null || !enc.canEncode(arr[i]))) {
                    if (arr[i] < 0x10) rtn.append("\\u000") else if (arr[i] < 0x100) rtn.append("\\u00") else if (arr[i] < 0x1000) rtn.append("\\u0") else rtn.append("\\u")
                    rtn.append(Integer.toHexString(arr[i]))
                } else {
                    rtn.append(arr[i])
                }
            }
        }
        return rtn.append(quotesUsed).toString()
    }

    /**
     * reapeats a string
     *
     * @param str string to repeat
     * @param count how many time string will be repeated
     * @return reapted string
     */
    fun repeatString(str: String, count: Int): String {
        if (count <= 0) return ""
        val chars: CharArray = str.toCharArray()
        val rtn = CharArray(chars.size * count)
        var pos = 0
        for (i in 0 until count) {
            for (y in chars.indices) rtn[pos++] = chars[y]
            // rtn.append(str);
        }
        return String(rtn)
    }

    /**
     * translate, like method toString, an object to a string, but when value is null value will be
     * translated to an empty String ("").
     *
     * @param o Object to convert
     * @return converted String
     */
    fun toStringEmptyIfNull(o: Object?): String {
        return if (o == null) "" else o.toString()
    }

    fun emptyIfNull(str: String?): String {
        return str ?: ""
    }

    fun emptyIfNull(key: Collection.Key?): String {
        return if (key == null) "" else key.getString()
    }

    /**
     * escape all special characters of the regular expresson language
     *
     * @param str String to escape
     * @return escaped String
     */
    fun reqExpEscape(str: String): String {
        val arr: CharArray = str.toCharArray()
        val sb = StringBuilder(str.length() * 2)
        for (i in arr.indices) {
            sb.append('\\')
            sb.append(arr[i])
        }
        return sb.toString()
    }

    /**
     * translate a string to a valid identity variable name
     *
     * @param varName variable name template to translate
     * @return translated variable name
     */
    fun toIdentityVariableName(varName: String): String {
        val chars: CharArray = varName.toCharArray()
        var changes: Long = 0
        val rtn = StringBuilder(chars.size + 2)
        rtn.append("CF")
        for (i in chars.indices) {
            val c = chars[i]
            if (c >= 'a' && c <= 'z' || c >= 'A' && c <= 'Z' || c >= '0' && c <= '9') rtn.append(c) else {
                rtn.append('_')
                changes += (c.toInt() * (i + 1)).toLong()
            }
        }
        return rtn.append(changes).toString()
    }

    /**
     * translate a string to a valid classname string
     *
     * @param str string to translate
     * @return translated String
     */
    fun toClassName(str: String): String {
        val rtn = StringBuilder()
        val arr: Array<String> = str.split("[\\\\|//]")
        for (i in arr.indices) {
            if (arr[i].length() === 0) continue
            if (rtn.length() !== 0) rtn.append('.')
            val chars: CharArray = arr[i].toCharArray()
            var changes: Long = 0
            for (y in chars.indices) {
                val c = chars[y]
                if (y == 0 && c >= '0' && c <= '9') rtn.append("_$c") else if (c >= 'a' && c <= 'z' || c >= 'A' && c <= 'Z' || c >= '0' && c <= '9') rtn.append(c) else {
                    rtn.append('_')
                    changes += (c.toInt() * (i + 1)).toLong()
                }
            }
            if (changes > 0) rtn.append(changes)
        }
        return rtn.toString()
    }

    /**
     * translate a string to a valid variable string
     *
     * @param str string to translate
     * @return translated String
     */
    fun toVariableName(str: String): String {
        return toVariableName(str, true, false)
    }

    fun toJavaClassName(str: String): String {
        return toVariableName(str, true, true)
    }

    fun toVariableName(str: String, addIdentityNumber: Boolean, allowDot: Boolean): String {
        val rtn = StringBuilder()
        val chars: CharArray = str.toCharArray()
        var changes: Long = 0
        var doCorrect = true
        for (i in chars.indices) {
            val c = chars[i]
            if (i == 0 && c >= '0' && c <= '9') rtn.append("_$c") else if (c >= 'a' && c <= 'z' || c >= 'A' && c <= 'Z' || c >= '0' && c <= '9' || c == '_' || c == '$' || allowDot && c == '.') rtn.append(c) else {
                doCorrect = false
                rtn.append('_')
                changes += (c.toInt() * (i + 1)).toLong()
            }
        }
        if (addIdentityNumber && changes > 0) rtn.append(changes)
        // print.ln(" - "+rtn);
        return if (doCorrect) correctReservedWord(rtn.toString()) else rtn.toString()
    }

    /**
     * if given string is a keyword it will be replaced with none keyword
     *
     * @param str
     * @return corrected word
     */
    private fun correctReservedWord(str: String): String {
        val first: Char = str.charAt(0)
        when (first) {
            'a' -> if (str.equals("abstract")) return "_$str"
            'b' -> if (str.equals("boolean")) return "_$str" else if (str.equals("break")) return "_$str" else if (str.equals("byte")) return "_$str"
            'c' -> if (str.equals("case")) return "_$str" else if (str.equals("catch")) return "_$str" else if (str.equals("char")) return "_$str" else if (str.equals("const")) return "_$str" else if (str.equals("class")) return "_$str" else if (str.equals("continue")) return "_$str"
            'd' -> if (str.equals("default")) return "_$str" else if (str.equals("do")) return "_$str" else if (str.equals("double")) return "_$str"
            'e' -> if (str.equals("else")) return "_$str" else if (str.equals("extends")) return "_$str" else if (str.equals("enum")) return "_$str"
            'f' -> if (str.equals("false")) return "_$str" else if (str.equals("final")) return "_$str" else if (str.equals("finally")) return "_$str" else if (str.equals("float")) return "_$str" else if (str.equals("for")) return "_$str"
            'g' -> if (str.equals("goto")) return "_$str"
            'i' -> if (str.equals("if")) return "_$str" else if (str.equals("implements")) return "_$str" else if (str.equals("import")) return "_$str" else if (str.equals("instanceof")) return "_$str" else if (str.equals("int")) return "_$str" else if (str.equals("interface")) return "_$str"
            'j' -> if (str.equals("java")) return "_$str"
            'n' -> if (str.equals("native")) return "_$str" else if (str.equals("new")) return "_$str" else if (str.equals("null")) return "_$str"
            'p' -> if (str.equals("package")) return "_$str" else if (str.equals("private")) return "_$str" else if (str.equals("protected")) return "_$str" else if (str.equals("public")) return "_$str"
            'r' -> if (str.equals("return")) return "_$str"
            's' -> if (str.equals("short")) return "_$str" else if (str.equals("static")) return "_$str" else if (str.equals("strictfp")) return "_$str" else if (str.equals("super")) return "_$str" else if (str.equals("switch")) return "_$str" else if (str.equals("synchronized")) return "_$str"
            't' -> if (str.equals("this")) return "_$str" else if (str.equals("throw")) return "_$str" else if (str.equals("throws")) return "_$str" else if (str.equals("transient")) return "_$str" else if (str.equals("true")) return "_$str" else if (str.equals("try")) return "_$str"
            'v' -> if (str.equals("void")) return "_$str" else if (str.equals("volatile")) return "_$str"
            'w' -> if (str.equals("while")) return "_$str"
        }
        return str
    }

    /**
     * This function returns a string with whitespace stripped from the beginning of str
     *
     * @param str String to clean
     * @return cleaned String
     */
    fun ltrim(str: String?, defaultValue: String): String {
        if (str == null) return defaultValue
        val len: Int = str.length()
        var st = 0
        while (st < len && str.charAt(st) <= ' ') {
            st++
        }
        return if (st > 0) str.substring(st) else str
    }

    /**
     * This function returns a string with whitespace stripped from the end of str
     *
     * @param str String to clean
     * @return cleaned String
     */
    fun rtrim(str: String?, defaultValue: String): String {
        if (str == null) return defaultValue
        var len: Int = str.length()
        while (0 < len && str.charAt(len - 1) <= ' ') {
            len--
        }
        return if (len < str.length()) str.substring(0, len) else str
    }

    /**
     * trim given value, return defaultvalue when input is null
     *
     * @param str
     * @param defaultValue
     * @return trimmed string or defaultValue
     */
    fun trim(str: String?, defaultValue: String): String {
        return if (str == null) defaultValue else str.trim()
    }

    /**
     *
     * @param c character to check
     * @param checkSpecialWhiteSpace if set to true, lucee checks also uncommon white spaces.
     * @return
     */
    fun isWhiteSpace(c: Char, checkSpecialWhiteSpace: Boolean): Boolean {
        if (Character.isWhitespace(c)) return true
        if (checkSpecialWhiteSpace) {
            for (i in SPECIAL_WHITE_SPACE_CHARS.indices) {
                if (c == SPECIAL_WHITE_SPACE_CHARS[i]) return true
            }
        }
        return false
    }

    fun isWhiteSpace(c: Char): Boolean {
        return isWhiteSpace(c, false)
    }

    /**
     * trim given value, return defaultvalue when input is null this function no only removes the
     * "classic" whitespaces, it also removes Byte order masks forgotten to remove when reading a UTF
     * file.
     *
     * @param str
     * @param removeBOM if set to true, Byte Order Mask that got forgotten get removed as well
     * @param removeSpecialWhiteSpace if set to true, lucee removes also uncommon white spaces.
     * @param defaultValue
     * @return trimmed string or defaultValue
     */
    fun trim(str: String?, removeBOM: Boolean, removeSpecialWhiteSpace: Boolean, defaultValue: String): String {
        var str = str ?: return defaultValue
        if (str.isEmpty()) return str
        // remove leading BOM Marks
        if (removeBOM) {
            // UTF-16, big-endian
            if (str.charAt(0) === '\uFEFF') str = str.substring(1) else if (str.charAt(0) === '\uFFFD') str = str.substring(1) else if (str.charAt(0) === '\uFFFE') str = str.substring(1) else if (str.length() >= 2) {
                // TODO i get this from UTF-8 files generated by suplime text, i was expecting something else
                if (str.charAt(0) === '\uBBEF' && str.charAt(1) === '\uFFFD') str = str.substring(2)
            }
        }
        if (removeSpecialWhiteSpace) {
            val len: Int = str.length()
            var startIndex = 0
            var endIndex = len - 1
            // left
            while (startIndex < len && isWhiteSpace(str.charAt(startIndex), true)) {
                startIndex++
            }
            // right
            while (startIndex < endIndex && isWhiteSpace(str.charAt(endIndex), true)) {
                endIndex--
            }
            return if (startIndex > 0 || endIndex + 1 < len) str.substring(startIndex, endIndex + 1) else str
        }
        return str.trim()
    }

    /**
     * return if in a string are line feeds or not
     *
     * @param str string to check
     * @return translated string
     */
    fun hasLineFeed(str: String): Boolean {
        val len: Int = str.length()
        var c: Char
        for (i in 0 until len) {
            c = str.charAt(i)
            if (c == '\n' || c == '\r') return true
        }
        return false
    }

    /**
     * remove all white spaces followed by whitespaces
     *
     * @param str string to translate
     * @return translated string
     */
    fun suppressWhiteSpace(str: String): String {
        val len: Int = str.length()
        val sb = StringBuilder(len)
        // boolean wasWS=false;
        var c: Char
        var buffer = 0.toChar()
        for (i in 0 until len) {
            c = str.charAt(i)
            if (c == '\n' || c == '\r') buffer = '\n' else if (isWhiteSpace(c)) {
                if (buffer.toInt() == 0) buffer = c
            } else {
                if (buffer.toInt() != 0) {
                    sb.append(buffer)
                    buffer = 0.toChar()
                }
                sb.append(c)
            }
            // sb.append(c);
        }
        if (buffer.toInt() != 0) sb.append(buffer)
        return sb.toString()
    }

    /**
     * returns string, if given string is null or length 0 return default value
     *
     * @param value
     * @param defaultValue
     * @return value or default value
     */
    fun toString(value: String?, defaultValue: String): String {
        return if (value == null || value.length() === 0) defaultValue else value
    }

    /**
     * returns string, if given string is null or length 0 return default value
     *
     * @param value
     * @param defaultValue
     * @return value or default value
     */
    fun toString(value: Object?, defaultValue: String): String {
        return if (value == null) defaultValue else toString(value.toString(), defaultValue)
    }

    /**
     * cut string to max size if the string is greater, otherwise to nothing
     *
     * @param content
     * @param max
     * @return cutted string
     */
    fun max(content: String?, max: Int): String? {
        return max(content, max, "")
    }

    fun max(content: String?, max: Int, dotDotDot: String): String? {
        if (content == null) return null
        return if (content.length() <= max) content else content.substring(0, max) + dotDotDot
    }

    /**
     * performs a replace operation on a string
     *
     * @param input - the string input to work on
     * @param find - the substring to find
     * @param repl - the substring to replace the matches with
     * @param firstOnly - if true then only the first occurrence of `find` will be replaced
     * @param ignoreCase - if true then matches will not be case sensitive
     * @return
     */
    fun replace(input: String, find: String, repl: String, firstOnly: Boolean, ignoreCase: Boolean): String {
        return _replace(input, find, repl, firstOnly, ignoreCase, null).toString()
    }

    fun _replace(input: String, find: String, repl: String, firstOnly: Boolean, ignoreCase: Boolean, positions: List<Pos?>?): CharSequence {
        val findLen: Int = find.length()
        if (findLen == 0) return input

        // String scan = input;

        /*
		 * if ( ignoreCase ) { scan = scan.toLowerCase(); find = find.toLowerCase(); } else
		 */if (!ignoreCase && findLen == repl.length()) {
            if (find.equals(repl)) return input
            if (!firstOnly && findLen == 1 && positions == null) return input.replace(find.charAt(0), repl.charAt(0))
        }
        var pos = if (ignoreCase) indexOfIgnoreCase(input, find) else input.indexOf(find)
        if (pos == -1) return input
        var start = 0
        val sb = StringBuilder(if (repl.length() > find.length()) Math.ceil(input.length() * 1.2) else input.length())
        while (pos != -1) {
            positions?.add(Pos(pos, repl.length()))
            sb.append(input.substring(start, pos))
            sb.append(repl)
            start = pos + findLen
            if (firstOnly) break
            pos = if (ignoreCase) indexOfIgnoreCase(input, find, start) else input.indexOf(find, start)
        }
        if (input.length() > start) sb.append(input.substring(start))
        return sb
    }

    @Throws(PageException::class)
    fun replace(pc: PageContext?, input: String, find: String, udf: UDF, firstOnly: Boolean): String {
        var len: Int
        if (find.length().also { len = it } == 0) return input
        val sb = StringBuilder()
        var repl: String
        var index: Int
        var last = 0
        while (input.indexOf(find, last).also { index = it } != -1) {
            sb.append(input.substring(last, index))
            repl = Caster.toString(udf.call(pc, arrayOf(find, index, input), true))
            sb.append(repl)
            last = index + len
            if (firstOnly) break
        }
        if (last < input.length()) sb.append(input.substring(last))
        return sb.toString()
    }

    /**
     * maintains the legacy signature of this method where matches are CaSe sensitive (sets the default
     * of ignoreCase to false).
     *
     * @param input - the string input to work on
     * @param find - the substring to find
     * @param repl - the substring to replace the matches with
     * @param firstOnly - if true then only the first occurrence of `find` will be replaced
     * @return - calls replace( input, find, repl, firstOnly, false )
     */
    fun replace(input: String?, find: String?, repl: String?, firstOnly: Boolean): String {
        return replace(input, find, repl, firstOnly, false)
    }

    /**
     * performs a CaSe sensitive replace all
     *
     * @param input - the string input to work on
     * @param find - the substring to find
     * @param repl - the substring to replace the matches with
     * @return - calls replace( input, find, repl, false, false )
     */
    fun replace(input: String?, find: String?, repl: String?): String {
        return replace(input, find, repl, false, false)
    }

    /**
     * adds zeros add the begin of an int example: addZeros(2,3) return "002"
     *
     * @param i number to add nulls
     * @param size
     * @return min len of return value;
     */
    fun addZeros(i: Int, size: Int): String {
        val rtn: String = Caster.toString(i)
        return if (rtn.length() < size) repeatString("0", size - rtn.length()) + rtn else rtn
    }

    /**
     * adds zeros add the begin of an int example: addZeros(2,3) return "002"
     *
     * @param i number to add nulls
     * @param size
     * @return min len of return value;
     */
    fun addZeros(i: Long, size: Int): String {
        val rtn: String = Caster.toString(i)
        return if (rtn.length() < size) repeatString("0", size - rtn.length()) + rtn else rtn
    }

    fun indexOf(haystack: String?, needle: String?): Int {
        return if (haystack == null) -1 else haystack.indexOf(needle)
    }

    fun indexOfIgnoreCase(haystack: String, needle: String): Int {
        return indexOfIgnoreCase(haystack, needle, 0)
    }

    fun indexOfIgnoreCase(haystack: String, needle: String, offset: Int): Int {
        var haystack = haystack
        var needle = needle
        var offset = offset
        if (isEmpty(haystack) || isEmpty(needle)) return -1
        needle = needle.toLowerCase()
        if (offset > 0) haystack = haystack.substring(offset) else offset = 0
        val lenHaystack: Int = haystack.length()
        val lenNeedle: Int = needle.length()
        val lastNeedle: Char = needle.charAt(lenNeedle - 1)
        var c: Char
        outer@ for (i in lenNeedle - 1 until lenHaystack) {
            c = Character.toLowerCase(haystack.charAt(i))
            if (c == lastNeedle) {
                for (y in 0 until lenNeedle - 1) {
                    if (needle.charAt(y) !== Character.toLowerCase(haystack.charAt(i - (lenNeedle - 1) + y))) continue@outer
                }
                return i - (lenNeedle - 1) + offset
            }
        }
        return -1
    }

    /**
     * Tests if this string starts with the specified prefix.
     *
     * @param str string to check first char
     * @param prefix the prefix.
     * @return is first of given type
     */
    fun startsWith(str: String?, prefix: Char): Boolean {
        return str != null && str.length() > 0 && str.charAt(0) === prefix
    }

    fun startsWith(str: String?, prefix1: Char, prefix2: Char): Boolean {
        return str != null && str.length() > 0 && (str.charAt(0) === prefix1 || str.charAt(0) === prefix2)
    }

    /**
     * Tests if this string ends with the specified suffix.
     *
     * @param str string to check first char
     * @param suffix the suffix.
     * @return is last of given type
     */
    fun endsWith(str: String?, suffix: Char): Boolean {
        return str != null && str.length() > 0 && str.charAt(str.length() - 1) === suffix
    }

    fun endsWith(str: String?, prefix1: Char, prefix2: Char): Boolean {
        return str != null && str.length() > 0 && (str.charAt(str.length() - 1) === prefix1 || str.charAt(str.length() - 1) === prefix2)
    }
    /**
     * Tests if this string ends with the specified suffix.
     *
     * @param str string to check first char
     * @param suffix the suffix.
     * @return is last of given type
     */
    /**
     * Helper functions to query a strings start portion. The comparison is case insensitive.
     *
     * @param base the base string.
     * @param start the starting text.
     *
     * @return true, if the string starts with the given starting text.
     */
    fun startsWithIgnoreCase(base: String, start: String): Boolean {
        return if (base.length() < start.length()) {
            false
        } else base.regionMatches(true, 0, start, 0, start.length())
    }

    /**
     * Helper functions to query a strings end portion. The comparison is case insensitive.
     *
     * @param base the base string.
     * @param end the ending text.
     *
     * @return true, if the string ends with the given ending text.
     */
    fun endsWithIgnoreCase(base: String, end: String): Boolean {
        return if (base.length() < end.length()) {
            false
        } else base.regionMatches(true, base.length() - end.length(), end, 0, end.length())
    }

    /**
     * returns if byte arr is a BOM character Stream (UTF-8,UTF-16)
     *
     * @param barr
     * @return is BOM or not
     */
    fun isBOM(barr: ByteArray): Boolean {
        return barr.size >= 3 && barr[0] == 0xEF && barr[1] == 0xBB && barr[2] == 0xBF
    }

    /**
     * return "" if value is null otherwise return same string
     *
     * @param str
     * @return string (not null)
     */
    fun valueOf(str: String?): String {
        return str ?: ""
    }

    /**
     * cast a string a lower case String, is faster than the String.toLowerCase, if all Character are
     * already Low Case
     *
     * @param str
     * @return lower case value
     */
    fun toLowerCase(str: String): String {
        val len: Int = str.length()
        var c: Char
        for (i in 0 until len) {
            c = str.charAt(i)
            if (!(c >= 'a' && c <= 'z' || c >= '0' && c <= '9')) {
                return str.toLowerCase()
            }
        }
        return str
    }

    fun toUpperCase(str: String): String {
        val len: Int = str.length()
        var c: Char
        for (i in 0 until len) {
            c = str.charAt(i)
            if (!(c >= 'A' && c <= 'Z' || c >= '0' && c <= '9')) {
                return str.toUpperCase()
            }
        }
        return str
    }

    /**
     * soundex function
     *
     * @param str
     * @return soundex from given string
     */
    fun soundex(str: String?): String {
        return Soundex().soundex(str)
    }

    /**
     * return the last character of a string, if string ist empty return 0;
     *
     * @param str string to get last character
     * @return last character
     */
    fun lastChar(str: String?): Char {
        return if (str == null || str.length() === 0) 0 else str.charAt(str.length() - 1)
    }

    /**
     *
     * @param str
     * @return return if a String is "Empty", that means NULL or String with length 0 (whitespaces will
     * not counted)
     */
    fun isEmpty(str: CharSequence?): Boolean {
        return str == null || str.length() === 0
    }

    /**
     *
     * @param str
     * @return return if a String is "Empty", that means NULL or String with length 0 (whitespaces will
     * not counted)
     */
    fun isEmpty(str: String?, trim: Boolean): Boolean {
        return if (!trim) isEmpty(str) else str == null || str.trim().length() === 0
    }

    /**
     * return the first character of a string, if string ist empty return 0;
     *
     * @param str string to get first character
     * @return first character
     */
    fun firstChar(str: String): Char {
        return if (isEmpty(str)) 0 else str.charAt(0)
    }

    fun removeWhiteSpace(str: String): String {
        if (isEmpty(str)) return str
        val sb = StringBuilder()
        val carr: CharArray = str.trim().toCharArray()
        for (i in carr.indices) {
            if (!isWhiteSpace(carr[i])) sb.append(carr[i])
        }
        return sb.toString()
    }

    /**
     * collapses multiple whitespace characters into a single space. the whitespace returned is always a
     * standard chr(32) .
     *
     * @param str
     * @return
     */
    fun collapseWhitespace(str: String): String {
        if (isEmpty(str)) return str
        val sb = StringBuilder(str.length())
        var wasLastWs = false
        val carr: CharArray = str.trim().toCharArray()
        for (i in carr.indices) {
            wasLastWs = if (isWhiteSpace(carr[i])) {
                if (wasLastWs) continue
                sb.append(' ')
                true
            } else {
                sb.append(carr[i])
                false
            }
        }
        return sb.toString()
    }

    fun replaceLast(str: String, from: Char, to: Char): String {
        val index: Int = str.lastIndexOf(from)
        return if (index == -1) str else str.substring(0, index) + to + str.substring(index + 1)
    }

    fun replaceLast(str: String, from: String, to: String): String {
        val index: Int = str.lastIndexOf(from)
        return if (index == -1) str else str.substring(0, index) + to + str.substring(index + from.length())
    }

    /**
     * removes quotes(",') that wraps the string
     *
     * @param string
     * @return
     */
    fun removeQuotes(string: String?, trim: Boolean): String? {
        var string = string
        if (string == null) return string
        if (trim) string = string.trim()
        if (string.length() < 2) return string
        if (startsWith(string, '"') && endsWith(string, '"') || startsWith(string, '\'') && endsWith(string, '\'')) {
            string = string.substring(1, string.length() - 1)
            if (trim) string = string.trim()
        }
        return string
    }

    fun isEmpty(obj: Object?, trim: Boolean): Boolean {
        if (obj == null) return true
        if (obj is String) return isEmpty(obj as String?, trim)
        if (obj is StringBuffer) return isEmpty(obj as StringBuffer?, trim)
        if (obj is StringBuilder) return isEmpty(obj as StringBuilder?, trim)
        return if (obj is Collection.Key) isEmpty((obj as Collection.Key).getString(), trim) else false
    }

    fun isEmpty(obj: Object?): Boolean {
        if (obj == null) return true
        if (obj is CharSequence) return isEmpty(obj as CharSequence?)
        return if (obj is Collection.Key) isEmpty((obj as Collection.Key).getString()) else false
    }

    fun isEmpty(sb: StringBuffer?, trim: Boolean): Boolean {
        return if (trim) sb == null || sb.toString().trim().length() === 0 else sb == null || sb.length() === 0
    }

    fun isEmpty(sb: StringBuilder?, trim: Boolean): Boolean {
        return if (trim) sb == null || sb.toString().trim().length() === 0 else sb == null || sb.length() === 0
    }

    fun isEmpty(sb: StringBuffer?): Boolean {
        return sb == null || sb.length() === 0
    }

    fun isEmpty(sb: StringBuilder?): Boolean {
        return sb == null || sb.length() === 0
    }

    fun removeStarting(str: String, sub: String): String {
        return if (isEmpty(str) || isEmpty(sub) || !str.startsWith(sub)) str else str.substring(sub.length())
    }

    fun removeStartingIgnoreCase(str: String, sub: String): String {
        return if (isEmpty(sub) || !startsWithIgnoreCase(str, sub)) str else str.substring(sub.length())
    }

    fun merge(str: String?, arr: Array<String?>): Array<String?> {
        val narr = arrayOfNulls<String>(arr.size + 1)
        narr[0] = str
        for (i in arr.indices) {
            narr[i + 1] = arr[i]
        }
        return narr
    }

    fun length(str: String?): Int {
        return str?.length() ?: 0
    }

    fun length(str: String?, trim: Boolean): Int {
        return str?.trim()?.length() ?: 0
    }

    fun hasUpperCase(str: String): Boolean {
        return if (isEmpty(str)) false else !str.equals(str.toLowerCase())
    }

    fun contains(str: String?, substr: String?): Boolean {
        return if (str == null) false else str.indexOf(substr) !== -1
    }

    fun containsIgnoreCase(str: String, substr: String): Boolean {
        return indexOfIgnoreCase(str, substr) != -1
    }

    fun substringEL(str: String?, index: Int, defaultValue: String): String {
        return if (str == null || index < 0 || index > str.length()) defaultValue else str.substring(index)
    }

    /**
     * translate a string in camel notation to a string in hypen notation example: helloWorld ->
     * hello-world
     *
     * @param str
     * @return
     */
    fun camelToHypenNotation(str: String): String {
        if (isEmpty(str)) return str
        val sb = StringBuilder()
        // int len=str.length();
        var c: Char
        sb.append(Character.toLowerCase(str.charAt(0)))
        for (i in 1 until str.length()) {
            c = str.charAt(i)
            if (Character.isUpperCase(c)) {
                sb.append('-')
                sb.append(Character.toLowerCase(c))
            } else sb.append(c)
        }
        return sb.toString()
    }

    /**
     * translate a string in hypen notation to a string in camel notation example: hello-world ->
     * helloWorld
     *
     * @param str
     * @return
     */
    fun hypenToCamelNotation(str: String): String {
        if (isEmpty(str)) return str
        val sb = StringBuilder()
        val len: Int = str.length()
        var c: Char
        var i = 0
        while (i < str.length()) {
            c = str.charAt(i)
            if (c == '-') {
                if (len > ++i) sb.append(Character.toUpperCase(str.charAt(i)))
            } else sb.append(c)
            i++
        }
        return sb.toString()
    }

    fun isAscii(str: String?): Boolean {
        if (str == null) return false
        for (i in str.length() - 1 downTo 0) {
            if (str.charAt(i) > 127) return false
        }
        return true
    }

    /**
     * returns true if all characters in the string are letters
     *
     * @param str
     * @return
     */
    fun isAllAlpha(str: String?): Boolean {
        if (str == null) return false
        for (i in str.length() - 1 downTo 0) {
            if (!Character.isLetter(str.charAt(i))) return false
        }
        return true
    }

    /**
     * returns true if the input string has letters and they are all UPPERCASE
     *
     * @param str
     * @return
     */
    fun isAllUpperCase(str: String?): Boolean {
        if (str == null) return false
        var hasLetters = false
        var c: Char
        for (i in str.length() - 1 downTo 0) {
            c = str.charAt(i)
            if (Character.isLetter(c)) {
                if (!Character.isUpperCase(c)) return false
                hasLetters = true
            }
        }
        return hasLetters
    }

    fun isWhiteSpace(str: String?): Boolean {
        if (str == null) return false
        for (i in str.length() - 1 downTo 0) {
            if (!isWhiteSpace(str.charAt(i))) return false
        }
        return true
    }

    /**
     * this method works different from the regular substring method, the regular substring method takes
     * startIndex and endIndex as second and third argument, this method takes offset and length
     *
     * @param str
     * @param off
     * @param len
     * @return
     */
    fun substring(str: String, off: Int, len: Int): String {
        return str.substring(off, off + len)
    }

    fun insertAt(str: String, substring: CharSequence, pos: Int): String {
        var pos = pos
        if (isEmpty(substring)) return str
        val len: Int = str.length()
        val sb = StringBuilder(len + substring.length())
        if (pos > len) pos = len
        if (pos > 0) sb.append(str.substring(0, pos))
        sb.append(substring)
        sb.append(str.substring(pos))
        return sb.toString()
    }

    /**
     * this is the public entry point for the replaceMap() method
     *
     * @param input - the string on which the replacements should be performed.
     * @param map - a java.util.Map with key/value pairs where the key is the substring to find and the
     * value is the substring with which to replace the matched key
     * @param ignoreCase - if true then matches will not be case sensitive
     * @return
     * @throws PageException
     */
    @Throws(PageException::class)
    fun replaceStruct(input: String, data: Struct, ignoreCase: Boolean): String {
        var result: CharSequence = input
        val it: Iterator<Entry<Key, Object>> = data.entryIterator()
        var e: Map.Entry<Key, Object>
        val positions: Map<Pos, String> = LinkedHashMap()
        var k: String
        var v: String
        var tmp: List<Pos?>
        while (it.hasNext()) {
            e = it.next()
            k = e.getKey().getString()
            v = Caster.toString(e.getValue())
            tmp = ArrayList<Pos>()
            result = _replace(result.toString(), k, placeholder(k), false, ignoreCase, tmp)
            for (pos in tmp) {
                positions.put(pos, v)
            }
        }
        if (result is StringBuilder) {
            val sb: StringBuilder = result as StringBuilder
            val list: List<Map.Entry<Pos, String>> = ArrayList<Map.Entry<Pos, String>>(positions.entrySet())
            // <Map.Entry<Integer,String>>
            Collections.sort(list, object : Comparator<Map.Entry<Pos?, String?>?>() {
                @Override
                fun compare(a: Map.Entry<Pos?, String?>, b: Map.Entry<Pos?, String?>): Int {
                    return Integer.compare(b.getKey().position, a.getKey().position)
                }
            })
            for (entry in list) {
                sb.delete(entry.getKey().position, entry.getKey().position + entry.getKey().len)
                sb.insert(entry.getKey().position, entry.getValue())
            }
            return sb.toString()
        }
        return result.toString()
    }

    private fun placeholder(str: String?): String {
        val count = str?.length() ?: 0
        if (count == 0) return ""
        val r = 0xFFFF.toChar()
        // r = '_';
        val carr = CharArray(count)
        for (i in 0 until count) {
            carr[i] = r
        }
        return String(carr)
    }

    /*
	 * public static void main(String[] args) throws PageException { Map<String, String> map = new
	 * HashMap<>(); map.put("target", "!target!"); map.put("replace", "er"); map.put("susi", "Susanne");
	 * print.e(
	 * replaceMap("I want replace replace to add 1 underscore with struct-replace... 'target' replace",
	 * map, false));
	 *
	 * map = new HashMap<>(); map.put("Susi", "Sorglos"); map.put("Sorglos", "Susi");
	 * print.e(replaceMap("Susi Sorglos foehnte ihr Haar", map, false));
	 *
	 * }
	 */
    fun unwrap(str: String): String {
        var str = str
        if (isEmpty(str)) return ""
        str = str.trim()
        var multiStart = false
        var multiEnd = false
        if ((startsWith(str, '"') || startsWith(str, 8220.toChar()) || startsWithWinRead8220(str).also { multiStart = it })
                && (endsWith(str, '"') || endsWith(str, 8221.toChar()) || endsWithWinRead8221(str).also { multiEnd = it })) str = str.substring(if (multiStart) 3 else 1, str.length() - if (multiEnd) 3 else 1)
        if (startsWith(str, '\'') && endsWith(str, '\'')) str = str.substring(1, str.length() - 1)
        return str
    }

    private fun startsWithWinRead8220(str: String): Boolean {
        return str.length() > 2 && str.charAt(0) === lucee.commons.lang.StringUtil.QUOTE_8220.get(0) && str.charAt(1) === lucee.commons.lang.StringUtil.QUOTE_8220.get(1) && str.charAt(2) === lucee.commons.lang.StringUtil.QUOTE_8220.get(2)
    }

    private fun endsWithWinRead8221(str: String): Boolean {
        val len: Int = str.length()
        return str.length() > 2 && str.charAt(len - 3) === lucee.commons.lang.StringUtil.QUOTE_8221.get(0) && str.charAt(len - 2) === lucee.commons.lang.StringUtil.QUOTE_8221.get(1) && str.charAt(len - 1) === lucee.commons.lang.StringUtil.QUOTE_8221.get(2)
    }

    fun toStringNative(obj: Object?, defaultValue: String): String {
        return if (obj == null) defaultValue else obj.toString()
    }

    fun emptyAsNull(str: String?, trim: Boolean): String? {
        return if (isEmpty(str, trim)) null else str
    }

    /*
	 * public function cleanSurrogateCharacters(String str) { var SURROGATE_CHARACTERS_RANGE =
	 * [55296,57343]; var carr = str.toCharArray(); var l=len(carr); for(var i=1;i<=l;i++) { var
	 * c=carr[i]; var a=asc(c); // detect one if (a >= SURROGATE_CHARACTERS_RANGE[1] && a <=
	 * SURROGATE_CHARACTERS_RANGE[2]) { if (isNull(sb)) { var
	 * StringBuilder=createObject('java','java.lang.StringBuilder'); var sb = i == 1 ?
	 * StringBuilder.init() : StringBuilder.init(mid(str,1,i-1)); } sb&="?"; i++; } else if
	 * (!isNull(sb)) { sb&=c; } } return isNull(sb) ? str : sb.toString(); }
	 */
    fun replaceSurrogateCharacters(value: String, fromIndex: Int, replacement: String?): String {
        var fromIndex = fromIndex
        val max: Int = value.length()
        if (fromIndex < 0) {
            fromIndex = 0
        } else if (fromIndex >= max) {
            return value
        }
        var sb: StringBuilder? = null
        var c: Char
        var i: Int
        i = fromIndex
        while (i < max - 1) {
            c = value.charAt(i)
            if (c >= SURROGATE_CHARACTERS_RANGE[0] && c <= SURROGATE_CHARACTERS_RANGE[1]) {
                c = value.charAt(i + 1)
                if (c >= SURROGATE_CHARACTERS_RANGE[0] && c <= SURROGATE_CHARACTERS_RANGE[1]) {
                    if (sb == null) {
                        sb = StringBuilder()
                        if (i > 0) sb.append(value.substring(0, i))
                    }
                    i++
                    sb.append(replacement)
                    i++
                    continue
                }
            }
            if (sb != null) sb.append(c)
            i++
        }
        if (sb == null) return value
        if (i < value.length()) sb.append(value.charAt(value.length() - 1))
        return sb.toString()
    }

    fun indexOfSurrogateCharacters(value: String, fromIndex: Int): Int {
        var fromIndex = fromIndex
        val max: Int = value.length()
        if (fromIndex < 0) {
            fromIndex = 0
        } else if (fromIndex >= max) {
            return -1
        }
        var c: Char
        var i = fromIndex
        while (i < max - 1) {
            c = value.charAt(i)
            if (c >= SURROGATE_CHARACTERS_RANGE[0] && c <= SURROGATE_CHARACTERS_RANGE[1]) {
                c = value.charAt(i + 1)
                if (c >= SURROGATE_CHARACTERS_RANGE[0] && c <= SURROGATE_CHARACTERS_RANGE[1]) return i
                i++
            }
            i++
        }
        return -1
    }

    fun isCompatibleWith(value: String, cs: Charset?): Boolean {
        return value.equals(String(value.getBytes(cs), cs))
    }

    class Pos(private val position: Int, private val len: Int) {
        @Override
        override fun toString(): String {
            return "pos:$position;len:$len"
        }
    }
}