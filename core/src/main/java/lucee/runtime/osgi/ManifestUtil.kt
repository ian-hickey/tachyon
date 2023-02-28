/**
 * Copyright (c) 2015, Lucee Assosication Switzerland. All rights reserved.
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
package lucee.runtime.osgi

import java.util.ArrayList

object ManifestUtil {
    private const val DEFAULT_MAX_LINE_SIZE = 100
    private val DEFAULT_MAIN_FILTER: Set<String?>? = HashSet<String?>()
    private val DEFAULT_INDIVIDUAL_FILTER: Set<String?>? = HashSet<String?>()
    fun toString(manifest: Manifest?, maxLineSize: Int, mainSectionIgnore: Set<String?>?, individualSectionIgnore: Set<String?>?): String? {
        var maxLineSize = maxLineSize
        var mainSectionIgnore = mainSectionIgnore
        var individualSectionIgnore = individualSectionIgnore
        if (maxLineSize < 0) maxLineSize = DEFAULT_MAX_LINE_SIZE
        val msb = StringBuilder()
        val main: Attributes = manifest.getMainAttributes()

        // prepare ignores
        if (mainSectionIgnore == null) mainSectionIgnore = DEFAULT_MAIN_FILTER else mainSectionIgnore.addAll(DEFAULT_MAIN_FILTER)
        if (individualSectionIgnore == null) individualSectionIgnore = DEFAULT_INDIVIDUAL_FILTER else individualSectionIgnore.addAll(DEFAULT_INDIVIDUAL_FILTER)

        // Manifest-Version comes first
        add(msb, "Manifest-Version", main.getValue("Manifest-Version"), "1.0")
        // all other main attributes
        printSection(msb, main, maxLineSize, mainSectionIgnore)

        // individual entries
        val entries: Map<String?, Attributes?> = manifest.getEntries()
        if (entries != null && entries.size() > 0) {
            val it: Iterator<Entry<String?, Attributes?>?> = entries.entrySet().iterator()
            var e: Entry<String?, Attributes?>?
            var sb: StringBuilder?
            while (it.hasNext()) {
                e = it.next()
                sb = StringBuilder()
                printSection(sb, e.getValue(), maxLineSize, individualSectionIgnore)
                if (sb.length() > 0) {
                    msb.append('\n') // new section need an empty line
                    add(msb, "Name", e.getKey(), null)
                    msb.append(sb)
                }
            }
        }
        return msb.toString()
    }

    private fun printSection(sb: StringBuilder?, attrs: Attributes?, maxLineSize: Int, ignore: Set<String?>?) {
        val it: Iterator<Entry<Object?, Object?>?> = attrs.entrySet().iterator()
        var e: Entry<Object?, Object?>?
        var name: String
        var value: String?
        while (it.hasNext()) {
            e = it.next()
            name = (e.getKey() as Name).toString()
            value = e.getValue()
            if (StringUtil.isEmpty(value)) continue
            if ("Import-Package".equals(name) || "Export-Package".equals(name) || "Require-Bundle".equals(name)) {
                value = splitByComma(value)
            } else if (value!!.length() > maxLineSize) value = split(value, maxLineSize)
            if (ignore != null && ignore.contains(name)) continue
            add(sb, name, value, null)
        }
    }

    private fun splitByComma(value: String?): String? {
        val st = StringTokenizer(value.trim(), ",")
        val sb = StringBuilder()
        while (st.hasMoreTokens()) {
            if (sb.length() > 0) sb.append(",\n ")
            sb.append(st.nextToken().trim())
        }
        return sb.toString()
    }

    private fun split(value: String?, max: Int): String? {
        val st = StringTokenizer(value, "\n")
        val sb = StringBuilder()
        while (st.hasMoreTokens()) {
            _split(sb, st.nextToken(), max)
        }
        return sb.toString()
    }

    private fun _split(sb: StringBuilder?, value: String?, max: Int) {
        var index = 0
        while (index + max <= value!!.length()) {
            if (sb.length() > 0) sb.append("\n ")
            sb.append(value.substring(index, index + max))
            index = index + max
        }
        if (index < value!!.length()) {
            if (sb.length() > 0) sb.append("\n ")
            sb.append(value.substring(index, value!!.length()))
        }
    }

    private fun add(sb: StringBuilder?, name: String?, value: String?, defaultValue: String?) {
        var value = value
        if (value == null) {
            if (defaultValue == null) return
            value = defaultValue
        }
        sb.append(name).append(": ").append(value).append('\n')
    }

    fun removeFromList(attrs: Attributes?, key: String?, valueToRemove: String?) {
        var valueToRemove = valueToRemove
        val `val`: String = attrs.getValue(key)
        if (StringUtil.isEmpty(`val`)) return
        val sb = StringBuilder()
        var removed = false
        var wildcard = false
        if (valueToRemove.endsWith(".*")) {
            wildcard = true
            valueToRemove = valueToRemove.substring(0, valueToRemove!!.length() - 1)
        }
        try {
            val it = toList(`val`)!!.iterator() // ListUtil.toStringArray(ListUtil.listToArrayTrim(val, ','));
            var str: String?
            while (it.hasNext()) {
                str = it.next()
                str = str.trim()
                // print.e("=="+str);
                if (if (wildcard) str.startsWith(valueToRemove) else str!!.equals(valueToRemove) || ListUtil.first(str, ";").trim().equals(valueToRemove)) {
                    removed = true
                    continue
                }
                if (sb.length() > 0) sb.append(",\n ")
                sb.append(str)
            }
        } catch (e: Throwable) {
            ExceptionUtil.rethrowIfNecessary(e)
        }
        if (removed) {
            if (sb.length() > 0) attrs.putValue(key, sb.toString()) else attrs.remove(key)
        }
    }

    fun removeOptional(attrs: Attributes?, key: String?) {
        val `val`: String = attrs.getValue(key)
        if (StringUtil.isEmpty(`val`)) return
        val sb = StringBuilder()
        var removed = false
        try {
            val it = toList(`val`)!!.iterator() // ListUtil.toStringArray(ListUtil.listToArrayTrim(val, ','));
            var str: String?
            while (it.hasNext()) {
                str = it.next()
                str = str.trim()
                // print.e("=="+str);
                if (str.indexOf("resolution:=optional") !== -1) {
                    removed = true
                    continue
                }
                if (sb.length() > 0) sb.append(",\n ")
                sb.append(str)
            }
        } catch (e: Throwable) {
            ExceptionUtil.rethrowIfNecessary(e)
        }
        if (removed) attrs.putValue(key, sb.toString())
    }

    private fun toList(`val`: String?): List<String?>? {
        val list: List<String?> = ArrayList<String?>()
        val len: Int = `val`!!.length()
        var inside = 0
        var c: Char
        var begin = 0
        for (i in 0 until len) {
            c = `val`.charAt(i)
            if (c == '"') {
                if (inside == '"'.toInt()) inside = 0 else if (inside == 0) inside = '"'.toInt()
            } else if (c == '\'') {
                if (inside == '\''.toInt()) inside = 0 else if (inside == 0) inside = '\''.toInt()
            } else if (c == ',' && inside == 0) {
                if (begin < i) list.add(`val`.substring(begin, i))
                begin = i + 1
            }
        }
        if (begin < len) list.add(`val`.substring(begin))
        return list
    }

    init {
        DEFAULT_MAIN_FILTER.add("Manifest-Version")
    }

    init {
        DEFAULT_INDIVIDUAL_FILTER.add("Name")
    }
}