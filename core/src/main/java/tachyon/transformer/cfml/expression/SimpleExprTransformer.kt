/**
 * Copyright (c) 2014, the Railo Company Ltd.
 * Copyright (c) 2015, Tachyon Assosication Switzerland
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
package tachyon.transformer.cfml.expression

import tachyon.runtime.exp.TemplateException

/**
 * Zum lesen von Attributen bei dem CFML expressions nicht geparst werden sollen (cfloop condition)
 */
class SimpleExprTransformer(private val specialChar: Char) : ExprTransformer {
    @Override
    @Throws(TemplateException::class)
    fun transformAsString(data: Data?): Expression? {
        return transform(data)
    }

    @Override
    @Throws(TemplateException::class)
    fun transform(data: Data?): Expression? {
        var expr: Expression? = null
        // String
        return if (string(data.factory, data.srcCode).also { expr = it } != null) {
            expr
        } else simple(data.factory, data.srcCode)
        // Simple
    }

    /**
     * Liest den String ein
     *
     * @return Element
     * @throws TemplateException
     */
    @Throws(TemplateException::class)
    fun string(f: Factory?, cfml: SourceCode?): Expression? {
        cfml.removeSpace()
        val quoter: Char = cfml.getCurrentLower()
        if (quoter != '"' && quoter != '\'') return null
        val str = StringBuffer()
        var insideSpecial = false
        val line: Position = cfml.getPosition()
        while (cfml.hasNext()) {
            cfml.next()
            // check special
            if (cfml.isCurrent(specialChar)) {
                insideSpecial = !insideSpecial
                str.append(specialChar)
            } else if (!insideSpecial && cfml.isCurrent(quoter)) {
                // Ecaped sharp
                if (cfml.isNext(quoter)) {
                    cfml.next()
                    str.append(quoter)
                } else {
                    break
                }
            } else {
                str.append(cfml.getCurrent())
            }
        }
        if (!cfml.forwardIfCurrent(quoter)) throw TemplateException(cfml, "Invalid Syntax Closing [$quoter] not found")
        val rtn: LitString = f.createLitString(str.toString(), line, cfml.getPosition())
        cfml.removeSpace()
        return rtn
    }

    /**
     * Liest ein
     *
     * @return Element
     * @throws TemplateException
     */
    @Throws(TemplateException::class)
    fun simple(f: Factory?, cfml: SourceCode?): Expression? {
        val sb = StringBuffer()
        val line: Position = cfml.getPosition()
        while (cfml.isValidIndex()) {
            if (cfml.isCurrent(' ') || cfml.isCurrent('>') || cfml.isCurrent("/>")) break else if (cfml.isCurrent('"') || cfml.isCurrent('#') || cfml.isCurrent('\'')) {
                throw TemplateException(cfml, "simple attribute value can't contain [" + cfml.getCurrent().toString() + "]")
            } else sb.append(cfml.getCurrent())
            cfml.next()
        }
        cfml.removeSpace()
        return f.createLitString(sb.toString(), line, cfml.getPosition())
    }
}