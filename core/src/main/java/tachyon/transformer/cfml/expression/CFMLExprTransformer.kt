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
package tachyon.transformer.cfml.expression

import tachyon.runtime.exp.TemplateException

class CFMLExprTransformer : AbstrCFMLScriptTransformer(), ExprTransformer {
    @Override
    @Throws(TemplateException::class)
    fun transformAsString(data: Data?): Expression? {
        val alt: Boolean = data.allowLowerThan
        data.allowLowerThan = false
        val ed: Data = init(data)
        return try {
            transformAsString(ed, arrayOf<String?>(" ", ">", "/>"))
        } finally {
            data.allowLowerThan = alt
        }
    }

    @Override
    @Throws(TemplateException::class)
    fun transform(data: Data?): Expression? {
        val alt: Boolean = data.allowLowerThan
        data.allowLowerThan = false
        val ed: Data = init(data)
        return try {
            comments(ed)
            assignOp(ed)
        } finally {
            data.allowLowerThan = alt
        }
    }
}