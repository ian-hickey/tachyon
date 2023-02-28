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
package tachyon.runtime.functions.list

import tachyon.commons.lang.StringUtil

/**
 * Implements the CFML Function listqualify
 */
class ListQualify : BIF() {
    @Override
    @Throws(PageException::class)
    operator fun invoke(pc: PageContext?, args: Array<Object?>?): Object? {
        if (args!!.size == 2) return call(pc, Caster.toString(args[0]), Caster.toString(args[1]))
        if (args.size == 3) return call(pc, Caster.toString(args[0]), Caster.toString(args[1]), Caster.toString(args[2]))
        if (args.size == 4) return call(pc, Caster.toString(args[0]), Caster.toString(args[1]), Caster.toString(args[2]), Caster.toString(args[3]))
        if (args.size == 5) return call(pc, Caster.toString(args[0]), Caster.toString(args[1]), Caster.toString(args[2]), Caster.toString(args[3]), Caster.toBooleanValue(args[4]))
        throw FunctionException(pc, "ListQualify", 2, 5, args.size)
    }

    companion object {
        private const val serialVersionUID = -7450079285934992224L
        fun call(pc: PageContext?, list: String?, qualifier: String?): String? {
            return call(pc, list, qualifier, ",", "all", false, false)
        }

        fun call(pc: PageContext?, list: String?, qualifier: String?, delimiter: String?): String? {
            return call(pc, list, qualifier, delimiter, "all", false, false)
        }

        fun call(pc: PageContext?, list: String?, qualifier: String?, delimiter: String?, elements: String?): String? {
            return call(pc, list, qualifier, delimiter, elements, false, false)
        }

        fun call(pc: PageContext?, list: String?, qualifier: String?, delimiter: String?, elements: String?, includeEmptyFields: Boolean): String? {
            return call(pc, list, qualifier, delimiter, elements, includeEmptyFields, false)
        }

        fun call(pc: PageContext?, list: String?, qualifier: String?, delimiter: String?, elements: String?, includeEmptyFields: Boolean, psq: Boolean // this is used only
                // internally by tachyon,
                // search for "PSQ-BIF" in
                // code
        ): String? {
            var list = list
            if (list!!.length() === 0) return ""
            if (psq) list = StringUtil.replace(list, "'", "''", false)
            val arr: Array = if (includeEmptyFields) ListUtil.listToArray(list, delimiter) else ListUtil.listToArrayRemoveEmpty(list, delimiter)
            val isQChar = qualifier!!.length() === 1
            val isDChar = delimiter!!.length() === 1
            return if (isQChar && isDChar) doIt(arr, qualifier.charAt(0), delimiter.charAt(0), elements) else if (isQChar && !isDChar) doIt(arr, qualifier.charAt(0), delimiter, elements) else if (!isQChar && isDChar) doIt(arr, qualifier, delimiter.charAt(0), elements) else doIt(arr, qualifier, delimiter, elements)
        }

        private fun doIt(arr: Array?, qualifier: Char, delimiter: Char, elements: String?): String? {
            val rtn = StringBuilder()
            val len: Int = arr.size()
            if (StringUtil.toLowerCase(elements!!).equals("all")) {
                rtn.append(qualifier)
                rtn.append(arr.get(1, ""))
                rtn.append(qualifier)
                for (i in 2..len) {
                    rtn.append(delimiter)
                    rtn.append(qualifier)
                    rtn.append(arr.get(i, ""))
                    rtn.append(qualifier)
                }
            } else {
                qualifyString(rtn, arr.get(1, "").toString(), qualifier)
                for (i in 2..len) {
                    rtn.append(delimiter)
                    qualifyString(rtn, arr.get(i, "").toString(), qualifier)
                }
            }
            return rtn.toString()
        }

        private fun doIt(arr: Array?, qualifier: Char, delimiter: String?, scope: String?): String? {
            val rtn = StringBuilder()
            val len: Int = arr.size()
            if (StringUtil.toLowerCase(scope!!).equals("all")) {
                rtn.append(qualifier)
                rtn.append(arr.get(1, ""))
                rtn.append(qualifier)
                for (i in 2..len) {
                    rtn.append(delimiter)
                    rtn.append(qualifier)
                    rtn.append(arr.get(i, ""))
                    rtn.append(qualifier)
                }
            } else {
                qualifyString(rtn, arr.get(1, "").toString(), qualifier)
                for (i in 2..len) {
                    rtn.append(delimiter)
                    qualifyString(rtn, arr.get(i, "").toString(), qualifier)
                }
            }
            return rtn.toString()
        }

        private fun doIt(arr: Array?, qualifier: String?, delimiter: Char, scope: String?): String? {
            val rtn = StringBuilder()
            val len: Int = arr.size()
            if (StringUtil.toLowerCase(scope!!).equals("all")) {
                rtn.append(qualifier)
                rtn.append(arr.get(1, ""))
                rtn.append(qualifier)
                for (i in 2..len) {
                    rtn.append(delimiter)
                    rtn.append(qualifier)
                    rtn.append(arr.get(i, ""))
                    rtn.append(qualifier)
                }
            } else {
                qualifyString(rtn, arr.get(1, "").toString(), qualifier)
                for (i in 2..len) {
                    rtn.append(delimiter)
                    qualifyString(rtn, arr.get(i, "").toString(), qualifier)
                }
            }
            return rtn.toString()
        }

        private fun doIt(arr: Array?, qualifier: String?, delimiter: String?, scope: String?): String? {
            val rtn = StringBuilder()
            val len: Int = arr.size()
            if (StringUtil.toLowerCase(scope!!).equals("all")) {
                rtn.append(qualifier)
                rtn.append(arr.get(1, ""))
                rtn.append(qualifier)
                for (i in 2..len) {
                    rtn.append(delimiter)
                    rtn.append(qualifier)
                    rtn.append(arr.get(i, ""))
                    rtn.append(qualifier)
                }
            } else {
                qualifyString(rtn, arr.get(1, "").toString(), qualifier)
                for (i in 2..len) {
                    rtn.append(delimiter)
                    qualifyString(rtn, arr.get(i, "").toString(), qualifier)
                }
            }
            return rtn.toString()
        }

        private fun qualifyString(rtn: StringBuilder?, value: String?, qualifier: String?) {
            if (Decision.isNumber(value)) rtn.append(value) else {
                rtn.append(qualifier)
                rtn.append(value)
                rtn.append(qualifier)
            }
        }

        private fun qualifyString(rtn: StringBuilder?, value: String?, qualifier: Char) {
            if (Decision.isNumber(value)) rtn.append(value) else {
                rtn.append(qualifier)
                rtn.append(value)
                rtn.append(qualifier)
            }
        }
    }
}