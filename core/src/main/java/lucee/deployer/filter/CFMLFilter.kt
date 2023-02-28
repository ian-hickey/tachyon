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
package lucee.deployer.filter

import lucee.commons.io.res.Resource

/**
 * Die Klasse CFMLFilter implementiert das Interface Filter, die Klasse prueft bei einem
 * uebergebenen File Objekt, ob dessen Extension mit denen die dem Konstruktor mitgegeben wurden
 * uebereinstimmen.
 */
class CFMLFilter(private val extensions: Array<String?>?) : Filter {
    @Override
    override fun isValid(file: Resource?): Boolean {
        val arr: Array<String?>
        arr = try {
            ListUtil.toStringArray(ListUtil.listToArray(file.getName(), '.'))
        } catch (e: PageException) {
            return false
        }
        val ext: String = arr[arr.size - 1].toLowerCase()
        for (i in extensions.indices) {
            if (extensions!![i]!!.equals(ext)) return true
        }
        return false
    }

    /**
     * Konstruktor von CFMLFilter, dem Konstruktor wird ein String Array uebergeben mit Extensions die
     * geprueft werden sollen, wie z.B. {"html","htm"}.
     *
     * @param extensions Extensions die geprueft werden sollen.
     */
    init {
        for (i in extensions.indices) {
            extensions!![i] = extensions[i].toLowerCase()
        }
    }
}