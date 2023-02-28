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
package lucee.runtime.util

import java.io.IOException

internal class XMLValidator(validator: InputSource?, private val strSchema: String?) : XMLEntityResolverDefaultHandler(validator) {
    @Override
    @Throws(SAXException::class)
    fun resolveEntity(publicID: String?, systemID: String?): InputSource {
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
    fun warning(spe: SAXParseException) {
        log(spe, "Warning", warnings)
    }

    @Override
    fun error(spe: SAXParseException) {
        hasErrors = true
        log(spe, "Error", errors)
    }

    @Override
    @Throws(SAXException::class)
    fun fatalError(spe: SAXParseException) {
        hasErrors = true
        log(spe, "Fatal Error", fatals)
    }

    private fun log(spe: SAXParseException, type: String, array: Array?) {
        val sb = StringBuffer("[$type] ")
        val id: String = spe.getSystemId()
        if (!Util.isEmpty(id)) {
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

    @Throws(PageException::class)
    fun validate(xml: InputSource?): Struct {
        val engine: CFMLEngine = CFMLEngineFactory.getInstance()
        warnings = engine.getCreationUtil().createArray()
        errors = engine.getCreationUtil().createArray()
        fatals = engine.getCreationUtil().createArray()
        try {
            val parser: XMLReader = XMLUtilImpl().createXMLReader("org.apache.xerces.parsers.SAXParser")
            parser.setContentHandler(this)
            parser.setErrorHandler(this)
            parser.setEntityResolver(this)
            parser.setFeature("http://xml.org/sax/features/validation", true)
            parser.setFeature("http://apache.org/xml/features/validation/schema", true)
            parser.setFeature("http://apache.org/xml/features/validation/schema-full-checking", true)
            // if(!validateNamespace)
            if (!Util.isEmpty(strSchema)) parser.setProperty("http://apache.org/xml/properties/schema/external-noNamespaceSchemaLocation", strSchema)
            parser.parse(xml)
        } catch (e: SAXException) {
        } catch (e: IOException) {
            throw engine.getExceptionUtil().createXMLException(e.getMessage())
        }

        // result
        val result: Struct = engine.getCreationUtil().createStruct()
        result.setEL("warnings", warnings)
        result.setEL("errors", errors)
        result.setEL("fatalerrors", fatals)
        result.setEL("status", engine.getCastUtil().toBoolean(!hasErrors))
        release()
        return result
    }
}