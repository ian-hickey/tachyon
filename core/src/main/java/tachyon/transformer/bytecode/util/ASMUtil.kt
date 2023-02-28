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
package tachyon.transformer.bytecode.util

import java.io.IOException

object ASMUtil {
    const val TYPE_ALL: Short = 0
    const val TYPE_BOOLEAN: Short = 1
    const val TYPE_NUMERIC: Short = 2
    const val TYPE_STRING: Short = 4
    const val INTERFACE = 1
    const val VIRTUAL = 2
    const val STATIC = 3
    private val CONSTRUCTOR_OBJECT: Method? = Method.getMethod("void <init> ()")
    private val _SRC_NAME: Method? = Method("_srcName", Types.STRING, arrayOf<Type?>())

    // private static final String VERSION_MESSAGE = "you use an invalid version of the ASM Jar, please
    // update your jar files";
    private var id: Long = 0

    /**
     * Gibt zurueck ob das direkt uebergeordnete Tag mit dem uebergebenen Full-Name (Namespace und Name)
     * existiert.
     *
     * @param el Startelement, von wo aus gesucht werden soll.
     * @param fullName Name des gesuchten Tags.
     * @return Existiert ein solches Tag oder nicht.
     */
    fun hasAncestorTag(stat: Statement?, fullName: String?): Boolean {
        return getAncestorTag(stat, fullName) != null
    }

    /**
     * Gibt das uebergeordnete CFXD Tag Element zurueck, falls dies nicht existiert wird null
     * zurueckgegeben.
     *
     * @param el Element von dem das parent Element zurueckgegeben werden soll.
     * @return uebergeordnete CFXD Tag Element
     */
    fun getParentTag(tag: Tag?): Tag? {
        var p: Statement = tag.getParent() ?: return null
        p = p.getParent()
        return if (p is Tag) p as Tag else null
    }

    fun isParentTag(tag: Tag?, fullName: String?): Boolean {
        val p: Tag = getParentTag(tag) ?: return false
        return p.getFullname().equalsIgnoreCase(fullName)
    }

    fun isParentTag(tag: Tag?, clazz: Class?): Boolean {
        val p: Tag = getParentTag(tag) ?: return false
        return p.getClass() === clazz
    }

    fun hasAncestorRetryFCStatement(stat: Statement?, label: String?): Boolean {
        return getAncestorRetryFCStatement(stat, null, label) != null
    }

    fun hasAncestorBreakFCStatement(stat: Statement?, label: String?): Boolean {
        return getAncestorBreakFCStatement(stat, null, label) != null
    }

    fun hasAncestorContinueFCStatement(stat: Statement?, label: String?): Boolean {
        return getAncestorContinueFCStatement(stat, null, label) != null
    }

    fun getAncestorRetryFCStatement(stat: Statement?, finallyLabels: List<FlowControlFinal?>?, label: String?): FlowControlRetry? {
        return getAncestorFCStatement(stat, finallyLabels, FlowControl.RETRY, label) as FlowControlRetry?
    }

    fun getAncestorBreakFCStatement(stat: Statement?, finallyLabels: List<FlowControlFinal?>?, label: String?): FlowControlBreak? {
        return getAncestorFCStatement(stat, finallyLabels, FlowControl.BREAK, label) as FlowControlBreak?
    }

    fun getAncestorContinueFCStatement(stat: Statement?, finallyLabels: List<FlowControlFinal?>?, label: String?): FlowControlContinue? {
        return getAncestorFCStatement(stat, finallyLabels, FlowControl.CONTINUE, label) as FlowControlContinue?
    }

    private fun getAncestorFCStatement(stat: Statement?, finallyLabels: List<FlowControlFinal?>?, flowType: Int, label: String?): FlowControl? {
        var parent: Statement? = stat
        var fcf: FlowControlFinal
        while (true) {
            parent = parent.getParent()
            if (parent == null) return null
            if ((flowType == FlowControl.RETRY && parent is FlowControlRetry || flowType == FlowControl.CONTINUE && parent is FlowControlContinue
                            || flowType == FlowControl.BREAK && parent is FlowControlBreak) && labelMatch(parent as FlowControl?, label)) {
                if (parent is ScriptBody) {
                    val _finallyLabels: List<FlowControlFinal?>? = if (finallyLabels == null) null else ArrayList<FlowControlFinal?>()
                    val scriptBodyParent: FlowControl? = getAncestorFCStatement(parent, _finallyLabels, flowType, label)
                    if (scriptBodyParent != null) {
                        if (finallyLabels != null) {
                            val it: Iterator<FlowControlFinal?> = _finallyLabels!!.iterator()
                            while (it.hasNext()) {
                                finallyLabels.add(it.next())
                            }
                        }
                        return scriptBodyParent
                    }
                    return parent as FlowControl?
                }
                return parent as FlowControl?
            }

            // only if not last
            if (finallyLabels != null) {
                fcf = parent.getFlowControlFinal()
                if (fcf != null) {
                    finallyLabels.add(fcf)
                }
            }
        }
    }

    private fun labelMatch(fc: FlowControl?, label: String?): Boolean {
        if (StringUtil.isEmpty(label, true)) return true
        val fcl: String = fc.getLabel()
        return if (StringUtil.isEmpty(fcl, true)) false else label.trim().equalsIgnoreCase(fcl.trim())
    }

    @Throws(TransformerException::class)
    fun leadFlow(bc: BytecodeContext?, stat: Statement?, flowType: Int, label: String?) {
        val finallyLabels: List<FlowControlFinal?> = ArrayList<FlowControlFinal?>()
        val fc: FlowControl?
        val name: String
        if (FlowControl.BREAK === flowType) {
            fc = getAncestorBreakFCStatement(stat, finallyLabels, label)
            name = "break"
        } else if (FlowControl.CONTINUE === flowType) {
            fc = getAncestorContinueFCStatement(stat, finallyLabels, label)
            name = "continue"
        } else {
            fc = getAncestorRetryFCStatement(stat, finallyLabels, label)
            name = "retry"
        }
        if (fc == null) throw TransformerException(bc, "$name must be inside a loop (for,while,do-while,<cfloop>,<cfwhile> ...)", stat.getStart())
        val adapter: GeneratorAdapter = bc.getAdapter()
        val end: Label
        end = if (FlowControl.BREAK === flowType) (fc as FlowControlBreak?).getBreakLabel() else if (FlowControl.CONTINUE === flowType) (fc as FlowControlContinue?).getContinueLabel() else (fc as FlowControlRetry?).getRetryLabel()

        // first jump to all final labels
        val arr: Array<FlowControlFinal?> = finallyLabels.toArray(arrayOfNulls<FlowControlFinal?>(finallyLabels.size()))
        if (arr.size > 0) {
            var fcf: FlowControlFinal?
            for (i in arr.indices) {
                fcf = arr[i]

                // first
                if (i == 0) {
                    adapter.visitJumpInsn(Opcodes.GOTO, fcf.getFinalEntryLabel())
                }

                // last
                if (arr.size == i + 1) fcf.setAfterFinalGOTOLabel(end) else fcf.setAfterFinalGOTOLabel(arr[i + 1].getFinalEntryLabel())
            }
        } else bc.getAdapter().visitJumpInsn(Opcodes.GOTO, end)
    }

    fun hasAncestorTryStatement(stat: Statement?): Boolean {
        return getAncestorTryStatement(stat) != null
    }

    fun getAncestorTryStatement(stat: Statement?): Statement? {
        var parent: Statement? = stat
        while (true) {
            parent = parent.getParent()
            if (parent == null) return null
            if (parent is TagTry) {
                return parent
            } else if (parent is TryCatchFinally) {
                return parent
            }
        }
    }

    /**
     * Gibt ein uebergeordnetes Tag mit dem uebergebenen Full-Name (Namespace und Name) zurueck, falls
     * ein solches existiert, andernfalls wird null zurueckgegeben.
     *
     * @param el Startelement, von wo aus gesucht werden soll.
     * @param fullName Name des gesuchten Tags.
     * @return uebergeornetes Element oder null.
     */
    fun getAncestorTag(stat: Statement?, fullName: String?): Tag? {
        var parent: Statement? = stat
        var tag: Tag?
        while (true) {
            parent = parent.getParent()
            if (parent == null) return null
            if (parent is Tag) {
                tag = parent as Tag?
                if (tag.getFullname().equalsIgnoreCase(fullName)) return tag
            }
        }
    }

    fun getAncestorComponent(stat: Statement?): TagComponent? {
        var parent: Statement? = stat
        while (true) {
            parent = parent.getParent()
            if (parent == null) return null
            if (parent is TagComponent) return parent as TagComponent?
        }
    }

    /**
     * extract the content of an attribute
     *
     * @param cfxdTag
     * @param attrName
     * @return attribute value
     * @throws EvaluatorException
     */
    @Throws(EvaluatorException::class)
    fun getAttributeBoolean(tag: Tag?, attrName: String?): Boolean? {
        return getAttributeLiteral(tag, attrName).getBoolean(null)
                ?: throw EvaluatorException("attribute [$attrName] must be a constant boolean value")
    }

    /**
     * extract the content of an attribute
     *
     * @param cfxdTag
     * @param attrName
     * @return attribute value
     * @throws EvaluatorException
     */
    fun getAttributeBoolean(tag: Tag?, attrName: String?, defaultValue: Boolean?): Boolean? {
        val lit: Literal = getAttributeLiteral(tag, attrName, null)
                ?: return defaultValue
        return lit.getBoolean(defaultValue)
    }

    /**
     * extract the content of an attribute
     *
     * @param cfxdTag
     * @param attrName
     * @return attribute value
     * @throws EvaluatorException
     */
    @Throws(EvaluatorException::class)
    fun getAttributeString(tag: Tag?, attrName: String?): String? {
        return getAttributeLiteral(tag, attrName).getString()
    }

    /**
     * extract the content of an attribute
     *
     * @param cfxdTag
     * @param attrName
     * @return attribute value
     * @throws EvaluatorException
     */
    fun getAttributeString(tag: Tag?, attrName: String?, defaultValue: String?): String? {
        val lit: Literal = getAttributeLiteral(tag, attrName, null)
                ?: return defaultValue
        return lit.getString()
    }

    /**
     * extract the content of an attribute
     *
     * @param cfxdTag
     * @param attrName
     * @return attribute value
     * @throws EvaluatorException
     */
    @Throws(EvaluatorException::class)
    fun getAttributeLiteral(tag: Tag?, attrName: String?): Literal? {
        val attr: Attribute = tag.getAttribute(attrName)
        if (attr != null && attr.getValue() is Literal) return attr.getValue() as Literal
        throw EvaluatorException("attribute [$attrName] must be a constant value")
    }

    /**
     * extract the content of an attribute
     *
     * @param cfxdTag
     * @param attrName
     * @return attribute value
     * @throws EvaluatorException
     */
    fun getAttributeLiteral(tag: Tag?, attrName: String?, defaultValue: Literal?): Literal? {
        val attr: Attribute = tag.getAttribute(attrName)
        return if (attr != null && attr.getValue() is Literal) attr.getValue() as Literal else defaultValue
    }

    /**
     * Prueft ob das das angegebene Tag in der gleichen Ebene nach dem angegebenen Tag vorkommt.
     *
     * @param tag Ausgangspunkt, nach diesem tag darf das angegebene nicht vorkommen.
     * @param nameToFind Tag Name der nicht vorkommen darf
     * @return kommt das Tag vor.
     */
    fun hasSisterTagAfter(tag: Tag?, nameToFind: String?): Boolean {
        val body: Body = tag.getParent() as Body
        val stats: List<Statement?> = body.getStatements()
        val it: Iterator<Statement?> = stats.iterator()
        var other: Statement?
        var isAfter = false
        while (it.hasNext()) {
            other = it.next()
            if (other is Tag) {
                if (isAfter) {
                    if ((other as Tag?).getTagLibTag().getName().equals(nameToFind)) return true
                } else if (other === tag) isAfter = true
            }
        }
        return false
    }

    /**
     * Prueft ob das angegebene Tag innerhalb seiner Ebene einmalig ist oder nicht.
     *
     * @param tag Ausgangspunkt, nach diesem tag darf das angegebene nicht vorkommen.
     * @return kommt das Tag vor.
     */
    fun hasSisterTagWithSameName(tag: Tag?): Boolean {
        val body: Body = tag.getParent() as Body
        val stats: List<Statement?> = body.getStatements()
        val it: Iterator<Statement?> = stats.iterator()
        var other: Statement?
        val name: String = tag.getTagLibTag().getName()
        while (it.hasNext()) {
            other = it.next()
            if (other !== tag && other is Tag && (other as Tag?).getTagLibTag().getName().equals(name)) return true
        }
        return false
    }

    /**
     * remove this tag from his parent body
     *
     * @param tag
     */
    fun remove(tag: Tag?) {
        val body: Body = tag.getParent() as Body
        body.getStatements().remove(tag)
    }

    fun move(src: Tag?, dest: Body?) {
        // switch children
        val srcBody: Body = src.getParent() as Body
        val it: Iterator<Statement?> = srcBody.getStatements().iterator()
        var stat: Statement?
        while (it.hasNext()) {
            stat = it.next()
            if (stat === src) {
                it.remove()
                dest.addStatement(stat)
            }
        }

        // switch parent
        src.setParent(dest)
    }

    /**
     * replace src with trg
     *
     * @param src
     * @param trg
     */
    fun replace(src: Tag?, trg: Tag?, moveBody: Boolean) {
        trg.setParent(src.getParent())
        val p: Body = src.getParent() as Body
        val stats: List<Statement?> = p.getStatements()
        val it: Iterator<Statement?> = stats.iterator()
        var stat: Statement?
        var count = 0
        while (it.hasNext()) {
            stat = it.next()
            if (stat === src) {
                if (moveBody && src.getBody() != null) src.getBody().setParent(trg)
                stats.set(count, trg)
                break
            }
            count++
        }
    }

    @Throws(TransformerException::class)
    fun getAncestorPage(bc: BytecodeContext?, stat: Statement?): Page? {
        var parent: Statement? = stat
        while (true) {
            parent = parent.getParent()
            if (parent == null) {
                throw TransformerException(bc, "missing parent Statement of Statement", stat.getStart())
                // return null;
            }
            if (parent is Page) return parent as Page?
        }
    }

    fun getAncestorPage(stat: Statement?, defaultValue: Page?): Page? {
        var parent: Statement? = stat
        while (true) {
            parent = parent.getParent()
            if (parent == null) {
                return defaultValue
            }
            if (parent is Page) return parent as Page?
        }
    }

    fun invokeMethod(adapter: GeneratorAdapter?, type: Type?, method: Method?) {
        if (type.getClass().isInterface()) adapter.invokeInterface(type, method) else adapter.invokeVirtual(type, method)
    }

    @Throws(PageException::class)
    fun createPojo(className: String?, properties: Array<ASMProperty?>?, parent: Class?, interfaces: Array<Class?>?, srcName: String?): ByteArray? {
        var className = className
        className = className.replace('.', '/')
        className = className.replace('\\', '/')
        className = ListUtil.trim(className, "/")
        var inter: Array<String?>? = null
        if (interfaces != null) {
            inter = arrayOfNulls<String?>(interfaces.size)
            for (i in inter.indices) {
                inter[i] = interfaces[i].getName().replace('.', '/')
            }
        }
        // CREATE CLASS
        val cw: ClassWriter? = getClassWriter()
        cw.visit(Opcodes.V1_6, Opcodes.ACC_PUBLIC, className, null, parent.getName().replace('.', '/'), inter)
        val md5: String?
        md5 = try {
            createMD5(properties)
        } catch (t: Throwable) {
            ExceptionUtil.rethrowIfNecessary(t)
            ""
        }
        val fv: FieldVisitor = cw.visitField(Opcodes.ACC_PUBLIC + Opcodes.ACC_FINAL + Opcodes.ACC_STATIC, "_md5_", "Ljava/lang/String;", null, md5)
        fv.visitEnd()

        // Constructor
        val adapter = GeneratorAdapter(Opcodes.ACC_PUBLIC, CONSTRUCTOR_OBJECT, null, null, cw)
        adapter.loadThis()
        adapter.invokeConstructor(toType(parent, true), CONSTRUCTOR_OBJECT)
        adapter.returnValue()
        adapter.endMethod()

        // properties
        for (i in properties.indices) {
            createProperty(cw, className, properties!![i])
        }

        // complexType src
        if (!StringUtil.isEmpty(srcName)) {
            val _adapter = GeneratorAdapter(Opcodes.ACC_PUBLIC + Opcodes.ACC_FINAL + Opcodes.ACC_STATIC, _SRC_NAME, null, null, cw)
            _adapter.push(srcName)
            _adapter.returnValue()
            _adapter.endMethod()
        }
        cw.visitEnd()
        return cw.toByteArray()
    }

    @Throws(PageException::class)
    private fun createProperty(cw: ClassWriter?, classType: String?, property: ASMProperty?) {
        val name: String = property!!.getName()
        val type: Type = property!!.getASMType()
        val clazz: Class = property!!.getClazz()
        cw.visitField(Opcodes.ACC_PRIVATE, name, type.toString(), null, null).visitEnd()
        val load = loadFor(type)
        // int sizeOf=sizeOf(type);

        // get<PropertyName>():<type>
        var types: Array<Type?>? = arrayOfNulls<Type?>(0)
        var method: Method? = Method((if (clazz === Boolean::class.javaPrimitiveType) "get" else "get") + StringUtil.ucFirst(name), type, types)
        var adapter: GeneratorAdapter? = GeneratorAdapter(Opcodes.ACC_PUBLIC, method, null, null, cw)
        var start: Label? = Label()
        adapter.visitLabel(start)
        adapter.visitVarInsn(Opcodes.ALOAD, 0)
        adapter.visitFieldInsn(Opcodes.GETFIELD, classType, name, type.toString())
        adapter.returnValue()
        var end: Label? = Label()
        adapter.visitLabel(end)
        adapter.visitLocalVariable("this", "L$classType;", null, start, end, 0)
        adapter.visitEnd()
        adapter.endMethod()

        // set<PropertyName>(object):void
        types = arrayOf<Type?>(type)
        method = Method("set" + StringUtil.ucFirst(name), Types.VOID, types)
        adapter = GeneratorAdapter(Opcodes.ACC_PUBLIC, method, null, null, cw)
        start = Label()
        adapter.visitLabel(start)
        adapter.visitVarInsn(Opcodes.ALOAD, 0)
        adapter.visitVarInsn(load, 1)
        adapter.visitFieldInsn(Opcodes.PUTFIELD, classType, name, type.toString())
        adapter.visitInsn(Opcodes.RETURN)
        end = Label()
        adapter.visitLabel(end)
        adapter.visitLocalVariable("this", "L$classType;", null, start, end, 0)
        adapter.visitLocalVariable(name, type.toString(), null, start, end, 1)
        // adapter.visitMaxs(0, 0);//.visitMaxs(sizeOf+1, sizeOf+1);// hansx
        adapter.visitEnd()
        adapter.endMethod()
    }

    fun loadFor(type: Type?): Int {
        if (type.equals(Types.BOOLEAN_VALUE) || type.equals(Types.INT_VALUE) || type.equals(Types.CHAR) || type.equals(Types.SHORT_VALUE)) return Opcodes.ILOAD
        if (type.equals(Types.FLOAT_VALUE)) return Opcodes.FLOAD
        if (type.equals(Types.LONG_VALUE)) return Opcodes.LLOAD
        return if (type.equals(Types.DOUBLE_VALUE)) Opcodes.DLOAD else Opcodes.ALOAD
    }

    fun sizeOf(type: Type?): Int {
        return if (type.equals(Types.LONG_VALUE) || type.equals(Types.DOUBLE_VALUE)) 2 else 1
    }

    /**
     * translate a string cfml type definition to a Type Object
     *
     * @param cfType
     * @param axistype
     * @return
     * @throws PageException
     */
    @Throws(PageException::class)
    fun toType(cfType: String?, axistype: Boolean): Type? {
        return toType(Caster.cfTypeToClass(cfType), axistype)
    }

    /**
     * translate a string cfml type definition to a Type Object
     *
     * @param cfType
     * @param axistype
     * @return
     * @throws PageException
     */
    @Throws(PageException::class)
    fun toType(type: Class?, axistype: Boolean): Type? {
        var type: Class? = type
        if (axistype) type = (ThreadLocalPageContext.getConfig() as ConfigWebPro).getWSHandler().toWSTypeClass(type)
        return Type.getType(type)
    }

    fun createMD5(props: Array<ASMProperty?>?): String? {
        val sb = StringBuffer()
        for (i in props.indices) {
            sb.append("name:" + props!![i]!!.getName().toString() + ";")
            if (props[i] is Property) {
                sb.append("type:" + (props[i] as Property?).getType().toString() + ";")
            } else {
                try {
                    sb.append("type:" + props[i]!!.getASMType().toString() + ";")
                } catch (e: PageException) {
                }
            }
        }
        return try {
            MD5.getDigestAsString(sb.toString())
        } catch (e: IOException) {
            ""
        }
    }

    fun removeLiterlChildren(tag: Tag?, recursive: Boolean) {
        val body: Body = tag.getBody()
        if (body != null) {
            val list: List<Statement?> = body.getStatements()
            val stats: Array<Statement?> = list.toArray(arrayOfNulls<Statement?>(list.size()))
            var po: PrintOut?
            var t: Tag?
            for (i in stats.indices) {
                if (stats[i] is PrintOut) {
                    po = stats[i] as PrintOut?
                    if (po.getExpr() is Literal) {
                        body.getStatements().remove(po)
                    }
                } else if (recursive && stats[i] is Tag) {
                    t = stats[i] as Tag?
                    if (t.getTagLibTag().isAllowRemovingLiteral()) {
                        removeLiterlChildren(t, recursive)
                    }
                }
            }
        }
    }

    @Synchronized
    fun getId(): String? {
        if (id < 0) id = 0
        return StringUtil.addZeros(++id, 6)
    }

    fun isEmpty(body: Body?): Boolean {
        return body == null || body.isEmpty()
    }

    /**
     * @param adapter
     * @param expr
     * @param mode
     */
    fun pop(adapter: GeneratorAdapter?, expr: Expression?, mode: Int) {
        if (mode == Expression.MODE_VALUE && expr is ExprNumber) {
            adapter.pop2()
        } else adapter.pop()
    }

    fun pop(adapter: GeneratorAdapter?, type: Type?) {
        if (type.equals(Types.DOUBLE_VALUE)) adapter.pop2() else if (type.equals(Types.VOID)) {
        } else adapter.pop()
    }

    fun getClassWriter(): ClassWriter? {
        return ClassWriter(ClassWriter.COMPUTE_MAXS) // |ClassWriter.COMPUTE_FRAMES);
    }

    fun createOverfowMethod(prefix: String?, id: Int): String? { // pattern is used in function callstackget
        var prefix = prefix
        if (StringUtil.isEmpty(prefix)) prefix = "call"
        return prefix.toString() + "_" + StringUtil.addZeros(id, 6)
    }

    fun isOverfowMethod(name: String?): Boolean {
        return name!!.length() > 6 && Decision.isNumber(name.substring(name.length() - 6, name.length()))
        // return name.startsWith("_call") && name.length()>=11;
    }

    fun isDotKey(expr: ExprString?): Boolean {
        return expr is LitString && !(expr as LitString?).fromBracket()
    }

    fun toString(bc: BytecodeContext?, exp: Expression?, defaultValue: String?): String? {
        return try {
            toString(bc, exp)
        } catch (e: TransformerException) {
            defaultValue
        }
    }

    @Throws(TransformerException::class)
    fun toString(bc: BytecodeContext?, exp: Expression?): String? {
        if (exp is Variable) {
            return toString(bc, VariableString.toExprString(exp))
        } else if (exp is VariableString) {
            return (exp as VariableString?).castToString(bc)
        } else if (exp is Literal) {
            return (exp as Literal?).toString()
        }
        return null
    }

    @Throws(TransformerException::class)
    fun toBoolean(bc: BytecodeContext?, attr: Attribute?, start: Position?): Boolean? {
        if (attr == null) throw TransformerException(bc, "attribute does not exist", start)
        if (attr.getValue() is Literal) {
            val b: Boolean = (attr.getValue() as Literal).getBoolean(null)
            if (b != null) return b
        }
        throw TransformerException(bc, "attribute [" + attr.getName().toString() + "] must be a constant boolean value", start)
    }

    fun toBoolean(attr: Attribute?, line: Int, defaultValue: Boolean?): Boolean? {
        if (attr == null) return defaultValue
        if (attr.getValue() is Literal) {
            val b: Boolean = (attr.getValue() as Literal).getBoolean(null)
            if (b != null) return b
        }
        return defaultValue
    }

    fun isCFC(s: Statement?): Boolean {
        var s: Statement? = s
        var p: Statement?
        while (s.getParent().also { p = it } != null) {
            s = p
        }
        return true
    }

    @Throws(EvaluatorException::class)
    fun isLiteralAttribute(tag: Tag?, attrName: String?, type: Short, required: Boolean, throwWhenNot: Boolean): Boolean {
        return isLiteralAttribute(tag, tag.getAttribute(attrName), type, required, throwWhenNot)
    }

    @Throws(EvaluatorException::class)
    fun isLiteralAttribute(tag: Tag?, attr: Attribute?, type: Short, required: Boolean, throwWhenNot: Boolean): Boolean {
        var strType = "/constant"
        if (attr != null && !isNull(attr.getValue())) {
            when (type) {
                TYPE_ALL -> if (attr.getValue() is Literal) return true
                TYPE_BOOLEAN -> {
                    if (tag.getFactory().toExprBoolean(attr.getValue()) is LitBoolean) return true
                    strType = " boolean"
                }
                TYPE_NUMERIC -> {
                    if (tag.getFactory().toExprNumber(attr.getValue()) is LitNumber) return true
                    strType = " numeric"
                }
                TYPE_STRING -> {
                    if (tag.getFactory().toExprString(attr.getValue()) is LitString) return true
                    strType = " string"
                }
            }
            if (!throwWhenNot) return false
            throw EvaluatorException("Attribute [" + attr.getName().toString() + "] of the Tag [" + tag.getFullname().toString() + "] must be a literal" + strType + " value. " + "attributes java class type " + attr.getValue().getClass().getName())
        }
        if (required) {
            if (!throwWhenNot) return false
            throw EvaluatorException("Attribute [" + attr.getName().toString() + "] of the Tag [" + tag.getFullname().toString() + "] is required")
        }
        return false
    }

    fun isNull(expr: Expression?): Boolean {
        return if (expr is Cast) {
            isNull((expr as Cast?).getExpr())
        } else expr.getFactory().isNull(expr)
    }

    fun isRefType(type: Type?): Boolean {
        return !(type === Types.BYTE_VALUE || type === Types.BOOLEAN_VALUE || type === Types.CHAR || type === Types.DOUBLE_VALUE || type === Types.FLOAT_VALUE || type === Types.INT_VALUE || type === Types.LONG_VALUE || type === Types.SHORT_VALUE)
    }

    fun toRefType(type: Type?): Type? {
        if (type === Types.BYTE_VALUE) return Types.BYTE
        if (type === Types.BOOLEAN_VALUE) return Types.BOOLEAN
        if (type === Types.CHAR) return Types.CHARACTER
        if (type === Types.DOUBLE_VALUE) return Types.DOUBLE
        if (type === Types.FLOAT_VALUE) return Types.FLOAT
        if (type === Types.INT_VALUE) return Types.INTEGER
        if (type === Types.LONG_VALUE) return Types.LONG
        return if (type === Types.SHORT_VALUE) Types.SHORT else type
    }

    /**
     * return value type only when there is one
     *
     * @param type
     * @return
     */
    fun toValueType(type: Type?): Type? {
        if (type === Types.BYTE) return Types.BYTE_VALUE
        if (type === Types.BOOLEAN) return Types.BOOLEAN_VALUE
        if (type === Types.CHARACTER) return Types.CHAR
        if (type === Types.DOUBLE) return Types.DOUBLE_VALUE
        if (type === Types.FLOAT) return Types.FLOAT_VALUE
        if (type === Types.INTEGER) return Types.INT_VALUE
        if (type === Types.LONG) return Types.LONG_VALUE
        return if (type === Types.SHORT) Types.SHORT_VALUE else type
    }

    fun getValueTypeClass(type: Type?, defaultValue: Class?): Class? {
        if (type === Types.BYTE_VALUE) return Byte::class.javaPrimitiveType
        if (type === Types.BOOLEAN_VALUE) return Boolean::class.javaPrimitiveType
        if (type === Types.CHAR) return Char::class.javaPrimitiveType
        if (type === Types.DOUBLE_VALUE) return Double::class.javaPrimitiveType
        if (type === Types.FLOAT_VALUE) return Float::class.javaPrimitiveType
        if (type === Types.INT_VALUE) return Int::class.javaPrimitiveType
        if (type === Types.LONG_VALUE) return Long::class.javaPrimitiveType
        return if (type === Types.SHORT_VALUE) Short::class.javaPrimitiveType else defaultValue
    }

    fun toASMProperties(properties: Array<Property?>?): Array<ASMProperty?>? {
        val asmp: Array<ASMProperty?> = arrayOfNulls<ASMProperty?>(properties!!.size)
        for (i in asmp.indices) {
            asmp[i] = properties!![i] as ASMProperty?
        }
        return asmp
    }

    fun containsComponent(body: Body?): Boolean {
        if (body == null) return false
        val it: Iterator<Statement?> = body.getStatements().iterator()
        while (it.hasNext()) {
            if (it.next() is TagComponent) return true
        }
        return false
    }

    fun dummy1(bc: BytecodeContext?) {
        bc.getAdapter().visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/System", "currentTimeMillis", "()J")
        bc.getAdapter().visitInsn(Opcodes.POP2)
    }

    fun dummy2(bc: BytecodeContext?) {
        bc.getAdapter().visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/System", "nanoTime", "()J")
        bc.getAdapter().visitInsn(Opcodes.POP2)
    }

    /**
     * convert a clas array to a type array
     *
     * @param classes
     * @return
     */
    fun toTypes(classes: Array<Class<*>?>?): Array<Type?>? {
        if (classes == null || classes.size == 0) return arrayOfNulls<Type?>(0)
        val types: Array<Type?> = arrayOfNulls<Type?>(classes.size)
        for (i in classes.indices) {
            types[i] = Type.getType(classes[i])
        }
        return types
    }

    fun display(name: ExprString?): String? {
        return if (name is Literal) {
            if (name is Identifier) (name as Identifier?).getRaw() else (name as Literal?).getString()
        } else name.toString()
    }

    fun cachedWithinValue(`val`: Expression?, dv: Literal?): Literal? {
        return try {
            cachedWithinValue(`val`)
        } catch (e: Exception) {
            dv
        }
    }

    @Throws(EvaluatorException::class)
    fun cachedWithinValue(`val`: Expression?): Literal? {
        if (`val` is Literal) {
            val l: Literal? = `val` as Literal?

            // double == days
            val n: Number = l.getNumber(null)
            return if (n != null) {
                `val`.getFactory().createLitLong(TimeSpanImpl.fromDays(n.doubleValue()).getMillis(), null, null)
            } else l
        } else if (`val` is Variable) {
            val `var`: Variable? = `val` as Variable?
            if (`var`.getMembers().size() === 1) {
                val first: Member = `var`.getFirstMember()
                if (first is BIF) {
                    val bif: BIF = first as BIF
                    if ("createTimeSpan".equalsIgnoreCase(bif.getFlf().getName())) {
                        val args: Array<Argument?> = bif.getArguments()
                        val len: Int = ArrayUtil.size(args)
                        if (len >= 4 && len <= 5) {
                            val days = toDouble(args[0].getValue())
                            val hours = toDouble(args[1].getValue())
                            val minutes = toDouble(args[2].getValue())
                            val seconds = toDouble(args[3].getValue())
                            val millis: Double = if (len == 5) toDouble(args[4].getValue()) else 0
                            return `val`.getFactory().createLitLong(TimeSpanImpl(days.toInt(), hours.toInt(), minutes.toInt(), seconds.toInt(), millis.toInt()).getMillis(), null, null)
                        }
                    }
                }
            }
        }
        throw cacheWithinException()
    }

    private fun cacheWithinException(): EvaluatorException? {
        return EvaluatorException(
                "value of cachedWithin must be a literal value (string,boolean,number), a timespan can be defined as follows: 0.1 or createTimespan(1,2,3,4)")
    }

    @Throws(EvaluatorException::class)
    private fun toDouble(e: Expression?): Double {
        if (e !is Literal) throw EvaluatorException("Paremeters of the function createTimeSpan have to be literal numeric values in this context")
        val n: Number = (e as Literal?).getNumber(null)
                ?: throw EvaluatorException("Paremeters of the function createTimeSpan have to be literal numeric values in this context")
        return n.doubleValue()
    }

    fun visitLabel(ga: GeneratorAdapter?, label: Label?) {
        if (label != null) ga.visitLabel(label)
    }

    @Throws(IOException::class)
    fun getClassName(res: Resource?): String? {
        val src: ByteArray = IOUtil.toBytes(res)
        val cr = ClassReader(src)
        return cr.getClassName()
    }

    fun getClassName(barr: ByteArray?): String? {
        return ClassReader(barr).getClassName()
    }

    @Throws(IOException::class)
    fun getSourceInfo(config: Config?, clazz: Class?, onlyCFC: Boolean): SourceInfo? {
        return SourceNameClassVisitor.getSourceInfo(config, clazz, onlyCFC)
    }

    fun hasOnlyDataMembers(`var`: Variable?): Boolean {
        val it: Iterator<Member?> = `var`.getMembers().iterator()
        var m: Member?
        while (it.hasNext()) {
            m = it.next()
            if (m !is DataMember) return false
        }
        return true
    }

    fun count(statements: List<Statement?>?, recursive: Boolean): Int {
        if (statements == null) return 0
        var count = 0
        val it: Iterator<Statement?> = statements.iterator()
        while (it.hasNext()) {
            count += count(it.next(), recursive)
        }
        return count
    }

    fun count(s: Statement?, recursive: Boolean): Int {
        var count = 1
        if (recursive && s is HasBody) {
            val b: Body = (s as HasBody?).getBody()
            if (b != null) count += count(b.getStatements(), recursive)
        }
        return count
    }

    fun dump(s: Statement?, level: Int) {
        for (i in 0 until level) System.err.print("-")
        aprint.e(s.getClass().getName())
        if (s is HasBody) {
            val b: Body = (s as HasBody?).getBody()
            if (b != null) {
                val it: Iterator<Statement?> = b.getStatements().iterator()
                while (it.hasNext()) {
                    dump(it.next(), level + 1)
                }
            }
        }
    }

    fun size(cw: ClassWriter?) {
        try {
            var mw: MethodVisitor? = null
            val fields: Array<Field?> = cw.getClass().getDeclaredFields()
            var f: Field?
            for (i in fields.indices) {
                f = fields[i]
                if (f.getType().getName().equals("org.objectweb.asm.MethodWriter")) {
                    f.setAccessible(true)
                    mw = f.get(cw) as MethodVisitor
                    break
                }
            }
        } catch (t: Throwable) {
            ExceptionUtil.rethrowIfNecessary(t)
        }
    }

    operator fun invoke(mode: Int, adapter: GeneratorAdapter?, type: Type?, method: Method?) {
        if (mode == INTERFACE) adapter.invokeInterface(type, method) else if (mode == VIRTUAL) adapter.invokeVirtual(type, method) else if (mode == STATIC) adapter.invokeStatic(type, method)
    }

    fun createBif(data: Data?, flf: FunctionLibFunction?): BIF? {
        val bif = BIF(data.factory, data.settings, flf)
        data.ep.add(flf, bif, data.srcCode)
        bif.setArgType(flf.getArgType())
        try {
            bif.setClassDefinition(flf.getFunctionClassDefinition())
        } catch (t: Throwable) {
            ExceptionUtil.rethrowIfNecessary(t)
            throw PageRuntimeException(t)
        }
        bif.setReturnType(flf.getReturnTypeAsString())
        return bif
    }

    fun isOnlyDataMember(v: Variable?): Boolean {
        val it: Iterator<Member?> = v.getMembers().iterator()
        while (it.hasNext()) {
            if (it.next() !is DataMember) return false
        }
        return true
    }

    fun addStatements(body: Body?, statements: List<Statement?>?) {
        val it: Iterator<Statement?> = statements!!.iterator()
        while (it.hasNext()) {
            body.addStatement(it.next())
        }
    }

    fun createEmptyStruct(adapter: GeneratorAdapter?) {
        adapter.newInstance(Types.STRUCT_IMPL)
        adapter.dup()
        adapter.invokeConstructor(Types.STRUCT_IMPL, Page.INIT_STRUCT_IMPL)
    }

    fun createEmptyArray(adapter: GeneratorAdapter?) {
        adapter.newInstance(Types.ARRAY_IMPL)
        adapter.dup()
        adapter.invokeConstructor(Types.ARRAY_IMPL, Switch.INIT)
    }
}