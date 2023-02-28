package lucee.commons.lang.compiler

import java.io.ByteArrayInputStream

class CustomJavaFileObject(javaObjectName: String, uri: URI, `is`: InputStream?, kind: Kind) : JavaFileObject {
    private val binaryName: String
    private val uri: URI

    @get:Override
    val name: String
    private var baos: ByteArrayOutputStream? = null
    private var kind: Kind
    @Override
    fun toUri(): URI {
        return uri
    }

    @Override
    @Throws(IOException::class)
    fun openInputStream(): InputStream {
        val byteArray: ByteArray = baos.toByteArray()
        return ByteArrayInputStream(byteArray)
    }

    @Override
    @Throws(IOException::class)
    fun openOutputStream(): OutputStream? {
        baos = ByteArrayOutputStream()
        return baos
    }

    @Override
    @Throws(IOException::class)
    fun openReader(ignoreEncodingErrors: Boolean): Reader {
        return InputStreamReader(openInputStream())
    }

    @Override
    @Throws(IOException::class)
    fun getCharContent(ignoreEncodingErrors: Boolean): CharSequence {
        if (baos != null) {
            val b: ByteArray = baos.toByteArray()
            return String(b)
        }
        throw UnsupportedOperationException()
    }

    @Override
    @Throws(IOException::class)
    fun openWriter(): Writer {
        throw UnsupportedOperationException()
    }

    @get:Override
    val lastModified: Long
        get() = 0

    @Override
    fun delete(): Boolean {
        throw UnsupportedOperationException()
    }

    @Override
    fun getKind(): Kind {
        return kind
    }

    fun setKind(k: Kind) {
        kind = k
    }

    @Override
    fun isNameCompatible(simpleName: String, kind: Kind): Boolean {
        val baseName = simpleName + kind.extension
        return kind.equals(getKind()) && (baseName.equals(name) || name.endsWith("/$baseName"))
    }

    @get:Override
    val nestingKind: NestingKind
        get() {
            throw UnsupportedOperationException()
        }

    @get:Override
    val accessLevel: Modifier
        get() {
            throw UnsupportedOperationException()
        }

    fun binaryName(): String {
        return binaryName
    }

    @Override
    override fun toString(): String {
        return "CustomJavaFileObject{uri=$uri}"
    }

    val size: Int
        get() = if (baos == null) {
            -1
        } else {
            baos.size()
        }

    init {
        this.uri = uri
        binaryName = javaObjectName
        this.kind = kind
        var stripName = javaObjectName
        if (stripName.endsWith("/")) {
            stripName = stripName.substring(0, stripName.length() - 1)
        }
        name = javaObjectName.substring(javaObjectName.lastIndexOf('/') + 1)
        if (`is` != null) {
            baos = ByteArrayOutputStream()
            IOUtil.copy(`is`, baos, false, true)
        }
    }
}