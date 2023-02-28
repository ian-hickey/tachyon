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
package tachyon.runtime.rest.path

import java.util.Iterator

abstract class Path {
    /**
     * check if given path part match this path Path part definition
     *
     * @param variables fill all key value pairs extracted from path to this Map
     * @param path path to check
     * @return true if the given path match, false otherwise
     */
    abstract fun match(variables: Struct?, path: String?): Boolean

    companion object {
        fun init(path: String?): Array<Path?>? {
            val arr: Array = ListUtil.listToArrayRemoveEmpty(path, '/')
            val rtn = arrayOfNulls<Path?>(arr.size())
            val it: Iterator = arr.valueIterator()
            var index = -1
            var str: String
            while (it.hasNext()) {
                index++
                str = Caster.toString(it.next(), null)
                // print.e("str:"+str);
                if (str.indexOf('{') !== -1) rtn[index] = ExpressionPath.getInstance(str) else rtn[index] = LiteralPath(str)
            }
            return rtn
        }
    }
}