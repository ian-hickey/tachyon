package lucee.commons.lang.compiler

import java.util.HashMap

class DynamicClassLoader(parent: ClassLoader?) : ClassLoader(parent) {
    private val customCompiledCode: Map<String, CompiledCode> = HashMap()
    fun addCode(cc: CompiledCode) {
        customCompiledCode.put(cc.getName(), cc)
    }

    @Override
    @Throws(ClassNotFoundException::class)
    protected fun findClass(name: String): Class<*> {
        val cc: CompiledCode = customCompiledCode[name] ?: return super.findClass(name)
        val byteCode: ByteArray = cc.getByteCode()
        return defineClass(name, byteCode, 0, byteCode.size)
    }

    fun getCompiledCode(name: String): CompiledCode? {
        return customCompiledCode[name]
    }
}