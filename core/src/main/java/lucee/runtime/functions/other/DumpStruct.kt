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
/**
 * Implements the CFML Function dump
 */
package lucee.runtime.functions.other

import java.util.Set

object DumpStruct : Function {
    fun call(pc: PageContext?, `object`: Object?): Struct? {
        return call(pc, `object`, 9999.0, null, null, 9999.0, true)
    }

    fun call(pc: PageContext?, `object`: Object?, maxLevel: Double): Struct? {
        return call(pc, `object`, maxLevel, null, null, 9999.0, true, true, null)
    }

    fun call(pc: PageContext?, `object`: Object?, maxLevel: Double, show: String?): Struct? {
        return call(pc, `object`, maxLevel, show, null, 9999.0, true, true, null)
    }

    fun call(pc: PageContext?, `object`: Object?, maxLevel: Double, show: String?, hide: String?): Struct? {
        return call(pc, `object`, maxLevel, show, hide, 9999.0, true, true, null)
    }

    fun call(pc: PageContext?, `object`: Object?, maxLevel: Double, show: String?, hide: String?, keys: Double): Struct? {
        return call(pc, `object`, maxLevel, show, hide, keys, true, true, null)
    }

    fun call(pc: PageContext?, `object`: Object?, maxLevel: Double, show: String?, hide: String?, keys: Double, metainfo: Boolean): Struct? {
        return call(pc, `object`, maxLevel, show, hide, keys, metainfo, true, null)
    }

    fun call(pc: PageContext?, `object`: Object?, maxLevel: Double, show: String?, hide: String?, keys: Double, metainfo: Boolean, showUDFs: Boolean): Struct? {
        return call(pc, `object`, maxLevel, show, hide, keys, metainfo, showUDFs, null)
    }

    fun call(pc: PageContext?, `object`: Object?, maxLevel: Double, show: String?, hide: String?, keys: Double, metainfo: Boolean, showUDFs: Boolean, label: String?): Struct? {
        var show = show
        var hide = hide
        if (show != null && "all".equalsIgnoreCase(show.trim())) show = null
        if (hide != null && "all".equalsIgnoreCase(hide.trim())) hide = null
        val setShow: Set<String?>? = if (show != null) ListUtil.listToSet(show.toLowerCase(), ",", true) else null
        val setHide: Set<String?>? = if (hide != null) ListUtil.listToSet(hide.toLowerCase(), ",", true) else null
        val properties = DumpProperties(maxLevel.toInt(), setShow, setHide, keys.toInt(), metainfo, showUDFs)
        var dd: DumpData? = DumpUtil.toDumpData(`object`, pc, maxLevel.toInt(), properties)
        if (!StringUtil.isEmpty(label)) {
            val table = DumpTable("#ffffff", "#cccccc", "#000000")
            table.appendRow(1, SimpleDumpData(label))
            table.appendRow(0, dd)
            dd = table
        }
        val hasReference: RefBoolean = RefBooleanImpl(false)
        val sct: Struct? = toStruct(dd, `object`, hasReference)
        sct.setEL("hasReference", hasReference.toBoolean())
        addMetaData(sct, `object`)
        return sct
    }

    private fun addMetaData(sct: Struct?, o: Object?) {
        var simpleType = "unknown" // simpleType will replace colorId and colors
        var simpleValue = ""
        try {
            if (o == null) {
                simpleType = "null"
            } else if (o is Scope) {
                simpleType = "struct"
                simpleValue = "Scope (" + getSize(o) + ")"
            } else if (Decision.isStruct(o)) {
                simpleType = "struct"
                simpleValue = "Struct (" + getSize(o) + ")"
            } else if (Decision.isArray(o)) {
                simpleType = "array"
                simpleValue = "Array (" + getSize(o) + ")"
            } else if (Decision.isQuery(o)) {
                simpleType = "query"
                simpleValue = "Query (" + getSize(o) + ")"
            } else if (Decision.isComponent(o)) {
                simpleType = "component"
                simpleValue = "Component: " + (o as ComponentImpl?).getDisplayName()
            } else if (Decision.isFunction(o) || Decision.isUserDefinedFunction(o) || Decision.isClosure(o)) {
                simpleType = "function"
                // simpleValue = "Function: " + ((Function)o).(); // TODO: add signature
            } else if (Decision.isDate(o, false)) {
                simpleType = "date"
                simpleValue = o.toString()
            } else if (Decision.isBoolean(o, false)) {
                simpleType = "boolean"
                simpleValue = o.toString()
            } else if (Decision.isInteger(o, false)) {
                simpleType = "numeric"
                simpleValue = Caster.toInteger(o).toString()
            } else if (Decision.isNumber(o, false)) {
                simpleType = "numeric"
                simpleValue = o.toString()
            } else if (Decision.isSimpleValue(o)) {
                simpleType = "string"
                simpleValue = Caster.toString(o)
                if (simpleValue.length() > 64) simpleValue = "String (" + simpleValue.length().toString() + ")"
            } else {
                simpleType = o.getClass().getSimpleName().toLowerCase()
            }
        } catch (t: Throwable) {
            ExceptionUtil.rethrowIfNecessary(t)
            simpleValue = "{error}"
        }
        sct.setEL("simpleType", simpleType)
        sct.setEL("simpleValue", simpleValue)
    }

    private fun getSize(o: Object?): String? {
        return Caster.toInteger(Len.invoke(o, 0)).toString()
    }

    private fun toStruct(dd: DumpData?, `object`: Object?, hasReference: RefBoolean?): Struct? {
        var dd: DumpData? = dd
        val table: DumpTable?
        if (dd is DumpTable) table = dd as DumpTable? else {
            if (dd == null) dd = SimpleDumpData("null")
            table = DumpTable("#ffffff", "#cccccc", "#000000")
            table.appendRow(1, dd)
        }
        return toCFML(table, `object`, hasReference, null)
    }

    private fun toCFML(dd: DumpData?, `object`: Object?, hasReference: RefBoolean?, colors: Struct?): Object? {
        if (dd is DumpTable) return toCFML(dd as DumpTable?, `object`, hasReference, colors)
        return if (dd == null) SimpleDumpData("null") else dd.toString()
    }

    private fun toCFML(dt: DumpTable?, `object`: Object?, hasReference: RefBoolean?, colors: Struct?): Struct? {
        var colors: Struct? = colors
        val sct: Struct = StructImpl()
        if (colors == null) {
            colors = StructImpl()
            sct.setEL("colors", colors)
        }
        val type: Collection.Key
        type = if (dt.getType() != null) KeyImpl.init(dt.getType()) else if (`object` != null) KeyImpl.init(`object`.getClass().getName()) else KeyConstants._null

        // colors
        val borderColor = toShortColor(dt.getBorderColor())
        val fontColor = toShortColor(dt.getFontColor())
        val highLightColor = toShortColor(dt.getHighLightColor())
        val normalColor = toShortColor(dt.getNormalColor())
        // create color id
        val colorId: Key = KeyImpl.init(toString(
                HashUtil.create64BitHash(StringBuilder(borderColor).append(':').append(fontColor).append(':').append(highLightColor).append(':').append(normalColor)),
                Character.MAX_RADIX))
        if (!colors.containsKey(colorId)) {
            val color: Struct = StructImpl()
            StructUtil.setELIgnoreWhenNull(color, "borderColor", borderColor)
            StructUtil.setELIgnoreWhenNull(color, "fontColor", fontColor)
            StructUtil.setELIgnoreWhenNull(color, "highLightColor", highLightColor)
            StructUtil.setELIgnoreWhenNull(color, "normalColor", normalColor)
            colors.setEL(colorId, color)
        }

        /*
		 * StructUtil.setELIgnoreWhenNull(sct,"borderColor", borderColor);
		 * StructUtil.setELIgnoreWhenNull(sct,"fontColor", fontColor);
		 * StructUtil.setELIgnoreWhenNull(sct,"highLightColor", highLightColor);
		 * StructUtil.setELIgnoreWhenNull(sct,"normalColor", normalColor);
		 */StructUtil.setELIgnoreWhenNull(sct, "colorId", colorId.getString())
        StructUtil.setELIgnoreWhenNull(sct, KeyConstants._comment, dt.getComment())
        StructUtil.setELIgnoreWhenNull(sct, KeyConstants._height, dt.getHeight())
        StructUtil.setELIgnoreWhenNull(sct, KeyConstants._width, dt.getWidth())
        StructUtil.setELIgnoreWhenNull(sct, KeyConstants._title, dt.getTitle())
        sct.setEL(KeyConstants._type, type.getString())
        if (!StringUtil.isEmpty(dt.getId())) sct.setEL(KeyConstants._id, dt.getId())
        if ("ref".equals(dt.getType())) {
            hasReference.setValue(true)
            sct.setEL(KeyConstants._ref, dt.getRef())
        }
        val drs: Array<DumpRow?> = dt.getRows()
        var dr: DumpRow?
        var qry: Query? = null
        var items: Array<DumpData?>
        for (r in drs.indices) {
            dr = drs[r]
            items = dr.getItems()
            if (qry == null) qry = QueryImpl(toColumns(items), drs.size, "data")
            for (c in 1..items.size) {
                qry.setAtEL("data$c", r + 1, toCFML(items[c - 1], `object`, hasReference, colors))
            }
            qry.setAtEL("highlight", r + 1, Double.valueOf(dr.getHighlightType()))
        }
        if (qry != null) sct.setEL(KeyConstants._data, qry)
        return sct
    }

    private fun toColumns(items: Array<DumpData?>?): Array<String?>? {
        val columns = arrayOfNulls<String?>(items!!.size + 1)
        columns[0] = "highlight"
        for (i in 1 until columns.size) {
            columns[i] = "data$i"
        }
        return columns
    }

    /*
	 * public static String getContext() { //Throwable cause = t.getCause(); StackTraceElement[] traces
	 * = Thread.currentThread().getStackTrace();
	 * 
	 * int line=0; String template; StackTraceElement trace=null; for(int i=0;i<traces.length;i++) {
	 * trace=traces[i]; template=trace.getFileName(); if((line=trace.getLineNumber())<=0 ||
	 * template==null || ResourceUtil.getExtension(template,"").equals("java")) continue; return
	 * template+":"+line; } return null; }
	 */
    private fun toShortColor(color: String?): String? {
        if (color != null && color.length() === 7 && color.startsWith("#")) {
            if (color.charAt(1) === color.charAt(2) && color.charAt(3) === color.charAt(4) && color.charAt(5) === color.charAt(6)) return "#" + color.charAt(1) + color.charAt(3) + color.charAt(5)
        }
        return color
    }
}