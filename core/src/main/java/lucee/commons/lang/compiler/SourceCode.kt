package lucee.commons.lang.compiler

import java.io.IOException

class SourceCode(val functionName: String, val className: String, private val contents: String) : SimpleJavaFileObject(URI.create("string:///" + className.replace('.', '/') + Kind.SOURCE.extension), Kind.SOURCE) {
    @Override
    @Throws(IOException::class)
    fun getCharContent(ignoreEncodingErrors: Boolean): CharSequence {
        return contents
    }
}