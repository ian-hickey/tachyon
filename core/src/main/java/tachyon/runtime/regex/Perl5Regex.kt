package tachyon.runtime.regex

import org.apache.oro.text.regex.MalformedPatternException

internal class Perl5Regex : Regex {
    @Override
    @Throws(PageException::class)
    override fun matches(strPattern: String?, strInput: String?): Boolean {
        return Perl5Util.matches(strPattern, strInput)
    }

    @Override
    override fun matches(strPattern: String?, strInput: String?, defaultValue: Boolean): Boolean {
        return Perl5Util.matches(strPattern, strInput, defaultValue)
    }

    @Override
    @Throws(PageException::class)
    override fun match(strPattern: String?, strInput: String?, offset: Int, caseSensitive: Boolean, multiLine: Boolean): String? {
        return try {
            Caster.toString(Perl5Util.match(strPattern, strInput, offset, caseSensitive, false, multiLine))
        } catch (e: MalformedPatternException) {
            throw Caster.toPageException(e)
        }
    }

    @Override
    @Throws(PageException::class)
    override fun matchAll(strPattern: String?, strInput: String?, offset: Int, caseSensitive: Boolean, multiLine: Boolean): Array? {
        return try {
            Perl5Util.match(strPattern, strInput, offset, caseSensitive, true, multiLine)
        } catch (e: MalformedPatternException) {
            throw Caster.toPageException(e)
        }
    }

    @Override
    @Throws(PageException::class)
    override fun indexOf(strPattern: String?, strInput: String?, offset: Int, caseSensitive: Boolean, multiLine: Boolean): Int {
        return try {
            Caster.toIntValue(Perl5Util.indexOf(strPattern, strInput, offset, caseSensitive, false, multiLine))
        } catch (e: MalformedPatternException) {
            throw Caster.toPageException(e)
        }
    }

    @Override
    @Throws(PageException::class)
    override fun indexOfAll(strPattern: String?, strInput: String?, offset: Int, caseSensitive: Boolean, multiLine: Boolean): Object? {
        return try {
            Perl5Util.indexOf(strPattern, strInput, offset, caseSensitive, true, multiLine)
        } catch (e: MalformedPatternException) {
            throw Caster.toPageException(e)
        }
    }

    @Override
    @Throws(PageException::class)
    override fun find(strPattern: String?, strInput: String?, offset: Int, caseSensitive: Boolean, multiLine: Boolean): Struct? {
        return try {
            Caster.toStruct(Perl5Util.find(strPattern, strInput, offset, caseSensitive, false, multiLine))
        } catch (e: MalformedPatternException) {
            throw Caster.toPageException(e)
        }
    }

    @Override
    @Throws(PageException::class)
    override fun findAll(strPattern: String?, strInput: String?, offset: Int, caseSensitive: Boolean, multiLine: Boolean): Array? {
        return try {
            Caster.toArray(Perl5Util.find(strPattern, strInput, offset, caseSensitive, true, multiLine))
        } catch (e: MalformedPatternException) {
            throw Caster.toPageException(e)
        }
    }

    @Override
    @Throws(PageException::class)
    override fun replace(strInput: String?, strPattern: String?, replacement: String?, caseSensitive: Boolean, multiLine: Boolean): String? {
        return try {
            Perl5Util.replace(strInput, strPattern, replacement, caseSensitive, false, multiLine)
        } catch (e: MalformedPatternException) {
            throw Caster.toPageException(e)
        }
    }

    @Override
    @Throws(PageException::class)
    override fun replaceAll(strInput: String?, strPattern: String?, replacement: String?, caseSensitive: Boolean, multiLine: Boolean): String? {
        return try {
            Perl5Util.replace(strInput, strPattern, replacement, caseSensitive, true, multiLine)
        } catch (e: MalformedPatternException) {
            throw Caster.toPageException(e)
        }
    }

    @Override
    override fun getTypeName(): String? {
        return "perl"
    }
}