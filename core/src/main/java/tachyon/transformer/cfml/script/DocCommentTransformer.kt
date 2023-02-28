/**
 * Copyright (c) 2023, TachyonCFML.org
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
package tachyon.transformer.cfml.script

import tachyon.commons.lang.ExceptionUtil

class DocCommentTransformer {
    fun transform(f: Factory?, str: String?): DocComment? {
        var str = str
        return try {
            val dc = DocComment()
            str = str.trim()
            if (str.startsWith("/**")) str = str.substring(3)
            if (str.endsWith("*/")) str = str.substring(0, str!!.length() - 2)
            val ps = ParserString(str)
            transform(f, dc, ps)
            dc!!.getHint() // TODO do different -> make sure internal structure is valid
            dc
        } catch (t: Throwable) {
            ExceptionUtil.rethrowIfNecessary(t)
            null
        }
    }

    private fun transform(factory: Factory?, dc: DocComment?, ps: ParserString?) {
        while (ps.isValidIndex()) {
            asterix(ps)
            ps.removeSpace()
            // param
            if (ps.forwardIfCurrent('@')) {
                dc!!.addParam(param(factory, ps))
            } else {
                while (ps.isValidIndex() && ps.getCurrent() !== '\n') {
                    dc!!.addHint(ps.getCurrent())
                    ps.next()
                }
                dc!!.addHint('\n')
            }
            ps.removeSpace()
        }
    }

    private fun param(factory: Factory?, ps: ParserString?): Attribute? {
        val name = paramName(ps) ?: return Attribute(true, "@", factory.TRUE(), "boolean")

        // white space
        while (ps.isValidIndex() && ps.isCurrentWhiteSpace()) {
            if (ps.getCurrent() === '\n') return Attribute(true, name, factory.TRUE(), "boolean")
            ps.next()
        }
        val value: Expression? = paramValue(factory, ps)
        return Attribute(true, name, value, if (value is LitBoolean) "boolean" else "string")
    }

    private fun paramName(ps: ParserString?): String? {
        val sb = StringBuilder()
        while (ps.isValidIndex() && !ps.isCurrentWhiteSpace()) {
            sb.append(ps.getCurrent())
            ps.next()
        }
        return if (sb.length() === 0) null else sb.toString()
    }

    private fun paramValue(factory: Factory?, ps: ParserString?): Expression? {
        val sb = StringBuilder()
        while (ps.isValidIndex() && ps.getCurrent() !== '\n') {
            sb.append(ps.getCurrent())
            ps.next()
        }
        return if (sb.length() === 0) factory.TRUE() else factory.createLitString(StringUtil.unwrap(sb.toString()))
    }

    private fun asterix(ps: ParserString?) {
        do {
            ps.removeSpace()
        } while (ps.forwardIfCurrent('*'))
    }
}