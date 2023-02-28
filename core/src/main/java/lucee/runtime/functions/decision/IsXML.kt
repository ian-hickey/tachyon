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
package lucee.runtime.functions.decision

import java.io.StringReader

/**
 * Check if a String is a well-formed XML
 */
object IsXML : Function {
    fun call(pc: PageContext?, xml: Object?): Boolean {
        return if (xml is Node) true else try {
            XMLUtil.parse(InputSource(StringReader(Caster.toString(xml))), null, false)
            true
        } catch (e: Exception) {
            false
        }
    }
}