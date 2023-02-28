package lucee.runtime.db

import java.io.Serializable

class ParamSyntax private constructor(val leadingDelimiter: String, val delimiter: String, val separator: String) : Serializable {
    override fun toString(): String {
        return "delimiter:$delimiter;leadingDelimiter:$leadingDelimiter;separator:$separator"
    }

    override fun equals(obj: Object): Boolean {
        if (obj !is ParamSyntax) return false
        val other = obj as ParamSyntax
        return other.delimiter.equals(delimiter) && other.leadingDelimiter.equals(leadingDelimiter) && other.separator.equals(separator)
    }

    companion object {
        val DEFAULT = ParamSyntax("?", "&", "=")
        fun toParamSyntax(leadingDelimiter: String, delimiter: String, separator: String): ParamSyntax {
            return if (DEFAULT.delimiter.equals(delimiter) && DEFAULT.leadingDelimiter.equals(leadingDelimiter) && DEFAULT.separator.equals(separator)) DEFAULT else ParamSyntax(leadingDelimiter, delimiter, separator)
        }

        @Throws(PageException::class)
        fun toParamSyntax(sct: Struct): ParamSyntax {
            val del: String = Caster.toString(sct.get("delimiter"))
            var ledel: String = Caster.toString(sct.get("leadingDelimiter", null), null)
            if (StringUtil.isEmpty(ledel)) ledel = del
            return toParamSyntax(ledel, del, Caster.toString(sct.get("separator")))
        }

        fun toParamSyntax(sct: Struct, defaultValue: ParamSyntax): ParamSyntax {
            val del: String = Caster.toString(sct.get("param_delimiter", null), null)
            val sep: String = Caster.toString(sct.get("param_separator", null), null)
            if (StringUtil.isEmpty(del) || StringUtil.isEmpty(sep)) return defaultValue
            var ledel: String = Caster.toString(sct.get("param_leadingDelimiter", null), null)
            if (StringUtil.isEmpty(ledel)) ledel = del
            return toParamSyntax(ledel, del, sep)
        }

        fun toParamSyntax(el: Element, defaultValue: ParamSyntax): ParamSyntax {
            if (!el.hasAttribute("param-delimiter") || !el.hasAttribute("param-separator")) return defaultValue
            val del: String = URLDecoder.decode(el.getAttribute("param-delimiter"))
            var ledel: String = el.getAttribute("param-leading-delimiter")
            val sep: String = el.getAttribute("param-separator")
            if (StringUtil.isEmpty(ledel)) ledel = del
            return toParamSyntax(ledel, del, sep)
        }
    }
}