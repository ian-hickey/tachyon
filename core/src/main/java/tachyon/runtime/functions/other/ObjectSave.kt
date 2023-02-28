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
package tachyon.runtime.functions.other

import java.io.ByteArrayInputStream

object ObjectSave {
    @Throws(PageException::class)
    fun call(pc: PageContext?, input: Object?): Object? {
        return call(pc, input, null)
    }

    @Throws(PageException::class)
    fun call(pc: PageContext?, input: Object?, filepath: String?): Object? {
        if (input !is Serializable) throw ApplicationException("can only serialize object from type Serializable")
        val baos = ByteArrayOutputStream()
        return try {
            JavaConverter.serialize(input as Serializable?, baos)
            val barr: ByteArray = baos.toByteArray()

            // store to file
            if (!StringUtil.isEmpty(filepath, true)) {
                val res: Resource = ResourceUtil.toResourceNotExisting(pc, filepath)
                pc.getConfig().getSecurityManager().checkFileLocation(res)
                IOUtil.copy(ByteArrayInputStream(barr), res, true)
            }
            barr
        } catch (e: IOException) {
            throw Caster.toPageException(e)
        }
    }
}