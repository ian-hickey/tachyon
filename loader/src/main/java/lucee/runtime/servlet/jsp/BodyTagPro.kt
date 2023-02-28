package lucee.runtime.servlet.jsp

import javax.servlet.jsp.tagext.BodyTag

interface BodyTagPro : BodyTag, TagPro {
    fun hasBody(hasBody: Boolean)
}