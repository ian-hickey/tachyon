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
package lucee.loader.util

import java.io.File

/**
 * Filter fuer die `listFiles` Methode des FIle Objekt, zum filtern von FIles mit einer
 * bestimmten Extension.
 */
class ExtensionFilter(extensions: Array<String>, allowDir: Boolean, ignoreCase: Boolean) : FileFilter {
    /**
     * @return Returns the extension.
     */
    val extensions: Array<String>
    private val allowDir: Boolean
    private val ignoreCase: Boolean
    // private int extLen;
    /**
     * Konstruktor des Filters
     *
     * @param extension Endung die geprueft werden soll.
     */
    constructor(extension: String) : this(arrayOf<String>(extension), false, true) {}
    constructor(extension: String, allowDir: Boolean) : this(arrayOf<String>(extension), allowDir, true) {}
    constructor(extensions: Array<String>) : this(extensions, false, true) {}
    constructor(extensions: Array<String>, allowDir: Boolean) : this(extensions, allowDir, true) {}

    /**
     * @see java.io.FileFilter.accept
     */
    @Override
    fun accept(res: File): Boolean {
        if (res.isDirectory()) return allowDir
        if (res.exists()) {
            val name: String = if (ignoreCase) res.getName().toLowerCase() else res.getName()
            for (extension in extensions) if (name.endsWith(extension)) return true
        }
        return false
    }

    init {
        for (i in extensions.indices) {
            if (!extensions[i].startsWith(".")) extensions[i] = "." + extensions[i]
            if (ignoreCase) extensions[i] = extensions[i].toLowerCase()
        }
        this.extensions = extensions
        this.allowDir = allowDir
        this.ignoreCase = ignoreCase
    }
}