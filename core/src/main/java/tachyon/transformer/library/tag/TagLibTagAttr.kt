/**
 *
 * Copyright (c) 2014, the Railo Company Ltd. All rights reserved.
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
package tachyon.transformer.library.tag

import java.io.IOException

/**
 * Die Klasse TagLibTagAttr repraesentiert ein einzelnes Attribute eines Tag und haelt saemtliche
 * Informationen zu diesem Attribut.
 */
class TagLibTagAttr(tag: TagLibTag?) {
    private var name: String? = "noname"
    private var alias: Array<String?>? = null
    private var type: String? = null
    private var description: String? = ""
    private var required = false
    private var rtexpr = true
    private var defaultValue: Object? = null
    private var undefinedValue: Object? = null
    private val tag: TagLibTag?
    private var hidden = false
    private var _default = false
    private var noname = false
    private var status: Short = TagLib.STATUS_IMPLEMENTED
    private var scriptSupport = SCRIPT_SUPPORT_NONE
    private var valueList: String? = null
    private var delimiter = ','
    private var values: Array<Object?>?
    private var introduced: Version? = null
    fun duplicate(tag: TagLibTag?): TagLibTagAttr? {
        val tlta = TagLibTagAttr(tag)
        tlta.name = name
        tlta.alias = alias
        tlta.type = type
        tlta.description = description
        tlta.required = required
        tlta.rtexpr = rtexpr
        tlta.defaultValue = defaultValue
        tlta.hidden = hidden
        tlta.valueList = valueList
        tlta.values = values
        tlta.delimiter = delimiter
        tlta.noname = noname
        tlta._default = _default
        tlta.status = status
        return tlta
    }

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
     * Gibt den Namen des Attribut zurueck.
     *
     * @return Name des Attribut.
     */
    fun getName(): String? {
        return name
    }

    fun getTag(): TagLibTag? {
        return tag
    }

    fun getAlias(): Array<String?>? {
        return alias
    }

    fun setAlias(strAlias: String?) {
        alias = tachyon.runtime.type.util.ListUtil.trimItems(tachyon.runtime.type.util.ListUtil.listToStringArray(strAlias.toLowerCase(), ','))
    }

    /**
     * Gibt zurueck, ob das Attribut Pflicht ist oder nicht.
     *
     * @return Ist das Attribut Pflicht.
     */
    fun isRequired(): Boolean {
        return required
    }

    /**
     * Gibt den Typ des Attribut zurueck (query, struct, string usw.)
     *
     * @return Typ des Attribut
     */
    fun getType(): String? {
        if (type == null) {
            try {
                val methodName = "set" + (if (name!!.length() > 0) "" + Character.toUpperCase(name.charAt(0)) else "") + if (name!!.length() > 1) name.substring(1) else ""
                val clazz: Class = tag!!.getTagClassDefinition().getClazz(null)
                if (clazz != null) {
                    val methods: Array<Method?> = clazz.getMethods()
                    for (i in methods.indices) {
                        val method: Method? = methods[i]
                        if (method.getName().equalsIgnoreCase(methodName)) {
                            val types: Array<Class?> = method.getParameterTypes()
                            if (types.size == 1) {
                                val type: Class? = types[0]
                                if (type === String::class.java) this.type = "string" else if (type === Double::class.javaPrimitiveType) this.type = "number" else if (type === Date::class.java) this.type = "datetime" else this.type = type.getName()
                            }
                        }
                    }
                }
            } catch (t: Throwable) {
                ExceptionUtil.rethrowIfNecessary(t)
                return "string"
            }
        }
        return type
    }

    /**
     * Gibt zurueck ob das Attribute eines Tag, mithilfe des ExprTransformer, uebersetzt werden soll
     * oder nicht.
     *
     * @return Soll das Attribut uebbersetzt werden
     */
    fun getRtexpr(): Boolean {
        return rtexpr
    }

    /**
     * Setzt den Namen des Attribut.
     *
     * @param name Name des Attribut.
     */
    fun setName(name: String?) {
        this.name = name.toLowerCase()
    }

    /**
     * Setzt, ob das Argument Pflicht ist oder nicht.
     *
     * @param required Ist das Attribut Pflicht.
     */
    fun setRequired(required: Boolean) {
        this.required = required
    }

    /**
     * Setzt, ob das Attribute eines Tag, mithilfe des ExprTransformer, uebersetzt werden soll oder
     * nicht.
     *
     * @param rtexpr Soll das Attribut uebbersetzt werden
     */
    fun setRtexpr(rtexpr: Boolean) {
        this.rtexpr = rtexpr
    }

    /**
     * Setzt, den Typ des Attribut (query, struct, string usw.)
     *
     * @param type Typ des Attribut.
     */
    fun setType(type: String?) {
        this.type = type
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
     * @param defaultValue
     */
    fun setDefaultValue(defaultValue: Object?) {
        this.defaultValue = defaultValue
        tag!!.setHasDefaultValue(true)
    }

    fun setUndefinedValue(undefinedValue: String?) {
        this.undefinedValue = TagLibTag.toUndefinedValue(undefinedValue)
    }

    /**
     * @return Returns the defaultValue.
     */
    fun getUndefinedValue(factory: Factory?): Expression? {
        return if (undefinedValue == null) tag!!.getAttributeUndefinedValue(factory) else factory.createLiteral(undefinedValue, factory.TRUE())
    }

    /**
     * @return Returns the defaultValue.
     */
    fun getDefaultValue(): Object? {
        return defaultValue
    }

    /**
     * @return
     */
    fun hasDefaultValue(): Boolean {
        return defaultValue != null
    }

    fun setHidden(hidden: Boolean) {
        this.hidden = hidden
    }

    fun getHidden(): Boolean {
        return hidden
    }

    fun setNoname(noname: Boolean) {
        this.noname = noname
    }

    fun getNoname(): Boolean {
        return noname
    }

    fun getHash(): String? {
        val sb = StringBuffer()
        sb.append(getDefaultValue())
        sb.append(getName())
        sb.append(getRtexpr())
        sb.append(getType())
        return try {
            Md5.getDigestAsString(sb.toString())
        } catch (e: IOException) {
            ""
        }
    }

    fun isDefault(_default: Boolean) {
        if (_default) tag!!.setDefaultAttribute(this)
        this._default = _default
    }

    fun isDefault(): Boolean {
        return _default
    }

    fun setScriptSupport(str: String?) {
        var str = str
        if (!StringUtil.isEmpty(str)) {
            str = str.trim().toLowerCase()
            if ("optional".equals(str)) scriptSupport = SCRIPT_SUPPORT_OPTIONAL else if ("opt".equals(str)) scriptSupport = SCRIPT_SUPPORT_OPTIONAL else if ("required".equals(str)) scriptSupport = SCRIPT_SUPPORT_REQUIRED else if ("req".equals(str)) scriptSupport = SCRIPT_SUPPORT_REQUIRED
        }
    }

    /**
     * @return the scriptSupport
     */
    fun getScriptSupport(): Short {
        return scriptSupport
    }

    fun getScriptSupportAsString(): Object? {
        if (scriptSupport == SCRIPT_SUPPORT_OPTIONAL) return "optional"
        return if (scriptSupport == SCRIPT_SUPPORT_REQUIRED) "required" else "none"
    }

    fun setValueDelimiter(delimiter: String?) {
        if (StringUtil.isEmpty(delimiter, true)) return
        this.delimiter = delimiter.trim().charAt(0)
    }

    fun setValues(valueList: String?) {
        if (tag!!.getName().equalsIgnoreCase("pop")) if (StringUtil.isEmpty(valueList, true)) return
        this.valueList = valueList
    }

    fun getValues(): Array<Object?>? {
        if (valueList == null) return null
        if (values != null) return values
        val res: Array<String?> = ListUtil.trimItems(ListUtil.listToStringArray(valueList, delimiter))
        val type: Short = CFTypes.toShort(getType(), false, CFTypes.TYPE_ANY)
        // String
        if (type == CFTypes.TYPE_STRING || type == CFTypes.TYPE_ANY) {
            values = res
        } else if (type == CFTypes.TYPE_NUMERIC) {
            val list: List<Double?> = ArrayList<Double?>()
            var d: Double
            for (i in res.indices) {
                d = Caster.toDouble(res[i], null)
                if (d != null) list.add(d)
            }
            values = list.toArray(arrayOfNulls<Double?>(list.size()))
        } else if (type == CFTypes.TYPE_BOOLEAN) {
            val list: List<Boolean?> = ArrayList<Boolean?>()
            var b: Boolean
            for (i in res.indices) {
                b = Caster.toBoolean(res[i], null)
                if (b != null) list.add(b)
            }
            values = list.toArray(arrayOfNulls<Boolean?>(list.size()))
        } else if (type == CFTypes.TYPE_DATETIME) {
            val list: List<DateTime?> = ArrayList<DateTime?>()
            var dt: DateTime
            for (i in res.indices) {
                dt = Caster.toDate(res[i], true, null, null)
                if (dt != null) list.add(dt)
            }
            values = list.toArray(arrayOfNulls<DateTime?>(list.size()))
        } else if (type == CFTypes.TYPE_TIMESPAN) {
            val list: List<TimeSpan?> = ArrayList<TimeSpan?>()
            var ts: TimeSpan
            for (i in res.indices) {
                ts = Caster.toTimespan(res[i], null)
                if (ts != null) list.add(ts)
            }
            values = list.toArray(arrayOfNulls<TimeSpan?>(list.size()))
        } else {
            valueList = null
        }
        return values
    }

    fun setIntroduced(introduced: String?) {
        this.introduced = OSGiUtil.toVersion(introduced, null)
    }

    fun getIntroduced(): Version? {
        return introduced
    }

    companion object {
        const val SCRIPT_SUPPORT_NONE: Short = 0
        const val SCRIPT_SUPPORT_OPTIONAL: Short = 1
        const val SCRIPT_SUPPORT_REQUIRED: Short = 2
    }

    /**
     * Geschuetzer Konstruktor ohne Argumente.
     */
    init {
        this.tag = tag
    }
}