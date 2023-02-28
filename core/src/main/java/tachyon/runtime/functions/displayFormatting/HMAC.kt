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
package tachyon.runtime.functions.displayFormatting

import java.nio.charset.Charset

object HMAC : Function {
    private const val serialVersionUID = -1999122154087043893L
    @Throws(PageException::class)
    fun call(pc: PageContext?, oMessage: Object?, oKey: Object?): String? {
        return call(pc, oMessage, oKey, null, null)
    }

    @Throws(PageException::class)
    fun call(pc: PageContext?, oMessage: Object?, oKey: Object?, algorithm: String?): String? {
        return call(pc, oMessage, oKey, algorithm, null)
    }

    @Throws(PageException::class)
    fun call(pc: PageContext?, oMessage: Object?, oKey: Object?, algorithm: String?, charset: String?): String? {
        // charset
        var algorithm = algorithm
        val cs: Charset
        cs = if (StringUtil.isEmpty(charset, true)) pc.getWebCharset() else CharsetUtil.toCharset(charset)

        // message
        var msg = toBinary(oMessage, cs)

        // message
        val key = toBinary(oKey, cs)

        // algorithm
        if (StringUtil.isEmpty(algorithm, true)) algorithm = "HmacMD5"
        val sk: SecretKey = SecretKeySpec(key, algorithm)
        return try {
            val mac: Mac = Mac.getInstance(algorithm)
            mac.init(sk)
            mac.reset()
            mac.update(msg)
            msg = mac.doFinal()
            MD5.stringify(msg).toUpperCase()
        } catch (e: Exception) {
            throw Caster.toPageException(e)
        }
    }

    @Throws(PageException::class)
    private fun toBinary(obj: Object?, cs: Charset?): ByteArray? {
        return if (Decision.isBinary(obj)) {
            Caster.toBinary(obj)
        } else Caster.toString(obj).getBytes(cs)
    }
}