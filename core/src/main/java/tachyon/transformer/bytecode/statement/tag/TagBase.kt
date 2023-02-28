/**
 * Copyright (c) 2014, the Railo Company Ltd.
 * Copyright (c) 2015, Tachyon Assosication Switzerland
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
package tachyon.transformer.bytecode.statement.tag

import java.util.HashMap

/**
 *
 */
abstract class TagBase  // private Label finallyLabel;
(factory: Factory?, start: Position?, end: Position?) : StatementBase(factory, start, end), Tag {
    private var body: Body? = null
    private var appendix: String? = null
    private var fullname: String? = null
    private var tagLibTag: TagLibTag? = null
    var attributes: Map<String?, Attribute?>? = LinkedHashMap<String?, Attribute?>()

    // Map<String,String> missingAttributes=new HashMap<String,String>();
    var missingAttributes: HashSet<TagLibTagAttr?>? = HashSet<TagLibTagAttr?>()
    private var scriptBase = false
    private var metadata: Map<String?, Attribute?>? = null

    /**
     * @see tachyon.transformer.bytecode.statement.tag.Tag.getAppendix
     */
    @Override
    override fun getAppendix(): String? {
        return appendix
    }

    @Override
    override fun getAttributes(): Map<String?, Attribute?>? {
        return attributes
    }

    @Override
    override fun getFullname(): String? {
        return fullname
    }

    @Override
    override fun getTagLibTag(): TagLibTag? {
        return tagLibTag
    }

    @Override
    override fun setAppendix(appendix: String?) {
        this.appendix = appendix
    }

    @Override
    override fun setFullname(fullname: String?) {
        this.fullname = fullname
    }

    @Override
    override fun setTagLibTag(tagLibTag: TagLibTag?) {
        this.tagLibTag = tagLibTag
    }

    @Override
    override fun addAttribute(attribute: Attribute?) {
        attributes.put(attribute!!.getName().toLowerCase(), attribute)
    }

    @Override
    override fun containsAttribute(name: String?): Boolean {
        return attributes!!.containsKey(name.toLowerCase())
    }

    @Override
    override fun getBody(): Body? {
        return body
    }

    @Override
    override fun setBody(body: Body?) {
        this.body = body
        body.setParent(this)
    }

    @Override
    @Throws(TransformerException::class)
    fun _writeOut(bc: BytecodeContext?) {
        _writeOut(bc, true, null)
    }

    @Throws(TransformerException::class)
    fun _writeOut(bc: BytecodeContext?, doReuse: Boolean) {
        _writeOut(bc, doReuse, null)
    }

    @Throws(TransformerException::class)
    protected fun _writeOut(bc: BytecodeContext?, doReuse: Boolean, fcf: FlowControlFinal?) {
        // _writeOut(bc, true);
        val output = tagLibTag.getParseBody() || Caster.toBooleanValue(getAttribute("output"), false)
        if (output) {
            val pbv = ParseBodyVisitor()
            pbv.visitBegin(bc)
            TagHelper.writeOut(this, bc, doReuse, fcf)
            pbv.visitEnd(bc)
        } else TagHelper.writeOut(this, bc, doReuse, fcf)
    }

    @Override
    override fun getAttribute(name: String?): Attribute? {
        return attributes!![name.toLowerCase()]
    }

    @Override
    override fun removeAttribute(name: String?): Attribute? {
        return attributes.remove(name)
    }

    @Override
    override fun toString(): String {
        return appendix.toString() + ":" + fullname + ":" + super.toString()
    }

    @Override
    override fun isScriptBase(): Boolean {
        return scriptBase
    }

    @Override
    override fun setScriptBase(scriptBase: Boolean) {
        this.scriptBase = scriptBase
    }

    @Override
    override fun addMissingAttribute(attr: TagLibTagAttr?) {
        missingAttributes.add(attr)
    }

    @Override
    override fun getMissingAttributes(): Array<TagLibTagAttr?>? {
        return missingAttributes.toArray(arrayOfNulls<TagLibTagAttr?>(missingAttributes.size()))
    }

    @Override
    override fun addMetaData(metadata: Attribute?) {
        if (this.metadata == null) this.metadata = HashMap<String?, Attribute?>()
        this.metadata.put(metadata!!.getName(), metadata)
    }

    @Override
    override fun getMetaData(): Map<String?, Attribute?>? {
        return metadata
    }
}