package lucee.runtime.regex

import lucee.commons.lang.StringUtil

object RegexFactory {
    const val TYPE_PERL = 1
    const val TYPE_JAVA = 2
    const val TYPE_UNDEFINED = 0
    fun toType(regexName: Int, defaultValue: String?): String? {
        if (regexName == TYPE_JAVA) return "java"
        return if (regexName == TYPE_PERL) "perl" else defaultValue
    }

    fun toRegex(regexName: Int, defaultValue: Regex?): Regex? {
        if (regexName == TYPE_JAVA) return JavaRegex()
        return if (regexName == TYPE_PERL) Perl5Regex() else defaultValue
    }

    fun toRegex(useJavaAsRegexEngine: Boolean): Regex? {
        return if (useJavaAsRegexEngine) JavaRegex() else Perl5Regex()
    }

    fun toType(regexName: String?, defaultValue: Int): Int {
        var regexName = regexName
        if (StringUtil.isEmpty(regexName, true)) return defaultValue
        regexName = regexName.trim()
        if ("java".equalsIgnoreCase(regexName) || "modern".equalsIgnoreCase(regexName)) return TYPE_JAVA else if ("perl".equalsIgnoreCase(regexName) || "perl5".equalsIgnoreCase(regexName) || "classic".equalsIgnoreCase(regexName)) return TYPE_PERL
        return defaultValue
    }

    @Throws(ApplicationException::class)
    fun toType(regexName: String?): Int {
        val res = toType(regexName, -1)
        if (res != -1) return res
        throw ApplicationException("invalid regex name [$regexName], valid names are [java or perl]")
    }
}