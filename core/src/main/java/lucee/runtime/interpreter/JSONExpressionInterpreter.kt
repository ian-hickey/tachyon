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
package lucee.runtime.interpreter

import lucee.commons.lang.NumberUtil

class JSONExpressionInterpreter @JvmOverloads constructor(strict: Boolean = false) : CFMLExpressionInterpreter() {
    @Override
    @Throws(PageException::class)
    protected override fun string(): Ref? {

        // Init Parameter
        val quoter: Char = cfml.getCurrentLower()
        val str = LStringBuffer()
        while (cfml.hasNext()) {
            cfml.next()
            // check sharp
            if (cfml.isCurrent('\\')) {
                if (cfml.isNext(quoter)) {
                    cfml.next()
                    str.append(quoter)
                } else if (cfml.isNext('\\')) {
                    cfml.next()
                    str.append('\\')
                } else if (cfml.isNext('"')) {
                    cfml.next()
                    str.append('"')
                } else if (cfml.isNext('\'')) {
                    cfml.next()
                    str.append('\'')
                } else if (cfml.isNext('t')) {
                    cfml.next()
                    str.append('\t')
                } else if (cfml.isNext('n')) {
                    cfml.next()
                    str.append('\n')
                } else if (cfml.isNext('b')) {
                    cfml.next()
                    str.append('\b')
                } else if (cfml.isNext('f')) {
                    cfml.next()
                    str.append('\f')
                } else if (cfml.isNext('r')) {
                    cfml.next()
                    str.append('\r')
                } else if (cfml.isNext('u')) {
                    cfml.next()
                    val sb = StringBuilder()
                    var i = 0
                    while (i < 4 && cfml.hasNext()) {
                        cfml.next()
                        sb.append(cfml.getCurrent())
                        i++
                    }
                    if (i < 4) {
                        str.append("\\u")
                        str.append(sb.toString())
                    } else {
                        val asc: Int = NumberUtil.hexToInt(sb.toString(), -1)
                        if (asc != -1) str.append(asc.toChar()) else {
                            str.append("\\u")
                            str.append(sb.toString())
                        }
                    }
                } else if (cfml.isNext('/')) {
                    cfml.next()
                    str.append('/')
                } else {
                    str.append('\\')
                }
            } else if (cfml.isCurrent(quoter)) {
                break
            } else {
                str.append(cfml.getCurrent())
            }
        }
        if (!cfml.forwardIfCurrent(quoter)) throw InterpreterException("Invalid String Literal Syntax Closing [$quoter] not found")
        cfml.removeSpace()
        mode = STATIC
        /*
		 * Ref value=null; if(value!=null) { if(str.isEmpty()) return value; return new
		 * Concat(pc,value,str); }
		 */return str
    }

    init { // strict is set to true, it should not be compatible with CFMLExpressionInterpreter
        allowNullConstant = true
    }
}