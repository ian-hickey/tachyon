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
 * implementation of the form tag
 */
class Form : BodyTagImpl() {
    // private static int _count=0;
    private var count = 0
    private var name: String? = null
    private var action: String? = null
    private var preserveData = false
    /**
     * @return Returns the onsubmit.
     */
    /**
     * @param onsubmit The onsubmit to set.
     */
    var onsubmit: String? = null
    private var onreset: String? = null
    private var onload: String? = null
    private var passthrough: String? = null
    private var method: String? = "post"
    private var scriptSrc: String? = null
    var format = FORMAT_HTML
        private set
    private val attributes: Struct? = StructImpl()
    private val inputs: Map? = LinkedHashMap()
    private var strSkin: String? = null
    private var archive: String? = null

    /**
     * @param codebase The codebase to set.
     * @throws ApplicationException
     */
    var codebase: String? = null
    private var height: String? = "100%"
    private var width: String? = "100%"
    private var preloader = true
    private var timeout = 0
    private var wMode = WMODE_WINDOW
    private var accessible = false
    private var onError: String? = null
    @Override
    fun release() {
        super.release()
        name = null
        action = null
        preserveData = false
        attributes.clear()
        onsubmit = null
        onreset = null
        onload = null
        passthrough = null
        method = "post"
        scriptSrc = null
        strSkin = null
        archive = null
        codebase = null
        height = "100%"
        width = "100%"
        preloader = true
        timeout = 0
        wMode = WMODE_WINDOW
        accessible = false
        onError = null
        inputs.clear()
    }

    /**
     * @param enablecab The enablecab to set.
     * @throws ApplicationException
     */
    fun setEnablecab(enablecab: Boolean) {
        // DeprecatedUtil.tagAttribute(pageContext,"Form", "enablecab");
    }

    /**
     * @param method The method to set.
     * @throws ApplicationException
     */
    @Throws(ApplicationException::class)
    fun setMethod(method: String?) {
        var method = method
        method = method.trim().toLowerCase()
        if (method.equals("get") || method.equals("post")) this.method = method else throw ApplicationException("invalid value for attribute method from tag form, attribute can have value [get,post] but now is [$method]")
    }

    /**
     * @param format the format to set
     * @throws ApplicationException
     */
    @Throws(ApplicationException::class)
    fun setFormat(strFormat: String?) {
        var strFormat = strFormat
        strFormat = strFormat.trim().toLowerCase()
        format = if ("html".equals(strFormat)) FORMAT_HTML else if ("xml".equals(strFormat)) FORMAT_XML else if ("flash".equals(strFormat)) FORMAT_FLASH else throw ApplicationException(
                "invalid value [$strFormat] for attribute format, for this attribute only the following values are supported [xml, html, flash]")
        if (format != FORMAT_HTML) throw ApplicationException("format [$strFormat] is not supported, only the following formats are supported [html]")
        // TODO support other formats
    }

    /**
     * @param skin The skin to set.
     */
    fun setSkin(strSkin: String?) {
        this.strSkin = strSkin
    }

    /**
     * @param action The action to set.
     */
    fun setAction(action: String?) {
        this.action = action
    }

    /**
     * @param scriptSrc The scriptSrc to set.
     */
    fun setScriptsrc(scriptSrc: String?) {
        this.scriptSrc = scriptSrc
    }

    /**
     * @param archive The archive to set.
     * @throws ApplicationException
     */
    fun setArchive(archive: String?) {
        var archive = archive
        archive = archive.trim().toLowerCase().replace('\\', '/')
        if (!StringUtil.startsWith(archive, '/')) archive = "/$archive"
        this.archive = archive
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
     * @param enctype The enctype to set.
     */
    fun setEnctype(enctype: String?) {
        attributes.setEL(KeyConstants._enctype, enctype)
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

    fun setAcceptcharset(accept_charset: String?) {
        attributes.setEL("accept-charset", accept_charset)
    }

    fun setAccept_charset(accept_charset: String?) {
        attributes.setEL("accept-charset", accept_charset)
    }

    /**
     * @param name The name to set.
     * @throws ApplicationException
     */
    @Throws(ApplicationException::class)
    fun setName(name: String?) {
        this.name = name
        checkName(name)
    }

    /**
     * @param onreset The onreset to set.
     */
    fun setOnreset(onreset: String?) {
        this.onreset = onreset
    }

    /**
     * @param onreset The onreset to set.
     */
    fun setOnload(onload: String?) {
        this.onload = onload
    }

    fun setOnerror(onError: String?) {
        this.onError = onError
    }

    fun setOnclick(onclick: String?) {
        attributes.setEL("onclick", onclick)
    }

    fun setOndblclick(ondblclick: String?) {
        attributes.setEL("ondblclick", ondblclick)
    }

    fun setOnmousedown(onmousedown: String?) {
        attributes.setEL("onmousedown", onmousedown)
    }

    fun setOnmouseup(onmouseup: String?) {
        attributes.setEL("onmouseup", onmouseup)
    }

    fun setOnmouseover(onmouseover: String?) {
        attributes.setEL("onmouseover", onmouseover)
    }

    fun setOnmousemove(onmousemove: String?) {
        attributes.setEL("onmousemove", onmousemove)
    }

    fun setOnmouseout(onmouseout: String?) {
        attributes.setEL("onmouseout", onmouseout)
    }

    fun setOnkeypress(onkeypress: String?) {
        attributes.setEL("onkeypress", onkeypress)
    }

    fun setOnkeydown(onkeydown: String?) {
        attributes.setEL("onkeydown", onkeydown)
    }

    fun setOnkeyup(onkeyup: String?) {
        attributes.setEL("onkeyup", onkeyup)
    }

    /**
     * @param passthrough The passthrough to set.
     * @throws PageException
     */
    @Throws(PageException::class)
    fun setPassthrough(passthrough: Object?) {
        if (passthrough is Struct) {
            val sct: Struct? = passthrough as Struct?
            // lucee.runtime.type.Collection.Key[] keys=sct.keys();
            // lucee.runtime.type.Collection.Key key;
            val it: Iterator<Entry<Key?, Object?>?> = sct.entryIterator()
            var e: Entry<Key?, Object?>?
            while (it.hasNext()) {
                e = it.next()
                attributes.setEL(e.getKey(), e.getValue())
            }
        } else this.passthrough = Caster.toString(passthrough)
    }

    /**
     * @param preserveData The preserveData to set.
     * @throws ApplicationException
     */
    @Throws(ApplicationException::class)
    fun setPreservedata(preserveData: Boolean) {
        // this.preserveData = preserveData;
        if (preserveData) throw ApplicationException("attribute preserveData for tag form is not supported at the moment")
    }

    /**
     * @param target The target to set.
     */
    fun setTarget(target: String?) {
        attributes.setEL(KeyConstants._target, target)
    }

    fun setTitle(title: String?) {
        attributes.setEL(KeyConstants._title, title)
    }

    fun setDir(dir: String?) {
        attributes.setEL(KeyConstants._dir, dir)
    }

    fun setLang(lang: String?) {
        attributes.setEL(KeyConstants._lang, lang)
    }

    /**
     * @param height the height to set
     */
    fun setHeight(height: String?) {
        this.height = height
    }

    /**
     * @param width the width to set
     */
    fun setWidth(width: String?) {
        this.width = width
    }

    /**
     * @param preloader the preloader to set
     */
    fun setPreloader(preloader: Boolean) {
        this.preloader = preloader
    }

    /**
     * @param timeout the timeout to set
     */
    fun setTimeout(timeout: Double) {
        this.timeout = timeout.toInt()
    }

    /**
     * @param strWMode the wmode to set
     * @throws ApplicationException
     */
    @Throws(ApplicationException::class)
    fun setWmode(strWMode: String?) {
        var strWMode = strWMode
        strWMode = strWMode.toLowerCase().trim()
        wMode = if ("window".equals(strWMode)) WMODE_WINDOW else if ("transparent".equals(strWMode)) WMODE_TRANSPARENT else if ("opaque".equals(strWMode)) WMODE_OPAQUE else throw ApplicationException(
                "invalid value [$strWMode] for attribute wmode, for this attribute only the following values are supported [window, transparent, opaque]")
    }

    /**
     * @param strWMode the wmode to set
     */
    fun setAccessible(accessible: Boolean) {
        this.accessible = accessible
    }

    @Override
    @Throws(PageException::class)
    fun doStartTag(): Int {
        return try {
            _doStartTag()
        } catch (e: IOException) {
            throw Caster.toPageException(e)
        }
    }

    @Throws(PageException::class, IOException::class)
    private fun _doStartTag(): Int {
        var contextPath: String = pageContext.getHttpServletRequest().getContextPath()
        if (contextPath == null) contextPath = ""
        count = IDGenerator.intId()
        if (name == null) {
            name = "CFForm_$count"
        }
        attributes.setEL(KeyConstants._name, name)
        if (action == null) action = ReqRspUtil.self(pageContext.getHttpServletRequest())
        attributes.setEL(KeyConstants._action, action)
        val suffix = if (StringUtil.isEmpty(name)) "" + count else StringUtil.toVariableName(name)
        val funcName = "lucee_form_$count"
        val checkName = "_CF_check$suffix"
        val resetName = "_CF_reset$suffix"
        val loadName = "_CF_load$suffix"

        // boolean hasListener=false;
        if (onsubmit == null) attributes.setEL("onsubmit", "return $funcName.check();") else {
            attributes.setEL("onsubmit", "return $checkName();")
            // hasListener=true;
        }
        if (onreset != null) {
            attributes.setEL("onreset", "$resetName();")
            // hasListener=true;
        }
        if (onload != null) {
            attributes.setEL("onload", "$loadName();")
            // hasListener=true;
        }
        if (scriptSrc == null) scriptSrc = "$contextPath/lucee/form.cfm"
        attributes.setEL(KeyConstants._method, method)
        pageContext.forceWrite("<script language = \"JavaScript\" type=\"text/javascript\" src=\"$scriptSrc\"></script>")
        // if(hasListener) {
        pageContext.forceWrite("<script language = \"JavaScript\" type=\"text/javascript\">\n")
        if (onsubmit != null) pageContext.forceWrite("function $checkName() { if($funcName.check()){$onsubmit\nreturn true;}else {return false;}}\n") else pageContext.forceWrite("function $checkName() { return $funcName.check();}\n")
        if (onreset != null) pageContext.forceWrite("function $resetName() {$onreset\n}\n")
        if (onload != null) pageContext.forceWrite("function $loadName() {$onload\n}\n")
        pageContext.forceWrite("\n</script>")

        // }
        pageContext.forceWrite("<form")
        val it: Iterator<Entry<Key?, Object?>?> = attributes.entryIterator()
        var e: Entry<Key?, Object?>?
        while (it.hasNext()) {
            e = it.next()
            pageContext.forceWrite(" ")
            pageContext.forceWrite(e.getKey().getString())
            pageContext.forceWrite("=")
            pageContext.forceWrite(de(Caster.toString(e.getValue())))
        }
        if (passthrough != null) {
            pageContext.forceWrite(" ")
            pageContext.forceWrite(passthrough)
        }
        pageContext.forceWrite(">")
        return EVAL_BODY_INCLUDE
    }

    @Override
    @Throws(PageException::class)
    fun doEndTag(): Int {
        val funcName = "lucee_form_$count"
        try {
            pageContext.forceWrite("</form><!-- name:$name --><script>\n")
            pageContext.forceWrite("""
    $funcName=new LuceeForms(${js(name)},${js(onError)});
    
    """.trimIndent())
            val it: Iterator = inputs.keySet().iterator()
            while (it.hasNext()) {
                val input: InputBean = inputs.get(it.next())
                pageContext.forceWrite("""
    $funcName.addInput(${js(input.getName())},${input!!.isRequired()},${input.getType()},${input.getValidate()},${input.getPattern()},${js(input.getMessage())},${js(input.getOnError())},${js(input.getOnValidate())},${range(input.getRangeMin())},${range(input.getRangeMax())},${input.getMaxLength()});
    
    """.trimIndent())
            }
            pageContext.forceWrite("</script>")
        } catch (e: IOException) {
            throw Caster.toPageException(e)
        }
        return EVAL_PAGE
    }

    private fun range(range: Double): String? {
        return if (!Decision.isValid(range)) "null" else Caster.toString(range)
    }

    @Throws(PageException::class)
    private fun de(str: String?): String? {
        return DE.call(pageContext, str)
    }

    private fun js(str: String?): String? {
        return if (str == null) "null" else "'" + JSStringFormat.call(pageContext, str).toString() + "'"
    }

    /**
     * @param input
     * @throws ApplicationException
     */
    @Throws(ApplicationException::class)
    fun setInput(input: InputBean?) {
        if (input.getType() === Input.TYPE_TEXT || input.getType() === Input.TYPE_PASSWORD) {
            val i: InputBean = inputs.get(input.getName().toLowerCase())
            if (i != null && (i.getType() === Input.TYPE_TEXT || i.getType() === Input.TYPE_PASSWORD)) {
                throw ApplicationException("duplicate input field [" + i.getName().toString() + "] for form", "a text or password field must be unique")
            }
        }

        // if(StringUtil.isEmpty(input.getOnError(),true) && !StringUtil.isEmpty(onError,true))
        // input.setOnError(onError);
        inputs.put(input.getName().toLowerCase(), input)
    }

    /**
     * @return Returns the name.
     */
    fun getName(): String? {
        return name
    }

    fun getArchive(): String? {
        return archive
    }

    companion object {
        const val FORMAT_HTML = 0
        const val FORMAT_FLASH = 1
        const val FORMAT_XML = 2
        private val DEFAULT_ARCHIVE: String? = ""
        private const val WMODE_WINDOW = 0
        private const val WMODE_TRANSPARENT = 1
        private const val WMODE_OPAQUE = 2
        @Throws(ApplicationException::class)
        private fun checkName(name: String?) {
            if (name!!.length() === 0) return
            val len: Int = name!!.length()
            for (pos in 0 until len) {
                val c: Char = name.charAt(pos)
                if (c >= 'a' && c <= 'z' || c >= 'A' && c <= 'Z' || c >= '0' && c <= '9' || c == '_' || c == '-' || c == ':' || c == '.') continue
                throw ApplicationException("value of attribute name [$name] is invalid, only the following characters are allowed [a-z,A-Z,0-9,-,_,:,.]")
            }
        }
    }
}