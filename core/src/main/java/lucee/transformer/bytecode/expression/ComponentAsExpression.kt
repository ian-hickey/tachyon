package lucee.transformer.bytecode.expression

import org.objectweb.asm.Opcodes

class ComponentAsExpression(tc: TagComponent?) : ExpressionBase(tc.getFactory(), tc.getStart(), tc.getEnd()) {
    private val tc: TagComponent?

    @Override
    @Throws(TransformerException::class)
    override fun _writeOut(bc: BytecodeContext?, mode: Int): Type? {
        // creates the component class file, but creates no output
        tc._writeOut(bc)

        // load the component
        val adapter: GeneratorAdapter = bc.getAdapter()
        val pageClassName: String = bc.getPage().getClassName()
        val inlineClassName: String = tc.getSubClassName(bc.getPage())
        // ASMConstants.NULL(adapter);
        adapter.visitTypeInsn(Opcodes.NEW, inlineClassName)
        adapter.visitInsn(Opcodes.DUP)

        /////// init class ///////
        adapter.visitVarInsn(Opcodes.ALOAD, 0)
        adapter.visitMethodInsn(Opcodes.INVOKEVIRTUAL, pageClassName, "getPageSource", "()Llucee/runtime/PageSource;")
        adapter.visitMethodInsn(Opcodes.INVOKESPECIAL, inlineClassName, "<init>", "(Llucee/runtime/PageSource;)V")
        adapter.checkCast(Types.CI_PAGE)
        /////// init class ///////
        adapter.loadArg(0)
        adapter.invokeStatic(COMPONENT_LOADER, LOAD_INLINE2)
        return Types.COMPONENT
    }

    /**
     * @return the closure
     */
    fun getTagComponent(): TagComponent? {
        return tc
    }

    companion object {
        private val COMPONENT_LOADER: Type? = Type.getType(ComponentLoader::class.java)

        // ComponentImpl loadInline(PageContext pc, CIPage page)
        private val LOAD_INLINE1: Method? = Method("loadInline", Types.COMPONENT_IMPL, arrayOf<Type?>(Types.CI_PAGE))
        private val LOAD_INLINE2: Method? = Method("loadInline", Types.COMPONENT_IMPL, arrayOf<Type?>(Types.CI_PAGE, Types.PAGE_CONTEXT))
    }

    init {
        this.tc = tc
    }
}