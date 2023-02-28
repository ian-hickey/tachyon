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
package org.apache.taglibs.datetime

import java.text.DateFormatSymbols

class FormatTag : BodyTagSupport() {
    // format tag attributes
    // Optional attribute, use users locale if known when formatting date
    private var locale_flag = false

    // Optional attribute, time pattern string to use when formatting date
    private var pattern: String? = null

    // Optional attribute, name of script variable to use as pattern
    private var patternid: String? = null

    // Optional attribute, timeZone script variable id to use when formatting date
    private var timeZone_string: String? = null

    // Optional attribute, date object from rtexprvalue
    private var date: Date? = null

    // Optional attribute, the default text if the tag body or date given is invalid/null
    private var default_text = "Invalid Date"

    // Optional attribute, the name of an attribute which contains the Locale
    private var localeRef: String? = null

    // Optional attribute, name of script variable to use as date symbols source
    private var symbolsRef: String? = null

    // format tag invocation variables
    // The symbols object
    private var symbols: DateFormatSymbols? = null

    // The date to be formatted an output by tag
    private var output_date: Date? = null

    /**
     * Method called at start of tag, always returns EVAL_BODY_TAG
     *
     * @return EVAL_BODY_TAG
     */
    @Override
    @Throws(JspException::class)
    fun doStartTag(): Int {
        output_date = date
        return EVAL_BODY_TAG
    }

    /**
     * Method called at end of format tag body.
     *
     * @return SKIP_BODY
     */
    @Override
    @Throws(JspException::class)
    fun doAfterBody(): Int {
        // Use the body of the tag as input for the date
        val body: BodyContent = getBodyContent()
        val s: String = body.getString().trim()
        // Clear the body since we will output only the formatted date
        body.clearBody()
        if (output_date == null) {
            val time: Long
            try {
                time = Long.valueOf(s).longValue()
                output_date = Date(time)
            } catch (nfe: NumberFormatException) {
            }
        }
        return SKIP_BODY
    }

    /**
     * Method called at end of Tag
     *
     * @return EVAL_PAGE
     */
    @Override
    @Throws(JspException::class)
    fun doEndTag(): Int {
        var date_formatted = default_text
        if (output_date != null) {
            // Get the pattern to use
            var sdf: SimpleDateFormat
            var pat = pattern
            if (pat == null && patternid != null) {
                val attr: Object = pageContext.findAttribute(patternid)
                if (attr != null) pat = attr.toString()
            }
            if (pat == null) {
                sdf = SimpleDateFormat()
                pat = sdf.toPattern()
            }

            // Get a DateFormatSymbols
            if (symbolsRef != null) {
                symbols = pageContext.findAttribute(symbolsRef) as DateFormatSymbols
                if (symbols == null) {
                    throw JspException("datetime format tag could not find dateFormatSymbols for symbolsRef \"$symbolsRef\".")
                }
            }

            // Get a SimpleDateFormat using locale if necessary
            if (localeRef != null) {
                val locale: Locale = pageContext.findAttribute(localeRef) as Locale
                        ?: throw JspException("datetime format tag could not find locale for localeRef \"$localeRef\".")
                sdf = SimpleDateFormat(pat, locale)
            } else if (locale_flag) {
                sdf = SimpleDateFormat(pat, pageContext.getRequest().getLocale())
            } else if (symbols != null) {
                sdf = SimpleDateFormat(pat, symbols)
            } else {
                sdf = SimpleDateFormat(pat)
            }

            // See if there is a timeZone
            if (timeZone_string != null) {
                val timeZone: TimeZone = pageContext.getAttribute(timeZone_string, PageContext.SESSION_SCOPE) as TimeZone
                        ?: throw JspTagException("Datetime format tag timeZone script variable \"$timeZone_string \" does not exist")
                sdf.setTimeZone(timeZone)
            }

            // Format the date for display
            date_formatted = sdf.format(output_date)
        }
        try {
            pageContext.getOut().write(date_formatted)
        } catch (e: Exception) {
            throw JspException("IO Error: " + e.getMessage())
        }
        return EVAL_PAGE
    }

    @Override
    fun release() {
        // tachyon.print.ln("release FormatTag");
        super.release()
        locale_flag = false
        pattern = null
        patternid = null
        date = null
        localeRef = null
        symbolsRef = null
        symbols = null
    }

    /**
     * Locale flag, if set to true, format date for client's preferred locale if known.
     *
     * @param boolean use users locale, true or false
     */
    fun setLocale(flag: Short) {
        // locale_flag = flag;
    }

    /**
     * Set the time zone to use when formatting date.
     *
     * Value must be the name of a **timeZone** tag script variable ID.
     *
     * @param String name of timeZone to use
     */
    fun setTimeZone(tz: String?) {
        timeZone_string = tz
    }

    /**
     * Set the pattern to use when formatting Date.
     *
     * @param String SimpleDateFormat style time pattern format string
     */
    fun setPattern(str: String?) {
        pattern = str
    }

    /**
     * Set the pattern to use when parsing Date using a script variable attribute.
     *
     * @param String name of script variable attribute id
     */
    fun setPatternId(str: String?) {
        patternid = str
    }

    /**
     * Set the date to use (overrides tag body) for formatting
     *
     * @param Date to use for formatting (could be null)
     */
    fun setDate(date: Date?) {
        this.date = date
    }

    /**
     * Set the default text if an invalid date or no tag body is given
     *
     * @param String to use as default text
     */
    fun setDefault(default_text: String) {
        this.default_text = default_text
    }

    /**
     * Provides a key to search the page context for in order to get the java.util.Locale to use.
     *
     * @param String name of locale attribute to use
     */
    fun setLocaleRef(value: String?) {
        localeRef = value
    }

    /**
     * Provides a key to search the page context for in order to get the java.text.DateFormatSymbols to
     * use
     *
     * @param symbolsRef
     */
    fun setSymbolsRef(symbolsRef: String?) {
        this.symbolsRef = symbolsRef
    }
}