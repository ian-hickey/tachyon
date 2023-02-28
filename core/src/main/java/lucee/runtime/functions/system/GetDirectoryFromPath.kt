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
 * Implements the CFML Function getdirectoryfrompath
 */
package lucee.runtime.functions.system

import java.io.File

object GetDirectoryFromPath : Function {
    fun call(pc: PageContext?, path: String?): String? {
        return invoke(path)
    }

    operator fun invoke(path: String?): String? {
        var posOfLastDel: Int = path.lastIndexOf('/')
        var parent: String? = ""
        if (path.lastIndexOf('\\') > posOfLastDel) posOfLastDel = path.lastIndexOf("\\")
        if (posOfLastDel != -1) parent = path.substring(0, posOfLastDel + 1) else if (path!!.equals(".") || path.equals("..")) parent = String.valueOf(File.separatorChar) else if (path.startsWith(".")) parent = String.valueOf(File.separatorChar) else parent = String.valueOf(File.separatorChar)
        return parent
    }
}