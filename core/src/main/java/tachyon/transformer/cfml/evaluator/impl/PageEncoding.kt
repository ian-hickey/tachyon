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
package tachyon.transformer.cfml.evaluator.impl

import java.nio.charset.Charset

class PageEncoding : EvaluatorSupport() {
    @Override
    @Throws(TemplateException::class)
    fun execute(config: Config?, tag: Tag?, libTag: TagLibTag?, flibs: Array<FunctionLib?>?, data: Data?): TagLib? {

        // encoding
        val str: String = ASMUtil.getAttributeString(tag, "charset", null)
                ?: throw TemplateException(data.srcCode, "attribute [pageencoding] of the tag [processingdirective] must be a constant value")
        var cs: Charset? = CharsetUtil.toCharset(str)
        val psc: PageSourceCode? = if (data.srcCode is PageSourceCode) data.srcCode as PageSourceCode else null
        if (psc == null || cs.equals(psc.getCharset()) || CharsetUtil.UTF8.equals(psc.getCharset())) {
            cs = null
        }

        //
        if (cs != null) {
            throw ProcessingDirectiveException(data.srcCode, cs, null, data.srcCode.getWriteLog())
        }
        return null
    }
}