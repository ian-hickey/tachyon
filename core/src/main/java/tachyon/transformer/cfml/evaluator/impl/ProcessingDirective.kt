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
package tachyon.transformer.cfml.evaluator.impl

import java.nio.charset.Charset

/**
 * Prueft den Kontext des Tag `catch`. Das Tag darf sich nur direkt innerhalb des Tag
 * `try` befinden.
 */
class ProcessingDirective : EvaluatorSupport() {
    @Override
    @Throws(TemplateException::class)
    fun execute(config: Config?, tag: Tag?, libTag: TagLibTag?, flibs: Array<FunctionLib?>?, data: Data?): TagLib? {
        // dot notation
        var dotNotationUpperCase: Boolean? = null
        if (tag.containsAttribute("preservecase")) {
            val preservecase: Boolean = ASMUtil.getAttributeBoolean(tag, "preservecase", null)
                    ?: throw TemplateException(data.srcCode, "attribute [preserveCase] of the tag [processingdirective] must be a constant boolean value")
            dotNotationUpperCase = if (preservecase.booleanValue()) Boolean.FALSE else Boolean.TRUE
            if (dotNotationUpperCase === data.settings.dotNotationUpper) dotNotationUpperCase = null
        }

        // page encoding
        var cs: Charset? = null
        if (tag.containsAttribute("pageencoding")) {
            val str: String = ASMUtil.getAttributeString(tag, "pageencoding", null)
                    ?: throw TemplateException(data.srcCode, "attribute [pageencoding] of the tag [processingdirective] must be a constant value")
            cs = CharsetUtil.toCharset(str)
            val psc: PageSourceCode? = if (data.srcCode is PageSourceCode) data.srcCode as PageSourceCode else null
            if (psc == null || cs.equals(psc.getCharset())) {
                cs = null
            }
        }

        // execution log
        var exeLog: Boolean? = null
        if (tag.containsAttribute("executionlog")) {
            val strExeLog: String = ASMUtil.getAttributeString(tag, "executionlog", null)
            exeLog = Caster.toBoolean(strExeLog, null)
            if (exeLog == null) throw TemplateException(data.srcCode, "attribute [executionlog] of the tag [processingdirective] must be a constant boolean value")
            if (exeLog.booleanValue() === data.srcCode.getWriteLog()) exeLog = null
        }
        if (cs != null || exeLog != null || dotNotationUpperCase != null) {
            if (cs == null) {
                cs = if (data.srcCode is PageSourceCode) (data.srcCode as PageSourceCode).getCharset() else CharsetUtil.UTF8
            }
            if (exeLog == null) exeLog = if (data.srcCode.getWriteLog()) Boolean.TRUE else Boolean.FALSE
            if (dotNotationUpperCase == null) dotNotationUpperCase = data.settings.dotNotationUpper
            throw ProcessingDirectiveException(data.srcCode, cs, dotNotationUpperCase, exeLog)
        }
        return null
    }
}