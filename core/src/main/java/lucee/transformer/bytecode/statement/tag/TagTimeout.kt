package lucee.transformer.bytecode.statement.tag

import org.objectweb.asm.Type

class TagTimeout(f: Factory?, start: Position?, end: Position?) : TagBaseNoFinal(f, start, end), ATagThread {
    private var index = 0
    @Throws(TransformerException::class)
    fun init() {
        val page: Page = ASMUtil.getAncestorPage(null, this)
        index = page.addThread(this)
    }

    @Override
    @Throws(TransformerException::class)
    override fun _writeOut(bc: BytecodeContext?) {
        val action: String = ASMUtil.getAttributeString(this, "action", "run")
        // no body
        if (!"run".equalsIgnoreCase(action)) {
            super._writeOut(bc)
            return
        }

        /*
		 * Attribute name = getAttribute("name"); if(name==null){ addAttribute(new Attribute(false,
		 * "name",bc.getFactory().createLitString("thread"+RandomUtil.createRandomStringLC(20)), "string"));
		 * }
		 */
        val adapter: GeneratorAdapter = bc.getAdapter()
        // Page page = ASMUtil.getAncestorPage(this);

        // int index=page.addThread(this);
        super._writeOut(bc, false)
        adapter.loadLocal(bc.getCurrentTag())
        adapter.loadThis()
        adapter.push(index)
        adapter.invokeVirtual(TIMEOUT_TAG, REGISTER)
    }

    /**
     * @see lucee.transformer.bytecode.statement.tag.TagBase.getBody
     */
    @Override
    override fun getBody(): Body? {
        return BodyBase(getFactory())
    }

    @Override
    override fun getRealBody(): Body? {
        return super.getBody()
    }

    companion object {
        val TIMEOUT_TAG: Type? = Type.getType(Timeout::class.java)
        private val REGISTER: Method? = Method("register", Types.VOID, arrayOf<Type?>(Types.PAGE, Types.INT_VALUE))
    }
}