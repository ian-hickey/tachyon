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

import java.io.IOException

/**
 * Serializes and de-serializes CFML data structures to the XML-based WDDX format. Generates
 * JavaScript statements to instantiate JavaScript objects equivalent to the contents of a WDDX
 * packet or some CFML data structures.
 *
 *
 *
 */
class Wddx : TagImpl() {
    /** The value to be processed.  */
    private var input: Object? = null

    /** Specifies the action taken by the cfwddx tag.  */
    private var action: String? = null

    /**
     * The name of the variable to hold the output of the operation. This attribute is required for
     * action = 'WDDX2CFML'. For all other actions, if this attribute is not provided, the result of the
     * WDDX processing is outputted in the HTML stream.
     */
    private var output: String? = null
    private var validate = false

    /**
     * The name of the top-level JavaScript object created by the deserialization process. The object
     * created is an instance of the WddxRecordset object, explained in WddxRecordset Object.
     */
    private var toplevelvariable: String? = null

    /**
     * Indicates whether to output time-zone information when serializing CFML to WDDX. If time-zone
     * information is taken into account, the hour-minute offset, as represented in the ISO8601 format,
     * is calculated in the date-time output. If time-zone information is not taken into account, the
     * local time is output. The default is Yes.
     */
    private var usetimezoneinfo = false
    private var xmlConform = false
    @Override
    fun release() {
        super.release()
        input = null
        action = null
        output = null
        validate = false
        toplevelvariable = null
        usetimezoneinfo = false
        xmlConform = false
    }

    /**
     * set the value input The value to be processed.
     *
     * @param input value to set
     */
    fun setInput(input: Object?) {
        this.input = input
    }

    /**
     * set the value action Specifies the action taken by the cfwddx tag.
     *
     * @param action value to set
     */
    fun setAction(action: String?) {
        this.action = action.toLowerCase()
    }

    /**
     * set the value output The name of the variable to hold the output of the operation. This attribute
     * is required for action = 'WDDX2CFML'. For all other actions, if this attribute is not provided,
     * the result of the WDDX processing is outputted in the HTML stream.
     *
     * @param output value to set
     */
    fun setOutput(output: String?) {
        this.output = output
    }

    /**
     * set the value validate
     *
     * @param validate value to set
     */
    fun setValidate(validate: Boolean) {
        this.validate = validate
    }

    /**
     * set the value toplevelvariable The name of the top-level JavaScript object created by the
     * deserialization process. The object created is an instance of the WddxRecordset object, explained
     * in WddxRecordset Object.
     *
     * @param toplevelvariable value to set
     */
    fun setToplevelvariable(toplevelvariable: String?) {
        this.toplevelvariable = toplevelvariable
    }

    /**
     * set the value usetimezoneinfo Indicates whether to output time-zone information when serializing
     * CFML to WDDX. If time-zone information is taken into account, the hour-minute offset, as
     * represented in the ISO8601 format, is calculated in the date-time output. If time-zone
     * information is not taken into account, the local time is output. The default is Yes.
     *
     * @param usetimezoneinfo value to set
     */
    fun setUsetimezoneinfo(usetimezoneinfo: Boolean) {
        this.usetimezoneinfo = usetimezoneinfo
    }

    /**
     * sets if generated code is xml or wddx conform
     *
     * @param xmlConform
     */
    fun setXmlconform(xmlConform: Boolean) {
        this.xmlConform = xmlConform
    }

    @Override
    @Throws(PageException::class)
    fun doStartTag(): Int {
        try {
            doIt()
        } catch (e: Exception) {
            throw Caster.toPageException(e)
        }
        return SKIP_BODY
    }

    @Throws(ExpressionException::class, PageException::class, ConverterException::class, IOException::class, FactoryConfigurationError::class)
    private fun doIt() {
        // cfml > wddx
        if (action!!.equals("cfml2wddx")) {
            if (output != null) pageContext.setVariable(output, cfml2wddx(input)) else pageContext.forceWrite(cfml2wddx(input))
        } else if (action!!.equals("wddx2cfml")) {
            if (output == null) throw ApplicationException("at tag cfwddx the attribute output is required if you set action==wddx2cfml")
            pageContext.setVariable(output, wddx2cfml(Caster.toString(input)))
        } else if (action!!.equals("cfml2js")) {
            if (output != null) pageContext.setVariable(output, cfml2js(input)) else pageContext.forceWrite(cfml2js(input))
        } else if (action!!.equals("wddx2js")) {
            if (output != null) pageContext.setVariable(output, wddx2js(Caster.toString(input))) else pageContext.forceWrite(wddx2js(Caster.toString(input)))
        } else throw ExpressionException("invalid attribute action for tag cfwddx, attributes are [cfml2wddx, wddx2cfml,cfml2js, wddx2js].")
    }

    @Throws(ConverterException::class)
    private fun cfml2wddx(input: Object?): String? {
        val converter = WDDXConverter(pageContext.getTimeZone(), xmlConform, true)
        if (!usetimezoneinfo) converter.setTimeZone(null)
        return converter.serialize(input)
    }

    @Throws(ConverterException::class, IOException::class, FactoryConfigurationError::class)
    private fun wddx2cfml(input: String?): Object? {
        val converter = WDDXConverter(pageContext.getTimeZone(), xmlConform, true)
        converter.setTimeZone(pageContext.getTimeZone())
        return converter.deserialize(input, validate)
    }

    @Throws(ConverterException::class, ApplicationException::class)
    private fun cfml2js(input: Object?): String? {
        if (toplevelvariable == null) throw missingTopLevelVariable()
        val converter = JSConverter()
        return converter.serialize(input, toplevelvariable)
    }

    @Throws(ConverterException::class, IOException::class, FactoryConfigurationError::class, ApplicationException::class)
    private fun wddx2js(input: String?): String? {
        if (toplevelvariable == null) throw missingTopLevelVariable()
        val converter = JSConverter()
        return converter.serialize(wddx2cfml(input), toplevelvariable)
    }

    private fun missingTopLevelVariable(): ApplicationException? {
        return ApplicationException("at tag cfwddx the attribute topLevelVariable is required if you set action equal wddx2js or cfml2js")
    }

    @Override
    fun doEndTag(): Int {
        return EVAL_PAGE
    }
}