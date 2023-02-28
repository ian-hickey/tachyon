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
package tachyon.runtime.tag

import java.io.IOException

// FUTURE tag input 
//attr validateAt impl tag atrr
//attr validate add support for submitOnce
// Added support for generating Flash and XML controls (specified in the cfform tag).
// Added support for preventing multiple submissions.
// attr mask impl. logik dahinter umsetzen
/**
 *
 */
class Input : TagImpl() {
    // TODO SubmitOnce
    /**
     * @param validate The validate to set.
     * @throws ApplicationException
     */
    @Throws(ApplicationException::class)
    fun setValidate(validate: String?) {
        var validate = validate
        validate = validate.toLowerCase().trim()
        if (validate.equals("creditcard")) input.setValidate(VALIDATE_CREDITCARD) else if (validate.equals("date")) input.setValidate(VALIDATE_DATE) else if (validate.equals("usdate")) input.setValidate(VALIDATE_USDATE) else if (validate.equals("eurodate")) input.setValidate(VALIDATE_EURODATE) else if (validate.equals("float")) input.setValidate(VALIDATE_FLOAT) else if (validate.equals("numeric")) input.setValidate(VALIDATE_FLOAT) else if (validate.equals("integer")) input.setValidate(VALIDATE_INTEGER) else if (validate.equals("int")) input.setValidate(VALIDATE_INTEGER) else if (validate.equals("regular_expression")) input.setValidate(VALIDATE_REGULAR_EXPRESSION) else if (validate.equals("regex")) input.setValidate(VALIDATE_REGULAR_EXPRESSION) else if (validate.equals("social_security_number")) input.setValidate(VALIDATE_SOCIAL_SECURITY_NUMBER) else if (validate.equals("ssn")) input.setValidate(VALIDATE_SOCIAL_SECURITY_NUMBER) else if (validate.equals("telephone")) input.setValidate(VALIDATE_TELEPHONE) else if (validate.equals("phone")) input.setValidate(VALIDATE_TELEPHONE) else if (validate.equals("time")) input.setValidate(VALIDATE_TIME) else if (validate.equals("zipcode")) input.setValidate(VALIDATE_ZIPCODE) else if (validate.equals("zip")) input.setValidate(VALIDATE_ZIPCODE) else if (validate.equals("range")) input.setValidate(VALIDATE_RANGE) else if (validate.equals("boolean")) input.setValidate(VALIDATE_BOOLEAN) else if (validate.equals("email")) input.setValidate(VALIDATE_EMAIL) else if (validate.equals("url")) input.setValidate(VALIDATE_URL) else if (validate.equals("uuid")) input.setValidate(VALIDATE_UUID) else if (validate.equals("guid")) input.setValidate(VALIDATE_GUID) else if (validate.equals("maxlength")) input.setValidate(VALIDATE_MAXLENGTH) else if (validate.equals("noblanks")) input.setValidate(VALIDATE_NOBLANKS) else throw ApplicationException("attribute validate has an invalid value [$validate]",
                "valid values for attribute validate are [creditcard, date, eurodate, float, integer, regular, social_security_number, telephone, time, zipcode]")
    }

    var attributes: Struct? = StructImpl()
    var input: InputBean? = InputBean()
    var passthrough: String? = null
    var daynames = DAYNAMES_DEFAULT

    /**
     * @return the monthnames
     */
    var monthnames = MONTHNAMES_DEFAULT
    var enabled = true
    var visible = true
    var label: String? = null
    var tooltip: String? = null
    var validateAt: String? = null
    var firstDayOfWeek = 0.0
    var mask: String? = null
    var encodeValue = true
    @Override
    fun release() {
        super.release()
        input = InputBean()
        attributes.clear()
        passthrough = null
        daynames = DAYNAMES_DEFAULT
        monthnames = MONTHNAMES_DEFAULT
        enabled = true
        visible = true
        label = null
        tooltip = null
        validateAt = null
        firstDayOfWeek = 0.0
        mask = null
        encodeValue = true
    }

    /**
     * @param cssclass The cssclass to set.
     */
    fun setClass(cssclass: String?) {
        attributes.setEL(KeyConstants._class, cssclass)
    }

    /**
     * @param cssstyle The cssstyle to set.
     */
    fun setStyle(cssstyle: String?) {
        attributes.setEL(KeyConstants._style, cssstyle)
    }

    /**
     * @param id The id to set.
     */
    fun setId(id: String?) {
        attributes.setEL(KeyConstants._id, id)
    }

    fun setAccept(accept: String?) {
        attributes.setEL(KeyConstants._accept, accept)
    }

    fun setAccesskey(accesskey: String?) {
        attributes.setEL("accesskey", accesskey)
    }

    fun setAlign(align: String?) {
        attributes.setEL(KeyConstants._align, align)
    }

    fun setAlt(alt: String?) {
        attributes.setEL(KeyConstants._alt, alt)
    }

    fun setAutocomplete(autocomplete: String?) {
        attributes.setEL("autocomplete", autocomplete)
    }

    fun setAutofocus(autofocus: String?) {
        attributes.setEL("autofocus", autofocus)
    }

    fun setBorder(border: String?) {
        attributes.setEL(KeyConstants._border, border)
    }

    fun setDatafld(datafld: String?) {
        attributes.setEL("datafld", datafld)
    }

    fun setDatasrc(datasrc: String?) {
        attributes.setEL("datasrc", datasrc)
    }

    fun setForm(form: String?) {
        attributes.setEL(KeyConstants._form, form)
    }

    fun setFormaction(formAction: String?) {
        attributes.setEL("formaction", formAction)
    }

    fun setFormenctype(formenctype: String?) {
        attributes.setEL("formenctype", formenctype)
    }

    fun setFormmethod(formmethod: String?) {
        attributes.setEL("formmethod", formmethod)
    }

    fun setFormnovalidate(formnovalidate: String?) {
        attributes.setEL("formnovalidate", formnovalidate)
    }

    fun setFormtarget(formtarget: String?) {
        attributes.setEL("formtarget", formtarget)
    }

    fun setLang(lang: String?) {
        attributes.setEL(KeyConstants._lang, lang)
    }

    fun setList(list: String?) {
        attributes.setEL(KeyConstants._list, list)
    }

    fun setDir(dir: String?) {
        // dir=dir.trim();
        // String lcDir=dir.toLowerCase();
        // if( "ltr".equals(lcDir) || "rtl".equals(lcDir))
        attributes.setEL(KeyConstants._dir, dir)

        // else throw new ApplicationException("attribute dir for tag input has an invalid value ["+dir+"],
        // valid values are [ltr, rtl]");
    }

    fun setDataformatas(dataformatas: String?) {
        var dataformatas = dataformatas
        dataformatas = dataformatas.trim()
        // String lcDataformatas=dataformatas.toLowerCase();
        // if( "plaintext".equals(lcDataformatas) || "html".equals(lcDataformatas))
        attributes.setEL("dataformatas", dataformatas)

        // else throw new ApplicationException("attribute dataformatas for tag input has an invalid value
        // ["+dataformatas+"], valid values are [plaintext, html");
    }

    fun setDisabled(disabled: String?) {
        // alles ausser false ist true
        // if(Caster.toBooleanValue(disabled,true))
        attributes.setEL("disabled", disabled)
    }

    fun setEnabled(enabled: String?) {
        // alles ausser false ist true
        // setDisabled(Caster.toString(!Caster.toBooleanValue(enabled,true)));
        attributes.setEL("enabled", enabled)
    }

    fun setIsmap(ismap: String?) {
        // alles ausser false ist true
        // if(Caster.toBooleanValue(ismap,true)) attributes.setEL("ismap","ismap");
        attributes.setEL("ismap", ismap)
    }

    fun setReadonly(readonly: String?) {
        // alles ausser false ist true
        // if(Caster.toBooleanValue(readonly,true)) attributes.setEL("readonly","readonly");
        attributes.setEL(KeyConstants._readonly, readonly)
    }

    fun setUsemap(usemap: String?) {
        attributes.setEL("usemap", usemap)
    }

    /**
     * @param onBlur The onBlur to set.
     */
    fun setOnblur(onBlur: String?) {
        attributes.setEL("onblur", onBlur)
    }

    /**
     * @param onChange The onChange to set.
     */
    fun setOnchange(onChange: String?) {
        attributes.setEL("onchange", onChange)
    }

    /**
     * @param onClick The onClick to set.
     */
    fun setOnclick(onClick: String?) {
        attributes.setEL("onclick", onClick)
    }

    /**
     * @param onDblclick The onDblclick to set.
     */
    fun setOndblclick(onDblclick: String?) {
        attributes.setEL("ondblclick", onDblclick)
    }

    /**
     * @param onFocus The onFocus to set.
     */
    fun setOnfocus(onFocus: String?) {
        attributes.setEL("onfocus", onFocus)
    }

    /**
     * @param onKeyDown The onKeyDown to set.
     */
    fun setOnkeydown(onKeyDown: String?) {
        attributes.setEL("onkeydown", onKeyDown)
    }

    /**
     * @param onKeyPress The onKeyPress to set.
     */
    fun setOnkeypress(onKeyPress: String?) {
        attributes.setEL("onkeypress", onKeyPress)
    }

    /**
     * @param onKeyUp The onKeyUp to set.
     */
    fun setOnkeyup(onKeyUp: String?) {
        attributes.setEL("onKeyUp", onKeyUp)
    }

    /**
     * @param onMouseDown The onMouseDown to set.
     */
    fun setOnmousedown(onMouseDown: String?) {
        attributes.setEL("onMouseDown", onMouseDown)
    }

    /**
     * @param onMouseMove The onMouseMove to set.
     */
    fun setOnmousemove(onMouseMove: String?) {
        attributes.setEL("onMouseMove", onMouseMove)
    }

    /**
     * @param onMouseUp The onMouseUp to set.
     */
    fun setOnmouseup(onMouseUp: String?) {
        attributes.setEL("onMouseUp", onMouseUp)
    }

    /**
     * @param onMouseUp The onMouseUp to set.
     */
    fun setOnselect(onselect: String?) {
        attributes.setEL("onselect", onselect)
    }

    /**
     * @param onMouseOut The onMouseOut to set.
     */
    fun setOnmouseout(onMouseOut: String?) {
        attributes.setEL("onMouseOut", onMouseOut)
    }

    /**
     * @param onMouseOver The onKeyPress to set.
     */
    fun setOnmouseover(onMouseOver: String?) {
        attributes.setEL("onMouseOver", onMouseOver)
    }

    /**
     * @param tabIndex The tabIndex to set.
     */
    fun setTabindex(tabIndex: String?) {
        attributes.setEL("tabindex", tabIndex)
    }

    /**
     * @param title The title to set.
     */
    fun setTitle(title: String?) {
        attributes.setEL(KeyConstants._title, title)
    }

    /**
     * @param value The value to set.
     */
    fun setValue(value: String?) {
        attributes.setEL(KeyConstants._value, value)
    }

    /**
     * @param size The size to set.
     */
    fun setSize(size: String?) {
        attributes.setEL(KeyConstants._size, size)
    }

    /**
     * @param maxLength The maxLength to set.
     */
    fun setMaxlength(maxLength: Double) {
        input.setMaxLength(maxLength.toInt())
        attributes.setEL("maxLength", Caster.toString(maxLength))
    }

    /**
     * @param checked The checked to set.
     */
    fun setChecked(checked: String?) {
        // alles ausser false ist true
        if (Caster.toBooleanValue(checked, true)) attributes.setEL("checked", "checked")
    }

    /**
     * @param daynames The daynames to set.
     * @throws ApplicationException
     */
    @Throws(ApplicationException::class)
    fun setDaynames(listDaynames: String?) {
        val arr: Array<String?> = ListUtil.listToStringArray(listDaynames, ',')
        if (arr.size != 7) throw ApplicationException("value of attribute [daynames] must contain a string list with 7 values, now there are " + arr.size + " values")
        daynames = arr
    }

    /**
     * @param daynames The daynames to set.
     * @throws ApplicationException
     */
    @Throws(ApplicationException::class)
    fun setFirstdayofweek(firstDayOfWeek: Double) {
        if (firstDayOfWeek < 0 || firstDayOfWeek > 6) throw ApplicationException("value of attribute [firstDayOfWeek] must contain a numeric value between 0-6")
        this.firstDayOfWeek = firstDayOfWeek
    }

    /**
     * @param daynames The daynames to set.
     * @throws ApplicationException
     */
    @Throws(ApplicationException::class)
    fun setMonthnames(listMonthNames: String?) {
        val arr: Array<String?> = ListUtil.listToStringArray(listMonthNames, ',')
        if (arr.size == 12) throw ApplicationException("value of attribute [MonthNames] must contain a string list with 12 values, now there are " + arr.size + " values")
        monthnames = arr
    }

    /**
     * @param daynames The daynames to set.
     */
    fun setLabel(label: String?) {
        this.label = label
    }

    /**
     * @param daynames The daynames to set.
     */
    fun setMask(mask: String?) {
        this.mask = mask
    }

    fun setMax(max: String?) {
        attributes.setEL(KeyConstants._max, max)
    }

    fun setMin(min: String?) {
        attributes.setEL(KeyConstants._min, min)
    }

    fun setMultiple(multiple: String?) {
        attributes.setEL(KeyConstants._multiple, multiple)
    }

    fun setPlaceholder(placeholder: String?) {
        attributes.setEL("placeholder", placeholder)
    }

    /**
     * @param daynames The daynames to set.
     */
    fun setNotab(notab: String?) {
        attributes.setEL("notab", notab)
    }

    /**
     * @param daynames The daynames to set.
     */
    fun setHspace(hspace: String?) {
        attributes.setEL("hspace", hspace)
    }

    /**
     * @param type The type to set.
     * @throws ApplicationException
     */
    @Throws(ApplicationException::class)
    fun setType(type: String?) {
        var type = type
        type = type.toLowerCase().trim()
        if ("checkbox".equals(type)) input.setType(TYPE_CHECKBOX) else if ("password".equals(type)) input.setType(TYPE_PASSWORD) else if ("text".equals(type)) input.setType(TYPE_TEXT) else if ("radio".equals(type)) input.setType(TYPE_RADIO) else if ("button".equals(type)) input.setType(TYPE_BUTTON) else if ("file".equals(type)) input.setType(TYPE_FILE) else if ("hidden".equals(type)) input.setType(TYPE_HIDDEN) else if ("image".equals(type)) input.setType(TYPE_IMAGE) else if ("reset".equals(type)) input.setType(TYPE_RESET) else if ("submit".equals(type)) input.setType(TYPE_SUBMIT) else if ("datefield".equals(type)) input.setType(TYPE_DATEFIELD) else throw ApplicationException("attribute type has an invalid value [$type]",
                "valid values for attribute type are " + "[checkbox, password, text, radio, button, file, hidden, image, reset, submit, datefield]")
        attributes.setEL(KeyConstants._type, type)
    }

    /**
     * @param onError The onError to set.
     */
    fun setOnerror(onError: String?) {
        input.setOnError(onError)
    }

    /**
     * @param onValidate The onValidate to set.
     */
    fun setOnvalidate(onValidate: String?) {
        input.setOnValidate(onValidate)
    }

    /**
     * @param passthrough The passThrough to set.
     * @throws PageException
     */
    @Throws(PageException::class)
    fun setPassthrough(passthrough: Object?) {
        if (passthrough is Struct) {
            val sct: Struct? = passthrough as Struct?
            val it: Iterator<Entry<Key?, Object?>?> = sct.entryIterator()
            var e: Entry<Key?, Object?>?
            while (it.hasNext()) {
                e = it.next()
                attributes.setEL(e.getKey(), e.getValue())
            }
        } else this.passthrough = Caster.toString(passthrough)

        // input.setPassThrough(passThrough);
    }

    /**
     * @param pattern The pattern to set.
     * @throws ExpressionException
     */
    @Throws(ExpressionException::class)
    fun setPattern(pattern: String?) {
        input.setPattern(pattern)
    }

    /**
     * @param range The range to set.
     * @throws PageException
     */
    @Throws(PageException::class)
    fun setRange(range: String?) {
        val errMessage = "attribute range has an invalid value [$range], must be string list with numbers"
        val errDetail = "Example: [number_from,number_to], [number_from], [number_from,], [,number_to]"
        val arr: Array = ListUtil.listToArray(range, ',')
        if (arr.size() === 1) {
            val from: Double = Caster.toDoubleValue(arr.get(1, null), true, Double.NaN)
            if (!Decision.isValid(from)) throw ApplicationException(errMessage, errDetail)
            input.setRangeMin(from)
            input.setRangeMax(Double.NaN)
        } else if (arr.size() === 2) {
            val strFrom: String = arr.get(1, "").toString().trim()
            val from: Double = Caster.toDoubleValue(strFrom, Double.NaN)
            if (!Decision.isValid(from) && strFrom.length() > 0) {
                throw ApplicationException(errMessage, errDetail)
            }
            input.setRangeMin(from)
            val strTo: String = arr.get(2, "").toString().trim()
            val to: Double = Caster.toDoubleValue(strTo, Double.NaN)
            if (!Decision.isValid(to) && strTo.length() > 0) {
                throw ApplicationException(errMessage, errDetail)
            }
            input.setRangeMax(to)
        } else throw ApplicationException(errMessage, errDetail)
    }

    /**
     * @param required The required to set.
     */
    fun setRequired(required: Boolean) {
        input.setRequired(required)
    }

    /**
     * @param name The name to set.
     */
    fun setName(name: String?) {
        attributes.setEL(KeyConstants._name, name)
        input.setName(name)
    }

    /**
     * @param message The message to set.
     */
    fun setMessage(message: String?) {
        if (!StringUtil.isEmpty(message)) input.setMessage(message)
    }

    /**
     * @param encodeValue Encode value using HTMLEntities.escapeHTML, or allow using htmlEncodeForAttribute()
     */
    fun setEncodevalue(encodeValue: Boolean) {
        this.encodeValue = encodeValue
    }

    @Override
    @Throws(PageException::class)
    fun doEndTag(): Int {
        try {
            _doEndTag()
        } catch (e: IOException) {
            throw Caster.toPageException(e)
        }
        return EVAL_PAGE
    }

    @Throws(PageException::class, IOException::class)
    private fun _doEndTag() {
        // check attributes
        if (input.getValidate() === VALIDATE_REGULAR_EXPRESSION && input.getPattern() == null) {
            throw ApplicationException("when validation type regular_expression is selected, the pattern attribute is required")
        }
        var parent: Tag = getParent()
        while (parent != null && parent !is Form) {
            parent = parent.getParent()
        }
        if (parent is Form) {
            val form: Form = parent
            form!!.setInput(input)
            if (input.getType() === TYPE_DATEFIELD && form.getFormat() !== Form.FORMAT_FLASH) throw ApplicationException("type [datefield] is only allowed if form format is flash")
        } else {
            throw ApplicationException("Tag must be inside a form tag")
        }
        draw()
    }

    @Throws(IOException::class, PageException::class)
    fun draw() {

        // start output
        pageContext.forceWrite("<input")

        // tachyon.runtime.type.Collection.Key[] keys = attributes.keys();
        // tachyon.runtime.type.Collection.Key key;
        val it: Iterator<Entry<Key?, Object?>?> = attributes.entryIterator()
        var e: Entry<Key?, Object?>?
        while (it.hasNext()) {
            e = it.next()
            pageContext.forceWrite(" ")
            pageContext.forceWrite(e.getKey().getString())
            pageContext.forceWrite("=\"")
            pageContext.forceWrite(enc(Caster.toString(e.getValue())))
            pageContext.forceWrite("\"")
        }
        if (passthrough != null) {
            pageContext.forceWrite(" ")
            pageContext.forceWrite(passthrough)
        }
        pageContext.forceWrite(">")
    }

    /**
     * html encode a string
     *
     * @param str string to encode
     * @return encoded string
     */
    fun enc(str: String?): String? {
        return if (encodeValue) HTMLEntities.escapeHTML(str, HTMLEntities.HTMLV20) else str
    }

    /**
     * @param monthnames the monthnames to set
     */
    fun setMonthnames(monthnames: Array<String?>?) {
        this.monthnames = monthnames
    }

    /**
     * @param height the height to set
     */
    fun setHeight(height: String?) {
        attributes.setEL(KeyConstants._height, height)
    }

    /**
     * @param input the input to set
     */
    fun setInput(input: InputBean?) {
        this.input = input
    }

    /**
     * @param passthrough the passthrough to set
     */
    fun setPassthrough(passthrough: String?) {
        this.passthrough = passthrough
    }

    /**
     * @param tooltip the tooltip to set
     * @throws ApplicationException
     */
    fun setTooltip(tooltip: String?) {
        this.tooltip = tooltip
    }

    /**
     * @param validateAt the validateAt to set
     * @throws ApplicationException
     */
    @Throws(ApplicationException::class)
    fun setValidateat(validateAt: String?) {
        this.validateAt = validateAt
        throw ApplicationException("attribute [validateAt] is not supported for tag input ")
    }

    /**
     * @param visible the visible to set
     * @throws ApplicationException
     */
    fun setVisible(visible: Boolean) {
        this.visible = visible
    }

    /**
     * @param width the width to set
     * @throws ApplicationException
     */
    fun setWidth(width: String?) {
        attributes.setEL(KeyConstants._width, width)
    }

    private fun notSupported(label: String?): ExpressionException? {
        return ExpressionException("attribute [$label] is not supported")
    }

    @Throws(ExpressionException::class)
    fun setAutosuggest(autosuggest: String?) {
        throw notSupported("autosuggest")
        // attributes.setEL("bind",bind);
    }

    @Throws(ExpressionException::class)
    fun setAutosuggestbinddelay(autosuggestBindDelay: Double) {
        throw notSupported("autosuggestBindDelay")
        // attributes.setEL("bind",bind);
    }

    @Throws(ExpressionException::class)
    fun setAutosuggestminlength(autosuggestMinLength: Double) {
        throw notSupported("autosuggestMinLength")
        // attributes.setEL("bind",bind);
    }

    @Throws(ExpressionException::class)
    fun setBind(bind: String?) {
        throw notSupported("bind")
        // attributes.setEL("bind",bind);
    }

    @Throws(ExpressionException::class)
    fun setBindattribute(bindAttribute: String?) {
        throw notSupported("bindAttribute")
        // attributes.setEL("bind",bind);
    }

    @Throws(ExpressionException::class)
    fun setBindonload(bindOnLoad: Boolean) {
        throw notSupported("bindOnLoad")
        // attributes.setEL("bind",bind);
    }

    @Throws(ExpressionException::class)
    fun setDelimiter(delimiter: String?) {
        throw notSupported("delimiter")
        // attributes.setEL("bind",bind);
    }

    @Throws(ExpressionException::class)
    fun setMaxresultsdisplayed(maxResultsDisplayed: Double) {
        throw notSupported("maxResultsDisplayed")
        // attributes.setEL("bind",bind);
    }

    @Throws(ExpressionException::class)
    fun setOnbinderror(onBindError: String?) {
        throw notSupported("onBindError")
        // attributes.setEL("bind",bind);
    }

    @Throws(ExpressionException::class)
    fun setShowautosuggestloadingicon(showAutosuggestLoadingIcon: Boolean) {
        throw notSupported("showAutosuggestLoadingIcon")
        // attributes.setEL("bind",bind);
    }

    @Throws(ExpressionException::class)
    fun setSourcefortooltip(sourceForTooltip: String?) {
        throw notSupported("sourceForTooltip")
        // attributes.setEL("bind",bind);
    }

    fun setSrc(src: String?) {
        attributes.setEL(KeyConstants._src, src)
    }

    fun setStep(step: String?) {
        attributes.setEL(KeyConstants._step, step)
    }

    @Throws(ExpressionException::class)
    fun setTypeahead(typeahead: Boolean) {
        throw notSupported("typeahead")
        // attributes.setEL("src",src);
    }

    companion object {
        const val TYPE_SELECT: Short = -1
        const val TYPE_TEXT: Short = 0
        const val TYPE_RADIO: Short = 1
        const val TYPE_CHECKBOX: Short = 2
        const val TYPE_PASSWORD: Short = 3
        const val TYPE_BUTTON: Short = 4
        const val TYPE_FILE: Short = 5
        const val TYPE_HIDDEN: Short = 6
        const val TYPE_IMAGE: Short = 7
        const val TYPE_RESET: Short = 8
        const val TYPE_SUBMIT: Short = 9
        const val TYPE_DATEFIELD: Short = 10
        const val VALIDATE_DATE: Short = 4
        const val VALIDATE_EURODATE: Short = 5
        const val VALIDATE_TIME: Short = 6
        const val VALIDATE_FLOAT: Short = 7
        const val VALIDATE_INTEGER: Short = 8
        const val VALIDATE_TELEPHONE: Short = 9
        const val VALIDATE_ZIPCODE: Short = 10
        const val VALIDATE_CREDITCARD: Short = 11
        const val VALIDATE_SOCIAL_SECURITY_NUMBER: Short = 12
        const val VALIDATE_REGULAR_EXPRESSION: Short = 13
        const val VALIDATE_NONE: Short = 14
        const val VALIDATE_USDATE: Short = 15
        const val VALIDATE_RANGE: Short = 16
        const val VALIDATE_BOOLEAN: Short = 17
        const val VALIDATE_EMAIL: Short = 18
        const val VALIDATE_URL: Short = 19
        const val VALIDATE_UUID: Short = 20
        const val VALIDATE_GUID: Short = 21
        const val VALIDATE_MAXLENGTH: Short = 22
        const val VALIDATE_NOBLANKS: Short = 23
        val DAYNAMES_DEFAULT: Array<String?>? = arrayOf("S", "M", "T", "W", "Th", "F", "S")
        val MONTHNAMES_DEFAULT: Array<String?>? = arrayOf("January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November",
                "December")
    }
}