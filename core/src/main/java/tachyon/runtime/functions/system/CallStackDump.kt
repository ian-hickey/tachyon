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
package tachyon.runtime.functions.system

import java.io.IOException

object CallStackDump {
    @Throws(PageException::class)
    fun call(pc: PageContext?): String? {
        return call(pc, null)
    }

    @Throws(PageException::class)
    fun call(pc: PageContext?, output: String?): String? {
        var sct: Struct? = null
        var func: String

        // create stack
        val sb = StringBuilder()
        val it: Iterator<Object?> = CallStackGet.call(pc).valueIterator()
        while (it.hasNext()) {
            sct = it.next() as Struct?
            func = sct.get(KeyConstants._function)
            sb.append(sct.get(KeyConstants._template))
            if (func.length() > 0) {
                sb.append(':')
                sb.append(func)
            }
            sb.append(':')
            sb.append(Caster.toString(sct.get(CallStackGet.LINE_NUMBER)))
            sb.append('\n')
        }

        // output
        try {
            if (StringUtil.isEmpty(output, true) || output.trim().equalsIgnoreCase("browser")) {
                pc.forceWrite("<pre>")
                pc.forceWrite(sb.toString())
                pc.forceWrite("</pre>")
            } else if (output.trim().equalsIgnoreCase("console")) {
                System.out.println(sb.toString())
            } else {
                val res: Resource = ResourceUtil.toResourceNotExisting(pc, output)
                IOUtil.write(res, sb.toString().toString() + "\n", (pc as PageContextImpl?).getResourceCharset().name(), true)
            }
        } catch (ioe: IOException) {
            throw Caster.toPageException(ioe)
        }
        return null
    }
}