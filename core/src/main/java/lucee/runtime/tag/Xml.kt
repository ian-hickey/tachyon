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
package lucee.runtime.tag

import java.io.StringReader

/**
 * Creates a XML document object that contains the markup in the tag body. This tag can include XML
 * and CFML tags. the engine processes the CFML code in the tag body, then assigns the resulting
 * text to an XML document object variable.
 *
 *
 *
 */
class Xml : BodyTagImpl() {
    /** name of an xml variable  */
    private var variable: String? = null
    private var validator: String? = null

    /** yes: maintains the case of document elements and attributes  */
    private var casesensitive = false
    private var strXML: String? = null
    private var lenient = false
    @Override
    fun release() {
        super.release()
        variable = null
        casesensitive = false
        strXML = null
        validator = null
        lenient = false
    }

    /**
     * set the value variable name of an xml variable
     *
     * @param variable value to set
     */
    fun setVariable(variable: String?) {
        this.variable = variable
    }

    /**
     * set the value casesensitive yes: maintains the case of document elements and attributes
     *
     * @param casesensitive value to set
     */
    fun setCasesensitive(casesensitive: Boolean) {
        this.casesensitive = casesensitive
    }

    fun setLenient(lenient: Boolean) {
        this.lenient = lenient
    }

    @Override
    fun doStartTag(): Int {
        return EVAL_BODY_BUFFERED
    }

    @Override
    @Throws(PageException::class)
    fun doEndTag(): Int {
        try {
            val vis: InputSource? = if (StringUtil.isEmpty(validator)) null else XMLUtil.toInputSource(pageContext, validator)
            pageContext.setVariable(variable, XMLCaster.toXMLStruct(XMLUtil.parse(InputSource(StringReader(strXML)), vis, lenient), casesensitive))
        } catch (e: Exception) {
            throw Caster.toPageException(e)
        }
        return EVAL_PAGE
    }

    @Override
    fun doInitBody() {
    }

    @Override
    fun doAfterBody(): Int {
        strXML = bodyContent.getString().trim()
        return SKIP_BODY
    }

    /**
     * @param validator the validator to set
     */
    fun setValidator(validator: String?) {
        this.validator = validator
    }
}