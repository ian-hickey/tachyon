/**
 * Copyright (c) 2014, the Railo Company Ltd.
 * Copyright (c) 2015, Lucee Assosication Switzerland
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library.  If not, see <http:></http:>//www.gnu.org/licenses/>.
 *
 */
package lucee.transformer.bytecode.statement.tag

import org.objectweb.asm.Type

class TagThread(f: Factory?, start: Position?, end: Position?) : TagBaseNoFinal(f, start, end), ATagThread {
    private var index = 0
    @Throws(TransformerException::class)
    fun init() {
        val action: String = ASMUtil.getAttributeString(this, "action", "run")
        // no body
        if (!"run".equalsIgnoreCase(action)) return
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
        adapter.invokeVirtual(THREAD_TAG, REGISTER)
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
        val THREAD_TAG: Type? = Type.getType(ThreadTag::class.java)
        private val REGISTER: Method? = Method("register", Types.VOID, arrayOf<Type?>(Types.PAGE, Types.INT_VALUE))
    }
}