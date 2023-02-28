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
package lucee.runtime.functions.file

import lucee.commons.io.res.filter.ExtensionResourceFilter

class FileUploadAll : BIF(), Function {
    @Override
    @Throws(PageException::class)
    operator fun invoke(pc: PageContext?, args: Array<Object?>?): Object? {
        return if (args!!.size == 1) call(pc, Caster.toString(args[0])) else if (args.size == 2) call(pc, Caster.toString(args[0]), Caster.toString(args[1])) else if (args.size == 3) call(pc, Caster.toString(args[0]), Caster.toString(args[1]), Caster.toString(args[2])) else if (args.size == 4) call(pc, Caster.toString(args[0]), Caster.toString(args[1]), Caster.toString(args[2]), Caster.toBooleanValue(args[3])) else if (args.size == 5) call(pc, Caster.toString(args[0]), Caster.toString(args[1]), Caster.toString(args[2]), Caster.toBooleanValue(args[3]),
                args[4]) else if (args.size == 6) call(pc, Caster.toString(args[0]), Caster.toString(args[1]), Caster.toString(args[2]), Caster.toBooleanValue(args[3]),
                args[4], Caster.toString(args[5])) else if (args.size == 7) call(pc, Caster.toString(args[0]), Caster.toString(args[1]), Caster.toString(args[2]), Caster.toBooleanValue(args[3]), args[4], Caster.toString(args[5]), Caster.toString(args[6])) else if (args.size == 8) call(pc, Caster.toString(args[0]), Caster.toString(args[1]), Caster.toString(args[2]), Caster.toBooleanValue(args[3]),
                args[4], Caster.toString(args[5]), Caster.toString(args[6]), Caster.toString(args[7])) else if (args.size == 9) call(pc, Caster.toString(args[0]), Caster.toString(args[1]), Caster.toString(args[2]), Caster.toBooleanValue(args[3]),
                args[4], Caster.toString(args[5]), Caster.toString(args[6]), Caster.toString(args[7]), args[8]) else throw FunctionException(pc, "FileUploadAll", 1, 9, args.size)
    }

    companion object {
        @Throws(PageException::class)
        fun call(pc: PageContext?, destination: String?): Array? {
            return call(pc, destination, null, null, true, null, null, null, null, null)
        }

        @Throws(PageException::class)
        fun call(pc: PageContext?, destination: String?, accept: String?): Array? {
            return call(pc, destination, accept, null, true, null, null, null, null, null)
        }

        @Throws(PageException::class)
        fun call(pc: PageContext?, destination: String?, accept: String?, nameConflict: String?): Array? {
            return call(pc, destination, accept, nameConflict, true, null, null, null, null, null)
        }

        @Throws(PageException::class)
        fun call(pc: PageContext?, destination: String?, accept: String?, nameConflict: String?, strict: Boolean): Array? {
            return call(pc, destination, accept, nameConflict, strict, null, null, null, null, null)
        }

        @Throws(PageException::class)
        fun call(pc: PageContext?, destination: String?, accept: String?, nameConflict: String?, strict: Boolean, allowedExtensions: Object?): Array? {
            return call(pc, destination, accept, nameConflict, strict, allowedExtensions, null, null, null, null)
        }

        @Throws(PageException::class)
        fun call(pc: PageContext?, destination: String?, accept: String?, nameConflict: String?, strict: Boolean, allowedExtensions: Object?, blockedExtensions: Object?): Array? {
            return call(pc, destination, accept, nameConflict, strict, allowedExtensions, blockedExtensions, null, null, null)
        }

        @Throws(PageException::class)
        fun call(pc: PageContext?, destination: String?, accept: String?, nameConflict: String?, strict: Boolean, allowedExtensions: Object?, blockedExtensions: Object?, mode: String?): Array? {
            return call(pc, destination, accept, nameConflict, strict, allowedExtensions, blockedExtensions, mode, null, null)
        }

        @Throws(PageException::class)
        fun call(pc: PageContext?, destination: String?, accept: String?, nameConflict: String?, strict: Boolean, allowedExtensions: Object?, blockedExtensions: Object?, mode: String?, attributes: String?): Array? {
            return call(pc, destination, accept, nameConflict, strict, allowedExtensions, blockedExtensions, mode, attributes, null)
        }

        @Throws(PageException::class)
        fun call(pc: PageContext?, destination: String?, accept: String?, nameConflict: String?, strict: Boolean, allowedExtensions: Object?, blockedExtensions: Object?, mode: String?, attributes: String?, acl: Object?): Array? {
            val securityManager: SecurityManager = pc.getConfig().getSecurityManager()
            val nc: Int = FileUtil.toNameConflict(nameConflict)

            // mode
            val m: Int = FileTag.toMode(mode)

            // allowed extensions
            var allowedFilter: ExtensionResourceFilter? = null
            if (!StringUtil.isEmpty(allowedExtensions)) {
                allowedFilter = FileUtil.toExtensionFilter(allowedExtensions)
            }

            // blocked extensions
            var blockedFilter: ExtensionResourceFilter? = null
            if (!StringUtil.isEmpty(blockedFilter)) {
                blockedFilter = FileUtil.toExtensionFilter(blockedExtensions)
            }
            return FileTag.actionUploadAll(pc, securityManager, destination, nc, accept, allowedFilter, blockedFilter, strict, m, attributes, acl, null)
        }
    }
}