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
 * Implements the CFML Function directoryexists
 */
package tachyon.runtime.functions.system

import tachyon.commons.io.res.Resource

class DirectoryExists : BIF() {
    @Override
    @Throws(PageException::class)
    operator fun invoke(pc: PageContext?, args: Array<Object?>?): Object? {
        if (args!!.size == 1) return call(pc, Caster.toString(args[0]))
        if (args.size == 2) return call(pc, Caster.toString(args[0]), args[1])
        throw FunctionException(pc, "DirectoryExists", 1, 2, args.size)
    }

    companion object {
        private const val serialVersionUID = 4375183479006129959L
        @Throws(PageException::class)
        fun call(pc: PageContext?, path: String?): Boolean {
            return call(pc, path, pc.getConfig().allowRealPath())
        }

        @Throws(PageException::class)
        fun call(pc: PageContext?, path: String?, oAllowRealPath: Object?): Boolean {
            if (StringUtil.isEmpty(path, true)) return false
            val file: Resource
            val allowRealPath: Boolean = if (oAllowRealPath == null) pc.getConfig().allowRealPath() else Caster.toBooleanValue(oAllowRealPath)
            if (allowRealPath) {
                file = ResourceUtil.toResourceNotExisting(pc, path, allowRealPath, false)
                // TODO das else braucht es eigentlich nicht mehr
            } else {
                // ARP
                file = pc.getConfig().getResource(path)
                if (file != null && !file.isAbsolute()) return false
            }
            pc.getConfig().getSecurityManager().checkFileLocation(file)
            return file.isDirectory()
        }
    }
}