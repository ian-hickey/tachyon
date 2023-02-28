package tachyon.runtime.servlet.jsp

import javax.servlet.jsp.tagext.Tag

interface TagPro : Tag {
    fun setAppendix(appendix: String?)
    fun setMetaData(name: String?, value: Object?)
}