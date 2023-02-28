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
package lucee.runtime.net.ftp

import org.apache.commons.net.ftp.FTPFile

/**
 *
 */
object FTPConstant {
    /**
     * Field `TRANSFER_MODE_AUTO`
     */
    const val TRANSFER_MODE_AUTO: Short = 0

    /**
     * Field `TRANSFER_MODE_BINARY`
     */
    const val TRANSFER_MODE_BINARY: Short = 1

    /**
     * Field `TRANSFER_MODE_ASCCI`
     */
    const val TRANSFER_MODE_ASCCI: Short = 2

    /**
     * Field `PERMISSION_READ`
     */
    const val PERMISSION_READ: Short = 4

    /**
     * Field `PERMISSION_WRITE`
     */
    const val PERMISSION_WRITE: Short = 2

    /**
     * Field `PERMISSION_EXECUTE`
     */
    const val PERMISSION_EXECUTE: Short = 1

    /**
     * Field `ACCESS_WORLD`
     */
    const val ACCESS_WORLD: Short = 1

    /**
     * Field `ACCESS_GROUP`
     */
    const val ACCESS_GROUP: Short = 10

    /**
     * Field `ACCESS_USER`
     */
    const val ACCESS_USER: Short = 100

    /**
     * @param type
     * @return file type as String
     */
    fun getTypeAsString(type: Int): String? {
        if (type == FTPFile.DIRECTORY_TYPE) return "directory" else if (type == FTPFile.SYMBOLIC_LINK_TYPE) return "link" else if (type == FTPFile.UNKNOWN_TYPE) return "unknown" else if (type == FTPFile.FILE_TYPE) return "file"
        return "unknown"
    }

    /**
     * @param file
     * @return permission as integer
     */
    fun getPermissionASInteger(file: FTPFile?): Integer? {
        var rtn = 0
        // world
        if (file.hasPermission(FTPFile.WORLD_ACCESS, FTPFile.READ_PERMISSION)) rtn += ACCESS_WORLD * PERMISSION_READ
        if (file.hasPermission(FTPFile.WORLD_ACCESS, FTPFile.WRITE_PERMISSION)) rtn += ACCESS_WORLD * PERMISSION_WRITE
        if (file.hasPermission(FTPFile.WORLD_ACCESS, FTPFile.EXECUTE_PERMISSION)) rtn += ACCESS_WORLD * PERMISSION_EXECUTE

        // group
        if (file.hasPermission(FTPFile.GROUP_ACCESS, FTPFile.READ_PERMISSION)) rtn += ACCESS_GROUP * PERMISSION_READ
        if (file.hasPermission(FTPFile.GROUP_ACCESS, FTPFile.WRITE_PERMISSION)) rtn += ACCESS_GROUP * PERMISSION_WRITE
        if (file.hasPermission(FTPFile.GROUP_ACCESS, FTPFile.EXECUTE_PERMISSION)) rtn += ACCESS_GROUP * PERMISSION_EXECUTE

        // user
        if (file.hasPermission(FTPFile.USER_ACCESS, FTPFile.READ_PERMISSION)) rtn += ACCESS_USER * PERMISSION_READ
        if (file.hasPermission(FTPFile.USER_ACCESS, FTPFile.WRITE_PERMISSION)) rtn += ACCESS_USER * PERMISSION_WRITE
        if (file.hasPermission(FTPFile.USER_ACCESS, FTPFile.EXECUTE_PERMISSION)) rtn += ACCESS_USER * PERMISSION_EXECUTE
        return Integer.valueOf(rtn)
    }

    fun setPermission(file: FTPFile?, mode: Int) {

        // world
        file.setPermission(FTPFile.WORLD_ACCESS, FTPFile.EXECUTE_PERMISSION, 1 and mode > 0)
        file.setPermission(FTPFile.WORLD_ACCESS, FTPFile.WRITE_PERMISSION, 2 and mode > 0)
        file.setPermission(FTPFile.WORLD_ACCESS, FTPFile.READ_PERMISSION, 4 and mode > 0)

        // group
        file.setPermission(FTPFile.GROUP_ACCESS, FTPFile.EXECUTE_PERMISSION, 8 and mode > 0)
        file.setPermission(FTPFile.GROUP_ACCESS, FTPFile.WRITE_PERMISSION, 16 and mode > 0)
        file.setPermission(FTPFile.GROUP_ACCESS, FTPFile.READ_PERMISSION, 32 and mode > 0)

        // user
        file.setPermission(FTPFile.USER_ACCESS, FTPFile.EXECUTE_PERMISSION, 64 and mode > 0)
        file.setPermission(FTPFile.USER_ACCESS, FTPFile.WRITE_PERMISSION, 128 and mode > 0)
        file.setPermission(FTPFile.USER_ACCESS, FTPFile.READ_PERMISSION, 256 and mode > 0)
    }

    fun setPermissionOld(file: FTPFile?, mode: Int) {
        var mode = mode
        val mu = mode / 100
        mode = mode - mu * 100
        val mg = mode / 10
        mode = mode - mg * 10

        // print.e(mu+"-"+mg+"-"+mode);

        // world
        file.setPermission(FTPFile.WORLD_ACCESS, FTPFile.READ_PERMISSION, PERMISSION_READ and mode > 0)
        file.setPermission(FTPFile.WORLD_ACCESS, FTPFile.WRITE_PERMISSION, PERMISSION_WRITE and mode > 0)
        file.setPermission(FTPFile.WORLD_ACCESS, FTPFile.EXECUTE_PERMISSION, PERMISSION_EXECUTE and mode > 0)

        // group
        file.setPermission(FTPFile.GROUP_ACCESS, FTPFile.READ_PERMISSION, PERMISSION_READ and mg > 0)
        file.setPermission(FTPFile.GROUP_ACCESS, FTPFile.WRITE_PERMISSION, PERMISSION_WRITE and mg > 0)
        file.setPermission(FTPFile.GROUP_ACCESS, FTPFile.EXECUTE_PERMISSION, PERMISSION_EXECUTE and mg > 0)

        // user
        file.setPermission(FTPFile.USER_ACCESS, FTPFile.READ_PERMISSION, PERMISSION_READ and mu > 0)
        file.setPermission(FTPFile.USER_ACCESS, FTPFile.WRITE_PERMISSION, PERMISSION_WRITE and mu > 0)
        file.setPermission(FTPFile.USER_ACCESS, FTPFile.EXECUTE_PERMISSION, PERMISSION_EXECUTE and mu > 0)
    }
}