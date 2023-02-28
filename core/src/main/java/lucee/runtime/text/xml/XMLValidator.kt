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
package lucee.runtime.text.xml

import org.xml.sax.InputSource

class XMLValidator(validator: InputSource?, private val strSchema: String?) : XMLEntityResolverDefaultHandler(validator) {
    @Override
    @Throws(SAXException::class)
    override fun resolveEntity(publicID: String?, systemID: String?): InputSource? {
        // print.out(publicID+":"+systemID);
        return super.resolveEntity(publicID, systemID)
    }

    private var warnings: Array? = null
    private var errors: Array? = null
    private var fatals: Array? = null
    private var hasErrors = false
    private fun release() {
        warnings = null
        errors = null
        fatals = null
        hasErrors = false
    }

    @Override
    fun warning(spe: SAXParseException?) {
        log(spe, "Warning", warnings)
    }

    @Override
    fun error(spe: SAXParseException?) {
        hasErrors = true
        log(spe, "Error", errors)
    }

    @Override
    @Throws(SAXException::class)
    fun fatalError(spe: SAXParseException?) {
        hasErrors = true
        log(spe, "Fatal Error", fatals)
    }

    private fun log(spe: SAXParseException?, type: String?, array: Array?) {
        val sb = StringBuffer("[$type] ")
        val id: String = spe.getSystemId()
        if (!StringUtil.isEmpty(id)) {
            val li: Int = id.lastIndexOf('/')
            if (li != -1) sb.append(id.substring(li + 1)) else sb.append(id)
        }
        sb.append(':')
        sb.append(spe.getLineNumber())
        sb.append(':')
        sb.append(spe.getColumnNumber())
        sb.append(": ")
        sb.append(spe.getMessage())
        sb.append(" ")
        array.appendEL(sb.toString())
    }

    @Throws(XMLException::class)
    fun validate(xml: InputSource?, result: Struct?): Struct? {
        var result: Struct? = result
        if (result == null) {
            warnings = ArrayImpl()
            errors = ArrayImpl()
            fatals = ArrayImpl()
            result = StructImpl()
            result.setEL(KeyConstants._warnings, warnings)
            result.setEL(KeyConstants._errors, errors)
            result.setEL(KeyConstants._fatalerrors, fatals)
        } else {
            warnings = getArray(result, KeyConstants._warnings)
            errors = getArray(result, KeyConstants._errors)
            fatals = getArray(result, KeyConstants._fatalerrors)
            hasErrors = !getBoolean(result, KeyConstants._status)
        }
        try {
            val parser: XMLReader = XMLUtil.createXMLReader()
            parser.setContentHandler(this)
            parser.setErrorHandler(this)
            parser.setEntityResolver(this)
            parser.setFeature("http://xml.org/sax/features/validation", true)
            parser.setFeature("http://apache.org/xml/features/validation/schema", true)
            parser.setFeature("http://apache.org/xml/features/validation/schema-full-checking", true)
            // if(!validateNamespace)
            if (!StringUtil.isEmpty(strSchema)) parser.setProperty("http://apache.org/xml/properties/schema/external-noNamespaceSchemaLocation", strSchema)
            parser.parse(xml)
        } catch (e: Exception) {
            throw XMLException(e)
        }
        result.setEL(KeyConstants._status, Caster.toBoolean(!hasErrors))
        release()
        return result
    }

    private fun getArray(result: Struct?, key: Key?): Array? {
        val arr: Array = Caster.toArray(result.get(key, null), null)
        return if (arr != null) arr else ArrayImpl()
    }

    private fun getBoolean(result: Struct?, key: Key?): Boolean {
        return Caster.toBooleanValue(result.get(key, null), true)
    }
}