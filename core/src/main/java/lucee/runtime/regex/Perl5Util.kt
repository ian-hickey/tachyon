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
package lucee.runtime.regex

import java.util.Map

/**
 *
 */
internal object Perl5Util {
    private val patterns: Map<String?, Pattern?>? = MapFactory.< String, Pattern>getConcurrentMap<String?, Pattern?>()

    /**
     * return index of the first occurence of the pattern in input text
     *
     * @param strPattern pattern to search
     * @param strInput text to search pattern
     * @param offset
     * @param caseSensitive
     * @return position of the first occurence
     * @throws MalformedPatternException
     */
    @Throws(MalformedPatternException::class)
    fun indexOf(strPattern: String?, strInput: String?, offset: Int, caseSensitive: Boolean, matchAll: Boolean, multiLine: Boolean): Object? {
        // Perl5Compiler compiler = new Perl5Compiler();
        var offset = offset
        val input = PatternMatcherInput(strInput)
        val matcher = Perl5Matcher()
        var compileOptions = if (caseSensitive) 0 else Perl5Compiler.CASE_INSENSITIVE_MASK
        compileOptions += if (multiLine) Perl5Compiler.MULTILINE_MASK else Perl5Compiler.SINGLELINE_MASK
        if (offset < 1) offset = 1
        val pattern: Pattern? = getPattern(strPattern, compileOptions)
        // Pattern pattern = compiler.compile(strPattern,compileOptions);
        if (offset <= strInput!!.length()) input.setCurrentOffset(offset - 1)
        if (offset <= strInput!!.length()) {
            val matches: Array = ArrayImpl()
            while (matcher.contains(input, pattern)) {
                val match: Int = matcher.getMatch().beginOffset(0) + 1
                if (!matchAll) {
                    return Double.valueOf(match)
                }
                matches.appendEL(match)
            }
            if (matches.size() !== 0) {
                return matches
            }
        }
        return 0
    }

    @Throws(MalformedPatternException::class)
    fun match(strPattern: String?, strInput: String?, offset: Int, caseSensitive: Boolean, matchAll: Boolean, multiLine: Boolean): Object? {
        var offset = offset
        val matcher = Perl5Matcher()
        val input = PatternMatcherInput(strInput)
        var compileOptions = if (caseSensitive) 0 else Perl5Compiler.CASE_INSENSITIVE_MASK
        compileOptions += if (multiLine) Perl5Compiler.MULTILINE_MASK else Perl5Compiler.SINGLELINE_MASK
        if (offset < 1) offset = 1
        val pattern: Pattern? = getPattern(strPattern, compileOptions)
        if (offset <= strInput!!.length()) input.setCurrentOffset(offset - 1)
        val rtn: Array = ArrayImpl()
        var result: MatchResult
        while (matcher.contains(input, pattern)) {
            result = matcher.getMatch()
            if (!matchAll) return result.toString()
            rtn.appendEL(result.toString())
        }
        return if (!matchAll) "" else rtn
    }

    /**
     * find occurence of a pattern in a string (same like indexOf), but dont return first ocurence , it
     * return struct with all information
     *
     * @param strPattern
     * @param strInput
     * @param offset
     * @param caseSensitive
     * @return
     * @throws MalformedPatternException
     */
    @Throws(MalformedPatternException::class)
    fun find(strPattern: String?, strInput: String?, offset: Int, caseSensitive: Boolean, matchAll: Boolean, multiLine: Boolean): Object? {
        var offset = offset
        val matcher = Perl5Matcher()
        val input = PatternMatcherInput(strInput)
        val matches: Array = ArrayImpl()
        var compileOptions = if (caseSensitive) 0 else Perl5Compiler.CASE_INSENSITIVE_MASK
        compileOptions += if (multiLine) Perl5Compiler.MULTILINE_MASK else Perl5Compiler.SINGLELINE_MASK
        if (offset < 1) offset = 1
        val pattern: Pattern? = getPattern(strPattern, compileOptions)
        if (offset <= strInput!!.length()) {
            input.setCurrentOffset(offset - 1)
            while (matcher.contains(input, pattern)) {
                val matchStruct: Struct? = getMatchStruct(matcher.getMatch(), strInput)
                if (!matchAll) {
                    return matchStruct
                }
                matches.appendEL(matchStruct)
            }
            if (matches.size() !== 0) {
                return matches
            }
        }
        val posArray: Array = ArrayImpl()
        val lenArray: Array = ArrayImpl()
        val matchArray: Array = ArrayImpl()
        posArray.appendEL(Constants.INTEGER_0)
        lenArray.appendEL(Constants.INTEGER_0)
        matchArray.appendEL("")
        val struct: Struct = StructImpl()
        struct.setEL("pos", posArray)
        struct.setEL("len", lenArray)
        struct.setEL("match", matchArray)
        if (matchAll) {
            matches.appendEL(struct)
            return matches
        }
        return struct
    }

    fun getMatchStruct(result: MatchResult?, input: String?): Struct? {
        val groupCount: Int = result.groups()
        val posArray: Array = ArrayImpl()
        val lenArray: Array = ArrayImpl()
        val matchArray: Array = ArrayImpl()
        var beginOff: Int
        var endOff: Int
        for (i in 0 until groupCount) {
            beginOff = result.beginOffset(i)
            endOff = result.endOffset(i)
            if (beginOff == -1 && endOff == -1) {
                posArray.appendEL(Integer.valueOf(0))
                lenArray.appendEL(Integer.valueOf(0))
                matchArray.appendEL(null)
                continue
            }
            posArray.appendEL(Integer.valueOf(beginOff + 1))
            lenArray.appendEL(Integer.valueOf(endOff - beginOff))
            matchArray.appendEL(input.substring(beginOff, endOff))
        }
        val struct: Struct = StructImpl()
        struct.setEL("pos", posArray)
        struct.setEL("len", lenArray)
        struct.setEL("match", matchArray)
        return struct
    }

    @Throws(MalformedPatternException::class)
    private fun _matches(strPattern: String?, strInput: String?): Boolean {
        val pattern: Pattern = Perl5Compiler().compile(strPattern, Perl5Compiler.DEFAULT_MASK)
        val input = PatternMatcherInput(strInput)
        return Perl5Matcher().matches(input, pattern)
    }

    @Throws(PageException::class)
    fun matches(strPattern: String?, strInput: String?): Boolean {
        return try {
            _matches(strPattern, strInput)
        } catch (e: MalformedPatternException) {
            throw ExpressionException("The provided pattern [$strPattern] is invalid", e.getMessage())
        }
    }

    fun matches(strPattern: String?, strInput: String?, defaultValue: Boolean): Boolean {
        return try {
            _matches(strPattern, strInput)
        } catch (e: MalformedPatternException) {
            defaultValue
        }
    }

    @Throws(MalformedPatternException::class)
    private fun getPattern(strPattern: String?, type: Int): Pattern? {
        val o: Object? = patterns!![strPattern + type]
        if (o == null) {
            val pattern: Pattern = Perl5Compiler().compile(strPattern, type)
            patterns.put(strPattern + type, pattern)
            return pattern
        }
        return o as Pattern?
    }

    /**
     * replace the first/all occurence of given pattern
     *
     * @param strInput text to search pattern
     * @param strPattern pattern to search
     * @param replacement text to replace with pattern
     * @param caseSensitive
     * @param replaceAll do replace all or only one
     * @return transformed text
     * @throws MalformedPatternException
     */
    @Throws(MalformedPatternException::class)
    fun replace(strInput: String?, strPattern: String?, replacement: String?, caseSensitive: Boolean, replaceAll: Boolean, multiLine: Boolean): String? {
        return _replace(strInput, strPattern, escape(replacement), caseSensitive, replaceAll, multiLine)
    }

    @Throws(MalformedPatternException::class)
    private fun _replace(strInput: String?, strPattern: String?, replacement: String?, caseSensitive: Boolean, replaceAll: Boolean, multiLine: Boolean): String? {
        val flag: Int = (if (caseSensitive) 0 else Perl5Compiler.CASE_INSENSITIVE_MASK) + if (multiLine) Perl5Compiler.MULTILINE_MASK else Perl5Compiler.SINGLELINE_MASK
        val pattern: Pattern? = getPattern(strPattern, flag)
        return Util.substitute(Perl5Matcher(), pattern, Perl5Substitution(replacement), strInput, if (replaceAll) -1 else 1)
    }

    @Throws(MalformedPatternException::class)
    private fun escape(replacement: String?): String? {
        var replacement = replacement
        replacement = _replace(replacement, "\\\\", "\\\\\\\\", false, true, false)
        replacement = _escape(replacement)
        replacement = _replace(replacement, "\\\\\\\\(\\d)", "\\$$1", false, true, false)
        return replacement
    }

    private fun _escape(str: String?): String? {
        val sb = StringBuffer()
        val len: Int = str!!.length()
        var c: Char
        var i = 0
        while (i < len) {
            c = str.charAt(i)
            if ('+' == c) sb.append("\\+") else if ('?' == c) sb.append("\\?") else if ('$' == c) sb.append("\\$") else if ('^' == c) sb.append("\\^") else if ('\\' == c) {
                if (i + 1 < len) {
                    val n: Char = str.charAt(i + 1)
                    if ('\\' == n) {
                        if (i + 2 < len) {
                            val nn: Char = str.charAt(i + 2)
                            var x = 0.toChar()
                            if ('U' == nn) x = 'U' else if ('L' == nn) x = 'L' else if ('u' == nn) x = 'u' else if ('l' == nn) x = 'l' else if ('E' == nn) x = 'E'
                            // else if('d'==nn) x='d';
                            if (x.toInt() != 0) {
                                sb.append("\\" + x)
                                i += 2
                                i++
                                continue
                            }
                        }
                    }
                }
                sb.append(c)
            } else sb.append(c)
            i++
        }
        return sb.toString()
    }
}