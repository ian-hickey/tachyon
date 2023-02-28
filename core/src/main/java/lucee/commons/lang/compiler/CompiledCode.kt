package lucee.commons.lang.compiler

import java.io.ByteArrayOutputStream

class CompiledCode(val className: String) : SimpleJavaFileObject(URI(className), Kind.CLASS) {
    private val baos: ByteArrayOutputStream = ByteArrayOutputStream()
    @Override
    @Throws(IOException::class)
    fun openOutputStream(): OutputStream {
        return baos
    }

    val byteCode: ByteArray
        get() = baos.toByteArray()
}