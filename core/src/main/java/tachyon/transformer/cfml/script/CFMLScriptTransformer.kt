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

import tachyon.runtime.exp.TemplateException

class CFMLScriptTransformer : AbstrCFMLScriptTransformer(), TagDependentBodyTransformer {
    @Override
    @Throws(TemplateException::class)
    fun transform(data: Data?, surroundingTagName: String?): Body? {
        val isCFC = data.page is Page && data.page.isComponent()
        val isInterface = data.page is Page && data.page.isInterface()
        val ed: Data = init(data)
        val oldAllowLowerThan: Boolean = ed.allowLowerThan
        ed.insideFunction = false
        val oldInsideFunction: Boolean = ed.insideFunction
        val oldTagName: String = ed.tagName
        val oldIsCFC: Boolean = ed.isCFC
        val oldIsInterface: Boolean = ed.isInterface
        ed.allowLowerThan = true
        ed.insideFunction = false
        ed.tagName = surroundingTagName
        ed.isCFC = isCFC
        ed.isInterface = isInterface
        return try {
            statements(ed)
        } finally {
            ed.allowLowerThan = oldAllowLowerThan
            ed.insideFunction = oldInsideFunction
            ed.tagName = oldTagName
            ed.isCFC = oldIsCFC
            ed.isInterface = oldIsInterface
        }
    }

    @Override
    @Throws(TemplateException::class)
    fun expression(data: Data?): Expression? {
        val expr: Expression?
        expr = super.expression(data)
        comments(data)
        return expr
    }
}