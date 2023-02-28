/**
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
package lucee.runtime.functions.file

import lucee.commons.io.res.Resource

class FileTouch : BIF() {
    @Override
    @Throws(PageException::class)
    operator fun invoke(pc: PageContext?, args: Array<Object?>?): Object? {
        if (args!!.size == 1) return call(pc, args[0], true) else if (args.size == 2) return call(pc, args[0], Caster.toBooleanValue(args[1]))
        throw FunctionException(pc, "FileTouch", 1, 2, args.size)
    }

    companion object {
        private const val serialVersionUID = -7478227658810128723L
        @Throws(PageException::class)
        fun call(pc: PageContext?, file: Object?, createPath: Boolean): String? {
            val res: Resource = Caster.toResource(pc, file, false)
            FileTag.actionTouch(pc, pc.getConfig().getSecurityManager(), res, null, createPath, null, -1, null)
            return null
        }
    }
}