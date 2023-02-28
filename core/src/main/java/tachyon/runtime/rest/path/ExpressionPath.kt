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

import java.util.ArrayList

class ExpressionPath(pattern: Pattern?, variables: List<String?>?) : Path() {
    private val pattern: Pattern?
    private val variables: List<String?>?

    @Override
    override fun match(result: Struct?, path: String?): Boolean {
        var `var`: String?
        val m: Matcher = pattern.matcher(path)
        val hasMatches: Boolean = m.find()
        if (!hasMatches) return false
        if (hasMatches) {
            // Get all groups for this match
            val len: Int = m.groupCount()
            for (i in 1..len) {
                val groupStr: String = m.group(i)
                `var` = variables!![i - 1]
                if (`var` != null) result.setEL(`var`, groupStr.trim())
            }
        }
        return true
    }

    @Override
    override fun toString(): String {
        return "expression:" + pattern.pattern()
    }

    companion object {
        fun getInstance(path: String?): Path? {
            /*
		 * TODO handle if a pattern already has defined a group
		 */
            var last = -1
            var startIndex: Int
            var endIndex = 0
            var index: Int
            var content: String?
            var variableName: String?
            var regexPart: String
            val regex = StringBuilder()
            val variables: List<String?> = ArrayList<String?>()
            while (path.indexOf('{', last).also { startIndex = it } != -1) {
                if (last + 1 < startIndex) {
                    delimiter(variables, regex, path.substring(last + 1, startIndex))
                }
                endIndex = path.indexOf('}', startIndex + 1)
                if (endIndex == -1) return LiteralPath(path)
                content = path.substring(startIndex + 1, endIndex)
                index = content.indexOf(':')
                if (index != -1) {
                    variableName = content.substring(0, index).trim()
                    regexPart = content.substring(index + 1).trim()
                } else {
                    variableName = content.trim()
                    regexPart = ".+"
                }
                regex.append('(')
                regex.append(regexPart)
                regex.append(')')
                variables.add(variableName)
                // print.e(variableName);
                // print.e(regexPart);
                last = endIndex
            }
            if (endIndex + 1 < path!!.length()) delimiter(variables, regex, path.substring(endIndex + 1))

            // regex.append("(.*)");
            val pattern: Pattern = Pattern.compile(regex.toString())
            // print.e(regex);
            // print.e(variables);
            return ExpressionPath(pattern, variables)
        }

        private fun delimiter(variables: List<String?>?, regex: StringBuilder?, delimiter: String?) {
            variables.add(null)
            regex.append('(')
            /*
		 * print.e(delimiter+":"+Pattern.quote(delimiter)); StringBuilder sb=new StringBuilder(); int
		 * len=delimiter.length(); char c; for (int i=0; i<len; i++) { c=delimiter.charAt(i); switch(c){
		 * case '.': sb.append("\\.");break; case '?': sb.append("\\?");break; case '\\':
		 * sb.append("\\\\");break; case '^': sb.append("\\^");break; case '$': sb.append("\\$");break; case
		 * '+': sb.append("\\+");break; default: sb.append(c); break; } }
		 */regex.append(Pattern.quote(delimiter))
            regex.append(')')
        }
    }

    init {
        this.pattern = pattern
        this.variables = variables
    }
}