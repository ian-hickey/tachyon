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
package lucee.runtime.tag

import java.io.IOException

/**
 * Creates a CFML Custom Tag
 */
class CFTag : BodyTagTryCatchFinallyImpl(), DynamicAttributes, AppendixTag {
    /**
     * Field `attributesScope`
     */
    // new scopes
    protected var attributesScope: StructImpl?
    private var callerScope: Caller?
    private var thistagScope: StructImpl? = null
    private var ctVariablesScope: Variables? = null
    private var hasBody = false
    /**
     * Field `filename`
     */
    // protected String filename;
    /**
     * Field `source`
     */
    protected var source: InitFile? = null
    /**
     * @return Returns the appendix.
     */// filename = appendix+'.'+pageContext.getConfig().getCFMLExtension();
    /**
     * sets the appendix of the class
     *
     * @param appendix
     */
    @get:Override
    @set:Override
    var appendix: String? = null
    private var cfc: Component? = null
    private var isEndTag = false
    @Override
    fun setDynamicAttribute(uri: String?, name: String?, value: Object?) {
        TagUtil.setDynamicAttribute(attributesScope, KeyImpl.init(name), value, TagUtil.ORIGINAL_CASE)
    }

    @Override
    fun setDynamicAttribute(uri: String?, name: Collection.Key?, value: Object?) {
        TagUtil.setDynamicAttribute(attributesScope, name, value, TagUtil.ORIGINAL_CASE)
    }

    @Override
    fun release() {
        super.release()
        hasBody = false
        // filename=null;
        attributesScope = StructImpl() // .clear();
        callerScope = CallerImpl()
        if (thistagScope != null) thistagScope = null
        if (ctVariablesScope != null) ctVariablesScope = null
        isEndTag = false

        // cfc=null;
        source = null
    }

    @Override
    @Throws(PageException::class)
    fun doStartTag(): Int {
        val pci: PageContextImpl? = pageContext as PageContextImpl?
        val old: Boolean = pci.useSpecialMappings(true)
        return try {
            initFile()
            callerScope.initialize(pageContext)
            if (source.isCFC()) cfcStartTag() else cfmlStartTag()
        } finally {
            pci.useSpecialMappings(old)
        }
    }

    @Override
    fun doEndTag(): Int {
        val pci: PageContextImpl? = pageContext as PageContextImpl?
        val old: Boolean = pci.useSpecialMappings(true)
        return try {
            if (source.isCFC()) _doCFCFinally()
            EVAL_PAGE
        } finally {
            pci.useSpecialMappings(old)
        }
    }

    @Override
    fun doInitBody() {
    }

    @Override
    @Throws(PageException::class)
    fun doAfterBody(): Int {
        return if (source.isCFC()) cfcEndTag() else cfmlEndTag()
    }

    @Override
    @Throws(Throwable::class)
    fun doCatch(t: Throwable?) {
        ExceptionUtil.rethrowIfNecessary(t)
        if (source.isCFC()) {
            val source = if (isEndTag) "end" else "body"
            isEndTag = false
            _doCFCCatch(t, source, true)
        } else super.doCatch(t)
    }

    @Throws(PageException::class)
    fun initFile() {
        source = initFile(pageContext)
    }

    @Throws(PageException::class)
    fun initFile(pageContext: PageContext?): InitFile? {
        return CustomTagUtil.loadInitFile(pageContext, appendix)
    }

    @Throws(PageException::class)
    private fun cfmlStartTag(): Int {
        callerScope.initialize(pageContext)

        // thistag
        if (thistagScope == null) thistagScope = StructImpl(Struct.TYPE_LINKED)
        thistagScope.set(GENERATED_CONTENT, "")
        thistagScope.set(EXECUTION_MODE, "start")
        thistagScope.set(EXECUTE_BODY, Boolean.TRUE)
        thistagScope.set(KeyConstants._HASENDTAG, Caster.toBoolean(hasBody))
        ctVariablesScope = VariablesImpl()
        ctVariablesScope.setEL(KeyConstants._ATTRIBUTES, attributesScope)
        ctVariablesScope.setEL(KeyConstants._CALLER, callerScope)
        ctVariablesScope.setEL(KeyConstants._THISTAG, thistagScope)

        // include
        doInclude()
        return if (Caster.toBooleanValue(thistagScope.get(EXECUTE_BODY))) EVAL_BODY_BUFFERED else SKIP_BODY
    }

    @Throws(PageException::class)
    private fun cfmlEndTag(): Int {
        // thistag
        val genConBefore: String = bodyContent.getString()
        thistagScope.set(GENERATED_CONTENT, genConBefore)
        thistagScope.set(EXECUTION_MODE, "end")
        thistagScope.set(EXECUTE_BODY, Boolean.FALSE)
        writeEL(bodyContent, MARKER)

        // include
        try {
            doInclude()
        } catch (t: Throwable) {
            ExceptionUtil.rethrowIfNecessary(t)
            writeOut(genConBefore)
            throw Caster.toPageException(t)
        }
        writeOut(genConBefore)
        return if (Caster.toBooleanValue(thistagScope.get(EXECUTE_BODY))) EVAL_BODY_BUFFERED else SKIP_BODY
    }

    @Throws(PageException::class)
    private fun writeOut(genConBefore: String?) {
        var output: String? = bodyContent.getString()
        bodyContent.clearBody()
        val genConAfter: String = Caster.toString(thistagScope.get(GENERATED_CONTENT))
        if (genConBefore !== genConAfter) {
            if (output.startsWith(genConBefore + MARKER)) {
                output = output.substring((genConBefore + MARKER).length())
            }
            output = genConAfter + output
        } else {
            if (output.startsWith(genConBefore + MARKER)) {
                output = output.substring((genConBefore + MARKER).length())
                output = genConBefore + output
            }
        }
        writeEL(bodyContent.getEnclosingWriter(), output)
    }

    @Throws(PageException::class)
    private fun writeEL(writer: JspWriter?, str: String?) {
        try {
            writer.write(str)
        } catch (e: IOException) {
            throw Caster.toPageException(e)
        }
    }

    @Throws(PageException::class)
    fun doInclude() {
        val `var`: Variables = pageContext.variablesScope()
        if (ctVariablesScope !== `var`) pageContext.setVariablesScope(ctVariablesScope)
        var cs: QueryStack? = null
        val undefined: Undefined = pageContext.undefinedScope()
        val oldMode: Int = undefined.setMode(Undefined.MODE_NO_LOCAL_AND_ARGUMENTS)
        if (oldMode != Undefined.MODE_NO_LOCAL_AND_ARGUMENTS) callerScope.setScope(`var`, pageContext.localScope(), pageContext.argumentsScope(), true) else callerScope.setScope(`var`, null, null, false)
        if (pageContext.getConfig().allowImplicidQueryCall()) {
            cs = undefined.getQueryStack()
            undefined.setQueryStack(QueryStackImpl())
        }
        try {
            pageContext.doInclude(arrayOf<PageSource?>(source.getPageSource()), false)
        } catch (t: Throwable) {
            ExceptionUtil.rethrowIfNecessary(t)
            throw Caster.toPageException(t)
        } finally {
            undefined.setMode(oldMode)
            // varScopeData=variablesScope.getMap();
            if (ctVariablesScope !== `var`) pageContext.setVariablesScope(`var`)
            if (pageContext.getConfig().allowImplicidQueryCall()) {
                undefined.setQueryStack(cs)
            }
        }
    }

    // CFC
    @Throws(PageException::class)
    private fun cfcStartTag(): Int {
        callerScope.initialize(pageContext)
        try {
            cfc = ComponentLoader.loadComponent(pageContext, source.getPageSource(), ResourceUtil.removeExtension(source.getFilename(), source.getFilename()), false, true)
        } catch (e: PageException) {
            var m: Mapping? = source.getPageSource().getMapping()
            val c: ConfigWebPro = pageContext.getConfig() as ConfigWebPro
            m = if (m === c.getDefaultTagMapping()) c.getDefaultServerTagMapping() else null
            // is te page source from a tag mapping, so perhaps it was moved from server to web context
            cfc = if (m != null) {
                val ps: PageSource = m.getPageSource(source.getFilename())
                try {
                    ComponentLoader.loadComponent(pageContext, ps, ResourceUtil.removeExtension(source.getFilename(), source.getFilename()), false, true)
                } catch (e1: PageException) {
                    throw e
                }
            } else throw e
        }
        validateAttributes(pageContext, cfc, attributesScope, StringUtil.ucFirst(ListUtil.last(source.getPageSource().getComponentName(), '.')))
        var exeBody = false
        try {
            var rtn: Object = Boolean.TRUE
            if (cfc.contains(pageContext, KeyConstants._init)) {
                var parent: Tag = getParent()
                while (parent != null && !(parent is CFTag && (parent as CFTag).isCFCBasedCustomTag)) {
                    parent = parent.getParent()
                }
                val args: Struct = StructImpl(Struct.TYPE_LINKED)
                args.set(KeyConstants._HASENDTAG, Caster.toBoolean(hasBody))
                if (parent is CFTag) {
                    args.set(PARENT, (parent as CFTag).component)
                }
                rtn = cfc.callWithNamedValues(pageContext, KeyConstants._init, args)
            }
            if (cfc.contains(pageContext, ON_START_TAG)) {
                val args: Struct = StructImpl()
                args.set(KeyConstants._ATTRIBUTES, attributesScope)
                setCaller(pageContext, args)
                rtn = cfc.callWithNamedValues(pageContext, ON_START_TAG, args)
            }
            exeBody = Caster.toBooleanValue(rtn, true)
        } catch (t: Throwable) {
            ExceptionUtil.rethrowIfNecessary(t)
            _doCFCCatch(t, "start", true)
        }
        return if (exeBody) EVAL_BODY_BUFFERED else SKIP_BODY
    }

    @Throws(PageException::class)
    private fun setCaller(pageContext: PageContext?, args: Struct?) {
        callerScope.initialize(pageContext)
        val checkAgs: Boolean = pageContext.undefinedScope().getCheckArguments()
        if (checkAgs) callerScope.setScope(pageContext.variablesScope(), pageContext.localScope(), pageContext.argumentsScope(), true) else callerScope.setScope(pageContext.variablesScope(), null, null, false)
        args.set(KeyConstants._CALLER, callerScope)

        // args.set(KeyConstants._CALLER, Duplicator.duplicate(pageContext.undefinedScope(),false));
    }

    @Throws(PageException::class)
    private fun cfcEndTag(): Int {
        var exeAgain = false
        try {
            var output: String? = null
            var rtn: Object = Boolean.FALSE
            if (cfc.contains(pageContext, ON_END_TAG)) {
                try {
                    output = bodyContent.getString()
                    bodyContent.clearBody()
                    // rtn=cfc.call(pageContext, ON_END_TAG, new
                    // Object[]{attributesScope,pageContext.variablesScope(),output});
                    val args: Struct = StructImpl(Struct.TYPE_LINKED)
                    args.set(KeyConstants._ATTRIBUTES, attributesScope)
                    setCaller(pageContext, args)
                    args.set(GENERATED_CONTENT, output)
                    rtn = cfc.callWithNamedValues(pageContext, ON_END_TAG, args)
                } finally {
                    writeEnclosingWriter()
                }
            } else writeEnclosingWriter()
            exeAgain = Caster.toBooleanValue(rtn, false)
        } catch (t: Throwable) {
            ExceptionUtil.rethrowIfNecessary(t)
            isEndTag = true
            throw Caster.toPageException(t)
        }
        return if (exeAgain) EVAL_BODY_BUFFERED else SKIP_BODY
    }

    @Throws(PageException::class)
    fun _doCFCCatch(t: Throwable?, source: String?, throwIfCFCNotExists: Boolean) {
        var t = t
        writeEnclosingWriter()

        // remove PageServletException wrap
        if (t is PageServletException) {
            val pse: PageServletException? = t as PageServletException?
            t = pse.getPageException()
        }

        // abort
        try {
            if (lucee.runtime.exp.Abort.isAbort(t)) {
                if (bodyContent != null) {
                    bodyContent.writeOut(bodyContent.getEnclosingWriter())
                    bodyContent.clearBuffer()
                }
                _doCFCFinally()
                throw Caster.toPageException(t)
            }
        } catch (ioe: IOException) {
            throw Caster.toPageException(ioe)
        }
        if (throwIfCFCNotExists && cfc == null) throw Caster.toPageException(t)
        try {
            if (cfc != null && cfc.contains(pageContext, ON_ERROR)) {
                val pe: PageException = Caster.toPageException(t)
                // Object rtn=cfc.call(pageContext, ON_ERROR, new Object[]{pe.getCatchBlock(pageContext),source});
                val args: Struct = StructImpl(Struct.TYPE_LINKED)
                args.set(CFCATCH, pe.getCatchBlock(ThreadLocalPageContext.getConfig(pageContext)))
                args.set(SOURCE, source)
                val rtn: Object = cfc.callWithNamedValues(pageContext, ON_ERROR, args)
                if (Caster.toBooleanValue(rtn, false)) throw t!!
            } else throw t!!
        } catch (th: Throwable) {
            ExceptionUtil.rethrowIfNecessary(th)
            writeEnclosingWriter()
            _doCFCFinally()
            throw Caster.toPageException(th)
        }
        writeEnclosingWriter()
    }

    private fun _doCFCFinally() {
        if (cfc != null && cfc.contains(pageContext, ON_FINALLY)) {
            try {
                cfc.call(pageContext, ON_FINALLY, ArrayUtil.OBJECT_EMPTY)
            } catch (pe: PageException) {
                throw PageRuntimeException(pe)
            } finally {
                // writeEnclosingWriter();
            }
        }
    }

    private fun writeEnclosingWriter() {
        if (bodyContent != null) {
            try {
                val output: String = bodyContent.getString()
                bodyContent.clearBody()
                bodyContent.getEnclosingWriter().write(output)
            } catch (e: IOException) {
                // throw Caster.toPageException(e);
            }
        }
    }

    /**
     * sets if tag has a body or not
     *
     * @param hasBody
     */
    fun hasBody(hasBody: Boolean) {
        this.hasBody = hasBody
    }

    /**
     * @return return thistag
     */
    val `this`: Struct?
        get() = if (isCFCBasedCustomTag) {
            cfc
        } else thistagScope

    /**
     * @return return thistag
     */
    fun getCallerScope(): Struct? {
        return callerScope
    }

    /**
     * @return return thistag
     */
    fun getAttributesScope(): Struct? {
        return attributesScope
    }

    /**
     * @return the ctVariablesScope
     */
    val variablesScope: Struct?
        get() = if (isCFCBasedCustomTag) {
            cfc.getComponentScope()
        } else ctVariablesScope

    /**
     * @return the cfc
     */
    val component: Component?
        get() = cfc
    val isCFCBasedCustomTag: Boolean
        get() = getSource().isCFC()

    private fun getSource(): InitFile? {
        if (source == null) {
            try {
                source = initFile(pageContext)
            } catch (e: PageException) {
                LogUtil.log(pageContext, CFTag::class.java.getName(), e)
            }
        }
        return source
    } /*
	 * class InitFile { PageSource ps; String filename; boolean isCFC;
	 * 
	 * public InitFile(PageSource ps,String filename,boolean isCFC){ this.ps=ps; this.filename=filename;
	 * this.isCFC=isCFC; } }
	 */

    companion object {
        private val GENERATED_CONTENT: Collection.Key? = KeyConstants._GENERATEDCONTENT
        private val EXECUTION_MODE: Collection.Key? = KeyConstants._EXECUTIONMODE
        private val EXECUTE_BODY: Collection.Key? = KeyConstants._EXECUTEBODY
        private val PARENT: Collection.Key? = KeyConstants._PARENT
        private val CFCATCH: Collection.Key? = KeyConstants._CFCATCH
        private val SOURCE: Collection.Key? = KeyConstants._SOURCE
        private val ON_ERROR: Collection.Key? = KeyConstants._onError
        private val ON_FINALLY: Collection.Key? = KeyConstants._onFinally
        private val ON_START_TAG: Collection.Key? = KeyConstants._onStartTag
        private val ON_END_TAG: Collection.Key? = KeyConstants._onEndTag
        private val ATTRIBUTE_TYPE: Collection.Key? = KeyImpl.getInstance("attributetype")
        private val SCRIPT: Collection.Key? = KeyConstants._script
        private val RT_EXPR_VALUE: Collection.Key? = KeyImpl.getInstance("rtexprvalue")
        private val MARKER: String? = "2w12801"
        @Throws(ApplicationException::class, ExpressionException::class)
        private fun validateAttributes(pc: PageContext?, cfc: Component?, attributesScope: StructImpl?, tagName: String?) {
            val tag: TagLibTag = getAttributeRequirments(cfc, false) ?: return
            if (tag.getAttributeType() === TagLibTag.ATTRIBUTE_TYPE_FIXED || tag.getAttributeType() === TagLibTag.ATTRIBUTE_TYPE_MIXED) {
                val it: Iterator<Entry<String?, TagLibTagAttr?>?> = tag.getAttributes().entrySet().iterator()
                var count = 0
                var key: Collection.Key
                var attr: TagLibTagAttr
                var value: Object
                var entry: Entry<String?, TagLibTagAttr?>?
                // check existing attributes
                while (it.hasNext()) {
                    entry = it.next()
                    count++
                    key = KeyImpl.toKey(entry.getKey(), null)
                    attr = entry.getValue()
                    value = attributesScope.get(pc, key, null)

                    // check alias
                    if (value == null) {
                        val alias: Array<String?> = attr.getAlias()
                        if (!ArrayUtil.isEmpty(alias)) for (i in alias.indices) {
                            value = attributesScope.get(pc, KeyImpl.toKey(alias[i], null), null)
                            if (value != null) break
                        }
                    }
                    if (value == null) {
                        if (attr.getDefaultValue() != null) {
                            value = attr.getDefaultValue()
                            attributesScope.setEL(key, value)
                        } else if (attr.isRequired()) throw ApplicationException("attribute [" + key.getString().toString() + "] is required for tag [" + tagName.toString() + "]")
                    }
                    if (value != null) {
                        if (!Decision.isCastableTo(attr.getType(), value, true, true, -1)) throw CasterException(createMessage(attr.getType(), value))
                    }
                }

                // check if there are attributes not supported
                if (tag.getAttributeType() === TagLibTag.ATTRIBUTE_TYPE_FIXED && count < attributesScope.size()) {
                    val keys: Array<Collection.Key?> = attributesScope.keys()
                    for (i in keys.indices) {
                        if (tag.getAttribute(keys[i].getLowerString(), true) == null) throw ApplicationException("attribute [" + keys[i].getString().toString() + "] is not supported for tag [" + tagName.toString() + "]")
                    }

                    // Attribute susi is not allowed for tag cfmail
                }
            }
        }

        private fun createMessage(type: String?, value: Object?): String? {
            return if (value is String) "can't cast String [" + CasterException.crop(value).toString() + "] to a value of type [" + type.toString() + "]" else if (value != null) "can't cast Object type [" + Type.getName(value).toString() + "] to a value of type [" + type.toString() + "]" else "can't cast Null value to value of type [$type]"
        }

        private fun getAttributeRequirments(cfc: Component?, runtime: Boolean): TagLibTag? {
            var meta: Struct? = null
            val mem: Member? = if (cfc != null) cfc.getMember(Component.ACCESS_PRIVATE, KeyConstants._metadata, true, false) else null
            if (mem != null) meta = Caster.toStruct(mem.getValue(), null, false)
            if (meta == null) return null
            val tag = TagLibTag(null)
            // TAG

            // type
            val type: String = Caster.toString(meta.get(ATTRIBUTE_TYPE, "dynamic"), "dynamic")

            // script
            var script: String? = Caster.toString(meta.get(SCRIPT, null), null)
            if (!StringUtil.isEmpty(script, true)) {
                script = script.trim()
                var tlts: TagLibTagScript? = TagLibTagScript(tag)
                if ("multiple".equalsIgnoreCase(script) || Caster.toBooleanValue(script, false)) {
                    tlts = TagLibTagScript(tag)
                    tlts.setType(TagLibTagScript.TYPE_MULTIPLE)
                } else if ("single".equalsIgnoreCase(script)) {
                    tlts = TagLibTagScript(tag)
                    tlts.setType(TagLibTagScript.TYPE_SINGLE)
                }
                if (tlts != null) tag.setScript(tlts)
            }
            if ("fixed".equalsIgnoreCase(type)) tag.setAttributeType(TagLibTag.ATTRIBUTE_TYPE_FIXED) else tag.setAttributeType(TagLibTag.ATTRIBUTE_TYPE_DYNAMIC)
            if (!runtime) {
                // hint
                val hint: String = Caster.toString(meta.get(KeyConstants._hint, null), null)
                if (!StringUtil.isEmpty(hint)) tag.setDescription(hint)
            }

            // ATTRIBUTES
            val attributes: Struct = Caster.toStruct(meta.get(KeyConstants._ATTRIBUTES, null), null, false)
            if (attributes != null) {
                val it: Iterator<Entry<Key?, Object?>?> = attributes.entryIterator()
                // Iterator it = attributes.entrySet().iterator();
                var entry: Entry<Key?, Object?>?
                var attr: TagLibTagAttr?
                var sct: Struct
                var name: String
                var defaultValue: Object
                while (it.hasNext()) {
                    entry = it.next()
                    name = Caster.toString(entry.getKey(), null)
                    if (StringUtil.isEmpty(name)) continue
                    attr = TagLibTagAttr(tag)
                    attr.setName(name)
                    sct = Caster.toStruct(entry.getValue(), null, false)
                    if (sct != null) {
                        attr.setRequired(Caster.toBooleanValue(sct.get(KeyConstants._required, Boolean.FALSE), false))
                        attr.setType(Caster.toString(sct.get(KeyConstants._type, "any"), "any"))
                        defaultValue = sct.get(KeyConstants._default, null)
                        if (defaultValue != null) attr.setDefaultValue(defaultValue)
                        if (!runtime) {
                            attr.setDescription(Caster.toString(sct.get(KeyConstants._hint, null), null))
                            attr.setRtexpr(Caster.toBooleanValue(sct.get(RT_EXPR_VALUE, Boolean.TRUE), true))
                        }
                    }
                    tag.setAttribute(attr)
                }
            }
            return tag
        }
    }

    /**
     * constructor for the tag class
     */
    init {
        attributesScope = StructImpl()
        callerScope = CallerImpl()
        // thistagScope = new StructImpl();
    }
}