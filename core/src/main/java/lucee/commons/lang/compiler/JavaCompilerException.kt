package lucee.commons.lang.compiler

import javax.tools.Diagnostic.Kind

class JavaCompilerException(message: String?, val lineNumber: Long, val columnNumber: Long, kind: Kind) : Exception(message) {
    private val kind: Kind
    fun getKind(): Kind {
        return kind
    }

    companion object {
        private const val serialVersionUID = 7791408833450791923L
    }

    init {
        this.kind = kind
    }
}