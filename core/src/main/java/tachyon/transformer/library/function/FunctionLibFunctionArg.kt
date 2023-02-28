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
package tachyon.transformer.library.function

import java.io.IOException

/**
 * Eine FunctionLibFunctionArg repraesentiert ein einzelnes Argument einer Funktion.
 */
class FunctionLibFunctionArg {
    /**
     * @return the hidden
     */
    fun isHidden(): Boolean {
        return hidden
    }

    private var strType: String? = null
    private var required = false
    private var function: FunctionLibFunction? = null
    private var name: String? = null
    private var description: String? = ""
    private var alias: String? = null
    private var defaultValue: String? = null
    private var hidden = false
    private var status: Short = TagLib.STATUS_IMPLEMENTED
    private var type = UNDEFINED
    private var introduced: Version? = null

    /**
     * Geschuetzer Konstruktor ohne Argumente.
     */
    constructor() {}
    constructor(function: FunctionLibFunction?) {
        this.function = function
    }

    /**
     * Gibt den Typ des Argument als String zurueck (query, struct, string usw.)
     *
     * @return Typ des Argument
     */
    fun getTypeAsString(): String? {
        return strType
    }

    /**
     * Gibt den Typ des Argument zurueck (query, struct, string usw.)
     *
     * @return Typ des Argument
     */
    fun getType(): Short {
        if (type == UNDEFINED) {
            type = CFTypes.toShort(strType, false, CFTypes.TYPE_UNKNOW)
        }
        return type
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
     * Gibt zurueck, ob das Argument Pflicht ist oder nicht, alias fuer isRequired.
     *
     * @return Ist das Argument Pflicht.
     */
    fun isRequired(): Boolean {
        return required
    }

    /**
     * Gibt zurueck, ob das Argument Pflicht ist oder nicht.
     *
     * @return Ist das Argument Pflicht.
     */
    fun getRequired(): Boolean {
        return required
    }

    /**
     * Gibt die Funktion zurueck zu der das Argument gehoert.
     *
     * @return Zugehoerige Funktion.
     */
    fun getFunction(): FunctionLibFunction? {
        return function
    }

    /**
     * Setzt die Funktion zu der das Argument gehoert.
     *
     * @param function Zugehoerige Funktion.
     */
    fun setFunction(function: FunctionLibFunction?) {
        this.function = function
    }

    /**
     * Setzt, den Typ des Argument (query, struct, string usw.)
     *
     * @param type Typ des Argument.
     */
    fun setType(type: String?) {
        strType = type
    }

    /**
     * Setzt, ob das Argument Pflicht ist oder nicht.
     *
     * @param value Ist das Argument Pflicht.
     */
    fun setRequired(value: String?) {
        var value = value
        value = value.toLowerCase().trim()
        required = value.equals("yes") || value.equals("true")
    }

    fun setRequired(value: Boolean) {
        required = value
    }

    /**
     * @return the name
     */
    fun getName(): String? {
        return name
    }

    /**
     * @param name the name to set
     */
    fun setName(name: String?) {
        this.name = name
    }

    fun getDescription(): String? {
        return description
    }

    /**
     * @param description the description to set
     */
    fun setDescription(description: String?) {
        this.description = description
    }

    /**
     * @return the defaultValue
     */
    fun getDefaultValue(): String? {
        return defaultValue
    }

    /**
     * @param defaultValue the defaultValue to set
     */
    fun setDefaultValue(defaultValue: String?) {
        this.defaultValue = defaultValue
    }

    fun getHash(): String? {
        val sb = StringBuffer()
        sb.append(getDefaultValue())
        sb.append(getName())
        sb.append(getRequired())
        sb.append(getTypeAsString())
        sb.append(getTypeAsString())
        sb.append(getAlias())
        return try {
            Md5.getDigestAsString(sb.toString())
        } catch (e: IOException) {
            ""
        }
    }

    /**
     * @return the alias
     */
    fun getAlias(): String? {
        return alias
    }

    /**
     * @param alias the alias to set
     */
    fun setAlias(alias: String?) {
        this.alias = alias
    }

    fun setHidden(hidden: Boolean) {
        this.hidden = hidden
    }

    fun setIntroduced(introduced: String?) {
        this.introduced = OSGiUtil.toVersion(introduced, null)
    }

    fun getIntroduced(): Version? {
        return introduced
    }

    companion object {
        private const val UNDEFINED: Short = -12553
    }
}