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
package tachyon.commons.digest

import java.io.InputStream

object MD5Checksum {
    @Throws(Exception::class)
    fun createChecksum(res: Resource): ByteArray {
        val `is`: InputStream = res.getInputStream()
        return try {
            val buffer = ByteArray(1024)
            val complete: MessageDigest = MessageDigest.getInstance("MD5")
            var numRead: Int
            do {
                numRead = `is`.read(buffer)
                if (numRead > 0) {
                    complete.update(buffer, 0, numRead)
                }
            } while (numRead != -1)
            complete.digest()
        } finally {
            IOUtil.close(`is`)
        }
    }

    // see this How-to for a faster way to convert
    // a byte array to a HEX string
    @Throws(Exception::class)
    fun getMD5Checksum(res: Resource): String {
        val b = createChecksum(res)
        var result = ""
        for (i in b.indices) {
            result += Integer.toString((b[i] and 0xff) + 0x100, 16).substring(1)
        }
        return result
    }
}