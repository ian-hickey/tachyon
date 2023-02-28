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
package tachyon.runtime.functions.file

import tachyon.commons.io.res.Resource

class GetCanonicalPath : BIF() {
    @Override
    @Throws(PageException::class)
    operator fun invoke(pc: PageContext?, args: Array<Object?>?): Object? {
        return call(pc, Caster.toString(args!![0]))
    }

    companion object {
        private const val serialVersionUID = -7516439220584467382L
        fun call(pc: PageContext?, path: String?): String? {
            // we only add a slash if there was already one (for FuseBox), otherwise we cannot know for sure it
            // is a directory (when path not exists ....)
            var path = path
            var addEndSep: Boolean = StringUtil.endsWith(path, '/', '\\')
            val res: Resource = ResourceUtil.toResourceNotExisting(pc, path)
            if (!addEndSep && res.isDirectory()) addEndSep = true
            path = ResourceUtil.getCanonicalPathEL(res)
            return if (addEndSep && !StringUtil.endsWith(path, '/', '\\')) {
                path + ResourceUtil.getSeparator(res.getResourceProvider())
            } else path
        }
    }
}