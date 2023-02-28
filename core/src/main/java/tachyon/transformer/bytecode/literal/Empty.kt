package tachyon.transformer.bytecode.literal

import org.objectweb.asm.Type

class Empty(f: Factory?, start: Position?, end: Position?) : ExpressionBase(f, start, end) {
    @Override
    @Throws(TransformerException::class)
    fun _writeOut(bc: BytecodeContext?, mode: Int): Type? {
        bc.getAdapter().loadArg(0)
        bc.getAdapter().invokeStatic(Types.NULL_SUPPORT_HELPER, EMPTY)
        return Types.OBJECT
    }

    companion object {
        private val EMPTY: Method? = Method("empty", Types.OBJECT, arrayOf<Type?>(Types.PAGE_CONTEXT))
    }
}