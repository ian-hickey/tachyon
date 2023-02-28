/**
 * Copyright (c) 2015, Tachyon Assosication Switzerland. All rights reserved.
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
package tachyon.runtime.functions.dynamicEvaluation

import tachyon.commons.lang.StringUtil

object Render : Function {
    private const val serialVersionUID = 669811806780804244L
    @Throws(PageException::class)
    fun call(pc: PageContext?, cfml: String?): String? {
        return Renderer.tag(pc, cfml, pc.getCurrentTemplateDialect(), false, pc.ignoreScopes()).getOutput()
    }

    @Throws(PageException::class)
    fun call(pc: PageContext?, cfml: String?, dialect: String?): String? {
        return if (StringUtil.isEmpty(dialect, true)) call(pc, cfml) else Renderer.tag(pc, cfml, ConfigWebUtil.toDialect(dialect.trim(), CFMLEngine.DIALECT_CFML), false, pc.ignoreScopes()).getOutput()
    }
}