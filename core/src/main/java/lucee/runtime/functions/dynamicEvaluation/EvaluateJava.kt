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
 * Implements the CFML Function UnserializeJava
 */
package lucee.runtime.functions.dynamicEvaluation

import java.io.ByteArrayInputStream

object EvaluateJava : Function {
    private const val serialVersionUID = 2665025287805145492L
    @Throws(PageException::class)
    fun call(pc: PageContext?, stringOrBinary: Object?): Object? {
        // Binary
        if (Decision.isBinary(stringOrBinary)) {
            var `is`: InputStream? = null
            return try {
                `is` = ByteArrayInputStream(Caster.toBinary(stringOrBinary))
                JavaConverter.deserialize(`is`)
            } catch (e: Exception) {
                throw Caster.toPageException(e)
            } finally {
                try {
                    IOUtil.close(`is`)
                } catch (e: IOException) {
                    throw Caster.toPageException(e)
                }
            }
        }

        // STring
        return try {
            JavaConverter.deserialize(Caster.toString(stringOrBinary))
        } catch (e: Exception) {
            throw Caster.toPageException(e)
        }
    }
}