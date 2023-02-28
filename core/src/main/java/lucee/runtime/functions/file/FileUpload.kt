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

class FileUpload : BIF(), Function {
    @Override
    @Throws(PageException::class)
    operator fun invoke(pc: PageContext?, args: Array<Object?>?): Object? {
        return if (args!!.size == 1) call(pc, Caster.toString(args[0])) else if (args.size == 2) call(pc, Caster.toString(args[0]), Caster.toString(args[1])) else if (args.size == 3) call(pc, Caster.toString(args[0]), Caster.toString(args[1]), Caster.toString(args[2])) else if (args.size == 4) call(pc, Caster.toString(args[0]), Caster.toString(args[1]), Caster.toString(args[2]), Caster.toString(args[3])) else if (args.size == 5) call(pc, Caster.toString(args[0]), Caster.toString(args[1]), Caster.toString(args[2]), Caster.toString(args[3]),
                Caster.toBooleanValue(args[4])) else if (args.size == 6) call(pc, Caster.toString(args[0]), Caster.toString(args[1]), Caster.toString(args[2]), Caster.toString(args[3]),
                Caster.toBooleanValue(args[4]), args[5]) else if (args.size == 7) call(pc, Caster.toString(args[0]), Caster.toString(args[1]), Caster.toString(args[2]), Caster.toString(args[3]), Caster.toBooleanValue(args[4]), args[5], Caster.toString(args[6])) else if (args.size == 8) call(pc, Caster.toString(args[0]), Caster.toString(args[1]), Caster.toString(args[2]), Caster.toString(args[3]),
                Caster.toBooleanValue(args[4]), args[5], Caster.toString(args[6]), Caster.toString(args[7])) else if (args.size == 9) call(pc, Caster.toString(args[0]), Caster.toString(args[1]), Caster.toString(args[2]), Caster.toString(args[3]),
                Caster.toBooleanValue(args[4]), args[5], Caster.toString(args[6]), Caster.toString(args[7]), Caster.toString(args[8])) else if (args.size == 10) call(pc, Caster.toString(args[0]), Caster.toString(args[1]), Caster.toString(args[2]), Caster.toString(args[3]),
                Caster.toBooleanValue(args[4]), args[5], Caster.toString(args[6]), Caster.toString(args[7]), Caster.toString(args[8]), args[9]) else throw FunctionException(pc, "FileUpload", 1, 10, args.size)
    }

    companion object {
        private const val serialVersionUID = 8289325119924649321L
        @Throws(PageException::class)
        fun call(pc: PageContext?, destination: String?): Struct? {
            return call(pc, destination, null, null, null, true, null, null, null)
        }

        @Throws(PageException::class)
        fun call(pc: PageContext?, destination: String?, fileField: String?): Struct? {
            return call(pc, destination, fileField, null, null, true, null, null, null)
        }

        @Throws(PageException::class)
        fun call(pc: PageContext?, destination: String?, fileField: String?, accept: String?): Struct? {
            return call(pc, destination, fileField, accept, null, true, null, null, null)
        }

        @Throws(PageException::class)
        fun call(pc: PageContext?, destination: String?, fileField: String?, accept: String?, nameConflict: String?): Struct? {
            return call(pc, destination, fileField, accept, nameConflict, true, null, null, null, null, null)
        }

        @Throws(PageException::class)
        fun call(pc: PageContext?, destination: String?, fileField: String?, accept: String?, nameConflict: String?, strict: Boolean): Struct? {
            return call(pc, destination, fileField, accept, nameConflict, strict, null, null, null, null, null)
        }

        @Throws(PageException::class)
        fun call(pc: PageContext?, destination: String?, fileField: String?, accept: String?, nameConflict: String?, strict: Boolean, allowedExtensions: Object?): Struct? {
            return call(pc, destination, fileField, accept, nameConflict, strict, allowedExtensions, null, null, null, null)
        }

        @Throws(PageException::class)
        fun call(pc: PageContext?, destination: String?, fileField: String?, accept: String?, nameConflict: String?, strict: Boolean, allowedExtensions: Object?, blockedExtensions: Object?): Struct? {
            return call(pc, destination, fileField, accept, nameConflict, strict, allowedExtensions, blockedExtensions, null, null, null)
        }

        @Throws(PageException::class)
        fun call(pc: PageContext?, destination: String?, fileField: String?, accept: String?, nameConflict: String?, strict: Boolean, allowedExtensions: Object?, blockedExtensions: Object?, mode: String?): Struct? {
            return call(pc, destination, fileField, accept, nameConflict, strict, allowedExtensions, blockedExtensions, mode, null, null)
        }

        @Throws(PageException::class)
        fun call(pc: PageContext?, destination: String?, fileField: String?, accept: String?, nameConflict: String?, strict: Boolean, allowedExtensions: Object?, blockedExtensions: Object?, mode: String?, attributes: String?): Struct? {
            return call(pc, destination, fileField, accept, nameConflict, strict, allowedExtensions, blockedExtensions, mode, attributes, null)
        }

        @Throws(PageException::class)
        fun call(pc: PageContext?, destination: String?, fileField: String?, accept: String?, nameConflict: String?, strict: Boolean, allowedExtensions: Object?, blockedExtensions: Object?, mode: String?, attributes: String?, acl: Object?): Struct? {
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
            return FileTag.actionUpload(pc, securityManager, fileField, destination, nc, accept, allowedFilter, blockedFilter, strict, m, attributes, acl, null)
        }
    }
}