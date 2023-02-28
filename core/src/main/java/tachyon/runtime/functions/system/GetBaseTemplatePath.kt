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
/**
 * Implements the CFML Function getbasetemplatepath
 */
package tachyon.runtime.functions.system

import tachyon.runtime.PageContext

class GetBaseTemplatePath : BIF() {
    @Override
    @Throws(PageException::class)
    operator fun invoke(pc: PageContext?, args: Array<Object?>?): Object? {
        if (args!!.size == 0) return call(pc)
        throw FunctionException(pc, "GetBaseTemplatePath", 0, 0, args.size)
    }

    companion object {
        private const val serialVersionUID = -6810643607690049685L
        @Throws(PageException::class)
        fun call(pc: PageContext?): String? {
            // pc.getTemplatePath();
            var ps: PageSource = pc.getBasePageSource()
            if (ps == null) {
                ps = (pc as PageContextImpl?).getPageSource(1)
                if (ps == null) throw ApplicationException("current context does not have a template it is based on")
            }
            return ps.getResourceTranslated(pc).getAbsolutePath()
        }
    }
}