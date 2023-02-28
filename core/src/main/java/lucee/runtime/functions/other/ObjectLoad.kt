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
package lucee.runtime.functions.other

import java.io.ByteArrayInputStream

object ObjectLoad {
    @Throws(PageException::class)
    fun call(pc: PageContext?, input: Object?): Object? {
        val `is`: InputStream?
        var closeStream = true
        if (Decision.isBinary(input)) {
            `is` = ByteArrayInputStream(Caster.toBinary(input))
        } else if (input is InputStream) {
            `is` = input as InputStream?
            closeStream = false
        } else {
            val res: Resource = ResourceUtil.toResourceExisting(pc, Caster.toString(input))
            pc.getConfig().getSecurityManager().checkFileLocation(res)
            `is` = try {
                res.getInputStream()
            } catch (e: IOException) {
                throw Caster.toPageException(e)
            }
        }
        return try {
            JavaConverter.deserialize(`is`)
        } catch (e: Exception) {
            throw Caster.toPageException(e)
        } finally {
            if (closeStream) {
                try {
                    IOUtil.close(`is`)
                } catch (e: IOException) {
                    throw Caster.toPageException(e)
                }
            }
        }
    }
}