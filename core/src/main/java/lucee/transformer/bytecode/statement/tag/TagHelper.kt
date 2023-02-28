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

import java.util.Iterator

object TagHelper {
    private val MISSING_ATTRIBUTE: Type? = Type.getType(MissingAttribute::class.java)
    private val MISSING_ATTRIBUTE_ARRAY: Type? = Type.getType(Array<MissingAttribute>::class.java)
    private val BODY_TAG: Type? = Type.getType(BodyTag::class.java)
    private val TAG: Type? = Type.getType(javax.servlet.jsp.tagext.Tag::class.java)
    private val TRY_CATCH_FINALLY_TAG: Type? = Type.getType(javax.servlet.jsp.tagext.TryCatchFinally::class.java)
    private val TAG_UTIL: Type? = Type.getType(lucee.runtime.tag.TagUtil::class.java)

    // TagUtil.setAttributeCollection(Tag, Struct)
    private val SET_ATTRIBUTE_COLLECTION: Method? = Method("setAttributeCollection", Types.VOID, arrayOf<Type?>(Types.PAGE_CONTEXT, TAG, MISSING_ATTRIBUTE_ARRAY, Types.STRUCT, Types.INT_VALUE))

    // Tag use(String)
    private val USE4: Method? = Method("use", TAG, arrayOf<Type?>(Types.STRING, Types.STRING, Types.INT_VALUE, Types.STRING))
    private val USE6: Method? = Method("use", TAG, arrayOf<Type?>(Types.STRING, Types.STRING, Types.STRING, Types.STRING, Types.INT_VALUE, Types.STRING))

    // void setAppendix(String appendix)
    private val SET_APPENDIX1: Method? = Method("setAppendix", Type.VOID_TYPE, arrayOf<Type?>(Types.STRING))

    // void setAppendix(String appendix)
    private val SET_APPENDIX2: Method? = Method("setAppendix", Type.VOID_TYPE, arrayOf<Type?>(Types.TAG, Types.STRING))

    // void setDynamicAttribute(String uri, String name, Object value)
    private val SET_DYNAMIC_ATTRIBUTE: Method? = Method("setDynamicAttribute", Type.VOID_TYPE, arrayOf<Type?>(Types.STRING, Types.COLLECTION_KEY, Types.OBJECT))

    // public static void setAttribute(PageContext pc,boolean doDynamic,boolean silently,Tag tag, String
    // name,Object value) throws PageException {
    private val SET_ATTRIBUTE4: Method? = Method("setAttribute", Type.VOID_TYPE, arrayOf<Type?>(Types.PAGE_CONTEXT, TAG, Types.STRING, Types.OBJECT))
    private val SET_META_DATA2: Method? = Method("setMetaData", Type.VOID_TYPE, arrayOf<Type?>(Types.STRING, Types.OBJECT))
    private val SET_META_DATA3: Method? = Method("setMetaData", Type.VOID_TYPE, arrayOf<Type?>(Types.TAG, Types.STRING, Types.OBJECT))

    // void hasBody(boolean hasBody)
    private val HAS_BODY1: Method? = Method("hasBody", Type.VOID_TYPE, arrayOf<Type?>(Types.BOOLEAN_VALUE))

    // void hasBody(boolean hasBody)
    private val HAS_BODY2: Method? = Method("hasBody", Type.VOID_TYPE, arrayOf<Type?>(Types.TAG, Types.BOOLEAN_VALUE))

    // int doStartTag()
    private val DO_START_TAG: Method? = Method("doStartTag", Types.INT_VALUE, arrayOf<Type?>())

    // int doEndTag()
    private val DO_END_TAG: Method? = Method("doEndTag", Types.INT_VALUE, arrayOf<Type?>())
    private val ABORT: Type? = Type.getType(Abort::class.java)

    // private static final Type EXPRESSION_EXCEPTION = Type.getType(ExpressionException.class);
    // ExpressionException newInstance(int)
    private val NEW_INSTANCE: Method? = Method("newInstance", ABORT, arrayOf<Type?>(Types.INT_VALUE))
    private val NEW_INSTANCE_MAX2: Method? = Method("newInstance", MISSING_ATTRIBUTE, arrayOf<Type?>(Types.COLLECTION_KEY, Types.STRING))
    private val NEW_INSTANCE_MAX3: Method? = Method("newInstance", MISSING_ATTRIBUTE, arrayOf<Type?>(Types.COLLECTION_KEY, Types.STRING, Types.STRING_ARRAY))

    // void initBody(BodyTag bodyTag, int state)
    private val INIT_BODY: Method? = Method("initBody", Types.VOID, arrayOf<Type?>(BODY_TAG, Types.INT_VALUE))

    // int doAfterBody()
    private val DO_AFTER_BODY: Method? = Method("doAfterBody", Types.INT_VALUE, arrayOf<Type?>())

    // void doCatch(Throwable t)
    private val DO_CATCH: Method? = Method("doCatch", Types.VOID, arrayOf<Type?>(Types.THROWABLE))

    // void doFinally()
    private val DO_FINALLY: Method? = Method("doFinally", Types.VOID, arrayOf<Type?>())

    // JspWriter popBody()
    private val POP_BODY: Method? = Method("popBody", Types.JSP_WRITER, arrayOf<Type?>())

    // void reuse(Tag tag)
    private val RE_USE1: Method? = Method("reuse", Types.VOID, arrayOf<Type?>(Types.TAG))
    private val RE_USE3: Method? = Method("reuse", Types.VOID, arrayOf<Type?>(Types.TAG, Types.STRING, Types.STRING))

    /**
     * writes out the tag
     *
     * @param tag
     * @param bc
     * @param doReuse
     * @throws TransformerException
     * @throws BundleException
     * @throws ClassException
     */
    @Throws(TransformerException::class)
    fun writeOut(tag: Tag?, bc: BytecodeContext?, doReuse: Boolean, fcf: FlowControlFinal?) {
        val adapter: GeneratorAdapter = bc.getAdapter()
        val tlt: TagLibTag = tag!!.getTagLibTag()
        val cd: ClassDefinition = tlt.getTagClassDefinition()
        val fromBundle = cd.getName() != null
        val currType: Type?
        val currDoFinallyType: Type?
        if (fromBundle) {
            try {
                currType = if (Reflector.isInstaneOf(cd.getClazz(), BodyTag::class.java, false)) BODY_TAG else TAG
                currDoFinallyType = TRY_CATCH_FINALLY_TAG
            } catch (e: Exception) {
                if (e is TransformerException) throw e as TransformerException
                throw TransformerException(bc, e, tag.getStart())
            }
        } else {
            currType = getTagType(bc, tag)
            currDoFinallyType = currType
        }
        val currLocal: Int = adapter.newLocal(currType)
        val tagBegin = Label()
        val tagEnd = Label()
        ExpressionUtil.visitLine(bc, tag.getStart())
        // TODO adapter.visitLocalVariable("tag", "L"+currType.getInternalName()+";", null, tagBegin,
        // tagEnd, currLocal);
        adapter.visitLabel(tagBegin)
        // tag=pc.use(String tagClassName,String tagBundleName, String tagBundleVersion, String fullname,int
        // attrType) throws PageException {
        adapter.loadArg(0)
        adapter.checkCast(Types.PAGE_CONTEXT_IMPL)
        adapter.push(cd.getClassName())
        // has bundle info/version
        if (fromBundle) {
            // name
            adapter.push(cd.getName())
            // version
            if (cd.getVersion() != null) adapter.push(cd.getVersionAsString()) else ASMConstants.NULL(adapter)
        }
        adapter.push(tlt.getFullName())
        adapter.push(tlt.getAttributeType())
        adapter.push((if (bc.getPageSource() == null) "<memory>" else bc.getPageSource().getDisplayPath()) + ":" + if (tag.getStart() == null) 0 else tag.getStart().line)
        adapter.invokeVirtual(Types.PAGE_CONTEXT_IMPL, if (fromBundle) USE6 else USE4)
        if (currType !== TAG) adapter.checkCast(currType)
        adapter.storeLocal(currLocal)
        val outerTcfv = TryFinallyVisitor(object : OnFinally() {
            @Override
            fun _writeOut(bc: BytecodeContext?) {
                adapter.loadArg(0)
                adapter.checkCast(Types.PAGE_CONTEXT_IMPL)
                adapter.loadLocal(currLocal)
                if (cd.getName() != null) {
                    adapter.push(cd.getName())
                    if (cd.getVersion() != null) adapter.push(cd.getVersionAsString()) else ASMConstants.NULL(adapter)
                }
                adapter.invokeVirtual(Types.PAGE_CONTEXT_IMPL, if (fromBundle) RE_USE3 else RE_USE1)
            }
        }, null)
        if (doReuse) outerTcfv.visitTryBegin(bc)

        // appendix
        if (tlt.hasAppendix()) {
            adapter.loadLocal(currLocal)
            adapter.push(tag!!.getAppendix())
            if (fromBundle) // PageContextUtil.setAppendix(tag,appendix)
                ASMUtil.invoke(ASMUtil.STATIC, adapter, Types.TAG_UTIL, SET_APPENDIX2) else  // tag.setAppendix(appendix)
                ASMUtil.invoke(ASMUtil.VIRTUAL, adapter, currType, SET_APPENDIX1)
        }

        // hasBody
        val hasBody = tag!!.getBody() != null
        if (tlt.isBodyFree() && tlt.hasBodyMethodExists()) {
            adapter.loadLocal(currLocal)
            adapter.push(hasBody)
            if (fromBundle) // PageContextUtil.setAppendix(tag,appendix)
                ASMUtil.invoke(ASMUtil.STATIC, adapter, Types.TAG_UTIL, HAS_BODY2) else  // tag.setAppendix(appendix)
                ASMUtil.invoke(ASMUtil.VIRTUAL, adapter, currType, HAS_BODY1)
        }

        // default attributes (get overwritten by attributeCollection because of that set before)
        setAttributes(bc, tag, currLocal, currType, true, fromBundle)

        // attributeCollection
        val attrColl: Attribute = tag!!.getAttribute("attributecollection")
        if (attrColl != null) {
            val attrType: Int = tag!!.getTagLibTag().getAttributeType()
            if (TagLibTag.ATTRIBUTE_TYPE_NONAME !== attrType) {
                tag!!.removeAttribute("attributecollection")
                // TagUtil.setAttributeCollection(Tag, Struct)
                adapter.loadArg(0)
                adapter.loadLocal(currLocal)
                if (currType !== TAG) adapter.cast(currType, TAG)

                ///
                val missings: Array<TagLibTagAttr?> = tag!!.getMissingAttributes()
                if (!ArrayUtil.isEmpty(missings)) {
                    val av = ArrayVisitor()
                    av.visitBegin(adapter, MISSING_ATTRIBUTE, missings.size)
                    var count = 0
                    var miss: TagLibTagAttr?
                    for (i in missings.indices) {
                        miss = missings[i]
                        av.visitBeginItem(adapter, count++)
                        bc.getFactory().registerKey(bc, bc.getFactory().createLitString(miss.getName()), false)
                        adapter.push(miss.getType())
                        if (ArrayUtil.isEmpty(miss.getAlias())) adapter.invokeStatic(MISSING_ATTRIBUTE, NEW_INSTANCE_MAX2) else {
                            LiteralStringArray(bc.getFactory(), miss.getAlias()).writeOut(bc, Expression.MODE_REF)
                            adapter.invokeStatic(MISSING_ATTRIBUTE, NEW_INSTANCE_MAX3)
                        }
                        av.visitEndItem(bc.getAdapter())
                    }
                    av.visitEnd()
                } else {
                    ASMConstants.NULL(adapter)
                }
                ///
                attrColl.getValue()!!.writeOut(bc, Expression.MODE_REF)
                adapter.push(attrType)
                adapter.invokeStatic(TAG_UTIL, SET_ATTRIBUTE_COLLECTION)
            }
        }

        // metadata
        var attr: Attribute
        val metadata: Map<String?, Attribute?> = tag!!.getMetaData()
        if (metadata != null) {
            val it: Iterator<Attribute?> = metadata.values().iterator()
            while (it.hasNext()) {
                attr = it.next()
                adapter.loadLocal(currLocal)
                adapter.push(attr!!.getName())
                attr!!.getValue()!!.writeOut(bc, Expression.MODE_REF)
                if (fromBundle) ASMUtil.invoke(ASMUtil.STATIC, adapter, Types.TAG_UTIL, SET_META_DATA3) else ASMUtil.invoke(ASMUtil.VIRTUAL, adapter, currType, SET_META_DATA2)
            }
        }

        // set attributes
        setAttributes(bc, tag, currLocal, currType, false, fromBundle)
        // Body
        if (hasBody) {
            val state: Int = adapter.newLocal(Types.INT_VALUE)

            // int state=tag.doStartTag();
            adapter.loadLocal(currLocal)
            ASMUtil.invoke(if (fromBundle) ASMUtil.INTERFACE else ASMUtil.VIRTUAL, adapter, currType, DO_START_TAG)
            // adapter.invokeVirtual(currType, DO_START_TAG);
            adapter.storeLocal(state)

            // if (state!=Tag.SKIP_BODY)
            val endBody = Label()
            adapter.loadLocal(state)
            adapter.push(javax.servlet.jsp.tagext.Tag.SKIP_BODY)
            adapter.visitJumpInsn(Opcodes.IF_ICMPEQ, endBody)
            // pc.initBody(tag, state);
            adapter.loadArg(0)
            adapter.loadLocal(currLocal)
            adapter.loadLocal(state)
            adapter.invokeVirtual(Types.PAGE_CONTEXT, INIT_BODY)
            val onFinally: OnFinally = object : OnFinally() {
                @Override
                fun _writeOut(bc: BytecodeContext?) {
                    val endIf = Label()
                    /*
					 * if(tlt.handleException() && fcf!=null && fcf.getAfterFinalGOTOLabel()!=null){
					 * ASMUtil.visitLabel(adapter, fcf.getFinalEntryLabel()); }
					 */adapter.loadLocal(state)
                    adapter.push(javax.servlet.jsp.tagext.Tag.EVAL_BODY_INCLUDE)
                    adapter.visitJumpInsn(Opcodes.IF_ICMPEQ, endIf)
                    // ... pc.popBody();
                    adapter.loadArg(0)
                    adapter.invokeVirtual(Types.PAGE_CONTEXT, POP_BODY)
                    adapter.pop()
                    adapter.visitLabel(endIf)

                    // tag.doFinally();
                    if (tlt.handleException()) {
                        adapter.loadLocal(currLocal)
                        ASMUtil.invoke(if (fromBundle) ASMUtil.INTERFACE else ASMUtil.VIRTUAL, adapter, currDoFinallyType, DO_FINALLY)
                        // adapter.invokeVirtual(currType, DO_FINALLY);
                    }
                    // GOTO after execution body, used when a continue/break was called before
                    /*
					 * if(fcf!=null) { Label l = fcf.getAfterFinalGOTOLabel();
					 * if(l!=null)adapter.visitJumpInsn(Opcodes.GOTO, l); }
					 */
                }
            }
            if (tlt.handleException()) {
                val tcfv = TryCatchFinallyVisitor(onFinally, fcf)
                tcfv.visitTryBegin(bc)
                doTry(bc, adapter, tag, currLocal, currType, fromBundle)
                val t: Int = tcfv.visitTryEndCatchBeging(bc)
                // tag.doCatch(t);
                adapter.loadLocal(currLocal)
                adapter.loadLocal(t)
                // adapter.visitVarInsn(Opcodes.ALOAD,t);
                ASMUtil.invoke(if (fromBundle) ASMUtil.INTERFACE else ASMUtil.VIRTUAL, adapter, currDoFinallyType, DO_CATCH)
                // adapter.invokeVirtual(currType, DO_CATCH);
                tcfv.visitCatchEnd(bc)
            } else {
                val tfv = TryFinallyVisitor(onFinally, fcf)
                tfv.visitTryBegin(bc)
                doTry(bc, adapter, tag, currLocal, currType, fromBundle)
                tfv.visitTryEnd(bc)
            }
            adapter.visitLabel(endBody)
        } else {
            // tag.doStartTag();
            adapter.loadLocal(currLocal)
            ASMUtil.invoke(if (fromBundle) ASMUtil.INTERFACE else ASMUtil.VIRTUAL, adapter, currType, DO_START_TAG)
            // adapter.invokeVirtual(currType, DO_START_TAG);
            adapter.pop()
        }

        // if (tag.doEndTag()==Tag.SKIP_PAGE) throw new Abort(0<!-- SCOPE_PAGE -->);
        val endDoEndTag = Label()
        adapter.loadLocal(currLocal)
        ASMUtil.invoke(if (fromBundle) ASMUtil.INTERFACE else ASMUtil.VIRTUAL, adapter, currType, DO_END_TAG)
        // adapter.invokeVirtual(currType, DO_END_TAG);
        adapter.push(javax.servlet.jsp.tagext.Tag.SKIP_PAGE)
        adapter.visitJumpInsn(Opcodes.IF_ICMPNE, endDoEndTag)
        adapter.push(Abort.SCOPE_PAGE)
        adapter.invokeStatic(ABORT, NEW_INSTANCE)
        adapter.throwException()
        adapter.visitLabel(endDoEndTag)
        if (doReuse) {
            // } finally{pc.reuse(tag);}
            outerTcfv.visitTryEnd(bc)
        }
        adapter.visitLabel(tagEnd)
        ExpressionUtil.visitLine(bc, tag.getEnd())
    }

    @Throws(TransformerException::class)
    private fun setAttributes(bc: BytecodeContext?, tag: Tag?, currLocal: Int, currType: Type?, doDefault: Boolean, interf: Boolean) {
        val adapter: GeneratorAdapter = bc.getAdapter()
        val attributes: Map<String?, Attribute?> = tag!!.getAttributes()
        var methodName: String
        var attr: Attribute
        val it: Iterator<Attribute?> = attributes.values().iterator()
        while (it.hasNext()) {
            attr = it.next()
            if (doDefault != attr!!.isDefaultAttribute()) continue
            if (attr!!.isDynamicType()) {
                adapter.loadLocal(currLocal)
                if (interf) adapter.checkCast(Types.DYNAMIC_ATTRIBUTES)
                adapter.visitInsn(Opcodes.ACONST_NULL)
                // adapter.push(attr.getName());
                bc.getFactory().registerKey(bc, bc.getFactory().createLitString(attr!!.getName()), false)
                attr!!.getValue()!!.writeOut(bc, Expression.MODE_REF)
                ASMUtil.invoke(if (interf) ASMUtil.INTERFACE else ASMUtil.VIRTUAL, adapter, if (interf) Types.DYNAMIC_ATTRIBUTES else currType, SET_DYNAMIC_ATTRIBUTE)
                // adapter.invokeVirtual(currType, SET_DYNAMIC_ATTRIBUTE);
            } else {
                // TagUtil.setAttribute(PageContext pc,boolean doDynamic,boolean silently,Tag tag, String
                // name,Object value)
                if (interf) {
                    adapter.loadArg(0) // pc
                    adapter.loadLocal(currLocal) // tag
                    bc.getFactory().createLitString(attr!!.getName()).writeOut(bc, Expression.MODE_REF) // name
                    attr!!.getValue()!!.writeOut(bc, Expression.MODE_REF) // value
                    adapter.invokeStatic(TAG_UTIL, SET_ATTRIBUTE4)
                } else {
                    val type: Type = CastOther.getType(bc, attr!!.getType())
                    methodName = tag!!.getTagLibTag().getSetter(attr, if (type == null) null else type.getClassName())
                    adapter.loadLocal(currLocal)
                    writeNumberAsDouble(bc, attr, if (Types.isPrimitiveType(type)) Expression.MODE_VALUE else Expression.MODE_REF)
                    adapter.invokeVirtual(currType, Method(methodName, Type.VOID_TYPE, arrayOf<Type?>(type)))
                }
            }
        }
    }

    @Throws(TransformerException::class)
    private fun writeNumberAsDouble(bc: BytecodeContext?, attr: Attribute?, i: Int) {
        val type: Type = CastOther.getType(bc, attr!!.getType())
        var expr: Expression = attr!!.getValue()
        if (type.equals(Types.DOUBLE_VALUE) && attr!!.getValue() !is ExprNumber) {
            expr = CastNumber.toExprNumber(attr!!.getValue())
        }
        expr.writeOut(bc, if (Types.isPrimitiveType(type)) Expression.MODE_VALUE else Expression.MODE_REF)
    }

    @Throws(TransformerException::class)
    private fun doTry(bc: BytecodeContext?, adapter: GeneratorAdapter?, tag: Tag?, currLocal: Int, currType: Type?, interf: Boolean) {
        val beginDoWhile = Label()
        adapter.visitLabel(beginDoWhile)
        bc.setCurrentTag(currLocal)
        tag!!.getBody().writeOut(bc)

        // while (tag.doAfterBody()==BodyTag.EVAL_BODY_AGAIN);
        adapter.loadLocal(currLocal)
        if (interf) adapter.checkCast(Types.BODY_TAG)
        ASMUtil.invoke(if (interf) ASMUtil.INTERFACE else ASMUtil.VIRTUAL, adapter, currType, DO_AFTER_BODY)
        // adapter.invokeVirtual(currType, DO_AFTER_BODY);
        adapter.push(IterationTag.EVAL_BODY_AGAIN)
        adapter.visitJumpInsn(Opcodes.IF_ICMPEQ, beginDoWhile)
    }

    @Throws(TransformerException::class)
    private fun getTagType(bc: BytecodeContext?, tag: Tag?): Type? {
        val tlt: TagLibTag = tag!!.getTagLibTag()
        return try {
            Type.getType(tlt.getTagClassDefinition().getClazz())
            // return tlt.getTagType();
        } catch (t: Throwable) {
            ExceptionUtil.rethrowIfNecessary(t)
            throw TransformerException(bc, t, tag.getStart())
        }
    }
}