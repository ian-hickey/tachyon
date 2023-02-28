package tachyon.runtime.regex

import java.util.regex.Matcher

internal class JavaRegex : Regex {
    @Override
    @Throws(PageException::class)
    override fun matches(strPattern: String?, strInput: String?): Boolean {
        return try {
            strInput.matches(strPattern)
        } catch (e: Exception) {
            throw Caster.toPageException(e)
        }
    }

    @Override
    override fun matches(strPattern: String?, strInput: String?, defaultValue: Boolean): Boolean {
        return try {
            strInput.matches(strPattern)
        } catch (e: Exception) {
            defaultValue
        }
    }

    @Override
    @Throws(PageException::class)
    override fun indexOf(strPattern: String?, strInput: String?, offset: Int, caseSensitive: Boolean, multiLine: Boolean): Int {
        return try {
            val strLen: Int = strInput!!.length()
            if (offset > strLen) return 0
            val matcher: Matcher = toPattern(strPattern, caseSensitive, multiLine).matcher(strInput)
            if (offset > 1) matcher.region(offset - 1, strLen)
            if (!matcher.find()) 0 else matcher.start() + 1
        } catch (e: Exception) {
            throw Caster.toPageException(e)
        }
    }

    @Override
    @Throws(PageException::class)
    override fun indexOfAll(strPattern: String?, strInput: String?, offset: Int, caseSensitive: Boolean, multiLine: Boolean): Object? {
        return try {
            val strLen: Int = strInput!!.length()
            if (offset > strLen) return 0
            val matcher: Matcher = toPattern(strPattern, caseSensitive, multiLine).matcher(strInput)
            if (offset > 1) matcher.region(offset - 1, strLen)
            var arr: ArrayImpl? = null
            while (matcher.find()) {
                if (arr == null) arr = ArrayImpl()
                arr.append(matcher.start() + 1)
            }
            if (arr == null) 0 else arr
        } catch (e: Exception) {
            throw Caster.toPageException(e)
        }
    }

    @Override
    @Throws(PageException::class)
    override fun find(strPattern: String?, strInput: String?, offset: Int, caseSensitive: Boolean, multiLine: Boolean): Struct? {
        return try {
            val strLen: Int = strInput!!.length()
            if (offset > strLen) return findEmpty()
            val matcher: Matcher = toPattern(strPattern, caseSensitive, multiLine).matcher(strInput)
            if (offset > 1) matcher.region(offset - 1, strLen)
            if (!matcher.find()) findEmpty() else toStruct(matcher, strInput)
        } catch (e: Exception) {
            throw Caster.toPageException(e)
        }
    }

    @Override
    @Throws(PageException::class)
    override fun findAll(strPattern: String?, strInput: String?, offset: Int, caseSensitive: Boolean, multiLine: Boolean): Array? {
        return try {
            val arr = ArrayImpl()
            val strLen: Int = strInput!!.length()
            if (offset > strLen) {
                arr.add(findEmpty())
                return arr
            }
            val matcher: Matcher = toPattern(strPattern, caseSensitive, multiLine).matcher(strInput)
            if (offset > 1) matcher.region(offset - 1, strLen)
            while (matcher.find()) {
                arr.append(toStruct(matcher, strInput))
            }
            if (arr.isEmpty()) arr.add(findEmpty())
            arr
        } catch (e: Exception) {
            throw Caster.toPageException(e)
        }
    }

    @Override
    @Throws(PageException::class)
    override fun match(strPattern: String?, strInput: String?, offset: Int, caseSensitive: Boolean, multiLine: Boolean): String? {
        return try {
            val matcher: Matcher = toPattern(strPattern, caseSensitive, multiLine).matcher(strInput)
            if (!matcher.find()) "" else strInput.substring(matcher.start(), matcher.end())
        } catch (e: Exception) {
            throw Caster.toPageException(e)
        }
    }

    @Override
    @Throws(PageException::class)
    override fun matchAll(strPattern: String?, strInput: String?, offset: Int, caseSensitive: Boolean, multiLine: Boolean): Array? {
        return try {
            val matcher: Matcher = toPattern(strPattern, caseSensitive, multiLine).matcher(strInput)
            val arr = ArrayImpl()
            while (matcher.find()) {
                arr.append(strInput.substring(matcher.start(), matcher.end()))
            }
            arr
        } catch (e: Exception) {
            throw Caster.toPageException(e)
        }
    }

    @Override
    @Throws(PageException::class)
    override fun replace(strInput: String?, strPattern: String?, replacement: String?, caseSensitive: Boolean, multiLine: Boolean): String? {
        return try {
            toPattern(strPattern, caseSensitive, multiLine).matcher(strInput).replaceFirst(replacement)
        } catch (e: Exception) {
            throw Caster.toPageException(e)
        }
    }

    @Override
    @Throws(PageException::class)
    override fun replaceAll(strInput: String?, strPattern: String?, replacement: String?, caseSensitive: Boolean, multiLine: Boolean): String? {
        return try {
            toPattern(strPattern, caseSensitive, multiLine).matcher(strInput).replaceAll(replacement)
        } catch (e: Exception) {
            throw Caster.toPageException(e)
        }
    }

    private fun findEmpty(): Struct? {
        val sct: Struct = StructImpl()
        var a: Array? = ArrayImpl()
        a.appendEL(ZERO)
        sct.setEL(LEN, a)
        a = ArrayImpl()
        a.appendEL(ZERO)
        sct.setEL(POS, a)
        a = ArrayImpl()
        a.appendEL("")
        sct.setEL(MATCH, a)
        return sct
    }

    private fun toStruct(matcher: Matcher?, input: String?): Struct? {
        val sct: Struct = StructImpl()
        val lenArray: Array = ArrayImpl()
        val posArray: Array = ArrayImpl()
        val matchArray: Array = ArrayImpl()
        for (i in 0..matcher.groupCount()) {
            lenArray.appendEL(matcher.end(i) - matcher.start(i))
            posArray.appendEL(matcher.start(i) + 1)
            matchArray.appendEL(matcher.group(i))
        }
        sct.setEL(POS, posArray)
        sct.setEL(LEN, lenArray)
        sct.setEL(MATCH, matchArray)
        return sct
    }

    private fun toPattern(strPattern: String?, caseSensitive: Boolean, multiLine: Boolean): Pattern? {
        var flags = 0
        if (!caseSensitive) flags += Pattern.CASE_INSENSITIVE
        if (multiLine) flags += Pattern.MULTILINE
        return Pattern.compile(strPattern, flags)
    }

    @Override
    override fun getTypeName(): String? {
        return "java"
    }

    companion object {
        private val ZERO: Double? = Double.valueOf(0)
        private val LEN: Key? = KeyConstants._len
        private val POS: Key? = KeyConstants._pos
        private val MATCH: Key? = KeyConstants._match
    }
}