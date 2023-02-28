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
package lucee.commons.io.res.filter

import lucee.commons.io.res.Resource

/**
 * Filter fuer die `listFiles` Methode des FIle Objekt, zum filtern von FIles mit einer
 * bestimmten Extension.
 */
class ExtensionResourceFilter(extensions: Array<String>, allowDir: Boolean, ignoreCase: Boolean, mustExists: Boolean) : ResourceFilter {
    /**
     * @return Returns the extension.
     */
    var extensions: Array<String?>
        private set
    private val allowDir: Boolean
    private val ignoreCase: Boolean
    private val mustExists: Boolean

    /**
     * Konstruktor des Filters
     *
     * @param extension Endung die geprueft werden soll.
     */
    constructor(extension: String) : this(arrayOf<String>(extension), false, true) {}

    /**
     * Konstruktor des Filters
     *
     * @param extension Endung die geprueft werden soll.
     */
    constructor(extension: String, allowDir: Boolean) : this(arrayOf<String>(extension), allowDir, true, true) {}
    constructor(extensions: Array<String>) : this(extensions, false, true, true) {}
    constructor(extensions: Array<String>, allowDir: Boolean) : this(extensions, allowDir, true, true) {}
    constructor(extensions: Array<String>, allowDir: Boolean, ignoreCase: Boolean) : this(extensions, allowDir, ignoreCase, true) {}

    fun addExtension(extension: String) {
        val tmp = arrayOfNulls<String>(extensions.size + 1)
        // add existing
        for (i in extensions.indices) {
            tmp[i] = extensions[i]
        }
        // add the new one
        if (!StringUtil.startsWith(extension, '.')) tmp[extensions.size] = ".$extension" else tmp[extensions.size] = extension
        extensions = tmp
    }

    @Override
    fun accept(res: Resource): Boolean {
        if (res.isDirectory()) return allowDir
        if (!mustExists || res.exists()) {
            val name: String = res.getName()
            for (i in extensions.indices) {
                if (ignoreCase) {
                    if (StringUtil.endsWithIgnoreCase(name, extensions[i])) return true
                } else {
                    if (name.endsWith(extensions[i])) return true
                }
            }
        }
        return false
    }

    fun accept(name: String): Boolean {
        for (i in extensions.indices) {
            if (ignoreCase) {
                if (StringUtil.endsWithIgnoreCase(name, extensions[i])) return true
            } else {
                if (name.endsWith(extensions[i])) return true
            }
        }
        return false
    }

    @Override
    override fun toString(): String {
        val sb = StringBuilder()
        for (ext in extensions) {
            if (sb.length() > 0) sb.append(',')
            sb.append(ListUtil.trim(ext, "."))
        }
        return sb.toString()
    }

    companion object {
        // private int extLen;
        val EXTENSION_JAR_NO_DIR = ExtensionResourceFilter(".jar", false)
        val EXTENSION_CLASS_DIR = ExtensionResourceFilter(".class", true)
    }

    init {
        val tmp = arrayOfNulls<String>(extensions.size)
        for (i in extensions.indices) {
            if (!StringUtil.startsWith(extensions[i], '.')) tmp[i] = "." + extensions[i] else tmp[i] = extensions[i]
        }
        this.extensions = tmp
        this.allowDir = allowDir
        this.ignoreCase = ignoreCase
        this.mustExists = mustExists
    }
}