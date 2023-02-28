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

/**
 *
 */
class Select : BodyTagImpl() {
    private var query: tachyon.runtime.type.Query? = null
    private var selected: Array<String?>?
    private var value: String? = null
    private var display: String? = null
    private var passthrough: String? = null
    private val attributes: Struct? = StructImpl()
    private var input: InputBean? = InputBean()
    private var editable = false
    private var height = -1
    private var width = -1
    private var label: String? = null
    private var visible = true
    private var tooltip: String? = null
    private var group: String? = null
    private var queryPosition = QUERY_POSITION_ABOVE
    private var caseSensitive = false
    @Override
    fun release() {
        super.release()
        query = null
        selected = null
        value = null
        display = null
        passthrough = null
        editable = false
        height = -1
        width = -1
        label = null
        visible = true
        tooltip = null
        group = null
        queryPosition = QUERY_POSITION_ABOVE
        caseSensitive = false
        attributes.clear()
        input = InputBean()
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

    /**
     * @param multiple The multiple to set.
     */
    fun setMultiple(multiple: String?) {
        // alles ausser false ist true
        if (Caster.toBooleanValue(multiple, true)) attributes.setEL("multiple", "multiple")
    }

    /**
     * @param name The name to set.
     */
    fun setName(name: String?) {
        attributes.setEL(KeyConstants._name, name)
        input.setName(name)
    }

    /**
     * @param size The size to set.
     */
    fun setSize(size: Double) {
        attributes.setEL(KeyConstants._size, Caster.toString(size))
    }

    /**
     * @param tabindex The tabindex to set.
     */
    fun setTabindex(tabindex: String?) {
        attributes.setEL("tabindex", tabindex)
    }

    /**
     * @param title The title to set.
     */
    fun setTitle(title: String?) {
        attributes.setEL(KeyConstants._title, title)
    }

    /**
     * @param title The title to set.
     */
    fun setDir(dir: String?) {
        attributes.setEL(KeyConstants._dir, dir)
    }

    /**
     * @param title The title to set.
     */
    fun setLang(lang: String?) {
        attributes.setEL(KeyConstants._lang, lang)
    }

    /**
     * @param onblur The onblur to set.
     */
    fun setOnblur(onblur: String?) {
        attributes.setEL("onblur", onblur)
    }

    /**
     * @param onchange The onchange to set.
     */
    fun setOnchange(onchange: String?) {
        attributes.setEL("onchange", onchange)
    }

    /**
     * @param onclick The onclick to set.
     */
    fun setOnclick(onclick: String?) {
        attributes.setEL("onclick", onclick)
    }

    /**
     * @param ondblclick The ondblclick to set.
     */
    fun setOndblclick(ondblclick: String?) {
        attributes.setEL("ondblclick", ondblclick)
    }

    /**
     * @param onmousedown The onmousedown to set.
     */
    fun setOnmousedown(onmousedown: String?) {
        attributes.setEL("onmousedown", onmousedown)
    }

    /**
     * @param ondblclick The ondblclick to set.
     */
    fun setOnmouseup(onmouseup: String?) {
        attributes.setEL("onmouseup", onmouseup)
    }

    /**
     * @param ondblclick The ondblclick to set.
     */
    fun setOnmouseover(onmouseover: String?) {
        attributes.setEL("onmouseover", onmouseover)
    }

    /**
     * @param ondblclick The ondblclick to set.
     */
    fun setOnmousemove(onmousemove: String?) {
        attributes.setEL("onmousemove", onmousemove)
    }

    /**
     * @param ondblclick The ondblclick to set.
     */
    fun setOnmouseout(onmouseout: String?) {
        attributes.setEL("onmouseout", onmouseout)
    }

    /**
     * @param ondblclick The ondblclick to set.
     */
    fun setOnkeypress(onkeypress: String?) {
        attributes.setEL("onkeypress", onkeypress)
    }

    /**
     * @param ondblclick The ondblclick to set.
     */
    fun setOnkeydown(onkeydown: String?) {
        attributes.setEL("onkeydown", onkeydown)
    }

    /**
     * @param ondblclick The ondblclick to set.
     */
    fun setOnkeyup(onkeyup: String?) {
        attributes.setEL("onkeyup", onkeyup)
    }

    /**
     * @param onfocus The onfocus to set.
     */
    fun setOnfocus(onfocus: String?) {
        attributes.setEL("onfocus", onfocus)
    }

    /**
     * @param message The message to set.
     */
    fun setMessage(message: String?) {
        input.setMessage(message)
    }

    /**
     * @param onerror The onerror to set.
     */
    fun setOnerror(onerror: String?) {
        input.setOnError(onerror)
    }

    /**
     * @param required The required to set.
     */
    fun setRequired(required: Boolean) {
        input.setRequired(required)
    }

    /**
     * @param passthrough The passthrough to set.
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
    }

    /**
     * @param query The query to set.
     * @throws PageException
     */
    @Throws(PageException::class)
    fun setQuery(strQuery: String?) {
        query = Caster.toQuery(pageContext.getVariable(strQuery))
    }

    /**
     * @param display The display to set.
     */
    fun setDisplay(display: String?) {
        this.display = display
    }

    @Throws(ApplicationException::class)
    fun setDataformatas(dataformatas: String?) {
        var dataformatas = dataformatas
        dataformatas = dataformatas.trim()
        val lcDataformatas: String = dataformatas.toLowerCase()
        if ("plaintext".equals(lcDataformatas) || "html".equals(lcDataformatas)) {
            attributes.setEL("dataformatas", dataformatas)
        } else throw ApplicationException("attribute dataformatas for tag input has an invalid value [$dataformatas], valid values are [plaintext, html")
    }

    fun setDatafld(datafld: String?) {
        attributes.setEL("datafld", datafld)
    }

    fun setDatasrc(datasrc: String?) {
        attributes.setEL("datasrc", datasrc)
    }

    fun setDisabled(disabled: String?) {
        // alles ausser false ist true
        if (Caster.toBooleanValue(disabled, true)) setDisabled(true)
    }

    private fun setDisabled(disabled: Boolean) {
        if (disabled) attributes.setEL(KeyConstants._disabled, "disabled")
    }

    /**
     * @param selected The selected to set.
     */
    fun setSelected(selected: String?) {
        this.selected = ListUtil.trimItems(ListUtil.listToStringArray(selected, ','))
    }

    /**
     * @param value The value to set.
     */
    fun setValue(value: String?) {
        this.value = value
    }

    @Override
    fun doStartTag(): Int {
        return EVAL_BODY_BUFFERED
    }

    @Override
    @Throws(PageException::class)
    fun doEndTag(): Int {
        return try {
            _doEndTag()
            EVAL_PAGE
        } catch (e: IOException) {
            throw Caster.toPageException(e)
        }
    }

    @Throws(IOException::class, ExpressionException::class, PageException::class)
    private fun _doEndTag() {

        // check
        if (query != null) {
            if (value == null) throw ApplicationException("if you have defined attribute query for tag select, you must also define attribute value") else if (!query.containsKey(value)) throw ApplicationException("invalid value for attribute [value], there is no column in query with name [$value]")
            if (display != null && !query.containsKey(display)) throw ApplicationException("invalid value for attribute [display], there is no column in query with name [$display]")
            if (group != null && !query.containsKey(group)) throw ApplicationException("invalid value for attribute [group], there is no column in query with name [$group]")
        }
        input.setType(Input.TYPE_SELECT)
        var parent: Tag = getParent()
        while (parent != null && parent !is Form) {
            parent = parent.getParent()
        }
        if (parent is Form) {
            parent!!.setInput(input)
        } else {
            throw ApplicationException("Tag cfselect must be inside a cfform tag")
        }
        pageContext.forceWrite("<select")
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
        pageContext.forceWrite(">\n")
        if (bodyContent != null && queryPosition == QUERY_POSITION_BELOW) pageContext.forceWrite(bodyContent.getString())

        // write query options
        if (query != null) {
            val rowCount: Int = query.getRowCount()
            var v: String
            var d: String
            var currentGroup: String? = null
            var tmp: String
            val hasDisplay = display != null
            val hasGroup = group != null
            for (i in 1..rowCount) {
                v = Caster.toString(query.getAt(value, i))
                d = if (hasDisplay) Caster.toString(query.getAt(display, i)) else v
                if (hasGroup) {
                    tmp = Caster.toString(query.getAt(group, i))
                    if (currentGroup == null || !OpUtil.equals(ThreadLocalPageContext.get(), currentGroup, tmp, true)) {
                        if (currentGroup != null) pageContext.forceWrite("</optgroup>\n")
                        pageContext.forceWrite("<optgroup label=\"$tmp\">\n ")
                        currentGroup = tmp
                    }
                }
                pageContext.forceWrite("""<option${selected(v, selected)} value="$v">$d</option>
""")
            }
            if (hasGroup) pageContext.forceWrite("</optgroup>\n")
        }
        if (bodyContent != null && queryPosition == QUERY_POSITION_ABOVE) pageContext.forceWrite(bodyContent.getString())
        pageContext.forceWrite("</select>")
    }

    @Throws(PageException::class)
    private fun selected(str: String?, selected: Array<String?>?): String? {
        if (selected != null) {
            for (i in selected.indices) {
                if (caseSensitive) {
                    if (str!!.compareTo(selected[i]!!) === 0) return " selected"
                } else {
                    if (OpUtil.compare(ThreadLocalPageContext.get(), str, selected[i]) === 0) return " selected"
                }
                // if(Operator.compare(str,selected[i])==0) return " selected";
            }
        }
        return ""
    }

    /**
     * html encode a string
     *
     * @param str string to encode
     * @return encoded string
     */
    private fun enc(str: String?): String? {
        return HTMLEntities.escapeHTML(str, HTMLEntities.HTMLV20)
    }

    /**
     * @param editable the editable to set
     * @throws ApplicationException
     */
    fun setEditable(editable: Boolean) {
        this.editable = editable
    }

    /**
     * @param group the group to set
     * @throws ApplicationException
     */
    fun setGroup(group: String?) {
        this.group = group
    }

    /**
     * @param height the height to set
     * @throws ApplicationException
     */
    fun setHeight(height: Double) {
        this.height = height.toInt()
    }

    /**
     * @param label the label to set
     * @throws ApplicationException
     */
    fun setLabel(label: String?) {
        this.label = label
    }

    /**
     * @param queryPosition the queryPosition to set
     * @throws ApplicationException
     */
    @Throws(ApplicationException::class)
    fun setQueryposition(strQueryPosition: String?) {
        var strQueryPosition = strQueryPosition
        strQueryPosition = strQueryPosition.trim().toLowerCase()
        queryPosition = if ("above".equals(strQueryPosition)) QUERY_POSITION_ABOVE else if ("below".equals(strQueryPosition)) QUERY_POSITION_BELOW else throw ApplicationException("attribute queryPosition for tag select has an invalid value [$strQueryPosition], valid values are [above, below]")
    }

    /**
     * @param tooltip the tooltip to set
     * @throws ApplicationException
     */
    fun setTooltip(tooltip: String?) {
        this.tooltip = tooltip
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
    fun setWidth(width: Double) {
        this.width = width.toInt()
    }

    /**
     * @param width the width to set
     * @throws ApplicationException
     */
    fun setEnabled(enabled: String?) {
        setDisabled(!Caster.toBooleanValue(enabled, true))
    }

    /**
     * @param caseSensitive the caseSensitive to set
     */
    fun setCasesensitive(caseSensitive: Boolean) {
        this.caseSensitive = caseSensitive
    }

    companion object {
        private const val QUERY_POSITION_ABOVE = 0
        private const val QUERY_POSITION_BELOW = 1
    }
}