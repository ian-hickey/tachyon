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
package lucee.transformer.library.tag

import java.io.IOException

/**
 * Die Klasse TagLibTag repaesentiert ein einzelne Tag Definition einer TagLib, beschreibt also alle
 * Informationen die man zum validieren eines Tags braucht.
 */
class TagLibTag(tagLib: TagLib?) {
    private var attributeType = 0
    private var name: String? = null
    private var hasBody = true
    private var isBodyReq = false
    private var isTagDependent = false
    private var bodyFree = true
    private var parseBody = false
    private var hasAppendix = false
    private var description: String? = ""
    private var tagCD: ClassDefinition? = null
    private var tteCD: ClassDefinition? = null
    private var tdbtCD: ClassDefinition? = null
    private var tttCD: ClassDefinition? = null
    private var min = 0
    private var max = 0
    private var tagLib: TagLib?
    private var eval: TagEvaluator? = null
    private var tdbt: TagDependentBodyTransformer? = null
    private val attributes: Map<String?, TagLibTagAttr?>? = LinkedHashMap<String?, TagLibTagAttr?>()
    private val setters: Map<String?, String?>? = HashMap<String?, String?>()
    private var attrFirst: TagLibTagAttr? = null
    private var attrLast: TagLibTagAttr? = null
    private var cdAttributeEvaluator: ClassDefinition<out AttributeEvaluator?>? = null
    private var handleException = false
    private var hasDefaultValue = false

    // private Type tagType;
    private var tttConstructor: Constructor? = null
    private var allowRemovingLiteral = false
    private var defaultAttribute: TagLibTagAttr? = null
    private var status: Short = TagLib.STATUS_IMPLEMENTED
    private val clazz: Class? = null
    private var script: TagLibTagScript? = null
    private var singleAttr: TagLibTagAttr? = UNDEFINED
    private var attrUndefinedValue: Object? = null
    private var bundleName: String? = null
    private var bundleVersion: Version? = null
    private var introduced: Version? = null
    fun duplicate(cloneAttributes: Boolean): TagLibTag? {
        val tlt = TagLibTag(tagLib)
        tlt.attributeType = attributeType
        tlt.name = name
        tlt.hasBody = hasBody
        tlt.isBodyReq = isBodyReq
        tlt.isTagDependent = isTagDependent
        tlt.bodyFree = bodyFree
        tlt.parseBody = parseBody
        tlt.hasAppendix = hasAppendix
        tlt.description = description
        tlt.tagCD = tagCD
        tlt.bundleName = bundleName
        tlt.bundleVersion = bundleVersion
        tlt.tteCD = tteCD
        tlt.eval = eval
        tlt.tdbtCD = tdbtCD
        tlt.min = min
        tlt.max = max
        tlt.cdAttributeEvaluator = cdAttributeEvaluator
        tlt.handleException = handleException
        tlt.hasDefaultValue = hasDefaultValue
        // tlt.tagType=tagType;
        tlt.tttCD = tttCD
        tlt.tttConstructor = tttConstructor
        tlt.allowRemovingLiteral = allowRemovingLiteral
        tlt.status = status
        tlt.eval = null
        tlt.tdbt = null
        val it: Iterator<Entry<String?, TagLibTagAttr?>?> = attributes.entrySet().iterator()
        if (cloneAttributes) {
            while (it.hasNext()) {
                tlt.setAttribute(it.next().getValue().duplicate(tlt))
            }
            if (defaultAttribute != null) tlt.defaultAttribute = defaultAttribute.duplicate(tlt)
        } else {
            while (it.hasNext()) {
                tlt.setAttribute(it.next().getValue())
                tlt.attrFirst = attrFirst
                tlt.attrLast = attrLast
            }
            tlt.defaultAttribute = defaultAttribute
        }

        // setter
        val sit: Iterator<Entry<String?, String?>?> = setters.entrySet().iterator()
        var se: Entry<String?, String?>?
        while (sit.hasNext()) {
            se = sit.next()
            tlt.setters.put(se.getKey(), se.getValue())
        }

        /*
		 * private Map attributes=new HashMap(); private TagLibTagAttr attrFirst; private TagLibTagAttr
		 * attrLast;
		 * 
		 * private Map setters=new HashMap(); private TagLibTagAttr defaultAttribute;
		 */return tlt
    }

    /**
     * Gibt alle Attribute (TagLibTagAttr) eines Tag als HashMap zurueck.
     *
     * @return HashMap Attribute als HashMap.
     */
    fun getAttributes(): Map<String?, TagLibTagAttr?>? {
        return attributes
    }

    /**
     * Gibt ein bestimmtes Attribut anhand seines Namens zurueck, falls dieses Attribut nicht existiert
     * wird null zurueckgegeben.
     *
     * @param name Name des Attribut das zurueckgegeben werden soll.
     * @return Attribute das angfragt wurde oder null.
     */
    fun getAttribute(name: String?): TagLibTagAttr? {
        return getAttribute(name, false)
    }

    fun getAttribute(name: String?, checkAlias: Boolean): TagLibTagAttr? {
        val attr: TagLibTagAttr? = attributes!![name]
        // checking alias
        return if (attr == null && checkAlias) getAttributeByAlias(name) else attr
    }

    fun getAttributeByAlias(alias: String?): TagLibTagAttr? {
        val it: Iterator<TagLibTagAttr?> = attributes!!.values().iterator()
        var attr: TagLibTagAttr?
        var aliases: Array<String?>
        while (it.hasNext()) {
            attr = it.next()
            if (ArrayUtil.isEmpty(attr!!.getAlias())) continue
            aliases = attr!!.getAlias()
            for (i in aliases.indices) {
                if (aliases[i].equalsIgnoreCase(alias)) return attr
            }
        }
        return null
    }

    /**
     * Gibt das erste Attribut, welches innerhalb des Tag definiert wurde, zurueck.
     *
     * @return Attribut das angfragt wurde oder null.
     */
    fun getFirstAttribute(): TagLibTagAttr? {
        return attrFirst
    }

    /**
     * Gibt das letzte Attribut, welches innerhalb des Tag definiert wurde, zurueck.
     *
     * @return Attribut das angfragt wurde oder null.
     */
    fun getLastAttribute(): TagLibTagAttr? {
        return attrLast
    }

    /**
     * Gibt den Namen des Tag zurueck.
     *
     * @return String Name des Tag.
     */
    fun getName(): String? {
        return name
    }

    /**
     * Gibt den kompletten Namen des Tag zurueck, inkl. Name-Space und Trenner.
     *
     * @return String Kompletter Name des Tag.
     */
    fun getFullName(): String? {
        val fullName: String?
        fullName = if (tagLib != null) {
            tagLib.getNameSpaceAndSeparator() + name
        } else {
            name
        }
        return fullName
    }

    fun getTagClassDefinition(): ClassDefinition? {
        return tagCD
    }

    fun setTagClassDefinition(tagClass: String?, id: Identification?, attributes: Map<String?, String?>?) {
        tagCD = ClassDefinitionImpl.toClassDefinition(tagClass, id, attributes)
    }

    fun setTagClassDefinition(cd: ClassDefinition?) {
        tagCD = cd
    }
    /*
	 * public Type getTagTypeX() throws ClassException, BundleException { if(tagType==null) {
	 * tagType=Type.getType(getTagClassDefinition().getClazz()); } return tagType; }
	 */
    /**
     * @return the status
     * (TagLib.,TagLib.STATUS_IMPLEMENTED,TagLib.STATUS_DEPRECATED,TagLib.STATUS_UNIMPLEMENTED)
     */
    fun getStatus(): Short {
        return status
    }

    /**
     * @param status the status to set
     * (TagLib.,TagLib.STATUS_IMPLEMENTED,TagLib.STATUS_DEPRECATED,TagLib.STATUS_UNIMPLEMENTED)
     */
    fun setStatus(status: Short) {
        this.status = status
    }

    /**
     * Gibt die Klassendefinition, der Klasse die den Evaluator (Translation Time Evaluator)
     * implementiert, als Zeichenkette zurueck. Falls kein Evaluator definiert ist wird null
     * zurueckgegeben.
     *
     * @return String Zeichenkette der Klassendefinition.
     */
    private fun getTTEClassDefinition(): ClassDefinition? {
        return tteCD
    }

    fun getTTTClassDefinition(): ClassDefinition? {
        return tttCD
    }

    /**
     * Gibt den Evaluator (Translation Time Evaluator) dieser Klasse zurueck. Falls kein Evaluator
     * definiert ist, wird null zurueckgegeben.
     *
     * @return Implementation des Evaluator zu dieser Klasse.
     * @throws EvaluatorException Falls die Evaluator-Klasse nicht geladen werden kann.
     */
    @Throws(EvaluatorException::class)
    fun getEvaluator(): TagEvaluator? {
        if (!hasTTE()) return null
        if (eval != null) return eval
        eval = try {
            ClassUtil.newInstance(getTTEClassDefinition().getClazz()) as TagEvaluator
        } catch (t: Throwable) {
            ExceptionUtil.rethrowIfNecessary(t)
            throw EvaluatorException(t.getMessage())
        }
        return eval
    }

    /**
     * Gibt den TagDependentBodyTransformer dieser Klasse zurueck. Falls kein
     * TagDependentBodyTransformer definiert ist, wird null zurueckgegeben.
     *
     * @return Implementation des TagDependentBodyTransformer zu dieser Klasse.
     * @throws TagLibException Falls die TagDependentBodyTransformer-Klasse nicht geladen werden kann.
     */
    @Throws(TagLibException::class)
    fun getBodyTransformer(): TagDependentBodyTransformer? {
        if (!hasTDBTClassDefinition()) return null
        if (tdbt != null) return tdbt
        tdbt = try {
            ClassUtil.newInstance(tdbtCD.getClazz()) as TagDependentBodyTransformer
        } catch (t: Throwable) {
            ExceptionUtil.rethrowIfNecessary(t)
            throw TagLibException(t)
        }
        return tdbt
    }

    /**
     * Gibt zurueck ob Exception durch die implementierte Klasse abgehandelt werden oder nicht
     *
     * @return Wird eine Exception abgehandelt?
     */
    fun handleException(): Boolean {
        return handleException
    }

    /**
     * Gibt zurueck, ob eine Klassendefinition der Klasse die den Evaluator (Translation Time Evaluator)
     * implementiert existiert.
     *
     * @return Ob eine Evaluator definiert ist.
     */
    fun hasTTE(): Boolean {
        return tteCD != null || eval != null
    }

    fun hasTTTClassDefinition(): Boolean {
        return tttCD != null
    }

    /**
     * Gibt zurueck, ob eine Klassendefinition der Klasse die den TagDependentBodyTransformer
     * implementiert existiert.
     *
     * @return Ob eine Evaluator definiert ist.
     */
    fun hasTDBTClassDefinition(): Boolean {
        return tdbtCD != null
    }

    /**
     * Gibt den Attributetyp der Klasse zurueck. ( ATTRIBUTE_TYPE_FIX, ATTRIBUTE_TYPE_DYNAMIC,
     * ATTRIBUTE_TYPE_NONAME)
     *
     * @return int
     */
    fun getAttributeType(): Int {
        return attributeType
    }

    /**
     * Gibt zurueck, ob das Tag einen Body haben kann oder nicht.
     *
     * @return Kann das Tag einen Body haben.
     */
    fun getHasBody(): Boolean {
        return hasBody
    }

    /**
     * Gibt die maximale Anzahl Attribute zurueck, die das Tag haben kann.
     *
     * @return Maximale moegliche Anzahl Attribute.
     */
    fun getMax(): Int {
        return max
    }

    /**
     * Gibt die minimale Anzahl Attribute zurueck, die das Tag haben muss.
     *
     * @return Minimal moegliche Anzahl Attribute.
     */
    fun getMin(): Int {
        return min
    }

    /**
     * Gibt die TagLib zurueck zu der das Tag gehoert.
     *
     * @return TagLib Zugehoerige TagLib.
     */
    fun getTagLib(): TagLib? {
        return tagLib
    }

    /**
     * Gibt zurueck ob das Tag seinen Body parsen soll oder nicht.
     *
     * @return Soll der Body geparst werden.
     */
    fun getParseBody(): Boolean {
        return parseBody
    }

    /**
     * Gibt zurueck, ob das Tag einen Appendix besitzen kann oder nicht.
     *
     * @return Kann das Tag einen Appendix besitzen.
     */
    fun hasAppendix(): Boolean {
        return hasAppendix
    }

    /**
     * Fragt ab ob der Body eines Tag freiwillig ist oder nicht.
     *
     * @return is required
     */
    fun isBodyReq(): Boolean {
        return isBodyReq
    }

    /**
     * Fragt ab ob die verarbeitung des Inhaltes eines Tag mit einem eigenen Transformer vorgenommen
     * werden soll.
     *
     * @return Fragt ab ob die verarbeitung des Inhaltes eines Tag mit einem eigenen Transformer
     * vorgenommen werden soll.
     */
    fun isTagDependent(): Boolean {
        return isTagDependent
    }

    /**
     * Setzt die TagLib des Tag. Diese Methode wird durch die Klasse TagLibFactory verwendet.
     *
     * @param tagLib TagLib des Tag.
     */
    fun setTagLib(tagLib: TagLib?) {
        this.tagLib = tagLib
    }

    /**
     * Setzt ein einzelnes Attribut (TagLibTagAttr) eines Tag. Diese Methode wird durch die Klasse
     * TagLibFactory verwendet.
     *
     * @param attribute Attribute eines Tag.
     */
    fun setAttribute(attribute: TagLibTagAttr?) {
        attributes.put(attribute!!.getName(), attribute)
        if (attrFirst == null) attrFirst = attribute
        attrLast = attribute
    }

    /**
     * Setzt den Attributtyp eines Tag. ( ATTRIBUTE_TYPE_FIX, ATTRIBUTE_TYPE_DYNAMIC,
     * ATTRIBUTE_TYPE_FULLDYNAMIC, ATTRIBUTE_TYPE_NONAME) Diese Methode wird durch die Klasse
     * TagLibFactory verwendet.
     *
     * @param attributeType The attributeType to set
     */
    fun setAttributeType(attributeType: Int) {
        this.attributeType = attributeType
    }

    /**
     * Setzt die Information, was fuer ein BodyContent das Tag haben kann. Diese Methode wird durch die
     * Klasse TagLibFactory verwendet.
     *
     * @param value BodyContent Information.
     */
    fun setBodyContent(value: String?) {
        // empty, free, must, tagdependent
        var value = value
        value = value.toLowerCase().trim()
        // if(value.equals("jsp")) value="free";
        hasBody = !value.equals("empty")
        isBodyReq = !value.equals("free")
        isTagDependent = value.equals("tagdependent")
        bodyFree = value.equals("free")
    }

    /**
     * Setzt wieviele Attribute das Tag maximal haben darf. Diese Methode wird durch die Klasse
     * TagLibFactory verwendet.
     *
     * @param max The max to set
     */
    fun setMax(max: Int) {
        this.max = max
    }

    /**
     * Setzt wieviele Attribute das Tag minimal haben darf. Diese Methode wird durch die Klasse
     * TagLibFactory verwendet.
     *
     * @param min The min to set
     */
    fun setMin(min: Int) {
        this.min = min
    }

    /**
     * Setzt den Namen des Tag. Diese Methode wird durch die Klasse TagLibFactory verwendet.
     *
     * @param name Name des Tag.
     */
    fun setName(name: String?) {
        this.name = name.toLowerCase()
    }

    fun setBundleName(bundleName: String?) {
        this.bundleName = bundleName.trim()
    }

    fun setBundleVersion(bundleVersion: String?) {
        // TODO allow 1.0.0.0-2.0.0.0,3.0.0.0
        this.bundleVersion = OSGiUtil.toVersion(bundleVersion.trim(), null)
    }

    /**
     * Setzt die implementierende Klassendefinition des Evaluator. Diese Methode wird durch die Klasse
     * TagLibFactory verwendet.
     *
     * @param tteClass Klassendefinition der Evaluator-Implementation.
     */
    fun setTTEClassDefinition(tteClass: String?, id: Identification?, attr: Map<String?, String?>?) {
        tteCD = ClassDefinitionImpl.toClassDefinition(tteClass, id, attr)
    }

    fun setTagEval(eval: TagEvaluator?) {
        this.eval = eval
    }

    /**
     * Setzt die implementierende Klassendefinition des Evaluator. Diese Methode wird durch die Klasse
     * TagLibFactory verwendet.
     *
     * @param tteClass Klassendefinition der Evaluator-Implementation.
     */
    fun setTTTClassDefinition(tttClass: String?, id: Identification?, attr: Map<String?, String?>?) {
        tttCD = ClassDefinitionImpl.toClassDefinition(tttClass, id, attr)
        tttConstructor = null
    }

    /**
     * Setzt die implementierende Klassendefinition des TagDependentBodyTransformer. Diese Methode wird
     * durch die Klasse TagLibFactory verwendet.
     *
     * @param tdbtClass Klassendefinition der TagDependentBodyTransformer-Implementation.
     */
    fun setTDBTClassDefinition(tdbtClass: String?, id: Identification?, attr: Map<String?, String?>?) {
        tdbtCD = ClassDefinitionImpl.toClassDefinition(tdbtClass, id, attr)
        tdbt = null
    }

    /**
     * Setzt, ob der Body des Tag geparst werden soll oder nicht. Diese Methode wird durch die Klasse
     * TagLibFactory verwendet.
     *
     * @param parseBody Soll der Body geparst werden.
     */
    fun setParseBody(parseBody: Boolean) {
        this.parseBody = parseBody
    }

    /**
     * Setzt ob das Tag einen Appendix besitzen kann oder nicht. Diese Methode wird durch die Klasse
     * TagLibFactory verwendet.
     *
     * @param hasAppendix Kann das Tag einen Appendix besitzen.
     */
    fun setAppendix(hasAppendix: Boolean) {
        this.hasAppendix = hasAppendix
    }

    /**
     * @return Returns the description.
     */
    fun getDescription(): String? {
        return description
    }

    /**
     * @param description The description to set.
     */
    fun setDescription(description: String?) {
        this.description = description
    }

    /**
     * @return Returns the bodyIsFree.
     */
    fun isBodyFree(): Boolean {
        return bodyFree
    }

    fun hasBodyMethodExists(): Boolean {
        val clazz: Class = getTagClassDefinition().getClazz(null) ?: return false
        try {
            val method: java.lang.reflect.Method = clazz.getMethod("hasBody", arrayOf<Class?>(Boolean::class.javaPrimitiveType))
                    ?: return false
            return method.getReturnType() === Void.TYPE
        } catch (e: Exception) {
        }
        return false
    }

    /**
     * @return Gibt zurueck ob ein Attribut Evaluator definiert ist oder nicht.
     */
    fun hasAttributeEvaluator(): Boolean {
        return cdAttributeEvaluator != null
    }

    /**
     * @return Gibt den AttributeEvaluator zum Tag zurueck
     * @throws AttributeEvaluatorException
     */
    @Throws(AttributeEvaluatorException::class)
    fun getAttributeEvaluator(): AttributeEvaluator? {
        return if (!hasAttributeEvaluator()) null else try {
            ClassUtil.loadInstance(cdAttributeEvaluator.getClazz()) as AttributeEvaluator
        } catch (e: Exception) {
            throw AttributeEvaluatorException(e.getMessage())
        }
    }

    /**
     * Setzt den Namen der Klasse welche einen AttributeEvaluator implementiert.
     *
     * @param value Name der AttributeEvaluator Klassse
     */
    fun setAttributeEvaluatorClassDefinition(className: String?, id: Identification?, attr: Map<String?, String?>?) {
        cdAttributeEvaluator = ClassDefinitionImpl.toClassDefinition(className, id, attr)
    }

    /**
     * sets if tag handle exception inside his body or not
     *
     * @param handleException handle it or not
     */
    fun setHandleExceptions(handleException: Boolean) {
        this.handleException = handleException
    }

    /**
     * @return
     */
    fun hasDefaultValue(): Boolean {
        return hasDefaultValue
    }

    /**
     * @param hasDefaultValue The hasDefaultValue to set.
     */
    fun setHasDefaultValue(hasDefaultValue: Boolean) {
        this.hasDefaultValue = hasDefaultValue
    }

    /**
     * return ASM Tag for this tag
     *
     * @param line
     * @return
     */
    @Throws(TagLibException::class)
    fun getTag(f: Factory?, start: Position?, end: Position?): Tag? {
        return if (StringUtil.isEmpty(tttCD)) TagOther(f, start, end) else try {
            _getTag(f, start, end)
        } catch (e: ClassException) {
            throw TagLibException(e.getMessage())
        } catch (e: NoSuchMethodException) {
            throw TagLibException(e.getMessage())
        } catch (e: Throwable) {
            ExceptionUtil.rethrowIfNecessary(e)
            throw TagLibException(e)
        }
    }

    @Throws(ClassException::class, SecurityException::class, NoSuchMethodException::class, IllegalArgumentException::class, InstantiationException::class, IllegalAccessException::class, InvocationTargetException::class, BundleException::class)
    private fun _getTag(f: Factory?, start: Position?, end: Position?): Tag? {
        if (tttConstructor == null) {
            val clazz: Class = tttCD.getClazz()
            tttConstructor = clazz.getConstructor(CONSTRUCTOR_PARAMS3)
        }
        return tttConstructor.newInstance(arrayOf<Object?>(f, start, end)) as Tag
    }

    fun setAllowRemovingLiteral(allowRemovingLiteral: Boolean) {
        this.allowRemovingLiteral = allowRemovingLiteral
    }

    /**
     * @return the allowRemovingLiteral
     */
    fun isAllowRemovingLiteral(): Boolean {
        return allowRemovingLiteral
    }

    fun getAttributeNames(): String? {
        val it: Iterator<String?> = attributes.keySet().iterator()
        val sb = StringBuffer()
        while (it.hasNext()) {
            if (sb.length() > 0) sb.append(", ")
            sb.append(it.next())
        }
        return sb.toString()
    }

    fun getSetter(attr: Attribute?, typeClassName: String?): String? {
        var typeClassName = typeClassName
        if (tagLib!!.isCore()) return "set" + StringUtil.ucFirst(attr.getName())
        var setter = setters!![attr.getName()]
        if (setter != null) return setter
        setter = "set" + StringUtil.ucFirst(attr.getName())
        val clazz: Class
        try {
            if (StringUtil.isEmpty(typeClassName)) typeClassName = CastOther.getType(null, attr.getType()).getClassName()
            clazz = getTagClassDefinition().getClazz()
            val m: java.lang.reflect.Method = ClassUtil.getMethodIgnoreCase(clazz, setter, arrayOf<Class?>(ClassUtil.loadClass(typeClassName)))
            setter = m.getName()
        } catch (e: Exception) {
            LogUtil.log(TagLibTag::class.java.getName(), e)
        }
        setters.put(attr.getName(), setter)
        return setter
    }

    fun getHash(): String? {
        val sb = StringBuilder()
        sb.append(tagCD)
        sb.append(getAttributeNames())
        sb.append(getAttributeType())
        sb.append(getMax())
        sb.append(getMin())
        sb.append(getName())
        sb.append(getParseBody())
        sb.append(getTTEClassDefinition())
        sb.append(getTTTClassDefinition())
        val it: Iterator<Entry<String?, TagLibTagAttr?>?> = getAttributes().entrySet().iterator()
        var entry: Entry<String?, TagLibTagAttr?>?
        while (it.hasNext()) {
            entry = it.next()
            sb.append(entry.getKey())
            sb.append(entry.getValue().getHash())
        }
        return try {
            Md5.getDigestAsString(sb.toString())
        } catch (e: IOException) {
            ""
        }
    }

    fun getDefaultAttribute(): TagLibTagAttr? {
        return defaultAttribute
    }

    fun setDefaultAttribute(defaultAttribute: TagLibTagAttr?) {
        this.defaultAttribute = defaultAttribute
    }

    fun setScript(script: TagLibTagScript?) {
        this.script = script
    }

    /**
     * @return the script
     */
    fun getScript(): TagLibTagScript? {
        return script
    }

    fun getSingleAttr(): TagLibTagAttr? {
        if (singleAttr === UNDEFINED) {
            singleAttr = null
            val it: Iterator<TagLibTagAttr?> = getAttributes()!!.values().iterator()
            var attr: TagLibTagAttr?
            while (it.hasNext()) {
                attr = it.next()
                if (attr!!.getNoname()) {
                    singleAttr = attr
                    break
                }
            }
        }
        return singleAttr
    }

    /**
     * attribute value set, if the attribute has no value defined
     *
     * @return
     */
    fun getAttributeUndefinedValue(factory: Factory?): Expression? {
        return if (attrUndefinedValue == null) factory.TRUE() else factory.createLiteral(attrUndefinedValue, factory.TRUE())
    }

    fun setAttributeUndefinedValue(undefinedValue: String?) {
        attrUndefinedValue = toUndefinedValue(undefinedValue)
    }

    fun setIntroduced(introduced: String?) {
        this.introduced = OSGiUtil.toVersion(introduced, null)
    }

    fun getIntroduced(): Version? {
        return introduced
    }

    companion object {
        const val ATTRIBUTE_TYPE_FIXED = 0
        const val ATTRIBUTE_TYPE_DYNAMIC = 1
        const val ATTRIBUTE_TYPE_NONAME = 3
        const val ATTRIBUTE_TYPE_MIXED = 4
        /**
         * Definition des Attribut Type
         */
        // public final static int ATTRIBUTE_TYPE_FULLDYNAMIC=2; deprecated
        /**
         * Definition des Attribut Type
         */
        private val CONSTRUCTOR_PARAMS3: Array<Class?>? = arrayOf<Class?>(Factory::class.java, Position::class.java, Position::class.java)
        private val UNDEFINED: TagLibTagAttr? = TagLibTagAttr(null)
        fun toUndefinedValue(undefinedValue: String?): Object? {
            var undefinedValue = undefinedValue
            undefinedValue = undefinedValue.trim()
            // boolean
            if (StringUtil.startsWithIgnoreCase(undefinedValue, "boolean:")) {
                val str: String = undefinedValue.substring(8).trim()
                val b: Boolean = Caster.toBoolean(str, null)
                if (b != null) return b
            } else if (StringUtil.startsWithIgnoreCase(undefinedValue, "number:")) {
                val str: String = undefinedValue.substring(7).trim()
                val d: Double = Caster.toDouble(str, null)
                if (d != null) return d
            }
            return undefinedValue
        }
    }

    /**
     * Geschuetzer Konstruktor ohne Argumente.
     *
     * @param tagLib
     */
    init {
        this.tagLib = tagLib
    }
}